package com.uniinformation.bicore;

import java.util.Hashtable;

import com.uniinformation.webcore.SessionHelper;

public class SummaryCache {
	static class SummaryAggregate {
		AggregateOrPivot.AGGREGATES agg;
		BiField field;
		Hashtable<Object,Object> values;
		public SummaryAggregate() {
			values = new Hashtable<Object,Object>();
		}
	}
	static String makeSummaryName(AggregateOrPivot.AGGREGATES p_agg,BiColumn p_column) {
		return(
				p_agg.toString() + "(" + p_column.getLabel() + ")"
		);
	}
	static Hashtable<BiView,Hashtable> scViewHash = new Hashtable<BiView,Hashtable>();
	static public Object getSummary(BiResult p_br,AggregateOrPivot.AGGREGATES p_aggregate,BiColumn p_bc) {
		Hashtable<BiView,Hashtable> mcv =  scViewHash.get(p_br.getView());
		if(mcv == null) {
			synchronized(scViewHash) {
				mcv = scViewHash.get(p_br.getView());
				if(mcv == null) {
					mcv = new Hashtable<BiView,Hashtable>();
					scViewHash.put(p_br.getView(), mcv);
				}
			}
		}
		Hashtable<String,SummaryAggregate> dcv = mcv.get(p_bc.getView());
		if(dcv == null) {
			synchronized(mcv) {
				dcv = mcv.get(p_bc.getView());
				if(dcv == null) {
//					dcv = new Hashtable<String,SummaryAggregate>();
					dcv = new Hashtable<String,SummaryAggregate>();
					mcv.put(p_bc.getView(),dcv);
				}
			}
		}
		String summaryName = makeSummaryName(p_aggregate,p_bc);
		SummaryAggregate sa = dcv.get(summaryName);
		if(sa == null) {
			synchronized(dcv) {
				sa = dcv.get(summaryName);
				if(sa == null) {
					sa = new SummaryAggregate();
					dcv.put(summaryName,sa);
				}
			}
		}
		/*
		Object val = sa.values.get(p_value);
		if(val == null) {
			synchronized(dcv) {
				val = sa.values.get(p_value);
				if(val == null) {
					// Do fetch record here
					sa.values.put(p_value,val);
				}
			}
		}
		return(val);
		*/
		return(null);
	}
}
