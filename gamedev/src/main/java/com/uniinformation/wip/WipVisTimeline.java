package com.uniinformation.wip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Div;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VisUtil;

public class WipVisTimeline {
	Component visJsEventDiv;
	String visJsContentDiv;
	WipGetTaskListInterface getTaskList;
	
	public WipVisTimeline(WipGetTaskListInterface p_interface, Component p_eventDiv,String p_contentDiv) {
		getTaskList = p_interface;
		visJsEventDiv = p_eventDiv;
		visJsContentDiv = p_contentDiv;
	}
	/*
	public void afterBind()
	{
		JxField f;
		UniLog.log("in JxJobStatusTimeline afterbind");
		new JxFieldAction("btToday") {
			public void actionPerformed(JxField fd) 
			{
				redrawTimeline(0);
			}
		};
		new JxFieldAction("btCurrWeek") {
			public void actionPerformed(JxField fd) 
			{
				redrawTimeline(1);
			}
		};
		new JxFieldAction("btCurrMonth") {
			public void actionPerformed(JxField fd) 
			{
				redrawTimeline(2);
			}
		};
		f = jxAdd("visEventDiv");
		if (f != null){
			Div div = (Div)f.getNativeObject();
			div.addEventListener("onVisupdate", 
				new EventListener() {
					public void onEvent(Event event) throws Exception {
						UniLog.log("Event Received:" + event.getName() +","+event.getData());
					}		
				});
			div.addEventListener("onVisremove", 
				new EventListener() {
					public void onEvent(Event event) throws Exception {
						UniLog.log("Event Received:" + event.getName() +","+event.getData());
					}		
				});
			
		}
		else {
			UniLog.log("zkDiv is null");
		}
	}
	public void setBiResult(BiResult p_br)
	{
		br = p_br;
	}
	*/
	public void redrawTimeline(int p_mode)
	{
				try{
				    JSONObject jo = new JSONObject();
				    
				    JSONArray jaItems = new JSONArray();
				    /*
			    	jaItems.put((new JSONObject()).put("id", 1)
			    								  .put("content", "event1 very long long long line")
			    								  .put("start", VisUtil.getDateTimeStr(2016, 12, 30, 0, 0, 0))
			    								  .put("end", VisUtil.getDateTimeStr(2016, 12, 30, 23, 59, 59))
			    								  .put("type", "point"));
			    	jaItems.put((new JSONObject()).put("id", 2)
			    								  .put("content", "event2<br>with line break tag")
			    								  .put("start", VisUtil.getDateTimeStr(2016, 12, 26, 0, 0, 0))
			    								  .put("end", VisUtil.getDateTimeStr(2016, 12, 26, 23, 59, 59))
			    								  .put("type", "point"));
			    	jaItems.put((new JSONObject()).put("id", 3)
			    								  .put("content", "event3<a href=\"http://www.google.com\">hello url</a>")
			    								  .put("start", VisUtil.getDateTimeStr(2016, 12, 25, 0, 0, 0))
			    								  .put("type", "point"));
			    								  */				    
					for(int i=0;i<getTaskList.getCount();i++) {
						int yy,mm,dd,hr,mn;
						int yy2,mm2,dd2,hr2,mn2;
						Date d0 = getTaskList.getStart(i);
						if(d0 == null || !d0.after(DateUtil.minTime)) {
							d0 = DateUtil.today();
						}
						Date d1 = getTaskList.getEnd(i);
						if(d1 == null || !d1.after(DateUtil.minTime)) {
//							d1 = new Date();
							d1 = DateUtil.dayEnding(new Date());
						}
							
						yy = com.kyoko.common.DateUtil.getYear(d0);
						mm = com.kyoko.common.DateUtil.getMonth(d0);
						dd = com.kyoko.common.DateUtil.getDay(d0);
						hr = DateUtil.getHour(d0);
						mn = DateUtil.getMinute(d0);
						yy2= com.kyoko.common.DateUtil.getYear(d1);
						mm2= com.kyoko.common.DateUtil.getMonth(d1);
						dd2= com.kyoko.common.DateUtil.getDay(d1);
						hr2= DateUtil.getHour(d1);
						mn2= DateUtil.getMinute(d1);

						jaItems.put((new JSONObject())
										.put("id", getTaskList.getId(i))
	    								.put("content", getTaskList.getName(i))
//			    						.put("start", VisUtil.getDateTimeStr(yy, mm, dd, hr, mn, 0))
//			    						.put("end", VisUtil.getDateTimeStr(yy2, mm2, dd2, hr2, mn2, 0))
			    						.put("start", VisUtil.getDateTimeStr(yy, mm, dd, 00, 00, 0))
			    						.put("end", VisUtil.getDateTimeStr(yy2, mm2, dd2, 23, 59, 0))
	    								.put("style","color:white;background:green;")
	    								.put("title",getTaskList.getTitle(i))
			    						.put("type", "box"));
						
						
//						if(br.getCell("woppdate").getDate().after(com.uniinformation.utils.DateUtil.minDate)) {
//							UniLog.log("HAHA 2016 add one running event " + i +  " " + br.getCell("woppdate").getDate());
//							Date d0 = br.getCell("woppdate").getDate();
//							int yy = com.uniinformation.utils.DateUtil.getYear(d0);
//							int mm = com.uniinformation.utils.DateUtil.getMonth(d0);
//							int dd = com.uniinformation.utils.DateUtil.getDay(d0);
//							int hr = br.getCell("wopptime").getInt() / 100;
//							int mn = br.getCell("wopptime").getInt() % 100;
//							Date d1 = getDateTime(yy, mm, dd, hr, mn, 0);
//							Date d2 = new Date();
//							int yy2 = com.uniinformation.utils.DateUtil.getYear(d2);
//							int mm2 = com.uniinformation.utils.DateUtil.getMonth(d2);
//							int dd2 = com.uniinformation.utils.DateUtil.getDay(d2);
//							int hr2 = com.uniinformation.utils.DateUtil.getHour(d2);
//							int mi2 = com.uniinformation.utils.DateUtil.getMinute(d2);
//							jaItems.put((new JSONObject()).put("id", br.getCell("woref").getInt())
//	    								  .put("content", ""+br.getCell("woref").getInt()+" "+br.getCell("wowipstatus").getString())
//			    						  .put("start", VisUtil.getDateTimeStr(yy, mm, dd, hr, mm, 0))
//			    						   .put("end", VisUtil.getDateTimeStr(yy2, mm2, dd2, hr2, mi2, 0))
//	    								  .put("style","color:white;background:green;")
//	    								  .put("title","HAHA 123")
//			    							.put("type", "box"));
//
//						} else 
//							if(br.getCell("woprtdate").getDate().after(com.uniinformation.utils.DateUtil.minDate)) {
//								UniLog.log("HAHA 2016 add one ready event " + i +  " " + br.getCell("woprtdate").getDate());
//								Date d2 = br.getCell("woprtdate").getDate();
//								int yy2 = com.uniinformation.utils.DateUtil.getYear(d2);
//								int mm2 = com.uniinformation.utils.DateUtil.getMonth(d2);
//								int dd2 = com.uniinformation.utils.DateUtil.getDay(d2);
//							jaItems.put((new JSONObject()).put("id", br.getCell("woref").getInt())
//			    								  .put("content", ""+br.getCell("woref").getInt())
//			    								  .put("start", VisUtil.getDateTimeStr(yy2, mm2, dd2, 0, 0, 0))
//			    								  .put("end", VisUtil.getDateTimeStr(yy2, mm2, dd2, 0, 0, 0))
//			    								  .put("type", "point"));
//						}
						
					}	
				    JSONObject joOptions = new JSONObject();
				    joOptions.put("width","100%");
				    joOptions.put("minHeight","500px");
				    joOptions.put("orientation","top");
//				    joOptions.put("editable",true);
//				    joOptions.put("start",VisUtil.getDateTimeStr(2017, 2, 25, 0, 0, 0));
//				    joOptions.put("end",VisUtil.getDateTimeStr(2017, 3, 3, 0, 0, 0));
				    joOptions.put("align","left");
					switch(p_mode) {
					case 1:
						joOptions.put("start",VisUtil.getDayBegin(DateUtil.weekStart(new Date())));
						joOptions.put("end",VisUtil.getDayEnd(DateUtil.weekEnd(new Date())));
							break;
					case 2: 
						joOptions.put("start",VisUtil.getDayBegin(DateUtil.monthStart(new Date())));
						joOptions.put("end",VisUtil.getDayEnd(DateUtil.monthEnd(new Date())));
							break;
					default:
						joOptions.put("start",VisUtil.getDayBegin(new Date()));
						joOptions.put("end",VisUtil.getDayEnd(new Date()));
							break;
					}
				    joOptions.put("zoomMax",31536000000L);
				    joOptions.put("zoomMin",86400000L);
				    //joOptions.put("locale","tw"); //not supported
				    
				    jo.put("containerId", visJsContentDiv);
				    jo.put("options", joOptions);
				    jo.put("items", jaItems);
				    

		          System.out.print("result"+jo.toString(5));
		          Clients.evalJavaScript(String.format("visUtilProcessTimeline(%s,'"+visJsEventDiv.getId()+"')", jo.toString(5)));
		          
		          
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
				
	}
	public void initTimeline()
	{
	}
	private Date getDateTime(int year, int month, int day, int hour, int min, int sec){
		   //Calendar calendar = GregorianCalendar.getInstance();
		   //Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("HKT"));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd HH:mm:ss");
			Calendar calendar = new GregorianCalendar(year,month-1,day,hour,min,sec);
			calendar.setTimeZone(TimeZone.getTimeZone("GMT+8"));  //HAHA TODO hardcode timezone
			System.out.println(sdf.format(calendar.getTime()));
			return (calendar.getTime());
	}
}
