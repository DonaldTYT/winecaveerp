package com.uniinformation.zkf.smartac;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.zkf.ZkCellActionForm;

public class ShopifyGetProduct extends ZkCellActionForm{
	static Hashtable<String,ApiProductRec> apiProductList = null;
	static final String baseURL = "https://winninggrace.myshopify.com/admin/api/2021-07/";
	static final String accessToken = "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";
	static class ApiProductRec {
		long id;
		long inventoryId;
		String title;
		String sku;
	}
	
	static class ApiOrderRec {
		String orderid;
		String created_at;
	}
	
	@Override
	public void doAfterCompose(Component arg0) throws Exception {
		onClickListener = new EventListener() {
			@Override
			public void onEvent(Event arg0) throws Exception {
				// TODO Auto-generated method stub
				Component c = (Component)arg0.getTarget();
				if(c.getId().equals("btApiGetProducts")) {
					apiGetProducts();
				}
				if(c.getId().equals("btApiGetInventoryId")) {
					Long l = apiGetInventoryIdBySku("Grace-Cosm-GLM-06");
					UniLog.log("btApiGetInventoryId got " + l);
				}
				if(c.getId().equals("btApiGetLocations")) {
					apiGetLocations();
				}
				if(c.getId().equals("btApiGetInventoryLevel")) {
					Long l = apiGetInventoryIdBySku("Grace-Cosm-WPC-13-test");
					UniLog.log("btApiGetInventoryId got " + l);
					if(l != null) {
//						int available = apiGetInventoryLevel((long) 55458955417L,(long) l);
						Integer available = apiGetInventoryLevel(55458955417L,l);
						if(available != null) {
							UniLog.log("inventory Level = " + available);
						} else {
							UniLog.log("inventory Level undefined");
						}
					}
				}
				if(c.getId().equals("btApiAdjustInventoryLevel")) {
					Long l = apiGetInventoryIdBySku("Grace-Cosm-WPC-13-test");
					UniLog.log("btApiGetInventoryId got " + l);
					if(l != null) {
						Integer available = apiAdjustInventoryLevel(55458955417L,l,1);
						if(available != null) {
							UniLog.log("new inventory Level = " + available);
						} else {
							UniLog.log("inventory Level undefined");
						}
					}
				}
			}
		};
		super.doAfterCompose(arg0);
	}

//	static JSONObject apiGetInventoryByLocation(long locationId) throws Exception {
//		  HttpClient client = HttpClients.custom().build();
//		  HttpUriRequest request = RequestBuilder.get().setUri(baseURL + "locations/"+locationId+"inventory_levels.json")
//		    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
//		    .setHeader("X-Shopify-Access-Token", accessToken)
//		    .build();
//		  HttpResponse response =  client.execute(request);
//		  String rtn = response.toString();
//		  ByteArrayOutputStream bo = new ByteArrayOutputStream();
//		  response.getEntity().writeTo(bo);
//		  String result = new String(bo.toByteArray(),"UTF-8");
//		  UniLog.log(result);
//		  JSONObject jo = new JSONObject(result);
//		  /*
//		  JSONArray pds = jo.getJSONArray("locations");
//		  for(int i=0;i<pds.length();i++) {
//			  JSONObject item = pds.getJSONObject(i);
//			  UniLog.log("Location " + i + " id:" + item.getLong("id") + " name: " + item.getString("name") + " address: " + item.getString("address1"));
//		  }
//		  */
//		  return(jo);
//	}
	
	static public Hashtable<String,ApiProductRec>apiGetProducts_250() throws Exception {
		  HttpClient client = HttpClients.custom().build();
		  HttpUriRequest request = RequestBuilder.get().setUri(baseURL + "products.json?limit=250")
		    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
		    .setHeader("X-Shopify-Access-Token", accessToken)
		    .build();
		  HttpResponse response =  client.execute(request);
		  String rtn = response.toString();
		  ByteArrayOutputStream bo = new ByteArrayOutputStream();
		  response.getEntity().writeTo(bo);
		  String result = new String(bo.toByteArray(),"UTF-8");
		  UniLog.log(result);
		  JSONObject jo = new JSONObject(result);
		  JSONArray products = jo.getJSONArray("products");
		  Hashtable<String,ApiProductRec> productList = new Hashtable<String,ApiProductRec>();
		  UniLog.log("Total " + products.length() + " records returned");
		  for(int i=0;i<products.length();i++) {
			  JSONObject product = products.getJSONObject(i);
			  long id = product.getLong("id");
			  String title = product.getString("title");
			  JSONArray variants = product.getJSONArray("variants");
			  for(int j=0;j<variants.length();j++) {
				 JSONObject variant = variants.getJSONObject(j) ;
				 String sku = variant.getString("sku");
				 if(!sku.trim().equals("")) {
					 ApiProductRec pRec = new ApiProductRec();
					 pRec.id = id;
					 pRec.title = title;
					 pRec.sku = sku;
					 pRec.inventoryId = variant.getLong("inventory_item_id");
					 productList.put(pRec.sku, pRec);
					 UniLog.log("Product " + " id " + id + " sku " + pRec.sku + " title : " + pRec.title);
				 }
				 
			  }
		  }
		  return(productList);
	}
	
	static HttpResponse callRestApi(String p_url) throws Exception {
		HttpClient client = HttpClients.custom().build();
		HttpUriRequest request = RequestBuilder.get().setUri(p_url)
		    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
		    .setHeader("X-Shopify-Access-Token", accessToken)
		    .build();
		HttpResponse response =  client.execute(request);
		return(response);
	}
	
	static public Hashtable<String,ApiOrderRec>apiGetOrders(HashSet<String>shops,java.util.Date p_fromDate,java.util.Date p_toDate) throws Exception {
		Hashtable<String,ApiOrderRec> ordersSet = new Hashtable<String,ApiOrderRec>();
		String url = null;
		String fromDateStr = DateUtil.toDateString(p_fromDate, "yyyy-mm-dd");
		String toDateStr = DateUtil.toDateString(p_toDate, "yyyy-mm-dd");
		UniLog.log("In apiGetOrders fromdate " + fromDateStr + " todate " + toDateStr);		
//		url = baseURL + "orders.json?limit=10?created_at_min="+fromDateStr+"?created_at_max="+toDateStr;
//		url = baseURL + "orders.json?created_at_min="+fromDateStr+"?created_at_max="+toDateStr;

		url = baseURL + "orders.json";
		
		
		  for(;;) {
		  HttpResponse response =  callRestApi(url);
		  String rtn = response.toString();
		  ByteArrayOutputStream bo = new ByteArrayOutputStream();
		  response.getEntity().writeTo(bo);
		  String result = new String(bo.toByteArray(),"UTF-8");
		  UniLog.log(result);
		  JSONObject jo = new JSONObject(result);
		  JSONArray orders = jo.getJSONArray("orders");
		  UniLog.log("Total " + orders.length() + " records returned");
		  for(int i=0;i<orders.length();i++) {
			  JSONObject order = orders.getJSONObject(i);
			  long id = order.getLong("id");
			  ApiOrderRec orderrec = new ApiOrderRec();
			  orderrec.orderid = ""+id;
			  orderrec.created_at = order.getString("created_at");
			  ordersSet.put(""+id, orderrec);
			  UniLog.log("Get Order " + orderrec.orderid + " create_at " + orderrec.created_at);
		  }
		    boolean hasNextUrl = false;
		  	Header hdrs[] = response.getHeaders("link");
		  	if(hdrs != null) {
		  		UniLog.log("check next pages");
		  		for(Header hdr : hdrs) {
		  			String str = hdr.getValue();
		  			StringTokenizer st = new StringTokenizer(str,",");  
		  			while (st.hasMoreTokens()) {  
		  				String str2 = st.nextToken();
		  				int cc = str2.indexOf(";");
		  				if(cc >= 0) {
		  					String urlCond = str2.substring(cc+1);
		  					if(urlCond.trim().equals("rel=\"next\"")) {
		  						String urlx = str2.substring(0,cc);
		  						url = urlx.substring(1,urlx.indexOf(">"));
		  						hasNextUrl = true;
		  					}
		  				}
		  				if(hasNextUrl) break;
		  			}  
		  			if(hasNextUrl) break;
		  		}
		  	} else {
		  		UniLog.log("no more pages");
		  	}
		  	if(hasNextUrl) continue;
		  	break;
	  }
	  UniLog.log("Total " + ordersSet.size() + " order returned ");
	  return(ordersSet);
	}
	static public Hashtable<String,ApiProductRec>apiGetProducts() throws Exception {
		  Hashtable<String,ApiProductRec> productList = new Hashtable<String,ApiProductRec>();
		  String url = null;
		  url = baseURL + "products.json?limit=250";
		  

		  for(;;) {
		  HttpResponse response =  callRestApi(url);
		  String rtn = response.toString();
		  ByteArrayOutputStream bo = new ByteArrayOutputStream();
		  response.getEntity().writeTo(bo);
		  String result = new String(bo.toByteArray(),"UTF-8");
		  UniLog.log(result);
		  JSONObject jo = new JSONObject(result);
		  JSONArray products = jo.getJSONArray("products");
		  UniLog.log("Total " + products.length() + " records returned");
		  for(int i=0;i<products.length();i++) {
			  JSONObject product = products.getJSONObject(i);
			  long id = product.getLong("id");
			  String title = product.getString("title");
			  /*
			  byte[] titleb5 = title.getBytes("MS950");
			  StringBuffer sb = new StringBuffer();
			  for(int j=0;j<titleb5.length;j++) {
				  int c = titleb5[j];
				  if(c < 0) c += 256;
				  char ch = (char) c;
				  sb.append(ch);
			  }
			  UniLog.log("getProduct id " + id + " title " + sb.toString()); 
			  */
			  JSONArray variants = product.getJSONArray("variants");
			  for(int j=0;j<variants.length();j++) {
				 JSONObject variant = variants.getJSONObject(j) ;
				 String sku = variant.getString("sku");
				 if(!sku.trim().equals("")) {
					 ApiProductRec pRec = new ApiProductRec();
					 pRec.id = id;
					 pRec.title = title;
					 pRec.sku = sku;
					 pRec.inventoryId = variant.getLong("inventory_item_id");
					 productList.put(pRec.sku, pRec);
					 UniLog.log("Product " + " id " + id + " sku " + pRec.sku + " title : " + pRec.title);
				 }
				 
			  }
		  }
		    boolean hasNextUrl = false;
		  	Header hdrs[] = response.getHeaders("link");
		  	if(hdrs != null) {
		  		for(Header hdr : hdrs) {
		  			String str = hdr.getValue();
		  			StringTokenizer st = new StringTokenizer(str,",");  
		  			while (st.hasMoreTokens()) {  
		  				String str2 = st.nextToken();
		  				int cc = str2.indexOf(";");
		  				if(cc >= 0) {
		  					String urlCond = str2.substring(cc+1);
		  					if(urlCond.trim().equals("rel=\"next\"")) {
		  						String urlx = str2.substring(0,cc);
		  						UniLog.log("nextURL(A):["+urlx+"]");
		  						int ust = urlx.indexOf("<");
		  						if(ust < 0) ust = 0;
		  						url = urlx.substring(ust+1,urlx.indexOf(">"));
		  						UniLog.log("nextURL(B):["+url+"]");
		  						hasNextUrl = true;
		  					}
		  				}
		  				if(hasNextUrl) break;
		  			}  
		  			if(hasNextUrl) break;
		  		}
		  	}
		  	if(hasNextUrl) continue;
		  	break;
		  }
		  return(productList);
	}
	synchronized static Long apiGetInventoryIdBySku(String sku) throws Exception {
		ApiProductRec pd = null;
		if(apiProductList != null) pd = apiProductList.get(sku);
		if(pd != null) return(pd.inventoryId);
		apiProductList = apiGetProducts();
		pd = apiProductList.get(sku);
		if(pd != null) return(pd.inventoryId);
		return(null);
	}
	static public void apiGetLocations() throws Exception {
	  HttpClient client = HttpClients.custom().build();
	  HttpUriRequest request = RequestBuilder.get().setUri(baseURL + "locations.json")
	    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
	    .setHeader("X-Shopify-Access-Token", accessToken)
	    .build();
	  HttpResponse response =  client.execute(request);
	  String rtn = response.toString();
	  ByteArrayOutputStream bo = new ByteArrayOutputStream();
	  response.getEntity().writeTo(bo);
	  String result = new String(bo.toByteArray(),"UTF-8");
	  UniLog.log(result);
	  JSONObject jo = new JSONObject(result);
	  JSONArray pds = jo.getJSONArray("locations");
	  for(int i=0;i<pds.length();i++) {
		  JSONObject item = pds.getJSONObject(i);
		  UniLog.log("Location " + i + " id:" + item.getLong("id") + " name: " + item.getString("name") + " address: " + item.getString("address1"));
	  }
	}
	static Integer apiGetInventoryLevel(long locationId,long inventoryId) throws Exception {
		UniLog.log("getInventoryLevel loc: " + locationId + " inventoryId " + inventoryId);
		HttpClient client = HttpClients.custom().build();
		HttpUriRequest request = RequestBuilder.get().setUri(baseURL + "inventory_levels.json"+"?location_ids="+locationId+"&inventory_item_ids="+inventoryId)
		    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
		    .setHeader("X-Shopify-Access-Token", accessToken)
		    .build();
		HttpResponse response =  client.execute(request);
		String rtn = response.toString();
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		response.getEntity().writeTo(bo);
		String result = new String(bo.toByteArray(),"UTF-8");
		UniLog.log(result);
		JSONObject jo = new JSONObject(result);
		JSONArray inventory_levels = jo.getJSONArray("inventory_levels");
		for(int i=0;i<inventory_levels.length();i++) {
			  JSONObject inventory_level = inventory_levels.getJSONObject(i);
			  long id = inventory_level.getLong("inventory_item_id");
			  if(id == inventoryId) {
				  return(inventory_level.getInt("available"));
			  }
		}
		return(null);
	}
	
	
	static Integer apiAdjustInventoryLevel(long locationId,long inventoryId,int adjustment) throws Exception {
		UniLog.log("getInventoryLevel loc: " + locationId + " inventoryId " + inventoryId);
		JSONObject jox = new JSONObject();
		jox.put("location_id",locationId);
		jox.put("inventory_item_id",inventoryId);
		jox.put("available_adjustment",adjustment);
		HttpClient client = HttpClients.custom().build();
		HttpUriRequest request = RequestBuilder.post().setUri(baseURL + "inventory_levels/adjust.json")
		    .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
		    .setHeader("X-Shopify-Access-Token", accessToken)
		    .setEntity(new StringEntity(jox.toString(), "UTF-8"))
		    .build();
		
		HttpResponse response =  client.execute(request);
		String rtn = response.toString();
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		response.getEntity().writeTo(bo);
		String result = new String(bo.toByteArray(),"UTF-8");
		UniLog.log(result);
		JSONObject jo = new JSONObject(result);
		JSONObject inventory_level = jo.getJSONObject("inventory_level");
		long id = inventory_level.getLong("inventory_item_id");
		if(id == inventoryId) {
			return(inventory_level.getInt("available"));
		}
		return(null);
	}
	
	static Long getInventoryListByLocationId(long locationId,String sku) {
		return(null);
	}
	

	static public long getApiLocationId(SelectUtil p_su,String p_loc)  throws Exception {
		TableRec tr = p_su.getQueryResult("select * from locationcode where loc_code = '" + p_loc + "'");
		if(tr.getRecordCount() > 0) {
			tr.setRecPointer(0);
			String ss = tr.getFieldString("loc_shopid");
			if(ss.trim().equals("")) return(0L);
			long ll = Long.parseLong(ss.trim());
			return(ll);
		}
		return(0L);
	}
	
	/*
	 *  stmd_apisynced : "" = not sync, "S" = sync, "F" = "failed"
	 */
	static public ReturnMsg syncTransferToWeb(BiResult p_br) throws Exception {
		SelectUtil su = p_br.getSelectUtil();
		TableRec tr = su.getQueryResult("select * from stmov where stm_mrg = " + p_br.getCellInt("stm_mrg"));
		boolean realUpdate = "Y".equals(Erpv4Config.getString(p_br.getSessionHelper(), "RealUpdateWeb"));
		long fromLocId  = 0L;
		long toLocId  = 0L;
		if(tr.getRecordCount() == 1) {
			tr.setRecPointer(0);
			fromLocId = getApiLocationId(su,tr.getFieldString("stm_fromloc"));
			toLocId = getApiLocationId(su,tr.getFieldString("stm_toloc"));
			int syncCnt = 0;
			int unsyncCnt = 0;
			TableRec tr2 = su.getQueryResult("select *,stmovd.serial_id sid from stmovd,stock where stmd_mrg = " + p_br.getCellInt("stm_mrg") + " and st_irg = stmd_irg order by stmd_tdindex,stmd_tdtype");

			UniLog.log("sync " + tr2.getRecordCount() + " records");
			for(int i=0;i<tr2.getRecordCount();i++) {
				tr2.setRecPointer(i);
				if(tr2.getFieldString("stmd_tdtype").equals("KO")) {
					if(fromLocId == 0L) {
						if(!tr2.getFieldString("stmd_apisynced").equals("")) {
							su.executeUpdate("update stmovd set stmd_apisynced = '' where serial_id = ?", 
										new Wherecl().appendArgument(tr2.getFieldInt("sid"))
									);
						}
					} else {
						if(!tr2.getFieldString("stmd_apisynced").equals("S")) {
							try {
								Long invId = apiGetInventoryIdBySku(tr2.getFieldString("st_icode"));
								int qty = - (int) tr2.getFieldDouble("stmd_qty");
								UniLog.log("update spotify inventory loc " + fromLocId + " invid " + invId.longValue() + " qty " + qty);
								if(realUpdate ) {
									apiAdjustInventoryLevel(fromLocId,invId,qty);
								}
								su.executeUpdate("update stmovd set stmd_apisynced = 'S' where serial_id = ?", 
										new Wherecl().appendArgument(tr2.getFieldInt("sid"))
									);
								syncCnt++;
							} catch (Exception ex) {
								su.executeUpdate("update stmovd set stmd_apisynced = 'F' where serial_id = ?", 
										new Wherecl().appendArgument(tr2.getFieldInt("sid"))
									);
								unsyncCnt++;
								UniLog.log(ex);
							}
						} else syncCnt++;
					}
				}
				
				if(tr2.getFieldString("stmd_tdtype").equals("KI")) {
					if(toLocId == 0L) {
						if(!tr2.getFieldString("stmd_apisynced").equals("")) {
							su.executeUpdate("update stmovd set stmd_apisynced = '' where serial_id = ?", 
										new Wherecl().appendArgument(tr2.getFieldInt("sid"))
									);
						}
					} else {
						if(!tr2.getFieldString("stmd_apisynced").equals("S")) {
							try {
								Long invId = apiGetInventoryIdBySku(tr2.getFieldString("st_icode"));
								int qty = (int) tr2.getFieldDouble("stmd_qty");
								UniLog.log("update spotify inventory loc " + toLocId + " invid " + invId.longValue() + " qty " + qty);
								if(realUpdate ) {
									apiAdjustInventoryLevel(toLocId,invId,qty);
								}
								su.executeUpdate("update stmovd set stmd_apisynced = 'S' where serial_id = ?", 
										new Wherecl().appendArgument(tr2.getFieldInt("sid"))
									);
								syncCnt++;
							} catch (Exception ex) {
								su.executeUpdate("update stmovd set stmd_apisynced = 'F' where serial_id = ?", 
										new Wherecl().appendArgument(tr2.getFieldInt("sid"))
									);
								unsyncCnt++;
								UniLog.log(ex);
							}
						} else syncCnt++;
					}
				}
			}
			String syncStat = "";
			if(unsyncCnt > 0) syncStat = "Progress";
				else if(syncCnt > 0) syncStat = "Synced";
			su.executeUpdate("update stmov set stm_syncstat = ? where stm_mrg = ?", 
						new Wherecl()
							.appendArgument(syncStat)
							.appendArgument(p_br.getCellInt("stm_mrg"))
					);
		}
		return(ReturnMsg.defaultOk);
	}
	
	static public ReturnMsg unsyncTransferToWeb(BiResult p_br) throws Exception {
		SelectUtil su = p_br.getSelectUtil();
		TableRec tr = su.getQueryResult("select * from stmov where stm_mrg = " + p_br.getCellInt("stm_mrg"));
		boolean realUpdate = "Y".equals(Erpv4Config.getString(p_br.getSessionHelper(), "RealUpdateWeb"));
		long fromLocId  = 0L;
		long toLocId  = 0L;
		if(tr.getRecordCount() == 1) {
			tr.setRecPointer(0);
			fromLocId = getApiLocationId(su,tr.getFieldString("stm_fromloc"));
			toLocId = getApiLocationId(su,tr.getFieldString("stm_toloc"));
			int syncCnt = 0;
			int unsyncCnt = 0;
			TableRec tr2 = su.getQueryResult("select *,stmovd.serial_id sid from stmovd,stock where stmd_mrg = " + p_br.getCellInt("stm_mrg") + " and st_irg = stmd_irg order by stmd_tdindex,stmd_tdtype");

			UniLog.log("unsync " + tr2.getRecordCount() + " records");
			for(int i=0;i<tr2.getRecordCount();i++) {
				tr2.setRecPointer(i);
				if(tr2.getFieldString("stmd_tdtype").equals("KO")) {
					if(fromLocId == 0L) {
						if(!tr2.getFieldString("stmd_apisynced").equals("")) {
							su.executeUpdate("update stmovd set stmd_apisynced = '' where serial_id = ?", 
										new Wherecl().appendArgument(tr2.getFieldInt("sid"))
									);
						}
					} else {
						if(tr2.getFieldString("stmd_apisynced").equals("S")) {
							try {
								Long invId = apiGetInventoryIdBySku(tr2.getFieldString("st_icode"));
								int qty = (int) tr2.getFieldDouble("stmd_qty");
								UniLog.log("reverse spotify inventory loc " + fromLocId + " invid " + invId.longValue() + " qty " + qty);
								if(realUpdate ) {
									apiAdjustInventoryLevel(fromLocId,invId,qty);
								}
								su.executeUpdate("update stmovd set stmd_apisynced = '' where serial_id = ?", 
										new Wherecl().appendArgument(tr2.getFieldInt("sid"))
									);
							} catch (Exception ex) {
								syncCnt++;
								UniLog.log(ex);
							}
						} else {
							su.executeUpdate("update stmovd set stmd_apisynced = '' where serial_id = ?", 
										new Wherecl().appendArgument(tr2.getFieldInt("sid"))
									);
						}
					}
				}
				if(tr2.getFieldString("stmd_tdtype").equals("KI")) {
					if(toLocId == 0L) {
						if(!tr2.getFieldString("stmd_apisynced").equals("")) {
							su.executeUpdate("update stmovd set stmd_apisynced = '' where serial_id = ?", 
										new Wherecl().appendArgument(tr2.getFieldInt("sid"))
									);
						}
					} else {
						if(tr2.getFieldString("stmd_apisynced").equals("S")) {
							try {
								Long invId = apiGetInventoryIdBySku(tr2.getFieldString("st_icode"));
								int qty = -(int) tr2.getFieldDouble("stmd_qty");
								UniLog.log("reverse spotify inventory loc " + toLocId + " invid " + invId.longValue() + " qty " + qty);
								if(realUpdate ) {
									apiAdjustInventoryLevel(toLocId,invId,qty);
								}
								su.executeUpdate("update stmovd set stmd_apisynced = '' where serial_id = ?", 
										new Wherecl().appendArgument(tr2.getFieldInt("sid"))
									);
							} catch (Exception ex) {
								syncCnt++;
								UniLog.log(ex);
							}
						} else {
							su.executeUpdate("update stmovd set stmd_apisynced = '' where serial_id = ?", 
										new Wherecl().appendArgument(tr2.getFieldInt("sid"))
									);
						}
					}
				}
			}
			
			String syncStat = "";
			if(unsyncCnt > 0) syncStat = "Progress";
				else if(syncCnt > 0) syncStat = "Synced";
			su.executeUpdate("update stmov set stm_syncstat = ? where stm_mrg = ?", 
						new Wherecl()
							.appendArgument("")
							.appendArgument(p_br.getCellInt("stm_mrg"))
					);
		}
		return(ReturnMsg.defaultOk);
	}
}
