package com.uniinformation.bicore;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.cell.Cell;

public interface JsonToBiCellCollectionInterface {
	
	public ReturnMsg onAddSubRecord(BiResult parentBr,BiResult sublinkBr,int idx);
	
	static public ReturnMsg JsonToBiCellCollection(BiCellCollection col,JSONObject jo, JsonToBiCellCollectionInterface ji) throws Exception {
        Iterator<String> keys = jo.keys();
        ReturnMsg rtn=ReturnMsg.defaultOk;
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jo.get(key);
            if(value instanceof JSONArray) {
            	BiResult sr = col.getBr().getSubLink(key);
            	if(sr != null) {
            		JSONArray ja = (JSONArray) value;
            		int i=0;
            		for(;i<ja.length();i++) {
            			BiCellCollection scol;
            			if(i >= sr.getRowCount()) {
            				if(ji != null) {
            					rtn = ji.onAddSubRecord(col.getBr(),sr,i) ;
            					if(!rtn.getStatus()) break;
            				}
            				scol = sr.newRowCollection();
            				rtn = sr.addSubRecord(scol, i,"");
            				if(!rtn.getStatus()) break;
            			} else {
            				Object o = sr.getTrStatObj(new Integer(i));
            				sr.markDelete( o, false);
            				scol = sr.getRowCollectionV(i);
            			}
            			rtn = JsonToBiCellCollection(scol,ja.getJSONObject(i), ji);
            		}
            		for(;i<sr.getRowCount();i++) {
            			Object o = sr.getTrStatObj(new Integer(i));
            			sr.markDelete( o, true);
            		}
            	}
            } else {
            	Object o = jo.get(key);
            	Cell cc = (Cell) col.testCell(key);
            	if(cc != null ) {
            		cc.set(o);
            	}
            }
        }
        return(rtn);
	}
}
