package com.uniinformation.zkf;

import java.io.InputStream;
import java.lang.reflect.Constructor;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.google.gson.JsonObject;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.utils.DynamicClassLoader;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiLogHelper;
import com.uniinformation.zkbi.ZkBiLogHelper.ETYPE;
import com.uniinformation.zkf.ZkCellComposer;

public class ZkCellActionForm extends ZkCellComposer {
	ZkCellActionInterface actionInterface;
//	@Wire
//	Button btOK;
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		String cellActionInterfaceName = (String) arg0.getAttribute("actionInterface");
		if(!StringUtils.isBlank(cellActionInterfaceName)) {
			Class[]	paramTypes = new Class[]{};
			actionInterface = (ZkCellActionInterface) DynamicClassLoader.newInstance(cellActionInterfaceName, paramTypes);
			actionInterface.init(arg0);
		}
				
//		Selectors.wireComponents(arg0, this, false);  //important for wire variable
//		onClickListener = new EventListener(){
//			void processAction(Component p_target,boolean p_needResponse) throws Exception {
//				  String actionHandler = (String) p_target.getAttribute("actionHandler");
//				  if(actionHandler != null) {
//				    	Class cl = Class.forName(actionHandler);
//				    	Constructor constructor = cl.getConstructor((Class[]) null);
//				    	ZkfAction zkfa = (ZkfAction) constructor.newInstance();
//				    	String actionId = (String) p_target.getAttribute("actionId");
//				    	if(actionId == null) actionId = p_target.getId();
//				    	String actionData = (String) p_target.getAttribute("actionData");
//				    	JsonObject actionDataJson = GsonUtil.createJsonObject(actionData);
//				    	ReturnMsg msg = zkfa.processAction(actionId, sessionHelper,formCollection,actionDataJson);
//				    	if(p_needResponse) {
//				    		if(msg.getStatus()) {
//				    			Messagebox.show("Request OK " + msg.getMsg());
//				    		} else {
//				    			Messagebox.show("Failed " + msg.getMsg());
//				    		}
//				    	}
//				  }
//			}
//			@Override
//			public void onEvent(final Event p_event) throws Exception {
//				String confirmMsg = (String) p_event.getTarget().getAttribute("confirmMsg");
//				if(confirmMsg != null) {
//				    Messagebox.show(confirmMsg, "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
//				    	     new EventListener() {
//				    	       public void onEvent(Event evt) throws Exception {
//				    	    	   if (((Integer)evt.getData()) == Messagebox.YES){
//				    	    		   processAction(p_event.getTarget(),true);
////				    	    		   onOkPressed(sessionHelper,ZkCellActionForm.this);
//				    	    	   } else{
//				    	    	   }
//				    	      }
//				    	    }
//				    );
//				} else {
//				    processAction(p_event.getTarget(),false);
//				}
//			}
//		};
		if(onClickListener == null) onClickListener = new ActionFormListener();
		super.doAfterCompose(arg0);
//		if (btOK != null){
//			btOK.addEventListener(Events.ON_CLICK, 
//				new EventListener(){
//				@Override
//				public void onEvent(Event p_event) throws Exception {
//				    Messagebox.show("Confirm " +  "?", "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
//				    	     new EventListener() {
//				    	       public void onEvent(Event evt) {
//				    	    	   if (((Integer)evt.getData()) == Messagebox.YES){
//				    	    		   UniLog.log("Do print statement");
//				    	    		   String actionHandler = (String) btOK.getAttribute("actionHandler");
//				    	    		   try {
//				    	    			   Class cl = Class.forName(actionHandler);
//				    	    			   Constructor constructor = cl.getConstructor((Class[]) null);
//				    	    			   ZkfAction zkfa = (ZkfAction) constructor.newInstance();
//				    	    			   String actionId = (String) btOK.getAttribute("actionId");
//				    	    			   if(actionId == null) actionId = btOK.getId();
//				    	    			   zkfa.processAction(actionId, sessionHelper,ZkCellActionForm.this);
//				    	    		   } catch(Exception ex) {
//				    	    			   UniLog.log(ex);
//				    	    		   }
////				    	    		   onOkPressed(sessionHelper,ZkCellActionForm.this);
//				    	    	   }
//				    	    	   else{
//				    	    	   }
//				    	      }
//				    	    }
//				    );
//				}
//				}
//			);
//		}
		if(actionInterface != null) {
			actionInterface.afterCompose(formCollection);
		}
	}
	@Override 
	protected void beforeMapCollection() {
		super.beforeMapCollection();
		if(actionInterface != null) {
			actionInterface.beforeMapCollection(sessionHelper);
		}
	}	
	
	@Override 
	protected void processActionByComposer(String p_eventName,Component p_target,boolean p_needResponse,InputStream p_upload)  throws Exception {
		if(actionInterface != null) {
			actionInterface.processActionByComposer(p_eventName,p_target,p_needResponse,p_upload);
		}
	}	
	
//	protected void onOkPressed(SessionHelper p_sh, CellCollection p_col) {
//	}
}
