package com.uniinformation.bicore.bischema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.erpv4.BiConfig;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

public class ExcelWorkSheetCache {
/******************************************************************************/
/*              Excel WorkSheet Cache Operation                               */
/******************************************************************************/
	
	//private static Hashtable<String,BiResult>biResultHash = new Hashtable<String,BiResult>();
//	private static ConcurrentHashMap<String,BiResult>biResultHash = new ConcurrentHashMap<String,BiResult>();
	private static HashMap<String,BiResult>biResultHash = new HashMap<String,BiResult>();
	private static HashSet<String> biResultDirtyHash = new HashSet<String>();
	private static SessionHelper daemonSh = null;
	
	
	static void setDirtyAll( BiSchema p_schema,String p_viewName) {
		synchronized(biResultHash) {
			HashSet<String> dvs = new HashSet<String>();
			collectAllDependentViews(p_schema,p_viewName,dvs);
			for(String dv : dvs) {
				setDirty( dv ,true);
			}
		}
	}
	public static void setDirty( String p_viewName,boolean p_sw) {
		synchronized(biResultDirtyHash){
			if(p_sw) {
				biResultDirtyHash.add(p_viewName);
			} else {
				biResultDirtyHash.remove(p_viewName);
			}
		}
	}
	static boolean isSubclassOf(Class<?> clazz, Class<?> superClass) {
	    if (superClass.equals(Object.class)) {
	        // Every class is an Object.
	        return true;
	    }
	    if (clazz.equals(superClass)) {
	        return true;
	    } else {
	        clazz = clazz.getSuperclass();
	        // every class is Object, but superClass is below Object
	        if (clazz.equals(Object.class)) {
	            // we've reached the top of the hierarchy, but superClass couldn't be found.
	            return false;
	        }
	        // try the next level up the hierarchy.
	        return isSubclassOf(clazz, superClass);
	    }
	}
	
	
	static public synchronized void cacheAllWorkSheet(SessionHelper p_sh,boolean ignoreNoDependencySheets) throws Exception {
		UniLog.log1("called ignoreNoDependencySheets:%s", ignoreNoDependencySheets);
		BiSchema schema = p_sh.getBiSchema();
		List<BiView> allViews = schema.getAllView();
		Class excelClass = Class.forName("com.uniinformation.bicore.bischema.BiResultExcelSheet");
		for(BiView bv : allViews) {
			if(!bv.getName().startsWith(schema.getDbLabel())) continue;
			HashSet<String> dependsView = schema.getDependedViews(bv.getName());
			if(ignoreNoDependencySheets && dependsView == null) {
				UniLog.log1("skip " + bv.getName() + " : no dependent worksheet");
				continue;
			}
			if(isSubclassOf(bv.getBiResultClass(),excelClass)) {
				getBrFromCache(p_sh,bv.getName());
			}
		}
	}
	
	static public boolean cacheRequireUpdate(String p_viewName) {
		return( biResultDirtyHash.contains(p_viewName));
	}
	
	static class RecalState {
		String name;
		int recalCnt;
		boolean ok;
		RecalState(String p_name) {
			name = p_name;
			ok = false;
		}
	}
	static public final int MAX_ITERATE = 5;
	static public final int MAX_TOTALITERATE = 100;
			

	//andrew240215 suggestion: this method very slow, synchronized is not suggested. change to lockfail abort is better.
	private static synchronized void recalAndUpdateCache(BiSchema p_schema,RecalState rcs[],boolean p_doUpdate) throws Exception {
		UniLog.log1("called doUpdate:%s", p_doUpdate);
		int i=0;
		boolean allOk = false;
		for(i=0;i<MAX_TOTALITERATE; i++) {
			RecalState rc = rcs[i % rcs.length];
			if(!rc.ok)  {
				if(rc.recalCnt >= MAX_ITERATE) {
					throw new Exception("ExcelView Recall Error : Too many iteration");
				}
	        	setDirty(rc.name, false);
				BiResult sbr = biResultHash.get(rc.name);
	        	rc.recalCnt++;
	        	UniLog.log1("recal " + rc.name);
	        	int updCnt = sbr.recal();
	        	UniLog.log1("recal " + rc.name + " got " + updCnt + " changes");
	        	rc.ok = true;
	        	if(updCnt > 0) {
	        		/*
	        		synchronized(biResultHash) {
	        			biResultDirtyHash.add(rc.name);
	        		}
	        		*/
	        		setDirty(rc.name, true);
	        		HashSet<String> dvs = p_schema.getDependedViews(rc.name);
	        		if(dvs != null) {
	        		for(String dv : dvs) {
	        			for(int k = 0; k < rcs.length;k++) {
	        				if(rcs[k].name.equals(dv)) {
	        					rcs[k].ok = false;
	        				}
	        			}
	        		}
	        		}
	        	}
			}
			allOk = true;
			for(int j=0;j<rcs.length;j++) {
				if(!rcs[j].ok) {
					allOk = false;
					break;
				}
			}
			if(allOk) break;
		}
		if(!allOk) {
			throw new Exception("ExcelView Recal All Failed : Too many total recal");
		}
		UniLog.log1("recal all completed after " + i + " iteration");
		if(p_doUpdate) {
			for(RecalState rc : rcs) {
				BiResult sbr = biResultHash.get(rc.name);
				if(sbr.hasMarkUpdatedRecord()) {
	        		sbr.batchAddUpdateDelete();
        			sbr.unMarkAll();
        			sbr.clearCurrentRec();
        			sbr.invalidateLoadCache();
	        		setDirty(rc.name, false);
				}
			}
		}
	}

	public static void recalAndUpdateAllCache(BiSchema p_schema,boolean p_doUpdate) throws Exception {
		RecalState rcs[]  = new RecalState[biResultHash.size()];
		int i=0;
		for(String vn : biResultHash.keySet()) {
			rcs[i] = new RecalState(vn);
			i++;
		}
		recalAndUpdateCache(p_schema,rcs,p_doUpdate);
	}
	
	
	public static BiResult getBrFromCache(SessionHelper p_sh,String viewName) throws Exception {
		synchronized(biResultHash) {
			if(!biResultHash.containsKey(viewName)) {
				BiResult sbr = null;
				BiView bv = p_sh.getBiSchema().getViewByName(viewName);
				if(bv != null) {
					biResultHash.put(viewName, null);
					String loginId = BiConfig.getString(p_sh, "ExcelCacheUser");
					if(StringUtils.isBlank(loginId)) {
						loginId = p_sh.getLoginId();
						sbr = bv.newBiResult(loginId, null, null, p_sh);
					} else {
						if(daemonSh == null) {
							daemonSh = ZkSessionHelper.getSessionHelperDummy(p_sh.getAgent(),loginId,p_sh.getSvc());
						}
						sbr = bv.newBiResult(loginId, null, null, daemonSh);
					}
					if(StringUtils.isBlank(sbr.getSessionHelper().getAgent())) {
						throw new Exception ("Error SessionHelper.getAgent is blank when create , abnormal,aborted ");
					}
//					HashSet<String> dependentViews = new HashSet<String>();
//					Vector<BiColumn> cols = sbr.getColumns();
					sbr.query(); 
					biResultHash.put(viewName, sbr);
					UniLog.log1("view " + viewName + " ok");
				} else {
					UniLog.log1("view " + viewName + " not exist!!!!");
				}
			}
			BiResult rtnBr = biResultHash.get(viewName);
			if(StringUtils.isBlank(rtnBr.getSessionHelper().getAgent())) {
				throw new Exception ("Error SessionHelper.getAgent is blank when get , abnormal,aborted ");
			}
			return(rtnBr);
//			return(biResultHash.get(viewName));
		}
	}
	/***
	 * clear all excel br cache
	 * @return
	 */
	public static ReturnMsg clearBrCache() {
		return clearBrCache(null,null);
	}
	
	/***
	 * clear excel br cache
	 * @param viewName
	 * @return
	 */
	static void collectAllDependentViews(BiSchema p_schema,String viewName,HashSet<String> p_collect) {
		p_collect.add(viewName);
		HashSet<String> depViews = p_schema.getDependedViews(viewName) ;
		if(depViews != null) {
		for(String dv : depViews) {
			if(!p_collect.contains(dv)) collectAllDependentViews(p_schema,dv,p_collect);
		}
		}
	}
	public static ReturnMsg clearBrCache(BiSchema p_schema,String viewName) {
		UniLog.log1("called view:%s", viewName);
		synchronized(biResultHash) {
			if (viewName == null) {
				biResultHash.clear();
			} else {
				HashSet<String> dvs = new HashSet<String>();
				collectAllDependentViews(p_schema,viewName,dvs);
				for(String dv : dvs) {
					biResultHash.remove(dv);
				}
			}
		}
		return ReturnMsg.defaultOk;
	}	
	public static ReturnMsg cacheAndRecalBr(SessionHelper p_sh,String viewName) throws Exception {
		HashSet<String> dvs = new HashSet<String>();
		collectAllDependentViews(p_sh.getBiSchema(),viewName,dvs);
		dvs.remove(viewName);
		if(!dvs.isEmpty())  {
			RecalState rcs[]  = new RecalState[dvs.size()];
			int i=0;
			for(String dv : dvs) {
				getBrFromCache(p_sh,dv);
				rcs[i] = new RecalState(dv);
				i++;
			}
			recalAndUpdateCache(p_sh.getBiSchema(),rcs,true);
		}
		return ReturnMsg.defaultOk;
	}	
	
	
	/***
	 * clear excel br cache
	 * 
	 * e.g
VA depends on VB depends on VC
when recal VB,
clearCacheDependsBy(VB) clear VA
clearCacheDependsOn(VB) clear VB and VC
	 * 
	 * @param viewName
	 * @return
	 */
	/*
	public static ReturnMsg clearBrCacheDependsBy(String viewName) {
		UniLog.log1("called view:%s", viewName);
		clearBrCache();
		return ReturnMsg.defaultOk;
	}
	public static ReturnMsg clearBrCacheDependsOn(String viewName) {
		UniLog.log1("called view:%s", viewName);
		synchronized(biResultHash) {
			if (viewName == null) {
				biResultHash.clear();
			}
			else {
				biResultHash.remove(viewName);
			}
		}
		return ReturnMsg.defaultOk;
	}
	*/
	
}
