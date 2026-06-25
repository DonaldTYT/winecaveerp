package com.uniinformation.axa;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Provider;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeBodyPart;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import com.drew.tools.FileUtil;
import com.google.gson.JsonObject;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.axa.BiResultAxaClaim;
import com.uniinformation.utils.Base64Util;
import com.uniinformation.utils.CloseUtil;
import com.uniinformation.utils.EmailUtil;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZipUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.EmailUtil.SecMode;
import com.uniinformation.utils.IniHelper;
import com.uniinformation.utils.bc.KeyBasedFileProcessor;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

public class AxaUtil {
	public static boolean fDebug = false;
	public final static String ATT_BK_FOLDER = "/yic/tmp/axaattbk";

	public static List<Map<String,Object>> getEmailAtts(String p_host, String p_login, String p_passwd, boolean p_delEmailFlag) throws Exception {
		return getEmailAtts(p_host, p_login, p_passwd, p_delEmailFlag, -1);
	}
	/***
	 * retrieve attachement name, file from inbox
	 * this call should be called WS
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String,Object>> getEmailAtts(String p_host, String p_login, String p_passwd, boolean p_delEmailFlag, int p_maxEmailCnt) throws Exception{
		UniLog.log1("called del:%s max:%d", p_delEmailFlag, p_maxEmailCnt);
		ArrayList<Map<String,Object>> attList = new ArrayList();
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
			
			UniLog.log1("msg total:%d new:%d", folder.getMessageCount(), folder.getUnreadMessageCount());
			Message messages[] = folder.getMessages();
			
			//filter email
			ArrayList<Message> filteredMsgList = new ArrayList<Message>();
			for (Message msg : messages) {
				if (StringUtils.startsWithIgnoreCase(msg.getSubject(), "Panel file for PEDD")) {
					filteredMsgList.add(msg);
				}
			}
			UniLog.log1("filtered msg total:%d", filteredMsgList.size());
			
			//sort by received date
			Collections.sort(filteredMsgList, new Comparator<Message>() {
			    @Override
			    public int compare(Message msg1, Message msg2) {
			    	try {
			    		return msg1.getReceivedDate().compareTo(msg2.getReceivedDate());
			    	}
			    	catch(Exception ex) {
			    		UniLog.log1("error:" +ex.getMessage());
			    		return 0;
			    	}
			    }
			});
			
			int idx = 0;
			for (Message msg : filteredMsgList) {
				idx++;
				String sub = msg.getSubject();
				String from = "unknown";
				if (msg.getReplyTo().length >= 1) {
					from = msg.getReplyTo()[0].toString();
				}
				else if (msg.getFrom().length >= 1) {
					from = msg.getFrom()[0].toString();
				}
				UniLog.log1("msg:%d/%d sub:%s from:%s seen:%s", idx, filteredMsgList.size(), sub, from, msg.isSet(Flags.Flag.SEEN));
				
				
				saveContent(msg, msg.getContent(), attList);
				
				
				//mark read and delete email
				if (p_delEmailFlag) {
					msg.setFlag(Flags.Flag.SEEN,true);
					if (fGmail) {
						folder.copyMessages(new Message[] {msg}, trashFolder);
					}
					else {
						msg.setFlag(Flags.Flag.DELETED, true);
					}
				}
				else {
					UniLog.log1("not delete email");
				}
				
				if (p_maxEmailCnt > 0 && idx >= p_maxEmailCnt) {
					UniLog.log1("maxEmailCnt reached %d/%d. stop process next email", idx, p_maxEmailCnt);
					return attList;
				}
				
			}
			
			if (fDebug) {
				for (Map<String,Object> att : attList) {
					UniLog.log1("result sub:%s date:%s file:%s name:%s", att.get("msgSub"), att.get("msgRecvDate"), att.get("attFile"), att.get("attName"));
				}
			}
			return attList;
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

	/***
	 * 
	 * @param msg - orgmsg
	 * @param content - current cotent
	 * @param p_outAttList - result
	 * @throws IOException
	 * @throws MessagingException
	 */
	private static void saveContent(Message msg, Object content, List<Map<String,Object>> p_outAttList) throws IOException, MessagingException {
		//UniLog.log1("called");
		
		OutputStream out = null;
		InputStream in = null;
		try {
			if (content instanceof Multipart) {
				Multipart multi = ((Multipart)content);
				if (fDebug) UniLog.log1("multi contentType:%s",multi.getContentType());
				int parts = multi.getCount();
				for (int j=0; j < parts; ++j) {
					MimeBodyPart part = (MimeBodyPart)multi.getBodyPart(j);
					if (fDebug) UniLog.log1("part:%d/%d contentType:%s", j+1, parts,part.getContentType());
					if (part.getContent() instanceof Multipart) {
						if (fDebug) UniLog.log1("is multipart. do recursion");
						saveContent(msg,part.getContent(), p_outAttList);
					}
					else {
						if (part.isMimeType("text/html")) {
							if (fDebug) UniLog.log1("ignore text/html");
						}
						else if (part.isMimeType("text/plain")) {
							if (fDebug) UniLog.log1("ignore text/plain");
						}
						else {
							String attName = part.getDataHandler().getName();
							UniLog.log1("sub:%s rdate:%s attname:%s", msg.getSubject(), msg.getReceivedDate(), attName);
							/*  filtering should perform by caller
							if (!StringUtils.endsWith(attName,".pgp")) {
								UniLog.log1("ignore non pgp file");
								continue;
							}
							*/
							File tmpFile = File.createTempFile("axaatt", ".tmp", new File("/tmp"));
							out = new FileOutputStream(tmpFile);
							in = part.getInputStream();
							
				            byte [] buf = new byte[1024];
							int len = 0;
				            while((len = in.read(buf)) != -1) {
				                out.write(buf,0,len);
				            }
							p_outAttList.add(buildAttMap(attName, tmpFile, msg.getSubject(), msg.getReceivedDate()));
				            /*
				            Map<String,Object> attMap = new HashMap();
							attMap.put("attName", attName);
							attMap.put("attFile", tmpFile);
							attMap.put("msgSub", msg.getSubject());
							attMap.put("msgRecvDate", msg.getReceivedDate());
							p_outAttList.add(attMap);
							*/
						}
					}
				}
			}
		}
		finally {
			if (in != null) { in.close(); }
			if (out != null) { out.flush(); out.close(); }
		}
	}
	public static Map<String,Object> buildAttMap(String attName, File attFile, String msgSub, Date msgRecvDate) {
		Map<String,Object> attMap = new HashMap();
		attMap.put("attName", attName);
		attMap.put("attFile", attFile);
		attMap.put("msgSub", msgSub);
		attMap.put("msgRecvDate", msgRecvDate);
		return attMap;
	}
	public static Map<String,Object> buildAttMap(JSONObject p_json) throws Exception{
		String attName = p_json.getString("attName");
		File attFile = Base64Util.decodeAsFile(p_json.getString("attFileB64"));
		String msgSub = p_json.getString("msgSub");
		Date msgRecvDate = DateUtil.dateTimeStrToDate(p_json.getString("msgRecvDate"));
		return buildAttMap(attName, attFile, msgSub, msgRecvDate);
	}
	public static JsonObject buildAttJson(Map<String, Object> attMap) throws Exception {
		JsonObject attJson = new JsonObject();
		attJson.addProperty("msgSub", (String) attMap.get("msgSub")); 
		attJson.addProperty("msgRecvDate", DateUtil.dateToDateTimeStr((Date)attMap.get("msgRecvDate"))); 
		attJson.addProperty("attName", (String) attMap.get("attName")); 
		File attFile = (File) attMap.get("attFile");
		attJson.addProperty("attFileB64", Base64Util.convertToString(attFile));
		return attJson;
	}
	
	/***
	 * ws client to retrieve email attachment
	 * @param p_delEmailFlag
	 * @param p_maxEmailCnt
	 * @return
	 * @throws Exception
	 */
	public static List<Map<String,Object>> getEmailAttsRemote(boolean p_delEmailFlag, int p_maxEmailCnt) throws Exception{
			Client client = ClientBuilder.newClient();
			IniHelper ini = SessionHelper.getIniHelper();
			if (ini == null) {
				throw new Exception("ini error");
			}
			String axaWSTarget = ini.getString("axaWSTarget","");
			if (StringUtils.isBlank(axaWSTarget)) {
				throw new Exception("axaWSTarget is not defined");
			}
			WebTarget webTarget = client.target(axaWSTarget).path("axa/getemailatts");
			Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
			JSONObject json = new JSONObject();
			
			//obtain from ini
			json.put("host", ini.getString("axaIMAPHost", "imap.gmail.com"));
			json.put("login", ini.getString("axaIMAPLogin", "axa.exchange@pedderhealth.com"));
//			json.put("passwd", ini.getString("axaIMAPPasswd", "LyMvyDtMmu8xSkZ"));
			json.put("passwd", ini.getString("axaIMAPPasswd", "jcku fmuu hmnn iixo"));
			json.put("delEmailFlag", p_delEmailFlag);
			json.put("maxEmailCnt", p_maxEmailCnt);
			UniLog.log1("post to %s, wait for response", axaWSTarget);
			Response response = invocationBuilder.post(Entity.json(json.toString()));
			UniLog.log1("response code:%d",response.getStatus());
			if (response.getStatus() != 200) {
				UniLog.log1("response error:%s", response.toString());
				String errorMsg = "response error";
				try {
					errorMsg = response.readEntity(String.class);
				}
				catch(Exception ex) { }
				//UniLog.log1("errorMsg:%s", errorMsg);
				throw new Exception(errorMsg);
			}
			String jsonStr = response.readEntity(String.class);
			JSONObject outJson = new JSONObject(jsonStr);
			//UniLog.log1("response ok.\njson:\n%s",outJson.toString(3));  //don't print this string, it's very long
			if (!StringUtils.equalsAnyIgnoreCase(outJson.getString("result"),"OK")) {
				UniLog.log1("response json result is bad. %s", outJson.getString("result"));
				throw new Exception("json result is bad");
			}
			
			JSONArray attsJson = outJson.getJSONArray("atts");
			ArrayList<Map<String,Object>> attList = new ArrayList();
			for (int i=0; i<attsJson.length(); i++) {
				JSONObject attJson = attsJson.getJSONObject(i);
				Map attMap = buildAttMap(attJson);
				attList.add(attMap);
			}
			return attList;
	}
	/*
	//for compatibility only, remove later
	public synchronized static ReturnMsg pollAndProcessEmail(String p_host,String p_login,String p_passwd,boolean p_delEmailFlag,SessionHelper p_sh) {
		return pollAndProcessEmail(p_delEmailFlag,1,p_sh);
	}
	*/
	
	private static ReturnMsg cmdImportPGP(String p_pgpFileName) {
		if (StringUtils.isBlank(p_pgpFileName)) {
			return new ReturnMsg(false,"infile is blank");
		}
		File pgpFile = new File(p_pgpFileName);
		if (!pgpFile.exists()) {
			return new ReturnMsg(false,"infile not exist");
		}
		ArrayList<Map<String,Object>> attList = new ArrayList();
		attList.add(buildAttMap("cmdImportPGP", pgpFile, "cmdImportPGP", new Date()));
		HashSet<String> hs = null;
		/*
		hs = new HashSet<String>();
		hs.add("PHR");
		hs.add("POL");
		hs.add("STA");
		*/
		return processEmail(attList,ZkSessionHelper.getSessionHelperDummy(null,"dummy",null),false,false,hs);
	}
	
	private static ReturnMsg processEmail(List<Map<String,Object>> attMapList, SessionHelper p_sh, boolean p_fBackup, boolean p_fDelAtt,HashSet<String> p_importWhat) {
		int datOkCnt = 0;
		int datFailCnt = 0;
		int decOkCnt = 0;
		int decFailCnt = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
		
		for (Map<String,Object> att: attMapList) {
			UniLog.log1("process sub:%s date:%s file:%s name:%s ", att.get("msgSub"), att.get("msgRecvDate"), att.get("attFile"), att.get("attName"));
			File attFile = (File) att.get("attFile");
			//check attachment is valid
			if (attFile == null) {
				UniLog.log1("attFile is null");
				continue;
			}
			
			//backup file pgp
			//e.g. /yic/tmp/axaattbk/2021/06/11/20210611010203000_20210611010203000_abc.pgp
			if (p_fBackup) {
				UniLog.log1("backup pgp file");
				String nowStr = sdf.format(new Date());
				String recvStr = nowStr;  //use now as default recv date
				try {
					recvStr = sdf.format((Date)att.get("msgRecvDate"));
				}
				catch(Exception ex) {}
				String attName = ((String) att.get("attName")).replaceAll("\\s", "");
				String bkFileName = String.format("%s/%s/%s/%s/%s_%s_%s",
						ATT_BK_FOLDER,
						StringUtils.substring(nowStr,0,4),
						StringUtils.substring(nowStr,4,6),
						StringUtils.substring(nowStr,6,8),
						nowStr, recvStr, attName);

				try {
					UniLog.log1("save a copy to %s", bkFileName);
					FileUtils.copyFile(attFile, new File(bkFileName));
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
			else {
				UniLog.log1("skip backup pgp file");
			}

			List<File> decFiles = null;
			String outFolderName = null;
			try {
				outFolderName = String.format("/tmp/axaemail%s", sdf.format(new Date()));
				
				UniLog.log1("outFolderName:%s", outFolderName);
				decFiles = axaDecFile("/yic/v/unidev/wxcache/bischema/axatest-pri-001.asc", "pass001", attFile.getAbsolutePath(), outFolderName);
				
				//del attachment for normal case
				if (p_fDelAtt) {
					UniLog.log1("del attachment %s", attFile.getAbsolutePath());
					attFile.delete();
				}
				else {
					UniLog.log1("skip delete attachment %s", attFile.getAbsolutePath());
				}
			}
			catch(Exception ex) {
				UniLog.log1("decrypt email error");
				ex.printStackTrace();
				decFailCnt++;
				continue;
			}
			if (decFiles == null) {
				UniLog.log1("cannot decrypt email");
				continue;
			}
			if (decFiles.size() == 0) {
				UniLog.log1("no file decrypted");
				continue;
			}
			decOkCnt++;


			//process the decrypted file
			int okCnt = 0;
			int failCnt = 0;
			for (File decFile : decFiles) {
				UniLog.log1("process file %s %s", decFile.getAbsolutePath(), FilenameUtils.getName(decFile.getAbsolutePath()));
				try {
					if (p_sh == null) {
						UniLog.log1("no sh, skip import file");
						continue;
					}
					String fName = FilenameUtils.getName(decFile.getAbsolutePath());
					FileInputStream fis = new FileInputStream(decFile);
					ReturnMsg rtn = null;
					if (StringUtils.equalsAnyIgnoreCase(fName, "GPPPECVP.TXT", "GPPPECVP.dat")) {
						if(p_importWhat == null || p_importWhat.contains("CVP")) {
							rtn = AxaEdi.uploadCVP(p_sh, fis);
						} else {
							UniLog.log1("skip import CVP");
						}
					}
					else if (StringUtils.equalsAnyIgnoreCase(fName, "GPPPEER2.TXT", "GPPPEER2.dat" )) {
						if(p_importWhat == null || p_importWhat.contains("ER2")) {
						HashSet<AxaEdi.ERRTYPE> updSet = new HashSet<AxaEdi.ERRTYPE>();
						updSet.add(AxaEdi.ERRTYPE.APHR);
						updSet.add(AxaEdi.ERRTYPE.APOL);
						updSet.add(AxaEdi.ERRTYPE.ACVP);
						updSet.add(AxaEdi.ERRTYPE.SPCN);
						updSet.add(AxaEdi.ERRTYPE.CHGC);
						rtn = AxaEdi.uploadER2(p_sh, fis,updSet);
						} else {
							UniLog.log1("skip import ER2");
						}
					}
					else if (StringUtils.equalsAnyIgnoreCase(fName, "GPPPEPHR.TXT", "GPPPEPHR.dat" )) {
						if(p_importWhat == null || p_importWhat.contains("PHR")) {
						rtn = AxaEdi.uploadPHR(p_sh, fis);
						} else {
							UniLog.log1("skip import PHR");
						}
					}
					else if (StringUtils.equalsAnyIgnoreCase(fName, "GPPPEPOL.TXT", "GPPPEPOL.dat" )) {
						if(p_importWhat == null || p_importWhat.contains("POL")) {
						rtn = AxaEdi.uploadPOL(p_sh, fis);
						} else {
							UniLog.log1("skip import POL");
						}
					}
					else if (StringUtils.equalsAnyIgnoreCase(fName, "GPPPESTA.TXT", "GPPPESTA.dat" )) {
						if(p_importWhat == null || p_importWhat.contains("STA")) {
						rtn = AxaEdi.uploadSTA(p_sh, fis);
						} else {
							UniLog.log1("skip import STA");
						}
					}
					else {
						UniLog.log1("ignore invalid filename");
						fis.close();
						continue;
					}
					if (rtn.getStatus()) {
						okCnt++;
					}
					else {
						failCnt++;
						UniLog.log1("fail:%s",rtn.getMsg());
					}
					
				}
				catch(Exception ex) {
					UniLog.log1("fail with exception:%s", ex.getMessage());
					ex.printStackTrace();
					failCnt++;
					//return new ReturnMsg(ex);
				}
			}
			UniLog.log1("finish okCnt:%d failCnt:%d", okCnt, failCnt);
			datOkCnt+=okCnt;
			datFailCnt+=failCnt;
			
			//remove the data file
			UniLog.log1("remove outFolderName:%s",outFolderName);
			FileUtils.deleteQuietly(new File(outFolderName));  //if want to keep decrupted file, can comment out this line
			UniLog.log("process end");
		}
		BiResultAxaClaim.clearCache();
		if (datFailCnt > 0 || decFailCnt > 0) {
			return new ReturnMsg(false, String.format("import result - datOk:%d datFail:%d decOk:%d decFail:%d", datOkCnt, datFailCnt, decOkCnt, decFailCnt));
		}
		return new ReturnMsg(true, String.format("import result - datOk:%d decOk:%d", datOkCnt, decOkCnt));
	}
	/***
	 * retrieve encrypted email from imap and process it
	 * @param p_host
	 * @param p_login
	 * @param p_passwd
	 * @return
	 */
	public synchronized static ReturnMsg pollAndProcessEmail(boolean p_delEmailFlag, int p_maxEmailCnt, SessionHelper p_sh) {
		List<Map<String,Object>> attMapList = null;
		try {
			//attMapList = AxaUtil.getEmailAtts(p_host,p_login,p_passwd,p_delEmailFlag,1);
			attMapList = AxaUtil.getEmailAttsRemote(p_delEmailFlag,p_maxEmailCnt);
			UniLog.log1("getEmailAttsRemote ok. cnt:%d", attMapList.size());
			
		}
		catch(Exception ex) {
			UniLog.log1("getEmailAttsRemote fail. msg:%s", ex.getMessage());
			return new ReturnMsg(ex);
		}
		return processEmail(attMapList, p_sh, true, true,null);
	}
	public static void cmdline(String args[]) {
		final Options options = new Options();
		new Option("d", "decrypt", true, "decrypt pgp file. e.g. /tmp/abc.pgp"){{this.setRequired(false); options.addOption(this);}};
		new Option("o", "outfolder", true, "output folder e.g. /tmp/abcout"){{this.setRequired(false); options.addOption(this);}};
		new Option("i", "import", true, "import pgp file e.g. /tmp/abc.pgp"){{this.setRequired(false); options.addOption(this);}};
		CommandLineParser cliParser = new DefaultParser();
		CommandLine cli;
		try {
			cli = cliParser.parse(options, args);
		} 
		catch (Exception ex) {
			//ex.printStackTrace();
			HelpFormatter helpFormatter = new HelpFormatter();
			UniLog.log1("ERROR:invalid option:" + ex.getMessage());
			System.exit(1);
			return;
		}
		if (cli.getOptionValue("decrypt") != null) {
			//obtain encrypted file
			String decFileName = cli.getOptionValue("decrypt");
			UniLog.log1("decrypt: %s", decFileName);
			if (!new File(decFileName).exists()) {
				UniLog.log1("ERROR:file %s not exist", decFileName);
				System.exit(1);
				return;
			}
			
			//obtain output folder
			String outFolderName = cli.getOptionValue("outfolder");
			if (StringUtils.isBlank(outFolderName)) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
				outFolderName = String.format("/tmp/clidec%s", sdf.format(new Date()));
			}
			if (new File(outFolderName).exists()) {
				UniLog.log1("ERROR:outfolder %s already exist", outFolderName);
				System.exit(1);
			}
			try {
				List<File>decFiles = axaDecFile("/yic/v/unidev/wxcache/bischema/axatest-pri-001.asc", "pass001", decFileName, outFolderName);
				for (File decFile : decFiles) {
					UniLog.log1("FILE:" + decFile.getAbsolutePath());
				}
			}
			catch(Exception ex) {
				UniLog.log1("ERROR:" + ex.getMessage());
			}
			System.exit(0);
		}
		if (cli.getOptionValue("import") != null) {
			ReturnMsg rtn = cmdImportPGP(cli.getOptionValue("import"));
			UniLog.log1("import result: %s", rtn);
			System.exit(0);
		}
	}
	public static void main(String args[]) throws Exception {
	   	//axaEncFile("/yic/v/unidev/wxcache/bischema/axatest-pub-001.asc", "/tmp/pgp/a.txt", "/tmp/pgp/a.txt.enc"); //use our our own pubkey for testing
    	//axaEncFile("/tmp/pgp/PEDDpanel.txt", Arrays.asList(Pair.of(new File("/tmp/pgp/a.txt"),"a1.txt")), "/tmp/pgp/a.txt.enc"); //use axa pubkey
    	//axaDecFile("/yic/v/unidev/wxcache/bischema/axatest-pri-001.asc", "pass001", "/tmp/pgp/PEDD20210609.pgp", "/tmp/pgp");
		//sendEmailRemote("Data file 210509", "testqr.pdf", FileUtils.readFileToByteArray(new File("/tmp/testqr.pdf")));
		//getEmailAttsRemote("imap.gmail.com", "axa.exchange@pedderhealth.com", "LyMvyDtMmu8xSkZ",false,1);
		//pollAndProcessEmail(true,1,null);
		AxaUtil.cmdline(args);
	}
	private static synchronized void loadProvider() {
		boolean fFound = false;
		for (Provider p : Security.getProviders()) {
			if (StringUtils.equals("BC", p.getName())) {
				fFound = true;
			}
		}
		if (fFound) {
			UniLog.log1("remove previous provider");
			//andrew210630: try to fix tomcat restart bug: org.bouncycastle.openpgp.PGPException: Exception constructing key
			Security.removeProvider("BC");
		}
		Security.addProvider(new BouncyCastleProvider());
	}
	
	private static List<File> axaDecFile(String p_priKeyFile, String p_priPass, String p_inFile, String p_outFolder) throws Exception{
		//Security.addProvider(new BouncyCastleProvider());
		loadProvider();
		File tmpDecFile = File.createTempFile("axadec", ".tmp",new File("/tmp"));
		UniLog.log1("tmpDecFile %s",tmpDecFile);
		KeyBasedFileProcessor.decryptFile(p_inFile, p_priKeyFile, p_priPass.toCharArray(), tmpDecFile.getAbsolutePath());
		List<File> outFiles = ZipUtil.untar(tmpDecFile, new File(p_outFolder));
		if (outFiles != null && outFiles.size() > 0) {
			//delete tmpfile for normal case
			tmpDecFile.delete();
		}
		return outFiles;
	}
	private static ReturnMsg axaEncFile(String p_pubKeyFile, List<Pair<File,String>> p_inFiles, String p_outFile) {
		//Security.addProvider(new BouncyCastleProvider());
		loadProvider();
		try {
			UniLog.log("before enc");
			File tmpDecFile = File.createTempFile("axaenc", ".tmp",new File("/tmp"));
			ZipUtil.tar(p_inFiles, tmpDecFile);
			KeyBasedFileProcessor.encryptFile(p_outFile, tmpDecFile.getAbsolutePath(), p_pubKeyFile, true, true);
			UniLog.log("after enc");
			return ReturnMsg.defaultOk;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return new ReturnMsg(ex);
		}
	}	
	/***
	 * call sendEmail via web service
	 * 
	 * Limitation: 
	 * as using byte[], the dat file cannot be too large(suggest within 1MB)
	 * @param p_subject
	 * @param p_datFileName
	 * @param p_dat
	 * @return
	 */
	public static ReturnMsg sendEmailRemote(String p_subject, String p_datFileName, byte[] p_dat) {
		return(sendEmailRemote(p_subject, p_datFileName, p_dat,null));
	}
	public static ReturnMsg sendEmailRemote(String p_subject, String p_datFileName, byte[] p_dat,String p_emailAddrs) {
		try {
			UniLog.log1("called sub:%s p_datFileName:%s", p_subject, p_datFileName);
			Client client = ClientBuilder.newClient();
			IniHelper ini = SessionHelper.getIniHelper();
			if (ini == null) {
				UniLog.log1("ini error");
				return ReturnMsg.defaultFail;
			}
			String axaWSTarget = ini.getString("axaWSTarget","");
			if (StringUtils.isBlank(axaWSTarget)) {
				UniLog.log1("axaWSTarget is not defined");
				return ReturnMsg.defaultFail;
			}
			WebTarget webTarget = client.target(axaWSTarget).path("axa/sendemail");
			Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);
			JSONObject json = new JSONObject();
			json.put("subject", p_subject);
			json.put("datFileName", p_datFileName);
			json.put("dat", Base64Util.convertToString(p_dat));
			if(!StringUtils.isBlank(p_emailAddrs)) {
				json.put("emailAddrs", p_emailAddrs);
			}
			UniLog.log1("post to %s, wait for response", axaWSTarget);
			Response response = invocationBuilder.post(Entity.json(json.toString()));
			UniLog.log1("response code:%d",response.getStatus());
			if (response.getStatus() != 200) {
				UniLog.log1("response error:%s", response.toString());
				return ReturnMsg.defaultFail;
			}
			String jsonStr = response.readEntity(String.class);
			JSONObject outJson = new JSONObject(jsonStr);
			UniLog.log1("response ok.\njson:\n%s",outJson.toString(3));
			if (StringUtils.equalsAnyIgnoreCase(outJson.getString("result"),"OK")) {
				return ReturnMsg.defaultOk;
			}
			else {
				return new ReturnMsg(false,outJson.getString("message"));
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return new ReturnMsg(ex);
		}
	}
	/***
	 * zip the dat bytes and send via smtp
	 * 
	 * Remark:
	 * This call should called by WS
	 * suggted flow: sendEmailRemote() -> AxaRS.sendEmail() -> sendMail()
	 * @param p_subject
	 * @param p_datFileName
	 * @param p_dat
	 * @return
	 */
	public static ReturnMsg sendEmail(String p_subject, String p_datFileName, byte[] p_dat) {
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
}