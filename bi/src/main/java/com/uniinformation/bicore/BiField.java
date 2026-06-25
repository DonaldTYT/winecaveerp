package com.uniinformation.bicore;
import java.io.*;
import java.util.*;
import com.uniinformation.utils.*;
import com.kyoko.common.DateUtil;
import com.uniinformation.cell.*;

/***
 *  Primitive object used for define data field
 *  Child of BiTable
 *
 */
public class BiField extends BiBase {
	Cell fieldName;
	/*
	Cell eName;
	Cell cName;
	*/
	Cell fieldType;
	Cell fieldLen;
	Cell flag;
	public BiField(CellCollection parent,String p_fieldName,String p_eName,String p_cName,String p_type,int p_len,int p_flag)
	{
		super(parent);
		fieldName = new Cell(p_fieldName.trim());
		/*
		if(p_eName == null || p_eName.trim().equals("")) eName = fieldName; else eName = new Cell(p_eName.trim());
		if(p_cName == null || p_cName.trim().equals("")) cName = eName; else cName = new Cell(p_cName.trim());
		*/
		fieldType = new Cell(p_type.trim());
		fieldLen  = new Cell(p_len);
		flag = new Cell(p_flag);
	}

	public String getName()
	{
		return(fieldName.getString());
	}
	/*
	public String getEName()
	{
		return(eName.getString());
	}
	public String getCName()
	{
		return(cName.getString());
	}
	*/
	public boolean isNumber() {
		String ft = fieldType.getString();
		return(ft.matches("float|money|serial|integer|decimal||smallint"));
	}
	public String getFieldType()
	{
		return(fieldType.getString());
	}
	public int getFieldLength()
	{
		String ft = fieldType.getString();
		if(ft.equals("integer")) return(10);
		else if(ft.equals("date")) return(10);
		else if(ft.equals("float")) return(14);
		else if(ft.equals("money")) {
			return(20);
		}
		else if(ft.equals("decimal")) {
			return(20);
		}
		return(fieldLen.getInt());
	}
	
	public Object getEmptyObject() {
		String ft = fieldType.getString();
		if(ft.equals("integer")) return(0);
		else if(ft.equals("date")) return(DateUtil.zeroDate);
		else if(ft.equals("float")) return(0.0);
		else if(ft.equals("money")) {
			return(0.0);
		}
		else if(ft.equals("decimal")) {
			return(0.0);
		} else  return("");
		
	}
	public BiTable getTable()
	{
		CellCollection c;
		for(c = getParent();c != null;c=c.getParent()){
			if(c instanceof BiTable) return((BiTable) c);
		}
		return(null);
	}
	
	public String getFullName() {
		return( getTable().getName()+ "." + fieldName.getString());
	}
	public String toString(){
		return(getFullName());
	}
	public static List<BiColumn> getBiColumns(BiField p_field, Vector<BiColumn> p_cols){
		ArrayList<BiColumn> biColumns = new ArrayList<BiColumn>();
		for (BiColumn biCol : p_cols){
			if (biCol.getField() == p_field){
				biColumns.add(biCol);
			}
		}
		return(biColumns);
	}
	public List<Object> getUniqueList(SelectUtil p_su,Wherecl p_where) {
		try { 
			String wstr = getTable().getWherecl();
			if(!wstr.trim().equals("")) wstr += " and ";
			wstr += getName() +  " is not null";
			Wherecl wcl = new Wherecl();
			/*
			TableRec tr = p_su.getQueryResult("select distinct " + getName() + " from " + getTable().getSelectFromName()
						 + " where " + wstr
					);
					*/
			/*
			if(p_where != null) {
				wcl.andWherecl(p_where);
				wcl.appendString(" and " + wstr);
			} else {
				wcl.appendString(wstr);
			}
			*/
			if(p_where != null) wcl.andWherecl(p_where);
			wcl.appendString(" and " + wstr).stripAnd();
			wcl.appendString(" order by 1");
			TableRec tr = p_su.getQueryResult("select distinct " + getName() + " from " + getTable().getSelectFromName() , wcl);
			ArrayList<Object> al = new ArrayList<Object>();
			for(int i = 0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				al.add(tr.getField(0));
			}
			return(al);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		return(null);
	}
}
