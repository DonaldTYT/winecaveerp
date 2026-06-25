package com.uniinformation.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiField;
import com.uniinformation.utils.whereclpar.Expression;
import com.uniinformation.webcore.SessionHelper;

public class ConditionPresets {
    private ConditionFieldMapMap mConditionPresetMap = new ConditionFieldMapMap();
    public static class ConditionField {
    	public BiColumn bc;
    	public String label = "", fdName = "", fdLabel = "";
    	public String operator = "";
    	public Object value1;
    	public Object value2;
    	public String conditionString;
    	public List<ConditionField> orConditionFields = new ArrayList<ConditionField>();
	    public static ConditionField parseConditionString(String p_condition) {
	    	final Pattern[] regs = {
	   			Pattern.compile("^(\\w+\\.\\w+|\\w+) *(=|>|<|<>|>=|<=) *(-?\\d*\\.?\\d+)$"),
	   			Pattern.compile("^(\\w+\\.\\w+|\\w+) *(=|>|<|<>|>=|<=) *'(.*)'$"),
	   			Pattern.compile("^(\\w+\\.\\w+|\\w+) *(=|>|<|<>|>=|<=) *\"(.*)\"$"),
	   			Pattern.compile("^(\\w+\\.\\w+|\\w+) +(?i)(is blank)$"),
	   			Pattern.compile("^(\\w+\\.\\w+|\\w+) +(?i)(matches) *'(.*)'$"),
	   			Pattern.compile("^(\\w+\\.\\w+|\\w+) +(?i)(matches) *\"(.*)\"$"),
	   			Pattern.compile("^(\\w+\\.\\w+|\\w+) +(?i)(between) +(-?\\d*\\.?\\d+) +and +(-?\\d*\\.?\\d+)$"),
	   			Pattern.compile("^(\\w+\\.\\w+|\\w+) +(?i)(between) *'(.*)' *and *'(.*)'$"),
	   			Pattern.compile("^(\\w+\\.\\w+|\\w+) +(?i)(between) *\"(.*)\" *and *\"(.*)\"$"),
	   			Pattern.compile("^(\\w+\\.\\w+|\\w+) +(?i)(in|not in) *\\((.*)\\)$"),
	    	};
	    	for (Pattern reg : regs) {
	    		Matcher matcher = reg.matcher(p_condition);
	    		if (matcher.find()) {
	    			ConditionField cf = new ConditionField();
	    			cf.label = matcher.group(1);
	    			cf.operator = matcher.group(2);
	    			if (matcher.groupCount() >= 3)
	    				cf.value1 = matcher.group(3);
	    			if (matcher.groupCount() >= 4)
	    				cf.value2 = matcher.group(4);
	    			return cf;
	    		}
	    	}
	    	return null;
	    }
	    @Override
	    public boolean equals(final Object src) {
	    	ConditionField cf = (ConditionField) src;
	    	if (StringUtils.equals(cf.label, label)
	    			&& StringUtils.equals(cf.fdName, fdName)
	    			&& StringUtils.equals(cf.fdLabel, fdLabel)
	    			&& StringUtils.equals(cf.operator, operator)
	    			&& ObjectUtils.equals(cf.value1, value1)
	    			&& ObjectUtils.equals(cf.value2, value2)
	    			) {
	    		if (cf.orConditionFields.size() != orConditionFields.size())
	    			return false;
	    		for (int i = 0; i < orConditionFields.size(); i++) {
	    			if (!ObjectUtils.equals(orConditionFields.get(i), cf.orConditionFields.get(i)))
	    				return false;
	    		}
	    		return true;
	    	}
	    	else
	    		return false;
	    }
   		private String realFieldName() {
//   			return parent.bc.getField() != null ? parent.bc.getField().getFullName() : parent.bc.getLabel();
   			return bc.getLabel();
   		}
   		public void setBc(BiColumn bc) {
   			this.bc = bc;
   			for (ConditionField cf : orConditionFields)
   				cf.bc = bc;
   		}
   		public ConditionField clone() {
   			ConditionField cf = new ConditionField();
   			cf.bc = bc;
   			cf.label = label;
   			cf.fdName = fdName;
   			cf.fdLabel = fdLabel;
   			cf.operator = operator;
   			cf.value1 = value1;
   			cf.value2 = value2;
   			cf.conditionString = conditionString;
   			cf.orConditionFields.addAll(orConditionFields);
   			return cf;
   		}
	    public void makeConditionString() {
	    	if (bc == null)
	    		return;
			if(
				StringUtils.equals(operator, "=") ||
				StringUtils.equals(operator, "<>") ||
				StringUtils.equals(operator, ">=") ||
				StringUtils.equals(operator, "<=") ||
				StringUtils.equals(operator, ">") ||
				StringUtils.equals(operator, "<")
				) {
				if (value1 == null)
					return;
				conditionString = String.format("%s %s '%s'", realFieldName(), operator, Expression.escapeStr(value1.toString()));
			}
			if( StringUtils.equals(operator, "in") ||
				StringUtils.equals(operator, "not in")) {
				if (value1 == null)
					return;
				conditionString = String.format("%s %s (%s)", realFieldName(), operator, value1.toString());
			}
			if( StringUtils.equals(operator, "matches")) {
				if (value1 == null)
					return;
				conditionString = String.format("%s %s '%s'", realFieldName(), operator, Expression.escapeStr(value1.toString()));
			}
			if( StringUtils.equals(operator, "between")) {
				if (value1 == null || value2 == null)
					return;
				conditionString = String.format("%s %s '%s' and '%s'", realFieldName(), operator, 
						Expression.escapeStr(value1.toString()), Expression.escapeStr(value2.toString()));
			}
			if (StringUtils.equals(operator, "is blank")) {
				conditionString = String.format("%s =''", realFieldName());
			}
			for (ConditionField cf : orConditionFields)
				cf.makeConditionString();
	    }
    }
    public static class ConditionFieldMap {
    	private int maxKey;
    	private Map<String, Map<String, Map<String, Integer>>> keyMap = new HashMap<String, Map<String, Map<String, Integer>>>();
    	private Map<Integer, ConditionField> valueMap = new HashMap<Integer, ConditionField>();
    	private String customCondition;

		private final boolean isCustom;
		private String accessKeyForPublic;
		private boolean isDefault, isDisplayRecCnt;
		private int orderby;
		private List<Integer> orderbys;
		private List<Integer> hideCols;
		private Pair<String, Boolean> orderbyLabel;
		private List<Pair<String, Boolean>> orderbyLabels; //label, false:asc | true:desc
		private List<String> hideColLabels;
		private List<String> pivotColLabels;
		private int recordLimit;
	    private List<String> hideAggregates;
	    private List<String> columnOrders;
		private List<String> adhocColumns;
		private List<String> noembedcolumn;
		private JSONObject generalReportSettingData;
		private int frozenCount;
		
		private SessionHelper sessionHelper;

    	public ConditionFieldMap(boolean isCustom, SessionHelper sessionHelper) {
    		this.isCustom = isCustom;
    		this.sessionHelper = sessionHelper;
    	}
    	public void setAccessKeyForPublic(String key) {
    		accessKeyForPublic = key;
    	}
    	public void setDefault(boolean dft) {
    		isDefault = dft;
    	}
    	private void setDisplayRecCnt(boolean b) {
    		isDisplayRecCnt = b;
    	}
    	public void setGeneralReportSettingData(JSONObject data) {
    		generalReportSettingData = data;
    	}
    	public void setOrderby(int orderby) {
    		this.orderby = orderby;
     	}
    	public void setOrderbys(List<Integer> orderbys) {
    		this.orderbys = orderbys;
    	}
    	public void setHideCols(List<Integer> hideCols) {
    		this.hideCols = hideCols;
    	}
    	public void setOrderbyLabel(Pair<String, Boolean> p) {
    		this.orderbyLabel = p;
     	}
    	public void setOrderbyLabels(List<Pair<String, Boolean>> orderbys) {
    		this.orderbyLabels = orderbys;
    	}
    	public void setHideColLabels(List<String> hideCols) {
    		this.hideColLabels = hideCols;
    	}
    	public void setPivotColLabels(List<String> pivotCols) {
    		this.pivotColLabels = pivotCols;
    	}
    	public void setHideAggregates(List<String> p_hideAggregates) {
    		this.hideAggregates = p_hideAggregates;
    	}
    	public void setAdhocColumns(List<String> p_adhocColumns) {
    		this.adhocColumns = p_adhocColumns;
    	}
    	public void setColumnOrders(List<String> p_columnOrders) {
    		this.columnOrders = p_columnOrders;
    	}
    	public void setNoembedcolumn(List<String> noembedcolumn) {
    		this.noembedcolumn = noembedcolumn;
    	}

    	public boolean isDefault() {
    		return isDefault;
    	}
    	public boolean isCustom() {
    		return isCustom;
    	}
    	public String getAccessKeyForPublic() {
    		return accessKeyForPublic;
    	}
    	public boolean canAccess() {
    		return !sessionHelper.getAllowPresetAccessKey() || isCustom || sessionHelper.isAdminUser() || StringUtils.isBlank(accessKeyForPublic) || sessionHelper.hasAccessRight(accessKeyForPublic);
    	}
    	public boolean isDisplayRecCnt() {
    		return isDisplayRecCnt;
    	}
    	public int getOrderby() {
    		return orderby;
    	}
    	public List<Integer> getOrderbys() {
    		return orderbys;
    	}
    	public List<Integer> getHideCols() {
    		return hideCols;
    	}
    	public Pair<String, Boolean> getOrderbyLabel() {
    		return orderbyLabel;
    	}
    	public List<Pair<String, Boolean>> getOrderbyLabels() {
    		return orderbyLabels;
    	}
    	public List<String> getHideColLabels() {
    		return hideColLabels;
    	}
    	public List<String> getPivotColLabels() {
    		return pivotColLabels;
    	}
    	public int getRecordLimit() {
    		return recordLimit;
    	}
    	public int getFrozenCount() {
    		return frozenCount;
    	}
    	public void setRecordLimit(int limit) {
    		recordLimit = limit;
    	}
    	public void setFrozenCount(int p_count) {
    		frozenCount = p_count;
    	}
    	public JSONObject getGeneralReportSettingData() {
    		return generalReportSettingData;
    	}
    	public void setCustomCondition(String customCondition) {
    		this.customCondition = customCondition;
    	}
    	public String getCustomCondition() {
    		return customCondition;
    	}
	    public List<String> getHideAggregates() {
	    	return(hideAggregates);
	    }
	    public List<String> getAdhocColumns() {
	    	return(adhocColumns);
	    }
	    public List<String> getColumnOrders() {
	    	return(columnOrders);
	    }
	    public List<String> getNoembedcolumn() {
	    	return noembedcolumn;
	    }
    	@Override
    	public boolean equals(final Object src) {
    		ConditionFieldMap cfm = (ConditionFieldMap) src;
    		if (valueMap.size() != cfm.valueMap.size())
    			return false;
    		for (Map.Entry<Integer, ConditionField> entry : valueMap.entrySet()) {
    			if (!ObjectUtils.equals(entry.getValue(), cfm.valueMap.get(entry.getKey())))
    				return false;
    		}
    		return true;
    	}
    	public void clear() {
    		valueMap.clear();
    		keyMap.clear();
    		maxKey = 0;
    	}
    	public ConditionField put(String label, String fdName, String fdLabel, ConditionField value) {
    		Map<String, Map<String, Integer>> mm = keyMap.get(label);
    		if (mm == null)
    			keyMap.put(label, mm = new HashMap<String, Map<String, Integer>>());
    		Map<String, Integer> m = mm.get(fdName);
    		if (m == null)
    			mm.put(fdName, m = new HashMap<String, Integer>());
    		Integer key = m.get(fdLabel);
    		if (key == null)
    			m.put(fdLabel, key = ++maxKey);
    		return valueMap.put(key, value);
    	}
    	private Integer getKey(String label, String fdName, String fdLabel) {
    		Map<String, Map<String, Integer>> mm = keyMap.get(label);
    		if (mm == null)
    			return null;
    		Map<String, Integer> m = mm.get(fdName);
    		if (m == null)
    			return null;
    		return m.get(fdLabel);
    	}
    	public ConditionField get(String label, BiField fd, String fdLabel) {
    		String fdName = fd != null ? fd.getFullName() : "";
    		return get(label, fdName, fdLabel);
    	}
    	private ConditionField get1(String label, String fdName, String fdLabel) {
    		Integer key = getKey(label, fdName, fdLabel);
    		return key != null ? valueMap.get(key) : null;
    	}
    	public ConditionField get(String label, String fdName, String fdLabel) {
    		ConditionField result = get1(label, fdName, fdLabel);
    		if (result == null && (!StringUtils.isEmpty(fdName) || !StringUtils.isEmpty(fdLabel)))
    			return get1(label, "", "");
    		return result;
    	}
		private boolean containsKey1(String label, String fdName, String fdLabel) {
    		Integer key = getKey(label, fdName, fdLabel);
    		return key != null ? valueMap.containsKey(key) : false;
		}
		public boolean containsKey(String label, String fdName, String fdLabel) {
			boolean result = containsKey1(label, fdName, fdLabel);
			if (!result && (!StringUtils.isEmpty(fdName) || !StringUtils.isEmpty(fdLabel)))
				return containsKey1(label, "", "");
			return result;
		}
		public Collection<ConditionField> values() {
			return valueMap.values();
		}
		public ConditionField remove(String label, String fdName, String fdLabel) {
    		Integer key = getKey(label, fdName, fdLabel);
    		return key != null ? valueMap.remove(key) : null;
		}
		public JSONObject toJson(String preset) throws Exception {
			JSONObject jsonItems = new JSONObject();
			jsonItems.put("preset", preset);
			jsonItems.put("isdefault", isDefault);
			jsonItems.put("isdisplayreccnt", isDisplayRecCnt);
			jsonItems.put("accesskeyforpublic", accessKeyForPublic);
			jsonItems.put("orderby", orderby);
			jsonItems.put("customcondition", StringUtils.defaultIfEmpty(customCondition, ""));
			jsonItems.put("recordLimit", recordLimit);
			jsonItems.put("frozenCount", frozenCount);
			if(hideCols != null) {
				JSONArray hideColArr = new JSONArray();
				for(Integer hc : hideCols) {
					JSONObject jhc = new JSONObject();
					jhc.put("colidx", hc);
					hideColArr.put(jhc);
				}
				jsonItems.put("hideCols", hideColArr);
			}
			if(orderbys != null) {
				JSONArray orderbyArr = new JSONArray();
				for(int orderby : orderbys)
					orderbyArr.put(orderby);
				jsonItems.put("orderbys", orderbyArr);
			}
			if (orderbyLabel != null)
				jsonItems.put("orderbyLabel", orderbyLabel);
			if(hideColLabels != null) {
				JSONArray hideCols = new JSONArray();
				for(String hc : hideColLabels)
					hideCols.put(hc);
				jsonItems.put("hideColLabels", hideCols);
			}
			if(pivotColLabels != null) {
				JSONArray pivotCols = new JSONArray();
				for(String hc : pivotColLabels)
					pivotCols.put(hc);
				jsonItems.put("pivotColLabels", pivotCols);
			}
			if(hideAggregates != null) {
				JSONArray hideAggCols = new JSONArray();
				for(String hc : hideAggregates)
					hideAggCols.put(hc);
				jsonItems.put("hideAggregates", hideAggCols);
			}
			if(adhocColumns != null && adhocColumns.size() > 0) {
				JSONArray jAdhocColumns = new JSONArray();
				for(String ach : adhocColumns)
					jAdhocColumns.put(new JSONObject(ach));
				jsonItems.put("adhocColumns", jAdhocColumns);
			}
			if(columnOrders != null) {
				JSONArray jColumnOrders = new JSONArray();
				for(String hc : columnOrders)
					jColumnOrders.put(hc);
				jsonItems.put("columnOrders", jColumnOrders);
			}
			if(orderbyLabels != null) {
				JSONArray orderbys = new JSONArray();
				for(Pair<String, Boolean> orderby : orderbyLabels) {
					JSONObject json = new JSONObject();
					json.put("colLabel", orderby.getLeft());
					json.put("isDesc", orderby.getRight());
					orderbys.put(json);
				}
				jsonItems.put("orderbyLabels", orderbys);
			}
			if (noembedcolumn != null) {
				JSONArray cols = new JSONArray();
				for (String col : noembedcolumn)
					cols.put(col);
				jsonItems.put("noembedcolumn", cols);
			}
			if (generalReportSettingData != null)
				jsonItems.put("generalreportsettingdata", generalReportSettingData);
			JSONArray fields = new JSONArray();
			for (ConditionField cf : values()) {
				JSONObject jsonItem = new JSONObject();
				jsonItem.put("name", cf.label);
				jsonItem.put("fd", cf.fdName);
				jsonItem.put("label", cf.fdLabel);
				if(cf.bc != null) jsonItem.put("colname",cf.bc.getLabel());
				if(cf.bc != null && cf.bc.getField() != null) {
					jsonItem.put("dbtable",cf.bc.getField().getTable().getName());
					jsonItem.put("dbfield",cf.bc.getField().getName());
				}
				jsonItem.put("op", cf.operator);
				if (cf.value1 != null)
					jsonItem.put("val1", cf.value1);
				if (cf.value2 != null)
					jsonItem.put("val2", cf.value2);
				JSONArray or_fields = new JSONArray();
				for (ConditionField cf1 : cf.orConditionFields) {
					JSONObject jsonItem1 = new JSONObject();
					jsonItem1.put("op", cf1.operator);
					if (cf1.value1 != null)
						jsonItem1.put("val1", cf1.value1);
					if (cf1.value2 != null)
						jsonItem1.put("val2", cf1.value2);
					or_fields.put(jsonItem1);
				}
				if (or_fields.length() > 0)
					jsonItem.put("or_fields", or_fields);
				fields.put(jsonItem);
			}
			jsonItems.put("fields", fields);
			return jsonItems;
		}
    }
    private static class ConditionFieldMapMap extends LinkedHashMap<String, ConditionFieldMap> {
		private static final long serialVersionUID = -7470930816561513629L;
		private boolean isFromUrl;
		private String customStoreKey;
    	private String customStoreName;
    	private String customStoreDesc;
		private String publicStoreKey;
    	private String publicStoreName;
    	private String publicStoreDesc;
    	@Override
    	public void clear() {
    		super.clear();
    		isFromUrl = false;
    		customStoreKey = null;
    		customStoreName = null;
    		customStoreDesc = null;
    		publicStoreKey = null;
    		publicStoreName = null;
    		publicStoreDesc = null;
    	}
    }
    public boolean isFromUrl() {
    	return mConditionPresetMap.isFromUrl;
    }
    public String getDefaultPreset() {
    	Map<String, String> m = getAllDefaultPreset();
    	return m.get("custom") != null ? m.get("custom") : m.get("public");
    }
    public Map<String, String> getAllDefaultPreset() {
    	String publicPreset = null, customPreset = null;
    	for (Map.Entry<String, ConditionFieldMap> entry : mConditionPresetMap.entrySet()) {
    		String preset = entry.getKey();
    		ConditionFieldMap map = entry.getValue();
    		if (map.isDefault()) {
    			if (map.isCustom)
    				customPreset = preset;
    			else if (map.canAccess())
    				publicPreset = preset;
    		}
    	}
    	Map<String, String> m = new HashMap<String, String>();
    	m.put("public", publicPreset);
    	m.put("custom", customPreset);
    	return m;
    }
    public void setDefaultPreset(String preset, boolean isDefault) {
    	if (isDefault) {
    		boolean isCustom = mConditionPresetMap.get(preset).isCustom();
    		for (Map.Entry<String, ConditionFieldMap> entry : mConditionPresetMap.entrySet()) {
    			String p = entry.getKey();
    			ConditionFieldMap m = entry.getValue();
    			if (m.isCustom() == isCustom)
    				m.setDefault(p.equals(preset));
    		}
    	} else
    		mConditionPresetMap.get(preset).setDefault(false);
    }
    public String getDisplayRecCntPreset() {
    	for (Map.Entry<String, ConditionFieldMap> entry : mConditionPresetMap.entrySet()) {
    		String preset = entry.getKey();
    		ConditionFieldMap map = entry.getValue();
    		if (map.isDisplayRecCnt())
    			return preset;
    	}
    	return null;
    }
    public void setDisplayRecCntPreset(String preset, boolean isDisplay) {
    	if (isDisplay) {
    		for (Map.Entry<String, ConditionFieldMap> entry : mConditionPresetMap.entrySet()) {
    			String p = entry.getKey();
    			ConditionFieldMap m = entry.getValue();
  				m.setDisplayRecCnt(p.equals(preset));
    		}
    	} else
    		mConditionPresetMap.get(preset).setDisplayRecCnt(false);
    }
    public boolean hasPresets() {
    	return !mConditionPresetMap.isEmpty();
    }
    public List<String> getPresets() {
    	//return mConditionPresetMap.keySet();
    	List<String> list = new ArrayList<String>();
    	for (Map.Entry<String, ConditionFieldMap> entry : mConditionPresetMap.entrySet()) {
    		if (entry.getValue().canAccess())
    			list.add(entry.getKey());
    	}
    	list.sort(new Comparator<String>() {
			@Override
			public int compare(String a, String b) {
				ConditionFieldMap ca = mConditionPresetMap.get(a);
				ConditionFieldMap cb = mConditionPresetMap.get(b);
				if (ca.isCustom && !cb.isCustom)
					return -1;
				else if (!ca.isCustom && cb.isCustom)
					return 1;
				else
					return a.compareTo(b);
			}
    	});
    	return list;
    }
    public boolean containsPreset(String preset) {
    	return mConditionPresetMap.containsKey(preset);
    }
    public ConditionFieldMap getFieldMap(String preset) {
    	return mConditionPresetMap.get(preset);
    }
    public void putFieldMap(String preset, final ConditionFieldMap fieldMap) {
    	mConditionPresetMap.put(preset, fieldMap);
    }
    public void removeFieldMap(String preset) {
    	mConditionPresetMap.remove(preset);
    }
    public String getCustomStoreKey() {
    	return mConditionPresetMap.customStoreKey;
    }
    public void setCustomStoreKey(String customStoreKey) {
    	mConditionPresetMap.customStoreKey = customStoreKey;
    }
    public String getCustomStoreName() {
    	return mConditionPresetMap.customStoreName;
    }
    public void setCustomStoreName(String customStoreName) {
    	mConditionPresetMap.customStoreName = customStoreName;
    }
    public String getCustomStoreDesc() {
    	return mConditionPresetMap.customStoreDesc;
    }
    public void setCustomStoreDesc(String customStoreDesc) {
    	mConditionPresetMap.customStoreDesc = customStoreDesc;
    }
    public String getPublicStoreKey() {
    	return mConditionPresetMap.publicStoreKey;
    }
    public void setPublicStoreKey(String publicStoreKey) {
    	mConditionPresetMap.publicStoreKey = publicStoreKey;
    }
    public String getPublicStoreName() {
    	return mConditionPresetMap.publicStoreName;
    }
    public void setPublicStoreName(String publicStoreName) {
    	mConditionPresetMap.publicStoreName = publicStoreName;
    }
    public String getPublicStoreDesc() {
    	return mConditionPresetMap.publicStoreDesc;
    }
    public void setPublicStoreDesc(String publicStoreDesc) {
    	mConditionPresetMap.publicStoreDesc = publicStoreDesc;
    }
    public boolean parseConditionPreset(String jsonContent, boolean isCustom, SessionHelper sessionHelper) {
    	try {
    		JSONArray jsonArray = new JSONArray(jsonContent);
    		parseConditionPreset(jsonArray, isCustom, sessionHelper);
    		return true;
    	}
    	catch (JSONException e) {
			UniLog.log("parseConditionPreset fail " + e.toString());
			return false;
    	}
    }
    public boolean parseConditionPreset(JSONArray jsonArray, boolean isCustom, SessionHelper sessionHelper) throws JSONException {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonItem = jsonArray.getJSONObject(i);
				String preset = jsonItem.getString("preset");
				JSONArray fields = jsonItem.getJSONArray("fields");
				ConditionFieldMap fieldMap = new ConditionFieldMap(isCustom, sessionHelper);
				if(jsonItem.has("isdefault")) {
					fieldMap.isDefault = jsonItem.getBoolean("isdefault");
				}
				if(jsonItem.has("isdisplayreccnt")) {
					fieldMap.isDisplayRecCnt = jsonItem.getBoolean("isdisplayreccnt");
				}
				if(jsonItem.has("accesskeyforpublic")) {
					fieldMap.accessKeyForPublic = jsonItem.getString("accesskeyforpublic");
				}
				if(jsonItem.has("generalreportsettingdata")) {
					fieldMap.generalReportSettingData = jsonItem.getJSONObject("generalreportsettingdata");
				}
				if(jsonItem.has("orderby")) {
					fieldMap.orderby = jsonItem.getInt("orderby");
				}
				if(jsonItem.has("orderbyLabel")) {
					JSONObject json = jsonItem.getJSONObject("orderbyLabel");
					fieldMap.orderbyLabel = Pair.of(json.getString("colLabel"), json.getBoolean("isDesc"));
				}
				if(jsonItem.has("hideCols")) {
					JSONArray hideCols = jsonItem.getJSONArray("hideCols");
					fieldMap.hideCols = new ArrayList<Integer>();
					for (int j = 0; j < hideCols.length(); j++) {
						JSONObject hideCol = hideCols.getJSONObject(j);
						fieldMap.hideCols.add(new Integer(hideCol.getInt("colidx")));
					}
				}
				if(jsonItem.has("hideColLabels")) {
					JSONArray hideCols = jsonItem.getJSONArray("hideColLabels");
					fieldMap.hideColLabels = new ArrayList<String>();
					for (int j = 0; j < hideCols.length(); j++) {
						String hideCol = hideCols.getString(j);
						fieldMap.hideColLabels.add(hideCol);
					}
				}
				if(jsonItem.has("pivotColLabels")) {
					JSONArray pivotCols = jsonItem.getJSONArray("pivotColLabels");
					fieldMap.pivotColLabels = new ArrayList<String>();
					for (int j = 0; j < pivotCols.length(); j++) {
						String pivotCol = pivotCols.getString(j);
						fieldMap.pivotColLabels.add(pivotCol);
					}
				}
				if(jsonItem.has("hideAggregates")) {
					JSONArray hideAggCols = jsonItem.getJSONArray("hideAggregates");
					fieldMap.hideAggregates = new ArrayList<String>();
					for (int j = 0; j < hideAggCols.length(); j++) {
						String hideAggCol = hideAggCols.getString(j);
						fieldMap.hideAggregates.add(hideAggCol);
					}
				}
				if(jsonItem.has("adhocColumns")) {
					JSONArray jAdhocCols = jsonItem.getJSONArray("adhocColumns");
					fieldMap.adhocColumns = new ArrayList<String>();
					for (int j = 0; j < jAdhocCols .length(); j++) {
						String adhColStr = jAdhocCols.getJSONObject(j).toString();
						fieldMap.adhocColumns.add(adhColStr);
					}
				}
				if(jsonItem.has("columnOrders")) {
					JSONArray jColumnOrders = jsonItem.getJSONArray("columnOrders");
					fieldMap.columnOrders = new ArrayList<String>();
					for (int j = 0; j < jColumnOrders.length(); j++) {
						String colName = jColumnOrders.getString(j);
						fieldMap.columnOrders.add(colName);
					}
				}
				if(jsonItem.has("orderbys")) {
					JSONArray orderbys = jsonItem.getJSONArray("orderbys");
					fieldMap.orderbys = new ArrayList<Integer>();
					for (int j = 0; j < orderbys.length(); j++)
						fieldMap.orderbys.add(orderbys.getInt(j));
				}
				if(jsonItem.has("orderbyLabels")) {
					JSONArray orderbys = jsonItem.getJSONArray("orderbyLabels");
					fieldMap.orderbyLabels = new ArrayList<Pair<String, Boolean>>();
					for (int j = 0; j < orderbys.length(); j++) {
						JSONObject json = orderbys.getJSONObject(j);
						fieldMap.orderbyLabels.add(Pair.of(json.getString("colLabel"), json.getBoolean("isDesc")));
					}
				}
				if(jsonItem.has("noembedcolumn")) {
					JSONArray cols = jsonItem.getJSONArray("noembedcolumn");
					fieldMap.noembedcolumn = new ArrayList<>();
					for (int j = 0; j < cols.length(); j++)
						fieldMap.noembedcolumn.add(cols.getString(j));
				}
				if(jsonItem.has("customcondition")) {
					fieldMap.customCondition = jsonItem.getString("customcondition");
				}
				if(jsonItem.has("recordLimit")) {
					fieldMap.recordLimit = jsonItem.getInt("recordLimit");
				}
				if(jsonItem.has("frozenCount")) {
					fieldMap.frozenCount = jsonItem.getInt("frozenCount");
				}
				for (int j = 0; j < fields.length(); j++) {
					JSONObject jsonFieldsItem = fields.getJSONObject(j);
					ConditionField field = new ConditionField();
					field.label = jsonFieldsItem.getString("name");
					field.fdName = jsonFieldsItem.optString("fd");
					field.fdLabel = jsonFieldsItem.optString("label");
					field.operator = jsonFieldsItem.getString("op").toLowerCase();
					field.value1 = jsonFieldsItem.opt("val1");
					field.value2 = jsonFieldsItem.opt("val2");
					JSONArray orFields = jsonFieldsItem.optJSONArray("or_fields");
					for (int k = 0; orFields != null && k < orFields.length(); k++) {
						jsonFieldsItem = orFields.getJSONObject(k);
						ConditionField field1 = new ConditionField();
						field1.label = field.label;
						field1.fdName = field.fdName;
						field1.fdLabel = field.fdLabel;
						field1.operator = jsonFieldsItem.getString("op").toLowerCase();
						field1.value1 = jsonFieldsItem.opt("val1");
						field1.value2 = jsonFieldsItem.opt("val2");
						field.orConditionFields.add(field1);
					}
					fieldMap.put(field.label, field.fdName, field.fdLabel, field);
				}
				mConditionPresetMap.put(preset, fieldMap);
			}
			return true;
    /*
    	try {
		} 
    	catch (JSONException e) {
			UniLog.log("parseConditionPreset fail " + e.toString());
			return false;
		}
		*/
    }
    public synchronized void parseConditionPresets(final String urlKey, final String viewid, final SessionHelper sessionHelper) {
    	mConditionPresetMap.clear();
		String conditionPreset = null;
		if (StringUtils.isNotBlank(urlKey))
//			conditionPreset = Executions.getCurrent().getParameter(urlKey);
			conditionPreset = sessionHelper.getURLParam(urlKey);
		if (conditionPreset != null) {
			try {
 				conditionPreset = URLDecoder.decode(new String(conditionPreset.getBytes("ISO-8859-1"), "UTF-8"), "UTF-8");
 				parseConditionPreset(conditionPreset, false, sessionHelper);
  			} catch (UnsupportedEncodingException e) {
  				UniLog.log("url decode preset fail " + e.toString());
  			}
		}
		if (conditionPreset == null) {
			if (viewid == null)
				return;
			try {
				String key = String.format("zkbi_querypreset_%s_public", viewid.trim());
				ByteArrayOutputStream outStream = new ByteArrayOutputStream();
				FilingUtilObject fuo = FilingUtil.getFile(sessionHelper.getAgent(), null, key, outStream);
				UniLog.log1("parseConditionPresets key:%s, fuo:%s", key, fuo);
				if (fuo != null) {
					conditionPreset = outStream.toString("UTF-8");
					UniLog.log1("conditionPreset:%s", conditionPreset);
  					if (conditionPreset != null) {
	  					mConditionPresetMap.publicStoreKey = fuo.key;
	  					mConditionPresetMap.publicStoreName = fuo.name;
	  					mConditionPresetMap.publicStoreDesc = fuo.desc;
  						parseConditionPreset(conditionPreset, false, sessionHelper);
  					}
					UniLog.log("ConditionPresetMap key:" + key + ",conditionPreset:" + conditionPreset);
				}
				outStream.close();

				key = String.format("zkbi_querypreset_%s_custom_%s", viewid.trim(), sessionHelper.getLoginId());
				outStream = new ByteArrayOutputStream();
				fuo = FilingUtil.getFile(sessionHelper.getAgent(), null, key, outStream);
				UniLog.log1("parseConditionPresets key:%s, fuo:%s", key, fuo);
				if (fuo != null) {
					conditionPreset = outStream.toString("UTF-8");
					UniLog.log1("conditionPreset:%s", conditionPreset);
	  				if (conditionPreset != null) {
	  					mConditionPresetMap.customStoreKey = fuo.key;
	  					mConditionPresetMap.customStoreName = fuo.name;
	  					mConditionPresetMap.customStoreDesc = fuo.desc;
	  					parseConditionPreset(conditionPreset, true, sessionHelper);
	  				}
					UniLog.log("ConditionPresetMap key:" + key + ",conditionPreset:" + conditionPreset);
				}
				outStream.close();
			} catch (Exception e) {
  				UniLog.log("read zkbi_querypreset in database fail " + e.toString());
			}
		} else
			mConditionPresetMap.isFromUrl = true;
		
    	for (Map.Entry<String, ConditionFieldMap> entry : mConditionPresetMap.entrySet()) {
    		UniLog.log("ConditionPresetMap " + entry.getKey() + " isDefault " + entry.getValue().isDefault + " isDisplayRecCnt " + entry.getValue().isDisplayRecCnt);
    		for (Map.Entry<Integer, ConditionField> entry1 : entry.getValue().valueMap.entrySet()) {
    			UniLog.log("ConditionPresetMap " + entry.getKey() + " " + entry1.getKey() + " label:" 
    					+ entry1.getValue().label + " fdName:"
    					+ entry1.getValue().fdName + " fdLabel:"
    					+ entry1.getValue().fdLabel + " op:"
    					+ entry1.getValue().operator + " val1:"
    					+ entry1.getValue().value1 + " val2:"
    					+ entry1.getValue().value2
    					);
    		}
    	}
    }

    public synchronized String saveConditionPresets(final String viewid, final SessionHelper sessionHelper) {
    	return saveConditionPresets(viewid, null, true, true, sessionHelper);
    }

    public synchronized String saveConditionPresets(final String viewid, String userId, boolean needSavePublic, boolean needSaveCustom, final SessionHelper sessionHelper) {
    	UniLog.log1("saveConditionPresets viewid:%s, userId:%s, needSavePublic:%b, needSaveCustom:%b", viewid, userId, needSavePublic, needSaveCustom);
		String errMsg = null;
		if (userId == null)
			userId = sessionHelper.getLoginId();
    	if (mConditionPresetMap.customStoreKey == null) {
    		mConditionPresetMap.customStoreKey = String.format("zkbi_querypreset_%s_custom_%s", viewid.trim(), userId);
			mConditionPresetMap.customStoreName = mConditionPresetMap.customStoreKey;
			mConditionPresetMap.customStoreDesc = mConditionPresetMap.customStoreKey;
    	}
    	if (mConditionPresetMap.publicStoreKey == null) {
    		mConditionPresetMap.publicStoreKey = String.format("zkbi_querypreset_%s_public", viewid.trim());
			mConditionPresetMap.publicStoreName = mConditionPresetMap.publicStoreKey;
			mConditionPresetMap.publicStoreDesc = mConditionPresetMap.publicStoreKey;
    	}
		try {
			JSONArray jsonCustArray = new JSONArray();
			JSONArray jsonPubArray = new JSONArray();
			for (Map.Entry<String, ConditionFieldMap> entry : mConditionPresetMap.entrySet()) {
				String preset = entry.getKey();
				ConditionFieldMap conditionFieldMap = entry.getValue();
				JSONObject jsonItems = conditionFieldMap.toJson(preset);
				if (conditionFieldMap.isCustom)
					jsonCustArray.put(jsonItems);
				else
					jsonPubArray.put(jsonItems);
			}
			InputStream inStream;
			if (needSaveCustom) {
				inStream = new ByteArrayInputStream(jsonCustArray.toString().getBytes("UTF-8"));
				FilingUtil.storeFile(sessionHelper.getAgent(), null, mConditionPresetMap.customStoreKey, mConditionPresetMap.customStoreName, mConditionPresetMap.customStoreDesc, inStream);
				inStream.close();
				UniLog.log1("save custom userId:%s, key:%s, json:%s", userId, mConditionPresetMap.customStoreKey, jsonCustArray.toString());
			}
			if (needSavePublic && sessionHelper.isAdminUser()) {
				inStream = new ByteArrayInputStream(jsonPubArray.toString().getBytes("UTF-8"));
				FilingUtil.storeFile(sessionHelper.getAgent(), null, mConditionPresetMap.publicStoreKey, mConditionPresetMap.publicStoreName, mConditionPresetMap.publicStoreDesc, inStream);
				inStream.close();
				UniLog.log1("save public userId:%s, key:%s, json:%s", userId, mConditionPresetMap.publicStoreKey, jsonPubArray.toString());
			}
		} catch (Exception e) {
			errMsg = sessionHelper.getLabel("Save preset fall") + ": " + e.toString();
			UniLog.log(errMsg);
		}
		return errMsg;
    }
    public static String buildPresetLabelByKey(String keyString, String accessKey){
		if (keyString.startsWith("public_") && keyString.length() > 7){
			//return("Public - " + keyString.substring(7));
			if (StringUtils.isNotBlank(accessKey))
				accessKey = ":" + accessKey.trim();
			else
				accessKey = "";
			return(keyString.substring(7) + " (public" + accessKey + ")");
		}
		else if (keyString.startsWith("custom_") && keyString.length() > 7){
			//return("Custom - " + keyString.substring(7));
			return(keyString.substring(7) + " (custom)");
		}
		else{
			return(keyString);
		}
    }
}
