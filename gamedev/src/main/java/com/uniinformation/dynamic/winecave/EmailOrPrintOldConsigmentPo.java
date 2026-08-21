package com.uniinformation.dynamic.winecave;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.erpv4.EmailOrPrintOldDocMulti;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;

/** Email or print consignment purchase orders from wc.StockOut/wc.StmpostExt. */
public class EmailOrPrintOldConsigmentPo extends EmailOrPrintOldDocMulti {
	private static final String SUBLINK = "wc.StmpostExt";
	private static final String PRINT_SEGMENT = "winecave_print_cnpo";

	private static class Recipient {
		String email;
		String name;
	}

	public EmailOrPrintOldConsigmentPo() {
		super();
		docName = "ConsigmentPO";
	}

	public EmailOrPrintOldConsigmentPo(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		docName = "ConsigmentPO";
	}

	@Override protected String getInvoiceSublinkName() { return SUBLINK; }
	@Override protected String getPrintSegmentName() { return PRINT_SEGMENT; }
	@Override protected String getSettingsFormPath() {
		return "zkf/winecave/PrintConsigmentPo.zul";
	}
	@Override protected String getSelectionFormPath() {
		return "zkf/winecave/SelectConsigmentPo.zul";
	}
	@Override protected String getDocumentNameFieldName() { return "stmp_sno"; }
	@Override protected String getDocumentDescription() { return "consignment PO"; }

	private Value generatePo(RpcClient client,BiCellCollection po) {
		return client.callSegment(PRINT_SEGMENT,new VectorUtil()
				.addElement(po.getCellInt("stmp_mrg"))
				.addElement(0)
				.addElement(po.getCellString("stmp_cocode"))
				.addElement("CHNPRINT")
				.addElement("VARIABLE")
				.addElement("A4P")
				.addElement("NORMAL")
				.addElement("LPTRAW")
				.toVector());
	}

	private ReturnMsg appendPoToCombinedPdf(BiCellCollection po) {
		Value value = generatePo(rpc,po);
		if(value == null || !value.toString().startsWith("OK  "))
			return new ReturnMsg(false,"Unable to generate consignment PO for "
					+ po.getCellString("stmp_cocode") + ": " + String.valueOf(value));
		return printOneDocToPdf(value.toString().substring(4));
	}

	@Override
	public ReturnMsg processAction(BiResult p_br,int p_idx) {
		br.fetchOneRecV(p_idx);
		BiResult poResult = br.getSubLink(SUBLINK);
		boolean printed = false;
		for(int i = 0;i < poResult.getRowCount();i++) {
			if(poResult.isMarkedDelete(poResult.getTrStatObj(i))) continue;
			ReturnMsg result = appendPoToCombinedPdf(poResult.getRowCollectionV(i));
			if(result != null && !result.getStatus()) return result;
			printed = true;
		}
		return printed ? ReturnMsg.defaultOk
				: new ReturnMsg(false,"No consignment purchase order is available to print.");
	}

	@Override
	protected void outputDocuments(JxZkBiBase jxf,List<BiCellCollection> documents,
			boolean sendByEmail) {
		if(sendByEmail) {
			emailDocuments(jxf,documents);
			return;
		}

		ReturnMsg result = beforeAction(jxf.getBr(),documents.size());
		if(result != null && result.getStatus()) {
			for(BiCellCollection po : documents) {
				result = appendPoToCombinedPdf(po);
				if(result != null && !result.getStatus()) break;
			}
			ReturnMsg finishResult = afterAction(jxf.getBr());
			if(result == null || result.getStatus()) result = finishResult;
		}
		if(result == null || !result.getStatus())
			Messagebox.show(result == null ? "Unable to print consignment PO."
					: result.getMsg());
	}

	private void initializeRpc(RpcClient client,BiResult stockOut) {
		ChnftrRpcServlet servlet = new ChnftrRpcServlet(client.getConnection());
		client.setRpcServlet(servlet.getClass().getName(),servlet);
		client.callSegment("printer_autoselect",new VectorUtil().addElement(1).toVector());
		String cocode = Erpv4Config.getDefaultCoCode(stockOut.getSessionHelper());
		client.callSegment("setCocodeBaseccy",new VectorUtil()
				.addElement(cocode)
				.addElement(Erpv4Config.getBaseCcy(stockOut.getSessionHelper(),cocode))
				.toVector());
		client.callSegment("erpv4SetImageDir",new VectorUtil()
				.addElement(stockOut.getSessionHelper()
						.getWebContentRealPath("images",true)).toVector());
	}

	private Recipient findRecipient(String cocode,BiResult stockOut) throws Exception {
		Recipient recipient = new Recipient();
		SelectUtil select = new SelectUtil();
		try {
			select.setLoginId(stockOut.getSessionHelper().getLoginId());
			select.init(BiSchema.loadSchema(stockOut.getSessionHelper()).getConn());
			String safeCode = cocode == null ? "" : cocode.replace("'","''");
			TableRec records = select.getQueryResult(
					"select vd_email,vd_vname from vendor where vd_vcode = '"
					+ safeCode + "'");
			if(records.getRecordCount() > 0) {
				records.setRecPointer(0);
				recipient.email = records.getFieldString("vd_email");
				recipient.name = records.getFieldString("vd_vname");
			}
			return recipient;
		} finally {
			select.close();
		}
	}

	private void emailDocuments(JxZkBiBase jxf,List<BiCellCollection> documents) {
		RpcClient client = jxf.getRpcClient();
		int sent = 0;
		int skipped = 0;
		int failed = 0;
		try {
			initializeRpc(client,jxf.getBr());
			for(BiCellCollection po : documents) {
				try {
					String cocode = po.getCellString("stmp_cocode");
					Recipient recipient = findRecipient(cocode,jxf.getBr());
					if(recipient.email == null || recipient.email.trim().isEmpty()) {
						skipped++;
						updateRemark(jxf,po,"skip");
						continue;
					}
					Value value = generatePo(client,po);
					if(value == null || !value.toString().startsWith("OK  "))
						throw new Exception("Print segment returned " + String.valueOf(value));

					byte[] pdf;
					try(InputStream input = jxf.erpFileInputStream(value.toString().substring(4));
							ByteArrayOutputStream output = new ByteArrayOutputStream()) {
						ChnftrRpcServlet.streamChnftrToPdf(input,output,jxf.getBr().getSessionHelper());
						pdf = output.toByteArray();
					}
					ReturnMsg sendResult = sendEmail(po,recipient,pdf,jxf.getBr());
					if(sendResult == null || !sendResult.getStatus())
						throw new Exception(sendResult == null ? "No email response" : sendResult.getMsg());
					sent++;
					updateRemark(jxf,po,"sent");
				} catch(Exception ex) {
					failed++;
					updateRemark(jxf,po,"failed");
					UniLog.log(ex);
				}
			}
		} catch(Exception ex) {
			UniLog.log(ex);
			Messagebox.show("Unable to initialize consignment PO email: " + ex.getMessage());
			return;
		} finally {
			client.close();
		}
		Messagebox.show(String.format("Email completed: %d sent, %d skipped, %d failed.",
				sent,skipped,failed));
	}

	private ReturnMsg sendEmail(BiCellCollection po,Recipient recipient,byte[] pdf,
			BiResult stockOut) {
		File attachmentFile = null;
		try {
			String poNumber = po.getCellString("stmp_sno");
			if(poNumber == null || poNumber.trim().isEmpty())
				poNumber = "CPO" + po.getCellInt("stmp_mrg") + "-"
						+ po.getCellString("stmp_cocode");
			attachmentFile = File.createTempFile("ConsigmentPO-",".pdf");
			Files.write(attachmentFile.toPath(),pdf);

			EmailAttachment attachment = new EmailAttachment();
			attachment.setPath(attachmentFile.getAbsolutePath());
			attachment.setName(poNumber.replaceAll("[^A-Za-z0-9_-]","_") + ".pdf");
			attachment.setDescription("Consignment Purchase Order");
			attachment.setDisposition(EmailAttachment.ATTACHMENT);

			List<Pair<String,String>> to = new ArrayList<Pair<String,String>>();
			for(String address : recipient.email.split("[,;]"))
				if(!address.trim().isEmpty()) to.add(Pair.of(address.trim(),recipient.name));
			List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
			attachments.add(attachment);
			String subject = "Purchase Order #" + poNumber;
			String html = "<html><body>Dear "
					+ (recipient.name == null ? "Supplier" : recipient.name)
					+ ",<br><br>Attached please find consignment purchase order <b>"
					+ poNumber + "</b>.<br><br>The Wine Cave Co., Ltd</body></html>";
			return ZkUtil.sendEmail(null,to,null,null,subject,html,"",attachments,
					stockOut.getSessionHelper());
		} catch(Exception ex) {
			return new ReturnMsg(ex);
		} finally {
			if(attachmentFile != null && attachmentFile.exists()) attachmentFile.delete();
		}
	}

	private void updateRemark(JxZkBiBase jxf,BiCellCollection po,String status) {
		try {
			po.getCell("stmp_remark").set(status + " - "
					+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			jxf.setDirtyFlag(true);
		} catch(Exception ex) {
			UniLog.log(ex);
		}
	}

	@Override
	protected void doInitRpcClient() {
		String cocode = Erpv4Config.getDefaultCoCode(sh);
		rpc.callSegment("setCocodeBaseccy",new VectorUtil()
				.addElement(cocode)
				.addElement(Erpv4Config.getBaseCcy(sh,cocode)).toVector());
		rpc.callSegment("erpv4SetImageDir",new VectorUtil()
				.addElement(sh.getWebContentRealPath("images",true)).toVector());
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return true;
		if(!p_br.getSessionHelper().hasAccessRight("#prtpo")) return true;
		return !p_isBatch && p_br.inBeginWork();
	}
}
