package com.uniinformation.rpccall;

import com.uniinformation.rpccall.*;

public class TokenVal extends Value
{
  int idx;

  public TokenVal(int d) {
    t = Valuable.ISTVALUE;
    idx = d;
  }
  public int toInt()       { return(idx);}
  public String toString() { return String.valueOf(idx);}
  public double toDouble() { return((double) idx);}
  public Value copy()      { return(new TokenVal(idx)); }
}
