package com.uniinformation.wip;

import com.uniinformation.rpccall.RpcServerConnection;
import com.uniinformation.rpccall.RpcServlet;

public class WipJobRpc extends WipJob implements RpcServlet {

	private RpcServerConnection currconn;
	
	/* for RpcServlet Interface started */
	public void setConnection(RpcServerConnection p_conn) {
		currconn = p_conn;
	}
	public void init_servlet() {
	}
	public void close_servlet() {
	}
	/* for RpcServlet Interface ended */

	/* rpcservice started */
	public String ping() {
		return("OK  xxx "+getClass().getName());
	}
	
	public String openJdbcConnection() {
		return("OK");
	}
}
