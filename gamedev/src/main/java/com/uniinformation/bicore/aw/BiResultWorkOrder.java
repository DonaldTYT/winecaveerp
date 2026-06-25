package com.uniinformation.bicore.aw;

import java.util.Date;
import java.util.Vector;

import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiCoreRpcServlet;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultWorkOrder extends BiResultErpv4  implements BiCoreRpcServlet.BiRpcInterface {
	public BiResultWorkOrder(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		UniLog.log("BiResultWorkOrder");
	}
	void updatePresetStrs() {
		/*
		PresetStr.updatePresetStr(getSelectUtil(), this.getCurrentCollection(),"PACK", "jm_packing");
		PresetStr.updatePresetStr(getSelectUtil(), this.getCurrentCollection(),"DELI", "jm_delivery");
		BiResult sr = getSubLink("aw.WoMat");
		Vector<BiCellCollection> v = sr.getRowCollectionList();
		for(BiCellCollection cl:v) {
			PresetStr.updatePresetStr(getSelectUtil(), cl,"MU", "wm_matname");
			PresetStr.updatePresetStr(getSelectUtil(), cl,"MW", "wm_matwt");
			PresetStr.updatePresetStr(getSelectUtil(), cl,"MT", "wm_mattype");
			PresetStr.updatePresetStr(getSelectUtil(), cl,"MS", "wm_matsize");
			PresetStr.updatePresetStr(getSelectUtil(), cl,"MB", "wm_brand");
			PresetStr.updatePresetStr(getSelectUtil(), cl,"MV", "wm_vendor");
			PresetStr.updatePresetStr(getSelectUtil(), cl,"FSCT", "wm_fsc");
		}
		sr = getSubLink("aw.WoExt");
		v = sr.getRowCollectionList();
		for(BiCellCollection cl:v) {
			PresetStr.updatePresetStr(getSelectUtil(), cl,"MU", "wt_name");
			PresetStr.updatePresetStr(getSelectUtil(), cl,"MS", "wt_matsize");
			PresetStr.updatePresetStr(getSelectUtil(), cl,"MK", "wt_matcut");
			PresetStr.updatePresetStr(getSelectUtil(), cl,"MC", "wt_cutsize");
		}
		sr = getSubLink("aw.WoPrt");
		v = sr.getRowCollectionList();
		for(BiCellCollection cl:v) {
			PresetStr.updatePresetStr(getSelectUtil(), cl,"MU", "wp_name");
			PresetStr.updatePresetStr(getSelectUtil(), cl,"BT", "wp_imposition");
			PresetStr.updatePresetStr(getSelectUtil(), cl,"FOLD", "wp_fold");
			PresetStr.updatePresetStr(getSelectUtil(), cl,"PTR", "wp_machine");
		}
		sr = getSubLink("aw.WoFin");
		v = sr.getRowCollectionList();
		for(BiCellCollection cl:v) {
			PresetStr.updatePresetStr(getSelectUtil(), cl,"FV", "wf_vendor");
			PresetStr.updatePresetStr(getSelectUtil(), cl,"FT", "wf_name");
		}
		sr = getSubLink("aw.WoProd");
		v = sr.getRowCollectionList();
		for(BiCellCollection cl:v) {
			PresetStr.updatePresetStr(getSelectUtil(), cl,"MU", "wr_name");
		}
		*/
		BiResult sr;
		Vector<BiCellCollection> v;
		try {
			getCell("jm_updtime").set(new Date());
			sr = getSubLink("aw.WoQty");
			v = sr.getRowCollectionList();
			String ss= null;
			for(CellCollection cl:v) {
				if(ss == null) ss = ""; else ss += ",";
				ss += cl.getCell("wq_product").getString() + " " + 
						StringUtil.ftostr(cl.getCell("wq_qty").getDouble(),"###,##0").trim()
						+ " " + cl.getCell("wq_unit");
			}
			if(ss == null) ss = "";
			getCell("jm_qty").set(ss); 
		} catch (CellException ex) {
			UniLog.log(ex);
		}
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col)
	{
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		updatePresetStrs();
		return(rtnMsg);
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col)
	{
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		updatePresetStrs();
		return(rtnMsg);
	}
	
	class BiCellAction_syncMatType extends CellValueAction 
	{
		CellCollection col=null;
//		private boolean enabled = false;
		BiCellAction_syncMatType(CellCollection p_col) {
			col = p_col;
		}
		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			// TODO Auto-generated method stub
			if(!isActionEnabled()) return;
			BiResult sr = getSubLink("aw.WoMat");
			int idx = sr.getIndexByCollection(col);
			CellCollection scol = getSubLink("aw.WoExt").getRowCollectionV(idx);
			scol.getCell("wt_name").set(p_value.getString());
			scol = getSubLink("aw.WoPrt").getRowCollectionV(idx);
			scol.getCell("wp_name").set(p_value.getString());
			scol = getSubLink("aw.WoProd").getRowCollectionV(idx);
			scol.getCell("wr_name").set(p_value.getString());
		}
		@Override
		public void cellAction_onfree() throws CellException {
			// TODO Auto-generated method stub
		}
		
	}	
	class BiCellAction_syncMatSize extends CellValueAction 
	{
		CellCollection col=null;
//		private boolean enabled = false;
		BiCellAction_syncMatSize(CellCollection p_col) {
			col = p_col;
		}
		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			// TODO Auto-generated method stub
			if(!isActionEnabled()) return;
			BiResult sr = getSubLink("aw.WoMat");
			int idx = sr.getIndexByCollection(col);
			CellCollection scol = getSubLink("aw.WoExt").getRowCollectionV(idx);
			scol.getCell("wt_matsize").set(p_value.getString());
		}
		@Override
		public void cellAction_onfree() throws CellException {
			// TODO Auto-generated method stub
		}
		
	}	
	protected void setSyncMatType(ColumnCell p_cell) {
		p_cell.addAction(new BiCellAction_syncMatType(p_cell.getCollection()) );
	}
	protected void setSyncMatSize(ColumnCell p_cell) {
		p_cell.addAction(new BiCellAction_syncMatSize(p_cell.getCollection()) );
	}

	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col, boolean p_isUpdate) {
		UniLog.log("In Workorder afterAddUpdate Current");
		String st = col.getCell("inv_quostatus").getString();
		if(st.equals("Void") /* || st.equals("Confirmed") */) return(null);
		try {
			SelectUtil su = getSelectUtil();
			TableRec tr = su.getQueryResult("select count(*) cnt from invoice where inv_voidflag <> 'Y' and inv_quonum = '" + col.getCellString("inv_invno") + "'");
			if(tr.getRecordCount() > 0 && tr.getFieldInt("cnt") > 0) {
				return(null);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		BiResult quoBr = null;
		ReturnMsg rtn;
		quoBr = getView().getSchema().getViewByName("erpv4.QuotationG2").newBiResult(getSelectUtil(), sh.getLoginId(), null, null, sh);
		if(st.equals("Confirmed") ) {
//			quoBr = getView().getSchema().getViewByName("erpv4.QuotationG2").newBiResult(getSelectUtil(), sh.getLoginId(), null, null, sh);
			if(quoBr == null) return(new ReturnMsg(false,"cannot open Quotation View"));
			quoBr.clear();
			quoBr.addCustomCondition("inv_rg = " + col.getCell("inv_rg").getInt());
			quoBr.query(false,true);
			if(quoBr.getRowCount() != 1) {
				quoBr = null;
				return(new ReturnMsg(false,"Quotation not found or not unique"));
			}
			try {
				quoBr.loadOneRecV(0);
				quoBr.fetchOneRecV(0);
//				quoBr.beginWork();
				quoBr.getCell("inv_quostatus").set("New");
				rtn = quoBr.updateCurrent();
				if(rtn != null && !rtn.getStatus()) {
					return(rtn);
				}
			} catch (Exception ex) {
				UniLog.log(ex);
				quoBr = null;
				return(new ReturnMsg(false,ex.toString()));
			}
		}
		RpcClient rpc = getSelectUtil().getRpcClient();
   		Vector args = new Vector();
		args.add(col.getCell("inv_rg").getInt());
		args.add(col.getCell("jm_updtime").getInt());
  		Value val=null;
		try {
			val = rpc.callSegment(
				"erpv4_generate_quotation",
				args
			);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		if(quoBr != null) {
			quoBr.clear();
			quoBr.addCustomCondition("inv_rg = " + col.getCell("inv_rg").getInt());
			quoBr.query(false,true);
			if(quoBr.getRowCount() != 1) {
				quoBr = null;
				return(new ReturnMsg(false,"Quotation not found or not unique"));
			}
			try {
				quoBr.fetchOneRecV(0);
				quoBr.getCell("inv_quostatus").set("Confirmed");
				rtn = quoBr.updateCurrent();
				if(rtn != null && !rtn.getStatus()) {
					return(rtn);
				}
			} catch (Exception ex) {
				UniLog.log(ex);
				quoBr = null;
				return(new ReturnMsg(false,ex.toString()));
			}
		}
  		if(val != null && val.toString().startsWith("OK")) {
  			/*
  			String s = StringUtil.strpart(val.toString(), 4 , -1);
  			if(!s.trim().equals("")) {
  				return(GenbucketUtil.qoGenBucketCheckResult(s));
  			} else return(null);
  			*/
  			return(null);
  		} 
  		ReturnMsg msg;
  		if(val == null) {
  			UniLog.log("generate quotation fatal error : got null");
  			msg = new ReturnMsg(false, "Save Failed: Unknown Reason");
  		} else {
  			msg = new ReturnMsg(false, "Save Failed: " + val.toString().substring(4));
  			UniLog.log("generate quotation fatal error : got "+val.toString());
  		}
  		msg.setFatal(true);
		return(msg);
	}
	
	ReturnMsg newGenerateQuotationFromWorkOrder() {
		return(ReturnMsg.defaultOk);
	}
	protected ReturnMsg biAfterAddUpdateCurrentXX(BiCellCollection col, boolean p_isUpdate) {
		UniLog.log("In Workorder afterAddUpdate Current");
		String st = col.getCell("inv_quostatus").getString();
		if(st.equals("Void") /* || st.equals("Confirmed") */) return(null);
		return(newGenerateQuotationFromWorkOrder());
	}
	@Override
	public String biRpcCallSegment(String p_segName, String p_jsonstr) {
		try {
		if(p_segName.equals("generateQuotation")) {
			boolean inBeginWork;
			JSONObject jo = new JSONObject(p_jsonstr);
			int invrg = jo.getInt("invrg");
			int ctime = jo.getInt("ctime");
			inBeginWork = inBeginWork();
			if(!inBeginWork) beginWork();
			RpcClient rpc = getSelectUtil().getRpcClient();
			Vector args = new Vector();
			args.add(invrg);
			args.add(ctime);
			Value val = rpc.callSegment(
					"erpv4_generate_quotation",
					args
					);
			if(!inBeginWork) commitWork();
			return(val == null ? "FAIL" : val.toString());
		}
		} catch (Exception ex) {
			UniLog .log(ex);
		}	
		
		return null;
	}
	
}
