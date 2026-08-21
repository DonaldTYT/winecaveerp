package com.uniinformation.dynamic.winecave;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;

import com.kyoko.common.ReturnMsg;
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

/** Controls posting and unposting of a wc.DeliveryNote. */
public class DeliveryPostControl extends BiActionHandler implements JxActionListener {
	private static final String POST = "post";
	private static final String UNPOST = "unpost";

	public DeliveryPostControl() {
		super(null);
	}

	public DeliveryPostControl(ZkBiComposerBase p_bibase) {
		super(p_bibase);
	}

	@Override
	public void actionPerformed(JxField field) {
		final JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		final BiResult delivery = jxf.getBr();
		if(jxf.isDirty()) {
			Messagebox.show("Please save or cancel the current changes before posting.");
			return;
		}
		if(delivery.getCellInt("stm_mrg") <= 0) {
			Messagebox.show("Please save the Delivery Note before posting.");
			return;
		}

		try {
			final ZkForm form = new ZkForm(null,"zkf/winecave/DeliveryPostControl.zul");
			final Radiogroup operationGroup = (Radiogroup) form.getComponent("deliveryOperation");
			final Radio postRadio = (Radio) form.getComponent("opPost");
			final Radio unpostRadio = (Radio) form.getComponent("opUnpost");

			boolean canPost = delivery.getSessionHelper().hasAccessRight("#stmovconfirm");
			boolean canUnpost = delivery.getSessionHelper().hasAccessRight("#stmovunconfirm");
			postRadio.setDisabled(!canPost);
			unpostRadio.setDisabled(!canUnpost);
			if(!canPost && canUnpost) operationGroup.setSelectedItem(unpostRadio);

			form.doModal(new CellCollection(),new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if("btCancel".equals(event.getTarget().getId())) {
						form.exitModal();
						return;
					}
					if(!"btProceed".equals(event.getTarget().getId())) return;

					String operation = operationGroup.getSelectedItem().getValue();
					form.exitModal();
					confirmAndExecute(jxf,delivery,operation);
				}
			});
		} catch(Exception ex) {
			UniLog.log(ex);
			Messagebox.show("Unable to open Delivery Note posting control: " + ex.getMessage());
		}
	}

	private void confirmAndExecute(final JxZkBiBase jxf,final BiResult delivery,
			final String operation) {
		String prompt = POST.equals(operation)
				? "Post this Delivery Note transaction?"
				: "UnPost this Delivery Note transaction?";
		Messagebox.show(prompt,"Confirm",Messagebox.YES | Messagebox.NO,
				Messagebox.EXCLAMATION,new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				if(((Integer) event.getData()) == Messagebox.YES)
					executeOperation(jxf,delivery,operation);
			}
		});
	}

	private void executeOperation(JxZkBiBase jxf,BiResult delivery,String operation) {
		RpcClient rpc = jxf.getRpcClient();
		try {
			String company = Erpv4Config.getDefaultCoCode(delivery.getSessionHelper());
			rpc.callSegment("setCocodeBaseccy",new VectorUtil()
					.addElement(company)
					.addElement(Erpv4Config.getBaseCcy(delivery.getSessionHelper(),company))
					.toVector());
			String segment = POST.equals(operation)
					? "erpv3_post_delivery" : "erpv3_unpost_delivery";
			Value value = rpc.callSegment(segment,new VectorUtil()
					.addElement(delivery.getCellInt("stm_mrg")).toVector());
			if(value != null && value.toString().startsWith("OK")) {
				if(biBase != null) biBase.reload(delivery);
				Messagebox.show(value.toString().substring(4).trim());
			} else {
				Messagebox.show(value == null ? "Delivery posting failed." : value.toString());
			}
		} catch(Exception ex) {
			UniLog.log(ex);
			Messagebox.show("Delivery posting failed: " + ex.getMessage());
		} finally {
			rpc.close();
		}
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
