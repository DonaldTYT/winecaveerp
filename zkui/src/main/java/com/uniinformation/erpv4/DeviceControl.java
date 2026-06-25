package com.uniinformation.erpv4;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
/*
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
*/

import com.uniinformation.cron.CronJob;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class DeviceControl extends CronJob {
	static public final String MOBILE_SCANNER_PREFIX = "MB_BS_";
	static public final String MOBILE_PREFIX = "MB_";
	static public final int ENQ = 5;
	static public final int ETX = 3;
//	public static enum DEVICE_TYPE {DEVTYPE_BARCODE_SCANNER,DEVICE_PRINTER,DEVICE_CAMERA,DEVICE_RECORDER,DEVICE_GPS};
	public static int queueIdx = 0;
	static final String BIND_SCANNER_PREFIX = "BindBcnToSession:";
	AtomicBoolean fStop = new AtomicBoolean(false); //1 stopping, 2 stopped
	static HashSet<String>tcpDeviceList;
	static HashSet<String>barcodeScannerList;  //andrew230922 seems it's useless
	static HashSet<String>labelPrinterList;  //andrew230922 seems it's useless

	SessionHelper sh;
	
	static Hashtable<String ,Socket> tcpListenHash;
	public class TcpDeviceListener implements Runnable {
		ServerSocket serverSocket;
		@Override
		public void run() {
			UniLog.log("Device Listerner tarted");
			tcpListenHash = new Hashtable<String,Socket>();
			for(;;) {
				try {
					Thread.sleep(1000);
					if(serverSocket == null) {
						serverSocket = new ServerSocket(5678);
						if(serverSocket == null) {
							UniLog.log("Tcp Listen Error");
							continue;
						} else {
							UniLog.log("Tcp Listen Started");
						}
					}
					UniLog.log("Device Listener try accept");
					Socket socket = serverSocket.accept();
					UniLog.log("Device Listener Connected");
					if(socket != null) {
						synchronized(tcpListenHash) {
							socket.setSoTimeout(10000);
							InputStream input = socket.getInputStream();
							String ss="";
							int cc = 0;
							for(;;) {
								cc = input.read();
								if( cc < 0) break;
								if( cc == 0x0d) {
									break;
								} else {
									char ch = (char) cc;
									ss += ch;
								}
							}
							if(cc >= 0) {
							if(!ss.trim().equals("")) {
								UniLog.log("Device Addr = " + ss.trim());
								/*
								if(tcpDeviceList.contains(ss.trim())) {
									Socket ssc = tcpListenHash.get(ss.trim());
									if(ssc != null) ssc.close();
									tcpListenHash.put(ss.trim(), socket);
								} else {
									UniLog.log("device " + ss.trim() + " not in tcpDeviceList, refjected");
									socket.close();
								}
								*/
									Socket ssc = tcpListenHash.get(ss.trim());
									if(ssc != null) ssc.close();
									tcpListenHash.put(ss.trim(), socket);
							}
							}
						}
					}
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		}
	}
	

	public static class DevHandler implements Runnable {
		String devid;
		Date lastActive;
		String sessionId;
		String queueid;
		String devip;
		int devport;
		Thread readerThread;
		Socket socket=null;
		StringBuffer sb=null;
		Object photoEventData;
		int tcpTimeout = 0;
		boolean isPassive = false;
		private boolean attached = false;
		SessionHelper sh;
		public DevHandler(String p_devid, SessionHelper p_sh) {
			devid = p_devid;
			sh = p_sh;
		}
		String getDevip() {
			return devip;
		}
		int getDevport() {
			return devport;
		}
		DevHandler setDevip(String p_devip) {
			devip = p_devip;
			return this;
		}
		DevHandler setDevport(int p_devport) {
			devport = p_devport;
			return this;
		}
		public Socket getOrConnectSocket() throws Exception {
			Socket socket = null;
			if(tcpDeviceList != null && tcpDeviceList.contains(devid) ) {
				synchronized(tcpListenHash) {
					String ss = getDevip()+":"+getDevport();
					socket = tcpListenHash.get(ss);
					if(socket != null) {
						tcpListenHash.remove(ss);
					}
				}
			}  else {
				String ip = BiConfig.getString(sh, devid+"_IP"); 
				int port = BiConfig.getInteger(sh, devid+"_PORT", -1);
				if(ip != null) {
					UniLog.log1("device ip(overrided) %s:%d", ip, port);
					socket = new Socket(ip, port);
				} else {
					//UniLog.log1("device ip %s:%d", ip, port);
					UniLog.log1("device ip %s:%d", devip, devport);
					socket = new Socket(devip, devport);
				}
			}
			return(socket);
		}
		public void startReader() {
			if(readerThread == null) {
				readerThread = new Thread(this);
				readerThread.start();
			}
		}
		synchronized public boolean unsetQueueid(String p_queueid) {
			try {
				if(queueid != null && queueid.equals(p_queueid)) {
					queueid = null;
					sessionId = null;
					if(socket != null ) {
						socket.close();
						socket = null;
					}
				}
				
				if(isPassive && attached) {
					attached = false;
				}
				return(true);
			} catch (IOException iex) {
				UniLog.log(iex);
				return(false);
			}
			
		}
		synchronized public boolean setQueueid(String p_queueid,boolean p_force) {
			if(queueid != null) {
				if(!queueid.equals(p_queueid) && !p_force) return(false);
			}
			if(p_queueid != null && p_queueid == queueid) {
				if(socket != null) {
//					EventQueue que = EventQueues.lookup(queueid, EventQueues.APPLICATION, false);
					Object que = sh.lookupEventQueue(queueid, SessionHelper.EVENT_TYPE.APPLICATION, false);
					if(que != null) {
						UniLog.log("Send BarcodeAttach Event(B) " + devid + " to " + queueid);
//						que.publish(new Event("onBarcodeAttached", null,devid));
						sh.publishEventQueue(que, "onBarcodeAttached", devid);
					}
					return(true);
				} else {
					int cc;
					cc = 0;
				}
			}
			if(isPassive) {
//				EventQueue que = EventQueues.lookup(queueid, EventQueues.APPLICATION, false);
				Object que = sh.lookupEventQueue(queueid, SessionHelper.EVENT_TYPE.APPLICATION, false);
				if(que != null) {
					UniLog.log("Send BarcodeDetach Event " + devid + " to " + queueid);
//					que.publish(new Event("onBarcodeDetached", null,devid));
					sh.publishEventQueue(que,"onBarcodeDetached",devid);
				}
			}
			queueid = p_queueid;
			if(socket != null) {
				try {
					socket.close();
					socket = null;
				} catch (IOException iex) {
					UniLog.log(iex);
					return(false);
				}
			} else {
				notifyAll();
			}
			return(true);
		}
		synchronized public void stopReader() {
			if(readerThread != null) {
				UniLog.log("restart reader " + devid);
				try {
					if(socket != null) {
						socket.close();
							socket = null;
					}
					/*
					synchronized(DevHandler.this) {
						if(socket != null) {
							socket.close();
							socket = null;
						}
					}
					*/
				} catch (IOException iex) {
					UniLog.log(iex);
				}
			}
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			UniLog.log("Device Reader " + devid + " started");
			for(;;) {
			try {
				String curqueueid = null;
				synchronized(DevHandler.this) {
					curqueueid = queueid;
				}
				if(isPassive) {
					/*  !!!!!!! this part of code is incomplete , need more code to properly handle device attach/detatch */
					synchronized(DevHandler.this) {
						UniLog.log(devid+" wait for 10 second");
						if(curqueueid != null && !attached) {
//							EventQueue que = EventQueues.lookup(curqueueid, EventQueues.APPLICATION, false);
							Object que = sh.lookupEventQueue(curqueueid, SessionHelper.EVENT_TYPE.APPLICATION, false);
							if(que != null) {
								UniLog.log("Send BarcodeAttach Event(A) " + devid + " to " + queueid);
//								que.publish(new Event("onBarcodeAttached", null,devid));
								sh.publishEventQueue(que, "onBarcodeAttached", devid);
								attached = true;
							} else {
								UniLog.log("event que " + queueid + " not exist , remove quueid and close socket" );
								synchronized(DevHandler.this) {
									attached = false;
									queueid = null;
								}
							}
						}
						if(photoEventData != null) {
							if(curqueueid != null) {
//								EventQueue que = EventQueues.lookup(curqueueid, EventQueues.APPLICATION, false);
								Object que = sh.lookupEventQueue(curqueueid, SessionHelper.EVENT_TYPE.APPLICATION, false);
								if(que != null) {
									UniLog.log("Send addPhoto Event to " + curqueueid);
									sh.publishEventQueue(que, "onAddPhoto", photoEventData);
								}
								photoEventData = null;
							}
						}
						if(sb != null) {
							String scannedCode = sb.toString();
							if(scannedCode.startsWith(BIND_SCANNER_PREFIX)) {
									UniLog.log("re-attache queue");
							} else {
								if(curqueueid != null) {
//									EventQueue que = EventQueues.lookup(curqueueid, EventQueues.APPLICATION, false);
									Object que = sh.lookupEventQueue(curqueueid, SessionHelper.EVENT_TYPE.APPLICATION, false);
									if(que != null) {
										UniLog.log("Send BarcodeNotify Event " + scannedCode);
//										que.publish(new Event("onBarcodeNotify", null,scannedCode));
										sh.publishEventQueue(que, "onBarcodeNotify", scannedCode);
									}
								}
							}
							sb = null;
						}
						UniLog.log(devid+" wait for 10 second");
						wait(10000);
					}
				} else if(devip != null && devport > 0 && curqueueid != null) {
					sb = null;
					String ip = BiConfig.getString(sh, devid+"_IP"); 
					int port = BiConfig.getInteger(sh, devid+"_PORT", -1);
					Socket mySocket = null;
					if(ip != null) {
						mySocket = new Socket(ip, port);
					} else {
						mySocket = new Socket(devip, devport);
					}
					if(mySocket != null) {
						if(tcpTimeout > 0) {
							mySocket.setSoTimeout(30000);
						} 
						InputStream is = mySocket.getInputStream();
						OutputStream os = mySocket.getOutputStream();
						String attachedQue=null;
						if(curqueueid != null) {
//							EventQueue que = EventQueues.lookup(curqueueid, EventQueues.APPLICATION, false);
							Object que = sh.lookupEventQueue(curqueueid, SessionHelper.EVENT_TYPE.APPLICATION, false);
							if(que != null) {
								UniLog.log("Send BarcodeAttach Event(A) " + devid + " to " + queueid);
//								que.publish(new Event("onBarcodeAttached", null,devid));
								sh.publishEventQueue(que, "onBarcodeAttached", devid);
								attachedQue = curqueueid;
								synchronized(DevHandler.this) {
									socket = mySocket;
								}
							} else {
								UniLog.log("event que " + queueid + " not exist , remove quueid and close socket" );
								synchronized(DevHandler.this) {
									queueid = null;
									mySocket.close();
								}
								continue;
							}
						}
						int cc;
						try {
						while((cc = is.read()) >= 0) {
							if(sb == null) sb=new StringBuffer();
							if(cc == 3) {
								UniLog.log("flush one packet [" + sb.toString()+"]");
								String scannedCode = sb.toString();
								if(scannedCode.startsWith(BIND_SCANNER_PREFIX)) {
									String thisId;
									String thisIp;
									int thisPort;
									synchronized(DevHandler.this) {
										thisId = devid;
										thisIp = devip;
										thisPort = devport;
									}
									refreshDevIp(null,thisId,thisIp,thisPort,scannedCode.substring(BIND_SCANNER_PREFIX.length()));

								} else {
								if(curqueueid != null) {
//									EventQueue que = EventQueues.lookup(curqueueid, EventQueues.APPLICATION, false);
									Object que = sh.lookupEventQueue(curqueueid, SessionHelper.EVENT_TYPE.APPLICATION, false);
									if(que != null) {
										UniLog.log("Send BarcodeNotify Event " + scannedCode);
//										que.publish(new Event("onBarcodeNotify", null,scannedCode));
										sh.publishEventQueue(que,"onBarcodeNotify", scannedCode);
									}
								}
								}
								sb = null;
							} else if(cc == 5) {
								UniLog.log("Got ENQ from " + devid);
								byte b[] = new byte[1];
								b[0] = 5;
//								EventQueue que = EventQueues.lookup(curqueueid, EventQueues.APPLICATION, false);
								Object que = sh.lookupEventQueue(curqueueid, SessionHelper.EVENT_TYPE.APPLICATION, false);
								if(que != null) {
									os.write(b);
									os.flush();
								} else {
									UniLog.log("Event Queue " + curqueueid + " not exist , remove queueid and close socket ");
									synchronized(DevHandler.this) {
										queueid = null;
									}
									break;
								}
							} else {
								char c = (char) cc;
								sb.append(c);
								UniLog.log("got byte " + cc);
							}
						}
						} catch (Exception ex) {
							UniLog.log(ex);
						}
						is.close();
						synchronized(DevHandler.this) {
							if(socket != null) {
								socket.close();
								socket = null;
							}
						}
						if(attachedQue != null) {
//							EventQueue que = EventQueues.lookup(attachedQue, EventQueues.APPLICATION, false);
							Object que = sh.lookupEventQueue(attachedQue, SessionHelper.EVENT_TYPE.APPLICATION, false);
							if(que != null) {
								UniLog.log("Send BarcodeDetach Event " + devid + " to " + attachedQue);
//								que.publish(new Event("onBarcodeDetached", null,devid));
								sh.publishEventQueue(que,"onBarcodeDetached",devid);
							}
						}
						attachedQue = null;
						if(queueid != null) unsetQueueid(queueid);
					}
				} else {
					synchronized(DevHandler.this) {
						UniLog.log(devid+" wait for 10 second");
						wait(10000);
					}
				}
			} catch (Exception iex) {
				UniLog.log(iex);
			}
			}
		}
	}
	
	static Hashtable<String,DevHandler> devList = new Hashtable<String,DevHandler>();
	
//	DeviceControl currentDevControl;
	
	
	// not yet start as cron job
	
	protected void processSessionMsg(SessionHelper sh,String devid,String devip,int port,String sessionMsg) {
		
	}
	
	@Override
	public int runOnce() throws Exception {
		// TODO Auto-generated method stub

		
		int udpPort = BiConfig.getInteger(sh, "DeviceControlUDPPort", 5678);
		int udpSoTimeout = BiConfig.getInteger(sh, "DeviceControlUDPSoTimeout", 20000);
		
		UniLog.log1("Device Control Poll. port:%d soTimeout:%d",udpPort, udpSoTimeout);
		
		DatagramSocket socket = new DatagramSocket(udpPort);
		try {
		socket.setSoTimeout(udpSoTimeout);
		
		byte[] buf = new byte[256];
		for(;;) {
//			Thread.sleep(20000);
//			UniLog.log("in Device Control Loop");
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				//UniLog.log1("waiting for packet");
				socket.receive(packet);
				String msg = new String(buf,packet.getOffset(),packet.getLength());
				UniLog.log("DeviceControl UDP Received ["+msg+"]");
				//sample msg
				//"RFID01              192.168.1.152             6003        593.13     ";
				//"RFID01              192.168.1.152             6003        173.13      1";
				
				try {
				String devid = StringUtil.strpart(msg, 0, 20).trim();
				String devip = StringUtil.strpart(msg, 20, 20).trim();
				int devport = Integer.parseInt(StringUtil.strpart(msg, 40, 10).trim());
				UniLog.log1("Attach Device " + devid + " ip " + devip + " port " + devport);
				String sessionMsg = StringUtil.strpart(msg, 70, -1).trim();
				if(sessionMsg != null && sessionMsg.startsWith(BIND_SCANNER_PREFIX)) {
					refreshDevIp(sh,devid,devip,devport,sessionMsg.substring(BIND_SCANNER_PREFIX.length()));
				} else {
					refreshDevIp(sh,devid,devip,devport,null);
					
					//if(sessionMsg != null && !sessionMsg.isEmpty()) processSessionMsg(sh,devid,devip,devport,sessionMsg);
					processSessionMsg(sh,devid,devip,devport,sessionMsg);
				}
				} catch (Exception xex) {
					UniLog.log(xex);
				}
			} catch (SocketTimeoutException sex) {
				UniLog.log1("DeviceControl Wait UDP timeout");
				
				//cleanup and abort
				if (fStop.get()) {
					UniLog.log1("close socket and abort");
					try {
						socket.close();
					}
					catch(Exception ex) { 
						ex.printStackTrace();
					}
					return 0;
				}
			}
		}
		} catch (Exception ex) {
			//UniLog.log(ex);
			UniLog.log1("error:" + ex.getMessage());
			socket.close();
			throw(ex);
		}
	}

	@Override
	public void setSessionHelper(SessionHelper p_sh) throws Exception {
		// TODO Auto-generated method stub
		UniLog.log("HAHA in Device Control Set SessionHelper");
		sh = p_sh;
		tcpDeviceList = new HashSet<String>();
		barcodeScannerList = new HashSet<String>();
		labelPrinterList= new HashSet<String>();
		String bcn1 = BiConfig.getString(p_sh, "BarcodeScanner1");
		if(bcn1 != null && !bcn1.trim().isEmpty()) barcodeScannerList.add(bcn1);
		String bcns = BiConfig.getString(p_sh, "BarcodeScanners");
		if(bcns != null) {
			for (StringTokenizer token = new StringTokenizer(bcns,",");
				  token.hasMoreTokens();) {
				String s = token.nextToken();
				if(s != null && !s.trim().isEmpty()) {
					barcodeScannerList.add(s);
				}
			}
		}
		bcns = BiConfig.getString(p_sh, "LabelPrinters");
		if(bcns != null) {
			for (StringTokenizer token = new StringTokenizer(bcns,",");
				  token.hasMoreTokens();) {
				String s = token.nextToken();
				if(s != null && !s.trim().isEmpty()) {
					labelPrinterList.add(s);
				}
			}
		}
		bcns = BiConfig.getString(p_sh, "DeviceProxy");
		if(bcns != null) {
			for (StringTokenizer token = new StringTokenizer(bcns,",");
				  token.hasMoreTokens();) {
				String s = token.nextToken();
				if(s != null && !s.trim().isEmpty()) {
					tcpDeviceList.add(s);
				}
			}
		}
		if(tcpDeviceList != null &&  !tcpDeviceList.isEmpty()) {
			Thread devThread = new Thread(new TcpDeviceListener());
			devThread.start();
		}
		
	}

	public static boolean attachListiner(String p_devid,String p_msgqueue,boolean p_force) {
		synchronized(devList) {
			DevHandler dhdr = devList.get(p_devid);
			if(dhdr == null) return(false);
			dhdr.setQueueid(p_msgqueue,p_force);
			return(true);
		}
	}

	public static boolean detachListiner(String p_devid,String p_msgqueue) {
		synchronized(devList) {
			DevHandler dhdr = devList.get(p_devid);
			if(dhdr == null) return(false);
			dhdr.unsetQueueid(p_msgqueue);
			return(true);
		}
	}

	static boolean isBarcodeScanner(SessionHelper p_sh,String p_devid) {
		if(p_devid.startsWith(MOBILE_SCANNER_PREFIX)) return(true);
		if(p_devid.equals("MB01")) return(true);
		String bcn1 = BiConfig.getString(p_sh, "BarcodeScanner1");
		if(p_devid.equals(bcn1)) return(true);
		String bcns = BiConfig.getString(p_sh, "BarcodeScanners");
		if(bcns != null) {
			for (StringTokenizer token = new StringTokenizer(bcns,",");
				  token.hasMoreTokens();) {
				String s = token.nextToken();
				if(p_devid.equals(s)) return(true);
			}
		}
		return(false);
	}
	static public void refreshDevIp(SessionHelper p_sh,String p_devid,String p_devip,int p_devport,String sessionId) {
		synchronized(devList) {
			DevHandler dhdr = devList.get(p_devid);
			if(dhdr == null) {
				dhdr = new DevHandler(p_devid, p_sh);
				devList.put(p_devid, dhdr);
				String bcn1 = BiConfig.getString(p_sh, "BarcodeScanner1");
				if(p_devid.startsWith(MOBILE_SCANNER_PREFIX) || isBarcodeScanner(p_sh,p_devid)) {
					String bcTimeout = BiConfig.getString(p_sh, "BarcodeTcpTimeout");
					if(bcTimeout != null) {
						dhdr.tcpTimeout = Integer.parseInt(bcTimeout);
					}
					UniLog.log("Start barcode scanner reader " + p_devid + " tcp timeout " + dhdr.tcpTimeout);
					dhdr.startReader();
				}
				/*
				if(p_devid.equals(bcn1)) {
					String bcTimeout = BiConfig.getString(p_sh, "BarcodeTcpTimeout");
					if(bcTimeout != null) {
						dhdr.tcpTimeout = Integer.parseInt(bcTimeout);
					}
					UniLog.log("Start barcode scanner reader " + p_devid + " tcp timeout " + dhdr.tcpTimeout);
					dhdr.startReader();
				}
				*/
				/*
				if(p_devid.startsWith("BCN")) {
					dhdr.startReader();
				}
				*/
			}
			dhdr.lastActive = new Date();
			boolean requireRestart = true;
			/*
			boolean requireRestart = false;
			if(dhdr.devip != null && dhdr.devport > 0) {
				if((!dhdr.devip.equals(p_devip)) || dhdr.devport != p_devport) {
					requireRestart = true;
				}
			}
			*/
			dhdr.devip = p_devip;
			dhdr.devport = p_devport;
			if(p_devid.startsWith(MOBILE_PREFIX) || p_devid.equals("MB01")) dhdr.isPassive = true;
			if(sessionId != null) {
				dhdr.sessionId = sessionId;
			}
			if(requireRestart) {
					dhdr.stopReader();
			}
		}
	}
	
	public static void postBarcode(SessionHelper p_sh,String p_devid,String p_barcode) {
		UniLog.log("postBarcode (" + p_devid +")(" + p_barcode+")");
		synchronized(devList) {
			DevHandler dhdr = devList.get(p_devid);
			if(dhdr == null) {
				if(p_barcode.startsWith(BIND_SCANNER_PREFIX)) {
					refreshDevIp(p_sh,p_devid,"",0,p_barcode.substring(BIND_SCANNER_PREFIX.length()));
				}
			} else {
				synchronized(dhdr) {
//						dhdr.sb = new StringBuffer(p_barcode);
//						dhdr.notifyAll();
					if(p_barcode.startsWith(BIND_SCANNER_PREFIX)) {
						dhdr.sessionId = p_barcode.substring(BIND_SCANNER_PREFIX.length());
//						if(dhdr.queueid != null) {
//							EventQueue que = EventQueues.lookup(dhdr.queueid, EventQueues.APPLICATION, false);
//							if(que != null) {
//								UniLog.log("Send BarcodeAttach Event(B) " + dhdr.devid + " to " + dhdr.queueid);
//								que.publish(new Event("onBarcodeAttached", null,dhdr.devid));
//							}
//						}
						
					} else {
						dhdr.sb = new StringBuffer(p_barcode);
						dhdr.notifyAll();
					}
				}
			}
			/*
			if(dhdr == null) {
				dhdr = new DevHandler(p_devid);
				devList.put(p_devid, dhdr);
			}
			if(dhdr != null) {
				dhdr.lastActive = new Date();
				UniLog.log("Post Barcode " + p_devid + " ["+p_barcode+"] queue " + dhdr.queueid);
				if(dhdr.queueid != null) {
					EventQueue que = EventQueues.lookup(dhdr.queueid, EventQueues.APPLICATION, false);
					if(que != null) {
						que.publish(new Event("onBarcodeNotify", null,p_barcode));
					}
				}
			}
			*/
		}
	}
	
	public static String getSessionIdByDevice(String p_devid) {
		synchronized(devList) {
			for (Enumeration<DevHandler> en=devList.elements(); en.hasMoreElements(); ) {
				DevHandler dhdr = en.nextElement();
				if(dhdr.devid != null && dhdr.devid.equals(p_devid)) {
					return(dhdr.sessionId);
				}
			}
		}
		return(null);
	}
	public static String getDevIdBySession(String p_sessionId) {
		synchronized(devList) {
			for (Enumeration<DevHandler> en=devList.elements(); en.hasMoreElements(); ) {
				DevHandler dhdr = en.nextElement();
				if(dhdr.sessionId != null && dhdr.sessionId.equals(p_sessionId)) {
					return(dhdr.devid);
				}
			}
		}
		return(null);
	}
	public static DevHandler getDevHandler(String p_devid) {
		if (StringUtils.isBlank(p_devid)) {
			return null;
		}
		synchronized(devList) {
			DevHandler dhdr = devList.get(p_devid);
			if(dhdr != null) {
				return(dhdr);
			}
		}
		return(null);
	}
	
	@Override
	public void stop() {
		UniLog.log1("called");
		fStop.set(true);
	}
	
	synchronized public static String getUniqueEventQueid() {
		queueIdx++;
		return("DevQueue"+queueIdx);
	}
	
	synchronized  public static HashSet<String> getLabelPrinterList() {
		return(labelPrinterList);
	}
	
	static public boolean hasBarcodePrinter(SessionHelper sp ) {
		return(BiConfig.getString(sp,"LabelPrinter1") != null);
	}
	static public boolean hasBarcodeScanner(SessionHelper sp ) {
		return(BiConfig.getString(sp,"BarcodeScanners") != null);
	}
	
	public static void postPhoto(SessionHelper p_sh,String p_devid, Object p_eventdata) {
		UniLog.log("postPhoto(" + p_devid +")");
		synchronized(devList) {
			DevHandler dhdr = devList.get(p_devid);
			synchronized(dhdr) {
				dhdr.photoEventData = p_eventdata;
				dhdr.notifyAll();
			}
		}
	}
}
