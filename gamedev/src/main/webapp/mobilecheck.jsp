<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<jsp:include page="commonInit.jsp" flush="true" />
<script src="js/zkbiloader-camera.js?v=260531-01"></script>
</head>
<body>
	<jsp:include page='zkf/mobileCheck.zul' flush="true" />
<%
	if ("Y".equalsIgnoreCase(request.getParameter("camera"))) {
%>
	<button type="button" onclick="ZkBiCamera.open({mode:'photo'});" style="position:fixed;right:12px;bottom:12px;z-index:9999;">Camera</button>
<%
	}
%>
</body>
</html>
