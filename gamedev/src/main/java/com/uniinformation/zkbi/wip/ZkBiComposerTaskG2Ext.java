package com.uniinformation.zkbi.wip;

import java.net.URLDecoder;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.ClientInfoEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.erpv4.wip.WfmEventData;
import com.uniinformation.erpv4.wip.WfmJob;
import com.uniinformation.erpv4.wip.WfmStep;
import com.uniinformation.erpv4.wip.WfmTask;
import com.uniinformation.erpv4.wip.WfmTaskBase;
import com.uniinformation.erpv4.wip.WfmTaskUpdate;
import com.uniinformation.utils.URLParamHash;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.wip.WipJob;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerTaskG2Ext extends ZkComposerBase {
	/*@Wire
	Label jobId;
	@Wire
	Label taskId;*/

	private Component pplComp;
	private WfmTaskUpdate wipTaskUpdate;

	private int jobRg, taskRg;
	
	
	@Override
	protected boolean validateURL(String p_url) {
		UniLog.log1("skip validate url temporary");
		//andrew231020 handle the security validation later. this page need to accessed by external user
		return true;
	}
	@Override
	public void doAfterCompose(final Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("called");
		UniLog.log1("jobid:%s taskid:%s, rootComp:%s, url:%s", sessionHelper.getURLParam("jobid"), sessionHelper.getURLParam("taskid"), p_comp, ZkUtil.getURL());
		
		if (URLParamHash.validateParamHash(ZkUtil.getURL(), "jobid", "taskid").isBad()) {
			ZkUtil.errMsg("Access denided - Invalid url");
			return;
		}

		String jobid = StringUtils.defaultString(sessionHelper.getURLParam("jobid"));
		jobRg = 0;
		taskRg = NumberUtils.toInt(sessionHelper.getURLParam("taskid"));
		int rev = NumberUtils.toInt(sessionHelper.getURLParam("rev"));
		if (!jobid.contains("."))
			jobid += "." + rev;
		
		if (p_comp.hasFellow("listPropertyPanel"))
			pplComp = p_comp.getFellow("listPropertyPanel");
		else {
			pplComp = new Div();
			pplComp.setId("listPropertyPanel");
			((HtmlBasedComponent) pplComp).setVflex("1");
			p_comp.appendChild(pplComp);
		}
		pplComp.setVisible(false);

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
						
						UniLog.log1("event:%s, getWfmrg:%d, jobRg:%d, taskRg:%d, isStateChanged:%b, isNetworkChanged:%b, isMessageAdded:%b, size:%d", event, wed.getWfmrg(), jobRg, taskRg, wed.isStateChanged(), wed.isNetworkChanged(), wed.isMessageAdded(), wed.getTaskStateChangeList().size());
						if (wed.getWfmrg() == jobRg && taskRg > 0) {
							if (wed.isStateChanged() || wed.isNetworkChanged() || !wed.getTaskStateChangeList().isEmpty())
								showWipTaskUpdateView(false);
							else if (wed.isMessageAdded())
								showWipTaskUpdateView(true);
						}
					}
				}
			});
			
			p_comp.addEventListener(Events.ON_CLIENT_INFO, new EventListener<ClientInfoEvent>() {
				@Override
				public void onEvent(ClientInfoEvent event) throws Exception {
					UniLog.log1("event:%s, target:%s, desktopHeight:%d", event, event.getTarget(), event.getDesktopHeight(), sessionHelper.getDesktopHeight());
					((Window)p_comp).setHeight(event.getDesktopHeight() + "px");
				}
			});
		}
		
		if (StringUtils.isNotBlank(jobid) && taskRg > 0) {
			BiResult br = BiResultHelper.create(sessionHelper, "wip.WfmWipJob", String.format("wfmj_id = '%s'", jobid), -1, null);
			if (br != null && br.getRowCount() > 0) {
				br.loadOneRecV(0);
				jobRg = br.getCellInt("wfmj_rg");
				String errMsg = showWipTaskUpdateView(false);
				if (errMsg != null)
					ZkUtil.errMsg(errMsg);
			} else
				ZkUtil.errMsg("Record not found");
		} else
			ZkUtil.errMsg("Job Id or Task Id can not be empty");
	}

	private String showWipTaskUpdateView(boolean handleMessageViewOnly) {
		UniLog.log1("showWipTaskUpdateView frg:%d, srg:%d, handleMessageViewOnly:%b", jobRg, taskRg, handleMessageViewOnly);
		if (pplComp == null)
			return null;
		if (jobRg > 0 && taskRg > 0) {
			try {
				if (handleMessageViewOnly)
					wipTaskUpdate.reloadActivity();
				else {
					wipTaskUpdate.loadWorkFlow(null, sessionHelper, jobRg, taskRg);
					wipTaskUpdate.setPanelDisplayMode(WfmTaskBase.NODE_PROPERTY_STATE_NODE, WfmStep.makeNodeId(taskRg));
				}
				pplComp.setVisible(true);
			} catch (Exception e) {
				UniLog.log(e);
				pplComp.setVisible(false);
				return StringUtils.defaultIfBlank(e.getMessage(), e.toString());
			}
		} else {
			pplComp.setVisible(false);
	    	try {
				wipTaskUpdate.setPanelDisplayMode(WfmTaskBase.NODE_PROPERTY_STATE_NONE, null);
			} catch (Exception e) {
				UniLog.log(e);
				return StringUtils.defaultIfBlank(e.getMessage(), e.toString());
			}
		}
		return null;
	}
}
