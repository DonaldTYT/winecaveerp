package com.uniinformation.zkbi;

import com.uniinformation.bicore.BiResult;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.webcore.SessionHelper;

abstract public class BiActionHandler {
	static public final int ActionAccessMode_Custom = 0;
	static public final int ActionAccessMode_AddAccess = 1;
	static public final int ActionAccessMode_UpdateAccess = 2;
	static public final int ActionAccessMode_DeleteAccess = 3;
	static public final int ActionAccessMode_ReportAccess = 4;
	static public final int ActionAccessMode_ImportAccess = 5;
	static public final int ActionAccessMode_DetailAccess = 6;
	
	protected ZkBiComposerBase biBase;
	protected boolean useAsync;
	
	public BiActionHandler(ZkBiComposerBase p_bibase) {
		biBase = p_bibase;
	}
	
	abstract public ReturnMsg beforeAction(BiResult p_result,int cnt);
	abstract public ReturnMsg processAction(BiResult p_result,int p_recIdx);
	abstract public ReturnMsg afterAction(BiResult p_result);
	public void afterActionAsync(AfterActionCallback cb) {
	}
	
	public boolean isUseAsync() {
		return useAsync;
	}
	
	public interface AfterActionCallback {
		void callback(ReturnMsg rtn);
	}
	
	public void afterActionCallback(BiResult br,ReturnMsg rtn) {
	}
	
	public boolean isVisible(BiResult br,boolean isBatch) {
		return(true);
	}
	public boolean isDisabled(BiResult br,boolean isBatch) {
		return(false);
	}
	public ReturnMsg isRunnable(BiResult br,boolean isBatch) {
		return(ReturnMsg.defaultOk);
	}
}
