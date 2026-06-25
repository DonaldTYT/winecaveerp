package com.uniinformation.bicore.edu;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStudentTokenBal extends BiResult {
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	//private Map<Integer, List<Map<String, Object>>> sessionBalanceCacheMap = new HashMap<Integer, List<Map<String, Object>>>(); //key: sid
	private Map<Integer, Map<Integer, Map<String, Object>>> sessionBalanceCacheMap = new HashMap<Integer, Map<Integer, Map<String, Object>>>(); //key: studentRg, value: (key: courseRg, value: map)
	boolean afterLoadSerialMapFlag = false;

	public BiResultStudentTokenBal(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	@Override 
	public HashSet<BiTable> addExtraWhereStr(Wherecl p_where, HashSet<BiTable> p_hash) {
		p_where.appendString(" and essb_status <> 'Cancelled' and tkbal_ccy = esav_tokenccy ");
		return super.addExtraWhereStr(p_where, p_hash);
	}

	/*
	//andrew211130 remark it temporary. the logic can move the Erpv4StudentTokenBalCellCollection to support wherecl
	@Override
	protected void afterLoadCollection(boolean p_isFetch, BiCellCollection p_cc){
		super.afterLoadCollection(p_isFetch, p_cc);
		try {
			int studentRg = p_cc.getCellInt("tkbal_org");
			int courseRg = p_cc.getCellInt("essbsd_avrg");
			Date startDate = p_cc.getCell("essbsd_startdate").getDate();
			Date endDate = p_cc.getCell("essbsd_enddate").getDate();
			String tokenCcy = p_cc.getCellString("eaav0_tokenccy");
			double sessionFee = p_cc.getCellDouble("eaav0_fee");

			Map<Integer, Boolean> sMap = new HashMap<Integer, Boolean>();
			//calc session remain
			if (!DateUtil.isDateNull(startDate) && !DateUtil.isDateNull(endDate) && endDate.compareTo(DateUtil.today()) >= 0) {
				Date startDate1 = (startDate.compareTo(DateUtil.today()) > 0) ? startDate : DateUtil.today();
				TableRec tr = getSelectUtil().getQueryResult("select essn_rg from essession where essn_avrg = ? and essn_date between ? and ?" , 
					new Wherecl().appendArgument(courseRg)
								.appendArgument(sdf.format(startDate1))
								.appendArgument(sdf.format(endDate)));
				for (int i = 0; i < tr.getRecordCount(); i++) {
					tr.setRecPointer(i);
					int sessionRg = tr.getFieldInt("essn_rg");
					sMap.put(sessionRg, false);
				}
			}

			//calc session completed count(Present Count+Absent Count)
			int completedCount = 0;
			TableRec tr = getSelectUtil().getQueryResult("select esat_snrg, esat_status from esattendance, essession "
					+ "where esat_attype = 'SD' and esat_atrg = ? and essn_avrg = ? and essn_rg = esat_snrg" , 
				new Wherecl().appendArgument(studentRg)
							.appendArgument(courseRg));
			for (int i = 0; i < tr.getRecordCount(); i++) {
				tr.setRecPointer(i);
				int sessionRg1 = tr.getFieldInt("esat_snrg");
				String attStatus = tr.getFieldString("esat_status");
				if (StringUtils.equalsAny(attStatus, "Present", "Absent"))
					completedCount++;
				if (sMap.containsKey(sessionRg1))
					sMap.put(sessionRg1, true);
			}
			p_cc.getCell("essbsd_sesscomp").set(completedCount);

			//calc session remain 
			int sessionRemain = 0;
			for (Map.Entry<Integer, Boolean> entry : sMap.entrySet()) {
				if (!entry.getValue())
					sessionRemain++;
			}
			p_cc.getCell("essbsd_sessremain").set(sessionRemain);

			//calc tentative balance
			double tentativeBalance = sessionRemain * sessionFee;
			p_cc.getCell("essbsd_tentatibal").set(tentativeBalance);
				
		} catch(Exception ex) {
			UniLog.log(ex);
		}
	}
	*/
	
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		UniLog.log("createColumnCollection");
		return(new Erpv4StudentTokenBalCellColletion(p_parent, this));
	}

	/*public Map<Integer, List<Map<String, Object>>> getSessionBalanceCacheMap() {
		//return sessionBalanceCacheMap;
		//andrew211203 no need to handle the non-db field before query
		return afterLoadSerialMapFlag ? sessionBalanceCacheMap : null;
	}*/
	public Map<Integer, Map<Integer, Map<String, Object>>> getSessionBalanceCacheMap() {
		return afterLoadSerialMapFlag ? sessionBalanceCacheMap : null;
	}
	
	private void clearSessionBalanceCacheMap() {
		UniLog.log("clearSessionBalanceCacheMap");
		sessionBalanceCacheMap.clear();
	}
	
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		//andrew211203 clear cache map when perform query/refresh
		ReturnMsg rtn = super.afterLoadSerialMap();
		if (!rtn.getStatus()) return rtn;
		
		clearSessionBalanceCacheMap();
		afterLoadSerialMapFlag = true;
		return(ReturnMsg.defaultOk);
	}
	
}
