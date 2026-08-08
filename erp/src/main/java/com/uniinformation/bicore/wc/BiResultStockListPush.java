package com.uniinformation.bicore.wc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.uniinformation.bicore.BiCoreRpcServlet.BiRpcInterface;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiCellCollectionToJsonInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellException;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.RpcServerConnection;
import com.uniinformation.rpccall.RpcServlet;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultStockListPush extends BiResultStockList implements BiRpcInterface{

	public BiResultStockListPush(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	
	private static Double cashDiscountPercent;
	public static double getCashDiscountPercent() {
		return(0.03);
	}
	public static double getWebConsigpprice(double p_price) {
		return Math.ceil(p_price / (1 - getCashDiscountPercent()));
	}
	public static double getWebStandardprice(double p_price) {
		return Math.ceil(p_price / (1 - getCashDiscountPercent()));
	}

	String decodeRichText(String value) {
		if(StringUtils.isBlank(value)) return("");
		try {
			JSONObject jo = new JSONObject(value);
			JSONArray blocks = jo.optJSONArray("blocks");
			if(blocks == null) return(value);

			StringBuilder sb = new StringBuilder();
			for(int i=0;i<blocks.length();i++) {
				JSONObject block = blocks.optJSONObject(i);
				if(block == null) continue;
				JSONObject data = block.optJSONObject("data");
				if(data == null) continue;
				String text = data.optString("text", "");
				if(StringUtils.isBlank(text)) continue;
				if(sb.length() > 0) sb.append("\n");
				sb.append(text);
			}
			return(sb.toString());
		} catch(Exception ex) {
			return(value);
		}
	}
	
	boolean checkAttributeValue(String icode,JSONObject jo,String value) {
		try {
		JSONArray ja = jo.optJSONArray("values");
		if(ja != null && ja.length() > 0) {
			String val = ja.getString(0);
			if(val != null) {
				if(!value.equals(val)) {
					return(true);
				}
			}
		}
		return(false);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(false);
		}
	}
	
	boolean checkSaleorAttribute(String icode,JSONObject ejo,JSONArray ja) throws JSONException {
		JSONObject country = null;
		JSONObject region  = null;
		JSONObject brand   = null;
		JSONObject tasting = null;
		for(int i = 0;i<ja.length();i++) {
			JSONObject jo = ja.getJSONObject(i);
			if("country".equals(jo.optString("attribute"))) {
				country = jo;
			}
			if("region".equals(jo.optString("attribute"))) {
				region = jo;
			}
			if("brand".equals(jo.optString("attribute"))) {
				brand = jo;
			}
			if("tasting".equals(jo.optString("attribute"))) {
				tasting = jo;
			}
		}
		if(country == null) {
			UniLog.log("WebPush ProductNeedSync " + icode + " country is null");
			return(true);
		}
		if(region == null) {
			UniLog.log("WebPush ProductNeedSync " + icode + " region is null");
			return(true);
		}
		if(brand == null) {
			UniLog.log("WebPush ProductNeedSync " + icode + " brand is null");
			return(true);
		}
		if(tasting == null) {
			UniLog.log("WebPush ProductNeedSync " + icode + " tasting is null");
			return(true);
		}
		if(checkAttributeValue(icode,brand,ejo.getString("stbd_name"))) {
			UniLog.log("WebPush ProductNeedSync " + icode + " brand name differ ");
			return(true);
		}
		JSONArray jav = country.optJSONArray("values");
		if(jav == null || jav.length() < 1 ||
				!jav.getString(0).equals(ejo.getString("storg_ecountry"))) {
			UniLog.log("WebPush ProductNeedSync " + icode + " country differ ");
			return(true);
		}
		jav = tasting.optJSONArray("values");
		if(jav == null || jav.length() < 1 ) {
			UniLog.log("WebPush ProductNeedSync " + icode + " tasting has no lines");
			return(true);
		}
		String dbTasting = ejo.getString("stnd_note");
		String saleorTasting = decodeRichText(jav.getString(0));
		if(dbTasting.length() > 160) {
			String s1 = dbTasting.substring(0,160);
			if(saleorTasting.length() < 160) {
				UniLog.log("WebPush ProductNeedSync " + icode + " tasting length differ ");
				return(true);
			}
			String s2 = saleorTasting.substring(0,160);
			if(!s1.equals(s2)) {
				UniLog.log("WebPush ProductNeedSync " + icode + String.format(" tasting differ (2) '%s':'%s'",
						s1,s2
						));
				return(true);
			}
		} else {
			if( !saleorTasting.equals(dbTasting)) {
				UniLog.log("WebPush ProductNeedSync " + icode + String.format(" tasting differ (2) '%s':'%s'",
						saleorTasting,dbTasting
						));
				return(true);
			}
		}
		/*
		{
			return(true);
		}
		*/
		return(false);
	}
	
	 boolean checkAndSyncProductRecord(JSONObject ejo, JSONObject joSaleor) throws Exception {
		  boolean needSync = false;
		  JSONArray ja = joSaleor.getJSONArray("metadata");
		  String ss = ejo.getString("st_icode");
//		  UniLog.log("WebPush sync icode " + ss);
		  if(ja.length() == 0) {
					JSONArray ja0 = new JSONArray();
					addProductMetadata(ja0,"icode",ejo.getString("st_icode"));
					if(!StringUtils.isBlank(ejo.getString("stbd_name"))) {
						addProductMetadata(ja0,"brand",ejo.getString("stbd_name"));
					}
					if(!StringUtils.isBlank(ejo.getString("st_maturity"))) {
						addProductMetadata(ja0,"maturity",ejo.getString("st_maturity"));
					}
					if(!StringUtils.isBlank(ejo.getString("st_vintage"))) {
						addProductMetadata(ja0,"vintage",ejo.getString("st_vintage"));
					}
					if(!StringUtils.isBlank(ejo.getString("storg_ecountry"))) {
						addProductMetadata(ja0,"country",ejo.getString("storg_ecountry"));
					}
					if(!StringUtils.isBlank(ejo.getString("storg_name"))) {
						addProductMetadata(ja0,"region",ejo.getString("storg_name"));
					}
					if(!StringUtils.isBlank(ejo.getString("stbd_appellation"))) {
						addProductMetadata(ja0,"appellation",ejo.getString("stbd_appellation"));
					}
					if(ejo.getInt("st_msize2") > 0) {
						addProductMetadata(ja0,"volumne",""+ejo.getInt("st_msize2")+"ml");
					}
					joSaleor.put("metadata", ja0);
					needSync = true;
		  } else {
//			  JSONObject country = null;
//			  JSONObject region  = null;
//			  JSONObject brand   = null;
//			  
//			  for(int i = 0;i<ja.length();i++) {
//				  JSONObject jo = ja.getJSONObject(i);
//				  if("country".equals(jo.optString("key"))) {
//					  country = jo;
//				  }
//				  if("region".equals(jo.optString("key"))) {
//					  region = jo;
//				  }
//			  int cc;
//			  cc = 0;
//
//			  
		  }
		  
		  if(checkSaleorAttribute(ss,ejo, joSaleor.getJSONArray("attributes"))) {
				needSync = true;
		  }
			  
		  
		  ja = joSaleor.getJSONArray("channels");
		  if(ja.length() == 0) {
				needSync = true;
		  } else {
			  boolean hkFound = false;
			  for(int i=0;i<ja.length();i++) {
				  JSONObject jc = ja.getJSONObject(i);
				  String channelName = jc.getString("slug");
				  if("hk".equals(channelName)) {
					  hkFound = true;
				  }
			  }
			  if(!hkFound) needSync = true;
		  }
		  String desc = joSaleor.optString("description","");
		  if (StringUtils.isBlank(desc)) {
			  	joSaleor.put("description", ejo.getString("st_icode"));
				needSync = true;
		  }
		  return(needSync);
	  }
	  boolean checkAndSyncProductVariant(JSONObject ejo, JSONObject joSaleor) throws Exception {
		  JSONArray ja = joSaleor.getJSONArray("channels");
		  boolean channelFound = false;
		  for(int i=0;i<ja.length();i++) {
			  JSONObject jo = ja.getJSONObject(i);
			  String channelName = jo.getString("slug");
			  if(channelName.equals("hk")) {
				  channelFound = true;
			  }
			  double price = jo.getDouble("price");
//			  double erpprice = WineCaveUtil.getWebConsigpprice( ejo.getDouble("consgp_price"));
			  double erpprice = ejo.getDouble("st_webprice");
			  double cost = jo.getDouble("costPrice");
			  if (price != erpprice
					 || cost !=  ejo.getDouble("consgp_cost")) {
				  return(true);
			  }
		  }
		  if(!channelFound) {
			  return(true);
		  }
		  return(false);
	  }
	  boolean checkAndSyncProductMedia(JSONObject joErp, JSONObject joSaleor) {
		  boolean needSync = false;
		  int epid = joErp.optInt("st_photoid");
		  JSONArray media = joSaleor.optJSONArray("media");
		  if(media != null && media.length() > 0) { 
			  try {
				  String slug =joErp.getString("st_slug");
//				  if(!slug.equals("wine-red-bordeaux-abcr25b-43499")) {
//					  return(false);
//				  }
				  String alt = media.getJSONObject(0).optString("alt");
//				  UniLog.log("photo " + epid + " alt : " + alt);
				  String url = joErp.getString("st_photourl");
				  if(url == null || ! url.equals(alt)) {
					  return(true);
				  }
			  } catch (Exception ex) { 
				  UniLog.log(ex);
			  }
		  }
		  return(needSync);
	  }
	  
	  void addProductAttributeWithChinese(JSONArray ja,String p_key,String p_evalue,String p_cvalue) throws Exception {
		  JSONObject jo = new JSONObject();
		  jo.put("attribute", p_key);
		  jo.put("inputType", "PLAIN_TEXT");
		  JSONArray jv = new JSONArray();
		  JSONObject jt = new JSONObject();
		  jt.put("slug", BiCellCollection.makeSlug(p_evalue));
		  jt.put("name", p_evalue);
		  JSONObject jtr = new JSONObject();
		  jtr.put("zh-Hant", p_cvalue);
		  jt.put("translations", jtr);
		  jv.put(jt);
		  jo.put("values", jv);
		  ja.put(jo);
	  }
	  void addProductAttributeWithType(JSONArray ja,String p_key,String p_type,Object p_value) throws Exception {
		  JSONObject jo = new JSONObject();
		  jo.put("attribute", p_key);
		  jo.put("inputType", p_type);
		  JSONArray jv = new JSONArray();
		  jv.put(p_value);
		  jo.put("values", jv);
		  ja.put(jo);
	  }
	  void addProductMetadata(JSONArray ja,String p_key,String p_value)  throws Exception{
		  JSONObject jo = new JSONObject();
		  jo.put("key",p_key);
		  jo.put("value", p_value);
		  ja.put(jo);
	  }
	  void addProductAttribute(JSONArray ja,String p_key,String p_value)  throws Exception{
		  addProductAttributeWithType(ja,p_key,"PLAIN_TEXT",p_value);
	  }
	  void addProductAttribute(JSONArray ja,String p_key,int p_value)  throws Exception{
		  addProductAttributeWithType(ja,p_key,"NUMERIC",""+p_value);
	  }
	  void addProductAttribute(JSONArray ja,String p_key,double p_value)  throws Exception{
		  addProductAttributeWithType(ja,p_key,"NUMERIC",""+p_value);
	  }
	  void addProductAttribute(JSONArray ja,String p_key,JSONObject p_value)  throws Exception{
		  if(p_value == null) addProductAttributeWithType(ja,p_key,"RICH_TEXT","");
		  else if(p_value.opt("rawtext") != null) {
			  String ss = p_value.getString("rawtext");
			  addProductAttributeWithType(ja,p_key,"RICH_TEXT",ss);
		  } else {
			  addProductAttributeWithType(ja,p_key,"RICH_TEXT",p_value.toString());
		  }
	  }
	  void addProductChannel(JSONArray ja, String p_channel, boolean p_visible, boolean p_published)  throws Exception{
		 JSONObject jo = new JSONObject();
		 jo.put("slug", p_channel);
		 jo.put("isPublished", p_published);
		 jo.put("visibleInListings", p_visible);
		 jo.put("publishedAt", "2025-11-01T00:00:00Z");
		 ja.put(jo);
	  }
	  void addVariantChannel(JSONArray ja, String p_channel, double p_price , double p_cost)  throws Exception{
		 JSONObject jo = new JSONObject();
		 jo.put("slug", p_channel);
		 jo.put("price", ""+p_price);
		 jo.put("costPrice", ""+p_cost);
		 ja.put(jo);
	  }
	  
	  public class SyncProductCallBack implements RpcServlet {
			Hashtable<String,JSONObject> insertVHash;
			Hashtable<String,JSONObject> deleteVHash;
			Hashtable<String,JSONObject> insertHash;
			Hashtable<String,JSONObject> deleteHash;
		    int pUpd;
		    int vUpd;
			
			public SyncProductCallBack(
					Hashtable<String,JSONObject> p_insertHash,
					Hashtable<String,JSONObject> p_deleteHash,
					Hashtable<String,JSONObject> p_insertVHash,
					Hashtable<String,JSONObject> p_deleteVHash
					) {
				insertHash = p_insertHash;
				deleteHash = p_deleteHash;
				insertVHash = p_insertVHash;
				deleteVHash = p_deleteVHash;
				
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
				return null;
			}
			
			public String variantRead(String p_str) {
				try {
					JSONObject jo = new JSONObject(p_str);
					JSONArray ja = jo.getJSONArray("data");

for(int i=0;i<ja.length();i++) {
	JSONObject joSaleor = ja.getJSONObject(i);
	String sku = joSaleor.getString("sku");
	JSONObject joErp = insertVHash.get(sku);
	if(joErp == null) {
		deleteVHash.put(sku, joSaleor);
		} else {
			boolean needSync = checkAndSyncProductVariant(joErp, joSaleor);
			if(!needSync) {
				insertVHash.remove(sku);
			} else {
				vUpd++;
			}
			/*
			if(needSync) {
				updateVHash.put(sku, joSaleor);
			}
			insertVHash.remove(sku);
			*/
		}
}
				} catch (Exception ex) {
					UniLog.log(ex);
				}
				return("OK");
			}
			public String productRead(String p_str) {
				try {
					JSONObject jo = new JSONObject(p_str);
					JSONArray ja = jo.getJSONArray("data");
					
					for(int i=0;i<ja.length();i++) {
						JSONObject joSaleor = ja.getJSONObject(i);
						String slug = joSaleor.getString("slug");
						JSONObject joErp = insertHash.get(slug);
						if(joErp == null) {
							deleteHash.put(slug, joSaleor);
			 			} else {
			 				boolean needSync = checkAndSyncProductRecord(joErp, joSaleor);
			 				if(!needSync) {
			 					insertHash.remove(slug);
			 				} else {
			 					pUpd++;
			 				}
			 				/*
			 				if(needSync) {
			 					updateHash.put(slug, joSaleor);
			 				}
			 				insertHash.remove(slug);
			 				*/
			 			}
					}					
					
				} catch (Exception ex) {
					UniLog.log(ex);
				}
				return("OK");
			}
		} 
	
	  private final int MAX_RECORD=10000;	
	  static final int cntPerFetch = 64;
	  static final int cntPerFetch2 = 512;
	  private String syncProductRecord(RpcClient rpcSaleor,boolean syncProduct,boolean syncVariant,boolean syncMedia,boolean testOnly,List<Integer> p_irgList) throws Exception {
		  	Value v;
			Hashtable<String,JSONObject> insertHash = new Hashtable<String,JSONObject>();
			Hashtable<String,JSONObject> updateHash = new Hashtable<String,JSONObject>();
			Hashtable<String,JSONObject> deleteHash = new Hashtable<String,JSONObject>();
			Hashtable<String,JSONObject> insertVHash = new Hashtable<String,JSONObject>();
			Hashtable<String,JSONObject> updateVHash = new Hashtable<String,JSONObject>();
			Hashtable<String,JSONObject> deleteVHash = new Hashtable<String,JSONObject>();
			Hashtable<String,JSONObject> insertMHash = new Hashtable<String,JSONObject>();
			Hashtable<String,JSONObject> updateMHash = new Hashtable<String,JSONObject>();
			Hashtable<String,JSONObject> deleteMHash = new Hashtable<String,JSONObject>();
			if(p_irgList != null) {
				StringUtil strU = new StringUtil();
				for(int irg : p_irgList) {
					strU.cat(""+irg, ",");
				}
				clear();
				clearCondition();
				addCustomCondition("consgp_qty > 0 and st_webprice > 0 and sttp_name in ('Sake','Spirit','Wine','Sparkling Tea') and pds_irg in (" + strU.toString() + ")");
				query();
			} else {
				clear();
				clearCondition();
				addCustomCondition("consgp_qty > 0 and st_webprice > 0 and sttp_name in ('Sake','Spirit','Wine','Sparkling Tea')");
				query();
			}
			int recCount = getRowCount();
			int idx = 0;
//			JSONObject joErp = null;
//			JSONObject joSaleor = null;
			
			if(recCount > MAX_RECORD) recCount = MAX_RECORD;
			for(int i=0;i<recCount;i++) {
				loadOneRecV(i);
				JSONObject joErp = BiCellCollectionToJsonInterface.BiCellCollectionToJSON(getCurrentCollection());
				/*
				String mbrand= joErp.optString("st_mbrand");
				double msize2= joErp.optDouble("st_msize2");
				double msize3= joErp.optDouble("st_msize3");
				TableRec tr = getSelectUtil().getQueryResult("select * from pdphoto_id where pdpi_code = ? and pdpi_vol = ? and pdpi_year = ?",
							new Wherecl().appendArgument(mbrand).appendArgument(msize2).appendArgument(msize3)
						);
					if(tr.getRecordCount() > 0) {
						tr.setRecPointer(0);
						int photoid = tr.getFieldInt("pdpi_photoid");
						if(photoid > 0) {
							joErp.put("st_photoid", photoid);
							joErp.put("st_photofmt", "jpg");
						}
					}
					*/
					if(syncProduct) {
						insertHash.put(joErp.getString("st_slug"), joErp);
					}
					if(syncVariant) {
						insertVHash.put(
							BiCellCollection.makeSlug(
											joErp.getString("st_icode"),
											""+joErp.getInt("pds_org")
											)
							, joErp);
					}
					if(syncMedia) {
						String slug =joErp.getString("st_slug");
						if(slug.equals("wine-red-bordeaux-abcr25b-43499")) {
							int cc ;
							cc = 0;
						}
						int photoid = joErp.getInt("st_photoid");
						if(photoid > 0) {
							insertMHash.put(joErp.getString("st_slug"), joErp);
						}
					}
			}
			rpcSaleor.open();

			SyncProductCallBack cb = new SyncProductCallBack(insertHash,deleteHash,insertVHash,deleteVHash);
			RpcServlet sv = cb;
			String svName = sv .getClass().getName();
			rpcSaleor.setRpcServlet(svName, sv);
			rpcSaleor.setTimeout(180000);
			if(syncProduct & p_irgList == null) {
			v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.getProductRecordsWithCallback",
					new VectorUtil().addElement(0).addElement(0).addElement(svName+".productRead").toVector()
					);
//			for(idx=0;idx<MAX_RECORD;idx+=cntPerFetch2) {
//				v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.getProductRecords",
//							new VectorUtil().addElement(idx).addElement(cntPerFetch2).toVector()
//							);
//				if(v == null || !v.toString().startsWith("OK")) {
//					UniLog.log("API getProductRecord failed");
//					return;
//				}
//				String ss = v.toString();
//				JSONArray ja = new JSONArray(ss.substring(4));
//				for(int i=0;i<ja.length();i++) {
//					JSONObject joSaleor = ja.getJSONObject(i);
//					String slug = joSaleor.getString("slug");
//					JSONObject joErp = insertHash.get(slug);
//					if(joErp == null) {
//						deleteHash.put(slug, joSaleor);
//		 			} else {
//		 				boolean needSync = checkAndSyncProductRecord(joErp, joSaleor);
//		 				if(!needSync) {
//		 					insertHash.remove(slug);
//		 				}
//		 				/*
//		 				if(needSync) {
//		 					updateHash.put(slug, joSaleor);
//		 				}
//		 				insertHash.remove(slug);
//		 				*/
//		 			}
//				}
//				if(ja.length() < cntPerFetch2) break;
//			}
			UniLog.log("WebPush 260420 SaleorSync ProductRecord " + insertHash.size() + " insert " + updateHash.size() + " update " + deleteHash.size() + " delete");
			}
			if(syncVariant && p_irgList == null) {
			v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.getProductVariantsWithCallback",
					new VectorUtil().addElement(0).addElement(0).addElement(svName+".variantRead").toVector()
					);
//			for(idx=0;idx<MAX_RECORD;idx+=cntPerFetch2) {
//				v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.getProductVariants",
//							new VectorUtil().addElement(idx).addElement(cntPerFetch2).toVector()
//							);
//				if(v == null || !v.toString().startsWith("OK")) {
//					UniLog.log("API getProductVariant failed");
//					return;
//				}
//				String ss = v.toString();
//				JSONArray ja = new JSONArray(ss.substring(4));
//				for(int i=0;i<ja.length();i++) {
//					JSONObject joSaleor = ja.getJSONObject(i);
//					String sku = joSaleor.getString("sku");
//					JSONObject joErp = insertVHash.get(sku);
//					if(joErp == null) {
//						deleteVHash.put(sku, joSaleor);
//		 			} else {
//		 				boolean needSync = checkAndSyncProductVariant(joErp, joSaleor);
//		 				if(!needSync) {
//		 					insertVHash.remove(sku);
//		 				}
//		 				/*
//		 				if(needSync) {
//		 					updateVHash.put(sku, joSaleor);
//		 				}
//		 				insertVHash.remove(sku);
//		 				*/
//		 			}
//				}
//				if(ja.length() < cntPerFetch2) break;
//			}
			UniLog.log("WebPush SaleorSync ProductVariant" + insertVHash.size() + " insert " + updateVHash.size() + " update " + deleteVHash.size() + " delete");
			}
			if(syncMedia && p_irgList == null) {
				int numUpdCnt = 0;
			for(idx=0;idx<MAX_RECORD;idx+=cntPerFetch2) {
				v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.getProductMedia",
							new VectorUtil().addElement(idx).addElement(cntPerFetch2).toVector()
							);
				if(v == null || !v.toString().startsWith("OK")) {
					UniLog.log("WebPush API getProductMedia failed");
					return("FAILAPI getProductMedia failed");
				}
				String ss = v.toString();
				JSONArray ja = new JSONArray(ss.substring(4));
				for(int i=0;i<ja.length();i++) {
					JSONObject joSaleor = ja.getJSONObject(i);
					String slug = joSaleor.getString("product");
					JSONArray media = joSaleor.getJSONArray("media");
					JSONObject joErp = insertMHash.get(slug);
					if(media.length() != 1) {
						if(media.length() > 1) {
							UniLog.log("WebPush delete media record with media length > 1");
							deleteMHash.put(slug, joSaleor); // media length should be 1 if media record exist, otherwise, delete it and insert again
						} else {
							if(joErp != null) {
								UniLog.log("WebPush media record with media length = 0 need insert back");
							}
						}
					} else {
						if(joErp == null) {
							deleteMHash.put(slug, joSaleor);
		 				} else {
		 					boolean needSync = checkAndSyncProductMedia(joErp, joSaleor);
		 					if(numUpdCnt > 10000) {
		 						insertMHash.remove(slug);	
		 					} else {
		 					if(needSync) {
		 						deleteMHash.put(slug, joSaleor);
//		 						updateMHash.put(slug, joSaleor);
		 						numUpdCnt++;
		 						UniLog.log("WebPush update photo for " + slug);
		 					} else {
		 						insertMHash.remove(slug);
		 					}
		 					}
		 				}
					}
				}
				if(ja.length() < cntPerFetch2) break;
			}
			UniLog.log("WebPush SaleorSync ProductMedia " + insertMHash.size() + " insert " + updateMHash.size() + " update " + deleteMHash.size() + " delete");
			}
			if(syncProduct && !testOnly) {
				if(deleteHash.size() > 0) {
					int cc;
					cc = 0;
				}
			}
			if(syncVariant && !testOnly) {
				if(deleteVHash.size() > 0) {
					Vector<String> delList = new Vector<String>();
					for(String sku : deleteVHash.keySet()) {
						delList.add(sku);
					}
					v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.deleteProductVariants",delList);
					if(v == null || !v.toString().startsWith("OK")) {
						UniLog.log("WebPush API delete producvariant failed");
						return("FAILAPI delete producvariant failed");
					}
				}
			}

			if(syncMedia && !testOnly) {
				if(deleteMHash.size() > 0) {
					Vector<String> delList = new Vector<String>();
					for(String sku : deleteMHash.keySet()) {
						delList.add(sku);
						if(delList.size() >= 100) {

					v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.deleteProductMedia",delList);
					if(v == null || !v.toString().startsWith("OK")) {
						UniLog.log("WebPush API delete producmedia failed");
						return("FAILAPI delete producmedia failed");
					}
							delList.clear();
							
						}
					}
					if(delList.size() > 0) {
					v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.deleteProductMedia",delList);
					if(v == null || !v.toString().startsWith("OK")) {
						UniLog.log("WebPush API delete producmedia failed");
						return("FAILAPI delete producmedia failed");
					}
					}
				}
			}
			
			if(syncProduct) {
			if(insertHash.size() > 0) {
				for(String slug : insertHash.keySet()) {
					JSONObject ejo = insertHash.get(slug);
					JSONObject sjo = new JSONObject();
					if(!StringUtils.isBlank(ejo.getString("stbd_name"))) {
						sjo.put("name",ejo.get("stbd_name"));
					} else {
						sjo.put("name",ejo.get("st_iname"));
					}
					sjo.put("slug",slug);
					sjo.put("description", ejo.getString("st_icode"));
					sjo.put("productType",BiCellCollection.makeSlug(
											ejo.getString("sttp_name"),
											ejo.getString("mt_tpname")
											));
					sjo.put("category",BiCellCollection.makeSlug(ejo.getString("sttp_name")));
					JSONArray ja0 = new JSONArray();
					addProductMetadata(ja0,"icode",ejo.getString("st_icode"));
					if(!StringUtils.isBlank(ejo.getString("stbd_name"))) {
						addProductMetadata(ja0,"brand",ejo.getString("stbd_name"));
					}
					if(!StringUtils.isBlank(ejo.getString("st_maturity"))) {
						addProductMetadata(ja0,"maturity",ejo.getString("st_maturity"));
					}
					if(!StringUtils.isBlank(ejo.getString("st_vintage"))) {
						addProductMetadata(ja0,"vintage",ejo.getString("st_vintage"));
					}
					if(!StringUtils.isBlank(ejo.getString("storg_ecountry"))) {
						addProductMetadata(ja0,"country",ejo.getString("storg_ecountry"));
					}
					if(!StringUtils.isBlank(ejo.getString("storg_name"))) {
						addProductMetadata(ja0,"region",ejo.getString("storg_name"));
					}
					if(!StringUtils.isBlank(ejo.getString("stbd_appellation"))) {
						addProductMetadata(ja0,"appellation",ejo.getString("stbd_appellation"));
					}
					sjo.put("metadata", ja0);
					
					JSONArray ja1 = new JSONArray();
					addProductAttribute(ja1,"icode",ejo.getString("st_icode"));
					String es =ejo.getString("storg_name");
					String cs =ejo.getString("storg_cname");
					if(!StringUtils.isBlank(es) && !StringUtils.isBlank(cs)) {
						addProductAttributeWithChinese(ja1,"region",es,cs);
					} else {
						addProductAttribute(ja1,"region", ejo.getString("storg_name"));
					}
					es =ejo.getString("stbd_appellation");
					cs =ejo.getString("stbd_cappellation");
					if(!StringUtils.isBlank(es) && !StringUtils.isBlank(cs)) {
						addProductAttributeWithChinese(ja1,"appellation", es,cs);
					} else {
						addProductAttribute(ja1,"appellation", ejo.getString("stbd_appellation"));
					}
					addProductAttribute(ja1,"maturity",ejo.getString("st_maturity"));
					addProductAttribute(ja1,"weight",ejo.getInt("st_msize2"));
					addProductAttribute(ja1,"long-description",(JSONObject) null);
					addProductAttribute(ja1,"packing",ejo.getInt("st_msize1"));
					addProductAttribute(ja1,"volume",""+ejo.getInt("st_msize2")+"ml");
					addProductAttribute(ja1,"score",ejo.getInt("st_score0"));
					addProductAttribute(ja1,"vintage",ejo.getString("st_vintage"));
					JSONObject jx = new JSONObject();
					jx.put("rawtext", ejo.getString("stnd_note"));
					addProductAttribute(ja1,"tasting",jx);
					es =ejo.getString("storg_ecountry");
					cs =ejo.getString("storg_ccountry");
					if(!StringUtils.isBlank(es) && !StringUtils.isBlank(cs)) {
						addProductAttributeWithChinese(ja1,"country",es,cs);
					} else {
						addProductAttribute(ja1,"country",ejo.getString("storg_ecountry"));
					}
					es =ejo.getString("stbd_name");
					cs =ejo.getString("stbd_cname");
					if(!StringUtils.isBlank(es) && !StringUtils.isBlank(cs)) {
						addProductAttributeWithChinese(ja1,"brand",es,cs);
					} else {
						addProductAttribute(ja1,"brand",ejo.getString("stbd_name"));
					}
					addProductAttribute(ja1,"class",ejo.getString("st_modelno"));
					sjo.put("attributes", ja1);
					ja1 = new JSONArray();
					addProductChannel(ja1,"hk",true,true);
					sjo.put("channels", ja1);
					updateHash.put(slug, sjo);
				}
			}
			}
			if(syncVariant) {
			if(insertVHash.size() > 0) {
				for(String slug : insertVHash.keySet()) {
					JSONObject ejo = insertVHash.get(slug);
					JSONObject sjo = new JSONObject();
					sjo.put("sku",slug);
//					sjo.put("name",slug);
					sjo.put("productSlug",ejo.get("st_slug"));
					JSONArray ja1 = new JSONArray();
					addProductAttribute(ja1,"owner",ejo.getString("or_cocode"));
					addProductAttribute(ja1,"org",ejo.getInt("pds_org"));
					sjo.put("attributes", ja1);
					ja1 = new JSONArray();
//					double price = ejo.getDouble("consgp_price");
//					price = getWebConsigpprice(price);
					double price = ejo.getDouble("st_webprice");
					addVariantChannel(ja1,"hk",price,ejo.getDouble("consgp_cost"));
					sjo.put("channels", ja1);
					updateVHash.put(slug, sjo);
				}
			}
			}
			if(syncMedia && !testOnly) {
			if(insertMHash.size() > 0) {
				for(String slug : insertMHash.keySet()) {
					if(slug.equals("wine-red-bordeaux-abcr25b-43499")) {
						int cc ;
						cc = 0;
					}
					JSONObject ejo = insertMHash.get(slug);
					if(ejo.getInt("st_photoid") > 0 /* && !StringUtils.isBlank(ejo.getString("st_photofmt") ) */) {
					JSONObject sjo = new JSONObject();
					sjo.put("product",slug);
					JSONArray ja1 = new JSONArray();
					JSONObject jo1 = new JSONObject();
					jo1.put("url", "https://hub.erpv4.com/saleorsync/getResource?url="+ejo.getString("st_photourl"));
					jo1.put("alt", ejo.getString("st_photourl"));
					jo1.put("sortOrder", 0);
					ja1.put(jo1);
					sjo.put("media", ja1);
					updateMHash.put(slug, sjo);
					}
				}
			}
				
			}
			if(syncProduct && !testOnly) {
			if(updateHash.size() > 0) {
				JSONArray ja = null;
				for(JSONObject jo : updateHash.values()) {
					if(ja == null) {
						ja = new JSONArray();
					}
					UniLog.log("WebPush update One Product " + jo.getString("slug"));
					ja.put(jo);
					if(ja.length() >= cntPerFetch) {
						v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.insertProductRecords",
							new VectorUtil().addElement(ja.toString()).toVector()
						);
						if(v == null || !v.toString().startsWith("OK")) {
							UniLog.log("WebPush API insert productrecord failed");
							return("FAILAPI insert productrecord failed");
						}
						ja = null;
					}
				}
				if(ja != null) {
						v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.insertProductRecords",
							new VectorUtil().addElement(ja.toString()).toVector()
						);
						if(v == null || !v.toString().startsWith("OK")) {
							UniLog.log("WebPush API insert productrecord failed");
							return("FAILAPI insert productrecord failed");
						}
						ja = null;
				}
			}
			}
			if(syncVariant && !testOnly) {
			if(updateVHash.size() > 0) {
				JSONArray ja = null;
				for(JSONObject jo : updateVHash.values()) {
					if(ja == null) {
						ja = new JSONArray();
					}
					ja.put(jo);
					if(ja.length() >= cntPerFetch) {
						v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.insertProductVariants",
							new VectorUtil().addElement(ja.toString()).toVector()
						);
						if(v == null || !v.toString().startsWith("OK")) {
							UniLog.log("WebPush API insert productvariants failed");
							return("FAILAPI insert productvariants failed");
						}
						ja = null;
					}
				}
				if(ja != null) {
						v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.insertProductVariants",
							new VectorUtil().addElement(ja.toString()).toVector()
						);
						if(v == null || !v.toString().startsWith("OK")) {
							UniLog.log("WebPush API insert productvariants failed");
							return("FAILAPI insert productvariants failed");
						}
						ja = null;
				}
			}
			}
			if(syncMedia && !testOnly) {
			if(updateMHash.size() > 0) {
				for(JSONObject jo : updateMHash.values()) {
					String slug = jo.getString("product");
					if(slug.equals("wine-red-bordeaux-abcr25b-43499")) {
						int cc ;
						cc = 0;
					}
					JSONArray ja = jo.getJSONArray("media");
					if(ja.length() > 0) {
						JSONObject jo1 = ja.getJSONObject(0);
						String url = jo1.getString("url");
						String alt = jo1.getString("alt");
						v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.addMediaToProduct",
							new VectorUtil()
							.addElement(slug)
							.addElement(url)
							.addElement("jpg")
							.addElement(alt)
							.toVector()
						);
						UniLog.log("WebPush upload media " + slug + " " + url+".jpg");
					}
				}
//				JSONArray ja = null;
//				for(JSONObject jo : updateMHash.values()) {
//					if(ja == null) {
//						ja = new JSONArray();
//					}
//					ja.put(jo);
//					if(ja.length() >= cntPerFetch) {
//						v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.insertProductMedia",
//							new VectorUtil().addElement(ja.toString()).toVector()
//						);
//						if(v == null || !v.toString().startsWith("OK")) {
//							LogUtil.log("API insert productmedia failed");
//							return;
//						}
//						ja = null;
//					}
//				}
//				if(ja != null) {
//						v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.insertProductMedia",
//							new VectorUtil().addElement(ja.toString()).toVector()
//						);
//						if(v == null || !v.toString().startsWith("OK")) {
//							LogUtil.log("API insert productmedia failed");
//							return;
//						}
//						ja = null;
//				}
			}
			}
			return(String.format("OK  %8d%8d%8d%8d%8d%8d, ",
							insertHash.size() - cb.pUpd,
							cb.pUpd,
							deleteHash.size(),
							insertVHash.size() - cb.vUpd,
							cb.vUpd,
							deleteVHash.size()
						));
	  }
	

	@Override
	public String biRpcCallSegment(String p_segName, String p_jsonstr) {
		// TODO Auto-generated method stub
		RpcClient rpcSaleor = null;
		
		try {
		if(p_segName.equals("downLoadGoogleMerchant")) {
			 clear();
			 clearCondition();
			 clearOrderBy();
			 addCustomCondition("consgp_qty > 0.0 and st_webprice > 0.0 and sttp_name in('Sake','Spirit','Wine','Sparkling Tea')");
			 query();
			 JSONObject jo = downloadGoogleMerchantCenterProduct(this);
			 if(jo != null) {
				 return("OK  "+convert(jo));
			 }
		}
		if(p_segName.equals("syncProductRecords")) {
		boolean syncMedia = false;
		boolean syncProduct = false;
		boolean syncVariant = false;
		boolean testOnly = false;
//		RpcClient rpcSaleor = new RpcClient("192.168.46.16",6022,30000);
		rpcSaleor = new RpcClient("192.168.33.3",6022,30000);
		if(!StringUtils.isBlank(p_jsonstr)) {
			try {
				JSONObject jo = new JSONObject(p_jsonstr);
				String ss = jo.optString("syncMedia",null);
				if("Y".equals(ss)) syncMedia = true;
				ss = jo.optString("syncProduct",null);
				if("Y".equals(ss)) syncProduct= true;
				ss = jo.optString("syncVariant",null);
				if("Y".equals(ss)) syncVariant= true;
				ss = jo.optString("testOnly",null);
				if("Y".equals(ss)) testOnly= true;
			} catch (JSONException jex) {
				UniLog.log(jex);
			}
		}
			JSONObject jo = new JSONObject(p_jsonstr);
			JSONArray ja = jo.optJSONArray("irgList");
			String rtn;
			if(ja != null) {
				ArrayList<Integer> irgList = new ArrayList<Integer>();
				for(int i=0;i<ja.length();i++) {
					irgList.add(ja.getInt(i));
				}
//				RpcClient rpcSaleor = new RpcClient("192.168.33.3",6022,30000);
				rtn = syncProductRecord(rpcSaleor,true,true,false,false,irgList);
			} else {
//				rtn = syncProductRecord(rpcSaleor,true,true,false,false,null); /* live system should use this */
//					rtn = syncProductRecord(rpcSaleor,false,false,true,false,null); 
				
//				rtn = syncProductRecord(rpcSaleor,true,false,false,false,null);
//				rtn = syncProductRecord(rpcSaleor,true,false,false,true,null);
				rtn = syncProductRecord(rpcSaleor,syncProduct,syncVariant,syncMedia,testOnly,null); /* live system should use this */
			}
			rpcSaleor.close();
			/*
			jo = new JSONObject();
			jo.put("ok", true);
			return("OK  "+ jo.toString());
			*/
			return(rtn == null  ? "FAIL" : rtn);
		}
		} catch (Exception ex) {
			UniLog.log(ex);
		} finally {
			if(rpcSaleor != null) rpcSaleor.close();
		}
		return null;
	}
	private static String escapeXml(String value) {
	        if (value == null) return "";
	        return value
	            .replace("&", "&amp;")
	            .replace("<", "&lt;")
	            .replace(">", "&gt;")
	            .replace("\"", "&quot;")
	            .replace("'", "&apos;");
	}	
	private static void tag(StringBuilder xml, String name, String value, int indent) {
	       spaces(xml, indent)
	           .append("<").append(name).append(">")
	           .append(escapeXml(value))
	           .append("</").append(name).append(">\n");
	}
    private static void gtag(StringBuilder xml, String name, String value, int indent) {
        spaces(xml, indent)
            .append("<g:").append(name).append(">")
            .append(escapeXml(value))
            .append("</g:").append(name).append(">\n");
    }
	private static StringBuilder spaces(StringBuilder xml, int count) {
        for (int i = 0; i < count; i++) {
            xml.append(' ');
        }
        return xml;
    }	
    private static void require(JSONObject obj, String key, int index) {
        if (!obj.has(key) || obj.optString(key).trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Product index " + index + " missing required field: " + key
            );
        }
    } 
    private static void validateProduct(JSONObject p, int index) {
        require(p, "id", index);
        require(p, "title", index);
        require(p, "description", index);
        require(p, "link", index);
        require(p, "image_link", index);
        require(p, "availability", index);
        require(p, "price", index);
    }
    private static void optionalGTag(StringBuilder xml, String name, JSONObject obj, int indent) {
        if (obj.has(name)) {
            String value = obj.optString(name, "").trim();
            if (!value.isEmpty()) {
                gtag(xml, name, value, indent);
            }
        }
    }	
	public static String convert(JSONObject root) throws JSONException {

        JSONObject feed = root.optJSONObject("feed");
        if (feed == null) {
            throw new IllegalArgumentException("Missing root object: feed");
        }

        JSONArray products = feed.optJSONArray("products");
        if (products == null || products.length() == 0) {
            throw new IllegalArgumentException("Missing or empty: feed.products");
        }

        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<rss version=\"2.0\" xmlns:g=\"http://base.google.com/ns/1.0\">\n");
        xml.append("  <channel>\n");

        tag(xml, "title", feed.optString("title", "Product Feed"), 4);
        tag(xml, "link", feed.optString("link", ""), 4);
        tag(xml, "description", feed.optString("description", "Product feed"), 4);

        for (int i = 0; i < products.length(); i++) {
            JSONObject p = products.getJSONObject(i);
            validateProduct(p, i);

            xml.append("    <item>\n");

            gtag(xml, "id", p.getString("id"), 6);
            gtag(xml, "title", p.getString("title"), 6);
            gtag(xml, "description", p.getString("description"), 6);
            gtag(xml, "link", p.getString("link"), 6);
            gtag(xml, "image_link", p.getString("image_link"), 6);
            gtag(xml, "availability", p.getString("availability"), 6);
            gtag(xml, "price", p.getString("price"), 6);
            gtag(xml, "condition", p.optString("condition", "new"), 6);

            optionalGTag(xml, "brand", p, 6);
            optionalGTag(xml, "item_group_id", p, 6);
            optionalGTag(xml, "gtin", p, 6);
            optionalGTag(xml, "mpn", p, 6);
            optionalGTag(xml, "google_product_category", p, 6);
            optionalGTag(xml, "product_type", p, 6);
            optionalGTag(xml, "adult", p, 6);

            if (p.has("shipping")) {
//                appendShipping(xml, p.getJSONObject("shipping"), 6);
            }

            xml.append("    </item>\n");
        }

        xml.append("  </channel>\n");
        xml.append("</rss>\n");

        return xml.toString();
    }
	public static ReturnMsg doBeforeAction(JSONObject jo, JSONArray ja,HashSet<String> productHash,BiResult p_result,int cnt) throws Exception {
		JSONObject jf = new JSONObject();
		jf.put("title",  "WineCave Product Feed");
		jf.put("link",  "https://winecavehk.com");
		jf.put("description", "WineCave Google Merchant product feed");
		jf.put("products", ja);
		jo.put("feed", jf);
		return (ReturnMsg.defaultOk);
	}	
	public static ReturnMsg doProcessAction(JSONArray ja,HashSet<String> productHash,BiResult p_result,int p_recIdx) throws Exception {
		if(StringUtils.isBlank(p_result.getCellString("st_photourl"))) {
			UniLog.log("Skip " + p_result.getCellString("st_icode") + " no photo");
			return (ReturnMsg.defaultOk);
		}
		if(StringUtils.isBlank(p_result.getCellString("stnd_note"))) {
			UniLog.log("Skip " + p_result.getCellString("st_icode") + " no tasting notes");
			return (ReturnMsg.defaultOk);
		}
		if(productHash.contains(p_result.getCellString("st_slug"))) {
			return (ReturnMsg.defaultOk);
		}
		productHash.add(p_result.getCellString("st_slug"));
		JSONObject ji = new JSONObject();
		ji.put("id", p_result.getCellString("st_slug"));
		ji.put("availability", "in_stock");
		ji.put("price", String.format("%.2f HKD", p_result.getCellDouble("st_webprice")));
		String ss = p_result.getCellString("st_iname")+" "+p_result.getCellInt("st_msize2")+"ml/Btl ";
		if(p_result.getCellInt("st_msize1") > 1) {
			ss += " "+p_result.getCellInt("st_msize1") + "/case";
		}
		ji.put("title", ss);
		ji.put("description", p_result.getCellString("stnd_note"));
		ji.put("link", "https://www.winecavehk.com/hk/en/products/"+p_result.getCellString("st_slug"));
		ji.put("image_link", "https://hub.erpv4.com/saleorsync/getResource?url="+p_result.getCellString("st_photourl"));
		ji.put("brand", p_result.getCellString("stbd_name"));
		ji.put("product_type", p_result.getCellString("mt_tpname"));
		ji.put("google_product_category", "Food, Beverages & Tobacco > Beverages > Alcoholic Beverages > Wine");
		ji.put("adult", "yes");
		ja.put(ji);
		return (ReturnMsg.defaultOk);
	}	
	static public JSONObject downloadGoogleMerchantCenterProduct(BiResult p_br) throws Exception {
		HashSet<String> productHash ;
		JSONObject jo;
		JSONArray ja;
		productHash = new HashSet<String>();
		jo = new JSONObject();
		ja = new JSONArray();
		ReturnMsg rtn = doBeforeAction(jo,ja,productHash,p_br,p_br.getRowCount());
		if(rtn != null && !rtn.getStatus()) throw new Exception("Download Error : "+rtn.getMsg());
		for(int i=0;i<p_br.getRecordCount();i++) {
			p_br.loadOneRecV(i);
			rtn = doProcessAction(ja,productHash,p_br,i);
			if(rtn != null && !rtn.getStatus()) throw new Exception("Download Error : "+rtn.getMsg());
		}
		return(jo);
	}
}

