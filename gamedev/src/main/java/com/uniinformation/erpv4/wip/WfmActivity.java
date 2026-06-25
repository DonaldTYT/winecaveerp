package com.uniinformation.erpv4.wip;

import java.util.Date;
import java.util.Iterator;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.Base64Util;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;

public class WfmActivity {
	
	public static final int WFMACTIVITY_TYPE_STATECHANGE = 0;
	public static final int WFMACTIVITY_TYPE_MESSAGE     = 1;
	BiResult actBr;
	Listbox listbox;
	ListModelList listModelList;
	Textbox wfmMessage;
	String loginId;
	HtmlBasedComponent printMessageContent(final SessionHelper p_sh, final int p_rg, final int p_frg, final int p_srg,
										final String p_user,final String p_lastUser,final Date p_time,final String p_content,final String p_sclass, final int listIdx) {
		//UniLog.log1("user:%s, lastuser:%s, rg:%d, frg:%d, srg:%d, content:%s", p_user, p_lastUser, p_rg, p_frg, p_srg, p_content);
		/*Vlayout vl = new Vlayout() ;
		vl.setHflex("min");
		Hlayout hl = new Hlayout();
		hl.setHflex("min");
		Label lb;
		hl.setParent(vl);		
		lb = new Label();
		lb.setSclass("myWfmMsgHdr");
		lb.setValue(p_user);
		lb.setParent(hl);
		Div dv = new Div();
		dv.setHflex("min");
		dv.setParent(hl);
		lb = new Label();
		lb.setSclass("myWfmMsgHdr");
		lb.setValue(p_time);
		lb.setParent(hl);
		lb = new Label();
		lb.setMultiline(true);
		lb.setValue(p_content);
//		lb.setHflex("min");
		lb.setParent(vl);
		return(vl);*/
		final Div div = new Div();
		div.setSclass(p_sclass);
		div.setAttribute("rg", p_rg);
		div.setAttribute("frg", p_frg);
		div.setAttribute("srg", p_srg);
		if (StringUtils.isNotBlank(p_user) && !StringUtils.equals(p_user, p_lastUser)) {
			div.appendChild(new Div() {{
				setSclass("myWfmMsgHdrDiv user");
				Label lb = new Label();
				lb.setSclass("myWfmMsgHdr");
				lb.setValue(p_user);
				appendChild(lb);
			}});
		}
		final String[] displayContents = new String[] {""};
		div.appendChild(new Div() {{
			setSclass("myWfmMsgHdrDiv");
			Label lb = null;
			//When message content with such special prefix "FILING://", show a file download link instead of the filing key.
			if (StringUtils.startsWith(p_content, "FILING://")) {
				if (StringUtils.startsWith(p_content, "FILING://zkbi_wfmact_") && p_content.length() > 21) {
					try {
						String b = p_content.substring(32);
						//final String flKey = p_content.substring(9);
						final String flKey = p_content.substring(9,31); //andrew231101 remove filename from filing key
						final String flName = Base64Util.decode(b);
						//UniLog.log1("DEBUG:flkey:%s flName:%s", flKey, flName);
						A a = new A();
						a.setLabel(flName);
						a.setSclass("myWfmMsgHdr");
						a.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
							@Override
							public void onZkBiEvent(Event event) throws Exception {
								UniLog.log1("a:%s", event);
								if (ZkUtil.downloadFileFromFiling(ZkSessionHelper.getSessionHelper(), flKey, flName))
									ZkUtil.showMsg("Download is in progress");
								else
									ZkUtil.showErrMsg("File not found");
							}
						});
						appendChild(a);
						displayContents[0] = flName;
					}
					catch (Exception e) {
						UniLog.log(e);
						lb = new Label();
						lb.setValue("");
					}
				} else {
					lb = new Label();
					lb.setValue("");
				}
			} else {
				lb = new Label();
				lb.setValue(p_content);
				displayContents[0] = p_content;
			}
			if (lb != null) {
				lb.setSclass("myWfmMsgHdr");
				lb.setPre(true);
				appendChild(lb);
			}
		}});
		div.appendChild(new Div() {{
			setSclass("myWfmMsgHdrDiv time");
			Label lb = new Label();
			lb.setSclass("myWfmMsgHdr");
			lb.setValue(DateUtil.dateToDateTimeStr(p_time, "yy/MM/dd HH:mm"));
			appendChild(lb);
		}});
		if (!StringUtils.equals(p_user, p_lastUser)) {
			div.appendChild(new Div() {{
				setSclass("bubble-arrow");
			}});
		}

		if (p_sh.getAllowWfmContextMenu()) {
			Menupopup contextMenu = new Menupopup();
			div.setContext(contextMenu);
			contextMenu.setParent(div.getRoot());
			Menuitem menuitem;
			//system msg not allowed to update/delete.
			//admin can update/delete any user created msg. No time restriction.
			//non-admin user can update/delete his own msg within 1 hour
			if (!StringUtils.equals(p_user, "System Message")
				&& (p_sh.isAdminUser() 
					|| (System.currentTimeMillis() - p_time.getTime() < 3600000 && StringUtils.isNotBlank(p_user) && StringUtils.equals(p_user, p_sh.getLoginId())))) {
				menuitem = new Menuitem(p_sh.getLabel("Delete"));
				menuitem.setIconSclass("z-icon-trash");
				contextMenu.appendChild(menuitem);
				menuitem.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						final Integer rg = (Integer)div.getAttribute("rg");
						final Integer frg = (Integer)div.getAttribute("frg");
						final Integer srg = (Integer)div.getAttribute("srg");
						UniLog.log1("event:%s, rg:%d, frg:%d, srg:%d", event, rg, frg, srg);
						ZkBiMsgboxButton[] btns = new ZkBiMsgboxButton[] {new ZkBiMsgboxButton(p_sh.getBtLabel("Ok")),new ZkBiMsgboxButton(p_sh.getBtLabel("Cancel"))};
						ZkBiMsgbox.show(String.format("Delete message '%s'?", displayContents[0]), btns, new ZkBiEventListener<Event>() {
							@Override
							public void onZkBiEvent(Event event) throws Exception {
								ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
								UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
								if (StringUtils.equals(btn.getName(), p_sh.getBtLabel("Ok"))) {
									try {
										/*actBr.getSelectUtil().executeUpdate("delete from wfmactivity where wfmact_rg = ?", new Wherecl().appendArgument(rg));
										if (p_content.startsWith("FILING://")) {
											//String flKey = String.format("zkbi_wfmact_%010d_%s", rg, Base64Util.encode(displayContents[0]));
											String flKey = String.format("zkbi_wfmact_%010d", rg); //andrew231101 remove filename from filing key
											FilingUtil.deleteFile(p_sh.getAgent(), null, flKey);
										}*/
										Object o = actBr.getTrStatObj(listIdx);
										actBr.markDelete(o, true);
										ReturnMsg rtnMsg = actBr.batchAddUpdateDelete();
										if (rtnMsg != null && !rtnMsg.getStatus())
											throw rtnMsg.getEx();
										ZkUtil.showMsg("Message deleted");
									} catch (Exception e) {
										UniLog.log(e);
										ZkUtil.errMsg(StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
									}
									WfmEventData wed = new WfmEventData(frg);
									wed.setMessageAdded(true);
									EventQueue<Event> que = EventQueues.lookup("WipNotify", EventQueues.APPLICATION, true);
									que.publish(new Event("onWipNotify", null, wed));
									//reloadActivity(frg, srg);
								}
							}
						});
					}
				});
				if (!p_content.startsWith("FILING://")) {
					menuitem = new Menuitem(p_sh.getLabel("Update"));
					menuitem.setIconSclass("z-icon-pencil");
					contextMenu.appendChild(menuitem);
					menuitem.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							final Integer rg = (Integer)div.getAttribute("rg");
							final Integer frg = (Integer)div.getAttribute("frg");
							final Integer srg = (Integer)div.getAttribute("srg");
							UniLog.log1("event:%s, rg:%d, frg:%d, srg:%d", event, rg, frg, srg);
							ZkBiMsgboxButton[] btns = new ZkBiMsgboxButton[] {new ZkBiMsgboxButton(p_sh.getBtLabel("Ok")),new ZkBiMsgboxButton(p_sh.getBtLabel("Cancel"))};
							final Textbox tb = new Textbox();
							tb.setMultiline(true);
							tb.setRows(3);
							tb.setWidth("100%");
							tb.setText(p_content);
							new ZkBiMsgbox(p_sh).setContent(tb).setButtons(btns).setEventListener(new ZkBiEventListener<Event>() {
								@Override
								public void onZkBiEvent(Event event) throws Exception {
									String text = tb.getText();
									ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
									UniLog.log1("event:%s, text:%s, btn:%s", event, text, btn.getName());
									if (btn.getName().equals(p_sh.getLabel("Ok"))) {
										if (StringUtils.isBlank(text))
											ZkUtil.showErrMsg("Message content cannot be empty");
										else if (text.startsWith("FILING://"))
											ZkUtil.showErrMsg("Invalid message content");
										else {
											try {
												actBr.getSelectUtil().executeUpdate("update wfmactivity set wfmact_content = ? where wfmact_rg = ?", new Wherecl().appendArgument(text).appendArgument(rg));
												ZkUtil.showMsg("Message updated");
											} catch (Exception e) {
												UniLog.log(e);
												ZkUtil.errMsg(StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
											}
											WfmEventData wed = new WfmEventData(frg);
											wed.setMessageAdded(true);
											EventQueue<Event> que = EventQueues.lookup("WipNotify", EventQueues.APPLICATION, true);
											que.publish(new Event("onWipNotify", null, wed));
											//reloadActivity(frg, srg);
										}
									}
								}
							}).build().doModal();
						}
					});
				}
			}
			menuitem = new Menuitem(p_sh.getLabel("Copy"));
			menuitem.setIconSclass("z-icon-copy");
			contextMenu.appendChild(menuitem);
			menuitem.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					ZkUtil.js("copyToClipboard('%s'); $.notify(\"Data copied\", { className: \"info\", globalPosition:\"bottom right\", autoHideDelay: 5000 });", StringEscapeUtils.escapeJavaScript(displayContents[0]));
				}
			});
		}

		Div divr = new Div();
		divr.setStyle("display:flex");
		divr.appendChild(div);
		return divr;
	}
	public WfmActivity(SelectUtil p_su,final SessionHelper p_sh,Listbox p_listBox,Textbox p_wfmmessage) throws Exception {
			BiSchema schema = BiSchema.loadSchema(p_sh);
			actBr = schema.getViewByName("wip.WfmActivity").newBiResult(p_su,p_sh.getLoginId(), null, null, p_sh);
			actBr.setUseTransaction(false);
			loginId = p_sh.getLoginId();
			wfmMessage = p_wfmmessage;
			if(p_listBox != null) {
				listbox = p_listBox;
				listModelList = new ListModelList();
				//p_listBox.setOddRowSclass("noclass");
				//p_listBox.setSclass("zkbi-linkcomp-listbox");
				p_listBox.setSclass("zkbi-whitebg-listbox");
				p_listBox.setModel(listModelList);
				p_listBox.setItemRenderer(
					new ListitemRenderer <Object> () {
						@Override
						public void render(Listitem arg0, Object trStat, int p_idx) throws Exception {
							int idx = listModelList.indexOf(trStat);
							actBr.loadOneRecV(idx);
							
							Listcell lc = new Listcell();
							lc.setParent(arg0);
//							lc.setSclass("float_center");
							Div dv = new Div();
							dv.setHflex("min");
							dv.setVflex("min");
							HtmlBasedComponent content;
//							if(actBr.getCellInt("wfmact_type") == WFMACTIVITY_TYPE_STATECHANGE) {
							String user;
							String lastUser = (String)listbox.getAttribute("lastPrintUser");
							if(actBr.getCell("wfmact_auser").getString().equals("cron")) {
								user = "System Message";
								content = printMessageContent(
//											actBr.getCell("wfmact_auser").getColumnDisplayString(),
											p_sh, actBr.getCellInt("wfmact_rg"), actBr.getCellInt("wfmact_frg"), actBr.getCellInt("wfmact_srg"),
											user, lastUser,
											actBr.getCellDate("wfmact_atime"),
											//DateUtil.dateToDateTimeStr(actBr.getCellDate("wfmact_atime"), "yy/MM/dd HH:mm"),
											actBr.getCell("wfmact_content").getString(),
											"myalign_left", idx
											);
								content.setParent(dv);
							} else {
								if(actBr.getCell("wfmact_auser").getString().equals(loginId)) {
									user = loginId;
									content = printMessageContent(
											p_sh, actBr.getCellInt("wfmact_rg"), actBr.getCellInt("wfmact_frg"), actBr.getCellInt("wfmact_srg"),
											loginId, lastUser,
											actBr.getCellDate("wfmact_atime"),
											actBr.getCell("wfmact_content").getString(),
											"myalign_right", idx
											);
									content.setParent(dv);
								} else {
									user = actBr.getCell("wfmact_auser").getColumnDisplayString();
									content = printMessageContent(
											p_sh, actBr.getCellInt("wfmact_rg"), actBr.getCellInt("wfmact_frg"), actBr.getCellInt("wfmact_srg"),
											user, lastUser,
											actBr.getCellDate("wfmact_atime"),
											actBr.getCell("wfmact_content").getString(),
											"myalign_left", idx
											);
									content.setParent(dv);
								}
								
								/*
								Label lb = new Label();
								lb.setValue(actBr.getCellString("wfmact_content"));
								if(actBr.getCell("wfmact_auser").getString().equals(loginId)) {
									dv.setClass("myalign_right");
								} else {
									dv.setClass("myalign_left");
								}
								lb.setParent(dv);
								*/
							}
							listbox.setAttribute("lastPrintUser", user);
							
							dv.setWidth("80%");
							dv.setParent(lc);
							
						}	
					}
				);	
			}
	}

	public int reloadActivity(int p_frg,int p_srg) throws CellException {
		UniLog.log1("called frg:%d srg:%d", p_frg, p_srg);
		actBr.clear();
		actBr.clearCondition();
		actBr.addCustomCondition("wfmact_frg = " + p_frg);
		if(p_srg > 0) {
			actBr.addCustomCondition("(wfmact_srg = " + p_srg + " or wfmact_srg = 0)");
		}
		actBr.query();
		if(listModelList != null) {
			listModelList.clear();
			listModelList.addAll(actBr.getResultStat());
			listbox.removeAttribute("lastPrintUser");

			if (!listModelList.isEmpty()) {
				listbox.renderAll();
				//listbox.scrollToIndex(listModelList.getSize() - 1); //When the browser is not in full height, click on a task will trigger a scrollbar bug.
				int n = listModelList.getSize();
				Listitem li = listbox.getItemAtIndex(n - 1);
				Clients.scrollIntoView(li);
			}

			Iterator<EventListener<?>> it = listbox.getEventListeners("onResizeView").iterator();
			while (it.hasNext())
				listbox.removeEventListener("onResizeView", it.next());
			listbox.addEventListener("onResizeView", new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					UniLog.log1("onResizeView %s", event);
					Clients.resize(listbox);
					if (!listModelList.isEmpty()) {
						listbox.renderAll();
						int n = listModelList.getSize();
						Listitem li = listbox.getItemAtIndex(n - 1);
						Clients.scrollIntoView(li);
					}
				}
			});
			ZkUtil.delayPostEvent("onResizeView", listbox, null, 500);
		}
		if (wfmMessage != null)
			wfmMessage.setText("");
		return(actBr.getRowCount());
	}
	
	public int newRg() {
		return actBr.getView().getSchema().getRg(actBr, "", 52025);
	}
	
	public void logActivty(String p_user,int p_rg,int p_frg,int p_srg,int p_type,String p_content, int p_messid,String p_messtype,
				Date p_odeadline, Date p_ndeadline,
				int p_ostate,int p_nstate,
				Date p_osttime,Date p_nsttime, 
				Date p_oendtime,Date p_nendtime,
				String p_ostby,String p_nstby,
				String p_oendby,String p_nendby,
				String p_oassignto,String p_nassignto
				) throws CellException {
		
		actBr.clearCurrentRec();
		actBr.getCell("wfmact_rg").set(p_rg);
		actBr.getCell("wfmact_frg").set(p_frg);
		actBr.getCell("wfmact_srg").set(p_srg);
		actBr.getCell("wfmact_type").set(p_type);
		actBr.getCell("wfmact_auser").set(p_user);
		actBr.getCell("wfmact_atime").set(new Date());
		actBr.getCell("wfmact_content").set(p_content);
		actBr.getCell("wfmact_msgid").set(p_messid);
		actBr.getCell("wfmact_msgtype").set(p_messtype);

		actBr.getCell("wfmact_odeadline").set(p_odeadline);
		actBr.getCell("wfmact_ostate").set(p_ostate);
		actBr.getCell("wfmact_ostarttime").set(p_osttime);
		actBr.getCell("wfmact_oendtime").set(p_oendtime);
		actBr.getCell("wfmact_ostartby").set(p_ostby);
		actBr.getCell("wfmact_oendby").set(p_oendby);
		actBr.getCell("wfmact_oassignto").set(p_oassignto);

		actBr.getCell("wfmact_ndeadline").set(p_ndeadline);
		actBr.getCell("wfmact_nstate").set(p_nstate);
		actBr.getCell("wfmact_nstarttime").set(p_nsttime);
		actBr.getCell("wfmact_nendtime").set(p_nendtime);
		actBr.getCell("wfmact_nstartby").set(p_nstby);
		actBr.getCell("wfmact_nendby").set(p_nendby);
		actBr.getCell("wfmact_nassignto").set(p_nassignto);
		
		ReturnMsg rtn = actBr.addCurrent();
		if(rtn != null && !rtn.getStatus()) {
			throw new CellException ("Error Inserting WfmActivity");
		}
	}
}
