package com.uniinformation.webcore;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.kikyosoft.utils.CleanupWatcher;

public class CleanupWatcherListener implements ServletContextListener {
   public static final String ATTR_NAME = "com.uniinformation.cleanupWatcher";

   public void contextInitialized(ServletContextEvent p_event) {
      CleanupWatcher watcher = new CleanupWatcher("UniInformation-CleanupWatcher");
      p_event.getServletContext().setAttribute(ATTR_NAME, watcher);
   }

   public void contextDestroyed(ServletContextEvent p_event) {
      ServletContext ctx = p_event.getServletContext();
      CleanupWatcher watcher = (CleanupWatcher) ctx.getAttribute(ATTR_NAME);
      if (watcher != null) {
         watcher.shutdown();
      }
   }

   public static CleanupWatcher getWatcher(ServletContext p_context) {
      return (CleanupWatcher) p_context.getAttribute(ATTR_NAME);
   }
}