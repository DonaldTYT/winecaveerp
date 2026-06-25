package com.kikyosoft.rpccall;

import java.util.*;

public interface RpcClientCallable
{
  public Value callSegmentWithException(String segname) throws Exception;
  public Value callSegmentWithException(String segname, Vector arglist) throws Exception;
  public Value callSegment(String segname);
  public Value callSegment(String segname, Vector arglist);
}
