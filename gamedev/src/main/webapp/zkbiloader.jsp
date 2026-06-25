<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page import="java.util.*, com.uniinformation.utils.*,com.uniinformation.webcore.*,org.apache.commons.lang3.StringUtils " %>
<%
response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
response.setHeader("Expires", "0"); // Proxies.
try {	
	SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper(request, response);   
	sessionHelper.validateLogin(request, response);
%>
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml">
<style>
        .textInBlack {
			color: black !important;
        }
        .textInRed {
			color: red !important;
        }
        .textInGreen {
			color: green !important;
        }
        .textInBlue {
			color: blue !important;
        }
        .backgroundInWhite {
			background-color: white !important;
        }
        .backgroundInRed {
			background-color: red !important;
        }
        .backgroundInGreen {
			background-color: green !important;
        }
        .backgroundInBlue {
			background-color: blue !important;
        }
        .textInError {
			color: red !important;
			text-decoration-line: line-through;
        }
</style>


<head>
<jsp:include page="commonInit.jsp" flush="true" />
<script>
//muLoadCss(null, "css/zkbiloader.css?version=<%=sessionHelper.getVersionId()%>", "cssMobile/zkbiloader.css?version=<%=sessionHelper.getVersionId()%>");

var channelPort;
$(document).ready(function(){
	$(window).on('message', function(e){
		const ports = e.originalEvent.ports;
		if (ports && ports.length > 0) {
			channelPort = ports[0];
			channelPort.onmessage = function(e) {
				console.log('zkbiloader receive port message:' + e.data);
				switch (e.data) {
					case 'doClose':
	        			var zkComp = zk.Widget.$('$zkbibrowser');  //get component by id
	        			if (zkComp !== null && zAu !== null)
	        				zAu.send(new zk.Event(zkComp, "onCloseParentWindow", null, {toServer:true}));	        		
	        			else
							channelPort.postMessage('closeWindow');
						break;
				}
	        };
			if (e.originalEvent.data === 'connectIFrame') {
				console.log('zkbiloader received message connectIFrame');
				//channelPort.postMessage('connectedIFrame');
       			var zkComp = zk.Widget.$('$zkbibrowser');
       			if (zkComp !== null && zAu !== null)
       				zAu.send(new zk.Event(zkComp, "onConnectJsClient", null, {toServer:true}));	        		
			}
		}
	});
});
function closeParentWindow() {
	console.log('closeParentWindow:' + channelPort);
	if (channelPort)
		channelPort.postMessage('closeWindow');
}
function connectJsClient() {
	console.log('connectJsClient:' + channelPort);
	if (channelPort)
		channelPort.postMessage('connectedIFrame');
}


</script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title><%=sessionHelper.getWebPageTitle()%></title>
<style>
<%
	if (sessionHelper.getAllowWallpaper()){
	
%>
@media (orientation: landscape) {
	body{ 
		background-image: url("<%=sessionHelper.getWallPaperPath(false)%>") !important;
		background-repeat: no-repeat !important;
		background-size: cover !important;
		background-position-x: center !important;
		background-position-y: center !important;
		background-attachment: fixed !important;
	}
}

@media (orientation: portrait) {
	body{ 
		background-image: url("<%=sessionHelper.getWallPaperPath(true)%>") !important;
		background-repeat: no-repeat !important;
		background-size: cover !important;
		background-position-x: center !important;
		background-position-y: center !important;
		background-attachment: fixed !important;
	}
}
.sidemenu-container { 
	background: none !important;
    border-bottom: none !important;
}
<%
	}	
%>
<%
	if ("full".equals(request.getParameter("fillscreen"))){
%>
.z-page {
	height:100%;
}
.zk-container{
	height:100%;
	padding:5px;
	width:100%;
}
body{
	#overflow:hidden;
}
<%
	}
%>

</style>
</head>

<body class="zkbi-body">
	<jsp:include page="sideMenu.jsp" flush="true" />
	<jsp:include page="embed_webgl.jsp" flush="true" />
	<div class="zk-container">
		<jsp:include page='<%=request.getParameter("zul")%>' flush="true" />
	</div>
	<div class="zkbi-editing-msg" style="display:none;position:fixed;top:0;right:0;">Editing Record</div>
	<!--
	<div style="height:50px;"></div>
	  -->
</body>
</html>
<%
} catch(Exception ex) {
	UniLog.log(ex);
	throw(ex);
}
%>
