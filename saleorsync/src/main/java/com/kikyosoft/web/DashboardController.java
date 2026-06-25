package com.kikyosoft.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class DashboardController {

  @GetMapping({"/dashboard", "/larva/dashboard"})
  public String dashboard(Model model) {
    // Page/meta
    model.addAttribute("pageTitle", "Home | Mantis (Thymeleaf)");
    model.addAttribute("pageHeading", "Home");
    model.addAttribute("showPreloader", false);
    model.addAttribute("meta", new Meta(
        "Mantis is made using Bootstrap 5 design framework. Download the free admin template & use it for your project.",
        "Mantis, Dashboard UI Kit, Bootstrap 5, Admin Template, Admin Dashboard, CRM, CMS, Bootstrap Admin Template",
        "CodedThemes"));

    // Layout flags
    model.addAttribute("layout", new Layout("preset-1", "ltr", "light", "false", "false", "Public-Sans"));

    // Sidebar
    model.addAttribute("navCaptions", List.of(
        new Caption("UI Components", "ti ti-dashboard"),
        new Caption("Pages", "ti ti-news"),
        new Caption("Other", "ti ti-brand-chrome")
    ));
    model.addAttribute("navItems", List.of(
        new Nav("/dashboard", "ti ti-dashboard", "Dashboard"),
        new Nav("/elements/typography", "ti ti-typography", "Typography"),
        new Nav("/elements/colors", "ti ti-color-swatch", "Color"),
        new Nav("/elements/icons", "ti ti-plant-2", "Icons"),
        new Nav("/login", "ti ti-lock", "Login"),
        new Nav("/register", "ti ti-user-plus", "Register")
    ));
    model.addAttribute("menuLevels", true);
    model.addAttribute("promo", new Promo(
        "Upgrade To Pro",
        "To get more features and components",
        "https://codedthemes.com/item/berry-bootstrap-5-admin-template/",
        "Buy Now"
    ));

    // Header user + messages
    model.addAttribute("user", new User("Stebin Ben", "UI/UX Designer", "/larva/assets/images/user/avatar-2.jpg"));
    model.addAttribute("messages", List.of(
        new Msg("/larva/assets/images/user/avatar-2.jpg", "3:00 AM", "It's <b>Cristina danny's</b> birthday today.", "2 min ago"),
        new Msg("/larva/assets/images/user/avatar-1.jpg", "6:00 PM", "<b>Aida Burg</b> commented your post.", "5 August"),
        new Msg("/larva/assets/images/user/avatar-3.jpg", "2:45 PM", "<b>There was a failure to your setup.</b>", "7 hours ago")
    ));

    // Breadcrumbs
    model.addAttribute("breadcrumbs", List.of(
        new Crumb("Home", "/dashboard"),
        new Crumb("Dashboard", null),
        new Crumb("Home", null) // last/current
    ));

    // KPIs
    model.addAttribute("kpis", List.of(
        new Kpi("Total Page Views","4,42,236","primary","ti ti-trending-up","59.3%","35,000"),
        new Kpi("Total Users","78,250","success","ti ti-trending-up","70.5%","8,900"),
        new Kpi("Total Order","18,800","warning","ti ti-trending-down","27.4%","1,943"),
        new Kpi("Total Sales","$35,078","danger","ti ti-trending-down","27.4%","$20,395")
    ));

    // Cards + tables
    model.addAttribute("income", new Money("$7,650"));
    model.addAttribute("sales",  new Money("$7,650"));

    model.addAttribute("orders", List.of(
        new Order("84564564","Camera Lens",40,"Rejected","$40,570"),
        new Order("84564564","Laptop",300,"Pending","$180,139"),
        new Order("84564564","Mobile",355,"Approved","$180,139")
    ));

    model.addAttribute("analytics", List.of(
        new Pair("Company Finance Growth","+45.14%"),
        new Pair("Company Expenses Ratio","0.58%"),
        new Pair("Business Risk Cases","Low")
    ));

    model.addAttribute("transactions", List.of(
        new Tx("ti ti-gift","success","Order #002434","Today, 2:00 AM","+ $1,430","78%"),
        new Tx("ti ti-message-circle","primary","Order #984947","5 August, 1:45 PM","- $302","8%"),
        new Tx("ti ti-settings","danger","Order #988784","7 hours ago","- $682","16%")
    ));

    // Footer
    model.addAttribute("footerHtml",
        "Mantis &#9829; crafted by Team <a href='https://themeforest.net/user/codedthemes' target='_blank'>Codedthemes</a> " +
        "Distributed by <a href='https://themewagon.com/'>ThemeWagon</a>.");

    // return Thymeleaf view in /templates/larva/dashboard.html
    return "larva/dashboard";
  }

  /* ===== Simple view models ===== */
  public static class Meta { public String description, keywords, author; public Meta(String d,String k,String a){description=d;keywords=k;author=a;} }
  public static class Layout { public String preset,direction,theme,boxed,rtl,font; public Layout(String p,String d,String t,String b,String r,String f){preset=p;direction=d;theme=t;boxed=b;rtl=r;font=f;} }
  public static class Nav { public String href, icon, text; public Nav(String h,String i,String t){href=h;icon=i;text=t;} }
  public static class Caption { public String label, icon; public Caption(String l,String i){label=l;icon=i;} }
  public static class Promo { public String title, subtitle, url, cta; public Promo(String t,String s,String u,String c){title=t;subtitle=s;url=u;cta=c;} }
  public static class User { public String name,title,avatar; public User(String n,String t,String a){name=n;title=t;avatar=a;} }
  public static class Msg { public String avatar,time,html,meta; public Msg(String a,String t,String h,String m){avatar=a;time=t;html=h;meta=m;} }
  public static class Crumb { public String label, href; public Crumb(String l,String h){label=l;href=h;} }
  public static class Kpi { public String label,value,trendColor,trendIcon,trendPct,extra; public Kpi(String l,String v,String c,String i,String p,String e){label=l;value=v;trendColor=c;trendIcon=i;trendPct=p;extra=e;} }
  public static class Money { public String amount; public Money(String a){amount=a;} }
  public static class Order { public String trackingNo,product,status,amount; public int qty; public Order(String t,String p,int q,String s,String a){trackingNo=t;product=p;qty=q;status=s;amount=a;} }
  public static class Pair { public String title,value; public Pair(String t,String v){title=t;value=v;} }
  public static class Tx { public String icon,color,title,time,amount,percent; public Tx(String i,String c,String t,String ti,String a,String p){icon=i;color=c;title=t;time=ti;amount=a;percent=p;} }
}
