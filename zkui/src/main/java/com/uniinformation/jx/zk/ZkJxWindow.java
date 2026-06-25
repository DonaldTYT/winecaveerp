package com.uniinformation.jx.zk;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zul.*;

import com.uniinformation.utils.*;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.jx.*;
import com.uniinformation.jx.zk.*;

public class ZkJxWindow extends org.zkoss.zul.Window {
	public void setTitle(String p_title) {
		super.setTitle(p_title);
		UniLog.log("HAHA 2016 in JxZkWindow setTitle");
	}
	
	@Override	
	public void onPageDetached(Page page) 
	{
		super.onPageDetached(page);
		UniLog.log("HAHA 2016 JxZkWindow detached from Page");
	}
	
	public void onPageAttached(Page newpage, Page oldpage)
	{
		super.onPageAttached(newpage, oldpage);
		UniLog.log("HAHA 2016 JxZkWindow attached Page");
	}
	
}
