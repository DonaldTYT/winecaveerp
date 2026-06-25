package com.uniinformation.erpv4.wip;


import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.wip.WipException;
import com.uniinformation.wip.WipJob;
import com.uniinformation.wip.WipStep;
import com.uniinformation.wip.WipTask;
import com.uniinformation.wip.WipVisCallbackInterface;
import com.uniinformation.wip.WipVisNetwork;
import com.uniinformation.zkf.ZkForm;

public class WfmTaskBase implements WipVisCallbackInterface {
	public static final String EV_ON_TASKSTATE_CHANGED = "ON_TASKSTATE_CHANGED";
	public static final String EV_ON_JOBSTATE_CHANGED = "ON_JOBSTATE_CHANGED";
	public static final int NODE_PROPERTY_STATE_NONE = 0;
	public static final int NODE_PROPERTY_STATE_START= 1;
	public static final int NODE_PROPERTY_STATE_END  = 2;
	public static final int NODE_PROPERTY_STATE_NODE = 3;
	private ZkForm zkf1;
	private WfmJob wj;
	private CellCollection propertyCollection;
	private int nodeProperyState = 0;
	private WfmTask currentNode = null;
//	WipVisNetwork nW;
	private Component taskDetailPage = null;
	private Component taskDetailEmptyPage = null;
	private Component taskChoices = null;
	private Component taskChoicesLayout = null;
	private Component taskIdLayout = null;
	private Component taskStatusLayout = null;
	protected EventListener onClickListener;
	private WipVisNetwork nW;
	protected CellValueAction 
	node_state_changed =
			new CellValueAction() {
				boolean needNotify = true;
				@Override
				public void cellAction_onchange (Cell p_value)
						throws CellException {
					// TODO Auto-generated method stub
					UniLog.log1("p_value:%s, nodeProperyState:%d", p_value, nodeProperyState);
					CellException taskSwEx = null;
					try {
						if(nodeProperyState == NODE_PROPERTY_STATE_NODE) {
							if(p_value == propertyCollection.testCell("nodeprop_switchstart")) {
								UniLog.log1("is nodeprop_switchstart");
								boolean sw = p_value.getBoolean();
								if(!sw) {
									Cell endSwitch = propertyCollection.testCell("nodeprop_switchend");
									if(endSwitch.getBoolean()) {
										needNotify = false;
										try {
											UniLog.log1("endSwitch sync false");
											endSwitch.sync(false);
										} catch (CellException cex) {
											UniLog.log("unstart end switch blocked");
											needNotify = false;
											endSwitch.sync(true);
											throw cex;
										}
										needNotify = true;
									}
								}
								if(!sw) {
									Cell choice = propertyCollection.testCell("nodeprop_choices");
									if(choice != null) {
										needNotify = false;
										Integer c = null;
										try {
											c = choice.getInt();
										} catch (Exception ex) {
										}
										try {
											UniLog.log1("nodeprop_choices sync 0");
											choice.sync(0);
										} catch (CellException cex) {
											UniLog.log("choices sync 0 blocked");
											needNotify = false;
											if (c != null)
												choice.sync(c);
											throw cex;
										}
										needNotify = true;
									}
									
								}
								try {
									WipJob.taskSwitchStart((WipJob) wj, currentNode, sw);
								} catch (CellException ex) {
									taskSwEx = ex;
									throw ex;
								}
							}
							if(p_value == propertyCollection.testCell("nodeprop_switchend")) {
								UniLog.log1("is nodeprop_switchend");
								boolean sw = p_value.getBoolean();
								if(sw) {
									Cell startSwitch = propertyCollection.testCell("nodeprop_switchstart");
									if(!startSwitch.getBoolean()) {
										needNotify = false;
										try {
											UniLog.log1("startSwitch sync true");
											startSwitch.sync(true);
										} catch (CellException cex) {
											UniLog.log("set start switch blocked");
											needNotify = false;
											startSwitch.sync(false);
											throw cex;
										}
										needNotify = true;
									}
								}
								try {
									WipJob.taskSwitchEnd((WipJob) wj, currentNode, sw);
								} catch (CellException ex) {
									taskSwEx = ex;
									throw ex;
								}
							}
							if(p_value == propertyCollection.testCell("nodeprop_choices")) {
								UniLog.log1("is nodeprop_choices");
								int choice =  p_value.getInt();
								currentNode.setChoice(choice);
								if(choice > 0) {
									Cell endSwitch = propertyCollection.testCell("nodeprop_switchend");
									if(endSwitch.getBoolean()) {
										try {
											WipJob.taskSwitchEnd((WipJob) wj, currentNode, true);
										} catch (CellException ex) {
											taskSwEx = ex;
											throw ex;
										}
									} else {
										needNotify = false;
										try {
											UniLog.log1("endSwitch sync true");
											endSwitch.sync(true);
										} catch (CellException ex) {
											UniLog.log("set end switch blocked");
											needNotify = false;
											endSwitch.sync(false);
											throw ex;
										}
										needNotify = true;
									}
								} else {
									Cell startSwitch = propertyCollection.testCell("nodeprop_switchstart");
									if(startSwitch.getBoolean()) {
										needNotify = false;
										try {
											UniLog.log1("startSwitch sync false");
											startSwitch.sync(false);
										} catch (CellException ex) {
											UniLog.log("unset start switch blocked");
											needNotify = false;
											startSwitch.sync(true);
											throw ex;
										}
										needNotify = true;
									} else {
										try {
											WipJob.taskSwitchStart((WipJob) wj, currentNode, false);
										} catch (CellException ex) {
											taskSwEx = ex;
											throw ex;
										}
									}
								}
							}
							if(needNotify) {
		//						if(nW != null) nW.drawNetwork(null);
								if(onClickListener != null) {
									try {
										onClickListener.onEvent(new Event(EV_ON_TASKSTATE_CHANGED,null,currentNode));
									} catch (Exception ex) {
										UniLog.log(ex);
									}
								}
								if(nW!=null) nW.drawNetwork(null);
								/*
								EventQueue que = EventQueues.lookup("WipNotify", EventQueues.APPLICATION, true);
								que.publish(new Event("onWipNotify", null, "Wip Updated"));
								*/
							}
						}
						if(nodeProperyState == NODE_PROPERTY_STATE_START
						   || nodeProperyState == NODE_PROPERTY_STATE_END) {
							if(p_value == propertyCollection.testCell("nodeprop_switchstart")) {
								UniLog.log1("is nodeprop_switchstart");
								boolean sw = p_value.getBoolean();
								if(!sw) {
									Cell endSwitch = propertyCollection.testCell("nodeprop_switchend");
									if(endSwitch.getBoolean()) {
										needNotify = false;
										try {
											UniLog.log1("endSwitch sync false");
											endSwitch.sync(false);
										} catch (CellException cex) {
											UniLog.log("unstart end switch blocked");
											needNotify = false;
											endSwitch.sync(true);
											throw cex;
										}
										needNotify = true;
									}
								}
								wj.switchStart(sw);
							}
							if(p_value == propertyCollection.testCell("nodeprop_switchend")) {
								UniLog.log1("is nodeprop_switchend");
								boolean sw = p_value.getBoolean();
								if(sw) {
									Cell startSwitch = propertyCollection.testCell("nodeprop_switchstart");
									if(!startSwitch.getBoolean()) {
										needNotify = false;
										try {
											UniLog.log1("startSwitch sync true");
											startSwitch.sync(true);
										} catch (CellException cex) {
											UniLog.log("set start switch blocked");
											needNotify = false;
											startSwitch.sync(false);
											throw cex;
										}
										needNotify = true;
									}
								}
								wj.switchEnd(sw);
							}
							if(needNotify) {
								if(nW != null) nW.drawNetwork(null);
								if(onClickListener != null) {
									try {
										onClickListener.onEvent(new Event(EV_ON_JOBSTATE_CHANGED,null,wj));
									} catch (Exception ex) {
										UniLog.log(ex);
									}
								}
								EventQueue que = EventQueues.lookup("broadcast", EventQueues.APPLICATION, true);
								que.publish(new Event("onWipNotify", null, "Wip Updated"));
							}
						}
						
						notifyStateChanged();
					} catch (CellException ex) {
						UniLog.log1("ex:%s, taskSwEx:%s", ex, taskSwEx);
						if (taskSwEx != null)
							ZkUtil.errMsg(StringUtils.defaultIfBlank(ex.getMessage(), ex.toString()));
						if(!needNotify) {
							needNotify = true;
						}
						throw(ex);
					}
				}

				@Override
				public void cellAction_onfree() throws CellException {
					// TODO Auto-generated method stub
				}
				
			};	
			
	protected void notifyStateChanged() {	
		
	}
	
	public WfmTaskBase(Component comp,boolean p_hasNw,EventListener p_onClickListener) {
		onClickListener = p_onClickListener;
		if(comp != null) {
			ZkForm zkf1 = new ZkForm(comp,"zkf/WfmProperties.zul");
			propertyCollection = new CellCollection();
			try {
				if(comp.hasFellow("taskDetailPage")) {
					taskDetailPage = comp.getFellow("taskDetailPage");
				}
				if(comp.hasFellow("taskDetailEmptyPage")) {
					taskDetailEmptyPage = comp.getFellow("taskDetailEmptyPage");
				}
				taskChoices = comp.getFellowIfAny("nodeprop_choices");
				taskChoicesLayout = comp.getFellowIfAny("nodeprop_choices_layout");
				taskIdLayout = comp.getFellowIfAny("nodeprop_taskid_layout");
				taskStatusLayout = comp.getFellowIfAny("nodeprop_taskstatus_layout");

				zkf1.mapCellCollection(propertyCollection,p_onClickListener);
				if(comp.hasFellow("visNwEventDiv")) {
					if(p_hasNw) {
						HtmlBasedComponent visNwEventDiv = (HtmlBasedComponent) comp.getFellow("visNwEventDiv");
//						visNwEventDiv.setHeight("150px");
						try {
							nW = new WipVisNetwork(visNwEventDiv, "visNwContentDiv",null, null,null,null,this);
						} catch (WipException wex) {
							UniLog.log(wex);
						}
					} else {
						comp.getFellow("visNwEventDiv").setVisible(false);
						
					}
				}
			} catch (Exception ex){
				UniLog.log(ex);
			}
		}
	}
	private void addOrDelSwitchAction(boolean isAdd) {
		Cell cc = propertyCollection.testCell("nodeprop_switchstart");
		if(cc != null) {
			if (isAdd)
				cc.addAction(node_state_changed);
			else
				cc.delAction(node_state_changed);
		}
		cc = propertyCollection.testCell("nodeprop_switchend");
		if(cc != null) {
			if (isAdd)
				cc.addAction(node_state_changed);
			else
				cc.delAction(node_state_changed);
		}
		if(taskChoices != null) {
			cc = propertyCollection.testCell("nodeprop_choices");
			if (isAdd)
				cc.addAction(node_state_changed);
			else
				cc.delAction(node_state_changed);
		}
	}
	void setupNodePropertyPanel() {
		try {
		addOrDelSwitchAction(false);
		if(nodeProperyState == NODE_PROPERTY_STATE_NODE) {
		switch(currentNode.getState()) {
		case WipJob.JOB_STATE_STARTED: 
			propertyCollection.getCell("nodeprop_switchstart").set(true);
			propertyCollection.getCell("nodeprop_switchend").set(false);
			break;
		case WipJob.JOB_STATE_COMPLETED: 
			propertyCollection.getCell("nodeprop_switchstart").set(true);
			propertyCollection.getCell("nodeprop_switchend").set(true);
			break;
		default:
			propertyCollection.getCell("nodeprop_switchstart").set(false);
			propertyCollection.getCell("nodeprop_switchend").set(false);
			break;
		}
		}
		if(nodeProperyState == NODE_PROPERTY_STATE_START) {
		switch(wj.getState()) {
		case WipJob.JOB_STATE_STARTED: 
			propertyCollection.getCell("nodeprop_switchstart").set(true);
			propertyCollection.getCell("nodeprop_switchend").set(false);
			break;
		case WipJob.JOB_STATE_COMPLETED: 
			propertyCollection.getCell("nodeprop_switchstart").set(true);
			propertyCollection.getCell("nodeprop_switchend").set(true);
			break;
		default:
			propertyCollection.getCell("nodeprop_switchstart").set(false);
			propertyCollection.getCell("nodeprop_switchend").set(false);
			break;
		}
		}
		if(nodeProperyState == NODE_PROPERTY_STATE_END) {
		switch(wj.getState()) {
		case WipJob.JOB_STATE_STARTED: 
			propertyCollection.getCell("nodeprop_switchstart").set(true);
			propertyCollection.getCell("nodeprop_switchend").set(false);
			break;
		case WipJob.JOB_STATE_COMPLETED: 
			propertyCollection.getCell("nodeprop_switchstart").set(true);
			propertyCollection.getCell("nodeprop_switchend").set(true);
			break;
		default:
			propertyCollection.getCell("nodeprop_switchstart").set(false);
			propertyCollection.getCell("nodeprop_switchend").set(false);
			break;
		}
		}

		if(taskChoices != null) {
			if(currentNode != null && currentNode.getType() == WfmStep.WFMTYPE_CHOICES) {
				taskChoicesLayout.setVisible(true);
				if(taskChoices instanceof Radiogroup) {
					Radiogroup cb = (Radiogroup) taskChoices;
					int n = cb.getItemCount();
					for(int i = n-1;i>=0;i--) {
						cb.removeItemAt(i);
					}	
					List<String> clist = currentNode.getChoiceList();
					for(int i=0;i<clist.size();i++) {
						Radio rd =  cb.appendItem(clist.get(i),null);
						rd.setValue(i);
						//if(i == 0) rd.setVisible(false);
					}
					cb.setSelectedIndex(currentNode.getChoice());
					propertyCollection.getCell("nodeprop_choices").sync(currentNode.getChoice());
				}
			} else {
				taskChoicesLayout.setVisible(false);
			}
		}
		taskIdLayout.setVisible(taskIdLayout != null && currentNode != null);
		taskStatusLayout.setVisible(taskIdLayout != null && currentNode != null);
		addOrDelSwitchAction(true);
		} catch (CellException cex) {
			UniLog.log(cex);
		}
	}
	
	void setWipJob(WfmJob p_wj) {
		wj = p_wj;
		if(nW != null) {
			nW.setWorkFlow(wj);
			nW.drawNetwork();
		}
	}
	public WfmJob getWipJob() {
		return(wj);
	}
	
	public void setPanelDisplayMode(int p_mode,String p_nodeid) throws Exception {
		switch(p_mode) {
		case NODE_PROPERTY_STATE_START :
		case NODE_PROPERTY_STATE_END  :
			nodeProperyState = p_mode;
			currentNode = null;
			if(taskDetailPage != null) {
				taskDetailPage.setVisible(true);
			}
			if(taskDetailEmptyPage != null) {
				taskDetailEmptyPage.setVisible(false);
			}
			if(propertyCollection.testCell("nodeprop_id") != null) {
				propertyCollection.testCell("nodeprop_id").set(wj.getId());
			}
			if(propertyCollection.testCell("nodeprop_title") != null) {
				propertyCollection.testCell("nodeprop_title").set(wj.getTitle());
			}
			if(propertyCollection.testCell("nodeprop_jobstatus") != null) {
				propertyCollection.testCell("nodeprop_jobstatus").set(WipJob.JOB_STATE_NAME[wj.getState()]);
			}
			if(propertyCollection.testCell("nodeprop_taskid") != null) {
				propertyCollection.testCell("nodeprop_taskid").set("");
			}
			if(propertyCollection.testCell("nodeprop_task") != null) {
				propertyCollection.testCell("nodeprop_task").set("");
			}
			if(propertyCollection.testCell("nodeprop_taskstatus") != null) {
				propertyCollection.testCell("nodeprop_taskstatus").set("");
			}
				if(propertyCollection.testCell("nodeprop_start") != null) {
					Date d0 = wj.getStart();
					if(d0.after(DateUtil.minTime)) {
						propertyCollection.testCell("nodeprop_start").set(
								"Start On:"+
								DateUtil.dateToDateTimeStr(d0)+
								" By " + 
								wj.getStartBy()
								);
						if(propertyCollection.testCell("nodeprop_end") != null) {
							Date d1 = wj.getEnd();
							if(d1.after(DateUtil.minTime)) {
								propertyCollection.testCell("nodeprop_end").set(
								"Completed On:"+
								DateUtil.dateToDateTimeStr(d1)+
								" By " + 
								wj.getEndBy()
								);
							} else {
								propertyCollection.testCell("nodeprop_end").set("Work In Progress");
							}
						}
					} else {
						propertyCollection.testCell("nodeprop_start").set("Not Yet Started");
						if(propertyCollection.testCell("nodeprop_end") != null) {
							propertyCollection.testCell("nodeprop_end").set("");
						}
					}
				}
				setupNodePropertyPanel();
			break;
		case NODE_PROPERTY_STATE_NONE :
			nodeProperyState = NODE_PROPERTY_STATE_NONE;
			if(taskDetailPage != null) {
				taskDetailPage.setVisible(false);
			}
			if(taskDetailEmptyPage != null) {
				taskDetailEmptyPage.setVisible(true);
			}
			break;
		case NODE_PROPERTY_STATE_NODE :
			currentNode = (WfmTask) wj.getTask(p_nodeid);
			if(currentNode == null) throw new Exception("Invalid Node");
			nodeProperyState = NODE_PROPERTY_STATE_NODE;
			if(taskDetailPage != null) {
				taskDetailPage.setVisible(true);
			}
			if(taskDetailEmptyPage != null) {
				taskDetailEmptyPage.setVisible(false);
			}
			if(propertyCollection.testCell("nodeprop_id") != null) {
				propertyCollection.testCell("nodeprop_id").set(wj.getId());
			}
			if(propertyCollection.testCell("nodeprop_title") != null) {
				propertyCollection.testCell("nodeprop_title").set(wj.getTitle());
			}
			if(propertyCollection.testCell("nodeprop_jobstatus") != null) {
				propertyCollection.testCell("nodeprop_jobstatus").set(WipJob.JOB_STATE_NAME[wj.getState()]);
			}
			if(propertyCollection.testCell("nodeprop_taskid") != null) {
				propertyCollection.testCell("nodeprop_taskid").set(
							currentNode.getId()
							);
			}
			if(propertyCollection.testCell("nodeprop_task") != null) {
				propertyCollection.testCell("nodeprop_task").set(
							wj.getName() + " "  + currentNode.getDescription()
							);
			}
			if(propertyCollection.testCell("nodeprop_taskstatus") != null) {
				propertyCollection.testCell("nodeprop_taskstatus").set(WipJob.JOB_STATE_NAME[currentNode.getState()]);
			}
				if(propertyCollection.testCell("nodeprop_start") != null) {
					Date d0 = currentNode.getStart();
					if(d0.after(DateUtil.minTime)) {
						propertyCollection.testCell("nodeprop_start").set(
								"Start On:"+
								DateUtil.dateToDateTimeStr(d0)+
								" By " + 
								currentNode.getStartBy()
								);
						if(propertyCollection.testCell("nodeprop_end") != null) {
							Date d1 = currentNode.getEnd();
							if(d1.after(DateUtil.minTime)) {
								propertyCollection.testCell("nodeprop_end").set(
								"Completed On:"+
								DateUtil.dateToDateTimeStr(d1)+
								" By " + 
								currentNode.getEndBy()
								);
							} else {
								propertyCollection.testCell("nodeprop_end").set("Work In Progress");
							}
						}
					} else {
						propertyCollection.testCell("nodeprop_start").set("Not Yet Started");
						if(propertyCollection.testCell("nodeprop_end") != null) {
							propertyCollection.testCell("nodeprop_end").set("");
						}
					}
				}
				setupNodePropertyPanel();
			break;
		default : throw new Exception("Invalid State");
		}
	}

	@Override
	public void visActionCallBack(int mode, WipStep s0, WipStep s1) {
		// TODO Auto-generated method stub
		UniLog.log("WfmTaskBase WipVisNetWork Callback");
		switch(mode) {
		case WipVisNetwork.WIPVIS_CBMODE_START_CLICKED:
			try {
				setPanelDisplayMode( NODE_PROPERTY_STATE_START,null) ;
//				if(nW != null) nW.drawNetwork(new VectorUtil().addElement("Start").addElement("End").toVector());
				if(nW != null) nW.drawNetwork();
				
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			break;
		case WipVisNetwork.WIPVIS_CBMODE_END_CLICKED:
			try {
				setPanelDisplayMode( NODE_PROPERTY_STATE_END,null) ;
//				if(nW != null) nW.drawNetwork(new VectorUtil().addElement("Start").addElement("End").toVector());
				if(nW != null) nW.drawNetwork();
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			break;
		case WipVisNetwork.WIPVIS_CBMODE_NODE_CLICKED: 
			try {
				setPanelDisplayMode( NODE_PROPERTY_STATE_NODE,((WfmTask) s0).getId()) ;
//				if(nW != null) nW.drawNetwork(new VectorUtil().addElement(s0.getId()).toVector());
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			break;
		case WipVisNetwork.WIPVIS_CBMODE_ARROW_CLICKED: 
		case WipVisNetwork.WIPVIS_CBMODE_NONE_CLICKED: 
//			if(nW != null) nW.drawNetwork(null);
			try {
				setPanelDisplayMode( NODE_PROPERTY_STATE_END,null) ;
//				if(nW != null) nW.drawNetwork(new VectorUtil().addElement("Start").addElement("End").toVector());
				if(nW != null) nW.drawNetwork();
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			break;
		}
	}
	
	public void setDrawStartEnd(boolean p_sw) {
		if(nW != null) nW.setDrawStartEnd(p_sw);
	}
	public void setCompactSpacing(boolean p_sw) {
		if(nW != null) nW.setCompactSpacing(p_sw);
	}
}