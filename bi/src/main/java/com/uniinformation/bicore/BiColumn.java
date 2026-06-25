package com.uniinformation.bicore;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.*;
import com.uniinformation.utils.*;
import com.uniinformation.webcore.SessionHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import com.uniinformation.cell.*;

/***
 *  child element of view
 *
 */
public class BiColumn extends BiBase implements BiGipiPickViewInterface {
	static public final String DEFAULT_ATTRIBUTE="NNNNNNNNNNNY";
	@Expose
	String label;
//	Cell value;
	@Expose
	String header;
	@Expose
	String formula;
	@Expose
	private boolean inList;
	@Expose
	private boolean inSelect;
	@Expose
	private boolean noUpdate=false;
	@Expose
	private boolean noEntry=false;
	@Expose
	private boolean noQuery = false;
	@Expose
	private boolean noSpace = false;
	@Expose
	private boolean upShift = false;
	@Expose
	private boolean required= false;
	@Expose
	private boolean flexWidth=false;
	@Expose
	private boolean excludeForMobile=false;
	@Expose
	private boolean lookup=false;
	@Expose
	private boolean invisible = false;
	private boolean storedFunction = false;
	@Expose
	private boolean protect = false;
	@Expose
	private boolean allowBatchUpdate = false;
	@Expose
	private boolean inPickList = false;
//	private boolean isAutoIncrement = false;
	@Expose
	private boolean formulaOnLoad = false;
	@Expose
	private boolean formulaOnSave = false;
	@Expose
	private boolean skipImport = false;
	@Expose
	private boolean skipExport = false;
	@Expose
	private boolean autoTranslate = false;
	@Expose
	private boolean isPivot = false;
	@Expose
	private boolean isAggregate = false;
	private int rgno;
	@Expose
	private String fdtype;
	@Expose
	private String accesskey;
	@Expose
	private String addupdatekey;
	@Expose
	private String defaultValue;
	@Expose
	private boolean readOnly=false;
	@Expose
	private String fdOptionList[] = null;
	@Expose
	int fdlen;
	@Expose
	int fdheight=0;
	@Expose
	String fdformat;
	@Expose
	String pickView;
	@Expose 
	int fgColor;
	@Expose 
	int bgColor;
	@Expose
	private boolean useS2Listbox=false;
	@Expose
	String validation;
	@Expose
	private String aggregate;
	@Expose
	boolean selfPick = false;
	@Expose
	boolean isChinese = false;
	@Expose
	boolean isAutoSave = false;
	@Expose
	boolean autoAddUpdate= false;
	boolean isUTF8 = false;
	private boolean isAggregateExpression = false;
	@Expose
	private String timeCompEndTime;
	@Expose
	private boolean timeCompIsShortFmt;
	@Expose
	private int timeCompStepMin;
	@Expose
	private boolean noPaste = false; //for control copy and paste
	@Expose
	private String fdOptionSelect = null;
	@Expose
	private String fdOptionCondition = null;
	@Expose
	private boolean allowPickOldInput=false;
	
	transient HashSet<Comparable> selfPickList = null;
	transient boolean allowUndo = false;
	transient String cellFormatStr = null;
	
	String attribute;
	public BiColumn(/* CellCollection */ BiBase parent,String p_label,BiField p_field,String p_header,boolean p_inlist,boolean p_inselect,String p_formula,String p_attribute,String p_format,boolean p_noquery,boolean p_invisible,int p_rgno,String p_fdtype,int p_fdlen,String p_accesskey,String p_optionlist,String p_default,String p_pickView,int p_fgcolor,int p_bgcolor,String p_validation,String p_aggregate)
	{
		
		/*
		 *  The effect of isChinese, isUTF8 and schema.autoTranslate() flag 
		 *  
		 *  This flags is used to properly handle the Byte Encoding and Tranditional/Simplified Chinese Character issue.
		 *  1) if schema.autoTranslate() is set, (set by ddd_database.per), all chinese characters in java string are converted into Tranditional Chinese before storing into database, and convert to Simplified Chinese for display
		 *     if user profile is set to simplfied chinese. This flag is specially used by scorpion database becourse scorpion is not unicode. For system that will only use chinese and english,
		 *     It is recommend that this flag should be set. If unset, it MAY be able to store and display both Simplified/Tranditional Character at the same time, but not garenteed, becaure some
		 *     simplified character does not have a valid BIG5 encoding.
		 *     For Unicode SQL Engine (E.g. mysql) this flag may or may not be set depends whether use needs auto-translate between Simplied and Traditional chinese by default. 
		 *     
		 *  2) The column attribute strpart(p_attribute, 18, 1) previouse call is "Auto Translate" is now rename to "Is Chinese". When flaged, auto-translate between Simplied/Tranditional Chinese
		 *     will be performed no-mattter which SQL Engined is used and schema.autoTranslate() is flagged or not.
		 *     
		 *     if this flag is set and schema.autoTranslate is true, the isChinese is set 
		 *       and if the SQL Engine is Scorption and schema.autoTranslate() is true, the default field length will be set to database's field length / 2, because it requireds 2 byte to store one chinese character.
		 *     the method isChinese() will return this flag , indicates that is column is expected to enter chinese character and the default px per char can be adjust accordingly
		 *      
		 *  3) The column attribute strpart(p_attribute, 22, 1) i.e. isUTF8 is only used by Scorpion Database , that will store the String as UTF-8 in database. UTF8 field can store any unicode 
		 *     character, but the default field length will be set to (database field length - 2) / 3 as 2 bytes prefix and 3 byte per character is required to store the string in utf8.
		 *     For Unicode SQL Engine , this flag is ignored.
		 *     **** currently isUTF8 column cannot be use in query conditions.
		 *          
		 */
		super(parent);
		attribute = p_attribute;
		addCollection("field",p_field);
		label = p_label;
		header = p_header;
		CellVector v = new CellVector();
		if(p_field != null) v.add(p_field.getTable());
		addCollectionList("tables",v);
		inList = p_inlist;
//		inSelect = p_inselect | p_inlist;
		inSelect = p_inselect;
		formula = p_formula;
		noQuery = p_noquery;
		invisible = p_invisible;
		rgno = p_rgno;
		
		fdtype = p_fdtype;
		fdlen = p_fdlen;
//		if(p_accesskey != null && !p_accesskey.trim().equals("")) accesskey = p_accesskey;
		if(p_accesskey != null && !p_accesskey.trim().equals("")) {
			String keys[] = p_accesskey.split(",");
			if(keys.length > 0) {
				if(!keys[0].trim().equals("")) accesskey = keys[0].trim();
			}
			if(keys.length > 1) {
				if(!keys[1].trim().equals("")) addupdatekey = keys[1].trim();
			}
		}
		
		if(fdtype == null || fdtype.trim().equals("")) {
			if(p_field != null) fdtype = p_field.getFieldType(); else fdtype = "float";
		}
		fgColor = p_fgcolor;
		bgColor = p_bgcolor;
		if(getSchema() != null && getSchema().getSqlEngine() == BiSchema.SQLENGINE_SCORPION && StringUtil.strpart(p_attribute, 22, 1).equals("Y")) isUTF8 = true;
		if(!isUTF8) {
//		if(StringUtil.strpart(p_attribute, 18, 1).equals("Y")) isChinese = true;
		boolean ischn = StringUtil.strpart(p_attribute, 18, 1).equals("Y");
		if(ischn && parent != null && parent.getSchema().getSchemaAutotranslate()) {
			isChinese = true;
		}
		if(p_field != null && p_field.getFieldType().equals("char") &&  !fdtype.equals("checkbox")) {
			
			if(parent != null && parent.getSchema().getSchemaAutotranslate() || ischn) {
				autoTranslate=true;
			}
		}
		}
		if(fdlen <= 0) {
			/*
			 * should properly handle long (64bit integer) and varchar 
			 */
			if(fdtype.equals("integer")) fdlen = 10; 
			else if(fdtype.equals("serial")) fdlen = 10;
			else if(fdtype.equals("date")) fdlen = 10;
			else if(fdtype.equals("money")) fdlen = 14;
			else if(fdtype.equals("float")) fdlen = 14;
			else if(fdtype.equals("double")) fdlen = 14;
			else if(fdtype.equals("button")) fdlen = 10;
			else {
				if(p_field != null) {
					if(getSchema() != null && getSchema().getSqlEngine() == BiSchema.SQLENGINE_SCORPION) {
						if(isUTF8) {
							fdlen = (p_field.getFieldLength() - 2 ) / 3;
							
						} /* else if(isChinese) {
							fdlen = p_field.getFieldLength() / 2;
						} */ else  {
							fdlen = p_field.getFieldLength();
						}
					} else {
						fdlen = p_field.getFieldLength();
					}
				} else {
					fdlen = 20;
				}	
			}
		}
		
		if(p_format != null && !p_format.trim().equals("")) fdformat = p_format.trim();
		if(p_default != null && !p_default.trim().equals("")) defaultValue = p_default.trim();
		if(p_pickView != null && !p_pickView.trim().equals("")) pickView = p_pickView.trim();
		
		fdOptionList = null;
		fdOptionSelect = null;
		if (p_optionlist != null && !p_optionlist.trim().equals("")){
			if(p_optionlist.startsWith("\\")) {
				int idx = p_optionlist.substring(1).indexOf("\\");
				if(idx >= 0) {
					fdOptionSelect = p_optionlist.substring(1,idx+1);
					fdOptionCondition = p_optionlist.substring(idx+2);
				} else {
					fdOptionSelect = p_optionlist.substring(1);
				}
			} else {
				fdOptionList = p_optionlist.trim().split(",");
			}
			if(StringUtils.isBlank(fdOptionSelect)) fdOptionSelect = null;
			if(fdOptionList != null && fdOptionList.length == 0) fdOptionList = null;
		}
		
		if(StringUtil.strpart(p_attribute, 0, 1).equals("Y")) noUpdate = true;
		if(StringUtil.strpart(p_attribute, 1, 1).equals("Y")) noEntry = true;
		if(StringUtil.strpart(p_attribute, 2, 1).equals("Y")) upShift = true;
		if(StringUtil.strpart(p_attribute, 3, 1).equals("Y")) noSpace = true;
		if(StringUtil.strpart(p_attribute, 4, 1).equals("Y")) required = true;
		if(StringUtil.strpart(p_attribute, 5, 1).equals("Y")) storedFunction = true;
		if(StringUtil.strpart(p_attribute, 6, 1).equals("Y")) flexWidth = true;
		if(StringUtil.strpart(p_attribute, 7, 1).equals("Y")) excludeForMobile = true;
		if(StringUtil.strpart(p_attribute, 8, 1).equals("Y")) lookup = true;
		if(StringUtil.strpart(p_attribute, 9, 1).equals("Y")) protect = true;
		if(StringUtil.strpart(p_attribute, 10, 1).equals("Y")) {
			if(parent instanceof BiView) ((BiView) parent).setSeqColumn(p_label);
		}
		if(StringUtil.strpart(p_attribute, 11, 1).equals("Y")) allowBatchUpdate = true;
		if(StringUtil.strpart(p_attribute, 12, 1).equals("Y")) readOnly = true;
		if(StringUtil.strpart(p_attribute, 13, 1).equals("Y")) inPickList = true;
		if(StringUtil.strpart(p_attribute, 14, 1).equals("Y")) formulaOnLoad = true;
		if(StringUtil.strpart(p_attribute, 15, 1).equals("Y")) formulaOnSave = true;
		if(StringUtil.strpart(p_attribute, 16, 1).equals("Y")) skipImport = true;
		if(StringUtil.strpart(p_attribute, 17, 1).equals("Y")) skipExport = true;
		if(StringUtil.strpart(p_attribute, 19, 1).equals("Y")) isPivot = true;
		if(StringUtil.strpart(p_attribute, 20, 1).equals("Y")) isAggregate = true;
		if(StringUtil.strpart(p_attribute, 21, 1).equals("Y")) noPaste = true;
//		if(StringUtil.strpart(p_attribute, 21, 1).equals("Y")) isAutoIncrement= true;
		if(getSchema().defaultAllowPickOldInput) {
			allowPickOldInput = true;
		}
		/*
		if(p_field != null) {
			String ft = p_field.getFieldType();
			if(ft.equals("integer")) parent.addCell(p_field.getTable().getName()+"."+ p_field.getName(), new Cell(0));
				else if(ft.equals("date")) parent.addCell(p_field.getTable().getName()+"."+ p_field.getName(), new Cell(0));
					else if(ft.equals("float")) parent.addCell(p_field.getTable().getName()+"."+ p_field.getName(), new Cell(0));
						else parent.addCell(p_field.getTable().getName()+"."+ p_field.getName(), new Cell(0));
		} else {
			parent.addCell(.getName(), new Cell(0));
		}
		*/
		if(p_validation != null && !p_validation.trim().equals("")) {
			validation = p_validation;
		}
		if(p_aggregate != null && !p_aggregate.trim().equals("")) {
			aggregate = p_aggregate;
			if(AggregateOrPivot.AGGREGATES.valueOf(aggregate) == AggregateOrPivot.AGGREGATES.EXPRESSION
			   || AggregateOrPivot.AGGREGATES.valueOf(aggregate) == AggregateOrPivot.AGGREGATES.EXPRESSION2) {
				isAggregateExpression = true;
			}
		}
		if(parent != null) {
			if(parent instanceof BiView) {
				allowUndo = ((BiView) parent).allowUndo();
			} else {
				allowUndo = false;
			}
		} else allowUndo = false;
	}
	public String getLabel()
	{
		return(label);
	}
	public String getCellLabel()
	{
		return(getLabel());
	}
	public BiField getField()
	{
		return((BiField) getCollection("field"));
	}
	public String getEngName()
	{
//		return(((BiField) getCollection("field")).getEName());
		return(header);
	}
	public String getChnName()
	{
//		return(((BiField) getCollection("field")).getCName());
		return(header);
	}
	public String getColumnType() {
		return getColumnType(false);
	}
	public String getColumnType(boolean checkLabelType)
	{
		if (checkLabelType && StringUtils.equals(fdtype, "label") && getField() != null)
			return getField().getFieldType();
		else
			return(fdtype);
	}
	public int getColumnLength()
	{
		return(fdlen);
	}
	public int getColumnHeight()
	{
		return(fdheight);
	}
	/*
	public void setTableIdx(int p_idx)
	{
		tableIdx = new Cell(p_idx);
	}
	
	public int getTableIdx()
	{
		return(tableIdx.getInt());
	}
	*/
	
	/*
	public String getFormatedValue(BiTableRec p_bitablerec)
	{
		BiField fd = (BiField) getCollection("field");
		return(p_bitablerec.getFieldString(fd));
	}
	*/
	
	public Vector getTableDepends() {
		return(getCollectionList("tables"));
	}
	
	//return the first table
	public BiTable getTableDepend(){
		Vector tables = getTableDepends();
		if (tables != null && tables.size() >= 1){
			return((BiTable)tables.elementAt(0));
		}
		return(null);
	}
	
	
	public String getSelectName() {
		BiField fd = (BiField) getCollection("field");
		if(fd != null) {
			return(fd.getTable().getName()+"."+fd.getName());
		} else {
			if(storedFunction)  {
				if(pickView != null) {
					if(fdtype.equals("integer")) return("0");
					if(fdtype.equals("serial")) return("0");
					if(fdtype.equals("date")) return("0");
					if(fdtype.equals("money")) return("0.0");
					if(fdtype.equals("float")) return("0.0");
					if(fdtype.equals("double")) return("0.0");
					return("''");
				} else {
					return(formula + " " + label); 
				}
			} else return(null);
		}
	}
	
	public String toString()
	{
		return(label);
	}
	
	public boolean isGroupByField(SessionHelper p_sh)
	{
		return(inList || inSelect);
	}
	public boolean isInList(SessionHelper p_sh)
	{
		return(
				(inList && (accesskey == null || accesskey.trim().equals("") || BiSchema.hasAccessRight(p_sh, accesskey))) ||
				((inSelect || inList) && p_sh.getSchemaDebugFlag())
				);
	}
	public boolean isInSelect()
	{
		return(inSelect || inList);
	}

	public boolean isNoUpdate(SessionHelper p_sh)
	{
//		return(noUpdate);
		return((  noUpdate || ( addupdatekey != null && p_sh != null && !BiSchema.hasAccessRight(p_sh, addupdatekey) && !p_sh.getSchemaDebugFlag())  ));
	}
	
	public boolean isNoQuery()
	{
		return(noQuery);
	}
	public boolean isInvisible(SessionHelper p_sh)
	{
//		return((invisible || (accesskey != null && p_sh != null && !BiSchema.hasAccessRight(p_sh, accesskey))) && !p_sh.getSchemaDebugFlag());
		return((  invisible || ( accesskey != null && p_sh != null && !BiSchema.hasAccessRight(p_sh, accesskey) && !p_sh.getSchemaDebugFlag())  ));
	}
	
	public boolean isNoEntry(SessionHelper p_sh)
	{
		return((  noEntry || ( addupdatekey != null && p_sh != null && !BiSchema.hasAccessRight(p_sh, addupdatekey) && !p_sh.getSchemaDebugFlag())  ));
//		return(noEntry);
	}
	
	public boolean displayOnly() {
		return(noEntry && noUpdate && readOnly);
	}
	
	public boolean isNoSpace()
	{
		return(noSpace);
	}
	boolean isRequired()
	{
		return(required);
	}
	public boolean isUpShift()
	{
		return(upShift);
	}
	public int getRgNo()
	{
		return(rgno);
	}
	public boolean isStoredFunction()
	{
		return(storedFunction);
	}
	public boolean isFlexWidth()
	{
		return(flexWidth);
	}

	
	public String getDecoration()
	{
		return(fdformat);
	}

	public String getFormat()
	{
		if(cellFormatStr == null) {
			cellFormatStr = BiUtil.extractColDecorationValue(fdformat, "format");
			if(cellFormatStr == null) cellFormatStr = "";
		}
		if(fdtype.equals("date") && fdformat == null) {
			return(getSchema().defaultDateFormat);
		}
		return(StringUtils.isBlank(cellFormatStr) ? null : cellFormatStr);
	}
//	public Comparable[] getOptionList(SessionHelper p_sh){
	
//	public String[] getOptionList(SessionHelper p_sh){
//		if (fdOptionList == null || fdOptionList.length == 0){
//			if(p_sh == null || fdOptionSelect == null || fdOptionSelect.length() == 0) {
//				return(null);
//			}
//			SelectUtil su =  null;
	
//			try {
//				su =  p_sh.getBiSchema().getSelectUtil();
//				TableRec tr = su.getQueryResult(fdOptionSelect);
//				String[] ol = new String[tr.getRecordCount()];
//				boolean isSchn = p_sh.getLHLang().equals("SCHN");
//				for(int i=0;i<tr.getRecordCount();i++) {
//					tr.setRecPointer(i);
//					String o = tr.getField(0).toString();
//					if(isSchn) {
//						ol[i] = ChineseConvert.convertAuto2Gnew(o);
//					} else {
//						ol[i] = o;
//					}
//				}
//				return(ol);
//			} catch (Exception ex) {
//				UniLog.log(ex);
//			} finally {
//				if(su != null) {
//					su.close();
//				}
//			}
//			
//		}
//		if(p_sh != null && p_sh.getLHLang().equals("SCHN")) {
//			if(getSchema().getSchemaAutotranslate() || autoTranslate) {
//				String[] schnList = new String[fdOptionList.length];
//				for(int i=0;i<fdOptionList.length;i++) {
//					schnList[i] = ChineseConvert.convertAuto2Gnew(fdOptionList[i]);
//				}
//				return(schnList);
//			}
//		}
//		return(fdOptionList);
//	}	

	public String[] getOptionList(BiResult p_br,BiCellCollection p_col){
		if(fdOptionList != null) return(fdOptionList);
		if(fdOptionSelect == null) return(null);
		Wherecl wherecl = null;
		SelectUtil su =  p_br.getSelectUtil();
		boolean isSchn = false;
		if(p_br.getSessionHelper() != null) isSchn = p_br.getSessionHelper().getLHLang().equals("SCHN");
		if(p_col != null && fdOptionCondition != null) {
			wherecl = new Wherecl().appendString(p_br.makeOptionSelectCondition(p_col,fdOptionCondition));
		}
		String[] ol = getOptionList(su,isSchn,wherecl);
		return(ol);
	}
	public String[] getOptionList(SessionHelper p_sh){
		if(fdOptionList != null) return(fdOptionList);
		if(fdOptionSelect == null) return(null);
			if(fdOptionList == null && fdOptionSelect == null) return(null);
			SelectUtil su =  null;
			boolean isSchn = false;
			try {
				if(p_sh != null) {
					su =  p_sh.getBiSchema().getSelectUtil();
					isSchn = p_sh.getLHLang().equals("SCHN");
				}
				String[] ol = getOptionList(su,isSchn,null);
				return(ol);
			} catch (Exception ex) {
				UniLog.log(ex);
				return(null);
			} finally {
				if(su != null) {
					su.close();
				}
			}
	}	
	
	private String[] getOptionList(SelectUtil su,boolean isSchn,Wherecl p_wherecl){
		if (fdOptionList == null || fdOptionList.length == 0){
			if(su == null || fdOptionSelect == null || fdOptionSelect.length() == 0) {
				return(null);
			}
			try {
				TableRec tr;
				if(p_wherecl == null) tr = su.getQueryResult(fdOptionSelect); else tr = su.getQueryResult(fdOptionSelect,p_wherecl);
				String[] ol = new String[tr.getRecordCount()];
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					String o = tr.getField(0).toString();
					if(isSchn) {
						ol[i] = ChineseConvert.convertAuto2Gnew(o);
					} else {
						ol[i] = o;
					}
				}
				return(ol);
			} catch (Exception ex) {
				UniLog.log(ex);
			} finally {
				if(su != null) {
					su.close();
				}
			}
			
		}
		if(su != null && isSchn) {
			if(getSchema().getSchemaAutotranslate() || autoTranslate) {
				String[] schnList = new String[fdOptionList.length];
				for(int i=0;i<fdOptionList.length;i++) {
					schnList[i] = ChineseConvert.convertAuto2Gnew(fdOptionList[i]);
				}
				return(schnList);
			}
		}
		return(fdOptionList);
	}
	
	public String getFormula(boolean p_skipAggregate) {
		if(p_skipAggregate) {
			if(isAggregateExpression) {
				return(null);
			}
		}
		if(!storedFunction) return(formula); else return(null);
	}
	
	public String getSummaryAggregate() {
		if(storedFunction && pickView != null) return(formula); else return(null);
	}
	
	/*
	public boolean isLookupColumn() {
		BiView bv = (BiView) getParent();
		BiField bf = getField();
		if(bf != null && bf.getTable() != bv.getTable()) {
			return(true);
		} else {
			return(false);
		}
	}
	*/
	
	/*
	public String getAccessKey() {
		return(accesskey);
	}
	*/
	public boolean isExcludeForMobile()
	{
		return(excludeForMobile);
	}
	public boolean isLookup(){
		return(lookup);
	}
	public BiTable getTable(){
		if (getField() == null){
			return(null);
		}
		return(getField().getTable());
	}
	
	public boolean isProtected() {
		return(protect);
	}
	public int getZkColIdx(){  //TODO: obtain colIdx from bi
		int colIdx = new Random().nextInt(2); //TODO: define it in bi 
		return(colIdx);
	}
	
	public BiView getView() {
		BiBase bb = (BiBase) getParent();
		if(bb instanceof BiView) return((BiView) bb);
		return(null);
	}
	public String getCellFullName(){
		return((getView() == null ? "temp" : getView().getName()) + "." + getCellLabel());
	}
	public boolean allowBatchUpdate() {
		return(allowBatchUpdate);
	}
	public boolean isNumber(){
		if(fdtype.equals("label")) {
			if(getField() == null) return(false);
			return(getField().isNumber());
		} else {
//			return StringUtils.equalsAny(fdtype, "integer","serial","float","double","money","decimal");
			return(fdtype.matches("integer|serial|float|double|money|decimal"));
		}
	}
	public boolean isReadOnly() {
		return(readOnly);
	}
	public boolean inPickList() {
		return(inPickList);
	}
	public boolean isformulaOnLoadOrSave() {
		return(formulaOnLoad || formulaOnSave);
	}
	public boolean isformulaOnLoad() {
		return(formulaOnLoad);
	}
	public boolean isformulaOnSave() {
		return(formulaOnSave);
	}
	public String getDefaultValue() {
		return(defaultValue);
	}
	Map <String,Object> toDataMap(){
		return MapUtil.of(
				"label",label,
				"header",header,
				"formula",formula,
				"inList",inList,
				"inSelect",inSelect,
				"noUpdate",noUpdate);
	}
	private <T> T getVar(String p_varName){
		try {
			return (T) this.getClass().getDeclaredField(p_varName);
		}
		catch(NoSuchFieldException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	private void setVar(String p_varName, Object p_val){
		if (StringUtils.isBlank(p_varName)) {
			return;
		}
		try {
			Field field = this.getClass().getDeclaredField(p_varName);
			Class type = field.getType();
			if (BiSchema.debugFlag.get()) UniLog.log1("%s Boolean:%s boolean:%s String:%s int:%s Integer:%s",
					p_varName,
					field.getType().equals(Boolean.class),
					field.getType().equals(boolean.class),
					field.getType().equals(String.class),
					field.getType().equals(int.class),
					field.getType().equals(Integer.class)
					);
			if (p_val instanceof String) {
				if (type.equals(Boolean.class) || type.equals(boolean.class)) {
					field.set(this, Boolean.parseBoolean((String)p_val));
					return;
				}
				else if (type.equals(String.class)) {
					field.set(this, p_val);
					return;
				}
				else if (type.equals(Integer.class) || type.equals(int.class)) {
					field.set(this, Integer.parseInt((String)p_val));
					return;
				}
			}
			if (p_val instanceof List) {  //assume List<String>
				field.set(this,((List) p_val).toArray(new String[((List) p_val).size()]));
			}
			UniLog.log1("field type not supported: %s", type);
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	public void update(JsonObject p_json) {
		if (p_json == null) {
			UniLog.log1("json is null");
			return;
		}
		for (String key : p_json.keySet()) {  //every col
			if (p_json.get(key).isJsonPrimitive()) {
				setVar(key,p_json.get(key).getAsString());
				//UniLog.log1("JSON1:%s:%s",key,p_json.get(key).getAsString());
			}
			else if (p_json.get(key).isJsonArray()) {
				//UniLog.log1("JSON2:%s:%s",key,p_json.get(key).getAsJsonArray().toString());
				boolean skipBlank = true;
				if(key.equals("fdOptionList")) {
//					if(getField() != null && getField().getFieldType().equals("integer")) {
					if(fdtype.equals("radio")
						|| fdtype.equals("list")) {
						skipBlank = false;
					}
				}
				ArrayList<String> aList = new ArrayList<String>();
				for (JsonElement je : p_json.get(key).getAsJsonArray()) {
					//UniLog.log1("pri:%s arr:%s obj:%s str:%s", je.isJsonPrimitive(), je.isJsonArray(), je.isJsonObject(), je.getAsString());
					if (!je.isJsonPrimitive() || (skipBlank && StringUtils.isBlank(je.getAsString()))) {
						continue;
					}
					aList.add(je.getAsString());
				}
				setVar(key,aList);
			}
			else {
				UniLog.log1("key %s not supported", key);
			}
		}
		
		
	}
	
	public String getPickViewName() {
		if(!StringUtils.isBlank(pickView)) {
			int cc = pickView.indexOf('@');
			if( cc >= 0) {
				return(pickView.substring(cc+1));
			} else return(pickView);
		}
		return(null);
	}
	public String getPickColName() {
		if(pickView != null) {
			int cc = pickView.indexOf('@');
			if( cc >= 0) {
				return(pickView.substring(0,cc));
			} else return(null);
		}
		return(null);
	}
	public String getPickCondition(ColumnCell p_cc) {
		return(p_cc.getBiResult().getPickColumnCondition(p_cc));
	}
	public boolean isSkipImport() {
		return(skipImport);
	}
	public boolean isSkipExport() {
		return(skipExport);
	}
	public boolean isAutoTranslate() {
		return(autoTranslate);
	}
	public boolean isPivot() {
		return(isPivot && StringUtils.isBlank(aggregate));
	}
	public boolean isAggregate() {
		return(isAggregate || (aggregate != null && !aggregate.isEmpty()));
	}
	public int getFgColor() {
		return (fgColor);
	}
	public int getBgColor() {
		return (bgColor);
	}
//	public boolean isAutoIncrement() {
//		return (isAutoIncrement);
//	}
	public int getAlignment() {
		int align = 0;
		String alignStr = BiUtil.extractColDecorationValue(fdformat, "align");
		if(!StringUtils.isBlank(alignStr)) align = NumberUtils.toInt(alignStr);
//		if (getColumnType().trim().matches("float|money|serial|integer|decimal")) {
		if (align != 0)
			return align;
		if (isNumber()) {
			return(-1);
		} else if (getColumnType().trim().matches("date|checkbox|button|radio|time|datetime")){
			return(0);
		} else if(getColumnType().trim().matches("label|memo") || (getColumnType().trim().matches("char") && isFlexWidth())) {
			return(2);
		} else{
			return(1);
		}
	}
	
	public boolean isUTF8() {
		return(isUTF8);
	}
	
	public boolean useS2Listbox() {
		return(useS2Listbox);
	}
	
	public String getTimeCompEndTime() {
		return timeCompEndTime;
	}

	public boolean getTimeCompIsShortFmt() {
		return timeCompIsShortFmt;
	}
	
	public int getTimeCompStepMin() {
		return timeCompStepMin;
	}
	
	public String getValidation() {
		return(validation);
	}
	public String getAggregate() {
		if(aggregate == null || aggregate.isEmpty()) return(null);
		return(aggregate);
	}
	public String getAggregateExpression() {
		if(!isAggregateExpression) return(null);
		return(formula);
	}
	
	public boolean isChinese() {
		return(isChinese);
	}
	
	public boolean isSelfPick() {
		return(selfPick);
	}
	
	public HashSet<Comparable> getSelPickList(BiResult br) throws Exception {
		if(selfPickList == null) {
			if(getField() != null && getField().getTable() == getView().getTable() && getField().getFieldType().equals("char")) {
				selfPickList = new HashSet<Comparable>();
				SelectUtil su = br.getSelectUtil();
				TableRec tr = su.getQueryResult("select distinct " + getField().getName() + " from " + getField().getTable().getDbtName(),null);
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					selfPickList.add(tr.getFieldString(getField().getName()));
				}
			}
		}
		return(selfPickList);
	}
	
	public void addToSelfPickList(Comparable cc) {
		if(selfPickList != null) {
			selfPickList.add(cc);
		}
	}
	
	public boolean isAutoSave() {
			return(isAutoSave);
	}
	public boolean isAutoAddUpdate() {
			return(pickView != null && autoAddUpdate);
	}
	/***
	 * for control copy and paste
	 * true: skip the paste action
	 * @return
	 */
	public boolean isNoPaste() {
		return noPaste;
	}
	BiColumn cloneBiColumn(BiView parent) {
		String strOptionList = null;
		if(fdOptionList != null) {
		for(String option :fdOptionList) {
			if(strOptionList == null) strOptionList = option; else strOptionList += "," + option;
		}
		} 
		if(fdOptionSelect != null) {
			strOptionList = "\\"+fdOptionSelect;
		}
		BiColumn nbc = new BiColumn(parent,label,getField(),header,inList,inSelect,formula,attribute,fdformat,noQuery,invisible,rgno,fdtype,fdlen,accesskey,strOptionList,defaultValue,pickView,fgColor,bgColor,validation, aggregate);
		nbc.useS2Listbox = useS2Listbox;
		nbc.allowPickOldInput = allowPickOldInput;
		return(nbc);
	}
//	public boolean setFormula(String p_formula) {
//		if(isAggregateExpression) {
//			return(false);
//		}
//		if(!storedFunction) {
//			formula = p_formula;
//			return(true); 
//		} else return(false);
//	}

	public boolean isPivotFlagOn() {
		return(isPivot);
	}
	public boolean isAggregateFlagOn() {
		return(isAggregate);
	}
	
	public boolean allowUndo() {
		return(allowUndo);
	}
	

	transient String colDisplayClass = null;
	public String getDisplayClass()
	{
		if(colDisplayClass == null) {
			colDisplayClass = BiUtil.extractColDecorationValue(fdformat, "displayClass");
			if(colDisplayClass == null) colDisplayClass = "";
		}
		return(StringUtils.isBlank(colDisplayClass) ? null : colDisplayClass);
	}
	transient String colDisplayStr = null;
	public String getDisplayStr()
	{
		if(colDisplayStr == null) {
			colDisplayStr = BiUtil.extractColDecorationValue(fdformat, "displayStr");
			if(colDisplayStr == null) colDisplayStr = "";
		}
		return(StringUtils.isBlank(colDisplayStr) ? null : colDisplayStr);
	}
	
	public boolean isVirtualMasterColumn() {
		return(invisible && (!inList) && (!inSelect) );
	}
	
	public boolean getAllowPickOldInput() {
		return(allowPickOldInput);
	}
}
