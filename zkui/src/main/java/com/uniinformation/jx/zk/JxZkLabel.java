package com.uniinformation.jx.zk;

import com.uniinformation.jx.*;
import com.uniinformation.utils.*;
import com.uniinformation.rpccall.*;

import java.util.Vector;

import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.util.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.InputElement;

public class JxZkLabel extends JxZkElement {
	public JxZkLabel(JxZkSkin p_skin,int p_fdtypeid, Component c)	
	{
		super(p_skin,p_fdtypeid, c);
	}
	public void setText(String p_text)
	{
		((Label) comp).setValue(p_text);
	}	
	public String getText()
	{
		return(((Label) comp).getValue());
	}	
	@Override
	public Object getValue() {
		return(getText());
	}

	/*
	@Override
	public void setFontColor(int p_color)
	{
//		ZkUtil.setFontColor((HtmlBasedComponent) comp, String.format("#%06x", (p_color & 0xffffff)));
		((HtmlBasedComponent) comp).setStyle(String.format("color:#%06x;! important",(p_color & 0xffffff)));
//		ZkUtil.appendStyle( (HtmlBasedComponent) comp, String.format("color:#%06x;! important",(p_color & 0xffffff)));
	}
	*/
}
