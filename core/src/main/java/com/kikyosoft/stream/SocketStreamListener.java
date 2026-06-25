package com.kikyosoft.stream;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import com.kikyosoft.utils.LogUtil;

public abstract class SocketStreamListener implements StreamAcceptInterface ,Runnable {

	private InetAddress bindaddr;
	private int port;
	Thread th;
	ServerSocket serverSock;
	public SocketStreamListener(String p_ip,int p_port) throws IOException {
		if(p_ip != null) {
			bindaddr = InetAddress.getByName(p_ip);
			port = p_port;
			serverSock = new ServerSocket(port,10,bindaddr);
		}
	}
	
	public void start() throws IOException {
		th = new Thread(this);
		th.start();
	}
	
	@Override
	public void run() {
		try {
			for(;;) {
				Socket sock = serverSock.accept();
				if(sock != null) {
					LogUtil.log("TCP accepted");
					sock.setTcpNoDelay(true);
					LogUtil.log("Turn On TCP no delay");
						try {
							LogUtil.log("create socket stream");
							SocketByteStream sbs = new SocketByteStream(sock);
							LogUtil.log("calling onStreamAccept");
							if(onStreamAccepted(sbs)) {
								LogUtil.log("accepted ok");
							} else {
								LogUtil.log("accepted fail");
								sbs.close();
							}
						} catch (Exception ex) {
							LogUtil.log(ex);
						}
					}	
				}
		} catch (Exception ex) {
			LogUtil.log(ex);
		}
	}
}
