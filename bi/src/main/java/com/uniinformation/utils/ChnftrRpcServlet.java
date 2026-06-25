package com.uniinformation.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;

import com.lowagie.text.Image;
import com.uniinformation.rpccall.RpcServerConnection;
import com.uniinformation.rpccall.RpcServlet;
import com.uniinformation.utils.ChnftrParser.ChnftrGetImageInterface;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.WebRpcServer;
import com.uniinformation.webcore.ZkSessionHelper;
import com.kyoko.common.*;

public class ChnftrRpcServlet implements RpcServlet {
	int cnt;
	RpcServerConnection rpcconn;
	public ChnftrRpcServlet() {
	}
	public ChnftrRpcServlet(RpcServerConnection p_conn) {
		setConnection(p_conn) ;
	}
	public String chnWordSplit(String p_callback,String p_text,String p_engFont,String p_chnFont,double p_fontSize,double p_width)
	{
			UniLog.log("chnftrWordSplit " + p_fontSize + " , " + p_width);
			List<String> retList = ChnftrParser.splitText(p_text, p_engFont, p_chnFont, (float) p_fontSize, (int) p_width);
			if(p_callback != null && !p_callback.trim().equals("")) {
				Vector<String> v = new Vector<String>();
				for(String s : retList) {
					v.add(s);
				}
				rpcconn.callSegment(p_callback,v);
			}
				
			return(String.format("OK  %10d",retList.size()));
	}
	
	static public ReturnMsg streamChnftrToPdf(InputStream is,OutputStream os,final SessionHelper sh) throws Exception {
			ChnftrParser ps = new ChnftrParser(is,"");
			ps.setChnftrGetImageInterface(new ChnftrGetImageInterface(){
				@Override
				public byte[] getImage(String p_key) {
					String url=null;
					if(p_key.startsWith(ChnftrParser.GETIMAGE_TAG)) {
						url = SessionHelper.URLHEADER_FILING+p_key.substring(ChnftrParser.GETIMAGE_TAG.length());
					} else {
						url = p_key;
					}
					try {
						return(sh.newErpFileToByteArray(url));
					} catch (Exception ex) {
						UniLog.log(ex);
						return(null);
					}
					
				}});
			ps.setUseGetImageInterfaceByDefault(true);
			ps.print(os);
			return(ReturnMsg.defaultOk);
	}
	
	public String erpChnftrToPdf(String p_chnftrFile,String p_pdfFile) {
		final SessionHelper sh = WebRpcServer.getSessionHelper();
		if(sh == null) return("FAILNo Sessionhelper");
		try {
			String chnftrFile = null;
			String pdfFile = null;
			if(p_pdfFile.startsWith(ChnftrParser.GETIMAGE_TAG)) {
				pdfFile = SessionHelper.URLHEADER_FILING+p_pdfFile.substring(ChnftrParser.GETIMAGE_TAG.length());
			} else {
				pdfFile = p_pdfFile;
			}
			if(p_chnftrFile.startsWith(ChnftrParser.GETIMAGE_TAG)) {
				chnftrFile = SessionHelper.URLHEADER_FILING+p_chnftrFile.substring(ChnftrParser.GETIMAGE_TAG.length());
			} else {
				chnftrFile = p_chnftrFile;
			}
			InputStream is = sh.newErpFileInputStream(chnftrFile);
			OutputStream os = sh.newErpFileOutputStream(pdfFile);
			ReturnMsg rtn = streamChnftrToPdf(is,os,sh);
			is.close();
			os.close();
			if(rtn == null) return("OK"); else if(rtn.getStatus()) return("OK"); else return("FAIL"+rtn.getMsg());
		} catch (Exception ex) {
			UniLog.log(ex);
			return(null);
		}
	}
	public String chnftrToPdf(String p_chnftrFile,String p_pdfFile) {
		try {
			InputStream is = new FileInputStream(p_chnftrFile);
			OutputStream os = new FileOutputStream(p_pdfFile);
			ReturnMsg rtn = streamChnftrToPdf(is,os,null);
			is.close();
			os.close();
			if(rtn == null) return("OK"); else if(rtn.getStatus()) return("OK"); else return("FAIL"+rtn.getMsg());
		} catch (Exception ex) {
			UniLog.log(ex);
			return(null);
		}
		
		/*
		ChnftrParser ps = new ChnftrParser(p_upload,chnftrInitStr);
		*/
	}
	
	public String sendEmail(String p_from, String p_subject, String p_text, Vector p_vargs) {
		final SessionHelper sh = WebRpcServer.getSessionHelper();
		if(sh == null) return("FAILNo Sessionhelper");
		List<Pair<String,String>> toList = new ArrayList<Pair<String,String>>();
		List<Pair<String,String>> ccList = new ArrayList<Pair<String,String>>();
		List<Pair<String,String>> bccList = new ArrayList<Pair<String,String>>();
		List<EmailAttachment> attList = new ArrayList<EmailAttachment>();
		try {
			int idx=0;
			int cnt = (Integer) p_vargs.get(idx++);
			for(;cnt > 0;idx++,cnt--) {
				toList.add(Pair.of((String) p_vargs.get(idx), ""));
			}
			cnt = (Integer) p_vargs.get(idx++);
			for(;cnt > 0;idx++,cnt--) {
				ccList.add(Pair.of((String) p_vargs.get(idx), ""));
			}
			cnt = (Integer) p_vargs.get(idx++);
			for(;cnt > 0;idx++,cnt--) {
				bccList.add(Pair.of((String) p_vargs.get(idx), ""));
			}
			cnt = (Integer) p_vargs.get(idx++);
			for(;cnt > 0;idx += 2,cnt--) {
				File tmpFile = sh.newErpFileToTmpfile((String) p_vargs.get(idx), "tmpatt", ".tmp", new File("/tmp"));
				EmailAttachment att1 = new EmailAttachment();
				att1.setPath(tmpFile.getAbsolutePath());
				att1.setName((String) p_vargs.get(idx+1));
				att1.setDisposition(EmailAttachment.ATTACHMENT);
				attList.add(att1);	
				UniLog.log("add email Attachement " + tmpFile.getAbsolutePath()+" "+att1.getName());
			}
			BiUtil.sendEmail(
					Pair.of(p_from, (String) null),
					toList, 
					ccList, 
					bccList, 
					p_subject, 
					null, p_text, attList, sh);
		
			return("OK");
		} catch (Exception ex) {
			UniLog.log(ex);
			return("FAILExcaption Catched");
		}
		finally {
			for(EmailAttachment att: attList) {
//				CloseUtil.close(new File(att.getPath()));
				CloseUtil.delete(new File(att.getPath()));
			}
		}
	}
	
	public String chnGetImageInfo(int type,String p_filename) {
//		final SessionHelper sh = WebRpcServer.getSessionHelper();
		final SessionHelper sh = ZkSessionHelper.getSessionHelper();
		if(sh == null) {
			return("FAILNo SessionHelper");
		}
		try {
			byte[] imageBytes = sh.newErpFileToByteArray(p_filename);
			Image image = Image.getInstance(imageBytes);
			if(image == null) {
				return("FAILInvalid Image File");
			}
			return("OK  " + String.format("%10.3f%10.3f%10d%10d", image.getWidth(),image.getHeight(),image.getDpiX(),image.getDpiY()));
			/*
			float angle = ChnftrParser.getExifOrientation(imageBytes);
			if (angle != 0){
				if(type != 19) image.setRotationDegrees(angle * -1);
					//if image rotated, swap width and height
				if (Math.abs(angle) == 90 || Math.abs(angle) == 270){
					rotated = true;
					if(type != 19) {
						int tmpWidth =  width;
						width = height;
						height = tmpWidth;
					}
				}
			}
			*/
		} catch (Exception ex) {
			UniLog.log("ex");
			return("FAIL"+ex.toString());
		}
	}
	public String sendHtmlEmail(String p_from, String p_subject, String p_html, Vector p_vargs) {
		final SessionHelper sh = WebRpcServer.getSessionHelper();
		if(sh == null) return("FAILNo Sessionhelper");
		List<Pair<String,String>> toList = new ArrayList<Pair<String,String>>();
		List<Pair<String,String>> ccList = new ArrayList<Pair<String,String>>();
		List<Pair<String,String>> bccList = new ArrayList<Pair<String,String>>();
		List<EmailAttachment> attList = new ArrayList<EmailAttachment>();
		try {
			int idx=0;
			int cnt = (Integer) p_vargs.get(idx++);
			String htmlStr;
			for(;cnt > 0;idx++,cnt--) {
				toList.add(Pair.of((String) p_vargs.get(idx), ""));
			}
			cnt = (Integer) p_vargs.get(idx++);
			for(;cnt > 0;idx++,cnt--) {
				ccList.add(Pair.of((String) p_vargs.get(idx), ""));
			}
			cnt = (Integer) p_vargs.get(idx++);
			for(;cnt > 0;idx++,cnt--) {
				bccList.add(Pair.of((String) p_vargs.get(idx), ""));
			}
			cnt = (Integer) p_vargs.get(idx++);
			for(;cnt > 0;idx += 2,cnt--) {
				File tmpFile = sh.newErpFileToTmpfile((String) p_vargs.get(idx), "tmpatt", ".tmp", new File("/tmp"));
				EmailAttachment att1 = new EmailAttachment();
				att1.setPath(tmpFile.getAbsolutePath());
				att1.setName((String) p_vargs.get(idx+1));
				att1.setDisposition(EmailAttachment.ATTACHMENT);
				attList.add(att1);	
				UniLog.log("add email Attachement " + tmpFile.getAbsolutePath()+" "+att1.getName());
			}
			if(p_html != null && p_html.startsWith(SessionHelper.URLHEADER_FILE)) {
				InputStream is = sh.newErpFileInputStream(p_html);
				htmlStr = IOUtils.toString(is, "UTF-8");
			} else {
				htmlStr = p_html;
			}
			BiUtil.sendEmail(
					Pair.of(p_from, (String) null),
					toList, 
					ccList, 
					bccList, 
					p_subject, 
					htmlStr, null, attList, sh);
		
			return("OK");
		} catch (Exception ex) {
			UniLog.log(ex);
			return("FAILExcaption Catched");
		}
		finally {
			for(EmailAttachment att: attList) {
				CloseUtil.close(new File(att.getPath()));
				CloseUtil.delete(new File(att.getPath()));
			}
		}
	}
		
	/*
	public static List<String> splitText(String text, String engFontFace, String chnFontFace, float fontSize, int chnftrWidth) {
	}
	}
	*/

	@Override
	public void init_servlet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close_servlet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConnection(RpcServerConnection conn) {
		// TODO Auto-generated method stub
		rpcconn = conn;
		
	}

	@Override
	public String ping() {
		// TODO Auto-generated method stub
		cnt++;
		return ("OK");
	}
	
}
