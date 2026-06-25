package com.uniinformation.dynamic.aw;

import com.uniinformation.zkbi.ZkBiComposerBase;

public class WipBatchJobComplete extends WipBatchUpdate {

	public WipBatchJobComplete(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doUpdateJobStatus() throws Exception {
			super.doUpdateJobStatus();
//			wipTaskUpdate.getWipJob().switchEnd(true);
			wj.switchEnd(true);
	}
}
