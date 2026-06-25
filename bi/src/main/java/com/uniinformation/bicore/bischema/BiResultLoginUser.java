package com.uniinformation.bicore.bischema;

import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.Base64Util;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.WordPressHelper;

public class BiResultLoginUser extends BiResult {
	public BiResultLoginUser(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
	}
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		if (sh.getWPLinkUser()) {
			WordPressHelper wph = new WordPressHelper(sh);
			ReturnMsg rtnMsg = wph.deleteUser(col.getCellString("lgu_login").trim());
			if (!rtnMsg.getStatus()) {
				return rtnMsg;
			}
		}
		return ReturnMsg.defaultOk;
	}
	
	ReturnMsg encryptCredentials() {
		if(getSessionHelper().getAESKey() != null && getCell("lgu_credentials") != null) { 
			try {
				JSONObject jo = new JSONObject();
				jo.put("lgu_bpcode",getCell("lgu_bpcode").getObject());
				getCell("lgu_bpcode").resetValue();
				if(getCell("lgu_email") != null) {
					jo.put("lgu_email",getCell("lgu_email").getObject());
					getCell("lgu_email").resetValue();
				}
				if(getCell("lgu_phone") != null) {
					jo.put("lgu_phone",getCell("lgu_phone").getObject());
					getCell("lgu_phone").resetValue();
				}
				getCell("lgu_credentials").set(Base64Util.encryptStrToBase64(getSessionHelper(), jo.toString()));
			} catch (Exception ex) {
				UniLog.log(ex);
				return(ReturnMsg.defaultFail);
			}
		}
		return(ReturnMsg.defaultOk);
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col) {
		ReturnMsg rtn = encryptCredentials();
		if(!rtn.getStatus()) return(rtn);
		return(super.biBeforeAddCurrent(col));
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg rtn = encryptCredentials();
		if(!rtn.getStatus()) return(rtn);
		return(super.biBeforeUpdateCurrent(col));
	}
	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		if(col.getCellString("lgu_login").startsWith("#")) {
			return(new ReturnMsg(false,"Login Id should start with a-z"));
		}
		if (sh.getWPLinkUser()) {
			WordPressHelper wph = new WordPressHelper(sh);
			//use chnname as email temporary
			ReturnMsg rtnMsg = wph.updateUser(col.getCellString("lgu_login").trim(), col.getCellString("lgu_chnname").trim(), col.getCellString("lgu_bpcode"));
			if (!rtnMsg.getStatus()) {
				return rtnMsg;
			}
		}
		sh.deleteLoginTokenRecord(col.getCellString("lgu_login"));
		return ReturnMsg.defaultOk;
	}
	
	
		@Override
		public String getColumnDisplayString(ColumnCell p_cell) {
			if(
					p_cell.getCellLabel().equals("lgu_bpcode")  ||
					p_cell.getCellLabel().equals("lgu_pwd2") 
					) {
				return("***");
				
			}
			return(super.getColumnDisplayString(p_cell));
		}	
	
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable>  ht = super.addExtraWhereStr(p_where, p_hash);
		String uid = getSelectUtil().getLoginId();
		UniLog.log("user = " + getSelectUtil().getLoginId());
		if(!BiSchema.hasAccessRight(sh, "#super") && !sh.isAdminUser()) {
				HashSet<String> accesslist = sh.getAccessRights();
				String ss = null;
				for(String as : accesslist) {
					if(ss == null)  {
						ss = " and (lgu_access = '' or lgu_access in ('"+ as + "'";
					} else ss += ",'"+as+"'";
				}
				ss += ")) ";
				p_where.appendString(ss);
		} 
		return(ht);
	}
}
