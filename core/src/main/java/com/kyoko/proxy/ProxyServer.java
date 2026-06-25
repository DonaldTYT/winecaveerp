package com.kyoko.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Hashtable;

import com.kyoko.common.*;
import com.kyoko.rpccall.RpcConnection;

public class ProxyServer extends SocketStreamListener {
	static public final int CONTROL_PORT = 30001;
	class ForwardRec {
		StreamMultiplexer mpx;
		int channel;
		ForwardRec(StreamMultiplexer p_mpx,int p_channel) {
			mpx = p_mpx;
			channel = p_channel;
		}
	}
	class MpxRedirRec extends MpxStreamListener{
		String toIp;
		int toPort;
		ProxyClient client;
		public MpxRedirRec(ProxyClient p_client, int p_channel) throws IOException {
			super(p_client.mpx, p_channel);
			client = p_client;
			// TODO Auto-generated constructor stub
		}
		@Override
		public boolean onStreamAccepted(ByteStream p_bs) throws IOException {
			connectStream(p_bs,toIp,toPort);
			return(true);
		}
	}
	class TcpRedirRec extends SocketStreamListener {
		String toIp;
		int toPort;
		public TcpRedirRec(String p_ip, int p_port) throws IOException {
			super(p_ip, p_port);
			// TODO Auto-generated constructor stub
		}
		@Override
		public boolean onStreamAccepted(ByteStream p_bs) throws IOException {
			connectStream(p_bs,toIp,toPort);
			// TODO Auto-generated method stub
			/*
			ByteStream sbs;
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(toIp, toPort), 30000);
			sbs = new SocketByteStream(socket);
			
			StreamPipe sp0  = new StreamPipe(p_bs,sbs,65536);
			StreamPipe sp1  = new StreamPipe(sbs,p_bs,65536);
			sp0.start();
			sp1.start();
			*/
			return true;
		}
	}
	void connectStream(ByteStream p_bs,String p_ip,int p_port) throws IOException {
		ByteStream sbs;
		String key = p_ip+":"+p_port;
		ForwardRec frec = forwardHash.get(key);
		if(frec != null) {
			sbs = frec.mpx.connect(frec.channel);
		} else {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(p_ip, p_port), 10000);
			sbs = new SocketByteStream(socket);
		}
		StreamPipe sp0  = new StreamPipe(p_bs,sbs,65536);
		StreamPipe sp1  = new StreamPipe(sbs,p_bs,65536);
		sp0.start();
		sp1.start();
	}
	Hashtable<Integer,TcpRedirRec> tcpRedirHash;
	Hashtable<String,MpxRedirRec> mpxRedirHash;
	Hashtable<String,ForwardRec> forwardHash;
	public ProxyServer(String p_ip, int p_port) throws IOException {
		super(p_ip, p_port);
		tcpRedirHash = new Hashtable<Integer,TcpRedirRec>();
		mpxRedirHash = new Hashtable<String,MpxRedirRec>();
		forwardHash = new Hashtable<String,ForwardRec>();
	}
	public void addRedirect(int p_fromPort,String p_toIp,int p_toPort) throws Exception {
		if(tcpRedirHash.get(p_fromPort) != null) {
			throw new Exception("Port Already Redired");
		}
		TcpRedirRec rdr = new TcpRedirRec("0.0.0.0",p_fromPort);
		rdr.toIp = p_toIp;
		rdr.toPort = p_toPort;
		tcpRedirHash.put(p_fromPort, rdr);
		CoreLog.log("Start TcpRedirect from " + p_fromPort + " to " + p_toIp+":"+p_toPort);
		rdr.start();
	}
	public void addRedirect(ProxyClient p_client,int p_channel,String p_ip,int p_port) throws Exception {
		String skey = p_ip+":"+p_port;
		MpxRedirRec frec = mpxRedirHash.get(skey);
		if(frec != null) {
			throw new Exception("Channed Already Redired");
		}
		frec = new MpxRedirRec(p_client,p_channel);
		frec.toIp = p_ip;
		frec.toPort = p_port;
		mpxRedirHash.put(skey, frec);
		CoreLog.log("Start redir" + skey + " to " + frec.client.mpx.getByteStream().getName() + " channel " + p_channel);
		frec.start();
	}
	public void addForward(StreamMultiplexer p_mpx,int p_channel,String p_ip,int p_port) throws Exception {
		String skey = p_ip+":"+p_port;
		ForwardRec frec = forwardHash.get(skey);
		if(frec != null) {
			throw new Exception("Channed Already Forward");
		}
		frec = new ForwardRec(p_mpx,p_channel);
		forwardHash.put(skey, frec);
		CoreLog.log("Start forward " + skey + " to " + p_mpx.getByteStream().getName() + " channel " + p_channel);
	}
	@Override
	public boolean onStreamAccepted(ByteStream p_bs) throws IOException {
		ProxyServerRpc sv = new ProxyServerRpc(this);
//		throw new IOException("HAHA exception");
		RpcConnection rpcConn = new RpcConnection(null);
		rpcConn.setServlet(sv.getClass().getName(), sv);
		rpcConn.setByteStream(p_bs);
		CoreLog.log("rpcServer Ready");
		Thread th = new Thread(rpcConn);
		th.start();
		return true;
	}
	
	public static void main(String args[]){
		CoreLog.log("HAHA port = %d",CONTROL_PORT);
		try {
			ProxyServer mss = new ProxyServer("0.0.0.0",CONTROL_PORT);
			mss.run();
		} catch (Exception sex) {
			CoreLog.log(sex);
		}
	}
}
