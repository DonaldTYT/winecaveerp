package com.uniinformation.zkbi;

import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.XulElement;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class ZkBiPopupBase {
	Window popupScr = null;
	protected BiResult popupBr;
	JxZkBiBase popupJx = null;
	

	public ZkBiPopupBase (XulElement masterWin,SessionHelper sessionHelper, String p_viewName) throws Exception {
		//popupScr = ZkUtil.newPopupWindow("Test Popup",masterWin);
		popupScr = ZkUtil.newPopupWindow(" ",masterWin);
		popupScr.setWidth("100%");
		popupScr.setHeight("100%");
		popupScr.setMaximizable(true);
		popupScr.setSizable(true);
		popupScr.setContentStyle("overflow:auto;");
		BiSchema schema = (BiSchema) sessionHelper.getSessionData("biSchema");
		if(schema == null) schema = BiSchema.loadSchema(sessionHelper);
		BiView view = schema.getViewByName(p_viewName);
		UniLog.log("queryResult view:"+view);
		popupBr = view.newBiResult(sessionHelper.getLoginId(),null,null,sessionHelper);
	}
	
	protected void init( JxZkBiBaseCallback p_BibaseCallBack) {
		popupJx = JxZkBiBase.buildDetailWindow(popupBr, popupScr, false, true, p_BibaseCallBack);
	}
	
	protected void beforePopup(int p_mode,BiResult p_br) {
		
	}
	
    public void popUp(int p_mode,String p_condition) {
        popupBr.clearCondition();
        if(p_mode == JxZkBiBase.MODE_ADD) {
           popupBr.clearCurrentRec();
           popupJx.setAddAndClose(JxZkBiBase.CloseAction.Close);
           popupBr.clearLastUpdate();
           popupJx.setIsMobile(false);
           beforePopup(p_mode,popupBr);
		   popupJx.bindCellCollection(popupBr,p_mode);
		   popupJx.showForm();	
		   popupJx.doModalAdd();
        }
        if(p_mode == JxZkBiBase.MODE_UPDATE) {
        popupBr.addCustomCondition(p_condition);
        popupBr.query(true);
        if(popupBr.getRowCount() > 0 ) {
           popupJx.setUpdateAndClose(JxZkBiBase.CloseAction.Reload);
           popupBr.loadOneRecV(0);
           popupBr.fetchOneRecV(0);
           popupBr.clearLastUpdate();
           popupJx.setIsMobile(false);
           beforePopup(p_mode,popupBr);
		   popupJx.bindCellCollection(popupBr,p_mode);
		   popupJx.showForm();	
		   popupJx.doModalUpdate();
        } else {
           Messagebox.show(
        		"Fatal System Error : Reason Unknown. Code 3102",
        		popupBr.getSessionHelper().getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        }
        }
	}	
	
}
