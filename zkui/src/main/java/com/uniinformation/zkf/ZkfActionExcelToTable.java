package com.uniinformation.zkf;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;

import com.google.gson.JsonObject;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.poi.ExcelPoi;
import com.uniinformation.webcore.SessionHelper;

public class ZkfActionExcelToTable implements ZkfAction {

	@Override
	public ReturnMsg processAction(String p_id, SessionHelper p_sh, CellCollection p_col, JsonObject p_actionData,
			InputStream p_upload, Component p_target) throws Exception {
		ExcelPoi exlpoi = null;
		try {
			/* currenly only support xlsx */
			exlpoi = ExcelPoi.newExcelPoi(p_upload,true);
			p_upload.close();
		} 
		catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false));
		}
		exlpoi.excel_translate_Chinese(0);
		UniLog.log("Excel row count = " + exlpoi.getRowCount());	
		// TODO Auto-generated method stub
		if(exlpoi.getRowCount() <= 0) {
			return new ReturnMsg(false,"File iS Empty");
		}
		int maxColumn = exlpoi.excel_getColumnCount(0);
		if(maxColumn <= 0) {
			return new ReturnMsg(false,"File Does Not Hava Any Column");
		}
		List <String> impColList = new ArrayList<String>();
		int lastColumnNum = -1;
		for(int i = 0;i < maxColumn;i++) {
			String colhdr = exlpoi.getStringValue(0, i);
			if(colhdr != null && !colhdr.trim().equals("")) {
				impColList.add(colhdr);
				UniLog.log("Add One Column "+i+ " " + colhdr);
				lastColumnNum = i;
			} else {
//				impColList.add(null);
				break;
			}
		}
		if(lastColumnNum <= 0) {
			return new ReturnMsg(false,"Columns Is Empty");
		}
		for(int i = 0;i <= lastColumnNum ;i++) {
			int styleIdx = exlpoi.excel_getColumnStyleIdx(i);
			UniLog.log("Column "+i+ " styleIdx " + styleIdx);
			exlpoi.getStyleFormatByIdx(styleIdx) ;
		}
		
		
		return ReturnMsg.defaultOk;
	}

}
