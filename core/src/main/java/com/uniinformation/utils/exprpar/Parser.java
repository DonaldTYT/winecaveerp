package com.uniinformation.utils.exprpar;

/*uncomment this code to fallback to old parser */

//public class Parser extends com.uniinformation.utils.oexprpar.Parser {
//	public Parser(int p_ignoreCase,String p_string) {
//		super(p_string);
//	}
//	public Parser(int p_ignoreCase,String p_string, VariableInterface p_varInterface, FunctionInterface p_functInterface) {
//		super(p_string,p_varInterface,p_functInterface);
//	}
//}

/* comments above code and uncomments the code below to use new parser */




import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import com.kyoko.parser.Condition;
import com.kyoko.parser.Expression;
import com.kyoko.parser.Variable;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.UniLog;

public class Parser implements com.kyoko.parser.FunctionInterface,com.kyoko.parser.VariableInterface {
	boolean inCollectName = false;
	Expression exp;
	FunctionInterface oFunctionInterface;
	VariableInterface oVariableInterface;
	Vector<String> functions;
	Vector<String> variables;
	public Parser(int p_ignoreCase,String p_string) /* throws Exception */ {
		try {
			functions=null;
			variables=null;
			logFormulaCharacterCheck(p_string);
			Object oo = new com.kyoko.parser.whereclpar.Parser(p_ignoreCase,p_string,this,this).parse();
			if(oo instanceof Condition) {
				exp = new Expression((Condition) oo);
			} else {
				exp = (Expression) oo;
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	public Parser(int p_ignoreCase,String p_string, VariableInterface p_varInterface, FunctionInterface p_functInterface) /* throws Exception */ {
		try {
			functions=null;
			variables=null;
			oFunctionInterface = p_functInterface;
			oVariableInterface = p_varInterface;
			logFormulaCharacterCheck(p_string);
			Object res = new com.kyoko.parser.whereclpar.Parser(p_ignoreCase,p_string,this,this).parse();
			if(res instanceof Expression) exp = (Expression) res; else {
				/* assumed res is Condition */
				exp = new Expression((Condition) res);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}

	/* TEMP: diagnose invisible or lexer-unsupported characters in formulas. */
	private static void logFormulaCharacterCheck(String p_formula) {
		if(p_formula == null) {
			UniLog.log1("TEMP formula character check: formula is null");
			return;
		}

		StringBuilder whitespace = new StringBuilder();
		StringBuilder unsupported = new StringBuilder();
		char quote = 0;

		for(int i=0;i<p_formula.length();) {
			int codePoint = p_formula.codePointAt(i);
			int charCount = Character.charCount(codePoint);

			if(Character.isWhitespace(codePoint) || Character.isSpaceChar(codePoint)) {
				appendCharacterDescription(whitespace, i, codePoint,
					isAcceptedFormulaWhitespace(codePoint) ? "accepted" : "UNACCEPTED");
			}

			if(quote != 0) {
				if(codePoint == '\\') {
					int nextIndex = i + charCount;
					if(nextIndex >= p_formula.length()) {
						appendCharacterDescription(unsupported, i, codePoint, "trailing escape");
					} else {
						int next = p_formula.codePointAt(nextIndex);
						if(next != '\\' && next != quote) {
							appendCharacterDescription(unsupported, i, codePoint, "unsupported escape");
						}
						i = nextIndex + Character.charCount(next);
						continue;
					}
				} else if(codePoint == quote) {
					quote = 0;
				}
			} else if(codePoint == '\'' || codePoint == '"') {
				quote = (char) codePoint;
			} else if(!isAcceptedOutsideLiteral(codePoint)) {
				appendCharacterDescription(unsupported, i, codePoint, "lexer does not accept");
			}

			i += charCount;
		}

		if(quote != 0) {
			if(unsupported.length() > 0) unsupported.append(", ");
			unsupported.append("unterminated ").append(quote).append(" string literal");
		}

		UniLog.log1("TEMP formula character check: whitespace=[%s] unsupported=[%s] formula=[%s]",
			whitespace.length() == 0 ? "none" : whitespace.toString(),
			unsupported.length() == 0 ? "none" : unsupported.toString(),
			escapeFormulaForLog(p_formula));
	}

	private static boolean isAcceptedFormulaWhitespace(int p_codePoint) {
		return p_codePoint == ' ' || p_codePoint == '\t' ||
			p_codePoint == '\r' || p_codePoint == '\n';
	}

	private static boolean isAcceptedOutsideLiteral(int p_codePoint) {
		if(isAcceptedFormulaWhitespace(p_codePoint)) return true;
		if(p_codePoint >= 'a' && p_codePoint <= 'z') return true;
		if(p_codePoint >= 'A' && p_codePoint <= 'Z') return true;
		if(p_codePoint >= '0' && p_codePoint <= '9') return true;
		return "_.,!+-*/^()[]:{}<>=&|".indexOf(p_codePoint) >= 0;
	}

	private static void appendCharacterDescription(StringBuilder p_output, int p_index,
			int p_codePoint, String p_status) {
		if(p_output.length() > 0) p_output.append(", ");
		String name = Character.getName(p_codePoint);
		p_output.append("index=").append(p_index)
			.append(" U+").append(String.format("%04X", p_codePoint))
			.append(" ").append(name == null ? "UNKNOWN" : name)
			.append(" (").append(p_status).append(")");
	}

	private static String escapeFormulaForLog(String p_formula) {
		return p_formula.replace("\\", "\\\\")
			.replace("\r", "\\r")
			.replace("\n", "\\n")
			.replace("\t", "\\t");
	}
	public Object evaluate() throws Exception {
		Cell rtn = exp.eval(null);
		if(rtn != null) return(rtn.getObject()) ; else return( null);
	}
	public void setFunctInterface(FunctionInterface p_functInterface) {
			oFunctionInterface = p_functInterface;
	}
	public void setVarInterface(VariableInterface p_varInterface) {
			oVariableInterface = p_varInterface;
	}
	public void collect() {
		
	}
	public Vector getVariables() throws CellException {
		if(variables == null) {
			variables = new Vector<String>();
			HashSet vHash = exp.getVariableHash(new HashSet(),false);
			for(Object oo : vHash) {
				variables.add(oo.toString());
			}
		}
		return(variables);
	}

	public Vector getVariablesUnIndexed() throws CellException {
		if(variables == null) {
			variables = new Vector<String>();
			HashSet vHash = exp.getVariableHash(new HashSet(),true);
			for(Object oo : vHash) {
//				Variable vv = (Variable) oo;
				variables.add(oo.toString());
			}
		}
		return(variables);
	}
	
	public Vector getFunctions() throws CellException {
		if(functions == null) {
			functions = new Vector<String>();
			HashSet<com.kyoko.parser.Function> vHash = exp.getFunctionHash(new HashSet());
			for(Object oo : vHash) {
				functions.add(oo.toString());
			}
		}
		return(functions);
	}
	
	public Expression getExpression() {
		return(exp);
	}
	
	/* new Parser Interfaces */
	@Override
	public String toString(String p_varName, int p_idx, boolean p_idxAbsolute) {
		/*
		if(inCollectName) return(p_varName);
		// TODO Auto-generated method stub
		if(p_idxAbsolute) {
			return(p_varName+"["+p_idx+"]");
		} else {
			if(p_idx == 0) return(p_varName); 
			else if(p_idx > 0) return(p_varName+"[+"+p_idx+"]");
			else return(p_varName+"[-"+p_idx+"]");
		}
		*/
		return(null);
	}
	@Override
	public Object collectObject(String p_varName, int p_idx, boolean p_idxAbsolute) throws CellException {
		// TODO Auto-generated method stub
		return(null);
	}
	
	@Override
	public Cell evalVariable(String p_varName, int p_idx, boolean p_idxAbsolute, Object p_recData) throws CellException {
		try {
		// TODO Auto-generated method stub
			if(p_idxAbsolute) {
				return new Cell(oVariableInterface.evalVariable(p_varName, p_idx));
			} else {
				if(p_idx != 0)
					return new Cell(oVariableInterface.evalVariableRelative(p_varName, p_idx));
				else
					return new Cell(oVariableInterface.evalVariable(p_varName));
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			throw new CellException(ex.getMessage());
		}
	}
	@Override
	public String toString(String p_functName, List p_args) {
		// TODO Auto-generated method stub
		return(null);
	}
	@Override
	public Cell evalFunction(String p_functName, Vector p_args, Object p_data) throws Exception {
		// TODO Auto-generated method stub
		/*
		try {
			Vector v = new Vector();
			for(Object oo : p_args) {
				if(oo instanceof Cell) v.add(((Cell) oo).getObject()); else v.add(oo);
			}
			return new Cell(oFunctionInterface.evalFunction(p_functName, v));
		} catch (Exception ex) {
			UniLog.log(ex);
			throw new CellException(ex.getMessage());
		}
		*/
		Object o = oFunctionInterface.evalFunction(p_functName, p_args);
		if(o != null && o instanceof String && o.equals("#N/A")) {
			Cell cc = new Cell("");
			cc.setFlag(1);
			return(cc);
		} else {
			return new Cell(o);
		}
		//		return new Cell(oFunctionInterface.evalFunction(p_functName, p_args));
	}
	@Override
	public int getDataType(String p_functName) {
		// TODO Auto-generated method stub
		return (Cell.VTYPE_DOUBLE);
	}
	@Override
	public Object collectObject(String p_functName, List p_args) throws CellException {
		// TODO Auto-generated method stub
		return (null);
	}
}
