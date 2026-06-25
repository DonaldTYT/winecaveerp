package com.uniinformation.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

/***
 * regular express list (experimental) with the following features:
 * - find against string content 
 * e.g. item1: pattern[0-4] value:hello1
 *      item2: pattern[5-9] value:hello2
 *      find("pattern5") will return hello2
 *      
 * - findAndReplace against string content
 * e.g. item1: (pattern[0-4]) value:hello $1
 *      item2: (pattern[5-9]) value:hello $1
 *      find("pattern5") will return "hello pattern5"
 * 
 * - threadsafe
 * - cache based on string content
 * 
 * builded for for message translation
 * 
 * @author andrew
 * 
 * @param <V>
 */
public class RegExpList<V> {
	public static boolean fDebug = false;
	
	final static int defaultCacheSize = 10000;
	public enum FindMode { FIRST, LAST, PATTERN_LENGTH };
	private Map<String,PatternItem<V>> patternCache = null;   //key: findMode:findstr, value: PatternItem, a pointer to patternList.PatternItem
	private List<PatternItem<V>> patternList = null;
	
	
	
	public static class PatternItem<V>{
		public Pattern pattern = null;
		public String patternStr = null;
		public int patternStrLeng = 0;
		public V value = null;
		
		private boolean isValid = true; //2 possible invalidate case: 1 - key not defined, 2 - pattern compile error
		public static <T> PatternItem<T> of(Pattern pattern, String patternStr, T value) {
			PatternItem<T> item = new PatternItem<T>();
			item.pattern = pattern;
			item.patternStr = patternStr;
			item.value = value;
			if (patternStr instanceof String) {
				item.patternStrLeng = patternStr.length();
			}
			return item;
		}
		public boolean isValid() {
			return isValid;
		}
		public PatternItem<V> invalidate() {
			isValid = false;
			return this;
		}
		
		@Override
		public String toString() {
			try {
				return String.format("pattern:%s patternStr:%s value:%s", pattern, patternStr, value);
			}
			catch(Exception ex) {
				ex.printStackTrace();
				return "";
			}
		}
	}
	/***
	 * default constructor
	 */
	public RegExpList() {
		this(true, defaultCacheSize);
	}
	public RegExpList(boolean p_threadSafe) {
		this(p_threadSafe, defaultCacheSize);
	}
	public RegExpList(int p_cacheSize) {
		this(true, p_cacheSize);
	}
	
	/***
	 * constructor
	 * @param p_threadSafe - threadsafe is costly, non-threadsafe require external lock in multithread env
	 * @param p_cache - cache the find result. good for limited number of result variation
	 */
	public RegExpList(boolean p_threadSafe, int p_cacheSize) {
		UniLog.log1("RegExpList threadSafe:%s cache:%d", p_threadSafe, p_cacheSize);
		if (p_threadSafe) {
			patternList = new CopyOnWriteArrayList<PatternItem<V>>();  //Triple<Compiled Pattern, Pattern, Value>
		}
		else {
			patternList = new ArrayList<PatternItem<V>>();  //Triple<Compiled Pattern, Pattern, Value>
		}
		if (p_cacheSize > 0) {
			//resultCache = new ConcurrentHashMap<String,V>();
			patternCache = MapUtil.createLRUMap(p_cacheSize);
		}
	}
	
	

	/***
	 * add to reg to patternList
	 * @param p_reg
	 * @param p_val
	 * @param p_removeDup - remove old entry (false better performace)
	 */
	private void add(String p_reg, V p_val, boolean p_removeDup) {
		if (fDebug) UniLog.log1("add reg:%s val:%s removeDup:%s", p_reg, p_val, p_removeDup);
		if (StringUtils.isBlank(p_reg)) {
			UniLog.log1("reg is blank");
			return;
		}
		if (p_removeDup) {
			remove(p_reg);
		}
		patternList.add(buildItem(p_reg, p_val));
		clearCache();
	}
	
	public void add(String p_reg, V p_val){
	    add(p_reg, p_val, true);
	}
	
	
	/***
	 * batch add 
	 * - much pattern performance for CopyOnWriteArrayList
	 * - without duplicate check
	 * @param p_newList
	 */
	public void add(List <PatternItem<V>> p_newList) {
		patternList.addAll(p_newList);
		clearCache();
	}
	
	
	private static String buildCacheKey(FindMode p_findMode, String p_findStr) {
		return p_findMode + ":" + p_findStr;
	}
	/***
	 * cache operation
	 * @param p_findStr
	 */
	public void removeCache(FindMode p_findMode, String p_findStr) {
		if (patternCache == null) return;
		if (fDebug) UniLog.log1("remove cache. key:%s", buildCacheKey(p_findMode,p_findStr));
		patternCache.remove(buildCacheKey(p_findMode,p_findStr));
	}
	public void clearCache() {
		if (patternCache == null) return;
		if (fDebug) UniLog.log1("clear cache");
		patternCache.clear();
	}
	public void addCache(FindMode p_findMode, String p_findStr, PatternItem<V> p_tri) {
		if (patternCache == null) return;
		if (fDebug) UniLog.log1("add cache. key:%s", buildCacheKey(p_findMode,p_findStr));
		patternCache.put(buildCacheKey(p_findMode,p_findStr), p_tri);
		
	}
	public PatternItem<V> getCache(FindMode p_findMode, String p_findStr) {
		if (patternCache == null) return null;
		return patternCache.get(buildCacheKey(p_findMode,p_findStr));
	}
	
	
	/***
	 * for create patternList item
	 * @param p_reg
	 * @param p_v
	 * @return
	 */
	public static <T> PatternItem<T> buildItem(String p_reg, T p_v){
		if (StringUtils.isBlank(p_reg) || p_v == null) {
			return PatternItem.of(null, p_reg, p_v).invalidate();
		}
		try {
			return PatternItem.of(Pattern.compile(p_reg), p_reg, p_v);
		}
		catch(Exception ex) {
			UniLog.log1("error %s %s ",p_reg, ex.getMessage());
			return PatternItem.of(null, p_reg, p_v).invalidate();
		}
	}
	
	
	/***
	 * remove reg from patternList
	 * @param p_reg
	 */
	public int remove(String p_reg) {
		if (fDebug) UniLog.log1("remove reg:%s", p_reg);
		int removeCnt = 0;
		if (StringUtils.isBlank(p_reg)) {
			UniLog.log1("reg is blank");
			return removeCnt;
		}
		for (PatternItem<V> patternItem : patternList) {
			if (p_reg.equals(patternItem.patternStr)){
				if (fDebug) UniLog.log1("found item");
				patternList.remove(patternItem);
				removeCnt++;
			}
		}
		if (fDebug) UniLog.log1("removeCnt:%d", removeCnt);
		if (removeCnt > 0) {
			clearCache();  //remove cannot identify findstr, so need to clear all cache
		}
		return removeCnt;
	}
	

	public int size() {
		return patternList.size();
	}

	
	/***
	 * search value by pattern matching
	 * 
	 * @param p_findStr
	 * @param p_matchFirstFlag true: if pattern is not unique, return the first matched entry
	 * @return
	 */
	public PatternItem<V> find(String p_findStr, FindMode p_findMode) {
		if (p_findStr == null) {
			return null;
		}
		
		//obtain from cache
		PatternItem<V> cacheValue = getCache(p_findMode,p_findStr);
		if (cacheValue != null) {
			if (cacheValue.isValid) {
				if (fDebug) UniLog.log1("cache is valid, return null");
				return cacheValue;
			}
			else {
				if (fDebug) UniLog.log1("cache is invalid, return null");
				return null;
			}
		}
		
		
		//sequential search 
		PatternItem<V> resultPatternItem = null;
		int maxPatternLeng = -1;
		int matchIdx = 0;
		for (PatternItem<V> patternItem : patternList) {
			matchIdx++;
			if (patternItem.isValid && patternItem.pattern.matcher(p_findStr).find()) {
				if (fDebug) UniLog.log1("matched: idx:"+ matchIdx);
				if (p_findMode == FindMode.FIRST) {
					resultPatternItem = patternItem;
					break;
				}
				else if (p_findMode == FindMode.LAST) {
					resultPatternItem = patternItem;
				}
				else if (p_findMode == FindMode.PATTERN_LENGTH && patternItem.patternStrLeng > maxPatternLeng) {
					resultPatternItem = patternItem;
					maxPatternLeng = patternItem.patternStr.length();
				}
			}
		}
		
		//UniLog.log1("matchIdx: %d",matchIdx);
		if (resultPatternItem != null) {
			addCache(p_findMode, p_findStr, resultPatternItem);  //cache the result by findStr
			return resultPatternItem;
		}
		
		addCache(p_findMode, p_findStr,buildItem(null,(V)null).invalidate()); //indicate record not found
		return null;
	}
	public PatternItem<V> find(String p_findStr) {
		return find(p_findStr, FindMode.FIRST);
	}
	
	/***
	 * search value by pattern matching and then use PatternItem.value for regexp replace
	 * @param p_str
	 * @return
	 */
	public String findAndReplace(String p_findStr, String p_def) {
		if (fDebug) UniLog.log1("findAndReplace findStr:%s", p_findStr);
		PatternItem<V> patternItem = find(p_findStr);
		if (patternItem == null) {
			return p_def;
		}
		Matcher matcher = patternItem.pattern.matcher(p_findStr);
		if (matcher.find() && patternItem.value instanceof String) {
			try {
				return matcher.replaceFirst((String)patternItem.value);  //TODO: can cache this result
			}
			catch(Exception ex) {
				UniLog.log1("error: %s", ex.getMessage());
			}
		}
		return p_def;
	}
	public String findAndReplace(String p_findStr) {
		return findAndReplace(p_findStr, "");
	}

	
	
	public static void concurrentTest() {
		final RegExpList<String> rList = new RegExpList();
		new Thread() {
			public void run(){
				while (true) {
					UniLog.log1("add running. size:%d", rList.size());
					//add one by one
					/*
					for (int i=0; i<10000; i++) {
						int rInt = new Random().nextInt(100000);
						rList.add(String.format("xxx (%d) ([0-9]*)",rInt), String.format("good $1 $2",rInt));
					}
					*/
					//batch add
					List newList = new ArrayList();
					for (int i=0; i<10000; i++) {
						int rInt = new Random().nextInt(100000);
						newList.add(RegExpList.buildItem(String.format("xxx (%d) ([0-9]*)",rInt), String.format("good $1 $2",rInt)));
					}
					rList.add(newList);
					
					
					
					try { Thread.sleep(30000); }catch(Exception ex) {}
				}
			}
		}.start();
		new Thread() {
			public void run(){
				while (true) {
					//StopWatchHelper sw = new StopWatchHelper();
					//if (true) return;
					UniLog.log1("read running. size:%d", rList.size());
					
					int goodCnt = 0;
					int totalCnt = 0;
					//add one by one
					for (int i=0; i<1000; i++) {
						int rInt = new Random().nextInt(1000);
						rInt = i;
						String findStr = String.format("xxx %d %d",rInt, rInt);
						String resultStr = rList.findAndReplace(findStr);
						//UniLog.log1("read size:%d find:%s result:%s", rList.size(), findStr, resultStr);
						if (resultStr.contains("good")) {
							goodCnt++;
						}
						totalCnt++;
					}
					UniLog.log1("read good: %d/%d", goodCnt, totalCnt);
					//sw.stop();
					try { Thread.sleep(500); }catch(Exception ex) {}
				}
			}
		}.start();
		new Thread() {
			public void run(){
				while (true) {
					//if (true) return;
					try { Thread.sleep(500); }catch(Exception ex) {}
					UniLog.log1("delete running. size:%d", rList.size());
					
					int removeCnt = 0;
					int totalCnt = 0;
					//add one by one
					for (int i=0; i<1000; i++) {
						int rInt = new Random().nextInt(1000);
						totalCnt++;
						removeCnt +=rList.remove(String.format("xxx (%d) ([0-9]*)",rInt));
					}
					UniLog.log1("delete good: %d/%d", removeCnt, totalCnt);
				}
			}
		}.start();
		
	}
	public static void selfTest() {
		RegExpList<Object> rList = new RegExpList();
		//rList.fDebug = true;
		rList.add("pattern1", "pattern first");
		rList.add("^pattern[0-9]*$", "pattern longest");
		rList.add("pattern[0-9]*", "pattern last");
		UniLog.log1("test findmode:");
		UniLog.log1("find:pattern1 result:%s",rList.find("pattern1").value);
		UniLog.log1("find:pattern1 result:%s",rList.find("pattern1",FindMode.LAST).value);
		UniLog.log1("find:pattern1 result:%s",rList.find("pattern1",FindMode.PATTERN_LENGTH).value);
		
		UniLog.log1("test findAndReplace:");
		rList = new RegExpList();
		rList.add("(xxx) ([0-9]*)", "newxxx $2");
		rList.add("(yyy) ([0-9]*)", "newyyy $2");
		UniLog.log1("result:%s",rList.findAndReplace("xxx 1"));
		UniLog.log1("result:%s",rList.findAndReplace("xxx 2"));
		UniLog.log1("result:%s",rList.findAndReplace("yyy 3"));
		UniLog.log1("result:%s",rList.findAndReplace("yyy 4"));
		UniLog.log1("result:%s",rList.findAndReplace("zzz 4"));
		rList.add("(zzz) ([0-9]*)", "333 $2");
		UniLog.log1("result:%s",rList.findAndReplace("zzz 4"));
		
		UniLog.log1("test special char");
		rList = new RegExpList();
		rList.add("(xxx) ([0-9]*)(.*)", "111 $2 $3");
		UniLog.log1("result:%s",rList.findAndReplace("xxx 1(kjadlkasd) $1 $2")); //special char in replaceStr
		
		
	}
	
	public static void main(String args[]) {
		selfTest();
		//concurrentTest();
	}
}
