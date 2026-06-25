package com.uniinformation.dynamic.afs;

import java.text.DecimalFormat;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultQuoDet;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.cell.Cell;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.VectorUtil;

public class AfsPrtdocPrintPartsQuotation extends PrtdocClass {
	BiResultQuotation br;
    PrtdocJson ppj ;
	public AfsPrtdocPrintPartsQuotation (BiResultQuotation p_br) throws Exception {
    	String dnDoc = "NAFSPQUO";
    	ppj = PrtdocJson.newPrtdocJson(	
    				"AFS",
    				"A4P",
    			    dnDoc,
    			    "erpv4_printDocument"
    	);
		br = p_br;
    	ppj.setTrailerAtLastPageOnly(true);
    	ppj.addPageNo("pagestr", "%s of %s",0, 60, 0);
    	ppj.addHeaderField("doctitle","Quotation");
    	if(Erpv4Config.getDefaultLogo(br.getSessionHelper()) != null) {
    		ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(br.getSessionHelper()) ,0,0,0,440);
    	}
	}

	public PrtdocJson getPrtdocJson() {
		return(ppj);
	}
	@Override
	public void print() throws Exception {
		// TODO Auto-generated method stub
		PrtdocJson.addMultiHeaderField(ppj, "ciname", 0, 0, 20, 0, 
				new VectorUtil()
				.addElement("客户明稱:")
				.addElement("電 話:")
				.addElement("傳真:")
//				.addElement("手機:")
				.addElement("聯系人:")
				.addElement("電 郵:")
//				.addElement("送貨地址:")
				.toVector()
				);
		PrtdocJson.addMultiHeaderField(ppj, "cicontent", 0, 0, 20, 0, 
				new VectorUtil()
				.addElement(br.getCellString("vd_vname"))
				.addElement(br.getCellString("inv_tel"))
				.addElement(br.getCellString("vd_fax"))
				.addElement(br.getCellString("inv_contact"))
				.addElement(br.getCellString("vd_email"))
				.toVector()
				);
		PrtdocJson.addMultiHeaderField(ppj, "dflabel", 0, 0, 20, 0, 
				new VectorUtil()
				.addElement("日期:")
				.addElement("報價單編號:")
				.addElement("報價人:")
				.addElement("電話:")
				.addElement("電郵:")
				.toVector()
				);
		PrtdocJson.addMultiHeaderField(ppj, "dfvalue", 0, 0, 20, 0, 
				new VectorUtil()
				.addElement(DateUtil.toDateString( br.getCell("inv_date").getDate(),"yyyy/mm/dd"))
				.addElement(br.getCellString("inv_quonum"))
				.addElement(br.getCellString("sm_name"))
				.addElement(br.getCellString("sm_mobile"))
				.addElement(br.getCellString("sm_email"))
				.toVector()
				);

    	ppj.addDetailHeaderField("hdr_seq","Item");
    	ppj.addDetailHeaderField("hdr_figindex","Fig/Index");
    	ppj.addDetailHeaderField("hdr_itemcode","Part No");
    	ppj.addDetailHeaderField("hdr_description","Description");
    	ppj.addDetailHeaderField("hdr_stock","Stock");
    	ppj.addDetailHeaderField("hdr_qty","Qty");
    	ppj.addDetailHeaderField("hdr_price","Price");
    	ppj.addDetailHeaderField("hdr_amount","Amount("+br.getCellString("inv_cid")+")");

    	Vector<BiCellCollection> v = null;
    	v = br.getSubLink(br.get_subLinkId()).getRowCollectionList();
    	String indPrefix = "ind_";

    		int n = 0;
    		double grossAmt = 0.0;
   			DecimalFormat fmt = new DecimalFormat("##,###,###,##0.00");
   			String mcModel = null;
    		for(BiCellCollection c : v) {
    			int pdsrg = c.getCellInt(indPrefix+"pdsrg");
    			if(BiResultQuoDet.getDeltaType(br.getSessionHelper(),pdsrg) == BiResultQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION) {
    				ppj.useDetailGroup("detsubhdr");
    				ppj.addDetailRecord();
    				ppj.addDetailRecordField("hdrdesp", c.getCellString(indPrefix+"desc"));
    				mcModel=null;
    				ppj.useDetailGroup("");
    				continue;
    			}
    			String md = c.getCellString(indPrefix+"ref1");
    			if(!md.equals(mcModel)) {
    				if(!md.isEmpty()) {
    					mcModel = md;
    					ppj.useDetailGroup("detsubhdr");
    					ppj.addDetailRecord();
    					ppj.addDetailRecordField("hdrtitle", "Model:"+md);
    					ppj.useDetailGroup("");
    				} else {
    					mcModel = null;
    				}
    			}
    			ppj.addDetailRecord();
    			String s = "";
    			boolean isSubitem = false;
    			n++;
    			ppj.addDetailRecordField("seq", ""+n);
    			if(c.testCell(indPrefix+"irg") != null) {
    				if(c.testCell(indPrefix+"irg").getInt() > 0) {
    					String sextra = null;
    					Cell cc = null;
    					cc = c.testCell("stbd_name");
    					if(cc != null && !cc.getString().trim().equals("")) {
    						if(sextra == null) sextra = cc.getString().trim(); else sextra += " " + cc.getString().trim();
    					}
    					/*
    					cc = c.testCell(indPrefix+"ref1");
    					if(cc != null && !cc.getString().trim().equals("")) {
    						if(sextra == null) sextra = cc.getString().trim(); else sextra += " " + cc.getString().trim();
    					}
    					*/
    					if(sextra != null && !sextra.trim().equals("")) {
    						s += sextra + "\r";
    					}
    					String ss = c.getCellString(indPrefix+"ref2");
    					ppj.addDetailRecordField("figindex", ss);
    					String oicode = c.getCellString("st_oicode");
    					ppj.addDetailRecordField("itemcode", oicode);
    					if(c.testCell("st_iname") != null)  {
    						s += c.getCellString("st_iname");
    					}
    				}
    			}
    			if(c.testCell(indPrefix+"desp") != null) {
   					s += c.getCellString(indPrefix+"desc");
    			}
    			ppj.addDetailRecordField("description", s);
    			if(c.getCell(indPrefix+"qty").getDouble() != 0) {
    			ppj.addDetailRecordField("qty", c.getCell(indPrefix+"qty").getString());
    			}
    			if(c.testCell(indPrefix+"instock") != null) {
   					boolean instock = c.getCell(indPrefix+"instock").getBoolean();
   					if(instock) {
//   						ppj.addDetailRecordField("stock", "yes");
//   							ppj.addDetailEndImage("stock",String p_image,p_x,int p_y,int p_h, int p_w) throws JSONException {
   							ppj.addDetailRecordImage("stock","icons/check-mark_128.png",10,0,18,18);
   					}
    			}
    			double amt = c.getCellDouble(indPrefix+"uprice") * c.getCellDouble(indPrefix+"qty");
    			grossAmt += amt;
    			ppj.addDetailRecordField("price",fmt.format( c.getCellDouble(indPrefix+"uprice") ));
    			ppj.addDetailRecordField("amount",fmt.format(amt));
    			ppj.setBold(false);
//    			ppj.setUnderLine(false);
    		}
    		double discount = 0;
    		discount = grossAmt - br.getCellDouble("inv_grtotal");
    		discount += br.getCellDouble("inv_discount");
    		double tradein = br.getCellDouble("inv_tradein");
    		double delichg = br.getCellDouble("inv_delichg");
    		double nettotal = br.getCellDouble("inv_total");
    		if(discount == 0.0 && tradein == 0.0 && tradein == 0 && delichg == 0) {
    			ppj.addDetailEndField("ddesp","Total:",0,0);
    			ppj.addDetailEndField("dtotal",fmt.format(nettotal),0,0);

    		} else {
    			int ofs = 0;
    			ppj.addDetailEndField("ddesp","Total:");
    			ppj.addDetailEndField("dtotal",fmt.format(grossAmt),0,ofs);
    			ofs += 20;
    			if(discount != 0) {
    				ppj.addDetailEndField("ddesp","Less Discount:",0,ofs);
    				ppj.addDetailEndField("dtotal",fmt.format(discount),0,ofs);
    				ofs += 20;
    			}
    			if(tradein != 0) {
    				ppj.addDetailEndField("ddesp","Less Tradein:",0,ofs);
    				ppj.addDetailEndField("dtotal",fmt.format(tradein),0,ofs);
    				ofs += 20;
    			}
    			if(delichg != 0) {
    				ppj.addDetailEndField("ddesp","Delivery Charge :",0,ofs);
    				ppj.addDetailEndField("dtotal",fmt.format(delichg),0,ofs);
    				ofs += 20;
    			}
    			ppj.addDetailEndField("ddesp","Net Total:",0,ofs);
    			ppj.addDetailEndField("dtotal",fmt.format(nettotal),0,ofs);
    		}
    		int ofs = 0;
   			String remark = null; 
   			String ss;
   			ss = br.getCellString("inv_term");
   			if(!ss.isEmpty()) {
   				if(remark == null) remark = ss; else remark += "\r"+ss;
   			}
   			ss = br.getCellString("inv_quodeli");
   			if(!ss.isEmpty()) {
   				if(remark == null) remark = ss; else remark += "\r"+ss;
   			}
   			ss = br.getCellString("inv_remark");
   			if(!ss.isEmpty()) {
   				if(remark == null) remark = ss; else remark += "\r"+ss;
   			}
			if(remark != null && !remark.isEmpty()) {
				ppj.addBottomField("val_remark",remark,0,0);
			}
	}
	@Override
	public PrtdocJson getPrintDocJson() throws Exception {
		return ppj;
	}

}
