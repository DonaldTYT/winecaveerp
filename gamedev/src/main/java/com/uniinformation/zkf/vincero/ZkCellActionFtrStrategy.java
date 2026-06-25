package com.uniinformation.zkf.vincero;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiCellCollectionToJsonInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.JsonToBiCellCollectionInterface;
import com.uniinformation.bicore.vincero.BiResultFtrStrategy;
import com.uniinformation.cell.Cell;
import com.uniinformation.jx.zk.ZkJxCellValueMapper;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkf.ZkCellActionWorkSheet;

public class ZkCellActionFtrStrategy extends ZkCellActionWorkSheet{
	protected String cprefix=null;
	protected String urlName=null;
	
	Component addOneListCell(BiCellCollection bc,String p_cellName,Listitem li,int span) throws Exception {
			Listcell lc;
			ColumnCell cc;
			Component comp;
			ZkJxCellValueMapper vm;
			lc = new Listcell();
			if(span > 1) {
				lc.setSpan(span);
			}
			cc = (ColumnCell) bc.getCell(p_cellName);
			comp = JxZkBiBase.createComponentByBiColumn(cc.getBiColumn());
			vm = new ZkJxCellValueMapper(comp);
			if(cc.getBiColumn().getColumnType().equals("memo")) {
				((Textbox) comp).setHflex("1");
			}
			comp.setAttribute("CellValueMapper", vm);
			comp.addEventListener(Events.ON_CHANGE, zkf.getTbChangeListener());
			comp.addEventListener(Events.ON_OPEN, zkf.getTbChangeListener());
			cc.mapAdd(vm);

			lc.appendChild(comp);
			li.appendChild(lc);
			return(comp);
	}
	
	void addOneReviewRow(Listbox lb,BiResult sr,String p_name,String p_memoCell,List<String> p_itemClasses) throws Exception {
		Listitem li;
		li = new Listitem();
		li.appendChild(new Listcell(p_name));
		for(BiCellCollection bc : sr.getRowCollectionList()) {
			Component comp = addOneListCell(bc,p_memoCell,li,2);
			if(p_itemClasses != null && comp instanceof Combobox) {
				Combobox cb = (Combobox) comp;
				List<Component> ciList = cb.getChildren();
				for(int i = 0;i<ciList.size();i++) {
					if(i < p_itemClasses.size()) {
						Comboitem ci = (Comboitem) ciList.get(i);
						ci.setSclass(p_itemClasses.get(i));
					}
				}
			}
		}
		lb.appendChild(li);
	}
	void addOneEconomyRow(Listbox lb,BiResult sr,String p_name,String p_selectCell,String p_scoreCell) throws Exception {
		Listitem li;
		li = new Listitem();
		li.appendChild(new Listcell(p_name));
		for(BiCellCollection bc : sr.getRowCollectionList()) {
			addOneListCell(bc,p_selectCell,li,1);
			addOneListCell(bc,p_scoreCell,li,1);
		}
		lb.appendChild(li);
	}
	void addOneBlankRowWithComment(Listbox lb,BiResult sr,String p_comment) throws Exception {
		Listitem li;
		li = new Listitem();
		li.appendChild(new Listcell(p_comment));
		for(BiCellCollection bc : sr.getRowCollectionList()) {
			Listcell lc = new Listcell();
			lc.setSpan(2);
			li.appendChild(lc);
		}
		lb.appendChild(li);
	}
	void addOneBlankRow(Listbox lb,BiResult sr) throws Exception {
		Listitem li;
		li = new Listitem();
		li.appendChild(new Listcell(""));
		for(BiCellCollection bc : sr.getRowCollectionList()) {
			Listcell lc = new Listcell();
			lc.setSpan(2);
			li.appendChild(lc);
		}
		lb.appendChild(li);
	}
	
	protected Listbox lb;
	protected BiResult sr;

	protected String getFilingKey() {
		return(String.format("VAWS_%s_%s_%s", "FtrStrategy", getSessionHelper().getLoginId(), getSessionHelper().getVcode()));
	}
	
	protected void addAdditionalRows () throws Exception {
		
	}
	
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		if(cprefix == null) cprefix = "ftrs";
		if(baseBrName == null) baseBrName = "vincero.FtrStrategy";
		if(urlName == null) urlName = "FtrStrategy.zul";
		try {
			Executions.getCurrent().createComponents(urlName, arg0, null);
		} catch (Exception ex) {
			UniLog.log1("Load Strategy.zul failed");
		}
		
		onClickListener = new EventListener() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				// TODO Auto-generated method stub
				Component c = (Component)arg0.getTarget();
				{
					ZkJxCellValueMapper vm = (ZkJxCellValueMapper) c.getAttribute("CellValueMapper");
					if(vm != null) {
						Cell cc = vm.getBindedCell();
						if(cc != null && cc instanceof ColumnCell) {
							String cl = cc.getCellLabel();
							if((cprefix+"d_bias").equals(cl)) {
								String val = cc.getString();
								HtmlBasedComponent hc = (HtmlBasedComponent) c;
								String bcolor;
								String fcolor;
								if( val.equals("Bullish") || val.equals("Neutral Bullish")) {
									bcolor = "green";
									fcolor = "white";
								} else if( val.equals("Bearish") || val.equals("Neutral Bearish")) {
									bcolor = "red";
									fcolor = "white";
								} else {
									bcolor = "white";
									fcolor = "black";
								}
								Clients.evalJavaScript(String.format("setBiasClass(\"%s\",\"%s\",\"%s\")",hc.getUuid(),bcolor,fcolor));
								/*
								Textbox tb = null;
								if(hc instanceof Combobox) {
									Combobox cb = (Combobox) hc;
									for (Component child : cb.getChildren()) {
								    	if (child instanceof Textbox) {
								       		tb = (Textbox) child;
								       		break;
								    	}
									}
								}
								if(tb != null) {
									if( val.equals("Bullish") || val.equals("Neutral Bullish")) {
										tb.setSclass("backgroundInGreen");
									} else if( val.equals("Bearish") || val.equals("Neutral Bearish")) {
										tb.setSclass("backgroundInRed");
									} else {
										tb.setSclass("backgroundInWhite");
									}
								}
								*/
								/*
								if( val.equals("Bullish") || val.equals("Neutral Bullish")) {
									hc.setSclass("backgroundInGreen");
								} else if( val.equals("Bearish") || val.equals("Neutral Bearish")) {
									hc.setSclass("backgroundInRed");
								} else {
									hc.setSclass("backgroundInWhite");
								}
								*/
								hc.invalidate();
							}
						}
					}
				}
				if(c.getId().equals("btSaveWorkSheet")) {
//					JSONObject jo = BiResult.resultToJson(baseBr);
					JSONObject jo = BiCellCollectionToJsonInterface.BiCellCollectionToJSON(baseBr.getCurrentCollection());
					String key = getFilingKey();
					FilingUtil.storeJson(getSessionHelper().getAgent(), null , key, null, null,jo);
					jo = null;
				}
				if(c.getId().equals("btLoadWorkSheet")) {
					String key = getFilingKey();
					JSONObject jo = FilingUtil.getJson(getSessionHelper().getAgent(), null , key);
					if(jo != null) {
						ReturnMsg rtn = JsonToBiCellCollectionInterface.JsonToBiCellCollection(baseBr.getCurrentCollection(), jo, 
									new JsonToBiCellCollectionInterface() {
										@Override
										public ReturnMsg onAddSubRecord(BiResult parentBr, BiResult sublinkBr,
												int idx) {
											return ReturnMsg.defaultOk;
										}
									}
								);
						
//						baseBr.saveCurrentToJson();
					}
					jo = null;
				}
			}
		};		
		
		super.doAfterCompose(arg0);
		baseBr.clearCurrentRec();
		((BiResultFtrStrategy) baseBr).addCcyRecords();
//		BiResult sr = baseBr.getSubLink("vincero.FtrStrategyDetail");
		sr = ((BiResultFtrStrategy) baseBr).getDetailLink();

		lb = (Listbox) zkf.getComponent("list_ftxstretegy");
		lb.getItems().clear();
//	void addOneBlankRowWithComment(Listbox lb,BiResult sr,String p_comment) throws Exception {
//		addOneEconomyRow(lb,sr,"Is the Country Doing Well or Not?",cprefix+"d_dowellstr",cprefix+"d_dowellscore");
		addOneBlankRowWithComment(lb,sr,"Is the Country Doing Well or Not?");
		addOneEconomyRow(lb,sr,"Economic Growth",cprefix+"d_econgrowth",cprefix+"d_ecscore");
		addOneEconomyRow(lb,sr,"Inflation",cprefix+"d_inflation",cprefix+"d_infscore");
		addOneEconomyRow(lb,sr,"Employment",cprefix+"d_employment",cprefix+"d_empscore");
		addOneEconomyRow(lb,sr,"Balance of Trade",cprefix+"d_baloftrade",cprefix+"d_botscore");
		addOneEconomyRow(lb,sr,"Geopolitical Event",cprefix+"d_geoevent",cprefix+"d_geoscore");
		addOneBlankRow(lb,sr);
		addOneBlankRow(lb,sr);
		addOneBlankRow(lb,sr);
		addOneEconomyRow(lb,sr,"Central Bank Stance?",cprefix+"d_ctrbankst",cprefix+"d_cbsscore");
		addOneBlankRow(lb,sr);
		addOneEconomyRow(lb,sr,"Risk On or Risk Off?",cprefix+"d_ccyrisk",cprefix+"d_ccyrscore");
		addOneBlankRow(lb,sr);
		addOneEconomyRow(lb,sr,"Calculator Result",cprefix+"d_result",cprefix+"d_resultscore");
		addOneBlankRow(lb,sr);
		addAdditionalRows();
		addOneReviewRow(lb,sr,"What is your Personal View on the strength of the Currency?",cprefix+"d_review",null);
		ArrayList<String> cList = new ArrayList<String>();
		cList.add("backgroundInGreen");
		cList.add("backgroundInGreen");
		cList.add("backgroundInWhite");
		cList.add("backgroundInRed");
		cList.add("backgroundInRed");
		addOneReviewRow(lb,sr,"Your Fundamental Bias ",cprefix+"d_bias",cList);
		
	}
}
