package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.poi.ExcelPoi;
import com.uniinformation.utils.poi.ExcelPoiFormula;
import com.uniinformation.webcore.SessionHelper;

public class BiResultErpv4GenMoDet extends BiResultErpv4 {
	BiResult cbr = null;
	Vector<BiColumn>vvv = null;
	public BiResultErpv4GenMoDet(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected String getExcelValidation(ExcelPoi jxf,BiColumn bc ,Vector<BiColumn> p_cols,BiCellCollection p_cl,int p_idx) {
		if(bc.getLabel().equals("st_iname")) {
			if(cbr != null) {
				int keyPos = -1;
				for(int i =0;i<vvv.size();i++) {
					BiColumn cbc = vvv.get(i);
					if(cbc.getLabel().equals("st_iname")) {
						keyPos = i;
					}
				}
				if(keyPos >= 0) {
					return(ExcelPoi.cellRangeToString("StockItem", 1 , cbr.getRowCount()+1 , keyPos, keyPos, true));
				}
			}
			
		}
		return(null);
	}
	
	@Override
	protected void beforeFormatExportExcel(ExcelPoi jxf,boolean p_forImport,Vector<BiColumn> cols) {
		if(!p_forImport) return;
		int orgidx = jxf.getCurrentSheetIndex();
		int idx = jxf.excel_cloneSheet(0,"StockItem");
		if(idx < 0) return;
		jxf.excel_useSheet(idx);
		
		if(cbr == null) {
			cbr = (BiResult) getView().getSchema().getViewByName(Erpv4Config.getStockViewId(sh)).newBiResult(getSessionHelper().getLoginId(),null, null, getSessionHelper());
		}
		cbr.clearCondition();
		cbr.clearOrderBy();
		cbr.addOrderByColumnList("st_iname",false);
		cbr.query();
		vvv = new Vector<BiColumn>();
		vvv.add(cbr.getColumnByLabel("st_iname"));
		vvv.add(cbr.getColumnByLabel("st_icode"));
		vvv.add(cbr.getColumnByLabel("st_unit"));
		int[] colt = cbr.formatExportExcel (
   					jxf,
   					null
   					, null,
   					false
   					, vvv
   					, null
			);
		int xr = 0;
		for(int i=0;i<cbr.getRowCount();i++) {
   			xr = cbr.putOneRowToExceli(
					i,
					null,
					null,
					vvv,
					colt,
					jxf,
					xr,
					false,
					p_forImport,
					null
					
			);
		}
    	cbr.postProcessExportExcel(jxf,false,vvv,false);
		jxf.excel_useSheet(orgidx);
	}
	
	
	@Override
	protected ExcelPoiFormula getExcelFormula(ExcelPoi jxf,BiColumn bc ,int p_row,Vector<BiColumn> p_cols) {
		if(cbr == null) return(null);
		String bcl = bc.getLabel();
		
		if(bcl.equals("st_unit"))  {
			return(
					cbr.makeExcelVlookup(p_row,p_cols.indexOf(getColumnByLabel("st_iname")),"StockItem",vvv,"st_iname","st_unit")
				);
		}
		if(bcl.equals("st_icode"))  {
			return(
					cbr.makeExcelVlookup(p_row,p_cols.indexOf(getColumnByLabel("st_iname")),"StockItem",vvv,"st_iname","st_icode")
				);
		}
		if(bcl.equals("stmd_exprice"))  {
			int col = p_cols.indexOf(bc);
			return(
					new ExcelPoiFormula(
								ExcelPoi.cellRangeToString(null, p_row, p_row, col-3, col-3, false)+"*"+
								ExcelPoi.cellRangeToString(null, p_row, p_row, col-1, col-1, false)
							)
				);
		}
		return(null);
	}
}
