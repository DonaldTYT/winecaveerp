package com.kikyosoft.servlet.auth;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

import java.io.IOException;

@WebServlet(name = "LoginServlet", urlPatterns = {"/auth/login"})
public class LoginServlet extends HttpServlet {

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    SessionHelper sp = ZkSessionHelper.getSessionHelper(req, resp);
    req.setCharacterEncoding("UTF-8");
    String email = trim(req.getParameter("email"));
    String password = trim(req.getParameter("password"));
    String remember = req.getParameter("remember"); // "1" if checked, else null
    ReturnMsg loginResult;
    try {
    	loginResult = sp.login(req, resp, email, password);
    } catch (Exception ex) {
    	UniLog.log(ex);
    	loginResult = new ReturnMsg(false,ex.toString());
    }

    // TODO: replace this with your real authentication (DB, LDAP, etc.)
//    boolean ok = isValid(email, password);

 //   if (!ok) {
    if (loginResult != null && !loginResult.getStatus()) {
      // redirect back to the login page with an error flag
      // adjust this URL to the actual JSP/route of your login page
      resp.sendRedirect(req.getContextPath() + "/login?err=1");
      return;
    }

    // create/login session
    HttpSession session = req.getSession(true);
    session.setAttribute("userEmail", email);
    session.setAttribute("isAuthenticated", true);

    // Remember-me demo: extend session timeout (or set cookie)
    if ("1".equals(remember)) {
      // e.g., 7 days
      session.setMaxInactiveInterval(7 * 24 * 60 * 60);
      // For a real remember-me, set a persistent secure cookie/token instead.
    }

    // Redirect to your app's landing page after login
    // CHANGE this to wherever you want to land the user
//    resp.sendRedirect(req.getContextPath() + "/larva/dashboard?action=browse&viewid=CountryOrOrigin&page_id=CountryOrOrigin_01&zul=zkbiloader.zul&menuitem=10056&agent=erpv4winecave&theme=larva");
    resp.sendRedirect(req.getContextPath() + "/larva/dashboard");
  }

  private boolean isValid(String email, String password) {
    if (email == null || password == null) return false;
    // Example only:
    return email.equalsIgnoreCase("user@example.com") && password.equals("secret123");
  }

  private String trim(String s) {
    return s == null ? null : s.trim();
  }
}
