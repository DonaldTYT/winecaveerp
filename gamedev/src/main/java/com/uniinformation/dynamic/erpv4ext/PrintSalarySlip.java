package com.uniinformation.dynamic.erpv4ext;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.BatchPrtdocHandler;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.erpv4ext.PaymentMaster;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class PrintSalarySlip extends BatchPrtdocHandler{

	public PrintSalarySlip(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void print() throws Exception {
		if(docCnt > 0) {
        	if(docCnt > 0) ppj.newContent();
		}
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
    	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	DecimalFormat df = new DecimalFormat("$#,##0.00");
        docCnt++;
        String cocode = Erpv4Config.getDefaultCoCode(sh);
        Map<String, Object> coMap = Erpv4Config.getCoFieldMap(sh, cocode);
        int ofs1=-30;
    	ppj.setTrailerAtLastPageOnly(false);
		ppj.addHeaderImage("logo", Erpv4Config.getString(br.getSessionHelper(), "LogoImage"),50,50,100,0);    	
		ppj.addHeaderField("doctitle", "Salary Slip",500,-90,0,0);
		ppj.addHeaderField("dflabel", "Employee Code",0,0,0,0);
		ppj.addHeaderField("dflabel", "Pay Period From",0,30,0,0);
		ppj.addHeaderField("dflabel", "To",90,60,0,0);
		ppj.addHeaderField("dfvalue", br.getCellString("pm_eid"),0,0,0,0);
		ppj.addHeaderField("dfvalue", sdf.format(br.getCell("pm_date").getDate()) ,0,30,0,0);
		ppj.addHeaderField("dfvalue", sdf.format(br.getCell("pm_edate").getDate()) ,0,60,0,0);
		ppj.addHeaderField("clphone", "Name:",0,0+ofs1,0,0);
		ppj.addHeaderField("cvphone", br.getCellString("em_ename"),50,0+ofs1,0,0);
		ppj.addHeaderField("cvphone", br.getCellString("em_chinname"),50,30+ofs1,0,0);
//		ppj.addHeaderField("clphone", "Mobile:",0,60+ofs1,0,0);
//		ppj.addHeaderField("cvphone", br.getCellString("em_mobilephone"),50,60+ofs1,0,0);
		ppj.addHeaderField("clphone", "Title:",0,90+ofs1,0,0);
		ppj.addHeaderField("cvphone", br.getCellString("ptmt_name"),50,90+ofs1,0,0);
		ppj.addHeaderField("clphone", "Department:",0,120+ofs1,0,0);
		ppj.addHeaderField("cvphone", br.getCellString("dpmt_name"),50,120+ofs1,0,0);
		ppj.addHeaderField("hdr_description", "Description",0,0,0,0);
		ppj.addHeaderField("hdr_qty", "Total",0,0,0,0);
		for (BiCellCollection c : br.getSubLink("erpv4ext.PaymentItem").getRowCollectionList()) {
			ppj.addDetailRecord();
			ppj.addDetailRecordField("description", 
					PaymentMaster.makePmiXdesc(
							c.getCellString("pmi_type"),
							c.getCellString("pmi_code"),
							br.getSelectUtil()
					)
					);
			ppj.addDetailRecordField("quantity", df.format(
							c.getCellDouble("pmi_rincome") - c.getCellDouble("pmi_pension")
					));
		}

		ppj.addDetailRecord();
		ppj.addDetailRecord();
		ppj.addDetailRecordField("quantity", "__________",0,0,0,0);
		ppj.addDetailRecord();
		ppj.addDetailRecordField("description", "Net Pay");
		ppj.addDetailRecordField("quantity", df.format(br.getCellDouble("pm_xnet")));
		ppj.addDetailRecord();
		ppj.addDetailRecordField("quantity", "==========",0,0,0,0);
		
		/*
    	ppj.addHeaderField("companyZhName", (String)coMap.get("co_coname"));
    	ppj.addHeaderField("companyEnName", (String)coMap.get("co_chnname"));
    	ppj.addHeaderField("companyAddress", Erpv4Config.getCoAddr(sh, cocode));
    	ppj.addHeaderField("companyPhone", String.format("電話 TEL: %s       傳真 FAX: %s       准照編號 LIC.: %s", coMap.get("co_telnum"), coMap.get("co_faxnum"), coMap.get("co_license")));
    	ppj.addHeaderField("noLabel", "No.:");
    	ppj.addHeaderField("noBValue", br.getCellString("col_b"));
    	ppj.addHeaderField("title", "正式收據 OFFICAL RECEIPT", 0, 20);
    	ppj.addHeaderField("noLabel", "參考編號 Ref:", 0, 42);
    	ppj.addHeaderField("noValue", br.getCellString("col_r"), 0, 42);
    	ppj.addHeaderField("noLabel", "日期 Date:", 0, 62);
//    	ppj.addHeaderField("noValue", br.getCellString("col_a") , 0, 62);
    	ppj.addHeaderField("noValue", sdf.format(br.getCell("col_a").getDate()) , 0, 62);
    	ppj.addHeaderField("noLabel", "付款方式 Payment type:", 0, 82);
    	ppj.addHeaderField("noValue", br.getCellString("ppm_name"), 0, 82);
    	
    	int offset = 0;
    	ppj.addHeaderField("hdr80", "#", 0, 15);
    	offset += 80;
    	ppj.addHeaderField("hdr230", "繳費單位", offset, 5);
    	ppj.addHeaderField("hdr230", "Property Unit", offset, 25);
    	offset += 230;
    	ppj.addHeaderField("hdr80", "繳費由月份", offset, 5);
    	ppj.addHeaderField("hdr80", "Month from", offset, 25);
    	offset += 80;
    	ppj.addHeaderField("hdr80", "繳費至月份", offset, 5);
    	ppj.addHeaderField("hdr80", "Month to", offset, 25);
    	offset += 80;
    	ppj.addHeaderField("hdr80", "月數", offset, 5);
    	ppj.addHeaderField("hdr80", "No of mon.", offset, 25);
    	offset += 80;
    	ppj.addHeaderField("hdr100", "每月金額", offset, 5);
    	ppj.addHeaderField("hdr100", "Monthly fee", offset, 25);
    	offset += 100;
    	ppj.addHeaderField("hdr110", "合計", offset, 5);
    	ppj.addHeaderField("hdr110", "Total amount", offset, 25);

    	boolean paidMgtFee = br.getCellBoolean("col_n");
    	boolean paidResFee = br.getCellBoolean("col_o");
    	double discount = br.getCellDouble("col_q");
    	double actualFee = br.getCellDouble("vcol_actualfee");

    	List<PayItem> payItemList = new ArrayList<PayItem>();
    	PayItem lpi = null;
    	DecimalFormat df = new DecimalFormat("$#,##0.00");	
    	*/
	}

	@Override
	protected ReturnMsg initPrtdoc() {
		try {
			String docCode = "GENMMO01";
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
		// TODO Auto-generated method stub
		return ("Salary Slip");
	}

}
