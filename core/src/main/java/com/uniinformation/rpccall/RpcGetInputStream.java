package com.uniinformation.rpccall;

import java.io.IOException;
import java.io.InputStream;
import com.uniinformation.utils.UniLog;


public class RpcGetInputStream extends InputStream {
	RpcClient rpcClient = null;
	int bidx = 0;
	byte[] rBytes = null;
	public RpcGetInputStream(String p_address , int p_port, String p_filename) throws RpcException {
		super();
		rpcClient = new RpcClient(p_address,p_port);
		rpcClient.open();
		int cc = rpcClient.getFileStart(p_filename);
		if( cc != 0) {
			rpcClient.close();
			UniLog.log("rpcclient getFileStart failed");
			throw new RpcException("Remote File Open Error");
		}
		UniLog.log("RpcGetInputFileStream initialized " + this + " rpc " + rpcClient);
	}
	
	public int read() 
	{
		if(rpcClient == null) {
			return(-1);
//			UniLog.log("Error: rpcClient is null where read called"+ this );
		}
		try {
			if(rBytes == null || bidx >= rBytes.length) {
				rBytes = rpcClient.getFileBytes();
				if(rBytes == null || rBytes.length <= 0) {
					UniLog.log("EOT reached close and free rpcClient "+ this );
					rpcClient.close();
					rpcClient=null;
					return(-1);
				}
				bidx = 0;
			}
			int cc;
			cc = (int) rBytes[bidx] & 0xff;
			bidx++;
			return(cc);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(-1);
		}
	}
	
	public void close() throws IOException
	{
		super.close();
		UniLog.log("RpcGetInputFileStream closing");
		if(rpcClient != null) {
			rpcClient.close();
			rpcClient = null;
		}
	}
	protected void finalize()  throws Throwable{
		super.finalize();
		UniLog.log("RpcGetInputFileStream finalization");
		if(rpcClient != null) {
			rpcClient.close();
			rpcClient = null;
		}
	}
}
