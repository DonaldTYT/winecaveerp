package com.uniinformation.jx.zk;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;
import javax.mail.internet.MimeUtility;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.commons.mail.util.MimeMessageUtils;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Label;
import org.zkoss.zul.Vlayout;

import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class JxZkEmailContent extends SelectorComposer<Div> {
	
	@Wire
	Div divHeader;
	@Wire
	Iframe ifrmContent;
	@Wire
	Vlayout vlAttachments;
	
	@Override
	public void doAfterCompose(final Div comp) throws Exception {
		super.doAfterCompose(comp);
    	final SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
    	if (sessionHelper == null || !sessionHelper.isLogin())
    		return;
    	ifrmContent.setClientAttribute("sandbox", "allow-same-origin");
    	ifrmContent.setClientAttribute("security", "restricted");
		final MimeMessage mimeMessage ;
		final long contentSize;
		final String name;
		if(Executions.getCurrent().getParameter("file") != null) {
			final File file = new File("C:\\tmp\\" + Executions.getCurrent().getParameter("file"));
			mimeMessage = MimeMessageUtils.createMimeMessage(null, file);
			contentSize = file.length();
			String nam = file.getName().replaceAll("\\.[^\\.]*$", ".eml");
			if (!nam.endsWith(".eml")) {
				name = nam+".eml";
			} else {
				name = nam;
			}
		} else if(Executions.getCurrent().getParameter("key") != null) {
			String key = Executions.getCurrent().getParameter("key");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			FilingUtil.getFile(sessionHelper.getAgent(), "emailfiling", key, bos);
			byte content[] = bos.toByteArray();
			contentSize = content.length;
			ByteArrayInputStream bis = new ByteArrayInputStream(content);
			mimeMessage = MimeMessageUtils.createMimeMessage(null, bis);
			name = "mailcontent.eml";
		} else {
			mimeMessage=null;
			contentSize = 0;
			name = null;
		}
		final MimeMessageParser parser = new MyMimeMessageParser(mimeMessage).parse();
		buildHeaderRow("From:", parser.getFrom());
		buildHeaderRow("To:", parser.getTo());
		buildHeaderRow("Cc:", parser.getCc());
		buildHeaderRow("Bcc:", parser.getBcc());
		buildHeaderRow("Date:", mimeMessage.getSentDate());
		buildHeaderRow("Subject:", parser.getSubject());
		UniLog.log("email content from:" + parser.getFrom() + "\nto:" + parser.getTo() + 
				"\ncc:" + parser.getCc() + "\nbcc:" + parser.getBcc() + "\ndate:" + mimeMessage.getSentDate() + "\nsubject:" + parser.getSubject());
		String ifrmSrc = null;
		if (parser.hasHtmlContent())
			ifrmSrc = parser.getHtmlContent().replaceAll("\\r\\n|\\r|\\n", "");
		else if (parser.hasPlainContent())
			ifrmSrc = "<html><body>" + parser.getPlainContent().replaceAll("\\r\\n|\\r|\\n", "<br>") + "</body></html>";
		final StringBuilder ifrmSrcB = new StringBuilder();
		if (ifrmSrc != null) {
			ifrmSrcB.append(ifrmSrc);
			int offset = 0;
			Pattern pattern = Pattern.compile("<img .*src=\"([^\"]+)\"[^>]*>", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(ifrmSrc);
			while (matcher.find()) {
				String findString = matcher.group(1);
				if (findString.toLowerCase().startsWith("cid:")) {
					String cid = findString.substring(4);
					DataSource ds = parser.findAttachmentByCid(cid);
					if (ds != null) {
						ByteArrayOutputStream outStream = new ByteArrayOutputStream();
						readDataSource(ds, outStream);
						outStream.close();
						String toString = String.format("data:%s;base64,%s", ds.getContentType().trim(), Base64.encodeBase64String(outStream.toByteArray()));
						int start = matcher.start(1) + offset;
						int end = matcher.end(1) + offset;
						offset += toString.length() - findString.length();
						ifrmSrcB.replace(start, end, toString);
					}
				}
			}
			String ss = 
					String.format("$(document).ready(function(){"
					+ "initContent(\"%s\");"
					+ "});", ifrmSrcB.toString().replace("\"", "\\\""));
			Clients.evalJavaScript(
					String.format("$(document).ready(function(){"
					+ "initContent(\"%s\");"
					+ "});", ifrmSrcB.toString().replace("\"", "\\\""))
					);
		}
		if (parser.hasAttachments()) {
			for (final DataSource ds : parser.getAttachmentList()) {
				vlAttachments.appendChild(new Hlayout(){{
					InputStream is = ds.getInputStream();
					int streamSize = is.available();
					is.close();
					final String dsName = StringUtils.defaultString(decodeMineText(ds.getName()), "untitled");
					A a = new A(dsName);
					a.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
						@Override
						public void onEvent(Event event) throws Exception {
							InputStream is = ds.getInputStream();
							Filedownload.save(is, ds.getContentType(), dsName);
							is.close();
						}
					});
					appendChild(a);
					appendChild(new Label(", " + ds.getContentType()));
					appendChild(new Label(", " + toFileSizeString(streamSize)));
				}});
			}
		}
		vlAttachments.appendChild(new Hlayout(){{
			String ext = ".eml";
//			String nam = file.getName().replaceAll("\\.[^\\.]*$", ext);
//			if (!nam.endsWith(ext))
//				nam += ext;
//			final long streamSize = file.length();
//			final String name = nam;
			final String contentType = "message/rfc822";
			A a = new A(name);
			a.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
				@Override
				public void onEvent(Event event) throws Exception {
					if(Executions.getCurrent().getParameter("file") != null) {
						File file = new File("C:\\tmp\\" + Executions.getCurrent().getParameter("file"));
						InputStream is = new FileInputStream(file);
						Filedownload.save(is, contentType, name);
					}
				}
			});
			appendChild(a);
			appendChild(new Label(", " + contentType));
			appendChild(new Label(", " + toFileSizeString(contentSize)));
		}});
	}
	private <T> void buildHeaderRow(String label, T value) {
		if (value == null || value instanceof String && StringUtils.isBlank((String) value))
			return;
		List<T> values = new ArrayList<T>();
		values.add(value);
		buildHeaderRow(label, values);
	}
	private <T> void buildHeaderRow(String label, List<T> values) {
		if (values.isEmpty())
			return;
		Div divRow = new Div();
		Div divCell = new Div();
		Div divCell1 = new Div();
		Vlayout vl = new Vlayout();
		divRow.setStyle("display:table-row;");
		divCell.setStyle("display:table-cell;padding-right:10px;white-space:nowrap;vertical-align:top;");
		divCell1.setStyle("display:table-cell;");
		divCell.appendChild(new Label(label));
		for (T t : values)
			vl.appendChild(new Label(decodeMineText(t.toString())));
		divCell1.appendChild(vl);
		divRow.appendChild(divCell);
		divRow.appendChild(divCell1);
		divHeader.appendChild(divRow);
	}
	private static String decodeMineText(String str) {
		try {
			str = MimeUtility.decodeText(str);
		} catch (UnsupportedEncodingException e) {
			UniLog.log("decode string fail " + e.toString());
		}
		return str;
	}
	private static void readDataSource(DataSource ds, OutputStream outStream) {
		InputStream inStream = null;
		try {
			inStream = ds.getInputStream();
			byte[] buffer = new byte[4096];
			int readBytes;
			while ((readBytes = inStream.read(buffer)) != -1)
				outStream.write(buffer, 0, readBytes);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (inStream != null) {
				try {
					inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private static String toFileSizeString(long size) {
		if (size < 1000)
			return String.format("%dByte", size);
		if (size < 1024 * 1000)
			return String.format("%.2fKB", size / 1024d);
		if (size < 1024 * 1024 * 1000)
			return String.format("%.2fMB", size / 1024d / 1024d);
		return String.format("%.2fGB", size / 1024d / 1024d / 1024d);
	}
	public static class MyMimeMessageParser extends MimeMessageParser {
		private String plainContent;
		private String htmlContent;
		public MyMimeMessageParser(MimeMessage message) {
			super(message);
		}
		@Override
		public String getPlainContent() {
	        return plainContent;
	    }
		@Override
		public boolean hasPlainContent() {
	        return plainContent != null;
	    }
		@Override
		public String getHtmlContent() {
			return htmlContent;
		}
		@Override
		public boolean hasHtmlContent() {
			return htmlContent != null;
		}
		@Override
		protected void parse(final Multipart parent, final MimePart part) throws MessagingException, IOException {
			super.parse(parent, part);
			if (htmlContent == null && super.getHtmlContent() != null 
					&& isMimeType(part, "text/html") && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()))
				htmlContent = StringUtils.defaultString(parseText(part), super.getHtmlContent());
			if (plainContent == null && super.getPlainContent() != null 
					&& isMimeType(part, "text/plain") && !Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()))
				plainContent = StringUtils.defaultString(parseText(part), super.getPlainContent());
		}
		private String parseText(MimePart part) throws MessagingException, IOException {
			Pattern pattern = Pattern.compile("charset=[\"']?(\\w+)[\"';\\s]?", Pattern.CASE_INSENSITIVE);
			Matcher matcher = pattern.matcher(part.getContentType());
			if (matcher.find() && matcher.group(1).equalsIgnoreCase("gb2312")) {
				InputStream in = part.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(in, "gbk"));
				StringBuilder sb = new StringBuilder();
				String str;
				while ((str = br.readLine()) != null)
					sb.append(str);
				br.close();
				in.close();
				UniLog.log("replace part");
				return sb.toString();
			}
			return null;
		}
		private boolean isMimeType(final MimePart part, final String mimeType) {
			try {
				Method method = getClass().getSuperclass().getDeclaredMethod("isMimeType", MimePart.class, String.class);
				method.setAccessible(true);
				return (Boolean) method.invoke(this, part, mimeType);
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			return false;
		}
	}
}
