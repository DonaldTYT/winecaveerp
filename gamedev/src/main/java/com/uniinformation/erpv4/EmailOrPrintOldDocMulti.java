package com.uniinformation.erpv4;

import com.uniinformation.zkbi.ZkBiComposerBase;

/**
 * Named base for old-document actions offering Email/Print and All/Selected.
 * The implementation is inherited from {@link PrintOrMailOldDocMulti}.
 */
public abstract class EmailOrPrintOldDocMulti extends PrintOrMailOldDocMulti {
	protected EmailOrPrintOldDocMulti() {
		super();
	}

	protected EmailOrPrintOldDocMulti(ZkBiComposerBase p_bibase) {
		super(p_bibase);
	}
}
