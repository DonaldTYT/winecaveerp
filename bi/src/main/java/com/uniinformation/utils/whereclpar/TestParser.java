package com.uniinformation.utils.whereclpar;

import java.util.HashSet;
import java.util.List;

import com.uniinformation.utils.UniLog;

public class TestParser {
	public static void main_XX (String args[]){
		try {
			Parser yyparser = new Parser(null, null, null);
//			Condition result= (Condition) yyparser.evaluate( "a like '%a%'");
			Condition result= (Condition) yyparser.parse( "a regexp '%a%'");
			UniLog.log(result.toString());
		} catch (Exception ex){ 
			UniLog.log(ex);
		}
	}
	public static void main(String args[]){
		UniLog.log("TestParser");
		try {
//			Parser yyparser = new Parser("(a = 1 and b = 1) or not ( c = 1 and d = 1) ", null, null);
//			Parser yyparser = new Parser("(a = 1 and b = 1) or ( c = 1 and d = 1) ", null, null);
			Parser yyparser = new Parser(null, null, null);
//			Condition result= (Condition) yyparser.evaluate( "(a = 1 and b = 1) or ( c = 1 and (d = 1 or e = 1)) ");
			Condition result= (Condition) yyparser.parse( "(a = 1 or a = 2) and (b = 3)");
//			Condition result= (Condition) yyparser.evaluate( "a = 1 and b = 1 and c = 1");
			UniLog.log(result.toString());
			List<Condition> l1 = result.serializeCondition(false, result);
			l1 = Condition.optimizeConditionList(l1, yyparser, false);
			String s = "";
			for(Condition cond : l1) {
				UniLog.log(cond.toString());
				if(s.equals("")) s = cond.toString(); else s += " and " + cond.toString();
			}
			UniLog.log("optimized in ((Or) And (Or)) format");
			UniLog.log(s);
			UniLog.log("reverse");
			result = (Condition) yyparser.parse(s);
			UniLog.log(result.toString());
			l1 = result.serializeCondition(true, result);
			l1 = Condition.optimizeConditionList(l1, yyparser, true);
			s = "";
			for(Condition cond : l1) {
				UniLog.log(cond.toString());
				if(s.equals("")) s = cond.toString(); else s += " or " + cond.toString();
			}
			UniLog.log("optimized in ((And) Or (And)) format");
			UniLog.log(s);
			/*
			HashSet hs = new HashSet();
			hs.add("a=1");
			hs.add("b=1");
			hs.add("c=1");
			hs.add("c=1");
			hs.add("c=1");
			HashSet hs2 = new HashSet();
			hs2.add("c=1");
			hs2.add("a=1");
			hs2.add("b=1");
			HashSet hs3 = new HashSet();
			hs3.add("c=1");
			hs3.add("b=1");
			UniLog.log(hs.toString());
			UniLog.log(hs2.toString());
			UniLog.log("" + hs.equals(hs2));
			UniLog.log("" + hs.containsAll(hs2));
			*/
//			UniLog.log("HAHA_0");
//			for(int i=0;i<1000000;i++) {
//				double f = i;
//				double g = Math.ceil(f / 1.00);
//				double h = Math.floor(g * 1.0);
//				if(h != f) {
//					UniLog.log("f = " + f + " h = " + h);
//				}
//			}
//			UniLog.log("HAHA_1");
		} catch (Exception ex){ 
			UniLog.log(ex);
		}
	}
}
