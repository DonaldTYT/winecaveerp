package com.uniinformation.jx.zk;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Layout;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Vlayout;

import com.kyoko.common.DateUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;


public class ZkJxCalendar extends Grid {
	static public final int CALENDAR_MODE_DAY=1;
	static public final int CALENDAR_MODE_WEEK=2;
	static public final int CALENDAR_MODE_MONTH=3;
	static public final int CALENDAR_MODE_YEAR=4;
	ArrayList <DayCell> dayCells;
	int year=0;
	int month=0;
	int firstDayIdx=0;
	int daysOfMonth=0;
	int calendarMode=0;
	String cellWidth="150px";
	String cellHeight="100px";
	Calendar cal=Calendar.getInstance();
	class DayCell {
		Layout layout;
		Div dayLabelArea;
		HtmlBasedComponent contentArea;
		Date cellDate;
		DayCell(Layout p_layout, Div p_dayLabelArea,HtmlBasedComponent p_contentArea) {
			layout = p_layout;
			dayLabelArea = p_dayLabelArea;
			//dayLabelArea.setStyle("background:red!important;");
			contentArea = p_contentArea;
			//((HtmlBasedComponent) contentArea).setStyle("background:green!important;");
		}
		void clearContentArea() {
			for(Component comp : contentArea.getChildren()){
				contentArea.removeChild(comp);
			}
		}
		void clear() {
			for(Component comp : dayLabelArea.getChildren()){
				dayLabelArea.removeChild(comp);
			}
			clearContentArea();
		}
		
		void addDayLabel(HtmlBasedComponent comp) {
			dayLabelArea.appendChild(comp);
		}
	}
	public ZkJxCalendar() {
		super();
		setSizedByContent(true);
		//setCalendarMonth(2019,1);
	}
	void setCalendarMode(int p_mode) {
		if(calendarMode == p_mode) return;
		calendarMode = p_mode;
		switch(calendarMode) {
		case CALENDAR_MODE_MONTH:

		dayCells = new ArrayList<DayCell>();
		Columns cols = new Columns();
		appendChild(cols);
		for(int j=0;j<7;j++) {
			Column col = new Column();
			col.setWidth(cellWidth);
			switch(j) {
			case 0: col.setLabel("Sunday");
					break;
			case 1: col.setLabel("Monday");
					break;
			case 2: col.setLabel("Tuesday");
					break;
			case 3: col.setLabel("Wednesday");
					break;
			case 4: col.setLabel("Thursday");
					break;
			case 5: col.setLabel("Friday");
					break;
			case 6: col.setLabel("Saturday");
					break;
			}
			cols.appendChild(col);
		}
		
		Rows rows = new Rows();
		appendChild(rows);
		for(int i=0;i<6;i++) {
			Row row = new Row();
			rows.appendChild(row);
			for(int j=0;j<7;j++) {
				Cell ce = new Cell();
				ce.setStyle("padding:0px 0px; border-right: solid 1px #f5f5f5;");
				Layout cellLayout = new Vlayout();
				/*
				if (j==0){
					ZkUtil.appendStyle(cellLayout, "background: #ffe7e7");
				}
				*/
				ce.appendChild(cellLayout);
				Div dayLabelDiv = new Div();	
				dayLabelDiv.setStyle("line-height:initial;");
				cellLayout.setHeight(cellHeight);
				//dayLabelDiv.setHeight(cellHeight);
				cellLayout.appendChild(dayLabelDiv);
				Vlayout contentArea = new Vlayout();
				dayCells.add(new DayCell(cellLayout,dayLabelDiv,contentArea));
				cellLayout.appendChild(contentArea);
				row.appendChild(ce);
			}
		}
		
			break;
		}
	}
	public void setCalendarMonthByDate(Date p_date){
		cal.setTime(p_date);
		setCalendarMonth(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1);
	}
	public void setCalendarMonth(int p_year,int p_month){
		if(p_year < 1970 || p_year > 2047 || p_month < 1 || p_month > 12) {
			UniLog.log(new Exception("setMonth error " + p_year + " " + p_month));
			return;
		}
		setCalendarMode(CALENDAR_MODE_MONTH);
		year = p_year;
		month = p_month-1;
		cal.set(year,month,1,0,0);
		
//		Date d = DateUtil.getDate(year, month, 1 );
		Date d = cal.getTime();
		UniLog.log("Date start = " + d);
		
		UniLog.log("Weekday = " + (cal.get(Calendar.DAY_OF_WEEK)-1));
		UniLog.log("Number of days = " + cal.getActualMaximum(Calendar.DAY_OF_MONTH));
		firstDayIdx = cal.get(Calendar.DAY_OF_WEEK)-1;
		daysOfMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		
		for(int i=0;i<dayCells.size();i++) {
			boolean inCurrentMonth = false;
			if (i >= firstDayIdx && i<firstDayIdx+daysOfMonth){
				inCurrentMonth = true;
			}
			
			//clear the cell
			DayCell dc = dayCells.get(i);
			dc.clear();
			
			//set cell background color
			if (inCurrentMonth){
				dc.layout.setStyle("background:#fff;");
				ZkUtil.appendStyle(dc.dayLabelArea, "opacity:0.8;");
			}
			else{
				dc.layout.setStyle("background:#f9f9f9;");
				ZkUtil.appendStyle(dc.dayLabelArea, "opacity:0.4;");
			}
		}
		
		//fill label of previous month
		Calendar preMonthCal = Calendar.getInstance();
		preMonthCal.set(year, month-1,1,0,0);
		int daysOfPreMonth = preMonthCal.getActualMaximum(Calendar.DAY_OF_MONTH);
		int dcIdx=0;
		for (int i=firstDayIdx-1; i>=0; i--){
			DayCell dc = dayCells.get(dcIdx);
			dc.addDayLabel(new Label(""+(daysOfPreMonth-i)));
			dcIdx++;
		}
		
		//fill label of current month
		for(int i=0;i<daysOfMonth;i++) {
			int dayIdx = i + firstDayIdx;
			DayCell dc = dayCells.get(dayIdx);
			dc.addDayLabel(new Label(""+(i+1)));
		}
		
		//fill label of next month
		int tmpDay = 1;
		for (int i=daysOfMonth+firstDayIdx; i<dayCells.size(); i++){
			DayCell dc = dayCells.get(i);
			dc.addDayLabel(new Label(""+tmpDay++));
		}
		
		//set label color
		for (int i=0; i<dayCells.size(); i++){
			if (i % 7 == 0){
				ZkUtil.appendStyle(dayCells.get(i).dayLabelArea, "color:red;");
			}
		}
		/*
		int i = 0;
		for(int j=0;j<firstDayIdx;j++,i++) {
			dayCells.get(i).clear();
		}
		for(int j=0;j<daysOfMonth;j++,i++) {
			dayCells.get(i).addDayLabel(new Label(""+(j+1)));
		}
		for(;i<dayCells.size();i++) {
			dayCells.get(i).clear();
		}
		*/
	}
	
	DayCell getDayCell(Date p_date) {
		switch(calendarMode) {
		case CALENDAR_MODE_MONTH : 
			cal.setTime(p_date);
			if(cal.get(Calendar.YEAR) != year) return(null);
			if(cal.get(Calendar.MONTH) != month) return(null);
			int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
			return(dayCells.get(firstDayIdx + dayOfMonth-1));
		}
		return(null);
	}
	public Component getContentArea(Date p_date) {
		DayCell dc = getDayCell(p_date);
		if(dc != null) return(dc.contentArea);
		return(null);
	}
	public void clearContentArea(Date p_date) {
		DayCell dc = getDayCell(p_date);
		if(dc != null) {
			dc.clearContentArea();
		}
	}
	
	public Date getCurrentDateStart() {
		switch(calendarMode) {
		case CALENDAR_MODE_MONTH:
			return(DateUtil.getDate(year,month+1,1));
		}
		return(null);
	}
}
