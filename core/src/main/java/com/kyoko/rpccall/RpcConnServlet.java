package com.kyoko.rpccall;

class RpcConnServlet
{
  private RpcServlet r;
  private boolean needReturn ;
  public RpcConnServlet(RpcServlet serv,boolean  b)
  {
    r = serv;
    needReturn = b;
  }
  public boolean returnToProvider()
  {
    return needReturn;
  }
  public RpcServlet getServlet()
  {
    return r;
  }
}
