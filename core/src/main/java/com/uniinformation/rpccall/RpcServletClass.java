package com.uniinformation.rpccall;

import java.util.Vector;
import com.uniinformation.rpccall.*;

public class RpcServletClass implements RpcServlet
{
  protected RpcServerConnection rpcconn;
  public void init_servlet()
  {
  }
  public void close_servlet()
  {
  }
  public void setConnection(RpcServerConnection conn)
  {
    rpcconn = conn;
  }
  public String ping()
  {
  	return("OK  ");
  }
}
