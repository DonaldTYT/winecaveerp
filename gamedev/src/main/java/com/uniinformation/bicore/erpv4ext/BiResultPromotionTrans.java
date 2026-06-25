package com.uniinformation.bicore.erpv4ext;

import java.util.Date;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.erpv4ext.LeaveApplication;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultPromotionTrans extends BiResult {

	public BiResultPromotionTrans(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col, boolean isUpdate) {
		ReturnMsg rtn = super.biAfterAddUpdateCurrent(col, isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		
		try {
			String eid = getCellString("emg_eid");
			Date startDate = getCellDate("emg_stdate");
			Date endDate = getCellDate("emg_enddate");
			Date lastStartDate = getCellDate("emg_xlaststdate");
			Date lastEndDate = DateUtil.prevday(startDate);
			if (!DateUtil.isDateNull(lastStartDate)) {
				//update last take office end date
				Wherecl wherecl = new Wherecl().appendArgument(lastEndDate).appendArgument(eid).appendArgument(lastStartDate);
				su.executeUpdate("update emgrade set emg_enddate = ? where emg_eid = ? and emg_stdate = ?", wherecl);
				su.executeUpdate("update emincome set emic_enddate = ? where emic_eid = ? and emic_date = ?", wherecl);
				su.executeUpdate("update emdeduction set emde_enddate = ? where emde_eid = ? and emde_date = ?", wherecl);
				su.executeUpdate("update empension set empe_enddate = ? where empe_eid = ? and empe_date = ?", wherecl);
				//if (!isUpdate)
				//	clonePaymentItem(lastStartDate, startDate, endDate);
			}
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		
		return rtn;
	}

	@Override
	protected ReturnMsg biAfterDeleteCurrent(CellCollection col) {
		ReturnMsg rtn = super.biAfterDeleteCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			String eid = getCellString("emg_eid");
			Date lastStartDate = getCellDate("emg_xlaststdate");
			Date nextStartDate = getCellDate("emg_xnextstdate");
			Date lastEndDate = DateUtil.isDateNull(nextStartDate) ? LeaveApplication.MAX_DATE : DateUtil.prevday(nextStartDate);
			if (!DateUtil.isDateNull(lastStartDate)) {
				//update last take office end date
				Wherecl wherecl = new Wherecl().appendArgument(lastEndDate).appendArgument(eid).appendArgument(lastStartDate);
				su.executeUpdate("update emgrade set emg_enddate = ? where emg_eid = ? and emg_stdate = ?", wherecl);
				su.executeUpdate("update emincome set emic_enddate = ? where emic_eid = ? and emic_date = ?", wherecl);
				su.executeUpdate("update emdeduction set emde_enddate = ? where emde_eid = ? and emde_date = ?", wherecl);
				su.executeUpdate("update empension set empe_enddate = ? where empe_eid = ? and empe_date = ?", wherecl);
			}
		}
		catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}

		return(rtn);
	}
	
	/*private void clonePaymentItem(Date fromStartDate, Date toStartDate, Date toEndDate) throws Exception {
		String eid = getCellString("emg_eid");
		su.executeUpdate("delete from emincome where emic_eid = ? and emic_date = ?", new Wherecl().appendArgument(eid).appendArgument(toStartDate));
		su.executeUpdate("delete from emdeduction where emde_eid = ? and emde_date = ?", new Wherecl().appendArgument(eid).appendArgument(toStartDate));
		su.executeUpdate("delete from empension where empe_eid = ? and empe_date = ?", new Wherecl().appendArgument(eid).appendArgument(toStartDate));
		CellVector resultList = su.getQueryResultToCellVector("select * from emincome where emic_eid = ? and emic_date = ?", new Wherecl().appendArgument(eid).appendArgument(fromStartDate));
		for (Object o : resultList) {
			CellCollection cc = (CellCollection)o;
			su.executeUpdate("insert into emincome(emic_eid, emic_date, emic_enddate, emic_code, emic_formula) values(?,?,?,?,?)", 
					new Wherecl()
						.appendArgument(eid)
						.appendArgument(toStartDate)
						.appendArgument(toEndDate)
						.appendArgument(cc.getCellString("emic_code"))
						.appendArgument(cc.getCellString("emic_formula")));
		}
		resultList = su.getQueryResultToCellVector("select * from emdeduction where emde_eid = ? and emde_date = ?", new Wherecl().appendArgument(eid).appendArgument(fromStartDate));
		for (Object o : resultList) {
			CellCollection cc = (CellCollection)o;
			su.executeUpdate("insert into emdeduction(emde_eid, emde_date, emde_enddate, emde_code, emde_formula) values(?,?,?,?,?)", 
					new Wherecl()
						.appendArgument(eid)
						.appendArgument(toStartDate)
						.appendArgument(toEndDate)
						.appendArgument(cc.getCellString("emde_code"))
						.appendArgument(cc.getCellString("emde_formula")));
		}
		resultList = su.getQueryResultToCellVector("select * from empension where empe_eid = ? and empe_date = ?", new Wherecl().appendArgument(eid).appendArgument(fromStartDate));
		for (Object o : resultList) {
			CellCollection cc = (CellCollection)o;
			su.executeUpdate("insert into empension(empe_eid, empe_date, empe_enddate, empe_code, empe_formula) values(?,?,?,?,?)", 
					new Wherecl()
						.appendArgument(eid)
						.appendArgument(toStartDate)
						.appendArgument(toEndDate)
						.appendArgument(cc.getCellString("empe_code"))
						.appendArgument(cc.getCellString("empe_formula")));
		}
	}*/
}
