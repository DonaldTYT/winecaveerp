package com.uniinformation.dynamic.chungkee;

import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.VectorUtil;

public class ChungkeePrintInvoice extends PrtdocClass  {
	BiResultQuotation br;
    PrtdocJson ppj;
    
	public ChungkeePrintInvoice (BiResultQuotation p_br) throws Exception {
    	String dnDoc = "CKHINV";
    	ppj = PrtdocJson.newPrtdocJson(	
    				"001",
    				"USER1",
    			    dnDoc,
    			    "erpv4_printDocument"
    	);
		br = p_br;
    	ppj.setTrailerAtLastPageOnly(true);
    	ppj.addPageNo("pageno", "%s of %s",0, 60, 0);
//    	ppj.addHeaderField("doctitle","Quotation");
//  	ppj.addHeaderImage("logo", Erpv4Config.getString(br.getSessionHelper(), "QuoBgImage"),0,0,0,800);
	}

	@Override
	public void print() throws Exception {
		// TODO Auto-generated method stub
		/*
    	String ds = DateUtil.toDateString( br.getCell("inv_date").getDate(),"yyyy/mm/dd");
    	ppj.addHeaderField("dfvalue", ds,-120,290);
    	ppj.addHeaderField("cicontent",br.getCellString("vd_vname"), -45, 150);
   		String addr = br.getCellString("vd_addr0").trim()+br.getCellString("vd_addr1").trim() + " " +  br.getCellString("vd_addr2").trim() + " " + br.getCellString("vd_addr3").trim();
    	ppj.addHeaderField("cicontent",addr,-45, 170);
    	ppj.addHeaderField("cicontent",br.getCellString("inv_contact"),-45,190);
    	ppj.addHeaderField("cicontent",br.getCellString("inv_tel"),-45,210);
    	*/
		/*
    	ppj.addHeaderField("logo","0123456789",0,500);
    	ppj.addHeaderField("logo","0123456789",100,500);
    	ppj.addHeaderField("logo","0123456789",200,500);
    	ppj.addHeaderField("logo","0123456789",300,500);
    	ppj.addHeaderField("logo","0123456789",400,500);
    	ppj.addHeaderField("logo","0123456789",500,500);
    	ppj.addHeaderField("logo","0123456789",600,500);
    	ppj.addHeaderField("logo","0123456789",700,500);
    	*/
		PrtdocJson.addMultiHeaderField(ppj, "ciname", 0, 0, 20, 0, 
				new VectorUtil()
				.addElement(br.getCellString("vd_vname"))
				.addElement(br.getCellString("inv_addr0"))
				/*
				.addElement(br.getCellString("inv_tel"))
				.addElement(br.getCellString("inv_fax"))
				.addElement(br.getCellString("vd_email"))
				*/
				.toVector()
				);
		PrtdocJson.addMultiHeaderField(ppj, "dfvalue", 0, 0, 20, 0, 
				new VectorUtil()
				.addElement(br.getCellString("inv_invno"))
				/*
				.addElement(br.getCellString("inv_tel"))
				.addElement(br.getCellString("inv_fax"))
				.addElement(br.getCellString("vd_email"))
				*/
				.toVector()
				);

    	ppj.addHeaderField("dfvalue",DateUtil.toDateString(br.getCellDate("inv_delidate"),"dd-mm-yyyy"),-50,115);
    	
   		Vector<BiCellCollection> v = null;
		v = br.getSubLink(br.get_subLinkId()).getRowCollectionList();
		String indPrefix = "ind_";

		int n = 0;
		for(BiCellCollection c : v) {
			ppj.addDetailRecord();
			String s = "";
			n++;
			/*
			if(c.testCell(indPrefix+"irg") != null) {
				if(c.testCell(indPrefix+"irg").getInt() > 0) {
					if(c.testCell("st_iname") != null)  {
						s += c.getCellString("st_iname");
					}
					if(Erpv4Config.getString(br.getSessionHelper(), "CustomSmartac") != null) {
						s += " ";
						s += "["+c.getCellString("st_icode")+"]";
					}
				}
			}
			if(c.testCell(indPrefix+"desp") != null) {
					s += c.getCellString(indPrefix+"desc");
			}
			ppj.addDetailRecordField("description", s, -200, 0);
			ppj.addDetailRecordField("description", c.getCellString("mt_tpname"), 90, 0);
			ppj.setBold(false);
			if(c.getCell(indPrefix+"qty").getDouble() != 0) {
				ppj.addDetailRecordField("quantity", c.getCell(indPrefix+"qty").getString());
			}
			*/
			ppj.addDetailRecordField("itemcode", c.getCell("st_icode").getString());
			ppj.addDetailRecordField("description", c.getCell(indPrefix+"desc").getString());
			ppj.addDetailRecordField("quantity", c.getCell(indPrefix+"qty").getString());
			ppj.addDetailRecordField("unitprice", c.getCell(indPrefix+"uprice").getString(), 0, 0);
			ppj.addDetailRecordField("amount", c.getCell(indPrefix+"amount").getString(), 0, 0);
		}
		ppj.addBottomField("val_amount", br.getCellString("inv_total") ,0,0,0,0);
		/*
		int ofs = 0;
			String remark = null; 
			remark = br.getCellString("inv_term");
		if(remark == null) remark = br.getCell("inv_quodeli").getString(); else remark += "\r" + br.getCell("inv_quodeli").getString();
		if(!br.getCellString("inv_remark").equals("")) {
			remark += "\r"+ br.getCellString("inv_remark");
		}
		if(!br.getCellString("invh_term").equals("")) {
			remark += "\r"+ br.getCellString("inv_remark");
		}
			ppj.addBottomField("val_remark",remark,160,255,20,0);
			*/
	}

	@Override
	public PrtdocJson getPrintDocJson() throws Exception {
		// TODO Auto-generated method stub
		return ppj;
	}

}
