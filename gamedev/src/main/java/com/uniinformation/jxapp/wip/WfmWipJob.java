package com.uniinformation.jxapp.wip;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.OpenEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Panel;
import org.zkoss.zul.Splitter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellCollectionToJsonInterface;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.wip.WfmEventData;
import com.uniinformation.erpv4.wip.WfmJob;
import com.uniinformation.erpv4.wip.WfmStep;
import com.uniinformation.erpv4.wip.WfmTask;
import com.uniinformation.erpv4.wip.WfmTaskBase;
import com.uniinformation.erpv4.wip.WfmTaskUpdate;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.QRCodeUtil;
import com.uniinformation.utils.URLParamHash;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.wip.WipFlow;
import com.uniinformation.wip.WipJob;
import com.uniinformation.wip.WipStep;
import com.uniinformation.wip.WipTask;
import com.uniinformation.wip.WipVisNetwork;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkf.ZkForm;

public class WfmWipJob extends WfmWipFlow{
	/*
	static final int NODE_PROPERTY_STATE_NONE = 0;
	static final int NODE_PROPERTY_STATE_START= 1;
	static final int NODE_PROPERTY_STATE_END  = 2;
	static final int NODE_PROPERTY_STATE_NODE = 3;
	int nodeProperyState = 0;
	WfmTask currentNode = null;
	*/
	Hashtable<String,BiResult> brHash = new Hashtable();
	BiResult targetBr;

	private Component pplComp;
	private WfmTaskUpdate wipTaskUpdate;
	private int selectedMode;
	private String selectedNodeId;
	private int firstMode;
	private String firstNodeId;

	// protected CellValueAction node_state_changed;
	public void afterBind() {
		flowPrefix="wfmj_";
		flowStepPrefix="wfmjt_";
		stepStepPrefix="wfmtt_";
		stepStepTable="wfmtasktask";	
		flowStepView="wip.WfmJobTask";
		super.afterBind();
		/*if(getSessionHelper().isMobileDevice()) {
			jxSetVisible("detail_grid",false);
			//jxSetVisible("list_wip_WfmJobTask",false);
		}*/
	}
	/*
	protected class WfmTask extends WfmStep implements WipTask {
		int taskState;
		WfmTask(int p_rg, String p_description) {
			super(p_rg, p_description);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Date getStart() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Date getEnd() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public double getProgress() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setStart(Date p_date) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setEnd(Date p_date) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void setProgress(double p_progress) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int getState() {
			// TODO Auto-generated method stub
			return taskState;
		}

		@Override
		public void setState(int p_state) {
			// TODO Auto-generated method stub
			taskState = p_state;
		}
		
		@Override 
		public String getColor() {
			return(WipJob.getStateColor(taskState));
		}
		public void switchStart(boolean p_sw) throws CellException{
			WipJob.taskSwitchStart((WipJob) wj, WfmTask.this, p_sw);
		}
		public void switchEnd(boolean p_sw) throws CellException{
			WipJob.taskSwitchEnd((WipJob) wj, WfmTask.this, p_sw);
		}
	}
	*/

	@Override
	protected WfmStep createWfmStep(CellCollection p_col,int p_rg,String p_description) {
		WfmTask wt = new WfmTask(p_rg,p_description,p_col.getCellInt("wfmjt_order"),p_col.getCellString("wfmjt_createcond"));
		wt.setState(p_col.getCell("wfmjt_state").getInt());
		return(wt);
	}
	
	@Override
	public void createWipFlow() {
		wj = new WfmJob(getBr().getCellInt("wfmj_rg"),getBr().getCellString("wfmj_id"),getBr().getCellString("wfmj_viewid"),getBr().getCellString("wfmj_keyfd"),getBr().getCellString("wfmj_keystr"));	
	}	
	static public void CreateJobFromFlow(int p_flowrg,int p_key) {
		

	}

	@Override
	public void visActionCallBack(int mode,WipStep p_s0,WipStep p_s1) {
		// TODO Auto-generated method stub
		UniLog.log1("called. mode:%d %s s0:%s s1:%s", mode, WipVisNetwork.getCbModeLabel(mode), p_s0, p_s1);
		super.visActionCallBack(mode, p_s0, p_s1);
		switch(mode) {
		case WipVisNetwork.WIPVIS_CBMODE_ARROW_DELETED: 
		case WipVisNetwork.WIPVIS_CBMODE_ARROW_ADDED: 
			if(p_s1 != null ) {
			try {
				WipJob.recalState((WipJob) wj, (WipTask) p_s1);
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			}
			nW.drawNetwork();
			break;
			/*
		case WipVisNetwork.WIPVIS_CBMODE_ARROW_UPDATED:
			try {
				final ZkForm zkf1 = new ZkForm(null,"zkf/erpv4/wipupdlink.zul"); 
				final CellCollection col = new CellCollection();
				Object data = wj.getLinkData(p_s0, p_s1);
				if(data != null) {
					JSONObject jo = new JSONObject(data);
					if(jo != null) CellCollectionToJsonInterface.JSONObjectToCellCollection(col, jo);
				}
				
    			zkf1.doModal(col,new ZkBiEventListener() {
							@Override
							public void onZkBiEvent(Event arg0) throws Exception {
								if(arg0.getTarget().getId().equals("btOK")) {
									zkf1.exitModal();
								}
								if(arg0.getTarget().getId().equals("btCancel")) {
									zkf1.exitModal();
								}
							}
    					}
        		);
				
				wj.setLinkData(p_s0, p_s1, "HAHA");
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			break;
				*/
		case WipVisNetwork.WIPVIS_CBMODE_NODE_CLICKED:
		case WipVisNetwork.WIPVIS_CBMODE_START_CLICKED:
		case WipVisNetwork.WIPVIS_CBMODE_END_CLICKED: 
			switch(mode) {
			case WipVisNetwork.WIPVIS_CBMODE_START_CLICKED:
					setupStartPropertyPanel();
					//nW.drawNetwork(new VectorUtil().addElement("Start").addElement("End").toVector());
					break;
			case WipVisNetwork.WIPVIS_CBMODE_NODE_CLICKED:
					setupNodePropertyPanel(p_s0);
					//nW.drawNetwork(new VectorUtil().addElement(p_s0.getId()).toVector());
					break;
			case WipVisNetwork.WIPVIS_CBMODE_END_CLICKED:
					setupEndPropertyPanel();
					//nW.drawNetwork(new VectorUtil().addElement("Start").addElement("End").toVector());
					break;
			}
			break;
		default:
//			ppl.setVisible(false);
			break;
		}
	}	
	
	void setupNodePropertyPanel(WipStep ws) {
		//int srg = NumberUtils.toInt(ws.getId());
		if (!StringUtils.equals(selectedNodeId, ws.getId())) {
			UniLog.log1("id:%s, desc:%s, order:%d, level:%d, order:%d", ws.getId(), ws.getDescription(), ws.getOrder(), ws.getLevel(), ws.getOrder());
			selectedMode = WipVisNetwork.WIPVIS_CBMODE_NODE_CLICKED;
			selectedNodeId = ws.getId();
			showWipTaskUpdateView(false);
		}
	}
	void setupStartPropertyPanel() {
		selectedMode = WipVisNetwork.WIPVIS_CBMODE_START_CLICKED;
		selectedNodeId = null;
		showWipTaskUpdateView(false);
	}

	void setupEndPropertyPanel() {
		selectedMode = WipVisNetwork.WIPVIS_CBMODE_END_CLICKED;
		selectedNodeId = null;
		showWipTaskUpdateView(false);
	}
	
	private void showWipTaskUpdateView(boolean handleMessageViewOnly) {
		UniLog.log1("showWipTaskUpdateView wfmj_rg:%d, selectedNodeId:%s, handleMessageViewOnly:%b", getBr().getCellInt("wfmj_rg"), selectedNodeId, handleMessageViewOnly);
		if (pplComp == null)
			return;
		jxSetVisible("jobTaskViewLink", false);
		if (NumberUtils.toInt(selectedNodeId) > 0) {
			try {
				if (handleMessageViewOnly)
					wipTaskUpdate.reloadActivity();
				else {
					int srg = NumberUtils.toInt(selectedNodeId);
					wipTaskUpdate.loadWorkFlow(null, sessionHelper, getBr().getCellInt("wfmj_rg"), srg);
					wipTaskUpdate.setPanelDisplayMode(WfmTaskBase.NODE_PROPERTY_STATE_NODE, WfmStep.makeNodeId(srg));
					nW.drawNetwork(new VectorUtil().addElement(selectedNodeId).toVector());
				}
				pplComp.setVisible(true);
				jxSetVisible("jobTaskViewLink", true);
			} catch (Exception e) {
				UniLog.log(e);
				pplComp.setVisible(false);
			}
		} else if (selectedMode == WipVisNetwork.WIPVIS_CBMODE_START_CLICKED) {
			try {
       			wipTaskUpdate.loadWorkFlow(null, sessionHelper, getBr().getCellInt("wfmj_rg"), 0, false);
       			wipTaskUpdate.setPanelDisplayMode(WfmTaskBase.NODE_PROPERTY_STATE_START, null);
       			nW.drawNetwork(new VectorUtil().addElement("Start").toVector());
				pplComp.setVisible(true);
			} catch (Exception e) {
				UniLog.log(e);
				pplComp.setVisible(false);
			}
		} else if (selectedMode == WipVisNetwork.WIPVIS_CBMODE_END_CLICKED) {
			try {
       			wipTaskUpdate.loadWorkFlow(null, sessionHelper, getBr().getCellInt("wfmj_rg"), 0, false);
       			wipTaskUpdate.setPanelDisplayMode(WfmTaskBase.NODE_PROPERTY_STATE_END, null);
       			nW.drawNetwork(new VectorUtil().addElement("End").toVector());
				pplComp.setVisible(true);
			} catch (Exception e) {
				UniLog.log(e);
				pplComp.setVisible(false);
			}
		} else {
			pplComp.setVisible(false);
	    	try {
				wipTaskUpdate.setPanelDisplayMode(WfmTaskBase.NODE_PROPERTY_STATE_NONE, null);
			} catch (Exception e) {
				UniLog.log(e);
			}
		}
	}
	
	@Override
	protected void loadNetwork() {
		super.loadNetwork();
		((WfmJob) wj).setState(getBr().getCellInt("wfmj_state"));
		((WfmJob) wj).setDeadLine(getBr().getCell("wfmj_timeout").getDate());
		((WfmJob) wj).setStart(getBr().getCell("wfmj_starttime").getDate());
		((WfmJob) wj).setEnd(getBr().getCell("wfmj_endtime").getDate());
	}
	
	@Override
	public void bindCellCollection(BiResult c,int mode) {
		super.bindCellCollection(c,mode);
 			String targetViewId = c.getCellString("wfmj_viewid");
		if(mode == JxZkBiBase.MODE_UPDATE && !targetViewId.trim().equals("") ) {
			targetBr = brHash.get(targetViewId);
			if(targetBr == null) {
				BiView tv = c.getView().getSchema().getViewByName(targetViewId);
				targetBr = tv.newBiResult(getLoginId(), null, null, getSessionHelper());
				brHash.put(targetViewId, targetBr);
			}
			//String pkey = targetBr.getView().getTable().getPrimaryKey();
			
			//andrew230928 fix parse error due to pkey is blank, should optian wfmj_keyfd first, then viewpkey
//			UniLog.log1("DEBUG: viewpkey:%s wfmj_keyfd:%s", targetBr.getView().getTable().getPrimaryKey(), c.getCellString("wfmj_keyfd"));
			UniLog.log1("DEBUG: viewpkey:%s wfmj_keyfd:%s", targetBr.getView().getTable().getPrimaryKeys(), c.getCellString("wfmj_keyfd"));
			String pkey = c.getCellString("wfmj_keyfd"); 
			if (StringUtils.isBlank(pkey)) {
				String primaryKeys[] = targetBr.getView().getTable().getPrimaryKeys();
				if(primaryKeys != null && primaryKeys.length == 1) {
					pkey = primaryKeys[0];
				}
				//pkey = targetBr.getView().getTable().getPrimaryKey();
			}
			targetBr.addCustomCondition(pkey + " = " + c.getCellInt("wfmj_keystr"));
			targetBr.query();
			if(targetBr.getRowCount() != 1) {
				UniLog.log("Warning !! Target Data for this job not found");
			} 

			IdSpace isp = (IdSpace) getNativeComponent();
			//setup propertyPanel
			Div propertyPanel = (Div)isp.getFellow("propertyPanel");
			if (propertyPanel.hasFellow("listPropertyPanel"))
				pplComp = propertyPanel.getFellow("listPropertyPanel");
			else {
				pplComp = new Div();
				pplComp.setId("listPropertyPanel");
				((HtmlBasedComponent) pplComp).setStyle("max-width:500px");
				((HtmlBasedComponent) pplComp).setVflex("1");
				propertyPanel.appendChild(pplComp);
			}
			pplComp.setVisible(false);
			selectedMode = firstMode;
			selectedNodeId = firstNodeId;
			firstMode = 0;
			firstNodeId = null;

			//setup WfmTaskUpdate
			if (wipTaskUpdate == null) {
				wipTaskUpdate = new WfmTaskUpdate(pplComp, false, new EventListener<Event>() {
					Button submitButton;
					@Override
					public void onEvent(Event event) throws Exception {
						UniLog.log1("event:%s", event);
						if (submitButton == null && pplComp.hasFellow("btSubmitChange"))
							submitButton = (Button) pplComp.getFellow("btSubmitChange");
						if (event.getName().equals(WfmTaskUpdate.EV_ON_MESSAGE_CHANGED) ||
								event.getName().equals(WfmTaskUpdate.EV_ON_TASKSTATE_CHANGED) ||
								event.getName().equals(WfmTaskUpdate.EV_ON_JOBSTATE_CHANGED)) {
							if (wipTaskUpdate.stateChangedOrHasMessage()) {
								try {
									wipTaskUpdate.updateJobStatus(0, "");
								} catch (Exception e) {
									UniLog.log(e);
									ZkUtil.showErrMsg(StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
								}
								if (submitButton != null) submitButton.setDisabled(false);
							} else {
								if (submitButton != null) submitButton.setDisabled(true);
							}
							return;
						}
						if (event.getName().equals(Events.ON_CLICK)) {
							if (event.getTarget() instanceof Button) {
								Button bt = (Button) event.getTarget();
								if (bt.getId().equals("btSubmitChange")) {
									try {
										wipTaskUpdate.updateJobStatus();
									} catch (Exception e) {
										UniLog.log(e);
										ZkUtil.showErrMsg(StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
									}
								}
							}
						}
					}
				});

				EventQueue<Event> eventQueue = EventQueues.lookup("WipNotify", EventQueues.APPLICATION, true);
				eventQueue.subscribe(new EventListener<Event>() {
					@Override
					public void onEvent(Event event) throws Exception {
						if (StringUtils.equals(event.getName(), "onWipNotify")) {
							WfmEventData wed = (WfmEventData) event.getData();
							
							//andrew231011 bug getBr() is null when WfmCronJob.doCreateJob() is running
							if (!checkBr()) {
								UniLog.log("br is null, skip");
								return;
							}
							UniLog.log1("event:%s, getWfmrg:%d, wfmj_rg:%d, selectedMode:%d, selectedNodeId:%s, isStateChanged:%b, isNetworkChanged:%b, isMessageAdded:%b, size:%d", event, wed.getWfmrg(), getBr().getCellInt("wfmj_rg"), selectedMode, selectedNodeId, wed.isStateChanged(), wed.isNetworkChanged(), wed.isMessageAdded(), wed.getTaskStateChangeList().size());
							if (wed.getWfmrg() == getBr().getCellInt("wfmj_rg")) {
								if (wed.isStateChanged() || wed.isNetworkChanged() || !wed.getTaskStateChangeList().isEmpty()) {
									/*showWipTaskUpdateView(false);
									loadNetwork();
									nW.drawNetwork();*/
									Button btReloadDetail = (Button)jxAdd("btReloadDetail").getNativeObject();
									if (!btReloadDetail.isDisabled()) {
										firstMode = selectedMode;
										firstNodeId = selectedNodeId;
										Events.echoEvent(Events.ON_CLICK, btReloadDetail, null);
									}
								} else if (wed.isMessageAdded()) {
									if (NumberUtils.toInt(selectedNodeId) > 0 || selectedMode == WipVisNetwork.WIPVIS_CBMODE_START_CLICKED || selectedMode == WipVisNetwork.WIPVIS_CBMODE_END_CLICKED)
										showWipTaskUpdateView(true);
								}
							}
						}
					}
				});

				//setup splitter
				final Splitter s1 = (Splitter)isp.getFellowIfAny("s1");
				final Panel p1 = (Panel)isp.getFellow("p1");
				final Div visNwEventDivDet = (Div)isp.getFellow("visNwEventDivDet");
				if (s1 != null) {
					final Hbox h1 = (Hbox)isp.getFellow("h1");
					final Vlayout v1 = (Vlayout)isp.getFellow("v1");
					s1.addEventListener(Events.ON_OPEN, new EventListener<OpenEvent>() {
						@Override
						public void onEvent(OpenEvent event) throws Exception {
							UniLog.log1("event:%s", event);
							if (event.isOpen()) {
								Integer h1Width = (Integer)h1.getAttribute("compWidth");
								Integer v1Width = (Integer)v1.getAttribute("compWidth");
								Integer ssWidth = (Integer)v1.getAttribute("ssWidth");
								if (h1Width != null && v1Width != null && ssWidth != null) {
									int tmpWidth = h1Width - v1Width - ssWidth;
									UniLog.log1("h1Width:%d, v1Width:%d, ssWidth:%d, tmpWidth:%d", h1Width, v1Width, ssWidth, tmpWidth);
									v1.setHflex("" + v1Width);
									p1.setHflex("" + (tmpWidth >= 0 ? tmpWidth : 0));
								} else {
									v1.setHflex("1");
									p1.setHflex("3");
								}
								v1.setVisible(true);
							} else
								v1.setVisible(false);
							event.getTarget().getParent().invalidate();
						}
					});
					final Component[] comps = new Component[] {h1, p1, v1};
					for (final Component comp : comps) {
						comp.addEventListener(Events.ON_AFTER_SIZE, new EventListener<AfterSizeEvent>() {
							@Override
							public void onEvent(AfterSizeEvent event) throws Exception {
								UniLog.log1("%s width:%d", comp.getId(), event.getWidth());
								comp.setAttribute("compWidth", event.getWidth());
								Integer h1Width = (Integer)h1.getAttribute("compWidth");
								Integer v1Width = (Integer)v1.getAttribute("compWidth");
								Integer p1Width = (Integer)p1.getAttribute("compWidth");
								if (h1Width != null && v1Width != null && p1Width != null && v1.isVisible() && s1.isOpen()) {
									int o = h1Width - v1Width - p1Width;
									UniLog.log1("o:%d", o);
									if (o > 0)
										v1.setAttribute("ssWidth", o);
								}
							}
						});
					}
				}
				p1.addEventListener(Events.ON_AFTER_SIZE, new EventListener<AfterSizeEvent>() {
					@Override
					public void onEvent(AfterSizeEvent event) throws Exception {
						visNwEventDivDet.invalidate();
						Events.sendEvent(Events.ON_SIZE, visNwEventDivDet, null);
					}
				});
				nW.setCompactSpacing(true);
			}
			if (NumberUtils.toInt(selectedNodeId) > 0 || selectedMode == WipVisNetwork.WIPVIS_CBMODE_START_CLICKED || selectedMode == WipVisNetwork.WIPVIS_CBMODE_END_CLICKED)
				showWipTaskUpdateView(false);
			else
				nW.drawNetwork();
			
			jxAdd("jobTaskViewLink").addActionListener(new JxActionListener() {
				@Override
				public void actionPerformed(JxField field) {
					try {
						String url1 = String.format("%s/wfmtaskg2ext.html?jobid=%s&taskid=%s", sessionHelper.getPublicBaseURL(), getBr().getCellString("wfmj_id"), selectedNodeId);
						final String url = URLParamHash.appendParamHash(url1, "jobid", "taskid");
						final Image img = new Image() {{
							byte[] data = QRCodeUtil.createQRCode(url, 150, 150, "PNG");
							setContent(new AImage("", data));
						}};
						final Textbox tb = new Textbox() {{
							setMultiline(true);
							setRows(4);
							setHflex("1");
							setReadonly(true);
							setValue(url);
						}};
						Vlayout vl = new Vlayout() {{
							appendChild(new Label("Scan the QR Code or Copy Link"));
							appendChild(img);
							appendChild(tb);
							setHflex("1");
						}};
						ZkBiMsgboxButton[] btns = new ZkBiMsgboxButton[] {new ZkBiMsgboxButton(sessionHelper.getBtLabel("Copy Link")),new ZkBiMsgboxButton(sessionHelper.getBtLabel("Close"))};
						new ZkBiMsgbox(sessionHelper).setContent(vl).setButtons(btns).setEventListener(new ZkBiEventListener<Event>() {
							@Override
							public void onZkBiEvent(Event event) throws Exception {
								ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
								UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
								if (StringUtils.equals(btn.getName(), sessionHelper.getBtLabel("Copy Link")))
									ZkUtil.js("copyToClipboard('%s'); $.notify(\"%s\", { className: \"info\", globalPosition:\"bottom right\", autoHideDelay: 5000 });", url, sessionHelper.getLabel("Data Copied")); 
							}
						}).build().appendStyle("width:100%;max-width:400px").doModal();
					} catch (Exception e) {
						UniLog.log(e);
					}
				}
			});
			jxSetVisible("jobTaskViewLink", NumberUtils.toInt(selectedNodeId) > 0);
		} else {
			targetBr = null;
		}
	}
	
	void syncStateToJob() throws CellException {
		int newState = ((WfmJob) wj).getState();
		if( newState != getBr().getCellInt("wfmj_state")) {
			getBr().getCell("wfmj_state").set(newState);
			if(newState == WipJob.JOB_STATE_STARTED) {
				Date d0 = getBr().getCell("wfmj_starttime").getDate();
				if(d0 == null || !d0.after(DateUtil.minTime)) {
					getBr().getCell("wfmj_starttime").set(new Date());
					getBr().getCell("wfmj_startby").set(getLoginId());
				}
			}
			if(newState == WipJob.JOB_STATE_COMPLETED) {
				Date d1 = getBr().getCell("wfmj_endtime").getDate();
				if(d1 == null || !d1.after(DateUtil.minTime)) {
					getBr().getCell("wfmj_endtime").set(new Date());
					getBr().getCell("wfmj_endby").set(getLoginId());
				}
			}
		}
		BiResult sr = getBr().getSubLink(flowStepView);
		Vector<BiCellCollection> v = sr.getRowCollectionList();
		for(BiCellCollection col : v) {
			int rg = col.getCell("wfmjt_rg").getInt();
			String nodeId = WfmStep.makeNodeId(rg);
			WfmTask t = (WfmTask) wj.getStep(nodeId);
			if(t != null) {
				newState = t.getState();
				if(newState != col.getCell("wfmjt_state").getInt()) {
					col.getCell("wfmjt_state").set(newState);
					if(newState == WipJob.JOB_STATE_STARTED) {
						Date d0 = col.getCell("wfmjt_starttime").getDate();
						if(d0 == null || !d0.after(DateUtil.minTime)) {
							col.getCell("wfmjt_starttime").set(new Date());
							col.getCell("wfmjt_startby").set(getLoginId());
						}
					}
					if(newState == WipJob.JOB_STATE_COMPLETED) {
						Date d0 = col.getCell("wfmjt_endtime").getDate();
						if(d0 == null || !d0.after(DateUtil.minTime)) {
							col.getCell("wfmjt_endtime").set(new Date());
							col.getCell("wfmjt_endby").set(getLoginId());
						}
					}
				}
			}
		}
	}
	
	@Override
	protected ReturnMsg beforeAdd(BiResult p_br) {
		try {
			syncStateToJob();
			getBr().getCell("wfmj_state").set(1);
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,"Exception "+cex));
		}
		return(super.beforeAdd(p_br));
	}
	@Override
	protected ReturnMsg beforeUpdate(BiResult p_br) {
		try {
			syncStateToJob();
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,"Exception "+cex));
		}
		return(super.beforeUpdate(p_br));
	}
	
	@Override
	protected void doAfterAddNewStep(WipStep wt) {
		super.doAfterAddNewStep(wt);
		if(((WfmJob)wj).getState() == WipJob.JOB_STATE_STARTED) {
			((WipTask) wt).setState(WipJob.JOB_STATE_AWAKED);
		}
		((WipTask) wt).setState(WfmTask.WFMTASK_STATE_SETNEW);
	}
}
