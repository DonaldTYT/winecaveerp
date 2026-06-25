package com.uniinformation.zkbi.edu;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.mail.internet.MimeUtility;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.text.StringSubstitutor;
import org.zkoss.zhtml.I;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Constraint;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Frozen;
import org.zkoss.zul.Html;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Space;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.impl.MessageboxDlg;

//import com.ibm.icu.text.DecimalFormat;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.edu.BiResultStudentTokenBal;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.edu.Student;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.GridHelper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiHelpDialog;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerPaymentReminder extends ZkBiComposerReport {
	private final static SimpleDateFormat ddf = new SimpleDateFormat("yyyy/MM/dd");
	private final static SimpleDateFormat eddf = new SimpleDateFormat("yyMMdd");
	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private final static SimpleDateFormat tdf = new SimpleDateFormat("HH:mm");
	private final static SimpleDateFormat etdf = new SimpleDateFormat("HHmm");
	private final static DecimalFormat fDecFmt = new DecimalFormat("0.00");
	private final static DecimalFormat feeDecFmt = new DecimalFormat(",###.##");
	private final static String TO_BE_ATTENDED = "(To be attended)";
	private final static boolean showLast10Entry = true;
	CellCollection rptCol;
   	Listbox s2Course, s2CourseEnd, s2CourseType, s2Tutor, s2Student, s2RemainingSession, s2RemainingSessionBySessNo, s2AllowPaymentReminder, s2LastReminderDate, s2SessionOverdue, s2TenRemainBal;
   	Checkbox cbAdjustValid;
   	Button btPaymentReminder;
   	Map<Object, Doublebox> adjustmentCompMap = new HashMap<Object, Doublebox>();
   	Map<Object, Label> adjustedFeeCompMap = new HashMap<Object, Label>();
   	Map<Object, Datebox> firstSessionDateCompMap = new HashMap<Object, Datebox>();

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		title = ZkSessionHelper.getSessionHelper().getLabel("Payment Reminder");
		super.doAfterCompose(p_comp);
		UniLog.log1("called");
    	queryBar.setVisible(false);
		bottomPanelVbox.setVisible(false);
		adjListboxHeight(45);

		//experimental 220314 make the long list easier to read by frozen first 3 column.
		if (!isMobile()) {
			listbox.appendChild(new Frozen() {{ this.setColumns(3); this.setStart(1); }});
		}
		

		String helpId = StringUtils.defaultIfBlank(Executions.getCurrent().getParameter("helpid"), "edu.PaymentReminder");
		UniLog.log1("helpId :%s", helpId);
   		new ZkBiHelpDialog(sessionHelper, btnHelp, masterWin, title, helpId, title);
	}

	@Override
    public void showListPanel() {
		super.showListPanel();
    	queryBar.setVisible(false);
		bottomPanelVbox.setVisible(false);
    }

	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
    	Div rpth = new Div();
    	zkbiListTop.getParent().insertBefore(rpth,zkbiListTop);
       	zkbiListTop.setVisible(false);
    	
	    final ZkForm zkf1 = new ZkForm(rpth, "zkf/edu/PaymentReminder.zul");
	    rptCol = new CellCollection();
	    try {
	    	s2Course = (Listbox)zkf1.getComponent("s2Course");
	    	s2CourseEnd = (Listbox)zkf1.getComponent("s2CourseEnd");
	    	s2CourseType = (Listbox)zkf1.getComponent("s2CourseType");
	    	s2Tutor = (Listbox)zkf1.getComponent("s2Tutor");
	    	s2Student = (Listbox)zkf1.getComponent("s2Student");
	    	s2RemainingSession = (Listbox)zkf1.getComponent("s2RemainingSession");
	    	s2RemainingSessionBySessNo = (Listbox)zkf1.getComponent("s2RemainingSessionBySessNo");
	    	s2AllowPaymentReminder = (Listbox)zkf1.getComponent("s2AllowPaymentReminder");
	    	s2LastReminderDate = (Listbox)zkf1.getComponent("s2LastReminderDate");
	    	s2SessionOverdue = (Listbox)zkf1.getComponent("s2SessionOverdue");
	    	s2TenRemainBal = (Listbox)zkf1.getComponent("s2TenRemainBal");
	    	cbAdjustValid = (Checkbox)zkf1.getComponent("cbAdjustValid");
	    	btPaymentReminder = (Button)zkf1.getComponent("btPaymentReminder");
	    	s2Course.appendChild(new Listitem("Any Course") {{setValue(null);}});
	    	s2CourseEnd.appendChild(new Listitem(">=Today or Blank") {{setValue(">=Today or Blank");}});
	    	s2CourseEnd.appendChild(new Listitem("Any") {{setValue(null);}});
	    	s2CourseType.appendChild(new Listitem("Any Type") {{setValue(null);}});
	    	s2CourseType.appendChild(new Listitem("Group") {{setValue("Group");}});
	    	s2CourseType.appendChild(new Listitem("Private") {{setValue("Private");}});
	    	s2Tutor.appendChild(new Listitem("Any Tutor") {{setValue(null);}});
	    	s2Student.appendChild(new Listitem("Any Student") {{setValue(null);}});
	    	s2RemainingSession.appendChild(new Listitem("<=Course Threshold") {{setValue("<=Course Threshold");}});
	    	s2RemainingSession.appendChild(new Listitem("<=0") {{setValue("<=0");}});
	    	s2RemainingSession.appendChild(new Listitem("<=1") {{setValue("<=1");}});
	    	s2RemainingSession.appendChild(new Listitem("<=2") {{setValue("<=2");}});
	    	s2RemainingSession.appendChild(new Listitem("<=3") {{setValue("<=3");}});
	    	s2RemainingSession.appendChild(new Listitem("<=4") {{setValue("<=4");}});
	    	s2RemainingSession.appendChild(new Listitem("<=5") {{setValue("<=5");}});
	    	s2RemainingSession.appendChild(new Listitem("Any") {{setValue(null);}});
	    	s2RemainingSessionBySessNo.appendChild(new Listitem("<=Course Threshold") {{setValue("<=Course Threshold");}});
	    	s2RemainingSessionBySessNo.appendChild(new Listitem("<=0") {{setValue("<=0");}});
	    	s2RemainingSessionBySessNo.appendChild(new Listitem("<=1") {{setValue("<=1");}});
	    	s2RemainingSessionBySessNo.appendChild(new Listitem("<=2") {{setValue("<=2");}});
	    	s2RemainingSessionBySessNo.appendChild(new Listitem("<=3") {{setValue("<=3");}});
	    	s2RemainingSessionBySessNo.appendChild(new Listitem("<=4") {{setValue("<=4");}});
	    	s2RemainingSessionBySessNo.appendChild(new Listitem("<=5") {{setValue("<=5");}});
	    	s2RemainingSessionBySessNo.appendChild(new Listitem("Any") {{setValue(null);}});
	    	s2AllowPaymentReminder.appendChild(new Listitem("Enable") {{setValue("Enable");}});
	    	s2AllowPaymentReminder.appendChild(new Listitem("Disable") {{setValue("Disable");}});
	    	s2AllowPaymentReminder.appendChild(new Listitem("Any") {{setValue(null);}});
	    	s2LastReminderDate.appendChild(new Listitem("Exclude Today (Exclude from Today)") {{setValue("<Today");}});
	    	s2LastReminderDate.appendChild(new Listitem("Exclude Today-1 (Exclude from Yesterday)") {{setValue("<Today-1");}});
	    	s2LastReminderDate.appendChild(new Listitem("Exclude Today-2 (Exclude from 2 days ago)") {{setValue("<Today-2");}});
	    	s2LastReminderDate.appendChild(new Listitem("Exclude Today-3 (Exclude from 3 days ago)") {{setValue("<Today-3");}});
	    	s2LastReminderDate.appendChild(new Listitem("Exclude Today-4 (Exclude from 4 days ago)") {{setValue("<Today-4");}});
	    	s2LastReminderDate.appendChild(new Listitem("Exclude Today-5 (Exclude from 5 days ago)") {{setValue("<Today-5");}});
	    	s2LastReminderDate.appendChild(new Listitem("Exclude Today-6 (Exclude from 6 days ago)") {{setValue("<Today-6");}});
	    	s2LastReminderDate.appendChild(new Listitem("Exclude Today-7 (Exclude from 7 days ago)") {{setValue("<Today-7");}});
	    	s2LastReminderDate.appendChild(new Listitem("Any") {{setValue(null);}});
	    	s2SessionOverdue.appendChild(new Listitem("Any") {{setValue(null);}});
	    	s2SessionOverdue.appendChild(new Listitem(">=1") {{setValue(">=1");}});
	    	s2SessionOverdue.appendChild(new Listitem(">=2") {{setValue(">=2");}});
	    	s2SessionOverdue.appendChild(new Listitem(">=3") {{setValue(">=3");}});
	    	s2SessionOverdue.appendChild(new Listitem(">=4") {{setValue(">=4");}});
	    	s2SessionOverdue.appendChild(new Listitem(">=5") {{setValue(">=5");}});
	    	s2TenRemainBal.appendChild(new Listitem("Any") {{setValue(null);}});
	    	s2TenRemainBal.appendChild(new Listitem(">0") {{setValue(">0");}});
	    	s2TenRemainBal.appendChild(new Listitem("=0") {{setValue("=0");}});
	    	s2TenRemainBal.appendChild(new Listitem("<0") {{setValue("<0");}});
	    	s2Course.setSelectedIndex(0);
	    	s2CourseEnd.setSelectedIndex(0);
	    	s2CourseType.setSelectedIndex(0);
	    	s2Tutor.setSelectedIndex(0);
	    	s2Student.setSelectedIndex(0);
	    	s2RemainingSession.setSelectedIndex(7);
	    	s2RemainingSessionBySessNo.setSelectedIndex(0);
	    	s2AllowPaymentReminder.setSelectedIndex(0);
	    	s2LastReminderDate.setSelectedIndex(3);
	    	s2SessionOverdue.setSelectedIndex(0);
	    	s2TenRemainBal.setSelectedIndex(0);
	    	cbAdjustValid.setChecked(true);
	    	
	    	loadPickCourseDatas();
	    	loadPickTutorDatas();
	    	loadPickStudentDatas();

	    	ZkUtil.setupSelect2(s2Course, true, false);
	    	ZkUtil.setupSelect2(s2CourseEnd, true, false);
	    	ZkUtil.setupSelect2(s2CourseType, true, false);
	    	ZkUtil.setupSelect2(s2Tutor, true, false);
	    	ZkUtil.setupSelect2(s2Student, true, false);
	    	ZkUtil.setupSelect2(s2RemainingSession, true, false);
	    	ZkUtil.setupSelect2(s2RemainingSessionBySessNo, true, false);
	    	ZkUtil.setupSelect2(s2AllowPaymentReminder, true, false);
	    	ZkUtil.setupSelect2(s2LastReminderDate, true, false);
	    	ZkUtil.setupSelect2(s2SessionOverdue, true, false);
	    	ZkUtil.setupSelect2(s2TenRemainBal, true, false);
	    	
	    	btPaymentReminder.setDisabled(true);

	    	zkf1.mapCellCollection(rptCol, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						UniLog.log1("event target:%s, id:%s", event.getTarget(), event.getTarget().getId());
						if(event.getTarget().getId().equals("btRecordQuery")) {
							/*
							//andrew211203 move the logic to BiResultStudentTokenBal
							if (result instanceof BiResultStudentTokenBal)
								((BiResultStudentTokenBal)result).clearSessionBalanceCacheMap();
							*/
							adjustmentCompMap.clear();
							adjustedFeeCompMap.clear();
							firstSessionDateCompMap.clear();
							result.clearOrderBy();
        			       	refresh(result, masterWin, (MultiSortMap) null, false);
        			       	zkbiListTop.setVisible(true);
        			       	
        			       	//disable the Payment Reminder button if the result list is blank
        			       	btPaymentReminder.setDisabled(listModelList.isEmpty());
						}
						else if(event.getTarget().getId().equals("btClear")) {
        			       	s2Course.setSelectedIndex(0);
	    	  			    s2CourseEnd.setSelectedIndex(0);
	    	  			    s2CourseType.setSelectedIndex(0);
	    	  			    s2Tutor.setSelectedIndex(0);
	    	  			    s2Student.setSelectedIndex(0);
	    	  			    s2RemainingSession.setSelectedIndex(7);
	    	  			    s2RemainingSessionBySessNo.setSelectedIndex(0);
	    	  			    s2AllowPaymentReminder.setSelectedIndex(0);
	    	  			    s2LastReminderDate.setSelectedIndex(3);
	    	  			    s2SessionOverdue.setSelectedIndex(0);
	    	  			    s2TenRemainBal.setSelectedIndex(0);
	    	  			    ZkUtil.setupSelect2(s2Course, true, false);
	    	  			    ZkUtil.setupSelect2(s2CourseEnd, true, false);
	    	  			    ZkUtil.setupSelect2(s2CourseType, true, false);
	    	  			    ZkUtil.setupSelect2(s2Tutor, true, false);
	    	  			    ZkUtil.setupSelect2(s2Student, true, false);
	    	  			    ZkUtil.setupSelect2(s2RemainingSession, true, false);
	    	  			    ZkUtil.setupSelect2(s2RemainingSessionBySessNo, true, false);
	    	  			    ZkUtil.setupSelect2(s2AllowPaymentReminder, true, false);
	    	  			    ZkUtil.setupSelect2(s2LastReminderDate, true, false);
	    	  			    ZkUtil.setupSelect2(s2SessionOverdue, true, false);
	    	  			    ZkUtil.setupSelect2(s2TenRemainBal, true, false);
	    	  			    cbAdjustValid.setChecked(true);
        			       	zkbiListTop.setVisible(false);
        			       	btPaymentReminder.setDisabled(true);
						}
						else if(event.getTarget().getId().equals("btPaymentReminderBatchOk")) {
							try {
								Pair<List<Map<String, Object>>, Integer> p = getBatchList(result, cbAdjustValid.isChecked());
								final List<Map<String, Object>> batchList = p.getLeft();
								final int emailCount = p.getRight();
								if (batchList.isEmpty()) {
									ZkUtil.showErrMsg("Please choose items");
									return;
								}
								if (emailCount == 0) {
									ZkUtil.showErrMsg("No email address found");
									return;
								}
								ZkBiMsgbox.show(ZkBiMsgbox.Type.question, String.format("Send %d payment reminder emails?", batchList.size()), new String[] {"Ok", "Cancel"}, new ZkBiEventListener<Event>() {
									@Override
									public void onZkBiEvent(Event event) throws Exception {
										ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
										if (btn.getName().equals("Ok")) {
											sendEmail(btPaymentReminder, batchList);
											Button btCancel = (Button) btPaymentReminder.getParent().query("#" + btPaymentReminder.getId() + "BatchCancel");
											if (btCancel != null)
												Events.echoEvent(Events.ON_CLICK, btCancel, null);
										}
									}
								});
							}
							catch (Exception e) {
								//e.printStackTrace();
								UniLog.log("error:" + e.getMessage());
								ZkUtil.errMsg(e.getMessage());
							}
						}
	    			}
	    		}
	    	);
	    	ZkUtil.setupBatchModeButton(btPaymentReminder, batchModeToggleButton);
	    } catch (CellException cex ) {
	    	UniLog.log(cex);
	    }
	}

	@Override
    protected BiResult getQueryResult(SessionHelper sessionHelper,String p_viewid, int p_sortIdx, boolean p_sortDesc) {
		BiResult br = super.getQueryResult(sessionHelper, p_viewid, p_sortIdx, p_sortDesc);
		if (isMobile()) {
			BiColumn bc = br.getColumnByLabel("essbsd_preview");
			if (bc != null)
				br.hideViewColumn(bc);
		}
		return br;
    }
	
	@Override
	protected void renderOneRecord_real(Listitem item, Object trStat, Vector listColumns,final BiResult result,final int idx,final Object ts) throws Exception {
		//Vlayout vl=null;
		Div divTb = null;
		int nColumns = 2;
		int curColumns = 0;
		Listcell lc;
		//UniLog.log("render record " + idx);
		if(isMobile()) {
			divTb = new Div();
			divTb.setId("mobile_divtb" + idx);
			divTb.setStyle("display:table;");
		}
		else{
			if(hasAUDColumn){
				lc = new Listcell("");
				lc.setStyle("padding-left:15px;text-align:left;");
				if(result.isMarkedDelete(ts)) {
					lc.setImage(IMG_DELETE);
				} else if(result.isMarkedUpdate(ts)) {
					lc.setImage(IMG_UPDATE);
				} else {
					if(!multiSelect && hasDetailButton) {
						Toolbarbutton tbb = new Toolbarbutton();
						tbb.setSclass("narrowtoolbarbutton");
						tbb.setImage("images/icons/zkweb/039-file-3-20x20.png");
						tbb.setTooltiptext("Record Detail");
						tbb.addEventListener(Events.ON_CLICK, itemClickListener);
						lc.appendChild(tbb);
						tbb.setAttribute("trStat", trStat);
						
						//copy all field to clipboard 
						addLcPopup(result, listColumns, -1, lc);
					}
				}
				lc.setParent(item);
			}
		}
		int colDisplayedCnt = 0;
		boolean hasHideMobileExclude = false;
		for(int ii = 0;ii<listColumns.size();ii++) {
			int i;
//			if(columnOrderArray != null) i = columnOrderArray.get(ii) ; else i = ii;
			i = ii;
			if(!isMobile()) {
				BiColumn biColumn = (BiColumn) listColumns.get(i);
				String str = result.getCell(biColumn.getLabel()).getColumnDisplayString();
				String sclass = result.getCell(biColumn.getLabel()).getColumnDisplayClass();
				int align = result.getCell(biColumn.getLabel()).getAlignment();
				final double courseFee = result.getCellDouble("eaav0_coursefee");
				final double tenRemainBalance = result.getCellDouble("essbsd_tenremainba");
				if (StringUtils.equals(biColumn.getLabel(), "essbsd_adjustment")) {
					lc = new Listcell();
					final Doublebox tb = new Doublebox();
					tb.setParent(lc);
					tb.setFormat("0.00");
					tb.setValue(adjustmentCompMap.containsKey(ts) ? adjustmentCompMap.get(ts).getValue() : 0.0);
					tb.addEventListener(Events.ON_CHANGING, new ZkBiEventListener<InputEvent>() {
						@Override
						public void onZkBiEvent(InputEvent event) throws Exception {
							UniLog.log1("event:%s", event);
							double adjustment = NumberUtils.toDouble(event.getValue());
							if (cbAdjustValid.isChecked()) {
								String errMsg = validAdjustment(adjustment, tenRemainBalance, courseFee);
								if (errMsg != null)
									Clients.showNotification(errMsg, "error", tb, "end_center", 5000, true); 
							}
							adjustedFeeCompMap.get(ts).setValue(fDecFmt.format(courseFee + adjustment));
						}
					});
					adjustmentCompMap.put(ts, tb);
				}
				else if (StringUtils.equals(biColumn.getLabel(), "essbsd_adjustedfee")) {
					if (adjustedFeeCompMap.containsKey(ts))
						str = adjustedFeeCompMap.get(ts).getValue();
					lc = new Listcell();
					Label lb = new Label(str);
					lb.setSclass(sclass);
					lb.setParent(lc);
					adjustedFeeCompMap.put(ts, lb);
				}
				else if (StringUtils.equals(biColumn.getLabel(), "essbsd_firstsessda")) {
					lc = new Listcell();
					final Datebox db = new Datebox();
					db.setFormat(ddf.toPattern());
					db.setWidth("110px");
					db.setParent(lc);
					db.setValue(firstSessionDateCompMap.containsKey(ts) ? firstSessionDateCompMap.get(ts).getValue() : null);
					firstSessionDateCompMap.put(ts, db);
				}
				else if (StringUtils.equals(biColumn.getLabel(), "essbsd_preview")) {
					lc = new Listcell();
					Button btn = new Button(biColumn.getEngName());
					btn.setTooltiptext("Preview Invoice");
					btn.setParent(lc);
					handlePreviewButtonEvent(btn, result, ts);
				}
				else {
					if(sclass != null) {
						lc = new Listcell();
						Label lb = new Label(str);
						lb.setSclass(sclass);
						lb.setParent(lc);
					} else {
						lc = new Listcell(str);
					}
				}
				lc.setParent(item);
				if(align != 0) {
					if(align > 0) {
						ZkUtil.appendStyle(lc, "text-align:left;");
					} else {
						ZkUtil.appendStyle(lc, "text-align:right;");
					}
					if(((align > 0 ? align : -align) & 2) != 0) {
						ZkUtil.appendStyle(lc, "word-wrap:word-break");
					}
				}
				/*
				if (biColumn.getColumnType().trim().matches("float|money|serial|integer|decimal")){
					ZkUtil.appendStyle(lc, "text-align:right;");
				}
				else{
					ZkUtil.appendStyle(lc, "text-align:left;");
				}
				*/
				if (doSearchSingle(lc.getLabel(),i)){
					ZkUtil.appendStyle(lc, "background-color:rgba(255,255,0,0.3);");
				}
				
				//ZkUtil.appendStyle(lc,"white-space:nowrap;overflow:hidden;text-overflow:ellipsis;");
				/*
				ZkUtil.appendStyle(lc,"white-space:nowrap;overflow:hidden;");
				if (!biColumn.getColumnType().trim().equals("date")){ //andrew210618: fix date field always show '...'
					ZkUtil.appendStyle(lc,"text-overflow:ellipsis;");
				}
				*/
				ZkUtil.appendStyle(lc,"white-space:nowrap;overflow:hidden;text-overflow:'';"); //andrew210618 fix any field show '...'
				
				//display popup content. may has performance issue
				if (!StringUtils.equalsAny(biColumn.getLabel(), "essbsd_adjustment", "essbsd_preview")) {
					if(addLcPopup(result, listColumns, i, lc)) {
//    						ZkUtil.appendStyle(lc, "color:rgb(26,13,171);font-style: italic;");
						ZkUtil.appendStyle(lc, "color:rgb(26,13,171);");
					}
				}
				colDisplayedCnt++;
			} 
			else { //for mobile
				BiColumn biColumn = (BiColumn) listColumns.get(i);
				Label lb0 = new Label(sessionHelper.getLabel(biColumn.getEngName()));
				String str = result.getCell(biColumn.getLabel()).getColumnDisplayString();
				Label lb = new Label(str);
				String sclass = result.getCell(biColumn.getLabel()).getColumnDisplayClass();
				if(sclass != null) {
					lb.setSclass(sclass);
				}
				boolean matchedFlag = doSearchSingle(lb.getValue(),i);
				
				Div divRow = new Div();
				divRow.setStyle("display:table-row;");
				Div divCell0 = new Div();
				divCell0.setStyle("display:table-cell;padding-right:10px;padding-bottom:0px;white-space:nowrap;color:#888;line-height:initial;");
				Div divCell = new Div();
				divCell.setStyle("display:table-cell;line-height:initial;");
				if (matchedFlag){
					lb0.setStyle("background-color:yellow;");
					lb.setStyle("background-color:yellow;");
				}
				divTb.appendChild(divRow);
				divRow.appendChild(divCell0);
				divRow.appendChild(divCell);
				divCell0.appendChild(lb0);
				divCell.appendChild(lb);
				/*
				if (biColumn.isExcludeForMobile() || 
						(sessionHelper.getMobileMaxCol() > 0 && colDisplayedCnt>=sessionHelper.getMobileMaxCol() && !matchedFlag)){
					divRow.setSclass("zkbi-hide-mobile-exclude");
					hasHideMobileExclude = true;
				}
				else{
					colDisplayedCnt++;
				}
				*/

				colDisplayedCnt++;
			}
		}
		if(isMobile()) {
			final Div divTb1 = divTb;
			if (hasHideMobileExclude) {
				divTb1.appendChild(new A(){{
					setHref("javascript:;");
					setStyle("display:inline-block;position:absolute;opacity:.7;right:0px;bottom:0px;border:1px solid #a7a5a6;background:#f0f0f0;color:#231f20;");
					appendChild(new I(){{
						setSclass("fa fa-ellipsis-h");
						setStyle("padding-left:3px;padding-right:3px;padding-top:2px;padding-bottom:2px;");
					}});
					addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							UniLog.log("showHideFieldDivCell event:" + event);
							Clients.evalJavaScript(
								String.format("var divTb = $('#' + jq('$%s').attr('id'));"
										+ "var he = divTb.find('.zkbi-hide-mobile-exclude');"
										+ "if (he.hasClass('zkbi-hide-mobile-exclude-cancel')){"
										+ "he.removeClass('zkbi-hide-mobile-exclude-cancel');"
										+ "}else{"
										+ "he.addClass('zkbi-hide-mobile-exclude-cancel');"
										+ "}", divTb1.getId(), divTb1.getId()));
						}
					});
				}});
			}
			lc =  new Listcell();
			divTb.setParent(lc);
			lc.setParent(item);
		}

//		if(result.getAggregateOrPivotList() != null) {
		if(result.aggregateOrPivotSize() > 0) {
			List <Component> lhdrs = item.getChildren();
			Object[] vals = result.getAggregateValues(idx);
//			int n = result.getAggregateOrPivotList().size();
			int n = result.aggregateOrPivotSize();
			int ncols = listColumns.size();
			DecimalFormat df = new DecimalFormat("##,###,###,##0.00");
			for(int i=0;i<n;i++) {
				if(i < vals.length && vals[i] != null) {
					if(vals[i] instanceof Double) {
						lc = new Listcell(df.format(vals[i]));
						ZkUtil.appendStyle(lc, "text-align:right;");
					} else {
						lc = new Listcell(""+vals[i]);
						ZkUtil.appendStyle(lc, "text-align:left;");
					}
				} else{
					lc = new Listcell("");
				}
				if(aggregateOffset <= 0) item.appendChild(lc); else item.insertBefore(lc, lhdrs.get(ncols+i-aggregateOffset));
				//lc.setParent(item);
			}
		}
		item.setAttribute("renderidx", idx);
	}
	
	private void handlePreviewButtonEvent(final Button btn, final BiResult result, final Object ts) {
		btn.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				final String contentTemplate = loadEmailHtmlContentTemplate(sessionHelper);
				if (contentTemplate == null) {
					ZkUtil.errMsg("Email content template not exist");
					return;
				}

				CellCollection cc = result.getRowCollectionO(ts);
				final String emailContent = ZkBiComposerPaymentReminder.buildEmailHtmlContent(sessionHelper, contentTemplate, "'images/logo/edu_logo.jpg'", "'images/logo/edu_logo1.jpg'", "'images/logo/edu_companychop.png'",
					"'images/logo/edu_logo.jpg'", "'images/whatsapp-icon-100.png'", "'images/facebook-icon-100.png'", "'images/instagram-icon-100.png'",
					"",
					DateUtil.now(),
					cc.getCellString("eaav0_tokenccy"),
					cc.getCellString("eaav0_name"), 
					cc.getCellString("eaav0_code"), 
					cc.getCellString("estt_name"), 
					cc.getCellString("eaav0_sessionday"), 
					cc.getCell("eaav0_sessiontime").getDate(), 
					cc.getCellInt("eaav0_sessionlen"), 
					cc.getCellInt("essbsd_sdrg"),
					cc.getCellString("essd_name"), 
					cc.getCellString("essd_sdno"), 
					cc.getCellInt("essbsd_sessremain"), 
					//cc.getCell("essbsd_startdate").getDate(),
					firstSessionDateCompMap.get(ts).getValue(),
					cc.getCell("essbsd_enddate").getDate(),
					cc.getCellString("essd_contactby"),
					cc.getCellString("essd_firstname"),
					cc.getCellInt("eaav0_numsession"),
					cc.getCellDouble("eaav0_coursefee"),
					adjustmentCompMap.get(ts).getValue(),
					showLast10Entry ?
						loadAttendanceHistoryV2(cc.getCellInt("essbsd_sdrg"), cc.getCellString("eaav0_tokenccy"))
						: loadAttendanceHistory(cc.getCellInt("essbsd_sdrg"), cc.getCellInt("essbsd_avrg"), cc.getCellInt("essbsd_sessoffset"), cc.getCellInt("eaav0_numsession"))
				);

				final Html html = new Html() {{
					setStyle("display:block;padding:50px 0");
					setContent(emailContent);
				}};
				final Div div = new Div() {{
					setStyle("display:flex;display:-webkit-flex;justify-content:center;-webkit-justify-content:center;background:white;overflow:auto");
					appendChild(new Div() {{
						setStyle("width:100%;max-width:900px;");
						appendChild(html);
					}});
				}};
				final MessageboxDlg dlg = ZkUtil.buildSimpleMessageboxDlg("Preview", 
					div, 
					new Messagebox.Button[] {Messagebox.Button.CANCEL}, 
					new String[] {"Close"}, btn.getRoot(), 
					new EventListener<Messagebox.ClickEvent>() {
						@Override
						public void onEvent(ClickEvent event) throws Exception {
						}
					}
				);
				html.addEventListener(Events.ON_AFTER_SIZE, new EventListener<AfterSizeEvent>() {
					@Override
					public void onEvent(AfterSizeEvent event) throws Exception {
						UniLog.log1("event:%s, width:%d, height:%d, dlgHeight:%s", event, event.getWidth(), event.getHeight(), dlg.getHeight());
						Integer oldHtmlHeight = (Integer) html.getAttribute("htmlHeight");
						int newHtmlHeight = event.getHeight();
						if (oldHtmlHeight == null || newHtmlHeight != oldHtmlHeight) {
							String newDlgHeight = (newHtmlHeight + 32 + 43 + 21) + "px";//32: titlebar, 43: bottombar
							UniLog.log1("newDlgHeight:%s", newDlgHeight);
							dlg.setHeight(newDlgHeight);
							html.setAttribute("htmlHeight", newHtmlHeight);
						}
					}
				});
				dlg.setWidth("950px");
				dlg.setStyle("max-width:100%;max-height:" + (sessionHelper.isMobile() ? "100" : "95") + "%");
				dlg.doHighlighted();
			}
		});
	}

	@Override
	protected ReturnMsg setAdditionalQueryCondition(BiResult result) {
		Integer courseRg = s2Course.getSelectedItem().getValue();
		String courseEnd = s2CourseEnd.getSelectedItem().getValue();
		String courseType = s2CourseType.getSelectedItem().getValue();
		Integer tutorRg = s2Tutor.getSelectedItem().getValue();
		Integer studentRg = s2Student.getSelectedItem().getValue();
		String remainingSession = s2RemainingSession.getSelectedItem().getValue();
		String remainingSessionBySessNo = s2RemainingSessionBySessNo.getSelectedItem().getValue();
		String allowPaymentReminder = s2AllowPaymentReminder.getSelectedItem().getValue();
		String reminderDate = s2LastReminderDate.getSelectedItem().getValue();
		String sessionOverdue = s2SessionOverdue.getSelectedItem().getValue();
		String tenRemainBal = s2TenRemainBal.getSelectedItem().getValue();
		result.addCustomCondition("eaav0_status = 'Normal'");
		result.addCustomCondition("essd_status <> 'Cancelled'");
		if (courseRg != null)
			result.addCustomCondition(String.format("essbsd_avrg = %d", courseRg));
		if (StringUtils.equals(courseEnd, ">=Today or Blank"))
			result.addCustomCondition(String.format("eaav0_enddate >= '%s' or eaav0_enddate = ''", DateUtil.dateToDateTimeStr(DateUtil.today(), "yyyy/MM/dd")));
		if (courseType != null)
			result.addCustomCondition(String.format("eaav0_coursetype = '%s'", courseType));
		if (tutorRg != null)
			result.addCustomCondition(String.format("eaav0_esttrg = %d", tutorRg));
		if (studentRg != null)
			result.addCustomCondition(String.format("essbsd_sdrg = %d", studentRg));
		if (StringUtils.equals(remainingSession, "<=Course Threshold"))
			result.addCustomCondition("essbsd_sessremain <= eaav0_alertth");
		else if (StringUtils.equals(remainingSession, "<=0"))
			result.addCustomCondition("essbsd_sessremain <= 0");
		else if (StringUtils.equals(remainingSession, "<=1"))
			result.addCustomCondition("essbsd_sessremain <= 1");
		else if (StringUtils.equals(remainingSession, "<=2"))
			result.addCustomCondition("essbsd_sessremain <= 2");
		else if (StringUtils.equals(remainingSession, "<=3"))
			result.addCustomCondition("essbsd_sessremain <= 3");
		else if (StringUtils.equals(remainingSession, "<=4"))
			result.addCustomCondition("essbsd_sessremain <= 4");
		else if (StringUtils.equals(remainingSession, "<=5"))
			result.addCustomCondition("essbsd_sessremain <= 5");
		if (StringUtils.equals(remainingSessionBySessNo, "<=Course Threshold"))
			result.addCustomCondition("essbsd_sessrebysn <= eaav0_alertth");
		else if (StringUtils.equals(remainingSessionBySessNo, "<=0"))
			result.addCustomCondition("essbsd_sessrebysn <= 0");
		else if (StringUtils.equals(remainingSessionBySessNo, "<=1"))
			result.addCustomCondition("essbsd_sessrebysn <= 1");
		else if (StringUtils.equals(remainingSessionBySessNo, "<=2"))
			result.addCustomCondition("essbsd_sessrebysn <= 2");
		else if (StringUtils.equals(remainingSessionBySessNo, "<=3"))
			result.addCustomCondition("essbsd_sessrebysn <= 3");
		else if (StringUtils.equals(remainingSessionBySessNo, "<=4"))
			result.addCustomCondition("essbsd_sessrebysn <= 4");
		else if (StringUtils.equals(remainingSessionBySessNo, "<=5"))
			result.addCustomCondition("essbsd_sessrebysn <= 5");
		if (allowPaymentReminder != null)
			result.addCustomCondition(String.format("essbsd_allowrem = '%s'", allowPaymentReminder));

		if (StringUtils.equals(reminderDate, "<Today"))
			result.addCustomCondition(String.format("essbsd_lastremdat < '%s'", ddf.format(DateUtil.today())));
		else if (StringUtils.equals(reminderDate, "<Today-1"))
			result.addCustomCondition(String.format("essbsd_lastremdat < '%s'", ddf.format(DateUtil.prevday(DateUtil.today(), 1))));
		else if (StringUtils.equals(reminderDate, "<Today-2"))
			result.addCustomCondition(String.format("essbsd_lastremdat < '%s'", ddf.format(DateUtil.prevday(DateUtil.today(), 2))));
		else if (StringUtils.equals(reminderDate, "<Today-3"))
			result.addCustomCondition(String.format("essbsd_lastremdat < '%s'", ddf.format(DateUtil.prevday(DateUtil.today(), 3))));
		else if (StringUtils.equals(reminderDate, "<Today-4"))
			result.addCustomCondition(String.format("essbsd_lastremdat < '%s'", ddf.format(DateUtil.prevday(DateUtil.today(), 4))));
		else if (StringUtils.equals(reminderDate, "<Today-5"))
			result.addCustomCondition(String.format("essbsd_lastremdat < '%s'", ddf.format(DateUtil.prevday(DateUtil.today(), 5))));
		else if (StringUtils.equals(reminderDate, "<Today-6"))
			result.addCustomCondition(String.format("essbsd_lastremdat < '%s'", ddf.format(DateUtil.prevday(DateUtil.today(), 6))));
		else if (StringUtils.equals(reminderDate, "<Today-7"))
			result.addCustomCondition(String.format("essbsd_lastremdat < '%s'", ddf.format(DateUtil.prevday(DateUtil.today(), 7))));

		if (StringUtils.equals(sessionOverdue, ">=1"))
			result.addCustomCondition("essbsd_sessoverdue >= 1");
		else if (StringUtils.equals(sessionOverdue, ">=2"))
			result.addCustomCondition("essbsd_sessoverdue >= 2");
		else if (StringUtils.equals(sessionOverdue, ">=3"))
			result.addCustomCondition("essbsd_sessoverdue >= 3");
		else if (StringUtils.equals(sessionOverdue, ">=4"))
			result.addCustomCondition("essbsd_sessoverdue >= 4");
		else if (StringUtils.equals(sessionOverdue, ">=5"))
			result.addCustomCondition("essbsd_sessoverdue >= 5");
		
		if (StringUtils.equals(tenRemainBal, ">0"))
			result.addCustomCondition("essbsd_tenremainba > 0");
		else if (StringUtils.equals(tenRemainBal, "=0"))
			result.addCustomCondition("essbsd_tenremainba = 0");
		else if (StringUtils.equals(tenRemainBal, "<0"))
			result.addCustomCondition("essbsd_tenremainba < 0");
		return ReturnMsg.defaultOk;
	}
	
	private void loadPickCourseDatas() {
		BiResult biResult = null;
		try {
			biResult = BiResultHelper.create(sessionHelper, "edu.Course", null, "eaav0_status = 'Normal'", null, -1, new ArrayList(Arrays.asList(Pair.of("eaav0_name", false))), false);
			while (biResult.next()) {
				final int rg = biResult.getCellInt("eaav0_rg");
				final String code = String.format("%s (%s)", biResult.getCellString("eaav0_name"), biResult.getCellString("eaav0_code"));
				s2Course.appendChild(new Listitem(code) {{setValue(rg);}});
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
			UniLog.log("error:" + e.getMessage());
		}
		finally {
			if (biResult != null)
				biResult.close();
		}
	}

	private void loadPickTutorDatas() {
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
			//e.printStackTrace();
			UniLog.log("error:" + e.getMessage());
		}
		finally {
			if (biResult != null)
				biResult.close();
		}
	}

	private void loadPickStudentDatas() {
		BiResult biResult = null;
		try {
			biResult = BiResultHelper.create(sessionHelper, "edu.Student", null, "essd_status <> 'Cancelled'",null, -1, new ArrayList(Arrays.asList(Pair.of("essd_name", false))), false);
			while (biResult.next()) {
				final int rg = biResult.getCellInt("essd_rg");
				final String code = String.format("%s (%s)", biResult.getCellString("essd_name"), biResult.getCellString("essd_sdno"));
				s2Student.appendChild(new Listitem(code) {{setValue(rg);}});
			}
		}
		catch (Exception e) {
			//e.printStackTrace();
			UniLog.log("error:" + e.getMessage());
		}
		finally {
			if (biResult != null)
				biResult.close();
		}
	}

	private String loadAttendanceHistoryV2(int studentRg, String tokenCcy) throws Exception {
		UniLog.log1("studentRg:%d, tokenCcy:%s", studentRg, tokenCcy);
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		BiResult brCourse = null;
		BiResult brAttend = null;
		try {
			brCourse = BiResultHelper.create(sessionHelper, "edu.StudentCourse", null, String.format("essbsd_sdrg = %d and eaav0_tokenccy = '%s'", studentRg, tokenCcy),null, -1, null, false);
			//query student attendance, view edu.StudentAttendance order by eaav0_name asc, essn_date desc, essn_sttime desc
			brAttend = BiResultHelper.create(sessionHelper, "edu.StudentAttendance", null, String.format("esatsd_atrg = %d and eaav0_tokenccy = '%s' and essn_type = 0", studentRg, tokenCcy),null, -1, null, false);

			Map<Integer, Integer> sMap = new HashMap<Integer, Integer>();
			for (int i = brAttend.getRowCount() - 1; i >= 0; i--) {
				brAttend.fetch(true, i);
				int courseRg = brAttend.getCellInt("essn_avrg");
				String courseCode = brAttend.getCellString("eaav0_code");
				Date attDate = brAttend.getCellDate("essn_date");
				String attStatus = brAttend.getCellString("esatsd_status");

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
					int courseNumSession = 0;
					Integer n = 0;
					courseNumSession = brAttend.getCellInt("eaav0_numsession");
					if (courseNumSession > 0) {
						n = sMap.get(courseRg);
						if (n == null)
							n = sessionNoOffset;
						if (++n > courseNumSession)
							n = 1;
						sMap.put(courseRg, n);
					}

					Map<String, Object> m = new HashMap<String, Object>();
					m.put("courseRg", courseRg);
					m.put("courseCode", courseCode);
					m.put("date", attDate);
					m.put("startTime", brAttend.getCellDate("essn_sttime"));
					m.put("endTime", brAttend.getCellDate("essn_endtime"));
					m.put("startDateTime", Student.unionDateTime(attDate, (Date)m.get("startTime")));
					m.put("attStatus", attStatus);
					m.put("attShortStatus", attStatus.substring(0, 1));
					m.put("sessionNo", String.format("%02d/%02d", n, courseNumSession));
					m.put("sessionNoIdx", n);
					m.put("courseNumSession", courseNumSession);
					list.add(0, m);
				}
			}
			list.sort(new Comparator<Map<String, Object>>(){
				@Override
				public int compare(Map<String, Object> m1, Map<String, Object> m2) {
					Date startDateTime1 = (Date)m1.get("startDateTime");
					Date startDateTime2 = (Date)m2.get("startDateTime");
					String courseCode1 = (String)m1.get("courseCode");
					String courseCode2 = (String)m2.get("courseCode");
					int sessionNoIdx1 = (Integer)m1.get("sessionNoIdx");
					int sessionNoIdx2 = (Integer)m2.get("sessionNoIdx");
					int d;
					if ((d = startDateTime2.compareTo(startDateTime1)) != 0)
						return d;
					else if ((d = courseCode1.compareTo(courseCode2)) != 0)
						return d;
					else 
						return sessionNoIdx2 - sessionNoIdx1;
				}
			});
			//list out last 10 attendance entry based using the same token type.
			for (int i = list.size() - 1; i >= 0 && list.size() > 10; i--)
				list.remove(i);
		}
		catch (Exception e) {
			throw new Exception(e);
		}
		finally {
			if (brCourse != null)
				brCourse.close();
			if (brAttend != null)
				brAttend.close();
		}
		StringBuilder sb = new StringBuilder();
		for (Map<String, Object> m : list) {
			sb.append(String.format("%s%s%s%010d%03d%03d%s\n", 
					eddf.format(m.get("date")), etdf.format(m.get("startTime")), etdf.format(m.get("endTime")), 
					(Integer)m.get("courseRg"),
					(Integer)m.get("sessionNoIdx"), (Integer)m.get("courseNumSession"), (String)m.get("attShortStatus")));
		}
		return sb.toString();
	}
	
	private String loadAttendanceHistory(int studentRg, int courseRg, int sessionNoOffset, int courseNumSession) throws Exception {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		BiResult brAttend = null;
		BiResult brSession = null;
		try {
			//query student attendance, view edu.StudentAttendance order by eaav0_name asc, essn_date desc, essn_sttime desc
			brAttend = BiResultHelper.create(sessionHelper, "edu.StudentAttendance", null, String.format("esatsd_atrg = %d and essn_avrg = %d", studentRg, courseRg),null, -1, null, false);
			int n = sessionNoOffset;
			if (n < 0)
				n = 0;
			Date lastAttDate = null;
			for (int i = brAttend.getRowCount() - 1; i >= 0; i--) {
				brAttend.fetch(true, i);
				String attStatus = brAttend.getCellString("esatsd_status");
				lastAttDate = brAttend.getCellDate("essn_date");
				//only show present / absent session
				if (StringUtils.equalsAny(attStatus, "Present", "Absent")) {
					if (courseNumSession > 0) {
						if (++n > courseNumSession)
							n = 1;
					}
					Map<String, Object> m = new HashMap<String, Object>();
					m.put("date", lastAttDate);
					m.put("startTime", brAttend.getCellDate("essn_sttime"));
					m.put("endTime", brAttend.getCellDate("essn_endtime"));
					m.put("attStatus", attStatus);
					m.put("attShortStatus", attStatus.substring(0, 1));
					m.put("sessionNo", String.format("%02d/%02d", n, courseNumSession));
					m.put("sessionNoIdx", n);
					m.put("courseNumSession", courseNumSession);
					list.add(0, m);
				}
			}
			int lastAttendanceIdx = n; 
			UniLog.log1("lastAttendanceIdx:%d, courseNumSession:%d, %f", lastAttendanceIdx, courseNumSession, (double)lastAttendanceIdx / courseNumSession);

			//query TBA(To be attended) session
			if (courseNumSession > 0 && n < courseNumSession && lastAttDate != null) {
				Date date = lastAttDate;
				//To be attended need to skip expired class, if session date < today, should not include in TBA
				if (date.compareTo(DateUtil.prevday(DateUtil.today())) < 0)
					date = DateUtil.prevday(DateUtil.today());
				brSession = BiResultHelper.create(sessionHelper, "edu.CourseSessionDet", null, String.format("essncs_avrg = %d and essncs_date > '%s'", courseRg, sdf.format(date)), null, -1, null, false);
				for (int i = 0; i < brSession.getRowCount(); i++) {
					brSession.fetch(true, i);
					if (++n > courseNumSession)
						break;
					Map<String, Object> m = new HashMap<String, Object>();
					m.put("date", brSession.getCellDate("essncs_date"));
					m.put("startTime", brSession.getCellDate("essncs_sttime"));
					m.put("endTime", brSession.getCellDate("essncs_endtime"));
					m.put("attStatus", TO_BE_ATTENDED);
					m.put("attShortStatus", "T");
					m.put("sessionNoIdx", n);
					m.put("courseNumSession", courseNumSession);
					m.put("sessionNo", String.format("%02d/%02d", n, courseNumSession));
					list.add(0, m);
				}
			}

			//list show from first session(1/X) to last session(X/X) 
			if (courseNumSession > 0 && !list.isEmpty()) {
				int endIdx = 0;
				boolean foundFirst = false;
				for (int i = 0; i < list.size(); i++) {
					endIdx = i;
					Map<String, Object> m = list.get(i);
					int sessionNoIdx = (Integer)m.get("sessionNoIdx");
					if (sessionNoIdx == 1) {
						if (!foundFirst) {
							foundFirst = true;
							//If lastAttendanceIdx / noOfSession < 0.5, try to show previous subscription range
							if ((double)lastAttendanceIdx / courseNumSession >= 0.5)
								break;
						}
						else
							break;
					}
				}
				for (int i = list.size() - 1; i > endIdx; i--)
					list.remove(i);
			}
		}
		catch (Exception e) {
			throw new Exception(e);
		}
		finally {
			if (brAttend != null)
				brAttend.close();
			if (brSession != null)
				brSession.close();
		}
		StringBuilder sb = new StringBuilder();
		for (Map<String, Object> m : list) {
			/*sb.append(String.format("%s %s - %s\t%s\t%s\n", 
					ddf.format(m.get("date")), tdf.format(m.get("startTime")), tdf.format(m.get("endTime")), 
					(String)m.get("sessionNo"), (String)m.get("attStatus")));*/
			sb.append(String.format("%s%s%s%03d%03d%s\n", 
					eddf.format(m.get("date")), etdf.format(m.get("startTime")), etdf.format(m.get("endTime")), 
					(Integer)m.get("sessionNoIdx"), (Integer)m.get("courseNumSession"), (String)m.get("attShortStatus")));
		}
		return sb.toString();
	}

	private Pair<List<Map<String, Object>>, Integer> getBatchList(BiResult result, boolean needAdjustValid) throws Exception {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Set selection = listModelList.getSelection();
		int emailCount = 0;
       	for (Iterator it = selection.iterator(); it.hasNext();) {
       		Object o = it.next();
         	Object ts = o;
          	if (ts instanceof TrStatFilter)
            	ts = ((TrStatFilter)ts).getTrStatIdx();
       		CellCollection cc = result.getRowCollectionO(ts);
       		int studentRg = cc.getCellInt("essbsd_sdrg");
       		int courseRg = cc.getCellInt("essbsd_avrg");
       		String studentCode = cc.getCellString("essd_sdno");
       		String studentName = cc.getCellString("essd_name");
       		String studentFirstName = cc.getCellString("essd_firstname");
       		String studentContactPerson = cc.getCellString("essd_contactby");
       		String courseCode = cc.getCellString("eaav0_code");
       		String courseName = cc.getCellString("eaav0_name");
       		String tokenCcy = cc.getCellString("eaav0_tokenccy");
       		Date subStartDate = cc.getCell("essbsd_startdate").getDate();
       		Date subEndDate = cc.getCell("essbsd_enddate").getDate();
       		int sessionRemain = cc.getCellInt("essbsd_sessremain");
       		String studentEmail = cc.getCellString("essd_sdemail");
       		String tutorName = cc.getCellString("estt_name");
       		String courseSessionDays = cc.getCellString("eaav0_sessionday");
       		Date courseSessionTime = cc.getCell("eaav0_sessiontime").getDate();
       		int courseSessionLen = cc.getCellInt("eaav0_sessionlen");
       		int courseNumSession = cc.getCellInt("eaav0_numsession");
       		double courseFee = cc.getCellDouble("eaav0_coursefee");
       		boolean allowPayReminder = StringUtils.equals(cc.getCellString("essbsd_allowrem"), "Enable");
       		int sessionOffset = cc.getCellInt("essbsd_sessoffset");
       		
       		double tenRemainBalance = cc.getCellDouble("essbsd_tenremainba");
       		double adjustment = !sessionHelper.isMobile() ? ObjectUtils.defaultIfNull(adjustmentCompMap.get(ts).getValue(), 0.0) : 0;
       		if (needAdjustValid) {
       			String errMsg = validAdjustment(adjustment, tenRemainBalance, courseFee);
       			if (errMsg != null)
       				throw new Exception(String.format("%s (Student Code: %s, Course Code:%s)", errMsg, studentCode, courseCode));
       		}
			double adjustedFee = courseFee + adjustment;
			Date firstSessionDate = !sessionHelper.isMobile() ? firstSessionDateCompMap.get(ts).getValue() : null;

       		UniLog.log1("studentRg:%d, courseRg:%d, studentName:%s, courseCode:%s, studentCode:%s, tutorName:%s, allowPayReminder:%b,%s", 
       				studentRg, courseRg, studentName, courseCode, studentCode, tutorName, allowPayReminder, cc.getCellString("essbsd_allowrem"));
       		
       		Map<String, Object> map = new HashMap<String, Object>();
       		map.put("studentRg", studentRg);
       		map.put("courseRg", courseRg);
       		map.put("studentCode", studentCode);
       		map.put("studentName", studentName);
       		map.put("studentFirstName", studentFirstName);
       		map.put("studentContactPerson", studentContactPerson);
       		map.put("courseCode", courseCode);
       		map.put("courseName", courseName);
       		map.put("tokenCcy", tokenCcy);
       		map.put("tutorName", tutorName);
       		map.put("courseSessionDays", courseSessionDays);
       		map.put("courseSessionTime", courseSessionTime);
       		map.put("courseSessionLen", courseSessionLen);
       		map.put("courseNumSession", courseNumSession);
       		map.put("courseFee", courseFee);
       		map.put("sessionRemain", sessionRemain);
       		map.put("subStartDate", subStartDate);
       		map.put("subEndDate", subEndDate);
       		map.put("allowPayReminder", allowPayReminder);
       		map.put("adjustment", adjustment);
       		map.put("adjustedFee", adjustedFee);
       		map.put("firstSessionDate", firstSessionDate);
       		map.put("sessionOffset", sessionOffset);
       		List<Pair<String, String>> emailList = getStudentEmailAddressList(studentEmail);
       		emailCount += emailList.size();
       		map.put("studentEmailText", studentEmail);
       		map.put("studentEmails", emailList);
       		list.add(map);
       	}
       	return Pair.of(list, emailCount);
	}
	
	private String validAdjustment(double adjustment, double tenRemainBalance, double courseFee) {
		/*
  		if (adjustment != 0) {
   			if (tenRemainBalance + adjustment < 0)
   				return "Adjustment must less than remaining balance.";
   			if (courseFee + adjustment < 0)
      			return "Adjustment cannot greater than subsfee.";
   		}
   		*/
		//andrew220315 adjustment markup, do not need to valdate
  		if (adjustment < 0) {
   			if (tenRemainBalance + adjustment < 0)
   				return "Adjustment cannot greater than remaining balance.";
   			if (courseFee + adjustment < 0)
      			return "Adjustment cannot greater than subscription fee.";
   		}
  		return null;
	}

	public static List<Pair<String, String>> getStudentEmailAddressList(String addr) {
		Set<String> list = new LinkedHashSet<String>();
		for (String s : addr.trim().split(",|;")) {
			if (StringUtils.isNotBlank(s))
				list.add(s.trim());
		}
		List<Pair<String, String>> rList = new ArrayList<Pair<String, String>>();
		for (String s : list) {
			rList.add(Pair.of(s, ""));
			UniLog.log1("getStudentEmailAddressList email:%s", s);
		}
		return rList;
	}
	
	private void sendEmail(Component comp, final List<Map<String, Object>> batchList) {
		final AtomicBoolean threadStopFlag = new AtomicBoolean(false);
		//final AtomicBoolean threadFinished = new AtomicBoolean(false);
		final AtomicInteger okCountRef = new AtomicInteger(0);
		final AtomicInteger failCountRef = new AtomicInteger(0);
		final AtomicInteger skipCountRef = new AtomicInteger(0);
		final AtomicReference<String> studentNameRef = new AtomicReference<String>();
		final AtomicReference<String> studentCodeRef = new AtomicReference<String>();
		final AtomicReference<String> courseCodeRef = new AtomicReference<String>();

		final File logoFile = new File(Sessions.getCurrent().getWebApp().getRealPath("images/logo/edu_logo.jpg"));
		final File logo1File = new File(Sessions.getCurrent().getWebApp().getRealPath("images/logo/edu_logo1.jpg"));
		final File logo2File = new File(Sessions.getCurrent().getWebApp().getRealPath("images/logo/edu_companychop.png"));
		final File linkLogoFile = logoFile;
		final File linkLogo1File = new File(Sessions.getCurrent().getWebApp().getRealPath("images/whatsapp-icon-100.png"));
		final File linkLogo2File = new File(Sessions.getCurrent().getWebApp().getRealPath("images/facebook-icon-100.png"));
		final File linkLogo3File = new File(Sessions.getCurrent().getWebApp().getRealPath("images/instagram-icon-100.png"));
		UniLog.log1("logoFile:%b,%b", logoFile.exists(), logo1File.exists());
		
		final String contentTemplate = loadEmailHtmlContentTemplate(sessionHelper);
		if (contentTemplate == null) {
			ZkUtil.errMsg("Email content template not exist");
			return;
		}

		//build progress dialog
		GridHelper gh = new GridHelper(2);
		gh.getColumn(0).setWidth("50px");
		gh.getColumn(0).setAlign("left");
		gh.getColumn(1).setHflex("1");
					
		final Textbox tbStatus = new Textbox();
		tbStatus.setHflex("1");
		tbStatus.setReadonly(true);
		gh.addRow(new Label("Status"), tbStatus);
		final Progressmeter pm = new Progressmeter(0);
		pm.setHflex("1");
		gh.addRow(new Space(), pm);

		final Map<String, Button> buttonMap = new HashMap<String, Button>();

		final MessageboxDlg dlg = ZkUtil.buildMessageboxDlg("Generating payment reminder email...", gh, 
			new Messagebox.Button[]{Messagebox.Button.CANCEL}, 
			new String[] {sessionHelper.getBtLabel("Cancel")},
			comp.getRoot(),
			new EventListener<Messagebox.ClickEvent>(){
			@Override
			public void onEvent(ClickEvent event) throws Exception {
				if (StringUtils.equalsAny(event.getName(), Events.ON_CANCEL, Events.ON_CLOSE)) {
					((Button)buttonMap.get("Cancel")).setDisabled(true);
					threadStopFlag.set(true);
					event.stopPropagation();
				}
			}
		});
		dlg.setStyle("width:100%;max-width:700px");
		//call send event before update preogress
		dlg.addEventListener("onPreUpdateProgress", new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				org.zkoss.json.JSONObject jobj = (org.zkoss.json.JSONObject)event.getData();
				final int i = (Integer)jobj.get("batchListIndex");
				if (StringUtils.isNotBlank(studentCodeRef.get()))
					tbStatus.setText(String.format("[%03d/%03d] Student:%s(%s) Course:%s", okCountRef.get()+failCountRef.get()+skipCountRef.get(), batchList.size(), studentNameRef.get(), studentCodeRef.get(), courseCodeRef.get()));
				pm.setValue((int)((double)(okCountRef.get() + failCountRef.get() + skipCountRef.get()) / batchList.size() * 100));
				Events.echoEvent("onSendEvent", dlg, new org.zkoss.json.JSONObject() {{ put("batchListIndex", i); put("preUpdateProgress", false); }});
			}
		});
		dlg.addEventListener("onSendEvent", new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				org.zkoss.json.JSONObject jobj = (org.zkoss.json.JSONObject)event.getData();
				final int i = (Integer)jobj.get("batchListIndex");
				final boolean preUpdateProgress = (Boolean)jobj.get("preUpdateProgress");
				if (i == batchList.size() || threadStopFlag.get()) {
					dlg.detach();
					int okCount = okCountRef.get();
					int failCount = failCountRef.get();
					int skipCount = skipCountRef.get();
					int cancelCount = batchList.size() - okCount - failCount - skipCount;
					ZkUtil.msg("Action completed.\nResult " + 
						Student.joinString(",", 
							okCount > 0 ? "Ok:" + okCount : "",
							failCount > 0 ? "Fail:" + failCount : "",
							skipCount > 0 ? "Skip:" + skipCount : "",
							cancelCount > 0 ? "Cancel:" + cancelCount : "")
					);
					return;
				}
				Map<String, Object> map = batchList.get(i);
				
				int studentRg = (Integer)map.get("studentRg");
				int courseRg = (Integer)map.get("courseRg");
				String studentName = (String)map.get("studentName");
				String studentCode = (String)map.get("studentCode");
				String studentFirstName = (String)map.get("studentFirstName");
				String studentContactPerson = (String)map.get("studentContactPerson");
				String courseName = (String)map.get("courseName");
				String courseCode = (String)map.get("courseCode");
				String tokenCcy = (String)map.get("tokenCcy");
				String tutorName = (String)map.get("tutorName");
				int courseNumSession = (Integer)map.get("courseNumSession");
				double courseFee = (Double)map.get("courseFee");
				double adjustment = (Double)map.get("adjustment");
				String courseSessionDays = (String)map.get("courseSessionDays");
				Date courseSessionTime = (Date)map.get("courseSessionTime");
				int courseSessionLen = (Integer)map.get("courseSessionLen");
				int sessionRemain = (Integer)map.get("sessionRemain");
				boolean allowPayReminder = (Boolean)map.get("allowPayReminder");
				int sessionOffset = (Integer)map.get("sessionOffset");
				Date subStartDate = (Date)map.get("subStartDate");
				Date subEndDate = (Date)map.get("subEndDate");
				Date firstSessionDate = (Date)map.get("firstSessionDate");
				Date createTime = DateUtil.now();
				
				map.put("createTime", createTime);
					
				studentNameRef.set(studentName);
				studentCodeRef.set(studentCode);
				courseCodeRef.set(courseCode);
					
	       		UniLog.log1("studentName:%s, courseCode:%s, studentCode:%s, tutorName:%s, allowPayReminder:%b", studentName, courseCode, studentCode, tutorName, allowPayReminder);
				UniLog.log1("courseSessionDays:%s, courseSessionTime:%s, courseSessionLen:%d", courseSessionDays, courseSessionTime, courseSessionLen);

				if (preUpdateProgress) {
					//update progress
					Events.echoEvent("onPreUpdateProgress", dlg, new org.zkoss.json.JSONObject() {{ put("batchListIndex", i); }});
					return;
				}
				
				String attendHistory;
				if (showLast10Entry)
					attendHistory = loadAttendanceHistoryV2(studentRg, tokenCcy);
				else
					attendHistory = loadAttendanceHistory(studentRg, courseRg, sessionOffset, courseNumSession);
				map.put("attendHistory", attendHistory);
					
				List<Pair<String, String>> emailList = (List<Pair<String, String>>)map.get("studentEmails");
		
				//to
				List<Pair<String, String>> toList = emailList;
				if (toList.isEmpty()) {
					skipCountRef.addAndGet(1);
					Events.echoEvent("onSendEvent", dlg, new org.zkoss.json.JSONObject() {{ put("batchListIndex", i + 1); put("preUpdateProgress", true); }});
					return;
				}
				if (!allowPayReminder) {
					//skipCountRef.addAndGet(toList.size());
					skipCountRef.addAndGet(1);
					Events.echoEvent("onSendEvent", dlg, new org.zkoss.json.JSONObject() {{ put("batchListIndex", i + 1); put("preUpdateProgress", true); }});
					return;
				}
				for (Pair<String, String> p : toList)
					UniLog.log1("to email:%s", p.getLeft());
	
				//subject
				//andrew220315: add datetime into subject to avoid gmail put same subject email into conversation group.
				String subject = String.format("Little Scholars Education Centre Invoice [%s] [%s]", courseName, sdf.format(createTime));
	
				//attachment
				List<EmailAttachment> attList = new ArrayList<EmailAttachment>();
	
				String status, result;
				BiResult br = null;
				try {
					br = sessionHelper.newBiResult("edu.PaymentReminderHistory");
					br.beginWork();
					br.clearCurrentRec();

					//get invoice number first
					Value v = br.getView().getSchema().getUniqueRg(null,"", 53009, "espayremhistory", "esprh_rg", null);
					br.getCell("esprh_rg").set(v.toInt());
					int prhrg = br.getCellInt("esprh_rg");
					String prhno = br.getCellString("esprh_invoiceno");
					UniLog.log1("prhrg:%d, prhno:%s", prhrg, prhno);
					if (prhrg <= 0 || StringUtils.isBlank(prhno)) {
						failCountRef.addAndGet(1);
						throw new Exception("invalid prhrg or prhno");
					}

					try {
						HtmlEmail hemailObj = new HtmlEmail();
						//content
						String htmlMsg = buildEmailHtmlContent(sessionHelper, contentTemplate, "cid:" + hemailObj.embed(logoFile), "cid:" + hemailObj.embed(logo1File), "cid:" + hemailObj.embed(logo2File),
											"cid:" + hemailObj.embed(linkLogoFile), "cid:" + hemailObj.embed(linkLogo1File), "cid:" + hemailObj.embed(linkLogo2File), "cid:" + hemailObj.embed(linkLogo3File),
											prhno, createTime, tokenCcy, courseName, courseCode, tutorName, 
											courseSessionDays, courseSessionTime, courseSessionLen, studentRg, studentName, studentCode,
											sessionRemain, firstSessionDate, subEndDate,
											studentContactPerson, studentFirstName, courseNumSession, courseFee, adjustment, attendHistory);
						UniLog.log1("htmlMsg:%s", htmlMsg);
		
						ReturnMsg rtnMsg = ZkUtil.sendEmail(hemailObj, null, toList, null, null, subject, htmlMsg, "", attList, sessionHelper);
						UniLog.log1("rtnMsg:" + rtnMsg);
						if (rtnMsg.getStatus()) {
							Map<String, Object> rMap = (Map<String, Object>) rtnMsg.getData();
							int okCnt = (Integer)rMap.get("okCnt");
							int failCnt = (Integer)rMap.get("failCnt");
							//okCountRef.addAndGet(okCnt);
							//failCountRef.addAndGet(failCnt);
							if (failCnt > 0) {
								failCountRef.addAndGet(1);
								status = "FAIL";
								result = "FAILfail email count:" + failCnt;
							}
							else {
								okCountRef.addAndGet(1);
								status = "OK";
								result = "OK";
							}
						}
						else {
							//failCountRef.addAndGet(toList.size());
							failCountRef.addAndGet(1);
							status = "FAIL";
							result = "FAIL" + rtnMsg.getMsg();
						}
					}
					catch (Exception e) {
						//e.printStackTrace();
						UniLog.log("error:" + e.getMessage());
						//failCountRef.addAndGet(toList.size());
						failCountRef.addAndGet(1);
						status = "FAIL";
						result = "FAIL" + e.getMessage();
					}

					br.getCell("esprh_avrg").set((Integer)map.get("courseRg"));
					br.getCell("esprh_sdrg").set((Integer)map.get("studentRg"));
					br.getCell("esprh_sremcnt").set((Integer)map.get("sessionRemain"));
					br.getCell("esprh_adjust").set((Double)map.get("adjustment"));
					br.getCell("esprh_startdate").set((Date)map.get("firstSessionDate"));
					br.getCell("esprh_finsdate").set((Date)map.get("subEndDate"));
					br.getCell("esprh_toemail").set((String)map.get("studentEmailText"));
					br.getCell("esprh_remuser").set(sessionHelper.getLoginId());
					br.getCell("esprh_cuser").set(sessionHelper.getLoginId());
					br.getCell("esprh_remtime").set((Date)map.get("createTime"));
					br.getCell("esprh_attendhis").set((String)map.get("attendHistory"));
					br.getCell("esprh_ctime").set((Date)map.get("createTime"));
					br.getCell("esprh_status").set(status);
					br.getCell("esprh_result").set(result);
		
					ReturnMsg rtn = br.addCurrent();
					if (!rtn.getStatus())
						throw new Exception(rtn.getMsg());
					
					br.getSelectUtil().executeUpdate("update essubscribe set essb_lastremdat = ? where essb_avrg = ? and essb_type = 0 and essb_sdrg = ?", 
							new Wherecl()
								.appendArgument(((Date)map.get("createTime")).getTime() / 1000)
								.appendArgument((Integer)map.get("courseRg"))
								.appendArgument((Integer)map.get("studentRg")));
					
					br.commitWork();
				}
				catch (Exception e) {
					//e.printStackTrace();
					UniLog.log("error:" + e.getMessage());
					if (br != null)
						br.rollbackWork();
				}
				finally {
					if (br != null)
						br.close();
				}

				//savePaymentReminderHistory(map, status, result);

				if (StringUtils.isNotBlank(studentCodeRef.get()))
					tbStatus.setText(String.format("[%03d/%03d] Student:%s(%s) Course:%s", okCountRef.get()+failCountRef.get()+skipCountRef.get(), batchList.size(), studentNameRef.get(), studentCodeRef.get(), courseCodeRef.get()));
				UniLog.log1("i:%d, okCountRef:%d, failCountRef:%d, skipCountRef:%d, batchList size:%d", i, okCountRef.get(), failCountRef.get(), skipCountRef.get(), batchList.size());
				pm.setValue((int)((double)(okCountRef.get() + failCountRef.get() + skipCountRef.get()) / batchList.size() * 100));
				
				Events.echoEvent("onSendEvent", dlg, new org.zkoss.json.JSONObject() {{ put("batchListIndex", i + 1); put("preUpdateProgress", true); }});
			}
		});
		dlg.doHighlighted();

		//find buttons
		for (Component cbtn : dlg.queryAll("Button")) {
			Button btn = (Button)cbtn;
			if (StringUtils.equals(btn.getLabel(), sessionHelper.getBtLabel("Cancel")))
				buttonMap.put("Cancel", btn);
		}
		
		Events.echoEvent("onSendEvent", dlg, new org.zkoss.json.JSONObject() {{
			//batchListIndex: batchList index
			//preUpdateProgress: need call update progress event
			put("batchListIndex", 0);
			put("preUpdateProgress", true);
		}});
	}
	
	/*private void savePaymentReminderHistory(Map<String, Object> map, String status, String result) {
		BiResult br = null;
		try {
			br = sessionHelper.newBiResult("edu.PaymentReminderHistory");
			br.beginWork();
			br.clearCurrentRec();

			Value v = br.getView().getSchema().getUniqueRg(null,"", 53009, "espayremhistory", "esprh_rg", null);
			br.getCell("esprh_rg").set(v.toInt());
			br.getCell("esprh_avrg").set((Integer)map.get("courseRg"));
			br.getCell("esprh_sdrg").set((Integer)map.get("studentRg"));
			br.getCell("esprh_sremcnt").set((Integer)map.get("sessionRemain"));
			br.getCell("esprh_adjust").set((Double)map.get("adjustment"));
			br.getCell("esprh_finsdate").set((Date)map.get("subEndDate"));
			br.getCell("esprh_toemail").set((String)map.get("studentEmailText"));
			br.getCell("esprh_remuser").set(sessionHelper.getLoginId());
			br.getCell("esprh_cuser").set(sessionHelper.getLoginId());
			br.getCell("esprh_remtime").set((Date)map.get("createTime"));
			br.getCell("esprh_attendhis").set((String)map.get("attendHistory"));
			br.getCell("esprh_ctime").set((Date)map.get("createTime"));
			br.getCell("esprh_status").set(status);
			br.getCell("esprh_result").set(result);

			ReturnMsg rtn = br.addCurrent();
			if (!rtn.getStatus())
				throw new Exception(rtn.getMsg());
			
			br.getSelectUtil().executeUpdate("update essubscribe set essb_lastremdat = ? where essb_avrg = ? and essb_type = 0 and essb_sdrg = ?", 
					new Wherecl()
						.appendArgument(((Date)map.get("createTime")).getTime() / 1000)
						.appendArgument((Integer)map.get("courseRg"))
						.appendArgument((Integer)map.get("studentRg")));
			
			br.commitWork();
		}
		catch (Exception e) {
			e.printStackTrace();
			if (br != null)
				br.rollbackWork();
		}
		finally {
			if (br != null)
				br.close();
		}
	}*/
	
	public static String loadEmailHtmlContentTemplate(SessionHelper p_sh) {
		InputStream inStream = null;
		String s = null;
		try {
			//inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("template_paymentreminder_edu.html");
			inStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(p_sh.getAllowDemo() ? "template_paymentreminder_edudemo.html" : "template_paymentreminder_edu.html");
			
			s = IOUtils.toString(inStream, "UTF-8");
		}
		catch (Exception e) {
			//e.printStackTrace();
			UniLog.log("error:" + e.getMessage());
		}
		finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					//e.printStackTrace();
					UniLog.log("error:" + e.getMessage());
				}
			}
		}
		return s;
	}

	public static String buildEmailHtmlContent(SessionHelper sessionHelper, String contentTemplate, String logoSrc, String logo1Src, String logo2Src, 
											String linkLogoSrc, String linkLogoSrc1, String linkLogoSrc2, String linkLogoSrc3,
											String invoiceNum, Date createTime, 
											String tokenCcy, String courseName, String courseCode, String tutorName, 
											String courseSessionDays, Date courseSessionTime, int courseSessionLen, int studentRg, String studentName, String studentCode,
											int sessionRemain, Date firstSessionDate, Date subEndDate, String studentContactPerson, String studentFirstName, 
											int courseNumSession, double courseFee, 
											double adjustment, String attendHistory) {
		StringBuilder sbCourseSessionDays = new StringBuilder();
		if (StringUtils.isNotBlank(courseSessionDays)) {
			for (String s : courseSessionDays.split(",")) {
				String ws;
				switch(NumberUtils.toInt(s)) {
				case 0: 
					ws = "Sun";
					break;
				case 1: 
					ws = "Mon";
					break;
				case 2: 
					ws = "Tue";
					break;
				case 3: 
					ws = "Wed";
					break;
				case 4: 
					ws = "Thu";
					break;
				case 5: 
					ws = "Fri";
					break;
				case 6: 
					ws = "Sat";
					break;
				default: 
					ws = "";
					break;
				}
				if (sbCourseSessionDays.length() > 0)
					sbCourseSessionDays.append(",");
				sbCourseSessionDays.append(ws);
			}
		}
		Date courseSessionEndTime = null;
		if (!DateUtil.isDateNull(courseSessionTime))
			courseSessionEndTime = new Date(courseSessionTime.getTime() + courseSessionLen * 60 * 1000);
		double adjustedFee = courseFee + adjustment;
		
		Map<String, String> map = new HashMap<String, String>();
		map.put("logo1", logoSrc);
		map.put("logo2", logo1Src);
		map.put("logo3", logo2Src);
		map.put("link_logo1", linkLogoSrc);
		map.put("link_logo2", linkLogoSrc1);
		map.put("link_logo3", linkLogoSrc2);
		map.put("link_logo4", linkLogoSrc3);
		map.put("title", "LITTLE SCHOLARS EDUCATION CENTRE");
		map.put("subtitle", "INVOICE");
		map.put("school_registration_number", "616524");
		map.put("registered_address", "101 & 102, 1/F, HOLLYWOOD CENTRE, 233 HOLLYWOOD ROAD, SHEUNG WAN, HONG KONG");
		map.put("telephone_number", "(852) 2537 9519");
		map.put("invoice_number", invoiceNum);
		map.put("date", sdf.format(createTime));
		map.put("course_name", StringUtils.defaultIfBlank(courseName, "N/A"));
		map.put("days", StringUtils.defaultIfBlank(sbCourseSessionDays.toString(), "N/A"));
		map.put("time", !DateUtil.isDateNull(courseSessionTime) ? (tdf.format(courseSessionTime) + " - " + tdf.format(courseSessionEndTime)) : "N/A");
		map.put("first_session_date", !DateUtil.isDateNull(firstSessionDate) ? ddf.format(firstSessionDate) : "");
		map.put("name_of_student", String.format("%s (%s)", studentName, studentCode));
		map.put("session_remain", "" + sessionRemain);
		map.put("student_contact_person", StringUtils.defaultIfBlank(studentContactPerson, "Parents"));
		map.put("student_first_name", StringUtils.defaultIfBlank(studentFirstName, "N/A"));
		map.put("course_no_of_session", "" + courseNumSession);
		map.put("subscription_fee", String.format("$%s for %d Sessions", feeDecFmt.format(courseFee), courseNumSession));
		map.put("adjusted_course_fee_sessions", "$" + feeDecFmt.format(adjustedFee));
		
		if (StringUtils.isNotBlank(invoiceNum))
			map.put("invoice_number_display", "block");
		else
			map.put("invoice_number_display", "none");

		//Adjustment
		if (adjustment != 0) {
			map.put("adjustment_display", "table-row");
			map.put("adjusted_fee_display", "table-row");
			map.put("adjustment", feeDecFmt.format(adjustment));
			map.put("adjusted_fee", "$" + feeDecFmt.format(adjustedFee));
		}
		else {
			map.put("adjustment_display", "none");
			map.put("adjusted_fee_display", "none");
		}
		
		if (!DateUtil.isDateNull(firstSessionDate))
			map.put("first_session_date_display", "block");
		else
			map.put("first_session_date_display", "none");

		//Student Attendance History
		boolean isV2Header = false;
		StringBuilder sbAttendHistoryHeader = new StringBuilder();
		StringBuilder sbAttendHistory = new StringBuilder();
		String[] ahss = StringUtils.split(attendHistory, "\n");
		if (ahss != null && ahss.length > 0) {
			BiResult brAttend = null;
			for (String ahs : ahss) {
				String ahs1 = ahs.trim();
				UniLog.log1("ahs1:%s", ahs1);
				if (ahs1.length() == 31) {
					BiResult brCourse = null;
					try {
						Date date = eddf.parse(ahs1.substring(0, 6));
						Date startTime = etdf.parse(ahs1.substring(6, 10));
						Date endTime = etdf.parse(ahs1.substring(10, 14));
						/*int courseRg = NumberUtils.toInt(ahs1.substring(14, 24));
						int sessionNoIdx = NumberUtils.toInt(ahs1.substring(24, 27));
						int courseNSession = NumberUtils.toInt(ahs1.substring(27, 30));
						String status = ahs1.substring(30, 31);
						if (StringUtils.equals(status, "P"))
							status = "Present";
						else if (StringUtils.equals(status, "A"))
							status = "Absent";
						else if (StringUtils.equals(status, "T"))
							status = TO_BE_ATTENDED;
						String courseCod = null;
						if (courseRg > 0) {
							if (brAttend == null)
								brAttend = BiResultHelper.create(sessionHelper, "edu.StudentAttendance", null, String.format("esatsd_atrg = %d and eaav0_tokenccy = '%s' and essn_type = 0", studentRg, tokenCcy),null, -1, null, false);
							for (int i = 0; i < brAttend.getRowCount(); i++) {
								brAttend.fetch(true, i);
								if (courseRg == brAttend.getCellInt("essn_avrg")) {
									courseCod = brAttend.getCellString("eaav0_code");
									break;
								}
							}
							if (courseCod == null) {
								brCourse = BiResultHelper.create(sessionHelper, "edu.Course", null, String.format("eaav0_rg = %d", courseRg),null, -1, null, false);
								if (brCourse.next())
									courseCod = brCourse.getCellString("eaav0_code");
							}
						}
						if (courseCod == null)
							courseCod = "";*/
						String hsDate = String.format("%s %s - %s", ddf.format(date), tdf.format(startTime), tdf.format(endTime));
						/*String hsCourseCode = courseCod;
						String hsSessionNo = String.format("%02d/%02d", sessionNoIdx, courseNSession);
						String hsStatus = status;*/
						sbAttendHistory.append("<div style='display:table-row'>\n");
						sbAttendHistory.append("<div style='display:table-cell'>"+hsDate+"</div>\n");
						/*sbAttendHistory.append("<div style='display:table-cell;padding-left:10px'>"+hsCourseCode+"</div>\n");
						sbAttendHistory.append("<div style='display:table-cell;padding-left:10px'>"+hsSessionNo+"</div>\n");
						sbAttendHistory.append("<div style='display:table-cell;padding-left:10px'>"+hsStatus+"</div>\n");*/
						sbAttendHistory.append("</div>\n");
					}
					catch (Exception e) {
						UniLog.log("error:" + e.getMessage());
					}
					finally {
						if (brCourse != null)
							brCourse.close();
					}
					isV2Header = true;
				}
				else if (ahs1.length() == 21) {
					try {
						Date date = eddf.parse(ahs1.substring(0, 6));
						Date startTime = etdf.parse(ahs1.substring(6, 10));
						Date endTime = etdf.parse(ahs1.substring(10, 14));
						int sessionNoIdx = NumberUtils.toInt(ahs1.substring(14, 17));
						int courseNSession = NumberUtils.toInt(ahs1.substring(17, 20));
						String status = ahs1.substring(20, 21);
						if (StringUtils.equals(status, "P"))
							status = "Present";
						else if (StringUtils.equals(status, "A"))
							status = "Absent";
						else if (StringUtils.equals(status, "T"))
							status = TO_BE_ATTENDED;
						String hsDate = String.format("%s %s - %s", ddf.format(date), tdf.format(startTime), tdf.format(endTime));
						String hsSessionNo = String.format("%02d/%02d", sessionNoIdx, courseNSession);
						String hsStatus = status;
						sbAttendHistory.append("<div style='display:table-row'>\n");
						sbAttendHistory.append("<div style='display:table-cell'>"+hsDate+"</div>\n");
						/*sbAttendHistory.append("<div style='display:table-cell;padding-left:10px'>"+hsSessionNo+"</div>\n");
						sbAttendHistory.append("<div style='display:table-cell;padding-left:10px'>"+hsStatus+"</div>\n");*/
						sbAttendHistory.append("</div>\n");
					}
					catch (Exception e) {
						//e.printStackTrace();
						UniLog.log("error:" + e.getMessage());
					}
				}
				else {
					String[] hs = ahs1.split("\t", -1);
					if (hs != null && hs.length >= 3) {
						String hsDate = hs[0];
						String hsSessionNo = hs[1];
						String hsStatus = hs[2];
						sbAttendHistory.append("<div style='display:table-row'>");
						sbAttendHistory.append("<div style='display:table-cell'>"+hsDate+"</div>");
						/*sbAttendHistory.append("<div style='display:table-cell;padding-left:10px'>"+hsSessionNo+"</div>");
						sbAttendHistory.append("<div style='display:table-cell;padding-left:10px'>"+hsStatus+"</div>");*/
						sbAttendHistory.append("</div>");
					}
				}
			}
			if (brAttend != null)
				brAttend.close();
		}
		sbAttendHistoryHeader.append("<div style='display:table-row'>\n");
		/*sbAttendHistoryHeader.append("<div style='display:table-cell'>Date</div>\n");
		if (isV2Header) {
			sbAttendHistoryHeader.append("<div style='display:table-cell;padding-left:10px'>Course</div>\n");
			sbAttendHistoryHeader.append("<div style='display:table-cell;padding-left:10px'>Session</div>\n");
		}
		else
			sbAttendHistoryHeader.append("<div style='display:table-cell;padding-left:10px'>Session No</div>\n");
		sbAttendHistoryHeader.append("<div style='display:table-cell;padding-left:10px'>Status</div>\n");*/
		sbAttendHistoryHeader.append("</div>");
		map.put("attendance_history_header", sbAttendHistoryHeader.toString());
		map.put("attendance_history_rows", sbAttendHistory.toString());
		map.put("attendance_history_display", sbAttendHistory.length() > 0 ? "block" : "none");
		/*int i;
		for (i = 0; i < 10; i++) {
			map.put(String.format("attendance_history_row%d_display", i), "none");
			map.put(String.format("attendance_history_date%d", i), "");
			map.put(String.format("attendance_history_sessionno%d", i), "");
			map.put(String.format("attendance_history_status%d", i), "");
		}
		i = 0;
		String[] ahss = StringUtils.split(attendHistory, "\n");
		if (ahss != null && ahss.length > 0) {
			for (String ahs : ahss) {
				if (i >= 10)
					break;
				String[] hs = ahs.split("\t", -1);
				if (hs != null && hs.length == 3) {
					map.put(String.format("attendance_history_row%d_display", i), "table-row");
					map.put(String.format("attendance_history_date%d", i), hs[0]);
					map.put(String.format("attendance_history_sessionno%d", i), hs[1]);
					map.put(String.format("attendance_history_status%d", i), hs[2]);
					i++;
				}
			}
		}
		map.put("attendance_history_display", i > 0 ? "block" : "none");*/

		StringSubstitutor sub = new StringSubstitutor(map);
		return sub.replace(contentTemplate);
	}
}
