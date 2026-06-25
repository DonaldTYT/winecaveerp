<%@ page import="java.util.*,java.io.*,org.apache.commons.codec.binary.Base64,com.uniinformation.webcore.*" %>
<%
response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
response.setHeader("Expires", "0"); // Proxies.

SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper(request, response);
InputStream is = null;
ByteArrayOutputStream bos = null;
javax.servlet.ServletOutputStream sos = null;
String zkPrintStreamKey = null;
String zkPrintMimeTypeKey = null;
boolean needKeepStream = false;
try {
	zkPrintStreamKey = request.getParameter("zk_print_stream_key");
	zkPrintMimeTypeKey = request.getParameter("zk_print_mimetype_key");
	String needKeepStreamStr = request.getParameter("keep_stream");
	if (needKeepStreamStr != null)
		needKeepStream = needKeepStreamStr.equals("Y");

	SessionHelper.SessionDataEx sd = (SessionHelper.SessionDataEx) sessionHelper.getSessionData(zkPrintMimeTypeKey);
	if (sd.isExpired())
		throw new Exception("Data expired");
	String mimeType = (String) sd.getData();

	sd = (SessionHelper.SessionDataEx) sessionHelper.getSessionData(zkPrintStreamKey);
	if (sd.isExpired())
		throw new Exception("Data expired");
	is = (InputStream) sd.getData();

	bos = new ByteArrayOutputStream();
	byte[] buf = new byte[8192];
	int readSize;
	while ((readSize = is.read(buf)) != -1)
		bos.write(buf, 0, readSize);

	//response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
	response.setContentType(mimeType);
	response.setContentLength(bos.size());
	sos = response.getOutputStream();
	bos.writeTo(sos);
	sos.flush();
	System.out.println("mimeType: " + mimeType + ",size:" + bos.size());
	out.clear();
} catch (Exception e) {
	e.printStackTrace();
	response.setContentType("text/html;charset=utf-8");
	out.print("<html><body>Application error: " + e.toString() + "</body></html>");
} finally {
	try {
		if (sos != null)
			sos.close();
		if (bos != null)
			bos.close();
		if (is != null) {
			is.close();
			sessionHelper.removeSessionData(zkPrintStreamKey);
			if (needKeepStream)
				sessionHelper.putSessionDataEx(zkPrintStreamKey, new ByteArrayInputStream(bos.toByteArray()), 
						new SessionHelper.SessionDataExCleanUpCallback());
		} else
			sessionHelper.removeSessionData(zkPrintStreamKey);
		sessionHelper.removeSessionData(zkPrintMimeTypeKey);
	} catch (Exception e) {
		e.printStackTrace();
	}
}
%>