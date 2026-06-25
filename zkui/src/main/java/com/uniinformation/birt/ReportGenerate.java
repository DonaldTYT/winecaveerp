package com.uniinformation.birt;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.birt.report.engine.api.DocxRenderOption;
import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.IPDFRenderOption;
import org.eclipse.birt.report.engine.api.IReportEngine;
import org.eclipse.birt.report.engine.api.IReportRunnable;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;
import org.eclipse.birt.report.model.api.CellHandle;
import org.eclipse.birt.report.model.api.ColumnHandle;
import org.eclipse.birt.report.model.api.DataItemHandle;
import org.eclipse.birt.report.model.api.DataSetHandle;
import org.eclipse.birt.report.model.api.DesignElementHandle;
import org.eclipse.birt.report.model.api.ElementFactory;
import org.eclipse.birt.report.model.api.GridHandle;
import org.eclipse.birt.report.model.api.ModuleHandle;
import org.eclipse.birt.report.model.api.PropertyHandle;
import org.eclipse.birt.report.model.api.ReportDesignHandle;
import org.eclipse.birt.report.model.api.RowHandle;
import org.eclipse.birt.report.model.api.ScriptDataSetHandle;
import org.eclipse.birt.report.model.api.SimpleMasterPageHandle;
import org.eclipse.birt.report.model.api.SlotHandle;
import org.eclipse.birt.report.model.api.StructureFactory;
import org.eclipse.birt.report.model.api.TableHandle;
import org.eclipse.birt.report.model.api.TextItemHandle;
import org.eclipse.birt.report.model.api.core.UserPropertyDefn;
import org.eclipse.birt.report.model.api.elements.DesignChoiceConstants;
import org.eclipse.birt.report.model.api.elements.structures.ComputedColumn;
import org.eclipse.birt.report.model.api.elements.structures.DateTimeFormatValue;
import org.eclipse.birt.report.model.api.elements.structures.HighlightRule;
import org.eclipse.birt.report.model.api.elements.structures.NumberFormatValue;
import org.eclipse.birt.report.model.api.elements.structures.ResultSetColumn;
import org.eclipse.birt.report.model.elements.interfaces.IListingElementModel;
import org.eclipse.birt.report.model.elements.interfaces.IMasterPageModel;
import org.eclipse.birt.report.model.elements.interfaces.IStyleModel;
import org.eclipse.birt.report.model.metadata.MetaDataDictionary;
import org.eclipse.birt.report.model.metadata.PropertyType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.util.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Auxhead;
import org.zkoss.zul.Auxheader;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Doublespinner;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Space;
import org.zkoss.zul.Span;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.MessageboxDlg;

import com.google.common.collect.Lists;
import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.AggregateOrPivot.AggregateRec;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.utils.BIRTUtil;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrParser.TextSpliter;
import com.uniinformation.utils.ConditionPresets;
import com.uniinformation.utils.ConditionPresets.ConditionFieldMap;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZipUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiComposerBase.MultiSortInfo;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;
import com.uniinformation.zkbi.ZkBiTranslateHelper;

import uk.co.spudsoft.birt.emitters.excel.ExcelEmitter;

public class ReportGenerate {
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private static final SimpleDateFormat datetimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static final DecimalFormat integerFormat = new DecimalFormat("#,##0");
	private static final DecimalFormat floatFormat = new DecimalFormat("#,##0.00");
	private static final DecimalFormat integerNoCommaFormat = new DecimalFormat("#0");
	private static final DecimalFormat floatNoCommaFormat = new DecimalFormat("#0.00");
	private static final float sortingTextFontSize = 5f;
	
	private static class ColumnInfo {
		String key, skey;
		int columnLabelIdx = -1;
		int aopIdx = -1;
		String name;
		String[] aopHeaderNames;
		int[] aopHeaderColSpans;
		MultiSortInfo sortInfo;
		boolean isAggColumn;
		AggFunc aggFunc;
		//String fieldLabel;
		String dataType;
		float width;
		boolean hasNotEmptyField;
		DecimalFormat format;
		Double minNumber;
		Double maxNumber;
		float maxWidth;
		int fieldColumnLength;
		String fieldColumnType;
		String[] fieldOptionList;
		void setMinNumber(Double n) {
			if (minNumber == null)
				minNumber = n;
			else
				minNumber = Math.min(minNumber, n);
		}
		void setMaxNumber(Double n) {
			if (maxNumber == null)
				maxNumber = n;
			else
				maxNumber = Math.max(maxNumber, n);
		}
	}
	private static class AggCalInfo {
		List<ColumnInfo> columnList = new ArrayList<ColumnInfo>();
		String dataType;
		int count;
		Double minNumber;
		Double maxNumber;
		double totalNumber;
		void clear() {
			count = 0;
			minNumber = null;
			maxNumber = null;
			totalNumber = 0;
		}
		void addNumber(double n) {
			if (Double.isNaN(n) || Double.isInfinite(n))
				n = 0;
			count++;
			totalNumber += n;
			minNumber = (minNumber == null) ? n : Math.min(minNumber, n);
			maxNumber = (maxNumber == null) ? n : Math.max(maxNumber, n);
		}
		Double getNumber(AggFunc aggFunc) {
			/*switch (aggFunc) {
				case Count:
					return getNumberFormat(aggFunc).format(count);
				case Min:
					//return minNumber != null ? (dataType.equals("float") ? floatFormat.format(minNumber) : integerFormat.format((int)(double)minNumber)) : "-";
					return minNumber != null ? getNumberFormat(aggFunc).format(dataType.equals("float") ? minNumber : (int)(double)minNumber) : "-";
				case Max:
					//return maxNumber != null ? (dataType.equals("float") ? floatFormat.format(maxNumber) : integerFormat.format((int)(double)maxNumber)) : "-";
					return maxNumber != null ? getNumberFormat(aggFunc).format(dataType.equals("float") ? maxNumber : (int)(double)maxNumber) : "-";
				case Sum:
					//return dataType.equals("float") ? floatFormat.format(totalNumber) : integerFormat.format((int)(double)totalNumber);
					return getNumberFormat(aggFunc).format(dataType.equals("float") ? totalNumber : (int)(double)totalNumber);
				case Avg:
					//return dataType.equals("float") ? floatFormat.format(totalNumber / count) : integerFormat.format((int)(totalNumber / count));
					return getNumberFormat(aggFunc).format(dataType.equals("float") ? (totalNumber / count) : (int)(totalNumber / count));
			}
			return "-";*/
			switch (aggFunc) {
				case Count:
					return (double)count;
				case Min:
					return minNumber;
				case Max:
					return maxNumber;
				case Sum:
					return totalNumber;
				case Avg:
					return totalNumber / count;
			}
			return null;
		}
	}
	private static enum AggFunc {
		Count, Sum, Min, Max, Avg
	}

	private List<Map<String, Object>> dsList = new ArrayList<Map<String, Object>>();
	private List<ColumnInfo> columnList = new ArrayList<ColumnInfo>();
	private Map<String, AggCalInfo> aggCalMap = new LinkedHashMap<String, AggCalInfo>();
	//private List<String> aggregateOrPivotList;
	private Map<String, List<Pair<Integer, String>>> aggregateOrPivotMap; //key: unique aop, value: list of Pair<aop index, aop alias>
	private Map<String, String> aggregateSubtotalMap = new HashMap<String, String>();
	private Map<String, Double> aggregateSubtotalValueMap = new HashMap<String, Double>();
	private Map<String, String> itemCreateScriptMap;
	private Map<String, Object> userPropMap;
	private float innerTableWidth;
	private boolean hasTableExtraSpaceColumn;
	private int designHeaderCount = 1;
	private boolean hasDesignFooter;
	
	private String rptPaperSize;
	private String rptOrientation;
	private boolean hasAlternateRowColor;
	private float fontSize;
	private String title, subTitle, queryConditions;
	private String designRes;
	private boolean isExcelAutoFilter;
	private boolean isExcelSingleSheet;
	private boolean isShowQueryConditions;
	private String outputFileType;
	private String password;
	private String outFileName;
	
	private boolean isCustomReport;
	private Callback callback;
	
	public ReportGenerate(SessionHelper sessionHelper, BiResult result, 
								List<StrAndNum> columnLabelList, Map<String, AggregateOrPivot.AGGREGATES> aggregateTypeMap, Map<String, Integer> aggregateVTypeMap, Map<Integer, MultiSortInfo> sortColumnMap, Map<String, String[]> aopHeaderMap,
								Map<String, String> itemCreateScriptMap, Map<String, Object> userPropMap, ListModelList listModelList, List<Integer> aopIdxList, Map<String, List<Pair<Integer, String>>> aggregateOrPivotMap,
								Set<StrAndNum> p_aggColumnSet, Map<StrAndNum, List<AggFunc>> aggResultColumnMap,
								String rptPaperSize, String rptOrientation, boolean hasAlternateRowColor, float fontSize, boolean hideEmptyColumns, 
								boolean isDisplayAggregateColFirst, boolean isDisplayAggregateResultAfterRegularColumn, boolean isDisplayAggregateColumnOnly, 
								boolean isExcelAutoFilter, boolean isExcelSingleSheet,
								boolean isShowQueryConditions, boolean isExportAllDetail,
								String title, String subTitle, String queryConditions, String designRes, String outputFileType, String password, String outFileName, Callback callback) {
		this.isCustomReport = !StringUtils.equals(designRes, "ReportGenerate.rptdesign");
		this.itemCreateScriptMap = itemCreateScriptMap != null ? itemCreateScriptMap : new HashMap<String, String>();
		this.userPropMap = userPropMap != null ? userPropMap : new HashMap<String, Object>();
		this.callback = callback;
		this.rptPaperSize = rptPaperSize;
		this.rptOrientation = rptOrientation;
		this.hasAlternateRowColor = hasAlternateRowColor;
		this.fontSize = fontSize;
		this.title = title;
		this.subTitle = subTitle;
		this.queryConditions = queryConditions;
		this.designRes = designRes;
		this.isExcelAutoFilter = isExcelAutoFilter;
		this.isExcelSingleSheet = isExcelSingleSheet;
		this.isShowQueryConditions = isShowQueryConditions;
		this.outputFileType = outputFileType;
		this.password = password;
		this.outFileName = outFileName;
		
		Integer count = null;
		for (String[] ss : aopHeaderMap.values()) {
			if (count == null)
				count = ss.length;
			else
				count = Math.min(count, ss.length);
		}
		if (count != null)
			designHeaderCount = Math.max(count, designHeaderCount);
		
		//aggregateOrPivotList = result.getAggregateOrPivotList();
		this.aggregateOrPivotMap = aggregateOrPivotMap;
		
		Set<String> aggColumnSet = new HashSet<String>();

		//setup subtotal map
		Object[] vals = result.getAggregateSubtotal();
		/*if (aggregateOrPivotList != null && vals != null) {
			for (int i = 0; i < vals.length && i < aggregateOrPivotList.size(); i++) {
				String key = aggregateOrPivotList.get(i);
				UniLog.log1("key:%s, vals:%s, class:%s", key, vals[i], vals[i] == null ? "null" : vals[i].getClass());
				if (vals[i] != null && vals[i] instanceof Double) {
					double d = (Double)vals[i];
 					DecimalFormat df = new DecimalFormat("##,###,###,##0.00");
					if (result.getAggregateOrPivotHeader() != null) {
    					AggregateRec aggRec = result.getAggregateOrPivotHeader().getAggregate(i);
 						String fmt = aggRec.getFormat(result);
 						if (fmt != null)
 							df = new DecimalFormat(fmt);
 					}
					if (!Double.isInfinite(d) && !Double.isNaN(d))
						aggregateSubtotalMap.put(key, df.format(d));
				}
			}
		}*/
		for (List<Pair<Integer, String>> list : aggregateOrPivotMap.values()) {
			for (Pair<Integer, String> p : list) {
				int i = p.x;
				String key = p.y;
				if (vals != null && i < vals.length) {
					UniLog.log1("key:%s, vals:%s, class:%s", key, vals[i], vals[i] == null ? "null" : vals[i].getClass());
					if (vals[i] != null && vals[i] instanceof Double) {
						double d = (Double)vals[i];
 						DecimalFormat df = new DecimalFormat("##,###,###,##0.00");
						if (result.getAggregateOrPivotHeader() != null) {
    						AggregateRec aggRec = result.getAggregateOrPivotHeader().getAggregate(i);
 							String fmt = aggRec.getFormat(result);
 							if (fmt != null)
 								df = new DecimalFormat(fmt);
 						}
						if (!Double.isInfinite(d) && !Double.isNaN(d)) {
							aggregateSubtotalMap.put(key, df.format(d));
							aggregateSubtotalValueMap.put(key, d);
						}
					}
				}
			}
		}
		
		//regular column
		int cikeyi = 0;
		for (StrAndNum sn : columnLabelList) {
			String label = sn.str;
			int type;
			ColumnInfo ci = new ColumnInfo();
			//ci.fieldLabel = label;
			ci.skey = ci.key = label;
			ci.columnLabelIdx = sn.num;
			if (StringUtils.isBlank(ci.key)) {
				String[] cls = new String[columnLabelList.size()];
				for (int i = 0; i < cls.length; i++)
					cls[i] = columnLabelList.get(i).str;
				for (;;) {
					ci.key = "cikey" + cikeyi++;
					//if (!StringUtils.equalsAny(ci.key, columnLabelList.toArray(new String[0])))
					if (!StringUtils.equalsAny(ci.key, cls))
						break;
				}
			}
			//if (aggregateOrPivotList != null && aggregateOrPivotList.indexOf(label) >= 0) {
			if (aggregateOrPivotMap.containsKey(label)) {
				if (ci.columnLabelIdx >= 0 && ci.columnLabelIdx < aopIdxList.size()) {
					/*int apIdx = aopIdxList.get(ci.columnLabelIdx);
					List<Pair<Integer, String>> list = aggregateOrPivotMap.get(label);
					for (Pair<Integer, String> p : list) {
						if (p.x == apIdx) {
							ci.aopIdx = p.x;
							if (StringUtils.isNotBlank(p.y))
								ci.key = p.y;
							break;
						}
					}
					if (ci.aopIdx == -1)
						ci.aopIdx = list.get(0).x;*/
					ColumnInfo tci = getAopIdx(aggregateOrPivotMap, aopIdxList, ci.columnLabelIdx, label);
					ci.aopIdx = tci.aopIdx;
					if (tci.key != null)
						ci.key = tci.key;
				}
				if (aopHeaderMap.containsKey(label)) {
					ci.aopHeaderNames = aopHeaderMap.get(label);
					ci.name = ci.aopHeaderNames[0];
					ci.aopHeaderColSpans = new int[ci.aopHeaderNames.length];
					Arrays.fill(ci.aopHeaderColSpans, 1);
				}
				else
					ci.name = label;
				boolean isDouble;
				AggregateOrPivot.AGGREGATES agm = aggregateTypeMap.get(label);
				Integer vt = aggregateVTypeMap.get(label);
				if (agm == AggregateOrPivot.AGGREGATES.STRCAT || agm == AggregateOrPivot.AGGREGATES.UNIQUECAT)
					isDouble = false;
				else if (agm == AggregateOrPivot.AGGREGATES.FIRST || agm == AggregateOrPivot.AGGREGATES.LAST) {
					if (vt != null && vt == Cell.VTYPE_DOUBLE)
						isDouble = true;
					else
						isDouble = false;
				} else
					isDouble = true;
				if (isDouble) {
					type = Cell.VTYPE_DOUBLE;
					ci.format = floatFormat;
					if (result.getAggregateOrPivotHeader() != null) {
						AggregateRec aggRec = result.getAggregateOrPivotHeader().getAggregate(label);
						String fmt = aggRec.getFormat(result);
						if (fmt != null)
							ci.format = new DecimalFormat(fmt);
					}
					ci.fieldColumnLength = 14;
				} else {
					type = Cell.VTYPE_STRING;
					ci.fieldColumnLength = 10;
				}
			}
			else {
				ColumnCell cc = result.getCell(label);
				BiColumn bc = cc.getBiColumn();
				//ci.name = sessionHelper.getLabel(bc.getEngName());
				ci.name = getBiColumnTranName(sessionHelper, bc);
				ci.format = cc.getDecFormat();
				ci.fieldColumnLength = bc.getColumnLength();
				ci.fieldColumnType = bc.getColumnType();
				ci.fieldOptionList = bc.getOptionList(sessionHelper);
				type = cc.getType();
			}
			
			//ci.sortInfo = sortColumnMap.get(label);
			ci.sortInfo = sortColumnMap.get(ci.columnLabelIdx);
			//ci.isAggColumn = aggColumnSet.contains(label);
			ci.isAggColumn = false;
			for (StrAndNum asn : p_aggColumnSet) {
				if (StringUtils.equals(asn.str, label) && asn.num == ci.columnLabelIdx) {
					aggColumnSet.add(ci.key);
					ci.isAggColumn = true;
					break;
				}
			}
			UniLog.log1("column label:%s, type:%d, format:%s, isAggColumn:%b, key:%s, skey:%s, name:%s, aopIdx:%d, aopHeaderNames:%s", label, type, ci.format != null ? ci.format.toPattern() : "", ci.isAggColumn, ci.key, ci.skey, ci.name, ci.aopIdx, ci.aopHeaderNames);
			switch (type) {
			case Cell.VTYPE_DOUBLE:
				ci.dataType = "float";
				if (ci.format == null)
					ci.format = floatNoCommaFormat;
				break;
			case Cell.VTYPE_INT:
				if (!isNullOrEmpty(ci.fieldOptionList))
					ci.dataType = "string";
				else {
					ci.dataType = "integer";
					if (ci.format == null)
						ci.format = integerNoCommaFormat;
				}
				break;
			case Cell.VTYPE_DATE:
				ci.dataType = "date";
				break;
			case Cell.VTYPE_DATETIME:
				ci.dataType = "date-time";
				break;
			default:
				ci.dataType = "string";
				break;
			}
			columnList.add(ci);
		}
		//aggregate result column
		for (Map.Entry<StrAndNum, List<AggFunc>> entry : aggResultColumnMap.entrySet()) {
			String label = entry.getKey().str;
			int colLabelIdx = entry.getKey().num;
			if (label != null /*&& !columnLabelList.contains(label)*/) {
				boolean flag = false;
				for (StrAndNum sn : columnLabelList) {
					if (StringUtils.equals(sn.str, label) && colLabelIdx == sn.num) {
						flag = true;
						break;
					}
				}
				if (!flag)
					continue;
			}
			List<AggFunc> aggFuncList = entry.getValue();
			String name, dataType;
			String[] aopHeaderNames = null;
			//if (aggregateOrPivotList != null && aggregateOrPivotList.indexOf(label) >= 0) {
			if (aggregateOrPivotMap.containsKey(label)) {
				if (aopHeaderMap.containsKey(label)) {
					aopHeaderNames = aopHeaderMap.get(label);
					name = aopHeaderNames[0];
				}
				else
					name = label;
				dataType = "float";
			}
			else {
				ColumnCell cc = label != null ? result.getCell(label) : null;
				BiColumn bc = cc != null ? cc.getBiColumn() : null;
				//name = bc != null ? sessionHelper.getLabel(bc.getEngName()) : "";
				name = bc != null ? getBiColumnTranName(sessionHelper, bc) : "";
				dataType = (cc != null && cc.getType() == Cell.VTYPE_DOUBLE) ? "float" : "integer";
			}
			
			for (ColumnInfo ci : columnList) {
				if (StringUtils.equals(ci.skey, label) && ci.columnLabelIdx == colLabelIdx) {
					label = ci.key;
					break;
				}
			}
			
			AggCalInfo aci = aggCalMap.get(label);
			if (aci == null) {
				aci = new AggCalInfo();
				aci.dataType = dataType;
				aggCalMap.put(label, aci);
			}
			for (AggFunc aggFunc : aggFuncList) {
				ColumnInfo ci = new ColumnInfo();
				//ci.fieldLabel = label;
				ci.skey = ci.key = String.format("%s(%s)", aggFunc.name(), label != null ? label : "");
				ci.name = String.format("%s(%s)", aggFunc.name(), name);
				if (aopHeaderNames != null) {
					ci.aopHeaderNames = aopHeaderNames;
					ci.aopHeaderColSpans = new int[aopHeaderNames.length];
					Arrays.fill(ci.aopHeaderColSpans, 1);
				}
				ci.isAggColumn = false;
				ci.aggFunc = aggFunc;
				//ci.dataType = "string";
				ci.dataType = "float";
				if (aggFunc == AggFunc.Count)
					ci.format = integerFormat;
				else {
					ci.format = floatFormat;
					for (ColumnInfo ci1 : columnList) {
						//if (ci1.skey.equals(label)) {
						if (ci1.key.equals(label)) {
							ci.format = ci1.format;
							break;
						}
					}
				}
				aci.columnList.add(ci);
			}
		}
		if (isDisplayAggregateColFirst) {
			//move the aggregate col to left hand side
			columnList.sort(new Comparator<ColumnInfo>() {
				@Override
				public int compare(ColumnInfo l, ColumnInfo r) {
					return (l.isAggColumn && !r.isAggColumn) ? -1 : ((!l.isAggColumn && r.isAggColumn) ? 1 : 0);
				}
			});
		}
		for (Map.Entry<String, AggCalInfo> entry : aggCalMap.entrySet()) {
			String aciKey = entry.getKey();
			AggCalInfo aci = entry.getValue();
			if (isDisplayAggregateResultAfterRegularColumn || aciKey == null) {
				//display aggregate col after all regular column 
				for (ColumnInfo ci : aci.columnList)
					columnList.add(ci);
			} else {
				//display aggregate col after aggregate func col
				for (int i = 0; i < columnList.size(); i++) {
					//String fl = columnList.get(i).fieldLabel;
					String fl = columnList.get(i).key;
					if (fl != null && fl.equals(aciKey)) {
						for (int j = 0; j < aci.columnList.size(); j++)
							columnList.add(i + 1 + j, aci.columnList.get(j));
						break;
					}
				}
			}
		}

		//set header column span
		for (int k = 1; k < designHeaderCount; k++) {
			for (int i = 0; i < columnList.size() - 1; ) {
				ColumnInfo curCi = columnList.get(i);
				String curHdr = "";
				if (curCi.aopHeaderNames != null)
					curHdr = StringUtils.defaultString(curCi.aopHeaderNames[k]);
				int startIdx = i;
				for (i++; i < columnList.size(); i++) {
					ColumnInfo nextCi = columnList.get(i);
					String nextHdr = "";
					if (nextCi.aopHeaderNames != null)
						nextHdr = StringUtils.defaultString(nextCi.aopHeaderNames[k]);
					if (!nextHdr.equals(curHdr))
						break;
				}
				int endIdx = i;
				for (int j = startIdx; j < endIdx; j++) {
					ColumnInfo ci = columnList.get(j);
					if (ci.aopHeaderColSpans != null)
						ci.aopHeaderColSpans[k] = endIdx - startIdx;
				}
			}
		}

		//setup quick filter List
		Set<Integer> quickFilterList = null;
		if (sessionHelper.getGeneralReportApplyQuickFilter() && listModelList != null) {
			quickFilterList = new HashSet<Integer>();
			for (int i = 0; i < listModelList.size(); i++) {
				Object o = listModelList.get(i);
				if (o instanceof TrStatFilter) {
					TrStatFilter tsf = (TrStatFilter)o;
					quickFilterList.add(tsf.getTrStatIdx());
				} else
					quickFilterList.add(i);
			}
		}

		//sublink items
		Vector<BiResult> subLinkList = null;
		Map<String, List<ColumnInfo>> subColInfosMap = new LinkedHashMap<String, List<ColumnInfo>>();
		Set<String> subColInfoFlags = new HashSet<String>();
		List<List<Map<String, Object>>> subDsList = new ArrayList<List<Map<String, Object>>>();
		if (isExportAllDetail) {
			subLinkList = result.getExportLinks();
			for (BiResult sbr : subLinkList) {
				List<BiColumn> subCols = sbr.getExportColumns();
				List<ColumnInfo> list = new ArrayList<ColumnInfo>();
				subColInfosMap.put(sbr.getView().getName(), list);
				for (BiColumn sbc : subCols) {
					ColumnInfo ci = new ColumnInfo();
					ci.skey = ci.key = sbr.getView().getName() + "_" + sbc.getLabel();
					ci.name = getBiColumnTranName(sessionHelper, sbc);
					ci.fieldColumnLength = sbc.getColumnLength();
					ci.fieldColumnType = sbc.getColumnType();
					ci.fieldOptionList = sbc.getOptionList(sessionHelper);
					ci.dataType = "string";
					list.add(ci);
				}
			}
		}

		//fill result records
		UniLog.log1("totalMemory 0:%dMB", Runtime.getRuntime().totalMemory() / 1024 / 1024);
		for (int i = 0; i < result.getRowCount(); i++) {
			if (quickFilterList != null && !quickFilterList.contains(i))
				continue;
			result.loadOneRecV(i);
			Map<String, Object> m = new LinkedHashMap<String, Object>();
			for (ColumnInfo ci : columnList) {
				if (ci.aggFunc != null) {
					//m.put(ci.key, "-");
					m.put(ci.key, Double.NaN);
					ci.hasNotEmptyField = true;
					ci.setMinNumber(0d);
					ci.setMaxNumber(0d);
				} else {
					int apIdx = ci.aopIdx;
					Object[] apVs = result.getAggregateValues(i);
					//if (apVs != null && (apIdx = aggregateOrPivotList.indexOf(ci.skey)) >= 0) {
					if (apVs != null && apIdx >= 0) {
						if(apVs.length > apIdx && apVs[apIdx] != null) {
							ci.hasNotEmptyField = true;
							Object v = apVs[apIdx];
							AggregateOrPivot.AGGREGATES agm = aggregateTypeMap.get(ci.skey);
							Integer vt = aggregateVTypeMap.get(ci.skey);
							//UniLog.log1("key:%s, agm:%s", ci.key, agm.name());
							boolean isDouble;
							if (agm == AggregateOrPivot.AGGREGATES.STRCAT || agm == AggregateOrPivot.AGGREGATES.UNIQUECAT)
								isDouble = false;
							else if (agm == AggregateOrPivot.AGGREGATES.FIRST || agm == AggregateOrPivot.AGGREGATES.LAST) {
								if (vt != null && vt == Cell.VTYPE_DOUBLE)
									isDouble = true;
								else
									isDouble = false;
							} else
								isDouble = true;
							if (isDouble) {
								double d = (Double)v;
								m.put(ci.key, v);
								if (Double.isInfinite(d) || Double.isNaN(d)) {
									ci.setMinNumber(0.0);
									ci.setMaxNumber(0.0);
								} else {
									ci.setMinNumber((Double) v);
									ci.setMaxNumber((Double) v);
								}
							} else
								m.put(ci.key, v.toString());
						} else {
							// prefer to set ci to Empty Field , but doing this will cause some pivot column to be missing in the generated report, set to 0.0 as intrim solution
//							ci.hasNotEmptyField = false;
							m.put(ci.key, Double.NaN);
							ci.hasNotEmptyField = true;
							ci.setMinNumber(0.0);
							ci.setMaxNumber(0.0);
						}
					}
					else {
						ColumnCell cc = result.getCell(ci.skey);
						switch (cc.getType()) {
						case Cell.VTYPE_DOUBLE:
							m.put(ci.key, cc.getDouble());
							ci.hasNotEmptyField = true;
							ci.setMinNumber(cc.getDouble());
							ci.setMaxNumber(cc.getDouble());
							break;
						case Cell.VTYPE_INT:
							if (!isNullOrEmpty(ci.fieldOptionList)) {
								String v;
								if (cc.getInt() >= 0 && cc.getInt() < ci.fieldOptionList.length)
									v = ci.fieldOptionList[cc.getInt()];
								else
									v = String.valueOf(cc.getInt());
								String value = StringUtils.defaultString(v).trim().replaceAll("\\s+", " ");
								m.put(ci.key, value);
								if (StringUtils.isNotBlank(value))
									ci.hasNotEmptyField = true;
							}
							else {
								m.put(ci.key, cc.getInt());
								ci.hasNotEmptyField = true;
								ci.setMinNumber((double)cc.getInt());
								ci.setMaxNumber((double)cc.getInt());
							}
							break;
						case Cell.VTYPE_DATE:
							m.put(ci.key, dateFormat.format((cc.getDate() == null || cc.getDate().before(DateUtil.minDate) ? DateUtil.minDate : cc.getDate())));
							ci.hasNotEmptyField = true;
							break;
						case Cell.VTYPE_DATETIME:
							m.put(ci.key, datetimeFormat.format((cc.getDate() == null || cc.getDate().before(DateUtil.minDate)) ? DateUtil.minDate : cc.getDate()));
							ci.hasNotEmptyField = true;
							break;
						default:
							String value = StringUtils.defaultString(cc.getString()).trim().replaceAll("\\s+", " ");
							m.put(ci.key, value);
							if (StringUtils.isNotBlank(value))
								ci.hasNotEmptyField = true;
							break;
						}
					}
				}
			}
			m.put("isDetailRow", false);
			dsList.add(m);

			if (isExportAllDetail) {
				result.fetchOneRecV(i);
				List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
				subDsList.add(list);
				for (BiResult sbr : subLinkList) {
					List<ColumnInfo> subColInfos = subColInfosMap.get(sbr.getView().getName());
					for (int j = 0; j < sbr.getRowCount(); j++) {
						CellCollection fds = sbr.getRowCollectionV(j);
						Map<String, Object> m1;
						if (j >= list.size()) {
							m1 = new LinkedHashMap<String, Object>();
							list.add(m1);
						} else
							m1 = list.get(j);
						for (ColumnInfo ci : subColInfos) {
							Cell cc = fds.testCell(ci.key.substring(sbr.getView().getName().length() + 1));
							UniLog.log1("sub key:%s,label:%s, datatype:%s, type:%d, format:%s", ci.key, ci.key.substring(sbr.getView().getName().length() + 1), ci.dataType, cc.getType(), cc.getDecFormat());
							if (cc != null) {
								boolean flag = subColInfoFlags.contains(ci.key);
								if (!flag)
									subColInfoFlags.add(ci.key);
								switch (cc.getType()) {
								case Cell.VTYPE_DOUBLE:
									if (!flag) {
										ci.dataType = "float";
										ci.format = cc.getDecFormat();
										if (ci.format == null)
											ci.format = floatNoCommaFormat;
									}
									m1.put(ci.key, cc.getDouble());
									ci.hasNotEmptyField = true;
									ci.setMinNumber(cc.getDouble());
									ci.setMaxNumber(cc.getDouble());
									break;
								case Cell.VTYPE_INT:
									if (!flag) {
										if (!isNullOrEmpty(ci.fieldOptionList))
											ci.dataType = "string";
										else {
											ci.dataType = "integer";
											ci.format = cc.getDecFormat();
											if (ci.format == null)
												ci.format = integerNoCommaFormat;
										}
									}
									if (!isNullOrEmpty(ci.fieldOptionList)) {
										String v;
										if (cc.getInt() >= 0 && cc.getInt() < ci.fieldOptionList.length)
											v = ci.fieldOptionList[cc.getInt()];
										else
											v = String.valueOf(cc.getInt());
										String value = StringUtils.defaultString(v).trim().replaceAll("\\s+", " ");
										m1.put(ci.key, value);
										if (StringUtils.isNotBlank(value))
											ci.hasNotEmptyField = true;
									}
									else {
										m1.put(ci.key, cc.getInt());
										ci.hasNotEmptyField = true;
										ci.setMinNumber((double)cc.getInt());
										ci.setMaxNumber((double)cc.getInt());
									}
									break;
								case Cell.VTYPE_DATE:
									if (!flag)
										ci.dataType = "date";
									m1.put(ci.key, dateFormat.format((cc.getDate() == null || cc.getDate().before(DateUtil.minDate) ? DateUtil.minDate : cc.getDate())));
									ci.hasNotEmptyField = true;
									break;
								case Cell.VTYPE_DATETIME:
									if (!flag)
										ci.dataType = "date-time";
									m1.put(ci.key, datetimeFormat.format((cc.getDate() == null || cc.getDate().before(DateUtil.minDate)) ? DateUtil.minDate : cc.getDate()));
									ci.hasNotEmptyField = true;
									break;
								default:
									String value = StringUtils.defaultString(cc.getString()).trim().replaceAll("\\s+", " ");
									m1.put(ci.key, value);
									if (StringUtils.isNotBlank(value))
										ci.hasNotEmptyField = true;
									break;
								}
							}
						}
					}
				}
			}
		}
		UniLog.log1("totalMemory 1:%dMB", Runtime.getRuntime().totalMemory() / 1024 / 1024);
		
		//cal agg
		if (!aggCalMap.isEmpty() && !dsList.isEmpty()) {
			Map<String, Object> curMap = null;
			for (int i = 0; i < dsList.size(); i++) {
				curMap = dsList.get(i);
				for (Map.Entry<String, AggCalInfo> entry : aggCalMap.entrySet()) {
					String aciKey = entry.getKey();
					AggCalInfo aci = entry.getValue();
					double d = 0;
					if (aciKey != null) {
						Object o = curMap.get(aciKey);
						if (o instanceof Double)
							d = (Double)o;
						else if (o instanceof Integer)
							d = (Integer)o;
					}
					aci.addNumber(d);
				}
				if (i < dsList.size() - 1) {
					Map<String, Object> nextMap = dsList.get(i + 1);
					for (String c : aggColumnSet) {
						//is end group
						if (!curMap.get(c).equals(nextMap.get(c))) {
							for (AggCalInfo aci : aggCalMap.values()) {
								for (ColumnInfo ci : aci.columnList) {
									Double d = aci.getNumber(ci.aggFunc);
									curMap.put(ci.key, d);
									ci.setMinNumber(d);
									ci.setMaxNumber(d);
								}
								aci.clear();
							}
							break;
						}
					}
				}
			}
			for (AggCalInfo aci : aggCalMap.values()) {
				for (ColumnInfo ci : aci.columnList) {
					Double d = aci.getNumber(ci.aggFunc);
					curMap.put(ci.key, d);
					ci.setMinNumber(d);
					ci.setMaxNumber(d);
				}
			}
		}
		
		//only show the row with aggregate result
		if (isDisplayAggregateColumnOnly && !aggResultColumnMap.isEmpty()) {
			for (Map.Entry<String, AggCalInfo> entry : aggCalMap.entrySet()) {
				if (!entry.getValue().columnList.isEmpty()) {
					String k = entry.getValue().columnList.get(0).key;
					Iterator<Map<String, Object>> it = dsList.iterator();
					while (it.hasNext()) {
						Map<String, Object> map = it.next();
						//if (map.get(k).equals("-"))
						if (map.get(k).equals(Double.NaN))
							it.remove();
					}
					break;
				}
			}
		}

		//hide empty columns, only show aggregate col and aggregate function col
		if ((!dsList.isEmpty() && hideEmptyColumns) || isDisplayAggregateColumnOnly) {
			Iterator<ColumnInfo> it = columnList.iterator();
			while (it.hasNext()) {
				ColumnInfo ci = it.next();
				if ((hideEmptyColumns && !ci.hasNotEmptyField)
						|| (isDisplayAggregateColumnOnly && (!aggColumnSet.isEmpty() || !aggResultColumnMap.isEmpty()) && !ci.isAggColumn && ci.aggFunc == null)) {
					for (Map<String, Object> m : dsList)
						m.remove(ci.key);
					it.remove();
				}
			}
		}
		
		if (isExportAllDetail) {
			for (List<ColumnInfo> ciList : subColInfosMap.values())
				columnList.addAll(ciList);
			int k = 0;
			for (int i = 0; i < dsList.size(); i++) {
				Map<String, Object> m = dsList.get(i);
				List<Map<String, Object>> list = subDsList.get(k++);
				for (int j = 0; j < list.size(); j++) {
					if (j == 0)
						m.putAll(list.get(j));
					else {
						m = new LinkedHashMap<String, Object>();
						m.putAll(list.get(j));
						m.put("isDetailRow", true);
						dsList.add(++i, m);
					}
				}
			}
		}

		//calc column width
		if (!StringUtils.equals(outputFileType, "csv")) {
			//page width: 297mm/210mm, margin left & right: 0.25in, border width: 1px
			innerTableWidth = ((rptOrientation.equals("landscape") ? getPaperSizeIn().y : getPaperSizeIn().x) - 0.25f * 2 - 1 / 96f * 2) * 72;
			final float wNumChar8 = getTextWidthPoint("99999999");
			final float wNumChar10 = getTextWidthPoint("9999999999");
			final float wStrChar10 = getTextWidthPoint("AAAAAAAAAA");
			float avgWidth = innerTableWidth / columnList.size() - 4;
			UniLog.log1("innerTableWidth:%f, avgWidth:%f", innerTableWidth, avgWidth);
			float leftWidth = innerTableWidth;
			int stringColumnCount = 0;
			float maxStringColumnWidth = 0f;
			int maxStringFieldColumnLength = 0;
			for (ColumnInfo ci : columnList) {
				String headerName = ci.name + (ci.sortInfo != null ? getSimilarSortingText() : "");
				float wHeader = getTextWidthPoint(headerName);
				if (ci.aopHeaderNames != null) {
					for (int i = 1; i < ci.aopHeaderNames.length; i++)
						wHeader = Math.max(getTextWidthPoint(ci.aopHeaderNames[i]) / ci.aopHeaderColSpans[i], wHeader);
					hasDesignFooter = true;
				}
				if (ci.dataType.equals("integer") || ci.dataType.equals("float")) {
					String minStr = ci.format.format(ci.minNumber != null ? ci.minNumber : 0.0).replaceAll("\\d", "9");
					String maxStr = ci.format.format(ci.maxNumber != null ? ci.maxNumber : 0.0).replaceAll("\\d", "9");
					if (ci.dataType.equals("integer") && this.itemCreateScriptMap.containsKey(ci.skey) && this.itemCreateScriptMap.get(ci.skey).contains("setHHmmDisplayValue(")) {
						minStr = (ci.minNumber / 60 + ":" + ci.minNumber % 60).replaceAll("\\d", "9");
						maxStr = (ci.maxNumber / 60 + ":" + ci.maxNumber % 60).replaceAll("\\d", "9");
					}
					float wDetail = Math.max(getTextWidthPoint(minStr), getTextWidthPoint(maxStr));
					if (ci.aopHeaderNames != null)
						wDetail = Math.max(wDetail, getTextWidthPoint(StringUtils.defaultString(aggregateSubtotalMap.get(ci.key))));
					if (wHeader > avgWidth) {
						float wNumCharN = ci.dataType.equals("float") ? wNumChar10 : wNumChar8;
						if (wNumCharN > avgWidth) {
							if (wHeader > wNumCharN)
								wHeader = getTextWidthPoint(headerName, wNumCharN);
						}
						else
							wHeader = wNumCharN;
					}
					ci.width = Math.max(wDetail, wHeader) + 4;
					leftWidth -= ci.width;
				}
				else if (ci.dataType.equals("date")) {
					float wDetail = getTextWidthPoint("9999/99/99");
					if (wHeader > avgWidth)
						wHeader = wDetail;
					ci.width = Math.max(wDetail, wHeader) + 4;
					leftWidth -= ci.width;
				}
				else if (ci.dataType.equals("date-time")) {
					float wDetail = getTextWidthPoint(StringUtils.equals(ci.fieldColumnType, "time") ? "99:99:99" : "9999/99/99 99:99:99");
					if (wHeader > avgWidth)
						wHeader = wDetail;
					ci.width = Math.max(wDetail, wHeader) + 4;
					leftWidth -= ci.width;
				}
				else {
					float wDetail = 0;
					for (Map<String, Object> m : dsList) {
						String v = (String)m.get(ci.key);
						wDetail = Math.max(wDetail, getTextWidthPoint(v));
					}
					if (wHeader > avgWidth)
					{
						if (wHeader > wStrChar10)
							wHeader = getTextWidthPoint(headerName, wStrChar10);
					}
					ci.maxWidth = Math.max(wDetail, wHeader) + 4;
					maxStringColumnWidth = Math.max(maxStringColumnWidth, ci.maxWidth);
					maxStringFieldColumnLength = Math.max(maxStringFieldColumnLength, ci.fieldColumnLength);
					stringColumnCount++;
				}
			}
			int leftColumnCount = stringColumnCount;
			int lastLeftColumnCount = leftColumnCount;
			boolean flag = false;
			List<ColumnInfo> tmpMList = new ArrayList<ColumnInfo>();
			while (leftWidth > 0 && stringColumnCount > 1 && leftColumnCount > 0) {
				avgWidth = leftWidth / stringColumnCount;
				UniLog.log1("haha0 leftWidth:%f,leftColumnCount:%d,stringColumnCount:%d,avgWidth:%f", leftWidth, leftColumnCount, stringColumnCount, avgWidth);
				List<ColumnInfo> tmpList = new ArrayList<ColumnInfo>();
				for (ColumnInfo ci : columnList) {
					if (leftColumnCount == 0)
						break;
					if (!ci.dataType.equals("string") || ci.width > 0)
						continue;
					if (ci.maxWidth == maxStringColumnWidth && ci.maxWidth >= avgWidth) {
						if (!flag) {
							leftColumnCount--;
							UniLog.log1("found maxStringColumnWidth:%f,%s", maxStringColumnWidth, ci.name);
							tmpMList.add(ci);
						}
						continue;
					}
					if (ci.maxWidth < avgWidth) {
						ci.width = ci.maxWidth;
						leftWidth -= ci.width;
						stringColumnCount--;
						leftColumnCount--;
						if (ci.fieldColumnLength == maxStringFieldColumnLength) {
							tmpList.add(ci);
							tmpMList.remove(ci);
						}
					}
				}
				if (leftColumnCount == 0 && stringColumnCount == 0 && !tmpList.isEmpty()) {
					if (leftWidth > 0) {
						float uLeftWidth = leftWidth;
						for (ColumnInfo ci : tmpList) {
							float f = Math.min(getSimilarTextWidth(ci.fieldColumnLength), ci.width * 2);
							if (f > ci.width)
								uLeftWidth -= f - ci.maxWidth;
						}
						UniLog.log1("uLeftWidth:%f, leftWidth:%f", uLeftWidth, leftWidth);
						if (uLeftWidth >= 0 && uLeftWidth < leftWidth) {
							for (ColumnInfo ci : tmpList) {
								float f = Math.min(getSimilarTextWidth(ci.fieldColumnLength), ci.width * 2);
								if (f > ci.width)
									ci.width = f;
							}
							leftWidth = uLeftWidth;
							if (leftWidth > 0)
								hasTableExtraSpaceColumn = true;
						} else
							hasTableExtraSpaceColumn = true;
					} else {
						for (ColumnInfo ci : tmpList)
							ci.width = 0;
					}
				}
				if (leftColumnCount == 0 && stringColumnCount > 0 && leftWidth > 0 && !tmpMList.isEmpty()) {
					float tLeftWidth = leftWidth;
					for (ColumnInfo ci : tmpMList)
						tLeftWidth -= ci.maxWidth;
					if (tLeftWidth > 0) {
						float uLeftWidth = tLeftWidth;
						for (ColumnInfo ci : tmpMList) {
							float f = Math.min(getSimilarTextWidth(ci.fieldColumnLength), ci.maxWidth * 2);
							if (f > ci.maxWidth)
								uLeftWidth -= f - ci.maxWidth;
						}
						if (uLeftWidth >= 0 && uLeftWidth < tLeftWidth) {
							for (ColumnInfo ci : tmpMList) {
								float f = Math.min(getSimilarTextWidth(ci.fieldColumnLength), ci.maxWidth * 2);
								if (f > ci.maxWidth)
									ci.width = f;
								else
									ci.width = ci.maxWidth;
							}
							leftWidth = uLeftWidth;
							if (leftWidth > 0)
								hasTableExtraSpaceColumn = true;
						} else {
							for (ColumnInfo ci : tmpMList)
								ci.width = ci.maxWidth;
							leftWidth = tLeftWidth;
							hasTableExtraSpaceColumn = true;
						}
						UniLog.log1("tLeftWidth:%f, uLeftWidth:%f", tLeftWidth, uLeftWidth);
					}
				}
				UniLog.log1("haha1 leftWidth:%f,leftColumnCount:%d,stringColumnCount:%d", leftWidth, leftColumnCount, stringColumnCount);
				if (lastLeftColumnCount == leftColumnCount)
					break;
				flag = true;
				lastLeftColumnCount = leftColumnCount;
			}
		}
		
		//When use choose to use pdf, please have some code to check the page width is feasible to accomodate all the selected column, otherwise , give a warning to user, ask them to choose fewer column
		/*if (outputFileType.equals("pdf") && leftColumnCount > 0 && avgWidth > 0) {
			for (ColumnInfo ci : columnList) {
				if (ci.width == 0 && ci.maxWidth / avgWidth > 5) { //check if there are more than 5 rows
					ZkUtil.warnMsg("The page width cannot accomodate all selected column, please choose fewer column");
					break;
				}
			}
		}*/

		UniLog.log1("totalMemory 2:%dMB", Runtime.getRuntime().totalMemory() / 1024 / 1024);
		if (StringUtils.equals(outputFileType, "csv"))
			createCsvDoc();
		else {
			try {
				createBirtDoc(sessionHelper);
			} catch (Exception e) {
				UniLog.log(e);
			}
		}
		System.gc();
		UniLog.log1("totalMemory 3:%dMB", Runtime.getRuntime().totalMemory() / 1024 / 1024);
	}
	private void createCsvDoc() {
		Writer writer = null;
		CSVPrinter printer = null;
		File tmpFile = new File("/tmp/birt/tmpcsv" + Thread.currentThread().getId() + "_" + System.currentTimeMillis() + "." + outputFileType);
		try {
			tmpFile.getParentFile().mkdirs();
			writer = new FileWriter(tmpFile);
			writer.write(new String(new byte[] {(byte)0xef, (byte)0xbb, (byte)0xbf}));
			
			String[] headerKeys = new String[columnList.size()];
			Object[] headerLabels = new String[columnList.size()];
			for (int i = 0; i < columnList.size(); i++) {
				ColumnInfo ci = columnList.get(i);
				headerKeys[i] = ci.key;
				StringBuilder sb = new StringBuilder();
				if (ci.aopHeaderNames != null) {
					for (int j = 1; j < ci.aopHeaderNames.length; j++) {
						String s = ci.aopHeaderNames[j];
						if (StringUtils.isNotBlank(s)) {
							if (sb.length() > 0)
								sb.append(":");
							sb.append(s);
						}
					}
					if (sb.length() > 0)
						sb.append(":");
				}
				sb.append(ci.name);
				if (ci.sortInfo != null)
					sb.append(String.format("%s%d", ci.sortInfo.sortDesc ? "\u25bc" : "\u25b2", ci.sortInfo.num));
				headerLabels[i] = sb.toString();
			}
			printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withHeader(headerKeys).withSkipHeaderRecord());
			printer.printRecord(headerLabels);
			
			
			Object[] detailValues = new Object[columnList.size()];
			SimpleDateFormat pdateFormat = new SimpleDateFormat("yyyy/MM/dd");
			SimpleDateFormat pdatetimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			SimpleDateFormat ptimeFormat = new SimpleDateFormat("HH:mm:ss");
			for (Map<String, Object> m : dsList) {
				for (int i = 0; i < columnList.size(); i++) {
					ColumnInfo ci = columnList.get(i);
					Object v = m.get(ci.key);
					if (ci.dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_INTEGER) || ci.dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_FLOAT)) {
						detailValues[i] = null;
						if (ci.dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_INTEGER) && this.itemCreateScriptMap.containsKey(ci.skey) && this.itemCreateScriptMap.get(ci.skey).contains("setHHmmDisplayValue(")) {
							int ii = (Integer)v;
							detailValues[i] = ii / 60 + ":" + ii % 60;
						} else {
							Double d;
							if (ci.dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_INTEGER))
								d = (double)(Integer)v;
							else
								d = (Double)v;
							if (ci.aggFunc != null) {
								if (d.isNaN())
									detailValues[i] = "-";
							} else if (aggregateOrPivotMap.containsKey(ci.skey) && (d.isInfinite() || d.isNaN()))
								detailValues[i] = "";
						}
						if (detailValues[i] == null)
							detailValues[i] = ci.format.format(v);
					} else if (ci.dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_DATE)) {
						Date date = dateFormat.parse((String)v);
						detailValues[i] = (date.compareTo(DateUtil.minDate) == 0) ? "" : pdateFormat.format(date);
					} else if (ci.dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_DATETIME)) {
						Date date = datetimeFormat.parse((String)v);
						detailValues[i] = (date.compareTo(DateUtil.minDate) == 0) ? "" : StringUtils.equals(ci.fieldColumnType, "time") ? ptimeFormat.format(date) : pdatetimeFormat.format(date);
					} else
						detailValues[i] = v.toString();
				}
				printer.printRecord(detailValues);
			}

			boolean hasFooter = false;
			for (int i = 0; i < columnList.size(); i++) {
				ColumnInfo ci = columnList.get(i);
				if (ci.aopHeaderNames != null 
					&& (ci.dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_INTEGER) || ci.dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_FLOAT))
					&& aggregateSubtotalMap.containsKey(ci.key)) {
					detailValues[i] = StringUtils.defaultString(aggregateSubtotalMap.get(ci.key));
					hasFooter = true;
				} else
					detailValues[i] = "";
			}
			if (hasFooter)
				printer.printRecord(detailValues);

			printer.flush();
			
			//export file
			if (StringUtils.isNotEmpty(password)) {
				FileInputStream fis = new FileInputStream(tmpFile);
				ByteArrayOutputStream fos = new ByteArrayOutputStream();
		       	ZipUtil.createZip(password, true, fos, fis, outFileName + "." + outputFileType);
		       	fos.close();
			    fis.close();
			    Filedownload.save(fos.toByteArray(), "application/zip", outFileName + ".zip");
			} else
			    Filedownload.save(FileUtils.readFileToByteArray(tmpFile), "application/" + outputFileType, outFileName + "." + outputFileType);
			ZkUtil.msg("Report generated successfully");
		} catch (Exception e) {
			UniLog.log(e);
		} finally {
			try {
				if (printer != null)
					printer.close();
				if (writer != null)
					writer.close();
				if (tmpFile.exists())
					tmpFile.delete();
			} catch (IOException e) {
				UniLog.log(e);
			}
		}

	}
	private void createBirtDoc(SessionHelper sessionHelper) throws Exception {
		synchronized(BIRTUtil.getObjLock()){
			final InputStream rptDesign = getClass().getResourceAsStream(designRes);

			UniLog.log1("start createBirtDoc");
			BIRTUtil.initEngine(sessionHelper);
			final IReportEngine engine = BIRTUtil.getReportEngine();

			//1. load design
			IReportRunnable design = engine.openReportDesign(rptDesign);
			rptDesign.close();

			//2. init rptdesign
			ReportDesignHandle designHandle = (ReportDesignHandle) design.getDesignHandle();
			ElementFactory ef = design.getDesignHandle().getElementFactory();

			//3. add User Property in rptdesign, and then set its value; setup mater page
			SimpleMasterPageHandle materPageHandle = (SimpleMasterPageHandle)designHandle.findMasterPage("Simple MasterPage");
			materPageHandle.setProperty(IMasterPageModel.TYPE_PROP, rptPaperSize);
			materPageHandle.setProperty(IMasterPageModel.ORIENTATION_PROP, rptOrientation);
			/*designHandle.setProperty("Title", title);
			if (StringUtils.isNotBlank(subTitle))
				designHandle.setProperty("SubTitle", subTitle);
			else
				materPageHandle.setProperty("headerHeight", "25pt");*/
			ModuleHandle mh = designHandle.getModuleHandle();
			if (!isCustomReport) {
				DesignElementHandle dehTitle = mh.findElement("HeaderTitle");
				DesignElementHandle dehSubTitle = mh.findElement("HeaderSubTitle");
				dehTitle.setProperty(IStyleModel.TEXT_ALIGN_PROP, "left");
				dehSubTitle.setProperty(IStyleModel.TEXT_ALIGN_PROP, "left");
				dehSubTitle.setProperty(IStyleModel.FONT_WEIGHT_PROP, "normal");
				dehSubTitle.setProperty(IStyleModel.FONT_SIZE_PROP, "x-small");
				dehSubTitle.setProperty(IStyleModel.COLOR_PROP, "#808080");
				designHandle.setProperty("Title", title + (StringUtils.isNotBlank(subTitle) ? " - " + subTitle : ""));
				if (isShowQueryConditions && StringUtils.isNotBlank(queryConditions))
					designHandle.setProperty("SubTitle", queryConditions);
				else
					materPageHandle.setProperty("headerHeight", "25pt");
			}
			designHandle.setProperty("TotalRecords", dsList.size());

			//resize footer
			if (!isCustomReport) {
				float footerHeight = fontSize * 4 / 3 + 2;
				materPageHandle.setProperty(SimpleMasterPageHandle.FOOTER_HEIGHT_PROP, footerHeight + "pt");
				GridHandle gridFooter = (GridHandle)materPageHandle.getPageFooter().get(0);
				gridFooter.setProperty(IStyleModel.FONT_SIZE_PROP, fontSize + "pt");
				RowHandle rowHandle = (RowHandle)gridFooter.getRows().get(0);
				rowHandle.setProperty(IStyleModel.HEIGHT_PROP, footerHeight + "pt");
				CellHandle cellHandle = (CellHandle)rowHandle.getCells().get(1);
				GridHandle gridFooRight = (GridHandle)cellHandle.getContent().get(0);
				ColumnHandle chPageLabel = (ColumnHandle)gridFooRight.getColumns().get(1);
				ColumnHandle chPageStart = (ColumnHandle)gridFooRight.getColumns().get(2);
				ColumnHandle chPageOf = (ColumnHandle)gridFooRight.getColumns().get(3);
				ColumnHandle chPageEnd = (ColumnHandle)gridFooRight.getColumns().get(4);
				float wFooDate = getTextWidthPoint("99/99/9999 99:99:99999") + 4;
				float wFooPageLabel = getTextWidthPoint("Page") + 4;
				float wFooPageNum = getTextWidthPoint("9999") + 4;
				float wFooPageOf = getTextWidthPoint("of") + 4;
				chPageLabel.setProperty(IStyleModel.WIDTH_PROP, wFooPageLabel + "pt");
				chPageStart.setProperty(IStyleModel.WIDTH_PROP, wFooPageNum + "pt");
				chPageOf.setProperty(IStyleModel.WIDTH_PROP, wFooPageOf + "pt");
				chPageEnd.setProperty(IStyleModel.WIDTH_PROP, wFooPageNum + "pt");
				gridFooRight.setWidth((wFooDate + wFooPageLabel + wFooPageNum * 2 + wFooPageOf) + "pt");
			}

			if (callback != null)
				callback.beforeAddUserProp(this);
			
			for (Map.Entry<String, Object> entry : userPropMap.entrySet()) {
				String name = entry.getKey();
				Object value = entry.getValue();
				int propType = PropertyType.STRING_TYPE;
				if (value instanceof Date)
					propType = PropertyType.DATE_TIME_TYPE;
				else if (value instanceof Double || value instanceof Float)
					propType = PropertyType.FLOAT_TYPE;
				else if (value instanceof Integer)
					propType = PropertyType.INTEGER_TYPE;
				UserPropertyDefn prop = new UserPropertyDefn();
				prop.setName(name);
				prop.setType(MetaDataDictionary.getInstance().getPropertyType(propType));
				designHandle.addUserPropertyDefn(prop);
				designHandle.setProperty(name, value);
			}
			for (Map.Entry<String, Double> entry : aggregateSubtotalValueMap.entrySet()) {
				String name = "subtotal." + entry.getKey();
				UserPropertyDefn prop = new UserPropertyDefn();
				prop.setName(name);
				prop.setType(MetaDataDictionary.getInstance().getPropertyType(PropertyType.FLOAT_TYPE));
				designHandle.addUserPropertyDefn(prop);
				designHandle.setProperty(name, entry.getValue());
			}

			//4. setup dataset
			final String dataSetName = "Data Set 1";
			DataSetHandle dataSet = (ScriptDataSetHandle) designHandle.findDataSet(dataSetName);
			PropertyHandle resultSet = dataSet.getPropertyHandle(DataSetHandle.RESULT_SET_PROP);
			for (ColumnInfo ci : columnList) {
				UniLog.log1("columnList ci key:%s, dataType:%s", ci.key, ci.dataType);
				ResultSetColumn column = StructureFactory.createResultSetColumn();
				column.setColumnName(ci.key);
				column.setDataType(ci.dataType); 
				resultSet.addItem(column);
			}
			ResultSetColumn cl = StructureFactory.createResultSetColumn();
			cl.setColumnName("isDetailRow");
			cl.setDataType("boolean"); 
			resultSet.addItem(cl);

			//5. create table
			TableHandle table = ef.newTableItem("Table"/*"Table 1"*/, columnList.size() + (hasTableExtraSpaceColumn ? 1 : 0), designHeaderCount, 1, hasDesignFooter ? 1 : 0);
			designHandle.getBody().add(table);
			table.setWidth("100%");
			table.setDataSet(dataSet);
			//setup page break interval, birt default is 40
			table.setProperty(IListingElementModel.PAGE_BREAK_INTERVAL_PROP, 0);
			
			RowHandle headerRow = (RowHandle)table.getHeader().getContents().get(designHeaderCount - 1);
			RowHandle detailRow = (RowHandle)table.getDetail().getContents().get(0);
			SlotHandle headerCells = headerRow.getCells();
			SlotHandle detailCells = detailRow.getCells();
			SlotHandle footerCells = null;
			SlotHandle[] aopExtraHeaderCellss = new SlotHandle[designHeaderCount - 1];
			int[] aopExtraHeaderCellsCounts = new int[aopExtraHeaderCellss.length]; //array of aop extra header cell count
			for (int i = 0; i < designHeaderCount - 1; i++) {
				headerRow = (RowHandle)table.getHeader().getContents().get(i);
				aopExtraHeaderCellss[i] = headerRow.getCells();
			}
			if (hasDesignFooter) {
				RowHandle footerRow = (RowHandle)table.getFooter().getContents().get(0);
				footerCells = footerRow.getCells();
			}
			for (int i = 0; i < columnList.size(); i++) {
				ColumnInfo ci = columnList.get(i);
				String dataType;
				//add column binding
				ComputedColumn computedColumn = StructureFactory.createComputedColumn();
				computedColumn.setName(ci.key);
				ResultSetColumn resultSetColumn = (ResultSetColumn) resultSet.getItems().get(i);
				dataType = resultSetColumn.getDataType();
				computedColumn.setExpression("dataSetRow[\""+resultSetColumn.getColumnName()+"\"]");
				computedColumn.setDataType(dataType);
				table.addColumnBinding(computedColumn, false);
				
				if (ci.aopHeaderNames != null && (dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_INTEGER) || dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_FLOAT))) {
					//add column binding for sum
					computedColumn = StructureFactory.createComputedColumn();
					computedColumn.setName(ci.key + ".sum");
					resultSetColumn = (ResultSetColumn) resultSet.getItems().get(i);
					dataType = resultSetColumn.getDataType();
					if (aggregateSubtotalValueMap.containsKey(ci.key))
						computedColumn.setExpression(String.format("getDesignProperty(\"subtotal.%s\")", ci.key));
					else
						computedColumn.setExpression(String.format("Total.sum(row[\"%s\"], isFinite(row[\"%s\"]))", resultSetColumn.getColumnName(), resultSetColumn.getColumnName()));
					computedColumn.setDataType(dataType);
					table.addColumnBinding(computedColumn, false);
				}

				//setup column style
				ColumnHandle column = (ColumnHandle) table.getColumns().get(i);
				if (ci.width > 0)
					column.setProperty(IStyleModel.WIDTH_PROP, ci.width + "pt");
				if (ci.aggFunc != null || dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_INTEGER) || dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_FLOAT))
					column.setProperty(IStyleModel.TEXT_ALIGN_PROP, DesignChoiceConstants.TEXT_ALIGN_RIGHT);
				else
					column.setProperty(IStyleModel.TEXT_ALIGN_PROP, DesignChoiceConstants.TEXT_ALIGN_LEFT);
				column.setProperty(IStyleModel.FONT_SIZE_PROP, fontSize + "pt");

				//setup common header
				CellHandle cell = (CellHandle) headerCells.get(i);
				if (i > 0) {
					cell.setProperty(IStyleModel.BORDER_LEFT_WIDTH_PROP, "0.5pt");
					cell.setProperty(IStyleModel.BORDER_LEFT_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
				}
				cell.setProperty(IStyleModel.BORDER_TOP_WIDTH_PROP, "0.5pt");
				cell.setProperty(IStyleModel.BORDER_TOP_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
				cell.setProperty(IStyleModel.BORDER_BOTTOM_WIDTH_PROP, "0.5pt");
				cell.setProperty(IStyleModel.BORDER_BOTTOM_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
				cell.setProperty(IStyleModel.TEXT_ALIGN_PROP, DesignChoiceConstants.TEXT_ALIGN_CENTER);

				TextItemHandle text = ef.newTextItem(null);
				text.setContentType(DesignChoiceConstants.TEXT_DATA_CONTENT_TYPE_HTML);
				StringBuilder sbText = new StringBuilder();
				if (ci.isAggColumn)
					sbText.append(String.format("<u>%s</u>", ci.name));
				else
					sbText.append(ci.name);
				if (ci.sortInfo != null)
					sbText.append(String.format("<span style='font-size:%dpt'>%s%d</span>", (int)Math.min(fontSize, sortingTextFontSize), ci.sortInfo.sortDesc ? "\u25bc" : "\u25b2", ci.sortInfo.num));
				text.setContent(sbText.toString());
				cell.getContent().add(text);
				
				//setup aop extra header
				for (int j = 0; j < aopExtraHeaderCellss.length; j++) {
					int index = aopExtraHeaderCellsCounts[j];
					String curHdr = "";
					String lastHdr = "";
					if (ci.aopHeaderNames != null)
						curHdr = StringUtils.defaultString(ci.aopHeaderNames[j + 1]);
					ColumnInfo lastCi = null;
					if (i > 0) {
						lastCi = columnList.get(i - 1);
						if (lastCi.aopHeaderNames != null)
							lastHdr = StringUtils.defaultString(lastCi.aopHeaderNames[j + 1]);
					}
					if (i == 0 || !curHdr.equals(lastHdr) || (ci.isAggColumn != lastCi.isAggColumn) || ((ci.aggFunc != null) != (lastCi.aggFunc != null))) {
						cell = (CellHandle) aopExtraHeaderCellss[j].get(index);
						cell.setProperty(IStyleModel.BORDER_TOP_WIDTH_PROP, "0.5pt");
						cell.setProperty(IStyleModel.BORDER_TOP_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
						cell.setProperty(IStyleModel.BORDER_BOTTOM_WIDTH_PROP, "0.5pt");
						cell.setProperty(IStyleModel.BORDER_BOTTOM_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
						if (i > 0) {
							cell.setProperty(IStyleModel.BORDER_LEFT_WIDTH_PROP, "0.5pt");
							cell.setProperty(IStyleModel.BORDER_LEFT_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
						}
						cell.setProperty(IStyleModel.TEXT_ALIGN_PROP, DesignChoiceConstants.TEXT_ALIGN_CENTER);
						text = ef.newTextItem(null);
						text.setContentType(DesignChoiceConstants.TEXT_DATA_CONTENT_TYPE_HTML);
						text.setContent(curHdr);
						cell.getContent().add(text);
						aopExtraHeaderCellsCounts[j] = index + 1;
					}
					else {
						CellHandle lastCell = (CellHandle) aopExtraHeaderCellss[j].get(index - 1);
						lastCell.setColumnSpan(lastCell.getColumnSpan() + 1);
					}
				}

				//setup detail
				cell = (CellHandle) detailCells.get(i);
				DataItemHandle dataItem = ef.newDataItem(ci.key + "_" + i);
				dataItem.setResultSetColumn(ci.key);
				dataItem.setProperty(IStyleModel.WHITE_SPACE_PROP, DesignChoiceConstants.WHITE_SPACE_NORMAL);
				if (dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_INTEGER) || dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_FLOAT)) {
					//dataItem.setOnCreate(String.format("setFormatDisplayValue(this, \"%s\")", ci.format.toPattern()));
					if (itemCreateScriptMap.containsKey(ci.skey))
						dataItem.setOnCreate(itemCreateScriptMap.get(ci.skey));
					else {
						MetaDataDictionary dd = MetaDataDictionary.getInstance();
						PropertyType strType = dd.getPropertyType(PropertyType.STRING_TYPE);
						UserPropertyDefn newPropDefn = new UserPropertyDefn();
						newPropDefn.setName(ExcelEmitter.CUSTOM_NUMBER_FORMAT);
						newPropDefn.setType(strType);
						dataItem.addUserPropertyDefn(newPropDefn);
						dataItem.setProperty(ExcelEmitter.CUSTOM_NUMBER_FORMAT, ci.format.toPattern());
	
						NumberFormatValue nfv = new NumberFormatValue();
						nfv.setPattern(ci.format.toPattern() + "{RoundingMode=HALF_UP}");
						nfv.setCategory("Fixed");
						UniLog.log1("key:%s, pattern:%s", ci.key, nfv.getPattern());
						dataItem.setProperty(IStyleModel.NUMBER_FORMAT_PROP, nfv);
					}
				}
				else if (dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_DATE)) {
					//dataItem.setOnCreate("setDefaultDateDisplayValue(this);");
					dataItem.setOnCreate(String.format("if (Formatter.format(this.getValue(), '%s') == '%s') this.setDisplayValue('');", dateFormat.toPattern(), dateFormat.format(DateUtil.minDate)));
					MetaDataDictionary dd = MetaDataDictionary.getInstance();
					PropertyType strType = dd.getPropertyType(PropertyType.STRING_TYPE);
					UserPropertyDefn newPropDefn = new UserPropertyDefn();
					newPropDefn.setName(ExcelEmitter.CUSTOM_NUMBER_FORMAT);
					newPropDefn.setType(strType);
					dataItem.addUserPropertyDefn(newPropDefn);
					dataItem.setProperty(ExcelEmitter.CUSTOM_NUMBER_FORMAT, "yyyy/mm/dd");

					DateTimeFormatValue dfv = new DateTimeFormatValue();
					dfv.setPattern("yyyy/MM/dd");
					dfv.setCategory("Custom");
					UniLog.log1("key:%s, pattern:%s", ci.key, dfv.getPattern());
					dataItem.setProperty(IStyleModel.DATE_TIME_FORMAT_PROP, dfv);
				}
				else if (dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_DATETIME)) {
					dataItem.setOnCreate(String.format("if (Formatter.format(this.getValue(), '%s') == '%s') this.setDisplayValue('');", datetimeFormat.toPattern(), datetimeFormat.format(DateUtil.minDate)));
					MetaDataDictionary dd = MetaDataDictionary.getInstance();
					PropertyType strType = dd.getPropertyType(PropertyType.STRING_TYPE);
					UserPropertyDefn newPropDefn = new UserPropertyDefn();
					newPropDefn.setName(ExcelEmitter.CUSTOM_NUMBER_FORMAT);
					newPropDefn.setType(strType);
					dataItem.addUserPropertyDefn(newPropDefn);
					dataItem.setProperty(ExcelEmitter.CUSTOM_NUMBER_FORMAT, StringUtils.equals(ci.fieldColumnType, "time") ? "hh:mm:ss" : "yyyy/mm/dd hh:mm:ss");

					DateTimeFormatValue dfv = new DateTimeFormatValue();
					dfv.setPattern(StringUtils.equals(ci.fieldColumnType, "time") ? "HH:mm:ss" : "yyyy/MM/dd HH:mm:ss");
					dfv.setCategory("Custom");
					UniLog.log1("key:%s, pattern:%s", ci.key, dfv.getPattern());
					dataItem.setProperty(IStyleModel.DATE_TIME_FORMAT_PROP, dfv);
				}
				if (ci.aggFunc != null) {
					if (itemCreateScriptMap.containsKey(ci.skey))
						dataItem.setOnCreate("if (this.getValue().toString() == 'NaN') this.setDisplayValue('-'); else { " + itemCreateScriptMap.get(ci.skey) + " }");
					else
						dataItem.setOnCreate("if (this.getValue().toString() == 'NaN') this.setDisplayValue('-');");
					//highlight
					HighlightRule hr = StructureFactory.createHighlightRule();
					hr.setOperator(DesignChoiceConstants.MAP_OPERATOR_NE);
					hr.setProperty(HighlightRule.FONT_WEIGHT_MEMBER, DesignChoiceConstants.FONT_WEIGHT_BOLD);
					hr.setTestExpression("row[\""+ci.key+"\"]");
					//hr.setValue1("'-'");
					hr.setValue1("NaN");
					dataItem.getPropertyHandle(IStyleModel.HIGHLIGHT_RULES_PROP).addItem(hr);
				}
				else {
					if (aggregateOrPivotMap.containsKey(ci.skey) && (dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_INTEGER) || dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_FLOAT))) {
						if (itemCreateScriptMap.containsKey(ci.skey))
							dataItem.setOnCreate("if (!isFinite(this.getValue())) this.setDisplayValue(null); else { " + itemCreateScriptMap.get(ci.skey) + " }");
						else
							dataItem.setOnCreate("if (!isFinite(this.getValue())) this.setDisplayValue(null);");
					}
				}
				cell.getContent().add(dataItem);
				
				//setup footer
				if (footerCells != null) {
					cell = (CellHandle) footerCells.get(i);
					if (ci.aopHeaderNames != null) {
						if (i > 0) {
							cell.setProperty(IStyleModel.BORDER_LEFT_WIDTH_PROP, "0.5pt");
							cell.setProperty(IStyleModel.BORDER_LEFT_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
						}
						/*text = ef.newTextItem(null);
						text.setContentType(DesignChoiceConstants.TEXT_DATA_CONTENT_TYPE_PLAIN);
						text.setContent(StringUtils.defaultString(aggregateSubtotalMap.get(ci.skey)));
						cell.getContent().add(text);*/
						if ((dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_INTEGER) || dataType.equals(DesignChoiceConstants.COLUMN_DATA_TYPE_FLOAT))
								&& aggregateSubtotalMap.containsKey(ci.key)) {
							dataItem = ef.newDataItem(null);
							dataItem.setResultSetColumn(ci.key + ".sum");
							if (itemCreateScriptMap.containsKey(ci.skey))
								dataItem.setOnCreate(itemCreateScriptMap.get(ci.skey));
							else {
								MetaDataDictionary dd = MetaDataDictionary.getInstance();
								PropertyType strType = dd.getPropertyType(PropertyType.STRING_TYPE);
								UserPropertyDefn newPropDefn = new UserPropertyDefn();
								newPropDefn.setName(ExcelEmitter.CUSTOM_NUMBER_FORMAT);
								newPropDefn.setType(strType);
								dataItem.addUserPropertyDefn(newPropDefn);
								dataItem.setProperty(ExcelEmitter.CUSTOM_NUMBER_FORMAT, ci.format.toPattern());
	
								NumberFormatValue nfv = new NumberFormatValue();
								nfv.setPattern(ci.format.toPattern() + "{RoundingMode=HALF_UP}");
								nfv.setCategory("Fixed");
								dataItem.setProperty(IStyleModel.NUMBER_FORMAT_PROP, nfv);
							}
							cell.getContent().add(dataItem);
						}
						else {
							text = ef.newTextItem(null);
							text.setContentType(DesignChoiceConstants.TEXT_DATA_CONTENT_TYPE_PLAIN);
							text.setContent("");
							cell.getContent().add(text);
						}
					}
					cell.setProperty(IStyleModel.BORDER_TOP_WIDTH_PROP, "0.5pt");
					cell.setProperty(IStyleModel.BORDER_TOP_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
					cell.setProperty(IStyleModel.BORDER_BOTTOM_WIDTH_PROP, "0.5pt");
					cell.setProperty(IStyleModel.BORDER_BOTTOM_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
				}
				
			}
			ComputedColumn cc = StructureFactory.createComputedColumn();
			cc.setName("isDetailRow");
			cc.setExpression("dataSetRow[\"isDetailRow\"]");
			cc.setDataType("boolean");
			table.addColumnBinding(cc, false);

			if (hasTableExtraSpaceColumn) {
				CellHandle cell = (CellHandle) headerCells.get(columnList.size());
				cell.setProperty(IStyleModel.BORDER_LEFT_WIDTH_PROP, "0.5pt");
				cell.setProperty(IStyleModel.BORDER_LEFT_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
				cell.setProperty(IStyleModel.BORDER_TOP_WIDTH_PROP, "0.5pt");
				cell.setProperty(IStyleModel.BORDER_TOP_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
				cell.setProperty(IStyleModel.BORDER_BOTTOM_WIDTH_PROP, "0.5pt");
				cell.setProperty(IStyleModel.BORDER_BOTTOM_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
				cell.setProperty(IStyleModel.TEXT_ALIGN_PROP, DesignChoiceConstants.TEXT_ALIGN_CENTER);
				for (int j = 0; j < aopExtraHeaderCellss.length; j++) {
					cell = (CellHandle) aopExtraHeaderCellss[j].get(aopExtraHeaderCellsCounts[j]);
					cell.setProperty(IStyleModel.BORDER_LEFT_WIDTH_PROP, "0.5pt");
					cell.setProperty(IStyleModel.BORDER_LEFT_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
					cell.setProperty(IStyleModel.BORDER_TOP_WIDTH_PROP, "0.5pt");
					cell.setProperty(IStyleModel.BORDER_TOP_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
					cell.setProperty(IStyleModel.BORDER_BOTTOM_WIDTH_PROP, "0.5pt");
					cell.setProperty(IStyleModel.BORDER_BOTTOM_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
					while ((cell = (CellHandle) aopExtraHeaderCellss[j].get(aopExtraHeaderCellsCounts[j] + 1)) != null) //drop all no use cell
						cell.dropAndClear();
				}
				if (footerCells != null) {
					cell = (CellHandle) footerCells.get(columnList.size());
					cell.setProperty(IStyleModel.BORDER_LEFT_WIDTH_PROP, "0.5pt");
					cell.setProperty(IStyleModel.BORDER_LEFT_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
					cell.setProperty(IStyleModel.BORDER_TOP_WIDTH_PROP, "0.5pt");
					cell.setProperty(IStyleModel.BORDER_TOP_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
					cell.setProperty(IStyleModel.BORDER_BOTTOM_WIDTH_PROP, "0.5pt");
					cell.setProperty(IStyleModel.BORDER_BOTTOM_STYLE_PROP, DesignChoiceConstants.LINE_STYLE_SOLID);
					cell.setProperty(IStyleModel.TEXT_ALIGN_PROP, DesignChoiceConstants.TEXT_ALIGN_CENTER);
				}
			}
			else {
				for (int j = 0; j < aopExtraHeaderCellss.length; j++) {
					CellHandle cell;
					while ((cell = (CellHandle) aopExtraHeaderCellss[j].get(aopExtraHeaderCellsCounts[j])) != null) //drop all no use cell
						cell.dropAndClear();
				}
			}
			//create detail row HighlightRule 
			if (hasAlternateRowColor) {
				HighlightRule hr = StructureFactory.createHighlightRule();
				hr.setOperator(DesignChoiceConstants.MAP_OPERATOR_EQ);
				hr.setProperty(HighlightRule.BACKGROUND_COLOR_MEMBER, "#D3D3D3");
				hr.setTestExpression("row.__rownum % 2");
				hr.setValue1("0");
				detailRow.getPropertyHandle(IStyleModel.HIGHLIGHT_RULES_PROP).addItem(hr);
			}

			//6. pass dataset to rptdesign dataset
			ScriptedDataSetEventHander.clearRecordList();
			ScriptedDataSetEventHander.addRecordList(dataSetName, dsList);
			
			//7. create and run task
			String tmpPath = "/tmp/birt/tmppdf" + Thread.currentThread().getId() + "_" + System.currentTimeMillis() + "." + outputFileType;
			File tmpFile = new File(tmpPath);
			tmpFile.getParentFile().mkdirs();
			UniLog.log1("outputPath:%s, dsList size:%d", tmpPath, dsList.size());
			RenderOption renderOption;
			if (StringUtils.endsWithAny(tmpPath, "xls")){
				renderOption = new EXCELRenderOption();
				renderOption.setOutputFormat("xls");
				if (isExcelAutoFilter)
					renderOption.setOption(ExcelEmitter.AUTO_FILTER, true);
				if (isExcelSingleSheet)
					renderOption.setOption(ExcelEmitter.SINGLE_SHEET, true);
			}
			else if (StringUtils.endsWithAny(tmpPath, "xlsx")){ //REMARK: does not work, probably due to lib version??
				renderOption = new EXCELRenderOption();
				renderOption.setOutputFormat("xlsx");
				//renderOption.setOption( IExcelRenderOption.OFFICE_VERSION, "office2007");
				if (isExcelAutoFilter)
					renderOption.setOption(ExcelEmitter.AUTO_FILTER, true);
				if (isExcelSingleSheet)
					renderOption.setOption(ExcelEmitter.SINGLE_SHEET, true);
			}
			else if (StringUtils.endsWithAny(tmpPath, "docx")){ 
				renderOption = new DocxRenderOption();
				renderOption.setOutputFormat("docx");
			}
			else if (StringUtils.endsWithAny(tmpPath, "html")){ //REMARK:
				renderOption = new HTMLRenderOption();
				renderOption.setOutputFormat("html");
			}
			else{
				renderOption = new PDFRenderOption();
				renderOption.setOutputFormat("pdf");
				renderOption.setOption(PDFRenderOption.PDF_HYPHENATION, true);
			}
			Locale locale = StringUtils.equals(sessionHelper.getLHLang(), "SCHN") ? Locale.SIMPLIFIED_CHINESE : Locale.TRADITIONAL_CHINESE;
			renderOption.setOutputFileName(tmpPath);
			renderOption.setOption(IPDFRenderOption.LOCALE, locale);
			BIRTUtil.createRunAndRenderTask(design, renderOption, locale);
			
			//8. export file
			if (StringUtils.isNotEmpty(password)) {
				FileInputStream fis = new FileInputStream(tmpFile);
				ByteArrayOutputStream fos = new ByteArrayOutputStream();
		       	ZipUtil.createZip(password, true, fos, fis, outFileName + "." + outputFileType);
		       	fos.close();
			    fis.close();
			    Filedownload.save(fos.toByteArray(), "application/zip", outFileName + ".zip");
			}
			else
			    Filedownload.save(FileUtils.readFileToByteArray(tmpFile), "application/" + outputFileType, outFileName + "." + outputFileType);
			tmpFile.delete();
			ZkUtil.msg("Report generated successfully");
			
			UniLog.log1("done");
		}
	}
	
	private Pair<Float, Float> getPaperSizeIn() {
		int h = 210;
		int w = 297;
		if (rptPaperSize.equals("a3")) {
			h = 297;
			w = 420;
		} else if (rptPaperSize.equals("a5")) {
			h = 148;
			w = 210;
		} else if (rptPaperSize.equals("us-letter")) {
			h = 216;
			w = 279;
		}
		return new Pair<Float, Float>(h / 25.4f, w / 25.4f);
	}
	private float getTextWidthPoint(String text, float maxPt) {
		if (text != null) {
			TextSpliter ts = ChnftrParser.getTextSpliter(text, "helv_nr", "chinese", fontSize, (int)Math.ceil(maxPt / 72 * ChnftrParser.CHNFTR_DPI));
			return ts.getWidthPoint();
		}
		else
			return 0;
	}
	private float getTextWidthPoint(String text) {
		return getTextWidthPoint(text, innerTableWidth);
	}
	private String similarSortingText;
	private String getSimilarSortingText() {
		if (similarSortingText != null)
			return similarSortingText;
		String oSortingText = '\u25bc' + "9";
		if (fontSize <= sortingTextFontSize)
			return similarSortingText = oSortingText;
		TextSpliter ts = ChnftrParser.getTextSpliter(oSortingText, "helv_nr", "chinese", sortingTextFontSize, (int)Math.ceil(innerTableWidth / 72 * ChnftrParser.CHNFTR_DPI));
		float minWidth = ts.getWidthPoint();
		float curWidth;
		String s = "";
		do {
			s += "l";
		} while ((curWidth = getTextWidthPoint(s)) < minWidth);
		UniLog.log1("getSimilarSortingText %s,%f,%f", s, minWidth, curWidth);
		return similarSortingText = s;
	}
	private float getSimilarTextWidth(int textCharCount) {
		String s = "";
		for (int i = 0; i < textCharCount; i++)
			s += "A";
		return getTextWidthPoint(s);
	}
	
	private static boolean isNumericCellType(int type) {
		return type == Cell.VTYPE_INT || type == Cell.VTYPE_DOUBLE;
	}
	
	public static String generateOutputFileName(String title) {
   	    final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return String.format("%s_%s", title.toLowerCase().replaceAll("\\s", ""), sdf.format(new Date()))
								.replaceAll("[^\\u4E00-\\u9FA5a-zA-Z0-9\\-_]", "");
	}

	private static String getBiColumnTranName(SessionHelper sessionHelper, BiColumn bc) {
		return ZkBiTranslateHelper.getText(sessionHelper, bc.getCellFullName(), "LABEL", sessionHelper.getLabel(bc));
	}
	
	private static ColumnInfo getAopIdx(Map<String, List<Pair<Integer, String>>> aggregateOrPivotMap, List<Integer> aopIdxList, int columnLabelIdx, String label) {
		ColumnInfo ci = new ColumnInfo();
		if (columnLabelIdx >= 0 && columnLabelIdx < aopIdxList.size()) {
			int apIdx = aopIdxList.get(columnLabelIdx);
			List<Pair<Integer, String>> list = aggregateOrPivotMap.get(label);
			for (Pair<Integer, String> p : list) {
				if (p.x == apIdx) {
					ci.aopIdx = p.x;
					if (StringUtils.isNotBlank(p.y))
						ci.key = p.y;
					break;
				}
			}
			if (ci.aopIdx == -1)
				ci.aopIdx = list.get(0).x;
		}
		return ci;
	}
	
	public Map<String, Double> getAggregateSubtotalValueMap() {
		return aggregateSubtotalValueMap;
	}
	
	public static void showDialog(final Component comp, final SessionHelper sessionHelper, final String viewId, final BiResult result, final String viewHeader,
									final String title, final String queryConditions, final String preset, final ConditionPresets conditionPresets, final String designRes, 
									final List<String> p_columnLabelList, final Map<Integer, MultiSortInfo> sortColumnMap, final Map<String, String[]> aopHeaderMap,
									final Map<String, String> itemCreateScriptMap, final Map<String, Object> userPropMap, final Map<String, Object> settingMap,
									final ListModelList listModelList, final List<Integer> aopIdxList, final Callback callback) {
    	final ConditionFieldMap conditionFieldMap = conditionPresets.getFieldMap(preset);
    	final String subTitle = (preset != null && conditionFieldMap != null) ? ConditionPresets.buildPresetLabelByKey(preset, conditionFieldMap.getAccessKeyForPublic()) : null;
   		JSONObject gReportSettingData = null;
    	if (conditionFieldMap != null) {
    		gReportSettingData = conditionFieldMap.getGeneralReportSettingData();
    		if (gReportSettingData == null)
    			gReportSettingData = new JSONObject();
    	}
		final JSONObject genRptSettingDatas = gReportSettingData;
		final Map<String, Button> btnMap = new HashMap<String, Button>();
//		final List<String> aggregateOrPivotList = result.getAggregateOrPivotList();
		List<String> aggregateOrPivotList;
		if(result.getAggregateOrPivotHeader() != null) {
			aggregateOrPivotList = result.getAggregateOrPivotHeader().getAggregateOrPivotList();
		} else {
			aggregateOrPivotList = result.getAggregateOrPivotList();
		}
		if (aggregateOrPivotList == null)
			aggregateOrPivotList = new ArrayList<String>();
		final Map<String, List<Pair<Integer, String>>> aggregateOrPivotMap = new LinkedHashMap<String, List<Pair<Integer, String>>>();
		for (int i = 0; i < aggregateOrPivotList.size(); i++) {
			String s = aggregateOrPivotList.get(i);
			if (aggregateOrPivotMap.containsKey(s)) {
				List<Pair<Integer, String>> list = aggregateOrPivotMap.get(s);
				list.add(new Pair<Integer, String>(i, s));
			} else
				aggregateOrPivotMap.put(s, Lists.newArrayList(new Pair<Integer, String>(i, s)));
		}
		Set<String> chkList = new HashSet<String>(aggregateOrPivotMap.keySet());
		for (List<Pair<Integer, String>> list : aggregateOrPivotMap.values()) {
			int j = 0;
			Pair<Integer, String> p0 = list.get(0);
			for (int i = 1; i < list.size(); i++) {
				Pair<Integer, String> p = list.get(i);
				String s = p0.y;
				do {
					s = p0.y + "_" + ++j;
				} while (chkList.contains(s));
				list.set(i, new Pair<Integer, String>(p.x, s));
				chkList.add(s);
			}
		}

		final Map<String, AggregateOrPivot.AGGREGATES> aggregateTypeMap = new HashMap<String, AggregateOrPivot.AGGREGATES>();
		final Map<String, Integer> aggregateVTypeMap = new HashMap<String, Integer>();
		final Map<String, List<StrAndNum>> agColumnLabelMap = new HashMap<String, List<StrAndNum>>();
		final Map<String, String> agrColumnLabelMap = new HashMap<String, String>();
		try {
			JSONObject jobj = result.getAnalysedData(null);
			if (jobj != null) {
				JSONArray jarr = jobj.optJSONArray("Aggregates");
				for (int i = 0; jarr != null && i < jarr.length(); i++) {
					JSONObject jo = jarr.getJSONObject(i);
	    			String aggregate = jo.getString("aggregate");
	    			String name = jo.getString("name");
	    			String key = jo.getString("key");
	    			aggregateTypeMap.put(name, AggregateOrPivot.AGGREGATES.valueOf(aggregate));
				}
				jarr = jobj.optJSONArray("ColHeaders");
				List<String> colHeaders = new ArrayList<String>();
				String aopColHeaderStr = "";
				for (int i = 0; jarr != null && i < jarr.length(); i++) {
					String str = jarr.getString(i);
					if (!aopColHeaderStr.isEmpty())
						aopColHeaderStr += ",";
					colHeaders.add(str);
					aopColHeaderStr += str;
				}
				jarr = jobj.optJSONArray("Columns");
				String[] aggregateTypeKeys = aggregateTypeMap.keySet().toArray(new String[0]);
				for (String name : aggregateTypeKeys) {
					String key = aopColHeaderStr + ":" + name;
					aggregateTypeMap.put(key, aggregateTypeMap.get(name));
					agColumnLabelMap.put(key, new ArrayList<StrAndNum>());
				}
				for (int i = 0; jarr != null && i < jarr.length(); i++) {
					JSONObject jo = jarr.getJSONObject(i);
					StringBuilder sb = new StringBuilder();
					for (String colHeader : colHeaders) {
						if (sb.length() > 0)
							sb.append(",");
						if (jo.has(colHeader))
							sb.append(jo.optString(colHeader));
					}
					for (String name : aggregateTypeKeys) {
						String key = sb.toString() + ":" + name;
						if (!aopHeaderMap.containsKey(key) && aggregateTypeKeys.length == 1)
							key = sb.toString();
						aggregateTypeMap.put(key, aggregateTypeMap.get(name));
						agColumnLabelMap.get(aopColHeaderStr + ":" + name).add(new StrAndNum(key, -1));
						agrColumnLabelMap.put(key, aopColHeaderStr + ":" + name);
					}
				}
				UniLog.log1("aopHeaderMap:%s", aopHeaderMap);
				UniLog.log1("aopColHeaderStr:%s", aopColHeaderStr);
				UniLog.log1("aggregateTypeMap:%s", aggregateTypeMap);
				UniLog.log1("agColumnLabelMap:%s", agColumnLabelMap);
				UniLog.log1("agrColumnLabelMap:%s", agrColumnLabelMap);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		final List<StrAndNum> columnLabelList = new ArrayList<StrAndNum>();
		for (int i = 0; i < p_columnLabelList.size(); i++)
			columnLabelList.add(new StrAndNum(p_columnLabelList.get(i), i));
		final List<StrAndNum> oldColumnLabelList = new ArrayList<StrAndNum>(columnLabelList);
		for (StrAndNum sn : columnLabelList.toArray(new StrAndNum[0])) {
			if (agrColumnLabelMap.containsKey(sn.str) /*&& !columnLabelList.contains(agrColumnLabelMap.get(sn.str))*/) {
				boolean flag = false;
				for (StrAndNum sn1 : columnLabelList) {
					if (StringUtils.equals(sn1.str, agrColumnLabelMap.get(sn.str))) {
						flag = true;
						break;
					}
				}
				if (!flag)
					columnLabelList.add(new StrAndNum(agrColumnLabelMap.get(sn.str), -1));
			}
		}
		for (List<StrAndNum> ss : agColumnLabelMap.values()) {
			List<StrAndNum> tmpList = new ArrayList<StrAndNum>(ss);
			ss.clear();
			for (StrAndNum sn : oldColumnLabelList) {
				//if (tmpList.contains(sn.str))
				//	ss.add(sn.str);
				for (StrAndNum sn1 : tmpList) {
					if (StringUtils.equals(sn1.str, sn.str)) {
						ss.add(new StrAndNum(sn.str, sn.num));
						break;
					}
				}
			}
		}
		Iterator<StrAndNum> it = columnLabelList.iterator();
		while (it.hasNext()) {
			if (agrColumnLabelMap.containsKey(it.next().str))
				it.remove();
		}
		UniLog.log1("oldColumnLabelList:%s, columnLabelList:%s", oldColumnLabelList, columnLabelList);
		
		final String[] nameList = new String[columnLabelList.size()];
		final int[] typeList = new int[columnLabelList.size()];
		final boolean[] hasOptionListList = new boolean[columnLabelList.size()];
		Object[] apVs = null;
		for (int i = 0; i < columnLabelList.size(); i++) {
			String label = columnLabelList.get(i).str;
			if (aggregateOrPivotList != null && (aggregateOrPivotList.indexOf(label) >= 0 
					|| (agColumnLabelMap.containsKey(label) && aggregateOrPivotList.indexOf(agColumnLabelMap.get(label).get(0).str) >= 0))) {
				nameList[i] = label;
				AggregateOrPivot.AGGREGATES agm = aggregateTypeMap.get(label);
				if (agm == AggregateOrPivot.AGGREGATES.STRCAT || agm == AggregateOrPivot.AGGREGATES.UNIQUECAT)
					typeList[i] = Cell.VTYPE_STRING;
				else if (agm == AggregateOrPivot.AGGREGATES.FIRST || agm == AggregateOrPivot.AGGREGATES.LAST) {
					if (result.getRowCount() > 0) {
						if (apVs == null) {
							result.loadOneRecV(0);
							apVs = result.getAggregateValues(0);
						}
						if (apVs != null) {
							ColumnInfo ci = getAopIdx(aggregateOrPivotMap, aopIdxList, columnLabelList.get(i).num, label);
							int apIdx = ci.aopIdx;
							if (apVs != null && apIdx >= 0 && apVs.length > apIdx && apVs[apIdx] != null) {
								if (apVs[apIdx] instanceof Double)
									typeList[i] = Cell.VTYPE_DOUBLE;
								else
									typeList[i] = Cell.VTYPE_STRING;
								aggregateVTypeMap.put(label, typeList[i]);
							} else
								typeList[i] = Cell.VTYPE_STRING;
						} else
							typeList[i] = Cell.VTYPE_STRING;
					} else
						typeList[i] = Cell.VTYPE_STRING;
				} else
					typeList[i] = Cell.VTYPE_DOUBLE;
				UniLog.log1("label:%s, type:%s", label, typeList[i]);
			}
			else {
				ColumnCell cc = result.getCell(label);
				BiColumn bc = cc.getBiColumn();
				//nameList[i] = sessionHelper.getLabel(bc.getEngName());
				nameList[i] = getBiColumnTranName(sessionHelper, bc);
				typeList[i] = cc.getType();
				hasOptionListList[i] = !isNullOrEmpty(bc.getOptionList(sessionHelper));
			}
		}

		final int defaultCbOutputFormat = 1;
		final boolean defaultRdOriLandscape = true;
		final int defaultCbPaperSize = 1;
		final boolean defaultCbAlternateRowColor = true;
		final double defaultDsFontSize = 8;
		final boolean defaultCbHideEmptyColumns = true;
		final boolean defaultCbDisplayAggregateColFirst = true;
		final boolean defaultCbDisplayAggregateResultAfterRegularColumn = true;
		final boolean defaultCbDisplayAggregateColumnOnly = false;
		final boolean defaultCbExcelAutoFilter = false;
		final boolean defaultCbExcelSingleSheet = false;
		final boolean defaultCbShowQueryConditions = false;
		final boolean defaultCbExportAllDetail = false;
		final boolean defaultCbPassword = false;

		final Combobox cbOutputFormat = new Combobox() {{
			String ss = BiConfig.getString(sessionHelper, "birt_outputformat");
			if (StringUtils.isNotBlank(ss)) {
				for (String s : ss.split(","))
					appendChild(new Comboitem(s));
			} else {
				appendChild(new Comboitem("pdf"));
				appendChild(new Comboitem("xlsx"));
				appendChild(new Comboitem("docx"));
				if (sessionHelper.getGeneralReportOutputCsv())
					appendChild(new Comboitem("csv"));
			}
			setReadonly(true);
			setSelectedIndex(defaultCbOutputFormat);
			setWidth(sessionHelper.getLargeFlag() ? "250px" : "190px");
		}};
		final Combobox cbPaperSize = new Combobox() {{
			appendChild(new Comboitem("A3") {{setValue("a3");}});
			appendChild(new Comboitem("A4") {{setValue("a4");}});
			appendChild(new Comboitem("A5") {{setValue("a5");}});
			appendChild(new Comboitem("Letter") {{setValue("us-letter");}});
			setReadonly(true);
			setSelectedIndex(defaultCbPaperSize);
			setWidth(sessionHelper.getLargeFlag() ? "250px" : "190px");
		}};
		final Radio rdOriPortrait = new Radio(sessionHelper.getLabel("Portrait"));
		final Radio rdOriLandscape = new Radio(sessionHelper.getLabel("Landscape"));
		final Checkbox cbAlternateRowColor = new Checkbox() {{
			setChecked(defaultCbAlternateRowColor);
		}};
		final Doublespinner dsFontSize = new Doublespinner(defaultDsFontSize) {{
			setStep(1);
			setFormat("#0");
			setConstraint("no empty,min 1 max 36: between 1pt to 36pt");
		}};
		final Checkbox cbHideEmptyColumns = new Checkbox() {{
			setChecked(defaultCbHideEmptyColumns);
		}};
		final Checkbox cbDisplayAggregateColFirst = new Checkbox() {{
			setChecked(defaultCbDisplayAggregateColFirst);
		}};
		final Checkbox cbDisplayAggregateResultAfterRegularColumn = new Checkbox() {{
			setChecked(defaultCbDisplayAggregateResultAfterRegularColumn);
		}};
		final Checkbox cbDisplayAggregateColumnOnly = new Checkbox() {{
			setChecked(defaultCbDisplayAggregateColumnOnly);
		}};
		final Checkbox cbExcelAutoFilter = new Checkbox() {{
			setChecked(defaultCbExcelAutoFilter);
		}};
		final Checkbox cbExcelSingleSheet = new Checkbox() {{
			setChecked(defaultCbExcelSingleSheet);
		}};
		final Checkbox cbShowQueryConditions = new Checkbox() {{
			setChecked(defaultCbShowQueryConditions);
		}};
		final Checkbox cbExportAllDetail = new Checkbox() {{
			setChecked(defaultCbExportAllDetail);
		}};
		//final Checkbox cbPassword = new Checkbox("Password protect");
		final Checkbox cbPassword = new Checkbox() {{
			setChecked(defaultCbPassword);
		}};
		final Textbox tbPassword = new Textbox() {{
			setWidth(sessionHelper.getLargeFlag() ? "250px" : "190px");
			setType("password");
			setDisabled(true);
		}};
		final Textbox tbOutFileName = new Textbox();
		tbOutFileName.setWidth(sessionHelper.getLargeFlag() ? "250px" : "190px");
        //tbOutFileName.setConstraint("/[a-zA-Z0-9\\-_]+/: Please enter a valid file name");
        tbOutFileName.setConstraint("/[\\u4E00-\\u9FA5a-zA-Z0-9\\-_]+/: Please enter a valid file name");
        tbOutFileName.setInstant(true);
    	tbOutFileName.setText(StringUtils.defaultIfBlank(settingMap != null ? (String)settingMap.get("outFileName") : null, generateOutputFileName(viewHeader)));

		final Textbox tbTitle = new Textbox();
		tbTitle.setWidth(sessionHelper.getLargeFlag() ? "250px" : "190px");
        //tbTitle.setConstraint("/[a-zA-Z0-9\\-_]+/: Please enter a valid file name");
//        tbTitle.setConstraint("/[\\u4E00-\\u9FA5a-zA-Z0-9\\-_]+/: Please enter a valid file name");
        tbTitle.setInstant(true);
//    	tbTitle.setText(title);
    	
		cbPassword.addEventListener(Events.ON_CHECK, new EventListener<CheckEvent>() {
			@Override
			public void onEvent(CheckEvent event) throws Exception {
				tbPassword.setDisabled(!cbPassword.isChecked());
			}
		});
		
		final Checkbox cbFullCheckReportColumn = new Checkbox();
		final Rows rowsReportColumn = new Rows() {{
			final Rows _this = this;
			for (int i = 0; i < columnLabelList.size(); i++) {
				final int j = i;
				final String label = columnLabelList.get(j).str;
				final int columnLabelIdx = columnLabelList.get(j).num;
				appendChild(new Row() {{
					final String name = nameList[j];
					boolean isVisible = true;
					String ss = BiConfig.getString(sessionHelper, "birt_exclude_column");
					if (StringUtils.isNotBlank(ss))
						isVisible = !Lists.newArrayList(ss.split(",")).contains(name);
					final boolean isVisible1 = isVisible;
					setVisible(isVisible1);
					appendChild(new Hbox() {{
						//final MultiSortInfo msi = sortColumnMap.get(label);
						final MultiSortInfo msi = sortColumnMap.get(columnLabelIdx);
						if (msi != null) {
							appendChild(new Div() {{
								appendChild(new Label(name));
								appendChild(new Span() {{ setSclass(msi.sortDesc ? "z-icon-caret-down" : "z-icon-caret-up"); }});
								appendChild(new Label("" + msi.num) {{ setStyle("font-size:12px !important"); }});
								setStyle("display:flex;display:-webkit-flex;align-items:center;margin:3px 0");
								setHflex("1");
							}});
						} else {
							appendChild(new Label(name) {{
								setHflex("1");
								setStyle("display:inline-block;margin:3px 0");
							}});
						}
						appendChild(new Checkbox() {{
							setStyle("margin-bottom:0px;");
							setAttribute("fieldLabel", label);
							setAttribute("columnLabelIdx", columnLabelIdx);
							setChecked(isVisible1);
							addEventListener(Events.ON_CHECK, new EventListener<CheckEvent>() {
								@Override
								public void onEvent(CheckEvent event) throws Exception {
									boolean bAllCheck;
									if (event.isChecked()) {
										bAllCheck = true;
										for (Component c : _this.queryAll("Checkbox")) {
											if (!((Checkbox)c).isChecked()) {
												bAllCheck = false;
												break;
											}
										}
									}
									else
										bAllCheck = false;
									cbFullCheckReportColumn.setChecked(bAllCheck);
								}
							});
						}});
						setHflex("1");
						setAlign("center");
					}});
				}});
			}
		}};
		cbFullCheckReportColumn.setChecked(true);
		cbFullCheckReportColumn.addEventListener(Events.ON_CHECK, new EventListener<CheckEvent>() {
			@Override
			public void onEvent(CheckEvent event) throws Exception {
				for (Component comp : rowsReportColumn.queryAll("Checkbox"))
					((Checkbox)comp).setChecked(event.isChecked());
			}
		});

		final Rows rowsAggregateFunction = new Rows() {{
			appendChild(new Row() {{
				appendChild(new Label(sessionHelper.getLabel("Any(Global)")) {{setStyle("display:inline-block;margin:3px 0");}});
				appendChild(new Checkbox() {{setDisabled(true);}});
				appendChild(new Checkbox() {{setAttribute("fieldLabel", null);setAttribute("columnLabelIdx", -1);setAttribute("aggFunc",AggFunc.Count);}});
				appendChild(new Checkbox() {{setDisabled(true);}});
				appendChild(new Checkbox() {{setDisabled(true);}});
				appendChild(new Checkbox() {{setDisabled(true);}});
				appendChild(new Checkbox() {{setDisabled(true);}});
			}});
			for (int i = 0; i < columnLabelList.size(); i++) {
				final StrAndNum sn = columnLabelList.get(i);
				final String label = sn.str;
				final int columnLabelIdx = sn.num;
				final String name = nameList[i];
				final int type = typeList[i];
				final boolean hasOptionList = hasOptionListList[i];
				appendChild(new Row() {{
					//final MultiSortInfo msi = sortColumnMap.get(label);
					final MultiSortInfo msi = sortColumnMap.get(columnLabelIdx);
					if (msi != null) {
						appendChild(new Div() {{
							appendChild(new Label(name));
							appendChild(new Span() {{ setSclass(msi.sortDesc ? "z-icon-caret-down" : "z-icon-caret-up"); }});
							appendChild(new Label("" + msi.num) {{ setStyle("font-size:12px !important"); }});
							setStyle("display:flex;display:-webkit-flex;align-items:center;margin:3px 0");
						}});
					} else {
						appendChild(new Label(name) {{
							setStyle("display:inline-block;margin:3px 0");
						}});
					}
					appendChild(new Checkbox() {{setAttribute("fieldLabel", label);setAttribute("columnLabelIdx", columnLabelIdx);setAttribute("aggFunc",null);}});
					appendChild(new Checkbox() {{setDisabled(true);setAttribute("fieldLabel", label);setAttribute("columnLabelIdx", columnLabelIdx);setAttribute("aggFunc", AggFunc.Count);}});
					appendChild(new Checkbox() {{setDisabled(!isNumericCellType(type) || hasOptionList);setAttribute("fieldLabel", label);setAttribute("columnLabelIdx", columnLabelIdx);setAttribute("aggFunc", AggFunc.Sum);}});
					appendChild(new Checkbox() {{setDisabled(!isNumericCellType(type) || hasOptionList);setAttribute("fieldLabel", label);setAttribute("columnLabelIdx", columnLabelIdx);setAttribute("aggFunc", AggFunc.Min);}});
					appendChild(new Checkbox() {{setDisabled(!isNumericCellType(type) || hasOptionList);setAttribute("fieldLabel", label);setAttribute("columnLabelIdx", columnLabelIdx);setAttribute("aggFunc", AggFunc.Max);}});
					appendChild(new Checkbox() {{setDisabled(!isNumericCellType(type) || hasOptionList);setAttribute("fieldLabel", label);setAttribute("columnLabelIdx", columnLabelIdx);setAttribute("aggFunc", AggFunc.Avg);}});
				}});
			}
		}};

		boolean isCustomReport = !StringUtils.equals(designRes, "ReportGenerate.rptdesign");
		final Vbox optionBox = new Vbox() {{
			final String spaceHeight = sessionHelper.isMobile() ? "0px" : "10px";
			appendChild(new Hbox() {{
				appendChild(new Label(sessionHelper.getLabel("Output format")) {{setHflex("1");}});
				appendChild(cbOutputFormat);
				setHflex("1");
			}});

			if (sessionHelper.hasAccessRight("#rptoptions")) {
				if (!sessionHelper.isMobile())
					appendChild(new Space(){{ this.setHeight(spaceHeight);}});
				appendChild(new Hbox() {{
					appendChild(new Label(sessionHelper.getLabel("File name")) {{setHflex("1");}});
					appendChild(tbOutFileName);
					setHflex("1");
				}});
	
				if (!sessionHelper.isMobile())
					appendChild(new Space(){{ this.setHeight(spaceHeight);}});
				appendChild(new Hbox() {{
					appendChild(new Label(sessionHelper.getLabel("Title")) {{setHflex("1");}});
					appendChild(tbTitle);
					setHflex("1");
				}});
	
				if (!sessionHelper.isMobile())
					appendChild(new Space(){{ this.setHeight(spaceHeight);}});
				appendChild(new Hbox() {{
					appendChild(new Hbox() {{
						appendChild(new Label(sessionHelper.getLabel("Password")));
						appendChild(cbPassword);
						setHflex("1");
					}});
					appendChild(tbPassword);
					setHflex("1");
				}});
	
				if (!sessionHelper.isMobile())
					appendChild(new Space(){{ this.setHeight(spaceHeight);}});
				appendChild(new Hbox() {{
					appendChild(new Label(sessionHelper.getLabel("Orientation")) {{ setHflex("1"); }});
					appendChild(new Radiogroup() {{
						appendChild(rdOriPortrait);
						appendChild(rdOriLandscape);
						rdOriLandscape.setChecked(defaultRdOriLandscape);
					}});
					setHflex("1");
				}});
	
				if (!sessionHelper.isMobile())
					appendChild(new Space(){{ this.setHeight(spaceHeight);}});
				appendChild(new Hbox() {{
					appendChild(new Label(sessionHelper.getLabel("Paper size")) {{ setHflex("1"); }});
					appendChild(cbPaperSize);
					setHflex("1");
				}});
	
				if (!sessionHelper.isMobile())
					appendChild(new Space(){{ this.setHeight(spaceHeight);}});
				appendChild(new Hbox() {{
					appendChild(new Label(sessionHelper.getLabel("Font size")) {{ setHflex("1"); }});
					appendChild(dsFontSize);
					setHflex("1");
				}});
	
				if (!sessionHelper.isMobile())
					appendChild(new Space(){{ this.setHeight(spaceHeight);}});
				appendChild(new Hbox() {{
					appendChild(new Label(sessionHelper.getLabel("Alternate row color")) {{ setHflex("1"); }});
					appendChild(cbAlternateRowColor);
					setHflex("1");
				}});
	
	
				if (!sessionHelper.isMobile())
					appendChild(new Space(){{ this.setHeight(spaceHeight);}});
				appendChild(new Hbox() {{
					appendChild(new Label(sessionHelper.getLabel("Hide empty columns")) {{ setHflex("1"); }});
					appendChild(cbHideEmptyColumns);
					setHflex("1");
				}});
	
				if (!sessionHelper.isMobile())
					appendChild(new Space(){{ this.setHeight(spaceHeight);}});
				appendChild(new Hbox() {{
					appendChild(new Label(sessionHelper.getLabel("Display aggregate column first")) {{ setHflex("1"); }});
					appendChild(cbDisplayAggregateColFirst);
					setHflex("1");
				}});
	
				if (!sessionHelper.isMobile())
					appendChild(new Space(){{ this.setHeight(spaceHeight);}});
				appendChild(new Hbox() {{
					appendChild(new Label(sessionHelper.getLabel("Display aggregate result after regular column")) {{ setHflex("1"); }});
					appendChild(cbDisplayAggregateResultAfterRegularColumn);
					setHflex("1");
				}});
	
				if (!sessionHelper.isMobile())
					appendChild(new Space(){{ this.setHeight(spaceHeight);}});
				appendChild(new Hbox() {{
					appendChild(new Label(sessionHelper.getLabel("Display aggregate column only")) {{ setHflex("1"); }});
					appendChild(cbDisplayAggregateColumnOnly);
					setHflex("1");
				}});
	
				if (!sessionHelper.isMobile())
					appendChild(new Space(){{ this.setHeight(spaceHeight);}});
				appendChild(new Hbox() {{
					appendChild(new Label(sessionHelper.getLabel("Excel Auto Filter")) {{ setHflex("1"); }});
					appendChild(cbExcelAutoFilter);
					setHflex("1");
				}});
	
				if (!sessionHelper.isMobile())
					appendChild(new Space(){{ this.setHeight(spaceHeight);}});
				appendChild(new Hbox() {{
					appendChild(new Label(sessionHelper.getLabel("Excel Single Sheet")) {{ setHflex("1"); }});
					appendChild(cbExcelSingleSheet);
					setHflex("1");
				}});
	
				if (!isCustomReport) {
					if (!sessionHelper.isMobile())
						appendChild(new Space(){{ this.setHeight(spaceHeight);}});
					appendChild(new Hbox() {{
						appendChild(new Label(sessionHelper.getLabel("Show Query Conditions")) {{ setHflex("1"); }});
						appendChild(cbShowQueryConditions);
						setHflex("1");
					}});
				}
	
				if (!sessionHelper.isMobile())
					appendChild(new Space(){{ this.setHeight(spaceHeight);}});
				appendChild(new Hbox() {{
					appendChild(new Label(sessionHelper.getLabel("Export All Detail")) {{ setHflex("1"); }});
					appendChild(cbExportAllDetail);
					setHflex("1");
				}});
			}

			if (sessionHelper.isMobile()) {
				setHflex("1");
				setVflex("min");
				setStyle("margin:0px 0px 0px 0px;");
			} else {
				setWidth(sessionHelper.getLargeFlag() ? "470px" : "340px");
				setStyle("margin:5px 0px 0px 10px;");
			}
		}};
		
		final Tabbox leftBox = new Tabbox() {{
			appendChild(new Tabs() {{
				appendChild(new Tab(sessionHelper.getLabel("Report Column")));
				appendChild(new Tab(sessionHelper.getLabel("Aggregate Function")));
			}});
			appendChild(new Tabpanels() {{
				appendChild(new Tabpanel() {{
					//Report Column
					appendChild(new Grid() {{
						setSclass("zbki-gr-column");
						appendChild(new Auxhead() {{
							appendChild(new Auxheader() {{
								appendChild(new Hbox() {{
									appendChild(new Label(sessionHelper.getLabel("Column List")) {{setHflex("1");}});
									appendChild(cbFullCheckReportColumn);
									setHflex("1");
								}});
							}});
							setStyle("background:#779CB1");
						}});
						appendChild(new Columns() {{
							appendChild(new Column());
						}});
						appendChild(rowsReportColumn);
						setVflex("1");
					}});
					setVflex("1");
				}});
				appendChild(new Tabpanel() {{
					//Aggregate Function
					appendChild(new Grid() {{
						setSclass("zbki-gr-column");
						appendChild(new Auxhead() {{
							appendChild(new Auxheader(sessionHelper.getLabel("Aggregate Column")) {{setColspan(2);}});
							appendChild(new Auxheader("Count") {{setAlign("center");}});
							appendChild(new Auxheader("Sum") {{setAlign("center");}});
							appendChild(new Auxheader("Min") {{setAlign("center");}});
							appendChild(new Auxheader("Max") {{setAlign("center");}});
							appendChild(new Auxheader("Avg") {{setAlign("center");}});
							setStyle("background:#779CB1");
						}});
						appendChild(new Columns() {{
							appendChild(new Column());
							appendChild(new Column() {{setWidth("40px");setAlign("center");}});
							appendChild(new Column() {{setWidth("40px");setAlign("center");}});
							appendChild(new Column() {{setWidth("40px");setAlign("center");}});
							appendChild(new Column() {{setWidth("40px");setAlign("center");}});
							appendChild(new Column() {{setWidth("40px");setAlign("center");}});
							appendChild(new Column() {{setWidth("40px");setAlign("center");}});
						}});
						appendChild(rowsAggregateFunction);
						setVflex("1");
					}});
					setVflex("1");
				}});
				setVflex("1");
			}});
			setVflex("1");
			if (sessionHelper.isMobile())
				setStyle("margin:0px 0px 0px 0px");
			else
				setStyle("margin:5px 0px 5px 0px");
		}};
		
		Component container;
		if (sessionHelper.isMobile())
			container = new Vbox() {{
				appendChild(leftBox);
				appendChild(optionBox);
				setHflex("1");
				setVflex("1");
			}};
		else
			container = new Hbox() {{ 
				appendChild(leftBox);
				appendChild(optionBox);
				setVflex("1");
			}};

		Window win = new Window();
		win.setStyle("padding:0px !important; user-select: none;");
		//win.setWidth("100%");
		//win.setHeight("calc(100% - 50px)");
		win.appendChild(container);
		
		class DialogSetting {
			void savePresetOrGenerateReport(boolean isGenReport) throws Exception {
				final List<String> newColumnLabelList = new ArrayList<String>();
				final List<StrAndNum> printColumnLabelList = new ArrayList<StrAndNum>();
				final List<StrAndNum> tmpprintColumnLabelList = new ArrayList<StrAndNum>();
				final Set<String> newAggColumnSet = new HashSet<String>();
				final Set<StrAndNum> printAggColumnSet = new HashSet<StrAndNum>();
				final Map<String, List<AggFunc>> newAggResultColumnMap = new LinkedHashMap<String, List<AggFunc>>();
				final Map<StrAndNum, List<AggFunc>> printAggResultColumnMap = new LinkedHashMap<StrAndNum, List<AggFunc>>();
				for (Component comp : rowsReportColumn.queryAll("Checkbox")) {
					String fieldLabel = (String)comp.getAttribute("fieldLabel");
					int columnLabelIdx = (Integer)comp.getAttribute("columnLabelIdx");
					if (((Checkbox)comp).isChecked()) {
						newColumnLabelList.add(fieldLabel);
						if (agColumnLabelMap.containsKey(fieldLabel)) {
							for (StrAndNum sn : agColumnLabelMap.get(fieldLabel))
								tmpprintColumnLabelList.add(new StrAndNum(sn.str, sn.num));
						} else
							tmpprintColumnLabelList.add(new StrAndNum(fieldLabel, columnLabelIdx));
					}
				}
				for (StrAndNum sn : oldColumnLabelList) {
					for (StrAndNum sn1 : tmpprintColumnLabelList) {
						if (StringUtils.equals(sn1.str, sn.str) && sn1.num == sn.num) {
							printColumnLabelList.add(new StrAndNum(sn.str, sn.num));
							break;
						}
					}
				}
				for (Component comp : rowsAggregateFunction.queryAll("Checkbox")) {
					if (((Checkbox)comp).isChecked()) {
						String label = (String)comp.getAttribute("fieldLabel");
						int columnLabelIdx = (Integer)comp.getAttribute("columnLabelIdx");
						AggFunc aggFunc = (AggFunc)comp.getAttribute("aggFunc");
						if (!newAggResultColumnMap.containsKey(label)) {
							newAggResultColumnMap.put(label, new ArrayList<AggFunc>());
							if (agColumnLabelMap.containsKey(label)) {
								for (StrAndNum sn : agColumnLabelMap.get(label))
									printAggResultColumnMap.put(sn, new ArrayList<AggFunc>());
							}
							else
								printAggResultColumnMap.put(new StrAndNum(label, columnLabelIdx), new ArrayList<AggFunc>());
						}
						if (aggFunc != null) {
							newAggResultColumnMap.get(label).add(aggFunc);
							if (agColumnLabelMap.containsKey(label)) {
								for (StrAndNum sn : agColumnLabelMap.get(label))
									printAggResultColumnMap.get(sn).add(aggFunc);
							} else {
								//printAggResultColumnMap.get(label).add(aggFunc);
								for (StrAndNum sn : printAggResultColumnMap.keySet()) {
									if (StringUtils.equals(sn.str, label)) {
										printAggResultColumnMap.get(sn).add(aggFunc);
										break;
									}
								}
							}
						} 
						else {
							newAggColumnSet.add(label);
							if (agColumnLabelMap.containsKey(label)) {
								for (StrAndNum sn : agColumnLabelMap.get(label))
									printAggColumnSet.add(sn);
							}
							else
								printAggColumnSet.add(new StrAndNum(label, columnLabelIdx));
						}
					}
				}
				UniLog.log1("newColumnLabelList:%s", newColumnLabelList);
				UniLog.log1("printColumnLabelList:%s", printColumnLabelList);
				UniLog.log1("newAggColumnSet:%s", newAggColumnSet);
				UniLog.log1("printAggColumnSet:%s", printAggColumnSet);
				UniLog.log1("newAggResultColumnMap:%s", newAggResultColumnMap);
				UniLog.log1("printAggResultColumnMap:%s", printAggResultColumnMap);
				if (!newColumnLabelList.isEmpty()) {
					if (isGenReport) {
						//generate report
						String rptTitle,rptSubtitle;
						if(StringUtils.isBlank(tbTitle.getText())) {
							rptTitle = title;
							rptSubtitle = subTitle;
						} else {
							rptTitle = tbTitle.getText();
							rptSubtitle = "";
						}
						new ReportGenerate(sessionHelper, result, printColumnLabelList, aggregateTypeMap, aggregateVTypeMap, sortColumnMap, aopHeaderMap, itemCreateScriptMap, userPropMap, listModelList, aopIdxList, aggregateOrPivotMap, 
								printAggColumnSet, printAggResultColumnMap,
								(String)cbPaperSize.getSelectedItem().getValue(), rdOriPortrait.isChecked() ? "portrait" : "landscape", 
								cbAlternateRowColor.isChecked(), (float)dsFontSize.doubleValue(), cbHideEmptyColumns.isChecked(), 
								cbDisplayAggregateColFirst.isChecked(), cbDisplayAggregateResultAfterRegularColumn.isChecked(), 
								cbDisplayAggregateColumnOnly.isChecked(), cbExcelAutoFilter.isChecked(), cbExcelSingleSheet.isChecked(), cbShowQueryConditions.isChecked(), cbExportAllDetail.isChecked(),
								rptTitle, rptSubtitle, queryConditions, designRes, cbOutputFormat.getSelectedItem().getLabel(),
								cbPassword.isChecked() ? tbPassword.getText() : null, tbOutFileName.getText(), callback);
					} else {
						//save preset
						genRptSettingDatas.put("columnLabelList", new JSONArray() {{
							for (String clabel : newColumnLabelList)
								put(clabel);
						}});
						genRptSettingDatas.put("aggregateColumnList", new JSONArray() {{
							for (String aCol : newAggColumnSet)
								put(aCol);
						}});
						genRptSettingDatas.put("aggregateResultColumnMap", new JSONObject() {{
							for (Map.Entry<String, List<AggFunc>> entry : newAggResultColumnMap.entrySet()) {
								String name = entry.getKey();
								JSONArray arr = new JSONArray();
								for (AggFunc af : entry.getValue())
									arr.put(af.name());
								if (arr.length() > 0)
									put(name != null ? name : "", arr);
							}
						}});
						genRptSettingDatas.put("paperSize", (String)cbPaperSize.getSelectedItem().getValue());
						genRptSettingDatas.put("orientation", rdOriPortrait.isChecked() ? "portrait" : "landscape");
						genRptSettingDatas.put("alternateRowColor", cbAlternateRowColor.isChecked());
						genRptSettingDatas.put("fontSize", dsFontSize.doubleValue());
						genRptSettingDatas.put("hideEmptyColumns", cbHideEmptyColumns.isChecked());
						genRptSettingDatas.put("displayAggregateColFirst", cbDisplayAggregateColFirst.isChecked());
						genRptSettingDatas.put("displayAggregateResultAfterRegularColumn", cbDisplayAggregateResultAfterRegularColumn.isChecked());
						genRptSettingDatas.put("displayAggregateColumnOnly", cbDisplayAggregateColumnOnly.isChecked());
						genRptSettingDatas.put("excelAutoFilter", cbExcelAutoFilter.isChecked());
						genRptSettingDatas.put("excelSingleSheet", cbExcelSingleSheet.isChecked());
						genRptSettingDatas.put("showQueryConditions", cbShowQueryConditions.isChecked());
						genRptSettingDatas.put("exportAllDetail", cbExportAllDetail.isChecked());
						genRptSettingDatas.put("outputFormat", cbOutputFormat.getSelectedItem().getLabel());
						genRptSettingDatas.put("fileName", tbOutFileName.getText());
						genRptSettingDatas.put("title", tbTitle.getText());
						genRptSettingDatas.put("hasPassword", cbPassword.isChecked());
						genRptSettingDatas.put("password", tbPassword.getText());
						conditionFieldMap.setGeneralReportSettingData(genRptSettingDatas);
						String errMsg = conditionPresets.saveConditionPresets(viewId, sessionHelper);
						ZkBiMsgbox.show(errMsg != null ? errMsg : sessionHelper.getLabel("Save finish"));
						btnMap.get("Clear Preset").setDisabled(false);
					}
				}
				else 
					ZkBiMsgbox.show(sessionHelper.getLabel("Please check columns"));
			}
			void loadPreset() throws Exception {
				if (genRptSettingDatas == null || genRptSettingDatas.length() == 0)
					return;
				UniLog.log("genRptSettingDatas:" + genRptSettingDatas);
				JSONArray clList = genRptSettingDatas.optJSONArray("columnLabelList");
				if (clList != null) {
					Set<String> columnLabelSet = new HashSet<String>();
					for (int i = 0; i < clList.length(); i++)
						columnLabelSet.add(clList.optString(i));
					boolean bAllCheck = true;
					for (Component component : rowsReportColumn.queryAll("Checkbox")) {
						String fieldLabel = (String)component.getAttribute("fieldLabel");
						boolean b = columnLabelSet.contains(fieldLabel);
						((Checkbox)component).setChecked(b);
						if (!b)
							bAllCheck = false;
					}
					cbFullCheckReportColumn.setChecked(bAllCheck);
				}
				JSONArray acList = genRptSettingDatas.optJSONArray("aggregateColumnList");
				JSONObject arObject = genRptSettingDatas.optJSONObject("aggregateResultColumnMap");
				Set<String> aggLabelSet = new HashSet<String>();
				Map<String, List<AggFunc>> aggResultColumnMap = new HashMap<String, List<AggFunc>>();
				if (acList != null) {
					for (int i = 0; i < acList.length(); i++)
						aggLabelSet.add(acList.getString(i));
				}
				if (arObject != null && arObject.length() > 0) {
					JSONArray arNames = arObject.names();
					for (int i = 0; i < arNames.length(); i++) {
						String name = arNames.getString(i);
						List<AggFunc> list = new ArrayList<AggFunc>();
						JSONArray arList = arObject.optJSONArray(name);
						if (arList != null) {
							for (int j = 0; j < arList.length(); j++)
								list.add(AggFunc.valueOf(arList.getString(j)));
							aggResultColumnMap.put(name.isEmpty() ? null : name, list);
						}
					}
				}
				for (Component component : rowsAggregateFunction.queryAll("Checkbox")) {
					String fieldLabel = (String)component.getAttribute("fieldLabel");
					AggFunc aggFunc = (AggFunc)component.getAttribute("aggFunc");
					Checkbox cb = (Checkbox)component;
					if (aggFunc != null) {
						List<AggFunc> afList = aggResultColumnMap.get(fieldLabel);
						cb.setChecked(afList != null && afList.contains(aggFunc));
					} else
						cb.setChecked(aggLabelSet.contains(fieldLabel));
				}

				String pageSize = genRptSettingDatas.optString("paperSize");
				for (Comboitem ci : cbPaperSize.getItems()) {
					if (ci.getValue().equals(pageSize)) {
						cbPaperSize.setSelectedItem(ci);
						break;
					}
				}
				String orientation = genRptSettingDatas.optString("orientation");
				rdOriPortrait.setChecked(orientation.equals("portrait"));
				if (genRptSettingDatas.has("alternateRowColor"))
					cbAlternateRowColor.setChecked(genRptSettingDatas.optBoolean("alternateRowColor"));
				double fontSize = genRptSettingDatas.optDouble("fontSize");
				if (!Double.isNaN(fontSize) && fontSize > 0)
					dsFontSize.setValue(fontSize);
				if (genRptSettingDatas.has("hideEmptyColumns"))
					cbHideEmptyColumns.setChecked(genRptSettingDatas.optBoolean("hideEmptyColumns"));
				if (genRptSettingDatas.has("displayAggregateColFirst"))
					cbDisplayAggregateColFirst.setChecked(genRptSettingDatas.optBoolean("displayAggregateColFirst"));
				if (genRptSettingDatas.has("displayAggregateResultAfterRegularColumn"))
					cbDisplayAggregateResultAfterRegularColumn.setChecked(genRptSettingDatas.optBoolean("displayAggregateResultAfterRegularColumn"));
				if (genRptSettingDatas.has("displayAggregateColumnOnly"))
					cbDisplayAggregateColumnOnly.setChecked(genRptSettingDatas.optBoolean("displayAggregateColumnOnly"));
				if (genRptSettingDatas.has("excelAutoFilter"))
					cbExcelAutoFilter.setChecked(genRptSettingDatas.optBoolean("excelAutoFilter"));
				if (genRptSettingDatas.has("excelSingleSheet"))
					cbExcelSingleSheet.setChecked(genRptSettingDatas.optBoolean("excelSingleSheet"));
				if (genRptSettingDatas.has("showQueryConditions"))
					cbShowQueryConditions.setChecked(genRptSettingDatas.optBoolean("showQueryConditions"));
				if (genRptSettingDatas.has("exportAllDetail"))
					cbExportAllDetail.setChecked(genRptSettingDatas.optBoolean("exportAllDetail"));
				String outputFormat = genRptSettingDatas.optString("outputFormat");
				for (Comboitem ci : cbOutputFormat.getItems()) {
					if (ci.getLabel().equals(outputFormat)) {
						cbOutputFormat.setSelectedItem(ci);
						break;
					}
				}
				String fileName = genRptSettingDatas.optString("fileName");
				if (StringUtils.isNotBlank(fileName))
					tbOutFileName.setText(fileName);
				String title = genRptSettingDatas.optString("title");
				if (StringUtils.isNotBlank(title))
					tbTitle.setText(title);
				if (genRptSettingDatas.has("hasPassword"))
					cbPassword.setChecked(genRptSettingDatas.optBoolean("hasPassword"));
				if (cbPassword.isChecked())
					tbPassword.setText(genRptSettingDatas.optString("password"));
			}
			void clearPreset() throws Exception {
				if (genRptSettingDatas == null || genRptSettingDatas.length() == 0)
					return;
				JSONArray names = genRptSettingDatas.names();
				for (int i = 0; i < names.length(); i++)
					genRptSettingDatas.remove(names.getString(i));
				conditionFieldMap.setGeneralReportSettingData(null);
				String errMsg = conditionPresets.saveConditionPresets(viewId, sessionHelper);
				ZkBiMsgbox.show(errMsg != null ? errMsg : sessionHelper.getLabel("Clear finish"));
				btnMap.get("Clear Preset").setDisabled(true);
			}
			void clear() {
				for (Component component : rowsReportColumn.queryAll("Checkbox"))
					((Checkbox)component).setChecked(true);
				for (Component component : rowsAggregateFunction.queryAll("Checkbox"))
					((Checkbox)component).setChecked(false);
				tbTitle.setText("");
				cbFullCheckReportColumn.setChecked(true);
				cbPaperSize.setSelectedIndex(defaultCbPaperSize);
				rdOriLandscape.setChecked(defaultRdOriLandscape);
				cbAlternateRowColor.setChecked(defaultCbAlternateRowColor);
				dsFontSize.setValue(defaultDsFontSize);
				cbHideEmptyColumns.setChecked(defaultCbHideEmptyColumns);
				cbDisplayAggregateColFirst.setChecked(defaultCbDisplayAggregateColFirst);
				cbDisplayAggregateResultAfterRegularColumn.setChecked(defaultCbDisplayAggregateResultAfterRegularColumn);
				cbDisplayAggregateColumnOnly.setChecked(defaultCbDisplayAggregateColumnOnly);
				cbExcelAutoFilter.setChecked(defaultCbExcelAutoFilter);
				cbExcelSingleSheet.setChecked(defaultCbExcelSingleSheet);
				cbShowQueryConditions.setChecked(defaultCbShowQueryConditions);
				cbExportAllDetail.setChecked(defaultCbExportAllDetail);
				cbOutputFormat.setSelectedIndex(defaultCbOutputFormat);
				tbOutFileName.setText(StringUtils.defaultIfBlank(settingMap != null ? (String)settingMap.get("outFileName") : null, generateOutputFileName(viewHeader)));
				cbPassword.setChecked(defaultCbPassword);
				tbPassword.setText("");
			}
		}
		final DialogSetting dlgSetting = new DialogSetting();
		
		MessageboxDlg dlg = ZkUtil.buildSimpleMessageboxDlg(String.format("%s - %s", sessionHelper.getLabel("General Report"), title), 
			win, 
			new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.YES, Messagebox.Button.NO, Messagebox.Button.ABORT, Messagebox.Button.CANCEL}, 
			new String[] {sessionHelper.getBtLabel("Create Report"), sessionHelper.getBtLabel("Save Preset"), sessionHelper.getBtLabel("Clear Preset"), sessionHelper.getBtLabel("Clear"), sessionHelper.getBtLabel("Close")},
			comp, 
			new ZkBiEventListener<Messagebox.ClickEvent>() {
				@Override
				public void onZkBiEvent(ClickEvent event) throws Exception {
					if (event.getButton() == null)
						return;
					switch (event.getButton()) {
						case OK:
							//generate report
							dlgSetting.savePresetOrGenerateReport(true);
							event.stopPropagation();
							break;
						case YES:
							//save preset
							dlgSetting.savePresetOrGenerateReport(false);
							event.stopPropagation();
							break;
						case NO:
							//clear Preset
							dlgSetting.clearPreset();
							event.stopPropagation();
							break;
						case ABORT:
							//clear
							dlgSetting.clear();
							event.stopPropagation();
							break;
					}
				}
			}
		);
		//find Clear Preset button
		for (Component cbtn : dlg.queryAll("Button")) {
			Button btn = (Button)cbtn;
			if (StringUtils.equals(btn.getLabel(), sessionHelper.getLabel("Save Preset")))
				btnMap.put("Save Preset", btn);
			else if (StringUtils.equals(btn.getLabel(), sessionHelper.getLabel("Clear Preset")))
				btnMap.put("Clear Preset", btn);
		}
		UniLog.log("isAdmin:" + sessionHelper.isAdminUser());
		if (genRptSettingDatas == null || (!sessionHelper.isAdminUser() && !conditionFieldMap.isCustom())) {
			btnMap.get("Save Preset").setDisabled(true);
			btnMap.get("Clear Preset").setDisabled(true);
		} else if (genRptSettingDatas.length() == 0)
			btnMap.get("Clear Preset").setDisabled(true);
		if (genRptSettingDatas != null && genRptSettingDatas.length() > 0) {
			try {
				dlgSetting.loadPreset();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		dlg.setSizable(true);
		if (sessionHelper.isMobile())
			ZkUtil.appendStyle(dlg, "width:100%;max-width:500px;height:100%;min-height:660px;");
		else {
			if (sessionHelper.getLargeFlag()) {
				if (isCustomReport)
					ZkUtil.appendStyle(dlg, "width:980px;height:900px;");
				else
					ZkUtil.appendStyle(dlg, "width:980px;height:960px;");
				//ZkUtil.appendStyle(dlg, "width:980px;height:900px;");
				//ZkUtil.appendStyle(dlg, "width:980px;height:860px;");
				//ZkUtil.appendStyle(dlg, "width:980px;height:820px;");
				//ZkUtil.appendStyle(dlg, "width:980px;height:780px;");
			} else {
				if (isCustomReport)
					ZkUtil.appendStyle(dlg, "width:800px;height:720px;");
				else
					ZkUtil.appendStyle(dlg, "width:800px;height:760px;");
				//ZkUtil.appendStyle(dlg, "width:800px;height:720px;");
				//ZkUtil.appendStyle(dlg, "width:800px;height:680px;");
				//ZkUtil.appendStyle(dlg, "width:800px;height:640px;");
				//ZkUtil.appendStyle(dlg, "width:800px;height:605px;");
			}
		}
		dlg.doHighlighted();
	}

    /*public static MessageboxDlg buildMessageboxDlg(String title, final HtmlBasedComponent child, Messagebox.Button[] buttons, String[] buttonsLabel, Component parent, EventListener<Messagebox.ClickEvent> eventListener) {
		MessageboxDlg dlg = new MessageboxDlg();
		dlg.setTitle(title);
		dlg.setBorder("normal");
		dlg.setClosable(true);
    	dlg.setParent(parent);
    	child.setVflex("1");
    	final Div div = new Div();
    	div.setId("buttons");
    	div.setStyle("display:flex;display:-webkit-flex;"
    			+ "flex-wrap:wrap;-webkit-wrap:wrap;"
    			+ "justify-content:center;-webkit-justify-content:center;"
    			+ "padding:0 5px 10px 0;");
    	div.setAttribute("button.sclass", "zkbi-messagebox-button");
    	dlg.appendChild(new Vbox() {{
    		appendChild(child);
    		appendChild(div);
    		setVflex("1");
    	}});

    	dlg.setButtons(buttons, buttonsLabel);
    	dlg.setEventListener(eventListener);
    	return dlg;
    }*/
	
	private static class StrAndNum {
		String str;
		int num;
		public StrAndNum(String str, int num) {
			this.str = str;
			this.num = num;
		}
	}
	
	public static interface Callback {
		void beforeAddUserProp(ReportGenerate rptGen);
	}
	
	 public static boolean isNullOrEmpty(Object arg) {
	        if (arg == null) {
	            return true;
	        }

	        // String
	        if (arg instanceof String) {
	            return ((String) arg).isEmpty();
	        }

	        // Collection
	        if (arg instanceof Collection) {
	            return ((Collection<?>) arg).isEmpty();
	        }

	        // Map
	        if (arg instanceof Map) {
	            return ((Map<?, ?>) arg).isEmpty();
	        }

	        // Array (any type: Object[], int[], byte[], etc.)
	        if (arg.getClass().isArray()) {
	            return Array.getLength(arg) == 0;
	        }

	        return false;
	    }
}
