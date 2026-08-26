package com.uniinformation.erpv4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Filedownload;
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
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkf.ZkForm;

/**
 * Common action for producing multiple Java-defined {@link PrtdocJson}
 * documents as one PDF, then either downloading that PDF or passing it to a
 * concrete email implementation.
 *
 * <p>Unlike {@link PrintOrMailOldDocMulti}, this class does not call a legacy
 * print segment and does not render CHNFTR output.  A subclass creates the
 * PrtdocJson job and writes each selected document into the current content.
 * This class creates subsequent contents with {@link PrtdocJson#newContent()}
 * and renders the complete job once.</p>
 */
public abstract class PrintOrEmailNewDocMulti extends PrintMultiDoc {

	protected PrintOrEmailNewDocMulti() {
		super(null);
	}

	protected PrintOrEmailNewDocMulti(ZkBiComposerBase p_bibase) {
		super(p_bibase);
	}

	protected String getDocumentSublinkName() { return null; }
	protected abstract String getSettingsFormPath();
	protected String getSelectionFormPath() { return null; }
	protected boolean useCurrentRecord() { return false; }

	/** Create an empty print job, including its template/document code. */
	protected abstract PrtdocJson createPrintDocJson(SessionHelper sessionHelper)
			throws Exception;

	/** Write one document into the current PrtdocJson content. */
	protected void addDocument(PrtdocJson printDocJson,JxZkBiBase jxf,
			BiCellCollection document) throws Exception {
		br = jxf.getBr();
		printOneDoc();
	}

	/**
	 * Send the already-rendered, combined PDF.  Customer-specific recipients,
	 * subject, body and attachment naming belong in the concrete class.
	 */
	protected abstract ReturnMsg emailPdf(JxZkBiBase jxf,
			List<BiCellCollection> documents,byte[] pdf) throws Exception;

	protected String getOutputRadiogroupId() { return "documentOutput"; }
	protected String getScopeRadiogroupId() { return "documentScope"; }
	protected String getSelectionListId() { return "documentList"; }
	protected String getDocumentCodeFieldName() { return "cocode"; }
	protected String getDocumentNameFieldName() { return "name"; }
	protected String getDocumentAmountFieldName() { return "amount"; }
	protected String getDocumentDescription() { return "document"; }
	protected String getDownloadFileName() { return "documents.pdf"; }

	@Override
	protected ReturnMsg initPrtdoc() {
		try {
			ppj = createPrintDocJson(sh);
			docCnt = 0;
			return ReturnMsg.defaultOk;
		} catch(Exception ex) {
			UniLog.log(ex);
			return new ReturnMsg(ex);
		}
	}

	@Override
	protected String getDocumentName(BiResult p_br) {
		String fileName = getDownloadFileName();
		return fileName != null && fileName.toLowerCase().endsWith(".pdf")
				? fileName.substring(0,fileName.length() - 4) : fileName;
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
						if(!useCurrentRecord() && scope != null && scope.getSelectedIndex() == 1)
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
			Messagebox.show(ex.toString());
		}
	}

	protected List<BiCellCollection> getDocuments(JxZkBiBase jxf) {
		if(useCurrentRecord())
			return Collections.singletonList(jxf.getBr().getCurrentCollection());
		return jxf.getBr().getSubLink(getDocumentSublinkName()).getRowCollectionList();
	}

	protected List<BiCellCollection> getFilteredDocuments(JxZkBiBase jxf) {
		if(useCurrentRecord()) return getDocuments(jxf);
		String sublinkName = getDocumentSublinkName();
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
			Messagebox.show(ex.toString());
		}
	}

	protected void outputDocuments(JxZkBiBase jxf,List<BiCellCollection> documents,
			boolean sendByEmail) {
		try {
			if(documents == null || documents.isEmpty()) {
				Messagebox.show("No " + getDocumentDescription() + " selected.");
				return;
			}

			sh = jxf.getBr().getSessionHelper();
			ReturnMsg initResult = initPrtdoc();
			if(initResult != null && !initResult.getStatus()) {
				Messagebox.show(initResult.getMsg());
				return;
			}

			for(BiCellCollection document : documents) {
				if(docCnt > 0) ppj.newContent();
				addDocument(ppj,jxf,document);
				docCnt++;
			}

			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ReturnMsg renderResult = ppj.toPdfStream(output,sh);
			if(renderResult != null && !renderResult.getStatus()) {
				Messagebox.show(renderResult.getMsg());
				return;
			}

			byte[] pdf = output.toByteArray();
			if(sendByEmail) {
				ReturnMsg emailResult = emailPdf(jxf,documents,pdf);
				if(emailResult != null && !emailResult.getStatus())
					Messagebox.show(emailResult.getMsg());
			} else {
				Filedownload.save(new ByteArrayInputStream(pdf),"application/pdf",
						getDownloadFileName());
			}
		} catch(Exception ex) {
			UniLog.log(ex);
			Messagebox.show(ex.toString());
		} finally {
			ppj = null;
			docCnt = 0;
			sh = null;
		}
	}
}
