package com.uniinformation.winecave;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;

import com.google.gson.Gson;
import com.kikyosoft.utils.DateUtil;
import com.kyoko.common.CoreLog;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.IniHelper;
import com.uniinformation.utils.NumberUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class WineCaveOrderPayPayDollar extends WineCaveOrderPay {
	public final static String CONFIGFILE ="OrderPayPayDollar.properties";
	private final static Properties config = readConfig();
	private String mPayUrl, mAllowPayIPs;
	private String mDataFeedLogFilePrefix;
	private String mOrderRefPrefix;
	private RequestParameters mRequestParameters;

	   private static Properties readConfig() {
		   try {
			  return(IniHelper.loadProperty(null, CONFIGFILE));
		   } catch (Exception ex) {
			   CoreLog.log(ex);
			   return(null);
		   }
	   }
	
	public static class RequestParameters {
		double amount;
		String orderRef;
		String lang;
		String merchantId;
		String currCode;
		String mpsMode;
		String successUrl;
		String failUrl;
		String cancelUrl;
		String payType;
		String payMethod;
		transient Map<Integer, Urls> urlsMap = new HashMap<Integer, Urls>();
		private static class Urls {
			String successUrl;
			String failUrl;
			String cancelUrl;
			Urls(String successUrl, String failUrl, String cancelUrl) {
				this.successUrl = successUrl;
				this.failUrl = failUrl;
				this.cancelUrl = cancelUrl;
			}
		}
		public RequestParameters() {
			readConfigurableParameters();
		}
		public void readConfigurableParameters() {
			merchantId = getProperty("merchantId");
			currCode = getProperty("currCode");
			mpsMode = getProperty("mpsMode");
			payType = getProperty("payType");
			payMethod = getProperty("payMethod");
			urlsMap.clear();
			urlsMap.put(0 /* English */,
					new Urls(getProperty("successUrl"), getProperty("failUrl"), getProperty("cancelUrl")));
			urlsMap.put(1 /* Chinese */, 
					new Urls(getProperty("successChiUrl"), getProperty("failChiUrl"), getProperty("cancelChiUrl")));
			UniLog.log("readConfigurableParameters merchantId:" + merchantId + ",currCode:" + currCode);
		}
		public static long dateTimeToLong(String dateTimeStr) {
			try {
				return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateTimeStr).getTime() / 1000;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return 0;
		}
		public static String cur(String curCode) {
			if (curCode == null)
				return "";
			else if (curCode.equals("344"))
				return "HKD";
			else if (curCode.equals("840"))
				return "USD";
			else if (curCode.equals("156"))
				return "RMB";
			else if (curCode.equals("392"))
				return "JPY";
			else if (curCode.equals("901"))
				return "TWD";
			else if (curCode.equals("702"))
				return "SGD";
			else if (curCode.equals("036"))
				return "AUD";
			else if (curCode.equals("978"))
				return "EUR";
			else if (curCode.equals("826"))
				return "GBP";
			else if (curCode.equals("124"))
				return "CAD";
			else if (curCode.equals("446"))
				return "MOP";
			else if (curCode.equals("608"))
				return "PHP";
			else if (curCode.equals("764"))
				return "THB";
			else if (curCode.equals("458"))
				return "MYR";
			else if (curCode.equals("360"))
				return "IDR";
			else if (curCode.equals("410"))
				return "KRW";
			else if (curCode.equals("682"))
				return "SAR";
			else if (curCode.equals("554"))
				return "NZD";
			else if (curCode.equals("784"))
				return "AED";
			else if (curCode.equals("096"))
				return "BND";
			else if (curCode.equals("704"))
				return "VND";
			else if (curCode.equals("356"))
				return "INR";
			else
				return curCode;
		}
		public String toJson() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}
	}
	private static String getProperty(String p_propname) {
	   return WineCaveConfig.getProperty(config, p_propname);
	}
	public String getDataFeedLogFilePrefix() {
		if (mDataFeedLogFilePrefix == null)
			mDataFeedLogFilePrefix = getProperty("dataFeedLogFilePrefix").trim();
		return mDataFeedLogFilePrefix;
	}
	public String getOrderRefPrefix() {
		if (mOrderRefPrefix == null)
			mOrderRefPrefix = getProperty("orderRefPrefix").trim();
		return mOrderRefPrefix;
	}
	@Override
	public String getPayUrl() {
		if (mPayUrl == null)
			mPayUrl = getProperty("payUrl");
		UniLog.log("getPayUrl:" + mPayUrl);
		return mPayUrl;
	}
	@Override
	public boolean isValidPayIP(String ip) {
		if (mAllowPayIPs == null)
			mAllowPayIPs = getProperty("allowPayIPs");
		return isValidPayIP(mAllowPayIPs, ip);
	}
	@Override
	public void logDatafeed(HttpServletRequest request) {
		String path = getDataFeedLogFilePrefix() + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".log";
		UniLog.log("logDatafeed path:" + path);
		logDatafeed(request, path, "PayDollar Data Feed");
	}
	@Override
	public String getRequestParams(int p_invrg, int p_language,SessionHelper sp) {
		if (mRequestParameters == null) 
			mRequestParameters = new RequestParameters();
		SelectUtil su = null;
		try {
			su = sp.getBiSchema().getSelectUtil();
//			su = new SelectUtil();	
//			su.init(UniDataClientUtil.getJdbcPool(WineCavePageHelper.WINE_CAVE_DATABASE).getConnection());
			TableRec orderTr = su.getQueryResult("select stm_mrg, stm_ref1, stm_date, stm_fref2, stm_discount, sum(-stmd_exprice1) order_total"
															+" from stmov,stmovd"
															+" where stm_type = 'OM' and stm_mrg = "+p_invrg
//															+" and stm_pmmethod = 'Online' "
															+" and stmd_mrg = stm_mrg and stmd_tdtype = 'MO'"
															+" group by stm_mrg, stm_ref1, stm_date, stm_fref2, stm_discount"
															, null
														);
			if (orderTr != null){
				orderTr.setRecPointer(0);
				int invoiceRg = NumberUtils.toInt(orderTr.getField("stm_mrg").toString());
				String date = DateUtil.toDateString(orderTr.getFieldDate("stm_date"), "dd/mm/yyyy");
				double price = NumberUtil.parseDouble(orderTr.getField("order_total").toString().trim());
				double discount =  NumberUtil.parseDouble(orderTr.getField("stm_discount").toString().trim());
				double deliverCharge =  NumberUtil.parseDouble(orderTr.getField("stm_fref2").toString().trim());
				RequestParameters.Urls urls = mRequestParameters.urlsMap.get(p_language);
				mRequestParameters.amount = price - discount + deliverCharge;
//				mRequestParameters.amount = 10.0;
				mRequestParameters.orderRef = String.format("%s%s%010d", getOrderRefPrefix(), new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()), invoiceRg);
				mRequestParameters.lang = p_language == 0 ? "E" : "C";
				
				String erpSO = orderTr.getFieldString("stm_ref1");
				mRequestParameters.successUrl = urls.successUrl+"&order="+erpSO;
				mRequestParameters.failUrl = urls.failUrl+"&order="+erpSO;
				mRequestParameters.cancelUrl = urls.cancelUrl+"&order="+erpSO;
				/*
				mRequestParameters.successUrl = urls.successUrl+"&mrg="+p_invrg;
				mRequestParameters.failUrl = urls.failUrl+"&mrg="+p_invrg;
				mRequestParameters.cancelUrl = urls.cancelUrl+"&mrg="+p_invrg;
				*/
				String s = mRequestParameters.toJson();
				UniLog.log("getRequestParams:" + s);
				return s;
			}
		} catch(Exception ex) {
			UniLog.log(ex);
		} finally {
			if (su != null)
				su.close();
		}
		return "{}";
	}
	@Override
	public String payWebOrder(HttpServletRequest request, String status) {
		String successcode = request.getParameter("successcode");
		UniLog.log("HAHA Pay Order = "+ request + ", status = " + status + ", successcode = " + successcode);
		if (successcode == null || !successcode.trim().equals("0")) {
			if (!status.equals("W"))
				return "FAIL Successcode=" + successcode;
		}
		if (getInvoiceRg(request.getParameter("Ref")) <= 0)
			return "FAIL Invalid order ref:" + request.getParameter("Ref");
		Enumeration<String> paramNames = request.getParameterNames();
		request.getParameterMap();
		Map<String, String> map = new HashMap<String, String>();
		while (paramNames.hasMoreElements()) {
			String name = paramNames.nextElement();
			map.put(name, request.getParameter(name));
		}
		RpcClient perfsvr = new RpcClient(WineCaveConfig.getProperty("rpcServerHost"),Integer.parseInt(WineCaveConfig.getProperty("rpcServerPort")));
		perfsvr.open();
		if (!perfsvr.isConnected()) {
			UniLog.log("payWebOrder Connect to jdbc server error");
			perfsvr.close();
			perfsvr = null;
			return("FAIL Connection Error");
		} else {
			Value val;
			UniLog.log("Pay Order " + request);
			perfsvr.setDebug(true);
			Vector<Object> v = new Vector<Object>();
			v.add("payOrder");
			v.add("PayDollar");
			v.add(status);
			v.add(StringUtils.defaultString(map.get("Ref")));
			if (status.equals("N")) {
				v.add(NumberUtils.toDouble(map.get("Amt")));
				v.add(StringUtils.defaultString(map.get("panFirst4")));
				v.add(StringUtils.defaultString(map.get("channelType")));
				v.add(StringUtils.defaultString(map.get("AlertCode")));
				v.add(StringUtils.defaultString(map.get("AuthId")));
				v.add(StringUtils.defaultString(map.get("eci")));
				v.add(StringUtils.defaultString(map.get("remark")));
				v.add(RequestParameters.cur(map.get("Cur")));
				v.add(StringUtils.defaultString(map.get("payerAuth")));
				v.add(StringUtils.defaultString(map.get("Ord")));
				v.add(NumberUtils.toInt(map.get("PayRef")));
				v.add(NumberUtils.toInt(map.get("prc")));
				v.add(RequestParameters.dateTimeToLong(map.get("TxTime")));
				v.add(StringUtils.defaultString(map.get("Holder")));
				v.add(NumberUtils.toInt(map.get("MerchantId")));
				v.add(StringUtils.defaultString(map.get("sourceIp")));
				v.add(StringUtils.defaultString(map.get("ipCountry")));
				v.add(StringUtils.defaultString(map.get("cardIssuingCountry")));
				v.add(NumberUtils.toInt(map.get("successcode")));
				v.add(NumberUtils.toInt(map.get("src")));
				v.add(StringUtils.defaultString(map.get("panLast4")));
				v.add(StringUtils.defaultString(map.get("payMethod")));
				v.add(StringUtils.defaultString(map.get("secureHash")));
				v.add(StringUtils.defaultString(map.get("airline_ticketNumber")));
			}
			val = perfsvr.callSegment("WineCaveConnection",v);
			UniLog.log("call to rpcserver payOrder got " + val);
			perfsvr.close();
			perfsvr = null;
			if(val != null) return(val.toString()); else return("FAIL Unknown Error");
		}
	}
	@Override
	public int getInvoiceRg(String orderRef) {
		Pattern p = Pattern.compile(getOrderRefPrefix() + "\\d{14}(\\d{10})");
		Matcher m = p.matcher(orderRef);
		if (m.matches())
			return NumberUtils.toInt(m.group(1));
		return 0;
	}
//	public static void main(String args[]){
//		WineCaveOrderPayPayDollar op = new WineCaveOrderPayPayDollar();
//		UniLog.log( ""+op.isValidPayIP("18.140.106.57"));
//		UniLog.log( ""+op.isValidPayIP("18.142.92.184"));
//	}
}
