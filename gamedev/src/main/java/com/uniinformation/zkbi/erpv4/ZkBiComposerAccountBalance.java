package com.uniinformation.zkbi.erpv4;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultAccountBalance;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerAccountBalance extends ZkBiComposerBase{
	CellCollection rptCol;
	
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
		super.doAfterCompose(comp);
		adjListboxHeight(75);
	}
	
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
    	Div rpth = new Div();
    	zkbiListTop.getParent().insertBefore(rpth,zkbiListTop);
	    final ZkForm zkf1 = new ZkForm(rpth,"zkf/erpv4/erpv4TrialBalance.zul");
		Date td = DateUtil.today();
		Date md = Erpv4Config.getMaxPcStart(result.getSelectUtil(), Erpv4Config.getDefaultCoCode(getSessionHelper()));
		if(td.before(md)) md = DateUtil.monthStart(td);
	    rptCol = new CellCollection();
    	rptCol.addCell("rptCondition", new Cell(""));
    	rptCol.addCell("sdate", new Cell(md));
    	rptCol.addCell("edate", new Cell(td));
	    if(Erpv4Config.isMultiDepartment(getSessionHelper())) {
	    	Cell cc = rptCol.addCell("mdDepartment", new Cell(""));
	    	cc.setItemList(
	    				new VectorUtil()
	    					.addElement("")
	    					.addElement("DP")
	    					.addElement("D0")
	    					.addElement("D1")
	    					.addElement("D2")
	    					.addElement("D3")
	    					.addElement("D4")
	    					.addElement("D5")
	    					.addElement("D6")
	    					.addElement("D7")
	    					.addElement("D8")
	    					.addElement("D9")
	    					.toVector()
	    			);
	    }
   		BiResultAccountBalance bs = (BiResultAccountBalance) result;

   		/*
   		rptCol.getCell("sdate").setItemList(
				new VectorUtil()
					.addElement("2021/03/01")
					.addElement("2021/04/01")
					.addElement("2021/05/01")
					.toVector()
				);   		
				*/
	    bs.setBlStart(rptCol.getCell("sdate").getDate());
		bs.setBlEnd(rptCol.getCell("edate").getDate());
		/*
		rptCol.getCell("sdate").addAction(new CellValueAction() {

			@Override
			public void cellAction_onchange(Cell p_value) throws CellException {
				// TODO Auto-generated method stub
		    		BiResultAccountBalance bs = (BiResultAccountBalance) result;
		    		bs.setBlStart(rptCol.getCell("sdate").getDate());
		    		bs.setBlEnd(rptCol.getCell("edate").getDate());
		    		refresh(result,masterWin,-1,true,true); 
			}

			@Override
			public void cellAction_onfree() throws CellException {
				// TODO Auto-generated method stub
			}
		}
		);
		
		rptCol.getCell("edate").addAction(new CellValueAction() {

			@Override
			public void cellAction_onchange(Cell p_value) throws CellException {
				// TODO Auto-generated method stub
		    		BiResultAccountBalance bs = (BiResultAccountBalance) result;
		    		bs.setBlStart(rptCol.getCell("sdate").getDate());
		    		bs.setBlEnd(rptCol.getCell("edate").getDate());
		    		refresh(result,masterWin,-1,true,true); 
			}

			@Override
			public void cellAction_onfree() throws CellException {
				// TODO Auto-generated method stub
			}
		}
		);
		*/
    	try {
    		zkf1.mapCellCollection(rptCol,new EventListener() {
		    	@Override
		    	public void onEvent(Event arg0) throws Exception {
		    		// TODO Auto-generated method stub
		    		if(arg0.getName().equals(Events.ON_CHANGE)) {
		    		BiResultAccountBalance bs = (BiResultAccountBalance) result;
		    		bs.setBlStart(rptCol.getCell("sdate").getDate());
		    		bs.setBlEnd(rptCol.getCell("edate").getDate());
		    		if(Erpv4Config.isMultiDepartment(getSessionHelper())) {
		    			bs.setDepartment(rptCol.getCellString("mdDepartment"));
		    		}
		    		refresh(result,masterWin,-1,true,true); 
		    		}
		    	}
	    	}
    		);
    	} catch(Exception ex) {
    		UniLog.log(ex);
    	}
	}	
	
	@Override
	public void biBaseClose(BiResult p_br) {
		super.biBaseClose(p_br);
		try {
			rptCol.getCell("sdate").setMode(Cell.VMODE_NORMAL);
			rptCol.getCell("edate").setMode(Cell.VMODE_NORMAL);
		} catch (CellException cex) {
			UniLog.log(cex);
		}
	}
	@Override
	public void biBaseOpen() {
		super.biBaseOpen();
		try {
			rptCol.getCell("sdate").setMode(Cell.VMODE_DISPONLY);
			rptCol.getCell("edate").setMode(Cell.VMODE_DISPONLY);
		} catch (CellException cex) {
			UniLog.log(cex);
		}
	}


	@Override
    protected void setupExtraButton(final BiResult result)
    {
		super.setupExtraButton(result);
		final Hashtable<BiReportInterface.ledgerColumns,String> fdHash = new Hashtable<BiReportInterface.ledgerColumns,String>();
		fdHash.put(BiReportInterface.ledgerColumns.st_icode,"ca_ano");
		fdHash.put(BiReportInterface.ledgerColumns.st_iname,"ca_aname");
		fdHash.put(BiReportInterface.ledgerColumns.lg_date,"jn_xdate");
		fdHash.put(BiReportInterface.ledgerColumns.stm_ref1,"tr_srcno");
		fdHash.put(BiReportInterface.ledgerColumns.stm_ref2,"jn_desc0");
		fdHash.put(BiReportInterface.ledgerColumns.stmd_openbal,"jn_begbal");
		fdHash.put(BiReportInterface.ledgerColumns.stmd_inqty,"jn_pamount");
		fdHash.put(BiReportInterface.ledgerColumns.stmd_outqty,"jn_namount");
		fdHash.put(BiReportInterface.ledgerColumns.stmd_closebal,"jn_bal");
		fdHash.put(BiReportInterface.ledgerColumns.stmd_openamt,"jn_lbegbal");
		fdHash.put(BiReportInterface.ledgerColumns.stmd_inamount,"jn_lpamount");
		fdHash.put(BiReportInterface.ledgerColumns.stmd_outamount,"jn_lnamount");
		fdHash.put(BiReportInterface.ledgerColumns.stmd_closeamt,"jn_lbal");
//		fdHash.put(BiReportInterface.ledgerColumns.stmd_avcost,"jn_avrate");
		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbPrintLedger","Print Ledger","fa-user",
				new LedgerReportBiActionHandler(this,"erpv4.JnDetail", fdHash, sessionHelper, masterWin,
						new LedgerReportBiActionHandler.OnReportInit() {

							@Override
							public void onInit(LedgerReportBiActionHandler p_hdr) {
								p_hdr.setFromDate(rptCol.getDate("sdate"));
								p_hdr.setToDate(rptCol.getDate("edate"));
								p_hdr.setSubTitle("Ledger Report");
								p_hdr.setOutputFileName("ledger_"+DateUtil.toDateString(rptCol.getDate("sdate"),"yyyymmdd")+"-"+DateUtil.toDateString(rptCol.getDate("edate"),"yyyymmdd"));
								p_hdr.setShowBreakDown(true);
								p_hdr.setOutputType(LedgerReportBiActionHandler.OUTPUT_Excel);
							}
						}
				)
			);	
    }
	
}
