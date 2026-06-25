package com.uniinformation.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

public class MapUtil {
	/***
	 * variable argument of key values (key0,value0,key1,value1)
	 * output map auto cast to Map<Object,Object>
	 * @param keyValues
	 * @return 
	 */
	public static <K, V>Map <K, V>of(Object... keyValues){
	    Map<K, V> map = new HashMap<K, V>();
	    for (int i=0; i<keyValues.length; i+=2) {
	        map.put((K)keyValues[i], (V)keyValues[i+1]);
	    }
	    return map;	
	}

	public static <K, V> Map<K, V> of2(K k, V v, Object... keyValues){
	    Map<K, V> map = new HashMap<K, V>();
	    map.put(k, v);
	    for (int i=0; i<keyValues.length; i+=2)
	        map.put((K)keyValues[i], (V)keyValues[i+1]);
	    return map;	
	}

	public static <K, V1, V2> Map<K, Pair<V1, V2>> ofp(Object... keyValues) {
	    Map<K, Pair<V1, V2>> map = new HashMap<K, Pair<V1, V2>>();
	    for (int i = 0; i < keyValues.length; i+= 3)
	    	map.put((K)keyValues[i], Pair.of((V1)keyValues[i+1], (V2)keyValues[i+2]));
	    return map;
	}

	public static <K, V1, V2, V3> Map<K, Triple<V1, V2, V3>> oft(Object... keyValues) {
	    Map<K, Triple<V1, V2, V3>> map = new HashMap<K, Triple<V1, V2, V3>>();
	    for (int i = 0; i < keyValues.length; i+= 4)
	    	map.put((K)keyValues[i], Triple.of((V1)keyValues[i+1], (V2)keyValues[i+2], (V3)keyValues[i+3]));
	    return map;
	}
	
	public static <K, V> Map<K, List<V>> ofl(int listSize, Object... keyValues) {
	    Map<K, List<V>> map = new HashMap<K, List<V>>();
	    for (int i = 0; i < keyValues.length; i+= listSize + 1) {
	    	List<V> list = new ArrayList<V>();
	    	for (int j = 0; j < listSize; j++)
	    		list.add((V)keyValues[i + 1 + j]);
	    	map.put((K)keyValues[i], list);
	    }
	    return map;
	}
	
	/***
	 * variable argument of key/value pair
	 * output map auto cast to Map<type of key, type of value>
	 * e.g. ofPairs(Pair.of("key1",1), Pair.of("key2",2)) => Map<String,Integer>
	 * @param p_pairs
	 * @return 
	 */
	public static <K, V>Map <K, V>ofPairs(Pair<K,V>...p_pairs){
	    Map<K, V> map = new HashMap<K, V>();
	    for (int i=0; i<p_pairs.length; i++) {
	        map.put((K)p_pairs[i].getKey(), (V)p_pairs[i].getValue());
	    }
	    return map;	
	}
	public static String getString(Object p_map, Object p_key){
		return(getString(p_map, p_key, null));
	}
	public static String getString(Object p_map, Object p_key, String p_defValue){
		if (!(p_map instanceof Map)){
			return(p_defValue);
		}
		Object value = ((Map)p_map).get(p_key);
		return(value == null ? p_defValue : (String)value);
	}
	
	public static Integer getInteger(Object p_map, Object p_key){
		return(getInteger(p_map, p_key, null));
	}
	public static Integer getInteger(Object p_map, Object p_key, Integer p_defValue){
		if (!(p_map instanceof Map)){
			return(p_defValue);
		}
		Object value = ((Map)p_map).get(p_key);
		return(value == null ? p_defValue : (Integer)value);
	}
	
	public static Long getLong(Object p_map, Object p_key){
		return(getLong(p_map, p_key, null));
	}
	public static Long getLong(Object p_map, Object p_key, Long p_defValue){
		if (!(p_map instanceof Map)){
			return(p_defValue);
		}
		Object value = ((Map)p_map).get(p_key);
		if (value instanceof String) { //auto cast from string
			return Long.parseLong((String)value);
		}
		return(value == null ? p_defValue : (Integer)value);
	}
	
	public static Object getObject(Object p_map, Object p_key){
		return(getObject(p_map, p_key, null));
	}
	public static Object getObject(Object p_map, Object p_key, Object p_defValue){
		if (!(p_map instanceof Map)){
			return(p_defValue);
		}
		Object value = ((Map)p_map).get(p_key);
		return(value == null ? p_defValue : value);
	}
	public static Boolean getBoolean(Object p_map, Object p_key){
		return(getBoolean(p_map, p_key, null));
	}
	public static Boolean getBoolean(Object p_map, Object p_key, Boolean p_defValue){
		if (!(p_map instanceof Map)){
			return(p_defValue);
		}
		Object value = ((Map)p_map).get(p_key);
		return(value == null ? p_defValue : (Boolean)value);
	}
	public static void main(String args[]) {
		int cc = 0;;
		Map<String,Integer> map1 = MapUtil.of("key1",111, "key2", 222);
		cc = map1.get("key1");
		UniLog.log1("cc = %d", cc);
		
		cc =  MapUtil.ofPairs(Pair.of("key1",333)).get("key1");
		UniLog.log1("cc = %d", cc);
		
		Map<String,String> lMap = MapUtil.createLRUMap(3);
		lMap.put("1", "1");
		lMap.get("1");
		UniLog.log1("%s", lMap.toString());
		lMap.put("2", "2");
		lMap.get("1");
		UniLog.log1("%s", lMap.toString());
		lMap.put("3", "3");
		lMap.get("1");
		UniLog.log1("%s", lMap.toString());
		lMap.put("4", "4");
		lMap.get("1");
		UniLog.log1("%s", lMap.toString());
		lMap.put("5", "5");
		lMap.get("1");
		UniLog.log1("%s", lMap.toString());
		
		lMap = MapUtil.createFIFOMap(3);
		lMap.put("1", "1");
		lMap.get("1");
		UniLog.log1("%s", lMap.toString());
		lMap.put("2", "2");
		lMap.get("1");
		UniLog.log1("%s", lMap.toString());
		lMap.put("3", "3");
		lMap.get("1");
		UniLog.log1("%s", lMap.toString());
		lMap.put("4", "4");
		lMap.get("1");
		UniLog.log1("%s", lMap.toString());
		lMap.put("5", "5");
		lMap.get("1");
		UniLog.log1("%s", lMap.toString());	
	}
	
	/***
	 * create size limited map (LRU)
	 * threadsafe
	 * 
	 * @param p_maxEntries
	 * @return
	 */
	public static <K, V> Map<K, V> createLRUMap(int p_maxEntries) {
		return createSizeLimitedHash(p_maxEntries, true, true);
	}	
	
	/***
	 * create size limited map (FIFO)
	 * @param p_maxEntries
	 * @return
	 */
	public static <K, V> Map<K, V> createFIFOMap(int p_maxEntries) {
		return createSizeLimitedHash(p_maxEntries, false, true);
	}	
	private static <K,V> Map<K,V> createSizeLimitedHash(final int p_maxEntries, boolean p_accessOrder, boolean p_sync) {
		Map<K,V> map = new LinkedHashMap<K, V>(p_maxEntries*10/7, 0.7f, p_accessOrder) {
			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				return size() > p_maxEntries;
			}
		};	
		if (p_sync) {
			map = Collections.synchronizedMap(map);
		}
		return map;
	}
}
