package com.uniinformation.zkbi.afs;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.ErrorEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Label;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Vlayout;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxCalendar;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerAfsServiceJob2 extends ZkBiComposerBase {
	ZkJxCalendar calendar;
	Combobox calendarMonth;
	Hashtable<String,Date> monthList = new Hashtable<String,Date>();
	public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
    	if(!masterWin.hasFellow("afsCalendar")) return;
    	calendar = (ZkJxCalendar) masterWin.getFellow("afsCalendar");
    	calendarMonth = (Combobox) masterWin.getFellow("afsCalendarMonth");
    	/*
    	Comboitem ci = calendarMonth.appendItem("Jan 2018"); 
    	ci.setValue(DateUtil.getDate(2019, 1, 1));
    	ci = calendarMonth.appendItem("Feb 2018"); 
    	ci.setValue(DateUtil.getDate(2019, 2, 1));
    	ci = calendarMonth.appendItem("Mar 2018"); 
    	ci.setValue(DateUtil.getDate(2019, 3, 1));
    	ci = calendarMonth.appendItem("Apr 2018"); 
    	ci.setValue(DateUtil.getDate(2019, 4, 1));
    	*/
      	calendarMonth.addEventListener(Events.ON_CHANGE, 
        		new EventListener() {
        			public void onEvent (Event ev) {
        				UniLog.log("Calender Month Changed");
        				Comboitem ci = calendarMonth.getSelectedItem();
        				if(ci != null) {
        					Date dd = (Date) ci.getValue();
        					updateCalendar(dd,result) ;
        					/*
        					calendar.setCalendarMonthByDate(dd);
							for(int i=0;i<result.getRowCount();i++) {
								result.loadOneRecV(i);
								Date d = result.getCell("svjob_stdate").getDate();
					        	calendar.clearContentArea(d);
					        	Component comp = calendar.getContentArea(d);
					        	if(comp != null) {
					        		comp.appendChild(new Label(result.getCell("svjob_jobcode").getString()){{ setStyle("background:#9af19a;");}});
					        	}
							}
							*/
        					/*
        					Component comp = calendar.clearContentArea(
        								new Date()
        							);
        					if(comp != null) {
        						Vlayout vl = new Vlayout();
        						vl.appendChild(new Label("Task 001"));
        						vl.appendChild(new Label("Task 002"));
        						vl.appendChild(new Label("Task 003"));
        						vl.appendChild(new Label("Task 004"));
        						comp.appendChild(vl);
        					}
        					*/
        				}
        			}
        		}
        	);	
    	if(!masterWin.hasFellow("afsCalendarTab")) return;
    	Tab calendarTab = (Tab) masterWin.getFellow("afsCalendarTab");
    	calendarTab.addEventListener(Events.ON_SELECT, 
    		new EventListener() {
    			public void onEvent (Event ev) {
    				UniLog.log("Calender Tab Selected");
//    				calendar.setCalendarMonthByDate(new Date());
    				Date dd = createCalendar(result);
    				if(dd != null) {
    					calendarMonth.setSelectedIndex(0);
    					updateCalendar(dd,result) ;
    				}
    			}
    		}
    	);
	}
	void updateCalendar(Date dd,BiResult result) {
       	calendar.setCalendarMonthByDate(dd);
		for(int i=0;i<result.getRowCount();i++) {
			result.loadOneRecV(i);
			Date d = result.getCell("svjob_stdate").getDate();
			calendar.clearContentArea(d);
			Component comp = calendar.getContentArea(d);
			if(comp != null) {
				comp.appendChild(new Label(result.getCell("svjob_jobcode").getString()){{ setStyle("background:#9af19a;");}});
			}
		}
	}
	Date createCalendar(BiResult br) {
		Date lastCdate = calendar.getCurrentDateStart();
		Date listSdate=null;
		Date listEdate=null;
		for(int i=0;i<br.getRowCount();i++) {
			br.loadOneRecV(i);
			Date d = br.getCell("svjob_stdate").getDate();
			if(listSdate == null || listSdate.after(d)) {
				listSdate = d;
			}
			if(listEdate == null || listEdate.before(d)) {
				listEdate = d;
			}
		}
		if(listSdate == null || listEdate == null) {
			calendar.setCalendarMonthByDate(DateUtil.today());
			return(null);
		}
		int n = calendarMonth.getItemCount();
		for(int i = n-1;i>=0;i--) {
			calendarMonth.removeItemAt(i);
		}
		for(Date cDate = DateUtil.dayBeginning(listSdate);!cDate.after(listEdate);cDate = DateUtil.nextmonth(cDate)) {
			Calendar cal=Calendar.getInstance();
			cal.setTime(cDate);
			Comboitem ci = calendarMonth.appendItem(String.format("%d-%02d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH) + 1));
			ci.setValue(cDate);
		}
		calendar.setCalendarMonthByDate(listSdate);
		return(listSdate);
		/*
		if(lastCdate == null || lastCdate.before(listSdate) || lastCdate.after(listEdate)) {
			calendar.setCalendarMonthByDate(listSdate);
			return(listSdate);
		} else {
			calendar.setCalendarMonthByDate(lastCdate);
			return(lastCdate);
		}
		*/
	}
}
