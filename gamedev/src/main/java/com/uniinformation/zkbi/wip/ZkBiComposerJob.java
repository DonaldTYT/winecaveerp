package com.uniinformation.zkbi.wip;


import java.net.URLDecoder;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Tabpanels;
import org.zkoss.zul.Tabs;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.XulElement;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.wip.WfmEventData;
import com.uniinformation.erpv4.wip.WfmJob;
import com.uniinformation.erpv4.wip.WfmTask;
import com.uniinformation.erpv4.wip.WfmTaskBase;
import com.uniinformation.erpv4.wip.WfmTaskUpdate;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.wip.WipException;
import com.uniinformation.wip.WipGetTaskListInterface;
import com.uniinformation.wip.WipStep;
import com.uniinformation.wip.WipVisCallbackInterface;
import com.uniinformation.wip.WipVisNetwork;
import com.uniinformation.wip.WipVisTimeline;
import com.uniinformation.zkbi.ZkBiComposerReport;

public class ZkBiComposerJob extends ZkBiComposerReport implements WipVisCallbackInterface  {
//	WfmJob wj;
	WfmTaskUpdate wipTaskUpdate;
	BiResult br;
	int currentIdx = -1;
	Component pplComp =null;
	Button submitButton=null;
	WipVisTimeline visTimeline = null;
//	WipVisNetwork	nW = null;
	EventListener onClickListener = new EventListener()	 {

		@Override
		public void onEvent(Event arg0) throws Exception {
			// TODO Auto-generated method stub
			if(br.getCurrentRecIdx() != currentIdx) {
				br.fetchOneRecV(currentIdx);
			}
			if(arg0.getName().equals(WfmTaskUpdate.EV_ON_MESSAGE_CHANGED)) {
				if(wipTaskUpdate.stateChangedOrHasMessage()) {
					if(submitButton != null) submitButton.setDisabled(false);
				} else {
					if(submitButton != null) submitButton.setDisabled(true);
				}
				return;
			}
			if(arg0.getName().equals(WfmTaskUpdate.EV_ON_TASKSTATE_CHANGED)) {
				WfmTask t = (WfmTask) arg0.getData();
				if(wipTaskUpdate.stateChangedOrHasMessage()) {
					if(submitButton != null) submitButton.setDisabled(false);
				} else {
					if(submitButton != null) submitButton.setDisabled(true);
				}
				return;
			}
			if(arg0.getName().equals(WfmTaskUpdate.EV_ON_JOBSTATE_CHANGED)) {
				WfmJob t = (WfmJob) arg0.getData();
				if(t.getRg() == br.getCellInt("wfmj_rg")) {
					if(wipTaskUpdate.stateChangedOrHasMessage()) {
						if(submitButton != null) submitButton.setDisabled(false);
					} else {
						if(submitButton != null) submitButton.setDisabled(true);
					}
				} else {
					throw new Exception ("Unexpected Error : WfmTask not matched with BiResult Current Record");
				}
				return;
			}
			if(arg0.getName().equals(Events.ON_CLICK)) {
				Button bt = (Button) arg0.getTarget();
				if(bt.getId().equals("btBackToList")) {
			        wipTaskUpdate.setPanelDisplayMode(WfmTaskBase.NODE_PROPERTY_STATE_NONE, null);
			        clearSelection();
					showListPanel();
					pplComp.setVisible(false);
				}
				if(bt.getId().equals("btSubmitChange")) {
					wipTaskUpdate.updateJobStatus();
				}
			}
		}
	};
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
		String isMobile = Executions.getCurrent().getParameter("MobileMode");
		if(isMobile == null) {
			HttpServletRequest request = ((HttpServletRequest) Executions.getCurrent().getNativeRequest());
			String ua = request.getHeader("User-Agent");
			if(ua != null && ua.indexOf("Mobile") != -1) {
			UniLog.log("jump to mobile url");
				Executions.getCurrent().sendRedirect(
					"zkbiloader.html?action=browse&viewid=wip.WfmWipJob&page_id=WfmWipJob_01&zul=zkbiMobileLoader.zul&composer=wip.ZkBiComposerJob&MobileMode=Y"
				);
				return;
				
			}
		}
   		super.doAfterCompose(comp);
		EventQueue eventQueue = EventQueues.lookup("WipNotify", EventQueues.APPLICATION, true);
		eventQueue.subscribe(new EventListener() {
            public void onEvent(Event event) throws Exception {
            	if (event.getName() != null && event.getName().equals("onWipNotify")){
            		WfmEventData wed = (WfmEventData) event.getData();
			        for(int i=0;i<br.getRowCount();i++) {
			        	br.loadOneRecV(i);
			        	if(br.getCellInt("wfmj_rg") == wed.getWfmrg()) {
			        		br.fetchOneRecV(i);
			        		biBaseRefreshListitems(br.getCurrentRecord());
			        	}
			        }
			        if(currentIdx >= 0) {
			        	if(br.getCurrentRecIdx() != currentIdx) {
			        		br.loadOneRecV(currentIdx);
			        		br.fetchOneRecV(currentIdx);
			        	}
			        	if(wed.getWfmrg() == br.getCellInt("wfmj_rg")) {
			        		if(wed.isStateChanged() || wed.isNetworkChanged() || wed.getTaskStateChangeList().size() > 0) {
			        			br.loadOneRecV(currentIdx);
			        			br.fetchOneRecV(currentIdx);
			        			wipTaskUpdate.loadWorkFlow(br,br.getSessionHelper(),br.getCellInt("wfmj_rg"),0);
			        			wipTaskUpdate.setPanelDisplayMode(WfmTaskBase.NODE_PROPERTY_STATE_START, null);
			        		}
			        		if(wed.isMessageAdded()) {
			        			wipTaskUpdate.reloadActivity();
			        		}
			        	}
			        }
            	}
            }
        });
   	}
	@Override
	public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		if(!result.getSessionHelper().isMobileDevice()) hasAUDColumn=true;
		super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
		br = result;
		if(comp.hasFellow("listDetailPanel")) {
			pplComp=comp.getFellow("listDetailPanel");
			if(!result.getSessionHelper().isMobileDevice()) {
				if(pplComp instanceof HtmlBasedComponent) {
					((HtmlBasedComponent) pplComp).setHflex(null);
					((HtmlBasedComponent) pplComp).setWidth("450px");
				}
			}
////			Executions.getCurrent().createComponents("zkf/WfmNetwork.zul", pplComp,null);
//			if(pplComp.hasFellow("visNwEventDiv")) {
//				HtmlBasedComponent visNwEventDiv = (HtmlBasedComponent) pplComp.getFellow("visNwEventDiv");
//				visNwEventDiv.setHeight("150px");
//				/*
//				try {
//					nW = new WipVisNetwork(visNwEventDiv, "visNwContentDiv",null, null,null,this);
//				} catch (WipException wex) {
//					UniLog.log(wex);
//				}
//				*/
//			}
			
			wipTaskUpdate = new WfmTaskUpdate(pplComp,true,onClickListener);
			wipTaskUpdate.setCompactSpacing(true);
			wipTaskUpdate.setDrawStartEnd(false);
			if(br.getSessionHelper().isMobileDevice()) {
				pplComp.setVisible(false);
			}
			if(pplComp.hasFellow("btSubmitChange")) {
				submitButton = (Button) comp.getFellow("btSubmitChange");
			}
			if(pplComp.hasFellow("btBackToList")) {
				Button bt = (Button) comp.getFellow("btBackToList");
				if(result.getSessionHelper().isMobileDevice()) {
					bt.setVisible(true);
				} else {
					bt.setVisible(false);
				}
			}
			if(pplComp.hasFellow("zkBiTabPanels")
			   && pplComp.hasFellow("zkBiTabs")) {
				Tabs tabs = (Tabs) pplComp.getFellow("zkBiTabs");
				Tabpanels tbls = (Tabpanels) pplComp.getFellow("zkBiTabPanels");
				Tab tab = new Tab("Time Line");
				tabs.appendChild(tab);
				Tabpanel tpl = new Tabpanel();
				tpl.setHeight("500px");
				tbls.appendChild(tpl);
				Executions.getCurrent().createComponents("zkf/VisJsContainer.zul", tpl,null);
				Component eventDiv = null;
				if(tbls.hasFellow("visJsEventDiv"))  {
					eventDiv = tbls.getFellow("visJsEventDiv");
					eventDiv.setId("visTlEventDiv01");
				}
				/*
				if(tbls.hasFellow("visJsContentDiv"))  {
					tbls.getFellow("visJsContentDiv").setId("visTlContentDiv01");
				}
				*/
				if(eventDiv != null) {
					visTimeline = new WipVisTimeline(
							new WipGetTaskListInterface() {

								@Override
								public int getCount() {
									// TODO Auto-generated method stub
									return(result.getRowCount());
								}

								@Override
								public Date getStart(int p_idx) {
									// TODO Auto-generated method stub
									result.loadOneRecV(p_idx);
									return(result.getCell("wfmj_starttime").getDate());
								}

								@Override
								public Date getEnd(int p_idx) {
									// TODO Auto-generated method stub
									result.loadOneRecV(p_idx);
									return(result.getCell("wfmj_endtime").getDate());
								}

								@Override
								public String getId(int p_idx) {
									// TODO Auto-generated method stub
									result.loadOneRecV(p_idx);
									return(result.getCell("wfmj_id").getString());
								}

								@Override
								public String getName(int p_idx) {
									// TODO Auto-generated method stub
									result.loadOneRecV(p_idx);
									return(result.getCell("wfmj_name").getString());
								}

								@Override
								public String getTitle(int p_idx) {
									// TODO Auto-generated method stub
									result.loadOneRecV(p_idx);
									return(result.getCell("wfmj_title").getString());
								}
								
							}
//							, eventDiv,"visTlContentDiv01");
							, eventDiv,"visJsContentDiv");
					tab.addEventListener(Events.ON_CLICK, 
						new EventListener() {
							@Override
							public void onEvent(Event arg0) throws Exception {
								UniLog.log("HAHA tl cicked");
								visTimeline.redrawTimeline(0);
							}	
						}
						
					);
				}
			}
		}
	}
	
    @Override
    public boolean doBrowseItemSelected(XulElement p_win, BiResult p_result)
    {
		if(!p_result.getSessionHelper().isMobileDevice()) return(doUpdateOneRow(p_win,p_result)) ; else return(true);
    }
    
	@Override
	protected void doZkbiItemSelected(int p_idx,BiResult p_br) {
		UniLog.log("Record " + p_idx + " Selected");
		try {
			currentIdx = p_idx;
			if(p_idx >= 0) {
				int frg = p_br.getCellInt("wfmj_rg");
				p_br.fetchOneRecV(p_idx);
				wipTaskUpdate.loadWorkFlow(p_br,p_br.getSessionHelper(),frg,0);
			    wipTaskUpdate.setPanelDisplayMode(WfmTaskBase.NODE_PROPERTY_STATE_START, null);
				if(br.getSessionHelper().isMobileDevice()) {
					hideListPanel();
					headerBox.setVisible(false);
					pplComp.setVisible(true);
				}
				if(submitButton != null) submitButton.setDisabled(true);
			} else {
			    wipTaskUpdate.setPanelDisplayMode(WfmTaskBase.NODE_PROPERTY_STATE_NONE, null);
				if(br.getSessionHelper().isMobileDevice()) {
					showListPanel();
					pplComp.setVisible(false);
				}
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	@Override
	public void visActionCallBack(int mode, WipStep s0, WipStep s1) {
		// TODO Auto-generated method stub
		
	}
}