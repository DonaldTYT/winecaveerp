package com.uniinformation.zkbi.edu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zhtml.Br;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Space;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.erpv4.edu.ProcessScanLog;
import com.uniinformation.jxapp.edu.Student;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiHelpDialog;

public class ZkBiComposerTodayAttendance extends ZkComposerBase {
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	private final SimpleDateFormat tdf = new SimpleDateFormat("HH:mm");
	private final SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
	
	private Date currentDate;
	private final List<Map<String, Object>> sessionList = new ArrayList<Map<String, Object>>();
	private final Map<Integer, Map<String, Object>> courseMap = new HashMap<Integer, Map<String, Object>>(); //key: courseRg
	private final Set<Integer> excludeCourseList = new HashSet<Integer>();

	@Wire
	Window winTodayAttend;
	@Wire
	Label lbRefreshTime;
	@Wire
	Datebox dbTargetAttend; //The target attendance date. today by default
	@Wire
	Spinner spWarningThreshold; //Warning Threshold - for determine warning state. 10min by default
	@Wire
	Button btRefresh; //refresh the attendance list to obtain latest attendance data.
	@Wire
	Checkbox cbAttendanceCompleteSession; //toggle attendance complete session - ON show attendance complete/incomplete session; OFF hide attendance complete session. default ON
											//Remark: attednance complete means all enrolled student status is Present/Absent/Leave
	@Wire
	Checkbox cbFinishedSession; //toggle finish session - ON show finished/non-finished session based on session endtime, OFF hide finished session. default ON
	@Wire
	Checkbox cbSubscriptionCancelled; //Subscription Cancelled - ON Show the student even the subscription status is cancelled, OFF Do not show the student if subscription status is cancelled
	@Wire
	Checkbox cbProcessScanLog; //When the toggle is ON and User click the Today Attendance > Refresh button, process the scan log record first, then refresh the attendance data.
	@Wire
	Listbox lbMain;

	@Wire
	Toolbarbutton btnHelp;

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("called");

		btnHelp = (Toolbarbutton) winTodayAttend.query("#btnHelp");
		
		dbTargetAttend.setValue(DateUtil.today());
		
		dbTargetAttend.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				clearSessionDatas();
			}
		});
		btRefresh.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				if (cbProcessScanLog.isChecked()) {
					//process scan log
					ProcessScanLog o = ProcessScanLog.getProcessScanLogObject();
					if (o != null) {
						ReturnMsg rtn = o.updateAttend();
						if (rtn.getStatus())
							UniLog.log("Process scan log done");
						else
							UniLog.log1("Process scan log fail: %s", rtn.getMsg());
					}
					else
						UniLog.log("Cannot call process scan log function");
				}
				loadSessionDatas(dbTargetAttend.getValue());
				ZkUtil.showMsg("Data Updated");
			}
		});
		cbAttendanceCompleteSession.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
			@Override
			public void onZkBiEvent(CheckEvent event) throws Exception {
				for (Map<String, Object> m : sessionList) {
					Listitem li = (Listitem)m.get("listItem");
					Date sessionEndTime = (Date)m.get("sessionEndTime");
					boolean isCompletedAtt = (Boolean)m.get("isCompletedAtt");
					boolean isFinished = sessionEndTime.compareTo(DateUtil.now()) <= 0;
					List<Map<String, Object>> attendanceList = (List<Map<String, Object>>)m.get("attendanceList");
					List<Div> subCancelledDivList = (List<Div>)m.get("subCancelledDivList");
					if (isCompletedAtt && isFinished)
						li.setVisible(event.isChecked() && cbFinishedSession.isChecked());
					else if (isCompletedAtt && !isFinished)
						li.setVisible(event.isChecked());
					else if (!isCompletedAtt && isFinished)
						li.setVisible(cbFinishedSession.isChecked());
					else
						li.setVisible(true);
					if (li.isVisible() && !attendanceList.isEmpty() && attendanceList.size() == subCancelledDivList.size() && !cbSubscriptionCancelled.isChecked())
						li.setVisible(false);
				}
			}
		});
		cbFinishedSession.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
			@Override
			public void onZkBiEvent(CheckEvent event) throws Exception {
				for (Map<String, Object> m : sessionList) {
					Listitem li = (Listitem)m.get("listItem");
					Date sessionEndTime = (Date)m.get("sessionEndTime");
					boolean isCompletedAtt = (Boolean)m.get("isCompletedAtt");
					boolean isFinished = sessionEndTime.compareTo(DateUtil.now()) <= 0;
					List<Map<String, Object>> attendanceList = (List<Map<String, Object>>)m.get("attendanceList");
					List<Div> subCancelledDivList = (List<Div>)m.get("subCancelledDivList");
					if (isCompletedAtt && isFinished)
						li.setVisible(cbAttendanceCompleteSession.isChecked() && event.isChecked());
					else if (isCompletedAtt && !isFinished)
						li.setVisible(cbAttendanceCompleteSession.isChecked());
					else if (!isCompletedAtt && isFinished)
						li.setVisible(event.isChecked());
					else
						li.setVisible(true);
					if (li.isVisible() && !attendanceList.isEmpty() && attendanceList.size() == subCancelledDivList.size() && !cbSubscriptionCancelled.isChecked())
						li.setVisible(false);
				}
			}
		});
		cbSubscriptionCancelled.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
			@Override
			public void onZkBiEvent(CheckEvent event) throws Exception {
				for (Map<String, Object> m : sessionList) {
					Listitem li = (Listitem)m.get("listItem");
					Date sessionEndTime = (Date)m.get("sessionEndTime");
					boolean isCompletedAtt = (Boolean)m.get("isCompletedAtt");
					boolean isFinished = sessionEndTime.compareTo(DateUtil.now()) <= 0;
					List<Map<String, Object>> attendanceList = (List<Map<String, Object>>)m.get("attendanceList");
					List<Div> subCancelledDivList = (List<Div>)m.get("subCancelledDivList");
					if (isCompletedAtt && isFinished)
						li.setVisible(cbAttendanceCompleteSession.isChecked() && cbFinishedSession.isChecked());
					else if (isCompletedAtt && !isFinished)
						li.setVisible(cbAttendanceCompleteSession.isChecked());
					else if (!isCompletedAtt && isFinished)
						li.setVisible(cbFinishedSession.isChecked());
					else
						li.setVisible(true);
					if (li.isVisible() && !attendanceList.isEmpty() && attendanceList.size() == subCancelledDivList.size() && !cbSubscriptionCancelled.isChecked())
						li.setVisible(false);
					for (Div div : subCancelledDivList)
						div.setVisible(event.isChecked());
				}
			}
		});
		Clients.showNotification("Click Refresh to obtain attendance data", "info", btRefresh,"end_center", 5000, true); 
		
		String helpId = StringUtils.defaultIfBlank(Executions.getCurrent().getParameter("helpid"), "edu.TodayAttendance");
		UniLog.log1("helpId :%s", helpId);
		new ZkBiHelpDialog(sessionHelper, btnHelp, winTodayAttend, winTodayAttend.getTitle(), helpId, winTodayAttend.getTitle());
	}
	
	private void clearSessionDatas() {
		lbRefreshTime.setValue("");
		sessionList.clear();
		courseMap.clear();
		excludeCourseList.clear();
		while (lbMain.getItems().size() > 0)
			lbMain.removeItemAt(0);
	}
	
	private void loadSessionDatas(Date pDate) {
		UniLog.log1("loadSessionDatas pDate:%s, spWarningThreshold:%d", pDate, spWarningThreshold.intValue());
		currentDate = pDate;
		clearSessionDatas();
		
		lbRefreshTime.setValue(df.format(DateUtil.now()));
		if (DateUtil.isDateNull(currentDate))
			return;

		BiResult brCourseSession = null;
		BiResult brCourse = null;
		BiResult brCourseStudent = null;
		try {
			brCourseSession = sessionHelper.newBiResult("edu.CourseSessionDet");
			brCourse = sessionHelper.newBiResult("edu.Course");
			brCourseStudent = sessionHelper.newBiResult("edu.CourseStudent");

			brCourseSession.clearCondition();
			if (StringUtils.isNotBlank(getCurrentTutorCode()))
				brCourseSession.addCustomCondition(String.format("estt_ttno = '%s'", getCurrentTutorCode()));
			brCourseSession.addCustomCondition(String.format("essncs_date = '%s'", sdf.format(currentDate)));
			ReturnMsg rtn;
			if ((rtn = brCourseSession.query(true, false)).getStatus()) {
				while (brCourseSession.next())
					loadSessionDatasOne(currentDate, brCourseSession, brCourse, brCourseStudent);
			}
			else
				throw new Exception(rtn.getMsg());

			for (Map<String, Object> cm : courseMap.values()) {
				int courseRg = (Integer)cm.get("eaav0_rg");
				cm.put("studentMap", findCourseStudentDatas(courseRg));
			}
			for (Map<String, Object> sm : sessionList) {
				final int sessionRg = (Integer)sm.get("sessionRg");
				final int courseRg = (Integer)sm.get("courseRg");
				final Date sessionDate = (Date)sm.get("sessionDate");
				final Date sessionStartTime = (Date)sm.get("sessionStartTime");
				final Date sessionEndTime = (Date)sm.get("sessionEndTime");
				final Map<Integer, Map<String, Object>> studentMap = (Map<Integer, Map<String, Object>>)courseMap.get(courseRg).get("studentMap");
				final Map<Integer, String> attendanceMap = findAttendancePresentDatas(sessionRg);

				final List<Map<String, Object>> attendanceList = new ArrayList<Map<String, Object>>();
				boolean isCompletedAtt = true; //attednance complete means all enrolled student status is Present/Absent/Leave
				for (Map.Entry<Integer, Map<String, Object>> entry : studentMap.entrySet()) { //stm: course student map
					final int studentRg = entry.getKey();
					final Map<String, Object> stm = entry.getValue();
					final String attStatus = attendanceMap.get(studentRg);
					final String sdNo = (String)stm.get("essd_sdno");
					final String sdName = (String)stm.get("essd_name");
					final Date subStartDate = (Date)stm.get("essbsd_startdate");
					final Date subEndDate = (Date)stm.get("essbsd_enddate");
					final String subStatus = (String)stm.get("essbsd_status");
					final boolean isInEnrolledList = sessionDate != null && !DateUtil.isDateNull(subStartDate) && !DateUtil.isDateNull(subEndDate) 
										&& sessionDate.compareTo(subStartDate) >= 0 && sessionDate.compareTo(subEndDate) <= 0;

					if (attStatus == null)
						isCompletedAtt = false;
					UniLog.log1("courseRg:%d, sessionRg:%d, sdNo:%d, attStatus:%s, isInEnrolledList:%b, sessionDate:%s, subStartDate:%s, subEndDate:%s", courseRg, sessionRg, sdNo, attStatus, isInEnrolledList, sessionDate, subStartDate, subEndDate);
					if (isInEnrolledList || attStatus != null) {
						attendanceList.add(new HashMap<String, Object>(){{
							/*
							 - There are 5 classes of attendance status:
	  						 - Present - Marked Present
	   						 - Absent - Marked Absent
	   						 - Leave - Marked Leave
	   						 - Warning - Current time > Session start time - Threshold
	   						 - Expired - Current time > Session start time
							 */
							String status = "";
							if (attStatus != null && !StringUtils.equals(attStatus, "Reserve"))
								status = attStatus.toLowerCase();
							else {
								if (System.currentTimeMillis() > sessionStartTime.getTime())
									status = "expired";
								else if (System.currentTimeMillis() > sessionStartTime.getTime() - spWarningThreshold.intValue() * 60 * 1000)
									status = "warn";
							}
							put("studentRg", studentRg);
							put("studentName", sdName);
							put("studentNo", sdNo);
							put("subStartdate", subStartDate);
							put("subEnddate", subEndDate);
							put("subStatus", subStatus);
							put("attStatus", attStatus);
							put("description", String.format("%s (%s)", sdName, sdNo));
							put("status", status);
							put("isInEnrolledList", isInEnrolledList);
							UniLog.log1("sessionRg:%d, sdNo:%s, attStatus:%s, status:%s", sessionRg, sdNo, attStatus, status);
						}});
					}
				}
				//The order should be same as mark attendance page.
				attendanceList.sort(new Comparator<Map<String, Object>>(){
					@Override
					public int compare(Map<String, Object> o1, Map<String, Object> o2) {
						return StringUtils.compare((String)o1.get("description"), (String)o2.get("description"));
					}
				});
				sm.put("attendanceList", attendanceList);
				sm.put("isCompletedAtt", isCompletedAtt);
				UniLog.log1("sessionRg:%d, sessionStartTime:%s, sessionEndTime:%s, isCompletedAtt:%b", sessionRg, sessionStartTime, sessionEndTime, isCompletedAtt);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (brCourse != null)
				brCourse.close();
			if (brCourseStudent != null)
				brCourseStudent.close();
			if (brCourseSession != null)
				brCourseSession.close();
		}
		//remove all session item of empty attendance list
		Iterator<Map<String, Object>> it = sessionList.iterator();
		while (it.hasNext()) {
			Map<String, Object> m = it.next();
			if (((List<Map<String, Object>>)m.get("attendanceList")).isEmpty())
				it.remove();
		}
		
		//Order by session starttime
		sessionList.sort(new Comparator<Map<String, Object>>(){
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				Date sessionStartTime1 = (Date) o1.get("sessionStartTime");
				Date sessionStartTime2 = (Date) o2.get("sessionStartTime");
				String desc1 = o1.get("courseCode") + " - " + o1.get("courseName");
				String desc2 = o2.get("courseCode") + " - " + o2.get("courseName");
				if (sessionStartTime1.compareTo(sessionStartTime2) < 0)
					return -1;
				else if (sessionStartTime1.compareTo(sessionStartTime2) > 0)
					return 1;
				else 
					return desc1.compareTo(desc2);
			}
		});
		
		Listitem markListitem = null, markListitem1 = null;
		for (final Map<String, Object> m : sessionList) {
			final Date startDateTime = (Date)m.get("sessionStartTime");
			final Date endDateTime = (Date)m.get("sessionEndTime");
			final int courseRg = (Integer)m.get("courseRg");
			final String courseCode = (String)m.get("courseCode");
			final String courseName = (String)m.get("courseName");
			final Date sessionStartTime = (Date)m.get("sessionStartTime");
			final Date sessionEndTime = (Date)m.get("sessionEndTime");
			final boolean isCompletedAtt = (Boolean)m.get("isCompletedAtt");
			final boolean isFinished = sessionEndTime.compareTo(DateUtil.now()) <= 0;
			final List<Map<String, Object>> attendanceList = (List<Map<String, Object>>)m.get("attendanceList");
			
			final List<Div> subCancelledDivList = new ArrayList<Div>();
			m.put("subCancelledDivList", subCancelledDivList);

			final Listitem li = new Listitem();
			final Button btCourse = new Button("Course");
			li.appendChild(new Listcell() {{
				appendChild(new Hbox() {{
					appendChild(new Label(df.format(startDateTime) + " - " + tdf.format(endDateTime)));
					appendChild(new Space());
					appendChild(new Label("Code: " + courseCode));
					appendChild(new Space());
					appendChild(new Label("Name: " + courseName));
				}});
				if (!attendanceList.isEmpty()) {
					appendChild(new Div() {{
						setStyle("display:flex;flex-wrap:wrap");
						for (Map<String, Object> atm : attendanceList) {
							final String sdName = (String)atm.get("studentName");
							final String sdNo = (String)atm.get("studentNo");
							final String status = (String)atm.get("status");
							final String subStatus = (String)atm.get("subStatus");
							final boolean isInEnrolledList = (Boolean)atm.get("isInEnrolledList");
							final Div div = new Div() {{
								setSclass("attbox " + status + (isInEnrolledList ? "" : " outrange"));
								appendChild(new Label(sdName));
								appendChild(new Br());
								appendChild(new Label(sdNo));
							}};
							appendChild(div);
							if (StringUtils.equals(subStatus, "Cancelled")) {
								subCancelledDivList.add(div);
								if (!cbSubscriptionCancelled.isChecked())
									div.setVisible(false);
							}
						}
					}});
				}
			}});
			li.appendChild(new Listcell() {{
				appendChild(btCourse);
			}});
			lbMain.getItems().add(li);

			if (isCompletedAtt && isFinished)
				li.setVisible(cbAttendanceCompleteSession.isChecked() && cbFinishedSession.isChecked());
			else if (isCompletedAtt && !isFinished)
				li.setVisible(cbAttendanceCompleteSession.isChecked());
			else if (!isCompletedAtt && isFinished)
				li.setVisible(cbFinishedSession.isChecked());
			else
				li.setVisible(true);
			
			//hide listitem if all student subscription cancelled
			if (li.isVisible() && !attendanceList.isEmpty() && attendanceList.size() == subCancelledDivList.size() && !cbSubscriptionCancelled.isChecked())
				li.setVisible(false);

			if (currentDate.compareTo(DateUtil.today()) == 0) {
				//scroll to nearest session based on current time
				if (sessionStartTime.compareTo(DateUtil.now()) < 0)
					markListitem = li;
				else if (markListitem1 == null)
					markListitem = markListitem1 = li;
			}
			m.put("listItem", li);
			
			btCourse.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					try {
						JSONObject jo = new JSONObject();
						JSONArray ja = new JSONArray();
						jo.put("tablist", ja);
						BiView pov = sessionHelper.getBiSchema().getViewByName("edu.Course");
						ja.put(pov.getTable().getName());
						jo.put("wherestr", "eaav0_rg = " + courseRg);
						String key = sessionHelper.putOneTimeData(jo);
						ZkUtil.js("openNewTab('%s')", "zkbiloader.html?action=update&viewid=edu.Course&page_id=course_01&zul=zkbiloader.zul&prefix=zkbi&composer=edu.ZkBiComposerCourse&closetab=Y&sidemenu=N&querycondition="+key);
						UniLog.log1("jo:%s", jo);
					}
					catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			});
		}
		if (markListitem != null)
			Clients.scrollIntoView(markListitem);
	}


	private void loadSessionDatasOne(Date startDate, BiResult brCourseSession, BiResult brCourse, BiResult brCourseStudent) throws Exception {
		int avrg = brCourseSession.getCellInt("essncs_avrg");
		int rg = brCourseSession.getCellInt("essncs_rg");
		int type = brCourseSession.getCellInt("essncs_type");
		Date date = brCourseSession.getCellDate("essncs_date");
		Date startTime = brCourseSession.getCellDate("essncs_sttime");
		Date endTime = brCourseSession.getCellDate("essncs_endtime");
		String remark = brCourseSession.getCellString("essncs_name");
		int ttrg = brCourseSession.getCellInt("esattt_atrg");
		int fcrg = brCourseSession.getCellInt("esatfc_atrg");
		String ttNo = brCourseSession.getCellString("estt_ttno");
		String ttName = brCourseSession.getCellString("estt_name");
		String fcCode = brCourseSession.getCellString("esfc_code");
		String fcName = brCourseSession.getCellString("esfc_name");
		
		if (excludeCourseList.contains(avrg))
			return;
		Map<String, Object> m = courseMap.get(avrg);
		if (m == null) {
			Date sdStartDate = null, sdEndDate = null;
			ReturnMsg rtn;
			if (StringUtils.isNotBlank(getCurrentStudentCode())) {
				brCourseStudent.clearCondition();
				brCourseStudent.addCustomCondition(String.format("essbsd_avrg = %d and essd_sdno = '%s'", avrg, getCurrentStudentCode()));
				if ((rtn = brCourseStudent.query(true, false)).getStatus()) {
					if (!brCourseStudent.next()) {
						excludeCourseList.add(avrg);
						return;
					}
					sdStartDate = brCourseStudent.getCellDate("essbsd_startdate");
					sdEndDate = brCourseStudent.getCellDate("essbsd_enddate");
				}
				else
					throw new Exception(rtn.getMsg());
			}

			brCourse.clearCondition();
			brCourse.addCustomCondition(String.format("eaav0_rg = %d", avrg));
			if ((rtn = brCourse.query(true, false)).getStatus()) {
				if (brCourse.next()) {
					UniLog.log1("add course");
					//add course
					int cttrg = brCourse.getCellInt("eaav0_esttrg");
					int cfcrg = brCourse.getCellInt("eaav0_esfcrg");
					m = new HashMap<String, Object>();
					m.put("eaav0_rg", brCourse.getCellInt("eaav0_rg"));
					m.put("eaav0_code", brCourse.getCellString("eaav0_code"));
					m.put("eaav0_name", brCourse.getCellString("eaav0_name"));
					m.put("eaav0_esttrg", cttrg);
					m.put("estt_ttno", brCourse.getCellString("estt_ttno"));
					m.put("estt_name", brCourse.getCellString("estt_name"));
					m.put("eaav0_esfcrg", cfcrg);
					m.put("esfc_code", brCourse.getCellString("esfc_code"));
					m.put("esfc_name", brCourse.getCellString("esfc_name"));
					m.put("sdStartDate", sdStartDate);
					m.put("sdEndDate", sdEndDate);
					m.put("visibled", true);
					courseMap.put(avrg, m);
				}
				else {
					excludeCourseList.add(avrg);
					return;
				}
			}
			else
				throw new Exception(rtn.getMsg());
		}
		//only include subscribe session for student
		if (StringUtils.isNotBlank(getCurrentStudentCode())) {
			Date sdStartDate = (Date)m.get("sdStartDate");
			Date sdEndDate = (Date)m.get("sdEndDate");
			if (DateUtil.isDateNull(sdStartDate) || DateUtil.isDateNull(sdEndDate) || date.compareTo(sdStartDate) < 0 || date.compareTo(sdEndDate) > 0)
				return;
		}

		Map<String, Object> m1 = new HashMap<String, Object>();
		m1.put("sessionDate", date);
		m1.put("sessionStartTime", Student.unionDateTime(date, startTime));
		m1.put("sessionEndTime", Student.unionDateTime(date, endTime));
		m1.put("courseRg", avrg);
		m1.put("sessionRg", rg);
		m1.put("courseCode", m.get("eaav0_code"));
		m1.put("courseName", m.get("eaav0_name"));
		UniLog.log1("add session %d", rg);
		sessionList.add(m1);
	}

	private Map<Integer, Map<String, Object>> findCourseStudentDatas(int courseRg) throws Exception {
		Map<Integer, Map<String, Object>> studentMap = new HashMap<Integer, Map<String, Object>>();
		BiResult brStudent = null;
		try {
			brStudent = sessionHelper.newBiResult("edu.CourseStudent");
			//brStudent.addCustomCondition(String.format("essbsd_avrg = %d and essbsd_status <> 'Cancelled'", courseRg));
			brStudent.addCustomCondition(String.format("essbsd_avrg = %d", courseRg));
			ReturnMsg rtn;
			if ((rtn = brStudent.query(true, false)).getStatus()) {
				while (brStudent.next()) {
					int studentRg = brStudent.getCellInt("essbsd_sdrg");
					Map<String, Object> m = new HashMap<String, Object>();
					m.put("essd_sdno", brStudent.getCellString("essd_sdno"));
					m.put("essd_name", brStudent.getCellString("essd_name"));
					m.put("essbsd_startdate", brStudent.getCellDate("essbsd_startdate"));
					m.put("essbsd_enddate", brStudent.getCellDate("essbsd_enddate"));
					m.put("essbsd_status", brStudent.getCellString("essbsd_status"));
					studentMap.put(studentRg, m);
				}
			}
			else
				throw new Exception(rtn.getMsg());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			if (brStudent != null)
				brStudent.close();
		}
		return studentMap;
	}

	private Map<Integer, String> findAttendancePresentDatas(int sessionRg) throws Exception {
		Map<Integer, String> presentMap = new HashMap<Integer, String>();
		BiResult brAttendance = null;
		try {
			brAttendance = sessionHelper.newBiResult("edu.Attendance");
			brAttendance.addCustomCondition(String.format("esatsd_snrg = %d", sessionRg));
			ReturnMsg rtn;
			if ((rtn = brAttendance.query(true, false)).getStatus()) {
				while (brAttendance.next()) {
					int studentRg = brAttendance.getCellInt("esatsd_atrg");
					presentMap.put(studentRg, brAttendance.getCellString("esatsd_status"));
				}
			}
			else
				throw new Exception(rtn.getMsg());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		finally {
			if (brAttendance != null)
				brAttendance.close();
		}
		return presentMap;
	}

	private String getCurrentStudentCode() {
		//if (StringUtils.isNotBlank(mCurStudentCode))
		//	return mCurStudentCode;
		if (!sessionHelper.isAdminUser() && sessionHelper.hasAccessRight("#student"))
			return sessionHelper.getLoginId().toUpperCase();
		return "";
	}

	private String getCurrentTutorCode() {
		//if (StringUtils.isNotBlank(mCurTutorCode))
		//	return mCurTutorCode;
		if (!sessionHelper.isAdminUser() && sessionHelper.hasAccessRight("#tutor"))
			return sessionHelper.getLoginId().toUpperCase();
		return "";
	}
}
