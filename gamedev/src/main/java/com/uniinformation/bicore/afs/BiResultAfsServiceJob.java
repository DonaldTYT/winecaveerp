package com.uniinformation.bicore.afs;

import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsServiceJob extends BiResult {
	public BiResultAfsServiceJob(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList,p_whereStr,p_sh);
		UniLog.log("BiResultAfsStockSet Used");
	}
	
	@Override
	protected void createColumnCells(BiCellCollection col)
	{	
		super.createColumnCells(col);
	}
	ReturnMsg checkBeforeAddUpdate() {
		//andrew230316 hotfix for afscn null exception
		if (getSubLink("AfsJobEngineer") == null) {
			UniLog.log("ignore validation");
			return null;
		}
		
		BiResult sl = getSubLink("AfsJobEngineer");
		if(sl.getRowCollectionList().size() <= 0) {
			return(new ReturnMsg(false,"Must Has At Least One Engineer Record"));
		}
		return(null);
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg rtnMsg = checkBeforeAddUpdate();
		if(rtnMsg != null) return(rtnMsg);
		return(super.biBeforeUpdateCurrent(col));
	}
	
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col)
	{
		ReturnMsg rtnMsg = checkBeforeAddUpdate();
		if(rtnMsg != null) return(rtnMsg);
		rtnMsg = super.biBeforeAddCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			if(col.getCell("svjob_odrg").getInt() == 0) {
				int cc = getView().getSchema().getRg(this,"", 52015);
//				su.getQueryResult("select ", p_wherecl)
				/*
				TableRec tr = getView().getTable("sv_order").newTableRec();
				tr.addRecord();
				tr.setField("svord_rg", cc);
				tr.setField("svord_ordercode", "SV"+cc);
				tr.setField("svord_calldate", getCell("svord_calldate").getDate());
				tr.setField("svord_sertype", getCell("svord_sertype").getString());
				tr.setField("svord_status", getCell("svord_status").getString());
				*/
				
//				su.executeUpdate("insert into sv_order(svord_rg,svord_ordercode,svord_calldate,svord_sertype,svord_status) values (?,?,?,?,?)", 
//		                  new Wherecl()
//							.appendArgument(cc)
//							.appendArgument("SV"+cc)
//							.appendArgument(DateUtil.toSqlDate(getCell("svord_calldate").getDate()))
//							.appendArgument(getCell("svord_sertype").getString())
//							.appendArgument(getCell("svord_status").getString())
//							);
				boolean ae = isActionEnabled();
				setActionEnabled (false);
				getCell("svjob_odrg").set(cc);
				//  GYYnnn<Engineer Initisl>
//				getCell("svjob_jobcode").set(String.format("G%s%05dW", cc));
				
				{
				java.util.Date d = DateUtil.today();
				String s = "";
				String ds = DateUtil.toDateString(d, "yymm");
				int nextidx = 1;
				TableRec tr = su.getQueryResult(
						"select svjob_jobcode from sv_job where svjob_jobcode matches '" + "G" + ds + "*' order by svjob_jobcode desc",null);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					s = tr.getField("svjob_jobcode").toString();
					String ss = StringUtil.strpart(s, 5, 3);
					nextidx = Integer.parseInt(ss) + 1;
				}
				String usr = su.getLoginId();
				tr = su.getQueryResult(
						"select svegr_empid from sv_engineer where svegr_code = '" + usr + "'",null);
				String egrsuffix;
				if(tr.getRecordCount() > 0) egrsuffix = (String) tr.getField("svegr_empid"); else egrsuffix = "";
				getCell("svjob_jobcode").set(String.format("G%s%03d%s", ds, nextidx,egrsuffix.trim()));
				}
				
				
				setActionEnabled(ae);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,-1,ex.getMessage()));
		}
		return(rtnMsg);
	}
	
	public void updateWorkTime() throws CellException {
		//andrew230316 hotfix for afscn null exception
		if (getSubLink("AfsJobEngineer") == null) {
			UniLog.log("ignore update worktime");
			return;
		}
		
		getCell("svjob_worktime").set(getSubLink("AfsJobEngineer").sumDouble("svjobegr_worktime"));
	}

	public String makeServiceMachineName(int p_mcrg,String p_modelno,String p_serialno,String p_series,String p_longname) {
		if(StringUtils.isBlank(p_modelno)) {
			if(!StringUtils.isBlank(p_series)) {
				return(String.format("%4d %s %s",p_mcrg,p_series,p_serialno));
			} else {
				return(String.format("%4d %s %s",p_mcrg,p_longname,p_serialno));
			}
		} else {
			return(String.format("%4d %s %s",p_mcrg,p_modelno,p_serialno));
		}
	}
	/*
	if(svmc_irg == 0,sprintf('%s (Series)',stmcm_name),
			if(isblank(st_iname_real),st_icode,st_iname_real))
			*/
	
	enum FuncName { 
		FUNC_serviceMachineName, 
		NOT_DEFINED }	
	
	class AfsServiceCellCollection extends BiCellCollection {
		public AfsServiceCellCollection(BiCellCollection p_col, BiResult p_br) {
			super(p_col, p_br);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		public Object evalFunction(String p_fname,Vector p_args) throws Exception {
			FuncName funcName = checkAndGetFuncNameCache(p_fname,FuncName.NOT_DEFINED);
			switch (funcName){
			case FUNC_serviceMachineName: {
				int mcrg = Cell.objectToInt(p_args.get(0));
				String modelno = p_args.get(1).toString();
				String serialno = p_args.get(2).toString();
				String series = p_args.get(3).toString();
				String longname = p_args.get(4).toString();
				return(makeServiceMachineName(mcrg,modelno,serialno,series,longname));
			}
			}
			return(super.evalFunction(p_fname,p_args) );
		}
	}

	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new AfsServiceCellCollection(p_parent,BiResultAfsServiceJob.this));
	}
}
