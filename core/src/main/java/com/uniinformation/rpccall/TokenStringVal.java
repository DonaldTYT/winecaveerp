package com.uniinformation.rpccall;

import com.uniinformation.rpccall.*;

public class TokenStringVal extends Value
{
  int idx;
  String str;

  public TokenStringVal(String p_str, int p_idx) {
    t = Valuable.ISTSVALUE;
	 str = p_str;
	 idx = p_idx;
  }
  public int toInt()       { return(idx);}
  public String toString() { return(str);}
  public double toDouble() { return((double) idx);}
  public Value copy()      { return(new TokenStringVal(str,idx)); }
}
