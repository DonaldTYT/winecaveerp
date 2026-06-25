package com.uniinformation.erpv4.wip;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
//import org.zkoss.image.Image;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Textbox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.Base64Util;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.wip.WipJob;
import com.uniinformation.wip.WipStep;
import com.uniinformation.wip.WipTask;
import com.uniinformation.wip.WipVisNetwork;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkf.ZkForm;

public class WfmTaskUpdate extends WfmTaskBase {
	public static final String EV_ON_MESSAGE_CHANGED = "ON_MESSAGE_CHANGED";
	BiResult brFlow = null;
	BiResult brJobTask = null;
//	BiResult brTask = null;
	BiResult brTaskTask = null;
	int currentRecIdx = 0;
	WfmActivity wfmA = null;
	String loginId;
	Listbox wfmaListBox = null;
	Textbox wfmMessage = null;
	Button btSubmitChange = null;
	Button btUpload = null;
	Label nodeprop_taskid = null;
	org.zkoss.zul.Image wfmImage = null;
	
//	Button submitButton=null;
	
	private static int countLines(String str){
		   String[] lines = str.split("\r\n|\r|\n");
		   return  lines.length;
		}
	
	public WfmTaskUpdate(Component comp,boolean p_hasVisNetwork ,EventListener p_onClickListener) {
		super(comp,p_hasVisNetwork,p_onClickListener);
		if(comp != null) {
			if(comp.hasFellow("wfmActivity")) {
				wfmaListBox = (Listbox) comp.getFellow("wfmActivity");
			}
			if(comp.hasFellow("nodeprop_taskid")) {
				nodeprop_taskid = (Label) comp.getFellow("nodeprop_taskid");
			}
			if(comp.hasFellow("wfmMessage")) {
				wfmMessage = (Textbox) comp.getFellow("wfmMessage");
				wfmMessage.setInstant(true);
				wfmMessage.addEventListener(Events.ON_CHANGE, 
					new EventListener() {
						@Override
						public void onEvent(Event arg0) throws Exception {
							// TODO Auto-generated method stub
							onClickListener.onEvent(new Event(EV_ON_MESSAGE_CHANGED,null,getWipJob()));
						}
			
					}
				);
			}
			if(comp.hasFellow("wfmImage")) {
				wfmImage = (org.zkoss.zul.Image) comp.getFellow("wfmImage");
				wfmImage.addEventListener(Events.ON_CHANGE, 
					new EventListener() {
						@Override
						public void onEvent(Event arg0) throws Exception {
							// TODO Auto-generated method stub
							onClickListener.onEvent(new Event(EV_ON_MESSAGE_CHANGED,null,getWipJob()));
						}

					}
				);
			}
			if (comp.hasFellow("btSubmitChange"))
				btSubmitChange = (Button) comp.getFellow("btSubmitChange");
			if (comp.hasFellow("btUpload")) {
				btUpload = (Button) comp.getFellow("btUpload");
				final SessionHelper sh = ZkSessionHelper.getSessionHelper();
				int maxUploadSize = Erpv4Config.getInteger(sh, "MaxWfmUploadSize", 0);
				UniLog.log1("maxUploadSize:%d", maxUploadSize);
				if (maxUploadSize > 0) {
					btUpload.setVisible(true);
					btUpload.setUpload(String.format("true,maxsize=%d,multiple=false,accept=image/*|*/*,native", maxUploadSize));
					btUpload.addEventListener(Events.ON_UPLOAD, new ZkBiEventListener<UploadEvent>() {
						@Override
						public void onZkBiEvent(UploadEvent event) throws Exception {
							try {
								org.zkoss.util.media.Media media = event.getMedia();
								InputStream mediaStream = media.getStreamData();
								int rg = wfmA.newRg();
								UniLog.log1("media conentType:%s, format:%s, isBinary:%b, name:%s, mediaSize:%s, rg:%d", media.getContentType(), media.getFormat(), media.isBinary(), media.getName(), mediaStream.available(), rg);
								//Store file to mysql filing db
								//String flKey = String.format("zkbi_wfmact_%010d_%s", rg, Base64Util.encode(media.getName()));
								String flKey = String.format("zkbi_wfmact_%010d", rg); //andrew231101 remove filename from filing key
								String flMsg = String.format("FILING://zkbi_wfmact_%010d_%s", rg, Base64Util.encode(media.getName())); //msg carry b64 filename
								String flName = media.getName();
								FilingUtil.storeFile(sh.getAgent(), null, flKey, flName, flName, mediaStream);
								//Store the filing key in msg content with a special prefix 
								//updateJobStatus(rg, "FILING://" + flKey);
								updateJobStatus(rg, flMsg);
								ZkUtil.showMsg("File uploaded");
							} catch (Exception e) {
								UniLog.log(e);
								ZkUtil.showErrMsg(StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
							}
						}
					});
				} else
					btUpload.setVisible(false);
			}
		}
	}
	@Override
	protected void notifyStateChanged() {	
	}

	public boolean stateChangedOrHasMessage() throws Exception {
		if(stateChanged()) return(true);
		//if(wfmMessage != null && !wfmMessage.getText().equals("")) return(true);
		if(wfmMessage != null && StringUtils.isNotBlank(wfmMessage.getText())) return(true);
		return(false);
	}
	boolean stateChanged() throws Exception {
		if(brFlow.getCellInt("wfmj_rg") != getWipJob().getRg()) throw new Exception("check Job State Error : job no not equal last loaded jobno");
		if(brFlow.getCellInt("wfmj_state") != getWipJob().getState()) return(true);
		for(CellCollection col : brJobTask.getRowCollectionList()) {
			WfmTask task = (WfmTask) getWipJob().getTask(WfmStep.makeNodeId(col.getCellInt("wfmjt_rg")));
			if(col.getCellInt("wfmjt_state") != task.getState()) return(true);
			if(col.getCellInt("wfmjt_choice") != task.getChoice()) return(true);
		}
		return(false);
	}
	
	public WfmJob loadWorkFlow(BiResult p_br,SessionHelper p_sh,int p_frg,int p_srg) throws Exception {
		return loadWorkFlow(p_br, p_sh, p_frg, p_srg, true);
	}

	/***
	 * 
	 * @param p_br jobBr
	 * @param p_sh sessionHelper
	 * @param p_frg jobrg e.g. WfmWipJob.wfmj_rg WfmJobTask.wfmjt_frg
	 * @param p_srg jobtaskrg e.g. WfmJobTask.wfmjt_rg
	 * @param p_usePanelTaskId
	 * @return
	 * @throws Exception
	 */
	public WfmJob loadWorkFlow(BiResult p_br,SessionHelper p_sh,int p_frg,int p_srg, boolean p_usePanelTaskId) throws Exception {
			if(p_frg < 0) {
				setPanelDisplayMode(WfmTaskBase.NODE_PROPERTY_STATE_NONE,null);
				return(null);
			}
			
			
			WfmJob twj = null;
			brFlow = p_br;
			loginId = p_sh.getLoginId();
			BiSchema schema = null;
			if(brFlow == null) {
				if(schema == null) schema = BiSchema.loadSchema(p_sh);
				brFlow =  schema.getViewByName("wip.WfmWipJob").newBiResult(null,loginId, null, null, p_sh);
				//				brFlow.setUseTransaction(false);
				brFlow.clear();
				brFlow.clearCondition();
				brFlow.addCustomCondition("wfmj_rg = " + p_frg);
				brFlow.query();
				brFlow.loadOneRecV(0);
				brFlow.fetchOneRecV(0);
				currentRecIdx = 0;
			} 
			else {
				if(brFlow.getCellInt("wfmj_rg") != p_frg) throw new Exception("Error p_frg not match wfmj_rg in biResult");
				currentRecIdx = brFlow.getCurrentRecIdx();
			}
			brJobTask = brFlow.getSubLink("wip.WfmJobTask");
			twj = new WfmJob(brFlow.getCellInt("wfmj_rg"),brFlow.getCellString("wfmj_id"),brFlow.getCellString("wfmj_viewid"),brFlow.getCellString("wfmj_keyfd"),brFlow.getCellString("wfmj_keystr"));
			twj.setDeadLine(brFlow.getCell("wfmj_timeout").getDate());
			twj.setState(brFlow.getCellInt("wfmj_state"));
			twj.setName(brFlow.getCellString("wfmj_name"));
			twj.setTitle(brFlow.getCellString("wfmj_title"));
			twj.setStart(brFlow.getCell("wfmj_starttime").getDate());
			twj.setEnd(brFlow.getCell("wfmj_endtime").getDate());
			twj.setStartBy(brFlow.getCell("wfmj_startby").getString());
			twj.setEndBy(brFlow.getCell("wfmj_endby").getString());
//			twj.setState(brFlow.getCellInt("wfmj_state"));

			for(CellCollection col : brJobTask.getRowCollectionList()) {
				WfmTask task = new WfmTask(col.getCellInt("wfmjt_rg"),col.getCell("wfmjt_desc").getString(),col.getCellInt("wfmjt_order"),col.getCellString("wfmjt_createcond"));
				task.setState(col.getCellInt("wfmjt_state"));
				task.setStart(col.getCell("wfmjt_starttime").getDate());
				task.setEnd(col.getCell("wfmjt_endtime").getDate());
				task.setStartBy(col.getCell("wfmjt_startby").getString());
				task.setEndBy(col.getCell("wfmjt_endby").getString());
				task.setChoice(col.getCell("wfmjt_choice").getInt());
				String ss = col.getCell("wfmjt_type").getString();
				if("PROGRESS".equals(ss)) {
					task.setType(WfmStep.WFMTYPE_PROGRESS);
				} else if("CHOICES".equals(ss)) {
					task.setType(WfmStep.WFMTYPE_CHOICES);
				} else  if("ACTION".equals(ss)) {
					task.setType(WfmStep.WFMTYPE_ACTION);
				}
				switch(task.getType()) {
				case WfmStep.WFMTYPE_PROGRESS: 
				case WfmStep.WFMTYPE_CHOICES: {
						Vector<String> choiceList = new Vector<String>();
						choiceList.add("N/A");
						for(int i=1;i<=4;i++) {
							ss = col.getCell("wfmjt_choice"+i).getString();
							if(StringUtils.isBlank(ss)) break;
							choiceList.add(ss);
						}
						((WfmTask) task).setChoiceList(choiceList);
					}
					break;
				}
				twj.addStep(task);
			}
			
			if(brTaskTask == null) {
				if(schema == null) schema = BiSchema.loadSchema(p_sh);
				brTaskTask =  schema.getViewByName("wip.WfmTaskTask").newBiResult(brFlow.getSelectUtil(),loginId, null, null, p_sh);
				brTaskTask.setUseTransaction(false);
			}
			brTaskTask.clear();
			brTaskTask.clearCondition();
			brTaskTask.addCustomCondition("wfmtt_frg = " + brFlow.getCellInt("wfmj_rg"));
			brTaskTask.query();
			for(int i=0;i<brTaskTask.getRowCount();i++) {
				brTaskTask.loadOneRecV(i);
				String n0 = WfmStep.makeNodeId(brTaskTask.getCellInt("wfmtt_fromrg"));
				String n1 = WfmStep.makeNodeId(brTaskTask.getCellInt("wfmtt_torg"));
				WipStep t0 = twj.getStep(n0);
				WipStep t1 = twj.getStep(n1);
				if(t0 != null && t1 != null) {
					twj.addPreq(t0, t1);
					String ss = brTaskTask.getCellString("wfmtt_condition");
					if(!StringUtils.isBlank(ss)) {
						twj.setLinkData(t0, t1, ss);
					}
				}
			}
			
			if(wfmA == null) {
				wfmA = new WfmActivity(brFlow.getSelectUtil(),p_sh,wfmaListBox,wfmMessage);
			}

			int srg = p_srg;
			if(srg == 0 && p_usePanelTaskId) {
				if(nodeprop_taskid != null) {
					String ss = nodeprop_taskid.getValue();
					if(!StringUtils.isBlank(ss)) {
						srg = WfmTask.getRgFromNodeId(ss);
					}
				}
			}
			wfmA.reloadActivity(p_frg,srg);
			setWipJob(twj);
			
			return(twj);
	}
	
	public void reloadActivity() throws CellException {
		if(wfmA != null) {
			int srg = 0;
			if(nodeprop_taskid != null) {
				String ss = nodeprop_taskid.getValue();
				if(!StringUtils.isBlank(ss)) {
					srg = WfmTask.getRgFromNodeId(ss);
				}
			}
			wfmA.reloadActivity(getWipJob().getRg(),srg);
		}
	}
	
	public ReturnMsg updateJobNetwork() throws Exception {
		brFlow.beginWork();
		BiResult sr = brFlow.getSubLink("wip.WfmJobTask");
		Vector<BiCellCollection> v = sr.getRowCollectionList();
		try {
		for(BiCellCollection cl : v) {
			cl.getCell("wfmjt_seq").set(getWipJob().getStep(WfmStep.makeNodeId(cl.getCellInt("wfmjt_rg"))).getLevel());
		}
		} catch (CellException cex) {
			UniLog.log(cex);
		}
		ReturnMsg rtn = brFlow.updateCurrent();
		if(rtn != null && !rtn.getStatus()) {
			brFlow.rollbackWork();
			return(rtn);
		}
		updateNetWorkLink(brFlow,"wfmtasktask","wfmtt_","wfmj_",getWipJob());
		brFlow.commitWork();
		return(ReturnMsg.defaultOk);
	}
	public void updateJobStatus() throws Exception {
		updateJobStatus(0, null);
	}
	public void updateJobStatus(int p_rg, String p_message) throws Exception {
		if(brFlow.getCurrentRecIdx() != currentRecIdx) {
			UniLog.log1("bridx:%d curidx:%d. fetch record:%d ", brFlow.getCurrentRecIdx(), currentRecIdx, currentRecIdx);
			brFlow.fetchOneRecV(currentRecIdx);
		}
		WfmEventData wed = null;
		String message = null;
		if (p_message != null) {
			message = p_message;
		}
		else {
			if (wfmMessage != null) {
				message = wfmMessage.getText();
			}
			if (StringUtils.startsWith(message, "FILING://"))
				throw new Exception("Invalid message content");
		}
		if (StringUtils.isNotBlank(message)) {
			if(wed == null) wed = new WfmEventData(getWipJob().getRg());
			wed.setMessageAdded(true);
			int srg = 0;
			if(nodeprop_taskid != null) {
				String ss = nodeprop_taskid.getValue();
				if(!StringUtils.isBlank(ss)) {
					srg = WfmTask.getRgFromNodeId(ss);
				}
			}
			wfmA.logActivty(loginId,p_rg,getWipJob().getRg(),srg,WfmActivity.WFMACTIVITY_TYPE_MESSAGE,message, 0,"",
				brFlow.getCell("wfmj_timeout").getDate(), getWipJob().getDeadLine(), 
				brFlow.getCell("wfmj_state").getInt(), getWipJob().getState(), 
				brFlow.getCell("wfmj_starttime").getDate(), getWipJob().getStart(), 
				brFlow.getCell("wfmj_endtime").getDate(), getWipJob().getEnd(), 
				brFlow.getCell("wfmj_startby").getString(), getWipJob().getStartBy(), 
				brFlow.getCell("wfmj_endby").getString(), getWipJob().getEndBy(), 
				"",""
				);
			if (p_message == null && wfmMessage != null) {
				wfmMessage.setText("");
			}
			if (btSubmitChange != null) {
				btSubmitChange.setDisabled(true);
			}
		}
		if(!stateChanged()) {
			if(wed != null) { 
				EventQueue que = EventQueues.lookup("WipNotify", EventQueues.APPLICATION, true);
				que.publish(new Event("onWipNotify", null,wed));
			}
			return;
		}
		brFlow.beginWork();
		if(wed == null) wed = new WfmEventData(getWipJob().getRg());
		brFlow.getCell("wfmj_updtime").set(new Date());
		brFlow.getCell("wfmj_updstr").set("");
		int newState = getWipJob().getState();
		Date old_timeout = brFlow.getCell("wfmj_timeout").getDate();
		int old_state = brFlow.getCell("wfmj_state").getInt();
		Date old_starttime = brFlow.getCell("wfmj_starttime").getDate();
		Date old_endtime = brFlow.getCell("wfmj_endtime").getDate();
		String old_startby = brFlow.getCell("wfmj_startby").getString();
		String old_endby = brFlow.getCell("wfmj_endby").getString();
		String job_content = null;
		if( newState != brFlow.getCellInt("wfmj_state")) {
			wed.setStateChanged(true);
			Date d = getWipJob().getStart();
			if(newState == WipJob.JOB_STATE_STARTED || newState == WipJob.JOB_STATE_COMPLETED) {
				if(d == null || !d.after(DateUtil.minTime)) {
					getWipJob().setStart(new Date());
					getWipJob().setStartBy(loginId);
				}
			} else {
				if(d != null && d.after(DateUtil.minTime)) {
					getWipJob().setStart(DateUtil.zeroTime);
					getWipJob().setStartBy("");
				}
			}
			d = getWipJob().getEnd();
			if(newState == WipJob.JOB_STATE_COMPLETED) {
				if(d == null || !d.after(DateUtil.minTime)) {
					getWipJob().setEnd(new Date());
					getWipJob().setEndBy(loginId);
				}
			} else {
				if(d != null && d.after(DateUtil.minTime)) {
					getWipJob().setEnd(DateUtil.zeroTime);
					getWipJob().setEndBy("");
				}
			}
			//String content="State Changed";
			String content = String.format("Job[%s] state changed", getWipJob().getId());
			switch(getWipJob().getState()) {
			case WipJob.JOB_STATE_COMPLETED:
				//content = getWipJob().getId() + " Completed";
				content = String.format("Job[%s] completed", getWipJob().getId());
				break;
			case WipJob.JOB_STATE_STARTED:
				switch( brFlow.getCellInt("wfmj_state"))  {
				case WipJob.JOB_STATE_COMPLETED:
					//content = getWipJob().getId() + " revert to started";
					content = String.format("Job[%s] revert to started", getWipJob().getId());
					break;
				default:
					//content = getWipJob().getId() + " started";
					content = String.format("Job[%s] started", getWipJob().getId());
					break;
				}
				break;
			case WipJob.JOB_STATE_AWAKED:
				switch( brFlow.getCellInt("wfmj_state"))  {
				case WipJob.JOB_STATE_STARTED:
				case WipJob.JOB_STATE_COMPLETED:
					//content = getWipJob().getId() + " revert to ready";
					content = String.format("Job[%s] revert to ready", getWipJob().getId());
					break;
				default:
					//content = getWipJob().getId() + " ready to start";
					content = String.format("Job[%s] ready to start", getWipJob().getId());
					break;
				}
				break;
			case WipJob.JOB_STATE_WAITING:
				//content = getWipJob().getId() + " revert to waiting";
				content = String.format("Job[%s] revert to waiting", getWipJob().getId());
				break;
			}
			job_content = content;
			/*wfmA.logActivty(loginId,0,getWipJob().getRg(),0,WfmActivity.WFMACTIVITY_TYPE_STATECHANGE,content, 0,"",
				brFlow.getCell("wfmj_timeout").getDate(), getWipJob().getDeadLine(), 
				brFlow.getCell("wfmj_state").getInt(), getWipJob().getState(), 
				brFlow.getCell("wfmj_starttime").getDate(), getWipJob().getStart(), 
				brFlow.getCell("wfmj_endtime").getDate(), getWipJob().getEnd(), 
				brFlow.getCell("wfmj_startby").getString(), getWipJob().getStartBy(), 
				brFlow.getCell("wfmj_endby").getString(), getWipJob().getEndBy(), 
				"",""
				);*/
			brFlow.getCell("wfmj_state").set(getWipJob().getState());
			brFlow.getCell("wfmj_starttime").set(getWipJob().getStart());
			brFlow.getCell("wfmj_startby").set(getWipJob().getStartBy());
			brFlow.getCell("wfmj_endtime").set(getWipJob().getEnd());
			brFlow.getCell("wfmj_endby").set(getWipJob().getEndBy());
			
		}
		for(CellCollection col : brJobTask.getRowCollectionList()) {
			int rg = col.getCell("wfmjt_rg").getInt();
			String nodeId = WfmStep.makeNodeId(rg);
			WfmTask t = (WfmTask) getWipJob().getTask(nodeId);
			int newChoice;
			if(t != null) {
				newState = t.getState();
				newChoice = t.getChoice();
				if(newState != col.getCell("wfmjt_state").getInt()
					|| newChoice != col.getCell("wfmjt_choice").getInt()) {
					wed.addTaskChangeInfo(
							new WfmEventData.TaskChangeInfo(t.getStepRg()).setStateChanged(true)
							);
					Date d = t.getStart();
					if(newState == WipJob.JOB_STATE_STARTED || newState == WipJob.JOB_STATE_COMPLETED) {
						if(d == null || !d.after(DateUtil.minTime)) {
							t.setStart(new Date());
							t.setStartBy(loginId);
						}
					} else {
						if(d != null && d.after(DateUtil.minTime)) {
							t.setStart(DateUtil.zeroTime);
							t.setStartBy("");
						}
					}
					d = t.getEnd();
					if(newState == WipJob.JOB_STATE_COMPLETED) {
						if(d == null || !d.after(DateUtil.minTime)) {
							t.setEnd(new Date());
							t.setEndBy(loginId);
						}
					} else {
						if(d != null && d.after(DateUtil.minTime)) {
							t.setEnd(DateUtil.zeroTime);
							t.setEndBy("");
						}
					}

			//String content=  t.getDescription();
			String content =  String.format("Task[%s]",t.getDescription());
			String euid = loginId;
			switch(t.getState()) {
			case WipJob.JOB_STATE_COMPLETED:
				content += " completed";
				break;
			case WipJob.JOB_STATE_STARTED:
				switch( col.getCell("wfmjt_state").getInt()) {
				case WipJob.JOB_STATE_COMPLETED:
					content += " revert to in progress";
					break;
				default:
					content += " in progress";
					break;
				}
				break;
			case WipJob.JOB_STATE_AWAKED:
				switch( col.getCell("wfmjt_state").getInt()) {
				case WipJob.JOB_STATE_STARTED:
				case WipJob.JOB_STATE_COMPLETED:
					content += " revert to ready";
					break;
				default:
					euid = "cron";
					content += " ready";
					break;
				}
				break;
			case WipJob.JOB_STATE_WAITING:
				euid = "cron";
				content += " revert to waiting";
				break;
			}
					brFlow.getCell("wfmj_updstr").set(content);
					if (newChoice != col.getCell("wfmjt_choice").getInt()) {
						wfmA.logActivty(euid,0,getWipJob().getRg(),t.getStepRg(),WfmActivity.WFMACTIVITY_TYPE_STATECHANGE,
							String.format("Task[%s] action [%s]", t.getDescription(), t.getChoiceLabel()), 0,"",
							DateUtil.zeroTime, DateUtil.zeroTime, 
							col.getCell("wfmjt_state").getInt(), t.getState(), 
							col.getCell("wfmjt_starttime").getDate(), t.getStart(), 
							col.getCell("wfmjt_endtime").getDate(), t.getEnd(), 
							col.getCell("wfmjt_startby").getString(), t.getStartBy(), 
							col.getCell("wfmjt_endby").getString(), t.getEndBy(), 
							"",""
							);
					}
					wfmA.logActivty(euid,0,getWipJob().getRg(),t.getStepRg(),WfmActivity.WFMACTIVITY_TYPE_STATECHANGE,content, 0,"",
						DateUtil.zeroTime, DateUtil.zeroTime, 
						col.getCell("wfmjt_state").getInt(), t.getState(), 
						col.getCell("wfmjt_starttime").getDate(), t.getStart(), 
						col.getCell("wfmjt_endtime").getDate(), t.getEnd(), 
						col.getCell("wfmjt_startby").getString(), t.getStartBy(), 
						col.getCell("wfmjt_endby").getString(), t.getEndBy(), 
						"",""
						);
					col.getCell("wfmjt_state").set(t.getState());
					col.getCell("wfmjt_starttime").set(t.getStart());
					col.getCell("wfmjt_startby").set(t.getStartBy());
					col.getCell("wfmjt_endtime").set(t.getEnd());
					col.getCell("wfmjt_endby").set(t.getEndBy());
					col.getCell("wfmjt_updtime").set(new Date());
					col.getCell("wfmjt_choice").set(t.getChoice());
				}
			}
		}
		if (wed.isStateChanged()) { //log for job
			wfmA.logActivty(loginId,0,getWipJob().getRg(),0,WfmActivity.WFMACTIVITY_TYPE_STATECHANGE, job_content, 0,"",
				old_timeout, getWipJob().getDeadLine(), 
				old_state, getWipJob().getState(), 
				old_starttime, getWipJob().getStart(), 
				old_endtime, getWipJob().getEnd(), 
				old_startby, getWipJob().getStartBy(), 
				old_endby, getWipJob().getEndBy(), 
				"",""
				);
		}
		brFlow.updateCurrent();
		brFlow.commitWork();
		EventQueue que = EventQueues.lookup("WipNotify", EventQueues.APPLICATION, true);
		que.publish(new Event("onWipNotify", null,wed));
	}
	
	void setPhoto(Image p_image) {
		
	}
	
	static public void updateNetWorkLink(BiResult br,String stepStepTable,String stepStepPrefix,String flowPrefix,WipJob wj)
	{
		SelectUtil su = br.getSelectUtil();
		try {
			su.executeUpdate("delete from "+stepStepTable,
						new Wherecl().andUniop(stepStepPrefix+"frg", "=",br.getCellInt(flowPrefix+"rg")).stripAnd());
			List <WipStep> stepList = wj.getStepList(null);
			{
				BiTable t = br.getView().getSchema().getTable(stepStepTable);
				TableRec tr = t.newTableRec();
				for(int i=0;i<stepList.size();i++) {
					List <WipStep> cl = wj.getChildList(stepList.get(i),true);
					for(int j=0;j<cl.size();j++) {
						tr.addRecord();
						tr.setField(stepStepPrefix+"frg", br.getCellInt(flowPrefix+"rg"));
						tr.setField(stepStepPrefix+"seq", 0);
						tr.setField(stepStepPrefix+"fromrg", ((WfmStep) stepList.get(i)).getStepRg());
						tr.setField(stepStepPrefix+"torg", ((WfmStep) cl.get(j)).getStepRg());
						String ldata = (String) wj.getLinkData(
								((WfmStep) stepList.get(i)),
								((WfmStep) cl.get(j)));
						tr.setField(stepStepPrefix+"condition", ldata);
						
					}
				}
				su.insertByTableRec(t.getDbtName(), tr, true, t.getSerialId());
			}	
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		
	}
	
	public void addPhoto(org.zkoss.image.Image p_image) {
		if(wfmImage != null) {
			wfmImage.setContent(p_image);
		}
	}
}
