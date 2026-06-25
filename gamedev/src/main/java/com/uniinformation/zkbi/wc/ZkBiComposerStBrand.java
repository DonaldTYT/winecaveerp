package com.uniinformation.zkbi.wc;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.wc.StBrand;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerStBrand extends ZkBiComposerBase {
	@Override
    protected void setupExtraButton(final BiResult result)
    {
		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbCopyImg","Copy Image","fa-user",
			new BiActionHandler(this) {

				@Override
				public ReturnMsg beforeAction(BiResult p_result,int cnt) {
					// TODO Auto-generated method stub
					try {
						result.beginWork();
						return(ReturnMsg.defaultOk);
					} catch (Exception ex) {
						UniLog.log(ex);
						return(new ReturnMsg(false,"Begin work failed"));
					}
				}

				@Override
				public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
					// TODO Auto-generated method stub
					try {
						p_result.fetchOneRecV(p_recIdx);
						return(StBrand.copyImageFromStockImages(p_result));
					} catch (Exception ex) {
						UniLog.log(ex);
						result.rollbackWork();
						return(new ReturnMsg(false,ex.toString()));
					}
				}

				@Override
				public ReturnMsg afterAction(BiResult p_br) {
					// TODO Auto-generated method stub
					try {
						result.commitWork();
						return(ReturnMsg.defaultOk);
					} catch (Exception ex) {
						UniLog.log(ex);
						return(new ReturnMsg(false,"Begin work failed"));
					}
				}
			}
		);
    }
}
