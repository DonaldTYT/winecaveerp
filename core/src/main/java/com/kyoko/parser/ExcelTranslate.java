package com.kyoko.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.kyoko.parser.Condition.DualExpression;
import com.kyoko.parser.excelformula.CellPositionInterface;
import com.kyoko.parser.excelformula.ColumnTranslateInterface;
import com.kyoko.parser.excelformula.ExcelCellRef;
import com.kyoko.parser.excelformula.ExcelRange;
import com.uniinformation.cell.Cell;
import com.kyoko.common.CoreLog;
import com.uniinformation.utils.VectorUtil;

public class ExcelTranslate {
	private static enum ExcelFuncName { NOT_DEFINED }
	static HashMap<String, String> OneOneMappedFunctions;
	
	
	static Condition translateExcelConditionToBi (Condition p_cond,VariableInterface biVariableInterface,FunctionInterface biFunctionInterface,ColumnTranslateInterface xlsToBiColumnMapper) throws Exception {
		if(!p_cond.get_isPredicate()) {
			Condition rightCond = translateExcelConditionToBi ((Condition) p_cond.roperend,biVariableInterface,biFunctionInterface,xlsToBiColumnMapper);
			if(p_cond.loperend == null) {
				return(new Condition(p_cond.get_operator(),rightCond));
			} else {
				Condition leftCond = translateExcelConditionToBi ((Condition) p_cond.loperend,biVariableInterface,biFunctionInterface,xlsToBiColumnMapper);
				return(new Condition(leftCond,p_cond.get_operator(),rightCond));
			}
		} else {
			if(p_cond.get_operator() == 0) return(new Condition (( Boolean ) p_cond.loperend)); 
			Expression leftExpr = translateFromXlsToBi((Expression) p_cond.loperend,biVariableInterface,biFunctionInterface,xlsToBiColumnMapper);
			if(p_cond.roperend == null) return(new Condition(leftExpr,p_cond.get_operator()));
			if(p_cond.roperend instanceof DualExpression) {
				DualExpression dualExpr = (DualExpression) p_cond.roperend;
				Expression rightExpr1 = translateFromXlsToBi((Expression) dualExpr.exp1,biVariableInterface,biFunctionInterface,xlsToBiColumnMapper);
				Expression rightExpr2 = translateFromXlsToBi((Expression) dualExpr.exp2,biVariableInterface,biFunctionInterface,xlsToBiColumnMapper);
				return(new Condition(Condition.COMPARE_MODE_IGNORECASE|Condition.COMPARE_MODE_STRICTTYPE,leftExpr,p_cond.get_operator(),rightExpr1,rightExpr2));
			} else {
				Expression rightExpr = translateFromXlsToBi((Expression) p_cond.roperend,biVariableInterface,biFunctionInterface,xlsToBiColumnMapper);
				return(new Condition(Condition.COMPARE_MODE_IGNORECASE|Condition.COMPARE_MODE_STRICTTYPE,leftExpr,p_cond.get_operator(),rightExpr));
			}
		}
	}
			
	static Expression translateOneOneMapFunction(String p_toName,Function fn,VariableInterface biVariableInterface,FunctionInterface biFunctionInterface,ColumnTranslateInterface xlsToBiColumnMapper) throws Exception {
		Expression expr0 = null;
		Vector argList = null;
		if(fn.argList != null) {
		argList = new Vector();
		for(Object arg : fn.argList) {
			if(arg instanceof Expression) {
					argList.add(translateFromXlsToBi((Expression) arg,biVariableInterface,biFunctionInterface,xlsToBiColumnMapper));
			} else if(arg instanceof Condition) {
					argList.add(translateExcelConditionToBi ((Condition) arg,biVariableInterface,biFunctionInterface,xlsToBiColumnMapper));
			} else if(arg instanceof ExcelRange) {
				/*
					argList.add(((ExcelRange) arg).getStartVariable(biVariableInterface, xlsToBiColumnMapper));
					argList.add(((ExcelRange) arg).getEndVariable(biVariableInterface, xlsToBiColumnMapper));
					*/
				argList.add(
//						new VariableSet(
//								new Expression (
//										((ExcelRange) arg).getStartVariable(biVariableInterface, xlsToBiColumnMapper)
//										),
//								new Expression (
//										((ExcelRange) arg).getEndVariable(biVariableInterface, xlsToBiColumnMapper)
//										)
//								)
						
						new VariableSet(
								((ExcelRange) arg).getStartVariable(biVariableInterface, xlsToBiColumnMapper),
								((ExcelRange) arg).getEndVariable(biVariableInterface, xlsToBiColumnMapper)
								)
						);
			}
		}
		} 
		expr0 = new Expression(new Function(p_toName,biFunctionInterface, argList));
		return(expr0);
	}
	public static Expression translateFromXlsToBi(Expression p_expr,VariableInterface biVariableInterface,FunctionInterface biFunctionInterface,ColumnTranslateInterface xlsToBiColumnMapper) throws Exception {
		if(OneOneMappedFunctions == null) {
			synchronized(ExcelTranslate.class) {
				OneOneMappedFunctions = new HashMap<String,String>();
				OneOneMappedFunctions.put("ABS", "fabs");
				OneOneMappedFunctions.put("YEAR", "yearofdate");
				OneOneMappedFunctions.put("MONTH", "monthofdate");
				OneOneMappedFunctions.put("DAY", "dayofdate");
				OneOneMappedFunctions.put("TODAY", "today");
				OneOneMappedFunctions.put("EDATE", "nextMonth");
				OneOneMappedFunctions.put("IF", "if");
			}
		}
		Expression expr0 = null;
		
		switch(p_expr.type) {
		case Expression.OPERENDTYPE_EXPRESSION:
			Expression expr1 = p_expr.loperend == null ? null : translateFromXlsToBi((Expression) p_expr.loperend,biVariableInterface,biFunctionInterface,xlsToBiColumnMapper);
			Expression expr2 = p_expr.roperend == null ? null : translateFromXlsToBi((Expression) p_expr.roperend,biVariableInterface,biFunctionInterface,xlsToBiColumnMapper);
			if(p_expr.operator == Expression.OPERATOR_AND) {
				expr0 = new Expression(new Function("strcat",biFunctionInterface,
						new VectorUtil()
							.addElement(expr1)
							.addElement(expr2)
							.toVector()
						));
			} else {
				expr0 = new Expression(expr1,p_expr.operator,expr2);
			}
			break;
		case Expression.OPERENDTYPE_FUNCTION:
			String mappedFunction = OneOneMappedFunctions.get(((Function )p_expr.loperend).fnName);
			if(mappedFunction != null) {
				return(translateOneOneMapFunction(mappedFunction,(Function) p_expr.loperend,biVariableInterface,biFunctionInterface,xlsToBiColumnMapper));
			}
			ExcelFuncName funcName = ExcelFuncName.NOT_DEFINED;
			try {
				funcName = ExcelFuncName.valueOf("XLSFUNC_"+((Function )p_expr.loperend).fnName);
				switch (funcName){
//				case XLSFUNC_SUM: {
//					Function fn = (Function) p_expr.loperend;
//					Vector argList = new Vector();
//					for(Object arg : fn.argList) {
//						if(arg instanceof Expression) {
//							argList.add(translateFromXlsToBi((Expression) arg,biVariableInterface,biFunctionInterface,xlsToBiColumnMapper));
//						} else if(arg instanceof ExcelRange) {
//							argList.add(((ExcelRange) arg).getStartVariable(biVariableInterface, xlsToBiColumnMapper));
//							argList.add(((ExcelRange) arg).getEndVariable(biVariableInterface, xlsToBiColumnMapper));
//						}
//					}
//				}
				default:
				return(null);
				}
			}
			catch(Exception ex) {
				//remark: if enum not exist, will got exception here.
				// throw new Exception("Excel Function " + ((Function )p_expr.loperend).fnName + " not supported");
//				CoreLog.log("Excel Function " + ((Function )p_expr.loperend).fnName + " not supported");
				return(translateOneOneMapFunction(
						"excel"+((Function )p_expr.loperend).fnName
						,(Function) p_expr.loperend,biVariableInterface,biFunctionInterface,xlsToBiColumnMapper));
			}
		case Expression.OPERENDTYPE_CONSTANT:
			expr0 = new Expression((Cell) p_expr.loperend);
			break;
		case Expression.OPERENDTYPE_VARIABLE:
			ExcelCellRef xlsCell = (ExcelCellRef) p_expr.loperend;
			int colRef = xlsCell.getColIdx();
			boolean isRowAbsolute = xlsCell.getRowAbsolute();
			String workSheet = xlsCell.getWorkSheet();
			Variable val;
			Pair<Boolean,Integer>[] zeropos = ExcelCellRef.decodeExcelRC(xlsToBiColumnMapper.getWorkSheetFirstValuePosition(workSheet)) ;
			int c = xlsCell.getColIdx() - zeropos[0].getRight();;
			String colName = xlsToBiColumnMapper.cellColumnToBiColumn(xlsCell.getWorkSheet(), c);
			CoreLog.log("Convert column number to name [" + xlsCell.getWorkSheet()+"] " + c + " -> " + colName);
			if(workSheet!= null && workSheet.equals(xlsToBiColumnMapper.getCurrentSheetName())) workSheet = null;
			if(workSheet != null) colName = normalizeTag(workSheet)+"!"+colName;
			if(isRowAbsolute) {
				int r = xlsCell.getRowIdx() - zeropos[1].getRight();
//				val = new Variable("C"+colRef,biVariableInterface,r,true);
				val = new Variable(colName,biVariableInterface,r,true);
			} else {
				int rowRef = xlsCell.getRowIdx();
				Pair<Boolean,Integer>[] zeropos0 = ExcelCellRef.decodeExcelRC(xlsToBiColumnMapper.getWorkSheetFirstValuePosition(null)) ;
				rowRef = rowRef + zeropos0[1].getRight() - zeropos[1].getRight();
				if(rowRef == 0) {
					val = new Variable(
							colName
							,biVariableInterface);
				} else {
					val = new Variable(
							colName
							,biVariableInterface,rowRef,false);
				}
			}
			expr0 = new Expression(val);
//			return("C"+colRef);
			break;
		}
		return(expr0);
	}
	
	public static String normalizeTag(String p_tag) throws Exception {
		if (StringUtils.isBlank(p_tag)) return "";
		CoreLog.log1("called tag:%s", p_tag);
		
		//pre validation, required for handle excel formula sheetname
		if (!p_tag.matches("^[a-zA-Z0-9_ -]*$")) {
			String errMsg = String.format("invalid name(%s). It contain unsupported character", p_tag);
			CoreLog.log1(errMsg);
			throw new Exception(errMsg);
		}
		
		
		//normalize
		String newTag = p_tag;
		newTag = p_tag.replaceAll("[ ]", "_").toLowerCase();
		newTag = newTag.replaceAll("\\$", "dols");  //special handle dollar sign
		newTag = newTag.replaceAll("[^a-zA-Z0-9_]*", "");
		newTag = newTag.replaceAll("^[_]*", "");  //remove _ from left
		newTag = newTag.replaceAll("[_]*$", "");  //remove _ from right
		CoreLog.log1("newtag:%s",  newTag);
		
		
		//validation
		if (newTag.length() < 2) {
			String errMsg = String.format("invalid name(%s). length at least 2", p_tag);
			CoreLog.log1(errMsg);
			throw new Exception(errMsg);
		}
		if (!newTag.matches("^[a-z].*")){
			String errMsg = String.format("invalid name(%s). first letter must be alphabet", p_tag);
			CoreLog.log1(errMsg);
			throw new Exception(errMsg);
		}
		return newTag;
	}
	
}
