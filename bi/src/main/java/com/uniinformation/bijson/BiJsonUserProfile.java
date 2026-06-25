package com.uniinformation.bijson;
import com.google.gson.annotations.SerializedName;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
public class BiJsonUserProfile extends BiJsonBase {
	public String lang = null; //default label helper lang
	public String otpSecret = null;
	
	/*
	@Override
	public String buildFilingKey(String p_format, SessionHelper p_sh) {
		return super.buildFilingKey(p_format, p_sh) + "_haha";
	}
	*/
	
	public static void main(String args[]) throws Exception{
		
		//env setup
		System.setProperty("erpsetup.properties", "/tmp/erpsetup.properties");
		SessionHelper sh = ZkSessionHelper.getSessionHelperDummy("afsdev","hlv",null);
		//sh.setLoginId("hlv");

		//construct java object. init from db
		BiJsonUserProfile userProfile = new BiJsonUserProfile();
		userProfile.loadFromDB(sh, "zkbi_userprofile_%s", "user profile json", true);
		UniLog.log1("before:%s",userProfile);
		
		//change value
		userProfile.lang = "TCHN";
		//userProfile.otpSecret = "";
		UniLog.log1("after:%s",userProfile);
		
		//update to db
		userProfile.save();
		System.exit(1);
	}

}
