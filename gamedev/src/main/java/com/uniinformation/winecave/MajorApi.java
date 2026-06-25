package com.uniinformation.winecave;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.uniinformation.rpccall.RpcServerConnection;
import com.uniinformation.rpccall.RpcServlet;
import com.uniinformation.utils.UniLog;

public class MajorApi implements RpcServlet{
	
	public MajorApi() {
		UniLog.log("MajorApi Initialized");
	}
	
	static String defaultSalesHeader = 
			"{"+
					"\"shopcode\": \"WH\","+
					"\"stationid\": \"01\","+
					"\"memono\": \"S200000009\","+
					"\"txdate\": \"20240315\","+
					"\"txtime\": \"08:44:13\","+
					"\"txtype\": \"CS\","+
					"\"txtypedescription\": \"Sales\","+
					"\"vipcode\": \"WH00000003\","+
					"\"salesmancode\": \"ONLINE\","+
					"\"mdiscode\": \"MCDDC\","+
					"\"inclexcl\": \"I\","+
					"\"byrateamt\": \"A\","+
					"\"rateoramt\": 0,"+
					"\"confirmflag\": \"Y\","+
					"\"voidflag\": \"N\","+
					"\"totalqty\": 1,"+
					"\"depositamt\": 0,"+
					"\"origamt\": 0,"+
					"\"grossamt\": 0,"+
					"\"netamt\": 0,"+
					"\"changeamt\": 0,"+
					"\"purchasegrossamt\": 0,"+
					"\"purchasenetamt\": 0,"+
					"\"purchaseqty\": 1,"+
					"\"returngrossamt\": 0,"+
					"\"returnnetamt\": 0,"+
					"\"returnqty\": 0,"+
					"\"actualsalesamt\": 0,"+
					"\"actualsalesqty\": 1,"+
					"\"initreftxtype\": \"CS\","+
					"\"refshop\": \"WH\","+
					"\"refstation\": \"01\","+
					"\"refmemo\": \"\","+
					"\"contact\": \"The Wine Cave Co Ltd\","+
					"\"phone1\": \"852-34279989\","+
					"\"phone2\": \"\","+
					"\"licensecode\": \"\","+
					"\"ref01\": \"\","+
					"\"ref02\": \"\","+
					"\"ref03\": \"\","+
					"\"ref04\": \"\","+
					"\"ref05\": \"\","+
					"\"ref06\": \"\","+
					"\"ref07\": \"\","+
					"\"ref08\": \"\","+
					"\"ref09\": \"\","+
					"\"ref10\": \"\","+
					"\"createuser\": \"A\","+
					"\"createdatetime\": \"\","+
					"\"lastmodifyuser\": \"A\","+
					"\"lastmodifydatetime\": \"\","+
					"\"vname\": \"The Wine Cave Co Ltd \","+
					"\"reasoncode\": \"NORMAL\""+
					"}";
	static String defaultSalesDetail= 
			"{"+
					"\"shopcode\": \"WH\","+
					"\"stationid\": \"01\","+
					"\"memono\": \"S200000009\","+
					"\"txseq\": \"001\","+
					"\"txdate\": \"20240315\","+
					"\"sku\": \"110300280\","+
					"\"description\": \"Harlan Maiden 2012\","+
					"\"dess\": \"Harlan Maiden 2012\","+
					"\"naturecode\": \"01\","+
					"\"tagpx\": 0,"+
					"\"promotionpx\": 0,"+
					"\"adiscount\": \"Y\","+
					"\"packageid\": \"\","+
					"\"packageseq\": \"\","+
					"\"packagecode\": \"\","+
					"\"packagedescription\": \"\","+
					"\"idiscode\": \"MCDDC\","+
					"\"inclexcl\": \"I\","+
					"\"byrateamt\": \"P\","+
					"\"rateoramt\": 0,"+
					"\"memodisamt\": 0,"+
					"\"salesqty\": 1,"+
					"\"totalqty\": 1,"+
					"\"itemamt\": 0,"+
					"\"distamt\": 0,"+
					"\"actualsalesamt\": 0,"+
					"\"actualsalesqty\": 1,"+
					"\"actualsalesalcamt\": 0,"+
					"\"osqty\": 0,"+
					"\"osamt\": 0,"+
					"\"osamtref\": 0,"+
					"\"osqtyref\": 0,"+
					"\"returnseq\": \"\","+
					"\"memorefseq\": \"\","+
					"\"memoreftxtype\": \"\","+
					"\"initreftxno\": \"CSWH   01S200000009\","+
					"\"initmemoref\": \"WH   01S200000009\","+
					"\"initmemorefseq\": \"001\","+
					"\"purchaseflag\": \"P\","+
					"\"salesmancode\": \"ONLINE\","+
					"\"backorder\": \"N\","+
					"\"expdate\": \"\","+
					"\"remark\": \"\","+
					"\"createuser\": \"A\","+
					"\"createdatetime\": \"\","+
					"\"lastmodifyuser\": \"A\","+
					"\"lastmodifydatetime\": \"\","+
					"\"oosqty\": 0,"+
					"\"oosamt\": 0,"+
					"\"isosqtyfin\": false,"+
					"\"isosamtfin\": false,"+
					"\"promotionpxold\": 0,"+
					"\"voidflag\": \"N\","+
					"\"delivery\": \"Y\""+
					"}";
	
	static String defaultSalesPayment= 
			"{"+
					"\"shopcode\": \"WH\","+
					"\"stationid\": \"01\","+
					"\"memono\": \"S200000009\","+
					"\"txseq\": \"01\","+
					"\"txdate\": \"20240315\","+
					"\"paymentcode\": \"WINEC\","+
					"\"description\": \"WINEC\","+
					"\"paymenttype\": \"O\","+
					"\"ref1\": \"\","+
					"\"ref2\": \"\","+
					"\"homecurrency\": \"HKD\","+
					"\"exchangerate\": 1,"+
					"\"paymentamt\": 0,"+
					"\"baseamt\": 0,"+
					"\"createuser\": \"A\","+
					"\"createdatetime\": \"\","+
					"\"lastmodifyuser\": \"A\","+
					"\"lastmodifydatetime\": \"\""+
					"}";
	
	static String defaultExtraInfo =
			"{"+
					"  \"contact\": \"The Wine Cave Co Ltd \","+
					"  \"phone1\": \"852-34279989\","+
					"  \"district\": \"HK\","+
					"  \"deliveryaddress1\": \"XXX FLAT C, F/L 19, TOWER 7, PHASE 6,, BEL AIR ON THE PEAK ,NO 8  BEL AIR\","+
					"  \"deliveryaddress2\": \"PEAK AVE\","+
					"  \"deliveryaddress3\": \"\","+
					"  \"deliveryaddress4\": \"\","+
					"  \"outloc\": \"WH\","+
					"  \"desireoutloc\": \"WH\","+
					"  \"deliverydate\": \"240315\","+
					"  \"deliverytime\": \"11:00-13:00\","+
					"  \"deliveryremark\": \"SO24-0302 \","+
					"  \"bpearn\": 0,"+
					"  \"bpredeem\": 0"+
					"}";
	
			
	
	String urlRoot;
	String refCode;
	String password;
	String accessToken;
	String refreshToken;
	public String setupUrl(String p_urlRoot,String p_refCode,String p_password) {
		accessToken = null;
		refreshToken = null;
		urlRoot = p_urlRoot;
		refCode = p_refCode;
		password = p_password;
		try {
			if(getToken()) {
				return("OK");
			} 
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		return("FAIL");
	}
	
	public boolean getToken() throws Exception {
		if(accessToken != null) return(true);
		UniLog.log("calling Api gettoken");
		HttpClient client = HttpClients.custom().build();
		HttpUriRequest request = RequestBuilder.post().setUri(urlRoot + "/post?mode=gettoken&refcode=" + refCode + "&password=" + password )
		    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
		    .addHeader("content-length", "0")
		    .build();
		HttpResponse response =  client.execute(request);
		String rtn = response.toString();
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		response.getEntity().writeTo(bo);
		String result = new String(bo.toByteArray(),"UTF-8");
		UniLog.log(result);
		JSONObject jo = new JSONObject(result);
		String rCode = jo.getString("responsecode");
		if(rCode.equals("0")) {
			accessToken = jo.getString("accesstoken");
			return(true);
		}
		return(false);
	}
	boolean placeOrderReal(String p_majorSno,String p_winecaveSO,String p_date,Vector items) throws Exception {
		java.util.Date d = DateUtil.dateTimeStrToDate(p_date);
		if(accessToken == null) throw new Exception("No Access Token");
		JSONObject jSalesHeader = new JSONObject(defaultSalesHeader);
		jSalesHeader.put("memono",p_majorSno);
		jSalesHeader.put("txdate",DateUtil.dateToDateTimeStr(d, "yyyyMMdd"));
		JSONArray jSalesDetails = new JSONArray();
		int itemcnt = items.size()/6;
		int tprice = 0;
		int tqty = 0;

		for(int i=0;i<itemcnt;i++) {
			int ofs = i*6;
			JSONObject jSalesDetail = new JSONObject(defaultSalesDetail);
			jSalesDetail.put("memono",p_majorSno);
			jSalesDetail.put("txdate",DateUtil.dateToDateTimeStr(d, "yyyyMMdd"));
			jSalesDetail.put("txseq",String.format("%03d", i+1));
			jSalesDetail.put("initreftxno","CSWH   01"+p_majorSno);
			jSalesDetail.put("initmemoref","WH   01"+p_majorSno);
			Double dv = (Double) items.get(ofs+4);
			String iname = (String) items.get(ofs + 5);
			int uprice = (int) Math.ceil(dv);
			dv = (Double) items.get(ofs+3);
			int qty = (int) Math.ceil(dv);
			tprice += uprice * qty;
			tqty += qty;
			jSalesDetail.put("sku",(String) items.get(ofs + 2));
//			jSalesDetail.put("tagpx",uprice);
//			jSalesDetail.put("promotionpx",uprice);

			jSalesDetail.put("itemamt",uprice * qty);
			jSalesDetail.put("distamt",uprice * qty);
			jSalesDetail.put("actualsalesamt",uprice * qty);

			jSalesDetail.put("tagpx",uprice);
			jSalesDetail.put("promotionpx",uprice);
			jSalesDetail.put("promotionpxold",uprice);
			
			jSalesDetail.put("salesqty",qty);
			jSalesDetail.put("totalqty",qty);
			jSalesDetail.put("actualsalesqty",qty);
			jSalesDetail.put("dess",iname);
			jSalesDetail.put("description",iname);
			
			jSalesDetails.put(jSalesDetail);
		}
		jSalesHeader.put("origamt",tprice);
		jSalesHeader.put("grossamt",tprice);
		jSalesHeader.put("netamt",tprice);
		jSalesHeader.put("purchasegrossamt",tprice);
		jSalesHeader.put("purchasenetamt",tprice);
		jSalesHeader.put("actualsalesamt",tprice);
		jSalesHeader.put("totalqty",tqty);
		jSalesHeader.put("purchaseqty",tqty);
		jSalesHeader.put("actualsalesqty",tqty);
		
		JSONObject jSalesPayment = new JSONObject(defaultSalesPayment);
		jSalesPayment.put("memono",p_majorSno);
		jSalesPayment.put("txdate",DateUtil.dateToDateTimeStr(d, "yyyyMMdd"));
		jSalesPayment.put("paymentamt",tprice);
		jSalesPayment.put("baseamt",tprice);
		
		JSONArray jSalesPayments = new JSONArray();
		jSalesPayments.put(jSalesPayment);
		JSONObject jExtraInfo = new JSONObject(defaultExtraInfo);
		jExtraInfo.put("deliveryremark",p_winecaveSO);
		
		UniLog.log("calling Api neworder");

		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("salesheader", jSalesHeader.toString()));
		formparams.add(new BasicNameValuePair("salesdetail", jSalesDetails.toString()));
		formparams.add(new BasicNameValuePair("salespayment", jSalesPayments.toString()));
		formparams.add(new BasicNameValuePair("extrainfo", jExtraInfo.toString()));
		
		UrlEncodedFormEntity entity = new UrlEncodedFormEntity(formparams, Consts.UTF_8);
		HttpClient client = HttpClients.custom().build();
		HttpUriRequest request = RequestBuilder.post().setUri(urlRoot + "/post?mode=postsalesinfowithextrainfo")
				/*
		    .setHeader(HttpHeaders.CONTENT_TYPE, "multipart/form-data")
		    .addHeader("content-length", "0")
		    */
		    .addHeader("Authorization", "key="+accessToken)
		    .setEntity(entity)
		    .build();
		HttpResponse response =  client.execute(request);
		
		
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		response.getEntity().writeTo(bo);
		String result = new String(bo.toByteArray(),"UTF-8");
		UniLog.log(result);
		JSONObject jo = new JSONObject(result);		
		return(jo.getBoolean("success"));
	}
	
	
	public static void main(String args[]){
		MajorApi mapi = new MajorApi();
		UniLog.log("setupUrl got" + mapi.setupUrl("http://210.3.102.6:13899", "A","509510"));
		try {
			UniLog.log("Token = " + mapi.accessToken);
			mapi.placeOrderReal("S300000304","SO24-0320","2024/03/18",null);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}

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
		
	}

	@Override
	public String ping() {
		// TODO Auto-generated method stub
	  	return("OK  ");
	}
	
	public String placeOrder(String p_majorso,String p_wcso,String p_date,Vector<Object> p_items) {
		try {
			if(placeOrderReal(p_majorso,p_wcso,p_date,p_items)) {
				UniLog.log("please order " + p_majorso + " OK ");
				return("OK");
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		return("FAIL");
	}
	

}
