package com.uniinformation.jx;

public interface JxEventListener
{
  public String postEvent(String p_skinname,int p_eventid,String p_focusname);
  public void syncValue(String p_skinname,String p_value);
}
