package com.uniinformation.jx.zk;

import java.sql.Time;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Bandpopup;
import org.zkoss.zul.Button;
import org.zkoss.zul.Calendar;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timebox;
import org.zkoss.zul.Vlayout;

import com.kyoko.common.DateUtil;
import com.uniinformation.cell.Cell;
import com.uniinformation.jxapp.JxSelOpt;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class ZkJxQueryInput extends Bandbox {
	/*
	public final static int TYPE_STRING = 1;
	public final static int TYPE_DATE = 2;
	public final static int TYPE_FLOAT = 3;
	public final static int TYPE_BOOLEAN = 4;
	public final static int TYPE_INTEGER = 5;
	public final static int TYPE_DATETIME = 6;
	*/
	public final static int TYPE_STRING = Cell.VTYPE_STRING;
	public final static int TYPE_DATE = Cell.VTYPE_DATE;
	public final static int TYPE_FLOAT = Cell.VTYPE_DOUBLE;
	public final static int TYPE_BOOLEAN = Cell.VTYPE_BOOLEAN;
	public final static int TYPE_INTEGER = Cell.VTYPE_INT;
	public final static int TYPE_DATETIME = Cell.VTYPE_DATETIME;
	private final static String DEFAULT_TIME_STRING = "00:00:00";
	private final static Pattern REL_DATE_PATTERN = Pattern.compile("@([A-Za-z]+)\\(\\) *(([+-]) *(\\d+))?");
	private final static Pattern TIME_PATTERN = Pattern.compile(" (([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9]))");
	AbstractGetItemProperty gipi;
	String[] colOptionList;
	boolean isMultiplePickSelect;
	boolean needCheckOptionListIndex, needUnionOptionList;
	int type;
	Bandpopup bp;
	Vlayout datePopup;
	Vlayout stringPopup;
	Listbox stringListbox;
	ListModelList stringModelList;
	Button stringPopupCloseButton, stringPopupClearButton;
	Radiogroup dateAbsRelFlag;
	Vlayout dateRelPanel;
	//	Combobox dateRelStr;
	Listbox dateRelStr;
	Spinner dateRelOfs;
	Calendar cal;
	Timebox caltmb;
	
	private boolean readonlyTextMode;
	private SessionHelper sessionHelper;
	
	
	EventListenerCallback eventListenerCallback;
	public interface EventListenerCallback {
		void callback(Event event) throws Exception;
	}
	public void setEventListenerCallback(EventListenerCallback eventListenerCallback) {
		this.eventListenerCallback = eventListenerCallback;
	}

	int myParseInt(String p_s) {
		int n;
		String s = p_s.trim();
		if(s.equals("")) return(0);
		if(s.charAt(0) == '+') {
			n = Integer.parseInt(s.substring(1));
		} else {
			n = Integer.parseInt(s);
		}
		return(n);
	}

	int myStartsWith(String s,String pattern) {
		if(s.startsWith(pattern)) {
			return(pattern.length());
		} else return(-1);
	}
	public String getQueryString()  {
		String rtnStr = "";
		switch(type) {
		case TYPE_DATE:
		case TYPE_DATETIME:
			//UniLog.log("HAHA in getQueryString");
			if (getRealText().trim().equals("")) {
				rtnStr = "";
			}
			else if (dateAbsRelFlag.getSelectedIndex()==0){
				rtnStr = getRealText();
			}
			else{
				String s = dateRelStr.getSelectedItem().getValue();
				int n = dateRelOfs.getValue();
				if(n != 0) s += new DecimalFormat("+0;-#").format(n);
				if (type == TYPE_DATETIME){
					s += " " + caltmb.getText();
				}
				rtnStr = s;
			}
			break;
		default: 
			rtnStr = getRealText();
			break;
		}
		//UniLog.log1("return %s",rtnStr);
		return(rtnStr);
	}
	private Date parseDateStr(String p_dateStr){
		Date val = null;
		if (StringUtils.isNotBlank(p_dateStr)){
			String dateStr = p_dateStr.trim();
			val = DateUtil.dateTimeStrToDate(dateStr);
			/*try{
				if (dateStr.length() >= 19){
					val = DateUtils.parseDate(dateStr, "yyyy/MM/dd HH:mm:ss");
				}
				else if (dateStr.length() >= 16){
					val = DateUtils.parseDate(dateStr, "yyyy/MM/dd HH:mm");
				}
				else if (dateStr.length() >= 16){
					val = DateUtils.parseDate(dateStr, "yyyy/MM/dd HH:mm");
				}
				else if (dateStr.length() >= 10){
					val = DateUtils.parseDate(dateStr, "yyyy/MM/dd");
				}
				else{
					UniLog.log1("cannot convert to date: %s", p_dateStr);
				}
			}
			catch(Exception ex){
				UniLog.log1("cannot convert to date: %s %s", p_dateStr, ex.getMessage());
			}*/
		}
		UniLog.log1("result %s", val);
		return(val);

	}
	public Object getQueryObjectFromText(String strVal){
		Object val = null;
		switch(type) {
			case TYPE_STRING:
				val = strVal;
				break;
			case TYPE_DATE:
				strVal = StringUtils.defaultString(strVal).trim();
				val = parseDateStr(strVal);
				if (val != null && strVal.matches(REL_DATE_PATTERN.pattern()))
					val = strVal;
				break;
			case TYPE_FLOAT:
				try{
					val = Double.parseDouble(strVal);
				}
				catch(Exception ex){
					UniLog.log1("cannot convert to double: %s %s", strVal, ex.getMessage());
				}
				break;
			case TYPE_BOOLEAN:
				if (StringUtils.equalsAnyIgnoreCase(strVal, "Y","TRUE","YES")){
					val = new Boolean(true);
				}
				else{
					val = new Boolean(false);
				}
				break;
			case TYPE_INTEGER:
				try{
					val = Integer.parseInt(strVal);
				}
				catch(Exception ex){
					UniLog.log1("cannot convert to int: %s %s", strVal, ex.getMessage());
				}
				break;
			case TYPE_DATETIME:
				val = parseDateStr(strVal);
				break;
			default:  //assume string
				UniLog.log1("type not supported: %d", type);
				break;
		}
		return(val);
	}
	/***
	 * This call is designed for Expression
	 * @return null if error
	 */
	public Object getQueryObject(){
		//String strVal = getQueryString(); 
		String strVal = getRealText();
		return getQueryObjectFromText(strVal);
	}

	public void setQueryString(String v) 
	{
		/*
		UniLog.log("HAHA in ZkJxQueryInput setvalue");
		UniLog.log(new Exception("Ex"));
		 */
		switch(type) {
		case TYPE_DATE:
		case TYPE_DATETIME:
			if(v != null) {
				String val = StringUtils.defaultString(v).trim();
				if (val.startsWith("Today"))
					val = val.replaceFirst("Today", "@today()");
				else if (val.startsWith("Week Start"))
					val = val.replaceFirst("Week Start", "@weekStart()");
				else if (val.startsWith("Week End"))
					val = val.replaceFirst("Week End", "@weekEnd()");
				else if (val.startsWith("Month Start"))
					val = val.replaceFirst("Month Start", "@monthStart()");
				else if (val.startsWith("Month End"))
					val = val.replaceFirst("Month End", "@monthEnd()");
				else if (val.startsWith("Year Start"))
					val = val.replaceFirst("Year Start", "@yearStart()");
				else if (val.startsWith("Year End"))
					val = val.replaceFirst("Year End", "@yearEnd()");
				Matcher m = TIME_PATTERN.matcher(val);
				if (val.startsWith("@") && m.find()) {
					Time t = Time.valueOf(m.group(1));
					int sec = DateUtil.getHour(t) * 3600 + DateUtil.getMinute(t) * 60 + DateUtil.getSecond(t);
					val = m.replaceFirst("+" + sec + "/86400");
				}
				UniLog.log("setQueryString:" + v + "," + val);
				setDateValueToPopup(true, val);
				/*String dateValue = null;
				String timeValue = null;
				if (type == TYPE_DATETIME) {
					String[] ss = splitDateAndTime(v);
					dateValue = ss[0];
					timeValue = ss[1];
					if (timeValue == null)
						timeValue = DEFAULT_TIME_STRING;
				} 
				else{
					dateValue = v;
				}
				int relidx=-1;
				int sofs=0;
				if(relidx < 0) if((sofs = myStartsWith(dateValue,"Today")) >= 0) relidx=0;
				if(relidx < 0) if((sofs = myStartsWith(dateValue,"Week Start")) >= 0) relidx=1;
				if(relidx < 0) if((sofs = myStartsWith(dateValue,"Week End")) >= 0) relidx=2;
				if(relidx < 0) if((sofs = myStartsWith(dateValue,"Month Start")) >= 0) relidx=3;
				if(relidx < 0) if((sofs = myStartsWith(dateValue,"Month End")) >= 0) relidx=4;
				if(relidx < 0) if((sofs = myStartsWith(dateValue,"Year Start")) >= 0) relidx=5;
				if(relidx < 0) if((sofs = myStartsWith(dateValue,"Year End")) >= 0) relidx=6;
				if(relidx >= 0) {
					dateAbsRelFlag.setSelectedIndex(1);
					dateRelStr.setSelectedIndex(relidx);
					dateRelOfs.setValue(myParseInt(dateValue.substring(sofs)));
					setDateValueFromPopup(null);
				} else {
					dateAbsRelFlag.setSelectedIndex(0);
					super.setText(dateValue + (timeValue != null ? " " + timeValue : ""));
				}
				if(dateAbsRelFlag.getSelectedIndex() == 0) {
					dateRelPanel.setVisible(false);
					cal.setVisible(true);
				} else  {
					dateRelPanel.setVisible(true);
					cal.setVisible(false);
				}*/
			} else setRealText(v);
			break;
		default : 
			setRealText(v);
			break;
		}
	}

	void setDateValueFromPopup(Object o) {
		setDateValueFromPopup(o, null);
	}
	void setDateValueFromPopup(Object o, String timeStr)
	{
		if(dateAbsRelFlag.getSelectedIndex() == 0) { 
			if (type == TYPE_DATETIME)
				setRealText(DateUtil.toDateString(cal.getValue(),"yyyy/mm/dd") + " " + (timeStr != null ? timeStr : caltmb.getText()));
			else
				setRealText(DateUtil.toDateString(cal.getValue(),"yyyy/mm/dd"));
		} 
		else {
			StringBuilder sbValue = new StringBuilder();
			Date d0;
			switch(dateRelStr.getSelectedIndex()){
			case 0://Today
				d0 = DateUtil.today();
				sbValue.append("@today()");
				break;
			case 1://Week Start
				d0 = DateUtil.weekStart(DateUtil.today());
				sbValue.append("@weekStart()");
				break;
			case 2://Week End;
				d0 = DateUtil.weekEnd(DateUtil.today());
				sbValue.append("@weekEnd()");
				break;
			case 3://Month Start;
				d0 = DateUtil.monthStart(DateUtil.today());
				sbValue.append("@monthStart()");
				break;
			case 4://Month End;
				d0 = DateUtil.monthEnd(DateUtil.today());
				sbValue.append("@monthEnd()");
				break;
			case 5://Year Start;
				d0 = DateUtil.yearStart(DateUtil.today());
				sbValue.append("@yearStart()");
				break;
			case 6://Year End;
				d0 = DateUtil.yearEnd(DateUtil.today());
				sbValue.append("@yearEnd()");
				break;
			default:
				d0 = DateUtil.today();
				sbValue.append("@today()");
			}
			int ofs;
			if(o != null) {
				try {
					if(o instanceof String) {
						ofs = myParseInt((String) o) ;
						String s = (String) o;
						if(s.startsWith("+")) {
							ofs = Integer.parseInt(s.substring(1));
						} else {
							ofs = Integer.parseInt(s);
						}
					} else 
						if(o instanceof Integer) ofs = ((Integer)o).intValue();
						else {
							UniLog.log("Unknow object type for dateRelOfs" );
							return;
						}
				} catch (Exception ex) {
					UniLog.log(ex);
					return;
				}
			} else ofs = dateRelOfs.getValue();
			if (ofs != 0)
				sbValue.append((ofs >= 0 ? "+" : "") + ofs);
			long t = d0.getTime() + (long) 86400000 * ofs;

			if (type == TYPE_DATETIME)
				setRealText(DateUtil.toDateString(new Date(t),"yyyy/mm/dd") + " " + (timeStr != null ? timeStr : caltmb.getText()));
			else
				setRealText(sbValue.toString());
				//setValue(DateUtil.toDateString(new Date(t),"yyyy/mm/dd"));

		}
	}
	/*String[] splitDateAndTime(String v) {
		String dateValue = null;
		String timeValue = null;
		try {
			v = v.trim();
			int pos = v.lastIndexOf(' ');
			if (pos > 0) {
				dateValue = v.substring(0, pos).trim();
				timeValue = v.substring(pos).trim();
			} else
				dateValue = v;
		} catch (Exception e) {
			dateValue = v;
		}
		return new String[]{dateValue, timeValue};
	}*/
	void setDateValueToPopup(boolean choosePanel, String customValue)
	{
		String tbVal = StringUtils.defaultString(customValue != null ? customValue : getRealText()).trim();
		Date d = parseDateStr(tbVal);
		Time t = d != null ? new Time(d.getTime()) : null;
		int absOrRelPanel = dateAbsRelFlag.getSelectedIndex();
		String relPanelDateCbStr = "";
		String relPanelPlusMinus = "+";
		int relPanelPlusMinusNum = 0;
		UniLog.logm(this, "setDateValueToPopup choosePanel:" + choosePanel + ",d:" + d + ",type:" + type);
		if (choosePanel) {
			absOrRelPanel = 0;
			if (type == TYPE_DATE) {
				if (d != null) {
					Matcher m = REL_DATE_PATTERN.matcher(tbVal);
					if (m.matches()) {
						absOrRelPanel = 1;
						relPanelDateCbStr = m.group(1);
						if (StringUtils.isNotBlank(m.group(2))) {
							relPanelPlusMinus = m.group(3);
							try {
								relPanelPlusMinusNum = Integer.parseInt(m.group(4));
							} catch (NumberFormatException e) {
							}
						}
						UniLog.logm(this, "setDateValueToPopup relPanelDateCbStr:" + relPanelDateCbStr + ",relPanelPlusMinus:" + relPanelPlusMinus + ",relPanelPlusMinusNum:" + relPanelPlusMinusNum);
					}
				}
			}
		}
		if (d == null)
			d = DateUtil.today();
		String val;
		if (type == TYPE_DATETIME) {
			if (t == null)
				t = new Time(System.currentTimeMillis());
			val = DateUtil.toDateString(d,"yyyy/mm/dd") + " " + t;
		} else if (!choosePanel || absOrRelPanel == 0)
			val = DateUtil.toDateString(d,"yyyy/mm/dd");
		else
			val = tbVal;
		if (choosePanel) {
			dateAbsRelFlag.setSelectedIndex(absOrRelPanel);
			if (absOrRelPanel == 0) {
				dateRelPanel.setVisible(false);
				cal.setVisible(true);
			} else {
				dateRelPanel.setVisible(true);
				cal.setVisible(false);
			}
		}
		if (dateAbsRelFlag.getSelectedIndex() == 0)
			cal.setValue(d);
		else {
			if (choosePanel) {
				if (relPanelDateCbStr.equalsIgnoreCase("today"))
					dateRelStr.setSelectedIndex(0);
				if (relPanelDateCbStr.equalsIgnoreCase("weekStart"))
					dateRelStr.setSelectedIndex(1);
				if (relPanelDateCbStr.equalsIgnoreCase("weekEnd"))
					dateRelStr.setSelectedIndex(2);
				if (relPanelDateCbStr.equalsIgnoreCase("monthStart"))
					dateRelStr.setSelectedIndex(3);
				if (relPanelDateCbStr.equalsIgnoreCase("monthEnd"))
					dateRelStr.setSelectedIndex(4);
				if (relPanelDateCbStr.equalsIgnoreCase("yearStart"))
					dateRelStr.setSelectedIndex(5);
				if (relPanelDateCbStr.equalsIgnoreCase("yearEnd"))
					dateRelStr.setSelectedIndex(6);
				dateRelOfs.setValue(relPanelPlusMinusNum * (relPanelPlusMinus.equals("-") ? -1 : 1));
			} else {
				StringBuilder sbVal = new StringBuilder();
				Date d0;
				switch(dateRelStr.getSelectedIndex()){
				case 0://Today
					d0 = DateUtil.today();
					sbVal.append("@today()");
					break;
				case 1://Week Start
					d0 = DateUtil.weekStart(DateUtil.today());
					sbVal.append("@weekStart()");
					break;
				case 2://Week End;
					d0 = DateUtil.weekEnd(DateUtil.today());
					sbVal.append("@weekEnd()");
					break;
				case 3://Month Start;
					d0 = DateUtil.monthStart(DateUtil.today());
					sbVal.append("@monthStart()");
					break;
				case 4://Month End;
					d0 = DateUtil.monthEnd(DateUtil.today());
					sbVal.append("@monthEnd()");
					break;
				case 5://Year Start;
					d0 = DateUtil.yearStart(DateUtil.today());
					sbVal.append("@yearStart()");
					break;
				case 6://Year End;
					d0 = DateUtil.yearEnd(DateUtil.today());
					sbVal.append("@yearEnd()");
					break;
				default:
					d0 = DateUtil.today();
					sbVal.append("@today()");
				}
				int ofs = (int) ((d.getTime()-d0.getTime())/86400000);
				dateRelOfs.setValue(ofs);
				if (ofs != 0)
					sbVal.append((ofs >= 0 ? "+" : "") + ofs);
				if (type == TYPE_DATE)
					val = sbVal.toString();
			}
		}
		if (type == TYPE_DATETIME)
			caltmb.setValue(t);
		UniLog.log1("setvalue:" + val);
		setRealText(val);
	}
	/***
	 * constructor
	 */
	public ZkJxQueryInput()
	{
		super();
		setButtonVisible(false);
		addEventListener(Events.ON_OPEN, new EventListener<Event>() {
			public void onEvent(Event event) throws Exception {
				UniLog.log1("event:" + event.toString());
				if(isOpen()) {
					switch(type) {
					case TYPE_DATE:
					case TYPE_DATETIME:
						setDateValueToPopup(true, null);
						/*
						if (eventListenerCallback != null)
							eventListenerCallback.callback(event);
							*/
						break;
					case TYPE_STRING:
						UniLog.log("QueryInput String Popup Opened");
						if(stringListbox == null) {
							stringListbox = new Listbox();
							ZkUtil.appendSclass(stringListbox, "zkbi-queryinput-listbox");
							stringListbox.setWidth(StringUtils.defaultIfBlank((String)getAttribute("stringListboxWidth"), "500px"));
							setAttribute("stringListbox", stringListbox);
							stringPopup.appendChild(stringListbox);
							if (isMultiplePickSelect) {
								stringListbox.setMultiple(true);
								stringListbox.setCheckmark(true);
								stringPopupCloseButton.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
									@Override
									public void onEvent(Event event) throws Exception {
										UniLog.log1("event:%s, data:%s", event, event.getData());
										Boolean ignoreClose = null;
										if (event.getData() != null && event.getData() instanceof org.zkoss.json.JSONObject) {
											org.zkoss.json.JSONObject jo = (org.zkoss.json.JSONObject)event.getData();
											ignoreClose = (Boolean)jo.get("ignoreClose");
										}
										Map<Integer, String> strMap = new TreeMap<Integer, String>();
										for (Listitem li : stringListbox.getSelectedItems())
											strMap.put(li.getIndex(), (String)li.getValue());
										setQueryString(escapeStringList(new ArrayList<String>(strMap.values())));
										if (eventListenerCallback != null)
											eventListenerCallback.callback(event);
										if (ignoreClose == null || !ignoreClose)
											close();
									}
								});
								stringPopupClearButton.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
									@Override
									public void onEvent(Event event) throws Exception {
										stringListbox.clearSelection();
									}
								});
							}
							else {
								stringListbox.addEventListener(Events.ON_SELECT, 
									new EventListener<Event>() {
										@Override
										public void onEvent(Event event) throws Exception {
											UniLog.log1("event:%s", event);
											int idx = stringListbox.getSelectedIndex();
											if(idx >= 0) {
												setQueryString((String) stringListbox.getSelectedItem().getValue()) ;
												if (eventListenerCallback != null)
													eventListenerCallback.callback(event);
											}
											close();
										}
								});
							}
							List<String> splitStrList = null;
							Set<Integer> optFlagList = new HashSet<Integer>();
							if(gipi != null) {
								splitStrList = unescapeString(getRealText());
								int n = gipi.getRowCount();
								for(int i=0;i<n;i++) {
									Object o = gipi.getRow(i);
									String v = gipi.getString(o);
									String s;
									if (needCheckOptionListIndex && colOptionList != null && NumberUtils.isDigits(v)) {
										int vi = NumberUtils.toInt(v);
										if (vi >= 0 && vi < colOptionList.length) {
											s = colOptionList[vi];
											if (needUnionOptionList)
												optFlagList.add(vi);
										} else
											s = v;
									}
									else
										s = v;
									Listitem li = stringListbox.appendItem(s, s);
									if (isMultiplePickSelect) {
										if (splitStrList.contains(s))
											li.setSelected(true);
									}
								}
							}
							if (colOptionList != null && needUnionOptionList) {
								if (splitStrList == null)
									splitStrList = unescapeString(getRealText());
								for (String s : colOptionList) {
									if (optFlagList.contains(s))
										continue;
									Listitem li = stringListbox.appendItem(s, s);
									if (isMultiplePickSelect) {
										if (splitStrList.contains(s))
											li.setSelected(true);
									}
								}
							}
						}
						break;
					}
				} 
				else {
					UniLog.log("HAHA ZkJxQueryInput Popup Closed");
					if (stringPopupCloseButton != null)
						Events.sendEvent(Events.ON_CLICK, stringPopupCloseButton, new org.zkoss.json.JSONObject() {{ put("ignoreClose", true); }});
				}
			};
		});
		/*
	 	addEventListener(Events.ON_CHANGE,
	    		new EventListener() {
	    		    public void onEvent(Event event) throws Exception {
	    		    	UniLog.log("HAHA ZkJxQueryInput Value Changed");
	    		    	switch(type) {
	    		    	case TYPE_DATE:
	    		    		if(dateAbsRelFlag.getSelectedIndex() == 0) {
	    		    			try {
	    		    				Date d = DateUtil.getDate(getValue());
	    		    				cal.setValue(d);
	    		    			} catch (Exception ex) {
	    		    				UniLog.log("Not a Valid Date");
	    		    				cal.setValue(null);
	    		    			}
	    		    		}
	    		    		break;	
	    		    	}
	    		    };
	    		}
	    	);
		 */
	 	addEventListener(Events.ON_CHANGE, new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				if (eventListenerCallback != null)
					eventListenerCallback.callback(event);
			}
	 	});
	 	addEventListener(Events.ON_CTRL_KEY, new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				if (eventListenerCallback != null)
					eventListenerCallback.callback(event);
			}
	 	});
	}
	public int getQueryInputType() {
		return type;
	}
	public void setType(final int p_type, SessionHelper sh) {
		UniLog.log1("type %d", p_type);
		type = p_type;
		sessionHelper = sh;
		switch(p_type) {
		case TYPE_STRING: 
			break;
		case TYPE_DATE:
		case TYPE_DATETIME:
			if(bp == null) {
				bp = new Bandpopup();
				appendChild(bp);
			}
			if(datePopup == null) {
				datePopup = new Vlayout();
				bp.appendChild(datePopup);
				dateAbsRelFlag = new Radiogroup();
				dateAbsRelFlag.appendItem(ZkUtil.getSessionHelperLabel(sh, "Calendar Date"), "A");
				dateAbsRelFlag.appendItem(ZkUtil.getSessionHelperLabel(sh, "Relative Date"), "R");
				dateAbsRelFlag.setSelectedIndex(0);
				dateAbsRelFlag.addEventListener(Events.ON_CLICK,
						new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						if(dateAbsRelFlag.getSelectedIndex() == 0) {
							dateRelPanel.setVisible(false);
							cal.setVisible(true);
						} else  {
							dateRelPanel.setVisible(true);
							cal.setVisible(false);
						}
						if (caltmb != null)
							caltmb.setVisible(p_type == TYPE_DATETIME);
						setDateValueToPopup(false, null);
						if (eventListenerCallback != null)
							eventListenerCallback.callback(event);
					};
				}
						);
				datePopup.appendChild(dateAbsRelFlag);
				dateRelPanel = new Vlayout();
				dateRelPanel.setVisible(false);
				datePopup.appendChild(dateRelPanel);
				/*
				dateRelStr = new Combobox();
				dateRelStr.setWidth("150px");
				dateRelStr.appendItem("Today");

				 */
				dateRelStr = new Listbox();
				dateRelStr.setWidth("150px");
				dateRelStr.appendItem(ZkUtil.getSessionHelperLabel(sh, "Today"),"Today");
				dateRelStr.appendItem(ZkUtil.getSessionHelperLabel(sh, "Week Start"),"Week Start");
				dateRelStr.appendItem(ZkUtil.getSessionHelperLabel(sh, "Week End"),"Week End");
				dateRelStr.appendItem(ZkUtil.getSessionHelperLabel(sh, "Month Start"),"Month Start");
				dateRelStr.appendItem(ZkUtil.getSessionHelperLabel(sh, "Month End"),"Month End");
				dateRelStr.appendItem(ZkUtil.getSessionHelperLabel(sh, "Year Start"),"Year Start");
				dateRelStr.appendItem(ZkUtil.getSessionHelperLabel(sh, "Year End"),"Year End");
				dateRelStr.setSelectedIndex(0);
				dateRelStr.setMold("select");
				dateRelStr.addEventListener(Events.ON_SELECT, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						setDateValueFromPopup(null);
						if (eventListenerCallback != null)
							eventListenerCallback.callback(event);
					};
				});

				dateRelPanel.appendChild(
					new Hlayout() {{
						appendChild(new Label(ZkUtil.getSessionHelperLabel(sh, "Option")) {{ setStyle("display:inline-block;width:70px"); }});
						appendChild(dateRelStr);
				}});
				dateRelOfs = new Spinner();
				dateRelOfs.setValue(0);
				//dateRelOfs.setFormat("+0;-#");
				dateRelOfs.setConstraint("no empty");
				dateRelOfs.setWidth("75px");
				dateRelOfs.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						UniLog.log("dateRelOfs Change " + dateRelOfs.getValue());
						setDateValueFromPopup(null);
						if (eventListenerCallback != null)
							eventListenerCallback.callback(event);
					};
				}
						);
				dateRelOfs.addEventListener(Events.ON_CHANGING, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						UniLog.log("dateRelOfs Changing " + ((InputEvent)event).getValue() + ":" + dateRelOfs.getVflex());
						setDateValueFromPopup(((InputEvent)event).getValue());
						if (eventListenerCallback != null)
							eventListenerCallback.callback(event);
					};
				}
						);
				/*
				dateRelOfs.addEventListener(Events.ON_CLICK,
					new EventListener() {
						public void onEvent(Event event) throws Exception {
							setDateValueFromPopup();
						};
					}
				);
				 */
				dateRelPanel.appendChild(
					new Hlayout() {{
						appendChild(new Label(ZkUtil.getSessionHelperLabel(sh, "Day offset")) {{ setStyle("display:inline-block;width:70px"); }});
						appendChild(dateRelOfs);
				}});
				cal = new Calendar();
				//cal.setShowTodayLink(true);
				cal.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
					public void onEvent(Event event) throws Exception {
						UniLog.log("HAHA Calander Changed");
						setDateValueFromPopup(null);
						/*if (bp != null && bp.getParent() instanceof Bandbox && ((Bandbox)bp.getParent()).isOpen()){ //change to javascript control
							((Bandbox)bp.getParent()).setOpen(false);
						}*/
						if (eventListenerCallback != null)
							eventListenerCallback.callback(event);
					};
				});
				datePopup.appendChild(cal);
				if (p_type == TYPE_DATETIME) {
					caltmb = new Timebox();
					caltmb.setFormat("HH:mm:ss");
					caltmb.setWidth("80%");
					caltmb.addEventListener(Events.ON_CHANGING, new EventListener<InputEvent>(){
						@Override
						public void onEvent(InputEvent event) throws Exception {
							UniLog.log("HAHA timebox Changing " + event.getValue());
							setDateValueFromPopup(null, event.getValue());
							if (eventListenerCallback != null)
								eventListenerCallback.callback(event);
						}
					});
					caltmb.addEventListener(Events.ON_CHANGE, new EventListener<InputEvent>(){
						@Override
						public void onEvent(InputEvent event) throws Exception {
							UniLog.log("HAHA timebox Changed " + event.getValue());
							setDateValueFromPopup(null);
							if (eventListenerCallback != null)
								eventListenerCallback.callback(event);
						}
					});
					datePopup.appendChild(caltmb);
				}
			}
			setButtonVisible(true);
			break;
		case TYPE_FLOAT:
			break;
		case TYPE_BOOLEAN:
			break;
		case TYPE_INTEGER:
			break;
		default :
		}
	}

	public void setGiPi(AbstractGetItemProperty p_gipi) {
		setGiPi(p_gipi, null, false, false, false);
	}

	public void setGiPi(AbstractGetItemProperty p_gipi, String[] p_colOptionList, boolean isMultiPickSelect, boolean needCheckOpList, boolean needUnionOptList) {
		gipi = p_gipi;
		colOptionList = p_colOptionList;
		isMultiplePickSelect = isMultiPickSelect;
		needCheckOptionListIndex = needCheckOpList;
		needUnionOptionList = needUnionOptList;
		if(gipi != null || (p_colOptionList != null && needUnionOptionList)) {
			if(bp == null) {
				bp = new Bandpopup();
				bp.setCtrlKeys("^d^e^f");
				appendChild(bp);
			}
			if(stringPopup == null) {
				stringPopup = new Vlayout();
				if (isMultiPickSelect) {
					stringPopupClearButton = new Button("Clear");
				    stringPopupClearButton.setIconSclass("z-icon-times");
					stringPopupCloseButton = new Button("Ok");
				    stringPopupCloseButton.setIconSclass("z-icon-check");
					bp.appendChild(new Div() {{
						appendChild(
							new Vlayout() {{
								appendChild(stringPopup);
								appendChild(new Hbox() {{
									appendChild(stringPopupCloseButton);
									appendChild(stringPopupClearButton);
									setHflex("1");
									setPack("center");
								}});
								setVflex("1");
							}}
						);
						setHeight("330px");
					}});
					stringPopup.setVflex("1");
					stringPopup.setStyle("overflow-y:auto");
				}
				else
					bp.appendChild(stringPopup);
			}
			setButtonVisible(true);
		}
	}
	
	public void clearStringListboxSelection() {
		if (stringListbox != null)
			stringListbox.clearSelection();
	}
	
	public static String escapeStringList(List<String> strList) {
		Pattern regex = Pattern.compile("(\\\\*)(,|$)");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < strList.size(); i++) {
			String str = strList.get(i);
			if (sb.length() > 0)
				sb.append(",");
			/*String s = str.replace(",", "\\,");
			sb.append(s);
			if (i < strList.size() - 1 && s.endsWith("\\"))
				sb.append("\\");*/
			StringBuffer sbf = new StringBuffer();
			Matcher m = regex.matcher(str);
			while (m.find()) {
				m.appendReplacement(sbf, "");
				if (!m.group(1).isEmpty() && (m.group(2).equals(",") || i < strList.size() - 1))
					sbf.append(StringUtils.repeat('\\', m.group(1).length() * 2));
				else
					sbf.append(StringUtils.repeat('\\', m.group(1).length()));
				if (m.group(2).equals(","))
					sbf.append("\\,");
			}
			m.appendTail(sbf);
			sb.append(sbf);
		}
		return sb.toString();
	}
	
	public static List<String> unescapeString(String str) {
		List<String> sl = new ArrayList<String>();
		/*String[] ss = str.split(",");
		for (int i = 0; i < ss.length; i++) {
			if (i < ss.length - 1 && ss[i].endsWith("\\\\"))
				sl.add(ss[i].substring(0, ss[i].length() - 1));
			else if (i < ss.length - 1 && ss[i].endsWith("\\"))
				sl.add(ss[i].substring(0, ss[i].length() - 1) + ",");
			else
				sl.add(ss[i]);
		}*/
		Pattern regex = Pattern.compile("(\\\\*),");
		Matcher m = regex.matcher(str);
		while (m.find()) {
			StringBuffer sbf = new StringBuffer();
			m.appendReplacement(sbf, "");
			if (!m.group(1).isEmpty()) {
				sbf.append(StringUtils.repeat("\\", m.group(1).length() / 2));
				if (m.group(1).length() % 2 == 1)
					sbf.append(",");
			}
			sl.add(sbf.toString());
		}
		StringBuffer sbf = new StringBuffer();
		m.appendTail(sbf);
		sl.add(sbf.toString());

		for (int i = 0; i < sl.size() - 1; i++) {
			if (sl.get(i).endsWith(",")) {
				StringBuilder sb = new StringBuilder(sl.get(i));
				do {
					sb.append(sl.get(i + 1));
					sl.remove(i + 1);
				} while(i + 1 < sl.size() && sb.toString().endsWith(","));
				sl.set(i, sb.toString());
			}
		}
		return sl;
	}
	
	public void useReadonlyTextMode() {
		readonlyTextMode = true;
		setReadonly(true);
	}
	
	private void setRealText(String value) {
		if (readonlyTextMode) {
			setAttribute("realText", value);
			String s = StringUtils.defaultString(value);
			Map<String, String> m = MapUtil.of("today", "Today", "weekStart", "Week Start", "weekEnd", "Week End", "monthStart", "Month Start", "monthEnd", "Month End", "yearStart", "Year Start", "yearEnd", "Year End");
			for (Map.Entry<String, String> entry : m.entrySet())
				s = s.replace("@" + entry.getKey() + "()", ZkUtil.getSessionHelperLabel(sessionHelper, entry.getValue()));
			setText(s);
		} else
			setText(value);
	}

	private String getRealText() {
		if (readonlyTextMode)
			return (String)getAttribute("realText");
		else
			return getText();
	}
}
