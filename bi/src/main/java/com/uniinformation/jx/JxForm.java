package com.uniinformation.jx;
/**************/
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.hssf.util.*;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import com.kyoko.common.*;


/**************/
import java.util.*;
import java.text.*;
import java.io.*;
import java.net.InetAddress;

import com.uniinformation.utils.*;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.rpccall.*;

/*
 * 250821 add later
import jssc.*;
*/
//import com.uniinformation.jxapp.*;
public class JxForm /* extends ClientTableMaint */ implements Runnable, JxPrintDlgInterface
{
	Hashtable fields = new Hashtable();
	private String nextFocusField = null;
	private String curFocusField = "";
	private String jxformname;
	private boolean fisbinded = false;
	private JxSkin myskin;
	private int nextAction = TM_ACTION_INIT;
	protected JxGadgetProvider gadgetProvider;
	public final static int MODAL_OK = 1;
	public final static int MODAL_CANCEL = -1;

	public static final int TM_ACTION_IDLE= 0;
	public static final int TM_ACTION_INIT= 1;
	public static final int TM_ACTION_TERMINATE = 2;
	public static final int TM_ACTION_ADD = 101;
	public static final int TM_ACTION_UPDATE = 102;
	public static final int TM_ACTION_QUERY = 103;
	public static final int TM_ACTION_BROWSE = 104;
	public static final int TM_PROGRESS_ADD = 1101;
	public static final int TM_PROGRESS_UPDATE = 1102;
	public static final int TM_PROGRESS_QUERY = 1103;
	public static final int TM_PROGRESS_BROWSE = 1104;

	JxForm tmThread = null;
	JxField Mb_exe;
	JxField Pb_exe;
	JxField Mb_esc;
	JxField Pb_esc;
	JxField Mb_prevrec;
	JxField Pb_prevrec;
	JxField Mb_nextrec;
	JxField Pb_nextrec;

	public boolean defaultEnabled = true;
	public boolean defaultVisible = true;

/***********************/
	HSSFWorkbook workBook = null;
	HSSFSheet sheet = null;
	int excelSheetIdx = -1;
	String excelSheetName = null;
	HSSFCellStyle hssf_cellStyle = null;
	int excelRowCount = 0;
	int excelDefaultRowHeight = 0;
	int excelCurrentRowHeight = 0;
	Hashtable excelNames = null;
	boolean excel_translateGTET = false;
	int excel_translateChinese = 0;
	boolean excel_filterNonPrintable = false;
	Hashtable<String ,Short>  excel_indexedColorMap;
	Hashtable<String, Short> excel_cellStyle;
	Hashtable<String, Short> excel_font;
/***********************/

	public JxCellUpdateInterface cellCheck;
	int state = 0;
	int buttonPress = 0;
	private String TM_formname = null;
	private boolean tmThreadSwitch=true;
	public JxEventListener eventListener=null;

	// SerialPort jxSerialPort; 250821 removed

		public void run() {
		}
		private void tmThreadPause()
		{
			tmThreadSwitch=false;
			for(;;) {
				UniLog.logClass(this,"TM_thread for " + TM_formname + " Daemon Pending " + state);
				try {
					notify();
					wait(5000);
					if(tmThreadSwitch || state < 0) return;
				} catch (Exception ex) {
					UniLog.log(ex);
				} 
			}
		}

/*	
	public JxForm()
	{
		super(null,null,null);
   }

	public JxForm(String p_tablename)
	{
		super(p_tablename,null,null);
		tmThread = this;
	}

	public JxForm(String p_tablename,JdbcPool p_jdbcpool)
	{
		super(p_tablename,null,null,p_jdbcpool);
		tmThread = this;
	}
*/

	protected void finalize()
	{
		UniLog.log("Finalizing JxForm");
		cleanup();
	}

	public void cleanup()
	{
		UniLog.log("Cleanup JxForm " + jxformname);
		if(fields != null) {
			fields.clear();
			fields = null;
		}
		jxformname = null;
		if(tmThread != null) {
			synchronized(tmThread) {
				tmThread.state = -1;
			}
			tmThread = null;
		}
	}

	protected JxField getJxField(String p_fieldname)
	{
		return((JxField)fields.get(p_fieldname));
	}
	
	/* Obselated */
	/*
	protected void add(JxField p_field)
	{
		String fieldname = p_field.name();
		fields.put(fieldname,p_field);
		if(isbinded() == true) {
			JxSkinElement jxse = myskin.getField(fieldname);
			if(jxse != null) {
				p_field.bind(this,jxse);
	//			System.err.println("bind " +fieldname);
			}
		}
	}
	*/
	protected JxField addWithoutCheck(String p_fieldname)
	{
		JxField jxfield = jxAdd(p_fieldname);
		if(jxfield != null) return(jxfield);
		jxfield = new JxField(p_fieldname);
		fields.put(p_fieldname,jxfield);
		return(jxfield);
	}
	
	public JxField jxAdd(String p_fieldname,String p_skinname) {
		/*
		UniLog.log("jxAdd " + p_fieldname + " " + p_skinname);
		*/
		JxField jxfield = (JxField) fields.get(p_fieldname);
		if(isbinded() == true) {
			JxSkinElement jxse = myskin.getField(p_skinname);
			if(jxse != null) {
				if(jxfield == null) jxfield = new JxField(p_fieldname);
					else if(jxfield.isbinded()) return(jxfield);
				jxfield.bind(this,jxse);
				fields.put(p_fieldname,jxfield);
				return(jxfield);
			} else return(jxfield);
		} else {
			if(jxfield != null) return jxfield;
			jxfield = new JxField(p_fieldname);
			fields.put(p_fieldname,jxfield);
			return(jxfield);
		}
	}

	public JxField jxAdd(String p_fieldname) {
		return(jxAdd(p_fieldname,p_fieldname));
	}

	public JxField jxAdd(String p_fieldname,int p_type)
	{
		return(jxAdd(p_fieldname,p_fieldname,p_type,0,null));
	}
	public JxField jxAdd(String p_fieldname,int p_type,int p_len)
	{
		return(jxAdd(p_fieldname,p_fieldname,p_type,p_len,null));
	}
	public JxField jxAdd(String p_fieldname,int p_type,String p_mask)
	{
		return(jxAdd(p_fieldname,p_fieldname,p_type,0,p_mask));
	}

	public JxField jxAdd(String p_fieldname,String p_skinname,int p_type)
	{
		return(jxAdd(p_fieldname,p_skinname,p_type,0,null));
	}
	public JxField jxAdd(String p_fieldname,String p_skinname,int p_type,int p_len)
	{
		return(jxAdd(p_fieldname,p_skinname,p_type,p_len,null));
	}
	public JxField jxAdd(String p_fieldname,String p_skinname,int p_type,String p_mask)
	{
		return(jxAdd(p_fieldname,p_skinname,p_type,0,p_mask));
	}

	private JxField jxAdd(String p_fieldname,String p_skinname,int p_type,int p_len,String p_mask)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname,p_skinname)) == null) return(null);
		jxfield.setFormat(p_type,p_len,p_mask);
		return(jxfield);
	}


	public void jxSetText(String p_fieldname,String p_text)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setText(p_text);
	}

	public void jxSetHint(String p_fieldname,String p_text)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setHint(p_text);
	}

	public String jxGetText(String p_fieldname)
	{
		JxField jxfield;
		if(isbinded() != true) return(null);
		if((jxfield = jxAdd(p_fieldname)) == null) return(null);
		return(jxfield.getText());
	}

	public void jxSetEnable(String p_fieldname,boolean p_flag)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setEnable(p_flag);
	}

	public void jxSetVisible(String p_fieldname,boolean p_visibleFlag)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setVisible(p_visibleFlag);
	}

	public void jxSetFocus(String p_fieldname)
	{
		JxField jxfield;
		if(!fisbinded) return;
		if(p_fieldname == null || p_fieldname.trim() == "") {
			setFocus(null);
			return;
		}
		if((jxfield = jxAdd(p_fieldname)) == null) {
			setFocus(p_fieldname);
		} else {
			setFocus(jxfield.getSkinName());
		}
	}

	public void jxSetFontColor(String p_fieldname,int p_color)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setFontColor(p_color);
	}
	
	public void showForm()
	{
		if(!fisbinded) return;
		myskin.showForm();
	}

	public void hideForm()
	{
		if(!fisbinded) return;
		myskin.hideForm();
	}
	public boolean isFormVisible()
	{
		if(!fisbinded) return false;
		return(myskin.isFormVisible());
	}
/*
	public void topForm()
	{
		if(!fisbinded) return;
	}
*/
	public void enableForm()
	{
		if(!fisbinded) return;
		myskin.enableForm();
	}

	public void disableForm()
	{
		if(!fisbinded) return;
		myskin.disableForm();
	}

	/*
	public void modalForm(JxForm owner)
	{
		if(!fisbinded) return;
		myskin.modalForm(owner.getSkin());
	}
	*/

	public String getModalResult()
	{
		return(null);
	}

	public void closeForm()
	{
		if(!fisbinded) return;
		myskin.closeForm();
	}

	public String createForm(String p_formname,String p_instanceName)
	{
		if(!fisbinded) return(null);
		return(gadgetProvider.jxCreateForm(p_formname,p_instanceName,null));
	}
	public String createForm(String p_formname,String p_instanceName,String p_dfm)
	{
		if(!fisbinded) return(null);
		return(gadgetProvider.jxCreateForm(p_formname,p_instanceName,p_dfm));
	}

	public void destroyForm(String p_instanceName)
	{
		if(!fisbinded) return;
		gadgetProvider.jxDestroyForm(p_instanceName);
	}

	public boolean isbinded()
	{
		return(fisbinded);
	}
	private void setSkin(JxSkin p_skin)
	{
		myskin = p_skin;
		fisbinded = true;
	}
	public JxSkin getSkin()
	{
		return(myskin);
	}
	
	protected void afterBindOneField(JxSkinElement skinElement,boolean bindOK) {
		
	}
	public void bind(JxSkin p_skin)
	{
		if(isbinded() == true) return;
		Enumeration fieldlist = p_skin.getFields();
		if(fieldlist == null) return;
		setSkin(p_skin);
		jxformname = p_skin.getName();
		while(fieldlist.hasMoreElements()) {
			JxSkinElement jxse = (JxSkinElement)fieldlist.nextElement();
			JxField jxfield = getJxField(jxse.getName());
			if(jxfield != null) 
				jxfield.bind(this,jxse);
			//else System.err.println("field " + jxse.getName() + " not found");
			afterBindOneField(jxse, jxfield != null);
		}
		gadgetProvider = p_skin.getGadgetProvider();
		beforeBind();
		afterBind();
		jxThreadPause();
	}
	
	protected void beforeBind()
	{
	}
	protected void afterBind()
	{
	}
	public String openDialog(String p_dir,String p_filter)
	{
		return(gadgetProvider == null ? null : gadgetProvider.jxOpenDialog(p_dir,p_filter));
	}
	public String saveDialog(String p_name)
	{
		return(gadgetProvider == null ? null : gadgetProvider.jxSaveDialog(p_name));
	}
	public boolean printDialog()
	{
		return(gadgetProvider == null ? false : ((JxPrintDlgInterface)gadgetProvider).printDialog());
	}
	public boolean confirm(String p_msg)
	{
		return(gadgetProvider == null ? false : gadgetProvider.jxConfirm(p_msg,null));
	}
	public boolean confirm(String p_msg,MessageBoxActionInterface p_action)
	{
		return(gadgetProvider == null ? false : gadgetProvider.jxConfirm(p_msg,p_action));
	}
	public void notifyMsg(String p_msg){
		notifyMsg(p_msg, 1, 3000);
	}
	public void notifyWarnMsg(String p_msg){
		notifyMsg(p_msg, 2, 3000);
	}
	public void notifyErrMsg(String p_msg){
		notifyMsg(p_msg, 3, 3000);
	}
	public void notifyMsg(String p_msg, int p_type, int p_dur)
	{
		if(gadgetProvider != null)
			gadgetProvider.jxNotifyMsg(p_msg, p_type, p_dur);
	}
	public void messageBox(String p_msg)
	{
		if(gadgetProvider != null)
			gadgetProvider.jxMessageBox(p_msg,0,null);
	}
	public void messageBox(String p_msg,int p_type,MessageBoxActionInterface p_action)
	{
		if(gadgetProvider != null)
			gadgetProvider.jxMessageBox(p_msg,p_type,p_action);
	}
	public int messageBoxWithReturn(Vector v)
	{
		if(gadgetProvider != null)
			return(gadgetProvider.jxMessageBoxWithReturn(v));
		else
			return(-1);
	}
	/*
	public void run()
	{
		if(myskin == null) return;        
		if(gadgetProvider != null)
			gadgetProvider.runApp(myskin.getControl());
	}
	*/
   public static void main(String[] args) {
		UniLog.log("new JxForm() ...");
	   JxForm form = new JxForm();
		UniLog.log("done");
	}
   public int isActionAllowed(String action) { 
		return(0);
	}
	public void setUserData(String p_name,Object p_object)
	{
		gadgetProvider.setUserData(p_name,p_object);	
	}
	public Object getUserData(String p_name)
	{
		if(!fisbinded) return(null);
		return(gadgetProvider.getUserData(p_name));
	}
	public String getName()
	{
		if(!fisbinded) return(null);
		return(jxformname);
	}
	public void tileChildForm()
	{
		if(!fisbinded) return;
		myskin.tileChildForm();
	}
	public void cascadeChildForm()
	{
		if(!fisbinded) return;
		myskin.cascadeChildForm();
	}
	public void arrangeChildForm()
	{
		if(!fisbinded) return;
		myskin.arrangeChildForm();
	}
	public String getActiveChildForm()
	{
		if(!fisbinded) return(null);
		return(myskin.getActiveChildForm());
	}
	public void setTitle(String p_string)
	{
		if(!fisbinded) return;
		myskin.setTitle(p_string);
	}
	public void setFormStyle(String p_string)
	{
		if(!fisbinded) return;
		myskin.setFormStyle(p_string);
	}

	private JxActionListener buttonConfirmCallback = new JxActionListener()
	{
		public void actionPerformed(JxField fd) {
			UniLog.log("Confirm Pressed");
			buttonPress = 1;
			jxThreadPause();
		}
	};

	private JxActionListener buttonAbortCallback = new JxActionListener()
	{
		public void actionPerformed(JxField fd) {
			UniLog.log("Abort Pressed");
			buttonPress = 2;
			jxThreadPause();
		}
	};

	private JxActionListener buttonPrevrecCallback = new JxActionListener()
	{
		public void actionPerformed(JxField fd) {
			UniLog.log("pb_prevrec Pressed");
			buttonPress = 3;
			jxThreadPause();
		}
	};

	private JxActionListener buttonNextrecCallback = new JxActionListener()
	{
		public void actionPerformed(JxField fd) {
			UniLog.log("pb_nextrec Pressed");
			buttonPress = 4;
			jxThreadPause();
		}
	};


	public int jxTM_SetNextAction(int p_action)
	{
		if(tmThread != null) {
			synchronized(tmThread) {
				nextAction = p_action;
				return(0);
			}
		} else return(-1);
	}
	private void jxThreadPause()
	{
		if(tmThread == null) return;
		synchronized(tmThread) {
			tmThread.tmThreadSwitch=true;
			for(;;) {
				UniLog.logClass(this,"JX_thread for " + jxformname + " Daemon Pending " + tmThread.state);
				if(state <= -9) {
					closeForm();
					tmThread = null;
					break;
				}
				try {
					tmThread.notify();
					tmThread.wait(5000);
				} catch (Exception ex) {
					UniLog.log(ex);
					return;
				} 
				if(!tmThread.tmThreadSwitch) return;
			}
		}
	}

	protected abstract class JxFieldSelect extends JxFieldSelectBase {
		public JxFieldSelect(String fieldlist) {
		   super(getJxForm(), fieldlist);
		}
	}
	protected abstract class JxFieldAction extends JxFieldActionBase {
		public JxFieldAction(String fieldlist) {
		   super(getJxForm(), fieldlist);
		}
	}
	protected abstract class JxFieldChange extends JxFieldChangeBase {
		public JxFieldChange(String fieldlist) {
		   super(getJxForm(), fieldlist);
		}
	}
	protected abstract class JxFieldGridChange extends JxFieldGridChangeBase {
		public JxFieldGridChange(String fieldlist) {
		   super(getJxForm(), fieldlist);
		}
	}
	protected abstract class JxColumnClick extends JxColumnClickBase {
		public JxColumnClick(String fieldlist) {
		   super(getJxForm(), fieldlist);
		}
	}
	protected abstract class JxGetDataByIdx extends JxGetDataByIdxBase {
		public JxGetDataByIdx(String fieldlist) {
		   super(getJxForm(), fieldlist);
		}
	}
	protected abstract class JxDblClick extends JxDblClickBase {
		public JxDblClick(String fieldlist) {
		   super(getJxForm(), fieldlist);
		}
	}
	protected abstract class JxDropTarget extends JxDropTargetBase {
		public JxDropTarget(String fieldlist) {
		   super(getJxForm(), fieldlist);
		}
	}
	protected abstract class JxFieldDrag extends JxFieldDragBase {
		public JxFieldDrag(String fieldlist) {
		   super(getJxForm(), fieldlist);
		}
	}
	protected abstract class JxFormClose extends JxFormCloseBase {
		public JxFormClose() {
		   super(getJxForm());
		}
	}
	public void addFormCloseListener(JxFormCloseListener p_listener)
	{	
		if(myskin != null) {
			myskin.addFormCloseListener(p_listener);
		}
	}

	public void setFocus()
	{
		if(!fisbinded) return;
		myskin.setFocus();
	}
	public void setFocus(String p_fieldname)
	{
		if(!fisbinded) return;
		/*
		myskin.setFocus(p_fieldname);
		*/
		UniLog.log("set Focus " + jxformname + "." + p_fieldname);
		nextFocusField = p_fieldname;
	}

	public String nextFocus()
	{
		String s;
		s = nextFocusField;
		UniLog.log("clear Focus " + jxformname + "." + nextFocusField);
		nextFocusField = null;
		return(s);
	}

	/*
	public void afterAdd(boolean p_flag)
	{
		UniLog.log("afteradd " + p_flag);
		if(p_flag) jxAfterAdd();
	}

	public void afterUpdate(boolean p_flag)
	{
		UniLog.log("afterupdate" + p_flag);
		if(p_flag) jxAfterUpdate();
	}
	*/

	public void jxAfterAdd()
	{
	}
	public void jxAfterUpdate()
	{
	}
	public boolean jxAfterEditAdd()
	{
		return(true);
	}
	public boolean jxAfterEditUpdate()
	{
		return(true);
	}
	public void jxAfterDisplay()
	{
	}
	public void modalForm()
	{
		if(!fisbinded) return;
		myskin.modalForm();
	}
	public void modalFormWithCallback()
	{
		if(!fisbinded) return;
		myskin.modalFormWithCallback(nextFocus());
	}
	public JxForm getForm(String p_formname)
	{
		/*
		if(gadgetProvider == null) return(null);
		JxForm fm = gadgetProvider.jxGetForm(p_formname);
		if(fm == null) {
			UniLog.log("Try Creating Form " + p_formname);
			String s = createForm(p_formname,p_formname,"Uniform");
			if(s == null || !s.equals("OK")) {
					UniLog.log("create jxForm Failed");
					return(null);
			}
			fm = getForm(p_formname);
		}
		return(fm);
		*/
		return(gadgetProvider.getOrCreateForm(p_formname));
//		return(gadgetProvider.jxGetForm(p_formname));
	}

	/*
	public void jxSetItemList(String p_fieldname,Vector p_itemlist,Vector labelList)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setItemList(p_itemlist,labelList);
	}
	*/

	public void jxSetItemList(String p_fieldname,List p_itemlist)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setItemList(p_itemlist);
	}
	public void jxSetItemListInterface(String p_fieldname,AbstractGetItemProperty p_interface)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setItemListInterface(p_interface);
	}

	/*
	public void jxSetItemList(String p_fieldname,String p_item)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.addItemToList(p_item);
	}
	*/

	public InetAddress getRemoteAddress()
	{
		if(gadgetProvider == null)
			return(null);
		return(gadgetProvider.jxRemoteAddress());
	}

	public String jxGetRemoteAddress()
	{
		String ipAddress;
		if(getRemoteAddress().getHostAddress() == null) 
			ipAddress = null;
		else 
			ipAddress = "OK  " + getRemoteAddress().getHostAddress();  
		return(ipAddress);
	}

	public void delayClick(String p_formname,String p_buttonname)
	{
		if(gadgetProvider == null) return;
		gadgetProvider.delayClick(p_formname,p_buttonname);
	}

	public JxField getCurFocusField()
	{
		if(!fisbinded) return(null);
		return((JxField)fields.get(curFocusField));
	}
	public int jxGetInt(String p_fieldname)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return(0);
		return(jxfield.getInt());
	}
	public void jxAbortTM()
	{
		if(tmThread != null) {
			state = -1;
			jxThreadPause();
		}
	}
	public void jxExecuteTM()
	{
		if(tmThread != null) {
			buttonPress = 1;
			jxThreadPause();
		}
	}
	public int jxGetTMstate()
	{
		if(tmThread != null) {
			return(state);
		} else return(-10);
	}
	public Vector jxGetSelectedIndexes(String p_fieldname)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return(null);
		return(jxfield.getSelectedIndexes());
	}
	public void jxSetSelectedIndexes(String p_fieldname,Vector v)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setSelectedIndexes(v);
		return;
	}
	public Vector jxGetSelectList(String p_fieldname)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return(null);
		return(jxfield.getSelectList());
	}
	public void jxSetSelectList(String p_fieldname,Vector v)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setSelectList(v);
		return;
	}
	
	public int jxPutFile(String p_inputfile,String p_destfile)
	{
		int cc;
		InputStream input = null;
		if (gadgetProvider == null ) return(-1);
		try {
			input = new FileInputStream(p_inputfile);
			cc = gadgetProvider.jxPutFile(input,p_destfile);
			input.close();
		} catch (Exception ex) {
			UniLog.log(ex);
			return(-1);
		}
		return(cc);
	}

	public int jxPutFile(InputStream p_input,String p_destfile)
	{
		return(gadgetProvider == null ? -1 : gadgetProvider.jxPutFile(p_input,p_destfile));
	}
	public int jxPutFile(DataInput p_input,String p_destfile)
	{
		return(gadgetProvider == null ? -1 : gadgetProvider.jxPutFile(p_input,p_destfile));
	}
	public int jxPutFile(InputStream p_input,String p_destfile,int p_kbyte,JxUpdateProgress p_interface)
	{
		return(gadgetProvider == null ? -1 : gadgetProvider.jxPutFile(p_input,p_destfile,p_kbyte,p_interface));
	}
	public int jxGetFile(String p_outputfile,String p_sourcefile)
	{
		int cc;
		OutputStream output = null;
		if (gadgetProvider == null ) return(-1);
		try {
			output = new FileOutputStream(p_outputfile);
			cc = gadgetProvider.jxGetFile(output,p_sourcefile);
			output.close();
		} catch (Exception ex) {
			UniLog.log(ex);
			return(-1);
		}
		return(cc);
	}
	public int jxGetFile(OutputStream p_output,String p_sourcefile)
	{
		return(gadgetProvider == null ? -1 : gadgetProvider.jxGetFile(p_output,p_sourcefile));
	}
	public int jxGetFile(DataOutput p_output,String p_sourcefile)
	{
		return(gadgetProvider == null ? -1 : gadgetProvider.jxGetFile(p_output,p_sourcefile));
	}
	public int jxGetFile(OutputStream p_output,String p_sourcefile,int p_kbyte,JxUpdateProgress p_interface)
	{
		return(gadgetProvider == null ? -1 : gadgetProvider.jxGetFile(p_output,p_sourcefile,p_kbyte,p_interface));
	}
	public String jxGetTempFile(String p_prefix)
	{
		return(gadgetProvider == null ? null : gadgetProvider.jxGetTempFile(p_prefix));
	}
	public int jxUnlink(String p_path)
	{
		return(gadgetProvider == null ? -1 : gadgetProvider.jxUnlink(p_path));
	}
	public int jxShellExecute(String p_operation,String p_path,String p_parameter,String p_dir)
	{
		return(gadgetProvider == null ? -1 : gadgetProvider.jxShellExecute(p_operation,p_path,p_parameter,p_dir));
	}
	public JxForm getJxForm() {
	   return(this);
	}
	public JxGadgetProvider getProvider()
	{
		return(gadgetProvider);
	}
	public String jxNewTempDir(String p_prefix)
	{
		return(gadgetProvider == null ? null : gadgetProvider.jxNewTempDir(p_prefix));
	}
	public int jxMakeDir(String p_path)
	{
		return(gadgetProvider == null ? -1 : gadgetProvider.jxMakeDir(p_path));
	}
	public int jxRemoveDir(String p_path)
	{
		return(gadgetProvider == null ? -1 : gadgetProvider.jxRemoveDir(p_path));
	}
	public int jxChnftr(Vector v)
	{
		return(gadgetProvider == null ? -1 : gadgetProvider.jxChnftr(v));
	}
	public int jxSaveFileToTif(String p_imgfile,String p_imgtype,String p_tiffname,String p_append_or_write,String p_colormode,int resolution)
	{
		return(gadgetProvider == null ? -1 : gadgetProvider.jxSaveFileToTif(
							p_imgfile,
							p_imgtype,
							p_tiffname,
							p_append_or_write,
							p_colormode,
							resolution));
	}
	public Enumeration getFields() {
		return(myskin.getFields());
	}
	public void jxBeep()
	{
		myskin.beep();
	}
	public void setConnectionDebug(boolean p_sw)
	{
		if(gadgetProvider != null)
			gadgetProvider.setDebug(p_sw);
	}
	public void setCellCheck(JxCellUpdateInterface p_interface)
	{
		cellCheck = p_interface;
	}
	public long jxGetFileSize(String p_path)
	{
		return(gadgetProvider == null ? -1 : gadgetProvider.jxGetFileSize(p_path));
	}

	public boolean jxVerifyTM(int p_state,int p_mode)
	{
		return(true);
	}

	public void jxTM_perform(int p_action)
	{
		buttonPress = p_action;
		jxThreadPause();
	}
	public void afterEvent()
	{
	}

	public void jxGridSetCol(String p_fieldname,int p_col)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.gridSetCol(p_col);
		return;
	}
	public void jxGridSetColHeader(String p_fieldname,int p_col,String p_header)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.gridSetColHeader(p_col,p_header);
		return;
	}
	public void jxGridSetRow(String p_fieldname,int p_row)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.gridSetRow(p_row);
		return;
	}
	public void jxGridSetValue(String p_fieldname,Vector p_v)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
//		jxfield.gridSetValue(p_v);
		return;
	}
	public void jxGridSetValue(String p_fieldname,int p_col,int p_row,String p_value)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.gridSetValue(p_col,p_row,p_value);
		return;
	}
	public void jxGridAppendRow(String p_fieldname)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.appendrow();
		return;
	}
	public void jxGridSetColWidth(String p_fieldname,int p_col,int p_width)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.gridSetColWidth(p_col,p_width);
		return;
	}
	public int jxGridGetCurrentRow(String p_fieldname)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return(-1);
		return(jxfield.getCurrentRow());
	}
	public Object jxGridGetValue(String p_fieldname,int p_col,int p_row)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return(null);
		return(jxfield.gridGetValue(p_col,p_row));
	}
	public void jxGridSetCurrentRow(String p_fieldname,int p_row)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.gridSetCurrentRow(p_row);
	}
	public void jxGridSetRowValues(String p_fieldname,int p_col,int p_row,Vector p_v)
	{
		JxField jxfield;
		int i,col;
		Vector v = new Vector();
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		for(col=p_col,i = 0;i<p_v.size();i++,col++) {
			v.add(new Integer(col));
			v.add(new Integer(p_row));
			v.add(p_v.get(i));
		}
		jxfield.gridSetValue(v);
		return;
	}
	/*
	public void jxGridSetRowValues(String p_fieldname,int p_col,int p_row,Vector v)
	{
		JxField jxfield;
		int i,col;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		for(col=p_col,i = 0;i<v.size();i++,col++) {
			
			jxfield.gridSetValue(col,p_row,(String) v.get(i));
		}
		return;
	}
	*/

	public int wxform_rpccall_noreturn(String p_callseg,Vector arglist) throws Exception
	{
		//UniLog.log("wxform_rpccall_noreturn " + p_callseg);
		gadgetProvider.getConn().setReturnFlag(false);
		gadgetProvider.getConn().callSegmentWithException(p_callseg,arglist);
		/*
		if(p_callseg.equals("wx_setfield"))
			pvdr.getConn().callSegment(p_callseg,arglist);
			*/
		gadgetProvider.getConn().setReturnFlag(true);
		return(0);
	}

	public String wxform_rpccall(String p_callseg,Vector arglist)
	{
		UniLog.log("wxform_rpccall" + p_callseg);
		Value v = gadgetProvider.getConn().callSegment(p_callseg,arglist);
		if(v != null) {
			return(v.toString());
		} else return(null);
	}


	public String getCurFocusFieldName()
	{
		return(curFocusField);
	}

	public void setCurFocusFieldName(String p_name)
	{
		curFocusField = p_name;
		nextFocus();
	}

	public String selFile(Vector arglist)
	{
		Value v = gadgetProvider.getConn().callSegment("wx_selfile",arglist);
		if(v != null) {
			return(v.toString());
		} else return(null);
	}

/**********************/
	private void setCell(HSSFRow p_row, short p_col, Object p_val)
	{
		HSSFCell cell = p_row.createCell(p_col);
		if(hssf_cellStyle != null) cell.setCellStyle(hssf_cellStyle);
//		cell.setEncoding(HSSFCell.ENCODING_UTF_16);
		if(p_val instanceof String) cell.setCellValue((String) p_val);
		else
			if(p_val instanceof Integer) 
				cell.setCellValue(((Integer) p_val).intValue());
			else
				if(p_val instanceof Double) 
					cell.setCellValue(((Double) p_val).doubleValue());
				else
					if(p_val instanceof java.util.Date) 
						cell.setCellValue((java.util.Date)  p_val);
		
	}

	public String excel_MergeCells(int p_r1,int p_c1,int p_r2,int p_c2)
	{
		if(sheet == null) return("FAIL WorkBook Not Opened");
//		sheet.addMergedRegion(new Region(p_r1,(short) p_c1,p_r2,(short) p_c2));
		sheet.addMergedRegion(new CellRangeAddress(p_r1, p_r2, p_c1, p_c2));
		return("OK");
	}
	
	public int excel_getStyleGen(String p_format,String p_fillColor,String p_fontStyle,String p_fontColor,String p_locked)
	{
		/*
		Short sidx = excel_formatStyle.get(p_format);
		if(sidx != null) return(sidx.intValue());
		HSSFDataFormat df = workBook.createDataFormat();
		int dfIdx = df.getFormat(p_format);
		if(dfIdx < 0) return(-1);
		HSSFCellStyle stl = workBook.createCellStyle();
		stl.setDataFormat((short) dfIdx);
		excel_formatStyle.put(p_format,new Short(stl.getIndex()));
		return(stl.getIndex());
		*/
		String ss = "";
		String sFormat;
		String sFillColor;
		String sFontStyle;
		String sFontColor;
		String sLocked;
		if(p_format == null) sFormat = "General"; else sFormat = p_format;
		if(p_fillColor == null) sFillColor = "AUTOMATIC"; else sFillColor = p_fillColor;
		if(p_fontStyle == null) sFontStyle = "Normal"; else sFontStyle = p_fontStyle;
		if(p_fontColor == null) sFontColor = "Black"; else sFontColor = p_fontColor;
		if(p_locked == null) sLocked = "unlocked"; else sLocked = p_locked;
		Short sidx = excel_cellStyle.get(sFormat+","+sFillColor+","+sFontStyle+","+sFontColor+","+sLocked);
		if(sidx != null) return(sidx.intValue());
		HSSFDataFormat df = workBook.createDataFormat();
		int dfIdx = df.getFormat(sFormat);
		if(dfIdx < 0) return(-1);
		HSSFCellStyle stl = workBook.createCellStyle();
		stl.setDataFormat((short) dfIdx);
		Short colIdx = excel_indexedColorMap.get(sFillColor);
		if(colIdx != null) {
			stl.setFillForegroundColor((short) colIdx.intValue());
			if(!sFillColor.equals("AUTOMATIC")) stl.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		}
		Short fidx = excel_font.get(sFontStyle+","+sFontColor);
		if(fidx == null) {
			HSSFFont ft = workBook.createFont();
			if(sFontColor.equals("red")) ft.setColor(HSSFFont.COLOR_RED); else ft.setColor(HSSFFont.COLOR_NORMAL);
			if(sFontStyle.contains("Bold")) ft.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD); else ft.setBoldweight(HSSFFont.BOLDWEIGHT_NORMAL);
			if(sFontStyle.contains("Italic")) ft.setItalic(true); else ft.setItalic(false);
			stl.setFont(ft);
			excel_font.put(sFontStyle+","+sFontColor,ft.getIndex());
		} else {
			stl.setFont( workBook.getFontAt((short) fidx.intValue()));
		}
		
//		stl.setVerticalAlignment(org.apache.poi.ss.usermodel.VerticalAlignment.CENTER);
		stl.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		
//		excel_cellStyle.put(p_format,new Short(stl.getIndex()));
		return(stl.getIndex());
		
	}
	public int excel_getFormatStyle(String p_format)
	{
		return(excel_getStyleGen(p_format,null,null,null,null));
	}
	
	String makeHSSFCellStyleStringHash(HSSFCellStyle p_style) {
		String s = "";
		String ss = getFormatFromStyle(p_style) ;
		if( ss == null ) return(null);
		s += ss;
		ss = getFillColorFromStyle(p_style) ;
		if( ss == null ) return(null);
		s += "," + ss;
		int fontIdx = p_style.getFontIndex();
		HSSFFont ft = workBook.getFontAt((short) fontIdx);
		if(ft == null) {
			UniLog.log("makeHSSFCellStyleStringHash getFont got null");
			return(null);
		}
		ss = getStyleFromFont(ft) ;
		if( ss == null ) return(null);
		s += "," + ss;
		ss = getColorFromFont(ft) ;
		if( ss == null ) return(null);
		s += "," + ss;
		ss = getLockedFromStyle(p_style) ;
		if( ss == null ) return(null);
		s += "," + ss;
		return(s);
	}
	
	void cacheOneCellStyleAndFont(HSSFCellStyle sty) {
		int fontIdx = sty.getFontIndex();
		HSSFFont ft = workBook.getFontAt((short) fontIdx);
		UniLog.log("cache CellStyle " + sty.getIndex() + " fontidx " + fontIdx + " key " +  makeHSSFCellStyleStringHash(sty));
		String sh = makeHSSFCellStyleStringHash(sty);
		if(sh != null ) {
			if(excel_cellStyle.get(sh) == null) {
				String ss = getStyleFromFont(ft)+","+getColorFromFont(ft);
				Short fidx = excel_font.get( ss );
				if(fidx == null) {
					UniLog.log("add HSSFFont " +  ss + " idx " + fontIdx );
					excel_font.put(ss, new Short(ft.getIndex()));
				}
				UniLog.log("add CellStyleHash " +  sh + " idx " + sty.getIndex());
				excel_cellStyle.put( sh, sty.getIndex());
			}
		}
		
	}
	
	void excel_initFormatStyle()
	{
			excel_cellStyle = new Hashtable<String,Short>();
			excel_font = new Hashtable<String,Short>();
			
			if(excel_indexedColorMap == null) {
				excel_indexedColorMap= new Hashtable<String,Short>();
				for(int i = 0;i<IndexedColors.values().length;i++) {
					IndexedColors ic = IndexedColors.values()[i];
					UniLog.log("IndexedColors " + i + " : " + ic.name() + " : " + ic.getIndex());
					excel_indexedColorMap.put(ic.name(), ic.getIndex());
				}
			}
			cacheOneCellStyleAndFont(workBook.getCellStyleAt((short) 0)); // assume CellStyle(0) as default;
			/*
			if(sheet != null) {
				HSSFRow row = row = sheet.getRow(0);
				if(row != null) {
					cacheOneCellStyleAndFont( row.getRowStyle() ) ; // add CellStyle at row 0 for header style
					HSSFCell cell = row.getCell(0);
					if(cell != null) {
						cacheOneCellStyleAndFont( cell.getCellStyle() ) ; // add CellStyle at row 0, col 0 for header style
					}
				}
				
			}
			*/
			
			/*
			int n = workBook.getNumberOfFonts();
			for(int i = 0;i<n;i++) {
				HSSFFont ft = workBook.getFontAt((short) i);
				String s = getStyleFromFont(ft);
				s += ",";
				s += getColorFromFont(ft);
				HSSFColor hcol = ft.getHSSFColor(workBook);
				UniLog.log("got HSSFFont " + i + " " + ft.getIndex() + " type " + ft.getFontName() + " pt " + ft.getFontHeightInPoints() + " HSSFcolor " + hcol.getIndex() + " key " +  s);
				if(excel_font.get(s) == null) {
					UniLog.log("add HSSFFont " +  s);
					excel_font.put(s, new Short(ft.getIndex()));
				}
			}
			n = workBook.getNumCellStyles();
			for(int i = 0;i<n;i++) {
				HSSFCellStyle sty = workBook.getCellStyleAt((short) i);
				if(sty != null) {
					int fontIdx = sty.getFontIndex();
					HSSFFont ft = workBook.getFontAt((short) fontIdx);
					UniLog.log("got CellStyle " + sty.getIndex() + " fontidx " + fontIdx + " key " +  makeHSSFCellStyleStringHash(sty));
					String sh = makeHSSFCellStyleStringHash(sty);
					if(sh != null ) {
					if(excel_cellStyle.get(sh) == null) {
						UniLog.log("add CellStyleHash " +  sh);
						excel_cellStyle.put( sh, sty.getIndex());
					}
					}
				} else break;
			}
			*/
	}	
	String getFormatFromStyle(HSSFCellStyle sty) {
		return(sty.getDataFormatString());
	}
	String getFillColorFromStyle(HSSFCellStyle sty) {
		int bgcol = sty.getFillForegroundColor();
		for(String key : excel_indexedColorMap.keySet()) {
			Short ic = excel_indexedColorMap.get(key);
			if(ic.intValue() == bgcol) return(key);
		}
		UniLog.log(" getFillColorFromStyle index " + bgcol + " unmapped");
		return(null);
	}
	String getStyleFromFont(HSSFFont ft) {
		String ss = "";
		if(ft.getBoldweight() == HSSFFont.BOLDWEIGHT_BOLD) ss += "Bold";
		if(ft.getItalic()) ss+= "Italic";
		if(ss.equals("")) ss = "Normal";
		return(ss);
	}
	String getColorFromFont(HSSFFont ft) {
		if(ft.getColor() == HSSFFont.COLOR_RED) return("red");else return("Black");
	}
	/*
	String getFontStyleFromStyle(HSSFCellStyle sty) {
		int fontIdx = sty.getFontIndex();
		HSSFFont ft = workBook.getFontAt((short) fontIdx);
		if(ft == null) {
			UniLog.log("makeHSSFCellStyleStringHash getFont got null");
			return(null);
		}
		String ss = "";
		if(ft.getBoldweight() == HSSFFont.BOLDWEIGHT_BOLD) ss += "bold";
		if(ft.getItalic()) ss+= "italic";
		if(ss.equals("")) ss = "normal";
		return(ss);
	}
	String getFontColorFromStyle(HSSFCellStyle sty) {
		int fontIdx = sty.getFontIndex();
		HSSFFont ft = workBook.getFontAt((short) fontIdx);
		if(ft == null) {
			UniLog.log("makeHSSFCellStyleStringHash getFont got null");
			return(null);
		}
		if(ft.getColor() == HSSFFont.COLOR_RED) return("red");else return("black");
	}
	*/
	String getLockedFromStyle(HSSFCellStyle sty) {
		if(sty.getLocked()) return("locked") ; else return("unlocked");
	}
	public String excel_getFormatFromStyleIdx(int p_idx) {
		int n = workBook.getNumCellStyles();
		if(p_idx < 0 || p_idx >= n) return(null);
		HSSFCellStyle sty = workBook.getCellStyleAt((short) p_idx);
		String s = getFormatFromStyle(sty);
		if(s != null) return("OK  "+s); else return("FAIL");
	}
	public String excel_getFillColorFromStyleIdx(int p_idx) {
		int n = workBook.getNumCellStyles();
		if(p_idx < 0 || p_idx >= n) return(null);
		HSSFCellStyle sty = workBook.getCellStyleAt((short) p_idx);
		String s = getFillColorFromStyle(sty);
		if(s != null) return("OK  "+s); else return("FAIL");
	}
	public String excel_getFontStyleFromStyleIdx(int p_idx) {
		int n = workBook.getNumCellStyles();
		if(p_idx < 0 || p_idx >= n) return(null);
		HSSFCellStyle sty = workBook.getCellStyleAt((short) p_idx);
		int fontIdx = sty.getFontIndex();
		HSSFFont ft = workBook.getFontAt((short) fontIdx);
		if(ft == null) {
			UniLog.log("makeHSSFCellStyleStringHash getFont got null");
			return(null);
		}
		String s = getStyleFromFont(ft);
		if(s != null) return("OK  "+s); else return("FAIL");
	}
	public String excel_getFontColorFromStyleIdx(int p_idx) {
		int n = workBook.getNumCellStyles();
		if(p_idx < 0 || p_idx >= n) return(null);
		HSSFCellStyle sty = workBook.getCellStyleAt((short) p_idx);
		int fontIdx = sty.getFontIndex();
		HSSFFont ft = workBook.getFontAt((short) fontIdx);
		if(ft == null) {
			UniLog.log("makeHSSFCellStyleStringHash getFont got null");
			return(null);
		}
		String s = getColorFromFont(ft);
		if(s != null) return("OK  "+s); else return("FAIL");
	}
	public String excel_getLockedFromStyleIdx(int p_idx) {
		int n = workBook.getNumCellStyles();
		if(p_idx < 0 || p_idx >= n) return(null);
		HSSFCellStyle sty = workBook.getCellStyleAt((short) p_idx);
		String s = getLockedFromStyle(sty);
		if(s != null) return("OK  "+s); else return("FAIL");
	}

	public String excel_OpenWorkBook(String p_pathname)
	{
		try {
			FileInputStream is = new FileInputStream(p_pathname);
			excel_OpenWorkBook(is);
			is.close();
			excel_initFormatStyle();
			return("OK");
		} catch (Exception ex) {
			UniLog.log(ex);
			return("FAIL");
		}
	}


	public void excel_OpenWorkBook(InputStream is) throws IOException
	{
		/*
		try {
			*/
	      workBook = new HSSFWorkbook(is);
			sheet = workBook.getSheetAt(0);
			excelSheetIdx = 0;
			excelSheetName = workBook.getSheetName(0);
			excelDefaultRowHeight = sheet.getDefaultRowHeight();
			excelCurrentRowHeight = excelDefaultRowHeight;
			excel_initFormatStyle();
			/*
		} catch (Exception ex) {
			UniLog.log(ex);
		}   
		*/
//		excelRowCount = sheet.getPhysicalNumberOfRows();
		excelRowCount = sheet.getLastRowNum()+1;
		if(excelRowCount < 1) sheet.createRow(0);
		excelNames = null;
	}
	public String excel_OpenWorkBook()
	{
      workBook = new HSSFWorkbook();
		sheet = workBook.createSheet();
		excelSheetIdx = 0;
		excelSheetName = workBook.getSheetName(0);
		excelRowCount = 0;
		excelDefaultRowHeight = sheet.getDefaultRowHeight();
		excelCurrentRowHeight = excelDefaultRowHeight;
		excel_initFormatStyle();
		return("OK");
	}

	public String excel_CloseWorkBook()
	{
		workBook = null;
		sheet = null;
		hssf_cellStyle = null;
		excelRowCount = 0;
		excelSheetIdx = -1;
		excelSheetName = null;
		excelNames = null;
		return("OK");
	}

	public String excel_setStringValue(int p_row, int p_col, String p_value)
	{
			HSSFRow row = null;
			String s;
//			UniLog.log("HAHA 2015 excel_setStringValue "+p_row+","+p_col+":["+p_value+"]");
			if(!excel_translateGTET) s = p_value; else s = excel_translateGTET(p_value);
			if(excel_translateChinese == 1) {
//				s = ChineseConvert.convertAuto2GSquare(s);
				s = ChineseConvert.convertB2G(ChineseConvert.convertG2B(s));
			} else {
				if(excel_translateChinese == 2) {
					s = ChineseConvert.convertAuto2BSquare(s);
				}
			}
			if(sheet == null) return("FAIL WorkBook Not Opened");
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = sheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = sheet.getRow(p_row);
			if(row == null) {
			/*
				UniLog.log("Get Row fail " + p_row);
				sheet.createRow(p_row);
				row = sheet.getRow(p_row);
				if(row == null) {
					UniLog.log("Create Row fail " + p_row);
					*/
					return("FAIL Get Row fail");
				/*
				}
				*/
			}
			HSSFCell cell = row.getCell( p_col);
			if(cell == null) {
				cell = row.createCell(p_col);
			}
			if(hssf_cellStyle != null) cell.setCellStyle(hssf_cellStyle);
//			cell.setEncoding(HSSFCell.ENCODING_UTF_16);
			cell.setCellValue(s);
			return("OK");
	}
	public String excel_setDateValue(int p_row, int p_col, java.util.Date p_javadate)
	{
			HSSFRow row = null;
			if(sheet == null) return("FAIL WorkBook Not Opened");
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = sheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = sheet.getRow(p_row);
			if(row == null) return("FAIL Get Row fail");
			HSSFCell cell = row.getCell( p_col);
			if(cell == null) {
				cell = row.createCell((short) p_col);
			}
			if(hssf_cellStyle != null) cell.setCellStyle(hssf_cellStyle);
//			cell.setEncoding(HSSFCell.ENCODING_UTF_16);
			/*
			Date tmpdate = p_javadate;
			cell.setCellValue(tmpdate);
			tmpdate = null;
			*/
			if(p_javadate != null) {
				cell.setCellValue(p_javadate);
			} else {
//				UniLog.log("HAHA 2017 setDateValue " + p_row + " : " + p_col + " null");
				cell.setCellValue((String) null);
			}	
			
			return("OK");
	}
	/*
	public String excel_setDateValue(int p_row, int p_col, int p_datevalue)
	{
			HSSFRow row = null;
			if(sheet == null) return("FAIL WorkBook Not Opened");
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = sheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = sheet.getRow(p_row);
			if(row == null) return("FAIL Get Row fail");
			HSSFCell cell = row.getCell((short) p_col);
			if(cell == null) {
				cell = row.createCell((short) p_col);
			}
			if(hssf_cellStyle != null) cell.setCellStyle(hssf_cellStyle);
//			cell.setEncoding(HSSFCell.ENCODING_UTF_16);
			Date tmpdate = new Date(p_datevalue);
			cell.setCellValue(tmpdate);
			tmpdate = null;
			return("OK");
	}
	*/
	public String excel_setNumericValue(int p_row, int p_col, double p_value)
	{
			HSSFRow row = null;
			if(sheet == null) return("FAIL WorkBook Not Opened");
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = sheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = sheet.getRow(p_row);
			if(row == null) return("FAIL Get Row fail");
			HSSFCell cell = row.getCell( p_col);
			if(cell == null) {
				cell = row.createCell((short) p_col);
//				if(hssf_cellStyle != null) cell.setCellStyle(hssf_cellStyle);
			}
			if(hssf_cellStyle != null) cell.setCellStyle(hssf_cellStyle);
//			cell.setEncoding(HSSFCell.ENCODING_UTF_16);
			cell.setCellValue(p_value);
			return("OK");
	}
	public String excel_setNumericValue(int p_row, int p_col, int p_value)
	{
			HSSFRow row = null;
			if(sheet == null) return("FAIL WorkBook Not Opened");
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = sheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = sheet.getRow(p_row);
			if(row == null) return("FAIL Get Row fail");
			HSSFCell cell = row.getCell( p_col);
			if(cell == null) {
				cell = row.createCell( p_col);
				if(hssf_cellStyle != null) cell.setCellStyle(hssf_cellStyle);
			}
//			cell.setEncoding(HSSFCell.ENCODING_UTF_16);
			cell.setCellValue(p_value);
			return("OK");
	}
	public String excel_InsertOneRow(Vector v)
	{
			if(sheet == null) return("FAIL WorkBook Not Opened");
			HSSFRow row = sheet.createRow(excelRowCount++);
			if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			for(int i = 0;i<v.size();i++) {
				setCell(row, (short) i , v.get(i));
			}
			return("OK");
	}
	public String excel_setValues(int p_row,int p_col,Vector v)
	{
			HSSFRow row = null;
			if(sheet == null) return("FAIL WorkBook Not Opened");
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = sheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = sheet.getRow(p_row);
			if(row == null) return("FAIL Get Row fail");
			for(int i = 0;i<v.size();i++) {
				setCell(row, (short) (i + p_col), v.get(i));
			}
			return("OK");
	}
	public String excel_setValuesWithStyle(int p_row,int p_col,Vector v)
	{
			HSSFRow row = null;
			if(sheet == null) return("FAIL WorkBook Not Opened");
			for(;excelRowCount <= p_row;excelRowCount++) {
				row = sheet.createRow(excelRowCount);
				if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
			}
			row = sheet.getRow(p_row);
			if(row == null) return("FAIL Get Row fail");
			for(int i = 0;i<v.size();i++) {
				int styleidx;
				if(v.get(i) instanceof Double)
						styleidx = (int) ((Double) v.get(i)).doubleValue();
					else
						styleidx = ((Integer) v.get(i)).intValue();
				if(hssf_cellStyle == null || 
						hssf_cellStyle.getIndex() != styleidx) {
						hssf_cellStyle = workBook.getCellStyleAt((short) styleidx);
				}
				i++;
				setCell(row, (short) (i / 2 + p_col), v.get(i));
			}
			return("OK");
	}

	public String excel_WriteWorkBook(String p_outfile)
	{
		try {
			FileOutputStream os = new FileOutputStream(p_outfile);
			workBook.write(os);
			os.close();
			return("OK");
		} catch (Exception ex) {
			return("FAIL");
		}
	}
	public void excel_WriteWorkBook(OutputStream os) throws Exception
	{
		workBook.write(os);
	}
	public String excel_newSheet(String p_sheetname) {
		if(workBook == null)
         return("FAILWorkBook is null");
		try {
   		HSSFSheet newsheet = workBook.createSheet(p_sheetname);
         return("OK");
      } catch (Exception ex) {
         return("FAIL");
		}
	}
	public String excel_renameSheet(String p_oldsheetname, String p_newsheetname) {
		if(workBook == null)
         return("FAILWorkBook is null");
		try {
			int idx = workBook.getSheetIndex(p_oldsheetname);
			if(idx < 0) {
				return("FAILSheet " + p_oldsheetname + " not found");
			}
//			workBook.setSheetName(idx, p_newsheetname,HSSFCell.ENCODING_UTF_16);
			workBook.setSheetName(idx, p_newsheetname);
			if(idx == excelSheetIdx) excelSheetName = p_newsheetname;
         return("OK");
      } catch (Exception ex) {
         return("FAIL");
		}
	}
	public String excel_removeSheet(String p_sheetname) {
		if(workBook == null)
         return("FAILWorkBook is null");
		try {
			int idx = workBook.getSheetIndex(p_sheetname);
			if(idx < 0) {
				return("FAILSheet " + p_sheetname + " not found");
			}
			workBook.removeSheetAt(idx);
			if(idx <= excelSheetIdx) {
				sheet = null;
				excelSheetName = null;
				excelSheetIdx = -1;
			}
         return("OK");
      } catch (Exception ex) {
         return("FAIL");
		}
	}
	public String excel_getSheet(String p_sheetname) {
		if(workBook == null)
         return("FAILWorkBook is null");
		try {
			int idx = workBook.getSheetIndex(p_sheetname);
			if(idx < 0) {
				return("FAILSheet " + p_sheetname + " not found");
			}
			sheet = workBook.getSheetAt(idx);
//      	excelRowCount = sheet.getPhysicalNumberOfRows();
      	excelRowCount = sheet.getLastRowNum()+1;
			if(excelRowCount < 1) sheet.createRow(0);
			excelSheetIdx = idx;
			excelSheetName = p_sheetname;
			UniLog.log("excel_getSheet " + p_sheetname+ " " + sheet.getLastRowNum() + " R " + sheet.getPhysicalNumberOfRows() + " H " + excelDefaultRowHeight);
			return("OK");
      } catch (Exception ex) {
         return("FAIL");
		}
	}

	public String excel_setColumnWidth(int p_col,int p_width)
	{
			if(sheet == null) return("FAIL WorkSheet Not Opened");
			sheet.setColumnWidth((short) p_col,(short) p_width);
			return("OK");
	}

	public String excel_isWriteProtected()
	{
			if(workBook == null) return("FAIL WorkBook Not Opened");
			boolean b = workBook.isWriteProtected();
			if(b ) return("OK  1"); else return("OK  0");
	}

	public String excel_protectWorkBook(boolean p_protected,String p_password,String p_username)
	{
			if(workBook == null) return("FAIL WorkBook Not Opened");
			if(p_protected) {
				workBook.writeProtectWorkbook(p_password,p_username);
			} else {
				workBook.unwriteProtectWorkbook();
			}
			return("OK");
	}

	public String excel_setProtected(boolean p_protected,String p_password)
	{
			UniLog.log("excel_setprotection " + p_protected);
			if(sheet == null) return("FAIL WorkSheet Not Opened");
/*
			sheet.setProtect(p_protected);
*/
			if(p_protected)
				sheet.protectSheet(p_password);
			else
				sheet.protectSheet(null);
			return("OK");
	}
	public String excel_setLock(boolean p_locked)
	{
		UniLog.log("excel_setLock " + p_locked);
		if(hssf_cellStyle == null) return("FAIL CellStyle Not Selected");
		if(p_locked) {
			UniLog.log("set excel style to locked");
			hssf_cellStyle.setLocked(true);
		} else {
			UniLog.log("set excel style to unlocked");
			hssf_cellStyle.setLocked(false);
		}
		return("OK");
	}
	public String excel_setDefaultRowHeight(int p_height)
	{
		if(p_height <= 0) 
			excelCurrentRowHeight = excelDefaultRowHeight;
		else
			excelCurrentRowHeight = p_height;
		return("OK");
	}
	public String excel_setCellColor(int p_color)
	{
		UniLog.log("excel_setCellColor " + p_color);
		if(hssf_cellStyle == null) return("FAIL CellStyle Not Selected");
//		hssf_cellStyle.setFillBackgroundColor((short) p_color);
		return("OK");
	}

	public String excel_createCellStyle()
	{
		if(workBook == null) return("FAIL WorkBook Not Opened");
		hssf_cellStyle = workBook.createCellStyle();
		if(hssf_cellStyle == null) return("FAIL Fail to Create CellStyle");
		return("OK  " + hssf_cellStyle.getIndex());
	}
	public String excel_setCellStyle(int p_idx)
	{
		if(workBook == null) return("FAIL WorkBook Not Opened");
		if(p_idx < 0) {
			hssf_cellStyle = null;
			return("OK");
		}
		if(p_idx >= workBook.getNumCellStyles()) return("FAIL CellStyle Not Created");
		hssf_cellStyle = workBook.getCellStyleAt((short) p_idx);
		return("OK");
	}

	public String excel_getCurrentSheetName()
	{
		if(sheet == null) return("FAIL WorkSheet Not Opened");
		return("OK  " + excelSheetName);
	}
	public String excel_getCurrentSheetIdx()
	{
		if(sheet == null) return("FAIL WorkSheet Not Opened");
		return("OK  " + excelSheetIdx);
	}

	public String excel_getDefaultRowHeight()
	{
		if(sheet == null) return("FAIL WorkSheet Not Opened");
		return("OK  " + excelDefaultRowHeight);
	}
	public String excel_setRowHeight(int p_row,int p_height)
	{
		HSSFRow row = null;
		if(sheet == null) return("FAIL WorkSheet Not Opened");
		for(;excelRowCount <= p_row;excelRowCount++) {
			row = sheet.createRow(excelRowCount);
			if(excelDefaultRowHeight != excelCurrentRowHeight) row.setHeight((short) excelCurrentRowHeight);
		}
		if(p_height != excelCurrentRowHeight) {
			row.setHeight((short) p_height);
		}
		return("OK");
	}
	public String excel_getStringValue(int p_row,int p_col)
	{
		HSSFRow row = null;
		if(sheet == null) return("FAIL WorkSheet Not Opened");
		if(p_row > sheet.getLastRowNum()) return("OK  ");
//		if(p_row >= sheet.getPhysicalNumberOfRows()) return("OK  ");
		row = sheet.getRow(p_row);
		if(row == null) return("OK  ");
		HSSFCell cell = row.getCell( p_col);
		if(cell == null) return("OK  ");
		switch(cell.getCellType()) {
		case HSSFCell.CELL_TYPE_STRING: {
				if(excel_filterNonPrintable) {
						return("OK  "+stripNonPrintable(cell.getStringCellValue()));
				} else {
						return("OK  "+cell.getStringCellValue());
				}
			}
		case HSSFCell.CELL_TYPE_FORMULA:
		case HSSFCell.CELL_TYPE_NUMERIC: return("OK  "+cell.getNumericCellValue());
		case HSSFCell.CELL_TYPE_BOOLEAN: if(cell.getBooleanCellValue()) return("OK  Y"); else return("OK  N");
		default : return("OK  ");
		}
	}
	public String excel_getStringValueAuto2B(int p_row,int p_col)
	{
		return(excel_getStringValueAuto2B(p_row,p_col,null));
	}
	public String excel_getStringValueAuto2B(int p_row,int p_col,String format)
	{
		HSSFRow row = null;
		if(sheet == null) return("FAIL WorkSheet Not Opened");
		if(p_row > sheet.getLastRowNum()) return("OK  ");
//		if(p_row >= sheet.getPhysicalNumberOfRows()) return("OK  ");
		row = sheet.getRow(p_row);
		if(row == null) return("OK  ");
		HSSFCell cell = row.getCell( p_col);
		if(cell == null) return("OK  ");
		switch(cell.getCellType()) {
		case HSSFCell.CELL_TYPE_STRING: {
					if(excel_filterNonPrintable) {
						return("OK  "+ ChineseConvert.convertAuto2B(stripNonPrintable(cell.getStringCellValue())));
					} else {
						return("OK  "+ ChineseConvert.convertAuto2B(cell.getStringCellValue()));
					}

				}
		case HSSFCell.CELL_TYPE_FORMULA:
		case HSSFCell.CELL_TYPE_NUMERIC: {
						UniLog.log("getStringValueAuto " + format);
						if(format != null) {
							double d = cell.getNumericCellValue();
							FieldPosition fpos = new FieldPosition(NumberFormat.INTEGER_FIELD);
							DecimalFormat df = new DecimalFormat(format);
							StringBuffer sb = new StringBuffer();
							df.format(d,sb,fpos);
							UniLog.log("getStringValueAuto " + d + " with format got " + sb);
							return("OK  "+ sb.toString());
						} else return("OK  "+cell.getNumericCellValue());
					}
		case HSSFCell.CELL_TYPE_BOOLEAN: if(cell.getBooleanCellValue()) return("OK  Y"); else return("OK  N");
		default : return("OK  ");
		}
	}
	public String excel_getDateValue(int p_row,int p_col)
	{
		HSSFRow row = null;
		if(sheet == null) return("FAIL WorkSheet Not Opened");
		if(p_row > sheet.getLastRowNum()) return("OK  ");
//		if(p_row >= sheet.getPhysicalNumberOfRows()) return("OK  ");
		row = sheet.getRow(p_row);
		if(row == null) return("OK  ");
		HSSFCell cell = row.getCell( p_col);
		if(cell == null) return("OK  ");
		switch(cell.getCellType()) {
		case HSSFCell.CELL_TYPE_STRING: return("OK  "+cell.getStringCellValue());
		case HSSFCell.CELL_TYPE_FORMULA:
		case HSSFCell.CELL_TYPE_NUMERIC: return("OK  "+DateUtil.toDateString(cell.getDateCellValue(),"yyyy/mm/dd"));
		default : return("OK  ");
		}
	}
	public String excel_getRowCount()
	{
		return("OK  " + excelRowCount);
	}

	public String excel_getSheetCount()
	{
		if(workBook == null)
         return("FAILWorkBook is null");
		try {
			int nSheet = workBook.getNumberOfSheets();
         return("OK  "+nSheet);
      } catch (Exception ex) {
         return("FAIL");
		}
	}
	public String excel_getSheetName(int p_index)
	{
		if(workBook == null)
         return("FAILWorkBook is null");
		try {
			String sheetName = workBook.getSheetName(p_index);
         return("OK  "+sheetName);
      } catch (Exception ex) {
         return("FAIL");
		}
	}
	public String excel_getSheetByIdx(int p_index) {
		if(workBook == null)
         return("FAILWorkBook is null");
		try {
			int nSheet = workBook.getNumberOfSheets();
			if(p_index >= nSheet) {
         	return("FAILWorksheet Index Out of Range");
			}
			sheet = workBook.getSheetAt(p_index);
//      	excelRowCount = sheet.getPhysicalNumberOfRows();
      	excelRowCount = sheet.getLastRowNum()+1;
			if(excelRowCount < 1) sheet.createRow(0);
			excelSheetIdx = p_index;
			excelSheetName = workBook.getSheetName(p_index);
			UniLog.log("excel_getSheetByIdx " + p_index + " " + sheet.getLastRowNum() + " R " + sheet.getPhysicalNumberOfRows() + " H " + excelDefaultRowHeight);
			return("OK");
      } catch (Exception ex) {
         return("FAIL");
		}
	}
	public String excel_getNameFormula(String p_name)
	{
		if(workBook == null) return("FAILWorkBook is null");
		if(excelNames == null) {
			excelNames = new Hashtable();
			int namecnt = workBook.getNumberOfNames();
			UniLog.log("Extract Excel Names "+namecnt);
			for(int i=0;i<namecnt;i++) {
				HSSFName nm = workBook.getNameAt(i);
				String ref = nm.getRefersToFormula();
				excelNames.put(nm.getNameName(),ref);
				UniLog.log("Excel Names "+i+" "+nm.getNameName()+ " " + ref);
			}
		}
		String ref = (String) excelNames.get(p_name);
		if(ref == null) {
			return("FAILName Not Found");
		} else {
			return("OK  "+ref);
		}
	}
	/*
	public String excel_getName(String p_name)
	{
		if(workBook == null) return("FAILWorkBook is null");
		if(excelNames == null) {
			excelNames = new Hashtable();
			int namecnt = workBook.getNumberOfNames();
			UniLog.log("Extract Excel Names "+namecnt);
			for(int i=0;i<namecnt;i++) {
				HSSFName nm = workBook.getNameAt(i);
				String ref = nm.getReference();
				excelNames.put(nm.getNameName(),ref);
				UniLog.log("Excel Names "+i+" "+nm.getNameName()+ " " + ref);
			}
		}
		String ref = (String) excelNames.get(p_name);
		if(ref == null) {
			return("FAILName Not Found");
		} else {
			int r0,c0,r1,c1;
			r0 = HSSFUtil.getReferenceRowidx0(ref);
			c0 = HSSFUtil.getReferenceColidx0(ref);
			r1 = HSSFUtil.getReferenceRowidx1(ref);
			c1 = HSSFUtil.getReferenceColidx1(ref);
			return(new Sprintf("OK  %10d%10d%10d%10d").add(r0).add(c0).add(r1).add(c1).toString());
		}
	}
	*/
	public String excel_setTranslateGTET(boolean p_boolean)
	{
		excel_translateGTET = p_boolean;
		return("OK");
	}
	String excel_translateGTET(String s)
	{
		if(s == null) return(null);
		char[] carr = s.toCharArray();
		for(int i = 0;i<carr.length;i++) {
			switch(carr[i]) {
			case 8807 : carr[i] = 8805; break;
			case 8806 : carr[i] = 8804; break;
			case 33274: carr[i] =21488; break;
			}
		}
		return(new String(carr));
	}
	public String excel_translate_Chinese(int p_sw) 
	{
		excel_translateChinese = p_sw;
		return("OK");
	}

	public String excel_clearRow(int p_row) 
	{
		if(sheet != null && p_row >= 0 && p_row < excelRowCount) {
			HSSFRow row = sheet.getRow(p_row);
			if(row != null) sheet.removeRow(row);
			excelRowCount = sheet.getLastRowNum()+1;
			return("OK");
		}
		return("FAIL");
	}

	public String excel_shiftRow(int p_start, int p_end, int p_cnt) 
	{
		if(sheet != null && p_start >= 0 && p_start < excelRowCount) {
			sheet.shiftRows(p_start,p_end,p_cnt);
			excelRowCount = sheet.getLastRowNum()+1;
			return("OK");
		}
		return("FAIL");
	}

	public String excel_cloneSheet(int p_sheetIdx,String p_name) 
	{
		if(workBook != null) {
			HSSFSheet newSheet = workBook.cloneSheet(p_sheetIdx);
			if(newSheet != null) {
				String s = newSheet.getSheetName();
				int idx = workBook.getSheetIndex(s);
				workBook.setSheetName(idx, p_name);
				return("OK  "+idx);
			}
		}
		return("FAIL");
	}

	public String excel_removeSheet(int p_sheetIdx) 
	{
		if(workBook != null) {
			workBook.removeSheetAt(p_sheetIdx);
		}
		return("OK");
	}


/**********************/

   public static boolean myputFileToRemote(String p_localFileName, String p_remoteFileName, JxForm p_form, boolean p_autoOpen) throws Exception{
      if (p_localFileName == null){
			throw new Exception("localFileName is null");
      }
		if (p_remoteFileName == null){
			throw new Exception("remoteFileName is null");
      }
		File localFile = new File(p_localFileName);
		FileInputStream localFileIS = new FileInputStream(localFile);

      String remoteDirName = StringUtil.dirname(p_remoteFileName, '\\');
		for(;;) {
      	p_form.jxUnlink(p_remoteFileName);
			long fs = p_form.jxGetFileSize(p_remoteFileName);
			UniLog.log("putFileToRemote getfilesize return " + fs);
			if( fs < 0) break;
			if(p_form.confirm("File "+p_remoteFileName+" is currently in used , retry ? ")) {
				continue;
			} else {
				return(false);
			}
		}
      p_form.jxPutFile(localFileIS, p_remoteFileName);
		localFileIS.close();
		localFile.delete();
		if (p_autoOpen){
         int cc = p_form.jxShellExecute("open", p_remoteFileName, "", remoteDirName);
         if (cc <= 0){
            throw new Exception("Open document fail");
         }
		}
		return(true);
	}
	public String putFileToRemote(String p_localfile,String p_remotefile,boolean p_autoOpen)
	{
		boolean result;
		UniLog.log("putfiletoremote " + p_localfile + " " + p_remotefile);
		try {
			result = myputFileToRemote(p_localfile,p_remotefile,this,p_autoOpen);
		} catch (Exception ex) {
			UniLog.log(ex);
			return("FAILputFileToRemote");
		}
		if(result) return("OK  putFileToRemote"); else return("FAIL");
	}
	public	void putChnCache(String p_fname,int p_mode)
	{
		gadgetProvider.putChnCache(p_fname,p_mode);
		/*
		if(gadgetProvider instanceof JxCbuilderGadgetProvider) {
			((JxCbuilderGadgetProvider) gadgetProvider).putChnCache(p_fname,p_mode);
		}
		*/
	}
	public	void putWxCache(String p_fname,int p_mode)
	{
		gadgetProvider.putWxCache(p_fname,p_mode);
		/*
		if(gadgetProvider instanceof JxCbuilderGadgetProvider) {
			((JxCbuilderGadgetProvider) gadgetProvider).putWxCache(p_fname,p_mode);
		}
		*/
	}
	public boolean jxFieldExist(String p_fdname)
	{
		if(isbinded() == true) {
			if(myskin.getField(p_fdname) != null) return(true);
		}
		return(false);
	}
	public String getClientWxCacheDir()
	{
		String s = gadgetProvider.getClientWxCacheDir();
		if(s != null) return("OK  " + s); else return("FAIL");
		/*
		if(gadgetProvider instanceof JxCbuilderGadgetProvider) {
			String s = ((JxCbuilderGadgetProvider) gadgetProvider).getClientWxCacheDir();
			if(s != null) return("OK  " + s); else return("FAIL");
		} 
		return("FAIL");
		*/
	}
	public String getClientWxTmpDir()
	{
		String s = gadgetProvider.getClientWxTmpDir();
		if(s != null) return("OK  " + s); else return("FAIL");
		/*
		if(gadgetProvider instanceof JxCbuilderGadgetProvider) {
			String s = ((JxCbuilderGadgetProvider) gadgetProvider).getClientWxTmpDir();
			if(s != null) return("OK  " + s); else return("FAIL");
		} 
		return("FAIL");
		*/
	}
	public String getClientChnCacheDir()
	{
		String s = gadgetProvider.getClientChnCacheDir();
		if(s != null) return("OK  " + s); else return("FAIL");
		/*
		if(gadgetProvider instanceof JxCbuilderGadgetProvider) {
			String s = ((JxCbuilderGadgetProvider) gadgetProvider).getClientChnCacheDir();
			if(s != null) return("OK  " + s); else return("FAIL");
		} 
		return("FAIL");
		*/
	}
	public String getSkinClass()
	{
		return(myskin.getSkinClass());
	}
	public String getProviderSessionLabel()
	{
		return( gadgetProvider.getSessionLabel());
	}
	public void chgHint(String p_fieldname,String p_hint)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.chgHint(p_hint);
	}
	public void jxSetColor(String p_fieldname,int p_color)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setColor(p_color);
	}
	public void jxSetHeight(String p_fieldname,int p_height)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setHeight(p_height);
	}
	public void jxSetWidth(String p_fieldname,int p_width)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setWidth(p_width);
	}
	public void jxUnFocus()
	{
		if(isbinded() != true) return;
		myskin.unFocus();
	}
	public void jxMaximize()
	{
		if(isbinded() != true) return;
		myskin.maximize();
	}
	public void jxSetFontSize(String p_fieldname,int p_size)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setFontSize(p_size);
	}
	public void jxGridSetRowHeight(String p_fieldname,int p_row,int p_height)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.gridSetRowHeight(p_row,p_height);
		return;
	}
	public void jxSetEncoding(String p_fieldname,String p_encoding)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setEncoding(p_encoding);
	}
	public void jxGridSetColor(String p_fieldname,int p_col,int p_row,int p_color)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.gridSetColor(p_col,p_row,p_color);
	}
	public void jxGridSetFontColor(String p_fieldname,int p_col,int p_row, int p_color)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.gridSetFontColor(p_col,p_row,p_color);
	}
	public void jxSetImageIndex(String p_fieldname,int p_target,int p_imgidx)
	{
		JxField jxfield;
		if(isbinded() != true) return;
		if((jxfield = jxAdd(p_fieldname)) == null) return;
		jxfield.setImageIndex(p_target,p_imgidx);
	}
	public void jxCacheRemove(String p_cacheFile)
	{
		gadgetProvider.removeWxCache(p_cacheFile);
		/*
		((JxCbuilderGadgetProvider) gadgetProvider).removeWxCache(p_cacheFile);
		*/
	}
	public String utf8ToBIG5(String p_string,boolean p_fedexEuro)
	{
		String s = "OK  ";
		char c[] = p_string.toCharArray();
		for(int i = 0;i < c.length; i++) {
			if(p_fedexEuro) {
			  
			}
			s += ChineseConvert.convertAuto2B(new String(c,i,1));
		}
		return(s);
	}

	public String stripNonPrintable(String p_s)
	{
		StringBuffer sb = new StringBuffer();
		char[] ca = p_s.toCharArray();
		for (int i=0; i<ca.length; i++) {
			if(ca[i] < 32) continue;
			if(Character.isWhitespace(ca[i])) sb.append(' '); else sb.append(ca[i]);
		}
		return(sb.toString());
	}
	public String excel_setFilterNonPrintable(boolean p_boolean)
	{
		excel_filterNonPrintable = p_boolean;
		return("OK");
	}
	public int jxGridGetCurrentCol(String p_fieldname)
	{
		JxField jxfield;
		if((jxfield = jxAdd(p_fieldname)) == null) return(-1);
		return(jxfield.getCurrentCol());
	}

	public String excel_getCellStyle(int p_row,int p_col)
	{
		HSSFRow row = null;
		if(workBook == null) return("FAIL WorkBook Not Opened");
		if(sheet == null) return("FAIL WorkSheet Not Opened");
		if(p_row > sheet.getLastRowNum()) return("FAILRow Not Exist 1");
		row = sheet.getRow(p_row);
		if(row == null) return("FAILRow Not Exist 2");
		HSSFCell cell = row.getCell(p_col);
		if(cell == null) return("FAILColumn Not Exist");
		hssf_cellStyle = cell.getCellStyle();
		if(hssf_cellStyle == null) return("FAILStyle Not Exist");
		return("OK  " + hssf_cellStyle.getIndex());
	}
	public int excel_getCellStyleIdx(int p_row,int p_col)
	{
		HSSFRow row = null;
		if(workBook == null) return(-1);
		if(sheet == null) return(-2);
		if(p_row > sheet.getLastRowNum()) return(-3);
		row = sheet.getRow(p_row);
		if(row == null) return(-4);
		HSSFCell cell = row.getCell(p_col);
		if(cell == null) return(-5);
		hssf_cellStyle = cell.getCellStyle();
		if(hssf_cellStyle == null) return(-6);
		return(hssf_cellStyle.getIndex());
	}
// 250821 removed
//	public String serial_openPort(String p_dev,int p_baudrate, int p_databits, int p_stopbits, int p_parity)
//	{
//		try {
//			if(jxSerialPort != null) {
//				jxSerialPort.closePort();
//				jxSerialPort = null;
//			}
//			jxSerialPort = new SerialPort(p_dev);
//			jxSerialPort.openPort();
//			jxSerialPort.setParams(
//					p_baudrate,
//					p_databits,
//					p_stopbits,
//					p_parity
//				);
//		} catch (Exception ex) {
//			UniLog.log(ex);
//			return("FAIL");
//		}
//		return("OK");
//	}
//	public String serial_closePort()
//	{
//		try {
//			if(jxSerialPort != null) {
//				jxSerialPort.closePort();
//				jxSerialPort = null;
//			}
//		} catch (Exception ex) {
//			UniLog.log(ex);
//			return("FAIL");
//		}
//		return("OK");
//	}
//	public String serial_println(String p_line)
//	{
//		try {
//			if(jxSerialPort == null) {
//				return("FAIL");
//			}
//			jxSerialPort.writeBytes((p_line+"\r\n").getBytes());
//		} catch (Exception ex) {
//			UniLog.log(ex);
//			return("FAIL");
//		}
//		return("OK");
//	}
	public String getImageDimension(String p_file)
	{
		try {
		Hashtable h = ImageUtil.getImageProperties(new File(p_file));
			return(new Sprintf("OK  %10d%10d").add(((Integer) h.get("width")).intValue())
					.add(((Integer) h.get("height")).intValue())
					.toString());
		} catch (Exception ex) {
			return("FAIL");
		}
	}
	public void EditHint(JxField f)
	{
		/*
			Edithint npdform = (Edithint) getForm("Edithint");
			if(npdform != null) {
				npdform.promptEditHint(f);
				setFocus();
				setFocus(f.getSkinName());
			}
		*/
		gadgetProvider.editHint(this,f);
	}
	
	public String jxGetParameter(String p_paramName)
	{
		if(isbinded() != true) return(null);
		return(myskin.getParameter(p_paramName));
	}
	
	public void unMapAllFields()
	{
//		for(Enumeration e = fields.values();e.hasMoreElements();) {
		for(Iterator itr = fields.values().iterator();itr.hasNext();) {
			JxField f = (JxField) itr.next();
			f.unMapCell();
		}
	}
	
	public void setKeepAliveInterval(int p_msec)
	{
		if(isbinded() != true) return;
		myskin.setKeepAliveInterval(p_msec);
	}
	
	public void doKeepAlive()
	{
		
	}
	
	
	public String excel_autoResizeColumn(int p_col)
	{
		if(workBook == null) return("FAIL");
		if(sheet == null) return("FAIL");
		sheet.autoSizeColumn(p_col);
		return("OK");
	}
	
	public String excel_setDefaultColumnStyle(int p_col,int p_idx) {
		if(workBook == null) return("FAIL");
		if(sheet == null) return("FAIL");
		if(p_idx < 0) {
			hssf_cellStyle = null;
			return("OK");
		}
		HSSFCellStyle thisCellStyle = null;
		if(p_idx >= workBook.getNumCellStyles()) return("FAIL CellStyle Not Created");
		if(p_idx >= 0) thisCellStyle = workBook.getCellStyleAt((short) p_idx);
		sheet.setDefaultColumnStyle(p_col, thisCellStyle);
		return("OK");
	}
	
	 private HSSFDataValidation setupSheetValidation(HSSFSheet sheet, int i,
	        DataValidationConstraint validationConstraint) {
	        CellRangeAddressList addressList = new CellRangeAddressList();
	        HSSFDataValidation dataValidation = new HSSFDataValidation(
	                addressList, validationConstraint);
	        addressList.addCellRangeAddress(1, i, 10000, i);
	        dataValidation.setEmptyCellAllowed(true);
	        dataValidation.setShowPromptBox(true);
	        sheet.addValidationData(dataValidation);
	        return dataValidation;
	    }
	
	public String excel_setColumnValidation(int p_col,Vector p_validList) {
		if(workBook == null) return("FAIL");
		if(sheet == null) return("FAIL");
		HSSFDataValidationHelper validationHelper = new HSSFDataValidationHelper(sheet);
		String [] vList = new String[p_validList.size()];
		for(int i=0;i<vList.length;i++) {
			vList[i] = (String) p_validList.get(i);
		}
		DataValidationConstraint validationConstraint = validationHelper.createExplicitListConstraint(vList);
		setupSheetValidation(sheet, p_col, validationConstraint );
		return("OK");
	}

	public String getLoginId() {
		return(gadgetProvider.getLoginId());
	}
	
	public RpcClient getRpcClient() {
		return(gadgetProvider.getRpcClient());
	}
	
	public InputStream erpFileInputStream(String p_file) throws Exception
	{
		return(gadgetProvider.erpFileInputStream(p_file));
	}

	public OutputStream erpFileOutputStream(String p_file) throws Exception
	{
		return(gadgetProvider.erpFileOutputStream(p_file));
	}
	
	public Object getSessionObject(String p_key)
	{
		return(gadgetProvider.getSessionObject(p_key));
	}
	
	public InputStream getResourceAsStream(String p_res) {
		return(gadgetProvider.getResourceAsStream(p_res) );
	}
	
	public Object getNativeComponent()
	{
		if(isbinded() != true) return(null);
		return(myskin.getNativeComponent());
	}
	
	public void setDirtyFlag(boolean p_flag)
	{
		if(isbinded() != true) return;
		myskin.setDirtyFlag(p_flag);
	}
	
	public boolean isDirty() {
		if(isbinded() != true) return(false);
		return(myskin.isDirty());
	}

	protected void addFormDirtyListener(JxFormDirtyListener p_listener)
	{	
		if(myskin != null) {
			myskin.addFormDirtyListener(p_listener);
		}
	}
	public AbstractGetItemProperty getGipi(String p_sublink)
	{
		return(null);
	}
	public static String replaceViewName(String p_vname) {
		return(p_vname.replace(".", "_"));
	}
	public void bindSublinkList(JxField sv , BiResult sl) {
	}
}
