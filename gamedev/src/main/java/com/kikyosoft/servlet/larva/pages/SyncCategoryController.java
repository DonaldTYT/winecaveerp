package com.kikyosoft.servlet.larva.pages;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

@WebServlet(name="SyncCategoryController", urlPatterns={"/larva/pages/sync-category"})
public class SyncCategoryController extends HttpServlet {

// --- simple job store; in real apps use a service/singleton
private final ExecutorService exec = Executors.newFixedThreadPool(2);
private final ConcurrentMap<String, JobStatus> jobs = new ConcurrentHashMap<>();
private boolean progressEnabled = true;
private String statusUrl; // e.g. /larva/pages/sync-category-status

@Override public void init() {
 ServletContext ctx = getServletContext();
 // server-side switch
 String flag = Optional.ofNullable(ctx.getInitParameter("sync.progress.enabled")).orElse("true");
 progressEnabled = "true".equalsIgnoreCase(flag);
 statusUrl = Optional.ofNullable(ctx.getInitParameter("sync.progress.statusUrl"))
     .orElse(ctx.getContextPath() + "/larva/pages/sync-category-status");
 progressEnabled = false;

}

@Override public void destroy() { exec.shutdownNow(); }

@Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
 req.setAttribute("assetsBase", "/larva/assets");
 Map<String,String> ui = Map.of("htmlTitle","Sync Category to Saleor");
 req.setAttribute("uiText", ui);
 req.getRequestDispatcher("/WEB-INF/views/larva/pages/sync-category.jsp").forward(req, resp);
}

@Override
protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
 req.setCharacterEncoding("UTF-8");
 boolean ajax = "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))
             || Optional.ofNullable(req.getHeader("Accept")).orElse("").contains("application/json");
 String remarks = trim(req.getParameter("remarks"));

 if (!ajax) { // fallback to full page
   // start sync synchronously for simplicity
   try { runSyncWork(remarks, null); flash(req,"success","Completed."); }
   catch (Exception ex) { flash(req,"danger","Failed: "+ex.getMessage()); }
   doGet(req, resp); return;
 }

 resp.setCharacterEncoding("UTF-8");
 resp.setContentType("application/json");

 if (progressEnabled) {
   // Kick off in background and return jobId immediately
   String jobId = UUID.randomUUID().toString();
   JobStatus st = new JobStatus(); st.percent = 0; st.done = false; st.ok = true; st.message = "Queued";
   jobs.put(jobId, st);

   exec.submit(() -> {
     try {
       runSyncWork(remarks, p -> { st.percent = p; }); // update %
       st.percent = 100;
       st.ok = true; st.done = true; st.message = "Category sync completed.";
     } catch (Exception e) {
       st.ok = false; st.done = true; st.message = "Error: " + e.getMessage();
     }
   });

   String json = "{"
       + "\"ok\":true,"
       + "\"message\":\"Sync started.\","
       + "\"jobId\":\""+jobId+"\","
       + "\"progress\":{\"enabled\":true,\"statusUrl\":\""+escape(statusUrl)+"\"}"
       + "}";
   resp.getWriter().write(json);
 } else {
   // Synchronous (no progress)
   try {
     runSyncWork(remarks, null);
     resp.getWriter().write("{\"ok\":true,\"message\":\"Category sync completed.\"}");
   } catch (Exception ex) {
     resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
     resp.getWriter().write("{\"ok\":false,\"message\":"+quote("Failed: "+ex.getMessage())+"}");
   }
 }
}

// Your real work; call progress.accept(percent) occasionally
private void runSyncWork(String remarks, java.util.function.IntConsumer progress) throws Exception {
 // EXAMPLE: simulate steps; replace with CategorySyncWrapper.syncAll(...)
 int[] steps = {10, 30, 55, 80, 95};
 for (int p : steps) {
   Thread.sleep(600);            // simulate work
   if (progress != null) progress.accept(p);
 }
 // Do final call here...
}

private static class JobStatus {
 volatile int percent;
 volatile boolean done;
 volatile boolean ok;
 volatile String message;
}

private static void flash(HttpServletRequest req, String type, String msg) {
 Map<String,String> f = new HashMap<>(); f.put("type",type); f.put("message",msg); req.setAttribute("flash", f);
}
private static String trim(String s){ return s==null? "": s.trim(); }
private static String quote(String s){ return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n")+"\""; }
private static String escape(String s){ return s.replace("\"","\\\""); }
}
