package com.uniinformation.rpccall;

import com.uniinformation.rpccall.*;

public class Longval extends Value
{
  private int longval;

  public Longval(int l) {
    t = Valuable.ISIVALUE;
    longval = l;
  }
  public int toInt()       { return(longval); }
  public String toString() { return String.valueOf(longval); }
  public double toDouble() { return((double) longval); }
  public Value copy()      { return(new Longval(longval)); }
}
