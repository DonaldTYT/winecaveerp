<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!DOCTYPE html>
<html lang="en">
<head>
  <title><c:out value="${pageTitle}" /></title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimal-ui">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="description" content="<c:out value='${metaDescription}'/>">
  <meta name="keywords" content="<c:out value='${metaKeywords}'/>">
  <meta name="author" content="<c:out value='${metaAuthor}'/>">

  <link rel="icon" href="<c:url value='${favIcon}'/>" type="image/x-icon">

  <!-- Google Font -->
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Public+Sans:wght@300;400;500;600;700&display=swap" id="main-font-link">

  <!-- Fonts / Icons -->
  <link rel="stylesheet" href="<c:url value='${assetsBase}/fonts/tabler-icons.min.css'/>">
  <link rel="stylesheet" href="<c:url value='${assetsBase}/fonts/feather.css'/>">
  <link rel="stylesheet" href="<c:url value='${assetsBase}/fonts/fontawesome.css'/>">
  <link rel="stylesheet" href="<c:url value='${assetsBase}/fonts/material.css'/>">

  <!-- Template CSS -->
  <link rel="stylesheet" href="<c:url value='${assetsBase}/css/style.css'/>" id="main-style-link">
  <link rel="stylesheet" href="<c:url value='${assetsBase}/css/style-preset.css'/>">
</head>

<body data-pc-preset="${pcPreset}" data-pc-direction="${pcDir}" data-pc-theme="${pcTheme}">
  <!-- Pre-loader -->
  <div class="loader-bg">
    <div class="loader-track"><div class="loader-fill"></div></div>
  </div>

  <!-- Sidebar -->
  <nav class="pc-sidebar">
    <div class="navbar-wrapper">
      <div class="m-header">
        <a href="<c:url value='/miyu/kyo-koh/dashboard'/>" class="b-brand text-primary">
          <img src="<c:url value='${assetsBase}/images/logo-dark.svg'/>" class="img-fluid logo-lg" alt="logo">
		>
        </a>
      </div>

      <div class="navbar-content">
        <ul class="pc-navbar">
          <c:forEach var="m" items="${menu}">
            <c:choose>
              <c:when test="${m.type eq 'caption'}">
                <li class="pc-item pc-caption">
                  <label><c:out value="${m.label}"/></label>
                  <i class="${m.icon}"></i>
                </li>
              </c:when>

              <c:when test="${m.type eq 'link'}">
                <li class="pc-item">
                  <a href="<c:url value='${m.href}'/>" class="pc-link ${m.active ? 'active' : ''}">
                    <span class="pc-micon"><i class="${m.icon}"></i></span>
                    <span class="pc-mtext"><c:out value="${m.text}"/></span>
                  </a>
                </li>
              </c:when>

              <c:when test="${m.type eq 'submenu'}">
                <li class="pc-item pc-hasmenu">
                  <a href="#!" class="pc-link">
                    <span class="pc-micon"><i class="${m.icon}"></i></span>
                    <span class="pc-mtext"><c:out value="${m.text}"/></span>
                    <span class="pc-arrow"><i data-feather="chevron-right"></i></span>
                  </a>
                  <ul class="pc-submenu">
                    <c:forEach var="lv2" items="${m.children}">
                      <c:choose>
                        <c:when test="${not empty lv2.children}">
                          <li class="pc-item pc-hasmenu">
                            <a href="#!" class="pc-link"><c:out value="${lv2.text}"/><span class="pc-arrow"><i data-feather="chevron-right"></i></span></a>
                            <ul class="pc-submenu">
                              <c:forEach var="lv3" items="${lv2.children}">
                                <c:choose>
                                  <c:when test="${not empty lv3.children}">
                                    <li class="pc-item pc-hasmenu">
                                      <a href="#!" class="pc-link"><c:out value="${lv3.text}"/><span class="pc-arrow"><i data-feather="chevron-right"></i></span></a>
                                      <ul class="pc-submenu">
                                        <c:forEach var="lv4" items="${lv3.children}">
                                          <li class="pc-item"><a class="pc-link" href="#!"><c:out value="${lv4.text}"/></a></li>
                                        </c:forEach>
                                      </ul>
                                    </li>
                                  </c:when>
                                  <c:otherwise>
                                    <li class="pc-item"><a class="pc-link" href="#!"><c:out value="${lv3.text}"/></a></li>
                                  </c:otherwise>
                                </c:choose>
                              </c:forEach>
                            </ul>
                          </li>
                        </c:when>
                        <c:otherwise>
                          <li class="pc-item"><a class="pc-link" href="#!"><c:out value="${lv2.text}"/></a></li>
                        </c:otherwise>
                      </c:choose>
                    </c:forEach>
                  </ul>
                </li>
              </c:when>
            </c:choose>
          </c:forEach>
        </ul>

        <div class="card text-center">
          <div class="card-body">
            <img src="<c:url value='${assetsBase}/images/img-navbar-card.png'/>" alt="images" class="img-fluid mb-2">
            <h5>Upgrade To Pro</h5>
            <p>To get more features and components</p>
            <a href="https://codedthemes.com/item/berry-bootstrap-5-admin-template/" target="_blank" class="btn btn-success">Buy Now</a>
          </div>
        </div>
      </div>
    </div>
  </nav>

  <!-- Header -->
  <header class="pc-header">
    <div class="header-wrapper">
      <div class="me-auto pc-mob-drp">
        <ul class="list-unstyled">
          <li class="pc-h-item pc-sidebar-collapse">
            <a href="#" class="pc-head-link ms-0" id="sidebar-hide"><i class="ti ti-menu-2"></i></a>
          </li>
          <li class="pc-h-item pc-sidebar-popup">
            <a href="#" class="pc-head-link ms-0" id="mobile-collapse"><i class="ti ti-menu-2"></i></a>
          </li>
          <li class="dropdown pc-h-item d-inline-flex d-md-none">
            <a class="pc-head-link dropdown-toggle arrow-none m-0" data-bs-toggle="dropdown" href="#" role="button">
              <i class="ti ti-search"></i>
            </a>
            <div class="dropdown-menu pc-h-dropdown drp-search">
              <form class="px-3">
                <div class="form-group mb-0 d-flex align-items-center">
                  <i data-feather="search"></i>
                  <input type="search" class="form-control border-0 shadow-none" placeholder="Search here. . .">
                </div>
              </form>
            </div>
          </li>
          <li class="pc-h-item d-none d-md-inline-flex">
            <form class="header-search">
              <i data-feather="search" class="icon-search"></i>
              <input type="search" class="form-control" placeholder="Search here. . .">
            </form>
          </li>
        </ul>
      </div>

      <div class="ms-auto">
        <ul class="list-unstyled">
          <!-- mail dropdown (static demo content preserved) -->
          <li class="dropdown pc-h-item">
            <a class="pc-head-link dropdown-toggle arrow-none me-0" data-bs-toggle="dropdown" href="#" role="button"><i class="ti ti-mail"></i></a>
            <div class="dropdown-menu dropdown-notification dropdown-menu-end pc-h-dropdown">
              <div class="dropdown-header d-flex align-items-center justify-content-between">
                <h5 class="m-0">Message</h5>
                <a href="#!" class="pc-head-link bg-transparent"><i class="ti ti-x text-danger"></i></a>
              </div>
              <div class="dropdown-divider"></div>
              <div class="dropdown-header px-0 text-wrap header-notification-scroll position-relative" style="max-height: calc(100vh - 215px)">
                <div class="list-group list-group-flush w-100">
                  <a class="list-group-item list-group-item-action">
                    <div class="d-flex">
                      <div class="flex-shrink-0"><img src="<c:url value='${assetsBase}/images/user/avatar-2.jpg'/>" alt="user-image" class="user-avtar"></div>
                      <div class="flex-grow-1 ms-1"><span class="float-end text-muted">3:00 AM</span><p class="text-body mb-1">It's <b>Cristina danny's</b> birthday today.</p><span class="text-muted">2 min ago</span></div>
                    </div>
                  </a>
                </div>
              </div>
              <div class="dropdown-divider"></div>
              <div class="text-center py-2"><a href="#!" class="link-primary">View all</a></div>
            </div>
          </li>

          <!-- user dropdown -->
          <li class="dropdown pc-h-item header-user-profile">
            <a class="pc-head-link dropdown-toggle arrow-none me-0" data-bs-toggle="dropdown" href="#" role="button" aria-expanded="false">
              <img src="<c:url value='${user.avatar}'/>" alt="user-image" class="user-avtar">
              <span><c:out value="${user.name}"/></span>
            </a>
            <div class="dropdown-menu dropdown-user-profile dropdown-menu-end pc-h-dropdown">
              <div class="dropdown-header">
                <div class="d-flex mb-1">
                  <div class="flex-shrink-0"><img src="<c:url value='${user.avatar}'/>" alt="user-image" class="user-avtar wid-35"></div>
                  <div class="flex-grow-1 ms-3">
                    <h6 class="mb-1"><c:out value="${user.name}"/></h6>
                    <span><c:out value="${user.role}"/></span>
                  </div>
                  <a href="#!" class="pc-head-link bg-transparent"><i class="ti ti-power text-danger"></i></a>
                </div>
              </div>
              <ul class="nav drp-tabs nav-fill nav-tabs" role="tablist">
                <li class="nav-item" role="presentation"><button class="nav-link active" type="button"><i class="ti ti-user"></i> Profile</button></li>
                <li class="nav-item" role="presentation"><button class="nav-link" type="button"><i class="ti ti-settings"></i> Setting</button></li>
              </ul>
              <div class="tab-content p-2">
                <a href="#!" class="dropdown-item"><i class="ti ti-edit-circle"></i><span>Edit Profile</span></a>
                <a href="#!" class="dropdown-item"><i class="ti ti-user"></i><span>View Profile</span></a>
                <a href="#!" class="dropdown-item"><i class="ti ti-clipboard-list"></i><span>Social Profile</span></a>
                <a href="#!" class="dropdown-item"><i class="ti ti-wallet"></i><span>Billing</span></a>
                <a href="#!" class="dropdown-item"><i class="ti ti-power"></i><span>Logout</span></a>
              </div>
            </div>
          </li>
        </ul>
      </div>
    </div>
  </header>

  <!-- Main Content -->
  <div class="pc-container">
    <div class="pc-content">

      <!-- breadcrumb -->
      <div class="page-header">
        <div class="page-block">
          <div class="row align-items-center">
            <div class="col-md-12">
              <div class="page-header-title"><h5 class="m-b-10">Home</h5></div>
              <ul class="breadcrumb">
                <c:forEach var="bc" items="${breadcrumb}">
                  <li class="breadcrumb-item ${bc.current eq 'true' ? 'active' : ''}">
                    <c:choose>
                      <c:when test="${not empty bc.href}">
                        <a href="<c:url value='${bc.href}'/>"><c:out value="${bc.text}"/></a>
                      </c:when>
                      <c:otherwise><c:out value="${bc.text}"/></c:otherwise>
                    </c:choose>
                  </li>
                </c:forEach>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <!-- KPI cards -->
      <div class="row">
        <c:forEach var="k" items="${kpis}">
          <div class="col-md-6 col-xl-3">
            <div class="card">
              <div class="card-body">
                <h6 class="mb-2 f-w-400 text-muted"><c:out value="${k.title}"/></h6>
                <h4 class="mb-3">
                  <c:out value="${k.value}"/>
                  <span class="badge ${k.badgeClass}"><i class="${k.badgeIcon}"></i> <c:out value="${k.badgeText}"/></span>
                </h4>
                <p class="mb-0 text-muted text-sm">
                  <c:out value="" />
                  <c:out value="${k.noteHtml}" escapeXml="false"/>
                </p>
              </div>
            </div>
          </div>
        </c:forEach>

        <!-- Unique Visitor charts -->
        <div class="col-md-12 col-xl-8">
          <div class="d-flex align-items-center justify-content-between mb-3">
            <h5 class="mb-0">Unique Visitor</h5>
            <ul class="nav nav-pills justify-content-end mb-0">
              <li class="nav-item"><button class="nav-link" data-bs-toggle="pill" data-bs-target="#chart-tab-home" type="button">Month</button></li>
              <li class="nav-item"><button class="nav-link active" data-bs-toggle="pill" data-bs-target="#chart-tab-week" type="button">Week</button></li>
            </ul>
          </div>
          <div class="card">
            <div class="card-body">
              <div class="tab-content">
                <div class="tab-pane" id="chart-tab-home" role="tabpanel"><div id="visitor-chart-1"></div></div>
                <div class="tab-pane show active" id="chart-tab-week" role="tabpanel"><div id="visitor-chart"></div></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Income Overview -->
        <div class="col-md-12 col-xl-4">
          <h5 class="mb-3">Income Overview</h5>
          <div class="card">
            <div class="card-body">
              <h6 class="mb-2 f-w-400 text-muted">This Week Statistics</h6>
              <h3 class="mb-3">$7,650</h3>
              <div id="income-overview-chart"></div>
            </div>
          </div>
        </div>

        <!-- Recent Orders -->
        <div class="col-md-12 col-xl-8">
          <h5 class="mb-3">Recent Orders</h5>
          <div class="card tbl-card">
            <div class="card-body">
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
                    <c:forEach var="o" items="${orders}">
                      <tr>
                        <td><a href="#" class="text-muted"><c:out value="${o.no}"/></a></td>
                        <td><c:out value="${o.product}"/></td>
                        <td><c:out value="${o.total}"/></td>
                        <td>
                          <span class="d-flex align-items-center gap-2">
                            <i class="fas fa-circle f-10 m-r-5 ${o.statusClass}"></i><c:out value="${o.status}"/>
                          </span>
                        </td>
                        <td class="text-end"><c:out value="${o.amount}"/></td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>

        <!-- Analytics + Sales + Transactions -->
        <div class="col-md-12 col-xl-4">
          <h5 class="mb-3">Analytics Report</h5>
          <div class="card">
            <div class="list-group list-group-flush">
              <c:forEach var="a" items="${analytics}">
                <a href="#" class="list-group-item list-group-item-action d-flex align-items-center justify-content-between">
                  <c:out value="${a.title}"/><span class="h5 mb-0"><c:out value="${a.value}"/></span>
                </a>
              </c:forEach>
            </div>
            <div class="card-body px-2"><div id="analytics-report-chart"></div></div>
          </div>
        </div>

        <div class="col-md-12 col-xl-8">
          <h5 class="mb-3">Sales Report</h5>
          <div class="card">
            <div class="card-body">
              <h6 class="mb-2 f-w-400 text-muted">This Week Statistics</h6>
              <h3 class="mb-0">$7,650</h3>
              <div id="sales-report-chart"></div>
            </div>
          </div>
        </div>

        <div class="col-md-12 col-xl-4">
          <h5 class="mb-3">Transaction History</h5>
          <div class="card">
            <div class="list-group list-group-flush">
              <c:forEach var="t" items="${transactions}">
                <a href="#" class="list-group-item list-group-item-action">
                  <div class="d-flex">
                    <div class="flex-shrink-0">
                      <div class="avtar avtar-s rounded-circle ${t.iconClass}">
                        <i class="${t.icon} f-18"></i>
                      </div>
                    </div>
                    <div class="flex-grow-1 ms-3">
                      <h6 class="mb-1"><c:out value="${t.title}"/></h6>
                      <p class="mb-0 text-muted"><c:out value="${t.time}"/></p>
                    </div>
                    <div class="flex-shrink-0 text-end">
                      <h6 class="mb-1"><c:out value="${t.amount}"/></h6>
                      <p class="mb-0 text-muted"><c:out value="${t.percent}"/></p>
                    </div>
                  </div>
                </a>
              </c:forEach>
            </div>
          </div>
        </div>

      </div>
      <!-- /row -->
    </div>
  </div>

  <!-- Footer -->
  <footer class="pc-footer">
    <div class="footer-wrapper container-fluid">
      <div class="row">
        <div class="col-sm my-1">
          <p class="m-0"><c:out value="${footerNote}"/>.</p>
        </div>
        <div class="col-auto my-1">
          <ul class="list-inline footer-link mb-0">
            <li class="list-inline-item"><a href="<c:url value='${footerHomeHref}'/>">Home</a></li>
          </ul>
        </div>
      </div>
    </div>
  </footer>

  <!-- Page JS -->
  <script src="<c:url value='${assetsBase}/js/plugins/apexcharts.min.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/pages/dashboard-default.js'/>"></script>

  <!-- Core JS -->
  <script src="<c:url value='${assetsBase}/js/plugins/popper.min.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/plugins/simplebar.min.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/plugins/bootstrap.min.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/fonts/custom-font.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/pcoded.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/plugins/feather.min.js'/>"></script>

  <!-- Presets (backend-controlled) -->
  <script>layout_change('<c:out value="${layoutLight}"/>');</script>
  <script>change_box_container('<c:out value="${boxContainer}"/>');</script>
  <script>layout_rtl_change('<c:out value="${rtl}"/>');</script>
  <script>preset_change("<c:out value='${preset}'/>");</script>
  <script>font_change("<c:out value='${fontFamily}'/>");</script>
</body>
</html>
