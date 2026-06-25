package com.uniinformation.zkbi.wc;


import java.io.ByteArrayOutputStream;
import java.util.HashSet;

import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.winecave.Winelist;
import com.uniinformation.zkbi.JxZkBiBaseCallback;
import com.uniinformation.zkbi.ZkBiComposerAnalysis;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkbi.ZkBiComposerAnalysis.AGGREGATES;
import com.uniinformation.zkbi.erpv4.ZkBiComposerSalesAnalysis;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerStockStatus extends ZkBiComposerSalesAnalysis {
	Object currentEditObject = null;
	class PopupControl  {
		Window popupPoScr = null;
		BiResult popupPoBr = null;
		BiResult listBr = null;
		JxZkBiBase popupJx = null;	
	
		PopupControl (String p_viewName,BiResult p_br) throws Exception {
			listBr = p_br;
   			popupPoScr = ZkUtil.newPopupWindow("Test Popup",masterWin);
			popupPoScr.setWidth("100%");
			popupPoScr.setHeight("100%");
   			popupPoScr.setMaximizable(true);
   			popupPoScr.setSizable(true);
			popupPoScr.setContentStyle("overflow:auto;");
			BiSchema schema = (BiSchema) sessionHelper.getSessionData("biSchema");
			if(schema == null) schema = BiSchema.loadSchema(sessionHelper);
			BiView view = schema.getViewByName(p_viewName);
			UniLog.log("queryResult view:"+view);
			popupPoBr = view.newBiResult(sessionHelper.getLoginId(),null,null,sessionHelper);
			popupJx = JxZkBiBase.buildDetailWindow(popupPoBr, popupPoScr, false, true, 
			new JxZkBiBaseCallback()  {
				public void biBaseRefresh(BiResult p_br) {
				}
				public void biBaseOpen() {
				}
				public void biBaseRefreshItem(Object p_obj) {
				}
				public void biBaseRefreshListitems(Object p_obj) {
				}
				public void biBaseClose(BiResult p_br) {
					if(p_br.getLastUpdate() == null) return;
					// direct call refresh don't update the screen list , use post event as in-trim solution
//					Events.echoEvent(Events.ON_CLICK, btReload, null);
//       		    	refresh(p_br,masterWin,(MultiSortMap)mMultiSortMap.clone(),false);
					if(currentEditObject != null) {
        			    int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, currentEditObject);
        			    try {
        			    	listBr.reloadOneRecV(idx);
        			    	refreshListItems(currentEditObject);
        			    } catch (Exception ex) {
        			    	refreshListItems(null);
        			    }
					}
				}
				@Override
				public ReturnMsg fetchNext(BiResult p_br) {
					// TODO Auto-generated method stub
					return null;
				}
				@Override
				public ReturnMsg fetchPrevious(BiResult p_br) {
					// TODO Auto-generated method stub
					return null;
				}
				@Override
				public String getExtraInfo() {
					// TODO Auto-generated method stub
					return null;
				}
				@Override
				public Boolean hasNextRec() {
					// TODO Auto-generated method stub
					return null;
				}
				@Override
				public Boolean hasPrevRec() {
					// TODO Auto-generated method stub
					return null;
				}
				@Override
				public HashSet<BiColumn> getVisibleColumns(BiResult p_br) {
					// TODO Auto-generated method stub
					return null;
				}
			}
					
			);
		}
	}
	   public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
	    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	    	btnHelp.setVisible(false);
	    	if(!isMobile()){
	    		batchModeToggleButton.setVisible(false);
	    	}
		}
		@Override
		protected void setupExportButton(final BiResult result)
		{
			super.setupExportButton(result);
			Button btnCopyOrder;
			String uid = result.getSelectUtil().getLoginId();
			if(uid.equals("wineac") || uid.equals("hlv")) {
				btnCopyOrder = new ZkBiButton();
				btnCopyOrder.setLabel("Update Stock Record");
				btnCopyOrder.setId("btUpdStock");
				btnCopyOrder.addEventListener("onClick",
						new EventListener() {
							@Override
							public void onEvent(Event arg0) throws Exception {
								UniLog.log("change stock");
								final java.util.Set selection = listModelList.getSelection();
								if(selection.size() != 1) {
									Messagebox.show(
											"Please Select Stock Record",
											sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
									return;
								}
        			        	currentEditObject = selection.toArray()[0];
        			        	int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, currentEditObject);
        			        	result.loadOneRecV(idx);
								doPopupStock("wc.Stock",result.getCell("pdls_irg").getInt(),result);
							}
					}
				);
				actionBar.appendChild(btnCopyOrder);
			}
				btnCopyOrder = new ZkBiButton();
				btnCopyOrder.setLabel("Download WineList");
				btnCopyOrder.setId("btWineList");
				btnCopyOrder.addEventListener("onClick",
						new EventListener() {
							@Override
							public void onEvent(Event arg0) throws Exception {
									UniLog.log("HAHA");
									result.beginWork();
									try {
									ByteArrayOutputStream bos = new ByteArrayOutputStream();
									Winelist.getFullWineList("WINECAVE", result.getSelectUtil(), bos);
									Filedownload.save(bos.toByteArray(), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "winelist.xls");
									} catch (Exception ex) {
										UniLog.log(ex);
										result.rollbackWork();
									}
									result.commitWork();
									
							}
					}
				);
				actionBar.appendChild(btnCopyOrder);
		}
	PopupControl ppc = null;
	void doPopupStock(String p_viewName ,int p_irg,BiResult p_br) throws Exception
	{
            		if(ppc == null) {
            			ppc = new PopupControl(p_viewName,p_br);
            		}
    				int irg = p_irg;
               		ppc.popupPoBr.clearCondition();
            		ppc.popupPoBr.addCustomCondition("st_irg = " + irg);
            		ppc.popupPoBr.query(true);
            		if(ppc.popupPoBr.getRowCount() > 0 ) {
            			ppc.popupJx.setUpdateAndClose(JxZkBiBase.CloseAction.Reload);
            			ppc.popupPoBr.loadOneRecV(0);
            			ppc.popupPoBr.fetchOneRecV(0);
            			ppc.popupPoBr.clearLastUpdate();
            			ppc.popupJx.setIsMobile(false);
					    ppc.popupJx.bindCellCollection(ppc.popupPoBr,JxZkBiBase.MODE_UPDATE);
//					    ppc.popupJx.jxSetVisible("btUpdate",false);
//					    ppc.popupJx.jxSetVisible("btAdd",true);
					    ppc.popupJx.showForm();	
					    ppc.popupJx.doModalUpdate();
            		} else {
            			Messagebox.show(
        					"Fatal System Error : Reason Unknown. Code 3102",
        					sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        			
            		}
            					
    				
	}	
	
	@Override
    protected void setupDataAnalysisButton(final BiResult result) {
		super.setupDataAnalysisButton(result);
		{
//			addReportButton(
//					new AnalysisReport("By Category By Month")
//					.addRow("mt_tpname")
//					.addRow("st_mbrand")
//					.addCol("ind_period")
//					.addAggregate(AGGREGATES.SUM,"palc_totqty")
//					.addAggregate(AGGREGATES.PERCENT_TOTAL,"palc_totqty")
//					.addAggregate(AGGREGATES.SUM,"ind_netltotal")
//					.addAggregate(AGGREGATES.PERCENT_TOTAL,"ind_netltotal")
//					.addAggregate(AGGREGATES.SUM,"ind_margin")
//					.addAggregate(AGGREGATES.PERCENT_TOTAL,"ind_margin")
//					.hideRow("st_mbrand", true)
//					.hideAggregate("SUM(palc_totqty)", true)
//					.hideAggregate("PERCENT_TOTAL(palc_totqty)", true)
//					.hideAggregate("PERCENT_TOTAL(ind_netltotal)", true)
//					.hideAggregate("SUM(ind_margin)", true)
//					.hideAggregate("PERCENT_TOTAL(ind_margin)", true)
//				,result);
			addReportButton(
					new AnalysisReport("By Customer By Month")
					.addRow("pdls_owner")
					.addRow("pdls_loc")
					.addRow("sttp_name")
					.addCol("or_yymm")
					.addAggregate(AGGREGATES.SUM,"pdls_stockqty")
					.hideRow("pdls_loc", true)
					.hideRow("sttp_name", true)
//					.hideCol("or_yymm", true)
				,result);
			currentRpt = rptList.get("By Customer By Month");
		}
	}
		@Override
		protected void processAnalysizedData(JSONObject p_data,BiResult p_result) throws JSONException{
			super.processAnalysizedData(p_data,p_result);
			rgChartType.setSelectedIndex(1);
			drawOneChart(p_data);
		}
}
