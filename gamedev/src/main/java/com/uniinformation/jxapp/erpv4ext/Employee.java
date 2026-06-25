package com.uniinformation.jxapp.erpv4ext;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.image.AImage;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Image;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.bicore.erpv4ext.BiResultEmployee;
import static com.uniinformation.jxapp.erpv4ext.LeaveApplication.LEAVE_EXPIRE_YEAR;
import static com.uniinformation.jxapp.erpv4ext.LeaveApplication.LEAVEUNIT_MAX_CARRYFORWARD;

public class Employee extends JxZkBiBase {

	private Image imgEmployeePhoto;
	private Map<String, Object> setupShiftArrangeMap = new HashMap<String, Object>();

	@Override
	public void afterBind() {
		UniLog.log1("afterBind called");
		super.afterBind();
		Button btEmployeePhoto = (Button)jxAdd("btEmployeePhoto").getNativeObject();
		imgEmployeePhoto = (Image)(btEmployeePhoto.getParent().getFellowIfAny("imgEmployeePhoto"));
		btEmployeePhoto.setUpload("true,maxsize=1024,multiple=false,native,accept=.jpg");
		btEmployeePhoto.addEventListener(Events.ON_UPLOAD, new ZkBiEventListener<UploadEvent>() {
			@Override
			public void onZkBiEvent(UploadEvent event) throws Exception {
				UniLog.log1("upload event:%s", event.getMedia().getClass());
				((BiResultEmployee)getBr()).setEmployeePhotoData(event.getMedia().getByteData(), true);
				AImage aimage = new AImage("", event.getMedia().getByteData());
				imgEmployeePhoto.setContent(aimage);
				jxSetEnable("btRemoveEmployeePhoto", true);
				jxSetEnable("btDownloadEmployeePhoto", true);
				setDirtyFlag(true);
			}
		});
		jxAdd("btRemoveEmployeePhoto").addActionListener(new JxActionListener() {
			@Override
			public void actionPerformed(JxField field) {
				((BiResultEmployee)getBr()).setEmployeePhotoData(null, true);
				imgEmployeePhoto.setContent((AImage)null);
				jxSetEnable("btRemoveEmployeePhoto", false);
				jxSetEnable("btDownloadEmployeePhoto", false);
				setDirtyFlag(true);
			}
		});
		jxAdd("btDownloadEmployeePhoto").addActionListener(new JxActionListener() {
			@Override
			public void actionPerformed(JxField field) {
				byte[] data = ((BiResultEmployee)getBr()).getEmployeePhotoData();
				if (data != null)
					Filedownload.save(data, "image/jpeg", String.format("emphoto-%s.jpg", DateUtil.dateToDateTimeStr(new Date(), "yyyyMMddHHmmss")));
			}
		});
		jxAdd("btOfficeHour").addActionListener(new JxActionListener() {
			@Override
			public void actionPerformed(JxField field) {
				Shift.setupShiftArrange(sessionHelper, Employee.this, getBr(), "em_shtar", setupShiftArrangeMap);
			}
		});
	}

	@Override
	public void bindCellCollection(BiResult p_br, int mode) {
		UniLog.log1("bindCellCollection called");
		super.bindCellCollection(p_br, mode);

		((BiResultEmployee)getBr()).setEmployeePhotoData(null, false);
		imgEmployeePhoto.setContent((AImage)null);
		try {
			//Date endDate = p_br.getCellDate("em_enddate");
			//p_br.getCell("em_enddatex").set(endDate.compareTo(LeaveApplication.MAX_DATE) >= 0 ? DateUtil.zeroDate : endDate);

			String em_alst_day_str1, em_alst_day_str2;
			if (StringUtils.equalsAny(sessionHelper.getLHLang(), "TCHN", "SCHN")) {
				em_alst_day_str1 = String.format("年假計算: 每年%s日起算，限期至第%d年%s前", p_br.getCellString("em_alstday"), LEAVE_EXPIRE_YEAR + 1, p_br.getCellString("em_alendday"));
				if (LEAVEUNIT_MAX_CARRYFORWARD != 0)
					em_alst_day_str1 += String.format("，最多可帶%s天到第二年", LeaveApplication.getLeaveUnit2LvStr(LEAVEUNIT_MAX_CARRYFORWARD));
				em_alst_day_str2 = String.format("年假天數: 第一年%d天，從第%d年開始每年遞增一天，最多每年%d天", p_br.getCellInt("em_stalcnt"), p_br.getCellInt("em_ofsalcnt") + 2, p_br.getCellInt("em_maxalcnt"));
			} else {
				em_alst_day_str1 = String.format("Calculation of annual leave: Calculated from %s every year, the deadline is until %s of the %dth year", p_br.getCellString("em_alstday"), p_br.getCellString("em_alendday"), LEAVE_EXPIRE_YEAR + 1);
				if (LEAVEUNIT_MAX_CARRYFORWARD != 0)
					em_alst_day_str1 += String.format(", and a maximum of %s days can be carried to the second year", LeaveApplication.getLeaveUnit2LvStr(LEAVEUNIT_MAX_CARRYFORWARD));
				em_alst_day_str2 = String.format("Number of days of annual leave: %d days in the first year, increasing by one day every year from the %dth year, up to %d days per year", p_br.getCellInt("em_stalcnt"), p_br.getCellInt("em_ofsalcnt") + 2, p_br.getCellInt("em_maxalcnt"));
			}
			p_br.getCell("em_alst_day_str1").set(em_alst_day_str1);
			p_br.getCell("em_alst_day_str2").set(em_alst_day_str2);

			if (mode == JxZkBiBase.MODE_UPDATE) {
				((BiResultEmployee)getBr()).loadEmployeePhotoData();
				byte[] data = ((BiResultEmployee)getBr()).getEmployeePhotoData();
				if (data != null) {
					imgEmployeePhoto.setContent(new AImage("", data));
					jxSetEnable("btRemoveEmployeePhoto", true);
					jxSetEnable("btDownloadEmployeePhoto", true);
				} else {
					jxSetEnable("btRemoveEmployeePhoto", false);
					jxSetEnable("btDownloadEmployeePhoto", false);
				}
			}
		}
		catch (Exception ex) {
			UniLog.log1("Error:%s", ex.getMessage());
		}
	}
	
	@Override
	protected ReturnMsg beforeAdd(BiResult br) {
		ReturnMsg rtn = super.beforeAdd(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			rtn = validationRecord(false);
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		
		return rtn;
	}

	@Override
	protected ReturnMsg beforeUpdate(BiResult br) {
		ReturnMsg rtn = super.beforeUpdate(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			rtn = validationRecord(true);
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		
		return rtn;
	}
	
	private ReturnMsg validationRecord(boolean isUpdate) throws Exception {
		Date startDate = getBr().getCellDate("em_stdate");
		Date endDate = getBr().getCellDate("em_enddate");
		if (!DateUtil.isDateNull(startDate) && startDate.compareTo(LeaveApplication.MAX_DATE) >= 0)
			return(new ReturnMsg(false,"Invalid Join Date",true));
		if (!DateUtil.isDateNull(endDate) && endDate.compareTo(LeaveApplication.MAX_DATE) >= 0)
			return(new ReturnMsg(false,"Invalid Leave Date",true));
		if (!DateUtil.isDateNull(startDate) && !DateUtil.isDateNull(endDate) && startDate.compareTo(endDate) > 0)
			return(new ReturnMsg(false,"Leave Date cannot be less than Join Date",true));
		
		String leaveReasonCode = getBr().getCellString("drmt_code");
		String otherReason = getBr().getCellString("em_dimotherreason");
		if (StringUtils.equals(leaveReasonCode, "Other") && StringUtils.isBlank(otherReason))
			return(new ReturnMsg(false,"Please input Leave Other Reason",true));
		return ReturnMsg.defaultOk;
	}
}
