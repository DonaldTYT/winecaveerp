package com.uniinformation.dynamic.winecave;

import java.io.File;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.PrintOrEmailNewDocMulti;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiComposerBase;

/** Prints the current erpv4.GlTran as a conventional double-entry voucher. */
public class PrintGlVoucher extends PrintOrEmailNewDocMulti {
	private static final String EMAIL_TO_CONFIG = "WineCaveGlVoucherEmailTo";
	private static final String DETAIL_SUBLINK = "erpv4.GlJn";

	public PrintGlVoucher() {
		super(null);
	}

	public PrintGlVoucher(ZkBiComposerBase p_bibase) {
		super(p_bibase);
	}

	@Override
	protected boolean useCurrentRecord() { return true; }

	@Override
	protected String getSettingsFormPath() {
		return "zkf/winecave/PrintGlVoucher.zul";
	}

	@Override
	protected String getDocumentDescription() { return "G/L voucher"; }

	@Override
	protected String getDownloadFileName() {
		String voucherNo = br == null ? "" : safeFilePart(br.getCellString("tr_srcno"));
		return StringUtils.isBlank(voucherNo)
				? "GlVoucher.pdf" : "GlVoucher_" + voucherNo + ".pdf";
	}

	@Override
	protected PrtdocJson createPrintDocJson(SessionHelper sessionHelper) throws Exception {
		String cocode = Erpv4Config.getDefaultCoCode(sessionHelper);
		PrtdocJson result = PrtdocJson.newPrtdocJson(
				cocode,"A4P","GLVOUCHER","erpv4_printDocument");
		result.setTopLeftMargin(0);
		return result;
	}

	@Override
	public ReturnMsg beforeAction(BiResult result,int count) {
		batchDownloadReport = true;
		return super.beforeAction(result,count);
	}

	@Override
	protected void printOneDoc() throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yy");
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
		DecimalFormat amountFormat = new DecimalFormat("#,##0.00");
		DecimalFormat rateFormat = new DecimalFormat("0.00000");

		String cocode = br.getCellString("tr_cocode");
		if(StringUtils.isBlank(cocode)) cocode = Erpv4Config.getDefaultCoCode(sh);
		Map<String,Object> company = Erpv4Config.getCoFieldMap(sh,cocode);
		String companyName = stringValue(company.get("co_coname"));
		String companyChineseName = stringValue(company.get("co_chnname"));

		ppj.setSkipB2GConvert(true);
		ppj.setTrailerAtLastPageOnly(true);
		ppj.addPageNo("pageno","%s of %s",0,0,0);
		ppj.addHeaderField("cvname",companyName,0,0);
		if(!StringUtils.isBlank(companyChineseName))
			ppj.addHeaderField("cvname",companyChineseName,0,20);
		ppj.addHeaderField("doctitle","G/L Voucher",0,0);

		ppj.addHeaderField("dflabel","Transaction",0,0);
		ppj.addHeaderField("dflabel","Voucher No.",0,20);
		ppj.addHeaderField("dflabel","Date",0,40);
		ppj.addHeaderField("dflabel","Post",0,60);
		ppj.addHeaderField("dfvalue",String.valueOf(br.getCellInt("tr_xno")),0,0);
		ppj.addHeaderField("dfvalue",br.getCellString("tr_srcno"),0,20);
		Date voucherDate = br.getCellDate("tr_xdate");
		ppj.addHeaderField("dfvalue",voucherDate == null ? "" : dateFormat.format(voucherDate),0,40);
		ppj.addHeaderField("dfvalue",br.getCellString("tr_post"),0,60);

		ppj.addDetailHeaderField("hdr_itemcode","Account");
		ppj.addDetailHeaderField("hdr_description","Description");
		ppj.addDetailHeaderField("hdr_qty","CCY");
		ppj.addDetailHeaderField("hdr_uprice","Amount");
		ppj.addDetailHeaderField("hdr_discount","Ex. Rate");
		ppj.addDetailHeaderField("hdr_pamount","Debit");
		ppj.addDetailHeaderField("hdr_amount","Credit");

		double totalDebit = 0.0;
		double totalCredit = 0.0;
		for(BiCellCollection line : br.getSubLink(DETAIL_SUBLINK).getRowCollectionList()) {
			ppj.addDetailRecord();
			ppj.addDetailRecordField("itemcode",line.getCellString("ca_ano"));
			ppj.addDetailRecordField("description",line.getCellString("jn_desc0"));
			ppj.addDetailRecordField("quantity",line.getCellString("cc_cid"));
			ppj.addDetailRecordField("price",amountFormat.format(line.getCellDouble("jn_amount")));
			ppj.addDetailRecordField("discount",rateFormat.format(line.getCellDouble("set_xrate")));

			double localAmount = line.getCellDouble("jn_lamount");
			if(localAmount >= 0.0) {
				totalDebit += localAmount;
				ppj.addDetailRecordField("pamount",amountFormat.format(localAmount));
			} else {
				double credit = -localAmount;
				totalCredit += credit;
				ppj.addDetailRecordField("amount",amountFormat.format(credit));
			}
		}

		ppj.addBottomField("val_ptotal",amountFormat.format(totalDebit));
		ppj.addBottomField("val_ptotal","=============",0,20);
		ppj.addBottomField("val_ntotal",amountFormat.format(totalCredit));
		ppj.addBottomField("val_ntotal","=============",0,20);
		ppj.addBottomField("val_remark","Last update: " + dateTimeFormat.format(new Date()),0,100);
		ppj.addBottomField("val_remark","Prepared by: " + sh.getLoginId(),0,125);
		ppj.addBottomField("val_remark","Checked by: ___________________",250,125);
		ppj.addBottomField("val_remark","Approved by: ___________________",500,125);
	}

	@Override
	protected ReturnMsg emailPdf(JxZkBiBase jxf,List<BiCellCollection> documents,
			byte[] pdf) throws Exception {
		String configuredRecipients = Erpv4Config.getString(
				jxf.getBr().getSessionHelper(),EMAIL_TO_CONFIG);
		Set<String> addresses = splitAddresses(configuredRecipients);
		if(addresses.isEmpty())
			return new ReturnMsg(false,"Configure " + EMAIL_TO_CONFIG
					+ " before emailing G/L vouchers.");

		List<Pair<String,String>> to = new ArrayList<Pair<String,String>>();
		for(String address : addresses) to.add(Pair.of(address,(String)null));

		String voucherNo = jxf.getBr().getCellString("tr_srcno");
		String subject = "G/L Voucher " + voucherNo;
		String html = "<html><body>Attached please find G/L voucher "
				+ escapeHtml(voucherNo) + ".</body></html>";
		File attachmentFile = File.createTempFile("GlVoucher-",".pdf");
		try {
			Files.write(attachmentFile.toPath(),pdf);
			EmailAttachment attachment = new EmailAttachment();
			attachment.setPath(attachmentFile.getAbsolutePath());
			attachment.setName(getDownloadFileName());
			attachment.setDescription("G/L Voucher");
			attachment.setDisposition(EmailAttachment.ATTACHMENT);
			List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
			attachments.add(attachment);
			return ZkUtil.sendEmail(null,to,null,null,subject,html,"",attachments,
					jxf.getBr().getSessionHelper());
		} finally {
			if(attachmentFile.exists() && !attachmentFile.delete())
				UniLog.log("Unable to delete temporary G/L voucher "
						+ attachmentFile.getAbsolutePath());
		}
	}

	private Set<String> splitAddresses(String value) {
		Set<String> result = new LinkedHashSet<String>();
		if(value != null) {
			for(String address : value.split("[,;]"))
				if(!StringUtils.isBlank(address)) result.add(address.trim());
		}
		return result;
	}

	private String stringValue(Object value) {
		return value == null ? "" : value.toString();
	}

	private String safeFilePart(String value) {
		return value == null ? "" : value.trim().replaceAll("[^A-Za-z0-9_-]","_");
	}

	private String escapeHtml(String value) {
		if(value == null) return "";
		return value.replace("&","&amp;").replace("<","&lt;")
				.replace(">","&gt;").replace("\"","&quot;");
	}
}
