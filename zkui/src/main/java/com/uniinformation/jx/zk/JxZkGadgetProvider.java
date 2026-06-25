package com.uniinformation.jx.zk;

import com.uniinformation.jx.*;
import com.uniinformation.rpccall.*;
import com.uniinformation.utils.*;
import com.uniinformation.webcore.SessionHelper;

import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.lang.reflect.*;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.*;
import org.zkoss.zk.ui.util.*;
import org.zkoss.zk.ui.event.*;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.*;
import org.zkoss.zul.impl.InputElement;

public class JxZkGadgetProvider extends JxGadgetProvider implements JxPrintDlgInterface {
	
	SessionHelper sessionHelper;
	
	public static final String STR_TBUTTON		  = "zul.wgt.Button";
	public static final String STR_TSELECTBOX     = "zul.wgt.Selectbox";

	public static final String STR_TTOOLBAR       = "zul.wgt.Toolbar";
	public static final String STR_TIMAGE         = "zul.wgt.Image";
	public static final String STR_THTML          = "zul.wgt.Html";
	public static final String STR_TPROGRESSMETER = "zul.wgt.Progressmeter";
	public static final String STR_TLABEL         = "zul.wgt.Label";
	public static final String STR_TCHECKBOX      = "zul.wgt.Checkbox";
	public static final String STR_TPOPUP         = "zul.wgt.Popup";
	public static final String STR_TTOOLBARBUTTON = "zul.wgt.Toolbarbutton";
	public static final String STR_TRADIOGROUP    = "zul.wgt.Radiogroup";
	

	public static final String STR_TTEXTBOX		  = "zul.inp.Textbox";

	public static final String STR_TCOMBOBOX      = "zul.inp.Combobox";
	public static final String STR_TDOUBLEBOX     = "zul.inp.Doublebox";
	public static final String STR_TINTBOX        = "zul.inp.Intbox";
	public static final String STR_TSPINNER       = "zul.inp.Spinner";
	public static final String STR_TDECIMALSPINNER= "zul.inp.Doublespinner";
	public static final String STR_TBANDBOX       = "zul.inp.Bandbox";
	
	public static final String STR_TDATEBOX       = "zul.db.Datebox";
	public static final String STR_TCALENDAR      = "zul.db.Calendar";
	public static final String STR_TTIMEBOX       = "zul.db.Timebox";
	
  	public static final String STR_TTABBOX		  = "zul.tab.Tabbox";
	public static final String STR_TTABPANEL      = "zul.tab.Tabpanel";
	public static final String STR_TVLAYOUT       = "zul.box.Vlayout";
	public static final String STR_THLAYOUT		  = "zul.box.Hlayout";
	public static final String STR_TBOX		  	  = "zul.box.Box";
	public static final String STR_TROWS 		  = "zul.grid.Rows";
	public static final String STR_TROW 		  = "zul.grid.Row";

	public static final String STR_TLISTBOX       = "zul.sel.Listbox";
	public static final String STR_TSELECT        = "zul.sel.Select";
	public static final String STR_TTREE          = "zul.sel.Tree";
	
	public static final String STR_TGRID          = "zul.grid.Grid";
	public static final String STR_TDIV           = "zul.wgt.Div";
	public static final String STR_TIFRAME	      = "zul.utl.Iframe";

	public static final String STR_TMENUBAR       = "zul.menu.Menubar";
	
	public static final String STR_TSPREADSHEET   = "zss.Spreadsheet";
	
	
	public static final String STR_EONCLICK		= "onClick";
	public static final String STR_EONDELAYCLICK= "onDelayClick";
	public static final String STR_EONCHANGE	= "onChange";
	public static final String STR_EONCELLCHANGE= org.zkoss.zss.ui.event.Events.ON_AFTER_CELL_CHANGE;
	public static final String STR_EONCHECK     = "onCheck";
	public static final String STR_EONCTRLKEY   = "onCtrlKey";
	public static final String STR_EONLONGOP    = "onLongOp";
	public static final String STR_EONSELECT    = "onSelect";
	public static final String STR_EONCANCEL    = "onCancel";
	public static final String STR_EONOK        = "onOK";
	public static final String STR_EONFOCUS     = "onFocus";
	public static final String STR_EONBLUR      = "onBlur";
	public static final String STR_EONDOUBLECLICK = Events.ON_DOUBLE_CLICK;
	public static final String STR_EONOPEN      = Events.ON_OPEN;
	
	public static final String STR_TTIMELINE	= "timelinez.Timeline";
	public static final String STR_TTAB         = "zul.tab.Tab";
	
	public static final int SEG_TBUTTON			= 20001;	
	public static final int SEG_TSELECTBOX      = 20002;	
	public static final int SEG_TTOOLBAR        = 20003;	
	public static final int SEG_TIMAGE          = 20004;	
	public static final int SEG_THTML           = 20005;	
	public static final int SEG_TPROGRESSMETER  = 20006;	
	public static final int SEG_TLABEL          = 20007;	
	public static final int SEG_TCHECKBOX       = 20008;	
	public static final int SEG_TPOPUP          = 20009;	
	public static final int SEG_TTOOLBARBUTTON  = 20010;	
	public static final int SEG_TRADIOGROUP     = 20011;	
	public static final int SEG_TTAB            = 20012;	
	

	public static final int SEG_TTEXTBOX		= 21001;
	public static final int SEG_TCOMBOBOX       = 21002;
	public static final int SEG_TDOUBLEBOX      = 21003;
	public static final int SEG_TINTBOX         = 21004;
	public static final int SEG_TSPINNER        = 21005;
	public static final int SEG_TDECIMALSPINNER = 21006;
	public static final int SEG_TBANDBOX        = 21007;
	
	public static final int SEG_TDATEBOX        = 22001;
	public static final int SEG_TCALENDAR       = 22002;
	public static final int SEG_TTIMEBOX        = 22003;
	
	public static final int SEG_TTABBOX			= 23001;	
	public static final int SEG_TTABPANEL	    = 23002;	
	public static final int SEG_TVLAYOUT 	    = 23003;	
	public static final int SEG_THLAYOUT 	    = 23004;	
	public static final int SEG_TROWS    	    = 23005;	
	public static final int SEG_TROW    	    = 23006;	
	public static final int SEG_TBOX    	    = 23007;	

	public static final int SEG_TLISTBOX        = 24001;
	public static final int SEG_TTREE           = 24002;
	public static final int SEG_TSELECT         = 24003;
 	
	public static final int SEG_TGRID           = 25001;

	public static final int SEG_TMENUBAR        = 25002;
	public static final int SEG_TDIV            = 25003;
	public static final int SEG_TIFRAME         = 25004;

	public static final int SEG_TSPREADSHEET    = 26001;

	public static final int SEG_TCHECKLISTBOX   = 27001;
	
	public static final int SEG_TTIMELINE    = 28001;
	
	public static final int EV_ONCLICK 			= 1;	
	public static final int EV_ONCHANGE			= 2;	
	public static final int EV_ONCELLCHANGE		= 3;	
	public static final int EV_ONCHECK			= 4;	
	public static final int EV_ONCTRLKEY		= 5;	
	public static final int EV_ONLONGOP			= 6;	
	public static final int EV_ONSELECT         = 7;	
	public static final int EV_ONCANCEL			= 8;	
	public static final int EV_ONOK     		= 9;	
	public static final int EV_ONFOCUS     		= 10;	
	public static final int EV_ONBLUR      		= 11;	
	public static final int EV_ONDOUBLECLICK	= 12;	
	public static final int EV_ONOPEN			= 13;	
	public static final int EV_ONDELAYCLICK		= 14;
	
	
	private static Hashtable winCallSegHashTable = initWinCallSegHashTable();	
	private static Hashtable initWinCallSegHashTable() {
		Hashtable ht;	
		UniLog.log("initializing WinCallSegHashTable");
		ht = new Hashtable();
		ht.put(STR_TBUTTON,new Integer(SEG_TBUTTON));		
		ht.put(STR_TSELECTBOX,new Integer(SEG_TSELECTBOX));		

		ht.put(STR_TTOOLBAR ,new Integer( SEG_TTOOLBAR       ));
		ht.put(STR_TIMAGE,new Integer( SEG_TIMAGE         ));
		ht.put(STR_THTML,new Integer( SEG_THTML          ));
		ht.put(STR_TPROGRESSMETER,new Integer( SEG_TPROGRESSMETER ));
		ht.put(STR_TLABEL,new Integer( SEG_TLABEL         ));
		ht.put(STR_TCHECKBOX,new Integer( SEG_TCHECKBOX      ));
		ht.put(STR_TPOPUP,new Integer( SEG_TPOPUP         ));
		ht.put(STR_TTOOLBARBUTTON,new Integer( SEG_TTOOLBARBUTTON ));
		ht.put(STR_TRADIOGROUP,new Integer( SEG_TRADIOGROUP    ));
		ht.put(STR_TTAB ,new Integer( SEG_TTAB));

		ht.put(STR_TTEXTBOX,new Integer(SEG_TTEXTBOX));
		ht.put(STR_TCOMBOBOX       , new Integer(SEG_TCOMBOBOX       ));
		ht.put(STR_TDOUBLEBOX      , new Integer(SEG_TDOUBLEBOX      ));
		ht.put(STR_TINTBOX         , new Integer(SEG_TINTBOX         ));
		ht.put(STR_TSPINNER        , new Integer(SEG_TSPINNER        ));
		ht.put(STR_TDECIMALSPINNER , new Integer(SEG_TDECIMALSPINNER ));
		
		ht.put(STR_TBANDBOX,new Integer(SEG_TBANDBOX));		
		ht.put(STR_TDATEBOX,new Integer(SEG_TDATEBOX));		
		ht.put(STR_TCALENDAR,new Integer(SEG_TCALENDAR));		
		ht.put(STR_TTIMEBOX,new Integer(SEG_TTIMEBOX));		
		
		ht.put(STR_TTABBOX,new Integer(SEG_TTABBOX));		
		ht.put(STR_TTABPANEL,new Integer(SEG_TTABPANEL));		
		ht.put(STR_TVLAYOUT,new Integer(SEG_TVLAYOUT));		
		ht.put(STR_TBOX,new Integer(SEG_TBOX));		
		ht.put(STR_THLAYOUT,new Integer(SEG_THLAYOUT));		
		ht.put(STR_TROWS,new Integer(SEG_TROWS));		
		ht.put(STR_TROW,new Integer(SEG_TROW));		

		ht.put(STR_TLISTBOX,new Integer(SEG_TLISTBOX));		
		ht.put(STR_TTREE,new Integer(SEG_TTREE));		
		ht.put(STR_TSELECT,new Integer(SEG_TSELECT));		

		ht.put(STR_TGRID,new Integer(SEG_TGRID));		
		ht.put(STR_TDIV,new Integer(SEG_TDIV));		
		ht.put(STR_TIFRAME,new Integer(SEG_TIFRAME));		

		ht.put(STR_TMENUBAR,new Integer(SEG_TMENUBAR));		

		ht.put(STR_TSPREADSHEET,new Integer(SEG_TSPREADSHEET));		
		
		ht.put(STR_TTIMELINE,new Integer(SEG_TTIMELINE));		
		return(ht);
	}
	
    static int getWinCallSegID(String p_callseg) {
        Integer i;
        i = (Integer) winCallSegHashTable.get(p_callseg);
        if(i == null) return(-1);else return(i.intValue());
    }
	
	private static Hashtable eventTable = initEventTable();	
	private static Hashtable initEventTable() {
		Hashtable ht;	
		UniLog.log("initializing eventTable");
		ht = new Hashtable();
		ht.put(STR_EONCLICK,new Integer(EV_ONCLICK));
		ht.put(STR_EONDELAYCLICK,new Integer(EV_ONDELAYCLICK));
		ht.put(STR_EONCHANGE,new Integer(EV_ONCHANGE));
		ht.put(STR_EONCELLCHANGE,new Integer(EV_ONCELLCHANGE));
		ht.put(STR_EONCHECK,new Integer(EV_ONCHECK));
		ht.put(STR_EONCTRLKEY,new Integer(EV_ONCTRLKEY));
		ht.put(STR_EONLONGOP,new Integer(EV_ONLONGOP));
		ht.put(STR_EONSELECT,new Integer(EV_ONSELECT));
		ht.put(STR_EONCANCEL,new Integer(EV_ONCANCEL));
		ht.put(STR_EONOK,new Integer(EV_ONOK));
		ht.put(STR_EONFOCUS,new Integer(EV_ONFOCUS));
		ht.put(STR_EONBLUR,new Integer(EV_ONBLUR));
		ht.put(STR_EONDOUBLECLICK ,new Integer(EV_ONDOUBLECLICK));
		ht.put(STR_EONOPEN,new Integer(EV_ONOPEN));
		return(ht);
	}
	
    static int getEventID(String p_eventName) {
        Integer i;
        i = (Integer) eventTable.get(p_eventName);
        if(i == null) return(-1);else return(i.intValue());
    }
	
    int instanceCnt = 0;	
	synchronized String getInstanceName()
	{
		instanceCnt++;
		return("JXZK_"+instanceCnt);
	}
    public JxForm jxzk_forminit(Component zkWindow) {
    	return( jxzk_forminit(zkWindow,null,null));
    }
    public JxForm jxzk_forminit(Component zkWindow,java.util.Map params,String p_instanceName) {
		String fname = zkWindow.getId();
		UniLog.log("jxzk_forminit"+fname);
		try {
			JxForm jxform;
			Class jxFormClass ;
			if(zkWindow.getAttribute("formClass") != null) {
				String cp = (String) zkWindow.getAttribute("formClass");
				jxFormClass = Class.forName(cp);
				
			} else {
				jxFormClass = Class.forName("com.uniinformation.jxapp." + fname);
			}
			Constructor constructor = jxFormClass.getConstructor((Class[]) null);
			if(constructor == null) {
				UniLog.log("get constructor failed " + fname);
				return(null);
			}	
			UniLog.log("Initializing form "+fname);
			jxform = (JxForm) constructor.newInstance();
			String s;
			if(p_instanceName == null) s = getInstanceName(); else s = p_instanceName;
			JxZkSkin skin = new JxZkSkin(this,fname,zkWindow,s,params);
			this.jxRegisterForm(s,jxform,skin);
			jxform.getSkin().setFocus(jxform.nextFocus());	
			zkWindow.setId(s);
			return(jxform);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(null);
		}
	}
    
	public JxZkGadgetProvider(SessionHelper p_sessionHelper) {
		UniLog.log("JxZkGadgetProvider Initialized :" + this);
		sessionHelper = p_sessionHelper;
		ThreadData.setThGrpData("JxCbuilderGadgetProvider", this);
	}
//	public void close() {
//		UniLog.log("JxZkGadgetProvider closed :" + this);
//	}
	

//	inherrited abstract mathods
	
	public int jxPutFile(DataInput p_input,String p_filename) {
		return(-1);
	}
	public String jxNewTempDir(String p_prefix) {
		return(null);
	}
	public void jxNotifyMsg(String p_msg,int p_type, int p_dur){
		int dur = p_dur;
		if (dur <= 1000){
			dur = 1000;
		}
		if (p_msg == null){
			UniLog.logm(this, "msg is null, ignore");
			return;
		}
		switch(p_type) {
		case 1:
			Clients.evalJavaScript("$.notify(\""+p_msg+"\", { className: \"info\", globalPosition:\"bottom right\", autoHideDelay: "+dur+" })");
			break;
		case 2:
			Clients.evalJavaScript("$.notify(\""+p_msg+"\", { className: \"warn\", globalPosition:\"bottom right\", autoHideDelay: "+dur+" })");
			break;
		case 3:
			Clients.evalJavaScript("$.notify(\""+p_msg+"\", { className: \"error\", globalPosition:\"bottom right\", autoHideDelay: "+dur+" })");
			break;
		default:
			Clients.evalJavaScript("$.notify(\""+p_msg+"\", { className: \"info\", globalPosition:\"bottom right\", autoHideDelay: "+dur+" })");
			break;
		}
	}
	public static void jxMessageboxNotify(InputElement p_inputComp, String p_msg){
		if (p_inputComp == null || StringUtils.isBlank(p_msg)){
			return;
		}
		UniLog.logm(null,"focus to comp %s", p_inputComp.getId());
		p_inputComp.setFocus(true);
		p_inputComp.select();
		Clients.showNotification(p_msg, "warning", p_inputComp, "end_center", 3000);
	}
	public void jxMessageBox(final String p_msg,int p_type,final MessageBoxActionInterface p_action) {
		
		//obtain inputComp
		JxGadgetProvider provider = getCurrentProvider();
		Component comp = null;
		if (provider != null){
			String compPath = (String) provider.getUserData("AFTER_MSGBOX_FOCUS");
			UniLog.logm(this,"jxMessagebox: AFTER_MSGBOX_FOCUS=%s", compPath);
			if (compPath != null){
				comp = Path.getComponent(compPath);
			}
		}
		
		//if inputComp is valid and no further action, display notification only, without messagebox
		final InputElement inputComp = (comp != null && comp instanceof InputElement) ? (InputElement) comp : null;
		if (p_action == null && inputComp != null){ 
			jxMessageboxNotify(inputComp, p_msg);
			return;
		}
		
		//has further action
		EventListener<Event> msgboxEventListener = null;
		if (p_action != null){
			msgboxEventListener = new EventListener<Event>(){
				public void onEvent(Event evt) throws InterruptedException {
					jxMessageboxNotify(inputComp, p_msg);
					if (p_action != null){
						p_action.onButtonClicked(evt.getData());
					}
				}
			};
		}
		
		
		if (msgboxEventListener != null){
			switch(p_type) {
			case 1:
				Messagebox.show(p_msg, sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR, msgboxEventListener);
				break;
			default:
				Messagebox.show(p_msg, sessionHelper.getLabel("System Message"), Messagebox.OK, Messagebox.INFORMATION, msgboxEventListener);
				break;
			}
		}
		else{
			switch(p_type) {
			case 1:
				Messagebox.show(p_msg, sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
				break;
			default:
				Messagebox.show(p_msg, sessionHelper.getLabel("System Message"), Messagebox.OK, Messagebox.INFORMATION);
				break;
			}
		}
	}
	/*
	//display either notification or messagebox
	public void jxMessageBox(final String p_msg) {
		JxGadgetProvider provider = getCurrentProvider();
		Component comp = null;
		String compPath = null;
		if (provider != null){
			compPath = (String) provider.getUserData("AFTER_MSGBOX_FOCUS");
			UniLog.log("jxMessagebox: AFTER_MSGBOX_FOCUS=" + compPath);
			if (compPath != null){
				comp = Path.getComponent(compPath);
			}
		}
		else{
			UniLog.log("provider is null");
		}
		final InputElement inputElement = (comp != null && comp instanceof InputElement) ? (InputElement) comp : null;
		if (inputElement == null){
			Messagebox.show(p_msg);
		}
		else{
			Clients.showNotification(p_msg, "info", inputElement, "end_center", 3000);
			inputElement.setFocus(true);
			inputElement.select();
		}
	}
	*/
	public void jxLoadDfm(String p_formName, String p_fieldName,String p_dfmFile) throws Exception {
	}
	public void jxLoadDfm(String p_formName, String p_dfmFile) throws Exception {
	}
	public String jxCreateForm(String p_formname,String p_instanceName,String p_dfm) {
		return(null);
	}
	public void jxModalForm(String p_instanceName) {
	}
	public void setDebug(boolean p_sw)
	{
	}
	public int jxChnftr(Vector arglist) {
		return(-1);
	}
	public void jxDestroyForm(String p_instanceName) {
	}
	public int jxShellExecute(String p_operation,String p_path,String p_parameter,String p_dir) {
		return(-1);
	}
	public int jxPutFile(InputStream p_input,String p_filename) {
		return(-1);
	}
	public String jxGetTempFile(String p_prefix) {
		return(null);
	}
	public int jxRemoveDir(String p_path) {
		return(0);
	}
	public int jxUnlink(String p_path) {
		return(-1);
	}
	public int jxSaveFileToTif(String p_imgfile,String p_imgtype,String p_tiffname,String p_append_or_write,String p_colormode,int resolution) {
		return(-1);
	}
	public long jxGetFileSize(String p_path) {
		return(-1L);
	}
	public String getSessionLabel()
	{
		return(null);
	}
	public boolean jxConfirm(String p_msg,final MessageBoxActionInterface p_action) {
		EventListener<Event> msgboxEventListener = null;
		if(msgboxEventListener == null && p_action  != null){
			msgboxEventListener = new EventListener<Event>(){
				public void onEvent(Event evt) throws InterruptedException {
					p_action.onButtonClicked(evt.getData());
				}
			};
		}
		if (msgboxEventListener != null){
			Messagebox.show(p_msg, "Message", Messagebox.OK|Messagebox.CANCEL, Messagebox.INFORMATION, msgboxEventListener);
		}
		else{
			Messagebox.show(p_msg);
		}
		return(true);
	}
	public JxSkinElement jxMenuItem(String p_fieldname) {
		return(null);
	}
	public String jxOpenDialog(String p_dir, String p_filter) {
		return(null);
	}
	public void runApp(Object p_form) {
	}
	public InetAddress jxRemoteAddress() {
		return(null);
	}
	public String jxSaveDialog(String p_name) {
		return(null);
	}
	public int jxMessageBoxWithReturn(Vector v) 
	{
		return(-1);
	}
	public void jxTranslateMessage()
	{
	}
	public void delayClick(String p_instancename,String p_buttonname) {
	}
	public RpcServerConnection getConn() {
		return(null);
	}
	public JxSkinElement jxNewTreeNode() {
		return(null);
	}
	public int jxPutFile(InputStream p_input,String p_filename,int p_kbyte,JxUpdateProgress p_interface) {
		return(-1);
	}
	public JxSkinElement jxImageList() {		
		return(null);
	}
	public int jxGetFile(DataOutput p_output,String p_filename) {
		return(-1);
	}
	public int jxGetFile(OutputStream p_output,String p_filename) {
		return(-1);
	}
	public int jxMakeDir(String p_path) {
		return(0);
	}
	public int jxGetFile(OutputStream p_output,String p_filename,int p_kbyte,JxUpdateProgress p_interface) {
		return(-1);
	}
	public boolean printDialog() {
		return(false);
	}
	 public void editHint(JxForm p_form,JxField p_field)
	  {
	  }
	  public JxForm getOrCreateForm(String p_formName)
	  {
	  		return(null);
	  }
	  public void putChnCache(String p_fname,int p_mode)
	  {
	  }
	  public void putWxCache(String p_fname,int p_mode)
	  {
	  }
	  public String getClientWxCacheDir()
	  {
	  		return(null);
	  }
	  public String getClientWxTmpDir()
	  {
	  		return(null);
	  }
	  public String getClientChnCacheDir()
	  {
	  		return(null);
	  }
	  public void removeWxCache(String p_cacheFile)
	  {
	  }	
	  
	  public void providerCleanUp() {
		  super.providerCleanUp();
		  sessionHelper = null;
	  }
	  
	  public String getLoginId()
	  {
		 return(sessionHelper.getLoginId());
	  }
	  
	  public RpcClient getRpcClient()
	  {
		 return(sessionHelper.getRpcClient());
	  }
	  
	  public InputStream erpFileInputStream(String p_filename) throws Exception
	  {
		 return(sessionHelper.newErpFileInputStream(p_filename));
	  }
	  
	  public OutputStream erpFileOutputStream(String p_filename) throws Exception
	  {
		 return(sessionHelper.newErpFileOutputStream(p_filename));
	  }
	  
	  public Object getSessionObject(String p_key) 
	  {
		  return(sessionHelper.getSessionData(p_key));
	  }
	  
	  public Properties loadProperty(String p_propfile)
	  {
		  UniLog.log("HAHA 2017 in provider.loadProperty " + p_propfile);
		  return(sessionHelper.loadProperty(p_propfile));
	  }
	  
	  public static JxGadgetProvider getCurrentProvider() {
		  JxGadgetProvider pvdr = (JxGadgetProvider) ThreadData.getThGrpData("JxCbuilderGadgetProvider");
		  if(pvdr != null) return(pvdr);
		  Execution exec;
		  if( (exec = Executions.getCurrent()) != null) {
			  Session sess;
			  if((sess = exec.getSession()) != null ) {
				 SessionHelper sessionHelper = (SessionHelper) sess.getAttribute(SessionHelper.getNameByContextPath(exec.getContextPath()));
				 pvdr = (JxGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider"); 
				 if(pvdr != null) {
						ThreadData.setThGrpData("JxCbuilderGadgetProvider", pvdr);
						return(pvdr);
				 }
			  }
		  }
		  return(null);
	  }
	  public InputStream getResourceAsStream(String p_str)
	  {
		  	return(sessionHelper.openResourceAsStream(p_str));
	  }
	  
	  public boolean isTouchPanel() {
		  	return(sessionHelper.isTouchPanel());
	  }
}
 