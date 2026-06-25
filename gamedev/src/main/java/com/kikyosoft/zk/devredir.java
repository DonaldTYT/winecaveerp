package com.kikyosoft.zk;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.*;
import javax.servlet.jsp.*;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;

import com.kikyosoft.rpccall.*;
import com.kikyosoft.utils.*;


public class devredir extends SelectorComposer<Component>   {
	@Wire
	private Button enterBtn;

	@Wire
	private Textbox pinBox;

	
//	String params;
	
	static final String BICORERPC_PREFIX = "com.uniinformation.bicore.BiCoreRpcServlet.";
	
	ReturnMsg doRedirWithUrlParam(String p_host,int p_port,String p_url, String p_loginid,Map<String,String[]> p_UrlParams ) {
		return(doRedirWithUrlParam(p_host,p_port,p_url, p_loginid,p_UrlParams,null));
		
	}
	ReturnMsg doRedirWithUrlParam(String p_host,int p_port,String p_url, String p_loginid,Map<String,String[]> p_UrlParams ,String task) {
		RpcClient rpc = new RpcClient(p_host,p_port);
        rpc.open();
        Value v = rpc.callSegment(BICORERPC_PREFIX+"ping");
        LogUtil.log("TestBiCoreRpc Ping got "+v);
        if(v == null || !v.toString().startsWith("OK")) {
        	rpc.close();
        	return(new ReturnMsg(false,"Ping to Server Failed"));
        }
        JSONObject jo = new JSONObject();
        for(String param : p_UrlParams.keySet()) {
        	String[] vals = p_UrlParams.get(param);
        			if(vals != null) {
        				if(vals.length == 1) {
        					jo.put(param,vals[0]);
        				} else {
        					JSONArray ja = new JSONArray();
        					for(int i=0;i<vals.length;i++) {
        						ja.put(vals[i]);
        					}
        					jo.put(param, ja);
        				}
        			}
        		}
        if(task != null) {
        	jo.put("wiptask",task);
        }
        v = rpc.callSegment(BICORERPC_PREFIX+"makePassPort",
    		new VectorUtil()
    			.addElement(p_loginid)
//    			.addElement(60000)
    			.addElement(8640000)
    			.addElement("")
    			.addElement("")
//    			.addElement("Y")
    			.addElement("N")
    			.addElement(jo.toString())
    			.toVector()
    		);		
        LogUtil.log("TestBiCoreRpc makePassPort got "+v);
        if(v == null || !v.toString().startsWith("OK")) {
        	rpc.close();
        	return(new ReturnMsg(false,"Ping to Server Failed"));
        } else {
        	rpc.close();
        	String finalUrl = p_url + (p_url.contains("?")  ? "&" : "?") +"passport="+URLEncoder.encode(v.toString().substring(4),StandardCharsets.UTF_8);
        	Executions.sendRedirect(finalUrl);
        	return(ReturnMsg.defaultOk);
        }
	}
	
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		super.doAfterCompose(arg0);
		Map<String,String[]> newUrlParams = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getParameterMap();
		LogUtil.log("In pin_keypad");
		enterBtn.addEventListener("onClick", new EventListener<Event>() {
        @Override
        public void onEvent(Event event) throws Exception {
        	LogUtil.log("Pin Enter :" + pinBox.getText());
        	ReturnMsg rtn = null;
        	if("9989".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.13",5101,"https://hub.erpv4.com/pmsdemo/mba.jsp","wiptest",newUrlParams);
        	} 
        	if("9988".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.13",5101,"https://hub.erpv4.com/pmsdemo/mba.jsp","wiptest2",newUrlParams);
        	} 
        	if("9987".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.13",5101,"https://hub.erpv4.com/pmsdemo/mba.jsp","hlv",newUrlParams);
        	} 
        	if("4426".equals(pinBox.getText())) {
//        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://hub.erpv4.com/artwayscp/mba.jsp",newUrlParams);
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","hlv",newUrlParams);
        	}
        	if("2977".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","lingcan",newUrlParams);
        	}
        	if("3851".equals(pinBox.getText())) {
//        		rtn = doRedirWithUrlParam("192.1)68.46.102",5101,"https://hub.erpv4.com/artwayscp/mba.jsp",newUrlParams);
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","cheong",newUrlParams);
        	}
        	if("1280".equals(pinBox.getText())) {
//        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://hub.erpv4.com/artwayscp/mba.jsp",newUrlParams);
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","ctpshing",newUrlParams);
        	}
        	if("3094".equals(pinBox.getText())) {
//        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://hub.erpv4.com/artwayscp/mba.jsp",newUrlParams);
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","chung",newUrlParams);
        	}
        	if("7231".equals(pinBox.getText())) {
//        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://hub.erpv4.com/artwayscp/mba.jsp",newUrlParams);
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","him",newUrlParams);
        	}
        	if("8140".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","dgpshing",newUrlParams);
        	}
        	if("8323".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","dgpshing",newUrlParams,"printing");
//        		rtn = doRedirWithUrlParam("192.168.46.13",5101,"https://hub.erpv4.com/pmsdemo/mba.jsp","dgpshing",newUrlParams,"printing");
        	}
        	if("7105".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","volo",newUrlParams);
        	}
        	if("6698".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","wipprt",newUrlParams);
        	}
        	if("6601".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","wipprt1",newUrlParams);
        	}
        	if("6602".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","wipprt2",newUrlParams);
        	}
        	if("6603".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","wipprt3",newUrlParams);
        	}
        	if("6604".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","wipprt4",newUrlParams);
        	}
        	if("6605".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","wipprt5",newUrlParams);
        	}
        	if("1378".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","wipdgp",newUrlParams);
        	}
        	if("5435".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","dgpdee",newUrlParams,"printing");
        	}
        	if("3109".equals(pinBox.getText())) {
        		rtn = doRedirWithUrlParam("192.168.46.102",5101,"https://svr.artway.erpv4.com:21443/artwayscp/mba.jsp","dgptony",newUrlParams,"printing");
        	}
        }
		});
	};

//	private static String appendParams(String baseUrl, HttpServletRequest request) {
//	        // Preserve URL fragment if present
//	        String fragment = "";
//	        int hash = baseUrl.indexOf('#');
//	        if (hash != -1) {
//	            fragment = baseUrl.substring(hash);      // includes '#...'
//	            baseUrl = baseUrl.substring(0, hash);
//	        }
//
//	        StringBuilder sb = new StringBuilder(baseUrl);
//	        boolean hasQuery = baseUrl.contains("?");
//
//	        for (Map.Entry<String, String[]> e : request.getParameterMap().entrySet()) {
//	            String name = e.getKey();
//
//	            // (optional) skip sensitive params:
//	            // if ("password".equalsIgnoreCase(name)) continue;
//
//	            String[] values = e.getValue();
//	            if (values == null || values.length == 0) {
//	                sb.append(hasQuery ? '&' : '?'); hasQuery = true;
//	                sb.append(URLEncoder.encode(name, StandardCharsets.UTF_8));
//	                continue;
//	            }
//	            for (String v : values) {
//	                sb.append(hasQuery ? '&' : '?'); hasQuery = true;
//	                sb.append(URLEncoder.encode(name, StandardCharsets.UTF_8))
//	                  .append('=')
//	                  .append(URLEncoder.encode(v == null ? "" : v, StandardCharsets.UTF_8));
//	            }
//	        }
//
//	        sb.append(fragment);
//	        return sb.toString();
//	    }
//
//		public static void main(String args[]){
//        		RpcClient rpc = new RpcClient("192.168.46.13",5101);
//        		rpc.open();
//        		Value v = rpc.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.ping");
//        		CoreLog.log("TestBiCoreRpc Ping got "+v);
//        		v = rpc.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.makePassPort",
//    					new VectorUtil()
//    						.addElement("hlv")
//    						.addElement(60000)
//    						.addElement("")
//    						.addElement("")
//    						.addElement("Y")
//    						.addElement("ABC")
//    						.toVector()
//    				);		
//        		CoreLog.log("TestBiCoreRpc makePassPort got "+v);
//        }
}
