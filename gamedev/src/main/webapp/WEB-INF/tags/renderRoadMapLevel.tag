<%@ tag description="Render one RoadMap level and its hidden child levels" pageEncoding="UTF-8" %>
<%@ attribute name="nodes" required="true" type="java.util.List" %>
<%@ attribute name="levelId" required="true" type="java.lang.String" %>
<%@ attribute name="parentId" required="false" type="java.lang.String" %>
<%@ attribute name="levelTitle" required="true" type="java.lang.String" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="app" tagdir="/WEB-INF/tags" %>

<section class="shell-roadmap-level${levelId == 'root' ? '' : ' d-none'}"
         data-roadmap-level="${levelId}" data-roadmap-parent="${parentId}">
  <span class="d-none" data-roadmap-level-title><c:out value="${levelTitle}"/></span>
  <div class="row g-3">
    <c:forEach var="node" items="${nodes}" varStatus="status">
      <c:choose>
        <c:when test="${node.caption}">
          <div class="col-12 shell-roadmap-caption">
            <h6 class="mb-0 text-uppercase text-muted">
              <i class="${node.icon} me-1"></i><c:out value="${node.text}"/>
            </h6>
          </div>
        </c:when>
        <c:otherwise>
          <div class="col-12 col-md-6 col-xl-3">
            <c:choose>
              <c:when test="${not empty node.children}">
                <button type="button" class="shell-roadmap-tile w-100"
                        data-roadmap-target="${levelId}-${status.index}">
                  <span class="shell-roadmap-media">
                    <i class="${empty node.icon ? 'ti ti-folder' : node.icon}"></i>
                  </span>
                  <span class="shell-roadmap-tile-title"><c:out value="${node.text}"/></span>
                  <span class="shell-roadmap-tile-hint">Open menu <i class="ti ti-chevron-right"></i></span>
                </button>
              </c:when>
              <c:otherwise>
                <a class="shell-roadmap-tile" href="<c:url value='${node.href}'/>">
                  <span class="shell-roadmap-media">
                    <i class="${empty node.icon ? 'ti ti-app-window' : node.icon}"></i>
                  </span>
                  <span class="shell-roadmap-tile-title"><c:out value="${node.text}"/></span>
                </a>
              </c:otherwise>
            </c:choose>
          </div>
        </c:otherwise>
      </c:choose>
    </c:forEach>
  </div>
</section>

<c:forEach var="node" items="${nodes}" varStatus="status">
  <c:if test="${not node.caption and not empty node.children}">
    <app:renderRoadMapLevel nodes="${node.children}"
                            levelId="${levelId}-${status.index}"
                            parentId="${levelId}"
                            levelTitle="${node.text}"/>
  </c:if>
</c:forEach>
