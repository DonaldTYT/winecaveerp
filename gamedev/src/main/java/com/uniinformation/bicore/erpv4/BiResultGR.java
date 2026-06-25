package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.UniqueStrings;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;


public class BiResultGR extends BiResultStmov {
	public BiResultGR(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		for(BiResult sr:getSubLinks()) {
			if(sr.getView().getTable().getName().equals("stmovd_bi")) {
				stmdLinkName = sr.getView().getName();
				
			}
		}
		extraStmds.add("stmdmi");
		UniLog.log("BiResultGR add extraStmd " + extraStmds.toString());
		if(getCell("stm_tolAmt") != null && stmdLinkName != null) {
			tolAmtCell = "stm_tolAmt";
			BiResult sr = getSubLink(stmdLinkName);
			if(sr.getColumnByLabel("stmd_exprice0") != null) {
				detAmtCell = "stmd_exprice0";
			} else {
				if(sr.getColumnByLabel("stmd_exprice") != null) {
					detAmtCell = "stmd_exprice";
				}
			}
		}
	}
	
	ReturnMsg updatePdStmdRef()
	{
			try {
				Vector <BiCellCollection> recs = getSubLinkResult(stmdLinkName);
				for(CellCollection col:recs) {
//					su.executeUpdate("update stmovd set stmd_ref = ? , stmd_fref1 = ? where stmd_tdtype in("+Erpv4Config.PURCHASE_TDtypes+") "
					su.executeUpdate("update stmovd set stmd_ref = ? , stmd_fref1 = ? where stmd_flag1 = 'Y' "
									+ " and stmd_org = " + col.getCell("stmd_org").getInt() 
									+ " and stmd_irg = " + col.getCell("stmd_irg").getInt(),
									new Wherecl()
										.appendArgument(col.getCell("orddet_ref").getString())
										.appendArgument(col.getCell("stmd_fref1").getDouble())
									);
					
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
		rtnMsg = super.biBeforeAddCurrent(pcol);
		if(rtnMsg == null || rtnMsg.getStatus()) {
			return(updatePdStmdRef());
		}
		return(rtnMsg);
	}	
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg;
		rtnMsg = super.biBeforeUpdateCurrent(pcol);
		if(rtnMsg == null || rtnMsg.getStatus()) {
			return(updatePdStmdRef());
		}
		return(rtnMsg);
	}	
	
	@Override
	protected void afterLoadCollection(boolean p_isFetch,BiCellCollection p_cc){
		if(p_cc.testCell("stm_custpo") != null) {
		try {
			TableRec tr = getSelectUtil().getQueryResult("select inv_pocode from stmovd,quodet,quotation  where"
				+ " stmd_mrg = " + p_cc.getCell("stm_mrg") 
				+ " and ind_odrg = stmd_qorg and inv_rg = ind_rg"
				, null);
			UniqueStrings us = new UniqueStrings(" / ");
			for(int i = 0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				us.add(tr.getFieldString("inv_pocode"));
			}
			String sss = us.toString();
			p_cc.getCell("stm_custpo").set(sss == null ? "" : sss);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		}
	}
	
	@Override
	protected ReturnMsg validateOneRow(CellCollection pcol,boolean isUpdate) {
		if(pcol.getCell("stm_ref1").isBlank()) {
		try {
			String cocode;
			if(pcol.testCell("stm_cocode") != null) {
				cocode = pcol.getCell("stm_cocode").getString();
			} else {
				cocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
			}
			pcol.getCell("stm_ref1").set(
						BiResultErpv4.getCodeByRgControl(this, cocode,"stmov_GR", pcol.getCell("stm_date").getDate())
						);
		} catch (Exception cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,cex.toString()));
		}
		}
		return(super.validateOneRow(pcol,isUpdate));
	}
}
