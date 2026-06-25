package com.uniinformation.dynamic.aw;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.wip.BiResultWipTask;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.wip.WfmJob;
import com.uniinformation.erpv4.wip.WfmStep;
import com.uniinformation.erpv4.wip.WfmTask;
import com.uniinformation.erpv4.wip.WfmTaskUpdate;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.wip.ZkBiComposerJobG2;
import com.uniinformation.zkbi.wip.ZkBiComposerTaskG2;
import com.uniinformation.zkf.ZkForm;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public class WipBatchUpdate extends BiActionHandler{
	WfmTaskUpdate wipTaskUpdate = null;
	WfmJob wj = null;
	WfmTask wt = null;
	
	public WipBatchUpdate(ZkBiComposerBase p_bibase) {
		super(p_bibase);
	}
	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		if(biBase instanceof ZkBiComposerJobG2) {
			wipTaskUpdate = ((ZkBiComposerJobG2) biBase).getWipTaskUpdate();
		}
		if(biBase instanceof ZkBiComposerTaskG2) {
			wipTaskUpdate = ((ZkBiComposerTaskG2) biBase).getWipTaskUpdate();
		}
		if(wipTaskUpdate == null) {
			return (new ReturnMsg(false,"Error wipTaskUpdate got null"));
		}
		return (ReturnMsg.defaultOk);
	}
	
	protected void doUpdateJobStatus() throws Exception {
	}
	
	int taskRg=0;

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		int jobrg = p_result.getCellInt("wfmj_rg");
		UniLog.log("Mark Complete for job " + jobrg);
		try {
			p_result.fetchOneRecV(p_recIdx);
			if(p_result instanceof BiResultWipTask) {
				wipTaskUpdate.loadWorkFlow(null, p_result.getSessionHelper(), jobrg,0);
				wj = wipTaskUpdate.getWipJob();
				wt = (WfmTask) wj.getStep(WfmStep.makeNodeId(p_result.getCellInt("wfmjt_rg")));
			} else {
				wipTaskUpdate.loadWorkFlow(p_result, p_result.getSessionHelper(), jobrg,0);
				wj = wipTaskUpdate.getWipJob();
				wt = null;
			}
			doUpdateJobStatus();
			wipTaskUpdate.updateJobStatus();
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return (ReturnMsg.defaultOk);
	}
	@Override
	public ReturnMsg afterAction(BiResult p_br) {
		return (ReturnMsg.defaultOk);
	}
}
