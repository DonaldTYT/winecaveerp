package com.uniinformation.scorpion.driver;
import com.kyoko.common.CoreLog;
import com.uniinformation.rpccall.*;
import com.uniinformation.utils.*;
import java.sql.*;

import java.util.*;

public class ScpDriver implements java.sql.Driver {
   static ScpDriver scpDriver = loadDriver();
   public boolean acceptsURL(String p_url) {
		StringTokenizer stok;
		int ntok;
		CoreLog.log("in acceptURL");
		stok = new StringTokenizer(p_url,":");
		ntok = stok.countTokens();
		if(ntok < 5) return(false);
		stok.nextToken();
		if(! stok.nextToken().equals("scorpion") ) {
			return(false);
		}
	   return(true);
	}
	public Connection connect(String p_url, Properties p_properties) throws SQLException {
		ScpConnection scpconnection;
		RpcClient rpcclient;
		Vector arglist;
		Value v;
		String dbpath=null;
		String chaindir=null;
		String s;
		StringTokenizer stok;
		int ntok;
		String serverhost;
		int serverport;
		String dbname;
		CoreLog.log("in connect " + p_url);
		stok = new StringTokenizer(p_url,":");
		ntok = stok.countTokens();
		if(ntok < 5) return(null);
		stok.nextToken();
		if(! stok.nextToken().equals("scorpion") ) {
			return(null);
		}
		if(! stok.nextToken().equals("perfrpc") ) {
			return(null);
		}
		serverhost = stok.nextToken();
		serverport = Integer.parseInt(stok.nextToken());
		dbname = stok.nextToken();
		if(stok.hasMoreTokens()){
			if(stok.nextToken().equals("dbpath") ) {
				dbpath=stok.nextToken();
			}
		}
		if(stok.hasMoreTokens()){
			if(stok.nextToken().equals("chaindir") ) {
				chaindir=stok.nextToken();
			}
		}
		rpcclient = new RpcClient(serverhost,serverport);
		rpcclient.open();
		if(rpcclient.isConnected() != true) {
			CoreLog.log("RpcClient connection error");
			return(null);
		}
		if(dbpath != null) {
			
		}
		scpconnection = new ScpConnection(rpcclient);
		scpconnection.dbname = dbname;
CoreLog.logClass(scpconnection, "ScpDriver.java:conect():ScpConnection created");
		rpcclient.setDebug(false);
		rpcclient.getConnection().setNoAckFlag(true);
		rpcclient.setRpcServlet("com.uniinformation.scorpion.driver.ScpConnection",scpconnection);
		{
			arglist = new Vector();
			arglist.addElement("PERFUSER=");
			rpcclient.callSegment("jdbc_putenv",arglist);
		}
		if(dbpath != null) {
			arglist = new Vector();
			arglist.addElement("DBPATH="+dbpath);
			rpcclient.callSegment("jdbc_putenv",arglist);
		}
		if(chaindir != null) {
			arglist = new Vector();
			arglist.addElement(chaindir);
			rpcclient.callSegment("perfwx_setchaindir",arglist);
		}

		arglist = new Vector();
		arglist.addElement(new Integer(0));
		arglist.addElement(new Integer(1800));
		v = rpcclient.callSegment("rpccall_settimeout",arglist);

		arglist = new Vector();
		arglist.addElement("jdbc_connect");
		arglist.addElement("rpcclt00000");
		arglist.addElement("com.uniinformation.scorpion.driver.ScpConnection.rpcCallback");
		arglist.addElement(dbname);
		arglist.addElement("");
		arglist.addElement("");
		v = rpcclient.callSegment("callfunction",arglist);
		if(v != null) {
			scpconnection.setVersion(v.toInt());
			//CoreLog.log("rpccall jdbc_connect successful version " + scpconnection.getVersion());
			//CoreLog.log("useDelayClose()="+scpconnection.useDelayClose());
		} else {
			CoreLog.log("rpccall jdbc_connect got null");
			throw new ScpSQLException("Connection Error");
		}
		v = rpcclient.callSegment("callfunction", new VectorUtil()
		                                          .addElement("getpid")
		                                          .toVector());
		CoreLog.logClass(scpconnection, "connect(): getpid() return "+v);
		//CoreLog.log("getpid() return "+v);
		scpconnection.scpGetdbschema();
	   return(scpconnection);
	}
	public int getMajorVersion() {
		CoreLog.log("in getMajorVersion");
	   return(1);
	}
	public int getMinorVersion() {
		CoreLog.log("in getMinorVersion");
	   return(0);
	}
	public DriverPropertyInfo[] getPropertyInfo(String p_url, Properties p_properties) {
		CoreLog.log("in getPropertyInfo");
	   return(null);
	}
	public boolean jdbcCompliant() {
		CoreLog.log("in jdbcCompliant");
	   return(false);
	}
	private static ScpDriver loadDriver() {
		CoreLog.log("ScpDriver:loadDriver() version 3.00 ...");
	   ScpDriver driver = new ScpDriver();
		try {
	      DriverManager.registerDriver(driver);
		} catch (Exception ex) {
		   CoreLog.log(ex);
		   return(null);
		}
		CoreLog.log("ScpDriver:loadDriver() done");
		return(driver);
	}
	
	public java.util.logging.Logger getParentLogger()
	{
		return(null);
	}
	/*
	public static void main(String[] args) {
		CoreLog.log("helloworld");
	}
	*/
}

