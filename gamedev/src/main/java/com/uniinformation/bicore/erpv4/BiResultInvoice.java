package com.uniinformation.bicore.erpv4;

import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.PrtdocPrintInvoice;
//import com.uniinformation.jxapp.afs.WcJsonPrtdocInvoice;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.prtdoc.PrtdocPerfJson;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Strval;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.DynamicClassLoader;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultInvoice extends BiResultInvoiceBase {
	
	public BiResultInvoice(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh,p_allowLookupItemList);
		// TODO Auto-generated constructor stub
		if(getIndLinkName() != null) {
				addSublinkAction(getIndLinkName(),indUpdateGrTotal);
		}
	}
	

	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		if(col.getCell("invh_quostatus").equals("Confirmed")) {
			RpcClient rpc = getSelectUtil().getRpcClient();
			rpc.callSegment("setCocodeBaseccy",
					new VectorUtil()
//						.addElement(Erpv4Config.getCoCode(getSessionHelper()))
//						.addElement(Erpv4Config.getBaseCcy(getSessionHelper()))
						.addElement(col.getCellString("invh_cocode"))
						.addElement(Erpv4Config.getBaseCcy(getSessionHelper(),col.getCellString("invh_cocode")))
						.toVector()
					);
			Value v = rpc.callSegment("invoice_removesih",
					new VectorUtil()
						.addElement(col.getCell("invh_invno").getString())
						.toVector()
					);
			
			if(v != null && v.toInt() != 0) {
				return( new ReturnMsg(false,"Remove Invoice Failed " + (v == null ? "Unknown" : v.toString()),true));
			}
		}
		return(super.biBeforeDeleteCurrent(col));
	}
	
	
	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg msg = super.biAfterAddUpdateCurrent(col,isUpdate);
		if(msg == null || !msg.getStatus()) return(msg);
		RpcClient rpc = getSelectUtil().getRpcClient();
		rpc.callSegment("setCocodeBaseccy",
					new VectorUtil()
//						.addElement(Erpv4Config.getCoCode(getSessionHelper()))
//						.addElement(Erpv4Config.getBaseCcy(getSessionHelper()))
						.addElement(col.getCellString("invh_cocode"))
						.addElement(Erpv4Config.getBaseCcy(getSessionHelper(),col.getCellString("invh_cocode")))
						.toVector()
					);
		Value v = rpc.callSegment("erpv4InvoiceUpdate",
					new VectorUtil()
						.addElement(col.getCell("invh_invno").getString())
						.addElement(col.getCell("invh_quostatus").getString())
						.addElement("")
						.toVector()
					);
		if(v == null || !v.toString().startsWith("OK")) {
			return(
				new ReturnMsg(false,"Update Invoice Failed " + (v == null ? "Unknown" : v.toString()),true)
			);
		}
		return(msg);
	}

//	@Override
//	protected void createColumnCells(BiCellCollection p_col)
//	{
//		super.createColumnCells(p_col);
//		p_col.getCell("invh_payratio").addAction(updateInvoice);
//		p_col.getCell("invh_qnettotal").addAction(updateInvoice);
//		/*
//		p_col.getCell("invh_grtotal").addAction(updateInvoice);
//		*/
//	}
	protected String makeInvoiceItemDescription(String indPrefix,CellCollection c,PrtdocJson ppj) {
    			String s = "";
    			if(c.testCell(indPrefix+"irg") != null) {
    				if(c.testCell(indPrefix+"irg").getInt() > 0) {
    					if(c.testCell("st_iname") != null)  {
    						s += c.getCellString("st_iname");
    					}
    				}
    			}
    			if(c.testCell(indPrefix+"desc") != null) {
   					s += c.getCellString(indPrefix+"desc");
    			}
    			return(s);
	}

	public ReturnMsg printInvoice(OutputStream os,JSONObject p_option) {
 		String printInvoiceClass = Erpv4Config.getString(getSessionHelper(), "PrtdocPrintInvoiceClass");
		if(printInvoiceClass == null || printInvoiceClass.isEmpty()) {
			printInvoiceClass = "com.uniinformation.erpv4.PrtdocPrintInvoice";
			return(PrintOneDocument(
  					printInvoiceClass,
  					os, getCellString("invh_cocode"),
   					Erpv4Config.getString(getSessionHelper(),"InvDocCode"),
   					Erpv4Config.getString(getSessionHelper(),"InvPaperType"),
   					p_option
   			));
		} else {
			try {
				PrtdocClass jpi = null;
				Class[]	paramTypes = new Class[]{BiResultErpv4.class};
				jpi = (PrtdocClass) DynamicClassLoader.newInstance(printInvoiceClass, paramTypes, this);
				jpi.print();
				return jpi.getPrintDocJson().toPdfStream(os, getSessionHelper());	
			}
			catch (Exception ex) {
				UniLog.log(ex); 
				return new ReturnMsg(false, ex);
			}
		}
	}
	
	
//	public ReturnMsg printInvoiceYY (OutputStream os) {
//		try {
//    		String paperType = Erpv4Config.getString(getSessionHelper(),"InvPaperType");
//    		String invDoc = Erpv4Config.getString(getSessionHelper(),"InvDocCode");
//    		if(invDoc == null) invDoc = "GENINV01";
//    		PrtdocJson ppj = PrtdocJson.newPrtdocJson(	
//    				getCellString("invh_cocode"),
//    				paperType,
//    			    invDoc,
//    			    "erpv4_printDocument"
//    				) ;
//    		PrtdocPrintInvoice jpi = null;
//    		String ss = Erpv4Config.getString(getSessionHelper(), "PrtdocPrintInvoiceClass");
//    		if(ss != null && !ss.isEmpty()) {
//    			
//				Class[]	paramTypes = new Class[]{BiResultErpv4.class,PrtdocJson.class,JSONObject.class};
//    			Class prtdocClass = Class.forName(ss);
//    			Constructor constructor = prtdocClass.getConstructor(paramTypes);
//    			if(constructor == null) {
//    				return(new ReturnMsg(false,"Fail PirintInvoiceClass "+ss + " not found"));
//    			}	
//    			jpi = (PrtdocPrintInvoice) constructor.newInstance(this,ppj,null);
//    		} else jpi = new PrtdocPrintInvoice(this,ppj,null);
// 			jpi.print();
//    		return(ppj.toPdfStream(os, getSessionHelper()));
//		} catch (Exception ex) {
//			UniLog.log(ex);
//			return(new ReturnMsg(false,"Fail Reason Unknown"));
//		}	
//	}
	public ReturnMsg printInvoiceXX(OutputStream os) {
		BiResultQuotation ppr = null;
		ppr = (BiResultQuotation) getParent();
//		if(ppr == null) return(new ReturnMsg(false,"Error : no parent quotation record"));
		
		try {
    		String paperType = Erpv4Config.getString(getSessionHelper(),"InvPaperType");
    		String invDoc = Erpv4Config.getString(getSessionHelper(),"InvDocCode");
    		if(invDoc == null) invDoc = "GENINV01";
    		PrtdocJson ppj = PrtdocJson.newPrtdocJson(	
    				getCellString("invh_cocode"),
    				paperType,
    			    invDoc,
    			    "erpv4_printDocument"
    				) ;

    		ppj.setTrailerAtLastPageOnly(true);
    		ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
    		int it = getCellInt("invh_invtype");
    		switch(it) {
    		case 1 : ppj.addHeaderField("doctitle","DEBIT NOTE");
    			break;
    		case 2 : ppj.addHeaderField("doctitle","CREDIT NOTE");
    			break;
    		default : ppj.addHeaderField("doctitle","INVOICE");
    			break;
    		}
    		if(Erpv4Config.getDefaultLogo(getSessionHelper()) != null) {
    			ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(getSessionHelper()) ,0,0,0,440);
    		}

    		ppj.addHeaderField("cvname",getCellString("vd_vname"));
    		/*
    		ppj.addHeaderField("cvname",getCellString("vd_addr0"),0,20);
    		ppj.addHeaderField("cvname",getCellString("vd_addr1"),0,40);
    		ppj.addHeaderField("cvname",getCellString("vd_addr2"),0,60);
    		*/
    		String addr = getCellString("vd_addr0").trim() + " " +  getCellString("vd_addr1").trim() + " " + getCellString("vd_addr2").trim();
    		ppj.addHeaderField("cvname",addr,0,20,0,320);
    		
    		ppj.addHeaderField("clphone","Phone",0,0);
    		ppj.addHeaderField("cvphone",getCellString("invh_tel"),0,0);
    		ppj.addHeaderField("clphone","Fax",0,18);
    		ppj.addHeaderField("cvphone",getCellString("invh_fax"),0,18);
    		ppj.addHeaderField("clphone","Attn",0,36);
    		ppj.addHeaderField("cvphone",getCellString("invh_contact"),0,36);
    		ppj.addHeaderField("dflabel","Invoice #",0,0);
    		ppj.addHeaderField("dfvalue",getCellString("invh_invno"),0,0);
    		ppj.addHeaderField("dflabel","Date",0,30);
    		ppj.addHeaderField("dfvalue", DateUtil.toDateString( getCell("invh_date").getDate(),"dd/mm/yyyy") ,0,30);
    		ppj.addHeaderField("dflabel","Page",0,60);


    		ppj.addDetailHeaderField("hdr_duedate","Due Date");
    		ppj.addDetailHeaderField("hdr_terms","TERMS.");
    		ppj.addDetailHeaderField("hdr_delivery","DELIVERY");
    		ppj.addDetailHeaderField("hdr_yourref","YOUR REF.");
    		ppj.addDetailHeaderField("hdr_ourref","OUR REF.");
    		ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
    		ppj.addDetailHeaderField("hdr_qty","QTY");
    		ppj.addDetailHeaderField("hdr_uprice","UNIT PRICE",0,-5);
    		ppj.addDetailHeaderField("hdr_uprice","("+ getCellString("invh_cid")+")",0,12);
    		ppj.addDetailHeaderField("hdr_disc","DISC",0,-5);
    		ppj.addDetailHeaderField("hdr_disc","%",0,12);
    		ppj.addDetailHeaderField("hdr_amount","AMOUNT",0,-5);
    		ppj.addDetailHeaderField("hdr_amount","("+ getCellString("invh_cid")+")",0,12);
    		ppj.addDetailHeaderField("val_yourref",getCell("invh_pocode").getColumnDisplayString());
    		ppj.addDetailHeaderField("val_ourref",getCell("invh_jobno").getColumnDisplayString());
    		
    		ppj.addDetailHeaderField("val_duedate",DateUtil.toDateString( getCell("invh_duedate").getDate(),"dd/mm/yyyy") ,0,0);
    		
    		Vector<BiCellCollection> v = null;
    		String indPrefix;
    		if(ppr != null) {
    			BiResult ssr = ppr.getSubLink(ppr.subLinkId);
    			v = ssr.getRowCollectionList();
    			indPrefix="ind_";
    		} else {
    			BiResult ssr = getSubLink(indLinkName);
    			v = ssr.getRowCollectionList();
    			indPrefix="invd_";
    		}
    		ppj.addDetailRecord();
    		ppj.addDetailRecordField("description", getCell("invh_projecttitle").getString());
    		
    		for(BiCellCollection c : v) {
    			ppj.addDetailRecord();
    			String s = makeInvoiceItemDescription(indPrefix,c,ppj);
    			ppj.addDetailRecordField("description", s);
    			ppj.setBold(false);
//    			ppj.setUnderLine(false);
    			if(c.getCell(indPrefix+"qty").getDouble() != 0) {
    			ppj.addDetailRecordField("quantity", c.getCell(indPrefix+"qty").getString());
    			}
    			if((c.getCellDouble(indPrefix+"sprice") != 0)  &&
    			   (c.getCell(indPrefix+"discpercent").getInt() != 0)) {
   					ppj.addDetailRecordField("price", c.getCell(indPrefix+"sprice").getString());
    			} else {
    				if(c.getCell(indPrefix+"uprice").getDouble() != 0) {
    					ppj.addDetailRecordField("price", c.getCell(indPrefix+"uprice").getString());
    				}
    				
    			}
    			if(c.getCell(indPrefix+"discpercent").getInt() > 0) {
    				ppj.addDetailRecordField("discount", c.getCell(indPrefix+"discpercent").getString());
    			}
    			if(c.getCell(indPrefix+"amount").getDouble() != 0) {
    				ppj.addDetailRecordField("amount", c.getCell(indPrefix+"amount").getString());
    			}
    		}
    		ppj.addDetailRecord();
    		if(!getCellString("invh_quonum").equals(""))
    			ppj.addDetailRecordField("description", "[Our Ref: " + getCell("invh_quonum").getString() + "]");
    		ppj.addBottomField("val_signco",Erpv4Config.getCoName(getSessionHelper(),getCellString("invh_cocode")));
    		ppj.addBottomField("hdr_total","Invoice Total:");
    		ppj.addBottomField("hdr_total","Less Discount:",0,20);
    		ppj.addBottomField("hdr_total","Less Trade In:",0,40);
    		ppj.addBottomField("hdr_total","Delivery:",0,60);
    		ppj.addBottomField("hdr_total","Net Amount:",0,80);
    		ppj.addBottomField("val_total",getCell("invh_total").getColumnDisplayString());
    		if(getCell("invh_discount").getDouble() != 0) {
    			ppj.addBottomField("val_total",getCell("invh_discount").getColumnDisplayString(),0,20);
    		}
    		if(getCell("invh_tradein").getDouble() != 0) {
    			ppj.addBottomField("val_total",getCell("invh_tradein").getColumnDisplayString(),0,40);
    		}
    		if(getCell("invh_delichg").getDouble() != 0) {
    			ppj.addBottomField("val_total",getCell("invh_delichg").getColumnDisplayString(),0,60);
    		}
   			ppj.addBottomField("val_total",getCell("invh_total").getColumnDisplayString(),0,80);
   			String remark = null; 
			if(remark == null) remark = getCell("invh_remark").getString(); else remark += "\r" + getCell("invh_remark").getString();
			if(!getCellString("invh_paytype").equals("")) {
				remark += "\r"+ getCellString("invh_paytype");
			}
			if(!getCellString("invh_term").equals("")) {
				remark += "\r"+ getCellString("invh_term");
			}

				SelectUtil su = getSelectUtil();
				TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+getCellString("invh_cocode")+ "'",null);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					if(!remark.trim().equals("")) remark += "\r";
					if(ppr == null) {
						if(getCellInt("invh_bankinfo") > 0) {
							remark += tr.getFieldString("co_payment2");
						} else {
							remark += tr.getFieldString("co_payment");
						}
					} else remark += tr.getFieldString("co_payment");
				}
   			ppj.addBottomField("val_remark",remark,0,0,15,0);
    		return(ppj.toPdfStream(os, getSessionHelper()));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Fail Reason Unknown"));
		}	
	}
	
	boolean inClearRec = false;
	public void clearCurrentRec() {
		inClearRec = true;
		super.clearCurrentRec();
		inClearRec = false;
	}
	public void sumGrTotal()
	{
			if(inClearRec) return;
			if(indLinkName == null) return;
			Cell grtotal  = getCell("invh_grtotal");
			if(grtotal == null) return;
    		BiResult sr = getSubLink(indLinkName);
    		if(sr == null) return;
    		Vector<BiCellCollection> v = sr.getRowCollectionList();
    		double fval = 0;
    		for(BiCellCollection c : v) {
    			fval += c.getDouble("invd_amount");
    		}
    		try {
    			grtotal.set(fval);
    		} catch (CellException cex) {
    			UniLog.log(cex);
    		}
	}
	public CellValueAction indUpdateGrTotal = new CellValueAction() {

		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			// TODO Auto-generated method stub
			if(isActionEnabled()) sumGrTotal();
		}
		@Override
		public void cellAction_onfree() throws CellException {
			// TODO Auto-generated method stub
		}
	};
	
	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtn = super.biBeforeAddUpdateCurrent(col, isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		if(col.getCellString("invh_invno").equals("")) {
			RpcClient rpc = getSelectUtil().getRpcClient();
			rpc.callSegment("setCocodeBaseccy",
							new VectorUtil()
							.addElement(
//										Erpv4Config.getCoCode(getSessionHelper()) 
										col.getCellString("invh_cocode")
								)
							.addElement(
									Erpv4Config.getBaseCcy(getSessionHelper(),col.getCellString("invh_cocode"))
									)
							.toVector()
							);
			int it = col.getCellInt("invh_invtype");

			String rgtype = "arinv";
			switch(it) {
			case 1: rgtype = "ardbn"; break;
			case 2: rgtype = "arcrn"; break;
			}
			Value val = rpc.callSegment("getrg_byrgcontrol_bycategory",
				new VectorUtil()
				.addElement(rgtype)
				.addElement(getCell("invh_date").getDate())
				.toVector()
				);
			if(val == null || !(val instanceof Strval)) {
				return(new ReturnMsg(false,"Error : get voucher number failed"));
			}
			try {
				getCell("invh_invno").set(val.toString().trim());
			} catch (CellException cex) {
				UniLog.log(cex);
				return(new ReturnMsg(false,"Error : get voucher number failed"));
			}
		}
		return(ReturnMsg.defaultOk);
	}	
	
}
