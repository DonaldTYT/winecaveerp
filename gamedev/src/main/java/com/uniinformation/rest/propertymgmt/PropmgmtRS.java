package com.uniinformation.rest.propertymgmt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

import javax.annotation.security.PermitAll;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventQueues;

import com.google.gson.reflect.TypeToken;
import com.lowagie.text.Rectangle;

import static com.kyoko.crypto.SHA256withRSA.*;

import com.uniinformation.erpv4.BatchBuildPrintHandler;
import com.uniinformation.rest.RSBase;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.CryptoUtil;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import static com.uniinformation.utils.ZkUtil.throwFunction;
import static com.uniinformation.utils.ZkUtil.throwConsumer;

@Path("/propmgmt")
public class PropmgmtRS extends RSBase {
	public static final byte[] PAYMENT_RECEIPT_KEYS = "zs*acdfe(35zsk2kxap235cs8xnpp22-".getBytes(StandardCharsets.UTF_8);

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

	@GET
	@PermitAll
	@Path("/pr/{voucherno}")
	@Produces("application/pdf")
	public Response downloadPaymentReceipt(@PathParam("voucherno") String voucherNoWrapper) {
		SelectUtil su = null;
		try {
			if (StringUtils.isBlank(voucherNoWrapper))
				return Response.status(Status.BAD_REQUEST).entity("Invalid key").build();
			byte[] voucherNoBytes = CryptoUtil.decryptFromBase64(PAYMENT_RECEIPT_KEYS, voucherNoWrapper, false);
			if (voucherNoBytes == null)
				return Response.status(Status.BAD_REQUEST).entity("Invalid key").build();
			String voucherNo = new String(voucherNoBytes, StandardCharsets.UTF_8);
			if (StringUtils.isBlank(voucherNo))
				return Response.status(Status.BAD_REQUEST).entity("Invalid key").build();
			UniLog.log1("voucherNo:%s", voucherNo);
			SessionHelper sh = ZkSessionHelper.getSessionHelper(request, null);

			//PdfCache pdfCache = PdfCache.getInstance();
			//AtomicReference<byte[]> data = new AtomicReference<>(pdfCache.get(voucherNo));
			AtomicReference<byte[]> data = new AtomicReference<>();
			if (data.get() == null) {
				su = new SelectUtil(); 
				su.init(sh.getLoginTokenJdbcPool());
				SelectUtil su1 = su;
				Stream.of("paymentreceipt", "paymentreceipt2", "paymentreceipt3").map(throwFunction(tb -> 
					ZkUtil.getFirstTableRec(su1, "select col_b from "+tb+" where col_a = ? and col_d like ?", new Wherecl().appendArgument(voucherNo)
									.appendArgument("%" + voucherNoWrapper.substring(voucherNoWrapper.length() - 8) + "%"))
							.map(throwFunction(tr -> (byte[])tr.getField("col_b"))).orElse(null)
				)).filter(Objects::nonNull).findFirst().ifPresent(throwConsumer(d -> {
					try (ByteArrayInputStream bis = new ByteArrayInputStream(d); 
							GZIPInputStream gzis = new GZIPInputStream(bis);
							ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
						IOUtils.copy(gzis, bos);

						Rectangle pageSize = BatchBuildPrintHandler.A5L;
						float docWidthPx = ChnftrParser.dpi100ToPx(760);
						float docHeightPx = ChnftrParser.dpi100ToPx(540);
						int lineHeight = 20;
						ChnftrParser p = new ChnftrParser(bos.toByteArray(), StandardCharsets.UTF_8.name(), pageSize, docWidthPx, docHeightPx, ChnftrParser.CHNFTR_DPI, 11, ChnftrParser.CHNFTR_DPI / lineHeight);
						p.setUseAscender(false);
						data.set(p.printToData());
						//pdfCache.put(voucherNo, data.get());
					}
				}));
				su.close();
			}
			if (data.get() != null) {
				CacheControl cacheControl = new CacheControl();
				cacheControl.setMaxAge(86400);
				StreamingOutput stream = output -> {
					try (ByteArrayInputStream bis = new ByteArrayInputStream(data.get())) {
		                byte[] buffer = new byte[4096];
			            int bytesRead;
			            while ((bytesRead = bis.read(buffer)) != -1)
			                output.write(buffer, 0, bytesRead);
			            output.flush();
		            } catch (Exception e) {
		            	UniLog.log(e);
		            }
		        };
				return Response.ok(stream, "application/pdf").cacheControl(cacheControl).build();
			}
		} catch (Exception e) {
			UniLog.log(e);
			return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
		} finally {
			if (su != null)
				su.close();
		}
		return null;
	}

	@Override
	public String getVersion() {
		return "1.0";
	}

	/*public static class PdfCache {
	    private static final int MAX_CACHE_ENTRIES = 10;
	    
	    private final Cache<String, byte[]> cache;
	    private static final PdfCache INSTANCE = new PdfCache();
	    
	    public static PdfCache getInstance() {
	        return INSTANCE;
	    }
	    
	    private PdfCache() {
	        cache = CacheBuilder.newBuilder()
	                .maximumSize(MAX_CACHE_ENTRIES)
	                //.maximumWeight(MAX_CACHE_SIZE)
	                //.weigher((String voucherNo, byte[] pdfData) -> pdfData.length)
	                .expireAfterAccess(1, TimeUnit.HOURS)
	                .expireAfterWrite(4, TimeUnit.HOURS)
	                .removalListener((RemovalNotification<String, byte[]> notification) -> {
	                	byte[] removedData = notification.getValue();
	                	if (removedData != null)
	                		UniLog.log1("key:%s, reason:%s", notification.getKey(), notification.getCause());
	                })
	                .build();
	    }
	    
	    public byte[] get(String voucherNo) {
	        byte[] data = cache.getIfPresent(voucherNo);
	        if (data != null)
	        	UniLog.log1("PDF %s got from cache", voucherNo);
	        return data;
	    }
	    
	    public void put(String voucherNo, byte[] pdfData) {
	        if (pdfData.length > 5 * 1024 * 1024) {
	            UniLog.log1("PDF %s too big, No cached: %s", voucherNo, pdfData.length / 1024 + "KB");
	            return;
	        }
	        cache.put(voucherNo, pdfData);
	        UniLog.log1("PDF cached: %s, size:%s", voucherNo, pdfData.length / 1024 + "KB");
	    }
	    
	    public void invalidate(String voucherNo) {
	        cache.invalidate(voucherNo);
	        UniLog.log1("Cache expired, voucher no:%s", voucherNo);
	    }
	}*/
}
