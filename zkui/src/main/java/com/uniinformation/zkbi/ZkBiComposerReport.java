package com.uniinformation.zkbi;

import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.XulElement;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.UniLog;

public class ZkBiComposerReport extends ZkBiComposerBase {
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
    	if(hasAUDColumn == null) hasAUDColumn=false;
		super.doAfterCompose(comp);
   	}
	/*
	public boolean doBrowseItemSelected(Window p_win,Listitem p_item,BiResult p_result) {
		UniLog.log("ZkBiComposerReportcalled");
		return(true);
	}
	public boolean doUpdateOneRow(Window p_win,BiResult p_result) {
		UniLog.log("ZkBiComposerReportcalled");
		return(true);
	}
	@Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
	}
	*/
    
    @Override
    public boolean doBrowseItemSelected(XulElement p_win, BiResult p_result)
    {
    	UniLog.log("doBrowseItemSelected do nothing in zkreport");
    	return(true);
    }
}
