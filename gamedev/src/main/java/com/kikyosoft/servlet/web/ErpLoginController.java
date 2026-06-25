package com.kikyosoft.servlet.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "ErpLoginController", urlPatterns = {"/erplogin"})
public class ErpLoginController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ObjectMapper om = new ObjectMapper();

    // Allow these browser origins (adjust as needed)
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
        "http://192.168.19.212:3000",
        "http://192.168.19.212:3001",
        "http://192.168.19.212:3002",
        "http://192.168.19.212:9000"
    );

    // ---- CORS helpers -------------------------------------------------------
    private void setCors(HttpServletRequest req, HttpServletResponse resp) {
        String origin = req.getHeader("Origin");
        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            resp.setHeader("Access-Control-Allow-Origin", origin);
            resp.setHeader("Vary", "Origin"); // cache-friendly
            resp.setHeader("Access-Control-Allow-Credentials", "true");
        }
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, X-Requested-With");
        resp.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
        resp.setHeader("Access-Control-Max-Age", "3600");
    }

    @Override
    protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCors(req, resp);
        resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    // ---- GET: /erplogin?loginid=...&password=... ---------------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCors(req, resp);
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json");

        String loginid = req.getParameter("loginid");
        String password = req.getParameter("password");

        writeResponse(handleLogin(loginid, password, req, resp), resp);
    }

    // ---- POST: supports JSON body or querystring ----------------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        setCors(req, resp);
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setContentType("application/json");

        String ct = req.getContentType();
        String loginid = null;
        String password = null;

        if (ct != null && ct.toLowerCase().startsWith("application/json")) {
            String body = readBody(req);
            if (body != null && !body.isEmpty()) {
                JsonNode root = om.readTree(body);
                loginid = asText(root, "loginid");
                password = asText(root, "password");
            }
        }
        if (loginid == null) loginid = req.getParameter("loginid");
        if (password == null) password = req.getParameter("password");

        writeResponse(handleLogin(loginid, password, req, resp), resp);
    }

    // ---- Core logic (same as your Spring version) --------------------------
    private Result handleLogin(String loginid, String password,
                               HttpServletRequest request, HttpServletResponse response) {

        if (!erpLogin(loginid, password, request, response)) {
            return Result.error("Invalid loginid or password");
        }

        // Your session helper (kept to mirror your Spring code)
        // Store whatever you need for later ERP APIs
        SessionHelper sp = ZkSessionHelper.getSessionHelper(request, response);

        // Map ERP login -> Saleor credentials + display name
        String saleorEmail = mapToSaleorEmail(loginid);
        String saleorPassword = mapToSaleorPassword(loginid);
        String shortName = mapToShortName(loginid);

        return Result.ok(saleorEmail, saleorPassword, shortName);
    }

    // ---- ERP login stub (replace with real auth) ---------------------------
    private boolean erpLogin(String loginid, String password,
                             HttpServletRequest request, HttpServletResponse response) {
        // TODO: replace with real validation against ERP store/LDAP/etc.
        // Minimal sanity check:
        if (loginid == null || loginid.isBlank() || password == null || password.isBlank()) return false;

        // Create/ensure HTTPSession if you rely on JSESSIONID:
        HttpSession session = request.getSession(true);
        session.setAttribute("erp_user_loginid", loginid);
        session.setAttribute("erp_user_roles", "USER");

        // If you need to set custom cookies, uncomment:
        // Cookie c = new Cookie("erp_auth", "1");
        // c.setPath("/");
        // c.setHttpOnly(true);
        // response.addCookie(c);

        return true;
    }

    // ---- Mapping stubs (mirror your values) --------------------------------
    private String mapToSaleorEmail(String loginid) {
        // return loginid.toLowerCase() + "@winecave.com";
        return "weborder@winecavehk.com";
    }

    private String mapToSaleorPassword(String loginid) {
        // return "TempP@ss-" + Math.abs(loginid.hashCode());
        return "Qwe123456";
    }

    private String mapToShortName(String loginid) {
        return loginid == null || loginid.isEmpty()
            ? "User"
            : Character.toUpperCase(loginid.charAt(0)) + loginid.substring(1);
    }

    // ---- Small JSON Result type --------------------------------------------
    private static final class Result {
        public boolean ok;
        public String saleorEmail;
        public String saleorPassword;
        public String shortName;
        public String error;

        static Result ok(String email, String password, String name) {
            Result r = new Result();
            r.ok = true; r.saleorEmail = email; r.saleorPassword = password; r.shortName = name;
            return r;
        }
        static Result error(String msg) {
            Result r = new Result();
            r.ok = false; r.error = msg; return r;
        }
    }

    // ---- Utilities ---------------------------------------------------------
    private static String readBody(HttpServletRequest req) throws IOException {
        try (BufferedReader r = req.getReader()) {
            return r.lines().collect(Collectors.joining("\n"));
        }
    }

    private static String asText(JsonNode node, String field) {
        JsonNode v = node != null ? node.get(field) : null;
        return (v != null && !v.isNull()) ? v.asText() : null;
    }

    private static void writeResponse(Result result, HttpServletResponse resp) throws IOException {
        try (PrintWriter out = resp.getWriter()) {
            ObjectMapper om = new ObjectMapper();
            out.write(om.writeValueAsString(result));
        }
    }
}
