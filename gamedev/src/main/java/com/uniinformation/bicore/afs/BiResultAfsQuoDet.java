package com.uniinformation.bicore.afs;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
//import com.uniinformation.bicore.erp.BiResultStmovOM;
import com.uniinformation.bicore.erpv4.BiResultQuoDet;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.cell.CellValueMapper;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
//import com.uniinformation.zkbi.ZkBiCellValueMapper;

public class BiResultAfsQuoDet extends BiResultQuoDet {
	public BiResultAfsQuoDet(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
	}
	@Override
	protected void createColumnCells(BiCellCollection col)
	{	
		super.createColumnCells(col);
		final CellCollection myCol  = col;
//		final Cell cellIrg = col.getCell("ind_irg");
//		cellIrg.addAction(
		CellValueAction priceChangeAction = new CellValueAction() {
			public void cellAction_onchange(Cell p_cell) throws CellException {
				double uPrice = myCol.getCell("ind_uprice").getDouble();
				if(uPrice > 0.0) {
					if(uPrice < myCol.getCell("ind_rprice").getDouble()) {
						myCol.getCell("ind_uprice").setHint("Price lower then Reference Price");
					}
				}
				/*
				if(myCol.getCell("ind_rprice").getDouble() > 0) {
					double netPrice = myCol.getCell("ind_netuprice").getDouble();
					if(netPrice < myCol.getCell("ind_rprice").getDouble()) {
						myCol.getCell("ind_uprice").setHint("Price lower then Reference Price");
					}
				}
				*/
			}
			public void cellAction_onfree() {
			}
		};
		if(myCol.testCell("vd_priceclass") == null) {
		myCol.getCell("ind_uprice").addAction(priceChangeAction);
		myCol.getCell("ind_irg").addAction(
				new CellValueAction() {
					public void cellAction_onchange(Cell p_cell) {
						UniLog.log("HAHA181203 default salleing price");
						try {
//							if(getSelectUtil() == null) {
//								UniLog.log("HAHA181203 not in transaction , skipped");
//								myCol.getCell("ind_rprice").set(0.0);
//								return;
//							}
							TableRec tr;
							String occy=null,sccy=null;
							double oprice=0.0,sprice=0.0;
							double markup = 0.0;
							double exrate = 0.0;
							occy = myCol.getCell("inv_cid").getString();
							if(occy.trim().equals("")) {
								UniLog.log("HAHA181203 order ccy not set, skipped");
								myCol.getCell("ind_rprice").set(0.0);
								return;
							}
							tr = getSelectUtil().getQueryResult("select * from stock where st_irg = " + myCol.getCell("ind_irg").getInt(),null);
							if(tr.getRecordCount() <= 0) {
								UniLog.log("HAHA181203 cannot select stock record, skipped");
								myCol.getCell("ind_rprice").set(0.0);
								return;
							}
							tr.setRecPointer(0);
							sprice = (Double) tr.getField("st_standardprice");
							sccy = (String) tr.getField("st_standardcur");
							if(sccy == null || sccy.trim().equals("") || sprice <= 0.0) {
								markup = (Double) tr.getField("st_markup");
								if(markup <= 0.0) {
									UniLog.log("HAHA181203 stock record markup not set, skipped");
									myCol.getCell("ind_rprice").set(0.0);
									return;
								}
								sccy = (String) tr.getField("st_standardcostcur");
								sprice = (Double) tr.getField("st_standardcost");
								if(sccy == null || sccy.trim().equals("") || sprice <= 0.0) {
									tr = getSelectUtil().getQueryResult("select stmd_mrg,stmd_uprice,stmd_cur from stmovd where stmd_tdtype in("+Erpv4Config.STOCKIN_TDtypes+") and stmd_irg = "+ myCol.getCell("ind_irg").getInt() + " and stmd_uprice > 0 order by stmd_mrg desc", null);
									if(tr.getRecordCount() <= 0) {
										UniLog.log("HAHA181203 stock record no historical cost, skipped");
										myCol.getCell("ind_rprice").set(0.0);
										return;
									}
									tr.setRecPointer(0);
									UniLog.log("HAHA181203 Cost = " + tr.getField("stmd_cur") + " " + tr.getField("stmd_uprice"));
									sccy = (String) tr.getField("stmd_cur");
									sprice = (Double) tr.getField("stmd_uprice");
									if(sccy == null || sccy.trim().equals("") || sprice <= 0.0) {
										UniLog.log("HAHA181203 historical price incomplete , skipped");
										myCol.getCell("ind_rprice").set(0.0);
										return;
									}
								}
								sprice *= markup;
							}
							String bccy = Erpv4Config.getBaseCcy(sh,getCellString("inv_cocode"));
							tr = getSelectUtil().getQueryResult("select * from bxrate where bx_basecid = '"+bccy+"' and bx_cid = '" + sccy.trim() + "'",null);
							tr.setRecPointer(0);
							exrate = (Double) tr.getField("bx_xrate");
							tr = getSelectUtil().getQueryResult("select * from bxrate where bx_basecid = '"+bccy+"' and bx_cid = '" + occy.trim() + "'",null);
							tr.setRecPointer(0);
							exrate /= (Double) tr.getField("bx_xrate");
							oprice = sprice * exrate;
							UniLog.log("HAHA181203 selling price is "+sccy+":"+sprice + " = " + occy + ":" + oprice + " exrate = " + exrate);
							myCol.getCell("ind_rprice").set(Math.round(oprice));
						} catch (Exception ex) {
							UniLog.log(ex);
						}
					}
					public void cellAction_onfree() {
					}
				}
				);
		}
	}
}
