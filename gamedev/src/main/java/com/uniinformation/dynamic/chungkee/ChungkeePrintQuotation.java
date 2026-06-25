package com.uniinformation.dynamic.chungkee;

import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.prtdoc.PrtdocJson;

public class ChungkeePrintQuotation extends PrtdocClass  {
	BiResultQuotation br;
    PrtdocJson ppj;
    
	public ChungkeePrintQuotation (BiResultQuotation p_br) throws Exception {
    	String dnDoc = "GENINV03";
    	ppj = PrtdocJson.newPrtdocJson(	
    				"001",
    				"A4P",
    			    dnDoc,
    			    "erpv4_printDocument"
    	);
		br = p_br;
    	ppj.setTrailerAtLastPageOnly(true);
    	ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
    	ppj.addHeaderField("doctitle","Quotation");
  		ppj.addHeaderImage("logo", Erpv4Config.getString(br.getSessionHelper(), "QuoBgImage"),0,0,0,800);
	}

	@Override
	public void print() throws Exception {
		// TODO Auto-generated method stub
    	String ds = DateUtil.toDateString( br.getCell("inv_date").getDate(),"yyyy/mm/dd");
    	ppj.addHeaderField("dfvalue", ds,-120,290);
    	ppj.addHeaderField("cicontent",br.getCellString("vd_vname"), -45, 150);
   		String addr = br.getCellString("vd_addr0").trim()+br.getCellString("vd_addr1").trim() + " " +  br.getCellString("vd_addr2").trim() + " " + br.getCellString("vd_addr3").trim();
    	ppj.addHeaderField("cicontent",addr,-45, 170);
    	ppj.addHeaderField("cicontent",br.getCellString("inv_contact"),-45,190);
    	ppj.addHeaderField("cicontent",br.getCellString("inv_tel"),-45,210);
    	
    	
   		Vector<BiCellCollection> v = null;
		v = br.getSubLink(br.get_subLinkId()).getRowCollectionList();
		String indPrefix = "ind_";

		int n = 0;
		for(BiCellCollection c : v) {
			ppj.addDetailRecord();
			String s = "";
			boolean isSubitem = false;
			if(c.testCell(indPrefix+"subitem") != null) {
				if(!c.testCell(indPrefix+"subitem").getBoolean()) {
					if(c.testCell("stmcm_name") != null)  {
						ppj.setBold(true);
						s += c.getCellString("stmcm_name");
						//ppj.addDetailRecordField("amount", c.getCell(indPrefix+"setamount").getString());
					}
				} else {
					isSubitem = true;
				}
			}
			if(!isSubitem) {
					n++;
					//ppj.addDetailRecordField("seq", ""+n);
			}
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
//			ppj.addDetailRecordField("serialno",c.getCellString(indPrefix+"ref4"));
//			ppj.addDetailRecordField("orderno",c.getCellString("inv_invno"));
			if(!isSubitem) {
				//ppj.addDetailRecordField("amount",c.getCellString(indPrefix+"amount"));
			}
			//ppj.addDetailRecordField("brand", c.getCellString("stbd_name"));
			String oicode = c.getCellString("st_oicode");
			String modelno = c.getCellString("st_modelno");
			if(oicode != null && !oicode.equals("")) {
				//ppj.addDetailRecordField("itemcode", oicode);
			} else {
				if(modelno != null && !modelno.equals("")) {
					//ppj.addDetailRecordField("itemcode", modelno);
				}
			}
			ppj.setBold(false);
//			ppj.setUnderLine(false);
			if(c.getCell(indPrefix+"qty").getDouble() != 0) {
				//ppj.addDetailRecordField("quantity", c.getCell(indPrefix+"qty").getString());
			}
				ppj.addDetailRecordField("amount", c.getCell(indPrefix+"uprice").getString(), -100, 0);
		}
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
	}

	@Override
	public PrtdocJson getPrintDocJson() throws Exception {
		// TODO Auto-generated method stub
		return ppj;
	}

}
