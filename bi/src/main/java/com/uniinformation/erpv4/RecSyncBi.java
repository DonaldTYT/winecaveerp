package com.uniinformation.erpv4;

import org.json.JSONException;
import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.utils.UniLog;

public class RecSyncBi extends RecSync {
	
	abstract public class BiSyncHandler extends SyncHandler{
		BiResult br;
		protected boolean allowAdd = false;
//		protected boolean allowDelete = false;
		
		void setBr(BiResult p_br) {
			br = p_br;
		}
		protected BiResult getBr() {
			return(br);
		}
	}
	
	@Override
	protected ReturnMsg syncOneRecord(String p_view,String p_key,String p_jsonDetail) {
//		BiSyncHandler shdr = (BiSyncHandler) viewHash.get(p_view);
		BiSyncHandler shdr = null;
		SyncHandler synhdr = viewHash.get(p_view);
		if(! (synhdr instanceof BiSyncHandler)) {
			try {
				return(synhdr.syncRec(null, new JSONObject(p_jsonDetail)));
			} catch (JSONException jex) {
				UniLog.log(jex);
				return(ReturnMsg.defaultFail);
			}
		} else {
			shdr = (BiSyncHandler) synhdr;
		}
		if(shdr == null) return(ReturnMsg.defaultFail);
		shdr.getBr().clearCondition();
		shdr.getBr().addCustomCondition(p_key);
		shdr.getBr().query();
		boolean isAdd = false;
		if(shdr.getBr().getRowCount() == 0) {
			if(!shdr.allowAdd) return(ReturnMsg.defaultOk); // remote no such record, return OK to remove the pending update
			shdr.getBr().clearCurrentRec();
			isAdd = true;
		} else {
			if(shdr.getBr().getRowCount() != 1) return(ReturnMsg.defaultFail);
			if(!shdr.getBr().fetchOneRecV(0)) return(ReturnMsg.defaultFail);
		}
		
		try {
			ReturnMsg rtn;
			if(!shdr.getBr().beginWork()) return(ReturnMsg.defaultFail);
			rtn = shdr.getBr().lockRecordForUpdate();
			if(rtn != null && !rtn.getStatus()) {
				shdr.getBr().rollbackWork();
				return(rtn);
			}
			JSONObject jo = new JSONObject(p_jsonDetail);
			rtn = shdr.syncRec(shdr.getBr().getCurrentCollection(), jo);
			if(rtn != null) {
				if(rtn.getStatus()) {
					if(isAdd) 
						rtn = shdr.getBr().addCurrent();
					else
						rtn = shdr.getBr().updateCurrent();
					if(rtn == null || rtn.getStatus()) { // previous use of ReturnMsg return null if ok
						shdr.getBr().commitWork();
						// fall to line 329 to return defaultOK
					} else {
						shdr.getBr().rollbackWork();
						return(rtn);
					}
				} else {
					return(rtn);
				}
			} else {
				// if rtn == null, rollback work and don't do any update, but still return OK, to remove the syncrec task
				shdr.getBr().rollbackWork();
				// fall to line 329 to return defaultOK
			}
		} catch (Exception ex) {
			//UniLog.log(ex);  //andrew210119: too many exception log in clerp
			UniLog.log1("Exception:"+ex.getMessage());
			shdr.getBr().rollbackWork();
			return(ReturnMsg.defaultFail);
		}
		return(ReturnMsg.defaultOk);
	}
	protected ReturnMsg addOneView(String p_view,SyncHandler p_handler) {
		if(p_handler instanceof BiSyncHandler) {
			BiView bv = sessionHelper.getBiSchema().getViewByName(p_view);
			((BiSyncHandler) p_handler).setBr(bv.newBiResult(sessionHelper.getLoginId(), null, null, sessionHelper));
		}
		return(super.addOneView(p_view, p_handler));
	}
	
}
