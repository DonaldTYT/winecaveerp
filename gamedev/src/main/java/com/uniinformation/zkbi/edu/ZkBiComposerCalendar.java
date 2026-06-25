package com.uniinformation.zkbi.edu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Window;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.BiView;
import com.uniinformation.jxapp.edu.Student;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.GridHelper;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiHelpDialog;

import static com.uniinformation.jxapp.edu.Student.joinString;

public class ZkBiComposerCalendar extends ZkComposerBase {
	private final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	private final SimpleDateFormat ddf = new SimpleDateFormat("yyyy-MM-dd");
	private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	
	private final Map<Date, JSONObject> holidayMap = new HashMap<Date, JSONObject>();
	private final Map<Integer, JSONObject> sessionMap = new HashMap<Integer, JSONObject>(); //key: sessionRg
	private final Map<Integer, Map<String, Object>> courseMap = new HashMap<Integer, Map<String, Object>>(); //key: courseRg
	private final Map<Integer, Map<String, Object>> assessmentMap = new HashMap<Integer, Map<String, Object>>(); //key: sessionRg
	private final Map<Integer, Map<String, Object>> tutorMap = new HashMap<Integer, Map<String, Object>>(); //key: tutorRg
	private final Map<Integer, Map<String, Object>> facilityMap = new HashMap<Integer, Map<String, Object>>(); //key: facilityRg
	private final Map<String, Map<String, Object>> coursetypeMap = new HashMap<String, Map<String, Object>>(); //key: course type

	private final Set<Integer> excludeCourseList = new HashSet<Integer>();
	private final Map<Date, Set<Integer>> sessionDateMap = new HashMap<Date, Set<Integer>>(); //key: date, value: sessionMap key List
	private final Map<Integer, Integer> sessionIdMap = new HashMap<Integer, Integer>(); //key: id, value: session rg
	private final Map<Date, Boolean> showDateConflictIconMap = new HashMap<Date, Boolean>();

	//private final String[] courseBgColorList = new String[] {"Orange", "Pink", "Orchid", "PaleGreen", "PaleTurquoise", "PaleVioletRed", "PowderBlue", "Purple", "RoyalBlue", "SeaGreen"};
	//private final String[] courseTextColorList = new String[] {"Black", "Black", "Black", "Black", "Black", "White", "Black", "White", "White", "White"};
	private final int[] courseBgColorList = new int[] {
		//Red     Orange    Yellow    Green     Cyan      Blue      Purple    Brown     Gray      Pink
				  0xFFA500, 0xFFFF00, 0x008000, 0x00FFFF, 0x0000FF, 0x800080, 0xA52A2A, 0x808080, 0xFFC0CB,
		0x8B0000, 0xFF4500,	0xBDB76B, 0x006400, 0x008080, 0x000080, 0x4B0082, 0x800000, 0x2F4F4F, 0xC71585,
		0xB22222, 0xFF6347, 0xFFD700, 0x556B2F, 0x008B8B, 0x00008B, 0x8B008B, 0x8B4513, 0x696969, 0xFF1493,
		0xDC143C, 0xFF8C00, 0xF0E68C, 0x228B22, 0x20B2AA, 0x0000CD, 0x9400D3, 0xA0522D, 0x708090, 0xDB7093,
		0xCD5C5C, 0xFF7F50, 0xFFDAB9, 0x2E8B57, 0x5F9EA0, 0x191970, 0x483D8B, 0xD2691E, 0x778899, 0xFF69B4,
		0xF08080,           0xEEE8AA, 0x808000, 0x00CED1, 0x4169E1, 0x8A2BE2, 0xB8860B, 0xA9A9A9, 0xFFB6C1,
		0xFA8072,           0xFFE4B5, 0x6B8E23, 0x48D1CC, 0x4682B4, 0x9932CC, 0xCD853F, 0xC0C0C0,
		0xE9967A,           0xFFEFD5, 0x3CB371, 0x40E0D0, 0x1E90FF, 0xFF00FF, 0xBC8F8F, 0xD3D3D3,
		0xFFA07A,           0xFAFAD2, 0x32CD32, 0x7FFFD4, 0x00BFFF, 0x6A5ACD, 0xDAA520, 0xDCDCDC,
						    0xFFFACD, 0x00FF00, 0xAFEEEE, 0x6495ED, 0x7B68EE, 0xF4A460,
						    0xFFFFE0, 0x00FF7F, 0xE0FFFF, 0x87CEEB, 0xBA55D3, 
						    		  0x00FA9A,           0x87CEFA, 0x9370DB, 0xDEB887,
						    		  0x8FBC8F,           0xB0C4DE, 0xDA70D6, 0xF5DEB3,
						    		  0x66CDAA,           0xADD8E6, 0xEE82EE, 0xFFDEAD,
						    		  0x9ACD32,           0xB0E0E6, 0xDDA0DD, 0xFFE4C4,
						    		  0x7CFC00,                     0xD8BFD8, 0xFFEBCD,
						    		  0x7FFF00,                     0xE6E6FA, 0xFFF8DC,
						    		  0x90EE90,
						    		  0xADFF2F,
						    		  0x98FB98,
	};
	private final Map<Integer, Integer> courseBgColorMap = new HashMap<Integer, Integer>();
	private final int holidayBgColor = 0xFF0000;//"Red";
	private final int assessmentBgColor = 0xD2B48C;//"Tan";

	private boolean holidayEventVisibled = true;
	private int maxEventId = 1;

	private Date activeStart;
	private Date activeEnd;

	private final List<Map<String, Object>> eventRowList = new ArrayList<Map<String, Object>>();
	private final List<Map<String, Object>> assessmentRowList = new ArrayList<Map<String, Object>>();
	private final List<Map<String, Object>> courseRowList = new ArrayList<Map<String, Object>>();
	private final List<Map<String, Object>> tutorRowList = new ArrayList<Map<String, Object>>();
	private final List<Map<String, Object>> facilityRowList = new ArrayList<Map<String, Object>>();
	private final List<Map<String, Object>> coursetypeRowList = new ArrayList<Map<String, Object>>();
	
	private String mCurStudentCode, mCurTutorCode;
	private final static boolean fShowCName = false;  //andrew220304 add flag for hide course name
	
	@Wire
	Div divCalendar;
	@Wire
	Window winCalendar, winEventList, winTutorList, winFacilityList, winCoursetypeList;
	@Wire
	Checkbox cbEventListAll, cbTutorListAll, cbFacilityListAll, cbCoursetypeListAll;
	@Wire
	Toolbarbutton btnHelp;

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		//Selectors.wireComponents(p_comp, this, false);  //important for wire variable
		super.doAfterCompose(p_comp);
		if (!accessOkFlag) {
			return;
		}
		UniLog.log1("called ZkBiComposerCalendar:%s, accessRights:%s", p_comp, sessionHelper.getAccessRights());
   		String queryCondition = getURLParam("querycondition");
		if (queryCondition != null) {
			JSONObject jo = (JSONObject) sessionHelper.getOneTimeData(queryCondition);
			UniLog.log1("queryCondition json:%s", jo);
	  		if (jo == null) 
	  			return;
	  		mCurStudentCode = jo.optString("studentCode");
	  		mCurTutorCode = jo.optString("tutorCode");
			UniLog.log1("mCurStudentCode:%s, mCurTutorCode:%s", mCurStudentCode, mCurTutorCode);
		}

		cbEventListAll = (Checkbox) winEventList.query("#cbEventListAll");
		cbTutorListAll = (Checkbox) winTutorList.query("#cbTutorListAll");
		cbFacilityListAll = (Checkbox) winFacilityList.query("#cbFacilityListAll");
		cbCoursetypeListAll = (Checkbox) winCoursetypeList.query("#cbCoursetypeListAll");

		btnHelp = (Toolbarbutton) winCalendar.query("#btnHelp");

		String titleUser = null;
		String tutorCode1 = getCurrentTutorCode1();
		if (StringUtils.isNotBlank(getCurrentStudentCode()))
			titleUser = String.format("%s (%s)", getCurrentStudentName(), getCurrentStudentCode());
		else if (StringUtils.isNotBlank(tutorCode1))
			titleUser = String.format("%s (%s)", getCurrentTutorName(tutorCode1), tutorCode1);
		else if (sessionHelper.isAdminUser() || sessionHelper.hasAccessRight("#edu"))
			titleUser = "Manager";
		else if (sessionHelper.hasAccessRight("#eduadmin"))
			titleUser = "Admin";
		winCalendar.setTitle(Student.joinString(" of ", "Calendar", titleUser));

		winCalendar.addEventListener("onCalEvent", new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				if (event.getData() == null)
					return;
				try {
					JSONObject json = new JSONObject(event.getData().toString()); 
					String action = json.getString("action");
					if (StringUtils.equals(action, "addMonthsEvent")) {
						final JSONArray requestAddMonthStarts = json.optJSONArray("requestAddMonthStarts");
						final JSONArray requestRemoveMonthStarts = json.optJSONArray("requestRemoveMonthStarts");
						activeStart = ddf.parse(json.getString("activeStart"));
						activeEnd = ddf.parse(json.getString("activeEnd"));
						final Date aStart;
						if (activeStart.compareTo(DateUtil.monthStart(activeStart)) == 0)
							aStart = DateUtil.prevMonthStart(activeStart);
						else
							aStart = DateUtil.monthStart(activeStart);
						final Date aEnd = DateUtil.nextMonthStart(activeEnd);
						UniLog.log1("aStart:%s, aEnd:%s, activeStart:%s, activeEnd:%s", aStart, aEnd, activeStart, activeEnd);
						if (requestAddMonthStarts != null) {
							Map<Date, Date> dMap = new TreeMap<Date, Date>();
							for (int i = 0; i < requestAddMonthStarts.length(); i++) {
								String dStr = requestAddMonthStarts.getString(i);
								Date startDate = ddf.parse(dStr);
								Date endDate = DateUtil.nextMonthStart(startDate);
								dMap.put(startDate, endDate);
							}
							//join map to list
							List<Pair<Date, Date>> dList = new ArrayList<Pair<Date, Date>>();
							for (Map.Entry<Date, Date> dEntry : dMap.entrySet()) {
								Date dKey = dEntry.getKey();
								Date dValue = dEntry.getValue();
								int c = dList.size() - 1;
								if (c >= 0 && dList.get(c).getRight().compareTo(dKey) == 0) {
									Pair<Date, Date> p = dList.get(c);
									dList.set(c, Pair.of(p.getLeft(), dValue));
								}
								else
									dList.add(Pair.of(dKey, dValue));
							}
							if (sessionHelper.isAdminUser() 
									|| sessionHelper.hasAccessRight("#edu") 
									|| sessionHelper.hasAccessRight("#eduadmin") 
									|| sessionHelper.hasAccessRight("#student") 
									|| sessionHelper.hasAccessRight("#tutor")) {
								for (Pair<Date, Date> p : dList)
									loadSessionDatas(p.getKey(), p.getValue());
							}
						}
						clearJsEventList();
						if (requestRemoveMonthStarts != null) {
							UniLog.log1("start remove session sessionMap:%d, sessionDateMap:%d, sessionIdMap:%d", sessionMap.size(), sessionDateMap.size(), sessionIdMap.size());
							Date[] dateList = sessionDateMap.keySet().toArray(new Date[0]);
							for (int i = 0; i < requestRemoveMonthStarts.length(); i++) {
								String dStr = requestRemoveMonthStarts.getString(i);
								Date startDate = ddf.parse(dStr);
								Date endDate = DateUtil.nextMonthStart(startDate);
								UniLog.log1("startDate:%s, endDate:%s", startDate, endDate);
								for (Date date : dateList) {
									Set<Integer> snList = sessionDateMap.get(date);
									if (date.compareTo(startDate) >= 0 && date.compareTo(endDate) < 0) {
										for (int snRg : snList) {
											JSONObject jobj = sessionMap.get(snRg);
											int id = jobj.getInt("id");
											addOrRemoveCalendarEvent(jobj, false);
											sessionIdMap.remove(id);
											sessionMap.remove(snRg);
										}
										sessionDateMap.remove(date);
									}
								}
							}
							UniLog.log1("end remove session sessionMap:%d, sessionDateMap:%d, sessionIdMap:%d", sessionMap.size(), sessionDateMap.size(), sessionIdMap.size());
						}
						showDateConflictIconMap.clear();
						setupRowList(aStart, aEnd);
						callEventListJs();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		winCalendar.addEventListener("onClickEventItem", new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s, data:%s", event, event.getData());
				if (event.getData() != null && event.getData() instanceof org.zkoss.json.JSONObject) {
					org.zkoss.json.JSONObject jobj = (org.zkoss.json.JSONObject)event.getData();
					Integer rg;
					int id = NumberUtils.toInt((String)jobj.get("id"));
					//Student should not allow to click calendar event
					if (StringUtils.isBlank(getCurrentStudentCode()) && id > 0 && (rg = sessionIdMap.get(id)) != null && rg > 0) {
						JSONObject item = sessionMap.get(rg);
						int avrg = item.getInt("tAvRg");
						UniLog.log1("rg:%d, avrg:%d", rg, avrg);
						try {
							JSONObject jo = new JSONObject();
							JSONArray ja = new JSONArray();
							jo.put("tablist", ja);
							if (avrg > 0) {
								BiView pov = sessionHelper.getBiSchema().getViewByName("edu.Course");
								ja.put(pov.getTable().getName());
								jo.put("wherestr", "eaav0_rg = " + avrg);
								String key = sessionHelper.putOneTimeData(jo);
								ZkUtil.js("openNewTab('%s')", "zkbiloader.html?action=update&viewid=edu.Course&page_id=course_01&zul=zkbiloader.zul&prefix=zkbi&composer=edu.ZkBiComposerCourse&closetab=Y&sidemenu=N&querycondition="+key);
							}
							else {
								BiView pov = sessionHelper.getBiSchema().getViewByName("edu.Assessment");
								ja.put(pov.getTable().getName());
								jo.put("wherestr", "essnas_rg = " + rg);
								String key = sessionHelper.putOneTimeData(jo);
								ZkUtil.js("openNewTab('%s')", "zkbiloader.html?action=update&viewid=edu.Assessment&page_id=Assesment_01&zul=zkbiloader.zul&prefix=zkbi&composer=edu.ZkBiComposerAssessment&closetab=Y&sidemenu=N&querycondition="+key);
							}
							UniLog.log1("jo:%s", jo);
						}
						catch(Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		});
		
		addRowListAllEventListener(cbEventListAll, eventRowList);
		addRowListAllEventListener(cbTutorListAll, tutorRowList);
		addRowListAllEventListener(cbFacilityListAll, facilityRowList);
		addRowListAllEventListener(cbCoursetypeListAll, coursetypeRowList);

		holidayMap.clear();
		sessionMap.clear();
		courseMap.clear();
		assessmentMap.clear();
		tutorMap.clear();
		facilityMap.clear();
		coursetypeMap.clear();
		sessionDateMap.clear();
		sessionIdMap.clear();
		loadHolidayDatas();

		JSONObject json = new JSONObject();
		JSONObject jsonOpt = new JSONObject();
		JSONArray jsonEvents = new JSONArray();
		json.put("opt", jsonOpt);
		json.put("events", jsonEvents);
		jsonOpt.put("selectable", false);
		jsonOpt.put("editable", false);
		//jsonOpt.put("eventClick", "");
		jsonOpt.put("requestMonthsData", true);
		jsonOpt.put("requestMaxMonthsSize", 5);
		jsonOpt.put("requestEventClickEvent", "onClickEventItem");
		ZkUtil.js("createCalendar('calendar001',%s,'%s')", json.toString(), sessionHelper.getLoginId());
		//UniLog.log1("json:" + json.toString(3));
		
		String helpId = StringUtils.defaultIfBlank(Executions.getCurrent().getParameter("helpid"), "edu.Calendar");
		UniLog.log1("helpId :%s", helpId);
   		new ZkBiHelpDialog(sessionHelper, btnHelp, winCalendar, "Calendar", helpId, "Calendar");
	}

	@Override
	protected int adjustRootCompWidthOffset() {
		return 0;
	}

	@Override
	protected boolean adjustRootCompWidth() {
		return false;
	}

	private void loadHolidayDatas() {
		BiResult biResult = null;
		try {
			biResult = BiResultHelper.create(sessionHelper, "edu.Holiday", null, -1, null);
			while (biResult.next()) {
				String desc = biResult.getCellString("eshd_desc");
				Date startDate = biResult.getCellDate("eshd_date");
				Date endDate = DateUtil.nextday(startDate);
				JSONObject jsonEvent = new JSONObject();
				//jsonEvent.put("rg", -1);
				jsonEvent.put("id", maxEventId++);
				jsonEvent.put("owner", sessionHelper.getLoginId());
				jsonEvent.put("title", desc);
				jsonEvent.put("allDay", true);
				jsonEvent.put("start", df.format(startDate));
				jsonEvent.put("end", df.format(endDate));
				jsonEvent.put("textColor", getContrastColorString(holidayBgColor));
				jsonEvent.put("color", getColorString(holidayBgColor));
				//jsonEvent.put("isPublic", false);
				jsonEvent.put("tVisibled", false);
				holidayMap.put(startDate, jsonEvent);
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

	private String getCurrentStudentName() {
		String studentCode = getCurrentStudentCode();
		if (StringUtils.isNotBlank(studentCode)) {
			BiResult biResult = null;
			try {
				biResult = BiResultHelper.create(sessionHelper, "edu.Student", String.format("essd_sdno = '%s'", studentCode), -1, null);
				if (biResult.next())
					return biResult.getCellString("essd_name");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if (biResult != null)
					biResult.close();
			}
		}
		return null;
	}

	private String getCurrentTutorName(String tutorCode) {
		if (StringUtils.isNotBlank(tutorCode)) {
			BiResult biResult = null;
			try {
				biResult = BiResultHelper.create(sessionHelper, "edu.Tutor", String.format("estt_ttno = '%s'", tutorCode), -1, null);
				if (biResult.next())
					return biResult.getCellString("estt_name");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if (biResult != null)
					biResult.close();
			}
		}
		return null;
	}

	private String getCurrentStudentCode() {
		if (StringUtils.isNotBlank(mCurStudentCode))
			return mCurStudentCode;
		if (!sessionHelper.isAdminUser() && sessionHelper.hasAccessRight("#student"))
			return sessionHelper.getLoginId().toUpperCase();
		return "";
	}

	private String getCurrentTutorCode() {
		if (StringUtils.isNotBlank(mCurTutorCode))
			return mCurTutorCode;
		if (!sessionHelper.isAdminUser() && sessionHelper.hasAccessRight("#tutor"))
			return sessionHelper.getLoginId().toUpperCase();
		return "";
	}

	private String getCurrentTutorCode1() {
		if (StringUtils.isNotBlank(mCurTutorCode))
			return mCurTutorCode;
		if (!sessionHelper.isAdminUser()) {
			String tutorCode =  sessionHelper.getLoginId().toUpperCase();
			if (sessionHelper.hasAccessRight("#edu") || sessionHelper.hasAccessRight("#eduadmin")) {
				BiResult br = null;
				try {
					br = BiResultHelper.create(sessionHelper, "edu.Tutor", String.format("estt_ttno = '%s'", tutorCode), -1, null);
					if (br.next())
						return tutorCode;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				finally {
					if (br != null)
						br.close();
				}
			}
			else if (sessionHelper.hasAccessRight("#tutor"))
				return tutorCode;
		}
		return "";
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
		String fcCode;
		String fcName;
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
			fcCode = brAssessment.getCellString("esfc_code");
			fcName = brAssessment.getCellString("esfc_name");
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
			fcCode = brCourseSession.getCellString("esfc_code");
			fcName = brCourseSession.getCellString("esfc_name");
		}
		String bgColor = "Wheat";
		String textColor = "Black";
		String title = remark;
		
		switch (type) {
		case 0: //Course
			//int colorIdx = avrg % courseBgColorList.length;
			bgColor = getColorString(getCourseBgColor(avrg));
			textColor = getContrastColorString(getCourseBgColor(avrg));

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
				/*if (!sessionHelper.isAdminUser() && sessionHelper.hasAccessRight("#student"))
					brCourse.addCustomCondition(String.format("essd_sdno = '%s'", sessionHelper.getLoginId().toUpperCase()));*/
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
						//m.put("eaav0_name", brCourse.getCellString("eaav0_name"));
						m.put("eaav0_name", fShowCName ? brCourse.getCellString("eaav0_name") : "");
						m.put("eaav0_coursetype", courseType);
						m.put("eaav0_esttrg", cttrg);
						m.put("estt_ttno", brCourse.getCellString("estt_ttno"));
						m.put("estt_name", brCourse.getCellString("estt_name"));
						m.put("eaav0_esfcrg", cfcrg);
						m.put("esfc_code", brCourse.getCellString("esfc_code"));
						m.put("esfc_name", brCourse.getCellString("esfc_name"));
						m.put("sdStartDate", sdStartDate);
						m.put("sdEndDate", sdEndDate);
						m.put("bgColor", bgColor);
						m.put("textColor", textColor);
						m.put("visibled", true);
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

			//only include subscribe session for student
			//if (!sessionHelper.isAdminUser() && sessionHelper.hasAccessRight("#student")) {
			if (StringUtils.isNotBlank(getCurrentStudentCode())) {
				Date sdStartDate = (Date)m.get("sdStartDate");
				Date sdEndDate = (Date)m.get("sdEndDate");
				if (DateUtil.isDateNull(sdStartDate) || DateUtil.isDateNull(sdEndDate) || date.compareTo(sdStartDate) < 0 || date.compareTo(sdEndDate) > 0)
					return;
			}
			break;
		case 1: //Assessment
			bgColor = getColorString(assessmentBgColor);
			textColor = getContrastColorString(assessmentBgColor);
			title = joinString(", ", remark, ttName, fcName);
			Map<String, Object> m0 = new HashMap<String, Object>();
			m0.put("visibled", true);
			assessmentMap.put(rg, m0);
			break;
		}

		Map<String, Object> m;
		if (!tutorMap.containsKey(ttrg)) {
			m = new HashMap<String, Object>();
			m.put("eaav0_esttrg", ttrg);
			m.put("estt_ttno", ttNo);
			m.put("estt_name", ttName);
			m.put("visibled", true);
			tutorMap.put(ttrg, m);
		}
		if (!facilityMap.containsKey(fcrg)) {
			m = new HashMap<String, Object>();
			m.put("eaav0_esfcrg", fcrg);
			m.put("esfc_code", fcCode);
			m.put("esfc_name", fcName);
			m.put("visibled", true);
			facilityMap.put(fcrg, m);
		}
		if (courseType != null && !coursetypeMap.containsKey(courseType)) {
			m = new HashMap<String, Object>();
			m.put("eaav0_coursetype", courseType);
			m.put("visibled", true);
			coursetypeMap.put(courseType, m);
		}

		if (!sessionMap.containsKey(rg)) {
			int id = maxEventId++;
			JSONObject jsonEvent = new JSONObject();
			//jsonEvent.put("rg", -1);
			jsonEvent.put("id", id);
			jsonEvent.put("owner", sessionHelper.getLoginId());
			jsonEvent.put("title", title);
			//jsonEvent.put("allDay", false);
			jsonEvent.put("start", df.format(Student.unionDateTime(date, startTime)));
			jsonEvent.put("end", df.format(Student.unionDateTime(date, endTime)));
			jsonEvent.put("textColor", textColor);
			jsonEvent.put("color", bgColor);
			//jsonEvent.put("isPublic", false);
			if (StringUtils.isBlank(getCurrentStudentCode())) //Student should not allow to click calendar event
				jsonEvent.put("cursor", "pointer");
			jsonEvent.put("tAvRg", avrg);
			jsonEvent.put("tType", type);
			jsonEvent.put("tTtRg", ttrg);
			jsonEvent.put("tTtNo", ttNo);
			jsonEvent.put("tTtName", ttName);
			jsonEvent.put("tFcRg", fcrg);
			jsonEvent.put("tFcCode", fcCode);
			jsonEvent.put("tFcName", fcName);
			jsonEvent.put("tCourseType", courseType);
			jsonEvent.put("tVisibled", false);
			sessionMap.put(rg, jsonEvent);
			Set<Integer> list = sessionDateMap.get(date);
			if (list == null) {
				list = new HashSet<Integer>();
				sessionDateMap.put(date, list);
			}
			list.add(rg);
			if (!sessionIdMap.containsKey(id))
				sessionIdMap.put(id, rg);
		}
	}

	private void loadSessionDatas(Date startDate, Date endDate) {
		UniLog.log1("loadSessionDatas %s, %s", startDate, endDate);
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
			if (StringUtils.isNotBlank(getCurrentTutorCode()))
				brCourseSession.addCustomCondition(String.format("estt_ttno = '%s'", getCurrentTutorCode()));
			ReturnMsg rtn;
			if ((rtn = brCourseSession.query(true, false)).getStatus()) {
				while (brCourseSession.next())
					loadSessionDatasOne(startDate, endDate, brCourseSession, null, brCourse, brCourseStudent);
			}
			else
				throw new Exception(rtn.getMsg());

			brAssessment.clearCondition();
			brAssessment.addCustomCondition(String.format("essnas_date >= '%s' and essnas_date < '%s'", sdf.format(startDate), sdf.format(endDate)));
			if (StringUtils.isNotBlank(getCurrentStudentCode()))
				brAssessment.addCustomCondition(String.format("essd_sdno = '%s'", getCurrentStudentCode()));
			if (StringUtils.isNotBlank(getCurrentTutorCode()))
				brAssessment.addCustomCondition(String.format("estt_ttno = '%s'", getCurrentTutorCode()));
			if ((rtn = brAssessment.query(true, false)).getStatus()) {
				while (brAssessment.next())
					loadSessionDatasOne(startDate, endDate, null, brAssessment, null, null);
			}
			else
				throw new Exception(rtn.getMsg());
			//handle conflict event
			for (Date d = startDate; d.compareTo(endDate) < 0; d = DateUtil.nextday(d)) {
				Set<Integer> snRgSet = sessionDateMap.get(d);
				if (snRgSet == null)
					continue;
				Integer[] snRgList = snRgSet.toArray(new Integer[0]);
				for (int i = 0; i < snRgList.length; i++) {
					JSONObject jobj1 = sessionMap.get(snRgList[i]);
					long startTime1 = df.parse(jobj1.getString("start")).getTime();
					long endTime1 = df.parse(jobj1.getString("end")).getTime();
					int ttrg1 = jobj1.getInt("tTtRg");
					int fcrg1 = jobj1.getInt("tFcRg");
					if (ttrg1 == 0 && fcrg1 == 0)
						continue;
					for (int j = i + 1; j < snRgList.length; j++) {
						JSONObject jobj2 = sessionMap.get(snRgList[j]);
						long startTime2 = df.parse(jobj2.getString("start")).getTime();
						long endTime2 = df.parse(jobj2.getString("end")).getTime();
						int ttrg2 = jobj2.getInt("tTtRg");
						int fcrg2 = jobj2.getInt("tFcRg");
						if (ttrg2 == 0 && fcrg2 == 0)
							continue;
						if (((ttrg1 == ttrg2 && ttrg1 != 0) || (fcrg1 == fcrg2 && fcrg1 != 0)) &&
								(startTime1 < endTime2 && startTime2 < endTime1)) {
							jobj1.put("faIcon", "exclamation-triangle");
							jobj1.put("faIconColor", "red");
							jobj2.put("faIcon", "exclamation-triangle");
							jobj2.put("faIconColor", "red");
							if (ttrg1 == ttrg2 && ttrg1 != 0) {
								jobj1.put("tTtConflict", true);
								jobj2.put("tTtConflict", true);
							}
							if (fcrg1 == fcrg2 && fcrg1 != 0) {
								jobj1.put("tFcConflict", true);
								jobj2.put("tFcConflict", true);
							}
							String extraDesc1 = joinString(", ", 
									jobj1.optBoolean("tTtConflict") ? "Tutor conflict" : "", 
									jobj1.optBoolean("tFcConflict") ? "Facility conflict" : "");
							String extraDesc2 = joinString(", ", 
									jobj2.optBoolean("tTtConflict") ? "Tutor conflict" : "", 
									jobj2.optBoolean("tFcConflict") ? "Facility conflict" : "");
							if (StringUtils.isNotBlank(extraDesc1))
								jobj1.put("extraDesc", extraDesc1);
							if (StringUtils.isNotBlank(extraDesc2))
								jobj2.put("extraDesc", extraDesc2);
						}
					}
				}
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
			if (brAssessment != null)
				brAssessment.close();
		}
	}

	private void setupRowList(final Date aStart, final Date aEnd) throws Exception {
		boolean holidayFlag = false;
		Map<Integer, Set<Integer>> tCourseMap = new LinkedHashMap<Integer, Set<Integer>>();
		Map<Integer, Set<Integer>> tTutorMap = new LinkedHashMap<Integer, Set<Integer>>();
		Map<Integer, Set<Integer>> tFacilityMap = new LinkedHashMap<Integer, Set<Integer>>();
		Map<String, Set<Integer>> tCoursetypeMap = new LinkedHashMap<String, Set<Integer>>();
		Set<Integer> activeCourseList = new LinkedHashSet<Integer>();
		Set<Integer> activeTutorList = new LinkedHashSet<Integer>();
		Set<Integer> activeFacilityList = new LinkedHashSet<Integer>();
		Set<String> activeCoursetypeList = new LinkedHashSet<String>();
		for (Date d = aStart; d.compareTo(aEnd) < 0; d = DateUtil.nextday(d)) {
			if (holidayMap.containsKey(d)) {
				if (d.compareTo(activeStart) >= 0 && d.compareTo(activeEnd) < 0)
					holidayFlag = true;
			}
			Set<Integer> sList = sessionDateMap.get(d);
			if (sList != null) {
				for (int snrg : sList) {
					JSONObject jobj = sessionMap.get(snrg);
					int avrg = jobj.getInt("tAvRg");
					int type = jobj.getInt("tType");
					switch (type) {
					case 0:
						Set<Integer> list = tCourseMap.get(avrg);
						if (list == null) {
							list = new LinkedHashSet<Integer>();
							tCourseMap.put(avrg, list);
						}
						list.add(snrg);
						if (d.compareTo(activeStart) >= 0 && d.compareTo(activeEnd) < 0)
							activeCourseList.add(avrg);
						break;
					case 1:
						break;
					}
					int ttrg = jobj.getInt("tTtRg");
					int fcrg = jobj.getInt("tFcRg");
					String courseType = jobj.optString("tCourseType", null);

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

					if (courseType != null) {
						list = tCoursetypeMap.get(courseType);
						if (list == null) {
							list = new LinkedHashSet<Integer>();
							tCoursetypeMap.put(courseType, list);
						}
						list.add(snrg);
					}

					if (d.compareTo(activeStart) >= 0 && d.compareTo(activeEnd) < 0) {
						activeTutorList.add(ttrg);
						activeFacilityList.add(fcrg);
						if (courseType != null)
							activeCoursetypeList.add(courseType);
					}
				}
			}
		}

		//setup event list
		Grid grid = (Grid) winEventList.query("#gridEventList");
		if (grid != null)
			winEventList.removeChild(grid);
		final GridHelper gh = new GridHelper(2);
		gh.setId("gridEventList");
		gh.setWidth("100%");
		gh.getColumn(0).setHflex("1");
		gh.getColumn(1).setWidth("60px");
		gh.setSclass("eventListGrid");
		eventRowList.clear();

		//holiday 
		if (holidayFlag) {
			Label label = new Label("Holiday");
			label.setStyle("color:" + getContrastColorString(holidayBgColor) + " !important");
			Checkbox cb = new Checkbox();
			cb.setMold("switch");
			cb.setChecked(holidayEventVisibled);
			cb.addEventListener(Events.ON_CHECK, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					Checkbox cb = (Checkbox)event.getTarget();
					holidayEventVisibled = cb.isChecked();
					clearJsEventList();
					for (Date d = aStart; d.compareTo(aEnd) < 0; d = DateUtil.nextday(d)) {
						JSONObject jobj = holidayMap.get(d);
						if (jobj != null)
							addOrRemoveCalendarEvent(jobj, holidayEventVisibled);
					}
					callEventListJs();
					checkRowListAll(eventRowList, cbEventListAll);
				}
			});
			Map<String, Object> m1 = new HashMap<String, Object>();
			m1.put("label", label);
			m1.put("cb", cb);
			m1.put("bgColor", getColorString(holidayBgColor));
			eventRowList.add(m1);
		}
		for (Date d = aStart; d.compareTo(aEnd) < 0; d = DateUtil.nextday(d)) {
			JSONObject jobj = holidayMap.get(d);
			if (jobj != null)
				addOrRemoveCalendarEvent(jobj, holidayEventVisibled);
		}

		//assessment 
		assessmentRowList.clear();
		for (Date d = aStart; d.compareTo(aEnd) < 0; d = DateUtil.nextday(d)) {
			Set<Integer> sList = sessionDateMap.get(d);
			if (sList != null) {
				for (int snrg : sList) {
					final JSONObject jobj = sessionMap.get(snrg);
					if (jobj.getInt("tType") == 1) {
						String title = jobj.getString("title");
						final int ttrg = jobj.getInt("tTtRg");
						final int fcrg = jobj.getInt("tFcRg");
						final Map<String, Object> m = assessmentMap.get(snrg);
						boolean ttVisibled = (Boolean)tutorMap.get(ttrg).get("visibled");
						boolean fcVisibled = (Boolean)facilityMap.get(fcrg).get("visibled");
						boolean aVisibled = (Boolean) m.get("visibled");
						Label label = new Label(title);
						label.setStyle("color:" + getContrastColorString(assessmentBgColor) + " !important");
						Checkbox cb = new Checkbox();
						cb.setMold("switch");
						cb.setChecked(aVisibled);
						cb.addEventListener(Events.ON_CHECK, new ZkBiEventListener<Event>() {
							@Override
							public void onZkBiEvent(Event event) throws Exception {
								Checkbox cb = (Checkbox)event.getTarget();
								boolean ttVisibled = (Boolean)tutorMap.get(ttrg).get("visibled");
								boolean fcVisibled = (Boolean)facilityMap.get(fcrg).get("visibled");
								m.put("visibled", cb.isChecked());
								clearJsEventList();
								addOrRemoveCalendarEvent(jobj, cb.isChecked() && ttVisibled && fcVisibled);
								callEventListJs();
								checkRowListAll(eventRowList, cbEventListAll);
							}
						});
						if (d.compareTo(activeStart) >= 0 && d.compareTo(activeEnd) < 0) {
							Map<String, Object> m1 = new HashMap<String, Object>();
							m1.put("label", label);
							m1.put("cb", cb);
							m1.put("bgColor", getColorString(assessmentBgColor));
							m1.put("title", title);
							assessmentRowList.add(m1);
						}
						addOrRemoveCalendarEvent(jobj, aVisibled && ttVisibled && fcVisibled);
					}
				}
			}
		}
		assessmentRowList.sort(new Comparator<Map<String, Object>>(){
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				String title1 = (String) o1.get("title");
				String title2 = (String) o2.get("title");
				return title1.compareTo(title2);
			}
		});
		eventRowList.addAll(assessmentRowList);

		//course
		courseRowList.clear();
		for (Map.Entry<Integer, Set<Integer>> entry : tCourseMap.entrySet()) {
			int avrg = entry.getKey();
			final Set<Integer> snList = entry.getValue();
			final Map<String, Object> m = courseMap.get(avrg);
			final boolean cVisibled = (Boolean) m.get("visibled");
			if (activeCourseList.contains(avrg)) {
				Label label = new Label(joinString(", ", m.get("eaav0_code"), m.get("eaav0_name"), m.get("estt_name"), m.get("esfc_name")));
				label.setStyle("color:" + m.get("textColor") + " !important");
				Checkbox cb = new Checkbox();
				cb.setMold("switch");
				cb.setChecked(cVisibled);
				cb.addEventListener(Events.ON_CHECK, new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						Checkbox cb = (Checkbox) event.getTarget();
						m.put("visibled", cb.isChecked());
						clearJsEventList();
						for (int snrg : snList) {
							JSONObject jobj = sessionMap.get(snrg);
							int ttrg = jobj.getInt("tTtRg");
							int fcrg = jobj.getInt("tFcRg");
							String courseType = jobj.optString("tCourseType", null);
							boolean ttVisibled = (Boolean)tutorMap.get(ttrg).get("visibled");
							boolean fcVisibled = (Boolean)facilityMap.get(fcrg).get("visibled");
							boolean ctVisibled = (Boolean)coursetypeMap.get(courseType).get("visibled");
							addOrRemoveCalendarEvent(jobj, cb.isChecked() && ttVisibled && fcVisibled && ctVisibled);
						}
						callEventListJs();
						checkRowListAll(eventRowList, cbEventListAll);
					}
				});
				Map<String, Object> m1 = new HashMap<String, Object>();
				m1.put("label", label);
				m1.put("cb", cb);
				m1.put("eaav0_rg", m.get("eaav0_rg"));
				m1.put("eaav0_esttrg", m.get("eaav0_esttrg"));
				m1.put("eaav0_esfcrg", m.get("eaav0_esfcrg"));
				m1.put("eaav0_code", m.get("eaav0_code"));
				m1.put("estt_name", m.get("estt_name"));
				m1.put("esfc_name", m.get("esfc_name"));
				m1.put("bgColor", m.get("bgColor"));
				courseRowList.add(m1);
			}
			for (int snrg : snList) {
				JSONObject jobj = sessionMap.get(snrg);
				int ttrg = jobj.getInt("tTtRg");
				int fcrg = jobj.getInt("tFcRg");
				boolean ttVisibled = (Boolean)tutorMap.get(ttrg).get("visibled");
				boolean fcVisibled = (Boolean)facilityMap.get(fcrg).get("visibled");
				addOrRemoveCalendarEvent(jobj, cVisibled && ttVisibled && fcVisibled);
			}
		}
		courseRowList.sort(new Comparator<Map<String, Object>>(){
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				String courseCode1 = (String) o1.get("eaav0_code");
				String courseCode2 = (String) o2.get("eaav0_code");
				return courseCode1.compareTo(courseCode2);
			}
		});

		eventRowList.addAll(courseRowList);
		for (Map<String, Object> m1 : eventRowList) {
			gh.addRow((Label)m1.get("label"), (Checkbox)m1.get("cb"));
			int c = gh.getRows().getChildren().size() - 1;
			gh.getRow(c).setStyle("background-color:" + m1.get("bgColor") + " !important");
		}
		checkRowListAll(eventRowList, cbEventListAll);
		winEventList.appendChild(gh);
		winEventList.setVisible(!eventRowList.isEmpty()); //If the filter list is empty. Hide the filter list
		
		
		//setup tutor list
		grid = (Grid) winTutorList.query("#gridTutorList");
		if (grid != null)
			winTutorList.removeChild(grid);
		final GridHelper ght = new GridHelper(2);
		ght.setId("gridTutorList");
		ght.setWidth("100%");
		ght.getColumn(0).setHflex("1");
		ght.getColumn(1).setWidth("60px");
		ght.setSclass("eventListGrid");

		//tutor
		tutorRowList.clear();
		for (Map.Entry<Integer, Set<Integer>> entry : tTutorMap.entrySet()) {
			int ttrg = entry.getKey();
			final Set<Integer> snList = entry.getValue();
			final Map<String, Object> m = tutorMap.get(ttrg);
			final boolean ttVisibled = (Boolean) m.get("visibled");
			if (activeTutorList.contains(ttrg)) {
				String labelStr = joinString(", ", m.get("estt_ttno"), m.get("estt_name"));
				if (StringUtils.isBlank(labelStr))
					labelStr = "No tutor assigned";
				Label label = new Label(labelStr);
				Checkbox cb = new Checkbox();
				cb.setMold("switch");
				cb.setChecked(ttVisibled);
				cb.addEventListener(Events.ON_CHECK, new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						Checkbox cb = (Checkbox) event.getTarget();
						m.put("visibled", cb.isChecked());
						clearJsEventList();
						for (int snrg : snList) {
							JSONObject jobj = sessionMap.get(snrg);
							int type = jobj.getInt("tType");
							int avrg = jobj.getInt("tAvRg");
							int fcrg = jobj.getInt("tFcRg");
							String courseType = jobj.optString("tCourseType", null);
							boolean cVisibled = (type == 1) ? (Boolean)assessmentMap.get(snrg).get("visibled") : (Boolean)courseMap.get(avrg).get("visibled");
							boolean fcVisibled = (Boolean)facilityMap.get(fcrg).get("visibled");
							if (courseType != null) {
								boolean ctVisibled = (Boolean)coursetypeMap.get(courseType).get("visibled");
								addOrRemoveCalendarEvent(jobj, cb.isChecked() && cVisibled && fcVisibled && ctVisibled);
							}
							else
								addOrRemoveCalendarEvent(jobj, cb.isChecked() && cVisibled && fcVisibled);
						}
						callEventListJs();
						checkRowListAll(tutorRowList, cbTutorListAll);
					}
				});
				Map<String, Object> m1 = new HashMap<String, Object>();
				m1.put("label", label);
				m1.put("cb", cb);
				m1.put("eaav0_esttrg", m.get("eaav0_esttrg"));
				m1.put("estt_ttno", m.get("estt_ttno"));
				m1.put("estt_name", m.get("estt_name"));
				tutorRowList.add(m1);
			}
		}
		tutorRowList.sort(new Comparator<Map<String, Object>>(){
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				String ttCode1 = (String) o1.get("estt_ttno");
				String ttCode2 = (String) o2.get("estt_ttno");
				return ttCode1.compareTo(ttCode2);
			}
		});
		for (Map<String, Object> m1 : tutorRowList) {
			ght.addRow((Label)m1.get("label"), (Checkbox)m1.get("cb"));
		}
		checkRowListAll(tutorRowList, cbTutorListAll);
		winTutorList.appendChild(ght);
		winTutorList.setVisible(!tutorRowList.isEmpty()); //If the filter list is empty. Hide the filter list
		
		
		//setup facility list
		grid = (Grid) winFacilityList.query("#gridFacilityList");
		if (grid != null)
			winFacilityList.removeChild(grid);
		final GridHelper ghf = new GridHelper(2);
		ghf.setId("gridFacilityList");
		ghf.setWidth("100%");
		ghf.getColumn(0).setHflex("1");
		ghf.getColumn(1).setWidth("60px");
		ghf.setSclass("eventListGrid");

		//facility
		facilityRowList.clear();
		for (Map.Entry<Integer, Set<Integer>> entry : tFacilityMap.entrySet()) {
			int fcrg = entry.getKey();
			final Set<Integer> snList = entry.getValue();
			final Map<String, Object> m = facilityMap.get(fcrg);
			final boolean fcVisibled = (Boolean) m.get("visibled");
			if (activeFacilityList.contains(fcrg)) {
				String labelStr = joinString(", ", m.get("esfc_code"), m.get("esfc_name"));
				if (StringUtils.isBlank(labelStr))
					labelStr = "No facility assigned";
				Label label = new Label(labelStr);
				Checkbox cb = new Checkbox();
				cb.setMold("switch");
				cb.setChecked(fcVisibled);
				cb.addEventListener(Events.ON_CHECK, new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						Checkbox cb = (Checkbox) event.getTarget();
						m.put("visibled", cb.isChecked());
						clearJsEventList();
						for (int snrg : snList) {
							JSONObject jobj = sessionMap.get(snrg);
							int type = jobj.getInt("tType");
							int avrg = jobj.getInt("tAvRg");
							int ttrg = jobj.getInt("tTtRg");
							String courseType = jobj.optString("tCourseType", null);
							boolean cVisibled = (type == 1) ? (Boolean)assessmentMap.get(snrg).get("visibled") : (Boolean)courseMap.get(avrg).get("visibled");
							boolean ttVisibled = (Boolean)tutorMap.get(ttrg).get("visibled");
							if (courseType != null) {
								boolean ctVisibled = (Boolean)coursetypeMap.get(courseType).get("visibled");
								addOrRemoveCalendarEvent(jobj, cb.isChecked() && cVisibled && ttVisibled && ctVisibled);
							}
							else
								addOrRemoveCalendarEvent(jobj, cb.isChecked() && cVisibled && ttVisibled);
						}
						callEventListJs();
						checkRowListAll(facilityRowList, cbFacilityListAll);
					}
				});
				Map<String, Object> m1 = new HashMap<String, Object>();
				m1.put("label", label);
				m1.put("cb", cb);
				m1.put("eaav0_esfcrg", m.get("eaav0_esfcrg"));
				m1.put("esfc_code", m.get("esfc_code"));
				m1.put("esfc_name", m.get("esfc_name"));
				facilityRowList.add(m1);
			}
		}
		facilityRowList.sort(new Comparator<Map<String, Object>>(){
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				String fcCode1 = (String) o1.get("esfc_code");
				String fcCode2 = (String) o2.get("esfc_code");
				return fcCode1.compareTo(fcCode2);
			}
		});
		for (Map<String, Object> m1 : facilityRowList) {
			ghf.addRow((Label)m1.get("label"), (Checkbox)m1.get("cb"));
		}
		checkRowListAll(facilityRowList, cbFacilityListAll);
		winFacilityList.appendChild(ghf);
		winFacilityList.setVisible(!facilityRowList.isEmpty()); //If the filter list is empty. Hide the filter list
		
		//setup facility list
		grid = (Grid) winCoursetypeList.query("#gridCoursetypeList");
		if (grid != null)
			winCoursetypeList.removeChild(grid);
		final GridHelper ghc = new GridHelper(2);
		ghc.setId("gridCoursetypeList");
		ghc.setWidth("100%");
		ghc.getColumn(0).setHflex("1");
		ghc.getColumn(1).setWidth("60px");
		ghc.setSclass("eventListGrid");

		//Course Type
		coursetypeRowList.clear();
		for (Map.Entry<String, Set<Integer>> entry : tCoursetypeMap.entrySet()) {
			String courseType = entry.getKey();
			final Set<Integer> snList = entry.getValue();
			final Map<String, Object> m = coursetypeMap.get(courseType);
			final boolean ctVisibled = (Boolean) m.get("visibled");
			if (activeCoursetypeList.contains(courseType)) {
				String labelStr = (String) m.get("eaav0_coursetype");
				if (StringUtils.isBlank(labelStr))
					labelStr = "No course type assigned";
				else
					labelStr += " Course";
				Label label = new Label(labelStr);
				Checkbox cb = new Checkbox();
				cb.setMold("switch");
				cb.setChecked(ctVisibled);
				cb.addEventListener(Events.ON_CHECK, new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						Checkbox cb = (Checkbox) event.getTarget();
						m.put("visibled", cb.isChecked());
						clearJsEventList();
						for (int snrg : snList) {
							JSONObject jobj = sessionMap.get(snrg);
							int type = jobj.getInt("tType");
							int avrg = jobj.getInt("tAvRg");
							int ttrg = jobj.getInt("tTtRg");
							int fcrg = jobj.getInt("tFcRg");
							boolean cVisibled = (type == 1) ? (Boolean)assessmentMap.get(snrg).get("visibled") : (Boolean)courseMap.get(avrg).get("visibled");
							boolean ttVisibled = (Boolean)tutorMap.get(ttrg).get("visibled");
							boolean fcVisibled = (Boolean)facilityMap.get(fcrg).get("visibled");
							addOrRemoveCalendarEvent(jobj, cb.isChecked() && cVisibled && ttVisibled && fcVisibled);
						}
						callEventListJs();
						checkRowListAll(coursetypeRowList, cbCoursetypeListAll);
					}
				});
				Map<String, Object> m1 = new HashMap<String, Object>();
				m1.put("label", label);
				m1.put("cb", cb);
				m1.put("eaav0_coursetype", m.get("eaav0_coursetype"));
				coursetypeRowList.add(m1);
			}
		}
		coursetypeRowList.sort(new Comparator<Map<String, Object>>(){
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				String ctCode1 = (String) o1.get("eaav0_coursetype");
				String ctCode2 = (String) o2.get("eaav0_coursetype");
				return ctCode1.compareTo(ctCode2);
			}
		});
		for (Map<String, Object> m1 : coursetypeRowList) {
			ghc.addRow((Label)m1.get("label"), (Checkbox)m1.get("cb"));
		}
		checkRowListAll(coursetypeRowList, cbCoursetypeListAll);
		winCoursetypeList.appendChild(ghc);
		winCoursetypeList.setVisible(!coursetypeRowList.isEmpty()); //If the filter list is empty. Hide the filter list
	}


	private void checkRowListAll(List<Map<String, Object>> rowList, Checkbox cbListAll) {
		Boolean flag = null;
		for (Map<String, Object> mt : rowList) {
			Checkbox cb = (Checkbox)mt.get("cb");
			if (flag == null)
				flag = cb.isChecked();
			else if (flag != cb.isChecked()) {
				flag = null;
				break;
			}
		}
		if (flag != null)
			cbListAll.setChecked(flag);
	}
	
	private void addRowListAllEventListener(Checkbox cbListAll, final List<Map<String, Object>> rowList) {
		cbListAll.addEventListener("onCallJs", new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				callEventListJs();
			}
		});
		cbListAll.addEventListener(Events.ON_CHECK, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("cbListAll event:%s", event);
				Checkbox cb = (Checkbox)event.getTarget();
				clearJsEventList();
				for (Map<String, Object> mt : rowList) {
					Checkbox cb1 = (Checkbox)mt.get("cb");
					if (cb1.isChecked() != cb.isChecked()) {
						cb1.setChecked(cb.isChecked());
						Events.echoEvent(Events.ON_CHECK, cb1, null);
					}
				}
				Events.echoEvent("onCallJs", cb, null);
			}
		});
	}
	
	private int jsEventListRef;
	private List<JSONObject> jsAddEventList = new ArrayList<JSONObject>();
	private List<Integer> jsRemoveEventList = new ArrayList<Integer>();
	private List<Date> jsShowCalenderDayNumberFaIconList = new ArrayList<Date>();
	private List<Date> jsHideCalenderDayNumberFaIconList = new ArrayList<Date>();
	private void clearJsEventList() {
		if (++jsEventListRef > 1)
			return;
		UniLog.log1("clearJsEventList ref:%d", jsEventListRef);
		jsAddEventList.clear();
		jsRemoveEventList.clear();
		jsShowCalenderDayNumberFaIconList.clear();
		jsHideCalenderDayNumberFaIconList.clear();
	}
	private void callEventListJs() {
		if (--jsEventListRef > 0)
			return;
		UniLog.log1("callEventListJs ref:%d, jsAddEventList size:%d, jsRemoveEventList size:%d, jsShowCalenderDayNumberFaIconList size:%d, jsHideCalenderDayNumberFaIconList size:%d", 
						jsEventListRef, jsAddEventList.size(), jsRemoveEventList.size(), jsShowCalenderDayNumberFaIconList.size(), jsHideCalenderDayNumberFaIconList.size());
		StringBuilder sbJs = new StringBuilder();
		if (!jsAddEventList.isEmpty()) {
			JSONArray jarr = new JSONArray();
			for (JSONObject jobj : jsAddEventList)
				jarr.put(jobj);
			sbJs.append(String.format("addCalendarEventList(%s);", jarr));
		}
		if (!jsRemoveEventList.isEmpty()) {
			JSONArray jarr = new JSONArray();
			for (int id : jsRemoveEventList)
				jarr.put("" + id);
			sbJs.append(String.format("removeCalendarEventList(%s);", jarr));
		}
		if (!jsShowCalenderDayNumberFaIconList.isEmpty()) {
			JSONArray jarr = new JSONArray();
			for (Date date : jsShowCalenderDayNumberFaIconList)
				jarr.put(ddf.format(date));
			sbJs.append(String.format("showCalenderDayNumberFaIconByList(%s, '%s', '%s');", jarr, "exclamation-triangle", "red"));
		}
		if (!jsHideCalenderDayNumberFaIconList.isEmpty()) {
			JSONArray jarr = new JSONArray();
			for (Date date : jsHideCalenderDayNumberFaIconList)
				jarr.put(ddf.format(date));
			sbJs.append(String.format("showCalenderDayNumberFaIconByList(%s);", jarr));
		}
		ZkUtil.js(sbJs.toString());
	}
	
	private void addOrRemoveCalendarEvent(JSONObject jobj, boolean newVisibled) throws Exception {
		boolean oldVisibled = jobj.getBoolean("tVisibled");
		jobj.put("tVisibled", newVisibled);
		
		if (newVisibled && !oldVisibled) {
			UniLog.log1("addCalendarEvent:%s", jobj);
			//ZkUtil.js("addCalendarEvent(%s)", jobj1);
			JSONObject jobj1 = new JSONObject();
			jobj1.put("id", jobj.get("id"));
			jobj1.put("owner", jobj.get("owner"));
			jobj1.put("title", jobj.get("title"));
			jobj1.put("start", jobj.get("start"));
			jobj1.put("end", jobj.get("end"));
			jobj1.put("textColor", jobj.get("textColor"));
			jobj1.put("color", jobj.get("color"));
			if (jobj.has("allDay"))
				jobj1.put("allDay", jobj.get("allDay"));
			if (jobj.has("cursor"))
				jobj1.put("cursor", jobj.get("cursor"));
			if (jobj.has("faIcon"))
				jobj1.put("faIcon", jobj.get("faIcon"));
			if (jobj.has("faIconColor"))
				jobj1.put("faIconColor", jobj.get("faIconColor"));
			if (jobj.has("extraDesc"))
				jobj1.put("extraDesc", jobj.get("extraDesc"));
			jsAddEventList.add(jobj1);
		}
		else if (!newVisibled && oldVisibled) {
			UniLog.log1("removeCalendarEvent:%s, %s", jobj.getInt("id"), jobj.getString("start"));
			//ZkUtil.js("removeCalendarEvent(%s)", jobj.getInt("id"));
			jsRemoveEventList.add(jobj.getInt("id"));
		}
		//show DayNumber conflict icon
		Date startDate = DateUtil.dayBeginning(df.parse(jobj.getString("start")));
		if (startDate.compareTo(activeStart) >= 0 && startDate.compareTo(activeEnd) < 0) {
			Set<Integer> list = sessionDateMap.get(startDate);
			if (list != null) {
				boolean newHasConflict = false;
				for (int snrg : list) {
					JSONObject o = sessionMap.get(snrg);
					if (o.getBoolean("tVisibled") && (o.optBoolean("tTtConflict") || o.optBoolean("tFcConflict"))) {
						newHasConflict = true;
						break;
					}
				}
				Boolean oldHasConflict = showDateConflictIconMap.get(startDate);
				if (oldHasConflict == null)
					oldHasConflict = false;
				if (newHasConflict != oldHasConflict) {
					if (newHasConflict)
						//ZkUtil.js("showCalenderDayNumberFaIcon('%s', '%s', '%s')", ddf.format(startDate), "exclamation-triangle", "red");
						jsShowCalenderDayNumberFaIconList.add(startDate);
					else
						//ZkUtil.js("showCalenderDayNumberFaIcon('%s')", ddf.format(startDate));
						jsHideCalenderDayNumberFaIconList.add(startDate);
					showDateConflictIconMap.put(startDate, newHasConflict);
				}
			}
		}
	}
	
	private static int getContrastColor(int color) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;

		// Calculate the perceptive luminance (aka luma) - human eye favors green color... 
		double luma = ((0.299 * r) + (0.587 * g) + (0.114 * b)) / 255;

		// Return black for bright colors, white for dark colors
		return luma > 0.5 ? 0 : 0xFFFFFF;
	} 
	
	private static String getColorString(int color) {
		return String.format("#%06x", color);
	}

	private static String getContrastColorString(int color) {
		return getColorString(getContrastColor(color));
	}
	
	private int courseBgColorCount;
	private int getCourseBgColor(int courseRg) {
		Integer color = courseBgColorMap.get(courseRg);
		if (color != null)
			return color;
		color = courseBgColorList[courseBgColorCount++ % courseBgColorList.length];
		courseBgColorMap.put(courseRg, color);
		return color;
	}
}
