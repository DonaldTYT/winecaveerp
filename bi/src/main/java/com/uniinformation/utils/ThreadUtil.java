package com.uniinformation.utils;

public class ThreadUtil {
	public static void sleep(long p_delay) {
		UniLog.log1("sleeping... %d", p_delay);
		try{    
			Thread.sleep(p_delay);
		}
		catch(Exception ex) {
			//ex.printStackTrace();
		}
	}

}
