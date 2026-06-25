package com.uniinformation.bicore.edu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultCourse extends BiResult {
	String agentId;

	public BiResultCourse(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeUpdateCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
//		refreshToken(col);
		return(rtn);
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col)
	{
		ReturnMsg rtn = super.biBeforeAddCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
//		refreshToken(col);
		return(rtn);
	}
	
//	private void refreshToken(CellCollection p_col) {
//		int courseRg = p_col.getCellInt("eaav0_rg");
//		Double courseFee = p_col.getCellDouble("eaav0_coursefee");
//		String tokenccy = p_col.getCellString("eaav0_tokenccy");
//		UniLog.log1("course:%d fee:%f token:%s", courseRg, courseFee, tokenccy);
//		if (courseRg <= 0) {
//			UniLog.log1("invalid sdrg");
//			return;
//		}
//		
//		Vector arg = new Vector();
//		
//		
//		arg.add(0);
//		arg.add(courseRg);
//		
//		Vector<BiCellCollection> vv = getSubLink("edu.CourseStudent").getRowCollectionList();
//		for(BiCellCollection bc : vv) {
//			int sdrg = bc.getCellInt("essbsd_sdrg");
//			String status = bc.getCellString("essbsd_status");
//			UniLog.log1("sdrg:%d status:%s", sdrg, status);
//			if (sdrg < 0) continue;
//			if (StringUtils.equalsIgnoreCase(status,"Cancelled")) {
//				continue;
//			}
//			arg.add(sdrg);
//			arg.add(courseRg);
//			arg.add(tokenccy);
//			arg.add(courseFee);
//		}
//		UniLog.log1("DEBUG:" +  arg.toString());
//		Value v = su.getRpcClient().callSegment("token_addUpdateCourseSubscribeMulti",arg);
//		UniLog.log1("result: " + v);
//	}

	/***
	 * called by ComposerBase
	 * @param p_agentId
	 */
	public void setAgentId(String p_agentId){
		agentId = p_agentId;
	}
}
