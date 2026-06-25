package com.uniinformation.zkbi.reports;

import java.util.Date;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zul.Button;

import com.uniinformation.bicore.AggregateOrPivot.AggregateRec;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.UniLog;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.AggregateOrPivotHeader;
import com.uniinformation.bicore.BiAsAtReportInterface;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiLedgerReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.reports.ZkBiComposerAggregateReport;

public class ZkBiComposerAsAtReport extends ZkBiComposerAggregateReport {
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
		zkfName = "zkf/reports/AsAtReport.zul";	
		listboxHeightAdjust=60;
		hasAUDColumn = true;
		super.doAfterCompose(comp);
	}

	@Override
	protected ReturnMsg setAdditionalQueryCondition(BiResult result) {
    	Cell cdc = rptCol.testCell("rptCondition");
    	if(cdc != null) {
   		try {
    	String conditions = inputFieldsList.getCustomCondition();
    	if(conditions != null && !conditions.equals(""))  {
    		cdc.set(BiCellCollection.translateCond(result.getView(),conditions,result));
		} else {
    		cdc.set("Anything");
		}	
   		} catch (CellException cex) {
   			UniLog.log(cex);
   		}
    	}
		
		Date d1 = rptCol.getDate("edate");
		if(d1 == null) d1 = DateUtil.zeroDate;
		if(!d1.after(DateUtil.minDate) || !d1.after(DateUtil.minDate)) {
			return(new ReturnMsg(false,"Please Select Closing Date"));
		}
		result.addCustomCondition( ((BiAsAtReportInterface) result).getAsAtColumn() + " <= '" + DateUtil.toDateString(d1, "yyyy/mm/dd") + "'");
		if(!getSessionHelper().hasAccessRight("#multicomp")) {
			result.addCustomCondition("loc_cocode = '"+Erpv4Config.getDefaultCoCode(getSessionHelper()) + "'");
			
		}
		return(ReturnMsg.defaultOk);
	}

	/* Override this method if sum pivoted aggregate is meanlingless and should be hide away */
	@Override
    protected boolean isAggregateVisible(BiResult p_result,AggregateOrPivotHeader p_aop,int p_idx) {
		AggregateRec aggRec = p_aop.getAggregate(p_idx);
		/*
		if(
			"stmd_avcost".equals(aggRec.getKey()) 
//			|| "sih_ltotal".equals(aggRec.getKey()) 
			) {
			if(!p_aop.isSubTotalColumn(p_idx)) return(false);
		}
		*/
		if(!p_aop.isSubTotalColumn(p_idx)) {
			if(((BiAsAtReportInterface) p_result).skipForPivot(aggRec.getKey(),p_aop)) return(false);
		} else {
			if(((BiAsAtReportInterface) p_result).skipForSummary(aggRec.getKey(),p_aop)) return(false);
		}
    	return(true);
    }
	@Override
	protected void onSetupParameterChange(BiResult result,String p_id) {
		if(p_id.equals("useFifoAging")) {
			boolean needResetHeader = ((BiAsAtReportInterface)result).setFifoAging(rptCol.getCell("useFifoAging").getBoolean());
    		if(needResetHeader) {
    			result.resetViewList();
    			resetListHeader(result);
    		}
		}
	} 

	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
    	if(rptCol.testCell("useFifoAging") != null) {
    		boolean needResetHeader = ((BiAsAtReportInterface)result).setFifoAging(rptCol.getCell("useFifoAging").getBoolean());
//    		if(needResetHeader) {
//    			result.resetViewList();
//    			resetListHeader(result);
//    		}
    	}
	}
	
	Object trStatList;
	protected void after_refresh(BiResult result) {
		trStatList = result.getResultStat();
	}
	
	protected void regenAggregateAndPivot(BiResult p_result) throws Exception {
		super.regenAggregateAndPivot(p_result);
		if(bahHash != null) {
			for(String batchButtonId : bahHash.keySet()) {
				BiActionHandler bah = bahHash.get(batchButtonId);
				Button btn = (Button) queryBar.getFellowIfAny(batchButtonId, true);
				btn.setDisabled(bah.isDisabled(p_result, true));
			}
		}
	}
	
}
