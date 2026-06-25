package com.uniinformation.jxapp.edu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.impl.MessageboxDlg;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Radio;

import com.google.common.collect.Sets;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.edu.ProcessScanLog;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.CryptoUtil;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.RegUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.GridHelper;
import com.uniinformation.webcore.HlayoutHelper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;

public class Course extends JxZkBiBase {
	boolean confirmSaveFlag = false;  //a state variable
	private final TreeMap<Date, String> holidayMap = new TreeMap<Date, String>();
	private static int SESSION_MAX_LOOKUP_DAY = 60;  //set a reasonable lookup range
	/***
	 * 
	 * @param p_sessionDayStr sessionDayStr e.g. 1,3 - Monday and Wednesday
	 * @param p_startDate - initial date
	 * @param p_skipHoliday - true skip holiday
	 * @return
	 */
	private Date findNextSessionDate(String p_sessionDayStr, Date p_startDate, boolean p_skipHoliday) {
		return findNextSessionDate(sessionHelper, holidayMap, p_sessionDayStr, p_startDate, p_skipHoliday, false);
	}

	public static Date findNextSessionDate(SessionHelper sessionHelper, Map<Date, String> holidayMap, String p_sessionDayStr, Date p_startDate, boolean p_skipHoliday, boolean p_isFindReverse) {
		if (StringUtils.isBlank(p_sessionDayStr)) {
			UniLog.log1("sessionDayStr is blank");
			return null;
		}
		if (p_startDate == null) {
			return null;
		}
		if (p_skipHoliday)
			loadHolidayData(sessionHelper, holidayMap);
		Date curDate = p_startDate;
		for (int j=0;j<SESSION_MAX_LOOKUP_DAY;j++) {
			int dayInWeek = DateUtil.dayInWeek(curDate);
			UniLog.log1("j:%d curDate:%s dayInWeek:%d", j, curDate, dayInWeek);
			if (StringUtils.containsAny(p_sessionDayStr, "" + dayInWeek)){
				/*
				if (!p_skipHoliday || !holidayMap.containsKey(curDate)) {
					UniLog.log1("found valid date %s",  curDate);
					return curDate;
				}
				else
					UniLog.log1("skipHoliday");
				*/
				if (p_skipHoliday && (holidayMap.containsKey(curDate) || dayInWeek == 0)) {
					UniLog.log1("skipHoliday");
				}
				else {
					UniLog.log1("found valid date %s",  curDate);
					return curDate;
				}
			}
			curDate = p_isFindReverse ? DateUtil.prevday(curDate) : DateUtil.nextday(curDate);
		}
		return null;
	}
	/***
	 * 
	 * @param p_startDate
	 * @param p_maxSessionCnt
	 * @param p_sessionDayStr
	 * @param p_skipHoliday
	 * @return Date value can be null
	 */
	private List<Date> findSessionDates(Date p_startDate, Date p_endDate, int p_maxSessionCnt, String p_sessionDayStr, boolean p_skipHoliday) {
		return findSessionDates(sessionHelper, holidayMap, getBr().getCellDate("eaav0_enddate",true), p_startDate, p_endDate, p_maxSessionCnt, p_sessionDayStr, p_skipHoliday);
	}
	public static List<Date> findSessionDates(SessionHelper sessionHelper, Map<Date, String> holidayMap, Date courseEndDate, Date p_startDate, Date p_endDate, int p_maxSessionCnt, String p_sessionDayStr, boolean p_skipHoliday) {
		ArrayList<Date> sessionDates = new ArrayList<Date>();
		int maxSessionCnt = p_maxSessionCnt > 0 ? p_maxSessionCnt : 999;
		
		Date curDate = p_startDate;
		//Date courseEndDate = brCourse.getCellDate("eaav0_enddate",true);
		
		for (;;) {
			Date nextSessionDate = findNextSessionDate(sessionHelper, holidayMap, p_sessionDayStr, curDate, p_skipHoliday, false);
			if (nextSessionDate == null) {
				UniLog.log1("nextSessionDate is null, abort");
				break;
			}
			if (courseEndDate != null && DateUtil.isValid(courseEndDate) && nextSessionDate.compareTo(courseEndDate) > 0) {
				UniLog.log1("courseEndDate reached next:%s courseend:%s", nextSessionDate,courseEndDate);
				break;
			}
			
			if (nextSessionDate.compareTo(p_endDate) > 0) {
				UniLog.log1("endDate reached next:%s end:%s", nextSessionDate,p_endDate);
				break;
			}
			sessionDates.add(nextSessionDate);
			if (sessionDates.size() >= maxSessionCnt) {
				UniLog.log1("maxSessioCnt reached size:%d", sessionDates.size());
				break;
			}
			curDate = DateUtil.nextday(nextSessionDate);
		}

		return sessionDates;
		
	}
	public static List<Date> findSessionDatesReverse(SessionHelper sessionHelper, Map<Date, String> holidayMap, Date courseStartDate, Date p_startDate, Date p_endDate, int p_maxSessionCnt, String p_sessionDayStr, boolean p_skipHoliday) {
		ArrayList<Date> sessionDates = new ArrayList<Date>();
		int maxSessionCnt = p_maxSessionCnt > 0 ? p_maxSessionCnt : 999;
		
		Date curDate = p_startDate;
		//Date courseStartDate = brCourse.getCellDate("eaav0_startdate",true);
		
		for (;;) {
			Date nextSessionDate = findNextSessionDate(sessionHelper, holidayMap, p_sessionDayStr, curDate, p_skipHoliday, true);
			if (nextSessionDate == null) {
				UniLog.log1("nextSessionDate is null, abort");
				break;
			}
			if (courseStartDate != null && DateUtil.isValid(courseStartDate) && nextSessionDate.compareTo(courseStartDate) < 0) {
				UniLog.log1("courseStartDate reached next:%s coursestart:%s", nextSessionDate,courseStartDate);
				break;
			}
			
			if (nextSessionDate.compareTo(p_endDate) < 0) {
				UniLog.log1("endDate reached next:%s end:%s", nextSessionDate,p_endDate);
				break;
			}
			sessionDates.add(nextSessionDate);
			if (sessionDates.size() >= maxSessionCnt) {
				UniLog.log1("maxSessioCnt reached size:%d", sessionDates.size());
				break;
			}
			curDate = DateUtil.prevday(nextSessionDate);
		}

		return sessionDates;
	}
	private Date findNextCourseDate(boolean p_skipHoliday) {
		Date sessionDateMax = findMaxSessionDate();
		if (sessionDateMax != null) {
			Date nextSessionDate = findNextSessionDate(getBr().getCellString("eaav0_sessionday"), DateUtil.nextday(sessionDateMax), p_skipHoliday);
			UniLog.log1("nextSessioDate:%s", nextSessionDate);
			if (nextSessionDate != null) {
				return nextSessionDate;
			}
		}
		return getBr().getCellDate("eaav0_startdate");
	}
	private Date findMaxSessionDate() {
		final BiResult sr = getBr().getSubLink("edu.CourseSessionDet");
		Date sessionDateMax = null;
		
		//loop all session, find the max date
		for(int i=0;i<sr.getRowCount();i++) {
			//sr.loadOneRecV(i);
			CellCollection col = sr.getRowCollectionV(i);
			Date tmpDate = col.getCell("essncs_date").getDate();
			if (sessionDateMax == null || (tmpDate != null && tmpDate.getTime() > sessionDateMax.getTime())) {
				sessionDateMax = tmpDate;
			}
		}
		return sessionDateMax;
	}
	private boolean checkIsDupSessionDate(Date p_date) {
		if (p_date == null) {
			UniLog.log1("date is null");
			return false;
		}
		final BiResult sr = getBr().getSubLink("edu.CourseSessionDet");
		for(int i=0;i<sr.getRowCount();i++) {
			CellCollection col = sr.getRowCollectionV(i);
			Date tmpDate = col.getCell("essncs_date").getDate();
			if (tmpDate.getTime() == p_date.getTime()) {
				return true;
			}
		}
		return false;
	}
	
	private void generateDefaultSchedule(Date p_startDate, Date p_endDate, boolean p_skipHoliday, boolean p_skipDuplicate) {
		final BiResult sr = getBr().getSubLink("edu.CourseSessionDet");
		if (p_startDate == null) {
			ZkUtil.errMsg("Invalid Start Date");
			return;
		}
		if (p_endDate == null) {
			ZkUtil.errMsg("Invalid End Date");
			return;
		}
		if (p_startDate.compareTo(p_endDate) > 0) {
			ZkUtil.errMsg("Start Date > End Date");
			return;
		}
		
		String sessionDay = getBr().getCellString("eaav0_sessionday");
		UniLog.log1("startDate:%s day:%s",p_startDate,sessionDay);
		String[] sessionDayArr = StringUtils.split(sessionDay, ",;");
		ZkUtil.dumpData(sessionDayArr);
		if (sessionDayArr == null || sessionDayArr.length == 0) {
			ZkUtil.errMsg("Please set Days[Mon-Sun] first.");
			return;
		}
		for (String s : sessionDayArr) {
			if (!StringUtils.isNumeric(s) || NumberUtils.toInt(s) >= 7) {
				ZkUtil.errMsg("Invalid Days");
				return;
			}
		}
		Date startTime = getBr().getCellDate("eaav0_sessiontime");
		int sessionLen = getBr().getCellInt("eaav0_sessionlen");

		
		List<Date> sessionDates =  findSessionDates(p_startDate, p_endDate, -1, sessionDay, p_skipHoliday);
		
		int facilityRg = NumberUtils.toInt(jxAdd("eaav0_esfcrg").getValue().toString());
		int tutorRg = NumberUtils.toInt(jxAdd("eaav0_esttrg").getValue().toString());
		UniLog.log1("facilityRg:%d, tutorRg:%d", facilityRg, tutorRg);
		
		int addedCnt = 0;
		for (Date sessionDate : sessionDates) {
			try {
				if (p_skipDuplicate && checkIsDupSessionDate(sessionDate)) {
					UniLog.log1("ignore duplicate date: %s", sessionDate);
					continue;
				}
				ReturnMsg rtn = listboxAddRow(Course.this, sr, jxAdd("list_edu_CourseSessionDet"), null, -1);
				if (rtn.getStatus()) {
					addedCnt++;
					CellCollection col = sr.getRowCollectionV(sr.getRowCount()-1);
					//col.getCell("essncs_name").set(String.format("Session %02d/%02d",i+1,sessionCnt));
					col.getCell("essncs_name").set(String.format("%s %s",getBr().getCellString("eaav0_code"), DateUtil.toDateStringY4MD(sessionDate)));
					col.getCell("esatfc_atrg").set(facilityRg);
					col.getCell("esattt_atrg").set(tutorRg);
					if (startTime != null) {
						col.getCell("essncs_sttime").set(startTime);
						col.getCell("essncs_endtime").set(sessionLen > 0 ? new Date(startTime.getTime()+sessionLen*60*1000) : startTime);
					}
					if (sessionDate != null) {
						col.getCell("essncs_date").set(sessionDate);
					}
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}

		}
		ZkUtil.showMsg("Added %d new session", addedCnt);

	}
	public void delSessionByDateRange(Date p_date0, Date p_date1) {
		if(p_date0 == null || p_date1 == null || p_date0.getTime() > p_date1.getTime()) {
			ZkUtil.showErrMsg("Invalid date range");
			return;
		}
		BiResult sr = getBr().getSubLink("edu.CourseSessionDet");
		if (sr == null) return;
		
		Object o = null;
		JxField sv = jxAdd(sr);
		int delCnt = 0;
		for (int i=0; i<sr.getRowCount(); i++) {
			sr.loadOneRecV(i);
			Date date = sr.getCellDate("essncs_date",true);
			UniLog.log1("checking date:%s", date);
			if (date != null && date.getTime() >= p_date0.getTime() && date.getTime() <= p_date1.getTime()) {
				o = sr.getTrStatObj(new Integer(i));
				if(o != null) {
					sr.markDelete(o, true);
					sv.gridSetDataFormat(-1,i,"add_deleted");
					delCnt++;
				}
			}
		}
		if (delCnt > 0) {
			setDirtyFlag(true);
		}
		else {
			ZkUtil.showMsg("No session available.");
		}
		
	}
	
	@Override
	public void afterBind() {
		UniLog.log1("afterBind called");
		super.afterBind();
		new JxFieldAction("btGenSchedule") {
			public void actionPerformed(JxField fd){
				final GridHelper gh = new GridHelper(4);
				gh.setWidth("500px");
				//gh.getColumn(0).setHflex("min");  //why it does not work
				gh.getColumn(0).setWidth("70px");
				gh.getColumn(0).setAlign("right");
				gh.getColumn(1).setHflex("min");
				gh.getColumn(2).setWidth("50px");
				gh.getColumn(2).setAlign("right");
				gh.getColumn(3).setHflex("1");
				
				final Checkbox cbSkipHoliday = new Checkbox("Skip holiday");
				final Datebox dbStartDate = new Datebox();
				
				//Date startDate = getBr().getCellDate("eaav0_startdate");
				//Date startDate = findNextCourseDate(true);
				//Date startDate = findNextCourseDate(false);
				Date startDate = DateUtil.nextday(findMaxSessionDate());
				if (startDate == null) {
					startDate = getBr().getCellDate("eaav0_startdate");
				}
				if (startDate != null){
					dbStartDate.setValue(startDate);
				}
				final Datebox dbEndDate = new Datebox();
				if (startDate != null) {
					dbEndDate.setValue(DateUtil.prevday(DateUtil.nextmonth(startDate)));
				}
				
				cbSkipHoliday.setChecked(true);
				cbSkipHoliday.setDisabled(false);
				gh.addRow(new Label("From"), dbStartDate, new Label("To"), dbEndDate);
				gh.addRow(new Div(),new HlayoutHelper().add(cbSkipHoliday),new Div(), new Div());
				
				ZkUtil.buildMessageboxDlg("Generate schedule", 
					gh, 
					new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
					parentComp,
					new EventListener<Messagebox.ClickEvent>(){
						@Override
						public void onEvent(ClickEvent event) throws Exception {
							if (event.getButton() == null) return;
							switch (event.getButton()) {
							case OK:
								if (StringUtils.equalsAnyIgnoreCase(getBr().getCellString("eaav0_status"),"Cancelled")){
									ZkUtil.errMsg("The course was ended.");
									break;
								}
								generateDefaultSchedule(dbStartDate.getValue(),dbEndDate.getValue(), cbSkipHoliday.isChecked(),true);
								break;
							default:
								break;
							}
						}
					}
				).doHighlighted();
				
				
				
			}
		};
		
		new JxFieldAction("btDelSchedule") {
			@Override
			public void actionPerformed(JxField jxfield) {
				try {
					BiResult brSession = getBr().getSubLink("edu.CourseSessionDet");
					
					JxField jxf = jxAdd("list_" + replaceViewName(brSession.getView().getName()));
					final GridHelper gh = new GridHelper(4);
					gh.setWidth("500px");
					gh.getColumn(0).setWidth("70px");
					gh.getColumn(0).setAlign("right");
					gh.getColumn(1).setHflex("min");
					gh.getColumn(2).setWidth("50px");
					gh.getColumn(2).setAlign("right");
					gh.getColumn(3).setHflex("1");
					
					final Datebox dbStartDate = new Datebox();
					final Datebox dbEndDate = new Datebox();
					
					Date startDate = null;
					Date endDate = null;
					if (brSession.getRowCount() > 0) {
						if (brSession.loadOneRecV(0)) {
							startDate = brSession.getCellDate("essncs_date");
						}
						if (brSession.loadOneRecV(brSession.getRowCount() - 1)){
							endDate = brSession.getCellDate("essncs_date");
						}
					}
					if (startDate == null || startDate.getTime() < DateUtil.nextday(new Date()).getTime()) {
						startDate = DateUtil.nextday(new Date());
					}
					if (endDate == null || endDate.getTime() < DateUtil.nextday(new Date()).getTime()) {
						endDate = DateUtil.nextday(new Date());
					}
					dbStartDate.setValue(startDate);
					dbEndDate.setValue(endDate);
					gh.addRow(new Label("From"), dbStartDate, new Label("To"), dbEndDate);
					
					ZkUtil.buildMessageboxDlg("Delete schedule", 
						gh, 
						new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
						parentComp,
						new EventListener<Messagebox.ClickEvent>(){
							@Override
							public void onEvent(ClickEvent event) throws Exception {
								if (event.getButton() == null) return;
								switch (event.getButton()) {
								case OK:
									if (StringUtils.equalsAnyIgnoreCase(getBr().getCellString("eaav0_status"),"Cancelled")){
										ZkUtil.errMsg("The course was ended.");
										break;
									}
									if (dbStartDate.getValue() == null || dbStartDate.getValue().getTime() <= DateUtil.dayBeginning(new Date()).getTime()) {
										ZkUtil.errMsg("Cannot delete completed session");
										break;
									}
									delSessionByDateRange(dbStartDate.getValue(), dbEndDate.getValue());
									break;
								default:
									break;
								}
							}
						}
					).doHighlighted();
					/*
					
					
					
					
					//dirty logic for click all delete row button
					BiResult brSession = getBr().getSubLink("edu.CourseSessionDet");
					JxField jxf = jxAdd("list_" + replaceViewName(brSession.getView().getName()));
					Listbox listbox = (Listbox) jxf.getNativeObject();
					for (Component comp : listbox.getChildren()) {
						if (!(comp instanceof Listitem)) continue;
						UniLog.log1("found listitem");
						Listitem li = (Listitem)comp;
						if (!li.isSelectable() || li.isDisabled()) {
							UniLog.log1("cannot select or disabled");
							continue;
						}
						if (!(li.getFirstChild() instanceof Listcell)) {
							UniLog.log1("is not a listcell");
							continue;
						}
						Listcell lc = (Listcell)li.getFirstChild();
						for (Component comp2 : lc.getChildren()) {
							if (comp2 instanceof Button && StringUtils.equalsAnyIgnoreCase((String)comp2.getAttribute("JxZkListbox.deleteItemButton"), "Y")) {
								Button btn = (Button)comp2;
								Events.echoEvent(Events.ON_CLICK, btn,null);
							}
						}
					}
					*/
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		};
		/*
		//obsoleted
		new JxFieldAction("btAddRecCourse") {
			public void actionPerformed(JxField fd){
				//new a group
				final GridHelper gh = new GridHelper(2);
				gh.setWidth("700px");
				gh.getColumn(0).setHflex("min");
				gh.getColumn(0).setAlign("right");
				gh.getColumn(1).setHflex("1");
				
				//new field
				final Textbox tbCode = new Textbox();
				tbCode.setWidth("300px");
				final Textbox tbName = new Textbox();
				tbName.setWidth("400px");
				final Datebox dbStartDate = new Datebox();
				final Checkbox cbCarryStudent = new Checkbox("Carry student");
				final Checkbox cbCarrySubStatus = new Checkbox("Carry subscribe status");
				cbCarrySubStatus.setVisible(false);  //andrew210722 hide this field to avoid messy token handling
				final Checkbox cbGenerateSchedule = new Checkbox("Generate schedule");
				final Checkbox cbSkipHoliday = new Checkbox("Skip holiday");
				cbGenerateSchedule.setChecked(true);
				cbSkipHoliday.setChecked(true);
				
				gh.addRow(new Label("Course Code"), tbCode);
				gh.addRow(new Label("Course Name"), tbName);
				gh.addRow(new Label("Start Date"), dbStartDate);
				gh.addRow(new Div(),new HlayoutHelper().add(cbCarryStudent,cbCarrySubStatus));
				gh.addRow(new Div(),new HlayoutHelper().add(cbGenerateSchedule,cbSkipHoliday)); //hide these options first
				
				
				//init field
				tbCode.setValue(buildNewCourseCode(getBr().getCellString("eaav0_code")));
				//Date startDate = getBr().getCellDate("eaav0_startdate");
				Date startDate = findNextCourseDate(true);
				if (startDate != null){
					dbStartDate.setValue(startDate);
				}
				
				tbName.setValue(getBr().getCellString("eaav0_name"));
				cbCarryStudent.setChecked(true);
				cbCarryStudent.addEventListener(Events.ON_CHECK, new ZkBiEventListener() {

					@Override
					public void onZkBiEvent(Event event) throws Exception {
						cbCarrySubStatus.setDisabled(!cbCarryStudent.isChecked());
					}
					
				});
				ZkUtil.buildMessageboxDlg("Add Recurring Course", 
					gh, 
					new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
					parentComp,
					new EventListener<Messagebox.ClickEvent>(){
						@Override
						public void onEvent(ClickEvent event) throws Exception {
							if (event.getButton() == null) return;
							switch (event.getButton()) {
							case OK:
								UniLog.log1("ok click");
								int studentCnt = 0;
								BiResult newCourseBr = sessionHelper.newBiResult("edu.Course",true);
								newCourseBr.getCell("eaav0_code").set(tbCode.getValue());
								newCourseBr.getCell("eaav0_name").set(tbName.getValue());
								newCourseBr.getCell("eaav0_startdate").set(dbStartDate.getValue());
								newCourseBr.getCell("eaav0_numsession").set(getBr().getCell("eaav0_numsession"));
								newCourseBr.getCell("eaav0_sessionday").set(getBr().getCell("eaav0_sessionday"));
								newCourseBr.getCell("eaav0_sessiontime").set(getBr().getCell("eaav0_sessiontime"));
								newCourseBr.getCell("eaav0_sessionlen").set(getBr().getCell("eaav0_sessionlen"));
								newCourseBr.getCell("eaav0_fee").set(getBr().getCell("eaav0_fee"));
								newCourseBr.getCell("eaav0_esfcrg").set(getBr().getCell("eaav0_esfcrg"));
								newCourseBr.getCell("eaav0_esttrg").set(getBr().getCell("eaav0_esttrg"));
								
								
								
								//copy CourseStudent
								if (cbCarryStudent.isChecked()) {
									BiResult newCourseStudentBr = newCourseBr.getSubLink("edu.CourseStudent");
									UniLog.log1("edu.CourseStudent=%s", newCourseStudentBr);
									BiResult courseStudentBr = getBr().getSubLink("edu.CourseStudent");

									for(int i=0;i<courseStudentBr.getRowCount();i++) {
										//courseStudentBr.loadOneRecV(i);
										CellCollection courseStudentCol = courseStudentBr.getRowCollectionV(i);
										int sdrg = courseStudentCol.getCellInt("essbsd_sdrg");
										UniLog.log1("copy sdrd:%d", sdrg);
										if (sdrg > 0) {
											studentCnt++;
											BiCellCollection newCc = newCourseStudentBr.newRowCollection(true);
											newCc.getCell("essbsd_sdrg").set(sdrg);
											//if (cbCarrySubStatus.isChecked()) {
											//	newCc.getCell("essbsd_status").set(courseStudentCol.getCell("essbsd_status"));
											//}
										}
										else {
											UniLog.log1("ignore invalid sdrg:%d", sdrg);

										}
									}
								}
								
								//gen schedule
								if (cbGenerateSchedule.isChecked()) {
									List<Date> sessionDates =  findSessionDates(
																newCourseBr.getCellDate("eaav0_startdate"), 
																newCourseBr.getCellInt("eaav0_numsession"),
																newCourseBr.getCellString("eaav0_sessionday"),
																cbSkipHoliday.isChecked());
									ZkUtil.dumpData(sessionDates);
									BiResult newCourseSessionBr = newCourseBr.getSubLink("edu.CourseSessionDet");
									final Date startTime = getBr().getCellDate("eaav0_sessiontime");
									final int sessionLen = getBr().getCellInt("eaav0_sessionlen");
									
									for (int i=0; i<sessionDates.size(); i++) {
										BiCellCollection newCc = newCourseSessionBr.newRowCollection(true);
										newCc.getCell("essncs_name").set(String.format("Session %02d/%02d",i+1,sessionDates.size()));
										newCc.getCell("esatfc_atrg").set(newCourseBr.getCellInt("eaav0_esfcrg"));
										newCc.getCell("esattt_atrg").set(newCourseBr.getCellInt("eaav0_esttrg"));
										if (startTime != null) {
											newCc.getCell("essncs_sttime").set(startTime);
										}
										if (startTime != null && sessionLen > 0) {
											newCc.getCell("essncs_endtime").set(new Date(startTime.getTime()+sessionLen*60*1000));
										}
										if (sessionDates.get(i) != null) {
											newCc.getCell("essncs_date").set(sessionDates.get(i));
										}
									}
									
								}
								
								ReturnMsg rtn = newCourseBr.addCurrent();
								UniLog.log1("addCurrent result:%s", rtn);
								if (rtn.getStatus()) {
									needRefreshFlag = true;
									ZkUtil.msg("New course created\nCopy Student Count:%d",studentCnt);
								}
								else {
									ZkUtil.errMsg("Create course fail.\n%s", rtn.getMsg());
								}
								break;
							default:
								break;
							}
						}
					}
				).doHighlighted();
				
				
			}
		};
		*/

		new JxFieldAction("btTodayAttendance") {
			@Override
			public void actionPerformed(JxField jxfield) {
				if (isDirty())
					ZkUtil.showErrMsg("Please save the newly input values before mark today attendance");
				else {
					try {
						doMarkAttendance(true, null, jxfield);
					}
					catch (Exception e) {
						ZkUtil.showErrMsg(e.getMessage());
					}
				}
			}
		};

		new JxFieldChange("eaav0_fee") {
			@Override
			public boolean valueChanged(JxField jxfield, String orgvalue) {
				UniLog.log1("eaav0_fee valueChanged orgvalue:%s, newValue:%s", orgvalue, jxfield.getValue());
				if (curMode == JxZkBiBase.MODE_UPDATE) {
					Component comp = (Component)jxfield.getNativeObject();
					String initValue = (String)comp.getAttribute("initValue");
					String newValue = (String)jxfield.getValue();
					double dInitValue = NumberUtils.toDouble(initValue);
					double dNewValue = NumberUtils.toDouble(newValue);
					if (dInitValue != 0 && dInitValue != dNewValue)
						Clients.showNotification(String.format("Warning: <br>Session Fee changed from [%s] to [%s]. <br>User should not modify Session Fee after mark attendance / payment.", initValue, newValue), "warning", comp, "end_center", 5000, true); 
				}
				return true;
			}
		};
		new JxFieldChange("tkccy_name") {
			@Override
			public boolean valueChanged(JxField jxfield, String orgvalue) {
				UniLog.log1("tkccy_name valueChanged orgvalue:%s, newValue:%s", orgvalue, jxfield.getValue());
				if (curMode == JxZkBiBase.MODE_UPDATE) {
					Component comp = (Component)jxfield.getNativeObject();

					JxField ccyField = jxAdd("eaav0_tokenccy");
					Component ccyComp = (Component)ccyField.getNativeObject();
					String ccyInitValue = (String)ccyComp.getAttribute("initValue");
					String ccyNewValue = (String)ccyField.getValue();
					UniLog.log1("eaav0_tokenccy initValue:%s, newValue:%s", ccyInitValue, ccyNewValue);

					if (StringUtils.isNotBlank(ccyInitValue) && !StringUtils.equals(ccyNewValue, ccyInitValue))
						Clients.showNotification(String.format("Warning: <br>Token changed from [%s] to [%s]. <br>User should not modify Token Type after mark attendance / payment.", ccyInitValue, ccyNewValue), "warning", comp, "end_center", 5000, true); 
				}
				return true;
			}
		};

		//init dropzone
		Clients.evalJavaScript("addDropzone('div#jsDropzone',true,false,'application/pdf,"
				+ "application/msword,"
				+ "application/vnd.openxmlformats-officedocument.wordprocessingml.document,"
				+ "application/vnd.ms-excel,"
				+ "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,"
				+ "application/vnd.ms-powerpoint,"
				+ "application/vnd.openxmlformats-officedocument.presentationml.presentation',false);");
		//handle dropzone add file
		JxField f = addWithoutCheck("zkDropzone");
		((Div) f.getNativeObject()).addEventListener("onDropzoneAdd", new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log("onDropzone: getName():" + event.getName() + " " + event.getData());
				if (!StringUtils.startsWith((String)event.getData(), "dropzone-")){
					UniLog.logm(this,"ignore invalid dropzone uuid");
					return;
				}
				SessionHelper.SessionDataEx filePairSd = (SessionHelper.SessionDataEx) sessionHelper.getSessionData((String)event.getData());
				Pair filePair = filePairSd == null ? null : (Pair)filePairSd.getData();
				if (filePair == null){
					UniLog.logm(this,"ignore, invalid filePair");
					return;
				}
				UniLog.log1("got file uuid:%s name:%s size:%d", event.getData(), filePair.getLeft(), ((byte[])filePair.getRight()).length);

				final BiResult sr = getBr().getSubLink("edu.CourseDoc");
				String fileName = (String) filePair.getLeft();
				byte[] fileData = (byte[]) filePair.getRight();
				byte[] encryptData = CryptoUtil.encrypt(sessionHelper.getAESKey(), fileData, null, true);
				if (encryptData == null) {
					Messagebox.show("encrypt data fail");
					return;
				}

				int courseRg = getBr().getCellInt("eaav0_rg");
				String key = "zkbi_edu_course_" + courseRg + "_" + fileName;
				UniLog.log("upload:" + key + ",name:" + fileName + ",dataSize:" + fileData.length + ",encryptSize:" + encryptData.length);
				ByteArrayInputStream bis = new ByteArrayInputStream(encryptData);
				FilingUtil.storeFile(sessionHelper.getAgent(), null, key, key, fileName, bis);
				bis.close();

				CellCollection col = null;
				int oldRowCount = sr.getRowCount();
				for (int i = 0; i < oldRowCount; i++) {
					CellCollection c = sr.getRowCollectionV(i);
					if (col == null && StringUtils.equals(c.getCell("esavd_ofilename").toString(), fileName))
						col = c;
				}
				if (col == null) {
					listboxAddRow(Course.this, sr, jxAdd("list_edu_CourseDoc"), null, -1);
					col = sr.getRowCollectionV(oldRowCount);
				}
				col.getCell("esavd_avrg").set(courseRg);
				col.getCell("esavd_type").set(0);
				col.getCell("esavd_key").set(key);
				col.getCell("esavd_ofilename").set(fileName);
				col.getCell("esavd_filesize").set(FileUtils.byteCountToDisplaySize(fileData.length));
				col.getCell("esavd_time").set(new Date());
				col.getCell("esavd_owner").set(sessionHelper.getLoginId());
				//disable the preview button for non pdf file
				if (!StringUtils.endsWithIgnoreCase(fileName, ".pdf"))
					col.getCell("esavd_preview").setMode(Cell.VMODE_DISPONLY);
				setDirtyFlag(true);
			}
		});
		
		//set record lock
		this.setRecLock(true);
		
	}
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		UniLog.log1("bindCellCollection called");
		super.bindCellCollection(p_br, mode);
		bindDayOfWeekComponents("eaav0_sessionday", "eaav0_sessionday_cbc");
		
		//set default value
		if (mode == JxZkBiBase.MODE_ADD) {
			p_br.getCell("eaav0_sessiontime").setSilent(DateUtil.dateTimeStrToDate("09:00:00"));
			p_br.getCell("eaav0_startdate").setSilent(new Date());
			p_br.getCell("eaav0_numsession").setSilent(10);
			p_br.getCell("eaav0_sessionlen").setSilent(60);
			p_br.getCell("eaav0_alertth").setSilent(2);
			p_br.getCell("eaav0_enddate").setSilent("");
		}
		jxSetEnable("btTodayAttendance", mode != JxZkBiBase.MODE_ADD);
		jxSetVisible("zkDropzone", mode != JxZkBiBase.MODE_ADD);
		if (!sessionHelper.isAdminUser() && !sessionHelper.hasAccessRight("#edu") && !sessionHelper.hasAccessRight("#eduadmin")) {
			jxSetEnable("btPrevious", false);
			jxSetEnable("btNext", false);
			jxSetVisible("btDelSchedule",false);
			jxSetVisible("btGenSchedule",false);
			jxSetEnable("eaav0_code", false);
			jxSetEnable("eaav0_name", false);
			jxSetEnable("eaav0_status", false);
			jxSetEnable("eaav0_startdate", false);
			jxSetEnable("eaav0_enddate", false);
			jxSetEnable("eaav0_numsession", false);
			jxSetEnable("eaav0_alertth", false);
			disableDayOfWeekComponents("eaav0_sessionday_cbc");
			jxSetEnable("eaav0_sessiontime", false);
			jxSetEnable("eaav0_sessionlen", false);
			jxSetEnable("eaav0_fee", false);
			jxSetEnable("tkccy_name", false);
			jxSetEnable("esfc_name", false);
			jxSetEnable("estt_name", false);
			jxSetEnable("eaav0_allowrem", false);
			jxSetEnable("eaav0_comments", false);
		}
		else {
			BiResult brStudent = p_br.getSubLink("edu.CourseStudent");
			for (int i = 0; i < brStudent.getRowCount(); i++) {
				brStudent.fetch(false, i);
				try {
					brStudent.getCell("essbsd_status").setMode(Cell.VMODE_NORMAL);
					brStudent.getCell("essbsd_allowrem").setMode(Cell.VMODE_NORMAL);
					brStudent.getCell("essbsd_lastremdat").setMode(Cell.VMODE_NORMAL);
				} catch (CellException e) {
					e.printStackTrace();
				}
			}
		}
		
		//disable the preview button for non pdf file
		BiResult brDoc = p_br.getSubLink("edu.CourseDoc");
		for (int i = 0; i < brDoc.getRowCount(); i++) {
			brDoc.fetch(false, i);
			if (!StringUtils.endsWithIgnoreCase(brDoc.getCellString("esavd_ofilename"), ".pdf")) {
				try {
					brDoc.getCell("esavd_preview").setMode(Cell.VMODE_DISPONLY);
				} catch (CellException e) {
					e.printStackTrace();
				}
			}
		}
		
		JxField f = jxAdd("eaav0_fee");
		((Component)f.getNativeObject()).setAttribute("initValue", f.getValue());
		f = jxAdd("tkccy_name");
		((Component)f.getNativeObject()).setAttribute("initValue", f.getValue());
		f = jxAdd("eaav0_tokenccy");
		((Component)f.getNativeObject()).setAttribute("initValue", f.getValue());

		Clients.evalJavaScript("clearDropzoneFiles('div#jsDropzone')");
	}

	@Override
	protected ReturnMsg beforeAddLink(JxField fd,BiResult sr,CellCollection cl,int p_insIdx) {
		ReturnMsg rtn = super.beforeAddLink(fd, sr, cl, p_insIdx);
		if (rtn != null && !rtn.getStatus()) return rtn;
		if (fd != null && StringUtils.equalsAny(fd.getName(), "list_edu_CourseStudent", "btadd_list_edu_CourseStudent")) {
			try {
				if (sessionHelper.isAdminUser() || sessionHelper.hasAccessRight("#edu") || sessionHelper.hasAccessRight("#eduadmin")) {
					cl.getCell("essbsd_status").setMode(Cell.VMODE_NORMAL);
					cl.getCell("essbsd_allowrem").setMode(Cell.VMODE_NORMAL);
					cl.getCell("essbsd_lastremdat").setMode(Cell.VMODE_NORMAL);
				}
				cl.getCell("essbsd_allowrem").set(getBr().getCellString("eaav0_allowrem"));
			} catch (CellException e) {
				e.printStackTrace();
			}
		}
		return rtn;
	}

	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		return ListUtil.of(
			new CourseSessionGetItemProperty(p_br.getSubLink("edu.CourseSessionDet")),
			new CourseStudentGetItemProperty(p_br.getSubLink("edu.CourseStudent")),
			new CourseDocGetItemProperty(p_br.getSubLink("edu.CourseDoc"))
		);	
	}
	
	private void disableDayOfWeekComponents(String cbcFdName) {
		Hbox hbCbc = (Hbox) jxAdd(cbcFdName).getNativeObject();
		for (Component cbComp : hbCbc.queryAll("Checkbox")) {
			final Checkbox cb = (Checkbox) cbComp;
			cb.setDisabled(true);
		}
	}

	private void bindDayOfWeekComponents(final String textFdName, String cbcFdName) {
		Hbox hbCbc = (Hbox) jxAdd(cbcFdName).getNativeObject();
		//jxSetVisible(textFdName, false);
		
		final List<String> allDayList = Arrays.asList("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat");
		final List<String> curDayList = Arrays.asList(StringUtils.split(getBr().getCellString(textFdName), ",;"));
		final Checkbox[] cbs = new Checkbox[allDayList.size()];

		for (Component cbComp: hbCbc.queryAll("Checkbox")) {
			final Checkbox cb = (Checkbox) cbComp;
			final int idx = allDayList.indexOf(cb.getLabel());
			cbs[idx] = cb;
			cb.setChecked(curDayList.contains("" + idx));
			ZkBiEventListener<CheckEvent> el = (ZkBiEventListener<CheckEvent>) cb.getAttribute("checkEvent");
			if (el != null)
				cb.removeEventListener(Events.ON_CHECK, el);
			cb.addEventListener(Events.ON_CHECK, el = new ZkBiEventListener<CheckEvent>() {
				@Override
				public void onZkBiEvent(CheckEvent event) throws Exception {
					UniLog.log1("cbComp %d onZkBiEvent:%s", idx, event);
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < cbs.length; i++) {
						if (cbs[i].isChecked()) {
							if (sb.length() > 0)
								sb.append(",");
							sb.append(i);
						}
					}
					getBr().getCell(textFdName).set(sb.toString());
					setDirtyFlag(true);
				}
			});
			cb.setAttribute("checkEvent", el);
		}
	}
	
	private String buildNewCourseCode(String p_orgCode) {
		if (StringUtils.isBlank(p_orgCode)) {
			return "";
		}
		try {
			List<String> codeParts = RegUtil.parse(p_orgCode, "(.*)_([0-9]*)$");
			if (codeParts.size() == 2) {
				return codeParts.get(0) + "_" + (Integer.parseInt(codeParts.get(1)) + 1);
			}
			else {
				return p_orgCode + "_1";
			}
				
		}
		catch(Exception ex) {
			UniLog.log("cannot generate suffixId: " + ex.getMessage());
			return "";
		}
	}
	
	public static void loadHolidayData(SessionHelper sessionHelper, Map<Date, String> holidayMap) {
		if (!holidayMap.isEmpty())
			return;
		BiResult biResult = null;
		try {
			biResult = BiResultHelper.create(sessionHelper, "edu.Holiday", null, -1, null);
			while (biResult.next())
				holidayMap.put(biResult.getCellDate("eshd_date"), biResult.getCellString("eshd_desc"));
			for (Map.Entry<Date, String> entry : holidayMap.entrySet())
				UniLog.log1("holiday date:%s, desc:%s", entry.getKey(), entry.getValue());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (biResult != null)
				biResult.close();
		}
	}

	/* Customized GIPI for CourseSession */
	class CourseSessionGetItemProperty extends BiGetItemProperty {
		Template markAttendanceTemplate;
		Vector<Object> columnListFiling;
		public CourseSessionGetItemProperty(BiResult p_br) {
			super(p_br);
			UniLog.log1("CourseSessionGetItemProperty");
			markAttendanceTemplate = ((Component) getNativeComponent()).getTemplate("template_MarkAttendance");
			columnListFiling = new Vector<Object>();

			columnListFiling.add(markAttendanceTemplate);
			Vector<BiColumn> v = p_br.getListColumns();
			for (BiColumn bc : v)
				columnListFiling.add(bc);
		}

		@Override
		protected Vector getListColumns(Object p_v) {
			return columnListFiling;
		}

		@Override
		public String getColumnWidth(Object p_v, int p_col) {
			//mark attendance button width
			if (p_col == 0)
				return "32px";
			return super.getColumnWidth(p_v, p_col);
		}
		
		@Override
		public Object getColumnValueByName(final Object p_v,String p_name) {
			final CellCollection col = bigibr.getRowCollectionO(bigibr.getTrStatObj(p_v));
			if (p_name.equals("btMarkAttendance")) {
				return (new JxActionListener() {
					public void actionPerformed(JxField fd){
						UniLog.log1("Pressed for %s, %s", p_v, fd.getNativeObject());
						try {
							doMarkAttendance(false, col, fd);
						}
						catch (Exception e) {
							e.printStackTrace();
							ZkUtil.showErrMsg(e.getMessage());
						}
					}
				});
			}
			return super.getColumnValueByName(p_v, p_name);
		}

		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			if(p_ctype != GIPI_CELL_MAPPED) {
				setDirtyFlag(true);
			}
		}
	}
	
	class CourseStudentGetItemProperty extends BiGetItemProperty {
		Template sessionNoOffsetTemplate;
		Vector<Object> columnListFiling;
		public CourseStudentGetItemProperty(BiResult p_br) {
			super(p_br);
			sessionNoOffsetTemplate = ((Component) getNativeComponent()).getTemplate("template_CourseStudent_SessionNoOffset");
			columnListFiling = new Vector<Object>();

			Vector<BiColumn> v = p_br.getListColumns();
			for (BiColumn bc : v) {
				if (StringUtils.equals(bc.getLabel(), "essbsd_sessoffset"))
					columnListFiling.add(sessionNoOffsetTemplate);
				else
					columnListFiling.add(bc);
			}
		}

		@Override
		protected Vector getListColumns(Object p_v) {
			return columnListFiling;
		}

		@Override
		public Object getHeader(Object p_v, int p_col) {
			Object o = getListColumns(p_v).get(p_col);
			UniLog.log("getHeader: " + o);
			if (o instanceof BiColumn) 
				return super.getHeader(p_v, p_col);
			if (o == sessionNoOffsetTemplate) {
				BiColumn col = bigibr.getColumnByLabel("essbsd_sessoffset");
				return MapUtil.of(
					"label", col.getEngName(),
					"biColumn", col,
					"biResult", bigibr
				);
			}
			return "";
		}	

		@Override
		public String getColumnWidth(Object p_v, int p_col) {
			Object o = getListColumns(p_v).get(p_col);
			if (o == sessionNoOffsetTemplate)
				return "hflex=min";
			return super.getColumnWidth(p_v, p_col);
		}

		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			if (p_ctype != GIPI_CELL_MAPPED)
				setDirtyFlag(true);
		}
	}
	
	class CourseDocGetItemProperty extends BiGetItemProperty {
		public CourseDocGetItemProperty(final BiResult p_br) {
			super(p_br);
			/*Vector<BiColumn> colList = getListColumns(null);
			Listbox listbox = (Listbox)jxAdd("list_" + replaceViewName(p_br.getView().getName())).getNativeObject();
			Listhead listhead = (Listhead) listbox.query("Listhead");
			int i = 0;
			for (Component c : listhead.queryAll("Listheader")) {
				final Listheader lh = (Listheader) c;
				UniLog.log1("list_edu_CourseDoc lh label:%s, id:%s, idx:%d", lh.getLabel(), lh.getId(), i);
				final int idx = i - 1;
				if (i > 0 && idx < colList.size()) {
					final BiColumn bc = colList.get(idx);
					if (bc.getLabel().equals("esavd_ofilename") || bc.getLabel().equals("esavd_time")) {
						lh.setSortDirection("natural");
   						lh.setSort("auto");
   						lh.setSortAscending(new ListitemComparator());
						lh.addEventListener(Events.ON_SORT, new EventListener<SortEvent>() {
							@Override
							public void onEvent(SortEvent event) throws Exception {
								UniLog.log1("onSort event:%s, target:%s, idx:%d, colLabel:%s", event, event.getTarget(), idx, bc.getLabel());
								boolean newDesc = false;
								String oldDir = lh.getSortDirection();
								if (oldDir.equals("ascending"))
									newDesc = true;
								p_br.clearOrderBy();
								p_br.addOrderByViewList(idx + 1, newDesc);
								p_br.sort();
								event.stopPropagation();
							}
						});
					}
				}
				i++;
			}*/
		}

		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			ColumnCell bcc = (ColumnCell) p_value;
			if (p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED) {
				String owner = bcc.getCollection().getCellString("esavd_owner");
				String key = bcc.getCollection().getCellString("esavd_key");
				String fileName = bcc.getCollection().getCellString("esavd_ofilename");
				UniLog.log1("%s clicked key:%s, fileName:%s", bcc.getCellLabel(), key, fileName);
				String cellLabel = bcc.getCellLabel();
				if (cellLabel.equals("esavd_download") || cellLabel.equals("esavd_preview")) {
					if (!owner.equals(sessionHelper.getLoginId())) {
						ZkUtil.showErrMsg("User '%s' cannot access this document", owner);
						return;
					}
					if (StringUtils.isBlank(key) || StringUtils.isBlank(fileName)) {
						ZkUtil.showErrMsg("Invalid key or filename");
						return;
					}
					ByteArrayOutputStream bos = null;
					try {
						bos = new ByteArrayOutputStream();
						FilingUtilObject fuo = FilingUtil.getFile(sessionHelper.getAgent(), null, key, bos);
						bos.close();
						if (fuo == null){
							ZkUtil.showErrMsg("Cannot open %s", fileName);
							return;
						}
						byte[] decryptData = CryptoUtil.decrypt(sessionHelper.getAESKey(), bos.toByteArray(), true);
						if (cellLabel.equals("esavd_preview")) {
							if (StringUtils.endsWithIgnoreCase(fileName, ".pdf"))
								ZkUtil.showPdfDialog(((Button) jxAdd("btGenSchedule").getNativeObject()).getRoot(), sessionHelper, decryptData, fileName);
							else
								ZkUtil.showErrMsg("This feature only support pdf file");
						}
						else if (cellLabel.equals("esavd_download"))
							Filedownload.save(decryptData, null, fileName);
					} catch (Exception e) {
						e.printStackTrace();
					}
					finally {
						if (bos != null) {
							try {
								bos.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
			if (p_ctype != GIPI_CELL_MAPPED) {
				setDirtyFlag(true);
			}
		}
	};

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

	private void doMarkAttendance(final boolean todayAttendMode, final CellCollection col, JxField fd) throws Exception {
		BiResult brStudent = getBr().getSubLink("edu.CourseStudent");
		final List<Map<String, Object>> colMList = new ArrayList<Map<String, Object>>();
		final List<Map<Integer, String>> newPresentMList = new ArrayList<Map<Integer, String>>();
		Date sessionDate = null;

		//find session records
		if (todayAttendMode) {
			BiResult brSession = getBr().getSubLink("edu.CourseSessionDet");
			for (int i = 0; i < brSession.getRowCount(); i++) {
				brSession.fetch(true, i);
				Date date = brSession.getCellDate("essncs_date");
				if (DateUtil.dayBeginning(date).compareTo(DateUtil.today()) == 0) {
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("essncs_rg", brSession.getCellInt("essncs_rg"));
					map.put("essncs_date", brSession.getCellDate("essncs_date"));
					map.put("essncs_sttime", brSession.getCellDate("essncs_sttime"));
					map.put("essncs_endtime", brSession.getCellDate("essncs_endtime"));
					colMList.add(map);
					sessionDate = date;
				}
			}
		}
		else {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("essncs_rg", col.getCellInt("essncs_rg"));
			map.put("essncs_date", col.getCell("essncs_date").getDate());
			map.put("essncs_sttime", col.getCell("essncs_sttime").getDate());
			map.put("essncs_endtime", col.getCell("essncs_endtime").getDate());
			if ((Integer)map.get("essncs_rg") <= 0)
				throw new Exception("session record not found");
			colMList.add(map);
			sessionDate = (Date)map.get("essncs_date");
		}
		if (colMList.isEmpty())
			throw new Exception("session record not found");
		
		//find student records
		final List<Map<String, Object>> studentList = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < brStudent.getRowCount(); i++) {
			brStudent.fetch(true, i);
			String sdName = brStudent.getCellString("essd_name");
			String sdNo = brStudent.getCellString("essd_sdno");
			String cardNo = brStudent.getCellString("essd_cardno");
			String status = brStudent.getCellString("essbsd_status");
			Date startDate = brStudent.getCellDate("essbsd_startdate");
			Date endDate = brStudent.getCellDate("essbsd_enddate");
			if (!StringUtils.equals(status, "Cancelled")) {
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("essbsd_sdrg", brStudent.getCellInt("essbsd_sdrg"));
				map.put("essd_name", sdName);
				map.put("essd_sdno", sdNo);
				map.put("essd_cardno", cardNo);
				map.put("essbsd_status", status);
				map.put("essbsd_startdate", startDate);
				map.put("essbsd_enddate", endDate);
				map.put("description", String.format("%s (%s)", sdName, sdNo));
				//map.put("isInEnrolledList", sessionDate != null && !DateUtil.isDateNull(startDate) && !DateUtil.isDateNull(endDate) 
				//								&& sessionDate.compareTo(startDate) >= 0 && sessionDate.compareTo(endDate) <= 0);
				boolean isSubscriptioned = sessionDate != null && !DateUtil.isDateNull(startDate) && !DateUtil.isDateNull(endDate) 
												&& sessionDate.compareTo(startDate) >= 0 && sessionDate.compareTo(endDate) <= 0;
				map.put("hasAttendance", false);
				map.put("isSubscriptioned", isSubscriptioned);
				map.put("isInEnrolledList", isSubscriptioned);
				studentList.add(map);
			}
		}
		if (studentList.isEmpty())
			throw new Exception("student record not found");

		//find present records
		for (Map<String, Object> colMap : colMList) {
			int sessionRg = (Integer)colMap.get("essncs_rg");
			Map<Integer, String> oldPresentMap = findAttendancePresentDatas(sessionRg);
			Map<Integer, String> newPresentMap = new HashMap<Integer, String>();
			for (Map<String, Object> m : studentList) {
				int studentRg = (Integer)m.get("essbsd_sdrg");
				String oldStatus = oldPresentMap.get(studentRg);
				if (StringUtils.equalsAny(oldStatus, "Present", "Absent", "Leave", "Reserve")) {
					newPresentMap.put(studentRg, oldStatus);
					m.put("hasAttendance", true);
					m.put("isInEnrolledList", true);
				}
				else
					newPresentMap.put(studentRg, null);
			}
			newPresentMList.add(newPresentMap);
		}

		Collections.sort(studentList, new Comparator<Map<String, Object>>() {
			@Override
			public int compare(Map<String, Object> o1, Map<String, Object> o2) {
				boolean isInEnrolledList1 = (Boolean)o1.get("isInEnrolledList");
				boolean isInEnrolledList2 = (Boolean)o2.get("isInEnrolledList");
				if (isInEnrolledList1 && !isInEnrolledList2)
					return -1;
				else if (!isInEnrolledList1 && isInEnrolledList2)
					return 1;
				else
					return StringUtils.compare((String)o1.get("description"), (String)o2.get("description"));
			}
		});

		//build dialog
		final Vlayout vl = new Vlayout();
		vl.setVflex("1");

		//dialog header
		final GridHelper ghh = new GridHelper(2);
		ghh.getColumn(0).setWidth("110");
		ghh.getColumn(1).setHflex("1");
		final SimpleDateFormat sddf = new SimpleDateFormat("yyyy/MM/dd");
		final SimpleDateFormat stdf = new SimpleDateFormat("HH:mm:ss");
		ghh.addRow(new Label("Course Code:"), new Label(getBr().getCellString("eaav0_code")));
		ghh.addRow(new Label("Course Name:"), new Label(getBr().getCellString("eaav0_name")));
		ghh.addRow(new Label("Session Date:"), new Label(sddf.format(sessionDate)));
		
		//dialog tabbox
		final Tabbox tabbox = new Tabbox();
		tabbox.setVflex("1");
		tabbox.appendChild(new Tabs() {{
			for (Map<String, Object> map : colMList) {
				String startTime = stdf.format((Date)map.get("essncs_sttime"));
				String endTime = stdf.format((Date)map.get("essncs_endtime"));
				appendChild(new Tab(startTime + " - " + endTime));
			}
		}});
		tabbox.appendChild(new Tabpanels() { 
			void addHeaderRow(GridHelper gh, boolean isInEnrolledList) { 
				gh.addRow(new Label(isInEnrolledList ? "Enrolled Student List" : "Non-Enrolled Student List") {{
					setStyle("color:white;font-size:16px !important");
				}});
				Row row = gh.getRow(gh.getRows().getChildren().size() - 1);
				row.setSpans("2");
				row.setStyle("background-color:#779cb1 !important");
			} 
			void addSpaceRow(GridHelper gh) { 
				gh.addRow(new Label());
				Row row = gh.getRow(gh.getRows().getChildren().size() - 1);
				row.setSpans("2");
				row.setHeight("16px");
				row.setStyle("background-color:#f2f2f2 !important");
			}
			void addNoRecordRow(GridHelper gh) { 
				gh.addRow(new Label("No Record"));
				Row row = gh.getRow(gh.getRows().getChildren().size() - 1);
				row.setSpans("2");
			}
			{
			for (final Map<Integer, String> newPresentMap : newPresentMList) {
				final GridHelper gh = new GridHelper(2);
				gh.setVflex("1");
				gh.getColumn(0).setHflex("1");
				gh.getColumn(1).setWidth("320px");
				if (studentList.isEmpty() || !(Boolean)studentList.get(0).get("isInEnrolledList")) {
					addHeaderRow(gh, true);
					addNoRecordRow(gh);
					addSpaceRow(gh);
				}
				for (int i = 0; i < studentList.size(); i++) {
					Map<String, Object> t = studentList.get(i);
					boolean isInEnrolledList = (Boolean)t.get("isInEnrolledList");
					if (i == 0 || (isInEnrolledList != (Boolean)studentList.get(i - 1).get("isInEnrolledList"))) {
						if (i > 0)
							addSpaceRow(gh);
						addHeaderRow(gh, isInEnrolledList);
					}
					final int studentRg = (Integer)t.get("essbsd_sdrg");
					final String description = (String)t.get("description");
					final Radio rdPresent = new Radio("Present");
					final Radio rdAbsent = new Radio("Absent");
					final Radio rdLeave = new Radio("Leave");
					final Radio rdReserve = new Radio("Reserve");
					final Radio rdNa = new Radio("N/A");
					gh.addRow(new Label(description), new Radiogroup() {{
						appendChild(rdPresent);
						appendChild(rdAbsent);
						appendChild(rdLeave);
						appendChild(rdReserve);
						appendChild(rdNa);
						addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
							@Override
							public void onZkBiEvent(CheckEvent event) throws Exception {
								UniLog.log1("event:%s", event);
								if (event.getTarget() == rdPresent || event.getTarget() == rdAbsent || event.getTarget() == rdLeave || event.getTarget() == rdReserve)
									newPresentMap.put(studentRg, ((Radio)event.getTarget()).getLabel());
								else
									newPresentMap.put(studentRg, null);
							}
						});
					}});
					String status = newPresentMap.get(studentRg);
					if (StringUtils.equals(status, "Present"))
						rdPresent.setChecked(true);
					else if (StringUtils.equals(status, "Absent"))
						rdAbsent.setChecked(true);
					else if (StringUtils.equals(status, "Leave"))
						rdLeave.setChecked(true);
					else if (StringUtils.equals(status, "Reserve"))
						rdReserve.setChecked(true);
					else
						rdNa.setChecked(true);
				}
				if (studentList.isEmpty() || (Boolean)studentList.get(studentList.size() - 1).get("isInEnrolledList")) {
					addSpaceRow(gh);
					addHeaderRow(gh, false);
					addNoRecordRow(gh);
				}
				appendChild(new Tabpanel() {{
					appendChild(gh);
				}});
			}
		}});
		
		vl.appendChild(ghh);
		vl.appendChild(tabbox);

		final MessageboxDlg dlg = ZkUtil.buildMessageboxDlg("Mark Attendance", 
			vl, 
			new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
				((Component)fd.getNativeObject()).getRoot(),
				new EventListener<Messagebox.ClickEvent>(){
					@Override
					public void onEvent(ClickEvent event) throws Exception {
						if (event.getButton() == null)
							return;
						switch (event.getButton()) {
						case OK:
							try {
								Map<String, Integer> t = markAttendance(colMList, studentList, newPresentMList);
								List<String> sl = new ArrayList<String>();
								if (t.get("add") > 0)
									sl.add(String.format("%d record inserted", t.get("add")));
								if (t.get("update") > 0)
									sl.add(String.format("%d record updated", t.get("update")));
								if (t.get("remove") > 0)
									sl.add(String.format("%d record removed", t.get("remove")));
								if (t.get("skip") > 0)
									sl.add(String.format("%d record ignored", t.get("skip")));
								if (sl.isEmpty())
									sl.add("0 record inserted");
								ZkUtil.msg(StringUtils.join(sl, ", "));
							}
							catch (Exception e) {
								ZkUtil.showErrMsg(e.getMessage());
							}
							break;
						default:
							break;
						}
				}}
		);

		final Grid gh = (Grid) tabbox.query("grid");
		ghh.addEventListener(Events.ON_AFTER_SIZE, new EventListener<AfterSizeEvent>() {
			@Override
			public void onEvent(AfterSizeEvent event) throws Exception {
				UniLog.log1("event:%s, width:%d, height:%d, dlgHeight:%s", event, event.getWidth(), event.getHeight(), dlg.getHeight());
				Integer oldHeaderHeight = (Integer) ghh.getAttribute("headerHeight");
				Integer gridHeight = (Integer) gh.getAttribute("gridHeight");
				int newHeaderHeight = event.getHeight();
				if (oldHeaderHeight == null || newHeaderHeight != oldHeaderHeight) {
					if (gridHeight != null) {
						String newDlgHeight = ((gridHeight + 2) + 32 + 43 + (newHeaderHeight + 7) + 45 + 16) + "px";//32: titlebar, 43: bottombar, 45: tabbox exclude grid, 16: other padding/border...
						UniLog.log1("newDlgHeight:%s, gridHeight:%d", newDlgHeight, gridHeight);
						dlg.setHeight(newDlgHeight);
					}
					ghh.setAttribute("headerHeight", newHeaderHeight);
				}
			}
		});
		gh.addEventListener(Events.ON_AFTER_SIZE, new EventListener<AfterSizeEvent>() {
			@Override
			public void onEvent(AfterSizeEvent event) throws Exception {
				UniLog.log1("event:%s, width:%d, height:%d, dlgHeight:%s", event, event.getWidth(), event.getHeight(), dlg.getHeight());
				//dialog setHeight
				Integer oldGridHeight = (Integer) gh.getAttribute("gridHeight");
				Integer headerHeight = (Integer) ghh.getAttribute("headerHeight");
				int newGridHeight = event.getHeight();
				if (oldGridHeight == null || newGridHeight != oldGridHeight) {
					if (headerHeight != null) {
						String newDlgHeight = ((newGridHeight + 2) + 32 + 43 + (headerHeight + 7) + 45 + 16) + "px";//32: titlebar, 43: bottombar, 45: tabbox exclude grid, 16: other padding/border...
						UniLog.log1("newDlgHeight:%s, headerHeight:%d", newDlgHeight, headerHeight);
						dlg.setHeight(newDlgHeight);
					}
					gh.setAttribute("gridHeight", newGridHeight);
				}
			}
		});
		dlg.setWidth("630px");
		//dlg.setHeight((32 + 43 + 16 + 32 * gh.getRows().getChildren().size()) + "px");
		dlg.setStyle("max-width:100%;max-height:" + (sessionHelper.isMobile() ? "100" : "95") + "%");
		dlg.doHighlighted();
	}
	
	private Map<String, Integer> markAttendance(List<Map<String, Object>> colMList, List<Map<String, Object>> studentList, List<Map<Integer, String>> newPresentMList) throws Exception {
		BiResult brAttendance = null;
		int addCount = 0;
		int updateCount = 0;
		int removeCount = 0;
		int skipCount = 0;
		try {
			int courseRg = getBr().getCellInt("eaav0_rg");
			double sessionFee = getBr().getCellDouble("eaav0_fee");
			String tokenCcy = getBr().getCellString("eaav0_tokenccy");

			brAttendance = sessionHelper.newBiResult("edu.Attendance");
			brAttendance.beginWork();

			RpcClient rpc = brAttendance.getSelectUtil().getRpcClient();
			Set<String> deductTokenStatusList = Sets.newHashSet("Present", "Absent");

			for (int i = 0; i < colMList.size(); i++) {
				Map<String, Object> colMap = colMList.get(i);
				Map<Integer, String> newPresentMap = newPresentMList.get(i);

				int sessionRg = (Integer)colMap.get("essncs_rg");
				Date sessionDate = (Date)colMap.get("essncs_date");
				Date sessionStartTime = (Date)colMap.get("essncs_sttime");
				Date sessionEndTime = (Date)colMap.get("essncs_endtime");
				Date startDateTime = Student.unionDateTime(sessionDate, sessionStartTime);
				Date endDateTime = Student.unionDateTime(sessionDate, sessionEndTime);

				Map<Integer, String> oldPresentMap = findAttendancePresentDatas(sessionRg);
				
				Map<Integer, Double> rm = new HashMap<Integer, Double>();
				Map<Integer, String> cardNoMap = new HashMap<Integer, String>();

				for (Map<String, Object> m : studentList) {
					int studentRg = (Integer)m.get("essbsd_sdrg");
					String cardNo = (String)m.get("essd_cardno");
					String oldStatus = oldPresentMap.get(studentRg);
					String newStatus = newPresentMap.get(studentRg);
					if (oldPresentMap.containsKey(studentRg)) {
						if (newStatus == null) {
							//remove attendance record
							brAttendance.getSelectUtil().executeUpdate("delete from esattendance", 
								new Wherecl().andUniop("esat_snrg", "=", sessionRg)
											.andUniop("esat_attype", "=", "SD")
											.andUniop("esat_atrg", "=", studentRg).stripAnd()
							);
							removeCount++;
							rm.put(studentRg, 0.0);
							cardNoMap.put(studentRg, cardNo);
						}
						else if (!StringUtils.equals(oldStatus, newStatus)) {
							//update attendance status
							brAttendance.getSelectUtil().executeUpdate("update esattendance set esat_status = ? where esat_snrg = ? and esat_attype = 'SD' and esat_atrg = ?", 
								new Wherecl().appendArgument(newStatus)
											.appendArgument(sessionRg)
											.appendArgument(studentRg)
							);
							updateCount++;
							if (deductTokenStatusList.contains(newStatus) && !deductTokenStatusList.contains(oldStatus)) {
								rm.put(studentRg, sessionFee);
								cardNoMap.put(studentRg, cardNo);
							}
							else if (!deductTokenStatusList.contains(newStatus) && deductTokenStatusList.contains(oldStatus)) {
								rm.put(studentRg, 0.0);
								cardNoMap.put(studentRg, cardNo);
							}
						}
						else {
							//ignore
							skipCount++;
						}
					}
					else {
						if (newStatus != null) {
							//add attendance record
							brAttendance.clearCurrentRec();
							brAttendance.getCell("esatsd_snrg").set(sessionRg);
							brAttendance.getCell("esatsd_attype").set("SD");
							brAttendance.getCell("esatsd_atrg").set(studentRg);
							brAttendance.getCell("esatsd_status").set(newStatus);
							brAttendance.getCell("esatsd_sttime").set(startDateTime);
							brAttendance.getCell("esatsd_endtime").set(endDateTime);
							ReturnMsg rtn = brAttendance.addCurrent();
							if (!rtn.getStatus())
								throw new Exception(rtn.getMsg());
							addCount++;
							if (deductTokenStatusList.contains(newStatus)) {
								rm.put(studentRg, sessionFee);
								cardNoMap.put(studentRg, cardNo);
							}
						}
						else {
							//ignore
							skipCount++;
						}
					}
				}
				
				//rpccall
				if (!rm.isEmpty()) {
					/*Vector args = new Vector();
					args.add(courseRg);
					args.add(sessionRg);
					for (Map.Entry<Integer, Double> entry : rm.entrySet()) {
						int studentRg = entry.getKey();
						double fee = entry.getValue();
						args.add(studentRg);
						args.add(tokenCcy);
						args.add(fee);
					}
					Value value = rpc.callSegment("token_addUpdateCourseAttendMulti", args);
					if (value == null)
						throw new Exception("rpccall failed");
					if (!StringUtils.startsWith(value.toString(), "OK"))
						throw new Exception(StringUtils.startsWith(value.toString(), "FAIL") ? value.toString().substring(4) : value.toString());*/
					for (Map.Entry<Integer, Double> entry : rm.entrySet()) {
						int studentRg = entry.getKey();
						double fee = entry.getValue();
						Vector args = new Vector();
						args.add(studentRg);
						args.add(sessionRg);
						args.add(courseRg);
						args.add(tokenCcy);
						args.add(fee);
						Value value = rpc.callSegment("token_addUpdateCourseAttend", args);
						if (value == null)
							throw new Exception("rpccall failed");
						if (!StringUtils.startsWith(value.toString(), "OK"))
							throw new Exception(StringUtils.startsWith(value.toString(), "FAIL") ? value.toString().substring(4) : value.toString());
						//need to call the ProcessScanLog.setCSMapDirty() when attendance updated
						ProcessScanLog.setCSMapDirty(cardNoMap.get(studentRg));
					}
				}
			}

			brAttendance.commitWork();
		}
		catch (Exception e) {
			e.printStackTrace();
			if (brAttendance != null)
				brAttendance.rollbackWork();
			throw e;
		}
		finally {
			if (brAttendance != null)
				brAttendance.close();
		}
		return MapUtil.of("add", addCount, "update", updateCount, "remove", removeCount, "skip", skipCount);
	}
	
	@Override
	protected ReturnMsg validateAddUpdate(final JxField fd, int mode) {
		//checkthe state variable
		if (confirmSaveFlag) {
			confirmSaveFlag = false;
			return ReturnMsg.defaultOk;
		}
		
		
		//TODO place the validation code here
		double sessionFee = getBr().getCellDouble("eaav0_fee");
		String tokenCcy = getBr().getCellString("eaav0_tokenccy");
		String msg = Student.joinString("\n", 
				StringUtils.isBlank(tokenCcy) ? "Currency is blank." : "", 
				sessionFee == 0 ? "Fee is 0." : "",
				sessionFee < 0 ? "Fee is negative." : "");
		
		if (StringUtils.isNotBlank(msg)) {
			//display a confirmation dialog if needed
			if (mode == MODE_ADD) {
				msg += "\nAre you sure to add new record?";
			}
			else if (mode == MODE_UPDATE) {
				msg += "\nAre you sure to update it?";
			}
			else {
				//not supported mode				
				return ReturnMsg.defaultOk;
			}

			ZkBiMsgbox.show(ZkBiMsgbox.Type.warning, msg, new String[]{"Yes","No"},new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
					if (StringUtils.equals(btn.getName(), "Yes")){
						Events.sendEvent(Events.ON_CLICK, (Component)fd.getNativeObject(), null);
						confirmSaveFlag = true; //update the state
					}
				}
			});				
			return ReturnMsg.defaultFail; //before dialog display, stop the original add/update action
		}
		else
			return ReturnMsg.defaultOk;
	}
}