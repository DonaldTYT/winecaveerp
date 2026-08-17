package com.uniinformation.bicore.wip;

import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.Base64Util;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultWfmActivity extends BiResult {

	public BiResultWfmActivity(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeDeleteCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		
		try {
			int rg = col.getInt("wfmact_rg");
			String content = col.getString("wfmact_content");
			if (StringUtils.startsWith(content, "FILING://")) {
				String flKey = String.format("zkbi_wfmact_%010d", rg);;
				UniLog.log1("deleteFile key:%s", flKey);
				FilingUtil.deleteFile(sh.getAgent(), null, flKey);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,-1,ex.getMessage()));
		}
		
		return rtn;
	}
}
