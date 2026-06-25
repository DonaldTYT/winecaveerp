package com.uniinformation.bicore.afs;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsStockSet extends BiResult {
	public BiResultAfsStockSet(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		UniLog.log("BiResultAfsStockSet Used");
	}
	@Override
	protected void createColumnCells(BiCellCollection col)
	{	
		super.createColumnCells(col);
		//BiResult tbr = getView().getSchema().getViewByName("AfsStockCat").newBiResult(su.getLoginId(), null,sh);
		BiResult tbr = getView().getSchema().getViewByName("AfsStockCat").newBiResult(su.getLoginId(), null,null,sh);
		tbr.query(true);
		Vector <String> v = new Vector<String>();
		for(int i = 0;i<tbr.getRowCount();i++) {
			tbr.loadOneRecV(i);
			v.add(tbr.getCell("mt_tpname").getString());
		}
		col.getCell("mt_tpname").setItemList(v);
		tbr.close();
		col.getCell("mt_tpname").addAction(new BiCellAction_mt_tpname());
	}
	
	class BiCellAction_mt_tpname extends CellValueAction 
	{
//		private boolean enabled = false;

		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			// TODO Auto-generated method stub
			if(!isActionEnabled()) return;
			if(!(p_value instanceof ColumnCell)) return;
			UniLog.log("BiCellAction_mt_tpname triggered");
			//BiResult tbr = getView().getSchema().getViewByName("AfsStockCat").newBiResult(su.getLoginId(), null,null);
			BiResult tbr = getView().getSchema().getViewByName("AfsStockCat").newBiResult(su.getLoginId(), null,null,sh);
			tbr.addCondition(new VectorUtil().addElement(tbr.getView().getTable()).toVector(), "mt_tpname = '"+p_value.getString()+"'");
			tbr.query(true);
			if(tbr.getRowCount() > 0) {
				tbr.loadOneRecV(0);
				getCell("stmcm_type").set(tbr.getCell("mt_tpcode").getInt());
				tbr.close();
			} else {
				tbr.close();
				throw new CellException("Category Incorrect");
			}
		}

		@Override
		public void cellAction_onfree() throws CellException {
			// TODO Auto-generated method stub
		}
		
	}
}
