package com.uniinformation.rpccall;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.net.*;
import java.io.*;
import java.lang.reflect.*;

import com.uniinformation.rpccall.*;
import com.uniinformation.utils.UniLog;

public class RpcServer implements Runnable
{

  private int _portno=5007;
  private int _backlog=0;
  private InetAddress _bindaddr;
  private ServerSocket sock;
  // private Vector servlist = new Vector(1);
  // static private Hashtable service_table = new Hashtable();
  private RpcServletProvider service;
  RpcServerListener listener = null;
  private boolean finterrupted = false;
  private String defaultServletClass = null;
  private AtomicBoolean isRunningFlag = new AtomicBoolean(false);

  public void run()
  {
	  isRunningFlag.set(true);
	  UniLog.log("RpcServer run");
	  for(;;) {
		  try {
			  Socket incoming;
			  ServerSocket ssock1;
			  synchronized(this) {
				  if(sock == null) {
					  try {
						  init();
					  } catch (Exception ex2) {
						  UniLog.log(ex2);
						  UniLog.log("sleep for 60 seconds ...");
						  try {
							  Thread.currentThread().sleep(60000);
						  } catch (Exception ex3) {
							  UniLog.log(ex3);
						  }
					  }
				  }
				  ssock1 = sock;
			  }
			  UniLog.log("Wait for request");
			  ssock1.setSoTimeout(5000);
			  for (;;) {
				  try {
					  if (finterrupted) {
						  ssock1.close();
						  UniLog.log("Rpc Service stopped");
						  isRunningFlag.set(false);
						  return;
					  }
					  incoming = ssock1.accept();
					  break;
				  } catch (InterruptedIOException ex) {
					  if(Thread.currentThread().isInterrupted()) {
						  UniLog.log("RpcServer Intterrupted");
						  isRunningFlag.set(false);
						  return;
					  }
				  }
			  }
			  UniLog.log(Thread.currentThread() + "Accepted");
			  RpcServerConnection conn = new RpcServerConnection(service);
			  conn.setRpcServer(this);
			  conn.setSocket(incoming);
			  if(defaultServletClass != null)
				  conn.setDefaultServletClassName(defaultServletClass);
			  if (listener != null)
				  listener.afterAccept(this, conn);
			  Thread servthread = new Thread(conn);
			  UniLog.log("Connection established");
			  // conn.start();
			  servthread.start();
		  }
		  catch (Exception e) {
			  UniLog.log(e);
		  }
	  }
  }
  public boolean isRunning() {
	  return isRunningFlag.get();
  }
  public void setDefaultServletClassName(String classname)
  {
		defaultServletClass = classname;
  }

  public boolean setBindAddress(String bindaddr)
  {
    try {
      _bindaddr = InetAddress.getByName(bindaddr);
      return true;
    } catch(Exception e) {
	   UniLog.log(e);
      return false;
    }
  }
  public void init() throws java.io.IOException
  {
    UniLog.log("init()");
    if(_bindaddr != null) {
	  UniLog.log("new ServerSocket("+ _portno + "," +_backlog + "," +_bindaddr + ")");
      sock = createServerSocket(_portno,_backlog,_bindaddr);
    } else {
	  if(_backlog > 0) {
		  UniLog.log("new ServerSocket(" + _portno + "," + _backlog + ")");
      	sock = createServerSocket(_portno,_backlog);
	  } else {
		  UniLog.log("new ServerSocket(" + _portno + ")");
        sock = createServerSocket(_portno);
	  } 
	}
    UniLog.log("sock+" + sock.toString());
	 if (_portno == 0) 
	    _portno = sock.getLocalPort();
	 UniLog.log("init success");
  }
  protected ServerSocket createServerSocket(int p_portno, int p_backlog, InetAddress p_bindaddr) throws IOException{
      return(new ServerSocket(p_portno,p_backlog,p_bindaddr));
  }
  protected ServerSocket createServerSocket(int p_portno, int p_backlog) throws IOException{
      return(new ServerSocket(p_portno,p_backlog));
  }
  protected ServerSocket createServerSocket(int p_portno) throws IOException{
      return(new ServerSocket(p_portno));
  }
  
  public RpcServer(int portno)
  {
    _portno = portno;
  }
  public RpcServer(int portno ,int backlog)
  {
    _portno = portno;
    _backlog = backlog;
  }
  public RpcServer(int portno,int backlog,String bindAddr)
  {
    _portno = portno;
    _backlog = backlog;
    setBindAddress(bindAddr);
  }
  protected boolean registerService(String servicename)
  { 
    if(service == null) 
    	service = new RpcServletProvider();
    service.registerService(servicename,false,true);
    return true;
  }
  protected boolean registerService(String servicename,int maxspool)
  { 
    if(service == null) 
    	service = new RpcServletProvider();
    service.registerService(servicename,false,true,maxspool);
    return true;
  }
  public boolean registerService(String servicename, boolean isClonable, boolean isReflect)
  { 
    if(service == null) 
    	service = new RpcServletProvider();
    service.registerService(servicename,isClonable,isReflect);
    return true;
  }
  protected boolean registerService(String servicename, boolean isClonable, boolean isReflect, int maxspool)
  { 
    if(service == null) 
    	service = new RpcServletProvider();
    service.registerService(servicename,isClonable,isReflect,maxspool);
    return true;
  }
  public void loadService()
  {
    registerService("com.uniinformation.rpccall.Uname");

	/* please register service in extended classs */

    /*
    registerService("com.uniinformation.utils.SSLRedir");
    registerService("com.uniinformation.ecg.EcgUtil");
    registerService("com.uniinformation.jacob.JacobUtil");
    */
  }
  public void setService(RpcServletProvider p_service) {
     service = p_service;
  }
  public int getLocalPort() {
     return(_portno);
  }
  public void addListener(RpcServerListener p_listener) {
     listener = p_listener;
  }
  // the parameter is prepared for multiple listener
  public void removeListener(RpcServerListener p_listener) {
     listener = null;
  }
  /* close the connected socket and stop the running thread */
  public void close() {
  }
  /* close the listening socket and stop the accept thread */
  public void stop() {
     finterrupted = true;
  }
  /* after execute of a server connection */
  void afterExecute(RpcServerConnection p_conn) {
     if (listener != null)
        listener.afterExecute(this, p_conn);
  }
  /*
  public static void main(String args[])
  {
    int portno;

    if(args.length < 0) {
      System.err.println("RpcServer portno <backlog> <bindaddr>");
      UniLog.log("RpcServer portno <backlog> <bindaddr>");
      return;
    }
    portno = Integer.parseInt(args[0]);
    UniLog.log(Thread.currentThread() + "RpcServer start " + portno);
    RpcServer rpcserver = new RpcServer(portno);
    rpcserver.loadService();
    rpcserver.run();
  }
  public static void main(String args[]) {
     UniLog.log("Hello World!");
  }
  */
  /***
   * Rpccall server starter. Easier way to start a rpcserver
   * @param p_port
   * @param p_classList
   */
  public static void starter(int p_port, String...p_classList){
	  RpcServer rs = new RpcServer(p_port);
	  for(int i=0; i<p_classList.length; i++){
		  try{
			  UniLog.log("starter class: " + p_classList[i]);
			  Class.forName(p_classList[i]);
			  rs.loadService();
			  rs.registerService(p_classList[i]);
		  }
		  catch(ClassNotFoundException ex){
			  UniLog.log("starter class: " + p_classList[i] + " failed. " + ex.getMessage());
		  }
	  }
	  rs.run();
  }
}