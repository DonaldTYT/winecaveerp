package com.kikyosoft.servlet.miyu.garyu;


import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

@WebServlet(urlPatterns = {
    "/miyu/ga-ryu",           // no trailing slash
    "/miyu/ga-ryu/",          // with trailing slash
    "/miyu/ga-ryu/index"      // pretty "index"
})

public class IndexController extends HttpServlet {
  SessionHelper sh;
  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
	sh = ZkSessionHelper.getSessionHelper(req,resp,true, () -> new ZkSessionHelper());
	req.setAttribute("pageHasHeader", true);
	req.setAttribute("pageHasHomeButton", true);
	req.setAttribute("pageHasTopMenu", true);
	req.setAttribute("pageHasMainBanner", true);
	req.setAttribute("pageHasAboutUs", true);
	req.setAttribute("pageHasOurServices", false);
	req.setAttribute("pageHasOurPortfolio", true);
	req.setAttribute("pageHasBlog", false);
	req.setAttribute("pageHasContact", true);
	req.setAttribute("pageHasFooter", true);
    // --- Page/meta/brand/nav ---
    req.setAttribute("pageTitle", "SPACYDNYA — Live");
    req.setAttribute("meta", Map.of("description","SEO & Marketing landing", "author","Your Company"));
    req.setAttribute("brand", Map.of("prefix","SPAC", "suffix","DYNA"));
    req.setAttribute("ctaText", "Contact Now");
    req.setAttribute("nav", Map.of("homeHref", req.getContextPath() + "/miyu/ga-ryu/"));

    // --- Hero block ---
    Map<String,Object> hero = new HashMap<>();
    hero.put("kicker", "WELCOME TO KIKYOSOFT"); //hero.put("kicker", "WELCOME TO SPACYDNYA");
    hero.put("h1Before", "We Make");
    hero.put("h1Em", "ERP System");
    hero.put("h1Span", "and");
    hero.put("h1After", " Provide Turnkey Solution");
    hero.put("text", "Erp/V4 is customizable from your backend without touching the HTML.");
    hero.put("linkHref", "https://templatemo.com/page/1");
    hero.put("linkText", "TemplateMo");
    hero.put("imageAlt", "team meeting");
    req.setAttribute("hero", hero);

    // --- Analyze form (GET to your analyzer endpoint) ---
    req.setAttribute("analyze", Map.of(
        "action", req.getContextPath() + "/analyze",
        "method", "GET",
        "placeholder", "demo.erpv4.com",
        "button", "Goto Live Demo"
    ));

    // --- Services (icon paths are relative to /miyu/ga-ryu) ---
    List<Map<String,String>> services = new ArrayList<>();
    services.add(Map.of("icon","/miyu/ga-ryu/assets/images/service-icon-01.png","alt","reporting","title","Data Analysis","text","Lorem ipsum dolor sit amet...","delay","0.5s"));
    services.add(Map.of("icon","/miyu/ga-ryu/assets/images/service-icon-02.png","alt","","title","Data Reporting","text","Lorem ipsum dolor sit amet...","delay","0.7s"));
    services.add(Map.of("icon","/miyu/ga-ryu/assets/images/service-icon-03.png","alt","","title","Web Analytics","text","Lorem ipsum dolor sit amet...","delay","0.9s"));
    services.add(Map.of("icon","/miyu/ga-ryu/assets/images/service-icon-04.png","alt","","title","SEO Suggestions","text","Lorem ipsum dolor sit amet...","delay","1.1s"));
    req.setAttribute("services", services);

    req.setAttribute("servicesBlock", Map.of(
        "em","SEO",
        "span","Project",
        "paragraph","All wording/images below can be changed from Java or your DB."
    ));

    // --- Progress bars ---
    List<Map<String,Object>> progressBars = List.of(
        Map.of("cssClass","first-bar", "title","Website Analysis", "percent", 84),
        Map.of("cssClass","second-bar", "title","SEO Reports",     "percent", 88),
        Map.of("cssClass","third-bar",  "title","Page Optimizations", "percent", 94)
    );
    req.setAttribute("progressBars", progressBars);

    // --- Portfolio grid ---
    List<Map<String,String>> portfolio = new ArrayList<>();
    portfolio.add(Map.of("href","#","delay","0.3s","title","SEO Analysis","text","Lorem ipsum...","image","/miyu/ga-ryu/assets/images/portfolio-image.png"));
    portfolio.add(Map.of("href","#","delay","0.4s","title","Website Reporting","text","Lorem ipsum...","image","/miyu/ga-ryu/assets/images/portfolio-image.png"));
    portfolio.add(Map.of("href","#","delay","0.5s","title","Performance Tests","text","Lorem ipsum...","image","/miyu/ga-ryu/assets/images/portfolio-image.png"));
    portfolio.add(Map.of("href","#","delay","0.6s","title","Data Analysis","text","Lorem ipsum...","image","/miyu/ga-ryu/assets/images/portfolio-image.png"));
    req.setAttribute("portfolio", portfolio);

    // --- Blog main + list ---
    req.setAttribute("blogMain", Map.of(
        "href","#",
        "image","/miyu/ga-ryu/assets/images/big-blog-thumb.jpg",
        "alt","Workspace Desktop",
        "date","24 Mar 2021",
        "author","TemplateMo",
        "category","Branding",
        "title","SEO Agency & Digital Marketing",
        "excerpt","Lorem ipsum dolor sit amet, consectetur adipiscing elit...",
        "cta","Discover More"
    ));
    req.setAttribute("blogPosts", List.of(
        Map.of("date","18 Mar 2021","href","#","title","New Websites & Backlinks","excerpt","Lorem ipsum...","thumb","/miyu/ga-ryu/assets/images/blog-thumb-01.jpg"),
        Map.of("date","14 Mar 2021","href","#","title","SEO Analysis & Content Ideas","excerpt","Lorem ipsum...","thumb","/miyu/ga-ryu/assets/images/blog-thumb-01.jpg"),
        Map.of("date","06 Mar 2021","href","#","title","SEO Tips & Digital Marketing","excerpt","Lorem ipsum...","thumb","/miyu/ga-ryu/assets/images/blog-thumb-01.jpg")
    ));

    // --- Contact & footer ---
    req.setAttribute("contact", Map.of(
        "heading","Feel Free To Send Us a Message About Your Website Needs",
        "paragraph","You control this text from your backend.",
        "phone","010-020-0340",
        "formAction", req.getContextPath() + "/contact",
        "formMethod","post"
    ));
    req.setAttribute("footer", Map.of(
        "year", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)),
        "company", "Space Dynamic Co.",
        "designHref","https://templatemo.com"
    ));

    // Forward to JSP (kept under WEB-INF so it cannot be hit directly)
    req.getRequestDispatcher("/WEB-INF/views/miyu/ga-ryu/index.jsp").forward(req, resp);
  }
//  @Override
//  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
//      throws ServletException, IOException {
//	sh = SessionHelper.getSessionHelper(req,resp,true);
//	req.setAttribute("pageHasHeader", false);
//	req.setAttribute("pageHasHomeButton", false);
//	req.setAttribute("pageHasTopMenu", false);
//	req.setAttribute("pageHasMainBanner", false);
//	req.setAttribute("pageHasAboutUs", false);
//	req.setAttribute("pageHasOurServices", false);
//	req.setAttribute("pageHasOurPortfolio", false);
//	req.setAttribute("pageHasBlog", false);
//	req.setAttribute("pageHasContact", false);
//	req.setAttribute("pageHasFooter", false);
//    // --- Page/meta/brand/nav ---
//    req.setAttribute("pageTitle", "SPACYDNYA — Live");
//    req.setAttribute("meta", Map.of("description","SEO & Marketing landing", "author","Your Company"));
//    req.setAttribute("brand", Map.of("prefix","SPAC", "suffix","DYNA"));
//    req.setAttribute("ctaText", "Contact Now");
//    req.setAttribute("nav", Map.of("homeHref", req.getContextPath() + "/miyu/ga-ryu/"));
//
//    // --- Hero block ---
//    Map<String,Object> hero = new HashMap<>();
//    hero.put("kicker", "WELCOME TO KIKYOSOFT"); //hero.put("kicker", "WELCOME TO SPACYDNYA");
//    hero.put("h1Before", "We Make");
//    hero.put("h1Em", "ERP System");
//    hero.put("h1Span", "and");
//    hero.put("h1After", " Provide Turnkey Solution");
//    hero.put("text", "Erp/V4 is customizable from your backend without touching the HTML.");
//    hero.put("linkHref", "https://templatemo.com/page/1");
//    hero.put("linkText", "TemplateMo");
//    hero.put("imageAlt", "team meeting");
//    req.setAttribute("hero", hero);
//
//    // --- Analyze form (GET to your analyzer endpoint) ---
//    req.setAttribute("analyze", Map.of(
//        "action", req.getContextPath() + "/analyze",
//        "method", "GET",
//        "placeholder", "demo.erpv4.com",
//        "button", "Goto Live Demo"
//    ));
//
//    // --- Services (icon paths are relative to /miyu/ga-ryu) ---
//    List<Map<String,String>> services = new ArrayList<>();
//    services.add(Map.of("icon","/miyu/ga-ryu/assets/images/service-icon-01.png","alt","reporting","title","Data Analysis","text","Lorem ipsum dolor sit amet...","delay","0.5s"));
//    services.add(Map.of("icon","/miyu/ga-ryu/assets/images/service-icon-02.png","alt","","title","Data Reporting","text","Lorem ipsum dolor sit amet...","delay","0.7s"));
//    services.add(Map.of("icon","/miyu/ga-ryu/assets/images/service-icon-03.png","alt","","title","Web Analytics","text","Lorem ipsum dolor sit amet...","delay","0.9s"));
//    services.add(Map.of("icon","/miyu/ga-ryu/assets/images/service-icon-04.png","alt","","title","SEO Suggestions","text","Lorem ipsum dolor sit amet...","delay","1.1s"));
//    req.setAttribute("services", services);
//
//    req.setAttribute("servicesBlock", Map.of(
//        "em","SEO",
//        "span","Project",
//        "paragraph","All wording/images below can be changed from Java or your DB."
//    ));
//
//    // --- Progress bars ---
//    List<Map<String,Object>> progressBars = List.of(
//        Map.of("cssClass","first-bar", "title","Website Analysis", "percent", 84),
//        Map.of("cssClass","second-bar", "title","SEO Reports",     "percent", 88),
//        Map.of("cssClass","third-bar",  "title","Page Optimizations", "percent", 94)
//    );
//    req.setAttribute("progressBars", progressBars);
//
//    // --- Portfolio grid ---
//    List<Map<String,String>> portfolio = new ArrayList<>();
//    portfolio.add(Map.of("href","#","delay","0.3s","title","SEO Analysis","text","Lorem ipsum...","image","/miyu/ga-ryu/assets/images/portfolio-image.png"));
//    portfolio.add(Map.of("href","#","delay","0.4s","title","Website Reporting","text","Lorem ipsum...","image","/miyu/ga-ryu/assets/images/portfolio-image.png"));
//    portfolio.add(Map.of("href","#","delay","0.5s","title","Performance Tests","text","Lorem ipsum...","image","/miyu/ga-ryu/assets/images/portfolio-image.png"));
//    portfolio.add(Map.of("href","#","delay","0.6s","title","Data Analysis","text","Lorem ipsum...","image","/miyu/ga-ryu/assets/images/portfolio-image.png"));
//    req.setAttribute("portfolio", portfolio);
//
//    // --- Blog main + list ---
//    req.setAttribute("blogMain", Map.of(
//        "href","#",
//        "image","/miyu/ga-ryu/assets/images/big-blog-thumb.jpg",
//        "alt","Workspace Desktop",
//        "date","24 Mar 2021",
//        "author","TemplateMo",
//        "category","Branding",
//        "title","SEO Agency & Digital Marketing",
//        "excerpt","Lorem ipsum dolor sit amet, consectetur adipiscing elit...",
//        "cta","Discover More"
//    ));
//    req.setAttribute("blogPosts", List.of(
//        Map.of("date","18 Mar 2021","href","#","title","New Websites & Backlinks","excerpt","Lorem ipsum...","thumb","/miyu/ga-ryu/assets/images/blog-thumb-01.jpg"),
//        Map.of("date","14 Mar 2021","href","#","title","SEO Analysis & Content Ideas","excerpt","Lorem ipsum...","thumb","/miyu/ga-ryu/assets/images/blog-thumb-01.jpg"),
//        Map.of("date","06 Mar 2021","href","#","title","SEO Tips & Digital Marketing","excerpt","Lorem ipsum...","thumb","/miyu/ga-ryu/assets/images/blog-thumb-01.jpg")
//    ));
//
//    // --- Contact & footer ---
//    req.setAttribute("contact", Map.of(
//        "heading","Feel Free To Send Us a Message About Your Website Needs",
//        "paragraph","You control this text from your backend.",
//        "phone","010-020-0340",
//        "formAction", req.getContextPath() + "/contact",
//        "formMethod","post"
//    ));
//    req.setAttribute("footer", Map.of(
//        "year", String.valueOf(Calendar.getInstance().get(Calendar.YEAR)),
//        "company", "Space Dynamic Co.",
//        "designHref","https://templatemo.com"
//    ));
//
//    // Forward to JSP (kept under WEB-INF so it cannot be hit directly)
//    req.getRequestDispatcher("/WEB-INF/views/miyu/ga-ryu/index.jsp").forward(req, resp);
//  }
}
