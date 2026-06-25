package com.uniinformation.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListUtil {
	public static <V>List <V>of(Object... values){
	   List<V> list = new ArrayList<V>();
	    for (int i=0; i<values.length; i++) {
	    	list.add((V) values[i]);
	    }
	    return list;	
	}
	
	/***
	 * chop a list into multiple sub list based on subListSize
	 * @param list
	 * @param subListSize
	 * @return
	 */
	static <T> List<List<T>> chopByListSize(List<T> list, final int subListSize) {
	    List<List<T>> parts = new ArrayList<List<T>>();
	    final int N = list.size();
	    for (int i = 0; i < N; i += subListSize) {
	        parts.add(new ArrayList<T>(list.subList(i, Math.min(N, i + subListSize))));
	    }
	    return parts;
	}
	/***
	 * chop a list into multiple sub list based on subListCount
	 * @param ls
	 * @param subListCount
	 * @return
	 */
	public static <T>List<List<T>> chopByListCount( final List<T> ls, final int subListCount )
	{
	    final List<List<T>> lsParts = new ArrayList<List<T>>();
	    final int iChunkSize = ls.size() / subListCount;
	    int iLeftOver = ls.size() % subListCount;
	    int iTake = iChunkSize;
	    for( int i = 0, iT = ls.size(); i < iT; i += iTake )
	    {
	        if( iLeftOver > 0 ){
	            iLeftOver--;
	            iTake = iChunkSize + 1;
	        }
	        else {
	            iTake = iChunkSize;
	        }
	        lsParts.add( new ArrayList<T>( ls.subList( i, Math.min( iT, i + iTake ) ) ) );
	    }
	    return lsParts;
	}
	public static <T> Iterable<T> emptyIfNull(Iterable<T> iterable) {
	    return iterable == null ? Collections.<T>emptyList() : iterable;
	}
	public static <T> Iterable<T> safe(Iterable<T> iterable) {
	    return emptyIfNull(iterable);
	}
	
	public static ArrayList<String> stripStringArrayList(ArrayList<String> p_arr,String p_prefix) {
		ArrayList<String> al = new ArrayList<String>(); {
			for(String ss : p_arr) {
				if(ss.startsWith(p_prefix)) {
					ss = ss.substring(p_prefix.length());
				}
				al.add(ss);
			}
		}
		return(al);
	}
	public static void main(String args[]){
		List<String> aList = Arrays.asList("1","2","3","4","5","6","7","8","9","10");
		List<List<String>> masterList = ListUtil.chopByListSize(aList, 3);
		for (int i=0; i<masterList.size(); i++){
			List subList = masterList.get(i);
			for (int j=0; j<subList.size(); j++){
				UniLog.logm(null,"%d:%d:%s", i,j, subList.get(j));
			}
		}
		
		masterList = ListUtil.chopByListCount(aList, 3);
		for (int i=0; i<masterList.size(); i++){
			List subList = masterList.get(i);
			for (int j=0; j<subList.size(); j++){
				UniLog.logm(null,"%d:%d:%s", i,j, subList.get(j));
			}
		}
	}
}
