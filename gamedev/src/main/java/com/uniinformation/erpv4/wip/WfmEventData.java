package com.uniinformation.erpv4.wip;

import java.util.ArrayList;
import java.util.List;

public class WfmEventData {
	static public class TaskChangeInfo {
		public int getTrg() {
			return trg;
		}
		public boolean isStateChanged() {
			return stateChanged;
		}
		public TaskChangeInfo setStateChanged(boolean stateChanged) {
			this.stateChanged = stateChanged;
			return(this);
		}
		public boolean isDeadlineChanged() {
			return deadlineChanged;
		}
		public void setDeadlineChanged(boolean deadlineChanged) {
			this.deadlineChanged = deadlineChanged;
		}
		public boolean isMessageAdded() {
			return messageAdded;
		}
		public void setMessageAdded(boolean messageAdded) {
			this.messageAdded = messageAdded;
		}
		public boolean isAssignToChanged() {
			return assignToChanged;
		}
		public void setAssignToChanged(boolean assignToChanged) {
			this.assignToChanged = assignToChanged;
		}
		int trg;
		boolean stateChanged = false;
		boolean deadlineChanged = false;
		boolean messageAdded = false;
		boolean assignToChanged = false;
		public TaskChangeInfo(int p_trg) {
			trg = p_trg;
		}
	}
	int wfmrg;
	boolean networkChanged = false;
	boolean stateChanged = false;
	boolean deadlineChange = false;
	boolean messageAdded = false;
	ArrayList<TaskChangeInfo> tcArray = new ArrayList<TaskChangeInfo> ();
	
	public WfmEventData (int p_rg) {
		wfmrg = p_rg;
	}
	public int getWfmrg() {
		return wfmrg;
	}
	public boolean isNetworkChanged() {
		return networkChanged;
	}
	public void setNetworkChanged(boolean networkChanged) {
		this.networkChanged = networkChanged;
	}
	public boolean isStateChanged() {
		return stateChanged;
	}
	public void setStateChanged(boolean stateChanged) {
		this.stateChanged = stateChanged;
	}
	public boolean isMessageAdded() {
		return messageAdded;
	}
	public void setMessageAdded(boolean messageAdded) {
		this.messageAdded = messageAdded;
	}
	
	public WfmEventData addTaskChangeInfo(TaskChangeInfo p_tc) {
		tcArray.add(p_tc);
		return(this);
	}
	public List<TaskChangeInfo> getTaskStateChangeList() {
		return(tcArray);
	}
}
