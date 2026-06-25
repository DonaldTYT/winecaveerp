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

public class JxZkCheckbox extends JxZkInputElement {
	public JxZkCheckbox(JxZkSkin p_skin,int p_fdtypeid, Component c)	
	{
		super(p_skin,p_fdtypeid, c);
		comp.addEventListener("onCheck", zkEventListener);
		//UniLog.log("HAHA 2016 JxZKCheckbox created " + getName());
	}
}
