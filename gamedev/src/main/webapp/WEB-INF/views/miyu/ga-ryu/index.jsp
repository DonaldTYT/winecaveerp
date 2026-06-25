<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">

    <meta name="description" content="<c:out value='${meta.description}' default=''/>">
    <meta name="author" content="<c:out value='${meta.author}' default=''/>">

    <link rel="preconnect" href="https://fonts.gstatic.com">
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@100;200;300;400;500;600;700;800;900&display=swap" rel="stylesheet">

    <title><c:out value="${pageTitle}" default="Space Dynamic - SEO HTML5 Template"/></title>

    <!-- Bootstrap core CSS (paths are context-aware) -->
    <link href="<c:url value='/miyu/ga-ryu/vendor/bootstrap/css/bootstrap.min.css'/>" rel="stylesheet">

    <!-- Additional CSS Files -->
    <link rel="stylesheet" href="<c:url value='/miyu/ga-ryu/assets/css/fontawesome.css'/>">
    <link rel="stylesheet" href="<c:url value='/miyu/ga-ryu/assets/css/templatemo-space-dynamic.css'/>">
    <link rel="stylesheet" href="<c:url value='/miyu/ga-ryu/assets/css/animated.css'/>">
    <link rel="stylesheet" href="<c:url value='/miyu/ga-ryu/assets/css/owl.css'/>">
  </head>

<body>

  <!-- ***** Preloader Start ***** -->
  <div id="js-preloader" class="js-preloader">
    <div class="preloader-inner">
      <span class="dot"></span>
      <div class="dots"><span></span><span></span><span></span></div>
    </div>
  </div>
  <!-- ***** Preloader End ***** -->

  <!-- ***** Header Area Start ***** -->
  <c:if test="${pageHasHeader}">
  <header class="header-area header-sticky wow slideInDown" data-wow-duration="0.75s" data-wow-delay="0s">
    <div class="container">
      <div class="row"><div class="col-12">
        <nav class="main-nav">
          <c:if test="${pageHasHomeButton}">
          <a href="<c:url value='${nav.homeHref}'/>" class="logo">
            <h4><c:out value="${brand.prefix}" default="Spac"/><span><c:out value="${brand.suffix}" default="Dyna"/></span></h4>
          </a>
		  </c:if>          
          <c:if test="${pageHasTopMenu}">
          <ul class="nav">
          	<!-- 
            <li class="scroll-to-section"><a href="#top" class="active">Home</a></li>
          	 -->
			<c:if test="${pageHasAboutUs}">
            <li class="scroll-to-section"><a href="#about">About Us</a></li>
			</c:if>
			<c:if test="${pageHasOurPortfolio}">
            <li class="scroll-to-section"><a href="#portfolio">Portfolio</a></li>
			</c:if>
			<c:if test="${pageHasOurServices}">
            <li class="scroll-to-section"><a href="#services">Services</a></li>
			</c:if>
			<c:if test="${pageHasBlog}">
            <li class="scroll-to-section"><a href="#blog">Blog</a></li>
			</c:if>
			<c:if test="${pageHasContact}">
            <li class="scroll-to-section"><a href="#contact">Contact Us</a></li>
			</c:if>
            <li class="scroll-to-section">
              <div class="main-red-button"><a href="#top">Home</a></div>
            </li>
			<!-- 
            <li class="scroll-to-section">
              <div class="main-red-button"><a href="#contact"><c:out value="${ctaText}" default="Contact Now"/></a></div>
            </li>
			 -->
          </ul>
          </c:if>
          <a class='menu-trigger'><span>Menu</span></a>
        </nav>
      </div></div>
    </div>
  </header>
   </c:if>
  <!-- ***** Header Area End ***** -->

  <c:if test="${pageHasMainBanner}">
  <div class="main-banner wow fadeIn" id="top" data-wow-duration="1s" data-wow-delay="0.5s">
    <div class="container">
      <div class="row"><div class="col-lg-12"><div class="row">
        <div class="col-lg-6 align-self-center">
          <div class="left-content header-text wow fadeInLeft" data-wow-duration="1s" data-wow-delay="1s">
            <h6><c:out value="${hero.kicker}" default="Welcome to Space Dynamic"/></h6>
            <h2>
              <c:out value="${hero.h1Before}" default="We Make"/> 
              <em><c:out value="${hero.h1Em}" default="Digital Ideas"/></em> &amp; 
              <span><c:out value="${hero.h1Span}" default="SEO"/></span> 
              <c:out value="${hero.h1After}" default=" Marketing"/>
            </h2>
            <p><c:out value="${hero.text}" default="Space Dynamic is a professional looking HTML template using Bootstrap 5."/>
              <c:if test="${not empty hero.linkHref}">
                <a rel="nofollow" href="${hero.linkHref}" target="_parent"><c:out value='${hero.linkText}' default='TemplateMo'/></a>.
              </c:if>
            </p>
            <form id="search" action="<c:url value='${analyze.action}'/>" method="<c:out value='${analyze.method}' default='GET'/>">
              <fieldset>
                <input type="url" name="address" class="email" placeholder="<c:out value='${analyze.placeholder}' default='Your website URL...'/>" autocomplete="on" required>
              </fieldset>
              <fieldset>
                <button type="submit" class="main-button"><c:out value="${analyze.button}" default="Analyze Site"/></button>
              </fieldset>
            </form>
          </div>
        </div>
        <div class="col-lg-6">
          <div class="right-image wow fadeInRight" data-wow-duration="1s" data-wow-delay="0.5s">
            <img src="<c:url value='/miyu/ga-ryu/assets/images/banner-right-image.png'/>" alt="<c:out value='${hero.imageAlt}' default='team meeting'/>">
          </div>
        </div>
      </div></div></div>
    </div>
  </div>
  </c:if>

  <c:if test="${pageHasAboutUs}">
  <div id="about" class="about-us section">
    <div class="container">
      <div class="row">
        <div class="col-lg-4">
          <div class="left-image wow fadeIn" data-wow-duration="1s" data-wow-delay="0.2s">
            <img src="<c:url value='/miyu/ga-ryu/assets/images/about-left-image.png'/>" alt="person graphic">
          </div>
        </div>
        <div class="col-lg-8 align-self-center">
          <div class="services"><div class="row">
            <c:forEach var="svc" items="${services}">
              <div class="col-lg-6">
                <div class="item wow fadeIn" data-wow-duration="1s" data-wow-delay="${svc.delay}">
                  <div class="icon">
                    <img src="<c:url value='${svc.icon}'/>" alt="<c:out value='${svc.alt}'/>">
                  </div>
                  <div class="right-text">
                    <h4><c:out value="${svc.title}"/></h4>
                    <p><c:out value="${svc.text}"/></p>
                  </div>
                </div>
              </div>
            </c:forEach>
            <c:if test="${empty services}">
              <div class="col-12"><p>(No services yet.)</p></div>
            </c:if>
          </div></div>
        </div>
      </div>
    </div>
  </div>
  </c:if>

  <c:if test="${pageHasOurServices}">
  <div id="services" class="our-services section">
    <div class="container">
      <div class="row">
        <div class="col-lg-6 align-self-center wow fadeInLeft" data-wow-duration="1s" data-wow-delay="0.2s">
          <div class="left-image">
            <img src="<c:url value='/miyu/ga-ryu/assets/images/services-left-image.png'/>" alt="">
          </div>
        </div>
        <div class="col-lg-6 wow fadeInRight" data-wow-duration="1s" data-wow-delay="0.2s">
          <div class="section-heading">
            <h2>
              Grow your website with our <em><c:out value='${servicesBlock.em}' default='SEO'/></em> service &amp; 
              <span><c:out value='${servicesBlock.span}' default='Project'/></span> Ideas
            </h2>
            <p><c:out value='${servicesBlock.paragraph}'/></p>
          </div>

          <div class="row">
            <c:forEach var="bar" items="${progressBars}">
              <div class="col-lg-12">
                <div class="${bar.cssClass} progress-skill-bar">
                  <h4><c:out value='${bar.title}'/></h4>
                  <span><c:out value='${bar.percent}'/>%</span>
                  <div class="filled-bar" style="width: <c:out value='${bar.percent}'/>%;"></div>
                  <div class="full-bar"></div>
                </div>
              </div>
            </c:forEach>
          </div>
        </div>
      </div>
    </div>
  </div>
  </c:if>

  <c:if test="${pageHasOurPortfolio}">
  <div id="portfolio" class="our-portfolio section">
    <div class="container">
      <div class="row">
        <div class="col-lg-6 offset-lg-3">
          <div class="section-heading wow bounceIn" data-wow-duration="1s" data-wow-delay="0.2s">
            <h2>See What Our Agency <em>Offers</em> &amp; What We <span>Provide</span></h2>
          </div>
        </div>
      </div>
      <div class="row">
        <c:forEach var="item" items="${portfolio}">
          <div class="col-lg-3 col-sm-6">
            <a href="${item.href}">
              <div class="item wow bounceInUp" data-wow-duration="1s" data-wow-delay="${item.delay}">
                <div class="hidden-content">
                  <h4><c:out value='${item.title}'/></h4>
                  <p><c:out value='${item.text}'/></p>
                </div>
                <div class="showed-content">
                  <img src="<c:url value='${item.image}'/>" alt="">
                </div>
              </div>
            </a>
          </div>
        </c:forEach>
      </div>
    </div>
  </div>
  </c:if>

  <c:if test="${pageHasBlog}">
  <div id="blog" class="our-blog section">
    <div class="container">
      <div class="row">
        <div class="col-lg-6 wow fadeInDown" data-wow-duration="1s" data-wow-delay="0.25s">
          <div class="section-heading">
            <h2>Check Out What Is <em>Trending</em> In Our Latest <span>News</span></h2>
          </div>
        </div>
        <div class="col-lg-6 wow fadeInDown" data-wow-duration="1s" data-wow-delay="0.25s">
          <div class="top-dec">
            <img src="<c:url value='/miyu/ga-ryu/assets/images/blog-dec.png'/>" alt="">
          </div>
        </div>
      </div>

      <div class="row">
        <!-- Main blog card -->
        <div class="col-lg-6 wow fadeInUp" data-wow-duration="1s" data-wow-delay="0.25s">
          <div class="left-image">
            <a href="${blogMain.href}"><img src="<c:url value='${blogMain.image}'/>" alt="<c:out value='${blogMain.alt}' default='Workspace Desktop'/>"></a>
            <div class="info">
              <div class="inner-content">
                <ul>
                  <li><i class="fa fa-calendar"></i> <c:out value='${blogMain.date}'/></li>
                  <li><i class="fa fa-users"></i> <c:out value='${blogMain.author}'/></li>
                  <li><i class="fa fa-folder"></i> <c:out value='${blogMain.category}'/></li>
                </ul>
                <a href="${blogMain.href}"><h4><c:out value='${blogMain.title}'/></h4></a>
                <p><c:out value='${blogMain.excerpt}'/></p>
                <div class="main-blue-button"><a href="${blogMain.href}"><c:out value='${blogMain.cta}' default='Discover More'/></a></div>
              </div>
            </div>
          </div>
        </div>

        <!-- Blog list -->
        <div class="col-lg-6 wow fadeInUp" data-wow-duration="1s" data-wow-delay="0.25s">
          <div class="right-list">
            <ul>
              <c:forEach var="p" items="${blogPosts}">
                <li>
                  <div class="left-content align-self-center">
                    <span><i class="fa fa-calendar"></i> <c:out value='${p.date}'/></span>
                    <a href="${p.href}"><h4><c:out value='${p.title}'/></h4></a>
                    <p><c:out value='${p.excerpt}'/></p>
                  </div>
                  <div class="right-image">
                    <a href="${p.href}"><img src="<c:url value='${p.thumb}'/>" alt=""></a>
                  </div>
                </li>
              </c:forEach>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
  </c:if>

  <c:if test="${pageHasContact}">
  <div id="contact" class="contact-us section">
    <div class="container">
      <div class="row">
        <div class="col-lg-6 align-self-center wow fadeInLeft" data-wow-duration="0.5s" data-wow-delay="0.25s">
          <div class="section-heading">
            <h2><c:out value='${contact.heading}'/></h2>
            <p><c:out value='${contact.paragraph}'/></p>
            <div class="phone-info">
              <h4>For any enquiry, Call Us: 
                <span><i class="fa fa-phone"></i> <a href="tel:${contact.phone}"><c:out value='${contact.phone}'/></a></span>
              </h4>
            </div>
          </div>
        </div>
        <div class="col-lg-6 wow fadeInRight" data-wow-duration="0.5s" data-wow-delay="0.25s">
          <form id="contactForm" action="<c:url value='${contact.formAction}'/>" method="<c:out value='${contact.formMethod}' default='post'/>">
            <div class="row">
              <div class="col-lg-6"><fieldset><input type="text" name="name" id="name" placeholder="Name" required></fieldset></div>
              <div class="col-lg-6"><fieldset><input type="text" name="surname" id="surname" placeholder="Surname" required></fieldset></div>
              <div class="col-lg-12"><fieldset><input type="email" name="email" id="email" placeholder="Your Email" required></fieldset></div>
              <div class="col-lg-12"><fieldset><textarea name="message" class="form-control" id="message" placeholder="Message" required></textarea></fieldset></div>
              <div class="col-lg-12"><fieldset><button type="submit" id="form-submit" class="main-button">Send Message</button></fieldset></div>
            </div>
            <div class="contact-dec"><img src="<c:url value='/miyu/ga-ryu/assets/images/contact-decoration.png'/>" alt=""></div>
          </form>
        </div>
      </div>
    </div>
  </div>
  </c:if>

  <c:if test="${pageHasFooter}">
  <footer>
    <div class="container">
      <div class="row"><div class="col-lg-12 wow fadeIn" data-wow-duration="1s" data-wow-delay="0.25s">
        <p>&copy; <c:out value='${footer.year}'/> <c:out value='${footer.company}'/>. All Rights Reserved.
        <br>Design: <a rel="nofollow" href="<c:out value='${footer.designHref}' default='https://templatemo.com'/>">TemplateMo</a></p>
      </div></div>
    </div>
  </footer>
  </c:if>

  <!-- Scripts -->
  <script src="<c:url value='/miyu/ga-ryu/vendor/jquery/jquery.min.js'/>"></script>
  <script src="<c:url value='/miyu/ga-ryu/vendor/bootstrap/js/bootstrap.bundle.min.js'/>"></script>
  <script src="<c:url value='/miyu/ga-ryu/assets/js/owl-carousel.js'/>"></script>
  <script src="<c:url value='/miyu/ga-ryu/assets/js/animation.js'/>"></script>
  <script src="<c:url value='/miyu/ga-ryu/assets/js/imagesloaded.js'/>"></script>
  <script src="<c:url value='/miyu/ga-ryu/assets/js/templatemo-custom.js'/>"></script>
</body>
</html>
