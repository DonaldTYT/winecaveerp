package com.uniinformation.bicore.erpv4;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TranslateListGetItemProperty;
import com.uniinformation.utils.TranslateUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
//import com.uniinformation.zkbi.ZkBiTranslateHelper;

public class Erpv4StockAttribute {
	/*
	static public final String key_Price0 = ".LB_ST_STANDARDPRICE";
	static public final String key_Price1 = ".LB_ST_PRICE1";
	static public final String key_Price2 = ".LB_ST_PRICE2";
	static public final String key_Price3 = ".LB_ST_PRICE3";
	*/
	static public final String key_Price0 = ".ST_STANDARDPRICE";
	static public final String key_Price1 = ".ST_PRICE1";
	static public final String key_Price2 = ".ST_PRICE2";
	static public final String key_Price3 = ".ST_PRICE3";
	static public final String dft_Price0_label = "List Price";
	static public final String dft_Price1_label = "Discount 1";
	static public final String dft_Price2_label = "Discount 2";
	static public final String dft_Price3_label = "Discount 3";
	static TranslateListGetItemProperty priceTypeList = null;
	static Vector<String> allUnitList = null;
	
	int irg;
	String baseUnit;
	String defaultSellUnit;
	String defaultBuyUnit;
	Vector<String> allUnits;
	Hashtable <String,Double> ratios;
	String priceCid;
	Vector allprices;
	Hashtable <String,Double> prices;
	String costCid;
	double cost;
	String promoStr;
	double vatRate=0;
	boolean includeVat = true;
	boolean fixComboUnit=false;
	boolean consumable = false;
//	Erpv4StockCostJournal cj = null;
	class Erpv4StockCostJournal {
		
	}
	
	static Boolean allowFlexibleProductUnit = null;
	static boolean allowFlexibleProductUnit(SessionHelper sh) {
		if( allowFlexibleProductUnit == null) {
			String ss = Erpv4Config.getString(sh, "allowFlexibleProductUnit");
			allowFlexibleProductUnit = "Y".equals(ss);
		}
		return( allowFlexibleProductUnit );
	}
	
	public double getAvCost(java.util.Date p_date) {
		return(cost);
	}
	
	public String getPromoStr() {
		return(promoStr);
	}
	public String getComboQty(double p_qty) {
		double remainQty = p_qty;
		String ss = null;
		for(int i = allUnits.size()-1;i>0;i--) {
			String eu = allUnits.get(i);
			double ratio = ratios.get(eu);
			if(ratio <= remainQty) {
				int cnt = (int) Math.floor(remainQty/ratio);
				remainQty = remainQty % ratio;
				if(ss == null) {
					ss = "= " + cnt + eu;
				} else {
					ss += ","+cnt+eu;
				}
			}
		}
		if(ss == null) return("");
		if(remainQty > 0) ss += ","+((int) remainQty) + baseUnit;
		return(ss);
	}

	static void addOneUnit(Erpv4StockAttribute stattr,String sizeStr,double ratio) {
		String mu = BiResultMcType.getMunit(sizeStr);
		if(mu != null && !mu.equals("") && ratio > 0) {
			String bu = BiResultMcType.getDunit(sizeStr);
			if(stattr.ratios.get(bu) != null) {
				double rt = stattr.ratios.get(bu);
				rt *= ratio;
				if(stattr.allUnits.indexOf(mu) < 0) stattr.allUnits.add(mu);
				stattr.ratios.put(mu, rt);
			}
		}
	}

	static public Erpv4StockAttribute getStockAttribute(Date p_date,SessionHelper p_sh,SelectUtil p_su,int p_irg) {
		try {
			TableRec tr;
//			if(Erpv4Config.isMultiStock(p_sh)) {
			String cocode = Erpv4Config.getDefaultCoCode(p_sh);
			if(Erpv4Config.isMultiStockCost(p_sh)) {
				if(Erpv4Config.isMultiStockPrice(p_sh)) {
					int lcrg = Erpv4Config.getDefaultLcrg(p_sh);
					tr = p_su.getQueryResult("select * from stock,mctype,outer costock "
						+ "	where st_irg = " + p_irg + " and mt_tpcode = st_msubtype and st_stirg = st_irg and st_cocode = '"+cocode+"' and st_lcrg = " + lcrg);
				} else {
					tr = p_su.getQueryResult("select * from stock,mctype,outer costock "
						+ "	where st_irg = " + p_irg + " and mt_tpcode = st_msubtype and st_stirg = st_irg and st_cocode = '"+cocode+"'");
				}
			} else {
			tr = p_su.getQueryResult("select * from stock,mctype "
					+ "	where st_irg = " + p_irg + " and mt_tpcode = st_msubtype ");
			}
			if(tr.getRecordCount() == 1) {
				Erpv4StockAttribute stattr = new Erpv4StockAttribute();
				tr.setRecPointer(0);
				
				stattr.irg = p_irg;
				stattr.baseUnit = tr.getFieldString("st_unit");
				stattr.defaultBuyUnit = tr.getFieldString("st_unit");
				stattr.consumable = tr.getFieldString("st_isconsumable").equals("Y");
				
				stattr.allUnits = new Vector<String>();
				stattr.ratios = new Hashtable<String,Double>();
				if(tr.existField("st_vatrate")) {
					stattr.vatRate = tr.getFieldDouble("st_vatrate");
				}
				if(tr.existField("st_includevat")) {
					stattr.includeVat = "Y".equals(tr.getFieldString("st_includevat"));
				}
				if(tr.existField("mt_baseunit") && !tr.getFieldString("mt_baseunit").equals("")) {
					stattr.defaultSellUnit = tr.getFieldString("st_unit");
					stattr.allUnits.add(stattr.baseUnit);
					stattr.ratios.put(stattr.baseUnit , 1.0);
					stattr.fixComboUnit=true;
					addOneUnit(stattr,tr.getFieldString("mt_tpsize1"),tr.getFieldDouble("st_msize1"));
					addOneUnit(stattr,tr.getFieldString("mt_tpsize2"),tr.getFieldDouble("st_msize2"));
					addOneUnit(stattr,tr.getFieldString("mt_tpsize3"),tr.getFieldDouble("st_msize3"));
				} else {
					stattr.fixComboUnit=false;
					stattr.defaultSellUnit = tr.getFieldString("st_unit");
					stattr.allUnits.add(stattr.baseUnit);
					stattr.ratios.put(stattr.baseUnit , 1.0);
					if(allowFlexibleProductUnit(p_sh)) {
						/*
						stattr.allUnits = getUnitList(p_sh);
								*/
						TableRec tr2 = p_su.getQueryResult("select * from st_unitratio where stur_cocode = ? and stur_irg = ? order by stur_updtime desc",
								new Wherecl()
								.appendArgument(cocode)
								.appendArgument(p_irg)
								);
						for(int i=0;i<tr2.getRecordCount();i++) {
							tr2.setRecPointer(i);
							if(!tr2.getFieldString("stur_unit").equals(stattr.baseUnit)) {
							if(i == 0) {
								stattr.defaultSellUnit = tr2.getFieldString("stur_unit");
							}
							stattr.allUnits.add(tr2.getFieldString("stur_unit"));
							stattr.ratios.put(
									tr2.getFieldString("stur_unit"),
									tr2.getFieldDouble("stur_ratio")
									);
							}
						}
					}
				}
				stattr.prices = new Hashtable<String,Double>();
				stattr.priceCid = tr.getFieldString("st_standardcur");
				stattr.cost = tr.getFieldDouble("st_standardcost");
				stattr.costCid = tr.getFieldString("st_standardcostcur");
				
				String pm = Erpv4Config.getString(p_sh,"HasPromotionPrice");
				if(pm != null && pm.equals("Y")) {
					double stprice = tr.getFieldDouble("st_standardprice");
					double stprice1 = tr.getFieldDouble("st_price1"); 
					double stprice2 = tr.getFieldDouble("st_price2"); 
					double stprice3 = tr.getFieldDouble("st_price3"); 
					if(p_date != null && p_date.after(DateUtil.zeroDate)) {
						tr = p_su.getQueryResult("select * from priceplan,pricepland where prpd_irg = ? and prp_rg = prpd_mrg and prp_start <= ? and prp_end >= ? and prp_active = 'Y' order by prp_rg desc",
									new Wherecl().appendArgument(p_irg)
											.appendArgument(p_date)
											.appendArgument(p_date)
						);
						if(tr.getRecordCount() > 0) {
							tr.setRecPointer(0);
							stattr.promoStr = tr.getFieldString("prp_remark");
							int disc = tr.getFieldInt ("prpd_discount");
							if(disc > 0) {
								stprice = Math.floor(stprice * ((double) (-disc + 100.0)) + 5.0) / 100.0;
								stprice1= Math.floor(stprice1 * ((double) (-disc + 100.0)) + 5.0) / 100.0;
								stprice2= Math.floor(stprice2 * ((double) (-disc + 100.0)) + 5.0) / 100.0;
								stprice3= Math.floor(stprice3 * ((double) (-disc + 100.0)) + 5.0) / 100.0;
							} else {
								double setPrice = tr.getFieldDouble("prpd_price");
								if(setPrice > 0) {
								stprice = setPrice;
								stprice1= setPrice;
								stprice2= setPrice;
								stprice3= setPrice;
								}
							}
						}
					}
					stattr.prices.put(BiResultCustomer.dft_Price0_label, stprice);
					stattr.prices.put(BiResultCustomer.dft_Price1_label,  stprice1);
					stattr.prices.put(BiResultCustomer.dft_Price2_label,  stprice2);
					stattr.prices.put(BiResultCustomer.dft_Price3_label,  stprice3);
				} else {
					stattr.prices.put(BiResultCustomer.dft_Price0_label, tr.getFieldDouble("st_standardprice"));
					stattr.prices.put(BiResultCustomer.dft_Price1_label, tr.getFieldDouble("st_price1"));
					stattr.prices.put(BiResultCustomer.dft_Price2_label, tr.getFieldDouble("st_price2"));
					stattr.prices.put(BiResultCustomer.dft_Price3_label, tr.getFieldDouble("st_price3"));
				}
				
				return(stattr);
			}
			return(null);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(null);
		}
	}
	static public TranslateListGetItemProperty getPriceTypeList(final SessionHelper p_sh) {
		if(priceTypeList == null) {
			String stockViewId = Erpv4Config.getStockViewId(p_sh);
			final Hashtable<String,String> ht = new Hashtable();
			ht.put(dft_Price0_label,stockViewId.toUpperCase()+Erpv4StockAttribute.key_Price0);
			ht.put(dft_Price1_label,stockViewId.toUpperCase()+Erpv4StockAttribute.key_Price1);
			ht.put(dft_Price2_label,stockViewId.toUpperCase()+Erpv4StockAttribute.key_Price2);
			ht.put(dft_Price3_label,stockViewId.toUpperCase()+Erpv4StockAttribute.key_Price3);
			priceTypeList = new TranslateListGetItemProperty(
				new VectorUtil()
				.addElement(dft_Price0_label)
				.addElement(dft_Price1_label)
				.addElement(dft_Price2_label)
				.addElement(dft_Price3_label)
				.toVector()
					) {

				@Override
				public String translate(Object p_item) {
					return(TranslateUtil.getText(p_sh, ht.get((String) p_item), "LABEL", (String) p_item));
					// TODO Auto-generated method stub
				}

				@Override
				public int getRowWidth() {
					// TODO Auto-generated method stub
					return 0;
				}};	
		}
		return(priceTypeList);
	}
	
	static public Vector<String >getUnitList(SessionHelper sh) throws Exception {
		if(allUnitList == null) {
			allUnitList = new Vector<String>();
			SelectUtil su = new SelectUtil();
			su.init(sh.getBiSchema().getConn());
			TableRec tr = su.getQueryResult("select * from st_unit order by stu_unit",null);
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				allUnitList.add(tr.getFieldString("stu_unit"));
			}
		}
		return(allUnitList);
	}

}
