package com.uniinformation.wip;

import java.util.Date;
import java.util.List;


public interface WipTask extends WipStep {
	public Date getStart();
	public Date getEnd();
	public double getProgress();
	public void setStart(Date p_date);
	public void setEnd(Date p_date);
	public void setProgress(double p_progress);
	public int getState();
	public void setState(int p_state);
	public String getStartBy();
	public void setStartBy(String p_startby);
	public String getEndBy();
	public void setEndBy(String p_startby);
	public boolean isLocked();
	/*
	int taskState;

	public void setState(int p_state) {
		taskState = p_state;
	}
	public int getState() {
		return(taskState);
	}
	public String getColor() {
		switch(taskState) {
		case WipJob.JOB_STATE_WAITING : return(WipJob.Color_Waiting);
		case WipJob.JOB_STATE_AWAKED  : return(WipJob.Color_Awaked);
		default : return(null);
		}
	}
	*/
}
