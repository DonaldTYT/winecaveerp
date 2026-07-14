package com.uniinformation.jxapp.propertymgmt;

import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ReturnMsg;
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
 		   		if (Objects.equals(loginid, sessionHelper.getLoginId()))
 		   			jxSetEnable("lgu_login", false);
 		   		jxSetVisible("list_bischema_WebMenuTree", sessionHelper.isAdminUser() || !Objects.equals(loginid, sessionHelper.getLoginId()));
			} catch (Exception e) {
				UniLog.log(e);
			}
		} else
		   	jxSetVisible("list_bischema_WebMenuTree", true);
	}

	@Override
	protected ReturnMsg beforeAdd(BiResult br) {
		ReturnMsg rtn = super.beforeAdd(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			rtn = validationRecord(false);
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		
		return rtn;
	}

	@Override
	protected ReturnMsg beforeUpdate(BiResult br) {
		ReturnMsg rtn = super.beforeUpdate(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			rtn = validationRecord(true);
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		
		return rtn;
	}

	private ReturnMsg validationRecord(boolean isUpdate) throws Exception {
		String s = getBr().getCellString("lgu_bpcode");
		if (StringUtils.isNotEmpty(s) && !s.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?~`]).{10,}"))
			return new ReturnMsg(false, "密碼長度最少 10 位，由大小寫英文、數字、符號組成");
		return ReturnMsg.defaultOk;
	}
}
