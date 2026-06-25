package com.kyoko.rpccall;

import java.io.*;
import java.net.*;
import java.util.*;

import com.kyoko.common.BufferedByteStream;
import com.kyoko.common.ByteStream;
import com.kyoko.common.SocketByteStream;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.CoreLog;

public class RpcClient 
{
  private int rpcserialnum = 0;
  public String hostName = null;
  public int portNumber;
  private RpcConnection conn;
  private RpcServletProvider service = null;
  private boolean debug = true;
  private int debugCnt = 2;
  private boolean finalized = false;
  private int socketConnectTimeout = 0; //0 infinite

  public boolean isConnected()
  {
    if(conn == null) return false;
    return true;
  }
  public RpcClient(String remotehost, int port) 
  {
    hostName = remotehost;
    portNumber = port;
    loadService();
  }
  public RpcClient(String remotehost, int port, int p_socketConnectTimeout)
  {
    hostName = remotehost;
    portNumber = port;
    socketConnectTimeout = p_socketConnectTimeout;
    loadService();
  }
  public RpcClient(String remotehost, int port, RpcServletProvider p_service) {
    hostName = remotehost;
    portNumber = port;
    setService(p_service);
  }
  private void loadService()
  {
    if(service != null) return;
    service = new RpcServletProvider();
    /*
    service.registerService("Uname",false,true);
    service.registerService("TestCallcount",false,true);
    service.registerService("TestClientSide",false,true);
    */
  }
  public void setService(RpcServletProvider p_service) {
     service = p_service;
  }
  public boolean setRpcServlet(String name,RpcServlet rpcservlet)
  {
    if (conn == null) {
       conn = new RpcConnection(service);
//       conn.setRpcClient(this);
		 conn.setDebug(debug);
		 conn.setDebugCnt(debugCnt);
    }
    return(conn.setServlet(name,rpcservlet));
  }
  public void removeRpcServlet(String name)
  {
    conn.removeServlet(name);
  }
  protected void finalize() throws Throwable
  {
    super.finalize();
    close();
    finalized = true;
  }
  
  public RpcClient open() throws RpcException
  {
	 if (finalized) {
	    throw(new RpcException("Finalized already"));
	 }
    try {
      if(conn == null) {
        conn= new RpcConnection(service);
        conn.setDebug(debug);
      }
      if (debug) {
		CoreLog.logClass(this, "open(): "+hostName+":"+portNumber);
      }
      Socket rpcSocket = null;
      rpcSocket = createSocket(hostName, portNumber);
      rpcSocket.setTcpNoDelay(true);
//	  conn.setByteStream(new BufferedByteStream(new SocketByteStream(rpcSocket),2048,8192));
	  conn.setByteStream(new SocketByteStream(rpcSocket));
//      rpcSocket.setSoTimeout(timeoutMSec);
	  if (debug) {
			CoreLog.logClass( this, "Rpc Connection Turn On Tcp_No_Delay " + rpcSocket.getSendBufferSize());
	  }
	  
	  
      return(this);
    } catch (UnknownHostException e) {
		throw(new RpcException("Don't know about host:" + hostName));
    } catch (IOException e) {
		throw(new RpcException("Couldn't get I/O for the connection to: " + hostName + ":" + portNumber));
    }
  }
  protected Socket createSocket(String p_host, int p_port) throws UnknownHostException, IOException {
      //return(new Socket(hostName, portNumber));
	  //andrew210601: add socketConnectTimeout
	  Socket socket = new Socket();
	  socket.connect(new InetSocketAddress(p_host, p_port), socketConnectTimeout);
	  return socket;
  }
  public void close()
  {
    if(conn == null) return;
    try {
    	//andrew230105 avoid exception
    	conn.stopService();
    }
    catch(Exception ex) {
    	CoreLog.log("error:" + ex.getMessage());
    }
    conn = null;
  }
  public Value callSegmentWithException(String segname) throws Exception {
    try {
      if (conn == null) open();
      return(conn.callSegment(segname,null,0));
    } catch (Exception e) {
    	//CoreLog.log("conn.isDebug()="+conn.isDebug());
		if (conn.isDebug())
		   CoreLog.log(e);
		throw(e);
    }
  }
  
  public Value callSegment(String segname) {
    try {
		return(callSegmentWithException(segname));
    } catch (Exception e) {
	   return(null);
    }
  }
  /***
   * callSegment simplified version.
   * @param parms
   * @return
   */
  public ReturnMsg callm(Object... parms){
	  if (parms == null || parms.length < 1 || !(parms[0] instanceof String)){
		  return(new ReturnMsg(false,"Invalid argument"));
	  }
	  try {
		  if(conn == null) open();
		  List<Object> argList = new ArrayList<Object>(Arrays.asList(parms));
		  argList.remove(0); //remove segname
		  Collections.enumeration(argList);
		  Value v = conn.callSegment((String)parms[0],Collections.enumeration(argList),argList.size());
		  if (v == null){
			  return(new ReturnMsg(false,"No return message"));
		  }
		  return(new ReturnMsg(v.toString().startsWith("OK") ? true : false , v.toString().trim()));
	  } catch(Exception e) {
		  return(new ReturnMsg(e));
	  }
  }
  /***
   * @param p_timeout
   */
  public void setTimeout(int p_timeout){
	  if (conn == null) open();
	  if (conn != null){
		  conn.setTimeOut(p_timeout);
	  }
  }
  public void setReadEncoding(String p_encoding)
  {
  	conn.setReadEncoding(p_encoding);
  }
  public String getReadEncoding()
  {
  	return(conn.getReadEncoding());
  }

  public void setWriteEncoding(String p_encoding)
  {
  	conn.setWriteEncoding(p_encoding);
  }
  public String getWriteEncoding()
  {
  	return(conn.getWriteEncoding());
  }
  public void setSimToTran(boolean p_flag) {
     conn.setSimToTran(p_flag);
  }
  public Value callSegmentWithException(String segname, Vector arglist) throws Exception {
    try {
      if(conn == null) open();
      return(conn.callSegment(segname,arglist.elements(),arglist.size()));
    } catch(Exception e) {
		if (conn.isDebug())
			CoreLog.log(e);
		throw(e);
    }
  }
  public Value callSegment(String segname, Vector arglist) {
    try {
       return(callSegmentWithException(segname, arglist));
    } catch(EOFException ex1) {
    	throw new RpcException(ex1);   //andrew191122: return a runtime exception instead of return null
    } catch(Exception e) {
      return(null);
    }
  }
  
  public int putFile(InputStream input,String filename)
  {
  	return(putFile(input,0,filename,0,0));
  }
  public int putFile(InputStream input,int maxlen,String filename,int offset,int maxrate)
  {
    try {
      if(conn == null) open();
      return(conn.putFile(input,maxlen,filename,offset,maxrate));
    } catch(Exception e) {
		if (conn.isDebug())
			CoreLog.log(e);
      return(-1);
    }
  }
  public int putFile(InputStream input,int maxlen,String filename,int offset,int maxrate,
  				int kbyte,String mode, String callbackname, Vector arglist)
  {
    try {
      if(conn == null) open();
      return(conn.putFileWithCallback(input,maxlen,filename,offset,maxrate,kbyte,mode,callbackname,arglist));
    } catch(Exception e) {
		if (conn.isDebug())
			CoreLog.log(e);
      return(-1);
    }
  }
  public int putFile(DataInput input,String filename)
  {
  	return(putFile(input,0,filename,0,0));
  }
  public int putFile(DataInput input,int maxlen,String filename,int offset,int maxrate)
  {
    try {
      if(conn == null) open();
      return(conn.putFile(input,maxlen,filename,offset,maxrate));
    } catch(Exception e) {
		if (conn.isDebug())
			CoreLog.log(e);
      return(-1);
    }
  }
  public int putFile(DataInput input,int maxlen,String filename,int offset,int maxrate,
  				int kbyte,String mode, String callbackname, Vector arglist)
  {
    try {
      if(conn == null) open();
      return(conn.putFileWithCallback(input,maxlen,filename,offset,maxrate,kbyte,mode,callbackname,arglist));
    } catch(Exception e) {
		if (conn.isDebug())
			CoreLog.log(e);
      return(-1);
    }
  }
  public int getFile(OutputStream stream,String filename)
  {
  	return(getFile(stream,0,filename,0,0));
  }
  public int getFile(OutputStream output,int maxlen,String filename,int offset,int maxrate)
  {
    try {
      if(conn == null) open();
      return(conn.getFile(output,maxlen,filename,offset,maxrate));
    } catch(Exception e) {
		if (conn.isDebug())
			CoreLog.log(e);
      return(-1);
    }
  }
  public int getFile(OutputStream output,int maxlen,String filename,int offset,int maxrate,
  				int kbyte,String mode, String callbackname, Vector arglist)
  {
    try {
      if(conn == null) open();
      return(conn.getFileWithCallback(output,maxlen,filename,offset,maxrate,
  				kbyte,mode, callbackname, arglist));
    } catch(Exception e) {
		if (conn.isDebug())
			CoreLog.log(e);
      return(-1);
    }
  }
  public int getFile(DataOutput output,String filename)
  {
  	return(getFile(output,0,filename,0,0));
  }
  public int getFile(DataOutput output,int maxlen,String filename,int offset,int maxrate)
  {
    try {
      if(conn == null) open();
      return(conn.getFile(output,maxlen,filename,offset,maxrate));
    } catch(Exception e) {
		if (conn.isDebug())
			CoreLog.log(e);
      return(-1);
    }
  }
  public int getFile(DataOutput output,int maxlen,String filename,int offset,int maxrate,
  				int kbyte,String mode, String callbackname, Vector arglist)
  {
    try {
      if(conn == null) open();
      return(conn.getFileWithCallback(output,maxlen,filename,offset,maxrate,
  				kbyte,mode, callbackname, arglist));
    } catch(Exception e) {
		if (conn.isDebug())
			CoreLog.log(e);
      return(-1);
    }
  }
  
  void afterExecute(RpcConnection p_conn) {
  }
  public void waitSegmentReturn(boolean b)
  {
    try {
    if(conn == null) open();
      conn.setReturnFlag(b);
    } catch(Exception e) {
		if (conn.isDebug())
         CoreLog.log(e);
    }
  }
  public void setDebug(boolean p_flag) {
	  debug = p_flag;
	  if (conn != null)
        conn.setDebug(p_flag);
  }
  public void setDebugCnt(int p_cnt) {
     debugCnt = p_cnt;
	  if (conn != null)
        conn.setDebugCnt(debugCnt);
  }
  public void setDefaultServletClassName(String p_classname)
  {
  		if(conn == null) return;
		conn.setDefaultServletClassName(p_classname);
  }
  public RpcConnection getConnection()
  {
  		return(conn);
  }
  protected void setConnection(RpcConnection p_conn)
  {
  		conn = p_conn;
  }
  public RpcClient()
  {
  }
  public int getFileStart(String filename)
  {
  	return(conn.getFileStart(0,filename,0,0));
  }
  
  public byte[] getFileBytes() throws RpcException,IOException
  {
  	return(conn.getFileBytes());
  }
  public int putFileStart(String filename)
  {
  	return(conn.putFileStart(0,filename,0,0));
  } 
  
  public int putFileBytes(byte[] putbuf,int ofs,int len)
  {
  	return(conn.putFileBytes(putbuf,ofs,len));
  }
  
  /*
  public static void main(String[] args) {
    int portno;
    if(args.length <3 ) {
      System.err.println("rpccall hostname portnum segname argument..");
      return;
    }
    portno = Integer.parseInt(args[1]);
    RpcClient rpcclient = new RpcClient(args[0] , portno);
    Vector arglist = new Vector();
    for(int i=3;i<args.length;i++) {
      arglist.addElement(args[i]);
    }
    rpcclient.open();
    Value val = rpcclient.callSegment(args[2],arglist);
    rpcclient.close();
    if(val != null) System.out.println(val.toString());
    else System.out.println("FAIL");
  }
  */
}
