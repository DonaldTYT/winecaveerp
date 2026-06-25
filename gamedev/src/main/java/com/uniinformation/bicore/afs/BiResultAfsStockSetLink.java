package com.uniinformation.bicore.afs;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsStockSetLink extends BiResult {
	public BiResultAfsStockSetLink(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		UniLog.log("BiResultAfsStockSetLink Used");
	}
//	@Override
//	protected void createColumnCells(CellCollection col)
//	{	
//		super.createColumnCells(col);
//		BiResult tbr = getView().getSchema().getViewByName("AfsStockSet").newBiResult(su.getLoginId(), null,null);
//		tbr.query();
//		Vector <String> v = new Vector<String>();
//		for(int i = 0;i<tbr.getRowCount();i++) {
//			tbr.loadOneRecV(i);
//			v.add(tbr.getCell("stmcm_name").getString());
//		}
//		col.getCell("stmcm_name").setItemList(v);
//		tbr.close();
//		col.getCell("stmcm_name").addAction(new BiCellAction_stmcm_name());
//		
//		tbr = getView().getSchema().getViewByName("AfsStock").newBiResult(su.getLoginId(), null,null);
//		tbr.query();
//		v = new Vector<String>();
//		for(int i = 0;i<tbr.getRowCount();i++) {
//			tbr.loadOneRecV(i);
//			v.add(tbr.getCell("st_icode").getString());
//		}
//		col.getCell("st_icode").setItemList(v);
//		tbr.close();
//		col.getCell("st_icode").addAction(new BiCellAction_st_icode());
//		
//	}
//	
//	class BiCellAction_stmcm_name extends CellValueAction 
//	{
////		private boolean enabled = false;
//
//		@Override
//		public void cellAction_onchange(Cell p_value) throws CellException {
//			// TODO Auto-generated method stub
//			if(!actionEnabled) return;
//			if(!(p_value instanceof ColumnCell)) return;
//			BiResult tbr = getView().getSchema().getViewByName("AfsStockSet").newBiResult(su.getLoginId(), null,null);
//			tbr.addCondition(new VectorUtil().addElement(tbr.getView().getTable()).toVector(), "stmcm_name = '"+p_value.getString()+"'");
//			tbr.query();
//			Vector <String> v = new Vector<String>();
//			if(tbr.getRowCount() > 0) {
//				tbr.loadOneRecV(0);
//				getCell("mcfm_modelrg").set(tbr.getCell("stmcm_rg").getInt());
//				tbr.close();
//			} else {
//				tbr.close();
//				throw new CellException("Set Model Incorrect");
//			}
//		}
//
//		@Override
//		public void cellAction_onfree() throws CellException {
//			// TODO Auto-generated method stub
//		}
//		
//	}	
//	
//	class BiCellAction_st_icode extends CellValueAction 
//	{
////		private boolean enabled = false;
//
//		@Override
//		public void cellAction_onchange(Cell p_value) throws CellException {
//			// TODO Auto-generated method stub
//			if(!actionEnabled) return;
//			if(!(p_value instanceof ColumnCell)) return;
//			BiResult tbr = getView().getSchema().getViewByName("AfsStock").newBiResult(su.getLoginId(), null,null);
//			tbr.addCondition(new VectorUtil().addElement(tbr.getView().getTable()).toVector(), "st_icode = '"+p_value.getString()+"'");
//			tbr.query();
//			Vector <String> v = new Vector<String>();
//			if(tbr.getRowCount() > 0) {
//				tbr.loadOneRecV(0);
//				getCell("mcfm_mrg").set(tbr.getCell("st_irg").getInt());
//				tbr.close();
//			} else {
//				tbr.close();
//				throw new CellException("Item Code Incorrect");
//			}
//		}
//
//		@Override
//		public void cellAction_onfree() throws CellException {
//			// TODO Auto-generated method stub
//		}
//		
//	}	
	@Override
	protected TableRec getLookupTabTr(BiTable p_lookupTable, Wherecl p_wherecl,BiCellCollection p_col) throws Exception{	
		Wherecl wcl = p_wherecl;
		if(p_lookupTable.getName().equals("stock")) {
			if(wcl == null ) wcl = new Wherecl();
			wcl.appendString(" and st_mtype in('M','O') ").stripAnd();
		}
		return(super.getLookupTabTr(p_lookupTable, wcl,p_col));
	}	
}
