package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStmovd extends BiResultErpv4 {
	public BiResultStmovd(BiResult p_parent, BiView p_view, SelectUtil p_su,
			Vector p_tabList, String p_whereStr, SessionHelper p_sh)
			throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}

//	class CellActionIrgChanged extends CellValueAction
//	{
//
//		@Override
//		public void cellAction_onchange(Cell p_value) throws CellException {
//			// TODO Auto-generated method stub
//			CellCollection ccol = ((ColumnCell) p_value).getCollection();
//			if(ccol.testCell("stmd_entryunit") != null) {
////				ccol.getCell("stmd_entryunit").setItemList( BiResultStock.getEntryUnits(su, ccol.getInt("stmd_irg")));
//				ccol.getCell("stmd_entryunit").set("");
//			}
//		}
//
//		@Override
//		public void cellAction_onfree() throws CellException {
//			// TODO Auto-generated method stub
//			
//		}
//	}
//	CellActionIrgChanged actionIrgChanged = new CellActionIrgChanged();
//	@Override
//	protected void createColumnCells(BiCellCollection p_col)
//	{
//		super.createColumnCells(p_col);
//		p_col.getCell("stmd_irg").addAction(actionIrgChanged);
//	}
	
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new Erpv4StmdCellCollection(p_parent,this));
	}
	@Override
	protected void afterLoadCollection(boolean p_isFetch,BiCellCollection p_cc){
		super.afterLoadCollection(p_isFetch, p_cc);
		if("Y".equals(Erpv4Config.getString(getSessionHelper(),p_cc.getCellString("stm_type")+"RequireApproval"))) {
			if(p_cc.getCell("stm_status").equals("Confirmed")) {
				try {
					if(!BiSchema.hasAccessRight(getSessionHelper(), "#cfm"+p_cc.getCellString("stm_type"))) {
						((BiCellCollection) p_cc).lock();
					}
				} catch(CellException cex) {
					UniLog.log(cex);
				}
			}
		}
	}	

	@Override
	protected void createColumnCells(final BiCellCollection col)
	{
		super.createColumnCells(col);	
		if(!getView().getName().equals("erpv4.MoGenericDet")) {
			if((getParent() != null) && ((BiResultStmov) getParent()).detAmtCell != null) {
				col.getCell(
						((BiResultStmov) getParent()).detAmtCell
						).addAction(((BiResultStmov) getParent()).stmCalAmount);
			}
		}
	}
	
	String paramsToRef4(Vector p_args) {
		String ss = null;
		for(Object oo : p_args) {
			if(ss == null) ss = oo.toString(); else ss += ","+oo.toString();
		}
		return(ss);
	}

	Object ref4ToParams(String p_ref4,int p_idx) {
		String ss[] = StringUtils.split(p_ref4,",");
		if(ss != null  && ss.length > p_idx) return(ss[p_idx]); else return(null);
	}
}
