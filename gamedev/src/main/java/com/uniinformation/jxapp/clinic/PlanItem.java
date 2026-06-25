package com.uniinformation.jxapp.clinic;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;

public class PlanItem extends JxZkBiBase {
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		
		if(mode == JxZkBiBase.MODE_ADD) {
			try{
				getBr().getCell("pi_status").set("Y");
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
}