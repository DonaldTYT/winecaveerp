package com.uniinformation.bicore.vincero;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultFtrStrategy extends BiResult {
	protected String[] ccys = {"USD","EUR","JPY","GBP","CHF","CAD","AUD","NZD"};
	protected String subLinkName = "vincero.FtrStrategyDetail";
	protected String columnPrefix = "ftrs";

	public BiResultFtrStrategy(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	
	void addOneCcyRecord(String p_ccy) throws CellException {
		BiResult ftrdBr = getSubLink(subLinkName);
		CellCollection col = ftrdBr.newRowCollection();
		ReturnMsg rtn = ftrdBr.addSubRecord(col, -1 ,"");
		col.getCell(columnPrefix+"d_ccy").set(p_ccy);
	}
	public void addCcyRecords() throws CellException {
		for(String ccy : ccys) {
			addOneCcyRecord(ccy);
		}
	}
	
	public BiResult getDetailLink() {
		return(getSubLink(subLinkName));
	}
			
}
