package com.uniinformation.dynamic.propertymgmt;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.propertymgmt.BiResultPayment;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.jxapp.propertymgmt.payment;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiMsgbox;

public class SyncPaymentItem extends BiActionHandler implements JxActionListener {
	private SessionHelper sh;

	public SyncPaymentItem(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		useAsync = p_bibase != null ? p_bibase.getSessionHelper().getAllowBatchPrtdocAsync() : false;
	}

	public SyncPaymentItem() {
		this(null);
	}

	@Override
	public void actionPerformed(JxField field) {
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		BiResultPayment br = (BiResultPayment) jxf.getBr();
		br.syncPayItemFromPayUnit(jxf);
		((payment)jxf).sortPayitemListbox();
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result, int cnt) {
		sh = p_result.getSessionHelper();
		return null;
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		try {
			if (!p_result.fetchOneRecV(p_recIdx)) 
				return new ReturnMsg(false, sh.getLabel("Fetch Record failed"));
			BiResultPayment br = (BiResultPayment)p_result;
			br.syncPayUnitFromPayItem();
			ReturnMsg rtn = br.syncPayItemFromPayUnit(null);
			if (rtn != null && !rtn.getStatus())
				throw new Exception(rtn.getMsg());
			rtn = br.updateCurrent();
			if (rtn != null && !rtn.getStatus())
				throw new Exception(rtn.getMsg());
			return ReturnMsg.defaultOk;
		} catch (Exception ex) {
			UniLog.log(ex);
			return new ReturnMsg(false, String.format(sh.getLabel("Sync Payment item %d Failed:%s"), p_recIdx, ex.getMessage()));
		}
	}

	@Override
	public ReturnMsg afterAction(BiResult p_result) {
		return null;
	}

	@Override
	public void afterActionAsync(BiActionHandler.AfterActionCallback cb) {
		biBase.hideProgressPanel();
		cb.callback(ReturnMsg.defaultOk);
	}

	@Override
	public void afterActionCallback(BiResult br, ReturnMsg rtn) {
		if (rtn == null || rtn.getStatus())
			ZkBiMsgbox.show("計算完成");
	}
}
