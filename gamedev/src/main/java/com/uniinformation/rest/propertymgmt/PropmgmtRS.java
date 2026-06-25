package com.uniinformation.rest.propertymgmt;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;

import com.google.gson.reflect.TypeToken;
import static com.kyoko.crypto.SHA256withRSA.*;
import com.uniinformation.rest.RSBase;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

@Path("/propmgmt")
public class PropmgmtRS extends RSBase {

	@POST
	@PermitAll
	@Path("/pmnotice/taifung")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response taifungPaymentNotice(String content) {
		UniLog.log1("content:%s", content);
		try {
			SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
			PrivateKey privateKey = loadPrivateKey(getIniString(sh, "taifung_private_key"));
			PublicKey systemPublicKey = loadPublicKey(getIniString(sh, "taifung_system_key"));
            Map<String, Map<String, String>> rm = gson.fromJson(content, new TypeToken<TreeMap<String, TreeMap<String, String>>>(){}.getType());
            Map<String, String> headerMap = rm.get("header");
            Map<String, String> payloadMap = rm.get("payload");
            String signatureStr = headerMap.get("sign");
            if (signatureStr != null) {
            	UniLog.log1("signatureStr:%s", signatureStr);
            	byte[] signatureData = Base64.getDecoder().decode(signatureStr);
            	String str = headerMap.get("method") + headerMap.get("timestamp") + DigestUtils.md5Hex(mapToFormString(payloadMap));
            	UniLog.log1("str:%s", str);
            	if (!verify("MD5withRSA", systemPublicKey, str.getBytes(), signatureData))
	        		throw new Exception("Return data verify failed");
            } else
        		throw new Exception("Return signatureStr not found");
            if (!StringUtils.equals(headerMap.get("method"), "TFPAY006"))
        		throw new Exception(String.format("Unknown method '%s'", headerMap.get("method")));
            
	    	Map<String, String> headerMap1 = new TreeMap<>(MapUtil.of(
		    	"appId", headerMap.get("appId"),  
			    "method", headerMap.get("method"),  
			    "requestId", headerMap.get("requestId"),  
			    "timestamp", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")),
			    "errorCode", "0",
			    "errorMsg", ""));
            String str = headerMap1.get("method") + headerMap1.get("timestamp");
            byte[] data = str.getBytes();
            byte[] signatureData = sign("MD5withRSA", privateKey, data);
            signatureStr = Base64.getEncoder().encodeToString(signatureData);
            headerMap1.put("sign", signatureStr);

	        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            double totalFee = NumberUtils.toLong(payloadMap.containsKey("payAmt") ? payloadMap.get("payAmt") : payloadMap.get("orderAmt")) / 100.0;
            int transTime = (int)(System.currentTimeMillis() / 1000);
            try {
            	transTime = (int)(LocalDateTime.parse(payloadMap.containsKey("timeEnd") ? payloadMap.get("timeEnd") : headerMap.get("timestamp"), dtf).atZone(ZoneId.systemDefault()).toEpochSecond());
            } catch (Exception e) {
            	UniLog.log(e);
            }
            Map<String, String> m = MapUtil.of(
            	"out_trade_no", payloadMap.get("outTradeNo"),
           		"transNo", payloadMap.get("bankSerialNo"),
            	"rtnCode", headerMap.get("errorCode"),
            	"rtnMsg", StringUtils.defaultIfEmpty(payloadMap.get("errMsg"), "沒有返回訊息"),
            	"resultCode", payloadMap.get("transStatus"),
           		"totalFee", String.valueOf(totalFee),
           		"transTime", String.valueOf(transTime),
           		"ok", String.valueOf(StringUtils.equals(headerMap.get("errorCode"), "0") && StringUtils.equals(payloadMap.get("transStatus"), "0")));
            EventQueues.lookup("EpaymentNotify", EventQueues.APPLICATION, true).publish(new Event("onTaifungNotify", null, m));

            return Response.ok(gson.toJson(new TreeMap<>(MapUtil.of("header", headerMap1, "result", new TreeMap<>())))).build();
		} catch (Exception e) {
			UniLog.log(e);
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}

	@POST
	@PermitAll
	@Path("/pmnotice/bocpay")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_FORM_URLENCODED)
	public Response bocpayPaymentNotice(String content) {
		UniLog.log1("content:%s", content);
		try {
			SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);
			PublicKey systemPublicKey = loadPublicKey(getIniString(sh, "bocpay_system_key"));
           	Map<String, String> rm = formStringToMap(content);
            String signatureStr = rm.get("serverSign");
	        byte[] signatureData;
	        String valueStr;
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
            if (!StringUtils.equals(rm.get("service"), "OfflineResult"))
        		throw new Exception(String.format("Unknown service '%s'", rm.get("service")));

            Map<String, String> m = MapUtil.of(
            	"out_trade_no", rm.get("mercOrderNo"),
           		"transNo", rm.get("logNo"),
            	"rtnCode", rm.get("returnCode"),
            	"rtnMsg", rm.get("returnMessage"),
            	"resultCode", StringUtils.equals(rm.get("status"), "SUCCESS") ? "S" : "F",
           		"totalFee", String.valueOf(NumberUtils.toLong(rm.get("amount")) / 100.0),
	            "transTime", String.valueOf((int)(System.currentTimeMillis() / 1000)),
	            "payType", rm.get("payType"),
	            "ok", String.valueOf(StringUtils.equals(rm.get("status"), "SUCCESS")));
            EventQueues.lookup("EpaymentNotify", EventQueues.APPLICATION, true).publish(new Event("onBocpayNotify", null, m));

            MultivaluedMap<String, String> responseData = new MultivaluedHashMap<>();
            responseData.add("result", "SUCCESS");
            return Response.ok(responseData).build();
		} catch (Exception e) {
			UniLog.log(e);
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
	}
	
	@Override
	public String getVersion() {
		return "1.0";
	}

}
