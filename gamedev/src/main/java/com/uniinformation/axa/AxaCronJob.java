package com.uniinformation.axa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.axa.BiResultAxaClaim;
import com.uniinformation.cron.CronJob;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.CloseUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.axa.ZkBiComposerAxaClaim;

public class AxaCronJob extends CronJob {

//	static public final int DATE0 = -14;
	static public final int DATE0 = -1;
//	static public final int DATE1 = 14;
	static public final int DATE1 = 1;
	BiSchema schema;
	SessionHelper sp;
	BiResultAxaClaim br;
	String uploadControlFile = "/tmp/nextUpload.txt";
	boolean realSendEmail=false;
	
	/*
	 * account@pedderhealth.com
	 * fionali@pedderhealth.com
	 */
	
	ReturnMsg doSubmitClaimFile(Date dd,Date td) throws Exception
	{
				br.clear();
				br.clearCondition();
				dd = DateUtil.nextday(td,DATE0);
				br.addCustomCondition("axaclm_status='Confirmed' and axaclm_totclaim > 0 and axaclm_date < '" + DateUtil.toDateString(dd, "yyyy/mm/dd")+"'");
				br.query();
				UniLog.log("Total " + br.getRowCount() + " records to upload ");
				
				HashMap<String,Object> dataHM = new HashMap();
				try {
					dataHM.clear();
					for (String id : Arrays.asList("1","04","7")) {
						OutputStream os = new ByteArrayOutputStream();
						dataHM.put("os" + id, os);
						dataHM.put("bw" + id, new BufferedWriter(new OutputStreamWriter(os,"ISO-8859-1")));
					}
					String inList = null;
					for(int p_recIdx = 0;p_recIdx < br.getRowCount();p_recIdx++) {
						boolean ok = br.fetchOneRecV(p_recIdx);
						if(!ok) {
							UniLog.log("Something Wrong, fetch record for claimUpload Failed, rechedule for next trial");
							return(ReturnMsg.defaultFail);
						}
						Vector<BiCellCollection> vv = br.getSubLink("axa.AxaClaimDet").getRowCollectionList();
						for(BiCellCollection bc : vv)		 {
							((BiResultAxaClaim) br).writeOneClaimRecord(dataHM,bc);
						}
						br.getCell("axaclm_submitdate").set(td);
						ReturnMsg rtn = br.updateCurrent();
						if(rtn != null && !rtn.getStatus()) {
							UniLog.log("Update Current Failed " + rtn == null ? "null" : rtn.getMsg());
							UniLog.log("Upload Aborted");
							return(ReturnMsg.defaultFail);
						}
						int sid = br.getCurrentCollection().getSid();
						if(inList == null) inList = " serial_id in (" + sid; 
							else inList += " ," + sid; 
					}

					CloseUtil.flush(dataHM.get("bw1"),dataHM.get("bw04"),dataHM.get("bw7"));
							
					int okCnt = 0;
					int failCnt = 0;
					for (String id : Arrays.asList("1","04","7")) {
						String outFileName = null;
						try {
							outFileName = (String) ZkBiComposerAxaClaim.class.getDeclaredField("CLAIM_FFMT_" + id).get(null);
						} catch(Exception ex) { }
						if (outFileName == null) {
							UniLog.log1("outFileName not avialble. id:%s",id);
							continue;
						}
						ByteArrayOutputStream os = (ByteArrayOutputStream) dataHM.get("os" + id);
						byte[] ba = os.toByteArray();
						if (ba.length <= 0) {
							continue;
						}
						Date minDate = (Date) dataHM.get("minDate" + id);
						Date maxDate = (Date) dataHM.get("maxDate" + id);
						String minDateStr = minDate == null ? "" : DateUtil.toDateString(minDate, "yyyymmdd");
						String maxDateStr = maxDate == null ? "" : DateUtil.toDateString(maxDate, "yyyymmdd");
								
						//ReturnMsg sendRtn = AxaUtil.sendEmail(String.format("UAT Claims File from PEDD on %s-%s",minDateStr,maxDateStr), outFileName, ba);
						
						if(realSendEmail) {
							ReturnMsg sendRtn = AxaUtil.sendEmailRemote(String.format("Claims File from PEDD on %s-%s",minDateStr,maxDateStr), outFileName, ba);
							UniLog.log1("sendRtn:" + sendRtn);
							if (sendRtn.getStatus()) okCnt++; else failCnt++;
						} else {
							UniLog.log("Skip real send email");
						}
					}
					UniLog.log(String.format("Email submit ok:%d fail:%d",okCnt, failCnt));
					
					SelectUtil su = schema.getSelectUtil();
//					TableRec tr = su.getQueryResult("select * from locationcode where loc_cocode = '"+p_cocode+"'");
					if(inList != null) su.executeUpdate("update axaclaim set axaclm_status='Submitted' where " + inList + ")" , null);
		
					dd = getNextUploadTime();
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sp.newErpFileOutputStream(uploadControlFile)));
					writer.write(DateUtil.dateToDateTimeStr(dd));
					writer.close();
					ReturnMsg rtn = new ReturnMsg(true);
					rtn.setData(inList);
					return(rtn);
					
				} catch (Exception ex) {
					UniLog.log(ex);
					CloseUtil.close(dataHM.get("bw1"),dataHM.get("bw04"),dataHM.get("bw7"));
					return(ReturnMsg.defaultFail);
				}
	}
	
	@Override
	public int runOnce() throws Exception {
		// TODO Auto-generated method stub
		UniLog.log("AxaCronJob run once");
		/*
		InputStream fis = sp.newErpFileInputStream("/tmp/nextUpload.txt");
		TextInputStream tis 
		*/
		BufferedReader reader;
//		reader = new BufferedReader(new InputStreamReader(sp.newErpFileInputStream("/tmp/nextUpload.txt")));
		reader = new BufferedReader(new InputStreamReader(sp.newErpFileInputStream(uploadControlFile)));
		String nextUpload = reader.readLine();
		reader.close();
		if(!StringUtils.isBlank(nextUpload)) {
			Date dd = DateUtil.dateTimeStrToDate(nextUpload);
			UniLog.log("Next Update At " + dd.toString());
			Date td = DateUtil.today();
			if(dd.before(td)) {
				UniLog.log("Times up, execute claim upload");

				/*
				{
					dd = getNextUploadTime();
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sp.newErpFileOutputStream(uploadControlFile)));
					writer.write(DateUtil.dateToDateTimeStr(dd));
					writer.close();
					byte excelStmt[] = br.exportStatementToExcel(td, null);
					if(excelStmt != null) UniLog.log("genstatement got " + excelStmt.length  + " bytes"); 
				}
				*/
				
				{
				ReturnMsg rtn = doSubmitClaimFile(dd,td);
				if(rtn.getStatus()) {
					if(rtn.getData() != null) {
						UniLog.log("has submited records,process claime statement");
 						byte excelStmt[] = br.exportStatementToExcel(td, null);
						if(excelStmt != null) {
							UniLog.log("genstatement got " + excelStmt.length  + " bytes"); 
							AxaUtil.sendEmailRemote(
								String.format("Clame Statement from PEDD submitted on %s - %s",
									DateUtil.toDateString(td, "yyyy/mm/dd"),
									DateUtil.toDateString(td, "yyyy/mm/dd")
								),
								String.format("Clame_Statement_%s_%s.xlsx",
									DateUtil.toDateString(td, "yyyymmdd"),
									DateUtil.toDateString(td, "yyyymmdd")
								)
								, excelStmt,
									"ahk_hk_sm_paneleclaims@axa.com.hk,account@pedderhealth.com,fionali@pedderhealth.com"
								);
						}
	
					}
				}
				}
				UniLog.log("Axa Cron Job Completed");
				
			}
		}
		return 0;
	}
	
	
	public int runOnceXX() throws Exception {
		// TODO Auto-generated method stub
		UniLog.log("AxaCronJob run once");
		/*
		InputStream fis = sp.newErpFileInputStream("/tmp/nextUpload.txt");
		TextInputStream tis 
		*/
		BufferedReader reader;
//		reader = new BufferedReader(new InputStreamReader(sp.newErpFileInputStream("/tmp/nextUpload.txt")));
		reader = new BufferedReader(new InputStreamReader(sp.newErpFileInputStream(uploadControlFile)));
		String nextUpload = reader.readLine();
		reader.close();
		if(!StringUtils.isBlank(nextUpload)) {
			Date dd = DateUtil.dateTimeStrToDate(nextUpload);
			UniLog.log("Next Update At " + dd.toString());
			Date td = DateUtil.today();
			if(dd.before(td)) {
				UniLog.log("Times up, execute claim upload");
				
				ReturnMsg rtn = doSubmitClaimFile(dd,td);
				if(rtn.getStatus()) {
					if(rtn.getData() != null) {
						byte excelStmt[] = br.exportStatementToExcel(td, null);
						if(excelStmt != null) UniLog.log("genstatement got " + excelStmt.length  + " bytes"); 
					}
				}
				UniLog.log("Axa Cron Job Completed");
				
//				br.clear();
//				br.clearCondition();
//				dd = DateUtil.nextday(td,DATE0);
//				br.addCustomCondition("axaclm_status='Confirmed' and axaclm_totclaim > 0 and axaclm_date < '" + DateUtil.toDateString(dd, "yyyy/mm/dd")+"'");
//				br.query();
//				UniLog.log("Total " + br.getRowCount() + " records to upload ");
//				
//				HashMap<String,Object> dataHM = new HashMap();
//				try {
//					dataHM.clear();
//					for (String id : Arrays.asList("1","04","7")) {
//						OutputStream os = new ByteArrayOutputStream();
//						dataHM.put("os" + id, os);
//						dataHM.put("bw" + id, new BufferedWriter(new OutputStreamWriter(os,"ISO-8859-1")));
//					}
//					String inList = null;
//					for(int p_recIdx = 0;p_recIdx < br.getRowCount();p_recIdx++) {
//						boolean ok = br.fetchOneRecV(p_recIdx);
//						if(!ok) {
//							UniLog.log("Something Wrong, fetch record for claimUpload Failed, rechedule for next trial");
//							return(0);
//						}
//						Vector<BiCellCollection> vv = br.getSubLink("axa.AxaClaimDet").getRowCollectionList();
//						for(BiCellCollection bc : vv)		 {
//							((BiResultAxaClaim) br).writeOneClaimRecord(dataHM,bc);
//						}
//						br.getCell("axaclm_submitdate").set(td);
//						ReturnMsg rtn = br.updateCurrent();
//						if(rtn != null && !rtn.getStatus()) {
//							UniLog.log("Update Current Failed " + rtn == null ? "null" : rtn.getMsg());
//							UniLog.log("Upload Aborted");
//							return(0);
//						}
//						int sid = br.getCurrentCollection().getSid();
//						if(inList == null) inList = " serial_id in (" + sid; 
//							else inList += " ," + sid; 
//					}
//
//					CloseUtil.flush(dataHM.get("bw1"),dataHM.get("bw04"),dataHM.get("bw7"));
//							
//					int okCnt = 0;
//					int failCnt = 0;
//					for (String id : Arrays.asList("1","04","7")) {
//						String outFileName = null;
//						try {
//							outFileName = (String) ZkBiComposerAxaClaim.class.getDeclaredField("CLAIM_FFMT_" + id).get(null);
//						} catch(Exception ex) { }
//						if (outFileName == null) {
//							UniLog.log1("outFileName not avialble. id:%s",id);
//							continue;
//						}
//						ByteArrayOutputStream os = (ByteArrayOutputStream) dataHM.get("os" + id);
//						byte[] ba = os.toByteArray();
//						if (ba.length <= 0) {
//							continue;
//						}
//						Date minDate = (Date) dataHM.get("minDate" + id);
//						Date maxDate = (Date) dataHM.get("maxDate" + id);
//						String minDateStr = minDate == null ? "" : DateUtil.toDateString(minDate, "yyyymmdd");
//						String maxDateStr = maxDate == null ? "" : DateUtil.toDateString(maxDate, "yyyymmdd");
//								
//						//ReturnMsg sendRtn = AxaUtil.sendEmail(String.format("UAT Claims File from PEDD on %s-%s",minDateStr,maxDateStr), outFileName, ba);
//						
//						if(realSendEmail) {
//							ReturnMsg sendRtn = AxaUtil.sendEmailRemote(String.format("Claims File from PEDD on %s-%s",minDateStr,maxDateStr), outFileName, ba);
//							UniLog.log1("sendRtn:" + sendRtn);
//							if (sendRtn.getStatus()) okCnt++; else failCnt++;
//						} else {
//							UniLog.log("Skip real send email");
//						}
//					}
//					UniLog.log(String.format("Email submit ok:%d fail:%d",okCnt, failCnt));
//					
//					SelectUtil su = schema.getSelectUtil();
//					//TableRec tr = su.getQueryResult("select * from locationcode where loc_cocode = '"+p_cocode+"'");
//					if(inList != null) su.executeUpdate("update axaclaim set axaclm_status='Submitted' where " + inList + ")" , null);
//		
//					dd = getNextUploadTime();
//					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sp.newErpFileOutputStream(uploadControlFile)));
//					writer.write(DateUtil.dateToDateTimeStr(dd));
//					writer.close();
//					
//				} catch (Exception ex) {
//					UniLog.log(ex);
//					CloseUtil.close(dataHM.get("bw1"),dataHM.get("bw04"),dataHM.get("bw7"));
//					return(0);
//				}
			}
		}
		return 0;
	}

	@Override
	public void setSessionHelper(SessionHelper p_sh) throws Exception {
		// TODO Auto-generated method stub
		UniLog.log("AxaCronJob setSessionHelper Initiated");
		sp = p_sh;
		schema = BiSchema.loadSchema(sp);
		br = (BiResultAxaClaim) schema.getViewByName("axa.AxaClaim").newBiResult(sp.getLoginId(), null, null, sp);
		realSendEmail = "Y".equals(Erpv4Config.getString(p_sh, "AxaRealSendEmail"));
		String ss = Erpv4Config.getString(p_sh, "AxaUploadControlFile");
		if(!StringUtils.isBlank(ss)) {
			uploadControlFile = ss;
		}
		UniLog.log("AxaCronJob setSessionHelper realSendEmail = "+realSendEmail) ;
		UniLog.log("AxaCronJob setSessionHelper uploadControlFile = "+uploadControlFile) ;
	}
	
	Date getNextUploadTime() {
		Date dd = DateUtil.nextday(DateUtil.today(),DATE1);
		/*
		for(int i=0;i<20;dd = DateUtil.nextday(dd,1),i++) {
			int mday = DateUtil.getDay(dd);
			switch(mday) {
			case 1:
			case 15 : return(dd);
			}
		}
		*/
		return(dd);
				
	}

}
