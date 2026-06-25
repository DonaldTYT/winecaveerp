package com.uniinformation.erpv4.wip;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.webcore.SessionHelper;

public interface WfmActionInterface {
	public ReturnMsg startAction(SessionHelper p_sh,WfmJob p_job,WfmTask p_task);
	public ReturnMsg getActionStatus(SessionHelper p_sh,WfmJob p_job,WfmTask p_task);
}
