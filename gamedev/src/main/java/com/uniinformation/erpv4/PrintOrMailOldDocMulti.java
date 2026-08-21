package com.uniinformation.erpv4;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radiogroup;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkf.ZkForm;

/**
 * Common old-document action which lets the user choose the output method
 * (print or email) and the scope (all currently available documents or a
 * selected subset).
 *
 * Concrete document handlers supply the sublink, print segment and ZUL forms,
 * then implement {@link #outputDocuments(JxZkBiBase, List, boolean)}.
 */
public abstract class PrintOrMailOldDocMulti extends PrintOldDocMulti {

	protected PrintOrMailOldDocMulti() {
		super(null);
	}

	protected PrintOrMailOldDocMulti(ZkBiComposerBase p_bibase) {
		super(p_bibase);
	}

	protected abstract String getInvoiceSublinkName();
	protected abstract String getPrintSegmentName();
	protected abstract String getSettingsFormPath();
	protected abstract String getSelectionFormPath();

	/** Execute the selected documents using print (false) or email (true). */
	protected abstract void outputDocuments(JxZkBiBase jxf,
			List<BiCellCollection> documents,boolean sendByEmail);

	protected String getOutputRadiogroupId() { return "stockOutInvOutput"; }
	protected String getScopeRadiogroupId() { return "stockOutInvScope"; }
	protected String getSelectionListId() { return "stockOutInvList"; }
	protected String getDocumentCodeFieldName() { return "stmp_cocode"; }
	protected String getDocumentNameFieldName() { return "vd_vname"; }
	protected String getDocumentAmountFieldName() { return "stmp_amount"; }
	protected String getDocumentMrgFieldName() { return "stmp_mrg"; }
	protected String getDocumentDescription() { return "document"; }

	@Override
	public ReturnMsg processAction(BiResult p_br,int p_idx) {
		br.fetchOneRecV(p_idx);
		List<BiCellCollection> documents = br.getSubLink(getInvoiceSublinkName())
				.getRowCollectionList();
		for(BiCellCollection document : documents) {
			Value val = rpc.callSegment(getPrintSegmentName(),new VectorUtil()
					.addElement(document.getCellInt(getDocumentMrgFieldName()))
					.addElement(document.getCellString(getDocumentCodeFieldName()))
					.addElement("CHNPRINT")
					.addElement("VARIABLE")
					.addElement("A4P")
					.addElement("NORMAL")
					.addElement("LPTRAW")
					.toVector());
			if(val == null || !val.toString().startsWith("OK  ")) return null;
			ReturnMsg rtn = printOneDocToPdf(val.toString().substring(4));
			if(rtn != null && !rtn.getStatus()) return rtn;
		}
		return ReturnMsg.defaultOk;
	}

	@Override
	public void actionPerformed(JxField field) {
		final JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		try {
			final ZkForm settingsForm = new ZkForm(null,getSettingsFormPath());
			final Radiogroup output = (Radiogroup) settingsForm
					.getComponent(getOutputRadiogroupId());
			final Radiogroup scope = (Radiogroup) settingsForm
					.getComponent(getScopeRadiogroupId());
			settingsForm.doModal(new CellCollection(),new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if("btProceed".equals(event.getTarget().getId())) {
						settingsForm.exitModal();
						boolean sendByEmail = output.getSelectedIndex() == 1;
						if(scope.getSelectedIndex() == 1)
							showDocumentSelectionDialog(jxf,sendByEmail);
						else
							outputDocuments(jxf,getDocuments(jxf),sendByEmail);
					} else if("btCancel".equals(event.getTarget().getId())) {
						settingsForm.exitModal();
					}
				}
			});
		} catch(Exception ex) {
			UniLog.log(ex);
		}
	}

	protected List<BiCellCollection> getDocuments(JxZkBiBase jxf) {
		return jxf.getBr().getSubLink(getInvoiceSublinkName()).getRowCollectionList();
	}

	protected List<BiCellCollection> getFilteredDocuments(JxZkBiBase jxf) {
		String sublinkName = getInvoiceSublinkName();
		BiResult documentResult = jxf.getBr().getSubLink(sublinkName);
		JxField listField = jxf.jxAdd("list_" + JxZkBiBase.replaceViewName(sublinkName));
		AbstractGetItemProperty gipi = jxf.getGipi(sublinkName);
		if(listField == null || !(listField.getNativeObject() instanceof Listbox)
				|| gipi == null) return documentResult.getRowCollectionList();

		org.zkoss.zul.ListModel<?> model = ((Listbox) listField.getNativeObject()).getListModel();
		if(model == null) return documentResult.getRowCollectionList();

		List<BiCellCollection> filtered = new ArrayList<BiCellCollection>();
		for(int i = 0;i < model.getSize();i++) {
			int rowIndex = gipi.getIndexOf(model.getElementAt(i));
			if(rowIndex >= 0 && rowIndex < documentResult.getRowCount())
				filtered.add(documentResult.getRowCollectionV(rowIndex));
		}
		return filtered;
	}

	protected void showDocumentSelectionDialog(final JxZkBiBase jxf,
			final boolean sendByEmail) {
		try {
			final ZkForm selectionForm = new ZkForm(null,getSelectionFormPath());
			final Listbox documentList = (Listbox) selectionForm
					.getComponent(getSelectionListId());
			for(BiCellCollection document : getFilteredDocuments(jxf)) {
				Listitem item = new Listitem();
				item.setValue(document);
				item.appendChild(new Listcell(document.getCellString(getDocumentCodeFieldName())));
				item.appendChild(new Listcell(document.getCellString(getDocumentNameFieldName())));
				item.appendChild(new Listcell(String.valueOf(
						document.getCellDouble(getDocumentAmountFieldName()))));
				documentList.appendChild(item);
			}
			selectionForm.doModal(new CellCollection(),new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if("btProceed".equals(event.getTarget().getId())) {
						if(documentList.getSelectedCount() == 0) {
							Messagebox.show("Please select at least one "
									+ getDocumentDescription() + ".");
							return;
						}
						List<BiCellCollection> selected = new ArrayList<BiCellCollection>();
						for(Listitem item : documentList.getItems())
							if(item.isSelected()) selected.add((BiCellCollection) item.getValue());
						selectionForm.exitModal();
						outputDocuments(jxf,selected,sendByEmail);
					} else if("btCancel".equals(event.getTarget().getId())) {
						selectionForm.exitModal();
					}
				}
			});
		} catch(Exception ex) {
			UniLog.log(ex);
		}
	}

	@Override
	protected void doInitRpcClient() {
		rpc.callSegment("setCocodeBaseccy",new VectorUtil()
				.addElement(Erpv4Config.getDefaultCoCode(sh))
				.addElement(Erpv4Config.getBaseCcy(br.getSessionHelper(),
						Erpv4Config.getDefaultCoCode(sh)))
				.toVector());
	}
}
