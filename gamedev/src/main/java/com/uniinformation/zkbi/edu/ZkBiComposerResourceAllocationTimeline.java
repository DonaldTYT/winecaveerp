package com.uniinformation.zkbi.edu;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;
import org.zkoss.zul.Radio;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiHelpDialog;

import static com.uniinformation.jxapp.edu.Student.joinString;
import static com.uniinformation.jxapp.edu.Student.unionDateTime;

public class ZkBiComposerResourceAllocationTimeline extends ZkComposerBase {
	private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") {{ setTimeZone(TimeZone.getTimeZone("UTC")); }};
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	private final int TIMELINE_SHOW_HOUR_FOR_DAYMODE = 6;

	//private final Map<Date, JSONObject> holidayMap = new HashMap<Date, JSONObject>();
	private final Map<Integer, JSONObject> sessionMap = new HashMap<Integer, JSONObject>(); //key: sessionRg
	private final Map<Integer, Map<String, Object>> courseMap = new HashMap<Integer, Map<String, Object>>(); //key: courseRg
	private final Map<Integer, Map<String, Object>> assessmentMap = new HashMap<Integer, Map<String, Object>>(); //key: sessionRg
	private final Map<Integer, Map<String, Object>> tutorMap = new HashMap<Integer, Map<String, Object>>(); //key: tutorRg
	private final Map<Integer, Map<String, Object>> facilityMap = new HashMap<Integer, Map<String, Object>>(); //key: facilityRg

	private final Set<Integer> excludeCourseList = new HashSet<Integer>();
	private final Map<Date, Set<Integer>> sessionDateMap = new HashMap<Date, Set<Integer>>(); //key: date, value: sessionMap key List
	//private final Map<Integer, Integer> sessionIdMap = new HashMap<Integer, Integer>(); //key: id, value: session rg
	
	private TimelineType timelineType = TimelineType.TUTOR;
	private TimelineMode timelineMode = TimelineMode.DAY;
	private Date currentDate;
	private Date activeStartDate, activeEndDate;
	private Date timelineMinDate, timelineMaxDate;
	//private Set<Date> loadedSessionMonthStartDateList = new TreeSet<Date>();

	private int maxEventId = 1;
	private int maxItemId = 1;
	private int maxGroupId = 1;
	
	@Wire
	Window winResourceAllocationTimeline;
	@Wire
	Combobox cbResourceType;
	@Wire
	Datebox dbFrom, dbTo;
	@Wire
	Label lblTo;
	@Wire
	private Button btnToday, btnPrev, btnNext;
	@Wire
	private Radiogroup rdgDayOrWeek;
	@Wire
	private Radio rdDay, rdWeek;
	@Wire
	private Div visDiv;
	@Wire
	Toolbarbutton btnHelp;
	
	private enum TimelineType { TUTOR, FACILITY };
	private enum TimelineMode { DAY, WEEK };

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		if (!accessOkFlag) {
			return;
		}
		UniLog.log1("called ZkBiComposerCalendar:%s, accessRights:%s", p_comp, sessionHelper.getAccessRights());
		
		dbFrom.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<InputEvent>() {
			@Override
			public void onZkBiEvent(InputEvent event) throws Exception {
				UniLog.log1("event:%s, value:%s, dbFrom:%s", event, event.getValue(), dbFrom.getValue());
				if (!DateUtil.isDateNull(dbFrom.getValue()))
					drawTimeline(dbFrom.getValue());
				else
					Clients.showNotification("Please input date", "error", dbFrom, "end_center", 5000, true); 
			}
		});
		btnToday.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			@Override
  			public void onZkBiEvent(Event event) throws Exception {
				drawTimeline(DateUtil.today());
  			}
		});
		btnPrev.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			@Override
  			public void onZkBiEvent(Event event) throws Exception {
				drawTimeline(timelineMode == TimelineMode.WEEK ? DateUtil.prevweek(currentDate) : DateUtil.prevday(currentDate));
  			}
		});
		btnNext.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			@Override
  			public void onZkBiEvent(Event event) throws Exception {
				drawTimeline(timelineMode == TimelineMode.WEEK ? DateUtil.nextweek(currentDate) : DateUtil.nextday(currentDate));
  			}
		});
		rdgDayOrWeek.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
			@Override
			public void onZkBiEvent(CheckEvent event) throws Exception {
				timelineMode = event.getTarget() == rdWeek ? TimelineMode.WEEK : TimelineMode.DAY;
				drawTimeline(currentDate);
			}
		});
		cbResourceType.addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Comboitem, Event>>() {
			@Override
			public void onZkBiEvent(SelectEvent<Comboitem, Event> event) throws Exception {
				UniLog.log1("event:%s, label:%s", event, event.getReference().getLabel());
				timelineType = StringUtils.equals(event.getReference().getLabel(), "Facility") ? TimelineType.FACILITY : TimelineType.TUTOR;
				drawTimeline();
			}
		});
		cbResourceType.setSelectedIndex(0);

		btnHelp = (Toolbarbutton) winResourceAllocationTimeline.query("#btnHelp");
		String helpId = StringUtils.defaultIfBlank(Executions.getCurrent().getParameter("helpid"), "edu.ResourceAllocationTimeline");
		UniLog.log1("helpId: %s", helpId);
   		new ZkBiHelpDialog(sessionHelper, btnHelp, winResourceAllocationTimeline, winResourceAllocationTimeline.getTitle(), helpId, winResourceAllocationTimeline.getTitle());

		//holidayMap.clear();
		sessionMap.clear();
		courseMap.clear();
		assessmentMap.clear();
		tutorMap.clear();
		facilityMap.clear();
		sessionDateMap.clear();
		//sessionIdMap.clear();

		loadTutorDatas();
		loadFacilityDatas();
		
		drawTimeline(DateUtil.today());
	}

	private void drawTimeline(Date date) {
		//date range: current week and next week
		Date startDate = DateUtil.weekStartOnSunday(date);
		Date endDate = DateUtil.nextweek(startDate, 2);
		UniLog.log1("startDate:%s, endDate:%s", sdf.format(startDate), sdf.format(endDate));
		if (currentDate == null || startDate.compareTo(activeStartDate) < 0 || startDate.compareTo(activeEndDate) >= 0 
								|| endDate.compareTo(activeStartDate) <= 0 || endDate.compareTo(activeEndDate) > 0) {
			//active date range: 6 week
			Date aStartDate = DateUtil.prevweek(startDate, 2);
			Date aEndDate = DateUtil.nextweek(endDate, 2);
			UniLog.log1("aStartDate:%s, aEndDate:%s", sdf.format(aStartDate), sdf.format(aEndDate));
			if (currentDate != null) {
				Set<Date> l = new TreeSet<Date>();
				l.add(aStartDate);
				l.add(aEndDate);
				l.add(activeStartDate);
				l.add(activeEndDate);
				if (l.size() > 2) {
					Date[] ds = l.toArray(new Date[0]);
					for (int i = 0; i < ds.length - 1; i++) {
						Date sd = ds[i];
						Date ed = ds[i + 1];
						if (sd.compareTo(aStartDate) == 0 && ed.compareTo(aEndDate) == 0)
							loadSessionDatas(sd, ed);
						else if (sd.compareTo(activeStartDate) == 0 && ed.compareTo(activeEndDate) == 0)
							removeSessionDatas(sd, ed);
						else if (sd.compareTo(aStartDate) == 0 && ed.compareTo(activeStartDate) == 0)
							loadSessionDatas(sd, ed);
						else if (sd.compareTo(aEndDate) == 0 && ed.compareTo(activeEndDate) == 0)
							removeSessionDatas(sd, ed);
						else if (sd.compareTo(activeStartDate) == 0 && ed.compareTo(aStartDate) == 0)
							removeSessionDatas(sd, ed);
						else if (sd.compareTo(activeEndDate) == 0 && ed.compareTo(aEndDate) == 0)
							loadSessionDatas(sd, ed);
					}
				}
			}
			else
				loadSessionDatas(aStartDate, aEndDate);
			activeStartDate = aStartDate;
			activeEndDate = aEndDate;
		}
		currentDate = date;
		if (timelineMode == TimelineMode.WEEK) {
			lblTo.setVisible(true);
			dbTo.setVisible(true);
			dbFrom.setValue(currentDate);
			dbTo.setValue(DateUtil.prevday(DateUtil.nextweek(currentDate)));
		}
		else {
			lblTo.setVisible(false);
			dbTo.setVisible(false);
			dbFrom.setValue(currentDate);
			dbTo.setValue(currentDate);
		}
		drawTimeline();
	}
	
	private void drawTimeline() {
		try {
			Date lastTimelineMinDate = timelineMinDate;
			Date lastTimelineMaxDate = timelineMaxDate;
			//Timeline min date, max date, start date, end date
			Date startDate, endDate;
			if (timelineMode == TimelineMode.WEEK) {
				timelineMinDate = dbFrom.getValue();
				timelineMaxDate = DateUtil.nextday(dbTo.getValue());
				if (timelineMinDate.compareTo(activeStartDate) < 0)
					timelineMinDate = activeStartDate;
				if (timelineMaxDate.compareTo(activeEndDate) > 0)
					timelineMaxDate = activeEndDate;
				startDate = timelineMinDate;
				endDate = timelineMaxDate;
			}
			else {
				timelineMinDate = currentDate;
				timelineMaxDate = DateUtil.nextday(currentDate);
				//get min session date
				Date minSessDate = null;
				Set<Integer> sList = sessionDateMap.get(currentDate);
				if (sList != null) {
					for (int snrg : sList) {
						JSONObject jobj = sessionMap.get(snrg);
						Date d = df.parse(jobj.getString("start"));
						if (minSessDate == null || d.compareTo(minSessDate) < 0)
							minSessDate = d;
					}
				}
				//show 6 hours, offset to min session date
				if (minSessDate != null) {
					if (timelineMaxDate.getTime() - minSessDate.getTime() >= TIMELINE_SHOW_HOUR_FOR_DAYMODE * 3600 * 1000)
						startDate = minSessDate;
					else
						startDate = DateUtils.addHours(timelineMaxDate, -TIMELINE_SHOW_HOUR_FOR_DAYMODE);
				}
				else
					startDate = timelineMinDate;
				endDate = DateUtils.addHours(startDate, TIMELINE_SHOW_HOUR_FOR_DAYMODE);
				if (endDate.compareTo(timelineMaxDate) > 0)
					endDate = timelineMaxDate;
			}
			String minDateStr = df.format(timelineMinDate);
			String maxDateStr = df.format(timelineMaxDate);
			String startDateStr = df.format(startDate);
			String endDateStr = df.format(endDate);

			//get visible group list
			Set<Integer> visibleGroupIdList = new HashSet<Integer>();
			if (timelineType == TimelineType.FACILITY) {
				for (int fcrg : facilityMap.keySet()) {
					Map<String, Object> fm = facilityMap.get(fcrg);
					int id = (Integer)fm.get("id");
					String fcStatus = (String)fm.get("esfc_status");
					if (fcrg == 0 || StringUtils.equals(fcStatus, "Enable"))
						visibleGroupIdList.add(id);
				}
			}
			else {
				for (int ttrg : tutorMap.keySet()) {
					Map<String, Object> tm = tutorMap.get(ttrg);
					int id = (Integer)tm.get("id");
					String ttStatus = (String)tm.get("estt_status");
					if (ttrg == 0 || StringUtils.equals(ttStatus, "Normal"))
						visibleGroupIdList.add(id);
				}
			}
			/*for (Date d = timelineMinDate; d.compareTo(timelineMaxDate) < 0; d = DateUtil.nextday(d)) {
				Set<Integer> sList = sessionDateMap.get(d);
				if (sList != null) {
					for (int snrg : sList) {
						JSONObject jobj = sessionMap.get(snrg);
						if (timelineType == TimelineType.FACILITY) {
							int fcrg = jobj.getInt("tFcRg");
							Map<String, Object> fm = facilityMap.get(fcrg);
							int id = (Integer)fm.get("id");
							String fcStatus = (String)fm.get("esfc_status");
							if (fcrg == 0 || StringUtils.equals(fcStatus, "Enable"))
								visibleGroupIdList.add(id);
						}
						else {
							int ttrg = jobj.getInt("tTtRg");
							Map<String, Object> tm = tutorMap.get(ttrg);
							int id = (Integer)tm.get("id");
							String ttStatus = (String)tm.get("estt_status");
							if (ttrg == 0 || StringUtils.equals(ttStatus, "Normal"))
								visibleGroupIdList.add(id);
						}
					}
				}
			}*/
			UniLog.log1("timelineMode:%s, currentDate:%s, min:%s, max:%s, start:%s, end:%s, activeStartDate:%s, activeEndDate:%s", 
						timelineMode, currentDate, timelineMinDate, timelineMaxDate, startDate, endDate, activeStartDate, activeEndDate);

			//setup timeline
			if (lastTimelineMinDate == null || timelineMinDate.compareTo(lastTimelineMinDate) != 0 || timelineMaxDate.compareTo(lastTimelineMaxDate) != 0) {
				//Map<Integer, Set<Integer>> tCourseMap = new LinkedHashMap<Integer, Set<Integer>>();
				Map<Integer, Set<Integer>> tTutorMap = new LinkedHashMap<Integer, Set<Integer>>(); //key: ttrg, value: session List
				Map<Integer, Set<Integer>> tFacilityMap = new LinkedHashMap<Integer, Set<Integer>>(); //key: fcrg, value: session List
				for (Date d = timelineMinDate; d.compareTo(timelineMaxDate) < 0; d = DateUtil.nextday(d)) {
					Set<Integer> sList = sessionDateMap.get(d);
					if (sList != null) {
						for (int snrg : sList) {
							JSONObject jobj = sessionMap.get(snrg);
							/*int avrg = jobj.getInt("tAvRg");
							int type = jobj.getInt("tType");
							switch (type) {
							case 0:
								Set<Integer> list = tCourseMap.get(avrg);
								if (list == null) {
									list = new LinkedHashSet<Integer>();
									tCourseMap.put(avrg, list);
								}
								list.add(snrg);
								break;
							case 1:
								break;
							}*/
							int ttrg = jobj.getInt("tTtRg");
							int fcrg = jobj.getInt("tFcRg");
		
							Set<Integer> list = tTutorMap.get(ttrg);
							if (list == null) {
								list = new LinkedHashSet<Integer>();
								tTutorMap.put(ttrg, list);
							}
							list.add(snrg);
		
							list = tFacilityMap.get(fcrg);
							if (list == null) {
								list = new LinkedHashSet<Integer>();
								tFacilityMap.put(fcrg, list);
							}
							list.add(snrg);
						}
					}
				}
				
			    JSONArray jaGroups = new JSONArray();
			    JSONArray jaItems = new JSONArray();

				//tutor
				for (int ttrg : tutorMap.keySet()) {
					Map<String, Object> m = tutorMap.get(ttrg);
					int groupId = (Integer)m.get("id");
					String content = joinString(", ", m.get("estt_ttno"), m.get("estt_name"));
					if (StringUtils.isBlank(content))
						content = "No tutor assigned";
					String ttStatus = (String)m.get("estt_status");
					if (ttrg == 0 || StringUtils.equals(ttStatus, "Normal")) {
						jaGroups.put(new JSONObject()
								.put("id", groupId)
								.put("type", "tutor")
			    				.put("title", content)
			    				.put("content", content)
			    				.put("code", m.get("estt_ttno"))
			    				.put("visible", visibleGroupIdList.contains(groupId)));
					}
				}
				for (Map.Entry<Integer, Set<Integer>> entry : tTutorMap.entrySet()) {
					int ttrg = entry.getKey();
					Set<Integer> snList = entry.getValue();
					Map<String, Object> m = tutorMap.get(ttrg);
					String ttStatus = (String)m.get("estt_status");
					if (ttrg > 0 && !StringUtils.equals(ttStatus, "Normal"))
						continue;
					int groupId = (Integer)m.get("id");
					/*String content = joinString(", ", m.get("estt_ttno"), m.get("estt_name"));
					if (StringUtils.isBlank(content))
						content = "No tutor assigned";
					jaGroups.put(new JSONObject()
								.put("id", groupId)
								.put("type", "tutor")
			    				.put("title", content)
			    				.put("content", content)
			    				.put("code", m.get("estt_ttno"))
			    				.put("visible", visibleGroupIdList.contains(groupId)));*/
					for (int snrg : snList) {
						JSONObject jobj = sessionMap.get(snrg);
						jaItems.put(new JSONObject()
									.put("id", jobj.getInt("idForTutor"))
									.put("group", groupId)
									.put("title", jobj.getString("title"))
									.put("content", jobj.getString("title"))
									.put("start", jobj.getString("start"))
									.put("end", jobj.getString("end")));
					}
				}

				//facility
				for (int fcrg : facilityMap.keySet()) {
					Map<String, Object> m = facilityMap.get(fcrg);
					int groupId = (Integer)m.get("id");
					String content = joinString(", ", m.get("esfc_code"), m.get("esfc_name"));
					if (StringUtils.isBlank(content))
						content = "No facility assigned";
					String fcStatus = (String)m.get("esfc_status");
					if (fcrg == 0 || StringUtils.equals(fcStatus, "Enable")) {
						jaGroups.put(new JSONObject()
								.put("id", groupId)
								.put("type", "facility")
			    				.put("title", content)
			    				.put("content", content)
			    				.put("code", m.get("esfc_code"))
			    				.put("visible", visibleGroupIdList.contains(groupId)));
					}
				}
				for (Map.Entry<Integer, Set<Integer>> entry : tFacilityMap.entrySet()) {
					int fcrg = entry.getKey();
					Set<Integer> snList = entry.getValue();
					Map<String, Object> m = facilityMap.get(fcrg);
					String fcStatus = (String)m.get("esfc_status");
					if (fcrg > 0 && !StringUtils.equals(fcStatus, "Enable"))
						continue;
					int groupId = (Integer)m.get("id");
					/*String content = joinString(", ", m.get("esfc_code"), m.get("esfc_name"));
					if (StringUtils.isBlank(content))
						content = "No facility assigned";
					jaGroups.put(new JSONObject()
								.put("id", groupId)
								.put("type", "facility")
			    				.put("title", content)
			    				.put("content", content)
			    				.put("code", m.get("esfc_code"))
			    				.put("visible", visibleGroupIdList.contains(groupId)));*/
					for (int snrg : snList) {
						JSONObject jobj = sessionMap.get(snrg);
						jaItems.put(new JSONObject()
									.put("id", jobj.getInt("idForFacility"))
									.put("group", groupId)
									.put("title", jobj.getString("title"))
									.put("content", jobj.getString("title"))
									.put("start", jobj.getString("start"))
									.put("end", jobj.getString("end")));
					}
				}
	
			    JSONObject joOptions = new JSONObject();
			    joOptions.put("min", minDateStr);
			    joOptions.put("max", maxDateStr);
			    joOptions.put("start", startDateStr);
			    joOptions.put("end", endDateStr);
			    joOptions.put("zoomMin", 15 * 60 * 1000); //15 minutes
			    joOptions.put("groupOrder", "function (a, b) { return a.code.localeCompare(b.code); }");
			    joOptions.put("orientation", new JSONObject() {{ this.put("axis", "both"); }});


				UniLog.log1("activeStartDate:%s, activeEndDate:%s, itemCount:%d", activeStartDate, activeEndDate, jaItems.length());
	
			    ZkUtil.js("eduResourceAllocationTimeline.setup('$%s','$%s #sliderZoom',%s,%s,%s)",
			    		visDiv.getId(), winResourceAllocationTimeline.getId(), joOptions, jaGroups, jaItems);
			}
			else {
				ZkUtil.js("eduResourceAllocationTimeline.showGroups('$%s',%s);"
						+ "eduResourceAllocationTimeline.setRange('$%s','%s','%s','%s','%s')",
						visDiv.getId(), GsonUtil.objToStr(visibleGroupIdList), 
						visDiv.getId(), minDateStr, maxDateStr, startDateStr, endDateStr);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void removeSessionDatas(Date startDate, Date endDate) {
		UniLog.log1("removeSessionDatas %s, %s", sdf.format(startDate), sdf.format(endDate));
		Date[] dateList = sessionDateMap.keySet().toArray(new Date[0]);
		for (Date date : dateList) {
			if (date.compareTo(startDate) >= 0 && date.compareTo(endDate) < 0) {
				Set<Integer> snList = sessionDateMap.get(date);
				for (int snRg : snList)
					sessionMap.remove(snRg);
				sessionDateMap.remove(date);
			}
		}
	}

	private void loadSessionDatas(Date startDate, Date endDate) {
		UniLog.log1("loadSessionDatas %s, %s", sdf.format(startDate), sdf.format(endDate));
		BiResult brCourseSession = null;
		BiResult brCourse = null;
		BiResult brCourseStudent = null;
		BiResult brAssessment = null;
		try {
			brCourseSession = sessionHelper.newBiResult("edu.CourseSessionDet");
			brCourse = sessionHelper.newBiResult("edu.Course");
			brCourseStudent = sessionHelper.newBiResult("edu.CourseStudent");
			brAssessment = sessionHelper.newBiResult("edu.Assessment");

			brCourseSession.clearCondition();
			brCourseSession.addCustomCondition(String.format("essncs_date >= '%s' and essncs_date < '%s'", sdf.format(startDate), sdf.format(endDate)));
			//if (StringUtils.isNotBlank(getCurrentTutorCode()))
			//	brCourseSession.addCustomCondition(String.format("estt_ttno = '%s'", getCurrentTutorCode()));
			ReturnMsg rtn;
			if ((rtn = brCourseSession.query(true, false)).getStatus()) {
				while (brCourseSession.next())
					loadSessionDatasOne(startDate, endDate, brCourseSession, null, brCourse, brCourseStudent);
			}
			else
				throw new Exception(rtn.getMsg());

			brAssessment.clearCondition();
			brAssessment.addCustomCondition(String.format("essnas_date >= '%s' and essnas_date < '%s'", sdf.format(startDate), sdf.format(endDate)));
			//if (StringUtils.isNotBlank(getCurrentStudentCode()))
			//	brAssessment.addCustomCondition(String.format("essd_sdno = '%s'", getCurrentStudentCode()));
			//if (StringUtils.isNotBlank(getCurrentTutorCode()))
			//	brAssessment.addCustomCondition(String.format("estt_ttno = '%s'", getCurrentTutorCode()));
			if ((rtn = brAssessment.query(true, false)).getStatus()) {
				while (brAssessment.next())
					loadSessionDatasOne(startDate, endDate, null, brAssessment, null, null);
			}
			else
				throw new Exception(rtn.getMsg());
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
			if (brAssessment != null)
				brAssessment.close();
		}
	}

	private void loadSessionDatasOne(Date startDate, Date endDate, BiResult brCourseSession, BiResult brAssessment, BiResult brCourse, BiResult brCourseStudent) throws Exception {
		int avrg;
		int rg;
		int type;
		Date date;
		Date startTime;
		Date endTime;
		String remark;
		int ttrg;
		int fcrg;
		String ttNo;
		String ttName;
		String ttStatus;
		String fcCode;
		String fcName;
		String fcStatus;
		String courseType = null;
		if (brAssessment != null) {
			avrg = 0;
			rg = brAssessment.getCellInt("essnas_rg");
			type = brAssessment.getCellInt("essnas_type");
			date = brAssessment.getCellDate("essnas_date");
			startTime = brAssessment.getCellDate("essnas_sttime");
			endTime = brAssessment.getCellDate("essnas_endtime");
			remark = brAssessment.getCellString("essnas_name");
			ttrg = brAssessment.getCellInt("esattt_atrg");
			fcrg = brAssessment.getCellInt("esatfc_atrg");
			ttNo = brAssessment.getCellString("estt_ttno");
			ttName = brAssessment.getCellString("estt_name");
			ttStatus = brAssessment.getCellString("estt_status");
			fcCode = brAssessment.getCellString("esfc_code");
			fcName = brAssessment.getCellString("esfc_name");
			fcStatus = brAssessment.getCellString("esfc_status");
		}
		else {
			avrg = brCourseSession.getCellInt("essncs_avrg");
			rg = brCourseSession.getCellInt("essncs_rg");
			type = brCourseSession.getCellInt("essncs_type");
			date = brCourseSession.getCellDate("essncs_date");
			startTime = brCourseSession.getCellDate("essncs_sttime");
			endTime = brCourseSession.getCellDate("essncs_endtime");
			remark = brCourseSession.getCellString("essncs_name");
			ttrg = brCourseSession.getCellInt("esattt_atrg");
			fcrg = brCourseSession.getCellInt("esatfc_atrg");
			ttNo = brCourseSession.getCellString("estt_ttno");
			ttName = brCourseSession.getCellString("estt_name");
			ttStatus = brCourseSession.getCellString("estt_status");
			fcCode = brCourseSession.getCellString("esfc_code");
			fcName = brCourseSession.getCellString("esfc_name");
			fcStatus = brCourseSession.getCellString("esfc_status");
		}
		//String bgColor = "Wheat";
		//String textColor = "Black";
		String title = remark;
		
		switch (type) {
		case 0: //Course
			//bgColor = getColorString(getCourseBgColor(avrg));
			//textColor = getContrastColorString(getCourseBgColor(avrg));

			if (excludeCourseList.contains(avrg))
				return;
			Map<String, Object> m = courseMap.get(avrg);
			if (m == null) {
				Date sdStartDate = null, sdEndDate = null;
				ReturnMsg rtn;
				/*if (StringUtils.isNotBlank(getCurrentStudentCode())) {
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
				}*/

				brCourse.clearCondition();
				brCourse.addCustomCondition(String.format("eaav0_rg = %d", avrg));
				if ((rtn = brCourse.query(true, false)).getStatus()) {
					if (brCourse.next()) {
						UniLog.log1("add course");
						//add course
						int cttrg = brCourse.getCellInt("eaav0_esttrg");
						int cfcrg = brCourse.getCellInt("eaav0_esfcrg");
						courseType = StringUtils.defaultString(brCourse.getCellString("eaav0_coursetype"), "").trim();
						m = new HashMap<String, Object>();
						m.put("eaav0_rg", brCourse.getCellInt("eaav0_rg"));
						m.put("eaav0_code", brCourse.getCellString("eaav0_code"));
						//m.put("eaav0_name", fShowCName ? brCourse.getCellString("eaav0_name") : "");
						m.put("eaav0_name", brCourse.getCellString("eaav0_name"));
						m.put("eaav0_coursetype", courseType);
						m.put("eaav0_esttrg", cttrg);
						m.put("estt_ttno", brCourse.getCellString("estt_ttno"));
						m.put("estt_name", brCourse.getCellString("estt_name"));
						m.put("eaav0_esfcrg", cfcrg);
						m.put("esfc_code", brCourse.getCellString("esfc_code"));
						m.put("esfc_name", brCourse.getCellString("esfc_name"));
						m.put("sdStartDate", sdStartDate);
						m.put("sdEndDate", sdEndDate);
						//m.put("bgColor", bgColor);
						//m.put("textColor", textColor);
						courseMap.put(avrg, m);
						title = joinString(", ", (String)m.get("eaav0_code"), (String)m.get("eaav0_name"), ttName, fcName);
					}
					else {
						excludeCourseList.add(avrg);
						return;
					}
				}
				else
					throw new Exception(rtn.getMsg());
			}
			else {
				courseType = (String)m.get("eaav0_coursetype");
				title = joinString(", ", (String)m.get("eaav0_code"), (String)m.get("eaav0_name"), ttName, fcName);
			}

			/*//only include subscribe session for student
			if (StringUtils.isNotBlank(getCurrentStudentCode())) {
				Date sdStartDate = (Date)m.get("sdStartDate");
				Date sdEndDate = (Date)m.get("sdEndDate");
				if (DateUtil.isDateNull(sdStartDate) || DateUtil.isDateNull(sdEndDate) || date.compareTo(sdStartDate) < 0 || date.compareTo(sdEndDate) > 0)
					return;
			}*/
			break;
		case 1: //Assessment
			//bgColor = getColorString(assessmentBgColor);
			//textColor = getContrastColorString(assessmentBgColor);
			title = joinString(", ", remark, ttName, fcName);
			Map<String, Object> m0 = new HashMap<String, Object>();
			assessmentMap.put(rg, m0);
			break;
		}

		Map<String, Object> m;
		if (!tutorMap.containsKey(ttrg)) {
			m = new HashMap<String, Object>();
			m.put("id", maxGroupId++);
			m.put("eaav0_esttrg", ttrg);
			m.put("estt_ttno", ttNo);
			m.put("estt_name", ttName);
			m.put("estt_status", ttStatus);
			tutorMap.put(ttrg, m);
		}
		if (!facilityMap.containsKey(fcrg)) {
			m = new HashMap<String, Object>();
			m.put("id", maxGroupId++);
			m.put("eaav0_esfcrg", fcrg);
			m.put("esfc_code", fcCode);
			m.put("esfc_name", fcName);
			m.put("esfc_status", fcStatus);
			facilityMap.put(fcrg, m);
		}

		if (!sessionMap.containsKey(rg)) {
			int id = maxEventId++;
			JSONObject jsonEvent = new JSONObject();
			jsonEvent.put("id", id);
			jsonEvent.put("idForTutor", maxItemId++);
			jsonEvent.put("idForFacility", maxItemId++);
			jsonEvent.put("owner", sessionHelper.getLoginId());
			jsonEvent.put("title", title);
			jsonEvent.put("start", df.format(unionDateTime(date, startTime)));
			jsonEvent.put("end", df.format(unionDateTime(date, endTime)));
			//jsonEvent.put("textColor", textColor);
			//jsonEvent.put("color", bgColor);
			//if (StringUtils.isBlank(getCurrentStudentCode())) //Student should not allow to click calendar event
			//	jsonEvent.put("cursor", "pointer");
			jsonEvent.put("tAvRg", avrg);
			jsonEvent.put("tType", type);
			jsonEvent.put("tTtRg", ttrg);
			jsonEvent.put("tTtNo", ttNo);
			jsonEvent.put("tTtName", ttName);
			jsonEvent.put("tTtStatus", ttStatus);
			jsonEvent.put("tFcRg", fcrg);
			jsonEvent.put("tFcCode", fcCode);
			jsonEvent.put("tFcName", fcName);
			jsonEvent.put("tFcStatus", fcStatus);
			jsonEvent.put("tCourseType", courseType);
			jsonEvent.put("tVisibled", false);
			sessionMap.put(rg, jsonEvent);
			Set<Integer> list = sessionDateMap.get(date);
			if (list == null) {
				list = new HashSet<Integer>();
				sessionDateMap.put(date, list);
			}
			list.add(rg);
			//if (!sessionIdMap.containsKey(id))
			//	sessionIdMap.put(id, rg);
		}
	}

	private void loadTutorDatas() {
		BiResult biResult = null;
		try {
			biResult = BiResultHelper.create(sessionHelper, "edu.Tutor", null, -1, null);
			while (biResult.next()) {
				int ttrg = biResult.getCellInt("estt_rg");
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("id", maxGroupId++);
				m.put("eaav0_esttrg", ttrg);
				m.put("estt_ttno", biResult.getCellString("estt_ttno"));
				m.put("estt_name", biResult.getCellString("estt_name"));
				m.put("estt_status", biResult.getCellString("estt_status"));
				tutorMap.put(ttrg, m);
			}	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (biResult != null)
				biResult.close();
		}
	}

	private void loadFacilityDatas() {
		BiResult biResult = null;
		try {
			biResult = BiResultHelper.create(sessionHelper, "edu.facility", null, -1, null);
			while (biResult.next()) {
				int fcrg = biResult.getCellInt("esfc_rg");
				Map<String, Object> m = new HashMap<String, Object>();
				m.put("id", maxGroupId++);
				m.put("eaav0_esfcrg", fcrg);
				m.put("esfc_code", biResult.getCellString("esfc_code"));
				m.put("esfc_name", biResult.getCellString("esfc_name"));
				m.put("esfc_status", biResult.getCellString("esfc_status"));
				facilityMap.put(fcrg, m);
			}	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (biResult != null)
				biResult.close();
		}
	}
	
	/*private Map<String, Object> getLoadOrRemoveSessionDateRange() {
		final int maxMonthCount = 5;
		List<Pair<Date, Date>> addList = new ArrayList<Pair<Date, Date>>();
		Set<Date> removeList = new TreeSet<Date>();
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("addList", addList);
		resultMap.put("removeList", removeList);

		Date startDate = DateUtil.prevMonthStart(currentDate);
		Date endDate = DateUtil.nextMonthStart(DateUtil.nextMonthStart(currentDate));
		resultMap.put("dateRange", Pair.of(startDate, endDate));
		resultMap.put("activeDateRange", Pair.of(DateUtil.weekStartOnSunday(startDate), DateUtil.nextday(DateUtil.weekEndWithSaturday(endDate))));

		Set<Date> dList = new TreeSet<Date>();
		for (Date d = startDate; d.compareTo(endDate) < 0; d = DateUtil.nextMonthStart(d)) {
			if (!loadedSessionMonthStartDateList.contains(d)) {
				dList.add(d);
				loadedSessionMonthStartDateList.add(d);
			}
		}
		for (Date d : dList) {
			int c = addList.size() - 1;
			if (c >= 0 && addList.get(c).getRight().compareTo(d) == 0) {
				Pair<Date, Date> p = addList.get(c);
				addList.set(c, Pair.of(p.getLeft(), DateUtil.nextMonthStart(d)));
			}
			else
				addList.add(Pair.of(d, DateUtil.nextMonthStart(d)));
		}
		int count = loadedSessionMonthStartDateList.size();
		if (count > maxMonthCount) {
			Iterator<Date> it = loadedSessionMonthStartDateList.iterator();
			while (count > maxMonthCount && it.hasNext()) {
				Date d = it.next();
				if (d.compareTo(startDate) < 0 || d.compareTo(endDate) >= 0) {
					removeList.add(d);
					it.remove();
					count--;
				}
			}
		}
		UniLog.log1("currentDate:%s, map:%s, loadedSessionMonthStartDateList size:%d", currentDate, resultMap, loadedSessionMonthStartDateList.size());
		return resultMap;
	}*/
}
