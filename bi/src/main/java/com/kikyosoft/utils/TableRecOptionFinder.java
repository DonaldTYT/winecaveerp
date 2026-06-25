package com.kikyosoft.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.CoreLog;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public abstract class TableRecOptionFinder extends OptionFinder {
	String key = null;
	static Hashtable<String,TableRec> trHash  = new Hashtable<String,TableRec>();
	public TableRecOptionFinder(SessionHelper p_sp,String p_key) {
		super();
		key = p_key;
	}
	
	abstract protected TableRec doQueryResult(SelectUtil su) throws Exception;
	
	public boolean compareOption(Object p_record, Condition p_cond) throws Exception{
		// TODO Auto-generated method stub
		Object o = ((TableRec) p_record).getField(options.get(p_cond.option));
		if(p_cond.operator == null) {
			if(p_cond.value1.equals(o)) return(true);
		}
		return(false);
	}

	protected Object getValueFromOption(TableRec tr,String p_id) throws Exception {
		return(tr.getField(p_id).toString());
	}
	
	@Override
	public JSONObject queryOptions(SessionHelper p_sp,String p_option) {
		// TODO Auto-generated method stub
		JSONObject jo = new JSONObject();
		jo.put("ok", true);
		JSONArray ja = new JSONArray();
		jo.put(p_option, ja);
		TableRec tr = getTr();
		if(tr == null)	 {
			try {
				
			SelectUtil su = p_sp.getBiSchema().getSelectUtil();
			tr = doQueryResult(su);
			} catch (Exception ex) {
				CoreLog.log(ex);
				return(null);
			}
			if(tr == null) return(null);
			setTr(tr);
		}
		synchronized(tr) {
			try {
				HashMap<String,Object> matchedList = new HashMap<String,Object>();
				if(options.get(p_option) != null) {
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					boolean matched = true;
					for(Condition cond : conditions.values()) {
						if(!compareOption(tr,cond)) {
							matched = false;
							break;
						}
					}
					if(matched) {
						Object o = getValueFromOption(tr,options.get(p_option));
						matchedList.put(JsonUtil.i18nGetKey(o),o);
					}
				}
				} else {
					int cc;
					cc = 0;
				}
				for(Object o : matchedList.values()) {
					ja.put(o);
				}
				return(jo);
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
		return null;
	}
	
	public synchronized TableRec getTr() {
		return(trHash.get(key)) ;
	}
	public synchronized TableRec setTr(TableRec p_tr) {
		trHash.put(key, p_tr);
		return(trHash.get(key)) ;
	}
}
