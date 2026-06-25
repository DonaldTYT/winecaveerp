package com.uniinformation.zkbi.erpv4;

import java.util.Date;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultBalanceSheet;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkf.ZkForm;
public class ZkBiComposerTrialBalance extends ZkBiComposerReport {
	CellCollection rptCol;
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
	    					.toVector()
	    			);
	    }
   		BiResultBalanceSheet bs = (BiResultBalanceSheet) result;
	    bs.setBlStart(rptCol.getCell("sdate").getDate());
		bs.setBlEnd(rptCol.getCell("edate").getDate());
//		rptCol.getCell("sdate").setItemList(
//				new VectorUtil()
//					.addElement("2021/03/01")
//					.addElement("2021/04/01")
//					.addElement("2021/05/01")
//					.toVector()
//				);
		
		
/*		
		rptCol.getCell("sdate").addAction(new CellValueAction() {

			@Override
			public void cellAction_onchange(Cell p_value) throws CellException {
				// TODO Auto-generated method stub
		    		BiResultBalanceSheet bs = (BiResultBalanceSheet) result;
		    		bs.setBlStart(rptCol.getCell("sdate").getDate());
		    		bs.setBlEnd(rptCol.getCell("edate").getDate());
		    		if(Erpv4Config.isMultiDepartment(getSessionHelper())) {
		    			bs.setDepartment(rptCol.getCellString("mdDepartment"));
		    		}
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
		    		BiResultBalanceSheet bs = (BiResultBalanceSheet) result;
		    		bs.setBlStart(rptCol.getCell("sdate").getDate());
		    		bs.setBlEnd(rptCol.getCell("edate").getDate());
		    		if(Erpv4Config.isMultiDepartment(getSessionHelper())) {
		    			bs.setDepartment(rptCol.getCellString("mdDepartment"));
		    		}
		    		refresh(result,masterWin,-1,true,true); 
			}

			@Override
			public void cellAction_onfree() throws CellException {
				// TODO Auto-generated method stub
			}
		}
		);
		if(Erpv4Config.isMultiDepartment(getSessionHelper())) {
		rptCol.getCell("mdDepartment").addAction(new CellValueAction() {

			@Override
			public void cellAction_onchange(Cell p_value) throws CellException {
				// TODO Auto-generated method stub
		    		BiResultBalanceSheet bs = (BiResultBalanceSheet) result;
		    		bs.setBlStart(rptCol.getCell("sdate").getDate());
		    		bs.setBlEnd(rptCol.getCell("edate").getDate());
		    		bs.setDepartment(rptCol.getCellString("mdDepartment"));
		    		refresh(result,masterWin,-1,true,true); 
			}

			@Override
			public void cellAction_onfree() throws CellException {
				// TODO Auto-generated method stub
			}
		}
		);
		}
*/		
		
    	try {
    		zkf1.mapCellCollection(rptCol,new EventListener() {
		    	@Override
		    	public void onEvent(Event arg0) throws Exception {
		    		// TODO Auto-generated method stub
		    		UniLog.log("Trialbalance onEvent " + arg0.getName());
		    		if(arg0.getName().equals(Events.ON_CHANGE)) {
		    		BiResultBalanceSheet bs = (BiResultBalanceSheet) result;
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
	
}
