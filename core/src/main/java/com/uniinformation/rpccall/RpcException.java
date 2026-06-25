package com.uniinformation.rpccall;

import java.lang.*;
import com.uniinformation.rpccall.*;

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
