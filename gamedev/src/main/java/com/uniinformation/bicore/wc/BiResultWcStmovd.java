package com.uniinformation.bicore.wc;

import java.util.Vector;

import org.zkoss.zk.ui.Executions;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultWcStmovd extends BiResult {
//	RpcClient rpc = null;
	double f = 0.0;
	public BiResultWcStmovd(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr,p_sh);
		UniLog.log("erp.BiResultWcStmovd used");
	}
	
//	@Override
//	protected boolean loadOneRec(int p_recidx,CellCollection col)
//	{
//		boolean b = super.loadOneRec(p_recidx, col);
//		if(b) {
//			try {
//			String stmd_tdtype = col.getCell("stmd_tdtype").getString();
//			String or_cocode = col.getCell("or_cocode").getString();
//			col.getCell("stmd_pcost").set(0.0);
//			col.getCell("stmd_sprice").set(0.0);
//			col.getCell("stmd_stkqty").set(0.0);
////			col.getCell("stmd_pcost").set(f);
//			f += 1.0;
//			if(stmd_tdtype.equals("MO")) {
//				col.getCell("stmd_sprice").set(
//								col.getCell("stmd_exprice1").getDouble() /
//								col.getCell("stmd_qty").getDouble() 
//							);
//				if(or_cocode.equals("WINECAVE")) {
//					col.getCell("stmd_stkqty").set(col.getCell("stmd_qty").getDouble());
//					if(rpc == null) {
//						SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
//						rpc = sessionHelper.getRpcClient();
//					}
//					Value v = rpc.callSegment("newstcost_getuprice", 
//								new VectorUtil()
//									.addElement(col.getCell("stmd_irg").getInt())
//									.addElement(col.getCell("stmd_org").getInt())
//									.addElement(new java.util.Date())
//									.toVector()
//							);
//					col.getCell("stmd_pcost").set(v.toDouble());
//				} else {
//					col.getCell("stmd_pcost").set(col.getCell("stmd_fref1").getDouble());
//				}
//			}
//			if(stmd_tdtype.equals("PD") || stmd_tdtype.equals("MI") ) {
//				if(or_cocode.equals("WINECAVE")) {
//					col.getCell("stmd_stkqty").set(col.getCell("stmd_qty").getDouble());
//					col.getCell("stmd_pcost").set(
//								col.getCell("stmd_exprice1").getDouble() /
//								col.getCell("stmd_qty").getDouble() 
//							);
//				}
//			}
//			} catch(CellException cex) {
//				UniLog.log(cex);
//			}
//		}
//		return(b);
//	}
	
	@Override
	protected void afterLoadCollection(boolean is_fetch,BiCellCollection col)
	{
			try {
			String stmd_tdtype = col.getCell("stmd_tdtype").getString();
			String or_cocode = col.getCell("or_cocode").getString();
			col.getCell("stmd_pcost").set(0.0);
			col.getCell("stmd_sprice").set(0.0);
			col.getCell("stmd_stkqty").set(0.0);
//			col.getCell("stmd_pcost").set(f);
			f += 1.0;
			if(stmd_tdtype.equals("MO")) {
				col.getCell("stmd_sprice").set(
								col.getCell("stmd_exprice1").getDouble() /
								col.getCell("stmd_qty").getDouble() 
							);
				if(or_cocode.equals("WINECAVE")) {
					col.getCell("stmd_stkqty").set(col.getCell("stmd_qty").getDouble());
					RpcClient rpc = null;
					if(rpc == null) {
						SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
						rpc = sessionHelper.getRpcClient();
					}
					Value v = rpc.callSegment("newstcost_getuprice", 
								new VectorUtil()
									.addElement(col.getCell("stmd_irg").getInt())
									.addElement(col.getCell("stmd_org").getInt())
									.addElement(new java.util.Date())
									.toVector()
							);
					rpc.close();
					col.getCell("stmd_pcost").set(v.toDouble());
				} else {
					col.getCell("stmd_pcost").set(col.getCell("stmd_fref1").getDouble());
				}
			}
			if(stmd_tdtype.equals("PD") || stmd_tdtype.equals("MI") ) {
				if(or_cocode.equals("WINECAVE")) {
					col.getCell("stmd_stkqty").set(col.getCell("stmd_qty").getDouble());
					col.getCell("stmd_pcost").set(
								col.getCell("stmd_exprice1").getDouble() /
								col.getCell("stmd_qty").getDouble() 
							);
				}
			}
			} catch(CellException cex) {
				UniLog.log(cex);
			}
	}
	
}
