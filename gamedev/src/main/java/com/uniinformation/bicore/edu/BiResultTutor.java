package com.uniinformation.bicore.edu;

import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultTutor extends BiResult {
	public BiResultTutor(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtn = super.biBeforeUpdateCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		
		try {
			if (StringUtils.isBlank(col.getCellString("estt_ttno"))) {
				Value v = getView().getSchema().getUniqueRg(this,"", 53008, "estutor", "estt_ttno", "T&&&&&&");
				col.getCell("estt_ttno").set(v.toString());
			}
		} catch (Exception cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,-1,cex.getMessage()));
		}
		return rtn;
	}

	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeUpdateCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		rtn = updateLoginUser(col);
		return(rtn);
	}

	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeDeleteCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		rtn = deleteLoginUser(col);
		return(rtn);
	}

	@Override 
	public HashSet<BiTable> addExtraWhereStr(Wherecl p_where, HashSet<BiTable> p_hash) {
		SessionHelper sh = getSessionHelper();
		if (!sh.isAdminUser() && !sh.hasAccessRight("#edu")) {
			if (sh.hasAccessRight("#tutor"))
				p_where.andUniop("estt_ttno", "=" , sh.getLoginId().toUpperCase());
		}
		return super.addExtraWhereStr(p_where, p_hash);
	}

	private ReturnMsg updateLoginUser(CellCollection col) {
		int ttrg = col.getCellInt("estt_rg");
		UniLog.log1("tutor rg:%d", ttrg);
		if (ttrg <= 0) {
			UniLog.log1("invalid ttrg");
			return ReturnMsg.defaultOk;
		}
		BiResult brWebMenuTreeQuery = null;
		String loginId = col.getCellString("estt_ttno").toLowerCase();
		String lvlName = col.getCellString("esmgl_name");
		String accessKey;
		if (StringUtils.equals(lvlName, "Manager"))
			accessKey = "#edu";
		else if (StringUtils.equals(lvlName, "Admin"))
			accessKey = "#eduadmin";
		else
			accessKey = "#tutor";
		try {
			//update accesskey
			brWebMenuTreeQuery = sh.newBiResult("WebMenuTree");
			brWebMenuTreeQuery.clearCondition();
			brWebMenuTreeQuery.addCustomCondition(String.format("webmt_user = '%s' and webmt_parent = '%s'", loginId, accessKey));
			ReturnMsg rtn;
			if ((rtn = brWebMenuTreeQuery.query(true, false)).getStatus()) {
				if (!brWebMenuTreeQuery.next()) {
					su.executeUpdate("delete from webmenutree where webmt_user = ?", new Wherecl().appendArgument(loginId));
					su.executeUpdate("insert into webmenutree (webmt_user, webmt_parent) values(?,?)", 
							new Wherecl().appendArgument(loginId) .appendArgument(accessKey));
				}
			}
			else
				throw new Exception(rtn.getMsg());

			//update username
			su.executeUpdate("update loginuser set lgu_name = ?, lgu_disabled = ? where lgu_login = ?",
					new Wherecl()
						.appendArgument(col.getCellString("estt_name"))
						.appendArgument(col.getCellString("estt_status").equals("Cancelled") ? "Y" : "N")
						.appendArgument(loginId)
						);
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ReturnMsg(false, e.getMessage());
		}
		finally {
			if (brWebMenuTreeQuery != null)
				brWebMenuTreeQuery.close();
		}
		return ReturnMsg.defaultOk;
	}

	private ReturnMsg deleteLoginUser(CellCollection col) {
		int ttrg = col.getCellInt("estt_rg");
		UniLog.log1("tutor rg:%d", ttrg);
		if (ttrg <= 0) {
			UniLog.log1("invalid ttrg");
			return ReturnMsg.defaultOk;
		}
		String loginId = col.getCellString("estt_ttno").toLowerCase();
		try {
			su.executeUpdate("delete from loginuser where lgu_login = ?", new Wherecl() .appendArgument(loginId));
			su.executeUpdate("delete from webmenutree where webmt_user = ?", new Wherecl() .appendArgument(loginId));
		}
		catch (Exception e) {
			e.printStackTrace();
			return new ReturnMsg(false, e.getMessage());
		}
		return ReturnMsg.defaultOk;
	}
}
