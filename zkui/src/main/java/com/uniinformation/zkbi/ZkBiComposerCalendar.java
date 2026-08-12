package com.uniinformation.zkbi;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.ZkBiAuService.ZkBiAuEvent;

public class ZkBiComposerCalendar extends ZkComposerBase{
	final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		//Selectors.wireComponents(p_comp, this, false);  //important for wire variable
		super.doAfterCompose(p_comp);
		if (!accessOkFlag) {
			return;
		}
		
		UniLog.log1("called");
		
						
		//TODO read BiCalendar record and construct the json
		/*JSONObject json = new JSONObject() {{  
			put("events", new JSONArray() {{
				put( new JSONObject() {{ put("title", "Event 1"); put("start", "2020-06-12"); put("color", "red"); put("textColor", "black"); }});
				put( new JSONObject() {{ put("title", "Event 2"); put("start", "2020-06-13"); put("color", "green"); put("textColor", "black"); }});
				put( new JSONObject() {{ put("title", "Event 3"); put("start", "2020-06-14"); put("color", "blue"); put("textColor", "black"); }});
			}});
			this.put("opt", "");
		}};*/
		JSONObject json = new JSONObject();
		JSONArray jsonEvents = new JSONArray();
		json.put("opt", "");
		json.put("events", jsonEvents);
		final SessionHelper sh = ZkSessionHelper.getSessionHelper();
		BiResult biResult = null;
		BiSchema schema = (BiSchema) sh.getSessionData("biSchema");
		if (schema == null)
			schema = BiSchema.loadSchema(sh);
		final BiView biView = schema.getViewByName("erpv4.ZkBiCalendar");
		try {
			if (biView != null) {
				UniLog.log1("load view ZkBiCalendar loginid:%s", sh.getLoginId());
				biResult = biView.newBiResult(sh.getLoginId(), null, null, sh);
				ReturnMsg rtnMsg = biResult.addCustomCondition(String.format("zbcal_owner = '%s' or zbcal_ispublic = 'Y'", sh.getLoginId()));
				if (rtnMsg.getStatus()) {
					if (biResult.query(true).getStatus()) {
						UniLog.log1("getrowcount:%d", biResult.getRowCount());
						for (int i = 0; i < biResult.getRowCount(); i++) {
							biResult.loadOneRecV(i);
							UniLog.log1("title:%s,allday:%b,start:%s,end:%s,textcolor:%s,bkcolor:%s", 
									biResult.getCell("zbcal_title").getString(), 
									biResult.getCell("zbcal_ispublic").getBoolean(),
									biResult.getCell("zbcal_wholeday").getBoolean(),
									df.format(biResult.getCell("zbcal_starttime").getDate()), df.format(biResult.getCell("zbcal_endtime").getDate()),
									biResult.getCell("zbcal_textcolor").getString(), biResult.getCell("zbcal_bkcolor").getString());

							String owner = biResult.getCell("zbcal_owner").getString();
							JSONObject jsonEvent = new JSONObject();
							jsonEvent.put("rg", biResult.getCell("zbcal_rg").getInt());
							jsonEvent.put("owner", owner);
							jsonEvent.put("title", biResult.getCell("zbcal_title").getString());
							jsonEvent.put("allDay", biResult.getCell("zbcal_wholeday").getBoolean());
							jsonEvent.put("start", df.format(biResult.getCell("zbcal_starttime").getDate()));
							jsonEvent.put("end", df.format(biResult.getCell("zbcal_endtime").getDate()));
							jsonEvent.put("textColor", biResult.getCell("zbcal_textcolor").getString());
							jsonEvent.put("color", biResult.getCell("zbcal_bkcolor").getString());
							jsonEvent.put("isPublic", biResult.getCell("zbcal_ispublic").getBoolean());
							jsonEvent.put("editable", owner.equals(sh.getLoginId()));
							jsonEvents.put(jsonEvent);
						}
					}
				} else
					UniLog.log1("add condition failed: %s", rtnMsg);
			} else
				UniLog.log1("Invalid view erpv4.ZkBiCalendar");
		} 
		catch (Exception e) {
			e.printStackTrace();
		} 
		finally {
			if (biResult != null)
				biResult.close();
		}

		ZkUtil.js("createCalendar('calendar001',%s,'%s')", json.toString(), sh.getLoginId());
		UniLog.log1("json:" + json.toString(3));
		
		addEventListener("onCalEvent", new EventListener<Event>() {
			@Override
			public void onEvent(Event p_event) {
				UniLog.log1("haha called: name:%s data:%s", p_event.getName(), p_event.getData());
				//TODO add/update/delete db
				if (biView == null)
					return;
				if (p_event.getData() == null)
					return;
				BiResult biResult = null;
				try {
					JSONObject json = new JSONObject(p_event.getData().toString()); 
					String action = json.getString("action");
					int rg = json.getInt("rg");
					String title = json.getString("title");
					String startStr = json.optString("start");
					String endStr = json.optString("end");
					boolean allDay = json.optBoolean("allDay");
					String textColor = json.optString("textColor");
					String backgroundColor = json.optString("color");
					Date startTime = startStr.isEmpty() ? null : parseDate(startStr);
					Date endTime = endStr.isEmpty() ? null : parseDate(endStr);
					boolean isPublic = json.optBoolean("isPublic");

					ReturnMsg rtnMsg;
					if (action.equals("add")) {
						UniLog.log("onCalEvent add");
						biResult = biView.newBiResult(sh.getLoginId(), null, null, sh);
						biResult.clearCurrentRec();
						biResult.getCell("zbcal_owner").set(sh.getLoginId());
						biResult.getCell("zbcal_title").set(title);
						biResult.getCell("zbcal_wholeday").set(allDay);
						biResult.getCell("zbcal_starttime").set(startTime);
						biResult.getCell("zbcal_endtime").set(endTime);
						biResult.getCell("zbcal_textcolor").set(textColor);
						biResult.getCell("zbcal_bkcolor").set(backgroundColor);
						biResult.getCell("zbcal_ispublic").set(isPublic);
						rtnMsg = biResult.addCurrent();
						if (rtnMsg != null && !rtnMsg.getStatus())
							UniLog.log("addCurrent errMsg:" + rtnMsg.getMsg());
						else {
							int newRg = biResult.getCell("zbcal_rg").getInt();
							UniLog.log1("addCurrent newRg:%d", newRg);
							json.put("rg", newRg);
							json.remove("action");
							ZkUtil.js("addCalendarEvent(%s)", json.toString());
						}
					}
					else if (action.equals("update")) {
						UniLog.log("onCalEvent update");
						if (rg > 0) {
							biResult = biView.newBiResult(sh.getLoginId(), null, null, sh);
							rtnMsg = biResult.addCustomCondition(String.format("zbcal_owner = '%s' and zbcal_rg = %d", sh.getLoginId(), rg));
							if (rtnMsg.getStatus()) {
								if (biResult.query(true).getStatus() && biResult.getRowCount() > 0) {
									biResult.fetchOneRecV(0);
									biResult.getCell("zbcal_title").set(title);
									biResult.getCell("zbcal_wholeday").set(allDay);
									biResult.getCell("zbcal_starttime").set(startTime);
									biResult.getCell("zbcal_endtime").set(endTime);
									biResult.getCell("zbcal_textcolor").set(textColor);
									biResult.getCell("zbcal_bkcolor").set(backgroundColor);
									biResult.getCell("zbcal_ispublic").set(isPublic);
									rtnMsg = biResult.updateCurrent();
									if (rtnMsg != null && !rtnMsg.getStatus())
										UniLog.log("updateCurrent errMsg:" + rtnMsg.getMsg());
								}
							} else
								UniLog.log1("add condition failed: %s", rtnMsg);
						} else
							UniLog.log1("rg = %d", rg);
					}
					else if (action.equals("drop") || action.equals("resize")) {
						UniLog.log1("onCalEvent %s", action);
						if (rg > 0) {
							biResult = biView.newBiResult(sh.getLoginId(), null, null, sh);
							rtnMsg = biResult.addCustomCondition(String.format("zbcal_owner = '%s' and zbcal_rg = %d", sh.getLoginId(), rg));
							if (rtnMsg.getStatus()) {
								if (biResult.query(true).getStatus() && biResult.getRowCount() > 0) {
									biResult.fetchOneRecV(0);
									biResult.getCell("zbcal_starttime").set(startTime);
									biResult.getCell("zbcal_endtime").set(endTime);
									rtnMsg = biResult.updateCurrent();
									if (rtnMsg != null && !rtnMsg.getStatus())
										UniLog.log("updateCurrent errMsg:" + rtnMsg.getMsg());
								}
							} else
								UniLog.log1("add condition failed: %s", rtnMsg);
						} else
							UniLog.log1("rg = %d", rg);
					}
					else if (action.equals("delete")) {
						UniLog.log("onCalEvent delete");
						if (rg > 0) {
							biResult = biView.newBiResult(sh.getLoginId(), null, null, sh);
							rtnMsg = biResult.addCustomCondition(String.format("zbcal_owner = '%s' and zbcal_rg = %d", sh.getLoginId(), rg));
							if (rtnMsg.getStatus()) {
								if (biResult.query(true).getStatus() && biResult.getRowCount() > 0) {
									for (int i = 0; i < biResult.getRowCount(); i++) {
										biResult.fetchOneRecV(i);
										Object o = biResult.getTrStatObj(i);
										biResult.markDelete(o, true);
									}
									rtnMsg = biResult.batchAddUpdateDelete();
									if (rtnMsg != null && !rtnMsg.getStatus())
										UniLog.log("deleteCurrent errMsg:" + rtnMsg.getMsg());
								}
							} else
								UniLog.log1("add condition failed: %s", rtnMsg);
						} else
							UniLog.log1("rg = %d", rg);
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (biResult != null)
						biResult.close();
				}
			}
		});		
		
	}
	@Override
	protected int adjustRootCompWidthOffset() {
		return 0;
	}
	protected boolean adjustRootCompWidth() {
		return false;
	}
	private Date parseDate(String dateStr) {
		try {
			return df.parse(dateStr);
		}
		catch (Exception e) {
			return null;
		}
	}
}
