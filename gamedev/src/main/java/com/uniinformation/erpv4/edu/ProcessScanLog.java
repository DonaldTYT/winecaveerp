package com.uniinformation.erpv4.edu;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.birt.report.model.api.util.StringUtil;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.cron.CronJob;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.edu.Student;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.utils.StopWatchHelper;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

public class ProcessScanLog extends CronJob {
	private static final int MIN_PROCESS_SCAN_LOG_INTERVAL = 60000;
	private static final int MIN_PROCESS_SCAN_LOG_DAY = 1;
	
	private static ProcessScanLog processScanLogObject;

	private SessionHelper sessionHelper;
	private int processScanLogInterval = MIN_PROCESS_SCAN_LOG_INTERVAL;
	private int processScanLogDay = MIN_PROCESS_SCAN_LOG_DAY;
	
	
	//skeleton code for card status 
	private static AtomicBoolean csMapDirtyFlag = new AtomicBoolean(true);
	private static ConcurrentHashMap<String,CardStatus> csMap = new ConcurrentHashMap<String,CardStatus>();  //key cardno, value CardStatus
	private static ConcurrentHashMap<String,String> csMapDirtyFlags = new ConcurrentHashMap<String,String>();  //key cardno, value cardno (uesless)
	private static AtomicBoolean allowCheckCardStatus = new AtomicBoolean(true);
	
	//card status enum. only VALID is true.
	public enum CardStatus {  
		VALID,              //the card status is valid. i.e. after mark attendance, all token balance still >= 0
		CSMAP_IS_EMPTY,     //csmap is not ready
		CARDNO_IS_BLANK,    //cardno is blank
		CARDNO_NOT_FOUND,   //cardno found not
		STUDENT_CANCELLED,  //student already cancelled
		NEGATIVE_BALANCE,  //current balance has negative token
		//NEGATIVE_BALANCE_AFTER_MARK_ATTENDANCE  //after mark today attendance, it has negative token balance (TBC)

	}
	
	

	@Override
	public int runOnce() throws Exception {
		for (;;) {
			UniLog.log1("Wakeup (interval:%d)", processScanLogInterval);
			updateAttend();
			
			//andrew220322 construct card status map after updateAtend
			//depend on the performance. if it's too slow, remove forceupdate flag
			updateCardStatusMap(sessionHelper);
		
			
			synchronized(this) {
				wait(processScanLogInterval);
			}
		}
	}

	@Override
	public void setSessionHelper(SessionHelper p_sh) throws Exception {
		processScanLogObject = this;
		sessionHelper = p_sh;
		processScanLogInterval = Math.max(Erpv4Config.getInteger(sessionHelper, "processScanLogInterval", processScanLogInterval), MIN_PROCESS_SCAN_LOG_INTERVAL);
		processScanLogDay = Math.max(Erpv4Config.getInteger(sessionHelper, "processScanLogDay", processScanLogDay), MIN_PROCESS_SCAN_LOG_DAY);
		allowCheckCardStatus.set(StringUtils.equalsAnyIgnoreCase(Erpv4Config.getString(sessionHelper, "allowCheckCardStatus",allowCheckCardStatus.get() ? "Y" : "N"),"Y","YES","TRUE"));
		
	}
	
	public static ProcessScanLog getProcessScanLogObject() {
		return processScanLogObject;
	}
	
	/*
	 * It called by the scanner device.
	 * @param scannerId: reserved for future use, set to blank first.
	 * @param cardNo
	 * */
	private static String lastAddScanLogCardNo;
	private static long lastAddScanLogTime;
	public static synchronized ReturnMsg addScanLog(SessionHelper p_sh, String scannerId, String cardNo) {
		UniLog.log1("called scannerid:%s cardno:%s", scannerId, cardNo);
		
		if (StringUtil.isBlank(cardNo)) {
			return new ReturnMsg(false, "Card no cannot be empty");
		}
		/*
		//andrew210918 remark: allow to insert duplicate scanlog to improve user experience. but shouldnot process those duplicate record to improve the efficient.
		if (StringUtils.equals(cardNo, lastAddScanLogCardNo) && System.currentTimeMillis() - lastAddScanLogTime <= 2000)
			return new ReturnMsg(false, "Skip duplicate Card no");
		*/
		
		//regdev callback. in case reader in reg state. intercept the scan
		ReturnMsg rtn = processScanLogRegDev(scannerId, cardNo);
		if (rtn.getStatus()) {
			return rtn;
		}
		
		lastAddScanLogCardNo = cardNo;
		lastAddScanLogTime = System.currentTimeMillis();

		SessionHelper sh = p_sh;
		if (sh == null) {
			sh = ZkSessionHelper.getSessionHelperDummy(null,"dummy",null);
		}
		BiResult brStudent = null;
		BiResult brScanLog = null;
		boolean commitDone = false;
		try {
			brScanLog = sh.newBiResult("edu.ScanLog");
			brScanLog.beginWork();
			brScanLog.clearCurrentRec();
			brScanLog.getCell("esscl_scantime").set(DateUtil.now());
			brScanLog.getCell("esscl_scannerid").set(scannerId);
			brScanLog.getCell("esscl_cardno").set(cardNo);
			brScanLog.getCell("esscl_sdrg").set(0);
			brScanLog.getCell("esscl_status").set("NEW");
			brScanLog.getCell("esscl_result").set("");

			//after insert scanlog record, perform card status check. e.g. it can alert user if they has negative balance
			if (allowCheckCardStatus.get()) {
				//obtain card status from memory 
				CardStatus cardStatus = checkCardStatus(cardNo);
				brScanLog.getCell("esscl_scanresp").set(cardStatus.name());
				brScanLog.addCurrent();
				brScanLog.commitWork();
				commitDone = true;
				if (cardStatus != CardStatus.VALID) {
					return new ReturnMsg(false, "Invalid card status:" + cardStatus);
				}
				return ReturnMsg.defaultOk;
			}
			else {
				brScanLog.getCell("esscl_scanresp").set("");
				brScanLog.addCurrent();
				brScanLog.commitWork();
				commitDone = true;
			}

			//andrew220322 for backward compatibility. it can be fade out later as cardno checking already include in checkcardstatus()
			brStudent = BiResultHelper.create(sh, "edu.Student", String.format("essd_cardno = '%s'", cardNo), -1, null);
			if (brStudent.next()) {
				if (!StringUtils.equals(brStudent.getCellString("essd_status"), "Cancelled"))
					return ReturnMsg.defaultOk;
				else
					return new ReturnMsg(false, "Student record cancelled");
			}
			else
				return new ReturnMsg(false, "Student record not found");
			
		}
		catch (Exception e) {
			UniLog.log(e);
			//e.printStackTrace();
			if (!commitDone && brScanLog != null)
				brScanLog.rollbackWork();
			return new ReturnMsg(e);
		}
		finally {
			if (brScanLog != null)
				brScanLog.close();
			if (brStudent != null)
				brStudent.close();
		}
	}
	
	/*
	 * Add a method for batch process scanlog record. It update student attendance based on scanlog. If student has scanned a card, it suppose he is present for whole day.
	 * */
	public synchronized ReturnMsg updateAttend() {
		BiResult brQScanLog = null;
		BiResult brAttendance = null;
		BiResult brStudent = null;
		List<String> errMsgList = new ArrayList<String>();
		try {
			Map<String, Integer> cardStudentMap = new HashMap<String, Integer>();
			Map<Date, Map<Integer, String>> dateStudentMap = new HashMap<Date, Map<Integer, String>>();

			brStudent = sessionHelper.newBiResult("edu.Student");
			brAttendance = sessionHelper.newBiResult("edu.Attendance");
			brAttendance.beginWork();
			RpcClient rpc = brAttendance.getSelectUtil().getRpcClient();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date endDate = DateUtil.dayEnding(DateUtil.today());
			Date startDate = DateUtil.dayBeginning(DateUtil.prevday(endDate, processScanLogDay - 1));
			brQScanLog = BiResultHelper.create(sessionHelper, "edu.ScanLog", 
					String.format("esscl_scantime between '%s' and '%s' and esscl_status = 'NEW'", sdf.format(startDate), sdf.format(endDate)), -1, null);
			while (brQScanLog.next()) {
				int sid = brQScanLog.getCurrentCollection().getSid();
				String cardNo = brQScanLog.getCellString("esscl_cardno");
				Date scanTime = brQScanLog.getCellDate("esscl_scantime");
				Date scanDate = DateUtil.dayBeginning(scanTime);
				UniLog.log1("cardNo:%s, scanDate:%s, sid:%d", cardNo, scanDate, sid);
				Integer studentRg = null;
				String errMsg = null;
				if (StringUtils.isNotBlank(cardNo)) {
					studentRg = cardStudentMap.get(cardNo);
					if (studentRg == null) {
						brStudent.clearCondition();
						brStudent.addCustomCondition(String.format("essd_cardno = '%s'", cardNo));
						ReturnMsg rtn;
						if ((rtn = brStudent.query(true, false)).getStatus()) {
							if (brStudent.next()) {
								if (!StringUtils.equals(brStudent.getCellString("essd_status"), "Cancelled")) {
									studentRg = brStudent.getCellInt("essd_rg");
									cardStudentMap.put(cardNo, studentRg);
								}
								else
									errMsg = "Student record cancelled";
							}
							else
								errMsg = "Student record not found";
						}
						else
							throw new Exception(rtn.getMsg());
					}
				}
				UniLog.log1("studentRg:%d", studentRg);
				if (studentRg != null) {
					brAttendance.getSelectUtil().executeUpdate("update esscanlog set esscl_sdrg = ?, esscl_status = 'OK', esscl_result = 'OK' where serial_id = ?", 
							new Wherecl().appendArgument(studentRg)
										.appendArgument(sid));
					Map<Integer, String> studentMap = dateStudentMap.get(scanDate);
					if (studentMap == null) {
						studentMap = new HashMap<Integer, String>();
						dateStudentMap.put(scanDate, studentMap);
					}
					studentMap.put(studentRg, cardNo);
				}
				else {
					errMsg = "FAIL" + errMsg;
					brAttendance.getSelectUtil().executeUpdate("update esscanlog set esscl_status = 'FAIL', esscl_result = ? where serial_id = ?", 
							new Wherecl().appendArgument(errMsg)
										.appendArgument(sid));
					errMsgList.add(errMsg);
				}
			}
			for (Map.Entry<Date, Map<Integer, String>> entry : dateStudentMap.entrySet()) {
				Date scanDate = entry.getKey();
				Map<Integer, String> studentMap = entry.getValue();
				UniLog.log1("student updateAttendance studentMap:%s, startDate:%s, endDate:%s", studentMap, scanDate, DateUtil.dayEnding(scanDate));
				Student.updateAttendance(sessionHelper, brAttendance, rpc, studentMap, "Present", scanDate, DateUtil.dayEnding(scanDate), true);
			}
			brAttendance.commitWork();
			if (errMsgList.isEmpty())
				return ReturnMsg.defaultOk;
			else {
				StringBuilder sb = new StringBuilder();
				for (String s : errMsgList) {
					if (sb.length() > 0)
						sb.append("\n");
					sb.append(s);
				}
				return new ReturnMsg(false, sb.toString());
			}
		}
		catch (Exception e) {
			UniLog.log("updateAttend error:" + e.toString());
			//e.printStackTrace();
			if (brAttendance != null) {
				UniLog.log1("brAttendance rollbackWork");
				brAttendance.rollbackWork();
			}
			errMsgList.add(e.toString());
			StringBuilder sb = new StringBuilder();
			for (String s : errMsgList) {
				if (sb.length() > 0)
					sb.append("\n");
				sb.append(s);
			}
			return new ReturnMsg(false, sb.toString());
		}
		finally {
			if (brQScanLog != null)
				brQScanLog.close();
			if (brAttendance != null)
				brAttendance.close();
			if (brStudent != null)
				brStudent.close();
		}
	}
	public static class RegDev {
		long expTime;
		AtomicReference<String> cardNoAR;
		public RegDev(long dur, AtomicReference<String> cardNoAR) {
			this.expTime = new Date().getTime() + dur;
			this.cardNoAR = cardNoAR;
		}
		public boolean isValid() {
			if (cardNoAR != null && new Date().getTime() <= expTime) {
				return true;
			}
			else {
				return false;
			}
		}
	}
	private static ConcurrentHashMap<String,RegDev> regDevHM = new ConcurrentHashMap();
	/***
	 * 
	 * @param p_devId
	 * @param p_maxDur max duration in ms
	 * @return
	 */
	public static ReturnMsg allocateRegDev(String p_devId, long p_maxDur, AtomicReference<String> p_cardNoAR) {
		if (StringUtils.isBlank(p_devId)) {
			UniLog.log1("devid is blank");
			return new ReturnMsg(false,"Dev ID is blank");
		}
		synchronized(regDevHM) {
			if (regDevHM.get(p_devId) == null || !regDevHM.get(p_devId).isValid()) {
				UniLog.log1("allocate device %s ok", p_devId);
				//regDevHM.put(p_devId, new Date().getTime() + p_maxDur);
				regDevHM.put(p_devId, new RegDev(p_maxDur, p_cardNoAR));
				return ReturnMsg.defaultOk;
			}
			else {
				UniLog.log1("allocate device %s fail", p_devId);
				return new ReturnMsg(false,"Device in use. Please try again later.");
			}
		}
	}
	public static void releaseRegDev(String p_devId) {
		if (StringUtils.isBlank(p_devId)) {
			UniLog.log1("devid is blank");
			return;
		}
		synchronized(regDevHM) {
			RegDev regDev = regDevHM.remove(p_devId);
			UniLog.log1("release device id:%s regdev:%s", p_devId, regDev);
		}
	}
	public static ReturnMsg processScanLogRegDev(String p_devId, String p_cardNo) {
		UniLog.log1("called. devId:%s cardNo:%s", p_devId, p_cardNo);
		synchronized(regDevHM) {
			//RegDev regDev = regDevHM.get(p_devId);
			RegDev regDev = regDevHM.remove(p_devId);  //each reg request process 1 time only
			UniLog.log1("check regdev %s", regDev);
			if (regDev != null && regDev.isValid()) {
				UniLog.log1("regdev scannerId:%s cardNo:%s", p_devId, p_cardNo);
				regDev.cardNoAR.set(p_cardNo);
				return ReturnMsg.defaultOk;
			}
			else {
				return ReturnMsg.defaultFail;
			}
		}
		
	}
	
	
	
	//skeleton code for card status 
	/***
	 * obtain card status from memory
	 * the response need to very quick as it need to show the result on rfid reader immediately
	 * @param cardNo
	 * @return
	 */
	public static CardStatus checkCardStatus(String cardNo) {
		if (!allowCheckCardStatus.get()) {
			UniLog.log1("function disable and return valid");
			return CardStatus.VALID;
		}
		if (StringUtils.isBlank(cardNo)) {
			UniLog.log1("cardno is blank");
			return CardStatus.CARDNO_IS_BLANK;
		}
		if (csMap.size() == 0) {
			UniLog.log1("csmap is empty cardNo:%s", cardNo);
			return CardStatus.CSMAP_IS_EMPTY;
		}
		CardStatus cs = csMap.get(cardNo);
		if (cs == null) {
			UniLog.log1("card not found cardNo:%s", cardNo);
			return CardStatus.CARDNO_NOT_FOUND;
		}
		UniLog.log1("cardNo:%s return:%s", cardNo, cs);
		return cs;
	}
	
	/***
	 * call by external code which will affect card status
	 * it will update the whole map
	 * e.g. student detail update(cardno update), payment updated(topup balance), course updated(e.g. course fee), attendance updated (e.g. consume/refund token)
	 */
	public static void setCSMapDirty() {
		UniLog.log1("set dirty flag");
		csMapDirtyFlag.set(true);
	}
	/***
	 * call by external code which affect card status
	 * it only update the specific cardno
	 * @param cardNo
	 * @param wakeupFlag - true, wakeup the background thread
	 */
	public static void setCSMapDirty(String cardNo, boolean wakeupFlag) {
		if (StringUtils.isBlank(cardNo)) {
			UniLog.log1("card no is blank, ignore");
			return;
		}
		UniLog.log1("set dirty flag cardNo:%s", cardNo);
		csMapDirtyFlags.put(cardNo,cardNo);
		if (wakeupFlag) {
			wakeup();
		}
	}
	public static void setCSMapDirty(String cardNo) {
		setCSMapDirty(cardNo, false);
	}
	
	/***
	 * wake up the waiting thread
	 */
	public static void wakeup() {
		try {
			UniLog.log1("wakeup called");
			if (processScanLogObject == null) {
				UniLog.log1("processScanLogObject is null");
				return;
			}
			
			//andrew220328 it's not guarantee threadsafe, as test null and synchronized in 2 statement. but should be good enough
			synchronized(processScanLogObject) {
				processScanLogObject.notify();
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void dumpCSMap() {
		try {
			ZkUtil.dumpData("csMap", csMap);
			ZkUtil.dumpData("csMapDirtyFlag", csMapDirtyFlag);
			ZkUtil.dumpData("csMapDirtyFlags", csMapDirtyFlags);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}

	public static void removeCSMapItem(String cardNo) {
		if (StringUtils.isBlank(cardNo)) {
			UniLog.log1("card no is blank, ignore");
			return;
		}
		UniLog.log1("remove CsMap item cardNo:%s", cardNo);
		csMap.remove(cardNo);
	}
	
	/***
	 * Update card status map
	 * called by cronjob or button
	 * 
	 * It only keep the current card status in a hashmap. 
	 * As the logic of mark attednance/token balance is keep unchange. DO NOT update any balance / attednance related record.
	 * 
	 * @param forceUpdate - true:update the map and ignore to check dirty flag
	 * @return
	 */
	private static synchronized ReturnMsg updateCardStatusMap(SessionHelper sh) {
		if (!allowCheckCardStatus.get()) {
			UniLog.log1("function disabled");
			return ReturnMsg.defaultOk;
		}
		
		//It's a slow task, update it when data is dirty
		if (!csMapDirtyFlag.get() && csMapDirtyFlags.size() == 0) {
			UniLog.log1("function ignore dirtyFlag:%s", csMapDirtyFlag.get());
			return ReturnMsg.defaultOk;
		}
		UniLog.log1("update csMap start");
		StopWatchHelper sw = new StopWatchHelper("updateCardStatusMap");
		
		//At this moment, can update the whole map one by one.
		//As the update time maybe long, need to make sure the map is still accessible during update
		//Later enhancement can update the affected data (optional, for performance concern only) 
		
		if (csMapDirtyFlag.get()) {
			UniLog.log1("update csMap all start");
			csMapDirtyFlags.clear();
			BiResult brStudent = null;
			BiResult brTokenBal = null;
			try {
				brStudent = BiResultHelper.create(sh, "edu.Student", "essd_cardno <> ''", -1, null);
				brTokenBal = brStudent.getSubLink("edu.TokenBal");
				while (brStudent.next(false)) {
					String cardNo = brStudent.getCellString("essd_cardno");
					if (!StringUtils.equals(brStudent.getCellString("essd_status"), "Cancelled")) {
						boolean hasNegativeBalance = false;
						for (BiCellCollection bc : brTokenBal.getRowCollectionList()) {
							double balance = bc.getCellDouble("tkbal_ostqty");
							if (balance < 0) {
								hasNegativeBalance = true;
								break;
							}
						}
						csMap.put(cardNo, hasNegativeBalance ? CardStatus.NEGATIVE_BALANCE : CardStatus.VALID);
					}
					else
						csMap.put(cardNo, CardStatus.STUDENT_CANCELLED);
				}
			}
			catch (Exception e) {
				UniLog.log("update csMap all error:" + e.toString());
				csMapDirtyFlag.set(false);
				return new ReturnMsg(e);
			}
			finally {
				if (brTokenBal != null)
					brTokenBal.close();
				if (brStudent != null)
					brStudent.close();
			}
			UniLog.log1("update csMap all end");
			csMapDirtyFlag.set(false);
		}
		else {
			//TODO handle the csMapDirtyFlags too. dummy code for clear the dirtyFlags
			for (Iterator<Map.Entry<String, String>> it = csMapDirtyFlags.entrySet().iterator(); it.hasNext();) {
				String cardNo = it.next().getKey();
				if (StringUtils.isBlank(cardNo)) {
					it.remove();
					continue;
				}
				UniLog.log1("update csMap cardNo:%s start", cardNo);
				BiResult brStudent = null;
				BiResult brTokenBal = null;
				try {
					brStudent = BiResultHelper.create(sh, "edu.Student", String.format("essd_cardno = '%s'", cardNo), -1, null);
					brTokenBal = brStudent.getSubLink("edu.TokenBal");
					if (brStudent.next(false)) {
						if (!StringUtils.equals(brStudent.getCellString("essd_status"), "Cancelled")) {
							boolean hasNegativeBalance = false;
							for (BiCellCollection bc : brTokenBal.getRowCollectionList()) {
								double balance = bc.getCellDouble("tkbal_ostqty");
								if (balance < 0) {
									hasNegativeBalance = true;
									break;
								}
							}
							UniLog.log1("update csMap cardNo:%s hasNegativeBalance:%b", cardNo, hasNegativeBalance);
							csMap.put(cardNo, hasNegativeBalance ? CardStatus.NEGATIVE_BALANCE : CardStatus.VALID);
						}
						else {
							UniLog.log1("update csMap cardNo:%s STUDENT_CANCELLED", cardNo);
							csMap.put(cardNo, CardStatus.STUDENT_CANCELLED);
						}
					}
					else {
						UniLog.log1("update csMap cardNo:%s CARDNO_NOT_FOUND", cardNo);
						csMap.put(cardNo, CardStatus.CARDNO_NOT_FOUND);
					}
				}
				catch (Exception e) {
					UniLog.log("update csMap all error:" + e.toString());
					return new ReturnMsg(e);
				}
				finally {
					if (brTokenBal != null)
						brTokenBal.close();
					if (brStudent != null)
						brStudent.close();
				}
				UniLog.log1("update csMap cardNo:%s end", cardNo);
				it.remove();
			}
		}

		//ZkUtil.dumpData(csMap);  //debug
		sw.stop();
		UniLog.log1("update csMap end");

		return ReturnMsg.defaultOk;
	}
}