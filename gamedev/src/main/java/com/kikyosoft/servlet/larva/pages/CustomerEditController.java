package com.kikyosoft.servlet.larva.pages;


import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Dual-mode controller:
 * - GET (HTML): forward to JSP (if you have a standalone page) or return 204 (if you only include fragments)
 * - GET (JSON): Accept: application/json  -> returns { ok, record }
 * - POST (JSON or x-www-form-urlencoded): action=save|cancel -> returns { ok, message, record? }
 *
 * URL: /larva/pages/customer-edit
 *   GET  ?code=MZWC1               (HTML or JSON)
 *   POST body: { action, vd_* ...} (JSON or x-www-form-urlencoded)
 */
@WebServlet(name = "CustomerEditController", urlPatterns = {"/larva/pages/customer-edit"})
public class CustomerEditController extends HttpServlet {

  // demo in-memory store (replace with ERP/DB calls)
  private final Map<String, Map<String,String>> store = new LinkedHashMap<>();

  @Override
  public void init() {
    // seed one record from your sample
    Map<String,String> r = new LinkedHashMap<>();
    r.put("vd_customerCode", "MZWC1");
    r.put("vd_name", "MZ Wine Cellar Co., Ltd");
    r.put("vd_chineseName", "MZ Wine Cellar Co., Ltd");
    r.put("vd_type", "C");
    r.put("vd_ctype", "");
    r.put("vd_loginId", "MZWC1");
    r.put("vd_password", "1342188571");
    r.put("vd_contact", "Mr. Seven Wu");
    r.put("vd_primaryEmail", "nicolassfan@hotmail.com");
    r.put("vd_altEmail", "");
    r.put("vd_phone", "");
    r.put("vd_mobileNumber", "");
    r.put("vd_addressLine1", "Shop A, G/F, 37-39 Parkes Street, Jordan");
    r.put("vd_addressLine2", "");
    r.put("vd_addressLine3", "");
    r.put("vd_addressLine4", "");
    r.put("vd_salesmanId", "AC2");
    r.put("vd_isSuspended", ""); // "on" if true
    store.put("MZWC1", r);
  }

  // ---------- CORS (for Next.js storefront if cross-origin) ----------
  private void applyCors(HttpServletRequest req, HttpServletResponse resp) {
    String origin = req.getHeader("Origin");
    if (origin != null) {
      resp.setHeader("Access-Control-Allow-Origin", origin);
      resp.setHeader("Vary", "Origin");
      resp.setHeader("Access-Control-Allow-Credentials", "true");
      resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
      resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }
  }

  @Override protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    applyCors(req, resp);
    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    applyCors(req, resp);
    boolean wantsJson =
        "XMLHttpRequest".equals(req.getHeader("X-Requested-With")) ||
        Optional.ofNullable(req.getHeader("Accept")).orElse("").contains("application/json") ||
        "json".equalsIgnoreCase(req.getParameter("format"));

    if (wantsJson) {
      resp.setCharacterEncoding("UTF-8");
      resp.setContentType("application/json");
      String code = trim(req.getParameter("code"));
      Map<String,String> rec = code.isEmpty() ? null : store.get(code);
      if (rec == null) {
        writeJson(resp, "{\"ok\":false,\"message\":\"Record not found\"}");
      } else {
        writeJson(resp, toRecordJson(rec));
      }
      return;
    }

    // HTML flow: if you have a full JSP page, forward to it; otherwise 204 (fragments only)
    req.setAttribute("assetsBase", "/larva/assets");
    Map<String,String> ui = new LinkedHashMap<>();
    ui.put("htmlTitle", "Customer Edit");
    req.setAttribute("uiText", ui);
    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    // Or: req.getRequestDispatcher("/WEB-INF/views/larva/pages/customer-edit.jsp").forward(req, resp);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    applyCors(req, resp);
    req.setCharacterEncoding(StandardCharsets.UTF_8.name());
    resp.setCharacterEncoding("UTF-8");
    resp.setContentType("application/json");

    boolean isJsonBody = Optional.ofNullable(req.getContentType()).orElse("").toLowerCase().contains("application/json");
    Map<String,String> p = isJsonBody ? readJsonBody(req) : flatParams(req.getParameterMap());

    String action = trim(p.get("action")); // "save" or "cancel"
    if ("cancel".equalsIgnoreCase(action)) {
      writeJson(resp, "{\"ok\":true,\"message\":\"Cancelled. No changes saved.\"}");
      return;
    }

    // Collect fields (trim)
    Map<String,String> rec = new LinkedHashMap<>();
    for (String k : FIELDS) {
      rec.put(k, trim(p.get(k)));
    }
    // checkbox vd_isSuspended normalize to "on" or ""
    rec.put("vd_isSuspended", checked(p.get("vd_isSuspended")) ? "on" : "");

    String code = rec.getOrDefault("vd_customerCode", "").trim();
    if (code.isEmpty()) {
      writeError(resp, HttpServletResponse.SC_BAD_REQUEST, "Customer code is required.");
      return;
    }

    // TODO: Persist (replace with ERP/DB call)
    store.put(code, rec);

    writeJson(resp, "{\"ok\":true,\"message\":\"Saved.\",\"record\":"+mapToJson(rec)+"}");
  }

  // ---------- helpers ----------

  private static final List<String> FIELDS = Arrays.asList(
      "vd_customerCode","vd_name","vd_chineseName",
      "vd_type","vd_ctype","vd_loginId","vd_password","vd_contact",
      "vd_primaryEmail","vd_altEmail","vd_phone","vd_mobileNumber",
      "vd_addressLine1","vd_addressLine2","vd_addressLine3","vd_addressLine4",
      "vd_salesmanId","vd_isSuspended"
  );

  private static String trim(String s){ return s==null? "" : s.trim(); }
  private static boolean checked(String v){
    return v!=null && ("1".equals(v) || "on".equalsIgnoreCase(v) || "true".equalsIgnoreCase(v));
  }
  private static Map<String,String> flatParams(Map<String,String[]> m) {
    Map<String,String> out = new LinkedHashMap<>();
    for (Map.Entry<String,String[]> e : m.entrySet()) {
      String[] v = e.getValue();
      out.put(e.getKey(), (v!=null && v.length>0) ? v[0] : null);
    }
    return out;
  }
  private static Map<String,String> readJsonBody(HttpServletRequest req) throws IOException {
    try (BufferedReader br = req.getReader()) {
      StringBuilder sb = new StringBuilder();
      String line; while ((line = br.readLine()) != null) sb.append(line);
      String raw = sb.toString().trim();
      // minimal flat JSON parser (keys/values as strings/bools)
      Map<String,String> out = new LinkedHashMap<>();
      if (raw.startsWith("{") && raw.endsWith("}")) {
        raw = raw.substring(1, raw.length()-1);
        String[] pairs = raw.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
          String[] kv = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
          if (kv.length == 2) {
            String k = unquote(kv[0].trim());
            String v = unquote(kv[1].trim());
            out.put(k, v);
          }
        }
      }
      return out;
    }
  }
  private static String unquote(String s) {
    if (s == null) return null;
    s = s.trim();
    if (s.startsWith("\"") && s.endsWith("\"")) {
      s = s.substring(1, s.length()-1).replace("\\\"", "\"").replace("\\\\", "\\");
    }
    return s;
  }
  private static void writeError(HttpServletResponse resp, int code, String msg) throws IOException {
    resp.setStatus(code);
    writeJson(resp, "{\"ok\":false,\"message\":"+quote(msg)+"}");
  }
  private static void writeJson(HttpServletResponse resp, String json) throws IOException {
    resp.getWriter().write(json);
  }
  private static String quote(String s){
    if (s==null) return "\"\"";
    return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n")+"\"";
  }
  private static String mapToJson(Map<String,String> m) {
    StringBuilder sb = new StringBuilder("{");
    boolean first = true;
    for (Map.Entry<String,String> e : m.entrySet()) {
      if (!first) sb.append(',');
      first = false;
      sb.append(quote(e.getKey())).append(':').append(quote(e.getValue()));
    }
    sb.append('}');
    return sb.toString();
  }
  private String toRecordJson(Map<String,String> rec) {
    return "{\"ok\":true,\"record\":"+mapToJson(rec)+"}";
  }
}
