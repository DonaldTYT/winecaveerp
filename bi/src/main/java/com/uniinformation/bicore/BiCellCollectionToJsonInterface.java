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
		// TODO Auto-generated method stub
		JSONArray ja = new JSONArray();
		jo.put(p_collectionName, ja);
		for(int i=0;i<p_collectionList.size();i++) {
			BiCellCollection col = (BiCellCollection) p_collectionList.get(i);
			Object o = col.br.getTrStatObj(i);
			if(!col.br.isMarkedDelete(o)) {
				JSONObject jo2 = new JSONObject();
				col.browse(new BiCellCollectionToJsonInterface(jo2),new Hashtable());
				ja.put(jo2);
			}
		}
	}
	static public JSONObject BiCellCollectionToJSON(BiCellCollection p_col) throws Exception {
		final JSONObject rootJo = new JSONObject();
		p_col.browse(new BiCellCollectionToJsonInterface(rootJo),new Hashtable());
		return(rootJo);
	}
}
