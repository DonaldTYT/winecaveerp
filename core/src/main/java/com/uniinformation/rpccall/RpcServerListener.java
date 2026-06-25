package com.uniinformation.rpccall;

import java.util.*;
import com.uniinformation.rpccall.*;

public interface RpcServerListener
{
   public void afterAccept(RpcServer p_rpcServer,	RpcServerConnection p_connection);
   public void afterExecute(RpcServer p_rpcServer,	RpcServerConnection p_connection);
}
