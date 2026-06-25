package com.uniinformation.zkbi;


import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Vbox;

import com.google.gson.JsonArray;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.kyoko.common.*;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

public class ZkBiSearchHelper {
	boolean defaultSearchAnd = true;
    Textbox tbSearchBox;
    Checkbox cbSearchBox;
	//Hbox searchDiv;
	HtmlBasedComponent searchDiv;
    //Hbox searchTagDiv;
    Div searchTagDiv;
    BiResult result;
    ZkBiSearchInterface searchCb;
    //ListModelList listModelList;
    AtomicLong searchTagCounter = new AtomicLong(0);
    final static int MODE_AND = 1;
    final static int MODE_OR = 2;
    
    //key searchTagId + col, matchedCnt)
    HashMap<String, SearchTagField> searchTagMap = new HashMap<String,SearchTagField>();
    
    HashMap<Object,TrStatFilter> trStatHM = new HashMap<Object, TrStatFilter>(); //key by TrStat
    public class SearchTagField{
    	AtomicLong matchedCnt = new AtomicLong(0);
    	boolean isChecked = true;
    }
	public class TrStatFilter{
		int trStatIdx;
		public TrStatFilter(int p_trStatIdx){
			trStatIdx = p_trStatIdx;
		}
		public int getTrStatIdx(){
			return(trStatIdx);
		}
	}
	
	private void updateSearchTagMatchedCnt(String p_searchTagId, int p_colIdx, long delta){
		synchronized(searchTagMap){
			String key = p_searchTagId + "_" + p_colIdx;
			SearchTagField searchTagField = searchTagMap.get(key);
			if (searchTagField == null){
				searchTagField = new SearchTagField();
				searchTagMap.put(key, searchTagField);
			}
			searchTagField.matchedCnt.addAndGet(delta);
				
		}
	}
	private void removeSearchTagMatchedCnt(String p_searchTagId){
		synchronized(searchTagMap){
			for (int i=0; i<100; i++){
				String key = p_searchTagId + "_" + i;
				Object removedObj = searchTagMap.remove(key);
				if (removedObj == null){
					break;
				}
			}
		}
	}
   
    /*
	public void setListModelList(ListModelList listModelList) {
		this.listModelList = listModelList;
	}
	*/
    public void setTbSearchBox(Textbox tbSearchBox) {
		this.tbSearchBox = tbSearchBox;
	}
    public void setCbSearchBox(Checkbox cbSearchBox) {
		this.cbSearchBox = cbSearchBox;
		cbSearchBox.setChecked(defaultSearchAnd);
    }
	public void setSearchDiv(HtmlBasedComponent searchDiv) {
		this.searchDiv = searchDiv;
	}
	public void setSearchTagDiv(Div searchTagDiv) {
		this.searchTagDiv = searchTagDiv;
	}
	
	synchronized public String getSearchTagId(){
		return(String.format("searchtagid_%d", searchTagCounter.incrementAndGet()));
	}
	
    public void setupSearch(final BiResult p_result, ZkBiSearchInterface p_searchCb)
    {
    	result = p_result;
    	searchCb = p_searchCb;
    	tbSearchBox.setVisible(true);
    	//tbSearchBox.setPlaceholder("Quick Search");
    	//tbSearchBox.setTooltiptext("To search type and hit enter");
        tbSearchBox.setId("tbSearchBox");
        tbSearchBox.setInstant(true);
        tbSearchBox.addEventListener("onChange",
	        	new EventListener() {
	        		public void onEvent(Event event) throws Exception {
	        			UniLog.logm(this, "tbSearchBox onChange: %s", tbSearchBox.getValue());
	        			doSearch(false, false);
	        		}
	        	}
        );
        tbSearchBox.addEventListener("onOpen",
	        	new EventListener() {
	        		public void onEvent(Event event) throws Exception {
	        			UniLog.logm(this, "tbSearchBox onOpen: %s", tbSearchBox.getValue());
	        			doSearch(false, true);
	        		}
	        	}
        );
        tbSearchBox.addEventListener("onOK",
	        	new EventListener() {
	        		public void onEvent(Event event) throws Exception {
	        			UniLog.logm(this, "tbSearchBox onOK: %s", tbSearchBox.getValue());
	        			doSearch(false, true);
	        		}
	        	}
        );
        if (cbSearchBox != null){
	        cbSearchBox.addEventListener("onCheck",
		        	new EventListener() {
		        		public void onEvent(Event event) throws Exception {
		        			UniLog.logm(this, "cbSearchBox onCheck: %s", cbSearchBox.getValue());
		        			updateLocationURLQuickFilterTags();
		        			doSearch(false, false);
		        		}
		        	}
	        );
        }
    }
    
    private Hbox buildToogleSwitch(final String p_searchTagId, final int p_colIdx, String p_label, boolean p_checked){
   		Hbox toggleHbox = new Hbox();
   		//toggleHbox.setPack("center");
   		toggleHbox.setAlign("center");
   		Label lb = new Label(p_label);
   		final Checkbox cb = new Checkbox();
		cb.setChecked(p_checked);
		cb.setMold("switch");
		/*
   		cb.setZclass("cmn-toggle");
   		cb.setSclass("cmn-toggle-round");
   		*/
    	cb.addEventListener("onCheck",
        	new EventListener() {
        		public void onEvent(Event event) throws Exception {
        			UniLog.logm(this, "onCheck:"+ event.getTarget().getId());
    				synchronized(searchTagMap){
    					String key = p_searchTagId + "_" + p_colIdx;
    					SearchTagField searchTagField = searchTagMap.get(key);
    					if (searchTagField != null){
    						searchTagField.isChecked = cb.isChecked();
    					}
        				doSearch(false, false);
    				}
        		}
        	}
		);
   		
   		toggleHbox.appendChild(cb);
   		toggleHbox.appendChild(lb);
   		return(toggleHbox);
    }
        
    public boolean addSearchTag(String p_searchTagId){
    	String inText = tbSearchBox.getValue().trim().toLowerCase();
    	if (inText.length() <= 0){
    		return false;
    	}
    	tbSearchBox.setValue("");
    	Toolbarbutton searchTagBt = new Toolbarbutton();
    	searchTagBt.setIconSclass("z-icon-times");
    	searchTagBt.setId(p_searchTagId);
    	searchTagBt.setStyle("border:solid;border-color:#e0e0e0;background:yellow;color:#6e6e00;margin:2px");
    	searchTagBt.setDir("reverse");
    	searchTagBt.setLabel(inText);
    	
    	searchTagDiv.appendChild(searchTagBt);
    	updateLocationURLQuickFilterTags();
    	searchTagBt.addEventListener("onClick",
        	new EventListener() {
        		public void onEvent(Event event) throws Exception {
        			UniLog.logm(this, "searchTagBt onClick:%s id:%s",((Toolbarbutton)event.getTarget()).getLabel(), event.getTarget().getId());
        			event.getTarget().detach();
        			updateLocationURLQuickFilterTags();
        			doSearch(false,false);
        		}
        	}
		);
    	Popup popup = new Popup();
    	Vbox vbox = new Vbox();
    	
    	popup.appendChild(vbox);
   		//vbox.appendChild(new Label("Options:"));
    	
    	SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper((HttpServletRequest) Executions.getCurrent().getNativeRequest() , (HttpServletResponse) Executions.getCurrent().getNativeResponse());
		synchronized(searchTagMap){
			for (int i=0; i<100; i++){
				String key = p_searchTagId + "_" + i;
				SearchTagField searchTagField = searchTagMap.get(key);
				if (searchTagField == null){
					break;
				}
				else{
					if (searchTagField.matchedCnt.get() > 0){
						Vector<BiColumn> listColumns = result.getListColumns();
						String fieldName = ((BiColumn)listColumns.get(i)).getEngName();
						fieldName = sessionHelper.getLabel(fieldName);
						vbox.appendChild(buildToogleSwitch(p_searchTagId, i, String.format("%s (%d)", fieldName, searchTagField.matchedCnt.get()), true));
					}
					
				}
			}
		}
		if (vbox.getChildren().size() <= 0){
			vbox.appendChild(new Label("No matches found"));
		}
    	searchTagBt.setTooltip(popup);
    	//searchTagDiv.appendChild(popup);
    	searchDiv.appendChild(popup);
    	return(true);
    }
    /***
     * perform string matching between BiColumn value and search tags
     * @param p_inText - BiColumn string value
     * @param p_colIdx - BiColumn idx
     * @return matched true
     */
    public boolean doSearchSingle(String p_inText, int p_colIdx){ //should obtain from search result???
    	if (!tbSearchBox.isVisible()){
    		UniLog.log("doSearch ignore invisible");
    		return false;
    	}
    	
    	//match with current search tag
    	String searchTag = tbSearchBox.getValue().toLowerCase().trim();
		if (!searchTag.equals("") && match(p_inText, searchTag)){
			return(true);
		}
		
		//match with stored search tag
		for (Component tmpTbb : searchTagDiv.getChildren()){
			if (tmpTbb instanceof Toolbarbutton){
				String tmpSearchTag = ((Toolbarbutton)tmpTbb).getLabel();
				
   				String key = tmpTbb.getId() + "_" + p_colIdx;
   				SearchTagField searchTagField = searchTagMap.get(key);
   				if (searchTagField != null){
   					if (!searchTagField.isChecked){
   						continue;
   					}
   				}
				
				if (match(p_inText, tmpSearchTag)){
					return(true);
				}
			}
		}
		return(false);
    }
    public void doSearch(boolean p_ignoreBlankFlag, boolean p_addSearchTag){
    	if (!tbSearchBox.isVisible()){
    		//UniLog.log("doSearch ignore invisible");
    		return;
    	}
    	if (p_ignoreBlankFlag && tbSearchBox.getValue().trim().equals("") && searchTagDiv.getChildren().size() == 0){ //no filter, keep list unchange
    		//UniLog.log("doSearch ignore blank");
    		return;
    	}
    	String searchTag = tbSearchBox.getValue().toLowerCase().trim();
		ArrayList <TrStatFilter> searchResultStatList = new ArrayList<TrStatFilter>();
		clearTrStatHM();
		
    	String searchTagId = getSearchTagId();
		
    	HashSet<TrStatFilter> tl = null;
    	HashSet<Integer> il = searchCb.getInludeList();
    	if(il != null) {
    		tl = new HashSet<TrStatFilter>();
    	}
    	for (int i=0; i<result.getResultStat().size(); i++){
			//result.fetchOneRecV(i);
			result.loadOneRecV(i);
			Vector<BiColumn> listColumns = result.getListColumns();
   			if (il != null && il.contains(i)) {
   				TrStatFilter trf = new TrStatFilter(i);
   				searchResultStatList.add(trf);
				updateTrStatHM(result.getResultStat().elementAt(i), trf, null);
				tl.add(trf);
   				continue;
   			}
   			if (searchTag.equals("") && searchTagDiv.getChildren().size() == 0){ //no filter
   				TrStatFilter trf = new TrStatFilter(i);
   				searchResultStatList.add(trf);
				updateTrStatHM(result.getResultStat().elementAt(i), trf, null);
   				continue;
    		}
   			
   			int matchMode = cbSearchBox.isChecked() ? MODE_AND : MODE_OR;
   			
   			
   			//first round, search current search tag
   			boolean curMatchedFlag = false;
   			if (searchTag.equals("")){
   				if (matchMode == MODE_AND){
   					curMatchedFlag = true;
   				}
   			}
   			else{
	    		for(int j = 0;j<listColumns.size();j++) {
	    			//if (match(result.getCell(((BiColumn) listColumns.get(j)).getLabel()).getString(),searchTag)){
	    			if (match(result.getCell(((BiColumn) listColumns.get(j)).getLabel()).getColumnDisplayString(),searchTag)){
	    				curMatchedFlag = true;
	    				updateSearchTagMatchedCnt(searchTagId, j, 1);
	    				//TODO can break here if not require matchedCnt
	    			}
	    			else{
	    				updateSearchTagMatchedCnt(searchTagId, j, 0);
	    			}
	    		}
//	    		if(result.getAggregateOrPivotList() != null) {
	    		if(result.aggregateOrPivotSize() > 0) {
	    			Object[] vals = result.getAggregateValues(i);
	    			for(int j = 0;j<vals.length;j++) {
	    				if(vals[j] != null) {
	    					if (match(vals[j].toString() ,searchTag)){
	    						curMatchedFlag = true;
//	    					updateSearchTagMatchedCnt(searchTagId, j, 1);
	    						//TODO can break here if not require matchedCnt
	    					}
	    				}
	    			}
	    		}
   			}
    		
   			boolean finFlag = false;
   			if (matchMode == MODE_OR && curMatchedFlag){
   				finFlag = true;
   			}
   			//second round, search old search tag
   			
			boolean oldMatchedFlag = true; //for and mode, require to match all tags
			if (matchMode == MODE_OR){  
				oldMatchedFlag = false;    //for or mode, require to match one tag only
			}
   			if (!finFlag){
	   			for (Component tmpTbb : searchTagDiv.getChildren()){
	   				boolean tmpMatchedFlag = false;
	   				
   					if (!(tmpTbb instanceof Toolbarbutton)){
   						continue;
   					}
    				String tmpSearchTag = ((Toolbarbutton)tmpTbb).getLabel();
    				//UniLog.log("HAHA"+ tmpTbb.getId());
    				
	   				for(int j = 0;j<listColumns.size();j++) {
	    				String key = tmpTbb.getId() + "_" + j;
	    				SearchTagField searchTagField = searchTagMap.get(key);
	    				if (searchTagField != null){
	    					if (!searchTagField.isChecked){
	    						continue;
	    					}
	    				}
	    				//if (match(result.getCell(((BiColumn) listColumns.get(j)).getLabel()).getString(),tmpSearchTag)){
	    				if (match(result.getCell(((BiColumn) listColumns.get(j)).getLabel()).getColumnDisplayString(),tmpSearchTag)){
	    					tmpMatchedFlag = true;
	    					break;
	    				}
	    			}
	   				
	   				
	   				if (matchMode == MODE_OR){
	   					if (tmpMatchedFlag){
	   						oldMatchedFlag = true;
	   						break;
	   					}
	   				}
	   				else if (matchMode == MODE_AND){
	   					if (!tmpMatchedFlag){
	   						oldMatchedFlag = false;
	   						break;
	   					}
	   				}
	    		}
	   			
   			}
   			
   			//final round, add to result
   			if (matchMode == MODE_OR){
   				if ((curMatchedFlag || oldMatchedFlag) || (result.isMarkedDelete(i) || result.isMarkedUpdate(i))){
					TrStatFilter trf = new TrStatFilter(i);
					searchResultStatList.add(trf);
					updateTrStatHM(result.getResultStat().elementAt(i), trf, null);
    			}
   			}
   			else{
   				if ((curMatchedFlag && oldMatchedFlag) || (result.isMarkedDelete(i) || result.isMarkedUpdate(i))){
					TrStatFilter trf = new TrStatFilter(i);
					searchResultStatList.add(trf);
					updateTrStatHM(result.getResultStat().elementAt(i), trf, null);
    			}
   				
   			}
    		
    	}
    	if (p_addSearchTag){
    		addSearchTag(searchTagId);
    	}
    	/*
		listModelList.clear();
        listModelList.addAll(searchResultStatList);
        */
    	searchCb.searchResult(searchResultStatList,tl);
    }
    public TrStatFilter getTrStatFilterByTrStat(Object p_trStat){
    	synchronized(trStatHM){
    		return(trStatHM.get(p_trStat));
    	}
    }
    private void updateTrStatHM(Object p_tr, TrStatFilter p_trf, Object p_oldTr){
    	synchronized(trStatHM){
    		if (p_oldTr != null){
    			trStatHM.remove(p_oldTr);
    		}
    		trStatHM.put(p_tr, p_trf);
    	}
    }
    private void clearTrStatHM(){
    	synchronized(trStatHM){
    		trStatHM.clear();
    	}
    }
    public void clearSearch(){
    	if (!tbSearchBox.isVisible()){
    		return;
    	}
    	tbSearchBox.setValue("");
    	cbSearchBox.setChecked(defaultSearchAnd);
    	Components.removeAllChildren(searchTagDiv);
    	updateLocationURLQuickFilterTags();
    }
    private void updateLocationURLQuickFilterTags() {
 		if (!ZkSessionHelper.getSessionHelper().getAllowQuickFilterURLParam())
 			return;
 		JsonArray ja = new JsonArray();
 		String matchMode = "or";
    	if (searchTagDiv != null && cbSearchBox != null) {
			for (Component tmpTbb : searchTagDiv.getChildren()){
   				if (!(tmpTbb instanceof Toolbarbutton))
   					continue;
   				try {
					ja.add(URLEncoder.encode(((Toolbarbutton)tmpTbb).getLabel(), "UTF-8"));
				} catch (Exception e) {
					UniLog.log1("updateLocationURLQuickFilterTags error:%s", e.getMessage());
				}
	   		}
			matchMode = cbSearchBox.isChecked() ? "and" : "or";
    	}
   		ZkUtil.js("updateLocationURLQuickFilterTags(%s, '%s')", ja.toString(), matchMode);
    }
    public static boolean match(String p_field, String p_searchTag){
    	return(StringUtil.matchString(p_field, p_searchTag, true));
    }
    
    public void setDefaultSearcAndMode(boolean p_and) {
		defaultSearchAnd = p_and;
    }
    public boolean getDefaultSearcAndMode() {
		return(defaultSearchAnd);
    }
}
