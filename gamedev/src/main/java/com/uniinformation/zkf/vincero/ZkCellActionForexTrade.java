package com.uniinformation.zkf.vincero;

import java.io.InputStream;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import com.google.gson.JsonObject;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkf.ZkCellActionWorkSheet;
import com.uniinformation.zkf.ZkfAction;

public class ZkCellActionForexTrade extends ZkCellActionWorkSheet{

	protected String getFilingKey() {
		return(String.format("VAWS_%s_%s_%s", "ForexTrade", getSessionHelper().getLoginId(), getSessionHelper().getVcode()));
	}
	
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		try {
			Executions.getCurrent().createComponents("ForexTrade.zul", arg0, null);
		} catch (Exception ex) {
			UniLog.log1("Load ForexTrade.zul failed");
		}
		
		onClickListener = new EventListener() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				// TODO Auto-generated method stub
				Component c = (Component)arg0.getTarget();
				if(c.getId().equals("btSaveWorkSheet")) {
					
					ArrayList<String> keys = FilingUtil.getKeys(getSessionHelper().getAgent(), getFilingKey() + "_%",null);
					if(keys != null) keys = ListUtil.stripStringArrayList(keys, getFilingKey()+"_");
					ZkUtil.comboboxDialogZkForm(
								new JSONObject()
									.put("lbPrompt","Save to ? (blank for default)")
									.put("btOK","Save")
									.put("btCancel","Cancel")
//								, new VectorUtil().addElement("001").addElement("002").toVector(),
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
											if(!StringUtils.isBlank(cbInput)) {
												key += "_"+cbInput;
											}
											JSONObject jo = BiResult.resultToJson(baseBr);
											FilingUtil.storeJson(getSessionHelper().getAgent(), null , key, null, null,jo);
											return(ReturnMsg.defaultOk);
										}
										return(ReturnMsg.defaultFail);
									}
						
								}
							);
					
					/*
					JSONObject jo = BiResult.resultToJson(baseBr);
					String key = getFilingKey();
					FilingUtil.storeJson(getSessionHelper().getAgent(), null , key, null, null,jo);
					jo = null;
					*/
				}
				if(c.getId().equals("btLoadWorkSheet")) {
					ArrayList<String> keys = FilingUtil.getKeys(getSessionHelper().getAgent(), getFilingKey() + "_%",null);
					if(keys != null) keys = ListUtil.stripStringArrayList(keys, getFilingKey()+"_");
					ZkUtil.comboboxDialogZkForm(
								new JSONObject()
									.put("lbPrompt","Load from ? (blank for default)")
									.put("btOK","Load")
									.put("btCancel","Cancel")
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
											if(!StringUtils.isBlank(cbInput)) {
												key += "_"+cbInput;
											}
											JSONObject jo = FilingUtil.getJson(getSessionHelper().getAgent(), null , key);
											if(jo != null) {
												BiResult.jsonToResult(baseBr,jo);
											}
											return(ReturnMsg.defaultOk);
										}
										return(ReturnMsg.defaultFail);
									}
						
								}
							);					
					
					/*
					String key = getFilingKey();
					JSONObject jo = FilingUtil.getJson(getSessionHelper().getAgent(), null , key);
					if(jo != null) {
//						baseBr.loadFromJson(jo);
						BiResult.jsonToResult(baseBr,jo);
					}
					jo = null;
					*/
				}
			}
		};		
		
		
		baseBrName = "vincero.ForexTrade";
		super.doAfterCompose(arg0);
	}
}
