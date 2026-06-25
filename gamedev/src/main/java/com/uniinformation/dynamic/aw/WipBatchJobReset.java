package com.uniinformation.dynamic.aw;

import com.uniinformation.zkbi.ZkBiComposerBase;

public class WipBatchJobReset extends WipBatchUpdate {

	public WipBatchJobReset(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void doUpdateJobStatus() throws Exception {
			super.doUpdateJobStatus();
			wj.switchStart(false);
			wj.switchStart(true);
	}
}
