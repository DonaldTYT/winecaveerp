package com.kyoko.proxy;

import java.util.Hashtable;
import java.util.Vector;

import com.kyoko.common.StreamMultiplexer;
import com.kyoko.common.CoreLog;
import com.kyoko.rpccall.RpcConnection;
import com.kyoko.rpccall.RpcServlet;
import com.kyoko.rpccall.Value;

public class ProxyServerRpc implements RpcServlet{
	
	ProxyServer proxyServer;
	RpcConnection rpcconn;
	
	public ProxyServerRpc(ProxyServer p_server) {
		proxyServer = p_server;
	}

	@Override
	public void init_servlet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close_servlet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConnection(RpcConnection conn) {
		// TODO Auto-generated method stub
		rpcconn = conn;
	}

	@Override
	public String ping() {
		// TODO Auto-generated method stub
		return "OK";
	}
	
	public String startRedir(String p_fromPort, String p_toIp, String p_toPort) {
		int fromPort = Integer.parseInt(p_fromPort);
		int toPort = Integer.parseInt(p_toPort);
		return(startRedir(fromPort, p_toIp, toPort) );
	}
	public String startRedir(int p_fromPort, String p_toIp, int p_toPort) {
		try {
			proxyServer.addRedirect(p_fromPort,p_toIp,p_toPort);
		} catch (Exception ex) {
			CoreLog.log(ex);
			return("FAIL"+ex.toString());
		}
		return("OK");
	}
	public String startForward() {
		try {
			CoreLog.log("Start Forward ....");
			for(;;) {
				Value v = rpcconn.callSegment("com.kyoko.proxy.ProxyClientRpc.poll");
				if(v == null || !v.toString().startsWith("OK")) break;
				Thread.sleep(5000);
			}
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("OK");
	}
	public String addForward(String p_ip,int p_port,int p_channel) {
		try {
			proxyServer.addForward(((StreamMultiplexer.MpxStream) rpcconn.getByteStream()).getMpx(),p_channel,p_ip,p_port);
		} catch (Exception ex) {
			CoreLog.log(ex);
			return("FAIL"+ex.toString());
		}
		return("OK");
	}
	/*
	public String startForward(Vector params) {
		try {
			CoreLog.log("Start Forward ....");
			for(;;) {
				for(int i=0;i<params.size();i+=3) {
					proxyServer.addForward((String) params.get(i+1),(Integer) params.get(i+2) ,
							((StreamMultiplexer.MpxStream) rpcconn.getByteStream()).getMpx(),
							(Integer) params.get(i)
					);
				}
				Value v = rpcconn.callSegment("com.kyoko.proxy.ProxyClientRpc.ping");
				if(v == null || !v.toString().startsWith("OK")) break;
				Thread.sleep(5000);
			}
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
		return("OK");
	}
	*/
	public String enableMultiPlexer() {
		if(rpcconn == null) return("FAIL");
		rpcconn.multiplexWhenReturn();
		return("OK");
	}
}
