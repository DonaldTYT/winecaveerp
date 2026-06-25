package com.uniinformation.utils.exprpar;

import com.uniinformation.utils.UniLog;

public class TestParser {
	public static void main(String args[]){
		UniLog.log("TestParser");
//		Parser p = new Parser("if(axaclm_dependno > 0,sprintf('%s-%d',axaclm_certno,axaclm_dependno),axaclm_certno)");
//		Parser p = new Parser("fn(abc.def)");
//		Parser p = new Parser(0,"xyz!abc[]");
//		Parser p = new Parser(0,"abc[*]");
//		Parser p = new Parser("fn([a1[0]:b2[0]])");
//		Parser p = new Parser(0,"[[a1[0]:a2[]]");
//		Parser p = new Parser("abc");
//		UniLog.log(p.getExpression().toString());
		Parser p = new Parser(0,"fn(a1[*])");
	}
}
