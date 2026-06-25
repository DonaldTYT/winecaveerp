package com.kikyosoft.servlet.larva.pages;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;

@WebServlet(name = "SaleorSyncStatusController", urlPatterns = {"/larva/pages/saleor-sync-status"})
public class SaleorSyncStatusController extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // CORS (optional, keep if you’ll poll from Next.js)
    String origin = req.getHeader("Origin");
    if (origin != null) {
      resp.setHeader("Access-Control-Allow-Origin", origin);
      resp.setHeader("Vary", "Origin");
      resp.setHeader("Access-Control-Allow-Credentials", "true");
      resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
      resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
    }
    if ("OPTIONS".equalsIgnoreCase(req.getMethod())) { resp.setStatus(204); return; }

    resp.setCharacterEncoding("UTF-8");
    resp.setContentType("application/json");

    // Read the jobs map that SaleorSyncController stored in ServletContext
    @SuppressWarnings("unchecked")
    ConcurrentMap<String, SaleorSyncController.JobStatus> jobs =
        (ConcurrentMap<String, SaleorSyncController.JobStatus>)
            getServletContext().getAttribute("saleorSync.jobs");

    if (jobs == null) {
      resp.getWriter().write("{\"ok\":false,\"done\":true,\"percent\":100,\"message\":\"Sync service not initialized.\"}");
      return;
    }

    String jobId = Optional.ofNullable(req.getParameter("jobId")).orElse("").trim();
    SaleorSyncController.JobStatus st = jobs.get(jobId);
    if (st == null) {
      resp.getWriter().write("{\"ok\":false,\"done\":true,\"percent\":100,\"message\":\"Unknown job.\"}");
      return;
    }

    String json = "{"
        + "\"ok\":" + st.ok + ","
        + "\"done\":" + st.done + ","
        + "\"percent\":" + st.percent + ","
        + "\"message\":" + quote(st.message)
        + "}";
    resp.getWriter().write(json);
  }

  private static String quote(String s){
    if (s==null) return "\"\"";
    return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n")+"\"";
  }
}
