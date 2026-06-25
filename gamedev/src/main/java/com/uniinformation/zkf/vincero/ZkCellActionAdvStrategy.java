package com.uniinformation.zkf.vincero;

import org.zkoss.zk.ui.Component;

import com.uniinformation.zkf.ZkCellActionWorkSheet;

public class ZkCellActionAdvStrategy extends ZkCellActionFtrStrategy{
	@Override 
	protected void addAdditionalRows() throws Exception {
		addOneEconomyRow(lb,sr,"Retail Sales",cprefix+"d_retailsales",cprefix+"d_retailscore");
		addOneEconomyRow(lb,sr,"ISM Manufaturing PMI",cprefix+"d_ismpmi",cprefix+"d_ismpmiscore");
		addOneEconomyRow(lb,sr,"ADP NFP (USD specific)",cprefix+"d_adpnfp",cprefix+"d_adpnfpscore");
		addOneEconomyRow(lb,sr,"Unemployment Claims",cprefix+"d_unemploy",cprefix+"d_unempscore");
		addOneEconomyRow(lb,sr,"PPI",cprefix+"d_ppi",cprefix+"d_ppiscore");
		addOneBlankRow(lb,sr);
		addOneEconomyRow(lb,sr,"Extreme ?",cprefix+"d_cotextreme",cprefix+"d_cotexscore");
		addOneEconomyRow(lb,sr,"COT Report Direction",cprefix+"d_cotdir",cprefix+"d_cotdirscore");
		addOneBlankRow(lb,sr);
		addOneEconomyRow(lb,sr,"Advanced Calculator Result",cprefix+"d_10xresult",cprefix+"d_10xscore");
	}
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		cprefix = "advs";
		baseBrName = "vincero.AdvStrategy";
		super.doAfterCompose(arg0);
		formCollection.getCell("sheetTitle").set("Advance Foundamental Analysys Calculator");
		
	}
	
	@Override
	protected String getFilingKey() {
		return(String.format("VAWS_%s_%s_%s", "AdvStrategy", getSessionHelper().getLoginId(), getSessionHelper().getVcode()));
	}
}
