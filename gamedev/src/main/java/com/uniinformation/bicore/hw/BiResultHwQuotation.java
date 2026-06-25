package com.uniinformation.bicore.hw;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
//import com.sun.glass.ui.Window;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValidation;
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
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;


public class BiResultHwQuotation extends BiResultHwOrderBase {

	public CellValueAction actionQuoTotal = new CellValueAction() {

		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			// TODO Auto-generated method stub
			if(isActionEnabled()) realCalQuoTotal();
			
		}

		@Override
		public void cellAction_onfree() throws CellException {
			// TODO Auto-generated method stub
			
		}
		
	};
	protected String quoExtraLinkid= null;
	public BiResultHwQuotation(BiResult p_parent, BiView p_view,
			SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		Vector<BiResult> v = getSubLinks();
		for(BiResult sr :v) {
			if(sr.getView().getTable().getName().equals("quoextra")) {
				quoExtraLinkid= sr.getView().getName();
			}
		}
		
		
		// TODO Auto-generated constructor stub
	}
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		try {
			TableRec tr = su.getQueryResult("select * from quodet where ind_rg = "+col.getCell("inv_rg").getInt()+" and ind_linked > 0");
			for(int i = 0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				int linkedInvdet = tr.getFieldInt("ind_linked");
				if(linkedInvdet > 0) {
					su.executeUpdate("update invdet set ind_linked = 0 where ind_odrg = " + linkedInvdet,null);
				}
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Error unlink invdet",true));
		}
		return(super.biBeforeDeleteCurrent(col));
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht  = super.addExtraWhereStr(p_where, p_hash);
		String uid = getSelectUtil().getLoginId();
		UniLog.log("user = " + getSelectUtil().getLoginId());
		if(!BiSchema.hasAccessRight(sh, "allorder")) {
//			p_where.appendString(" and inv_assignby = '"+uid+"' ");
			
			
//			p_where.genInList("and", "inv_assignby", "in", sh.getAccessUsers());
			Wherecl wcl1 = new Wherecl();
			wcl1.genInList("and", "inv_assignby", "in", sh.getAccessUsers());
			HashSet<String> deptlist = getSessionHelper().getMatchedAccessRights("^dept");
			if(deptlist != null && !deptlist.isEmpty()) {
				Wherecl wcl2 = new Wherecl();
				String ss = null;
				for(String as : deptlist) {
					if(ss == null) ss = (
							"inv_rg in (select quodetxx.ind_rg from quodet quodetxx,stmcmodel stmcmodelxx where stmcmodelxx.stmcm_rg = quodetxx.ind_srg and stmcmodelxx.stmcm_code in('"
							+as+"')"); else ss += ",'"+as+"'";
				}
				ss += ")";
				wcl2.appendString(ss);
				wcl1.orWherecl(wcl2);
			}
			p_where.andWherecl(wcl1);
		} 
		return(ht);
	}
	ReturnMsg checkAndUpdateQuoStatus(ReturnMsg rtnMsg,CellCollection col) {
		try {
			if(quoExtraLinkid != null) {
				BiResult sr = getSubLink(quoExtraLinkid);
				BiResult sr2 = getSubLink(subLinkId);
				int nWo = sr2.getRowCollectionList().size();
				int nQuo = sr.getRowCollectionList().size();
				if(nWo == 0 && nQuo == 0) {
					return(new ReturnMsg(false,"Error : both workorder and quotation detail are empty"));
				}
				if(sr.getRowCollectionList().size() > 0) {
					String ss = col.getCell("inv_quonum").getString();
					if(ss == null || ss.trim().equals("")) {
						ss = super.getNewOrderNumber(DateUtil.today());
						col.getCell("inv_quonum").set(ss);
					}
				}
				if(nWo == 0) {
					getCell("inv_quostatus").set("Quoted");
				} else {
					if(getCell("inv_quostatus").getString().equals("Quoted")) {
						getCell("inv_quostatus").set("New");
					}
				}
			}
			return(rtnMsg);
		} catch (Exception cex ) {
			UniLog.log(cex);
			return(new ReturnMsg(false,cex.toString()));
		}
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		return(checkAndUpdateQuoStatus(rtnMsg,col));
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col) {
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		return(checkAndUpdateQuoStatus(rtnMsg,col));
	}
	@Override
	public String getNewOrderNumber(java.util.Date p_date) throws Exception {
		int cc ;
		cc = getView().getSchema().getRg(this,"", 3801);
		return(String.format("J%06d", cc));
//		return(String.format("J%05d", getCell("inv_rg").getInt()));
	}
	
	public void realCalQuoTotal() {
    		BiResult sr = getSubLink(quoExtraLinkid);
    		Vector<BiCellCollection> v = sr.getRowCollectionList();
    		double fval = 0;
    		for(BiCellCollection c : v) {
    			fval += c.getDouble("indx_amount");
    		}
    		try {
//    			fval -= getCell("inv_quodiscount").getDouble();
//    			getCell("inv_grnettotal").set(fval);
    			getCell("inv_quototal").set(fval);
    		} catch (CellException cex) {
    			UniLog.log(cex);
    		}
	}
	public ReturnMsg printQuotation(OutputStream os) {
		try {
    		String paperType = Erpv4Config.getString(getSessionHelper(),"QuoPaperType");
    		PrtdocJson ppj = PrtdocJson.newPrtdocJson(
    				getCellString("inv_cocode"),
    				paperType,
    			    "KHQUO01",
    			    "erpv4_printDocument"
    				) ;
    		ppj.setTrailerAtLastPageOnly(true);
    		ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
    		ppj.addHeaderField("doctitle","Quotation");
//			ppj.addHeaderImage("logo","logo/kh_logo_a.png",0,0,0,440);
    		ppj.addHeaderField("cvname",getCellString("vd_vname"));
    		ppj.addHeaderField("cvname",getCellString("inv_addr0"),0,20);
    		ppj.addHeaderField("cvname",getCellString("inv_addr1"),0,40);
    		ppj.addHeaderField("cvname",getCellString("inv_addr2"),0,60);
    		ppj.addHeaderField("cvname",getCellString("inv_addr3"),0,80);
    		ppj.addHeaderField("clphone","Phone",0,0);
    		ppj.addHeaderField("cvphone",getCellString("inv_tel"),0,0);
    		ppj.addHeaderField("clphone","Fax",0,18);
    		ppj.addHeaderField("cvphone",getCellString("inv_fax"),0,18);
    		ppj.addHeaderField("clphone","Attn",0,36);
    		ppj.addHeaderField("cvphone",getCellString("inv_contact"),0,36);
    		ppj.addHeaderField("dflabel","Quotation #",0,0);
    		ppj.addHeaderField("dfvalue",getCellString("inv_quonum"),0,0);
    		ppj.addHeaderField("dflabel","Date",0,30);
    		ppj.addHeaderField("dfvalue", DateUtil.toDateString( getCell("inv_quodate").getDate(),"dd/mm/yyyy") ,0,30);
    		ppj.addHeaderField("dflabel","Page",0,60);


    		ppj.addDetailHeaderField("hdr_pono","P.O. No.");
    		ppj.addDetailHeaderField("hdr_terms","TERMS.");
    		ppj.addDetailHeaderField("hdr_yourref","YOUR REF.");
    		ppj.addDetailHeaderField("hdr_ourref","OUR REF.");
    		ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
    		ppj.addDetailHeaderField("hdr_qty","QTY");
    		ppj.addDetailHeaderField("hdr_uprice","UNIT PRICE",0,-5);
    		ppj.addDetailHeaderField("hdr_uprice","(HKD)",0,12);
    		ppj.addDetailHeaderField("hdr_disc","DISC",0,-5);
    		ppj.addDetailHeaderField("hdr_disc","%",0,12);
    		ppj.addDetailHeaderField("hdr_amount","AMOUNT");	
    		ppj.addDetailHeaderField("val_ourref",getCellString("inv_ourref"));
    		ppj.addDetailHeaderField("val_yourref",getCellString("inv_yourref"));
    		ppj.addDetailHeaderField("val_terms",getCellString("inv_term"));
    		ppj.addDetailHeaderField("val_pono",getCellString("inv_pocode"));
    		
    		BiResult sr = getSubLink(quoExtraLinkid);
    		Vector<BiCellCollection> v = sr.getRowCollectionList();
    		for(BiCellCollection c : v) {
    			ppj.addDetailRecord();
    			ppj.addDetailRecordField("description", c.getCell("indx_desc").getString());
    			if(c.getCell("indx_qty").getDouble() != 0) {
    				ppj.addDetailRecordField("quantity", c.getCell("indx_qty").getString());
    			}
    			if(c.getCell("indx_uprice").getDouble() != 0) {
    				ppj.addDetailRecordField("price", c.getCell("indx_uprice").getString());
    			}
    			if(c.getCell("indx_discount").getInt() > 0) {
    				ppj.addDetailRecordField("discount", c.getCell("indx_discount").getString());
    			}
    			if(c.getCell("indx_amount").getDouble() != 0) {
    				ppj.addDetailRecordField("amount", c.getCell("indx_amount").getString());
    			}
    		}
    		
    		ppj.addBottomField("val_signco",Erpv4Config.getCoName(getSessionHelper(),getCellString("inv_cocode")));
    		if(getCell("inv_quodiscount").getDouble() != 0) {
    			ppj.addBottomField("hdr_total","Less Discount:",0,-10);
    			ppj.addBottomField("val_total",getCell("inv_quodiscount").getColumnDisplayString(),0,-10);
    			ppj.addBottomField("hdr_total","Net Total:",0,10);
    			ppj.addBottomField("val_total",getCell("inv_grnettotal").getColumnDisplayString(),0,10);
    		} else {
    			ppj.addBottomField("hdr_total","Total:");
    			ppj.addBottomField("val_total",getCell("inv_grnettotal").getColumnDisplayString());
    		}
    		ppj.addBottomField("val_remark","Quotation is valid within 30 days from the date of issue");
    		
    		return(ppj.toPdfStream(os, getSessionHelper()));
		} catch (Exception ex) {
			return(new ReturnMsg(false,"Fail Reason Unknown"));
		}
	}
	
	@Override
	protected void createColumnCells(BiCellCollection p_col) {
		super.createColumnCells(p_col);
		Cell c = p_col.getCell("inv_delidate");
		c.setValidation(new CellValidation(){

			@Override
			public boolean validate(Cell p_cell, Object p_value) {
				// TODO Auto-generated method stub
				Date cd = getCell("inv_date").getDate();
				Date dd;
				if(p_value instanceof Date) {
					dd = (Date) p_value;
				} else if(p_value instanceof String) {
					if(((String) p_value).length() <= 8) {
						dd = DateUtil.getDate((String) p_value);
					} else {
						dd = DateUtil.dateTimeStrToDate((String) p_value);
					}
				} else dd = null;
				if (cd == null || dd == null) {
					UniLog.log1("cd or dd is null");
					return false;
				}
				if(cd.after(DateUtil.minDate) && dd.after(DateUtil.minDate)) {
					if(cd.after(dd)) return(false);
				}
				return true;
			}

			@Override
			public String getErrMsg() {
				// TODO Auto-generated method stub
				return ("Delivery Date Must Greated Than Create Date");
			}
			
			}
		);
	}
	
}
