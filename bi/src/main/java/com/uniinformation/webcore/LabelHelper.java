package com.uniinformation.webcore;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import com.uniinformation.utils.UniLog;

public class LabelHelper {
	public final static String LANG_ENG = "ENG";
	public final static String LANG_TCHN = "TCHN";
	public final static String LANG_SCHN = "SCHN";
	public final static String LANG_JAP = "JAP";
	public final static String LANG_KOR = "KOR";
	public final static int TYPE_LB = 0;
	public final static int TYPE_BTN = 1;
	public final static int TYPE_MSG = 2;
	public final static int TYPE_TP = 3;
	public static boolean DEBUG = false;
	
	static HashMap<String, String> labelHm = new HashMap<String, String>();
	static {
		init();
	}
	private static void init(){
		/*
		synchronized(labelHm){
			labelHm.put(buildKey("hello",TYPE_LB,LANG_ENG), "Hello");
			labelHm.put(buildKey("hello",TYPE_LB,LANG_TCHN), "\u4F60\u597D");
			labelHm.put(buildKey("hello",TYPE_LB,LANG_SCHN), "\u4F60\u597D");
			labelHm.put(buildKey("hello",TYPE_LB,LANG_JAP), "\u3053\u3093\u306B\u3061\u306F");
			labelHm.put(buildKey("hello",TYPE_LB,LANG_KOR), "\uC548\uB155\uD558\uC138\uC694");
		}
		*/
		
		
		loadLabel("label_helper_btn_eng_utf8.txt", "UTF-8", TYPE_BTN, LANG_ENG);
		loadLabel("label_helper_btn_schn_utf8.txt", "UTF-8", TYPE_BTN, LANG_SCHN);
		loadLabel("label_helper_btn_tchn_utf8.txt", "UTF-8", TYPE_BTN, LANG_TCHN);
		loadLabel("label_helper_btn_jap_utf8.txt", "UTF-8", TYPE_BTN, LANG_JAP);
		loadLabel("label_helper_btn_kor_utf8.txt", "UTF-8", TYPE_BTN, LANG_KOR);
		
		loadLabel("label_helper_lb_eng_utf8.txt", "UTF-8", TYPE_LB, LANG_ENG);
		loadLabel("label_helper_lb_schn_utf8.txt", "UTF-8", TYPE_LB, LANG_SCHN);
		loadLabel("label_helper_lb_tchn_utf8.txt", "UTF-8", TYPE_LB, LANG_TCHN);
		loadLabel("label_helper_lb_jap_utf8.txt", "UTF-8", TYPE_LB, LANG_JAP);
		loadLabel("label_helper_lb_kor_utf8.txt", "UTF-8", TYPE_LB, LANG_KOR);
		
		loadLabel("label_helper_tp_eng_utf8.txt", "UTF-8", TYPE_TP, LANG_ENG);
		loadLabel("label_helper_tp_schn_utf8.txt", "UTF-8", TYPE_TP, LANG_SCHN);
		loadLabel("label_helper_tp_tchn_utf8.txt", "UTF-8", TYPE_TP, LANG_TCHN);
		loadLabel("label_helper_tp_jap_utf8.txt", "UTF-8", TYPE_TP, LANG_JAP);
		loadLabel("label_helper_tp_kor_utf8.txt", "UTF-8", TYPE_TP, LANG_KOR);
		/*
		loadLabel("label_helper_tchn.txt", "BIG5", TYPE_LB, LANG_TCHN);
		loadLabel("label_helper_schn.txt", "GB2312", TYPE_LB, LANG_SCHN);
		loadLabel("label_helper_jap.txt", "Shift-JIS", TYPE_LB, LANG_JAP);
		loadLabel("label_helper_kor.txt", "EUC-KR", TYPE_LB, LANG_KOR);
		*/
	}
	/**
	 *  File format (UTF-8)
	 *  <TAG1>,<LABEL 1> 
	 *  <TAG2>,<LABEL 2> 
	 *  <TAG3>,<LABEL 3> 
	 *  e.g. /tmp/label_eng.txt
	 *  or
	 *  hello1
	 *  Hello1
	 *  hello %d
	 *  Hello %d
	 *  
	 *  
	 */
	private static void loadLabel(String p_fileName, String p_charSet, int p_type, String p_lang){
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try{
			is = Thread.currentThread().getContextClassLoader().getResourceAsStream(p_fileName);
			if (is == null){
				if (DEBUG) UniLog.log("fail to open: " + p_fileName);
				return;
			}
			if (p_charSet != null){
				isr = new InputStreamReader(is, p_charSet);
			}
			else{
				isr = new InputStreamReader(is);
			}
			br = new BufferedReader(isr);
			if ("UTF8".equals(p_charSet) || "UTF-8".equals(p_charSet)){ //skip BOM if any
				br.mark(1);
				if (br.read() != 0xFEFF){
					br.reset(); 
				}
			}
			loadLabel(br, p_type, p_lang);
   		}
   		catch(Exception ex){
   			ex.printStackTrace();
   		}
		finally{
			if (br != null){
				try { br.close(); } catch(Exception ex){}
			}
			if (isr != null){
				try { isr.close(); } catch(Exception ex){}
			}
			if (is != null){
				try { is.close(); } catch(Exception ex){}
			}
		}
	}
	private static void loadLabel(BufferedReader p_br, int p_type, String p_lang) throws Exception{
		String line;
		String tag = null;
		String value = null;
		while((line = p_br.readLine()) != null){
			if (tag == null){
				tag = line;
				continue;
			}
			if (value == null){
				value = line;
				String key = buildKey(tag, p_type, p_lang);
				//UniLog.log(String.format("loadLabel:%s %s", key, value));
				labelHm.put(key, value);
				tag = null;
				value = null;
				continue;
			}
		}
	}
	private static String buildKey(String p_tag, int p_type, String p_lang){
		//return("TAG:" +p_tag.trim().toLowerCase() + " LANG:" + p_lang);
		return("TAG:" +p_tag.trim().toLowerCase() +" TYPE:"+ p_type + " LANG:" + p_lang);  //andrew230918 key should contain type
	}
	
	/***
	 * remark should support String.format()
	 * @param p_tag
	 * @param p_type
	 * @param p_lang
	 * @return
	 */
	public static String getText(String p_tag, int p_type, String p_lang){ 
		if (p_tag == null) {
			return("");
		}
		String tag = p_tag.trim().toLowerCase();
		String label = labelHm.get(buildKey(tag, p_type, p_lang));
		if (label == null){
			label = labelHm.get(buildKey(tag, TYPE_LB, p_lang));
		}
		if (label == null){
			label = labelHm.get(buildKey(tag, TYPE_BTN, p_lang));
		}
		if (label == null){
			label = labelHm.get(buildKey(tag, TYPE_MSG, p_lang));
		}
		if (label == null){
			label = labelHm.get(buildKey(tag, TYPE_TP, p_lang));
		}
		if (label == null){
			if (DEBUG) UniLog.log("getText undefined:" +buildKey(tag, p_type, p_lang));
			return(p_tag);
		}
		else{
			return(label);
		}
	}
	public static void main (String args[]){
		UniLog.log(LabelHelper.getText("Add",TYPE_LB,"ENG"));
		UniLog.log(LabelHelper.getText("Add",TYPE_LB,"TCHN"));
		UniLog.log(LabelHelper.getText("Add",TYPE_LB,"SCHN"));
		UniLog.log(LabelHelper.getText("Add",TYPE_LB,"JAP"));
		UniLog.log(LabelHelper.getText("Add",TYPE_LB,"KOR"));
		
		UniLog.log(LabelHelper.getText("Detail",TYPE_LB,"ENG"));
		UniLog.log(LabelHelper.getText("Detail",TYPE_LB,"TCHN"));
		UniLog.log(LabelHelper.getText("Detail",TYPE_LB,"SCHN"));
		UniLog.log(LabelHelper.getText("Detail",TYPE_LB,"JAP"));
		UniLog.log(LabelHelper.getText("Detail",TYPE_LB,"KOR"));
		
		UniLog.log(LabelHelper.getText("Download",TYPE_BTN,"ENG"));
		UniLog.log(LabelHelper.getText("Download",TYPE_BTN,"TCHN"));
		UniLog.log(LabelHelper.getText("Download",TYPE_BTN,"SCHN"));
	}
}
