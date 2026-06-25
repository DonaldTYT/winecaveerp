package com.uniinformation.rpccall;

import java.io.*;
import java.net.*;
import java.util.*;
import com.uniinformation.rpccall.*;
import com.uniinformation.utils.UniLog;

public class RpcListenClient extends RpcClient implements Runnable
{
	static final String NOTCONNECTED = "Client is not connected";
	private ServerSocket sock = null;
	int portNumber =0;
	RpcListenClientListener listener = null;
	boolean isAccepted = false;
	boolean stopAccept = false;
	Thread connThread = null;
	String rpcServletName = null;
	RpcServlet rpcServlet = null;

	public RpcListenClient(int p_port)
	{
		super();
		try {
			sock =  new ServerSocket(p_port);
		} catch(IOException oe) {
			printError("new RpcListenClient " + p_port + " fail." , oe);
		}
		portNumber = sock.getLocalPort();
	}
	private void printError(String prefix, Exception e)
	{
		UniLog.log(prefix +  " "  + e.getMessage());
		e.printStackTrace();
	}
	public int getPortNumber()
	{
		return(portNumber);
	}
	public boolean setRpcServlet(String p_name,RpcServlet p_rpcservlet)
	{
		synchronized(this) {
			rpcServletName = p_name;
			rpcServlet = p_rpcservlet;
			if(getConnection() != null) {
				getConnection().setServlet(rpcServletName, rpcServlet);
			}
		}
		return(true);
	}
	protected void finalize() throws Throwable
	{
		close();
		super.finalize();
	}
	public RpcClient open() throws RpcException
	{
		/* override super */
		return(this);
	}
	public void close()
	{
		UniLog.log("RpcListenClient close");
		stopAccept = true;
		synchronized(sock) {
			if(sock != null) {
				try {
					sock.close();
				} catch(Exception e) {
				}
				sock = null;
			} 
		}
		super.close();
	}
	public Value callSegmentWithException(String segname) throws Exception {
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		return(super.callSegmentWithException(segname));
	}
	public Value callSegment(String segname) {
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		return(super.callSegment(segname));
	}
	public void setReadEncoding(String p_encoding)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
  		super.setReadEncoding(p_encoding);
	}
	public String getReadEncoding()
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
  		return(super.getReadEncoding());
	}
	public void setWriteEncoding(String p_encoding)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
  		super.setWriteEncoding(p_encoding);
	}
	public String getWriteEncoding()
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		return(super.getWriteEncoding());
	}
	public Value callSegmentWithException(String segname, Vector arglist) throws Exception {
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		return(super.callSegmentWithException(segname, arglist));
	}
	public Value callSegment(String segname, Vector arglist) {
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		return(super.callSegment(segname, arglist));
	}
  
	public int putFile(InputStream input,String filename)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
  		return(super.putFile(input,filename));
	}
	public int putFile(InputStream input,int maxlen,String filename,int offset,int maxrate)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
  		return(super.putFile(input,maxlen,filename,offset,maxrate));
	}
	public int putFile(InputStream input,int maxlen,String filename,int offset,int maxrate,
  				int kbyte,String mode, String callbackname, Vector arglist)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		return(super.putFile(input,maxlen,filename,offset,maxrate,kbyte,mode, callbackname, arglist));
	}
	public int putFile(DataInput input,String filename)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		return(super.putFile(input,filename));
	}
	public int putFile(DataInput input,int maxlen,String filename,int offset,int maxrate)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		return(super.putFile(input,maxlen,filename,offset,maxrate));
	}
	public int putFile(DataInput input,int maxlen,String filename,int offset,int maxrate,
  				int kbyte,String mode, String callbackname, Vector arglist)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		return(super.putFile(input,maxlen,filename,offset,maxrate,
  				kbyte,mode, callbackname, arglist));
	}
	public int getFile(OutputStream stream,String filename)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		return(super.getFile(stream,filename));
	}
	public int getFile(OutputStream output,int maxlen,String filename,int offset,int maxrate)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		return(super.getFile(output,maxlen,filename,offset,maxrate));
	}
	public int getFile(OutputStream output,int maxlen,String filename,int offset,int maxrate,
  				int kbyte,String mode, String callbackname, Vector arglist)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		return(super.getFile(output,maxlen,filename,offset,maxrate,
  				kbyte,mode, callbackname, arglist));
	}
	public int getFile(DataOutput output,String filename)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		return(super.getFile(output,filename));
	}
	public int getFile(DataOutput output,int maxlen,String filename,int offset,int maxrate)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		return(super.getFile(output,maxlen,filename,offset,maxrate));
	}
	public int getFile(DataOutput output,int maxlen,String filename,int offset,int maxrate,
  				int kbyte,String mode, String callbackname, Vector arglist)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		return(super.getFile(output,maxlen,filename,offset,maxrate,
  				kbyte,mode, callbackname, arglist));
	}
  
	void afterExecute(RpcServerConnection p_conn) 
	{
		UniLog.log(this + "after Execute");
		/*
		UniLog.log(this + "RpcListenClient DisConnected.");
		setConnection(null);
		*/
	}

	public void waitSegmentReturn(boolean b)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		super.waitSegmentReturn(b);
	}
	public void setDebug(boolean p_flag)
	{
		super.setDebug(p_flag);
	}
	public void setDebugCnt(int p_cnt)
	{
		super.setDebugCnt(p_cnt);
	}
	public void setDefaultServletClassName(String p_classname)
	{
		if(!isConnected())
			throw new RpcException(NOTCONNECTED);
		super.setDefaultServletClassName(p_classname);
	}
	public void setListener(RpcListenClientListener p_listener)
	{
		listener = p_listener;
	}
	public void run()
	{
		Socket incoming = null;
		for(;;) {
			try {
				synchronized(sock) {
					if(stopAccept) {
						UniLog.log(this + " stop accept");
						sock.close();
						sock = null;
						break;
					}
					sock.setSoTimeout(5000);
					incoming = sock.accept();
					break;
				}
			} catch (InterruptedIOException ignore) {
				stopAccept = true; /* will loop infinitely if just ignored */
			} catch (Exception ex) {
				printError("RpcListenClient accept fail:", ex);
				break;
			}
		}
		if(incoming != null) {
			RpcServerConnection conn = new RpcServerConnection((RpcServletProvider) null);
			conn.setRpcClient(this);
			conn.setSocket(incoming);
			conn.setDebug(true);
			setConnection(conn);
			synchronized(this) {
				if(rpcServletName != null) {
					conn.setServlet(rpcServletName, rpcServlet);
				}
			}
			if(listener != null)
				listener.afterAccept(this, conn);
			isAccepted = true;
		}
		UniLog.log("RpcListenClient " + Thread.currentThread() + " thread gone." + 
				"Socket is " + (isAccepted ? "" : "not") + " accepted");
	}
}
