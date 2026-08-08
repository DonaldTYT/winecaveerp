package com.uniinformation.dynamic.winecave;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Radiogroup;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.PrintOldDocMulti;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrParser.ChnftrGetImageInterface;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiAiAgentContext;
import com.uniinformation.zkf.ZkForm;

public class PrintOldStockOutInv extends PrintOldDocMulti {
	private static final boolean RealSendEmail = false;
	private static final ArrayList<String> mail_cc_list = new ArrayList<String>(Arrays.asList(
			"anita@winecavehk.com",
			"storage@winecavehk.com",
			"general@winecavehk.com",
			"tyt223@gmail.com"));
	private ZkBiAiAgentContext aiAgentContext;

	public PrintOldStockOutInv() {
		super(null);
		docName = "StockOutInvoice";
	}

	public PrintOldStockOutInv(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
		docName = "StockOutInvoice";
	}

	@Override
	public ZkBiAiAgentContext getAiAgentContext() {
		if (aiAgentContext == null && biBase != null)
			aiAgentContext = new PrintOldStockOutInvAIHelperContext(biBase, this);
		return aiAgentContext;
	}

	boolean isRealEmailEnabled() {
		return RealSendEmail;
	}

	protected String getInvoiceSublinkName() { return "graphql.StmpostExtOM"; }
	protected String getPrintSegmentName() { return "winecave_print_stockoutinv"; }
	protected String getSettingsFormPath() { return "zkf/winecave/PrintStockOutInv.zul"; }
	protected String getSelectionFormPath() { return "zkf/winecave/SelectStockOutInv.zul"; }
	protected String getProgressFormPath() { return "zkf/winecave/EmailStockOutInvProgress.zul"; }
	protected String getInvoiceDescription() { return "stock-out invoice"; }
	protected String getChargeDescription() { return "Stock Out Charge Invoice"; }
	protected String getChargeDescriptionLowerCase() { return "stock out charge invoice"; }
	protected String getAttachmentPrefix() { return "StockOutInvoice"; }

	@Override
	public ReturnMsg processAction(BiResult p_br,int p_idx) {
		br.fetchOneRecV(p_idx);

		// TODO Auto-generated method stub
		List<BiCellCollection> outInvoices = br.getSubLink(getInvoiceSublinkName()).getRowCollectionList();
		for(BiCellCollection bcol : outInvoices) {
				int mrg = bcol.getCellInt("stmp_mrg");
				String cocode = bcol.getCellString("stmp_cocode");
				Value val = rpc.callSegment(getPrintSegmentName(),
							new VectorUtil()
							.addElement(mrg)
							.addElement(cocode)
							.addElement("CHNPRINT")
							.addElement("VARIABLE")
							.addElement("A4P")
							.addElement("NORMAL")
							.addElement("LPTRAW")
							.toVector()
						);
				if(val != null && val.toString().startsWith("OK  ")) {
					ReturnMsg rtn = printOneDocToPdf(val.toString().substring(4));
					if(rtn != null && !rtn.getStatus()) return(rtn);
				} else {
					return(null);
				}
			
		}
		return ReturnMsg.defaultOk;
	}

	@Override
	public void actionPerformed(JxField field) {
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		try {
			final ZkForm printForm = new ZkForm(null,getSettingsFormPath());
			final CellCollection settings = new CellCollection();
			final Radiogroup printOutput = (Radiogroup) printForm.getComponent("stockOutInvOutput");
			final Radiogroup printScope = (Radiogroup) printForm.getComponent("stockOutInvScope");
			printForm.doModal(settings,new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if("btProceed".equals(event.getTarget().getId())) {
						printForm.exitModal();
						boolean sendByEmail = printOutput.getSelectedIndex() == 1;
						if(printScope.getSelectedIndex() == 1) {
							showInvoiceSelectionDialog(jxf,sendByEmail);
						} else {
							printStockOutInvoices(jxf,getStockOutInvoices(jxf),sendByEmail);
						}
					} else if("btCancel".equals(event.getTarget().getId())) {
						printForm.exitModal();
					}
				}
			});
		} catch(Exception ex) {
			UniLog.log(ex);
		}
	}

	private List<BiCellCollection> getStockOutInvoices(JxZkBiBase jxf) {
		return jxf.getBr().getSubLink(getInvoiceSublinkName()).getRowCollectionList();
	}

	private List<BiCellCollection> getFilteredStockOutInvoices(JxZkBiBase jxf) {
		final String sublinkName = getInvoiceSublinkName();
		BiResult invoiceResult = jxf.getBr().getSubLink(sublinkName);
		JxField invoiceListField = jxf.jxAdd("list_" + JxZkBiBase.replaceViewName(sublinkName));
		AbstractGetItemProperty invoiceGipi = jxf.getGipi(sublinkName);
		if(invoiceListField == null || !(invoiceListField.getNativeObject() instanceof Listbox)
				|| invoiceGipi == null) {
			return invoiceResult.getRowCollectionList();
		}

		Listbox filteredListbox = (Listbox) invoiceListField.getNativeObject();
		org.zkoss.zul.ListModel<?> filteredModel = filteredListbox.getListModel();
		if(filteredModel == null) {
			return invoiceResult.getRowCollectionList();
		}

		List<BiCellCollection> filteredInvoices = new ArrayList<BiCellCollection>();
		for(int i = 0;i < filteredModel.getSize();i++) {
			int rowIndex = invoiceGipi.getIndexOf(filteredModel.getElementAt(i));
			if(rowIndex >= 0 && rowIndex < invoiceResult.getRowCount()) {
				filteredInvoices.add(invoiceResult.getRowCollectionV(rowIndex));
			}
		}
		return filteredInvoices;
	}

	private void showInvoiceSelectionDialog(JxZkBiBase jxf,boolean sendByEmail) {
		try {
			final ZkForm selectionForm = new ZkForm(null,getSelectionFormPath());
			final Listbox invoiceList = (Listbox) selectionForm.getComponent("stockOutInvList");
			for(BiCellCollection invoice : getFilteredStockOutInvoices(jxf)) {
				Listitem item = new Listitem();
				item.setValue(invoice);
				item.appendChild(new Listcell(invoice.getCellString("stmp_cocode")));
				item.appendChild(new Listcell(invoice.getCellString("vd_vname")));
				item.appendChild(new Listcell(String.valueOf(invoice.getCellDouble("stmp_amount"))));
				invoiceList.appendChild(item);
			}

			selectionForm.doModal(new CellCollection(),new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if("btProceed".equals(event.getTarget().getId())) {
						if(invoiceList.getSelectedCount() == 0) {
							Messagebox.show("Please select at least one invoice.");
							return;
						}
						List<BiCellCollection> selectedInvoices = new ArrayList<BiCellCollection>();
						for(Listitem item : invoiceList.getItems()) {
							if(item.isSelected()) {
								selectedInvoices.add((BiCellCollection) item.getValue());
							}
						}
						selectionForm.exitModal();
						printStockOutInvoices(jxf,selectedInvoices,sendByEmail);
					} else if("btCancel".equals(event.getTarget().getId())) {
						selectionForm.exitModal();
					}
				}
			});
		} catch(Exception ex) {
			UniLog.log(ex);
		}
	}

	private void emailStockOutInvoices(JxZkBiBase jxf,List<BiCellCollection> outInvoices) {
		new StockOutEmailJob(jxf,outInvoices).start();
	}

	private class StockOutEmailJob {
		private static final String SEND_NEXT_EVENT = "onSendNextStockOutInvoice";
		private final JxZkBiBase jxf;
		private final List<BiCellCollection> invoices;
		private final SessionHelper sessionHelper;
		private ZkForm progressForm;
		private Progressmeter progressMeter;
		private Label progressStatus;
		private Label sentCounter;
		private Label failedCounter;
		private Label skippedCounter;
		private Button abortButton;
		private RpcClient jobRpc;
		private Date stockOutDate;
		private int nextIndex;
		private int sentCount;
		private int failedCount;
		private int skippedCount;
		private boolean abortRequested;
		private boolean finished;

		StockOutEmailJob(JxZkBiBase p_jxf,List<BiCellCollection> p_invoices) {
			jxf = p_jxf;
			invoices = new ArrayList<BiCellCollection>(p_invoices);
			sessionHelper = jxf.getBr().getSessionHelper();
		}

		void start() {
			try {
				progressForm = new ZkForm(null,getProgressFormPath());
				progressMeter = (Progressmeter) progressForm.getComponent("emailProgressMeter");
				progressStatus = (Label) progressForm.getComponent("emailProgressStatus");
				sentCounter = (Label) progressForm.getComponent("emailSentCount");
				failedCounter = (Label) progressForm.getComponent("emailFailedCount");
				skippedCounter = (Label) progressForm.getComponent("emailSkippedCount");
				abortButton = (Button) progressForm.getComponent("btAbort");
				progressMeter.addEventListener(SEND_NEXT_EVENT,new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						processNextInvoice();
					}
				});
				updateProgress("Preparing email job...");
				progressForm.doModal(new CellCollection(),new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						if("btAbort".equals(event.getTarget().getId())) {
							abortRequested = true;
							abortButton.setDisabled(true);
							finishJob(true,null);
						}
					}
				});
				Events.echoEvent(SEND_NEXT_EVENT,progressMeter,null);
			} catch(Exception ex) {
				UniLog.log(ex);
				finishJob(false,ex.getMessage());
			}
		}

		private void initializeJob() throws Exception {
			stockOutDate = jxf.getBr().getCellDate("storh_date");
			if(stockOutDate == null) {
				throw new Exception("Stock-out charge date is required");
			}
			jobRpc = jxf.getRpcClient();
			ChnftrRpcServlet rpcServlet = new ChnftrRpcServlet(jobRpc.getConnection());
			jobRpc.setRpcServlet(rpcServlet.getClass().getName(),rpcServlet);
			jobRpc.callSegment("printer_autoselect",new VectorUtil().addElement(1).toVector());
			jobRpc.callSegment("setCocodeBaseccy",
					new VectorUtil()
					.addElement(Erpv4Config.getDefaultCoCode(sessionHelper))
					.addElement(Erpv4Config.getBaseCcy(sessionHelper,
							Erpv4Config.getDefaultCoCode(sessionHelper)))
					.toVector());
			jobRpc.callSegment("erpv4SetImageDir",new VectorUtil()
					.addElement(sessionHelper.getWebContentRealPath("images",true)).toVector());
		}

		private void processNextInvoice() {
			if(finished) return;
			if(abortRequested) {
				finishJob(true,null);
				return;
			}
			BiCellCollection invoice = null;
			try {
				if(jobRpc == null) initializeJob();
				if(nextIndex >= invoices.size()) {
					finishJob(false,null);
					return;
				}

				invoice = invoices.get(nextIndex++);
				String cocode = invoice.getCellString("stmp_cocode");
				updateProgress(String.format("Processing %d of %d: %s",
						nextIndex,invoices.size(),cocode));
				if(RealSendEmail && isBlank(invoice.getCellString("vd_email"))) {
					skippedCount++;
					updateEmailRemark(invoice,"skip");
					UniLog.log("Skip " + getInvoiceDescription() + " with no customer email: " + cocode);
				} else {
					sendOneInvoice(invoice);
				}
				updateProgress(String.format("Processed %d of %d",
						nextIndex,invoices.size()));
				if(abortRequested) {
					finishJob(true,null);
				} else if(nextIndex < invoices.size()) {
					Events.echoEvent(SEND_NEXT_EVENT,progressMeter,null);
				} else {
					finishJob(false,null);
				}
			} catch(Exception ex) {
				failedCount++;
				if(invoice != null) updateEmailRemark(invoice,"failed");
				UniLog.log(ex);
				updateProgress("Failed to process an invoice");
				if(nextIndex < invoices.size() && !abortRequested) {
					Events.echoEvent(SEND_NEXT_EVENT,progressMeter,null);
				} else {
					finishJob(abortRequested,null);
				}
			}
		}

		private void sendOneInvoice(BiCellCollection invoice) throws Exception {
			Value val = jobRpc.callSegment(getPrintSegmentName(),
					new VectorUtil()
					.addElement(invoice.getCellInt("stmp_mrg"))
					.addElement(invoice.getCellString("stmp_cocode"))
					.addElement("CHNPRINT")
					.addElement("VARIABLE")
					.addElement("A4P")
					.addElement("NORMAL")
					.addElement("LPTRAW")
					.toVector());
			if(val == null || !val.toString().startsWith("OK  ")) {
				failedCount++;
				updateEmailRemark(invoice,"failed");
				UniLog.log("Unable to generate " + getInvoiceDescription() + " for email: " + val);
				return;
			}

			try(InputStream input = jxf.erpFileInputStream(val.toString().substring(4));
					ByteArrayOutputStream invoiceOutput = new ByteArrayOutputStream()) {
				ChnftrRpcServlet.streamChnftrToPdf(input,invoiceOutput,sessionHelper);
				ReturnMsg sendResult = sendStockOutInvoiceEmail(invoice,stockOutDate,
						invoiceOutput.toByteArray(),sessionHelper);
				if(sendResult != null && sendResult.getStatus()) {
					sentCount++;
					updateEmailRemark(invoice,"sent");
				} else {
					failedCount++;
					updateEmailRemark(invoice,"failed");
					UniLog.log("Unable to email " + getInvoiceDescription() + ": "
							+ (sendResult == null ? "No response" : sendResult.getMsg()));
				}
			}
		}

		private void updateEmailRemark(BiCellCollection invoice,String status) {
			try {
				String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				invoice.getCell("stmp_remark").set(status + " - " + timestamp);
				jxf.setDirtyFlag(true);
			} catch(Exception ex) {
				UniLog.log(ex);
			}
		}

		private void updateProgress(String status) {
			if(progressStatus != null) progressStatus.setValue(status);
			if(sentCounter != null) sentCounter.setValue(String.valueOf(sentCount));
			if(failedCounter != null) failedCounter.setValue(String.valueOf(failedCount));
			if(skippedCounter != null) skippedCounter.setValue(String.valueOf(skippedCount));
			if(progressMeter != null) {
				int value = invoices.isEmpty() ? 100 : nextIndex * 100 / invoices.size();
				progressMeter.setValue(value);
			}
		}

		private void finishJob(boolean aborted,String error) {
			if(finished) return;
			finished = true;
			if(jobRpc != null) {
				jobRpc.close();
				jobRpc = null;
			}
			if(progressForm != null) progressForm.exitModal();
			int notProcessed = Math.max(0,invoices.size() - nextIndex);
			String summary;
			if(error != null) {
				summary = "Email job failed: " + error;
			} else if(aborted) {
				summary = String.format(
						"Email aborted: %d sent, %d skipped, %d failed, %d not processed.",
						sentCount,skippedCount,failedCount,notProcessed);
			} else {
				summary = String.format("Email completed: %d sent, %d skipped, %d failed.",
						sentCount,skippedCount,failedCount);
			}
			Messagebox.show(summary);
		}
	}

	private ReturnMsg sendStockOutInvoiceEmail(BiCellCollection invoice,Date stockOutDate,
			byte[] pdf,SessionHelper sessionHelper) {
		File attachmentFile = null;
		try {
			String cocode = invoice.getCellString("stmp_cocode");
			if(cocode == null) cocode = "";
			cocode = cocode.trim();

			Date periodStart = DateUtil.monthStart(DateUtil.prevmonth(stockOutDate,1));
			Date periodEnd = DateUtil.monthEnd(periodStart);
			String subject = String.format("%s - %s for %s",
					cocode,getChargeDescription(),new SimpleDateFormat("MM/yyyy").format(periodStart));
			String htmlMsg = String.format(
					"<html><body>"
					+ "Dear Customer, <BR><BR>"
					+ "Attached please find the %s from %s to %s.<BR>"
					+ "This is an automated email and for your reference only, if you have any questions, please feel free to contact us.  <BR><BR>"
					+ "The Wine Cave Co., Ltd<BR>"
					+ "15 &amp; 16/F,LMK Development Estate,<BR>"
					+ "10-16 Kwai Ting Road,<BR>"
					+ "Kwai Chung, N. T.<BR>"
					+ "Tel: (852) 3427 9989<BR>"
					+ "Fax: (852) 3572 0895<BR>"
					+ "Website: www.winecavehk.com<BR><BR><BR>"
					+ "</body></html>",
					getChargeDescriptionLowerCase(),
					new SimpleDateFormat("dd/MM/yyyy").format(periodStart),
					new SimpleDateFormat("dd/MM/yyyy").format(periodEnd));

			Set<String> recipientAddresses = new LinkedHashSet<String>();
			if(RealSendEmail) {
				addEmailAddresses(recipientAddresses,invoice.getCellString("vd_email"));
			} else {
				recipientAddresses.add("tyt223@gmail.com");
			}
			if(recipientAddresses.isEmpty()) {
				return new ReturnMsg(false,"No customer email address for " + cocode);
			}

			List<Pair<String,String>> toList = new ArrayList<Pair<String,String>>();
			for(String email : recipientAddresses) {
				toList.add(Pair.of(email,(String) null));
			}
			List<Pair<String,String>> ccList = null;
			if(RealSendEmail) {
				ccList = new ArrayList<Pair<String,String>>();
				for(String email : mail_cc_list) {
					ccList.add(Pair.of(email,(String) null));
				}
			}

			attachmentFile = File.createTempFile(getAttachmentPrefix() + "-",".pdf");
			Files.write(attachmentFile.toPath(),pdf);
			EmailAttachment attachment = new EmailAttachment();
			attachment.setPath(attachmentFile.getAbsolutePath());
			attachment.setName(getAttachmentPrefix() + "_" + cocode.replaceAll("[^A-Za-z0-9_-]","_")
					+ "_" + new SimpleDateFormat("yyyyMM").format(periodStart) + ".pdf");
			attachment.setDescription(getChargeDescription());
			attachment.setDisposition(EmailAttachment.ATTACHMENT);
			List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
			attachments.add(attachment);

			return ZkUtil.sendEmail(null,toList,ccList,null,subject,htmlMsg,"",attachments,
					sessionHelper);
		} catch(Exception ex) {
			UniLog.log(ex);
			return new ReturnMsg(ex);
		} finally {
			if(attachmentFile != null && attachmentFile.exists() && !attachmentFile.delete()) {
				UniLog.log("Unable to delete temporary " + getInvoiceDescription() + " "
						+ attachmentFile.getAbsolutePath());
			}
		}
	}

	private void addEmailAddresses(Set<String> recipients,String addresses) {
		if(addresses == null) return;
		for(String address : addresses.split("[,;]")) {
			if(address != null && !address.trim().isEmpty()) {
				recipients.add(address.trim());
			}
		}
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}

	private void printStockOutInvoices(JxZkBiBase jxf,List<BiCellCollection> outInvoices,
			boolean sendByEmail) {
		if(sendByEmail) {
			emailStockOutInvoices(jxf,outInvoices);
			return;
		}
		BiResult result = jxf.getBr();
		SessionHelper sessionHelper = result.getSessionHelper();
		RpcClient jxRpc = jxf.getRpcClient();
		Document pdfDocument = null;
		PdfCopy pdfCopy = null;
		ByteArrayOutputStream combinedOutput = new ByteArrayOutputStream();
		int pageCount = 0;

		try {
			ChnftrRpcServlet rpcServlet = new ChnftrRpcServlet(jxRpc.getConnection());
			jxRpc.setRpcServlet(rpcServlet.getClass().getName(),rpcServlet);
			jxRpc.callSegment("printer_autoselect",new VectorUtil().addElement(1).toVector());
			jxRpc.callSegment("setCocodeBaseccy",
					new VectorUtil()
					.addElement(Erpv4Config.getDefaultCoCode(sessionHelper))
					.addElement(Erpv4Config.getBaseCcy(sessionHelper,
							Erpv4Config.getDefaultCoCode(sessionHelper)))
					.toVector());
			jxRpc.callSegment("erpv4SetImageDir",new VectorUtil()
					.addElement(sessionHelper.getWebContentRealPath("images",true)).toVector());

			pdfDocument = new Document();
			pdfCopy = new PdfCopy(pdfDocument,combinedOutput);
			pdfDocument.open();

			for(BiCellCollection invoice : outInvoices) {
				Value val = jxRpc.callSegment(getPrintSegmentName(),
						new VectorUtil()
						.addElement(invoice.getCellInt("stmp_mrg"))
						.addElement(invoice.getCellString("stmp_cocode"))
						.addElement("CHNPRINT")
						.addElement("VARIABLE")
						.addElement("A4P")
						.addElement("NORMAL")
						.addElement("LPTRAW")
						.toVector());
				if(val == null || !val.toString().startsWith("OK  ")) {
					UniLog.log("Unable to print " + getInvoiceDescription() + ": " + val);
					return;
				}

				PdfReader invoiceReader = null;
				try {
					InputStream input = jxf.erpFileInputStream(val.toString().substring(4));
					ChnftrParser parser = new ChnftrParser(input,"");
					parser.setChnftrGetImageInterface(new ChnftrGetImageInterface() {
						@Override
						public byte[] getImage(String key) {
							String url = key.startsWith(ChnftrParser.GETIMAGE_TAG)
									? SessionHelper.URLHEADER_FILING
											+ key.substring(ChnftrParser.GETIMAGE_TAG.length())
									: key;
							try {
								return sessionHelper.newErpFileToByteArray(url);
							} catch(Exception ex) {
								UniLog.log(ex);
								return null;
							}
						}
					});
					parser.setUseGetImageInterfaceByDefault(true);
					ByteArrayOutputStream invoiceOutput = new ByteArrayOutputStream();
					parser.print(invoiceOutput);
					invoiceReader = new PdfReader(new ByteArrayInputStream(invoiceOutput.toByteArray()));
					for(int page = 1; page <= invoiceReader.getNumberOfPages(); page++) {
						pdfCopy.addPage(pdfCopy.getImportedPage(invoiceReader,page));
						pageCount++;
					}
				} finally {
					if(invoiceReader != null) {
						pdfCopy.freeReader(invoiceReader);
						invoiceReader.close();
					}
				}
			}

			pdfDocument.close();
			pdfDocument = null;
			if(pageCount > 0) {
				ByteArrayInputStream pdfInput = new ByteArrayInputStream(combinedOutput.toByteArray());
				if(sessionHelper.isMobileDevice()) {
					ZkUtil.openPdfInlineFromStream(pdfInput,"application/pdf",sessionHelper);
				} else {
					ZkUtil.printFromStream(pdfInput,"application/pdf",sessionHelper);
				}
			}
		} catch(Exception ex) {
			UniLog.log(ex);
		} finally {
			if(pdfDocument != null && pdfDocument.isOpen()) {
				try {
					pdfDocument.close();
				} catch(Exception ex) {
					UniLog.log(ex);
				}
			}
			jxRpc.close();
		}
	}

	@Override
	protected void doInitRpcClient() {
		// TODO Auto-generated method stub
		rpc.callSegment("setCocodeBaseccy",
		new VectorUtil()
		.addElement( Erpv4Config.getDefaultCoCode(sh))
		.addElement( Erpv4Config.getBaseCcy(br.getSessionHelper(),
				Erpv4Config.getDefaultCoCode(sh)
				))
		.toVector()
		);	
	}

}
