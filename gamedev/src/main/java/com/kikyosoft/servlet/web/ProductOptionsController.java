package com.kikyosoft.servlet.web;

import com.uniinformation.erpv4.ProductOptionFinder;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.json.JSONObject;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet(name = "ProductOptionController", urlPatterns = {"/getProductOptions"})
public class ProductOptionsController extends HttpServlet {

    private static final long serialVersionUID = 1L;

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

        SessionHelper sp = ZkSessionHelper.getSessionHelper(req, resp);
        String target =  req.getParameter("target");
        
        ProductOptionFinder pof = new ProductOptionFinder(sp);
        for(String option : pof.getOptions()) {
        	String ss =  req.getParameter(option);
        	if(ss != null) {
        		pof.addCondition(option, ss);
        	}
        }
        JSONObject jo = pof.queryOptions(sp, target);
        try (PrintWriter out = resp.getWriter()) {
            out.write(jo.toString());
        }
    }

}
