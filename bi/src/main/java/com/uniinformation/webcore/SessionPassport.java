package com.uniinformation.webcore;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Hashtable;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.utils.UniLog;

//import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

import java.io.ObjectOutputStream;
import java.io.Serializable;

public class SessionPassport implements Serializable {
	private static final long serialVersionUID = 989898L; 
	int version;
	boolean oneTimeOnly;
	java.util.Date createOn;
	java.util.Date expireOn;
	String effectiveUser;
	String peerAllowed;
	String urlAllowd;
	String content;
	static Hashtable<String , SessionPassport> passportHash = new Hashtable<String , SessionPassport> ();

	static public SessionPassport getPassPort( String p_base64 ) {
		SessionPassport spp;
		synchronized(passportHash) {
			java.util.Date jd = new java.util.Date();
			spp = passportHash.get(p_base64);
			if(spp != null) {
				if(spp.expireOn != null && spp.expireOn.before(jd))  {
					passportHash.remove(spp);
					return(null);
				}
				if(spp.oneTimeOnly) {
					passportHash.remove(spp);
				}
				return(spp);
			}
		}
		return(null);
	}
	public String getLoginId() {
		return(effectiveUser);
	}
	public String getVcode() {
		return(effectiveUser);
	}
	
	static public String makePassport( String p_user,long p_timeToExpire,String p_peerAllowed,String p_urlAllowed,boolean p_OneTimeOnly,String p_content)  throws Exception{
		SessionPassport spp = new SessionPassport();
		if(!StringUtils.isBlank(p_user)) spp.effectiveUser = p_user;
		spp.createOn = new java.util.Date();
		if(p_timeToExpire > 0L) spp.expireOn = new java.util.Date((spp.createOn.getTime() + p_timeToExpire));
		if(!StringUtils.isBlank(p_peerAllowed)) spp.peerAllowed = p_peerAllowed;
		if(!StringUtils.isBlank(p_urlAllowed)) spp.urlAllowd = p_urlAllowed;
		spp.oneTimeOnly = p_OneTimeOnly;
		if(!StringUtils.isBlank(p_content)) spp.content = p_content; 
		for(spp.version = 0;spp.version < 1000;spp.version++ ) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(spp);
			oos.close();
			MessageDigest md = MessageDigest.getInstance("MD5"); 
			byte[] md5Hash = md.digest(bos.toByteArray());
			String b64 = Base64.getEncoder().encodeToString(md5Hash); 
			synchronized(passportHash) {
				if(passportHash.get(b64) == null) {
					passportHash.put(b64, spp);
					return(b64);
				}
			}
		}
		return(null);
	}

//	public static void main(String args[]){
//		try {
//			String pk = makePassPort( "hlv",0L,null,null,true,"DN=0");
//			UniLog.log("TestPassPort " + pk); 
//		} catch (Exception ex) {
//			UniLog.log(ex);
//		}
//	}
}
