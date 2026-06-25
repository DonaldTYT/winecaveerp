package com.kikyosoft.servlet.larva.pages;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import com.kyoko.common.CoreLog;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.webcore.ZkSessionHelper;

import javax.servlet.RequestDispatcher;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@WebServlet(name = "LoginPageController", urlPatterns = {"/larva/pages/login"})
public class LoginPageController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
    	ZkSessionHelper sp = (ZkSessionHelper) ZkSessionHelper.getSessionHelper(req, resp);

    	if(sp.isLogin()) {
//    		resp.sendRedirect(resp.encodeRedirectURL(req.getContextPath()+"/"+sp.getLandingPage()));
    		resp.sendRedirect(req.getContextPath() + "/larva/dashboard");
    		return;
    	}
        // #2 convention: controller decides the base, JSP references it
    	String assetBase = "/larva/assets";
        req.setAttribute("assetsBase", assetBase);
        //String logoPath = assetBase+"/images/logo-dark.svg";
        String cocode = BiConfig.getDefaultCoCode(sp);
//        String logoPath = "/api/getResource/filing/LOGO_IMAGE_"+cocode;
        String logoPath = "/api/getResource?"+
        		"url=" +URLEncoder.encode("filing://LOGO_IMAGE_"+cocode,StandardCharsets.UTF_8)
        		+"&agent="+URLEncoder.encode(sp.getAgent(),StandardCharsets.UTF_8)
        		;
        req.setAttribute("logoPath", logoPath);
//    	String wallpaperPath = sp.getWallPaperPath();
//    	String wallpaperPath = "/larva/assets/images/authentication/winecave_login.jpg";
    	String wallpaperPath = "/larva/assets/images/abstract-textured-backgound.jpg";
        try {
        	FilingUtilObject fo = FilingUtil.getFile(sp.getAgent(), null , "BACKGROUND_IMAGE_"+cocode, null);
        	if(fo != null) {
        		wallpaperPath = "/api/getResource?"+
        				"url=" +URLEncoder.encode("filing://BACKGROUND_IMAGE_"+cocode,StandardCharsets.UTF_8)
        				+"&agent="+URLEncoder.encode(sp.getAgent(),StandardCharsets.UTF_8)
        		;
        	}
        } catch (Exception ex) {
        	CoreLog.log(ex.toString());
        }
        		
        req.setAttribute("wallpaperPath", wallpaperPath);

        // UI text bundle (easy to i18n later)
        Map<String, String> uiText = new LinkedHashMap<>();
//        uiText.put("htmlTitle", "Login | Mantis Bootstrap 5 Admin Template");
        uiText.put("htmlTitle", BiConfig.getCoName(sp, BiConfig.getDefaultCoCode(sp)));
        uiText.put("loginTitle", "Login");
        uiText.put("noAccount", "Don't have an account?");
//        uiText.put("emailLabel", "Email Address");
        uiText.put("emailLabel", "User ID");
        uiText.put("passwordLabel", "Password");
        uiText.put("rememberLabel", "Keep me sign in");
        uiText.put("forgotLabel", "Forgot Password?");
        uiText.put("loginBtn", "Login");
        uiText.put("loginWith", "Login with");
        uiText.put("footerCopyPrefix", "Copyright ©");
        uiText.put("footerCopyBrand", "Codedthemes");
        uiText.put("footerHome", "Home");
        uiText.put("footerPrivacy", "Privacy Policy");
        uiText.put("footerContact", "Contact us");

        // Optional: small notices / error message placeholder
        uiText.put("flashMessage", ""); // set a message here if needed

        req.setAttribute("uiText", uiText);

        // Social buttons (label + icon path suffix under assetsBase)
        List<Map<String, String>> socials = new ArrayList<>();
        socials.add(mkSocial("Google",     "images/authentication/google.svg",   "#"));
        socials.add(mkSocial("Twitter",    "images/authentication/twitter.svg",  "#"));
        socials.add(mkSocial("Facebook",   "images/authentication/facebook.svg", "#"));
        req.setAttribute("socials", socials);

        // Forward to JSP
        RequestDispatcher rd = req.getRequestDispatcher("/WEB-INF/views/larva/pages/login.jsp");
        rd.forward(req, resp);
    }

    private Map<String, String> mkSocial(String name, String iconRelPath, String href) {
        Map<String, String> m = new HashMap<>();
        m.put("name", name);
        m.put("icon", iconRelPath);
        m.put("href", href);
        return m;
    }
}
