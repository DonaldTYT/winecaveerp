package com.uniinformation.utils;
import java.util.*;
import java.util.Hashtable;
import java.math.BigDecimal;

import com.kyoko.common.DateUtil;
import com.kyoko.common.NumberUtil;
import com.kyoko.common.StringUtil;
import com.uniinformation.cell.*;
/**
 * Maintain a resultset of a table of a query
 */
public class TableRec implements java.io.Serializable {
   static final long serialVersionUID = -5147399207273097810L;
   private static boolean fDebug = false;
   static final String DEFAULT_DATEFORMAT = "yyyy/mm/dd";
   // the name of the table
   private String tableName = null;
   // store the definition of the table
   private FieldDefine fieldDef = null;
   private int columnCount = 0;
   // store the resultset of the table or a query for the table
   private Vector <Object[]> data = null;
   // a pointer locating the current record
   private int recPointer = -1;
   private boolean validIns = false;
   private Hashtable h_format = null;
   private String dateFormat = DEFAULT_DATEFORMAT;
   private Vector userData = null;
   private class FieldDefine implements java.io.Serializable {
      // Store all field name, maintain relationship of field name and field index
      private Hashtable field = null;
      // Store all field name for returning an array of field name
      private String fieldName[];
      private int[] coltypes = null;
      private int[] collens = null;
      /**
       * Initialize a newly created <code>FieldDefine</code> object so that it
       * represents the schema of a table.
       *
       * @param    p_fieldName   all field name of a table.
       */
      public FieldDefine(String[] p_fieldName) {
         field = new Hashtable(p_fieldName.length, 1);
         for (int i=0; i < p_fieldName.length; i++)
           field.put(p_fieldName[i].toLowerCase(), new Integer(i));
         fieldName = p_fieldName;
      }
      public FieldDefine(String[] p_fieldName, int[] p_coltypes) {
         this(p_fieldName);
         coltypes = p_coltypes;
      }
      public FieldDefine(String[] p_fieldName, int[] p_coltypes, int[] p_collens) {
         this(p_fieldName, p_coltypes);
         collens = p_collens;
      }
      /**
       * Return the index of the specifically field.
       *
       * @param    p_fieldName   the name of the specifically field.
       * @return   the field index.
       */
      public int getFieldIndex(String p_fieldName) { 
         Integer ii = (Integer) field.get(p_fieldName);
         if (ii == null)
            UniLog.log(new Exception("Field "+p_fieldName+" not found"));
         return(ii.intValue());
      }
      /**
       * Return the count of all fields of the table.
       *
       * @return   the field count.
       */
      public int getFieldCount() {
         return(fieldName.length);
      }
      /**
       * Return all fields of the table.
       *
       * @return   the name of all fields.
       */
      public String[] getFieldNames() {
         return(fieldName);
      }
      /**
       * Check if the specifically field exists in the table
       *
       * @param    p_fldName   the name of the specifically field.
       * @return   <code>true</code> if the field exists;
       *           <code>false</code> otherwise.
       */
      public boolean existField(String p_fldName) {
         return field.containsKey(p_fldName);
      }
      /**
       * Check if the specifically field index is valid.
       *
       * @param    index   the specifically index
       * @return   <code>true</code> if the index is valid;
       *           <code>false</code> otherwise.
       */
      public boolean existFieldIndex(int index) {
         return (index >= 0 && index < fieldName.length);
      }
      public String toString() {
         return(
            new StringBuffer()
                .append("<FieldDefine>")
                .append("<field>").append(""+field).append("</field>")
                .append("<fieldName>").append(""+fieldName).append("</fieldName>")
                .append("</FieldDefine>")
                .toString()
         );
      }
      // java.sql.Types.*
      public int getFieldType(int p_idx) {
         if (coltypes == null)
            return(-1);
         if (p_idx < 0)
            return(-1);
         else if (p_idx >= coltypes.length)
            return(-1);
         return(coltypes[p_idx]);
      }
      // java.sql.Types.*
      public int[] getColtypes() {
         return(coltypes);
      }
      public int getColumnDisplaySize(int p_idx) {
         if (collens == null)
            return(-1);
         if (p_idx < 0)
            return(-1);
         else if (p_idx >= collens.length)
            return(-1);
         return(collens[p_idx]);
      }
      public int[] getColumnDisplaySizes() {
         return(collens);
      }
   }
   /**
    * Initialize a newly created <code>TableRec</code> object so that it
    * represents a resultset of a table or a query.
    *
    * @param    name    the name of the table.
    * @param    fields  the field name of the table.
    */
   public TableRec(String name, String fields[]) {
   	  if (fDebug) UniLog.log1("name:%s fields:%s types:%s", name, fields ==null ? "null" : Arrays.toString(fields), "null");
      tableName = name;
      fieldDef = new FieldDefine(fields);
      data = new Vector();
      userData = new Vector();
      validIns = true;
      columnCount = fields.length;
   }
   public TableRec(String name, String[] fields, int[] coltypes) {
   	  if (fDebug) UniLog.log1("name:%s fields:%s types:%s", name, fields ==null ? "null" : Arrays.toString(fields), coltypes == null ? "null": Arrays.toString(coltypes));
      tableName = name;
      fieldDef = new FieldDefine(fields, coltypes);
      data = new Vector();
      userData = new Vector();
      validIns = true;
      columnCount = fields.length;
   }
   public TableRec(String name, String[] fields, int[] coltypes, int[] collens) {
   	  if (fDebug) UniLog.log1("name:%s fields:%s types:%s", name, fields ==null ? "null" : Arrays.toString(fields), coltypes == null ? "null": Arrays.toString(coltypes));
      tableName = name;
      fieldDef = new FieldDefine(fields, coltypes, collens);
      data = new Vector();
      userData = new Vector();
      validIns = true;
      columnCount = fields.length;
   }
   /**
    * Initialize a newly created <code>TableRec</code> object so that it
    * represents a resultset of a table or a query.
    *
    * @param    colcount  the field count of the recordset.
    */
   public TableRec(int colcount) {
      if (colcount > 0) {
         columnCount = colcount;
         data = new Vector();
         userData = new Vector();
         validIns = true;
      }
   }
   /**
    * Return the record pointer
    *
    * @return   the record pointer
    */
   public int getRecPointer() {
      return recPointer;
   }
   /**
    * Move the record pointer
    *
    * @param    p_recPointer   the location of record.
    * @return   the previous location of the record pointer
    * @throws   atoms.utils.table.TableRecException(INVALID_RECORD_POINTER) if <code>p_recPointer</code>
    *           is out of the valid record bounds.
    * @throws   atoms.utils.table.TableRecException(INVALID_INSTANCE) if the instance is not available.
    */
   public int setRecPointer(int p_recPointer)  
              throws TableRecException {
      if (p_recPointer < 0 || p_recPointer >= data.size()) 
         throw new TableRecException(TableRecException.INVALID_RECORD_POINTER);
      int prev = recPointer;
      recPointer = p_recPointer;   
      return prev;
   }
   /**
    * Move the record pointer to the first record.
    *
    * @return   <code>true</code> if the resultset is valid;
    *           <code>false</code> otherwise.
    * @throws   atoms.utils.table.TableRecordPointer(INVALID_INSTANCE) if the instance is not available.
    */
   public boolean first() throws TableRecException{
      if (isEmpty())
         return false;
      recPointer = 0;
      return true;
   }
   /**
    * Move the record pointer to the last record.
    *
    * @return   <code>true</code> if the resultset is valid;
    *           <code>false</code> otherwise.
    * @throws   atoms.utils.table.TableRecException(INVALID_INSTANCE) if the instance is not available.
    */
   public boolean last() throws TableRecException{
      if (isEmpty())
         return false;
      recPointer = getRecordCount() - 1;
      return true;
   }
   /**
    * Move the record pointer to the next record.
    *
    * @return   <code>true</code> if the resultset is valid and 
    *           the record pointer does not point to the last record;
    *           <code>false</code> otherwise.
    * @throws   atoms.utils.table.TableRecException(INVALID_INSTANCE) if the instance is not available.
    */
   public boolean next() throws TableRecException {
      if (isEmpty())
         return false;
      if (recPointer < getRecordCount() - 1) {
         recPointer ++;
        return true;
      }
      else
         return false;
   }
   /**
    * Move the record pointer to the last record.
    *
    * @return   <code>true</code> if the resultset is valid and 
    *           the record pointer does not point to the first record;
    *           <code>false</code> otherwise.
    * @throws   atoms.utils.table.TableRecException(INVALID_INSTANCE) if the instance is not available.
    */
   public boolean prev() throws TableRecException {
      if (isEmpty())
         return false;
      if (recPointer > 0) {
         recPointer --;
        return true;
      }
      else
         return false;
   }
   /**
    * Add a new record and the record pointer will be located at the last record.
    *
    * @return    the pointer of the new empty record.
    * @throws   atoms.utils.table.TableRecException(INVALID_INSTANCE) if the instance is not available.
    */
   public int addRecord() throws TableRecException {
      Object obj[] = null;
      if (fieldDef != null)
         obj = new Object[fieldDef.getFieldCount()];
      else 
         obj = new Object[columnCount];
      data.addElement(obj);
      userData.addElement(null);
      recPointer = data.size() - 1;
      return recPointer;
   }
   public int addRecord(int p_atidx) throws TableRecException {
      Object obj[] = null;
      if (fieldDef != null)
         obj = new Object[fieldDef.getFieldCount()];
      else 
         obj = new Object[columnCount];
      try {
         data.add(p_atidx, obj);
         userData.add(p_atidx, null);
      } catch (ArrayIndexOutOfBoundsException e) {
         throw new TableRecException(TableRecException.INVALID_RECORD_POINTER);
      }
      recPointer = p_atidx;
      return recPointer;
   }
   public void clear() {
      data.clear();
      userData.clear();
      recPointer = -1;
   }
   /**
    * Delete a record;
    * the record pointer will be located at: 
    *       the last record if the deleted record is the last record;
    *     the original location otherwise.
    *     -1 if the recordset become empty.
    *
    * @param    the pointer of the deleted record.
    * @throws   atoms.utils.table.TableRecException(EMPTY_TABLE) if not data.
    * @throws   atoms.utils.table.TableRecException(INVALID_RECORD_POINTER) if <code>row</code>
    *           is out of the valid record bounds.
    * @throws   atoms.utils.table.TableRecException(INVALID_INSTANCE) if the instance is not available.
    */
   public void deleteRecord(int row) throws TableRecException {
      if (isEmpty())
         throw new TableRecException(TableRecException.EMPTY_TABLE, getEmptyTableMessage());
      try {
         data.removeElementAt(row);
         userData.removeElementAt(row);
         if (data.size() == 0)                  // recordset is empty
            recPointer = -1;
         else if (recPointer == data.size())      // the last record
            recPointer --;
      }
      catch (ArrayIndexOutOfBoundsException e) {
         throw new TableRecException(TableRecException.INVALID_RECORD_POINTER);
      }
   }
   /**
    * Get the value of the specifically field of the current record.
    *
    * @param     key    the name of the specifically field.
    * @return    the value of the field of the current record.
    * @throws    atoms.utils.table.TableRecException(EMPTY_TABLE) if not data.
    * @throws    atoms.utils.table.TableRecException(INVALID_FIELD_NAME) if <code>key</code> is invalid fieldname.
    * @throws   atoms.utils.table.TableRecException(INVALID_INSTANCE) if the instance is not available.
    */
   public Object getField(String key) throws TableRecException {
      if (isEmpty())
         throw new TableRecException(TableRecException.EMPTY_TABLE, getEmptyTableMessage());
      String field= key.toLowerCase();
      if (! fieldDef.existField(field)) {
    	 String fn[] = fieldDef.getFieldNames();
//    	 for(int i = 0;i<fn.length;i++) {
//    		UniLog.log("HAHA 2016 field_"+i+" : " + fn[i]); 
//    	 }
         throw new TableRecException(TableRecException.INVALID_FIELD_NAME, "The field '" + key + "' is not exist.");
      }
      Object[] rec = (Object[])data.elementAt(recPointer);
      Object obj = rec[fieldDef.getFieldIndex(field)];
      return obj;
   }
   public Object getField(String key, int p_idx) throws TableRecException {
      if (isEmpty())
         throw new TableRecException(TableRecException.EMPTY_TABLE, getEmptyTableMessage());
      String field = key.toLowerCase();
      if (! fieldDef.existField(field)) {
         throw new TableRecException(TableRecException.INVALID_FIELD_NAME, "The field '" + key + "' is not exist.");
      }
      Object[] rec = (Object[])data.elementAt(p_idx);
      Object obj = rec[fieldDef.getFieldIndex(field)];
      return obj;
   }
   public int getFieldInt(String key) throws TableRecException {
      return(((Integer) getField(key)).intValue());
   }
   
   //andrew230111 handle long for gbp checkpoint report
   public long getFieldLong(String key) throws TableRecException {
      return(((Long) getField(key)).longValue());
   }
   
   public double getFieldDouble(String key) throws TableRecException {
      return(((Double) getField(key)).doubleValue());
   }
   public String getFieldString(String key) throws TableRecException {
      return(getFieldString(key, recPointer));
   }
   public java.util.Date getFieldDate(String key) throws TableRecException {
      return(getFieldDate(key, recPointer));
   }
   public java.util.Date getFieldTime(String key) throws TableRecException {
      return(getFieldTime(key, recPointer));
   }
   public int getFieldInt(String key, int p_idx) throws TableRecException {
      return(((Integer) getField(key, p_idx)).intValue());
   }
   public double getFieldDouble(String key, int p_idx) throws TableRecException {
      return(((Double) getField(key, p_idx)).doubleValue());
   }
   public String getFieldString(String key, int p_idx) throws TableRecException {
      Object obj = getField(key, p_idx);
      if (obj == null)
         obj = TableBrowser.getDefaultValue(getFieldType(key));
      return(fieldObjectToString(obj));
   }

   public String getFieldString(int index, int p_rowidx) throws TableRecException {
      Object[] rec = (Object[]) data.elementAt(p_rowidx);
      return(fieldObjectToString(rec[index]) );
   }
   
   public String fieldObjectToString(Object obj) {
      if (obj instanceof String) 
         return((String) obj);
      else if (obj instanceof java.util.Date) 
         return(DateUtil.toDateString((java.util.Date) obj, dateFormat));
      else if (obj instanceof java.sql.Date) 
         return(DateUtil.toDateString((java.sql.Date) obj, dateFormat));
      else if (obj instanceof Double) 
			return(StringUtil.ftostr(((Double) obj).doubleValue(), "#.#########"));
      else if (obj instanceof Float) 
			return(StringUtil.ftostr(((Float) obj).doubleValue(), "#.#########"));
      else
         return(obj.toString());
   }
   
   public java.util.Date getFieldDate(String key, int p_idx) throws TableRecException {
      Object obj = getField(key, p_idx);
      if (obj instanceof java.util.Date)
         return((java.util.Date) obj);
      return(new java.util.Date(((java.sql.Date) obj).getTime()));
   }
   public java.util.Date getFieldTime(String key, int p_idx) throws TableRecException {
      Object obj = getField(key, p_idx);
      if (obj instanceof java.util.Date)
         return((java.util.Date) obj);
      else if (obj instanceof java.sql.Date)
         return(new java.util.Date(((java.sql.Date) obj).getTime()));
      else if (obj instanceof Integer) {
         if (((Integer) obj).intValue() == 0)
            return(null);
         return(new java.util.Date(((Integer) obj).longValue() * 1000));
      }
      else
         return(null);
   }
   public int getFieldType(int p_idx) {
      return(fieldDef.getFieldType(p_idx));
   }
   public int getFieldType(String key) throws TableRecException {
      String field = key.toLowerCase();
      if (! fieldDef.existField(field))
         throw new TableRecException(TableRecException.INVALID_FIELD_NAME, "The field '" + key + "' is not exist.");
      return(fieldDef.getFieldType(fieldDef.getFieldIndex(field)));
   }
   /**
    * Set the value of the specifically field of the current record.
    *
    * @param     key    the name of the specifically field.
    * @param     value  the value of the specifically field.
    * @throws    atoms.utils.table.EmptyTableException if not data.
    * @throws    atoms.utils.table.InvalidFieldNameException if <code>key</code> is invalid fieldname.
    * @throws   atoms.utils.table.InvalidInstanceException if the instance is not available.
    */
   public void setField(String key, Object value) throws TableRecException{
      if (isEmpty())
         throw new TableRecException(TableRecException.EMPTY_TABLE, getEmptyTableMessage());
      String field = key.toLowerCase();
      if (! fieldDef.existField(field))
         throw new TableRecException(TableRecException.INVALID_FIELD_NAME, "The field '" + key + "' is not exist.");
      Object[] rec = (Object[])data.elementAt(recPointer);
      rec[fieldDef.getFieldIndex(field)] = value;
   }
   /**
    * Get the value of the specifically field of the current record.
    *
    * @param     index   the index of the specifically field.
    * @return    the value of the field of the current record.
    * @throws    atoms.utils.table.EmptyTableException if not data.
    * @throws    atoms.utils.table.InvalidFieldIndexException if <code>index</code> is invalid fieldindex.
    * @throws   atoms.utils.table.InvalidInstanceException if the instance is not available.
    */
   public Object getField(int index) throws TableRecException {
      if (isEmpty())
         throw new TableRecException(TableRecException.EMPTY_TABLE, getEmptyTableMessage());
      if (fieldDef != null) {
         if (! fieldDef.existFieldIndex(index))
            throw new TableRecException(TableRecException.INVALID_FIELD_NAME, "The field index is invalid.");
      }
      else if (index < 0 || index >= columnCount)
         throw new TableRecException(TableRecException.INVALID_FIELD_INDEX);
      Object[] rec = (Object[])data.elementAt(recPointer);
      Object obj = rec[index];
      return obj;
   }
   public Object getField(int index, int p_rowidx) throws TableRecException {
      Object[] rec = (Object[]) data.elementAt(p_rowidx);
      return(rec[index]);
   }
   /**
    * Set the value of the specifically field of the current record.
    *
    * @param     index    the index of the specifically field.
    * @param     value    the value of the specifically field.
    * @throws    atoms.utils.table.EmptyTableException if not data.
    * @throws    atoms.utils.table.InvalidFieldIndexException if <code>index</code> is invalid fieldindex.
    * @throws   atoms.utils.table.InvalidInstanceException if the instance is not available.
    */
   public void setField(int index, Object value) 
               throws TableRecException {
      if (isEmpty())
         throw new TableRecException(TableRecException.EMPTY_TABLE, getEmptyTableMessage());
      if (fieldDef != null) {
         if (! fieldDef.existFieldIndex(index))
            throw new TableRecException(TableRecException.INVALID_FIELD_INDEX);
      }
      else if (index < 0 || index >= columnCount)
         throw new TableRecException(TableRecException.INVALID_FIELD_INDEX);
      Object[] rec = (Object[])data.elementAt(recPointer);
      rec[index] = value;
   }
   /**
    * Return a string that is composed of all fieldname of the table.
    *
    * @return    a string that is composed of all fieldname of the table.
    * @throws   atoms.utils.table.InvalidInstanceException if the instance is not available.
    */
   public String getColumnList() throws TableRecException {
      if (fieldDef != null) {   
         StringBuffer sb = new StringBuffer();
         String[] v = fieldDef.getFieldNames();
         for (int i=0; i<fieldDef.getFieldCount(); i++) {
           if (i > 0)
             sb.append(",");
            sb.append(v[i]);
         }
         return sb.toString();
      }
      else
         return null;
   }
   /**
    * Return a string array including all fieldname of the table.
    *
    * @return    a string array.
    * @throws    atoms.utils.table.EmptyTableException if not data.
    * @throws   atoms.utils.table.InvalidInstanceException if the instance is not available.
    */
   public String[] getFieldNames() throws TableRecException {
      if (fieldDef != null)
         return fieldDef.getFieldNames();
      else
         return null;
   }
   public int[] getColtypes() throws TableRecException {
      if (fieldDef != null)
         return fieldDef.getColtypes();
      else
         return null;
   }
   public Vector getFieldNamesVector() throws TableRecException {
      if (fieldDef != null) {   
         Vector vt = new Vector();
         String[] v = getFieldNames();
         for (int i=0; i<v.length; i++) {
            vt.addElement(v[i]);
         }
         return(vt);
      }
      else
         return null;
   }
   public Hashtable getFieldNamesHash() throws TableRecException {
      if (fieldDef != null) {   
         Hashtable h = new Hashtable();
         String[] v = getFieldNames();
         for (int i=0; i<v.length; i++) {
            h.put(v[i], v[i]);
         }
         return(h);
      }
      else
         return null;
   }
   public String getFieldName(String p_fieldName) throws TableRecException {
      return(getFieldName(getFieldIndex(p_fieldName)));
   }
   /**
    * Return the fieldname is specificied by index.
    *
    * @param     index    the field index.
    * @return    the field name.
    * @throws    atoms.utils.table.EmptyTableException if not data.
    * @throws    atoms.utils.table.InvalidFieldIndexException if <code>index</code> is invalid fieldindex.
    * @throws   atoms.utils.table.InvalidInstanceException if the instance is not available.
    */
   public String getFieldName(int index) throws TableRecException {
	   /*
      if (isEmpty())
         throw new TableRecException(TableRecException.EMPTY_TABLE, getEmptyTableMessage());
         */
      if (fieldDef != null) {
         if (! fieldDef.existFieldIndex(index))
            throw new TableRecException(TableRecException.INVALID_FIELD_INDEX);
         String[] v = fieldDef.getFieldNames();
         return v[index];
      }
      else
         return null;
   }
   /**
    * Return the count of field of the table.
    *
    * @return    the count of field.
    * @throws    atoms.utils.table.EmptyTableException if not data.
    * @throws   atoms.utils.table.InvalidInstanceException if the instance is not available.
    */
   public int getFieldCount() throws TableRecException {
      if (fieldDef != null)
         return fieldDef.getFieldCount();
      else
         return columnCount;
   }
   /**
    * Test if the resultset is empty.
    *
    * @return    <code>true</code> if the resultset is not empty;
    *            <code>false</code> otherwise.
    * @throws   atoms.utils.table.InvalidInstanceException if the instance is not available.
    */
   public boolean isEmpty() throws TableRecException {
      return (data == null || data.isEmpty());
   }
   public String toString() {
      try {
         if (isEmpty())
            return "Empty recordset.";
         int cc = getFieldCount();
         StringBuffer sb = new StringBuffer();
         sb.append("TableName: " + getTableName()).append("\n");
         sb.append("RecordCount: " + getRecordCount()).append("\n");
         for (int i=0; i < getRecordCount(); i++) {
            setRecPointer(i);
            sb.append("Row " + i).append("\n");
            for (int j=0; j < cc; j++)
               sb.append(getField(j)).append("\t");
            sb.append("\n");
         }
         return sb.toString();
      }
      catch (Exception e) {
         e.printStackTrace();
         return e.toString();
      }
   }
   /**
    * Return the name of the table.
    *
    * @return    the name of the table.
    * @throws   atoms.utils.table.InvalidInstanceException if the instance is not available.
    */
   public String getTableName() throws TableRecException {
      return tableName;
   }
   /**
    * Return the record count of the resultset.
    *
    * @return    the record count.
    * @throws   atoms.utils.table.InvalidInstanceException if the instance is not available.
    */
   public int getRecordCount() {
      return data.size();
   }
   public int size() {
      return(getRecordCount());
   }
   private String getEmptyTableMessage() {
      return (tableName == null ? "" : "The table '" + tableName + "' is empty");
   }
   public String toXML() {
      return(toXML(0, getRecordCount()));
   }
   public String toXML(int p_at, TableRecToXmlCallable p_everyrow, Object p_userdata) {
      return(toXML(p_at, getRecordCount()-p_at, p_everyrow, p_userdata));
   }
   public String toXML(TableRecToXmlCallable p_everyrow, Object p_userdata) {
      return(toXML(0, getRecordCount(), p_everyrow, p_userdata));
   }
   public String toXML(int p_at) {
      return(toXML(p_at, getRecordCount()-p_at));
   }
   public String toXML(int p_at, int p_count) {
      return(toXML(p_at, p_count, (TableRecToXmlCallable) null, (Object) null));
   }
   public String toXML(int p_at, int p_count, TableRecToXmlCallable p_everyrow, Object p_userdata) {
      StringBuffer sb = new StringBuffer();
      if (getRecordCount() == 0) return(null);
      for (int i = p_at; i<(p_at+p_count); i++) {
         sb.append(toXMLOne(i, p_everyrow, p_userdata));
      }
      return(sb.toString());
   }
   public String toXMLOne(int p_at, TableRecToXmlCallable p_everyrow, Object p_userdata) {
      try {
         StringBuffer sb = new StringBuffer();
         String[] colnames;
         if (p_at < 0 || p_at >= getRecordCount()) return("");
         colnames = getFieldNames();
         if (colnames == null) return("");
         setRecPointer(p_at);
         sb.append("<tablerec tabname=\""+getTableName()+"\" recIdx=\""+p_at+"\">\n");
         for (int i=0; i<colnames.length; i++) {
            sb.append("   <column colname=\""+colnames[i]+"\">");
            if (getField(colnames[i]) instanceof java.sql.Date ||
                getField(colnames[i]) instanceof java.sql.Timestamp)
               sb.append(
                  getField(colnames[i]) == null ?
                     "" : StringUtil.convertWebString(getField(colnames[i]).toString().substring(0, 10))
               );
            else
               sb.append(
                  getField(colnames[i]) == null ?
                     "" : StringUtil.convertWebString(getField(colnames[i]).toString())
               );
            sb.append("</column>\n");
            if (h_format != null && getField(colnames[i]) != null) {
               TMFormat format = (TMFormat) h_format.get(colnames[i]);
               if (format != null) {
                  sb.append("   <formattedColumn colname=\""+colnames[i]+"\">")
                    .append(StringUtil.convertWebString(format.formatDisplay(getField(colnames[i]).toString())))
                    .append("</formattedColumn>\n")
                    ;
               }
            }
         }
         if (p_everyrow != null) {
            p_everyrow.everyrow(this, sb, p_userdata);
         }
         sb.append("</tablerec>\n");
         return(sb.toString());
      } catch (Exception e) {
         e.printStackTrace();
         return(null);
      }
   }
   public void setAutoIndex(String p_fdname) throws TableRecException {
      int idx = recPointer;
      for (int i=0; i<getRecordCount(); i++) {
         setRecPointer(i);
         setField(p_fdname, new Integer(i));
      }
      recPointer = idx;
   }
   public void setFieldAll(String p_fdname, Object p_object) throws TableRecException {
      int idx = recPointer;
      for (int i=0; i<getRecordCount(); i++) {
         setRecPointer(i);
         setField(p_fdname, p_object);
      }
      recPointer = idx;
   }
   public int searchRecord(String p_fdname, Object p_object) throws TableRecException {
      int idx = recPointer;
      int i;
      for (i=0; i<getRecordCount(); i++) {
         setRecPointer(i);
         if (p_object == getField(p_fdname)) 
            break;
         if (p_object == null || getField(p_fdname) == null)
            continue;
         if (p_object.equals(getField(p_fdname)))
            break;
      }
      if (i >= getRecordCount())
         i = -1;
      recPointer = idx;
      return(i);
   }
   public void dump() {
      UniLog.log("fieldDef = "+fieldDef);
   }
   public int getFieldIndex(String p_fieldName) { 
      return(fieldDef.getFieldIndex(p_fieldName));
   }
   public void setFormatHash(Hashtable p_hash) {
      h_format = p_hash;
   }
   public Hashtable getFormatHash() {
      return(h_format);
   }
   public int getColumnCount() {
      return(columnCount);
   }
   public int join(TableRec p_tr) throws Exception {
      if (p_tr == null || p_tr.getRecordCount() <= 0) {
         return(0);
      }
      if (p_tr.getColumnCount() != columnCount)
         throw(new Exception("Column count not match"));
      for (int i=0; i<p_tr.getRecordCount(); i++) {
         p_tr.setRecPointer(i);
         addRecord();
         for (int j=0; j<p_tr.getColumnCount(); j++) {
            setField(j, p_tr.getField(j));
         }
      }
      return(p_tr.getRecordCount());
   }
   public Vector getFieldVector(String key) throws TableRecException {
      String field = key.toLowerCase();
      if (!fieldDef.existField(field)) {
         throw new TableRecException(TableRecException.INVALID_FIELD_NAME, "The field '" + key + "' is not exist.");
      }
      int idx = fieldDef.getFieldIndex(key);
      return(getFieldVector(idx));
   }
   public Vector getFieldVector(int p_idx) throws TableRecException {
      Vector v = new Vector();
      for (int i=0; i<getRecordCount(); i++) {
         setRecPointer(i);
         v.addElement(getField(p_idx));
      }
      return(v);
   }
   public void setField(String key, int value) throws TableRecException{
      setField(key, new Integer(value));
   }
   public void setField(String key, double value) throws TableRecException{
      setField(key, new Double(value));
   }
   //andrew221223 handle long value. for fix long deckid bug
   public void setField(String key, long value) throws TableRecException{
      setField(key, new Long(value));
   }
   public void setFieldFromString(String p_key, String p_value) throws TableRecException {
      switch (getFieldType(p_key)) {
         case java.sql.Types.SMALLINT:
         case java.sql.Types.INTEGER :
	      case java.sql.Types.TINYINT :
	      case java.sql.Types.BIT :
	         if (p_value == null || p_value.trim().equals(""))
	            setField(p_key, 0);
            else
               setField(p_key, Integer.parseInt(p_value));
	         break;
	      case java.sql.Types.BOOLEAN:
	         setField(p_key, p_value != null &&
	            (p_value.equalsIgnoreCase("true") || p_value.equalsIgnoreCase("y") || p_value.equals("1")));
	         break;
         case java.sql.Types.REAL :
         case java.sql.Types.DOUBLE :
         case java.sql.Types.DECIMAL :
         case java.sql.Types.NUMERIC :
         case java.sql.Types.FLOAT :
            if (p_value == null || p_value.trim().equals(""))
               setField(p_key, (double) 0.0);
            else
               setField(p_key, Double.parseDouble(p_value));
            break;
         case java.sql.Types.LONGVARBINARY:
	      case java.sql.Types.VARBINARY:
	      case java.sql.Types.BINARY:
	      case java.sql.Types.BLOB:
            setField(p_key, p_value);
            break;
         case java.sql.Types.LONGVARCHAR :
         case java.sql.Types.VARCHAR:
         case java.sql.Types.CHAR :
            setField(p_key, p_value);
            break;
         case java.sql.Types.DATE :
            setField(p_key, DateUtil.getDate(p_value, dateFormat));
            break;
         case java.sql.Types.BIGINT :
            if (p_value == null || p_value.trim().equals(""))
               setField(p_key, 0);
            else
               setField(p_key, Integer.parseInt(p_value));
            break;
	      case java.sql.Types.TIMESTAMP :
	      case java.sql.Types.TIMESTAMP_WITH_TIMEZONE:
            setField(p_key, new java.sql.Timestamp(NumberUtil.parseLong(p_value)*1000));
            break;
         default: 
            UniLog.log(new Exception("key: "+p_key+" type not supported: "+getFieldType(p_key)+" value="+p_value));
            break;
      }
   }
   public TableRec duplicateSchema() {
      try {
         TableRec tr = new TableRec(tableName, getFieldNames(), getColtypes());
         tr.setFormatHash(h_format);
         return(tr);
      } catch (Exception ex) {
         UniLog.log(ex);
         return(null);
      }
   }
   public Hashtable toHash(int p_idx, Hashtable p_hash) throws TableRecException {
      String[] fieldNames = getFieldNames();
      setRecPointer(p_idx);
      for (int i=0; i<fieldNames.length; i++) {
         Object value = getField(fieldNames[i]);
         if (value instanceof String)
            p_hash.put(fieldNames[i], getField(fieldNames[i]).toString().trim());
         else
            p_hash.put(fieldNames[i], getField(fieldNames[i]));
      }
      return(p_hash);
   }
   public void setFieldFromHash(Hashtable p_hash) throws TableRecException {
      for (Enumeration en=p_hash.keys(); en.hasMoreElements(); ) {
         String fieldname = (String) en.nextElement();
         if (fieldDef.existField(fieldname)) {
            setField(fieldname, p_hash.get(fieldname));
         }
      }
   }
   public CellCollection toCellCollection(int p_idx) throws Exception {
      CellCollection cc = new CellCollection();
      return(toCellCollection(cc, p_idx));
   }
   public static CellCollection convertFieldValueForCellection(CellCollection p_cc, String p_colname, Object p_value, int p_fieldtype) throws Exception {
      if (p_value == null)
         UniLog.log(new Exception("field is "+p_colname+" is null"));
	   else {
		   switch (p_fieldtype) {
            case java.sql.Types.SMALLINT:
            case java.sql.Types.INTEGER :
            case java.sql.Types.BIGINT :
               if (p_value instanceof Integer) {
                  p_cc.putValue(p_colname, ((Integer) p_value).intValue());
                  p_cc.putValue(p_colname+"_formatted",
                                 new TMFormatNumber("#,###,###,##0")
                                     .formatDisplay(p_value.toString())
                  );
               }
               else if (p_value instanceof Long) {
                  long value = ((Long) p_value).longValue();
                  if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE) {
                     p_cc.putValue(p_colname, (int) value);
                  }
                  else {
UniLog.log("Warning: convertFieldValueForCellection(): cell "+p_colname+" is a Long and value "+value+" exceed integer value, converting to String");
                     p_cc.putValue(p_colname, ""+value);
                  }
               }
               else if (p_value instanceof BigDecimal) {
                  double value = ((BigDecimal) p_value).doubleValue();
                  if (value <= Double.MAX_VALUE && value >= Double.MIN_VALUE) {
                     p_cc.putValue(p_colname, (int) value);
                  }
                  else {
UniLog.log("Warning: convertFieldValueForCellection(): cell "+p_colname+" is a BigDecimal and value "+value+" exceed double value, converting to String");
                     p_cc.putValue(p_colname, ""+value);
                  }
					}
               else if (p_value instanceof java.math.BigInteger) {
                  long value = ((java.math.BigInteger) p_value).longValue();
                  if (value <= Long.MAX_VALUE && value >= Long.MIN_VALUE) {
                     p_cc.putValue(p_colname, (int) value);
                  }
                  else {
UniLog.log("Warning: convertFieldValueForCellection(): cell "+p_colname+" is a BigInteger and value "+value+" exceed integer value, converting to String");
                     p_cc.putValue(p_colname, ""+value);
                  }
					}
               else
                  throw(new Exception("invalid type "+p_colname+" ["+p_fieldtype+"] value class "+p_value.getClass().getName()));
               break;
            case java.sql.Types.REAL :
            case java.sql.Types.DOUBLE :
            case java.sql.Types.DECIMAL :
            case java.sql.Types.NUMERIC :
            case java.sql.Types.FLOAT :
               if (p_value instanceof Double) {
                  p_cc.putValue(p_colname, ((Double) p_value).doubleValue());
                  p_cc.putValue(p_colname+"_formatted",
                                 new TMFormatNumber("###,###,###,##0.00")
                                     .formatDisplay(p_value.toString())
                  );
               }
               else if (p_value instanceof Integer) {
                  p_cc.putValue(p_colname, ((Integer) p_value).intValue());
                  p_cc.putValue(p_colname+"_formatted",
                                 new TMFormatNumber("#,###,###,##0")
                                     .formatDisplay(p_value.toString())
                  );
               }
               else if (p_value instanceof Long) {
                  long value = ((Long) p_value).longValue();
                  if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE) {
                     p_cc.putValue(p_colname, (int) value);
                  }
                  else {
UniLog.log("Warning: convertFieldValueForCellection(): cell "+p_colname+" is a Long and value "+value+" exceed integer value, converting to String");
                     p_cc.putValue(p_colname, ""+value);
                  }
               }
               else if (p_value instanceof java.math.BigDecimal) {
                  double value = ((java.math.BigDecimal) p_value).doubleValue();
                  if (value <= Double.MAX_VALUE && value >= Double.MIN_VALUE) {
                     p_cc.putValue(p_colname, (int) value);
                  }
                  else {
UniLog.log("Warning: convertFieldValueForCellection(): cell "+p_colname+" is a Long and value "+value+" exceed integer value, converting to String");
                     p_cc.putValue(p_colname, ""+value);
                  }
               }
               else
                  throw(new Exception("invalid type "+p_colname+" ["+p_fieldtype+"] "+p_value.getClass().getName()));
               break;
            case java.sql.Types.LONGVARCHAR :
            case java.sql.Types.VARCHAR:
            case java.sql.Types.CHAR :
               p_cc.putValue(p_colname, StringUtil.sr(p_value.toString()));
               break;
            case java.sql.Types.DATE :
               if (p_value instanceof java.util.Date)
                  p_cc.putValue(p_colname, (java.util.Date) p_value);
               else if (p_value instanceof java.sql.Date)
                  p_cc.putValue(p_colname, new java.util.Date(((java.sql.Date) p_value).getTime()));
               else
                  throw(new Exception("invalid type "+p_colname+" ["+p_fieldtype+"]"));
               break;
	         case java.sql.Types.TIMESTAMP:
	         case java.sql.Types.TIMESTAMP_WITH_TIMEZONE:
               if (p_value instanceof java.util.Date)
                  p_cc.putValue(p_colname, (int) (((java.util.Date) p_value).getTime()/1000));
               else
                  throw(new Exception("invalid type "+p_colname+" ["+p_fieldtype+"]"));
               break;
            case java.sql.Types.TINYINT:
	         case java.sql.Types.BIT :
	         case java.sql.Types.BOOLEAN:
					if (p_value instanceof java.lang.Boolean) {
						if (((java.lang.Boolean) p_value).booleanValue()) {
               		p_cc.putValue(p_colname, 1);
						} else {
               		p_cc.putValue(p_colname, 0);
						}
					} else {
               	p_cc.putValue(p_colname, NumberUtil.parseInt(p_value.toString()));
					}
               break;
            default: 
               UniLog.log(new Exception("invalid type "+p_colname+" ["+p_fieldtype+"]"));
               p_cc.putValue(p_colname, p_value.toString());
               break;
			}
		}
	/*
      if (p_value == null)
         UniLog.log(new Exception("field is "+p_colname+" is null"));
      else if (p_value instanceof String) 
         p_cc.putValue(p_colname, StringUtil.sr(p_value.toString()));
      else if (p_value instanceof Integer) {
         p_cc.putValue(p_colname, ((Integer) p_value).intValue());
         p_cc.putValue(p_colname+"_formatted",
                        new TMFormatNumber("#,###,###,##0")
                            .formatDisplay(p_value.toString())
         );
		}
      else if (p_value instanceof Long) {
         long value = ((Long) p_value).longValue();
         if (value <= Integer.MAX_VALUE && value >= Integer.MIN_VALUE) {
            p_cc.putValue(p_colname, (int) value);
			}
		   else {
UniLog.log("Warning: convertFieldValueForCellection(): cell "+p_colname+" is a Long and value "+value+" exceed integer value, converting to String");
            p_cc.putValue(p_colname, ""+value);
			}
		}
      else if (p_value instanceof Double) {
         p_cc.putValue(p_colname, ((Double) p_value).doubleValue());
         p_cc.putValue(p_colname+"_formatted",
                        new TMFormatNumber("###,###,###,##0.00")
                            .formatDisplay(p_value.toString())
         );
      }
      else if (p_value instanceof java.util.Date)
         p_cc.putValue(p_colname, (java.util.Date) p_value);
      else if (p_value instanceof java.sql.Date)
         p_cc.putValue(p_colname, new java.util.Date(((java.sql.Date) p_value).getTime()));
       else if (p_value instanceof byte[]) 
          p_cc.putValue(p_colname, p_value);
      else
         p_cc.putValue(p_colname, p_value.toString());
*/
      return(p_cc);
   }
   public CellCollection toCellCollection(CellCollection p_cc, int p_idx) throws Exception {
      String[] colnames = getFieldNames();
      setRecPointer(p_idx);
      for (int j=0; j<colnames.length; j++) {
         convertFieldValueForCellection(p_cc, colnames[j], getField(j), getFieldType(j));
			/*
         if (getField(j) == null)
            UniLog.log(new Exception("field is "+colnames[j]+" is null"));
         else if (getField(j) instanceof String) 
            p_cc.putValue(colnames[j], StringUtil.sr(getField(j).toString()));
         else if (getField(j) instanceof Integer) 
            p_cc.putValue(colnames[j], ((Integer) getField(j)).intValue());
         else if (getField(j) instanceof Double) {
            p_cc.putValue(colnames[j], ((Double) getField(j)).doubleValue());
            p_cc.putValue(colnames[j]+"_formatted",
                           new TMFormatNumber("###,###,###,##0.00")
                               .formatDisplay(getField(j).toString())
            );
         }
         else if (getField(j) instanceof java.util.Date) 
            p_cc.putValue(colnames[j], (java.util.Date) getField(j));
         else if (getField(j) instanceof java.sql.Date) 
            p_cc.putValue(colnames[j], new java.util.Date(((java.sql.Date) getField(j)).getTime()));
          else if (getField(j) instanceof byte[]) 
             p_cc.putValue(colnames[j], getField(j));
         else
            p_cc.putValue(colnames[j], getField(j).toString());
		   */
      }
      return(p_cc);
   }
   public CellVector toCellVector() throws Exception {
      CellVector cv = new CellVector();
      for (int i=0; i<size(); i++) {
         CellCollection cc = toCellCollection(i);
         cv.addElement(cc);
      }
      return(cv);
   }
   public int[] getColumnDisplaySizes() throws TableRecException {
      if (fieldDef != null)
         return fieldDef.getColumnDisplaySizes();
      else
         return null;
   }
   public int getColumnDisplaySize(String key) throws TableRecException {
      String field = key.toLowerCase();
      if (! fieldDef.existField(field))
         throw new TableRecException(TableRecException.INVALID_FIELD_NAME, "The field '" + key + "' is not exist.");
      return(fieldDef.getColumnDisplaySize(fieldDef.getFieldIndex(field)));
   }
   public void stripSpace() throws Exception {
      int oIndex = recPointer;
      for (int i=0; i<getRecordCount(); i++) {
         setRecPointer(i);
         for (int j=0; j<getFieldCount(); j++) {
            switch (fieldDef.getFieldType(j)) {
               case java.sql.Types.LONGVARCHAR :
               case java.sql.Types.VARCHAR:
               case java.sql.Types.CHAR :
                  setField(j, StringUtil.sr(getField(j).toString()));
                  break;
            }
         }
      }
      if (oIndex >= 0)
         setRecPointer(oIndex);
   }
   public Object clone() {
      try {
         TableRec newTr = duplicateSchema();
         for (int i=0; i<size(); i++) {
            setRecPointer(i);
            newTr.addRecord();
            for (int j=0; j<getColumnCount(); j++)
               newTr.setField(j, getField(j));
         }
         return(newTr);
      } catch (Exception ex) {
         UniLog.log(ex);
         return(null);
      }
   }
   public void copyRow(int p_srcIdx, TableRec p_destTr) throws Exception {
      for (int j=0; j<getColumnCount(); j++)
         p_destTr.setField(j, getField(j, p_srcIdx));
   }
   public void setDateFormat(String p_format) {
      dateFormat = p_format;
   }
	public void setFieldsFromCellCollection(CellCollection p_cc) throws Exception {
      String[] colnames = getFieldNames();
		for (int i=0; i<colnames.length; i++) {
		   Cell cell = (p_cc == null) ? null : p_cc.testCell(colnames[i]);
		   if (cell != null)
			   setField(colnames[i], cell.getObject());
         else
			   setField(colnames[i], TableBrowser.getDefaultValue(getFieldType(i)));
		}
	}
	public void setUserData(Object p_obj) throws Exception {
	   setUserData(p_obj, recPointer);
	}
	public void setUserData(Object p_obj, int p_idx) throws Exception {
      userData.setElementAt(p_obj, p_idx);
	}
	public Object getUserData() throws Exception {
	   return(getUserData(recPointer));
	}
	public Object getUserData(int p_idx) throws Exception {
      return(userData.elementAt(p_idx));
	}
	public boolean isDifferenceFrom(TableRec p_tr1) {
		try {
		   for (int i=0; i<this.getFieldCount(); i++) {
			   if (!this.getField(i).toString().equals(p_tr1.getField(i).toString())) {
				   String fieldName = this.getFieldName(i);
					if (fieldName.endsWith("_udate")
					    || fieldName.endsWith("_utime") 
						 || fieldName.endsWith("_uuser"))
				      continue;
			      if (this.getField(i) instanceof java.util.Date) {
						if (((java.util.Date) this.getField(i)).getTime() == ((java.util.Date) p_tr1.getField(i)).getTime())
						   continue;
					}
				   return(true);
				}
			}
			return(false);
		} catch (Exception ex) {
			UniLog.logClass(this, "Exception ignored");
			UniLog.log(ex);
			return(true);
		}
	}
	public Hashtable getDifferentColnames(TableRec p_tr1) throws Exception {
	   Hashtable h = new Hashtable();
		for (int i=0; i<this.getFieldCount(); i++) {
			if (!this.getField(i).toString().equals(p_tr1.getField(i).toString())) {
				String fieldName = this.getFieldName(i);
				if (fieldName.endsWith("_udate")
					 || fieldName.endsWith("_utime") 
					 || fieldName.endsWith("_uuser"))
				   continue;
			   if (this.getField(i) instanceof java.util.Date) {
					if (((java.util.Date) this.getField(i)).getTime() == ((java.util.Date) p_tr1.getField(i)).getTime())
						continue;
				}
			   h.put(fieldName, fieldName);
			}
		}
		return(h);
	}
	
	public Vector <Object[]> getAllData()
	{
		return(data);
	}
	
	public Object[] getRecord(int p_idx) {
		if(p_idx < 0 || p_idx >= data.size()) return(null);
		return(data.get(p_idx));
	}
	public boolean existField(String p_field) {
		if(fieldDef == null) return(false);
		else return(fieldDef.existField(p_field));
	}
}
