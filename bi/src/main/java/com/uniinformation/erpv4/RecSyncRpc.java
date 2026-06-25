package com.uniinformation.erpv4;

import java.util.ArrayList;

import com.uniinformation.rpccall.RpcServer;
import com.uniinformation.rpccall.RpcServerConnection;
import com.uniinformation.rpccall.RpcServlet;
import com.uniinformation.utils.UniLog;

public class RecSyncRpc extends RpcServer {
	ArrayList<String> rpcClassList;
	RecSyncRpc(int p_port) {
		super(p_port);
		rpcClassList = new ArrayList<String>();
		registerService("com.uniinformation.erpv4.RecSyncRpcServlet", false, true);
		for(String p_class : rpcClassList) {
			registerService("com.uniinformation.erpv4.RecSyncRpcServlet", false, true);
		}
	}
	
	public void addRpcClass(String p_class) {
//		rpcClassList.add(p_class);
		registerService(p_class, false, true);
	}
}
