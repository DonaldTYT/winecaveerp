package com.uniinformation.dynamic.aw;

import com.uniinformation.wip.WipJob;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class WipBatchTaskComplete extends WipBatchUpdate {

	public WipBatchTaskComplete(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doUpdateJobStatus() throws Exception {
			super.doUpdateJobStatus();
//			wipTaskUpdate.getWipJob().switchEnd(true);
			WipJob.taskSwitchStart(wj, wt, true);
			WipJob.taskSwitchEnd(wj, wt, true);
	}
}
