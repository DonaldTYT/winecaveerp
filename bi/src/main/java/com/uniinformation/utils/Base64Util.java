package com.uniinformation.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.uniinformation.webcore.SessionHelper;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Base64;

public class Base64Util {
	public static void main(String args[]) throws Exception{
		//Base64Util.convertToString(new File("/tmp/ok.wav"));
		//Base64Util.convertToString(new File("/tmp/fail.wav"));
		String str = "Hello world!abc123!@#$=";
		UniLog.log1("test base64: [%s] [%s] [%s]",str, Base64Util.encode(str), Base64Util.decode(Base64Util.encode(str)));
		
		//remark: UTF8 cannot store binary bytes. For binary, use ISO8859_1 instead of UTF8. when convert KQCROAITZOH7WFOM to string, it become KQCROAITH6H7WFJ7. 
		UniLog.log1("test base32:" + new String(Base64Util.encodeBase32(Base64Util.decodeBase32("KQCROAITZOH7WFOM")))); 
		UniLog.log1("test base32(ISO8859_1) good:" +  new String(Base64Util.encodeBase32((new String(Base64Util.decodeBase32("KQCROAITZOH7WFOM"),"ISO8859_1")).getBytes("ISO8859_1")))); 
		UniLog.log1("test base32(UTF-8) bad:" +  new String(Base64Util.encodeBase32((new String(Base64Util.decodeBase32("KQCROAITZOH7WFOM"),"UTF-8")).getBytes("UTF-8")))); 
		UniLog.log1("convert base32 to base64:" + convertBase32ToBase64("KQCROAITZOH7WFOM"));
		
	}
	public static String convertToString(File p_file) throws IOException{
		byte[] bytes = FileUtils.readFileToByteArray(p_file);
		return convertToString(bytes);
	}
	public static String convertToString(byte[] p_bytes) throws IOException{
		if (p_bytes == null || p_bytes.length == 0) {
			return null;
		}
		final Base64 base64 = new Base64();
		String encoded = base64.encodeToString(p_bytes);                                       
		//UniLog.log("Encoded String: " + encoded);
		return(encoded);
	}
	public static String convertToImgString(byte[] p_bytes, String p_fmt) throws IOException{
		String imgStr = convertToString(p_bytes);
		if (StringUtils.isBlank(imgStr) || StringUtils.isBlank(p_fmt)) {
			return imgStr;
		}
		return "data:image/"+StringUtils.lowerCase(p_fmt)+";base64," + imgStr;
	}
	public static boolean convertStringToOutputStream(String p_base64, OutputStream p_os){
		try{
			if (p_base64 == null){
				UniLog.logm(null, "baase64 is null");
				return(false);
			}
			String[] base64Split = p_base64.split(",");
			//convert base64 string to binary data
			byte[] data = DatatypeConverter.parseBase64Binary(base64Split[1]);
			p_os.write(data);
			p_os.close();
			return(true);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return(false);
	}
	/*
	 * encode str to base64 (urlsafe)
	 */
	public static String encode(String p_str) {
		try {
			Base64 base64 = new Base64(true);
			byte[] textByte = p_str.getBytes("UTF-8");
			return base64.encodeBase64URLSafeString(textByte);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	/***
	 * decode base64 
	 * @param p_base64Str
	 * @return
	 */
	public static String decode(String p_base64Str) {
		try {
			Base64 base64 = new Base64(true);
			return new String(base64.decode(p_base64Str), "UTF-8");
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static byte[] decodeAsBytes(String p_base64Str) {
		try {
			Base64 base64 = new Base64(true);
			return base64.decode(p_base64Str);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	public static File decodeAsFile(String p_base64Str) {
		try {
			Base64 base64 = new Base64(true);
			byte[] bytes = base64.decode(p_base64Str);
			File tmpFile = File.createTempFile("base64", ".tmp",new File("/tmp"));
			FileUtils.writeByteArrayToFile(tmpFile, bytes);
			return tmpFile;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/***
	 * decode base32 to bytes
	 * @param p_base32Str
	 * @return
	 */
	public static byte[] decodeBase32(String p_base32Str) {
		
		try {
			Base32 base32 = new Base32();
			return base32.decode(p_base32Str);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/***
	 * encode str to base32 bytes
	 * @param p_str
	 * @return
	 */
	public static byte[] encodeBase32(byte[] p_bytes) {
		try {
			Base32 base32 = new Base32();
			return base32.encode(p_bytes);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	public static String convertBase32ToBase64(String p_str) {
		try {
			Base64 base64 = new Base64();
			return base64.encodeBase64String(decodeBase32(p_str));
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	/***
	 * encrypt string
	 * remark: the encrypted string is much larger than original, as it contain iv(16byte), hash(32byte) and base64 (~+33%) + sha256
	 * 
	 * @param p_sh
	 * @param p_inStr
	 * @return
	 */
	public static String encryptStrToBase64(SessionHelper p_sh, String p_inStr) {
		try {
			return CryptoUtil.encryptToBase64(p_sh.getAESKey(), p_inStr.getBytes("UTF-8"), true);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	public static String decryptStrFromBase64(SessionHelper p_sh, String p_eDataWithIvString) {
		try {
			return new String(CryptoUtil.decryptFromBase64(p_sh.getAESKey(),p_eDataWithIvString),"UTF-8");
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
}
