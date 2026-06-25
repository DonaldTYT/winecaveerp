package com.kikyosoft.stream;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Hashtable;

import com.kikyosoft.utils.LogUtil;

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
					LogUtil.log("MPX accepted");
						try {
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
