package com.uniinformation.rpccall;

import java.io.*;
import java.net.*;
import java.util.*;
import com.uniinformation.rpccall.*;
import com.uniinformation.utils.UniLog;

public interface RpcClientCallable
{
  public Value callSegmentWithException(String segname) throws Exception;
  public Value callSegmentWithException(String segname, Vector arglist) throws Exception;
  public Value callSegment(String segname);
  public Value callSegment(String segname, Vector arglist);
}
