package com.kyoko.utils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.net.URLEncoder;
import java.util.StringJoiner;

import com.uniinformation.utils.UniLog;


public class UrlUtils {
    public static Map<String, String> getQueryParams(String url) {
        URL uri;
		try {
			uri = new URL(url);
        String query = uri.getQuery();
        Map<String, String> params = new HashMap<>();

        if (query != null) {
            for (String pair : query.split("&")) {
                String[] keyValue = pair.split("=", 2);
                String key = URLDecoder.decode(keyValue[0], "UTF-8");
                String value = keyValue.length > 1 ? URLDecoder.decode(keyValue[1], "UTF-8") : "";
                params.put(key, value);
            }
        }
        return params;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			UniLog.log(e);
			return(null);
		}
    }

    /*
    public static void main(String[] args) throws Exception {
        String url = "https://example.com/api?param1=%E6%B5%8B%E8%AF%95&param2=value+with+spaces+%26+symbols";

        Map<String, String> queryParams = getQueryParams(url);
        System.out.println("param1: " + queryParams.get("param1")); // Output: 测试
        System.out.println("param2: " + queryParams.get("param2")); // Output: value with spaces & symbols
    }
    */
    public static String buildURLWithParams (String baseURL, String... args) throws UnsupportedEncodingException {
    	Map<String,String> m = new HashMap<String,String>();
    	for(int i=0;i<args.length;i+=2){
    		m.put(args[i], args[i+1]);
    	}
    	return(buildURLWithParams(baseURL, m));
    }
    public static String buildURLWithParams(String baseURL, Map<String, String> params) throws UnsupportedEncodingException {
        StringJoiner joiner = new StringJoiner("&");

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String encodedKey = URLEncoder.encode(entry.getKey(), "UTF-8");
            String encodedValue = URLEncoder.encode(entry.getValue(), "UTF-8");
            joiner.add(encodedKey + "=" + encodedValue);
        }

        return baseURL + "?" + joiner.toString();
   }

}
