package com.uniinformation.bicore.bischema;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONObject;

import com.kyoko.common.ChineseConvert;
import com.kyoko.common.DateUtil;
import com.kyoko.parser.Condition;
import com.kyoko.parser.Variable;
import com.kyoko.parser.VariableSet;
import com.kyoko.parser.Expression;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;

public class ExcelFunction {

	BiCellCollection bc;
	
	Pair<String,String> getPairedName(String p_varname) throws Exception {
		String viewName = null;
		String varName = null;
		int sheetNameIdx = p_varname.indexOf('!');
		if(sheetNameIdx >= 0) {
			if(sheetNameIdx == 0) {
				viewName = bc.getBr().getView().getName();
				varName = p_varname.substring(1);
			} else {
//				sheetName = com.uniinformation.bicore.DataModelHelper.workSheetNameToViewName(br.getSessionHelper(), p_varname.substring(0,sheetNameIdx));
				viewName = bc.getBr().getSessionHelper().getDbName()+"."+p_varname.substring(0,sheetNameIdx);
				varName = p_varname.substring(sheetNameIdx+1);
			}
		} else {
			varName = p_varname;
		}
		return(Pair.of(viewName, varName));
	}
	
	public ExcelFunction(BiCellCollection p_bc) {
		bc = p_bc;
	}
	public class BicoreDataSet {
		private BiResult ssbr;
		private Pair<String,String> varName;
		private int[] xcolPosition;
		private String[]xcolumns;
		private int startRow;
		private int endRow;
		private Cell matchedCell;
		private int operator;
		
		public boolean sbrIsNull() {
			return(ssbr == null);
		}
		public int getRowCount(){
			return(ssbr.getRecordCount());
		}
		public int getColumnCount(){
			return(xcolPosition.length);
		}
		public Object getDataSetObject(int p_col,int p_row) throws Exception {
			return(ssbr.getColumnValueFromCache(xcolumns[p_col], p_row));
		}
		public void setMatchedCell(Cell p_value) {
			if(p_value != null && p_value.getType() == Cell.VTYPE_STRING) {
				if(p_value.getString().startsWith(">=")) {
					operator = Condition.COMPARE_OP_GE;
					matchedCell = new Cell(p_value.getString().substring(2));
				} else if(p_value.getString().startsWith("<=")) {
					operator = Condition.COMPARE_OP_LE;
					matchedCell = new Cell(p_value.getString().substring(2));
				} else if(p_value.getString().startsWith("<>")) {
					operator = Condition.COMPARE_OP_NE;
					matchedCell = new Cell(p_value.getString().substring(2));
				} else if(p_value.getString().startsWith(">")) {
					operator = Condition.COMPARE_OP_GT;
					matchedCell = new Cell(p_value.getString().substring(1));
				} else if(p_value.getString().startsWith("<")) {
					operator = Condition.COMPARE_OP_LT;
					matchedCell = new Cell(p_value.getString().substring(1));
				} else if(p_value.getString().startsWith("=")) {
					operator = Condition.COMPARE_OP_EQ;
					matchedCell = new Cell(p_value.getString().substring(1));
				} else {
					operator = Condition.COMPARE_OP_EQ;
					matchedCell = p_value;
				}
			} else {
				matchedCell = p_value;
				operator = Condition.COMPARE_OP_EQ;
			}
		}
		public boolean match(Object o) {
			/*
			switch(operator) {
			case Condition.COMPARE_OP_GT : return(matchedCell.compareToIgnoreCase(o) > 0);
			case Condition.COMPARE_OP_LT : return(matchedCell.compareToIgnoreCase(o) < 0);
			case Condition.COMPARE_OP_GE : return(matchedCell.compareToIgnoreCase(o) >= 0);
			case Condition.COMPARE_OP_LE : return(matchedCell.compareToIgnoreCase(o) <= 0);
			case Condition.COMPARE_OP_NE : return(!matchedCell.equalsIgnoreCase(o));
			default : return(matchedCell.equalsIgnoreCase(o));
			}
			*/
			switch(operator) {
			case Condition.COMPARE_OP_GT : return(Cell.objectCompare(o,matchedCell,true) > 0);
			case Condition.COMPARE_OP_LT : return(Cell.objectCompare(o,matchedCell,true) < 0);
			case Condition.COMPARE_OP_GE : return(Cell.objectCompare(o,matchedCell,true) >= 0);
			case Condition.COMPARE_OP_LE : return(Cell.objectCompare(o,matchedCell,true) <= 0);
			case Condition.COMPARE_OP_NE : return(Cell.objectCompare(o,matchedCell,true) != 0);
			default : return(Cell.objectCompare(o,matchedCell,true) == 0);
			}
		}
		public int getMatchType() {
			if(matchedCell == null) {
				return(-1); 
			} else if( matchedCell.getType()== Cell.VTYPE_STRING &&  matchedCell.getString().equals("")) {
				return(-1);
			} else return matchedCell.getType();
		}
		public BicoreDataSet(String p_brName,int p_startRow,int p_endRow,String[] p_columns) throws Exception {
			startRow = p_startRow;
			endRow = p_endRow;
			if(startRow < 0 ) startRow = bc.getIdx();
			if(endRow < 0 ) endRow = bc.getIdx();
			if(StringUtils.isBlank(p_brName)) ssbr = bc.getBr(); else ssbr = ExcelWorkSheetCache.getBrFromCache(bc.getBr().getSessionHelper(),p_brName);
			if(endRow > ssbr.getRowCount()) endRow = ssbr.getRowCount();
			if(startRow > ssbr.getRowCount()) endRow = ssbr.getRowCount();
			xcolumns = p_columns;
			xcolPosition = new int[xcolumns.length];
			for(int i=0;i<xcolumns.length;i++) {
				xcolPosition[i] = ssbr.getColumnCachePositionFromHash(xcolumns[i]);
			}
		}
	}
	
	private class ExcelDataSet {
		private BiResult ssbr;
		private Pair<String,String> varName;
		private int[] xcolPosition;
		private int startRow;
		private int endRow;
		private Cell matchedCell;
		private int operator;
		private String lhLANG=null;
		
		public boolean sbrIsNull() {
			return(ssbr == null);
		}
		public boolean columnHashIsNull() {
			if(ssbr.isVirtualMaster()) {
				return(ssbr.getListColumns().size() <= 0);
			} else {
				return(ssbr.getColumnObjectPositionHash() == null);
			}
		}
		public int getRowCount(){
			return(ssbr.getRecordCount());
		}
		public int getColumnCount(){
			return(xcolPosition.length);
		}
		public Object getDataSetObject(int p_col,int p_row) throws Exception {
			Object o;
			if(ssbr.isVirtualMaster()) {
				o = (ssbr.getColumnValueFromCache(ssbr.getListColumns().get(xcolPosition[p_col]).getLabel(), p_row));
			} else {
				o = (ssbr.getResultTrObject(false,xcolPosition[p_col],p_row));
			}
			if(lhLANG != null && o instanceof String) {
				if(lhLANG.equals("TCHN")) {
					o  = ChineseConvert.convertAuto2Bnew((String) o);
				}
				if(lhLANG.equals("SCHN")) {
					o  = ChineseConvert.convertAuto2Gnew((String) o);
				}
			}
			return(o);
		}
		public void setMatchedCell(Cell p_value) {
			if(p_value != null && p_value.getType() == Cell.VTYPE_STRING) {
				if(p_value.getString().startsWith(">=")) {
					operator = Condition.COMPARE_OP_GE;
					matchedCell = new Cell(p_value.getString().substring(2));
				} else if(p_value.getString().startsWith("<=")) {
					operator = Condition.COMPARE_OP_LE;
					matchedCell = new Cell(p_value.getString().substring(2));
				} else if(p_value.getString().startsWith("<>")) {
					operator = Condition.COMPARE_OP_NE;
					matchedCell = new Cell(p_value.getString().substring(2));
				} else if(p_value.getString().startsWith(">")) {
					operator = Condition.COMPARE_OP_GT;
					matchedCell = new Cell(p_value.getString().substring(1));
				} else if(p_value.getString().startsWith("<")) {
					operator = Condition.COMPARE_OP_LT;
					matchedCell = new Cell(p_value.getString().substring(1));
				} else if(p_value.getString().startsWith("=")) {
					operator = Condition.COMPARE_OP_EQ;
					matchedCell = new Cell(p_value.getString().substring(1));
				} else {
					operator = Condition.COMPARE_OP_EQ;
					matchedCell = p_value;
				}
			} else {
				matchedCell = p_value;
				operator = Condition.COMPARE_OP_EQ;
			}
		}
		public boolean match(Object o) {
			/*
			switch(operator) {
			case Condition.COMPARE_OP_GT : return(matchedCell.compareToIgnoreCase(o) > 0);
			case Condition.COMPARE_OP_LT : return(matchedCell.compareToIgnoreCase(o) < 0);
			case Condition.COMPARE_OP_GE : return(matchedCell.compareToIgnoreCase(o) >= 0);
			case Condition.COMPARE_OP_LE : return(matchedCell.compareToIgnoreCase(o) <= 0);
			case Condition.COMPARE_OP_NE : return(!matchedCell.equalsIgnoreCase(o));
			default : return(matchedCell.equalsIgnoreCase(o));
			}
			*/
			switch(operator) {
			case Condition.COMPARE_OP_GT : return(Cell.objectCompare(o,matchedCell,true) > 0);
			case Condition.COMPARE_OP_LT : return(Cell.objectCompare(o,matchedCell,true) < 0);
			case Condition.COMPARE_OP_GE : return(Cell.objectCompare(o,matchedCell,true) >= 0);
			case Condition.COMPARE_OP_LE : return(Cell.objectCompare(o,matchedCell,true) <= 0);
			case Condition.COMPARE_OP_NE : return(Cell.objectCompare(o,matchedCell,true) != 0);
			default : return(Cell.objectCompare(o,matchedCell,true) == 0);
			}
		}
		public int getMatchType() {
			if(matchedCell == null) {
				return(-1); 
			} else if( matchedCell.getType()== Cell.VTYPE_STRING &&  matchedCell.getString().equals("")) {
				return(-1);
			} else return matchedCell.getType();
		}
		public ExcelDataSet(Object fromCell,Object toCell) throws Exception {
//			Variable vStart = ((Expression) fromCell).getVariable();
//			Variable vEnd = ((Expression) toCell).getVariable();
			Variable vStart = ((Variable) fromCell);
			Variable vEnd = ((Variable) toCell);
			Pair<Boolean,Integer> rowIndexPair = vStart.getRowIndex();
			if(rowIndexPair.getLeft()) {
				startRow = rowIndexPair.getRight();
			} else {
				startRow = rowIndexPair.getRight() + bc.getIdx();
			}
			rowIndexPair = vEnd.getRowIndex();
			if(rowIndexPair.getLeft()) {
				endRow = rowIndexPair.getRight();
			} else {
				endRow = rowIndexPair.getRight() + bc.getIdx();
			}
			varName = getPairedName(vStart.getName());
			String endName = getPairedName(vEnd.getName()).getRight();
			if(varName.getLeft() == null) {
				ssbr = bc.getBr();
			} else {
				ssbr = ExcelWorkSheetCache.getBrFromCache(bc.getBr().getSessionHelper(),varName.getLeft());
			}
			if(ssbr != null && ssbr.getSessionHelper().getBiSchema().getSchemaAutotranslate()) {
				lhLANG = bc.getBr().getSessionHelper().getLHLang();
			}
			/*
			if(sbr.getColumnObjectPositionHash() == null) {
				return(0);
			}
			*/
			if(ssbr != null) {
//			int startColPosition = sbr.getColumnObjectPositionHash().get(varName.getRight());
			if(ssbr.isVirtualMaster() ) {
				int startColPosition = ssbr.getListColumns().indexOf(ssbr.getColumnByLabel(varName.getRight()));
				int endColPosition = ssbr.getListColumns().indexOf(ssbr.getColumnByLabel(endName));
				if(startColPosition < 0 || endColPosition < 0 || (endColPosition - startColPosition < 0)) {
					xcolPosition = new int[0];
				} else {
					xcolPosition = new int[endColPosition-startColPosition+1];
					for(int j = 0;j<xcolPosition.length;j++) {
						xcolPosition[j] = startColPosition+j;
					}
				}
			} else {
				Integer startColPosition = null;
				if(ssbr.getColumnObjectPositionHash() != null) startColPosition = ssbr.getColumnObjectPositionHash().get(varName.getRight());
				if(startColPosition == null) {
					startColPosition = 0;
				}
//				int endColPosition = sbr.getColumnObjectPositionHash().get(endName);
				Integer endColPosition = null;
				if(ssbr.getColumnObjectPositionHash() != null) endColPosition = ssbr.getColumnObjectPositionHash().get(endName);
				if(endColPosition == null) {
					endColPosition = ssbr.getSelectListSize()-1;
				}
				xcolPosition = new int[endColPosition-startColPosition+1];
				for(int j = 0;j<xcolPosition.length;j++) {
					xcolPosition[j] = startColPosition+j;
				}
			}
			}
		}
	}	
	/*
	public class CountDataSet {
		BiResult sbr;
		Pair<String,String> varName;
		int[] colPosition;
		int startRow;
		int endRow;
		Cell matchCell;
	}	
	*/
	private enum FuncName { 
		FUNC_excelSUM,FUNC_excelVLOOKUP,FUNC_excelCOUNTIF,FUNC_excelCOUNTIFS, FUNC_excelDAY,FUNC_excelCOUNTA,FUNC_excelIFERROR,FUNC_excelSUMIFS
		,FUNC_excelSUMIF,FUNC_excelOR,FUNC_excelAND,FUNC_excelTEXT,FUNC_excelDATEDIF,FUNC_excelEOMONTH,FUNC_excelIFNA,FUNC_excelDATE
		,FUNC_excelMAXIF,FUNC_excelMAXIFS
		,FUNC_excelMINIF,FUNC_excelMINIFS
		,FUNC_defaultLdesc , FUNC_countMonths
		,FUNC_getUniqueFieldEx,FUNC_defaultLcrg
		,FUNC_excelXLOOKUP
		,FUNC_jsonSUMBY
		,FUNC_jsonGet
		,NOT_DEFINED }

	public Object evalFunction(String p_fname,Vector args,ColumnCell formulaInit) throws Exception
	{
		/*
		FuncName funcName = FuncName.NOT_DEFINED;
		try {
			funcName = FuncName.valueOf("FUNC_"+p_fname);
		}
		catch(Exception ex) {
			//remark: if enum not exist, will got exception here.
		}
		*/
		/*
		//230425 obsoelected. replaced by checkAndGetFuncNameCache
		FuncName funcName = checkAndGetFuncName(p_fname);
		*/
		FuncName funcName = FuncName.NOT_DEFINED;
		try {
			funcName = FuncName.valueOf("FUNC_"+p_fname);
		}
		catch(Exception ex) {
			//remark: if enum not exist, will got exception here.
		}
		
		switch (funcName){
			case FUNC_excelDATE: {
				if(formulaInit != null) return(DateUtil.zeroDate);
				Cell y = ((Expression) args.get(0)).eval(null);
				Cell m = ((Expression) args.get(1)).eval(null);
				Cell d = ((Expression) args.get(2)).eval(null);
				return DateUtil.getTime(y.getInt(),m.getInt(),d.getInt(),0,0,0);
			}
			case FUNC_excelIFNA:
			case FUNC_excelIFERROR:  {
				if(formulaInit != null) return("");
				Cell c = ((Expression) args.get(0)).eval(null);
				if(c == null) {
					c = ((Expression) args.get(1)).eval(null);
				} else {
					switch(c.getType()) {
					case Cell.VTYPE_DATE : if(!DateUtil.minDate.before(c.getDate())) {
							c = ((Expression) args.get(1)).eval(null);
						}
					break;
					case Cell.VTYPE_DOUBLE : if(Double.isNaN(c.getDouble())) {
							c = ((Expression) args.get(1)).eval(null);
						}
					break;
					/*
					case Cell.VTYPE_STRING : if(c.getString().equals("#N/A")) {
							c = ((Expression) args.get(1)).eval(null);
						}
					break;
					*/
					default :
						if((c.getFlag() & 1) != 0) {
							c = ((Expression) args.get(1)).eval(null);
						}
					}
				}
				if(c == null) {
					return(null);
				} else {
					return(c.getObject());
				}
			}
			case FUNC_excelTEXT:  {
				if(formulaInit != null) return("");
				Cell c = ((Expression) args.get(0)).eval(null);
				if(c.getType() == Cell.VTYPE_DATE) {
					Cell fmt = ((Expression) args.get(1)).eval(null);
					if(fmt.getType() != Cell.VTYPE_STRING) return(c.getDate());
					if(fmt.getString().equals("DDD")) {
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE");
						return(simpleDateFormat.format(c.getDate()));
					} else if(fmt.getString().equals("DDDD")) {
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE");
						return(simpleDateFormat.format(c.getDate()));
					} else {
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat("DD/MM/YY");
						return(simpleDateFormat.format(c.getDate()));
					}
				} else return(c.toString());
			}
			case FUNC_excelDATEDIF:  {
				if(formulaInit != null) return(0.0);
//				UniLog.log1("HAHA240215 ignore DATEDIF return 0");
				Cell sdate = ((Expression) args.get(0)).eval(null);
				Cell edate = ((Expression) args.get(1)).eval(null);
				Cell fmt = ((Expression) args.get(2)).eval(null);
				String ss = fmt.getString();
				if(ss.equals("Y")) {
					int y0 = DateUtil.getYear(sdate.getDate());
					int y1 = DateUtil.getYear(edate.getDate());
					return((double) (y1-y0));
				} else if(ss.equals("M")) {
					int y0 = DateUtil.getYear(sdate.getDate());
					int y1 = DateUtil.getYear(edate.getDate());
					int m0 = DateUtil.getMonth(sdate.getDate());
					int m1 = DateUtil.getMonth(edate.getDate());
					return((double) ((y1-y0) * 12 + m1 - m0));
				} else if(ss.equals("D")) {
					int id0 = DateUtil.dateToInformix(sdate.getDate());
					int id1 = DateUtil.dateToInformix(edate.getDate());
					return((double) (id1-id0));
				} else if(ss.equals("MD")) {
					int d0 = DateUtil.getDay(sdate.getDate());
					int d1 = DateUtil.getDay(edate.getDate());
					return((double) (d1-d0));
				} else if(ss.equals("YM")) {
					int m0 = DateUtil.getMonth(sdate.getDate());
					int m1 = DateUtil.getMonth(edate.getDate());
					return((double) (m1-m0));
				} else if(ss.equals("YD")) {
					int y1 = DateUtil.getYear(edate.getDate());
					int m0 = DateUtil.getMonth(sdate.getDate());
					int d0 = DateUtil.getDay(sdate.getDate());
					java.util.Date dd = DateUtil.getDate(y1,m0,d0); /* not check what is returned if day is invalid for this year e.g. 2023/02/29 */
					int id0 = DateUtil.dateToInformix(dd);
					int id1 = DateUtil.dateToInformix(edate.getDate());
					return((double) (id1-id0));
				}
				return 0.0;
			}
			case FUNC_excelEOMONTH:  {
				if(formulaInit != null) return(DateUtil.zeroDate);
				Cell sdate = ((Expression) args.get(0)).eval(null);
				Cell mcount = ((Expression) args.get(1)).eval(null);
				int mCnt = mcount.getInt();
				java.util.Date  dd = sdate.getDate();
				if(mCnt > 0) {
					dd = DateUtil.nextmonth(dd, mCnt);
				}
				return(DateUtil.monthEnd(dd));
			}
			case FUNC_excelDAY:  {
				if(formulaInit != null) return(0);
				Cell c = ((Expression) args.get(0)).eval(null);

				Date d = c.getDate();
				if(d == null) return(0);
				int m = DateUtil.getDay(d);
				return(m);
			}
			case FUNC_excelCOUNTA:  {
				if(formulaInit != null) return(0);
				int cnt = 0;
				for(Object o : args) {
					if(o instanceof Expression ) {
						cnt++;
					}
					if(o instanceof VariableSet) {
						ExcelDataSet ds = new ExcelDataSet(
							((VariableSet) o).getStart(),
							((VariableSet) o).getEnd()
						);
						if(ds.sbrIsNull()) return(0);
						int rowCount = ds.endRow;
//						if(rowCount == Integer.MAX_VALUE) rowCount = ds.sbr.getRowCount()-1;
						if(rowCount == Integer.MAX_VALUE) rowCount = ds.getRowCount() - 1;
						rowCount = rowCount + 1;
						for(int i=ds.startRow;i<rowCount;i++) {
							for(int j=0;j<ds.getColumnCount();j++) {
								Object oo = ds.getDataSetObject(j,i);
								if(oo != null) {
									cnt++;
								}
							}
						}
					}
					return(cnt);
				}
			}
			case FUNC_excelSUM:  {
				if(formulaInit != null) return(0.0);
					double sumval = 0.0;
					for(Object o : args) {
						if(o instanceof Expression ) {
							Cell c = ((Expression) o).eval(null);
							if(c != null && c.getType() == Cell.VTYPE_DOUBLE) sumval += c.getDouble();
						}
						if(o instanceof VariableSet) {
							ExcelDataSet ds = new ExcelDataSet(
								((VariableSet) o).getStart(),
								((VariableSet) o).getEnd()
							);
							if(ds.sbrIsNull()) return(0);
							int rowCount = ds.endRow;
//							if(rowCount == Integer.MAX_VALUE) rowCount = ds.sbr.getRowCount()-1;
							if(rowCount == Integer.MAX_VALUE) rowCount = ds.getRowCount() - 1;
							rowCount = rowCount + 1;
							for(int i=ds.startRow;i<rowCount;i++) {
								for(int j=0;j<ds.getColumnCount();j++) {
									Object oo = ds.getDataSetObject(j,i);
									if(oo instanceof Double) {
										sumval += ((Double) oo);
									}
								}
							}
						}
						return(sumval);
					}
			}
			case FUNC_excelXLOOKUP:  {
				if(formulaInit != null) return("");
					/*
					 * 0 workshheet, 1 startrow, 2 endrow, 3 resultColumn, 4 defaultValue [lookupValue,lookupColumn]..
					 */
					if(args.size() == 7) {
						Cell lookupCell = ((Expression) args.get(5)).eval(null);
						String cols[] = new String[2];
						cols[0] = ((Expression) args.get(6)).eval(null).getString();
						cols[1] = ((Expression) args.get(3)).eval(null).getString();
						Object defaultValue = ((Expression) args.get(4)).eval(null).getObject();
						BicoreDataSet ds = new BicoreDataSet(
								((Expression) args.get(0)).eval(null).getString(),
								((Expression) args.get(1)).eval(null).getInt(),
								((Expression) args.get(2)).eval(null).getInt(),
								cols
								);
						if(ds.sbrIsNull()) return(defaultValue);
						for (int i=ds.startRow;i<ds.getRowCount();i++) {
							Object o = ds.getDataSetObject(0,i);
							if(lookupCell.equalsIgnoreCase(o)) {
								return(ds.getDataSetObject(1,i));
							}
						}
						return(defaultValue);					
					}
					else if(args.size() < 7 || (((args.size()-5) % 2) != 0)) {
							throw new Exception("Argument Count Mismatch"); // Simple version 3 argument and single value return only 
					} else { int numMatch = (args.size()-5)/2;
						String cols[] = new String[numMatch+1];
						Cell[] lookupCell = new Cell[numMatch];
						cols[numMatch] = ((Expression) args.get(3)).eval(null).getString();
						for(int i=0;i<numMatch;i++) {
							cols[i] = ((Expression) args.get(6+i*2)).eval(null).getString();
							lookupCell[i] = ((Expression) args.get(5+i*2)).eval(null);
						}
						Object defaultValue = ((Expression) args.get(4)).eval(null).getObject();
						BicoreDataSet ds = new BicoreDataSet(
								((Expression) args.get(0)).eval(null).getString(),
								((Expression) args.get(1)).eval(null).getInt(),
								((Expression) args.get(2)).eval(null).getInt(),
								cols
								);
						if(ds.sbrIsNull()) return(defaultValue);
						for (int i=ds.startRow;i<ds.getRowCount();i++) {
							boolean allMatched = true;
							for(int j=0;j<numMatch;j++) {
								Object o = ds.getDataSetObject(j,i);
								if(!lookupCell[j].equalsIgnoreCase(o)) {
									allMatched = false;
									break;
								}
							}
							if(allMatched) {
								return(ds.getDataSetObject(numMatch,i));
							}
						}
						return(defaultValue);					
					}
				}
			case FUNC_excelVLOOKUP:  {
				if(formulaInit != null) return("");
					int lookupColIdx = 0;
					if(args.size() != 4 && args.size() != 5) throw new Exception("Argument Count Mismatch");
					if(args.size() == 5) {
						lookupColIdx = ((Expression) args.get(4)).eval(null).getInt()-1;
					}
					ExcelDataSet ds = new ExcelDataSet(
								((VariableSet) args.get(1)).getStart(),
								((VariableSet) args.get(1)).getEnd()
							);
					if(ds.sbrIsNull()) return(0);
					if(ds.columnHashIsNull()) return(null);
					Cell lookupCell = ((Expression) args.get(0)).eval(null);
					int lookupColumn = ((Expression) args.get(2)).eval(null).getInt()-1;
					boolean notExactMatch = ((Expression) args.get(3)).eval(null).getBoolean();
					int rowCount = ds.endRow;
//					if(rowCount == Integer.MAX_VALUE) rowCount = ds.sbr.getRowCount() - 1;
					if(rowCount == Integer.MAX_VALUE) rowCount = ds.getRowCount() - 1;
					rowCount = rowCount + 1;
					if(lookupCell.getString().equals("AFS-CN230412-122（DGH)_2023A03002_PJ-200_020009")) {
						int cc;
						cc = 0;
						
					}
					for (int i=ds.startRow;i<rowCount;i++) {
//						Object o = ds.sbr.getResultTrObject(false,ds.colPosition[lookupColIdx],i);
						Object o = ds.getDataSetObject(lookupColIdx,i);
						if(i >= 110) {
							int cc;
							cc = 0;
						}
						if(notExactMatch) {
							if(lookupCell.compareToIgnoreCase(o) <= 0) {
//								return(ds.sbr.getResultTrObject(false,ds.colPosition[lookupColumn],i));
								return(ds.getDataSetObject(lookupColumn,i));
							}
						} else {
							if(lookupCell.equalsIgnoreCase(o)) {
//								return(ds.sbr.getResultTrObject(false,ds.colPosition[lookupColumn],i));
								return(ds.getDataSetObject(lookupColumn,i));
							}
						}
					}
					/*
					switch(lookupCell.getType()) {
					case Cell.VTYPE_BOOLEAN : return(false);
					case Cell.VTYPE_STRING : return("");
					case Cell.VTYPE_DOUBLE : return(0.0);
					case Cell.VTYPE_INT : return(0);
					case Cell.VTYPE_DATETIME : return(DateUtil.zeroDate);
					case Cell.VTYPE_DATE : return(DateUtil.zeroDate);
					}
					*/
					switch(lookupCell.getType()) {
					case Cell.VTYPE_BOOLEAN : return(null);
					case Cell.VTYPE_STRING : {
						return("#N/A");
					}
					case Cell.VTYPE_DOUBLE : return(Double.NaN);
					case Cell.VTYPE_INT : return(Integer.MIN_VALUE);
					case Cell.VTYPE_DATETIME : return(DateUtil.zeroDate);
					case Cell.VTYPE_DATE : return(DateUtil.zeroDate);
					}
					return(null);					
					
				}
			case FUNC_excelSUMIF:
			case FUNC_excelSUMIFS:  {
				if(formulaInit != null) return(0.0);
					double sumval = 0.0;
					int cnt;
					ExcelDataSet sumds=null;
					ExcelDataSet[] dataSets;
					switch(funcName) {
					case FUNC_excelSUMIF:
						cnt = 1;
						dataSets= new ExcelDataSet[1];
						dataSets[0] = new ExcelDataSet(
										((VariableSet) args.get(0)).getStart(),
										((VariableSet) args.get(0)).getEnd());
//						dataSets[0].matchCell = ((Expression) args.get(1)).eval(null);
						dataSets[0].setMatchedCell(((Expression) args.get(1)).eval(null));
//						if(dataSets[0].sbr == null) return(0.0);
						if(dataSets[0].sbrIsNull()) return(0.0);
						if( dataSets[0].getMatchType() < 0) return(0.0);
						if(args.size() < 3) {
							sumds = dataSets[0];
						} else {
							sumds = new ExcelDataSet(
									((VariableSet) args.get(2)).getStart(),
									((VariableSet) args.get(2)).getEnd());
							
						}
						break;
					case FUNC_excelSUMIFS:
						cnt = ((args.size() - 1)/ 2);
						dataSets= new ExcelDataSet[cnt];
						sumds = new ExcelDataSet(
									((VariableSet) args.get(0)).getStart(),
									((VariableSet) args.get(0)).getEnd());
						for(int n=0;n<cnt;n++) {
							dataSets[n] = 
									new ExcelDataSet(
									((VariableSet) args.get(1+n*2)).getStart(),
									((VariableSet) args.get(1+n*2)).getEnd());
//							if(dataSets[n].sbr == null) return(0.0);
							if(dataSets[n].sbrIsNull()) return(0.0);
							dataSets[n].setMatchedCell(((Expression) args.get(2+n*2)).eval(null));
							if( dataSets[n].getMatchType() < 0) {
								return(0.0);
							}
						}
						break;
					default : return(0.0);
					}
//					if(sumds.sbr == null) return(0.0);
					if(sumds.sbrIsNull()) return(0.0);

					int rowCount = dataSets[0].endRow;
//					if(rowCount == Integer.MAX_VALUE) rowCount = dataSets[0].sbr.getRowCount()-1;
//					if(rowCount == Integer.MAX_VALUE) rowCount = dataSets[0].sbr.getRecordCount()-1;
					if(rowCount == Integer.MAX_VALUE) rowCount = dataSets[0].getRowCount() - 1;
					rowCount = rowCount - dataSets[0].startRow + 1;
					int colCount = dataSets[0].getColumnCount();
					if(colCount == 1) {
						rowCount+=dataSets[0].startRow;
						for (int i=dataSets[0].startRow;i<rowCount;i++) {
							boolean matched=true;
							for(ExcelDataSet ds : dataSets) {
//								Object o = ds.sbr.getResultTrObject(false,ds.colPosition[0],i);
								Object o = ds.getDataSetObject(0,i);
								if(!(matched = ds.match(o)) ) break;
							}	
							if(matched) {
//								Object so = sumds.sbr.getResultTrObject(false,sumds.colPosition[0],i);
								Object so = sumds.getDataSetObject(0,i);
								if(so instanceof Double) sumval += (Double) so;
							}
						}
					} else {
						for (int i=0;i<rowCount;i++) {
							for(int j=0;j<colCount;j++) {
								boolean matched=true;
								for(ExcelDataSet ds : dataSets) {
//									Object o = ds.sbr.getResultTrObject(false,ds.colPosition[j],i+ds.startRow);
									Object o = ds.getDataSetObject(j,i+ds.startRow);
									if(!(matched = ds.match(o)) ) break;
								}	
								if(matched) {
//									Object so = sumds.sbr.getResultTrObject(false,sumds.colPosition[j],i);
									Object so = sumds.getDataSetObject(j,i);
									if(so instanceof Double) sumval += (Double) so;
								}
							}
						}
					}
					return(sumval);
				}
			case FUNC_excelMAXIF:
			case FUNC_excelMAXIFS:  {
				if(formulaInit != null) return(0.0);
					Object maxval = null;
					int cnt;
					ExcelDataSet sumds=null;
					ExcelDataSet[] dataSets;
					switch(funcName) {
					case FUNC_excelMAXIF:
						cnt = 1;
						dataSets= new ExcelDataSet[1];
						dataSets[0] = new ExcelDataSet(
										((VariableSet) args.get(0)).getStart(),
										((VariableSet) args.get(0)).getEnd());
//						dataSets[0].matchCell = ((Expression) args.get(1)).eval(null);
						dataSets[0].setMatchedCell(((Expression) args.get(1)).eval(null));
//						if(dataSets[0].sbr == null) return(0.0);
						if(dataSets[0].sbrIsNull()) return(0.0);
						if( dataSets[0].getMatchType() < 0) return(0.0);
						if(args.size() < 3) {
							sumds = dataSets[0];
						}
						break;
					case FUNC_excelMAXIFS:
						cnt = ((args.size() - 1)/ 2);
						dataSets= new ExcelDataSet[cnt];
						sumds = new ExcelDataSet(
									((VariableSet) args.get(0)).getStart(),
									((VariableSet) args.get(0)).getEnd());
						for(int n=0;n<cnt;n++) {
							dataSets[n] = 
									new ExcelDataSet(
									((VariableSet) args.get(1+n*2)).getStart(),
									((VariableSet) args.get(1+n*2)).getEnd());
//							if(dataSets[n].sbr == null) return(0.0);
							if(dataSets[n].sbrIsNull()) return(0.0);
							dataSets[n].setMatchedCell(((Expression) args.get(2+n*2)).eval(null));
							if( dataSets[n].getMatchType() < 0) {
								return(0.0);
							}
						}
						break;
					default : return(0.0);
					}
//					if(sumds.sbr == null) return(0.0);
					if(sumds.sbrIsNull()) return(0.0);

					int rowCount = dataSets[0].endRow;
//					if(rowCount == Integer.MAX_VALUE) rowCount = dataSets[0].sbr.getRowCount()-1;
					if(rowCount == Integer.MAX_VALUE) rowCount = dataSets[0].getRowCount() - 1;
					rowCount = rowCount - dataSets[0].startRow + 1;
					int colCount = dataSets[0].getColumnCount();
					if(colCount == 1) {
						rowCount+=dataSets[0].startRow;
						for (int i=dataSets[0].startRow;i<rowCount;i++) {
							boolean matched=true;
							for(ExcelDataSet ds : dataSets) {
//								Object o = ds.sbr.getResultTrObject(false,ds.colPosition[0],i);
								Object o = ds.getDataSetObject(0,i);
								if(!(matched = ds.match(o)) ) break;
							}	
							if(matched) {
//								Object so = sumds.sbr.getResultTrObject(false,sumds.colPosition[0],i);
								Object so = sumds.getDataSetObject(0,i);
								if(maxval== null) {
									maxval = so ;
								} else {
									if(Cell.objectCompare(so, maxval, true) > 0) {
										maxval = so;
									}
								}
							}
						}
					} else {
						for (int i=0;i<rowCount;i++) {
							for(int j=0;j<colCount;j++) {
								boolean matched=true;
								for(ExcelDataSet ds : dataSets) {
//									Object o = ds.sbr.getResultTrObject(false,ds.colPosition[j],i+ds.startRow);
									Object o = ds.getDataSetObject(j,i+ds.startRow);
									if(!(matched = ds.match(o)) ) break;
								}	
								if(matched) {
//									Object so = sumds.sbr.getResultTrObject(false,sumds.colPosition[j],i);
									Object so = sumds.getDataSetObject(j,i);
									if(maxval== null) {
										maxval = so ;
									} else {
										if(Cell.objectCompare(so, maxval, true) > 0) {
											maxval = so;
										}
									}
								}
							}
						}
					}
					return(maxval == null ? "#N/A" : maxval);
				}
			case FUNC_excelMINIF:
			case FUNC_excelMINIFS:  {
				if(formulaInit != null) return(0.0);
					Object minval = null;
					int cnt;
					ExcelDataSet sumds=null;
					ExcelDataSet[] dataSets;
					switch(funcName) {
					case FUNC_excelMINIF:
						cnt = 1;
						dataSets= new ExcelDataSet[1];
						dataSets[0] = new ExcelDataSet(
										((VariableSet) args.get(0)).getStart(),
										((VariableSet) args.get(0)).getEnd());
//						dataSets[0].matchCell = ((Expression) args.get(1)).eval(null);
						dataSets[0].setMatchedCell(((Expression) args.get(1)).eval(null));
//						if(dataSets[0].sbr == null) return(0.0);
						if(dataSets[0].sbrIsNull()) return(0.0);
						if( dataSets[0].getMatchType() < 0) return(0.0);
						if(args.size() < 3) {
							sumds = dataSets[0];
						}
						break;
					case FUNC_excelMINIFS:
						cnt = ((args.size() - 1)/ 2);
						dataSets= new ExcelDataSet[cnt];
						sumds = new ExcelDataSet(
									((VariableSet) args.get(0)).getStart(),
									((VariableSet) args.get(0)).getEnd());
						for(int n=0;n<cnt;n++) {
							dataSets[n] = 
									new ExcelDataSet(
									((VariableSet) args.get(1+n*2)).getStart(),
									((VariableSet) args.get(1+n*2)).getEnd());
//							if(dataSets[n].sbr == null) return(0.0);
							if(dataSets[n].sbrIsNull()) return(0.0);
							dataSets[n].setMatchedCell(((Expression) args.get(2+n*2)).eval(null));
							if( dataSets[n].getMatchType() < 0) {
								return(0.0);
							}
						}
						break;
					default : return(0.0);
					}
//					if(sumds.sbr == null) return(0.0);
					if(sumds.sbrIsNull()) return(0.0);

					int rowCount = dataSets[0].endRow;
//					if(rowCount == Integer.MAX_VALUE) rowCount = dataSets[0].sbr.getRowCount()-1;
					if(rowCount == Integer.MAX_VALUE) rowCount = dataSets[0].getRowCount() - 1;
					rowCount = rowCount - dataSets[0].startRow + 1;
					int colCount = dataSets[0].getColumnCount();
					if(colCount == 1) {
						rowCount+=dataSets[0].startRow;
						for (int i=dataSets[0].startRow;i<rowCount;i++) {
							boolean matched=true;
							for(ExcelDataSet ds : dataSets) {
//								Object o = ds.sbr.getResultTrObject(false,ds.colPosition[0],i);
								Object o = ds.getDataSetObject(0,i);
								if(!(matched = ds.match(o)) ) break;
							}	
							if(matched) {
//								Object so = sumds.sbr.getResultTrObject(false,sumds.colPosition[0],i);
								Object so = sumds.getDataSetObject(0,i);
								if(minval== null) {
									minval = so ;
								} else {
									if(Cell.objectCompare(so, minval, true) < 0) {
										minval = so;
									}
								}
							}
						}
					} else {
						for (int i=0;i<rowCount;i++) {
							for(int j=0;j<colCount;j++) {
								boolean matched=true;
								for(ExcelDataSet ds : dataSets) {
//									Object o = ds.sbr.getResultTrObject(false,ds.colPosition[j],i+ds.startRow);
									Object o = ds.getDataSetObject(j,i+ds.startRow);
									if(!(matched = ds.match(o)) ) break;
								}	
								if(matched) {
//									Object so = sumds.sbr.getResultTrObject(false,sumds.colPosition[j],i);
									Object so = sumds.getDataSetObject(j,i);
									if(minval== null) {
										minval = so ;
									} else {
										if(Cell.objectCompare(so, minval, true) > 0) {
											minval = so;
										}
									}
								}
							}
						}
					}
					return(minval == null ? "#N/A" : minval);
				}
			case FUNC_excelCOUNTIF: 
			case FUNC_excelCOUNTIFS: 
				{
				if(formulaInit != null) return(0);
				int cnt = 0;
				int numDataSet = args.size()/2;
				switch(funcName) {
				case FUNC_excelCOUNTIF:
					if(args.size() != 2) throw new Exception("Argument Count Mismatch");
					break;
				case FUNC_excelCOUNTIFS:
					if(args.size() % 2 != 0 || args.size() < 2) throw new Exception("Argument Count Mismatch");
					break;
				}
				ExcelDataSet[] dataSets= new ExcelDataSet[numDataSet];
//				Cell[] matchCells = new Cell[numDataSet];
				for(int i=0;i<numDataSet;i++) {
					dataSets[i] = new ExcelDataSet(
								((VariableSet) args.get(i*2)).getStart(),
								((VariableSet) args.get(i*2)).getEnd()
							);
//					matchCells[i] = ((Expression) args.get(i*2+1)).eval(null);
//					dataSets[i].matchCell = ((Expression) args.get(i*2+1)).eval(null);
					dataSets[i].setMatchedCell(((Expression) args.get(i*2+1)).eval(null));
					if (dataSets[i].getMatchType() < 0) return(0);
				}
				int rowCount = dataSets[0].endRow;
//				if(rowCount == Integer.MAX_VALUE) rowCount = dataSets[0].sbr.getRowCount()-1;
				if(rowCount == Integer.MAX_VALUE) rowCount = dataSets[0].getRowCount() - 1;
				rowCount = rowCount - dataSets[0].startRow + 1;
				int colCount = dataSets[0].getColumnCount();
				if(colCount == 1) {
					rowCount+=dataSets[0].startRow;
					for (int i=dataSets[0].startRow;i<rowCount;i++) {
						boolean matched=true;
//						for(int k=0;k<dataSets.length;k++) {
//							ExcelDataSet ds = dataSets[k];
//							if(k == 3) {
//								int cc;
//								cc = 0;
//							}
//							Object o = ds.sbr.getResultTrObject(false,ds.colPosition[0],i);
//							if(!(matched = ds.match(o)) ) break;
//						}	
						for(ExcelDataSet ds : dataSets) {
//							Object o = ds.sbr.getResultTrObject(false,ds.colPosition[0],i);
							Object o = ds.getDataSetObject(0,i);
							if(!(matched = ds.match(o)) ) break;
						}	
						if(matched) {
							cnt++;
						}
					}
					
				} else {
				for (int i=0;i<rowCount;i++) {
					for(int j=0;j<colCount;j++) {
						boolean matched=true;
						for(ExcelDataSet ds : dataSets) {
//							Object o = ds.sbr.getResultTrObject(false,ds.colPosition[j],i+ds.startRow);
							Object o = ds.getDataSetObject(j,i+ds.startRow);
							if(!(matched = ds.match(o)) ) break;
						}	
						if(matched) {
							cnt++;
						}
					}
				}
				}
				return(cnt);
				}			
			case FUNC_excelAND : {
				if(formulaInit != null) return(false);
				for(Object o : args) {
					if(o == null) return(false);
					if(o instanceof Condition) {
						boolean tv = ((Condition) o).eval(null);
						if(!tv) return(false);
					} else if(o instanceof Expression) {
						Cell cc = ((Expression) o).eval(null);
						if(!cc.getBoolean()) return(false);
					} else if(o instanceof Integer) {
						if(((Integer) o) == 0) return(false);
					} else if(o instanceof Double) {
						if(((Double ) o) == 0.0) return(false);
					} else if(o instanceof String ) {
						if(!((String) o).trim().equals("Y")) return(false);
					} else if(o instanceof Boolean ) {
						if(!((Boolean) o)) return(false);
					} else {
						return(false);
					}
				}
				return(true);
			}
			case FUNC_excelOR: {
				if(formulaInit != null) return(false);
				for(Object o : args) {
					if(o == null) return(false);
					if(o instanceof Condition) {
						boolean tv = ((Condition) o).eval(null);
						if(tv) return(true);
					} else if(o instanceof Expression) {
						Cell cc = ((Expression) o).eval(null);
						if(cc.getBoolean()) return(true);
					} else if(o instanceof Integer) {
						if(((Integer) o) != 0) return(true);
					} else if(o instanceof Double) {
						if(((Double ) o) != 0.0) return(true);
					} else if(o instanceof String ) {
						if(((String) o).trim().equals("Y")) return(true);
					} else if(o instanceof Boolean ) {
						if(((Boolean) o)) return(true);
					} else {
						return(false);
					}
				}
				return(false);
			}
			case FUNC_countMonths: {
				if(formulaInit != null) return(0);
				Date date0 = (Date) args.get(0);
				Date date1 = (Date) args.get(1);
				if(DateUtil.minDate.after(date0)) return(0);
				if(DateUtil.minDate.after(date1)) return(0);
				int y0 = DateUtil.getYear(date0);
				int y1 = DateUtil.getYear(date1);
				int m0 = DateUtil.getMonth(date0);
				int m1 = DateUtil.getMonth(date1);
				return((y1-y0) * 12 + m1 - m0 + 1);
			}
			case FUNC_jsonSUMBY:  {
				if(formulaInit != null) return("{}");
					int cnt;
					ExcelDataSet sumds=null;
					ExcelDataSet[] dataSets;
					switch(funcName) {
					case FUNC_jsonSUMBY:
						cnt = 1;
						dataSets= new ExcelDataSet[1];
						dataSets[0] = new ExcelDataSet(
										((VariableSet) args.get(0)).getStart(),
										((VariableSet) args.get(0)).getEnd());
//						dataSets[0].matchCell = ((Expression) args.get(1)).eval(null);
//						if(dataSets[0].sbr == null) return(0.0);
						if(dataSets[0].sbrIsNull()) return("{}");
						sumds = new ExcelDataSet(
							((VariableSet) args.get(2)).getStart(),
							((VariableSet) args.get(2)).getEnd());
//						sumds.setMatchedCell(((Expression) args.get(1)).eval(null));
						break;
					default : return("{}");
					}
//					if(sumds.sbr == null) return(0.0);
					if(sumds.sbrIsNull()) return("{}");

					JSONObject sumval = new JSONObject();
					int rowCount = dataSets[0].endRow;
					if(rowCount == Integer.MAX_VALUE) rowCount = dataSets[0].getRowCount() - 1;
					rowCount = rowCount - dataSets[0].startRow + 1;
					int colCount = dataSets[0].getColumnCount();
					if(colCount == 1) {
						rowCount+=dataSets[0].startRow;
//						String aggregate = sumds.matchedCell.toString();
						for (int i=dataSets[0].startRow;i<rowCount;i++) {
							for(ExcelDataSet ds : dataSets) {
//								Object o = ds.sbr.getResultTrObject(false,ds.colPosition[0],i);
								Object o = ds.getDataSetObject(0,i);
								Object so = sumds.getDataSetObject(0,i);
								if(so instanceof Double) {
									double dval = (Double) so;
									/* assumed sum , ignored aggregate */
									Double currval = sumval.optDouble(o.toString());
									if(currval != null && !currval.isNaN()) dval += currval;
									sumval.put(o.toString(), dval);
								} else {
									String sval = so.toString();
									/* ignore if not double */
								}
							}	
						}
					} else {
					}
					return(sumval.toString());
				}
			case FUNC_jsonGet:  {
				JSONObject jo;
				try {
					jo = new JSONObject(args.get(0).toString());
					String key = args.get(1).toString();
					Object val = jo.opt(key);
					if(val == null) {
						if(args.size() > 2) return(args.get(2)); else return(0.0);
					} else {
						return(val);
					}
				} catch (Exception ex) {
					UniLog.log(ex);
					if(args.size() > 2) return(args.get(2)); else return(0.0);
				}
			}
			case FUNC_getUniqueFieldEx: {
			/*
			Object o = (Double) p_args.get(0);
			double di = (Double) p_args.get(0);
			int rg = (int) di;
			*/
			int rg = Cell.objectToInt(args.get(0));
			String tabName = (String) args.get(1);
			String fieldName = (String) args.get(2);
			String format = (String) args.get(3);
			if(rg <= 0) return("");
			if(StringUtils.isBlank(tabName)) return("");
			if(StringUtils.isBlank(fieldName)) return("");
			if(StringUtils.isBlank(format)) return("");
			boolean reuse = true;
			if(args.size() > 4) reuse = (Boolean) args.get(4);
			String s = BiSchema.getUniqueFieldEx(bc.getBr(),tabName,fieldName,format,rg,reuse);
			return(s);
		}
			

		}
		return(null);
	}
}
