<%@ page import="java.io.*, java.util.*, java.text.*, com.kyoko.common.*, com.uniinformation.webcore.*, com.uniinformation.jx.zk.*" %>
<%
	SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper(request, response);
	String versionTag = "?version=" + sessionHelper.getVersionId();
	
	ReturnMsg loginResult = sessionHelper.login(request, response, request.getParameter("login"), request.getParameter("password"));
	if (loginResult.getStatus()){
		sessionHelper.putSessionData("jxzkgadgetprovider", new JxZkGadgetProvider(sessionHelper));
		out.println("ok redirecting...");
		response.sendRedirect("custom_menu.html");
		out.flush();
		//request.getRequestDispatcher("custom_menu.html").forward(request, response);
	}
	else{
		out.println("login fail");
	}
%>