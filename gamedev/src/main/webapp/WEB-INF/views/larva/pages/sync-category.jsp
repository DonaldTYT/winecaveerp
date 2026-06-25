<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="assetsBase" value="${assetsBase != null ? assetsBase : '/larva/assets'}"/>
<c:set var="pageTitle" value="${uiText.htmlTitle != null ? uiText.htmlTitle : 'Sync Category to Saleor'}"/>

<!DOCTYPE html>
<html lang="en">
<head>
  <title><c:out value="${pageTitle}"/></title>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimal-ui">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="description" content="Mantis is made using Bootstrap 5 design framework.">
  <meta name="keywords" content="Mantis, Dashboard UI Kit, Bootstrap 5, Admin Template">

  <link rel="icon" href="<c:url value='${assetsBase}/images/favicon.svg'/>" type="image/x-icon">

  <!-- Fonts & Icons (same as login.jsp) -->
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Public+Sans:wght@300;400;500;600;700&display=swap" id="main-font-link">
  <link rel="stylesheet" href="<c:url value='${assetsBase}/fonts/tabler-icons.min.css'/>">
  <link rel="stylesheet" href="<c:url value='${assetsBase}/fonts/feather.css'/>">
  <link rel="stylesheet" href="<c:url value='${assetsBase}/fonts/fontawesome.css'/>">
  <link rel="stylesheet" href="<c:url value='${assetsBase}/fonts/material.css'/>">

  <!-- Mantis CSS -->
  <link rel="stylesheet" href="<c:url value='${assetsBase}/css/style.css'/>" id="main-style-link">
  <link rel="stylesheet" href="<c:url value='${assetsBase}/css/style-preset.css'/>">

  <style>
    .maxw-700 { max-width: 700px; }
    .center-box { margin: 48px auto; }
    .title-strong { font-weight: 700; }
    .form-card { border: 2px solid #000; border-radius: .5rem; }
  </style>
</head>
<body>
  <div class="container">
    <div class="center-box maxw-700">
      <div class="card form-card shadow-0">
        <!-- ✨ this wrapper was missing -->
        <div class="card-body text-center">
          <h3 class="mb-5 title-strong">Sync Category to Saleor</h3>

          <!-- UI fragment -->
          <jsp:include page="/WEB-INF/views/larva/fragments/sync-category-form.jspf">
            <jsp:param name="ids" value="sc1"/>
            <jsp:param name="actionUrl" value="${pageContext.request.contextPath}/larva/pages/sync-category"/>
          </jsp:include>

          <!-- JS behavior fragment -->
          <jsp:include page="/WEB-INF/views/larva/fragments/sync-category-form.js.jspf">
            <jsp:param name="ids" value="sc1"/>
            <jsp:param name="statusUrl" value="${pageContext.request.contextPath}/larva/pages/sync-category-status"/>
          </jsp:include>
        </div>
      </div>
    </div>
  </div>

  <!-- ✅ bring back the JS bundle just like login.jsp -->
  <script src="<c:url value='${assetsBase}/js/plugins/popper.min.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/plugins/simplebar.min.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/plugins/bootstrap.min.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/fonts/custom-font.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/pcoded.js'/>"></script>
  <script src="<c:url value='${assetsBase}/js/plugins/feather.min.js'/>"></script>
  <script>
    // optional Mantis presets
    try {
      layout_change('light'); change_box_container('false'); layout_rtl_change('false');
      preset_change("preset-1"); font_change("Public-Sans");
    } catch(e) {}
  </script>
</body>
</html>
