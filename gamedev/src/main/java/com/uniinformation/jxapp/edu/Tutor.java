package com.uniinformation.jxapp.edu;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Foot;
import org.zkoss.zul.Footer;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Space;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.impl.MessageboxDlg;

import com.google.common.collect.Sets;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.poi.ExcelPoi;
import com.uniinformation.webcore.GridHelper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiTranslateHelper;

public class Tutor extends JxZkBiBase {
	@Override
	public void afterBind() {
		UniLog.log1("afterBind called");
		super.afterBind();
		jxAdd("btSetLoginPassword").addActionListener(new JxActionListener() {
			@Override
			public void actionPerformed(JxField field) {
				try {
					int tutorRg = getBr().getCellInt("estt_rg");
					if (tutorRg <= 0) {
						UniLog.log1("Invalid Tutor rg:%d", tutorRg);
						return;
					}
					UniLog.log1("tutorRg:%d", tutorRg);
					String loginId = getBr().getCellString("estt_ttno").toLowerCase();
					String userName = getBr().getCellString("estt_name");
					String lvlName = getBr().getCellString("esmgl_name");
					String accessKey;
					if (StringUtils.equals(lvlName, "Manager"))
						accessKey = "#edu";
					else if (StringUtils.equals(lvlName, "Admin"))
						accessKey = "#eduadmin";
					else
						accessKey = "#tutor";
					new Student.DoSetLoginPassword(sessionHelper, loginId, userName, "", accessKey, (Button)field.getNativeObject());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		jxAdd("btUpdateAttendance").addActionListener(new JxActionListener() {
			@Override
			public void actionPerformed(JxField field) {
				try {
					int tutorRg = getBr().getCellInt("estt_rg");
					if (tutorRg <= 0) {
						UniLog.log1("Invalid Tutor rg:%d", tutorRg);
						return;
					}
					UniLog.log1("tutorRg:%d", tutorRg);
					doUpdateAttendance(sessionHelper, "Update Attendance", (Button) field.getNativeObject(), Tutor.this, curMode, Sets.newHashSet(tutorRg));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		jxAdd("btCalendar").addActionListener(new JxActionListener(){
			@Override
			public void actionPerformed(JxField field) {
				try {
					JSONObject jo = new JSONObject();
					jo.put("tutorCode", getBr().getCellString("estt_ttno"));
					//String key = sessionHelper.putOneTimeData(jo);
					String key = sessionHelper.putOneTimeData(jo,true);
					//ZkUtil.js("openNewTab('%s')", "zkbiloader.html?action=browse&viewid=ZkBiCalendar&page_id=ZkBiCalendar_01&zul=eduCalendar.zul&composer=edu.ZkBiComposerCalendar&load=fc&load=pickr&querycondition=" + key);
					ZkUtil.js("openNewTab('%s')", "zkbiloader.html?action=browse&viewid=ZkBiCalendar&page_id=ZkBiCalendar_01&zul=eduCalendar.zul&composer=edu.ZkBiComposerCalendar&load=fc&load=pickr&sidemenu=N&querycondition=" + key);
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		jxAdd("btCourseSummary").addActionListener(new JxActionListener() {
			@Override
			public void actionPerformed(JxField field) {
				try {
					int tutorRg = getBr().getCellInt("estt_rg");
					if (tutorRg <= 0) {
						UniLog.log1("Invalid Tutor rg:%d", tutorRg);
						return;
					}
					UniLog.log1("tutorRg:%d", tutorRg);
					showCourseSummary(sessionHelper, (Button) field.getNativeObject(), tutorRg, getBr().getCellString("estt_ttno"), getBr().getCellString("estt_name"));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		UniLog.log1("bindCellCollection called");
		super.bindCellCollection(p_br, mode);
		jxSetEnable("btUpdateAttendance", mode != JxZkBiBase.MODE_ADD);
		jxSetEnable("btSetLoginPassword", mode != JxZkBiBase.MODE_ADD);
		jxSetEnable("btCalendar", mode != JxZkBiBase.MODE_ADD);
		if (!sessionHelper.isAdminUser() && !sessionHelper.hasAccessRight("#edu")) {
			if (!sessionHelper.hasAccessRight("#eduadmin"))
				jxSetVisible("btCourseSummary", false);
			jxSetVisible("btUpdateAttendance", false);
			jxSetEnable("btPrevious", false);
			jxSetEnable("btNext", false);
			try {
				jxAdd("estt_idtype").setFieldMode(JxField.FMODE_DISPONLY);
				jxAdd("estt_idno").setFieldMode(JxField.FMODE_DISPONLY);
				jxAdd("estt_name").setFieldMode(JxField.FMODE_DISPONLY);
				jxAdd("estt_status").setFieldMode(JxField.FMODE_DISPONLY);
				jxAdd("esmgl_name").setFieldMode(JxField.FMODE_DISPONLY);
			} catch (CellException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		//handle open course record button
		return ListUtil.of(new BiGetItemProperty(p_br.getSubLink("edu.TutorCourse")) {
			@Override
			public void onValueChanged(Object p_value,int p_ctype) {
				ColumnCell bcc = (ColumnCell) p_value;
				if(p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED && bcc.getCellLabel().equals("eaav0_tocourse")){
					int avrg = bcc.getCollection().getCellInt("eaav0_rg");
					UniLog.log1("%s clicked avrg:%d", bcc.getCellLabel(), avrg);
					if (avrg <= 0) {
						UniLog.log1("invalid avrg");
						return;
					}
					try {
						JSONObject jo = new JSONObject();
						JSONArray ja = new JSONArray();
						BiView pov = getBr().getView().getSchema().getViewByName("edu.Course");
						ja.put(pov.getTable().getName());
						jo.put("tablist", ja);
						jo.put("wherestr", "eaav0_rg = " + avrg);
						//String key = sessionHelper.putOneTimeData(jo);
						String key = sessionHelper.putOneTimeData(jo,true);
						//ZkUtil.js("openNewTab('%s')", "zkbiloader.html?action=update&viewid=edu.Course&page_id=course_01&zul=zkbiloader.zul&prefix=zkbi&composer=edu.ZkBiComposerCourse&closetab=Y&querycondition="+key);
						ZkUtil.js("openNewTab('%s')", "zkbiloader.html?action=update&viewid=edu.Course&page_id=course_01&zul=zkbiloader.zul&prefix=zkbi&composer=edu.ZkBiComposerCourse&closetab=Y&sidemenu=N&querycondition="+key);
					}
					catch(Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});	
	}

	public static void doUpdateAttendance(final SessionHelper sessionHelper, String dialogTitle, final Component comp, final JxZkBiBase biBase, final int curMode, final Set<Integer> tutorRgList) {
		final GridHelper gh = new GridHelper(2);
		final Radiogroup rgAttendance = new Radiogroup();
		final Radio rdPresentAttendance = new Radio("Present");
		final Radio rdAbsentAttendance = new Radio("Absent");
		final Radio rdLeaveAttendance = new Radio("Leave");
		final Radio rdRemoveAttendance = new Radio("Remove");

		final Radiogroup rgDateRange = new Radiogroup();
		final Radio rdDateRange = new Radio("Date");
		final Radio rdDateTimeRange = new Radio("Date Time");
		final Datebox dbDateFrom = new Datebox();
		final Datebox dbDateTo = new Datebox();
		final Datebox dbDateTimeFrom = new Datebox();
		final Datebox dbDateTimeTo = new Datebox();
		
		rgAttendance.appendChild(rdPresentAttendance);
		rgAttendance.appendChild(rdAbsentAttendance);
		rgAttendance.appendChild(rdLeaveAttendance);
		rgAttendance.appendChild(rdRemoveAttendance);
		
		dbDateFrom.setFormat("yyyy/MM/dd");
		dbDateTo.setFormat("yyyy/MM/dd");
		dbDateTimeFrom.setFormat("yyyy/MM/dd HH:mm:ss");
		dbDateTimeTo.setFormat("yyyy/MM/dd HH:mm:ss");
		dbDateFrom.setWidth("120px");
		dbDateTo.setWidth("120px");
		dbDateTimeFrom.setWidth("180px");
		dbDateTimeTo.setWidth("180px");
		rdDateRange.setRadiogroup(rgDateRange);
		rdDateTimeRange.setRadiogroup(rgDateRange);
		rdPresentAttendance.setChecked(true);
		rdDateRange.setChecked(true);
		dbDateTimeFrom.setDisabled(true);
		dbDateTimeTo.setDisabled(true);

		Calendar cal = Calendar.getInstance();
		cal.setTime(new Date());
		cal.set(Calendar.SECOND, 0);
		dbDateFrom.setValue(cal.getTime());
		dbDateTo.setValue(cal.getTime());
		dbDateTimeFrom.setValue(cal.getTime());
		dbDateTimeTo.setValue(Student.dateTimeAfterHour(cal.getTime(), 3)); //after 3 hour
		
		gh.setWidth("100%");
		gh.getColumn(0).setWidth("100px");
		gh.getColumn(0).setAlign("left");
		gh.getColumn(1).setHflex("1");
		gh.addRow(rgAttendance);
		gh.addRow(rgDateRange);
		gh.addRow(rdDateRange, new Hlayout() {{
			appendChild(dbDateFrom);
			appendChild(new Label("to"));
			appendChild(dbDateTo);
			setStyle("white-space: normal;");
		}});
		gh.addRow(rdDateTimeRange, new Hlayout() {{
			appendChild(dbDateTimeFrom);
			appendChild(new Label("to"));
			appendChild(dbDateTimeTo);
			setStyle("white-space: normal;");
		}});
		gh.getRow(0).setSpans("2");
		gh.getRow(1).setSpans("2");
		gh.getRow(1).setHeight("24px");

		rgDateRange.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
			@Override
			public void onZkBiEvent(CheckEvent event) throws Exception {
				UniLog.log1("event:%s", event);
				boolean b = event.getTarget() == rdDateTimeRange;
				dbDateFrom.setDisabled(b);
				dbDateTo.setDisabled(b);
				dbDateTimeFrom.setDisabled(!b);
				dbDateTimeTo.setDisabled(!b);
			}
		});

		//When user change date0, need to auto update date1
		dbDateFrom.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s, dbDateFrom:%s", event, dbDateFrom.getValue());
				dbDateTo.setValue(dbDateFrom.getValue());
			}
		});
		dbDateTimeFrom.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s, dbDateTimeFrom:%s", event, dbDateTimeFrom.getValue());
				Date dtFrom = dbDateTimeFrom.getValue();
				dbDateTimeTo.setValue(dtFrom != null ? Student.dateTimeAfterHour(dtFrom, 3) : null);
			}
		});
		
		MessageboxDlg dlg = ZkUtil.buildMessageboxDlg(dialogTitle, 
			gh, 
    		new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
    			comp.getRoot(),
  				new EventListener<Messagebox.ClickEvent>(){
   					@Override
   					public void onEvent(ClickEvent event) throws Exception {
   						if (event.getButton() == null)
   							return;
   						switch (event.getButton()) {
   						case OK:
   							try {
   								String newStatus = rgAttendance.getSelectedItem().getLabel();
   								if (newStatus.equals("Remove"))
   									newStatus = null;
   								Date dateFrom = dbDateFrom.getValue();
   								Date dateTo = dbDateTo.getValue();
  								Date dateTimeFrom = dbDateTimeFrom.getValue();
   								Date dateTimeTo = dbDateTimeTo.getValue();
   								if (rdDateTimeRange.isChecked()) {
   									if (dateTimeFrom == null || dateTimeTo == null || dateTimeFrom.getTime() >= dateTimeTo.getTime()) {
										Student.showErrMsg(gh, "Invalid date time");
										event.stopPropagation();
										return;
									}
   								}
   								else {
   									if (dateFrom == null || dateTo == null || dateFrom.getTime() > dateTo.getTime()) {
										Student.showErrMsg(gh, "Invalid date");
										event.stopPropagation();
										return;
   									}
   									SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
   									SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd 00:00:00");
   									dateTimeFrom = sdf.parse(sdf1.format(dateFrom));
   									sdf1 = new SimpleDateFormat("yyyy/MM/dd 23:59:59");
   									dateTimeTo = sdf.parse(sdf1.format(dateTo));
   								}
  								UniLog.log1("dateTimeFrom:%s, dateTimeTo:%s", dateTimeFrom, dateTimeTo);

   								int p = updateAttendance(sessionHelper, tutorRgList, newStatus, dateTimeFrom, dateTimeTo);
  								ZkUtil.msg("%d record updated", p);
   							}
   							catch (Exception e) {
   								ZkUtil.showErrMsg(e.getMessage());
   							}
 							if (biBase != null) {
 								biBase.getBr().refetchCurrent();
								biBase.bindCellCollection(biBase.getBr(), curMode);
 							}
 							else {
 								Button btCancel = (Button) comp.getParent().query("#" + comp.getId() + "BatchCancel");
								if (btCancel != null)
									Events.echoEvent(Events.ON_CLICK, btCancel, null);
 							}
   							break;
   						default:
   							break;
   						}
				}}
    	);
		dlg.setWidth("520px");
		dlg.setStyle("max-width:100%");
      	dlg.doHighlighted();
	}

	private static int updateAttendance(SessionHelper sessionHelper, Set<Integer> tutorRgList, String newStatus, Date pStartDateTime, Date pEndDateTime) throws Exception {
		BiResult brTutorAttendance = null;
		BiResult brAttendance = null;
		int updateCount = 0;
		try {
			brTutorAttendance = sessionHelper.newBiResult("edu.TutorAttendance");
			brAttendance = sessionHelper.newBiResult("edu.Attendance");

			SimpleDateFormat dsdf = new SimpleDateFormat("yyyy/MM/dd");

			//find tutor session records
			ReturnMsg rtn;
			Map<Integer, Map<String, Object>> sessionMap = new LinkedHashMap<Integer, Map<String, Object>>();
			for (int tutorRg : tutorRgList) {
				brTutorAttendance.addCustomCondition(String.format("esattt_atrg = %d and essn_date between '%s' and '%s'", 
						tutorRg,
						dsdf.format(pStartDateTime),
						dsdf.format(pEndDateTime)
						));
				if ((rtn = brTutorAttendance.query(true, false)).getStatus()) {
					while (brTutorAttendance.next()) {
						int courseRg = brTutorAttendance.getCellInt("essn_avrg");
						int sessionRg = brTutorAttendance.getCellInt("esattt_snrg");
						Date sessionDate = brTutorAttendance.getCellDate("essn_date");
						Date sessionStartTime = brTutorAttendance.getCellDate("essn_sttime");
						Date sessionEndTime = brTutorAttendance.getCellDate("essn_endtime");
						String status = brTutorAttendance.getCellString("essn_status");
						String remark = brTutorAttendance.getCellString("essn_name");
						String attStatus = brTutorAttendance.getCellString("esattt_status");
						Date attStartDateTime = brTutorAttendance.getCellDate("esattt_sttime");
						Date attEndDateTime = brTutorAttendance.getCellDate("esattt_endtime");
						UniLog.log1("found session courseRg:%d, sessionRg:%d, sessionDate:%s, startTime:%s, endTime:%s, remark:%s, status:%s", courseRg, sessionRg, sessionDate, sessionStartTime, sessionEndTime, remark, status);
						
						Date sessionStartDateTime = Student.unionDateTime(sessionDate, sessionStartTime);
						Date sessionEndDateTime = Student.unionDateTime(sessionDate, sessionEndTime);
						UniLog.log1("startDateTime:%s, endDateTime:%s, pStartDateTime:%s, pEndDateTime:%s", sessionStartDateTime, sessionEndDateTime, pStartDateTime, pEndDateTime);
						if (sessionStartDateTime.compareTo(pEndDateTime) < 0 && sessionEndDateTime.compareTo(pStartDateTime) > 0) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("courseRg", courseRg);
							map.put("tutorRg", tutorRg);
							map.put("sessionDate", sessionDate);
							map.put("sessionStartDateTime", sessionStartDateTime);
							map.put("sessionEndDateTime", sessionEndDateTime);
							map.put("attStatus", attStatus);
							map.put("attStartDateTime", attStartDateTime);
							map.put("attEndDateTime", attEndDateTime);
							sessionMap.put(sessionRg, map);
							UniLog.log("mSessionMap put");
						}
					}
				}
				else
					throw new Exception(rtn.getMsg());
			}

			brAttendance.beginWork();
			for (Map.Entry<Integer, Map<String, Object>> entry : sessionMap.entrySet()) {
				int sessionRg = entry.getKey();
				Map<String, Object> map = entry.getValue();
				int tutorRg = (Integer)map.get("tutorRg");
				String attStatus = "";
				int attStartTime = 0;
				int attEndTime = 0;
				if (newStatus != null) {
					attStatus = newStatus;
					attStartTime = (int)(((Date)map.get("sessionStartDateTime")).getTime() / 1000);
					attEndTime = (int)(((Date)map.get("sessionEndDateTime")).getTime() / 1000);
				}
				brAttendance.getSelectUtil().executeUpdate("update esattendance set esat_status = ?, esst_sttime = ?, esst_endtime = ? where esat_snrg = ? and esat_attype = 'TT' and esat_atrg = ?",
						new Wherecl()
							.appendArgument(attStatus)
							.appendArgument(attStartTime)
							.appendArgument(attEndTime)
							.appendArgument(sessionRg)
							.appendArgument(tutorRg)
							);
				updateCount++;
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
			if (brTutorAttendance != null)
				brTutorAttendance.close();
			if (brAttendance != null)
				brAttendance.close();
		}
		return updateCount;
	}
	
	public static void showCourseSummary(final SessionHelper sessionHelper, Component comp, final Integer pTutorRg, final String pTutorCode, final String pTutorName) {
		final Vlayout vl = new Vlayout();
		final Listbox s2Tutor = new Listbox();
		final Datebox dbFrom = new Datebox(LocalDateTime.now().minusMonths(1).plusDays(1));
		final Datebox dbTo = new Datebox(LocalDateTime.now());
		final Tabbox tabbox = new Tabbox();
		final Tabs tabs = new Tabs();
		final Tabpanels tabpanels = new Tabpanels();
		dbFrom.setFormat("yyyy/MM/dd");
		dbTo.setFormat("yyyy/MM/dd");

		s2Tutor.setMold("select");
		s2Tutor.setMultiple(true);
		s2Tutor.setAttribute("placeholder", "Please choose tutor");
		s2Tutor.setAttribute("select2-enable", "Y");
		s2Tutor.setAttribute("select2-multiple", "Y");
		s2Tutor.setHflex("1");

		tabbox.setVflex("1");
		tabbox.appendChild(tabs);
		tabbox.appendChild(tabpanels);
		
		final String labelStyle = sessionHelper.isMobile() ? "width:38px;display:inline-block;" 
									: "padding-left:20px;width:58px;display:inline-block;";
		final Div s2TutorDiv = new Div() {{
			appendChild(s2Tutor);
			setHflex("1");
			setStyle("display:flex");
		}};
		vl.appendChild(new Hlayout() {{
			appendChild(new Label("Tutor: ") {{ setStyle(labelStyle); }});
			appendChild(s2TutorDiv);
		}});
		vl.appendChild(new Hlayout() {{
			appendChild(new Label("From: ") {{ setStyle(labelStyle); }});
			appendChild(dbFrom);
		}});
		vl.appendChild(new Hlayout() {{
			appendChild(new Label("To: ") {{ setStyle(labelStyle); }});
			appendChild(dbTo);
		}});
		final Div buttonsDiv = new Div() {{
			setId("buttons");
			setStyle("display:flex;display:-webkit-flex;"
					+ "flex-wrap:wrap;-webkit-wrap:wrap;"
					+ (sessionHelper.isMobile() ? "padding:0 5px 5px 0px;" : "padding:0 5px 5px 58px;"));
			setAttribute("button.sclass", "zkbi-messagebox-button");
		}};
		vl.appendChild(new Hlayout() {{
			appendChild(buttonsDiv);
		}});
		vl.appendChild(tabbox);

		tabs.appendChild(new Tab());
		tabpanels.appendChild(new Tabpanel() {{
			final Map<String, Component> gridMap = buildCourseSummaryGrid();
			appendChild(new Div() {{
				appendChild(gridMap.get("gh"));
				setStyle("overflow-x:auto");
				setVflex("1");
			}});
			setAttribute("gridMap", gridMap);
		}});
		
		if (pTutorRg != null) {
			s2Tutor.appendChild(new Listitem(String.format("%s (%s)", pTutorName, pTutorCode)) {{setValue(pTutorRg);}});
			s2Tutor.setSelectedIndex(0);
			s2Tutor.setDisabled(true);
		}
		else 
			loadPickTutorDatas(sessionHelper, s2Tutor);
		
		final Map<String, Button> dialogButtonMap = new HashMap<String, Button>();
		final MessageboxDlg dlg = ZkUtil.buildNoButtonsMessageboxDlg("Course Summary", 
			vl, 
    		new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.ABORT, Messagebox.Button.IGNORE, Messagebox.Button.CANCEL}, 
    		new String[] {"Refresh", "Export", "Clear", "Close"},
    		comp.getRoot(),
  			new EventListener<Messagebox.ClickEvent>(){
				List<String> exportSheetList = new ArrayList<String>();
				List<List<Object[]>> exportDetailListList = new ArrayList<List<Object[]>>();
				List<Map<String, Object>> exportTotalMapList = new ArrayList<Map<String, Object>>();
				
				
				private void refreshResultList(final MessageboxDlg dlg, Map<Integer, String> tutorMap, Date fromDate, Date toDate) {
					dialogButtonMap.get("export").setDisabled(true);
					exportSheetList.clear();
					exportDetailListList.clear();
					exportTotalMapList.clear();
					
					if (tutorMap == null)
						tutorMap = loadAllTutorDatas(sessionHelper);

					//build tabbox
					while (!tabs.getChildren().isEmpty())
						tabs.removeChild(tabs.getChildren().get(0));
					while (!tabpanels.getChildren().isEmpty())
						tabpanels.removeChild(tabpanels.getChildren().get(0));
					final Map<Integer, Map<String, Component>> tutorGridMap = new HashMap<Integer, Map<String, Component>>();
					for (Map.Entry<Integer, String> map : tutorMap.entrySet()) {
						final Map<String, Component> gridMap = buildCourseSummaryGrid();
						addCourseSummaryGridAfterSizeEvent(dlg, s2Tutor, buttonsDiv, tabbox, tabs, tabpanels, (Grid)gridMap.get("gh"));
						tutorGridMap.put(map.getKey(), gridMap);
						tabs.appendChild(new Tab(map.getValue()));
						tabpanels.appendChild(new Tabpanel() {{
							appendChild(new Div() {{
								appendChild(gridMap.get("gh"));
								setStyle("overflow-x:auto");
								setVflex("1");
							}});
							setAttribute("gridMap", gridMap);
						}});
						exportSheetList.add(map.getValue());
					}

					//fill grid data
					ReturnMsg rtn;
					BiResult brCourse = null;
					BiResult brAttend = null;
					SimpleDateFormat dsdf = new SimpleDateFormat("yyyy/MM/dd");
					Date fromDate1 = DateUtil.dayBeginning(fromDate);
					Date toDate1 = DateUtil.nextday(DateUtil.dayBeginning(toDate));
					try {
						brCourse = sessionHelper.newBiResult("edu.Course");
						brAttend = sessionHelper.newBiResult("edu.StudentAttendance");
						boolean hasRecords = false;
						for (int tutorRg : tutorMap.keySet()) {
							Map<String, Component> gridMap = tutorGridMap.get(tutorRg);
							final GridHelper gh = (GridHelper)gridMap.get("gh");
							final Foot ftGh = (Foot)gridMap.get("ftGh");
							final Label lbSumSessionCount = (Label)gridMap.get("lbSumSessionCount");
							final Label lbSumTotalDuration = (Label)gridMap.get("lbSumTotalDuration");
							final Label lbSumStudentCount = (Label)gridMap.get("lbSumStudentCount");
							final Label lbSumStudentSessionCount = (Label)gridMap.get("lbSumStudentSessionCount");
							final Label lbSumStudentSessionFee = (Label)gridMap.get("lbSumStudentSessionFee");
							final Rows rows = gh.getRows();

							List<Object[]> exportDetailList = new ArrayList<Object[]>();
							Map<String, Object> exportTotalMap = new HashMap<String, Object>();
							exportDetailListList.add(exportDetailList);
							exportTotalMapList.add(exportTotalMap);

							brCourse.clearCondition();
							brCourse.addCustomCondition(String.format("eaav0_esttrg = %d and essncs_date between '%s' and '%s'", tutorRg, dsdf.format(fromDate), dsdf.format(toDate)));
							brCourse.addOrderByColumnList("eaav0_code", false);
							if ((rtn = brCourse.query(true, false)).getStatus()) {
								int sumSessionCount = 0;
								long sumTotalDuration = 0;
								int sumStudentCount = 0;
								int sumStudentSessionCount = 0;
								double sumStudentSessionFee = 0;
								while (brCourse.next(false)) {
									BiResult brSession = brCourse.getSubLink("edu.CourseSessionDet");
									int courseRg = brCourse.getCellInt("eaav0_rg");
									int sessionCount = 0;
									long totalDuration = 0;
									while (brSession.next()) {
										Date sessionDate = brSession.getCellDate("essncs_date");
										Date startTime = brSession.getCellDate("essncs_sttime");
										Date endTime = brSession.getCellDate("essncs_endtime");
										if (sessionDate.compareTo(fromDate1) >= 0 && sessionDate.compareTo(toDate1) < 0) {
											if (!DateUtil.isDateNull(startTime) && !DateUtil.isDateNull(endTime) && startTime.compareTo(endTime) < 0)
												totalDuration += endTime.getTime() - startTime.getTime();
											sessionCount++;
										}
									}
									sumSessionCount += sessionCount;
	
									Set<Integer> studentRgList = new HashSet<Integer>();
									int studentSessionCount = 0;
									brAttend.clearCondition();
									brAttend.addCustomCondition(String.format("essn_avrg = %d and essn_date between '%s' and '%s'", courseRg, dsdf.format(fromDate), dsdf.format(toDate)));
									if ((rtn = brAttend.query(true, false)).getStatus()) {
										while (brAttend.next()) {
											int studentRg = brAttend.getCellInt("esatsd_atrg");
											int sessionRg = brAttend.getCellInt("esatsd_snrg");
											String attStatus = brAttend.getCellString("esatsd_status");
											UniLog.log1("calcStudentSessionCount studentRg:%d, sessionRg:%d, attStatus:%s", studentRg, sessionRg, attStatus);
											studentRgList.add(studentRg);
											if (StringUtils.equalsAny(attStatus, "Present", "Absent"))
												studentSessionCount++;
										}
									}
									else
										throw new Exception(rtn.getMsg());
									sumStudentCount += studentRgList.size();
									sumStudentSessionCount += studentSessionCount;
									
									double studentSessionFee = 0;
									TableRec tr = brAttend.getSelectUtil().getQueryResult("select tkmovd_org, tkmovd_srg, tkmovd_ccy, tkmovd_outqty from tokenmovd, essession "
											+ "where tkmovd_type = 'AT' and tkmovd_crg = ?"
											+ " and essn_type = 0 and essn_date between ? and ?"
											+ " and essn_rg = tkmovd_srg", 
											new Wherecl().appendArgument(courseRg)
											.appendArgument(dsdf.format(fromDate))
											.appendArgument(dsdf.format(toDate)));
									for (int i = 0; i < tr.getRecordCount(); i++) {
										tr.setRecPointer(i);
										int studentRg = tr.getFieldInt("tkmovd_org");
										int sessionRg = tr.getFieldInt("tkmovd_srg");
										String ccy = tr.getFieldString("tkmovd_ccy");
										double outQty = tr.getFieldDouble("tkmovd_outqty");
										UniLog.log1("calcStudentSessionFee studentRg:%d, sessionRg:%d, ccy:%s, outQty:%f", studentRg, sessionRg, ccy, outQty);
										studentSessionFee += outQty;
									}
									sumStudentSessionFee += studentSessionFee;
	
									if (totalDuration > 0) {
										totalDuration /= 60000; //minsec to minute
										sumTotalDuration += totalDuration;
										Object[] os = new Object[] {
											brCourse.getCellString("eaav0_code"),
											brCourse.getCellString("eaav0_name"),
											sessionCount,
											brCourse.getCellInt("eaav0_sessionlen"),
											String.format("%02dh%02dm", totalDuration / 60, totalDuration % 60),
											brCourse.getCellDouble("eaav0_fee"),
											studentRgList.size(),
											studentSessionCount,
											studentSessionFee
										};
										gh.addRow(new Label((String)os[0]), 
													new Label((String)os[1]),
													new Label("" + os[2]),
													new Label("" + os[3]),
													new Label((String)os[4]),
													new Label(Student.getDecimalPrintString((Double)os[5])),
													new Label("" + os[6]),
													new Label("" + os[7]),
													new Label(Student.getDecimalPrintString((Double)os[8])));
										exportDetailList.add(os);
									}
								}
								exportTotalMap.put("sumSessionCount", sumSessionCount);
								exportTotalMap.put("sumTotalDuration", String.format("%02dh%02dm", sumTotalDuration / 60, sumTotalDuration % 60));
								exportTotalMap.put("sumStudentCount", sumStudentCount);
								exportTotalMap.put("sumStudentSessionCount", sumStudentSessionCount);
								exportTotalMap.put("sumStudentSessionFee", sumStudentSessionFee);
								lbSumSessionCount.setValue("" + exportTotalMap.get("sumSessionCount"));
								lbSumTotalDuration.setValue((String)exportTotalMap.get("sumTotalDuration"));
								lbSumStudentCount.setValue("" + exportTotalMap.get("sumStudentCount"));
								lbSumStudentSessionCount.setValue("" + exportTotalMap.get("sumStudentSessionCount"));
								lbSumStudentSessionFee.setValue(Student.getDecimalPrintString((Double)exportTotalMap.get("sumStudentSessionFee")));
								if (!rows.getChildren().isEmpty()) {
									ftGh.setVisible(true);
									hasRecords = true;
								}
							}
							else
								throw new Exception(rtn.getMsg());
						}
						if (hasRecords)
							dialogButtonMap.get("export").setDisabled(false);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
					finally {
						if (brCourse != null)
							brCourse.close();
						if (brAttend != null)
							brAttend.close();
					}
					Clients.resize(dlg);
					//loop setSelectedIndex for fit dialog size
					ZkUtil.delayPostEvent("onLoopSetSelectedIndex", dlg, null, 100);
				}
				private void exportToExcel() {
					try {
						//open template
						InputStream is = sessionHelper.openResourceAsStream("/template/export_template.xlsx");
						ExcelPoi jxf = ExcelPoi.newExcelPoi(is,true); 
						is.close();

						for (int i = 0; i < exportSheetList.size(); i++) {
							if (i == 0)
								jxf.excel_setSheetName(0, exportSheetList.get(0));
							else
								jxf.excel_cloneSheet(0, exportSheetList.get(i));
						}
						for (int i = 0; i < exportSheetList.size(); i++) {
							jxf.excel_useSheet(i);

							Vector<Object> v = new Vector<Object>();

							//add header
							jxf.excel_setCellStyle(jxf.excel_getStyleGen(null,null,"Bold",null,"alignTop"));
							GridHelper gh = (GridHelper)((Map<String, Component>)tabpanels.getChildren().get(0).getAttribute("gridMap")).get("gh");
							for (int j = 0; j < gh.getColumns().getChildren().size(); j++)
								v.add(gh.getColumn(j).getLabel());
							jxf.excel_setValues(0, 0, v);

							//add detail
							int[] colts = new int[] {
								jxf.excel_getStyleGen("General","AUTOMATIC",null,null,"alignTop"),
								jxf.excel_getStyleGen("General","AUTOMATIC",null,null,"alignTop"),
								jxf.excel_getStyleGen("0","AUTOMATIC",null,null,"alignTop"),
								jxf.excel_getStyleGen("0","AUTOMATIC",null,null,"alignTop"),
								jxf.excel_getStyleGen("General","AUTOMATIC",null,null,"alignTop"),
								jxf.excel_getStyleGen("0.00","AUTOMATIC",null,null,"alignTop"),
								jxf.excel_getStyleGen("0","AUTOMATIC",null,null,"alignTop"),
								jxf.excel_getStyleGen("0","AUTOMATIC",null,null,"alignTop"),
								jxf.excel_getStyleGen("0.00","AUTOMATIC",null,null,"alignTop")
							};
							List<Object[]> exportDetailList = exportDetailListList.get(i);
							for (int j = 0; j < exportDetailList.size(); j++) {
								v.clear();
								for (int k = 0; k < colts.length; k++) {
									v.add(colts[k]);
									v.add(exportDetailList.get(j)[k]);
								}
								jxf.excel_setValuesWithStyle(j + 1, 0, v);
							}

							//add total
							int[] colts1 = new int[] {
								jxf.excel_getStyleGen("General","AUTOMATIC","Bold",null,"alignTop"),
								jxf.excel_getStyleGen("0","AUTOMATIC","Bold",null,"alignTop"),
								jxf.excel_getStyleGen("General","AUTOMATIC","Bold",null,"alignTop"),
								jxf.excel_getStyleGen("0.00","AUTOMATIC","Bold",null,"alignTop"),
								jxf.excel_getStyleGen("General","AUTOMATIC","Bold",null,"alignTop"),
								jxf.excel_getStyleGen("0","AUTOMATIC","Bold",null,"alignTop"),
								jxf.excel_getStyleGen("0","AUTOMATIC","Bold",null,"alignTop"),
								jxf.excel_getStyleGen("0.00","AUTOMATIC","Bold",null,"alignTop")
							};
							Map<String, Object> exportTotalMap = exportTotalMapList.get(i);
							Object[] ds = new Object[] {
								"Total:",
								exportTotalMap.get("sumSessionCount"),
								"",
								exportTotalMap.get("sumTotalDuration"),
								"",
								exportTotalMap.get("sumStudentCount"),
								exportTotalMap.get("sumStudentSessionCount"),
								exportTotalMap.get("sumStudentSessionFee")
							};
							v.clear();
							for (int j = 0; j < colts1.length; j++) {
								v.add(colts1[j]);
								v.add(ds[j]);
							}
							jxf.excel_setValuesWithStyle(exportDetailList.size() + 1, 1, v);

							//auto resize
							for (int j = 0; j < colts.length; j++)
								jxf.excel_autoResizeColumn(j);
						}

						//write to wookbook
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						jxf.writeWorkBook(bos);
						
						//save
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    			    	Filedownload.save(bos.toByteArray(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", 
    			    			String.format("coursesummary_%s_%s.xlsx", pTutorCode != null ? pTutorCode : "batch", sdf.format(DateUtil.now())));
					}
					catch (Exception e) {
						ZkUtil.errMsg("Error: %s", e.getMessage());
						e.printStackTrace();
					}
				}
				private void showErrMsg(Component comp, String p_format, Object...p_args){
					Clients.evalJavaScript("$('#"+comp.getUuid()+"').notify(\""+String.format(p_format, p_args)+"\", { className: \"error\", elementPosition:\"top right\", arrowShow: false, autoHideDelay: 5000 })");
				}
   				@Override
   				public void onEvent(final ClickEvent event) throws Exception {
   					UniLog.log1("event:%s, target:%s", event, event.getTarget());
   					if (event.getButton() == null)
   						return;
   					final MessageboxDlg dlg = (MessageboxDlg)event.getTarget();
   					switch (event.getButton()) {
   					case OK: //Refresh
   						final Date fromDate = dbFrom.getValue();
   						final Date toDate = dbTo.getValue();
   						Map<Integer, String> tutorMap = new LinkedHashMap<Integer, String>();
   						Map<String, Integer> tutorTreeMap = new TreeMap<String, Integer>();
   						for (Listitem li : s2Tutor.getSelectedItems()) {
   							int tutorRg = li.getValue();
   							if (tutorRg <= 0) {
   								tutorTreeMap = null;
   								break;
   							}
   							String tutorLabel = li.getLabel();
   							tutorTreeMap.put(tutorLabel, tutorRg);
   						}
   						if (tutorTreeMap != null) {
   							for (Map.Entry<String, Integer> entry : tutorTreeMap.entrySet())
   								tutorMap.put(entry.getValue(), entry.getKey());
   						}
   						else
   							tutorMap = null;
   						if (tutorMap != null && tutorMap.isEmpty()) {
							showErrMsg(tabbox, "Please choose tutor");
							event.stopPropagation();
							break;
   						}
   						if (DateUtil.isDateNull(fromDate)) {
							showErrMsg(tabbox, "Please input from date");
							event.stopPropagation();
							break;
   						}
   						if (DateUtil.isDateNull(toDate)) {
							showErrMsg(tabbox, "Please input from date");
							event.stopPropagation();
							break;
   						}
   						if (fromDate.compareTo(toDate) > 0) {
							showErrMsg(tabbox, "'From Date' must be less than or equals with 'To Date'");
							event.stopPropagation();
							break;
   						}
   						final Map<Integer, String> tutorMap1 = tutorMap;
   						new ZkBiAbstractLongOp(dlg, "Loading...", 50) {
							@Override
							public ReturnMsg longOp() {
								refreshResultList(dlg, tutorMap1, fromDate, toDate);
								return null;
							}
   						};

   						event.stopPropagation();
   						break;
   					case ABORT: //Export
   						new ZkBiAbstractLongOp(dlg, "Processing...", 50) {
							@Override
							public ReturnMsg longOp() {
								exportToExcel();
								return null;
							}
   						};
   						event.stopPropagation();
   						break;
   					case IGNORE: //Clear
   						dialogButtonMap.get("export").setDisabled(true);
   						if (pTutorRg == null)
   							ZkUtil.js("$('#%s').val(null).trigger('change').trigger('select2:unselect')", s2Tutor.getUuid());
   						dbFrom.setValueInLocalDateTime(LocalDateTime.now().minusMonths(1).plusDays(1));
   						dbTo.setValueInLocalDateTime(LocalDateTime.now());
   						while (!tabs.getChildren().isEmpty())
   							tabs.removeChild(tabs.getChildren().get(0));
   						while (!tabpanels.getChildren().isEmpty())
   							tabpanels.removeChild(tabpanels.getChildren().get(0));
   						tabs.appendChild(new Tab());
 						tabpanels.appendChild(new Tabpanel() {{
							final Map<String, Component> gridMap = buildCourseSummaryGrid();
							addCourseSummaryGridAfterSizeEvent(dlg, s2Tutor, buttonsDiv, tabbox, tabs, tabpanels, (Grid)gridMap.get("gh"));
							appendChild(new Div() {{
								appendChild(gridMap.get("gh"));
								setStyle("overflow-x:auto");
								setVflex("1");
							}});
							setAttribute("gridMap", gridMap);
 						}});
   						event.stopPropagation();
   						break;
   					}
			}}
    	);
		s2Tutor.addEventListener("onResize", new ZkBiEventListener<Event>() {
	  		@Override
	  		public void onZkBiEvent(Event event) throws Exception {
	  			UniLog.log1("s2Tutor event:%s,data:%s", event, event.getData());
	  			if (event.getData() != null && event.getData() instanceof org.zkoss.json.JSONObject) {
	  				org.zkoss.json.JSONObject json = (org.zkoss.json.JSONObject)event.getData();
	  				Integer oldS2TutorHeight = (Integer)s2Tutor.getAttribute("s2tutorHeight");
	  				int newS2TutorHeight = Math.max((Integer)json.get("height"), 24);
	  				Integer buttonsDivHeight = (Integer)buttonsDiv.getAttribute("buttonsdivHeight");
	  				Integer tabboxHeight = (Integer)tabbox.getAttribute("tabboxHeight");
	  				if (oldS2TutorHeight == null || newS2TutorHeight != oldS2TutorHeight) {
	  					UniLog.log1("newS2TutorHeight:%d, oldS2TutorHeight:%d, buttonsDivHeight:%d, tabboxHeight:%d", newS2TutorHeight, oldS2TutorHeight, buttonsDivHeight, tabboxHeight);
	  					s2Tutor.setAttribute("s2tutorHeight", newS2TutorHeight);
	  					if (tabboxHeight != null && buttonsDivHeight != null) {
	  						int newDlgHeight = newS2TutorHeight + 5 + 29*2 + tabboxHeight + 32 + buttonsDivHeight + 16; //5: s2tutorbottompadding, 29*2: header, 32: titlebar, 16: other padding/border...
	  						UniLog.log1("newDlgHeight:%d", newDlgHeight);
	  						dlg.setHeight(newDlgHeight + "px");
	  						dlg.setAttribute("dlgHeight", newDlgHeight);
	  					}
	  				}
	  			}
	  		}
		});
		buttonsDiv.addEventListener(Events.ON_AFTER_SIZE, new ZkBiEventListener<AfterSizeEvent>() {
			@Override
			public void onZkBiEvent(AfterSizeEvent event) throws Exception {
				UniLog.log1("buttonsDiv event:%s, height:%d", event, event.getHeight());
  				Integer oldButtonsDivHeight = (Integer)s2Tutor.getAttribute("buttonsdivHeight");
  				int newButtonsDivHeight = Math.max(event.getHeight(), 43);
      			Integer s2TutorHeight = (Integer)s2Tutor.getAttribute("s2tutorHeight");
  				Integer tabboxHeight = (Integer)tabbox.getAttribute("tabboxHeight");
  				if (oldButtonsDivHeight == null || newButtonsDivHeight != oldButtonsDivHeight) {
  					UniLog.log1("newButtonsDivHeight:%d, oldButtonsDivHeight:%d, s2TutorHeight:%d, tabboxHeight:%d", newButtonsDivHeight, oldButtonsDivHeight, s2TutorHeight, tabboxHeight);
  					buttonsDiv.setAttribute("buttonsdivHeight", newButtonsDivHeight);
  					if (s2TutorHeight != null && tabboxHeight != null) {
  						int newDlgHeight = newButtonsDivHeight + s2TutorHeight + 5 + 29*2 + tabboxHeight + 32 + 16; //5: s2tutorbottompadding, 29*2: header, 32: titlebar, 16: other padding/border...
  						UniLog.log1("newDlgHeight:%d", newDlgHeight);
  						dlg.setHeight(newDlgHeight + "px");
  						dlg.setAttribute("dlgHeight", newDlgHeight);
  					}
  				}
			}
		});
		dlg.setStyle("max-width:100%;max-height:" + (sessionHelper.isMobile() ? "100" : "95") + "%");
		dlg.addEventListener("onDelayInit", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				ZkUtil.setupSelect2(s2Tutor, true, true, true);
			}
		});
		dlg.addEventListener("onDelayInit2", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				ZkUtil.js("$('#%s .select2').css('flex', '1')", s2TutorDiv.getUuid());
			}
		});
		dlg.addEventListener("onLoopSetSelectedIndex", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				final Component busyComp = ZkBiAbstractLongOp.getBusyComp(dlg);
				Clients.showBusy(busyComp, "Loading");
				busyComp.setAttribute("ZkBiAbstractLongOp.busyFlag", "Y");
				final Timer timer = new Timer();
				timer.setPage(dlg.getPage());
	  			timer.setDelay(150);
	  			timer.setRepeats(true);
	  			timer.addEventListener(Events.ON_TIMER, new EventListener<Event>(){
	  				int tabIndex = 0;
		  			@Override
		  			public void onEvent(Event event) throws Exception {
		  				if (tabIndex < tabs.getChildren().size()) {
		  					tabbox.setSelectedIndex(tabIndex);
		  					tabIndex++;
		  				}
		  				else {
		  					tabbox.setSelectedIndex(0);
		  					timer.setRunning(false);
		  					timer.detach();
		  					Clients.clearBusy(busyComp);
		  					busyComp.removeAttribute("ZkBiAbstractLongOp.busyFlag");
		  				}
		  			}
	  			});
	  			timer.setRunning(true);
			}
		});
      	dlg.doHighlighted();
		ZkUtil.delayPostEvent("onDelayInit", dlg, null, 100);
		ZkUtil.delayPostEvent("onDelayInit2", dlg, null, 500);
		addCourseSummaryGridAfterSizeEvent(dlg, s2Tutor, buttonsDiv, tabbox, tabs, tabpanels, (Grid)((Map<String, Component>)tabpanels.getChildren().get(0).getAttribute("gridMap")).get("gh"));

		//find buttons
		for (Component cbtn : dlg.queryAll("Button")) {
			Button btn = (Button)cbtn;
			if (StringUtils.equals(btn.getLabel(), "Refresh"))
				dialogButtonMap.put("refresh", btn);
			else if (StringUtils.equals(btn.getLabel(), "Close"))
				dialogButtonMap.put("close", btn);
			else if (StringUtils.equals(btn.getLabel(), "Export")) {
				dialogButtonMap.put("export", btn);
				btn.setDisabled(true);
			}
			else if (StringUtils.equals(btn.getLabel(), "Clear"))
				dialogButtonMap.put("clear", btn);
		}
	}

	private static void loadPickTutorDatas(SessionHelper sessionHelper, Listbox s2Tutor) {
		s2Tutor.appendChild(new Listitem("All Tutor") {{setValue(0);}});
		BiResult biResult = null;
		try {
			biResult = BiResultHelper.create(sessionHelper, "edu.Tutor", null, "estt_status = 'Normal'", null, -1, new ArrayList(Arrays.asList(Pair.of("estt_name", false))),false);
			while (biResult.next()) {
				final int rg = biResult.getCellInt("estt_rg");
				final String code = String.format("%s (%s)", biResult.getCellString("estt_name"), biResult.getCellString("estt_ttno"));
				s2Tutor.appendChild(new Listitem(code) {{setValue(rg);}});
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

	private static Map<Integer, String> loadAllTutorDatas(SessionHelper sessionHelper) {
		Map<Integer, String> resultMap = new LinkedHashMap<Integer, String>();
		BiResult biResult = null;
		try {
			biResult = BiResultHelper.create(sessionHelper, "edu.Tutor", null, "estt_status = 'Normal'", null, -1, new ArrayList(Arrays.asList(Pair.of("estt_name", false))),false);
			while (biResult.next()) {
				final int rg = biResult.getCellInt("estt_rg");
				final String code = String.format("%s (%s)", biResult.getCellString("estt_name"), biResult.getCellString("estt_ttno"));
				resultMap.put(rg, code);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (biResult != null)
				biResult.close();
		}
		return resultMap;
	}
	
	private static Map<String, Component> buildCourseSummaryGrid() {
		final GridHelper gh = new GridHelper(9);
		final Label lbSumSessionCount = new Label();
		final Label lbSumTotalDuration = new Label();
		final Label lbSumStudentCount = new Label();
		final Label lbSumStudentSessionCount = new Label();
		final Label lbSumStudentSessionFee = new Label();
		gh.setSclass("zkbi-da");
		gh.setStyle("white-space:nowrap");
		gh.setVflex("1");
		gh.setEmptyMessage("No Record");
		gh.getColumn(0).setLabel("Course Code");
		gh.getColumn(0).setTooltiptext("Course Code");
		gh.getColumn(1).setLabel("Course Name");
		gh.getColumn(1).setTooltiptext("Course Name");
		gh.getColumn(2).setLabel("Session Count");
		gh.getColumn(2).setTooltiptext("Number of Session in a given time frame");
		gh.getColumn(3).setLabel("Session Duration");
		gh.getColumn(3).setTooltiptext("Duration of each session(Min)");
		gh.getColumn(4).setLabel("Total Duration");
		gh.getColumn(4).setTooltiptext("Total session duration(Min)\nSession Count x Duration");
		gh.getColumn(5).setLabel("Current Session Fee");
		gh.getColumn(5).setTooltiptext("Course Fee/Session");
		gh.getColumn(6).setLabel("Student Count");
		gh.getColumn(6).setTooltiptext("Count the student with attendance record");
		gh.getColumn(7).setLabel("Student Session Count");
		gh.getColumn(7).setTooltiptext("Count all actual charged session");
		gh.getColumn(8).setLabel("Student Session Fee");
		gh.getColumn(8).setTooltiptext("Sum all actual charged session fee");
		gh.getColumn(0).setHflex("min");
		gh.getColumn(1).setHflex("min");
		gh.getColumn(2).setHflex("min");
		gh.getColumn(3).setHflex("min");
		gh.getColumn(4).setHflex("min");
		gh.getColumn(5).setHflex("min");
		gh.getColumn(6).setHflex("min");
		gh.getColumn(7).setHflex("min");
		gh.getColumn(8).setHflex("min");
		gh.getColumn(2).setAlign("end");
		gh.getColumn(3).setAlign("end");
		gh.getColumn(4).setAlign("end");
		gh.getColumn(5).setAlign("end");
		gh.getColumn(6).setAlign("end");
		gh.getColumn(7).setAlign("end");
		gh.getColumn(8).setAlign("end");
		final Foot ftGh = new Foot() {{
			appendChild(new Footer());
			appendChild(new Footer("Total:"));
			appendChild(new Footer() {{
				appendChild(lbSumSessionCount);
			}});
			appendChild(new Footer());
			appendChild(new Footer() {{
				appendChild(lbSumTotalDuration);
			}});
			appendChild(new Footer());
			appendChild(new Footer() {{
				appendChild(lbSumStudentCount);
			}});
			appendChild(new Footer() {{
				appendChild(lbSumStudentSessionCount);
			}});
			appendChild(new Footer() {{
				appendChild(lbSumStudentSessionFee);
			}});
			setVisible(false);
		}};
		gh.appendChild(ftGh);
		return new HashMap<String, Component>() {{
			put("gh", gh);
			put("ftGh", ftGh);
			put("lbSumSessionCount", lbSumSessionCount);
			put("lbSumTotalDuration", lbSumTotalDuration);
			put("lbSumStudentCount", lbSumStudentCount);
			put("lbSumStudentSessionCount", lbSumStudentSessionCount);
			put("lbSumStudentSessionFee", lbSumStudentSessionFee);
		}};
	}

	private static void addCourseSummaryGridAfterSizeEvent(final MessageboxDlg dlg, final Listbox s2Tutor, final Div buttonsDiv, final Tabbox tabbox, final Tabs tabs, final Tabpanels tabpanels, final Grid gh) {
		gh.addEventListener(Events.ON_AFTER_SIZE, new ZkBiEventListener<Event>() {
	  		@Override
	  		public void onZkBiEvent(Event event) throws Exception {
	  			boolean heightFlag = false, widthFlag = false;
	  			int newGridHeight = 0, newGridWidth = 0;
	  			for (int i = 0; i < tabpanels.getChildren().size(); i++) {
	  				Component tp = tabpanels.getChildren().get(i);
	  				Map<String, Component> map = (Map<String, Component>)tp.getAttribute("gridMap");
       				GridHelper tgh = (GridHelper)map.get("gh");
       				final Foot tftGh = (Foot)map.get("ftGh");
       				Integer oldtGridHeight = (Integer) tgh.getAttribute("gridHeight");
       				Integer oldtGridWidth = (Integer) tgh.getAttribute("gridWidth");
       				Integer newtGridHeight = (event instanceof AfterSizeEvent && tgh == event.getTarget()) ? (Integer)((AfterSizeEvent)event).getHeight() : (Integer)tgh.getAttribute("gridHeight");
       				Integer newtGridWidth = (event instanceof AfterSizeEvent && tgh == event.getTarget()) ? (Integer)((AfterSizeEvent)event).getWidth() : (Integer)tgh.getAttribute("gridWidth");
       				if (newtGridHeight == null)
       					newtGridHeight = 0;
       				if (newtGridWidth == null)
       					newtGridWidth = 0;
       				int tgridFootHeight = tftGh.isVisible() ? 33 : 1;
	  				newGridHeight = Math.max(newGridHeight, newtGridHeight + tgridFootHeight);
	  				newGridWidth = Math.max(newGridWidth, newtGridWidth);
	  				tgh.setAttribute("gridHeight", newtGridHeight);
	  				tgh.setAttribute("gridWidth", newtGridWidth);
	  				UniLog.log1("newtGridHeight:%d, oldtGridHeight:%d, newtGridWidth:%d, oldtGridWidth:%d", newtGridHeight, oldtGridHeight, newtGridWidth, oldtGridWidth);
		  			if (oldtGridHeight == null || newtGridHeight != oldtGridHeight)
		  				heightFlag = true;
		  			if (oldtGridWidth == null || newtGridWidth != oldtGridWidth)
		  				widthFlag = true;
	  			}
       			if (heightFlag) {
       				Integer s2TutorHeight = (Integer)s2Tutor.getAttribute("s2tutorHeight");
       				if (s2TutorHeight != null)
       					s2TutorHeight += 5;
       				else
       					s2TutorHeight = 29;
       				Integer buttonsDivHeight = (Integer)buttonsDiv.getAttribute("buttonsdivHeight");
       				if (buttonsDivHeight == null)
       					buttonsDivHeight = 43;
       				int gridHeaderHeight = 34;
       				int tabboxHeight = gridHeaderHeight + newGridHeight + 2 + 45;
       				int newDlgHeight = tabboxHeight + s2TutorHeight + 29*2 + 32 + buttonsDivHeight + 16; //2: grid border, 29*3: header, 45: tabbox exclude grid, 32: titlebar, 16: other padding/border...
	  				UniLog.log1("newGridHeight:%d, tabboxHeight:%d, newDlgHeight:%d", newGridHeight, tabboxHeight, newDlgHeight);
		  			dlg.setHeight(newDlgHeight + "px");
		  			tabbox.setAttribute("tabboxHeight", tabboxHeight);
		  			dlg.setAttribute("dlgHeight", newDlgHeight);
       			}
       			if (widthFlag) {
       				int newDlgWidth = newGridWidth + 18 + 32; //18: scrollbar width, 32: dialog width - grid width
	  				UniLog.log1("newGridWidth:%d, newDlgWidth:%d", newGridWidth, newDlgWidth);
  					dlg.setWidth(newDlgWidth + "px");
		  			dlg.setAttribute("gridWidth", newGridWidth);
		  			dlg.setAttribute("dlgWidth", newDlgWidth);
       			}
	       	}
		});
	}
}
