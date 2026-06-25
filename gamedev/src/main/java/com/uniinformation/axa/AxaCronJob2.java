package com.uniinformation.axa;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.axa.BiResultAxaClaim;
import com.uniinformation.cron.CronJob;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class AxaCronJob2 extends CronJob {

//	static public final int DATE0 = -14;
//	static public final int DATE0 = -1;
//	static public final int DATE1 = 14;
//	static public final int DATE1 = 1;
	BiSchema schema;
	SessionHelper sp;
	BiResult br;
	String syncControlFile = "/tmp/nextSyncNotice.txt";
//	long syncWaitTime = 86400000L;
	long syncWaitTime = 7200000L;
	
	ReturnMsg doSyncEmailNotive() {
		try {
			return( AxaUtilEx.getEmailNotice(br,"imap.gmail.com", "tyt92791082@gmail.com", "ykvsymvlllpojzsi", false, 1000,true));
		} catch (Exception ex) {
			UniLog.log(ex);
			return( new ReturnMsg(false,ex.toString()));
		}
	}
	@Override
	public int runOnce() throws Exception {
		// TODO Auto-generated method stub
		UniLog.log("AxaCronJob SyncNotice run once");
		/*
		InputStream fis = sp.newErpFileInputStream("/tmp/nextUpload.txt");
		TextInputStream tis 
		*/
		BufferedReader reader;
//		reader = new BufferedReader(new InputStreamReader(sp.newErpFileInputStream("/tmp/nextUpload.txt")));
		reader = new BufferedReader(new InputStreamReader(sp.newErpFileInputStream(syncControlFile)));
		String nextUpload = reader.readLine();
		reader.close();
		if(!StringUtils.isBlank(nextUpload)) {
			Date dd = DateUtil.dateTimeStrToDate(nextUpload);
			Date td = new Date();
			if(!dd.after(td)) {
				UniLog.log("AxaCronJob SyncNotice Times up, sync email notice");
				ReturnMsg rtn = doSyncEmailNotive();
				if(rtn == null || rtn.getStatus()) {
					dd = getNextUploadTime();
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sp.newErpFileOutputStream(syncControlFile)));
					writer.write(DateUtil.dateToDateTimeStr(dd));
					writer.close();
				}
				UniLog.log("AxaCronJob SyncNotice Completed. Next Sync At " + dd.toString());
				
			} else {
				UniLog.log("AxaCronJob SyncNotice Wait Until " + dd.toString());
			}
		}
		return 0;
	}
	

	@Override
	public void setSessionHelper(SessionHelper p_sh) throws Exception {
		// TODO Auto-generated method stub
		UniLog.log("AxaCronJob SyncNotice setSessionHelper Initiated");
		sp = p_sh;
		schema = BiSchema.loadSchema(sp);
		br = schema.getViewByName("axa.EmailMessage").newBiResult(sp.getLoginId(), null, null, sp);
		String ss = Erpv4Config.getString(p_sh, "AxaSyncNoticeControlFile");
		if(!StringUtils.isBlank(ss)) {
			syncControlFile = ss;
		}
		UniLog.log("AxaCronJob SynNotice setSessionHelper syncControlFile = "+syncControlFile) ;
	}
	
	Date getNextUploadTime() {
//		Date dd = DateUtil.nextday(DateUtil.today(),DATE1);
		Date dd = new Date( new Date().getTime() + syncWaitTime);
		
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
