package com.uniinformation.jxapp.edu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.zkoss.zhtml.I;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.event.SortEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Auxhead;
import org.zkoss.zul.Auxheader;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Foot;
import org.zkoss.zul.Footer;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.ListitemComparator;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Space;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.impl.MessageboxDlg;

import com.google.common.collect.Sets;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.edu.ProcessScanLog;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrBuilder;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.GridHelper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.edu.ZkBiComposerStudent;
import com.uniinformation.zkf.ZkForm;

public class Student extends JxZkBiBase {
	private final static DecimalFormat decFormatPrint = new DecimalFormat("#,##0.00");
	private final TreeMap<Date, String> holidayMap = new TreeMap<Date, String>();

	@Override
	public void afterBind() {
		UniLog.log1("afterBind called");
		super.afterBind();
		jxAdd("btGeneratePdf").addActionListener(new JxActionListener(){
			@Override
			public void actionPerformed(JxField field) {
				if (isDirty())
					ZkUtil.showErrMsg("Please save the newly input values before generate pdf");
				else
					doPdfGenerate();
			}
		});
		jxAdd("btUpdateAttendance").addActionListener(new JxActionListener() {
			@Override
			public void actionPerformed(JxField field) {
				if (isDirty())
					ZkUtil.showErrMsg("Please save the newly input values before update attendance");
				else {
					//int studentRg = NumberUtils.toInt(jxAdd("essd_rg").getValue().toString());
					int studentRg = getBr().getCellInt("essd_rg");
					String cardNo = getBr().getCellString("essd_cardno");
					if (studentRg <= 0) {
						UniLog.log1("Invalid student rg:%d", studentRg);
						return;
					}
					UniLog.log1("studentRg:%d", studentRg);
					doUpdateAttendance(sessionHelper, "Update Attendance", (Button) field.getNativeObject(), Student.this, curMode, MapUtil.ofPairs(Pair.of(studentRg, cardNo)));
				}
			}
		});
		jxAdd("btPayment").addActionListener(new JxActionListener() {
			@Override
			public void actionPerformed(JxField field) {
				if (isDirty())
					ZkUtil.showErrMsg("Please save the newly input values before payment");
				else
					doPayment((Button) field.getNativeObject());
			}
		});
		jxAdd("btAddToAsmt").addActionListener(new JxActionListener() {
			@Override
			public void actionPerformed(JxField field) {
				if (isDirty())
					ZkUtil.showErrMsg("Please save the newly input values before add to asmt");
				else {
					try {
						int studentRg = getBr().getCellInt("essd_rg");
						if (studentRg <= 0) {
							UniLog.log1("Invalid student rg:%d", studentRg);
							return;
						}
						UniLog.log1("studentRg:%d", studentRg);
						ZkBiComposerStudent.doAddToAsmtSch(sessionHelper, (Button) field.getNativeObject(), false, Sets.newHashSet(studentRg));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		jxAdd("btAddToCourse").addActionListener(new JxActionListener() {
			@Override
			public void actionPerformed(JxField field) {
				if (isDirty())
					ZkUtil.showErrMsg("Please save the newly input values before add to course");
				else {
					try {
						int studentRg = getBr().getCellInt("essd_rg");
						if (studentRg <= 0) {
							UniLog.log1("Invalid student rg:%d", studentRg);
							return;
						}
						UniLog.log1("studentRg:%d", studentRg);
						ZkBiComposerStudent.doAddToCourse(sessionHelper, (Button) field.getNativeObject(), Student.this, curMode, Sets.newHashSet(studentRg));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
		jxAdd("btRecalTokenBal").addActionListener(new JxActionListener() {
			@Override
			public void actionPerformed(JxField field) {
				if (isDirty())
					ZkUtil.showErrMsg("Please save the newly input values before recal token balance");
				else {
					try {
						final int studentRg = getBr().getCellInt("essd_rg");
						final String cardNo = getBr().getCellString("essd_cardno");
						if (studentRg <= 0) {
							UniLog.log1("Invalid student rg:%d", studentRg);
							return;
						}
						UniLog.log1("studentRg:%d", studentRg);
						ZkBiMsgbox.show(ZkBiMsgbox.Type.question, "Are you sure to Recal Token Balance?", 
  							new String[] {"Ok", "Cancel"}, new ZkBiEventListener<Event>() {
							@Override
							public void onZkBiEvent(Event event) throws Exception {
								ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
								if (btn.getName().equals("Ok")) {
									try {
										recalTokenBal(sessionHelper, null, MapUtil.ofPairs(Pair.of(studentRg, cardNo)), null, null, true, true);

										getBr().refetchCurrent();
										bindCellCollection(getBr(), curMode);
										ZkUtil.showMsg("Recal Token Balance done");
									}
									catch (Exception e) {
										ZkUtil.errMsg(e.getMessage());
									}
								}
							}
						});
					} catch (Exception e) {
						ZkUtil.showMsg(e.getMessage());
					}
				}
			}
		});
		jxAdd("btSetLoginPassword").addActionListener(new JxActionListener() {
			@Override
			public void actionPerformed(JxField field) {
				try {
					int studentRg = getBr().getCellInt("essd_rg");
					if (studentRg <= 0) {
						UniLog.log1("Invalid student rg:%d", studentRg);
						return;
					}
					UniLog.log1("studentRg:%d", studentRg);
					String loginId = getBr().getCellString("essd_sdno").toLowerCase();
					String userName = getBr().getCellString("essd_name");
					String userChnName = getBr().getCellString("essd_chnname");
					new DoSetLoginPassword(sessionHelper, loginId, userName, userChnName, "#student", (Button)field.getNativeObject());
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
					jo.put("studentCode", getBr().getCellString("essd_sdno"));
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
		
		//dummy code for generate scan event
		jxAdd("btRegCardTest").addActionListener(new JxActionListener(){
			@Override
			public void actionPerformed(JxField field) {
				String readerId = Erpv4Config.getString(sessionHelper, "RegCardReader", "RFID01");
				ProcessScanLog.processScanLogRegDev(readerId, "" + (new Date().getTime() / 1000));
				//ProcessScanLog.processScanLogRegDev(readerId, "HAHA");
			}
		});
		
		
		//dummy code for test card status
		jxAdd("btCardStatusTest").addActionListener(new JxActionListener(){
			@Override
			public void actionPerformed(JxField field) {
				String cardNo = getBr().getCellString("essd_cardno");
				ZkUtil.normMsg("cardno:%s result:%s\n", cardNo, ProcessScanLog.checkCardStatus(cardNo));
			}
		});
		
		//dummy code for test addscanlog
		jxAdd("btAddScanLogTest").addActionListener(new JxActionListener(){
			@Override
			public void actionPerformed(JxField field) {
				String cardNo = getBr().getCellString("essd_cardno");
				ReturnMsg rtn = ProcessScanLog.addScanLog(sessionHelper, "RFID01", cardNo);
				ZkUtil.normMsg("cardno:%s result:%s\n", cardNo, rtn);
			}
		});
		
		
		jxAdd("btRegCard").addActionListener(new JxActionListener(){
			@Override
			public void actionPerformed(JxField field) {
				try {
					final String readerId = Erpv4Config.getString(sessionHelper, "RegCardReader", "RFID01");
					final AtomicInteger timeLimit = new AtomicInteger(30000);
					final AtomicInteger timeRemain = new AtomicInteger(timeLimit.get());
					final AtomicInteger timeDelay = new AtomicInteger(1000);
					final AtomicReference<String> cardNoAR = new AtomicReference();
					
					
					ReturnMsg rtn = ProcessScanLog.allocateRegDev(readerId, timeLimit.get(), cardNoAR);
					UniLog.log1("allocation return %s", rtn);
					if (!rtn.getStatus()) {
						ZkUtil.errMsg(rtn.getMsg());
						return;
					}
					
					GridHelper gh = new GridHelper(2);
					
					gh.getColumn(0).setWidth("100px");
					gh.getColumn(0).setAlign("left");
					gh.getColumn(1).setHflex("1");
					
					gh.addRow(new Label(String.format("Please scan student card [%s]",readerId)));
					gh.getRow(0).setSpans("2");
					
					Textbox tbStatus = new Textbox(String.format("Waiting for scan event(%.1fs)...", ((float)timeRemain.get())/1000));
					tbStatus.setWidth("250px");
					tbStatus.setReadonly(true);
					gh.addRow(new Label("Status"), tbStatus);
					final Progressmeter pm = new Progressmeter(100);
					pm.setWidth("250px");
					gh.addRow(new Space(), pm);
					
					
					final AtomicBoolean fRegCardStop = new AtomicBoolean(false);
					final MessageboxDlg dlg = ZkUtil.buildMessageboxDlg("Register Student Card", gh, 
							new Messagebox.Button[]{Messagebox.Button.CANCEL}, 
							zkf.getRootComponent(), 
							new EventListener<Messagebox.ClickEvent>(){

								@Override
								public void onEvent(ClickEvent event) throws Exception {
									UniLog.log1("called:" + event);
									fRegCardStop.set(true);
									ZkUtil.showMsg("Action abort");
								}
						});
					dlg.setWidth("400px");
					dlg.doHighlighted();
					
					final Timer pmTimer = new Timer();
					pmTimer.setPage(zkf.getRootComponent().getPage());
					pmTimer.setDelay(timeDelay.get());
					pmTimer.setRepeats(true);
					pmTimer.start();
					pmTimer.addEventListener(Events.ON_TIMER, new EventListener() {
						@Override
						public void onEvent(Event event) throws Exception {
							timeRemain.addAndGet(-timeDelay.get());
							int progress = (int) (((float)timeRemain.get() / (float)timeLimit.get())*100);
							if (progress < 0) {
								ZkUtil.msg("No student card detected, abort.");
								progress = 0;
								fRegCardStop.set(true);
							}
							UniLog.log1("called: event:%s timeRemain:%d progress:%d limit:%d", event, timeRemain.get(), progress, timeLimit.get());
							
							//found cardno
							if (cardNoAR.get() != null) {
								if (!StringUtils.equals(cardNoAR.get(), getBr().getCellString("essd_cardno"))) {
									ZkUtil.showMsg("Student card no updated. [%s]", cardNoAR.get());
									getBr().getCell("essd_cardno").set(cardNoAR.get());
									setDirtyFlag(true);
								}
								else {
									ZkUtil.showMsg("Student card keep unchanged. [%s]", cardNoAR.get());
									
								}
								fRegCardStop.set(true);
							}
							
							//end
							if (fRegCardStop.get()) {
								ProcessScanLog.releaseRegDev(readerId);
								UniLog.log1("stop timer");
								pmTimer.setRunning(false);
								pmTimer.detach();
								dlg.detach();
							}
							
							pm.setValue(progress);
						}
					});
					
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		
		this.setRecLock(true);
		
	}
	final public static String zkfName = "zkf/edu/StudentAs.zul";
	ZkForm zkf = null;
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		UniLog.log1("bindCellCollection called");
		super.bindCellCollection(p_br, mode);
		
		jxSetVisible("row_essdx_jsonstr",sessionHelper.isAdminUser());
		jxSetVisible("btCardStatusTest",sessionHelper.isAdminUser());
		jxSetVisible("btAddScanLogTest",sessionHelper.isAdminUser());
		
		/*
		jxSetVisible("essdx_jsonstr",sessionHelper.isAdminUser());
		jxSetVisible("lb_essdx_jsonstr",sessionHelper.isAdminUser());
		*/
		jxSetEnable("btGeneratePdf", mode != JxZkBiBase.MODE_ADD);
		jxSetEnable("btUpdateAttendance", mode != JxZkBiBase.MODE_ADD);
		jxSetEnable("btPayment", mode != JxZkBiBase.MODE_ADD);
		jxSetEnable("btAddToAsmt", mode != JxZkBiBase.MODE_ADD);
		jxSetEnable("btAddToCourse", mode != JxZkBiBase.MODE_ADD);
		jxSetEnable("btSetLoginPassword", mode != JxZkBiBase.MODE_ADD);
		jxSetEnable("btRecalTokenBal", mode != JxZkBiBase.MODE_ADD);
		jxSetEnable("btCalendar", mode != JxZkBiBase.MODE_ADD);
		if (!sessionHelper.isAdminUser() && !sessionHelper.hasAccessRight("#edu") && !sessionHelper.hasAccessRight("#eduadmin")) {
			jxSetVisible("btUpdateAttendance", false);
			jxSetVisible("btGeneratePdf", false);
			jxSetVisible("btPayment", false);
			jxSetVisible("btAddToAsmt", false);
			jxSetVisible("btAddToCourse", false);
			jxSetVisible("btRecalTokenBal", false);
			jxSetVisible("btCalendar", false);
			jxSetVisible("btRegCard", false);
			if (sessionHelper.hasAccessRight("#tutor"))
				jxSetVisible("btSetLoginPassword", false);
			else {
				jxSetEnable("btPrevious", false);
				jxSetEnable("btNext", false);
				jxSetEnable("btReloadDetail", false);
			}
			try {
				jxAdd("essd_surname").setFieldMode(JxField.FMODE_DISPONLY);
				jxAdd("essd_firstname").setFieldMode(JxField.FMODE_DISPONLY);
				jxAdd("essd_chnname").setFieldMode(JxField.FMODE_DISPONLY);
				//if (!sessionHelper.hasAccessRight("#tutor"))
					jxAdd("essd_status").setFieldMode(JxField.FMODE_DISPONLY);
				jxAdd("essd_cardno").setFieldMode(JxField.FMODE_DISPONLY);
				jxAdd("essd_regdate").setFieldMode(JxField.FMODE_DISPONLY);
			} catch (CellException e) {
				e.printStackTrace();
			}
			jxSetVisible("row_essd_contactby", false);
			jxSetVisible("row_essd_lastcontime", false);
			jxSetVisible("row_essd_sex", false);
			jxSetVisible("row_essd_birthdate", false);
			jxSetVisible("row_essd_motongue", false);
			jxSetVisible("row_essd_regreferral", false);
			jxSetVisible("row_essd_schatname", false);
			jxSetVisible("row_essd_grade", false);
			jxSetVisible("row_essd_education", false);
			jxSetVisible("row_essd_clum", false);
			jxSetVisible("row_essd_comments", false);
			if (!sessionHelper.hasAccessRight("#tutor"))
				jxSetVisible("row_essd_asdiv", false);
			else {
				jxSetVisible("row_essd_sdemail", false);
				jxSetVisible("row_essd_sdtel", false);
				jxSetVisible("row_essd_hometel", false);
				jxSetVisible("row_essd_officetel", false);
				jxSetVisible("row_list_edu_StudentCourse", false);
				jxSetVisible("row_list_edu_StudentAttendance", false);
				jxSetVisible("row_list_edu_TokenBal", false);
				jxSetVisible("row_list_edu_StudentPayment", false);
			}
		}
		
		//need to define in view
		JxField fd = jxAdd("essd_asdiv");  
		
		CellCollection col = p_br.getCurrentCollection().getCollection("essdx_jsoncc");
		if(fd != null && col != null) {
			final Component comp = (Component) fd.getNativeObject();
			if(zkf != null) {
				comp.removeChild(zkf.getRootComponent());
				zkf = null;
			}
			if (sessionHelper.isMobile()) {
				Component parentcomp = comp.getParent();
				if (parentcomp instanceof Row) {
					Row row = (Row) parentcomp;
					row.setSpans("2");
					final Label label = (Label) row.query("#lb_essd_asdiv");
					row.removeChild(label);
					row.removeChild(comp);
					row.appendChild(new Vlayout() {{
						setStyle("text-align:left");
						appendChild(label);
						appendChild(comp);
					}});
				}
			}
			Div d = new Div();
			comp.appendChild(d);
			zkf = new ZkForm(d,zkfName); 
			try {
				zkf.mapCellCollection(col, new EventListener() {

					@Override
					public void onEvent(Event event) throws Exception {
						//set the form to dirty when got a zkform event
						UniLog.log1("event class:%s, name:%s, target:%s", event.getClass(), event.getName(), event.getTarget());
						if (!(event.getTarget() instanceof Textbox || event.getTarget() instanceof Radiogroup || event.getTarget() instanceof Radio || event.getTarget() instanceof Checkbox
								|| event.getTarget() instanceof Datebox))
							return;
						setDirtyFlag(true);
					}
					
				});
			} catch (CellException cex) {
				UniLog.log(cex);
			}
			
			Tabbox tbxAss = (Tabbox) zkf.getComponent("tbxAss");
			for (Component cTabpanel : tbxAss.queryAll("tabpanel")) {
				final Tabpanel panel = (Tabpanel) cTabpanel;
				//setup clear button
				final Button btnClear = (Button) panel.query(".btnclear");
				btnClear.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						UniLog.log1("event:%s", event);
						for (Component cRadioGroup : panel.queryAll("radiogroup")) {
							Radiogroup radiogroup = (Radiogroup) cRadioGroup;
							radiogroup.setSelectedIndex(0);
							ZkForm.notifyUpdate(radiogroup);
						}
						for (Component cCheckbox : panel.queryAll("checkbox")) {
							Checkbox checkbox = (Checkbox) cCheckbox;
							checkbox.setChecked(false);
							ZkForm.notifyUpdate(checkbox);
						}
						setDirtyFlag(true);
					}
				});
				//setup all checkbox
				for (Component cCbAllOpt : panel.queryAll(".cballopt")) {
					final Checkbox cbAllOpt = (Checkbox) cCbAllOpt;
					final Div parentDiv = (Div) cbAllOpt.getParent();
					cbAllOpt.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
						@Override
						public void onZkBiEvent(CheckEvent event) throws Exception {
							UniLog.log1("event:%s", event);
							for (Component cCheckbox : parentDiv.queryAll("checkbox")) {
								Checkbox cb = (Checkbox) cCheckbox;
								if (StringUtils.isNotBlank(cb.getId())) {
									cb.setChecked(event.isChecked());
									ZkForm.notifyUpdate(cb);
								}
							}
							setDirtyFlag(true);
						}
					});
					boolean allCheckFlag = true;
					for (Component cCheckbox : parentDiv.queryAll("checkbox")) {
						Checkbox cb = (Checkbox) cCheckbox;
						if (StringUtils.isNotBlank(cb.getId()) && !cb.isChecked())
							allCheckFlag = false;
					}
					cbAllOpt.setChecked(allCheckFlag);
				}
			}
		}
		
		calcStudentCourseField();
	}
	

	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		return ListUtil.of(
			new StudentCourseGetItemProperty(p_br.getSubLink("edu.StudentCourse")),
			new StudentAttendanceGetItemProperty(p_br.getSubLink("edu.StudentAttendance")),
			new TokenBalGetItemProperty(p_br.getSubLink("edu.TokenBal")),
			new StudentPaymentGetItemProperty(p_br.getSubLink("edu.StudentPayment"))
		);	
	}

	private class StudentCourseGetItemProperty extends BiGetItemProperty {
		public StudentCourseGetItemProperty(BiResult p_br) {
			super(p_br);
			setItemMode(BiGetItemProperty.GETITEM_MODE_LIST);
		}
		@Override
		public String getColumnWidth(Object p_v, int p_col){
			Object o = getListColumns(p_v).get(p_col);
			if (o instanceof BiColumn) {
				BiColumn bc = (BiColumn)o;
				if (StringUtils.equals(bc.getLabel(), "eaav0_name"))
					return "hflex=min";
				else if (StringUtils.equals(bc.getLabel(), "eaav0_code"))
					return "hflex=min";
				else if (StringUtils.equals(bc.getLabel(), "eaav0_status"))
					return "100px";
				else if (StringUtils.equals(bc.getLabel(), "eaav0_numsession"))
					return "80px";
				else if (StringUtils.equals(bc.getLabel(), "eaav0_sessionday"))
					return "86px";
				else if (StringUtils.equals(bc.getLabel(), "eaav0_sessiontime"))
					return "70px";
				else if (StringUtils.equals(bc.getLabel(), "essbsd_startdate"))
					return "86px";
				else if (StringUtils.equals(bc.getLabel(), "essbsd_enddate"))
					return "86px";
				else if (StringUtils.equals(bc.getLabel(), "essbsd_status"))
					return "90px";
				else if (StringUtils.equals(bc.getLabel(), "eaav0_fee"))
					return "70px";
				else if (StringUtils.equals(bc.getLabel(), "eaav0_tokenccy"))
					return "50px";
				else if (StringUtils.equals(bc.getLabel(), "essbsd_tentatibal"))
					return "120px";
				else if (StringUtils.equals(bc.getLabel(), "essbsd_sesscomp"))
					return "110px";
				else if (StringUtils.equals(bc.getLabel(), "essbsd_sessremain"))
					return "90px";
				else if (StringUtils.equals(bc.getLabel(), "essbsd_balfee"))
					return "90px";
			}
			return super.getColumnWidth(p_v, p_col);
		}
	}

	private class StudentAttendanceGetItemProperty extends BiGetItemProperty {
		public StudentAttendanceGetItemProperty(final BiResult p_br) {
			super(p_br);
			setItemMode(BiGetItemProperty.GETITEM_MODE_LIST);

			/*final Vector<BiColumn> colList = getListColumns(null);
			final Listbox listbox = (Listbox)jxAdd("list_" + replaceViewName(p_br.getView().getName())).getNativeObject();
			UniLog.log1("listbox:%s", listbox);
			listbox.addEventListener("onInitSort", new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					final Listhead listhead = (Listhead) listbox.query("Listhead");
					int i = 0;
					for (Component c : listhead.queryAll("Listheader")) {
						final Listheader lh = (Listheader) c;
						UniLog.log1("list_edu_StudentAttendance lh label:%s, id:%s, idx:%d", lh.getLabel(), lh.getId(), i);
						final int idx = i;
						if (idx < colList.size()) {
							final BiColumn bc = colList.get(idx);
							lh.setSortDirection("natural");
							lh.setSort("auto");
							lh.setSortAscending(new ListitemComparator());
							lh.addEventListener(Events.ON_SORT, new EventListener<SortEvent>() {
								@Override
								public void onEvent(SortEvent event) throws Exception {
									String oldDir = lh.getSortDirection();
									boolean newDesc = StringUtils.equals(oldDir, "ascending");
									String newDir = newDesc ? "descending" : "ascending";
									UniLog.log1("onSort event:%s, target:%s, idx:%d, colLabel:%s, oldDir:%s, newDir:%s", event, event.getTarget(), idx, bc.getLabel(), oldDir, newDir);
									//delay change sort direction
									ZkUtil.delayPostEvent("onChangeSortDirection", lh, newDir, 100);
									p_br.clearOrderBy();
									p_br.addOrderByViewList(idx + 1, newDesc);
									p_br.sort();
									ListModelList lml = (ListModelList) listbox.getModel();
									lml.clear();
									lml.addAll(p_br.getResultStat());
									event.stopPropagation();
								}
							});
							lh.addEventListener("onChangeSortDirection", new EventListener<Event>() {
								@Override
								public void onEvent(Event event) throws Exception {
									for (Component c : listhead.queryAll("Listheader")) {
										if (c != lh)
											((Listheader)c).setSortDirection("natural");
									}
									lh.setSortDirection((String)event.getData());
								}
							});
						}
						i++;
					}
				}
			});
			ZkUtil.delayPostEvent("onInitSort", listbox, null, 100);*/

		}
		@Override
		public String getColumnWidth(Object p_v, int p_col){
			Object o = getListColumns(p_v).get(p_col);
			if (o instanceof BiColumn) {
				BiColumn bc = (BiColumn)o;
				if (StringUtils.equals(bc.getLabel(), "eaav0_courseassnax"))
					return "hflex=min";
				else if (StringUtils.equals(bc.getLabel(), "essn_name"))
					return "hflex=min";
				else if (StringUtils.equals(bc.getLabel(), "essn_date"))
					return "103px";
				else if (StringUtils.equals(bc.getLabel(), "essn_sttime"))
					return "77px";
				else if (StringUtils.equals(bc.getLabel(), "essn_endtime"))
					return "77px";
				else if (StringUtils.equals(bc.getLabel(), "esatsd_sttime"))
					return "139px";
				else if (StringUtils.equals(bc.getLabel(), "esatsd_endtime"))
					return "139px";
				else if (StringUtils.equals(bc.getLabel(), "esatsd_sessionno"))
					return "86px";
			}
			return super.getColumnWidth(p_v, p_col);
		}
	}

	private class TokenBalGetItemProperty extends BiGetItemProperty {
		public TokenBalGetItemProperty(BiResult p_br) {
			super(p_br);
			setItemMode(BiGetItemProperty.GETITEM_MODE_LIST);
		}
		@Override
		public String getColumnWidth(Object p_v, int p_col){
			Object o = getListColumns(p_v).get(p_col);
			if (o instanceof BiColumn) {
				BiColumn bc = (BiColumn)o;
				if (StringUtils.equals(bc.getLabel(), "tkbal_tenremainbal"))
					return "193px";
			}
			return super.getColumnWidth(p_v, p_col);
		}
	}

	private class StudentPaymentGetItemProperty extends BiGetItemProperty {
		public StudentPaymentGetItemProperty(BiResult p_br) {
			super(p_br);
			//setItemMode(BiGetItemProperty.GETITEM_MODE_LIST);
		}
		@Override
		public String getColumnWidth(Object p_v, int p_col){
			Object o = getListColumns(p_v).get(p_col);
			if (o instanceof BiColumn) {
				BiColumn bc = (BiColumn)o;
				if (StringUtils.equalsAny(bc.getLabel(), "esphsd_receiptno", "esphsd_recfrom", "esphsd_chequeno", "essd_name","esphsd_paymethod","essd_sdno"))
					return "hflex=min";
			}
			return super.getColumnWidth(p_v, p_col);
		}
		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			ColumnCell bcc = (ColumnCell) p_value;
			//handle preview payment button
			if (p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED && bcc.getCellLabel().equals("esphsd_preview")){
				int phrg = bcc.getCollection().getCellInt("esphsd_rg");
				String phno = bcc.getCollection().getCellString("esphsd_receiptno");
				UniLog.log1("%s clicked phrg:%d, phno:%s", bcc.getCellLabel(), phrg, phno);
				if (phrg <= 0 || StringUtils.isBlank(phno)) {
					UniLog.log1("invalid phrg or phno");
					return;
				}
				String receivedFrom = bcc.getCollection().getCellString("esphsd_recfrom");
				double totalAmount = bcc.getCollection().getCellInt("esphsd_totamt");
				double discount = bcc.getCollection().getCellInt("esphsd_discount");
				double grandTotalAmount = bcc.getCollection().getCellInt("esphsd_gtotamt");
				String paymentMethod = bcc.getCollection().getCellString("esphsd_paymethod");
				Date paymentDate = bcc.getCollection().getCell("esphsd_paydate").getDate();
				if (DateUtil.isDateNull(paymentDate))
					paymentDate = bcc.getCollection().getCell("esphsd_ctime").getDate();
				String chequeNo = bcc.getCollection().getCellString("esphsd_chequeno");

				List<Map<String, Object>> courseList = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> assessmentList = new ArrayList<Map<String, Object>>();
				List<Map<String, Object>> tokenList = new ArrayList<Map<String, Object>>();
				BiResult brPaymentDet = null;
				try {
					brPaymentDet = BiResultHelper.create(sessionHelper, "edu.PaymentDet", String.format("espd_phrg = %d", phrg), -1, null);
					while (brPaymentDet.next()) {
						int courseRg = brPaymentDet.getCellInt("espd_avrg");
						int sessionRg = brPaymentDet.getCellInt("espd_snrg");
						if (courseRg > 0) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("courseRg", courseRg);
							map.put("courseCode", brPaymentDet.getCellString("eaav0_code"));
							map.put("courseName", brPaymentDet.getCellString("eaav0_name"));
							map.put("startDate", brPaymentDet.getCellDate("espd_startdate"));
							map.put("endDate", brPaymentDet.getCellDate("espd_enddate"));
							map.put("teacher", brPaymentDet.getCellString("estt_name"));
							map.put("room", brPaymentDet.getCellString("esfc_name"));
							map.put("price", brPaymentDet.getCellDouble("espd_amount"));
							map.put("enabled", true);
							courseList.add(map);
						}
						else if (sessionRg > 0) {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("sessionRg", sessionRg);
							map.put("description", brPaymentDet.getCellString("essnas_name"));
							map.put("date", brPaymentDet.getCellDate("espd_startdate"));
							map.put("teacher", brPaymentDet.getCellString("esttas_name"));
							map.put("room", brPaymentDet.getCellString("esfcas_name"));
							map.put("price", brPaymentDet.getCellDouble("espd_amount"));
							map.put("enabled", true);
							assessmentList.add(map);
						}
						else {
							Map<String, Object> map = new HashMap<String, Object>();
							map.put("tokenCcy", brPaymentDet.getCellString("espd_tokenccy"));
							map.put("tokenCcyName", brPaymentDet.getCellString("tkccy_name"));
							map.put("remark", brPaymentDet.getCellString("espd_remark"));
							map.put("price", brPaymentDet.getCellDouble("espd_amount"));
							map.put("enabled", true);
							tokenList.add(map);
						}
					}	
					printPaymentReceipt(((Button)jxAdd("btPayment").getNativeObject()).getRoot(), phno, receivedFrom, totalAmount, discount, grandTotalAmount, paymentMethod, paymentDate, chequeNo, courseList, assessmentList, tokenList, sessionHelper.getAllowDemo());
				}
				catch (Exception e) {
					e.printStackTrace();
					ZkUtil.showErrMsg(e.getMessage());
				}
				finally {
					if (brPaymentDet != null)
						brPaymentDet.close();
				}
			}
		}
	}
	
	private void doPdfGenerate() {
		final Vbox vbox = new Vbox();
		final Checkbox cbPersonalInfo = new Checkbox("Personal information");
		final Checkbox cbAssessment = new Checkbox("Assessment");
		final Checkbox cbHideEmptyAssessmentItem = new Checkbox("Hide empty assessment item");
		final Checkbox cbAlternateRowColor = new Checkbox("Alternate row color");
		vbox.setStyle("margin-top:10px");
		vbox.appendChild(cbPersonalInfo);
		vbox.appendChild(cbAssessment);
		vbox.appendChild(cbHideEmptyAssessmentItem);
		vbox.appendChild(cbAlternateRowColor);
		cbPersonalInfo.setChecked(true);
		cbAssessment.setChecked(true);
		cbHideEmptyAssessmentItem.setChecked(true);
		cbAlternateRowColor.setChecked(true);
		ZkUtil.buildMessageboxDlg("Generate Pdf", 
			vbox, 
			new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
			((Button) jxAdd("btGeneratePdf").getNativeObject()).getRoot(),
			new EventListener<Messagebox.ClickEvent>(){
				@Override
				public void onEvent(ClickEvent event) throws Exception {
					if (event.getButton() == null)
						return;
					switch (event.getButton()) {
					case OK:
						pdfGenerate(cbPersonalInfo.isChecked(), cbAssessment.isChecked(), cbHideEmptyAssessmentItem.isChecked(), cbAlternateRowColor.isChecked());
						break;
					default:
						break;
					}
				}
			}
		).doHighlighted();
	}

	private void pdfGenerate(boolean isPrintPersonalInfo, boolean isPrintAssessment, boolean isHideEmptyAssessmentItem, boolean isAlternateRowColor) {
		ByteArrayInputStream inStream = null;
		ByteArrayOutputStream outStream = null;
		try {
			//parse studentInfo to stream
			PdfGenerateStudentInfo gsi = new PdfGenerateStudentInfo(isPrintPersonalInfo, isPrintAssessment, isHideEmptyAssessmentItem, isAlternateRowColor);
			ByteArrayOutputStream bosStudentInfo = gsi.build();

			//new ChnftrParser, load studentInfo stream
			ChnftrParser parser = gsi.createChnftrParser(null);
			inStream = new ByteArrayInputStream(bosStudentInfo.toByteArray());
			parser.loadTemplateStream(inStream);

			//setup page header, pageNum/pageCount
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			ChnftrBuilder b = new ChnftrBuilder();
			ChnftrBuilder.Cell cell = new ChnftrBuilder.Cell(b, 0, 0, 0, 0).setFontAndSize(gsi.fontPt, gsi.engFont, gsi.chnFont);
			cell.addItem(new ChnftrBuilder.TextItem(0, 0))
				.setAlign(PdfContentByte.ALIGN_CENTER, gsi.docWidth)
				.setFontSize(16)
				.setText("Student Record");
			cell.addItem(new ChnftrBuilder.TextItem(0, 4))
				.setAlign(PdfContentByte.ALIGN_RIGHT, gsi.docWidth)
				.setText(String.format("%s (%s)", cellValue("essd_name"), cellValue("essd_sdno")));
			cell.addItem(new ChnftrBuilder.TextItem(0, gsi.docHeight - parser.getChnftrPx(gsi.fontPt))
				.setAlign(PdfContentByte.ALIGN_RIGHT, gsi.docWidth)
				.setText(String.format("%s   Page ${pagenum} of ${pagecount}", sdf.format(new Date()))));
			cell.build();
			parser.setTemplatePageChnftrText(b.toString());

			//print ChnftrParser stream
			outStream = new ByteArrayOutputStream();
			parser.print(outStream);
			outStream.close();
			ZkUtil.showPdfDialog(((Button) jxAdd("btGeneratePdf").getNativeObject()).getRoot(), sessionHelper, outStream.toByteArray(), buildPdfFilename("STUDENT_"));
		}
		catch (Exception e) {
			e.printStackTrace();
			ZkUtil.showErrMsg("Error: %s", e.getMessage());
		} 
		finally {
			try {
				if (inStream != null)
					inStream.close();
				if (outStream != null)
					outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class PdfGenerateStudentInfo {
		final Rectangle pageSize = PageSize.A4;
		final float docWidthPx = pageSize.getWidth() - ChnftrParser.dpi100ToPx(100);
		final float docHeightPx = pageSize.getHeight() - ChnftrParser.dpi100ToPx(100);
		final int docWidth = ChnftrParser.pxToDpi100(docWidthPx);
		final int docHeight = ChnftrParser.pxToDpi100(docHeightPx);
		final int fontPt = 10;
		final String engFont = "helv_nr";
		final String chnFont = "chinese";
		final int rowHeight = 20;
		
		final int docLeft = 5;
		final int docTop = 50; 
		final int contentWidth = docWidth - docLeft * 2;
		final int maxHeight = docHeight - rowHeight;

		boolean isPrintPersonalInfo, isPrintAssessment, isHideEmptyAssessmentItem, isAlternateRowColor;
		ChnftrBuilder builder = new ChnftrBuilder();
		ChnftrBuilder.Cell currCell;
		int offset;

		PdfGenerateStudentInfo(boolean isPrintPersonalInfo, boolean isPrintAssessment, boolean isHideEmptyAssessmentItem, boolean isAlternateRowColor) {
			this.isPrintPersonalInfo = isPrintPersonalInfo;
			this.isPrintAssessment = isPrintAssessment;
			this.isHideEmptyAssessmentItem = isHideEmptyAssessmentItem;
			this.isAlternateRowColor = isAlternateRowColor;
			UniLog.log1("docWidthPx:%f, docHeightPx:%f, docWidth:%d, docHeight:%d, fontHeight:%d", docWidthPx, docHeightPx, docWidth, docHeight, ChnftrParser.pxToDpi100(fontPt));
		}

		ChnftrParser createChnftrParser(InputStream inStream) throws Exception {
			return new ChnftrParser(inStream, pageSize, docWidthPx, docHeightPx, ChnftrParser.CHNFTR_DPI, 11, 6);
		}

		boolean offset(int o, int height) throws Exception {
			offset += o;
			if (currCell.getAbsoluteY() + offset + height > maxHeight) {
				currCell.build();
				builder.P();
				currCell = new ChnftrBuilder.Cell(builder, docLeft, docTop, 0, 0).setFontAndSize(fontPt, engFont, chnFont);
				offset = 0;
				return true;
			}
			return false;
		}

		boolean offset(int o) throws Exception {
			return offset(o, rowHeight);
		}

		boolean offset() throws Exception {
			return offset(rowHeight);
		}
		
		void addAlternateRowColorItem(int i, int height) {
			if (isAlternateRowColor) {
				if (i % 2 == 0)
					currCell.addItem(new ChnftrBuilder.ColorItem().setColor(0xd3, 0xd3, 0xd3));
				else
					currCell.addItem(new ChnftrBuilder.ColorItem().setColor(0xf3, 0xf3, 0xf3));
				currCell.addItem(new ChnftrBuilder.SolidItem(-docLeft, offset - 4).setRect(0, 0, docWidth, height));
				currCell.addItem(new ChnftrBuilder.ColorItem().setColor(0, 0, 0));
			}
		}

		void addAlternateRowColorItem(int i) {
			addAlternateRowColorItem(i, rowHeight);
		}

		void printPersonalInfo() throws Exception {
			currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setB().setU().setText("Personal Information");

			String[] labels = new String[] {
				"Student Name:", "Student Code:", "Student Card Number:", "Status:",
				"Contact Person:", "Last Contact Time:", "Last Reply Time:", "Registration Date:",
				"Sex:", "Birth Date:", "Age:", "Email:",
				"Mobile:", "Home Tel:", "Guardian Name #1:", "Guardian Tel #1:",
				"Guardian Name #2:", "Guardian Tel #2:", "Referral:", "Highest Education:",
				"Apply for Chinese:", "Apply for English:", "Apply for Mathematics:"
			};
			String[] fieldNames = new String[] {
				"essd_name", "essd_sdno", "essd_cardno", "essd_status",
				"essd_contactby", "essd_lastcontime", "essd_lastrpytime", "essd_regdate",
				"essd_sex", "essd_birthdate", "essd_age", "essd_sdemail",
				"essd_sdtel", "essd_hometel", "essd_guardian1", "essd_guardiantel1",
				"essd_guardian2", "essd_guardiantel2", "essd_regreferral", "essd_education",
				"essd_clum0", "essd_clum1", "essd_clum2"
			};
			int count = 0;
			for (int i = 0; i < labels.length; i++) {
				String label = labels[i];
				String fieldName = fieldNames[i];
				String value = cellValue(fieldName);
				if (fieldName.equals("essd_sex")) {
					if (value.equals("1"))
						value = "Male";
					else if (value.equals("2"))
						value = "Female";
					else
						value = "N/A";
				}
				if (fieldName.equals("essd_age")
						|| fieldName.equals("essd_hometel")
						|| fieldName.equals("essd_guardiantel1")
						|| fieldName.equals("essd_guardiantel2")) {
					currCell.addItem(new ChnftrBuilder.TextItem(440, offset)).setText(label);
					currCell.addItem(new ChnftrBuilder.TextItem(550, offset)).setText(value);
					continue;
				}
				offset();
				addAlternateRowColorItem(count);
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(label);
				currCell.addItem(new ChnftrBuilder.TextItem(150, offset)).setText(value);
				count++;
			}

			List<String> ss = ChnftrParser.splitText(cellValue("essd_comments"), engFont, chnFont, fontPt, contentWidth);
			offset();
			addAlternateRowColorItem(count);
			currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText("Comments and Remarks:");
			for (String s : ss) {
				offset();
				addAlternateRowColorItem(count);
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(s);
			}
			count++;
			offset(rowHeight * 2);
		}

		void printAccessment() throws Exception {
			String[] headers = new String[] {
				"Assessment - PN-K",
				"Assessment - K(Phonics)",
				"Assessment - P1",
				"Assessment - P2",
				"Assessment - P3",
				"Assessment - P4",
				"Assessment - P5 & P6",
			};
			String[] idPrefixs = new String[] {
				"pk", "kp", "p1", "p2", "p3", "p4", "p5"
			};
			String[] pk008Opts = new String[] {
				"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
			};
			String[] pk024Opts = new String[] {
				"Circle", "Square", "Triangle", "Rectangle", "Oval", "Rhombus", "Heart", "Star"
			};
			String[] pk025Opts = new String[] {
			   	"Red", "Orange", "Yellow", "Green", "Blue", "Purple", "Pink", "Brown", "Black", "White"
			};
			String[] pk026Opts = new String[] {
				"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"
			};
			class AssOption {
				private String label;
				private String[] options;
				public AssOption(String label, String[] options) {
					this.label = label;
					this.options = options;
				}
				public AssOption(String label) {
					this.label = label;
				}
			}
			List<AssOption> lmPk = new ArrayList<AssOption>();
			lmPk.add(new AssOption("Knowledge of alphabets - identifies only upper case"));
			lmPk.add(new AssOption("Knowledge of alphabets - identifies both upper and lower case"));
			lmPk.add(new AssOption("Knowledge of alphabets - knowledge of writing upper case"));
			lmPk.add(new AssOption("Knowledge of alphabets - knowledge of writing lower case"));
			lmPk.add(new AssOption("Knowledge of alphabets - cannot write but can mimic"));
			lmPk.add(new AssOption("Knowledge of alphabets - cannot write but can trace"));
			lmPk.add(new AssOption("Knowledge of alphabets - mirror images: (please specify)"));
			lmPk.add(new AssOption("Letter sounds recognition:", pk008Opts));
			lmPk.add(new AssOption("Idea of writing - Capable to draw: straight lines"));
			lmPk.add(new AssOption("Idea of writing - Capable to draw: zigzag lines"));
			lmPk.add(new AssOption("Idea of writing - Capable to draw: crisscross lines i.e. \"X\""));
			lmPk.add(new AssOption("Idea of writing - Capable to draw: circles"));
			lmPk.add(new AssOption("Idea of writing - Capable to draw: squares"));
			lmPk.add(new AssOption("Idea of writing - Capable to draw: triangles"));
			lmPk.add(new AssOption("Name writing - writes name correctly"));
			lmPk.add(new AssOption("Name writing - writes name using all uppercase"));
			lmPk.add(new AssOption("Name writing - writes some letters of name"));
			lmPk.add(new AssOption("Name writing - writes using letter-like symbols"));
			lmPk.add(new AssOption("Name writing - scribbles or repetitive"));
			lmPk.add(new AssOption("Motor skills - Holds pencil/marker correctly"));
			lmPk.add(new AssOption("Motor skills - Fits small items together"));
			lmPk.add(new AssOption("Patterns - identifies same and different"));
			lmPk.add(new AssOption("Patterns - identifies patterns"));
			lmPk.add(new AssOption("Patterns - identifies shapes:", pk024Opts));
			lmPk.add(new AssOption("Colour identification:", pk025Opts));
			lmPk.add(new AssOption("Recognition of numerals:", pk026Opts));
			List<AssOption> lmKp = new ArrayList<AssOption>();
			lmKp.add(new AssOption("Able to recognize all capital letters"));
			lmKp.add(new AssOption("Able to recognize all small letters"));
			lmKp.add(new AssOption("Able to write all letters"));
			lmKp.add(new AssOption("Mirror image"));
			lmKp.add(new AssOption("Able to recognize beginning consonants"));
			lmKp.add(new AssOption("Able to recognize ending sounds"));
			lmKp.add(new AssOption("Able to read CVC words"));
			lmKp.add(new AssOption("Able to spell CVC words"));
			lmKp.add(new AssOption("Able to read words with digraphs."));
			lmKp.add(new AssOption("Able to spell words with digraphs."));
			lmKp.add(new AssOption("Able to read words with long vowels"));
			lmKp.add(new AssOption("Able to spell words with long vowels"));
			lmKp.add(new AssOption("Able to read words with naughty y"));
			lmKp.add(new AssOption("Able to spell words with naughty y"));
			lmKp.add(new AssOption("Able to read words with magic e"));
			lmKp.add(new AssOption("Able to spell words with magic e"));
			lmKp.add(new AssOption("Able to read words with silent sounds"));
			lmKp.add(new AssOption("Able to spell words with silent sounds"));
			lmKp.add(new AssOption("Able to read a short passage"));
			lmKp.add(new AssOption("Able to write short sentences"));
			lmKp.add(new AssOption("Able to write a short passage"));
			List<AssOption> lmP1 = new ArrayList<AssOption>();
			lmP1.add(new AssOption("Able to comprehend a short passage"));
			lmP1.add(new AssOption("Able to write a short passage"));
			lmP1.add(new AssOption("Able to apply present tense"));
			lmP1.add(new AssOption("Able to apply the usage of verb to \"be\""));
			lmP1.add(new AssOption("Able to apply the usage of verb to \"have\""));
			lmP1.add(new AssOption("Able to understand plural and singular nouns"));
			lmP1.add(new AssOption("Able to apply present continuous tense"));
			lmP1.add(new AssOption("Able to apply future tense"));
			lmP1.add(new AssOption("Able to apply past tense"));
			List<AssOption> lmP2 = new ArrayList<AssOption>();
			lmP2.add(new AssOption("Able to comprehend fiction"));
			lmP2.add(new AssOption("Able to comprehend non-fiction"));
			lmP2.add(new AssOption("Able to write a piece of story (around 100 words)"));
			lmP2.add(new AssOption("Able to apply present tense"));
			lmP2.add(new AssOption("Able to apply the usage of verb to \"be\""));
			lmP2.add(new AssOption("Able to apply the usage of verb to \"have\""));
			lmP2.add(new AssOption("Able to understand plural and singular nouns"));
			lmP2.add(new AssOption("Able to apply present continuous tense"));
			lmP2.add(new AssOption("Able to apply future tense"));
			lmP2.add(new AssOption("Able to apply past tense"));
			lmP2.add(new AssOption("Able to apply past continuous tense"));
			lmP2.add(new AssOption("Able to relate cause and effect"));
			lmP2.add(new AssOption("Able to do inference in reading"));
			List<AssOption> lmP3 = new ArrayList<AssOption>();
			lmP3.add(new AssOption("Able to comprehend fiction"));
			lmP3.add(new AssOption("Able to comprehend non-fiction"));
			lmP3.add(new AssOption("Able to write a piece of story (around 200 words)"));
			lmP3.add(new AssOption("Able to apply present tense"));
			lmP3.add(new AssOption("Able to understand plural and singular nouns"));
			lmP3.add(new AssOption("Able to apply present continuous tense"));
			lmP3.add(new AssOption("Able to apply future tense"));
			lmP3.add(new AssOption("Able to apply past tense"));
			lmP3.add(new AssOption("Able to apply past continuous tense"));
			lmP3.add(new AssOption("Able to relate cause and effect"));
			lmP3.add(new AssOption("Able to do inference in reading"));
			lmP3.add(new AssOption("Able to do compare and contrast"));
			List<AssOption> lmP4 = new ArrayList<AssOption>();
			lmP4.add(new AssOption("Able to comprehend fiction"));
			lmP4.add(new AssOption("Able to comprehend non-fiction"));
			lmP4.add(new AssOption("Able to write a piece of persuasive writing (around 300 words)"));
			lmP4.add(new AssOption("Able to apply present tense"));
			lmP4.add(new AssOption("Able to apply conjunctions"));
			lmP4.add(new AssOption("Able to apply present continuous tense"));
			lmP4.add(new AssOption("Able to apply future tense"));
			lmP4.add(new AssOption("Able to apply past tense"));
			lmP4.add(new AssOption("Able to apply past continuous tense"));
			lmP4.add(new AssOption("Able to apply present perfect tense"));
			lmP4.add(new AssOption("Able to apply past perfect tense"));
			lmP4.add(new AssOption("Able to relate cause and effect"));
			lmP4.add(new AssOption("Able to do inference in reading"));
			lmP4.add(new AssOption("Able to do compare and contrast"));
			lmP4.add(new AssOption("Able to identify different genres"));
			lmP4.add(new AssOption("Able to comprehend poems"));
			List<AssOption> lmP5 = new ArrayList<AssOption>();
			lmP5.add(new AssOption("Able to comprehend fiction"));
			lmP5.add(new AssOption("Able to comprehend non-fiction"));
			lmP5.add(new AssOption("Able to write a piece of persuasive writing (around 300 words)"));
			lmP5.add(new AssOption("Able to summarize a text"));
			lmP5.add(new AssOption("Able to apply present tense"));
			lmP5.add(new AssOption("Able to apply conjunctions"));
			lmP5.add(new AssOption("Able to apply present continuous tense"));
			lmP5.add(new AssOption("Able to apply future tense"));
			lmP5.add(new AssOption("Able to apply past tense"));
			lmP5.add(new AssOption("Able to apply past continuous tense"));
			lmP5.add(new AssOption("Able to apply present perfect tense"));
			lmP5.add(new AssOption("Able to apply conditional sentences"));
			lmP5.add(new AssOption("Able to apply question tags"));
			lmP5.add(new AssOption("Able to apply past perfect tense"));
			lmP5.add(new AssOption("Able to relate cause and effect"));
			lmP5.add(new AssOption("Able to do inference in reading"));
			lmP5.add(new AssOption("Able to do compare and contrast"));
			lmP5.add(new AssOption("Able to identify different genres"));
			lmP5.add(new AssOption("Able to comprehend poems"));
			List<AssOption>[] lms = new List[] {
				lmPk, lmKp, lmP1, lmP2, lmP3, lmP4, lmP5
			};

			String jsonStr = cellValue("essdx_jsonstr");
			JSONObject json = new JSONObject(jsonStr);
			for (int i = 0; i < lms.length; i++) {
				List<AssOption> lm = lms[i];
				boolean printedHeader = false;
				int count = 0;
				for (int j = 0; j < lm.size(); j++) {
					AssOption ao = lm.get(j);
					if (ao.options != null) {
						StringBuilder sb = new StringBuilder("");
						for (int k = 0; k < ao.options.length; k++) {
							String key = String.format("%s%03d_%d", idPrefixs[i], j + 1, k + 1);
							boolean bValue = json.optBoolean(key);
							String sValue = ao.options[k];
							if (bValue) {
								if (sb.length() > 0)
									sb.append(", ");
								sb.append(sValue);
							}
						}
						if (isHideEmptyAssessmentItem && sb.length() == 0)
							continue;
						if (!printedHeader) {
							currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setB().setU().setText(headers[i]);
							printedHeader = true;
						}
						sb.insert(0, "[ ");
						sb.append(" ]");
						offset(rowHeight, rowHeight * 2);
						addAlternateRowColorItem(count, rowHeight * 2);
						currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(String.format("%02d.", j + 1));
						currCell.addItem(new ChnftrBuilder.TextItem(25, offset)).setText(ao.label);
						offset();
						currCell.addItem(new ChnftrBuilder.TextItem(25, offset)).setText(sb.toString());
						count++;
					}
					else {
						String key = String.format("%s%03d", idPrefixs[i], j + 1);
						int iValue = json.optInt(key);
						String sValue;
						switch (iValue) {
						case 1:
							sValue = "Y";
							break;
						case 2:
							sValue = "N";
							break;
						default:
							sValue = "NA";
							break;
						}
						if (isHideEmptyAssessmentItem && sValue.equals("NA"))
							continue;
						if (!printedHeader) {
							currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setB().setU().setText(headers[i]);
							printedHeader = true;
						}
						offset();
						addAlternateRowColorItem(count);
						currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(String.format("%02d.", j + 1));
						currCell.addItem(new ChnftrBuilder.TextItem(25, offset)).setText(ao.label);
						final int l = 35;
						currCell.addItem(new ChnftrBuilder.TextItem(contentWidth - l, offset)).setText("[");
						currCell.addItem(new ChnftrBuilder.TextItem(contentWidth - l, offset)).setAlign(PdfContentByte.ALIGN_RIGHT, l).setText("]");
						currCell.addItem(new ChnftrBuilder.TextItem(contentWidth - l, offset)).setAlign(PdfContentByte.ALIGN_CENTER, l).setText(sValue);
						count++;
					}
				}
				if (printedHeader)
					offset(rowHeight * 2);
			}
		}

		ByteArrayOutputStream build() throws Exception {
			ByteArrayInputStream inStream = null;
			ByteArrayOutputStream outStream = null;
			try {
				currCell = new ChnftrBuilder.Cell(builder, docLeft, docTop, 0, 0).setFontAndSize(fontPt, engFont, chnFont);

				if (isPrintPersonalInfo)
					printPersonalInfo();
				if (isPrintAssessment)
					printAccessment();

				//builder write to stream
				currCell.build();
				outStream = new ByteArrayOutputStream();
				builder.writeTo(outStream);
				outStream.close();

				//ChnftrParser read builder stream
				inStream = new ByteArrayInputStream(outStream.toByteArray());
				ChnftrParser parser = createChnftrParser(inStream);

				//ChnftrParser write to stream
				outStream = new ByteArrayOutputStream();
				parser.print(outStream);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			finally {
				try {
					if (inStream != null)
						inStream.close();
					if (outStream != null)
						outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return outStream;
		}
	}

	private String cellValue(String cellLabel) {
		return getBr().getCell(cellLabel).getString();
	}

	private String buildPdfFilename(String prefix){
		StringBuilder sb = new StringBuilder();
		sb.append(prefix);
		sb.append(cellValue("essd_sdno"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		sb.append("_");
		sb.append(sdf.format(new Date()));
		sb.append(".pdf");
		return sb.toString();
	}
	
	
	public static Triple<Integer, Integer, Integer> updateAttendance(SessionHelper sessionHelper, BiResult p_brAttendance, RpcClient rpc, 
									Map<Integer, String> studentMap, String newStatus, Date pStartDateTime, Date pEndDateTime, boolean scanStudentCardMode) throws Exception {
		BiResult brCourseSession = null;
		BiResult brCourseStudent = null;
		BiResult brCourse = null;
		BiResult brQAttendance = null;
		BiResult brAttendance = p_brAttendance;
		int addCount = 0;
		int updateCount = 0;
		int removeCount = 0;
		try {
			brCourseSession = sessionHelper.newBiResult("edu.CourseSessionDet");
			brCourseStudent = sessionHelper.newBiResult("edu.CourseStudent");
			brCourse = sessionHelper.newBiResult("edu.Course");

			//find course session records
			SimpleDateFormat dsdf = new SimpleDateFormat("yyyy/MM/dd");
			Set<String> deductTokenStatusList = Sets.newHashSet("Present", "Absent");
			Map<Integer, Map<String, Object>> mSessionMap = new LinkedHashMap<Integer, Map<String, Object>>();
			brCourseSession.addCustomCondition(String.format("essncs_date between '%s' and '%s'", 
					dsdf.format(pStartDateTime),
					dsdf.format(pEndDateTime)
					));
			ReturnMsg rtn;
			if ((rtn = brCourseSession.query(true, false)).getStatus()) {
				while (brCourseSession.next()) {
					int courseRg = brCourseSession.getCellInt("essncs_avrg");
					int sessionRg = brCourseSession.getCellInt("essncs_rg");
					Date sessionDate = brCourseSession.getCellDate("essncs_date");
					Date startTime = brCourseSession.getCellDate("essncs_sttime");
					Date endTime = brCourseSession.getCellDate("essncs_endtime");
					String status = brCourseSession.getCellString("essncs_status");
					String remark = brCourseSession.getCellString("essncs_name");
					UniLog.log1("found session courseRg:%d, sessionRg:%d, sessionDate:%s, startTime:%s, endTime:%s, remark:%s, status:%s", courseRg, sessionRg, sessionDate, startTime, endTime, remark, status);
					
					Date startDateTime = unionDateTime(sessionDate, startTime);
					Date endDateTime = unionDateTime(sessionDate, endTime);
					UniLog.log1("startDateTime:%s, endDateTime:%s, pStartDateTime:%s, pEndDateTime:%s", startDateTime, endDateTime, pStartDateTime, pEndDateTime);
					if (startDateTime.compareTo(pEndDateTime) < 0 && endDateTime.compareTo(pStartDateTime) > 0) {
						Map<String, Object> map = new HashMap<String, Object>();
						map.put("courseRg", courseRg);
						map.put("sessionDate", sessionDate);
						map.put("startDateTime", startDateTime);
						map.put("endDateTime", endDateTime);
						map.put("studentAttMap", new HashMap<Integer, String>());
						mSessionMap.put(sessionRg, map);
						UniLog.log("mSessionMap put");
					}
				}
			}
			else
				throw new Exception(rtn.getMsg());
			
			brQAttendance = sessionHelper.newBiResult("edu.Attendance");
			if (brAttendance == null) {
				brAttendance = sessionHelper.newBiResult("edu.Attendance");
				brAttendance.beginWork();
			}
			if (rpc == null)
				rpc = brAttendance.getSelectUtil().getRpcClient();
			for (int studentRg : studentMap.keySet()) {
				//find student course session records 
				Map<Integer, Map<String, Object>> sessionMap = new LinkedHashMap<Integer, Map<String, Object>>();
				Map<Integer, Map<String, Object>> courseMap = new HashMap<Integer, Map<String, Object>>();
				for (Map.Entry<Integer, Map<String, Object>> entry : mSessionMap.entrySet()) {
					int sessionRg = entry.getKey();
					Map<String, Object> mOldMap = entry.getValue();
					int courseRg = (Integer)mOldMap.get("courseRg");
					Map<String, Object> cNewMap = courseMap.get(courseRg);
					if (cNewMap == null) {
						cNewMap = new HashMap<String, Object>();
						Map<String, Object> sNewMap = new HashMap<String, Object>(entry.getValue());
						courseMap.put(courseRg, cNewMap);
						cNewMap.put("isStudentInList", false);
						brCourseStudent.clearCondition();
						brCourseStudent.addCustomCondition(String.format("essbsd_avrg = %d and essbsd_sdrg = %d", courseRg, studentRg));
						if ((rtn = brCourseStudent.query(true, false)).getStatus()) {
							if (brCourseStudent.next()) {
								Date sessionDate = (Date)sNewMap.get("sessionDate");
								Date startDate = brCourseStudent.getCellDate("essbsd_startdate");
								Date endDate = brCourseStudent.getCellDate("essbsd_enddate");
								String studentStatus = brCourseStudent.getCellString("essd_status");
								String subStatus = brCourseStudent.getCellString("essbsd_status");
								cNewMap.put("startDate", startDate);
								cNewMap.put("endDate", endDate);
								cNewMap.put("studentStatus", studentStatus);
								cNewMap.put("subStatus", subStatus);
								cNewMap.put("isStudentInList", true);
								sNewMap.put("studentStatus", studentStatus);
								sNewMap.put("subStatus", subStatus);
								sNewMap.put("isInEnrolledList", sessionDate != null && !DateUtil.isDateNull(startDate) && !DateUtil.isDateNull(endDate) 
												&& sessionDate.compareTo(startDate) >= 0 && sessionDate.compareTo(endDate) <= 0);
								sessionMap.put(sessionRg, sNewMap);
							}
						}
						else
							throw new Exception(rtn.getMsg());
						brCourse.clearCondition();
						brCourse.addCustomCondition(String.format("eaav0_rg = %d", courseRg));
						if ((rtn = brCourse.query(true, false)).getStatus()) {
							if (brCourse.next()) {
								cNewMap.put("sessionFee", brCourse.getCellDouble("eaav0_fee"));
								cNewMap.put("ccy", brCourse.getCellString("eaav0_tokenccy"));
								sNewMap.put("courseRg", courseRg);
								sNewMap.put("sessionFee", (Double)cNewMap.get("sessionFee"));
								sNewMap.put("ccy", (String)cNewMap.get("ccy"));
								mOldMap.put("sessionFee", (Double)cNewMap.get("sessionFee"));
								mOldMap.put("ccy", (String)cNewMap.get("ccy"));
							}
							else
								throw new Exception(String.format("Course %d not found", courseRg));
						}
						else
							throw new Exception(rtn.getMsg());
					}
					else if ((Boolean)cNewMap.get("isStudentInList")) {
						Map<String, Object> sNewMap = new HashMap<String, Object>(entry.getValue());
						Date sessionDate = (Date)sNewMap.get("sessionDate");
						Date startDate = (Date)cNewMap.get("startDate");
						Date endDate = (Date)cNewMap.get("endDate");
						sNewMap.put("courseRg", courseRg);
						sNewMap.put("sessionFee", (Double)cNewMap.get("sessionFee"));
						sNewMap.put("ccy", (String)cNewMap.get("ccy"));
						mOldMap.put("sessionFee", (Double)cNewMap.get("sessionFee"));
						mOldMap.put("ccy", (String)cNewMap.get("ccy"));
						sNewMap.put("studentStatus", (String)cNewMap.get("studentStatus"));
						sNewMap.put("subStatus", (String)cNewMap.get("subStatus"));
						sNewMap.put("isInEnrolledList", sessionDate != null && !DateUtil.isDateNull(startDate) && !DateUtil.isDateNull(endDate) 
										&& sessionDate.compareTo(startDate) >= 0 && sessionDate.compareTo(endDate) <= 0);
						sessionMap.put(sessionRg, sNewMap);
					}
				}

				//find student attendance records
				Map<Integer, Map<String, Object>> eSnRgMap = new LinkedHashMap<Integer, Map<String, Object>>();
				for (int sessionRg : sessionMap.keySet()) {
					brQAttendance.clearCondition();
					brQAttendance.addCustomCondition(String.format("esatsd_snrg = %d and esatsd_atrg = %d", sessionRg, studentRg));
					if ((rtn = brQAttendance.query(true, false)).getStatus()) {
						if (brQAttendance.next()) {
							String actStatus = brQAttendance.getCellString("esatsd_status");
							Date actStartTime = brQAttendance.getCellDate("esatsd_sttime");
							Date actEndTime = brQAttendance.getCellDate("esatsd_endtime");
							UniLog.log1("found attendance sessionRg:%d, actStartTime:%s, actEndTime:%s", sessionRg, actStartTime, actEndTime);
							Map<String, Object> m = new HashMap<String, Object>();
							m.put("attStatus", actStatus);
							m.put("attStartDateTime", actStartTime);
							m.put("attEndDateTime", actEndTime);
							m.put("deleted", false);
							eSnRgMap.put(sessionRg, m);
						}
					}
					else
						throw new Exception(rtn.getMsg());
				}
				
				//remove student attendance records
				for (Map.Entry<Integer, Map<String, Object>> m : eSnRgMap.entrySet()) {
					int esnRg = m.getKey();
					Map<String, Object> em = m.getValue();
					Map<String, Object> sm = sessionMap.get(esnRg);
					if (!StringUtils.equals((String)sm.get("studentStatus"), "Cancelled")) {
						brAttendance.getSelectUtil().executeUpdate("delete from esattendance", 
							new Wherecl().andUniop("esat_snrg", "=", esnRg)
										.andUniop("esat_attype", "=", "SD")
										.andUniop("esat_atrg", "=", studentRg).stripAnd()
						);
						em.put("deleted", true);
					}
					else
						em.put("deleted", false);
				}
				

				if (newStatus != null) {
					//add student attendance records
					for (Map.Entry<Integer, Map<String, Object>> entry : sessionMap.entrySet()) {
						int sessionRg = entry.getKey();
						Map<String, Object> map = entry.getValue();
						Map<String, Object> em = eSnRgMap.get(sessionRg);
						if (StringUtils.equals((String)map.get("studentStatus"), "Cancelled"))
							continue;
						
						//apply for the session within subscription date range or existing attendance record
						if (em == null) {
							//add
							boolean canAdd = false;
							if (scanStudentCardMode) {
								//No need to check the subscription date anymore.
								//But need to check the subscription status <> cancelled. 
								if (!StringUtils.equals((String)map.get("subStatus"), "Cancelled"))
									canAdd = true;
							}
							else {
								if ((Boolean)map.get("isInEnrolledList") || StringUtils.equals((String)map.get("subStatus"), "Reserve"))
									canAdd = true;
							}
							if (canAdd) {
								brAttendance.clearCurrentRec();
								brAttendance.getCell("esatsd_snrg").set(sessionRg);
								brAttendance.getCell("esatsd_attype").set("SD");
								brAttendance.getCell("esatsd_atrg").set(studentRg);
								brAttendance.getCell("esatsd_status").set(newStatus);
								brAttendance.getCell("esatsd_sttime").set((Date)map.get("startDateTime"));
								brAttendance.getCell("esatsd_endtime").set((Date)map.get("endDateTime"));
								rtn = brAttendance.addCurrent();
								if (!rtn.getStatus())
									throw new Exception(rtn.getMsg());
								((HashMap<Integer, String>)mSessionMap.get(sessionRg).get("studentAttMap")).put(studentRg, newStatus);
								addCount++;
							}
						}
						else {
							//update
							if ((Boolean)em.get("deleted")) {
								brAttendance.clearCurrentRec();
								brAttendance.getCell("esatsd_snrg").set(sessionRg);
								brAttendance.getCell("esatsd_attype").set("SD");
								brAttendance.getCell("esatsd_atrg").set(studentRg);
								brAttendance.getCell("esatsd_status").set(newStatus);
								brAttendance.getCell("esatsd_sttime").set((Date)map.get("startDateTime"));
								brAttendance.getCell("esatsd_endtime").set((Date)map.get("endDateTime"));
								rtn = brAttendance.addCurrent();
								if (!rtn.getStatus())
									throw new Exception(rtn.getMsg());
								((HashMap<Integer, String>)mSessionMap.get(sessionRg).get("studentAttMap")).put(studentRg, newStatus);
								updateCount++;
								eSnRgMap.remove(sessionRg);
							}
						}
					}
				}
				
				for (Map.Entry<Integer, Map<String, Object>> entry : eSnRgMap.entrySet()) {
					int sessionRg = entry.getKey();
					Map<String, Object> em = entry.getValue();
					if ((Boolean)em.get("deleted")) {
						((HashMap<Integer, String>)mSessionMap.get(sessionRg).get("studentAttMap")).put(studentRg, null);
						removeCount++;
						//deleted
					}
				}
			}


			//recalTokenBal(sessionHelper, rpc, studentRgList, pStartDateTime, pEndDateTime, false, true);
			//rpccall
			UniLog.log("start rpccall");
			for (Map.Entry<Integer, Map<String, Object>> entry : mSessionMap.entrySet()) {
				int sessionRg = entry.getKey();
				Map<String, Object> m = entry.getValue();
				int courseRg = (Integer)m.get("courseRg");
				HashMap<Integer, String> sdAttMap = (HashMap<Integer, String>)m.get("studentAttMap");
				if (sdAttMap.isEmpty())
					continue;
				String tokenCcy = (String)m.get("ccy");
				double sessionFee = (Double)m.get("sessionFee");

				/*Vector args = new Vector();
				args.add(courseRg);
				args.add(sessionRg);
				for (Map.Entry<Integer, String> entry1 : sdAttMap.entrySet()) {
					int studentRg = entry1.getKey();
					String attStatus = entry1.getValue();
					double fee = (attStatus != null && deductTokenStatusList.contains(attStatus)) ? sessionFee : 0.0;
					args.add(studentRg);
					args.add(tokenCcy);
					args.add(fee);
					UniLog.log1("rpccall sessionRg:%d, courseRg:%d, studentRg:%d, ccy:%s, fee:%f", sessionRg, courseRg, studentRg, tokenCcy, fee);
				}
				Value value = rpc.callSegment("token_addUpdateCourseAttendMulti", args);
				if (value == null)
					throw new Exception("rpccall failed");
				if (!StringUtils.startsWith(value.toString(), "OK"))
					throw new Exception(StringUtils.startsWith(value.toString(), "FAIL") ? value.toString().substring(4) : value.toString());*/
				for (Map.Entry<Integer, String> entry1 : sdAttMap.entrySet()) {
					int studentRg = entry1.getKey();
					String attStatus = entry1.getValue();
					double fee = (attStatus != null && deductTokenStatusList.contains(attStatus)) ? sessionFee : 0.0;
					Vector args = new Vector();
					args.add(studentRg);
					args.add(sessionRg);
					args.add(courseRg);
					args.add(tokenCcy);
					args.add(fee);
					UniLog.log1("rpccall sessionRg:%d, courseRg:%d, studentRg:%d, ccy:%s, fee:%f", sessionRg, courseRg, studentRg, tokenCcy, fee);
					Value value = rpc.callSegment("token_addUpdateCourseAttend", args);
					if (value == null)
						throw new Exception("rpccall failed");
					if (!StringUtils.startsWith(value.toString(), "OK"))
						throw new Exception(StringUtils.startsWith(value.toString(), "FAIL") ? value.toString().substring(4) : value.toString());
					//need to call the ProcessScanLog.setCSMapDirty() when attendance updated
					ProcessScanLog.setCSMapDirty(studentMap.get(studentRg));
				}
			}

			if (p_brAttendance == null && brAttendance != null)
				brAttendance.commitWork();
		}
		catch (Exception e) {
			e.printStackTrace();
			if (p_brAttendance == null && brAttendance != null) {
				UniLog.log1("brAttendance rollbackWork");
				brAttendance.rollbackWork();
			}
			throw e;
		}
		finally {
			if (brCourseSession != null)
				brCourseSession.close();
			if (brCourseStudent != null)
				brCourseStudent.close();
			if (brCourse != null)
				brCourse.close();
			if (brQAttendance != null)
				brQAttendance.close();
			if (p_brAttendance == null && brAttendance != null)
				brAttendance.close();
		}
		return Triple.of(addCount, updateCount, removeCount);
	}

	public static void recalTokenBal(SessionHelper sessionHelper, RpcClient rpc, Map<Integer, String> studentMap, Date pStartDateTime, Date pEndDateTime, boolean fPayment, boolean fAttendance) throws Exception {
		BiResult brStudentCourse = null;
		BiResult brCourseSession = null;
		BiResult brStudentAttendance = null;
		BiResult brAssessment = null;
		BiResult brPayment = null;
		BiResult brAttendance = null;
		try {
			brStudentCourse = sessionHelper.newBiResult("edu.StudentCourse");
			brCourseSession = sessionHelper.newBiResult("edu.CourseSessionDet");
			brStudentAttendance = sessionHelper.newBiResult("edu.StudentAttendance");
			brAssessment = sessionHelper.newBiResult("edu.Assessment");
			brPayment = sessionHelper.newBiResult("edu.Payment");
			if (rpc == null) {
				brAttendance = sessionHelper.newBiResult("edu.Attendance");
				brAttendance.beginWork();
				rpc = brAttendance.getSelectUtil().getRpcClient();
			}
			UniLog.log1("rpc:%s", rpc);
			
			ReturnMsg rtn;
			if (fPayment) {
				for (Map.Entry<Integer, String> entry : studentMap.entrySet()) {
					int studentRg = entry.getKey();
					String cardNo = entry.getValue();
					brPayment.clearCondition();
					brPayment.addCustomCondition(String.format("esph_sdrg = %d", studentRg));
					if ((rtn = brPayment.query(true, false)).getStatus()) {
						while (brPayment.next(false)) {
							int paymentRg = brPayment.getCellInt("esph_rg");
							Vector args = new Vector();
							args.add(studentRg);
							args.add(paymentRg);
							int size0 = args.size();
							BiResult brPaymentDet = brPayment.getSubLink("edu.PaymentDet");
							while (brPaymentDet.next()) {
								double amount = brPaymentDet.getCellDouble("espd_amount");
								args.add(0);
								String ccy = brPaymentDet.getCellString("eaav0_tokenccy");
								if (StringUtils.isBlank(ccy))
									ccy = brPaymentDet.getCellString("essnas_tokenccy");  
								if (StringUtils.isBlank(ccy))
									ccy = brPaymentDet.getCellString("espd_tokenccy");  
								args.add(ccy);
								args.add(amount);
								UniLog.log1("rpccall studentRg:%d, paymentRg:%d, ccy:%s, amount:%f", studentRg, paymentRg, ccy, amount);
							}
							if (args.size() > size0) {
								Value value = rpc.callSegment("token_addUpdatePaymentMulti", args);
								if (value == null)
									throw new Exception("rpccall failed");
								if (!StringUtils.startsWith(value.toString(), "OK"))
									throw new Exception(StringUtils.startsWith(value.toString(), "FAIL") ? value.toString().substring(4) : value.toString());
								//need to call the ProcessScanLog.setCSMapDirty() when payment updated
								ProcessScanLog.setCSMapDirty(cardNo);
							}
						}
					}
					else
						throw new Exception(rtn.getMsg());
				}
			}
			if (fAttendance) {
				SimpleDateFormat dsdf = new SimpleDateFormat("yyyy/MM/dd");
				Set<String> deductTokenStatusList = Sets.newHashSet("Present", "Absent");
	
				Map<Integer, Map<String, Object>> rMap = new HashMap<Integer, Map<String, Object>>();
				for (Map.Entry<Integer, String> sdEntry : studentMap.entrySet()) {
					int studentRg = sdEntry.getKey();
					String cardNo = sdEntry.getValue();
					UniLog.log1("studentRg:%d", studentRg);
					Map<Integer, Map<String, Object>> sessionMap = new HashMap<Integer, Map<String, Object>>();
					//student course
					brStudentCourse.clearCondition();
					brStudentCourse.addCustomCondition(String.format("essbsd_sdrg = %d", studentRg));
					if ((rtn = brStudentCourse.query(true, false)).getStatus()) {
						while (brStudentCourse.next()) {
							int courseRg = brStudentCourse.getCellInt("essbsd_avrg");
							String ccy = brStudentCourse.getCellString("eaav0_tokenccy");
							double fee = brStudentCourse.getCellDouble("eaav0_fee");
							UniLog.log1("courseRg:%d, ccy:%s, fee:%f", courseRg, ccy, fee);
		
							brCourseSession.clearCondition();
							brCourseSession.addCustomCondition(String.format("essncs_avrg = %d", courseRg));
							if (!DateUtil.isDateNull(pStartDateTime) && !DateUtil.isDateNull(pEndDateTime)) {
								brCourseSession.addCustomCondition(String.format("essncs_date between '%s' and '%s'", 
										dsdf.format(pStartDateTime), dsdf.format(pEndDateTime)));
							}
							if ((rtn = brCourseSession.query(true, false)).getStatus()) {
								while (brCourseSession.next()) {
									int sessionRg = brCourseSession.getCellInt("essncs_rg");
									String status = brCourseSession.getCellString("essncs_status");
									Date sessionDate = brCourseSession.getCellDate("essncs_date");
									Date startTime = brCourseSession.getCellDate("essncs_sttime");
									Date endTime = brCourseSession.getCellDate("essncs_endtime");
									Date startDateTime = unionDateTime(sessionDate, startTime);
									Date endDateTime = unionDateTime(sessionDate, endTime);
									UniLog.log1("sessionRg:%d, startDateTime:%s, endDateTime:%s", sessionRg, startDateTime, endDateTime);
									if (DateUtil.isDateNull(pStartDateTime) || DateUtil.isDateNull(pEndDateTime) || 
										(startDateTime.compareTo(pEndDateTime) < 0 && endDateTime.compareTo(pStartDateTime) > 0)) {
										Map<String, Object> m = new HashMap<String, Object>();
										m.put("sessionType", 0);
										m.put("sessionStatus", status);
										m.put("courseRg", courseRg);
										m.put("tokenCcy", ccy);
										m.put("sessionFee", fee);
										m.put("attStatus", null);
										sessionMap.put(sessionRg, m);
									}
								}
							}
							else
								throw new Exception(rtn.getMsg());
						}
					}
					else
						throw new Exception(rtn.getMsg());

					//assessment
					brAssessment.clearCondition();
					brAssessment.addCustomCondition(String.format("esatsd_atrg = %d", studentRg));
					if ((rtn = brAssessment.query(true, false)).getStatus()) {
						while (brAssessment.next()) {
							int sessionRg = brAssessment.getCellInt("essnas_rg");
							String status = brAssessment.getCellString("essnas_status");
							String ccy = brAssessment.getCellString("essnas_tokenccy");
							double fee = brAssessment.getCellDouble("essnas_fee");
							Date sessionDate = brAssessment.getCellDate("essnas_date");
							Date startTime = brAssessment.getCellDate("essnas_sttime");
							Date endTime = brAssessment.getCellDate("essnas_endtime");
							Date startDateTime = unionDateTime(sessionDate, startTime);
							Date endDateTime = unionDateTime(sessionDate, endTime);
							UniLog.log1("sessionRg:%d, ccy:%s, fee:%f, startDateTime:%s, endDateTime:%s", sessionRg, ccy, fee, startDateTime, endDateTime);
							if (DateUtil.isDateNull(pStartDateTime) || DateUtil.isDateNull(pEndDateTime) || 
								(startDateTime.compareTo(pEndDateTime) < 0 && endDateTime.compareTo(pStartDateTime) > 0)) {
								Map<String, Object> m = new HashMap<String, Object>();
								m.put("sessionType", 1);
								m.put("sessionStatus", status);
								m.put("courseRg", 0);
								m.put("tokenCcy", ccy);
								m.put("sessionFee", fee);
								m.put("attStatus", null);
								sessionMap.put(sessionRg, m);
							}
						}
					}
					else
						throw new Exception(rtn.getMsg());
	
					//student attendance
					brStudentAttendance.clearCondition();
					brStudentAttendance.addCustomCondition(String.format("esatsd_atrg = %d and essn_type in (0,1)", studentRg));
					if (!DateUtil.isDateNull(pStartDateTime) && !DateUtil.isDateNull(pEndDateTime)) {
						brStudentAttendance.addCustomCondition(String.format("essn_date between '%s' and '%s'", 
								dsdf.format(pStartDateTime), dsdf.format(pEndDateTime)));
					}
					if ((rtn = brStudentAttendance.query(true, false)).getStatus()) {
						while (brStudentAttendance.next()) {
							int sessionRg = brStudentAttendance.getCellInt("esatsd_snrg");
							String attStatus = brStudentAttendance.getCellString("esatsd_status");
							Date attStartDateTime = brStudentAttendance.getCellDate("esatsd_sttime");
							Date attEndDateTime = brStudentAttendance.getCellDate("esatsd_endtime");
							Date sessionDate = brStudentAttendance.getCellDate("essn_date");
							Date startTime = brStudentAttendance.getCellDate("essn_sttime");
							Date endTime = brStudentAttendance.getCellDate("essn_sttime");
							Date startDateTime = unionDateTime(sessionDate, startTime);
							Date endDateTime = unionDateTime(sessionDate, endTime);
							UniLog.log1("attStatus:%s, startDateTime:%s, endDateTime:%s", attStatus, startDateTime, endDateTime);
							if (DateUtil.isDateNull(pStartDateTime) || DateUtil.isDateNull(pEndDateTime) || 
								(startDateTime.compareTo(pEndDateTime) < 0 && endDateTime.compareTo(pStartDateTime) > 0)) {
								Map<String, Object> m = sessionMap.get(sessionRg);
								if (m != null)
									m.put("attStatus", attStatus);
								else {
									//throw new Exception(String.format("Session %d not found", sessionRg));
									UniLog.log1("Student %d no join session %d", studentRg, sessionRg); //should not refund if remove student from course
								}
							}
						}
					}
					else
						throw new Exception(rtn.getMsg());
					
					for (Map.Entry<Integer, Map<String, Object>> entry : sessionMap.entrySet()) {
						int sessionRg = entry.getKey();
						Map<String, Object> m = entry.getValue();
						int sessionType = (Integer)m.get("sessionType");
						String sessionStatus = (String)m.get("sessionStatus");
						int courseRg = (Integer)m.get("courseRg");
						String attStatus = (String)m.get("attStatus");
						String tokenCcy = (String)m.get("tokenCcy");
						double sessionFee = (Double)m.get("sessionFee");
						
						Map<String, Object> rm = rMap.get(sessionRg);
						if (rm == null) {
							rm = new HashMap<String, Object>();
							rm.put("sessionType", sessionType);
							rm.put("sessionStatus", sessionStatus);
							rm.put("courseRg", courseRg);
							rm.put("studentRgList", new HashMap<Integer, String>());
							rm.put("tokenCcy", tokenCcy);
							rm.put("sessionFee", sessionFee);
							rMap.put(sessionRg, rm);
						}
						((HashMap<Integer, String>)rm.get("studentRgList")).put(studentRg, attStatus);
					}
				}
	
				//rpccall
				for (Map.Entry<Integer, Map<String, Object>> entry : rMap.entrySet()) {
					int sessionRg = entry.getKey();
					Map<String, Object> m = entry.getValue();
					int sessionType = (Integer)m.get("sessionType");
					String sessionStatus = (String)m.get("sessionStatus");
					int courseRg = (Integer)m.get("courseRg");
					HashMap<Integer, String> sdList = (HashMap<Integer, String>)m.get("studentRgList");
					String tokenCcy = (String)m.get("tokenCcy");
					double sessionFee = (Double)m.get("sessionFee");

					for (Map.Entry<Integer, String> entry1 : sdList.entrySet()) {
						int studentRg = entry1.getKey();
						String attStatus = entry1.getValue();
						double fee;
						switch (sessionType) {
							case 0:
								fee = (attStatus != null && deductTokenStatusList.contains(attStatus)) ? sessionFee : 0.0;
								break;
							case 1:
								if (!StringUtils.equals(sessionStatus, "Normal"))
									continue;
								fee = (attStatus != null && deductTokenStatusList.contains(attStatus)) ? sessionFee : 0.0;
								break;
							default:
								fee = 0.0;
								break;
						}
						Vector args = new Vector();
						args.add(studentRg);
						args.add(sessionRg);
						args.add(courseRg);
						args.add(tokenCcy);
						args.add(fee);
						UniLog.log1("rpccall sessionType:%d, sessionRg:%d, attStatus:%s, courseRg:%d, studentRg:%d, ccy:%s, fee:%f", sessionType, sessionRg, attStatus, courseRg, studentRg, tokenCcy, fee);
						Value value = rpc.callSegment("token_addUpdateCourseAttend", args);
						if (value == null)
							throw new Exception("rpccall failed");
						if (!StringUtils.startsWith(value.toString(), "OK"))
							throw new Exception(StringUtils.startsWith(value.toString(), "FAIL") ? value.toString().substring(4) : value.toString());
						//need to call the ProcessScanLog.setCSMapDirty() when attendance updated
						ProcessScanLog.setCSMapDirty(studentMap.get(studentRg));
					}
				}
			}

			if (brAttendance != null)
				brAttendance.commitWork();
		}
		catch (Exception e) {
			e.printStackTrace();
			if (brAttendance != null) {
				brAttendance.rollbackWork();
				UniLog.log1("brAttendance rollbackwork");
			}
			throw e;
		}
		finally {
			if (brStudentCourse != null)
				brStudentCourse.close();
			if (brCourseSession != null)
				brCourseSession.close();
			if (brStudentAttendance != null)
				brStudentAttendance.close();
			if (brAssessment != null)
				brAssessment.close();
			if (brPayment != null)
				brPayment.close();
			if (brAttendance != null)
				brAttendance.close();
		}
	}
	
	public static void doUpdateAttendance(final SessionHelper sessionHelper, String dialogTitle, final Component comp, final JxZkBiBase biBase, final int curMode, final Map<Integer, String> studentMap) {
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
		Vbox vboxAttend = new Vbox();
		vboxAttend.appendChild(rgAttendance);
		
		//add remark message
		vboxAttend.appendChild(new Vbox(){{
			this.setStyle("color:grey;line-height:12px;margin:2px;");
			Label label1 = new Label("Present/Absent/Leave - apply for the session within subscription range or existing record.");
			Label label2 = new Label("Remove - apply for existing attendance record only.");
			label1.setStyle("font-size:8px !important;");
			label2.setStyle("font-size:8px !important;");
			this.appendChild(label1);
			this.appendChild(label2);
		}});
		
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
		dbDateTimeTo.setValue(dateTimeAfterHour(cal.getTime(), 3)); //after 3 hour
		
		gh.setWidth("100%");
		gh.getColumn(0).setWidth("100px");
		gh.getColumn(0).setAlign("left");
		gh.getColumn(1).setHflex("1");
		gh.addRow(vboxAttend);
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
				dbDateTimeTo.setValue(dtFrom != null ? dateTimeAfterHour(dtFrom, 3) : null);
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
										showErrMsg(gh, "Invalid date time");
										event.stopPropagation();
										return;
									}
   								}
   								else {
   									if (dateFrom == null || dateTo == null || dateFrom.getTime() > dateTo.getTime()) {
										showErrMsg(gh, "Invalid date");
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

   								Triple<Integer, Integer, Integer> p = updateAttendance(sessionHelper, null, null, studentMap, newStatus, dateTimeFrom, dateTimeTo, false);
								List<String> sl = new ArrayList<String>();
								if (p.getLeft() > 0)
									sl.add(String.format("%d record inserted", p.getLeft()));
								if (p.getMiddle() > 0)
									sl.add(String.format("%d record updated", p.getMiddle()));
								if (p.getRight() > 0)
									sl.add(String.format("%d record removed", p.getRight()));
								if (sl.isEmpty())
									sl.add("0 record inserted");
								ZkUtil.msg(StringUtils.join(sl, ", "));
   							}
   							catch (Exception e) {
   								ZkUtil.showErrMsg(e.toString());
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
	
	private void printPaymentReceipt(Component comp, String receiptNum, String receivedFrom, double totalAmount, double discount, double grandTotalAmount, String paymentMethod, Date paymentDate, String chequeNo,
									List<Map<String, Object>> courseList, List<Map<String, Object>> assessmentList, List<Map<String, Object>> tokenList, boolean isDemo) throws Exception {
		//parse paymentReceipt to stream
		PaymentReceipt pr = new PaymentReceipt(receiptNum, receivedFrom, totalAmount, discount, grandTotalAmount, paymentMethod, paymentDate, chequeNo, courseList, assessmentList, tokenList);
		ByteArrayOutputStream bosPaymentReceipt = pr.build();

		//print ChnftrParser stream
		ZkUtil.showPdfDialog(comp, sessionHelper, bosPaymentReceipt.toByteArray(), buildPdfFilename("PAYMENT_RECEIPT_"));
	}
	
	private void doPayment(final Component comp) {
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		final Vlayout vl = new Vlayout();
		vl.setVflex("1");
		
		final GridHelper ghh = new GridHelper(2);
		ghh.getColumn(0).setWidth("120px");
		ghh.getColumn(0).setAlign("right");
		ghh.getColumn(1).setHflex("1");
		
		final Toolbarbutton btAddToken = new Toolbarbutton();
		btAddToken.setImage("images/row_add20.png");
		btAddToken.setSclass("narrowtoolbarbutton");
		btAddToken.setTooltiptext("Add Token");
		
		final Checkbox cbAll = new Checkbox();
		cbAll.setMold("switch");
		cbAll.setChecked(true);

		final GridHelper gh = new GridHelper(8);
		gh.setVflex("1");
		gh.setSclass("zkbi-da");
		if (sessionHelper.isMobile())
			gh.setWidth("880px");
		gh.appendChild(new Auxhead() {{
			appendChild(new Auxheader() {{ appendChild(new Label("Course")); }});
			appendChild(new Auxheader() {{ appendChild(new Label("Token")); }});
			appendChild(new Auxheader() {{ appendChild(new Label("Session")); }});
			appendChild(new Auxheader() {{ appendChild(new Label("Period")); }});
			appendChild(new Auxheader() {{ appendChild(new Label("Teacher")); }});
			appendChild(new Auxheader() {{ appendChild(new Label("Room")); }});
			appendChild(new Auxheader() {{ appendChild(new Label("Amount")); setAlign("right"); }});
			appendChild(new Auxheader() {{ 
				appendChild(new Hlayout() {{
					appendChild(btAddToken);
					appendChild(cbAll);
				}}); 
				setAlign("right"); 
			}});
		}});
		gh.getColumn(0).setHflex("1");
		gh.getColumn(1).setWidth("50px");
		gh.getColumn(2).setWidth("70px");
		gh.getColumn(3).setWidth("300px");
		gh.getColumn(4).setWidth("150px");
		gh.getColumn(5).setWidth("150px");
		gh.getColumn(6).setWidth("90px");
		gh.getColumn(6).setAlign("right");
		gh.getColumn(7).setWidth("100px");
		gh.getColumn(7).setAlign("right");
		
		final Textbox tbReceiptNumber = new Textbox();
		final Textbox tbReceivedFrom = new Textbox();
		tbReceivedFrom.setWidth("200px");
		tbReceivedFrom.setPlaceholder("*required");
		
		final Doublebox dbAmount = new Doublebox();
		final Doublebox dbDiscount = new Doublebox();
		final Combobox cbPaymentMethod = new Combobox() {{
			appendItem("Cash");
			appendItem("Cheque");
			appendItem("Direct Deposit");
			setReadonly(true);
			setSelectedIndex(0);
		}};
		final Textbox tbChequeNo = new Textbox() {{
			setWidth("250px");
		}};
		final Datebox dbPaymentDate = new Datebox() {{
			setFormat("yyyy/MM/dd");
			setValue(new Date());
		}};
		//final Footer ftDiscount = new Footer();
		//final Footer ftTotal = new Footer();
		final Label ftDiscountLabel = new Label("Discount:");
		final Label ftDiscount = new Label();
		final Label ftTotal = new Label();
		tbReceiptNumber.setDisabled(true);
		dbAmount.setFormat(decFormatPrint.toPattern());
		dbAmount.setValue(0);
		dbDiscount.setFormat(decFormatPrint.toPattern());
		dbDiscount.setValue(0);

		ghh.addRow(new Label("Receipt Number"), tbReceiptNumber);
		ghh.addRow(new Label("Name of Student"), new Label(getBr().getCellString("essd_name")));
		ghh.addRow(new Label("Student Number"), new Label(getBr().getCellString("essd_sdno")));
		ghh.addRow(new Label("Payment Date"), dbPaymentDate);
		ghh.addRow(new Label("Received From"), tbReceivedFrom);
		ghh.addRow(new Label("Net Amount"), new Hlayout() {{
			appendChild(dbAmount);
			appendChild(new Space());
			appendChild(new Label("Discount"));
			appendChild(dbDiscount);
		}});
		ghh.addRow(new Label("Payment Method"), cbPaymentMethod);
		ghh.addRow(new Label("Check No."), tbChequeNo);
		
		final BiResult brCourse = getBr().getSubLink("edu.StudentCourse");
		final double[] totalDiscounts = new double[1];
		final double[] grandTotalAmounts = new double[1];
		ftDiscount.setValue(getDecimalPrintString(0.0));
		ftTotal.setValue(getDecimalPrintString(grandTotalAmounts[0]));
		ftDiscountLabel.setVisible(false);
		ftDiscount.setVisible(false);

		final List<Map<String, Object>> courseList = new ArrayList<Map<String, Object>>();
		final List<Map<String, Object>> assessmentList = new ArrayList<Map<String, Object>>();
		final List<Map<String, Object>> tokenList = new ArrayList<Map<String, Object>>();
		final Map<String, Button> dialogButtonMap = new HashMap<String, Button>();
		//ZkUtil.dumpData(brCourse);
		for (int i = 0; i < brCourse.getRowCount(); i++) {
			brCourse.fetch(false, i);
			//if (!StringUtils.equals(brCourse.getCellString("essbsd_status"), "New"))
			//	continue;
			if (StringUtils.equals(brCourse.getCellString("eaav0_status"), "Cancelled") || StringUtils.equals(brCourse.getCellString("essbsd_status"), "Cancelled"))
				continue;
			final Map<String, Object> map = new HashMap<String, Object>();
			final Checkbox cbEnable = new Checkbox();
			cbEnable.setMold("switch");
			cbEnable.setChecked(true);

			final Date courseStartDate = brCourse.getCellDate("eaav0_startdate");
			final Date courseEndDate = brCourse.getCellDate("eaav0_enddate");
			final String sessionDayStr = brCourse.getCellString("eaav0_sessionday");
			final Date sdStartDate = brCourse.getCellDate("essbsd_startdate");
			final Date sdEndDate = brCourse.getCellDate("essbsd_enddate");

			Date validStartDate0 = DateUtil.isDateNull(sdEndDate) ? courseStartDate : DateUtil.nextday(sdEndDate);
			Course.loadHolidayData(sessionHelper, holidayMap);
			List<Date> dateList = Course.findSessionDates(sessionHelper, holidayMap, courseEndDate, validStartDate0, new Date(Long.MAX_VALUE), 1, sessionDayStr, true);
			final Date validStartDate = dateList.isEmpty() ? null : dateList.get(0);

			final Combobox cbSession = new Combobox();
			final Datebox dbStartDate = new Datebox();
			final Datebox dbEndDate = new Datebox();
			final Toolbarbutton btStartDatePrev = new Toolbarbutton();
			final Toolbarbutton btStartDateNext = new Toolbarbutton();
			cbSession.setHflex("1");

			//startDate, endDate
			dbStartDate.setFormat(sdf.toPattern());
			dbStartDate.setWidth("110px");
			dbEndDate.setFormat(sdf.toPattern());
			dbEndDate.setWidth("110px");
			btStartDatePrev.setIconSclass("z-icon-caret-left");
			btStartDatePrev.setSclass("narrowtoolbarbutton");
			btStartDateNext.setIconSclass("z-icon-caret-right");
			btStartDateNext.setSclass("narrowtoolbarbutton");
			dbStartDate.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					map.put("startDate", dbStartDate.getValue());
					if (!DateUtil.isDateNull(dbStartDate.getValue())) {
						List<Date> dateList = Course.findSessionDates(sessionHelper, holidayMap, courseEndDate, 
								dbStartDate.getValue(), new Date(Long.MAX_VALUE), NumberUtils.toInt(cbSession.getValue()), sessionDayStr, true);
						dbEndDate.setValue(dateList.isEmpty() ? null : dateList.get(dateList.size() - 1));
						cbSession.setValue("" + dateList.size());
						map.put("endDate", dbEndDate.getValue());
						map.put("validEndDate", dbEndDate.getValue());
						map.put("numSession", NumberUtils.toInt(cbSession.getValue()));
						map.put("validNumSession", NumberUtils.toInt(cbSession.getValue()));
					}
					grandTotalAmounts[0] = -totalDiscounts[0];
					for (Map<String, Object> m : courseList) {
						double amount = (Double)m.get("fee") * (Integer)m.get("numSession");
						m.put("price", amount);
						((Label)m.get("amountComp")).setValue(getDecimalPrintString(amount));
						if ((Boolean)m.get("enabled"))
							grandTotalAmounts[0] += (Double)m.get("price");
					}
					for (Map<String, Object> m : assessmentList) {
						if ((Boolean)m.get("enabled"))
							grandTotalAmounts[0] += (Double)m.get("price");
					}
					for (Map<String, Object> m : tokenList) {
						if ((Boolean)m.get("enabled"))
							grandTotalAmounts[0] += (Double)m.get("price");
					}
					ftTotal.setValue(getDecimalPrintString(grandTotalAmounts[0]));
				}
			});
			dbEndDate.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					map.put("endDate", dbEndDate.getValue());
					if (!DateUtil.isDateNull(dbStartDate.getValue()) && !DateUtil.isDateNull(dbEndDate.getValue())) {
						List<Date> dateList = Course.findSessionDates(sessionHelper, holidayMap, courseEndDate, 
								dbStartDate.getValue(), dbEndDate.getValue(), -1, sessionDayStr, true);
						map.put("validEndDate", dateList.isEmpty() ? null : dateList.get(dateList.size() - 1));
						cbSession.setValue("" + dateList.size());
						map.put("numSession", NumberUtils.toInt(cbSession.getValue()));
						map.put("validNumSession", NumberUtils.toInt(cbSession.getValue()));
					}
					grandTotalAmounts[0] = -totalDiscounts[0];
					for (Map<String, Object> m : courseList) {
						double amount = (Double)m.get("fee") * (Integer)m.get("numSession");
						m.put("price", amount);
						((Label)m.get("amountComp")).setValue(getDecimalPrintString(amount));
						if ((Boolean)m.get("enabled"))
							grandTotalAmounts[0] += (Double)m.get("price");
					}
					for (Map<String, Object> m : assessmentList) {
						if ((Boolean)m.get("enabled"))
							grandTotalAmounts[0] += (Double)m.get("price");
					}
					for (Map<String, Object> m : tokenList) {
						if ((Boolean)m.get("enabled"))
							grandTotalAmounts[0] += (Double)m.get("price");
					}
					ftTotal.setValue(getDecimalPrintString(grandTotalAmounts[0]));
				}
			});
			btStartDatePrev.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					Date oldDate = dbStartDate.getValue();
					if (!DateUtil.isDateNull(oldDate)) {
						List<Date> dateList = Course.findSessionDatesReverse(sessionHelper, holidayMap, courseStartDate, 
								oldDate, DateUtil.prevyear(oldDate), 2, sessionDayStr, true);
						UniLog.log1("oldDate:%s, btStartDatePrev dateList:%s", oldDate, dateList);
						Collections.reverse(dateList);
						Date newDate = null;
						for (Date d : dateList) {
							if (d.compareTo(oldDate) < 0) {
								newDate = d;
								break;
							}
						}
						if (newDate != null) {
							dbStartDate.setValue(newDate);
							Events.echoEvent(Events.ON_CHANGE, dbStartDate, null);
						}
					}
					else {
						dbStartDate.setValue(validStartDate);
						Events.echoEvent(Events.ON_CHANGE, dbStartDate, null);
					}
				}
			});
			btStartDateNext.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					Date oldDate = dbStartDate.getValue();
					if (!DateUtil.isDateNull(oldDate)) {
						List<Date> dateList = Course.findSessionDates(sessionHelper, holidayMap, courseEndDate, 
								oldDate, new Date(Long.MAX_VALUE), 2, sessionDayStr, true);
						UniLog.log1("oldDate:%s, btStartDateNext dateList:%s", oldDate, dateList);
						Date newDate = null;
						for (Date d : dateList) {
							if (d.compareTo(oldDate) > 0) {
								newDate = d;
								break;
							}
						}
						if (newDate != null) {
							dbStartDate.setValue(newDate);
							Events.echoEvent(Events.ON_CHANGE, dbStartDate, null);
						}
					}
					else {
						dbStartDate.setValue(validStartDate);
						Events.echoEvent(Events.ON_CHANGE, dbStartDate, null);
					}
				}
			});

			//session num
			int defaultNumSession = brCourse.getCellInt("eaav0_numsession");
			int numSession = defaultNumSession > 0 ? defaultNumSession : 10;
			for (int j = 1; j <= 5; j++)
				cbSession.appendItem("" + (numSession * j));
			cbSession.setSelectedIndex(0);
			cbSession.addEventListener(Events.ON_CHANGE, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					map.put("numSession", NumberUtils.toInt(cbSession.getValue()));
					if (!DateUtil.isDateNull(dbStartDate.getValue())) {
						List<Date> dateList = Course.findSessionDates(sessionHelper, holidayMap, courseEndDate, 
								dbStartDate.getValue(), new Date(Long.MAX_VALUE), NumberUtils.toInt(cbSession.getValue()), sessionDayStr, true);
						dbEndDate.setValue(dateList.isEmpty() ? null : dateList.get(dateList.size() - 1));
						map.put("endDate", dbEndDate.getValue());
						map.put("validEndDate", dbEndDate.getValue());
						map.put("validNumSession", dateList.size());
					}
					grandTotalAmounts[0] = -totalDiscounts[0];
					for (Map<String, Object> m : courseList) {
						double amount = (Double)m.get("fee") * NumberUtils.toInt(((Combobox)m.get("sessionComp")).getValue());
						m.put("price", amount);
						((Label)m.get("amountComp")).setValue(getDecimalPrintString(amount));
						if ((Boolean)m.get("enabled"))
							grandTotalAmounts[0] += (Double)m.get("price");
					}
					for (Map<String, Object> m : assessmentList) {
						if ((Boolean)m.get("enabled"))
							grandTotalAmounts[0] += (Double)m.get("price");
					}
					for (Map<String, Object> m : tokenList) {
						if ((Boolean)m.get("enabled"))
							grandTotalAmounts[0] += (Double)m.get("price");
					}
					ftTotal.setValue(getDecimalPrintString(grandTotalAmounts[0]));
				}
			});

			//calc startDate, endDate by session num
			Date d = DateUtil.nextday(DateUtil.isDateNull(sdEndDate) ? DateUtil.today() : sdEndDate);
			if (!DateUtil.isDateNull(validStartDate) && d.compareTo(validStartDate) < 0)
				d = validStartDate;
			dateList = Course.findSessionDates(sessionHelper, holidayMap, courseEndDate, 
					d, new Date(Long.MAX_VALUE), NumberUtils.toInt(cbSession.getValue()), sessionDayStr, true);
			if (!dateList.isEmpty()) {
				dbStartDate.setValue(dateList.get(0));
				dbEndDate.setValue(dateList.get(dateList.size() - 1));
				cbSession.setValue("" + dateList.size());
			}
			
			//double amount = brCourse.getCellDouble("eaav0_fee") * brCourse.getCellInt("eaav0_numsession");
			double fee = brCourse.getCellDouble("eaav0_fee");
			double amount = fee * NumberUtils.toInt(cbSession.getValue());
			grandTotalAmounts[0] += amount;
			final Label lbAmount = new Label(getDecimalPrintString(amount));
			ftTotal.setValue(getDecimalPrintString(grandTotalAmounts[0]));
			
			map.put("courseRg", brCourse.getCellInt("essbsd_avrg"));
			map.put("courseCode", brCourse.getCellString("eaav0_code"));
			map.put("courseName", brCourse.getCellString("eaav0_name"));
			map.put("tokenCcy", brCourse.getCellString("eaav0_tokenccy"));
			map.put("startDate", dbStartDate.getValue());
			map.put("endDate", dbEndDate.getValue());
			map.put("numSession", NumberUtils.toInt(cbSession.getValue()));
			map.put("validStartDate", validStartDate);
			map.put("validEndDate", dbEndDate.getValue());
			map.put("validNumSession", NumberUtils.toInt(cbSession.getValue()));
			map.put("teacher", brCourse.getCellString("estt_name"));
			map.put("room", brCourse.getCellString("esfc_name"));
			map.put("fee", fee);
			map.put("defaultNumSession", defaultNumSession);
			map.put("price", amount);
			map.put("enabled", cbEnable.isChecked());
			map.put("enableComp", cbEnable);
			map.put("startDateComp", dbStartDate);
			map.put("endDateComp", dbEndDate);
			map.put("sessionComp", cbSession);
			map.put("amountComp", lbAmount);
			courseList.add(map);

			gh.addRow(
				new Label(String.format("%s (%s)", map.get("courseCode"), map.get("courseName"))),
				new Label((String)map.get("tokenCcy")),
				cbSession,
				new Div() {{
					appendChild(btStartDatePrev);
					appendChild(dbStartDate);
					appendChild(btStartDateNext);
					appendChild(new I() {{ 
						setSclass("fa fa-minus"); 
						setStyle("vertical-align:middle;margin-right:6px;");
					}});
					appendChild(dbEndDate);
				}},
				new Label((String)map.get("teacher")),
				new Label((String)map.get("room")),
				lbAmount,
				cbEnable
			);
			cbEnable.addEventListener(Events.ON_CHECK, new EventListener<CheckEvent>() {
				@Override
				public void onEvent(CheckEvent event) throws Exception {
					map.put("enabled", cbEnable.isChecked());
					grandTotalAmounts[0] = -totalDiscounts[0];
					int count = courseList.size() + assessmentList.size() + tokenList.size();
					int checkCount = 0;
					for (Map<String, Object> m : courseList) {
						if ((Boolean)m.get("enabled")) {
							grandTotalAmounts[0] += (Double)m.get("price");
							checkCount++;
						}
					}
					for (Map<String, Object> m : assessmentList) {
						if ((Boolean)m.get("enabled")) {
							grandTotalAmounts[0] += (Double)m.get("price");
							checkCount++;
						}
					}
					for (Map<String, Object> m : tokenList) {
						if ((Boolean)m.get("enabled")) {
							grandTotalAmounts[0] += (Double)m.get("price");
							checkCount++;
						}
					}
					ftTotal.setValue(getDecimalPrintString(grandTotalAmounts[0]));
					setCheckboxAll(cbAll, count, checkCount);
				}
			});
		}
		//find assessment records
		BiResult brAssessment = null;
		try {
			//brAssessment = BiResultHelper.create(sessionHelper, "edu.Assessment", 
			//		String.format("esatsd_atrg = %d and esatsd_status = '' and essnas_fee > 0 and essnas_status = 'Normal'", getBr().getCellInt("essd_rg")), -1, null);
			//xjcheng211129: allow free assessment
			brAssessment = BiResultHelper.create(sessionHelper, "edu.Assessment", 
					String.format("esatsd_atrg = %d and esatsd_status = '' and essnas_status = 'Normal'", getBr().getCellInt("essd_rg")), -1, null);
			while (brAssessment.next()) {
				final Map<String, Object> map = new HashMap<String, Object>();
				final Checkbox cbEnable = new Checkbox();
				cbEnable.setMold("switch");
				cbEnable.setChecked(true);

				double fee = brAssessment.getCellDouble("essnas_fee");
				double amount = fee;
				grandTotalAmounts[0] += amount;
				final Label lbAmount = new Label(getDecimalPrintString(amount));

				map.put("sessionRg", brAssessment.getCellInt("essnas_rg"));
				map.put("description", brAssessment.getCellString("essnas_name"));
				map.put("tokenCcy", brAssessment.getCellString("essnas_tokenccy"));
				map.put("date", brAssessment.getCellDate("essnas_date"));
				map.put("startTime", brAssessment.getCellDate("essnas_sttime"));
				map.put("endTime", brAssessment.getCellDate("essnas_endtime"));
				map.put("teacher", brAssessment.getCellString("estt_name"));
				map.put("room", brAssessment.getCellString("esfc_name"));
				map.put("fee", fee);
				map.put("price", amount);
				map.put("enabled", cbEnable.isChecked());
				map.put("enableComp", cbEnable);
				map.put("amountComp", lbAmount);
				assessmentList.add(map);

				gh.addRow(
					new Label(String.format("%s (%s)", map.get("description"), map.get("sessionRg"))),
					new Label((String)map.get("tokenCcy")),
					new Label("1"),
					new Label(sdf.format(map.get("date"))),
					new Label((String)map.get("teacher")),
					new Label((String)map.get("room")),
					lbAmount,
					cbEnable
				);
				cbEnable.addEventListener(Events.ON_CHECK, new EventListener<CheckEvent>() {
					@Override
					public void onEvent(CheckEvent event) throws Exception {
						map.put("enabled", cbEnable.isChecked());
						grandTotalAmounts[0] = -totalDiscounts[0];
						int count = courseList.size() + assessmentList.size() + tokenList.size();
						int checkCount = 0;
						for (Map<String, Object> m : courseList) {
							if ((Boolean)m.get("enabled")) {
								grandTotalAmounts[0] += (Double)m.get("price");
								checkCount++;
							}
						}
						for (Map<String, Object> m : assessmentList) {
							if ((Boolean)m.get("enabled")) {
								grandTotalAmounts[0] += (Double)m.get("price");
								checkCount++;
							}
						}
						for (Map<String, Object> m : tokenList) {
							if ((Boolean)m.get("enabled")) {
								grandTotalAmounts[0] += (Double)m.get("price");
								checkCount++;
							}
						}
						ftTotal.setValue(getDecimalPrintString(grandTotalAmounts[0]));
						setCheckboxAll(cbAll, count, checkCount);
					}
				});
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (brAssessment != null)
				brAssessment.close();
		}
		
		gh.appendChild(new Foot() {{
			//ftTotal.setLabel(decFormatPrint.format(grandTotalAmounts[0]));
			appendChild(new Footer());
			appendChild(new Footer());
			appendChild(new Footer());
			appendChild(new Footer());
			appendChild(new Footer());
			//appendChild(ftTotalLabel);
			//appendChild(ftTotal);
			appendChild(new Footer() {{
				appendChild(new Vlayout() {{
					appendChild(ftDiscountLabel);
					appendChild(new Label("Total:"));
				}});
				setAlign("right");
			}});
			appendChild(new Footer() {{
				appendChild(new Vlayout() {{
					appendChild(ftDiscount);
					appendChild(ftTotal);
				}});
				setAlign("right");
			}});
			appendChild(new Footer());
			//setHeight("64px");
		}});

		vl.appendChild(ghh);
		vl.appendChild(new Div() {{
			appendChild(gh);
			setStyle("overflow-x:auto");
			setVflex("1");
		}});
		
		final MessageboxDlg dlg = ZkUtil.buildMessageboxDlg("Payment", 
			vl, 
    		new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL, Messagebox.Button.ABORT}, 
    		new String[] {"Save", "Close", "Print Receipt"},
    		comp.getRoot(),
  			new EventListener<Messagebox.ClickEvent>(){
   				@Override
   				public void onEvent(final ClickEvent event) throws Exception {
   					if (event.getButton() == null)
   						return;
   					switch (event.getButton()) {
   					case OK: //Save
   						UniLog.log1("cbPaymentMethod value:%s", cbPaymentMethod.getValue());
   						try {
   							if (DateUtil.isDateNull(dbPaymentDate.getValue())) {
		   						showErrMsg(vl, "'Payment Date' cannot be empty");
  								event.stopPropagation();
   								return;
   							}
   							if (StringUtils.isBlank(tbReceivedFrom.getValue())) {
		   						showErrMsg(vl, "'Received From' cannot be empty");
  								event.stopPropagation();
   								return;
   							}
   							if (dbAmount.getValue() == null) {
   								showErrMsg(vl, "'Amount' cannot be empty");
   								event.stopPropagation();
   								return;
   							}
   							if (dbDiscount.getValue() == null) {
   								showErrMsg(vl, "'Discount' cannot be empty");
   								event.stopPropagation();
   								return;
   							}
   							if (dbDiscount.getValue() < 0) {
   								showErrMsg(vl, "'Discount' cannot be less than 0");
   								event.stopPropagation();
   								return;
   							}
   							boolean foundCourse = false;
   							boolean foundAssessment = false;
   							boolean foundToken = false;
							boolean invalidFound = false;
							int courseCount = 0;
   							for (Map<String, Object> m : courseList) {
								if ((Boolean)m.get("enabled")) {
									String courseCode = (String)m.get("courseCode");
									Date validStartDate = (Date)m.get("validStartDate");
									Date validEndDate = (Date)m.get("validEndDate");
									int validNumSession = (Integer)m.get("validNumSession");
									Date startDate = (Date)m.get("startDate");
									Date endDate = (Date)m.get("endDate");
									int numSession = (Integer)m.get("numSession");
									int defaultNumSession = (Integer)m.get("defaultNumSession");
									/*
									if (numSession < defaultNumSession) {
										showErrMsg(vl, String.format("The session must be greater than or equals with default value. Course Code: %s", courseCode));
										event.stopPropagation();
										return;
									}
									*/
									if (numSession < 1) {
										showErrMsg(vl, String.format("The session count must be greater than 1"));
										event.stopPropagation();
										return;
									}
									if (DateUtil.isDateNull(startDate)) {
										showErrMsg(vl, String.format("'Start date' cannot be empty. Course code: %s", courseCode));
										event.stopPropagation();
										return;
									}
									if (DateUtil.isDateNull(endDate)) {
										showErrMsg(vl, String.format("'End date' cannot be empty. Course code: %s", courseCode));
										event.stopPropagation();
										return;
									}
									if (StringUtils.isBlank((String)m.get("tokenCcy"))) {
										showErrMsg(vl, "'Token' cannot be empty.");
										event.stopPropagation();
										return;
									}
									/*
									if (startDate.compareTo(endDate) >= 0) {
										showErrMsg(vl, String.format("'Start date' must be less than 'End date'. Course code: %s", courseCode));
										event.stopPropagation();
										return;
									}
									*/
									//andrew210902 handle special case: 1 session course, startdate equal to enddate
									if (startDate.compareTo(endDate) > 0) {
										showErrMsg(vl, String.format("'End date' cannot later then 'Start date'. Course code: %s", courseCode));
										event.stopPropagation();
										return;
									}
									boolean invalidFound0 = false;
									if (validStartDate == null) {
										UniLog.log1("No valid session date found. Course code: %s", courseCode);
										invalidFound0 = true;
									}
									if (!invalidFound0 && startDate.compareTo(validStartDate) < 0) {
										UniLog.log1("Invalid start date. Course code: %s, validStartDate:%s", courseCode, sdf.format(validStartDate));
										invalidFound0 = true;
									}
									if (!invalidFound0 && validEndDate == null) {
										UniLog.log1("Invalid end date. Course code: %s", courseCode);
										invalidFound0 = true;
									}
									if (!invalidFound0 && endDate.compareTo(validEndDate) != 0) {
										UniLog.log1("Invalid end date ('%s' != '%s'). Course code: %s", courseCode, sdf.format(endDate), sdf.format(validEndDate));
										invalidFound0 = true;
									}
									if (!invalidFound0 && numSession != validNumSession) {
										UniLog.log1("Invalid session number (%d != %d). Course code: %s", courseCode, numSession, validNumSession);
										invalidFound0 = true;
									}
									if (!invalidFound && invalidFound0)
										invalidFound = true;
									courseCount++;
									foundCourse = true;
								}
							}
							int assessmentCount = 0;
   							for (Map<String, Object> m : assessmentList) {
								if ((Boolean)m.get("enabled")) {
									if (StringUtils.isBlank((String)m.get("tokenCcy"))) {
										showErrMsg(vl, "'Token' cannot be empty.");
										event.stopPropagation();
										return;
									}
									//xjcheng211129: allow free assessment
									/*if ((Double)m.get("price") == 0) {
										showErrMsg(vl, "Amount cannot be equal to zero");
										event.stopPropagation();
										return;
									}*/
									assessmentCount++;
									foundAssessment = true;
								}
   							}
   							int statTokenCount = 0;
   							int tokenCount = 0;
   							for (Map<String, Object> m : tokenList) {
								if ((Boolean)m.get("enabled")) {
									if (m.get("tokenCcy") == null) {
										showErrMsg(vl, "Please choose token ccy");
										event.stopPropagation();
										return;
									}
									/*
									//andrew210923: allow input negative amount for fund reverse
									if ((Double)m.get("price") <= 0) {
										//showErrMsg(vl, "The price must be more then zero");
										showErrMsg(vl, "Amount must be greater then zero");
										event.stopPropagation();
										return;
									}
									*/
									/*
									//xjcheng211129: allow free token row
									if ((Double)m.get("price") == 0) {
										showErrMsg(vl, "Amount cannot be equal to zero");
										event.stopPropagation();
										return;
									}*/
									if (StringUtils.equals((String)m.get("tokenCcy"), "STAT"))
										statTokenCount++;
									tokenCount++;
									foundToken = true;
								}
   							}
   							boolean isMixedStatItem = false;
   							if (statTokenCount > 0) {
   								if (tokenCount != statTokenCount || courseCount > 0 || assessmentCount > 0) {
   									/*showErrMsg(vl, "Payment cannot mixed with STAT item and non-STAT item");
   									event.stopPropagation();
   									return;*/
   									isMixedStatItem = true;
   								}
   							}
   							if (!foundCourse && !foundAssessment && !foundToken) {
   								showErrMsg(vl, "Please enable course or assessment or token");
   								event.stopPropagation();
   								return;
   							}
   							if (dbAmount.getValue() != grandTotalAmounts[0]) {
   								showErrMsg(vl, "The amount must be equal to total");
   								event.stopPropagation();
   								return;
   							}
   							StringBuilder sbTitle = new StringBuilder("Are you sure to save payment record?");
   							if (isMixedStatItem)
   								sbTitle.insert(0, "Warning: the payment mixed with STAT item and non-STAT item.\n");
   							if (invalidFound)
   								sbTitle.insert(0, "Warning: startdate/enddate not on session day.\n");
   							ZkBiMsgbox.show(ZkBiMsgbox.Type.question, sbTitle.toString(),
   								new String[] {"Ok", "Cancel"}, new ZkBiEventListener<Event>() {
								@Override
								public void onZkBiEvent(Event event1) throws Exception {
									ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event1.getTarget();
									if (btn.getName().equals("Ok")) {
										String receiptNum = savePayment(tbReceivedFrom.getValue(), grandTotalAmounts[0], totalDiscounts[0], cbPaymentMethod.getValue(), tbChequeNo.getValue(), dbPaymentDate.getValue(), courseList, assessmentList, tokenList);
										tbReceiptNumber.setValue(receiptNum);
 										tbReceivedFrom.setDisabled(true);
					   					dbAmount.setDisabled(true);
					   					cbPaymentMethod.setDisabled(true);
					   					dbPaymentDate.setDisabled(true);
					   					for (Map<String, Object> m : courseList) {
					   						((Combobox)m.get("sessionComp")).setDisabled(true);
					   						((Datebox)m.get("startDateComp")).setDisabled(true);
					   						((Datebox)m.get("endDateComp")).setDisabled(true);
					   						((Checkbox)m.get("enableComp")).setDisabled(true);
					   					}
					   					for (Map<String, Object> m : assessmentList) {
					   						((Checkbox)m.get("enableComp")).setDisabled(true);
					   					}
					   					for (Map<String, Object> m : tokenList) {
					   						((Combobox)m.get("tokenCcyNameComp")).setDisabled(true);
					   						((Textbox)m.get("remarkComp")).setDisabled(true);
					   						((Doublebox)m.get("amountComp")).setDisabled(true);
					   						((Checkbox)m.get("enableComp")).setDisabled(true);
					   					}
					   					((Button)dialogButtonMap.get("save")).setDisabled(true);
					   					((Button)dialogButtonMap.get("print")).setDisabled(false);
					   					//event.stopPropagation();
					
										getBr().refetchCurrent();
										bindCellCollection(getBr(), curMode);
					   					showMsg(vl, "Save finish");
									}
								}
							});
		   					event.stopPropagation();
   						}
   						catch (Exception e) {
   							ZkUtil.showErrMsg(e.getMessage());
   						}
   						break;
   					case ABORT: //Print Receipt
   						try {
   							if (StringUtils.isBlank(tbReceiptNumber.getValue())) {
   								showErrMsg(vl, "'Receipt number' cannot be empty");
   								event.stopPropagation();
   								return;
   							}
   							if (StringUtils.isBlank(tbReceivedFrom.getValue())) {
   								showErrMsg(vl, "'Received from' cannot be empty");
   								event.stopPropagation();
   								return;
   							}
   							if (dbAmount.getValue() == null) {
   								showErrMsg(vl, "'Amount' cannot be empty");
   								event.stopPropagation();
   								return;
   							}
   							if (dbAmount.getValue() != grandTotalAmounts[0]) {
   								showErrMsg(vl, "The amount must be equal to total");
   								event.stopPropagation();
   								return;
   							}
   							printPaymentReceipt(comp.getRoot(), tbReceiptNumber.getValue(), tbReceivedFrom.getValue(), 
   									grandTotalAmounts[0] + totalDiscounts[0], totalDiscounts[0], grandTotalAmounts[0], cbPaymentMethod.getValue(), 
   									dbPaymentDate.getValue(), tbChequeNo.getValue(), courseList, assessmentList, tokenList, sessionHelper.getAllowDemo());
   						}
   						catch (Exception e) {
   							ZkUtil.showErrMsg(e.getMessage());
   						}
   						break;
   					default:
   						break;
   					}
   				}
  			}
    	);
		gh.addEventListener(Events.ON_AFTER_SIZE, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				if (event instanceof AfterSizeEvent)
					UniLog.log1("event:%s, width:%d, height:%d, dlgHeight:%s", event, ((AfterSizeEvent)event).getWidth(), ((AfterSizeEvent)event).getHeight(), dlg.getHeight());
				//dialog setHeight
				Integer oldGridHeight = (Integer) gh.getAttribute("gridHeight");
				Integer oldGridFootHeight = (Integer) gh.getAttribute("gridFootHeight");
				//Integer headerHeight = (Integer) ghh.getAttribute("headerHeight");
				int headerHeight = 258;//194;
				int gridHeaderHeight = 35;
				//int gridFooterHeight = 32;
				int gridFootHeight = ftDiscount.isVisible() ? 61 : 32;
				int newGridHeight = (event instanceof AfterSizeEvent) ? ((AfterSizeEvent)event).getHeight() : (Integer)gh.getAttribute("gridHeight");
				UniLog.log1("newGridHeight:%d, oldGridHeight:%d", newGridHeight, oldGridHeight);
				UniLog.log1("gridFootHeight:%d, oldGridFootHeight:%d", gridFootHeight, oldGridFootHeight);
				if (oldGridHeight == null || newGridHeight != oldGridHeight || (oldGridFootHeight != null && oldGridFootHeight != gridFootHeight)) {
					int newDlgHeight = ((newGridHeight + 2) + 32 + 43 + (headerHeight + 7) + gridHeaderHeight + gridFootHeight + 16);//32: titlebar, 43: bottombar, 45: tabbox exclude grid, 16: other padding/border...
					dlg.setHeight(newDlgHeight + "px");
					dlg.setAttribute("dlgHeight", newDlgHeight);
					gh.setAttribute("gridHeight", newGridHeight);
					gh.setAttribute("gridFootHeight", gridFootHeight);
				}
			}
		});
		dlg.setWidth("1240px");
		dlg.setStyle("max-width:100%;max-height:" + (sessionHelper.isMobile() ? "100" : "95") + "%");
      	dlg.doHighlighted();
      	
      	cbAll.addEventListener(Events.ON_CHECK, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				boolean isCheckAll = cbAll.isChecked();
				grandTotalAmounts[0] = -totalDiscounts[0];
				for (Map<String, Object> m : courseList) {
 					((Checkbox)m.get("enableComp")).setChecked(isCheckAll);
 					m.put("enabled", isCheckAll);
					if ((Boolean)m.get("enabled"))
						grandTotalAmounts[0] += (Double)m.get("price");
				}
				for (Map<String, Object> m : assessmentList) {
 					((Checkbox)m.get("enableComp")).setChecked(isCheckAll);
 					m.put("enabled", isCheckAll);
					if ((Boolean)m.get("enabled"))
						grandTotalAmounts[0] += (Double)m.get("price");
				}
				for (Map<String, Object> m : tokenList) {
 					((Checkbox)m.get("enableComp")).setChecked(isCheckAll);
 					m.put("enabled", isCheckAll);
					if ((Boolean)m.get("enabled"))
						grandTotalAmounts[0] += (Double)m.get("price");
				}
				ftTotal.setValue(getDecimalPrintString(grandTotalAmounts[0]));
			}
      	});
      	
		dbDiscount.addEventListener(Events.ON_CHANGING, new ZkBiEventListener<InputEvent>() {
			@Override
			public void onZkBiEvent(InputEvent event) throws Exception {
				totalDiscounts[0] = toDouble(event.getValue());
				grandTotalAmounts[0] = -totalDiscounts[0];
				for (Map<String, Object> m : courseList) {
					if ((Boolean)m.get("enabled"))
						grandTotalAmounts[0] += (Double)m.get("price");
				}
				for (Map<String, Object> m : assessmentList) {
					if ((Boolean)m.get("enabled"))
						grandTotalAmounts[0] += (Double)m.get("price");
				}
				for (Map<String, Object> m : tokenList) {
					if ((Boolean)m.get("enabled"))
						grandTotalAmounts[0] += (Double)m.get("price");
				}
				if (totalDiscounts[0] != 0) {
					ftDiscount.setValue(getDecimalPrintString(-totalDiscounts[0]));
					ftDiscountLabel.setVisible(true);
					ftDiscount.setVisible(true);
					Events.echoEvent(Events.ON_AFTER_SIZE, gh, null);
				}
				else {
					ftDiscountLabel.setVisible(false);
					ftDiscount.setVisible(false);
					Events.echoEvent(Events.ON_AFTER_SIZE, gh, null);
				}
				ftTotal.setValue(getDecimalPrintString(grandTotalAmounts[0]));
			}
		});

      	
      	//add token
      	final Map<String, String> tokenCcyMap = new LinkedHashMap<String, String>();
		loadTokenCcy(sessionHelper, tokenCcyMap);
		btAddToken.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				if (dlg.getAttribute("dlgHeight") != null)
					dlg.setHeight(((Integer)dlg.getAttribute("dlgHeight") + 32) + "px");
				final Map<String, Object> map = new HashMap<String, Object>();
				final Combobox cbTokenCcyName = new Combobox();
				final Label lbTokenCcy = new Label();
				final Textbox tbRemark = new Textbox();
				final Doublebox dbAmount = new Doublebox();
				final Checkbox cbEnable = new Checkbox();
				cbTokenCcyName.setHflex("1");
				cbTokenCcyName.setReadonly(true);
				if (!tokenCcyMap.isEmpty()) {
					for (Map.Entry<String, String> entry : tokenCcyMap.entrySet()) {
						Comboitem cbi = cbTokenCcyName.appendItem(entry.getValue());
						cbi.setValue(entry.getKey());
					}
				}
				tbRemark.setHflex("1");
				tbRemark.setPlaceholder("Optional Remark");
				dbAmount.setHflex("1");
				dbAmount.setFormat(decFormatPrint.toPattern());
				dbAmount.setValue(0);
				dbAmount.setStyle("text-align:right");
				cbEnable.setMold("switch");
				cbEnable.setChecked(true);
				cbTokenCcyName.addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Comboitem, Event>>() {
					@Override
					public void onZkBiEvent(SelectEvent<Comboitem, Event> event) throws Exception {
						map.put("tokenCcy", (String)event.getReference().getValue());
						map.put("tokenCcyName", event.getReference().getLabel());
						lbTokenCcy.setValue((String)map.get("tokenCcy"));
					}
				});
				dbAmount.addEventListener(Events.ON_CHANGING, new ZkBiEventListener<InputEvent>() {
					@Override
					public void onZkBiEvent(InputEvent event) throws Exception {
						UniLog.log1("event:%s, value:%s", event, event.getValue());
						map.put("price", toDouble(event.getValue()));
						grandTotalAmounts[0] = -totalDiscounts[0];
						for (Map<String, Object> m : courseList) {
							if ((Boolean)m.get("enabled"))
								grandTotalAmounts[0] += (Double)m.get("price");
						}
						for (Map<String, Object> m : assessmentList) {
							if ((Boolean)m.get("enabled"))
								grandTotalAmounts[0] += (Double)m.get("price");
						}
						for (Map<String, Object> m : tokenList) {
							if ((Boolean)m.get("enabled"))
								grandTotalAmounts[0] += (Double)m.get("price");
						}
						ftTotal.setValue(getDecimalPrintString(grandTotalAmounts[0]));
					}
				});
				tbRemark.addEventListener(Events.ON_CHANGING, new ZkBiEventListener<InputEvent>() {
					@Override
					public void onZkBiEvent(InputEvent event) throws Exception {
						map.put("remark", event.getValue());
					}
				});
				cbEnable.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
					@Override
					public void onZkBiEvent(CheckEvent event) throws Exception {
						map.put("enabled", cbEnable.isChecked());
						grandTotalAmounts[0] = -totalDiscounts[0];
						int count = courseList.size() + assessmentList.size() + tokenList.size();
						int checkCount = 0;
						for (Map<String, Object> m : courseList) {
							if ((Boolean)m.get("enabled")) {
								grandTotalAmounts[0] += (Double)m.get("price");
								checkCount++;
							}
						}
						for (Map<String, Object> m : assessmentList) {
							if ((Boolean)m.get("enabled")) {
								grandTotalAmounts[0] += (Double)m.get("price");
								checkCount++;
							}
						}
						for (Map<String, Object> m : tokenList) {
							if ((Boolean)m.get("enabled")) {
								grandTotalAmounts[0] += (Double)m.get("price");
								checkCount++;
							}
						}
						ftTotal.setValue(getDecimalPrintString(grandTotalAmounts[0]));
						setCheckboxAll(cbAll, count, checkCount);
					}
				});

				gh.addRow(
					cbTokenCcyName,
					lbTokenCcy, 
					tbRemark, 
					//new Label(), 
					//new Label(), 
					//new Label(), 
					dbAmount,
					cbEnable
				);
				int rowCount = gh.getRows().getChildren().size();
				gh.getRow(rowCount - 1).setSpans(",,4");

				map.put("tokenCcy", null);
				map.put("tokenCcyName", null);
				map.put("remark", "");
				map.put("price", 0.0);
				map.put("enabled", cbEnable.isChecked());
				map.put("enableComp", cbEnable);
				map.put("tokenCcyNameComp", cbTokenCcyName);
				map.put("remarkComp", tbRemark);
				map.put("amountComp", dbAmount);

				tokenList.add(map);
			}
		});

		//find buttons
		for (Component cbtn : dlg.queryAll("Button")) {
			Button btn = (Button)cbtn;
			if (StringUtils.equals(btn.getLabel(), "Save"))
				dialogButtonMap.put("save", btn);
			else if (StringUtils.equals(btn.getLabel(), "Close"))
				dialogButtonMap.put("close", btn);
			else if (StringUtils.equals(btn.getLabel(), "Print Receipt")) {
				dialogButtonMap.put("print", btn);
				btn.setDisabled(true);
			}
		}
	}

	private class PaymentReceipt {
		final Rectangle pageSize = PageSize.A4;
		final float docWidthPx = pageSize.getWidth() - ChnftrParser.dpi100ToPx(100);
		final float docHeightPx = pageSize.getHeight() - ChnftrParser.dpi100ToPx(100);
		final int docWidth = ChnftrParser.pxToDpi100(docWidthPx);
		final int docHeight = ChnftrParser.pxToDpi100(docHeightPx);
		final int fontPt = 10;
		final String engFont = "helv_nr";
		final String chnFont = "chinese";
		final int rowHeight = 20;
		final SimpleDateFormat dsdf = new SimpleDateFormat("yyyy/MM/dd");
		
		final int docLeft = 0;
		final int docTop = 0; 
		final int maxHeight = docHeight;
		final Map<String, TCol> tcolMap = new LinkedHashMap<String, TCol>();
		final Map<String, TCol> ttcolMap = new LinkedHashMap<String, TCol>();

		ChnftrBuilder builder = new ChnftrBuilder();
		ChnftrBuilder.Cell currCell;
		int offset;
		
		final String receiptNum;
		final String receivedFrom;
		final double totalAmount, discount, grandTotalAmount;
		final String paymentMethod;
		final Date paymentDate;
		final String chequeNo;
		final List<Map<String, Object>> unionList = new ArrayList<Map<String, Object>>();
		
		//230103 mask out data for demo (temp solution, those common data should be share for all pages)
		boolean isDemo = sessionHelper.getAllowDemo();
		String leftLogo = isDemo ? "images/logo/blank_250x250.png": "images/logo/edu_logo1.jpg";
		String rightLogo = isDemo ? "images/logo/blank_250x250.png" : "images/logo/edu_logo.jpg";
		String coName = isDemo ? "DEMO EDUCATION CENTRE" : "LITTLE SCHOLARS EDUCATION CENTRE";
		String regNo = isDemo ? "123456A" : "616524";
		String address = isDemo ? "RM 1001, 1/F, DEMO BUILDING, HONG KONG" : "101 & 102, 1/F, HOLLYWOOD CENTRE, 233 HOLLYWOOD ROAD, SHEUNG WAN, HONG KONG";
		String telNo = isDemo ? "(852) 1234 5678" : "(852) 2537 9519";
		String chopImg = isDemo ? "images/logo/blank_250x250.png": "images/logo/edu_companychop.png";
		
		
		PaymentReceipt(String receiptNum, String receivedFrom, double totalAmount, double discount, double grandTotalAmount, String paymentMethod, Date paymentDate, String chequeNo,
						List<Map<String, Object>> courseList, List<Map<String, Object>> assessmentList, List<Map<String, Object>> tokenList) {
			this.receiptNum = receiptNum;
			this.receivedFrom = receivedFrom;
			this.totalAmount = totalAmount;
			this.discount = discount;
			this.grandTotalAmount = grandTotalAmount;
			this.paymentMethod = paymentMethod;
			this.paymentDate = paymentDate;
			this.chequeNo = chequeNo;
			

			UniLog.log1("courseList size:%d, assessmentList size:%d, tokenList size:%d", courseList.size(), assessmentList.size(), tokenList.size());
			for (Map<String, Object> map : courseList) {
				if (!(Boolean)map.get("enabled"))
					continue;
				Map<String, Object> map1 = new HashMap<String, Object>();
				map1.put("courseName", map.get("courseName"));
				map1.put("courseCode", map.get("courseCode"));
				if (!DateUtil.isDateNull((Date)map.get("startDate")) && !DateUtil.isDateNull((Date)map.get("endDate")))
					map1.put("period", String.format("%s - %s", dsdf.format(map.get("startDate")), dsdf.format(map.get("endDate"))));
				else
					map1.put("period", "");
				map1.put("teacher", map.get("teacher"));
				map1.put("room", map.get("room"));
				map1.put("price", map.get("price"));
				unionList.add(map1);
			}
			for (Map<String, Object> map : assessmentList) {
				if (!(Boolean)map.get("enabled"))
					continue;
				Map<String, Object> map1 = new HashMap<String, Object>();
				map1.put("courseName", map.get("description"));
				map1.put("courseCode", "");
				map1.put("period", dsdf.format(map.get("date")));
				map1.put("teacher", map.get("teacher"));
				map1.put("room", map.get("room"));
				map1.put("price", map.get("price"));
				unionList.add(map1);
			}
			for (Map<String, Object> map : tokenList) {
				if (!(Boolean)map.get("enabled"))
					continue;
				Map<String, Object> map1 = new HashMap<String, Object>();
				map1.put("courseName", map.get("tokenCcyName"));
				map1.put("courseCode", "");
				map1.put("period", "");
				map1.put("teacher", "");
				map1.put("room", "");
				map1.put("remark", map.get("remark"));
				map1.put("price", map.get("price"));
				unionList.add(map1);
			}

			UniLog.log1("docWidth:%d", docWidth);
			tcolMap.put("courseName", new TCol(172, "Course(s)"));
			tcolMap.put("courseCode", new TCol(80, "Course\nnumber"));
			tcolMap.put("period", new TCol(155, "Period"));
			tcolMap.put("teacher", new TCol(115, "Teacher"));
			tcolMap.put("room", new TCol(115, "Room"));
			tcolMap.put("price", new TCol(90, PdfContentByte.ALIGN_RIGHT, "Price"));

			ttcolMap.put("courseName", new TCol(172, "Course(s)"));
			ttcolMap.put("remark", new TCol(465, "Remark"));
			ttcolMap.put("price", new TCol(90, PdfContentByte.ALIGN_RIGHT, "Price"));
		}
		
		int sumTColWidth() {
			int w = 0;
			for (TCol tcol : tcolMap.values())
				w += tcol.width;
			return w;
		}

		int tColLeft(String key) {
			int o = 0;
			Map<String, TCol> map = ttcolMap.containsKey(key) ? ttcolMap : tcolMap;
			for (Map.Entry<String, TCol> entry : map.entrySet()) {
				if (entry.getKey().equals(key))
					break;
				o += entry.getValue().width;
			}
			return o;
		}

		int tColWidth(String key) {
			return ttcolMap.containsKey(key) ? ttcolMap.get(key).width : tcolMap.get(key).width;
		}

		int tColAlign(String key) {
			return ttcolMap.containsKey(key) ? ttcolMap.get(key).align : tcolMap.get(key).align;
		}
		
		void printTColVerticalLines(boolean remarkMode, int offset, int height) {
			int width = sumTColWidth();
			Map<String, TCol> map = remarkMode ? ttcolMap : tcolMap;
			for (Map.Entry<String, TCol> entry : map.entrySet()) {
				String key = entry.getKey();
				currCell.addItem(new ChnftrBuilder.LineItem(tColLeft(key), offset).setRect(0, 0, 0, height));
			}
			currCell.addItem(new ChnftrBuilder.LineItem(width, offset).setRect(0, 0, 0, height));
		}

		void printTColHorizontalLine(int offset) {
			currCell.addItem(new ChnftrBuilder.LineItem(0, offset).setRect(0, 0, sumTColWidth(), 0));
		}

		void printTColHeaders(int offset) {
			for (Map.Entry<String, TCol> entry : tcolMap.entrySet()) {
				String key = entry.getKey();
				TCol tcol = entry.getValue();
				currCell.addItem(new ChnftrBuilder.TextItem(tColLeft(key) + 2, offset)).setAlign(tcol.align, tcol.width - 4).setText(tcol.header);
			}
		}

		void printTColText(String key, int offset, int align, String value) {
			currCell.addItem(new ChnftrBuilder.TextItem(tColLeft(key) + 2, offset)).setAlign(align, tColWidth(key) - 4).setText(value);
		}
		
		void printTColText(String key, int offset, String value) {
			printTColText(key, offset, tColAlign(key), value);
		}
		
		void printTColTotalBox(int offset, int o, int height, String label) {
			currCell.addItem(new ChnftrBuilder.LineItem(tColLeft("room"), offset).setRect(0, 0, 0, height));
			currCell.addItem(new ChnftrBuilder.LineItem(tColLeft("price"), offset).setRect(0, 0, 0, height));
			currCell.addItem(new ChnftrBuilder.LineItem(sumTColWidth(), offset).setRect(0, 0, 0, height));
			currCell.addItem(new ChnftrBuilder.LineItem(0, offset + 25).setRect(tColLeft("room"), 0, sumTColWidth(), 0));
			printTColText("room", offset + o, label);
		}

		boolean offset(int o, int height) throws Exception {
			offset += o;
			if (currCell.getAbsoluteY() + offset + height > maxHeight) {
				currCell.build();
				builder.P();
				currCell = new ChnftrBuilder.Cell(builder, docLeft, docTop, 0, 0).setFontAndSize(fontPt, engFont, chnFont);
				offset = 0;
				return true;
			}
			return false;
		}

		boolean offset(int o) throws Exception {
			return offset(o, rowHeight);
		}
		
		class TCol {
			int width;
			int align;
			String header;

			TCol(int width, int align, String header) {
				this.width = width;
				this.align = align;
				this.header = header;
			}

			TCol(int width, String header) {
				this(width, PdfContentByte.ALIGN_LEFT, header);
			}
		}
		
		void printHeader() throws Exception {
			
			currCell.addItem(new ChnftrBuilder.PictureItem(150, offset).setAll(0, 0, 0, false, Sessions.getCurrent().getWebApp().getRealPath(leftLogo)));
			currCell.addItem(new ChnftrBuilder.PictureItem(450, offset).setAll(0, 0, 0, false, Sessions.getCurrent().getWebApp().getRealPath(rightLogo)));
			offset(130);
			currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setFontSize(15).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setText(coName);
			offset(50);
			currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setFontSize(15).setAlign(PdfContentByte.ALIGN_CENTER, docWidth).setText("OFFICIAL RECEIPT");
			offset(50);
			currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setFontSize(8).setText("School Registration Number:");
			currCell.addItem(new ChnftrBuilder.TextItem(180, offset)).setFontSize(8).setText(regNo);
			offset(15);
			currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setFontSize(8).setText("Registered Address:");
			currCell.addItem(new ChnftrBuilder.TextItem(180, offset)).setFontSize(8).setText(address);
			offset(15);
			currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setFontSize(8).setText("Telephone Number:");
			currCell.addItem(new ChnftrBuilder.TextItem(180, offset)).setFontSize(8).setText(telNo);
			offset(50);
			currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setFontSize(10).setText("Receipt number:");
			currCell.addItem(new ChnftrBuilder.TextItem(150, offset)).setFontSize(10).setText(
				String.format("%s", receiptNum)
			);
			offset(20);
			currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setFontSize(10).setText("Name of student:");
			currCell.addItem(new ChnftrBuilder.TextItem(150, offset)).setFontSize(10).setText(
				String.format("%s (%s)", getBr().getCellString("essd_name"), getBr().getCellString("essd_sdno"))
			);
			offset(20);
			currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setFontSize(10).setText("Payment Date:");
			currCell.addItem(new ChnftrBuilder.TextItem(150, offset)).setFontSize(10).setText(
				String.format("%s", DateUtil.isDateNull(paymentDate) ? "" : dsdf.format(paymentDate))
			);
			if (StringUtils.isNotBlank(chequeNo)) {
				offset(20);
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setFontSize(10).setText("Cheque number:");
				currCell.addItem(new ChnftrBuilder.TextItem(150, offset)).setFontSize(10).setText(chequeNo);
			}

			offset(50);
			currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setFontSize(10).setText(
				String.format("Received from %s the amount HKD %s for:", receivedFrom, getDecimalPrintString(grandTotalAmount))
			);
			offset(40);
		}
		
		void printDetail() throws Exception {
			String[] splitKeys = new String[] { "courseName", "courseCode", "teacher", "room", "remark" };
			for (Map<String, Object> map : unionList) {
				int rowCount = 1;
				for (String key : splitKeys) {
					List<String> strList = ChnftrParser.splitText((String)map.get(key), engFont, chnFont, fontPt, tColWidth(key) - 4);
					map.put(key + "Split", strList);
					rowCount = Math.max(rowCount, strList.size());
				}
				map.put("rowCount", rowCount);
			}
			
			printTColHorizontalLine(offset);
			printTColVerticalLines(false, offset, 40);
			printTColHeaders(offset + 7);
			offset(40);
			for (Map<String, Object> map : unionList) {
				boolean remarkMode = map.containsKey("remark");
				UniLog.log1("remarkMode:%b, price:%d", remarkMode, map.get("price"));
				printTColHorizontalLine(offset);
				int rh = 25 + rowHeight * ((Integer)map.get("rowCount") - 1);
				if (offset(0, rh))
					printTColHorizontalLine(offset);
				printTColVerticalLines(remarkMode, offset, rh);
				int o = offset + 7;
				for (String key : splitKeys) {
					int i = 0;
					for (String s : (List<String>)map.get(key + "Split")) {
						printTColText(key, o + i * rowHeight, s);
						i++;
					}
				}
				printTColText("period", o, (String)map.get("period"));
				printTColText("price", o, getDecimalPrintString((Double)map.get("price")));
				offset(rh, 0);
			}
			printTColHorizontalLine(offset);
			if (discount != 0) {
				offset(0, 25);
				printTColTotalBox(offset, 7, 25, "Discount:");
				printTColText("price", offset + 7, getDecimalPrintString(-discount));
				offset(25, 0);
			}
			offset(0, 25);
			printTColTotalBox(offset, 7, 25, "Total:");
			printTColText("price", offset + 7, getDecimalPrintString(grandTotalAmount));
			offset(25, 0);

			offset(30, 40);
			//currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setAlign(PdfContentByte.ALIGN_RIGHT, docWidth).setText("Payment by cheque / cash / bank transfer");
			currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setAlign(PdfContentByte.ALIGN_RIGHT, docWidth).setText(String.format("Payment by %s", paymentMethod));
			offset(20);
			//currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setAlign(PdfContentByte.ALIGN_RIGHT, docWidth).setText("(circle the appropriate)");
			offset(20);

			offset(150, 100);
			/*currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setAlign(PdfContentByte.ALIGN_RIGHT, docWidth).setText("[Company chop]");
			currCell.addItem(new ChnftrBuilder.LineItem(0, offset + 40).setRect(docWidth - 200, 0, docWidth, 0));
			currCell.addItem(new ChnftrBuilder.TextItem(docWidth - 180, offset + 88)).setText("Received by");
			currCell.addItem(new ChnftrBuilder.LineItem(0, offset + 100).setRect(docWidth - 100, 0, docWidth, 0));*/
			currCell.addItem(new ChnftrBuilder.PictureItem(docWidth - 120, offset).setAll(0, 0, 0, false, Sessions.getCurrent().getWebApp().getRealPath(chopImg)));
		}

		ChnftrParser createChnftrParser(InputStream inStream) throws Exception {
			return new ChnftrParser(inStream, pageSize, docWidthPx, docHeightPx, ChnftrParser.CHNFTR_DPI, 11, 6);
		}

		ByteArrayOutputStream build() throws Exception {
			ByteArrayInputStream inStream = null;
			ByteArrayOutputStream outStream = null;
			try {
				currCell = new ChnftrBuilder.Cell(builder, docLeft, docTop, 0, 0).setFontAndSize(fontPt, engFont, chnFont);

				printHeader();
				printDetail();

				//builder write to stream
				currCell.build();
				outStream = new ByteArrayOutputStream();
				builder.writeTo(outStream);
				outStream.close();

				//ChnftrParser read builder stream
				inStream = new ByteArrayInputStream(outStream.toByteArray());
				ChnftrParser parser = createChnftrParser(inStream);

				//ChnftrParser write to stream
				outStream = new ByteArrayOutputStream();
				parser.print(outStream);
			}
			catch (Exception e) {
				e.printStackTrace();
				throw e;
			}
			finally {
				try {
					if (inStream != null)
						inStream.close();
					if (outStream != null)
						outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return outStream;
		}
	}
	
	private String savePayment(String receivedFrom, double grandTotalAmount, double discount, String paymentMethod, String chequeNo, Date paymentDate, 
							List<Map<String, Object>> courseList, List<Map<String, Object>> assessmentList, List<Map<String, Object>> tokenList) throws Exception {
		int phrg = 0;
		String phno = "";
		BiResult brPayment = null;
		BiResult brPaymentDet = null;
		BiResult brStudentCourse = null;
		try {
			brPayment = sessionHelper.newBiResult("edu.Payment");
			brStudentCourse = sessionHelper.newBiResult("edu.StudentCourse");
			
			brPaymentDet = brPayment.getSubLink("edu.PaymentDet");
			int studentRg = getBr().getCellInt("essd_rg");
			String cardNo = getBr().getCellString("essd_cardno");
			brStudentCourse.clearCondition();
			//brStudentCourse.addCustomCondition(String.format("essbsd_sdrg = %d and essbsd_status = 'New'", studentRg));
			brStudentCourse.addCustomCondition(String.format("essbsd_sdrg = %d and essbsd_status <> 'Cancelled'", studentRg));
			Map<Integer, Map<String, Object>> essbMapMap = new HashMap<Integer, Map<String, Object>>();
			ReturnMsg rtn;
			if ((rtn = brStudentCourse.query(true, false)).getStatus()) {
				for (int i = 0; i < brStudentCourse.getRowCount(); i++) {
					brStudentCourse.fetch(false, i);
					Map<String, Object> map = new HashMap<String, Object>();
					map.put("startDate", brStudentCourse.getCellDate("essbsd_startdate"));
					map.put("endDate", brStudentCourse.getCellDate("essbsd_enddate"));
					essbMapMap.put(brStudentCourse.getCellInt("essbsd_avrg"), map);
				}
			}
			else
				throw new Exception(rtn.getMsg());

			brPayment.clearCurrentRec();
			brPayment.beginWork();
			brPayment.setColumnRg(brPayment.getCurrentCollection(), "esph_rg");
			phrg = brPayment.getCellInt("esph_rg");
			phno = brPayment.getCellString("esph_receiptno");
			UniLog.log1("phrg:%d, phno:%s", phrg, phno);
			if (phrg <= 0 || StringUtils.isBlank(phno))
				throw new Exception("Got payment rg fail");
			brPayment.getCell("esph_sdrg").set(studentRg);
			brPayment.getCell("essd_cardno").set(cardNo);
			brPayment.getCell("esph_recfrom").set(receivedFrom);
			brPayment.getCell("esph_totamt").set(grandTotalAmount + discount);
			brPayment.getCell("esph_discount").set(discount);
			brPayment.getCell("esph_gtotamt").set(grandTotalAmount);
			brPayment.getCell("esph_paymethod").set(paymentMethod);
			brPayment.getCell("esph_chequeno").set(chequeNo);
			brPayment.getCell("esph_paydate").set(paymentDate);
			brPayment.getCell("esph_cuser").set(sessionHelper.getLoginId());
			brPayment.getCell("esph_ctime").set(new Date());
			brPayment.getCell("esph_uuser").set(sessionHelper.getLoginId());
			brPayment.getCell("esph_utime").set(new Date());

			for (Map<String, Object> map : courseList) {
				if (!(Boolean)map.get("enabled"))
					continue;
				int courseRg = (Integer)map.get("courseRg");
				Map<String, Object> essbMap = essbMapMap.get(courseRg);
				if (essbMap == null)
					throw new Exception(String.format("Cannot insert course record %d", courseRg));
				//add payment detail
				BiCellCollection bc = brPaymentDet.newRowCollection(true);
				bc.getCell("espd_phrg").set(phrg);
				bc.getCell("espd_avrg").set(courseRg);
				bc.getCell("espd_snrg").set(0);
				bc.getCell("espd_amount").set(map.get("price"));
				bc.getCell("espd_startdate").set(map.get("startDate"));
				bc.getCell("espd_enddate").set(map.get("endDate"));
				bc.getCell("espd_numsession").set(map.get("numSession"));
				//update subscribe status
				Date essbStartDate = (Date)essbMap.get("startDate");
				Date essbEndDate = (Date)essbMap.get("endDate");
				UniLog.log1("essbStartDate:%s, essbEndDate:%s, %b, %b", essbStartDate, essbEndDate, DateUtil.isDateNull(essbStartDate), DateUtil.isDateNull(essbEndDate));
				if (DateUtil.isDateNull(essbStartDate)) {
					brPayment.getSelectUtil().executeUpdate("update essubscribe set essb_status = 'Confirmed', essb_startdate = ?, essb_enddate = ? where essb_type=0 and essb_avrg = ? and essb_sdrg = ?",
							new Wherecl()
								.appendArgument((Date)map.get("startDate"))
								.appendArgument((Date)map.get("endDate"))
								.appendArgument(bc.getCellInt("espd_avrg"))
								.appendArgument(brPayment.getCellInt("esph_sdrg"))
								);
				}
				else if (DateUtil.isDateNull(essbEndDate) || ((Date)map.get("endDate")).compareTo(essbEndDate) > 0) {
					brPayment.getSelectUtil().executeUpdate("update essubscribe set essb_status = 'Confirmed', essb_enddate = ? where essb_type=0 and essb_avrg = ? and essb_sdrg = ?",
							new Wherecl()
								.appendArgument((Date)map.get("endDate"))
								.appendArgument(bc.getCellInt("espd_avrg"))
								.appendArgument(brPayment.getCellInt("esph_sdrg"))
								);
				}
			}

			for (Map<String, Object> map : assessmentList) {
				if (!(Boolean)map.get("enabled"))
					continue;
				//add payment detail
				BiCellCollection bc = brPaymentDet.newRowCollection(true);
				bc.getCell("espd_phrg").set(phrg);
				bc.getCell("espd_avrg").set(0);
				bc.getCell("espd_snrg").set(map.get("sessionRg"));
				bc.getCell("espd_amount").set(map.get("price"));
				bc.getCell("espd_startdate").set(map.get("date"));
				bc.getCell("espd_enddate").set(map.get("date"));
				bc.getCell("espd_numsession").set(1);
				brPayment.getSelectUtil().executeUpdate("update esattendance set esat_status = 'N/A' where esat_snrg = ? and esat_attype = 'SD' and esat_atrg = ? and esat_status = ''",
						new Wherecl()
							.appendArgument(bc.getCellInt("espd_snrg"))
							.appendArgument(brPayment.getCellInt("esph_sdrg"))
							);
			}

			for (Map<String, Object> map : tokenList) {
				if (!(Boolean)map.get("enabled"))
					continue;
				//add payment detail
				BiCellCollection bc = brPaymentDet.newRowCollection(true);
				bc.getCell("espd_phrg").set(phrg);
				bc.getCell("espd_avrg").set(0);
				bc.getCell("espd_snrg").set(0);
				bc.getCell("espd_amount").set(map.get("price"));
				bc.getCell("espd_tokenccy").set(map.get("tokenCcy"));
				bc.getCell("espd_remark").set(map.get("remark"));
			}

			rtn = brPayment.addCurrent();
			if (!rtn.getStatus())
				throw new Exception(rtn.getMsg());
			brPayment.commitWork();
		}
		catch (Exception e) {
			e.printStackTrace();
			if (brPayment != null)
				brPayment.rollbackWork();
			throw e;
		}
		finally {
			if (brStudentCourse != null)
				brStudentCourse.close();
			if (brPaymentDet != null)
				brPaymentDet.close();
			if (brPayment != null)
				brPayment.close();
		}
		return phno;
	}
	
	private void calcStudentCourseField() {
		UniLog.log("calcStudentCourseField");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		ReturnMsg rtn;
		BiResult brSession = null;
		try {
			brSession = sessionHelper.newBiResult("edu.CourseSessionDet");
			BiResult brCourse = getBr().getSubLink("edu.StudentCourse");
			BiResult brAttendance = getBr().getSubLink("edu.StudentAttendance");
			BiResult brTokenBal = getBr().getSubLink("edu.TokenBal");
			Map<String, Double> tMap = new HashMap<String, Double>(); //sum(Courses tentative balance) group by tokenCcy
			for (int i = 0; i < brCourse.getRowCount(); i++) {
				brCourse.fetch(false, i);
				int courseRg = brCourse.getCellInt("essbsd_avrg");
				Date startDate = brCourse.getCellDate("essbsd_startdate");
				Date endDate = brCourse.getCellDate("essbsd_enddate");
				String tokenCcy = brCourse.getCellString("eaav0_tokenccy");
				double sessionFee = brCourse.getCellDouble("eaav0_fee");

				Map<Integer, Boolean> sMap = new HashMap<Integer, Boolean>();

				//calc session remain
				if (!DateUtil.isDateNull(startDate) && !DateUtil.isDateNull(endDate) && endDate.compareTo(DateUtil.today()) >= 0) {
					Date startDate1 = (startDate.compareTo(DateUtil.today()) > 0) ? startDate : DateUtil.today();
					brSession.clearCondition();
					brSession.addCustomCondition(String.format("essncs_avrg = %d and essncs_date between '%s' and '%s'", courseRg, sdf.format(startDate1), sdf.format(endDate)));
					if ((rtn = brSession.query(true, false)).getStatus()) {
						while (brSession.next()) {
							int sessionRg = brSession.getCellInt("essncs_rg");
							sMap.put(sessionRg, false);
						}
					}
					else
						throw new Exception(rtn.getMsg());
				}

				//calc session completed count(Present Count+Absent Count)
				int completedCount = 0;
				for (int j = 0; j < brAttendance.getRowCount(); j++) {
					brAttendance.fetch(false, j);
					int courseRg1 = brAttendance.getCellInt("essn_avrg");
					int sessionRg1 = brAttendance.getCellInt("esatsd_snrg");
					String attStatus = brAttendance.getCellString("esatsd_status");
					if (courseRg1 == courseRg) {
						if (StringUtils.equalsAny(attStatus, "Present", "Absent"))
							completedCount++;
						if (sMap.containsKey(sessionRg1))
							sMap.put(sessionRg1, true);
					}
				}
				brCourse.getCell("essbsd_sesscomp").set(completedCount);
				
				//calc session remain 
				int sessionRemain = 0;
				for (Map.Entry<Integer, Boolean> entry : sMap.entrySet()) {
					if (!entry.getValue())
						sessionRemain++;
				}
				brCourse.getCell("essbsd_sessremain").set(sessionRemain);

				//calc tentative balance
				double tentativeBalance = sessionRemain * sessionFee;
				brCourse.getCell("essbsd_tentatibal").set(tentativeBalance);
				
				//sum tentative balance
				Double ttb = tMap.get(tokenCcy);
				if (ttb == null)
					tMap.put(tokenCcy, tentativeBalance);
				else
					tMap.put(tokenCcy, ttb + tentativeBalance);
			}

			Map<Integer, Integer> sMap = new HashMap<Integer, Integer>();
			for (int i = brAttendance.getRowCount() - 1; i >= 0; i--) {
				brAttendance.fetch(false, i);
				int type = brAttendance.getCellInt("essn_type");
				int courseRg = brAttendance.getCellInt("essn_avrg");
				String attStatus = brAttendance.getCellString("esatsd_status");

				//session no offset
				int sessionNoOffset = 0;
				for (int j = 0; j < brCourse.getRowCount(); j++) {
					brCourse.fetch(false, j);
					if (brCourse.getCellInt("essbsd_avrg") == courseRg) {
						sessionNoOffset = brCourse.getCellInt("essbsd_sessoffset");
						break;
					}
				}

				if (StringUtils.equalsAny(attStatus, "Present", "Absent")) {
					int numSession = 0;
					Integer n = 0;
					if (type == 0) {
						numSession = brAttendance.getCellInt("eaav0_numsession");
						if (numSession > 0) {
							n = sMap.get(courseRg);
							if (n == null)
								n = sessionNoOffset;
							if (++n > numSession)
								n = 1;
							sMap.put(courseRg, n);
						}
					}
					else {
						numSession = 1;
						n = 1;
					}
					brAttendance.getCell("esatsd_sessionno").set(String.format("%02d/%02d", n, numSession));
				}
				else
					brAttendance.getCell("esatsd_sessionno").set("");
			}

			for (int i = 0; i < brTokenBal.getRowCount(); i++) {
				brTokenBal.fetch(false, i);
				String tokenCcy = brTokenBal.getCellString("tkbal_ccy");
				double tokenBalance = brTokenBal.getCellDouble("tkbal_ostqty");
				
				//andrew220530 add overdue notification
				if (StringUtils.isNotBlank(tokenCcy) && tokenBalance < 0) {
					ZkUtil.showWarnMsg("Overdue Notification. %s:%.02f",tokenCcy, tokenBalance);
				}
				
				Double courseTentativeBalance = tMap.get(tokenCcy);
				if (courseTentativeBalance == null)
					courseTentativeBalance = 0.0;
				brTokenBal.getCell("tkbal_tenremainbal").set(tokenBalance - courseTentativeBalance);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			brSession.close();
		}
	}
	
	public static class DoSetLoginPassword {
		private SessionHelper sessionHelper;
		private String loginId, userName, userChnName, accessRight;
		private String passwordStrengthDesc, passwordStatus;
		public DoSetLoginPassword(final SessionHelper sessionHelper, final String loginId, final String userName, final String userChnName, final String accessRight, Component comp) {
			this.sessionHelper = sessionHelper;
			this.loginId = loginId;
			this.userName = userName;
			this.userChnName = userChnName;
			this.accessRight = accessRight;

			final GridHelper gh = new GridHelper(2);
			gh.getColumn(0).setWidth("130px");
			gh.getColumn(0).setAlign("end");
			gh.getColumn(1).setHflex("1");
			final Textbox tbPassword = new Textbox();
			final Textbox tbConfirmPassword = new Textbox();
			final Div divMeter = new Div();
			final Div divMeterInner = new Div();
			final Label lbMeterMsg = new Label();
			tbPassword.setWidth("80px");
			tbConfirmPassword.setWidth("80px");
			tbPassword.setType("password");
			tbConfirmPassword.setType("password");
			tbPassword.setMaxlength(10);
			tbConfirmPassword.setMaxlength(10);
			divMeter.setWidth("240px");
			divMeter.setSclass("meter");
			divMeterInner.setSclass("meter-inner");
			gh.addRow(new Label("Password"), new Hlayout() {{
				appendChild(tbPassword);
				appendChild(new Label("(6-10 char, one number, one alphabetic character)") {{
					setStyle("font-size:12px !important");
				}});
			}});
			gh.addRow(new Label("Confirm Password"), tbConfirmPassword);
			gh.addRow(new Label("Password Strength"), new Vlayout() {{
				appendChild(divMeter);
				appendChild(lbMeterMsg);
			}});
			gh.getRow(2).setHeight("40px");
			divMeter.appendChild(divMeterInner);
			final Map<String, Button> dialogButtonMap = new HashMap<String, Button>();

			tbPassword.addEventListener(Events.ON_CHANGING, new ZkBiEventListener<InputEvent>() {
				@Override
				public void onZkBiEvent(InputEvent event) throws Exception {
					UniLog.log1("event:%s, value:%s", event, event.getValue());
					String[] desc = new String[] { "", "Very Weak", "Weak", "Medium", "Medium", "Strong", "Strongest" };
					int score = getPasswordStrength(event.getValue());
					UniLog.log1("score:%d", score);
					switch (score) {
					case 1:
					case 2:
						divMeter.setSclass("meter meter-red");
						break;
					case 3:
					case 4:
						divMeter.setSclass("meter meter-orange");
						break;
					case 5:
					case 6:
						divMeter.setSclass("meter meter-green");
						break;
					default:
						divMeter.setSclass("meter");
						break;
					}
					divMeterInner.setWidth(score * 240 / desc.length + "px");
					dialogButtonMap.get("setLoginPassword").setDisabled(validationLoginPassword(event.getValue(), tbConfirmPassword.getText()) != null);
					passwordStrengthDesc = desc[score];
					passwordStatus = validationLoginPassword1(event.getValue(), tbConfirmPassword.getText());
					lbMeterMsg.setValue(joinString(", ", passwordStrengthDesc, passwordStatus));
				}
			});
			tbConfirmPassword.addEventListener(Events.ON_CHANGING, new ZkBiEventListener<InputEvent>() {
				@Override
				public void onZkBiEvent(InputEvent event) throws Exception {
					UniLog.log1("event:%s, value:%s", event, event.getValue());
					dialogButtonMap.get("setLoginPassword").setDisabled(validationLoginPassword(tbPassword.getText(), event.getValue()) != null);
					passwordStatus = validationLoginPassword1(tbPassword.getText(), event.getValue());
					lbMeterMsg.setValue(joinString(", ", passwordStrengthDesc, passwordStatus));
				}
			});

			MessageboxDlg dlg = ZkUtil.buildMessageboxDlg("Set Login Password", 
				gh, 
	    		new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.ABORT, Messagebox.Button.IGNORE, Messagebox.Button.CANCEL}, 
	    			new String[] {"Set Login Password", "Assign Random Password", "Remove Password", "Cancel"},
	    			comp.getRoot(),
	  				new EventListener<Messagebox.ClickEvent>(){
	   					@Override
	   					public void onEvent(ClickEvent event) throws Exception {
	   						if (event.getButton() == null)
	   							return;
	   						switch (event.getButton()) {
	   						case OK: //Set Login Password
	   							String password = tbPassword.getText();
	   							String confirmPassword = tbConfirmPassword.getText();
	   							String errMsg = validationLoginPassword(password, confirmPassword);
	   							if (errMsg != null) {
	   								ZkUtil.errMsg(errMsg);
	   								event.stopPropagation();
	   								return;
	   							}
	   							try {
	   								setLoginPassword(password);
	   								ZkUtil.msg("Set login password successfully");
	   							}
	   							catch (Exception e) {
	   								ZkUtil.errMsg(e.getMessage());
	   							}
	   							break;
	   						case ABORT: //Assign Random Password
	   							Messagebox.show("Confirm Assign Random Password?", "Confirm", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new ZkBiEventListener<Event>() {
									@Override
 									public void onZkBiEvent(Event event) {
										if (event.getName().equals(Events.ON_OK)) {
											try {
												String randomPwd = getRandomPassword();
												setLoginPassword(randomPwd);
												ZkUtil.msg(String.format("Set login password successfully, assigned password: %s", randomPwd));
											}
											catch (Exception e) {
												ZkUtil.errMsg(e.getMessage());
											}
										}
									}
	   							});
	   							break;
	   						case IGNORE: //Remove Password
	   							Messagebox.show("Confirm Remove Password?", "Confirm", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new ZkBiEventListener<Event>() {
									@Override
 									public void onZkBiEvent(Event event) {
										if (event.getName().equals(Events.ON_OK)) {
											try {
												removeLoginRecord();
												ZkUtil.msg("Remove password successfully");
											}
											catch (Exception e) {
												ZkUtil.errMsg(e.getMessage());
											}
										}
									}
	   							});
	   							break;
	   						default:
	   							break;
	   						}
					}}
	    	);
			dlg.setWidth("610px");
			dlg.setStyle("max-width:100%");
	      	dlg.doHighlighted();
	
			//find buttons
			for (Component cbtn : dlg.queryAll("Button")) {
				Button btn = (Button)cbtn;
				if (StringUtils.equals(btn.getLabel(), "Set Login Password")) {
					dialogButtonMap.put("setLoginPassword", btn);
					btn.setDisabled(true);
				}
				else if (StringUtils.equals(btn.getLabel(), "Assign Random Password")) {
					dialogButtonMap.put("assignRandomPassword", btn);
					if (!sessionHelper.isAdminUser() && !sessionHelper.hasAccessRight("#edu") && !sessionHelper.hasAccessRight("#eduadmin"))
						btn.setVisible(false);
				}
				else if (StringUtils.equals(btn.getLabel(), "Remove Password")) {
					dialogButtonMap.put("removePassword", btn);
					btn.setSclass("zkbi-deletebutton");
					btn.setDisabled(true);
					if (!sessionHelper.isAdminUser() && !sessionHelper.hasAccessRight("#edu") && !sessionHelper.hasAccessRight("#eduadmin"))
						btn.setVisible(false);
				}
			}
			
			
			//find exist login user
			BiResult brLoginUser = null;
			try {
				brLoginUser = BiResultHelper.create(sessionHelper, "erpv4.LoginUser", String.format("lgu_login = '%s'", loginId), -1, null);
				if (brLoginUser.next())
					dialogButtonMap.get("removePassword").setDisabled(false);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			finally {
				if (brLoginUser != null)
					brLoginUser.close();
			}
		}

		private boolean textRegexFound(String text, String regex) {
			return Pattern.compile(regex).matcher(text).find();
		}
		
		private int getPasswordStrength(String text) {
			int score = 0;
			if (text.length() > 0)
				score++;
			if (text.length() > 6)
				score++;
			if (textRegexFound(text, "[a-z]") && textRegexFound(text, "[A-Z]"))
				score++;
			if (textRegexFound(text, "\\d+"))
				score++;
			if (textRegexFound(text, ".[!,@,#,$,%,^,&,*,?,_,~,-,(,)]"))
				score++;
			if (text.length() > 12)
				score++;
			if (text.length() == 0)
				score = 0;
			return score;
		}
		
		private String validationLoginPassword(String password, String confirmPassword) {
			if (password.length() < 6 || password.length() > 10)
	   			return "The password must be 6-10 characters";
			if (!textRegexFound(password, "[A-Za-z]") || !textRegexFound(password, "[0-9]"))
				return "The password must have at least one alphabetic character, one number";
			if (!password.equals(confirmPassword))
				return "The password confirmation does not match";
			return null;
		}

		private String validationLoginPassword1(String password, String confirmPassword) {
			if (password.length() < 6 || password.length() > 10)
	   			return "Not Pass";
			if (!textRegexFound(password, "[A-Za-z]") || !textRegexFound(password, "[0-9]"))
				return "Not Pass";
			if (!password.equals(confirmPassword))
				return "Passwords does not match";
			return "Pass";
		}
		
		private String getRandomPassword() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < 10; i++) {
				if (i != 0 && i != 1)
					sb.append(i);
			}
			for (char c = 'A'; c <= 'Z'; c++) {
				if (c != 'I' && c != 'L' && c != 'O')
					sb.append(c);
			}
			for (char c = 'a'; c <= 'z'; c++) {
				if (c != 'i' && c != 'l' && c != 'o')
					sb.append(c);
			}
			Random r = new Random();
			String src = sb.toString();
			do {
				sb.setLength(0);
				for (int i = 0; i < 10; i++) {
					int p = r.nextInt(src.length());
					sb.append(src.charAt(p));
				}
			} while (validationLoginPassword(sb.toString(), sb.toString()) != null);
			return sb.toString();
		}

		private void setLoginPassword(String password) throws Exception {
			UniLog.log1("login id:%s, password:%s", loginId, password);
	
			BiResult brLoginUser = null;
			BiResult brLoginUserQuery = null;
			BiResult brWebMenuTree = null;
			BiResult brWebMenuTreeQuery = null;
			try {
				brLoginUser = sessionHelper.newBiResult("erpv4.LoginUser");
				brLoginUserQuery = sessionHelper.newBiResult("erpv4.LoginUser");
				brWebMenuTree = brLoginUser.getSubLink("WebMenuTree");
				brWebMenuTreeQuery = brLoginUserQuery.getSubLink("WebMenuTree");
	
				brLoginUserQuery.clearCondition();
				brLoginUserQuery.addCustomCondition(String.format("lgu_login = '%s'", loginId));
	
				ReturnMsg rtn;
				brLoginUser.beginWork();
				if ((rtn = brLoginUserQuery.query(true, false)).getStatus()) {
					if (brLoginUserQuery.next(false)) {
						if (sessionHelper.getAESKey() != null) {
							String c = brLoginUserQuery.getCellString("lgu_credentials");
							JSONObject jo;
							if (StringUtils.isNotBlank(c)) {
								String js = ZkUtil.decryptStrFromBase64(sessionHelper, c);
								jo = new JSONObject(js);
							}
							else
								jo = new JSONObject();
							jo.put("lgu_bpcode", password);
							brLoginUser.getSelectUtil().executeUpdate("update loginuser set lgu_name = ?, lgu_chnname = ?, lgu_bpcode = '', lgu_credentials = ? where lgu_login = ?",
									new Wherecl()
										.appendArgument(userName)
										.appendArgument(userChnName)
										.appendArgument(ZkUtil.encryptStrToBase64(sessionHelper, jo.toString()))
										.appendArgument(loginId)
										);
						}
						else {
							brLoginUser.getSelectUtil().executeUpdate("update loginuser set lgu_name = ?, lgu_chnname = ?, lgu_bpcode = ? where lgu_login = ?",
									new Wherecl()
										.appendArgument(userName)
										.appendArgument(userChnName)
										.appendArgument(password)
										.appendArgument(loginId)
										);
						}
						boolean flag = false;
						for (BiCellCollection bc : brWebMenuTreeQuery.getRowCollectionList()) {
							String ar = bc.getCellString("webmt_parent");
							UniLog.log1("accessRight:%s", ar);
							if (StringUtils.equals(accessRight, ar)) {
								flag = true;
								break;
							}
						}
						if (!flag) {
							brLoginUser.getSelectUtil().executeUpdate("delete from webmenutree where webmt_user = ?", new Wherecl().appendArgument(loginId));
							brLoginUser.getSelectUtil().executeUpdate("insert into webmenutree (webmt_user, webmt_parent) values(?,?)", 
									new Wherecl().appendArgument(loginId) .appendArgument(accessRight));
						}
					}
					else {
						brLoginUser.clearCurrentRec();
						brLoginUser.getCell("lgu_login").set(loginId);
						brLoginUser.getCell("lgu_type").set("U");
						brLoginUser.getCell("lgu_name").set(userName);
						brLoginUser.getCell("lgu_chnname").set(userChnName);
						brLoginUser.getCell("lgu_bpcode").set(password);
						BiCellCollection bc = brWebMenuTree.newRowCollection(true);
						bc.getCell("webmt_parent").set(accessRight);
						rtn = brLoginUser.addCurrent();
						if (!rtn.getStatus())
							throw new Exception(rtn.getMsg());
						if (sessionHelper.getAESKey() != null) {
							JSONObject jo = new JSONObject();
							jo.put("lgu_bpcode", password);
							brLoginUser.getCell("lgu_bpcode").resetValue();
							brLoginUser.getCell("lgu_credentials").set(ZkUtil.encryptStrToBase64(sessionHelper, jo.toString()));
						}
					}
				}
				else
					throw new Exception(rtn.getMsg());
				brLoginUser.commitWork();
			}
			catch (Exception e) {
				e.printStackTrace();
				if (brLoginUser != null)
					brLoginUser.rollbackWork();
				throw e;
			}
			finally {
				if (brWebMenuTree != null)
					brWebMenuTree.close();
				if (brLoginUser != null)
					brLoginUser.close();
				if (brWebMenuTreeQuery != null)
					brWebMenuTreeQuery.close();
				if (brLoginUserQuery != null)
					brLoginUserQuery.close();
			}
		}

		private void removeLoginRecord() throws Exception {
			BiResult brLoginUser = null;
			try {
				brLoginUser = sessionHelper.newBiResult("erpv4.LoginUser");
				brLoginUser.beginWork();
				brLoginUser.getSelectUtil().executeUpdate("delete from webmenutree where webmt_user = ?",
						new Wherecl()
							.appendArgument(loginId)
							);
				brLoginUser.getSelectUtil().executeUpdate("delete from loginuser where lgu_login = ?",
						new Wherecl()
							.appendArgument(loginId)
							);
				brLoginUser.commitWork();
			}
			catch (Exception e) {
				e.printStackTrace();
				if (brLoginUser != null)
					brLoginUser.rollbackWork();
				throw e;
			}
			finally {
				if (brLoginUser != null)
					brLoginUser.close();
			}
		}
	}

    public static void showErrMsg(Component comp, String p_format, Object...p_args){
       	Clients.evalJavaScript("$('#"+comp.getUuid()+"').notify(\""+String.format(p_format, p_args)+"\", { className: \"error\", elementPosition:\"bottom right\", arrowShow: false, autoHideDelay: 5000 })");
    }

    public static void showMsg(Component comp, String p_format, Object...p_args){
       	Clients.evalJavaScript("$('#"+comp.getUuid()+"').notify(\""+String.format(p_format, p_args)+"\", { className: \"info\", elementPosition:\"bottom right\", arrowShow: false, autoHideDelay: 5000 })");
    }

	public static String joinString(String delimiter, Object... str) {
		StringBuilder sb = new StringBuilder();
		for (Object o : str) {
			if (StringUtils.isNotBlank(o.toString())) {
				if (sb.length() > 0)
					sb.append(delimiter);
				sb.append(o);
			}
		}
		return sb.toString();
	}

	public static Date unionDateTime(Date pDate, Date pTime) {
		Calendar cal = Calendar.getInstance();
		Calendar calTime = Calendar.getInstance();
		cal.setTime(pDate);
		calTime.setTime(pTime);
		cal.set(Calendar.HOUR_OF_DAY, calTime.get(Calendar.HOUR_OF_DAY));
		cal.set(Calendar.MINUTE, calTime.get(Calendar.MINUTE));
		cal.set(Calendar.SECOND, calTime.get(Calendar.SECOND));
		return cal.getTime();
	}
	
	public static String getDecimalFormatString(double d, DecimalFormat df) {
		if (d == 0)
			return df.format(0);
		else
			return df.format(d);
	}

	public static String getDecimalPrintString(double d) {
		return getDecimalFormatString(d, decFormatPrint);
	}
	
	public static double toDouble(String s) {
		return NumberUtils.toDouble(StringUtils.replace(s, ",", ""));
	}
	
	public static void setCheckboxAll(Checkbox cbAll, int count, int checkCount) {
		if (checkCount > 0) {
			if (checkCount == count)
				cbAll.setChecked(true);
		}
		else
			cbAll.setChecked(false);
	}

	public static Date dateTimeAfterHour(Date pDate, int offset) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(pDate);
		int hour = cal.get(Calendar.HOUR_OF_DAY) + offset;
		if (hour > 23) {
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
		}
		else
			cal.set(Calendar.HOUR_OF_DAY, hour);
		return cal.getTime();
	}
	
	public static void loadTokenCcy(SessionHelper sessionHelper, Map<String, String> tokenCcyMap) {
		if (!tokenCcyMap.isEmpty())
			return;
		BiResult brTokenCcy = null;
		try {
			brTokenCcy = BiResultHelper.create(sessionHelper, "edu.TokenCcy", null, -1, null);
			while (brTokenCcy.next())
				tokenCcyMap.put(brTokenCcy.getCellString("tkccy_ccy"), brTokenCcy.getCellString("tkccy_name"));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (brTokenCcy != null)
				brTokenCcy.close();
		}
	}
}