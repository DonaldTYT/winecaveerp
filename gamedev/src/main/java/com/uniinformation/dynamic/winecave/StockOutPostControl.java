package com.uniinformation.dynamic.winecave;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkf.ZkForm;

/** Controls posting of a wc.StockOut and its individual consignment POs. */
public class StockOutPostControl extends BiActionHandler implements JxActionListener {
	private static final String SUBLINK = "wc.StmpostExt";
	private static final String POST = "post";
	private static final String UNPOST = "unpost";
	private static final String CONFIRM_PO = "confirm_po";
	private static final String UNCONFIRM_PO = "unconfirm_po";

	public StockOutPostControl() {
		super(null);
	}

	public StockOutPostControl(ZkBiComposerBase p_bibase) {
		super(p_bibase);
	}

	@Override
	public void actionPerformed(JxField field) {
		final JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		final BiResult stockOut = jxf.getBr();
		if(jxf.isDirty()) {
			Messagebox.show("Please save or cancel the current changes before posting.");
			return;
		}
		if(stockOut.getCellInt("stm_mrg") <= 0) {
			Messagebox.show("Please save the StockOut record before posting.");
			return;
		}

		try {
			final ZkForm form = new ZkForm(null,"zkf/winecave/StockOutPostControl.zul");
			final Radiogroup operationGroup = (Radiogroup) form.getComponent("stockOutOperation");
			final Radio postRadio = (Radio) form.getComponent("opPost");
			final Radio unpostRadio = (Radio) form.getComponent("opUnpost");
			final Radio confirmPoRadio = (Radio) form.getComponent("opConfirmPo");
			final Radio unconfirmPoRadio = (Radio) form.getComponent("opUnconfirmPo");
			final Listbox consigneeList = (Listbox) form.getComponent("consigneeList");

			boolean canPost = stockOut.getSessionHelper().hasAccessRight("#stmovconfirm");
			boolean canUnpost = stockOut.getSessionHelper().hasAccessRight("#stmovunconfirm");
			postRadio.setDisabled(!canPost);
			confirmPoRadio.setDisabled(!canPost);
			unpostRadio.setDisabled(!canUnpost);
			unconfirmPoRadio.setDisabled(!canUnpost);
			if(!canPost && canUnpost) operationGroup.setSelectedItem(unpostRadio);

			BiResult poResult = stockOut.getSubLink(SUBLINK);
			for(int i = 0;i < poResult.getRowCount();i++) {
				if(poResult.isMarkedDelete(poResult.getTrStatObj(i))) continue;
				BiCellCollection po = poResult.getRowCollectionV(i);
				Listitem item = new Listitem();
				item.setValue(po);
				item.appendChild(new Listcell(po.getCellString("stmp_cocode")));
				item.appendChild(new Listcell(po.getCellString("stmp_sno")));
				item.appendChild(new Listcell(String.valueOf(po.getCellDouble("stmp_amount"))));
				item.appendChild(new Listcell(isBlank(po.getCellString("stmp_sno"))
						? "Unconfirmed" : "Confirmed"));
				consigneeList.appendChild(item);
			}

			form.doModal(new CellCollection(),new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if("btCancel".equals(event.getTarget().getId())) {
						form.exitModal();
						return;
					}
					if(!"btProceed".equals(event.getTarget().getId())) return;

					String operation = operationGroup.getSelectedItem().getValue();
					List<BiCellCollection> selected = new ArrayList<BiCellCollection>();
					if(CONFIRM_PO.equals(operation) || UNCONFIRM_PO.equals(operation)) {
						for(Listitem item : consigneeList.getItems())
							if(item.isSelected()) selected.add((BiCellCollection) item.getValue());
						if(selected.isEmpty()) {
							Messagebox.show("Please select at least one consignee PO.");
							return;
						}
					}
					form.exitModal();
					confirmAndExecute(jxf,stockOut,operation,selected);
				}
			});
		} catch(Exception ex) {
			UniLog.log(ex);
			Messagebox.show("Unable to open StockOut posting control: " + ex.getMessage());
		}
	}

	private void confirmAndExecute(final JxZkBiBase jxf,final BiResult stockOut,
			final String operation,final List<BiCellCollection> selected) {
		String prompt;
		if(POST.equals(operation)) prompt = "Post this StockOut transaction?";
		else if(UNPOST.equals(operation)) prompt = "UnPost this StockOut transaction?";
		else if(CONFIRM_PO.equals(operation))
			prompt = "Confirm " + selected.size() + " selected consignment PO(s)?";
		else prompt = "Unconfirm " + selected.size() + " selected consignment PO(s)?";

		Messagebox.show(prompt,"Confirm",Messagebox.YES | Messagebox.NO,
				Messagebox.EXCLAMATION,new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				if(((Integer) event.getData()) == Messagebox.YES)
					executeOperation(jxf,stockOut,operation,selected);
			}
		});
	}

	private void executeOperation(JxZkBiBase jxf,BiResult stockOut,String operation,
			List<BiCellCollection> selected) {
		RpcClient rpc = jxf.getRpcClient();
		int succeeded = 0;
		List<String> failures = new ArrayList<String>();
		try {
			String company = Erpv4Config.getDefaultCoCode(stockOut.getSessionHelper());
			rpc.callSegment("setCocodeBaseccy",new VectorUtil()
					.addElement(company)
					.addElement(Erpv4Config.getBaseCcy(stockOut.getSessionHelper(),company))
					.toVector());
			int mrg = stockOut.getCellInt("stm_mrg");
			if(POST.equals(operation) || UNPOST.equals(operation)) {
				String segment = POST.equals(operation)
						? "erpv3_post_miscout" : "erpv3_unpost_miscout";
				Value value = rpc.callSegment(segment,new VectorUtil().addElement(mrg).toVector());
				if(isOk(value)) succeeded++;
				else failures.add(String.valueOf(value));
			} else {
				String segment = CONFIRM_PO.equals(operation)
						? "erpv3_confirm_cnpo" : "erpv3_unconfirm_cnpo";
				for(BiCellCollection po : selected) {
					String cocode = po.getCellString("stmp_cocode");
					Value value = rpc.callSegment(segment,new VectorUtil()
							.addElement(mrg).addElement(cocode).toVector());
					if(isOk(value)) succeeded++;
					else failures.add(cocode + ": " + String.valueOf(value));
				}
			}
		} catch(Exception ex) {
			UniLog.log(ex);
			failures.add(ex.getMessage());
		} finally {
			rpc.close();
		}

		if(succeeded > 0 && biBase != null) {
			try {
				biBase.reload(stockOut);
			} catch(Exception ex) {
				UniLog.log(ex);
				failures.add("Operation completed, but the form could not be refreshed.");
			}
		}
		String summary = succeeded + " operation(s) completed.";
		if(!failures.isEmpty()) summary += "\nFailed: " + String.join("; ",failures);
		Messagebox.show(summary);
	}

	private boolean isOk(Value value) {
		return value != null && value.toString().startsWith("OK");
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	@Override public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		return ReturnMsg.defaultOk;
	}
	@Override public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
		return ReturnMsg.defaultOk;
	}
	@Override public ReturnMsg afterAction(BiResult p_result) {
		return ReturnMsg.defaultOk;
	}
	@Override public boolean isVisible(BiResult p_br,boolean p_isBatch) {
		if(p_br == null || p_isBatch) return false;
		return p_br.getSessionHelper().hasAccessRight("#stmovconfirm")
				|| p_br.getSessionHelper().hasAccessRight("#stmovunconfirm");
	}
	@Override public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		return p_br == null || p_isBatch || p_br.inBeginWork();
	}
}
