package com.uniinformation.jxapp.clinic;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;

public class DocType extends JxZkBiBase {
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		
		if(mode == JxZkBiBase.MODE_ADD) {
			updateRecordTime();
			try{
				getBr().getCell("bcdt_status").set("Y");
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
		UniLog.log("doctype bindCellCollection");
	}
	private void updateRecordTime() {
		try {
			getBr().getCell("bcdt_utime").set((int)(System.currentTimeMillis() / 1000));
		} catch (CellException e) {
			e.printStackTrace();
		}
	}
	@Override
	protected ReturnMsg beforeAdd(BiResult br) {
		UniLog.log("doctype beforeAdd");
		//updateRecordTime();
		return super.beforeAdd(br);
	}
	@Override
	protected ReturnMsg beforeUpdate(BiResult br) {
		UniLog.log("doctype beforeUpdate");
		updateRecordTime();
		return super.beforeUpdate(br);
	}
}
