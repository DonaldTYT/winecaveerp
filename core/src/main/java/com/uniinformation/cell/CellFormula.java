package com.uniinformation.cell;

import java.io.*;
import java.util.*;

import com.kyoko.common.CoreLog;
import com.uniinformation.utils.*;
import com.uniinformation.utils.exprpar.*;

public class CellFormula implements Serializable
{
	static final long serialVersionUID = 3944821432683188433L;
	protected CellCollection collection;
	protected String formula;
	protected transient Parser parser = null;
	public CellFormula (String p_formula,CellCollection p_collection)
	{
		formula = p_formula;
		collection = p_collection;
		initParser();
	}
	protected void initParser()
	{
//		parser = new Parser(formula,collection,collection);
		parser = new Parser(collection.getCompareMode(),formula);
	}
	public void setTrigger(Cell p_cell)
		{
			/* debug */
			Vector v=null;
			if(parser == null) initParser();
			parser.setFunctInterface(collection);
			parser.setVarInterface(collection);
			try {
				parser.collect();
				v = parser.getVariablesUnIndexed();
			} catch (Exception e) {
				CoreLog.log(e);
			}
			for(Enumeration e = v.elements();e.hasMoreElements();) {
				String s = (String) e.nextElement();
				Cell c = collection.testCell(s);
				if(c != null) {
					c.addTrigger(p_cell);
				}
			}
			/*
			v = parser.getFunctions();
			for(Enumeration e = v.elements();e.hasMoreElements();) {
				String s = (String) e.nextElement();
				Cell c = collection.getCell(s);
				if(c != null) {
					c.addTrigger(p_cell);
				}
			}*/
		}
	public void unsetTrigger(Cell p_cell)
		{
			/* debug */
			Vector v = null;
			if(parser == null) initParser();
			parser.setFunctInterface(collection);
			parser.setVarInterface(collection);
			try {
				parser.collect();
				v = parser.getVariables();
			} catch (Exception e) {
				CoreLog.log(e);
			}
			for(Enumeration e = v.elements();e.hasMoreElements();) {
				String s = (String) e.nextElement();
				Cell c = collection.getCell(s);
				if(c != null) {
					c.delTrigger(p_cell);
				}
			}
			/*
			v = parser.getFunctions();
			for(Enumeration e = v.elements();e.hasMoreElements();) {
				String s = (String) e.nextElement();
				Cell c = collection.getCell(s);
				if(c != null) {
					c.addTrigger(p_cell);
				}
			}*/
		}
	public Object eval() throws CellException
	{
		Object o = null;
		if(parser == null) initParser();
		try {
			parser.setFunctInterface(collection);
			parser.setVarInterface(collection);
			o = parser.evaluate();
		} catch (Exception e) {
			if(e instanceof CellException) {
				if(((CellException) e).getExceptionType() == CellException.CELLEXCEPTION_EVAL_ERROR) {
					throw((CellException) e);
				}
			}
			CoreLog.log(e);
		}
		return(o);
	}

	public String toString()
	{
		return(formula);
	}
	
	public Vector getFunctions() throws CellException{
		if(parser == null) initParser();
		parser.setFunctInterface(collection);
		parser.setVarInterface(collection);
		return(parser.getFunctions());
	}

	public Vector getVariables() throws CellException{
		if(parser == null) initParser();
		parser.setFunctInterface(collection);
		parser.setVarInterface(collection);
		return(parser.getVariables());
	}
}
