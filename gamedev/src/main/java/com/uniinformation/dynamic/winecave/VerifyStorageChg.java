package com.uniinformation.dynamic.winecave;

import java.util.List;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.wc.BiResultStorageChg;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class VerifyStorageChg extends BiActionHandler implements JxActionListener {

	BiResultStorageChg br;

	public VerifyStorageChg(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		if(p_bibase != null) useAsync = p_bibase.getSessionHelper().getAllowBatchPrtdocAsync();
		else useAsync = false;
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		br = (BiResultStorageChg) p_result;
		try {
			br.beginWork();
		} catch(Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return(ReturnMsg.defaultOk);
	}

	@Override
	public ReturnMsg processAction(BiResult p_br,int p_idx) {
		br.fetchOneRecV(p_idx);
		try {
			List<String> errList = br.cal_storage_charge(true);
			if(errList != null && !errList.isEmpty()) {
				return(new ReturnMsg(false,String.join(System.lineSeparator(),errList)));
			}
		} catch(Exception ex) {
			br.rollbackWork();
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return(ReturnMsg.defaultOk);
	}

	@Override
	public ReturnMsg afterAction(BiResult p_result) {
		br.rollbackWork();
		return(ReturnMsg.defaultOk);
	}

	@Override
	public boolean isVisible(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(false);
		return(true);
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		if(p_isBatch) return(false);
		if(!p_br.getSessionHelper().hasAccessRight("#detstor")) return(true);
		if(p_br.inBeginWork()) return(true);
		return(false);
	}

	@Override
	public ReturnMsg isRunnable(BiResult br,boolean isBatch) {
		return(ReturnMsg.defaultOk);
	}

	@Override
	public void afterActionAsync(BiActionHandler.AfterActionCallback cb) {
		UniLog.log1("afterActionAsync start");
		ReturnMsg rtn = afterAction(null);
		biBase.hideProgressPanel();
		cb.callback(rtn);
	}

	@Override
	public boolean preserveListOrder() {
		return(true);
	}

	@Override
	public void actionPerformed(JxField field) {
	}
}
