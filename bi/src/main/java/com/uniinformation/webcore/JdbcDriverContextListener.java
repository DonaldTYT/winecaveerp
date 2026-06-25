package com.uniinformation.webcore;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.uniinformation.utils.ThreadPool;
import com.uniinformation.utils.UniLog;


/***
 * for fix tomcat jdbc memory leak issue
 * need to register in web.xml
 * @author Andrew
 *
 */
public class JdbcDriverContextListener implements ServletContextListener {


	@Override
	public void contextInitialized(ServletContextEvent sce) {
		//can load driver here
		UniLog.log1("called");
	}

	@Override
	public final void contextDestroyed(ServletContextEvent sce) {
		UniLog.log1("called");
		//close any db related background task here
		//WebCoreUtil.closeAllCachedJdbcPool();
		try {
			//ThreadPool.getDefaultThreadPool().shutdown();
			ThreadPool.shutdownDefaultThreadPool();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}

		// deregister jdbc drivers in this context's classloader:
		ClassLoader cl = Thread.currentThread().getContextClassLoader();
		Enumeration<Driver> drivers = DriverManager.getDrivers();
		int okCnt = 0;
		int failCnt = 0;
		int skipCnt = 0;
		while (drivers.hasMoreElements()) {
			Driver driver = drivers.nextElement();
			if (driver.getClass().getClassLoader() == cl) {
				try {
					UniLog.log1("deregistering jdbc driver %s", driver);
					DriverManager.deregisterDriver(driver);
					okCnt++;
				} 
				catch (SQLException ex) {
					UniLog.log1("error deregistering jdbc driver %s", driver);
					ex.printStackTrace();
					failCnt++;
				}
			} else {
				// driver was not registered by the webapp
				UniLog.log1("skip deregistering jdbc driver %s as it does not belong webapp", driver);
				skipCnt++;
			}
		}
		UniLog.log1("ok:%d fail:%d skip:%d", okCnt, failCnt, skipCnt);
	}
}