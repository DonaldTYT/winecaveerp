package com.kyoko.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import com.kyoko.common.ByteStream;
import com.kyoko.common.MpxStreamListener;
import com.kyoko.common.SocketByteStream;
import com.kyoko.common.SocketStreamListener;
import com.kyoko.common.StreamMultiplexer;
import com.kyoko.common.StreamPipe;
import com.kyoko.common.CoreLog;
import com.kyoko.rpccall.RpcClient;
import com.kyoko.rpccall.RpcConnection;
import com.kyoko.rpccall.Value;

public class ProxyClient implements Runnable{
	ProxyServer server;
	String connectIp;
	int connectPort;
	StreamMultiplexer mpx;
	int nextChannel = 2;
	private HashSet<String>pendingRedir;
	
	void addPendingRedir(String p_ip,int p_port) {
		String key = p_ip+":"+p_port;
		synchronized(pendingRedir) {
			pendingRedir.add(key);
		}
	}
	void delPendingRedir(String p_ip,int p_port) {
		String key = p_ip+":"+p_port;
		synchronized(pendingRedir) {
			pendingRedir.remove(key);
		}
	}

	public ProxyClient(ProxyServer p_server,String p_connectIp,int p_connectPort) {
		server = p_server;
		connectIp = p_connectIp;
		connectPort = p_connectPort;
		pendingRedir = new HashSet<String>();
	}
	public void poll(RpcConnection p_conn) throws Exception {
		for(;;) {
			String nextkey=null;
			if(pendingRedir.isEmpty()) break;
			synchronized(pendingRedir) {
				for(String key:pendingRedir) {
					nextkey = key;
					pendingRedir.remove(key);
					break;
				}
			}
			if(nextkey != null) {
				int idx = nextkey.indexOf(":");
				String ip = nextkey.substring(0,idx);
				int port = Integer.parseInt(nextkey.substring(idx+1));
				CoreLog.log("set Stream Redir ip " + ip + " port " + port + " channed " + nextChannel);
				server.addRedirect(this,nextChannel,ip,port);
				Vector arglist = new Vector();
				arglist.add(ip);
				arglist.add(port);
				arglist.add(nextChannel);
				Value v = p_conn.callSegment("com.kyoko.proxy.ProxyServerRpc.addForward", arglist);
				if(v == null || !v.toString().startsWith("OK")) {
					throw new Exception("Add forward to remote failed");
				}
				nextChannel++;
			}
		}
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		CoreLog.log("Proxy Client");
		try {
			RpcClient rpc = new RpcClient(connectIp,connectPort);
			ProxyClientRpc crpc = new ProxyClientRpc(this);
			rpc.open();
			rpc.setRpcServlet( crpc.getClass().getName(), crpc);
			Value v;
			for(int i=0;i<5;i++) {
				CoreLog.log("test call " + i);
				v = rpc.callSegment("com.kyoko.proxy.ProxyServerRpc.ping");
				CoreLog.log(v.toString());
				Thread.sleep(1000);
			}
			v = rpc.callSegment("com.kyoko.proxy.ProxyServerRpc.enableMultiPlexer");
			CoreLog.log(v.toString());
			if(v.toString().startsWith("OK")) {
				ByteStream bs = rpc.getConnection().getByteStream();
				mpx = new StreamMultiplexer(bs);
				mpx.start();
				ByteStream bs2 = mpx.connect(1);
				CoreLog.log("do rpc call on mpx channel 1 "+ bs2);
				rpc.getConnection().setByteStream(bs2);
				for(int i=0;i<5;i++) {
					CoreLog.log("test call " + i);
					v = rpc.callSegment("com.kyoko.proxy.ProxyServerRpc.ping");
					CoreLog.log(v.toString());
					Thread.sleep(1000);
				}
				v = rpc.callSegment("com.kyoko.proxy.ProxyServerRpc.startForward");
				/*
				Vector params = new Vector();
				addOneMpxListener(mpx,30101,"localhost",6002,params);
				v = rpc.callSegment("com.kyoko.proxy.ProxyServerRpc.startForward",params);
				*/
			}
			
			
		} catch (Exception sex) {
			CoreLog.log(sex);
		}
		
	}
	/*
	static void addOneMpxListener(StreamMultiplexer p_mpx,int p_channel,String p_toIp,int p_toPort,Vector params) throws Exception {
			ForwardRec fwr  = new ForwardRec(p_mpx,p_channel);
			fwr.toIp = p_toIp;
			fwr.toPort = p_toPort;
			fwr.start();
			params.add(p_channel);
			params.add(p_toIp);
			params.add(p_toPort);
	}
	*/
	public static void main(String args[]){
		try {
			ProxyServer svr = new ProxyServer(null,0);
			ProxyClient pc = new ProxyClient(svr,"192.168.1.208",30001);
			pc.addPendingRedir("210.3.102.2",6002);
			pc.run();
		} catch (IOException ex) {
			CoreLog.log(ex);
		}
	}

}
