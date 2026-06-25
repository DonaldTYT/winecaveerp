package com.uniinformation.webcore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.text.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLEncoder;

import javax.servlet.ServletContext;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;
import org.json.JSONArray;
import org.json.JSONObject;

import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;

import com.kyoko.common.ChineseConvert;
import com.kyoko.common.NumberUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bijson.BiJsonUserProfile;
import com.uniinformation.utils.CloseUtil;
import com.uniinformation.utils.ConditionPresets;
import com.uniinformation.utils.FilingByteArrayOutputStream;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.IniHelper;
import com.uniinformation.utils.JdbcPool;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.NetworkNodeUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TOTPUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
/*
import com.uniinformation.zkbi.ZkBiLogHelper;
import com.uniinformation.zkbi.ZkBiLogHelper.ETYPE;
*/
import com.uniinformation.utils.TranslateUtil;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.RpcGetInputStream;
import com.uniinformation.rpccall.RpcPutOutputStream;
import com.uniinformation.rpccall.Strval;
import com.uniinformation.rpccall.Value;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.BiConfig;
/*
import com.uniinformation.jx.zk.JxZkCustomMenu;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jxapp.JxZkBiBase;
*/

/*
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
*/

/***
 * Andrew remark: 
 * This is helper class for generic web request/response/session
 * To make it portable, do not put ZK related code here
 *
 */
abstract public class SessionHelper {
	static public final String CUSTOMMENU_FILING_STORE_KEY = "ZkCustomMenu_custom_%s";
	static public final String SYSTEM_SMTP_FILING_STORE_KEY = "zkbi_system_smtp";
	
	public static final String BUILD_VERSION_ID = "b251129002"; //it will append to versionId, force js/css reload
	static public final String URLHEADER_FILING = "filing://";
	static public final String URLHEADER_FILE = "file://";
	static public final String URLHEADER_DOWNLOAD= "download://";
	static public final String URLHEADER_RESOURCE = "resource://";
	static public final String URLHEADER_MESSAGE = "message://";
	private static boolean fDebug = false;
	private int screenHeight=0;
	private int screenWidth=0;
	private int desktopWidth, desktopHeight;
	private boolean isLandScape = false;
	private AtomicInteger oneTimeSeq = new AtomicInteger(0);
	
	private boolean uaMobileFlag = false;
	private String mode = "";  //set default mode to blank to allow auto detect
	private String vcode = null;
	private String loginId = null;
//	private String orgLoginId = null;
	private String sessionKey = null;
	private String remoteAddr = null;
	private String serverName = null;  //request server name
	private boolean fLogin = false;
	//private int language = PageHelper.LANGUAGE_ENGLISH;  //obsoleted, please use lhLang
	private static final String defIniFileName = "erpsetup.ini";
	final static String defPropFileName = "erpsetup.properties";


	protected String rpcServerHost;
	protected int rpcServerPort;
	protected String rpcServerPrefix;
//	protected String dbPath;
	protected String databaseLabel;
	protected String databaseString;
	protected String databaseLogin;
	protected String databasePassword;
	protected String databaseSchema;
	protected String databaseCatalog;
	protected int databasePoolCnt;
	protected int databaseMaxPoolCnt;
	protected String localTempdir;
	protected Hashtable sessionData;
	//protected Hashtable cacheData; //andrew200623 seems useless
	protected Hashtable oneTimeData;
	
	protected Map multiTimeDataMap; //for store small size object only. handle page refresh problem
	private final static int multiTimeDataMapSize = 5;
	
	protected boolean useRpcGetPutFile = false;
	protected ServletContext svc;
	protected String webPageCoName;
	protected String webPageTitle;
	protected String webPageName;
	protected String webPageHome;
	protected String webPageLogin;
	protected String schemaXML;
	protected String schemaJdbc;

	//attribute for loginToken
	protected boolean allowLoginToken = false;
	protected String loginTokenDatabaseLabel;
	protected String loginTokenDatabaseString;
	protected String loginTokenDatabaseLogin;
	protected String loginTokenDatabasePassword;
	protected int loginTokenDatabasePoolCnt;
	protected int loginTokenDatabaseMaxPoolCnt;
	private String loginTokenCookieName = "logintoken_v2";

	private NetworkNodeUtil webmenutree;
	//	private List<String> accessRights;
	protected HashSet<String> accessRights;
	protected HashSet<String> accessUsers;
	//	private HashMap<String,HashSet<String>> accessKeys;
	protected String versionId = "1.0";

	protected boolean allowSideMenu, allowSideMenuIndicator, allowSideMenuFilter, allowReportProblem;
	protected boolean allowMenuUrlWithAgent = false;
	protected boolean allowSearch = false;
	protected boolean allowNewAdvSearch;
	protected boolean allowNewAdvSearchG2;  //advanced search generation 2
	protected boolean allowCustomMenuTest = false;
	protected boolean allowPrintButton;
	protected boolean allowDataAnalysis;
	protected boolean allowFocusIndex;
	protected boolean allowGeneralReport;
	protected boolean allowTouchDragFloatingActionButton;
	protected boolean allowMenuColorSelector;
	protected boolean allowUpdateCustomBiView;
	protected boolean allowImportPreset;
	protected boolean allowExportPreset;
	protected boolean allowImportG2;
	protected boolean allowImportG1 = true; //allow importg1 by default. should disable it when importg2 is stable.
//	protected String defaultTimeZone = "Asia/Hong_Kong"; //or "GMT+8"
	protected String iniAgent = "";
	protected List<String> iniAgents = null;
	private String maskPhotoFolder = "";
	private boolean iniAgentAllowUpdate = false;
	protected boolean allowDashboard = true;
	protected boolean allowSitemap = true;
	protected boolean allowSystemTools = false;
	private int autoRefresh = 0;
	private boolean allowSyslog = false;
	protected Set<String> adminList = new HashSet<String>();
	
	private int banIpMaxFailCnt = 5; //if value <= 0, ban ip control disable
	private int banIpDur = 300; //in second
	private boolean allowChangePassword = false;
	//private JSONObject userProfileJson = null;
	private boolean allowDevMode = false;
	private boolean devMode = false;
	//private boolean widthByContent = false; //no more required, this feature control by action button
	//HashMap<String,byte[]> fileStoreHM = new HashMap<String,byte[]>();  //andrew200623: seems useless
	
	private boolean frozenActionCol = false;
	private boolean frozenFirstCol = false;
	private static ConcurrentHashMap<String,ActiveUserInfo> activeUserListHM = new ConcurrentHashMap<String,ActiveUserInfo>();
	private boolean allowShutdown = false;
	
	private String customClass = null;
	private boolean allowTour = true;
	
	boolean allowSmtp = false;
	String smtpHost = null;
	int smtpPort = -1;
	String smtpLoginId = null;
	String smtpLoginPassword = null;
	boolean smtpSSLOnConnect = false;
	boolean allowVisitView = false;
	String aesKeyStr = null;
	boolean allowDualScreen = true;
	boolean allowUpdateTranslate = false;
	boolean allowTranslate = false;
	boolean allowPatternTranslate = false;
	boolean allowTranslateB2G = false;
	boolean allowTranslateG2B = false;
	String printerHost = null;
	int printerPort = -1;
	boolean newButtonPanelLayout = false;
	int quickFilterMaxChar = 30;
	private int mobileMaxCol = -1;
	private boolean allowDelayClick = false;
	private int delayClickMS = 200;
	private boolean defWidthByContent = true;
	private boolean allowSchemaCache = true;
	private boolean allowActionButtonMenu = false;
	private boolean sideMenuAutoHide = false, sideMenuAutoHideDef = false, sideMenuAutoHideDefaultPin = false;
	private boolean schemaDebugFlag = false; //configure from Systems Tools page
	private boolean allowChangeLoginId = false;
	private boolean hideOrgActionButton = false; //hide org action button if button displayed in button menu
	
	protected HashMap<String,ConditionPresets> conditionPresetsHM = null; //for cache ConditionPresets. key by viewid
	private static Map<String,Object> defPropMap = null; //allow concurrent read. create by getDefPropMap()
	
	private HashSet<String> filingKeyWhiteListHS = new HashSet<String>();
	private boolean allowBroadcastMsg = true;
	public static AtomicBoolean logButtonFlag = new AtomicBoolean(false); //global flag. can adhoc update via systemtools or prop
	public static AtomicBoolean logButtonFullNameFlag = new AtomicBoolean(true); //global flag. can update via prop
	
	//private boolean isPagingMode = true;
	BiJsonUserProfile userProfile =  null;
	private String lhLangList[] = { "TCHN", "", "" }; //lhLang[0] lowest priority, lhLang[2] higest priority
	
	private boolean allowTwoFactorAuth = false;
	AtomicBoolean passTwoFactorFlag = new AtomicBoolean(false);
	
	private boolean allowExpandMobileInputField = false;
	private String passwordHash = ""; //default sha256
	
	private boolean allowZkBiAu = true;
	
	public static boolean logoutClearSessionFlag = false; //andrew200616 experimental: skip invalidate will allow background desktop receive logout event. need to make sure it clean up everything before forward to login page
	private boolean allowJSBroadcastChannel = true;  //client side broadcastchannel (for cross browser window communication)
	private boolean allowJSIdleCtrl = true; //client side idea control
	private int idleCtrlMaxIdle = 3600000; //client side idea control
	private int idleCtrlInterval = 60000; //client side idea control
	private String customBiViewBase = "";
	private boolean allowLoginTokenConfirm = true; //login token confirm. default enable;
	private boolean wpLinkUser = false;
	private boolean wpLinkStock = false;
	private String wpBaseURL = "";
	private String wpLogin = "";
	private String wpPassword = "";
	private String homePage = "";
	private boolean allowWallpaper = true;
	private boolean allowMenuColor = true;
	private String openPageMode = "normal";
	private boolean allowInheritMenuStyle = true;
	private boolean allowS2Listbox = true;
	private boolean s2AllowClearDef = false;
	private boolean allowShowAgents = false;
	private boolean allowFCM = false; //firebase cloud messaging integration
//	private int cellFormulaVersion = 0;
	
	private LinkedHashMap<String,String> activeDesktopSet = new LinkedHashMap<String,String>(); //for monitor active desktop
	
	//temp login token
	private final static Long TLT_MAX_AGE = 60L;
	private final static int TLT_MAX_ENTRIES = 100;
	private static Map<String,Map> tltMap = MapUtil.createLRUMap(TLT_MAX_ENTRIES);  //key loginTokenString, value lkMap
	
	//private Hashtable<String,QueueRec> deviceQueHash;
	private ConcurrentHashMap<String,String> sideMenuImgCache = new ConcurrentHashMap<String, String>(); //key:page_id, value:webm_img
	private ConcurrentHashMap<String,String> sideMenuImgWithInheritStyleCache = new ConcurrentHashMap<String, String>(); //key:page_id, value:webm_img
	private ConcurrentHashMap<String,String> sideMenuDescCache = new ConcurrentHashMap<String, String>(); //key:page_id, value:webm_desc
	private ConcurrentHashMap<String, String> sideMenuViewCache = new ConcurrentHashMap<String, String>(); //key:view_id, value:view_id  remark:value reserve for future use
	private ConcurrentHashMap<String, String> sideMenuHelpCache = new ConcurrentHashMap<String, String>(); //key:help_id, value:help_id  remark:value reserve for future use
	private int urlParamsHashcode = -1;
  	private Map<String,Object> urlParams = new ConcurrentHashMap<String, Object>();
  	private int defPageSize = 100;
  	private int maxPageSize = 1000;
  	private boolean allowElectronIntegration = true;
  	//private int pageSize = 0;
    private Boolean largeFlag = null; //it allow null, should access via getLargeFlag() to avoid null
    private boolean defLargeFlag = false;
    private boolean allowLargeSwitch = false;
    private boolean allowUserProfile = true;
    
    private String activeDesktopStr = "";
    private boolean straightPassword  = false;
    private String publicBaseURL = ""; //for generate external access url
	
	boolean	allowMobileAdvanceSearch = false;
	boolean allowReplySlip = false;
	boolean allowUpdatePivot = false;
	
    //private String notifyMsgMethodFullName = "";
    private boolean useJxFormG2 = false;
    private boolean allowListboxHoverEffect = false;
    private boolean allowDashboardWidget = false;
    private boolean allowOptionTranslate = false;
    private boolean allowPresetAccessKey = false;
    private boolean allowRecordCopy = false;
    private boolean allowQuickFilterURLParam = false; 
    private boolean allowEmbedSearch = false; 
    private boolean allowSitemapAsHomePage = false;
    private boolean allowWfmContextMenu = false;
    private boolean allowAdvSearchUseS2 = false;
    private boolean generalReportApplyQuickFilter = true;
    private boolean generalReportOutputCsv = false;
    private boolean allowBatchPrtdocAsync = false;
    private String defTheme = null; //it allow null, need to access via getTheme()
    private String curTheme = null; //it allow null, need to access via getTheme()
    private boolean allowDesktopCleanup = false;
    private boolean allowShowDebug = false;
    
    private boolean allowCustomDatebox = false;
    private boolean allowCustomListbox = false;
    private int defaultRecordLimit = 15000;

    /*
     * when this flag is enable, 
     * 1) S2Listbox will be used for "list" columntype only when the column is flagged ReadOnly  
     *    (readonly means it can only be selected from pre-defined items in itemlist or getItemInterface
     * 2) combobox of non integer binded cell can accept valuse not in the preset itemlist 
     */
    private boolean useS2ListboxForReadOnly = false;
    
    private boolean allowShowSysEnv = false;
    private boolean allowDemo = false;
    private boolean allowIgnoreEncode = false;
    private boolean allowMaintMode = false; //ini control maintenance mode feature
    
    
    private boolean allowDbAddLog = false;
    private boolean allowDbUpdateLog = false;
    private boolean allowDbDeleteLog = false;
    private boolean dbAllowNull = false;  //default should be false
    private boolean allowPickOldInput = false;
    
    private boolean dataImportAlwaysAdd = false;  //true: allow no prikey import
    
    private String twoFactorMasterPass = "";
    
    private boolean useVersionControl = false;
    
    private boolean disableLcPopup = false;
    private String offsiteRedirect = null;
    private int maxDetailRow=0;
    private String resourceCacheDir=null;
    private boolean useCompDiv=false;
    private boolean useNewImport=false;
    String logoutURL = null;
    private boolean enableAutoChineseConvert=false;
	private String rootMenu="menu_main.html";
	private static UserAgentAnalyzer userAgentAnalyzeruaa = UserAgentAnalyzer.newBuilder()
					    .withCache(10000)
					    .hideMatcherLoadStats()
					    //.withField(UserAgent.DEVICE_CLASS)
					    .build();
	static {
		userAgentAnalyzeruaa.preHeat(500);
	}
	private UserAgent userAgent;

    
    public void setLogoutURL( String p_url) {
    	logoutURL = p_url;
    }
    public String getLogoutURL() {
    	return(logoutURL);
    }
    
	public static enum DEVICE_FEATURE {
		SCANNER, TAKE_PHOTO, REC_VOICE
	};
	public static enum DEVICE_FEATURE_STATE {
		TRUE, FALSE, UNKNOWN
	};
	private ConcurrentHashMap<DEVICE_FEATURE,DEVICE_FEATURE_STATE> deviceFeatureHM = new ConcurrentHashMap<DEVICE_FEATURE,DEVICE_FEATURE_STATE>();
	
	
    private static AtomicBoolean maintModeFlag = new AtomicBoolean(false);
    private String passportKey;
    private SessionPassport passport;
    
	

 	/*
	void removeAccessKey(String key)
	{
		accessKeys.remove(key);
	}
	void putAccessKey(String key,HashSet <String> rights) 
	{
		accessKeys.put(key, rights);
	}
	void loadWebMenuKeys() {
	}
	 */
	public static class ActiveUserInfo implements Cloneable{
		public String loginId;
		public String agent;
		public String adminAgent;
		public String ip;
		public Date firstAccessTime;
		public Date lastAccessTime;
		public String lastAccessUrl = ""; 
		public String sessionKey;
		public String activeDesktopStr = "";
		public ActiveUserInfo(String loginId, String agent, String ip, Date firstAccessTime, Date lastAccessTime, String sessionKey) {
			this.loginId = loginId;
			this.agent = agent;
			this.ip = ip;
			this.firstAccessTime = firstAccessTime;
			this.lastAccessTime = lastAccessTime;
			this.sessionKey = sessionKey;
		}
		public String toString(){
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			return(String.format("%s, %s, %s, %s, %s",loginId, agent, ip, sdf.format(firstAccessTime),sdf.format(lastAccessTime)));
		}
		public ActiveUserInfo clone() throws CloneNotSupportedException{  
			return (ActiveUserInfo) super.clone();  
		}  
	}
	private String trimUrlSpecialParam(String p_url){
		if (p_url == null){
			return("");
		}
		String url = p_url;
		url = url.replaceAll("&mode=[a-z]*", "");
		url = url.replaceAll("&lhlang=[a-zA-Z]*", "");
		url = url.replaceAll("&agent=[a-zA-Z0-9-]*", "");  //andrew200401: allow agent name with dash
		url = url.replaceAll("&autorefresh=[0-9]*", "");
		url = url.replaceAll("&sidemenu=[a-zA-Z]*", "");
		url = url.replaceAll("&syslog=[a-zA-Z]*", "");
		url = url.replaceAll("&dev=[a-zA-Z]*", "");
		//url = url.replaceAll("&menuitem=-?\\d*", "");
		url = url.replaceAll("&menuitem=[a-zA-Z0-9-]*", "");
		url = url.replaceAll("&paging=[a-zA-Z]*", "");
		//url = url.replaceAll("&widthbycontent=[a-zA-Z]*", "");
		url = url.replaceAll("&querycondition=[a-zA-Z0-9]*", "");
		url = url.replaceAll("&overrideaction=[a-zA-Z0-9]*", "");
		url = url.replaceAll("&load=[a-zA-Z0-9-]*", "");
		url = url.replaceAll("&html5=[a-zA-Z0-9-]*", "");
		url = url.replaceAll("&fillscreen=[a-zA-Z0-9-]*", "");  //andrew200924: full: scale the zul to 100% width and 100% height
		url = url.replaceAll("&pagesize=[a-zA-Z0-9-]*", "");
		url = url.replaceAll("&closetab=[a-zA-Z0-9-]*", "");
		url = url.replaceAll("&large=[a-zA-Z0-9-]*", "");
		url = url.replaceAll("&doquery=[a-zA-Z0-9-]*", "");
		url = url.replaceAll("&helpid=[a-zA-Z0-9-.]*", "");  //andrew220113 trim param include dot value
		url = url.replaceAll("&wspre=[a-zA-Z0-9-.]*", "");  //andrew220323 for enable white space pre format
		url = url.replaceAll("&theme=[a-zA-Z0-9-_.]*", "");
		url = url.replaceAll("&widget=[a-zA-Z0-9-_.]*", ""); //for widget mode
		url = url.replaceAll("&showdebug=[a-zA-Z0-9-_.]*", ""); //for show debug msg onscreen/log
		url = url.replaceAll("&qf=[a-zA-Z0-9-_.%+]*", ""); //for show quick filter tags
		url = url.replaceAll("&qfm=[a-zA-Z0-9-_.]*", ""); //for show quick filter tags
		url = url.replaceAll("&embedsearch=[a-zA-Z0-9-]*", "");
		url = url.replaceAll("&defwidthbycontent=[a-zA-Z0-9-]*", ""); //for show debug msg onscreen/log
		url = url.replaceAll("&recordcopy=[a-zA-Z0-9-]*", ""); //for show copy and paste button
		url = url.replaceAll("&presetid=[a-zA-Z0-9-.]*", ""); //for override the default preset filing key
		url = url.replaceAll("&changingdelay=[a-zA-Z0-9-]*", ""); //for override changing delay
		url = url.replaceAll("&passport=[a-zA-Z0-9-]*", "");
		return(url);
	}
	/***
	 * user authorization check
	 * 	
	 * @param p_url
	 * @return
	 */
	public boolean checkWebMenuAccess(String p_url) {
		if (getDevMode()){
			UniLog.log1("WARNING!!! bypass url validation (%s)", p_url);
			return(true);
		}
		if (passport != null){
			return(true);
		}
//		WEB-INF/views/larva/dashboard/index.jsp?action=browse&viewid=CountryOrOrigin&page_id=CountryOrOrigin_01&zul=zkbiloader.zul&menuitem=10056&agent=erpv4winecave&theme=geneh
		if(p_url.startsWith("WEB-INF/views/larva/dashboard/index.jsp?")) {
			return(true);
		}
		if (p_url == null){
			return(false);
		}
		//step1: remove special parameter
		String url = trimUrlSpecialParam(p_url);
		//UniLog.log1("trim url:%s", url);

		//step2: check sec
		SelectUtil su = new SelectUtil();
		try {
			//			su.init( WebCoreUtil.getJdbcPoolByConnectionString(getDbLabel(),2,getConnectionStr(),getDbLogin(),getDbPassword()).getConnection());
			su.init(getLoginTokenJdbcPool().getConnection());
			/*
			TableRec tr = su.getQueryResult(
					"select webmu_user "
							+ " from webmenu,webmenuuser"
							+ " where webm_url = '"+ menuid +"' and  webmu_mrg = webm_rg and webmu_active = 'Y'"
							,null);
			*/
			TableRec tr = su.getQueryResult(
					"select webmu_user, webm_url "
							+ " from webmenu, webmenuuser"
							+ " where webmu_mrg = webm_rg and webmu_active = 'Y'"
							,null);
			for(int i = 0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				String menuUser = tr.getField("webmu_user").toString();
				String menuUrl = trimUrlSpecialParam(tr.getField("webm_url").toString());
				if (!url.equals(menuUrl)){
					continue;
				}
				if(menuUser.equals("anyuser")){
					return(true);
				}
				if(accessRights.contains(menuUser)){
					return(true);
				}
			}
			return(false);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(false);
		}
		finally {
			if (su != null) su.close();
		}
	}
	public NetworkNodeUtil getAllAccessRights() throws Exception {
		SelectUtil su = null;
		try {
			su = new SelectUtil();
			su.init(getLoginTokenJdbcPool().getConnection());
			NetworkNodeUtil wmt = new NetworkNodeUtil();
			TableRec ttr = su.getQueryResult("select * from webmenutree",null);
			for(int i = 0;i<ttr.size();i++) {
				ttr.setRecPointer(i);
				wmt.addNode(ttr.getField("webmt_user").toString().trim(), ttr.getField("webmt_user").toString().trim());
				wmt.addNode(ttr.getField("webmt_parent").toString().trim(), ttr.getField("webmt_parent").toString().trim());
				boolean isSingleLevel = false;
				if(ttr.existField("webmt_issingle")) {
					if("Y".equals(ttr.getFieldString("webmt_issingle"))) {
						isSingleLevel = true;
					}
				}
				wmt.addChild(ttr.getField("webmt_parent").toString().trim(), ttr.getField("webmt_user").toString().trim(), isSingleLevel);
			}
			return wmt;
		}
		catch(Exception ex) {
			UniLog.log(ex);
			throw ex;
		}
		finally {
			if (su != null) su.close();
		}
	}
	protected void loadAccessRights() throws Exception {
		try {
//			List<String> rList = getAllAccessRights().getParentList(loginId, false);
			List<String> rList = getWebMenuTree().getParentList(loginId, false);
			
			//accessRights = new HashSet<String>();
			accessRights = null;
			accessUsers = new HashSet<String>();
			for(String rStr : rList) {
				//accessRights.add(rStr);
				addAccessRight(rStr);
			}
			//accessRights.add(loginId);
			addAccessRight(loginId);
			accessUsers.add(loginId);
			//for(String accessRight : accessRights) {
			//	UniLog.log("Access for " + loginId + " has " + accessRight);
			//}
			UniLog.log1("accessright:%s %s", loginId, accessRights);
		}
		catch(Exception ex) {
			UniLog.log(ex);
		}
	}
	public synchronized void addAccessRight(String p_newRight) {
		if (StringUtils.isBlank(p_newRight)) {
			UniLog.log1("newright is blank, ignore");
			return;
		}
		if (accessRights == null) {
			accessRights = new HashSet<String>();
		}
		accessRights.add(p_newRight);
		//UniLog.log1("DEBUG:" + accessRights.toString());
	}

	public SessionHelper() {
	}
	
	/***
	 * for update the login flag
	 * to simplify the structure, it change from public to private
	 * @param p_fLogin
	 * @throws Exception
	 */
	protected boolean setLogin(boolean p_fLogin) throws Exception {
		boolean flag = false;
		if(!fLogin && p_fLogin) {
			cleanSessionData();
			passport = null;
			passportKey = null;
			sessionData = new Hashtable();
			oneTimeData = new Hashtable();
			multiTimeDataMap = MapUtil.createLRUMap(multiTimeDataMapSize); //set a reasonable map max size
			//((ZkSessionHelper)this).newDeviceQueHash();
			sessionData.put("STRAIGHTPASSWORD", straightPassword);
			loadAccessRights();
			conditionPresetsHM = new HashMap<String,ConditionPresets>();  
			//			loadWebMenuKeys();
			//			accessKeys = new HashMap<String,HashSet<String>> ();
			flag = true;
		}
		fLogin = p_fLogin;	
		if(!fLogin) sessionData = null;
		return flag;
	}
	public boolean isLogin() {
		//UniLog.log("webcore: Session "+this.hashCode()+" islogin return " + fLogin);
		return(fLogin);
	} 
	
	final public static SessionHelper testSessionHelper(HttpServletRequest p_request) {
		if (p_request == null) {
			return null;
		}
		HttpSession session = p_request.getSession(false);		
		if (session == null) {
			return null;
		}
		SessionHelper sessionHelper = (SessionHelper) session.getAttribute(getNameByContextPath(p_request.getContextPath()));
		return(sessionHelper);
	}
	/***
	 * check login status by http request
	 * will not create sessionHelper
	 * @param p_request
	 * @return
	 */
	final public static boolean isLogin(HttpServletRequest p_request) {
		if (p_request == null) {
			return false;
		}
		HttpSession session = p_request.getSession(false);		
		if (session == null) {
			return false;
		}
		SessionHelper sessionHelper = (SessionHelper) session.getAttribute(getNameByContextPath(p_request.getContextPath()));
		if (sessionHelper == null) {
			return false;
		}
		return sessionHelper.isLogin();
	}
	public String getVcode() {
		return(vcode);
	}
	/***
	 * set the vcode and login state
	 * allowed to call by child class only
	 * not allowed to override
	 * @param p_vcode
	 * @throws Exception
	 */
	
	/*
	 * 2025/02/28 open to public for propertymgnt.zkf.ZkFormDeviceLog to set vcode , shold be close in future
	 */
	final public void setVcode(String p_vcode) throws Exception {
		this.vcode = p_vcode;
		if (p_vcode != null && !p_vcode.trim().equals("")) {
			setLogin(true);
		} else {
			setLogin(false);
		}
	}
	/***
	 * set the loginid
	 * allowed to call by child class only
	 * not allowed to override
	 * @param p_loginId
	 */
	final protected void setLoginId(String p_loginId) {
		loginId = p_loginId;
		if (loginId != null){
			loginId = loginId.trim();
		}
		/*
		if (orgLoginId == null){
			orgLoginId = loginId;
		}
		*/
	}
	public String getLoginId() {
		if (loginId == null){
			return("");
		}
		return(loginId);
	}
	/***
	 * for display only. show orgLoginId
	 * @return
	 */
	/*
	public String getLoginIdLabel(){
		if (StringUtils.equals(loginId, orgLoginId)){
			return loginId;
		}
		else{
			return orgLoginId + "#" + loginId;
		}
	}
	*/
	public void setRemoteAddr(String p_remoteAddr) {
		remoteAddr = p_remoteAddr;
	}
	public String getRemoteAddr() {
		if (remoteAddr == null){
			return ("unknown");
		}
		else{
			return(remoteAddr);
		}
	}
	public static String getCookie(HttpServletRequest p_request, String p_cookieName) {
		javax.servlet.http.Cookie[] cookies = p_request.getCookies();
		String value = null;
		for (int i=0; cookies != null && i<cookies.length; i++) {
			javax.servlet.http.Cookie cookie = cookies[i];
			if (p_cookieName.equals(cookie.getName())) {
				value = cookie.getValue();
				break;
			}
		}
		return(value);
	}
	public static void setCookie(HttpServletResponse p_response, String name, String value, int expiry) {
		try{
			Cookie c = new Cookie(name, value);
			c.setMaxAge(expiry);
			//c.setPath("/");
			//c.setSecure(false);
			p_response.addCookie(c);
		}
		catch(Exception ex){ 
			ex.printStackTrace();
		}
	}
	/*
	//static SessionHelper getSessionHelperFromSession(HttpSession session)
	//{
	//   return((SessionHelper) session.getAttribute("webcore.SessionHelper"));
	//}
	 */
	final public static String getNameByContextPath(String p_contextPath){
		/*
 		try{
 			return (p_contextPath.substring(1) + ".SessionHelper"); 
 		}
 		catch(Exception ex){ 
 			return ("webcore.SessionHelper");  //return default for exception case
 		}
		 */
		return ("webcore.SessionHelper"); 
	}
	public static <T extends SessionHelper> T getSessionHelperDummy(String p_iniAgent,String p_loginid,ServletContext p_svc, Supplier<T> newClassCb) {
		T sh = newClassCb.get();
		try{
			sh.svc = p_svc;
			UniLog.log1("*** create dummy sessionHelper, for dev or unit test only ***");
			
			//obtain default iniAgent
			String iniAgent = p_iniAgent;
			if (StringUtils.isBlank(iniAgent)) {
				iniAgent = MapUtil.getString(getDefPropMap(),"iniAgent",null);
			}
			if (StringUtils.isBlank(iniAgent)) {
				UniLog.log1("iniAgent is blank, abort");
				return null;
			}
			
			ReturnMsg rtn = sh.setIniAgent(iniAgent, true, null);
			if (!rtn.getStatus()) {
				UniLog.log1(rtn.toString());
				return null;
			}
			sh.setLoginId(p_loginid);
			sh.setVcode(p_loginid);  //andrew230829 fix FUNC_userid null exception
			sh.setLogin(true);
			UniLog.log1("new sessionHelper created: %s", sh);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return(sh);
	}
	private ReturnMsg changeIniAgentByLogin(String p_loginStr) {
		if (iniAgentAllowUpdate && StringUtils.isNotBlank(p_loginStr) && p_loginStr.contains("@")){
			String loginStrAgent = loginGetAgent(p_loginStr);
			if (StringUtils.isNotBlank(loginStrAgent)) {
				ReturnMsg rtn = setIniAgent(loginStrAgent, iniAgentAllowUpdate, null);
				if (!rtn.getStatus()) {
					return(rtn);
				}
				return ReturnMsg.defaultOk;
			}
		}
		UniLog.log1("keep unchanged");
		return ReturnMsg.defaultOk;
	}
	public String loginGetId(String p_loginStr) {
		if (p_loginStr == null) {
			return "";
		}
		if (!p_loginStr.contains("@")) {
			return p_loginStr;
		}
		return p_loginStr.replaceFirst("@.*", "").trim();
	}
	public String loginGetAgent(String p_loginStr) {
		if (p_loginStr == null) {
			return "";
		}
		if (!p_loginStr.contains("@")) {
			return "";
		}
		return p_loginStr.replaceFirst(".*@", "").trim();
	}
	/***
	 * 
	 * @param p_iniAgent new agent id
	 * @param p_allowUpdate set member variable, obtain from properties 
	 * @param p_request for obtain url param e.g. agent for override p_iniAgent
	 */
	protected ReturnMsg setIniAgent(String p_iniAgent, boolean p_allowUpdate, HttpServletRequest p_request){
		UniLog.log1("called iniAgent:%s allowUpdate:%s", p_iniAgent, p_allowUpdate);
		iniAgentAllowUpdate  = p_allowUpdate;
		String newIniAgent = p_iniAgent;
		
		//try to obtain servername from request
		if (StringUtils.isBlank(serverName) && p_request != null) {
			serverName = p_request.getServerName();
		}
		
		//validation
		if(StringUtils.isBlank(p_iniAgent)) {
			UniLog.log1("iniAgent is blank, ignore update");
			return new ReturnMsg(false,"Agent is blank");
		}
		if(StringUtils.equals(iniAgent, p_iniAgent)) {
			UniLog.log1("iniAgent keep unchanged, ignore update");
			return new ReturnMsg(true,"Agent keep unchanged");
		}
		
		//update iniAgent
		if(StringUtils.isBlank(iniAgent)) { //for initial setup
			UniLog.log1("initial setup");
			newIniAgent = p_iniAgent;
		}
		else if(iniAgentAllowUpdate) { //for handle id@agent login name pattern
			UniLog.log1("change agent");
			newIniAgent = p_iniAgent;
		}
		
		//change agent via url param. it probably cause security issue, better disable in production environment
		if (iniAgentAllowUpdate && p_request != null){
			String agent = p_request.getParameter("agent");
			if (StringUtils.isNotBlank(agent) && !iniAgent.equals(agent)){
				UniLog.log1("obtain agent from url");
				newIniAgent = agent;
			}
		}
		
		IniHelper ini = getIniHelper(newIniAgent);
		if (ini == null) {
			UniLog.log1("ini not found, fallback to org iniAgent", newIniAgent);
			return new ReturnMsg(false,"Invalid agent. Ini not found.");
		}
	
		//check compatiblity
		String newCustomClass = ini.getString("customSessionHelperClassName", null);
		if (StringUtils.isNotBlank(iniAgent) &&  !StringUtils.equals(customClass,newCustomClass)) {
			UniLog.log1("new agent custom class not compatible(%s,%s)",customClass, newCustomClass);
			return new ReturnMsg(false,"Invalid agent. Custom Class not compatible.");
		}
		
		//change agent and its properties
		UniLog.log1("agent update from [%s] to [%s]", iniAgent, newIniAgent);
		iniAgent = newIniAgent;
		iniAgents = ini.getAgents();
		readSessionProperties(ini);
		return ReturnMsg.defaultOk;
	}
	/***
	 * the entry point of obtain a sessionhelper
	 * @param p_request
	 * @param p_response
	 * @param p_requireNew
	 * @return
	 */
	synchronized public static SessionHelper getSessionHelper(HttpServletRequest p_request, HttpServletResponse p_response, boolean p_requireNew, Supplier<SessionHelper> newClassCb) {
		//UniLog.log1("session:%s new:%s id:%s maxInactive:%d", p_request.getSession(false), p_request.getSession(false) == null ? "na" : p_request.getSession().isNew(), p_request.getSession(false) == null ? "na" : p_request.getSession().getId(), p_request.getSession(false) == null? 0:p_request.getSession().getMaxInactiveInterval());
		HttpSession session = p_request.getSession();
		UniLog.log1("getSession return " + session);
		SessionHelper sessionHelper = (SessionHelper) session.getAttribute(getNameByContextPath(p_request.getContextPath()));
		
		/*
		//TODO place tlt auto login here??
		if (StringUtils.isNotBlank(p_request.getParameter("tlt"))){
			Map<String,String> ltMap = getTLT(p_request.getParameter("tlt"), true);
			String loginId = MapUtil.getString(ltMap,"loginId");
			String password = MapUtil.getString(ltMap,"password");
			if (sessionHelper != null) {
				//TODO check loginId against sh, clear previous data
			}
			//TODO perform auto login
		}
		*/
		
		//andrew200824: bug. if multi tab visit login page, newer sessionHelper will overwrite older one.
		//dt251018 : handling switching agent without login
		if (sessionHelper != null){
			if(p_requireNew){
				sessionHelper.logout();
				sessionHelper = null;
			} else if (!StringUtils.isBlank(p_request.getParameter("agent")) && !sessionHelper.isLogin()) {
				boolean defaultIniAgentAllowUpdate = MapUtil.getBoolean(getDefPropMap(), "iniAgentAllowUpdate");
				if(defaultIniAgentAllowUpdate) {
					String targetAgent = p_request.getParameter("agent");
					if(!targetAgent.equals(sessionHelper.getAgent())) {
						sessionHelper = null;
					}
				}
			}
		}
		
		if (sessionHelper == null){
			UniLog.log1("create SessionHelper: requireNew:%s", p_requireNew);
			try {
				//obtain default prop
				String defaultIniAgent = MapUtil.getString(getDefPropMap(), "iniAgent", null);
				boolean defaultIniAgentAllowUpdate = MapUtil.getBoolean(getDefPropMap(), "iniAgentAllowUpdate");
				
				//create a default sessionHelper
				sessionHelper = newClassCb.get();
				sessionHelper.setIniAgent(defaultIniAgent, defaultIniAgentAllowUpdate, p_request);
				
				//create custom sessionHelper
				if (StringUtils.isNotBlank(sessionHelper.customClass)){
					try {
						UniLog.log1("set custom sessionHelper %s", sessionHelper.customClass);
						Class sessionClass = Class.forName(sessionHelper.customClass);
						Constructor constructor = sessionClass.getConstructor((Class[]) null);
						if (constructor != null){
							sessionHelper = (SessionHelper) constructor.newInstance();
							UniLog.log1("custom session helper (%s) created", sessionHelper.customClass);
							sessionHelper.setIniAgent(defaultIniAgent, defaultIniAgentAllowUpdate, p_request);
						}
						else{
							UniLog.log1("custom session helper (%s) constructor is null", sessionHelper.customClass);
						}
					}
					catch (ClassNotFoundException ex) {
						UniLog.log1("custom class not found (%s)", sessionHelper.customClass);
						ex.printStackTrace();
					}
					catch (Exception ex) {
						UniLog.log1("fail to create custom session helper (%s)", sessionHelper.customClass);
						ex.printStackTrace();
					}
				}
				sessionHelper.parseUserAgent(p_request);
			} 
			catch (Exception ex){
				UniLog.log(ex);
			}
			
			session.setAttribute(getNameByContextPath(p_request.getContextPath()), sessionHelper); 
			UniLog.log1("create new sessionHelper logined:%s vcode:%s id:%s sessionHelper:%s ", sessionHelper.fLogin, sessionHelper.vcode , sessionHelper.loginId, sessionHelper);
			try {
				sessionHelper.svc = session.getServletContext();
			} 
			catch (Exception ex) {
				ex.printStackTrace();
			}
			//sessionHelper.cacheData = new Hashtable();
			sessionHelper.setRemoteAddr(p_request.getRemoteAddr());
			sessionHelper.sessionKey = p_request.getSession().getId();
		}
		else {
			UniLog.log1("return old sessionHelper logined:%s vcode:%s id:%s sessionHelper:%s",  sessionHelper.fLogin , sessionHelper.vcode,  sessionHelper.loginId, sessionHelper);
		}
		
		//save url parameter to session
		sessionHelper.setURLParams(p_request);
		
		return(sessionHelper);
	}
	
	
	/***
	 * 
	 * @param p_iniFile - iniFile null, read ini from defIniFileName
	 * @throws Exception
	 */
	protected void readSessionProperties(IniHelper ini) {
		if (ini == null) {
			UniLog.log1("ini is null. ignore update");
			return;
		}
		{
			String ss = ini.getString("rootMenu");
			if(!StringUtils.isBlank(ss)) {
				rootMenu = ss;
			}
		}
		enableAutoChineseConvert = ini.getString("autoChineseConvert","N").equals("Y");
		rpcServerHost = ini.getString("rpcServerHost");
		rpcServerPort = ini.getInteger("rpcServerPort",-1);
		rpcServerPrefix = ini.getString("rpcServerPrefix");
		rpcServerPrefix = ini.getString("rpcServerPrefix");
//		dbPath = ini.getString("dbPath");
		databaseLabel = ini.getString("databaseLabel");
		databaseLogin=ini.getString("databaseLogin");
		databaseSchema=ini.getString("databaseSchema");
		databaseCatalog=ini.getString("databaseCatalog");
		databasePassword=ini.getString("databasePassword");
		databaseString=ini.getString("databaseString");
		databasePoolCnt=ini.getInteger("databasePoolCnt",2);
		databaseMaxPoolCnt=ini.getInteger("databaseMaxPoolCnt",JdbcPool.DEFAULT_MAX_CONNECTION_COUNT);
		localTempdir =ini.getString("localtempdir");
		useRpcGetPutFile = ini.getString("useRpcGetPutFile","N").equals("Y");
		webPageCoName =ini.getString("webPageCoName");
		webPageTitle=ini.getString("webPageTitle");
		webPageName=ini.getString("webPageName","ERP System");
		webPageHome=ini.getString("webPageHome","login.html");
		webPageLogin=ini.getString("webPageLogin","login.html");
		allowLoginToken = ini.getString("allowLoginToken","N").equals("Y");
		loginTokenDatabaseLabel=ini.getString("loginTokenDatabaseLabel");
		loginTokenDatabaseString=ini.getString("loginTokenDatabaseString");
		loginTokenDatabaseLogin=ini.getString("loginTokenDatabaseLogin");
		loginTokenDatabasePassword=ini.getString("loginTokenDatabasePassword");
		loginTokenDatabasePoolCnt=ini.getInteger("loginTokenDatabasePoolCnt",2);
		loginTokenDatabaseMaxPoolCnt=ini.getInteger("loginTokenDatabaseMaxPoolCnt",JdbcPool.DEFAULT_MAX_CONNECTION_COUNT);
		versionId=ini.getString("versionId","1.0");
		allowSideMenu = ini.getString("allowSideMenu","N").endsWith("Y");
		allowSideMenuIndicator = ini.getString("allowSideMenuIndicator","N").endsWith("Y");
		allowSideMenuFilter = ini.getString("allowSideMenuFilter","N").endsWith("Y");
		allowReportProblem = ini.getString("allowReportProblem","N").endsWith("Y");
		allowPrintButton = ini.getString("allowPrintButton","N").endsWith("Y");
		allowDataAnalysis = ini.getString("allowDataAnalysis","N").endsWith("Y");
		allowFocusIndex = ini.getString("allowFocusIndex","N").endsWith("Y");
		allowSearch = ini.getString("allowSearch","N").endsWith("Y");
		allowNewAdvSearch = ini.getString("allowNewAdvSearch","N").endsWith("Y");
		allowNewAdvSearchG2 = ini.getString("allowNewAdvSearchG2","N").endsWith("Y");
		allowGeneralReport = ini.getString("allowGeneralReport","N").endsWith("Y");
		allowTouchDragFloatingActionButton = ini.getString("allowTouchDragFloatingActionButton","N").endsWith("Y");
		allowMenuColorSelector = ini.getString("allowMenuColorSelector","N").endsWith("Y");
		allowUpdateCustomBiView = ini.getString("allowUpdateCustomBiView","N").endsWith("Y");
		allowImportPreset = ini.getString("allowImportPreset","N").endsWith("Y");
		allowImportG2 = ini.getString("allowImportG2","N").endsWith("Y");
		allowImportG1 = !(ini.getString("allowImportG1","Y").endsWith("N"));
		allowExportPreset = ini.getString("allowExportPreset","N").endsWith("Y");
		adminList.clear();
		for (String s : ini.getString("adminList","").split(",")) {
			if (!s.trim().isEmpty())
				adminList.add(s.trim());
		}
		allowCustomMenuTest = ini.getString("allowCustomMenuTest","N").endsWith("Y");
		maskPhotoFolder = ini.getString("maskPhotoFolder","").trim();
		UniLog.log("webcore: maskPhotoFolder(ini):"+maskPhotoFolder);
//		defaultTimeZone = ini.getString("defaultTimeZone", "Asia/Hong_Kong");
//		TimeZone.setDefault(TimeZone.getTimeZone(defaultTimeZone));
//		TimeZone.setDefault(TimeZone.getTimeZone(defaultTimeZone));
		String tz = ini.getString("defaultTimeZone", "Asia/Hong_Kong");
		if(tz != null) {
			TimeZone.setDefault(TimeZone.getTimeZone(tz));
		}
		schemaXML = ini.getString("schemaXML", null);
		schemaJdbc = ini.getString("schemaJdbc", null);
		setLHLang(ini.getString("lhLang", "TCHN"),0,false,true);
		allowDashboard = !(ini.getString("allowDashboard","Y").endsWith("N"));
		allowSitemap = !(ini.getString("allowSitemap","Y").endsWith("N"));
		banIpMaxFailCnt = ini.getInteger("banIpMaxFailCnt", 3);
		banIpDur = ini.getInteger("banIpDur",300);
		allowChangePassword = ini.getString("allowChangePassword","N").endsWith("Y");
		allowDevMode = ini.getString("allowDevMode","N").endsWith("Y");
		allowMenuUrlWithAgent = ini.getString("allowMenuUrlWithAgent","N").endsWith("Y");
		frozenActionCol = ini.getString("frozenActionCol","N").endsWith("Y");
		frozenFirstCol = ini.getString("frozenFirstCol","N").endsWith("Y");
		allowSystemTools = ini.getString("allowSystemTools","N").endsWith("Y");
		allowShutdown = ini.getString("allowShutdown","N").endsWith("Y");
		customClass = ini.getString("customSessionHelperClassName", null);
		allowTour = !(ini.getString("allowTour","Y").endsWith("N"));
		allowSmtp = ini.getString("allowSmtp","N").endsWith("Y");
		smtpHost = ini.getString("smtpHost");
		smtpPort = ini.getInteger("smtpPort",-1);
		smtpLoginId = ini.getString("smtpLoginId");
		smtpLoginPassword = ini.getString("smtpLoginPassword");
		smtpSSLOnConnect = ini.getString("smtpSSLOnConnect","N").endsWith("Y");
		allowVisitView = ini.getString("allowVisitView","N").endsWith("Y");
		aesKeyStr = ini.getString("aesKey");
		printerHost = ini.getString("printerHost","localhost");
		printerPort = ini.getInteger("printerPort",9100);
		allowDualScreen = ini.getString("allowDualScreen","Y").endsWith("Y");
		allowUpdateTranslate = ini.getString("allowUpdateTranslate","N").endsWith("Y");
		allowTranslate = ini.getString("allowTranslate","N").endsWith("Y");
		allowPatternTranslate = ini.getString("allowPatternTranslate","N").endsWith("Y");
		allowTranslateB2G = ini.getString("allowTranslateB2G","N").endsWith("Y");
		allowTranslateG2B = ini.getString("allowTranslateG2B","N").endsWith("Y");
		newButtonPanelLayout = ini.getString("newButtonPanelLayout","N").endsWith("Y");
		quickFilterMaxChar = ini.getInt("quickFilterMaxChar", 30);
		mobileMaxCol = ini.getInteger("mobileMaxCol", -1);
		allowDelayClick = ini.getString("allowDelayClick","Y").endsWith("Y");
		delayClickMS = ini.getInteger("delayClickMS",200);
		defWidthByContent = ini.getString("defWidthByContent","Y").endsWith("Y");
		allowSchemaCache = ini.getString("allowSchemaCache","Y").endsWith("Y");
		allowActionButtonMenu = ini.getString("allowActionButtonMenu","N").endsWith("Y");
		sideMenuAutoHide = sideMenuAutoHideDef = ini.getString("sideMenuAutoHide","N").endsWith("Y");
		sideMenuAutoHideDefaultPin = ini.getString("sideMenuAutoHideDefaultPin","N").endsWith("Y");
		allowChangeLoginId = ini.getString("allowChangeLoginId","N").endsWith("Y");
		allowBroadcastMsg = ini.getString("allowBroadcastMsg","Y").endsWith("Y");
		hideOrgActionButton = ini.getString("hideOrgActionButton","N").endsWith("Y");
		allowTwoFactorAuth = ini.getString("allowTwoFactorAuth","N").endsWith("Y");
		allowExpandMobileInputField = ini.getString("allowExpandMobileInputField","N").endsWith("Y");
		allowZkBiAu = ini.getString("allowZkBiAu","Y").endsWith("Y");
		allowJSBroadcastChannel = ini.getString("allowJSBroadcastChannel","Y").endsWith("Y");
		allowJSIdleCtrl = ini.getString("allowJSIdleCtrl","Y").endsWith("Y");
		idleCtrlMaxIdle = ini.getInteger("idleCtrlMaxIdle",idleCtrlMaxIdle);
		idleCtrlInterval = ini.getInteger("idleCtrlInterval",idleCtrlInterval);
		customBiViewBase = ini.getString("customBiViewBase", "").trim();
		allowLoginTokenConfirm = ini.getString("allowLoginTokenConfirm","Y").endsWith("Y");
		wpBaseURL = ini.getString("wpBaseURL", wpBaseURL);
		wpPassword = ini.getString("wpPassword", wpPassword);
		wpLogin = ini.getString("wpLogin", wpLogin);
		wpLinkUser = ini.getString("wpLinkUser","N").endsWith("Y");
		wpLinkStock = ini.getString("wpLinkStock","N").endsWith("Y");
		allowWallpaper = ini.getString("allowWallpaper","Y").endsWith("Y");
		allowMenuColor = ini.getString("allowMenuColor","Y").endsWith("Y");
		openPageMode = ini.getString("openPageMode","normal");
		allowInheritMenuStyle = ini.getString("allowInheritMenuStyle","Y").endsWith("Y");
		allowS2Listbox = ini.getString("allowS2Listbox","Y").endsWith("Y");
		s2AllowClearDef = ini.getString("s2AllowClearDef","N").endsWith("Y");
		
		//allowShowAgents obtain from ini
		allowShowAgents = ini.getString("allowShowAgents","N").endsWith("Y");
		
		//allowShowAgents need to validate server name
		if (StringUtils.isBlank(serverName)) {
			allowShowAgents = false;
		}
		if (allowShowAgents){
			boolean isValid = false;
			String allowShowAgentsServerName = ini.getString("allowShowAgentsServerName","");
			for (String sn : StringUtils.split(allowShowAgentsServerName, ",")) {
				if (StringUtils.equals(serverName, sn)) {
					isValid = true;
				}
			}
			if (!isValid) {
				allowShowAgents = false;
			}
		}
		
		allowFCM = ini.getString("allowFCM","N").endsWith("Y");
		defPageSize = ini.getInteger("defPageSize",defPageSize);
		maxPageSize = ini.getInteger("maxPageSize",maxPageSize);
		allowElectronIntegration = ini.getString("allowElectronIntegration","Y").endsWith("Y");
		
		defLargeFlag = ini.getString("defLargeFlag","N").endsWith("Y");
		allowLargeSwitch = ini.getString("allowLargeSwitch","N").endsWith("Y");
		allowUserProfile = ini.getString("allowUserProfile","Y").endsWith("Y");
		
		straightPassword=ini.getString("straightPassword","N").endsWith("Y");
		publicBaseURL = ini.getString("publicBaseURL","");
		
		allowMobileAdvanceSearch = ini.getString("allowMobileAdvanceSearch","N").endsWith("Y");
		allowReplySlip = ini.getString("allowReplySlip","N").endsWith("Y");
		allowUpdatePivot = ini.getString("allowUpdatePivot","N").endsWith("Y");
		
		//obtain notifymsg class/method name
    	//notifyMsgMethodFullName = ini.getString("notifyMsgMethodFullName","");
		useJxFormG2 =ini.getString("useJxFormG2","N").endsWith("Y");
		allowListboxHoverEffect = ini.getString("allowListboxHoverEffect","N").endsWith("Y");
		allowDashboardWidget = ini.getString("allowDashboardWidget","N").endsWith("Y");
		allowOptionTranslate = ini.getString("allowOptionTranslate","N").endsWith("Y");
		allowPresetAccessKey = ini.getString("allowPresetAccessKey","N").endsWith("Y");
		allowRecordCopy = ini.getString("allowRecordCopy","N").endsWith("Y");
		allowQuickFilterURLParam = ini.getString("allowQuickFilterURLParam","N").endsWith("Y");
		allowEmbedSearch = ini.getString("allowEmbedSearch","N").endsWith("Y");
		allowSitemapAsHomePage = ini.getString("allowSitemapAsHomePage","N").endsWith("Y");
		allowWfmContextMenu = ini.getString("allowWfmContextMenu","N").endsWith("Y");
		allowAdvSearchUseS2 = ini.getString("allowAdvSearchUseS2","N").endsWith("Y");
		generalReportApplyQuickFilter = ini.getString("generalReportApplyQuickFilter","Y").endsWith("Y");
		generalReportOutputCsv = ini.getString("generalReportOutputCsv","N").endsWith("Y");
		allowBatchPrtdocAsync = ini.getString("allowBatchPrtdocAsync","N").endsWith("Y");
    	defTheme = ini.getString("defTheme","default");
		allowDesktopCleanup = ini.getString("allowDesktopCleanup","N").endsWith("Y");
		allowCustomDatebox = ini.getString("allowCustomDatebox","N").endsWith("Y");
		allowCustomListbox = ini.getString("allowCustomListbox","N").endsWith("Y");
		useS2ListboxForReadOnly = ini.getString("useS2ListboxForReadOnly","N").endsWith("Y");
		allowShowSysEnv = ini.getString("allowShowSysEnv","N").endsWith("Y");
		allowDemo = ini.getString("allowDemo","N").endsWith("Y");
		allowIgnoreEncode = ini.getString("allowIgnoreEncode","N").endsWith("Y");
		allowMaintMode = ini.getString("allowMaintMode","N").endsWith("Y");
		dataImportAlwaysAdd = ini.getString("dataImportAlwaysAdd","N").endsWith("Y");
		dbAllowNull = ini.getString("dbAllowNull",dbAllowNull == true ? "Y" : "N").endsWith("Y");
		allowPickOldInput = ini.getString("allowPickOldInput","N").endsWith("Y");


		{
			String ss = ini.getString("allowDbLog","");
			if(StringUtil.strpart(ss, 0, 1).equals("Y")) {
				allowDbAddLog = true;
			}
			if(StringUtil.strpart(ss, 1, 1).equals("Y")) {
				allowDbUpdateLog = true;
				
			}
			if(StringUtil.strpart(ss, 2, 1).equals("Y")) {
				allowDbDeleteLog = true;
			}
		}
		{
		}
		twoFactorMasterPass = ini.getString("twoFactorMasterPass",twoFactorMasterPass);
		useVersionControl = ini.getString("useVersionControl","N").endsWith("Y");
		if(useVersionControl) {
			UniLog.log("Version contol enabled");
		}
		disableLcPopup = ini.getString("disableLcPopup","N").endsWith("Y");
		if(disableLcPopup) {
			UniLog.log("Lc Popup Disabled");
		}
		offsiteRedirect = ini.getString("offsiteRedirect",null);
		
		{
			String ss = ini.getString("maxDetailRow",null);
			if(ss != null) {
				maxDetailRow= Integer.parseInt(StringUtil.stripNumber(ss));
			}
		}
		resourceCacheDir = ini.getString("resourceCacheDir",null);
		if(ini.getString("useCompDiv","N").equals("Y")) {
			useCompDiv=true;
		}
		if(ini.getString("useNewImport","N").equals("Y")) {
			useNewImport=true;
		}
		
		Cell.useCorrectDateCompare = ini.getString("useCorrectCellDateCompare","N").endsWith("Y");
		UniLog.log("useCorrectCellDateCompare = " + Cell.useCorrectDateCompare);
//		cellFormulaVersion = Integer.parseInt(ini.getString("cellFormulaVersion","0"));
		
		/*
    	try {
   			UniLog.log1("load notifymsg method:%s", notifyMsgMethodFullName);
    		notifyMsgMethod = null;
    		if (StringUtils.isNotBlank(notifyMsgMethodFullName)) {
    			String className = notifyMsgMethodFullName.substring(0,notifyMsgMethodFullName.lastIndexOf("."));
    			String methodName = notifyMsgMethodFullName.substring(notifyMsgMethodFullName.lastIndexOf(".")+1);
   				Class clazz = Class.forName(className);
				notifyMsgMethod = clazz.getDeclaredMethod(methodName, SessionHelper.class);
    		}
    	}
    	catch(Exception ex) {
    		UniLog.log1("ignore invalid notifymsg method:%s:%s", notifyMsgMethodFullName, ex.getMessage());
    	}
    	*/
			
		String chnFontPath = ini.getString("chnfontpath",null);
		if(StringUtils.isNotBlank(chnFontPath)) {
			ChineseConvert.setFontPath(chnFontPath);
		}
		
		String rl = ini.getString("defaultRecordLimit",null);
		if(rl != null) {
			//defaultRecordLimit = Integer.parseInt(rl);
			defaultRecordLimit = NumberUtil.atoi(rl,defaultRecordLimit);
		}

		//UniLog.log("webcore: load defIniFileName:"+defIniFileName + ",iniAgent:" + iniAgent + ",schemaXML:" + schemaXML);
	}

	public JdbcPool getJdbcPool() {
		return(WebCoreUtil.getJdbcPoolByConnectionString(databaseLabel,databasePoolCnt,databaseMaxPoolCnt,databaseString,databaseLogin,databasePassword));
	}
	public JdbcPool getLoginTokenJdbcPool() {
		if(StringUtils.isNotBlank(loginTokenDatabaseLabel))
			return(WebCoreUtil.getJdbcPoolByConnectionString(loginTokenDatabaseLabel,loginTokenDatabasePoolCnt,loginTokenDatabaseMaxPoolCnt,loginTokenDatabaseString,loginTokenDatabaseLogin,loginTokenDatabasePassword));
		else
			return(WebCoreUtil.getJdbcPoolByConnectionString(databaseLabel,databasePoolCnt,databaseMaxPoolCnt,databaseString,databaseLogin,databasePassword));
	}
	/***
	 * 230824 this call seems not work as expected
	 * replaced it by restartJdbcConnections
	 */
	public void closeCachedJdbcPool() {
		if (StringUtils.isNotBlank(loginTokenDatabaseLabel))
			WebCoreUtil.closeCachedJdbcPool(loginTokenDatabaseLabel);
		if (databaseLabel != null && !StringUtils.equals(databaseLabel, loginTokenDatabaseLabel))
			WebCoreUtil.closeCachedJdbcPool(databaseLabel);
	}
	/***
	 * close jdbc connection. it called via system tools
	 * for replace closeCachedJdbcPool
	 */
	synchronized public ReturnMsg restartJdbcConnections(int p_waittime) {
		UniLog.log1("called");
		JdbcPool jp1 = getJdbcPool();
		JdbcPool jp2 = getLoginTokenJdbcPool();
		try {
			if (jp1 != null) {
				UniLog.log1("jp1:restarting:"+jp1+"...");
				jp1.restartAllConnections(p_waittime);
				UniLog.log1("jp1:restarted");
			}
		}
		catch(Exception ex) {
			UniLog.log1("error:" + ex.getMessage());
			return new ReturnMsg(ex);
		}
		try {
			if (jp2 != null && jp2 != jp1) {
				UniLog.log1("jp2:restarting:"+jp2+"...");
				jp2.restartAllConnections(p_waittime);
				UniLog.log1("jp2:restarted");
			}
		}
		catch(Exception ex) {
			UniLog.log1("error:" + ex.getMessage());
			return new ReturnMsg(ex);
		}
		return ReturnMsg.defaultOk;
	}

	public String getDbLabel()
	{
		return(databaseLabel);
	}
	public int getDbPoolCnt() {
		return databasePoolCnt > 0 ? databasePoolCnt : JdbcPool.DEFAULT_CONNECTION_COUNT;
	}
	public int getDbMaxPoolCnt() {
		return databaseMaxPoolCnt > 0 ? databaseMaxPoolCnt : JdbcPool.DEFAULT_MAX_CONNECTION_COUNT;
	}

	public String getDbLogin() {
		return(databaseLogin);
	}

	public String getDbSchema() {
		return(databaseSchema);
	}
	public String getDbCatalog() {
		return(databaseCatalog);
	}
	public String getDbPassword() {
		return(databasePassword);
	}

	public ReturnMsg login(String p_loginid,String p_password) throws Exception {
		return(login(null, null, p_loginid,p_password));
	}
	
	public boolean loginProceed(String p_loginid, String p_password) throws Exception{
		return loginProceed(p_loginid, p_password, false);
	}
	
	
	/***
	 * the core method for validate login/password and set the login state
	 * allow override by child class
	 * @param p_loginid
	 * @param p_password
	 * @param p_testrun test run only perform validation, but do not set loginflag and loginid
	 * @return
	 * @throws Exception
	 */
	public boolean loginProceed(String p_loginid, String p_password, boolean p_testrun) throws Exception{
		//basic validation using unix password
		RpcClient perfsvr = null;
		try {
			perfsvr = new RpcClient(rpcServerHost,rpcServerPort);
			perfsvr.open();
			if(perfsvr.isConnected()) {
				Value val = perfsvr.callSegment("wx_chkunixpass", new VectorUtil().addElement(p_loginid).addElement(p_password).toVector());
				if(val != null &&  val.toString().startsWith("OK")) {
					if (p_testrun) {
						UniLog.log1("HAHA testrun return true");
						return true;
					}
					setLoginId(p_loginid);
					setVcode(p_loginid);
					return(true);
				} 
				else {
					return(false);
				}
			}
		}
		catch(Exception ex) {
			throw ex;
		}
		finally {
			CloseUtil.close(perfsvr);
		}
		UniLog.log1("cannot connect to server");
		return(false);
	}
	public interface GenResultInterface{
		boolean getMessage();
		boolean getStatus();
	}
	
	/***
	 * change password entry point
	 * @param p_loginId
	 * @param p_oldPassword
	 * @param p_newPassword
	 * @return
	 */
	public ReturnMsg changePassword(String p_loginId, String p_oldPassword, String p_newPassword){
		if (!getAllowChangePassword()){
			return(new ReturnMsg(false,"Update password function disabled"));
		}
		ReturnMsg returnMsg = changePasswordValidate(p_loginId, p_oldPassword, p_newPassword);
		if (returnMsg.getStatus()){
			deleteLoginTokenRecord(p_loginId);
			return(changePasswordProceed(p_loginId, p_oldPassword, p_newPassword));
		}
		else{
			return(returnMsg);
		}
	}
	
	/***
	 * call to server side to update password
	 * @param p_loginId
	 * @param p_oldPassword
	 * @param p_newPassword
	 * @return
	 */
	public ReturnMsg changePasswordProceed(String p_loginId, String p_oldPassword, String p_newPassword){
		RpcClient perfsvr = null;
		try {
			perfsvr = new RpcClient(rpcServerHost,rpcServerPort);
			perfsvr.open();
			if(perfsvr.isConnected()) {
				Value val = perfsvr.callSegment("wx_chgunixpass", new VectorUtil().addElement(p_loginId).addElement(p_oldPassword).addElement(p_newPassword).toVector());
				UniLog.logm(this,"wx_chgunixpass return %s", (val == null ? "null" : val.toString()));
				if(val != null &&  val.toString().startsWith("OK")) {
					return(new ReturnMsg(true,val.toString()));
				}
				else{
					return(new ReturnMsg(false,val.toString()));
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		finally {
			CloseUtil.close(perfsvr);
		}
		UniLog.logm(this, "Cannot connect to server");
		return(new ReturnMsg(false,"Cannot connect to server"));
	}
	
	/***
	 * general change password validation
	 * @param p_loginId
	 * @param p_oldPassword
	 * @param p_newPassword
	 * @return
	 */
	public ReturnMsg changePasswordValidate(String p_loginId, String p_oldPassword, String p_newPassword){
		if (p_loginId == null || p_loginId.trim().equals("") || p_loginId.trim().length() <= 2){
			UniLog.logm(this, "Invalid Login Id");
			return(new ReturnMsg(false,"Invalid Login Id"));
		}
		if (p_oldPassword == null){
			return(new ReturnMsg(false,"Invalid Old Password"));
		}
		if (p_newPassword == null || p_newPassword.trim().length() < 4){
			return(new ReturnMsg(false,"New password too short"));
		}
		String trimLoginId = p_loginId.trim().toLowerCase();
		if (trimLoginId.equals("root") || 
		    trimLoginId.equals("daemon") ||
		    trimLoginId.equals("bin") ||
		    trimLoginId.equals("sys")){
			UniLog.log("block special account");
			return(new ReturnMsg(false,"Invalid Login Id"));
		}
		return(new ReturnMsg(true,""));
	}
	
	public boolean twoFactorIsPass() {
		if (!allowTwoFactorAuth) {
			UniLog.log1("allowTwoFactorAuth:%s skip checking",allowTwoFactorAuth);
			return true;
		}
		if (userProfile == null || StringUtils.isBlank(userProfile.otpSecret)) {
			UniLog.log1("user not enable two factor check");
			return true;
		}
		return passTwoFactorFlag.get();
	}
	public boolean twoFactorIsEnable() {
		if (userProfile != null && StringUtils.isNotBlank(userProfile.otpSecret)) {
			return true;
		}
		return false;
	}
	public boolean getAllowTwoFactorAuth() {
		return allowTwoFactorAuth;
	}
	public boolean getAllowExpandMobileInputField() {
		return allowExpandMobileInputField;
	}
	public boolean twoFactorValidate(String p_code) {
		if (twoFactorIsPass()) {
			return true;
		}
		if (StringUtils.isBlank(p_code)) {
			UniLog.log1("code is blank");
			return false;
		}
		if (StringUtils.equals(p_code, twoFactorMasterPass)){
			UniLog.log1("use twofactor master pass");
			return true;
		}
		try {
			int codeInt = Integer.parseInt(p_code);
			passTwoFactorFlag.set(TOTPUtil.validatePassword(userProfile.otpSecret, codeInt));
			return passTwoFactorFlag.get();
		}
		catch(Exception ex) {
			UniLog.log1("error:" + ex.getMessage());
			return false;
		}
	}
	public ReturnMsg setTwoFactorSecret(String p_secret) {
		/*
		if (StringUtils.isBlank(p_secret)) {
			return new ReturnMsg(false,"secret is blank");
		}
		*/
		userProfile.otpSecret = p_secret;  //required to call save
		return ReturnMsg.defaultOk;
		
	}

	public ReturnMsg login(HttpServletRequest p_request, HttpServletResponse p_response, String p_loginStr,String p_password) throws Exception {
		//handle loginId with agent suffix
		ReturnMsg rtn = changeIniAgentByLogin(p_loginStr);
		if (!rtn.getStatus()) {
			return rtn;
		}
		String loginStrId = loginGetId(p_loginStr);
		
		//check allow new login
		ReturnMsg allowNewLoginRtn = allowNewLogin(loginStrId);
		if (!allowNewLoginRtn.getStatus()) {
			return allowNewLoginRtn;
		}
		
		if (!isValidAgent()) {
//			ZkBiLogHelper.logAccess(this, ETYPE.LOGIN_FAIL, "invalid agent");
			return new ReturnMsg(false, "Login Fail.\nInvalid agent");
			
		}
		if (StringUtils.isBlank(loginStrId) || StringUtils.isBlank(p_password)){
			UniLog.log("login/password is empty");
//			ZkBiLogHelper.logAccess(this, ETYPE.LOGIN_FAIL, "login/password is empty loginid:" + (loginStrId == null ? "" : loginStrId));
			return new ReturnMsg(false, "Login Fail.\nInvalid Login or password.");
		}
		
		
		
		if (loginProceed(loginStrId, p_password)){
			if (!readUserProfileJson(true).getStatus()) {
				UniLog.log1("readUserProfile fail");
				return new ReturnMsg(false,"Internal Error.\nUnable to readUserProfile.");
			}
//			ZkBiLogHelper.logAccess(this, ETYPE.LOGIN_OK);
			passwordHash = calHash(p_password);
			return(ReturnMsg.defaultOk);
		}
		else {
//			ZkBiLogHelper.logAccess(this, ETYPE.LOGIN_FAIL, "invalid login/password loginid:"+ (loginStrId == null ? "" : loginStrId));
			return new ReturnMsg(false, "Login Fail.\nInvalid Login or Password.");
		}
	}
	public ReturnMsg readUserProfileJson(boolean p_requireNew){
		if (userProfile == null || p_requireNew) {
			userProfile = new BiJsonUserProfile();
		}
		ReturnMsg rtnMsg = userProfile.loadFromDB(this,"zkbi_userprofile_%s","user profile json",true);
		if (rtnMsg.getStatus()) {
			setLHLang(userProfile.lang, 1, false);
		}
		return rtnMsg;
	}
	public ReturnMsg saveUserProfile(){
		if (userProfile == null) {
			UniLog.log1("user profile is null");
			return new ReturnMsg(false,"Cannot save user profile");
		}
		return userProfile.save();
	}
	public boolean genLoginToken(HttpServletRequest p_request, HttpServletResponse p_response, String p_loginStr){
		if (p_request == null || p_response == null){
			return(false);
		}
		if (!allowLoginToken){
			return false;
		}
		if (StringUtils.isBlank(p_loginStr)) {
			return false;
		}
		clearLoginToken(p_request, p_response);

		SelectUtil su = null;
		try {
			su = new SelectUtil(); 
			su.init(getLoginTokenJdbcPool());
			Map<String,String> lkMap = buildLoginTokenString(getAgent());
			int rowCnt = su.executeUpdate("insert into logintoken (lt_loginid, lt_createtime, lt_logintoken) values (?, ?, ?)",
					new Wherecl()
			.appendArgument(p_loginStr)
			.appendArgument((long) (new Date()).getTime() / 1000)  //unixtime
			.appendArgument(MapUtil.getString(lkMap, "key","")//random char 50
					));
			setCookie(p_response, loginTokenCookieName, MapUtil.getString(lkMap, "full",""), 2592000); //30 day

			return(true);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		} 
		finally {
			if (su != null) {
				su.close();
			}
		}
		return(false);



	}
	
	/***
	 * delete login token record.
	 * invalidate auto login from server side
	 * @param p_loginStr
	 * @return
	 */
	public ReturnMsg deleteLoginTokenRecord(String p_loginStr) {
		if (!allowLoginToken){
			return new ReturnMsg(false,"not allow login token");
		}
		
		String loginId = loginGetId(p_loginStr);
		if (StringUtils.isBlank(loginId)){
			return new ReturnMsg(false,"Invalid loginid");
		}
		SelectUtil su = null;
		int delCnt = -1;
		try {
			su = new SelectUtil(); 
			su.init(getLoginTokenJdbcPool());
			delCnt = su.executeUpdate("delete from logintoken", 
				new Wherecl().orUniop("lt_loginid", "=", loginId)
				             .orUniop("lt_loginid", "=", String.format("%s@%s",loginId,getAgent()))
				             .stripOr());
			UniLog.log1("delete logintoken [%s] delCnt:%d",loginId, delCnt);
			return new ReturnMsg(true).setData(delCnt);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		} 
		finally {
			if (su != null) {
				su.close();
			}
		}
		return ReturnMsg.defaultFail;
	}
	public ReturnMsg deleteAllLoginTokenRecord() {
		UniLog.log1("called");
		if (!allowLoginToken){
			return new ReturnMsg(false,"not allow login token");
		}
		
		SelectUtil su = null;
		int delCnt = -1;
		try {
			su = new SelectUtil(); 
			su.init(getLoginTokenJdbcPool());
			delCnt = su.executeUpdate("delete from logintoken",null);
			UniLog.log1("delete logintoken [%s] delCnt:%d",loginId, delCnt);
			return new ReturnMsg(true).setData(delCnt);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		} 
		finally {
			if (su != null) {
				su.close();
			}
		}
		return ReturnMsg.defaultFail;
	}
	public boolean clearLoginToken(HttpServletRequest p_request, HttpServletResponse p_response){
		if (!allowLoginToken){
			return false;
		}
		String oldLTCookie = getCookie(p_request, loginTokenCookieName);
		if (oldLTCookie == null || oldLTCookie.trim().isEmpty()){
			return(true);
		}
		UniLog.log("clearLoginToken:" + oldLTCookie);
		setCookie(p_response, loginTokenCookieName, "", 0);
		
		Map<String,String> ltMap = extractLTCookie(oldLTCookie);
		String key = MapUtil.getString(ltMap,"key","");
		if (StringUtils.isBlank(key)){
			UniLog.log1("key is blank, skip remove logintoken record");
			return true;
		}

		SelectUtil su = null;
		try {
			su = new SelectUtil(); 
			su.init(getLoginTokenJdbcPool());
			su.executeUpdate("delete from logintoken", 
					new Wherecl().andUniop("lt_logintoken", "=", key)
					.stripAnd());	
			return(true);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		} 
		finally {
			if (su != null) {
				su.close();
			}
		}
		return(false);


	}

	
	protected String getLoginIdByVcode(String p_vcode) {
		return(p_vcode);
	}
	protected ReturnMsg afterLoginByToken(String p_login) throws Exception {
		//return(ReturnMsg.defaultOk);
		return(new ReturnMsg(true).setData(p_login));
	}
	final public ReturnMsg loginByToken(HttpServletRequest p_request, HttpServletResponse p_response) {
		return loginByToken(p_request, p_response, false);
	}
	final public ReturnMsg loginByToken(HttpServletRequest p_request, HttpServletResponse p_response, boolean p_checkTokenOnly){
		String remoteAddr = p_request.getRemoteAddr();
		if (!allowLoginToken){
			UniLog.log1("system not allow login token, ignore. addr:%s", remoteAddr);
			return ReturnMsg.defaultFail;
		}
		
		
		//check allow new login
		ReturnMsg allowNewLoginRtn = allowNewLogin(null);
		if (!allowNewLoginRtn.getStatus()) {
			return allowNewLoginRtn;
		}
				
		
		String ltCookie = getCookie(p_request, loginTokenCookieName);
		if (StringUtils.isBlank(ltCookie)) {
			UniLog.log1("no login token, ignore. addr:%s", remoteAddr);
			return ReturnMsg.defaultFail;
		}
		Map<String,String> ltMap = extractLTCookie(ltCookie);
		String ltKey = MapUtils.getString(ltMap, "key", "");
		if (StringUtils.isBlank(ltKey)) {
			UniLog.log1("login token key is blank, ignore. addr:%s", remoteAddr);
			return ReturnMsg.defaultFail;
		}
		String ltAgent = MapUtils.getString(ltMap, "agent", "");
		if (StringUtils.isBlank(ltAgent)) {
			UniLog.log1("login token agent is blank, ignore. addr:%s", ltAgent, getAgent(),remoteAddr);
			return ReturnMsg.defaultFail;
		}
		

		SelectUtil su = null;
		try {
			su = new SelectUtil(); 
			su.init(getLoginTokenJdbcPool());
			TableRec ltTr = su.getQueryResult("select lt_loginid, lt_createtime from logintoken",
					new Wherecl().andUniop("lt_logintoken", "=", ltKey));
			int recSize = ltTr.size();
			
			if (p_checkTokenOnly && recSize <= 0) {
				return ReturnMsg.defaultFail;
			}
			
			//login success
			if (recSize > 0) {
				//Int cast to long fail!! UniLog.log("lt_createtime:" + ((Long)ltTr.getField("lt_createtime", 0)).longValue());
				UniLog.log1("lt_loginid:" + ltTr.getFieldString("lt_loginid", 0).trim());
				String loginStr = ltTr.getFieldString("lt_loginid", 0).trim();
				String vcodeStrId = loginGetId(loginStr);
				String loginStrId = getLoginIdByVcode(vcodeStrId);
				if(loginStrId == null) return ReturnMsg.defaultFail;
				String loginStrAgent = loginGetAgent(loginStr);
				
				//validate agent
				String targetAgent = loginStrAgent;
				if (StringUtils.isBlank(targetAgent)) {
					targetAgent = getAgent();
				}
				if (!StringUtils.equals(ltAgent, targetAgent)){
					UniLog.log1("login token agent mismatched(%s,%s), ignore. addr:%s",ltAgent,targetAgent,remoteAddr);
					return ReturnMsg.defaultFail;
				}
				
				if (p_checkTokenOnly) {
					return new ReturnMsg(true).setData(loginStr);
				}
				
				
				if (StringUtils.isNotBlank(loginStrAgent)) {
					ReturnMsg rtn = changeIniAgentByLogin(loginStr);
					if (!rtn.getStatus()) {
						return rtn;
					}
				}
				
				UniLog.log1("loginByToken ok. login:%s trim:%s addr:%s",loginStr, loginStrId, remoteAddr);

				setLoginId(loginStrId);
				setVcode(vcodeStrId);
				clearLoginToken(p_request, p_response); //consume the login token
				//genLoginToken(p_request, p_response, loginStr);  //generate new token  //moved to JxZkLogin
//				ZkBiLogHelper.logAccess(this, ETYPE.TOKENLOGIN_OK);
				if (!readUserProfileJson(true).getStatus()) {
					UniLog.log1("readUserProfile fail");
					return ReturnMsg.defaultFail;
				}
				return(afterLoginByToken(loginStrId));
			}
			else{
				UniLog.log1("loginByToken fail, invalid token. token:%s addr:%s",ltCookie, remoteAddr);
//				ZkBiLogHelper.logAccess(this, ETYPE.TOKENLOGIN_FAIL, "invalid token:" + ltCookie);
				clearLoginToken(p_request, p_response);
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		} 
		finally {
			su.close();
		}
		return ReturnMsg.defaultFail;
	}

	public boolean logout() {
		cleanSessionData();
		sessionData = null;
		oneTimeData = null;
		multiTimeDataMap = null;
		accessRights = null;
		try {
			setVcode(null);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		setLoginId(null);
		//setUserProfileJson(null);
		userProfile = null;
		conditionPresetsHM = null;
		filingKeyWhiteListHS.clear();
		passTwoFactorFlag.set(false);
		devMode = false;
		passwordHash = ""; 
		try {
			PageLoader.purgeBySession(sessionKey);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
       	deleteActiveUser(sessionKey);
       	setHomePage(null);
       	iniAgent = "";
       	iniAgents = null;
       	
		//andrew220112 clear the sideMenuCache
		sideMenuImgCache.clear();  
		sideMenuImgWithInheritStyleCache.clear();  
		sideMenuDescCache.clear();  
		sideMenuViewCache.clear();
		sideMenuHelpCache.clear();
		
    	deviceFeatureHM.clear();
		return(true);
	}
	public CellCollection getAcTopMenus() 
	{	
		UniLog.log("webcore: default getAcTopMenus return null");
		return(null);
	}

	public void putSessionData(Object p_key,Object p_data)
	{
		if (sessionData == null) {
			if (fDebug) UniLog.log1("session data is null, ignore action");
			return;
		}
		synchronized(sessionData) {
			cleanExpiredSessionDataEx();
			removeSessionData(p_key);
			if (p_data == null) {
				sessionData.remove(p_key);
			}
			else {
				sessionData.put(p_key, p_data);
			}
		}
	}

	public Object getSessionData(Object p_key)
	{
		if (sessionData == null){
			if (fDebug) UniLog.log1("session data is null, ignore action");
			return null;
		}
		synchronized(sessionData) {
			cleanExpiredSessionDataEx();
			return(sessionData.get(p_key));
		}
	}

	public void removeSessionData(Object p_key)
	{
		if (sessionData == null) {
			if (fDebug) UniLog.log1("session data is null, ignore action");
			return;
		}
		synchronized(sessionData) {
			Object data = sessionData.get(p_key);
			if (data != null) {
				if (data instanceof SessionDataEx) {
					((SessionDataEx) data).cleanUp();
					UniLog.log("removeSessionDataEx key:" + p_key + ",data:" + data);
				}
				sessionData.remove(p_key);
			}
		}
	}
	/*
	public void putCacheData(Object p_key,Object p_data)
	{
		cacheData.put(p_key, p_data);
	}

	public Object getCacheData(Object p_key)
	{
		return(cacheData.get(p_key));
	}

	public void removeCacheData(Object p_key)
	{
		cacheData.remove(p_key);
	}
	*/

	public String getConnectionStr()
	{
		return(databaseString);
	}
	public RpcClient getRpcClient() {
		return getRpcClient(true);
	}
	public RpcClient getRpcClient(boolean p_debug)
	{
		if(rpcServerHost == null) return(null);
		if(rpcServerPort <= 0) return(null);
		RpcClient rpc = new RpcClient(rpcServerHost,rpcServerPort);
		rpc.open();
		rpc.setDebug(p_debug);
		if(rpcServerPrefix != null && rpcServerPrefix.startsWith(":")) {
			rpc.getConnection().setReturnFlag(false);
			StringTokenizer stok = new StringTokenizer(rpcServerPrefix,":");
			String s = null;
			if(stok.hasMoreTokens()) {
				s = stok.nextToken();
				rpc.callSegment("jdbc_putenv",
						new VectorUtil()
							.addElement("DBPATH="+s)
							.toVector()
							);
				rpc.callSegment("jdbc_putenv",
						new VectorUtil()
							.addElement("PERFUSER="+loginId)
							.toVector()
							);
				if(stok.hasMoreTokens()) {
					s = stok.nextToken();
					rpc.callSegment("perfwx_setchaindir",
						new VectorUtil()
							.addElement(s)
							.toVector()
							);
				}
			}
			rpc.getConnection().setReturnFlag(true);
		}
		return(rpc);	
		
		
	}
	/*
	public String getDbPath()
	{
		return(dbPath);
	}
	*/
	public String getSchemaXML(){
		return(schemaXML);
	}
	public String getSchemaJdbc(){
		return(schemaJdbc);
	}

	public static void log(String p_message) 
	{
		UniLog.log(p_message);
	}

	/*
	static public final String URLHEADER_FILING = "filling://";
	static public final String URLHEADER_FILE = "file://";
	static public final String URLHEADER_RESOURCE = "resource://";
	*/
	
	synchronized public File newErpFileToTmpfile(String p_filename,String p_prefix,String p_extention,File p_folder) 
	{
		FileOutputStream	fos = null;
		InputStream is=null;
		try {
			File tmpFile = File.createTempFile(p_prefix, p_extention, p_folder);
			fos = new FileOutputStream(tmpFile);
			is=null;
			if(p_filename.startsWith(URLHEADER_FILE)) {
				is = new FileInputStream(p_filename.substring(URLHEADER_FILE.length()));
			}
			if(p_filename.startsWith(URLHEADER_FILING)) {
				FilingUtil.getFile(getAgent(), null, p_filename.substring(URLHEADER_FILING.length()), fos);
				return(tmpFile);
			}
			if(p_filename.startsWith(URLHEADER_RESOURCE)) {
				is = new FileInputStream(getWebContentRealPath(p_filename.substring(URLHEADER_FILE.length()),true));
			}
			if(useRpcGetPutFile) 
				is = new RpcGetInputStream( rpcServerHost, rpcServerPort,p_filename);
			else
				is = new FileInputStream(p_filename);
			int readLen;
			int bufLen = 1024;
			byte[] buf = new byte[bufLen];
			while ((readLen = is.read(buf, 0, bufLen)) != -1)
        	     fos.write(buf, 0, readLen);
        	return tmpFile;
		} catch (Exception ex) {
			UniLog.log(ex);
			return(null);
		} 
		finally {
			CloseUtil.close(is);
			CloseUtil.close(fos);
		}
	}
	
	synchronized public byte[] newErpFileToByteArray(String p_filename) throws Exception
	{
		InputStream is=null;
		if(p_filename.startsWith(URLHEADER_FILE)) {
			is = new FileInputStream(p_filename.substring(URLHEADER_FILE.length()));
		} else if(p_filename.startsWith(URLHEADER_FILING)) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			FilingUtil.getFile(getAgent(), null, p_filename.substring(URLHEADER_FILING.length()), bos);
			byte[] bytes = bos.toByteArray();
			bos.close();
			return(bos.toByteArray());
		} else if(p_filename.startsWith(URLHEADER_RESOURCE)) {
//			is = new FileInputStream(getWebContentRealPath(p_filename.substring(URLHEADER_FILE.length()),true));
			is = openResourceAsStream(p_filename.substring(URLHEADER_RESOURCE.length()));
		} else if(useRpcGetPutFile) 
			is = new RpcGetInputStream( rpcServerHost, rpcServerPort,p_filename);
		else
			is = new FileInputStream(p_filename);
		if(is == null) return(null);
		int readLen;
		int bufLen = 1024;
		byte[] buf = new byte[bufLen];
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		while ((readLen = is.read(buf, 0, bufLen)) != -1)
             bos.write(buf, 0, readLen);
		is.close();
		bos.close();
        return bos.toByteArray();
	}
	
	synchronized public InputStream newErpFileInputStream(String p_filename) throws Exception
	{
		if(p_filename.startsWith(URLHEADER_FILE)) {
			return(new FileInputStream(p_filename.substring(URLHEADER_FILE.length())));
		}
		if(p_filename.startsWith(URLHEADER_FILING)) {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			String ss = p_filename.substring(URLHEADER_FILING.length());
			int idx = ss.indexOf("/");
			if(idx > 0) {
				String tabName = ss.substring(0,idx);
				ss = ss.substring(idx+1);
				FilingUtil.getFile(getAgent(), tabName, ss, bos);
			} else {
				FilingUtil.getFile(getAgent(), null, p_filename.substring(URLHEADER_FILING.length()), bos);
			}
			byte[] bytes = bos.toByteArray();
			bos.close();
			return(new ByteArrayInputStream(bytes));
		}
		if(p_filename.startsWith(URLHEADER_RESOURCE)) {
			return(new FileInputStream(getWebContentRealPath(p_filename.substring(URLHEADER_RESOURCE.length()),false)));
		}
		if(p_filename.startsWith(URLHEADER_MESSAGE)) {
			String ss = p_filename.substring(URLHEADER_MESSAGE.length());
			int idx = ss.indexOf("/");
			if(idx > 0) {
				String msgGroup = ss.substring(0,idx);
				ss = ss.substring(idx+1);
				idx = ss.indexOf("/");
				if(idx > 0) {
					int msgid = Integer.parseInt(ss.substring(0,idx));
					ss = ss.substring(idx+1);
//					RpcClient perfsvr = new RpcClient(rpcServerHost,rpcServerPort);
					RpcClient perfsvr = getRpcClient();
//					perfsvr.open();
					Value v = perfsvr.callSegment(
								"erpv4_getmessagepath",
								new VectorUtil()
									.addElement(msgGroup)
									.addElement(msgid)
									.addElement(ss)
									.toVector()
							);
					perfsvr.close();
					if(v != null && v instanceof Strval) {
						ss = v.toString();
						if(!StringUtils.isBlank(ss)) {
							return(newErpFileInputStream(ss));
						}
					}
				}
			}
			return(null);
		}
		if(useRpcGetPutFile) 
			return(new RpcGetInputStream( rpcServerHost, rpcServerPort,p_filename));
		else
			return(new FileInputStream(p_filename));
	}
	synchronized public OutputStream newErpFileOutputStream(String p_filename) throws Exception
	{
		if(p_filename.startsWith(URLHEADER_FILE)) {
			return(new FileOutputStream(p_filename.substring(URLHEADER_FILE.length())));
		}
		if(p_filename.startsWith(URLHEADER_FILING)) {
			return(new FilingByteArrayOutputStream(
						this,
						null,
						p_filename.substring(URLHEADER_FILING.length()),
						p_filename.substring(URLHEADER_FILING.length()),
						""
					));
		}
		if(p_filename.startsWith(URLHEADER_DOWNLOAD)) {
			/*return(new DownloadByteArrayOutputStream(
						this,
						null,
						p_filename.substring(URLHEADER_FILING.length()),
						p_filename.substring(URLHEADER_FILING.length()),
						""
					));*/
			throw new Exception("URLHEADER_DOWNLOAD not supported");
		}

		if(p_filename.startsWith(URLHEADER_RESOURCE)) {
//			String dirname = StringUtil.dirname(p_filename.substring(URLHEADER_RESOURCE.length()));
//		    String basename = StringUtil.basename(p_filename.substring(URLHEADER_RESOURCE.length()));
//			return(new FileOutputStream(ZkUtil.getWebContentRealPath( dirname,true) + basename));
			
//			return(new FileOutputStream(ZkUtil.getWebContentRealPath( p_filename.substring(URLHEADER_FILE.length()),false)));
			String contentPath = getWebContentRealPath("",true);
			return(new FileOutputStream(contentPath+p_filename.substring(URLHEADER_RESOURCE.length())));
		}
		if(useRpcGetPutFile) 
			return(new RpcPutOutputStream( rpcServerHost, rpcServerPort,p_filename));
		else
			return(new FileOutputStream(p_filename));
	}
	
	protected void cleanSessionObject(Object o) {
		
	}
	
	static public void logJVMStat()
	{
		Runtime runtime = Runtime.getRuntime();
		UniLog.log("JVM Status mem tot " + runtime.totalMemory() + " max " + runtime.maxMemory() + " free " + runtime.freeMemory());
	}

	public void cleanSessionData() {
		try {
			if(sessionData != null) {
				for(Iterator it = sessionData.values().iterator();it.hasNext();) {
					Object o = it.next();
					cleanSessionObject(o);
					if(o instanceof BiResult) {
						UniLog.log("Calling BiResult Close");
						((BiResult) o).close();
					}
				}
				cleanExpiredSessionDataEx();
			}
			//((ZkSessionHelper)this).cleanDeviceQue();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}

	public InputStream openResourceAsStream(String p_path) {
		return(svc.getResourceAsStream(p_path));
	}

	public String getWebPageTitle()
	{
		return(webPageTitle);
	}
	public String getWebPageCoName()
	{
		return(webPageCoName);
	}
	public String getDbLocation()
	{
		return("");
	}
	public String getWebPageName()
	{
		return(getLabel(webPageName));
	}
	public String getWebPageHome()
	{
		return(getLabel(webPageHome));
	}
	public String getWebPageLogin()
	{
		return(getLabel(webPageLogin));
	}
	public String getWebPageCoHeaderHtml()
	{
		String coName = getDbLocation();
		if(getWebPageCoName() != null && !getWebPageCoName().isEmpty()) {
			if(!coName.isEmpty()) coName += "<br>";
			coName += getWebPageCoName();
		}
		return (String.format("<div class=\"hbg dropdownlink\" style=\"display:table;vertical-align:middle;text-align:left;padding-right:2px;\">"
				+ "<div style=\"display:table-cell;text-align:left\">"
				+ "<span style=\"padding-left:0px;vertical-align:middle;text-align:left\">%s"
				//+ (getSideMenuAutoHide() ? "<i class=\"pin fa fa-rotate-90 fa-thumb-tack\" style=\"width:20px;height:20px;line-height:20px;\" aria-hidden=\"true\"></i>" : "")
				+ "</span></div>"
				//+ "<i class=\"fa fa-chevron-down\" aria-hidden=\"true\" style=\"display:table-cell;\"></i></div>"
				//+ (getSideMenuAutoHideDef() ? "<i class=\"pin fa fa-thumb-tack\" title=\"Pin/Unpin Menu (Alt+M)\" style=\"display:table-cell;vertical-align:top;width:10px;height:20px;line-height:20px;\" aria-hidden=\"true\"></i>" : "")
				+ (getSideMenuAutoHideDef() ? "<div style='display:table-cell;vertical-align:top;width:15px'><i class=\"pin fa fa-thumb-tack\" title=\"Pin/Unpin Menu (Alt+M)\" style=\"display:inline-block;width:10px;height:15px;line-height:15px;\" aria-hidden=\"true\"></i></div>" : "")
				+ "</div>"
				, coName));
	}

	
	public static Map<String,String> buildLoginTokenString(String p_agent){
		//generate a 50 char random string and agentid
		StringBuilder sb = new StringBuilder();
		String key = (UUID.randomUUID().toString() + UUID.randomUUID().toString()).replaceAll("-", "").substring(0,50);
		return MapUtil.of("key", key, "agent", p_agent, "full", key + "_" + (p_agent == null ? "" : p_agent));
	}
	public static Map<String,String> extractLTCookie(String p_loginTokenString) {
		if (StringUtils.isBlank(p_loginTokenString)) {
			return MapUtil.of("key","","agent","", "full","");
		}
		String splits[] = StringUtils.split(p_loginTokenString, "_", 2);
		if (splits == null || splits.length != 2) {
			return MapUtil.of("key",p_loginTokenString.trim(),"agent","", "full", p_loginTokenString.trim());
		}
		return MapUtil.of("key",splits[0].trim(),"agent",splits[1].trim(), "full", p_loginTokenString.trim());
	}
	public void validateLogin(HttpServletRequest p_request, HttpServletResponse p_response) throws Exception{
		if (!isLogin()) {
			String servletPath = buildServletPath(p_request, true);
			if (servletPath.length() > 0){
				String targetURLBase64 = (new org.apache.commons.codec.binary.Base64(true)).encodeBase64URLSafeString(servletPath.getBytes("UTF-8"));
				String agentTag = "";
				if (p_request.getParameter("agent") != null && !p_request.getParameter("agent").trim().equals("")){
					agentTag = "&agent=" + p_request.getParameter("agent").trim();
				}
				p_response.sendRedirect("login.html?targetURL=" + targetURLBase64 + agentTag);
			}
			else{
				p_response.sendRedirect("login.html");
			}	
		}

	}

	/***
	 * sitemap???
	 * @param p_href
	 * @param p_image
	 * @param p_description
	 * @param p_menuitemid
	 * @param p_isFolder
	 * @throws Exception
	 */
	private String geneintOneMenuItem(String p_href,String p_image,String p_description, int p_menuitemid, boolean p_isFolder, boolean p_isRoot) throws Exception
	{
		Map<String,List<String>> urlParamMap = StringUtil.getURLParamMap(p_href);
		String newMenuId = urlParamMap.get("menuid") != null ? urlParamMap.get("menuid").get(0) : null;
		String pageId = urlParamMap.get("page_id") != null ? urlParamMap.get("page_id").get(0) : null;
		if (StringUtils.isBlank(pageId) && StringUtils.isNotBlank(newMenuId))
			pageId = newMenuId; 

		String image = p_image;
		if (StringUtils.isNotBlank(pageId) && sideMenuImgWithInheritStyleCache.get(pageId) != null)
			image = sideMenuImgWithInheritStyleCache.get(pageId);
		
		//230306 remove image color class
		if (!allowMenuColor) {
			image = StringUtils.substringBefore(image, " ");
		}

		String label = getLabel(p_description);
		if (StringUtils.isNotBlank(pageId))
			label = TranslateUtil.getText(this, pageId.toUpperCase(), "MENU", label);

		StringBuilder sb = new StringBuilder();
		/*if(isMobileDevice())  {
			sb.append("<div class=\"dashboard\" style=\"padding-bottom:15px;\">");
			sb.append("<ul text-align=\"center\" class=\"menublk\" align=\"center\">");
		}*/
		String descStyle = "";
		if (p_isFolder){
			descStyle = "text-decoration: overline underline;text-underline-position: under;";
		}
		String divClass = p_isFolder ? "zkbi-sitemap-folder" : "zkbi-sitemap-item";
		if (p_isRoot) {
			divClass = "zkbi-sitemap-root";
		}
		
		if (allowSideMenuIndicator) {
			if (!p_href.startsWith("javascript"))
				p_href += (p_href.indexOf('?') > 0 ? '&' : "?") + "menuitem=" + p_menuitemid;
		}
		//if (allowMenuUrlWithAgent && !StringUtils.isBlank(iniAgent)){
		//	if (!p_href.startsWith("javascript"))
		//		p_href += (p_href.indexOf('?') > 0 ? '&' : "?") + "agent=" + iniAgent;
		//}
		//load bsip for webmenu page
		if (allowMenuColorSelector && p_href.startsWith("zkbiloader") && StringUtils.contains(p_href, "viewid=WebMenu")){
			p_href += (p_href.indexOf('?') > 0 ? '&' : "?") + "load=bsip";
		}
		//if (!p_href.startsWith("javascript") && StringUtils.isNotBlank(mode)){
		//	p_href += (p_href.indexOf('?') > 0 ? '&' : "?") + "mode=" + mode;
		//}
	
		//andrew221107 sitemap carry large flag
		//if (getLargeFlag() && !StringUtils.startsWith(p_href, "javascript")) {
		//	p_href += (p_href.indexOf('?') > 0 ? '&' : "?") + "large=Y";
		//}
		
		p_href = carryURLParam(p_href);
		
		if (image != null && image.trim().startsWith("fa-")){
			String iconClass = "fa " + image + " " + (p_isRoot ? "fa-3x" : "fa-5x");
			sb.append("<a href=\""+p_href+"\"><li style=\"vertical-align:top; padding:0 5px 0 0px;\"><div class=\""+divClass+"\" style=\"display:table;min-height:170px;min-width:180px;vertical-align:top;\"><div style=\"display:table-cell; padding-top:10px; padding-bottom:15px;vertical-align:bottom;\"><i class=\""+iconClass+"\" aria-hidden=\"true\" class=\"zkbi-sitemap\" style=\"\"></i><span style=\""+descStyle+"\">"+label+"</span></div></div></li></a>");	
		}
		else if (image != null && image.trim().startsWith("flaticon-")){
			sb.append("<a href=\""+p_href+"\"><li style=\"vertical-align:top; padding:0 5px 0 0px;\"><div class=\""+divClass+"\" style=\"display:table;min-height:170px;min-width:180px;vertical-align:top;\"><div style=\"display:table-cell; padding-top:10px; padding-bottom:15px;vertical-align:bottom;\"><i class=\""+image+"\" aria-hidden=\"true\" style=\"font-size:75px;\"></i><span style=\""+descStyle+"\">"+label+"</span></div></div></li></a>");	
		}
		else{
			sb.append("<a href=\""+p_href+"\"><li style=\"vertical-align:top; padding:0 5px 0 0px;\"><div class=\""+divClass+"\" style=\"display:table;min-height:170px;min-width:180px;vertical-align:top;\"><div style=\"display:table-cell; padding-top:10px; padding-bottom:15px;vertical-align:bottom;\"><img src=\""+image+"\" width=\"100px\" /><span style=\""+descStyle+"\">"+label+"</span></div></div></li></a>");	
		}

		/*if(isMobileDevice())  {
			sb.append("</ul> </div>");
		}*/
		return sb.toString();
	}

	public void geneintOneSideMenuItem(JspWriter p_out,String p_href,String p_image,String p_description, String p_menuitemid, String p_pageId, int p_level, int p_idx) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		geneintOneSideMenuItem(sb, p_href, p_image, p_description, p_menuitemid, p_pageId, p_level, p_idx);
		p_out.println(sb.toString());
	}
	public String getSideMenuBorderStyle(int p_level, int p_idx) {
		String color = "";
		if (p_level < 0) {
			color = "rgb(59,85,115)"; //same as background color
		}
		else if (p_level == 0) { //first level has gradient color
			//return "#6699ff";
			//range 59,85,115 109,157,251
			//range 67,97,137 109,157,251
			int rr = 67 + p_idx * (109-59)/6 ;
			int gg = 97 + p_idx * (157-85)/6;
			int bb = 137 + p_idx * (251-115)/6;
			if (rr > 109) rr = 109;
			if (gg > 157) gg = 157;
			if (bb > 251) bb = 251;
			color = String.format("rgb(%d,%d,%d)",rr,gg,bb);
		}
		else if (p_level == 1) { //brighter color
			color = "rgb(153,187,255)";
		}
		else if (p_level == 2) { //more brighter color
			color = "rgb(204,221,255)";
		}
		return String.format("border-left-width:3px;border-left-style:solid;border-left-color:%s;",color);
	}
	/***
	 * side menu
	 * @param sb
	 * @param p_href
	 * @param p_image
	 * @param p_description
	 * @param p_menuitemid
	 * @param p_pageId
	 * @param p_level
	 * @param p_idx
	 * @throws Exception
	 */
	public void geneintOneSideMenuItem(StringBuilder sb,String p_href,String p_image,String p_description, String p_menuitemid, String p_pageId, int p_level, int p_idx) throws Exception
	{
		//obtain menu border style
		String sideMenuBorderStyle = getSideMenuBorderStyle(p_level, p_idx);
		
		//230306 remove image color class
		if (!allowMenuColor) {
			p_image = StringUtils.substringBefore(p_image, " ");
		}
		
		if (p_href != null) {
			if (allowSideMenuIndicator) {
				if (!p_href.startsWith("javascript"))
					p_href += (p_href.indexOf('?') > 0 ? '&' : "?") + "menuitem=" + p_menuitemid;
			}
			//if (allowMenuUrlWithAgent && !StringUtils.isBlank(iniAgent)){
			//	if (!p_href.startsWith("javascript"))
			//		p_href += (p_href.indexOf('?') > 0 ? '&' : "?") + "agent=" + iniAgent;
			//}
			
			//andrew211005 carry large param
			//if (getLargeFlag() && !StringUtils.startsWith(p_href, "javascript")) {
			//	p_href += (p_href.indexOf('?') > 0 ? '&' : "?") + "large=Y";
			//}
			
			//carry theme param
			//if (!StringUtils.startsWith(p_href, "javascript")) {
			//	p_href += (p_href.indexOf('?') > 0 ? '&' : "?") + "theme=" + getTheme();
			//}
			
			//load bsip for webmenu page
			if (allowMenuColorSelector && p_href.startsWith("zkbiloader") && StringUtils.contains(p_href, "viewid=WebMenu")){
				p_href += (p_href.indexOf('?') > 0 ? '&' : "?") + "load=bsip";
			}
			//if (!p_href.startsWith("javascript") && StringUtils.isNotBlank(mode)){
			//	p_href += (p_href.indexOf('?') > 0 ? '&' : "?") + "mode=" + mode;
			//}
			p_href = carryURLParam(p_href);
			
			//open page in iframe window
			if (isOpenPageIframe() && p_href.startsWith("zkbiloader")) {
				p_href = String.format("javascript:zkbiOpenIframe('%s', '%s&sidemenu=N&fillscreen=full' ,800,480);",p_description.replaceAll("[^0-9a-zA-Z ]", ""), p_href);
			}
			//sb.append("<a href=\""+p_href+"\">");
			sb.append("<a style=\""+sideMenuBorderStyle+"\" href=\""+p_href+"\">");
			sb.append("<div id=\"menuitem"+p_menuitemid+"\" style=\"display:table;width:100%;vertical-align:middle;text-align:left;\">");
		} 
		else {
			//sb.append("<div id=\"menuitem"+p_menuitemid+"\" class=\"dropdownlink\" style=\"display:table;width:100%;vertical-align:middle;text-align:left;\">");
			sb.append("<div id=\"menuitem"+p_menuitemid+"\" class=\"dropdownlink\" style=\"display:table;width:100%;vertical-align:middle;text-align:left;"+sideMenuBorderStyle +"\">");
		}
		if (p_image != null && p_image.trim().startsWith("fa-"))
			sb.append("<i class=\"fa "+p_image+" fa1\" aria-hidden=\"true\" style=\"display:table-cell;\"></i>");
		else if (p_image != null && p_image.trim().startsWith("flaticon-"))
			sb.append("<i class=\""+p_image+" fa1\" aria-hidden=\"true\" style=\"display:table-cell;\"></i>");
		else
			sb.append("<div class=\"fa1\" style=\"display:table-cell;\"><img src=\""+p_image+"\" style=\"width:100%;height:100%;\"/></div>");
		
		String label = getLabel(p_description);
		if (StringUtils.isNotBlank(p_pageId)){
			label = TranslateUtil.getText(this, p_pageId.toUpperCase(), "MENU", label);
			
			//special formatting for User Profile
			if (StringUtils.equals(p_pageId, "SysUserProfile")){
				String userProfileLabel = TranslateUtil.getText(this, p_pageId.toUpperCase(), "MENU", null);
				if (StringUtils.isBlank(userProfileLabel)){
					label = p_description;
				}
				else{
					label = String.format("<div><b>%s</b></div><div>%s</div>",getLoginId().trim(), userProfileLabel);
				}
				
			}
			else{
				
			}
		}
		String pageIdTag = "";
		if (getAllowUpdateTranslate() && StringUtils.isNotBlank(p_pageId)){
			pageIdTag = String.format(" data-pageid=\"%s\"",p_pageId);
		}
		String descTag = "";
		if (StringUtils.isNotBlank(p_description)) {
			descTag = String.format(" data-desc=\"%s\"",StringUtil.convertWebStringTrim(p_description));
		}
		
		if (p_href != null) {
			sb.append(String.format("<span class=\"item-title\" style=\"display:table-cell;vertical-align:middle;padding-right:0px;\"%s%s>%s</span>",pageIdTag,descTag,label));
			sb.append("</div></a>");	
		}
		else {
			//is parent node
			sb.append(String.format("<span class=\"item-title\" style=\"display:table-cell;vertical-align:middle;\"%s%s>%s</span>",pageIdTag,descTag,label));
			sb.append("<i style=\"display:table-cell;\" class=\"fa fa-chevron-down\" aria-hidden=\"true\"></i></div>");
		}
	}

	/*public void generateMenu(JspWriter p_out) {
		try {
			p_out.println(generateMenu());
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}*/
	
	public NetworkNodeUtil getWebMenuTree() throws Exception {
		if(webmenutree == null) {
			SelectUtil su = new SelectUtil();
			su.init(getLoginTokenJdbcPool().getConnection());
			initWebMenuTree(su);
		}
//			generateSideMenuItems(null,null, null, null, null, 0, null);
		return(webmenutree);
	}
	void initWebMenuTree(SelectUtil su) throws Exception {
			if(webmenutree == null){
				webmenutree = new NetworkNodeUtil();
				TableRec ttr = su.getQueryResult("select * from webmenutree",null);
				for(int i = 0;i<ttr.size();i++) {
					ttr.setRecPointer(i);
					webmenutree.addNode(
							ttr.getField("webmt_user").toString().trim(),
							ttr.getField("webmt_user").toString().trim()
							);
					webmenutree.addNode(
							ttr.getField("webmt_parent").toString().trim(),
							ttr.getField("webmt_parent").toString().trim()
							);
					boolean isSingleLevel = false;
					if(ttr.existField("webmt_issingle")) {
						if("Y".equals(ttr.getFieldString("webmt_issingle"))) {
							isSingleLevel = true;
						}
					}
					webmenutree.addChild(
							ttr.getField("webmt_parent").toString().trim(),
							ttr.getField("webmt_user").toString().trim(),
							isSingleLevel
							);
				}

			} 
	}
	/***
	 * site map???
	 * @return
	 */
	public String generateMenu() {
		StringBuilder sb = new StringBuilder();
		SelectUtil su = null;
		try {
			su = new SelectUtil();
			su.init(getLoginTokenJdbcPool().getConnection());
			initWebMenuTree(su);
			TableRec tr;
			List<String> parentList = webmenutree.getParentList(getLoginId().trim(), false);
			String sParent = "";
			for(Iterator it = parentList.iterator();it.hasNext();){
				sParent += " , '" + it.next() + "' ";
			}
			//if(!isMobileDevice())  {
				sb.append("<div class=\"dashboard\">");
				sb.append("<ul style=\"text-align:center\" class=\"menublk\">");
			//}
			sb.append(geneintOneMenuItem(
//				"menu.html?menuid=menu_main.html",
				"menu.html?menuid="+rootMenu,
				"fa-sitemap", 
				"Sitemap", 
				0,
				true,
				true
			));	
			//if(!isMobileDevice())  {
				sb.append("</ul> </div>");
			//}
			tr = su.getQueryResult(
					"select * "
							+ " from webmenu,webmenuuser"
							+ " where webm_rg= '" + getURLParam("menuitem")+ "' and  webmu_mrg = webm_rg and webmu_active = 'Y' and  webmu_user in ("
							+ "'" + "anyuser" + "','" + getLoginId().trim() + "'" + sParent
							+ ") "
							,null);
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				/*
				if(!isMobileDevice())  {
					sb.append("<div class=\"menublk\" align=\"center\">");
				}
				sb.append("<span class=\"zkbi-sitemap-title\">"+tr.getFieldString("webm_desc")+"</span>");
				if(!isMobileDevice())  {
					sb.append("</div>");
				}
				*/
				Map<String,List<String>> urlParamMap = StringUtil.getURLParamMap(tr.getFieldString("webm_url"));
				String newMenuId = urlParamMap.get("menuid") != null ? urlParamMap.get("menuid").get(0) : null;
				String pageId = urlParamMap.get("page_id") != null ? urlParamMap.get("page_id").get(0) : null;
				if (StringUtils.isBlank(pageId) && StringUtils.isNotBlank(newMenuId))
					pageId = newMenuId; 
				String label = getLabel(tr.getFieldString("webm_desc"));
				if (StringUtils.isNotBlank(pageId))
					label = TranslateUtil.getText(this, pageId.toUpperCase(), "MENU", label);

				sb.append("<div class=\"menublk\" align=\"center\">");
				sb.append("<span class=\"zkbi-sitemap-title\">"+label+"</span>");
				sb.append("</div>");
			}
			/*//if(!isMobileDevice())  {
				sb.append("<div class=\"dashboard\">");
				sb.append("<ul text-align=\"center\" class=\"menublk\" align=\"center\">");
			//}*/
			List<String> folderList = new ArrayList<String>();
			List<String> itemList = new ArrayList<String>();
			tr = su.getQueryResult(
					"select distinct webm_seq,webm_url,webm_img,webm_desc,webm_rg"
							+ " from webmenu,webmenuuser"
							+ " where webm_menuid = '" + getURLParam("menuid")+ "' and  webmu_mrg = webm_rg and webmu_active = 'Y' and  webmu_user in ("
							+ "'" + "anyuser" + "','" + getLoginId().trim() + "'" + sParent
							+ ") order by 1,4,2,3,5"
							,null);
			for(int i = 0;i<tr.size();i++) {
				tr.setRecPointer(i);
				/*sb.append(geneintOneMenuItem(
					tr.getField("webm_url").toString().trim(),
					tr.getField("webm_img").toString().trim(),
					tr.getField("webm_desc").toString().trim(),
					Integer.parseInt(tr.getField("webm_rg").toString()),
					tr.getField("webm_url").toString().contains("menuid="),
					false
				));*/
				boolean isFolder = tr.getField("webm_url").toString().contains("menuid=");
				String s = geneintOneMenuItem(
					tr.getField("webm_url").toString().trim(),
					tr.getField("webm_img").toString().trim(),
					tr.getField("webm_desc").toString().trim(),
					Integer.parseInt(tr.getField("webm_rg").toString()),
					isFolder,
					false
				);
				if (isFolder)
					folderList.add(s);
				else
					itemList.add(s);
			}
			for (List<String> list : new List[] {itemList, folderList}) {
				if (!list.isEmpty()) {
					sb.append("<div class=\"dashboard\">");
					sb.append("<ul style=\"text-align:center\" class=\"menublk\">");
					for (String s : list)
						sb.append(s);
					sb.append("</ul> </div>");
				}
			}
			/*//if(!isMobileDevice())  {
				sb.append("</ul> </div>");
			//}*/
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		finally {
			if (su != null) su.close();
		}
		return sb.toString();
	}

	private static enum SIDE_MENU_ITEM {
		USER, APP_SETUP, SIGNOUT, DASHBOARD, SITEMAP, SYSTEM, CHANGE_LAYOUT, CHANGE_SCALE
	};
	public String getCurrentSideMenuItem(HttpServletRequest request) {
		if (!getAllowSideMenuIndicator())
			return null;
		if (request.getParameter("menuitem") != null)
			return request.getParameter("menuitem").trim();
		if (StringUtils.endsWithIgnoreCase(request.getServletPath(), "custom_menu.html"))
			return SIDE_MENU_ITEM.DASHBOARD.name();
		return null;
	}
	
	protected String getSiteMapUrl(String rootMenu) {
		return("menu.html?menuid="+rootMenu);
	}
	public synchronized void generateSideMenu(HttpServletRequest request,JspWriter p_out, String p_sidemenuid)   //andrew220117 try to fix side menu incomplete
	{
		if (!allowSideMenu)
			return;
		SelectUtil su = null;
		try {
			//String s = (String) getSessionData("sideMenu");
			String s = getSideMenuCache();
			if (s != null) {
				UniLog.log1("return sideMenu cache");
				if (p_out != null)
					p_out.println(s);
				return;
			}
			UniLog.log1("create sideMenu");
			StringBuilder sb = new StringBuilder();
			sideMenuImgCache.clear();
			sideMenuImgWithInheritStyleCache.clear();
			sideMenuDescCache.clear();
			sideMenuViewCache.clear();
			sideMenuHelpCache.clear();
			sb.append("<div id='" + p_sidemenuid + "' style='display:none'>");
			sb.append("<div id=\"sidemenu-menu-area\"><div>");
			sb.append("<ul style=\"margin:0;border:0\">");
			sb.append("<li>");
			/*String coName = getDbLocation();
			if(getWebPageCoName() != null && !getWebPageCoName().isEmpty()) {
				if(!coName.isEmpty()) coName += "<br>";
				coName += getWebPageCoName();
			}
			sb.append(String.format("<div class=\"hbg dropdownlink\" style=\"display:table;vertical-align:middle;text-align:left;padding-right:2px;\">"
					+ "<div style=\"display:table-cell;text-align:left\">"
					+ "<span style=\"padding-left:0px;vertical-align:middle;text-align:left\">%s"
					//+ (getSideMenuAutoHide() ? "<i class=\"pin fa fa-rotate-90 fa-thumb-tack\" style=\"width:20px;height:20px;line-height:20px;\" aria-hidden=\"true\"></i>" : "")
					+ "</span></div>"
					//+ "<i class=\"fa fa-chevron-down\" aria-hidden=\"true\" style=\"display:table-cell;\"></i></div>"
					//+ (getSideMenuAutoHideDef() ? "<i class=\"pin fa fa-thumb-tack\" title=\"Pin/Unpin Menu (Alt+M)\" style=\"display:table-cell;vertical-align:top;width:10px;height:20px;line-height:20px;\" aria-hidden=\"true\"></i>" : "")
					+ (getSideMenuAutoHideDef() ? "<div style='display:table-cell;vertical-align:top;width:15px'><i class=\"pin fa fa-thumb-tack\" title=\"Pin/Unpin Menu (Alt+M)\" style=\"display:inline-block;width:10px;height:15px;line-height:15px;\" aria-hidden=\"true\"></i></div>" : "")
					+ "</div>"
					, coName));*/
			sb.append(getWebPageCoHeaderHtml());
			sb.append("<ul class=\"submenuItems\">");
			if (allowUserProfile) {
				sb.append("<li>");
				geneintOneSideMenuItem(sb, "jxzkloader.html?zul=JxZkUserProfile.zul", "fa-user", String.format("<div><b>%s</b></div><div>%s</div>",getVcode().trim(), getLabel("Profile")), SIDE_MENU_ITEM.USER.name(),"SysUserProfile", 1, 0);	
				sb.append("</li>");
			}
			
			sb.append("<li>");
			if (isMobileDevice()){
				geneintOneSideMenuItem(sb, "javascript:mobileUtil.muChangeMode('pc');", "fa-desktop", "Switch To Desktop Layout", SIDE_MENU_ITEM.CHANGE_LAYOUT.name(),"SysChangeMode_01", 1, 0);	
			}
			else{
				geneintOneSideMenuItem(sb, "javascript:mobileUtil.muChangeMode('mobile');", "fa-mobile", "Switch To Mobile Layout", SIDE_MENU_ITEM.CHANGE_LAYOUT.name(),"SysChangeMode_02", 1, 0);	
			}
			sb.append("</li>");
			
			if (allowLargeSwitch) {
				sb.append("<li>");
				if (getLargeFlag()){
					geneintOneSideMenuItem(sb, "javascript:changeLargeScale('N');", "fa-font", "Switch To Normal Scale", SIDE_MENU_ITEM.CHANGE_SCALE.name(),"SysChangeScale_01", 1, 0);	
				}
				else{
					geneintOneSideMenuItem(sb, "javascript:changeLargeScale('Y');", "fa-font", "Switch To Large Scale", SIDE_MENU_ITEM.CHANGE_SCALE.name(),"SysChangeScale_02", 1, 0);	
				}
				sb.append("</li>");
			}
			
			sb.append("<li class='appsetup' style='display:none'>");
			geneintOneSideMenuItem(sb, "javascript:$.showAppSetup()", "fa-wrench", "App Setup", SIDE_MENU_ITEM.APP_SETUP.name(), null, 1, 0);	
			sb.append("</li>");
			sb.append("<li>");
			geneintOneSideMenuItem(sb, "javascript:zkLogout();", "fa-sign-out", "Logout", SIDE_MENU_ITEM.SIGNOUT.name(), "SysLogout_01", 1, 0);	
			sb.append("</li>");
			sb.append("</li>");
			sb.append("</ul>");
			sb.append("</li>");
			if (allowDashboard){
				sb.append("<li>");
				//geneintOneSideMenuItem(sb, "custom_menu.html", "home", "Home", SIDE_MENU_ITEM.DASHBOARD.name());	
				//geneintOneSideMenuItem(sb, "custom_menu.html", "images/icons/zkweb/095-home-25x25.png", "Home", SIDE_MENU_ITEM.DASHBOARD.name());	
				geneintOneSideMenuItem(sb, "custom_menu.html", "images/icons/zkweb/095-home-25x25.png", "Home", SIDE_MENU_ITEM.DASHBOARD.name(),"Dashboard_01", 0, 0);	
				sb.append("</li>");
			}
			if (allowSitemap){
				sb.append("<li>");
//				geneintOneSideMenuItem(sb, "menu.html?menuid=menu_main.html", "fa-sitemap", "Sitemap", SIDE_MENU_ITEM.SITEMAP.name(),"Sitemap_01", 0, 0);	
//				geneintOneSideMenuItem(sb, "menu.html?menuid=menu_main.html", "fa-sitemap black", "Sitemap", SIDE_MENU_ITEM.SITEMAP.name(),"Sitemap_01", 0, 0);	
//				geneintOneSideMenuItem(sb, "menu.html?menuid="+rootMenu, "fa-sitemap black", "Sitemap", SIDE_MENU_ITEM.SITEMAP.name(),"Sitemap_01", 0, 0);	
				geneintOneSideMenuItem(sb, getSiteMapUrl(rootMenu), "fa-sitemap black", "Sitemap", SIDE_MENU_ITEM.SITEMAP.name(),"Sitemap_01", 0, 0);	
				sb.append("</li>");
			}
			if (allowSystemTools && hasAccessRight("systools")) {
				sb.append("<li>");
				//geneintOneSideMenuItem(sb, "jxzkloader.html?zul=JxZkSystem.zul", "flaticon-bes-tools",getLabel("System Tools"), SIDE_MENU_ITEM.SYSTEM.name());	
				//geneintOneSideMenuItem(sb, "jxzkloader.html?zul=JxZkSystem.zul", "images/icons/zkweb/087-tools-25x25.png",getLabel("System Tools"), SIDE_MENU_ITEM.SYSTEM.name(),"SysTools_01", 0, 0);	
				geneintOneSideMenuItem(sb, "jxzkloader.html?zul=JxZkSystem.zul", "fa-gears black",getLabel("System Tools"), SIDE_MENU_ITEM.SYSTEM.name(),"SysTools_01", 0, 0);	
				sb.append("</li>");
			}
			sb.append("</ul>");
			sb.append("</div>");

			sb.append("<div>");
			sb.append("<ul>");
			su = new SelectUtil();
			su.init(getLoginTokenJdbcPool().getConnection());
			initWebMenuTree(su);
			List<String> parentList = webmenutree.getParentList(getLoginId().trim(), false);
			String sParent = "";
			for(Iterator it = parentList.iterator();it.hasNext();){
				sParent += " , '" + it.next() + "' ";
			}
//			generateSideMenuItems(request,sb, su, sParent, "menu_main.html", 0, new WebMenuNode());
			generateSideMenuItems(request,sb, su, sParent, rootMenu, 0, new WebMenuNode());
			sb.append("</ul>");
			sb.append("</div></div>");
			if (getallowSideMenuFilter()) {
				sb.append("<div id=\"sidemenu-filter-area\">"
						+ "<i class=\"search-button fa fa-search\" aria-hidden=\"true\"></i>"
						+ "<div class=\"input-area\">"
						+ "<input class=\"input-text\" type=\"text\" placeholder=\""+ getLabel("Menu Filter") +"\"></input>"
						+ "<span class=\"current-total\"></span>"
						+ "</div>"
						+ "<a class=\"up-button disabled\" href=\"javascript:;\"><i class=\"fa fa-angle-up\" aria-hidden=\"true\"></i></a>"
						+ "<a class=\"down-button disabled\" href=\"javascript:;\"><i class=\"fa fa-angle-down\" aria-hidden=\"true\"></i></a>"
						+ "</div>");
			}
			sb.append("</div>");
			sb.append("<div id=\"sidemenu-hover-area\"></div>");
			sb.append("<div id=\"sidemenu-button-container\" style=\"display:none\">");
			sb.append("<a id=\"sidemenu-button\" href=\"\">");
			sb.append("<i class=\"fa fa-bars\" aria-hidden=\"true\"></i>");
			sb.append("</a>");
			sb.append("<a title='Dashboard' href='" + getLandingPage() + "' class=\"coname\" style=\"display:none\">" + getWebPageCoName() + "</a>");
			sb.append("</div>");
			if (p_out != null)
				p_out.println(sb.toString());
			//putSessionData("sideMenu", sb.toString());
			updateSideMenuCache(sb.toString());
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		finally {
			if (su != null) su.close();
		}
	}
	public void generateTopMenu(JspWriter p_out){
		StringBuilder sb = new StringBuilder();
		/*
		sb.append("<div style=\"line-height:35px;float:left;\">");
		sb.append(String.format("<span style=\"font-size:10px;padding-left:50px;\">HAHA;</span>"));
		sb.append("</div>");
		*/
		sb.append("<div style=\"line-height:35px;float:right;\">");
		sb.append(String.format("<span style=\"font-size:10px;\">Hello %s&nbsp;&nbsp;</span>", getVcode()));
		
		
		
		//sb.append(String.format("<a href=\"custom_menu.html?menuitem=DASHBOARD&agent=%s\">", iniAgent));
		sb.append(String.format("<a href=\"%s\">", getLandingPage()));
		//sb.append("<i aria-hidden=\"true\" class=\"fa fa-home\" title=\"Home\"></i></a>&nbsp;&nbsp;");
		sb.append(String.format("<i aria-hidden=\"true\" class=\"fa fa-home\" title=\"%s\"></i></a>&nbsp;&nbsp;", getLabel("Home",true)));
		
		if (allowUserProfile) {
			//sb.append(String.format("<a href=\"jxzkloader.html?zul=JxZkUserProfile.zul&agent=%s\">", iniAgent));
			sb.append(String.format("<a href=\"%s \">", carryURLParam("jxzkloader.html?zul=JxZkUserProfile.zul")));  //andrew221207 carryurlparam for userprofile
			//sb.append("<i aria-hidden=\"true\" class=\"fa fa-user\" title=\"User Profile\"></i></a>&nbsp;&nbsp;");
			sb.append(String.format("<i aria-hidden=\"true\" class=\"fa fa-user\" title=\"%s\"></i></a>&nbsp;&nbsp;", getLabel("User Profile",true)));
		}
		
		if (allowReportProblem) {
			sb.append(String.format("<a href=\"javascript:zkReportProblem();\">"));
			sb.append(String.format("<i aria-hidden=\"true\" style=\"font-size:15px; position:relative; bottom:2px;\" class=\"fa fa-comment-o\" title=\"%s\"></i></a>&nbsp;&nbsp;", getLabel("Report Problem",true)));
		}
		
		if (isMobileDevice()){
			sb.append(String.format("<a href=\"javascript:mobileUtil.muChangeMode('pc');\">"));
			//sb.append("<i aria-hidden=\"true\" style=\"font-size:15px;\" class=\"fa fa-desktop\" title=\"Switch To PC Layout\"></i></a>&nbsp;&nbsp;");
			sb.append(String.format("<i aria-hidden=\"true\" style=\"font-size:15px;\" class=\"fa fa-desktop\" title=\"%s\"></i></a>&nbsp;&nbsp;", getLabel("Desktop Layout", true)));
		}
		else{
			sb.append(String.format("<a href=\"javascript:mobileUtil.muChangeMode('mobile');\">"));
			//sb.append("<i aria-hidden=\"true\" style=\"font-size:20px;\" class=\"fa fa-mobile\" title=\"Switch To Mobile Layout\"></i></a>&nbsp;&nbsp;");
			sb.append(String.format("<i aria-hidden=\"true\" style=\"font-size:20px;\" class=\"fa fa-mobile\" title=\"%s\"></i></a>&nbsp;&nbsp;", getLabel("Mobile Layout",true)));
			
		}
		if (allowLargeSwitch) {
			if (getLargeFlag()) {
				sb.append(String.format("<a href=\"javascript:changeLargeScale('N');\">"));
				//sb.append("<i aria-hidden=\"true\" style=\"font-size:15px;\" class=\"fa fa-font\" title=\"Switch To Normal Scale\"></i></a>&nbsp;&nbsp;");
				sb.append(String.format("<i aria-hidden=\"true\" style=\"font-size:15px;\" class=\"fa fa-font\" title=\"%s\"></i></a>&nbsp;&nbsp;", getLabel("Switch To Normal Scale", true)));
			}
			else {
				sb.append(String.format("<a href=\"javascript:changeLargeScale('Y');\">"));
				//sb.append("<i aria-hidden=\"true\" style=\"font-size:15px;\" class=\"fa fa-font\" title=\"Switch To Large Scale\"></i></a>&nbsp;&nbsp;");
				sb.append(String.format("<i aria-hidden=\"true\" style=\"font-size:15px;\" class=\"fa fa-font\" title=\"%s\"></i></a>&nbsp;&nbsp;", getLabel("Switch To Large Scale", true)));
			}
		}
		if (getAllowDualScreen()){
			sb.append(String.format("<a href=\"dualscreen.html?agent=%s\">", iniAgent));
			sb.append("<i aria-hidden=\"true\" style=\"font-size:15px;\" class=\"fa fa-window-restore\" title=\"Dual Screen\"></i></a>&nbsp;&nbsp;");
		}
		
		//sb.append(String.format("<a href=\"logout.html\">"));
		sb.append(String.format("<a href=\"javascript:zkLogout();\">"));
		//sb.append("<i class=\"fa fa-sign-out\" title=\"Logout\"></i></a>&nbsp;");
		sb.append(String.format("<i class=\"fa fa-sign-out\" title=\"%s\"></i></a>&nbsp;", getLabel("Logout",true)));
		sb.append("</div>");
		
//		sb.append("    <div id=\"unity-container\" class=\"unity-desktop\">");
//		sb.append("      <canvas id=\"unity-canvas\" width=100 height=100 tabindex=\"-1\"></canvas>");
//		sb.append("      <div id=\"unity-loading-bar\">");
//		sb.append("        <div id=\"unity-logo\"></div>");
//		sb.append("        <div id=\"unity-progress-bar-empty\">");
//		sb.append("          <div id=\"unity-progress-bar-full\"></div>");
//		sb.append("        </div>");
//		sb.append("      </div>");
//		sb.append("      <div id=\"unity-warning\"> </div>");
//		sb.append("    </div>");
		
//		sb.append("    <div id=\"unity-container\" style=\"margin:auto;width:200px\">");
//		sb.append("      <canvas id=\"unity-canvas\" width=100 height=100 tabindex=\"-1\"></canvas>");
//		sb.append("      <div id=\"unity-loading-bar\">");
//		sb.append("        <div id=\"unity-logo\"></div>");
//		sb.append("        <div id=\"unity-progress-bar-empty\">");
//		sb.append("          <div id=\"unity-progress-bar-full\"></div>");
//		sb.append("        </div>");
//		sb.append("      </div>");
//		sb.append("      <div id=\"unity-warning\"> </div>");
//		sb.append("    </div>");		
		
		
//		sb.append("    <div id=\"unity-container\" style=\"margin:auto;width:200px;display:block\">");
//		sb.append("    <div id=\"unity-container\" style=width:100%;display:block\">");
//		sb.append("    HAHA222");	
//		sb.append("    </div>");	
		
		try{
			p_out.println(sb.toString());
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/*
	private void generateSideMenu(HttpServletRequest request,JspWriter p_out, SelectUtil su, String sParent, String p_menuid, int p_level) throws Exception {
		StringBuilder sb = new StringBuilder();
		generateSideMenuReal(request,sb, su, sParent, p_menuid, p_level, new WebMenuNode());
		p_out.println(sb.toString());
	}
	*/
	public String getSideMenuImg(String p_pageId) {
		if (StringUtils.isBlank(p_pageId)) {
			return null;
		}
		return sideMenuImgCache.get(p_pageId);
	}
	public String getSideMenuDesc(String p_pageId) {
		if (StringUtils.isBlank(p_pageId)) {
			return null;
		}
		return sideMenuDescCache.get(p_pageId);
	}
	void generateSideMenuItems(HttpServletRequest request,StringBuilder sb, SelectUtil su, String sParent, String p_menuid, int p_level, WebMenuNode p_parentNode) throws Exception {
		TableRec tr = su.getQueryResult(
				"select distinct webm_seq,webm_url,webm_img,webm_desc,webm_rg"
						+ " from webmenu,webmenuuser"
						+ " where webm_menuid = '"+ p_menuid +"' and  webmu_mrg = webm_rg and webmu_active = 'Y' and  webmu_user in ("
						+ "'" + "anyuser" + "','" + getLoginId().trim() + "'" + sParent
						+ ") order by 1,4,2,3,5"
						,null);
		//Pattern pattern = Pattern.compile("menuid=([\\w\\.]+)($|&)");
		for(int i = 0;i<tr.size();i++) {
			tr.setRecPointer(i);
			WebMenuNode webMenuNode = p_parentNode.add(tr.getField("webm_img").toString());
			
			Map<String,List<String>> urlParamMap = StringUtil.getURLParamMap(tr.getField("webm_url").toString().trim());
			sb.append("<li>");
			//String newMenuId = null;
			String newMenuId = urlParamMap.get("menuid") != null ? urlParamMap.get("menuid").get(0) : null;
			
			String pageId = urlParamMap.get("page_id") != null ? urlParamMap.get("page_id").get(0) : null;
			/*if (StringUtils.isNoneBlank(pageId, tr.getFieldString("webm_img"))){
				sideMenuImgCache.put(pageId, tr.getFieldString("webm_img"));
			}
			if (StringUtils.isNoneBlank(pageId, tr.getFieldString("webm_desc"))){
				sideMenuDescCache.put(pageId, tr.getFieldString("webm_desc"));
			}*/
			
			//construct viewid cache
			String viewId = urlParamMap.get("viewid") != null ? urlParamMap.get("viewid").get(0) : null;
			if (StringUtils.isNotBlank(viewId)) {
				sideMenuViewCache.put(viewId, viewId);
			}
			
			//construct helpid cache
			String helpId = urlParamMap.get("helpid") != null ? urlParamMap.get("helpid").get(0) : null;
			if (StringUtils.isNotBlank(helpId)) {
				sideMenuHelpCache.put(helpId, helpId);
			}
			
			//parent node withid pageid, use menuid as pageId
			if (StringUtils.isBlank(pageId) && StringUtils.isNotBlank(newMenuId)){
				pageId = newMenuId; 
			}

			//construct img cache
			if (StringUtils.isNoneBlank(pageId, tr.getFieldString("webm_img"))){
				sideMenuImgCache.put(pageId, tr.getFieldString("webm_img"));
				sideMenuImgWithInheritStyleCache.put(pageId, getAllowInheritMenuStyle() ? webMenuNode.getImg() : tr.getFieldString("webm_img"));
			}
			if (StringUtils.isNoneBlank(pageId, tr.getFieldString("webm_desc"))){
				sideMenuDescCache.put(pageId, tr.getFieldString("webm_desc"));
			}

			String url = tr.getField("webm_url").toString().trim();
			{
				if(offsiteRedirect != null && url.startsWith("zkbiloader")) {
					String xuri = request.getRequestURI();
					String xurl = request.getRequestURL().toString();
					int idx = xurl.indexOf(xuri);
					if(idx > 0) {
						String urlRoot = xurl.substring(0,idx)+request.getContextPath();
//						urlRoot = "urlroot="+URLEncoder.encode("http://localhost:8080/pmsdemo","UTF-8");
						urlRoot = "urlroot="+URLEncoder.encode(urlRoot,"UTF-8");
//						url = "http://127.0.0.1:8080/pmsdemo/servlet/redirect/"+url+"&"+urlRoot;
						url = offsiteRedirect+url+"&"+urlRoot;
					}
				}
			}
			/*
			Matcher m = pattern.matcher(url);
			if (m.find())
				newMenuId = m.group(1);
			*/
			if (newMenuId != null || !url.contains("widget=Y")) {
				geneintOneSideMenuItem(sb,
					newMenuId == null ? url : null,
							//tr.getField("webm_img").toString().trim(),
							getAllowInheritMenuStyle() ? webMenuNode.getImg() : tr.getField("webm_img").toString().trim(),
							tr.getField("webm_desc").toString().trim(),
							tr.getField("webm_rg").toString(),
							pageId, 
							p_level,
							i
					);	
			}
			if (newMenuId != null) {
				sb.append("<ul class=\"submenuItems\">");
				generateSideMenuItems(request,sb, su, sParent, newMenuId, p_level+1, webMenuNode);
				sb.append("</ul>");
			}
			sb.append("</li>");
		}
	}

	private int curCustomMenuItemOriSeq;
	public Map<String, Object> generateCustomMenuList(HttpServletRequest request) {
		SelectUtil su = null;
		Map<String, Object> resultMap = new HashMap<String, Object>();
		resultMap.put("sortMode", 0);
		resultMap.put("items", "[]");
		try {
			Map<Integer, JSONObject> jsonMap = new LinkedHashMap<Integer, JSONObject>();
			Map<Integer, Boolean> inTableMap = new HashMap<Integer, Boolean>();
			ByteArrayOutputStream outStream = null;
			try {
				outStream = new ByteArrayOutputStream();
				if (FilingUtil.getFile(getAgent(), null, String.format(CUSTOMMENU_FILING_STORE_KEY, getLoginId()), outStream) != null) {
					String str = outStream.toString();
					if (str != null) {
						JSONArray jsonArray;
						try {
							JSONObject jsonObj = new JSONObject(str);
							resultMap.put("sortMode", jsonObj.getInt("sortMode"));
							jsonArray = jsonObj.getJSONArray("items");
						}
						catch (Exception ex) {
							jsonArray = new JSONArray(str);
						}
						for (int i = 0; i < jsonArray.length(); i++) {
							JSONObject json = jsonArray.getJSONObject(i);
							int rg = json.getInt("rg");
							jsonMap.put(rg, json);
							inTableMap.put(rg, false);
						}
					}
				}
			} catch (Exception ex) {
				UniLog.log("read filing data fail " + ex.toString());
			} finally {
				if (outStream != null)
					outStream.close();
			}

			su = new SelectUtil();
			//			su.init(
			//				WebCoreUtil.getJdbcPoolByConnectionString(getDbLabel(),2,getConnectionStr(),getDbLogin(),getDbLabel()).getConnection()
			//			);
			su.init(getLoginTokenJdbcPool().getConnection());
			initWebMenuTree(su);
			List<String> parentList = webmenutree.getParentList(getLoginId().trim(), false);
			String sParent = "";
			for(Iterator it = parentList.iterator();it.hasNext();){
				sParent += " , '" + it.next() + "' ";
			}
			curCustomMenuItemOriSeq = 0;
//			generateCustomMenuList(request,su, sParent, "menu_main.html", jsonMap, inTableMap, new WebMenuNode());
			generateCustomMenuList(request,su, sParent, rootMenu, jsonMap, inTableMap, new WebMenuNode());
			if (allowCustomMenuTest) {
				//createCustomMenuEmbdedItemJson("zkf/dashboard/Sample.zul", null, 1000000001, "ZZZZZZZZZA", "", jsonMap, inTableMap);
//				createCustomMenuEmbdedItemJson("SampleWidget1.zul", 1000000001, "ZZZZZZZZZA", "", jsonMap, inTableMap);
//				createCustomMenuEmbdedItemJson("SampleWidget2.zul", 1000000002, "ZZZZZZZZZB", "", jsonMap, inTableMap);
			}
			JSONArray jsonArray = new JSONArray();
			for (Map.Entry<Integer, JSONObject> entry : jsonMap.entrySet()) {
				int rg = entry.getKey();
				JSONObject json = entry.getValue();
				if (inTableMap.get(rg)) {
					if (json.getBoolean("removed")) {
						json.put("x", -1)
						.put("y", -1);
					}
					jsonArray.put(json);
				}
			}
			//UniLog.log("generateCustomMenuList: " + jsonArray.toString(4));
			resultMap.put("items", jsonArray.toString(4));
			return resultMap;
		} catch (Exception ex) {
			UniLog.log(ex);
		} finally {
			if (su != null)
				su.close();
		}
		return resultMap;
	}
	private void createCustomMenuEmbdedItemJson(String zul, Map<String, Object> zulParams, String icon, int rg, String title, String link, Map<Integer, JSONObject> jsonMap, Map<Integer, Boolean> inTableMap) throws Exception {
		curCustomMenuItemOriSeq++;
		JSONObject json;
		if ((json = jsonMap.get(rg)) == null) {
			json = new JSONObject();
			json.put("x", -1)
			.put("y", -1)
			.put("removed", true);
		}
		json.put("rg", rg)
		.put("link", link)
		.put("title", title)
		.put("icon", icon)
		.put("zul", zul)
		.put("oriSeq", curCustomMenuItemOriSeq);
		if (json.optBoolean("removed")) {
			json.put("width", 4)
			.put("height", 4)
			.put("minWidth", 4)
			.put("maxWidth", 4)
			.put("minHeight", 4)
			.put("maxHeight", 4);
		}
		else {
			json.put("width", 0)
			.put("height", 0)
			.put("minWidth", 0)
			.put("maxWidth", 0)
			.put("minHeight", 0)
			.put("maxHeight", 0);
		}
		if (zulParams != null)
			json.put("zulParams", zulParams);
		json.remove("embdedNum");
		jsonMap.put(rg, json);
		inTableMap.put(rg, true);
	}
	/***
	 * custom menu
	 * @param su
	 * @param sParent
	 * @param p_menuid
	 * @param jsonMap
	 * @param inTableMap
	 * @throws Exception
	 */
	private void generateCustomMenuList(HttpServletRequest request,SelectUtil su, String sParent, String p_menuid, 
			final Map<Integer, JSONObject> jsonMap, final Map<Integer, Boolean> inTableMap, WebMenuNode p_parentNode) throws Exception {
		TableRec tr = su.getQueryResult(
				"select distinct webm_seq,webm_url,webm_img,webm_desc,webm_rg"
						+ " from webmenu,webmenuuser"
						+ " where webm_menuid = '"+ p_menuid +"' and  webmu_mrg = webm_rg and webmu_active = 'Y' and  webmu_user in ("
						+ "'" + "anyuser" + "','" + getLoginId().trim() + "'" + sParent
						+ ") order by 1,4,2,3,5"
						,null);
		Pattern pattern = Pattern.compile("menuid=([\\w\\.]+)($|&)");
		for(int i = 0;i<tr.size();i++) {
			tr.setRecPointer(i);
			Map<String,List<String>> urlParamMap = StringUtil.getURLParamMap(tr.getField("webm_url").toString().trim());
			String newMenuId = urlParamMap.get("menuid") != null ? urlParamMap.get("menuid").get(0) : null;
			String pageId = urlParamMap.get("page_id") != null ? urlParamMap.get("page_id").get(0) : null;
			
			//parent node withid pageid, use menuid as pageId
			if (StringUtils.isBlank(pageId) && StringUtils.isNotBlank(newMenuId)){
				pageId = newMenuId; 
			}
			WebMenuNode webMenuNode = p_parentNode.add(tr.getField("webm_img").toString().trim());
			if (newMenuId != null)
				generateCustomMenuList(request,su, sParent, newMenuId, jsonMap, inTableMap, webMenuNode);
			else {
				int rg = Integer.parseInt(tr.getField("webm_rg").toString());
				String link =tr.getField("webm_url").toString().trim(); 
				if(offsiteRedirect != null && link.startsWith("zkbiloader")) {
					String xuri = request.getRequestURI();
					String xurl = request.getRequestURL().toString();
					int idx = xurl.indexOf(xuri);
					if(idx > 0) {
						String urlRoot = xurl.substring(0,idx)+request.getContextPath();
//						urlRoot = "urlroot="+URLEncoder.encode("http://localhost:8080/pmsdemo","UTF-8");
						urlRoot = "urlroot="+URLEncoder.encode(urlRoot,"UTF-8");
//						url = "http://127.0.0.1:8080/pmsdemo/servlet/redirect/"+url+"&"+urlRoot;
						link = offsiteRedirect+link+"&"+urlRoot;
					}
				}
				//String icon = tr.getField("webm_img").toString().trim();
				String icon = getAllowInheritMenuStyle() ? webMenuNode.getImg() : tr.getField("webm_img").toString().trim();
				
				//230306 remove image color class
				if (!allowMenuColor) {
					icon = StringUtils.substringBefore(icon, " ");
				}
				
				String title = getLabel(tr.getField("webm_desc").toString().trim());
				
				//translate desc
				if (StringUtils.isNotBlank(pageId)){
					title = TranslateUtil.getText(this, pageId.toUpperCase(), "MENU", title);
				}
				
				if (allowSideMenuIndicator) {
					if (!link.startsWith("javascript"))
						link += (link.indexOf('?') > 0 ? "&" : "?") + "menuitem=" + rg;
				}
				//if (allowMenuUrlWithAgent && !StringUtils.isBlank(iniAgent)){
				//	if (!link.startsWith("javascript"))
				//		link += (link.indexOf('?') > 0 ? '&' : "?") + "agent=" + iniAgent;
				//}
				
				//andrew211005 carry large param
				//if (getLargeFlag() && !StringUtils.startsWith(link, "javascript")) {
				//	link += (link.indexOf('?') > 0 ? '&' : "?") + "large=Y";
				//}
				
				//carry theme param
				//if (!StringUtils.startsWith(link, "javascript")) {
				//	link += (link.indexOf('?') > 0 ? '&' : "?") + "theme=" + getTheme();
				//}
				
				//load bsip for webmenu page
				if (allowMenuColorSelector && link.startsWith("zkbiloader") && StringUtils.contains(link, "viewid=WebMenu")){
					link += (link.indexOf('?') > 0 ? '&' : "?") + "load=bsip";
				}
				//if (!link.startsWith("javascript") && StringUtils.isNotBlank(mode)){
				//	link += (link.indexOf('?') > 0 ? '&' : "?") + "mode=" + mode;
				//}
				link = carryURLParam(link);
				
				//open page in iframe window
				if (isOpenPageIframe() && link.startsWith("zkbiloader")) {
					link = String.format("javascript:zkbiOpenIframe('%s', '%s&sidemenu=N&fillscreen=full' ,800,480);",title.replaceAll("[^a-zA-Z0-9 ]", ""),link);
				}

				if (StringUtils.contains(link, "widget=Y")) {
					if (allowDashboardWidget && link.startsWith("zkbiloader.html") && link.contains("zul=")) {
						Map<String, List<String>> map = StringUtil.getURLParamMap(link);
						Map<String, Object> paramMap = new HashMap<String, Object>();
						for (Map.Entry<String, List<String>> entry : map.entrySet()) {
							String[] ss = entry.getValue().toArray(new String[0]);
							if (ss.length == 1)
								paramMap.put(entry.getKey(), ss[0]);
							else
								paramMap.put(entry.getKey(), ss);
						}
						String zul = (String)paramMap.get("zul");
						paramMap.remove("zul");
						UniLog.log1("paramMap:%s, icon:%s", paramMap, icon);
						createCustomMenuEmbdedItemJson(zul, paramMap, icon, rg, title, "", jsonMap, inTableMap);
					}
				}
				else {
					curCustomMenuItemOriSeq++;
					JSONObject json;
					if ((json = jsonMap.get(rg)) == null) {
						json = new JSONObject();
						json.put("x", -1)
						.put("y", -1)
						.put("removed", false);
					}
					json.put("rg", rg)
					.put("link", link)
					.put("icon", icon)
					.put("title", title)
					.put("width", 4)
					.put("height", 4)
					.put("minWidth", 4)
					.put("maxWidth", 4)
					.put("minHeight", 4)
					.put("maxHeight", 4)
					.put("oriSeq", curCustomMenuItemOriSeq);
					json.remove("embdedNum");
					jsonMap.put(rg, json);
					inTableMap.put(rg, true);
				}
			}
		}
	}

	public boolean isMobileDevice()
	{
		if (StringUtils.isBlank(mode)){
			return(uaMobileFlag);
		}
		return(StringUtils.equals(mode, "mobile"));
	}
	/***
	 * shortcut for isMobileDevice()
	 * @return
	 */
	public boolean isMobile() {
		return isMobileDevice();
	}
	
	public boolean isTouchPanel() {
		return(isMobileDevice());
	}

	public Properties loadProperty(String p_propfile)
	{
		Properties prop = new Properties();
		try {
			prop.load(svc.getResourceAsStream("/WEB-INF/"+p_propfile));
			return(prop);
		} catch (Exception ex){
			UniLog.log(ex);
			return(null);
		}
	}
	
	public String getVersionId(){
		return(versionId + BUILD_VERSION_ID);
	}

	public boolean getAllowSearch(){
		return(allowSearch);
	}

	public boolean getAllowNewAdvSearch(){
		return(allowNewAdvSearch);
	}
	public boolean getAllowNewAdvSearchG2(){
		return(allowNewAdvSearchG2);
	}

	public boolean getAllowGeneralReport(){
		return(allowGeneralReport);
	}

	public boolean getAllowTouchDragFloatingActionButton(){
		return(allowTouchDragFloatingActionButton);
	}

	public boolean getAllowMenuColorSelector(){
		return(allowMenuColorSelector);
	}

	public boolean getAllowUpdateCustomBiView(){
		if (!isAdminUser()) return false;
		return(allowUpdateCustomBiView);
	}

	public boolean getAllowImportPreset(){
		if (!isAdminUser()) return false;
		return(allowImportPreset);
	}

	public boolean getAllowExportPreset(){
		if (!isAdminUser()) return false;
		return(allowExportPreset);
	}

	public boolean getAllowImportG2(){
		//if (!isAdminUser()) return false;
		return(allowImportG2);
	}
	public boolean getAllowImportG1(){
		return(allowImportG1);
	}


	public boolean isAdminUser(){
		if(adminList.contains("all")) return(true);
		return(adminList.contains(getLoginId()));
	}
	
	/***
	 * obtain loginid from parameter
	 * @param p_loginId
	 * @return
	 */
	public boolean isAdminUser(String p_loginId) {
		if(adminList.contains("all")) return(true);
		return(adminList.contains(p_loginId));
	}

	public boolean getAllowCustomMenuTest(){
		return allowCustomMenuTest;
	}

	public String getMaskPhotoFolder() {
		return maskPhotoFolder;
	}
	
	public boolean getAllowSideMenuIndicator() {
		return allowSideMenuIndicator;
	}

	public boolean getallowSideMenuFilter() {
		//return !isMobile() && allowSideMenuFilter;
		return allowSideMenuFilter;
	}
	
	public boolean getAllowReportProblem() {
		return allowReportProblem;
	}

	public boolean getAllowPrintButton() {
		return allowPrintButton;
	}
	
	public boolean getAllowDataAnalysis() {
		return allowDataAnalysis;
	}
	
	public boolean getAllowFocusIndex() {
		return allowFocusIndex;
	}

//	public String getDefaultTimeZone(){
//		return defaultTimeZone;
//	}

	public int getRpcServerPort() {
		return(rpcServerPort);
	}
	public String getRpcServerPrefix() {
		return(rpcServerPrefix);
	}
	public String getRpcServerHost() {
		return(rpcServerHost);
	}

	public HashSet<String> getMatchedAccessRights(String p_regexp)
	{
		HashSet<String> result = new HashSet<String>();
		try {
			RE regexp = new RE(p_regexp);
			for(String rights : accessRights) {
				if(rights != null) {
					if(regexp.match(rights)) result.add(rights);
				}
			}
		} catch (RESyntaxException rex) {
			UniLog.log(rex);
			return(null);
		}
		return(result);
	}
	public HashSet<String> getAccessRights()
	{
		return(accessRights);
	}
	public HashSet<String> getAccessUsers()
	{
		return(accessUsers);
	}
	public String getLabel(String p_tag){
		return getLabel(p_tag, false);
	}
	public String getLabel(String p_tag, boolean p_webStringFlag){
		if (p_webStringFlag) {
			//return(StringEscapeUtils.escapeHtml4(LabelHelper.getText(p_tag, LabelHelper.TYPE_LB, getLHLang())));
			return(StringUtil.convertWebString(LabelHelper.getText(p_tag, LabelHelper.TYPE_LB, getLHLang())));
		}
		else {
			return(LabelHelper.getText(p_tag, LabelHelper.TYPE_LB, getLHLang()));
		}
	}
	public String getLabel(BiColumn col){
		StringBuilder sb = new StringBuilder();
		sb.append(LabelHelper.getText(col.getEngName(), LabelHelper.TYPE_LB, getLHLang()));
		return(sb.toString());
	}
    public String getBtLabel(String p_tag){
	    return(LabelHelper.getText(p_tag, LabelHelper.TYPE_BTN, getLHLang()));
    }
    public String getBtLabel(BiColumn col) {
	    return(LabelHelper.getText(col.getEngName(), LabelHelper.TYPE_BTN, getLHLang()));
    }
    public String getMsgLabel(String p_tag){
	    return(LabelHelper.getText(p_tag, LabelHelper.TYPE_MSG, getLHLang()));
    }
    public String getTtLabel(String p_tag){
	    return(LabelHelper.getText(p_tag, LabelHelper.TYPE_TP, getLHLang()));
    }
    
	public int getAutoRefresh(){
		return(autoRefresh);
	}
	public boolean getAllowSyslog(){
		return(allowSyslog);
	}
	public String getAgent(){
		if (iniAgent != null && !iniAgent.trim().equals("")){
			return(iniAgent);
		}
		return("");
	}
	/***
	 * get all available agent
	 * @return
	 */
	public List<String> getAgents(){
		if (iniAgents == null) {
			return new ArrayList();
		}
		return iniAgents;
	}
	public String getAgentAndDbLabel(){
		if (iniAgent != null && !iniAgent.trim().equals("")){
			return(iniAgent+"."+databaseLabel);
		}
		return("");
	}
	public int getBadIpMaxFailCnt(){
		return(banIpMaxFailCnt);
	}
	public long getBanIpDur(){
		return(banIpDur);
	}
	public boolean getAllowChangePassword(){
		return(allowChangePassword);
	}
	public void setDevMode(boolean p_flag){
		if (allowDevMode){
			devMode = p_flag;
		}
		else{
			UniLog.logm(this,"not allow dev mode");
			
		}
	}
	public boolean getDevMode(){
		if (allowDevMode && devMode) {
			UniLog.log1("WARNING!!! getDevMode return true.");
			return true;
		}
		return false;
	}
	public boolean getAllowLoginToken(){
		return(allowLoginToken);
	}
	public boolean getAllowShutdown(){
		return(allowShutdown);
	}
	public boolean getAllowTour(){
		return(allowTour);
	}
	
	/***
	 * 
	 * @param p_lhLang 
	 * @param p_pri priority 0-default, 1-userProfile 2-override
	 * @param p_updateUserProfile
	 */
	public void setLHLang(String p_lhLang, int p_pri, boolean p_updateUserProfile){
		setLHLang(p_lhLang, p_pri, p_updateUserProfile,false);
	}
	private void setLHLang(String p_lhLang, int p_pri, boolean p_updateUserProfile,boolean noLangChange){
		String tmpLang = p_lhLang == null ? "" : p_lhLang.trim().toUpperCase();
		if (p_pri == 0 &&  StringUtils.isBlank(tmpLang)) {
			UniLog.log1("pri 0 cannot set as blank");
			return;
		}
		if (p_pri < 0 || p_pri >= lhLangList.length) {
			UniLog.log1("invalid pri %d",p_pri);
			return;
		}
		if (p_updateUserProfile && userProfile != null) {
			userProfile.lang = tmpLang;  //required to call save
		}
		if (!StringUtils.equals(lhLangList[p_pri], tmpLang)) {
			lhLangList[p_pri] = tmpLang;
			clearSideMenuCache();
		}
//		if(!noLangChange) {
		if(sessionData != null){
			BiSchema schema = (BiSchema) sessionData.get("biSchema");
			if(schema != null) {
				BiView.translateHeaders(schema, getLHLang());
			}
		}
//		}
	}
	/***
	 * return the highest priority lhLang
	 * @return
	 */
	public String getLHLang(){
		for (int i=lhLangList.length-1; i>=0; i--) {
			if (StringUtils.isNotBlank(lhLangList[i])) {
				return lhLangList[i];
			}
		}
		return lhLangList[0];
	}
	/*
	public void setLHLangByUserProfile){
		if (userProfileJson != null){
			try{
				if (userProfileJson.get("lang") != null && !userProfileJson.get("lang").toString().trim().equals("")){
					setLHLang(userProfileJson.get("lang").toString());
				}
				
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			
		}
		else{
			setLHLang("TCHN");
		}
	}
	*/
	/*
	public void setWidthByContent(boolean p_widthByContent){
		widthByContent = p_widthByContent;
	}
	*/
	/*
	public boolean getWidthByContent(){
		return(widthByContent);
	}
	*/
	
	/*
	//andrew200623: seems useless
	public void addToFileStore(String p_key, byte[] p_data) throws Exception{
		if (StringUtils.isBlank(p_key) || ArrayUtils.isEmpty(p_data)){
			throw new Exception ("key/value cannot be blank");
		}
		fileStoreHM.put(p_key, p_data);
	}
	public void addToFileStore(String p_key, File p_file) throws Exception{
		if (StringUtils.isBlank(p_key) || p_file == null){
			throw new Exception ("key/value cannot be blank");
		}
		fileStoreHM.put(p_key, FileUtils.readFileToByteArray(p_file));
	}
	
	public byte[] getFromFileStore(String p_key, boolean p_deleteFlag){
		if (p_deleteFlag){
			return(fileStoreHM.remove(p_key));
		}
		else{
			return(fileStoreHM.get(p_key));
		}
	}
	*/
	
	
	public String putOneTimeData(Object o) {
		return putOneTimeData(o, false);  //andrew211015: should change default value from false to true for handle page refresh
	}
	
	/***
	 * 
	 * @param o
	 * @param p_multi false - data can access one time only. No size limit.
	 *                true - data keep in LRU map and can access multiple time. designed for for keep small amount of data and object size should be small.
	 *                       
	 * @return
	 */
	public synchronized String putOneTimeData(Object o, boolean p_multi) {
		if (p_multi) {
			//multiple time data
			//String key = "MTD" + oneTimeSeq.getAndIncrement();
			String key = "MTD" + new Date().getTime() + (UUID.randomUUID().toString()).replaceAll("-", "").toUpperCase();
			multiTimeDataMap.put(key, o);
			return(key);
		}
		else {
			//original one time data
			String key = "OTD" + oneTimeSeq.getAndIncrement();
			oneTimeData.put(key, o);
			return(key);
		}
	}
	
	public boolean hasOneTimeData(String key) {
		if (StringUtils.startsWith(key, "OTD")) {
			Object o = oneTimeData.get(key);
			return(o != null);
		}
		if (StringUtils.startsWith(key, "MTD")) {
			return (multiTimeDataMap.get(key) != null);
		}
		return false;
	}
	public Object getOneTimeData(String key) {
		if (StringUtils.startsWith(key,"OTD")){
			Object o = oneTimeData.get(key);
			if(o != null) oneTimeData.remove(key);
			return(o);
		}
		if (StringUtils.startsWith(key, "MTD")) {
			return multiTimeDataMap.get(key);
		}
		return null;
	}
	

	public class SessionDataEx {
		private final static long ttl = 300000;
		private long createTime, expireTime;
		private Object key;
		private Object data;
		private SessionDataExCleanUpCallback sessionDataExCleanUpCallback;
		public SessionDataEx(Object key, Object data, long expireTime, SessionDataExCleanUpCallback callback) {
			this.data = data;
			this.createTime = System.currentTimeMillis();
			this.expireTime = expireTime;
			sessionDataExCleanUpCallback = callback;
		}
		public SessionDataEx(String key, Object data, SessionDataExCleanUpCallback callback) {
			this(key, data, ttl, callback);
		}
		public SessionDataEx(String key, Object data) {
			this(key, data, ttl, null);
		}
		public Object getData() {
			return data;
		}
		public boolean isExpired() {
			return System.currentTimeMillis() - createTime >= expireTime;
		}
		public void cleanUp() {
			if (sessionDataExCleanUpCallback != null)
				sessionDataExCleanUpCallback.cleanUp(key, data);
		}
	}
	public static class SessionDataExCleanUpCallback {
		public void cleanUp(Object key, Object data) {
			if (data != null) {
				if (data instanceof InputStream) {
					try {
						((InputStream) data).close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
	}
	public void putSessionDataEx(Object key, Object data, long expireTime, SessionDataExCleanUpCallback callback) {
		putSessionData(key, new SessionDataEx(key, data, expireTime, callback));
		UniLog.log("putSessionDataEx key:" + key + ",data:" + data + ",expireTime:" + expireTime + ",callback:" + callback);
	}
	public void putSessionDataEx(String key, Object data, long expireTime) {
		putSessionDataEx(key, data, expireTime, null);
	}
	public void putSessionDataEx(String key, Object data, SessionDataExCleanUpCallback callback) {
		putSessionData(key, new SessionDataEx(key, data, callback));
		UniLog.log("putSessionDataEx key:" + key + ",data:" + data + ",callback:" + callback);
	}
	public void putSessionDataEx(String key, Object data) {
		putSessionDataEx(key, data, null);
	}
	private void cleanExpiredSessionDataEx() {
		synchronized(sessionData) {
			Iterator<Map.Entry<Object, Object>> it = sessionData.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Object, Object> entry = it.next();
				Object data = entry.getValue();
				if (data instanceof SessionDataEx) {
					SessionDataEx sd = (SessionDataEx) data;
					if (sd.isExpired()) {
						sd.cleanUp();
						it.remove();
						UniLog.log("cleanSessionDataEx key:" + sd.key + ",data:" + sd.data);
					}
				}
			}
		}
	}
	public boolean getFrozenActionCol(){
		return(frozenActionCol);
	}
	public boolean getFrozenFirstCol(){
		return(frozenFirstCol);
	}
	
	/***
	 * 
	 * @param p_sh - the record key
	 * @param p_url - for update lastAccessUrl (optional)
	 */
	public static void updateActiveUser(SessionHelper p_sh, HttpSession p_httpsession, String p_url) {
		try {
			//obtain sessionKey 
			String sessionKey = null;
			if (p_sh != null)
				sessionKey = p_sh.sessionKey;
			else if (p_httpsession != null)
				sessionKey = p_httpsession.getId();
			if (StringUtils.isBlank(sessionKey)) {
				UniLog.log1("session key is blank");
				return;
			}

			synchronized(activeUserListHM){
				ActiveUserInfo userInfo = activeUserListHM.get(sessionKey);

				//add new active user
				if (userInfo == null && p_sh != null){
					Date curDate = new Date();
					userInfo = new ActiveUserInfo(p_sh.getLoginId(), p_sh.getAgent(), p_sh.getRemoteAddr(), curDate, curDate, sessionKey);
					activeUserListHM.put(sessionKey, userInfo);
				}
				if (userInfo == null) {
					UniLog.log1("userinfo is null");
					return;
				}

				//update active user
				synchronized(userInfo) {
					//with sh, can update all data
					if (p_sh != null) {
						userInfo.loginId = p_sh.loginId;
						userInfo.agent = p_sh.iniAgent;
						userInfo.ip = p_sh.remoteAddr;
						userInfo.activeDesktopStr = p_sh.activeDesktopStr;
					}

					//without sh, can update access time / url only
					userInfo.lastAccessTime = new Date();
					if (StringUtils.isNotBlank(p_url)) { //if url is blank, preserve last url
						userInfo.lastAccessUrl = p_url; 
					}
				}
			}
			
		}
		catch(Exception ex) {
			UniLog.log1("Error:" + ex.getMessage());
		}
		
	}
	public static void deleteActiveUser(String p_key){
		UniLog.logm(null,"key:%s", p_key);
		synchronized(activeUserListHM){
			if (activeUserListHM.get(p_key) != null){
				activeUserListHM.remove(p_key);
			}
		}
	}
	/***
	 * users count including nologin session
	 */
	public static int getUserListCount(){
		return(activeUserListHM.size());
	}
	
	/***
	 * users count not include nologin session
	 * @return
	 */
	public static int getActiveUserListCount() {
		int count = 0;
		for (ActiveUserInfo userInfo: activeUserListHM.values()){
			if (!StringUtils.isBlank(userInfo.loginId)){
				count++;
			}
		}
		return(count);
	}
	public static List<ActiveUserInfo> getActiveUserList(){
		ArrayList userList = new ArrayList();
		for (ActiveUserInfo userInfo: activeUserListHM.values()){
			if (!StringUtils.isBlank(userInfo.loginId)){
				userList.add(userInfo);
			}
		}
		return(userList);
	}
	/***
	 * it will return a clone of ActiveUserInfo
	 * @return
	 */
	public ActiveUserInfo getActiveUserInfo(){
		synchronized(activeUserListHM){
			ActiveUserInfo aui = activeUserListHM.get(sessionKey);
			try{
				return(aui == null ? null : aui.clone());
			}
			catch(Exception ex){
				ex.printStackTrace();
				return(null);
			}
		}
	}
	public String getLandingPage(){
		return getLandingPage(null);
	}
	public String getLandingPage(String p_targetURL){
		UniLog.log1("targetURL=%s",p_targetURL);
		if (StringUtils.isBlank(p_targetURL)) {
			StringBuilder urlSb = new StringBuilder();
			if (getAllowSitemapAsHomePage())
//				urlSb.append("menu.html?menuid=menu_main.html&menuitem=SITEMAP");
				urlSb.append("menu.html?menuid="+rootMenu+"&menuitem=SITEMAP");
			else
				urlSb.append("custom_menu.html?menuitem=DASHBOARD&page_id=Dashboard_01");
			/*
			if (allowMenuUrlWithAgent && !StringUtils.isBlank(iniAgent) && isLogin()){
				urlSb.append(String.format("&agent=%s", iniAgent));
			}
			if (StringUtils.isNotBlank(mode)){
				urlSb.append(String.format("&mode=%s", mode));
			}
			if (largeFlag || defLargeFlag){
				urlSb.append(String.format("&large=Y"));
			}
			
			//append theme param
			urlSb.append(String.format("&theme=%s", getTheme()));
			
			return(urlSb.toString());
			*/
			return(carryURLParam(urlSb.toString()));
		}
		else {
			if (isLogin() && StringUtils.isNotBlank(iniAgent)) {
				return p_targetURL
						.replaceAll("\\?agent=[a-zA-Z0-9-]*", "?agent=" + iniAgent)
						.replaceAll("&agent=[a-zA-Z0-9-]*", "&agent=" + iniAgent);
			}
			else {
				return p_targetURL;
			}
			
		}
	}
	public String getRequestSidrAction() {
		/***
		 * default: for pc view, initial based on available width, then based on previous selection
		 * auto: for mobile view, hide/display menu based on available width
		 * close: always close. added for autohide mode
		 * 
		 */
		if (isMobileDevice()){ //andrew 190903: fix switch pc/mobile layout has inconsistent layout
			return("auto");
		}
		if (getSideMenuAutoHide() && !getSideMenuAutoHideDefaultPin()){
			return("close");
		}
		if (getSessionData("requestSidrAction") == null)
			setRequestSidrAction(isMobileDevice() ? "auto" : "default");
		return (String)getSessionData("requestSidrAction");
	}
	public void setRequestSidrAction(String p_action) {
		putSessionData("requestSidrAction", p_action);
	}
	public boolean getAllowSmtp(){
		return(allowSmtp);
	}
	/*
	//obsoleted, smtp call moved to filing
	public String getSmtpHost(){
		return(smtpHost);
	}
	public int getSmtpPort(){
		return(smtpPort);
	}
	public String getSmtpLoginId(){
		return(smtpLoginId);
	}
	public String getSmtpLoginPassword(){
		return(smtpLoginPassword);
	}
	public boolean getSmtpSSLOnConnect(){
		return(smtpSSLOnConnect);
	}
	*/
	public boolean getAllowVisitView(){
		return(allowVisitView);
	}
	public boolean getAllowDualScreen(){
		return(allowDualScreen);
	}
	public boolean getAllowUpdateTranslate(){
//		if (!isAdminUser() && (accessRights==null || !accessRights.contains("updtransl"))) return false;
//		if (!isAdminUser() && (accessRights==null || !accessRights.contains("updtransl"))) return false;
		if (!hasAccessRight("updtransl")) return(false);
		return(allowUpdateTranslate);
	}
	public boolean getAllowTranslate(){
		return(allowTranslate);
	}
	public boolean getAllowPatternTranslate(){
		return(allowTranslate && allowPatternTranslate);
	}
	public boolean getAllowTranslateB2G() {
		return(allowTranslateB2G);
	}
	public boolean getAllowTranslateG2B() {
		return(allowTranslateG2B);
	}
	public boolean getNewButtonPanelLayout(){
		return(newButtonPanelLayout);
	}
	public int getQuickFilterMaxChar() {
		return quickFilterMaxChar;
	}
	
	/***
	 * @param p_viewId
	 * @return
	 */
	public synchronized ConditionPresets getConditionPresets(String p_viewId){ 
		if (conditionPresetsHM == null){ 
			UniLog.logm(this,"conditionPresetsHM is null");
			return(null);
		}
		
		synchronized(conditionPresetsHM) {
			ConditionPresets conditionPresets = conditionPresetsHM.get(p_viewId);
			if (conditionPresets == null){ 
				//TODO:1 construct it on demand.
				conditionPresets = new ConditionPresets();
				conditionPresets.parseConditionPresets(null, p_viewId, this);
				if (fDebug) UniLog.log1("parseConditionPresets %s", p_viewId);
				
				//TODO:2 probably need to update conditionPresets to make it thread safe.
				conditionPresetsHM.put(p_viewId, conditionPresets);
			}
			return (conditionPresets);
		}
	}
	
	public byte[] getAESKey(){
		try{
			if (aesKeyStr == null || (aesKeyStr.length() != 16 && aesKeyStr.length() != 32)){
				UniLog.logm(this,"invalid aeskey, return null");
				return(null);
			}
			return(aesKeyStr.getBytes("UTF-8"));
		}
		catch(Exception ex){
			ex.printStackTrace();
			return(null);
		}
		
	}
	public String getPrinterHost(){
		return(printerHost);
	}
	public int getPrinterPort(){
		return(printerPort);
	}
	/***
	 * handy call for obtain BiSchema
	 * @return null if error occur
	 */
	public synchronized BiSchema getBiSchema(){
		try{
			return(BiSchema.loadSchema(this));
		}
		catch(Exception ex){
			UniLog.log1("error:" + ex.getMessage());
			//ex.printStackTrace();
			return(null);
		}
	}
	/***
	 * handy method for obtain biview
	 * @param p_name
	 * @return
	 */
	public BiView getBiView(String p_name){
		BiSchema schema = getBiSchema();
		if (schema == null){
			return null;
		}
		BiView view = schema.getViewByName(p_name);
		if (view == null){
			UniLog.log1("view %s does not exist", p_name);
			return null;
		}
		return(view);
	}
	
	/***
	 * handy function to create new BiResult
	 * @param p_name
	 * @return
	 */
	public BiResult newBiResult(String p_name, boolean p_clearCurrentRec) {
		BiView view = getBiView(p_name);
		if (view == null) {
			return null;
		}
		BiResult br = view.newBiResult(getLoginId(), null, null, this);
		if (br != null && p_clearCurrentRec) {
			br.clearCurrentRec();
		}
		return br;
	}
	public BiResult newBiResult(String p_name) {
		return newBiResult(p_name, false);
	}
	
	public int getMobileMaxCol(){
		return(mobileMaxCol);
	}
	public boolean getAllowDelayClick(){
		return(allowDelayClick);
	}
	public int getDelayClickMS(){
		return(delayClickMS);
	}
	
	/***
	 * obtain default property map
	 * do not modify the content of the map 
	 * @return
	 */
	private static synchronized Map<String,Object> getDefPropMap(){
		if (defPropMap != null){
			if (fDebug) UniLog.log1("return defPropMap cache");
			return defPropMap;
		}
//		Properties prop = new Properties();
		Properties prop = null;
		String iniAgent = null;  //default iniAgent
		String iniFile = null;
		boolean iniAgentAllowUpdate = false;
		boolean iniAgentAllowChildIni = false;
		boolean allowLogButton = false;
		try {
			//load property
			
			String custPropFile= System.getProperty("erpsetup.properties");
			if (StringUtils.isNotBlank(custPropFile) && (new File(custPropFile)).isFile()){
				//-Derpsetup.properties param override default CLASSPATH/erpsetup.properties. This option for dev env.
				UniLog.log1("load custom prop %s", custPropFile);
//				prop.load(new FileReader(custPropFile));
				prop = IniHelper.loadProperty(custPropFile,null);
			}
			else{
				//This option for regular env.
				UniLog.log1("load default prop %s", defPropFileName);
//				prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream(defPropFileName));
				prop = IniHelper.loadProperty(null,defPropFileName);
			}
			iniAgent = prop.getProperty("iniAgent");
			iniAgentAllowUpdate =  StringUtils.equals(prop.getProperty("iniAgentAllowUpdate"),"Y");
			iniFile = prop.getProperty("iniFile");
			iniAgentAllowChildIni =  StringUtils.equals(prop.getProperty("iniAgentAllowChildIni"),"Y");
			if (StringUtils.isBlank(iniFile) || !(new File(iniFile)).isFile()){
				UniLog.log1("iniFile %s does not exist, skip override", iniFile);
				iniFile = null;
			}
			
			//sepcial static global flag for all agent
			logButtonFlag.set(StringUtils.equals(prop.getProperty("logButtonFlag"),"Y"));
			logButtonFullNameFlag.set(StringUtils.equals(prop.getProperty("logButtonFullNameFlag",logButtonFullNameFlag.get() ? "Y" : "N"), "Y"));
			
			defPropMap = MapUtil.of("iniAgent",iniAgent,"iniFile",iniFile,"iniAgentAllowUpdate",iniAgentAllowUpdate,"iniAgentAllowChildIni", iniAgentAllowChildIni);
			UniLog.log1("defProMap created");
			return defPropMap;
		}
		catch(Exception ex){
			ex.printStackTrace();
			return null;
		}
	}
	
	public boolean getDefWidthByContent(){
		//return(defWidthByContent);

		//andrew221102 add url param for override defWidthByContent
    	String defWidthByContentURLParam = getURLParam("defwidthbycontent");
		return(defWidthByContentURLParam == null ? defWidthByContent : StringUtils.equalsAnyIgnoreCase(defWidthByContentURLParam, "Y","YES","TRUE"));
	}
	public boolean getAllowSchemaCache(){
		return(allowSchemaCache);
	}
	public boolean getAllowActionButtonMenu(){
		return(allowActionButtonMenu);
	}
	public boolean getHideOrgActionButton(){
		return(hideOrgActionButton);
	}
	
	/***
	 * clear side menu cache
	 */
	public void clearSideMenuCache(){
		removeSessionData("sideMenu");
	}
	/***
	 * update side menu cache
	 * @param p_data side menu html
	 */
	public void updateSideMenuCache(String p_data){
		putSessionData("sideMenu", p_data);
	}
	/***
	 * get side menu cache
	 * @return side menu html
	 */
	public String getSideMenuCache(){
		if (devMode) {
			UniLog.log1("dev mode not cache side menu");
			return null;
		}
		return (String) getSessionData("sideMenu");
	}
	public int getScreenHeight() {
		return screenHeight;
	}
	public void setScreenHeight(int screenHeight) {
		this.screenHeight = screenHeight;
	}
	public int getScreenWidth() {
		return screenWidth;
	}
	public void setScreenWidth(int screenWidth) {
		this.screenWidth = screenWidth;
	}
	public int getDesktopHeight() {
		return desktopHeight;
	}
	public void setDesktopHeight(int desktopHeight) {
		this.desktopHeight = desktopHeight;
	}
	public int getDesktopWidth() {
		return desktopWidth;
	}
	public void setDesktopWidth(int desktopWidth) {
		this.desktopWidth = desktopWidth;
	}
	public boolean isLandScape() {
		return isLandScape;
	}
	public void setLandScape(boolean isLandScape) {
		this.isLandScape = isLandScape;
	}
	public boolean getSideMenuAutoHide(){
		if (isMobileDevice()) return false;  //if remove this line, need to handle mobile hover event
		return(sideMenuAutoHide);
	}
	public boolean getSideMenuAutoHideDef(){
		if (isMobileDevice()) return false;  //if remove this line, need to handle mobile hover event
		return(sideMenuAutoHideDef);
	}
	public void setSideMenuAutoHide(boolean isAutoHide){
		if (isMobileDevice()) return;
		sideMenuAutoHide = isAutoHide;
	}
	public boolean getSideMenuAutoHideDefaultPin(){
		if (isMobileDevice()) return false;  //if remove this line, need to handle mobile hover event
		return(sideMenuAutoHideDefaultPin);
	}
	public void setSideMenuAutoHideDefaultPin(boolean isAutoHide){
		if (isMobileDevice()) return;
		sideMenuAutoHideDefaultPin = isAutoHide;
	}
	public boolean getSchemaDebugFlag(){
		return schemaDebugFlag;
	}
	public void setSchemaDebugFlag(boolean p_schemaDebugFlag){
		schemaDebugFlag = p_schemaDebugFlag;
	}
	public boolean getAllowChangeLoginId(){
		return(allowChangeLoginId);
	}
	public boolean getAllowBroadcastMsg(){
		return(allowBroadcastMsg);
	}
	/*
	public boolean isPagingMode(){
		if (isMobileDevice()) return true;
		return(isPagingMode);
	}
	*/
	/***
	 * this method is not well tested
	 * it's use for debug only
	 * @param p_newLoginId
	 * @return
	 */
	public ReturnMsg changeLoginId(String p_newLoginId){
		return(changeLoginId(p_newLoginId,p_newLoginId));
	}
	public ReturnMsg changeLoginId(String p_newLoginId,String p_vcode){
		try{
//			if (!(getAllowChangeLoginId() && isAdminUser())){
//				return new ReturnMsg(false, "No permission.");
//			}
			String newLoginId = p_newLoginId == null ? null : p_newLoginId.trim();

			//change id case
			if (StringUtils.isNotBlank(newLoginId)){
				/*
				if (StringUtils.equals(newLoginId, getLoginId())){
					UniLog.log1("newId and curId are the same, skip up");
					return new ReturnMsg(false, "No update performed.");
				}
				*/
				UniLog.log1("change id to " + newLoginId);
				setLoginId(newLoginId);
				setVcode(null); //clear fLogin
				setVcode(p_vcode); //update access right
				if (!readUserProfileJson(true).getStatus()) {
					UniLog.log1("readUserProfile fail");
					return ReturnMsg.defaultFail;
				}
				clearSideMenuCache();
				return new ReturnMsg(true, "Login ID change to " + newLoginId + " / " + p_vcode);
			}
			else {
				return new ReturnMsg(false, "Login id cannot be blank");
				/*
				if (orgLoginId != null){ //reset login id to org
					if (StringUtils.equals(orgLoginId, getLoginId())){
						UniLog.log1("orgId and curId are the same, skip up");
						return new ReturnMsg(false, "No update performed.");
					}
					UniLog.log1("reset id to " + orgLoginId);
					setLoginId(orgLoginId);
					setVcode(null); //clear fLogin
					setVcode(orgLoginId); //update access right
					if (!readUserProfileJson(true).getStatus()) {
						UniLog.log1("readUserProfile fail");
						return ReturnMsg.defaultFail;
					}
					clearSideMenuCache();
					return new ReturnMsg(true, "Login ID reset");
				}
				*/
			}
//			return new ReturnMsg(false, "No update performed.");
		}
		catch(Exception ex){
			return new ReturnMsg(false,ex);
		}
	}
	/***
	 * check user has permission to retrieve the file
	 * @param p_key - filing key
	 * @return
	 */
	public boolean isFilingKeyValid(String p_key){
		if (StringUtils.isBlank(p_key)){
			UniLog.log1("key is blank");
			return false;
		}
    	//andrew191004: winecave stock photo allow public access.
    	if (StringUtils.startsWith(p_key, "jxStockImageFiling")){
    		UniLog.log1("allow jxStockImage for public");
    		return true;  
    	}
    	
		synchronized(filingKeyWhiteListHS){
			return (filingKeyWhiteListHS.contains(p_key));
		}
	}
	/***
	 * add filingkey to white list
	 * @param p_key - filing key
	 */
	public void addFilingKeyWhiteList(String p_key){
		if (StringUtils.isBlank(p_key)){
			return;
		}
		synchronized(filingKeyWhiteListHS){
			filingKeyWhiteListHS.add(p_key);
		}
	}
	
	public String toString(){
		return(String.format("loginId:%s isLogin:%s agent:%s hashCode:%d", getLoginId(), isLogin(), getAgent(),this.hashCode()));
		
	}
	public String getSessionKey() {
		if (sessionKey == null) {
			return "";
		}
		return sessionKey;
	}
	
	/***
	 * handy method to create a iniHelper
	 * @param p_agent - null default agent
	 * @return
	 */
	public static IniHelper getIniHelper(String p_agent) {
		try {
			String iniAgent = p_agent;
			if (StringUtils.isBlank(iniAgent)) {
				iniAgent = MapUtil.getString(getDefPropMap(),"iniAgent",null);
			}
			if (StringUtils.isBlank(iniAgent)) {
				UniLog.log1("iniAgent is blank, abort");
				return null;
			}
			
			String iniFile = MapUtil.getString(getDefPropMap(), "iniFile", null);
			/*
			if (StringUtils.isNotBlank(iniFile)){
				UniLog.log1("use custom iniFile:%s agent:%s", iniFile, iniAgent);
				return new IniHelper(new FileReader(new File(iniFile)), iniAgent);
			}
			else{
				UniLog.log1("use default iniFile:%s agent:%s", defIniFileName, iniAgent);
				return new IniHelper(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream(defIniFileName)), iniAgent);
			}
			*/
			return new IniHelper(iniFile, defIniFileName, iniAgent, getIniAgentAllowChildIni());
			
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	public static boolean getIniAgentAllowChildIni() {
		return MapUtil.getBoolean(getDefPropMap(), "iniAgentAllowChildIni", false);
	}
	public static IniHelper getIniHelper() {
		return getIniHelper(null);
	}
	public static String buildServletPath(HttpServletRequest p_request, boolean p_withQueryString) {
		StringBuffer sb = new StringBuffer();
		if (p_request.getServletPath() != null){
			sb.append(p_request.getServletPath());
		}
		if (p_withQueryString && p_request.getQueryString() != null){
			sb.append("?");
			sb.append(p_request.getQueryString());
		}
		return sb.toString();
	}
	/***
	 * calculate hash of a string
	 * @param p_string
	 * @return
	 */
	public static String calHash(String p_string) {
		try {
			return DigestUtils.sha256Hex(p_string);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return "";
		}
	}
	/***
	 * obtain password hash
	 * @return
	 */
	public String getPasswordHash() {
		return passwordHash;
	}
	final public boolean isValidAgent() {
		return StringUtils.isNotBlank(rpcServerHost) && rpcServerPort > 0;
	}
	
	public boolean getAllowZkBiAu() {
		return allowZkBiAu;
	}
	public boolean getAllowJSBroadcastChannel() {
		return allowJSBroadcastChannel;
	}
	public boolean getAllowJSIdleCtrl() {
		return allowJSIdleCtrl;
	}
	public int getIdleCtrlMaxIdle() {
		//return idleCtrlMaxIdle > 600000 ? idleCtrlMaxIdle : 600000;  //min 10m, suggest 60m
		return idleCtrlMaxIdle;
	}
	public int getIdleCtrlInterval() {
		return idleCtrlInterval > 1000 ? idleCtrlInterval : 1000;  //min 1s, suggest 60s
	}
	
	public String getCustomBiViewBase() {
		//return ""; //dev in progress, disable it first
		return customBiViewBase;
	}
	public boolean getAllowLoginTokenConfirm() {
		return allowLoginTokenConfirm;
	}
	
	public void addActiveDesktop(String p_dtId) {
		if (StringUtils.isBlank(p_dtId)) return;
		synchronized(activeDesktopSet) {
			activeDesktopSet.put(p_dtId,null);
			activeDesktopStr = StringUtils.join(activeDesktopSet,",");
			updateActiveUser(this,null,null);
			UniLog.log1("add desktop: %s %s", p_dtId, activeDesktopSet);
		}
	}
	public void deleteActiveDesktop(String p_dtId) {
		if (StringUtils.isBlank(p_dtId)) return;
		synchronized(activeDesktopSet) {
			activeDesktopSet.remove(p_dtId);
			activeDesktopStr = StringUtils.join(activeDesktopSet,",");
			UniLog.log1("delete desktop: %s %s", p_dtId, activeDesktopSet);
		}
	}
	public LinkedHashMap<String,String> getActiveDesktopMap(){
		LinkedHashMap<String,String> cloneMap = null;
		synchronized(activeDesktopSet) {
			cloneMap = (LinkedHashMap<String,String>) activeDesktopSet.clone(); //only clone the structure
		}
		return cloneMap;
	}
	
	public String getWPBaseURL() {
		return wpBaseURL;
	}
	public boolean getWPLinkUser() {
		return wpLinkUser;
	}
	public boolean getWPLinkStock() {
		return wpLinkStock;
	}
	public String getWPLogin() {
		return wpLogin;
	}
	public String getWPPassword() {
		return wpPassword;
	}
	
	public void setHomePage(String p_homePage) {
		homePage = p_homePage;
	}
	
	public String getHomePage() {
		if(homePage != null && !homePage.trim().equals("")) {
				return(homePage);
		}
		return(getLandingPage());
	}
	public boolean getAllowWallpaper() {
		return allowWallpaper;
	}
	public boolean getAllowMenuColor() {
		return allowMenuColor;
	}
	public boolean getAllowInheritMenuStyle() {
		return allowInheritMenuStyle;
	}
	public boolean getAllowS2Listbox() {
		return allowS2Listbox;
	}
	public boolean getS2AllowClearDef() {
		return s2AllowClearDef;
	}
	public boolean getAllowShowAgents() {
		return allowShowAgents;
	}
	public boolean getAllowFCM() {
		return allowFCM;
	}
	public boolean isOpenPageIframe() {
		/*
		if (isMobileDevice()) {
			return false;
		}
		*/
		return StringUtils.equals(openPageMode, "iframe") ? true : false;
	}
	
	/***
	 * create tmp login token
	 * it keep login/password into tltMap without validation
	 * 
	 * @param p_loginid
	 * @param p_password
	 * @return
	 */
	public ReturnMsg genTLT(String p_loginId, String p_password) {
		UniLog.log1("called");
		//validation
		if (StringUtils.isAnyBlank(p_loginId, p_password)) {
			UniLog.log1("loginId or password is blank");
			return new ReturnMsg(false,"loginid or password is blank");
		}
		
		Map<String,String> lkMap = buildLoginTokenString(getAgent());
		String key = MapUtil.getString(lkMap, "key","");
		if (StringUtils.isBlank(key)) {
			UniLog.log1("key is blank");
			return new ReturnMsg(false, "unable to create token, key is blank");
		}
		lkMap.put("loginId", p_loginId);
		lkMap.put("password", p_password);
		lkMap.put("cuser", getLoginId());
		lkMap.put("ctime", String.valueOf((long) (new Date()).getTime() / 1000));
		synchronized(tltMap) {
			tltMap.put(key, lkMap);
		}
		return new ReturnMsg(true).setData(key);
	}
	
	
	/***
	 * get tmp login token key from tltMap 
	 * without login/password validation
	 * @param p_key
	 * @return return the associated lkMap. null if invalid
	 */
	public static Map<String,String> getTLT(String p_key, boolean p_destroy){
		if (StringUtils.isBlank(p_key)) {
			UniLog.log1("key is blank");
			return null;
		}
			
		Map<String,String> lkMap = null;
		synchronized(tltMap) {
			lkMap = tltMap.get(p_key);
			if (lkMap == null) {
				UniLog.log1("tlt:%s lkMap is null", p_key);
				return null;
			}
			if (p_destroy) {
				UniLog.log1("DEBUG: remove %s from map", p_key);
				tltMap.remove(p_key);
			}
		}
		
		Long ctime = MapUtil.getLong(lkMap, "ctime");
		if (ctime == null) {
			UniLog.log1("tlt:%s ctime is null", p_key);
			return null;
		}
		if (new Date().getTime() < ( ctime + TLT_MAX_AGE)) {
			UniLog.log1("tlt:%s lkMap is valid");
			return lkMap;
		}
		else {
			UniLog.log1("tlt:%s lkMap expired", p_key);
			return null;
		}
	}
	public boolean hasAccessRight(String p_key)	{
		if(StringUtils.isBlank(p_key)) return(true);
		if (!"#never".equals(p_key) && isAdminUser()) { //andrew201113 admin can bypass access right checking
			return true;  
		}
		if(accessRights == null) {
			UniLog.log1("accessRights is null");
			return(false);
		}
		return(accessRights.contains(p_key));
	}
	/***
	 * get access right key by keyprefix
	 * e.g. #maxdel -> #maxdel50
	 * @param p_key
	 * @return
	 */
	public String getAccessRightKeyByPrefix(String p_keyPrefix) {
		for (String key:accessRights) {
			if (StringUtils.startsWith(key, p_keyPrefix)){
				return key;
			}
		}
		return null;
	}
	
	
	/***
	 * for construct webmenu data strcture
	 * remark: better move all menu related code to standalone file
	 *
	 */
	private class WebMenuNode{
		int level = 0; //level 0 root node;
		WebMenuNode parent = null;
		
		HashMap<String,String> attrs = new HashMap<String,String>();
		public WebMenuNode add(String p_img) {
			WebMenuNode newNode = new WebMenuNode();
			newNode.attrs.put("img",p_img);
			newNode.level = level + 1;
			newNode.parent = this;
			return newNode;
		}
		/***
		 * icon will inherit parent node style
		 * it does not affect image
		 * 
		 * @return
		 */
		public String getImg() {
			String img = attrs.get("img");
			try {
				String iconClass = null;
				
				//loop from child to parent
				for (WebMenuNode curNode = this; curNode != null; curNode = curNode.parent) {
					String curImg = curNode.attrs.get("img");
					int curLevel = curNode.level;
					//UniLog.log1("debug level:%d img:%s", curLevel, curImg);

					//use parent img when img is blank
					if (StringUtils.isBlank(curImg)) {
						UniLog.log1("level:%d curImg is blank, try to use parent img", curLevel);
						continue;
					}
					
					//non icon img, abort
					if (!StringUtils.startsWithAny(curImg, "fa-","flaticon-")){
						break;
					}

					String [] curImgSplit = StringUtils.split(curImg, " ");
					if (iconClass == null && curImgSplit.length > 0) {
						iconClass = curImgSplit[0];
					}

					//icon with style class
					if (curImgSplit.length > 1) {
						String mergedImg = iconClass + " " +StringUtils.join(curImgSplit, " ", 1, curImgSplit.length);
						UniLog.log1("getImg org:(%s) new:(%s)", img, mergedImg);
						return mergedImg;
					}
					
					//icon without style class
					if (curLevel == 1 && StringUtils.isNotBlank(iconClass)) {
						return iconClass;
					}
				}
				if (StringUtils.isNotBlank(iconClass)) {
					return iconClass;
					
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			return img == null ? "" : img;
		}
	}
	/*
	public static void main(String[] args) {
		UniLog.log1("%s", getSessionHelperDummy(null));
		System.exit(0);
	}
	*/
	
	static public String getUrlByViewid(SessionHelper p_sp,String p_viewid) {
		Hashtable<String,String> viewUrl =  (Hashtable<String,String>)p_sp.getSessionData("VIEWURL");
		if(viewUrl == null) {
			viewUrl = new Hashtable<String,String>();
			p_sp.putSessionData("VIEWURL", viewUrl);
			SelectUtil su = null;
			try {
				su = new SelectUtil();
				su.init(p_sp.getLoginTokenJdbcPool());
				TableRec tr = su.getQueryResult("select * from webmenu");
				if(tr.existField("webm_viewid")) {
					for(int i=0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						String viewid = tr.getFieldString("webm_viewid");
						String url = tr.getFieldString("webm_url");
						if(!viewid.isEmpty() && !url.isEmpty()) {
							viewUrl.put(viewid, url);
						}
					}
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			finally {
				if (su != null) su.close();
			}
		}
		return(viewUrl.get(p_viewid));
	}
	
	/***
	 * save all url param
	 * @param p_urlParams
	 */
	protected void setURLParams(HttpServletRequest p_request) {
		if (p_request == null) {
			return;
		}
		Map<String,String[]> newUrlParams = p_request.getParameterMap();
		if (newUrlParams == null) {
			urlParams.clear();
			return;
		}
		{
			String pko[] = newUrlParams.get("passport");
			if(pko != null && pko.length > 0) {
				String ppk = pko[0];
				if( ppk != null) {
					if(passportKey != null && passportKey.equals(ppk)) {
						// request Passport = current Passport, do nothing;
					} else {
						SessionPassport spp = SessionPassport.getPassPort(ppk);
						if(spp != null) {
							changeLoginId(spp.getLoginId(),spp.getVcode());
							passportKey = ppk;
							passport = spp;
						} else {
							// passport not valid or expired
							int cc;
							cc = 0;
							passportKey = null;
							passport=null;
						}
					}
				}
					
			}
		}
		
		//andrew210512: not yet handle multitab
		if (urlParamsHashcode == newUrlParams.hashCode()) {
			UniLog.log1("url param keep unchange");
			return;
		}
		
		boolean needRefreshSideMenu = false;
		
		//update urlParams
		urlParams.clear();
		urlParams.putAll(newUrlParams);
		urlParamsHashcode = newUrlParams.hashCode();
		
		
	
		//url parameter related code
		this.uaMobileFlag = false;
		parseUserAgent(p_request);
		if (userAgent != null && userAgent.getAvailableFieldNamesSorted().contains("OperatingSystemClass"))
			this.uaMobileFlag = StringUtils.equalsIgnoreCase(userAgent.getValue("OperatingSystemClass"), "Mobile");
		else {
			String ua = p_request.getHeader("User-Agent");
			if(ua != null && ua.indexOf("Mobile") != -1) {
				this.uaMobileFlag = true;
			}
		}
		
		
		boolean orgIsMobileFlag = this.isMobileDevice();
		String mode = p_request.getParameter("mode");
		if (StringUtils.equals(mode, "mobile")){
			this.mode = "mobile";
		}
		else if (StringUtils.equals(mode, "pc")){
			this.mode = "pc";
		}
		if (orgIsMobileFlag != this.isMobileDevice()){  //andrew190808: fix mode equal to blank bug
			needRefreshSideMenu = true;
		}
		
		

		this.setLHLang(p_request.getParameter("lhlang"),2,false);
		
		/*
		boolean newLargeFlag = StringUtils.equalsAnyIgnoreCase(p_request.getParameter("large"),"Y","YES","TRUE");
    	if (newLargeFlag != getLargeFlag()) {
    		this.largeFlag = newLargeFlag;
    		needRefreshSideMenu = true;
    	}
    	*/
		String largeStr = p_request.getParameter("large");
		if (StringUtils.isNotBlank(largeStr)) {
			boolean newLargeFlag = StringUtils.equalsAnyIgnoreCase(largeStr,"Y","YES","TRUE");
			if (newLargeFlag != getLargeFlag()) {
				this.largeFlag = newLargeFlag;
				needRefreshSideMenu = true;
			}
		}
    	
		String newTheme = p_request.getParameter("theme");
		if (StringUtils.isNotBlank(newTheme) && !StringUtils.equals(newTheme, curTheme)) {
			curTheme = newTheme;
			needRefreshSideMenu = true;
		}
		
		allowShowDebug = StringUtils.equalsAnyIgnoreCase(p_request.getParameter("showdebug"), "Y", "TRUE");
    	
		
		String dev = p_request.getParameter("dev");
		if (dev != null && dev.toUpperCase().trim().equals("Y")){
			this.setDevMode(true);
		}
		
		this.autoRefresh = 0;
		String autoRefreshString = p_request.getParameter("autorefresh");
		if (autoRefreshString != null){
			try{
				this.autoRefresh = Integer.parseInt(autoRefreshString) * 1000; //change unit to ms
				if (this.autoRefresh > 0){
					UniLog.log("set autoRefresh to" + this.autoRefresh);
				}
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
		
		String syslog = p_request.getParameter("syslog");
		if (syslog != null && syslog.toUpperCase().trim().equals("Y")){
			this.allowSyslog = true;
		}
		else{
			this.allowSyslog = false;
		}
		
		//this.isPagingMode = StringUtils.equalsAnyIgnoreCase(p_request.getParameter("paging"), "Y","",null);
		
		if (needRefreshSideMenu) {
			this.clearSideMenuCache();
		}
		//this.updateActiveUser(p_request.getParameter("viewid"));
		updateActiveUser(this,null,buildServletPath(p_request,true));
	}
//	public abstract Map<String,Object> getURLParams();
//	public Map<String,Object> getURLParams(){
//   	return urlParams;
//   }
	abstract public String getURLParam(String p_key);
//    public String getURLParam(String p_key) {
//    	/*
//	  	Object obj = urlParams.get(p_key);
//	  	if (obj == null) {
//	  		return null;
//	  	}
//	  	else if (obj instanceof String) {
//	  	   return (String) obj;
//	  	}
//	  	else if (obj instanceof String[] && ((String []) obj).length > 0) {
//	  		return ((String []) obj)[((String []) obj).length-1];
//	  	}
//	  	return null;
//	  	*/
//    	return(getURLParamAsString(p_key,urlParams));
//    }

    static public String getURLParamAsString(String p_key,Map<String,Object> urlParams)  {
	  	Object obj = urlParams.get(p_key);
	  	if (obj == null) {
	  		return null;
	  	}
	  	else if (obj instanceof String) {
	  	   return (String) obj;
	  	}
	  	else if (obj instanceof String[] && ((String []) obj).length > 0) {
	  		return ((String []) obj)[((String []) obj).length-1];
	  	}
	  	return null;
    }
    
    public boolean getURLParamBool(String p_key) {
    	return StringUtils.equalsAnyIgnoreCase(getURLParam(p_key),"Y","YES","TRUE");
    }

    public int getPageSize() {
    	//obtain from request to handle multitab share same sessionHelper
    	//HttpServletRequest request = getHttpServletRequest(getClass());
    	/*if (request == null) {
    		return defPageSize;
    	}
		String pageSizeStr = request.getParameter("pagesize");*/
    	String pageSizeStr = getURLParam("pagesize");
		if (pageSizeStr != null) {
			try {
				int newPageSize = Integer.parseInt(pageSizeStr);
				if (newPageSize > maxPageSize) {
					return maxPageSize;
				}
				return newPageSize;
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		return defPageSize;
    }
    public boolean getAllowElectronIntegration() {
		return allowElectronIntegration;
    }
    public boolean getLargeFlag() {
    	//return largeFlag;
    	return (largeFlag == null ? defLargeFlag : largeFlag.booleanValue());
    }
    public boolean getAllowLargeSwitch() {
    	return allowLargeSwitch;
    }
    public void setAllowUserProfile(boolean p_sw) {
    	allowUserProfile = p_sw;
    }
    public boolean getAllowUserProfile() {
    	return allowUserProfile;
    }
    /***
     * check viewid exist in current sidemenu
     * @param p_viewId
     * @return
     */
    public boolean checkSideMenuViewExist(String p_viewId) {
    	return sideMenuViewCache.containsKey(p_viewId);
    }
    
    /***
     * check helpid exist in current sidemenu
     * 
     * @param p_helpId
     * @return
     */
    public boolean checkSideMenuHelpExist(String p_helpId) {
    	return sideMenuHelpCache.containsKey(p_helpId) ? true : checkSideMenuViewExist(p_helpId);
    }
    
    public boolean isStraightPassword() {
    	return(straightPassword);
    }
    
    public String getPublicBaseURL() {
    	return publicBaseURL;
    }
    
    public boolean getAllowMobileAdvanceSearch() {
		return(allowMobileAdvanceSearch);
    }
    public boolean getAllowReplySlip() {
    	return allowReplySlip;
    }
    public boolean getAllowUpdatePivot() {
    	return allowUpdatePivot;
    }
    /*
    public boolean getAllowNotifyMsg() {
    	return(notifyMsgMethod != null);
    }
    */
    
    public boolean useJxFormG2() {
    	return(useJxFormG2);
    }
    public boolean getAllowListboxHoverEffect() {
    	if (isMobile()) return false;
    	return (allowListboxHoverEffect);
    }
    public boolean getAllowDashboardWidget() {
    	return allowDashboardWidget;
    }
    public boolean getAllowOptionTranslate() {
    	return (allowTranslate && allowOptionTranslate);
    }
    public boolean getAllowPresetAccessKey() {
    	return allowPresetAccessKey;
    }
    public boolean getAllowRecordCopy() {
    	//check urlparam
    	if (StringUtils.equalsAnyIgnoreCase(getURLParam("recordcopy"),"Y","YES","TRUE")) {
    		return true;
    	}
    	if (StringUtils.equalsAnyIgnoreCase(getURLParam("recordcopy"),"N","NO","FALSE")) {
    		return false;
    	}
    	
    	//if no urlparam, use ini as default
    	return allowRecordCopy;
    }
    public boolean getAllowQuickFilterURLParam() {
    	return allowQuickFilterURLParam;
    }
    public boolean getAllowEmbedSearch() {
    	return allowEmbedSearch;
    }
    public boolean getAllowSitemapAsHomePage() {
    	return allowSitemapAsHomePage;
    }
    public boolean getAllowWfmContextMenu() {
    	return allowWfmContextMenu;
    }
    public boolean getAllowAdvSearchUseS2() {
    	return allowS2Listbox && allowAdvSearchUseS2;
    }
    public boolean getGeneralReportApplyQuickFilter() {
    	return generalReportApplyQuickFilter;
    }
    public boolean getGeneralReportOutputCsv() {
    	return generalReportOutputCsv;
    }
    public boolean getAllowBatchPrtdocAsync() {
    	return allowBatchPrtdocAsync;
    }
    
    /***
     * get current theme
     * @return non null value. format:theme1_theme2_theme3 e.g. large_g2
     */
    public String getTheme() {
    	if (StringUtils.isNotBlank(curTheme)) {
    		return curTheme;
    	}
    	return StringUtils.defaultIfBlank(defTheme,"default");
    }
    
    /***
     * just show more debug info on screen/log
     * default false, turn it on by add showdebug=Y 
     * don't use it access control
     * @return
     */
    public boolean getAllowShowDebug() {
    	return allowShowDebug;
    }
    
    public boolean getAllowDesktopCleanup() {
    	return allowDesktopCleanup;
    }
    
    public boolean getAllowCustomDatebox() {
    	return allowCustomDatebox;
    }
    public boolean getAllowCustomListbox() {
    	return allowCustomListbox;
    }
    
    public int getDefaultRecordLimit() {
    	return(defaultRecordLimit);
    }
    public boolean useS2ListboxForReadOnly() {
    	return(useS2ListboxForReadOnly);
    }
    
    /*
    //221207 duplicated with getLargeFlag
    public boolean isLarge() {
    	return (largeFlag == null ? defLargeFlag : largeFlag);
    }
    */
    public String carryURLParam(String p_href) {
    	//validation
    	if (StringUtils.isBlank(p_href)) {
    		return p_href;
    	}
    	if (StringUtils.startsWith(p_href,"javascript")){
    		return p_href;
    	}
    	StringBuilder sb = new StringBuilder();
    	sb.append(p_href);
    	
    	//carry url param
    	
    	//agent
    	if (allowMenuUrlWithAgent && StringUtils.isNotBlank(iniAgent)){
    		sb.append((sb.toString().indexOf('?') > 0 ? '&' : "?") + "agent=" + iniAgent);
    	}
    	
		//mode
		if (StringUtils.isNoneBlank(mode)) {
			sb.append((sb.toString().indexOf('?') > 0 ? '&' : "?") + "mode=" + mode);
		}
		
		//large
		if (getLargeFlag()) {
			sb.append((sb.toString().indexOf('?') > 0 ? '&' : "?") + "large=Y");
		}
		
		//theme
		sb.append((sb.toString().indexOf('?') > 0 ? '&' : "?") + "theme=" + getTheme());
		
		return sb.toString();	
    }
    
    public boolean isLocalhostRequest() {
    	return StringUtils.equalsAnyIgnoreCase(serverName, "localhost","127.0.0.1");
    }
    
    public boolean getAllowShowSysEnv() {
    	return (allowShowSysEnv && isLocalhostRequest());
    }
    
    /***
     * when demo is on, mask out sensitive data if possible
     * @return
     */
    public boolean getAllowDemo() {
    	return allowDemo;
    }
    
    public boolean dblogAddEnabled() {
    	return(allowDbAddLog);
    }
    public void setdblogAddEnabled(boolean p_bool) {
    	allowDbAddLog = p_bool;
    }
    public boolean dblogUpdateEnabled() {
    	return(allowDbUpdateLog);
    }
    public void setdblogUpdateEnabled(boolean p_bool) {
    	allowDbUpdateLog = p_bool;
    }
    public boolean dblogDeleteEnabled() {
    	return(allowDbDeleteLog);
    }
    public void setdblogDeleteEnabled(boolean p_bool) {
    	allowDbDeleteLog = p_bool;
    }
    
    /***
     * when ignoreencode is on, normalize schn/tchn
     * @return
     */
    public boolean getAllowIgnoreEncode() {
    	return allowIgnoreEncode;
    }
    
    public boolean getAllowMaintMode() {
    	return (allowMaintMode);
    }
    
    public boolean getDataImportAlwaysAdd() {
		return dataImportAlwaysAdd;
    }
    
    
    /***
     * obtain device feature
     * remark: data usually collected via clientside js onStartup. These data is availble after doAfterCompose.
     * the trick is obtain it in the LoginPage first, so these info will be available on the landing page.
     * @param p_feature
     * @return
     */
    public DEVICE_FEATURE_STATE checkDeviceFeature(DEVICE_FEATURE p_feature) {
    	DEVICE_FEATURE_STATE feature = deviceFeatureHM.get(p_feature);
    	if (feature != null) {
    		return feature;
    	}
    	return DEVICE_FEATURE_STATE.UNKNOWN;
    }
    public void setDeviceFeature(String p_featureStr, String p_stateStr) {
    	try {
    		setDeviceFeature(DEVICE_FEATURE.valueOf(p_featureStr), DEVICE_FEATURE_STATE.valueOf(p_stateStr));
    	}
    	catch(Exception ex) {
    		//ex.printStackTrace();
    		UniLog.log1("error:" + ex.getMessage());
    	}
    }
    public void setDeviceFeature(DEVICE_FEATURE p_feature, DEVICE_FEATURE_STATE p_state) {
    	if (p_feature == null || p_state == null) return;
   		deviceFeatureHM.put(p_feature, p_state);
    }
    
    
    /***
     * enter maintenance mode
     * @return
     */
    public ReturnMsg enterMaintMode() {
    	UniLog.log1("called");
    	if (!getAllowMaintMode()) return new ReturnMsg(false, "Feature disabled");
		maintModeFlag.set(true);
		return ReturnMsg.defaultOk;
    }
    
    /***
     * leave maintenance mode
     * @return
     */
    public ReturnMsg leaveMaintMode() {
    	UniLog.log1("called");
    	if (!getAllowMaintMode()) return new ReturnMsg(false, "Feature disabled");
		maintModeFlag.set(false);
		return ReturnMsg.defaultOk;
    }
    
    /***
     * check does allow new login
     * @param p_loginId
     * @return
     */
    private ReturnMsg allowNewLogin(String p_loginId) {
    	//if disabled, skip validation
    	if (!getAllowMaintMode()) {
    		return ReturnMsg.defaultOk;
    	}
    	
    	//if not under maintenance, allow login
    	if (!maintModeFlag.get()) {
    		return ReturnMsg.defaultOk;
    	}
    	
    	//allow admin to login
    	if (StringUtils.isNotBlank(p_loginId) && isAdminUser(p_loginId)) {
    		return ReturnMsg.defaultOk;
    	}
    	
    	UniLog.log1("not allow new login. loginid:%s maintModeFlag:%s", p_loginId, maintModeFlag.get());
    	return new ReturnMsg(false, "System is under maintenance. Please try again later.");
    }
    
    /***
     * get current maintenance mode flag
     * @return
     */
    public static boolean getMaintModeFlag() {
    	return maintModeFlag.get();
    }
    
    /*
     obsoleted
    public int getCellFormulaVersion() {
		return(cellFormulaVersion);
    }
    */
    
    public String getDbName() {
    	String ss = databaseLabel;
    	int cc =ss.indexOf('@');
    	if(cc >= 0) {
    		return(databaseLabel.substring(0, cc));
    	} else {
    		return(databaseLabel);
    	}
    }
    
    public boolean isBiSchemaView() {
    	return(getDbName().equals("bischema"));
    }
    
    public boolean isDbAllowNull() {
    	return(dbAllowNull);
    }
    
    public boolean getAllowPickOldInput() {
    	return allowPickOldInput;
    }
    public boolean isUseVersionControl() {
    	return(useVersionControl);
    } 
    public boolean disableLcPopup() {
    	return(disableLcPopup);
    } 

	public static void testJSP(HttpServletRequest p_request, HttpServletResponse p_response) {
		UniLog.log("HAHA in testJSP");
	}
	

	
	public int getMaxDetailRow() {
		return(maxDetailRow);
	}
	
	public URL getResourceURL(String p_path) throws Exception {
		return(svc.getResource(p_path));
	}

	public String getWallPaperPath_gen(boolean p_portriat,String p_style) {
		String style;
		if(!StringUtils.isBlank(p_style)) {
			style = p_style+"_";
		} else {
			style = "";
		}
		try {
			String path;
			if(resourceCacheDir != null) {
				if(p_portriat) {
					path = "cache/"+resourceCacheDir+"/"+"images/wallpaper_"+style+"p.jpg";
					if(getResourceURL(path) != null) return(path);
				} else {
					path = "cache/"+resourceCacheDir+"/"+"images/wallpaper_"+style+"l.jpg";
					if(getResourceURL(path) != null) return(path);
				}
				path = "cache/"+resourceCacheDir+"/"+"images/default_wallpaper"+".jpg";
				if(getResourceURL(path) != null) return(path);
			}
			if(p_portriat) {
				path = "images/wallpaper_"+style+"p.jpg";
				if(getResourceURL(path) != null) return(path);
			} else {
				path = "images/wallpaper_"+style+"l.jpg";
				if(getResourceURL(path) != null) return(path);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		if(p_portriat) {
				return("images/wallpaper_light_p.jpg");
		} else {
			return("images/wallpaper_light_l.jpg");
		}
	}
	public String getWallPaperPath(boolean p_portriat) {
		return(getWallPaperPath_gen(p_portriat,"light"));
		/*
		try {
			String path;
			if(resourceCacheDir != null) {
				if(p_portriat) {
					path = "cache/"+resourceCacheDir+"/"+"images/wallpaper_light_p.jpg";
					if(getResourceURL(path) != null) return(path);
					path = "cache/"+resourceCacheDir+"/"+"images/wallpaper_light_p.png";
					if(getResourceURL(path) != null) return(path);
				} else {
					path = "cache/"+resourceCacheDir+"/"+"images/wallpaper_light_l.jpg";
					if(getResourceURL(path) != null) return(path);
					path = "cache/"+resourceCacheDir+"/"+"images/wallpaper_light_l.png";
					if(getResourceURL(path) != null) return(path);
				}
			}
			
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		if(p_portriat) {
			return("images/wallpaper_light_p.jpg");
		} else {
			return("images/wallpaper_light_l.jpg");
		}
		*/
	}
	
	
	public String getWallPaperPath() {
		return(getWallPaperPath(false));
	}
	public String getLoginBackgroundPath(boolean p_portriat) {
		return(getWallPaperPath_gen(p_portriat,""));
		/*
		try {
			String path;
			if(resourceCacheDir != null) {
				if(p_portriat) {
					path = "cache/"+resourceCacheDir+"/"+"images/wallpaper_p.jpg";
					if(getResourceURL(path) != null) return(path);
					path = "cache/"+resourceCacheDir+"/"+"images/wallpaper_p.png";
					if(getResourceURL(path) != null) return(path);
				} else {
					path = "cache/"+resourceCacheDir+"/"+"images/wallpaper_l.jpg";
					if(getResourceURL(path) != null) return(path);
					path = "cache/"+resourceCacheDir+"/"+"images/wallpaper_l.png";
					if(getResourceURL(path) != null) return(path);
				}
			}
			
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		if(p_portriat) {
			return("images/wallpaper_p.jpg");
		} else {
			return("images/wallpaper_l.jpg");
		}
		*/
	}
	
	
	public String getLoginBackgroundPath() {
		return(getLoginBackgroundPath(false));
	}
	
	public boolean useCompDiv() {
		return(useCompDiv);
	}
	public boolean useNewImport() {
		return(useNewImport);
	}

	public ServletContext getSvc() {
		return(svc);
	}

	abstract public String getWebContentRealPath(String p_path, boolean p_withSeparator);
//	public String getWebContentRealPath(String p_path, boolean p_withSeparator){
//		return ZkUtil.getWebContentRealPath(svc, p_path, p_withSeparator);
//		return(null);
//	}	
    public boolean autoChineseConvertEnabled() {
    	return(enableAutoChineseConvert);
    }
    
    private void parseUserAgent(HttpServletRequest p_request) {
    	userAgent = null;
    	try {
    		String userAgentStr = p_request.getHeader("User-Agent");
		  	userAgent = userAgentAnalyzeruaa.parse(userAgentStr);
		  	UniLog.log1("userAgent:%s", userAgent);
    	} catch (Exception e) {
    		UniLog.log(e);
    	}
    }
    
    public UserAgent getUserAgent() {
    	return userAgent;
    }
    
    public static void setSessionTimeout(HttpServletRequest request, int sec) {
    	request.getSession().setMaxInactiveInterval(sec);
    }
    public String getRootMenu() {
    	return(rootMenu);
    }
    public String getPassportContent() {
    	if(passport != null) return(passport.content); else return(null);
    }
    public String getPassportkey() {
    	if(passportKey != null) return(passportKey); else return(null);
    }
    public String getClassRootPath() {
    	return(null);
    }
    public void resetIniAgent(HttpServletRequest p_request) {
		String defaultIniAgent = MapUtil.getString(getDefPropMap(), "iniAgent", null);
		boolean defaultIniAgentAllowUpdate = MapUtil.getBoolean(getDefPropMap(), "iniAgentAllowUpdate");
		setIniAgent(defaultIniAgent, defaultIniAgentAllowUpdate, p_request);
    }
	public static enum EVENT_TYPE{ APPLICATION};
	public Object lookupEventQueue(String p_name,EVENT_TYPE p_type,boolean p_autoCreate) {
		return(null);
	}
	public void publishEventQueue(Object p_que,String p_eventStr,Object p_data) {
	}
}