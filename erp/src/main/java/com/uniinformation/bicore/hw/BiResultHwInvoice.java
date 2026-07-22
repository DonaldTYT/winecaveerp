package com.uniinformation.bicore.hw;

import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.bicore.erpv4.BiResultQuoDet;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.prtdoc.PrtdocPerfJson;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.UniqueStrings;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultHwInvoice extends BiResultHwOrderBase {

	public CellValueAction actionInvTotal = new CellValueAction() {

		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			// TODO Auto-generated method stub
			if(isActionEnabled()) realCalInvTotal();
			
		}

		@Override
		public void cellAction_onfree() throws CellException {
			// TODO Auto-generated method stub
			
		}
		
	};

	public BiResultHwInvoice(BiResult p_parent, BiView p_view, SelectUtil p_su,
			Vector p_tabList, String p_whereStr, SessionHelper p_sh)
			throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
//		subLinkId = "hw.InvDet";
		subLinkId = "hw.InvQuoDet";
		invdLinkId = "hw.InvDet";
		isInvoice = true;
	}

	public static String getNewQuotationNumber(SelectUtil p_su,java.util.Date p_date) throws Exception {
				java.util.Date d = p_date;
				String s = "";
				String ds = DateUtil.toDateString(d, "yymm");
				int nextidx = 1;
				TableRec tr = p_su.getQueryResult("select inv_quonum from quotation where inv_quonum like 'Q" + ds + "' order by inv_quonum desc",null);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					s = tr.getField("inv_quonum").toString();
					String ss = StringUtil.strpart(s, 5, -1);
					nextidx = Integer.parseInt(ss) + 1;
				}
				s = String.format("%s%s%03d",'Q',ds, nextidx);
				return(s);
	}
	/*
	public static String getNewInvoiceNumber(SelectUtil p_su,java.util.Date p_date) throws Exception {
				java.util.Date d = p_date;
				String s = "";
				String ds = DateUtil.toDateString(d, "yymm");
				int nextidx = 1;
				TableRec tr = p_su.getQueryResult("select inv_invno from invoice where inv_invno like '" + ds + "' order by inv_invno desc",null);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					s = tr.getField("inv_invno").toString();
					String ss = StringUtil.strpart(s, 4, -1);
					nextidx = Integer.parseInt(ss) + 1;
				}
				s = String.format("%s%03d",ds, nextidx);
				return(s);
	}
	*/
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg msg = super.biBeforeUpdateCurrent(col);
		if(msg == null || !msg.getStatus()) return(msg);
		RpcClient rpc = getSelectUtil().getRpcClient();
		rpc.callSegment("setCocodeBaseccy",
					new VectorUtil()
//						.addElement(Erpv4Config.getCoCode(getSessionHelper()))
//						.addElement(Erpv4Config.getBaseCcy(getSessionHelper()))
						.addElement(getCellString("inv_cocode"))
						.addElement(Erpv4Config.getBaseCcy(getSessionHelper(),getCellString("inv_cocode")))
						.toVector()
					);
		Value v = rpc.callSegment("erpv4InvoiceUpdate",
					new VectorUtil()
						.addElement(getCell("inv_invno").getString())
						.addElement(getCell("inv_quostatus").getString())
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
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
//		try {
//			TableRec tr = su.getQueryResult("select * from invdet where ind_rg = "+col.getCell("inv_rg").getInt()+" and ind_linked > 0");
//			for(int i = 0;i<tr.getRecordCount();i++) {
//				tr.setRecPointer(i);
//				int linkedInvdet = tr.getFieldInt("ind_linked");
//				if(linkedInvdet > 0) {
//					su.executeUpdate("update quodet set ind_linked = 0 where ind_odrg = " + linkedInvdet,null);
//				}
//			}
//		} catch (Exception ex) {
//			UniLog.log(ex);
//			return(new ReturnMsg(false,"Error unlink quodet",true));
//		}
		if(getCell("inv_quostatus").equals("Confirmed")) {
			return(new ReturnMsg(false,"Cannot Remove Confirmed Invoice"));
		}
		return(super.biBeforeDeleteCurrent(col));
	}
	@Override
	public String getNewOrderNumber(java.util.Date p_date) throws Exception {
		return(BiResultErpv4.getCodeByRgControl(this, getCellString("inv_cocode"),"invoicing", p_date));
//		return(getNewInvoiceNumber(su,p_date));
//		return("");
	}

	@Override
	public void real_calTotalAmount() throws CellException {
		
	}
	public ReturnMsg printInvoice(OutputStream os) {
		try {
    		String paperType = Erpv4Config.getString(getSessionHelper(),"InvPaperType");
    		PrtdocJson ppj = PrtdocJson.newPrtdocJson(	
    				getCellString("inv_cocode"),
    				paperType,
    			    "KHINV01",
    			    "erpv4_printDocument"
    				) ;
    		ppj.setTrailerAtLastPageOnly(false);
    		ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
    		ppj.addHeaderField("doctitle","INVOICE");
    		/*
    		if(Erpv4Config.getDefaultLogo(getSessionHelper()) != null) {
    			ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(getSessionHelper()) ,0,0,0,440);
    		}
    		*/
    		ppj.addHeaderField("cvnameb",getCellString("vd_vname"));
    		/*
    		ppj.addHeaderField("cvname",getCellString("inv_addr0"),0,20);
    		ppj.addHeaderField("cvname",getCellString("inv_addr1"),0,40);
    		ppj.addHeaderField("cvname",getCellString("inv_addr2"),0,60);
    		*/
    		PrtdocJson.addMultiHeaderField(ppj, "cvname", 0, 20, 20, 0, 
    					PrtdocJson.joinAndSplit("ariblk", "chinese", 9, 360,
    									getCellString("inv_addr0"),
    										getCellString("inv_addr1"),
    											getCellString("inv_addr2")
    							) 
    				);
    		ppj.addHeaderField("clphone","Phone",0,0);
    		ppj.addHeaderField("cvphone",getCellString("inv_tel"),0,0);
    		ppj.addHeaderField("clphone","Fax",0,18);
    		ppj.addHeaderField("cvphone",getCellString("inv_fax"),0,18);
    		ppj.addHeaderField("clphone","Attn",0,36);
    		ppj.addHeaderField("cvphone",getCellString("inv_contact"),0,36);
    		ppj.addHeaderField("dflabel","Invoice #",0,0);
    		ppj.addHeaderField("dfvalue",getCellString("inv_invno"),0,0);
    		ppj.addHeaderField("dflabel","Date",0,30);
    		ppj.addHeaderField("dfvalue", DateUtil.toDateString( getCell("inv_date").getDate(),"dd/mm/yyyy") ,0,30);
    		ppj.addHeaderField("dflabel","Page",0,60);

    		ppj.addDetailHeaderField("hdr_duedate","Due Date");
    		ppj.addDetailHeaderField("hdr_terms","TERMS.");
    		ppj.addDetailHeaderField("hdr_delivery","DELIVERY");
    		ppj.addDetailHeaderField("hdr_yourref","YOUR REF.");
    		ppj.addDetailHeaderField("hdr_ourref","OUR REF.");
    		ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
    		ppj.addDetailHeaderField("hdr_qty","QTY");
    		ppj.addDetailHeaderField("hdr_uprice","UNIT PRICE",0,-5);
    		ppj.addDetailHeaderField("hdr_uprice","(HKD)",0,12);
    		ppj.addDetailHeaderField("hdr_disc","DISC",0,-5);
    		ppj.addDetailHeaderField("hdr_disc","%",0,12);
    		ppj.addDetailHeaderField("hdr_amount","AMOUNT",0,-5);
    		ppj.addDetailHeaderField("hdr_amount","(HKD)",0,12);
    		ppj.addDetailHeaderField("val_yourref",getCell("inv_pocode").getColumnDisplayString());
    		
    		ppj.addDetailHeaderField("val_duedate",DateUtil.toDateString( getCell("inv_duedate").getDate(),"dd/mm/yyyy") ,0,0);
    		
    		{
    			Date d0 = getCell("inv_duedate").getDate();
    			Date d1 = getCell("inv_date").getDate();
    			if(
    					d0.after(DateUtil.zeroDate)
    					&& d1.after(DateUtil.zeroDate)
    					&& !d0.after(d1)) {
    					ppj.addDetailHeaderField("val_terms","COD");
    						
  					}
    		}
    		
    		BiResult sr = getSubLink(invdLinkId);
    		Vector<BiCellCollection> v = sr.getRowCollectionList();
    		ppj.addDetailRecord();
    		ppj.addDetailRecordField("description", getCell("inv_projecttitle").getString());
    			
    		for(BiCellCollection c : v) {
    			ppj.addDetailRecord();
    			ppj.addDetailRecordField("description", c.getCell("invd_desc").getString());
    			if(c.getCell("invd_qty").getDouble() != 0) {
    			ppj.addDetailRecordField("quantity", c.getCell("invd_qty").getString());
    			}
    			if((c.getCell("invd_sprice").getDouble() != 0)  &&
    			   (c.getCell("invd_discpercent").getInt() != 0)) {
   					ppj.addDetailRecordField("price", "$"+((ColumnCell) c.getCell("invd_sprice")).getColumnDisplayString());
    			} else {
    				if(c.getCell("invd_uprice").getDouble() != 0) {
    					ppj.addDetailRecordField("price", "$"+((ColumnCell) c.getCell("invd_uprice")).getColumnDisplayString());
    				}
    			}
    			if(c.getCell("invd_discpercent").getInt() > 0) {
    				ppj.addDetailRecordField("discount", c.getCell("invd_discpercent").getString()+"%");
    			}
    			if(c.getCell("invd_amount").getDouble() != 0) {
    				ppj.addDetailRecordField("amount", "$"+((ColumnCell) c.getCell("invd_amount")).getColumnDisplayString());
    			}
    		}
    		sr = getSubLink(subLinkId);
    		v = sr.getRowCollectionList();
    		UniqueStrings us = new UniqueStrings(",");
    		for(CellCollection c : v) {
    			us.add(c.getCellString("quotation_invno"));
    		}
    		
    		ppj.addDetailRecord();
   			ppj.addDetailRecordField("description", "[Our Ref: " + us.toString() + "]");
    		/*
    		if(!getCellString("inv_quonum").equals(""))
    			ppj.addDetailRecordField("description", "[Our Ref: " + getCell("inv_quonum").getString() + "]");
    		*/
   			ppj.setAttribute(PrtdocJson.AttrName.ATTR_hideOnFirstPage);
   			ppj.setAttribute(PrtdocJson.AttrName.ATTR_hideOnMiddlePage);
    		ppj.addBottomField("hdr_signco","For and on behalf of");
    		ppj.addBottomField("val_signco",Erpv4Config.getCoName(getSessionHelper(),getCellString("inv_cocode")));
    		ppj.addBottomField("hdr_signco","(Authorized Signature)",0,120);
    		ppj.setUnderLine(true);
    		ppj.addBottomField("hdr_signco","                                                                  ",0,100);
    		ppj.setUnderLine(false);
    		ppj.addBottomField("hdr_total","Total:");
    		ppj.addBottomField("hdr_total","Delivery:",0,20);
   			ppj.addBottomField("val_total","$0",0,20);
    		ppj.addBottomField("hdr_total","Less Discount:",0,40);
    		ppj.addBottomField("hdr_total","PAID TODAY:",0,60);
    		ppj.addBottomField("hdr_totalb","Balance Due:",0,85);
    		ppj.addBottomField("val_total","$"+getCell("inv_dettotal").getColumnDisplayString());
    		if(getCell("inv_discount").getDouble() != 0) {
    			ppj.addBottomField("val_total","$"+getCell("inv_discount").getColumnDisplayString(),0,40);
    		} else {
    			ppj.addBottomField("val_total","$0",0,40);
    		}
   			ppj.addBottomField("val_totalb","$"+getCell("inv_total").getColumnDisplayString(),0,85);

   			String remark = null; 
				SelectUtil su = getSelectUtil();
				TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+getCellString("inv_cocode")+ "'",null);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					remark = tr.getFieldString("co_payment");
				}
			if(remark == null) remark = getCell("inv_remark").getString(); else remark += "\r" + getCell("inv_remark");
   			ppj.addBottomField("val_remark",remark,0,0,15,0);
   			ppj.unsetAttribute(PrtdocJson.AttrName.ATTR_hideOnFirstPage);
   			ppj.unsetAttribute(PrtdocJson.AttrName.ATTR_hideOnMiddlePage);
    		return(ppj.toPdfStream(os, getSessionHelper()));
		} catch (Exception ex) {
			return(new ReturnMsg(false,"Fail Reason Unknown"));
		}
	}

	public void realCalInvTotal() {
    		BiResult sr = getSubLink("hw.InvDet");
    		Vector<BiCellCollection> v = sr.getRowCollectionList();
    		double fval = 0;
    		for(BiCellCollection c : v) {
    			fval += c.getDouble("invd_amount");
    		}
    		try {
    			getCell("inv_dettotal").set(fval);
    		} catch (CellException cex) {
    			UniLog.log(cex);
    		}
	}
	
}
