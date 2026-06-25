package com.kyoko.rpccall;

public class Strval extends Value {
  private String str;
  private String encoding;

  public Strval(String s) {
    t = Valuable.ISSVALUE;
    str = new String(s);
  }
  public Strval(String s,int len) {
    t = Valuable.ISSVALUE;
    StringBuffer sbuf;
    sbuf = new StringBuffer(len);
    for(int i=0;i<len;i++)
      sbuf.append(s.charAt(i));
    str = new String(sbuf.toString());
  }
  public Strval(String s,String p_encoding) {
  	this(s);
	encoding = p_encoding;
  }
  public Strval(String s,int len,String p_encoding) {
  	this(s,len);
	encoding = p_encoding;
  }

  public String toString() { return(str); }
  public Value copy()      { return(new Strval(str));}
  public String getEncoding()
  {
  	return(encoding);
  }
}
