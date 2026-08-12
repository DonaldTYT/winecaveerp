package com.uniinformation.zkf;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;

import com.uniinformation.utils.UniLog;


public class JsonToXmlExample {
    public static String jsonToXml(String json) {
    	try  {
    		JSONObject jsonObject = new JSONObject(json);
    		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + XML.toString(jsonObject);
    	} catch (Exception ex) {
    		UniLog.log(ex);
    		return(null);
    	}
    }

    public static void main(String[] args) throws Exception {
        String json = "{"
            + "\"salesOrder\":{"
            + "\"orderNo\":\"SO-1001\","
            + "\"customer\":\"ABC Restaurant\","
            + "\"lines\":["
            + "{\"itemCode\":\"WINE-001\",\"qty\":1,\"price\":800},"
            + "{\"itemCode\":\"WINE-002\",\"qty\":2,\"price\":650}"
            + "]"
            + "}"
            + "}";

        System.out.println(jsonToXml(json));
    }
}
