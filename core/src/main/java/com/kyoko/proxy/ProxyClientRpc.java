package com.kyoko.proxy;

import com.kyoko.common.CoreLog;
import com.kyoko.rpccall.RpcConnection;
import com.kyoko.rpccall.RpcServlet;

public class ProxyClientRpc implements RpcServlet{
	
	RpcConnection rpcconn;
	ProxyClient client;
	
	public ProxyClientRpc(ProxyClient p_client) {
		client = p_client;
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
		return "OK";
	}

	public String poll() {
		try {
			client.poll(rpcconn);
		} catch (Exception ex) {
			CoreLog.log(ex);
			return("FAIL");
		}
		return "OK";
	}
}
