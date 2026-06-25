package com.uniinformation.dynamic.afs;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.BatchPrtdocHandler;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class PrintGlVoucher extends BatchPrtdocHandler {
	public PrintGlVoucher() {
		super(null);
		// TODO Auto-generated constructor stub
	}
	public PrintGlVoucher(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void print() throws Exception {
		// TODO Auto-generated method stub
		if(docCnt > 0) {
        	if(docCnt > 0) ppj.newContent();
		}
        docCnt++;
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
    	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	DecimalFormat df = new DecimalFormat("#,##0.00");
    	DecimalFormat df1 = new DecimalFormat("##0.0000");
        String cocode = Erpv4Config.getDefaultCoCode(sh);
        Map<String, Object> coMap = Erpv4Config.getCoFieldMap(sh, cocode);
    	ppj.setTrailerAtLastPageOnly(false);
    	ppj.addHeaderField("cvname", (String)coMap.get("co_coname"),0,0);
    	ppj.addHeaderField("cvname", (String)coMap.get("co_chnname"),0,20);
    	ppj.addHeaderField("dflabel","Transaction",0,0);
    	ppj.addHeaderField("dflabel","Voucher No",0,20);
    	ppj.addHeaderField("dflabel","Date",0,40);
    	ppj.addHeaderField("dfvalue",""+br.getCellInt("tr_xno"),0,0);
    	ppj.addHeaderField("dfvalue",br.getCellString("tr_srcno"),0,20);
    	ppj.addHeaderField("dfvalue", sdf.format(br.getCellDate("tr_xdate")), 0,40);

    	ppj.addHeaderField("hdr_itemcode", "Account Code");
    	ppj.addHeaderField("hdr_description", "Description");
    	ppj.addHeaderField("hdr_qty", "CCY");
    	ppj.addHeaderField("hdr_uprice", "Amount");
    	ppj.addHeaderField("hdr_discount", "Rate");
    	ppj.addHeaderField("hdr_pamount", "Debit HKD");
    	ppj.addHeaderField("hdr_amount", "Credit HKD");
    	
    	double totalDebit = 0.0;
    	double totalCredit  = 0.0;
		for (BiCellCollection c : br.getSubLink("erpv4.GlJn").getRowCollectionList()) {
			ppj.addDetailRecord();
			ppj.addDetailRecordField("itemcode", c.getCellString("ca_ano"));
			ppj.addDetailRecordField("description", c.getCellString("jn_desc0"));
			ppj.addDetailRecordField("quantity", c.getCellString("cc_cid"));
			ppj.addDetailRecordField("price", df.format(c.getCellDouble("jn_amount")));
			ppj.addDetailRecordField("discount", df1.format(c.getCellDouble("set_xrate")));
			double dd = c.getCellDouble("jn_lamount");
			if(dd >= 0) {
				totalDebit += dd;
				ppj.addDetailRecordField("pamount", df.format(c.getCellDouble("jn_lamount")));
			} else {
				totalCredit += dd;
				ppj.addDetailRecordField("amount", df.format(c.getCellDouble("jn_lamount")));
			}
		}
		ppj.addBottomField("val_ptotal", df.format(totalDebit));
		ppj.addBottomField("val_ptotal", "=============",0,20);
		ppj.addBottomField("val_ntotal", df.format(totalCredit));
		ppj.addBottomField("val_ntotal", "=============",0,20);
		ppj.addBottomField("val_remark", "Input: ___________________",0,150);
		ppj.addBottomField("val_remark", "Review: ___________________",250,150);
//    	ppj.addHeaderField("companyPhone", String.format("電話 TEL: %s       傳真 FAX: %s       准照編號 LIC.: %s", coMap.get("co_telnum"), coMap.get("co_faxnum"), coMap.get("co_license")));
//    	ppj.addHeaderField("noLabel", "No.:");
		
//    	ppj.addHeaderField("noBValue", br.getCellString("col_b"));
//    	ppj.addHeaderField("title", "正式收據 OFFICAL RECEIPT", 0, 20);
//    	ppj.addHeaderField("noLabel", "參考編號 Ref:", 0, 42);
//    	ppj.addHeaderField("noValue", br.getCellString("col_r"), 0, 42);
//    	ppj.addHeaderField("noLabel", "日期 Date:", 0, 62);
//    	ppj.addHeaderField("noValue", br.getCellString("col_a"), 0, 62);
//    	ppj.addHeaderField("noLabel", "付款方式 Payment type:", 0, 82);
//    	ppj.addHeaderField("noValue", br.getCellString("ppm_name"), 0, 82);
//    	
//    	int offset = 0;
//    	ppj.addHeaderField("hdr80", "#", 0, 15);
//    	offset += 80;
//    	ppj.addHeaderField("hdr230", "繳費單位", offset, 5);
//    	ppj.addHeaderField("hdr230", "Property Unit", offset, 25);
//    	offset += 230;
//    	ppj.addHeaderField("hdr80", "繳費由月份", offset, 5);
//    	ppj.addHeaderField("hdr80", "Month from", offset, 25);
//    	offset += 80;
//    	ppj.addHeaderField("hdr80", "繳費至月份", offset, 5);
//    	ppj.addHeaderField("hdr80", "Month to", offset, 25);
//    	offset += 80;
//    	ppj.addHeaderField("hdr80", "月數", offset, 5);
//    	ppj.addHeaderField("hdr80", "No of mon.", offset, 25);
//    	offset += 80;
//    	ppj.addHeaderField("hdr100", "每月金額", offset, 5);
//    	ppj.addHeaderField("hdr100", "Monthly fee", offset, 25);
//    	offset += 100;
//    	ppj.addHeaderField("hdr110", "合計", offset, 5);
//    	ppj.addHeaderField("hdr110", "Total amount", offset, 25);
//
//    	boolean paidMgtFee = br.getCellBoolean("col_n");
//    	boolean paidResFee = br.getCellBoolean("col_o");
//    	double discount = br.getCellDouble("col_q");
//    	double actualFee = br.getCellDouble("vcol_actualfee");
//
//    	List<PayItem> payItemList = new ArrayList<PayItem>();
//    	PayItem lpi = null;
    	
//    	String pmContact = null;
//		for (BiCellCollection c : br.getSubLink("propertymgmt.payitem").getRowCollectionList()) {
//			PayItem pi = new PayItem();
//			pi.propUnit = c.getCellString("col_c");
//			pi.startDate = pi.endDate = sdf.parse(c.getCellString("col_d") + "-01");
//			pi.monthCount = 1;
//			pi.mgtFee = paidMgtFee ? c.getCellDouble("col_e") : 0;
//			pi.resFee = paidResFee ? c.getCellDouble("col_f") : 0;
//			
//			String ss = c.getCellString("pm_col_k");
//			if(!StringUtils.isBlank(ss)) {
//				if(pmContact == null) {
//					pmContact = ss; 
//				} else if(!pmContact.equals(ss)) {
//					pmContact = "";
//				} else {
//					pmContact = ss;
//				}
//			}
//			if (lpi == null || !StringUtils.equals(lpi.propUnit, pi.propUnit) 
//					|| DateUtil.nextmonth(lpi.endDate).compareTo(pi.endDate) != 0 
//					|| lpi.mgtFee != pi.mgtFee || lpi.resFee != pi.resFee) {
//				payItemList.add(pi);
//				lpi = pi;
//			} else {
//				lpi.endDate = pi.startDate;
//				lpi.monthCount++;
//			}
//		}
//		
//		for (PayItem pi : payItemList) {
//			if (paidMgtFee) {
//				addDetailRow("管理費", pi.propUnit, sdf1.format(pi.startDate), 
//							sdf1.format(pi.endDate), String.valueOf(pi.monthCount), 
//							df.format(pi.mgtFee), df.format(pi.mgtFee * pi.monthCount));
//			}
//			if (paidResFee) {
//				addDetailRow("儲備金", pi.propUnit, sdf1.format(pi.startDate), 
//							sdf1.format(pi.endDate), String.valueOf(pi.monthCount), 
//							df.format(pi.resFee), df.format(pi.resFee * pi.monthCount));
//			}
//		}
//		if (discount != 0) {
//			ppj.addDetailRecord();
//			ppj.addDetailRecordField("detLeft", "預繳優惠");
//			ppj.addDetailRecordField("detRight110", "-" + df.format(discount), 760 - 110, 0);
//		}
//		ppj.addDetailEndField("dtendRight200", "收款金額 (MOP):", 760 - 110 - 205, 0);
//		ppj.addDetailEndField("dtendRight110", df.format(actualFee), 760 - 110, 0);
//		
//    	ppj.addHeaderField("cvname", "聯絡人Contact:",0,-50);
//    	if(pmContact != null) {
//    		ppj.addHeaderField("cvname", pmContact,100,-50);
//    	}
//    	//ppj.addHeaderField("noValue", br.getCellString("ppm_name"), 0, 82);
//		
//		RpcClient rpc = sh.getRpcClient();
//		Value v1 = rpc.callSegment("saynumber",
//					new VectorUtil()
//						.addElement("CHINESE")
//						.addElement(actualFee)
//						.toVector()
//				);
//		Value v2 = rpc.callSegment("saynumber",
//					new VectorUtil()
//						.addElement("ENGLISH")
//						.addElement(actualFee)
//						.toVector()
//				);
//		rpc.close();
//		if (v1 != null)
//			ppj.addBottomField("bottomLeft9f", "交來澳門元" + v1.toString().replaceAll("\\s", "") + "元整");
//		if (v2 != null)
//			ppj.addBottomField("bottomRight9f", "The sum of MOP " + v2.toString() + " Dollars.");
//		
//		ppj.addBottomField("bottomLeft9f", "系統產生的收據，無需簽署。System generated receipt, no signature is required.", 0, 30);
//		ppj.addBottomField("bottomRight9f", "打印時間 Print time: " + sdf2.format(new Date()), 0, 30);
	}

	private void addDetailRow(String...p) throws JSONException {
//		ppj.addDetailRecord();
//		int offset = 0;
//		ppj.addDetailRecordField("detLeft", p[0]);
//		offset += 80;
//		ppj.addDetailRecordField("detLeft", p[1], offset, 0, 20, 230);
//		offset += 230;
//		ppj.addDetailRecordField("detLeft", p[2], offset, 0);
//		offset += 80;
//		ppj.addDetailRecordField("detLeft", p[3], offset, 0);
//		offset += 80;
//		ppj.addDetailRecordField("detCenter80", p[4], offset, 0);
//		offset += 80;
//		ppj.addDetailRecordField("detRight100", p[5], offset, 0);
//		offset += 100;
//		ppj.addDetailRecordField("detRight110", p[6], offset, 0);
	}

	@Override
	protected ReturnMsg initPrtdoc() {
		try {
			String docCode = "GLVOUCHER";
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			ppj = PrtdocJson.newPrtdocJson(	
    				cocode,
    				"A4P",
    			    docCode,
    			    "erpv4_printDocument"
			);
			ppj.setTopLeftMargin(0);
			docCnt = 0;
			//ppj.addHeaderField("doctitle","Quotation");
			//ppj.addHeaderImage("logo", Erpv4Config.getString(br.getSessionHelper(), "QuoBgImage"),0,0,0,800);    	
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return ReturnMsg.defaultOk;
	}

	@Override
	protected String getDocumentName(BiResult p_br) {
		return ("Voucher");
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		batchDownloadReport = true;
		ReturnMsg rtn = super.beforeAction(p_result, cnt);
		return(rtn);
	}
}
