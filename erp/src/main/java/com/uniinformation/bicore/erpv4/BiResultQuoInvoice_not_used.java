package com.uniinformation.bicore.erpv4;

import java.io.OutputStream;
import java.util.Vector;

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
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.prtdoc.PrtdocPerfJson;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultQuoInvoice_not_used extends BiResultErpv4 {
	
	CellValueAction updateInvoice = new CellValueAction() {

		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			ColumnCell cc= ((ColumnCell) p_value);
			CellCollection col = cc.getCollection();
			if(cc.getCellLabel().equals("invh_payratio")) {
//				double gTotal = col.getCell("inv_total").getDouble();
				double qTotal = col.getCell("invh_qnettotal").getDouble();
				double ratio = col.getCell("invh_payratio").getDouble();
				col.getCell("invh_total").set(qTotal * ratio / 100);
			} else if(cc.getCellLabel().equals("invh_qnettotal")) {
				double qTotal = col.getCell("invh_qnettotal").getDouble();
				double iTotal = col.getCell("invh_total").getDouble();
				double ratio = iTotal/qTotal * 100;
				col.getCell("invh_payratio").set(ratio);
				col.getCell("invh_total").set(iTotal);
			}
			
		}

		@Override
		public void cellAction_onfree() throws CellException {
			// TODO Auto-generated method stub
			
		}
		
	};

	public BiResultQuoInvoice_not_used(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		if(col.getCell("invh_quostatus").equals("Confirmed")) {
			return(new ReturnMsg(false,"Cannot Remove Invoice"));
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
						.addElement(getCellString("invh_cocode"))
						.addElement(Erpv4Config.getBaseCcy(getSessionHelper(),getCellString("invh_cocode")))
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

	@Override
	protected void afterLoadCollection(boolean p_isFetch,BiCellCollection p_cc){
		super.afterLoadCollection(p_isFetch, p_cc);
		if(p_cc.getCell("invh_quostatus").equals("Confirmed")) {
			try {
				((BiCellCollection) p_cc).lock();
				p_cc.getCell("invh_post").setMode(Cell.VMODE_NORMAL);
				p_cc.getCell("invh_print").setMode(Cell.VMODE_NORMAL);
			} catch(CellException cex) {
				UniLog.log(cex);
			}
		}
	}
	@Override
	protected void createColumnCells(BiCellCollection p_col)
	{
		super.createColumnCells(p_col);
		p_col.getCell("invh_payratio").addAction(updateInvoice);
		p_col.getCell("invh_qnettotal").addAction(updateInvoice);
		/*
		p_col.getCell("invh_grtotal").addAction(updateInvoice);
		*/
	}

	public ReturnMsg printInvoice(OutputStream os) {
		BiResultQuotation pr = (BiResultQuotation) getParent();
		if(pr == null) return(new ReturnMsg(false,"Error : no parent quotation record"));
		
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
    		ppj.addHeaderField("doctitle","INVOICE");
    		/*
    		if(Erpv4Config.getDefaultLogo(getSessionHelper()) != null) {
    			ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(getSessionHelper()) ,0,0,0,440);
    		}
    		*/
    		ppj.addHeaderField("cvname",getCellString("vd_vname"));
    		ppj.addHeaderField("cvname",getCellString("inv_addr0"),0,20);
    		ppj.addHeaderField("cvname",getCellString("inv_addr1"),0,40);
    		ppj.addHeaderField("cvname",getCellString("inv_addr2"),0,60);
    		ppj.addHeaderField("clphone","Phone",0,0);
    		ppj.addHeaderField("cvphone",getCellString("inv_tel"),0,0);
    		ppj.addHeaderField("clphone","Fax",0,18);
    		ppj.addHeaderField("cvphone",getCellString("inv_fax"),0,18);
    		ppj.addHeaderField("clphone","Attn",0,36);
    		ppj.addHeaderField("cvphone",getCellString("inv_contact"),0,36);
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
    		ppj.addDetailHeaderField("hdr_uprice","(HKD)",0,12);
    		ppj.addDetailHeaderField("hdr_disc","DISC",0,-5);
    		ppj.addDetailHeaderField("hdr_disc","%",0,12);
    		ppj.addDetailHeaderField("hdr_amount","AMOUNT");
    		ppj.addDetailHeaderField("val_yourref",pr.getCell("inv_pocode").getColumnDisplayString());
    		
    		ppj.addDetailHeaderField("val_duedate",DateUtil.toDateString( getCell("invh_duedate").getDate(),"dd/mm/yyyy") ,0,0);
    		
    		BiResult sr = pr.getSubLink(pr.subLinkId);
    		Vector<BiCellCollection> v = sr.getRowCollectionList();
    		ppj.addDetailRecord();
    		ppj.addDetailRecordField("description", getCell("inv_projecttitle").getString());
    			
    		for(BiCellCollection c : v) {
    			ppj.addDetailRecord();
    			ppj.addDetailRecordField("description", c.getCell("ind_desc").getString());
    			if(c.getCell("ind_qty").getDouble() != 0) {
    			ppj.addDetailRecordField("quantity", c.getCell("ind_qty").getString());
    			}
    			if((c.getCellDouble("ind_sprice") != 0)  &&
    			   (c.getCell("ind_discpercent").getInt() != 0)) {
   					ppj.addDetailRecordField("price", c.getCell("ind_sprice").getString());
    			} else {
    				if(c.getCell("ind_uprice").getDouble() != 0) {
    					ppj.addDetailRecordField("price", c.getCell("ind_uprice").getString());
    				}
    			}
    			if(c.getCell("ind_discpercent").getInt() > 0) {
    				ppj.addDetailRecordField("discount", c.getCell("ind_discpercent").getString());
    			}
    			if(c.getCell("ind_amount").getDouble() != 0) {
    				ppj.addDetailRecordField("amount", c.getCell("ind_amount").getString());
    			}
    		}
    		ppj.addDetailRecord();
    		if(!getCellString("inv_quonum").equals(""))
    			ppj.addDetailRecordField("description", "[Our Ref: " + getCell("inv_quonum").getString() + "]");
    		ppj.addBottomField("val_signco",Erpv4Config.getCoName(getSessionHelper(),getCellString("invh_cocode")));
    		ppj.addBottomField("hdr_total","Total:");
    		ppj.addBottomField("hdr_total","Delivery:",0,25);
    		ppj.addBottomField("hdr_total","Less Discount:",0,50);
    		ppj.addBottomField("hdr_total","Balance Due:",0,75);
    		ppj.addBottomField("val_total",getCell("inv_grtotal").getColumnDisplayString());
//    		ppj.addBottomField("val_total",getCell("inv_dettotal").getColumnDisplayString());
    		if(getCell("inv_discount").getDouble() != 0) {
    			ppj.addBottomField("val_total",getCell("inv_discount").getColumnDisplayString(),0,50);
    		}
   			ppj.addBottomField("val_total",getCell("inv_total").getColumnDisplayString(),0,75);

   			String remark = null; 
				SelectUtil su = getSelectUtil();
//				TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+Erpv4Config.getCoCode(getSessionHelper())+ "'",null);
				TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+getCellString("invh_cocode")+ "'",null);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					remark = tr.getFieldString("co_payment");
				}
			if(remark == null) remark = getCell("inv_remark").getString(); else remark += "\r" + getCell("inv_remark");
   			ppj.addBottomField("val_remark",remark,0,0,15,0);
    		return(ppj.toPdfStream(os, getSessionHelper()));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Fail Reason Unknown"));
		}	
		
	}

}
