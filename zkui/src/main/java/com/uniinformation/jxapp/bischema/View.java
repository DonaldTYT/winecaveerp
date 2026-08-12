package com.uniinformation.jxapp.bischema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class View extends JxZkBiBase {
	/*
	class FieldInfo {
		String fdName;
	}
	*/
	HashMap<String,HashMap> jdbcSchema = new HashMap<String,HashMap>();
	@Override
	public void afterBind() {
		super.afterBind();
		new JxFieldAction("btAddAllField") {

			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				String tname = getBr().getCellString("grpth_table");
				HashSet<String> cf = new HashSet<String>();
				BiResult sr = getBr().getSubLink("bischema.Column");
				JxField sv = jxAdd("list_"+JxZkBiBase.replaceViewName(sr.getView().getName()));
				int n = sr.getRowCount();
				for(int i=0;i<n;i++) {
					BiCellCollection sc = sr.getRowCollectionV(i);
					if(!sc.getCell("grptc_fd").getString().equals("") &&
							sc.getCell("grptc_subtable").getString().equals(tname) 
							) {
						cf.add(sc.getCell("grptc_fd").getString());
						Object o = sr.getTrStatObj(i);
						if(sr.isMarkedDelete(o)) {
							sr.markDelete(o, false);
							sv.gridSetDataFormat(-1,i,"remove_deleted");
						}
					}
				}
				String jsonStr = BiSchema.getJdbcFieldList(getBr().getSessionHelper(), 
						getBr().getCellString("dddb_host"),
						getBr().getCellInt("dddb_port"),
						getBr().getCellString("dddb_jdbcstr"),
						getBr().getCellString("dddb_jdbcuser"),
						getBr().getCellString("dddb_jdbcpassword"),
						getBr().getCellString("dddb_dbpath"),
						getBr().getCellString("dddb_dbname"),
						getBr().getCellString("dddb_catalog"),
						getBr().getCellString("dddb_schema"),
						getBr().getCellString("ddt_dbtname")
							);
				if(jsonStr != null) {
					try {
						JSONArray jFlist = new JSONArray(jsonStr);
						for(int i=0;i<jFlist.length();i++) {
							JSONObject jf = jFlist.getJSONObject(i);
							String fdName = jf.getString("colname").trim();
							if(!cf.contains(fdName)) {
								UniLog.log("Add One field " + jf.getString("colname") + " type " + jf.getString("coltype") + " len " + jf.getInt("collen"));
								int idx = sr.getRowCount();
								CellCollection col = sr.newRowCollection();
								ReturnMsg rtn = sr.addSubRecord(col, idx,"");
								Object tr = rtn.getData();
								col.getCell("grptc_seq").set(idx+1);
								col.getCell("grptc_fd").set(fdName);
								col.getCell("grptc_label").set(fdName);
								col.getCell("grptc_header").set(fdName);
								col.getCell("grptc_subtable").set(tname);
								col.getCell("grptc_inselect").set(true);
								int rowIdx = getGipi(sr.getView().getName()).getIndexOf(tr);
								sv.addItemToList(tr, rowIdx);
							}
						}
					} catch (Exception jex) {
						UniLog.log(jex);
					}
				}
			}
		
		};
	}
	
	/*
	Vector<FieldInfo> getFieldList(String p_table) throws JSONException {
			HashMap<String,FieldInfo> fList = getFieldHash(p_table);
			Vector v = new Vector();
			if(fList != null) {
				for(String fd : fList.keySet()) {
					v.add(fList.get(fd));
				}
			}
			return(v);
	}
	
	HashMap<String,FieldInfo> getFieldHash(String p_table) throws JSONException {
		HashMap<String,HashMap> tList = getCurrentTableHash();
		if(tList == null) return(null);
		HashMap<String,FieldInfo> fList = tList.get(p_table);
		if(fList == null) {
				JSONArray jo = getJdbcFieldList(getBr().getSessionHelper(),
						getBr().getCellString("dddb_host"),
						getBr().getCellInt("dddb_port"),
						getBr().getCellString("dddb_jdbcstr"),
						getBr().getCellString("dddb_jdbcuser"),
						getBr().getCellString("dddb_jdbcpassword"),
						getBr().getCellString("dddb_dbpath"),
						getBr().getCellString("dddb_dbname"),
						getBr().getCellString("dddb_catalog"),
						getBr().getCellString("dddb_schema"),
						getBr().getCellString(p_table)
						);
				if(jo != null) {
					fList = new HashMap<String,FieldInfo>();
					for(int i=0;i<jo.length();i++) {
						fList.put(jo.getString(i), null);
					}
					tList.put(p_table, fList);
				}
				jdbcSchema.put(getBr().getCellString("grpth_database"),tList);
		}
		return(fList);
	}
	Vector<String> getCurrentTableList() {
			HashMap<String,HashMap> tList = getCurrentTableHash();
			Vector v = new Vector();
			if(tList != null) {
				for(String s : tList.keySet()) {
					v.add(s);
				}
			}
			return(v);
	}
	HashMap<String,HashMap> getCurrentTableHash() {
		try {
			HashMap<String,HashMap> tList = jdbcSchema.get(getBr().getCellString("grpth_database"));
			if(tList == null) {
				tList = new HashMap<String,HashMap>();
				JSONArray jo = BiSchema.getJdbcTableList(getBr().getSessionHelper(),
						getBr().getCellString("dddb_host"),
						getBr().getCellInt("dddb_port"),
						getBr().getCellString("dddb_jdbcstr"),
						getBr().getCellString("dddb_jdbcuser"),
						getBr().getCellString("dddb_jdbcpassword"),
						getBr().getCellString("dddb_dbpath"),
						getBr().getCellString("dddb_dbname"),
						getBr().getCellString("dddb_catalog"),
						getBr().getCellString("dddb_schema")
						);
				if(jo != null) {
					for(int i=0;i<jo.length();i++) {
						tList.put(jo.getString(i), null);
					}
				}
				jdbcSchema.put(getBr().getCellString("grpth_database"),tList);
			}
			return(tList);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(null);
		}
	}
			
	*/
	
	@Override
	public void bindCellCollection(BiResult c,int mode) {
		super.bindCellCollection(c, mode);
		/*
		try {
			getBr().getCell("grpth_table").setItemList(getCurrentTableList()) ;
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		*/
		
	}
}
