package com.uniinformation.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.google.zxing.BarcodeFormat;
//import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import com.warrenstrange.googleauth.KeyRepresentation;
import com.warrenstrange.googleauth.GoogleAuthenticatorConfig.GoogleAuthenticatorConfigBuilder;

public class TOTPUtil {
	/***
	 * generate a key for google authenticator
	 * @return
	 */
	public static Map<String,String> createTOTP(String p_issuer, String p_accountName) {
		//remark: is it really require to use the lib? or just simply generate secret in base32??

		GoogleAuthenticatorKey key = getGA().createCredentials();
		String secret = key.getKey();
		/*
		//scratch code for account recovery. we don't need it
        List<Integer> scratchCodes = key.getScratchCodes();
        for (Integer i : scratchCodes) {
            UniLog.log1("Scratch code: " + i);
        }
		*/

		//qrcode format: otpauth://totp/MY_ISSUER:MY_ACCOUNT?secret=MY_SECRET&issuer=MY_ISSUER
		
		String otpAuthURL = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(p_issuer, p_accountName, key);
		UniLog.log1("secret: %s", secret);
		//UniLog.log1("otpAuthURL: %s", StringUtil.urldecode(otpAuthURL));
		UniLog.log1("otpAuthURL: %s", otpAuthURL);
		try {
			byte[] imgBytes = QRCodeUtil.createQRCode(otpAuthURL, 200, 200, "PNG");
			String imgStr = Base64Util.convertToImgString(imgBytes, "PNG");
			UniLog.log1("imgStr:%s", imgStr);
			return MapUtil.of("secret", secret, "otpAuthImg", imgStr);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}

	/***
	 * 
	 * @param p_secret
	 * @param p_password
	 */
	public static boolean validatePassword(String p_secret, int p_password) {
		if (StringUtils.isBlank(p_secret)) {
			UniLog.log1("secret is blank, bypass checking");
			return true;
		}
		if (p_password <= 0) {
			UniLog.log1("invalid password");
			return false;
		}
		boolean result = getGA().authorize(p_secret, p_password);
		UniLog.log1("secret:%s password:%d result:%s", p_secret, p_password, result);
		return result;
	}
	
	public static String encryptSecret(String p_secret, byte[] p_aesKey) throws Exception{
		return CryptoUtil.encryptToBase64(p_aesKey, p_secret.getBytes("UTF-8"), true);
	}
	

	private static GoogleAuthenticator getGA() {
		return new GoogleAuthenticator( 
				new GoogleAuthenticatorConfigBuilder()
				.setKeyRepresentation(KeyRepresentation.BASE32)  //key format in base32/64, google authenticator require base32
				.setCodeDigits(6)  //password lenght, default 6
				.setTimeStepSizeInMillis(TimeUnit.SECONDS.toMillis(30))  //adjust timestep, default value 30
				.setWindowSize(3)  // windows size * timeStepSizeInMillis that are checked during the validation process, default 3
				.setNumberOfScratchCodes(0)  //for recovery,
				.build()
				);

	}
	/***
	 * generate a one time password (simulate client side password)
	 * @param p_secret
	 * @return
	 */
	public static int genPassword(String p_secret) {
		return getGA().getTotpPassword(p_secret);
	}
	
	public static void main(String args[]) {
		Map<String,String> resultMap = TOTPUtil.createTOTP("testing", "haha@haha.com");
		String secret = MapUtil.getString(resultMap, "secret");
		//int password = TOTPUtil.genPassword(secret);
		for (int i=0; i<10; i++) {
			int password = TOTPUtil.genPassword(secret);
			TOTPUtil.validatePassword(secret, password);
			try { Thread.sleep(100); }catch(Exception ex) {}
		}
	}
}
