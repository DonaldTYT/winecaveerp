package com.uniinformation.wip;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.kyoko.common.DateUtil;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.UniLog;


abstract public class WipJob extends WipFlow {
	
	public static final int JOB_STATE_WAITING   = 0;
	public static final int JOB_STATE_AWAKED    = 1;
	public static final int JOB_STATE_STARTED   = 2;
	public static final int JOB_STATE_SUSPENDED = 3;
	public static final int JOB_STATE_COMPLETED = 4;
	public static final int JOB_STATE_ABORTED   = 5;
	public static final int JOB_STATE_LOCKED    = 6;
	public static final String[]JOB_STATE_NAME= {"Waiting","Ready","Progress","Suspended","Completed","Aborted","Locked"};
	
	public static final String Color_Waiting = "#E0E0E0"; //grey
	public static final String Color_Awaked  = "#FFB749"; //orange
	public static final String Color_Started = "#8EEEC7"; //green
	public static final String Color_Suspended = null;
	public static final String Color_Completed = "#20cdff"; //blue
	public static final String Color_Aborted = "#ff5f5f"; //red
	public static final String Color_Locked = "#e8ff00"; //yellow
	
	int  jobState = JOB_STATE_WAITING;
//	Date jobStartTime = null;
//	Date jobEndTime = null;
	boolean keepConsistentState = true;
	public static AtomicBoolean fDebug = new AtomicBoolean(true);
	
	public List<WipTask> getTaskList(Comparator <WipTask> c) {
		List l = nu.getNodeValueList();
		Collections.sort(l,c);
		return(l);
	}

	public List<WipTask> getChildList(WipTask t, boolean p_immediate) {
		List l = super.getChildList(t, p_immediate);
		return(l);
	}
	public WipTask getTask(String id) {
		return((WipTask) nu.getNode(id));
	}
	
	public void addTask(WipTask p_task) {
		super.addStep(p_task);
	}

	public void delTask(WipTask p_task,boolean p_joinFlag) {
		super.delStep(p_task,p_joinFlag);
	}
	
	@Override 
	public void addStep(WipStep p_step) {
		if(p_step instanceof WipTask) {
			
		}
		super.addStep(p_step);
	}
	@Override 
	public void delStep(WipStep p_step,boolean p_joinFlag) {
		List childSteps = null;
		if(p_step instanceof WipTask) {
			childSteps = getChildList(p_step,true);
		}
		super.delStep(p_step,p_joinFlag);
		if(childSteps != null) {
			for(WipTask wt : (List<WipTask>) childSteps) {
				try {
					recalState(this,wt);
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		}
	}
	
	@Override
	public String getStartColor() {
			switch(jobState) {
			case JOB_STATE_WAITING :  return(Color_Waiting);
			case JOB_STATE_AWAKED  :  return(Color_Awaked);
			case JOB_STATE_STARTED :
			case JOB_STATE_SUSPENDED :
			case JOB_STATE_COMPLETED :return(Color_Completed);
			case JOB_STATE_ABORTED :  return(Color_Aborted);
			default : return(null);
			}
	}
	@Override
	public String getEndColor() {
		List l = getStepsWithoutChild();
		if(l.size() <= 0) {
		switch(jobState) {
		case JOB_STATE_SUSPENDED :
		case JOB_STATE_WAITING :
		case JOB_STATE_AWAKED  :return(Color_Waiting);
		case JOB_STATE_STARTED :return(Color_Awaked);
		case JOB_STATE_COMPLETED : return(Color_Completed);
		case JOB_STATE_ABORTED :return(Color_Aborted);
		default : return(null);
		}
		} else {
			switch(jobState) {
			case JOB_STATE_SUSPENDED :return(Color_Waiting);
			case JOB_STATE_ABORTED :return(Color_Aborted);
			case JOB_STATE_COMPLETED:return(Color_Completed);
			case JOB_STATE_AWAKED:return(Color_Waiting);
			case JOB_STATE_STARTED :
				for(WipTask wt : (List<WipTask>) l) {
					if(wt.getState() != JOB_STATE_COMPLETED)return(Color_Waiting);
				}
				return(Color_Awaked);
			default:
				return(null);
			}
		}
	}
	
	static public String getStateColor(int p_state) {
			switch(p_state) {
			case JOB_STATE_WAITING : return(Color_Waiting);
			case JOB_STATE_AWAKED  :return(Color_Awaked);
			case JOB_STATE_STARTED :return(Color_Started);
			case JOB_STATE_SUSPENDED :return(Color_Suspended);
			case JOB_STATE_COMPLETED :return(Color_Completed);
			case JOB_STATE_ABORTED :return(Color_Aborted);
			case JOB_STATE_LOCKED :return(Color_Locked);
			default : return(null);
			}
		
	}
	/*
	public void start(Date p_date) throws CellException {
		if(p_date == null || !p_date.after(DateUtil.minDate)) {
			throw new CellException("Canoot UnStart Job");
		}
		if(jobStartTime != null && p_date.after(DateUtil.minDate)) {
			throw new CellException("Job Already Started");
		}
		jobStartTime = p_date;
		jobState = JOB_STATE_STARTED;
	}
	*/
	public void switchAwake(boolean p_sw) throws CellException {
		if (fDebug.get()) UniLog.log1("called %s", p_sw);
		if(p_sw) {
			switch(jobState) {
			case JOB_STATE_WAITING :
				jobState = JOB_STATE_AWAKED;
				break;
			case JOB_STATE_AWAKED  :
			case JOB_STATE_STARTED :
			case JOB_STATE_SUSPENDED :
			case JOB_STATE_COMPLETED :
			case JOB_STATE_ABORTED :
			}
		} else {
			jobState = JOB_STATE_WAITING;
		}
	}
	public void switchEnd(boolean p_sw) throws CellException {
		if (fDebug.get()) UniLog.log1("called %s", p_sw);
		if(p_sw) {
			switch(jobState) {
			case JOB_STATE_WAITING:
			case JOB_STATE_AWAKED:
				if(keepConsistentState) {
					throw new CellException("Job Not Started , Cannot End");
				}
			case JOB_STATE_STARTED:
				List wsl = getStepList(null);
				for(WipTask ws : (List<WipTask>)wsl) {
					ws.setState(JOB_STATE_COMPLETED);
				}
				jobState = JOB_STATE_COMPLETED;
				break;
			case JOB_STATE_SUSPENDED :
			case JOB_STATE_COMPLETED :
			case JOB_STATE_ABORTED :
			}
		} else {
			switch(jobState) {
			case JOB_STATE_WAITING:
			case JOB_STATE_AWAKED  :
			case JOB_STATE_STARTED:
				break;
			case JOB_STATE_COMPLETED :
				jobState = JOB_STATE_STARTED;
				break;
			case JOB_STATE_SUSPENDED :
			case JOB_STATE_ABORTED :
			}
		}	
	}
	
	
	
	public void switchStart(boolean p_sw) throws CellException {
		if (fDebug.get()) UniLog.log1("called %s", p_sw);
		if(p_sw) {
			switch(jobState) {
			case JOB_STATE_WAITING:
				if(keepConsistentState) {
					throw new CellException("Job Still Waiting, Cannot Start");
				}
			case JOB_STATE_AWAKED:
				jobState = JOB_STATE_STARTED;
				List wsl = getStepsWithoutParent();
				for(WipTask ws : (List<WipTask>)wsl) {
					if(ws.getState() == JOB_STATE_WAITING) {
						ws.setState(JOB_STATE_AWAKED);
					}
				}
				break;
			case JOB_STATE_STARTED :
			case JOB_STATE_SUSPENDED :
			case JOB_STATE_COMPLETED :
			case JOB_STATE_ABORTED :
			}
		} else {
			switch(jobState) {
			case JOB_STATE_WAITING:
			case JOB_STATE_AWAKED  :
				break;
			case JOB_STATE_COMPLETED :
			case JOB_STATE_STARTED:
				jobState = JOB_STATE_AWAKED;
				List wsl = getStepList(null);
				for(WipTask ws : (List<WipTask>)wsl) {
					ws.setState(JOB_STATE_WAITING);
				}
				break;
			case JOB_STATE_SUSPENDED :
			case JOB_STATE_ABORTED :
			}
		}
	}
	public void setState(int p_state) {
		UniLog.log1("state %s->%s", getStateLabel(jobState), getStateLabel(p_state));
		jobState = p_state;
	}
	public int getState() {
		return(jobState);
	}
	public static String getStateLabel(int p_state) {
		try {
			return JOB_STATE_NAME[p_state];
		}
		catch(Exception ex) {
			UniLog.log1("error:"+ex.getMessage());
			return "";
		}
	}
	static public void taskSwitchStart(WipJob p_job,WipTask p_task,boolean p_sw) throws CellException {
		if (fDebug.get()) UniLog.log1("called sw:%s task:%s currentTaskState:%s", p_sw, p_task, getStateLabel(p_task.getState()));
		if(p_sw) {
			switch(p_task.getState()) {
			case JOB_STATE_ABORTED:
				if(p_job.keepConsistentState) {
					throw new CellException("Cannot start this task. Current task was aborted.");
				}
			case JOB_STATE_WAITING:
				if(p_job.keepConsistentState) {
					throw new CellException("Cannot start this task. Please finish previous task first.");
				}
			case JOB_STATE_AWAKED:
				p_task.setState(JOB_STATE_STARTED);
				break;
			case JOB_STATE_STARTED :
			case JOB_STATE_SUSPENDED :
			case JOB_STATE_COMPLETED :
			}
		} else {
			switch(p_task.getState()) {
			case JOB_STATE_WAITING:
			case JOB_STATE_AWAKED  :
				break;
			case JOB_STATE_STARTED:
				p_task.setState(JOB_STATE_AWAKED);
				List wsl = p_job.getParentList(p_task, true);
				boolean aborted = false;
				for(WipTask ws2 : (List<WipTask>)wsl) {
					int nextstate = p_job.getNextStateWhenComplete(ws2,p_task);
					if(nextstate == JOB_STATE_ABORTED) {
						aborted = true;
					}
				}
				if(aborted) {
					taskAbort(p_job,p_task,true);
				}
				break;
			case JOB_STATE_SUSPENDED :
			case JOB_STATE_COMPLETED :
			case JOB_STATE_ABORTED :
			}
		}
	}
	static public void recalState(WipJob p_job,WipTask p_task) throws CellException {
		List wsl2 = p_job.getParentList(p_task, true);
		boolean allParentCompleted = true;
		if(wsl2.size() <= 0) {
			if(p_job.getState() == JOB_STATE_WAITING || p_job.getState() == JOB_STATE_AWAKED) {
				allParentCompleted = false;
			}
		} else {
		for(WipTask ws2 : (List<WipTask>)wsl2) {
			if(ws2.getState() != JOB_STATE_COMPLETED) {
				allParentCompleted = false;
			}
		}
		}
		if(allParentCompleted) {
			if(p_task.getState()== JOB_STATE_WAITING) p_task.setState(JOB_STATE_AWAKED);
		} else {
			if(p_task.getState()== JOB_STATE_AWAKED) p_task.setState(JOB_STATE_WAITING);
		}
	}
	static public void taskAbort(WipJob p_job,WipTask p_task,boolean p_sw) throws CellException {
		if (fDebug.get()) UniLog.log1("called %s", p_sw);
		if(p_sw) {
		switch(p_task.getState()) {
		case JOB_STATE_WAITING:
		case JOB_STATE_AWAKED:
			p_task.setState(JOB_STATE_ABORTED);
			List wsl = p_job.getChildList(p_task, true);
			if(wsl.isEmpty()) {
				wsl = p_job.getStepsWithoutChild();
				boolean jobCompleted = true;
				for(WipTask ws : (List<WipTask>)wsl) {
					if(ws.getState() != JOB_STATE_COMPLETED && ws.getState() != JOB_STATE_ABORTED) {
						jobCompleted = false;
					}
				}
				if(jobCompleted) {
					p_job.setState(JOB_STATE_COMPLETED);
				}
			} else {
				for(WipTask ws : (List<WipTask>)wsl) {
					taskAbort(p_job,ws,p_sw);
				}
			}
			break;
		case JOB_STATE_ABORTED:
			break;
		default: 
			if(p_job.keepConsistentState) {
				throw new CellException("Cannot abort. It due to task already started.");
			}
			break;
		}
		} else {
		switch(p_task.getState()) {
		case JOB_STATE_ABORTED:
			p_task.setState(JOB_STATE_WAITING);
			List wsl = p_job.getChildList(p_task, true);
			if(wsl.isEmpty()) {
				p_job.setState(JOB_STATE_STARTED);
			} else {
				for(WipTask ws : (List<WipTask>)wsl) {
					taskAbort(p_job,ws,p_sw);
				}
			}
			break;
		case JOB_STATE_WAITING:
		case JOB_STATE_AWAKED:
			break;
		default:
			break;
		}
		}
		
	}
	static public void taskSwitchEnd(WipJob p_job,WipTask p_task,boolean p_sw) throws CellException {
		if (fDebug.get()) UniLog.log1("called sw:%s task:%s currentTaskState:%s", p_sw, p_task, getStateLabel(p_task.getState()));
		if(p_sw) {
			switch(p_task.getState()) {
			case JOB_STATE_WAITING:
			case JOB_STATE_AWAKED:
				if(p_job.keepConsistentState) {
					UniLog.log1("job not started, cannot end, abort");
					throw new CellException("job not started, cannot end");
				}
			case JOB_STATE_STARTED:
				p_task.setState(JOB_STATE_COMPLETED);
			case JOB_STATE_COMPLETED:
				List wsl = p_job.getChildList(p_task, true);
				if(wsl.isEmpty()) {
					wsl = p_job.getStepsWithoutChild();
					boolean jobCompleted = true;
					for(WipTask ws : (List<WipTask>)wsl) {
						if(ws.getState() != JOB_STATE_COMPLETED && ws.getState() != JOB_STATE_ABORTED) {
							jobCompleted = false;
						}
					}
					if(jobCompleted) {
						p_job.setState(JOB_STATE_COMPLETED);
					}
					
				} else {
				for(WipTask ws : (List<WipTask>)wsl) {
					int nextState = p_job.getNextStateWhenComplete(p_task,ws);
					switch(nextState) {
					case JOB_STATE_AWAKED:
						if(ws.getState() == JOB_STATE_ABORTED) {
							taskAbort(p_job,ws,false);
						}
						if(ws.getState() == JOB_STATE_WAITING) {
							List wsl2 = p_job.getParentList(ws, true);
							boolean allParentCompleted = true;
							for(WipTask ws2 : (List<WipTask>)wsl2) {
								if(ws2.getState() != JOB_STATE_COMPLETED &&
								   ws2.getState() != JOB_STATE_ABORTED) {
									allParentCompleted = false;
								}
							}
							if(allParentCompleted) ws.setState(JOB_STATE_AWAKED);
						}
						break;
					case JOB_STATE_ABORTED:
						if(ws.getState() == JOB_STATE_WAITING ||
						   ws.getState() == JOB_STATE_AWAKED ||
						   ws.getState() == JOB_STATE_ABORTED
						   ) {
							taskAbort(p_job,ws,true);
						} else {
							if(p_job.keepConsistentState) {
								throw new CellException("Cannot abort. It due to task already started");
							}
						}
						break;
					}
//					if(ws.getState() == JOB_STATE_WAITING) {
//						switch(nextState) {
//						case JOB_STATE_AWAKED:
//							List wsl2 = p_job.getParentList(ws, true);
//							boolean allParentCompleted = true;
//							for(WipTask ws2 : (List<WipTask>)wsl2) {
//								if(ws2.getState() != JOB_STATE_COMPLETED) {
//									allParentCompleted = false;
//								}
//							}
//							if(allParentCompleted) ws.setState(JOB_STATE_AWAKED);
//							break;
//						case JOB_STATE_ABORTED:
//							taskAbort(p_job,ws,true);
//							break;
//						}
//					}
				}
				}
				break;
			case JOB_STATE_SUSPENDED :
			case JOB_STATE_ABORTED :
			}
		} else {
			switch(p_task.getState()) {
			case JOB_STATE_WAITING:
			case JOB_STATE_AWAKED  :
			case JOB_STATE_STARTED:
				break;
			case JOB_STATE_COMPLETED :
				List wsl = p_job.getChildList(p_task, true);
				if(wsl.isEmpty()) {
					if(p_job.getState() == JOB_STATE_COMPLETED) {
//						throw new CellException("Job Completed, Cannot revert completed task");
						p_job.setState(JOB_STATE_STARTED);
					}
				} else {
				for(WipTask ws : (List<WipTask>)wsl) {
					if(ws.getState() == JOB_STATE_ABORTED) {
						taskAbort(p_job,ws,false);
					} if(ws.getState() == JOB_STATE_AWAKED) {
						ws.setState(JOB_STATE_WAITING);
					} else
					if(ws.getState() != JOB_STATE_WAITING) {
						if(p_job.keepConsistentState) {
							throw new CellException("Cannot revert completed task. It due to dependent task was completed.");
						}
					}
				}
				}
				p_task.setState(JOB_STATE_STARTED);
				break;
			case JOB_STATE_SUSPENDED :
			case JOB_STATE_ABORTED :
			}
		}	
		
	}
	
	protected int getNextStateWhenComplete(WipTask parent,WipTask child) {
		return(JOB_STATE_AWAKED);
	}
}
