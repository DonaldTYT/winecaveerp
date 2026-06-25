package com.uniinformation.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.kyoko.common.ChineseConvert;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiChain;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiField;
import com.uniinformation.bicore.BiJoin;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.utils.RegExpList.PatternItem;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

public class TranslateUtil {
	public static boolean fDebug = false;
	public static enum Type { LABEL, BUTTON, MENU, PATTERN };
	private static HashMap<String,String> translateHM = new HashMap<String,String>();  //key is uppercase
	//key <agent>:<lang>, value:RegExpList(pattern list)
	private static HashMap<String, RegExpList<String>> pListHM = new HashMap<String, RegExpList<String>>() ; 
	
	
	
	
	public static void main(String args[]){
		SessionHelper sh = ZkSessionHelper.getSessionHelperDummy("erpv4","hlv",null);
		//listField(sh, "clinic.HealthQnr");
		sh.setLHLang("SCHN", 2, false);
		
		
		UniLog.log1("result:%s",getText(sh, null, "PATTERN", "haha1 123 haha1"));
		
		updateText(sh, "(haha1) ([0-9]*) (haha1)", "PATTERN", "TCHN", "xxx1 $2 with tchn content \u7E41\u9AD4\u4E2D\u6587");
		updateText(sh, "(haha2) ([0-9]*) (haha2)", "PATTERN", "SCHN", "xxx2 $2 with schn content \u7B80\u4F53\u4E2D\u6587");
		
		UniLog.log1("result:%s",getText(sh, null, "PATTERN", "haha1 123 haha1"));
		UniLog.log1("result:%s",getText(sh, null, "PATTERN", "haha2 123 haha2"));
		UniLog.log1("result:%s",getText(sh, null, "PATTERN", "haha3 123 haha3"));
		System.exit(0);
		
		
	}
	/***
	 * TODO
	 * list all field with label 
	 * will include label/button/popup/childform/etc.
	 * @param p_sh
	 * @param p_viewName
	 */
	
/* Remarked By DT 2022/07/10, view.getLinkViewws no longer visiable by public class, since the actual visible linked Views now depends on BiResult */	
	
//	public static void listField(SessionHelper p_sh, String p_viewName){
//		BiSchema schema;
//		BiView view;
//		try {
//			schema = BiSchema.loadSchema(p_sh);
//			view = schema.getViewByName(p_viewName);
//			//UniLog.log1("view:%s table:%s", view.getName(), view.getTable().getName());
//			listField(p_sh,view);
//			for (BiView linkView : view.getLinkViews()){
//				//UniLog.log1("lview:%s table:%s", linkView.getName(), linkView.getTable().getName());
//				listField(p_sh, linkView);
//			}
//			
//		} 
//		catch (Exception ex) {
//			UniLog.log(ex);
//		}
//		UniLog.logm(null,"bye");
//	}
	public static void listField(SessionHelper p_sh, BiView view){
		try{
			if (view == null) return;
			Vector<BiColumn> cols = view.getColumns();
			for(int i=0;i<cols.size();i++) {
				BiColumn col = cols.get(i);
				BiField field = col.getField();
				if (field == null){
					//UniLog.log1("%s(%s) field is null, ignore", col.getCellLabel(), col.getEngName());
					continue;
				}
				if (!col.isInList(p_sh) && col.isInvisible(p_sh)){
					//UniLog.log1("%s(%s) field is invisible, ignore", col.getCellLabel(), col.getEngName());
					continue;
				}
				UniLog.log1("view:%s cell:%s label:%s", view.getName(), col.getCellLabel(), col.getEngName());
				/*
				BiTable table = field.getTable();
				BiChain chain = view.findChain(table);
				if (chain.getParent() != null){
					CellCollection subChainCC =  chain.getParent().getCollection("subChains");
					BiTable subChainTable = subChainCC == null ? null : (BiTable)subChainCC.getCollection("table");
					
					if (subChainTable != null){
						if (!subChainTable.equals(view.getTable())){
							UniLog.log1("view:%s subChainTable:%s  table:%s label:%s(%s) field:%s", view.getName(),
									subChainTable == null ? "null" : subChainTable.getName(), table.getName(), col.getLabel(), col.getEngName(), field.getName());
						}
					}
					
				}
				
				BiTable tableDep = col.getTableDepend();
				boolean tableDepFlag = false;
				if (tableDep != null && !tableDep.getName().equals(view.getTable().getName())){
					tableDepFlag = true;
				}
				UniLog.log1("view:%s checkdep: col:%d: label:%s parent:%s tableDep:%s joinFlag:%s size:%d", view.getName(), i, col.getLabel(), ((BiView)col.getParent()).getName(), tableDep == null ? "null" : tableDep.getName(), tableDepFlag, col.getTableDepends().size());
				if (tableDepFlag){
					BiJoin join = tableDep.getJoin(view.getTable());
					if (join != null){
						for (int j=0; j<join.getJoinCount(); j++){
							UniLog.log1("view:%s join from:%s to:%s", view.getName(), join.getFromField(j).getName(), join.getToField(j).getName());
						}
					}
				}
				*/
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
	}
		
	/***
	 * translate by pattern. called by getText()
	 * e.g. aaa 2 bbb -> AAA 2 BBB
	 * @param p_sh
	 * @param p_msg
	 * @return 
	 */
	private static String getPatternText(SessionHelper p_sh, String p_lang, String p_msg, String p_def) {
		if (p_sh == null || !p_sh.getAllowPatternTranslate()) {
			if (fDebug) UniLog.log1("not allow pattern translate");
			//return p_def;
			return p_msg;
		}
		RegExpList<String> pList = pListHM.get(buildPListHMKey(p_sh.getAgent(), p_lang));
		if (pList == null) {
			if (fDebug) UniLog.log1("load pattern fail. key:%s", buildPListHMKey(p_sh.getAgent(), p_lang));
			return p_def;
		}
		String translateStr = pList.findAndReplace(p_msg,p_def);
		if (fDebug) UniLog.log1("return %s", translateStr);
		return translateStr;
	}
	/***
	 * update the pList cache. called by updateText()
	 * @param p_sh
	 * @param p_patternStr
	 * @param p_replaceStr
	 */
	private static void updatePatternText(SessionHelper p_sh, String p_lang, String p_patternStr, String p_replaceStr) {
		if (p_sh == null || !p_sh.getAllowPatternTranslate() || StringUtils.isBlank(p_patternStr) || p_replaceStr == null || StringUtils.isBlank(p_lang)) {
			return;
		}
		synchronized(pListHM) {
			RegExpList<String> pList = pListHM.get(buildPListHMKey(p_sh.getAgent(), p_lang));
			if (pList == null) {
				UniLog.log1("pList not found. create now");
				pList = new RegExpList<String>();
				pListHM.put(buildPListHMKey(p_sh.getAgent(),p_lang), pList);
			}
			
			//pList.add(p_patternStr, p_replaceStr);
			pList.add(p_patternStr, StringEscapeUtils.unescapeJava(p_replaceStr)); //andrew200928: handle newline \n 
			UniLog.log1("add new pattern:%s replace:%s", p_patternStr, p_replaceStr);
			return;
		}
	}
	
	/***
	 * translate by key
	 * @param p_sh
	 * @param p_key - for type LABEL/BUTTON/MENU
	 * @param p_type
	 * @param p_defaultValue
	 * @return
	 */
	public static String getText(SessionHelper p_sh, String p_key, String p_type, String p_defaultValue){
		if (p_sh == null || !p_sh.getAllowTranslate()){
			return(p_defaultValue);
		}
		loadTranslate(p_sh);
		if (p_type.matches("LABEL|BUTTON|MENU|OPTION")) {
			synchronized(translateHM){
				String text = translateHM.get(buildKey(p_sh.getAgent(), p_key, p_type, p_sh.getLHLang()));
				if (StringUtils.isNotBlank(text)){
					return(text);
				}
				
				//auto B2G, G2B (experimental)
				if (StringUtils.equals(p_sh.getLHLang(),"SCHN") && p_sh.getAllowTranslateB2G()) {
					text = translateHM.get(buildKey(p_sh.getAgent(), p_key, p_type, "TCHN"));
					if (StringUtils.isNotBlank(text)){
						String cText = ChineseConvert.convertAuto2Gnew(text);
						if (fDebug) UniLog.log1("cache convert %s %s %s", p_sh.getLHLang(), text, cText);
						updateText(p_sh, p_key, p_type, p_sh.getLHLang(), cText);
						return(cText);
						
					}
				}
				if (StringUtils.equals(p_sh.getLHLang(),"TCHN") && p_sh.getAllowTranslateG2B()) {
					text = translateHM.get(buildKey(p_sh.getAgent(), p_key, p_type, "SCHN"));
					if (StringUtils.isNotBlank(text)){
						String cText = ChineseConvert.convertAuto2Bnew(text);
						if (fDebug) UniLog.log1("cache convert %s %s %s", p_sh.getLHLang(), text, cText);
						updateText(p_sh, p_key, p_type, p_sh.getLHLang(), cText);
						return(cText);
					}
				}
				return(p_defaultValue);
			}
		}
		else if (StringUtils.equals(p_type,"PATTERN")){  //pattern ignore key
			String text = getPatternText(p_sh, p_sh.getLHLang(), p_defaultValue, null);
			if (text != null) {
				return text;
			}
			if (p_sh.getLHLang().equals("SCHN") && p_sh.getAllowTranslateB2G()) {
				text = getPatternText(p_sh, "TCHN", p_defaultValue, null);
				if (StringUtils.isNotBlank(text)){
					return ChineseConvert.convertAuto2Gnew(text);
				}
			}
			if (p_sh.getLHLang().equals("TCHN") && p_sh.getAllowTranslateG2B()) {
				text = getPatternText(p_sh, "SCHN", p_defaultValue, null);
				if (StringUtils.isNotBlank(text)){
					return ChineseConvert.convertAuto2Bnew(text);
				}
			}
			return p_defaultValue;
		}
		else {
			UniLog.log1("type %s not supported", p_type);
			return p_defaultValue;
		}
			

	}
	/***
	 * update the cache translation update
	 * TODO: delete old cache
	 * @param p_sh
	 * @param p_key
	 * @param p_type
	 * @param p_text
	 */
	public static void updateText(SessionHelper p_sh,String p_key, String p_type, String p_lang, String p_text){
		if (!p_sh.getAllowUpdateTranslate()){
			if (fDebug) UniLog.log1("not allow update translate");
			return;
		}
		loadTranslate(p_sh);
		
		if (fDebug) UniLog.log1("update text. key:%s type:%s lang:%s text:%s", p_sh, p_type, p_lang, p_text);
		if (p_type.matches("LABEL|BUTTON|MENU|OPTION")) {
			synchronized(translateHM){
				translateHM.put(buildKey(p_sh.getAgent(), p_key, p_type,p_lang), p_text);

				//if it's menu label, clear side menu cache
				if (StringUtils.equals("MENU", p_type)){
					p_sh.clearSideMenuCache();
				}
			}
		}
		else if (p_type.matches("PATTERN")) {
			updatePatternText(p_sh, p_lang, p_key, p_text);
		}
		else {
			UniLog.log1("type [%s] not supported", p_type);
			return;
		}
	}
	public static String buildKey(String p_agent, String p_key, String p_type, String p_lang){
		return (p_agent +":" + p_key +":" + p_type +":" +p_lang).toUpperCase().trim();
	}
	public static void loadTranslate(SessionHelper p_sh){
		if (!p_sh.getAllowTranslate()){
			return;
		}
		if (translateHM.get(p_sh.getAgent()) != null) { //already loaded
			if (fDebug) UniLog.log1("%s already loaded", p_sh.getAgent());
			return;
		}
		UniLog.log1("init start: agent:%s" ,p_sh.getAgent());
		BiView view = p_sh.getBiView("BiTranslate");
		if (view == null){
			UniLog.log1("view is null");
			return;
		}
		//BiResult biResult = view.newBiResult(p_sh.getVcode(),null,null,p_sh);  //andrew190520: not good, it use current user identity to obtain record
		BiResult biResult = view.newBiResult(null,null,null,null);
		if (biResult == null){
			UniLog.log1("biResult is null");
			return;
		}
		biResult.clearCondition();
		biResult.query(true);
		
		//construct translateHM
		synchronized(translateHM){
			translateHM.put(p_sh.getAgent(), p_sh.getAgent()); //mark the agent to hashmap
			
			//translateHM.clear();  //andrew200525: now support multi agent, so not required to clear it anymore
			for (int i=0; i< biResult.getRowCount();i++){
				biResult.loadOneRecV(i);
				if (!biResult.getCellString("bitl_type","").matches("LABEL|BUTTON|MENU|OPTION")) {
					continue;
				}
				String key = buildKey(p_sh.getAgent(), biResult.getCellString("bitl_key"), biResult.getCellString("bitl_type"), biResult.getCellString("bitl_lang"));
				translateHM.put(key, biResult.getCellString("bitl_labelstr"));
			}
			UniLog.log("init end");
		}
		
		//construct pattern translate
		loadPatternTranslate(p_sh, biResult);
		
	}
	
	private static String buildPListHMKey(String p_agent, String p_lang) {
		return (p_agent + ":" + p_lang).toUpperCase().trim();
	}
	
	/***
	 * construct pListHM, should called by loadTranslate
	 * @param p_sh
	 * @param p_br
	 */
	private static void loadPatternTranslate(SessionHelper p_sh, BiResult p_br) {
		if (!p_sh.getAllowPatternTranslate()) {
			if (fDebug) UniLog.log1("skip load pattern");
			return;
		}
		try {
			synchronized(pListHM) {
				UniLog.log1("load pList start");

				//build tmp pList for each language
				HashMap<String,ArrayList<PatternItem<String>>> tmpPatternsMap = new HashMap<String,ArrayList<PatternItem<String>>>();  //key: lang, value: pattern list

				//loop all translate record
				int loadCnt = 0;
				for (int i=0; i< p_br.getRowCount();i++){
					if (!p_br.loadOneRecV(i)) {
						UniLog.log1("load record error. idx:%d", i);
						break;
					}
					String type = p_br.getCellString("bitl_type","");
					String lang = p_br.getCellString("bitl_lang");
					String patternStr = p_br.getCellString("bitl_key");
					//String translateStr = p_br.getCellString("bitl_labelstr");
					String translateStr = StringEscapeUtils.unescapeJava(p_br.getCellString("bitl_labelstr")); //andrew200928: handle newline \n 
					if (StringUtils.isBlank(lang)) {
						if (fDebug) UniLog.log1("skip invalid language");
						continue;
					}

					//handle pattern type only
					if (!StringUtils.equals(type,"PATTERN")){
						continue;
					}
					if (tmpPatternsMap.get(lang) == null) {
						tmpPatternsMap.put(lang, new ArrayList<PatternItem<String>>());
					}
					tmpPatternsMap.get(lang).add(RegExpList.buildItem(patternStr, translateStr));
				}
				if (fDebug) UniLog.log1("loadCnt %d", loadCnt);
				
				for (String lang :tmpPatternsMap.keySet()) {
					RegExpList<String> pList = new RegExpList<String>();
					pList.add(tmpPatternsMap.get(lang));
					pListHM.put(buildPListHMKey(p_sh.getAgent(),lang), pList);
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	/***
	 * clear cache
	 */
	public static void clearCache() {
		synchronized(translateHM){
			UniLog.log1("clear translateHM");
			translateHM.clear();
		}
		synchronized(pListHM) {
			UniLog.log1("clear pListHM");
			pListHM.clear();
		}
	}
	
	public static String getTextByCell(SessionHelper p_sh, ColumnCell p_cell, String p_defaultValue) {
		try {
			//validation
			if (p_sh == null || p_cell == null || p_cell.getBiColumn() == null) {
				return p_defaultValue;
			}
			if (!p_sh.getAllowTranslate()) {
				return p_defaultValue;
			}

			//radio button translation
			if (StringUtils.equals(p_cell.getBiColumn().getColumnType(), "radio")) {
				if (!p_sh.getAllowOptionTranslate()) {
					return p_defaultValue;
				}
				//for radio button: key by view + celllabel + value
				String key = p_cell.getBiResult().getView().getName() + "." + p_cell.getCellLabel() + "." + p_defaultValue;  
				return TranslateUtil.getText(p_sh, key, "OPTION", p_defaultValue);
			}
			UniLog.log1("column type[%s] not supported",  p_cell.getBiColumn().getColumnType());
		}
		catch(Exception ex) {
			UniLog.log1("error:%s", ex.getMessage());
		}
		return p_defaultValue;
	}
	
}
