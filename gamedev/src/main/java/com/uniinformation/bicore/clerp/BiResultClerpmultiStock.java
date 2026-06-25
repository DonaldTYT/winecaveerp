package com.uniinformation.bicore.clerp;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultStock;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultClerpmultiStock extends BiResultStock{

	public BiResultClerpmultiStock(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void afterFetch() {
		super.afterFetch();
		try {
			getCell("st_iname").setMode(Cell.VMODE_NORMAL);
			getCell("st_einame").setMode(Cell.VMODE_NORMAL);
			getCell("st_mszrange").setMode(Cell.VMODE_NORMAL);
			getCell("st_oicode").setMode(Cell.VMODE_NORMAL);
			getCell("st_barcode").setMode(Cell.VMODE_NORMAL);
			getCell("mt_tpname").setMode(Cell.VMODE_NORMAL);
			getCell("stu_desc").setMode(Cell.VMODE_NORMAL);
			getCell("st_remark").setMode(Cell.VMODE_NORMAL);
			if(Erpv4Config.getDefaultCoCode(sh).equals("002")) {
				int stirg = getCellInt("st_stirg");
				if(stirg == 0) {
					try {
						TableRec tr = getSelectUtil().getQueryResult("select * from costock where st_cocode = ? and st_lcrg = ? and st_stirg = ?",
								new Wherecl()
									.appendArgument("001")
									.appendArgument("64")
									.appendArgument(getCellInt("st_irg"))
							);
						if(tr.getRecordCount() == 1) {
							tr.setRecPointer(0);
							getCell("st_price1").set(tr.getFieldDouble("st_price1"));
							getCell("st_price2").set(tr.getFieldDouble("st_price2"));
							getCell("st_price3").set(tr.getFieldDouble("st_price3"));
						}
					} catch (Exception ex) {
						UniLog.log("ex");
					}
				}
			}
		} catch (CellException cex) {
			UniLog.log(cex);
		}
	}
	@Override
	public String getColumnDisplayClass(ColumnCell p_cell) {
//		if((!p_cell.getCellLabel().equals("wfmjt_state")) 
//			/* && (!p_cell.getCellLabel().equals("wfmj_updtime")) */) return(null);
//		Date d = getCell("wfmjt_updtime").getDate();
//		long dd = new Date().getTime();
//		dd -= d.getTime();
//		if(dd < 60000) {
//		} else {
//			return(null);
//		}
		
		if(p_cell.getCellLabel().equals("stl1_stockqty")) {
			double d0 = getCellDouble("stl1_stockqty");
			double d1 = getCellDouble("st_lowmark");
			if(d0 < 0 || (d1 > 0 && d0 < d1)) {
				return("textInRed");
			}
		}
		return(super.getColumnDisplayClass(p_cell));
	}

	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeUpdateCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		int lcrg = col.getCellInt("st_lcrg");
		if(lcrg == 64 /* HQ location */) {
			SelectUtil su = getSelectUtil();
			try {
				su.executeUpdate("update costock set st_price1 = ? , st_price2 = ? , st_price3 = ? where st_lcrg not in (64,65,56,59,60,66,67) and st_stirg = "+getCellInt("st_irg"), 
						new Wherecl()
							.appendArgument(col.getCellDouble("st_price1"))
							.appendArgument(col.getCellDouble("st_price2"))
							.appendArgument(col.getCellDouble("st_price3"))
						);
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,ex.toString()));
			}
		}
		return(ReturnMsg.defaultOk);
	}
	
}
