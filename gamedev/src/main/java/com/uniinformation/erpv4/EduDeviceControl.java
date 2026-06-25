package com.uniinformation.erpv4;

import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
//import org.zkoss.zsoup.helper.StringUtil;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.erpv4.edu.ProcessScanLog;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
/*****************
 * remark220831: 
 * this class should be obsoleted and replaced by RfidController+DeviceControl
 *
 */
@Deprecated
public class EduDeviceControl extends DeviceControl {
	final static int LED_OFF = 0x0;
	final static int LED_RED = 0x1;
	final static int LED_YELLOW = 0x2;
	//final static int LED_GREEN = 0x4;
	final static int BUZZER = 0x4;
	final static int LED_WHITE = 0x8;
	final static int LED_ALL = LED_RED+LED_YELLOW+LED_WHITE;
	
	ConcurrentHashMap<String,Long> devHM = new ConcurrentHashMap();  //key:ip, value:timestamp
	

	@Override
	protected void processSessionMsg(SessionHelper sh,String devid,String devip,int port,String sessionMsg) {
		UniLog.log1("process scan called. id:%s ip:%s:%d [%s]", devid, devip, port, sessionMsg);
		
		//startup or keep alive call
		if (StringUtils.isBlank(sessionMsg)) {
			if (devHM.get(devip) == null || (new Date().getTime() - devHM.get(devip) > 60000)) {  //if initial call, on all led and standby
				UniLog.log1("keep alive new");
				setLED(LED_ALL, 1000, devip, port, 2);
			}
			else {
				UniLog.log1("keep alive old");
				//setLED(LED_YELLOW, 500, devip, port);
			}
			devHM.put(devip, new Date().getTime());
			return;
		}
		
		//with card no
		String cardNo = extractCardNo(sessionMsg);
		if (cardNo == null) {
			UniLog.log1("cannot obtain cardNo [%s]", sessionMsg);
			setLED(LED_RED, 500, devip, port);
			return;
		}
		
		ReturnMsg rtn = ProcessScanLog.addScanLog(sh, devid, cardNo);
		UniLog.log1("addScanLog return:%s", rtn);
		if (rtn.getStatus()) {
			setLED(LED_YELLOW, 500, devip, port);  //OK
		}
		else {
			setLED(LED_RED, 500, devip, port, 3);  //FAIL
		}
	}
	public static void main(String args[]) throws Exception{
		UniLog.log1("" + extractCardNo("55AA04000000010107954CDA"));
		UniLog.log1("" + extractCardNo("55AA04000000000107954CDA"));
		EduDeviceControl edc = new EduDeviceControl();
		//edc.processSessionMsg(SessionHelper.getSessionHelperDummy(),"TEST001","123.123.123.123",123,"");
		edc.processSessionMsg(null,"TEST001","123.123.123.123",123,"55AA04000000010107954CDA");
		
	}
	
	/***
	 * extract 10 digit from sessionMsg
	 * @param p_sessionMsg
	 * @return
	 */
	private static String extractCardNo(String p_sessionMsg) {
		//e.g. 55AA04000000010107954CDA - 10107954C - 43122,41484
		try {
			if (StringUtils.isBlank(p_sessionMsg)) {
				UniLog.log1("sessionMsg is blank");
				return null;
			}
			String hexVal = StringUtils.substring(p_sessionMsg, p_sessionMsg.length()-12, p_sessionMsg.length() - 2);
			UniLog.log1("in:%s hex:%s", p_sessionMsg, hexVal);
			if (StringUtils.isBlank(hexVal)) {
				UniLog.log1("hexVal is blank");
				return null;
			}
			long longVal = Long.parseLong(hexVal, 16);
			String strVal = String.format("%010d", longVal);
			UniLog.log1("cardNo is [%d] [%s]", longVal, strVal);
			return strVal;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}
	
	private static synchronized void setLED(int color, int duration, String devip, int port) {
		setLED(color, duration, devip, port, 1);
	}
	private static synchronized void setLED(int color, int duration, String devip, int port, int cnt) {
		UniLog.log1("set color %d ip:%s:%d", color, devip, port);
		Socket socket = null;
		DataOutputStream dos = null;
		try {
			//TODO: need to cache the socket and use event handler to receive scan event
			//current implementation is not responsive 
			socket = new Socket(devip, port); 
			if (socket == null) {
				UniLog.log1("create socket fail ip:%s port:%d", devip, port);
				return;
			}
			Thread.sleep(100);
			dos = new DataOutputStream(socket.getOutputStream());
			for (int i=0; i<cnt; i++) {
				dos.write(0x2);
				dos.write('A');
				dos.write(color+BUZZER);
				dos.flush();
				Thread.sleep(duration);
				
				dos.write(0x2);
				dos.write('A');
				dos.write(LED_WHITE);
				dos.flush();
				Thread.sleep(duration);
			}
			
		}
		catch (Exception ex) {
			UniLog.log1("ERROR:" + ex.getMessage());
			ex.printStackTrace();
		}
		finally {
			if (dos != null) {
				try {
					dos.close();
				}
				catch(Exception ex3) {
					UniLog.log1("ERROR:" + ex3.getMessage());
				}
			}
			if (socket != null) {
				try {
					socket.close();
				}
				catch(Exception ex2) {
					UniLog.log1("ERROR:" + ex2.getMessage());
					//ex2.printStackTrace();
				}
			}
		}
		
	}
}