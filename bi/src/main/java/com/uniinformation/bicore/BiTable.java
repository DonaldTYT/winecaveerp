package com.uniinformation.bicore;
import java.io.*;
import java.util.*;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.utils.*;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.cell.*;

/***
 *  Primitive object used for define table structure
 *  single object share by views, therefore can use '==' for comparison
 */
public class BiTable extends BiBase {
	Cell tableName;
	Cell dbtName;
	Cell eName;
	Cell cName;
	Cell selectWhere;
//	CellCollection fields;
//	CellCollection forwardJoins;
	Hashtable backwordJoins;
//	String primaryKey;
	String primaryKeys[];
	String serialId;

	public BiTable(CellCollection parent,String p_tableName,String p_dbtName,String p_eName,String p_cName,String p_selectWhere,String p_primaryKey,String p_serialid) 
	{
		super(parent);
		if(p_tableName.equals("stock")) {
			int cc;
			cc = 0;
		}
		if(p_tableName.equals("stocknames")) {
			int cc;
			cc = 0;
		}
		tableName = new Cell(p_tableName.trim());
		if(p_dbtName == null || p_dbtName.trim().equals("")) dbtName = tableName; else dbtName = new Cell(p_dbtName.trim());
		if(p_eName == null || p_eName.trim().equals("")) eName = tableName; else eName = new Cell(p_eName.trim());
		if(p_cName == null || p_eName.trim().equals("")) cName = eName; else cName = new Cell(p_cName.trim());
		if(p_selectWhere != null && !(p_selectWhere.trim().equals("")) ) selectWhere = new Cell(p_selectWhere.trim()); else selectWhere = new Cell("");
		addCollection("fields",new CellCollection(this));
		addCollection("forwardJoins",new CellCollection(this));
//		primaryKey = p_primaryKey;
		if(!StringUtils.isBlank(p_primaryKey)) {
			if(p_primaryKey.indexOf(",") >= 0) {
				String keys[] = p_primaryKey.split(",");
				int n = 0;
				for(int i=0;i<keys.length;i++) {
					if(!StringUtils.isBlank(keys[i])) {
						n++;
					}
				}
				if(n > 0) {
					primaryKeys = new String[n];
					n = 0;
					for(int i=0;i<keys.length;i++) {
						if(!StringUtils.isBlank(keys[i])) {
							primaryKeys[n] = keys[i];
							n++;
						}
					}
				}
			} else {
				primaryKeys = new String[1];
				primaryKeys[0] = p_primaryKey;
			}
		}
		serialId = p_serialid;
		backwordJoins = new Hashtable();
		if("noserial".equals(serialId)) serialId = null; else {
			if(serialId == null || serialId.trim().equals("")) serialId = "serial_id";
		}
	}
	public BiField addField(String p_field,String p_ename,String p_cname,String p_type,int p_len,int p_flag)
	{
		BiField field = new BiField(getCollection("fields"),p_field,p_ename,p_cname,p_type,p_len,p_flag);
		getCollection("fields").addCollection(p_field,field);
		return(field);
	}
	public BiField getField(String p_field)
	{
		return((BiField) getCollection("fields").getCollection(p_field));
	}
	public List<BiField> getFields(){
		Object[] fieldObjs = (getCollection("fields").getCollections());
		ArrayList<BiField> fieldList = new ArrayList<BiField>();
		if (fieldObjs == null || fieldObjs.length == 0){
			return(fieldList);
		}
		for (Object fieldObj : fieldObjs){
			if (fieldObj instanceof BiField){
				fieldList.add((BiField)fieldObj);
			}
		}
		return(fieldList);
	}
	public boolean addJoin(String p_toTable ,String p_fromField,String p_toField,boolean p_optional,String p_condition,boolean p_onetoone)
	{
		BiField fromField,toField;
		BiTable toTable;
		if((toTable = getTable(p_toTable)) == null) {
				return(false);
		}
		if((fromField = getField(p_fromField)) == null) {
				return(false);
		}
		if((toField = toTable.getField(p_toField)) == null) {
			return(false);
		}
		BiJoin join;
		join = getJoin(p_toTable);
		if(join != null) {
			join.addJoin(fromField,toField);
			return(true);
		}
		join = new BiJoin(getCollection("forwardJoins"),toTable,fromField,toField,p_optional,p_condition,p_onetoone);
		getCollection("forwardJoins").addCollection(p_toTable,join);
		toTable.addBackwordJoin(tableName.getString(),this);
		return(true);
	}
	
	public BiJoin getJoin(BiTable p_toTable) {
		if (p_toTable == null) {
			return null;
		}
		return(getJoin(p_toTable.getName()));
	}
	public BiJoin getJoin(String p_toTable) {
		/*
		CellCollection col = getCollection_SingleLevel("forwardJoins");
		if(col == null) return(null);
		CellCollection col2 = col.getCollection(p_toTable);
		UniLog.log("getJoin "+p_toTable+ " got " + col);
		return((BiJoin) col2);
		*/
	
		return((BiJoin) getCollection_SingleLevel("forwardJoins").getCollection_SingleLevel(p_toTable));
	}
	/***
	 * get name of forward joins
	 * andrew190508: newly added method
	 * @return
	 */
	public String[] getJoinTableNames(){
		String[] joinNames = getCollection_SingleLevel("forwardJoins").listCollection_SingleLevel();
		UniLog.log1("joinNames:"+ joinNames);
		return(joinNames);
	}
	/***
	 * get all forward joins
	 * andrew190508: newly added method
	 * @return
	 */
//	public BiJoin[] getJoins(){
//		BiJoin[] joins = null;
//		Object[] objs = getCollection_SingleLevel("forwardJoins").getCollections();
//		if (objs != null && objs.length > 0){
//			joins = Arrays.copyOf(objs, objs.length, BiJoin[].class);
//			for (BiJoin join : joins){
//				for (BiField field : join.getFromFields()){
//					UniLog.log1("debug:from:%s:%s",field.getTable(),field.getName());
//				}
//				for (BiField field : join.getToFields()){
//					UniLog.log1("debug:to:%s:%s",field.getTable(),field.getName());
//				}
//				
//			}
//		}
//		//UniLog.log1("joins:%s",joins);
//		return(joins);
//	}

	protected boolean addBackwordJoin(String p_tabname,BiTable p_table)
	{
		backwordJoins.put(p_tabname,p_table);
		return(true);
	}

	public String getName()
	{
		return(tableName.getString());
	}
	
	public String getWherecl()
	{
		return(selectWhere.getString());
	}
	public String getSidField()
	{
		if(serialId == null) return("0");
		return(getName()+"."+serialId);
	}
	
	public String getDbtName()
	{
		return(dbtName.getString());
	}
	public String getSelectFromName()
	{
		if(tableName.getString().equals(dbtName.getString())) {
			return(tableName.getString());
		} else {
			return(dbtName.getString()+" "+tableName.getString());
		}
		
	}
	
	/*
	public TableRec newTableRec() {
		BiField fds[] = (BiField[]) (getCollection("fields").getCollections());
		String fdname[] = new String[fds.length];
		int fdtype[] = new int[fds.length];
		for(int i=0;i<fds.length;i++) {
			fds[i].getName();
			fds[i].getFieldType();
			fdname[i] = fds[i].getName();
			if(fds[i].getFieldType().equals("integer"))
			fdtype[i] = BiSchema.getSqlTypeFromFieldType(fds[i].getFieldType());
		}
		TableRec tr = new TableRec(getName(),fdname,fdtype);
		return(tr);
	}
	*/
	public TableRec newTableRec() {
		Object fds[] = (getCollection("fields").getCollections());
		String fdname[] = new String[fds.length];
		int fdtype[] = new int[fds.length];
		for(int i=0;i<fds.length;i++) {
			BiField fd = (BiField) fds[i];
			fd.getName();
			fd.getFieldType();
			fdname[i] = fd.getName();
			fdtype[i] = BiSchema.getSqlTypeFromFieldType(fd.getFieldType());
		}
		TableRec tr = new TableRec(getName(),fdname,fdtype);
//		try {
//			
//		for(int i=0;i<fdname.length;i++) {
//			UniLog.log("HAHA 2016 field " + i  + " = " + tr.getFieldName(i) + " type " + tr.getFieldType(i));
//			
//		}
//		} catch (Exception ex) {
//			UniLog.log(ex);
//		}
//		UniLog.log("HAHA 2016 serial_id idx (B) = " + tr.getFieldIndex("serial_id"));
		return(tr);
	}
	public String[] getPrimaryKeys() {
		return(primaryKeys);
	}
	public String getPrimaryKey()
	{
		if(primaryKeys.length > 0) {
			return(primaryKeys[0]);
		}
		return(null);
//		return(primaryKey);
	}
	public String toString(){
		return(getName());
	}
	public String getSerialId()
	{
		return(serialId);
	}
	
	protected Wherecl getFieldUniqueListAppendWhere(BiField p_fd,SessionHelper p_sh) {
		return(null);
	}

//	public BiTableRec newBiTableRec() {
//		BiTableRec bitr = new BiTableRec(newTableRec());
//		return(bitr);
//	}
//	public BiTableRec queryBiTableRecBySerialId(SelectUtil su,int p_sid) throws Exception {
//		TableRec tr = su.getQueryResult(
//					"select * from "+getSelectFromName()+ " where " + getSerialId() + " = "+p_sid,null
//				);
//		tr.setRecPointer(0);
//		BiTableRec bitr = new BiTableRec(tr);
//		return(bitr);
//	}
//	public void updateBiTableRecBySerialId(BiTableRec p_tr,SelectUtil su,Vector colList) throws Exception {
//		su.updateByTableRec(getDbtName(), p_tr.getTableRec(), colList, colList, 
//						new Wherecl().andUniop(getSerialId(), "=",p_tr.getTableRec().getField(getSerialId()) ).stripAnd());
//	}
//	public void insertBiTableRec(BiTableRec p_tr,SelectUtil su) throws Exception {
//		su.insertByTableRec(getDbtName(), p_tr.getTableRec(), true, getSerialId());
//	}
}
