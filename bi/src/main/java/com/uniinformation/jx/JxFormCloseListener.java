package com.uniinformation.jx;

public interface JxFormCloseListener
{
  public static final int caDefault = 0;
  public static final int caNone = 1;
  public static final int caHide = 2;
  public static final int caFree = 3;
  public static final int caMinimize = 4;
  public int formClose(JxForm p_form);  
}
