package com.kikyosoft.servlet.larva.pages;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

@WebServlet(name="SyncCategoryStatusController", urlPatterns={"/larva/pages/sync-category-status"})
public class SyncCategoryStatusController extends HttpServlet {
@Override
protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
 // Access the same job map stored in SyncCategoryController
 // Simplest approach: keep the map in ServletContext
 @SuppressWarnings("unchecked")
 ConcurrentMap<String, ?> jobs = (ConcurrentMap<String, ?>) getServletContext().getAttribute("SYNC_JOBS");
 if (jobs == null) {
   resp.setContentType("application/json;charset=UTF-8");
   resp.getWriter().write("{\"ok\":false,\"done\":true,\"message\":\"No job store\"}");
   return;
 }
 String jobId = req.getParameter("jobId");
 var st = (Map<String,Object>) jobs.get(jobId); // or the JobStatus object from above
 resp.setContentType("application/json;charset=UTF-8");
 if (st == null) {
   resp.getWriter().write("{\"ok\":false,\"done\":true,\"message\":\"Unknown job\"}");
   return;
 }
 // If you re-use the exact JobStatus class, cast properly and serialize fields:
 // Example if you expose JobStatus in context; otherwise adapt
}
}
