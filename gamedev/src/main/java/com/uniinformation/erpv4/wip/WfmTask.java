package com.uniinformation.erpv4.wip;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.parser.VariableInterface;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.whereclpar.Expression;
import com.uniinformation.wip.WipFlow;
import com.uniinformation.wip.WipJob;
import com.uniinformation.wip.WipTask;

public class WfmTask extends WfmStep implements WipTask , VariableInterface {
		public static final int WFMTASK_STATE_SETNEW = 101;
		public static final int WFMTASK_STATE_UNSETNEW = 102;
		int taskState;
		boolean isNew = false;
		Date start;
		Date end;
		String startBy;
		String endBy;
		String createCond;
		int choice;
		List<String> choiceList;
		public WfmTask(int p_rg, String p_description,int p_order,String p_createcond) {
			super(p_rg, p_description,p_order);
			if(!StringUtils.isBlank(p_createcond)) createCond = p_createcond;
			// TODO Auto-generated constructor stub
		}
		
		public String getCreateCond() {
			return(createCond);
		}

		@Override
		public Date getStart() {
			// TODO Auto-generated method stub
			return start;
		}

		@Override
		public Date getEnd() {
			// TODO Auto-generated method stub
			return end;
		}

		@Override
		public double getProgress() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setStart(Date p_date) {
			// TODO Auto-generated method stub
			start = p_date;
		}

		@Override
		public void setEnd(Date p_date) {
			// TODO Auto-generated method stub
			end = p_date;
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
			switch(p_state) {
			case WFMTASK_STATE_SETNEW:
				UniLog.log1("state %s->%s (%s)", getStateLabel(taskState), getStateLabel(taskState), getStateLabel(p_state));
				isNew = true;
				break;
			case WFMTASK_STATE_UNSETNEW:
				UniLog.log1("state %s->%s (%s)", getStateLabel(taskState), getStateLabel(taskState), getStateLabel(p_state));
				isNew = false;
				break;
			default : 
				UniLog.log1("state %s->%s", getStateLabel(taskState), getStateLabel(p_state));
				taskState = p_state;
				break;
			}
		}
		
		@Override 
		public String getColor() {
			if(isNew) 
				return(WipJob.getStateColor(WipJob.JOB_STATE_LOCKED));
			else 
				return(WipJob.getStateColor(taskState));
		}

		@Override
		public String getStartBy() {
			// TODO Auto-generated method stub
			return startBy;
		}

		@Override
		public void setStartBy(String p_startby) {
			// TODO Auto-generated method stub
			startBy = p_startby;
		}

		@Override
		public String getEndBy() {
			// TODO Auto-generated method stub
			return endBy;
		}

		@Override
		public void setEndBy(String p_endby) {
			// TODO Auto-generated method stub
			endBy = p_endby;
		}

		@Override
		
		public boolean isLocked() {
			// TODO Auto-generated method stub
			return isNew;
		}

		public int getChoice() {
			// TODO Auto-generated method stub
			return choice;
		}

		public void setChoice(int p_choice) {
			// TODO Auto-generated method stub
			choice = p_choice;
		}

		public void setChoiceList(List<String> p_list) {
			// TODO Auto-generated method stub
			choiceList = p_list;
		}
		public List<String >getChoiceList() {
			return(choiceList);
		}
		
		public String getChoiceLabel() {
			if (choiceList != null && choice >= 0 && choice < choiceList.size())
				return choiceList.get(choice);
			return null;
		}

		@Override
		public String toString(String p_varname, int p_idx,boolean p_idxAbsolute) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Object collectObject(String p_varname, int p_idx, boolean p_idxAbsolute) throws CellException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Cell evalVariable(String p_varname, int p_idx, boolean p_idxAbsolute,Object p_recData) throws CellException {
			// TODO Auto-generated method stub
			if(p_varname.equals("choice")) {
				return(new Cell(choice));
			}
			return null;
		}

		@Override
		public int getDataType(String p_varname) {
			// TODO Auto-generated method stub
			return 0;
		}
		public static String getStateLabel(int p_state) {
			switch(p_state) {
				case WFMTASK_STATE_SETNEW: return("Setnew");
				case WFMTASK_STATE_UNSETNEW: return("Unsetnew");
			}
			return WipJob.getStateLabel(p_state);
		}
}
