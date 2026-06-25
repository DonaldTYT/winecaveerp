package com.uniinformation.cell;

import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.DateUtil;

public class CellCollectionToJsonInterface implements CellCollectionBrowserInterface {
	
	protected JSONObject jo;
	
	public CellCollectionToJsonInterface(JSONObject p_jo) {
		jo = p_jo;
	}

	@Override
	public void gotCell(String p_cellName, Cell p_cell) throws Exception {
						// TODO Auto-generated method stub
						if(p_cell.getType() == Cell.VTYPE_DATETIME) {
							Date d = p_cell.getDate();
							jo.put(p_cellName, DateUtil.toSqlTimestamp(d));
						} else if(p_cell.getType() == Cell.VTYPE_DATE) {
							Date d = p_cell.getDate();
							if(d == null || !d.after(DateUtil.zeroDate)) {
								jo.put(p_cellName, "1899/12/31");
							} else {
								jo.put(p_cellName, DateUtil.toDateString(d, "yyyy/mm/dd"));
							}
						} else if(p_cell.getType() == Cell.VTYPE_DOUBLE){
							double d = p_cell.getDouble();
							if(Double.isNaN(d)) 
								jo.put(p_cellName, 0.0);
							else
								jo.put(p_cellName, d);
						} else  {
							jo.put(p_cellName, p_cell.getObject());
						}
	}
	@Override
	public void gotCellArray(String p_cellName, Cell[] p_cellArray) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gotCollection(String p_cellName, CellCollection p_collection) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void gotCollectionList(String p_collectionName, Vector p_collectionList) throws Exception {
		// TODO Auto-generated method stub
		JSONArray ja = new JSONArray();
		jo.put(p_collectionName, ja);
		for(int i=0;i<p_collectionList.size();i++) {
			CellCollection col = (CellCollection) p_collectionList.get(i);
			JSONObject jo2 = new JSONObject();
			col.browse(new CellCollectionToJsonInterface(jo2),new Hashtable());
			ja.put(jo2);
		}
	}
	
	static public JSONObject CellCollectionToJSON(CellCollection p_col) throws Exception {
		final JSONObject rootJo = new JSONObject();
		p_col.browse(new CellCollectionToJsonInterface(rootJo),new Hashtable());
		return(rootJo);
	}

	static public void JSONObjectToCellCollection(CellCollection p_col,JSONObject jo) throws Exception {
		Iterator<String> it  = jo.keys();
		while(it.hasNext()) {
			String key = it.next();
			Object o =jo.get(key);
			if(o instanceof JSONObject) {
				JSONObject jo2 = (JSONObject) o;
				CellCollection col2 = new CellCollection(p_col);
				p_col.addCollection(key, col2);
				JSONObjectToCellCollection(col2,jo2);
			} else if(o instanceof JSONArray) {
				JSONArray ja2 = (JSONArray) o;
				int len = ja2.length();
				if(len <= 0) {
					// current not support empty json array, skipped
					continue;
				}
				Object o3 = ja2.getJSONObject(0);
				// CellCollection only support JSONArray of JSONObject or JSONArrry of values
				if(o3 instanceof JSONObject) {
					CellVector cv2 = new CellVector();
					for(int i=0;i<len;i++) {
						JSONObject jo3  = ja2.getJSONObject(i);
						CellCollection col3 = new CellCollection(p_col);
						cv2.add(col3);
						JSONObjectToCellCollection(col3,jo3);
					}
					p_col.addCollectionList(key, cv2);
				} else if(o3 instanceof JSONArray) {
					// JSONArray of JSONArray not supported
					continue;
					
				} else {
					Cell[] ca = new Cell[len];
					for(int i=0;i<len;i++) {
						o3 = ja2.get(i);
						ca[i] = new Cell(o3);
					}
					p_col.addCell(key, ca);
				}
			} else {
				Cell c;
				if(o instanceof String && !((String) o).trim().isEmpty()) {
					Date d = DateUtil.dateTimeStrToDate((String) o);
					if(d != null) {
						c = new Cell(d);
					} else {
						c = new Cell(o);
					}
				} else c = new Cell(o);
				p_col.addCell(key, c);
			}
		}
		return;
	}
}
