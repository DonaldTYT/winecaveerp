package com.uniinformation.bicore.erpv4;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.tuple.Pair;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.prtdoc.PrtdocPerfJson;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

class QoDetailControl {
	int qorg;
	int qirg;
	Comparable item;
	double value;
	double consumed;
	public QoDetailControl(int p_qorg,int p_qirg,Comparable p_item) {
		qorg = p_qorg;
		qirg = p_qirg;
		item = p_item;
		consumed = 0.0;
	}
	public void qoDetailControlDeltaChange(Comparable p_position,double p_value) throws Exception {
		if(value + p_value < 0.0) {
			throw new Exception("Allocation Reduce to negative");
		}
		value += p_value;
	}
	public double getValue() {
		return(value);
	}
	public Object getItem() {
		return(item);
	}
	public Object getIrg() {
		return(qirg);
	}
}

public class BiResultPO extends BiResultStmov {
	protected String poType;
	Hashtable<Integer,List <QoDetailControl>> quotationHash;
	public BiResultPO (BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr,p_sh);

		stmdLinkName = "erpv4.PoDet";
		poType = "PO";
		if(getCell("stm_tolAmt") != null) {
			tolAmtCell = "stm_tolAmt";
			detAmtCell = "stmd_exprice";
		}
	}

	ReturnMsg doUpdatePoDet()
	{
		try {
			Vector <BiCellCollection> recs = getSubLinkResult(stmdLinkName);
			for(CellCollection col:recs) {
				col.getCell("stmd_cur").set(getCell("stm_cur").getString());
			}
			return(null);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
	}
	
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg;
//		try {
//			pcol.getCell("stm_ref1").set(newPoCode(su,pcol.getCell("stm_date").getDate(),poType));
//		} catch (CellException cex) {
//			UniLog.log(cex);
//			return(new ReturnMsg(false,cex.toString()));
//		}
		rtnMsg = super.biBeforeAddCurrent(pcol);
		if(rtnMsg == null || rtnMsg.getStatus()) {
			return(doUpdatePoDet());
		}
		return(rtnMsg);
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg;
		rtnMsg = super.biBeforeUpdateCurrent(pcol);
		if(rtnMsg == null || rtnMsg.getStatus()) {
			return(doUpdatePoDet());
		}
		return(rtnMsg);
	}	
	public String newPoCode(SelectUtil su,java.util.Date p_date,String p_prefix) {
		try {
			String s = null;
			java.util.Date d = p_date;
			
			String rgc = Erpv4Config.getString(getSessionHelper(), "rgcontrol_PO" );
			if(rgc != null && !rgc.trim().equals("")) {
				String cocode;
				if(getCell("stm_cocode") != null) {
					cocode = getCellString("stm_cocode");
				} else {
					cocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
				}
				return(BiResultErpv4.getCodeByRgControl(this,cocode,rgc,d));
			}			
			
			
			String ds = DateUtil.toDateString(d, "yymmdd");
			int nextidx = 1;
			TableRec tr = su.getQueryResult("select stm_ref1 from stmov where stm_ref1 regexp '^" + p_prefix + ds + ".*' order by stm_ref1 desc",null);
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				s = tr.getField("stm_ref1").toString();
				String ss = StringUtil.strpart(s, 11, -1);
				nextidx = Integer.parseInt(ss) + 1;
			}
			s = String.format("%s%s-%03d",p_prefix,ds, nextidx);
			return(s);
		} catch (Exception cex ) {
			UniLog.log(cex);
			return(null);
		}
	}	
//	@Override 
//	public void addExtraWhereStr(Wherecl p_where) {
//		super.addExtraWhereStr(p_where);
//		p_where.andUniop("stm_ref1", "like", poType+"%");
//	}
	
//	@Override
//	public ReturnMsg lockRecordForUpdate() {
//		ReturnMsg rtnMsg = super.lockRecordForUpdate();
//		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
//		if(getCell("stm_ref1").getString().equals("")) {
//			try {
//				if(!DateUtil.minDate.before(getCell("stm_date").getDate())) {
//					getCell("stm_date").set(DateUtil.today());
//				}
//				getCell("stm_ref1").set(newPoCode(su,getCell("stm_date").getDate(),poType));
//				getCell("stm_date").setMode(Cell.VMODE_DISPONLY);
//			} catch (CellException cex) {
//				UniLog.log(cex);
//				rollbackWork();
//				return(new ReturnMsg(false,"Error generation PO Number"));
//			}
//		}
//		return(rtnMsg);
//	}
	
	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		if(getCellString("stm_ref1").equals("")) {
			java.util.Date d = getCell("stm_date").getDate();
			if(d.after(DateUtil.minDate)) {
				try {
					getCell("stm_ref1").set(newPoCode(su,getCell("stm_date").getDate(),poType));
				} catch (CellException cex ) {
					UniLog.log(cex);
					return(new ReturnMsg(false,cex.toString()));
				}
			}
		}
		return(ReturnMsg.defaultOk);
	}

	public ReturnMsg printPO(OutputStream os) {
		BiResultQuotation ppr = null;
		ppr = (BiResultQuotation) getParent();
//		if(ppr == null) return(new ReturnMsg(false,"Error : no parent quotation record"));
		try {
    		String paperType = Erpv4Config.getString(getSessionHelper(),"InvPaperType");
    		String invDoc = Erpv4Config.getString(getSessionHelper(),"InvDocCode");
    		if(invDoc == null) invDoc = "GENINV01";
    		PrtdocJson ppj = PrtdocJson.newPrtdocJson(	
    				getCellString("stm_cocode"),
    				paperType,
    			    invDoc,
    			    "erpv4_printDocument"
    				) ;
    		ppj.setTrailerAtLastPageOnly(true);
    		ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
    		ppj.addHeaderField("doctitle","PURCHASE ORDER");
    		if(Erpv4Config.isMultiCompany(getSessionHelper())) {
    			String logo = Erpv4Config.getCoLogo(getSessionHelper(), getCellString("stm_cocode"));
    			if(logo != null && !logo.equals("")) {
    				ppj.addHeaderImage("logo", logo ,50,0,100,0);
    			}
    		} else {
    			if(Erpv4Config.getDefaultLogo(getSessionHelper()) != null) {
    				ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(getSessionHelper()) ,50,0,100,0);
    			}
    		}
    		ppj.addHeaderField("cvname",getCellString("vd_vname"));
    		ppj.addHeaderField("cvname",getCellString("vd_addr0"),0,20);
    		ppj.addHeaderField("cvname",getCellString("vd_addr1"),0,40);
    		ppj.addHeaderField("cvname",getCellString("vd_addr2"),0,60);
    		ppj.addHeaderField("clphone","Phone",0,0);
    		ppj.addHeaderField("cvphone",getCellString("vd_tel"),0,0);
    		ppj.addHeaderField("clphone","Fax",0,18);
    		ppj.addHeaderField("cvphone",getCellString("vd_fax"),0,18);
    		ppj.addHeaderField("clphone","Attn",0,36);
    		ppj.addHeaderField("cvphone",getCellString("vd_contact"),0,36);
    		ppj.addHeaderField("dflabel","P.O. #",0,0);
    		ppj.addHeaderField("dfvalue",getCellString("stm_ref1"),0,0);
    		ppj.addHeaderField("dflabel","Date",0,30);
    		ppj.addHeaderField("dfvalue", DateUtil.toDateString( getCell("stm_date").getDate(),"dd/mm/yyyy") ,0,30);
    		ppj.addHeaderField("dflabel","Page",0,60);
 
    		ppj.addDetailHeaderField("hdr_duedate","Due Date");
    		ppj.addDetailHeaderField("hdr_terms","TERMS.");
    		ppj.addDetailHeaderField("hdr_delivery","DELIVERY");
    		ppj.addDetailHeaderField("hdr_yourref","YOUR REF.");
    		ppj.addDetailHeaderField("hdr_ourref","OUR REF.");
    		ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
    		ppj.addDetailHeaderField("hdr_qty","QTY");

    		ppj.addDetailHeaderField("hdr_uprice","UNIT PRICE",0,-5);
    		ppj.addDetailHeaderField("hdr_uprice","("+ getCellString("stm_cur")+")",0,12);
    		
    		/*
   			if(getCellString("stm_module").equals("machine")) {
   				ppj.addDetailHeaderField("hdr_disc","DEPO",0,-5);
   			} else {
   				ppj.addDetailHeaderField("hdr_disc","DISC",0,-5);
   			}
    		ppj.addDetailHeaderField("hdr_disc","%",0,12);
    		*/
    		ppj.addDetailHeaderField("hdr_amount","AMOUNT",0,-5);
    		ppj.addDetailHeaderField("hdr_amount","("+ getCellString("stm_cur")+")",0,12);

//    		ppj.addDetailHeaderField("val_yourref",getCell("invh_pocode").getColumnDisplayString());
//    		ppj.addDetailHeaderField("val_duedate",DateUtil.toDateString( getCell("invh_duedate").getDate(),"dd/mm/yyyy") ,0,0);

    		Vector<BiCellCollection> v = null;
    		v = getSubLink(stmdLinkName).getRowCollectionList();
    		String indPrefix = "stmd_";

    		for(BiCellCollection c : v) {
    			ppj.addDetailRecord();
    			String s = "";
    			/*
    			if(c.testCell(indPrefix+"desc") != null)  {
    				s += c.getCellString(indPrefix+"desc");
    			}
    			*/
    			if(c.testCell(indPrefix+"subitem") != null) {
    				if(!c.testCell(indPrefix+"subitem").getBoolean()) {
    					if(c.testCell("stmcm_name") != null)  {
    						ppj.setBold(true);
//    						ppj.setUnderLine(true);
    						s += c.getCellString("stmcm_name");
    					}
    				}
    			}
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
    			/*
    			if(getCellString("stm_module").equals("machine")) {
    				double dp = c.getCellDouble(indPrefix+"iexprice1");
    				double amt  = c.getCellDouble(indPrefix+"exprice");
    				if(dp != 0.0 && amt != 0) {
    					int  dpi = (int) (dp * 100 / amt);
    					ppj.addDetailRecordField("discount", (""+dpi));
    				}
    			} else {
    				if(c.getCellInt(indPrefix+"discpercent") > 0) {
    					ppj.addDetailRecordField("discount", c.getCell(indPrefix+"discpercent").getString());
    				}
    			}
    			*/
    			if(c.getCell(indPrefix+"exprice").getDouble() != 0) {
    				ppj.addDetailRecordField("amount", c.getCell(indPrefix+"exprice").getString());
    			}
    		}

    		ppj.addDetailRecord();
    		if(!getCellString("stm_ref1").equals(""))
    			ppj.addDetailRecordField("description", "[Our Ref: " + getCell("stm_ref1").getString() + "]");
    		ppj.addBottomField("val_signco",Erpv4Config.getCoName(getSessionHelper(),getCellString("stm_cocode")));


    		int ofs = 0;
    		/*
    		if(getCellDouble("stm_discount") != 0) {
    			ppj.addBottomField("hdr_total","Less Discount:",0,ofs);
    			ppj.addBottomField("val_total",getCell("stm_discount").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    		}
    		ppj.addBottomField("hdr_total","Net Total:",0,ofs);
    		ppj.addBottomField("val_total",getCell("stm_nettotal").getColumnDisplayString(),0,ofs);
   			ofs += 20;
    		if(getCellDouble("stm_deposit") != 0) {
    			ppj.addBottomField("hdr_total","Less Deposit:",0,ofs);
    			ppj.addBottomField("val_total",getCell("stm_deposit").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    		}
   			*/
   			ppj.addBottomField("hdr_total","Net Total:",0,ofs);
   			ppj.addBottomField("val_total",getCell("stm_nettotal").getColumnDisplayString(),0,ofs);
   			ofs += 20;
   			String remark = null; 
			if(remark == null) remark = getCell("stm_shipagent").getString(); else remark += "\r" + getCell("stm_shipagent").getString();
			if(!getCellString("stm_shipname").equals("")) {
				remark += "\r"+ getCellString("stm_shipname");
			}
			if(!getCellString("invh_term").equals("")) {
				remark += "\r"+ getCellString("invh_term");
			}

			/*
				SelectUtil su = getSelectUtil();
				TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+getCellString("stm_cocode")+ "'",null);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					if(!remark.trim().equals("")) remark += "\r";
					if(getCellString("stm_module").equals("parts")) 
						remark += tr.getFieldString("co_payment2");
					else 
						remark += tr.getFieldString("co_payment");
				}
				*/
   			ppj.addBottomField("val_remark",remark,0,0,15,0);
    		return(ppj.toPdfStream(os, getSessionHelper()));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Fail Reason Unknown"));
		}	
		
	}
	
	Pair<Integer,Comparable> getNewOdrgFromQuotation(int p_invrg,int p_irg,double p_qty) {
		List<QoDetailControl> qdlist =  getQdListFromHash(p_invrg);
		for(QoDetailControl qdc : qdlist) {
			if(qdc.qirg == p_irg && qdc.value - qdc.consumed >= p_qty) {
				return(Pair.of(qdc.qorg, qdc.item));
			}
		}
		return(null);
	}
	
	@Override
	public void clearCurrentRec()
	{
		super.clearCurrentRec();
		quotationHash = new Hashtable<Integer,List<QoDetailControl>>();
	}
	
	QoDetailControl getQdDetailControlFromHash(int p_invrg,int p_odrg) throws Exception {
		List<QoDetailControl> qdlist = getQdListFromHash(p_invrg);
		for(QoDetailControl qd : qdlist) {
			if(qd.qorg == p_odrg) {
				return(qd);
			}
		}
		return(null);
	}
	
	protected void updateConsumedQtyToHash() {
			for( List<QoDetailControl> qdlist : quotationHash.values()) {
				for(QoDetailControl qdc : qdlist) {
					qdc.consumed = 0.0;
				}
			}
			
			for(BiCellCollection bc : getSubLink(getStmdLinkName()).getRowCollectionList()) {
				int invrg = bc.getCellInt("ind_rg");
				int qorg = bc.getCellInt("stmd_qorg");
				if(invrg > 0 && qorg > 0) {
					double qty = bc.getCellDouble("stmd_qty") + bc.getCellDouble("stmd_xqty");
					if(qty != 0) {
					try {
						QoDetailControl qd = getQdDetailControlFromHash(invrg,qorg);
						qd.consumed += qty;
					} catch (Exception ex) {
						UniLog.log(ex);
					}
					}
				}
			}
	}
	List<QoDetailControl> getQdListFromHash(int p_invrg) {
		List<QoDetailControl> qdlist = quotationHash.get(p_invrg);
		if(qdlist == null) {
			qdlist = new ArrayList<QoDetailControl>();
			try {
			TableRec tr;
			tr = getSelectUtil().getQueryResult("select ind_odrg,ind_irg,ind_itemno from quodet where ind_stqty > 0 and ind_rg = " + p_invrg);
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				int odrg = tr.getFieldInt("ind_odrg");
				int itemno = tr.getFieldInt("ind_itemno"); 
				int qirg = tr.getFieldInt("ind_irg"); 
				QoDetailControl qdc = new QoDetailControl(odrg,qirg,itemno);
				TableRec tr2 = getSelectUtil().getQueryResult("select * from qodetstatus where qdst_ostqty > 0 and qdst_qorg = " + odrg + " and qdst_qirg = " + qirg);
				for(int j=0;j<tr2.getRecordCount();j++) {
					tr2.setRecPointer(j);
					qdc.value += tr2.getFieldDouble("qdst_ostqty");
				}
				qdlist.add(qdc);
			}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			Collections.sort(qdlist, new Comparator<QoDetailControl>() {
				@Override
				public int compare(QoDetailControl o1, QoDetailControl o2) {
					return(o1.item.compareTo(o2.item));
				}
			}
			);
			quotationHash.put(p_invrg, qdlist);
		}
		return(qdlist);
	}
	
	protected void afterFetch() {
		super.afterFetch();
		quotationHash = new Hashtable<Integer,List<QoDetailControl>>();
		if(getCellString("stm_stauts").equals("Confirmed")) { 
			/* try { */
			for(BiCellCollection bc : getSubLink(getStmdLinkName()).getRowCollectionList()) {
				int invrg = bc.getCellInt("inv_rg");
				int qorg = bc.getCellInt("stmd_qorg");
				if(qorg > 0) {
					double qty = bc.getCellDouble("stmd_qty") + bc.getCellDouble("stmd_xqty");
					try {
						QoDetailControl qd = getQdDetailControlFromHash(invrg,qorg);
						qd.value += qty;
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
			}
			/*
			} catch (Exception ex) {
				
			}
			*/
		}
	}
}
