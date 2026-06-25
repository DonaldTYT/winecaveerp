package com.uniinformation.bicore.clinic;

import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultHealthQnrFiling extends BiResult{
	BiResult brParent;

	public BiResultHealthQnrFiling(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		UniLog.log1("called " + p_parent);
		brParent = p_parent;
	}
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col){
		UniLog.log1("called");
		ReturnMsg rtnMsg = super.biBeforeDeleteCurrent(col);
		if(!rtnMsg.getStatus()) return(rtnMsg);
		try {
			UniLog.log1("bckf_rg:%d seq:%d key:%s bckf_dtrg:%d bckf_pdtrg:%d", col.getCell("bckf_rg").getInt(), col.getCell("bckf_seq").getInt(), col.getCell("bckf_key").getString(), col.getCell("bckf_dtrg").getInt(), col.getCell("bckf_pdtrg").getInt());
			String key = col.getCell("bckf_key").getString();
			if (brParent != null && brParent instanceof BiResultHealthQnr && StringUtils.isNotBlank(key)) {
				String agentId = ((BiResultHealthQnr)brParent).agentId;
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
