package com.kikyosoft.rpccall;

public class Value
{
  protected int t = 0;
  public int valType()     { return(t); }
  public String toString() { return(null); }
  public double toDouble() { return(0); }
  public int toInt()       { return(0); }
  public Value copy()      { return(null); }
  public static Value getValue(int p_value) {
     return(new Longval(p_value));
  }
  public static Value getValue(double p_value) {
     return(new Realval(p_value));
  }
  public static Value getValue(String p_value) {
     return(new Strval(p_value));
  }
  public static Value getValue(Object p_value) {
     if (p_value instanceof Integer)
	     return(getValue(((Integer) p_value).intValue()));
     else if (p_value instanceof Double)
	     return(getValue(((Double) p_value).doubleValue()));
     else if (p_value instanceof String)
	     return(getValue((String) p_value));
     else
	     return(null);
  }
  public static Value newValue(int i) {
	  return(new Longval(i));
  }
}
