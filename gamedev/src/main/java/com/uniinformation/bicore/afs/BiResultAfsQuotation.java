package com.uniinformation.bicore.afs;

import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.bicore.erpv4.Erpv4StockAttribute;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.PrtdocPrintInvoice;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.DynamicClassLoader;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;




import com.uniinformation.webcore.SessionHelper;

//import org.zkoss.json.parser.JSONParser;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BiResultAfsQuotation extends BiResultQuotation {
	CellValueAction updateInvoice;
	public BiResultAfsQuotation(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr,SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr,p_sh);
		UniLog.log("BiResultAfsQuotation");
		if(getView().getName().equals("afs.AfsQuoParts")) {
				quotationType = "AQP";
		} else 
		if(getView().getName().equals("afs.AfsQuoPartsAll")) {
				quotationType = "AQP";
		} else 
		if(getView().getName().equals("afs.AfsQuotationMc")) {
				quotationType = "AQM";
		} else 
		if(getView().getName().equals("afs.AfsQuoMc")) {
				quotationType = "AQM";
		} else 
		if(getView().getName().equals("afs.AfsQuoMcAll")) {
				quotationType = "AQM";
		} else 
		if(getView().getName().equals("AfsQuotation")) {
				quotationType = null;
		} else
		if(getView().getName().equals("erpv4.QuoApprove")) {
				quotationType = null;
		} 
		else throw new CellException("unspported sales order view");
		subLinkId = "AfsQuoDet";
		hideComboDetailAmount = true;
	}
	
	@Override 
	public HashSet<BiTable>addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash) {
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
		if(quotationType != null) p_where.andUniop("inv_quonum", "like", quotationType+"%");
		return(ht);
	}
	
	@Override
	public String getNewOrderNumber(java.util.Date p_date) throws Exception {
		java.util.Date d = p_date;
		String s = "";
		String ds = DateUtil.toDateString(d, "yymmdd");
		int nextidx = 1;
		TableRec tr = su.getQueryResult("select inv_invno from quotation where inv_invno matches '" + quotationType + ds + "*' order by inv_invno desc",null);
		if(tr.getRecordCount() > 0) {
			tr.setRecPointer(0);
			s = tr.getField("inv_invno").toString();
			String ss = StringUtil.strpart(s, 9, -1);
			nextidx = Integer.parseInt(ss) + 1;
		}
		s = String.format("%s%s%03d",quotationType,ds, nextidx);
		return(s);
	}
	

//	@Override
//	protected void createColumnCells(BiCellCollection p_col)
//	{
//		super.createColumnCells(p_col);
//		if(updateInvoice == null) {
//		updateInvoice = new CellValueAction() {
//
//		@Override
//		public void cellAction_onchange(Cell p_value) throws CellException {
//			// TODO Auto-generated method stub
//			BiResult sr = getSubLink("erpv4.QuoInvoice");
//			if(sr != null) {
//				Vector<CellCollection> v = sr.getRowCollectionList();
//				for(CellCollection col : v) {
//					col.getCell("invh_grtotal").sync(getCell("inv_grtotal").getDouble());
//				}
//			}
//			
//		}
//
//		@Override
//		public void cellAction_onfree() throws CellException {
//			// TODO Auto-generated method stub
//			
//		}
//		
//		};
//		}
//		p_col.getCell("inv_grtotal").addAction(updateInvoice);
//	}

	ReturnMsg printQuotationParts(OutputStream os,JSONObject option) { 
		try {
		PrtdocClass jpi = null;
		Class[]	paramTypes = new Class[]{BiResultQuotation.class};
		jpi = (PrtdocClass) DynamicClassLoader.newInstance("com.uniinformation.dynamic.afs.AfsPrtdocPrintPartsQuotation", paramTypes,this);
		jpi.print();
		return(jpi.getPrintDocJson().toPdfStream(os, getSessionHelper()));	
		} catch (Exception ex) {
			UniLog.log(ex); 
			return(new ReturnMsg(false,ex.toString()));
		}
	}
	ReturnMsg printQuotationPartsYY(OutputStream os,JSONObject option) { 
//		BiResultQuotation ppr = null;
//		ppr = (BiResultQuotation) getParent();
//		if(ppr == null) return(new ReturnMsg(false,"Error : no parent quotation record"));
		try {
    		String paperType = Erpv4Config.getString(getSessionHelper(),"InvPaperType");
    		String dnDoc = "NAFSPQUO";
    		PrtdocJson ppj = PrtdocJson.newPrtdocJson(	
    				getCellString("stm_cocode"),
    				paperType,
    			    dnDoc,
    			    "erpv4_printDocument"
    				) ;
    		ppj.setTrailerAtLastPageOnly(true);
    		ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
    		ppj.addHeaderField("doctitle","Quotation");
    		if(Erpv4Config.isMultiCompany(getSessionHelper())) {
    			String logo = Erpv4Config.getCoLogo(getSessionHelper(), getCellString("stm_cocode"));
    			if(logo != null && !logo.equals("")) {
    				ppj.addHeaderImage("logo", logo ,0,0,0,440);
    			}
    		} else {
    			if(Erpv4Config.getDefaultLogo(getSessionHelper()) != null) {
    				ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(getSessionHelper()) ,0,0,0,440);
    			}
    		}
    		ppj.addHeaderField("ciname","公司明稱:");
    		ppj.addHeaderField("ciname","電 話:",0,20);
    		ppj.addHeaderField("ciname","傳 真:",0,40);
    		ppj.addHeaderField("ciname","報價人:",0,60);
    		ppj.addHeaderField("ciname","電 郵:",0,80);
    		
    		ppj.addHeaderField("cicontent",getCellString("vd_vname"));
   			String addr = getCellString("vd_addr0").trim()+getCellString("vd_addr1").trim() + " " +  getCellString("vd_addr2").trim() + " " + getCellString("vd_addr3").trim();
    		ppj.addHeaderField("cicontent",addr,0,20);
    		ppj.addHeaderField("cicontent",getCellString("inv_contact"),0,40);
    		ppj.addHeaderField("cicontent",getCellString("inv_tel"),0,80);
    		ppj.addHeaderField("cicontent",getCellString("inv_fax"),0,100);

    		
    		ppj.addHeaderField("dflabel","Refs:",0,0);
    		ppj.addHeaderField("dfvalue",getCellString("inv_quonum"),0,0);
    		ppj.addHeaderField("dflabel","Date:",0,30);
//    		ppj.addHeaderField("dfvalue", DateUtil.toDateString( getCell("stm_date").getDate(),"yyyy/mm/dd") ,0,30);
    		String ds = DateUtil.toDateString( getCell("inv_date").getDate(),"yyyy/mm/dd");
    		ppj.addHeaderField("dfvalue", " "+ds ,-5,30);
    		ppj.addHeaderField("dflabel","Page",0,60);
 
    		ppj.addDetailHeaderField("hdr_seq","Item");
    		ppj.addDetailHeaderField("hdr_figindex","Fig/Index");
    		ppj.addDetailHeaderField("hdr_itemcode","Part No");
    		ppj.addDetailHeaderField("hdr_description","Description");
    		ppj.addDetailHeaderField("hdr_stock","Stock");
    		ppj.addDetailHeaderField("hdr_qty","Qty");
    		ppj.addDetailHeaderField("hdr_price","Price");
    		ppj.addDetailHeaderField("hdr_amount","Amount("+getCellString("inv_cid")+")");

    		Vector<BiCellCollection> v = null;
    		v = getSubLink(subLinkId).getRowCollectionList();
    		String indPrefix = "ind_";

    		int n = 0;
    		double grossAmt = 0.0;
   			DecimalFormat fmt = new DecimalFormat("##,###,###,##0.00");
    		for(BiCellCollection c : v) {
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
    					cc = c.testCell(indPrefix+"ref1");
    					if(cc != null && !cc.getString().trim().equals("")) {
    						if(sextra == null) sextra = cc.getString().trim(); else sextra += " " + cc.getString().trim();
    					}
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
    		discount = grossAmt - getCellDouble("inv_grtotal");
    		discount += getCellDouble("inv_discount");
    		double tradein = getCellDouble("inv_tradein");
    		double delichg = getCellDouble("inv_delichg");
    		double nettotal = getCellDouble("inv_total");
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
   			remark = getCellString("inv_term");
			if(remark == null) remark = getCell("inv_quodeli").getString(); else remark += "\r" + getCell("inv_quodeli").getString();
			if(!getCellString("inv_remark").equals("")) {
				remark += "\r"+ getCellString("inv_remark");
			}
			if(!getCellString("invh_term").equals("")) {
				remark += "\r"+ getCellString("inv_remark");
			}
   			ppj.addBottomField("val_remark",remark,0,0,15,0);
    		return(ppj.toPdfStream(os, getSessionHelper()));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Fail Reason Unknown"));
		}	
	}
	
	@Override
	public ReturnMsg printQuotation(OutputStream os,JSONObject option) { 
    	if(quotationType.equals("AQP")) return(printQuotationParts(os,option));
		BiResultQuotation ppr = null;
		ppr = (BiResultQuotation) getParent();
//		if(ppr == null) return(new ReturnMsg(false,"Error : no parent quotation record"));
		try {
    		String paperType = Erpv4Config.getString(getSessionHelper(),"InvPaperType");
//    		String dnDoc = Erpv4Config.getString(getSessionHelper(),"dnDocCode");
    		String dnDoc = null;
    		
    		if(quotationType.equals("AQP")) dnDoc = "NAFSPQUO"; else dnDoc = "NAFSMQUO";
    		PrtdocJson ppj = PrtdocJson.newPrtdocJson(	
    				getCellString("stm_cocode"),
    				paperType,
    			    dnDoc,
    			    "erpv4_printDocument"
    				) ;
    		ppj.setTrailerAtLastPageOnly(true);
    		ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
    		ppj.addHeaderField("doctitle","Quotation");
    		if(Erpv4Config.isMultiCompany(getSessionHelper())) {
    			String logo = Erpv4Config.getCoLogo(getSessionHelper(), getCellString("stm_cocode"));
    			if(logo != null && !logo.equals("")) {
    				ppj.addHeaderImage("logo", logo ,0,0,0,440);
    			}
    		} else {
    			if(Erpv4Config.getDefaultLogo(getSessionHelper()) != null) {
    				ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(getSessionHelper()) ,0,0,0,440);
    			}
    		}
    		ppj.addHeaderField("ciname","Company Name:");
    		ppj.addHeaderField("ciname","Address:",0,20);
    		ppj.addHeaderField("ciname","Contact Person:",0,40);
    		ppj.addHeaderField("ciname","Email:",0,60);
    		ppj.addHeaderField("ciname","Tel:",0,80);
    		ppj.addHeaderField("ciname","Mob:",0,100);
    		
    		/*
    		ppj.addHeaderField("ciname","Courtesy of ",0,140);
    		ppj.addHeaderField("ciname","Model No.",0,170);
    		*/

    		ppj.addHeaderField("cicontent",getCellString("vd_vname"));
   			String addr = getCellString("vd_addr0").trim()+getCellString("vd_addr1").trim() + " " +  getCellString("vd_addr2").trim() + " " + getCellString("vd_addr3").trim();
    		ppj.addHeaderField("cicontent",addr,0,20);
    		ppj.addHeaderField("cicontent",getCellString("inv_contact"),0,40);
    		ppj.addHeaderField("cicontent",getCellString("inv_tel"),0,80);
    		ppj.addHeaderField("cicontent",getCellString("inv_fax"),0,100);

    		
    		ppj.addHeaderField("dflabel","Refs:",0,0);
    		ppj.addHeaderField("dfvalue",getCellString("inv_quonum"),0,0);
    		ppj.addHeaderField("dflabel","Date:",0,30);
//    		ppj.addHeaderField("dfvalue", DateUtil.toDateString( getCell("stm_date").getDate(),"yyyy/mm/dd") ,0,30);
    		String ds = DateUtil.toDateString( getCell("inv_date").getDate(),"yyyy/mm/dd");
    		ppj.addHeaderField("dfvalue", " "+ds ,-5,30);
    		ppj.addHeaderField("dflabel","Page",0,60);
 
    		ppj.addDetailHeaderField("hdr_seq","Item");
    		ppj.addDetailHeaderField("hdr_orderno","Brand");
    		if(quotationType.equals("AQP")) {
    			ppj.addDetailHeaderField("hdr_itemcode","Part No");
    		} else {
    			ppj.addDetailHeaderField("hdr_itemcode","Model");
    		}
    		ppj.addDetailHeaderField("hdr_description","Description");
    		ppj.addDetailHeaderField("hdr_qty","Qty");
    		ppj.addDetailHeaderField("hdr_serialno","Amount("+getCellString("inv_cid")+")");

    		/*
    		ppj.addDetailHeaderField("hdr_seq","Item",0,20);
    		ppj.addDetailHeaderField("hdr_orderno","Contract No.",0,20);
    		ppj.addDetailHeaderField("hdr_itemcode","Model",0,20);
    		ppj.addDetailHeaderField("hdr_description","Description",0,20);
    		ppj.addDetailHeaderField("hdr_qty","QTY",0,20);
    		ppj.addDetailHeaderField("hdr_serialno","Serial No.",0,20);
    		*/

    		Vector<BiCellCollection> v = null;
    		v = getSubLink(subLinkId).getRowCollectionList();
    		String indPrefix = "ind_";

    		int n = 0;
    		double grossAmt = 0.0;
   			DecimalFormat fmt = new DecimalFormat("##,###,###,##0.00");
    		for(BiCellCollection c : v) {
    			ppj.addDetailRecord();
    			String s = "";
    			boolean isSubitem = false;
    			if(quotationType.equals("AQP")) {
    				Cell cc = c.testCell(indPrefix+"ref1");
    				if(cc != null && !cc.getString().trim().equals("")) {
    					s += cc.getString().trim() + "\r";
    				}
    			}
    			if(!quotationType.equals("AQP")) {
    			if(c.testCell(indPrefix+"subitem") != null) {
    				if(!c.testCell(indPrefix+"subitem").getBoolean()) {
    					if(c.testCell("stmcm_name") != null)  {
    						ppj.setBold(true);
//    						ppj.setUnderLine(true);
    						s += c.getCellString("stmcm_name");
    						ppj.addDetailRecordField("amount", c.getCell(indPrefix+"setamount").getString());
    					}
    				} else {
    					isSubitem = true;
    				}
    			}
    			}
    			if(!isSubitem) {
    					n++;
    					ppj.addDetailRecordField("seq", ""+n);
    			}
    			if(c.testCell(indPrefix+"irg") != null) {
    				if(c.testCell(indPrefix+"irg").getInt() > 0) {
    					if(c.testCell("st_iname") != null)  {
    						s += c.getCellString("st_iname");
    					}
    					if(Erpv4Config.getString(getSessionHelper(), "CustomSmartac") != null) {
							s += " ";
							s += "["+c.getCellString("st_icode")+"]";
    					}
    				}
    			}
    			if(c.testCell(indPrefix+"desp") != null) {
   					s += c.getCellString(indPrefix+"desc");
    			}
    			ppj.addDetailRecordField("description", s);
//    			ppj.addDetailRecordField("serialno",c.getCellString(indPrefix+"ref4"));
//    			ppj.addDetailRecordField("orderno",c.getCellString("inv_invno"));
    			if(quotationType.equals("AQP")) {
    				double amt = c.getCellDouble(indPrefix+"uprice") * c.getCellDouble(indPrefix+"qty");
    				grossAmt += amt;
    				ppj.addDetailRecordField("amount",fmt.format(amt));
    			} else {
    				if(!isSubitem) {
    					ppj.addDetailRecordField("amount",c.getCellString(indPrefix+"amount"));
    				}
    			}
    			ppj.addDetailRecordField("brand", c.getCellString("stbd_name"));
    			if(quotationType.equals("AQP")) {
    				String oicode = c.getCellString("st_oicode");
    				String ss = c.getCellString(indPrefix+"ref2");
    				if(!ss.equals("")) {
    					oicode += "\r"+ss;
    				}
    				ppj.addDetailRecordField("itemcode", oicode);
    			} else {
    				String oicode = c.getCellString("st_oicode");
    				String modelno = c.getCellString("st_modelno");
    				if(oicode != null && !oicode.equals("")) {
    					ppj.addDetailRecordField("itemcode", oicode);
    				} else {
    					if(modelno != null && !modelno.equals("")) {
    						ppj.addDetailRecordField("itemcode", modelno);
    					}
    				}
    			}
    			ppj.setBold(false);
//    			ppj.setUnderLine(false);
    			if(c.getCell(indPrefix+"qty").getDouble() != 0) {
    			ppj.addDetailRecordField("quantity", c.getCell(indPrefix+"qty").getString());
    			}
    		}
    		double discount = 0;
    		if(quotationType.equals("AQP")) {
    			discount = grossAmt - getCellDouble("inv_grtotal");
    		} else {
    			grossAmt = getCellDouble("inv_grtotal");
    		}
    		discount += getCellDouble("inv_discount");
    		double tradein = getCellDouble("inv_tradein");
    		double delichg = getCellDouble("inv_delichg");
    		double nettotal = getCellDouble("inv_total");
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
   			remark = getCellString("inv_term");
			if(remark == null) remark = getCell("inv_quodeli").getString(); else remark += "\r" + getCell("inv_quodeli").getString();
			if(!getCellString("inv_remark").equals("")) {
				remark += "\r"+ getCellString("inv_remark");
			}
			if(!getCellString("invh_term").equals("")) {
				remark += "\r"+ getCellString("inv_remark");
			}
   			ppj.addBottomField("val_remark",remark,0,0,15,0);
    		return(ppj.toPdfStream(os, getSessionHelper()));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Fail Reason Unknown"));
		}	
	}

	@Override
	protected void createColumnCells(BiCellCollection p_col)
	{
		super.createColumnCells(p_col);
		if(p_col.testCell("vd_priceclass") != null) {
			p_col.getCell("vd_priceclass").setItemPropertyInterface(Erpv4StockAttribute.getPriceTypeList(sh));
		}
	}
}
