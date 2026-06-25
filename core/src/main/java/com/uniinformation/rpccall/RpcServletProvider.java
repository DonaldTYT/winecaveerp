
package com.uniinformation.rpccall;

import com.uniinformation.rpccall.*;
import com.uniinformation.utils.UniLog;

import java.util.*;
import java.lang.reflect.*;

class RpcRegistry
{
  String classname;
  boolean isClonable;
  boolean isReflect;
  Vector pool =  new Vector(1);
  int maxPool = 1;

  public void setMaxPool(int m)
  {
    maxPool = m;
  }
  public void preload(int n)
  {
    int i;
    if( /* maxPool > 0 &&  */ n > maxPool) maxPool = n;
    RpcServlet arr[] =  new RpcServlet[n];
    for(i=0;i<n;i++) 
      arr[i] = get();
    for(i=0;i<n;i++) 
      store(arr[i]);
    UniLog.log("preload " + classname + " " + pool.size() + " Max " + maxPool);
  }
  public synchronized void store(RpcServlet rpcserv)
  {
	if(maxPool <= 0) {
		UniLog.log("drop rpcservlet " + rpcserv);
		return;
	}
    if( /* maxPool > 0 &&  */ pool.size() >= maxPool) {
      UniLog.log("drop rpcservlet " + rpcserv);
      return;
    }
    pool.addElement(rpcserv);
  }
  public RpcServlet get()
  {
    RpcServlet rpcservlet;
    synchronized(pool) {
      if(pool.isEmpty() == false) {
        try {
          rpcservlet = (RpcServlet)pool.elementAt(0);
          rpcservlet.init_servlet();
          // pool.remove(0); window JVM no remove(int)
          pool.removeElementAt(0);
          return(rpcservlet);
        }
        catch(Exception e) {
          return null;
        }
      }
    }
    if(isReflect) {
      try {
        Class myclass = Class.forName(classname);
        Constructor constructor = myclass.getConstructor((Class []) null);
        if(constructor == null) {
          UniLog.log("Cannot get constructor for " + classname);
        }
        rpcservlet = (RpcServlet) constructor.newInstance((Object[]) null);
        rpcservlet.init_servlet();
        return(rpcservlet);
      }
      catch(Exception e) {
        UniLog.log(e);
        UniLog.log("getService Fail:" + e.getMessage());
        return null;
      }
    }
    return null;
  }
  public RpcRegistry(String name,boolean c,boolean r)
  {
    classname = new String(name);
    isClonable = c;
    isReflect = r;
  }
}
public class RpcServletProvider
{
  static private Hashtable service_table = new Hashtable();

  public boolean preloadService(String service_name,int n)
  {
    RpcRegistry reg = (RpcRegistry)service_table.get(service_name);
    if(reg != null) {
      reg.preload(n);
      return true;
    }
    return false;
  }
  public synchronized RpcServlet getService(String service_name)
  {
    RpcServlet rpcservlet = null;
    RpcRegistry reg = (RpcRegistry)service_table.get(service_name);
    if(reg != null) {
      rpcservlet =reg.get();
    } else {
    	UniLog.log("RpcServlet get Service ("+service_name+") got null");
    }
    return(rpcservlet);
  }
  public void freeService(RpcServlet rpcservlet)
  {
    rpcservlet.close_servlet();
	 synchronized (this) {
       String classname = rpcservlet.getClass().getName();
       RpcRegistry reg = (RpcRegistry)service_table.get(classname);
       if(reg != null)
         reg.store(rpcservlet);
    }
  }

  public void registerService(String classname,boolean isclonable, boolean isreflect,int maxstore)
  {
    RpcRegistry reg = new RpcRegistry(classname,isclonable,isreflect);
	reg.setMaxPool(maxstore);
    service_table.put(classname,reg);
  }
  public void registerService(String classname,boolean isclonable, boolean isreflect)
  {
    RpcRegistry reg = new RpcRegistry(classname,isclonable,isreflect);
    service_table.put(classname,reg);
  }
  public void printService()
  {
    UniLog.log("Service Table:-");
    for(Enumeration enum0 = service_table.keys();enum0.hasMoreElements();) {
            String s = (String) enum0.nextElement();
        UniLog.log(s);
    }
    UniLog.log("");
  }
  public Object getParameter(Object p_key) { // to be overrided
     return(null);
  }
}
