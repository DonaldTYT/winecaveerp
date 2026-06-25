package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.SelectUtil;
import com.kyoko.common.*;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultMcType extends BiResultErpv4 {
	Vector<String> baseUnitList = null;
	public BiResultMcType(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		UniLog.log("BiResultMcType Used");
	}
	
//	void setMunitDUnit(CellCollection p_cc,String p_sizecell,String p_munitcell,String p_dunitcell) throws CellException {
//		String s = p_cc.getCell(p_sizecell).getString();
//		if(p_munitcell != null) p_cc.getCell(p_munitcell).set(getMunit(s));
//		if(p_dunitcell != null) p_cc.getCell(p_dunitcell).set(getDunit(s));
//	}
//	static protected void triggerMunitDUnit(CellCollection p_cc,String p_sizecell,String p_munitcell,String p_dunitcell) throws CellException {
//		final Cell c0 = p_cc.testCell(p_sizecell);
//		final Cell c1 = p_cc.testCell(p_munitcell);
//		final Cell c2 = p_cc.testCell(p_dunitcell);
//		if(c0 == null || c1 == null || c2 == null) return;
//		c0.addAction(
//				new CellValueAction() {
//
//					@Override
//					public void cellAction_onchange(Cell p_value)
//							throws CellException {
//						// TODO Auto-generated method stub
//						String s = p_value.getString();
//						c1.set(getMunit(s));
//						c2.set(getDunit(s));
//					}
//					@Override
//					public void cellAction_onfree() throws CellException {
//						// TODO Auto-generated method stub
//						
//					}
//				}
//		);
//	}
	static public String getMunit(String p_unitstr) {
		int cc;
		if(p_unitstr == null) return(null);
		cc = p_unitstr.indexOf("/");
		if(cc >= 0) {
			if(cc == 0) return(""); else return(StringUtil.strpart(p_unitstr, 0, cc));
		} else return("");
	}
	static public String getDunit(String p_unitstr) {
		int cc;
		if(p_unitstr == null) return(null);
		cc = p_unitstr.indexOf("/");
		if(cc >= 0) {
			return(StringUtil.strpart(p_unitstr, cc+1, -1).trim());
		} else return("");
	}
	
	@Override
	protected void createColumnCells(BiCellCollection p_col)
	{
		super.createColumnCells(p_col);
		if(baseUnitList == null) {
			try {
				SelectUtil sr = getSelectUtil();
				TableRec tr= sr.getQueryResult("select stu_unit from st_unit",null);
				baseUnitList = new Vector();
				baseUnitList.add("");
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					baseUnitList.add(tr.getFieldString("stu_unit"));
					
				}
			
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			getCell("mt_baseunit").setItemList(baseUnitList);
			getCell("mt_munit1").setItemList(baseUnitList);
			getCell("mt_munit2").setItemList(baseUnitList);
			getCell("mt_munit3").setItemList(baseUnitList);
			/*
			getCell("mt_dunit1").setItemList(uList);
			getCell("mt_dunit2").setItemList(uList);
			getCell("mt_dunit3").setItemList(uList);
			*/
		}
	}
	
}
