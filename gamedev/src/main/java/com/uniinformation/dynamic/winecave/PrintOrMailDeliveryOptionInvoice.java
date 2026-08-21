package com.uniinformation.dynamic.winecave;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radiogroup;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.PrintOldDocMulti;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkf.ZkForm;

/** Prints or emails the accounting invoice generated from Delivery options. */
public class PrintOrMailDeliveryOptionInvoice extends PrintOldDocMulti {
	private static final String PRINT_SEGMENT = "winecave_print_delioption_invoice";
	private static final List<String> MAIL_CC = Arrays.asList(
			"anita@winecavehk.com", "storage@winecavehk.com", "general@winecavehk.com");

	private static final class InvoiceInfo {
		int rg;
		String number;
		String companyCode;
		String customerCode;
		String customerName;
		String email;
	}

	public PrintOrMailDeliveryOptionInvoice() {
		super(null);
		docName = "DeliveryOptionInvoice";
	}

	public PrintOrMailDeliveryOptionInvoice(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		docName = "DeliveryOptionInvoice";
	}

	@Override
	public void actionPerformed(JxField field) {
		final JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		try {
			final ZkForm settings = new ZkForm(null,
					"zkf/winecave/PrintDeliveryOptionInvoice.zul");
			final Radiogroup output = (Radiogroup) settings
					.getComponent("deliveryOptionInvoiceOutput");
			settings.doModal(new CellCollection(),new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					if("btProceed".equals(event.getTarget().getId())) {
						settings.exitModal();
						outputInvoice(jxf,output.getSelectedIndex() == 1);
					} else if("btCancel".equals(event.getTarget().getId())) {
						settings.exitModal();
					}
				}
			});
		} catch(Exception ex) {
			UniLog.log(ex);
			Messagebox.show("Unable to open Delivery option invoice output: " + ex.getMessage());
		}
	}

	private InvoiceInfo findInvoice(BiResult delivery) throws Exception {
		String deliveryNumber = delivery.getCellString("stm_ref1");
		if(deliveryNumber == null || deliveryNumber.trim().isEmpty())
			throw new Exception("Please save the Delivery Note before printing its option invoice.");

		TableRec invoice = delivery.getSelectUtil().getQueryResult(
				"select inv_rg,inv_invno,inv_cocode,inv_vcode from invoice where inv_dncode = ?",
				new Wherecl().appendArgument(deliveryNumber));
		if(invoice == null || invoice.getRecordCount() == 0)
			throw new Exception("No option invoice exists for D/N " + deliveryNumber + ".");
		invoice.setRecPointer(0);

		InvoiceInfo info = new InvoiceInfo();
		info.rg = invoice.getFieldInt("inv_rg");
		info.number = invoice.getFieldString("inv_invno");
		info.companyCode = invoice.getFieldString("inv_cocode");
		info.customerCode = invoice.getFieldString("inv_vcode");

		TableRec vendor = delivery.getSelectUtil().getQueryResult(
				"select vd_vname,vd_email from vendor where vd_vcode = ?",
				new Wherecl().appendArgument(info.customerCode));
		if(vendor != null && vendor.getRecordCount() > 0) {
			vendor.setRecPointer(0);
			info.customerName = vendor.getFieldString("vd_vname");
			info.email = vendor.getFieldString("vd_email");
		}
		return info;
	}

	private Value generateInvoice(RpcClient client,InvoiceInfo invoice,String cocode) {
		return client.callSegment(PRINT_SEGMENT,new VectorUtil()
				.addElement(invoice.rg)
				.addElement(cocode)
				.addElement("CHNPRINT")
				.addElement("VARIABLE")
				.addElement("A4P")
				.addElement("NORMAL")
				.addElement("LPTRAW")
				.toVector());
	}

	private void outputInvoice(JxZkBiBase jxf,boolean sendByEmail) {
		BiResult delivery = jxf.getBr();
		try {
			InvoiceInfo invoice = findInvoice(delivery);
			if(sendByEmail) emailInvoice(jxf,delivery,invoice);
			else printInvoice(delivery,invoice);
		} catch(Exception ex) {
			UniLog.log(ex);
			Messagebox.show(ex.getMessage());
		}
	}

	private void printInvoice(BiResult delivery,InvoiceInfo invoice) {
		ReturnMsg result = beforeAction(delivery,1);
		if(result != null && result.getStatus()) {
			Value value = generateInvoice(rpc,invoice,invoice.companyCode);
			if(value == null || !value.toString().startsWith("OK  ")) {
				result = new ReturnMsg(false,"Unable to generate Delivery option invoice: "
						+ String.valueOf(value));
			} else {
				result = printOneDocToPdf(value.toString().substring(4));
			}
			ReturnMsg finish = afterAction(delivery);
			if(result == null || result.getStatus()) result = finish;
		}
		if(result == null || !result.getStatus())
			Messagebox.show(result == null ? "Unable to print Delivery option invoice."
					: result.getMsg());
	}

	private void emailInvoice(JxZkBiBase jxf,BiResult delivery,InvoiceInfo invoice)
			throws Exception {
		if(invoice.email == null || invoice.email.trim().isEmpty())
			throw new Exception("No customer email address is available for "
					+ invoice.customerCode + ".");

		RpcClient client = jxf.getRpcClient();
		try {
			initializeRpc(client,delivery);
			Value value = generateInvoice(client,invoice,invoice.companyCode);
			if(value == null || !value.toString().startsWith("OK  "))
				throw new Exception("Unable to generate Delivery option invoice: "
						+ String.valueOf(value));

			byte[] pdf;
			try(InputStream input = jxf.erpFileInputStream(value.toString().substring(4));
					ByteArrayOutputStream output = new ByteArrayOutputStream()) {
				ChnftrRpcServlet.streamChnftrToPdf(input,output,delivery.getSessionHelper());
				pdf = output.toByteArray();
			}
			ReturnMsg sent = sendEmail(invoice,pdf,delivery.getSessionHelper());
			if(sent == null || !sent.getStatus())
				throw new Exception(sent == null ? "No email response" : sent.getMsg());
			Messagebox.show("Delivery option invoice emailed successfully.");
		} finally {
			client.close();
		}
	}

	private void initializeRpc(RpcClient client,BiResult delivery) {
		ChnftrRpcServlet servlet = new ChnftrRpcServlet(client.getConnection());
		client.setRpcServlet(servlet.getClass().getName(),servlet);
		client.callSegment("printer_autoselect",new VectorUtil().addElement(1).toVector());
		String cocode = Erpv4Config.getDefaultCoCode(delivery.getSessionHelper());
		client.callSegment("setCocodeBaseccy",new VectorUtil()
				.addElement(cocode)
				.addElement(Erpv4Config.getBaseCcy(delivery.getSessionHelper(),cocode))
				.toVector());
		client.callSegment("erpv4SetImageDir",new VectorUtil()
				.addElement(delivery.getSessionHelper().getWebContentRealPath("images",true))
				.toVector());
	}

	private ReturnMsg sendEmail(InvoiceInfo invoice,byte[] pdf,SessionHelper session) {
		File attachmentFile = null;
		try {
			attachmentFile = File.createTempFile("DeliveryOptionInvoice-",".pdf");
			Files.write(attachmentFile.toPath(),pdf);
			String invoiceNumber = invoice.number == null || invoice.number.trim().isEmpty()
					? String.valueOf(invoice.rg) : invoice.number.trim();

			EmailAttachment attachment = new EmailAttachment();
			attachment.setPath(attachmentFile.getAbsolutePath());
			attachment.setName("Invoice_" + invoiceNumber.replaceAll("[^A-Za-z0-9_-]","_")
					+ ".pdf");
			attachment.setDescription("Delivery Option Invoice");
			attachment.setDisposition(EmailAttachment.ATTACHMENT);

			List<Pair<String,String>> to = new ArrayList<Pair<String,String>>();
			for(String address : invoice.email.split("[,;]"))
				if(!address.trim().isEmpty())
					to.add(Pair.of(address.trim(),invoice.customerName));
			List<Pair<String,String>> cc = new ArrayList<Pair<String,String>>();
			for(String address : MAIL_CC) cc.add(Pair.of(address,(String) null));
			List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
			attachments.add(attachment);

			String subject = "Invoice #" + invoiceNumber;
			String html = "<html><body>Dear "
					+ (invoice.customerName == null ? "Customer" : invoice.customerName)
					+ ",<br><br>Attached please find invoice <b>" + invoiceNumber
					+ "</b> for the Delivery Note options.<br><br>The Wine Cave Co., Ltd</body></html>";
			return ZkUtil.sendEmail(null,to,cc,null,subject,html,"",attachments,session);
		} catch(Exception ex) {
			return new ReturnMsg(ex);
		} finally {
			if(attachmentFile != null && attachmentFile.exists() && !attachmentFile.delete())
				UniLog.log("Unable to delete temporary Delivery option invoice "
						+ attachmentFile.getAbsolutePath());
		}
	}

	@Override
	public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
		p_result.fetchOneRecV(p_recIdx);
		try {
			InvoiceInfo invoice = findInvoice(p_result);
			Value value = generateInvoice(rpc,invoice,invoice.companyCode);
			if(value == null || !value.toString().startsWith("OK  "))
				return new ReturnMsg(false,"Unable to generate Delivery option invoice: "
						+ String.valueOf(value));
			return printOneDocToPdf(value.toString().substring(4));
		} catch(Exception ex) {
			return new ReturnMsg(ex);
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
	public boolean isDisabled(BiResult p_result,boolean p_isBatch) {
		if(p_result == null) return true;
		if(!p_result.getSessionHelper().hasAccessRight("#Prtvoucher")) return true;
		return !p_isBatch && p_result.inBeginWork();
	}
}
