package com.uniinformation.axa;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.CloseUtil;
import com.uniinformation.utils.EmailUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZipUtil;
import com.uniinformation.utils.EmailUtil.SecMode;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.IniHelper;
import com.uniinformation.webcore.SessionHelper;

public class AxaUtilEx {
	public static ReturnMsg sendEmailEx(String p_subject, String p_datFileName, byte[] p_dat,String p_emailAddrs) {
		UniLog.log1("In AxaUtilEx emailAddrs : [%s]",p_emailAddrs);
		UniLog.log1("subject:%s datFileName:%s size:%d", p_subject, p_datFileName, p_dat == null ? 0: p_dat.length); 
		File zipFile = null;
		FileOutputStream zfos = null;
		IniHelper ini = SessionHelper.getIniHelper();
		if (ini == null) {
			UniLog.log1("ini error");
			return ReturnMsg.defaultFail;
		}
		try {
			zipFile = File.createTempFile("zipatt", ".tmp", new File("/tmp"));
			zfos = new FileOutputStream(zipFile);
			
			//connection
			String axaZipPass = ini.getString("axaZipPass","85ejd34");
			String axaSMTPLogin = ini.getString("axaSMTPLogin","axa.exchange@pedderhealth.com");
//			String axaSMTPPass = ini.getString("axaSMTPPass","LyMvyDtMmu8xSkZ");
			String axaSMTPPass = ini.getString("axaSMTPPass","jcku fmuu hmnn iixo");
			String axaSMTPHost = ini.getString("axaSMTPHost","smtp.gmail.com");
			int axaSMTPPort = ini.getInteger("axaSMTPPort",465);
			//String axaToAddr = ini.getString("axaToAddr","healthclaims@axa.com.hk");  //for UAT  / production. It can be config by ini
			String axaToAddr = ini.getString("axaToAddr","andrew@hellovoice.com,donald@hellovoice.com");
			if(!StringUtils.isBlank(p_emailAddrs)) {
				axaToAddr = p_emailAddrs;
			}
			String[] axaToAddrArr = StringUtils.split(axaToAddr,",");
			String axaFromAddr = ini.getString("axaFromAddr","axa.exchange@pedderhealth.com");
			String axaFromAddrName = ini.getString("axaFromAddrName","AXA Exchange");
			
			ZipUtil.createZip(axaZipPass, false, zfos, p_dat, p_datFileName);
			List<EmailAttachment> attList = new ArrayList<EmailAttachment>();
			EmailAttachment att1 = new EmailAttachment();
			att1.setPath(zipFile.getAbsolutePath());
			att1.setName(FilenameUtils.getBaseName(p_datFileName)+".zip");
			att1.setDisposition(EmailAttachment.ATTACHMENT);
			attList.add(att1);

			Pair<String,String> from = Pair.of(axaFromAddr,axaFromAddrName);
			List<Pair<String,String>> toList = new ArrayList<Pair<String,String>>();
			
			for (String toAddr : axaToAddrArr) {
				toList.add(Pair.of(toAddr, toAddr));
			}
			

			return EmailUtil.sendHtmlEmail(from, toList, null, null, p_subject, "<html><body></body></html>", null, attList, axaSMTPHost, axaSMTPPort, axaSMTPLogin, axaSMTPPass, SecMode.SSL, true);
		}
		catch(Exception ex) {
			return new ReturnMsg(ex);
		}
		finally {
			CloseUtil.close(zfos);
			CloseUtil.delete(zipFile);
		}
	}

	static int MAX_EMAIL_PERRUN = 300;
	static String LABEL_POLICY_NO = "Policy No";
	static String PATTERN_POLICY_NO = "[0-9]{8}GH";
	static String DELIMITER_POLICY_NO = ":";
	static String cutValueFromLabeledText(String p_text,String p_label,String p_delimiter) {
		int idx = p_text.indexOf(p_label);
		if( idx < 0 ) return(null);
		String ss = p_text.substring(idx + LABEL_POLICY_NO.length());
		if(StringUtils.isEmpty(ss)) return(null);
		if(p_delimiter != null) {
			idx = ss.indexOf(p_delimiter);
			if(idx >= 0) {
				ss = ss.substring(idx+DELIMITER_POLICY_NO.length());
			}
		}
		if(StringUtils.isEmpty(ss)) return(null);
		return(ss.trim());
	}
	
//	static private void searchHashTagFromHtml2(String html,HashSet<String> hashTag) {
//		Document doc = Jsoup.parse(html);
//		UniLog.log("HAHA Jsoup " + doc.text());
//		Elements els = doc.getElementsMatchingOwnText(LABEL_POLICY_NO);
//		for(Element el : els ) {
//			UniLog.log("matched text = "+el.ownText());
//			if(el.tagName().equals("div")) {
//				UniLog.log("policy div " + " located ["+el.text()+"]");
//				String ss = cutValueFromLabeledText(el.text(),LABEL_POLICY_NO,DELIMITER_POLICY_NO);
//				if(ss != null) {
//					hashTag.add(ss);
//				}
//				/*
//				String ss = el.text();
//				int sidx = ss.indexOf(":");
//				if(sidx > 0) {
//					ss = ss.substring(sidx + 1).trim();
//					hashTag.add(ss);
//				}
//				*/
//			} else 
//			if(el.tagName().equals("span")) {
//				String ss = cutValueFromLabeledText(el.text(),LABEL_POLICY_NO,DELIMITER_POLICY_NO);
//				if(ss != null) {
//					hashTag.add(ss);
//				}
//				Element tbody =null;
//				for(Element pel = el.parent();pel != null; pel = pel.parent()) {
//					if(pel.tagName().equals("tbody")) {
//						tbody  = pel;
//						break;
//					}
//				}
//				if(tbody  != null) {
//					UniLog.log("Table for policy found");
//					Elements trs = tbody.getElementsByTag("tr");
//					if(trs != null) {
//						UniLog.log("trs size = " + trs.size());
//						int policyCol = -1;
//						if(trs.size() > 1) {
//							Element tr = trs.get(0);
//							Elements tds = tr.getElementsByTag("td");
//							if(tds != null) {
//								for(int i=0;i<tds.size();i++) {
//									Elements policynoTds = tds.get(i).getElementsMatchingOwnText(LABEL_POLICY_NO);
//									if(policynoTds != null && policynoTds.size() > 0) {
//										UniLog.log("policy column located at " + i);
//										policyCol = i;
//										break;
//									}
//								}
//							}
//						}
//						if(policyCol >= 0) {
//							for(int i=1;i<trs.size();i++) {
//								Element tr = trs.get(i);
//								Elements tds = tr.getElementsByTag("td");
//								if(tds.size() > policyCol) {
//									Element td = tds.get(policyCol);
//									Elements span = td.getElementsByTag("span");
//									if(span != null) {
//										UniLog.log("policy cell " + i + " located ["+span.text()+"]");
//										hashTag.add(span.text().trim());
//									}
//								}
//							}
//						}
//					}
//				}
//            } else {
//				UniLog.log("policy in tag( " + el.tagName() + ") " + " located ["+el.text()+"]");
//				String ss = cutValueFromLabeledText(el.text(),LABEL_POLICY_NO,DELIMITER_POLICY_NO);
//				if(ss != null) {
//					hashTag.add(ss);
//				}
//            }
//		}
//	}
//	static private void searchHashTagFromHtml1(String html,HashSet<String> hashTag) {
//		Document doc = Jsoup.parse(html);
//		UniLog.log("HAHA Jsoup " + doc.text());
//		//Elements els = doc.getElementsMatchingOwnText(LABEL_POLICY_NO);
//		Elements els = doc.getElementsContainingOwnText(LABEL_POLICY_NO);
//		for(Element el : els ) {
//			UniLog.log("matched text = "+el.ownText());
//			if(el.tagName().equals("div")) {
//				UniLog.log("policy div " + " located ["+el.text()+"]");
//				String ss = cutValueFromLabeledText(el.text(),LABEL_POLICY_NO,DELIMITER_POLICY_NO);
//				if(ss != null) {
//					hashTag.add(ss);
//				}
//				/*
//				String ss = el.text();
//				int sidx = ss.indexOf(":");
//				if(sidx > 0) {
//					ss = ss.substring(sidx + 1).trim();
//					hashTag.add(ss);
//				}
//				*/
//			} else 
//			if(el.tagName().equals("span")) {
//				String ss = cutValueFromLabeledText(el.text(),LABEL_POLICY_NO,DELIMITER_POLICY_NO);
//				if(ss != null) {
//					hashTag.add(ss);
//				}
//				Element tbody =null;
//				for(Element pel = el.parent();pel != null; pel = pel.parent()) {
//					if(pel.tagName().equals("tbody")) {
//						tbody  = pel;
//						break;
//					}
//				}
//				if(tbody  != null) {
//					UniLog.log("Table for policy found");
//					Elements trs = tbody.getElementsByTag("tr");
//					if(trs != null) {
//						UniLog.log("trs size = " + trs.size());
//						int policyCol = -1;
//						if(trs.size() > 1) {
//							Element tr = trs.get(0);
//							Elements tds = tr.getElementsByTag("td");
//							if(tds != null) {
//								for(int i=0;i<tds.size();i++) {
//									//Elements policynoTds = tds.get(i).getElementsMatchingOwnText(LABEL_POLICY_NO);
//									Elements policynoTds = tds.get(i).getElementsContainingOwnText(LABEL_POLICY_NO);
//									if(policynoTds != null && policynoTds.size() > 0) {
//										UniLog.log("policy column located at " + i);
//										policyCol = i;
//										break;
//									}
//								}
//							}
//						}
//						if(policyCol >= 0) {
//							for(int i=1;i<trs.size();i++) {
//								Element tr = trs.get(i);
//								Elements tds = tr.getElementsByTag("td");
//								if(tds.size() > policyCol) {
//									Element td = tds.get(policyCol);
//									Elements span = td.getElementsByTag("span");
//									if(span != null) {
//										UniLog.log("policy cell " + i + " located ["+span.text()+"]");
//										hashTag.add(span.text().trim());
//									}
//								}
//							}
//						}
//					}
//				}
//            } else {
//				UniLog.log("policy in tag( " + el.tagName() + ") " + " located ["+el.text()+"]");
//				String ss = cutValueFromLabeledText(el.text(),LABEL_POLICY_NO,DELIMITER_POLICY_NO);
//				if(ss != null) {
//					hashTag.add(ss);
//				}
//            }
//		}
//		
//	}

	static int addMatchedPattenToHashTag(Pattern p,String p_str,HashSet<String> hashTag) {
		int matchCnt = 0;
		Matcher matcher = p.matcher(p_str);
		while (matcher.find()) {
			String ms = p_str.substring(matcher.start(),matcher.end());
			hashTag.add(ms);
			matchCnt++;
		}
		/*
		for(String ss=p_str;;) {
			Matcher m = p.matcher(ss);
			if(m.matches()) {
				String ms = m.group(1);
				hashTag.add(ms);
				ss = ss.substring(ss.indexOf(ms+ms.length()));
				matchCnt++;
			} else break;
		}
		*/
		return(matchCnt);
	}
	
	static private void searchHashTagFromHtml(String html,HashSet<String> hashTag) {
		Document doc = Jsoup.parse(html);
//		Elements els = doc.getElementsMatchingOwnText(LABEL_POLICY_NO);
//		Pattern p = Pattern.compile(".*[^0-9]*([0-9]+GH)");
		Pattern p = Pattern.compile(PATTERN_POLICY_NO );
//		Pattern p = Pattern.compile(".*[^0-9]*([0-9]+GH)");
		Elements els = doc.getElementsContainingOwnText(LABEL_POLICY_NO);
		for(Element el : els ) {
			UniLog.log("matched text = "+el.ownText());
			/*
				String ss = cutValueFromLabeledText(el.text(),LABEL_POLICY_NO,DELIMITER_POLICY_NO);
				if(ss != null) {
					hashTag.add(ss);
				}
				*/
				int matchCnt = addMatchedPattenToHashTag(p,el.text(),hashTag);
				/*
				String ss = null;
				{
					Matcher m = p.matcher(el.text());
					if(m.matches()) {
						hashTag.add(m.group(1));
						ss = m.group(1);
					}
				}
				*/
				Element tbody =null;
				Element htr = null;
				Element htd = null;
				for(Element pel = el.parent();pel != null; pel = pel.parent()) {
					if(pel.tagName().equals("td")) {
						htd = pel;
					}
					if(pel.tagName().equals("tr")) {
						htr = pel;
					}
					if(pel.tagName().equals("tbody")) {
						tbody  = pel;
						break;
					}
				}
				if(tbody  != null && htd != null && htr != null) {
					UniLog.log("Table for policy found");
					Elements trs = tbody.getElementsByTag("tr");
					if(trs != null) {
						Elements tds = htr.getElementsByTag("td");
						int rowIdx = trs.indexOf(htr);
						int colIdx = tds.indexOf(htd);
						UniLog.log("trs size = " + trs.size());
						for(int i=rowIdx+1;i<trs.size();i++) {
							Element tr = trs.get(i);
							tds = tr.getElementsByTag("td");
							if(tds.size() > colIdx) {
								Element td = tds.get(colIdx);
								/*
								Elements span = td.getElementsByTag("span");
								if(span != null) {
									UniLog.log("policy cell " + i + " located ["+span.text()+"]");
									hashTag.add(span.text().trim());
								}
								*/
								matchCnt += addMatchedPattenToHashTag(p,td.text(),hashTag);
								/*
								{
									Matcher m = p.matcher(td.text());
									if(m.matches()) {
										hashTag.add(m.group(1));
									}
								}
								*/
							}
						}
					}
				} else {
					// Policy No Label found but no phr found within the found tag and the tag is not in a td , try matching patten <tag> <tag>Policy No</tag> nnnnnnnnPH </tag>
					if(matchCnt <= 0) {
					for(Element pel = el.parent();pel != null; pel = pel.parent()) {
						Elements elss = pel.getElementsMatchingOwnText(PATTERN_POLICY_NO);
						if(elss != null && elss.size() > 0) {
							for(int n = 0;n<elss.size();n++) {
								matchCnt += addMatchedPattenToHashTag(p,elss.get(n).text(),hashTag);
								/*
								Matcher m = p.matcher(elss.get(n).text().trim());
								if(m.matches()) {
									hashTag.add(m.group(1));
								}
								*/
							}
							break;
						}
					}
					}
				}
		}
	}
	static private void searchHashTagFromString(String content,HashSet<String> hashTag) {
		Pattern p = Pattern.compile(PATTERN_POLICY_NO );
		addMatchedPattenToHashTag(p,content,hashTag);
	}
	static private void searchHashTagFromMultipart(MimeMultipart multi,HashSet<String> hashTag)
	{
		try {
				int parts = multi.getCount();
				for (int j=0; j < parts; ++j) {
					MimeBodyPart part = (MimeBodyPart)multi.getBodyPart(j);
					if (part.getContent() instanceof MimeMultipart) {
						searchHashTagFromMultipart((MimeMultipart) part.getContent(),hashTag);
						return;
					}
					else {
						if (part.isMimeType("text/html")) {
							{
//								InputStream is = part.getInputStream();
								/*
								byte xx[] = new byte[65536];
								int cnt = 0;
								for (;;) {
									int cc = is.read(xx);
									if( cc <= 0) break;
									cnt += cc;
								}
								UniLog.log("bytes of contents = " + cnt);
								*/
//								byte[] content = IOUtils.toByteArray(part.getInputStream());
//								UniLog.log("bytes of contents = " + content.length);
							}
							String ss = part.getContent().toString();
							searchHashTagFromHtml( ss,hashTag);
//							searchHashTagFromHtml( IOUtils.toByteArray(part.getInputStream()).toString(),hashTag);
						}
						else if (part.isMimeType("text/plain")) {
							String content = new String(IOUtils.toByteArray(part.getInputStream()));
//							searchHashTagFromString( IOUtils.toByteArray(part.getInputStream()).toString(),hashTag);
							searchHashTagFromString( content,hashTag);
						}
						else {
						}
					}
				}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	
	static public int MAXEMAILSIZE = 16777216; // 16MB
	public static ReturnMsg getEmailNotice_real(BiResult p_br,String p_host, String p_login, String p_passwd, boolean p_delEmailFlag, int p_maxEmailCnt,boolean fDebug) throws Exception{
		ArrayList<Map<String,Object>> attList = new ArrayList<Map<String,Object>>();
		Folder folder = null;
		Folder trashFolder = null;
		Store store = null;
		String host = p_host;
		String login = p_login;
		String passwd = p_passwd;
		boolean fGmail = false;
		try {
			Properties props = System.getProperties();
			props.setProperty("mail.store.protocol", "imaps");
			props.put("mail.imaps.fetchsize", "1000000");  //much improve download speed

			Session session = Session.getDefaultInstance(props, null);
			if (fDebug) {
				session.setDebug(true);
			}
			store = session.getStore("imaps");
			store.connect(host,login,passwd);
			
			folder = store.getFolder("Inbox");
			folder.open(Folder.READ_WRITE);
			
			if (StringUtils.containsAny(host, "gmail.com")) {
				fGmail = true;
			}
			
			if (fGmail) {
				trashFolder = store.getFolder("[Gmail]/Trash");
				trashFolder.open(Folder.READ_WRITE);
			}
			
			UniLog.log1("Poll EmailNotice total:%d new:%d", folder.getMessageCount(), folder.getUnreadMessageCount());
			Message messages[] = folder.getMessages();
			for(Message msg : messages) {
				Date stime = msg.getSentDate();
				String subject = msg.getSubject();
				UniLog.log1("msg ct " + stime.toString() + " size " + msg.getSize() + " subject[" + subject+"]");
				if(msg instanceof MimeMessage) {
					MimeMessage mmsg = (MimeMessage) msg;
					if(mmsg.getSize() > MAXEMAILSIZE) {
						UniLog.log1("msg size " + mmsg.getSize() + " > " + MAXEMAILSIZE + " skiped");
					} else {
						String md5 = null; 
						try { 

							
							
//							byte content[] = IOUtils.toByteArray(mmsg.getInputStream());
//							byte content[] = IOUtils.toByteArray(mmsg.getRawInputStream());
//							byte content[] = IOUtils.toByteArray(msg.getInputStream());
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							msg.writeTo(bos);
							byte content[] = bos.toByteArray();
							MessageDigest md = MessageDigest.getInstance("MD5"); 
							byte[] hash = md.digest(content);
							StringBuilder sb = new StringBuilder(2*hash.length); 
							for(byte b : hash) { 
								sb.append(String.format("%02x", b&0xff)); 
							} 
							md5 = sb.toString(); 
							p_br.clear();
							p_br.clearCondition();
							p_br.addCustomCondition("emm_key='"+md5+"'");
							p_br.query();
							if(p_br.getRecordCount() > 0) {
								UniLog.log1("mime msg ct " + stime.toString() + " size " + msg.getSize() + " md5 " + md5 + " already exist, skipped");
							} else {

								boolean storeToFiling = false;
								boolean deleteFromFolder = false;
								boolean moveToSaved = false;
								
								Object multipart = msg.getContent();
								HashSet<String> hashtag = new HashSet<String>();
						
								if(mmsg.getSender().toString().contains("network.provider@axa.com.hk") ||
								   mmsg.getSender().toString().contains("donald@integratec.net")) {
									storeToFiling = true;
									if(msg.isMimeType("multipart/*")) {
										searchHashTagFromMultipart((MimeMultipart) multipart,hashtag);
									}
									/*
									if(multipart instanceof Multipart) {
										searchHashTagFromContent((Multipart) multipart,hashtag);
									}
									*/
								}
								
								if(storeToFiling) {
									p_br.clearCurrentRec();
									p_br.getCell("emm_key").set(md5);
									p_br.getCell("emm_sendtime").set(mmsg.getSentDate());
									p_br.getCell("emm_recvtime").set(mmsg.getReceivedDate());
									p_br.getCell("emm_fromaddr").set(mmsg.getSender().toString());
									p_br.getCell("emm_subject").set(mmsg.getSubject().toString());
									if(!hashtag.isEmpty()) {
										UniLog.log("Policy Nos grapped [" + hashtag.toString());
										p_br.getCell("emm_hashtag").set(hashtag.toString());
									}
									p_br.addCurrent();
									ByteArrayInputStream bis = new ByteArrayInputStream(content);
									FilingUtil.storeFile(p_br.getSessionHelper().getAgent(), "emailfiling", md5, p_host+":"+p_login, subject, bis,stime) ;
								}
							}
						} catch (Exception ex) {
							UniLog.log(ex);
						}
						UniLog.log1("mime msg ct " + stime.toString() + " size " + msg.getSize() + " md5 " + md5 + " subject[" + subject+"]");
					}
				} else {
					UniLog.log1("msg is not mime st " + stime.toString() + " size " + msg.getSize() + " subject[" + subject+"]");
				}
			}
			
			return (ReturnMsg.defaultOk);
		}
		/*
		catch(Exception ex) {
			ex.printStackTrace();
			return attList;
		}
		*/
		finally {
			if (folder != null) { try {folder.close(true); }catch(Exception ex) {} }
			if (trashFolder != null) { try {trashFolder.close(true); }catch(Exception ex) {} }
			if (store != null) { try {store.close(); } catch(Exception ex) {} }
		}
	}
	public static ReturnMsg getEmailNotice(BiResult p_br,String p_host, String p_login, String p_passwd, boolean p_delEmailFlag, int p_maxEmailCnt,boolean fDebug) throws Exception{
//		ArrayList<Map<String,Object>> attList = new ArrayList<Map<String,Object>>();
		Folder folder = null;
		Folder trashFolder = null;
		Folder filedFolder = null;
		Folder unknownFolder = null;
		Store store = null;
		String host = p_host;
		String login = p_login;
		String passwd = p_passwd;
		boolean fGmail = false;
		try {
			Properties props = System.getProperties();
			props.setProperty("mail.store.protocol", "imaps");
			props.put("mail.imaps.fetchsize", "1000000");  //much improve download speed

			Session session = Session.getDefaultInstance(props, null);
			if (fDebug) {
				session.setDebug(true);
			}
			store = session.getStore("imaps");
			store.connect(host,login,passwd);
			folder = store.getFolder("Inbox");
//			folder = store.getFolder("INBOX/Import From Fiona");
//			folder = store.getFolder("TempFolder1");
			folder.open(Folder.READ_WRITE);
			filedFolder = store.getFolder("Filed To Claim System");
			unknownFolder = store.getFolder("Unknown Sender");
			
			if (StringUtils.containsAny(host, "gmail.com")) {
				fGmail = true;
			}
			if (fGmail) {
				trashFolder = store.getFolder("[Gmail]/Trash");
				trashFolder.open(Folder.READ_WRITE);
			}
			UniLog.log("folder has " + folder.getMessageCount() + " messages");
			int msgCnt = folder.getMessageCount() ;
			
			if( msgCnt > MAX_EMAIL_PERRUN) msgCnt = MAX_EMAIL_PERRUN;
			Message messages[] = folder.getMessages();
			ArrayList<Message> filedList = new ArrayList<Message>();
			ArrayList<Message> unknownList = new ArrayList<Message>();
			for(int n=0;n <msgCnt;n++) {
				Message msg = messages[n];
				Date stime = msg.getSentDate();
				String subject = msg.getSubject();
				UniLog.log1("msg ct " + stime.toString() + " size " + msg.getSize() + " subject[" + subject+"]");
				boolean storeToFiling = false;
				boolean moveToOther = false;
				boolean moveToSaved = false;
				if(msg instanceof MimeMessage) {
					MimeMessage mmsg = (MimeMessage) msg;
					if(mmsg.getSize() > MAXEMAILSIZE) {
						UniLog.log1("msg size " + mmsg.getSize() + " > " + MAXEMAILSIZE + " skiped");
						moveToOther = true;
					} else {
						String md5 = null; 
						try { 
//							byte content[] = IOUtils.toByteArray(mmsg.getInputStream());
//							byte content[] = IOUtils.toByteArray(mmsg.getRawInputStream());
//							byte content[] = IOUtils.toByteArray(msg.getInputStream());
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							msg.writeTo(bos);
							byte content[] = bos.toByteArray();
							MessageDigest md = MessageDigest.getInstance("MD5"); 
							byte[] hash = md.digest(content);
							StringBuilder sb = new StringBuilder(2*hash.length); 
							for(byte b : hash) { 
								sb.append(String.format("%02x", b&0xff)); 
							} 
							md5 = sb.toString(); 
							p_br.clear();
							p_br.clearCondition();
							p_br.addCustomCondition("emm_key='"+md5+"'");
							p_br.query();
							if(p_br.getRecordCount() > 0) {
								UniLog.log1("mime msg ct " + stime.toString() + " size " + msg.getSize() + " md5 " + md5 + " already exist, skipped");
								moveToSaved = true;
							} else {
								Address fromList[] = mmsg.getFrom();
								String fromAddr = "";
								for(int i = 0;i < fromList.length;i++) {
									fromAddr += fromList[i].toString() + " ";
								}
								if(
								   fromAddr.contains("network.provider@axa.com.hk") ||
								   fromAddr.contains("fionali@pedderhealth.com") ||
								   fromAddr.contains("donald@integratec.net")) {
									storeToFiling = true;
								} else {
									moveToOther = true;
								}
								
								if(storeToFiling) {
									Object multipart = msg.getContent();
									HashSet<String> hashtag = new HashSet<String>();
//									{
//										Pattern p = Pattern.compile(PATTERN_POLICY_NO );
//										addMatchedPattenToHashTag(p, mmsg.getSubject().toString() ,hashtag) ;
//									}
									searchHashTagFromString(mmsg.getSubject().toString() , hashtag);
									if(msg.isMimeType("multipart/*")) {
										searchHashTagFromMultipart((MimeMultipart) multipart,hashtag);
									}
									/*
									if(multipart instanceof Multipart) {
										searchHashTagFromContent((Multipart) multipart,hashtag);
									}
									*/
									p_br.clearCurrentRec();
									p_br.getCell("emm_key").set(md5);
									p_br.getCell("emm_sendtime").set(mmsg.getSentDate());
									p_br.getCell("emm_recvtime").set(mmsg.getReceivedDate());
									p_br.getCell("emm_fromaddr").set(fromAddr.trim());
									p_br.getCell("emm_subject").set(mmsg.getSubject().toString());
									if(!hashtag.isEmpty()) {
										UniLog.log("Policy Nos grapped [" + hashtag.toString());
										p_br.getCell("emm_hashtag").set(hashtag.toString());
									}
									p_br.addCurrent();
									ByteArrayInputStream bis = new ByteArrayInputStream(content);
									FilingUtil.storeFile(p_br.getSessionHelper().getAgent(), "emailfiling", md5, p_host+":"+p_login, subject, bis,stime) ;
									moveToSaved = true;
								}
							}
						} catch (Exception ex) {
							UniLog.log(ex);
						}
						UniLog.log1("mime msg ct " + stime.toString() + " size " + msg.getSize() + " md5 " + md5 + " subject[" + subject+"]");
					}
				} else {
					UniLog.log1("msg is not mime st " + stime.toString() + " size " + msg.getSize() + " subject[" + subject+"]");
				}
				if(moveToSaved) filedList.add(msg); else if(moveToOther) unknownList.add(msg);
			}
			if(filedList.size() > 0) {
				UniLog.log("move filed message to Filed Folder");
				Message[] msgs = new Message[filedList.size()];
				for(int n = 0; n < filedList.size();n++) {
					msgs[n] = filedList.get(n);
				}
				com.sun.mail.imap.IMAPFolder xx,yy;
				xx = (com.sun.mail.imap.IMAPFolder) folder;
				yy = (com.sun.mail.imap.IMAPFolder) filedFolder;
				xx.moveMessages(msgs, yy);
			}
			if(unknownList.size() > 0) {
				UniLog.log("move unprocessed message to Unknown Folder");
				Message[] msgs = new Message[unknownList.size()];
				for(int n = 0; n < unknownList.size();n++) {
					msgs[n] = unknownList.get(n);
				}
				com.sun.mail.imap.IMAPFolder xx,yy;
				xx = (com.sun.mail.imap.IMAPFolder) folder;
				yy = (com.sun.mail.imap.IMAPFolder) unknownFolder;
				xx.moveMessages(msgs, yy);
			}
			
			return (ReturnMsg.defaultOk);
		}
		/*
		catch(Exception ex) {
			ex.printStackTrace();
			return attList;
		}
		*/
		finally {
			if (folder != null) { try {folder.close(true); }catch(Exception ex) {} }
			if (trashFolder != null) { try {trashFolder.close(true); }catch(Exception ex) {} }
			if (store != null) { try {store.close(); } catch(Exception ex) {} }
		}
	}
	/*
	public static void main(String args[]){
		UniLog.log("Test Get Mail");
		try {
//			getEmailNotice("imap.gmail.com", "disk55760@gmail.com", "mvu18s!x", false, 1000,true);
//			getEmailNotice("imap.gmail.com", "axa.exchange@pedderhealth.com", "LyMvyDtMmu8xSkZ", false, 1000,true);
			getEmailNotice("imap.gmail.com", "tyt92791082@gmail.com", "ykvsymvlllpojzsi", false, 1000,true);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	*/
	public static void main(String args[]){
//		Pattern p = Pattern.compile(".*([0-9]+GH)");
//		Matcher m = p.matcher("<ABC><b>Policy</b>12345678GH</ABC");
		/*
		Pattern p = Pattern.compile("(.*)");
		Matcher m = p.matcher("GH");
		UniLog.log(m.group(0));
		UniLog.log(m.group(1));
		*/
		
	//	Pattern p = Pattern.compile("(.*[^0-9]|^)([0-9]+GH)");
//		Pattern p = Pattern.compile("([0-9]+GH)");
//		Matcher m = p.matcher("HAAH 123 xx12345GH");
//		UniLog.log(p.matches(regex, input));
		/*(
		Matcher m = p.matcher("HAAH ABCDE xx12345GH");
		
		UniLog.log(""+m.matches());		
		UniLog.log(m.group(0));
		UniLog.log(m.group(1));
		*/
//		UniLog.log(""+Pattern.matches("ABCDE", "1ABCDE2"));
		
		
		/*
		Pattern pattern = Pattern.compile("ab", Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher("ABcabdAb");
		// using Matcher find(), group(), start() and end() methods
		while (matcher.find()) {
			System.out.println("Found the text \"" + matcher.group()
					+ "\" starting at " + matcher.start()
					+ " index and ending at index " + matcher.end());
		}	
		*/
		Pattern pattern = Pattern.compile("[0-9]{8}GH");
		Matcher matcher = pattern.matcher("123GH 33224888GH");
		while (matcher.find()) {
			System.out.println("Found the text \"" + matcher.group()
					+ "\" starting at " + matcher.start()
					+ " index and ending at index " + matcher.end());
		}	
	}
}