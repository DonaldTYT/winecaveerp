package com.kikyosoft.stream;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Hashtable;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import com.kikyosoft.utils.LogUtil;

//public class WebSocketMultiplexerServer extends WebSocketServer implements Runnable {
	
public abstract class WebSocketStreamServer extends WebSocketServer implements StreamAcceptInterface {
	
	boolean isSingle;
	int port;
	Hashtable<WebSocket,WebSocketByteStream> connectionHash;
    public class WebSocketByteStream extends DataStream
    {

    	WebSocket conn;
		public WebSocketByteStream(WebSocket p_conn) throws IOException {
			super(65536,65536);
			conn = p_conn;
			// TODO Auto-generated constructor stub
		}

		@Override
		public void setTimeout(int msec) throws IOException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void flush() throws IOException {
			flushOutputPipe();
		}

		@Override
		protected void flushOutputPipe() throws IOException {
			// TODO Auto-generated method stub
         	int available;
        	while ( (available = pis.available()) > 0) {
        		byte ba[] = new byte[available];
        		int idx = 0;
        		while(available > 0) {
        			int cc = pis.read(ba,idx,available);
        			if(cc <= 0) {
        				throw new IOException("WebSocketDataStream read error");
        			}
        			idx+=cc;
        			available-=cc;
        		}
        		LogUtil.log("WebSocketStream send " + ba.length + " bytes");
        		conn.send(ba);
        	}	
		}

		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return ("websocket:"+conn.toString());
		}
    }
	

	public WebSocketStreamServer(int p_port,boolean p_isSingle) {
		super(new InetSocketAddress(p_port));
		port = p_port;
		isSingle = p_isSingle;
		connectionHash = new Hashtable<WebSocket,WebSocketByteStream>();
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		// TODO Auto-generated method stub
		LogUtil.log("websocket opened");
		
		synchronized(connectionHash) {
			try {
				if(isSingle) {
					for(WebSocket wconn : connectionHash.keySet()) {
						connectionHash.get(wconn).close();
						wconn.close();
					}
					connectionHash.clear();
				}
				WebSocketByteStream wbs = new WebSocketByteStream(conn);
				if(onStreamAccepted(wbs)) {
					connectionHash.put(conn, wbs);
				} else {
					conn.close();
				}
			} catch (Exception ex) {
				LogUtil.log(ex);
			}
		}
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		// TODO Auto-generated method stub
		LogUtil.log("websocket closed");
		synchronized(connectionHash) {
			try {
				connectionHash.remove(conn);
			} catch (Exception ex) {
				LogUtil.log(ex);
			}
		}
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		// TODO Auto-generated method stub
		LogUtil.log("websocket text message");
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer message) {
		LogUtil.log("websocket binary message");
		try {
			WebSocketByteStream wbs = connectionHash.get(conn);
			byte[] ba = message.array();
//			if(wbs != null) ByteStream.loopWrite(wbs, ba , 0, ba.length);
			if(wbs != null) wbs.pos.write(ba);
		} catch (Exception ex) {
			LogUtil.log(ex);
		}
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		// TODO Auto-generated method stub
		LogUtil.log("websocket error");
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		LogUtil.log("websocket start");
	}
	
	public int getPort() {
		return(port);
	}
	

}
