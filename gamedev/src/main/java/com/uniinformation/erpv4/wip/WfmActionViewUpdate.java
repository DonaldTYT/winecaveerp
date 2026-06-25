package com.uniinformation.erpv4.wip;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.wip.WfmActionInterface;
import com.uniinformation.erpv4.wip.WfmJob;
import com.uniinformation.erpv4.wip.WfmTask;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class WfmActionViewUpdate implements WfmActionInterface {

	@Override
	public ReturnMsg startAction(SessionHelper p_sh, WfmJob p_job, WfmTask p_task) {
		BiResult br = p_sh.getBiSchema().getViewByName(p_job.getViewid()).newBiResult(p_sh.getLoginId(), null, null, p_sh);
		br.clear();
		br.clearCondition();
		br.addCustomCondition(String.format("%s = '%s'",p_job.getKeyfd(),p_job.getKeystr()));
		br.query();
		if(br.getRowCount() <= 0) {
			return(new ReturnMsg(false,String.format("PO %s not found",p_job.getKeystr())));
		}  else if(br.getRowCount() > 1) {
			return(new ReturnMsg(false,String.format("PO %s not unique",p_job.getKeystr())));
		}
		try {
			ReturnMsg rtn;
			br.fetchOneRecV(0);
			rtn = br.assigneValues(p_task.getCreateCond(),false);
			if(rtn != null && !rtn.getStatus()) return(rtn);
			rtn = br.updateCurrent();
			if(rtn == null) return(ReturnMsg.defaultOk); else return(rtn);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,String.format("PO %s got Exception %s",p_job.getKeystr(),ex.toString())));
		}
	}

	@Override
	public ReturnMsg getActionStatus(SessionHelper p_sh, WfmJob p_job, WfmTask p_task) {
		BiResult br = p_sh.getBiSchema().getViewByName(p_job.getViewid()).newBiResult(p_sh.getLoginId(), null, null, p_sh);
		br.clear();
		br.clearCondition();
		br.addCustomCondition(String.format("%s = '%s'",p_job.getKeyfd(),p_job.getKeystr()));
		br.query();
		if(br.getRowCount() <= 0) {
			return(new ReturnMsg(false,String.format("PO %s not found",p_job.getKeystr())));
		}  else if(br.getRowCount() > 1) {
			return(new ReturnMsg(false,String.format("PO %s not unique",p_job.getKeystr())));
		}
		br.loadOneRecV(0);
		try {
			ReturnMsg rtn = br.assigneValues(p_task.getCreateCond(),true);
			return(rtn);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,String.format("PO %s got Exception %s",p_job.getKeystr(),ex.toString())));
		}
	}

}
