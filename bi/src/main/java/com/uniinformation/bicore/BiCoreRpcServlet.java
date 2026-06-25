package com.uniinformation.bicore;

import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.RpcServerConnection;
import com.uniinformation.rpccall.RpcServlet;
import com.uniinformation.utils.UniLog;
import com.kyoko.common.*;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.SessionPassport;
import com.uniinformation.webcore.WebRpcServer;
import com.uniinformation.webcore.ZkSessionHelper;

public class BiCoreRpcServlet implements RpcServlet{
	
	public interface BiRpcInterface {
		public String biRpcCallSegment(String p_segName,String p_jsonstr);
	};

	SessionHelper sh;
	Hashtable<String,BiResult> resultHash;
	String agentid;
	String loginid;
	@Override
	public void init_servlet() {
		resultHash = null;
		sh = null;
		agentid=null;
		loginid=null;
		// TODO Auto-generated method stub
	}

	@Override
	public void close_servlet() {
		// TODO Auto-generated method stub
		resultHash = null;
		sh = null;
		agentid=null;
		loginid=null;
	}

	@Override
	public void setConnection(RpcServerConnection conn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String ping() {
		return "OK  BiCoreRpcServlet Version 1.0";
	}

	public String login(String p_agent, String p_loginid, String p_password) {
		if( sh != null && p_agent.equals(agentid) && p_loginid.equals(loginid)) {
			return("OK");
		}
		sh = ZkSessionHelper.getSessionHelperDummy(p_agent, p_loginid, WebRpcServer.getSessionHelper().getSvc());
		if(sh != null) {
			loginid = p_loginid;
			agentid = p_agent;
			resultHash=new Hashtable<String,BiResult>();
			return("OK");
		} else {
			return("FAIL");
		}
	}
	public String view(String p_viewName, String p_aliaseName) {
		BiResult br = null;
		if(sh == null) return("FAILNo Login");
		br = resultHash.get(p_aliaseName);
		if(br != null) {
			if(!br.getView().getName().equals(p_viewName)) {
				resultHash.remove(p_aliaseName);
				br = null;
			}
		} 
		if(br == null) {
			br = sh.newBiResult(p_viewName);
			if(br == null) return("FAILCannot Create View");
		}
		resultHash.put(p_aliaseName, br);
		return("OK");
	}
	public String query(String p_aliaseName, String p_condition) {
		if(sh == null) return("FAILNo Login");
		BiResult br = null;
		if(resultHash == null || (br = resultHash.get(p_aliaseName)) == null) {
			return("FAILView Not Exist");
		}
		br.clear();
		br.clearCondition();
		if(!StringUtils.isBlank(p_condition)) br.addCustomCondition(p_condition);
		br.query();
		return(String.format("OK  %10d",br.getRowCount()));
	}
	public String rollbackWork(String p_aliaseName) {
		if(sh == null) return("FAILNo Login");
		BiResult br = null;
		if(resultHash == null || (br = resultHash.get(p_aliaseName)) == null) return("FAILView Not Exist");
		try {
			br.rollbackWork();
		} catch (Exception ex) {
			CoreLog.log(ex);
			return("FAIL");
		}
		return("OK");
	}
	public String commitWork(String p_aliaseName) {
		if(sh == null) return("FAILNo Login");
		BiResult br = null;
		if((br = resultHash.get(p_aliaseName)) == null) return("FAILView Not Exist");
		try {
			br.commitWork();
		} catch (Exception ex) {
			CoreLog.log(ex);
			return("FAIL");
		}
		return("OK");
	}
	public String load(String p_aliaseName,int start,int len) {
		if(sh == null) return("FAILNo Login");
		BiResult br = null;
		if((br = resultHash.get(p_aliaseName)) == null) return("FAILName Not Exist");
		br.clearCurrentRec();
		JSONArray ja = new JSONArray();
		int end = start+len;
		UniLog.log("fetch from "+start+" to "+end);
		try {
			for(int i=start;i<end;i++) {
				br.loadOneRecV(i);
				ja.put( BiCellCollectionToJsonInterface.BiCellCollectionToJSON(br.getCurrentCollection()));
				UniLog.log("fetch record " + i);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return("FAIL"+ex.toString());
		}
		return("OK  "+ja.toString());
	}
	public String fetch(String p_aliaseName,int start,int len) {
		if(sh == null) return("FAILNo Login");
		BiResult br = null;
		if((br = resultHash.get(p_aliaseName)) == null) return("FAILName Not Exist");
		JSONArray ja = new JSONArray();
		int end = start+len;
		UniLog.log("fetch from "+start+" to "+end);
		try {
			for(int i=start;i<end;i++) {
				br.fetchOneRecV(i);
				ja.put( BiCellCollectionToJsonInterface.BiCellCollectionToJSON(br.getCurrentCollection()));
				UniLog.log("fetch record " + i);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return("FAIL"+ex.toString());
		}
		return("OK  "+ja.toString());
	}
	public String beginWork(String p_aliaseName) {
		if(sh == null) return("FAILNo Login");
		BiResult br = null;
		if((br = resultHash.get(p_aliaseName)) == null) return("FAILName Not Exist");
		try {
			br.beginWork();
		} catch (Exception ex) {
			CoreLog.log(ex);
			return("FAIL");
		}
		return("OK");
	}
	public String update(String p_aliaseName, int p_row,String p_jsonstr) {
		if(sh == null) return("FAILNo Login");
		BiResult br = null;
		if((br = resultHash.get(p_aliaseName)) == null) {
			return("FAILView Not Exist");
		}
		br.loadOneRecV(p_row);
		br.fetchOneRecV(p_row);
		try {
			JSONObject jo = new JSONObject(p_jsonstr);
			JsonToBiCellCollectionInterface.JsonToBiCellCollection(br.getCurrentCollection(), jo, null);
		} catch (Exception ex ) {
			CoreLog.log(ex);
			return("FAIL"+ex.toString());
		}
		ReturnMsg rtn = br.updateCurrent();
		if(rtn == null || rtn.getStatus()) return("OK");
		return("FAIL"+(rtn == null ? "" :rtn.toString()));
	}
	public String call(String p_aliaseName, String p_segName,String p_jsonstr) {
		if(sh == null) return("FAILNo Login");
		BiResult br = null;
		if((br = resultHash.get(p_aliaseName)) == null) {
			return("FAILView Not Exist");
		}
		if(br instanceof BiRpcInterface) {
			String rtn = ((BiRpcInterface) br).biRpcCallSegment(p_segName, p_jsonstr);
			if(rtn != null) return(rtn);
		}
		return("FAILSegment Not Exist");
	}
	
	/*
	public String makePassPort( String p_user,int p_timeToExpire,String p_peerAllowed,String p_urlAllowed,String p_OneTimeOnly,String p_content)  {
		try {
			String pk = SessionPassPort.makePassPort( p_user,(long) p_timeToExpire,p_peerAllowed,p_urlAllowed,"Y".equals(p_OneTimeOnly),p_content);
			if(pk != null ) return("OK  "+pk); else return("FAIL");
		} catch (Exception ex) {
			UniLog.log("FAIL"+ex.toString());
			return("FAIL Failed with exception");
		}
	}*/
	public String makePassPort( String p_user,int p_timeToExpire,String p_peerAllowed,String p_urlAllowed,boolean p_OneTimeOnly,String p_content)  {
		try {
			String pk = SessionPassport.makePassport( p_user,(long) p_timeToExpire,p_peerAllowed,p_urlAllowed,p_OneTimeOnly,p_content);
			if(pk != null ) return("OK  "+pk); else return("FAIL");
		} catch (Exception ex) {
			CoreLog.log("FAIL"+ex.toString());
			return("FAIL Failed with exception");
		}
	}
	static public RpcClient getBiCoreRpcClient(SessionHelper p_sp) {
		BiCoreRpcServlet biRpc = new BiCoreRpcServlet();
		biRpc.sh = p_sp;
		biRpc.loginid = p_sp.getLoginId();
		biRpc.agentid = p_sp.getAgent();
		biRpc.resultHash=new Hashtable<String,BiResult>();
		RpcClient rpc = p_sp.getRpcClient();
		rpc.setRpcServlet(biRpc.getClass().getName(), biRpc);
		return(rpc);
	}
	public String getViewList( String p_loginid , String p_password, String p_agent) {
		try {
			sh = ZkSessionHelper.getSessionHelperDummy(p_agent, p_loginid, null);
			JSONArray ja = new BiCoreRpcApi().getViewList(sh);
			if(ja != null) {
				return("OK  "+ja.toString());
			} else {
				return("FAIL Failed with null");
			}
				/*
			List<BiView> vList = sh.getBiSchema().getAllView();
			JSONArray ja = new JSONArray();
			for(BiView bv : vList) {
				ja.put(bv.getName());
			}
			return("OK  "+ja.toString());
			*/
		} catch (Exception ex) {
			CoreLog.log("FAIL"+ex.toString());
			return("FAIL Failed with exception");
		}
	}
	public String getViewListColumns( String p_loginid , String p_password, String p_agent, String p_viewName) {
		try {
			sh = ZkSessionHelper.getSessionHelperDummy(p_agent, p_loginid, null);
			/*
			BiView bv = sh.getBiSchema().getViewByName(p_viewName);
			BiResult br = bv.newBiResult(p_loginid, null, null, sh);
			JSONArray ja = new JSONArray();
			for(BiColumn bc : br.getListColumns()) {
				JSONObject jo = new JSONObject();
				jo.put("id", bc.getLabel());
				jo.put("header", bc.getEngName());
				jo.put("type", bc.getColumnType());
				jo.put("format", bc.getFormat());
				ja.put(jo);
			}
			JSONObject jo = new JSONObject();
			jo.put("name",p_viewName);
			jo.put("fields",ja);
			*/
			JSONObject jo = new BiCoreRpcApi().getViewListColumns(sh, p_viewName);
			if(jo != null) {
				return("OK  "+jo.toString());
			} else {
				return("FAIL");
			}
		} catch (Exception ex) {
			CoreLog.log("FAIL"+ex.toString());
			return("FAIL Failed with exception");
		}
	}
}
