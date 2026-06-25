package com.uniinformation.dynamic.aw;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkf.ZkCellActionInterface;


public class ActionMarkDelivered implements ZkCellActionInterface {
	static public enum RUNMODE{
		DN_MARKDELIVERED
	};
	
	RUNMODE curMode;
	BiResult br;

//	@Wire
//	Button btnMarkDelivered;

	@Override
	public void init(Component arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void afterCompose(CellCollection arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeMapCollection(SessionHelper p_sh) {
		try {
			String ss = p_sh.getPassportContent();
			if(ss != null) {
				JSONObject jo = new JSONObject(ss);
				String dnno = jo.optString("dnno");
				if(!StringUtils.isBlank(dnno)) {
//					curMode = RUNMODE.DN_MARKDELIVERED;
					SelectUtil su = p_sh.getBiSchema().getSelectUtil();
					TableRec tr = su.getQueryResult("select stm_ref1 from stmov where stm_status = 'Confirmed' and stm_ref1 = '"+dnno+"'");
					if(tr.getRecordCount() >= 1) {
						JSONObject jox = new JSONObject();
						jox.put("customCondition","wfmj_id = '" + tr.getFieldString("stm_ref1")+"'");
						String url = "/";
						url +="zkbiloader.html?action=browse&viewid=wip.WfmWipTask&page_id=WfmWipTask_01&zul=zkbiloader_wfmtask.zul&composer=wip.ZkBiComposerTaskG2";
						String key = p_sh.putOneTimeData( jox);
						url += "&querycondition="+key;
						Executions.sendRedirect(url);
						return;
					}
					
					return;
				}
				String jobno = jo.optString("jobno");
				if(!StringUtils.isBlank(jobno)) {
					SelectUtil su = p_sh.getBiSchema().getSelectUtil();
					TableRec tr = su.getQueryResult("select inv_invno from quotation where inv_invno <> '' and inv_quostatus = 'Confirmed' and inv_jobno = '"+jobno+"'");
					if(tr.getRecordCount() >= 1) {
						JSONObject jox = new JSONObject();
						jox.put("customCondition","wfmj_id like '" + tr.getFieldString("inv_invno") + "%'");
						String url = "/";
						url +="zkbiloader.html?action=browse&viewid=wip.WfmWipTask&page_id=WfmWipTask_01&zul=zkbiloader_wfmtask.zul&composer=wip.ZkBiComposerTaskG2";
						String key = p_sh.putOneTimeData( jox);
						url += "&querycondition="+key;
						Executions.sendRedirect(url);
						return;
					}
//					Executions.sendRedirect("/zkbiloader.html?action=browse&viewid=wip.WfmWipTask&page_id=WfmWipTask_01&zul=zkbiloader_wfmtask.zul&composer=wip.ZkBiComposerTaskG2");
				}
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		Executions.sendRedirect("/zkf/mError001.zul");
	}

	@Override
	public void processActionByComposer(String p_eventName, Component p_target, boolean p_needResponse,
			InputStream p_upload) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
