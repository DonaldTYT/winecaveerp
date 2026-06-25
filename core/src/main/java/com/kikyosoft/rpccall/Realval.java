package com.kikyosoft.rpccall;

public class Realval extends Value{
  private double dbval;

  public Realval(double d) {
    t = Valuable.ISRVALUE;
    dbval = d;
  }
  public int toInt()       { return((int) dbval); }
  public double toDouble() { return(dbval);}
  public String toString() { return(String.valueOf(dbval)); }
  public Value copy()      { return(new Realval(dbval)); }
}
