package com.uniinformation.utils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;

import com.google.common.primitives.Bytes;

/***
 *  for udp redirect
 *  allow inspect / modify packet 
 *  known issue:
 *  - no data loss handling
 *  - bidirectional mode need to wait for sotimeout
 *
 */
public class UdpRedir {
	boolean fDebug = true;
	String lAddr;
	int lPort;
	
	String tAddr;
	int tPort;
	
	int soTimeout = 0; //default no timeout
	DatagramSocket rSock = null;  //communicate between source and server
	DatagramSocket sSock = null;  //communicate between server and target
	boolean biFlag = false;  //bidirectional communication
	
	ConcurrentLinkedQueue<byte[]> c2s = new ConcurrentLinkedQueue();
	ConcurrentLinkedQueue<byte[]> s2c = new ConcurrentLinkedQueue();

	InetAddress lInet = null;
	InetAddress tInet = null;
	
	AtomicBoolean fRun = new AtomicBoolean(false);
	Thread sendThread = null, recvThread = null;
	 
			
	
	/***
	 * you can inspect or modify data 
	 * @param data
	 * @return
	 */
	public byte[] processData(byte[] data, DatagramPacket rawPacket) {
		return data;
	}
	/***
	 * 
	 * @param lAddr - listen addr. blank 0.0.0.0
	 * @param lPort - listen port.
	 * @param tAddr - dest addr. blank localhost
	 * @param tPort - dest port.
	 */
	public UdpRedir(String lAddr, int lPort, String tAddr, int tPort) {
		this.lAddr = lAddr;
		this.lPort = lPort;
		this.tAddr = tAddr;
		this.tPort = tPort;
		this.soTimeout = 0;
	}
	public UdpRedir setSoTimeout(int soTimeout) {
		this.soTimeout = soTimeout;
		return this;
	}
	public UdpRedir setDebug(boolean fDebug) {
		this.fDebug = fDebug;
		return this;
	}
	public UdpRedir setBi(boolean biFlag) {
		this.biFlag = biFlag;
		return this;
		
	}
	public synchronized UdpRedir stop() {
		UniLog.log1("stop called");
		if (!fRun.get()) {
			UniLog.log1("already stop");
			return this;
		}
		try {
			fRun.set(false);
			rSock.close(); //close the socket to force abort socket receive
			sSock.close();
		}
		catch(Exception ex) {
			UniLog.log1("error:" + ex.getMessage());
		}
		
		return this;
	}
	
	
	public synchronized UdpRedir start() {
		if (fRun.get()) {
			UniLog.log1("already startted");
			return this;
		}
		fRun.set(true);
		

		try {
			
			lInet = InetAddress.getByName(StringUtils.isBlank(lAddr) ? "0.0.0.0" : lAddr);
			tInet = InetAddress.getByName(StringUtils.isBlank(tAddr) ? "localhost" : tAddr);
			
			
			
			if (fDebug) UniLog.log1("receive data: addr:%s port:%d", lInet,lPort);
			rSock = new DatagramSocket(lPort, lInet);
			rSock.setReuseAddress(true);  //allow immediate resue after restart
			rSock.setSoTimeout(soTimeout);
			
			if (fDebug) UniLog.log1("send data: target addr:%s port:%d", tInet, tPort);
			sSock = new DatagramSocket();
			sSock.setReuseAddress(true);
			sSock.setSoTimeout(soTimeout);
			UniLog.log1("biFlag:%s timeout:%d", biFlag, soTimeout);
			
			
			recvThread = new Thread("receive") {
				@Override
				public void run() {
					try {
						byte[] buf = new byte[8192];
						
						InetAddress cInet = null;
						int cPort = -1;
								
						
						while (true) {
							if (!fRun.get()) {
								UniLog.log1("stop");
								return;
							}
							if (c2s.size() > 0 || s2c.size() > 0) {
								if (fDebug) UniLog.log1("c2s:%d s2c:%d", c2s.size(), s2c.size());
							}
							
							DatagramPacket c2sPack = new DatagramPacket(buf, buf.length, lInet, lPort);
							
							//target to source communication
							if (s2c.size() > 0 && cInet != null && cPort > 0) {
								byte[] s2cData = (byte[]) s2c.poll();
								if (fDebug) UniLog.log1("send data to source:[%s,%s,%s]", new String(s2cData).trim(), cInet, cPort);
								DatagramPacket s2cPack = new DatagramPacket(s2cData, s2cData.length, cInet, cPort);
								rSock.send(s2cPack);
								continue;
							}
							
							//source to target communication
							try {
								rSock.receive(c2sPack);
								cPort = c2sPack.getPort();
								cInet = c2sPack.getAddress();
								
								

								byte[] data = Arrays.copyOfRange(buf, c2sPack.getOffset(), c2sPack.getLength());  
								if (data != null && data.length > 0) {
									//if (fDebug) UniLog.log1("got data recvbuf:%d:%d:%d:[%s]", buf.length, c2sPack.getLength(), c2sPack.getOffset(), new String(buf,c2sPack.getOffset(),c2sPack.getLength()).trim());
									data = processData(data, c2sPack);
								}
								if (data != null && data.length > 0) {
									//if (fDebug) UniLog.log1("data after process:%d:%s", data.length, new String(data).trim());
									c2s.add(data);
									sendThread.interrupt(); //can consider change to sotimeout temporary
								}
							}
							catch(SocketTimeoutException timeoutex) {
								//if (fDebug) UniLog.log1("receive timeout");
							}
						}
					}
					catch(Exception ex) {
						ex.printStackTrace();
					}
					finally {
						try { rSock.close(); }catch(Exception ex) {}
					}

					
				}
			};
			recvThread.start();
			
			sendThread = new Thread("send") {
				@Override
				public void run() {
					if (!fRun.get()) {
						UniLog.log1("stop");
						return;
					}
					
					try {
						byte[] buf = new byte[8192];
						InetAddress cInet = null;
						int cPort = -1;
								
						while (true) {
							if (!fRun.get()) {
								UniLog.log1("stop");
								return;
							}
	
							//source to target communication
							if (c2s.size() > 0) {
								byte[] data = (byte[]) c2s.poll();
								UniLog.log1("send data to target [%s]", new String(data).trim());
								
								DatagramPacket s2cPack = new DatagramPacket(data, data.length, tInet, tPort);
								cInet = s2cPack.getAddress();
								cPort = s2cPack.getPort();
								sSock.send(s2cPack);
								//UniLog.log1("after send data");	
								continue;
							}
							
							
							//for unidirectional, wait for source data
							if (!biFlag) {
								try {
									//UniLog.log1("sleep");
									Thread.sleep(soTimeout);
								}
								catch(InterruptedException  ex) {
									//UniLog.log1("got interrupt");
								}
								continue;
							}
							
							//target to source data (i.e. bidirectional)
							try {
								if (cInet != null) {
									DatagramPacket c2sPack = new DatagramPacket(buf, buf.length, cInet, cPort);
									sSock.receive(c2sPack);
									
									byte[] data = Arrays.copyOfRange(buf, c2sPack.getOffset(), c2sPack.getLength());  
									if (data != null && data.length > 0) {
										UniLog.log1("got data:%d:%d:[%s]", c2sPack.getLength(), data.length, new String(data).trim());
										s2c.add(data);
									}
								}
							}
							catch(SocketTimeoutException timeoutex) {
								//if (fDebug) UniLog.log1("receive timeout");
							}
							
							
							
						}
					}
					catch(Exception ex) {
						ex.printStackTrace();
					}
					finally {
						try { sSock.close(); }catch(Exception ex) {}
					}
					
				}
			};
			sendThread.start();
			
		}
		catch(Exception ex) {
			UniLog.log1("error:" + ex.getMessage());
		}
		return this;
		
		
	}
	
	public static void main(String args[]) throws Exception{
		new UdpRedir(null, 1234, "localhost", 2345){
			@Override
			public byte[] processData(byte[] data, DatagramPacket rawPacket) {
				UniLog.log1("from:%s:%d data:%s", rawPacket.getAddress().getHostAddress(), rawPacket.getPort(), new String(data).trim());
				//return Bytes.concat(data, " ack!!".getBytes());
				return data;
			}
		}.setSoTimeout(5000).setBi(true).start();
		
		/*
		UdpRedir uredir = new UdpRedir(null, 1234, "localhost", 2345){
			@Override
			public byte[] processData(byte[] data, DatagramPacket rawPacket) {
				UniLog.log1("from:%s:%d data:%s", rawPacket.getAddress().getHostAddress(), rawPacket.getPort(), new String(data));
				return data;
			}
		}.start();
		Thread.sleep(1000);
		uredir.stop();
		*/
	}

}
