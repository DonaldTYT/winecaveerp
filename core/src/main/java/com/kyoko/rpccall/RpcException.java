package com.kyoko.rpccall;

import com.kyoko.rpccall.*;
import com.kyoko.common.*;


public class RpcException extends RuntimeException
{
  public RpcException(String str)
  {
    super(str);
  }
  public RpcException(Exception ex){
	  super(ex);
  }
}
