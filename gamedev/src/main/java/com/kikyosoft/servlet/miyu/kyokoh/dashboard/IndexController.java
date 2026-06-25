package com.kikyosoft.servlet.miyu.kyokoh.dashboard;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.*;

@WebServlet(urlPatterns = {
    "/miyu/kyo-koh/dashboard",
    "/miyu/kyo-koh/dashboard/",
    "/miyu/kyo-koh/dashboard/index"
})
public class IndexController extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {

    // --- optional: prevent stale caching during development ---
    resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
    resp.setHeader("Pragma", "no-cache");
    resp.setHeader("Expires", "0");

    // ==== CONFIG: where your static files live under webapp ====
    final String BASE = "/miyu/kyo-koh";             // folder that contains dashboard + assets
    final String ASSETS = BASE + "/assets";          // css/js/fonts/images

    // ---- META / HEAD ----
    req.setAttribute("pageTitle", "Home | Mantis Bootstrap 5 Admin Template");
    req.setAttribute("metaDescription", "Mantis is made using Bootstrap 5 design framework. Download the free admin template & use it for your project.");
    req.setAttribute("metaKeywords", "Mantis, Dashboard UI Kit, Bootstrap 5, Admin Template, Admin Dashboard, CRM, CMS, Bootstrap Admin Template");
    req.setAttribute("metaAuthor", "CodedThemes");

    // ---- STATIC PATHS (used by JSP with <c:url> and ${ctx}) ----
    req.setAttribute("assetsBase", ASSETS);                              // e.g. /miyu/kyo-koh/assets
    req.setAttribute("favIcon",   ASSETS + "/images/favicon.svg");
//    req.setAttribute("brandLogo", ASSETS + "/images/logo-dark.svg");

    // ---- BODY DATA-* PRESET FLAGS ----
    req.setAttribute("pcPreset", "preset-1");
    req.setAttribute("pcDir", "ltr");
    req.setAttribute("pcTheme", "light");

    // ---- USER ----
    Map<String,String> user = new HashMap<>();
    user.put("name", "Stebin Ben");
    user.put("role", "UI/UX Designer");
    user.put("avatar", ASSETS + "/images/user/avatar-2.jpg");
    req.setAttribute("user", user);

    // ---- SIDEBAR MENU ----
    List<Map<String,Object>> menu = new ArrayList<>();
    menu.add(Map.of("type","link","href", BASE + "/dashboard","icon","ti ti-dashboard","text","Dashboard","active", true));
    menu.add(Map.of("type","caption","label","UI Components","icon","ti ti-dashboard"));
    menu.add(Map.of("type","link","href","/elements/typography","icon","ti ti-typography","text","Typography"));
    menu.add(Map.of("type","link","href","/elements/color","icon","ti ti-color-swatch","text","Color"));
    menu.add(Map.of("type","link","href","/elements/icons","icon","ti ti-plant-2","text","Icons"));

    menu.add(Map.of("type","caption","label","Pages","icon","ti ti-news"));
    menu.add(Map.of("type","link","href","/pages/login","icon","ti ti-lock","text","Login"));
    menu.add(Map.of("type","link","href","/pages/register","icon","ti ti-user-plus","text","Register"));

    menu.add(Map.of("type","caption","label","Other","icon","ti ti-brand-chrome"));

    Map<String,Object> lvl23 = new LinkedHashMap<>();
    lvl23.put("text","Level 2.3");
    lvl23.put("icon","ti ti-menu");
    lvl23.put("children", List.of(
        Map.of("text","Level 3.1"),
        Map.of("text","Level 3.2"),
        Map.of("text","Level 3.3",
            "children", List.of(
                Map.of("text","Level 4.1"),
                Map.of("text","Level 4.2")
            ))
    ));

    menu.add(Map.of(
        "type","submenu",
        "text","Menu levels",
        "icon","ti ti-menu",
        "children", List.of(
            Map.of("text","Level 2.1"),
            Map.of("text","Level 2.2",
                "children", List.of(
                    Map.of("text","Level 3.1"),
                    Map.of("text","Level 3.2"),
                    Map.of("text","Level 3.3",
                        "children", List.of(
                            Map.of("text","Level 4.1"),
                            Map.of("text","Level 4.2")
                        ))
                )),
            lvl23
        )));
    menu.add(Map.of("type","link","href","/other/sample-page","icon","ti ti-brand-chrome","text","Sample page"));
    req.setAttribute("menu", menu);

    // ---- KPI CARDS ----
    List<Map<String,String>> kpis = new ArrayList<>();
    kpis.add(Map.of("title","Total Page Views","value","4,42,236","badgeIcon","ti ti-trending-up","badgeClass","bg-light-primary border border-primary","badgeText","59.3%","noteHtml","You made an extra <span class='text-primary'>35,000</span> this year"));
    kpis.add(Map.of("title","Total Users","value","78,250","badgeIcon","ti ti-trending-up","badgeClass","bg-light-success border border-success","badgeText","70.5%","noteHtml","You made an extra <span class='text-success'>8,900</span> this year"));
    kpis.add(Map.of("title","Total Order","value","18,800","badgeIcon","ti ti-trending-down","badgeClass","bg-light-warning border border-warning","badgeText","27.4%","noteHtml","You made an extra <span class='text-warning'>1,943</span> this year"));
    kpis.add(Map.of("title","Total Sales","value","$35,078","badgeIcon","ti ti-trending-down","badgeClass","bg-light-danger border border-danger","badgeText","27.4%","noteHtml","You made an extra <span class='text-danger'>$20,395</span> this year"));
    req.setAttribute("kpis", kpis);

    // ---- BREADCRUMB ----
    req.setAttribute("breadcrumb", List.of(
        Map.of("text","Home", "href", BASE + "/dashboard"),
        Map.of("text","Dashboard"),
        Map.of("text","Home", "current","true")
    ));

    // ---- TABLE: Recent Orders ----
    List<Map<String,String>> orders = new ArrayList<>();
    orders.add(Map.of("no","84564564","product","Camera Lens","total","40","status","Rejected","statusClass","text-danger","amount","$40,570"));
    orders.add(Map.of("no","84564564","product","Laptop","total","300","status","Pending","statusClass","text-warning","amount","$180,139"));
    orders.add(Map.of("no","84564564","product","Mobile","total","355","status","Approved","statusClass","text-success","amount","$180,139"));
    req.setAttribute("orders", orders);

    // ---- Analytics list ----
    req.setAttribute("analytics", List.of(
        Map.of("title","Company Finance Growth", "value","+45.14%"),
        Map.of("title","Company Expenses Ratio", "value","0.58%"),
        Map.of("title","Business Risk Cases",   "value","Low")
    ));

    // ---- Transactions ----
    List<Map<String,String>> txs = new ArrayList<>();
    txs.add(Map.of("icon","ti ti-gift","iconClass","text-success bg-light-success","title","Order #002434","time","Today, 2:00 AM","amount","+ $1,430","percent","78%"));
    txs.add(Map.of("icon","ti ti-message-circle","iconClass","text-primary bg-light-primary","title","Order #984947","time","5 August, 1:45 PM","amount","- $302","percent","8%"));
    txs.add(Map.of("icon","ti ti-settings","iconClass","text-danger bg-light-danger","title","Order #988784","time","7 hours ago","amount","- $682","percent","16%"));
    req.setAttribute("transactions", txs);

    // ---- Footer ----
    req.setAttribute("footerHomeHref", "/index");
    req.setAttribute("footerNote", "Mantis \u2665 crafted by Team Codedthemes. Distributed by ThemeWagon.");

    // ---- JS preset helpers (strings) ----
    req.setAttribute("layoutLight", "light");
    req.setAttribute("boxContainer", "false");
    req.setAttribute("rtl", "false");
    req.setAttribute("preset", "preset-1");
    req.setAttribute("fontFamily", "Public-Sans");

    // ---- Forward to JSP view (under WEB-INF) ----
    req.getRequestDispatcher("/WEB-INF/views/miyu/kyo-koh/dashboard/index.jsp").forward(req, resp);
  }
}
