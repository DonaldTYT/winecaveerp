package com.uniinformation.bijson;

import java.lang.reflect.Constructor;

/*
import org.apache.commons.beanutils.BeanUtils;
*/
import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
/***
 * A handly base class for create json data object using gson.
 * Feature:
 * - create json data object from db
 * - save to db
 * 
 * By default, it will exclude final, static, transient variable
 * 
 * TODO: add encryption feature
 * 
 * 
 * @author andrew
 *
 */
public class BiJsonBase {
	//remark: base class basic field, will not expose to json
	private transient String orgJsonStr = null; //for dirty checking
	private transient SessionHelper sh = null;
	private transient String baseFilingKey = null; 
	private transient String baseFilingKeyName = null;
	protected transient boolean allowSaveFlag = false;
	protected transient boolean loadFromDBFlag = false;
	
	/***
	 * default buildFilingKey, you can override it to customize the filing key
	 * 
	 * @param p_format
	 * @param p_sh
	 * @return
	 */
	protected String buildFilingKey(String p_format, SessionHelper p_sh) {
		if (StringUtils.isBlank(p_format)) {
			UniLog.log1("filing key is blank");
			return null;
		}
		//format: prefix(at least 10 char) + "_%s" 
		//e.g. format zkbi_userprofile_%s
		if (!p_format.matches("^[a-z_]{10,}_%s$")) { 
			UniLog.log1("invalid filing key format");
			return null;
		}
		if (p_sh == null || !p_sh.isLogin() || StringUtils.isBlank(p_sh.getLoginId())){
			UniLog.log1("not yet login");
			return null;
		}
		if (!p_sh.getLoginId().equals(p_sh.getVcode())) {
			return String.format(p_format, p_sh.getLoginId()+"_"+p_sh.getVcode());
		}
		return String.format(p_format, p_sh.getLoginId());
		
	}
	/***
	 * 
	 * @param p_sh - sessionHelper
	 * @param p_class - json data class, child class of BiJsonBase
	 * @param p_filingKeyFormat - e.g. zkbi_userprofile_%s
	 * @return
	 */
	public ReturnMsg loadFromDB(SessionHelper p_sh, String p_filingKeyFormat, String p_filingKeyName, boolean p_allowSave) {
		if (p_sh == null || !p_sh.isLogin() || StringUtils.isBlank(p_sh.getLoginId())){
			UniLog.log1("not yet login");
			return ReturnMsg.defaultFail;
		}
		
		try {

			//construct filingKey
			String filingKey = buildFilingKey(p_filingKeyFormat, p_sh);  
			if (StringUtils.isBlank(filingKey)) {
				UniLog.log1("invalid filing key");
				return ReturnMsg.defaultFail;
			}
			if (StringUtils.isBlank(p_filingKeyName)) {
				UniLog.log1("invalid filing key name");
				return ReturnMsg.defaultFail;
			}
		
			//load/create json
			JsonObject json = FilingUtil.getGson(p_sh.getAgent(), null, filingKey);
			if (json == null) { //if record not found, it will be blank
				UniLog.log1("no db record, create a default object");
				json = new JsonObject();
			}
			
			//construct object from json
			UniLog.log1("before: " + this);
			BiJsonBase profile = GsonUtil.convertToObject(json, this.getClass(), this);
			UniLog.log1("after: " + this);
			//BeanUtils.copyProperties(profile, this);

			//fill basic field
			
			UniLog.log1("this:%s profile:%s",this.hashCode(), profile.hashCode());
			profile.sh = p_sh;
			profile.baseFilingKey = filingKey;
			profile.baseFilingKeyName = p_filingKeyName;
			profile.orgJsonStr = toString();
			profile.allowSaveFlag = p_allowSave;
			profile.loadFromDBFlag = true;
			return ReturnMsg.defaultOk;
		}
		catch(Exception ex) {
			UniLog.log1("error " + ex.getMessage());
			return new ReturnMsg(ex);
		}
	}
	/***
	 * check any changes
	 * @return
	 */
	protected boolean isDirty() {
		UniLog.log1("before:" + orgJsonStr);
		UniLog.log1("after:" + toString());
		return (!StringUtils.equals(toString(),orgJsonStr));
	}
	
	/***
	 * check allow save
	 * allow override
	 * @return
	 */
	protected ReturnMsg checkAllowSave() {
		if (!allowSaveFlag) return new ReturnMsg(false,"allowSaveFlag is false");
		if (!loadFromDBFlag) return new ReturnMsg(false,"loadFromDBFlag is false");
		return ReturnMsg.defaultOk;
	}
	
	/***
	 * construct json string
	 */
	public String toString() {
		return GsonUtil.objToStr(this);
	}
	/***
	 * save to db
	 * skip update if no changes
	 * @return
	 */
	public ReturnMsg save(){
		if (sh == null || !sh.isLogin() || StringUtils.isBlank(sh.getLoginId())){
			UniLog.log1("not yet login.");
			return new ReturnMsg(false,"not yet login");
		}
		if (StringUtils.isBlank(baseFilingKey)) {
			UniLog.log1("filing key is blank");
			return new ReturnMsg(false,"filing key is blank");
		}
		ReturnMsg checkAllowResult = checkAllowSave();
		if (!checkAllowResult.getStatus()) {
			UniLog.log1("checkallowResult:"+ checkAllowResult.getMsg());
			return checkAllowResult;
		}
		if (!isDirty()) {
			UniLog.log1("no changes, skip save");
			return new ReturnMsg(true,"no changes");
		}
		JsonObject json = GsonUtil.objToJson(this);
		//UniLog.log1("jsonsize:"+ json.size());

		if (json.size() == 0) {
			UniLog.log1("profile is empty, skip save");
			return ReturnMsg.defaultOk;
		}

		if (!isDirty()) {
			UniLog.log1("profile no update, skip save");
			return ReturnMsg.defaultOk;
		}
		try {
			return FilingUtil.storeGson(sh.getAgent(), null, baseFilingKey, baseFilingKeyName, "", GsonUtil.objToJson(this));
		}
		catch(Exception ex) {
			return new ReturnMsg(ex);
		}
	}

	public static void main(String args[]) throws Exception{
		System.setProperty("erpsetup.properties", "/tmp/erpsetup.properties");
		SessionHelper sh = ZkSessionHelper.getSessionHelperDummy("afsdev","hlv",null);
		//sh.setLoginId("hlv");
		UniLog.log1("sh:%s",GsonUtil.objToJson(sh));
		BiJsonBase base = new BiJsonBase();
		ReturnMsg rtn = base.loadFromDB(sh, "zkbi_userprofile_%s", "user profile json", true);
		if (rtn.getStatus()) {
			UniLog.log1("profile:%s",GsonUtil.objToJson(base));
			base.save();
		}
		else {
			UniLog.log1("error");
		}
		System.exit(1);

	}

}
