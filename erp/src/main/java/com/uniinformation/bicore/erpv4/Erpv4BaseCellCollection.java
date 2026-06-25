package com.uniinformation.bicore.erpv4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.GenbucketUtil;
import com.uniinformation.erpv4.GlBalanceCalculation;
import com.uniinformation.erpv4.RecSync;
import com.uniinformation.erpv4.RecSyncErpv4;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.utils.ImageUtil;
import com.kyoko.common.*;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.erpv4.BxRateUtil;
import com.uniinformation.webcore.erpv4.Erpv4SessionHelper;

public class Erpv4BaseCellCollection extends BiCellCollection {
//	BiResult br;
//	HashMap <String,Double> currentXrateMap = null;
	BxRateUtil rateUtil=null;
	
	static public Set<String> stkInQty = new HashSet<String>(Arrays.asList("BI", "MI", "JI","RI"));
	static public Set<String> stkOutQty = new HashSet<String>(Arrays.asList("MO", "JO","RO","SO"));
	static public Set<String> stmdOutQty = new HashSet<String>(Arrays.asList("MO","JO","RO","KO","SO"));
	static public Set<String> stmdInQty = new HashSet<String>(Arrays.asList("MI", "JI","RI","KI","BI"));
//	static HashSet<String> stkInQty = new HashSet<String> () = {"ABC"};
	private enum FuncName { FUNC_baseccy, FUNC_getCurrentXrate, FUNC_getXrateByDate, FUNC_getStDimFactor, FUNC_getStDimW, FUNC_getStDimL, FUNC_getStDimH, 
					FUNC_getStmdInQty, FUNC_getStmdOutQty,FUNC_getMunit,FUNC_getDunit,FUNC_defaultCocode,FUNC_getDueDate,FUNC_datePeriod,FUNC_getConfig,FUNC_callsegi,FUNC_callsegs,FUNC_getWtAvOrg,
					FUNC_getAverageCost,FUNC_convToRef4,FUNC_ref4ToExpD,FUNC_ref4ToLotNo,FUNC_getStmdDirection,FUNC_getCrossRateByDate,Func_convToRef4Ex,FUNC_getLocBalance,FUNC_getRemoteFreeStock,
					FUNC_getRemoteOnOrder,/* FUNC_getLoginProperty, */FUNC_getDefaultLoc,FUNC_defaultLcrg,FUNC_getCoShortcode,FUNC_getConfigString,
					FUNC_getUniqueField,FUNC_defaultLcGroup,FUNC_getLocWtAvOrg,FUNC_getBiMiOrg,FUNC_getStmovUnitCost,
					FUNC_getGlBeginBal, FUNC_getGlEndBal, FUNC_getGlPosBeginBal, FUNC_getGlPosEndBal, FUNC_getGlNegBeginBal, FUNC_getGlNegEndBal,
					FUNC_getGlDaBeginBal, FUNC_getGlDaEndBal, FUNC_getGlDaPosBeginBal, FUNC_getGlDaPosEndBal, FUNC_getGlDaNegBeginBal, FUNC_getGlDaNegEndBal,
					FUNC_getGlDaBeginlBal, FUNC_getGlDaEndlBal, FUNC_getGlDaPosBeginlBal, FUNC_getGlDaPosEndlBal, FUNC_getGlDaNegBeginlBal, FUNC_getGlDaNegEndlBal,
					FUNC_filingToHtmlImage,
					FUNC_urlToHtmlImage,
					FUNC_voicemsgToResourceUrl,
//					FUNC_ResourceUrlToHtmlImage,
					FUNC_getPoFromAllocate,
					NOT_DEFINED }

	/*
	private HashSet<String>notExistFunc;
	private FuncName checkAndGetFuncName(String p_fname) {
		if(notExistFunc != null && notExistFunc.contains(p_fname)) return(FuncName.NOT_DEFINED);
		try {
			return(FuncName.valueOf("FUNC_"+p_fname));
		}
		catch(Exception ex) {
			//remark: if enum not exist, will got exception here.
			if(notExistFunc == null) notExistFunc = new HashSet<String>();
			notExistFunc.add(p_fname);
			return(FuncName.NOT_DEFINED);
		}
	}
	*/
	
	
	public Erpv4BaseCellCollection(BiCellCollection p_parent,BiResultErpv4 p_br) {
		super(p_parent,p_br);
//		br = p_br;
	}
	@Override
	public Object evalFunction(String p_fname,Vector p_args) throws Exception {
		/*
		FuncName funcName = FuncName.NOT_DEFINED;
		try {
			funcName = FuncName.valueOf("FUNC_"+p_fname);
		}
		catch(Exception ex) {
			//remark: if enum not exist, will got exception here.
		}
		*/
		/*
		FuncName funcName = checkAndGetFuncName(p_fname);		
		*/
		FuncName funcName = checkAndGetFuncNameCache(p_fname,FuncName.NOT_DEFINED);		
		
		
		if(formulaInit != null) return(super.evalFunction(p_fname, p_args));
		switch (funcName){
		case FUNC_baseccy: {
			String cocode;
			if(p_args != null && p_args.size() > 0) {
				cocode = (String) p_args.get(0);
			} else {
				cocode = Erpv4Config.getDefaultCoCode(br.getSessionHelper());
			}
			return(Erpv4Config.getBaseCcy(br.getSessionHelper(),cocode));
			}
		case FUNC_defaultCocode: {
			return(Erpv4Config.getDefaultCoCode(br.getSessionHelper()));
			}
		case FUNC_defaultLcGroup : {
			String lc = (Erpv4Config.getDefaultLcGroup(br.getSessionHelper()));
			return(lc == null ? "" : lc);
			}
		case FUNC_getConfigString: {
			String lc = (Erpv4Config.getString(br.getSessionHelper(), (String) p_args.get(0)));
			return(lc == null ? "" : lc);
			}
		case FUNC_getCoShortcode: {
			String cocode;
			if(p_args != null && p_args.size() > 0) {
				cocode = (String) p_args.get(0);
			} else {
				cocode = Erpv4Config.getDefaultCoCode(br.getSessionHelper());
			}
			return(Erpv4Config.getCoShortcode(br.getSessionHelper(),cocode));
			}
		case FUNC_getCurrentXrate: {
			String cocode;
			if(p_args.size() > 1) {
				cocode = (String) p_args.get(1);
			} else {
				cocode = Erpv4Config.getDefaultCoCode(br.getSessionHelper());
			}
			String bccy = Erpv4Config.getBaseCcy(br.getSessionHelper(),cocode);
			if(rateUtil == null || rateUtil.getBaseCid().equals(bccy)) {
				rateUtil = new BxRateUtil(bccy);
			}
			Double xrate = rateUtil.getRate((String) p_args.get(0),br.getSelectUtil());
			if(xrate == null) xrate = 1.0;
			return(xrate);
			}
		case FUNC_getCrossRateByDate: {
				String cocode = (String) p_args.get(0);
				String ccy1 = (String) p_args.get(1); // stmd_cur
				String ccy2 = (String) p_args.get(2); // stm_cur
				Date d = (Date) p_args.get(3);
				if(ccy1 == null || ccy2 == null || d == null || ccy1.equals("") || ccy2.equals("") || !d.after(DateUtil.minDate)) {
					return(0.0);
				}
				if(ccy1.equals(ccy2)) return(1.0);
				String bccy = Erpv4Config.getBaseCcy(br.getSessionHelper(),cocode);
				if(rateUtil == null || !rateUtil.getBaseCid().equals(bccy)) {
					rateUtil = new BxRateUtil(bccy);
				}
				if(ccy2.equals(bccy)) {
					return(rateUtil.getRateByDate(ccy1,d,br.getSelectUtil()));
				}
				if(ccy1.equals(bccy)) {
					return(1/rateUtil.getRateByDate(ccy2,d,br.getSelectUtil()));
				}
				return(
						rateUtil.getRateByDate(ccy1,d,br.getSelectUtil())/
						rateUtil.getRateByDate(ccy2,d,br.getSelectUtil())
						);
			}
		case FUNC_getXrateByDate: {
			String cocode;
			if(p_args.size() > 2) {
				cocode = (String) p_args.get(2);
			} else {
				cocode = Erpv4Config.getDefaultCoCode(br.getSessionHelper());
			}
			String bccy = Erpv4Config.getBaseCcy(br.getSessionHelper(),cocode);
			if(bccy == null) return(1.0);
			if(rateUtil == null || !rateUtil.getBaseCid().equals(bccy)) {
				rateUtil = new BxRateUtil(bccy);
			}
			Double xrate = rateUtil.getRateByDate((String) p_args.get(0),(Date) p_args.get(1),br.getSelectUtil());
			if(xrate == null) xrate = 1.0;
			return(xrate);
			}
		case FUNC_getStDimFactor: {
			String s = (String) p_args.get(0);
			if(s.equals("M")) return(1.0);
			if(s.equals("c")) return(0.01);
			if(s.equals("m")) return(0.001);
			if(s.equals("Y")) return(0.9144);
			if(s.equals("f")) return(0.3048);
			if(s.equals("i")) return(0.0254);
			if(s.equals("MM")) return(1.0);
			if(s.equals("iM")) return(0.0254);
			if(s.equals("MY")) return(0.9144);
			if(s.equals("iY")) return(0.02322576);
			if(s.equals("if")) return(0.00774192);
			if(s.equals("fM")) return(0.3048);
			if(s.equals("ff")) return(0.09290304);
			if(s.equals("fy")) return(0.27870912);
			if(s.equals("ii")) return(0.00064516);
			return(0.0);
			}
		case FUNC_getStDimW :{
			String s = (String) p_args.get(0);
			if(s.equals("")) return(0.0);
			return(new com.uniinformation.cell.IgnoreValue());
			}
		case FUNC_getStDimL:  {
			String s = (String) p_args.get(0);
			if(s.equals("")) return(0.0);
			if(s.equals("M")) return(1.0);
			if(s.equals("c")) return(1.0);
			if(s.equals("m")) return(1.0);
			if(s.equals("Y")) return(1.0);
			if(s.equals("f")) return(1.0);
			if(s.equals("i")) return(1.0);
			return(new com.uniinformation.cell.IgnoreValue());
			}
		case FUNC_getStDimH:  {
			String s = (String) p_args.get(0);
			if(s.equals(""))  return(0.0);
			return(1.0);
			}
		case FUNC_getStmdInQty : {
				String tdtype = (String) p_args.get(0);
				double qty = ((Double) p_args.get(1));
				if(stkInQty.contains(tdtype)) return(qty); else return(0.0);
			}
		case FUNC_getStmdOutQty : {
				String tdtype = (String) p_args.get(0);
				double qty = ((Double) p_args.get(1));
				if(stkOutQty.contains(tdtype)) return(qty); else return(0.0);
			}
		case FUNC_getStmdDirection : {
			String tdtype = (String) p_args.get(0);
			if(stmdOutQty.contains(tdtype)) return(-1.0);
			if(stmdInQty.contains(tdtype)) return(1.0);
			return(0.0);
		}
		case FUNC_getMunit: {
				String unitStr = (String) p_args.get(0);
				return(BiResultMcType.getMunit(unitStr));
			}
		case FUNC_getDunit: {
				String unitStr = (String) p_args.get(0);
				return(BiResultMcType.getDunit(unitStr));
			}
		case FUNC_getDueDate : {
				Date d = (Date) p_args.get(0);
				if(d == null || !d.after(DateUtil.minDate)) return(DateUtil.zeroDate);
				double d_dd = (Double) p_args.get(1);
				double d_tt = (Double) p_args.get(2);
				int dd = (int) d_dd;
				int tt = (int) d_tt;
				int sd;
				Date d0;
				switch(tt) {
				case 1:
					d0 = d;
					sd = DateUtil.dateToInformix(d0);
					sd += dd;
					return(DateUtil.informixToDate(sd));
				case 2:
					int std = -1; // default statement date = last monthday (count from 0)
					d0 = DateUtil.monthStart(d); // monthstart date of invoice date
					int nd = DateUtil.getDay(d)-1; // date dayofmonth of invoice date (count from 0)
					if(p_args.size() > 3) {
						double d_std = (Double) p_args.get(3);
						std = ((int) d_std) - 1;
						// if(has stmtdate argument get statement date) (count from 0)
					}  
					if(nd > std) {
						// if invoice date > stmtdate move to nextmonth
						d0 = DateUtil.nextmonth(d0);
					}
					sd = DateUtil.dateToInformix(d0) + std; // change to informix int date and add stmtdate
					sd += dd;
					return(DateUtil.informixToDate(sd)); // add duedays
					/*
					sd = DateUtil.dateToInformix(d0);
					sd = sd + dd-1;
					d0 = DateUtil.informixToDate(sd);
					if(d0.before(d)) {
						d0 = DateUtil.nextmonth(d0);
					}
					*/
					/*
					d0 = DateUtil.nextmonth(d0);
					for(int i=0;i<dd;i++) {
						d0 = DateUtil.nextmonth(d0);
					}
					return(d0);
					*/
				default : return(d);
				}
			}
		case FUNC_datePeriod: {
			SimpleDateFormat dfmt=null;
				try {
					if(p_args.size() < 2)
					dfmt = new SimpleDateFormat("yyyy-MM");
					else
					dfmt = new SimpleDateFormat((String) p_args.get(1));
				} catch (IllegalArgumentException iex) {
					UniLog.log("Period Format Invalid use default yyyy-MM");
					dfmt = new SimpleDateFormat("yyyy-MM");
				} catch (ArrayIndexOutOfBoundsException iex) {
					UniLog.log("Period Format Invalid use default yyyy-MM");
					dfmt = new SimpleDateFormat("yyyy-MM");
				}
				return(dfmt.format((Date) p_args.get(0)));
			}
		case FUNC_getWtAvOrg: {
			String cocode = (String) p_args.get(0);
			int org = Erpv4Config.getCoWtAvOrg(br.getSessionHelper(), cocode);
			return(org);
		}
		case FUNC_getLocWtAvOrg: {
			String cocode = (String) p_args.get(0);
			String loccode = (String) p_args.get(1);
			int org = Erpv4Config.getLocWtAvOrg(br.getSessionHelper(), cocode,loccode);
			return(org);
		}
		case FUNC_getBiMiOrg: {
			String cocode = (String) p_args.get(0);
			String loccode = (String) p_args.get(1);
			boolean isNotWtAv= (Boolean) p_args.get(2);
			double o = (Double) p_args.get(3);
			int org = (int) o;
			if( org <= 0) return(0);
			if(isNotWtAv) return(org); else return(
					Erpv4Config.getLocWtAvOrg(br.getSessionHelper(), cocode,loccode)
			);
		}
		case FUNC_defaultLcrg:
			return(Erpv4Config.getDefaultLcrg(br.getSessionHelper()));
		case FUNC_getDefaultLoc : {
			String cocode = (String) p_args.get(0);
			if(cocode == null || cocode.trim().equals("")) return("");
			Set<String>locs = Erpv4Config.getLocationListByCompany(br.getSessionHelper(), cocode,Erpv4Config.LOCATION_TYPE.LOCATION_TYPE_DEFAULT);
			if(locs.size() != 1) return( new com.uniinformation.cell.IgnoreValue());
			return(locs.toArray()[0]);
		}
		case FUNC_getAverageCost: {

			double firg;
			int irg;
			double forg;
			int org;
			if(p_args.get(0) instanceof Double) {
				firg = (Double) p_args.get(0);
				irg = (int) firg;
			} else {
				irg = (Integer) p_args.get(0);
			}
			if(p_args.get(1) instanceof Double) {
				forg = (Double) p_args.get(1);
				org = (int) forg;
			} else {
				org = (Integer) p_args.get(1);
			}
			
			Date d = (Date) p_args.get(2);
			if(irg <= 0 || org <= 0 || !d.after(DateUtil.minDate)) return(0.0);
			double dd = CostCalculation.getWaCost(br.getSessionHelper(),irg, org, d);
//			if(Double.isNaN(dd)) {
//				UniLog.log("Average Cost is NAN, show last avcost");
//				dd = CostCalculation.getLastWaCost(br.getSessionHelper(),irg, org);
//				if(testCell("stg_costvalid") != null) {
//					getCell("stg_costvalid").set("Projected");
//				}
//			} else {
//				if(testCell("stg_costvalid") != null) {
//					getCell("stg_costvalid").set("Calculated");
//				}
//			}
			return(dd);
			
		}
		case FUNC_getStmovUnitCost: {

			double firg;
			int irg;
			double forg;
			int org;
			if(p_args.get(0) instanceof Double) {
				firg = (Double) p_args.get(0);
				irg = (int) firg;
			} else {
				irg = (Integer) p_args.get(0);
			}
			if(p_args.get(1) instanceof Double) {
				forg = (Double) p_args.get(1);
				org = (int) forg;
			} else {
				org = (Integer) p_args.get(1);
			}
			
			Date d = (Date) p_args.get(2);
			if(irg <= 0 || org <= 0 || !d.after(DateUtil.minDate)) return(0.0);
			String tdType = (String) p_args.get(3);
			if(StringUtils.isBlank(tdType)) return(0.0);
			double dd = 0.0;
			if(stkInQty.contains(tdType)) {
				dd = (Double) p_args.get(4);
			} else {
				dd = CostCalculation.getWaCost(br.getSessionHelper(),irg, org, d);
			}
			return(dd);
			
		}
		case FUNC_getLocBalance: {

			double firg;
			int irg;
			double forg;
			int org;
			if(p_args.get(0) instanceof Double) {
				firg = (Double) p_args.get(0);
				irg = (int) firg;
			} else {
				irg = (Integer) p_args.get(0);
			}
			if(irg <= 0) return(0.0);
			if(p_args.get(1) instanceof Double) {
				forg = (Double) p_args.get(1);
				org = (int) forg;
			} else {
				org = (Integer) p_args.get(1);
			}
			if(org <= 0) return(0.0);
			String loc = (String) p_args.get(2);
			if(loc == null || loc.equals("")) return(0.0);
			Date d;
			if(p_args.size() >= 4) {
				d = (Date) p_args.get(3);
				if(d == null || !d.after(DateUtil.minDate)) return(0.0);
			} else {
				d = DateUtil.prevday(DateUtil.maxDate);
			}
			if(irg <= 0 || org <= 0 || !d.after(DateUtil.minDate)) return(0.0);
			return(CostCalculation.getLocBalance(br.getSessionHelper(),irg, org, loc,d));
			
		}
		case FUNC_getConfig: {
				String s = Erpv4Config.getString(br.getSessionHelper(),(String) p_args.get(0));
				if(s == null) return("") ; else return(s);
			}
		case FUNC_callsegi: {
				RpcClient rpc = br.getSelectUtil().getRpcClient();
				Vector args = (Vector) p_args.clone();
				args.remove(0);
				Value v = rpc.callSegment((String) p_args.get(0), args);
				if(v != null) return(v.toInt()); else return(0);
			}
		case FUNC_callsegs: {
				RpcClient rpc = br.getSelectUtil().getRpcClient();
				Vector args = (Vector) p_args.clone();
				args.remove(0);
				Value v = rpc.callSegment((String) p_args.get(0), args);
				if(v != null) return(v.toString()); else return("");
			}

		case FUNC_convToRef4: {
				Date expD = (Date) p_args.get(0);
				String lotNo = (String) p_args.get(1);
				return(convToRef4(expD, lotNo));
		}
		case Func_convToRef4Ex: {
				Date expD = (Date) p_args.get(0);
				String lotNo = (String) p_args.get(1);
				String s = convToRef4(expD, lotNo);
				if(s == null || s.isEmpty()) {
					return(p_args.get(2));
				} else {
					return(s);
				}
		}
		case FUNC_ref4ToExpD: {
				String ref4 = (String) p_args.get(0);
				return(ref4ToExpd(ref4));
		}
		case FUNC_ref4ToLotNo : {
				String ref4 = (String) p_args.get(0);
				return(ref4ToLotNo(ref4));
		}
		
		case FUNC_getRemoteFreeStock : {
			String remoteAgent=(String) p_args.get(0);
			double firg;
			int irg;
			double forg;
			int org;
			if(p_args.get(1) instanceof Double) {
				firg = (Double) p_args.get(1);
				irg = (int) firg;
			} else {
				irg = (Integer) p_args.get(1);
			}
			if(irg <= 0) return(0.0);
			if(p_args.get(2) instanceof Double) {
				forg = (Double) p_args.get(2);
				org = (int) forg;
			} else {
				org = (Integer) p_args.get(2);
			}
			double freeStock = RecSyncErpv4.erpv4GetRemoteFreeStock(br.getSessionHelper().getAgent(),remoteAgent,irg,org,RecSyncErpv4.STOCKSTATUS.FREESTOCK);
			return(freeStock);
		}
		/*
		case FUNC_getLoginProperty : {
			try {
				Erpv4SessionHelper.PROPNAME pn = Erpv4SessionHelper.PROPNAME.valueOf((String) p_args.get(0));
				return(((Erpv4SessionHelper) br.getSessionHelper()).getLoginProperty(pn));
			} catch(Exception ex) {
				//remark: if enum not exist, will got exception here.
				UniLog.log("getLoginProperty " + p_args.get(0) + " Failed");
				return(null);
			}
		}
		*/
		case FUNC_getRemoteOnOrder: {
			String remoteAgent=(String) p_args.get(0);
			double firg;
			int irg;
			double forg;
			int org;
			if(p_args.get(1) instanceof Double) {
				firg = (Double) p_args.get(1);
				irg = (int) firg;
			} else {
				irg = (Integer) p_args.get(1);
			}
			if(irg <= 0) return(0.0);
			if(p_args.get(2) instanceof Double) {
				forg = (Double) p_args.get(2);
				org = (int) forg;
			} else {
				org = (Integer) p_args.get(2);
			}
			double freeStock = RecSyncErpv4.erpv4GetRemoteFreeStock(br.getSessionHelper().getAgent(),remoteAgent,irg,org,RecSyncErpv4.STOCKSTATUS.ONORDER);
			return(freeStock);
		}
		case FUNC_getUniqueField: {
			/*
			Object o = (Double) p_args.get(0);
			double di = (Double) p_args.get(0);
			int rg = (int) di;
			*/
			int rg = Cell.objectToInt(p_args.get(0));
			String tabName = (String) p_args.get(1);
			String fieldName = (String) p_args.get(2);
			String format = (String) p_args.get(3);
			if(rg <= 0) return("");
			if(StringUtils.isBlank(tabName)) return("");
			if(StringUtils.isBlank(fieldName)) return("");
			if(StringUtils.isBlank(format)) return("");
			boolean reuse = true;
			if(p_args.size() > 4) reuse = (Boolean) p_args.get(4);
			String s = BiSchema.getUniqueField(br,tabName,fieldName,format,rg,reuse);
			return(s);
		}
		case FUNC_urlToHtmlImage : {
			String url = (String) p_args.get(0);
			if(StringUtils.isBlank(url)) return("");
			int width = 0;
			int height = 0;
			String alt = null;
			if(p_args.size() > 1) width = Cell.objectToInt(p_args.get(1));
			if(p_args.size() > 2) height = Cell.objectToInt(p_args.get(2));
			if(p_args.size() > 3) alt = (String) p_args.get(3);
			String imgTag = "<img src=\""+url+"\"";
			if(width > 0) imgTag+=" width=\""+width+"\"";
			if(height > 0) imgTag+=" height=\""+height+"\"";
			if(!StringUtils.isBlank(alt)) imgTag+=" alt=\""+alt+"\"";
			imgTag += "/>";
			return(imgTag);
		}
//		case FUNC_ResourceUrlToHtmlImage : {
//			String contextPath = (String) p_args.get(0);
//			String url = (String) p_args.get(1);
//			int width = 0;
//			int height = 0;
//			String alt = null;
//			if(p_args.size() > 2) width = Cell.objectToInt(p_args.get(2));
//			if(p_args.size() > 3) height = Cell.objectToInt(p_args.get(3));
//			if(p_args.size() > 4) alt = (String) p_args.get(4);
//			String imgTag = "<img src=\""+contextPath+"?url="+url+"\"";
//			if(width > 0) imgTag+=" width=\""+width+"\"";
//			if(height > 0) imgTag+=" height=\""+height+"\"";
//			if(!StringUtils.isBlank(alt)) imgTag+=" alt=\""+alt+"\"";
//			imgTag += "/>";
//		}
		case FUNC_voicemsgToResourceUrl: {
			String messageGroup = (String) p_args.get(0);
			int messageId = Cell.objectToInt(p_args.get(1));
			String suffix = (String) p_args.get(2);
			if(StringUtils.isBlank(messageGroup) || messageId == 0 || StringUtils.isBlank(suffix)) return("");
			String ss = (String.format("message://%s/%d/%s&ext=%s", messageGroup,messageId,suffix,suffix.toLowerCase()));
			return(ss);
		}		
		case FUNC_filingToHtmlImage: {
			int maxH = 300;
			int maxW = 300;
			String base64Img = null;
			try{
				String key = (String) p_args.get(0);
				String filingTable = null;
				if(p_args.size() > 1) {
					maxH = Cell.objectToInt(p_args.get(1));
					maxW = Cell.objectToInt(p_args.get(2));
				}
				if(p_args.size() > 3) {
					if(!StringUtils.isBlank((String) p_args.get(3))) {
						filingTable = (String) p_args.get(3);
					}
				}
				
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				FilingUtilObject fo = FilingUtil.getFile(br.getSessionHelper().getAgent(), filingTable, key, bos);
				bos.close();
				//TODO: obtain image
//				base64Img = ImageUtil.getBase64ImageString(new FileInputStream(ZkUtil.getWebContentRealPath("/images/logo/banner_hw.jpg",false)),"image/jpeg");
				if(fo != null) {
				ByteArrayInputStream ios = new ByteArrayInputStream(bos.toByteArray());
				base64Img = ImageUtil.getBase64ImageString(ios ,"image/jpeg");
//				base64Img = ImageUtil.getBase64ImageString(ios ,"image/png");
				ios.close();
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			if (base64Img != null){
				return("<img src=\""+ base64Img + "\" style=\"max-width:"+maxW+"px; max-height:"+maxH+"px;\" alt=\"Image\" />");
			}
			
			return("");
		}
		case FUNC_getGlBeginBal: 
		case FUNC_getGlEndBal: 
		case FUNC_getGlPosBeginBal:
		case FUNC_getGlPosEndBal:
		case FUNC_getGlNegBeginBal:
		case FUNC_getGlNegEndBal:
				{
			Date bdate = (Date) p_args.get(1);
			if(DateUtil.minDate.after(bdate)) return(0.0);
			String cocode = Erpv4Config.getDefaultCoCode(br.getSessionHelper());
			String ano = (String) p_args.get(0);
			GlBalanceCalculation.BalanceAccumulator acu = GlBalanceCalculation.getCaBalanceAccumulator(br.getSessionHelper(), cocode, ano);
			if(acu == null) return(0.0);
			/*
			switch(funcName) {
			case FUNC_getGlEndBal: 
			case FUNC_getGlPosEndBal: 
			case FUNC_getGlNegEndBal: 
					bdate = new Date (bdate.getTime() + 4320000L);
			}
			*/
			switch (funcName){
			case FUNC_getGlBeginBal: return(acu.getBalanceBegin(bdate));
			case FUNC_getGlEndBal: return(acu.getBalanceEnd(bdate));
			case FUNC_getGlPosBeginBal: return(acu.getPosBalanceBegin(bdate));
			case FUNC_getGlPosEndBal: return(acu.getPosBalanceEnd(bdate));
			case FUNC_getGlNegBeginBal: return(acu.getNegBalanceBegin(bdate));
			case FUNC_getGlNegEndBal: return(acu.getNegBalanceEnd(bdate));
			default: return(0.0);
			}
		}
		case FUNC_getGlDaBeginBal: 
		case FUNC_getGlDaEndBal: 
		case FUNC_getGlDaPosBeginBal:
		case FUNC_getGlDaPosEndBal:
		case FUNC_getGlDaNegBeginBal:
		case FUNC_getGlDaNegEndBal:
		case FUNC_getGlDaBeginlBal: 
		case FUNC_getGlDaEndlBal: 
		case FUNC_getGlDaPosBeginlBal:
		case FUNC_getGlDaPosEndlBal:
		case FUNC_getGlDaNegBeginlBal:
		case FUNC_getGlDaNegEndlBal:
				{
			Date bdate = (Date) p_args.get(2);
			if(DateUtil.minDate.after(bdate)) return(0.0);
			String cocode = Erpv4Config.getDefaultCoCode(br.getSessionHelper());
			String ano = (String) p_args.get(0);
			String ccy = (String) p_args.get(1);
			GlBalanceCalculation.BalanceAccumulatorPair acuPair = GlBalanceCalculation.getDaBalanceAccumulator(br.getSessionHelper(), cocode, ano,ccy);
			if(acuPair == null) return(0.0);
			/*
			switch(funcName) {
			case FUNC_getGlEndBal: 
			case FUNC_getGlPosEndBal: 
			case FUNC_getGlNegEndBal: 
					bdate = new Date (bdate.getTime() + 4320000L);
			}
			*/
			switch (funcName){
			case FUNC_getGlDaBeginBal: return(acuPair.getCacu().getBalanceBegin(bdate));
			case FUNC_getGlDaEndBal: return(acuPair.getCacu().getBalanceEnd(bdate));
			case FUNC_getGlDaPosBeginBal: return(acuPair.getCacu().getPosBalanceBegin(bdate));
			case FUNC_getGlDaPosEndBal: return(acuPair.getCacu().getPosBalanceEnd(bdate));
			case FUNC_getGlDaNegBeginBal: return(acuPair.getCacu().getNegBalanceBegin(bdate));
			case FUNC_getGlDaNegEndBal: return(acuPair.getCacu().getNegBalanceEnd(bdate));
			case FUNC_getGlDaBeginlBal: return(acuPair.getLacu().getBalanceBegin(bdate));
			case FUNC_getGlDaEndlBal: return(acuPair.getLacu().getBalanceEnd(bdate));
			case FUNC_getGlDaPosBeginlBal: return(acuPair.getLacu().getPosBalanceBegin(bdate));
			case FUNC_getGlDaPosEndlBal: return(acuPair.getLacu().getPosBalanceEnd(bdate));
			case FUNC_getGlDaNegBeginlBal: return(acuPair.getLacu().getNegBalanceBegin(bdate));
			case FUNC_getGlDaNegEndlBal: return(acuPair.getLacu().getNegBalanceEnd(bdate));
				
			default: return(0.0);
			}
		}
		case FUNC_getPoFromAllocate: {
			int org = Cell.objectToInt(p_args.get(0));
			if(org == 0) return("");
			int qorg = Cell.objectToInt(p_args.get(1));
			if(qorg == 0) return("");
			int irg = Cell.objectToInt(p_args.get(2));
			if(irg == 0) return("");
			if(org >= GenbucketUtil.WEIGHTED_AVERAGE_ORGMIN) return("");
			TableRec tr = br.getSelectUtil().getQueryResult("select * from stmovd,stmov where stmd_org = ? and stmd_flag1 = 'Y' and stm_mrg = stmd_mrg",
						new Wherecl().appendArgument(org)
					);
			if(tr.getRecordCount() == 1) {
				return(tr.getFieldString("stm_ref1"));
			}
			return("");
		}
			
				
		}
		return(super.evalFunction(p_fname,p_args) );
	}
	
	protected String ref4ToLotNo(String p_ref4) {
		int idx = p_ref4.indexOf(':');
		if(idx < 0) return("");
		String ss  = p_ref4.substring(idx+1);
		if(ss.length() > 18) ss = ss.substring(0,18);
		return(ss);
	}
	protected Date ref4ToExpd(String p_ref4) {
		String dStr = StringUtil.strpart(p_ref4, 0, 10);
		Date d = DateUtil.dateTimeStrToDate(dStr,false);
		if(d == null) return(DateUtil.zeroDate);
		return(d);
	}
	protected String convToRef4(java.util.Date p_date, String p_lotNo) {
		String ref4;
		if(p_date.after(DateUtil.minDate)) {
			ref4 = DateUtil.dateToDateTimeStr(p_date, "yyyy/MM/dd")+":"+p_lotNo;
		} else {
			if(!p_lotNo.trim().equals("")) {
				ref4 = "          :"+p_lotNo;
			} else {
				ref4 = "";
			}
		}
		return(ref4);
	}
	
	public double getXrateByDate(String p_cocode,String p_ccy,Date p_date) throws Exception {
		String cocode;
		if(p_cocode != null) {
			cocode = p_cocode;
		} else {
			cocode = Erpv4Config.getDefaultCoCode(br.getSessionHelper());
		}
		String bccy = Erpv4Config.getBaseCcy(br.getSessionHelper(),cocode);
		if(bccy == null) return(1.0);
		if(rateUtil == null || !rateUtil.getBaseCid().equals(bccy)) {
			rateUtil = new BxRateUtil(bccy);
		}
		Double xrate = rateUtil.getRateByDate(p_ccy,p_date,br.getSelectUtil());
		if(xrate == null) xrate = 1.0;
		return(xrate);
	}
}
