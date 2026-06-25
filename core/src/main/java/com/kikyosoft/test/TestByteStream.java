package com.kikyosoft.test;

import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import com.kikyosoft.rpccall.RpcClient;
import com.kikyosoft.rpccall.Value;
import com.kikyosoft.stream.BufferedByteStream;
import com.kikyosoft.stream.ByteStream;
import com.kikyosoft.stream.SocketByteStream;
import com.kikyosoft.stream.StreamMultiplexer;
import com.kikyosoft.utils.LogUtil;

public class TestByteStream {
	static class Receiver implements Runnable {
		ByteStream bs;
		int port;
		public Receiver(ByteStream p_bs,int p_port) {
			
//			bs = p_bs;
			bs = new BufferedByteStream(p_bs,4096,4096);
			port = p_port;
			Thread th = new Thread(this);
			th.setDaemon(true);
			th.start();
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				byte buf[] = new byte[1024];
				for(;;) {
					int cc = bs.read(buf, 0, 1024);
					if( cc > 0) {
						LogUtil.log("Mpx " + cc + " byte read");
					}
				}
			} catch (Exception ex) {
				LogUtil.log(ex);
			}
			LogUtil.log("Socket closed");
		}
	}
	static class Listener implements Runnable {
		int port;
		public Listener(int p_port) {
			port = p_port;
			Thread th = new Thread(this);
			th.setDaemon(true);
			th.start();
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				InetAddress ia = InetAddress.getByName("0.0.0.0");
				ServerSocket serversock = new ServerSocket(port,10,ia);
				serversock.setSoTimeout(5000);
				for(;;) {
					/*
					Thread.sleep(1000);
					CoreLog.log("listener sleep 1 second");
					*/
					Socket sock = serversock.accept();
					if(sock != null) {
						LogUtil.log("tcp call accepted");
						SocketByteStream ss = new SocketByteStream(sock);

//						new Receiver(ss,3456);
						StreamMultiplexer mpx = new StreamMultiplexer(ss);
						mpx.start();
						ByteStream bs = mpx.connect(3457);
						new Receiver(bs,3456);
						Thread.sleep(1000);
					} else {
						LogUtil.log("tcp listen timeout");
					}
				}
			} catch (Exception ex) {
				LogUtil.log(ex);
			}
		}
	}
	public static void main(String args[]){
		try {
			KeyGenerator keygen = KeyGenerator.getInstance("AES");
        	SecretKey k = keygen.generateKey();
            Cipher aes = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aes.init(Cipher.ENCRYPT_MODE, k);
            PipedInputStream pis = new PipedInputStream();
            PipedOutputStream pos = new PipedOutputStream(pis);
            Cipher aes2 = Cipher.getInstance("AES/ECB/PKCS5Padding");
            CipherInputStream cis = new CipherInputStream(pis,aes);
            CipherOutputStream cos = new CipherOutputStream(pos,aes);
            cos.write("0123456789012345".getBytes());
            cos.flush();
            pos.flush();
            //pos.write("A".getBytes());;
            //pos.flush();
            byte[] buf = new byte[256];
            LogUtil.log("before read");
            int cc = cis.read(buf);
            LogUtil.log("'A' -> "+cc + " bytes");
            	
            /*
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            CipherOutputStream cos = new CipherOutputStream(bos,aes);
            cos.write("AB".getBytes());
            cos.flush();
            */
            /*
            cos.write("A".getBytes());
            cos.flush();
            cos.write("B".getBytes());
            cos.flush();
            cos.close();
            byte[] ba = bos.toByteArray();
            CoreLog.log(""+ba.length);
            */
		} catch (Exception ex) {
			LogUtil.log(ex);
		}
        
	}
	public static void mainyy(String args[]){
		LogUtil.log("main start");
		try {
		RpcClient rpc = new RpcClient("192.168.1.208",30001);
		rpc.open();
		Value v;
		for(int i=0;i<5;i++) {
			LogUtil.log("test call " + i);
			v = rpc.callSegment("com.kyoko.proxy.ProxyControlServlet.ping");
			LogUtil.log(v.toString());
			Thread.sleep(1000);
		}
		v = rpc.callSegment("com.kyoko.proxy.ProxyControlServlet.enableMultiPlexer");
		LogUtil.log(v.toString());
		if(v.toString().startsWith("OK")) {
			ByteStream bs = rpc.getConnection().getByteStream();
			StreamMultiplexer mpx = new StreamMultiplexer(bs);
			mpx.start();
			ByteStream bs2 = mpx.connect(1);
			LogUtil.log("do rpc call on mpx channel 1 "+ bs2);
			rpc.getConnection().setByteStream(bs2);
			for(int i=0;i<5;i++) {
				LogUtil.log("test call " + i);
				v = rpc.callSegment("com.kyoko.proxy.ProxyControlServlet.ping");
				LogUtil.log(v.toString());
				Thread.sleep(1000);
			}
		}
		for(;;) {
			LogUtil.log("Wait 5 sec");
			Thread.sleep(3000);
		}
		} catch (Exception ex) {
			LogUtil.log(ex);
		}
	}
	public static void mainxx(String args[]){
		Listener th0;
		LogUtil.log("main start");
		th0 = new Listener(3446);
		LogUtil.log("listenr created");
		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress("localhost", 3446), 30000);
			if(socket == null) {
				LogUtil.log("tcp call timoeut");
			} else {
				SocketByteStream sos = new SocketByteStream(socket);
				StreamMultiplexer mpx = new StreamMultiplexer(sos);
				mpx.start();
				ByteStream ss = mpx.accept(3457);
				
				
				byte buf[] = new byte[1024];
				for(int i=0;i<10;i++) {
					int cc = ss.write(buf, 0, 1024);
					ss.flush();
					LogUtil.log("try "+i+ " " + cc + " byte send");
					Thread.sleep(500);
				}
				Thread.sleep(5000);
				ss.close();
			}
			for(;;) {
				Thread.sleep(1000);
				LogUtil.log("main sleep 1 second");
			}
		} catch (Exception ex) {
			LogUtil.log(ex);
		}
	}
	
	
}
