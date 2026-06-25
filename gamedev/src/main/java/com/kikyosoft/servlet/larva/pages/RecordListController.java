package com.kikyosoft.servlet.larva.pages;

import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet(name = "RecordListController", urlPatterns = {"/larva/pages/records"})
public class RecordListController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // === CORS support (safe defaults) ===
        String origin = req.getHeader("Origin");
        if (origin != null) {
            // In production, replace "*" with your Next.js site origin
            resp.setHeader("Access-Control-Allow-Origin", origin);
            resp.setHeader("Vary", "Origin");
            resp.setHeader("Access-Control-Allow-Credentials", "true");
            resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
            resp.setHeader("Access-Control-Allow-Methods", "GET, OPTIONS");
        }
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        boolean wantsJson =
                "XMLHttpRequest".equals(req.getHeader("X-Requested-With"))
             || Optional.ofNullable(req.getHeader("Accept")).orElse("").contains("application/json");

        if (wantsJson) {
            handleJson(req, resp);
        } else {
            handleHtml(req, resp);
        }
    }

    /** Normal JSP include mode (admin dashboard or standalone JSP) */
    private void handleHtml(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setAttribute("assetsBase", "/larva/assets");
        Map<String, String> ui = new HashMap<>();
        ui.put("htmlTitle", "Record List Viewer");
        req.setAttribute("uiText", ui);

        req.getRequestDispatcher("/WEB-INF/views/larva/pages/record-list.jsp").forward(req, resp);
    }

    /** JSON API for AJAX or Next.js storefront */
    private void handleJson(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json");

        int page = parseInt(req.getParameter("page"), 1);
        int pageSize = parseInt(req.getParameter("pageSize"), 20);

        // --- Demo data generation (replace with your API call or DB query) ---
        int total = 47;
        int totalPages = (int) Math.ceil((double) total / pageSize);
        page = Math.max(1, Math.min(page, totalPages));

        List<Map<String, Object>> items = new ArrayList<>();
        int start = (page - 1) * pageSize + 1;
        for (int i = start; i < start + pageSize && i <= total; i++) {
            Map<String, Object> it = new LinkedHashMap<>();
            it.put("id", i);
            it.put("title", "Record #" + i);
            it.put("description", "Auto-generated record " + i);
            it.put("status", (i % 3 == 0) ? "Pending" : (i % 3 == 1) ? "Active" : "Closed");
            it.put("date", String.format("2025-10-%02d", (i % 28) + 1));
            items.add(it);
        }

        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"ok\":true,");
        sb.append("\"page\":").append(page).append(",");
        sb.append("\"pageSize\":").append(pageSize).append(",");
        sb.append("\"total\":").append(total).append(",");
        sb.append("\"totalPages\":").append(totalPages).append(",");
        sb.append("\"items\":[");
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> it = items.get(i);
            if (i > 0) sb.append(",");
            sb.append("{")
              .append("\"id\":").append(it.get("id")).append(",")
              .append("\"title\":").append(quote(it.get("title"))).append(",")
              .append("\"description\":").append(quote(it.get("description"))).append(",")
              .append("\"status\":").append(quote(it.get("status"))).append(",")
              .append("\"date\":").append(quote(it.get("date")))
              .append("}");
        }
        sb.append("]}");

        resp.getWriter().write(sb.toString());
    }

    private static int parseInt(String s, int def) {
        try { return Integer.parseInt(s); } catch (Exception e) { return def; }
    }

    private static String quote(Object v) {
        if (v == null) return "\"\"";
        String s = v.toString().replace("\\", "\\\\")
                               .replace("\"", "\\\"")
                               .replace("\n", "\\n");
        return "\"" + s + "\"";
    }
}
