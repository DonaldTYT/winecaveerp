package com.uniinformation.zkbi.erpv4;

import java.util.Date;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerExtendedReport;

public class ZkBiComposerExtendedArStmt extends ZkBiComposerExtendedReport {
	boolean statementReady = false;
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
		zkfName = "zkf/erpv4/ArStatement";
		detailIcon = "images/icons/zkweb/011-bill-25x25.png" ;	
		super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	}
	
	@Override
	protected void onZkFEvent(BiResult result,Event event) throws Exception {
		UniLog.log("zkf event catched");
		if(statementReady) {
			statementReady = false;
			resetBatchActionHandler(result);
		}
	}
	
	@Override
	public void refresh(final BiResult result, final Component p_comp, MultiSortMap sortMap, boolean p_doSearch){
		super.refresh(result, p_comp, sortMap, p_doSearch);
		Date sdate = result.getCellDate("stmt_sdate");
		Date edate = result.getCellDate("stmt_edate");
		if(sdate != null && edate != null && sdate.after(DateUtil.minDate) && edate.after(sdate)) {	
			if(!statementReady) {
				statementReady = true;
				resetBatchActionHandler(result);
			}
		}
	}
	
	@Override
    public Object getStateValue(String key) {
		if(key.equals("statementReady")) {
			return((Boolean) statementReady);
		}
		return(super.getStateValue(key));
    }
}
