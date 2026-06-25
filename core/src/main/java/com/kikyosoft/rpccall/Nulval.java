package com.kikyosoft.rpccall;

public class Nulval extends Value {

  short nulType;
  public Nulval(short vt) {
    t = Valuable.ISNULVALUE;
    nulType = vt;
  }
  public short getNulType()
  {
  	return(nulType);
  }

  public String toString() { return(null);}
  public Value copy()      { return(new Nulval(nulType));}
}
