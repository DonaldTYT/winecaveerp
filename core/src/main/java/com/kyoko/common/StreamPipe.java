package com.kyoko.common;

import java.io.IOException;

public class StreamPipe implements Runnable {
	

	static final int BUFSIZE = 16384;
	ByteStream from,to;
	Thread th;
	boolean started;
	byte[] buf;
	public StreamPipe (ByteStream p_from,ByteStream p_to,int p_bufsize) {
		from  = p_from;
		to = p_to;
		th = new Thread(this);
		buf = new byte[p_bufsize];
	}
	public void start() {
		started = true;
		th.start();
		CoreLog.log("thread " + th + " started");
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			for(;;) {
				int cc = from.read(buf,0,buf.length);
				if(cc <= 0) break;
				ByteStream.loopWrite(to, buf, 0, cc);
				to.flush();
			}
		} catch (IOException ex) {
//			CoreLog.log(ex);
		}
		CoreLog.log("streampipe thread " + th + " ended");
		try {
			to.close();
		} catch (IOException ex) {
//			CoreLog.log(ex);
		}
		started = false;
	}
}
