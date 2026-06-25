package com.uniinformation.zkbi.erpv4;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.RecSync;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;


public class ZkBiComposerMoGeneric extends ZkBiComposerBase {
	   protected void setupExtraButton(final BiResult result)
	    {
			addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbRecsync","Force Resync","fa-user",
				new BiActionHandler(this) {

					@Override
					public ReturnMsg beforeAction(BiResult p_result,int cnt) {
						return(null);
					}

					@Override
					public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
						result.fetchOneRecV(p_recIdx);
						RecSync.updateOneRecord(getSessionHelper().getAgent(), result.getView().getName(), result.getCurrentCollection());
						return(null);
					}

					@Override
					public ReturnMsg afterAction(BiResult p_br) {
						return(null);
					}
				}
			);
			
			addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbVerify","Verify Remote","fa-user",
					new BiActionHandler(this) {

						@Override
						public ReturnMsg beforeAction(BiResult p_result,int cnt) {
							return(null);
						}

						@Override
						public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
							/*
							result.fetchOneRecV(p_recIdx);
							RecSync.updateOneRecord(getSessionHelper().getAgent(), result.getView().getName(), result.getCurrentCollection());
							*/
							return(null);
						}

						@Override
						public ReturnMsg afterAction(BiResult p_br) {
							ZkUtil.normMsg("Verify Completed");
							return(ReturnMsg.defaultOk);
						}
					}
				);
	    }	
}
