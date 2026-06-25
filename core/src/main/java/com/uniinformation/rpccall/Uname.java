package com.uniinformation.rpccall;

import java.util.*;
import java.net.*;
import com.uniinformation.rpccall.*;
import com.uniinformation.utils.UniLog;

public class Uname implements RpcServlet
{
  public void setConnection(RpcServerConnection r)
  {
  }
  public void init_servlet()
  {
  }
  public void close_servlet()
  {
  }
  public String getServiceName()
  {
    return("uname");
  }
  public String execute()
  {
    try {
      InetAddress localhost =InetAddress.getLocalHost();
      return new String(localhost.getHostName());
    } catch(Exception e) {
      UniLog.log(e.getMessage());
    }
    return "";
  }
  public Value execute(Vector arglist)
  {
    try {
      InetAddress localhost =InetAddress.getLocalHost();
      return new Strval(localhost.getHostName());
    } catch(Exception e) {
      UniLog.log(e.getMessage());
    }
    return new Strval("");
  }
	public String ping()
	{
		return("OK  ");
	}
  static void main(String argv[])
  {
    try {
      InetAddress localhost =InetAddress.getLocalHost();
      System.out.println(localhost.getHostName());
    } catch(Exception e) {
      UniLog.log(e.getMessage());
    }
  }
}
