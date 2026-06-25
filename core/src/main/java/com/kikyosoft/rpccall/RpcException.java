package com.kikyosoft.rpccall;

import com.kikyosoft.rpccall.*;
import com.kikyosoft.utils.*;


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
