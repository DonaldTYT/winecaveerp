package com.uniinformation.utils;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class FilingUtilObject {
	public String key;
	public int version;
	public String name;
	public String desc;
	public Timestamp cts; //in UTC TZ
	public Timestamp uts; //in UTC TZ
	public Timestamp ots; //in UTC TZ
	public long size = 0;
	
	public FilingUtilObject(String key, int version, String name, String desc, Timestamp cts, Timestamp uts, Timestamp ots, long size) {
		this.key = key;
		this.version = version;
		this.name = name;
		this.desc = desc;
		this.cts = cts;
		this.uts = uts;
		this.ots = ots;
		this.size = size;
	}

	public String toString(){
		try{
			/*
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
			UniLog.log(""+sdf.format(cts));
			UniLog.log(""+sdf.format(uts));
			*/
			
			/*
			//convert to date to solve timezone problem
			TimeZone.setDefault(TimeZone.getTimeZone("Asia/Hong_Kong"));
			UniLog.log("cts to date:"+new java.util.Date(cts.getTime()));
			UniLog.log("uts to date:"+new java.util.Date(uts.getTime()));
			UniLog.log("ots to date:"+new java.util.Date(ots.getTime()));
			*/
			return(XStreamUtil.objToXMLString(this, null));
		}
		catch(Exception ex){
			ex.printStackTrace();
			return("");
		}
	}
}