package com.kikyosoft.stream;

import java.io.IOException;

import com.kikyosoft.utils.LogUtil;

public class WebSocketToTcpWrapper extends WebSocketStreamServer {
	
	int tcpport=0;
	public WebSocketToTcpWrapper(int p_tcpport, int p_wsport,boolean p_isSingle) {
		super(p_wsport, p_isSingle);
		// TODO Auto-generated constructor stub
		tcpport = p_tcpport;
	}

	@Override
	public boolean onStreamAccepted(ByteStream p_bs) throws IOException {
		byte x[] = new byte[10];
		p_bs.write(x,0,10);
		p_bs.flush();
		
		StreamMultiplexer mtx = new StreamMultiplexer(p_bs);
		mtx.start();
		SocketStreamListener ss = new SocketStreamListener("0.0.0.0",tcpport) {

			@Override
			public boolean onStreamAccepted(ByteStream p_bs) throws IOException {
				// TODO Auto-generated method stub
				LogUtil.log("before mtx.connect");
				ByteStream mbs = mtx.connect(port);
				LogUtil.log("after mtx.connect");
				if(mbs != null) {
					StreamPipe sp0  = new StreamPipe(p_bs,mbs,65536);
					StreamPipe sp1  = new StreamPipe(mbs,p_bs,65536);
					sp0.start();
					sp1.start();
					return(true);
				}
				return false;
			}
			
		};
		ss.start();
		return(true);
	}

	public static void main(String args[]){
		LogUtil.log("WebSocketStreamServer main start");
		int wsport = 5330;
		int tcpport = 5331;
		try {
			WebSocketToTcpWrapper s = new WebSocketToTcpWrapper(tcpport,wsport,true);
			s.start();
			for(;;) {
				LogUtil.log("sleep 5 seconds");
				Thread.sleep(5000);
			}
		} catch (Exception ex) {
			LogUtil.log(ex);
		}
		LogUtil.log("WebSocketStreamServer main end");
	}
	
	static class dummyWebSocketStreamServer extends WebSocketStreamServer{

		public dummyWebSocketStreamServer(int p_port, boolean p_isSingle) {
			super(p_port, p_isSingle);
			// TODO Auto-generated constructor stub
		}

		@Override
		public boolean onStreamAccepted(ByteStream p_bs) throws IOException {
			LogUtil.log("dummy Stream accepted");
			StreamMultiplexer mtx = new StreamMultiplexer(p_bs);
			mtx.start();
			/*
			byte bf[] = new byte[10];
			p_bs.write(bf, 0, 10);
			p_bs.flush();
			*/
			LogUtil.log("mtx connect begin");
			ByteStream mps = mtx.connect(getPort());
			LogUtil.log("mtx connect end got " + mps);
			// TODO Auto-generated method stub
			return true;
		}
		
	}
//	public static void main(String args[]){
//		int wsport = 5330;
//		try {
//			dummyWebSocketStreamServer s = new dummyWebSocketStreamServer(wsport,true);
//			s.start();
//			for(;;) {
//				CoreLog.log("sleep 5 seconds");
//				Thread.sleep(5000);
//			}
//		} catch (Exception ex) {
//			CoreLog.log(ex);
//		}
//		CoreLog.log("WebSocketStreamServer main end");
//	}

}
