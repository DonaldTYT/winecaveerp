package com.uniinformation.zkbi;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxActionListener;
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
	protected boolean delayStart;
	JxActionListener startAction;
	
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
	
	public boolean preserveListOrder () {
		return(false);
	}

	/**
	 * Optional read-only AI context describing this action.
	 *
	 * <p>Returning {@code null} keeps the action out of AI Help. The composer
	 * only exposes a contributed context when the action's actual button is
	 * visible to the current user. Execution of the action is never implied by
	 * this context.</p>
	 */
	public ZkBiAiAgentContext getAiAgentContext() {
		return null;
	}
	
	public final void delayStart() {
		if(startAction != null) {
			startAction.actionPerformed(null);
		}
	}

	public final void delayAbort() {
		startAction = null;
	}
}
