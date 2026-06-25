package com.uniinformation.utils;

import com.uniinformation.rpccall.RpcServerConnection;

public class PrtdocJsonChnftrRpcServlet extends ChnftrRpcServlet {
	private Callback callback;

	public PrtdocJsonChnftrRpcServlet(RpcServerConnection connection, Callback cb) {
		super(connection);
		callback = cb;
	}

	public String notify(int docNum) {
		UniLog.log1("notify:%d, callback:%s", docNum, callback);
		if (callback != null)
			callback.notify(docNum);
		return "OK";
	}
	
	public interface Callback {
		void notify(int docNum);
	}
}
