package com.uniinformation.erpv4;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

//import org.zkoss.zul.Image;

import com.uniinformation.bicore.BiView;
//import com.uniinformation.jx.JxField;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.kyoko.common.*;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
//import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class Erpv4Config extends BiConfig {
	// static public String STOCKIN_TDtypes="'RI','MI','JI','BI'";
	// static public String PURCHASE_TDtypes="'PD'";

	static public enum LOCATION_TYPE {LOCATION_TYPE_ANY,LOCATION_TYPE_DEFAULT,LOCATION_TYPE_TRANSFER,LOCATION_TYPE_BYLCRG_EXCLUDE_TRANSIT,LOCATION_TYPE_COMPANY_DEFAULT,LOCATION_TYPE_COMPANY_EXCLUDE_TRANSIT}

	private static String getStmdType(SessionHelper sh, String type) {
		String key = "stmd_" + type;
		String value = (String) getAgentData(sh.getAgent(), key);
		if (value == null) {
			value = BiConfig.getString(sh, "STMD_" + type);
			if (value == null) value = type;
			putAgent(sh.getAgent(), key, value);
		}
		return value;
	}

	static public String getStmd_BI(SessionHelper sh) { return getStmdType(sh, "BI"); }
	static public String getStmd_JI(SessionHelper sh) { return getStmdType(sh, "JI"); }
	static public String getStmd_JO(SessionHelper sh) { return getStmdType(sh, "JO"); }
	static public String getStmd_KI(SessionHelper sh) { return getStmdType(sh, "KI"); }
	static public String getStmd_KO(SessionHelper sh) { return getStmdType(sh, "KO"); }
	static public String getStmd_MI(SessionHelper sh) { return getStmdType(sh, "MI"); }
	static public String getStmd_MO(SessionHelper sh) { return getStmdType(sh, "MO"); }
	static public String getStmd_PD(SessionHelper sh) { return getStmdType(sh, "PD"); }
	static public String getStmd_RI(SessionHelper sh) { return getStmdType(sh, "RI"); }
	static public String getStmd_RO(SessionHelper sh) { return getStmdType(sh, "RO"); }
	static public String getStmd_SI(SessionHelper sh) { return getStmdType(sh, "SI"); }
	static public String getStmd_SO(SessionHelper sh) { return getStmdType(sh, "SO"); }

	static public boolean isStkInTdtype(SessionHelper sh, String type) {
		return getStmd_BI(sh).equals(type) || getStmd_MI(sh).equals(type)
				|| getStmd_JI(sh).equals(type) || getStmd_RI(sh).equals(type);
	}

	static public boolean isStkOutTdtype(SessionHelper sh, String type) {
		return getStmd_MO(sh).equals(type) || getStmd_JO(sh).equals(type)
				|| getStmd_RO(sh).equals(type) || getStmd_SO(sh).equals(type);
	}

	static public boolean isStmdInTdtype(SessionHelper sh, String type) {
		return getStmd_MI(sh).equals(type) || getStmd_JI(sh).equals(type)
				|| getStmd_RI(sh).equals(type) || getStmd_KI(sh).equals(type)
				|| getStmd_BI(sh).equals(type);
	}

	static public boolean isStmdOutTdtype(SessionHelper sh, String type) {
		return getStmd_MO(sh).equals(type) || getStmd_JO(sh).equals(type)
				|| getStmd_RO(sh).equals(type) || getStmd_KO(sh).equals(type)
				|| getStmd_SO(sh).equals(type);
	}
	
	static public boolean isMultiDepartment(SessionHelper sp) {
		String ss = getString(sp,"multiDepartment");
		return("Y".equals(ss));
	}
	static public int getLocWtAvOrg(SessionHelper p_sp,String p_cocode,String p_loccode) throws Exception {
			SelectUtil su = null;
			try {
				if(p_loccode == null || p_loccode.trim().equals("")) getCoWtAvOrg(p_sp,p_cocode);
				Hashtable<String,Integer> orgHash = (Hashtable<String,Integer>) p_sp.getSessionData("LOCWTAVORGLIST");
				if(orgHash == null) {
					orgHash = new Hashtable<String,Integer>();
					su = new SelectUtil();
					su.init(p_sp.getBiSchema().getConn());
					TableRec tr = su.getQueryResult("select * from locationcode");
					for(int i=0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						int wtavorg = tr.getFieldInt("loc_wtavorg");
						if(wtavorg > GenbucketUtil.WEIGHTED_AVERAGE_ORGMIN) {
							orgHash.put(tr.getFieldString("loc_code"), wtavorg);
						}
					}
					p_sp.putSessionData("LOCWTAVORGLIST", orgHash);
				}
				Integer org = orgHash.get(p_loccode);
				if(org != null) return(org);
				return(getCoWtAvOrg(p_sp,p_cocode));
			} catch (Exception ex) {
				//UniLog.log(ex);
				throw ex;
			}
			finally {
				if (su != null) su.close();
			}
		
	}
	static public int getCoWtAvOrg(SessionHelper p_sp,String p_cocode) throws Exception {
		if(isMultiCompany(p_sp)) {
			SelectUtil su = null;
			try {
				if(p_cocode == null || p_cocode.trim().equals("")) return(0);
				Hashtable<String,Integer> orgHash = (Hashtable<String,Integer>) p_sp.getSessionData("WTAVORGLIST");
				if(orgHash == null) {
					orgHash = new Hashtable<String,Integer>();
					su = new SelectUtil();
					su.init(p_sp.getBiSchema().getConn());
					TableRec tr = su.getQueryResult("select * from cocode ");
					for(int i=0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						int wtavorg = tr.getFieldInt("co_wtavorg");
						if(wtavorg < GenbucketUtil.WEIGHTED_AVERAGE_ORGMIN) wtavorg = GenbucketUtil.WEIGHTED_AVERAGE_ORGMIN;
						orgHash.put(tr.getFieldString("co_cocode"), wtavorg);
					}
					p_sp.putSessionData("WTAVORGLIST", orgHash);
				}
				return(orgHash.get(p_cocode));
			} catch (Exception ex) {
				//UniLog.log(ex);
				throw ex;
			}
			finally {
				if (su != null) su.close();
			}
		} 
		return(GenbucketUtil.WEIGHTED_AVERAGE_ORGMIN);
	}
	static public boolean allowMultipleCustomerPayment(SessionHelper sp,SelectUtil su) {
		return(allowMultipleCustomerDN(sp,su));
	}
	static public boolean allowMultipleCustomerDN(SessionHelper sp,SelectUtil su) {
		Boolean sw = (Boolean) sp.getSessionData("ALLOWMULTIPLECUSTOMERDN");
		if(sw == null) {
			RpcClient rpc = sp.getRpcClient();
			Value v = rpc.callSegment("erpv4GetSetupParam",
						new VectorUtil()
							.addElement("ALLOWMULTIPLECUSTOMERDN")
							.toVector()
					);
			rpc.close();
			if(v != null && v.toString().equals("Y")) {
				sw = new Boolean(true);
				
			} else {
				sw = new Boolean(false);
			}
			sp.putSessionData("ALLOWMULTIPLECUSTOMERDN",sw);
		}
		return(sw);
	}
	static public boolean useWeightedAverageOrg(SessionHelper p_sp) {
			String s = Erpv4Config.getString(p_sp, "useWeightedAverageOrg");
			if(s == null || !s.equals("Y")) return(false);
			return(true);
	}
	
	static public boolean isMultiStockPrice(SessionHelper p_sp) {
		String s = Erpv4Config.getString(p_sp, "MultiStockPrice");
		return("Y".equals(s));
	}
	static public boolean isMultiStockCost(SessionHelper p_sp) {
		Boolean b = (Boolean) p_sp.getSessionData("MULTISTOCK");
		if(b == null) {
			String s = Erpv4Config.getString(p_sp, "MultiStock");
			/*
			if(s != null && s.equals("Y")) {
				b = true;
			} else {
				b = false;
			}
			*/
			if(s == null) {
				// if MultiStock is not set, MultiStock default to sames as MultiCompany
				b = isMultiCompany(p_sp);
			} else {
				b = s.equals("Y");
			}
			p_sp.putSessionData("MULTISTOCK", b);
		}
		return(b);
	}
	/*
	static public void setupLogoImage(JxField fd,SessionHelper br) {
		if(fd != null) {
			String logo = Erpv4Config.getString(br.getSessionHelper(), "LogoImage");
			if(logo != null) {
				Image img = (Image) fd.getNativeObject();
				img.setSrc(logo);
			}
		}
	}	
	*/
	
	// use previous of p_date's stocktake total
	static public StockOpening getStockOpening(SelectUtil p_su,int p_irg,java.util.Date p_date,boolean p_locBal) {
		try {
			TableRec tr = null;
			tr = p_su.getQueryResult("select * from stmov where stm_type = 'MO' and stm_status='Confirmed' and stm_module='stake' and stm_nref4=? and stm_date = ? order by stm_date desc", 
							new Wherecl().appendArgument(p_irg).appendArgument(p_date)
					);
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				StockOpening sto = new StockOpening();
				sto.date = tr.getFieldDate("stm_date");
				sto.balance = tr.getFieldDouble("stm_fref3");
				sto.unitcost = tr.getFieldDouble("stm_fref4");
				if(p_locBal) {
					sto.locBalance = new Hashtable<String,Double>();
					tr = p_su.getQueryResult("select sttk_loc,sum(sttk_cqty) sumqty from stmov,stocktake where stm_type = 'MO' and stm_status='Confirmed' and stm_module='stake' and stm_nref4= ? and stm_date = ? and sttk_mrg = stm_mrg group by sttk_loc", 
							new Wherecl().appendArgument(p_irg).appendArgument(p_date)
					);
					for(int i=0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						sto.locBalance.put(tr.getFieldString("sttk_loc"), tr.getFieldDouble("sumqty"));
					}
				}
				return(sto);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		return(null);
	}
	
	// use previous p_date's stocktake detail
	static public StockOpening getLocgroupOpening(SelectUtil p_su,int p_irg,java.util.Date p_date,int p_group) {
		try {
//			java.util.Date dd = DateUtil.prevday(p_date);
			java.util.Date dd = p_date;
			TableRec tr = null;
			tr = p_su.getQueryResult("select sum(sttk_cqty) sumqty from stmov,stocktake,locationcode where stm_type = 'MO' and stm_status='Confirmed' and stm_module='stake' and stm_nref4= ? and stm_date = ? and sttk_mrg = stm_mrg and loc_code = sttk_loc and loc_group = ? ", 
							new Wherecl().appendArgument(p_irg).appendArgument(dd).appendArgument(p_group)
					);
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				StockOpening sto = new StockOpening();
				sto.date = dd;
				sto.balance = tr.getFieldDouble("sumqty");
				sto.unitcost = 0;
				return(sto);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		return(null);
	}
	
	static public java.util.Date getCostOpeningErpDate(SessionHelper p_sh) {
		java.util.Date jsd = (java.util.Date) p_sh.getSessionData("CostOpeningDate");
		if(jsd != null) return(jsd);
		String csd = getString(p_sh, "CostOpeningDate");
		if(csd != null)  
			jsd = DateUtil.dateTimeStrToDate(csd);
		else
			jsd = DateUtil.zeroDate;
		p_sh.putSessionData("CostOpeningDate", jsd);
		return(jsd);
	}
	static public boolean getAllowNegativeStock(SessionHelper p_sh) {
		String an = getString(p_sh,"AllowNegativeStock");
		return("Y".equals(an));
	}
	static public boolean getLocationAllowNegative(SessionHelper p_sh,String p_loccode) {
		if(isMultiStockLoc(p_sh)) {
			if(p_loccode == null || "".equals(p_loccode)) return(true);
		}
		String an = getString(p_sh,"AllowNegativeStock");
		if(an != null && an.equals("Y")) return(true);
		HashSet<String> lan = (HashSet<String>) p_sh.getSessionData("LocAllowNegative");
		if(lan == null) {
			lan =  new HashSet<String>();
			SelectUtil su=null;
			try {
			BiView stv ;
			TableRec tr;
			stv = p_sh.getBiSchema().getViewByName("erpv4.LocationCode");
			if(stv != null) {
				su =  p_sh.getBiSchema().getSelectUtil();
				tr = su.getQueryResult("select * from locationcode ");
				su.close();
				if(tr.existField("loc_allowneg")) {
					for(int i=0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						an = tr.getFieldString("loc_allowneg");
						if(an.equals("Y")) {
							lan.add(tr.getFieldString("loc_code"));
						}
					}
				}
			}
			p_sh.putSessionData("LocAllowNegative", lan);
			} catch (Exception ex) {
				UniLog.log(ex);
				if(su != null) su.close();
			}
		}
		if(lan.contains(p_loccode)) return(true);
		return(false);
	}
	
	static class StmdTypeRec {
		String name;
		String formula;
		String desc;
		int order;
	}
	static public String getStmdName(SessionHelper p_sh,String p_type) {
		Hashtable<String,StmdTypeRec> ht = (Hashtable<String,StmdTypeRec>) p_sh.getSessionData("STMDTYPE");
		StmdTypeRec stmdType;
		if(ht == null ) {
				SelectUtil su = null;
				try {
					ht = new Hashtable<String,StmdTypeRec>();
					su = new SelectUtil();
					su.init(p_sh.getBiSchema().getConn());
					TableRec tr = su.getQueryResult("select * from stmdtype");
					for(int i=0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						stmdType = new StmdTypeRec();
						stmdType.name = tr.getFieldString("stmdo_name");
						if(stmdType.name.trim().equals("")) {
							stmdType.name = tr.getFieldString("stmdo_tdtype");
						}
						ht.put(tr.getFieldString("stmdo_tdtype"), stmdType);
					}
					p_sh.putSessionData("STMDTYPE", ht);
				} catch (Exception ex) {
					UniLog.log(ex);
					return(null);
				}
				finally {
					if (su != null) su.close();
				}
		}
		stmdType = ht.get(p_type);
		if(stmdType == null) return(null);
		return(stmdType.name);
	}
	
	static public java.util.Date stmMinDate(SessionHelper p_sh) {
		java.util.Date pd = (java.util.Date) p_sh.getSessionData("STMMINDATE");
		if(pd == null) {
			pd = DateUtil.zeroDate;
			String pds = getString(p_sh,"StmMinDate");
			if(pds != null) {
				pd = DateUtil.dateTimeStrToDate(pds);
			}
			try {
				SelectUtil su = new SelectUtil();
				su.init(p_sh.getBiSchema().getConn());
				TableRec tr = su.getQueryResult("select * from postdate where pd_cocode = ? and pd_type = 'STOCK'",
							new Wherecl()
								.appendArgument(getDefaultCoCode(p_sh))
								);
				if(tr.getRecordCount() == 1) {
					tr.setRecPointer(0);
					pd = tr.getFieldDate("pd_mindate");
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			p_sh.putSessionData("STMMINDATE",pd);
		}
		return(pd);
	}
	static public boolean requiredLoc(SessionHelper p_sp) {
		Boolean b = (Boolean) p_sp.getSessionData("REQUIRELOC");
		if(b == null) {
			String s = getString(p_sp,"RequireLoc");
			if(s != null && s.equals("Y")) b = true; else b = false;
			p_sp.putSessionData("REQUIRELOC", b);
		}
		return(b);
	}
	static public String getStockMoveTypes(SessionHelper p_sp) {
		Boolean b = (Boolean) p_sp.getSessionData("USEGMMI");
		if(b == null) {
			RpcClient rpc = p_sp.getRpcClient();
			Value v = rpc.callSegment("erpv4_use_gmmi");
			if(v != null && v.toString().equals("Y")) {
				b = true;
			} else {
				b = false;
			}
			rpc.close();
			p_sp.putSessionData("USEGMMI", b);
		}
		if(b) {
			return("'" + getStmd_RI(p_sp) + "','" + getStmd_MI(p_sp) + "','"
					+ getStmd_JI(p_sp) + "','" + getStmd_KI(p_sp) + "','" + getStmd_RO(p_sp) + "','"
					+ getStmd_MO(p_sp) + "','" + getStmd_JO(p_sp) + "','" + getStmd_KO(p_sp) + "','"
					+ getStmd_SO(p_sp) + "'");
		} else {
			return("'" + getStmd_RI(p_sp) + "','" + getStmd_MI(p_sp) + "','"
					+ getStmd_JI(p_sp) + "','" + getStmd_KI(p_sp) + "','" + getStmd_RO(p_sp) + "','"
					+ getStmd_MO(p_sp) + "','" + getStmd_JO(p_sp) + "','" + getStmd_KO(p_sp) + "','"
					+ getStmd_SO(p_sp) + "','" + getStmd_BI(p_sp) + "'");
		}
	}
	
	static public java.util.Date getMaxPcStart(SelectUtil su,String p_cocode) {
		try {
			TableRec tr = su.getQueryResult("select * from maxpc where mp_cocode = '" + p_cocode + "'");
			tr.setRecPointer(0);
			return(tr.getFieldDate("mp_pstart"));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(DateUtil.maxDate);
		}	
	}
	

	static public boolean ignoreOrgInCost(SessionHelper p_sp) {
		Boolean b = (Boolean) p_sp.getSessionData("IGNOREORGINCOST");
		if(b == null) {
			String s = getString(p_sp,"IgnoreOrgInCost");
			if(s != null && s.equals("Y")) b = true; else b = false;
			p_sp.putSessionData("IGNOREORGINCOST", b);
		}
		return(b);
	}
	
	static public Set<String> getLocationListByCompany(SessionHelper p_sh,String p_cocode,LOCATION_TYPE p_locType){
		if(!isMultiCompany(p_sh)) return(null);
		SelectUtil su = new SelectUtil();
		Set<String> ss = new HashSet<String>();
		try {
		su.init(p_sh.getBiSchema().getConn());
		TableRec tr = null;
		switch(p_locType) {
		case LOCATION_TYPE_ANY:
			tr = su.getQueryResult("select loc_code from locationcode where loc_cocode = '" + p_cocode + "'");
			break;
		case LOCATION_TYPE_BYLCRG_EXCLUDE_TRANSIT:
			tr = su.getQueryResult("select loc_code from locationcode where loc_transit <> 'Y' and loc_cocode = '" + p_cocode + "' and loc_mrg = " + getDefaultLcrg(p_sh));
			break;
		case LOCATION_TYPE_COMPANY_EXCLUDE_TRANSIT:
			tr = su.getQueryResult("select loc_code from locationcode where loc_transit <> 'Y' and loc_cocode = '" + p_cocode + "' ");
			break;
		case LOCATION_TYPE_COMPANY_DEFAULT:
			tr = su.getQueryResult("select loc_code from locationcode where loc_cocode = '" + p_cocode + "'");
			break;
		case LOCATION_TYPE_DEFAULT:
			if(isMultiStockLoc(p_sh)) {
				tr = su.getQueryResult("select loc_code from locationcode where loc_cocode = '" + p_cocode + "' and loc_mrg = " + getDefaultLcrg(p_sh) + " and loc_tfronly <> 'Y' ");
			} else {
				tr = su.getQueryResult("select loc_code from locationcode where loc_cocode = '" + p_cocode + "'");
			}
			break;
		}
		if(tr.getRecordCount() <= 0) { 
			ss.add("No Location");
		} else {
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				ss.add(tr.getFieldString("loc_code"));
			}
		}
		} catch (Exception ex) {
			UniLog.log(ex);
		} finally {
			su.close();
		}
		return(ss);
	}
	
	public static String getStockViewId(SessionHelper sp) {
		String stockView = getString(sp, "customStockView");
		if(stockView == null) return("erpv4.Stock");
		return(stockView);
	}
	
	/*
	public static String getStockTakeLoc(SessionHelper sp,String p_cocode) {
		Hashtable<String,String> stlocs = (Hashtable<String,String>) sp.getSessionData("STOCKTAKELOCS");
		if(stlocs == null) {
			stlocs = new Hashtable<String,String>();
			sp.putSessionData("STOCKTAKELOCS", stlocs);
			String ss = getString(sp,"StockTakeLocation");
			if(ss != null) {
				try {
					int slrg = Integer.parseInt(ss);
					SelectUtil su = new SelectUtil();
					su.init(sp.getBiSchema().getConn());
					TableRec tr = su.getQueryResult("select * from locationcode where loc_mrg = "+slrg);
					for(int i=0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						stlocs.put(tr.getFieldString("loc_cocode"),tr.getFieldString("loc_code"));
					}
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		}
		return(stlocs.get(p_cocode));
	}
	*/
	public static String getStockTakeLoc(SessionHelper sp,String p_cocode) {
		Hashtable<String,String> stlocs = (Hashtable<String,String>) sp.getSessionData("STOCKTAKELOCS");
		if(stlocs == null) {
			stlocs = new Hashtable<String,String>();
			sp.putSessionData("STOCKTAKELOCS", stlocs);
			int slrg = getStockTakeLcrg(sp);
			if(slrg > 0) {
				try {
					SelectUtil su = new SelectUtil();
					su.init(sp.getBiSchema().getConn());
					TableRec tr = su.getQueryResult("select * from locationcode where loc_mrg = "+slrg);
					for(int i=0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						stlocs.put(tr.getFieldString("loc_cocode"),tr.getFieldString("loc_code"));
					}
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		}
		return(stlocs.get(p_cocode));
	}
	public static int getStockTakeLcrg(SessionHelper sp) {
		Integer stlcrg = (Integer) sp.getSessionData("STOCKTAKELCRG");
		if(stlcrg == null) {
			String ss = getString(sp,"StockTakeLocation");
			if(ss != null) {
				try {
					stlcrg = Integer.parseInt(ss);
				} catch (Exception ex) {
					UniLog.log(ex);
					stlcrg = 0;
				}
			} else stlcrg = 0;
			sp.putSessionData("STOCKTAKELCRG", stlcrg);
		}
		return(stlcrg);
	}
	
	public static boolean useStockGen(SessionHelper sp) {
		Boolean useStg = (Boolean) sp.getSessionData("USESTOCKGEN");
		if(useStg == null) {
			if("Y".equals(getString(sp,"NoStockGen"))) {
				useStg = false;
			} else {
				useStg = true;
			}
			sp.putSessionData("USESTOCKGEN",useStg);
		}
		return(useStg);
	}
}
