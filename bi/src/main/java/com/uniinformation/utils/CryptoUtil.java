package com.uniinformation.utils;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;

public class CryptoUtil {
	final private static String ALGORITHM = "AES";
	final private static String TRANSFORMATION = "AES/CBC/PKCS5PADDING";
	final private static boolean debug = false;

	public static String encryptToBase64(byte[] p_key, byte[] p_data, boolean p_urlSafeFlag){
		byte[] eBytes = encrypt(p_key, p_data, null, true);
		if (p_urlSafeFlag){
			return Base64.encodeBase64URLSafeString(eBytes);
		}
		else{
			return Base64.encodeBase64String(eBytes);
		}
	}
	public static byte[] decryptFromBase64(byte[] p_key, String p_eDataWithIvString) {
		return(decrypt(p_key, Base64.decodeBase64(p_eDataWithIvString), true));
	}
	
	/***
	 * Encrypt data using aes
	 * @param p_key - 16byte aes128 
	 *                24byte aes192 
	 *                32byte aes256 (suggested)
	 * @param p_data - input data, not accept null data
	 * @param p_ivBytes - iv byte (16byte). if null, it will generate iv automatically. 
	 * 					  suggest:null
	 * @param p_hashFlag - generate hash. 
	 *                     suggest:true
	 * @param p_urlSafe - urlsafe
	 * @return (16byte iv + base64 encrypted data). null if sth wrong
	 */
	public static byte[] encrypt(byte[] p_key, byte[] p_data, byte[] p_ivBytes, boolean p_hashFlag){
		try {
			//input validation
			if (p_key == null || (p_key.length != 16 && p_key.length != 24 && p_key.length != 32)){
				throw new Exception("invalid key");
			}
			if (p_data == null || p_data.length == 0){
				throw new Exception("invalid data");
			}
			if (p_ivBytes != null && p_ivBytes.length != 16){
				throw new Exception("invalid iv");
			}
			if (debug){
				if (p_key.length == 16){
					UniLog.logm(null, "encryption algo: AES 128bit");
				}
				else if (p_key.length == 24){
					UniLog.logm(null, "encryption algo: AES 192bit");
				}
				else if (p_key.length == 32){
					UniLog.logm(null, "encryption algo: AES 256bit");
				}
			}
			
			//create iv
			byte[] ivBytes = p_ivBytes;
			if (ivBytes == null){
				ivBytes = new byte[16];
				SecureRandom random = new SecureRandom();
				random.nextBytes(ivBytes); 
			}
			IvParameterSpec iv = new IvParameterSpec(ivBytes);
			if (debug){
				UniLog.logm(null,"debug: iv(hex)=" + Hex.encodeHexString(ivBytes));
			}
			
			//create aes chipher 
			SecretKeySpec skeySpec = new SecretKeySpec(p_key, ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
			
			//create 256bit hash (hex string format)
			byte[] hashBytes = null;
			if (p_hashFlag){
				MessageDigest md = MessageDigest.getInstance("SHA-256");  //32byte 
				md.update(p_data);
				hashBytes = md.digest();
				if (hashBytes == null || hashBytes.length != 32){
					throw new Exception("invalid hash");
				}
				if (debug){
					UniLog.logm(null, "debug: hash hex:%s", Hex.encodeHexString(hashBytes));
				}
			}
			
			//encrypt data
			byte[] eData = null;
			eData = p_hashFlag ? cipher.doFinal(ArrayUtils.addAll(hashBytes,p_data)) : cipher.doFinal(p_data);	
			if (debug){
				UniLog.logm(null, "debug: eData base64:%s", Base64.encodeBase64String(eData));
			}
			byte[] eDataWithIv = ArrayUtils.addAll(ivBytes, eData);
			return(eDataWithIv);
		} 
		catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/***
	 * 
	 * @param p_key - 16byte aes128 / 32byte aes256
	 * @param p_eDataWithIv - encrypted data - (16byte iv + base64 encrypted data) 
	 * @param p_hashFlag - data contain hash or not
	 * @return decrypted data. null if sth wrong
	 *                 
	 */
	public static byte[] decrypt(byte[] p_key, byte[] p_eDataWithIv, boolean p_hashFlag) {
		try {
			if (p_key == null || (p_key.length != 16 && p_key.length != 24 && p_key.length != 32)){
				throw new Exception("invalid key");
			}
			//decode base64string
			//byte[] eDataWithIv = org.apache.commons.codec.binary.Base64.decodeBase64(p_eDataWithIvString);
			
			//extract iv
			if (p_eDataWithIv == null || p_eDataWithIv.length < 16 ){ //16byte iv check
				UniLog.log1("keylen:%d datalen:%d", p_key == null ? 0:p_key.length, p_eDataWithIv == null ? 0 : p_eDataWithIv.length);
				throw new Exception("data too short - iv check fail");
			}
			byte[] ivBytes = ArrayUtils.subarray(p_eDataWithIv, 0, 16);  //16byte / 128bit
			IvParameterSpec iv = new IvParameterSpec(ivBytes);
			SecretKeySpec skeySpec = new SecretKeySpec(p_key, ALGORITHM);
			if (debug){
				UniLog.logm(null,"debug: iv(hex)=" + Hex.encodeHexString(ivBytes));
			}

			//create chipher
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
			
			//decode eData
			byte[] dataBytes = null;
			byte[] dataWithHash = cipher.doFinal(ArrayUtils.subarray(p_eDataWithIv, 16, p_eDataWithIv.length));
			if (p_hashFlag){
				if (dataWithHash == null || dataWithHash.length < 32){
					throw new Exception("data too short - hash check fail");
				}
				byte[] hashBytes = ArrayUtils.subarray(dataWithHash, 0, 32);  //32byte / 256bit
				dataBytes = ArrayUtils.subarray(dataWithHash, 32, dataWithHash.length);
				
				//calculate hash
				MessageDigest md = MessageDigest.getInstance("SHA-256");  //32byte 
				md.update(dataBytes);
				byte[] calHashBytes = md.digest();
				
				//compare hash
				if (debug){
					UniLog.logm(null, "debug: check hash:%s:%s", Hex.encodeHexString(hashBytes), Hex.encodeHexString(calHashBytes));
				}
				if (!Arrays.equals(hashBytes,calHashBytes)){
					throw new Exception("hash does not match");
				}
			}
			else{
				dataBytes = dataWithHash;
			}
			return (dataBytes);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return(null);
		}
	}
	
	private static void selfTest() throws Exception{
		byte[] keys = "zsk2kxap235zsk2kxap235cs8xnpp22-".getBytes("UTF-8");   //256 bit
		//byte[] keys = "xap235cs8xnpp22-".getBytes("UTF-8");  //128bit
		
		//test case: simple encoding/decoding
		String eString = encryptToBase64(keys, "aaaa This is a cat Not lunch eat lunch hahahahaha 1234567890 This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890This is a cat Not lunch eat lunch hahahahaha 1234567890 xxxx".getBytes("UTF-8"), true);
		UniLog.logm(null, "eString:[%s]", eString);
		UniLog.logm(null, "decrypt:[%s]", new String(decryptFromBase64(keys,  eString), "UTF-8"));
		
		//test case: jost encoding/decoding
		JSONObject json = new JSONObject();
        String out_trade_no = "myuid_201808030004";
        String subject = "item name";
        String total_fee = "2.00";
        String body = "item description";
        String currency = "USD";
		
		json.put("out_trade_no", "myuid_201808030005");
		json.put("subject", "my item name");
		json.put("total_fee", "2.00");
		json.put("body", "my item desc");
		json.put("currency", "USD");
		
		String jsonStr = encryptToBase64(keys, json.toString().getBytes("UTF-8"),true);
		UniLog.log("json:" +  json.toString());
		UniLog.log("ejson:" +  jsonStr);
		
		UniLog.log("djson:" +  new String(decryptFromBase64(keys,jsonStr),"UTF-8"));
		JSONObject dJson = new JSONObject(new String(decryptFromBase64(keys,jsonStr),"UTF-8"));
		UniLog.log("1:" + dJson.get("out_trade_no"));
		UniLog.log("2:" + dJson.get("subject"));
		UniLog.log("3:" + dJson.get("total_fee"));
		UniLog.log("4:" + dJson.get("body"));
		UniLog.log("5:" + dJson.get("currency"));
		
		
		//test case: pdf encrypt/decrypt (byte)
		UniLog.log("encrypt to byte");
		FileUtils.writeByteArrayToFile(
				new File("/tmp/a.pdf.enc"),
				encrypt(keys, IOUtils.toByteArray(new FileInputStream("/tmp/a.pdf")), null, true),
				false);
		
		UniLog.log("decrypt from byte");
		FileUtils.writeByteArrayToFile(
				new File("/tmp/a.decrypted_from_byte.pdf"),
				decrypt(keys, FileUtils.readFileToByteArray(new File("/tmp/a.pdf.enc")),true));
		
		
		//test case: pdf encrypt/decrypt (base64)
		UniLog.log("encrypt to b64");
		FileUtils.writeStringToFile(
				new File("/tmp/a.pdf.encb64"),
				encryptToBase64(keys, IOUtils.toByteArray(new FileInputStream("/tmp/a.pdf")), true),
				"UTF-8",
				false);
		
		UniLog.log("decrypt from b64");
		FileUtils.writeByteArrayToFile(
				new File("/tmp/a.decrypted_from_b64.pdf"), 
			    decryptFromBase64(keys,FileUtils.readFileToString(new File("/tmp/a.pdf.encb64"),"UTF-8")),
			    false);
	}
	public static void main(String[] args) throws Exception{
		selfTest();
	}
}
