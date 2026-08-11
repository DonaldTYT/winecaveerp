package com.uniinformation.webcore;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.uniinformation.utils.UniLog;

public class SessionListener implements HttpSessionListener {

  private static final Set<String> activeSessionIds = ConcurrentHashMap.newKeySet();
  private static final boolean debugFlag = false;
  private static final long SLOW_CLEANUP_WARNING_MS = 1000L;

  public static int getTotalActiveSession(){
    return activeSessionIds.size();
  }

  @Override
  public void sessionCreated(HttpSessionEvent p_event) {
    try {
      activeSessionIds.add(p_event.getSession().getId());
      logActiveSessions();
    }
    catch(Exception ex) {
      UniLog.log(ex);
    }
  }

  @Override
  public void sessionDestroyed(HttpSessionEvent p_event) {
    HttpSession session = p_event.getSession();
    String sessionId = session.getId();

    // Remove tracking before cleanup. Cleanup can close JDBC/ZK resources and
    // must not retain or block the global session-tracking data structures.
    activeSessionIds.remove(sessionId);
    SessionHelper.deleteActiveUser(sessionId);
    logActiveSessions();

    long cleanupStart = System.currentTimeMillis();
    try {
      SessionHelper sh = (SessionHelper) session.getAttribute(SessionHelper.getNameByContextPath(null));
      if(sh != null) {
        sh.cleanSessionData();
      }
    }
    catch(Exception ex) {
      UniLog.log(ex);
    }
    finally {
      // Remove once more in case a request raced with session invalidation and
      // recreated its active-user entry while cleanup was running.
      SessionHelper.deleteActiveUser(sessionId);
      long cleanupElapsed = System.currentTimeMillis() - cleanupStart;
      if (cleanupElapsed >= SLOW_CLEANUP_WARNING_MS) {
        UniLog.log1("slow session cleanup: session:%s elapsedMs:%d", sessionId, cleanupElapsed);
      }
    }
  }

  private static void logActiveSessions() {
    if (debugFlag) {
      UniLog.log1("current active session count: %d", activeSessionIds.size());
      for (String sessionId : activeSessionIds) {
        UniLog.log1("active session: %s", sessionId);
      }
    }
  }
}
