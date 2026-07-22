package com.uniinformation.bicore.erpv4;

import java.util.Vector;

//import org.zkoss.zss.api.model.CellData.CellType;

import com.kyoko.common.DateUtil;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;

public class Erpv4StmdCellCollection extends Erpv4BaseCellCollection {
	Erpv4StockAttribute stattr = null;
//	CellActionIrgChanged actionIrgChanged = null;
	CellValueAction actionIrgChanged = null;
	CellValueAction actionEntryUnitChanged = null;

	private enum FuncName { FUNC_getRprice, FUNC_getRcost,FUNC_ref4ToParams,FUNC_paramsToRef4,NOT_DEFINED }
	
	@Override
	public Object evalFunction(String p_fname,Vector p_args) throws Exception {
		FuncName funcName = FuncName.NOT_DEFINED;
		try {
			funcName = FuncName.valueOf("FUNC_"+p_fname);
		}
		catch(Exception ex) {
			//remark: if enum not exist, will got exception here.
		}
		switch (funcName){
		case FUNC_getRprice: {
				double dirg = (Double) p_args.get(0);
				if(p_args.get(1) == null) return(0.0);
				int irg = (int) dirg;
				if(irg <= 0) return(0.0);
				if(stattr == null || stattr.irg != irg) return(0.0);
				java.util.Date pDate;
				if(p_args.size() > 2) {
					pDate = (java.util.Date) p_args.get(2);
				} else {
					pDate = DateUtil.zeroDate;
				}
				String s = (String) p_args.get(1);
						if(s.trim().equals("")) {
//							return(0.0);
							return(new com.uniinformation.cell.IgnoreValue());
						} else { 
							return(stattr.prices.get(s));
						}
			}
		case FUNC_getRcost: {
				double dirg = (Double) p_args.get(0);

//				if(p_args.get(1) == null) return(0.0);
				int irg = (int) dirg;
				if(irg <= 0) return(0.0);
				if( getCellString("stm_module").equals("stadj")
					|| getCellString("stm_module").equals("cotfr")) {
					dirg = (Double) p_args.get(1);
					int org = (int) dirg;
					if(org <= 0) return(0.0);
					java.util.Date d;
					d = (java.util.Date) p_args.get(2);
					if(! d.after(DateUtil.minDate)) return(0.0);
					double avCost = CostCalculation.getWaCost(br.getSessionHelper(), irg, org, d);
					return(avCost);
				}
				if(stattr == null || stattr.irg != irg) return(0.0);
				return(stattr.cost);
			}
		case FUNC_ref4ToParams: {
			String ref4 = p_args.get(0).toString();
			int idx = Cell.objectToInt(p_args.get(1));
			return(((BiResultStmovd) br).ref4ToParams(ref4,idx));
		}
		case FUNC_paramsToRef4: {
			return(((BiResultStmovd) br).paramsToRef4(p_args));
		}
		}
		return(super.evalFunction(p_fname,p_args) );
	}

	public Erpv4StockAttribute getStockAttribute() {
		return(stattr);
	}
	
	public Erpv4StmdCellCollection(BiCellCollection p_parent, BiResultErpv4 p_br) {
		super(p_parent, p_br);
		// TODO Auto-generated constructor stub
		actionIrgChanged = new CellValueAction() {

			@Override
			public void cellAction_onchange(Cell p_value) throws CellException {
				// TODO Auto-generated method stub
				int irg = getInt("stmd_irg");
				if(irg > 0) {
					if(stattr == null || stattr.irg != irg) {
						stattr = Erpv4StockAttribute.getStockAttribute(getCell("stm_date").getDate(),br.getSessionHelper(),br.getSelectUtil(), irg);
					}
				} else {
					stattr = null;
				}
				if(testCell("stmd_entryunit") != null) {
					if(stattr != null) {
						getCell("stmd_entryunit").setItemList( stattr.allUnits);
						if(stattr.allUnits.size() != 1 ) {
							getCell("stmd_entryunit").set("");
							getCell("stmd_eratio").set(0);
						} else {
							getCell("stmd_entryunit").set(stattr.allUnits.get(0));
							getCell("stmd_eratio").set(stattr.ratios.get(stattr.allUnits.get(0)));
						}
					} else {
						getCell("stmd_entryunit").setItemList((Vector) null);
						getCell("stmd_entryunit").set("");
						getCell("stmd_eratio").set(0);
					}
				}				
				if(stattr != null) {
					/*
					if(testCell("vd_priceclass") != null && testCell("stmd_rprice") != null)  {
						String s = getCell("vd_priceclass").getString();
						if(s.trim().equals("")) {
							getCell("stmd_rprice").set(0.0);
						} else { 
							getCell("stmd_rprice").set(stattr.prices.get(s));
						}
					}
					*/
					if(testCell("stmd_rprice") != null)  {
							getCell("stmd_rprice").eval();
					}
					if(testCell("st_standardcost") != null)  {
							getCell("st_standardcost").eval();
					}
					if(testCell("stmd_uprice") != null)  {
							getCell("stmd_uprice").resetValue();
					}
				}
				String useListForSerialNo = Erpv4Config.getString(br.getSessionHelper(), "UseListForSerialNo");
				if(useListForSerialNo != null && useListForSerialNo.equals("Y")) {
				if(testCell("stmd_ref4") != null ) {

					getCell("stmd_ref4").resetValue();
					try {
						TableRec tr;
						/*
						tr = br.getSelectUtil().getQueryResult(
								"select distinct stsn_ref4 from stockserial where stsn_irg = " + getInt("stmd_irg") + " and stsn_nqty > 0 and stsn_ref4 <> '' order by stsn_ref4"
							);
							*/
						if(getCellString("stm_module").equals("sttfr") && (getCellInt("stm_nref4") == 1) ){
							tr = br.getSelectUtil().getQueryResult(
							"select distinct stsn_ref4 from stockserial where stsn_irg = " + getInt("stmd_irg") + " and stsn_org = " + getInt("stmdki_org") + " and stsn_nqty > 0 and stsn_ref4 <> '' "
										+ " and stsn_loc = '" + getCellString("stmdki_loc") + "' "
										+ " order by stsn_ref4"
							
							);
						} else if(getCellString("stm_module").equals("cstmo") && (getCellString("stmd_loc").equals("HZ01")) ){
							tr = br.getSelectUtil().getQueryResult(
								"select distinct stsn_ref4 from stockserial where stsn_irg = " + getInt("stmd_irg") +  " and stsn_nqty > 0 and stsn_ref4 <> '' "
										+ " and stsn_loc in ('HZ01','HQ01')"
										+ " order by stsn_ref4"
							);
						} else {
							tr = br.getSelectUtil().getQueryResult(
//								"select distinct stsn_ref4 from stockserial where stsn_irg = " + getInt("stmd_irg") + " and stsn_org = " + getInt("stmd_org") + " and stsn_nqty > 0 and stsn_ref4 <> '' order by stsn_ref4"
								"select distinct stsn_ref4 from stockserial where stsn_irg = " + getInt("stmd_irg") + " and stsn_org = " + getInt("stmd_org") + " and stsn_nqty > 0 and stsn_ref4 <> '' "
										+ " and stsn_loc = '" + getCellString("stmd_loc") + "' "
										+ " order by stsn_ref4"
							);
						}
						/*
						TableRec tr = br.getSelectUtil().getQueryResult(
								"select distinct stsn_ref4 from stockserial where stsn_irg = " + getInt("stmd_irg") + " and stsn_nqty > 0 and stsn_ref4 <> '' order by stsn_ref4"
							);
							*/
						Vector iList = new Vector();
						iList.add("");
						String fifoRef4 = null;
						boolean skipExpired = false;
						if("Y".equals(Erpv4Config.getString(br.getSessionHelper(), "MoSkipExpired")))  {
							if(getCellString("stm_module").equals("cstmo") && (getCellString("stmd_tdtype").equals("MO"))  ){
								skipExpired = true;
							}
						}
						for(int i=0;i<tr.getRecordCount();i++) {
							tr.setRecPointer(i);
							if(skipExpired) {
								String expdStr = StringUtil.strpart(tr.getFieldString("stsn_ref4"),0,10);
								java.util.Date expd =  DateUtil.dateTimeStrToDate(expdStr);
								if(DateUtil.today().after(expd)) {
									continue;
								}
								
										
							}
							if(fifoRef4 == null) {
								fifoRef4 = tr.getFieldString("stsn_ref4");
							}
							iList.add(tr.getFieldString("stsn_ref4"));
						}

						if(iList.indexOf(getCellString("stmd_ref4")) < 0) {
							iList.add(getCellString("stmd_ref4"));
						}
						getCell("stmd_ref4").setItemList(iList);

						if(fifoRef4 != null && getSid() <= 0) {
							getCell("stmd_ref4").set(fifoRef4);
						}
						
						
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
				}
			}

			@Override
			public void cellAction_onfree() throws CellException {
				// TODO Auto-generated method stub
				
			}
			
		};
		actionEntryUnitChanged = new CellValueAction() {

			@Override
			public void cellAction_onchange(Cell p_value) throws CellException {
				// TODO Auto-generated method stub
					if(stattr != null) {
						if(stattr.ratios.get(getCell("stmd_entryunit").getString()) != null) {
							getCell("stmd_eratio").set( stattr.ratios.get(getCell("stmd_entryunit").getString()));
						} else {
							getCell("stmd_eratio").set(0);
						}
					} else {
						getCell("stmd_eratio").set(0);
					}
			}

			@Override
			public void cellAction_onfree() throws CellException {
				// TODO Auto-generated method stub
				
			}
			
		};
	}
	@Override
	protected void afterCreateColumnCells() {
		if(br == null || br.getParent() == null || br.getParent().exportMode) return;
		if(!br.getView().getName().equals("erpv4.MoGenericDet")) {
		if(testCell("stmd_entryunit") != null) {
			getCell("stmd_irg").addAction(actionIrgChanged);
			getCell("stmd_entryunit").addAction(actionEntryUnitChanged);
		}
		}
	}
	
//	class CellActionIrgChanged extends CellValueAction
//	{
//
//		@Override
//		public void cellAction_onchange(Cell p_value) throws CellException {
//			int irg = getInt("stmd_irg");
//			if(irg > 0) {
//				if(stattr == null || stattr.irg != irg) {
//					stattr = BiResultStock.getStockAttribute(br.getSelectUtil(), irg);
//				}
//			} else {
//				stattr = null;
//			}
//			if(testCell("stmd_entryunit") != null) {
//				if(stattr != null) {
//					getCell("stmd_entryunit").setItemList( stattr.allUnits);
//					if(stattr.allUnits.size() != 1 ) {
//						getCell("stmd_entryunit").set("");
//						getCell("stmd_eratio").set(0);
//					} else {
//						getCell("stmd_entryunit").set(stattr.allUnits.get(0));
//						getCell("stmd_eratio").set(stattr.ratios.get(stattr.allUnits.get(0)));
//					}
//				} else {
//					getCell("stmd_entryunit").set(stattr.allUnits.get(0));
//					getCell("stmd_eratio").set(stattr.ratios.get(stattr.allUnits.get(0)));
//				}
//			}
//		}
//
//		@Override
//		public void cellAction_onfree() throws CellException {
//			// TODO Auto-generated method stub
//			
//		}
//	}
}
