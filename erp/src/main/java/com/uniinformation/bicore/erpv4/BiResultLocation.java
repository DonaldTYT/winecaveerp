package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultLocation extends BiResultErpv4 {

	public BiResultLocation(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected ReturnMsg afterLoadSerialMap2() {
		if(!sh.hasAccessRight("#allloc")) {
			for(int i=resultTr.getRecordCount()-1;i>=0;i--) {
				loadOneRec(i,getCurrentCollection(),false);
				int lcrg = getCellInt("lc_rg");
				String cocode = getCellString("cocode");
				UniLog.log("LocationAfterLoadSerialMap2 " + i + " lcrg "+ lcrg 
							+ " cocode " + cocode
							+ " #lcrg "  + sh.hasAccessRight("#lcrg_"+lcrg)
							+ " #lcco_ " + sh.hasAccessRight("#lcco_"+cocode)
						);
				if(
					(!sh.hasAccessRight("#lcrg_"+lcrg)) ||
					(!sh.hasAccessRight("#lcco_"+cocode)) ) {
					try {
						UniLog.log("LocationAfterLoadSerialMap2 " + i + " deleted");
						resultTr.deleteRecord(i);
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
			}
		}
		
		return(ReturnMsg.defaultOk);
	}
}
