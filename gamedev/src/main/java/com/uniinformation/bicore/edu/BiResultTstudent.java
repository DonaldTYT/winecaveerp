package com.uniinformation.bicore.edu;

import java.util.Vector;

import org.json.JSONException;
import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellCollectionToJsonInterface;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultTstudent extends BiResultErpv4 {

	public BiResultTstudent(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void afterLoadCollection(boolean p_isFetch,BiCellCollection p_cc){
		CellCollection col = p_cc.clearCollection("estd_jsoncc");
		String ss = p_cc.getString("estd_jsonstr");
		//ss = "{'fd01':'111','tb03':'111','cb02':true}";
		//ss = "{'json_tb':'111','json_cb':true}";
		if(!ss.isEmpty()) {
			try {
				JSONObject jo = new JSONObject(ss);
				CellCollectionToJsonInterface.JSONObjectToCellCollection(col, jo);
			} 
			catch (Exception jex) {
				UniLog.log(jex);
			}
		}
		
		if(col.testCell("fd01") != null) {
			try {
				p_cc.getCell("estd_fd01").set(col.getCellString("fd01"));
			} 
			catch (CellException ce) {
				UniLog.log(ce);
			}
		}
	}
	@Override
	public void clearCurrentRec() {
		super.clearCurrentRec();
		getCurrentCollection().clearCollection("estd_jsoncc");
	}

	void doCollectionToJson(CellCollection p_col) {
		CellCollection col = p_col.getCollection("estd_jsoncc");
		try {
			if(col != null) {
				JSONObject jo = CellCollectionToJsonInterface.CellCollectionToJSON(col);
				getCell("estd_jsonstr").set(jo.toString());
			} else {
				getCell("estd_jsonstr").set("");
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeUpdateCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		doCollectionToJson(col);
		return(rtn);
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col)
	{
		ReturnMsg rtn = super.biBeforeAddCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		doCollectionToJson(col);
		return(rtn);
	}

}
