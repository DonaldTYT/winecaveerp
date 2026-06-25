package com.kyoko.crypto;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketTimeoutException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.kyoko.common.StringUtil;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.IniHelper;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

import okhttp3.Credentials;
import okhttp3.FormBody;
import okhttp3.FormBody.Builder;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.ByteString;

public class SHA256withRSA {
	//private static String TEST_AGENT = "propertymgmt";
	private static String TEST_AGENT = null;
	private static final Map<String, String> taifungStatusMap = MapUtil.of("0", "已发送", "1", "待确认", "2", "成功", "3", "失败", "4", "已撤销", "5", "已冲正", "6", "已退货");
	private static final Map<String, String> bocpayStatusMap = MapUtil.of("S", "交易成功", "F", "交易失敗", "A", "等待付款", "Z", "交易未明", "W", "退款中", "D", "已撤销");
	private static final Map<String, Map<String, String>> taifungH5StatusMap = MapUtil.of(
		"TFPAY006", MapUtil.of("0", "成功", "1", "失敗", "2", "處理中"),
		"TFPAY002", MapUtil.of("0", "支付成功", "1", "支付失敗", "2", "處理中（支付中）", "3", "已撤銷（已關閉）", "4", "撤銷中"),
		"TFPAY003", MapUtil.of("0", "退款成功", "1", "退款失敗", "2", "處理中"),
		"TFPAY004", MapUtil.of("0", "退款成功", "1", "退款失敗", "2", "處理中"),
		"TFPAY007", MapUtil.of("0", "退款成功", "1", "退款失敗", "2", "退款中"),
		"TFPAY005", MapUtil.of("0", "撤銷成功", "1", "撤銷失敗", "2", "撤銷中"));

	public static PublicKey loadPublicKey(String str) throws Exception {
	    byte[] publicKeyBytes = Base64.getDecoder().decode(str);
	    X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
	    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	    return keyFactory.generatePublic(keySpec);
	}
	
	public static PrivateKey loadPrivateKey(String str) throws Exception {
	    byte[] privateKeyBytes = Base64.getDecoder().decode(str);
	    PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
	    KeyFactory keyFactory = KeyFactory.getInstance("RSA");
	    return keyFactory.generatePrivate(keySpec);
	}

	static public KeyPair getKeyPair(String algorithm,int keySize) {
		try {
//			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
			keyPairGenerator.initialize(keySize); // Specify the desired key size
			KeyPair keyPair = keyPairGenerator.generateKeyPair();
//			PrivateKey privateKey = keyPair.getPrivate();
//			PublicKey publicKey = keyPair.getPublic();
			return(keyPair);
		} catch ( NoSuchAlgorithmException nex) {
			UniLog.log(nex.toString());
			return(null);
		}
	}
	
	static public byte[] sign(PrivateKey key, byte[] data) {
		return sign("SHA256withRSA", key, data);
	}

	static public byte[] sign(String algorithm, PrivateKey key, byte[] data) {
		try {
			//MessageDigest digest = MessageDigest.getInstance("SHA-256");
			//byte[] hashedData = digest.digest(data);
			Signature signature = Signature.getInstance(algorithm);
			signature.initSign(key);
			//signature.update(hashedData);
			signature.update(data);
			byte[] signedHash = signature.sign();
			return(signedHash);
		} catch ( NoSuchAlgorithmException | InvalidKeyException | SignatureException nex) {
			UniLog.log(nex.toString());
			return(null);
		}
	}

	static public Boolean verify(PublicKey publicKey, byte[] data, byte[] signedHash) {
		return verify("SHA256withRSA", publicKey, data, signedHash);
	}
	
	static public Boolean verify(String algorithm, PublicKey publicKey ,byte[] data,byte[] signedHash) {
		try {
			//MessageDigest digest = MessageDigest.getInstance("SHA-256");
			//byte[] hashedData = digest.digest(data);
			Signature signature = Signature.getInstance(algorithm);
			signature.initVerify(publicKey);
			//signature.update(hashedData);
			signature.update(data);
			boolean isValid = signature.verify(signedHash);	
			return(isValid);
		} catch ( NoSuchAlgorithmException | InvalidKeyException | SignatureException nex) {
			UniLog.log(nex.toString());
			return(null);
		}
	}
	
	public static String generateRandomString(int maxLen) {
		final String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
		final Random r = new Random();
        int length = r.nextInt(maxLen) + 1;
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = r.nextInt(charSet.length());
            sb.append(charSet.charAt(randomIndex));
        }
        return sb.toString();
    }
	
	/*public static SSLContext createInsecureSSLContext() throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        TrustManager[] trustAllCerts = new TrustManager[] {
        		new X509TrustManager() {
        			public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }
        };
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, (chain, authType) -> true)
                .build();
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        return sslContext;
    }
	
	public static Executor createInsecureExecutor() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
        SSLContext sslContext = createInsecureSSLContext();
        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(new SSLConnectionSocketFactory(sslContext, NoopHostnameVerifier.INSTANCE))
                .build();
        return Executor.newInstance(httpClient);
	}*/
	
	public static OkHttpClient createOkHttpClient() throws NoSuchAlgorithmException, KeyManagementException {
        X509TrustManager trustManager = new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }
            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, new TrustManager[]{trustManager}, new java.security.SecureRandom());
		
		/*TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);
		//KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		//try (InputStream is = new FileInputStream("/tmp/cacerts")) {
		//    trustStore.load(is, "changeit".toCharArray());
		//}
		//tmf.init(trustStore);
        TrustManager[] trustManagers = tmf.getTrustManagers();
        X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
		sslContext.init(null, trustManagers, null);*/

        return new OkHttpClient.Builder()
                .sslSocketFactory(sslContext.getSocketFactory(), trustManager)
                .hostnameVerifier((hostname, session) -> true)
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .build();
    }

	public static Map<String, String> formStringToMap(String str) {
        /*return URLEncodedUtils.parse(str, StandardCharsets.UTF_8).stream()
       			.collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue, (existingValue, newValue) -> newValue, TreeMap::new
		));*/
		return Arrays.stream(str.split("&"))
	            .map(pair -> pair.split("=", 2))
	            .filter(arr -> arr.length > 0)
	            .collect(Collectors.toMap(
	                arr -> arr[0],
	                arr -> arr.length > 1 ? arr[1] : "",
	                (oldValue, newValue) -> oldValue + ";" + newValue,
	                TreeMap::new
	            ));
	}
	
	public static String mapToFormString(Map<String, String> m) {
		return m.entrySet().stream().map(entry -> entry.getKey() + "=" + urlEncode(entry.getValue())).collect(Collectors.joining("&"));
	}
	
	public static void decodeMapValues(Map<String, String> m) {
		m.entrySet().forEach(entry -> m.put(entry.getKey(), urlDecode(entry.getValue())));
	}
	
	public static String joinMapValues(Map<String, String> m) {
		return m.values().stream().collect(Collectors.joining());
	}
	
	public static FormBody mapToFormBody(Map<String, String> m) {
		Builder builder = new FormBody.Builder();
		m.entrySet().forEach(entry -> builder.add(entry.getKey(), entry.getValue()));
		return builder.build();
	}
	
	public static String urlDecode(String str) {
		try {
			return URLDecoder.decode(str, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			return str;
		}
	}
	
	public static String urlEncode(String str) {
		try {
			return URLEncoder.encode(str, StandardCharsets.UTF_8.name());
		} catch (Exception e) {
			UniLog.log(e);
			return str;
		}
	}
	
	public static boolean isProbablyUtf8(Buffer buffer) {
        try {
            ByteString sample = buffer.snapshot().substring(0, (int)Math.min(1024, buffer.size()));
            // 检查是否包含非 ASCII 控制字符（如 0x00-0x1F，排除 \r、\n、\t）
            for (byte b : sample.toByteArray()) {
                if (b < 0x20 && b != 0x0A && b != 0x0D && b != 0x09)
                    return false;
            }
            sample.utf8();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
	
	public static boolean taifungPayment(Map<String, String> p_map, SessionHelper sh) {
		try {
	        PublicKey publicKey = loadPublicKey(getIniString(sh, "taifung_public_key"));
			PrivateKey privateKey = loadPrivateKey(getIniString(sh, "taifung_private_key"));
			PublicKey systemPublicKey = loadPublicKey(getIniString(sh, "taifung_system_key"));

	        Gson gson = new Gson();
	        Map<String, String> m = new TreeMap<>(p_map);
	        m.put("mch_id", getIniString(sh, "taifung_mch_id"));
	        m.put("org_code", getIniString(sh, "taifung_org_code"));
	        m.put("nonce_str", generateRandomString(32));
	        if (StringUtils.equals(m.get("service"), "pay.qrcode.micropay")) {
	        	m.put("term_no", getIniString(sh, "taifung_term_no"));
	        	m.put("body", StringUtils.defaultIfBlank(p_map.get("body"), "EPayment"));
	        }
	        if (StringUtils.equalsAny(m.get("service"), "pay.qrcode.micropay", "pay.qrcode.cancel"))
	        	m.put("mch_create_ip", getIniString(sh, "taifung_mch_create_ip"));

	        String json = gson.toJson(m);
	        UniLog.log1("json:%s", json);
	        
	        byte[] signatureData = sign(privateKey, json.getBytes());
	        String signatureStr = Base64.getEncoder().encodeToString(signatureData);
	        UniLog.log1("signatureStr:%s", signatureStr);

	        if (!verify(publicKey, json.getBytes(), signatureData))
	        	throw new Exception("signature data verify failed");
	        
	        m.put("sign", signatureStr);
	        json = gson.toJson(m);
	        
            /*String content = createInsecureExecutor().execute(Request.Post("https://tf-api.xgdmo.com:14055/api")
                            .bodyString(json, ContentType.APPLICATION_JSON))
            				.returnContent().asString();
            UniLog.log1("content:%s", content);*/

	        String url = getIniString(sh, "taifung_url");
	        UniLog.log1("url:%s", url);
	        Request request = new Request.Builder()
	                .url(url)
	                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json))
	                .build();
	        String content = null;
	        try (Response response = createOkHttpClient().newCall(request).execute()) {
	        	if (!response.isSuccessful())
	        		throw new Exception("request failed:" + response);
	        	content = response.body().string();
	        	UniLog.log1("content:%s", content);
	        }
            
            Map<String, String> rm = gson.fromJson(content, new TypeToken<TreeMap<String, String>>(){}.getType());
            String rtnCode = rm.get("ret_code");
            String status = rm.get("status");
            p_map.put("rtnCode", rtnCode);
            p_map.put("rtnMsg", rm.get(StringUtils.equals(m.get("service"), "pay.qrcode.micropay") ? "ret_msg" : "trans_msg"));
            p_map.put("resultCode", status);
            if (StringUtils.equals(rtnCode, "00"))
            	p_map.put("statusMessage", StringUtils.defaultIfBlank(taifungStatusMap.get(status), status));
            else
            	p_map.put("statusMessage", p_map.get("rtnMsg"));
            if (p_map.get("rtnMsg") == null || p_map.get("rtnMsg").equals("[null]"))
            	p_map.put("rtnMsg", "沒有返回訊息");
            signatureStr = rm.get("sign");
            if (signatureStr != null) {
            	UniLog.log1("signatureStr:%s", signatureStr);
            	signatureData = Base64.getDecoder().decode(signatureStr);
            	rm.remove("sign");
            	json = gson.toJson(rm);
            	if (!verify(systemPublicKey, json.getBytes(), signatureData))
	        		throw new Exception("return data verify failed");
            } else
        		throw new Exception("return signatureStr not found");

            double totalFee = NumberUtils.toLong(rm.get("total_fee")) / 100.0;
            long transTime = System.currentTimeMillis() / 1000;
            String transTimeStr = rm.get("trans_time");
            if (StringUtils.isNotBlank(transTimeStr)) {
            	try {
            		Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(transTimeStr);
            		transTime = d.getTime() / 1000;
            	} catch (Exception e) {
            	}
            }
            p_map.put("transNo", rm.get("transaction_id"));
            p_map.put("totalFee", String.valueOf(totalFee));
            p_map.put("transTime", String.valueOf(transTime));
            return StringUtils.equals(rm.get("ret_code"), "00") && StringUtils.equals(rm.get("status"), "2");
		} catch (SocketTimeoutException e) {
            p_map.put("rtnMsg", "連線逾時");
			UniLog.log(e);
		} catch (Exception e) {
			UniLog.log(e);
		}
		return false;
	}

	public static boolean taifungH5Payment(Map<String, String> p_map, SessionHelper sh) {
		try {
	        PublicKey publicKey = loadPublicKey(getIniString(sh, "taifung_public_key"));
			PrivateKey privateKey = loadPrivateKey(getIniString(sh, "taifung_private_key"));
			PublicKey systemPublicKey = loadPublicKey(getIniString(sh, "taifung_system_key"));

	        Gson gson = new Gson();
	        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
	        LocalDateTime now = LocalDateTime.now();
	        String method = p_map.get("method");
	        Map<String, String> headerMap = new TreeMap<>(MapUtil.of(
        		"appId", "EFP",
        		"requestId", p_map.get("outTradeNo"),
        		"method", method,
        		"timestamp", now.format(dtf)));
	        p_map.remove("method");
	        Map<String, String> payloadMap = new TreeMap<>(p_map);
	        payloadMap.put("supMchId", getIniString(sh, "taifung_mch_id"));
	        if (StringUtils.equals(method, "TFPAY008")) {
	        	payloadMap.putAll(MapUtil.of(
	        		"orderCurrency", StringUtils.defaultIfBlank(p_map.get("orderCurrency"), "446"),
        			"payNotifyUrl", getIniString(sh, "taifung_h5_paynotifyurl"),
        			"outTradeTime", now.format(dtf),
        			"orderActiveTime", now.plusMinutes(5).format(dtf),
        			"dynamicCodeFlag", "0",
        			"onlineFlag", "1"));
	        }
	        if (StringUtils.equalsAny(method, "TFPAY002", "TFPAY004"))
	        	payloadMap.put("searchType", "0");
	        if (StringUtils.equalsAny(method, "TFPAY008", "TFPAY002", "TFPAY003", "TFPAY004", "TFPAY005"))
	        	payloadMap.put("tradeType", "H5");

	        String str = headerMap.get("method") + headerMap.get("timestamp") + DigestUtils.md5Hex(mapToFormString(payloadMap));
	        byte[] data = str.getBytes();
	        UniLog.log1("str:%s", str);
	        byte[] signatureData = sign("MD5withRSA", privateKey, data);
	        String signatureStr = Base64.getEncoder().encodeToString(signatureData);
	        UniLog.log1("signatureStr:%s", signatureStr);
	        if (!verify("MD5withRSA", publicKey, data, signatureData))
	        	throw new Exception("signature data verify failed");
	        headerMap.put("sign", signatureStr);

	        String json = gson.toJson(new TreeMap<>(MapUtil.of("header", headerMap, "payload", payloadMap)));
	        UniLog.log1("json:%s", json);
	        
	        String url = getIniString(sh, "taifung_h5_url");
	        UniLog.log1("url:%s", url);
	        Request request = new Request.Builder()
	                .url(url)
	                .post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json))
	                .build();
	        String content = null;
	        try (Response response = createOkHttpClient().newCall(request).execute()) {
	        	if (!response.isSuccessful())
	        		throw new Exception("request failed:" + response);
	        	content = response.body().string();
	        	UniLog.log1("content:%s", content);
	        }

            Map<String, Map<String, String>> rm = gson.fromJson(content, new TypeToken<TreeMap<String, TreeMap<String, String>>>(){}.getType());
            headerMap = rm.get("header");
            Map<String, String> resultMap = rm.get("result");
            String errorCode = headerMap.get("errorCode");
            String errorMsg = headerMap.get("errorMsg");
            String errCode = resultMap.get("errCode");
            String errMsg = resultMap.get("errMsg");
            String status = resultMap.get("transStatus");
            p_map.putAll(MapUtil.of(
            	"rtnCode", errorCode,
            	"rtnMsg", StringUtils.defaultIfEmpty(StringUtils.equalsAny(method, "TFPAY002", "TFPAY006", "TFPAY007") ? errMsg : errorMsg, "沒有返回訊息"),
            	"resultCode", status));
            if (!StringUtils.equals(errorCode, "0"))
            	p_map.put("statusMessage", errorMsg);
            else if (!StringUtils.equals(errCode, "0") && StringUtils.isNotBlank(errCode))
            	p_map.put("statusMessage", errMsg);
            else
            	p_map.put("statusMessage", StringUtils.defaultIfBlank(taifungH5StatusMap.get(method).get(status), status));

            signatureStr = headerMap.get("sign");
            if (signatureStr != null) {
            	UniLog.log1("signatureStr:%s", signatureStr);
            	signatureData = Base64.getDecoder().decode(signatureStr);
            	str = headerMap.get("method") + headerMap.get("timestamp") + DigestUtils.md5Hex(mapToFormString(resultMap));
            	UniLog.log1("str:%s", str);
            	if (!verify("MD5withRSA", systemPublicKey, str.getBytes(), signatureData))
	        		throw new Exception("return data verify failed");
            } else
        		throw new Exception("return signatureStr not found");

            double totalFee = NumberUtils.toLong(resultMap.containsKey("payAmt") ? resultMap.get("payAmt") : resultMap.get("orderAmt")) / 100.0;
            long transTime = System.currentTimeMillis() / 1000;
            try {
            	transTime = (LocalDateTime.parse(resultMap.containsKey("timeEnd") ? resultMap.get("timeEnd") : headerMap.get("timestamp"), dtf).atZone(ZoneId.systemDefault()).toEpochSecond());
            } catch (Exception e) {
            	UniLog.log(e);
            }
            
            p_map.putAll(MapUtil.of(
           		"prepayURL", resultMap.get("prepayURL"),
           		"transNo", resultMap.get("bankSerialNo"),
           		"totalFee", String.valueOf(totalFee),
           		"transTime", String.valueOf(transTime)));
            return StringUtils.equals(headerMap.get("errorCode"), "0") && (StringUtils.equals(method, "TFPAY008") || StringUtils.equals(resultMap.get("transStatus"), "0"));
		} catch (SocketTimeoutException e) {
            p_map.put("rtnMsg", "連線逾時");
			UniLog.log(e);
		} catch (Exception e) {
			UniLog.log(e);
		}
		return false;
	}
	

	public static boolean bocpayPayment(Map<String, String> p_map, SessionHelper sh) {
		try {
	        PublicKey publicKey = loadPublicKey(getIniString(sh, "bocpay_public_key"));
	        PrivateKey privateKey = loadPrivateKey(getIniString(sh, "bocpay_private_key"));
			PublicKey systemPublicKey = loadPublicKey(getIniString(sh, "bocpay_system_key"));

			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	        Map<String, String> m = new TreeMap<>(p_map);
	        m.put("version", getIniString(sh, "bocpay_version"));
	        m.put("merchantId", getIniString(sh, "bocpay_merchantId"));
	        m.put("trmNo", getIniString(sh, "bocpay_trmNo"));
	        if (StringUtils.equalsAny(m.get("service"), "B2CPay", "C2BPay", "EMVC2BPay", "OrderCancel", "OrderRefund"))
	        	m.put("payOrderNo", m.get("requestId"));
	        if (StringUtils.equalsAny(m.get("service"), "B2CPay", "C2BPay", "EMVC2BPay", "OfflineResult")) {
	        	Date d = new Date();
	        	m.put("ordDt", new SimpleDateFormat("yyyyMMdd").format(d));
	        	m.put("ordTm", new SimpleDateFormat("HHmmss").format(d));
	        }
	        if (StringUtils.equalsAny(m.get("service"), "B2CPay", "C2BPay", "EMVC2BPay"))
	        	m.put("subject", StringUtils.defaultIfBlank(p_map.get("subject"), "EPayment"));
	        if (StringUtils.equalsAny(m.get("service"), "C2BPay", "EMVC2BPay"))
	        	m.put("valNum", "300");
	        if (StringUtils.equals(m.get("service"), "OrderQuery"))
	        	m.put("qryNo", m.get("requestId"));
	        if (StringUtils.equalsAny(m.get("service"), "OrderCancel"))
	        	m.put("revOrderNo", m.get("requestId") + "C");
            UniLog.log1("m:%s", gson.toJson(m));
			boolean useUrl2 = StringUtils.equalsAny(m.get("service"), "Statement", "Settlement", "BRSSFileDownLoad");
	        
	        String valueStr = joinMapValues(m);
	        UniLog.log1("valueStr:%s", valueStr);
	        byte[] signatureData = sign(privateKey, valueStr.getBytes());
	        String signatureStr = Base64.getEncoder().encodeToString(signatureData);
	        UniLog.log1("signatureStr:%s", signatureStr);
	        if (!verify(publicKey, valueStr.getBytes(), signatureData))
	        	throw new Exception("signature data verify failed");
	        m.put("merchantSign", signatureStr);
	        
	        String url = getIniString(sh, useUrl2 ? "bocpay_url2" : "bocpay_url");
	        UniLog.log1("url:%s", url);
	        Request request = new Request.Builder()
	                .url(url)
	                //.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded; charset=UTF-8"), postStr))
	                .post(mapToFormBody(m))
	                .build();
	        String content = null;
            Map<String, String> rm = null;
	        try (Response response = createOkHttpClient().newCall(request).execute()) {
	        	if (!response.isSuccessful())
	        		throw new Exception("request failed:" + response);
	        	content = response.body().string();
            	UniLog.log1("content:%s", content);
            	rm = formStringToMap(content);
            	if (useUrl2) {
            		decodeMapValues(rm);
            		UniLog.log1("rm:%s", gson.toJson(rm));
            		return !rm.containsKey("rspCode");
            	}
	        }

	        String returnCode = urlDecode(rm.get("returnCode"));
	        String returnMessage = urlDecode(rm.get("returnMessage"));
	        String result = urlDecode(rm.get("result"));
	        String resultMessage = urlDecode(rm.get("resultMessage"));
            p_map.put("rtnCode", returnCode);
            p_map.put("rtnMsg", returnMessage);
            p_map.put("resultCode", result);
            p_map.put("resultMessage", resultMessage);
            p_map.put("statusMessage", StringUtil.defaultStringIfBlank(resultMessage, bocpayStatusMap.get(result), result, returnMessage, returnCode));
           	UniLog.log1("statusMessage:%s", p_map.get("statusMessage"));
            signatureStr = rm.get("serverSign");
            if (signatureStr != null) {
            	UniLog.log1("signatureStr:%s", signatureStr);
            	signatureData = Base64.getDecoder().decode(signatureStr);
            	rm.remove("serverSign");
            	valueStr = joinMapValues(rm);
            	UniLog.log1("valueStr:%s", valueStr);
            	if (!verify(systemPublicKey, valueStr.getBytes(), signatureData))
	        		throw new Exception("return data verify failed");
            } else
        		throw new Exception("return signatureStr not found");
           	decodeMapValues(rm);
           	UniLog.log1("rm:%s", gson.toJson(rm));
            
            double totalFee = NumberUtils.toLong(rm.get("amount")) / 100.0;
            long transTime = System.currentTimeMillis() / 1000;
            p_map.put("transNo", rm.get("logNo"));
            p_map.put("totalFee", String.valueOf(totalFee));
            p_map.put("transTime", String.valueOf(transTime));
            p_map.put("valTime", rm.get("valTime"));
            p_map.put("payType", rm.get("payType"));
            p_map.put("prepayURL", rm.get("payCode"));
            return StringUtils.equals(rm.get("result"), "S");
		} catch (SocketTimeoutException e) {
            p_map.put("resultMessage", "連線逾時");
			UniLog.log(e);
		} catch (Exception e) {
			UniLog.log(e);
		}
		return false;
	}

	public static boolean reconPayment(Map<String, String> p_map, SessionHelper sh) {
		try {
			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	        Map<String, String> m = new TreeMap<>(p_map);
	        String method = m.remove("method");
	        String cardNo = m.get("cardNo");
	        m.putAll(MapUtil.of(
        		"merCode", "",
        		"ver", "1"
       		));
	        if (StringUtils.equalsAny(method, "b2bPay", "refund") && StringUtils.equalsAny(m.get("payType"), "alipay", "alipay_hk"))
	        	m.put("termId", "");
	        if (StringUtils.equals(method, "b2bPay")) {
	        	m.putAll(MapUtil.of(
	        		"curr", StringUtils.defaultIfBlank(p_map.get("curr"), "HKD"),
	        		"desc", StringUtils.defaultIfBlank(p_map.get("desc"), "EPayment"),
	        		"payGroup", StringUtils.defaultIfBlank(p_map.get("payGroup"), "payGroup1")
       			));
	        }
	        m.putAll(MapUtil.of(
	        	"sign", DigestUtils.sha256Hex(mapToFormString(m) + "&" + "9c9948ab-524e-437e-927b-f8cb2309da73"),
        		"signType", "SHA-256"
       		));
            UniLog.log1("m:%s", gson.toJson(m));

	        String url = "https://secure-uat.reconpayment.com" + "/ws/" + method;
	        UniLog.log1("url:%s", url);
	        Request request = new Request.Builder().url(url).post(mapToFormBody(m)).build();
	        String content = null;
	        try (Response response = createOkHttpClient().newCall(request).execute()) {
	        	if (!response.isSuccessful())
	        		throw new Exception("request failed:" + response);
	        	content = response.body().string();
            	UniLog.log1("content:%s", content);
	        }

            Map<String, String> rm = gson.fromJson(content, new TypeToken<TreeMap<String, String>>(){}.getType());
            p_map.putAll(MapUtil.of(
            	"rtnCode", rm.get("state"),
            	"rtnMsg", StringUtils.defaultString(rm.get("resMsg"), "Message no return"),
	            "resultCode", rm.get("state")
          	));

           	String panHash = rm.get("panHash");
            if (StringUtils.isNotBlank(cardNo) && StringUtils.isNotBlank(panHash)) {
            	String srcCardhash = DigestUtils.sha256Hex(cardNo);
            	if (!StringUtils.equals(srcCardhash, panHash))
            		throw new Exception("CardNo hash no match");
            }

            p_map.putAll(MapUtil.of(
           		"transNo", rm.get("payRef"),
           		"totalFee", String.valueOf(NumberUtils.toLong(rm.get("amt")) / 100.0),
	            "transTime", String.valueOf(System.currentTimeMillis() / 1000),
	            "payType", rm.get("payType")
       		));
            return StringUtils.equals(rm.get("state"), "1");
		} catch (SocketTimeoutException e) {
            p_map.put("rtnMsg", "連線逾時");
			UniLog.log(e);
		} catch (Exception e) {
			UniLog.log(e);
		}
		return false;
	}
	
	public static boolean downloadFile(String url, String username, String password, String savePath) {
		try {
	        Request request = new Request.Builder()
	                .url(url)
	                .header("Authorization", Credentials.basic(username, password))
	                .build();
	        try (Response response = createOkHttpClient().newCall(request).execute()) {
	        	if (!response.isSuccessful())
	        		throw new Exception("request failed:" + response);
	        	try (InputStream inputStream = response.body().byteStream(); OutputStream outputStream = new FileOutputStream(savePath)) {
	                byte[] buffer = new byte[4096];
	                int bytesRead;
	                while ((bytesRead = inputStream.read(buffer)) != -1)
	                	outputStream.write(buffer, 0, bytesRead);
	                UniLog.log("download done");
	        	}
	        }
		} catch (SocketTimeoutException e) {
			UniLog.log(e);
		} catch (Exception e) {
			UniLog.log(e);
		}
		return false;
	}

	private static void taifungH5PaymentTest() {
        Map<String, String> m = new HashMap<>();
        m.put("method", "TFPAY002");
        m.put("outTradeNo", "x001");
        m.put("searchType", "0");
        UniLog.log1("ok:%b", taifungH5Payment(m, null));
	}
	
	private static void taifungPaymentTest() {
        Map<String, String> m = new HashMap<>();
        //m.put("service", "pay.qrcode.micropay");
        m.put("service", "pay.qrcode.chnquery");
        //m.put("service", "pay.api.query.bill");
        /*m.put("agent_no", "50264265");
        m.put("settle_date", "20250328");*/
        m.put("out_trade_no", "EP0000000058");
        /*m.put("out_trade_no", "x009");
        m.put("total_fee", "1000");
        m.put("body", "Test Payment");
        m.put("auth_code", "33430972390796841963");*/
        UniLog.log1("ok:%b", taifungPayment(m, null));
	}
	
	public static void bocpayPaymentTest() {
        Map<String, String> m = new HashMap<>();
        m.put("requestId", "x022");
        /*m.put("service", "B2CPay");
        m.put("amount", "100");
        m.put("authCode", "991278224336852154964");
        m.put("subject", "Test Payment");*/
        //m.put("service", "OrderQuery");
        //m.put("service", "OrderCancel");
        m.put("service", "C2BPay");
        m.put("amount", "1000");
        /*m.put("service", "Settlement");
        m.put("acDate", "20250418");*/
        UniLog.log1("ok:%b", bocpayPayment(m, null));
	}

	public static void reconPaymentTest() {
        Map<String, String> m = new HashMap<>(MapUtil.of(
       		"method", "b2bPay",
        	"merRef", "x001",
       		"amt", "100",
        	"cardNo", "xxx",
        	"cardExp", "202507",
        	"payType", "wechat"
     	));
        UniLog.log1("ok:%b", reconPayment(m, null));
	}

	private static IniHelper iniHelper;
	public static String getIniString(SessionHelper sh, String key) throws Exception {
		if (TEST_AGENT != null) {
			if (iniHelper == null)
				iniHelper = new IniHelper("/eclipse_dev/unidev/src/erpv4config.ini", null, TEST_AGENT);		
				//iniHelper = new IniHelper("/usr/app/tomcat/webapps/propertymgmt/WEB-INF/classes/erpv4config.ini", null, TEST_AGENT);		
			return iniHelper.getString(key, "");
		} else
			return Erpv4Config.getString(sh, key, "");
	}
	
	public static void main(String args[]) throws Exception {
		/*UniLog.log("TestSign");
		KeyPair kp = getKeyPair("RSA",2048);
		//UniLog.log("Private Key: "+kp.getPrivate().toString());
		
        UniLog.log("Private Key: " + Base64.getEncoder().encodeToString(kp.getPrivate().getEncoded()));

//        UniLog.log("Public Key: " + Base64.getEncoder().encodeToString(kp.getPublic().getEncoded()));
        byte[] encodedBytes = kp.getPublic().getEncoded();
        String encodedString = Base64.getEncoder().encodeToString(encodedBytes);
        UniLog.log("Public Key: " + encodedString);
	
		byte[] signedHash = sign(kp.getPrivate(),"ABCDE".getBytes());
		String signedString = Base64.getEncoder().encodeToString(signedHash);
 		UniLog.log("Signed : "+ signedString);
 		
		
 		
 		UniLog.log("Verifying");
 		encodedBytes = Base64.getDecoder().decode(encodedString);
 		try {
 		KeyFactory kf = KeyFactory.getInstance("RSA");
 			PublicKey pk = kf.generatePublic(new X509EncodedKeySpec(encodedBytes));	
 			UniLog.log("Verify : "+ verify(pk,"ABCDE".getBytes(), signedHash));
		} catch ( Exception nex) {
 			UniLog.log(nex);
 		}
 				
// 		UniLog.log("Verify : "+ verify(kp.getPublic(),"ABCDE".getBytes(), signedHash));
		//downloadFile("https://svn.hellovoice.com:16081/svn/uniconn_repo/public/pmsMobile-debug-250307.apk", "", "", "/tmp/test.apk");*/
 		//taifungH5PaymentTest();	
		//bocpayPaymentTest();
		reconPaymentTest();
	}
	
    private static final int BUFFER_SIZE = 8 * 1024; // 8 KB
    
    public static DigestInputStream newDigestInputStream(InputStream p_is) {
        try {
        	MessageDigest md = MessageDigest.getInstance("SHA-256");
    	  	DigestInputStream dis = new DigestInputStream(p_is, md);
    	  	return(dis);
        } catch (NoSuchAlgorithmException e) {
            // Should never happen for SHA-256 on a normal JDK
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    public static byte[] sha256Hex(InputStream in) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Wrap the InputStream in a DigestInputStream so reads update the digest
            try (DigestInputStream dis = new DigestInputStream(in, md)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                // Read the entire stream
                while (dis.read(buffer) != -1) {
                    // nothing to do, digest is updated automatically
                }
            }

            byte[] digest = md.digest();
            return digest;
        } catch (NoSuchAlgorithmException e) {
            // Should never happen for SHA-256 on a normal JDK
            throw new RuntimeException("SHA-256 not supported", e);
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }
    public static String bytesToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);  // 44 chars for SHA-256
    }	
    public static String bytesToBase64URL(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);  // 44 chars for SHA-256
    }	
}
