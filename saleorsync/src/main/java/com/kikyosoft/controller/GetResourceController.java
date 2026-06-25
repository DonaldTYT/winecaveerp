package com.kikyosoft.controller;

import com.kikyosoft.utils.StringUtil;
import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.ProductOptionFinder;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.winecave.webcore.WinecaveSessionHelper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@RestController
public class GetResourceController {
	SessionHelper cmsSp = null;
    // Minimal extension → MIME map (extend as needed)
    private static final Map<String, String> EXT_TO_MIME = Map.ofEntries(
        Map.entry("jpg",  "image/jpeg"),
        Map.entry("jpeg", "image/jpeg"),
        Map.entry("png",  "image/png"),
        Map.entry("webp", "image/webp"),
        Map.entry("gif",  "image/gif"),
        Map.entry("pdf",  "application/pdf"),
        Map.entry("xls",  "application/vnd.ms-excel"),
        Map.entry("xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"),
        Map.entry("ppt",  "application/vnd.ms-powerpoint"),
        Map.entry("pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation"),
        Map.entry("doc",  "application/msword"),
        Map.entry("docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document"),
        Map.entry("zip",  "application/zip")
    );

    @GetMapping("/getResource")
    public ResponseEntity<?> getResource(
            String url,
            String ext,
            HttpServletRequest request,
            HttpServletResponse response) {

        if (!StringUtils.hasText(url)) {
            return ResponseEntity.badRequest().body("Missing resource path (url).");
        }

        try {
            // Preserve your session-based ERP access
            SessionHelper sp = ZkSessionHelper.getSessionHelper(request, response);

            // Decode URL if it came encoded
            String decodedUrl = URLDecoder.decode(url, StandardCharsets.UTF_8);

            InputStream in = sp.newErpFileInputStream(decodedUrl);
            if (in == null) {
                return ResponseEntity.status(404).body("Resource not found.");
            }

            // Determine content type
            String contentType = resolveContentType(ext, decodedUrl);

            // Cache for 1 hour (public)
            CacheControl cache = CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic();

            // Inline display by default (browsers will preview images/pdf)
            HttpHeaders headers = new HttpHeaders();
            headers.setCacheControl(cache);
            headers.set(HttpHeaders.CONTENT_TYPE, contentType);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "inline"); // or build a filename if you prefer

            // Stream the bytes without buffering whole file in memory
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(in));

        } catch (Exception ex) {
            return ResponseEntity.badRequest().body("Failed to load resource: " + ex.getMessage());
        }
    }

    private static String resolveContentType(String ext, String urlMaybeWithExt) {
        String guess = null;

        if (StringUtils.hasText(ext)) {
            guess = EXT_TO_MIME.get(ext.toLowerCase(Locale.ROOT));
        }

        if (guess == null) {
            // Try to infer from URL path extension
            Optional<String> extFromUrl = extensionFromUrl(urlMaybeWithExt);
            if (extFromUrl.isPresent()) {
                guess = EXT_TO_MIME.get(extFromUrl.get().toLowerCase(Locale.ROOT));
            }
        }

        return (guess != null) ? guess : MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }

    private static Optional<String> extensionFromUrl(String url) {
        if (url == null) return Optional.empty();
        int q = url.indexOf('?');
        String path = (q >= 0) ? url.substring(0, q) : url;
        int dot = path.lastIndexOf('.');
        if (dot >= 0 && dot < path.length() - 1) {
            return Optional.of(path.substring(dot + 1));
        }
        return Optional.empty();
    }
    @GetMapping("/getProductOptions")
    public ResponseEntity<?> getProductOptions(
    		String target,
            HttpServletRequest request,
            HttpServletResponse response) {
    		try {
        	SessionHelper sp = ZkSessionHelper.getSessionHelper(request, response);
            ProductOptionFinder pof = new ProductOptionFinder(sp);
            for(String option : pof.getOptions()) {
            	String ss =  request.getParameter(option);
            	if(ss != null) {
            		pof.addCondition(option, ss);
            	}
            }
            JSONObject jo = pof.queryOptions(sp, target);
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jo.toString()); // <-- send JSON text
    		} catch (Exception ex) {
//    			return ResponseEntity.badRequest().body("Failed to query options "+ex.getMessage());
    			  String json = new org.json.JSONObject()
    				        .put("ok", false)
    				        .put("message", "Failed to query options: " + ex.getMessage())
    				        .toString();

    				    return ResponseEntity
    				        .status(400)
    				        .contentType(MediaType.APPLICATION_JSON)
    				        .body(json);
    		}
    }

    @GetMapping("/getStockDetail")
    public ResponseEntity<?> getStockDetail(
    		@RequestParam("sku") List<String> skus,   
            HttpServletRequest request,
            HttpServletResponse response) {
    		try {
    			SessionHelper sp = ZkSessionHelper.getSessionHelper(request, response);
    			JSONObject jo = new JSONObject();
    			jo.put("ok", true);
    			for(String sku : skus) {
    				synchronized(sp) {
    					jo.put(sku,WinecaveSessionHelper.getStockDetail(sp,sku));
    				}
    			}
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jo.toString()); // <-- send JSON text
    		} catch (Exception ex) {
    			  String json = new org.json.JSONObject()
    				        .put("ok", false)
    				        .put("message", "Failed to get stock info : " + ex.getMessage())
    				        .toString();

    				    return ResponseEntity
    				        .status(400)
    				        .contentType(MediaType.APPLICATION_JSON)
    				        .body(json);
    		}
    }
    
    JSONObject loadAndCache(String name,String locale,JSONObject jo) {
    	if(name.equals("news")) {
    	if(cmsSp == null) cmsSp = ZkSessionHelper.getSessionHelperDummy("winecaveerp", "hlv",null);
    	JSONObject jg = jo.optJSONObject("grid");
    	if(jg != null) {
    		JSONArray ja = new JSONArray();
    		BiResult br = cmsSp.getBiSchema().getViewByName("wcerp.NewsCards").newBiResult(cmsSp.getLoginId(), null, null, cmsSp);
    		String locs = new StringUtil().cat("'all'", ",").cat("'"+locale+"'", ",").toString();
    		br.addCustomCondition("cmsnc_post = 'Y' and cmsnc_locale in ("+locs+")");
    		br.query();
    		for(int i=0;i < br.getRowCount();i++) {
    			br.loadOneRecV(i);
    			JSONObject jd = new JSONObject();
    			jd.put("title", br.getCellString("cmsnc_title"));
    			jd.put("date", DateUtil.toDateString(br.getCellDate("cmsnc_date"),"yyyy/mmm/dd"));
    			jd.put("category", br.getCellString("cmsnc_category"));
    			jd.put("excerpt", br.getCellString("cmsnc_excerpt"));
    			jd.put("img", br.getCellString("cmsnc_imgurl"));
    			jd.put("cta", br.getCellString("cmsnc_cta"));
    			ja.put(jd);
    		}
    		jg.put("cards",ja);
    	}
    	}
    	if(name.equals("home")) {
    	if(cmsSp == null) cmsSp = ZkSessionHelper.getSessionHelperDummy("winecaveerp", "hlv",null);
    	JSONObject jg = jo.optJSONObject("experiences");
    	if(jg != null) {
    		JSONArray ja = new JSONArray();
    		BiResult br = cmsSp.getBiSchema().getViewByName("wcerp.Experience").newBiResult(cmsSp.getLoginId(), null, null, cmsSp);
    		String locs = new StringUtil().cat("'all'", ",").cat("'"+locale+"'", ",").toString();
    		br.addCustomCondition("cmsep_post = 'Y' and cmsep_locale in ("+locs+")");
    		br.query();
    		for(int i=0;i < br.getRowCount();i++) {
    			br.loadOneRecV(i);
    			JSONObject jd = new JSONObject();
    			jd.put("title", br.getCellString("cmsep_title"));
    			jd.put("date", DateUtil.toDateString(br.getCellDate("cmsep_date"),"yyyy/mmm/dd"));
    			jd.put("body", br.getCellString("cmsep_body"));
    			jd.put("alt", "HAHA");
    			String ss = br.getCellString("cmsep_imgurl");
    			if(ss != null && !ss.trim().equals("")) {
    				jd.put("img", ss);
    			}
    			ja.put(jd);
    		}
    		jg.put("cards",ja);
    		br.getSelectUtil().close();
    	}
    	jg =  jo.optJSONObject("testimonials");
    	if(jg != null) {
    		if(cmsSp == null) cmsSp = ZkSessionHelper.getSessionHelperDummy("winecaveerp", "hlv",null);
    		JSONArray ja = new JSONArray();
    		BiResult br = cmsSp.getBiSchema().getViewByName("wcerp.Testimonials").newBiResult(cmsSp.getLoginId(), null, null, cmsSp);
    		br.addCustomCondition("cmstm_post = 'Y'");
    		br.query();
    		for(int i=0;i < br.getRowCount();i++) {
    			br.loadOneRecV(i);
    			JSONObject jd = new JSONObject();
    			jd.put("name", br.getCellString("cmstm_name"));
    			jd.put("role", br.getCellString("cmstm_role"));
    			jd.put("avatar", br.getCellString("cmstm_avator"));
    			jd.put("rating", br.getCellInt("cmstm_rating"));
    			jd.put("quote", br.getCellString("cmstm_quote"));
    			
    			ja.put(jd);
    		}
    		jg.put("list",ja);
    		br.getSelectUtil().close();
    	}
    	}
    	if(name.equals("faq")) {
    	JSONArray ja =  jo.optJSONArray("faqs");
    	if(ja != null) {
    		if(cmsSp == null) cmsSp = ZkSessionHelper.getSessionHelperDummy("winecaveerp", "hlv",null);
    		ja = new JSONArray();
    		jo.put("faqs", ja);
    		BiResult br = cmsSp.getBiSchema().getViewByName("wcerp.Faq").newBiResult(cmsSp.getLoginId(), null, null, cmsSp);
    		String locs = new StringUtil().cat("'all'", ",").cat("'"+locale+"'", ",").toString();
    		br.addCustomCondition("cmsfq_post = 'Y' and cmsfq_locale in ("+locs+")");
    		br.query();
    		for(int i=0;i < br.getRowCount();i++) {
    			br.loadOneRecV(i);
    			JSONObject jd = new JSONObject();
    			jd.put("question", br.getCellString("cmsfq_question"));
    			jd.put("answer", br.getCellString("cmsfq_answer"));
    			ja.put(jd);
    		}
    		br.getSelectUtil().close();
    	}
    	}
    	return(jo);
    }
    
    @GetMapping(value = "/getPageContent", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPageContent(
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            SessionHelper sp = ZkSessionHelper.getSessionHelper(request, response);

            // 1) Read query params ?name=<fileName>&locale=<localeCode>
            String name = request.getParameter("name");
            String locale = request.getParameter("locale");

            if (name == null || name.isBlank() || locale == null || locale.isBlank()) {
                String errJson = new JSONObject()
                        .put("ok", false)
                        .put("message", "Missing required parameters: name and/or locale")
                        .toString();
                return ResponseEntity
                        .badRequest()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errJson);
            }

//            // 2) Build resource path: resources/pagecontent/<name>/<locale>.json
//            String path = String.format("pagecontent/%s/%s.json",  locale, name);
//
//            ClassPathResource resource = new ClassPathResource(path);
//            if (!resource.exists()) {
//                String errJson = new JSONObject()
//                        .put("ok", false)
//                        .put("message", "Content file not found: " + path)
//                        .toString();
//                return ResponseEntity
//                        .status(404)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .body(errJson);
//            }
//
//            // 3) Read the file as UTF-8 text
//            String jsonText = StreamUtils.copyToString(
//                    resource.getInputStream(), StandardCharsets.UTF_8);
            
            

            // 2) Build filesystem path: /pagecontent/<locale>/<name>.json
            //    This is an absolute path from the filesystem root.
            String pathString = String.format("/pagecontent/%s/%s.json", locale, name);
//            String pathString = String.format("/pc2/%s/%s.json", locale, name);
            Path filePath = Paths.get(pathString);

            if (!Files.exists(filePath)) {
                String errJson = new JSONObject()
                        .put("ok", false)
                        .put("message", "Content file not found: " + pathString)
                        .toString();
                return ResponseEntity
                        .status(404)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(errJson);
            }

            // 3) Read the file as UTF-8 text
            String jsonText = Files.readString(filePath, StandardCharsets.UTF_8);

            // 4) Parse it to JSONObject (optional, but matches your code)
            JSONObject jo = new JSONObject(jsonText);
            jo = loadAndCache(name,locale,jo);

            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jo.toString());  // front-end can do res.json()
        } catch (Exception ex) {
            String json = new JSONObject()
                    .put("ok", false)
                    .put("message", "Failed to get content: " + ex.getMessage())
                    .toString();

            return ResponseEntity
                    .status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(json);
        }
    }

    @GetMapping("/getStockAvailability")
    public ResponseEntity<?> getStockAvailability(
    		@RequestParam("slug") List<String> slugs,   
            HttpServletRequest request,
            HttpServletResponse response) {
    		try {
    			SessionHelper sp = ZkSessionHelper.getSessionHelper(request, response);
    			JSONObject jo = WinecaveSessionHelper.getStockAvailability(sp,slugs);
            return ResponseEntity
                    .ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(jo.toString()); // <-- send JSON text
    		} catch (Exception ex) {
    			  String json = new org.json.JSONObject()
    				        .put("ok", false)
    				        .put("message", "Failed to get stock availability for : " + ex.getMessage())
    				        .toString();

    				    return ResponseEntity
    				        .status(400)
    				        .contentType(MediaType.APPLICATION_JSON)
    				        .body(json);
    		}
    }

//    @GetMapping("/getGoogleMerchant")
    @GetMapping(
    	    value = "/googleProductList.xml",
    	    produces = MediaType.APPLICATION_XML_VALUE
    	)
    public ResponseEntity<?> getGoogleMerchant(
            @RequestParam(required = false) String tok,
            HttpServletRequest request,
            HttpServletResponse response) {

        try {
            // Preserve your session-based ERP access
            SessionHelper sp = ZkSessionHelper.getSessionHelper(request, response);

            WinecaveSessionHelper wxp = (WinecaveSessionHelper) sp;
            String js = wxp.getGoogleMerchant();

            if (js == null || !js.startsWith("OK  ")) {
                return ResponseEntity.status(404).body("Resource not found.");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(js.substring(4));

        } catch (Exception ex) {
//            return ResponseEntity.badRequest().body("Failed to load resource: " + ex.getMessage());
            return ResponseEntity.status(500)
                    .contentType(MediaType.TEXT_PLAIN)
                    .body("Failed to load resource: " + ex.getMessage());
        }
    }
}
