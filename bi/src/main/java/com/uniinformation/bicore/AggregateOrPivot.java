package com.uniinformation.bicore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.uniinformation.cell.CellPair;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.utils.TranslateUtil;
import com.uniinformation.utils.UniLog;

public class AggregateOrPivot {
	
		/* 
		 *  New Version with extention to have different pivot setting per aggregate
		 *  the colValsArr is nolonger global to each AggregateOrPivot object. It is moved to inside AggregateRec, 
		 *  stage a) in backwork compatible mode, only colValsArr in aggsArr.get(0) ( the first aggregate) is initialized and used as previouse global colValsArr
		 *  stage b) later, codes in BiResult will be modified to call addOrGetPivotList for each aggregate, the colValsArr in each AggregateRec will all in used but will all have
		 *        same values , this should still be compatable will previous version
		 *  stage 3) the code that use colValsArr should handle the case that colValsArr for differnent AggregateRec will be differnet. E.g. the code to check and get the 
		 *        column index of a pivoted aggregate column can not be calculated by sizeofpivotcolumns * n + index position of aggregate bechase the sizeofpivotcolumn is nolonger
		 *        constant for all aggregate.
		 *  stage 4) add code in BiResult to allow the values passed to addOrGetPivotList to be different for each aggregate.
		 *  
		 *  2023/03/09 
		 */
		int maxValCol = 200;
		private boolean useNewPivotHeader = false;
		private boolean hasPivotSubtotal = false;
		private Boolean headerAggregateFirst = null;
		public static boolean enableTranslateAggHeader = true; //andrew221201 translate aggregate header (experimental)
		
		static public enum AGGREGATES {
			COUNT,
			SUM,
			STRCAT,
			UNIQUECAT,
			FIRST,
			LAST,
			MAX,
			MIN,
			PERCENT_TOTAL,
			PERCENT_ROW,
			PERCENT_COL,
			EXPRESSION,
			EXPRESSION2
		};	
    	public ArrayList<ColRec> getColsArr() {
			return colsArr;
		}

		public ArrayList<RowRec> getRowsArr() {
			return rowsArr;
		}

		public ArrayList<AggregateRec> getAggsArr() {
			return aggsArr;
		}

		public String getRptName() {
			return rptName;
		}

		public class RowRec {
    		public boolean isHided() {
				return hided;
			}
//			public void setName(String p_name) {
//    			rowName = p_name;
//			}
			public String getName(BiResult p_result) {
//				if(rowName != null) return(rowName);
				if(p_result != null) {
					return(p_result.getCell(rowId).getBiColumn().getEngName());
				}
    			return(rowId);
			}
			boolean hided = false;
//    		String rowName;
    		String rowId;
    		String style;
    		public RowRec(String p_rowId,String p_style) {
    			rowId = p_rowId;
    			style = p_style;
    		}
    		public String getId() {
				return rowId;
    		}
    	}
    	public class ColRec {
    		public boolean isHided() {
				return hided;
			}
			public String getName(BiResult p_result) {
//				if(colName != null) return(colName);
				if(p_result != null) return(p_result.getCell(colId).getBiColumn().getEngName());
				return(colId);
			}
			boolean hided = false;
    		String colId;
 //   		String colName;
    		String style;
    		public ColRec(String p_id,String p_style) {
    			colId = p_id;
    			style = p_style;
    		}
    		public String getId() {
				return colId;
    		}
    	}
    	public class AggregateRec {
    		private String aggid;
    		AGGREGATES aggregate;
    		private String format;
    		private String width;
    		private String style;
    		String[] varId;
    		boolean hided;
    		com.uniinformation.utils.exprpar.Parser parser;
    		private ArrayList<Comparable[]> colValsArr = null;
    		public boolean isHided() {
				return hided;
			}
    		AggregateRec(String p_aggid,AGGREGATES p_aggregate,String p_format,String p_width,String p_style,String... args) {
    			aggid = p_aggid;
    			aggregate = p_aggregate;
    			format = p_format;
    			width = p_width;
    			style = p_style;
    			varId= args;
    		}
    		public String getKey() {
    			if(aggid != null) return(aggid);
    			String s = null ;
    			for(String ss : varId) {
    				if(s == null) s = ss ; else s += "," + ss;
    			}
				return (aggregate.name()+"("+(s==null ? "": s)+ ")");
    		}
    		/***
    		 * andrew221201 expose aggregate bicolumn for create translation context menu
    		 * @param p_result
    		 * @return
    		 */
    		public BiColumn getBiColumn(BiResult p_result) {
    			if (p_result == null) return null;
    			if (aggid == null) return null;
    			BiColumn bc = p_result.getColumnByLabel(aggid);
    			if(bc != null) return(bc);
    			bc = p_result.getTempColumnByLabel(aggid);
    			return(bc);
    		}
    		public String getName(BiResult p_result) {
				if(p_result != null && aggid != null) {
					BiColumn bc = p_result.getColumnByLabel(aggid);
					//if(bc != null) return(bc.getEngName());
					
					//andrew221201 translate aggregate header (experimental)
					if (bc != null) {
						if (enableTranslateAggHeader) {
							return TranslateUtil.getText(p_result.getSessionHelper(), bc.getCellFullName(), "LABEL", p_result.getSessionHelper().getLabel(bc));
						}
						else {
							return(bc.getEngName());
						}
					}
				}
    			switch (aggregate) {
    			case COUNT:
    					return("Count");
    			case SUM:
    			case MAX:
    			case MIN:
    			case FIRST:
    			case LAST:
    					if(p_result != null)
    						return(p_result.getCell(varId[0]).getBiColumn().getEngName());
    					else
    						return(aggregate.toString()+"("+varId[0]+")");
    			case EXPRESSION:
    			case EXPRESSION2:
    					if(p_result != null)
    						return(p_result.getCell(varId[0]).getBiColumn().getEngName());
    					else
    						return("("+varId[1]+")");
    			case PERCENT_TOTAL:
    			case PERCENT_ROW:
    			case PERCENT_COL:
    					return("%");
    			default : return(aggregate.toString());
    			}
    					
    		}
    		public String getFormat(BiResult p_result) {
    			if(format == null) {
    				switch (aggregate) {
    				case COUNT:
    						return("##,##0");
    				case EXPRESSION:
    				case EXPRESSION2:
    				case SUM:
    				case FIRST:
    				case LAST:
    				case MAX:
    				case MIN: {
    					BiColumn bc = p_result.getCell(varId[0]).getBiColumn();
   						String fmt = bc.getFormat();
   						if (StringUtils.isNotBlank(fmt)) 
   							return(fmt); 
   						else
   							return StringUtils.equalsAny(bc.getColumnType(), "float", "double", "money") ? "###,###,##0.00" : "###,###,##0";
   					}
    				case PERCENT_TOTAL:
    				case PERCENT_ROW:
    				case PERCENT_COL:
    						return("##0.0");
    				}
    					
    			} 
    			return(format);
    		}
    		public String getWidth(BiResult p_result) {
    			if(width == null) {
    				switch (aggregate) {
    				case COUNT:
    						return("70px");
    				case EXPRESSION:
    				case EXPRESSION2:
    				case SUM: {
    						return("100px");
    					}
    				case MAX:
    				case MIN: {
    						return("100px");
    					}
    				case PERCENT_TOTAL:
    				case PERCENT_ROW:
    				case PERCENT_COL:
    						return("50px");
    				}
    					
    			} 
    			return(width);
    		}
    		
    		com.uniinformation.utils.exprpar.Parser getParser() {
    			return(parser);
    		}
    	}

    	protected ArrayList<ColRec> colsArr = null;
        protected ArrayList<RowRec> rowsArr = null;
        protected ArrayList<AggregateRec> aggsArr = null;
        protected String rptName = null;
//        private ArrayList<Comparable[]> colValsArr = null;
        ArrayList<String> aopList;
    	ArrayList<String> sortList;
        private int[] aopListOrder;
        AggregateOrPivotHeader aopHeader = null;
        
        
        public void setHeaderAggregateFirst(Boolean p_af) {
        	headerAggregateFirst = p_af;
        }
        
    	public AggregateOrPivot(String p_rptName,boolean p_UseNewPivotHeader) {
    		/*
    		{
    		  String s = Erpv4Config.getString(p_result.getSessionHelper(), "UseNewPivotHeader");
    		  if("Y".equals(s)) {
    			  useNewPivotHeader = true;
    		  }
    		}
    		*/
    		useNewPivotHeader = p_UseNewPivotHeader;
    		rptName = p_rptName;
    		colsArr = new ArrayList<ColRec>();
    		rowsArr = new ArrayList<RowRec>();
    		aggsArr = new ArrayList<AggregateRec>();
    		aopList = null;
//    		colValsArr = new ArrayList<Comparable[]>();
    	}
    	
    	public AggregateOrPivot addRow(String p_rowName) {
    		return(addRow(p_rowName,null,-1));
    	}
    	public AggregateOrPivot addRow(String p_rowName,String p_style) {
    		return(addRow(p_rowName,p_style,-1));
    	}
    	public AggregateOrPivot addRow(String p_rowName,String p_style,int p_idx) {
    		if(p_idx < 0) rowsArr.add(new RowRec(p_rowName,p_style)); else  rowsArr.add(p_idx, new RowRec(p_rowName,p_style));
    		return(this);
    	}
    	public AggregateOrPivot addCol(String p_colName) {
    		return(addCol(p_colName,null,-1));
    	}
    	public AggregateOrPivot addCol(String p_colName,String p_style) {
    		return(addCol(p_colName,p_style,-1));
    	}
    	public AggregateOrPivot addCol(String p_colName,String p_style,int p_idx) {
    		if(p_idx < 0) colsArr.add(new ColRec(p_colName,p_style)); else  colsArr.add(p_idx, new ColRec(p_colName,p_style));
    		return(this);
    	}
    	public AggregateOrPivot addAggregate(AGGREGATES p_aggregate,String... p_args) {
    		return addAggregate((p_args != null && p_args.length > 0 && p_args[0] != null) ? p_args[0] : null,p_aggregate,null,null,null,-1,p_args);
    	}
    	AggregateOrPivot addAggregate(String p_aggid ,AGGREGATES p_aggregate,String p_format,String p_width,String p_style,int p_idx,String... p_args) {
    		AggregateRec aggrec = new AggregateRec(p_aggid,p_aggregate,p_format,p_width,p_style,p_args); 
    		if(p_aggregate == AGGREGATES.EXPRESSION
    		   || p_aggregate == AGGREGATES.EXPRESSION2) {
    			aggrec.parser = new com.uniinformation.utils.exprpar.Parser(0,p_args[1]);
    		}
    		if(p_idx < 0) 
    			aggsArr.add(aggrec);
    		else  
    			aggsArr.add(p_idx, aggrec);
    		aggrec.colValsArr = new ArrayList<Comparable[]>();
    		return(this);
    	}
    	
    	/*
    	public AggregateOrPivot addAggregate(AGGREGATES p_aggregate,String p_args[]) {
    		aggsArr.add(new AggregateRec(null,p_aggregate,null,null,null,p_args));
    		return(this);
    	}
    	*/
    	public AggregateOrPivot hideRow(String p_rowId,boolean p_sw) {
    		for(RowRec rec : rowsArr) {
    			if(rec.getId().equals(p_rowId)) {
    				rec.hided = p_sw;
    			}
    		}
    		return(this);
    	}
    	public AggregateOrPivot hideCol(String p_colId,boolean p_sw) {
    		for(ColRec rec : colsArr) {
    			if(rec.getId().equals(p_colId)) {
    				rec.hided = p_sw;
    			}
    		}
    		return(this);
    	}
    	public AggregateOrPivot hideAggregate(String p_aggName,boolean p_sw) {
    		for(AggregateRec rec : aggsArr) {
    			if(rec.getKey().equals(p_aggName)) {
    				rec.hided = p_sw;
    			}
    		}
    		return(this);
    	}

//    	public int addAggregateOrPivotList(String s)
//    	{
//    		if(aopList == null) aopList = new ArrayList<String>();
//    		aopList.add(s);
//    		return(aopList.size()-1);
//    	}
    	String makePivotColumnsName(Comparable o[]) {
    		String s = null;
    		for(int i=0;i<o.length;i++) {
    			if(s == null ) s=(o[i] == null ? null : o[i].toString()); else s += "," + o[i];
    		}
    		return(s);
    	}
    	
    	ArrayList<String> aggregateNameList(BiResult p_result) {
    		ArrayList<String> nl = new ArrayList<String>();
   			for(int i=0;i<aggsArr.size();i++) {
   				nl.add(aggsArr.get(i).getName(p_result));
   			}
    		return(nl);
    	}
    	public void makeAggregateOrPivotList(BiResult p_result,boolean p_sort)
    	{
    		aopList = new ArrayList<String>();
    		if(colsArr.size() <= 0) {
    			for(AggregateRec ag : aggsArr) {
    				aopList.add(ag.getName(p_result));
    			}
    			if(useNewPivotHeader) aopHeader = new AggregateOrPivotHeader(this,aggregateNameList(p_result),null,headerAggregateFirst == null ? true : headerAggregateFirst);
    		} else if(aggsArr.size() <= 1) {
    			for(Comparable o[] : aggsArr.get(0).colValsArr) {
    				aopList.add(makePivotColumnsName(o));
    			}
    			if(useNewPivotHeader) aopHeader = new AggregateOrPivotHeader(this,aggregateNameList(p_result),aggsArr.get(0).colValsArr,headerAggregateFirst == null ? true : headerAggregateFirst);
    		} else {
    			/* assume all aggregates has same pivot columns */
    			for(Comparable o[] : aggsArr.get(0).colValsArr) {
    				String s = makePivotColumnsName(o);
    				for(AggregateRec ag : aggsArr) {
    					aopList.add(s + ":" + ag.getName(p_result));
    				}
    			}
    			if(useNewPivotHeader) aopHeader = new AggregateOrPivotHeader(this,aggregateNameList(p_result),aggsArr.get(0).colValsArr,headerAggregateFirst == null ? false : headerAggregateFirst);
    		}
    		if(!useNewPivotHeader && p_sort && aopList.size() > 1 ) {
    			aopListOrder = new int[aopList.size()];
    			sortList = (ArrayList<String>) aopList.clone();
    			Collections.sort(sortList);
    			for(int i=0;i<aopListOrder.length;i++) {
    				aopListOrder[i] = sortList.indexOf(aopList.get(i));
    			}
    		} else {
    			sortList = aopList;
    			aopListOrder = null;
    		}
    	}
    	public List<String>getAggregateOrPivotList()
    	{
//    		return(aopList);
    		if(useNewPivotHeader) {
    			return(aopHeader.getAggregateOrPivotList());
    		} else {
    			return(sortList);
    		}
    	}
    	
    	JSONObject addOneJsonRow(BiResult result , int i, int nAgg,int nCol) throws Exception {
    		JSONObject jo = new JSONObject();
    		for(RowRec rr : rowsArr) {
    			jo.put(rr.getName(result),result.getColumnValueFromCacheV(rr.getId(),i).toString());
    		}
    		JSONArray datas = new JSONArray();
    		Object[] dd = result.getAggregateValues(i);
    		/*
    		for(int j=0;j<n;j++) {
    			JSONArray dataset = new JSONArray();
    			if(j < n && dd[j] != null) {
    				dataset.put(dd[j]);
    			} else {
			 		dataset.put(0.0);
    			}
    			datas.put(dataset);
    		}
    		*/
    		for(int j=0;j<nCol;j++) {
    			JSONArray dataset = new JSONArray();
    			for(int k=0;k<nAgg;k++) {
    				int n = j * nAgg + k;
    				if(dd.length <= n || dd[n] == null) {
    					dataset.put(0.0);
    				} else {
    					dataset.put(dd[n]);
    				}
    			}
    			datas.put(dataset);
    		}
    		
    		jo.put("datas", datas);
    		return(jo);
    	}
    	public JSONObject toJson(BiResult result,List<Integer> trList) throws Exception {
    		JSONObject jsonAnalysedData = new JSONObject();
    		JSONArray jsonAggsArr = new JSONArray();
    		JSONArray jsonRowsArr = new JSONArray();
    		JSONArray jsonColsArr = new JSONArray();
    		JSONArray jsonColValsArr = new JSONArray(); 
    		JSONArray jsonRowValsArr = new JSONArray(); 
    		for(AggregateRec rec:aggsArr) {
    			if(rec.hided) continue;
    			JSONObject jo;
    			JSONArray ja;
    			jo = new JSONObject();
    			jo.put("aggregate", rec.aggregate.name());// remember : use .name() instead of using the emum value for easier debug
    			jo.put("name", rec.getName(result));
    			jo.put("key", rec.getKey());
    			if(rec.getFormat(result) != null) jo.put("format", rec.getFormat(result));
    			if(rec.getWidth(result) != null) jo.put("width", rec.getWidth(result));
			 	ja = new JSONArray();
			 	for(String val : rec.varId) {
			 		ja.put(val);
			 	}
			 	jo.put("vals", ja);
			 	jsonAggsArr.put(jo);
    		}
    		for(RowRec rec:rowsArr) {
    			if(!rec.hided) jsonRowsArr.put(rec.getName(result));
    		}
    		for(ColRec rec:colsArr) {
    			if(!rec.hided) jsonColsArr.put(rec.getName(result));
    		}

    		/* probable got null pointer exception if no pivot table, in this case , colValsArr is not initialized, should handle this case in future */
    		/* DT 2023/03/09 */
    		
    		for(Comparable o[] : aggsArr.get(0).colValsArr) {
    			JSONObject jo = new JSONObject();
    			for(int i=0;i<o.length;i++) {
    				jo.put(colsArr.get(i).getName(result),o[i] != null ? o[i].toString() : "");
    			}
    			jsonColValsArr.put(jo);
    		}
    		if(jsonColValsArr.length() <= 0) {
    			jsonColValsArr.put(new JSONObject());
    		}
//    		int n = aopList.size();
    		int nAgg = aggsArr.size();
    		int nCol = jsonColValsArr.length();
    		if(trList != null) {
    			for(int i : trList) {
    				jsonRowValsArr.put( addOneJsonRow(result , i, nAgg,nCol));
    			}
    			
    		} else {
    		for(int i=0;i<result.getRowCount();i++) {
    			jsonRowValsArr.put( addOneJsonRow(result , i, nAgg,nCol));
    			/*
    			JSONObject jo = new JSONObject();
    			for(RowRec rr : rowsArr) {
    				jo.put(rr.getName(result),result.getColumnValueFromCacheV(rr.getKey(),i).toString());
    			}
    			JSONArray datas = new JSONArray();
    			Double[] dd = result.getAggregateValues(i);
    			for(int j=0;j<n;j++) {
    				JSONArray dataset = new JSONArray();
    				if(j < n && dd[j] != null) {
    					dataset.put(dd[j]);
    				} else {
    					dataset.put(0.0);
    				}
    				datas.put(dataset);
    			}
    			jo.put("datas", datas);
    			jsonRowValsArr.put(jo);
    			*/
    		}
    		}
    		jsonAnalysedData.put("Aggregates", jsonAggsArr);
    		jsonAnalysedData.put("RowHeaders", jsonRowsArr);
    		jsonAnalysedData.put("ColHeaders", jsonColsArr);
    		jsonAnalysedData.put("Columns", jsonColValsArr);
    		jsonAnalysedData.put("Rows", jsonRowValsArr);
    		return(jsonAnalysedData);
    	}
    	
    	public List<String> getRowColumnIds() {
    		ArrayList<String> l = new ArrayList<String>();
    		for(RowRec r : rowsArr) {
    			l.add(r.getId());
    		}
    		return(l);
    	}
    	public List<String> getColColumnIds() {
    		ArrayList<String> l = new ArrayList<String>();
    		for(ColRec r : colsArr) {
    			l.add(r.getId());
    		}
    		return(l);
    	}
    	public int addOrGetPivotList(Object p_o[]) throws Exception {
    		for(int i=0;i<aggsArr.get(0).colValsArr.size();i++) {
    			boolean matched = true;
    			for(int j = 0;j<p_o.length;j++) {
    				if(p_o[j] == null && aggsArr.get(0).colValsArr.get(i)[j] != null) {
    					matched = false;
    					break;
    				}
    				if(p_o[j] != null && aggsArr.get(0).colValsArr.get(i)[j] == null) {
    					matched = false;
    					break;
    				}
    				Object pivotCol;
    				if(p_o[j] instanceof Double) {
    					pivotCol = p_o[j].toString();
    				} else if(p_o[j] instanceof Integer) {
    					pivotCol = p_o[j].toString();
    				} else {
    					pivotCol = p_o[j];
    				}
    				if(! (aggsArr.get(0).colValsArr.get(i)[j]).equals(pivotCol)) {
    					matched = false;
    					break;
    				}
    			}
    			if(matched) return(i);
    		}
    		UniLog.log("Number of pivot column " + aggsArr.get(0).colValsArr.size());
    		if(aggsArr.get(0).colValsArr.size() >= maxValCol) {
    			throw new Exception("Too May Data Column");
    		}
    		
    		Comparable oo[] = new Comparable[p_o.length];
    		for(int j = 0;j<p_o.length;j++) {
    			oo[j] = p_o[j] == null ? null : ( p_o[j] instanceof CellPair ? ((CellPair)p_o[j]): new CellPair(0,p_o[j].toString()));
    		}
    		aggsArr.get(0).colValsArr.add(oo);
    		return(aggsArr.get(0).colValsArr.size()-1);
    	}
    	public void reset()
    	{
//    		colValsArr = new ArrayList<Comparable[]>();
    		aopList = new ArrayList();
    		aopHeader = null;
    	}
    	
    	public AggregateRec getAggregate(int p_idx) {
    		return(aggsArr.get(p_idx));
    	}
    	
    	public int[] getAopListOrder() {
    		if(useNewPivotHeader) {
    			return(aopHeader.getAopListOrder());
    		} else {
    			return(aopListOrder);
    		}
    	}
    	
    	public AggregateOrPivotHeader getAggregateOrPivotHeader() {
    		return(aopHeader);
    	}
    	
    	public boolean hasPivotSubtotal() {
    		return(hasPivotSubtotal);
    	}
    	
    	public void setPivotSubtotal(boolean p_sw) {
    		if(useNewPivotHeader) hasPivotSubtotal = p_sw; else hasPivotSubtotal = false;
    	}
}
