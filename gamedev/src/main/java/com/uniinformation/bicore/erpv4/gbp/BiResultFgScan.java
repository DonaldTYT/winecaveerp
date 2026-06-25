package com.uniinformation.bicore.erpv4.gbp;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.RecSync;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultFgScan extends BiResultErpv4 {

	public BiResultFgScan(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	ReturnMsg updateFgMaster( ) {
		try {
			SelectUtil su = getSelectUtil();
			TableRec tr = su.getQueryResult("select count(*) cnt,sum(fgs_qty) tot from fgscan where fgs_mrg = " + getCellInt("fgs_mrg"));
			tr.setRecPointer(0);
			su.executeUpdate("update fgmaster set fgm_finqty = ? , fgm_finbox = ? where fgm_rg = ? ", 
						new Wherecl()
							.appendArgument(tr.getFieldInt("tot"))
							.appendArgument(tr.getFieldInt("cnt"))
							.appendArgument(getCellInt("fgs_mrg"))
							);
			
			BiView v = sh.getBiSchema().getViewByName("gbp.FgMaster");
			BiResult mbr = v.newBiResult(sh.getLoginId(), null, null, sh);	
			mbr.clearCurrentRec();
			mbr.addCustomCondition("fgm_rg =" + getCellInt("fgs_mrg"));
			mbr.query();
			if(mbr.getRowCount() == 1) {
				mbr.loadOneRecV(0);
				RecSync.updateOneRecord(sh.getAgent(), mbr.getView().getName(), mbr.getCurrentCollection());
			}
			
			return(ReturnMsg.defaultOk);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		
	}
	/* use this for post process validataion after add/update to database */
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtn = updateFgMaster();
		if(!rtn.getStatus()) return(rtn);
		return(super.biAfterAddUpdateCurrent(col, isUpdate));
	}
	
	protected ReturnMsg biAfterDeleteCurrent(CellCollection col) {
		ReturnMsg rtn = updateFgMaster();
		if(!rtn.getStatus()) return(rtn);
		return(super.biAfterDeleteCurrent(col));
	}	
}
