package com.uniinformation.webcore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.BiUtil;

public class BanIpHelper {
	private static Hashtable<String ,List<IpEntry>> banIpHt = new Hashtable<String, List<IpEntry>>(); //ip, last attempt ts
	private static HashSet<String> whiteListHs = new HashSet<String>();
	private static class IpEntry{
		public String ip;
		public long expirationTime;
		public IpEntry(String p_ip, long p_expirationTime){
			ip = p_ip;
			expirationTime = p_expirationTime;
		}
	}
	public static void clearIp(String p_ip){
		synchronized(banIpHt){
			UniLog.logm(null, "%s", p_ip);
			if (p_ip == null || p_ip.trim().equals("")){
				UniLog.logm(null, "ip is null, ignore");
				return;
			}
			List<IpEntry> banIpList = banIpHt.get(p_ip);
			if (banIpList != null){
				banIpList.clear();
			}
		}
	}
	public static int addIp(String p_ip, long p_banDur){
		synchronized(banIpHt){
			UniLog.logm(null, "%s,%d", p_ip, p_banDur);
			if (p_ip == null || p_ip.trim().equals("")){
				UniLog.logm(null, "ip is null, ignore");
				return(0);
			}
			if (whiteListHs.contains(p_ip)) {
				UniLog.log1("ip[%s] in whitelist, ignore", p_ip);
				return(0);
			}
			
			//step 1: add ban entry
			long curTime = (new java.util.Date().getTime()) / 1000;
			List<IpEntry> banIpList = banIpHt.get(p_ip);
			if (banIpList == null){
				banIpList = new ArrayList<IpEntry>();
				banIpHt.put(p_ip, banIpList);
			}
			if (p_banDur > 0){
				banIpList.add(new IpEntry(p_ip, curTime + p_banDur));
			}
			
			//step 2: remove expired entry, and count valid entry
			int banCnt = 0;
			ListIterator<IpEntry> iter = banIpList.listIterator();
			while(iter.hasNext()){
				IpEntry entry = iter.next();
				long remain = entry.expirationTime - curTime;
				if (remain <= 0){
					UniLog.logm(null, "remove expired entry:%s remain:%d", entry.ip, remain);
					iter.remove();
				}
				else{
					banCnt++;
					UniLog.logm(null, "valid entry:%s remain:%d", entry.ip, remain);
				}
			}
			UniLog.logm(null, "return banCnt:%s", banCnt);
			return(banCnt);
		}
	}
	public static void addWhiteList(String p_ip) {
		if (StringUtils.isBlank(p_ip)) {
			UniLog.log1("ip is blank");
			return;
		}
		synchronized(whiteListHs) {
			whiteListHs.add(p_ip);
		}
	}
	public static void removeWhiteList(String p_ip) {
		if (StringUtils.isBlank(p_ip)) {
			UniLog.log1("ip is blank");
			return;
		}
		synchronized(whiteListHs) {
			whiteListHs.remove(p_ip);
		}
	}
	/*
	public static void main(String args[]) throws Exception{
		addWhiteList("abc");
		addWhiteList("def");
		addWhiteList("abc");
		removeWhiteList("abc");
		ZkUtil.dumpData(whiteListHs);
		UniLog.log1("check:" + whiteListHs.contains("abc"));
		UniLog.log1("check:" + whiteListHs.contains("def"));
		UniLog.log1("check:" + whiteListHs.contains("def2"));
		
		addWhiteList("1112");
		addIp("111", 2);
		addIp("111", 2);
	}
	*/
	/*
	public static void main(String args[]) throws Exception{
		addIp("111", 2);
		clearIp("111");
		addIp("111", 5);
		for (int i=0; i<10; i++){
			UniLog.log("HAHA:" + i);
			addIp("111", 0);
			Thread.sleep(1000);
		}
		
	}
	*/
}
