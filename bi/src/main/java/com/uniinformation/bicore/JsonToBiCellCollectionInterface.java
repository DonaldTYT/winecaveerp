package com.uniinformation.bicore;

import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.cell.Cell;

public interface JsonToBiCellCollectionInterface {
	
	public ReturnMsg onAddSubRecord(BiResult parentBr,BiResult sublinkBr,int idx);
	
	static public ReturnMsg JsonToBiCellCollection(BiCellCollection col, JSONObject jo,
			JsonToBiCellCollectionInterface ji) throws Exception {
		Iterator<String> keys = jo.keys();
		ReturnMsg rtn = ReturnMsg.defaultOk;
		while(keys.hasNext()) {
			String key = keys.next();
			Object value = jo.get(key);
			if(value instanceof JSONArray) {
				BiResult sr = col.getBr().getSubLink(key);
				if(sr == null) continue;

				JSONArray ja = (JSONArray) value;
				int i = 0;
				for(;i<ja.length();i++) {
					BiCellCollection scol;
					if(i >= sr.getRowCount()) {
						if(ji != null) {
							rtn = ji.onAddSubRecord(col.getBr(), sr, i);
							if(rtn != null && !rtn.getStatus()) return rtn;
						}
						scol = sr.newRowCollection();
						if(scol == null) {
							return new ReturnMsg(false, "Cannot create subrecord for " + key);
						}
						rtn = sr.addSubRecord(scol, i, "");
						if(rtn != null && !rtn.getStatus()) return rtn;
					} else {
						Object rowState = sr.getTrStatObj(new Integer(i));
						sr.markDelete(rowState, false);
						scol = sr.getRowCollectionV(i);
					}
					rtn = JsonToBiCellCollection(scol, ja.getJSONObject(i), ji);
					if(rtn != null && !rtn.getStatus()) return rtn;
				}
				for(;i<sr.getRowCount();i++) {
					Object rowState = sr.getTrStatObj(new Integer(i));
					sr.markDelete(rowState, true);
				}
			} else {
				Object cellValue = value == JSONObject.NULL ? null : value;
				Cell cell = (Cell) col.testCell(key);
				if(cell != null) cell.set(cellValue);
			}
		}
		return rtn;
	}
}
