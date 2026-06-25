package com.uniinformation.zkbi.wip;

import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Vlayout;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.wip.WfmEventData;
import com.uniinformation.erpv4.wip.WfmJob;
import com.uniinformation.erpv4.wip.WfmStep;
import com.uniinformation.erpv4.wip.WfmTask;
import com.uniinformation.erpv4.wip.WfmTaskBase;
import com.uniinformation.erpv4.wip.WfmTaskUpdate;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.wip.WipJob;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerTaskG2 extends ZkBiComposerReport {
	WfmTaskUpdate wipTaskUpdate;
	BiResult br;
	int currentIdx = -1;
	Component pplComp =null;
	Button submitButton=null;
	EventListener onClickListener = new EventListener()	 {

		@Override
		public void onEvent(Event event) throws Exception {
			UniLog.log1("got event:%s target:%s", event.getName(), event.getTarget());
			// TODO Auto-generated method stub
			if(event.getName().equals(WfmTaskUpdate.EV_ON_MESSAGE_CHANGED) ||
			   event.getName().equals(WfmTaskUpdate.EV_ON_TASKSTATE_CHANGED) ||
			   event.getName().equals(WfmTaskUpdate.EV_ON_JOBSTATE_CHANGED)
			   ) {
				if(wipTaskUpdate.stateChangedOrHasMessage()) {
					try {
						wipTaskUpdate.updateJobStatus(0, "");
					}
					catch (Exception e) {
						UniLog.log(e);
						ZkUtil.showErrMsg(StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
					}
					if(submitButton != null) submitButton.setDisabled(false);
				} else {
					if(submitButton != null) submitButton.setDisabled(true);
				}
				return;
			}
			if(event.getName().equals(Events.ON_CLICK)) {
				if(event.getTarget() instanceof Button) {
				Button bt = (Button) event.getTarget();
				if(bt.getId().equals("btBackToList")) {
			        wipTaskUpdate.setPanelDisplayMode(WfmTaskBase.NODE_PROPERTY_STATE_NONE, null);
			        clearSelection();
					showListPanel();
					pplComp.setVisible(false);
				}
				if(bt.getId().equals("btSubmitChange")) {
					try {
						wipTaskUpdate.updateJobStatus();
					}
					catch (Exception e) {
						UniLog.log(e);
						ZkUtil.showErrMsg(StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
					}
				}
				}
			}
		}
	};
   	public void doAfterCompose(final Component comp) throws Exception { 
   		/*
		String isMobile = Executions.getCurrent().getParameter("MobileMode");
		if(isMobile == null) {
			HttpServletRequest request = ((HttpServletRequest) Executions.getCurrent().getNativeRequest());
			String ua = request.getHeader("User-Agent");
			if(ua != null && ua.indexOf("Mobile") != -1) {
			UniLog.log("jump to mobile url");
				Executions.getCurrent().sendRedirect(
					"zkbiloader.html?action=browse&viewid=wip.WfmWipTask&page_id=WfmWipTask_01&zul=zkbiMobileLoader.zul&composer=wip.ZkBiComposerTask&MobileMode=Y"
				);
				return;
				
			}
		}
		*/
   		super.doAfterCompose(comp);
   		
   		//andrew2307070 hotfix allow mobile see message box without scrolling
   		if (sessionHelper.isMobile()) {
   			adjListboxHeight(65);
   		}
   		
		EventQueue eventQueue = EventQueues.lookup("WipNotify", EventQueues.APPLICATION, true);
		eventQueue.subscribe(new EventListener() {
            public void onEvent(Event event) throws Exception {
            	if (event.getName() != null && event.getName().equals("onWipNotify")){
            		WfmEventData wed = (WfmEventData) event.getData();
            		List<WfmEventData.TaskChangeInfo> tcArray = wed.getTaskStateChangeList();
			        for(int i=0;i<br.getRowCount();i++) {
			        	br.loadOneRecV(i);
			        	for(WfmEventData.TaskChangeInfo tci : tcArray) {
			        		if(br.getCellInt("wfmjt_rg") == tci.getTrg()) {
//			        			br.fetchOneRecV(i);
//			        			biBaseRefreshListitems(br.getCurrentRecord());
        				        br.reloadOneRecV(i);
        				        refreshListItems(br.getTrStatObj(i));
			        		}
			        	}
			        }
			        if(currentIdx >= 0) {
			        	if(br.getCurrentRecIdx() != currentIdx) {
			        		br.loadOneRecV(currentIdx);
			        		br.fetchOneRecV(currentIdx);
			        	}
			        	if(wed.getWfmrg() == br.getCellInt("wfmjt_frg")) {
			        		if(wed.isStateChanged() || wed.isNetworkChanged() || tcArray.size() > 0) {
			        			br.loadOneRecV(currentIdx);
			        			br.fetchOneRecV(currentIdx);
			        			wipTaskUpdate.loadWorkFlow(null,br.getSessionHelper(),br.getCellInt("wfmjt_frg"),0);
			        			wipTaskUpdate.setPanelDisplayMode(WfmTaskBase.NODE_PROPERTY_STATE_NODE, WfmStep.makeNodeId(br.getCellInt("wfmjt_rg")));
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
		super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
		br = result;
		if(comp.hasFellow("listDetailPanel")) {
			pplComp=comp.getFellow("listDetailPanel");
		} else {
			if(sessionHelper.isMobile()) {
				masterWin.setHeight("100%");
				pplComp = new Vlayout();
				pplComp.setId("listDetailPanel");
				((HtmlBasedComponent) pplComp).setVflex("1");
				zkbiListTop.getParent().insertBefore(pplComp,zkbiListTop);
			} else {
				Component lb = comp.getFellow("browser_listbox");
				Component pl = lb.getParent();
				Hlayout hl = new Hlayout();
				pl.insertBefore(hl, lb);
				lb.setParent(hl);
				pplComp = new Vlayout();
				pplComp.setId("listDetailPanel");
				//((HtmlBasedComponent) pplComp).setWidth("500px");
				//((HtmlBasedComponent) pplComp).setVflex("1");
				((HtmlBasedComponent) pplComp).setStyle("max-width:500px");
				((HtmlBasedComponent) pplComp).setHeight("calc(75vh + 30px)");
				//pplComp.setParent(hl);
				hl.appendChild(new Div() {{
					setHflex("1");
					appendChild(pplComp);
				}});
			}
			
		}
		{
			/*
				if(pplComp instanceof HtmlBasedComponent) {
					((HtmlBasedComponent) pplComp).setHflex(null);
					((HtmlBasedComponent) pplComp).setWidth("450px");
				}
			if(!result.getSessionHelper().isMobileDevice()) {
				if(pplComp instanceof HtmlBasedComponent) {
					((HtmlBasedComponent) pplComp).setHflex(null);
					((HtmlBasedComponent) pplComp).setWidth("450px");
				}
			}
			*/
			/*
			if(br.getSessionHelper().isMobileDevice()) {
			} else {
				wipTaskUpdate = new WipTaskUpdate(pplComp,false,onClickListener);
			}
			*/
			wipTaskUpdate = new WfmTaskUpdate(pplComp,false,onClickListener);

			//ZkForm zkf1 = new ZkForm(pplComp,"zkf/WfmProperties.zul");
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
		}
		/*
		if(sessionHelper.isMobileDevice()) {
			String isMobile = Executions.getCurrent().getParameter("MobileMode");
			if(isMobile == null || !isMobile.equals("Y")) {
			UniLog.log("jump to mobile url");
			Executions.getCurrent().sendRedirect(
					"zkbiloader.html?action=browse&viewid=wip.WfmWipTask&page_id=WfmWipTask_01&zul=zkbiMobileLoader.zul&composer=wip.ZkBiComposerTask&MobileMode=Y"
			);
			}
		}
		*/
	}
	@Override
	protected void doZkbiItemSelected(int p_idx,BiResult p_br) {
		UniLog.log("Record " + p_idx + " Selected");
		try {
			currentIdx = p_idx;
			if(p_idx >= 0) {
				int frg = p_br.getCellInt("wfmjt_frg");
				int trg = p_br.getCellInt("wfmjt_rg");
				p_br.fetchOneRecV(p_idx);
				wipTaskUpdate.loadWorkFlow(/* p_br */ null,p_br.getSessionHelper(),frg,trg);
			    wipTaskUpdate.setPanelDisplayMode(WfmTaskBase.NODE_PROPERTY_STATE_NODE, WfmStep.makeNodeId(trg));
				if(br.getSessionHelper().isMobileDevice()) {
					hideListPanel();
					pplComp.setVisible(true);
				}
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

	/*
	@Override
    protected void setupExtraButton(final BiResult result)
	{
		super.setupExtraButton(result);
		Button btnScan;
        btnScan = new ZkBiButton();
        btnScan.setLabel("Scan");
        btnScan.setId("btScan");
//        btnCopyOrder.setDisabled(true);
        btnScan.addEventListener("onClick",
            new EventListener() {
            	public void onEvent(Event event) throws Exception {

//   		 			ZkUtil.js("zkDevice.launchScanner()"); 
   		 			ZkUtil.js("zkDevice.launchPhotoCapture()"); 
        		}
        	}
        );
        actionBar.appendChild(btnScan);
		
	}
	*/

	@Override
	protected void onAddPhoto(BiResult p_result,org.zkoss.image.Image p_image) {
		wipTaskUpdate.addPhoto(p_image);
	}

	public WfmTaskUpdate getWipTaskUpdate() {
		return(wipTaskUpdate);
	}
}
