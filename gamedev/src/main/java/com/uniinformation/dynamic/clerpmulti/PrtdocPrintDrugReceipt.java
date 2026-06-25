package com.uniinformation.dynamic.clerpmulti;

import java.io.ByteArrayOutputStream;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultMO;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;

public class PrtdocPrintDrugReceipt extends PrtdocClass  {
	BiResultMO br;
    PrtdocJson ppj;
    int docCnt;
    String cocode;
    String paperType;
    String docCode;
	public PrtdocPrintDrugReceipt (BiResultMO p_br) throws Exception {
    	docCode = "GENRCP01";
    	cocode = Erpv4Config.getDefaultCoCode(p_br.getSessionHelper());
    	paperType = "A4P";
    	ppj = PrtdocJson.newPrtdocJson(	
    				cocode,
    				paperType,
    			    docCode,
    			    "erpv4_printDocument",
    			    PrtdocJson.Encoding.UTF8
    	);
		br = p_br;
		docCnt = 0;
//    	ppj.addHeaderField("doctitle","Quotation");
//  	ppj.addHeaderImage("logo", Erpv4Config.getString(br.getSessionHelper(), "QuoBgImage"),0,0,0,800);
	}
	
	@Override
	public void print() throws Exception {
				String module = br.getCellString("stm_module");
				BiResult ssr = br.getSubLink(br.getStmdLinkName());
				Vector<BiCellCollection> v = ssr.getRowCollectionList();

				ppj.setTrailerAtLastPageOnly(true);
//				ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
				ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(br.getSessionHelper()), 50, 30, 120, 0);
//				ppj.addHeaderField("doctitle",Erpv4Config.getCoName(br.getSessionHelper(),Erpv4Config.getDefaultCoCode(br.getSessionHelper())));
//				ppj.addHeaderField("doctitle","Pedder Health Pharmacy");
				
//				ppj.addHeaderField("docinfo","O/B Pedder Health Pharma Co., Ltd",0,0);
//				ppj.addHeaderField("docinfo","Address: Portion of shop 1011-16, 10/F., H Zentre, 15 Middle Road,",0,20);
//				ppj.addHeaderField("docinfo","Tsim Sha Tsui, Kowloon,",0,40);
//				ppj.addHeaderField("docinfo","Tel: (852) 2905 8271,",0,70);
//				ppj.addHeaderField("docinfo","Fax: (852) 2905 8222,",300,70);

				/*
				ppj.addHeaderField("docinfo2","O/B Pedder Health Pharma Co., Ltd",0,0);
				ppj.addHeaderField("docinfo2","Address: Portion of shop 1011-16, 10/F.,",0,16);
				ppj.addHeaderField("docinfo2","Address: H Zentre, 15 Middle Road,",0,32);
				ppj.addHeaderField("docinfo2","Tsim Sha Tsui, Kowloon,",0,48);
				ppj.addHeaderField("docinfo2","Tel: (852) 2905 8271,",0,64);
				ppj.addHeaderField("docinfo2","Fax: (852) 2905 8222,",0,80);
				*/
				
//				ppj.addHeaderField("doctitle2","Receipt of Pharamceutical Product",0,130);
				ppj.addHeaderField("doctitle2","Poison Form / Delivery Note",0,120);
				ppj.addHeaderField("docinfo","The following items are classified controlled drugs / antibiotics",0,130);
				ppj.addHeaderField("docinfo","Please chop, sign and return back to us within 72 hours for processing",0,150);
				ppj.addHeaderField("docinfo","Customer Name:",0,180);
				TableRec tr = br.getSelectUtil().getQueryResult("select * from location,locationcode where loc_code = '"+ br.getCellString("stm_toloc")+"' and lc_rg = loc_mrg");
				tr.setRecPointer(0);
				String toCoName = Erpv4Config.getCoName(br.getSessionHelper(), tr.getFieldString("loc_cocode"));
				String toLoc    =tr.getFieldString("lc_addr1");
				String toTel    = tr.getFieldString("lc_tel");
				String toFax    = tr.getFieldString("lc_fax");
				ppj.addHeaderField("docinfo",toCoName,160,180);
				ppj.addHeaderField("docinfo","Date:",550,180);
				ppj.addHeaderField("docinfo",DateUtil.dateToDateTimeStr((br.getCell("stm_date")).getDate(), "dd/MMM/yyyy"),600,180);
				ppj.addHeaderField("docinfo","No.:",530,90);
				ppj.addHeaderField("docinfo",br.getCellString("stm_ref1"),570,90);
				ppj.addHeaderField("docinfo","Location:",0,200);
				ppj.addHeaderField("docinfo",toLoc,160,200);
				ppj.addHeaderField("docinfo","Telephone:",0,220);
				ppj.addHeaderField("docinfo",toTel,160,220);
				ppj.useDetailGroup("detailh1");
				ppj.addDetailRecord();
				ppj.addDetailRecordField("h1c1", "Drugs");
				ppj.addDetailRecordField("h1c2", "Batch No.");
				ppj.addDetailRecordField("h1c3", "Expiry Date");
				ppj.addDetailRecordField("h1c4", "Quantity");

				ppj.useDetailGroup("");
					for(BiCellCollection c : v) {
						ppj.addDetailRecord();
						String s = "";
						s += c.getCellString("st_iname");
						ppj.addDetailRecordField("d1drugname", s);

						s = c.getCellString("stmd_lotno");
						ppj.addDetailRecordField("d1batchno", s);

						s = c.getCellString("stmd_exprdate");
						ppj.addDetailRecordField("d1expiredate", s);

						s = c.getCellString("stmd_qty")+" "+c.getCellString("st_unit");
						ppj.addDetailRecordField("d1qty", s);
					}
					
				ppj.addBottomField("val_sighdr", "_______________________________________",0,40);
//				ppj.addBottomField("val_signco", "Company Stamp",0,60);
				ppj.addBottomField("val_signco", "Stamp and Signature of Pharmacist",0,60);
				ppj.addBottomField("val_signco", "(Pedder Health Pharmacy)",0,80);
				ppj.addBottomField("val_sighdr", "_______________________________________",350,40);
				ppj.addBottomField("val_signco", "Stamp and Sigature of Authorized Person",350,60);

				ppj.newContent();
				ppj.setTrailerAtLastPageOnly(true);
//				ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
				ppj.addHeaderField("doctitle2","Written Order",0,150);
				ppj.addHeaderField("docinfo","To:Pedder Health Pharmacy",0,0);
				ppj.addHeaderField("docinfo","Writen Order No.: _______________",400,0);
				ppj.addHeaderField("docinfo",br.getCellString("stm_ref1"),570,0);
				ppj.addHeaderField("docinfo","Date:",513,25);
				ppj.addHeaderField("docinfo",DateUtil.dateToDateTimeStr((br.getCell("stm_date")).getDate(), "dd/MMM/yyyy"),565,25);
				ppj.addHeaderField("docinfo","O/B Pedder Health Pharma Company Limited",0,20);
				ppj.addHeaderField("docinfo","1011-1016, 10/F., H Zentre",0,40);
				ppj.addHeaderField("docinfo","15 Middle Road, Tsim Sha Tsui",0,60);
				ppj.addHeaderField("docinfo","Tel: (852) 2905 8333",0,80);
				ppj.addHeaderField("docinfo","Fax: (852) 2905 8222",0,100);
//				ppj.addHeaderField("docinfo","Date: _______________",0,150);
				
				ppj.useDetailGroup("detailh2");
				ppj.addDetailRecord();
				ppj.addDetailRecordField("h2c1", "Item No.");
				ppj.addDetailRecordField("h2c2", "Drug Name");
				ppj.addDetailRecordField("h2c3", "Quantity");
				
				ppj.useDetailGroup("detailr2");
					int n = 1;
					for(BiCellCollection c : v) {
						ppj.addDetailRecord();
						String s = "";
						ppj.addDetailRecordField("d2itemno", ""+n);
						s += c.getCellString("st_iname");
						ppj.addDetailRecordField("d2drugname", s);
						s = c.getCellString("stmd_qty")+" "+c.getCellString("st_unit");
						ppj.addDetailRecordField("d2qty", s);
						n++;
					}
   				ppj.addBottomField("val_remark", "Purpose: For medical treatment use only", 0, 0);
   				ppj.addBottomField("val_remark", "Deliver To: " + toCoName,0,20);
   				ppj.addBottomField("val_remark", "" + toLoc,0,40);
   				ppj.addBottomField("val_remark", "Tel: " ,0,60);
   				ppj.addBottomField("val_remark", "Fax: " ,0,80);
   				ppj.addBottomField("val_remark", toTel ,50,60);
   				ppj.addBottomField("val_remark", toFax ,50,80);
				ppj.addBottomField("val_sighdr", "_______________________________________",350,40);
				ppj.addBottomField("val_signco", "Stamp and Signature of Authorized Person",350,60);
				
				/*
					if(module.equals("cstmo")) {
						ppj.addHeaderField("doctitle","Stock Out");	
						ppj.addHeaderField("cvname","From: " + br.getCellString("floc_desc"),-2,-40);
						ppj.addHeaderField("cvname",br.getCellString("vd_vname"),0,0);
						ppj.addHeaderField("dflabel","No.");
						ppj.addHeaderField("dfvalue",br.getCellString("stm_ref1"),0,0);
						ppj.addHeaderField("dflabel","Date",0,30);
						ppj.addHeaderField("dfvalue"," "+DateUtil.toDateString( br.getCell("stm_date").getDate(),"yyyy/mm/dd") ,0,30);
						ppj.addHeaderField("dflabel","Page",0,60);

						ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
						ppj.addDetailHeaderField("hdr_qty","QTY");
						ppj.addDetailHeaderField("hdr_uprice","UNIT PRICE",0,-5);
						ppj.addDetailHeaderField("hdr_uprice","("+ br.getCellString("stm_cur")+")",0,12);
						ppj.addDetailHeaderField("hdr_amount","AMOUNT");
						
					} else if(module.equals("vstmo")) {
						ppj.addHeaderField("cvname","From: " + br.getCellString("floc_desc"),0,-30);
						ppj.addHeaderField("doctitle","Stock In");	
					} else if(module.equals("stadj")) {
						ppj.addHeaderField("doctitle","Adjustment");	
						ppj.addHeaderField("cvname","" + br.getCellString("floc_desc"),-2,0);
						ppj.addHeaderField("dflabel","No.");
						ppj.addHeaderField("dfvalue",br.getCellString("stm_ref1"),0,0);
						ppj.addHeaderField("dflabel","Date",0,30);
						ppj.addHeaderField("dfvalue"," "+DateUtil.toDateString( br.getCell("stm_date").getDate(),"yyyy/mm/dd") ,0,30);
						ppj.addHeaderField("dflabel","Page",0,60);
						ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
						ppj.addDetailHeaderField("hdr_qty","QTY");
					} else if(module.equals("sttfr")) {
						ppj.addHeaderField("doctitle","Drug Transfer");	
						ppj.addHeaderField("cvname","From: " + br.getCellString("floc_desc"),-2,-40);
						ppj.addHeaderField("cvname",br.getCellString("tloc_desc"),40,-20);
//						ppj.addHeaderField("cvname",br.getCellString("tloc_desc"),0,0);
						ppj.addHeaderField("dflabel","No.");
						ppj.addHeaderField("dfvalue",br.getCellString("stm_ref1"),0,0);
						ppj.addHeaderField("dflabel","Date",0,30);
						ppj.addHeaderField("dfvalue"," "+DateUtil.toDateString( br.getCell("stm_date").getDate(),"yyyy/mm/dd") ,0,30);
						ppj.addHeaderField("dflabel","Page",0,60);

						ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
						ppj.addDetailHeaderField("hdr_qty","QTY");
						ppj.addDetailHeaderField("hdr_uprice","LOT NO");
						ppj.addDetailHeaderField("hdr_disc","UNIT");
						ppj.addDetailHeaderField("hdr_amount","EXPIRY DATE");
					}
					*/
					/*
					BiResult ssr = br.getSubLink(br.getStmdLinkName());
					Vector<BiCellCollection> v = ssr.getRowCollectionList();
					for(BiCellCollection c : v) {
						ppj.addDetailRecord();
						String s = "";
						s += c.getCellString("st_iname");
						s += "("+c.getCellString("st_icode")+")";
						ppj.addDetailRecordField("description", s);
//    					ppj.setBold(false);
//    					ppj.setUnderLine(false);
						ppj.addDetailRecordField("quantity", c.getCell("stmd_qty").getString());

						s = c.getCellString("stmd_lotno");
						ppj.addDetailRecordField("price", s);

						s = c.getCellString("stmd_entryunit");
						ppj.addDetailRecordField("discount", s);

						s = " " + c.getCellString("stmd_exprdate");
						ppj.addDetailRecordField("amount", s);
					}
					*/
		
	}
	@Override
	public PrtdocJson getPrintDocJson() throws Exception {
		return ppj;
	}
}
