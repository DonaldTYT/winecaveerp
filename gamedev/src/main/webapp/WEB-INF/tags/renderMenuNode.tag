<%@ tag import="com.kikyosoft.utils.LogUtil" %>
<%@ tag description="Render a single menu node" pageEncoding="UTF-8" %>
<%@ attribute name="node" required="true" type="com.kikyosoft.utils.MenuNode" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="app" tagdir="/WEB-INF/tags" %>

<c:choose>
  <c:when test="${node.caption}">
    <li class="pc-item pc-caption">
      <label><c:out value='${node.text}'/></label>
      <i class="${node.icon}"></i>
    </li>
  </c:when>

  <c:when test="${not empty node.children}">
    <li class="pc-item pc-hasmenu">
      <a href="<c:url value='${node.href}'/>" class="pc-link">
        <span class="pc-micon"><i class="${empty node.icon ? 'ti ti-circle' : node.icon}"></i></span>
        <span class="pc-mtext"><c:out value='${node.text}'/></span>
        <span class="pc-arrow"><i data-feather="chevron-right"></i></span>
      </a>
      <app:renderMenu nodes="${node.children}" root="false"/>
    </li>
  </c:when>

  <c:otherwise>
    <li class="pc-item">
      <a href="<c:url value='${node.href}'/>" class="pc-link">
        <span class="pc-micon"><i class="${empty node.icon ? 'ti ti-circle' : node.icon}"></i></span>
        <span class="pc-mtext"><c:out value='${node.text}'/></span>
      </a>
    </li>
  </c:otherwise>
</c:choose>
