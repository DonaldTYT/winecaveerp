package com.uniinformation.zkf.vincero;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Vlayout;

import com.google.gson.JsonObject;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.utils.ChartUtils;
import com.uniinformation.bicore.BiCellCollectionToJsonInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.JsonToBiCellCollectionInterface;
import com.uniinformation.bicore.vincero.BiResultCompoundResult;
import com.uniinformation.bicore.vincero.BiResultTradeJournal;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.TraverseInterface;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkf.ZkCellActionWorkSheet;
import com.uniinformation.zkf.ZkfAction;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

public class ZkCellActionTradeJournal extends ZkCellActionWorkSheet  {
	protected Vector<String> months = new VectorUtil()
					.addElement("tb_Jan")
					.addElement("tb_Feb")
					.addElement("tb_Mar")
					.addElement("tb_Apr")
					.addElement("tb_May")
					.addElement("tb_Jun")
					.addElement("tb_Jul")
					.addElement("tb_Aug")
					.addElement("tb_Sep")
					.addElement("tb_Oct")
					.addElement("tb_Nov")
					.addElement("tb_Dec")
					.toVector();
	
	
	
    @Wire
    private Div div_chart;

    @Wire
    private Listbox lb_statistic;

    @Wire
    private Listbox lb_breakdown;

    @Wire
    private Listbox lb_moneymgmt;

    @Wire
    private Listbox lb_tradedetail;
    
    @Wire
    private Div chartContainer;

    @Wire
    private Button plotButton;
    
    @Wire
    private Button clearButton;

    @Wire
    private Listitem liNewTrade;
    
    @Wire
    private Listitem li_starting;
    
    @Wire
    private Listitem li_initial;

    @Wire
    private Button btNewTrade;
    

    @Wire
    private Button btnSaveAs;
    
    @Wire
    private Button btnLoadFrom;
    
    int currDisplayMonth = -1;

	void displayMonthJournal(int midx) throws Exception {
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
    	SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	DecimalFormat df = new DecimalFormat("$#,##0.00");
    	DecimalFormat df2 = new DecimalFormat("#0.0000");

		div_chart.setVisible(false);
		lb_breakdown.setVisible(false);
		lb_moneymgmt.setVisible(false);
		lb_tradedetail.setVisible(true);
		currDisplayMonth = midx;

		liNewTrade.setParent(null);
		lb_tradedetail.getItems().clear();
		baseBr.invalidateLoadRecIdx();
		for(int i=0;i<baseBr.getRowCount();i++) {
			baseBr.loadOneRecV(i);
			if(baseBr.getCellInt("tradj_nmonth") != currDisplayMonth) continue;
			Listitem li = new Listitem();
			Listcell lc;

			Hlayout hl;
			Vlayout vl;
			Div dv;
			lc = new Listcell(); lc.setStyle("vertical-align:top;");
			lc.appendChild(new Label( sdf.format( baseBr.getCell("tradj_date").getDate())));
			li.appendChild(lc);
			
			lc = new Listcell(); lc.setStyle("vertical-align:top;");
			vl = new Vlayout();
			vl.appendChild(new Label( baseBr.getCellString("tradj_ccy")));
//			vl.appendChild(new Label( "Stop Loss"));
//			vl.appendChild(new Label( "Take Profit"));
			lc.appendChild(vl);
			li.appendChild(lc);

			lc = new Listcell();lc.setStyle("vertical-align:top;");
			vl = new Vlayout();

			hl = new Hlayout(); hl.setParent(vl);
			dv = new Div();dv.setWidth("80px");dv.appendChild(new Label("Buy/Sell"));hl.appendChild(dv);
			hl.appendChild(new Label( baseBr.getCellString("tradj_buysell")));

			hl = new Hlayout(); hl.setParent(vl);
			dv = new Div();dv.setWidth("80px");dv.appendChild(new Label("Entry"));hl.appendChild(dv);
			hl.appendChild(new Label( df2.format(baseBr.getCell("tradj_entry").getDouble())));
			
			hl = new Hlayout(); hl.setParent(vl);
			dv = new Div();dv.setWidth("80px");dv.appendChild(new Label("Exit"));hl.appendChild(dv);
			hl.appendChild(new Label( df2.format(baseBr.getCell("tradj_exit").getDouble())));
			hl = new Hlayout(); hl.setParent(vl);
			dv = new Div();dv.setWidth("80px");dv.appendChild(new Label("Stop Loss"));hl.appendChild(dv);
			hl.appendChild(new Label( df2.format(baseBr.getCell("tradj_stoplost").getDouble())));
			hl = new Hlayout(); hl.setParent(vl);
			dv = new Div();dv.setWidth("80px");dv.appendChild(new Label("Take Profit"));hl.appendChild(dv);
			hl.appendChild(new Label( df2.format(baseBr.getCell("tradj_takeprofit").getDouble())));
			hl = new Hlayout(); hl.setParent(vl);
			dv = new Div();dv.setWidth("80px");dv.appendChild(new Label("Take Profit 2"));hl.appendChild(dv);
			hl.appendChild(new Label( df2.format(baseBr.getCell("tradj_takeprofit2").getDouble())));
		
			lc.appendChild(vl);
			li.appendChild(lc);
			
			lc = new Listcell();lc.setStyle("vertical-align:top;");
			lc.appendChild(new Label( df.format(baseBr.getCell("tradj_pl").getDouble())));
			li.appendChild(lc);

			lc = new Listcell();lc.setStyle("vertical-align:top;");
			lc.appendChild(new Label( df.format(baseBr.getCell("tradj_rewardtorisk").getDouble())));
			li.appendChild(lc);

			lc = new Listcell();lc.setStyle("vertical-align:top;");
			lc.appendChild(new Label( baseBr.getCellString("tradj_timeframe")));
			li.appendChild(lc);

			lc = new Listcell();lc.setStyle("vertical-align:top;");
			lc.appendChild(new Label( sdf2.format( baseBr.getCell("tradj_timetaken").getDate())));
			li.appendChild(lc);

			lc = new Listcell();lc.setStyle("vertical-align:top;");
			lc.appendChild(new Label( baseBr.getCellString("tradj_reason")));
			li.appendChild(lc);

			lc = new Listcell();lc.setStyle("vertical-align:top;");
			lc.appendChild(new Label( baseBr.getCellString("tradj_comments")));
			li.appendChild(lc);


			
			/*
			li.appendChild(new Listcell(getCellString("cprtn_trade")));
			li.appendChild(new Listcell(getCellString("cprtn_result")));
			li.appendChild(new Listcell(getCellString("cprtn_risk")));
			li.appendChild(new Listcell(getCellString("cprtn_pl")));
			li.appendChild(new Listcell(getCellString("cprtn_balance")));
			*/
			lc = new Listcell();lc.setStyle("vertical-align:top;");
			Button delBt = (new Button("Delete"));
			vl = new Vlayout();
			final Integer delRecNum = i;
			delBt.addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
            		UniLog.log("delete record " + delRecNum);
            		baseBr.loadOneRecV(delRecNum);
            		baseBr.fetchOneRecV(delRecNum);
            		baseBr.deleteCurrent();
					baseBr.query();
            		baseBr.getCell("tradj_jsonpl").resetValue();
					baseBr.recal();
					displayMonthJournal(currDisplayMonth);
            }
			});
			vl.appendChild(delBt);
			Button editBt = (new Button("Edit"));
			final Integer editRecNum = i;
			editBt.addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
            		Listitem li = null;
            		Component c = event.getTarget();
            		while(c.getParent() != null) {
            			if(c.getParent() instanceof Listitem) {
            				li = (Listitem) c.getParent();
            				break;
            			}
            			c = c.getParent();
            		}
            		if(li != null) {
            		UniLog.log("edit record " + editRecNum);
					liNewTrade.setAttribute("inputRecV",new Integer(editRecNum));
					baseBr.loadOneRecV(editRecNum);
					copyCellsByPrefix(formCollection,"tradj_","trnew_");
//            		baseBr.fetchOneRecV(editRecNum);
					btNewTrade.setVisible(false);
//					lb_tradedetail.appendChild(liNewTrade);
					lb_tradedetail.insertBefore(liNewTrade, li);
					li.setParent(null);
					
            		}
            };
			});
			vl.appendChild(editBt);
			lc.appendChild(vl);
			li.appendChild(lc);
			
			
			lb_tradedetail.appendChild(li);
		}
		{
			Listitem li = new Listitem();
//			Div dv = new Div();
//			dv.setHeight("30px");
//			li.appendChild(dv);
			li.setHeight("10px");
			lb_tradedetail.appendChild(li);
		}
//		lb_tradedetail.appendChild(liNewTrade);
//		baseBr.clearCurrentRec();
		btNewTrade.setVisible(true);
		if(midx >= 0) {
			baseBr.getCell("tradj_page").set(months.get(midx-1).substring(3));
		} else {
				int cc;
				cc = 0;
		}
	}
    protected String getFilingKey() {
    	return(((BiResultTradeJournal) baseBr).getFilingKey());
//		return(String.format("VAWS_%s_%s_%s", "TradeJournal", getSessionHelper().getLoginId(), getSessionHelper().getVcode()));
	}
    @Override
    public void doAfterCompose(Component comp) throws Exception {
		onClickListener = new EventListener() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				Component c = (Component)arg0.getTarget();
				UniLog.log(c.getId() + "clicked");
				int midx = months.indexOf(c.getId());
				if(midx >= 0) {
					li_starting.setVisible(true);
					li_initial.setVisible(false);
					displayMonthJournal(midx+1);
				} else if(c.getId().equals("btAbort")) {
					displayMonthJournal(currDisplayMonth);
				} else if(c.getId().equals("btNewTrade")) {
					UniLog.log("new trade");
					btNewTrade.setVisible(false);
					resetCellsByPrefix(formCollection,"trnew_");
					liNewTrade.removeAttribute("inputRecV");
					lb_tradedetail.appendChild(liNewTrade);
				} else if(c.getId().equals("tb_total")) {
					div_chart.setVisible(true);
					lb_breakdown.setVisible(true);
					lb_moneymgmt.setVisible(true);
					lb_tradedetail.setVisible(false);
					li_starting.setVisible(false);
					li_initial.setVisible(true);
					currDisplayMonth = 0;
					baseBr.getCell("tradj_page").set("Total");
				} else if(c.getId().equals("btAddRecord")) {
					Date dd = baseBr.getCellDate("trnew_date");
					int monthOfDate = DateUtil.getMonth(dd);
					if(monthOfDate != currDisplayMonth) {
						ZkUtil.showErrMsg("Trade Date %s Not Within Current Displayed Month", DateUtil.dateToDateTimeStr(baseBr.getCellDate("trnew_date"),"yyyy/MM/dd"));
						return;
					}
					Integer inputRecNum = (Integer) liNewTrade.getAttribute("inputRecV");
					if(inputRecNum == null) {
						copyCellsByPrefix(formCollection,"trnew_","tradj_");
						baseBr.addCurrent();
						baseBr.query();
						baseBr.recal();
						displayMonthJournal(currDisplayMonth);
						ZkUtil.showMsg("New Trade On %s", DateUtil.dateToDateTimeStr(baseBr.getCellDate("tradj_date"),"yyyy/MM/dd"));
					} else {
						baseBr.loadOneRecV(inputRecNum);
						baseBr.fetchOneRecV(inputRecNum);
						copyCellsByPrefix(formCollection,"trnew_","tradj_");
						baseBr.updateCurrent();
						baseBr.query();
						baseBr.recal();
						displayMonthJournal(currDisplayMonth);
					}
					/*
				} else if(c.getId().equals("tb_jan")) {
					div_chart.setVisible(false);
					lb_breakdown.setVisible(false);
					lb_moneymgmt.setVisible(false);
					lb_tradedetail.setVisible(true);
					displayMonthJournal("Jan");
					*/
				}
			}
		};	
		
    	
    	
		baseBrName = "vincero.TradeJournal";
        super.doAfterCompose(comp);
        Clients.evalJavaScript(String.format("setEditlistboxStyle(\"%s\")",lb_tradedetail.getUuid()));

        // Debugging: Check if components are wired correctly
        if (chartContainer == null) {
            System.out.println("chartContainer is NULL! Check @Wire annotation.");
        }
        if (plotButton == null) {
            System.out.println("plotButton is NULL! Ensure the button has id='plotButton' in ZUL.");
        }
        if (clearButton == null) {
            System.out.println("clearButton is NULL! Ensure the button has id='clearButton' in ZUL.");
        }

        // Add event listener to plot chart button
        plotButton.addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                System.out.println("PlotChart() called!"); // Debugging log
                if (chartContainer != null) {
                	double dmin = Double.MIN_VALUE;
                	double dmax = Double.MAX_VALUE;
                	ArrayList<Double> dval = new ArrayList<Double>();
                	ArrayList<String> mons = new ArrayList<String>();

                	for(int i=1;i<=12;i++) {
                		double v = baseBr.getCellDouble("tradj_gain_"+i);
                		if(v < dmin) dmin = v;
                		if(v > dmax) dmax = v;
                		dval.add(v);
                		mons.add(months.get(i-1).substring(3));
                	}
                    ChartUtils.plotChart(chartContainer, "bar",dmax, dmin, dval,"Monthly Gain",mons);
                    Clients.evalJavaScript(String.format("setPlotChartStyle(\"%s\")",chartContainer.getUuid()));
                	/*
                	for(int i=1;i<=12;i++) {
                		double v = baseBr.getCellDouble("tradj_pl_"+i);
                		if(v < dmin) dmin = v;
                		if(v > dmax) dmax = v;
                		dval.add(v);
                		mons.add(months.get(i-1).substring(3));
                	}
//                    ChartUtils.plotBarChart(chartContainer, 100, 0, Arrays.asList(50.0, 50.0, 30.0, 80.0, 60.0, 30.0 , 33.0, 45.0 , 20.2, 20.0, 88.2,50.2));
                    ChartUtils.plotChart(chartContainer, "bar",dmax, dmin, dval,"P/L By Month",mons);
//                    ChartUtils.plotChart(chartContainer, "pie",dmax, dmin, dval,"P/L By Month",mons);
                	 */
                }
            }
        });

        // Add event listener to clear chart button
        clearButton.addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                System.out.println("ClearChart() called!"); // Debugging log
                if (chartContainer != null) {
                    ChartUtils.clearChart(chartContainer);
                }
            }
        });
        if (btnSaveAs != null) {
        btnSaveAs.addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                System.out.println("Save As called!"); // Debugging log
                JSONArray ja = new JSONArray();

                /*
                JSONObject jo = BiCellCollectionToJsonInterface.BiCellCollectionToJSON(baseBr.getCurrentCollection()); 
                String key = getFilingKey()+"_001";
				FilingUtil.storeJson(getSessionHelper().getAgent(), null , key, null, null,jo);
				key = null;
				*/
				ArrayList<String> keys = FilingUtil.getKeys(getSessionHelper().getAgent(), getFilingKey() + "_%",null);
				if(keys != null) keys = ListUtil.stripStringArrayList(keys, getFilingKey()+"_");
				ZkUtil.comboboxDialogZkForm(
							new JSONObject()
								.put("lbPrompt","Save to ? (Required)")
								.put("btOK","Save")
								.put("btCancel","Cancel")
//							, new VectorUtil().addElement("001").addElement("002").toVector(),
							, keys,
							new ZkfAction() {

								@Override
								public ReturnMsg processAction(String p_id, SessionHelper p_sh,
										CellCollection p_col, JsonObject p_actionData, InputStream p_upload,
										Component p_target) throws Exception {
									if(p_id.equals("btCancel")) {
										return(ReturnMsg.defaultOk);
									}
									if(p_id.equals("btOK")) {
										String key = getFilingKey();
										String cbInput = p_col.getCellString("cbInput");
										if(StringUtils.isBlank(cbInput)) {
											ZkUtil.msg("Please Enter Id to save as");
											return(ReturnMsg.defaultOk);
										}
										JSONObject jo = FilingUtil.getJson(getSessionHelper().getAgent(), null , key);
										key += "_"+cbInput;
										JSONArray ja = new JSONArray();
										for(int i=0;i<baseBr.getRowCount();i++) {
											baseBr.loadOneRecV(i);
											JSONObject jao = BiCellCollectionToJsonInterface.BiCellCollectionToJSON(baseBr.getCurrentCollection()); 
											ja.put(jao);
										}
										jo.put("records",ja);
										FilingUtil.storeJson(getSessionHelper().getAgent(), null , key, null, null,jo);
										return(ReturnMsg.defaultOk);
									}
									return(ReturnMsg.defaultFail);
								}
					
							}
						);	
				
				/*
				for(int i=0;i<baseBr.getRowCount();i++) {
					baseBr.loadOneRecV(i);
					JSONObject jo = BiCellCollectionToJsonInterface.BiCellCollectionToJSON(baseBr.getCurrentCollection()); 
					ja.put(jo);
				}
				*/
                String key = getFilingKey()+"_001";
				FilingUtil.storeJsonArray(getSessionHelper().getAgent(), null , key, null, null,ja);
//				baseBr.query();
//				baseBr.recal();
//				displayMonthJournal(currDisplayMonth);            		
            }
        });
        }
        if (btnLoadFrom != null) {
        btnLoadFrom.addEventListener("onClick", new EventListener<Event>() {
            @Override
            public void onEvent(Event event) throws Exception {
                System.out.println("Load From called!"); // Debugging log
                
				ArrayList<String> keys = FilingUtil.getKeys(getSessionHelper().getAgent(), getFilingKey() + "_%",null);
				if(keys != null) keys = ListUtil.stripStringArrayList(keys, getFilingKey()+"_");
				ZkUtil.comboboxDialogZkForm(
							new JSONObject()
								.put("lbPrompt","Load from ? (Required)")
								.put("btOK","Load")
								.put("btCancel","Cancel")
//							, new VectorUtil().addElement("001").addElement("002").toVector(),
							, keys,
							new ZkfAction() {

								@Override
								public ReturnMsg processAction(String p_id, SessionHelper p_sh,
										CellCollection p_col, JsonObject p_actionData, InputStream p_upload,
										Component p_target) throws Exception {
									if(p_id.equals("btCancel")) {
										return(ReturnMsg.defaultOk);
									}
									if(p_id.equals("btOK")) {
										String key = getFilingKey();
										String cbInput = p_col.getCellString("cbInput");
										if(StringUtils.isBlank(cbInput)) {
											ZkUtil.msg("Please Enter Id to Load From");
											return(ReturnMsg.defaultOk);
										}
										key += "_"+cbInput;
										JSONObject jo = FilingUtil.getJson(getSessionHelper().getAgent(), null , key);
										if(jo == null ) {
											ZkUtil.msg("No Previouse Save for Id " + cbInput);
											return(ReturnMsg.defaultOk);
										}
										for(int i=0;i<baseBr.getRowCount();i++) {
											baseBr.loadOneRecV(i);
											baseBr.markDelete(i, true);
										}
										
										/*
										JSONArray ja = new JSONArray();
										for(int i=0;i<baseBr.getRowCount();i++) {
											baseBr.loadOneRecV(i);
											JSONObject jao = BiCellCollectionToJsonInterface.BiCellCollectionToJSON(baseBr.getCurrentCollection()); 
											ja.put(jao);
										}
										jo.put("records",ja);
										*/
										baseBr.batchAddUpdateDelete();
										baseBr.query();
										baseBr.recal();
										JSONArray ja = jo.getJSONArray("records");
										for(int i=0;i<ja.length();i++) {
											JSONObject jox = ja.getJSONObject(i);
											baseBr.clearCurrentRec();
											//BiResult.jsonToResult(baseBr,jox);
											JsonToBiCellCollectionInterface.JsonToBiCellCollection(baseBr.getCurrentCollection(), jox, null);	
											baseBr.addCurrent();
										}
										baseBr.query();
										baseBr.recal();
										currDisplayMonth = -1;
										displayMonthJournal(currDisplayMonth);
										return(ReturnMsg.defaultOk);
									}
									return(ReturnMsg.defaultFail);
								}
					
							}
						);	 
                
                
            }
        });
        }
        baseBr.clearCurrentRec();
		baseBr.getCell("tradj_page").set("Total");
    }
    
    static void copyCellsByPrefix(CellCollection p_col,String p_fromPrefix,String p_toPrefix) throws Exception {
    	Hashtable <String,Cell> cellTable = p_col.getCellTable();
    	for(String fk : cellTable.keySet()) {
    		if(fk.startsWith(p_fromPrefix)) {
    			String tk = p_toPrefix + fk.substring(p_fromPrefix.length());
    			Cell fc = p_col.testCell(fk);
    			Cell tc = p_col.testCell(tk);
    			if(fc != null && tc != null) {
    				tc.set(fc.getObject());
    			}
    		}
    	}
    }
    static void resetCellsByPrefix(CellCollection p_col,String p_fromPrefix) throws Exception {
    	Hashtable <String,Cell> cellTable = p_col.getCellTable();
    	for(String fk : cellTable.keySet()) {
    		if(fk.startsWith(p_fromPrefix)) {
    			Cell fc = p_col.testCell(fk);
    			if(fc != null ) {
    				fc.resetValue();
    			}
    		}
    	}
    }

}
