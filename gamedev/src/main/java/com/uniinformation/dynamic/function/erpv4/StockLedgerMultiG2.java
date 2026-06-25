package com.uniinformation.dynamic.function.erpv4;

import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiDynamicFunction;
import com.uniinformation.cell.Cell;

public class StockLedgerMultiG2 extends BiDynamicFunction {

	BiCellCollection col;
	HashSet<String> stmdTypeSet;
	HashSet<String> exchangeLocs;
	HashSet<String> stocktakeLocs;
	public StockLedgerMultiG2(BiCellCollection p_col) {
		super(p_col);
		stmdTypeSet = new HashSet();
		stmdTypeSet.add("KO");
		stmdTypeSet.add("KI");
		stmdTypeSet.add("JI");
		stmdTypeSet.add("JO");
		exchangeLocs = new HashSet();
		exchangeLocs.add("CTL06");
		exchangeLocs.add("DVR03");
		exchangeLocs.add("HZ03");
		exchangeLocs.add("TST10");
		exchangeLocs.add("PPDVR03");
		exchangeLocs.add("PO003");
		stocktakeLocs = new HashSet();
		stocktakeLocs.add("PD000");
		stocktakeLocs.add("PP000");
		stocktakeLocs.add("GA000");
		stocktakeLocs.add("PE000");
		stocktakeLocs.add("PH000");
		stocktakeLocs.add("PA000");
		stocktakeLocs.add("PO000");
		stocktakeLocs.add("HQ000");
		col = p_col;
		// TODO Auto-generated constructor stub
	}
	@Override
	protected Object eval(List p_args) {
		// TODO Auto-generated method stub
		String ss = (String) p_args.get(0);
		if(ss.equals("trtype")) {
			String stmddesc = (String) p_args.get(7);
			String stmtype = (String) p_args.get(1);
			if(StringUtils.isBlank(stmtype)) return(stmddesc);
			String module = (String) p_args.get(2);
			if(StringUtils.isBlank(module)) return(stmddesc);
			String stmdtype = (String) p_args.get(5);
			if(StringUtils.isBlank(stmdtype)) return(stmddesc);
			String fromloc = (String) p_args.get(3);
			String toloc = (String) p_args.get(4);
			String stmdloc = (String) p_args.get(6);
			if(!stmdTypeSet.contains(stmdtype)) {
					return(stmddesc);
			} else {
				if(
						stmdtype.equals("JO") || stmdtype.equals("JI")
						) {
					if (module.equals("stake")){
						return("Stock Take Adjustment");
					}
					if( module.equals("stadj")) {
						if( (exchangeLocs.contains(fromloc) || exchangeLocs.contains(toloc))) {
							return("Supplier Exchange");
						} else {
							return(stmddesc);
							
						}
					} else {
						return(stmddesc);
					}
				} else if(
						stmdtype.equals("KO") || stmdtype.equals("KI")
						) {
					if( module.equals("stkg2")) {
						return("Stock Take Adjustment");
					}
					if(stocktakeLocs.contains(stmdloc)) {
						return("Stock Take Adjustment");
					}
					if( module.equals("sttfr")) {
						if(toloc.equals("HQ01")) {
							return("Purchase from HQ");
						}
					}
					return(stmddesc);
				}
			return(
					stmtype
					+ "_" + module
					+ "_" + fromloc
					+ "_" + toloc
					+ "_" + stmdtype
					+ "_" + stmdloc
					);
			}
		} else if(ss.equals("trtypex")) {
			String stmddesc = (String) p_args.get(7);
			String stmtype = (String) p_args.get(1);
			if(StringUtils.isBlank(stmtype)) return(stmddesc);
			String module = (String) p_args.get(2);
			if(StringUtils.isBlank(module)) return(stmddesc);
			String stmdtype = (String) p_args.get(5);
			if(StringUtils.isBlank(stmdtype)) return(stmddesc);
			String fromloc = (String) p_args.get(3);
			String toloc = (String) p_args.get(4);
			String stmdloc = (String) p_args.get(6);
			return(
					stmtype
					+ "_" + module
					+ "_" + fromloc
					+ "_" + toloc
					+ "_" + stmdtype
					+ "_" + stmdloc
					);
		}
		return ("Unknow Function");
	}
}
