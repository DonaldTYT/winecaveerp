package com.uniinformation.zkbi.erpv4;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Messagebox;

import com.uniinformation.bicore.AggregateOrPivot.AggregateRec;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.AggregateOrPivotHeader;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiLedgerReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultLedger;
import com.uniinformation.bicore.erpv4.BiResultReceivableLedger;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.reports.ZkBiComposerLedgerReport;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerReceivableLedgerG2 extends ZkBiComposerLedgerReport {
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
		if(zkfName == null) zkfName = "zkf/reports/ReceivableLedgerReport.zul";	
		super.doAfterCompose(comp);
		listboxHeightAdjust = 100;
	}
	@Override
	protected void resetListHeader(BiResult result) {
			Cell c = rptCol.getCell("rgSummaryOrDetail");
			if(c.getInt() == 0) {
				result.resetViewList();
			} else {
				Vector<BiColumn> vl = (Vector <BiColumn>) result.getListColumns().clone();
				for(BiColumn bc:vl) {
					if(bc.getLabel().equals("jn_xdate")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("jn_xno")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("jn_seq")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("tr_srcno")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("tr_post")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("tr_flag")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("tr_jcode")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("jn_desc0")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("jn_desc1")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("sih_srcref")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("jn_gldpcode")) result.hideViewColumn(bc);
					if(bc.getLabel().equals("gldp_gldpcode")) result.hideViewColumn(bc);
					if(bc.getLabel().startsWith("cldoc")) {
						result.hideViewColumn(bc);
					}
				}
			}
			super.resetListHeader(result);
	}
	/* Override this method if sum pivoted aggregate is meanlingless and should be hide away */
	@Override
    protected boolean isAggregateVisible(BiResult p_result,AggregateOrPivotHeader p_aop,int p_idx) {
		AggregateRec aggRec = p_aop.getAggregate(p_idx);
		if(!p_aop.isSubTotalColumn(p_idx)) {
			if(
			"jn_balance".equals(aggRec.getKey()) 
//			|| "jn_namount".equals(aggRec.getKey()) 
					) {
				return(true);
			} else {
				return(false);
			}
		}
		Cell c = rptCol.getCell("rgSummaryOrDetail");
		if(c.getInt() != 0) {
			if(
			"jn_balance".equals(aggRec.getKey()) 
			|| "jn_lbalance".equals(aggRec.getKey()) 
					) {
				return(false);
			}	
		}
		
    	return(true);
    }	
	
	
	/*
	@Override
	protected ReturnMsg setAdditionalQueryCondition(BiResult result) {
		ReturnMsg rtn = super.setAdditionalQueryCondition(result);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		if(!getSessionHelper().hasAccessRight("#multicomp")) {
			result.addCustomCondition("loc_cocode = '"+Erpv4Config.getDefaultCoCode(getSessionHelper()) + "'");
			
		}
		return(ReturnMsg.defaultOk);
	}
	*/
    protected void setupExtraButton(final BiResult result)
    {
    	super.setupExtraButton(result);
		Button btPrintStatement;
    	if(masterWin.hasFellow("btPrintStatement")) {
    		btPrintStatement = (Button) masterWin.getFellow("btPrintStatement");
    	} 
    	else {	
	        btPrintStatement = new ZkBiButton();
	        btPrintStatement.setLabel("Print Statement");
	        btPrintStatement.setAttribute("tlkey", "bt_master_print_statement");
	        btPrintStatement.setId("btPrintStatement");
	        //batchActionBar.appendChild(btnPrintLabel);
	        abHelper.addButton(btPrintStatement, "fa-user");
    	} 
        btPrintStatement.addEventListener("onClick",
        	new ZkBiEventListener() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					// TODO Auto-generated method stub
					UniLog.log("print statement");
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ReturnMsg rtn = ((BiResultReceivableLedger) result).printStatement(bos,null);
				if(rtn.getStatus()) {
					String ss = "";
					ZkUtil.showPdfDialog((Component) masterWin, getSessionHelper(), bos.toByteArray(), "Statement-"+ss);
				} else {
					Messagebox.show(rtn.getMsg());
				}
					
					
				}
        }
        );

//		Button btGenDn;
//    	if(masterWin.hasFellow("btGenDn")) {
//    		btGenDn = (Button) masterWin.getFellow("btGenDn");
//    	} 
//    	else {	
//	        btGenDn = new ZkBiButton();
//	        btGenDn.setLabel("Generate Statement");
//	        btGenDn.setId("btGenDn");
//	        //batchActionBar.appendChild(btnPrintLabel);
//	        abHelper.addButton(btGenDn, "fa-user");
//    	} 
//        btGenDn.addEventListener("onClick",
//        	new ZkBiEventListener() {
//				@Override
//				public void onZkBiEvent(Event event) throws Exception {
//					Date d0 = ((BiResultLedger) result).getRptStartDate();
//					Date d1 = ((BiResultLedger) result).getRptEndDate();
//					ZkBiComposerOsDeli.autoGenerateDn(getSessionHelper(), d0, d1);
//					
//				}
//        }
//        );
    	
    }
}
