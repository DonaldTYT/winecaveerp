package com.uniinformation.jxapp;

import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.utils.UniLog;

public class AntiqueAdmin {
	private static Map<String, Set<Integer>> gSelectedMap = new ConcurrentHashMap<String, Set<Integer>>(); //key: secretKey, value: antique id list
	
	/*
	 * list: antique id list
	 * */
	public static String addSelectedList(String key, Set<Integer> list) throws Exception{
		synchronized (gSelectedMap) {
			//caller provide key
			String secretKey = StringUtils.isNotBlank(key) ? key : genKey();
			if (gSelectedMap.get(secretKey) == null) {
				gSelectedMap.put(secretKey, list);
				return secretKey;
			}
			else {
				UniLog.log1("fail duplicate key");
				throw new Exception("duplicate key");
			}
		}
	}
	private static String genKey() {
			String secretKey;
			StringBuilder sb = new StringBuilder();
			Random r = new Random();
			do {
				sb.setLength(0);
				for (int i = 0; i < 13; i++) {
					int rn = r.nextInt(26);
					sb.append((char)('A' + rn));
				}
				secretKey = sb.toString();
			} while (gSelectedMap.containsKey(secretKey));
			return secretKey;
	}
	
	public static Set<Integer> getSelectedList(String secretKey) {
		return gSelectedMap.get(secretKey);
	}
}
