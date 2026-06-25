package com.uniinformation.utils;

import java.util.HashMap;
import java.util.List;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import com.kyoko.common.ReturnMsg;

/***
 * 
 * param based hash to avoid user modify parameter
 * it's only a weak protection. 
 *
 */
public class URLParamHash {
	private final static String PH_SEPARATOR = "::";
	private final static String PH_NOISE = "just for fun 231025";
	private final static String PH_TAG = "phtv1";
	/***
	 * validate param hash
	 * @param p_url
	 * @param p_tags
	 * @return
	 */
	public static ReturnMsg validateParamHash(String p_url, String...p_tags) {
		try {
			if (StringUtils.isBlank(p_url)) {
				UniLog.log1("url is blank");
				return new ReturnMsg(false,"url is blank");
			}

			HashMap<String,String> paramsMap = buildParamsMap(p_url);

			//contruct hash from param
			String paramHash = buildParamHash(paramsMap, p_tags);
			if (StringUtils.isBlank(paramHash)) {
				UniLog.log1("param hash is blank");
				return new ReturnMsg(false,"param hash is blank");
			}

			//read hash from url
			String paramHashFromURL = paramsMap.get(PH_TAG);

			//compare
			if (StringUtils.equals(paramHash, paramHashFromURL)) {
				UniLog.log1("hash is good");
				return ReturnMsg.defaultOk;
			}
			else {
				UniLog.log1("paramHash:%s:%s", paramHash,paramHashFromURL);
				UniLog.log1("hash is bad");
				return ReturnMsg.defaultFail;
			}
		}
		catch(Exception ex) {
			return new ReturnMsg(false,ex);
		}
	}
	
	private static void appendDataSb(StringBuilder p_dataSb, String p_name, String p_value) {
		if (p_dataSb.length() == 0) {
			p_dataSb.append(PH_NOISE+PH_SEPARATOR);
		}
		else {
			p_dataSb.append("::");
		}
		p_dataSb.append(p_name+"="+p_value);
		
	}
	/***
	 * build params hashmap string url string
	 * @param p_url
	 * @return
	 * @throws Exception
	 */
	private static HashMap<String,String> buildParamsMap(String p_url) throws Exception{
		List<NameValuePair> pairs = new URIBuilder(p_url).getQueryParams();
		HashMap<String,String> paramsMap = new HashMap();
		for (NameValuePair pair : pairs) {
			//UniLog.log1("name:%s value:%s", pair.getName(), pair.getValue());
			paramsMap.put(pair.getName(), pair.getValue());
		}
		return paramsMap;
	}
	
	
	/***
	 * param hash 
	 * @param p_allParamsHM
	 * @param p_tags
	 * @return
	 * @throws Exception
	 */
	private static String buildParamHash(HashMap<String,String> p_allParamsHM, String...p_tags) throws Exception{
		
		//construct data string for hash
		StringBuilder dataSb = new StringBuilder();
		for (int i=0; i<p_tags.length; i++) {
			String paramName = p_tags[i];
			String paramValue = p_allParamsHM.get(p_tags[i]);
			if (paramValue != null) {
				appendDataSb(dataSb, paramName, paramValue);
			}
			else {
				UniLog.log1("tag not exist [%s]");
			}
		}
		
		if (dataSb.length() > 0) {
			return DigestUtils.md2Hex(dataSb.toString());
		}
		return "";
		
	}
	/***
	 * build a param based hash and append to url parameter
	 * @param p_url
	 * @param p_tags
	 * @return
	 * @throws Exception
	 */
	public static String appendParamHash(String p_url, String...p_tags) {
		try {
			UniLog.log1("called url:" +p_url);

			if (p_tags == null || p_tags.length == 0) {
				UniLog.log1("no tag, return org url");
				return p_url;
			}


			HashMap<String,String> paramsMap = buildParamsMap(p_url);
			String paramHash = buildParamHash(paramsMap, p_tags);
			if (StringUtils.isNotBlank(paramHash)) {
				//UniLog.log1("dataSb:" +dataSb.toString());
				String newUrl = new URIBuilder(p_url).setParameter(PH_TAG, paramHash).toString();
				UniLog.log1("return:updated:" + newUrl);
				return newUrl;
			}
			else {
				UniLog.log1("return:unchanged:" + p_url);
				return p_url;
			}
		}
		catch(Exception ex) {
			//ex.printStackTrace();
			UniLog.log1("error:" + ex.getMessage());
			return p_url;
		}
	}
	
	public static void main(String args[]) {
		appendParamHash("http://localhost:8080/pms/abc.html?var1=abc&var2=00001","var1","var2");
		validateParamHash("http://localhost:8080/pms/abc.html?var1=abc&var2=00001&phtv1=71d7223d3ea0f86a54836af7561c6d1c","var1","var2");
		validateParamHash("http://localhost:8080/pms/abc.html?var1=abc&var2=00001&phtv1=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx","var1","var2");
		/*
		appendParamHash("http://localhost:8080/pms/abc.html?var1=abc&var2=00001","var1","var2");
		appendParamHash("http://localhost:8080/pms/abc.html?var1=abc&var2=00002","var1","var2");
		validateParamHash("http://localhost:8080/pms/abc.html?var1=abc&var2=00001&phtv1=71d7223d3ea0f86a54836af7561c6d1c","var1","var2");
		validateParamHash("http://localhost:8080/pms/abc.html?var1=abc&var2=00002&&phtv1=c9ca630b16809b1380374f78225c602b","var1","var2");
		validateParamHash("http://localhost:8080/pms/abc.html?var1=abc&var2=00002&&phtv1=71d7223d3ea0f86a54836af7561c6d1c","var1","var2");
		*/
		
	}
	

}
