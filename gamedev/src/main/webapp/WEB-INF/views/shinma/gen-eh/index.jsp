<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<!-- Translate banner (auto-shown if user language != EN) -->
<style>
  .translate-banner {
    position: fixed; left: 0; right: 0; bottom: 0; z-index: 1050;
    display: none; /* shown by JS if needed */
    background: #0d6efd; color: #fff;
    padding: .75rem 1rem; box-shadow: 0 -6px 18px rgba(0,0,0,.12);
  }
  .translate-banner .msg { margin: 0; font-size: .95rem; }
  .translate-banner .actions { gap: .5rem; }
  @media (min-width: 992px) {
    .translate-banner .wrap { display:flex; align-items:center; justify-content:space-between; max-width:1140px; margin:0 auto; }
  }
</style>

<div id="translateBanner" class="translate-banner">
  <div class="wrap">
    <p class="msg">
      We detected your browser language is <span id="detectedLangLabel"></span>. Would you like to translate this page?
    </p>
    <div class="actions d-flex">
      <a id="translateBtn" class="btn btn-light btn-sm" rel="nofollow noopener" target="_top">Translate</a>
      <button id="dismissTranslate" type="button" class="btn btn-outline-light btn-sm">No thanks</button>
    </div>
  </div>
</div>

<script>
(function () {
  // 1) Detect preferred language
  const langs = navigator.languages && navigator.languages.length ? navigator.languages : [navigator.language || 'en'];
  const pref = (langs[0] || 'en').toLowerCase();
  const pref2 = pref.slice(0,2);

  // 2) Only offer if not English
  if (pref2 === 'en') return;

  // Optional: respect user dismiss for 30 days
  try {
    const key = 'translateBanner:dismissUntil';
    const until = localStorage.getItem(key);
    if (until && Date.now() < parseInt(until,10)) return;
  } catch(_) {}

  // 3) Build Google Translate URL (works across Chrome/Edge/Safari/Firefox)
  const url = location.href;
  const tl  = encodeURIComponent(pref); // target language, e.g. 'zh-tw', 'fr'
  const glink = 'https://translate.google.com/translate?sl=auto&tl=' + tl + '&u=' + encodeURIComponent(url);

  // 4) Hook up UI
  const banner = document.getElementById('translateBanner');
  const label  = document.getElementById('detectedLangLabel');
  const btn    = document.getElementById('translateBtn');
  const dismiss= document.getElementById('dismissTranslate');

  // Nice label (language code → readable)
  const displayName = (Intl.DisplayNames ? new Intl.DisplayNames([pref], {type:'language'}) : null);
  label.textContent = displayName ? (displayName.of(pref2) || pref) : pref;

  btn.href = glink;

  dismiss.addEventListener('click', function () {
    try {
      const days30 = 30 * 24 * 60 * 60 * 1000;
      localStorage.setItem('translateBanner:dismissUntil', String(Date.now() + days30));
    } catch(_) {}
    banner.style.display = 'none';
  });

  // 5) Show the banner
  banner.style.display = 'block';
})();
</script>

<noscript>
  <!-- Fallback: simple translate link -->
  <div style="position:fixed;left:0;right:0;bottom:0;background:#0d6efd;color:#fff;padding:.5rem 1rem;z-index:1050">
    Your browser language is not English.
    <a href="https://translate.google.com/translate?sl=auto&tl=auto&u=<c:url value=''/>" style="color:#fff;text-decoration:underline" rel="nofollow noopener" target="_top">Translate with Google</a>.
  </div>
</noscript>


<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta content="width=device-width, initial-scale=1.0" name="viewport">
  <title><c:out value="${pageTitle}"/></title>
  <meta name="description" content="<c:out value='${metaDescription}'/>">
  <meta name="keywords" content="<c:out value='${metaKeywords}'/>">

  <!-- Favicons -->
  <link href="<c:url value='${favIcon}'/>" rel="icon">
  <link href="<c:url value='${appleIcon}'/>" rel="apple-touch-icon">

  <!-- Fonts -->
  <link href="https://fonts.googleapis.com" rel="preconnect">
  <link href="https://fonts.gstatic.com" rel="preconnect" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@100;300;400;500;700;900&family=Poppins:wght@100;200;300;400;500;600;700;800;900&family=Raleway:wght@100;200;300;400;500;600;700;800;900&display=swap" rel="stylesheet">

  <!-- Vendor CSS Files -->
  <link href="<c:url value='${assetsBase}/assets/vendor/bootstrap/css/bootstrap.min.css'/>" rel="stylesheet">
  <link href="<c:url value='${assetsBase}/assets/vendor/bootstrap-icons/bootstrap-icons.css'/>" rel="stylesheet">
  <link href="<c:url value='${assetsBase}/assets/vendor/aos/aos.css'/>" rel="stylesheet">
  <link href="<c:url value='${assetsBase}/assets/vendor/glightbox/css/glightbox.min.css'/>" rel="stylesheet">
  <link href="<c:url value='${assetsBase}/assets/vendor/swiper/swiper-bundle.min.css'/>" rel="stylesheet">

  <!-- Main CSS File -->
  <link href="<c:url value='${assetsBase}/assets/css/main.css'/>" rel="stylesheet">
</head>

<style>
/* Increase logo image size */
.header .logo img {
  max-height: 80px;   /* adjust this number until it fills the white bar */
  width: auto;        /* maintain aspect ratio */
}
</style>

<body class="index-page">

  <!-- Header -->
  <header id="header" class="header d-flex align-items-center sticky-top">
    <div class="container-fluid container-xl position-relative d-flex align-items-center">

      <a href="#hero" class="logo d-flex align-items-center me-auto">
        <img src="<c:url value='${assetsBase}/${brand.logoImg}'/>" alt="">
        <!-- <img src="<c:url value='${assetsBase}/assets/img/logo.png'/>" alt=""> -->
        <h1 class="sitename d-none d-lg-block"><c:out value="${brand.logoText}"/></h1>
      </a>

      <nav id="navmenu" class="navmenu">
        <ul>
        <!-- 
          <c:forEach var="n" items="${nav}">
            <c:choose>
              <c:when test="${not empty n.children}">
                <li class="dropdown">
                  <a href="<c:out value='${n.href}'/>">
                    <span><c:out value="${n.text}"/></span>
                    <i class="bi bi-chevron-down toggle-dropdown"></i>
                  </a>
                  <ul>
                    <c:forEach var="citem" items="${n.children}">
                      <c:choose>
                        <c:when test="${not empty citem.children}">
                          <li class="dropdown">
                            <a href="<c:out value='${citem.href}'/>">
                              <span><c:out value='${citem.text}'/></span>
                              <i class="bi bi-chevron-down toggle-dropdown"></i>
                            </a>
                            <ul>
                              <c:forEach var="cc" items="${citem.children}">
                                <li><a href="<c:out value='${cc.href}'/>"><c:out value="${cc.text}"/></a></li>
                              </c:forEach>
                            </ul>
                          </li>
                        </c:when>
                        <c:otherwise>
                          <li><a href="<c:out value='${citem.href}'/>"><c:out value="${citem.text}"/></a></li>
                        </c:otherwise>
                      </c:choose>
                    </c:forEach>
                  </ul>
                </li>
              </c:when>
              <c:otherwise>
                <li><a href="<c:out value='${n.href}'/>" class="${n.active ? 'active' : ''}"><c:out value="${n.text}"/></a></li>
              </c:otherwise>
            </c:choose>
          </c:forEach>
        </ul>
         -->
        <i class="mobile-nav-toggle d-xl-none bi bi-list" style="display:none"></i>
      </nav>

      <a class="btn-getstarted" href="<c:out value='${cta.href}'/>"><c:out value="${cta.text}"/></a>
    </div>
  </header>

  <main class="main">
    <!-- Hero -->
    <section id="hero" class="hero section dark-background">
      <div id="hero-carousel" class="carousel slide carousel-fade" data-bs-ride="carousel" data-bs-interval="5000">
        <c:forEach var="s" items="${heroSlides}" varStatus="st">
          <div class="carousel-item ${st.first ? 'active' : ''}">
            <img src="<c:url value='${assetsBase}/${s.img}'/>" alt="">
            <div class="carousel-container">
              <h2><c:out value='${s.title}'/><br></h2>
              <p><c:out value='${s.desc}'/></p>
              <!-- 
              <a href="<c:out value='${heroCtaHref}'/>" class="btn-get-started"><c:out value='${heroCtaText}'/></a>
               -->
            </div>
          </div>
        </c:forEach>

        <a class="carousel-control-prev" href="#hero-carousel" role="button" data-bs-slide="prev">
          <span class="carousel-control-prev-icon bi bi-chevron-left" aria-hidden="true"></span>
        </a>
        <a class="carousel-control-next" href="#hero-carousel" role="button" data-bs-slide="next">
          <span class="carousel-control-next-icon bi bi-chevron-right" aria-hidden="true"></span>
        </a>
        <ol class="carousel-indicators"></ol>
      </div>
    </section>

    <!-- About -->
    <section id="about" class="about section">
      <div class="container section-title" data-aos="fade-up">
        <h2><c:out value='${aboutSection.title}'/></h2>
        <p><c:out value='${aboutSection.subtitle}'/><br></p>
      </div>

      <div class="container">
        <div class="row gy-4">
          <div class="col-lg-6 content" data-aos="fade-up" data-aos-delay="100">
            <p><c:out value='${aboutSection.lead}'/></p>
            <ul>
              <c:forEach var="b" items="${aboutSection.bullets}">
                <li><i class="bi bi-check2-circle"></i> <span><c:out value='${b}'/></span></li>
              </c:forEach>
            </ul>
          </div>

          <div class="col-lg-6" data-aos="fade-up" data-aos-delay="200">
            <p><c:out value='${aboutSection.moreText}'/></p>
            <a href="<c:out value='${aboutSideHref}'/>" class="read-more">
              <span><c:out value='${aboutSection.moreLabel}'/></span><i class="bi bi-arrow-right"></i>
            </a>
          </div>
        </div>
      </div>
    </section>

    <!-- Clients -->
    <section id="clients" class="clients section light-background">
      <div class="container" data-aos="fade-up">
        <div class="row gy-4">
          <c:forEach var="cimg" items="${clients}">
            <div class="col-xl-2 col-md-3 col-6 client-logo">
              <img src="<c:url value='${assetsBase}/${cimg}'/>" class="img-fluid" alt="">
            </div>
          </c:forEach>
        </div>
      </div>
    </section>

    <!-- Services -->
    <section id="services" class="services section">
      <div class="container">
        <div class="row gy-4">
          <c:forEach var="svc" items="${services}" varStatus="s">
            <div class="col-md-6" data-aos="fade-up" data-aos-delay="${(s.index+1)*100}">
              <div class="service-item d-flex position-relative h-100">
                <i class="${svc.icon} icon flex-shrink-0"></i>
                <div>
                  <h4 class="title"><a href="<c:out value='${svc.href}'/>" class="stretched-link"><c:out value='${svc.title}'/></a></h4>
                  <p class="description"><c:out value='${svc.desc}'/></p>
                </div>
              </div>
            </div>
          </c:forEach>
        </div>
      </div>
    </section>

    <!-- Portfolio -->
    <section id="portfolio" class="portfolio section">
      <div class="container section-title" data-aos="fade-up">
        <h2><c:out value='${portfolioTitle}'/></h2>
        <p><c:out value='${portfolioSubtitle}'/></p>
      </div>

      <div class="container">
        <div class="isotope-layout" data-default-filter="*" data-layout="masonry" data-sort="original-order">
          <ul class="portfolio-filters isotope-filters" data-aos="fade-up" data-aos-delay="100">
            <c:forEach var="f" items="${portfolioFilters}">
              <li data-filter="<c:out value='${f.filter}'/>" class="${f.active ? 'filter-active' : ''}">
                <c:out value='${f.label}'/>
              </li>
            </c:forEach>
          </ul>

          <div class="row gy-4 isotope-container" data-aos="fade-up" data-aos-delay="200">
            <c:forEach var="p" items="${portfolioItems}">
              <div class="col-lg-4 col-md-6 portfolio-item isotope-item ${p.filter}">
                <img src="<c:url value='${assetsBase}/${p.img}'/>" class="img-fluid" alt="">
                <div class="portfolio-info">
                  <h4><c:out value='${p.title}'/></h4>
                  <p><c:out value='${p.desc}'/></p>
                  <a href="<c:url value='${assetsBase}/${p.lightbox}'/>" title="<c:out value='${p.title}'/>" data-gallery="portfolio-gallery" class="glightbox preview-link"><i class="bi bi-zoom-in"></i></a>
                  <a href="<c:out value='${p.details}'/>" title="More Details" class="details-link"><i class="bi bi-link-45deg"></i></a>
                </div>
              </div>
            </c:forEach>
          </div>
        </div>
      </div>
    </section>
  </main>

  <!-- Footer -->
  <footer id="footer" class="footer dark-background">
    <div class="container footer-top">
      <div class="row gy-4">
        <div class="col-lg-4 col-md-6 footer-about">
          <a href="#hero" class="logo d-flex align-items-center">
            <span class="sitename"><c:out value='${footerBrand}'/></span>
          </a>
          <div class="footer-contact pt-3">
            <p><c:out value='${footerContact.addr1}'/></p>
            <p><c:out value='${footerContact.addr2}'/></p>
            <p class="mt-3"><strong><c:out value='${footerContact.phoneLabel}'/></strong> <span><c:out value='${footerContact.phone}'/></span></p>
            <p><strong><c:out value='${footerContact.emailLabel}'/></strong> <span><c:out value='${footerContact.email}'/></span></p>
          </div>
          <!-- 
          <div class="social-links d-flex mt-4">
            <a href="#"><i class="bi bi-twitter-x"></i></a>
            <a href="#"><i class="bi bi-facebook"></i></a>
            <a href="#"><i class="bi bi-instagram"></i></a>
            <a href="#"><i class="bi bi-linkedin"></i></a>
          </div>
           -->
        </div>

        <div class="col-lg-2 col-md-3 footer-links">
          <h4><c:out value='${footerLinks1Title}'/></h4>
          <ul>
            <c:forEach var="l" items="${footerLinks1}">
              <li><a href="#"><c:out value='${l}'/></a></li>
            </c:forEach>
          </ul>
        </div>

<!-- 
        <div class="col-lg-2 col-md-3 footer-links">
          <h4><c:out value='${footerLinks2Title}'/></h4>
          <ul>
            <c:forEach var="l2" items="${footerLinks2}">
              <li><a href="#"><c:out value='${l2}'/></a></li>
            </c:forEach>
          </ul>
        </div>
 -->
        <div class="col-lg-4 col-md-12 footer-newsletter">
          <h4><c:out value='${newsletterTitle}'/></h4>
          <p><c:out value='${newsletterNote}'/></p>
          <form action="forms/newsletter.php" method="post" class="php-email-form">
            <div class="newsletter-form"><input type="email" name="email"><input type="submit" value="Subscribe"></div>
            <div class="loading">Loading</div>
            <div class="error-message"></div>
            <div class="sent-message">Your subscription request has been sent. Thank you!</div>
          </form>
        </div>
      </div>
    </div>

    <div class="container copyright text-center mt-4">
      <p>© <span>Copyright</span> <strong class="px-1 sitename"><c:out value='${copyRight}'/></strong> <span>All Rights Reserved</span></p>
      <div class="credits"><c:out value='${creditsHtml}' escapeXml="false"/></div>
    </div>
  </footer>

  <!-- Scroll Top -->
  <a href="#" id="scroll-top" class="scroll-top d-flex align-items-center justify-content-center">
    <i class="bi bi-arrow-up-short"></i>
  </a>

  <!-- Preloader -->
  <div id="preloader"></div>

  <!-- Vendor JS Files -->
  <script src="<c:url value='${assetsBase}/assets/vendor/bootstrap/js/bootstrap.bundle.min.js'/>"></script>
  <script src="<c:url value='${assetsBase}/assets/vendor/php-email-form/validate.js'/>"></script>
  <script src="<c:url value='${assetsBase}/assets/vendor/aos/aos.js'/>"></script>
  <script src="<c:url value='${assetsBase}/assets/vendor/glightbox/js/glightbox.min.js'/>"></script>
  <script src="<c:url value='${assetsBase}/assets/vendor/imagesloaded/imagesloaded.pkgd.min.js'/>"></script>
  <script src="<c:url value='${assetsBase}/assets/vendor/isotope-layout/isotope.pkgd.min.js'/>"></script>
  <script src="<c:url value='${assetsBase}/assets/vendor/purecounter/purecounter_vanilla.js'/>"></script>
  <script src="<c:url value='${assetsBase}/assets/vendor/waypoints/noframework.waypoints.js'/>"></script>
  <script src="<c:url value='${assetsBase}/assets/vendor/swiper/swiper-bundle.min.js'/>"></script>

  <!-- Main JS File -->
  <script src="<c:url value='${assetsBase}/assets/js/main.js'/>"></script>
</body>
</html>
