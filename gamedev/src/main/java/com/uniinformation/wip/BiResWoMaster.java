package com.uniinformation.wip;

import java.util.Vector;

import org.zkoss.zk.ui.Executions;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResWoMaster extends BiResult {
//	RpcClient rpc = null;
	public BiResWoMaster(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		UniLog.logm(this,"BiResWoMaster initialized");
//		SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
//		rpc = sessionHelper.getRpcClient();
	}
	
//	public boolean loadOneRec(int p_recidx,CellCollection col)
//	{
//		boolean OK = super.loadOneRec(p_recidx,col);
//		if(!OK) return(OK);
//		try {
//	
//			TableRec tr = null;
//			tr = su.getQueryResult(
//				"select wq_edname,wq_quantity from woquantity where wq_wocode = " + col.getCell("wowocode").getInt() 
//						,null);
//				String s = ""; 
//				int qqty = 0;
//				for(int i = 0;i<tr.getRecordCount();i++) {
//					tr.setRecPointer(i);
//					if(i > 0) s+= "/";
//					s += (String) tr.getField("wq_edname");
//					qqty += ((Integer)tr.getField("wq_quantity")).intValue();
//				}
//				col.getCell("wqcategory").set(s);
//				col.getCell("wqquantity").set(qqty);
//			} catch (Exception tex){
//				UniLog.log(tex);
//			}
//		return(OK);
//	}
	
	@Override 
	protected void afterLoadCollection(boolean is_fectch,BiCellCollection col)
	{
		try {
	
			TableRec tr = null;
			tr = su.getQueryResult(
				"select wq_edname,wq_quantity from woquantity where wq_wocode = " + col.getCell("wowocode").getInt() 
						,null);
				String s = ""; 
				int qqty = 0;
				for(int i = 0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					if(i > 0) s+= "/";
					s += (String) tr.getField("wq_edname");
					qqty += ((Integer)tr.getField("wq_quantity")).intValue();
				}
				col.getCell("wqcategory").set(s);
				col.getCell("wqquantity").set(qqty);
			} catch (Exception tex){
				UniLog.log(tex);
			}
	}
}
