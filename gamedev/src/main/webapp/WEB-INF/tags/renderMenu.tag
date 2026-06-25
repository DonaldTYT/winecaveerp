<%@ tag import="com.kikyosoft.utils.LogUtil" %>
<%@ tag description="Render menu list (root or submenu)" pageEncoding="UTF-8" %>
<%@ attribute name="nodes" required="true" type="java.util.List" %>
<%@ attribute name="root"  required="false" type="java.lang.Boolean" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="app" tagdir="/WEB-INF/tags" %>

<ul class="${root == true ? 'pc-navbar' : 'pc-submenu'}">
  <c:forEach var="n" items="${nodes}">
    <app:renderMenuNode node="${n}" />
  </c:forEach>
</ul>
