package com.kyoko.rpccall;

public interface RpcServlet
{
  final int ON_DEMAND = 1;
  public void init_servlet();
  public void close_servlet();
  // public String getServiceName();
  // public Value execute(Vector arglist);
  public void setConnection(RpcConnection conn);
  public String ping();
}
