package com.uniinformation.bicore.erpv4ext;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellVector;
import com.uniinformation.jxapp.erpv4ext.LeaveApplication;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultEmployee extends BiResult {
	private static final String EMPLOYEE_PHOTO_KEY_PREFIX = "zkbi_erpv4ext_employee_photo_";
	private byte[] employeePhotoData;
	private boolean employeePhotoDataUploadFlag;
	
	public BiResultEmployee(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col, boolean isUpdate) {
		ReturnMsg rtn = super.biBeforeAddUpdateCurrent(col, isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			//calc trial end date
			Date joinDate = getCellDate("em_stdate");
			String probation = getCellString("em_probation");
			int monthCnt = NumberUtils.toInt(StringUtils.substring(probation, 0, 1));
			getCell("em_trialenddate").set(DateUtil.nextmonth(joinDate, monthCnt));

			//set enddate
			//Date endDate = getCellDate("em_enddatex");
			//getCell("em_enddate").set(DateUtil.isDateNull(endDate) ? LeaveApplication.MAX_DATE : endDate);

			//get rg
			String eid = col.getCellString("em_eid");
			if (StringUtils.isBlank(eid)) {
				Value v = getView().getSchema().getUniqueRg(this,"", 2005, "employee", "em_eid", "CT&&&&");
				eid = v.toString();
				getCell("em_eid").set(eid);
			}
			if (col.getCellInt("em_shtar") == 0) {
				Value v = getView().getSchema().getUniqueRg(this, "", 12001, "shiftarrange", "shtar_rg", "");
				getCell("em_shtar").set(v.toInt());
				getCell("shtar_rg").set(v.toInt());
			}
			
			String key = EMPLOYEE_PHOTO_KEY_PREFIX + eid;
			if (employeePhotoDataUploadFlag) {
				if (employeePhotoData != null) {
					//store photo
					ByteArrayInputStream bis = new ByteArrayInputStream(employeePhotoData);
					FilingUtil.storeFile(sh.getAgent(), null, key, key, eid + ".jpg", bis);
					bis.close();
				} else
					removeEmployeePhotoData();
			}
			employeePhotoDataUploadFlag = false;
		} catch (Exception cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,-1,cex.getMessage()));
		}
		return(rtn);
	}

	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col, boolean isUpdate) {
		ReturnMsg rtn = super.biAfterAddUpdateCurrent(col, isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			String eid = col.getCellString("em_eid");
			Date joinDate = getCellDate("em_stdate");
			Date startDate = getCellDate("emg_stdate");
			int emtypeRg = getCellInt("etmt_rg");
			int deptRg = getCellInt("dpmt_rg");
			int gradeRg = getCellInt("gdmt_rg");
			int postRg = getCellInt("ptmt_rg");
			if (isUpdate) {
				//update emgrade
				su.executeUpdate("update emgrade set emg_graderg = ?, emg_deptrg = ?, emg_postrg = ?, emg_emtyperg = ? where emg_eid = ? and emg_stdate = ?", 
					new Wherecl().appendArgument(gradeRg)
								.appendArgument(deptRg)
								.appendArgument(postRg)
								.appendArgument(emtypeRg)
								.appendArgument(eid)
								.appendArgument(startDate));
				updateEmGrade();
				/*su.executeUpdate("update emgrade set emg_stdate = ?, emg_graderg = ?, emg_deptrg = ?, emg_postrg = ?, emg_emtyperg = ? where emg_eid = ? and emg_stdate <= today and emg_enddate >= today", 
					new Wherecl().appendArgument(startDate)
								.appendArgument(gradeRg)
								.appendArgument(deptRg)
								.appendArgument(postRg)
								.appendArgument(emtypeRg)
								.appendArgument(eid));*/
			} else {
				//insert into emgrade
				su.executeUpdate("insert into emgrade (emg_eid, emg_stdate, emg_enddate, emg_deptrg, emg_postrg, emg_graderg, emg_emtyperg, emg_wgtype, emg_includepay, emg_poststatus, emg_tranreason, emg_wage, emg_shiftcode) values(?,?,?,?,?,?,?,?,?,?,?,?,?)",
					new Wherecl().appendArgument(eid)
								.appendArgument(joinDate)
								.appendArgument(LeaveApplication.MAX_DATE)
								.appendArgument(deptRg)
								.appendArgument(postRg)
								.appendArgument(gradeRg)
								.appendArgument(emtypeRg)
								.appendArgument("M")
								.appendArgument("Y")
								.appendArgument("")
								.appendArgument("")
								.appendArgument(0f)
								.appendArgument(""));
			}
		} catch (Exception cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,-1,cex.getMessage()));
		}
		
		return rtn;
	}

	@Override
	protected ReturnMsg biAfterDeleteCurrent(CellCollection col) {
		ReturnMsg rtn = super.biAfterDeleteCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			String eid = getCellString("em_eid");
			su.executeUpdate("delete from emgrade where emg_eid = ?", new Wherecl().appendArgument(eid));
			su.executeUpdate("delete from emincome where emic_eid = ?", new Wherecl().appendArgument(eid));
			su.executeUpdate("delete from emdeduction where emde_eid = ?", new Wherecl().appendArgument(eid));
			su.executeUpdate("delete from empension where empe_eid = ?", new Wherecl().appendArgument(eid));
			removeEmployeePhotoData();
		}
		catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,-1,ex.getMessage()));
		}

		return(rtn);
	}
	
	public void setEmployeePhotoData(byte[] data, boolean flag) {
		employeePhotoData = data;
		employeePhotoDataUploadFlag = flag;
	}
	
	public byte[] getEmployeePhotoData() {
		return employeePhotoData;
	}
	
	public void loadEmployeePhotoData() {
		employeePhotoData = null;
		employeePhotoDataUploadFlag = false;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			String key = EMPLOYEE_PHOTO_KEY_PREFIX + getCellString("em_eid");
			if (FilingUtil.getFile(sh.getAgent(), null, key, bos) != null)
				employeePhotoData = bos.toByteArray();
		}
		catch (Exception ex) {
			UniLog.log1("Error:%s", ex.getMessage());
			try {
				bos.close();
			} catch (IOException e) {
			}
		}
	}

	private void removeEmployeePhotoData() throws Exception {
		String eid = getCellString("em_eid");
		String key = EMPLOYEE_PHOTO_KEY_PREFIX + eid;
		FilingUtil.deleteFile(sh.getAgent(), null, key);
	}
	
	private void updateEmGrade() throws Exception {
		String eid = getCellString("em_eid");
		Date joinDate = getCellDate("em_stdate");
		CellVector cv = su.getQueryResultToCellVector("select serial_id, emg_stdate, emg_enddate from emgrade where emg_eid = ? order by emg_stdate", new Wherecl().appendArgument(eid));
		for (int i = cv.size() - 1; i >= 0; i--) {
			CellCollection cc = (CellCollection)cv.get(i);
			Date startDate = cc.getDate("emg_stdate");
			int sid = cc.getCellInt("serial_id");
			if (startDate.compareTo(joinDate) == 0)
				return;
			if (startDate.compareTo(joinDate) < 0 || i == 0) {
				su.executeUpdate("update emgrade set emg_stdate = ? where serial_id = ?", new Wherecl().appendArgument(joinDate).appendArgument(sid));
				Wherecl wherecl = new Wherecl().appendArgument(joinDate).appendArgument(eid).appendArgument(startDate);
				su.executeUpdate("update emincome set emic_date = ? where emic_eid = ? and emic_date = ?", wherecl);
				su.executeUpdate("update emdeduction set emde_date = ? where emde_eid = ? and emde_date = ?", wherecl);
				su.executeUpdate("update empension set empe_date = ? where empe_eid = ? and empe_date = ?", wherecl);
				if (i > 0) {
					cc = (CellCollection)cv.get(i - 1);
					startDate = cc.getDate("emg_stdate");
					sid = cc.getCellInt("serial_id");
					su.executeUpdate("update emgrade set emg_enddate = ? where serial_id = ?", new Wherecl().appendArgument(DateUtil.prevday(joinDate)).appendArgument(sid));
					wherecl = new Wherecl().appendArgument(DateUtil.prevday(joinDate)).appendArgument(eid).appendArgument(startDate);
					su.executeUpdate("update emincome set emic_enddate = ? where emic_eid = ? and emic_date = ?", wherecl);
					su.executeUpdate("update emdeduction set emde_enddate = ? where emde_eid = ? and emde_date = ?", wherecl);
					su.executeUpdate("update empension set empe_enddate = ? where empe_eid = ? and empe_date = ?", wherecl);
				}
				return;
			}
		}
	}
}