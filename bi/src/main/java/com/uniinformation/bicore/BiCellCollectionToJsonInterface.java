package com.uniinformation.bicore;

import java.util.Hashtable;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.uniinformation.cell.CellCollectionToJsonInterface;

public class BiCellCollectionToJsonInterface extends CellCollectionToJsonInterface {

	public BiCellCollectionToJsonInterface(JSONObject p_jo) {
		super(p_jo);
		// TODO Auto-generated constructor stub
		
	}
	@Override
	public void gotCollectionList(String p_collectionName, Vector p_collectionList) throws Exception {
		JSONArray ja = new JSONArray();
		jo.put(p_collectionName, ja);
		if(p_collectionList == null || p_collectionList.isEmpty()) {
			return;
		}

		/*
		 * A sublink's collection vector is stored in physical record order, while
		 * resultStatList contains its current logical order and delete state.  Using
		 * the vector index as a resultStat index copied the wrong row after a row was
		 * inserted, deleted or resequenced.  Always walk the owning BiResult instead.
		 */
		BiCellCollection first = (BiCellCollection) p_collectionList.get(0);
		BiResult detailResult = first.br;
		for(int i=0;i<detailResult.getRowCount();i++) {
			Object rowState = detailResult.getTrStatObj(i);
			if(detailResult.isMarkedDelete(rowState)) {
				continue;
			}
			BiCellCollection col = detailResult.getRowCollectionV(i);
			if(col == null) {
				continue;
			}
			JSONObject jo2 = new JSONObject();
			col.browse(new BiCellCollectionToJsonInterface(jo2),new Hashtable());
			ja.put(jo2);
		}
	}
	static public JSONObject BiCellCollectionToJSON(BiCellCollection p_col) throws Exception {
		final JSONObject rootJo = new JSONObject();
		p_col.browse(new BiCellCollectionToJsonInterface(rootJo),new Hashtable());
		return(rootJo);
	}
}
