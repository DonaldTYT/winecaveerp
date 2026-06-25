package com.uniinformation.jx.zk;

import com.uniinformation.jx.*;
import com.uniinformation.utils.*;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.rpccall.*;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.lang.reflect.*;

import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.util.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;
import org.zkoss.zul.Timer;

public class JxZkSkin extends JxSkin
{
  JxZkGadgetProvider provider;
//  String skinname;
  java.util.Map params;
  
  
  JxFormCloseListener formcloselistener;
  JxFormDirtyListener formdirtylistener;
  Component rootWin;
  Timer  klTimer;
  boolean dirtyFlag;
 // Timer  abTimer;
 // Label  browserWindowId;
  int tokenValue;
  public void addOneElementToSkin(Component c)
  {
	  	 String fdname;
	  	 String fdtype;
	  	 int fdtypeid;
		 fdname = c.getId();
		 fdtype = c.getWidgetClass();
		 fdtypeid = JxZkGadgetProvider.getWinCallSegID(fdtype); 
		 if(c instanceof ZkJxCheckListBox) {
			 fdtypeid = JxZkGadgetProvider.SEG_TCHECKLISTBOX;
		 }
		 UniLog.log("Component: "+ fdname + " " + fdtype + " id "+fdtypeid);
		 
		 switch(fdtypeid) {
			case JxZkGadgetProvider.SEG_THTML           :	
					this.addField (fdname,new JxZkHtml(this,fdtypeid,c));
					break;
			case JxZkGadgetProvider.SEG_TLABEL          :	
					this.addField (fdname,new JxZkLabel(this,fdtypeid,c));
					break;
			case JxZkGadgetProvider.SEG_TCOMBOBOX       :
					this.addField (fdname,new JxZkCombobox(this,fdtypeid,c));
					break;
			case JxZkGadgetProvider.SEG_TTEXTBOX		:
			case JxZkGadgetProvider.SEG_TDOUBLEBOX      :
			case JxZkGadgetProvider.SEG_TINTBOX         :
			case JxZkGadgetProvider.SEG_TSPINNER        :
			case JxZkGadgetProvider.SEG_TDECIMALSPINNER :
			case JxZkGadgetProvider.SEG_TDATEBOX        :
			case JxZkGadgetProvider.SEG_TTIMEBOX        :
					this.addField (fdname,new JxZkInputElement(this,fdtypeid,c));
					break;
			case JxZkGadgetProvider.SEG_TBANDBOX        :
					this.addField (fdname,new JxZkBandBox(this,fdtypeid,c));
					break;
			case JxZkGadgetProvider.SEG_TRADIOGROUP     :	
					this.addField (fdname,new JxZkRadiogroup(this,fdtypeid,c));
					break;
			case JxZkGadgetProvider.SEG_TCHECKBOX       :	
					this.addField (fdname,new JxZkCheckbox(this,fdtypeid,c));
					break;
			case JxZkGadgetProvider.SEG_TBUTTON         : 
			case JxZkGadgetProvider.SEG_TTOOLBARBUTTON  :
			case JxZkGadgetProvider.SEG_TSELECTBOX      :
			case JxZkGadgetProvider.SEG_TIMAGE          :	
				
			case JxZkGadgetProvider.SEG_TCALENDAR       :
					this.addField (fdname,new JxZkButton(this,fdtypeid,c));
					break;

			case JxZkGadgetProvider.SEG_TLISTBOX        :
					this.addField (fdname,new JxZkListbox(this,fdtypeid,c));
					break;
			case JxZkGadgetProvider.SEG_TSELECT         :
					this.addField (fdname,new JxZkSelector(this,fdtypeid,c));
					break;
				

			case JxZkGadgetProvider.SEG_TVLAYOUT: 
			case JxZkGadgetProvider.SEG_THLAYOUT: 
			case JxZkGadgetProvider.SEG_TROWS: 
			case JxZkGadgetProvider.SEG_TROW: 
			case JxZkGadgetProvider.SEG_TBOX: 
					this.addField (fdname,new JxZkLayout(this,fdtypeid,c));
					break;

			case JxZkGadgetProvider.SEG_TSPREADSHEET:	
					this.addField (fdname,new JxZkSpreadsheet(this,fdtypeid,c));
					break;
			case JxZkGadgetProvider.SEG_TTOOLBAR        :	
			case JxZkGadgetProvider.SEG_TPOPUP          :	
			case JxZkGadgetProvider.SEG_TPROGRESSMETER  :	
				
			case JxZkGadgetProvider.SEG_TTABBOX: 
			case JxZkGadgetProvider.SEG_TTABPANEL: 

			case JxZkGadgetProvider.SEG_TMENUBAR: 
			case JxZkGadgetProvider.SEG_TGRID: 
			case JxZkGadgetProvider.SEG_TDIV: 
			case JxZkGadgetProvider.SEG_TIFRAME: 
				
					this.addField (fdname,new JxZkContainer(this,fdtypeid,c));
					break;
			case JxZkGadgetProvider.SEG_TTAB: 
					this.addField (fdname,new JxZkTab(this,fdtypeid,c));
					break;
//			case JxZkGadgetProvider.SEG_TTIMELINE:	
//					this.addField (fdname,new JxZkTimeline(this,fdtypeid,c));
//					break;
			case JxZkGadgetProvider.SEG_TCHECKLISTBOX:	
					this.addField (fdname,new JxZkCheckListBox(this,fdtypeid,c));
					break;
			case JxZkGadgetProvider.SEG_TTREE: 
					this.addField (fdname,new JxZkTree(this,fdtypeid,c));
					break;
			default:
       			UniLog.log("ignore: " + fdname);
				break;
		 }
  }
  
  void addAllChildren(Component c) {
		  List childs = c.getChildren();
		  for(Iterator it = childs.iterator();it.hasNext();) {
			 Component child = (Component)  it.next();
			 if(!child.getId().equals("")) addOneElementToSkin(child);
			 addAllChildren(child);
		  }
  }
  public JxZkSkin(JxZkGadgetProvider p_provider,String p_skinname,Component w, String p_instanceName, java.util.Map p_params)
  {
	 super();
	 /*
	 String fdname;
	 String fdtype;
	 int fdtypeid;
	 */
	 provider = p_provider;
	 jxskinname = p_skinname;
	 instanceName = p_instanceName;
//	 jxskinclass = (String) argvector.get(0);
	 params = p_params;
     rootWin = w;
	 if(w instanceof Window) {
	 Collection <Component> cols = w.getFellows();
	 Iterator itr = cols.iterator();
	 while(itr.hasNext()) {
		 Component c = (Component) itr.next();
		 if (c instanceof Window){
			 if (w.getId() == null || c.getId().equals(w.getId()))
				 continue;
			 for (Component c2 : c.getFellows()){
				 addOneElementToSkin(c2);
			 }
		 }
		 else{
			 addOneElementToSkin(c);
		 }
      } 
	  } else {
		  addAllChildren(w);
	  }
//	  w.setVisible(false);
//	  if(rootWin instanceof Window ) {
	 {
	  klTimer = new Timer();
	  if (w.getPage() != null){
		  UniLog.log("klTimer attach to current page:");
		  klTimer.setPage(w.getPage());
	  }
	  else{
		  UniLog.log("klTimer attach to object:" + w.getId());
		  klTimer.setParent(w);
	  }
	  klTimer.setDelay(10000);
	  klTimer.setRepeats(true);
	  klTimer.setRunning(false);
	  klTimer.addEventListener("onTimer", new EventListener() {
	  @Override
	  	public void onEvent(Event event) throws Exception {
		  	JxForm jxf = provider.jxGetForm(instanceName);
		  	if(jxf != null) jxf.doKeepAlive();
		  		/*
		  	if(skToken == null) {
		  		UniLog.log("HAHA 2016 first timer event, creating tokens");
		  		skToken = new Label();
		  		rootWin.appendChild(skToken);
		  		skToken.setValue(""+tokenValue);

		  		skInstance = new Label();
		  		rootWin.appendChild(skInstance);
		  		skInstance.setValue(instanceName);

		  		klTimer.setDelay(10000);
		  	} else {
		    	UniLog.log("HAHA 2016 timer fired "+instanceName+","+skToken.getValue() + "," + skInstance.getValue() + "," + klTimer.getDelay());
	    		tokenValue++;
		    	skToken.setValue(""+tokenValue);
		  	}
		  		*/
	  	}
	  });
      rootWin.addEventListener(Events.ON_CLOSE,
	        	new EventListener() {
	        		public void onEvent(Event event) throws Exception {
	        			UniLog.log("Close button pressed");
	        			if(formcloselistener != null) {
	        				JxForm jxf = provider.jxGetForm(instanceName);
	        				int cc = formcloselistener.formClose(jxf);
	        				switch(cc) {
	        				case JxFormCloseListener.caDefault: break;
	        				case JxFormCloseListener.caFree :
	        						rootWin.detach();
	        						provider.jxUnRegisterForm(instanceName);
	        						event.stopPropagation();
	        						break;	
	        				case JxFormCloseListener.caMinimize :
	        						if(rootWin instanceof Window) ((Window) rootWin).setMinimized(true);
	        						event.stopPropagation();
	        						break;	
	        				case JxFormCloseListener.caHide:
	        						rootWin.setVisible(false);
	        						event.stopPropagation();
	        						break;	
	        				case JxFormCloseListener.caNone:
	        						event.stopPropagation();
	        						break;	
	        				}
	        			}
	            	}	
	        	}
	        );   
	  }
  }

  public JxGadgetProvider getGadgetProvider()
  {
  		return (provider);
  }

  public JxZkGadgetProvider getZkGadgetProvider()
  {
  		return (provider);
  }
//  public String getName()
//  {
//  		return(skinname);
//  }

  public void enableForm()
  {
	// control.setEnabled(true);
  }
  public void disableForm()
  {
	// control.setEnabled(false);
  }
  public void showForm()
  {
		rootWin.setVisible(true);
  }
  public void hideForm()
  {
		rootWin.setVisible(false);
  }
  public boolean isFormVisible(){
	  	return(rootWin.isVisible());
  }
 
  public Object getControl()
  {
  		return(null);
  }
  public void closeForm()
  {
	  if(formcloselistener != null) {
	  	JxForm jxf = provider.jxGetForm(instanceName);
		int cc = formcloselistener.formClose(jxf);
		UniLog.logm(this,"closeForm[%s] return %d", instanceName, cc);
		switch(cc) {
		case JxFormCloseListener.caDefault: 
			break;
		case JxFormCloseListener.caFree :
			rootWin.detach();
			provider.jxUnRegisterForm(instanceName);
			break;	
		case JxFormCloseListener.caMinimize :
			if(rootWin instanceof Window) {
				((Window) rootWin).setMinimized(true);
			}
			break;	
		case JxFormCloseListener.caHide:
			rootWin.setVisible(false);
			break;	
		case JxFormCloseListener.caNone:
			break;	
		}
	  }

  }

  public void tileChildForm()
  {
  }

  public void cascadeChildForm()
  {
  }

  public void arrangeChildForm()
  {
  }

  public String getActiveChildForm()
  {
		return(null);
  }

  public void setTitle(String p_string)
  {
	if(rootWin instanceof Window) ((Window) rootWin).setTitle(p_string);
  }


  public void setFormStyle(String p_string)
  {
  }



	public void setFocus()
	{
	}
	public void setFocus(String p_fieldname)
	{
	}

  public void modalForm()
  {		
	  if(rootWin instanceof Window) {
		((Window) rootWin).doHighlighted();
		((Window) rootWin).setPosition("middle_center"); 
		((Window) rootWin).setVisible(true);
	  }
  }
  public void modalFormWithCallback(String p_focusfield)
  {		
  }
  public void addFormCloseListener(JxFormCloseListener p_formcloselistener)
  {
		formcloselistener = p_formcloselistener;
  }
  public int processFormClose(JxForm p_form)
  {
  		if(formcloselistener != null) {
			return(formcloselistener.formClose(p_form));
		} else {
			return(JxFormCloseListener.caDefault);
		}
  }
  public void beep()
  {
  }
  public void unFocus()
  {
  }
  public void maximize()
  {
  }
  public String getParameter(String p_paramName)
  {
//	  	String retString = null;
	  	if(params == null) return(null);
	  	return(SessionHelper.getURLParamAsString(p_paramName, params));
	  	/*
	  	Object obj = params.get(p_paramName);
	  	if (obj == null) 
	  		retString = null;
	  	else if (obj instanceof String) 
	  			   retString = (String) obj;
	  	else if (obj instanceof String[] && ((String []) obj).length > 0) {
	  		//retString = ((String []) obj)[0];
	  		retString = ((String []) obj)[((String []) obj).length-1];
	  	}
	  	return(retString);		
//	  	return(Executions.getCurrent().getParameter(p_paramName));
//	  	return(null);
	  	 */
  }
  public Object getParameterObject(String p_paramName){
	  if (params == null) return null;
	  return params.get(p_paramName);
  }
  public void setKeepAliveInterval(int p_msec)
  {
	  UniLog.log("HAHA 2016 setKeepAliveInterval " + p_msec);
	  klTimer.setDelay(p_msec);
	  if(p_msec > 0) klTimer.setRunning(true); else klTimer.setRunning(false);
  }
  public Object getNativeComponent()
  {
	  return(rootWin);
  }
  public void setDirtyFlag(boolean p_flag) {
	  if(p_flag != dirtyFlag) {
		  dirtyFlag = p_flag;
		  if(formdirtylistener != null) {
			  JxForm jxf = provider.jxGetForm(instanceName);
			  formdirtylistener.formDirtyFlagChanged(jxf);
		  }
	  }
  }
  public boolean isDirty(){
	  return(dirtyFlag);
  }
  public void addFormDirtyListener(JxFormDirtyListener p_formdirtylistener)
  {
		formdirtylistener = p_formdirtylistener;
  }
  
  
}
