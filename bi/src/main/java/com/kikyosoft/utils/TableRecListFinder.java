package com.kikyosoft.utils;

import java.util.HashSet;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.CoreLog;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public abstract class TableRecListFinder extends OptionFinder {
	static TableRec prdTr;
	public TableRecListFinder(SessionHelper p_sp) {
		super();
	}
	
	abstract protected TableRec doQueryResult(SelectUtil su) throws Exception;

	@Override
	public boolean compareOption(Object p_record, Condition p_cond) throws Exception{
		// TODO Auto-generated method stub
		Object o = ((TableRec) p_record).getField(options.get(p_cond.option));
		if(p_cond.operator == null) {
			if(p_cond.value1.equals(o)) return(true);
		}
		return(false);
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
		synchronized(prdTr) {
			try {
				HashSet<Object> matchedList = new HashSet<Object>();
				for(int i=0;i<prdTr.getRecordCount();i++) {
					prdTr.setRecPointer(i);
					boolean matched = true;
					for(Condition cond : conditions.values()) {
						if(!compareOption(tr,cond)) {
							matched = false;
							break;
						}
					}
					if(matched) matchedList.add(tr.getField(options.get(p_option)));
				}
				for(Object o : matchedList) {
					ja.put(o.toString());
				}
				return(jo);
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
		return null;
	}
	
	public static synchronized TableRec getTr() {
		return(prdTr) ;
	}
	public static synchronized TableRec setTr(TableRec p_tr) {
		prdTr = p_tr;
		return(prdTr) ;
	}
}
