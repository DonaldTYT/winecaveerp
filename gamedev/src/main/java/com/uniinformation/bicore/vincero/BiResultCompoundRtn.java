package com.uniinformation.bicore.vincero;

import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.bischema.BiResultExcelSheet;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultCompoundRtn extends BiResultExcelSheet {

	public BiResultCompoundRtn(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		BiCellCollection col = super.createColumnCollection(p_parent);
		col.addCell("cprtn_capital", new Cell(0.0));
		col.addCell("cprtn_winrate", new Cell(0.0));
		col.addCell("cprtn_rewardratio", new Cell(0.0));
		col.addCell("cprtn_riskpertrade", new Cell(0.0));
		col.addCell("cprtn_wincount", new Cell(0));
		col.addCell("cprtn_lostcount", new Cell(0));
		return(col);
	}	
	
	void calPostFormula(String p_cellName,String p_formula) throws Exception {
		CellCollection col = getCurrentCollection();
		Cell c = col.getCell(p_cellName);
		com.uniinformation.utils.exprpar.Parser parser 
			= new com.uniinformation.utils.exprpar.Parser(ignoreCase,p_formula,col,col);
		Object oo = parser.evaluate();
		c.sync(oo);
	}
	@Override
	public int recal() throws Exception {
		int rtn = super.recal();
		/*
		CellCollection col = getCurrentCollection();
		Cell c = col.getCell("cprtn_wincount");
		com.uniinformation.utils.exprpar.Parser parser 
			= new com.uniinformation.utils.exprpar.Parser(ignoreCase,"excelCOUNTIFS([cprtn_result[0]:cprtn_result[]],'Win')",col,col);
		Object oo = parser.evaluate();
		c.sync(oo);
		*/
		calPostFormula("cprtn_wincount","excelCOUNTIFS([cprtn_result[0]:cprtn_result[]],'Win')");
		calPostFormula("cprtn_losscount","excelCOUNTIFS([cprtn_result[0]:cprtn_result[]],'Loss')");
		return(rtn);
	}
	
	/*
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		try {
			for(int i=0;i<100;i++) {
				addTrRecord(null,i);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		return(ReturnMsg.defaultOk);
	}
	*/
}
