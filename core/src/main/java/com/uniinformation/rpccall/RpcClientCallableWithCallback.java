package com.uniinformation.rpccall;

import java.io.*;
import java.net.*;
import java.util.*;
import com.uniinformation.rpccall.*;
import com.uniinformation.utils.UniLog;

public interface RpcClientCallableWithCallback extends RpcClientCallable
{
  public void addCallback(RpcServlet p_callback);
}
