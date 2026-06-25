package com.uniinformation.erpv4.wip;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.utils.whereclpar.Parser;
import com.uniinformation.wip.WipJob;
import com.uniinformation.wip.WipTask;

public class WfmJob extends WipJob {
	int rg;
	String id;
	String name;
	String title;
	Date start;
	Date end;
	String startBy;
	String endBy;
	Date deadLine;
	String viewid;
	String keystr;
	String keyfd;
	public WfmJob(int p_rg,String p_id,String p_viewid,String p_keyfd,String p_keystr) {
		rg = p_rg;
		id = p_id;
		deadLine = DateUtil.zeroTime;
		viewid = p_viewid;
		keyfd = p_keyfd;
		keystr = p_keystr;
	}
	public String getViewid() {
		return(viewid);
	}
	public String getKeyfd() {
		return(keyfd);
	}
	public String getKeystr() {
		return(keystr);
	}
	public int getRg() {
		return(rg);
	}
	public String getId() {
		return(id);
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public Date getStart() {
		return start;
	}
	public void setStart(Date startTime) {
		this.start = startTime;
	}
	public Date getEnd() {
		return end;
	}
	public void setEnd(Date endTime) {
		this.end = endTime;
	}
	public String getStartBy() {
		return startBy;
	}
	public void setStartBy(String startBy) {
		this.startBy = startBy;
	}
	public String getEndBy() {
		return endBy;
	}
	public void setEndBy(String endBy) {
		this.endBy = endBy;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getDeadLine() {
		return(deadLine);
	}
	public void setDeadLine(Date p_date) {
		deadLine = p_date;
	}
	
	@Override
	public String getStartFontColor() {
		if(deadLine.after(DateUtil.minTime)) {
			Date d;
			if(start == null || !start.after(DateUtil.minTime)) {
				d = new Date();
			} else {
				d = start;
			}
			if(d.after(deadLine)) {
				return("red");
			}
		}
		return(null);
	}
	@Override
	public String getEndFontColor() {
		if(deadLine.after(DateUtil.minTime)) {
			Date d;
			if(end == null || !end.after(DateUtil.minTime)) {
				d = new Date();
			} else {
				d = end;
			}
			if(d.after(deadLine)) {
				return("red");
			}
		}
		return(null);
	}
	
	@Override
	protected int getNextStateWhenComplete(WipTask parent,WipTask child) {
		try {
			String data = (String) getLinkData(parent,child);
			if(!StringUtils.isBlank(data)) {
				JSONObject jo = new JSONObject(data);
				String condition = jo.optString("linkCond");
				if(!StringUtils.isBlank(condition)) {
					if(((Condition) (new Parser(condition,(WfmTask) parent,null).parse())).eval(null)) {
						return(WipJob.JOB_STATE_AWAKED);
					} else {
						return(WipJob.JOB_STATE_ABORTED);
					}
					
				}
				return(WipJob.JOB_STATE_AWAKED);
			}
		} catch (Exception ex) {
					UniLog.log(ex);
		}
		return(super.getNextStateWhenComplete(parent, child));
	}
}
