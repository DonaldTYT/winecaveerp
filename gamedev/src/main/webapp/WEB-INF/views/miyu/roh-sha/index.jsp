<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <meta name="author" content="<c:out value='${metaAuthor}'/>">
  <link rel="shortcut icon" href="<c:url value='${favicon}'/>">
  <meta name="description" content="<c:out value='${metaDescription}'/>" />
  <meta name="keywords" content="<c:out value='${metaKeywords}'/>" />
  <title><c:out value="${pageTitle}"/></title>

  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Work+Sans:wght@400;600;700&display=swap" rel="stylesheet">

  <link rel="stylesheet" href="<c:url value='${assetsBase}/fonts/icomoon/style.css'/>">
  <link rel="stylesheet" href="<c:url value='${assetsBase}/fonts/flaticon/font/flaticon.css'/>">

  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.8.1/font/bootstrap-icons.css">

  <link rel="stylesheet" href="<c:url value='${assetsBase}/css/tiny-slider.css'/>">
  <link rel="stylesheet" href="<c:url value='${assetsBase}/css/aos.css'/>">
  <link rel="stylesheet" href="<c:url value='${assetsBase}/css/glightbox.min.css'/>">
  <link rel="stylesheet" href="<c:url value='${assetsBase}/css/style.css'/>">
  <link rel="stylesheet" href="<c:url value='${assetsBase}/css/flatpickr.min.css'/>">
</head>
<body>

  <!-- Mobile menu -->
  <div class="site-mobile-menu site-navbar-target">
    <div class="site-mobile-menu-header">
      <div class="site-mobile-menu-close">
        <span class="icofont-close js-menu-toggle"></span>
      </div>
    </div>
    <div class="site-mobile-menu-body"></div>
  </div>

  <!-- Nav -->
  <nav class="site-nav">
    <div class="container">
      <div class="menu-bg-wrap">
        <div class="site-navigation">
          <div class="row g-0 align-items-center">
            <div class="col-2">
              <a href="index.html" class="logo m-0 float-start">
                <c:out value="${header.brand}"/><span class="text-primary"><c:out value="${header.brandDot}"/></span>
              </a>
            </div>

            <div class="col-8 text-center">
              <form action="#" class="search-form d-inline-block d-lg-none">
                <input type="text" class="form-control" placeholder="Search...">
                <span class="bi-search"></span>
              </form>

              <ul class="js-clone-nav d-none d-lg-inline-block text-start site-menu mx-auto">
                <c:forEach var="item" items="${nav}">
                  <c:choose>
                    <c:when test="${not empty item.children}">
                      <li class="has-children">
                        <a href="<c:out value='${item.href}'/>"><span><c:out value='${item.text}'/></span> <i class="bi bi-chevron-down toggle-dropdown"></i></a>
                        <ul class="dropdown">
                          <c:forEach var="ch" items="${item.children}">
                            <c:choose>
                              <c:when test="${not empty ch.children}">
                                <li class="has-children">
                                  <a href="<c:out value='${ch.href}'/>"><c:out value='${ch.text}'/></a>
                                  <ul class="dropdown">
                                    <c:forEach var="gch" items="${ch.children}">
                                      <li><a href="<c:out value='${gch.href}'/>"><c:out value='${gch.text}'/></a></li>
                                    </c:forEach>
                                  </ul>
                                </li>
                              </c:when>
                              <c:otherwise>
                                <li><a href="<c:out value='${ch.href}'/>"><c:out value='${ch.text}'/></a></li>
                              </c:otherwise>
                            </c:choose>
                          </c:forEach>
                        </ul>
                      </li>
                    </c:when>
                    <c:otherwise>
                      <li class="${item.active eq 'true' ? 'active' : ''}">
                        <a href="<c:out value='${item.href}'/>"><c:out value='${item.text}'/></a>
                      </li>
                    </c:otherwise>
                  </c:choose>
                </c:forEach>
              </ul>
            </div>

            <div class="col-2 text-end">
              <a href="#" class="burger ms-auto float-end site-menu-toggle js-menu-toggle d-inline-block d-lg-none light"><span></span></a>
              <form action="#" class="search-form d-none d-lg-inline-block">
                <input type="text" class="form-control" placeholder="Search...">
                <span class="bi-search"></span>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  </nav>

  <!-- Retro hero -->
  <section class="section bg-light">
    <div class="container">
      <div class="row align-items-stretch retro-layout">
        <!-- left col -->
        <div class="col-md-4">
          <c:forEach var="p" items="${retroCols[0]}">
            <a href="<c:out value='${p.href}'/>" class="h-entry mb-30 v-height gradient">
              <div class="featured-img" style="background-image: url('<c:url value="${assetsBase}/${p.img}"/>');"></div>
              <div class="text">
                <span class="date"><c:out value='${p.date}'/></span>
                <h2><c:out value='${p.title}'/></h2>
              </div>
            </a>
          </c:forEach>
        </div>

        <!-- center col -->
        <div class="col-md-4">
          <c:forEach var="p" items="${retroCols[1]}">
            <a href="<c:out value='${p.href}'/>" class="h-entry img-5 h-100 gradient">
              <div class="featured-img" style="background-image: url('<c:url value="${assetsBase}/${p.img}"/>');"></div>
              <div class="text">
                <span class="date"><c:out value='${p.date}'/></span>
                <h2><c:out value='${p.title}'/></h2>
              </div>
            </a>
          </c:forEach>
        </div>

        <!-- right col -->
        <div class="col-md-4">
          <c:forEach var="p" items="${retroCols[2]}">
            <a href="<c:out value='${p.href}'/>" class="h-entry mb-30 v-height gradient">
              <div class="featured-img" style="background-image: url('<c:url value="${assetsBase}/${p.img}"/>');"></div>
              <div class="text">
                <span class="date"><c:out value='${p.date}'/></span>
                <h2><c:out value='${p.title}'/></h2>
              </div>
            </a>
          </c:forEach>
        </div>
      </div>
    </div>
  </section>

  <!-- Business -->
  <section class="section posts-entry">
    <div class="container">
      <div class="row mb-4">
        <div class="col-sm-6"><h2 class="posts-entry-title"><c:out value='${businessHdr.title}'/></h2></div>
        <div class="col-sm-6 text-sm-end"><a href="<c:out value='${businessHdr.viewAllHref}'/>" class="read-more"><c:out value='${businessHdr.viewAllText}'/></a></div>
      </div>

      <div class="row g-3">
        <div class="col-md-9">
          <div class="row g-3">
            <c:forEach var="b" items="${businessFeatured}">
              <div class="col-md-6">
                <div class="blog-entry">
                  <a href="<c:out value='${b.href}'/>" class="img-link">
                    <img src="<c:url value='${assetsBase}/${b.img}'/>" alt="Image" class="img-fluid">
                  </a>
                  <span class="date"><c:out value='${b.date}'/></span>
                  <h2><a href="<c:out value='${b.href}'/>"><c:out value='${b.title}'/></a></h2>
                  <p><c:out value='${b.excerpt}'/></p>
                  <p><a href="<c:out value='${b.ctaHref}'/>" class="btn btn-sm btn-outline-primary"><c:out value='${b.ctaText}'/></a></p>
                </div>
              </div>
            </c:forEach>
          </div>
        </div>

        <div class="col-md-3">
          <ul class="list-unstyled blog-entry-sm">
            <c:forEach var="s" items="${businessSide}">
              <li>
                <span class="date"><c:out value='${s.date}'/></span>
                <h3><a href="<c:out value='${s.href}'/>"><c:out value='${s.title}'/></a></h3>
                <p><c:out value='${s.excerpt}'/></p>
                <p><a href="<c:out value='${s.readMoreHref}'/>" class="read-more"><c:out value='${s.readMoreText}'/></a></p>
              </li>
            </c:forEach>
          </ul>
        </div>
      </div>
    </div>
  </section>

  <!-- Four-up small section -->
  <section class="section posts-entry posts-entry-sm bg-light">
    <div class="container">
      <div class="row">
        <c:forEach var="f" items="${fourUp}">
          <div class="col-md-6 col-lg-3">
            <div class="blog-entry">
              <a href="<c:out value='${f.href}'/>" class="img-link">
                <img src="<c:url value='${assetsBase}/${f.img}'/>" alt="Image" class="img-fluid">
              </a>
              <span class="date"><c:out value='${f.date}'/></span>
              <h2><a href="<c:out value='${f.href}'/>"><c:out value='${f.title}'/></a></h2>
              <p><c:out value='${f.excerpt}'/></p>
              <p><a href="<c:out value='${f.readMoreHref}'/>" class="read-more">Continue Reading</a></p>
            </div>
          </div>
        </c:forEach>
      </div>
    </div>
  </section>

  <!-- Culture -->
  <section class="section posts-entry">
    <div class="container">
      <div class="row mb-4">
        <div class="col-sm-6"><h2 class="posts-entry-title"><c:out value='${cultureHdr.title}'/></h2></div>
        <div class="col-sm-6 text-sm-end"><a href="<c:out value='${cultureHdr.viewAllHref}'/>" class="read-more"><c:out value='${cultureHdr.viewAllText}'/></a></div>
      </div>

      <div class="row g-3">
        <div class="col-md-9 order-md-2">
          <div class="row g-3">
            <c:forEach var="cItem" items="${cultureFeatured}">
              <div class="col-md-6">
                <div class="blog-entry">
                  <a href="<c:out value='${cItem.href}'/>" class="img-link">
                    <img src="<c:url value='${assetsBase}/${cItem.img}'/>" alt="Image" class="img-fluid">
                  </a>
                  <span class="date"><c:out value='${cItem.date}'/></span>
                  <h2><a href="<c:out value='${cItem.href}'/>"><c:out value='${cItem.title}'/></a></h2>
                  <p><c:out value='${cItem.excerpt}'/></p>
                  <p><a href="<c:out value='${cItem.ctaHref}'/>" class="btn btn-sm btn-outline-primary"><c:out value='${cItem.ctaText}'/></a></p>
                </div>
              </div>
            </c:forEach>
          </div>
        </div>

        <div class="col-md-3">
          <ul class="list-unstyled blog-entry-sm">
            <c:forEach var="s" items="${cultureSide}">
              <li>
                <span class="date"><c:out value='${s.date}'/></span>
                <h3><a href="<c:out value='${s.href}'/>"><c:out value='${s.title}'/></a></h3>
                <p><c:out value='${s.excerpt}'/></p>
                <p><a href="<c:out value='${s.readMoreHref}'/>" class="read-more"><c:out value='${s.readMoreText}'/></a></p>
              </li>
            </c:forEach>
          </ul>
        </div>
      </div>
    </div>
  </section>

  <!-- Politics (9 posts) -->
  <section class="section">
    <div class="container">
      <div class="row mb-4">
        <div class="col-sm-6"><h2 class="posts-entry-title"><c:out value='${politicsHdr.title}'/></h2></div>
        <div class="col-sm-6 text-sm-end"><a href="<c:out value='${politicsHdr.viewAllHref}'/>" class="read-more"><c:out value='${politicsHdr.viewAllText}'/></a></div>
      </div>

      <div class="row">
        <c:forEach var="p" items="${politics}">
          <div class="col-lg-4 mb-4">
            <div class="post-entry-alt">
              <a href="<c:out value='${p.href}'/>" class="img-link">
                <img src="<c:url value='${assetsBase}/${p.img}'/>" alt="Image" class="img-fluid">
              </a>
              <div class="excerpt">
                <h2><a href="<c:out value='${p.href}'/>"><c:out value='${p.title}'/></a></h2>
                <div class="post-meta align-items-center text-left clearfix">
                  <figure class="author-figure mb-0 me-3 float-start">
                    <img src="<c:url value='${assetsBase}/${p.authorImg}'/>" alt="Image" class="img-fluid">
                  </figure>
                  <span class="d-inline-block mt-1">By <a href="#"><c:out value='${p.author}'/></a></span>
                  <span>&nbsp;-&nbsp; <c:out value='${p.date}'/></span>
                </div>
                <p><c:out value='${p.excerpt}'/></p>
                <p><a href="#" class="read-more">Continue Reading</a></p>
              </div>
            </div>
          </div>
        </c:forEach>
      </div>
    </div>
  </section>

  <!-- Travel -->
  <div class="section bg-light">
    <div class="container">
      <div class="row mb-4">
        <div class="col-sm-6"><h2 class="posts-entry-title"><c:out value='${travelHdr.title}'/></h2></div>
        <div class="col-sm-6 text-sm-end"><a href="<c:out value='${travelHdr.viewAllHref}'/>" class="read-more"><c:out value='${travelHdr.viewAllText}'/></a></div>
      </div>

      <div class="row align-items-stretch retro-layout-alt">
        <div class="col-md-5 order-md-2">
          <a href="<c:out value='${travelFeatured.href}'/>" class="hentry img-1 h-100 gradient">
            <div class="featured-img" style="background-image: url('<c:url value="${assetsBase}/${travelFeatured.img}"/>');"></div>
            <div class="text">
              <span><c:out value='${travelFeatured.date}'/></span>
              <h2><c:out value='${travelFeatured.title}'/></h2>
            </div>
          </a>
        </div>

        <div class="col-md-7">
          <a href="<c:out value='${travelLarge.href}'/>" class="hentry img-2 v-height mb30 gradient">
            <div class="featured-img" style="background-image: url('<c:url value="${assetsBase}/${travelLarge.img}"/>');"></div>
            <div class="text text-sm">
              <span><c:out value='${travelLarge.date}'/></span>
              <h2><c:out value='${travelLarge.title}'/></h2>
            </div>
          </a>

          <div class="two-col d-block d-md-flex justify-content-between">
            <c:forEach var="t" items="${travelTwo}">
              <a href="<c:out value='${t.href}'/>" class="hentry v-height img-2 ${status.index == 1 ? 'ms-auto float-end' : ''} gradient">
                <div class="featured-img" style="background-image: url('<c:url value="${assetsBase}/${t.img}"/>');"></div>
                <div class="text text-sm">
                  <span><c:out value='${t.date}'/></span>
                  <h2><c:out value='${t.title}'/></h2>
                </div>
              </a>
            </c:forEach>
          </div>
        </div>
      </div>

    </div>
  </div>

  <!-- Footer -->
  <footer class="site-footer">
    <div class="container">
      <div class="row">
        <div class="col-lg-4">
          <div class="widget">
            <h3 class="mb-4"><c:out value='${footerAboutTitle}'/></h3>
            <p><c:out value='${footerAboutText}'/></p>
          </div>
          <div class="widget">
            <h3>Social</h3>
            <ul class="list-unstyled social">
              <c:forEach var="s" items="${footerSocial}">
                <li><a href="#"><span class="icon-${s}"></span></a></li>
              </c:forEach>
            </ul>
          </div>
        </div>

        <div class="col-lg-4 ps-lg-5">
          <div class="widget">
            <h3 class="mb-4"><c:out value='${footerCompanyTitle}'/></h3>
            <ul class="list-unstyled float-start links">
              <c:forEach var="l" items="${footerCompanyCol1}">
                <li><a href="#"><c:out value='${l}'/></a></li>
              </c:forEach>
            </ul>
            <ul class="list-unstyled float-start links">
              <c:forEach var="l" items="${footerCompanyCol2}">
                <li><a href="#"><c:out value='${l}'/></a></li>
              </c:forEach>
            </ul>
          </div>
        </div>

        <div class="col-lg-4">
          <div class="widget">
            <h3 class="mb-4"><c:out value='${footerRecentTitle}'/></h3>
            <div class="post-entry-footer">
              <ul>
                <c:forEach var="r" items="${footerRecent}">
                  <li>
                    <a href="">
                      <img src="<c:url value='${assetsBase}/${r.img}'/>" alt="Image placeholder" class="me-4 rounded">
                      <div class="text">
                        <h4><c:out value='${r.title}'/></h4>
                        <div class="post-meta"><span class="mr-2"><c:out value='${r.date}'/></span></div>
                      </div>
                    </a>
                  </li>
                </c:forEach>
              </ul>
            </div>
          </div>
        </div>
      </div>

      <div class="row mt-5">
        <div class="col-12 text-center">
          <p>
            <c:out value='${copyrightPrefix}'/> &copy;<script>document.write(new Date().getFullYear());</script>.
            All Rights Reserved. — Designed with love by
            <a href="https://untree.co"><c:out value='${copyrightBrand}'/></a>
            Distributed by <a href="https://themewagon.com"><c:out value='${copyrightDistributor}'/></a>
          </p>
        </div>
      </div>
    </div>
  </footer>

  <!-- Preloader -->
  <div id="overlayer"></div>
  <div class="loader">
    <div class="spinner-border text-primary" role="status"><span class="visually-hidden">Loading...</span></div>
  </div>

  <!-- JS -->
  <script src="<c:url value='${assetsBase}/js/bootstrap.bundle.min.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/tiny-slider.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/flatpickr.min.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/aos.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/glightbox.min.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/navbar.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/counter.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/custom.js'/>"></script>
</body>
</html>
