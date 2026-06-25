package com.uniinformation.jxapp.propertymgmt;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;

public class LoginUser extends com.uniinformation.jxapp.LoginUser {

	@Override
	public void bindCellCollection(BiResult p_br, int mode) {
		UniLog.log("bindCellCollection called");
		super.bindCellCollection(p_br, mode);
		if (mode == JxZkBiBase.MODE_UPDATE) {
			try {
				String loginid = p_br.getCellString("lgu_login");
 		   		TableRec tr = p_br.getSelectUtil().getQueryResult("select col_a from payment where col_x = ?", 
									new Wherecl().appendArgument(loginid));
 		   		if (tr.getRecordCount() > 0) {
 		   			jxSetEnable("lgu_login", false);
 		   			jxSetEnable("lgu_name", false);
 		   		}
			} catch (Exception e) {
				UniLog.log(e);
			}
		}
	}
}
