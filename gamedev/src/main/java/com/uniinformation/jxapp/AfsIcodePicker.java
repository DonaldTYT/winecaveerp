package com.uniinformation.jxapp;

import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.XulElement;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
//import com.uniinformation.estimation.database.EstDb;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.JxFormCloseListener;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jxapp.erpv4.IcodePicker;
import com.uniinformation.rpccall.RpcClient;
//import com.uniinformation.utils.AbstractGetItemProperty;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class AfsIcodePicker extends IcodePicker {
	@Override 
	public void afterBind() {
		super.afterBind();
		myViewName = "AfsStockCreator";
	}
}
