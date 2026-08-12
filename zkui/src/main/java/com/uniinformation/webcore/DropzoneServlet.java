package com.uniinformation.webcore;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONException;
import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.utils.CryptoUtil;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.QRCodeUtil;
import com.uniinformation.utils.RegUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.whereclpar.Expression;

public class DropzoneServlet extends HttpServlet {
	//final static long expireTime = 60000;  //1 min
	final static long expireTime = 900000;  //15 min
	//final static long expireTime = 3600000;  //1 hr
	final static String qrCodeRE = "BCKRG([0-9]{10})BDTRG([0-9]{10}).*";
	protected void doPost(HttpServletRequest request, HttpServletResponse response){
		try{
			response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
			response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
			response.setHeader("Expires", "0"); // Proxies.
			response.setContentType("text/html");  
			boolean isMultipart = ServletFileUpload.isMultipartContent(request);  
			PrintWriter out = response.getWriter();  
			if (isMultipart){ 
				// Create a factory for disk-based file items  
				FileItemFactory factory = new DiskFileItemFactory();  
				// Create a new file upload handler  
				ServletFileUpload upload = new ServletFileUpload(factory);  
				
				//fix chinese file name problem
				upload.setHeaderEncoding("UTF-8"); 
				
				// Parse the request  
				List items = upload.parseRequest(request);  
				Iterator iterator = items.iterator();  
				String uuid = null;
				boolean autoFiling = false;
				while (iterator.hasNext()) {  
					FileItem item = (FileItem) iterator.next();  
					if (item.isFormField()){
						UniLog.logm(this, "form field: %s:%s", item.getFieldName(), item.getString());
						if (StringUtils.equalsIgnoreCase("uuid", item.getFieldName())){
							uuid = item.getString();
						}
						else if (StringUtils.equalsIgnoreCase("autoFiling", item.getFieldName())){
							autoFiling = StringUtils.equalsIgnoreCase(item.getString(),"true");
						}
						continue;
					}
					else{
						String fileName = item.getName();      
						if (StringUtils.isBlank(fileName)){
							UniLog.log("filename is blank, ignore");
							continue;
						}
						if (uuid == null || !StringUtils.startsWith(uuid, "dropzone-")){
							UniLog.log("invalid uuid, ignore");
							continue;
						}
						
						
						byte[] bytes = IOUtils.toByteArray(item.getInputStream());
						Pair filePair = Pair.of(fileName, bytes);
						
						//IOUtils.write((byte[])filePair.getRight(), new FileOutputStream("/tmp/haha.out"));  //write to fs for debug
						SessionHelper sh = ZkSessionHelper.getSessionHelper(request, response);
						if (sh == null || !sh.isLogin()){
							UniLog.logm(this,"invalid session, ignore upload file");
							return;
						}
						if (autoFiling){
							//TODO implment auto filing 
							//read QR code from pdf, if error, return error
							ByteArrayInputStream bis = null;
							try {
								bis = new ByteArrayInputStream(bytes);
								List<String> qrcodeList = QRCodeUtil.decodeQRCode(bis);
								UniLog.logm(this,"qrcodeList size:" + qrcodeList.size() + ",list:" + qrcodeList);
								removeInvalidQRCode(qrcodeList);
								UniLog.logm(this,"after:qrcodeList size:" + qrcodeList.size() + ",list:" + qrcodeList);
								
								if (!qrcodeList.isEmpty()) {
									if (qrcodeList.size() == 1) {
										int bckRg = -1;
										String[] bckErrMsgs = new String[1];
										//String docType = null;
										int dtRg = -1;
										try {
											String s = qrcodeList.get(0);
											List<String> regResult = RegUtil.parse(s, qrCodeRE);
											if (regResult.size() >= 2){
												bckRg = Integer.parseInt(regResult.get(0));
												dtRg = Integer.parseInt(regResult.get(1));
												UniLog.log1("valid qr code:%s bckRg:%d dtRg:%d", s, bckRg, dtRg);
											}
											else{
												UniLog.log1("invalid qr code:%s", s);
											}
											/*
											if (s.startsWith("BCKRG")) {
												bckRg = Integer.parseInt(s.substring(5, 15));
												dtRg = s.substring(20).trim();
											}
											*/
											/*JSONObject json = new JSONObject(qrcodeList.get(0));
											String id = json.optString("User ID");
											String date = json.optString("Date");
											docType = json.optString("Doc Type");
											bckRg = getBodyCheckRg(sh, id, date, docType, bckErrMsgs);*/
											
											ReturnMsg rtn = checkHealthQnr(sh, bckRg);
											if (!rtn.getStatus()){
												bckRg = -1;
												bckErrMsgs[0] = rtn.getMsg();
											}
										} catch (Exception e) {
											e.printStackTrace();
										}
										if (bckRg > 0) {
											byte[] encryptData = CryptoUtil.encrypt(sh.getAESKey(), bytes, null, true);
											if (encryptData != null) {
												if (savePdfToDb(sh, bckRg, fileName, dtRg, encryptData)) {
													UniLog.logm(this,"file saved to db %s %s", uuid, fileName);
													out.println(StringUtil.convertWebString(fileName) + " saved to db");
												} else {
													UniLog.logm(this,"file saved to db fail %s %s", uuid, fileName);
													response.setStatus(HttpServletResponse.SC_FORBIDDEN);
													out.println(StringEscapeUtils.escapeJava(fileName) + ", file saved to db fail");
												}
											} else {
												UniLog.logm(this,"file encrypt fail %s %s", uuid, fileName);
												response.setStatus(HttpServletResponse.SC_FORBIDDEN);
												out.println(StringEscapeUtils.escapeJava(fileName) + ", file encrypt fail");
											}
										} else {
											UniLog.logm(this,"file upload fail %s %s user record not found", uuid, fileName);
											response.setStatus(HttpServletResponse.SC_FORBIDDEN);
											out.println(StringEscapeUtils.escapeJava(fileName) + ", " + bckErrMsgs[0]);
										}
									} else {
										UniLog.logm(this,"file upload fail %s %s Multiple QR code detected", uuid, fileName);
										response.setStatus(HttpServletResponse.SC_FORBIDDEN);
										out.println(StringEscapeUtils.escapeJava(fileName) + ", Multiple QR code detected");
									}
								} else {
									UniLog.logm(this,"file upload fail %s %s QR code not found", uuid, fileName);
									response.setStatus(HttpServletResponse.SC_FORBIDDEN);
									out.println(StringEscapeUtils.escapeJava(fileName) + ", QR code not found");
								}
							} catch (Exception e) {
								UniLog.logm(this,"file upload fail %s %s QR code not found", uuid, fileName);
								response.setStatus(HttpServletResponse.SC_FORBIDDEN);
								out.println(StringEscapeUtils.escapeJava(fileName) + ", QR code not found");
								e.printStackTrace();
							} finally {
								if (bis != null)
									bis.close();
							}
							return;
						}
						else{
							sh.putSessionDataEx(uuid, filePair, expireTime);
							UniLog.logm(this,"file uploaded %s %s", uuid, fileName);
							out.println(StringUtil.convertWebString(fileName) + " uploaded");
						}
					}  
				}  
			}
			else{  
				UniLog.logm(this,"not multipart, ignore");
			}  
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	private ReturnMsg checkHealthQnr(SessionHelper sh, int p_rg){
		if (p_rg <= 0){
			return(new ReturnMsg(false,"User record not found. Ref Id:"+ p_rg));
		}
		BiResult biResult = null;
		try {
			BiSchema schema = (BiSchema) sh.getSessionData("biSchema");
			if (schema == null){
				schema = BiSchema.loadSchema(sh);
			}
			BiView view = schema.getViewByName("clinic.HealthQnr");
			if (view == null){
				return new ReturnMsg(false,"invalid view");
			}
			biResult = view.newBiResult(sh.getLoginId(), null,null,sh);
			ReturnMsg rtnMsg = biResult.addCustomCondition(String.format("bck_rg = %d", p_rg));
			if (!rtnMsg.getStatus()) {
				return(rtnMsg);
			}
			if (biResult.query(true).getStatus() && biResult.getRowCount() > 0) {
				UniLog.log("found bodychk records ");
				return(new ReturnMsg(true));
			}
			return(new ReturnMsg(false,"User record not found. Ref Id:"+ p_rg));
		} 
		catch (Exception e) {
			e.printStackTrace();
			return(new ReturnMsg(e));
		} 
		finally {
			if (biResult != null)
				biResult.close();
		}
		
	}
	/*
	//obsoleted, the qr code now contain bck_rg
	private int getBodyCheckRg(SessionHelper sh, String id, String date, String docType, String[] errMsgs) {
		BiResult biResult = null;
		errMsgs[0] = "user record not found";
		try {
			BiSchema schema = (BiSchema) sh.getSessionData("biSchema");
			if (schema == null)
				schema = BiSchema.loadSchema(sh);
			BiView view = schema.getViewByName("clinic.HealthQnr");
			if (view == null){
				UniLog.log("invalid view: cliinic.HealthQnr");
				return 0;
			}
			biResult = view.newBiResult(sh.getLoginId(), null);
			ReturnMsg rtnMsg = biResult.addCustomCondition(String.format("bck_id = '%s' and bck_date = '%s'", Expression.escapeStr(id), date));
			if (!rtnMsg.getStatus()) {
				UniLog.logm(this, "add condition failed: %s", rtnMsg);
				return 0;
			}
			if (biResult.query(true) && biResult.getRowCount() > 0) {
				UniLog.log("found bodychk records ");
	    		biResult.loadOneRecV(0);
	    		int result = biResult.getCell("bck_rg").getInt();
	    		TableRec tr = biResult.getSelectUtil().getQueryResult(
	    				String.format("select * from bodychk_doctype where bcdt_name = '%s'", Expression.escapeStr(docType)), null);
	    		if (tr.getRecordCount() > 0)
	    			return result;
	    		else
	    			errMsgs[0] = String.format("document type '%s' not found", docType);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (biResult != null)
				biResult.close();
		}
		return 0;
	}
	*/
	private static void removeInvalidQRCode(List<String> qrcodeList){
		UniLog.log1("before remove:%s",qrcodeList);
		if (qrcodeList  == null) return;
		Iterator<String> it = qrcodeList.iterator();
		while (it.hasNext()) {
		   String str = it.next();
		   List<String> regResult = RegUtil.parse(str, qrCodeRE);
		   if (regResult.size() != 2){
			   UniLog.log1("ignore %s", str);
			   it.remove();
		   }
		   /*
		   if (!StringUtils.startsWith(str, "BCKRG")){
			   UniLog.log1("ignore %s", str);
			   it.remove();
		   }
		   */
		}
		UniLog.log1("after remove:%s",qrcodeList);
	}
	private boolean savePdfToDb(SessionHelper sh, int bckrg, String fileName, int dtRg, byte[] data) {
		UniLog.log1("bodycheck rg:%d file:%s dtRg:%d", bckrg, fileName, dtRg);
		ByteArrayInputStream bis = null;
		BiResult biResult = null;
		try {
			BiSchema schema = (BiSchema) sh.getSessionData("biSchema");
			if (schema == null)
				schema = BiSchema.loadSchema(sh);
			BiView view = schema.getViewByName("clinic.HealthQnrFiling");
			if (view == null){
				UniLog.log("invalid view: cliinic.HealthQnrFiling");
				return false;
			}
			biResult = view.newBiResult(sh.getLoginId(), null,null,sh);
			ReturnMsg rtnMsg = biResult.addCustomCondition(String.format("bckf_rg = %d", bckrg));
			if (!rtnMsg.getStatus()) {
				UniLog.logm(this, "add condition failed: %s", rtnMsg);
				return false;
			}
			int foundPointer = -1;
			int maxSeq = 0;
			if (biResult.query(true).getStatus()) {
				for (int i = 0; i < biResult.getRowCount(); i++) {
					biResult.loadOneRecV(i);
					if (StringUtils.equals(biResult.getCell("bckf_ofilename").getString(), fileName))
						foundPointer = i;
					maxSeq = Math.max(maxSeq, biResult.getCell("bckf_seq").getInt());
				}
			}
			String key = "zkbi_bodychk_" + bckrg + "_" + fileName;
			bis = new ByteArrayInputStream(data);
			FilingUtil.storeFile(sh.getAgent(), null, key, key, fileName, bis);
			biResult.beginWork();
			if (foundPointer >= 0) {
				biResult.fetchOneRecV(foundPointer);
				UniLog.log("update bodychkfiling " + biResult.getCell("bckf_seq").getInt() + "," + biResult.getCell("bckf_time").getInt());
				biResult.getCell("bckf_key").set(key);
				biResult.getCell("bckf_dtrg").set(dtRg); //rg replace type
				if (StringUtils.isBlank(biResult.getCell("bckf_desc").toString()))
					biResult.getCell("bckf_desc").set(fileName);
				biResult.getCell("bckf_time").set((int)(System.currentTimeMillis() / 1000));
				rtnMsg = biResult.updateCurrent();
			} else {
				UniLog.log("insert bodychkfiling");
				biResult.clearCurrentRec();
				biResult.getCell("bckf_rg").set(bckrg);
				biResult.getCell("bckf_type").set("R"); //Report
				biResult.getCell("bckf_seq").set(maxSeq + 1);
				biResult.getCell("bckf_key").set(key);
				biResult.getCell("bckf_ofilename").set(fileName);
				biResult.getCell("bckf_dtrg").set(dtRg); //rg replace type
				biResult.getCell("bckf_desc").set(fileName);
				biResult.getCell("bckf_time").set((int)(System.currentTimeMillis() / 1000));
				rtnMsg = biResult.addCurrent();
			}
			if (rtnMsg != null && !rtnMsg.getStatus()) {
				UniLog.log("rollbackwork errMsg:" + rtnMsg.getMsg());
				biResult.rollbackWork();
				return false;
			} else {
				UniLog.log("commitwork");
				biResult.commitWork();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (biResult != null)
				biResult.close();
		}
		return false;
	}
}
