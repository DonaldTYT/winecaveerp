package com.uniinformation.dynamic.clerpmulti;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.utils.MoneyToChinese;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultMO;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.BatchPrtdocHandler;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class PrintSalesInvoice extends BatchPrtdocHandler {
	public PrintSalesInvoice() {
		super(null);
		// TODO Auto-generated constructor stub
	}
	public PrintSalesInvoice(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void print() throws Exception {
		ppj.setSkipB2GConvert(true);
		// TODO Auto-generated method stub
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
    	ppj.setTrailerAtLastPageOnly(false);
    	
				String module = br.getCellString("stm_module");
				BiResult ssr = br.getSubLink(((BiResultMO)br).getStmdLinkName());
				Vector<BiCellCollection> v = ssr.getRowCollectionList();

				ppj.setTrailerAtLastPageOnly(true);
//				ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
				ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(br.getSessionHelper()), 50, 30, 120, 0);
				
//				ppj.addHeaderField("doctitle2","Receipt of Pharamceutical Product",0,130);
				ppj.addHeaderField("doctitle2","Invoice",0,120);
//				ppj.addHeaderField("docinfo","The following items are classified controlled drugs / antibiotics",0,130);
//				ppj.addHeaderField("docinfo","Please chop, sign and return back to us within 72 hours for processing",0,150);
//				ppj.addHeaderField("docinfo","Customer Name:",0,180);
				TableRec tr = br.getSelectUtil().getQueryResult("select * from location,locationcode where loc_code = '"+ br.getCellString("stm_toloc")+"' and lc_rg = loc_mrg");
				tr.setRecPointer(0);
				String toCoName = Erpv4Config.getCoName(br.getSessionHelper(), tr.getFieldString("loc_cocode"));
				String toLoc    =tr.getFieldString("lc_addr1");
				String toTel    = tr.getFieldString("lc_tel");
				String toFax    = tr.getFieldString("lc_fax");

				ppj.addHeaderField("docinfo","No.:",530,130);
				ppj.addHeaderField("docinfo",br.getCellString("stm_ref1"),570,130);
				
				ppj.addHeaderField("docinfo","Customer",0,130);
				ppj.addHeaderField("docinfo3",toCoName,120,130);
				ppj.addHeaderField("docinfo","Date:",520,150);
				ppj.addHeaderField("docinfo",DateUtil.dateToDateTimeStr((br.getCell("stm_date")).getDate(), "dd/MM/yyyy"),570,150);
				
				ppj.addHeaderField("docinfo","Address:",0,150);
				ppj.addHeaderField("docinfo3",toLoc,120,150);
				ppj.addHeaderField("docinfo","Telephone:",0,220);
				ppj.addHeaderField("docinfo",toTel,120,220);

				ppj.useDetailGroup("detailh1");
				ppj.addDetailRecord();
				ppj.addDetailRecordField("h1c1", "Drugs");
				ppj.addDetailRecordField("h1c2", "Batch No.");
				ppj.addDetailRecordField("h1c3", "Expiry Date");
				ppj.addDetailRecordField("h1c4", "Quantity");
				ppj.addDetailRecordField("h1c5", "Unit Price");
				ppj.addDetailRecordField("h1c6", "Subtotal");

				double tAmt = 0.0f;
				ppj.useDetailGroup("");
					for(BiCellCollection c : v) {
						ppj.addDetailRecord();
						String s = "";
						s += c.getCellString("st_iname");
						ppj.addDetailRecordField("d1drugname", s);

						s = c.getCellString("stmd_lotno");
						ppj.addDetailRecordField("d1batchno", s);

//						s = c.getCellString("stmd_exprdate");
//						ppj.addDetailRecordField("d1expiredate", s);
						s = DateUtil.dateToDateTimeStr((c.getCell("stmd_exprdate")).getDate(), "dd/MM/yyyy");
						ppj.addDetailRecordField("d1expiredate", s);

						s = c.getCellString("stmd_qty")+" "+c.getCellString("st_unit");
						ppj.addDetailRecordField("d1qty", s);
						
						double up = c.getCellDouble("stmd_uprice");
						up = CellCollection.round(up + 0.05, 0.1);
						ppj.addDetailRecordField("d1price", df.format(up));
						
						double st = up * c.getCellDouble("stmd_qty");
//						double st = c.getCellDouble("stmd_exprice");
						ppj.addDetailRecordField("d1total", df.format(st));
						tAmt += st;
					}
					
//				tAmt = -br.getCellDouble("stm_tolAmt");
				ppj.addBottomField("hdr_total", "Total Amount",0,-50);
				ppj.addBottomField("val_total", df.format(tAmt) ,0,-50);
				ppj.addBottomField("val_remark", 
						"Please make your cheque payable to  Pedder Health Pharma Company Limited and settle the amount within one month from the date of this invoice."
						,0,0);
				ppj.addBottomField("val_remark", 
						"Should you have any enquiries, please contact our Accounts Department by:"
						,0,90);
				ppj.addBottomField("val_remark", 
						"-Email: account@pedderhealth.com"
						,0,130);
				ppj.addBottomField("val_remark", 
						"-Telephone: 2905 8294 (Lydia Lam)"
						,0,150);
				
				
//				double tamt = -br.getCellDouble("stm_tolamt");
//				ppj.addBottomField("val_sighdr", df.format(tamt) ,200,0);
				ppj.addBottomField("val_sighdr", "_______________________________________",0,40);
//				ppj.addBottomField("val_signco", "Company Stamp",0,60);
				ppj.addBottomField("val_signco", "Stamp and Signature of Pharmacist",0,60);
				ppj.addBottomField("val_signco", "(Pedder Health Pharmacy)",0,80);
				/*
				ppj.addBottomField("val_sighdr", "_______________________________________",350,40);
				ppj.addBottomField("val_signco", "Stamp and Sigature of Authorized Person",350,60);
				*/

//				ppj.newContent();
//				ppj.setTrailerAtLastPageOnly(true);
//				ppj.addHeaderField("doctitle2","Written Order",0,150);
//				ppj.addHeaderField("docinfo","To:Pedder Health Pharmacy",0,0);
//				ppj.addHeaderField("docinfo","Writen Order No.: _______________",400,0);
//				ppj.addHeaderField("docinfo",br.getCellString("stm_ref1"),570,0);
//				ppj.addHeaderField("docinfo","Date:",513,25);
//				ppj.addHeaderField("docinfo",DateUtil.dateToDateTimeStr((br.getCell("stm_date")).getDate(), "dd/MMM/yyyy"),565,25);
//				ppj.addHeaderField("docinfo","O/B Pedder Health Pharma Company Limited",0,20);
//				ppj.addHeaderField("docinfo","1011-1016, 10/F., H Zentre",0,40);
//				ppj.addHeaderField("docinfo","15 Middle Road, Tsim Sha Tsui",0,60);
//				ppj.addHeaderField("docinfo","Tel: (852) 2905 8333",0,80);
//				ppj.addHeaderField("docinfo","Fax: (852) 2905 8222",0,100);
//				
//				ppj.useDetailGroup("detailh2");
//				ppj.addDetailRecord();
//				ppj.addDetailRecordField("h2c1", "Item No.");
//				ppj.addDetailRecordField("h2c2", "Drug Name");
//				ppj.addDetailRecordField("h2c3", "Quantity");
//				
//				ppj.useDetailGroup("detailr2");
//					int n = 1;
//					for(BiCellCollection c : v) {
//						ppj.addDetailRecord();
//						String s = "";
//						ppj.addDetailRecordField("d2itemno", ""+n);
//						s += c.getCellString("st_iname");
//						ppj.addDetailRecordField("d2drugname", s);
//						s = c.getCellString("stmd_qty")+" "+c.getCellString("st_unit");
//						ppj.addDetailRecordField("d2qty", s);
//						n++;
//					}
//   				ppj.addBottomField("val_remark", "Purpose: For medical treatment use only", 0, 0);
//   				ppj.addBottomField("val_remark", "Deliver To: " + toCoName,0,20);
//   				ppj.addBottomField("val_remark", "" + toLoc,0,40);
//   				ppj.addBottomField("val_remark", "Tel: " ,0,60);
//   				ppj.addBottomField("val_remark", "Fax: " ,0,80);
//   				ppj.addBottomField("val_remark", toTel ,50,60);
//   				ppj.addBottomField("val_remark", toFax ,50,80);
//				ppj.addBottomField("val_sighdr", "_______________________________________",350,40);
//				ppj.addBottomField("val_signco", "Stamp and Signature of Authorized Person",350,60);
//				
    	
	}

	private void addDetailRow(String...p) throws JSONException {
		ppj.addDetailRecord();
		int offset = 0;
		ppj.addDetailRecordField("detLeft", p[0]);
		offset += 80;
		ppj.addDetailRecordField("detLeft", p[1], offset, 0, 20, 230);
		offset += 230;
		ppj.addDetailRecordField("detLeft", p[2], offset, 0);
		offset += 80;
		ppj.addDetailRecordField("detLeft", p[3], offset, 0);
		offset += 80;
		ppj.addDetailRecordField("detCenter80", p[4], offset, 0);
		offset += 80;
		ppj.addDetailRecordField("detRight100", p[5], offset, 0);
		offset += 100;
		ppj.addDetailRecordField("detRight110", p[6], offset, 0);
	}

	@Override
	protected ReturnMsg initPrtdoc() {
		try {
			String docCode = "GENRCP02";
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
		return ("Invoice");
	}

	
	private class PayItem {
		String propUnit;
		Date startDate;
		Date endDate;
		int monthCount;
		double mgtFee;
		double resFee;
	}
	
	@Override
	public boolean isVisible(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(false);
		if(p_isBatch) return(false);
		return(true);
	}
	
	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		if(p_isBatch) {
			return(false);
		} else {
			if(p_br.inBeginWork()) return(true);
			String qs = p_br.getCellString("stm_toloc");
			if(qs.equals("PO005")) {
				return(false);
			} else {
				return(true);
			}
		}
	}
}
