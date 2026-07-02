package com.uniinformation.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.uniinformation.utils.UniLog;

public class BcTagUtil {
	static public final int TAGTYPE_unknown = 0;
	static public final int TAGTYPE_erpv4_v1= 1;
	static public final String TAGPREFIX_erpv4_v1 = "https://www.erpv4.com/bctag?agent=aw&";
	
	public static JSONObject parseQueryToJsonObject(String url) throws JSONException {
	    JSONObject result = new JSONObject();

	    int q = url.indexOf('?');
	    if (q < 0) return result;

	    String query = url.substring(q + 1);

	    int hash = query.indexOf('#');
	    if (hash >= 0) {
	        query = query.substring(0, hash);
	    }

	    query = query.replaceAll("[\\r\\n]+$", "");

	    for (String pair : query.split("&")) {
	        if (pair.isEmpty()) continue;

	        int eq = pair.indexOf('=');

	        String key = eq >= 0 ? pair.substring(0, eq) : pair;
	        String value = eq >= 0 ? pair.substring(eq + 1) : "";

//	        key = URLDecoder.decode(key, StandardCharsets.UTF_8);
//	        value = URLDecoder.decode(value, StandardCharsets.UTF_8)
//	                          .replaceAll("[\\r\\n]+$", "");

	        if (!result.has(key)) {
	            result.put(key, value);
	        } else {
	            Object oldValue = result.get(key);

	            JSONArray values;
	            if (oldValue instanceof JSONArray) {
	                values = (JSONArray) oldValue;
	            } else {
	                values = new JSONArray();
	                values.put(oldValue);
	            }

	            values.put(value);
	            result.put(key, values);
	        }
	    }

	    return result;
	}
	
	
	
	static public JSONObject getParamsFromBcTag(String p_bctag) {
		try {
			if(p_bctag.startsWith(TAGPREFIX_erpv4_v1)) {
				return(parseQueryToJsonObject(p_bctag));
			}
		} catch (JSONException jex) {
			UniLog.log(jex);
		}
		return(null);
	}
	
}
