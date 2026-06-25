package com.kyoko.parser.excelformula;

import com.kyoko.parser.*;
import com.kyoko.common.CoreLog;

public class TestParser {
	public static void main(String args[]){
		CoreLog.log("TestParser");
		CellPositionInterface cif = new CellPositionInterface() {

			@Override
			public int getColIdx() {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int getRowIdx() {
				// TODO Auto-generated method stub
				return 0;
			}
			
		};
		try {
			Parser yyparser = new Parser(cif);
//			Expression result = (Expression) yyparser.parse("SUM(5:6,\"aa\")");
//			Expression result = (Expression) yyparser.parse("sum('b1_s bb'!F1:G2)");
//			Expression result = (Expression) yyparser.parse("\'AA\'!F1");
//			Expression result = (Expression) yyparser.parse("sum('abc def'!$G$1,'defgh 123'!$G$1:$H$1)");
//			Expression result = (Expression) yyparser.parse("'abc def'!G1");
//			Expression result = (Expression) yyparser.parse("sum(abcdef!$G$1:$F22,defgh123!$G$1:$H$1)");
//			Expression result = (Expression) yyparser.parse("sum(abcdef!$G$1:$F22,defgh123!$G$1:$H$1)");
//			Expression result = (Expression) yyparser.parse("sum(abcdef-xx!$G$1,defgh123!$G$1:$H$1) + G5 - xx!H6");
			//Expression result = (Expression) yyparser.parse("VLOOKUP('.5'!B1:B1,'ABC xx'!A2)");
//			Expression result = (Expression) yyparser.parse("COUNTIFS(B1:D5,\"ABC\" & C5)");
//			Expression result = (Expression) yyparser.parse("D4");
//			Expression result = (Expression) yyparser.parse("IF(OR(N2=\"live\",N2=\"auto\"),MONTH(P2),\"\")");
//			Expression result = (Expression) yyparser.parse("IF(OR(A1=1,A2=3),MONTH(P2),\"\")");
			Expression result = (Expression) yyparser.parse("IF(OR(XX-2!A1-2=1,A2=3),MONTH(P2),\"\")");
			CoreLog.log(result.toString());
		} catch (Exception ex){ 
			CoreLog.log(ex);
		}
	}
}
