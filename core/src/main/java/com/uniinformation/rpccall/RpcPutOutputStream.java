package com.uniinformation.rpccall;

import java.io.IOException;
import java.io.OutputStream;

import com.uniinformation.utils.UniLog;

public class RpcPutOutputStream extends OutputStream {
	static final int TBUFSIZE = 8192;
	RpcClient rpcClient = null;
	int bidx = 0;
	byte[] tBytes = null;
	public RpcPutOutputStream(String p_address , int p_port, String p_filename) throws RpcException {
		super();
		rpcClient = new RpcClient(p_address,p_port);
		rpcClient.open();
		int cc = rpcClient.putFileStart(p_filename);
		if( cc != 0) throw new RpcException("Remote File Createion Error");
		tBytes = new byte[TBUFSIZE];
	}

	public void write(int b) throws IOException
	{
		tBytes[bidx] = (byte) b;
		bidx++;
		if(bidx >= TBUFSIZE) {
			flush();
		}
	}
	
	public void flush() throws IOException
	{
		super.flush();
		if(bidx > 0) {
			rpcClient.putFileBytes(tBytes, 0, bidx);
			bidx = 0;
		}
	}
	
	public void close() throws IOException
	{
		if(rpcClient != null) {
			flush();
			rpcClient.putFileBytes(null, 0, 0);
			rpcClient.close();
			rpcClient = null;
		}
	}
}
