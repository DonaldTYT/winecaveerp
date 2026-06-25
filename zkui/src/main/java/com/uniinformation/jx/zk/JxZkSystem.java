package com.uniinformation.jx.zk;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.Composer;
//import org.zkoss.zsoup.helper.StringUtil;
import org.zkoss.zul.A;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Space;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabpanel;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.MessageboxDlg;

import com.google.gson.reflect.TypeToken;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiFieldAnno;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.bischema.ExcelCellCollection;
import com.uniinformation.bicore.bischema.ExcelWorkSheetCache;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellVector;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.erpv4.RecSync;
import com.uniinformation.utils.AnnoUtil;
import com.uniinformation.utils.EmailUtil;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.HealthCheckUtil;
import com.uniinformation.utils.JdbcPool;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.LabelHelper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.SessionHelper.ActiveUserInfo;
import com.uniinformation.webcore.SessionListener;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiLogHelper;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiLogHelper.ETYPE;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.ZkBiTranslateHelper;
import com.uniinformation.zkcomp.ZkBiButton;
import static com.uniinformation.utils.ZkUtil.importAction;

public class JxZkSystem extends ZkComposerBase {
	@Wire
	Button btSendBC;
	@Wire
	Textbox tbBCMsg;
	@Wire
	Vbox vboxSystemInfo;
	@Wire
	Window winMain;
	@Wire
	Listbox userList;
	@Wire
	Button btShutdown;
	@Wire
	Button btCloseCachedJdbcPool;
	@Wire
	Button btRestartJdbcConn;
	@Wire
	Button btGC;
	@Wire
	Button btGCRefresh;
	@Wire
	Button btGenWinecavePhotoSlideThumb;
	@Wire
	Button btGenWinecavePhotoSlideThumbRewrite;
	@Wire
	Listbox targetList;
	@Wire
	Button btZkTemplate;
	@Wire
	Textbox tbZkTemplateViewId;
	@Wire
	Textbox tbZkTemplateOutput;
	@Wire
	Intbox ibZkTemplateMaxColIdx;
	@Wire
	Button btTestEmail;
	@Wire
	Textbox tbTestEmailTo;
	@Wire
	Textbox tbTestEmailContent;
	@Wire
	Textbox tbSmtpHost;
	@Wire
	Intbox ibSmtpPort;
	@Wire
	Combobox cbbSecMode;
	@Wire
	Combobox cbbSSLValidate;
	@Wire
	Textbox tbSmtpLogin;
	@Wire
	Textbox tbSmtpPassword;
	@Wire
	Textbox tbSmtpFrom;
	@Wire
	Div divReportProblem;
	@Wire 
	Textbox tbReportProblemEmailTo;
	@Wire
	Textbox tbReportProblemZipPassword;
	@Wire
	Button btSmtpSave;
	@Wire
	Button btExportSchema;
	@Wire
	Button btExportSchemaUpdateIni;
	@Wire
	Checkbox cbSchemaDebug;
	@Wire
	Checkbox cbJdbcDebug;
	@Wire
	Button btClearSchemaCache;
	@Wire
	Button btReloadSchema;
	@Wire
	Textbox tbChangeLoginId;
	@Wire
	Button btChangeLoginId;
	/*
	//andrew220909 obsoleted, replaced by dfList
	//@Wire
	//Checkbox cbLogButton;
	*/
	@Wire
	Tab tabWinecave;
	@Wire
	Tab tabSystemInfo;
	@Wire
	Tab tabBroadcast;
	@Wire
	Tab tabSchema;
	@Wire
	Tab tabCustomBiView;
	@Wire
	Tab tabRestartService;
	@Wire
	Tab tabEmailSetup;
	@Wire
	Tab tabDebug;
	@Wire
	Button btCustomBiViewGetJson;
	@Wire
	Textbox tbCustomBiViewJson;
	@Wire
	Button btCustomBiViewSave;
	@Wire
	Button btClearTranslateCache;
	@Wire
	Button btClearCostCalculation;
	@Wire
	Textbox tbJdbcDebugOutput;
	@Wire
	Button btJdbcPoolStatus;
	@Wire
	Button btRefreshSysInfo;
	@Wire
	Button btHealthCheck;
	@Wire
	Listbox debugFlagList;
	@Wire
	Button btRefreshDFList;

	@Wire
	Button btSchemaCompare;
	@Wire
	Listbox lbSchema1Option, lbSchema2Option;
	@Wire
	Textbox tbSchema1Status, tbSchema2Status;
	@Wire
	Button btSchema1Upload, btSchema2Upload;
	@Wire
	Label lbSchema1Upload, lbSchema2Upload;
	@Wire
	Textbox tbSchemaCompareResult;
	@Wire
	Button btSchemaCompareResultDownload, btSchemaCompareClear;
	
	@Wire
	Button btClearAllLoginToken;
	
	@Wire
	Hbox hboxMaintMode;
	
	@Wire
	Checkbox cbMaintMode;
	
	@Wire
	Button btClearExcelBrCache;

	@Wire
	Button btReloadConfig;
	
	@Wire
	Checkbox cbDbLogAdd;
	
	@Wire
	Checkbox cbDbLogUpdate;
	
	@Wire
	Checkbox cbDbLogDelete;

	@Wire
	Button btExportDDDAll, btImportDDDView, btImportDDDJoin;
	@Wire
	Textbox tbImportDDDJoinDbName, tbImportDDDViewDbName, tbImportDDDViewId;
	
	/*
	//testing code for test BiFieldAnno
	@Wire
	@BiFieldAnno
	String testBiFieldAnno = "abc";
	*/
	
	//SessionHelper sessionHelper;  //moved to parent class
	boolean validSmtpConfig = false;
	public static final String smtpFilingKey = "zkbi_system_smtp";
	
	@Override
	protected boolean validateURL(String p_url) {
		return true;
		//return sessionHelper.isAdminUser();
	}

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		//Selectors.wireComponents(p_comp, this, false);  //important for wire variable
		/*
		AnnoUtil.showAnno(this, Wire.class);
		*/
   		if (!accessOkFlag) {
   			winMain.setVisible(false);
   			ZkUtil.registerClientInfoEvent(winMain, sessionHelper, true, -100); 
   			return;
   		}
		Clients.evalJavaScript(String.format("document.title='%s'", StringEscapeUtils.escapeJavaScript(sessionHelper.getLabel(winMain.getTitle()))));
		
		
		//set tab access control
		boolean rightFlag = sessionHelper.hasAccessRight("systools");
		tabSystemInfo.setVisible(rightFlag);
		tabBroadcast.setVisible(rightFlag);
		tabSchema.setVisible(sessionHelper.isAdminUser());
		tabCustomBiView.setVisible(sessionHelper.isAdminUser());
		tabRestartService.setVisible(sessionHelper.isAdminUser());
		tabEmailSetup.setVisible(rightFlag);
		tabDebug.setVisible(sessionHelper.isAdminUser());
		
		divReportProblem.setVisible(sessionHelper.getAllowReportProblem());
		
		if (btSendBC != null){
			btSendBC.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
				@Override
				public void onZkBiEvent(Event p_event) throws Exception {
					UniLog.logm(this, "%s:%s", targetList.getSelectedItem().getLabel(), targetList.getSelectedItem().getValue());
					ReturnMsg rtn = ZkUtil.sendBroadcast(sessionHelper, targetList.getSelectedItem().getValue().toString() , "message", tbBCMsg.getValue());
   	    		   	if (rtn.getStatus()) {
   	    			   ZkUtil.showMsg("Message Sent");
			    	}
   	    		   	else {
   	    			   ZkUtil.showErrMsg(rtn.getMsg());
   	    		   	}
					tbBCMsg.setText("");
				}});
		}
		
		//get system info
		refreshSystemInfo();
		
		//get active user list
		refreshUserList();
		
		if (btRefreshSysInfo != null) {
			btRefreshSysInfo.addEventListener(Events.ON_CLICK, new ZkBiEventListener() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					refreshUserList();
					refreshSystemInfo();
				}
				
			});
		}
		
		if (btShutdown != null){
			btShutdown.setDisabled(true);
			if (sessionHelper.getAllowShutdown()){
				btShutdown.setDisabled(false);
				btShutdown.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
					@Override
					public void onZkBiEvent(Event p_event) throws Exception {
					    Messagebox.show("Are you sure?", "Warning", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
					    	     new ZkBiEventListener() {
					    	       public void onZkBiEvent(Event evt) {
					    	    	   if (((Integer)evt.getData()) == Messagebox.YES){
					    	    		   ZkBiLogHelper.logEvent(sessionHelper, evt, ETYPE.SHUTDOWN);
					    	    		   UniLog.logm(this, "shutdown system now");
					    	    		   System.exit(0);
					    	    	   }
					    	    	   else{
					    	    		   UniLog.logm(this, "not shutdown system");
					    	    	   }
					    	      }
					    	    });
					}
				});
			}
			else{
				btShutdown.setTooltiptext("Function Disabled");
			}
		}
		
		if (btCloseCachedJdbcPool != null) {
			btCloseCachedJdbcPool.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					sessionHelper.closeCachedJdbcPool();
				}
			});
		}
		if (btRestartJdbcConn != null) {
			btRestartJdbcConn.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					ReturnMsg rtn = sessionHelper.restartJdbcConnections(60);
					if (rtn.getStatus()) {
						ZkUtil.msg("connection restarted");
					}
					else {
						ZkUtil.warnMsg("error:" + rtn.getMsg());
					}
				}
			});
		}
		if (btGC != null) {
			btGC.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					UniLog.log1("call gc");
					ZkUtil.showMsg("call gc");
					System.gc();
				}
			});
		}
		if (btGCRefresh != null) {
			btGCRefresh.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					UniLog.log1("call gc");
					ZkUtil.showMsg("call gc");
					System.gc();
					refreshUserList();
					refreshSystemInfo();
				}
			});
		}
		if (tabWinecave != null && StringUtils.contains(sessionHelper.getAgent(), "winecave")) {
			tabWinecave.setVisible(true);
			if (btGenWinecavePhotoSlideThumb != null) {
				btGenWinecavePhotoSlideThumb.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						Messagebox.show("Are you sure to generate thumbnail(normal)?", "Warning", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
								new ZkBiEventListener() {
							public void onZkBiEvent(Event evt) {
								if (((Integer)evt.getData()) == Messagebox.YES){
									ZkBiLogHelper.logEvent(sessionHelper, evt, ETYPE.SHUTDOWN);
									generateWinecavePhotoSliderThunbnail(false);
								}
								else {
									ZkUtil.showMsg("Action abort");
								}
							}});
					}
				});
			}
			if (btGenWinecavePhotoSlideThumbRewrite != null) {
				btGenWinecavePhotoSlideThumbRewrite.setDisabled(true);  //need to add protection to this feature
				btGenWinecavePhotoSlideThumbRewrite.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						Messagebox.show("Are you sure to generate thumbnail(rewrite)?", "Warning", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
								new ZkBiEventListener() {
							public void onZkBiEvent(Event evt) {
								if (((Integer)evt.getData()) == Messagebox.YES){
									ZkBiLogHelper.logEvent(sessionHelper, evt, ETYPE.SHUTDOWN);
									generateWinecavePhotoSliderThunbnail(true);
								}
								else {
									ZkUtil.showMsg("Action abort");
								}
							}});
					}
				});
			}
		}
		
		if (btZkTemplate != null){
			btZkTemplate.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
				@Override
				public void onZkBiEvent(Event p_event) throws Exception {
					UniLog.logm(this, "got event");
					
					if (StringUtils.isBlank(tbZkTemplateViewId.getValue())){
						UniLog.logm(this,"view is blank, ignore");
						return;
					}
					String resultTemplate = "";
					try{
						resultTemplate = ZkUtil.genZkTemplate(sessionHelper, tbZkTemplateViewId.getValue(), ibZkTemplateMaxColIdx.getValue());
					}
					catch(Exception ex){
						ex.printStackTrace();
						Messagebox.show("Error:"+ex.getMessage());
					}
					tbZkTemplateOutput.setValue(resultTemplate);
				}});
		}
		
		btTestEmail.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event p_event) throws Exception {
				UniLog.logm(this, "got click event");
				if (!validSmtpConfig){
					Messagebox.show("Invalid config");
					return;
				}
				
				Pair<String,String> from = Pair.of(tbSmtpFrom.getValue(), null);
				List<Pair<String,String>> toList = new ArrayList<Pair<String,String>>();
				toList.add(Pair.of(tbTestEmailTo.getValue(), ""));
				List<Pair<String,String>> bccList = null;
	
				//content
				String subject = "This is a testing email from: " + sessionHelper.getLoginId();
				String htmlMsg = "<html><body>"+ tbTestEmailContent.getValue()+"</body></html>";
				String txtMsg = null;
				List attList = null;
				
				ReturnMsg rtnMsg = ZkUtil.sendEmail(from, toList, null, subject, htmlMsg, txtMsg, attList, sessionHelper);
				UniLog.logm(this,"rtnMsg:" + rtnMsg);
				if (rtnMsg.getStatus()){
					Messagebox.show("Email has been sent successfully");
				}
				else{
					Messagebox.show("Unable to send email. result:" + rtnMsg);
				}
				
			}});
		
		loadSmtpConfig();
		
		btSmtpSave.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event arg0) throws Exception {
				saveSmtpConfig();
			}
			
		});
		btExportSchema.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event arg0) throws Exception {
				exportSchema();
			}
		});
		
		btExportSchemaUpdateIni.setDisabled(!sessionHelper.isAdminUser());
		btExportSchemaUpdateIni.setTooltiptext("sysadmin permission required");
		btExportSchemaUpdateIni.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event arg0) throws Exception {
				exportSchemaUpdateIni();
			}
		});
		btClearSchemaCache.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event p_event) throws Exception {
				ReturnMsg rtnMsg = BiSchema.clearSchemaCache();
				if (rtnMsg.booleanValue()){
					ZkUtil.showMsg("Clear cache OK");
				}
				else{
					ZkUtil.showErrMsg("Clear Cache FAIL");
				}
			}
		});
		btClearExcelBrCache.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event p_event) throws Exception {
				ReturnMsg rtnMsg = ExcelWorkSheetCache.clearBrCache();
				if (rtnMsg.booleanValue()){
					ZkUtil.showMsg("Clear brcache OK");
				}
				else{
					ZkUtil.showErrMsg("Clear brcache FAIL");
				}
			}
		});

		if(btReloadConfig != null) {
			btReloadConfig.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
				@Override
				public void onZkBiEvent(Event p_event) throws Exception {
					BiConfig.resetErpv4Config(sessionHelper);
					ZkUtil.showMsg("Erpv4Config Reloaded");
				}
			});
		}
		
		btReloadSchema.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event p_event) throws Exception {
				BiSchema.loadSchema(sessionHelper, true);
				ZkUtil.showMsg("reload schema OK");
			}
		});
		cbSchemaDebug.setChecked(sessionHelper.getSchemaDebugFlag());
		cbSchemaDebug.addEventListener(Events.ON_CHECK, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event p_event) throws Exception {
				UniLog.log("Debug Mode:" + cbSchemaDebug.isChecked());
				BiSchema schema = BiSchema.loadSchema(sessionHelper);
				sessionHelper.setSchemaDebugFlag(cbSchemaDebug.isChecked());
				ZkBiLogHelper.logEvent(sessionHelper, p_event, cbSchemaDebug.isChecked() ? ETYPE.DEBUG_SCHEMA_ENABLE : ETYPE.DEBUG_SCHEMA_DISABLE);
			}
			
		});
		cbJdbcDebug.setChecked(JdbcPool.getDebug());
		cbJdbcDebug.addEventListener(Events.ON_CHECK, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event p_event) throws Exception {
				UniLog.log("jdbc debug:" + cbJdbcDebug.isChecked());
				JdbcPool.setDebug(cbJdbcDebug.isChecked());
				//TODO should debug both jdbcpool and connection
				ZkBiLogHelper.logEvent(sessionHelper, p_event, cbJdbcDebug.isChecked() ? ETYPE.DEBUG_JDBC_ENABLE : ETYPE.DEBUG_JDBC_DISABLE);
			}
			
		});
		
		/*
		//andrew220909 obsoleted, replaced by dfList
		cbLogButton.setChecked(SessionHelper.logButtonFlag.get());
		cbLogButton.addEventListener(Events.ON_CHECK, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event p_event) throws Exception {
				BiSchema schema = BiSchema.loadSchema(sessionHelper);
				SessionHelper.logButtonFlag.set(cbLogButton.isChecked());
				ZkUtil.showMsg("Log button " + (cbLogButton.isChecked() ? "ENABLE" : "DISABLE"));
				ZkBiLogHelper.logEvent(sessionHelper, p_event, cbLogButton.isChecked() ? ETYPE.LOG_BUTTON_ENABLE: ETYPE.LOG_BUTTON_DISABLE);
			}
		});
		*/
		
		tbChangeLoginId.setValue("");
		tbChangeLoginId.setDisabled(!sessionHelper.getAllowChangeLoginId());
		btChangeLoginId.setDisabled(!sessionHelper.getAllowChangeLoginId());
		if (sessionHelper.getAllowChangeLoginId()){
			btChangeLoginId.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
				public void onZkBiEvent(Event event) throws Exception {
					ReturnMsg rtnMsg = sessionHelper.changeLoginId(tbChangeLoginId.getValue());
					if (rtnMsg.getStatus()){
						ZkBiLogHelper.logEvent(sessionHelper, event, ETYPE.DEBUG_CHANGE_ID);
						ZkUtil.showMsg(rtnMsg.getMsg());
					}
					else{
						ZkUtil.showWarnMsg(rtnMsg.getMsg());
					}
				}
				
			});
		}
		else{
			btChangeLoginId.setTooltiptext("Function Disabled");
		}
		
		tbCustomBiViewJson.setValue("");
		btCustomBiViewSave.addEventListener(Events.ON_CLICK, new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				ZkUtil.showWarnMsg("Work in progress...");
			}
		});
		
		btCustomBiViewGetJson.addEventListener(Events.ON_CLICK, new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				ZkUtil.showWarnMsg("Work in progress...");
				
			}
		});
		
		btClearTranslateCache.addEventListener(Events.ON_CLICK, new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				ZkBiTranslateHelper.clearCache();
				ZkUtil.showMsg("Clear Cache OK");
			}
		});

		/*
		 Will be added back using dynamic class  260309 DT
		btClearCostCalculation.addEventListener(Events.ON_CLICK, new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				CostCalculation.clearCostCalculation();
				RecSync.clearAllAgentHash();
				ZkUtil.showMsg("Clear Cache OK");
			}
		});
		*/
		
		btJdbcPoolStatus.addEventListener(Events.ON_CLICK, new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				//tbJdbcDebugOutput.setValue(sessionHelper.getJdbcPool().logStatus(""));
				tbJdbcDebugOutput.setValue(sessionHelper.getJdbcPool().logStatus("default") +sessionHelper.getLoginTokenJdbcPool().logStatus("logintoken"));
			}
		});
		
		btHealthCheck.addEventListener(Events.ON_CLICK, new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				ZkUtil.showMsg("healthcheck called. please check console log.");
				HealthCheckUtil.run();
			}
		});
		
		refreshDebugCtrlList();
		btRefreshDFList.addEventListener(Events.ON_CLICK, new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				refreshDebugCtrlList();
			}
		});
		
		if (btClearAllLoginToken != null) {
			btClearAllLoginToken.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
				@Override
				public void onZkBiEvent(Event p_event) throws Exception {
				    Messagebox.show("Are you sure to clear all logintoken record?", "Warning", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
			    	     new ZkBiEventListener() {
			    	       public void onZkBiEvent(Event evt) {
			    	    	   if (((Integer)evt.getData()) == Messagebox.YES){
			    	    		   ReturnMsg rtn = sessionHelper.deleteAllLoginTokenRecord();
			    	    		   UniLog.log1("clear logintoken return:" + rtn.toString());
			    	    		   if (rtn.getStatus() && rtn.getData() instanceof Integer) {
			    	    			   ZkUtil.msg("clean logintoken ok. delcnt:" + rtn.getData());
			    	    		   }
			    	    		   else {
			    	    			   ZkUtil.msg("clean logintoken fail:" + rtn.toString());
			    	    		   }
			    	    	   }
			    	    	   else{
			    	    		   ZkUtil.showMsg("clean logintoken cancel");
			    	    		   UniLog.log1("clear logintoken cancel");
			    	    	   }
			    	      }
			    	    });
				}
			});
		}
		
		//maintenance mode 
		if (sessionHelper.getAllowMaintMode() && sessionHelper.isAdminUser()) {
			hboxMaintMode.setVisible(true);
			cbMaintMode.setChecked(SessionHelper.getMaintModeFlag());
			cbMaintMode.addEventListener(Events.ON_CHECK, new ZkBiEventListener(){
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					UniLog.log1("cbMaintMode isChecked:%s", cbMaintMode.isChecked());
					if (cbMaintMode.isChecked()) {
						Messagebox.show("Are you sure enter maintenance mode?\nIt will disable user login", "Warning", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
								new ZkBiEventListener() {
							public void onZkBiEvent(Event evt) {
								if (((Integer)evt.getData()) == Messagebox.YES){
									ReturnMsg rtn = sessionHelper.enterMaintMode();
									ZkUtil.warnMsg(rtn.getStatus() ? "System enter Maintenance Mode" : rtn.getMsg());
								}
								else {
									UniLog.log1("action abort, reverse checkbox status");
									cbMaintMode.setChecked(false);
								}
							}
						});
					}
					else { //unchecked
						ReturnMsg rtn = sessionHelper.leaveMaintMode();
						ZkUtil.warnMsg(rtn.getStatus() ? "System return to Normal Mode." : rtn.getMsg());
					}
				}
			});
		}
		
		
		cbDbLogAdd.setChecked(sessionHelper.dblogAddEnabled());
		cbDbLogAdd.addEventListener(Events.ON_CHECK, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event p_event) throws Exception {
				sessionHelper.setdblogAddEnabled(cbDbLogAdd.isChecked());
				ZkUtil.showMsg("Status updated");
			}
		});	
		cbDbLogUpdate.setChecked(sessionHelper.dblogUpdateEnabled());
		cbDbLogUpdate.addEventListener(Events.ON_CHECK, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event p_event) throws Exception {
				sessionHelper.setdblogUpdateEnabled(cbDbLogUpdate.isChecked());
				ZkUtil.showMsg("Status updated");
			}
		});	
		cbDbLogDelete.setChecked(sessionHelper.dblogDeleteEnabled());
		cbDbLogDelete.addEventListener(Events.ON_CHECK, new ZkBiEventListener(){
			@Override
			public void onZkBiEvent(Event p_event) throws Exception {
				sessionHelper.setdblogDeleteEnabled(cbDbLogDelete.isChecked());
				ZkUtil.showMsg("Status updated");
			}
		});	
		
		
		btExportDDDAll.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				exportDDDAllRecord(sessionHelper);
				ZkUtil.showMsg("Export done");
			}
		});

		btImportDDDJoin.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			private void importRecord(String impDbName, boolean isOverwrite) {
				try {
					importDDDJoinRecord(sessionHelper, impDbName, isOverwrite);
					ZkUtil.showMsg("Import done");
				} catch (Exception e) {
					UniLog.log(e);
					ZkBiMsgbox.show(ZkBiMsgbox.Type.error, StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
				}
			}
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("btImportDDDJoin Clicked");
				String impDbName = tbImportDDDJoinDbName.getText().trim();
				if (StringUtils.isNotBlank(impDbName)) {
					Vlayout vl = new Vlayout();
					Checkbox cb = new Checkbox("Overwrite record");
					vl.appendChild(new Label(String.format("Import DDD Join '%s'?", impDbName)));
					vl.appendChild(cb);
					new ZkBiMsgbox(sessionHelper).setContent(vl).setButtons(new String[] {"Ok", "Cancel"}).setEventListener(new ZkBiEventListener<Event>(){
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
							if (btn.getIdx() == 0)
								importRecord(impDbName, cb.isChecked());
						}
					}).build().doModal();
				}
				else
					ZkUtil.showMsg("Please input Database name");
			}
		});

		btImportDDDView.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			private void importRecord(String impDbName, String impViewId, boolean isOverwrite) {
				try {
					importDDDViewRecord(sessionHelper, impDbName, impViewId, isOverwrite);
					ZkUtil.showMsg("Import done");
				} catch (Exception e) {
					UniLog.log(e);
					ZkBiMsgbox.show(ZkBiMsgbox.Type.error, StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
				}
			}
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("btImportDDDView Clicked");
				String impDbName = tbImportDDDViewDbName.getText().trim();
				String impViewId = tbImportDDDViewId.getText().trim();
				String msg = null;
				if (StringUtils.isNotBlank(impDbName) && StringUtils.isNotBlank(impViewId))
					msg = String.format("Import View '%s' - '%s'?", impDbName, impViewId);
				else if (StringUtils.isNotBlank(impDbName))
					msg = String.format("Import View of database '%s'?", impDbName);
				else if (StringUtils.isBlank(impDbName) && StringUtils.isBlank(impViewId))
					msg = "Import AllView?";
				if (msg != null) {
					Vlayout vl = new Vlayout();
					Checkbox cb = new Checkbox("Overwrite record");
					vl.appendChild(new Label(msg));
					vl.appendChild(cb);
					new ZkBiMsgbox(sessionHelper).setContent(vl).setButtons(new String[] {"Ok", "Cancel"}).setEventListener(new ZkBiEventListener<Event>(){
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
							if (btn.getIdx() == 0)
								importRecord(impDbName, impViewId, cb.isChecked());
						}
					}).build().doModal();
				} else
					ZkUtil.showMsg("Please input Database name and ViewId");
			}
		});
		
		//for testing only
		//ZkUtil.createDummyGrid(p_comp, 500, 5, false, true);
		
			
    	//receive broadcast message
   		//ZkUtil.receiveBroadcast(sessionHelper);
   		
	    //ZkUtil.autoAdjustWinWidth(winMain, -100, sessionHelper.isMobileDevice());
   		//ZkUtil.registerClientInfoEvent(winMain, sessionHelper, true, -100);
   		
		//ZkBiTranslateHelper.addOnUpdateTranslateEventListener(winMain, sessionHelper);
		
		setupSchemaCompare();
	}

	private void setupSchemaCompare() {
		setupSchemaCompareUpload(lbSchema1Option, btSchema1Upload, lbSchema1Upload);
		setupSchemaCompareUpload(lbSchema2Option, btSchema2Upload, lbSchema2Upload);
		btSchemaCompare.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			private StringBuilder logSummarySb = new StringBuilder();
			private StringBuilder logViewSb = new StringBuilder();
			private StringBuilder logCellSb = new StringBuilder();
			private void appendLog(StringBuilder sb, String fmt, Object...params) {
				if (params.length > 0)
					sb.append(String.format(fmt, params));
				else
					sb.append(fmt);
				sb.append("\n");
			}
			private void appendSummaryLog(String fmt, Object...params) {
				appendLog(logSummarySb, fmt, params);
			}
			private void appendViewLog(String fmt, Object...params) {
				appendLog(logViewSb, fmt, params);
			}
			private void appendCellLog(String fmt, Object...params) {
				appendLog(logCellSb, fmt, params);
			}
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				tbSchemaCompareResult.setText("");
				logSummarySb.setLength(0);
				logViewSb.setLength(0);
				logCellSb.setLength(0);
				BiSchema obj1 = getSchemaCompareObject(lbSchema1Option, tbSchema1Status, btSchema1Upload, lbSchema1Upload);
				BiSchema obj2 = getSchemaCompareObject(lbSchema2Option, tbSchema2Status, btSchema2Upload, lbSchema2Upload);
				if (obj1 == null || obj2 == null)
					return;
				
				appendSummaryLog("Summary");
				appendSummaryLog("======================");
				appendSummaryLog("Timestamp: %s", DateUtil.dateToDateTimeStr(DateUtil.now(), "yyyy/MM/dd HH:mm:ss"));
				appendSummaryLog("S1: %s", (String)tbSchema1Status.getAttribute("schema_name"));
				appendSummaryLog("S2: %s", (String)tbSchema2Status.getAttribute("schema_name"));
				
				appendViewLog("View List");
				appendViewLog("======================");
				appendViewLog("\"Type\"\t\"View name\"");
				
				appendCellLog("Cell Different");
				appendCellLog("======================");
				appendCellLog("\"Cell Fullname\"\t\"Attribute\"\t\"S1 value\"\t\"S2 value\"");

				Map<String, BiView> viewMap1 = new TreeMap<String, BiView>();
				Map<String, BiView> viewMap2 = new TreeMap<String, BiView>();
				Set<String> viewMarkList = new TreeSet<String>();
				for (BiView view : obj1.getAllView())
					viewMap1.put(view.getName(), view);
				for (BiView view : obj2.getAllView())
					viewMap2.put(view.getName(), view);
				for (Map.Entry<String, BiView> entry : viewMap1.entrySet()) {
					String name = entry.getKey();
					if (viewMap2.containsKey(name))
						viewMarkList.add(name);
				}

				int s1Count = 0;
				for (String key : viewMap1.keySet()) {
					if (!viewMarkList.contains(key)) {
						appendViewLog("\"S1 only view\"\t\"%s\"", key);
						s1Count++;
					}
				}
				int s2Count = 0;
				for (String key : viewMap2.keySet()) {
					if (!viewMarkList.contains(key)) {
						appendViewLog("\"S2 only view\"\t\"%s\"", key);
						s2Count++;
					}
				}
				appendSummaryLog("S1 unique view count: %d", s1Count);
				appendSummaryLog("S2 unique view count: %d", s2Count);
				appendSummaryLog("S1/S2 common view count: %d", viewMarkList.size());
				appendSummaryLog("View diff: %d", Math.max(s1Count, s2Count));
				
				int cellDiffCount = 0;
				s1Count = 0;
				s2Count = 0;
				for (String viewKey : viewMarkList) {
					appendViewLog("\"Common view\"\t\"%s\"", viewKey);
					Vector<BiColumn> bcs1 = viewMap1.get(viewKey).getColumns();
					Vector<BiColumn> bcs2 = viewMap2.get(viewKey).getColumns();
					Map<String, BiColumn> bcMap1 = new LinkedHashMap<String, BiColumn>();
					Map<String, BiColumn> bcMap2 = new LinkedHashMap<String, BiColumn>();
					Set<String> bcMarkList = new LinkedHashSet<String>();
					for (BiColumn bc : bcs1)
						bcMap1.put(bc.getLabel(), bc);
					for (BiColumn bc : bcs2)
						bcMap2.put(bc.getLabel(), bc);
					for (Map.Entry<String, BiColumn> entry : bcMap1.entrySet()) {
						String name = entry.getKey();
						if (bcMap2.containsKey(name))
							bcMarkList.add(name);

					}

					for (String labelKey : bcMap1.keySet()) {
						if (!bcMarkList.contains(labelKey)) {
							appendCellLog("\"%s.%s\"\t\"S1 only cell\"", viewKey, labelKey);
							s1Count++;
						}
					}

					for (String labelKey : bcMap2.keySet()) {
						if (!bcMarkList.contains(labelKey)) {
							appendCellLog("\"%s.%s\"\t\"S2 only cell\"", viewKey, labelKey);
							s2Count++;
						}
					}
					
					for (String labelKey : bcMarkList) {
						BiColumn bc1 = bcMap1.get(labelKey);
						BiColumn bc2 = bcMap2.get(labelKey);
						List<String> sbb = new ArrayList<String>();
						Field[] fieldArray = bc1.getClass().getDeclaredFields();		
						for (final Field f : fieldArray) {
							try {
								f.setAccessible(true);
								Annotation[] annotations = f.getDeclaredAnnotations();
								boolean isExpose = false;
								for (Annotation a : annotations) {
									if (a.annotationType().getName().equals("com.google.gson.annotations.Expose")) {
										isExpose = true;
										break;
									}
								}
								final String fieldName = f.getName();
								final String fieldType = f.getType().getName();
								if (isExpose) {
									Object fieldValue1 = f.get(bc1);
									Object fieldValue2 = f.get(bc2);
									if (!ObjectUtils.equals(fieldValue1, fieldValue2)) {
										if (fieldValue1 instanceof Boolean)
											sbb.add(String.format("\"%s\"\t\"%b\"\t\"%b\"", fieldName, fieldValue1, fieldValue2));
										else if (fieldValue1 instanceof Integer || fieldValue1 instanceof Long)
											sbb.add(String.format("\"%s\"\t%d\t%d", fieldName, fieldValue1, fieldValue2));
										else if (fieldValue1 instanceof String[] || fieldValue2 instanceof String[]) {
											String s1 = fieldValue1 != null ? String.join(",", (String[])fieldValue1) : "";
											String s2 = fieldValue2 != null ? String.join(",", (String[])fieldValue2) : "";
											if (!StringUtils.equals(s1, s2))
												sbb.add(String.format("\"%s\"\t[%s]\t[%s]", fieldName, s1, s2));
										}
										else 
											sbb.add(String.format("\"%s\"\t\"%s\"\t\"%s\"", fieldName, fieldValue1, fieldValue2));
									}
								}
							}
							catch (Exception ex) {
								UniLog.log1("setupSchemaCompare error:%s", ex.getMessage());
							}
						}
						for (String s : sbb)
							appendCellLog("\"%s.%s\"\t%s", viewKey, labelKey, s);
						cellDiffCount += sbb.size();
					}
				}
				appendSummaryLog("Cell diff: %d", cellDiffCount + Math.max(s1Count, s2Count));
				tbSchemaCompareResult.setText(logSummarySb.toString() + "\n" + logViewSb.toString() + "\n" + logCellSb.toString());
				btSchemaCompareResultDownload.setDisabled(false);
			}
		});
		btSchemaCompareResultDownload.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				Filedownload.save(tbSchemaCompareResult.getValue(), "text/plain", String.format("schecomp_%s.log", DateUtil.dateToDateTimeStr(DateUtil.now(), "yyyyMMddHHmmss")));
			}
		});
		btSchemaCompareClear.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				File file = (File)btSchema1Upload.getAttribute("uploaded_file");
				if (file != null && file.exists()) {
					if (file.delete()) {
						btSchema1Upload.removeAttribute("uploaded_file");
						lbSchema1Upload.setValue("");
					}
				} else {
					btSchema1Upload.removeAttribute("uploaded_file");
					lbSchema1Upload.setValue("");
				}
				file = (File)btSchema2Upload.getAttribute("uploaded_file");
				if (file != null && file.exists()) {
					if (file.delete()) {
						btSchema2Upload.removeAttribute("uploaded_file");
						lbSchema2Upload.setValue("");
					}
				} else {
					btSchema2Upload.removeAttribute("uploaded_file");
					lbSchema2Upload.setValue("");
				}
				lbSchema1Option.setSelectedIndex(-1);
				lbSchema2Option.setSelectedIndex(-1);
				tbSchema1Status.setText("");
				tbSchema2Status.setText("");
				tbSchema1Status.removeAttribute("schema_name");
				tbSchema2Status.removeAttribute("schema_name");
				btSchema1Upload.setVisible(false);
				btSchema2Upload.setVisible(false);
				btSchema1Upload.removeAttribute("uploaded_old_filename");
				btSchema2Upload.removeAttribute("uploaded_old_filename");
				tbSchemaCompareResult.setText("");
			}
		});
	}
	private void setupSchemaCompareUpload(Listbox lbSchemaOption, final Button btUpload, final Label lbUpload) {
		lbSchemaOption.addEventListener(Events.ON_SELECT, new ZkBiEventListener<SelectEvent<Listitem, String>>() {
			@Override
			public void onZkBiEvent(SelectEvent<Listitem, String> event) throws Exception {
				btUpload.setVisible(event.getReference().getIndex() == 1);
			}
		});
		btUpload.setUpload(String.format("true,maxsize=-1,multiple=false,native"));
		btUpload.addEventListener(Events.ON_UPLOAD, new ZkBiEventListener<UploadEvent>() {
			@Override
			public void onZkBiEvent(UploadEvent event) throws Exception {
				Media media = event.getMedia();
				UniLog.log1("upload event name:%s, contenttype:%s, format:%s, length:%d", media.getName(), media.getContentType(), media.getFormat(), media.getByteData().length);
				File oldFile = (File)btUpload.getAttribute("uploaded_file");
				if (oldFile != null && oldFile.exists()) {
					oldFile.delete();
					btUpload.removeAttribute("uploaded_file");
					lbUpload.setValue("");
				}
				//if (StringUtils.equalsIgnoreCase(media.getContentType(), "text/xml")) {
					File file;
					int i = 0;
					do {
						file = new File(String.format("/tmp/jxzksyssche_%s_%s_%d", sessionHelper.getAgent(), sessionHelper.getLoginId(), System.currentTimeMillis() + i++));
					} while (file.exists());
					FileUtils.writeByteArrayToFile(file, media.getByteData());
					btUpload.setAttribute("uploaded_file", file);
					btUpload.setAttribute("uploaded_old_filename", media.getName());
					lbUpload.setValue(file.getName());
				//}
			}
		});
	}
	private BiSchema getSchemaCompareObject(Listbox lbSchemaOption, Textbox tbSchemaStatus, Button btUpload, Label lbUpload) {
		if (lbSchemaOption.getSelectedIndex() == 0) {
			BiSchema biSchema = sessionHelper.getBiSchema();
			if (biSchema != null) {
				tbSchemaStatus.setText("OK");
				tbSchemaStatus.setAttribute("schema_name", "current schema");
				return biSchema;
			}
		}
		else if (lbSchemaOption.getSelectedIndex() == 1) {
			File file = (File)btUpload.getAttribute("uploaded_file");
			if (file != null && file.exists()) {
				BiSchema biSchema = null;
				try {
					biSchema = BiSchema.loadSchema_gen(sessionHelper, file.getAbsolutePath());
					if (biSchema != null) {
						tbSchemaStatus.setText("OK");
						tbSchemaStatus.setAttribute("schema_name", (String)btUpload.getAttribute("uploaded_old_filename"));
					}
					else
						tbSchemaStatus.setText("cannot gen schema");
				}
				catch (Exception ex) {
					ex.printStackTrace();
					UniLog.log1("error:%s", ex.getMessage());
					tbSchemaStatus.setText("cannot gen schema: " + ex.toString());
				}
				if (file.delete()) {
					btUpload.removeAttribute("uploaded_file");
					lbUpload.setValue("");
				}
				return biSchema;
			}
			else
				tbSchemaStatus.setText("file not found");
		}
		return null;
	}
	
	
	private void loadSmtpConfig(){
		try{
			JSONObject json = FilingUtil.getJson(sessionHelper.getAgent(), null, smtpFilingKey);
			if (json == null){
				UniLog.logm(this,"%s json is null", smtpFilingKey);
				return;
			}
			tbSmtpHost.setValue(getJsonStr(json,"smtpHost",""));
			ibSmtpPort.setValue(getJsonInt(json,"smtpPort",25));
			cbbSecMode.setValue(getJsonStr(json,"smtpSecMode","NONE"));
			cbbSSLValidate.setValue(getJsonStr(json,"smtpSSLValidate","Y"));
			tbSmtpLogin.setValue(getJsonStr(json,"smtpLogin",""));
			tbSmtpPassword.setValue(getJsonStr(json,"smtpPassword",""));
			tbSmtpFrom.setValue(getJsonStr(json,"smtpFrom",""));
			tbReportProblemEmailTo.setValue(getJsonStr(json,"reportProblemEmailTo",""));
			tbReportProblemZipPassword.setValue(getJsonStr(json,"reportProblemZipPassword",""));
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		validateSmtpConfig();
	}
	private static String getJsonStr(JSONObject p_json, String p_key, String p_defValue) {
		if (p_json == null || p_key == null) {
			return p_defValue;
		}
		try {
			if (!p_json.has(p_key)) {
				return p_defValue;
			}
			return p_json.getString(p_key);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return p_defValue;
		}
	}
	private static int getJsonInt(JSONObject p_json, String p_key, int p_defValue) {
		if (p_json == null || p_key == null) {
			return p_defValue;
		}
		try {
			if (!p_json.has(p_key)) {
				return p_defValue;
			}
			return p_json.getInt(p_key);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return p_defValue;
		}
	}
	
	public static String validateReportProblemZipPassword(String password) {
		if (StringUtils.isNotEmpty(password)) {
			if (password.length() < 6 || password.length() > 8)
				return "Password length must be 6-8 char";
		}
		return null;
	}
	
	private void saveSmtpConfig(){
		String errMsg = validateReportProblemZipPassword(tbReportProblemZipPassword.getValue());
		if (errMsg != null) {
			Messagebox.show(errMsg);
			return;
		}

		try{
			JSONObject json = new JSONObject();
			json.put("smtpHost", tbSmtpHost.getValue());
			json.put("smtpPort", ibSmtpPort.intValue());
			json.put("smtpSecMode", cbbSecMode.getValue());
			json.put("smtpSSLValidate", cbbSSLValidate.getValue());
			json.put("smtpLogin", tbSmtpLogin.getValue());
			json.put("smtpPassword", tbSmtpPassword.getValue());
			json.put("smtpFrom", tbSmtpFrom.getValue());
			json.put("reportProblemEmailTo", tbReportProblemEmailTo.getValue());
			json.put("reportProblemZipPassword", tbReportProblemZipPassword.getValue());
			FilingUtil.storeJson(sessionHelper.getAgent(), null, smtpFilingKey, smtpFilingKey, smtpFilingKey, json);
			Messagebox.show("Config saved successfully");
		}
		catch(Exception ex){
			ex.printStackTrace();
			Messagebox.show("Unable to save config:" + ex.getMessage());
		}
		
		validateSmtpConfig();
	}
	private void validateSmtpConfig(){
		validSmtpConfig = false;
		if (StringUtils.isNotBlank(tbSmtpHost.getValue()) && ibSmtpPort.intValue() > 0 && StringUtils.isNotBlank(tbSmtpFrom.getValue())){
			validSmtpConfig = true;
		}
		btTestEmail.setDisabled(!validSmtpConfig);
	}
	private void exportSchema(){
		try{
    		BiSchema schema = BiSchema.loadSchema(sessionHelper);
    		File tmpFile = File.createTempFile("system_exportschema", ".tmp");
    		UniLog.logm(this,"export tmpfile: %s" + tmpFile.getAbsolutePath());
    		schema.exportToXML(tmpFile);
	    	Filedownload.save( IOUtils.toByteArray(new FileInputStream(tmpFile)) , "application/octet-stream", String.format("schema_%s.xml", sessionHelper.getAgent()));
    		tmpFile.deleteOnExit();
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	private void exportSchemaUpdateIni(){
		try{
			if (StringUtils.isBlank(sessionHelper.getSchemaXML())) {
				ZkUtil.errMsg("SchemaXML is blank, force abort");
				return;
			}
			if (!new File(sessionHelper.getSchemaXML()).exists()) {
				ZkUtil.errMsg("SchemaXML does not exist, force abort");
				return;
			}
			if (!sessionHelper.getSchemaXML().endsWith(".xml")) {
				ZkUtil.errMsg("Invalid schemaXML extension: %s", sessionHelper.getSchemaXML());
				return;
			}
			ZkBiMsgbox.show(String.format("Are you sure to update %s?", sessionHelper.getSchemaXML()),new String[]{"Yes","No"},new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
					UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
					if (StringUtils.equalsAnyIgnoreCase(btn.getName(),"Yes")){
						BiSchema schema = BiSchema.loadSchema(sessionHelper);
			    		File tmpFile = File.createTempFile("system_exportschema", ".tmp");
			    		UniLog.logm(this,"export tmpfile: %s" + tmpFile.getAbsolutePath());
			    		schema.exportToXML(tmpFile);
			    		FileUtils.copyFile(tmpFile, new File(sessionHelper.getSchemaXML()));
			    		tmpFile.deleteOnExit();
			    		ZkUtil.showMsg("schemaXML %s updated", sessionHelper.getSchemaXML());
					}
				}
			});
		}
		catch(Exception ex){
			ZkUtil.errMsg("error:"+ex.getMessage());
			ex.printStackTrace();
		}
	}
	private void generateWinecavePhotoSliderThunbnail(boolean p_rewriteFlag) {
		class MDocRow {
			int id;
			int mrg;
			int drg;
			String filekey;
			String sfilekey;
			String photoSize;
			String thumbSize;
		}
		List<MDocRow> list = new ArrayList<MDocRow>();
		ZkUtil.showMsg("generate thumbnail in progress, please wait");
		try {
			BiResult biResult = BiResultHelper.create(sessionHelper, "wc.StockImgs", null, -1, null);
			UniLog.log1("row count:" + biResult.getRowCount());
			if (biResult.query(true).getStatus()) {
				for (int i = 0; i < biResult.getRowCount(); i++) {
					biResult.loadOneRecV(i);
					MDocRow row = new MDocRow();
					row.id = i;
					row.mrg = biResult.getCell("mdoc_mrg").getInt();
					row.drg = biResult.getCell("mdoc_drg").getInt();
					row.filekey = biResult.getCell("mdoc_filekey").getString();
					row.sfilekey = biResult.getCell("mdoc_sfilekey").getString();
					row.photoSize = biResult.getCell("mdoc_photosize").getString();
					row.thumbSize = biResult.getCell("mdoc_thumbsize").getString();
					if (p_rewriteFlag){ //force regenerate thumbmail	
						row.sfilekey = ""; 
						row.photoSize = "";
						row.thumbSize = "";
					}
					list.add(row);
				}
			}
			biResult.beginWork();
			int count = 0;
			for (MDocRow row : list) {
				boolean updateTableFlag = false;
				byte[] photoData = null;
				UniLog.log1("process id:%d mrg:%d drg:%d filekey:%s sfilekey:%s photoSize:%s thubmSize:%s ", row.id, row.mrg, row.drg, row.filekey, row.sfilekey, row.photoSize, row.thumbSize);
				//get photo size
				if (StringUtils.isBlank(row.photoSize)) {
					photoData = getPhotoData(row.filekey);
					if (photoData == null || photoData.length == 0){
						UniLog.log1("photoData is empty, skip process this row");
						continue;
					}
					row.photoSize = getPhotoSize(photoData);
					updateTableFlag = true;
				}
				//extract thumbnail
				if (StringUtils.isBlank(row.sfilekey)) {
					if (photoData == null || photoData.length == 0)
						photoData = getPhotoData(row.filekey);
					if (photoData == null || photoData.length == 0){
						UniLog.log1("photoData is empty, skip process this row");
						continue;
					}
					Map<String, String> photoAttrMap = new HashMap<String, String>();
					row.sfilekey = storeThumbnal(row.mrg, row.drg, photoData, photoAttrMap);
					row.thumbSize = photoAttrMap.get("data_size");
					updateTableFlag = true;
				}
				//get thumbnail size
				if (StringUtils.isBlank(row.thumbSize)) {
					byte[] thumbData = getPhotoData(row.sfilekey);
					if (photoData == null || photoData.length == 0){
						UniLog.log1("photoData is empty, skip process this row");
						continue;
					}
					row.thumbSize = getPhotoSize(thumbData);
					updateTableFlag = true;
				}
				//update table
				if (updateTableFlag) {
					biResult.fetchOneRecV(row.id);
					biResult.getCell("mdoc_filekey").set(row.filekey);
					biResult.getCell("mdoc_photosize").set(row.photoSize);
					biResult.getCell("mdoc_sfilekey").set(row.sfilekey);
					biResult.getCell("mdoc_thumbsize").set(row.thumbSize);
					UniLog.log1("update mrg:%d,drg:%d,filekey:%s,photoSize:%s,sfilekey:%s,thumbSize:%s", row.mrg, row.drg, row.filekey, row.photoSize, row.sfilekey, row.thumbSize);
					ReturnMsg rtnMsg = biResult.updateCurrent();
					if (rtnMsg != null && !rtnMsg.getStatus()) {
						UniLog.log("rollbackwork errMsg:" + rtnMsg.getMsg());
						biResult.rollbackWork();
						ZkUtil.showErrMsg("update error:" + rtnMsg.getMsg());
						return;
					} else
						count++;
				}
			}
			biResult.commitWork();
			UniLog.log1("updated count:" + count);
			ZkUtil.showMsg("updated count:%d", count);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private byte[] getPhotoData(String filekey) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		FilingUtil.getFile(sessionHelper.getAgent(), null, filekey, bos);
		bos.close();
		return bos.toByteArray();
	}
	private static String getPhotoSize(byte[] photoData) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(photoData);
	    BufferedImage img = ImageIO.read(is);
	    is.close();
	    return img.getWidth() + "x" + img.getHeight();
	}
	private String storeThumbnal(int mrg, int drg, byte[] photoData, Map<String, String> photoAttrMap) throws Exception {
		final int thumbnailMinWidth = 360;
		ByteArrayInputStream is = new ByteArrayInputStream(photoData);
		String sfilekey = String.format("jxStockImageFiling_S_%010d_%010d",mrg,drg);  
	    BufferedImage img = ImageIO.read(is);
	    is.close();
		float ratio = (float)thumbnailMinWidth / Math.min(img.getWidth(), img.getHeight());
		int newWidth = (int)(ratio * img.getWidth());
		int newHeight = (int)(ratio * img.getHeight());
		BufferedImage dimg = new BufferedImage(newWidth, newHeight, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, newWidth, newHeight, 0, 0, img.getWidth(), img.getHeight(), null);
		g.dispose();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(dimg, "jpg", bos);
		bos.close();
		is = new ByteArrayInputStream(bos.toByteArray());
		FilingUtil.storeFile(
		   sessionHelper.getAgent(),
		   null,
		   sfilekey,
		   "",
		   "",
		   is);
		is.close();
		if (photoAttrMap != null)
			photoAttrMap.put("data_size", String.format("%dx%d", newWidth, newHeight));
		return sfilekey;
	}
	
	private synchronized void refreshUserList() {
		try {
			userList.getChildren().clear();
			userList.appendChild(new Listhead(){{
				this.setSizable(true);
				this.appendChild(new Listheader("Action"){{ this.setSort("auto"); this.setHflex("min");}});
				this.appendChild(new Listheader("Login ID"){{ this.setSort("auto"); this.setHflex("min");}});
				this.appendChild(new Listheader("Agent"){{ this.setSort("auto"); this.setHflex("min");}});
				this.appendChild(new Listheader("IP"){{ this.setSort("auto"); this.setHflex("min");}});
				this.appendChild(new Listheader("Login Time"){{ this.setSort("auto"); this.setHflex("min");}});
				this.appendChild(new Listheader("Duration"){{ this.setSort("auto"); this.setHflex("min"); this.setAlign("right");}});
				this.appendChild(new Listheader("Last Access Time"){{ this.setSort("auto"); this.setHflex("min");}});
				this.appendChild(new Listheader("Idle"){{ this.setSort("auto"); this.setHflex("min");}});
				this.appendChild(new Listheader("Desktop"){{ this.setSort("auto"); this.setHflex("min");}});
				this.appendChild(new Listheader("Last Access URL"){{ this.setSort("auto"); this.setHflex("min");}});
			}
				
			});
			for (final ActiveUserInfo userInfo : SessionHelper.getActiveUserList()){
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				final String firstAccessTimeStr = sdf.format(userInfo.firstAccessTime);
				final String lastAccessTimeStr = sdf.format(userInfo.lastAccessTime);
				final String durStr = DateUtil.secToText(((new Date()).getTime() - userInfo.firstAccessTime.getTime())/1000);
				final String idleStr = DateUtil.secToText(((new Date()).getTime() - userInfo.lastAccessTime.getTime())/1000);
				//DateUtil.secToText(p_sec)
				final Button btLogoutUser = new ZkBiButton("Logout User");
				btLogoutUser.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
					    Messagebox.show(String.format("Are you sure logout user [%s]?", userInfo.loginId), "Warning", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
					    	new ZkBiEventListener() {
								@Override
								public void onZkBiEvent(Event event) throws Exception {
				    	    	   if (((Integer)event.getData()) == Messagebox.YES){
				    	    		   ReturnMsg rtn = ZkUtil.sendBroadcast(sessionHelper, userInfo.sessionKey, "logoutUser", "");
				    	    		   if (rtn.getStatus()) {
				    	    			   ZkUtil.showMsg("Command Sent");
				    	    		   }
				    	    		   else {
				    	    			   ZkUtil.showErrMsg(rtn.getMsg());
				    	    		   }
				    	    	   }
				    	    	   else {
				    	    		   //UniLog.log1("action cancel");
				    	    	   }
								}
					    });
					}});
				final Button btMessage = new ZkBiButton("Message");
				btMessage.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						final Textbox tbMessage = new Textbox() {{ this.setCols(80);this.setRows(5);}};
					    MessageboxDlg dlg = ZkUtil.buildMessageboxDlg(
					    		"Message", 
					    		tbMessage,
					    		new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL},  
					    		winMain, 
					    		new ZkBiEventListener<Messagebox.ClickEvent>() {
					    			@Override
								 		public void onZkBiEvent(Messagebox.ClickEvent event) throws Exception {
											if (event.getButton() == Messagebox.Button.OK) {
												ReturnMsg rtn = ZkUtil.sendBroadcast(sessionHelper, userInfo.sessionKey, "message", tbMessage.getValue());
												if (rtn.getStatus()) {
													ZkUtil.showMsg("Message Sent");
												}
												else {
													ZkUtil.showErrMsg(rtn.getMsg());
												}
	
								 			}
										}
					    		});
						dlg.doHighlighted();
					}
				});
				userList.appendChild(new Listitem(){{  
					appendChild(new Listcell() {{ 
						this.appendChild(new Hbox() {{
							this.appendChild(btMessage); 
							this.appendChild(btLogoutUser); 
						}});
					}});
					appendChild(new Listcell(userInfo.loginId + ((StringUtils.equals(sessionHelper.getSessionKey(), userInfo.sessionKey)) ? "*" : "")));
					appendChild(new Listcell(userInfo.agent));
					appendChild(new Listcell(userInfo.ip));
					appendChild(new Listcell(firstAccessTimeStr));
					appendChild(new Listcell(durStr));
					appendChild(new Listcell(lastAccessTimeStr));
					appendChild(new Listcell(idleStr));
					appendChild(new Listcell() {{ this.setLabel(userInfo.activeDesktopStr); this.setStyle("white-space: nowrap;"); }});
					appendChild(new Listcell() {{ this.setLabel(userInfo.lastAccessUrl); this.setStyle("white-space: nowrap;"); }});
				}});
			}
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	private synchronized void refreshSystemInfo() {
		Runtime runtime = Runtime.getRuntime();
		vboxSystemInfo.getChildren().clear();
		
		/*
		vboxSystemInfo.appendChild(new Label(String.format("Memory: total:%s max:%s free:%s", 
				FileUtils.byteCountToDisplaySize(runtime.totalMemory()) , 
				FileUtils.byteCountToDisplaySize(runtime.maxMemory()), 
				FileUtils.byteCountToDisplaySize(runtime.freeMemory()))));
				*/
		long usedMem = 0;
		try {
			usedMem = (long) (runtime.totalMemory()-runtime.freeMemory())/1048576;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		vboxSystemInfo.appendChild(new Label(String.format("Memory max:%dM total:%dM used:%dM free:%dM Cell:%d Cd:%d", 
				runtime.maxMemory()/1048576, 
				runtime.totalMemory()/1048576,
				usedMem,
				runtime.freeMemory()/1048576,
				Cell.getCellCount(),
				Cell.getCdCount()
				)));
		
	}
	private void refreshDebugCtrlList() {
		try {
			
			
			//declare the debug flag list
			ArrayList<MutableTriple<String,String,AtomicBoolean>> dfList = new ArrayList();  //class,var,status
			dfList.add(MutableTriple.of("com.uniinformation.webcore.SessionHelper", "logButtonFlag", (AtomicBoolean) null));
			dfList.add(MutableTriple.of("com.uniinformation.webcore.SessionHelper", "logButtonFullNameFlag", (AtomicBoolean) null));
			dfList.add(MutableTriple.of("com.uniinformation.utils.JdbcPool", "fDebug", (AtomicBoolean) null));
			dfList.add(MutableTriple.of("com.uniinformation.bicore.BiSchema", "debugFlag", (AtomicBoolean) null));
			dfList.add(MutableTriple.of("com.uniinformation.utils.ZkUtil", "fDebug", (AtomicBoolean) null));
			dfList.add(MutableTriple.of("com.uniinformation.jx.zk.JxZkDataModelBuilder", "fDebug", (AtomicBoolean) null));
			
			dfList.add(MutableTriple.of("com.uniinformation.zkbi.vincero.VinceroCronJob", "fForceRun", (AtomicBoolean) null));
			
			//collect debug flag
			for (final  MutableTriple<String,String,AtomicBoolean> dfItem : dfList) {
				try {
					Class cl = Class.forName(dfItem.getLeft());
					Field field = cl.getField(dfItem.getMiddle());
					dfItem.setRight((AtomicBoolean)field.get(null));
				}
				catch(Exception ex) {
					UniLog.log1("error:"+ ex.getMessage());
				}
			}
			
			//render ui
			debugFlagList.getChildren().clear();
			debugFlagList.appendChild(new Listhead(){{
				this.setSizable(true);
				this.appendChild(new Listheader("Class"){{ this.setSort("auto"); this.setHflex("min");}});
				this.appendChild(new Listheader("Variable"){{ this.setSort("auto"); this.setHflex("min");}});
				this.appendChild(new Listheader("Status"){{ this.setSort("auto"); this.setHflex("min");}});
			}});
			for (final Triple<String,String,AtomicBoolean> dfItem : dfList) {
				if (dfItem.getRight() == null) {
					continue;
				}
				debugFlagList.appendChild(new Listitem(){{  
					appendChild(new Listcell(dfItem.getLeft()));
					appendChild(new Listcell(dfItem.getMiddle()));
					appendChild(new Listcell(){{
						appendChild(new Checkbox() {{
							this.setMold("switch");
							this.setChecked(dfItem.getRight().get());  //init status
							this.addEventListener("onCheck", new ZkBiEventListener<CheckEvent>() {
								@Override
								public void onZkBiEvent(CheckEvent event) throws Exception {
									//update status 
									dfItem.getRight().set(event.isChecked());  
								}});
						}});
					}});;
				}});
				
			}
			
			
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	
	static void checkmem () {
		System.gc();
		System.runFinalization();
		Runtime runtime = Runtime.getRuntime();
		long usedMem = 0;
		try {
			usedMem = (long) (runtime.totalMemory()-runtime.freeMemory())/1048576;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		UniLog.log(String.format("Memory max:%dM total:%dM used:%dM free:%dM", 
				runtime.maxMemory()/1048576, 
				runtime.totalMemory()/1048576,
				usedMem,
				runtime.freeMemory()/1048576
				));
	}
	
	private static class FieldValueType {
		Object value;
		int type;
	}
	
	public static void exportDDDAllRecord(SessionHelper sessionHelper) throws Exception {
		UniLog.log1("exportDDDAllRecord");
		SelectUtil su = new SelectUtil(); 
		su.init(sessionHelper.getLoginTokenJdbcPool());
		List<Map<String, FieldValueType>> hdrList = null, linksList = null;
		for (String tbName : new String[] {"ddd_table", "ddd_joinhdr", "ddd_joindet", "dddviewrpthdr", "dddviewrptcols", "dddviewrptorder", "dddviewrptcond", "dddviewrptlinks"}) {
			List<Map<String, FieldValueType>> list = new ArrayList<>();
			TableRec tr = su.getQueryResult(String.format("select * from %s", tbName));
			for (int i = 0; i < tr.getRecordCount(); i++) {
				Map<String, FieldValueType> m = new LinkedHashMap<>();
				Hashtable<String, Cell> ct = tr.toCellCollection(i).getCellTable();
				String[] names = tr.getFieldNames();
				for (int j = 0; j < names.length; j++) {
					FieldValueType vt = new FieldValueType();
					vt.value = tr.getField(j);
					vt.type = ct.get(names[j]).getType();
					m.put(names[j], vt);
				}
				list.add(m);
			}
			if (tbName.equals("dddviewrpthdr"))
				hdrList = list;
			else if (tbName.equals("dddviewrptlinks")) {
				linksList = list;
				continue;
			}
			FileUtils.writeStringToFile(new File(String.format("/tmp/%s.json", tbName)), GsonUtil.objToStr(list), Charset.forName("UTF-8"));
		}
		Map<Integer, Map<String, FieldValueType>> hdrMap = new HashMap<>();
		for (Map<String, FieldValueType> m : hdrList)
			hdrMap.put((int)m.get("grpth_rg").value, m);
		for (Map<String, FieldValueType> m : linksList) {
			int mrg = (int)m.get("grptl_mrg").value;
			int lrg = (int)m.get("grptl_lrg").value;
			Map<String, FieldValueType> m1 = hdrMap.get(lrg);
			if (m1 != null) {
				FieldValueType fvt = new FieldValueType();
				fvt.value = m1.get("grpth_database").value;
				fvt.type = Cell.VTYPE_STRING;
				m.put("database", fvt);
				fvt = new FieldValueType();
				fvt.value = m1.get("grpth_id").value;
				fvt.type = Cell.VTYPE_STRING;
				m.put("viewid", fvt);
			} else
				UniLog.log1("links lrg:%d not found in mrg:%d", lrg, mrg);
		}
		FileUtils.writeStringToFile(new File(String.format("/tmp/dddviewrptlinks.json")), GsonUtil.objToStr(linksList), Charset.forName("UTF-8"));
		su.close();
	}

	public static void importDDDJoinRecord(SessionHelper sessionHelper, String impDbName, boolean isOverwrite) throws Exception {
		UniLog.log1("importDDDJoinRecord impDbName:%s, isOverwrite:%b", impDbName, isOverwrite);
		Map<Integer, Map<String, Object>> hdrMap = new LinkedHashMap<>();
		Map<Integer, List<Map<String, Object>>> detMap = new LinkedHashMap<>();
		for (String tbName : new String[] {"ddd_joinhdr", "ddd_joindet"}) {
			List<Map<String, FieldValueType>> list = GsonUtil.convertToObject(FileUtils.readFileToString(new File(String.format("/tmp/%s.json", tbName)), Charset.forName("UTF-8")), new TypeToken<List<Map<String, FieldValueType>>>(){}.getType());
			UniLog.log1("%s: %d", tbName, list.size());
			for (Map<String, FieldValueType> m : list) {
				Map<String, Object> m1 = new LinkedHashMap<String, Object>();
				List<Map<String, Object>> l1;
				int rg;
				switch (tbName) {
				case "ddd_joinhdr":
					rg = (int)(double)m.get("ddjh_rg").value;
					hdrMap.put(rg, m1);
					detMap.put(rg, new ArrayList<Map<String, Object>>());
					break;
				case "ddd_joindet":
					rg = (int)(double)m.get("ddjd_mrg").value;
					l1 = detMap.get(rg);
					if (l1 != null)
						l1.add(m1);
					else
						UniLog.log1("ddd_joindet mrg:%d not found", rg);
					break;
				}
				for (Map.Entry<String, FieldValueType> entry : m.entrySet()) {
					String fieldName = entry.getKey();
					FieldValueType vt = entry.getValue();
					Cell c = new Cell(vt.value);
					switch (vt.type) {
					case Cell.VTYPE_INT:
						vt.value = c.getInt();
						break;
					case Cell.VTYPE_STRING:
						vt.value = c.getString();
						break;
					case Cell.VTYPE_DOUBLE:
						vt.value = c.getDouble();
						break;
					case Cell.VTYPE_DATE:
						vt.value = c.getDate();
						break;
					case Cell.VTYPE_BOOLEAN:
						vt.value = c.getBoolean();
						break;
					case Cell.VTYPE_DATETIME:
						vt.value = c.getDate();
						break;
					default:
						UniLog.log1("ignore %s, field:%s, value:%s", tbName, fieldName, c.getObject());
						break;
					}
					m1.put(fieldName, vt.value);
				}
			}
		}
		
		boolean hasJoinRecord = false;
		SelectUtil su = new SelectUtil(); 
		su.init(sessionHelper.getLoginTokenJdbcPool());
		TableRec tr = su.getQueryResult("select serial_id from ddd_joinhdr where ddjh_database = ?", new Wherecl().appendArgument(impDbName));
		if (tr.getRecordCount() > 0)
			hasJoinRecord = true;
		su.close();
		
		if (hasJoinRecord && !isOverwrite) {
			UniLog.log("no record insert");
			return;
		}

		if (StringUtils.isNotBlank(impDbName)) {
			final boolean hasJoinRecord1 = hasJoinRecord;
			importAction.accept(sessionHelper, su1 -> {
				importDDDJoinRecord(sessionHelper, hdrMap, detMap, impDbName, hasJoinRecord1, su1);
			});
		}
	}

	private static void importDDDJoinRecord(SessionHelper sessionHelper, 
			Map<Integer, Map<String, Object>> hdrMap, Map<Integer, List<Map<String, Object>>> detMap, 
			String dbName, boolean isOverwrite, SelectUtil su) throws Exception {
		if (isOverwrite) {
			Wherecl wherecl = new Wherecl().appendArgument(dbName);
			UniLog.log1("delete from dbName=%s", dbName);
			su.executeUpdate("delete from ddd_joindet where ddjd_mrg in (select ddjh_rg from ddd_joinhdr where ddjh_database = ?)", wherecl);
			su.executeUpdate("delete from ddd_joinhdr where ddjh_database = ?", wherecl);
		}
		boolean hasRecord = false;
		for (Map.Entry<Integer, Map<String, Object>> entry : hdrMap.entrySet()) {
			int rg = entry.getKey();
			Map<String, Object> m = entry.getValue();
			String database = ((String)m.get("ddjh_database")).trim();
			if (StringUtils.equals(database, dbName)) {
				hasRecord = true;
				List<String> fieldNameList = new ArrayList<String>();
				Wherecl wherecl = new Wherecl();
				int hdrRg = 0;
				for (Map.Entry<String, Object> entry1 : m.entrySet()) {
					String fieldName = entry1.getKey();
					Object value = entry1.getValue();
					if (StringUtils.equals(fieldName, "serial_id"))
						continue;
					if (StringUtils.equals(fieldName, "ddjh_rg")) {
						value = hdrRg = sessionHelper.getBiSchema().getUniqueRg(null, "", 16002, "ddd_joinhdr", "ddjh_rg", null).toInt();
						UniLog.log1("gen hdrRg:%d", hdrRg);
					}
					fieldNameList.add(fieldName);
					wherecl.appendArgument(value);
				}
				ZkUtil.executeInsertIntoSql(su, "ddd_joinhdr", fieldNameList, wherecl);
				if (hdrRg == 0)
					throw new Exception("hdrRg == 0");

				for (Map<String, Object> m1 : detMap.get(rg)) {
					fieldNameList = new ArrayList<String>();
					wherecl = new Wherecl();
					for (Map.Entry<String, Object> entry1 : m1.entrySet()) {
						String fieldName = entry1.getKey();
						Object value = entry1.getValue();
						if (StringUtils.equals(fieldName, "serial_id"))
							continue;
						if (StringUtils.equals(fieldName, "ddjd_mrg"))
							value = hdrRg;
						fieldNameList.add(fieldName);
						wherecl.appendArgument(value);
					}
					ZkUtil.executeInsertIntoSql(su, "ddd_joindet", fieldNameList, wherecl);
				}
			}
		}
		if (!hasRecord)
			throw new Exception("Join record not found");
	}

	
	public static void importDDDViewRecord(SessionHelper sessionHelper, String impDbName, String impViewId, boolean isOverwrite) throws Exception {
		UniLog.log1("importDDDViewRecord impDbName:%s,, impViewId:%s, isOverwrite:%b", impDbName, impViewId, isOverwrite);
		Map<Integer, Map<String, Object>> hdrMap = new LinkedHashMap<>();
		Map<Integer, List<Map<String, Object>>> colsMap = new LinkedHashMap<>();
		Map<Integer, List<Map<String, Object>>> orderMap = new LinkedHashMap<>();
		Map<Integer, List<Map<String, Object>>> condMap = new LinkedHashMap<>();
		Map<Integer, List<Map<String, Object>>> linksMap = new LinkedHashMap<>();
		Map<String, Integer> hdrViewIdMap = new HashMap<>();
		for (String tbName : new String[] {"dddviewrpthdr", "dddviewrptcols", "dddviewrptorder", "dddviewrptcond", "dddviewrptlinks"}) {
			List<Map<String, FieldValueType>> list = GsonUtil.convertToObject(FileUtils.readFileToString(new File(String.format("/tmp/%s.json", tbName)), Charset.forName("UTF-8")), new TypeToken<List<Map<String, FieldValueType>>>(){}.getType());
			UniLog.log1("%s: %d", tbName, list.size());
			for (Map<String, FieldValueType> m : list) {
				Map<String, Object> m1 = new LinkedHashMap<String, Object>();
				List<Map<String, Object>> l1;
				int rg;
				switch (tbName) {
				case "dddviewrpthdr":
					rg = (int)(double)m.get("grpth_rg").value;
					hdrMap.put(rg, m1);
					colsMap.put(rg, new ArrayList<Map<String, Object>>());
					orderMap.put(rg, new ArrayList<Map<String, Object>>());
					condMap.put(rg, new ArrayList<Map<String, Object>>());
					linksMap.put(rg, new ArrayList<Map<String, Object>>());
					break;
				case "dddviewrptcols":
					rg = (int)(double)m.get("grptc_mrg").value;
					l1 = colsMap.get(rg);
					if (l1 != null)
						l1.add(m1);
					else
						UniLog.log1("dddviewrptcols mrg:%d not found", rg);
					break;
				case "dddviewrptorder":
					rg = (int)(double)m.get("grpto_mrg").value;
					l1 = orderMap.get(rg);
					if (l1 != null)
						l1.add(m1);
					else
						UniLog.log1("dddviewrptorder mrg:%d not found", rg);
					break;
				case "dddviewrptcond":
					rg = (int)(double)m.get("grptd_mrg").value;
					l1 = condMap.get(rg);
					if (l1 != null)
						l1.add(m1);
					else
						UniLog.log1("dddviewrptcond mrg:%d not found", rg);
					break;
				case "dddviewrptlinks":
					rg = (int)(double)m.get("grptl_mrg").value;
					l1 = linksMap.get(rg);
					if (l1 != null)
						l1.add(m1);
					else
						UniLog.log1("dddviewrptlinks mrg:%d not found", rg);
					break;
				}
				for (Map.Entry<String, FieldValueType> entry : m.entrySet()) {
					String fieldName = entry.getKey();
					FieldValueType vt = entry.getValue();
					Cell c = new Cell(vt.value);
					switch (vt.type) {
					case Cell.VTYPE_INT:
						vt.value = c.getInt();
						break;
					case Cell.VTYPE_STRING:
						vt.value = c.getString();
						break;
					case Cell.VTYPE_DOUBLE:
						vt.value = c.getDouble();
						break;
					case Cell.VTYPE_DATE:
						vt.value = c.getDate();
						break;
					case Cell.VTYPE_BOOLEAN:
						vt.value = c.getBoolean();
						break;
					case Cell.VTYPE_DATETIME:
						vt.value = c.getDate();
						break;
					default:
						UniLog.log1("ignore %s, field:%s, value:%s", tbName, fieldName, c.getObject());
						break;
					}
					m1.put(fieldName, vt.value);
				}
			}
		}
		for (Map.Entry<Integer, Map<String, Object>> entry : hdrMap.entrySet()) {
			int rg = entry.getKey();
			Map<String, Object> m = entry.getValue();
			String database = ((String)m.get("grpth_database")).trim();
			String viewid = ((String)m.get("grpth_id")).trim();
			hdrViewIdMap.put(database + "|" + viewid, rg);
		}

		Map<String, List<Integer>> hdrViewIdMap1 = new HashMap<>();
		SelectUtil su = new SelectUtil(); 
		su.init(sessionHelper.getLoginTokenJdbcPool());
		TableRec tr = su.getQueryResult("select grpth_rg, grpth_database, grpth_id from dddviewrpthdr");
		for (int i = 0; i < tr.getRecordCount(); i++) {
			tr.setRecPointer(i);
			int rg = tr.getFieldInt("grpth_rg");
			String dbName = tr.getFieldString("grpth_database");
			String viewId = tr.getFieldString("grpth_id");
			String key = dbName.trim() + "|" + viewId.trim();
			List<Integer> list = hdrViewIdMap1.get(key);
			if (list == null) {
				list = new ArrayList<Integer>();
				hdrViewIdMap1.put(key, list);
			}
			list.add(rg);
		}
		su.close();

		Map<String, Integer> skipMap = new LinkedHashMap<>();
		if (StringUtils.isNotBlank(impDbName) && StringUtils.isNotBlank(impViewId)) {
			importAction.accept(sessionHelper, su1 -> {
				importDDDViewRecord(sessionHelper, hdrMap, hdrViewIdMap, colsMap, orderMap, condMap, hdrViewIdMap1, impDbName, impViewId, isOverwrite, skipMap, su1);
				for (Map.Entry<String, Integer> entry : skipMap.entrySet())
					importDDDViewRecordForLinks(sessionHelper, hdrMap, hdrViewIdMap, linksMap, hdrViewIdMap1, entry.getKey(), entry.getValue(), su1);
			});
		} else if (StringUtils.isNotBlank(impDbName)) {
			importAction.accept(sessionHelper, su1 -> {
				for (Map<String, Object> m : hdrMap.values()) {
					if (StringUtils.equals((String)m.get("grpth_database"), impDbName))
						importDDDViewRecord(sessionHelper, hdrMap, hdrViewIdMap, colsMap, orderMap, condMap, hdrViewIdMap1, (String)m.get("grpth_database"), (String)m.get("grpth_id"), isOverwrite, skipMap, su1);
				}
				for (Map.Entry<String, Integer> entry : skipMap.entrySet())
					importDDDViewRecordForLinks(sessionHelper, hdrMap, hdrViewIdMap, linksMap, hdrViewIdMap1, entry.getKey(), entry.getValue(), su1);
			});
		} else if (StringUtils.isBlank(impDbName) && StringUtils.isBlank(impViewId)) {
			importAction.accept(sessionHelper, su1 -> {
				for (Map<String, Object> m : hdrMap.values())
					importDDDViewRecord(sessionHelper, hdrMap, hdrViewIdMap, colsMap, orderMap, condMap, hdrViewIdMap1, (String)m.get("grpth_database"), (String)m.get("grpth_id"), isOverwrite, skipMap, su1);
				for (Map.Entry<String, Integer> entry : skipMap.entrySet())
					importDDDViewRecordForLinks(sessionHelper, hdrMap, hdrViewIdMap, linksMap, hdrViewIdMap1, entry.getKey(), entry.getValue(), su1);
			});
		}
		UniLog.log1("skipMap size:%d", skipMap.size());
	}

	private static void importDDDViewRecord(SessionHelper sessionHelper, 
			Map<Integer, Map<String, Object>> hdrMap, Map<String, Integer> hdrViewIdMap, Map<Integer, List<Map<String, Object>>> colsMap, 
				Map<Integer, List<Map<String, Object>>> orderMap, Map<Integer, List<Map<String, Object>>> condMap, 
			Map<String, List<Integer>> hdrViewIdMap1, 
			String dbName, String viewId, boolean isOverwrite, Map<String, Integer> skipMap, SelectUtil su) throws Exception {
		int viewRg = 0;
		Map<String, Object> viewMap = null;
		List<Map<String, Object>> colsList = null;
		List<Map<String, Object>> orderList = null;
		List<Map<String, Object>> condList = null;
		String key = dbName.trim() + "|" + viewId.trim();
		if (skipMap.containsKey(key))
			return;
		Integer rg = hdrViewIdMap.get(key);
		if (rg != null) {
			Map<String, Object> m = hdrMap.get(rg);
			viewRg = (int)m.get("grpth_rg");
			viewMap = m;
			colsList = colsMap.get(viewRg);
			orderList = orderMap.get(viewRg);
			condList = condMap.get(viewRg);
		}
		if (viewMap == null)
			throw new Exception("View not found");
		
		
		int viewRg1 = 0;
		List<Integer> rgList = hdrViewIdMap1.get(key);
		if (rgList != null && !rgList.isEmpty()) {
			if (isOverwrite) {
				for (int rg1 : rgList) {
					Wherecl wherecl = new Wherecl().appendArgument(rg1);
					UniLog.log1("delete from mrg=%d", rg1);
					su.executeUpdate("delete from dddviewrpthdr where grpth_rg = ?", wherecl);
					su.executeUpdate("delete from dddviewrptcols where grptc_mrg = ?", wherecl);
					su.executeUpdate("delete from dddviewrptorder where grpto_mrg = ?", wherecl);
					su.executeUpdate("delete from dddviewrptcond where grptd_mrg = ?", wherecl);
					su.executeUpdate("delete from dddviewrptlinks where grptl_mrg = ?", wherecl);
				}
			} else
				viewRg1 = rgList.get(0);
		}
		UniLog.log1("dbName:%s, viewId:%s, viewRg:%d, viewRg1:%d", dbName, viewId, viewRg, viewRg1);
		
		if (viewRg1 == 0) {
			List<String> fieldNameList = new ArrayList<String>();
			Wherecl wherecl = new Wherecl();
			for (Map.Entry<String, Object> entry : viewMap.entrySet()) {
				String fieldName = entry.getKey();
				Object value = entry.getValue();
				if (StringUtils.equals(fieldName, "serial_id"))
					continue;
				if (StringUtils.equals(fieldName, "grpth_rg")) {
					value = viewRg1 = sessionHelper.getBiSchema().getUniqueRg(null, "", 1056, "dddviewrpthdr", "grpth_rg", null).toInt();
					rgList = new ArrayList<Integer>();
					rgList.add(viewRg1);
					hdrViewIdMap1.put(key, rgList);
					skipMap.put(key, viewRg1);
					UniLog.log1("gen viewRg:%d", viewRg1);
				}
				fieldNameList.add(fieldName);
				wherecl.appendArgument(value);
			}
			ZkUtil.executeInsertIntoSql(su, "dddviewrpthdr", fieldNameList, wherecl);

			for (Map<String, Object> map : colsList) {
				fieldNameList = new ArrayList<String>();
				wherecl = new Wherecl();
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					String fieldName = entry.getKey();
					Object value = entry.getValue();
					if (StringUtils.equals(fieldName, "serial_id"))
						continue;
					if (StringUtils.equals(fieldName, "grptc_mrg"))
						value = viewRg1;
					fieldNameList.add(fieldName);
					wherecl.appendArgument(value);
				}
				ZkUtil.executeInsertIntoSql(su, "dddviewrptcols", fieldNameList, wherecl);
			}
			for (Map<String, Object> map : orderList) {
				fieldNameList = new ArrayList<String>();
				wherecl = new Wherecl();
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					String fieldName = entry.getKey();
					Object value = entry.getValue();
					if (StringUtils.equals(fieldName, "serial_id"))
						continue;
					if (StringUtils.equals(fieldName, "grpto_mrg"))
						value = viewRg1;
					fieldNameList.add(fieldName);
					wherecl.appendArgument(value);
				}
				ZkUtil.executeInsertIntoSql(su, "dddviewrptorder", fieldNameList, wherecl);
			}
			for (Map<String, Object> map : condList) {
				fieldNameList = new ArrayList<String>();
				wherecl = new Wherecl();
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					String fieldName = entry.getKey();
					Object value = entry.getValue();
					if (StringUtils.equals(fieldName, "serial_id"))
						continue;
					if (StringUtils.equals(fieldName, "grptd_mrg"))
						value = viewRg1;
					fieldNameList.add(fieldName);
					wherecl.appendArgument(value);
				}
				ZkUtil.executeInsertIntoSql(su, "dddviewrptcond", fieldNameList, wherecl);
			}
		}
		if (viewRg1 == 0)
			throw new Exception("viewRg1 == 0");
	}

	private static void importDDDViewRecordForLinks(SessionHelper sessionHelper, 
			Map<Integer, Map<String, Object>> hdrMap, Map<String, Integer> hdrViewIdMap, Map<Integer, List<Map<String, Object>>> linksMap, 
			Map<String, List<Integer>> hdrViewIdMap1, 
			String key, int viewRg1, SelectUtil su) throws Exception {
		int viewRg = 0;
		Integer rg = hdrViewIdMap.get(key);
		List<Map<String, Object>> linksList = null;
		if (rg != null) {
			Map<String, Object> m = hdrMap.get(rg);
			viewRg = (int)m.get("grpth_rg");
			linksList = linksMap.get(viewRg);
		}
		if (linksList == null)
			throw new Exception("linksList not found");

	 	for (Map<String, Object> map : linksList) {
			String dbName1 = (String)map.get("database");
			String viewId1 = (String)map.get("viewid");
			if (dbName1 == null || viewId1 == null)
				continue;
			String key1 = dbName1.trim() + "|" + viewId1.trim();
			Integer lrg1 = hdrViewIdMap1.containsKey(key1) ? hdrViewIdMap1.get(key1).get(0) : null;
			List<String> fieldNameList = new ArrayList<String>();
			Wherecl wherecl = new Wherecl();
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				String fieldName = entry.getKey();
				Object value = entry.getValue();
				if (StringUtils.equalsAny(fieldName, "serial_id", "database", "viewid"))
					continue;
				if (StringUtils.equals(fieldName, "grptl_mrg"))
					value = viewRg1;
				if (StringUtils.equals(fieldName, "grptl_lrg") && lrg1 != null)
					value = lrg1;
				fieldNameList.add(fieldName);
				wherecl.appendArgument(value);
			}
			ZkUtil.executeInsertIntoSql(su, "dddviewrptlinks", fieldNameList, wherecl);
		}
	}

	public static void main(String args[]){
//
//		UniLog.log("TestSystem");
//		String ss = "";
//		checkmem () ;
//		for(int i=0;i<1000;i++) {
//			ss +="0123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890123456789";
//			
//		}
//		checkmem () ;
//		Vector memlist = new Vector();
//		for(int i=0;i<1000000;i++) {
//			//memlist.add(new Double(0.0));
//			memlist.add(new String(ss));
//		}
//		checkmem () ;
		Cell cc;
		checkmem () ;
		UniLog.log("getCellCount = "+Cell.getCellCount());
		cc = new Cell("HAHA");
		cc.setCellLabel("ABC");
		UniLog.log("getCellCount = "+Cell.getCellCount() + " cdCount " + Cell.getCdCount());
		cc = null;
//		UniLog.log("getCellCount = "+Cell.getCellCount() + " cdCount " + Cell.getCdCount());
		for(int i=0;i<20;i++) {
			try {
				Thread.sleep(1000);
			} catch (Exception x) { 
				
			}
			UniLog.log("getCellCount = "+Cell.getCellCount() + " cdCount " + Cell.getCdCount());
		}
	}
	
}
