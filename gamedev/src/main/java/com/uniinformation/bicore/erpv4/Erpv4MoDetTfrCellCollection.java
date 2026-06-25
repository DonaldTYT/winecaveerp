package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.erpv4.CostCalculation;

public class Erpv4MoDetTfrCellCollection extends Erpv4StmdCellCollection {

	public Erpv4MoDetTfrCellCollection(BiCellCollection p_parent, BiResultErpv4 p_br) {
		super(p_parent, p_br);
		// TODO Auto-generated constructor stub
	}
	private enum FuncName { FUNC_getKiStmdLoc, FUNC_getKiStmdBin,FUNC_ref4ToStmref,FUNC_convToKiRef4,NOT_DEFINED }
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
		case FUNC_getKiStmdBin: {
				String fromLoc = (String) p_args.get(0);
				if(fromLoc == null) return("");
				String toLoc = (String) p_args.get(1);
				if(toLoc == null) return("");
				Boolean transit = (Boolean) p_args.get(2);
				if(transit == null) return("");
				Boolean tfronly = (Boolean) p_args.get(3);
				if(tfronly == null) return("");
				Double dd = (Double) p_args.get(4);
				if(dd == null) return("");
				int outOrIn = (int) dd.doubleValue();
				if(outOrIn != 0) { 
					if(!tfronly && !transit) return(toLoc);
				} else {
					if(!tfronly && !transit) return(fromLoc);
				}
				return("");
		}
		case FUNC_getKiStmdLoc: {
				String fromLoc = (String) p_args.get(0);
				if(fromLoc == null) return("");
				String toLoc = (String) p_args.get(1);
				if(toLoc == null) return("");
				Boolean transit = (Boolean) p_args.get(2);
				if(transit == null) return("");
				Boolean tfronly = (Boolean) p_args.get(3);
				if(tfronly == null) return("");
				Double dd = (Double) p_args.get(4);
				if(dd == null) return("");
				int outOrIn = (int) dd.doubleValue();
				if(tfronly) return(toLoc);
				if(outOrIn != 0) { 
				if(fromLoc.equals("CTL01")) return("CTL03");
				if(fromLoc.equals("HZ01")) return("HZ02");
				if(fromLoc.equals("TST01")) return("TST06");
				if(fromLoc.equals("DVR01")) return("DVR02");
				if(fromLoc.equals("DSC01")) return("DSCT1");
				if(fromLoc.equals("PPDVR0")) return("PPDVRT");
				if(fromLoc.equals("DSC02")) return("DSCT2");
				if(fromLoc.equals("PO001")) return("PO005");
				if(fromLoc.equals("NC001")) return("NC005");
				if(fromLoc.equals("PANA01")) return("PANA05");
				} else {
				if(toLoc.equals("CTL01")) return("CTL03");
				if(toLoc.equals("HZ01")) return("HZ02");
				if(toLoc.equals("TST01")) return("TST06");
				if(toLoc.equals("DVR01")) return("DVR02");
				if(toLoc.equals("DSC01")) return("DSCT1");
				if(toLoc.equals("PPDVR0")) return("PPDVRT");
				if(toLoc.equals("DSC02")) return("DSCT2");
				if(toLoc.equals("PO001")) return("PO005");
				if(fromLoc.equals("NC001")) return("NC005");
				if(fromLoc.equals("PANA01")) return("PANA05");
				}
				return(toLoc);
			}
		case FUNC_convToKiRef4: {
			Date expD = (Date) p_args.get(0);
			String lotNo = (String) p_args.get(1);
			String ss = convToRef4(expD, lotNo);
			if(!getCellBoolean("tloc_transit") && !getBoolean("tloc_tfronly"))  {
				if(getCellInt("stmd_nref4") == 0) {
					if(getCellBoolean("stm_ref6")) ss = String.format("%-30s%s",ss,getCellString("stm_ref1"));
				} else {
					ss = String.format("%-30s%s",ss,getCellString("stmdki_stmref"));
				}
			}
			return(ss);
		}

		case FUNC_ref4ToStmref: {
				String ref4 = (String) p_args.get(0);
				if(ref4 == null) return("");
				Boolean transit = (Boolean) p_args.get(1);
				if(transit == null) return("");
				Boolean tfronly = (Boolean) p_args.get(2);
				Double dnref4 = (Double) p_args.get(3);
				if(dnref4 == null) return("");
				int nnref4 = (int) dnref4.doubleValue();
				if(tfronly == null) return("");
				if(!transit && !tfronly) {
					if(nnref4 == 0) {
						return(getCellString("stm_ref1"));
					} else {
						return(StringUtil.strpart(ref4, 30, 20).trim());
					}
				}
				return("");
		}
		}
		return(super.evalFunction(p_fname,p_args) );
	}
//	@Override
//	protected String convToRef4(java.util.Date p_date, String p_lotNo) {
//		String ss = super.convToRef4(p_date, p_lotNo);
//		if(!getCellBoolean("tloc_transit") && !getBoolean("tloc_tfronly")) ss = String.format("%-30s%s",ss,getCellString("stmdki_stmref"));
//		return(ss);
//	}

}
