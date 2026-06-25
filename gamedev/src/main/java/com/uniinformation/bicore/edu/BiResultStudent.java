package com.uniinformation.bicore.edu;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellCollectionToJsonInterface;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.edu.ProcessScanLog;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStudent extends BiResult{
	private String lastLoadCardNo, lastLoadStatus;
	
	public BiResultStudent(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	@Override
	protected void afterLoadCollection(boolean p_isFetch,BiCellCollection p_cc){
		//build jsoncc from json str
		CellCollection col = p_cc.clearCollection("essdx_jsoncc");
		String ss = p_cc.getString("essdx_jsonstr");
		if(!ss.isEmpty()) {
			try {
				//map json data to jsoncc
				JSONObject jo = new JSONObject(ss);
				CellCollectionToJsonInterface.JSONObjectToCellCollection(col, jo);
			} 
			catch (Exception jex) {
				UniLog.log1("error:" + jex.getMessage());
				//UniLog.log(jex);
			}
		}
		
		/*
		if(col.testCell("fd01") != null) {
			try {
				p_cc.getCell("estd_fd01").set(col.getCellString("fd01"));
			} 
			catch (CellException ce) {
				UniLog.log(ce);
			}
		}
		*/
		lastLoadCardNo = p_cc.getString("essd_cardno");
		lastLoadStatus = p_cc.getString("essd_status");
		UniLog.log1("lastLoadCardNo:%s,lastLoadStatus:%s,p_isFetch:%b", lastLoadCardNo, lastLoadStatus, p_isFetch);
	}
	@Override
	public void clearCurrentRec() {
		super.clearCurrentRec();
		
		//clear jsoncc
		getCurrentCollection().clearCollection("essdx_jsoncc");
	}

	/***
	 * build json str from jsoncc
	 * @param p_col
	 */
	void doCollectionToJson(CellCollection p_col) {
		CellCollection col = p_col.getCollection("essdx_jsoncc");
		try {
			if(col != null) {
				JSONObject jo = CellCollectionToJsonInterface.CellCollectionToJSON(col);
				getCell("essdx_jsonstr").set(jo.toString());
			} else {
				getCell("essdx_jsonstr").set("");
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
//		refreshToken(col);
		rtn = updateLoginUser(col);
		return(rtn);
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col)
	{
		ReturnMsg rtn = super.biBeforeAddCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		doCollectionToJson(col);
//		refreshToken(col);
		/*
		v = su.getRpcClient().callSegment("getuniquerg",
				new VectorUtil()
					.addElement(2015)
					.addElement("stmov")
					.addElement("stm_ref1")
					.addElement("ADN&&&&&")
					.addElement("")
					.toVector()
					);	
		*/
		return(rtn);
	}

	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtn = super.biBeforeUpdateCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		rtn = checkUniqueCardNo(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		
		try {
			if (StringUtils.isBlank(col.getCellString("essd_sdno"))) {
				Value v = getView().getSchema().getUniqueRg(this,"", 53006, "esstudent", "essd_sdno", "S&&&&&&");
				col.getCell("essd_sdno").set(v.toString());
			}
		} catch (Exception cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,-1,cex.getMessage()));
		}
		return rtn;
	}

	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeDeleteCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		rtn = deleteLoginUser(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		UniLog.log1("check attendnace");
		BiResult brAttendance = null;
		try {
			brAttendance = sh.newBiResult("edu.StudentAttendance");
			brAttendance.addCustomCondition(String.format("esatsd_atrg = %d", col.getCellInt("essd_rg")));
			if ((rtn = brAttendance.query(true, false)).getStatus()) {
				if (brAttendance.next())
					//return new ReturnMsg(false, String.format("Not allowed to removed student if attendance record is exist, student code:%s", col.getCellString("essd_sdno")));
					return new ReturnMsg(false, "Not allowed to removed student if attendance record exists. Please remove attendance record first.");
			}
			else
				throw new Exception(rtn.getMsg());
		}
		catch (Exception e) {
			return new ReturnMsg(e);
		}
		finally {
			if (brAttendance != null)
				brAttendance.close();
		}
		
		UniLog.log1("check payment");
		BiResult brPayment = null;
		try {
			brPayment = BiResultHelper.create(sh, "edu.Payment", null, String.format("esph_sdrg = %d",col.getCellInt("essd_rg")) , null, 0, null, false);
			if (brPayment != null && brPayment.getRowCount() > 0) {
				return new ReturnMsg(false,"Not allowed to removed student if payment record exists. Please remove payment record first.");
			}
		}
		catch(Exception e) {
			return new ReturnMsg(e);
		}
		finally {
			BiResultHelper.close(brPayment);
		}
		
		return(rtn);
	}

	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtn = super.biAfterAddUpdateCurrent(col, isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		
		String cardNo = col.getCellString("essd_cardno");
		String status = col.getCellString("essd_status");

		//need to call ProcessScanLog.setCSMapDirty() when cardNo is changed.
		if (isUpdate) {
			UniLog.log1("cardNo:%s, lastLoadCardNo:%s, status:%s, lastLoadStatus:%s", cardNo, lastLoadCardNo, status, lastLoadStatus);
			if (!StringUtils.equals(lastLoadCardNo, cardNo) 
					|| (StringUtils.equals(lastLoadStatus, "Cancelled") != StringUtils.equals(status, "Cancelled"))) {
				//ProcessScanLog.setCSMapDirty(cardNo);
				ProcessScanLog.setCSMapDirty(cardNo,true); //andrew220516 when add/update card, immediate reflect changes to csmap
				ProcessScanLog.removeCSMapItem(lastLoadCardNo);
			}
		}
		else
			ProcessScanLog.setCSMapDirty(cardNo);
		
		return rtn;
	}

	@Override
	protected ReturnMsg biAfterDeleteCurrent(CellCollection col) {
		ReturnMsg rtn = super.biAfterDeleteCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		String cardNo = col.getCellString("essd_cardno");

		//need to call ProcessScanLog.setCSMapDirty() when cardNo is changed.
		//ProcessScanLog.setCSMapDirty();
		ProcessScanLog.removeCSMapItem(cardNo);

		return rtn;
	}

	@Override 
	public HashSet<BiTable> addExtraWhereStr(Wherecl p_where, HashSet<BiTable> p_hash) {
		SessionHelper sh = getSessionHelper();
		if (!sh.isAdminUser() && !sh.hasAccessRight("#edu") && !sh.hasAccessRight("#eduadmin")) {
			if (sh.hasAccessRight("#student"))
				p_where.andUniop("essd_sdno", "=" , sh.getLoginId().toUpperCase());
		}
		return super.addExtraWhereStr(p_where, p_hash);
	}

//	private void refreshToken(CellCollection p_col) {
//		int sdrg = p_col.getCellInt("essd_rg");
//		UniLog.log1("student rg:%d", sdrg);
//		if (sdrg <= 0) {
//			UniLog.log1("invalid sdrg");
//			return;
//		}
//		
//		Vector arg = new Vector();
//		arg.add(sdrg);
//		arg.add(0);
//		
//		Vector<BiCellCollection> vv = getSubLink("edu.StudentCourse").getRowCollectionList();
//		for(BiCellCollection bc : vv) {
//			int courseRg = bc.getCellInt("eaav0_rg");
//			Double courseFee = bc.getCellDouble("eaav0_coursefee");
//			String tokenccy = bc.getCellString("eaav0_tokenccy");
//			String status = bc.getCellString("essbsd_status");
//			UniLog.log1("rg:%d fee:%f token:%s status:%s", courseRg, courseFee, tokenccy, status);
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
//		
//		
//	}
	
	private ReturnMsg updateLoginUser(CellCollection col) {
		int sdrg = col.getCellInt("essd_rg");
		UniLog.log1("student rg:%d", sdrg);
		if (sdrg <= 0) {
			UniLog.log1("invalid sdrg");
			return ReturnMsg.defaultOk;
		}
		String loginId = col.getCellString("essd_sdno").toLowerCase();
		try {
			su.executeUpdate("update loginuser set lgu_name = ?, lgu_chnname = ?, lgu_disabled = ? where lgu_login = ?",
					new Wherecl()
						.appendArgument(col.getCellString("essd_name"))
						.appendArgument(col.getCellString("essd_chnname"))
						.appendArgument(col.getCellString("essd_status").equals("Cancelled") ? "Y" : "N")
						.appendArgument(loginId)
						);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ReturnMsg(false, e.getMessage());
		}
		return ReturnMsg.defaultOk;
	}

	private ReturnMsg deleteLoginUser(CellCollection col) {
		int sdrg = col.getCellInt("essd_rg");
		UniLog.log1("student rg:%d", sdrg);
		if (sdrg <= 0) {
			UniLog.log1("invalid sdrg");
			return ReturnMsg.defaultOk;
		}
		String loginId = col.getCellString("essd_sdno").toLowerCase();
		try {
			su.executeUpdate("delete from loginuser where lgu_login = ?",
					new Wherecl()
						.appendArgument(loginId)
						);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ReturnMsg(false, e.getMessage());
		}
		return ReturnMsg.defaultOk;
	}

	/*
	 *When add/save student record, check the uniqueness of student cardno
		- If student cardno is already exist, abort it
		- If student cardno is blank, allow duplicate 
	 * */
	private ReturnMsg checkUniqueCardNo(CellCollection col) {
		int sdrg = col.getCellInt("essd_rg");
		UniLog.log1("student rg:%d", sdrg);
		String cardNo = col.getCellString("essd_cardno");
		if (StringUtils.isBlank(cardNo)) {
			UniLog.log1("cardNo is blank");
			return ReturnMsg.defaultOk;
		}
		BiResult brStudent = null;
		try {
			brStudent = sh.newBiResult("edu.Student");
			brStudent.addCustomCondition(String.format("essd_cardno = '%s' and essd_rg <> %d", cardNo, sdrg));
			ReturnMsg rtn;
			if ((rtn = brStudent.query(true, false)).getStatus()) {
				if (brStudent.next())
					return new ReturnMsg(false, "Duplicate Card no");
			}
			else
				throw new Exception(rtn.getMsg());
		}
		catch (Exception e) {
			return new ReturnMsg(e);
		}
		finally {
			if (brStudent != null)
				brStudent.close();
		}
		return ReturnMsg.defaultOk;
	}
}
