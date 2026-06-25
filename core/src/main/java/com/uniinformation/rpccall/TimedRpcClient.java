package com.uniinformation.rpccall;

import java.io.EOFException;
import java.util.Date;
import java.util.Vector;

import com.uniinformation.utils.UniLog;

// Experimental , implementation incomplete, only the following method are trapped and function properly
    
// 2021/04/22 DT

public class TimedRpcClient extends RpcClient {
	int minReconnectInterval = 20000; // 10 seconds
	int maxInactiveTime = 20000; // 10 seconds
	long lastCallTime;
	long lastFailTime;
	String pingSeg;
	public TimedRpcClient(String remotehost, int port) {
		super(remotehost, port);
	}	
	public TimedRpcClient(String remotehost, int port, RpcServletProvider p_service) {
		super(remotehost, port, p_service);
	}	
	public void setPingSeg(String p_seg) {
		pingSeg = p_seg;
	}
	boolean checkAndOpen() {
		long now;
//		if(!super.isConnected()) {
//			super.open();
//			lastCallTime = new Date().getTime();
//		}
		
		now = new Date().getTime();
		if(now - lastCallTime > maxInactiveTime) {
			if(now - lastFailTime < minReconnectInterval) return(false);
			try {
				super.close();
				super.open();
				super.setTimeout(maxInactiveTime + 10000);
			} catch (RpcException rex) {
				UniLog.log(rex);
				lastFailTime = new Date().getTime();
				return(false);
			}
			if(pingSeg != null) {
				Value v = super.callSegment(pingSeg);
				if(v == null) {
					lastFailTime = new Date().getTime();
					return(false);
				}
			}
		}
		lastCallTime = new Date().getTime();
		return(true);
	}
	public Value callSegment(String segname) {
		if(!checkAndOpen()) return(null);
		return(super.callSegment(segname));
	}
	 public Value callSegment(String segname, Vector arglist) {
		if(!checkAndOpen()) return(null);
		return(super.callSegment(segname,arglist));
	 }
}
