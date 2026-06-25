package com.uniinformation.erpv4.edu;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
//import org.zkoss.zsoup.helper.StringUtil;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.cron.CronJob;
import com.uniinformation.erpv4.DeviceControl.DevHandler;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.DeviceControl;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class RfidController extends CronJob {
	final static int LED_OFF = 0x0;
	final static int LED_RED = 0x1;
	final static int LED_YELLOW = 0x2;
	//final static int LED_GREEN = 0x4;
	final static int BUZZER = 0x4;
	final static int LED_WHITE = 0x8;
	final static int LED_ALL = LED_RED+LED_YELLOW+LED_WHITE;
	private SessionHelper sh;

	HashMap<String,RfidReader> attendanceReaders;
	final static int SO_TIMEOUT = 30000;

	class RfidReader implements Runnable  {
		String devid;
		Socket socket;
		OutputStream dos;	
		InputStream dis;	
		public RfidReader(String p_devid,Socket p_socket) throws Exception {
			devid = p_devid;
			socket = p_socket;
			dos = socket.getOutputStream();
			dis = socket.getInputStream();
		}
		@Override
		public void run() {
			/* RFID reader handling code start here */
			UniLog.log1("[%s] rfid reader thread started", devid);
			
			try {
				// socket.setSoTimeout(10000);
				if (SO_TIMEOUT > 0) {
					socket.setSoTimeout(SO_TIMEOUT);
				}
				setLED(LED_ALL+BUZZER, 1000, 3);
				for(;;) {
					int cc = dis.read();
					if(cc < 0) break;
					UniLog.log1("[%s] got byte %d ", devid, cc);
					switch(cc) {
					case DeviceControl.ENQ:
//						dos.write(DeviceControl.ENQ);
						setLED(LED_YELLOW,500);
						break;
					default : 
						StringBuffer sb = new StringBuffer();
						sb.append((char) cc);
						for(;;) {
							cc = dis.read();
							if( cc < 0) break;
							if(cc == DeviceControl.ETX) {
								String cardNo = extractCardNo(sb.toString());
								UniLog.log1("[%s] rfid got [%s] [%s]", devid, sb.toString(), cardNo);
								
								ReturnMsg rtn = ProcessScanLog.addScanLog(sh, devid, cardNo);
								UniLog.log1("[%s] addScanLog return:%s", devid, rtn);
								if (rtn.getStatus()) {
									//setLED(LED_WHITE+BUZZER, 100,3);  //OK
									setLED(LED_WHITE+BUZZER, 100,1);  //OK
									
								}
								else {
									setLED(LED_RED+BUZZER, 500, 3);  //FAIL
								}
								
								/* validate the scanned record */
								
								/* flash RED 3 times 0.3 second each */
								/*
								dos.write(0x2);
								dos.write('B');
								dos.write(LED_RED);
								dos.write(3);

								dos.write(0x2);
								dos.write('B');
								dos.write(0);
								dos.write(3);

								dos.write(0x2);
								dos.write('B');
								dos.write(LED_RED);
								dos.write(3);

								dos.write(0x2);
								dos.write('B');
								dos.write(0);
								dos.write(3);

								dos.write(0x2);
								dos.write('B');
								dos.write(LED_RED);
								dos.write(3);

								dos.write(0x2);
								dos.write('B');
								dos.write(LED_WHITE);
								dos.write(1);

								dos.flush();
								*/
								
								break;
							} else {
								sb.append((char) cc);
							}
						}
						break;
					}
					dos.flush();
					if( cc < 0) break;
				}
			} catch (Exception ex) {
				/* tcp disconnected */
				//UniLog.log(ex);
				UniLog.log1("[%s] error:%s", devid, ex.getMessage());
			} finally {
				synchronized(attendanceReaders) {
					/* close socket , terminate thread and let the main controller to reconnect */
					attendanceReaders.put(devid, null);
					try {
						UniLog.log1("[%s] rfid reader socket close and thread terminated", devid);
						socket.close();
					} 
					catch (Exception ex2) {
						//UniLog.log(ex2);
						UniLog.log1("[%s] error:%s", devid, ex2.getMessage());
					}
				}
			}
		}
		
		private void setLED(int color, int duration) throws Exception{
			setLED(color, duration, 1);
		}
		private void setLED(int color, int dur, int cnt) throws Exception {
			UniLog.log1("[%s] color:%d dur:%d cnt:%d", devid, color, dur, cnt);
			for (int i=0; i<cnt; i++) {
				writeCmd(color, dur);
				writeCmd(LED_OFF, dur);
			}
			writeCmd(LED_WHITE, 100);
		}
		/***
		 * 
		 * @param color
		 * @param dur
		 * @throws Exception
		 */
		private void writeCmd(int color, int dur) throws Exception {
			int dd = dur/100;
			//UniLog.log1("write color:%d dd:%d", color, dd);
			dos.write(0x2);
			dos.write('B');
			dos.write(color);
			dos.write(dd);
		}
	}

	
	/* Cronjob Entry point called by Cronserver */
	@Override
	public int runOnce() throws Exception {

		/* initialze the attendanceReaders device list, should read from config file later */
		attendanceReaders = new HashMap<String,RfidReader>();
		String rfidReader = Erpv4Config.getString(sh, "RfidReader");
		if (StringUtils.isBlank(rfidReader)) {
			UniLog.log1("no rfidreader refined.");
			return -1;
		}
		String rfidReaderArr[] = StringUtils.split(rfidReader, ",");
		for (String rfid : rfidReaderArr) {
			UniLog.log1("rfid:%s", rfid);
			attendanceReaders.put(rfid.trim(), null);
		}
		//attendanceReaders.put("RFID01", null);
		
		for (;;) {
		//UniLog.log1("running..."); 
			
			/* check for any registered reader that is not yet connected to the controller */
			for(String devid : attendanceReaders.keySet()) {  //andrew220111 risk: if key updated, it will throw concurrent modification error. 
				//RfidReader rfid = attendanceReaders.get(devid);  //andrew220111 fix get object multiple time
				RfidReader rfid = null;
				synchronized(attendanceReaders) {
					rfid = attendanceReaders.get(devid);
					
					/* reader is already connected , nothing to do */
					if(rfid != null) continue;
				}
				
				/* get the dhdr to get the reader's ip and port no reported from the udp packet (if received) */
				//DevHandler dhdr = new DeviceControl.DevHandler(devid).setDevip("192.168.1.1").setDevport(1111); //dummy test
				DevHandler dhdr = DeviceControl.getDevHandler(devid);
				if(dhdr != null) {
					//UniLog.log1("Device Handler for " + devid + " exists, ip " + dhdr.getDevip() + " port " + dhdr.getDevport() + " connecting... ");
					//Socket socket = new Socket(dhdr.getDevip(), dhdr.getDevport());
					
					Socket socket = null;
					try {
						String ip = Erpv4Config.getString(sh, devid+"_IP"); 
						int port = Erpv4Config.getInteger(sh, devid+"_PORT", -1);
						if (!StringUtils.isBlank(ip) && port > 0) {
							socket = new Socket(ip,port);
						} else {
							socket = dhdr.getOrConnectSocket();
						}
					} catch (Exception ex ) {
						UniLog.log1("error:"+ ex.getMessage());
					}
					/*
					if (StringUtils.isBlank(ip)) {
						ip = dhdr.getDevip();
					}
					if (port <= 0) {
						port = dhdr.getDevport();
					}
					UniLog.log1("device handler exists. id:%s dev:%s:%d effective:%s:%d. connecting...", devid, dhdr.getDevip(), dhdr.getDevport(), ip, port);
					Socket socket = null;
					try {
						socket = new Socket(ip,port);
					}
					catch(Exception ex) { 
						UniLog.log1("error:"+ ex.getMessage());
					} 
					*/
					
					if(socket != null) {
						/* if tcp connect to reader is ok, spwan new thread to handle this reader */
						UniLog.log1("connection ok, spawn thread to handle this connection");
						synchronized(attendanceReaders) {
							//attendanceReaders.put(devid, new RfidReader(devid,socket));
							//attendanceReaders.get(devid).run();
							//220111:andrew fix bug. spawn new thread for each rfidReader
							RfidReader rfidReaderRunnnable = new RfidReader(devid,socket);
							attendanceReaders.put(devid, rfidReaderRunnnable);
							new Thread(rfidReaderRunnnable).start();
						}
					} else {
						UniLog.log1("connection failed, delay to next run");
					}
				} else {
					UniLog.log1("device handler for " + devid + " not exist, delay to next run");
				}
			}
			//Thread.sleep(10000);
			Thread.sleep(30000);
		}
	}

	@Override
	public void setSessionHelper(SessionHelper p_sh) throws Exception {
		sh = p_sh;
	}
	
	public static void main(String args[]) {
		UniLog.log1("" + extractCardNo("55AA04000000010107954CDA"));
		UniLog.log1("" + extractCardNo("55aa040000001000009e9b11"));
		UniLog.log1("" + extractCardNo("55aa040000001012345678XX"));
	}
	
	private static String extractCardNo(String p_sessionMsg) {
		//e.g. 55AA04000000010107954CDA - 10107954C - 43122,41484
		//new  55aa040000001000009e9b11
		//55aa 04 00000010 0000 9e9b 11
//		use the the first 2 byte is always 55 aa
//		the third byte is the card type (normally 04 for ic card)
//		4-7 is the card batch number which is not shown on the card's face
//		8-11 is the card number that is matched with the number on the card's face
//		e.g. 00 00 9e 9b = decimal 40603
//		
		try {
			if (StringUtils.isBlank(p_sessionMsg)) {
				UniLog.log1("sessionMsg is blank");
				return null;
			}
			String majorHex = StringUtils.substring(p_sessionMsg, 14, 18);
			String minorHex = StringUtils.substring(p_sessionMsg, 18, 22);
			UniLog.log1("in:%s major:%s minor:%s", p_sessionMsg, majorHex,minorHex);
			if (StringUtils.isAnyBlank(majorHex,minorHex)) {
				UniLog.log1("major or minor is blank");
				return null;
			}
			long major = Long.parseLong(majorHex, 16);
			long minor = Long.parseLong(minorHex, 16);
			String strVal = String.format("%05d%05d", major,minor);
			UniLog.log1("cardNo is %d,%d [%s]", major,minor, strVal);
			return strVal;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}	
	
}