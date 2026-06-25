package com.uniinformation.erpv4.wip;

import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cron.CronJob;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.DynamicClassLoader;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.wip.WipJob;
import com.uniinformation.wip.WipStep;

public class WfmCronJob extends CronJob {
	SessionHelper sessionHelper;
	BiSchema schema;
	BiResult brFlow;
//	BiResult brFlowStep;
	BiResult brJob;
	WfmTaskUpdate wipTaskUpdate;
	AtomicBoolean fDebug = new AtomicBoolean(true);
	Hashtable<String,BiResult> linkBrHash = new Hashtable<String,BiResult>();
	@Override
	public int runOnce() throws Exception {
		UniLog.log1("wfmcronjob running");
		doCreateJob();
		doProcessJob();
		doProcessTask();
		return(0);
	}

	private void doProcessTask() throws Exception {
		brJob.clear();
		brJob.clearCondition();
//		brJob.addCustomCondition("wfmjt_state = " + WipJob.JOB_STATE_AWAKED + " and wfmjt_actenabled = 'Y'");
		brJob.addCustomCondition(String.format("wfmjt_state in (%d,%d) and wfmjt_actenabled = 'Y'",WipJob.JOB_STATE_AWAKED,WipJob.JOB_STATE_STARTED));
		brJob.query();
		UniLog.log1("start brJob.count:%d", brJob.getRowCount());
		for(int i=0;i<brJob.getRowCount();i++) {
			brJob.loadOneRecV(i);
			if (fDebug.get()) UniLog.log1("%d: processjob. wfmj_rg:%d", i, brJob.getCellInt("wfmj_rg"));
			try {
				brJob.fetchOneRecV(i);
				wipTaskUpdate.loadWorkFlow(brJob, brJob.getSessionHelper(), brJob.getCellInt("wfmj_rg"),0);
				boolean needUpdateReload = false;
				BiResult sr = brJob.getSubLink("wip.WfmJobTask");
				Vector <BiCellCollection> v = sr.getRowCollectionList();
				for(int j=0;j<v.size();j++) {
					BiCellCollection col = v.get(j);
					boolean actionEnabled = col.getCell("wfmjt_actenabled").getBoolean() ;
					String action = StringUtils.trim(col.getCellString("wfmjt_action"));
					UniLog.log1("%d: processtask. actionEnabled:%s action:%s", j, actionEnabled, action);
					if(actionEnabled) {
						if(!StringUtils.isBlank(action)) {
							try {
								String actionClass = StringUtils.startsWith(action,"com.") ? action : "com.uniinformation."+action;
								ReturnMsg rtn ;
								WfmJob wj = wipTaskUpdate.getWipJob();
								WfmTask wt = (WfmTask) wj.getStep(WfmStep.makeNodeId(col.getCellInt("wfmjt_rg")));
								Class[]	paramTypes = new Class[]{};
								WfmActionInterface wai = (WfmActionInterface) DynamicClassLoader.newInstance(actionClass, paramTypes);
								switch(wt.getState()) {
								case WipJob.JOB_STATE_AWAKED:
									rtn = wai.startAction(sessionHelper,wj, wt);
									if(rtn.getStatus()) {
										WipJob.taskSwitchStart(wj, wt, true);
										needUpdateReload = true;
									}
									break;
								case WipJob.JOB_STATE_STARTED:
									rtn = wai.getActionStatus(sessionHelper,wj, wt);
									if(rtn.getStatus()) {
										WipJob.taskSwitchEnd(wj, wt, true);
										needUpdateReload = true;
									}
									break;
								}
							} catch (Exception ex) {
								UniLog.log(ex);
							}
						}
					}
				}
				if (fDebug.get()) UniLog.log1("needUpdateReload:%s", needUpdateReload);
				if(needUpdateReload) {
					wipTaskUpdate.updateJobStatus();
				}
			} catch (Exception ex) {
				if(brJob.inBeginWork()) brJob.rollbackWork();
				UniLog.log(ex);
			}
		}
		//if (fDebug.get()) UniLog.log1("end");
	}

	private void doProcessJob() throws Exception {
		brJob.clear();
		brJob.clearCondition();
		brJob.addCustomCondition("wfmj_state = " + WipJob.JOB_STATE_WAITING);
		brJob.query();
		UniLog.log1("start brJob.count:%d", brJob.getRowCount());
		for(int i=0;i<brJob.getRowCount();i++) {
			brJob.loadOneRecV(i);
			if (fDebug.get()) UniLog.log1("%d: processjob. wfmj_rg:%d", i, brJob.getCellInt("wfmj_rg"));
			if(brJob.getCellInt("wfmj_state") == WipJob.JOB_STATE_WAITING) {
				try {
					brJob.fetchOneRecV(i);
					wipTaskUpdate.loadWorkFlow(brJob, brJob.getSessionHelper(), brJob.getCellInt("wfmj_rg"),0);
					
					//state: waiting to awake
					wipTaskUpdate.getWipJob().switchAwake(true);
					
					//state: awake to start
					if(brJob.getCell("wfmj_autostart").getBoolean()) {
						wipTaskUpdate.getWipJob().switchStart(true);
					}
					wipTaskUpdate.updateJobStatus();
				} catch (Exception ex) {
					if(brJob.inBeginWork()) brJob.rollbackWork();
					UniLog.log(ex);
				}
			}
		}
		//if (fDebug.get()) UniLog.log1("end");
	}
//	void doProcessJobxx() throws Exception {
//		brJob.clear();
//		brJob.clearCondition();
//		brJob.addCustomCondition("wfmj_state <> " + WipJob.JOB_STATE_COMPLETED);
//		brJob.query();
//		UniLog.log1("CronServer WipCronJob doProcessJob. count:%d", brJob.getRowCount());
//		for(int i=0;i<brJob.getRowCount();i++) {
//			brJob.loadOneRecV(i);
//			if (fDebug.get()) UniLog.log1("CronServer WipCronJob monitor job " + brJob.getCellInt("wfmj_rg"));
//			if(brJob.getCellInt("wfmj_state") == WipJob.JOB_STATE_WAITING) {
//				try {
//					brJob.fetchOneRecV(i);
//					wipTaskUpdate.loadWorkFlow(brJob, brJob.getSessionHelper(), brJob.getCellInt("wfmj_rg"),0);
//					String linkView = brJob.getCellString("wfmj_viewid");
//					if(linkView != null && !linkView.equals("")) {
//						boolean needUpdateReload = false;
//						BiResult sr = brJob.getSubLink("wip.WfmJobTask");
//						Vector <BiCellCollection> v = sr.getRowCollectionList();
//						for(int j=0;j<v.size();j++) {
//							BiCellCollection col = v.get(j);
//							String ccond = col.getCell("wfmjt_createcond").getString();
//							if(!ccond.equals("")) {
//								BiResult linkBr = getBrFromHash(linkView);
//								linkBr.clear();
//								linkBr.clearCondition();
//								linkBr.addCustomCondition(brJob.getCellString("wfmj_keyfd") + " = " + brJob.getCellInt("wfmj_key"));
//								linkBr.addCustomCondition(ccond);
//								linkBr.query();
//								if(linkBr.getQueryRecCount() <= 0) {
//									UniLog.log1("Task " + col.getCellInt("wfmjt_rg") + " createcondiion failed, remove from network");
//									wipTaskUpdate.getWipJob().delStep(wipTaskUpdate.getWipJob().getStep(WfmStep.makeNodeId(col.getCellInt("wfmjt_rg"))), true);
//									Object o = sr.getTrStatObj(j);
//									sr.markDelete(o, true);
//									needUpdateReload = true;
//								}
//							}
//						}
//						if(needUpdateReload) {
//							ReturnMsg rtn = wipTaskUpdate.updateJobNetwork();
//							if(rtn != null && !rtn.getStatus()) {
//								UniLog.log1("Update Job Network " + brJob.getCellInt("wfmj_rg") + " Failed " + rtn.getMsg());
//								continue;
//							}
//							brJob.refetchCurrent();
//							wipTaskUpdate.loadWorkFlow(brJob, brJob.getSessionHelper(), brJob.getCellInt("wfmj_rg"),0);
//						} else {
//							UniLog.log1("Cronserver job start without delete link");
//						}
//					}
//					
//					wipTaskUpdate.getWipJob().switchAwake(true);
//					if(brJob.getCell("wfmj_autostart").getBoolean()) {
//						wipTaskUpdate.getWipJob().switchStart(true);
//					}
//					wipTaskUpdate.updateJobStatus();
//				} catch (Exception ex) {
//					if(brJob.inBeginWork()) brJob.rollbackWork();
//					UniLog.log(ex);
//				}
//			}
//		}
//	}
	
	private BiResult getBrFromHash(String p_viewid) {
		BiResult linkBr = linkBrHash.get(p_viewid);
		if(linkBr == null) {
			linkBr = schema.getViewByName(p_viewid).newBiResult(sessionHelper.getLoginId(), null, null, sessionHelper);
			linkBrHash.put(p_viewid, linkBr);
		}
		return(linkBr);
	}
	
	private boolean setupJobTask(BiResult linkBr) {
		boolean needUpdateReload=false;
		BiResult sr = brJob.getSubLink("wip.WfmJobTask");
		Vector <BiCellCollection> v = sr.getRowCollectionList();
		for(int j=0;j<v.size();j++) {
			BiCellCollection col = v.get(j);
			String ccond = col.getCell("wfmjt_createcond").getString();
			if(!ccond.equals("")) {
				BiCellCollection bc = linkBr.getCurrentCollection();
				com.uniinformation.utils.exprpar.Parser parser 
						= new com.uniinformation.utils.exprpar.Parser(0,ccond.trim(),bc,bc);
				try {
					Object oo = parser.evaluate();
					boolean needRemove = false;
					if(oo != null) {
						if(oo instanceof Boolean && !((Boolean) oo).booleanValue()) {
							needRemove = true;
						}
					}
					if(needRemove) {
						wipTaskUpdate.getWipJob().delStep(wipTaskUpdate.getWipJob().getStep(WfmStep.makeNodeId(col.getCellInt("wfmjt_rg"))), true);
						Object o = sr.getTrStatObj(j);
						sr.markDelete(o, true);
						needUpdateReload = true;
					}
				} catch(Exception ex) {
					UniLog.log(ex);
				}
			}
		}
		return(needUpdateReload);

//							String linkView = brJob.getCellString("wfmj_viewid");
//					if(linkView != null && !linkView.equals("")) {
//						boolean needUpdateReload = false;
//						BiResult sr = brJob.getSubLink("wip.WfmJobTask");
//						Vector <BiCellCollection> v = sr.getRowCollectionList();
//						for(int j=0;j<v.size();j++) {
//							BiCellCollection col = v.get(j);
//							String ccond = col.getCell("wfmjt_createcond").getString();
//							if(!ccond.equals("")) {
//								BiResult linkBr = getBrFromHash(linkView);
//								linkBr.clear();
//								linkBr.clearCondition();
//								linkBr.addCustomCondition(brJob.getCellString("wfmj_keyfd") + " = " + brJob.getCellInt("wfmj_key"));
//								linkBr.addCustomCondition(ccond);
//								linkBr.query();
//								if(linkBr.getQueryRecCount() <= 0) {
//									UniLog.log1("Task " + col.getCellInt("wfmjt_rg") + " createcondiion failed, remove from network");
//									wipTaskUpdate.getWipJob().delStep(wipTaskUpdate.getWipJob().getStep(WfmStep.makeNodeId(col.getCellInt("wfmjt_rg"))), true);
//									Object o = sr.getTrStatObj(j);
//									sr.markDelete(o, true);
//									needUpdateReload = true;
//								}
//							}
//						}
//						if(needUpdateReload) {
//							ReturnMsg rtn = wipTaskUpdate.updateJobNetwork();
//							if(rtn != null && !rtn.getStatus()) {
//								UniLog.log1("Update Job Network " + brJob.getCellInt("wfmj_rg") + " Failed " + rtn.getMsg());
//								continue;
//							}
//							brJob.refetchCurrent();
//							wipTaskUpdate.loadWorkFlow(brJob, brJob.getSessionHelper(), brJob.getCellInt("wfmj_rg"),0);
//						} else {
//							UniLog.log1("Cronserver job start without delete link");
//						}
//					}
//					
//					wipTaskUpdate.getWipJob().switchAwake(true);
//					if(brJob.getCell("wfmj_autostart").getBoolean()) {
//						wipTaskUpdate.getWipJob().switchStart(true);
//					}
//					wipTaskUpdate.updateJobStatus();
		
	}
	private void doCreateJob() throws Exception {
		//step 1: loop flow list
		brFlow.clear();
		brFlow.clearCondition();
		brFlow.addCustomCondition("wfmf_autocreate='Y'");
		brFlow.query();
		RpcClient rpc = null;
		UniLog.log1("start brFlow.count:%d", brFlow.getRowCount());
		for(int i=0;i<brFlow.getRowCount();i++) {
			brFlow.loadOneRecV(i);
			if (fDebug.get()) UniLog.log1("%d: processflow. id:%d name:%s viewid:%s", i, brFlow.getCellInt("wfmf_rg"), brFlow.getCellString("wfmf_name"), brFlow.getCellString("wfml_viewid"));
			/*
			BiResult linkBr = linkBrHash.get(brFlow.getCellString("wfmf_viewid"));
			if(linkBr == null) {
				linkBr = schema.getViewByName(brFlow.getCellString("wfmf_viewid")).newBiResult(sessionHelper.getLoginId(), null, null, sessionHelper);
				linkBrHash.put(brFlow.getCellString("wfmf_viewid"), linkBr);
			}
			*/
			
			
			//step 2: create job from flow
			BiResult linkBr = getBrFromHash(brFlow.getCellString("wfmf_viewid"));
			linkBr.clear();
			linkBr.clearCondition();
			linkBr.addCustomCondition(brFlow.getCellString("wfmf_createcond"));
			linkBr.appendWherecl(
					new Wherecl().appendString(linkBr.getView().getColumnByLabel(brFlow.getCellString("wfmf_keyfd")).getField().getFullName()
			                        + " not in (select  wfmj_keystr from wfmjob where wfmj_viewid = '"+ 
									brFlow.getCellString("wfmf_viewid")
									+ "' and wfmj_frg = " + brFlow.getCellInt("wfmf_rg") + " ) ")
					);
			linkBr.query();
			UniLog.log1("linkBr:%s view:%s row:%d", linkBr,linkBr.getView(), linkBr.getRowCount());
			for(int j=0;j<linkBr.getRowCount();j++) {
				linkBr.loadOneRecV(j);
				if(rpc == null) {
					rpc = sessionHelper.getRpcClient();
				}
				Value v=rpc.callSegment("wiputil_createjobfromflow", 
											new VectorUtil()
												.addElement("")
												.addElement(brFlow.getCellInt("wfmf_rg"))
												.addElement(linkBr.getCellInt(brFlow.getCellString("wfmf_keyfd")))
												.addElement(linkBr.getCellString(brFlow.getCellString("wfmf_idfd")))
												.addElement(linkBr.getCellString(brFlow.getCellString("wfmf_titlefd")))
												.toVector()
										);
				UniLog.log1("create job for " + linkBr.getCellInt(brFlow.getCellString("wfmf_keyfd")) + " got " + (v == null ? "(null)" : v.toString()));
				if(v != null) {
					if(v.toString().startsWith("OK")) {
						//step 3: update job network
						brJob.clear();
						brJob.clearCondition();
						int wfmjrg = Integer.parseInt(v.toString().substring(4).trim());
						brJob.addCustomCondition("wfmj_rg = " + wfmjrg);
						brJob.query();
						if(brJob.getRowCount() == 1)  {
							brJob.fetchOneRecV(0);
							linkBr.fetchOneRecV(j);
							wipTaskUpdate.loadWorkFlow(brJob, brJob.getSessionHelper(), brJob.getCellInt("wfmj_rg"),0);
							boolean needUpdateReload = setupJobTask(linkBr);
							if(needUpdateReload) {
								ReturnMsg rtn = wipTaskUpdate.updateJobNetwork();
								if(rtn != null && !rtn.getStatus()) {
									UniLog.log1("update job network " + brJob.getCellInt("wfmj_rg") + " Failed " + rtn.getMsg());
								}
							}
							
						}
					}
				}
			}
			/*
			  if(autocreate) {
			  		do select not created viewRecord
			  		createJob
			  		if(autostart) startjob
			 	} 
			 */
		}
		if(rpc != null) {
			rpc.close();
			rpc = null;
		}
		/*
		  select autocomplete and time waiting tasks and task has timeout
		  check if completed change state
		  		if timeout send notification
		  		if timesup if(autostart task start task, else awake task)
		 */
		//if (fDebug.get()) UniLog.log1("end");
	}

	@Override
	public void setSessionHelper(SessionHelper p_sh) throws Exception{
		sessionHelper = p_sh;
		// TODO Auto-generated method stub
		schema = BiSchema.loadSchema(sessionHelper);
		brFlow = schema.getViewByName("wip.WfmWipFlow").newBiResult(sessionHelper.getLoginId(), null, null, sessionHelper);
		brJob  = schema.getViewByName("wip.WfmWipJob").newBiResult(sessionHelper.getLoginId(), null, null, sessionHelper);
		wipTaskUpdate = new WfmTaskUpdate(null, false, null);
	}

}
