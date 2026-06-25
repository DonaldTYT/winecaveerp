package com.kikyosoft.servlet.larva.pages;


import javax.servlet.*;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kikyosoft.rpccall.RpcClient;
import com.kikyosoft.rpccall.Value;
import com.kikyosoft.utils.LogUtil;
import com.kikyosoft.utils.StringUtil;
import com.kikyosoft.utils.VectorUtil;
import com.kyoko.common.CoreLog;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.winecave.WineCaveUtil;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.IntConsumer;
@MultipartConfig
@WebServlet(name = "SaleorSyncController", urlPatterns = {"/larva/pages/saleor-sync"})
public class SaleorSyncController extends HttpServlet {

  // --- simple background job store (for progress mode) ---
  private final ExecutorService exec = Executors.newFixedThreadPool(2);
  private final ConcurrentMap<String, JobStatus> jobs = new ConcurrentHashMap<>();

  // server toggles (can also be set via context-param)
  private boolean progressEnabled = true;
  private String  statusUrl; // e.g. /larva/pages/saleor-sync-status
  
  private final int MAX_RECORD=10000;

  @Override public void init() {
    ServletContext ctx = getServletContext();
    progressEnabled = !"false".equalsIgnoreCase(
        Optional.ofNullable(ctx.getInitParameter("saleorsync.progress.enabled")).orElse("true"));
    String ctxPath = ctx.getContextPath();
    statusUrl = Optional.ofNullable(ctx.getInitParameter("saleorsync.progress.statusUrl"))
        .orElse(ctxPath + "/larva/pages/saleor-sync-status");
    ctx.setAttribute("saleorSync.jobs", jobs);   // <-- make jobs visible to the status servlet 
    // uncomment if you want sync/no-progress by default:
    // progressEnabled = false;
  }

  @Override public void destroy() { exec.shutdownNow(); }

  // --- Handle CORS for cross-origin storefront calls ---
  private void applyCors(HttpServletRequest req, HttpServletResponse resp) {
    String origin = req.getHeader("Origin");
    if (origin != null) {
      // In production, whitelist your Next.js origin instead of echoing.
      resp.setHeader("Access-Control-Allow-Origin", origin);
      resp.setHeader("Vary", "Origin");
      resp.setHeader("Access-Control-Allow-Credentials", "true");
      resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With");
      resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    }
  }

  @Override
  protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    applyCors(req, resp);
    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
    applyCors(req, resp);

    boolean wantsJson =
        "XMLHttpRequest".equals(req.getHeader("X-Requested-With")) ||
        Optional.ofNullable(req.getHeader("Accept")).orElse("").contains("application/json") ||
        "json".equalsIgnoreCase(req.getParameter("format"));

    if (wantsJson) {
      // optional: return simple capability info for Next.js
      resp.setCharacterEncoding("UTF-8");
      resp.setContentType("application/json");
      resp.getWriter().write("{\"ok\":true,\"progress\":"+progressEnabled+"}");
      return;
    }

    // HTML flow (standalone page if you use one; otherwise you just include JSPFs in dashboard)
    req.setAttribute("assetsBase", "/larva/assets");
    Map<String,String> ui = new LinkedHashMap<>();
    ui.put("htmlTitle","Saleor Sync");
    req.setAttribute("uiText", ui);

    // If you don’t keep a standalone page, you can 204 here.
    // req.getRequestDispatcher("/WEB-INF/views/larva/pages/saleor-sync.jsp").forward(req, resp);
    resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    	ZkSessionHelper sp = (ZkSessionHelper) ZkSessionHelper.getSessionHelper(req, resp);

    	if(!sp.isLogin()) {
    		writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Failed: " + "No Login ");
    		return;
    	}
	  
	  
	  
    applyCors(req, resp);
    req.setCharacterEncoding(StandardCharsets.UTF_8.name());

    boolean wantsJson =
        "XMLHttpRequest".equals(req.getHeader("X-Requested-With")) ||
        Optional.ofNullable(req.getHeader("Accept")).orElse("").contains("application/json") ||
        "json".equalsIgnoreCase(req.getParameter("format"));

    // Accept both urlencoded (from your JSPF) and JSON (from Next.js)
    boolean isJsonBody = Optional.ofNullable(req.getContentType()).orElse("").toLowerCase().contains("application/json");

    // ---- read inputs
    Map<String,String> p = isJsonBody ? readJsonBody(req) : req.getParameterMap().entrySet().stream()
        .collect(LinkedHashMap::new,
            (m,e)-> m.put(e.getKey(), e.getValue()!=null && e.getValue().length>0 ? e.getValue()[0] : null),
            Map::putAll);

    // flags (checkboxes)
    boolean productCategory = checked(p.get("productCategory"));
    boolean productType     = checked(p.get("productType"));
    boolean productDetail   = checked(p.get("productDetail"));
    boolean productVariant  = checked(p.get("productVariant"));
    boolean testOnly        = checked(p.get("testOnly"));

    String remarks          = trim(p.get("remarks"));
    String action           = trim(p.get("action"));   // in case you have multiple submit buttons

    resp.setCharacterEncoding("UTF-8");
    resp.setContentType("application/json");

    if (progressEnabled) {
      // background job + immediate JSON with jobId for polling
      String jobId = UUID.randomUUID().toString();
      JobStatus st = new JobStatus(); st.percent=0; st.message="Queued"; st.done=false; st.ok=true;
      jobs.put(jobId, st);

      exec.submit(() -> {
        try {
          runSyncWork(sp,remarks, productCategory, productType, productDetail, productVariant, testOnly, action,
              pct -> st.percent = pct);
          st.percent = 100;
          st.done = true; st.ok = true; st.message = "Saleor sync completed.";
        } catch (Exception e) {
          st.done = true; st.ok = false; st.message = "Error: " + e.getMessage();
        }
      });

      String json = "{"
          + "\"ok\":true,"
          + "\"message\":\"Sync started.\","
          + "\"jobId\":\""+escape(jobId)+"\","
          + "\"progress\":{\"enabled\":true,\"statusUrl\":\""+escape(statusUrl)+"\"}"
          + "}";
      resp.getWriter().write(json);
    } else {
      // synchronous (no polling)
      try {
        runSyncWork(sp,remarks, productCategory, productType, productDetail, productVariant, testOnly, action, null);
        resp.getWriter().write("{\"ok\":true,\"message\":\"Saleor sync completed.\"}");
      } catch (Exception ex) {
        writeJsonError(resp, HttpServletResponse.SC_BAD_REQUEST, "Failed: " + ex.getMessage());
      }
    }
  }
  
  boolean checkAndSyncCategory(JSONObject joErp, JSONObject joSaleor) {
	  boolean needSync = false;
	  if(!joErp.getString("sttp_name").equals(joSaleor.getString("name"))) {
		  joSaleor.put("name",joErp.getString("sttp_name"));
		  needSync = true;
	  }
	  return(needSync);
  }

  boolean checkAndSyncProductType(JSONObject joErp, JSONObject joSaleor) {
	  boolean needSync = true;
	  return(needSync);
  }

  boolean checkAndSyncProductRecord(JSONObject ejo, JSONObject joSaleor) {
	  boolean needSync = false;
	  JSONArray ja = joSaleor.getJSONArray("metadata");
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
  boolean checkAndSyncProductVariant(JSONObject ejo, JSONObject joSaleor) {
	  JSONArray ja = joSaleor.getJSONArray("channels");
	  boolean channelFound = false;
	  for(int i=0;i<ja.length();i++) {
		  JSONObject jo = ja.getJSONObject(i);
		  String channelName = jo.getString("slug");
		  if(channelName.equals("hk")) {
			  channelFound = true;
		  }
		  double price = jo.getDouble("price");
//		  double erpprice = WineCaveUtil.getWebConsigpprice( ejo.getDouble("consgp_price"));
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
	  return(needSync);
  }
  
  void addProductAttributeWithType(JSONArray ja,String p_key,String p_type,Object p_value) {
	  JSONObject jo = new JSONObject();
	  jo.put("attribute", p_key);
	  jo.put("inputType", p_type);
	  JSONArray jv = new JSONArray();
	  jv.put(p_value);
	  jo.put("values", jv);
	  ja.put(jo);
  }
  void addProductMetadata(JSONArray ja,String p_key,String p_value) {
	  JSONObject jo = new JSONObject();
	  jo.put("key",p_key);
	  jo.put("value", p_value);
	  ja.put(jo);
  }
  void addProductAttribute(JSONArray ja,String p_key,String p_value) {
	  addProductAttributeWithType(ja,p_key,"PLAIN_TEXT",p_value);
  }
  void addProductAttribute(JSONArray ja,String p_key,int p_value) {
	  addProductAttributeWithType(ja,p_key,"NUMERIC",""+p_value);
  }
  void addProductAttribute(JSONArray ja,String p_key,double p_value) {
	  addProductAttributeWithType(ja,p_key,"NUMERIC",""+p_value);
  }
  void addProductAttribute(JSONArray ja,String p_key,JSONObject p_value) {
	  if(p_value == null) addProductAttributeWithType(ja,p_key,"RICH_TEXT","");
	  else if(p_value.opt("rawtext") != null) {
		  String ss = p_value.getString("rawtext");
		  addProductAttributeWithType(ja,p_key,"RICH_TEXT",ss);
	  } else {
		  addProductAttributeWithType(ja,p_key,"RICH_TEXT",p_value.toString());
	  }
  }
  void addProductChannel(JSONArray ja, String p_channel, boolean p_visible, boolean p_published) {
	 JSONObject jo = new JSONObject();
	 jo.put("slug", p_channel);
	 jo.put("isPublished", p_published);
	 jo.put("visibleInListings", p_visible);
	 jo.put("publishedAt", "2025-11-01T00:00:00Z");
	 ja.put(jo);
  }
  void addVariantChannel(JSONArray ja, String p_channel, double p_price , double p_cost) {
	 JSONObject jo = new JSONObject();
	 jo.put("slug", p_channel);
	 jo.put("price", ""+p_price);
	 jo.put("costPrice", ""+p_cost);
	 ja.put(jo);
  }
  
  static final int cntPerFetch = 64;
  static final int cntPerFetch2 = 512;
  private void syncProductRecord(RpcClient rpcErp,RpcClient rpcSaleor,boolean syncProduct,boolean syncVariant,boolean syncMedia,boolean testOnly,List<Integer> p_irgList) {
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
		v = rpcErp.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.view",
					new VectorUtil()
						.addElement("wc.stocklist")
						.addElement("ProductRecord")
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("API open productrecord view failed");
			return;
		}
		if(p_irgList != null) {
			StringUtil strU = new StringUtil();
			for(int irg : p_irgList) {
				strU.cat(""+irg, ",");
			}
			v = rpcErp.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.query",
				new VectorUtil()
					.addElement("ProductRecord")
					.addElement("consgp_qty > 0 and st_webprice > 0 and sttp_name in ('Sake','Spirit','Wine') and pds_irg in (" + strU.toString() + ")")
					.toVector()
			);
		} else {
			v = rpcErp.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.query",
				new VectorUtil()
					.addElement("ProductRecord")
					.addElement("consgp_qty > 0 and st_webprice > 0 and sttp_name in ('Sake','Spirit','Wine')")
					.toVector()
			);
		}
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("API query producrecord view failed");
			return;
		}
		int recCount = Integer.parseInt(v.toString().substring(4).trim());
		int idx = 0;
//		JSONObject joErp = null;
//		JSONObject joSaleor = null;
		
		if(recCount > MAX_RECORD) recCount = MAX_RECORD;
		while(recCount > 0) {
			int n = recCount > cntPerFetch ? cntPerFetch : recCount;
			v = rpcErp.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.load",
					new VectorUtil()
						.addElement("ProductRecord")
						.addElement(idx)
						.addElement(n)
						.toVector()
				);
			if(v == null || !v.toString().startsWith("OK")) {
				LogUtil.log("API load productrecord load failed");
				return;
			}
			idx += n;
			recCount -=n;
			JSONArray ja = new JSONArray(v.toString().substring(4));
			for(int i=0;i<ja.length();i++) {
				JSONObject joErp = ja.getJSONObject(i);
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
					insertMHash.put(joErp.getString("st_slug"), joErp);
				}
			}
		}
		rpcSaleor.open();
		rpcSaleor.setTimeout(180000);
		if(syncProduct & p_irgList == null) {
		for(idx=0;idx<MAX_RECORD;idx+=cntPerFetch2) {
			v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.getProductRecords",
						new VectorUtil().addElement(idx).addElement(cntPerFetch2).toVector()
						);
			if(v == null || !v.toString().startsWith("OK")) {
				LogUtil.log("API getProductRecord failed");
				return;
			}
			String ss = v.toString();
			JSONArray ja = new JSONArray(ss.substring(4));
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
	 				}
	 				/*
	 				if(needSync) {
	 					updateHash.put(slug, joSaleor);
	 				}
	 				insertHash.remove(slug);
	 				*/
	 			}
			}
			if(ja.length() < cntPerFetch2) break;
		}
		UniLog.log("SaleorSync ProductRecord" + insertHash.size() + " insert " + updateHash.size() + " update " + deleteHash.size() + " delete");
		}
		if(syncVariant && p_irgList == null) {
		for(idx=0;idx<MAX_RECORD;idx+=cntPerFetch2) {
			v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.getProductVariants",
						new VectorUtil().addElement(idx).addElement(cntPerFetch2).toVector()
						);
			if(v == null || !v.toString().startsWith("OK")) {
				LogUtil.log("API getProductVariant failed");
				return;
			}
			String ss = v.toString();
			JSONArray ja = new JSONArray(ss.substring(4));
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
	 				}
	 				/*
	 				if(needSync) {
	 					updateVHash.put(sku, joSaleor);
	 				}
	 				insertVHash.remove(sku);
	 				*/
	 			}
			}
			if(ja.length() < cntPerFetch2) break;
		}
		UniLog.log("SaleorSync ProductVariant" + insertVHash.size() + " insert " + updateVHash.size() + " update " + deleteVHash.size() + " delete");
		}
		if(syncMedia && p_irgList == null) {
		for(idx=0;idx<MAX_RECORD;idx+=cntPerFetch2) {
			v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.getProductMedia",
						new VectorUtil().addElement(idx).addElement(cntPerFetch2).toVector()
						);
			if(v == null || !v.toString().startsWith("OK")) {
				LogUtil.log("API getProductMedia failed");
				return;
			}
			String ss = v.toString();
			JSONArray ja = new JSONArray(ss.substring(4));
			for(int i=0;i<ja.length();i++) {
				JSONObject joSaleor = ja.getJSONObject(i);
				String slug = joSaleor.getString("product");
				JSONArray media = joSaleor.getJSONArray("media");
				if(media.length() > 0) {
					JSONObject joErp = insertMHash.get(slug);
					if(joErp == null) {
						deleteMHash.put(slug, joSaleor);
	 				} else {
	 					boolean needSync = checkAndSyncProductMedia(joErp, joSaleor);
	 					if(needSync) {
	 						updateMHash.put(slug, joSaleor);
	 					}
	 					insertMHash.remove(slug);
	 				}
				} else {
					CoreLog.log("igore saleor media record with no media");
				}
			}
			if(ja.length() < cntPerFetch2) break;
		}
		UniLog.log("SaleorSync ProductMedia " + insertVHash.size() + " insert " + updateVHash.size() + " update " + deleteVHash.size() + " delete");
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
					LogUtil.log("API delete producvariant failed");
					return;
				}
			}
		}

		if(syncMedia) {
			if(deleteMHash.size() > 0) {
				Vector<String> delList = new Vector<String>();
				for(String sku : deleteMHash.keySet()) {
					delList.add(sku);
				}
				v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.deleteProductMedia",delList);
				if(v == null || !v.toString().startsWith("OK")) {
					LogUtil.log("API delete producmedia failed");
					return;
				}
			}
		}
		
		
		if(syncProduct) {
		if(insertHash.size() > 0) {
			for(String slug : insertHash.keySet()) {
				JSONObject ejo = insertHash.get(slug);
				JSONObject sjo = new JSONObject();
				sjo.put("name",ejo.get("st_iname"));
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
				addProductAttribute(ja1,"region", ejo.getString("storg_name"));
				addProductAttribute(ja1,"appellation", ejo.getString("stbd_appellation"));
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
				addProductAttribute(ja1,"country",ejo.getString("storg_ecountry"));
				addProductAttribute(ja1,"brand",ejo.getString("stbd_name"));
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
//				sjo.put("name",slug);
				sjo.put("productSlug",ejo.get("st_slug"));
				JSONArray ja1 = new JSONArray();
				addProductAttribute(ja1,"owner",ejo.getString("or_cocode"));
				addProductAttribute(ja1,"org",ejo.getInt("pds_org"));
				sjo.put("attributes", ja1);
				ja1 = new JSONArray();
				double price = ejo.getDouble("st_webprice");
//				price = WineCaveUtil.getWebConsigpprice(price);
				addVariantChannel(ja1,"hk",price,ejo.getDouble("consgp_cost"));
				sjo.put("channels", ja1);
				updateVHash.put(slug, sjo);
			}
		}
		}
		if(syncMedia) {
		if(insertMHash.size() > 0) {
			for(String slug : insertMHash.keySet()) {
				JSONObject ejo = insertMHash.get(slug);
				if(ejo.getInt("st_photoid") > 0 && !StringUtils.isBlank(ejo.getString("st_photofmt"))) {
				JSONObject sjo = new JSONObject();
				sjo.put("product",slug);
				JSONArray ja1 = new JSONArray();
				JSONObject jo1 = new JSONObject();
				jo1.put("url", "https://hub.erpv4.com/saleorsync/getResource?url="+ejo.getString("st_photourl"));
				jo1.put("alt", "No Label");
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
				ja.put(jo);
				if(ja.length() >= cntPerFetch) {
					v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.insertProductRecords",
						new VectorUtil().addElement(ja.toString()).toVector()
					);
					if(v == null || !v.toString().startsWith("OK")) {
						LogUtil.log("API insert productrecord failed");
						return;
					}
					ja = null;
				}
			}
			if(ja != null) {
					v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.insertProductRecords",
						new VectorUtil().addElement(ja.toString()).toVector()
					);
					if(v == null || !v.toString().startsWith("OK")) {
						LogUtil.log("API insert productrecord failed");
						return;
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
						LogUtil.log("API insert productvariants failed");
						return;
					}
					ja = null;
				}
			}
			if(ja != null) {
					v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.insertProductVariants",
						new VectorUtil().addElement(ja.toString()).toVector()
					);
					if(v == null || !v.toString().startsWith("OK")) {
						LogUtil.log("API insert productvariants failed");
						return;
					}
					ja = null;
			}
		}
		}
		if(syncMedia) {
		if(updateMHash.size() > 0) {
			for(JSONObject jo : updateMHash.values()) {
				String slug = jo.getString("product");
				JSONArray ja = jo.getJSONArray("media");
				if(ja.length() > 0) {
					JSONObject jo1 = ja.getJSONObject(0);
					String url = jo1.getString("url");
					v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.addMediaToProduct",
						new VectorUtil()
						.addElement(slug)
						.addElement(url)
						.addElement("jpg")
						.toVector()
					);
					CoreLog.log("upload media " + slug + " " + url,"jpg");
				}
			}
//			JSONArray ja = null;
//			for(JSONObject jo : updateMHash.values()) {
//				if(ja == null) {
//					ja = new JSONArray();
//				}
//				ja.put(jo);
//				if(ja.length() >= cntPerFetch) {
//					v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.insertProductMedia",
//						new VectorUtil().addElement(ja.toString()).toVector()
//					);
//					if(v == null || !v.toString().startsWith("OK")) {
//						LogUtil.log("API insert productmedia failed");
//						return;
//					}
//					ja = null;
//				}
//			}
//			if(ja != null) {
//					v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.insertProductMedia",
//						new VectorUtil().addElement(ja.toString()).toVector()
//					);
//					if(v == null || !v.toString().startsWith("OK")) {
//						LogUtil.log("API insert productmedia failed");
//						return;
//					}
//					ja = null;
//			}
		}
		}
  }
  private void syncProductType(RpcClient rpcErp,RpcClient rpcSaleor) {
	  	Value v;
		Hashtable<String,JSONObject> insertHash = new Hashtable<String,JSONObject>();
		Hashtable<String,JSONObject> updateHash = new Hashtable<String,JSONObject>();
		Hashtable<String,JSONObject> deleteHash = new Hashtable<String,JSONObject>();
		v = rpcErp.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.view",
					new VectorUtil()
						.addElement("erpv4.McType")
						.addElement("ProductType")
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("API open producttype view failed");
			return;
		}
		v = rpcErp.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.query",
				new VectorUtil()
					.addElement("ProductType")
					.addElement("sttp_name in ('Wine','Spirit','Sake')")
					.toVector()
			);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("API query productype view failed");
			return;
		}
		int recCount = Integer.parseInt(v.toString().substring(4).trim());
		v = rpcErp.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.load",
					new VectorUtil()
						.addElement("ProductType")
						.addElement(0)
						.addElement(recCount)
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("API load producttype load failed");
			return;
		}
		JSONArray ja = new JSONArray(v.toString().substring(4));
		JSONObject joErp = null;
		JSONObject joSaleor = null;
		for(int i=0;i<ja.length();i++) {
			joErp = ja.getJSONObject(i);
			insertHash.put(joErp.getString("mt_slug"), joErp);
		}
		rpcSaleor.open();
		v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.getProductType");
		String ss = v.toString();
		if(ss.startsWith("OK")) {
			ja = new JSONArray(ss.substring(4));
			for(int i=0;i<ja.length();i++) {
				joSaleor = ja.getJSONObject(i);
				String slug = joSaleor.getString("slug");
				joErp = insertHash.get(slug);
				if(joErp == null) {
					deleteHash.put(slug, joSaleor);
	 			} else {
	 				boolean needSync = checkAndSyncProductType(joErp, joSaleor);
	 				if(!needSync) {
	 					insertHash.remove(slug);
	 				}
	 				/*
	 				if(needSync) {
	 					updateHash.put(slug, joSaleor);
	 				}
	 				insertHash.remove(slug);
	 				*/
	 			}
			}
		}
		UniLog.log("SaleorSync ProductType" + insertHash.size() + " insert " + updateHash.size() + " update " + deleteHash.size() + " delete");
		if(deleteHash.size() > 0) {
			Vector<String> delList = new Vector<String>();
			for(String slug : deleteHash.keySet()) {
				delList.add(slug);
			}
			v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.deleteProductType",delList);
			if(v == null || !v.toString().startsWith("OK")) {
				LogUtil.log("API delete productype failed");
				return;
			}
		}
		if(insertHash.size() > 0) {
			for(String slug : insertHash.keySet()) {
				JSONObject ejo = insertHash.get(slug);
				JSONObject sjo = new JSONObject();
				sjo.put("slug",slug);
				sjo.put("name",ejo.get("mt_tpname"));
				sjo.put("hasVariants",true);
				JSONArray ja1 = new JSONArray();
				ja1.put("icode");
				ja1.put("country");
				ja1.put("maturity");
				ja1.put("weight");
				ja1.put("long-description");
				ja1.put("packing");
				ja1.put("volume");
				ja1.put("score");
				ja1.put("vintage");
				ja1.put("tasting");
				ja1.put("region");
				ja1.put("brand");
				ja1.put("class");
				ja1.put("appellation");
				sjo.put("productAttributes", ja1);
				ja1 = new JSONArray();
				JSONObject jo1 = new JSONObject();
				jo1.put("slug", "owner");
				jo1.put("variantSelection", false);
				ja1.put(jo1);
				jo1 = new JSONObject();
				jo1.put("slug", "org");
				jo1.put("variantSelection", false);
				ja1.put(jo1);
				sjo.put("variantAttributes", ja1);
				updateHash.put(slug, sjo);
			}
		}
		if(updateHash.size() > 0) {
			ja = new JSONArray();
			for(JSONObject jo : updateHash.values()) {
				ja.put(jo);
			}
			v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.insertProductTypes",
						new VectorUtil().addElement(ja.toString()).toVector()
					);
			if(v == null || !v.toString().startsWith("OK")) {
				LogUtil.log("API insert producttype failed");
				return;
			}
		}
  }
  private void syncCategory(RpcClient rpcErp,RpcClient rpcSaleor) {
	  	Value v;
		Hashtable<String,JSONObject> insertHash = new Hashtable<String,JSONObject>();
		Hashtable<String,JSONObject> updateHash = new Hashtable<String,JSONObject>();
		Hashtable<String,JSONObject> deleteHash = new Hashtable<String,JSONObject>();
		v = rpcErp.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.view",
					new VectorUtil()
						.addElement("erpv4.StockType")
						.addElement("ProductCategory")
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("API open category view failed");
			return;
		}
		v = rpcErp.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.query",
					new VectorUtil()
						.addElement("ProductCategory")
						.addElement("")
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("API query category view failed");
			return;
		}
		int recCount = Integer.parseInt(v.toString().substring(4).trim());
		v = rpcErp.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.fetch",
					new VectorUtil()
						.addElement("ProductCategory")
						.addElement(0)
						.addElement(recCount)
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("API query category fetch failed");
			return;
		}
		JSONArray ja = new JSONArray(v.toString().substring(4));
		JSONObject joErp = null;
		JSONObject joSaleor = null;
		for(int i=0;i<ja.length();i++) {
			joErp = ja.getJSONObject(i);
			insertHash.put(BiCellCollection.makeSlug(joErp.getString("sttp_name")), joErp);
		}
		
		rpcSaleor.open();
		v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.getProductCategory");
		String ss = v.toString();
		if(ss.startsWith("OK")) {
			ja = new JSONArray(ss.substring(4));
			for(int i=0;i<ja.length();i++) {
				joSaleor = ja.getJSONObject(i);
				String slug = joSaleor.getString("slug");
				joErp = insertHash.get(slug);
				if(joErp == null) {
					deleteHash.put(slug, joSaleor);
	 			} else {
	 				boolean needSync = checkAndSyncCategory(joErp, joSaleor);
	 				if(needSync) {
	 					updateHash.put(slug, joSaleor);
	 				}
	 				insertHash.remove(slug);
	 			}
			}
		}
		UniLog.log("SaleorSync Category " + insertHash.size() + " insert " + updateHash.size() + " update " + deleteHash.size() + " delete");
		if(deleteHash.size() > 0) {
			Vector<String> delList = new Vector<String>();
			for(String slug : deleteHash.keySet()) {
				delList.add(slug);
			}
			v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.deleteProductCategory",delList);
			if(v == null || !v.toString().startsWith("OK")) {
				LogUtil.log("API delete category failed");
				return;
			}
		}
		if(insertHash.size() > 0) {
			for(String slug : insertHash.keySet()) {
				JSONObject ejo = insertHash.get(slug);
				JSONObject sjo = new JSONObject();
				sjo.put("slug",slug);
				sjo.put("name",ejo.get("sttp_name"));
				sjo.put("parentSlug", JSONObject.NULL);
				updateHash.put(slug, sjo);
			}
		}
		if(updateHash.size() > 0) {
			ja = new JSONArray();
			for(JSONObject jo : updateHash.values()) {
				ja.put(jo);
			}
			v = rpcSaleor.callSegment("com.kikyosoft.rpcservlet.SaleorSyncRpc.insertProductCategory",
						new VectorUtil().addElement(ja.toString()).toVector()
					);
			if(v == null || !v.toString().startsWith("OK")) {
				LogUtil.log("API insert category failed");
				return;
			}
		}
	  
  }
  // --- Your REAL sync logic goes here. Report progress via progress.accept(%) when non-null.
  private void runSyncWork_org(
	  ZkSessionHelper sp,
      String remarks,
      boolean productCategory,
      boolean productType,
      boolean productDetail,
      boolean productVariant,
      boolean testOnly,
      String  action,
      IntConsumer progress
  ) throws Exception {
    // TODO: call your wrappers/services here (Category/Type/Detail/Variant)
    // Example simulated steps

	RpcClient rpcErp = new RpcClient("192.168.33.3",5102,10000);
	RpcClient rpcOld = new RpcClient("192.168.33.3",5101,10000);

//	RpcClient rpcOld = new RpcClient("192.168.1.204",5101,10000);
//	RpcClient rpcOld = new RpcClient("192.168.46.13",5101,10000);
//	RpcClient rpcErp = new RpcClient("192.168.1.204",5102,10000);
//	RpcClient rpcSaleor = new RpcClient("localhost",6022,10000);
	RpcClient rpcSaleor = new RpcClient("192.168.33.3",6022,10000);
	try  {
		rpcErp.open();
		Value v = rpcErp.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.login",
					new VectorUtil()
//						.addElement("winecavescp")
						.addElement("")
						.addElement("hlv")
						.addElement("k2khlv")
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("API Login failed");
		}
//		syncCategory(rpcErp,rpcSaleor);
//		syncProductType(rpcErp,rpcSaleor);
		rpcErp.close();
		rpcOld.open();
		v = rpcOld.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.login",
					new VectorUtil()
//						.addElement("winecaveold")
						.addElement("")
						.addElement("hlv")
						.addElement("k2khlv")
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("APIOLD Login failed");
			return;
		}
		v = rpcOld.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.view",
				new VectorUtil()
					.addElement("wc.stocklist")
					.addElement("ProductRecord")
					.toVector()
			);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("API open productrecord view failed");
			return;
		}
		rpcOld.setTimeout(3600000);
		JSONObject jo = new JSONObject();
		v = rpcOld.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.call",
					new VectorUtil()
						.addElement("ProductRecord")
						.addElement("syncProductRecords")
						.addElement(jo.toString())
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("call to syncProductRecord failed");
			return;
		}
		LogUtil.log("Sync Completed " + v.toString());
		
		
//		syncProductRecord(rpcOld,rpcSaleor,true,true,false,false,null);
		
//		{
//			ArrayList<Integer> irgList = new ArrayList<Integer>();
//			irgList.add(39715);
//			syncProductRecord(rpcOld,rpcSaleor,true,true,false,false,irgList);
//		}
		int[] steps = {10, 35, 60, 85, 95};
		for (int pct : steps) {
			Thread.sleep(5000);
			if (progress != null) progress.accept(pct);
		}
	} finally {
		rpcOld.close();
		rpcErp.close();
		rpcSaleor.close();
	}
    // Example: choose sub-tasks based on flags
    // if (productCategory) categoryService.syncAll(...);
    // ...
  }

  // --- Your REAL sync logic goes here. Report progress via progress.accept(%) when non-null.
  private void runSyncWork(
	  ZkSessionHelper sp,
      String remarks,
      boolean productCategory,
      boolean productType,
      boolean productDetail,
      boolean productVariant,
      boolean testOnly,
      String  action,
      IntConsumer progress
  ) throws Exception {
    // TODO: call your wrappers/services here (Category/Type/Detail/Variant)
    // Example simulated steps

	RpcClient rpcErp = new RpcClient("192.168.33.3",5102,10000);
	RpcClient rpcOld = new RpcClient("192.168.33.3",5101,10000);

//	RpcClient rpcOld = new RpcClient("192.168.1.204",5101,10000);
//	RpcClient rpcOld = new RpcClient("192.168.46.13",5101,10000);
//	RpcClient rpcErp = new RpcClient("192.168.1.204",5102,10000);
//	RpcClient rpcSaleor = new RpcClient("localhost",6022,10000);
	RpcClient rpcSaleor = new RpcClient("192.168.33.3",6022,10000);
	try  {
		rpcErp.open();
		Value v = rpcErp.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.login",
					new VectorUtil()
//						.addElement("winecavescp")
						.addElement("")
						.addElement("hlv")
						.addElement("k2khlv")
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("API Login failed");
		}
//		syncCategory(rpcErp,rpcSaleor);
//		syncProductType(rpcErp,rpcSaleor);
		rpcErp.close();
		rpcOld.open();
		v = rpcOld.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.login",
					new VectorUtil()
//						.addElement("winecaveold")
						.addElement("")
						.addElement("hlv")
						.addElement("k2khlv")
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("APIOLD Login failed");
			return;
		}
		v = rpcOld.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.view",
				new VectorUtil()
					.addElement("wc.stocklist")
					.addElement("ProductRecord")
					.toVector()
			);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("API open productrecord view failed");
			return;
		}
		rpcOld.setTimeout(3600000);
		JSONObject jo = new JSONObject();
		v = rpcOld.callSegment("com.uniinformation.bicore.BiCoreRpcServlet.call",
					new VectorUtil()
						.addElement("ProductRecord")
						.addElement("syncProductRecords")
						.addElement(jo.toString())
						.toVector()
				);
		if(v == null || !v.toString().startsWith("OK")) {
			LogUtil.log("call to syncProductRecord failed");
			return;
		}
		LogUtil.log("Sync Completed " + v.toString());
		
		
//		syncProductRecord(rpcOld,rpcSaleor,true,true,false,false,null);
		
//		{
//			ArrayList<Integer> irgList = new ArrayList<Integer>();
//			irgList.add(39715);
//			syncProductRecord(rpcOld,rpcSaleor,true,true,false,false,irgList);
//		}
		int[] steps = {10, 35, 60, 85, 95};
		for (int pct : steps) {
			Thread.sleep(5000);
			if (progress != null) progress.accept(pct);
		}
	} finally {
		rpcOld.close();
		rpcErp.close();
		rpcSaleor.close();
	}
    // Example: choose sub-tasks based on flags
    // if (productCategory) categoryService.syncAll(...);
    // ...
  }


  // --- Helpers

  static class JobStatus {
    volatile int percent;
    volatile boolean done;
    volatile boolean ok;
    volatile String message;
  }

  private static boolean checked(String v) {
    return v != null && ("1".equals(v) || "true".equalsIgnoreCase(v) || "on".equalsIgnoreCase(v));
  }
  private static String trim(String s){ return s==null? "" : s.trim(); }

  private static Map<String,String> readJsonBody(HttpServletRequest req) throws IOException {
    try (BufferedReader br = req.getReader()) {
      StringBuilder sb = new StringBuilder();
      String line; while ((line = br.readLine()) != null) sb.append(line);
      String raw = sb.toString().trim();
      // super-lightweight parser for simple flat JSON key/values
      Map<String,String> out = new LinkedHashMap<>();
      if (raw.startsWith("{") && raw.endsWith("}")) {
        raw = raw.substring(1, raw.length()-1);
        // split on commas not inside quotes (simple case)
        String[] pairs = raw.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        for (String pair : pairs) {
          String[] kv = pair.split(":(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 2);
          if (kv.length == 2) {
            String k = unquote(kv[0].trim());
            String v = unquote(kv[1].trim());
            out.put(k, v);
          }
        }
      }
      return out;
    }
  }
  private static String unquote(String s) {
    if (s == null) return null;
    s = s.trim();
    if (s.startsWith("\"") && s.endsWith("\"")) {
      s = s.substring(1, s.length()-1).replace("\\\"", "\"").replace("\\\\", "\\");
    }
    return s;
  }

  private static void writeJsonError(HttpServletResponse resp, int code, String msg) throws IOException {
    resp.setStatus(code);
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    resp.getWriter().write("{\"ok\":false,\"message\":"+quote(msg)+"}");
  }
  private static String quote(String s){
    if (s==null) return "\"\"";
    return "\""+s.replace("\\","\\\\").replace("\"","\\\"").replace("\n","\\n")+"\"";
  }
  private static String escape(String s){ return s==null? "" : s.replace("\"","\\\""); }
}
