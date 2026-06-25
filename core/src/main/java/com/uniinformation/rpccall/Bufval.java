package com.uniinformation.rpccall;

public class Bufval extends Value {
  private byte []buffer;

  public Bufval(byte []b) {
    t = Valuable.ISBUFVALUE;
    buffer = b;
  }
  public byte[] getBuffer() { return(buffer); }

  public String toString() { return(buffer.toString()); }
  public Value copy()      { return(new Bufval(buffer));}
}
