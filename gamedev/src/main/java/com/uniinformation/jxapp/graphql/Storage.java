package com.uniinformation.jxapp.graphql;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;

public class Storage extends JxZkBiBase {
	@Override
	public void afterBind() {
		super.afterBind();
		LOCK_RECORD_FOR_UPDATE = true;
	}

	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br,mode);
		jxAdd("list_" + JxZkBiBase.replaceViewName("graphql.StorageDet"))
				.setAttribute("paging","withfilter");
		jxAdd("list_" + JxZkBiBase.replaceViewName("graphql.StmpostExtSM"))
				.setAttribute("paging","withfilter");
	}
}
