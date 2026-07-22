package com.uniinformation.dynamic.winecave;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.internal.StringUtil;
import org.zkoss.util.media.AMedia;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.prtdoc.PrtdocInterface;
import com.uniinformation.utils.FileUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public class GoogleMerchantCenterProduct extends BiActionHandler {
	HashSet<String> productHash ;
	JSONObject jo;
	JSONArray ja;
	/*
	int sendEmailRg = 0;
	String plainTextPath="c:/tmp/email_plaintext.txt";
	String htmlPath="c:/tmp/email_html.txt";
	String attachmentPath="c:/tmp/email_attachment.txt";
	*/
	/*
	String plainText = null;
	String htmlText = null;
	ArrayList<EmailAttachment> attachment = null;
	HashMap<String,String> sendEmail;
	*/
	public GoogleMerchantCenterProduct() {
		super(null);
	}
	public GoogleMerchantCenterProduct(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}
	
	static ReturnMsg doBeforeAction(JSONObject jo, JSONArray ja,HashSet<String> productHash,BiResult p_result,int cnt) throws Exception {
			JSONObject jf = new JSONObject();
			jf.put("title",  "WineCave Product Feed");
			jf.put("link",  "https://winecavehk.com");
			jf.put("description", "WineCave Google Merchant product feed");
			jf.put("products", ja);
			jo.put("feed", jf);
			return (ReturnMsg.defaultOk);
	}
	
	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		try {
			productHash = new HashSet<String>();
			jo = new JSONObject();
			ja = new JSONArray();
			return(doBeforeAction(jo,ja,productHash,p_result,cnt));
		} catch(Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
	}
	
	static ReturnMsg doProcessAction(JSONArray ja,HashSet<String> productHash,BiResult p_result,int p_recIdx) throws Exception {
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
	
	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		try {
			return(doProcessAction(ja,productHash,p_result,p_recIdx));
		} catch(Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
	}
	@Override
	public ReturnMsg afterAction(BiResult p_br) {
		if(jo != null) {
			Messagebox.show("Confirm Export Xml ?", "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
				new EventListener() {
				   public void onEvent(Event evt) throws Exception {
				    	if (((Integer)evt.getData()) == Messagebox.YES){
				    	    AMedia media = new AMedia(
				    	            "thisdownload.xml",
				    	            "xml",
				    	            "application/xml",
				    	            convert(jo).getBytes(StandardCharsets.UTF_8)
				    	        );

				    	    Filedownload.save(media);
				    	    Messagebox.show("File Exported");
				   	    }
				   }
				}
			);
		}
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

	@Override
	public boolean isVisible(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(false);
		return(p_br.getSessionHelper().hasAccessRight("#massmail"));
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		return(false);
	}
	
    private static void optionalGTag(StringBuilder xml, String name, JSONObject obj, int indent) {
        if (obj.has(name)) {
            String value = obj.optString(name, "").trim();
            if (!value.isEmpty()) {
                gtag(xml, name, value, indent);
            }
        }
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
    
    private static String escapeXml(String value) {
        if (value == null) return "";

        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
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
    
    private static void require(JSONObject obj, String key, int index) {
        if (!obj.has(key) || obj.optString(key).trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Product index " + index + " missing required field: " + key
            );
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
}
