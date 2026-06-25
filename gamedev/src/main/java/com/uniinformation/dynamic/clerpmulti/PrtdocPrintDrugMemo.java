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
import com.uniinformation.utils.UniLog;

public class PrtdocPrintDrugMemo extends PrtdocClass  {

	BiResultMO br;
    PrtdocJson ppj;
    int docCnt;
    String cocode;
    String paperType;
    String docCode;
	public PrtdocPrintDrugMemo (BiResultMO p_br) throws Exception {
    	docCode = "GENMMO01";
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
				ppj.setTrailerAtLastPageOnly(true);
				ppj.addHeaderImage("logo", Erpv4Config.getCompanyLogo(br.getSessionHelper(),"001"), 50, 30, 120, 0);
				ppj.addPageNo("pageno", "Page %s of %s",0, 0, 0);
				String module = br.getCellString("stm_module");
					if(module.equals("cstmo")) {
						ppj.addHeaderField("doctitle","Prescriptions Record");	
						ppj.addHeaderField("dflabel","Prescription No:");
						ppj.addHeaderField("dfvalue",br.getCellString("stm_ref1"),0,0);
						ppj.addHeaderField("dflabel","Dispensing Date:",0,30);
						ppj.addHeaderField("dfvalue"," "+DateUtil.toDateString( br.getCell("stm_date").getDate(),"yyyy/mm/dd") ,0,30);
						ppj.addHeaderField("dflabel","Prescription Date:",0,60);
						ppj.addHeaderField("dfvalue"," "+DateUtil.toDateString( br.getCell("stm_date").getDate(),"yyyy/mm/dd") ,0,60);

						ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
						ppj.addDetailHeaderField("hdr_qty","QTY");
						
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
						ppj.addHeaderField("doctitle","Transfer");	
						ppj.addHeaderField("cvname","From: " + br.getCellString("floc_desc"),-2,-40);
						ppj.addHeaderField("cvname",br.getCellString("tloc_desc"),0,0);
						ppj.addHeaderField("dflabel","No.");
						ppj.addHeaderField("dfvalue",br.getCellString("stm_ref1"),0,0);
						ppj.addHeaderField("dflabel","Date",0,30);
						ppj.addHeaderField("dfvalue"," "+DateUtil.toDateString( br.getCell("stm_date").getDate(),"yyyy/mm/dd") ,0,30);
						ppj.addHeaderField("dflabel","Page",0,60);

						ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
						ppj.addDetailHeaderField("hdr_qty","QTY");
					}
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
					}
					ppj.addBottomField("val_remark", br.getCellString("cldoc_name"));
	}
	@Override
	public PrtdocJson getPrintDocJson() throws Exception {
		return ppj;
	}

		public ReturnMsg printVoucher(ByteArrayOutputStream bos) {
			PrtdocJson ppj;
			if(paperType == null) {
				paperType = Erpv4Config.getString(br.getSessionHelper(),"MoPaperType");
				if(paperType == null || paperType.trim().equals("")) paperType = "A4P";
			}
			if(docCode == null) {
				docCode = Erpv4Config.getString(br.getSessionHelper(),"MoDocCode");
				if(docCode == null || docCode .trim().equals("")) docCode = "GENINV01";
			}
			try {
				ppj = PrtdocJson.newPrtdocJson(	
					Erpv4Config.getDefaultCoCode(br.getSessionHelper()),
				    paperType,
				    docCode,
				    "erpv4_printDocument"
				    ) ;
//					ppj.setTrailerAtLastPageOnly(true);
				ppj.setTrailerAtLastPageOnly(true);
				ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
				String module = br.getCellString("stm_module");
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
						ppj.addHeaderField("doctitle","Transfer");	
						ppj.addHeaderField("cvname","From: " + br.getCellString("floc_desc"),-2,-40);
						ppj.addHeaderField("cvname",br.getCellString("tloc_desc"),0,0);
						ppj.addHeaderField("dflabel","No.");
						ppj.addHeaderField("dfvalue",br.getCellString("stm_ref1"),0,0);
						ppj.addHeaderField("dflabel","Date",0,30);
						ppj.addHeaderField("dfvalue"," "+DateUtil.toDateString( br.getCell("stm_date").getDate(),"yyyy/mm/dd") ,0,30);
						ppj.addHeaderField("dflabel","Page",0,60);

						ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
						ppj.addDetailHeaderField("hdr_qty","QTY");
					}
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
					}
					
				return(ppj.toPdfStream(bos, br.getSessionHelper()));
			} catch(Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,ex.toString()));
			}
	    	
	    }	

}
