package com.uniinformation.zkbi.vincero;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.bischema.ExcelCellCollection;
import com.uniinformation.bicore.bischema.ExcelWorkSheetCache;
import com.uniinformation.cron.CronJob;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class VinceroCronJob extends CronJob{
	SessionHelper sh = null;
	final private static int CRON_INTERVAL = 24*3600000; //24 hr
	final private static int CRON_HOUR = 2;
	final private static int CRON_MIN = 0;
	//final private static int CRON_POLLTIME = 300000; //5min
	//final private static int CRON_POLLTIME = 60000; //1min
    final private static int CRON_POLLTIME = 900000;  //15min
	
	private final boolean fEnable = true;
	public static AtomicBoolean fForceRun = new AtomicBoolean(false); //controlled by system tools
	private Date lastTask = null;
	

	@Override
	public int runOnce() throws Exception {
		//UniLog.log1("called");
		
		for (;;) {
			if (!fEnable) {
				UniLog.log1("feature disabled");
				return 0;
			}
			
			Date nextTask = DateUtil.nextTask(lastTask, CRON_INTERVAL, CRON_HOUR, CRON_MIN);
			if (!fForceRun.get() && (nextTask.getTime() > new Date().getTime())) {
				UniLog.log1("waiting interval:%d hour:%d min:%d nextTask:%s forceRun:%s", CRON_INTERVAL, CRON_HOUR, CRON_MIN, nextTask, fForceRun.get());
				return 0;
			}
			try {
				UniLog.log1("wakeup interval:%d hour:%d min:%d nextTask:%s forceRun:%s", CRON_INTERVAL, CRON_HOUR, CRON_MIN, nextTask, fForceRun.get());
				ExcelWorkSheetCache.cacheAllWorkSheet(sh,false);
				ExcelWorkSheetCache.recalAndUpdateAllCache(sh.getBiSchema(),true);
				
				fForceRun.set(false);
				lastTask = new Date();
				UniLog.log1("done");
			}
			catch(Exception ex) {
				UniLog.log1("Exception (delay 30 sec):" + ex.getMessage());
				Thread.sleep(30000);
			}
		}
	}

	@Override
	public void setSessionHelper(SessionHelper p_sh) throws Exception {
		UniLog.log1("called");
		sh = p_sh;
	}
	
	@Override
	public int getPollTime() {
		return CRON_POLLTIME;
	}

}