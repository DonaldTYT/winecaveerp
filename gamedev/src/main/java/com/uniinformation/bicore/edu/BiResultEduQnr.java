package com.uniinformation.bicore.edu;

import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellCollectionToJsonInterface;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.edu.EduQnrReplySlip;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultEduQnr extends BiResult {

	public BiResultEduQnr(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	@Override
	protected void afterLoadCollection(boolean p_isFetch,BiCellCollection p_cc){
		//build jsoncc from json str
		CellCollection col = p_cc.clearCollection("eqnr_jsoncc");
		String ss = p_cc.getString("eqnr_jsonstr");
		if(!ss.isEmpty()) {
			try {
				//map json data to jsoncc
				JSONObject jo = new JSONObject(ss);
				CellCollectionToJsonInterface.JSONObjectToCellCollection(col, jo);
			} 
			catch (Exception jex) {
				UniLog.log1("error:" + jex.getMessage());
			}
		}
	}
	
	@Override
	public void clearCurrentRec() {
		super.clearCurrentRec();
		
		//clear jsoncc
		getCurrentCollection().clearCollection("eqnr_jsoncc");
	}

	/***
	 * build json str from jsoncc
	 * @param p_col
	 */
	String doCollectionToJson(CellCollection p_col) {
		CellCollection col = p_col.getCollection("eqnr_jsoncc");
		try {
			if(col != null) {
				JSONObject jo = CellCollectionToJsonInterface.CellCollectionToJSON(col);
				if (StringUtils.equals(p_col.getCellString("eqnr_qnrtype"), EduQnrReplySlip.QNR_TYPE)) {
					String errMsg = EduQnrReplySlip.validJsonObject(jo);
					if (errMsg != null)
						return errMsg;
				}
				getCell("eqnr_jsonstr").set(jo.toString());
			} else {
				getCell("eqnr_jsonstr").set("");
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return ex.getMessage();
		}
		return null;
	}
	
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeUpdateCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		String errMsg = doCollectionToJson(col);
		if (errMsg != null)
			return new ReturnMsg(false, errMsg);
		try {
			col.getCell("eqnr_utime").set(DateUtil.now());
		}
		catch (Exception ex) {
			return new ReturnMsg(ex);
		}
		return(rtn);
	}

	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col)
	{
		ReturnMsg rtn = super.biBeforeAddCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		String errMsg = doCollectionToJson(col);
		if (errMsg != null)
			return new ReturnMsg(false, errMsg);
		try {
			col.getCell("eqnr_ctime").set(DateUtil.now());
			col.getCell("eqnr_utime").set(DateUtil.now());
		}
		catch (Exception ex) {
			return new ReturnMsg(ex);
		}
		return(rtn);
	}
}
