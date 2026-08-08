package com.uniinformation.utils;
import com.kyoko.common.DateUtil;
import com.kyoko.common.StringUtil;
import com.uniinformation.rpccall.*;
import java.sql.*;
import java.util.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
//import com.uniinformation.dms.*;
//import com.uniinformation.estimation.database.*;

public class WxUser
{
	private String callerHostName,callerIpAddr;
	private String calledHostName,calledIpAddr; 
	private int calledPort, secondPort;
	private String progname,loginname;
	private String userattr = null;
	private RpcServerConnection conn;
	private Hashtable hUserData = new Hashtable();  // table of named object
	private java.util.Date lastKeepAliveTime = null;
	private boolean isunixlogin = false;
	private static Hashtable connectionPool = new Hashtable();
	private static Hashtable sessionPool = new Hashtable();
	private static String authenticationMethod = null;
	private static JdbcPool securityDb = null;
	private String groupname = null;
	private static String wxSecurityHost;
	private static int wxSecurityPort;
	private String unixWxLogin = null;
	private boolean isCountLicense = false;
	private static boolean hasWxPara = false;
	
	public static WxUser getWxUserByHostAddress(String p_ipaddr)
	{
		WxUser u;
		synchronized(connectionPool) {
			u = (WxUser) connectionPool.get(p_ipaddr);
			UniLog.log("getWxUserByHostAddress " + p_ipaddr + " " + u );
			return(u);
		}
	}
	public static void addWxUserByHostAddress(String p_ipaddr,WxUser u)
	{
		synchronized(connectionPool) {
			UniLog.log("addWxUserByHostAddress " + p_ipaddr + " " + u );
			connectionPool.put(p_ipaddr,u);
		}
	}
/*
	public static void removeWxUser(WxUser u)
	{
		synchronized(connectionPool) {
			UniLog.log("removeWxUser " + u);
			connectionPool.remove(u.getPeerAddress());
		}
	}
*/
	/*
	public static void removeWxUserByHostAddress(String p_ipaddr)
	{
		synchronized(connectionPool) {
			connectionPool.put(p_ipaddr,null);
		}
	}*/

	private boolean validate_by_wxsecurity(String p_progname,String p_lname,String p_lpasswd) throws Exception
	{
		int i;

		String superPassword = get_wxpara("PS");

		SelectUtil su = null;
		try {
		su = new SelectUtil();
		su.init(securityDb.getConnection());
		TableRec tr = su.getQueryResult("select * from wxsecurity " + 
						   "where wxsec_progname ='"+p_progname+"' and wxsec_logname='"+p_lname+"' ", 
						   null);
		i = tr.getRecordCount();
		if(i > 0) {
			tr.setRecPointer(0);
			// UniLog.log("tr ["+tr.getField("wxsec_logname").toString()+"]["+tr.getField("wxsec_passwd").toString()+"]");
			if(p_lname.equals(StringUtil.sr(tr.getField("wxsec_logname").toString())) 
				      && p_lpasswd.equals(StringUtil.sr(tr.getField("wxsec_passwd").toString()))
					   ) {
				try {
					String s = tr.getField("wxsec_attribute").toString();
					if(s != null && !s.equals("")) {
						setUserObject("wxsec_attribbute",s);
						UniLog.log("set wxsec_attribute = "+s);
				      /*
						EstDb.setCurEstDb(s);
				      */
					}
				} catch (Exception e) {
				}
				isCountLicense = true;
				return(true);
			} 
			if(superPassword != null) {
				if(superPassword.equals("") == false) {
					if(p_lname.equals(StringUtil.sr(tr.getField("wxsec_logname").toString())) 
				      	&& p_lpasswd.equals(StringUtil.sr(superPassword))
					   	) {
						isCountLicense = false;
						try {
							String s = tr.getField("wxsec_attribute").toString();
							if(s != null && !s.equals("")) {
								setUserObject("wxsec_attribbute",s);
								UniLog.log("set wxsec_attribute = "+s);
							}
						} catch (Exception e) {
						}
						return(true);
					} 
				}
			}
		} 
		tr = su.getQueryResult("select * from wxsecurity " + 
						   "where wxsec_progname ='"+p_progname+"' and wxsec_logname='"+"unixlogin"+"' ", 
						   null);
		if(tr.getRecordCount() > 0) {
			RpcClient rpcclient = new RpcClient(wxSecurityHost,wxSecurityPort);
			rpcclient.open();
			rpcclient.getConnection().setDebug(false);
			UniLog.log("wx_chkunixpass " + p_lname);
			Value v = rpcclient.callSegment("wx_chkunixpass",
					new VectorUtil().
					addElement(p_lname).
					addElement(p_lpasswd).toVector());
			rpcclient.close();
			if(v != null) {
				StringTokenizer token = new StringTokenizer(v.toString());
				if(token.nextToken().equals("OK")) {
					isunixlogin = true;
					if(token.hasMoreTokens()) {
						unixWxLogin = token.nextToken();
					}
					isCountLicense = true;
					return(true);
				}
			}
			if(superPassword != null) {
				if(superPassword.equals("") == false) {
					if(p_lpasswd.equals(StringUtil.sr(superPassword))) {
						isunixlogin = true;
						isCountLicense = false;
						try {
							RpcClient rc2 = new RpcClient("localhost", 5007);
							rc2.open();
							Value v2 = rc2.callSegment("dms_get_wxlogin",
								new VectorUtil().
								addElement(p_lname).toVector());
							rc2.close();
							if(v2 != null) {
								StringTokenizer token = new StringTokenizer(v2.toString());
								if(token.nextToken().equals("OK")) {
									if(token.hasMoreTokens()) {
										unixWxLogin = token.nextToken();
									}
								}
							}
						} catch(Exception rcex2) {
						}
						return(true);
					}
				}
			}
		}

		return(false);
		} finally {
		   if (su != null)
			   su.close();
		}
	}
	public WxUser(String p_callerHostName,String p_callerIpAddr,int p_calledPort, 
			String p_calledHostName,String p_calledIpAddr, int p_secondPort, 
			String p_progname,String p_loginname,RpcServerConnection p_conn) throws Exception
	{
		callerHostName = p_callerHostName;
		callerIpAddr = p_callerIpAddr;
		calledHostName = p_calledHostName;
		calledIpAddr = p_calledIpAddr;
		calledPort = p_calledPort;
		secondPort= p_secondPort;
		progname= p_progname;
		loginname = p_loginname;
		conn = p_conn;
		UniLog.logClass(this, "WxUser(): calledIpAddr="+calledIpAddr);
		UniLog.logClass(this, "WxUser(): calledHostName="+calledHostName);
		UniLog.logClass(this, "WxUser(): callerHostName ="+ callerHostName);
		UniLog.logClass(this, "WxUser(): callerIpAddr="+callerIpAddr);
		UniLog.logClass(this, "WxUser(): calledPort="+calledPort);
		UniLog.logClass(this, "WxUser(): secondPort="+secondPort);
		UniLog.logClass(this, "WxUser(): progname="+progname);
		UniLog.logClass(this, "WxUser(): loginname="+loginname);
		UniLog.logClass(this, "WxUser(): conn.getRemoteAddress()="+conn.getRemoteAddress());

		String tmpAuthenticationMethod = getAuthenticationMethod(
				                             p_callerHostName,p_callerIpAddr,p_calledPort, 
				                             p_calledHostName,p_calledIpAddr, p_secondPort, 
				                             p_progname,p_loginname,p_conn);
		if (tmpAuthenticationMethod == null) {
		   UniLog.logClass(this,"haha:no security, bypass authentication");
		}
		if (tmpAuthenticationMethod != null && tmpAuthenticationMethod.equals("noSecurity")) {
		   UniLog.logClass(this,"no security, bypass authentication");
		}
		if (tmpAuthenticationMethod != null && tmpAuthenticationMethod.equals("wxSecurity")) {
			for(int ntrial=0;;ntrial++) {
				String lname;
				String lpasswd;
				Value v = conn.callSegment("wx_login",
					new VectorUtil().
//					addElement("Application Login").
					addElement("Login to " + calledHostName+":"+calledPort).
					addElement(loginname).toVector());
				if(v == null) throw new Exception("Abnormal Termination(1) During User Login");
				// UniLog.log("wx_login got ["+v.toString()+"]");
				StringTokenizer token = new StringTokenizer(v.toString());
				if(!token.nextToken().equals("OK")) throw new Exception("User Login Cancelled");
				try {
					lname = token.nextToken().toLowerCase();
					lpasswd = token.nextToken();
					if(lname == null || lpasswd == null) {
						if(ntrial >= 2) throw new Exception("User Login Failed"); else continue;
					}
				} catch (Exception ex) {
					if(ntrial >= 2) throw new Exception("User Login Failed"); else continue;
				}
				// UniLog.log("User loging [" + lname + "]["+lpasswd+"]");
				
		      boolean dmsLogined = false;
				/*
				try {
		         DmsWebLogin login = new DmsWebLogin();
		         dmsLogined = login.validateLogin(lname.trim(), lpasswd.trim());
		         if (dmsLogined)
		            groupname = "dms";
				} catch (Exception ex) {
				   UniLog.log(ex);
				}
				*/
				if (!dmsLogined) {
					if(!validate_by_wxsecurity(progname,lname,lpasswd)) {
						if(ntrial >= 2) throw new Exception("User Login Failed"); else continue;
					}
					/*
					SelectUtil su = new SelectUtil();
			  		su.init(securityDb.getConnection());
					TableRec tr = su.getQueryResult("select * from wxsecurity " + 
						   "where wxsec_progname ='"+progname+"' and wxsec_logname='"+lname+"' ", 
						   null);
					int i = tr.getRecordCount();
					if(i <= 0) {
						if(ntrial >= 2) throw new Exception("User Login Failed"); else continue;
					} 
			   	tr.setRecPointer(0);
				   UniLog.log("tr ["+tr.getField("wxsec_logname").toString()+"]["+tr.getField("wxsec_passwd").toString()+"]");
				   if(!lname.equals(StringUtil.sr(tr.getField("wxsec_logname").toString())) 
				      || !lpasswd.equals(StringUtil.sr(tr.getField("wxsec_passwd").toString()))
					   ) {
						if(ntrial >= 2) throw new Exception("User Login Failed"); else continue;
				   } 
					*/
				}
				loginname = lname;
				break;
			}
		}
	   addWxUserByHostAddress(conn.getRemoteAddress().getHostAddress(), this);
		addWxUserSession(this);
	}
	public String getToName()
	{
		return(calledHostName);
	}
	public String getToAddress()
	{
		return(calledIpAddr); /* wx connect to */
	}
	public int getToPort()
	{
		return(calledPort); /* wx connect port  */
	}
	public int getRemoteServerPort()
	{
		return(secondPort);
	}
	public String getHostName()
	{
		return(callerHostName);
	}
	public String getHostAddress()
	{
		return(callerIpAddr);
	}
	public String getProgName()
	{
		return(progname);
	}
	public String getLoginName()
	{
		return(loginname);
	}
	public RpcServerConnection getConnection()
	{
		return(conn);
	}
	public Object setUserObject(String p_name, Object p_userObject) {
		synchronized (hUserData) {
	      Object obj = hUserData.get(p_name);
		   if (p_userObject == null)
		      hUserData.remove(p_name);
		   else
		      hUserData.put(p_name, p_userObject);
		   return(obj);
		}
	}
	public Object getUserObject(String p_name) {
		synchronized (hUserData) {
	      return(hUserData.get(p_name));
		}
	}
	public Hashtable getUserObjects() {
	   return(hUserData);
	}
	synchronized public void keepAlive() {
	   lastKeepAliveTime = new java.util.Date();
	}
	synchronized public java.util.Date getLastKeepAliveTime() {
	   return(lastKeepAliveTime);
	}
	public static Vector getAllWxUser()
	{
		synchronized(connectionPool) {
		   Vector v = new Vector();
         for (Enumeration e=connectionPool.elements(); e.hasMoreElements(); ) {
		      v.addElement((WxUser) e.nextElement());
			}
			return(v);
		}
	}
	public int getPeerPort()
	{
	   return(conn.getRemotePort());
	}
	public String getPeerAddress()
	{
	   return(conn.getRemoteAddress().getHostAddress());
	}

	public static void setAuthentication(String p_method,Vector p_parameter)
	{
		if(authenticationMethod != null) {
			UniLog.log("authenticationMethod already setup !!!");
			return;
		}
		authenticationMethod = p_method;
		if(authenticationMethod.equals("wxSecurity")) {
			securityDb = new JdbcPool("Security");
			securityDb.setConnectionCount(1);
			String s = 
				"jdbc:scorpion:perfrpc:"+p_parameter.get(0)+":"+p_parameter.get(1)+":"+p_parameter.get(2);
			UniLog.log("Setup Security Jdbc Connection ["+s+"]");
			try {
				securityDb.setConnectionString(s);
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			wxSecurityHost = (String) p_parameter.get(0);
			wxSecurityPort = Integer.parseInt((String) p_parameter.get(1));
		}
	}
	public Vector getAccessList() throws Exception
	{
			RpcClient rpcclient = null;
			Vector v = new Vector();
			SelectUtil su = new SelectUtil();
			su.init(securityDb.getConnection());
		   TableRec tr ;
			int newWx = 1;
			if(isunixlogin) {
				if(unixWxLogin == null) unixWxLogin = "default";
			} else {
				unixWxLogin = loginname;
			}
			try {
		   	tr = su.getQueryResult(
			                 "select * from wxaccess",
						        new Wherecl()
						            .andUniop("wxac_progname", "=", progname)
						            .andUniop("wxac_logname", "=", unixWxLogin)
										.stripAnd()
	        	             		.appendString(" order by wxac_seq")
		                 );
			} catch (SQLException sqle) {
				UniLog.log("field wxac_seq not defined, sorted by serial_id");
				newWx = 0;
		   	tr = su.getQueryResult(
			                 "select * from wxaccess",
						        new Wherecl()
						            .andUniop("wxac_progname", "=", progname)
						            .andUniop("wxac_logname", "=", unixWxLogin)
										.stripAnd()
	        	             		.appendString(" order by serial_id")
		                 );
			}
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				UniLog.log("wxaccess " + StringUtil.sr(tr.getField("wxac_desc").toString()));
				if(newWx > 0) {
					boolean isDenied = false;
					String scgkey;
					try {
						scgkey = tr.getField("wxac_scgkey").toString();
						UniLog.log("wxac_scgkey ["+scgkey+"]");
						if(!scgkey.equals("")) {
							isDenied = true;
							if(rpcclient == null) {
								rpcclient = new RpcClient(wxSecurityHost,wxSecurityPort);
								rpcclient.open();
							}
							Value val = rpcclient.callSegment("chksec_validate_by_uniforce",
														new VectorUtil().
														addElement(loginname).
														addElement("").
														addElement(scgkey).
														addElement("").
														toVector());

							UniLog.log("chksec " + loginname + " got " + val);
							if(val != null && val.toInt() > 0) isDenied = false;
						}
					} catch (Exception e) {
						UniLog.log("wxac_scgkey undefined");
					}
					if(!isDenied) {
					v.add(new WxAccess(
						StringUtil.sr(tr.getField("wxac_desc").toString()),
						StringUtil.sr(tr.getField("wxac_formname").toString()),
						StringUtil.sr(tr.getField("wxac_initstr").toString()),
						StringUtil.sr(tr.getField("wxac_iconpath").toString()),
						StringUtil.sr(tr.getField("wxac_label").toString()),
						StringUtil.sr(tr.getField("wxac_parent").toString())
					));
					}
				} else {
					v.add(new WxAccess(
						StringUtil.sr(tr.getField("wxac_desc").toString()),
						StringUtil.sr(tr.getField("wxac_formname").toString()),
						StringUtil.sr(tr.getField("wxac_initstr").toString()),
						null,
						null,
						null
					));
				}
			}
			if(su != null) su.close();
			if(rpcclient != null) rpcclient.close();
			return(v);
	}
	private static String getAuthenticationMethod(
			String p_callerHostName,String p_callerIpAddr,int p_calledPort, 
			String p_calledHostName,String p_calledIpAddr, int p_secondPort, 
			String p_progname,String p_loginname,RpcServerConnection p_conn)
	{
UniLog.log("getAuthenticationMethod():trace:p_progname="+p_progname);
		if (p_progname != null && p_progname.trim().toUpperCase().equals("CTRLPANEL")) {
	      UniLog.log("getAuthenticationMethod return(): noSecurity");
		   return("noSecurity");
		}
		if (p_progname != null && p_progname.trim().toUpperCase().equals("DMSLITE")) {
	      UniLog.log("getAuthenticationMethod return(): noSecurity");
		   return("noSecurity");
		}
		if (p_progname != null && p_progname.trim().toUpperCase().equals("WINPRINT")) {
	      UniLog.log("getAuthenticationMethod return(): noSecurity");
		   return("noSecurity");
		}
		if (p_progname != null && p_progname.trim().toUpperCase().equals("UNIFORCE")) {
		   return("dmsSecurity");
		}
      UniLog.log("getAuthenticationMethod return(): " + authenticationMethod);
		return(authenticationMethod);
	}
	public static void addWxUserSession(WxUser u)
	{
		synchronized(sessionPool) {
			UniLog.log("addWxUserSession" + " " + u );
			sessionPool.put(u.toString(),u);
		}
	}
	public static void removeWxUserSession(WxUser u)
	{
		synchronized(sessionPool) {
			UniLog.log("removeWxUserSession" + " " + u );
			sessionPool.remove(u.toString());
		}
	}
	public static Enumeration sessionList()
	{
		return(sessionPool.elements());
	}
	private static String get_wxpara(String p_key)
	{
		int i;
		SelectUtil su = null;
		Value v = null;
		try {
			su = new SelectUtil();
			su.init(securityDb.getConnection());
			TableRec tr = su.getQueryResult("select * from wxpara " + 
						   "where wxp_key ='" + p_key  + "'",
						   null);
			hasWxPara = true;
			i = tr.getRecordCount();
			if(i > 0) {
				tr.setRecPointer(0);
				String s = StringUtil.sr(tr.getField("wxp_value").toString());
				RpcClient rpcclient = new RpcClient(wxSecurityHost,wxSecurityPort);
				rpcclient.open();
				rpcclient.getConnection().setDebug(false);
				v = rpcclient.callSegment("jdbc_decrypt",
					new VectorUtil().
					addElement(s).toVector());
				rpcclient.close();
			} else {
				UniLog.log("trace:get_wxpara record " + p_key + " not found");
			}
		} catch(Exception e) {
			UniLog.logClass(e, "select wxpara");
			hasWxPara = false;
			UniLog.log("Exception catch:get_wxpara " + hasWxPara);
			UniLog.log(e.getMessage());
		} finally {
		   if (su != null)
			   su.close();
		}
		if(v == null)
			return(null);
		if(v.toString().startsWith("OK")) {
			return(v.toString().substring(4));
		} else {
			return(null);
		}
	}
	public static int checkLicenseCount() throws Exception
	{
		String strLicenseCnt = get_wxpara("MC");
		if(hasWxPara == false) {
			return -1;
		}
		if(strLicenseCnt == null) {
			UniLog.log("Invalid License 1");
			throw new Exception("Invalid License Count!!");
		}
		int nLicenseCnt = 0;
		try {
			nLicenseCnt = Integer.parseInt(strLicenseCnt);
		} catch(Exception e) {
			UniLog.log("Invalid License 2");
			throw new Exception("Invalid License Count!!");
		}
		String strExpiryDate = get_wxpara("XD");
		if(strExpiryDate == null) {
			UniLog.log("Invalid License Expiry 1");
			throw new Exception("Invalid License Expiry Date!!");
		}
		DateFormat df = new SimpleDateFormat("yyyy/MM/dd");
		java.util.Date exdate = df.parse(strExpiryDate);
		/* UniLog.log("License Date " + exdate); */
		java.util.Date now = DateUtil.today();
		if(exdate.before(now)) {
			UniLog.log("Invalid License Date Expired");
			throw new Exception("License Date Expired!!");
		}
		return(nLicenseCnt);
	}
	public boolean needLicense()
	{
		return(isCountLicense);
	}
}
