package com.uniinformation.zkbi;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;

import com.uniinformation.utils.UniLog;

public class ZkBiHotkeyHelper {
	public final static int ALT_KEY = 1;
	public final static int ESC_KEY = 2;
	ZkBiHotkeyInterface targetInterface = null;
	public ZkBiHotkeyHelper(Component p_targetComp, ZkBiHotkeyInterface p_targetInterface, boolean p_alt, boolean p_esc){
		targetInterface = p_targetInterface;
		if (p_alt){
			addEventListener(ALT_KEY, "onCustomAltDown", p_targetComp);
		}
		if (p_esc){
			addEventListener(ESC_KEY, "onCustomEsc", p_targetComp);
		}
	}
	private void addEventListener(final int p_modifierKey, final String p_event, Component p_targetComp){
    	p_targetComp.addEventListener(p_event, new EventListener() {
    		public void onEvent(Event event) throws Exception {
    			try{
    				char inKey = 0;
	    			if (event.getData() != null){
	    				inKey = (char)((Integer)event.getData()).intValue();
	    			}
	    			UniLog.logm(null, "event:%s key:%c(%d)", p_event, inKey, (int)inKey);
	   				targetInterface.hotkeyEvent(p_modifierKey, inKey);
    			}
    			catch(Exception ex){
    				ex.printStackTrace();
    			}
    			
    	    }
    	});
	}
	/***
	 * check the comp accept hotkey and its parents component is visible
	 * @param p_comp
	 * @return true or false
	 */
	public static boolean checkAcceptHotkey(Component p_comp){
		if (p_comp == null){
			return (false);
		}
		if (p_comp instanceof Button && ((Button)p_comp).isDisabled()){
			return(false);
		}
		if (!p_comp.isVisible()){
			//UniLog.log("checkAcceptHotkey: fail" + p_comp); 
			return(false);
		}
		else{
			//UniLog.log("checkAcceptHotkey: ok" + p_comp); 
		}
		if (p_comp.getParent() != null){
			return(checkAcceptHotkey(p_comp.getParent()));
		}
		else{
			return(true);
		}
	}

}
