package com.kikyosoft.cell;

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

import com.kikyosoft.parser.Condition;
import com.kikyosoft.parser.Expression;
import com.kikyosoft.parser.Variable;
import com.kikyosoft.utils.*;

public class Parser implements com.kikyosoft.parser.FunctionInterface,com.kikyosoft.parser.VariableInterface {
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
			Object oo = new com.kikyosoft.parser.whereclpar.Parser(p_ignoreCase,p_string,this,this).parse();
			if(oo instanceof Condition) {
				exp = new Expression((Condition) oo);
			} else {
				exp = (Expression) oo;
			}
		} catch (Exception ex) {
			LogUtil.log(ex);
		}
	}
	public Parser(int p_ignoreCase,String p_string, VariableInterface p_varInterface, FunctionInterface p_functInterface) /* throws Exception */ {
		try {
			functions=null;
			variables=null;
			oFunctionInterface = p_functInterface;
			oVariableInterface = p_varInterface;
			Object res = new com.kikyosoft.parser.whereclpar.Parser(p_ignoreCase,p_string,this,this).parse();
			if(res instanceof Expression) exp = (Expression) res; else {
				/* assumed res is Condition */
				exp = new Expression((Condition) res);
			}
		} catch (Exception ex) {
			LogUtil.log(ex);
		}
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
			HashSet<com.kikyosoft.parser.Function> vHash = exp.getFunctionHash(new HashSet());
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
			LogUtil.log(ex);
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
			CoreLog.log(ex);
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
