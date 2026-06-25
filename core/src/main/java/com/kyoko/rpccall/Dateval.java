package com.kyoko.rpccall;

public class Dateval extends Value
{
  int dateval;

  public Dateval(int d) {
    t = Valuable.ISDVALUE;
    dateval = d;
  }
  public Dateval(String datestr) {
  }
  public int toInt()       { return(dateval);}
  public String toString() { return String.valueOf(dateval);}
  public double toDouble() { return((double) dateval);}
  public Value copy()      { return(new Dateval(dateval)); }
}
