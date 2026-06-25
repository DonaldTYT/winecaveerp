package com.uniinformation.erpv4;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.json.JSONArray;
import org.json.JSONObject;

import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cron.CronJob;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.TimedRpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtil.FilingUtilPropObj;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public abstract class RecSync extends CronJob {
	static final int RECSYNC_STATE_ADD = 1;
	static final int RECSYNC_STATE_UPDATE = 2;
	static final int RECSYNC_STATE_DELETE = 3;
	static final int MIN_REC_SYNC_INTERVAL = 10000;
	static Hashtable<String,RecSync> agentHash = new Hashtable<String,RecSync>();
	static RecSyncRpc rpcServer;
	String agent;
	protected SessionHelper sessionHelper;
	int recSyncInterval = MIN_REC_SYNC_INTERVAL;
	
	
	class RecSyncHost {
		String addr;
		int port;
		RpcClient trpc;
		Hashtable<String,Object> cacheHash;
	}
	public abstract class SyncHandler {
		abstract protected String getDestViewId();
		abstract protected String getKey(CellCollection p_bicol);
		abstract protected ReturnMsg syncRec(CellCollection p_bicol,JSONObject p_jo);
		abstract protected ReturnMsg updateRec(CellCollection p_bicol) throws Exception;
		abstract protected String getRemoteHosts(CellCollection p_bicol);
		abstract protected String srcToDestColumn(String p_srcCol);
		protected ReturnMsg afterSync(String p_jsonStr) {
			return(ReturnMsg.defaultOk);
		}
	}
	Hashtable<String,SyncHandler> viewHash= new Hashtable<String,SyncHandler>();
	Hashtable<String,RecSyncHost> serverHash = new Hashtable<String,RecSyncHost>();

	@Override
	public int runOnce() throws Exception {
		// TODO Auto-generated method stub
//		viewHash = new Hashtable<String,SyncHandler>();
		
		/*
		if(rpc == null) {
			String recsyncPort  = Erpv4Config.getString(sessionHelper, "RecSyncPort");
			int p = 6733;
			if(recsyncPort != null) p = Integer.parseInt(recsyncPort);
			rpc = new RecSyncRpc(p);
			Thread servthread = new Thread(rpc);
			//		servthread.setDaemon(true);
			servthread.start();
		}
		*/
		for(;;) {
			UniLog.log1("RecSync Wakeup (interval:%d)",recSyncInterval);
			syncToRemote();
			synchronized(this) {
				wait(recSyncInterval);
			}
		}
	}

	public SessionHelper getSessionHelper() {
		return(sessionHelper);
	}
	@Override
	public void setSessionHelper(SessionHelper p_sh) throws Exception {
		sessionHelper = p_sh;
		agent = sessionHelper.getAgent();
		String recsyncHosts = BiConfig.getString(sessionHelper, "RecSyncHosts");
		recSyncInterval = BiConfig.getInteger(sessionHelper, "RecSyncInterval", recSyncInterval);
		if (recSyncInterval < MIN_REC_SYNC_INTERVAL) {
			recSyncInterval = MIN_REC_SYNC_INTERVAL;
		}
		if(recsyncHosts != null) {
			for (StringTokenizer token = new StringTokenizer(recsyncHosts,",");
				  token.hasMoreTokens();) {
				String s = token.nextToken();
				int cc;
				cc = s.indexOf("@");
				RecSyncHost rh = new RecSyncHost();
				if(cc >= 0) {
					String agent = s.substring(0,cc);
					int c2 = s.indexOf(":");
					if( c2 >= 0) {
						rh.addr = s.substring(cc+1,c2);
						rh.port = Integer.parseInt(s.substring(c2+1));
						rh.trpc = new TimedRpcClient(rh.addr,rh.port);
						rh.cacheHash = new Hashtable<String,Object>();
						serverHash.put(agent, rh);
					}
				}
			}
		}
		putRecSync(agent,this);
		if(rpcServer == null) {
			String recsyncPort  = BiConfig.getString(sessionHelper, "RecSyncPort");
			int p = 6733;
			if(recsyncPort != null) p = Integer.parseInt(recsyncPort);
			rpcServer = new RecSyncRpc(p);
			Thread servthread = new Thread(rpcServer);
			//		servthread.setDaemon(true);
			servthread.start();
		}
	}
	static public synchronized void putRecSync(String p_agent,RecSync recsync) {
		agentHash.put(p_agent,recsync);
	}	
	static public synchronized RecSync getRecSync(String p_agent) {
		return(agentHash.get(p_agent));
	}
	
	ReturnMsg syncToRemote() {
		Connection conn = null;
		try {
			FilingUtilPropObj fprop = FilingUtil.loadProp(agent);
			conn = fprop.jdbcPool.getConnection();
			String selectRowSQL = String.format("SELECT serial_id,rsync_agent, rsync_view, rsync_host, rsync_key, rsync_time, rsync_status, rsync_data FROM recsync WHERE rsync_agent = ? order by rsync_view,rsync_time");
			PreparedStatement pstmt = conn.prepareStatement(selectRowSQL);
            pstmt.setString(1, agent);
			String deleteSQL = String.format("DELETE from recsync where  serial_id=?");
			PreparedStatement delstmt = conn.prepareStatement(deleteSQL);
			String updateSQL = String.format("UPDATE recsync set rsync_host = ? , rsync_status = ? where  serial_id=?");
			PreparedStatement updstmt = conn.prepareStatement(updateSQL);
			ResultSet rs = pstmt.executeQuery();	
			RpcClient rpc;
			while (rs.next()) {
				switch(rs.getInt(7)) {
				case RECSYNC_STATE_UPDATE:	
					SyncHandler shdr = viewHash.get(rs.getString(3));
					if(shdr == null) {
						UniLog.log("recsync " + agent + rs.getString(3) + " key = " + rs.getString(4) + " failed  shdr == null");
						continue;
					}
					UniLog.log("recsync " + agent + rs.getString(3) + " key = " + rs.getString(4));
					String viewid = shdr.getDestViewId();
					String key = rs.getString(5);
					byte[] jsonBytes = rs.getBytes(8);
					String jsonStr = new String(jsonBytes,"UTF-8");
					
					JSONArray shosts = new JSONArray(rs.getString(4));
					JSONArray rhosts = new JSONArray();
					for(int i=0;i<shosts.length();i++) {
						String ag = shosts.getString(i);
						RecSyncHost rh = serverHash.get(ag);
						if(rh == null) {
							rhosts.put(ag);
							UniLog.log("recsync " + ag + " not configured, do later ");
							continue;
						}
						rpc = new RpcClient(rh.addr,rh.port);
						rpc.open();
						Value v = null;
						if(jsonStr.length() > 30000) {
							File f = File.createTempFile("prefix",null);
						    BufferedWriter writer = new BufferedWriter(new FileWriter(f));
						    writer.write(jsonStr);
						    writer.close();
						    v = rpc.callSegment("com.uniinformation.erpv4.RecSyncRpcServlet.syncOneRecordByFile", 
								new VectorUtil()
									.addElement(ag)
									.addElement(viewid)
									.addElement(key)
									.addElement(f.getPath())
									.toVector()
							);
						    f.delete();
						} else {
						v = rpc.callSegment("com.uniinformation.erpv4.RecSyncRpcServlet.syncOneRecord", 
								new VectorUtil()
									.addElement(ag)
									.addElement(viewid)
									.addElement(key)
									.addElement(jsonStr)
									.toVector()
							);
						}
						rpc.close();
						if(v == null || !v.toString().startsWith("OK")) {
							if(v != null) {
								UniLog.log("recsync " + ag + " incomplete , do later reason ("+v.toString()+")");
							} else {
								UniLog.log("recsync " + ag + " incomplete , do later reason (null)");
							}
							rhosts.put(ag);
						}
					}
					/*
					String destAgent = "afsdev";
					rpc = new RpcClient("192.168.17.204",6733);
					rpc.open();
					Value v = rpc.callSegment("com.uniinformation.erpv4.RecSyncRpcServlet.syncOneRecord", 
								new VectorUtil()
									.addElement(destAgent)
									.addElement(viewid)
									.addElement(key)
									.addElement(jsonStr)
									.toVector()
							);
					if(v != null && v.toString().startsWith("OK")) {
						delstmt.setInt(1, rs.getInt(1));
						delstmt.executeUpdate();
					}
					*/
					ReturnMsg rtn = null;
					if(rhosts.length() <= 0) {
						rtn = shdr.afterSync(jsonStr);
						if(!rtn.getStatus()) UniLog.log("recsync " + " post-processing failed, do later " + rtn.getMsg());
					};
						
					if(rtn != null && rtn.getStatus()) {
						delstmt.setInt(1, rs.getInt(1));
						delstmt.executeUpdate();
					} else {
						updstmt.setString(1, rhosts.toString());
						updstmt.setInt(2, rs.getInt(7));
						updstmt.setInt(3, rs.getInt(1));
						updstmt.executeUpdate();
					}
					break;
				}
			}
            conn.close();
            return(ReturnMsg.defaultOk);
		} catch (Exception ex) {
			//UniLog.log(ex);
			UniLog.log("error:"+ex.getMessage());
			try {
				if(conn != null) conn.close();
			}  catch (Exception exx) {
				UniLog.log(exx);
			}
            return(ReturnMsg.defaultFail);
		}
	}
	ReturnMsg updateOneRecord(String p_view,CellCollection p_bicol) {
		SyncHandler shdr = viewHash.get(p_view);
		Connection conn = null;
		if(shdr == null) return(ReturnMsg.defaultFail);
		try {
			ReturnMsg rtn = shdr.updateRec(p_bicol);
			if(rtn == null || !rtn.getStatus()) return(rtn); // !! rtn == null treat as ignored, previous ReturnMsg default to ok if return null
			FilingUtilPropObj fprop = FilingUtil.loadProp(agent);
			conn = fprop.jdbcPool.getConnection();
			String insertSQL = String.format("INSERT INTO recsync(rsync_agent, rsync_view, rsync_host, rsync_key, rsync_time, rsync_status, rsync_data) VALUES(?,?,?,?,?,?,?)");
			PreparedStatement pstmt = conn.prepareStatement(insertSQL);
            pstmt.setString(1, agent);
            pstmt.setString(2, p_view);
            pstmt.setString(3, shdr.getRemoteHosts(p_bicol));
            pstmt.setString(4, shdr.getKey(p_bicol));
            pstmt.setTimestamp(5, new Timestamp(new Date().getTime()));
            pstmt.setInt(6, RECSYNC_STATE_UPDATE);
            pstmt.setBinaryStream(7, new ByteArrayInputStream(((String) rtn.getData()).getBytes("UTF-8")));
            pstmt.executeUpdate();
            conn.close();
            synchronized(this) {
            	notifyAll();
            }
            return(ReturnMsg.defaultOk);
		} catch (Exception ex) {
			UniLog.log(ex);
			try {
				if(conn != null) conn.close();
			}  catch (Exception exx) {
				UniLog.log(exx);
			}
            return(ReturnMsg.defaultFail);
		}
	}
	
	protected ReturnMsg addOneView(String p_view,SyncHandler p_handler) {
		synchronized(viewHash){ 
			viewHash.put(p_view, p_handler);
		}
		return(ReturnMsg.defaultOk);
	}
	
	protected void joToCell(JSONObject p_jo,CellCollection p_bicol,String p_name)  throws Exception {
		if(p_jo.opt(p_name) != null) {
			java.util.Date dd;
			String ss = null;
			switch(p_bicol.getCell(p_name).getType()) {
			case Cell.VTYPE_DOUBLE:
				p_bicol.getCell(p_name).set(p_jo.getDouble(p_name));
				break;
			case Cell.VTYPE_DATETIME:
				int ut = p_jo.getInt(p_name);
//				dd = DateUtil.unixtimeToDate(ut);
//				p_bicol.getCell(p_name).set(dd);
				p_bicol.getCell(p_name).set(ut);
				break;
			case Cell.VTYPE_DATE:
				ss = p_jo.getString(p_name);
				dd = DateUtil.dateTimeStrToDate(ss);
				p_bicol.getCell(p_name).set(dd);
				break;
			case Cell.VTYPE_BOOLEAN:
				Object oj = p_jo.get(p_name);
				if(oj instanceof String) {
					ss = (String ) oj;
					if(ss.equals("Y")) {
						p_bicol.getCell(p_name).set(true);
					} else {
						p_bicol.getCell(p_name).set(false);
					}
				} else if(oj instanceof Boolean ) {
					p_bicol.getCell(p_name).set((Boolean) oj);
				} else {
					p_bicol.getCell(p_name).set(false);
				}
				break;
			case Cell.VTYPE_INT:
				p_bicol.getCell(p_name).set(p_jo.getInt(p_name));
				break;
			default:
				p_bicol.getCell(p_name).set(p_jo.getString(p_name));
				break;
			}
		}
	}
	
	abstract protected ReturnMsg syncOneRecord(String p_view,String p_key,String p_jsonDetail);
	
	public void clearRemoteHash() {
		for(RecSyncHost rh: serverHash.values()) {
			rh.cacheHash.clear();
		}
	}

	public static boolean needSync(String p_agent,String p_view) {
		RecSync rs = agentHash.get(p_agent);
		if(rs == null) return(false);
		SyncHandler sh = rs.viewHash.get(p_view);
		if(sh == null) return(false);
		return(sh.getDestViewId() != null);
	}
	public static ReturnMsg updateOneRecord(String p_agent,String p_view,CellCollection p_bicol) {
		//ReturnMsg rtn = agentHash.get(p_agent).updateOneRecord(p_view, p_bicol);
		//andrew210510 fix recsync null exception
		RecSync rs = agentHash.get(p_agent);
		if (rs == null) return ReturnMsg.defaultFail;
		ReturnMsg rtn = rs.updateOneRecord(p_view, p_bicol);
		return(rtn);
	}
	

	public static RpcClient openRpc(String p_localAgent,String p_remoteAgent,int p_timeout) {
		RecSync rs = agentHash.get(p_localAgent);
		if(rs == null) return(null);
		RecSyncHost rh = rs.serverHash.get(p_remoteAgent);
		if(rh == null) return(null);
		UniLog.log("Recsync.openRpc " + rh.addr + " port " + rh.port);
		RpcClient rpc = new RpcClient(rh.addr,rh.port);
		rpc.open();
		return(rpc);
	}

	public static void addRpcClass(String p_localAgent,String p_class) {
		rpcServer.addRpcClass(p_class);
	}
	
	public static SessionHelper getSessionHelperByAgent(String p_agent) {
		RecSync rs = agentHash.get(p_agent);
		if(rs == null) return(null);
		return(rs.sessionHelper);
	}
	public static RpcClient openTimedRpc(String p_localAgent,String p_remoteAgent,int p_timeout) {
		RecSync rs = agentHash.get(p_localAgent);
		if(rs == null) return(null);
		RecSyncHost rh = rs.serverHash.get(p_remoteAgent);
		if(rh == null) return(null);
		TimedRpcClient rpc = new TimedRpcClient(rh.addr,rh.port);
		return(rpc);
	}

	static public synchronized void clearAllAgentHash() {
		for(RecSync rs : agentHash.values()) {
			rs.clearRemoteHash();
		}
	}	
	
	@Override
	public void start() {
		super.start();
		UniLog.log1("called");
		
	}
	
	@Override
	public void stop() {
		super.stop();
		UniLog.log1("called");
		if (rpcServer != null) {
			UniLog.log1("try to stop rpcserver");
			rpcServer.stop();  //andrew230918 fix cannot restart recsync cronjob
		}
	}
}
