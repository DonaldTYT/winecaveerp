package com.uniinformation.bicore;

import java.io.*;
import java.util.*;

import com.uniinformation.utils.*;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.cell.*;

public class BiJoin extends BiBase {
//	BiTable toTable;
//	BiField fromField;
//	BiField toField;
	private boolean optional=false;
	private boolean onetoone=false;
	private String condition=null;
	class JoinRec {
		BiField fromField;
		BiField toField;
		
		JoinRec(BiField p_from,BiField p_to) {
			fromField = p_from;
			toField = p_to;
		}
	}
	Vector<JoinRec> joins;
	public BiJoin(CellCollection parent,BiTable p_toTable,BiField p_fromField,BiField p_toField,boolean p_optional,String p_condition,boolean p_onetoone)
	{
		super(parent);
		joins = new Vector();
		optional = p_optional;
		onetoone = p_onetoone;
		if(!p_condition.isEmpty()) condition = p_condition;
		{
			if(optional) {
				//UniLog.log("HAHA 20170906 set optional " + p_fromField.getName() + " -> " + p_toField.getName());
			}
		}
		addCollection("toTable",p_toTable);
		/*
		addCollection("fromField",p_fromField);
		addCollection("toField",p_toField);
		*/
		joins.add(new JoinRec(p_fromField,p_toField));
		for(CellCollection c = this.getParent();c != null; c = c.getParent()){
			if(c instanceof BiTable) {
				addCollection("fromTable",c);
			}
		}
		/*
		UniLog.log("HAHA new BiJoin " +
				((BiTable) getCollection("fromTable")).getName() + " : " +
				((BiField) getCollection("fromField")).getName() + " : " +
				((BiTable) getCollection("toTable")).getName() + " : " +
				((BiField) getCollection("toField")).getName());
		*/
	}

	public String getPredicate()
	{
		/*
		return(
					((BiTable) getCollection("toTable")).getName()+"."+((BiField) getCollection("toField")).getName()
					+ "=" + 
					((BiTable) getCollection("fromTable")).getName() + "." + ((BiField)getCollection("fromField")).getName() 
					);
		*/
		String s = "";
		for(int i = 0;i<joins.size();i++) {
			JoinRec jr = (JoinRec) joins.get(i);
			if( i > 0) s += " and ";
			s += ((BiTable) getCollection("toTable")).getName()+"."+jr.toField.getName()
					+ "=" + 
					((BiTable) getCollection("fromTable")).getName() + "." + jr.fromField.getName() 
					;
			
		}
		return(s);
	}
	public int getJoinCount()
	{
		return(joins.size());
	}
	/***
	 * same as getJoinCount
	 */
	public int size()
	{
		return(joins.size());
	}
	public BiField getFromField(int p_idx)
	{
//		return((BiField)getCollection("fromField"));
		return(((JoinRec) joins.get(p_idx)).fromField);
	}
	public BiField getToField(int p_idx)
	{
		return(((JoinRec) joins.get(p_idx)).toField);
//		return((BiField)getCollection("toField"));
	}
	public List<BiField> getFromFields() {
		ArrayList<BiField> fieldList = new ArrayList<BiField>();
		for (JoinRec jr : joins){
			fieldList.add(jr.fromField);
		}
		return(fieldList);
	}
	public List<BiField> getToFields() {
		ArrayList<BiField> fieldList = new ArrayList<BiField>();
		for (JoinRec jr : joins){
			fieldList.add(jr.toField);
		}
		return(fieldList);
	}
	public void addJoin(BiField p_fromField,BiField p_toField)
	{
		joins.add(new JoinRec(p_fromField,p_toField));
	}
	
	public boolean isOptional() {
		return(optional);
	}

	public boolean isOneToOne() {
		return(onetoone);
	}
	public String getBaseCondition() {
		return(condition);
	}
	public String getParsedCondition(BiResult p_br) {
		if(condition != null) {
			try {
				Condition baseCondition = (Condition) p_br.whereStrParser.parse(condition);
				return(baseCondition.toString());
			} catch(Exception ex) {
				UniLog.log(ex);
				return("ERROR: baseWhereString Syntax error");
			}
		}
		return(condition);
	}
}
