package com.uniinformation.bicore.erpv4;

import java.text.DecimalFormat;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;

public class Erpv4QuoDetCellCollection extends Erpv4BaseCellCollection {
	Erpv4StockAttribute stattr = null;
	CellValueAction actionIrgChanged = null;
	CellValueAction actionEntryUnitChanged = null;

	private enum FuncName { FUNC_getRprice, FUNC_getVat,FUNC_getRpriceStr,FUNC_getRpriceStr2,FUNC_getComboQty,NOT_DEFINED }
	
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
		case FUNC_getVat: {
				double dirg = (Double) p_args.get(0);
				int irg = (int) dirg;
				if(irg <= 0) return(0.0);
				if(stattr == null || stattr.irg != irg) return(0.0);
				double x = (Double) p_args.get(1);
				if(x == 0.0) return(0.0);
				return( CellCollection.round( stattr.vatRate / 100 * x / (1+ stattr.vatRate/100),0.01));
		}
		case FUNC_getComboQty : {
				double dirg = (Double) p_args.get(0);
				int irg = (int) dirg;
				if(irg <= 0) return(0.0);
				double dqty = (Double) p_args.get(1);
				if(stattr == null || stattr.irg != irg) return("");
				String eu = (String) p_args.get(2);
				if(eu != null && eu.equals(stattr.baseUnit)) {
					return(stattr.getComboQty(dqty));
				} else return("");
		}
		case FUNC_getRpriceStr2: {
				double dirg = (Double) p_args.get(0);
				int irg = (int) dirg;
				if(irg <= 0) return(0.0);
				if(stattr == null || stattr.irg != irg) return(0.0);
				java.util.Date pDate;
				if(p_args.size() > 2) {
					pDate = (java.util.Date) p_args.get(2);
				} else {
					pDate = DateUtil.zeroDate;
				}
				return(myGetRpriceStr2(irg,(Double) p_args.get(1),pDate,(String) p_args.get(3)));
		}
		case FUNC_getRpriceStr: {
				double dirg = (Double) p_args.get(0);
				int irg = (int) dirg;
				if(irg <= 0) return(0.0);
				if(stattr == null || stattr.irg != irg) return(0.0);
				java.util.Date pDate;
				if(p_args.size() > 2) {
					pDate = (java.util.Date) p_args.get(2);
				} else {
					pDate = DateUtil.zeroDate;
				}
				/*
				double vatRate0 = stattr.includeVat ? stattr.vatRate : 0;
				String s = (String) p_args.get(1);
				if(s.trim().equals("")) {
					return("");
				} else { 
					double x = stattr.prices.get(s);
					if(x == 0.0) {
						return("");
					}
					String ss = "@";
					ss += stattr.priceCid;
					ss += CellCollection.round(x / (1+vatRate0/100),0.01);
					ss += "/";
					ss += stattr.baseUnit;
					return(ss);
				}
				*/
				return( myGetRpriceStr(dirg,(String) p_args.get(1) ,pDate));
		}
		case FUNC_getRprice: {
				double dirg = (Double) p_args.get(0);
				int irg = (int) dirg;
				if(irg <= 0) return(0.0);
				if(stattr == null || stattr.irg != irg) return(0.0);
				java.util.Date pDate;
				if(p_args.size() > 2) {
					pDate = (java.util.Date) p_args.get(2);
				} else {
					pDate = DateUtil.zeroDate;
				}
				double vatRate = stattr.vatRate;
				double vatRate0 = stattr.includeVat ? stattr.vatRate : 0;
				if(p_args.size() > 3) {
					Object o = p_args.get(3);
					if(o instanceof Double) {
						vatRate = (Double) p_args.get(3);
					}
				}
				String s = (String) p_args.get(1);
						if(s.trim().equals("")) {
							return(0.0);
						} else { 
							double x = stattr.prices.get(s);
							if(vatRate != vatRate0) {
								double p = x / (1 + vatRate0 / 100);
								x = p * (1 + vatRate / 100);
							}
							/*
							if(stattr.vatRate != 0 && !stattr.includeVat) {
								p += CellCollection.round(p * stattr.vatRate/100,0.01);
							}
							*/
							return(x);
						}
			}
		}
		return(super.evalFunction(p_fname,p_args) );
	}

	public Erpv4QuoDetCellCollection(BiCellCollection p_parent, BiResultErpv4 p_br) {
		super(p_parent, p_br);
		// TODO Auto-generated constructor stub
		actionIrgChanged = new CellValueAction() {
			@Override
			public void cellAction_onchange(Cell p_value) throws CellException {
				// TODO Auto-generated method stub
				int irg = getInt("ind_irg");
				if(irg > 0) {
					if(stattr == null || stattr.irg != irg) {
						stattr = Erpv4StockAttribute.getStockAttribute(getCell("inv_date").getDate(),br.getSessionHelper(),br.getSelectUtil(), irg);
					}
				} else {
					stattr = null;
				}
				if(testCell("ind_eratio") != null) {
					if(br.isActionEnabled()) {
					if(stattr != null) {
						getCell("ind_unit").setItemList( stattr.allUnits);
						if(stattr.allUnits.size() != 1 ) {
								getCell("ind_unit").set(stattr.defaultSellUnit);
							/*
								getCell("ind_unit").set("");
								getCell("ind_eratio").set(0);
								*/
						} else {
							getCell("ind_unit").set(stattr.allUnits.get(0));
							if(!stattr.consumable) {
								getCell("ind_eratio").set(stattr.ratios.get(stattr.allUnits.get(0)));
							} else {
								getCell("ind_eratio").set(0);
							}
						}
					} else {
						getCell("ind_unit").setItemList((Vector) null);
						getCell("ind_unit").set("");
						getCell("ind_eratio").set(0);
						if(br.isActionEnabled()) {
						}
					}
					} else {
						if(stattr != null) {
							getCell("ind_unit").setItemList( stattr.allUnits);
							if( stattr.ratios.get(getCell("ind_unit").getString()) != null) {
								/*
								if(getCell("ind_eratio").isOverrided()) {
									getCell("ind_eratio").clearOverride();
								}
								getCell("ind_eratio").set(stattr.ratios.get(getCell("ind_unit").getString()));
								*/
								if(!stattr.consumable) {
									getCell("ind_eratio").sync(stattr.ratios.get(getCell("ind_unit").getString()));
								} else {
									getCell("ind_eratio").sync(0);
								}
							}
						}
					}
				}				
				if(stattr != null) {
						/*
					if(testCell("vd_priceclass") != null)  {
						String s = getCell("vd_priceclass").getString();
						if(s.trim().equals("")) {
							getCell("ind_rprice").set(0.0);
						} else { 
							getCell("ind_rprice").set(stattr.prices.get(s));
						}
						
					}
						*/
					if(testCell("ind_vat") != null)  {
							getCell("ind_vat").sync(stattr.vatRate);
					}
					if(testCell("ind_rprice") != null)  {
							getCell("ind_rprice").eval();
					}
					if(testCell("ind_rpricestr") != null)  {
							getCell("ind_rpricestr").eval();
					}
					if(testCell("ind_uprice") != null)  {
							getCell("ind_uprice").resetValue();
					}
					if(testCell("ind_promostr") != null)  {
					if(stattr.promoStr != null) {
						getCell("ind_promostr").set(stattr.promoStr);
					} else {
						getCell("ind_promostr").set("");
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
						if(stattr.ratios.get(getCell("ind_unit").getString()) != null) {
							getCell("ind_eratio").set( stattr.ratios.get(getCell("ind_unit").getString()));
						} else {
							getCell("ind_eratio").set(0);
						}
						if(testCell("ind_ratiodiv") != null) {
							if( !getCellString("ind_unit").equals("") &&
									!getCellString("ind_unit").equals(stattr.baseUnit)) {
								getCell("ind_ratiodiv").setMode(Cell.VMODE_NORMAL);
							} else {
								getCell("ind_ratiodiv").setMode(Cell.VMODE_HIDDEN);
							}
						}
					} else {
						getCell("ind_eratio").set(0);
						getCell("ind_ratiodiv").setMode(Cell.VMODE_HIDDEN);
					}
					/*
					if(testCell("ind_comboqtyDiv") != null) {
						if(stattr.fixComboUnit) {
							getCell("ind_ratiodiv").setMode(Cell.VMODE_NORMAL);
						} else {
							getCell("ind_ratiodiv").setMode(Cell.VMODE_HIDDEN);
						}
					}
					*/
			}

			@Override
			public void cellAction_onfree() throws CellException {
				// TODO Auto-generated method stub
				
			}
			
		};
	}
	
	Object myGetRpriceStr2(int irg,double price ,java.util.Date pDate,String priceccy) {
				if(stattr == null || stattr.irg != irg) return(0.0);
    			DecimalFormat df = new DecimalFormat("###,###,##0.00");
    			double baseprice = price;
    			String sp = df.format(baseprice).trim();
				String ss = "@";
				ss += stattr.priceCid;
				ss += sp;
				ss += "/";
				ss += stattr.baseUnit;
				return(ss);
				/*
				if(s.trim().equals("")) {
					return("");
				} else { 
					double x = stattr.prices.get(s);
					if(x == 0.0) {
						return("");
					}
					String ss = "@";
					ss += stattr.priceCid;
					ss += CellCollection.round(x / (1+vatRate0/100),0.01);
					ss += "/";
					ss += stattr.baseUnit;
					return(ss);
				}
				*/
	}
	Object myGetRpriceStr(double dirg,String s ,java.util.Date pDate) {
				int irg = (int) dirg;
				if(irg <= 0) return(0.0);
				if(stattr == null || stattr.irg != irg) return(0.0);
				double vatRate0 = stattr.includeVat ? stattr.vatRate : 0;
				if(s.trim().equals("")) {
					return("");
				} else { 
					double x = stattr.prices.get(s);
					if(x == 0.0) {
						return("");
					}
					String ss = "@";
					ss += stattr.priceCid;
					ss += CellCollection.round(x / (1+vatRate0/100),0.01);
					ss += "/";
					ss += stattr.baseUnit;
					return(ss);
				}
	}
	@Override
	protected void afterCreateColumnCells() {
		getCell("ind_irg").addAction(actionIrgChanged);
		if(testCell("ind_eratio") != null) {
			getCell("ind_unit").addAction(actionEntryUnitChanged);
//			getCell("stmd_entryunit").addAction(actionEntryUnitChanged);
		}
	}	
}
