package com.uniinformation.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.Email;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.commons.mail.SimpleEmail;

import com.kyoko.common.ReturnMsg;
import com.sun.mail.util.MailSSLSocketFactory;

import org.apache.commons.mail.HtmlEmail;

public class EmailUtil {
	public enum SecMode { NONE, SSL, STARTTLS };
	public static ReturnMsg sendHtmlEmail(
			Pair<String,String> p_from, 
			List<Pair<String,String>> p_toList, 
			List<Pair<String,String>> p_bccList, 
			String p_subject, String p_htmlMsg, String p_txtMsg, List<EmailAttachment> p_attList, String p_host, int p_port, String p_loginId, String p_password, SecMode p_secMode , boolean p_sslValidate){
		return sendHtmlEmail(p_from, p_toList, null, p_bccList, p_subject, p_htmlMsg, p_txtMsg, p_attList, p_host, p_port, p_loginId, p_password, p_secMode, p_sslValidate);
	}
	public static ReturnMsg sendHtmlEmail(
			Pair<String,String> p_from, 
			List<Pair<String,String>> p_toList, 
			List<Pair<String,String>> p_ccList, 
			List<Pair<String,String>> p_bccList, 
			String p_subject, String p_htmlMsg, String p_txtMsg, List<EmailAttachment> p_attList, String p_host, int p_port, String p_loginId, String p_password, SecMode p_secMode , boolean p_sslValidate){
		return sendHtmlEmail(new HtmlEmail(), p_from, p_toList, p_ccList, p_bccList, p_subject, p_htmlMsg, p_txtMsg, p_attList, p_host, p_port, p_loginId, p_password, p_secMode, p_sslValidate);
	}
	public static ReturnMsg sendHtmlEmail(
			HtmlEmail email,
			Pair<String,String> p_from, 
			List<Pair<String,String>> p_toList, 
			List<Pair<String,String>> p_ccList, 
			List<Pair<String,String>> p_bccList, 
			String p_subject, String p_htmlMsg, String p_txtMsg, List<EmailAttachment> p_attList, String p_host, int p_port, String p_loginId, String p_password, SecMode p_secMode , boolean p_sslValidate){

		int okCnt = 0, failCnt = 0;
		try{
			if (p_from == null || StringUtils.isBlank(p_from.getLeft())){
				return(new ReturnMsg(false,"Invalid sender address"));
			}
			if ((p_toList == null || p_toList.size() == 0) && (p_ccList == null || p_ccList.size() == 0) && (p_bccList == null || p_bccList.size() == 0)){
				return(new ReturnMsg(false,"Invalid recipient address"));
			}
			if (StringUtils.isBlank(p_subject) && StringUtils.isBlank(p_htmlMsg) && StringUtils.isBlank(p_txtMsg)){
				return(new ReturnMsg(false,"Invalid email content"));
			}
			if (StringUtils.isBlank(p_host) || p_port <= 0){
				return(new ReturnMsg(false,"Invalid smtp host"));
			}
			
			//HtmlEmail email = new HtmlEmail();
			email.setCharset("UTF-8");
			//MultiPartEmail email = new MultiPartEmail();
			email.setHostName(p_host);
			email.setSmtpPort(p_port);
			
			if (StringUtils.isNotBlank(p_loginId) && p_password != null){
				email.setAuthenticator(new DefaultAuthenticator(p_loginId, p_password));
			}
			email.setSSLOnConnect(p_secMode == SecMode.SSL);
			if (p_secMode == SecMode.SSL) {
				email.setSslSmtpPort(""+p_port);
				//andrew200826: try to skip server cert validation, but does not work
				//email.getMailSession().getProperties().put("mail.debug",true);
				//email.getMailSession().getProperties().put("mail.smtp.ssl.trust", "*");
			    //MailSSLSocketFactory sf = new MailSSLSocketFactory();
			    //sf.setTrustAllHosts(true);
			    //email.getMailSession().getProperties().put("mail.smtp.ssl.socketFactory", sf);
				
				
				//andrew201104 skip server cert validation. need to update javax.mail v1.6.2
			}
			if (p_secMode == SecMode.STARTTLS) {
				email.isStartTLSRequired();
				email.setStartTLSEnabled(true);
			}
			if (!p_sslValidate) {
				email.getMailSession().getProperties().put("mail.smtp.ssl.trust", "*");
			}
			
			if (StringUtils.isNotBlank(p_from.getLeft()) && StringUtils.isNotBlank(p_from.getRight())){
				email.setFrom(p_from.getLeft(), p_from.getRight());
			}
			else{
				email.setFrom(p_from.getLeft());
			}

			for (Pair<String,String> to : ListUtil.emptyIfNull(p_toList)){
				try {
					if (StringUtils.isNotBlank(to.getLeft()) && StringUtils.isNotBlank(to.getRight())){
						email.addTo(to.getLeft(), to.getRight());
					}
					else if (StringUtils.isNotBlank(to.getLeft())){
						email.addTo(to.getLeft());
					}
					okCnt++;
				}
				catch (Exception e) {
					e.printStackTrace();
					failCnt++;
				}
			}

			for (Pair<String,String> cc : ListUtil.emptyIfNull(p_ccList)){
				try {
					if (StringUtils.isNotBlank(cc.getLeft()) && StringUtils.isNotBlank(cc.getRight())){
						email.addCc(cc.getLeft(), cc.getRight());
					}
					else if (StringUtils.isNotBlank(cc.getLeft())){
						email.addCc(cc.getLeft());
					}
					okCnt++;
				}
				catch (Exception e) {
					e.printStackTrace();
					failCnt++;
				}
			}

			for (Pair<String,String> bcc : ListUtil.emptyIfNull(p_bccList)){
				try {
					if (StringUtils.isNotBlank(bcc.getLeft()) && StringUtils.isNotBlank(bcc.getRight())){
						email.addBcc(bcc.getLeft(), bcc.getRight());
					}
					else if (StringUtils.isNotBlank(bcc.getLeft())){
						email.addBcc(bcc.getLeft());
					}
					okCnt++;
				}
				catch (Exception e) {
					e.printStackTrace();
					failCnt++;
				}
			}
			
			if ((email.getToAddresses() == null || email.getToAddresses().isEmpty()) 
					&& (email.getCcAddresses() == null || email.getCcAddresses().isEmpty())
					&& (email.getBccAddresses() == null || email.getBccAddresses().isEmpty())) {
				return new ReturnMsg(false, "No valid email address found");
			}
			
			email.setSubject(p_subject);
			if (StringUtils.isNotBlank(p_htmlMsg)){
				email.setHtmlMsg(p_htmlMsg);
			}
			if (StringUtils.isNotBlank(p_txtMsg)){
				email.setTextMsg(p_txtMsg);
			}
			else{
				email.setTextMsg("Your email client does not support HTML messages");
			}
			
			for (EmailAttachment att: ListUtil.emptyIfNull(p_attList)){
				email.attach(att);
			}
		    //UniLog.log1("prop:" + email.getMailSession().getProperties().toString());
			
			// send the email
			String result = email.send();
			ReturnMsg rtn = new ReturnMsg(true, result);
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("okCnt", okCnt);
			map.put("failCnt", failCnt);
			rtn.setData(map);
			return rtn;
		}
		catch(Exception ex){
			ex.printStackTrace();
			return(new ReturnMsg(false,ex));
		}
	}
	private static void selfTest1(){  //scp0318 without ssl - good
		//address
		Pair<String,String> from = Pair.of("andrew@hellovoice.com", "Andrew From 1");
		List<Pair<String,String>> toList = new ArrayList<Pair<String,String>>();
		toList.add(Pair.of("andrew@hellovoice.com", "Andrew To 1"));
		List<Pair<String,String>> bccList = null;
		
		//content
		String subject = "no sec test";
		String htmlMsg = "<html><body>Hello world<br>1111<br>222</body></html>";
		String txtMsg = null;
		
		EmailAttachment att1 = new EmailAttachment();
	    att1.setPath("/tmp/strangedot.png");
	    att1.setDisposition(EmailAttachment.ATTACHMENT);
	    List<EmailAttachment> attList = new ArrayList<EmailAttachment>();
	    attList.add(att1);
	    
		//connection
		String host = "scp0318.uniconn.com";
		int port = 25;  
		String loginId = null;
		String password = null;
		UniLog.logm(null, "result:" + sendHtmlEmail(from, toList, bccList, subject, htmlMsg, txtMsg, attList, host, port, loginId, password, SecMode.NONE, true));
	}
	private static void selfTest2(){ //gmail - good
		//address
		Pair<String,String> from = Pair.of("your email", "your name");
		List<Pair<String,String>> toList = new ArrayList<Pair<String,String>>();
		toList.add(Pair.of("target email", "target name"));
		List<Pair<String,String>> bccList = null;
		
		//content
		String subject = "ssl test";
		String htmlMsg = "<html><body>Hello world<br>1111<br>222</body></html>";
		String txtMsg = null;
		List<EmailAttachment> attList = null;
		
		//connection
		String host = "smtp.gmail.com";
		int port = 465;  
		String loginId = "your login";
		String password = "your password";
		UniLog.logm(null, "result:" + sendHtmlEmail(from, toList, bccList, subject, htmlMsg, txtMsg, attList, host, port, loginId, password, SecMode.SSL, true));
		
	}
	private static void selfTest3(){ //scp0318 with ssl (skip validation)
		//address
		Pair<String,String> from = Pair.of("andrew@hellovoice.com", "Andrew From 1");
		List<Pair<String,String>> toList = new ArrayList<Pair<String,String>>();
		toList.add(Pair.of("andrew@hellovoice.com", "Andrew To 1"));
		List<Pair<String,String>> bccList = null;
		
		//content
		String subject = "ssl novalidate";
		String htmlMsg = "<html><body>Hello world<br>1111<br>222</body></html>";
		String txtMsg = null;
		
		//connection
		String host = "scp0318.uniconn.com";
		int port = 29125;  
		String loginId = "";
		String password = "";
		UniLog.logm(null, "result:" + sendHtmlEmail(from, toList, bccList, subject, htmlMsg, txtMsg, null, host, port, loginId, password, SecMode.SSL, false));
	}
	private static void selfTest4(){ //office365 starttls test
		//address
		Pair<String,String> from = Pair.of("sales@wineac.com", "Wine AC");
		List<Pair<String,String>> toList = new ArrayList<Pair<String,String>>();
		toList.add(Pair.of("andrew@hellovoice.com", "Andrew To 1"));
		List<Pair<String,String>> bccList = null;
		
		//content
		String subject = "starttls test";
		String htmlMsg = "<html><body>Hello world<br>1111<br>222</body></html>";
		String txtMsg = null;
		
		//connection
		String host = "smtp.office365.com";
		int port = 587;  
		String loginId = "sales@wineac.com";
		String password = "!a1b2c3d4";
		UniLog.logm(null, "result:" + sendHtmlEmail(from, toList, bccList, subject, htmlMsg, txtMsg, null, host, port, loginId, password, SecMode.STARTTLS, true));
	}
	private static void selfTest5() {
		Pair<String,String> from = Pair.of("axa.exchange@pedderhealth.com", "AXA Exchange");
		List<Pair<String,String>> toList = new ArrayList<Pair<String,String>>();
		toList.add(Pair.of("donald@hellovoice.com", "donald@hellovoice.com"));
		toList.add(Pair.of("andrew@hellovoice.com", "andrew@hellovoice.com"));
		List<Pair<String,String>> bccList = null;
		
		//content
		String subject = "testing email subject";
		String htmlMsg = "<html><body>testing email context</body></html>";
		String txtMsg = null;
		List<EmailAttachment> attList = null;
		
		//connection
		String host = "smtp.gmail.com";
		int port = 465;  
		String loginId = "axa.exchange@pedderhealth.com";
		String password = "LyMvyDtMmu8xSkZ";
		UniLog.logm(null, "result:" + sendHtmlEmail(from, toList, bccList, subject, htmlMsg, txtMsg, attList, host, port, loginId, password, SecMode.SSL, true));
		
	}
	public static void main(String args[]) throws Exception{
		//selfTest1();
		//selfTest2();
		//selfTest3();
		//selfTest4();  //good
		//selfTest5();
	}
}
