package com.uniinformation.zkf;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zul.Fileupload;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.Composer;

import com.google.gson.JsonObject;
import com.uniinformation.cell.CellCollection;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Messagebox;

import com.uniinformation.utils.GsonUtil;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiLogHelper;
import com.uniinformation.zkbi.ZkBiLogHelper.ETYPE;
import com.uniinformation.zkbi.ZkBiTranslateHelper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
public class ZkCellComposer extends ZkComposerBase implements Composer<Component> {
	protected EventListener onClickListener;
	protected ZkForm zkf;
	
	protected void processActionByComposer(String p_eventName,Component p_target,boolean p_needResponse,InputStream p_upload)  throws Exception {
		
	}
	
	class ActionFormListener implements EventListener
	{
		void processAction(String p_eventName,Component p_target,boolean p_needResponse,InputStream p_upload) throws Exception {
			  String actionHandler = (String) p_target.getAttribute("actionHandler");
			  if(actionHandler != null) {
			    	Class cl = Class.forName(actionHandler);
			    	Constructor constructor = cl.getConstructor((Class[]) null);
			    	ZkfAction zkfa = (ZkfAction) constructor.newInstance();
			    	String actionId = (String) p_target.getAttribute("actionId");
			    	if(actionId == null) actionId = p_target.getId();
			    	String actionData = (String) p_target.getAttribute("actionData");
			    	JsonObject actionDataJson = GsonUtil.createJsonObject(actionData);
			    	ReturnMsg msg = zkfa.processAction(actionId, sessionHelper,formCollection,actionDataJson,p_upload,p_target);
			    	if(p_needResponse) {
			    		if(msg.getStatus()) {
			    			ZkUtil.msg("Request OK " + msg.getMsg());
			    			//Messagebox.show("Request OK " + msg.getMsg());
			    		} else {
			    			ZkUtil.errMsg("Failed " + msg.getMsg());
			    			//Messagebox.show("Failed " + msg.getMsg());
			    		}
			    	}
			  } else {
				  processActionByComposer(p_eventName,p_target,p_needResponse,p_upload) ;
			  }
		}
		
		@Override
		public void onEvent(final Event p_event) throws Exception {
			String confirmMsg = (String) p_event.getTarget().getAttribute("confirmMsg");
			String uploadFileType = (String) p_event.getTarget().getAttribute("uploadType");
			String strNeedResponse = (String) p_event.getTarget().getAttribute("needResponse");
			if(uploadFileType != null) {
//    		    Fileupload.get(new ZkBiEventListener <UploadEvent>(){
				final boolean needResponse;
				if(strNeedResponse == null) needResponse = true; else {
					needResponse = strNeedResponse.equals("Y");
				}
				HashMap<String,Object> params = new HashMap<String,Object>();
    		    Fileupload.get(
    		    	params,
    		    	null,
    		    	null,
    		    	-1,-1,true,
    		    	new ZkBiEventListener <UploadEvent>(){
    		    		@Override
						public void onZkBiEvent(UploadEvent event) throws Exception {
    		    			// TODO Auto-generated method stub
						    org.zkoss.util.media.Media media = event.getMedia();
						    if(media != null) {
						    	try  {
						    		InputStream is = media.getStreamData();
						    		processAction(p_event.getName(),p_event.getTarget(),needResponse,is);
						    		is.close();
						    	} catch (Exception ex) {
						    		UniLog.log(ex);
						    		throw(ex);
						    	}
						    }
						
    		    		}
    		    	}
    		    );
				
			} else if(confirmMsg != null) {
				final boolean needResponse;
				if(strNeedResponse == null) needResponse = true; else {
					needResponse = strNeedResponse.equals("Y");
				}
			    Messagebox.show(confirmMsg, "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
			    	     new EventListener() {
			    	       public void onEvent(Event evt) throws Exception {
			    	    	   if (((Integer)evt.getData()) == Messagebox.YES){
			    	    		   processAction(p_event.getName(),p_event.getTarget(),needResponse,null);
//			    	    		   onOkPressed(sessionHelper,ZkCellActionForm.this);
			    	    	   } else{
			    	    	   }
			    	      }
			    	    }
			    );
			} else {
				final boolean needResponse;
				if(strNeedResponse == null) needResponse = false; else {
					needResponse = strNeedResponse.equals("Y");
				}
			    processAction(p_event.getName(),p_event.getTarget(),needResponse,null);
			}
		}
	};
	
	
	protected void beforeMapCollection() {
		
	}
	protected CellCollection formCollection;
	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		if (!accessOkFlag) {
			return;
		}
		UniLog.log("after compose");
		
   			
		String layoutForm = Executions.getCurrent().getParameter("zkLayout");
		if(layoutForm != null) {
			Executions.getCurrent().createComponents(layoutForm, p_comp,null);
		}
		beforeMapCollection();
		
		if(formCollection == null) formCollection = new CellCollection();
		zkf = new ZkForm(p_comp,null);
		zkf.mapCellCollection(formCollection,onClickListener);
		
		//process translate
		ZkUtil.translateAllComp(sessionHelper, Executions.getCurrent().getParameter("page_id") , p_comp);
		
	}
	@Override
	protected boolean adjustRootCompWidth() {
		return false;
	}
}
