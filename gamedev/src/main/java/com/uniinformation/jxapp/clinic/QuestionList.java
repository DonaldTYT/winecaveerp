package com.uniinformation.jxapp.clinic;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;

public class QuestionList extends JxZkBiBase {
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		
		if(mode == JxZkBiBase.MODE_ADD) {
			try{
				getBr().getCell("qnrq_status").set("Y");
				getBr().getCell("qnrq_mf").set("U");
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
	}
}