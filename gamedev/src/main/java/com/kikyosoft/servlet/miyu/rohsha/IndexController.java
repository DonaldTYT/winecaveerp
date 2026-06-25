package com.kikyosoft.servlet.miyu.rohsha;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet(urlPatterns = {
    "/miyu/roh-sha",
    "/miyu/roh-sha/",
    "/miyu/roh-sha/index"
})
public class IndexController extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // ---- META / HEAD ----
    req.setAttribute("pageTitle", "Blogy — Free Bootstrap 5 Website Template by Untree.co");
    req.setAttribute("metaAuthor", "Untree.co");
    req.setAttribute("metaDescription", "");
    req.setAttribute("metaKeywords", "bootstrap, bootstrap5");

    // ---- STATIC ASSETS ROOT ----
    // Files live under: webapp/miyu/roh-sha/{css,js,images,fonts,...}
    req.setAttribute("assetsBase", "/miyu/roh-sha");
    req.setAttribute("favicon", "/miyu/roh-sha/favicon.png");

    // ---- NAV / HEADER ----
    Map<String,String> header = new HashMap<>();
    header.put("brand", "Blogy");
    header.put("brandDot", "."); // colored dot
    header.put("getStartedHref", "about.html");
    req.setAttribute("header", header);

    // top menu (supports nested)
    List<Map<String,Object>> nav = new ArrayList<>();
    nav.add(Map.of("text","Home","href","index.html","active","true"));
    Map<String,Object> pages = new LinkedHashMap<>();
    pages.put("text","Pages");
    pages.put("href","category.html");
    pages.put("children", List.of(
      Map.of("text","Search Result","href","search-result.html"),
      Map.of("text","Blog","href","blog.html"),
      Map.of("text","Blog Single","href","single.html"),
      Map.of("text","Category","href","category.html"),
      Map.of("text","About","href","about.html"),
      Map.of("text","Contact Us","href","contact.html"),
      Map.of("text","Menu One","href","#"),
      Map.of("text","Menu Two","href","#"),
      Map.of("text","Dropdown","href","#","children", List.of(
        Map.of("text","Sub Menu One","href","#"),
        Map.of("text","Sub Menu Two","href","#"),
        Map.of("text","Sub Menu Three","href","#")
      ))
    ));
    nav.add(pages);
    nav.add(Map.of("text","Culture","href","category.html"));
    nav.add(Map.of("text","Business","href","category.html"));
    nav.add(Map.of("text","Politics","href","category.html"));
    req.setAttribute("nav", nav);

    // ---- RETRO LAYOUT (hero grid) ----
    // columns: [ [left-1, left-2], [center-1], [right-1, right-2] ]
    List<List<Map<String,String>>> retroCols = new ArrayList<>();
    retroCols.add(List.of(
      Map.of("href","single.html","img","images/img_2_horizontal.jpg","date","Apr. 14th, 2022","title","AI can now kill those annoying cookie pop-ups"),
      Map.of("href","single.html","img","images/img_5_horizontal.jpg","date","Apr. 14th, 2022","title","Don’t assume your user data in the cloud is safe")
    ));
    retroCols.add(List.of(
      Map.of("href","single.html","img","images/img_1_vertical.jpg","date","Apr. 14th, 2022","title","Why is my internet so slow?")
    ));
    retroCols.add(List.of(
      Map.of("href","single.html","img","images/img_3_horizontal.jpg","date","Apr. 14th, 2022","title","Startup vs corporate: What job suits you best?"),
      Map.of("href","single.html","img","images/img_4_horizontal.jpg","date","Apr. 14th, 2022","title","Thought you loved Python? Wait until you meet Rust")
    ));
    req.setAttribute("retroCols", retroCols);

    // ---- SECTION: Business ----
    Map<String,String> businessHdr = Map.of(
      "title","Business",
      "viewAllHref","category.html",
      "viewAllText","View All"
    );
    req.setAttribute("businessHdr", businessHdr);

    List<Map<String,String>> businessFeatured = List.of(
      Map.of("href","single.html","img","images/img_1_sq.jpg","date","Apr. 14th, 2022",
             "title","Thought you loved Python? Wait until you meet Rust",
             "excerpt","Lorem ipsum dolor sit amet consectetur adipisicing elit. Unde, nobis ea quis inventore vel voluptas.",
             "ctaHref","single.html","ctaText","Read More"),
      Map.of("href","single.html","img","images/img_2_sq.jpg","date","Apr. 14th, 2022",
             "title","Startup vs corporate: What job suits you best?",
             "excerpt","Lorem ipsum dolor sit amet consectetur adipisicing elit. Unde, nobis ea quis inventore vel voluptas.",
             "ctaHref","single.html","ctaText","Read More")
    );
    req.setAttribute("businessFeatured", businessFeatured);

    List<Map<String,String>> businessSide = List.of(
      Map.of("date","Apr. 14th, 2022","title","Don’t assume your user data in the cloud is safe","href","single.html",
             "excerpt","Lorem ipsum dolor sit amet consectetur adipisicing elit. Unde, nobis ea quis inventore vel voluptas.",
             "readMoreHref","#","readMoreText","Continue Reading"),
      Map.of("date","Apr. 14th, 2022","title","Meta unveils fees on metaverse sales","href","single.html",
             "excerpt","Lorem ipsum dolor sit amet consectetur adipisicing elit. Unde, nobis ea quis inventore vel voluptas.",
             "readMoreHref","#","readMoreText","Continue Reading"),
      Map.of("date","Apr. 14th, 2022","title","UK sees highest inflation in 30 years","href","single.html",
             "excerpt","Lorem ipsum dolor sit amet consectetur adipisicing elit. Unde, nobis ea quis inventore vel voluptas.",
             "readMoreHref","#","readMoreText","Continue Reading")
    );
    req.setAttribute("businessSide", businessSide);

    // ---- SECTION: posts-entry-sm (4 cards) ----
    List<Map<String,String>> fourUp = List.of(
      Map.of("href","single.html","img","images/img_1_horizontal.jpg","date","Apr. 14th, 2022","title","Thought you loved Python? Wait until you meet Rust","excerpt","Lorem ipsum dolor sit amet consectetur adipisicing elit.","readMoreHref","#"),
      Map.of("href","single.html","img","images/img_2_horizontal.jpg","date","Apr. 14th, 2022","title","Startup vs corporate: What job suits you best?","excerpt","Lorem ipsum dolor sit amet consectetur adipisicing elit.","readMoreHref","#"),
      Map.of("href","single.html","img","images/img_3_horizontal.jpg","date","Apr. 14th, 2022","title","UK sees highest inflation in 30 years","excerpt","Lorem ipsum dolor sit amet consectetur adipisicing elit.","readMoreHref","#"),
      Map.of("href","single.html","img","images/img_4_horizontal.jpg","date","Apr. 14th, 2022","title","Don’t assume your user data in the cloud is safe","excerpt","Lorem ipsum dolor sit amet consectetur adipisicing elit.","readMoreHref","#")
    );
    req.setAttribute("fourUp", fourUp);

    // ---- SECTION: Culture ----
    Map<String,String> cultureHdr = Map.of(
      "title","Culture",
      "viewAllHref","category.html",
      "viewAllText","View All"
    );
    req.setAttribute("cultureHdr", cultureHdr);

    List<Map<String,String>> cultureFeatured = businessFeatured;  // same two-card layout/text as sample
    List<Map<String,String>> cultureSide = businessSide;
    req.setAttribute("cultureFeatured", cultureFeatured);
    req.setAttribute("cultureSide", cultureSide);

    // ---- SECTION: Politics (9 posts as list) ----
    List<Map<String,String>> politics = new ArrayList<>();
    politics.add(Map.of("img","images/img_7_horizontal.jpg","title","Startup vs corporate: What job suits you best?","href","single.html","authorImg","images/person_1.jpg","author","David Anderson","date","July 19, 2019","excerpt","Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quo sunt tempora dolor laudantium sed optio, explicabo ad deleniti impedit facilis fugit recusandae! Illo, aliquid, dicta beatae quia porro id est."));
    politics.add(Map.of("img","images/img_6_horizontal.jpg","title","Startup vs corporate: What job suits you best?","href","single.html","authorImg","images/person_2.jpg","author","David Anderson","date","July 19, 2019","excerpt","Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quo sunt tempora dolor laudantium sed optio, explicabo ad deleniti impedit facilis fugit recusandae! Illo, aliquid, dicta beatae quia porro id est."));
    politics.add(Map.of("img","images/img_5_horizontal.jpg","title","Startup vs corporate: What job suits you best?","href","single.html","authorImg","images/person_3.jpg","author","David Anderson","date","July 19, 2019","excerpt","Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quo sunt tempora dolor laudantium sed optio, explicabo ad deleniti impedit facilis fugit recusandae! Illo, aliquid, dicta beatae quia porro id est."));
    politics.add(Map.of("img","images/img_4_horizontal.jpg","title","Startup vs corporate: What job suits you best?","href","single.html","authorImg","images/person_4.jpg","author","David Anderson","date","July 19, 2019","excerpt","Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quo sunt tempora dolor laudantium sed optio, explicabo ad deleniti impedit facilis fugit recusandae! Illo, aliquid, dicta beatae quia porro id est."));
    politics.add(Map.of("img","images/img_3_horizontal.jpg","title","Startup vs corporate: What job suits you best?","href","single.html","authorImg","images/person_5.jpg","author","David Anderson","date","July 19, 2019","excerpt","Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quo sunt tempora dolor laudantium sed optio, explicabo ad deleniti impedit facilis fugit recusandae! Illo, aliquid, dicta beatae quia porro id est."));
    politics.add(Map.of("img","images/img_2_horizontal.jpg","title","Startup vs corporate: What job suits you best?","href","single.html","authorImg","images/person_4.jpg","author","David Anderson","date","July 19, 2019","excerpt","Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quo sunt tempora dolor laudantium sed optio, explicabo ad deleniti impedit facilis fugit recusandae! Illo, aliquid, dicta beatae quia porro id est."));
    politics.add(Map.of("img","images/img_1_horizontal.jpg","title","Startup vs corporate: What job suits you best?","href","single.html","authorImg","images/person_3.jpg","author","David Anderson","date","July 19, 2019","excerpt","Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quo sunt tempora dolor laudantium sed optio, explicabo ad deleniti impedit facilis fugit recusandae! Illo, aliquid, dicta beatae quia porro id est."));
    politics.add(Map.of("img","images/img_4_horizontal.jpg","title","Startup vs corporate: What job suits you best?","href","single.html","authorImg","images/person_2.jpg","author","David Anderson","date","July 19, 2019","excerpt","Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quo sunt tempora dolor laudantium sed optio, explicabo ad deleniti impedit facilis fugit recusandae! Illo, aliquid, dicta beatae quia porro id est."));
    politics.add(Map.of("img","images/img_3_horizontal.jpg","title","Startup vs corporate: What job suits you best?","href","single.html","authorImg","images/person_5.jpg","author","David Anderson","date","July 19, 2019","excerpt","Lorem ipsum dolor sit amet, consectetur adipisicing elit. Quo sunt tempora dolor laudantium sed optio, explicabo ad deleniti impedit facilis fugit recusandae! Illo, aliquid, dicta beatae quia porro id est."));
    req.setAttribute("politicsHdr", Map.of("title","Politics","viewAllHref","category.html","viewAllText","View All"));
    req.setAttribute("politics", politics);

    // ---- SECTION: Travel (retro alt) ----
    req.setAttribute("travelHdr", Map.of("title","Travel","viewAllHref","category.html","viewAllText","View All"));
    req.setAttribute("travelFeatured", Map.of("href","single.html","img","images/img_2_vertical.jpg","date","February 12, 2019","title","Meta unveils fees on metaverse sales"));
    req.setAttribute("travelLarge", Map.of("href","single.html","img","images/img_1_horizontal.jpg","date","February 12, 2019","title","AI can now kill those annoying cookie pop-ups"));
    req.setAttribute("travelTwo",
      List.of(
        Map.of("href","single.html","img","images/img_2_sq.jpg","date","February 12, 2019","title","Don’t assume your user data in the cloud is safe"),
        Map.of("href","single.html","img","images/img_3_sq.jpg","date","February 12, 2019","title","Startup vs corporate: What job suits you best?")
      )
    );

    // ---- FOOTER ----
    req.setAttribute("footerAboutTitle", "About");
    req.setAttribute("footerAboutText", "Far far away, behind the word mountains, far from the countries Vokalia and Consonantia, there live the blind texts.");
    req.setAttribute("footerSocial", List.of("instagram","twitter","facebook","linkedin","pinterest","dribbble"));

    req.setAttribute("footerCompanyTitle", "Company");
    req.setAttribute("footerCompanyCol1", List.of("About us","Services","Vision","Mission","Terms","Privacy"));
    req.setAttribute("footerCompanyCol2", List.of("Partners","Business","Careers","Blog","FAQ","Creative"));

    List<Map<String,String>> recent = List.of(
      Map.of("img","images/img_1_sq.jpg","title","There’s a Cool New Way for Men to Wear Socks and Sandals","date","March 15, 2018"),
      Map.of("img","images/img_2_sq.jpg","title","There’s a Cool New Way for Men to Wear Socks and Sandals","date","March 15, 2018"),
      Map.of("img","images/img_3_sq.jpg","title","There’s a Cool New Way for Men to Wear Socks and Sandals","date","March 15, 2018")
    );
    req.setAttribute("footerRecentTitle","Recent Post Entry");
    req.setAttribute("footerRecent", recent);

    req.setAttribute("copyrightPrefix","Copyright");
    req.setAttribute("copyrightBrand","Untree.co");
    req.setAttribute("copyrightDistributor","ThemeWagon");

    // ---- FORWARD ----
    req.getRequestDispatcher("/WEB-INF/views/miyu/roh-sha/index.jsp").forward(req, resp);
  }
}
