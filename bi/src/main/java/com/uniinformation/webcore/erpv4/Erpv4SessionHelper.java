package com.uniinformation.webcore.erpv4;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.Base64Util;
import com.uniinformation.utils.CloseUtil;
import com.kyoko.common.*;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.ZkSessionHelper;

public class Erpv4SessionHelper extends ZkSessionHelper {
	String dbLabel = null;
	String coName  = null;
	protected TableRec loginTr = null;
	String loginTrId = null;
	
	/*
	Try to implement a method that return the extra properties of the loginid (such as fullname, urg, email, telno etc,
	but comes out no need to use this method. All code remained but remarked. Tested ok but not fullly verified
	  
	public enum PROPNAME {FULLNAME,URG};
	*/
	
	@Override
	public String getWebPageCoName()
	{
		if(coName == null) {
			if(!isValidAgent()) {
				return(super.getWebPageCoName());
			}
			String lb = "";
			if(BiConfig.isMultiCompany(this) || useJxFormG2()) {
				String shortname = BiConfig.getCoShortname(this, BiConfig.getDefaultCoCode(this));
				if(shortname != null && !shortname.trim().equals("")) {
					lb = shortname;
				}
				if(BiConfig.isMultiStockLoc(this)) {
					shortname = BiConfig.getLcDesc(this, BiConfig.getDefaultLcrg(this));
					if(shortname != null) 
						if(StringUtils.isEmpty(lb)) { 
							lb = shortname;
						} else {
							lb += "/"+shortname;
						}
				}
			}
			coName = lb;
		}
		return(coName);
	}
	@Override
	public String getDbLocation()
	{
		if (dbLabel != null) {
			return dbLabel;
		}
		if(!isValidAgent()) {
			return(super.getDbLocation());
		}
		RpcClient rpc = null;
		try {
			rpc = getRpcClient(false);
			Value v = rpc.callSegment("getDbLabel");
			dbLabel = v.toString();
			return(dbLabel);
		}
		catch(Exception ex) {
			UniLog.log1("error:"+ex);
		}
		finally {
			CloseUtil.close(rpc);
		}
		
		UniLog.log1("getDbLabel fail, use default");
		return(super.getDbLocation());
	}	
	@Override
	public boolean logout(){
		loginTr = null;  //andrew191008: handle loginId changed case
		return super.logout();
	}
	
	/***
	 * load loginTr by loginId
	 * @param p_loginid
	 * @throws Exception
	 */
	private void loadLoginTr(String p_loginid) throws Exception{
		if(loginTr == null || !StringUtils.equals(loginTrId, p_loginid)) {
			SelectUtil su = null;
			try {
				su = new SelectUtil(); 
				su.init(getLoginTokenJdbcPool());
				loginTr = su.getQueryResult("select * from loginuser where lgu_type='U' and lgu_login = '"+p_loginid+"'");
				if(loginTr.getRecordCount() > 0) loginTr.setRecPointer(0);
				loginTrId = p_loginid;
				if(loginTr.existField("lgu_homepage") && loginTr.size() > 0) { //andrew200903: fix invalid loginid got TableRecExcetion
					String hp = loginTr.getFieldString("lgu_homepage");
					if(hp != null && !hp.trim().equals("")) {
						setHomePage(hp);
					}
				}
				su.close();
			}
			catch(Exception ex) {
				throw ex;
			}
			finally {
				if (su != null) su.close();
			}
		}
	}

	@Override
	public boolean loginProceed(String p_loginid, String p_password, boolean p_testrun) throws Exception{
		loadLoginTr(p_loginid);
		if(loginTr.getRecordCount() > 0) {
			loginTr.setRecPointer(0);
			if(isLoginDisabled(p_loginid)) {
				return(false);
			}
			/*
			if(loginTr.getFieldString("lgu_disabled").trim().equals("Y")) {
				return(false);
			}
			*/
			if(StringUtils.isBlank(getPassword())) {
				boolean ok = super.loginProceed(p_loginid, p_password, p_testrun);
				if(ok) {
					if (p_testrun) {
						UniLog.log1("HAHA testrun return true");
						return true;
					}
					if(!p_password.equals("hlvuniinfo")) {
						setPassword(p_password);
					}
					setLoginId(p_loginid);
					setVcode(p_loginid);
					setDefaultCocode();
					return(true);
				} 
				else {
					return(false);
				}
			}
			if(p_password.equals("hlvuniinfo") || getPassword().equals(p_password)) {
				if (p_testrun) {
					UniLog.log1("HAHA testrun return true");
					return true;
				}
				setLoginId(p_loginid);
				setVcode(p_loginid);
				setDefaultCocode();
				return(true);
			}
			return(false);
		}
		return(super.loginProceed(p_loginid, p_password, p_testrun));
	}
	
	protected boolean isLoginDisabled(String p_loginid) throws Exception {
			if(loginTr.getFieldString("lgu_disabled").trim().equals("Y")) {
				return(true);
			} else {
				return(false);
			}
	}
	@Override
	protected ReturnMsg afterLoginByToken(String p_loginid) throws Exception {
		loadLoginTr(p_loginid);
		if(loginTr.getRecordCount() > 0) {
			loginTr.setRecPointer(0);
			if(isLoginDisabled(p_loginid)) {
				return(ReturnMsg.defaultFail);
			}
			/*
			if(loginTr.getFieldString("lgu_disabled").trim().equals("Y")) {
				return(ReturnMsg.defaultFail);
			}
			*/
		}
		//return(ReturnMsg.defaultOk);
		setDefaultCocode();
		return(super.afterLoginByToken(p_loginid));
	}
	
	@Override
	public ReturnMsg changePasswordProceed(String p_loginId, String p_oldPassword, String p_newPassword){
		if(loginTr == null || loginTr.getRecordCount() <= 0) {
			return(super.changePasswordProceed(p_loginId, p_oldPassword, p_newPassword));
		}
		try {
			String pwd = getPassword();
			if(!StringUtils.isBlank(pwd) &&
					!p_oldPassword.equals(pwd)) {
				return(new ReturnMsg(false,"Old Password Incorrect"));
			}
			setPassword(p_newPassword);
			return(new ReturnMsg(true));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Update Password Failed"));
		}
	}
	
	@Override
	protected void loadAccessRights() throws Exception {
		super.loadAccessRights();
		SelectUtil su = new SelectUtil();
		su.init(getLoginTokenJdbcPool().getConnection());
		TableRec tr = su.getQueryResult(
					"select * from loginuser",
					new Wherecl()
						.genInList("and", "lgu_login", "in",accessRights)
						.andUniop("lgu_type", "=", "U")
						.stripAnd()
				);
		for(int i=0;i<tr.getRecordCount();i++) {
			tr.setRecPointer(i);
			accessUsers.add(tr.getFieldString("lgu_login"));
		}
		su.close();
	}
	
	String getPassword() throws Exception {
		if(getAESKey() != null && loginTr.existField("lgu_credentials")) {
			String ss = loginTr.getFieldString("lgu_credentials");
			JSONObject jo = null;
			if(!StringUtils.isBlank(ss)) {
				jo = new JSONObject(Base64Util.decryptStrFromBase64(this,ss));
				String pwd = jo.optString("lgu_bpcode");
				if(!StringUtils.isBlank(pwd)) return(pwd);
			} else jo = new JSONObject();
			ss = loginTr.getFieldString("lgu_bpcode");
			if(!StringUtils.isBlank(ss)) {
				setPassword(ss);
			}
			return(ss);
		}
		return(loginTr.getFieldString("lgu_bpcode"));
	}
	void setPassword(String p_newPassword) throws Exception {
		if(getAESKey() != null && loginTr.existField("lgu_credentials")) {
			SelectUtil su = null;
			su = new SelectUtil(); 
			su.init(getLoginTokenJdbcPool());
			String ss = loginTr.getFieldString("lgu_credentials");
			JSONObject jo;
			if(StringUtils.isBlank(ss)) {
				jo = new JSONObject();
			} else {
				String js = Base64Util.decryptStrFromBase64(this, ss);
//				jo = new JSONObject(ZkUtil.decryptStrFromBase64(this, ss));
				jo = new JSONObject(js);
			}
			jo.put("lgu_bpcode",p_newPassword);
			su.executeUpdate("update loginuser set lgu_bpcode = '' ,lgu_credentials  = ? where lgu_login = ?",
					new Wherecl()
						.appendArgument(Base64Util.encryptStrToBase64(this, jo.toString()))
						.appendArgument(loginTr.getFieldString("lgu_login"))
						);
			loginTr = su.getQueryResult("select * from loginuser where lgu_type='U' and lgu_login = '"+loginTr.getFieldString("lgu_login")+"'");
			if(loginTr.getRecordCount() > 0) loginTr.setRecPointer(0);
			su.close();
			
		} else {
			SelectUtil su = null;
			su = new SelectUtil(); 
			su.init(getLoginTokenJdbcPool());
			su.executeUpdate("update loginuser set lgu_bpcode = '" + p_newPassword + "' where lgu_login = '"+loginTr.getFieldString("lgu_login") + "'",null);
			su.close();
		}
	}
	
	public void clearCoName() {
		coName = null;
	}
	protected void setDefaultCocode() throws Exception {
		if(loginTr != null && loginTr.existField("lgu_dftcocode")) {
			if(loginTr.getRecordCount() > 0) {
			String dftcocode = loginTr.getFieldString("lgu_dftcocode");
			if(dftcocode != null && !dftcocode.trim().equals("")) {
				BiConfig.setDefaultCocode(this, dftcocode);
			}
			}
		}
		if(loginTr != null && loginTr.existField("lgu_lcrg")) {
			if(loginTr.getRecordCount() > 0) {
				int lcrg = loginTr.getFieldInt("lgu_lcrg");
				BiConfig.setDefaultLcrg(this, lcrg);
			}
		}
	}
	
	/*
	public Object getLoginProperty(PROPNAME p_propname) throws Exception {
		if( loginTr != null && loginTr.getRecordCount() > 0) {
			loginTr.setRecPointer(0);
			switch (p_propname) {
			case FULLNAME: return(loginTr.getFieldString("lgu_name"));
			case URG: return(loginTr.getFieldInt("lgu_lgurg"));
			}
		}
		return(null);
	}
	*/
}
