package com.kikyosoft.servlet.larva.dashboard;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Executions;

import com.kikyosoft.rpccall.RpcClient;
import com.kikyosoft.rpccall.Value;
import com.kikyosoft.servlet.larva.common.UserProfile;
import com.kikyosoft.utils.LogUtil;
import com.kikyosoft.utils.MenuNode;
import com.kikyosoft.utils.ReturnMsg;
import com.kikyosoft.utils.SessionUtil;
import com.kikyosoft.utils.VectorUtil;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@WebServlet(urlPatterns = {"/larva/dashboard", "/larva/dashboard/", "/larva/dashboard/index"})
public class IndexController extends HttpServlet {

  // Simple DTOs for table/list sections
  public static class OrderRow {
    private final String trackingNo;
    private final String productName;
    private final int totalOrder;
    private final String status;        // Approved | Pending | Rejected
    private final BigDecimal totalAmount;
    public OrderRow(String t, String p, int o, String s, BigDecimal a) {
      this.trackingNo = t; this.productName = p; this.totalOrder = o; this.status = s; this.totalAmount = a;
    }
    public String getTrackingNo() { return trackingNo; }
    public String getProductName() { return productName; }
    public int getTotalOrder() { return totalOrder; }
    public String getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
  }

  public static class TransactionRow {
    private final String orderNo;
    private final String when;
    private final BigDecimal amount; // +/- value
    private final String ratio;      // "78%"
    private final String icon;       // "ti ti-gift"
    private final String tone;       // success | primary | danger
    public TransactionRow(String o, String w, BigDecimal a, String r, String i, String t) {
      this.orderNo = o; this.when = w; this.amount = a; this.ratio = r; this.icon = i; this.tone = t;
    }
    public String getOrderNo() { return orderNo; }
    public String getWhen() { return when; }
    public BigDecimal getAmount() { return amount; }
    public String getRatio() { return ratio; }
    public String getIcon() { return icon; }
    public String getTone() { return tone; }
  }
  
  
	static final String BICORERPC_PREFIX = "com.uniinformation.bicore.BiCoreRpcServlet.";
	
	
	public final class UrlSplit {
		  private final String baseUrl;
		  private final Map<String, String> params; // unmodifiable

		  /**
		   * @param fullUrlIncludeParams e.g. "https://host/app/page?a=1&b=two#frag"
		   * @param additionalParams extra params to merge; overrides existing keys if present
		   */
		  public UrlSplit(String fullUrlIncludeParams, Map<String, String> additionalParams) {
		    if (fullUrlIncludeParams == null) {
		      throw new IllegalArgumentException("fullUrlIncludeParams must not be null");
		    }
		    try {
		      URI uri = new URI(fullUrlIncludeParams);

		      // Build base URL (scheme + authority + path), no query, no fragment.
		      String scheme = uri.getScheme();                       // may be null for relative URLs
		      String authority = nullToEmpty(uri.getRawAuthority());
		      String path = nullToEmpty(uri.getRawPath());

		      String base;
		      if (scheme != null) {
		        base = scheme + "://" + authority + path;
		      } else if (!authority.isEmpty()) {
		        // scheme-less but with authority (rare): start with //
		        base = "//" + authority + path;
		      } else {
		        // relative URL like "/path/page"
		        base = path;
		      }
		      this.baseUrl = base;

		      // Parse existing query params (decoded) into insertion-ordered map
		      Map<String, String> m = new LinkedHashMap<>();
		      parseQueryIntoDecodedMap(uri.getRawQuery(), m);

		      // Merge additional params (override existing keys)
		      if (additionalParams != null) {
		        for (Map.Entry<String, String> e : additionalParams.entrySet()) {
		          if (e.getKey() != null) {
		            m.put(e.getKey(), e.getValue() == null ? "" : e.getValue());
		          }
		        }
		      }

		      this.params = Collections.unmodifiableMap(m);
		    } catch (URISyntaxException e) {
		      throw new IllegalArgumentException("Invalid URL: " + fullUrlIncludeParams, e);
		    }
		  }

		  public String getBaseUrl() {
		    return baseUrl;
		  }

		  /** Returns a read-only map of all combined params (original + additional; additional wins). */
		  public Map<String, String> getParams() {
		    return params;
		  }

		  /** Optional helper to rebuild a full URL including the merged params. */
		  public String buildFullUrl() {
		    if (params.isEmpty()) return baseUrl;
		    StringBuilder sb = new StringBuilder(baseUrl).append('?');
		    boolean first = true;
		    for (Map.Entry<String, String> e : params.entrySet()) {
		      if (!first) sb.append('&');
		      first = false;
		      sb.append(encode(e.getKey())).append('=').append(encode(e.getValue()));
		    }
		    return sb.toString();
		  }

		  // ----------------- helpers -----------------

		  private void parseQueryIntoDecodedMap(String rawQuery, Map<String, String> out) {
		    if (rawQuery == null || rawQuery.isEmpty()) return;
		    String[] pairs = rawQuery.split("&");
		    for (String pair : pairs) {
		      if (pair.isEmpty()) continue;
		      int i = pair.indexOf('=');
		      String rawKey = i >= 0 ? pair.substring(0, i) : pair;
		      String rawVal = i >= 0 ? pair.substring(i + 1) : "";

		      String key = decode(rawKey);
		      String val = decode(rawVal);

		      if (!key.isEmpty()) {
		        // If the same key repeats, last one wins (consistent with Map semantics)
		        out.put(key, val);
		      }
		    }
		  }

		  private String decode(String s) {
		    return URLDecoder.decode(nullToEmpty(s), StandardCharsets.UTF_8);
		  }

		  private String encode(String s) {
		    // URLEncoder uses '+' for space (application/x-www-form-urlencoded), which is fine for query strings.
		    // If you prefer %20, replace("+", "%20") after encoding.
		    return URLEncoder.encode(nullToEmpty(s), StandardCharsets.UTF_8);
		  }

		  private String nullToEmpty(String s) {
		    return (s == null) ? "" : s;
		  }

		  @Override
		  public String toString() {
		    return "UrlSplit{baseUrl='" + baseUrl + "', params=" + params + '}';
		  }
		}	
	
	
	
	String createIframeUrlWithPassport(String p_host,int p_port,String p_url, String p_loginid,Map<String,String> p_UrlParams ) {
		RpcClient rpc = new RpcClient(p_host,p_port);
        rpc.open();
        Value v = rpc.callSegment(BICORERPC_PREFIX+"ping");
        LogUtil.log("TestBiCoreRpc Ping got "+v);
        if(v == null || !v.toString().startsWith("OK")) {
        	rpc.close();
        	return(null);
        }
        JSONObject jo = new JSONObject();
        UrlSplit us = new UrlSplit(p_url,p_UrlParams);
        for(String param : us.getParams().keySet()) {
        	String val = p_UrlParams.get(param);
        	jo.put(param,val);
        }
        v = rpc.callSegment(BICORERPC_PREFIX+"makePassPort",
    		new VectorUtil()
    			.addElement(p_loginid)
    			.addElement(60000)
    			.addElement("")
    			.addElement("")
    			.addElement("Y")
    			.addElement(jo.toString())
    			.toVector()
    		);		
        LogUtil.log("TestBiCoreRpc makePassPort got "+v);
        if(v == null || !v.toString().startsWith("OK")) {
        	rpc.close();
        	return(null);
        } else {
        	rpc.close();
        	String finalUrl = p_url + (p_url.contains("?")  ? "&" : "?") +"passport="+URLEncoder.encode(v.toString().substring(4),StandardCharsets.UTF_8);
        	for(String param : p_UrlParams.keySet()) {
        		String val = p_UrlParams.get(param);
        		finalUrl += "&"+param+"="+val;
        	}
        	return(finalUrl);
        }
	}
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    SessionHelper sp = ZkSessionHelper.getSessionHelper(req, resp);
    if(!sp.isLogin()) {
    	resp.sendRedirect(req.getContextPath() + "/" +  sp.getWebPageLogin());
    	return;
    }
    // Set the header user profile dynamically
    UserProfile profile = new UserProfile(
        sp.getVcode() /* "Stebin Ben" */,                    // put the real user name here
        "/larva/assets/images/user/avatar-2.jpg"  // or whatever avatar path you want
    );
    // ----- Metrics (stubbed) -----
    DashboardMetrics m = new DashboardMetrics();
    m.setTotalPageViews(442236);
    m.setPageViewsChangePct(59.3);
    m.setTotalUsers(78250);
    m.setUsersChangePct(70.5);
    m.setTotalOrders(18800);
    m.setOrdersChangePct(-27.4);
    m.setTotalSales(new BigDecimal("35078"));
    m.setSalesChangePct(-27.4);
    m.setIncomeThisWeek(new BigDecimal("7650"));
    m.setSalesThisWeek(new BigDecimal("7650"));

    // ----- Recent Orders -----
    List<OrderRow> recentOrders = Arrays.asList(
        new OrderRow("84564564","Camera Lens",40,"Rejected", new BigDecimal("40570")),
        new OrderRow("84564564","Laptop",300,"Pending",  new BigDecimal("180139")),
        new OrderRow("84564564","Mobile",355,"Approved", new BigDecimal("180139")),
        new OrderRow("84564564","Camera Lens",40,"Rejected", new BigDecimal("40570")),
        new OrderRow("84564564","Laptop",300,"Pending",  new BigDecimal("180139")),
        new OrderRow("84564564","Mobile",355,"Approved", new BigDecimal("180139")),
        new OrderRow("84564564","Camera Lens",40,"Rejected", new BigDecimal("40570")),
        new OrderRow("84564564","Laptop",300,"Pending",  new BigDecimal("180139")),
        new OrderRow("84564564","Mobile",355,"Approved", new BigDecimal("180139")),
        new OrderRow("84564564","Mobile",355,"Approved", new BigDecimal("180139"))
    );
    
//    List <MenuNode> menu = null;
    List <MenuNode> menu = (List<MenuNode>) sp.getSessionData("sideMenuNode");
    if(menu == null) {
    	menu = SessionUtil.generateSideMenu(menu,"Application","ti ti-dashboard","winecavescp",/* req.getContextPath() +  */ "/larva/dashboard" ,sp, /* sp.getRootMenu()  "menu_main.html" */ "menu_main.html");
    	menu = SessionUtil.generateSideMenu(menu,null,"ti ti-dashboard","winecaveold",/* req.getContextPath() +  */ "/larva/dashboard" ,sp, /* sp.getRootMenu()  "menu_main.html" */ "oldwc_main.html");
//      menu = SessionUtil.generateSideMenu(menu,"Application","ti ti-dashboard","erpv4winecave",/* req.getContextPath() +  */ "/larva/dashboard" ,sp, /* sp.getRootMenu()  "menu_main.html" */ "menu_main.html");
//      menu = SessionUtil.generateSideMenu(menu,null,"ti ti-dashboard","winecavedevold",/* req.getContextPath() +  */ "/larva/dashboard" ,sp, /* sp.getRootMenu()  "menu_main.html" */ "oldwc_main.html");
        menu = SessionUtil.generateSideMenu(menu,"Administration","ti ti-dashboard","",/* req.getContextPath() + */ "/larva/dashboard",sp, /* sp.getRootMenu()  "menu_main.html" */ "bicore_main.html");
        if(sp.isAdminUser() || sp.hasAccessRight("#saleorsync")) {
        	menu.add(new MenuNode("SalerSync",    "/larva/dashboard?showSetupScreen=true",    "ti ti-lock"));
        }
         sp.putSessionData("sideMenuNode",menu);
    }
    req.setAttribute("sideMenu", menu);
    
    // ----- Analytics list (label, value) -----
    List<String[]> analyticsReport = Arrays.asList(
        new String[]{"Company Finance Growth", "+45.14%"},
        new String[]{"Company Expenses Ratio", "0.58%"},
        new String[]{"Business Risk Cases", "Low"}
    );

    // ----- Transactions -----
    List<TransactionRow> transactions = Arrays.asList(
        new TransactionRow("#002434", "Today, 2:00 AM",   new BigDecimal("1430"),  "78%", "ti ti-gift",            "success"),
        new TransactionRow("#984947", "5 August, 1:45 PM",new BigDecimal("-302"),  "8%",  "ti ti-message-circle",  "primary"),
        new TransactionRow("#988784", "7 hours ago",      new BigDecimal("-682"),  "16%", "ti ti-settings",        "danger")
    );

    // ----- Feature toggles (override via query string) -----
    req.setAttribute("showSidebar",            getBool(req, "showSidebar", true));
    req.setAttribute("showHeader",             getBool(req, "showHeader", true));
    req.setAttribute("showFooter",             getBool(req, "showFooter", false));
    req.setAttribute("showStatsTiles",         getBool(req, "showStatsTiles", false));
    req.setAttribute("showUniqueVisitor",      getBool(req, "showUniqueVisitor", false));
    req.setAttribute("showIncomeOverview",     getBool(req, "showIncomeOverview", false));
    req.setAttribute("showRecentOrders",       getBool(req, "showRecentOrders", false));
    req.setAttribute("showAnalyticsReport",    getBool(req, "showAnalyticsReport", false));
    req.setAttribute("showSalesReport",        getBool(req, "showSalesReport", false));
    req.setAttribute("showTransactionHistory", getBool(req, "showTransactionHistory", false));
    req.setAttribute("showPageHeader", 		   getBool(req, "showPageHeader", false));
    req.setAttribute("showListView", 		   getBool(req, "showListView", false));
    String iframeUrl = req.getParameter("iframeUrl");
    if(!StringUtils.isBlank(iframeUrl)) {
// req.setAttribute("iFrameUrl", "http://192.168.1.204:8080/pmsdemo/vincero_compound_result.html?action=run");
// String iframeUrl = "http://192.168.1.204:8080/pmsdemo/zkbiloader.html?action=browse&viewid=erpv4.Stock&page_id=Stock_01&zul=zkbiloader.zul&composer=erpv4.ZkBiComposerStock&sidemenu=N";
    	String agent = req.getParameter("agent");
    	String fullPathUrl = null;
    	if(!StringUtils.isBlank(agent)) {
//    		iframeUrl = "http://192.168.1.204:8080/pmsdemo/zkbiloader.html?action=browse&viewid=erpv4.Stock&page_id=Stock_01&zul=zkbiloader.zul&composer=erpv4.ZkBiComposerStock";
    		Map<String,String> pm = new HashMap<String,String>();
//   		pm.put("sidemenu", "N");
    		/*
    		if("winecavescp".equals(agent)) fullPathUrl = createIframeUrlWithPassport("192.168.33.3",5102,iframeUrl, sp.getLoginId(),pm);
    		if("erpv4winecave".equals(agent)) fullPathUrl = createIframeUrlWithPassport("192.168.1.204",5102,iframeUrl, sp.getLoginId(),pm);
    		if("winecaveold".equals(agent)) fullPathUrl = createIframeUrlWithPassport("192.168.33.3",5101,iframeUrl, sp.getLoginId(),pm);
    		if("winecavedevold".equals(agent))  fullPathUrl = createIframeUrlWithPassport("192.168.1.204",5101,iframeUrl, sp.getLoginId(),pm);
    		*/
    		String agentRpcHost=BiConfig.getString(sp, "AgentRpcHost_"+agent);
    		String agentRpcPort=BiConfig.getString(sp, "AgentRpcPort_"+agent);
   			fullPathUrl = createIframeUrlWithPassport(agentRpcHost,Integer.parseInt(agentRpcPort),iframeUrl, sp.getLoginId(),pm);
    	} else {
    		fullPathUrl="/"+iframeUrl;
    	}
    	if(fullPathUrl != null) {
    		req.setAttribute("showIframe", 		   	   getBool(req, "showIframe", true));
    		req.setAttribute("iFrameUrl", fullPathUrl);
    	} else {
    		req.setAttribute("showIframe", 		   	   getBool(req, "showIframe", false));
    	}
    } else {
    	req.setAttribute("showIframe", 		   	   getBool(req, "showIframe", false));
    }
    req.setAttribute("showCalendar", 		   getBool(req, "showCalendar", false));
    req.setAttribute("showSetupScreen", 	   getBool(req, "showSetupScreen", false));
    req.setAttribute("showDashboard", 		   getBool(req, "showDashboard", true));

    // ----- Attributes for JSP -----
    req.setAttribute("pageTitle", "Home");
    req.setAttribute("metrics", m);
    req.setAttribute("recentOrders", recentOrders);
    req.setAttribute("analyticsReport", analyticsReport);
    req.setAttribute("transactions", transactions);

    // Chart series as JSON strings (read in JSP through data-series attributes)
    req.setAttribute("visitorChartWeekJson",  "[10,18,12,25,30,22,28]");
    req.setAttribute("visitorChartMonthJson", "[120,150,110,180,210,160,175,190,220,205,230,240]");
    req.setAttribute("incomeOverviewJson",    "[5,12,9,14,18,16,22]");
    req.setAttribute("salesReportJson",       "[10,14,13,18,19,17,21]");
    req.setAttribute("analyticsReportJson",   "[12,15,11,13,17]");

    // Optional: let you override assets base by ?larvaAssets=/my/custom/path
    String larvaAssets = req.getParameter("larvaAssets");
    if (larvaAssets != null && !larvaAssets.isEmpty()) {
      req.setAttribute("larvaAssets", larvaAssets);
    } else {
      req.setAttribute("larvaAssets", "/larva/assets");
    }

    
    req.setAttribute("profile", profile);
    req.setAttribute("logoutPath", "/logout.html");
    RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/views/larva/dashboard/index.jsp");
    rd.forward(req, resp);
  }

  private boolean getBool(HttpServletRequest req, String name, boolean defVal) {
    String v = req.getParameter(name);
    if (v == null) return defVal;
    return "true".equalsIgnoreCase(v) || "1".equals(v) || "yes".equalsIgnoreCase(v);
  }
}
