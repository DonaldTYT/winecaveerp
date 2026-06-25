package com.uniinformation.clerpmulti;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.axa.BiResultAxaClaim;
import com.uniinformation.cron.CronJob;
import com.uniinformation.dynamic.clerpmulti.RunAutoRefillHz;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.DynamicClassLoader;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class ClerpCronJob extends CronJob {

//	static public final int DATE0 = -14;
	static public final int DATE0 = -1;
//	static public final int DATE1 = 14;
	static public final int DATE1 = 1;
	BiSchema schema;
	
	SessionHelper sp;
	BiResultAxaClaim br;
	String uploadControlFile = "/tmp/nextUpload.txt";
	boolean realSendEmail=false;
	@Override
	public int runOnce() throws Exception {
		// TODO Auto-generated method stub
		UniLog.log("ClerpCronJob run once");
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
//			Date td = DateUtil.today();
			Date td = new Date();
			if(dd.before(td)) {

				UniLog.log("Times up, execute HZ refill ");
				dd = DateUtil.nextday(td,DATE0);
				
				try {
							
//					UniLog.log(String.format("Email submit ok:%d fail:%d",okCnt, failCnt));
//					RunAutoRefillHz xx = new RunAutoRefillHz();
//					xx.runCronJob(sp,dd);
					Class[]	paramTypes = new Class[]{};
					RefillHzInterface jpi = (RefillHzInterface) DynamicClassLoader.newInstance("com.uniinformation.dynamic.clerpmulti.RunAutoRefillHz", paramTypes);
					jpi.runRefill(sp, dd);
					dd = getNextUploadTime();
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(sp.newErpFileOutputStream(uploadControlFile)));
					writer.write(DateUtil.dateToDateTimeStr(dd));
					writer.close();
					
				} catch (Exception ex) {
					UniLog.log(ex);
					return(0);
				}
			}
		}
		return 0;
	}

	@Override
	public void setSessionHelper(SessionHelper p_sh) throws Exception {
		// TODO Auto-generated method stub
		UniLog.log("ClerpCronJob setSessionHelper Initiated");
		sp = p_sh;
		schema = BiSchema.loadSchema(sp);
		realSendEmail = "Y".equals(Erpv4Config.getString(p_sh, "ClerpRealSendEmail"));
		String ss = Erpv4Config.getString(p_sh, "ClerpUploadControlFile");
		if(!StringUtils.isBlank(ss)) {
			uploadControlFile = ss;
		}
		UniLog.log("ClerpCronJob setSessionHelper realSendEmail = "+realSendEmail) ;
		UniLog.log("ClerpCronJob setSessionHelper uploadControlFile = "+uploadControlFile) ;
	}
	
	Date getNextUploadTime() {
		Date dd = DateUtil.nextday(DateUtil.today(),DATE1);
		return(dd);
	}

}
