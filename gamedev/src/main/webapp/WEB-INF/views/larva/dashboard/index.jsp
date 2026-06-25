<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="app" tagdir="/WEB-INF/tags" %>

<c:set var="larvaAssets" value="${larvaAssets != null ? larvaAssets : '/larva/assets'}"/>

<!DOCTYPE html>
<html lang="en">
<head>
  <title><c:out value="${pageTitle != null ? pageTitle : 'Home'}"/> | Mantis Bootstrap 5 Admin Template</title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimal-ui">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="description" content="Mantis is made using Bootstrap 5 design framework. Download the free admin template & use it for your project.">
  <meta name="keywords" content="Mantis, Dashboard UI Kit, Bootstrap 5, Admin Template, Admin Dashboard, CRM, CMS, Bootstrap Admin Template">
  <meta name="author" content="CodedThemes">

  <link rel="icon" href="<c:url value='${larvaAssets}/images/favicon.svg'/>" type="image/x-icon">
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Public+Sans:wght@300;400;500;600;700&display=swap" id="main-font-link">
  <link rel="stylesheet" href="<c:url value='${larvaAssets}/fonts/tabler-icons.min.css'/>">
  <link rel="stylesheet" href="<c:url value='${larvaAssets}/fonts/feather.css'/>">
  <link rel="stylesheet" href="<c:url value='${larvaAssets}/fonts/fontawesome.css'/>">
  <link rel="stylesheet" href="<c:url value='${larvaAssets}/fonts/material.css'/>">
  <link rel="stylesheet" href="<c:url value='${larvaAssets}/css/style.css'/>" id="main-style-link">
  <link rel="stylesheet" href="<c:url value='${larvaAssets}/css/style-preset.css'/>">
</head>
<style>
    /* Full-bleed content on small screens: remove horizontal padding + gutters */
  @media (max-width: 768px) {
    /* kill container paddings */
    .pc-container,
    .pc-content,
    .page-header,
    .page-block {
      padding-left: 0 !important;
      padding-right: 0 !important;
      margin-left: 0 !important;
      margin-right: 0 !important;
    }

    /* if any Bootstrap containers sit inside, strip their padding too */
    .pc-content .container,
    .pc-content .container-fluid,
    .page-block .container,
    .page-block .container-fluid {
      padding-left: 0 !important;
      padding-right: 0 !important;
      margin-left: 0 !important;
      margin-right: 0 !important;
    }

    /* remove Bootstrap row gutters inside the content area */
    .pc-content .row,
    .page-block .row {
      --bs-gutter-x: 0 !important;
      margin-left: 0 !important;
      margin-right: 0 !important;
    }
  }
    /* kill the 20px top padding on the main content area */
  .pc-container .pc-content { padding-top: 0 !important; }

  /* if the page header still creates space, remove that too */
  .pc-content .page-header,
  .pc-content .page-block { padding-top: 0 !important; margin-top: 0 !important; }

 /* Make the wrapper fill remaining viewport height.
   Tweak the -Xpx to match your header/breadcrumb/spacing total. */
.iframe-wrap {
  margin: 0;
  padding: 0;
  height: calc(100dvh - 60px);   /* try 160–220px depending on your layout */
}

/* Let the iframe fill the wrapper and avoid extra borders/margins */
.iframe-wrap iframe {
  display: block;
  width: 100%;
  height: 100%;
  border: 0;
}

body.iframe-mode .pc-container .pc-content,
body.iframe-mode .pc-container .pc-content > div {
  padding-left: 0 !important;
  padding-right: 0 !important;
  margin-left: 0 !important;
  margin-right: 0 !important;
}

body.iframe-mode .iframe-wrap {
  width: 100%;
}
  :root { --sidebar-logo-max-h: 48px; } /* tweak this value to taste */

  .pc-sidebar .m-header { 
    height: calc(var(--sidebar-logo-max-h) + 16px); /* room for padding */
    display: flex; 
    align-items: center;
    padding: 8px 16px;
  }

  .pc-sidebar .m-header .b-brand { line-height: 0; } /* kill inline-gap */
  .pc-sidebar .m-header .b-brand img,
  .pc-sidebar .m-header .logo-lg {
    max-height: var(--sidebar-logo-max-h);
    width: auto;          /* preserve aspect ratio */
    height: auto;         /* preserve aspect ratio */
    object-fit: contain;  /* safety for odd image boxes */
    display: block;       /* avoid baseline gap */
  }

  :root { --header-logo-max-h: 32px; }
  .pc-header .b-brand img {
    max-height: var(--header-logo-max-h);
    width: auto; height: auto; object-fit: contain; display: block;
  }
</style>
<c:if test="${!showSidebar}">
<style>
  /* ===== No-sidebar overrides ===== */
  body.no-sidebar { --pc-sidebar-width: 0px; }           /* if theme uses this var */
  body.no-sidebar .pc-sidebar { display: none !important; }

  /* Reclaim all left offset the theme adds */
  body.no-sidebar .pc-header,
  body.no-sidebar .pc-container,
  body.no-sidebar .pc-content,
  body.no-sidebar .pc-footer {
    margin-left: 0 !important;
    padding-left: 0 !important;
    left: 0 !important;
  }
  body.no-sidebar .pc-header .header-wrapper {
    margin-left: 0 !important;
    padding-left: 0 !important;
    left: 0 !important;
  }

  /* Hide any sidebar toggles */
  body.no-sidebar .pc-sidebar-collapse,
  body.no-sidebar .pc-sidebar-popup {
    display: none !important;
  }

  /* Make the left header group (search) hug the left edge */
  body.no-sidebar .pc-header .pc-mob-drp,
  body.no-sidebar .pc-header .pc-mob-drp ul {
    margin: 0 !important;
    padding: 0 !important;
  }
  body.no-sidebar .pc-header .pc-mob-drp .pc-h-item {
    margin-left: 0 !important;
    padding-left: 0 !important;
  }
  body.no-sidebar .pc-header .header-search { margin-left: 0 !important; }
  body.no-sidebar .pc-header .me-auto { margin-left: 0 !important; } /* neutralize BS helper */

  /* In case a container adds padding on small screens */
  @media (max-width: 992px) {
    body.no-sidebar .pc-header,
    body.no-sidebar .pc-container,
    body.no-sidebar .pc-content,
    body.no-sidebar .pc-footer {
      padding-left: 0 !important;
    }
  }
  
</style>
</c:if>
<body data-pc-preset="preset-1"
      data-pc-direction="ltr"
      data-pc-theme="light"
      class="${!showSidebar ? 'no-sidebar' : ''} ${showIframe ? 'iframe-mode' : ''}">

<div class="loader-bg"><div class="loader-track"><div class="loader-fill"></div></div></div>
<c:if test="${showSidebar}">
<nav class="pc-sidebar">
  <div class="navbar-wrapper">
    <div class="m-header">
      <a href="<c:url value='/larva/dashboard'/>" class="b-brand text-primary">
        <img src="<c:url value='${larvaAssets}/images/logo-dark.jpg'/>" class="img-fluid logo-lg" alt="logo">
      </a>
    </div>
    <div class="navbar-content">
		<app:renderMenu nodes="${sideMenu}" root="true"/>
<%--
      <div class="card text-center">
        <div class="card-body">
          <img src="<c:url value='${larvaAssets}/images/img-navbar-card.png'/>" alt="images" class="img-fluid mb-2">
          <h5>Upgrade To Pro</h5>
          <p>To get more features and components</p>
          <a href="https://codedthemes.com/item/berry-bootstrap-5-admin-template/" target="_blank" class="btn btn-success">Buy Now</a>
        </div>
      </div>
 --%>
    </div>
  </div>
</nav>
</c:if>

<c:if test="${showHeader}">
<header class="pc-header">
  <div class="header-wrapper">
  <div class="header-wrapper">
  <c:choose>
    <%-- WITH SIDEBAR --%>
    <c:when test="${showSidebar}">
      <div class="me-auto pc-mob-drp">
        <ul class="list-unstyled">
          <li class="pc-h-item pc-sidebar-collapse">
            <a href="#" class="pc-head-link ms-0" id="sidebar-hide"><i class="ti ti-menu-2"></i></a>
          </li>
          <li class="pc-h-item pc-sidebar-popup">
            <a href="#" class="pc-head-link ms-0" id="mobile-collapse"><i class="ti ti-menu-2"></i></a>
          </li>
          <%--
          <li class="pc-h-item d-none d-md-inline-flex">
            <form class="header-search">
              <i data-feather="search" class="icon-search"></i>
              <input type="search" class="form-control" placeholder="Search here. . .">
            </form>
          </li>
           --%>
        </ul>
      </div>
    </c:when>

    <%-- NO SIDEBAR --%>
    <c:otherwise>
      <div class="pc-mob-drp">
        <ul class="list-unstyled m-0 p-0">
          <li class="pc-h-item d-none d-md-inline-flex ms-0">
            <form class="header-search ms-0">
              <i data-feather="search" class="icon-search"></i>
              <input type="search" class="form-control" placeholder="Search here. . .">
            </form>
          </li>
        </ul>
      </div>
    </c:otherwise>
  </c:choose>
  
    <div class="ms-auto">
      <ul class="list-unstyled">
        <li class="dropdown pc-h-item"><a class="pc-head-link dropdown-toggle arrow-none me-0" data-bs-toggle="dropdown" href="#"><i class="ti ti-mail"></i></a></li>
        <li class="dropdown pc-h-item header-user-profile">
          <a class="pc-head-link dropdown-toggle arrow-none me-0" data-bs-toggle="dropdown" href="#" data-bs-auto-close="outside">
		<%--
            <img src="<c:url value='${larvaAssets}/images/user/avatar-2.jpg'/>" alt="user-image" class="user-avtar"><span>Stebin Ben</span>
		--%>
		<img src="<c:url value='${empty profile ? "/larva/assets/images/user/avatar-2.jpg" : profile.avatarPath}'/>" alt="user-image" class="user-avtar" />
		<span><c:out value='${empty profile ? "Guest" : profile.displayName}'/></span>
          </a>
          <div class="dropdown-menu dropdown-user-profile dropdown-menu-end pc-h-dropdown">
            <div class="dropdown-header">
              <div class="d-flex mb-1">
                <div class="flex-shrink-0"><img src="<c:url value='${larvaAssets}/images/user/avatar-2.jpg'/>" alt="user" class="user-avtar wid-35"></div>
                <%--
                <div class="flex-grow-1 ms-3"><h6 class="mb-1">Stebin Ben</h6><span>UI/UX Designer</span></div>
                 --%>
                <div class="flex-grow-1 ms-3">
                	<h6 class="mb-1" ><c:out value='${empty profile ? "Guest" : profile.displayName}'/></h6>
                	<%--
                	<h6 class="mb-1">Stebin Ben</h6>
                	<span>UI/UX Designer</span>
                	 --%>
                </div>
                <%--
                <a href="#!" class="pc-head-link bg-transparent"><i class="ti ti-power text-danger"></i></a>
                 --%>
                <a href="<c:url value="${logoutPath}"/>" class="pc-head-link bg-transparent"><i class="ti ti-power text-danger"></i></a>
              </div>
            </div>
          </div>
        </li>
      </ul>
    </div>
  </div>
</header>
</c:if>

<div class="pc-container">
  <div class="pc-content">
	<c:if test="${showPageHeader}">
    <div class="page-header">
      <div class="page-block">
        <div class="row align-items-center">
          <div class="col-md-12">
            <div class="page-header-title"><h5 class="m-b-10">Home</h5></div>
            <ul class="breadcrumb">
              <li class="breadcrumb-item"><a href="<c:url value='/larva/dashboard'/>">Home</a></li>
              <li class="breadcrumb-item"><a href="javascript:void(0)">Dashboard</a></li>
              <li class="breadcrumb-item" aria-current="page">Home</li>
            </ul>
          </div>
        </div>
      </div>
    </div>
	</c:if>
    <div>
    <%--
                <div class="flex-shrink-0"><img src="<c:url value='${larvaAssets}/images/user/avatar-2.jpg'/>" alt="user" class="user-avtar wid-35"></div>
           	<iframe src="http://192.168.1.204:8080/pmsdemo/vincero_compound_result.html?action=run" height="800px" width="100%" title="Iframe Example"></iframe> 
			<iframe src="<c:url value='${iFrameUrl}'/>" height="800" width="100%" title="Iframe Example"></iframe>
     --%>
	<c:if test="${showIframe}">
			<div class="iframe-wrap">
			<iframe src="<c:url value='${iFrameUrl}'/>" title="Iframe Example"></iframe>
			</div>
	</c:if>
	<c:if test="${showListView}">
	<div>HAHA_showListView</div>
	<div class="card">
  	<div class="card-header"><h5 class="mb-0">Records</h5></div>
  	<div class="card-body">
    <div class="mx-auto" style="max-width: 900px;">
      <jsp:include page="/WEB-INF/views/larva/fragments/record-list.jspf">
        <jsp:param name="ids" value="rl1"/>
        <jsp:param name="dataUrl" value="${pageContext.request.contextPath}/larva/pages/records"/>
      </jsp:include>
    </div>
  	</div>
	</div>
	<jsp:include page="/WEB-INF/views/larva/fragments/record-list.js.jspf">
  	<jsp:param name="ids" value="rl1"/>
	</jsp:include>
	</c:if>
<%--
	<div class="card">
  	<div class="card-header"><h5 class="mb-0">Records</h5></div>
  	<div class="card-body">
  	<div>HAHA_before</div>
    <div class="mx-auto" style="max-width: 900px;">
      <jsp:include page="/WEB-INF/views/larva/fragments/record-list.jspf">
        <jsp:param name="ids" value="rl1"/>
        <jsp:param name="dataUrl" value="${pageContext.request.contextPath}/larva/pages/records"/>
      </jsp:include>
    </div>
  	<div>HAHA_after</div>
  	</div>
	</div>
	<jsp:include page="/WEB-INF/views/larva/fragments/record-list.js.jspf">
  	<jsp:param name="ids" value="rl1"/>
	</jsp:include>
 --%>
	<c:if test="${showSetupScreen}">
<div class="row">
  <div class="col-12">
      <!-- add the custom border class -->
    <div class="card form-card shadow-0">
      <div class="card-body">
        <!-- move & center the title -->
        <h3 class="mb-4 text-center fw-bold">Sync Category</h3>

        <!-- center the whole form area and keep it narrow -->
        <div class="mx-auto" style="max-width: 700px;">
          <jsp:include page="/WEB-INF/views/larva/fragments/saleor-sync-form.jspf">
            <jsp:param name="ids" value="scDash"/>
            <jsp:param name="actionUrl" value="${pageContext.request.contextPath}/larva/pages/saleor-sync"/>
          </jsp:include>
        </div>
      </div>
    </div>

    <!-- keep JS after the card -->
    <jsp:include page="/WEB-INF/views/larva/fragments/saleor-sync-form.js.jspf">
      <jsp:param name="ids" value="scDash"/>
      <jsp:param name="statusUrl" value="${pageContext.request.contextPath}/larva/pages/saleor-sync-status"/>
    </jsp:include>
   
  </div>
</div>
	</c:if>
    
<c:if test="${showCalendar}">
<div class="row">
  <div class="col-12">
    <div class="card">
      <div class="card-body position-relative">
        <div id="calendar" class="calendar"></div>
      </div>
    </div>
  </div>
</div>

<div class="modal fade" id="calendar-modal" data-bs-keyboard="false" tabindex="-1" aria-hidden="true">
  <div class="modal-dialog modal-dialog-centered modal-dialog-scrollable">
    <div class="modal-content">
      <div class="modal-header">
        <h3 class="calendar-modal-title f-w-600 text-truncate">Modal title</h3>
        <a href="#" class="avtar avtar-s btn-link-danger btn-pc-default" data-bs-dismiss="modal">
          <i class="ti ti-x f-20"></i>
        </a>
      </div>
      <div class="modal-body">
        <div class="d-flex">
          <div class="flex-shrink-0">
            <div class="avtar avtar-xs bg-light-secondary">
              <i class="ti ti-heading f-20"></i>
            </div>
          </div>
          <div class="flex-grow-1 ms-3">
            <h5 class="mb-1"><b>Title</b></h5>
            <p class="pc-event-title text-muted"></p>
          </div>
        </div>
        <div class="d-flex">
          <div class="flex-shrink-0">
            <div class="avtar avtar-xs bg-light-warning">
              <i class="ti ti-map-pin f-20"></i>
            </div>
          </div>
          <div class="flex-grow-1 ms-3">
            <h5 class="mb-1"><b>Venue</b></h5>
            <p class="pc-event-venue text-muted"></p>
          </div>
        </div>
        <div class="d-flex">
          <div class="flex-shrink-0">
            <div class="avtar avtar-xs bg-light-danger">
              <i class="ti ti-calendar-event f-20"></i>
            </div>
          </div>
          <div class="flex-grow-1 ms-3">
            <h5 class="mb-1"><b>Date</b></h5>
            <p class="pc-event-date text-muted"></p>
          </div>
        </div>
        <div class="d-flex">
          <div class="flex-shrink-0">
            <div class="avtar avtar-xs bg-light-primary">
              <i class="ti ti-file-text f-20"></i>
            </div>
          </div>
          <div class="flex-grow-1 ms-3">
            <h5 class="mb-1"><b>Description</b></h5>
            <p class="pc-event-description text-muted"></p>
          </div>
        </div>
      </div>
      <div class="modal-footer justify-content-between">
        <ul class="list-inline me-auto mb-0">
          <li class="list-inline-item align-bottom">
            <a href="#" id="pc_event_remove" class="avtar avtar-s btn-link-danger btn-pc-default w-sm-auto" data-bs-toggle="tooltip" title="Delete">
              <i class="ti ti-trash f-18"></i>
            </a>
          </li>
          <li class="list-inline-item align-bottom">
            <a href="#" id="pc_event_edit" class="avtar avtar-s btn-link-success btn-pc-default" data-bs-toggle="tooltip" title="Edit">
              <i class="ti ti-edit-circle f-18"></i>
            </a>
          </li>
        </ul>
        <div class="flex-grow-1 text-end">
          <button type="button" class="btn btn-primary" data-bs-dismiss="modal">Close</button>
        </div>
      </div>
    </div>
  </div>
</div>
</c:if>

<div class="offcanvas offcanvas-end cal-event-offcanvas" tabindex="-1" id="calendar-add_edit_event">
  <div class="offcanvas-header">
    <h3 class="f-w-600 text-truncate">Add Events</h3>
    <a href="#" class="avtar avtar-s btn-link-danger btn-pc-default" data-bs-dismiss="offcanvas">
      <i class="ti ti-x f-20"></i>
    </a>
  </div>
  <div class="offcanvas-body">
    <form id="pc-form-event" novalidate>
      <div class="form-group">
        <label class="form-label">Title</label>
        <input type="email" class="form-control" id="pc-e-title" placeholder="Enter event title" autofocus>
      </div>
      <div class="form-group">
        <label class="form-label">Venue</label>
        <input type="email" class="form-control" id="pc-e-venue" placeholder="Enter event venue">
      </div>
      <div class="form-group m-0">
        <input type="hidden" class="form-control" id="pc-e-sdate">
        <input type="hidden" class="form-control" id="pc-e-edate">
      </div>
      <div class="form-group">
        <label class="form-label">Description</label>
        <textarea class="form-control" placeholder="Enter event description" rows="3" id="pc-e-description"></textarea>
      </div>
      <div class="form-group">
        <label class="form-label">Type</label>
        <select class="form-select" id="pc-e-type">
          <option value="empty" selected>Type</option>
          <option value="event-primary">Primary</option>
          <option value="event-secondary">Secondary</option>
          <option value="event-success">Success</option>
          <option value="event-danger">Danger</option>
          <option value="event-warning">Warning</option>
          <option value="event-info">Info</option>
        </select>
      </div>
      <div class="row justify-content-between">
        <div class="col-auto">
          <button type="button" class="btn btn-link-danger btn-pc-default" data-bs-dismiss="offcanvas">
            <i class="align-text-bottom me-1 ti ti-circle-x"></i> Close
          </button>
        </div>
        <div class="col-auto">
          <button id="pc_event_add" type="button" class="btn btn-secondary" data-pc-action="add">
            <span id="pc-e-btn-text"><i class="align-text-bottom me-1 ti ti-calendar-plus"></i> Add</span>
          </button>
        </div>
      </div>
    </form>
  </div>
</div>
    
    
    
    </div> 
    

    <c:if test="${showDashboard}">
    <div class="row">

      <c:if test="${showStatsTiles}">
        <div class="col-md-6 col-xl-3">
          <div class="card"><div class="card-body">
            <h6 class="mb-2 f-w-400 text-muted">Total Bottles in Storage</h6>
            <h4 class="mb-3">
              <fmt:formatNumber value="${metrics.totalPageViews}" type="number" groupingUsed="true"/>
              <c:set var="pvPos" value="${metrics.pageViewsChangePct >= 0}"/>
              <span class="badge ${pvPos ? 'bg-light-primary border border-primary' : 'bg-light-danger border border-danger'}">
                <i class="ti ${pvPos ? 'ti-trending-up' : 'ti-trending-down'}"></i>
                <fmt:formatNumber value="${metrics.pageViewsChangePct}" pattern="#0.0"/>%
              </span>
            </h4>
            <p class="mb-0 text-muted text-sm">You made an extra <span class="text-primary">35,000</span> this year</p>
          </div></div>
        </div>

        <div class="col-md-6 col-xl-3">
          <div class="card"><div class="card-body">
            <h6 class="mb-2 f-w-400 text-muted">Total Bottles in Consignment</h6>
            <h4 class="mb-3">
              <fmt:formatNumber value="${metrics.totalUsers}" type="number" groupingUsed="true"/>
              <span class="badge bg-light-success border border-success">
                <i class="ti ti-trending-up"></i>
                <fmt:formatNumber value="${metrics.usersChangePct}" pattern="#0.0"/>%
              </span>
            </h4>
            <p class="mb-0 text-muted text-sm">You made an extra <span class="text-success">8,900</span> this year</p>
          </div></div>
        </div>

<%--
        <div class="col-md-6 col-xl-3">
          <div class="card"><div class="card-body">
            <h6 class="mb-2 f-w-400 text-muted">Total Order</h6>
            <h4 class="mb-3">
              <fmt:formatNumber value="${metrics.totalOrders}" type="number" groupingUsed="true"/>
              <span class="badge ${metrics.ordersChangePct >= 0 ? 'bg-light-success border border-success' : 'bg-light-warning border border-warning'}">
                <i class="ti ${metrics.ordersChangePct >= 0 ? 'ti-trending-up' : 'ti-trending-down'}"></i>
                <fmt:formatNumber value="${metrics.ordersChangePct}" pattern="#0.0"/>%
              </span>
            </h4>
            <p class="mb-0 text-muted text-sm">You made an extra <span class="text-warning">1,943</span> this year</p>
          </div></div>
        </div>

        <div class="col-md-6 col-xl-3">
          <div class="card"><div class="card-body">
            <h6 class="mb-2 f-w-400 text-muted">Total Sales</h6>
            <h4 class="mb-3">
              $<fmt:formatNumber value="${metrics.totalSales}" type="number" groupingUsed="true"/>
              <span class="badge ${metrics.salesChangePct >= 0 ? 'bg-light-success border border-success' : 'bg-light-danger border border-danger'}">
                <i class="ti ${metrics.salesChangePct >= 0 ? 'ti-trending-up' : 'ti-trending-down'}"></i>
                <fmt:formatNumber value="${metrics.salesChangePct}" pattern="#0.0"/>%
              </span>
            </h4>
            <p class="mb-0 text-muted text-sm">You made an extra <span class="text-danger">$20,395</span> this year</p>
          </div></div>
        </div>
 --%>
      </c:if>
      <c:if test="${showUniqueVisitor}">
        <div class="col-md-12 col-xl-8">
          <div class="d-flex align-items-center justify-content-between mb-3">
            <h5 class="mb-0">Unique Visitor</h5>
            <ul class="nav nav-pills justify-content-end mb-0" id="chart-tab-tab" role="tablist">
              <li class="nav-item"><button class="nav-link" id="chart-tab-home-tab" data-bs-toggle="pill" data-bs-target="#chart-tab-home" type="button">Month</button></li>
              <li class="nav-item"><button class="nav-link active" id="chart-tab-profile-tab" data-bs-toggle="pill" data-bs-target="#chart-tab-profile" type="button">Week</button></li>
            </ul>
          </div>
          <div class="card"><div class="card-body">
            <div class="tab-content" id="chart-tab-tabContent">
              <div class="tab-pane" id="chart-tab-home" role="tabpanel" tabindex="0">
                <div id="visitor-chart-1" data-series='${visitorChartMonthJson}'></div>
              </div>
              <div class="tab-pane show active" id="chart-tab-profile" role="tabpanel" tabindex="0">
                <div id="visitor-chart" data-series='${visitorChartWeekJson}'></div>
              </div>
            </div>
          </div></div>
        </div>
      </c:if>

      <c:if test="${showIncomeOverview}">
        <div class="col-md-12 col-xl-4">
          <h5 class="mb-3">Income Overview</h5>
          <div class="card"><div class="card-body">
            <h6 class="mb-2 f-w-400 text-muted">This Week Statistics</h6>
            <h3 class="mb-3">$<fmt:formatNumber value="${metrics.incomeThisWeek}" type="number" groupingUsed="true"/></h3>
            <div id="income-overview-chart" data-series='${incomeOverviewJson}'></div>
          </div></div>
        </div>
      </c:if>

      <c:if test="${showRecentOrders}">
        <div class="col-md-12 col-xl-8">
          <h5 class="mb-3">Recent Orders</h5>
          <div class="card tbl-card"><div class="card-body">
            <div class="table-responsive">
              <table class="table table-hover table-borderless mb-0">
                <thead>
                  <tr>
                    <th>TRACKING NO.</th>
                    <th>PRODUCT NAME</th>
                    <th>TOTAL ORDER</th>
                    <th>STATUS</th>
                    <th class="text-end">TOTAL AMOUNT</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="r" items="${recentOrders}">
                    <tr>
                      <td><a href="#" class="text-muted"><c:out value="${r.trackingNo}"/></a></td>
                      <td><c:out value="${r.productName}"/></td>
                      <td><c:out value="${r.totalOrder}"/></td>
                      <td>
                        <span class="d-flex align-items-center gap-2">
                          <c:choose>
                            <c:when test="${r.status eq 'Rejected'}"><i class="fas fa-circle text-danger f-10 m-r-5"></i>Rejected</c:when>
                            <c:when test="${r.status eq 'Pending'}"><i class="fas fa-circle text-warning f-10 m-r-5"></i>Pending</c:when>
                            <c:otherwise><i class="fas fa-circle text-success f-10 m-r-5"></i>Approved</c:otherwise>
                          </c:choose>
                        </span>
                      </td>
                      <td class="text-end">$<fmt:formatNumber value="${r.totalAmount}" type="number" groupingUsed="true"/></td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </div></div>
        </div>
      </c:if>

      <c:if test="${showAnalyticsReport}">
        <div class="col-md-12 col-xl-4">
          <h5 class="mb-3">Analytics Report</h5>
          <div class="card">
            <div class="list-group list-group-flush">
              <c:forEach var="row" items="${analyticsReport}">
                <a href="#" class="list-group-item list-group-item-action d-flex align-items-center justify-content-between">
                  <c:out value='${row[0]}'/><span class="h5 mb-0"><c:out value='${row[1]}'/></span>
                </a>
              </c:forEach>
            </div>
            <div class="card-body px-2">
              <div id="analytics-report-chart" data-series='${analyticsReportJson}'></div>
            </div>
          </div>
        </div>
      </c:if>

      <c:if test="${showSalesReport}">
        <div class="col-md-12 col-xl-8">
          <h5 class="mb-3">Sales Report</h5>
          <div class="card"><div class="card-body">
            <h6 class="mb-2 f-w-400 text-muted">This Week Statistics</h6>
            <h3 class="mb-0">$<fmt:formatNumber value="${metrics.salesThisWeek}" type="number" groupingUsed="true"/></h3>
            <div id="sales-report-chart" data-series='${salesReportJson}'></div>
          </div></div>
        </div>
      </c:if>

      <c:if test="${showTransactionHistory}">
        <div class="col-md-12 col-xl-4">
          <h5 class="mb-3">Transaction History</h5>
          <div class="card">
            <div class="list-group list-group-flush">
              <c:forEach var="t" items="${transactions}">
                <a href="#" class="list-group-item list-group-item-action">
                  <div class="d-flex">
                    <div class="flex-shrink-0">
                      <div class="avtar avtar-s rounded-circle text-${t.tone} bg-light-${t.tone}">
                        <i class="${t.icon} f-18"></i>
                      </div>
                    </div>
                    <div class="flex-grow-1 ms-3">
                      <h6 class="mb-1">Order <c:out value="${t.orderNo}"/></h6>
                      <p class="mb-0 text-muted"><c:out value="${t.when}"/></p>
                    </div>
                    <div class="flex-shrink-0 text-end">
                      <h6 class="mb-1"><c:out value="${t.amount.signum() >= 0 ? '+ $' : '- $'}"/><fmt:formatNumber value="${t.amount.abs()}" type="number" groupingUsed="true"/></h6>
                      <p class="mb-0 text-muted"><c:out value="${t.ratio}"/></p>
                    </div>
                  </div>
                </a>
              </c:forEach>
            </div>
          </div>
        </div>
      </c:if>
    </div>
  </div>
  </c:if>
</div>

<c:if test="${showFooter}">
<footer class="pc-footer">
  <div class="footer-wrapper container-fluid">
    <div class="row">
      <div class="col-sm my-1">
        <p class="m-0">Mantis &#9829; crafted by Team <a href="https://themeforest.net/user/codedthemes" target="_blank">Codedthemes</a> Distributed by <a href="https://themewagon.com/">ThemeWagon</a>.</p>
      </div>
      <div class="col-auto my-1">
        <ul class="list-inline footer-link mb-0">
          <li class="list-inline-item"><a href="<c:url value='/larva'/>">Home</a></li>
        </ul>
      </div>
    </div>
  </div>
</footer>
</c:if>

<script src="<c:url value='${larvaAssets}/js/plugins/apexcharts.min.js'/>"></script>
<%--
<script src="<c:url value='${larvaAssets}/js/pages/dashboard-default.js'/>"></script>
 --%>
<script src="<c:url value='${larvaAssets}/js/plugins/popper.min.js'/>"></script>
<script src="<c:url value='${larvaAssets}/js/plugins/simplebar.min.js'/>"></script>
<script src="<c:url value='${larvaAssets}/js/plugins/bootstrap.min.js'/>"></script>
<script src="<c:url value='${larvaAssets}/js/fonts/custom-font.js'/>"></script>
<script src="<c:url value='${larvaAssets}/js/pcoded.js'/>"></script>
<script src="<c:url value='${larvaAssets}/js/plugins/feather.min.js'/>"></script>

<script>
  (function () {
    function init(id) {
      var el = document.getElementById(id);
      if (!el || !window.ApexCharts) return;
      var data = [];
      try { data = JSON.parse(el.getAttribute('data-series') || '[]'); } catch(e) {}
      var opt = { chart:{type:'line', height:240}, series:[{data:data}], xaxis:{labels:{show:false}}, yaxis:{labels:{show:true}} };
      new ApexCharts(el, opt).render();
    }
    init('visitor-chart');
    init('visitor-chart-1');
    init('income-overview-chart');
    init('sales-report-chart');
    init('analytics-report-chart');
  })();
</script>
<style>
  /* match the standalone page’s frame */
  .form-card { border: 2px solid #000; border-radius: .5rem; }
</style>
<%--
<script>layout_change && layout_change('light');</script>
<script>change_box_container && change_box_container('false');</script>
<script>layout_rtl_change && layout_rtl_change('false');</script>
<script>preset_change && preset_change("preset-1");</script>
<script>font_change && font_change("Public-Sans");</script>
 --%>
 
<script src="<c:url value='${larvaAssets}/js/plugins/index.global.min.js'/>"></script>

<script src="<c:url value='${larvaAssets}/js/plugins/sweetalert2.all.min.js'/>"></script>

<script src="<c:url value='${larvaAssets}/js/pages/calendar.js'/>"></script>

</body>
</html>
