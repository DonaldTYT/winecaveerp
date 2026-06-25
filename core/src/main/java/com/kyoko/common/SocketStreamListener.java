package com.kyoko.common;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

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
					CoreLog.log("TCP accepted");
					sock.setTcpNoDelay(true);
					CoreLog.log("Turn On TCP no delay");
						try {
							CoreLog.log("create socket stream");
							SocketByteStream sbs = new SocketByteStream(sock);
							CoreLog.log("calling onStreamAccept");
							if(onStreamAccepted(sbs)) {
								CoreLog.log("accepted ok");
							} else {
								CoreLog.log("accepted fail");
								sbs.close();
							}
						} catch (Exception ex) {
							CoreLog.log(ex);
						}
					}	
				}
		} catch (Exception ex) {
			CoreLog.log(ex);
		}
	}
}
