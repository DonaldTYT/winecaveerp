package com.kikyosoft.parser;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.kikyosoft.utils.*;

public class SerializedCondition {
	boolean andOrAnd;
	List<Condition> conditions;

	public List<Condition> get_conditions() {
		return(conditions);
	}
	
	public List<Condition> get_predicates(int p_idx,Parser p_parser) {
		if(conditions == null || p_idx < 0 || p_idx >= conditions.size()) return(null);
		Condition c = conditions.get(p_idx);
		try {
			SerializedCondition  sc = serialize(c,!andOrAnd,p_parser);
			return(sc.get_conditions());
		} catch (Exception ex) {
			LogUtil.log(ex);
			return(null);
		}
	}
	
	public String toString() {
//		String s = "";
		if(conditions == null) return(null);
		if(conditions.size() == 1) return(conditions.get(0).toString());
		Condition c = null;
		try {
		for(Condition cond : conditions) {
			if(c == null) c = cond; else {
				if(andOrAnd) {
					c = new Condition(c,Condition.LOGIC_OP_OR,cond);
				} else {
					c = new Condition(c,Condition.LOGIC_OP_AND,cond);
				}
			}
//			if(s.equals("")) s = cond.toString(); else s += (andOrAnd ? " or " : " and ") + cond.toString();
		}
		} catch (Exception ex) {
			LogUtil.log(ex);
			return(null);
		}
//		return(s);
		return(c.toString());
	}
	
	public boolean get_andOrAnd() {
		return(andOrAnd);
	}
	
	public static SerializedCondition serialize(Condition p_condition,boolean p_andOrAnd,Parser p_parser) throws Exception {
		SerializedCondition sc = new SerializedCondition();
		sc.andOrAnd = p_andOrAnd;
		sc.conditions = Condition.optimizeConditionList(Condition.serializeCondition(p_andOrAnd, p_condition),p_parser,p_andOrAnd);
		return(sc);
	}
	/*
	public static SerializedCondition stringToSerializedCondition(String p_conditionStr) {
		try {
			Parser p = new  Parser();
			Condition c0 = (Condition) p.parse(p_conditionStr);
			SerializedCondition sc = serialize(c0,false,p);
			if(sc.toString().equals(c0.toString())) return(sc);
			sc = serialize(c0,true,p);
			if(sc.toString().equals(c0.toString())) return(sc);
		} catch (Exception ex) {
			CoreLog.log(ex);
			return(null);
		}
		return(null);
	}
	*/
//	public static void main(String args[]) throws Exception{
//		SerializedCondition sc;
//
//		/* serialzied condtion is of the form 
//
//			1)
//			
//		 	(predicate1a and predicate2a .... and predicateNa) or (predicate1b and predicate2b .... and predicateNb) or ...
//		 	
//		 	or 
//
//			2)
//		 	(predicate1a or predicate2a .... or predicateNa) and (predicate1b or predicate2b .... or predicateNb) and ...
//		 */
//		/* create serialzedCondtion from serialized condition String */
//		sc = stringToSerializedCondition("(a = 1 and b = 1 and c = 1) or (d = 1 and e = 1 and f = 1) or (g = 1 and h = 1 and i = 1) or (a+1 = b-2)");
////		sc = stringToSerializedCondition("(a = 1 or b = 1 or c = 1) and (d = 1 or e = 1 or f = 1) and (g = 1 or h = 1 or i = 1)");
////		sc = stringToSerializedCondition("a between 1 and 2");
////		sc = stringToSerializedCondition("a in ('A','B','C')");
//		
//		if(sc != null) {
//			// tell whether the condition string is of form 1) (andOrand) or form 2) (orAndOr)
//			CoreLog.log("andOrAnd: "+sc.get_andOrAnd());
//			CoreLog.log(""+sc.toString());
//			for (int i = 0;i<sc.get_conditions().size();i++) {
//				CoreLog.log("group " + i);
//				// get the Predicate List of each group 
//				List<Condition> predicateList = sc.get_predicates(i);
//				for(Condition c : predicateList) {
//					// condition c should be a predicate
//					CoreLog.log(c.toString());
//					Expression loperand = c.get_leftExpression();
//					// loperend should be a variable
//					CoreLog.log("variable: " + loperand.toString());
//					int op = c.get_operator();
//					CoreLog.log("operator: " + op);
//					switch(op) {
//					case Condition.COMPARE_OP_IN_ITEMLIST:
//					case Condition.COMPARE_OP_NOTIN_ITEMLIST:
//							// roperend is a list
//							List<Expression> itemList = c.get_rightExpressionList();
//							{
//								String s = "Values [";
//								for(Expression exp : itemList) {
//									s += exp.toString();
//									s += ",";
//								}
//								s += "]";
//								CoreLog.log(s);
//							}
//							break;
//					case Condition.COMPARE_OP_BETWEEN:
//					case Condition.COMPARE_OP_NOT_BETWEEN:
//							// roperend has two expression
//							{
//								String s = "Values (";
//								Expression exp = c.get_rightExpression1();
//								s += exp.toString();
//								s += ",";
//								exp = c.get_rightExpression2();
//								s += exp.toString();
//								s += ")";
//								CoreLog.log(s);
//							}
//							break;
//					default :
//							// roperend is single expression
//							{
//								String s = "Values (";
//								Expression exp = c.get_rightExpression();
//								s += exp.toString();
//								s += ")";
//								CoreLog.log(s);
//							}
//							break;
//					}
//				}
//			}
//		} else {
//			CoreLog.log("condition string not serialized");
//		}
//	}
//	public static void main02(String args[]) throws Exception{
//		CoreLog.log("Examples of expression primitive");
//		Expression exp;
//		exp = new Expression("\"'1'");
//		CoreLog.log(exp.toString());
//		exp = new Expression(123);
//		CoreLog.log(exp.toString());
//		exp = new Expression(1.3);
//		CoreLog.log(exp.toString());
//		exp = new Expression(new Date());
//		CoreLog.log(exp.toString());
//		exp = new Expression(new Variable("v1",null,null));
//		CoreLog.log(exp.toString());
//		exp = new Expression( new Expression(1) , Expression.OPERATOR_PLUS , new Expression(2));
//		CoreLog.log(exp.toString());
//		exp = new Expression( new Function("fn1",null,new VectorUtil().addElement(new Expression(11)).toVector()));
//		CoreLog.log(exp.toString());
//		
//		CoreLog.log("Examples of predicate (primitive of condition)");
//		Condition predicate ;
//		predicate = new Condition(new Expression(new Variable("v1",null,null)),Condition.COMPARE_OP_EQ,exp);
//		CoreLog.log(predicate.toString());
//
//		predicate = new Condition(new Expression(new Variable("v1",null,null)),Condition.COMPARE_OP_IN_ITEMLIST,
//						new VectorUtil()
//						.addElement(new Expression("AAA"))
//						.toVector()
//					);
//		CoreLog.log(predicate.toString());
//		
//		CoreLog.log("Examples of composite condition)");
//		Condition  cond = new Condition(predicate,Condition.LOGIC_OP_AND,predicate);
//		CoreLog.log(cond.toString());
//
//	}
//	public static void main01(String args[]) throws Exception{
//		CoreLog.log("Examples of expression primitive");
//		Expression exp;
//
//		Parser s = new Parser();
//		Object o = s.parse("'\"A\\''");
//		CoreLog.log(o.toString());
//		o = s.parse(o.toString());
//		CoreLog.log(o.toString());
////		String s  = "B'A";
////		CoreLog.log(""+s.length());
////		s = s.replaceAll("'", "\\\\'");
////		CoreLog.log(s+":"+s.length());
//	}
}	
