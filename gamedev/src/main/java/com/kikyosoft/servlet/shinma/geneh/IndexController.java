package com.kikyosoft.servlet.shinma.geneh;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet(urlPatterns = {
    "/shinma/gen-eh",
    "/shinma/gen-eh/",
    "/shinma/gen-eh/index"
})
public class IndexController extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // ---- HEAD/META ----
    req.setAttribute("pageTitle", "Index - Sailor Bootstrap Template");
    req.setAttribute("metaDescription", "");
    req.setAttribute("metaKeywords", "");
    req.setAttribute("assetsBase", "/shinma/gen-eh");                     // <-- mini-site root
    req.setAttribute("favIcon",   "/shinma/gen-eh/assets/img/favicon.png");
    req.setAttribute("appleIcon", "/shinma/gen-eh/assets/img/apple-touch-icon.png");

    // ---- BRAND / NAV ----
    Map<String, Object> brand = new HashMap<>();
    brand.put("logoText", "ERP Solution for SME");
    brand.put("logoImg", "assets/img/erpv4_logo.png");
    brand.put("homeHref", "#hero");
    req.setAttribute("brand", brand);

    List<Map<String, Object>> nav = new ArrayList<>();
    nav.add(link("#hero", "Home", true));
    /*
    Map<String, Object> aboutDrop = new LinkedHashMap<>();
    aboutDrop.put("text", "About");
    aboutDrop.put("href", "about.html");
    aboutDrop.put("children", List.of(
        link("team.html", "Team"),
        link("testimonials.html", "Testimonials"),
        submenu("Deep Dropdown", List.of(
            link("#", "Deep Dropdown 1"),
            link("#", "Deep Dropdown 2"),
            link("#", "Deep Dropdown 3"),
            link("#", "Deep Dropdown 4"),
            link("#", "Deep Dropdown 5")
        ))
    ));
    nav.add(aboutDrop);
    */
    nav.add(link("#about", "About"));
    //nav.add(link("services.html", "Services"));
    nav.add(link("#portfolio", "Products"));
    /*
    nav.add(link("pricing.html", "Pricing"));
    nav.add(link("blog.html", "Blog"));
    nav.add(link("contact.html", "Contact"));
    */
    req.setAttribute("nav", nav);
    req.setAttribute("cta", Map.of("text", "Live Demo", "href", "#services"));

    // ---- HERO (carousel) ----
    List<Map<String, String>> heroSlides = new ArrayList<>();
    heroSlides.add(slide("assets/img/datacentre.jpg","Welcome to ErpV4", "ERP/V4 is an intelligent resource management platform tailored for small and medium-sized enterprises. Originally developed for businesses in Hong Kong and Macau, it can be easily extended to support global operations with proper configuration."));
    heroSlides.add(slide("assets/img/datacentre.jpg","Fully Integrated","The system seamlessly integrates core functions—including sales, logistics, CRM, finance, procurement, manufacturing, and HR—into a unified platform that enables resource sharing and workflow collaboration."));
    heroSlides.add(slide("assets/img/datacentre.jpg","Scalable","Scalable and adaptable for companies of all sizes, ERP/V4 streamlines management processes, enhances operational efficiency, and drives digital transformation and sustainable business growth."));
    /*
    heroSlides.add(slide("assets/img/hero-carousel/hero-carousel-1.jpg",
        "Welcome to ErpV4", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua."));
        */
    /*
    heroSlides.add(slide("assets/img/hero-carousel/hero-carousel-2.jpg",
        "At vero eos et accusamus", "Nam libero tempore, cum soluta nobis est eligendi optio cumque nihil impedit quo minus id quod maxime placeat facere possimus, omnis voluptas assumenda est, omnis dolor repellendus. Temporibus autem quibusdam et aut officiis debitis aut."));
    heroSlides.add(slide("assets/img/hero-carousel/hero-carousel-3.jpg",
        "Temporibus autem quibusdam", "Beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt omnis iste natus error sit voluptatem accusantium."));
        */
    req.setAttribute("heroSlides", heroSlides);
    req.setAttribute("heroCtaText", "Get Started");
    req.setAttribute("heroCtaHref", "#featured-services");

    // ---- ABOUT ----
    req.setAttribute("aboutSection", Map.of(
        "title", "About",
        "subtitle", "About Us",
        "lead", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        "bullets", List.of(
            "Ullamco laboris nisi ut aliquip ex ea commodo consequat.",
            "Duis aute irure dolor in reprehenderit in voluptate velit.",
            "Ullamco laboris nisi ut aliquip ex ea commodo"
        ),
        "moreText", "Ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.",
        "moreHref", "about.html",
        "moreLabel", "Read More",
        "leftImg", "assets/img/hero-carousel/hero-carousel-1.jpg" // not used (this template uses just text in left) — keeping placeholder
    ));
    req.setAttribute("aboutSideHref", "about.html");

    // ---- CLIENT LOGOS ----
    req.setAttribute("clients", List.of(
        "assets/img/clients/winecave.png"
        ,"assets/img/clients/afs.png"
        ,"assets/img/clients/acwine.jpg"
        ,"assets/img/clients/hapyik.png"
        ,"assets/img/clients/vincero.png"
        ,"assets/img/clients/edu.jpg"
    		/*
        "assets/img/clients/client-1.png",
        "assets/img/clients/client-2.png",
        "assets/img/clients/client-3.png",
        "assets/img/clients/client-4.png",
        "assets/img/clients/client-5.png",
        "assets/img/clients/client-6.png"
        */
    ));

    // ---- SERVICES (6 cards) ----
    List<Map<String, String>> services = new ArrayList<>();
    services.add(svc("bi bi-briefcase", "Wholesale Sales Order Processing", "Voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident", "#"));
    services.add(svc("bi bi-card-checklist", "Payroll and Attendence", "Minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat tarad limino ata", "#"));
    /*
    services.add(svc("bi bi-bar-chart", "Distributer Sales Order Processing", "Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur", "#"));
    */
    services.add(svc("bi bi-brightness-high", "Building Management", "At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque", "#"));
    services.add(svc("bi bi-calendar4-week", "ERP for printing industry", "Et harum quidem rerum facilis est et expedita distinctio. Nam libero tempore, cum soluta nobis est eligendi", "#"));
    req.setAttribute("services", services);

    // ---- PORTFOLIO ----
    req.setAttribute("portfolioTitle", "Screen Shots");
    req.setAttribute("portfolioSubtitle", "Necessitatibus eius consequatur");
    req.setAttribute("portfolioFilters", List.of(
        Map.of("label","All","filter","*","active", true),
        Map.of("label","App","filter",".filter-app"),
        Map.of("label","Card","filter",".filter-product"),
        Map.of("label","Web","filter",".filter-branding")
    ));

    List<Map<String, Object>> portfolioItems = new ArrayList<>();
    portfolioItems.add(pf("assets/img/screenshot/0001.jpg","App 1","Login","assets/img/screenshot/0001.jpg","portfolio-details.html","filter-app"));
    portfolioItems.add(pf("assets/img/masonry-portfolio/masonry-portfolio-1.jpg","App 1","Lorem ipsum, dolor sit","assets/img/masonry-portfolio/masonry-portfolio-1.jpg","portfolio-details.html","filter-app"));
    portfolioItems.add(pf("assets/img/masonry-portfolio/masonry-portfolio-2.jpg","Product 1","Lorem ipsum, dolor sit","assets/img/masonry-portfolio/masonry-portfolio-2.jpg","portfolio-details.html","filter-product"));
    portfolioItems.add(pf("assets/img/masonry-portfolio/masonry-portfolio-3.jpg","Branding 1","Lorem ipsum, dolor sit","assets/img/masonry-portfolio/masonry-portfolio-3.jpg","portfolio-details.html","filter-branding"));
    portfolioItems.add(pf("assets/img/masonry-portfolio/masonry-portfolio-4.jpg","App 2","Lorem ipsum, dolor sit","assets/img/masonry-portfolio/masonry-portfolio-4.jpg","portfolio-details.html","filter-app"));
    portfolioItems.add(pf("assets/img/masonry-portfolio/masonry-portfolio-5.jpg","Product 2","Lorem ipsum, dolor sit","assets/img/masonry-portfolio/masonry-portfolio-5.jpg","portfolio-details.html","filter-product"));
    portfolioItems.add(pf("assets/img/masonry-portfolio/masonry-portfolio-6.jpg","Branding 2","Lorem ipsum, dolor sit","assets/img/masonry-portfolio/masonry-portfolio-6.jpg","portfolio-details.html","filter-branding"));
    portfolioItems.add(pf("assets/img/masonry-portfolio/masonry-portfolio-7.jpg","App 3","Lorem ipsum, dolor sit","assets/img/masonry-portfolio/masonry-portfolio-7.jpg","portfolio-details.html","filter-app"));
    portfolioItems.add(pf("assets/img/masonry-portfolio/masonry-portfolio-8.jpg","Product 3","Lorem ipsum, dolor sit","assets/img/masonry-portfolio/masonry-portfolio-8.jpg","portfolio-details.html","filter-product"));
    portfolioItems.add(pf("assets/img/masonry-portfolio/masonry-portfolio-9.jpg","Branding 3","Lorem ipsum, dolor sit","assets/img/masonry-portfolio/masonry-portfolio-9.jpg","portfolio-details.html","filter-branding"));
    req.setAttribute("portfolioItems", portfolioItems);

    // ---- FOOTER ----
    req.setAttribute("footerBrand", "Contact Us");
    req.setAttribute("copyRight", "KikyoSoft");
    req.setAttribute("footerContact", Map.of(
        "addr1", "Rm 1208 Cyberport 2",
        "addr2", "Hong Kong",
        "phoneLabel", "Phone:",
        "phone", "+852 3005 8888",
        "emailLabel", "Email:",
        "email", "erpv4@hellovoice.com"
    ));
    req.setAttribute("footerLinks1Title", "Useful Links");
    req.setAttribute("footerLinks1", List.of("Home","About us","Services","Terms of service","Privacy policy"));
    req.setAttribute("footerLinks2Title", "Our Services");
    req.setAttribute("footerLinks2", List.of("Web Design","Web Development","Product Management","Marketing","Graphic Design"));
    req.setAttribute("newsletterTitle", "Leave Message");
    req.setAttribute("newsletterNote", "Subscribe to our newsletter and receive the latest news about our products and services!");
    req.setAttribute("creditsHtml",
        "Designed by <a href=\"https://bootstrapmade.com/\">BootstrapMade</a> Distributed by <a href=\"https://themewagon.com\">ThemeWagon</a>");

    // Forward to JSP
    req.getRequestDispatcher("/WEB-INF/views/shinma/gen-eh/index.jsp").forward(req, resp);
  }

  // ---- helpers ----
  private static Map<String,Object> link(String href, String text) {
    return link(href, text, false);
  }
  private static Map<String,Object> link(String href, String text, boolean active) {
    Map<String,Object> m = new LinkedHashMap<>();
    m.put("href", href);
    m.put("text", text);
    if (active) m.put("active", true);
    return m;
  }
  private static Map<String,Object> submenu(String text, List<Map<String,Object>> children) {
    Map<String,Object> m = new LinkedHashMap<>();
    m.put("text", text);
    m.put("href", "#");
    m.put("children", children);
    return m;
  }
  private static Map<String,String> slide(String img, String title, String desc) {
    Map<String,String> m = new LinkedHashMap<>();
    m.put("img", img);
    m.put("title", title);
    m.put("desc", desc);
    return m;
  }
  private static Map<String,String> svc(String iconClass, String title, String desc, String href) {
    Map<String,String> m = new LinkedHashMap<>();
    m.put("icon", iconClass);
    m.put("title", title);
    m.put("desc", desc);
    m.put("href", href);
    return m;
  }
  private static Map<String,Object> pf(String img, String title, String desc, String lightboxHref, String detailsHref, String filterClass) {
    Map<String,Object> m = new LinkedHashMap<>();
    m.put("img", img);
    m.put("title", title);
    m.put("desc", desc);
    m.put("lightbox", lightboxHref);
    m.put("details", detailsHref);
    m.put("filter", filterClass);
    return m;
  }
}
