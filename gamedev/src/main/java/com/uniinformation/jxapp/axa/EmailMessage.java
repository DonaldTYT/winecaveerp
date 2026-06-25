package com.uniinformation.jxapp.axa;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePart;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.mail.util.MimeMessageParser;
import org.apache.commons.mail.util.MimeMessageUtils;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Iframe;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.PageLoader;
import com.uniinformation.zkf.ZkForm;
public class EmailMessage extends JxZkBiBase {
	MimeMessage mimeMessage;
	MimeMessageParser parser;
	
	@Override
	public void afterBind() {
		super.afterBind();
		new JxFieldAction("btDownload") {
			@Override
			public void actionPerformed(JxField jxfield) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				try {
					FilingUtil.getFile(getSessionHelper().getAgent(), "emailfiling", getBr().getCellString("emm_key"), bos);
					byte content[] = bos.toByteArray();
					ByteArrayInputStream bis = new ByteArrayInputStream(content);
					Filedownload.save(bis, "message/rfc822", "mailcontent.eml");
				} catch (Exception ex) {
					UniLog.log(ex);
				}
				
			}
		};
		new JxFieldAction("btUpdHashtag") {

			@Override
			public void actionPerformed(JxField jxfield) {
				try {
					final ZkForm zkf1 = new ZkForm(null,"zkf/axa/UpdateHashtag.zul");
					zkf1.doModal(getBr().getCurrentCollection(),new EventListener() {

						@Override
						public void onEvent(Event arg0) throws Exception {
							// TODO Auto-generated method stub
							if(arg0.getTarget().getId().equals("btOK")) {
								zkf1.exitModal();					
								ReturnMsg rtnMsg = processUpdate(JxZkBiBase.AFTERADDUPDATE_ACTION_NONE);
								return;
							}
							if(arg0.getTarget().getId().equals("btCancel")) {
								zkf1.exitModal();					
								return;
							}
						}
					}
					);
				} catch (Exception ex) {
					UniLog.log(ex);
				}
				
			}
		};
	}

	
	private class MyMimeMessageParser extends MimeMessageParser {
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
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br,mode);
		if(!(mode == JxZkBiBase.MODE_ADD)) {
		try {
			String key = p_br.getCellString("emm_key");
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			FilingUtil.getFile(sessionHelper.getAgent(), "emailfiling", key, bos);
			byte content[] = bos.toByteArray();
			ByteArrayInputStream bis = new ByteArrayInputStream(content);
			mimeMessage = MimeMessageUtils.createMimeMessage(null, bis);
			parser = new MyMimeMessageParser(mimeMessage).parse();
			p_br.getCell("emm_to").set(parser.getTo().toString());
			p_br.getCell("emm_cc").set(parser.getCc().toString());
			p_br.getCell("emm_bcc").set(parser.getBcc().toString());
			String pageName = null;
			if(parser.hasHtmlContent()) {
				String htmlContent = parser.getHtmlContent();
//				FileUtils.writeStringToFile(new File("/tmp/ttt.html"), htmlContent);
				
				StringBuilder ifrmSrcB = new StringBuilder();
				ifrmSrcB.append(htmlContent);
				int offset = 0;
				Pattern pattern = Pattern.compile("<img .*src=\"([^\"]+)\"[^>]*>", Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(htmlContent);
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
				htmlContent = ifrmSrcB.toString();
				
				
				
				
				pageName = "eml:"+key;
				PageLoader.addPageX(getSessionHelper().getSessionKey(),getSessionHelper().getAgent(), null , null, pageName, htmlContent);
			} else if(parser.hasPlainContent()) {
				String plainContent = parser.getPlainContent();
				FileUtils.writeStringToFile(new File("/tmp/ttt.txt"), plainContent);
			}
			if(parser.hasAttachments()) {
			for (final DataSource ds : parser.getAttachmentList()) {
					InputStream is = ds.getInputStream();
			}
			}
			JxField jxf = jxAdd("ifrmContent");
			if(jxf != null) {
				Iframe ifr = (Iframe) jxf.getNativeObject();
				if(pageName != null) {
					ifr.setSrc("pageloader?pagename="+pageName);
				} else {
					ifr.setSrc("pageloader");
				}
			}
			if(p_br.getSessionHelper().hasAccessRight("#updemail")) {
				jxSetVisible("btUpdHashtag",true);
			} else {
				jxSetVisible("btUpdHashtag",false);
			}
			if(p_br.getSessionHelper().hasAccessRight("#expEmail")) {
				jxSetVisible("btDownload",true);
			} else {
				jxSetVisible("btDownload",false);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		}
	}
	
	@Override
	public void initForm(int p_mode) {
		super.initForm(p_mode);
		if(getBr() != null) {
			if(getBr().getRowCount() > 1) {
				jxSetVisible("btNext",true);
				jxSetVisible("btPrevious",true);
			}
		}
	}
	
}
