package com.kyoko.common;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

public abstract class MpxStreamListener implements StreamAcceptInterface ,Runnable {
	private StreamMultiplexer mpx;
	private int channel;
	Thread th;
	public MpxStreamListener(StreamMultiplexer p_mpx,int p_channel) throws IOException {
		mpx = p_mpx;
		channel = p_channel;
	}
	
	public void start() throws IOException {
		th = new Thread(this);
		th.start();
	}
	
	@Override
	public void run() {
		try {
			for(;;) {
				ByteStream sbs = mpx.accept(channel);
				if(sbs != null) {
					CoreLog.log("MPX accepted");
						try {
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
