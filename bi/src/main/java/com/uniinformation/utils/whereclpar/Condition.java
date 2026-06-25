package com.uniinformation.utils.whereclpar;

import com.kyoko.common.StringUtil;
import com.kyoko.parser.ValueTranslationInterface;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.*;

import java.util.LinkedHashSet;
import java.util.Vector;
import java.util.HashSet;
import java.util.List;
public class Condition {
	class DualExpression {
		Expression exp1;
		Expression exp2;
		DualExpression(Expression p_exp1,Expression p_exp2)
		{
			exp1 = p_exp1;
			exp2 = p_exp2;
		}
	}
	static public final int LOGIC_OP_AND = 1;
	static public final int LOGIC_OP_OR  = 2;
	static public final int LOGIC_OP_NOT = 3;

	static public final int COMPARE_OP_GT = 1;
	static public final int COMPARE_OP_GE = 2;
	static public final int COMPARE_OP_EQ = 3;
	static public final int COMPARE_OP_NE = 4;
	static public final int COMPARE_OP_LT = 5;
	static public final int COMPARE_OP_LE = 6;
	static public final int COMPARE_OP_MA = 7;
	static public final int COMPARE_OP_NM = 8;
	static public final int COMPARE_OP_IS_NULL = 9;
	static public final int COMPARE_OP_IS_NOT_NULL = 10;
	static public final int COMPARE_OP_IN_ITEMLIST = 11;
	static public final int COMPARE_OP_NOTIN_ITEMLIST = 12;
	static public final int COMPARE_OP_LK = 13;
	static public final int COMPARE_OP_RE = 14;
	static public final int COMPARE_OP_NLK = 15;
	static public final int COMPARE_OP_NRE = 16;
	static public final int COMPARE_OP_BETWEEN = 17;
	static public final int COMPARE_OP_NOT_BETWEEN = 18;
	static public final int COMPARE_OP_REGEXP = 19;
	static public final int COMPARE_OP_NOT_REGEXP = 20;

	int operator;
	boolean isPredicate;
	Object loperend , roperend;
	public Condition (boolean p_truthValue) {
		isPredicate = true;
		operator = 0;
		loperend = new Boolean(p_truthValue);
	}
	public Condition (Expression p_loperend, int p_operator, Expression p_roperend1,Expression p_roperend2) throws Exception {
		switch(p_operator) {
		case COMPARE_OP_BETWEEN:
		case COMPARE_OP_NOT_BETWEEN:
						break;
		default : throw new Exception("operator incorrect");
		}
		isPredicate = true;
		operator = p_operator;
		loperend = p_loperend;
		roperend = new DualExpression(p_roperend1,p_roperend2);
	}
	public Condition (Expression p_loperend, int p_operator, Expression p_roperend) throws Exception {
		switch(p_operator) {
		case COMPARE_OP_GT:
		case COMPARE_OP_GE:
		case COMPARE_OP_EQ:
		case COMPARE_OP_NE:
		case COMPARE_OP_LT:
		case COMPARE_OP_LE:
		case COMPARE_OP_MA:
		case COMPARE_OP_NM:
		case COMPARE_OP_REGEXP:
		case COMPARE_OP_NOT_REGEXP:
		case COMPARE_OP_LK:
		case COMPARE_OP_NLK: break;
		default : throw new Exception("operator incorrect");
		}
		isPredicate = true;
		operator = p_operator;
		loperend = p_loperend;
		roperend = p_roperend;
	}
	public Condition (Condition p_loperend, int p_operator, Condition p_roperend) throws Exception {
		switch(p_operator) {
		case LOGIC_OP_AND:
		case LOGIC_OP_OR:
			break;
		default : throw new Exception("operator incorrect");
		}
		isPredicate = false;
		operator = p_operator;
		loperend = p_loperend;
		roperend = p_roperend;
	}
	public Condition (Expression p_loperend, int p_operator) throws Exception {
		switch(p_operator) {
		case COMPARE_OP_IS_NULL:
		case COMPARE_OP_IS_NOT_NULL:
					break;
		default : throw new Exception("argument missed");
		}
		isPredicate = true;
		operator = p_operator;
		loperend = p_loperend;
		roperend = null;
	}
	public Condition (int p_operator,Condition p_roperend) throws Exception {
		switch(p_operator) {
		case LOGIC_OP_NOT:
					break;
		default : throw new Exception("argument missed");
		}
		isPredicate = false;
		operator = p_operator;
		loperend = null;
		roperend = p_roperend;
	}
	public Condition (Expression p_loperend, int p_operator, List p_arglist) throws Exception {
		switch(p_operator) {
		case COMPARE_OP_IN_ITEMLIST:
		case COMPARE_OP_NOTIN_ITEMLIST:
					break;
		default : throw new Exception("arglist should only by used with 'in'");
		}

		isPredicate = true;
		operator = p_operator;
		loperend = p_loperend;
		roperend = p_arglist;
	}
	
	String toWhereclBinaryOp(String op,List p_argList) throws CellException {
		return(
				((Expression) loperend).toArgList(p_argList,0) + 
				" " + op + " " + 
				((Expression) roperend).toArgList(p_argList,((Expression) loperend).getDataType())
				);
	}
	public String toWherecl(List p_argList) throws CellException {
	if(!isPredicate) {
		switch(operator) {
		case LOGIC_OP_AND: return("("+((Condition) loperend).toWherecl(p_argList)+" and "+((Condition) roperend).toWherecl(p_argList)+")");
		case LOGIC_OP_OR: return("("+((Condition) loperend).toWherecl(p_argList)+" or "+((Condition) roperend).toWherecl(p_argList)+")");
		case LOGIC_OP_NOT: return(" not "+((Condition) roperend).toWherecl(p_argList)+")");
		}
	} else {
		switch(operator) {
		case 0 : if(((Boolean) loperend).booleanValue()) {
						return(" 1 = 1 ");
					} else {
						return(" 1 = 0 ");
					}
		case COMPARE_OP_BETWEEN: return(((Expression) loperend).toArgList(p_argList,0) + " between " + ((DualExpression) roperend).exp1.toArgList(p_argList,((Expression) loperend).getDataType()) + " and " + ((DualExpression) roperend).exp2.toArgList(p_argList,((Expression) loperend).getDataType()));
		case COMPARE_OP_NOT_BETWEEN: return(((Expression) loperend).toArgList(p_argList,0) + " not between " + ((DualExpression) roperend).exp1.toArgList(p_argList,((Expression) loperend).getDataType()) + " and " + ((DualExpression) roperend).exp2.toArgList(p_argList,((Expression) loperend).getDataType()));
		case COMPARE_OP_GT : return(toWhereclBinaryOp(">",p_argList));
		case COMPARE_OP_GE : return(toWhereclBinaryOp(">=",p_argList));
		case COMPARE_OP_EQ : return(toWhereclBinaryOp("=",p_argList));
		case COMPARE_OP_NE : return(toWhereclBinaryOp("<>",p_argList));
		case COMPARE_OP_LT : return(toWhereclBinaryOp("<",p_argList));
		case COMPARE_OP_LE : return(toWhereclBinaryOp("<=",p_argList));
		case COMPARE_OP_MA : return(toWhereclBinaryOp("matches",p_argList));
		case COMPARE_OP_NM : return(toWhereclBinaryOp("not matches",p_argList));
		case COMPARE_OP_IS_NULL : return(toWhereclBinaryOp("is null",p_argList));
		case COMPARE_OP_IS_NOT_NULL : return(toWhereclBinaryOp("is not null",p_argList));
		case COMPARE_OP_LK : return(toWhereclBinaryOp("like",p_argList));
		case COMPARE_OP_NLK : return(toWhereclBinaryOp("not like",p_argList));
		case COMPARE_OP_REGEXP : return(toWhereclBinaryOp("regexp",p_argList));
		case COMPARE_OP_NOT_REGEXP : return(toWhereclBinaryOp("not regexp",p_argList));
		case COMPARE_OP_IN_ITEMLIST : 
		case COMPARE_OP_NOTIN_ITEMLIST : 
					String s0 = ((Expression) loperend).toArgList(p_argList, 0);
					int lt = ((Expression) loperend).getDataType();
					String s = null;
					for(Object o : (List) roperend) {
						if(s == null) {
							s = ((Expression) o).toArgList(p_argList,lt);
						} else {
							s += "," + ((Expression) o).toArgList(p_argList,lt);
						}
					}
					switch(operator) {
					case COMPARE_OP_IN_ITEMLIST :  return(s0 + " in (" + s + ")");
					case COMPARE_OP_NOTIN_ITEMLIST : return(s0 + " not in (" + s + ")");
					}
		}
	}
	return(null);
}	
	
	
//	public String toWherecl(List p_argList) throws CellException {
//		if(!isPredicate) {
//			switch(operator) {
//			case LOGIC_OP_AND: return("("+((Condition) loperend).toWherecl(p_argList)+" and "+((Condition) roperend).toWherecl(p_argList)+")");
//			case LOGIC_OP_OR: return("("+((Condition) loperend).toWherecl(p_argList)+" or "+((Condition) roperend).toWherecl(p_argList)+")");
//			case LOGIC_OP_NOT: return(" not "+((Condition) roperend).toWherecl(p_argList)+")");
//			}
//		} else {
//			switch(operator) {
//			case 0 : if(((Boolean) loperend).booleanValue()) {
//							return(" 1 = 1 ");
//						} else {
//							return(" 1 = 0 ");
//						}
//			case COMPARE_OP_BETWEEN: return(((Expression) loperend).toArgList(p_argList) + " between " + ((DualExpression) roperend).exp1.toArgList(p_argList) + " and " + ((DualExpression) roperend).exp2.toArgList(p_argList));
//			case COMPARE_OP_NOT_BETWEEN: return(((Expression) loperend).toArgList(p_argList) + " not between " + ((DualExpression) roperend).exp1.toArgList(p_argList) + " and " + ((DualExpression) roperend).exp2.toArgList(p_argList));
//			case COMPARE_OP_GT : return(((Expression) loperend).toArgList(p_argList) + " > " + ((Expression) roperend).toArgList(p_argList));
//			case COMPARE_OP_GE : return(((Expression) loperend).toArgList(p_argList) + " >= " + ((Expression) roperend).toArgList(p_argList));
//			case COMPARE_OP_EQ : return(((Expression) loperend).toArgList(p_argList) + " = " + ((Expression) roperend).toArgList(p_argList));
//			case COMPARE_OP_NE : return(((Expression) loperend).toArgList(p_argList) + " <> " + ((Expression) roperend).toArgList(p_argList));
//			case COMPARE_OP_LT : return(((Expression) loperend).toArgList(p_argList) + " < " + ((Expression) roperend).toArgList(p_argList));
//			case COMPARE_OP_LE : return(((Expression) loperend).toArgList(p_argList) + " <= " + ((Expression) roperend).toArgList(p_argList));
//			case COMPARE_OP_MA : return(((Expression) loperend).toArgList(p_argList) + " matches " + ((Expression) roperend).toArgList(p_argList));
//			case COMPARE_OP_NM : return(((Expression) loperend).toArgList(p_argList) + " not matches " + ((Expression) roperend).toArgList(p_argList));
//			case COMPARE_OP_IS_NULL : return(((Expression) loperend).toArgList(p_argList) + " is null");
//			case COMPARE_OP_IS_NOT_NULL : return(((Expression) loperend).toArgList(p_argList) + " is not null");
//			case COMPARE_OP_LK : return(((Expression) loperend).toArgList(p_argList) + " like " + ((Expression) roperend).toArgList(p_argList));
//			case COMPARE_OP_NLK : return(((Expression) loperend).toArgList(p_argList) + " not like " + ((Expression) roperend).toArgList(p_argList));
//			case COMPARE_OP_REGEXP : return(((Expression) loperend).toArgList(p_argList) + " regexp " + ((Expression) roperend).toArgList(p_argList));
//			case COMPARE_OP_NOT_REGEXP : return(((Expression) loperend).toArgList(p_argList) + " not regexp " + ((Expression) roperend).toArgList(p_argList));
//			case COMPARE_OP_IN_ITEMLIST : 
//			case COMPARE_OP_NOTIN_ITEMLIST : 
//						String s = null;
//						for(Object o : (List) roperend) {
//							if(s == null) {
//								s = ((Expression) o).toArgList(p_argList);
//							} else {
//								s += "," + ((Expression) o).toArgList(p_argList);
//							}
//						}
//						switch(operator) {
//						case COMPARE_OP_IN_ITEMLIST :  return(((Expression) loperend).toArgList(p_argList) + " in (" + s + ")");
//						case COMPARE_OP_NOTIN_ITEMLIST : return(((Expression) loperend).toArgList(p_argList) + " not in (" + s + ")");
//						}
//			}
//		}
//		return(null);
//	}
	public String toString() {
		if(!isPredicate) {
			switch(operator) {
			case LOGIC_OP_AND: return("("+loperend.toString()+" and "+roperend.toString()+")");
			case LOGIC_OP_OR: return("("+loperend.toString()+" or "+roperend.toString()+")");
			case LOGIC_OP_NOT: return(" not "+roperend.toString()+")");
			}
		} else {
			switch(operator) {
			case 0 : if(((Boolean) loperend).booleanValue()) {
							return(" true ");
						} else {
							return(" false ");
						}
			case COMPARE_OP_BETWEEN: return(loperend.toString() + " between " + ((DualExpression) roperend).exp1.toString() + " and " + ((DualExpression) roperend).exp2.toString());
			case COMPARE_OP_NOT_BETWEEN: return(loperend.toString() + " not between " + ((DualExpression) roperend).exp1.toString() + " and " + ((DualExpression) roperend).exp2.toString());
			case COMPARE_OP_GT : return(loperend.toString() + " > " + roperend.toString());
			case COMPARE_OP_GE : return(loperend.toString() + " >= " + roperend.toString());
			case COMPARE_OP_EQ : return(loperend.toString() + " = " + roperend.toString());
			case COMPARE_OP_NE : return(loperend.toString() + " <> " + roperend.toString());
			case COMPARE_OP_LT : return(loperend.toString() + " < " + roperend.toString());
			case COMPARE_OP_LE : return(loperend.toString() + " <= " + roperend.toString());
			case COMPARE_OP_MA : return(loperend.toString() + " matches " + roperend.toString());
			case COMPARE_OP_NM : return(loperend.toString() + " not matches " + roperend.toString());
			case COMPARE_OP_IS_NULL : return(loperend.toString() + " is null");
			case COMPARE_OP_IS_NOT_NULL : return(loperend.toString() + " is not null");
			case COMPARE_OP_LK : return(loperend.toString() + " like " + roperend.toString());
			case COMPARE_OP_NLK : return(loperend.toString() + " not like " + roperend.toString());
			case COMPARE_OP_REGEXP : return(loperend.toString() + " regexp " + roperend.toString());
			case COMPARE_OP_NOT_REGEXP : return(loperend.toString() + " not regexp " + roperend.toString());
			case COMPARE_OP_IN_ITEMLIST : 
			case COMPARE_OP_NOTIN_ITEMLIST : 
						String s = null;
						for(Object o : (List) roperend) {
							if(s == null) {
								s = o.toString();
							} else {
								s += "," + o.toString();
							}
						}
						switch(operator) {
						case COMPARE_OP_IN_ITEMLIST :  return(loperend.toString() + " in (" + s + ")");
						case COMPARE_OP_NOTIN_ITEMLIST : return(loperend.toString() + " not in (" + s + ")");
						}
			}
		}
		return(null);
	}
	public HashSet getVariableHash(HashSet vSet) throws CellException {
		if(!isPredicate) {
			if(loperend != null) {
				vSet = ((Condition) loperend).getVariableHash(vSet);
			}
			if(roperend != null) {
				vSet = ((Condition) roperend).getVariableHash(vSet);
			}
		} else {
			vSet = ((Expression) loperend).getVariableHash(vSet);
			if(roperend != null) {
				if(roperend instanceof Expression) {
					vSet = ((Expression) roperend).getVariableHash(vSet);
				}
				if(roperend instanceof List) {
					for(Expression o : (List <Expression>) roperend) {
						vSet = ((Expression) o).getVariableHash(vSet);
					}
				}
				if(roperend instanceof DualExpression) {
					vSet = ((DualExpression) roperend).exp1.getVariableHash(vSet);
					vSet = ((DualExpression) roperend).exp2.getVariableHash(vSet);
				}
			}
		}
		return(vSet);
	}

	public void translateValue(ValueTranslationInterface p_intf) throws CellException {
		if(!isPredicate) {
			if(loperend != null) {
				((Condition) loperend).translateValue(p_intf);
			}
			if(roperend != null) {
				((Condition) roperend).translateValue(p_intf);
			}
		} else {
			((Expression) loperend).translateValue(p_intf);
			if(roperend != null) {
				if(roperend instanceof Expression) {
					((Expression) roperend).translateValue(p_intf);
				}
				if(roperend instanceof List) {
					for(Expression o : (List <Expression>) roperend) {
						((Expression) o).translateValue(p_intf);
					}
				}
				if(roperend instanceof DualExpression) {
					((DualExpression) roperend).exp1.translateValue(p_intf);
					((DualExpression) roperend).exp2.translateValue(p_intf);
				}
			}
		}
	}


	public boolean eval(Object p_recData) throws Exception {
		boolean lv=true,rv=true;
		if(!isPredicate) {
			if(loperend != null) lv = ((Condition) loperend).eval(p_recData);
			if(roperend != null) rv = ((Condition) roperend).eval(p_recData);
			switch(operator) {
			case LOGIC_OP_AND: return(lv && rv);
			case LOGIC_OP_OR:  return(lv || rv);
			case LOGIC_OP_NOT: return(!rv);
				default : throw new CellException("Invalid Operatod");
			}
		} else {
			switch(operator) {
			case 0 : return((Boolean) loperend).booleanValue();
			case COMPARE_OP_GT : 
			case COMPARE_OP_GE : 
			case COMPARE_OP_EQ : 
			case COMPARE_OP_NE : 
			case COMPARE_OP_LT : 
			case COMPARE_OP_LE :  {
					Cell lc = null;
					if(loperend != null) lc = (((Expression) loperend).eval(p_recData));
					if(lc == null) return(false);
					switch(lc.getType()) {
					case Cell.VTYPE_DOUBLE : {
							double ld = 0,rd = 0;
							ld = lc.getDouble();
							if(Double.isNaN(ld)) return(false);
							if(roperend != null) rd = (((Expression) roperend).eval(p_recData)).getDouble();
							switch(operator) {
							case COMPARE_OP_GT : return(ld > rd);
							case COMPARE_OP_GE : return(ld >= rd);
							case COMPARE_OP_EQ : return(ld == rd);
							case COMPARE_OP_NE : return(ld != rd);
							case COMPARE_OP_LT : return(ld < rd);
							case COMPARE_OP_LE :  return(ld <= rd);
							}
							break;
						}
					case Cell.VTYPE_STRING: {
							String ls = "",rs = "";
							ls = lc.getString().trim();
							if(roperend != null) {
								Cell cr = ((Expression) roperend).eval(p_recData);
								rs = cr.getString().trim();
							}
							switch(operator) {
							case COMPARE_OP_GT : 
												 if(ls.equals(rs)) return(false);
												 return(ls.startsWith(rs));
							case COMPARE_OP_GE : return(ls.startsWith(rs));
							case COMPARE_OP_EQ : return(ls.equals(rs));
							case COMPARE_OP_NE : return(!ls.equals(rs));
							case COMPARE_OP_LT : 
												 if(ls.equals(rs)) return(false);
												 return(rs.startsWith(ls));
							case COMPARE_OP_LE : return(rs.startsWith(ls));
							}
							break;
						}
					case Cell.VTYPE_INT: {
							int ld = 0,rd = 0;
							ld = lc.getInt();
							if(roperend != null) rd = (((Expression) roperend).eval(p_recData)).getInt();
							switch(operator) {
							case COMPARE_OP_GT : return(ld > rd);
							case COMPARE_OP_GE : return(ld >= rd);
							case COMPARE_OP_EQ : return(ld == rd);
							case COMPARE_OP_NE : return(ld != rd);
							case COMPARE_OP_LT : return(ld < rd);
							case COMPARE_OP_LE :  return(ld <= rd);
							}
							break;
						}

					case Cell.VTYPE_BOOLEAN: {
							Cell rc;
							if(roperend != null) rc = (((Expression) roperend).eval(p_recData)); else return(false);
							boolean rb=false;
							if(rc.getType() == Cell.VTYPE_BOOLEAN) {
										rb = rc.getBoolean();
							} else if(rc.getType() == Cell.VTYPE_STRING) {
										rb = rc.getString().equals("Y");
							} else if(rc.getType() == Cell.VTYPE_INT) {
										rb = rc.getInt() == 1;
							} else if(rc.getType() == Cell.VTYPE_DOUBLE) {
										rb = rc.getDouble() == 1.0;
							}
							switch(operator) {
							case COMPARE_OP_EQ : return(!(lc.getBoolean() ^ rb));
							case COMPARE_OP_NE : return((lc.getBoolean() ^ rb));
							}
							break;
					}
					case Cell.VTYPE_DATE: {
							java.util.Date ld = lc.getDate();
							java.util.Date rd;
							if(roperend != null) rd = (((Expression) roperend).eval(p_recData)).getDate(); else return(false);
							switch(operator) {
							case COMPARE_OP_GT : return(ld.after(rd));
							case COMPARE_OP_GE : return(!(rd.after(ld)));
							case COMPARE_OP_EQ : return(ld.equals(rd));
							case COMPARE_OP_NE : return(!ld.equals(rd));
							case COMPARE_OP_LT : return(ld.before(rd));
							case COMPARE_OP_LE : return(!(rd.before(ld)));
							}
							break;
						}
					default :
							throw new CellException("Compare Op Error");
					}
				}
			case COMPARE_OP_MA : 
			case COMPARE_OP_NM : 
						{
						String ls = (((Expression) loperend).eval(p_recData)).getString();
						String rs = (((Expression) roperend).eval(p_recData)).getString();
						return((operator == COMPARE_OP_NM) ^ StringUtil.matchString(ls, rs, true));
						}
			case COMPARE_OP_LK : 
			case COMPARE_OP_NLK : 
						{
						String ls = (((Expression) loperend).eval(p_recData)).getString();
						String rs = (((Expression) roperend).eval(p_recData)).getString();
						return((operator == COMPARE_OP_NLK) ^ StringUtil.matchString(ls, rs, true));
						}
			case COMPARE_OP_REGEXP : 
			case COMPARE_OP_NOT_REGEXP : 
						{
						String ls = (((Expression) loperend).eval(p_recData)).getString();
						String rs = (((Expression) roperend).eval(p_recData)).getString();
						return((operator == COMPARE_OP_NLK) ^ StringUtil.matchString(ls, rs, true));
						}
			case COMPARE_OP_IN_ITEMLIST : 
			case COMPARE_OP_NOTIN_ITEMLIST : 
						{
							Cell cl = ((Expression) loperend).eval(p_recData);
							for(Expression expr : (List <Expression>) roperend) {
								Cell cr = expr.eval(p_recData);
								if(cl.equals(cr)) {
									return((operator == COMPARE_OP_IN_ITEMLIST));
								}
							}
							return((operator == COMPARE_OP_NOTIN_ITEMLIST));
						}
			case COMPARE_OP_BETWEEN:
			case COMPARE_OP_NOT_BETWEEN: 
						{
							Cell cl = ((Expression) loperend).eval(p_recData);
							Cell from = ((DualExpression) roperend).exp1.eval(p_recData);
							Cell to = ((DualExpression) roperend).exp2.eval(p_recData);
							if(cl.compareTo(from) < 0) return(operator == COMPARE_OP_NOT_BETWEEN);
							if(cl.compareTo(to) > 0) return(operator == COMPARE_OP_NOT_BETWEEN);
							return(operator == COMPARE_OP_BETWEEN);
						}
			case COMPARE_OP_IS_NULL : return(isNull(loperend,p_recData));
			case COMPARE_OP_IS_NOT_NULL : return(!isNull(loperend,p_recData));
			default : throw new CellException("Invalid Operator");
			}
		}
		
	}
	
	boolean isNull(Object operend,Object p_recData) throws Exception{
		if(operend == null) return(true);
		Cell lc = ((Expression) operend).eval(p_recData);
		if(lc.getType() == Cell.VTYPE_DOUBLE) {
			double d = lc.getDouble();
			if(Double.isNaN(d)) return(true);
		}
		return(false);
	}

	public static Condition eliminateNotCondition(Condition p_condition) throws Exception {
		if(!p_condition.isPredicate && p_condition.operator == LOGIC_OP_NOT) {
			Condition cond = (Condition) p_condition.roperend;
			if(cond.isPredicate) {
			switch(cond.operator) {
			case 0 : cond.loperend = new Boolean(!((Boolean) cond.loperend).booleanValue()); break;
			case COMPARE_OP_GT : cond.operator = COMPARE_OP_LE; break;
			case COMPARE_OP_LE : cond.operator = COMPARE_OP_GT; break;
			case COMPARE_OP_GE : cond.operator = COMPARE_OP_LT; break;
			case COMPARE_OP_LT : cond.operator = COMPARE_OP_GE; break;
			case COMPARE_OP_EQ : cond.operator = COMPARE_OP_NE; break;
			case COMPARE_OP_NE : cond.operator = COMPARE_OP_EQ; break;
			case COMPARE_OP_MA : cond.operator = COMPARE_OP_NM; break;
			case COMPARE_OP_NM : cond.operator = COMPARE_OP_MA; break;
			case COMPARE_OP_LK : cond.operator = COMPARE_OP_NLK; break;
			case COMPARE_OP_NLK : cond.operator = COMPARE_OP_LK; break;
			case COMPARE_OP_IN_ITEMLIST : cond.operator = COMPARE_OP_NOTIN_ITEMLIST; break;
			case COMPARE_OP_NOTIN_ITEMLIST : cond.operator = COMPARE_OP_IN_ITEMLIST; break;
			case COMPARE_OP_BETWEEN: cond.operator = COMPARE_OP_NOT_BETWEEN; break;
			case COMPARE_OP_NOT_BETWEEN: cond.operator = COMPARE_OP_BETWEEN; break;
			case COMPARE_OP_IS_NULL : cond.operator = COMPARE_OP_IS_NOT_NULL; break;
			case COMPARE_OP_IS_NOT_NULL : cond.operator = COMPARE_OP_IS_NULL; break;
			case COMPARE_OP_REGEXP : cond.operator = COMPARE_OP_NOT_REGEXP; break;
			case COMPARE_OP_NOT_REGEXP : cond.operator = COMPARE_OP_REGEXP; break;
			default : throw new CellException("Invalid Operator");
			}
			return(cond);
			} else {
			switch(cond.operator) {
			case LOGIC_OP_NOT : return((Condition) cond.roperend);
			case LOGIC_OP_AND : 
									  return(
									  	new Condition(
									  	eliminateNotCondition(new Condition(
											LOGIC_OP_NOT,
											(Condition) cond.loperend
										)),
										LOGIC_OP_OR,
									  	eliminateNotCondition(new Condition(
											LOGIC_OP_NOT,
											(Condition) cond.roperend
										))
									  ));
			case LOGIC_OP_OR: 

									  return(
									   new Condition(
									  	eliminateNotCondition(new Condition(
											LOGIC_OP_NOT,
											(Condition) cond.loperend
										)),
										LOGIC_OP_AND,
									  	eliminateNotCondition(new Condition(
											LOGIC_OP_NOT,
											(Condition) cond.roperend
										))
									  ));
			default : throw new CellException("Invalid Operator");
			}
			}
			
		} else {
			if(p_condition.loperend instanceof Condition) {
				p_condition.loperend = eliminateNotCondition((Condition) p_condition.loperend);
			}
			if(p_condition.roperend instanceof Condition) {
				p_condition.roperend = eliminateNotCondition((Condition) p_condition.roperend);
			}
			return(p_condition);
		}
	}

	static Vector <Vector<Boolean>> crossProduct( Vector <Vector<Boolean>> p_l, Vector <Vector<Boolean>> p_r) throws Exception
	{
		Vector <Vector<Boolean>> result = new Vector<Vector<Boolean>>();;
		for(Vector<Boolean> rowl : p_l) {
			for(Vector<Boolean> rowr : p_r) {
				Vector<Boolean> row = new Vector<Boolean>();
				for(boolean b:rowl) {
					row.add(b);
				}
				for(boolean b:rowr) {
					row.add(b);
				}
				result.add(row);
			}
		}
		return(result);
	}
	static Vector <Vector<Boolean>> unionProduct( Vector <Vector<Boolean>> p_l, Vector <Vector<Boolean>> p_r) throws Exception
	{
		int sizeL = p_l.get(0).size();
		int sizeR = p_r.get(0).size();
		Vector <Vector<Boolean>> result = new Vector<Vector<Boolean>>();;
		for(Vector<Boolean> rowl : p_l) {
			Vector<Boolean> row = new Vector<Boolean>();
			for(boolean b:rowl) {
				row.add(b);
			}
			for(int i=0;i<sizeR;i++) row.add(false);
			result.add(row);
		}
		for(Vector<Boolean> rowr : p_r) {
			Vector<Boolean> row = new Vector<Boolean>();
			for(int i=0;i<sizeL;i++) row.add(false);
			for(boolean b:rowr) {
				row.add(b);
			}
			result.add(row);
		}
		return(result);
	}
	static Vector <Vector<Boolean>> serializeConditionOne(boolean p_ByAnd,Condition p_condition,Vector <Condition> predicateList) throws Exception
	{
		if(p_condition.isPredicate) {
			predicateList.add(p_condition);
			Vector <Boolean> r0 = new Vector<Boolean>();
			r0.add(true);
			Vector <Vector<Boolean>> s = new Vector<Vector<Boolean>>();
			s.add(r0);
			return(s);
		} else {
			Vector <Vector<Boolean>> vl = serializeConditionOne(p_ByAnd,(Condition) p_condition.loperend,predicateList);
			Vector <Vector<Boolean>> vr = serializeConditionOne(p_ByAnd,(Condition) p_condition.roperend,predicateList);
			if(p_ByAnd) {
			switch(p_condition.operator) {
			case LOGIC_OP_AND: return(crossProduct(vl,vr));
			case LOGIC_OP_OR:  return(unionProduct(vl,vr));
			}
			} else {
			switch(p_condition.operator) {
			case LOGIC_OP_AND: return(unionProduct(vl,vr));
			case LOGIC_OP_OR:  return(crossProduct(vl,vr));
			}
			}
		}
		return(null);
	}
	static void factorize(LinkedHashSet<String> p_hs,Condition p_cond,LinkedHashSet p_lhs) {
		if(p_cond.isPredicate) {
			p_hs.add(p_cond.toString());
			p_lhs.add(p_cond.toString());
			return;
		} else {
			if(p_cond.loperend != null) factorize(p_hs,(Condition) p_cond.loperend,p_lhs);
			if(p_cond.roperend != null) factorize(p_hs,(Condition) p_cond.roperend,p_lhs);
		}
	}
	static public List<Condition> serializeCondition(boolean p_ByAnd,Condition p_condition) throws Exception {
		Vector <Condition> result = new Vector<Condition>();
		Vector<Condition> predicateList = new Vector<Condition> ();
		Vector<Vector<Boolean>> matrix = serializeConditionOne(p_ByAnd,eliminateNotCondition(p_condition),predicateList);
		for(int i=0;i<matrix.size();i++) {
			Condition cond = null;
			Vector<Boolean> row = matrix.get(i);
			for(int j=0;j<row.size();j++) {
				if(row.get(j)) {
					if(cond == null) {
						cond = predicateList.get(j);
					} else {
						if(p_ByAnd) {
							cond = new Condition(cond,LOGIC_OP_AND,predicateList.get(j));
						} else {
							cond = new Condition(cond,LOGIC_OP_OR,predicateList.get(j));
						}
					}
				}
//				UniLog.log("matrix " + i + "x" + j + " " + row.get(j));	
			}
			if(cond != null) result.add(cond);
		}
		return(result);
	}
	
	static public Vector<Condition> optimizeConditionList(List <Condition> p_conditionList,Parser p_parser,boolean p_ByAnd) throws Exception {
			LinkedHashSet<String> predicateSet = new LinkedHashSet<String>();
			LinkedHashSet<LinkedHashSet<String>> groupConditionSet = null;
			for(Condition cond : p_conditionList) {
				LinkedHashSet<String> ruleConditionSet = new LinkedHashSet<String>();
				factorize(ruleConditionSet,cond,predicateSet);
				if(groupConditionSet == null) {
					groupConditionSet = new LinkedHashSet<LinkedHashSet<String>>();
					groupConditionSet.add(ruleConditionSet);
				} else {
					LinkedHashSet<LinkedHashSet<String>> tmpHashSet = new LinkedHashSet<LinkedHashSet<String>>();
					boolean ruleEliminated = false;
					for(LinkedHashSet<String> groupCondition : groupConditionSet) {
						if(p_ByAnd) {
							if(groupCondition.containsAll(ruleConditionSet)) {
//								UniLog.log("A" + ruleConditionSet.toString() + " <-> " + groupCondition.toString());
							} else if(ruleConditionSet.containsAll(groupCondition)) {
//								UniLog.log("B" + ruleConditionSet.toString() + " <-> " + groupCondition.toString());
								ruleEliminated = true;
								tmpHashSet.add(groupCondition);
							} else {
//								UniLog.log("C" + ruleConditionSet.toString() + " <-> " + groupCondition.toString());
								tmpHashSet.add(groupCondition);
							}
						} else {
							if(groupCondition.containsAll(ruleConditionSet)) {
//								UniLog.log("D" + ruleConditionSet.toString() + " <-> " + groupCondition.toString());
								ruleEliminated = true;
								tmpHashSet.add(groupCondition);
							} else if(ruleConditionSet.containsAll(groupCondition)) {
//								UniLog.log("E" + ruleConditionSet.toString() + " <-> " + groupCondition.toString());
							} else {
								tmpHashSet.add(groupCondition);
//								UniLog.log("F" + ruleConditionSet.toString() + " <-> " + groupCondition.toString());
							}
						}
					}
					if(!ruleEliminated) tmpHashSet.add(ruleConditionSet);
					groupConditionSet = tmpHashSet;
				}
//				UniLog.log("After " + groupConditionSet.toString());
			}
			Vector<Condition> result = new Vector<Condition>();
			for(HashSet<String> groupCondition : groupConditionSet) {
				String groupConditionStr = null;
				for(String predicate : groupCondition) {
						if(groupConditionStr == null) {
							groupConditionStr = predicate;
						} else {
							groupConditionStr += (p_ByAnd ? " and " : " or ") + predicate;
						}
				}
//				UniLog.log("HAHA factorized: " + groupConditionStr);
				Condition cond = (Condition) p_parser.parse(groupConditionStr);
				result.add(cond);
			}
			return(result);
	}
	
	public boolean get_isPredicate() {
		return(isPredicate);
	}
	
	public Expression get_leftExpression() {
		if(!isPredicate) return(null);
		return((Expression) loperend);
	}
	public int get_operator() {
		return(operator);
	}
	public Expression get_rightExpression() {
		if(!isPredicate) return(null);
		return((Expression) roperend);
	}
	public List get_rightExpressionList() {
		if(!isPredicate) return(null);
		return((List) roperend);
	}
	public Expression get_rightExpression1() {
		if(!isPredicate) return(null);
		return(((DualExpression) roperend).exp1);
	}
	public Expression get_rightExpression2() {
		if(!isPredicate) return(null);
		return(((DualExpression) roperend).exp2);
	}
}


