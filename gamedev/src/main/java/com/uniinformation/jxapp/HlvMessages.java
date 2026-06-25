package com.uniinformation.jxapp;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
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
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.commons.mail.util.MimeMessageUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.A;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Iframe;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Vlayout;

import com.kyoko.common.Sprintf;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;

public class HlvMessages extends JxZkBiBase {
	@Override
	public void afterBind() {
		super.afterBind();
	}
	
	@Override
	public void bindCellCollection(BiResult c,int mode) {
		super.bindCellCollection(c,mode);
		UniLog.log("HAHA in HlvMessage.bindCellCollection");
		String msgFile = loadMessage();
		UniLog.log("Message File = " + msgFile);
		if(msgFile != null) {
			try {
				InputStream is = erpFileInputStream(msgFile);
				UniLog.log("parse mail content and display here");
				displayEmailContent(is);
				is.close();
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
	}	
	
	String loadMessage()
	{
		RpcClient rpc = getRpcClient();
		Value v = null;
		v = rpc.callSegment("selectmessagegroup",
					new VectorUtil()
						.addElement( new Sprintf("MSG_%07d").add(getBr().getCell("vm_msgdpu_1").getString()).toString())
						.toVector()
					);
		if(v == null) { rpc.close(); return(null) ;};
		if(getBr().getCell("vm_type").getString().equals("4")) {
			v = rpc.callSegment("getmessagefile",
					new VectorUtil()
						.addElement(getBr().getCell("vm_msgid_1").getInt())
						.addElement("822")
						.toVector()
					);
			if(v == null) { rpc.close(); return(null) ;};
			return(v.toString());
		}
		return(null);
	}
	private void displayEmailContent(final InputStream inStream) throws Exception {
		Div divEmailContent = (Div) addWithoutCheck("emailcontent_div").getNativeObject();
		Div divHeader = (Div) divEmailContent.query("#divEcHeader");
		Iframe ifrmContent = (Iframe) divEmailContent.query("#ifrmEcContent");
		Vlayout vlAttachments = (Vlayout) divEmailContent.query("#vlEcAttachments");
		while (divHeader.getChildren().size() > 0)
			divHeader.getChildren().get(0).detach();
		while (vlAttachments.getChildren().size() > 0)
			vlAttachments.getChildren().get(0).detach();
		ifrmContent.setClientAttribute("sandbox", "allow-same-origin allow-popups allow-scripts");
		ifrmContent.setClientAttribute("security", "restricted");

		final MimeMessage mimeMessage = MimeMessageUtils.createMimeMessage(null, inStream);
		final MimeMessageParser parser = new MyMimeMessageParser(mimeMessage).parse();
		//header
		buildHeaderRow(divHeader, "From:", parser.getFrom());
		buildHeaderRow(divHeader, "To:", parser.getTo());
		buildHeaderRow(divHeader, "Cc:", parser.getCc());
		buildHeaderRow(divHeader, "Bcc:", parser.getBcc());
		buildHeaderRow(divHeader, "Date:", mimeMessage.getSentDate());
		buildHeaderRow(divHeader, "Subject:", parser.getSubject());
		//content
		String ifrmSrc;
		if (parser.hasHtmlContent())
			ifrmSrc = parser.getHtmlContent().replaceAll("\\r\\n|\\r|\\n", "");
		else if (parser.hasPlainContent())
			ifrmSrc = "<html><body>" + parser.getPlainContent().replaceAll("\\r\\n|\\r|\\n", "<br>") + "</body></html>";
		else
			ifrmSrc = "<html><body></body></html>";
		final StringBuilder ifrmSrcB = new StringBuilder(ifrmSrc);
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
		Clients.evalJavaScript(String.format("$(document).ready(function(){"
				+ "EmailContentUI.initContent(\"%s\");"
				+ "});", ifrmSrcB.toString().replace("\"", "\\\"")));
		//attach
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
					appendChild(new Label(){{
						setZclass("z-icon-paperclip");
					}});
					appendChild(a);
					appendChild(new Label("(" + ds.getContentType()));
					appendChild(new Label(", " + toFileSizeString(streamSize) + ")"));
				}});
			}
		}
		vlAttachments.appendChild(new Hlayout(){{
			InputStream is = mimeMessage.getInputStream();
			final int streamSize = is.available();
			final String name = "untitled.eml";
			final String contentType = "message/rfc822";
			is.close();
			A a = new A(name);
			a.addEventListener(Events.ON_CLICK, new EventListener<Event>(){
				@Override
				public void onEvent(Event event) throws Exception {
					InputStream is = mimeMessage.getInputStream();
					Filedownload.save(is, contentType, name);
					is.close();
				}
			});
			appendChild(new Label(){{
				setZclass("z-icon-paperclip");
			}});
			appendChild(a);
			appendChild(new Label("(" + contentType));
			appendChild(new Label(", " + toFileSizeString(streamSize) + ")"));
		}});
		divEmailContent.setVisible(true);
	}
	private <T> void buildHeaderRow(Div divHeader, String label, T value) {
		if (value == null || value instanceof String && StringUtils.isBlank((String) value))
			return;
		List<T> values = new ArrayList<T>();
		values.add(value);
		buildHeaderRow(divHeader, label, values);
	}
	private <T> void buildHeaderRow(Div divHeader, String label, List<T> values) {
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
