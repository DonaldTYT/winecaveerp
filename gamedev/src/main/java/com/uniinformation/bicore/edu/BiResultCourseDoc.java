package com.uniinformation.bicore.edu;

import java.util.Vector;

import org.apache.commons.lang.StringUtils;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultCourseDoc extends BiResult {
	private BiResult brParent;

	public BiResultCourseDoc(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
		brParent = p_parent;
	}

	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col){
		UniLog.log1("called");
		ReturnMsg rtnMsg = super.biBeforeDeleteCurrent(col);
		if(!rtnMsg.getStatus()) return(rtnMsg);
		try {
			String key = col.getCellString("esavd_key");
			UniLog.log1("key:%s", key);
			if (brParent != null && brParent instanceof BiResultCourse && StringUtils.isNotBlank(key)) {
				String agentId = ((BiResultCourse)brParent).agentId;
				UniLog.log1("deletefile " + agentId + "," + key);
				FilingUtil.deleteFile(agentId, null, key);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return(new ReturnMsg(e));
		}
		return(new ReturnMsg(true));
	}
}
