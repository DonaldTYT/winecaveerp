package com.uniinformation.jx;

import com.uniinformation.bicore.BiActionListener;

public interface JxActionListener extends BiActionListener<JxField>
{
  @Override
  public void actionPerformed(JxField field);  
}
