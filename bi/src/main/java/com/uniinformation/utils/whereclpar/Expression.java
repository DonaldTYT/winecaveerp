package com.uniinformation.utils.whereclpar;
import java.util.HashSet;
import java.util.List;

import com.kyoko.common.DateUtil;
import com.kyoko.parser.ValueTranslationInterface;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.rpccall.Strval;
import com.uniinformation.utils.*;
public class Expression {
	public static final int OPERENDTYPE_EXPRESSION = 0;
	public static final int OPERENDTYPE_CONSTANT = 1;
	public static final int OPERENDTYPE_VARIABLE = 2;
	public static final int OPERENDTYPE_FUNCTION = 3;

	static public final int OPERATOR_PLUS  = 1;
	static public final int OPERATOR_MINUS = 2;
	static public final int OPERATOR_MULTIPLY = 3;
	static public final int OPERATOR_DIVIDE = 4;
	static public final int OPERATOR_XOR = 5;
	int operator;
	int type;
	Object loperend , roperend;
	public Expression (int p_i) { // Constant
		type = OPERENDTYPE_CONSTANT;
		operator = 0;
		loperend = new Cell(p_i);
		roperend = null;
	}
	public Expression (double p_d) { // Constant
		type = OPERENDTYPE_CONSTANT;
		operator = 0;
		loperend = new Cell(p_d);
		roperend = null;
	}
	public Expression (String p_s) { // Constant
		type = OPERENDTYPE_CONSTANT;
		operator = 0;
		loperend = new Cell(p_s);
		roperend = null;
	}
	public Expression (java.util.Date p_d) { // Constant
		type = OPERENDTYPE_CONSTANT;
		operator = 0;
		loperend = new Cell(p_d);
		roperend = null;
	}
	public Expression (Cell p_val) { // Constant
		type = OPERENDTYPE_CONSTANT;
		operator = 0;
		loperend = p_val;
		roperend = null;
	}
	public Expression (Variable p_val) { // Variable
		type = OPERENDTYPE_VARIABLE;
		operator = 0;
		loperend = p_val;
		roperend = null;
	}
	public Expression (Function p_function) {
		type = OPERENDTYPE_FUNCTION;
		operator = 0;
		loperend = p_function;
		roperend = null;
	}
	public Expression (Expression p_l, int p_operator, Expression p_r) {
		type = OPERENDTYPE_EXPRESSION;
		operator = p_operator;
		loperend = p_l;
		roperend = p_r;
	}

	public String toString() {
		if(type != 0) {
			if(type == OPERENDTYPE_CONSTANT && ((Cell) loperend).getType() == Cell.VTYPE_STRING) {
//				return("'"+loperend.toString()+"'");
				return("'"+loperend.toString().replaceAll("'", "\\\\'")+"'");
			} else if(type == OPERENDTYPE_CONSTANT && ((Cell) loperend).getType() == Cell.VTYPE_DATETIME) {
				return("'"+loperend.toString()+"'");
			} else if(type == OPERENDTYPE_CONSTANT && ((Cell) loperend).getType() == Cell.VTYPE_DATE) {
				return("'"+loperend.toString()+"'");
			} else return(loperend.toString());
		} else {
		switch(operator) {
		case OPERATOR_PLUS:
			return("( "+loperend.toString() + " + " + roperend.toString() + ")");
		case OPERATOR_MINUS:
			if(loperend == null) {
				return(" -" + roperend.toString());
			} else {
				return( "("+loperend.toString() + " - " + roperend.toString() + ")");
			}
		case OPERATOR_MULTIPLY:
			return( "("+loperend.toString() + " * " + roperend.toString() + ")");
		case OPERATOR_DIVIDE:
			return( "("+loperend.toString() + " / " + roperend.toString() + ")");
		case OPERATOR_XOR :
			return( "("+loperend.toString() + " ^ " + roperend.toString() + ")");
		}
		}
		return(null);
	}
	public HashSet getVariableHash(HashSet vSet) throws CellException {
		if(operator == 0) {
			if(type == OPERENDTYPE_VARIABLE) {
				vSet.add(((Variable) loperend).getObject());
			}
			if(type == OPERENDTYPE_FUNCTION) {
				List<Expression> argList = ((Function) loperend).argList;
				if(argList != null) {
				for(Expression expr : argList) {
					vSet = expr.getVariableHash(vSet);
				}
				}
			}
		} else {
			if(loperend != null) vSet = ((Expression)loperend).getVariableHash(vSet);
			if(roperend != null) vSet = ((Expression)roperend).getVariableHash(vSet);
		}
		return(vSet);
	}

	Cell evalArithmetic(Cell cLeft,int operator,Cell cRight) throws CellException {
		double d0,d1;
		boolean isDouble = false;
		if(cLeft == null) d0 = 0; else {
			d0 = cLeft.getDouble();
			if(cLeft.getType() == Cell.VTYPE_DOUBLE) isDouble = true;
		}
		if(cRight == null) d1 = 0; else {
			d1 = cRight.getDouble();
			if(!isDouble && cRight.getType() == Cell.VTYPE_DOUBLE) isDouble = true;
		}
		switch(operator) {
		case OPERATOR_PLUS  : {
								double dr = d0 + d1;
								if(isDouble) return(new Cell(dr)); else {
									int ir = (int) dr;
									return(new Cell(ir));
								}
							}
		case OPERATOR_MINUS : {
								double dr = d0 - d1;
								if(isDouble) return(new Cell(dr)); else {
									int ir = (int) dr;
									return(new Cell(ir));
								}
							}
		case OPERATOR_MULTIPLY : {
								double dr = d0 * d1;
								if(isDouble) return(new Cell(dr)); else {
									int ir = (int) dr;
									return(new Cell(ir));
								}
							}
		case OPERATOR_DIVIDE : {
								double dr = d0/d1;
								if(isDouble) return(new Cell(dr)); else {
									int ir = (int) dr;
									return(new Cell(ir));
								}
							}
		case OPERATOR_XOR : if(isDouble){
								throw new CellException("XOR only allowed for interger operend");
							} else {
								int i0 = (int) d0;
								int i1 = (int) d1;
								return(new Cell(i0 ^ i1));
							}
		}
		return(null);
	}
	public String toArgList(List p_argList,int p_dataType) throws CellException {
		if(type != 0) {
//			if(type == OPERENDTYPE_CONSTANT && ((Cell) loperend).getType() == Cell.VTYPE_STRING) {
//				String s = loperend.toString();
////				if(s.contains("'") || s.contains("\"")) {
////					p_argList.add(s);
////					return("?");
////				} else {
////					return("'"+loperend.toString()+"'");
////				}
//					p_argList.add(s);
//					return("?");
//			} else return(loperend.toString());
			if(type == OPERENDTYPE_CONSTANT) {
				switch(p_dataType) {
				case Cell.VTYPE_DATE :
					p_argList.add( DateUtil.toSqlDate(((Cell)loperend).getDate()));
					break;
				case Cell.VTYPE_DATETIME:
					p_argList.add( DateUtil.toSqlTimestamp(((Cell)loperend).getDate()));
					break;
				case Cell.VTYPE_STRING|0x100:
					Cell cc = (Cell) loperend;
					p_argList.add( new Strval(cc.getString(),"UTF8"));
					break;
				default : 
					p_argList.add(((Cell)loperend).getObject());
					break;
				}
				
//				if(p_dataType == 0) {
//					p_argList.add(((Cell)loperend).getObject());
//				} else {
//				}
				return("?");
			} else {
				return(loperend.toString());
			}
		} else {
		switch(operator) {
		case OPERATOR_PLUS:
			return("( "+loperend.toString() + " + " + roperend.toString() + ")");
		case OPERATOR_MINUS:
			if(loperend == null) {
				return(" -" + roperend.toString());
			} else {
				return( "("+loperend.toString() + " - " + roperend.toString() + ")");
			}
		case OPERATOR_MULTIPLY:
			return( "("+loperend.toString() + " * " + roperend.toString() + ")");
		case OPERATOR_DIVIDE:
			return( "("+loperend.toString() + " / " + roperend.toString() + ")");
		case OPERATOR_XOR :
			return( "("+loperend.toString() + " ^ " + roperend.toString() + ")");
		}
		}
		return(null);
	}
	public Cell eval(Object p_recData) throws Exception {
		if(operator == 0) {
			switch(type) {
			case OPERENDTYPE_CONSTANT: return((Cell) loperend);
			case OPERENDTYPE_VARIABLE: return(((Variable) loperend).eval(p_recData));
			case OPERENDTYPE_FUNCTION: return(((Function) loperend).eval(p_recData));
			default : throw new CellException("Eval failed");
			}
		} else {
			Cell cLeft=null,cRight=null;
			if(loperend != null) cLeft = ((Expression)loperend).eval(p_recData);
			if(roperend != null) cRight= ((Expression)roperend).eval(p_recData);
			return(evalArithmetic(cLeft,operator,cRight));
		}
	}

	public int getDataType() {
		switch(type) {
		case OPERENDTYPE_CONSTANT: return(((Cell) loperend).getType());
		case OPERENDTYPE_VARIABLE: return(((Variable) loperend).getDataType());
		case OPERENDTYPE_FUNCTION: return(((Function) loperend).getDataType());
		case OPERENDTYPE_EXPRESSION: return(((Expression) loperend).getDataType());
		default : return(0);
		}
	}
	
	static public String escapeStr(String p_str) {
	  return(p_str.replace("\\", "\\\\").replace("'","\\'"));
	}
	
	public void translateValue(ValueTranslationInterface p_intf) throws CellException {
		if(operator == 0) {
			switch(type) {
			case OPERENDTYPE_CONSTANT: p_intf.translaceCell((Cell) loperend);
			case OPERENDTYPE_VARIABLE: break;
			case OPERENDTYPE_FUNCTION: 
										List<Expression> argList = ((Function) loperend).argList;
										if(argList != null) {
										for(Expression expr : argList) {
											expr.translateValue(p_intf);
										}
										}
										break;
			default : throw new CellException("Eval failed");
			}
		} else {
			if(loperend != null) ((Expression)loperend).translateValue(p_intf);
			if(roperend != null) ((Expression)roperend).translateValue(p_intf);
		}
		
	}
	
	public int getOperendType() {
		return(type);
	}
}


