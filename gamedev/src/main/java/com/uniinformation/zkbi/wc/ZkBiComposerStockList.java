package com.uniinformation.zkbi.wc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.kyoko.common.ChineseConvert;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellFormula;
import com.uniinformation.cell.CellValidation;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.poi.ExcelPoi;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.WordPressHelper;
import com.uniinformation.zkbi.JxZkBiBaseCallback;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.ZkBiTranslateHelper;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;

public class ZkBiComposerStockList extends ZkBiComposerReport{
	Cell costCell = null;
	Object currentEditObject = null;
	CellValueAction updateConsgnCost = new CellValueAction() {

		@Override
		public void cellAction_onchange(Cell p_value) throws CellException {
			// TODO Auto-generated method stub
			UniLog.log("Price changed , calculate cost");
			RpcClient rpc = getSessionHelper().getRpcClient();
			Value v= rpc.callSegment(
						"WineCaveConnection",
						new VectorUtil()
							.addElement("winecave_getnetunitprice")
							.addElement("")
							.addElement(p_value.getDouble())
							.toVector()
					);
			rpc.close();
			if(v != null && v.toString().startsWith("OK")) {
				if(costCell != null) {

					double cost = Double.parseDouble(v.toString().substring(4).trim());
					costCell.set(cost);
				}
			}
		}

		@Override
		public void cellAction_onfree() throws CellException {
			// TODO Auto-generated method stub
			
		}
		
	};
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
	@Override
	protected void setupExportButton(final BiResult result) {
		super.setupExportButton(result);
		if(sessionHelper.hasAccessRight("consign")) {
		//append button after delete button
		if (sessionHelper.getWPLinkStock() && sessionHelper.hasAccessRight("#syncweb")) {
			Button btSync = new ZkBiButton();
			btSync.setLabel(sessionHelper.getBtLabel("Sync Data"));
			btSync.setId("btWPSync");
   			btSync.setIconSclass("z-icon-refresh");
			abHelper.addButton(btSync, "fa-refresh");
			
			btSync.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
				@Override
				public void onZkBiEvent(Event event) throws Exception {
				    Messagebox.show("Are you sync data to wordpress?", "System Message", Messagebox.YES|Messagebox.NO, Messagebox.QUESTION,
				    	new ZkBiEventListener() {
							@Override
							public void onZkBiEvent(Event event) throws Exception {
			    	    	   if (((Integer)event.getData()) == Messagebox.YES){
			    	    			new ZkBiAbstractLongOp(masterWin, "Sync data in progress...") {
			    						@Override
			    						public ReturnMsg longOp() {
			    							WordPressHelper wp = new WordPressHelper(sessionHelper);
			    							return wp.triggerSync();
			    						}
			    						public void afterLongOp(ReturnMsg p_rtnMsg){
			    							super.afterLongOp(p_rtnMsg);
			    							if (p_rtnMsg.getStatus()) {
			    								ZkUtil.normMsg("Sync completed");
			    							}
			    							else {
			    								ZkUtil.warnMsg("Sync error: %s", p_rtnMsg.getMsg());
			    							}
			    						}
			    					};
			    	    	   }
							}
				    });
				}});
			
		}
		if (sessionHelper.getWPLinkStock() && sessionHelper.hasAccessRight("#consign")) {
			Button btSetConsignment = new ZkBiButton();
			btSetConsignment.setLabel(sessionHelper.getBtLabel("Consignment"));
			btSetConsignment.setId("btConsignment");
   			btSetConsignment.setIconSclass("z-icon-refresh");
			abHelper.addButton(btSetConsignment, "fa-refresh");	
			btSetConsignment.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
				@Override
				public void onZkBiEvent(Event event) throws Exception {
								UniLog.log("set consignment haha");
								final java.util.Set selection = listModelList.getSelection();
								if(selection.size() != 1) {
									Messagebox.show(
											"Please Select Stock Record",
											sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
									return;
								}
        			        	currentEditObject = selection.toArray()[0];
        			        	final int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, currentEditObject);
        			        	result.loadOneRecV(idx);
        	        			final ZkForm zkf1 = new ZkForm(null,"zkf/winecave/set_consignment.zul");
        	        			final CellCollection col = new CellCollection();
        	        			col.addCell("stlqty_iname", new Cell(result.getCellString("st_iname")));
        	        			col.addCell("stlqty_botpercase", new Cell(result.getCellInt("st_msize1")));
        	        			col.addCell("stlqty_total", new Cell(result.getCell("pdlsstor_stockqty").getInt()+result.getCell("pdlswh01_stockqty").getInt()));
        	        			col.addCell("stlqty_storage", new Cell(result.getCellInt("pdlsstor_stockqty")));

        	        			col.addCell("stlqty_consignment", new Cell(result.getCellInt("pdlswh01_stockqty")));
        	        			col.addCell("stlqty_sold", new Cell(result.getCellInt("pdlssold_stockqty")));
        	        			col.addCell("stlqty_irg", new Cell(result.getCellInt("pds_irg")));
        	        			col.addCell("stlqty_org", new Cell(result.getCellInt("pds_org")));
        	        			col.addCell("stlqty_vcode", new Cell(result.getCellString("or_cocode")));
        	        			col.addCell("stlqty_salebybtl",new Cell( result.getCell("consgp_salebybtl").getBoolean()));

        	        			col.addCell("stlqty_forsale", new Cell(0));
        	        			col.addCell("stlqty_uom", new Cell(0));
        	        			
        	        			col.addCell("stlqty_price", new Cell(result.getCell("consgp_price").getDouble()));
        	        			col.addCell("stlqty_cost", new Cell(result.getCell("consgp_cost").getDouble()));
        	        			col.addCell("stlqty_punit",new Cell(""));
        	        			col.addCell("stlqty_nunit",new Cell(""));
        	        			
        	        			col.getCell("stlqty_cost").setMode(Cell.VMODE_DISPONLY);
        	        			col.getCell("stlqty_botpercase").setMode(Cell.VMODE_DISPONLY);
        	        			col.getCell("stlqty_storage").setMode(Cell.VMODE_DISPONLY);
        	        			col.getCell("stlqty_total").setMode(Cell.VMODE_DISPONLY);
        	        			col.getCell("stlqty_price").addAction(updateConsgnCost);

        	        			col.getCell("stlqty_uom").setItemList(
        	        						new VectorUtil()
        	        						.addElement("Bot")
        	        						.addElement("Case")
        	        						.toVector()
        	        					);
        	        			col.getCell("stlqty_uom").set(
        	        						col.getCell("stlqty_salebybtl").getBoolean() ? 0 : 1
        	        					);
        	        			if(col.getCell("stlqty_uom").getInt() == 0) {
        	        				col.getCell("stlqty_forsale").set(col.getCell("stlqty_consignment").getInt());
        	        			} else {
        	        				col.getCell("stlqty_forsale").set(
        	        							col.getCell("stlqty_consignment").getInt() /
        	        							col.getCell("stlqty_botpercase").getInt() 
        	        							);
        	        			}
        	        			col.getCell("stlqty_consignment").setFormula(
        	        					new CellFormula("if(stlqty_uom == 0, stlqty_forsale , stlqty_forsale * stlqty_botpercase)",col)
        	        					);
        	        			col.getCell("stlqty_salebybtl").setFormula(
        	        					new CellFormula("if(stlqty_uom == 0, 'Y', 'N')",col)
        	        					);
        	        			col.getCell("stlqty_storage").setFormula(
        	        					new CellFormula("stlqty_total - stlqty_consignment",col)
        	        					);
        	        			col.getCell("stlqty_punit").setFormula(
        	        					new CellFormula("if(stlqty_uom == 0,'/Bot','/Case')",col)
        	        					);
        	        			col.getCell("stlqty_nunit").setFormula(
        	        					new CellFormula("if(stlqty_uom == 0,'/Bot','/Case')",col)
        	        					);
        	        			col.getCell("stlqty_storage").addAction(
        	        					new CellValueAction() {

											@Override
											public void cellAction_onchange(Cell p_value) throws CellException {
												// TODO Auto-generated method stub
												if(p_value.getInt() < 0) throw new CellException("For Sell Quantity Cannot Exceed Total Quantity");
											}

											@Override
											public void cellAction_onfree() throws CellException {
												// TODO Auto-generated method stub
												
											}
        	        						
        	        					}
        	        					);
        	        			if(col.getCell("stlqty_consignment").getInt() <= 0) {
        	        				col.getCell("stlqty_uom").set(0);
        	        				if(col.getCell("stlqty_total").getInt() < col.getCell("stlqty_botpercase").getInt()) {
        	        					col.getCell("stlqty_uom").setMode(Cell.VMODE_DISPONLY);
									}
        	        			}

        	        				/*	
        	        			col.getCell("stlqty_storage").setValidation(
        	        					new CellValidation() {

											@Override
											public boolean validate(Cell p_cell, Object p_value) {
												// TODO Auto-generated method stub
												int n=0;
												if(p_value instanceof Integer) {
													n = ((Integer) p_value).intValue();
												}
												if(p_value instanceof Double) {
													n = (int) ((Double) p_value).doubleValue();
												}
												if(n < 0) return false;
												return(true);
											}

											@Override
											public String getErrMsg() {
												// TODO Auto-generated method stub
												return null;
											}
        	        						
        	        					}
        	        							
        	        					);
        	        				*/
        	        			costCell = col.getCell("stlqty_cost");
//        	        			col.addCell("userid", new Cell("donald"));
        	        			
        	        			zkf1.doModal(col,new EventListener() {
        								@Override
        								public void onEvent(Event arg0) throws Exception {
        									// TODO Auto-generated method stub
        									UniLog.log("HAHA clicked");
        									if(arg0.getTarget().getId().equals("btOK")) {
        										UniLog.log("OK clicked");
        										RpcClient rpc = sessionHelper.getRpcClient();
        										Value v = rpc.callSegment("updateConsignmentDetail", 
        												new VectorUtil()
        													.addElement(0)
        													.addElement(col.getCellString("stlqty_vcode"))
        													.addElement(col.getCellInt("stlqty_irg"))
        													.addElement(col.getCellInt("stlqty_org"))
        													.addElement(col.getCellInt("stlqty_storage"))
        													.addElement(col.getCellInt("stlqty_consignment"))
        													.addElement(col.getCell("stlqty_salebybtl").getBoolean())
        													.addElement(col.getCellDouble("stlqty_price"))
        													.addElement(col.getCellInt("stlqty_sold"))
        													.toVector()
        												);
        										if(v == null || !v.toString().startsWith("OK")) {
        											Messagebox.show("Update Failed " + (v == null ? "" : v.toString()));
        											return;
        										} 

        										zkf1.exitModal();
        					       			    try {
        				        			    	result.reloadOneRecV(idx);
        				        			    	refreshListItems(currentEditObject);
        				        			    } catch (Exception ex) {
        				        			    	refreshListItems(null);
        				        			    }	
        									}
        									if(arg0.getTarget().getId().equals("btCancel")) {
        										zkf1.exitModal();
        									}
        								}
        	        				}
        	        			);
        			        	
						
				}});
		}
//		if("Y".equals(Erpv4Config.getString(getSessionHelper(), "#batchcon"))){
		if(getSessionHelper().hasAccessRight("#batchcon")){
			Button btSetConsignment = new ZkBiButton();
			btSetConsignment.setLabel(sessionHelper.getBtLabel("Batch Consignment Update"));
			btSetConsignment.setId("btBatchConsignment");
   			btSetConsignment.setIconSclass("z-icon-refresh");
			abHelper.addButton(btSetConsignment, "fa-refresh");	
			btSetConsignment.setUpload(String.format("true,maxsize=%d,multiple=false,accept=.xls|.xlsx,native", 100*1024));
			btSetConsignment.addEventListener(Events.ON_UPLOAD, new ZkBiEventListener<UploadEvent>() {
				@Override
				public void onZkBiEvent(UploadEvent event) throws Exception {
					UniLog.log1("event:%s", event);
					new BatchConsignment(event.getMedia(), result, event.getTarget().getRoot());
				}
		});
		}
		}
		if("Y".equals(Erpv4Config.getString(getSessionHelper(), "HasBatchImportSO"))){ 
			Button btCreateSO = new ZkBiButton();
			btCreateSO.setLabel(sessionHelper.getBtLabel("Import SO"));
			btCreateSO.setId("btImportSO");
   			btCreateSO.setIconSclass("z-icon-refresh");
			abHelper.addButton(btCreateSO, "fa-refresh");	
			btCreateSO.setUpload(String.format("true,maxsize=%d,multiple=false,accept=.xls|.xlsx,native", 100*1024));
			btCreateSO.addEventListener(Events.ON_UPLOAD, new ZkBiEventListener<UploadEvent>() {
				@Override
				public void onZkBiEvent(UploadEvent event) throws Exception {
					UniLog.log1("event:%s", event);
					new ImportSO(event.getMedia(), getSessionHelper(), event.getTarget().getRoot());
				}
			});
		}
		
		{
		Button btnCopyOrder;
		String uid = result.getSelectUtil().getLoginId();
		if(BiSchema.hasAccessRight(getSessionHelper(), "#updStkAtr")) {
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
							doPopupStock("wc.MStock",result.getCell("pds_irg").getInt(),result);
						}
				}
			);
			actionBar.appendChild(btnCopyOrder);
		}
		}
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
	
	private class BatchConsignment {
		private BiResult result;
    	private ExcelPoi exlpoi;

    	private Window progressPanel;
    	private Label progressName;
    	private Progressmeter progressMeter;
    	private Button progressCancel;

		private boolean aborted;
		private int okCount, failCount, skipCount;
		public BatchConsignment(final org.zkoss.util.media.Media media, BiResult br, final Component comp) {
			this.result = br;
			ZkBiMsgbox.show(ZkBiMsgbox.Type.question, String.format("Import file '%s' and consignment?", media.getName()), new String[] {"Ok", "Cancel"}, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
					if (btn.getName().equals("Ok")) {
						try {
							readExcelAndProcess(media, comp);
						} catch (Exception e) {
							ZkBiMsgbox.show(ZkBiMsgbox.Type.error, StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
						}
					}
				}
			});
		}
		
		private void readExcelAndProcess(org.zkoss.util.media.Media media, Component comp) throws Exception {
	    	//load excel file
	    	InputStream is = media.getStreamData();
	   		try {
	   			exlpoi = ExcelPoi.newExcelPoi(is,false);
	      	} 
		    catch (Exception ex) {
		    	is.close();
				is = media.getStreamData();
				exlpoi = ExcelPoi.newExcelPoi(is,true);
			}
			exlpoi.excel_translate_Chinese(0);
			is.close();
			UniLog.log("Excel row count = " + exlpoi.getRowCount());
	
			//get excel header
			final Map<String, BiColumn> biColMap = new HashMap<String, BiColumn>();
			final Map<String, Integer> biColColMap = new HashMap<String, Integer>();
			int maxColumn = exlpoi.excel_getColumnCount(0);
			if (maxColumn > MAX_IMPORT_COLUMNS) maxColumn = MAX_IMPORT_COLUMNS;
			for (int i = 0; i < maxColumn; i++) {
				String colhdr = exlpoi.getStringValue(0, i);
				BiColumn biCol;
				if (StringUtils.isNotBlank(colhdr) && (biCol = findBiColumnByHeader(sessionHelper, result, colhdr)) != null) {
					UniLog.log1("colhdr:%s, label:%s", colhdr, biCol.getLabel());
					biColMap.put(biCol.getLabel(), biCol);
					biColColMap.put(biCol.getLabel(), i);
				}
			}

			if (!biColMap.containsKey("pds_org"))
				throw new Exception("'Ref No.' Column not found");
			if (!biColMap.containsKey("st_icode"))
				throw new Exception("'Item Code' Column not found");
			if (!biColMap.containsKey("consgp_qty")) 
				throw new Exception("'Selling Qty' Column not found"); 
			if (!biColMap.containsKey("consgp_unit")) 
				throw new Exception("'Selling Unit' Column not found"); 
			if (!biColMap.containsKey("consgp_price"))
				throw new Exception("'Selling Price' Column not found");
						
			//add read excel row event
			Iterator<EventListener<? extends Event>> it = comp.getEventListeners("onStockListImportRow").iterator();
	    	while (it.hasNext())
	    		comp.removeEventListener("onStockListImportRow", it.next());
			org.zkoss.json.JSONObject jo = new org.zkoss.json.JSONObject();
			jo.put("excelRow", 1);
			comp.addEventListener("onStockListImportRow", new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					org.zkoss.json.JSONObject jobj = (org.zkoss.json.JSONObject)event.getData();
					final int i = (Integer)jobj.get("excelRow");
					UniLog.log1("READ_EXCEL_ROWS_EVENT excelRow i:%d", i);
					if (i >= exlpoi.getRowCount() || aborted) {
						//finally show dialog
						progressPanel.setVisible(false);
						skipCount = exlpoi.getRowCount() - 1 - okCount - failCount;
						StringBuilder sb = new StringBuilder();
						if (okCount > 0)
							sb.append("OK: "+okCount+", ");
						if (failCount > 0)
							sb.append("Fail: "+failCount+", ");
						if (skipCount > 0)
							sb.append("Skip: "+skipCount+", ");
						ZkBiMsgbox.show(String.format("Update finish. (%s)", sb.toString().substring(0, sb.length() - 2)));
						refresh(result, masterWin, (MultiSortMap)mMultiSortMap.clone(),false);
						return;
					} else {
						try {
							int org = (Integer)getPoiObjectByBiCol(biColMap.get("pds_org"), i, biColColMap.get("pds_org"), false);
							String icode = (String)getPoiObjectByBiCol(biColMap.get("st_icode"), i, biColColMap.get("st_icode"), false);
							double price = (Double)getPoiObjectByBiCol(biColMap.get("consgp_price"), i, biColColMap.get("consgp_price"), false);
							if (org <= 0)
								throw new Exception(String.format("'Ref No.' not found at row %d", i + 1));
							else if (StringUtils.isBlank(icode))
								throw new Exception(String.format("'Item Code' not found at row %d", i + 1));
							else {
								//query record
								result.clearCondition();
								result.addCondition(new VectorUtil().addElement(result.getView().getTable()).toVector(), String.format("st_icode = '%s' and pds_org = %d", icode, org));
								result.query(false);
								if (result.getRowCount() == 1) {
									result.loadOneRecV(0);
									String owner = result.getCellString("or_cocode");
									int irg = result.getCellInt("pds_irg");
									int msize1 = result.getCellInt("st_msize1");
									int totalQty = result.getCellInt("pdlsstor_stockqty") + result.getCellInt("pdlswh01_stockqty");
									int soldQty = result.getCellInt("pdlssold_stockqty");

									/*
									boolean salebybtl;
									int forSalesQty;
									if (StringUtils.isNotBlank(forSales)) {
										String[] forSalesSplit = forSales.trim().split(" ");
										if (!StringUtils.equalsAny(forSalesSplit[1], "Btl", "Case"))
											throw new Exception(String.format("For Sale Unit is not 'Bot' or 'Case' at row %d", i));
										forSalesQty = NumberUtils.toInt(forSalesSplit[0]);
										salebybtl = !StringUtils.equals(forSalesSplit[1], "Case");
									} else {
										forSalesQty = 0;
										salebybtl = true;
									}
									*/
									
									boolean skip = false;
									String sellUnit = (String)getPoiObjectByBiCol(biColMap.get("consgp_unit"), i, biColColMap.get("consgp_unit"), false);
									boolean salebybtl = !"Case".equals(sellUnit);
									double dd = (Double) getPoiObjectByBiCol(biColMap.get("consgp_qty"), i, biColColMap.get("consgp_qty"), false);
									if(!salebybtl) dd *= msize1;
									if(price == 0 && result.getCellDouble("consgp_price") == 0) {
										skipCount++;
										skip = true;
									} else if(
											result.getCellDouble("pdlswh01_stockqty") == dd 
											&& result.getCellString("consgp_unit").equals(sellUnit)
											&& result.getCellDouble("consgp_price") == price
											) {
										skipCount++;
										skip = true;
									}
									if(!skip) {
									int consignment = (int) dd;
									int storage = totalQty - consignment;
									if (storage < 0)
										throw new Exception(String.format("For Sell Quantity Cannot Exceed Total Quantity at row %d", i));
									UniLog.log1("icode:%s, irg:%d, org:%d, msize1:%d, storage:%d, consignment:%d, salebybtl:%b, price:%f, soldQty:%d", icode, irg, org, msize1, storage, consignment, salebybtl, price, soldQty);
									
									//rpccall
    								RpcClient rpc = sessionHelper.getRpcClient();
        							Value v = rpc.callSegment("updateConsignmentDetail", 
     											new VectorUtil()
        											.addElement(0)
        											.addElement(owner)
        											.addElement(irg)
        											.addElement(org)
        											.addElement(storage)
        											.addElement(consignment)
        											.addElement(salebybtl)
        											.addElement(price)
        											.addElement(soldQty)
        											.toVector()
        										);
        							if (v == null || !v.toString().startsWith("OK"))
        								throw new Exception("Update Failed " + (v == null ? "" : v.toString()) + " at row " + (i + 1));
									
									okCount++;
									}
								} else if (result.getRowCount() > 1) 
									throw new Exception(String.format("Row %d primary key not unique", i + 1));
								else
									skipCount++;
							}
						}
						catch (Exception e) {
							UniLog.log(e);
							failCount++;
						}
						progressMeter.setValue(i * 100 / (exlpoi.getRowCount() - 1));
						//read next excel row
						Events.echoEvent("onStockListImportRow", event.getTarget(), new org.zkoss.json.JSONObject() {{
							put("excelRow", i + 1);
						}});
					}
				}
			});
			Events.echoEvent("onStockListImportRow", comp, jo);
			
			//build progress panel
			progressPanel = (Window)masterWin.getFellowIfAny("wcStockListProgPanel");
			if (progressPanel == null) {
				progressPanel = new Window();
				progressPanel.setId("wcStockListProgPanel");
				progressName = new Label();
				progressName.setSclass("progressName");
				progressMeter = new Progressmeter();
				progressMeter.setSclass("progressMeter");
				progressCancel = new ZkBiButton();
				progressCancel.setSclass("progressCancel");

	    		progressName.setValue("Progress:");
	    		progressMeter.setWidth("200px");
	    		progressMeter.setValue(50);
	    		progressCancel.setLabel("Cancel");
	    		progressPanel.appendChild(progressName);
	    		progressPanel.appendChild(progressMeter);
	    		progressPanel.appendChild(progressCancel);
	    		progressPanel.setVisible(false);
	    		masterWin.appendChild(progressPanel);
			} else {
				progressName = (Label)progressPanel.query(".progressName");
				progressMeter = (Progressmeter)progressPanel.query(".progressMeter");
				progressCancel = (ZkBiButton)progressPanel.query(".progressCancel");
			}
			it = progressCancel.getEventListeners(Events.ON_CLICK).iterator();
	    	while (it.hasNext())
	    		progressCancel.removeEventListener(Events.ON_CLICK, it.next());
    		progressCancel.addEventListener(Events.ON_CLICK, 
				new ZkBiEventListener(){
					public void onZkBiEvent(Event event) throws Exception {
						UniLog.log("Progress Panel Closed");
						aborted = true;
						progressPanel.setVisible(false);
					}
				}
			);
	    	
    		progressName.setValue("Processing ...");
    		progressMeter.setValue(0);
    		progressMeter.invalidate();
    		progressPanel.doModal();
		}
		
	    private BiColumn findBiColumnByHeader(SessionHelper sh, BiResult result,String colHdr) {
			List<BiColumn> xv = result.getExportColumns();		
			for(int i=0;i<xv.size();i++) {
				BiColumn bl = (BiColumn) xv.get(i);
				if(bl.isInList(sh) && !bl.isSkipImport() &&
						(bl.getEngName().equals(colHdr) ||
						colHdr.equals( ZkBiTranslateHelper.getText(sessionHelper, bl.getCellFullName(), "LABEL", sessionHelper.getLabel(bl)))
						)) {
					return(bl);
				}
			}
			return(null);
	    }

	    private Object getPoiObjectByBiCol(BiColumn bc,int row,int col,boolean autoTranslate) throws Exception {
	    	Object xo = null;
			if(bc != null && bc.getColumnType().equals("date")) {
				xo = exlpoi.getDateValue(row, col);
				if(xo == null && exlpoi.getStringValue(row,col) != null) {
					throw new Exception ("Input Error, Date Column "+ bc.getLabel() + " contains non-date value");
				}
			} 
			else if(bc != null && bc.getColumnType().equals("serial")) {
				xo = exlpoi.getIntegerValue(row, col);
			} 
			else if(bc != null && bc.getColumnType().equals("integer")) {
				xo = exlpoi.getIntegerValue(row, col);
			} 
			else if(bc != null && bc.getColumnType().equals("float")) {
				xo = exlpoi.getDoubleValue(row, col);
			} 
			else if(bc != null && bc.getColumnType().equals("money")) {
				xo = exlpoi.getDoubleValue(row, col);
			} 
			else {
	    		if(autoTranslate)  {
					if( sessionHelper.getLHLang().equals("SCHN")) {
						xo = ChineseConvert.convertAuto2Gnew(stripNonPrintable(exlpoi.getStringValue(row, col)));
					} else {
						xo = ChineseConvert.convertAuto2Bnew(stripNonPrintable(exlpoi.getStringValue(row, col)));
					}
	    		}  else
	    			xo = stripNonPrintable(exlpoi.getStringValue(row, col));
			}
			return(xo);
	    }

		private String stripNonPrintable(String p_s) {
			StringBuffer sb = new StringBuffer();
			if(p_s == null) return(null);
			char[] ca = p_s.toCharArray();
			for (int i=0; i<ca.length; i++) {
				if(ca[i] < 32) continue;
				if(Character.isWhitespace(ca[i])) sb.append(' '); else sb.append(ca[i]);
			}
			return(StringUtils.stripEnd(sb.toString()," "));
		}
	}
	private class ImportSO{
		private BiResult result;
		private BiResult sr;
    	private ExcelPoi exlpoi;

    	private Window progressPanel;
    	private Label progressName;
    	private Progressmeter progressMeter;
    	private Button progressCancel;

		private boolean aborted;
		private int lastMrg=0;
		final Map<String, Integer> biColMap = new HashMap<String, Integer>();
		final Map<String, Integer> biColMapSr = new HashMap<String, Integer>();
		public ImportSO(final org.zkoss.util.media.Media media, SessionHelper sh, final Component comp) {
			BiView bv = sessionHelper.getBiSchema().getViewByName("wc.StockOut");
			this.result = bv.newBiResult(sh.getLoginId(), null, null, sh);
			this.sr = result.getSubLink("wc.StmdMoSi");
			ZkBiMsgbox.show(ZkBiMsgbox.Type.question, String.format("Import file '%s' as Sales Order ?", media.getName()), new String[] {"Ok", "Cancel"}, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
					if (btn.getName().equals("Ok")) {
						try {
							readExcelAndProcess(media, comp);
						} catch (Exception e) {
							ZkBiMsgbox.show(ZkBiMsgbox.Type.error, StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
						}
					}
				}
			});
		}
		
		private void checkColumnExist(BiColumn bc) throws Exception {
			if(biColMap.containsKey(bc.getLabel())) return;
			if(biColMapSr.containsKey(bc.getLabel())) return;
			throw new Exception("Column " + bc.getEngName() + " Not Found");
		}
		private void readExcelAndProcess(org.zkoss.util.media.Media media, Component comp) throws Exception {
	    	//load excel file
	    	InputStream is = media.getStreamData();
	   		try {
	   			exlpoi = ExcelPoi.newExcelPoi(is,false);
	      	} 
		    catch (Exception ex) {
		    	is.close();
				is = media.getStreamData();
				exlpoi = ExcelPoi.newExcelPoi(is,true);
			}
			exlpoi.excel_translate_Chinese(0);
			is.close();
			UniLog.log("Excel row count = " + exlpoi.getRowCount());
			//get excel header
//			final Map<String, BiColumn> biColMap = new HashMap<String, BiColumn>();
			int maxColumn = exlpoi.excel_getColumnCount(0);
			if (maxColumn > MAX_IMPORT_COLUMNS) maxColumn = MAX_IMPORT_COLUMNS;
			for (int i = 0; i < maxColumn; i++) {
				String colhdr = exlpoi.getStringValue(0, i);
				BiColumn biCol;
				if (StringUtils.isNotBlank(colhdr) ) {
					if ((biCol = findBiColumnByHeader(sessionHelper, result, colhdr)) != null) {
						UniLog.log1("colhdr:%s, label:%s", colhdr, biCol.getLabel());
						biColMap.put(biCol.getLabel(), i);
					} else {
						if ((biCol = findBiColumnByHeader(sessionHelper, sr, colhdr)) != null) {
							biColMapSr.put(biCol.getLabel(), i);
						}
					}
				}
			}
			checkColumnExist(result.getColumnByLabel("stm_date"));
			checkColumnExist(result.getColumnByLabel("stm_ref2"));
//			if (!biColMap.containsKey("stm_date"))
//				throw new Exception("'Date' Column not found");
//			if (!biColMap.containsKey("stm_ref1"))
//				throw new Exception("'Ref1' Column not found");
//			if (!biColMapSr.containsKey("st_icode"))
//				throw new Exception("'Item Code' Column not found");
			
			//add read excel row event
			
			Iterator<EventListener<? extends Event>> it = comp.getEventListeners("onStockOutImportRow").iterator();
	    	while (it.hasNext())
	    		comp.removeEventListener("onStockOutImportRow", it.next());
			org.zkoss.json.JSONObject jo = new org.zkoss.json.JSONObject();
			jo.put("excelRow", 1);
			comp.addEventListener("onStockOutImportRow", new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					org.zkoss.json.JSONObject jobj = (org.zkoss.json.JSONObject)event.getData();
					final int i = (Integer)jobj.get("excelRow");
					UniLog.log1("READ_EXCEL_ROWS_EVENT excelRow i:%d", i);
					RpcClient rpc= getSessionHelper().getRpcClient();
					try {
					if (i >= exlpoi.getRowCount() || aborted) {
						//finally show dialog
						progressPanel.setVisible(false);
						if(lastMrg > 0) {
								ReturnMsg rtn = result.addCurrent();
								if(rtn != null && !rtn.getStatus()) {
									ZkBiMsgbox.show(rtn.getMsg());
									refresh(result, masterWin, (MultiSortMap)mMultiSortMap.clone(),false);
									return;
								}
						}
						ZkBiMsgbox.show(String.format("Import finish. (%s)", "ABC"));
						refresh(result, masterWin, (MultiSortMap)mMultiSortMap.clone(),false);
						return;
					} else {
//							int org = (Integer)getPoiObjectByBiCol(biColMap.get("pds_org"), i, biColColMap.get("pds_org"), false);
							String vcode = (String) getPoiObjectByBiCol(result.getColumnByLabel("stm_ref2")
									, i, biColMap.get("stm_ref2"), false);
							Date stmDate = (Date) getPoiObjectByBiCol(result.getColumnByLabel("stm_date")
									, i, biColMap.get("stm_date"), false);
							if(!StringUtils.isBlank(vcode)) {
								if(lastMrg > 0) {
									ReturnMsg rtn = result.addCurrent();
									if(rtn != null && !rtn.getStatus()) {
										ZkBiMsgbox.show(rtn.getMsg());
										refresh(result, masterWin, (MultiSortMap)mMultiSortMap.clone(),false);
										return;
									}
								}
								lastMrg = 12345;
								result.clearCurrentRec();
								result.getCell("stm_ref2").set(vcode);
								result.getCell("stm_date").set(stmDate);
							}
							
							BiCellCollection col = sr.newRowCollection();
							ReturnMsg rtn = sr.addSubRecord(col, -1 ,"");
							String icode = (String) getPoiObjectByBiCol(sr.getColumnByLabel("st_icode")
									, i, biColMapSr.get("st_icode"), false);
							String bin = (String) getPoiObjectByBiCol(sr.getColumnByLabel("stmd_bin")
									, i, biColMapSr.get("stmd_bin"), false);
							double pqty = (Double) getPoiObjectByBiCol(sr.getColumnByLabel("stmd_pqty")
									, i, biColMapSr.get("stmd_pqty"), false);
							double pexprice = (Double) getPoiObjectByBiCol(sr.getColumnByLabel("stmd_pexprice1")
									, i, biColMapSr.get("stmd_pexprice1"), false);
							int org = (Integer) getPoiObjectByBiCol(sr.getColumnByLabel("stmd_org")
									, i, biColMapSr.get("stmd_org"), false);
							col.getCell("st_icode").set(icode);
							col.getCell("stmd_org").set(org);
							col.getCell("stmd_loc").set("WH01");
							col.getCell("stmd_bin").set(bin);
							col.getCell("stmd_exprice").set(-pexprice);
							col.getCell("stmd_exprice1").set(-pexprice);
							col.getCell("stmd_qty").set(-pqty);
							col.getCell("stmd_entryqty").set(-pqty);
							col.getCell("stmd_entryunit").set("Bot");
							col.getCell("stmd_cur").set("HKD");
							
							if(pqty != 0) {
								col.getCell("stmd_uprice").set(pexprice/pqty);
							}
//							Object tr = rtn.getData();
//							int rowIdx = getGipi(sr.getView().getName()).getIndexOf(tr);
//							sv.addItemToList(tr, rowIdx);
							Vector args = new Vector();
							args.add(col.getCellString("or_ocode"));
							args.add(col.getCellString("stm_ref2"));
							args.add(col.getCellInt("or_stmmrg"));
							args.add(col.getCellString("or_vcode"));
							args.add(col.getCell("stm_date").getDate());
							Value v = rpc.callSegment( "erpv3_check_or_create_porecord_bypocode", args);
							int cc = v.toInt();
							if(cc <= 0) {
								ZkBiMsgbox.show("Fail to get org");
								return;
							}
							col.getCell("stmdsi_org").set(cc);
							col = null;
						}
						
						progressMeter.setValue(i * 100 / (exlpoi.getRowCount() - 1));
						//read next excel row
						Events.echoEvent("onStockOutImportRow", event.getTarget(), new org.zkoss.json.JSONObject() {{
							put("excelRow", i + 1);
						}});
					} catch (Exception e) {
						UniLog.log(e);
					} finally {
						rpc.close();
					}
				}
			});
			Events.echoEvent("onStockOutImportRow", comp, jo);
			
			//build progress panel
			progressPanel = (Window)masterWin.getFellowIfAny("wcStockListProgPanel");
			if (progressPanel == null) {
				progressPanel = new Window();
				progressPanel.setId("wcStockListProgPanel");
				progressName = new Label();
				progressName.setSclass("progressName");
				progressMeter = new Progressmeter();
				progressMeter.setSclass("progressMeter");
				progressCancel = new ZkBiButton();
				progressCancel.setSclass("progressCancel");

	    		progressName.setValue("Progress:");
	    		progressMeter.setWidth("200px");
	    		progressMeter.setValue(50);
	    		progressCancel.setLabel("Cancel");
	    		progressPanel.appendChild(progressName);
	    		progressPanel.appendChild(progressMeter);
	    		progressPanel.appendChild(progressCancel);
	    		progressPanel.setVisible(false);
	    		masterWin.appendChild(progressPanel);
			} else {
				progressName = (Label)progressPanel.query(".progressName");
				progressMeter = (Progressmeter)progressPanel.query(".progressMeter");
				progressCancel = (ZkBiButton)progressPanel.query(".progressCancel");
			}
			it = progressCancel.getEventListeners(Events.ON_CLICK).iterator();
	    	while (it.hasNext())
	    		progressCancel.removeEventListener(Events.ON_CLICK, it.next());
    		progressCancel.addEventListener(Events.ON_CLICK, 
				new ZkBiEventListener(){
					public void onZkBiEvent(Event event) throws Exception {
						UniLog.log("Progress Panel Closed");
						aborted = true;
						progressPanel.setVisible(false);
					}
				}
			);
	    	
    		progressName.setValue("Processing ...");
    		progressMeter.setValue(0);
    		progressMeter.invalidate();
    		progressPanel.doModal();
		}
		
	    private BiColumn findBiColumnByHeader(SessionHelper sh, BiResult result,String colHdr) {
			List<BiColumn> xv = result.getExportColumns();		
			for(int i=0;i<xv.size();i++) {
				BiColumn bl = (BiColumn) xv.get(i);
				if(bl.isInList(sh) && !bl.isSkipImport() &&
						(bl.getEngName().equals(colHdr) ||
						colHdr.equals( ZkBiTranslateHelper.getText(sessionHelper, bl.getCellFullName(), "LABEL", sessionHelper.getLabel(bl)))
						)) {
					return(bl);
				}
			}
			return(null);
	    }

	    private Object getPoiObjectByBiCol(BiColumn bc,int row,int col,boolean autoTranslate) throws Exception {
	    	Object xo = null;
			if(bc != null && bc.getColumnType().equals("date")) {
				xo = exlpoi.getDateValue(row, col);
				if(xo == null && exlpoi.getStringValue(row,col) != null) {
					throw new Exception ("Input Error, Date Column "+ bc.getLabel() + " contains non-date value");
				}
			} 
			else if(bc != null && bc.getColumnType().equals("serial")) {
				xo = exlpoi.getIntegerValue(row, col);
			} 
			else if(bc != null && bc.getColumnType().equals("integer")) {
				xo = exlpoi.getIntegerValue(row, col);
			} 
			else if(bc != null && bc.getColumnType().equals("float")) {
				xo = exlpoi.getDoubleValue(row, col);
			} 
			else if(bc != null && bc.getColumnType().equals("money")) {
				xo = exlpoi.getDoubleValue(row, col);
			} 
			else {
	    		if(autoTranslate)  {
					if( sessionHelper.getLHLang().equals("SCHN")) {
						xo = ChineseConvert.convertAuto2Gnew(stripNonPrintable(exlpoi.getStringValue(row, col)));
					} else {
						xo = ChineseConvert.convertAuto2Bnew(stripNonPrintable(exlpoi.getStringValue(row, col)));
					}
	    		}  else
	    			xo = stripNonPrintable(exlpoi.getStringValue(row, col));
			}
			return(xo);
	    }

		private String stripNonPrintable(String p_s) {
			StringBuffer sb = new StringBuffer();
			if(p_s == null) return(null);
			char[] ca = p_s.toCharArray();
			for (int i=0; i<ca.length; i++) {
				if(ca[i] < 32) continue;
				if(Character.isWhitespace(ca[i])) sb.append(' '); else sb.append(ca[i]);
			}
			return(StringUtils.stripEnd(sb.toString()," "));
		}
	}
	
}
