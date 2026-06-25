package com.uniinformation.jxapp;

import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxField;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;

public class AfsServiceItem extends JxZkBiBase{
	void reloadSerialItemList() {
		try {
				TableRec tr;
				if(getBr().getCell("svmc_irg").getInt()> 0) 
					tr = getBr().getSelectUtil().getQueryResult("select stmcm_name from mcfitmodel,stmcmodel where stmcm_rg = mcfm_modelrg and mcfm_mrg =" + getBr().getCell("svmc_irg").getInt() + " order by 1" , null);
				else
					tr = getBr().getSelectUtil().getQueryResult("select stmcm_name from stmcmodel order by 1", null);
					Vector v = new Vector();
					for(int i=0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						v.add(tr.getField("stmcm_name"));
					}
					getBr().getCell("stmcm_name").setItemList(v);
		} catch (Exception ex){ 
			UniLog.log(ex);
		}	
	}
	@Override
	public void afterBind() {
		super.afterBind();
		new JxFieldChange("st_icode") {
			public boolean valueChanged(JxField fd,String orgValue){  
				reloadSerialItemList();
				try {
				if(getBr().getCell("stmcm_name").getItemList().size() == 1) {
					getBr().getCell("stmcm_name").set(getBr().getCell("stmcm_name").getItemList().get(0));
				} else {
					getBr().getCell("stmcm_name").set("");
				}
				} catch (CellException cex ) {
					UniLog.log(cex);
				}
				return(true);
			}
		};
	}
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		if(mode == JxZkBiBase.MODE_ADD) {
				getBr().getCell("stmcm_name").setItemList(new Vector());
				reloadSerialItemList();
		} 
		if(mode == JxZkBiBase.MODE_UPDATE) {
				getBr().getCell("stmcm_name").setItemList(new Vector());
				reloadSerialItemList();
		} 
	}
}
