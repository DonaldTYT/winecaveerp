package com.uniinformation.bicore.erpv4ext;

import java.util.Date;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultPromotionTransHdr extends BiResult {

	public BiResultPromotionTransHdr(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col, boolean isUpdate) {
		ReturnMsg rtn = super.biAfterAddUpdateCurrent(col, isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			String eid = getCellString("emg_eid");
			Vector<BiCellCollection> recs = getSubLinkResult("erpv4ext.PromotionTransDet");
			for (BiCellCollection cc : recs) {
				Date startDate = cc.getDate("emg0_stdate");
				Date endDate = cc.getDate("emg0_enddate");
				UniLog.log1("update emincome,emdeduction,empension enddate eid:%s, stdate:%s, enddate:%s", eid, startDate, endDate);
				Wherecl wherecl = new Wherecl().appendArgument(endDate).appendArgument(eid).appendArgument(startDate);
				su.executeUpdate("update emincome set emic_enddate = ? where emic_eid = ? and emic_date = ?", wherecl);
				su.executeUpdate("update emdeduction set emde_enddate = ? where emde_eid = ? and emde_date = ?", wherecl);
				su.executeUpdate("update empension set empe_enddate = ? where empe_eid = ? and empe_date = ?", wherecl);
			}
			Wherecl wherecl = new Wherecl().appendArgument(eid).appendArgument(eid);
			su.executeUpdate("delete from emincome where emic_eid = ? and emic_date not in (select emg_stdate from emgrade where emg_eid = ?)", wherecl);
			su.executeUpdate("delete from emdeduction where emde_eid = ? and emde_date not in (select emg_stdate from emgrade where emg_eid = ?)", wherecl);
			su.executeUpdate("delete from empension where empe_eid = ? and empe_date not in (select emg_stdate from emgrade where emg_eid = ?)", wherecl);
			//refresh header emg
			boolean flag = false;
			for (BiCellCollection cc : recs) {
				Date startDate = cc.getDate("emg0_stdate");
				Date endDate = cc.getDate("emg0_enddate");
				if (startDate.compareTo(DateUtil.today()) <= 0 && endDate.compareTo(DateUtil.today()) >= 0) {
					getCell("emg_stdate").set(startDate);
					getCell("emg_enddate").set(endDate);
					getCell("emg_emtyperg").set(cc.getInt("emg0_emtyperg"));
					getCell("emg_deptrg").set(cc.getInt("emg0_deptrg"));
					getCell("emg_graderg").set(cc.getInt("emg0_graderg"));
					getCell("emg_postrg").set(cc.getInt("emg0_postrg"));
					getCell("emg_poststatus").set(cc.getString("emg0_poststatus"));
					getCell("emg_tranreason").set(cc.getString("emg0_tranreason"));
					getCell("emg_wage").set(cc.getDouble("emg0_wage"));
					getCell("emg_wgtype").set(cc.getString("emg0_wgtype"));
					flag = true;
					break;
				}
			}
			if (!flag) {
				getCell("emg_stdate").set(DateUtil.zeroDate);
				getCell("emg_enddate").set(DateUtil.zeroDate);
				getCell("emg_emtyperg").set(0);
				getCell("emg_deptrg").set(0);
				getCell("emg_graderg").set(0);
				getCell("emg_postrg").set(0);
				getCell("emg_poststatus").set("");
				getCell("emg_tranreason").set("");
				getCell("emg_wage").set(0.0);
				getCell("emg_wgtype").set("");
			}
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}

		return rtn;
	}
}
