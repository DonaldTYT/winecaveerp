package com.uniinformation.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.ClientInfoEvent;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Layout;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.NoDOM;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Separator;
import org.zkoss.zul.Space;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.InputElement;
import org.zkoss.zul.impl.LabelElement;
import org.zkoss.zul.impl.MessageboxDlg;
import org.zkoss.zul.impl.XulElement;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.google.common.collect.Lists;
import com.google.gson.JsonObject;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxChangeListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.JxZkSystem;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxSelOpt;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.ZkBiReportProblemDialog;
import com.uniinformation.zkbi.ZkBiTranslateHelper;
import com.uniinformation.zkcomp.S2Listbox;
import com.uniinformation.zkcomp.ZkBiButton;
import com.uniinformation.zkf.ZkForm;
import com.uniinformation.zkf.ZkfAction;

public class ZkUtil extends BiUtil{
	
	public static Component getRootComponent(Component c){
		if (c.getParent() == null){
			return(c);
		}
		else{
			return(getRootComponent(c.getParent()));
		}
	}
	/***
	 * get attribute from component / component parent 
	 * @param c 
	 * @param p_attrName
	 * @param p_maxLevel > 0, obtain from parent component
	 * @return
	 */
	public static Object getAttribute(Component p_comp, String p_attrName, int p_maxLevel){
		if (p_comp == null){
			return(null);
		}
		if (p_comp.getAttribute(p_attrName) != null){
			return(p_comp.getAttribute(p_attrName));
		}
		if (p_maxLevel > 0){
			return(getAttribute(p_comp.getParent(), p_attrName, p_maxLevel - 1));
		}
		return(null);
	}
	public static int getAttributeInt(Component p_comp, String p_attrName, int p_maxLevel, int p_defaultValue){
		Object resultObj = getAttribute(p_comp, p_attrName, p_maxLevel);
		if (resultObj instanceof Integer){
			return ((Integer)resultObj);
		}
		else{
			return(p_defaultValue);
		}
	}
	public static String getAttributeString(Component p_comp, String p_attrName, int p_maxLevel, String p_defaultValue){
		Object resultObj = getAttribute(p_comp, p_attrName, p_maxLevel);
		if (resultObj instanceof String){
			return ((String)resultObj);
		}
		else{
			return(p_defaultValue);
		}
	}
	/*
	//delayExecute replaced by ZkBiAbstractiLongOp
	public static void delayExecute(Component p_comp, int p_delay, EventListener<?> p_ev){
		if (p_comp.getPage() == null){
			UniLog.log("page is null, ignore set focus");
			return;
		}
		Timer timer = new Timer();
		timer.setDelay(p_delay);
		timer.setPage(p_comp.getPage());
		timer.setRepeats(false);
		timer.addEventListener(Events.ON_TIMER, p_ev);
		timer.setRunning(true);
	}
	*/
	
	public static void delayPostEvent(final String name, final Component target, final Object data, int delay) {
		delayPostEvent(name, target, target, data, delay);
	}
	public static void delayPostEvent(final String p_eventName, final Component p_pageComp, final Component p_targetComp, final Object data, int delay) {
		final Timer timer = new Timer();
		timer.setPage(p_pageComp.getPage());
		timer.setDelay(delay);
		timer.setRepeats(false);
		timer.addEventListener(Events.ON_TIMER, new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				Events.postEvent(p_eventName, p_targetComp, data);
				timer.setRunning(false);
				timer.detach();
			}
		});
		timer.setRunning(true);
	}
	public static void delayPostEvent(final String p_eventName, final Component p_fellowComp, final String p_targetId, final Object data, int delay) {
		final Timer timer = new Timer();
		timer.setPage(p_fellowComp.getPage());
		timer.setDelay(delay);
		timer.setRepeats(false);
		timer.addEventListener(Events.ON_TIMER, new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				Component targetComp = p_fellowComp.getFellowIfAny(p_targetId);
				if (targetComp != null){
					Events.postEvent(p_eventName, targetComp, data);
				}
				else{
					UniLog.logm(null, "targetComp not found %s", p_targetId);
				}
				timer.setRunning(false);
				timer.detach();
			}
		});
		timer.setRunning(true);
	}
	
	public static void main(String args[]) throws Exception{
		
		InputStream is = new FileInputStream("/tmp/a.txt");
		byte[] datas = IOUtils.toByteArray(is);
		is.close();
		InputStream is1 = new ByteArrayInputStream(datas);
		InputStream is2 = new ByteArrayInputStream(datas);
		backupFile(null, null, is1);
		backupFile(null, null, is2);
		
		
		UniLog.log1("DEBUG:%s",ZkUtil.checkEnv("DEBUG", "Y"));
		if (true) return;
		
		
		//genZkTemplate(SessionHelper.getSessionHelperDummy("afsdev"), "AfsCustomer",2);
		
		//test encryption / decyption
		SessionHelper sh = ZkSessionHelper.getSessionHelperDummy("afsdev","dummy",null);
		String str = "abc\u4F60\u597D";
		ArrayList<String> eStrList = new ArrayList<String>();
		eStrList.add(ZkUtil.encryptStrToBase64(sh, str));
		eStrList.add(ZkUtil.encryptStrToBase64(sh, str));
		eStrList.add(ZkUtil.encryptStrToBase64(sh, str));
		for (String eStr : eStrList) {
			String dStr = ZkUtil.decryptStrFromBase64(sh , eStr);
			UniLog.log1("[%s] [%s]", eStr, dStr);
		}
		
		str = "0000000000";
		eStrList = new ArrayList<String>();
		eStrList.add(ZkUtil.encryptStrToBase64(sh, str));
		eStrList.add(ZkUtil.encryptStrToBase64(sh, str));
		eStrList.add(ZkUtil.encryptStrToBase64(sh, str));
		for (String eStr : eStrList) {
			String dStr = ZkUtil.decryptStrFromBase64(sh , eStr);
			UniLog.log1("[%s] [%s]", eStr, dStr);
		}
		
		str = "00000000001111111111";
		eStrList = new ArrayList<String>();
		eStrList.add(ZkUtil.encryptStrToBase64(sh, str));
		eStrList.add(ZkUtil.encryptStrToBase64(sh, str));
		eStrList.add(ZkUtil.encryptStrToBase64(sh, str));
		for (String eStr : eStrList) {
			String dStr = ZkUtil.decryptStrFromBase64(sh , eStr);
			UniLog.log1("[%s] [%s]", eStr, dStr);
		}
		
		
		
		/*
		String rtn;
		rtn = extractColWidthValue("hflex=min","hflex"); UniLog.log(rtn);
		rtn = extractColWidthValue("hflex=min","width"); UniLog.log(rtn);
		rtn = extractColWidthValue("width=123px","hflex"); UniLog.log(rtn);
		rtn = extractColWidthValue("width=123px","width"); UniLog.log(rtn);
		*/
		System.exit(0);
	}
	
	private static void genTemplateRowByColsBis(StringBuffer p_outSb, int p_rowIdx, ArrayList<ArrayList<BiColumn>> p_colsBis){
	    p_outSb.append("\t\t<row>\r\n");
	    for (int colIdx=0; colIdx<p_colsBis.size(); colIdx++){
	    	if (p_rowIdx < p_colsBis.get(colIdx).size()){
	    		BiColumn col = p_colsBis.get(colIdx).get(p_rowIdx);
	    		//p_outSb.append(String.format("\t\t\t<label hflex=\"1\" id=\"lb_%s\" value=\"%s\"><custom-attributes tlkey=\"lb_%s\"></custom-attributes></label>\r\n", col.getLabel(), col.getEngName(), col.getLabel()));
	    		p_outSb.append(String.format("\t\t\t<label hflex=\"1\" id=\"lb_%s\" value=\"%s\"><custom-attributes tlkey=\"%s\"></custom-attributes></label>\r\n", col.getLabel(), col.getEngName(), col.getLabel()));  //andrew220705 unify tlkey without lb_ prefix. ref redmine 869
	    		if (col.isLookup()){
			   		//p_outSb.append(String.format("\t\t\t<bandbox hflex=\"1\" id=\"%s\"/>\r\n",col.getLabel()));
			   		p_outSb.append(String.format("\t\t\t<bandbox hflex=\"1\" id=\"%s\" use=\"com.uniinformation.jx.zk.ZkJxPickInput\"/>\r\n",col.getLabel()));
		   		}
		   		else if (col.getColumnType().trim().equals("integer")) {
			   		p_outSb.append(String.format("\t\t\t<doublebox hflex=\"1\" id=\"%s\"/>\r\n",col.getLabel()));
		   		}
		   		else if (col.getColumnType().trim().equals("float")) {
			   		p_outSb.append(String.format("\t\t\t<intbox hflex=\"1\" id=\"%s\"/>\r\n",col.getLabel()));
		   		}
		   		else if (col.getColumnType().trim().equals("checkbox")) {
			   		p_outSb.append(String.format("\t\t\t<checkbox hflex=\"1\" id=\"%s\"/>\r\n",col.getLabel()));
		   		}
		   		else {
			   		p_outSb.append(String.format("\t\t\t<textbox hflex=\"1\" id=\"%s\"/>\r\n",col.getLabel()));
		   		}
	    	}
	    	else{
	    		p_outSb.append(String.format("\t\t\t<div></div>\r\n")); //empty div
	    		p_outSb.append(String.format("\t\t\t<div></div>\r\n")); //empty div
	    	}
	    }
	    p_outSb.append("\t\t</row>\r\n");
	}
	/*
	private static void genTemplateRowByCol(StringBuffer p_outSb, BiColumn p_col){
	    p_outSb.append("\t<row>\r\n");
	    p_outSb.append(String.format("\t\t<label id=\"lb_%s\" value=\"%s\"/>\r\n", p_col.getLabel(), p_col.getEngName()));
		if (p_col.isLookup()){
			p_outSb.append(String.format("\t\t<bandbox` id=\"%s\"/>\r\n",p_col.getLabel()));
		}
		else if (p_col.getColumnType().trim().equals("integer")) {
			p_outSb.append(String.format("\t\t<doublebox id=\"%s\"/>\r\n",p_col.getLabel()));
		}
		else if (p_col.getColumnType().trim().equals("float")) {
			p_outSb.append(String.format("\t\t<intbox id=\"%s\"/>\r\n",p_col.getLabel()));
		}
		else {
			p_outSb.append(String.format("\t\t<textbox id=\"%s\"/>\r\n",p_col.getLabel()));
		}
	    p_outSb.append("\t</row>\r\n");
	}
	*/
	private static void genTemplateRowByView(StringBuffer p_outSb, BiView p_view, int p_spans){
	    p_outSb.append(String.format("\t<row spans=\"%d\">\r\n", p_spans));
	    p_outSb.append("\t<div>\r\n");
	    p_outSb.append(String.format("\t\t<label id=\"lb_list_%s\" value=\"%s\"/>\r\n", p_view.getHeader(), p_view.getHeader()));
	    p_outSb.append(String.format("\t\t<listbox id=\"list_%s\"/>",p_view.getName()));
	    p_outSb.append("\t</div>\r\n");
	    p_outSb.append("\t</row>\r\n");
	}
	
	public static String genZkTemplate(SessionHelper p_sh, String p_viewId, int p_maxColIdx) throws Exception{
			int maxColIdx = p_maxColIdx;
			if (maxColIdx < 0){
				maxColIdx = 0;
			}
			ArrayList<ArrayList<BiColumn>> colsBis = new ArrayList<ArrayList<BiColumn>>();  //list of bicol list. one item = one column
			for (int i=0; i<=maxColIdx; i++){
				colsBis.add(new ArrayList<BiColumn>());
			}
			
			BiSchema schema;
			BiView view;
			StringBuffer sb = new StringBuffer();
			sb.append("<zk>\r\n");
			sb.append("<grid style=\"text-align:left;\" id=\"detail_grid\">\r\n");
		    sb.append("\t<columns>\r\n");
		    for (int i=0; i<=maxColIdx; i++){
		    	sb.append("\t\t<column align=\"right\" hflex=\"min\" />\r\n");
		    	sb.append("\t\t<column hflex=\"1\" />\r\n");
		    }
		    sb.append("\t</columns>\r\n");
		    sb.append("\t<rows>\r\n");
	    
			schema = BiSchema.loadSchema(p_sh);
			
			view = schema.getViewByName(p_viewId);
			UniLog.log("view = " + view);
			Vector cols = view.getColumns();
			
			//construct data structure
			for(int i=0; i<cols.size(); i++) {
				BiColumn col = (BiColumn) cols.get(i);
				UniLog.logm(null, "view:%s col:%d: label:%s engName:%s chnName:%s parent:%s isInvisible:%s", view.getName(), i, col.getLabel(), col.getEngName(), col.getChnName(), col.getParent(), col.isInvisible(p_sh));
				if (col.isInvisible(p_sh)) {
					continue;
				}
				
				int colIdx = col.getZkColIdx();
				if (colIdx > maxColIdx){
					colIdx = maxColIdx;
				}
				colsBis.get(colIdx).add(col);
			}
			
			//render row
			for (int rowIdx=0;;rowIdx++){
				boolean hasData = false;
				for (int colIdx=0; colIdx<=maxColIdx; colIdx++){
					if (rowIdx < colsBis.get(colIdx).size()){
						hasData = true;
					}
				}
				if (hasData){
					genTemplateRowByColsBis(sb, rowIdx, colsBis);
				}
				else{
					UniLog.logm(null,"no more data");
					break;
				}
			}
			
			//render detail view
			BiResult result = view.newBiResult(p_sh.getLoginId(),null,null,p_sh);
			Vector subLinks = result.getSubLinks();
			if (subLinks != null && subLinks.size() > 0){
				for(int i=0;i<subLinks.size();i++) {
					Component linkComp = null;
					BiResult subLinkResult = (BiResult) subLinks.get(i);
					BiView subLinkView = subLinkResult.getView();
					Vector subLinkCols = subLinkView.getColumns();
					UniLog.logm(null, "subLinkBiView:%d:%s",i,subLinkView.getName());
					genTemplateRowByView(sb, subLinkView, (maxColIdx+1)*2);
					for (int j=0; j<subLinkCols.size(); j++){
						BiColumn subLinkCol = (BiColumn) subLinkCols.get(j);
						UniLog.logm(null, "subview:%s col:%d: label:%s engName:%s chnName:%s parent:%s", subLinkView.getName(), j, subLinkCol.getLabel(), subLinkCol.getEngName(), subLinkCol.getChnName(), subLinkCol.getParent());
					}
				}
			}
			
			
		    sb.append("\t</rows>\r\n");
			sb.append("</grid>\r\n");
			sb.append("</zk>\r\n");
			
			UniLog.logm(null,"\n"+sb.toString());
			/*
			UniLog.logm(null,"write to file %s", p_outFile);
			FileOutputStream fos = new FileOutputStream(p_outFile);
			fos.write(sb.toString().getBytes());
			*/
			
			/*
			//obtain query result
			UniLog.logm(null,"BiResult : " + result);
			if(result != null) {
				result.query();
			}
			*/
			return(sb.toString());
	}
	
	public static void print(Component comp) throws JSONException {
		print(comp, "zkprint.zul");
	}
	public static void print(Component comp, String uri) throws JSONException {
		print(comp, uri, null);
	}
	public static void print(Component comp, JSONObject jsonData) throws JSONException {
		print(comp, "zkprint.zul", jsonData);
	}
	public static void print(Component comp, String uri, JSONObject jsonData) throws JSONException {
		Clients.evalJavaScript(String.format(
			"if (typeof ZkPrint === 'undefined') {"
			+ "	alert('Print no support.');"
			+ "} else {"
			+ "	ZkPrint.print('%s', '%s', %s);"
			+ "}", comp.getUuid(), uri, jsonData != null ? jsonData.toString() : "{}"));
	}
	public static void printFromStream(InputStream inputDataStream, String mimeType, SessionHelper sessionHelper) {
		Clients.evalJavaScript(String.format(
			"if (typeof ZkPrint === 'undefined') {"
			+ "	alert('Print no support.');"
			+ "} else {"
			+ "	ZkPrint.printFromStream('%s', '%s');"
			+ "}", getDownloadLinkFromStream(inputDataStream, mimeType, sessionHelper), mimeType));
	}
	public static String getDownloadLinkFromStream(InputStream inputDataStream, String mimeType, SessionHelper sessionHelper) {
		return getDownloadLinkFromStream(inputDataStream, mimeType, sessionHelper, null, null, false);
	}
	public static String getDownloadLinkFromStream(final InputStream inputDataStream, String mimeType, SessionHelper sessionHelper, 
			String streamKey, String mimeTypeKey, boolean needKeepStream) {
		long currentTime = new Date().getTime();
		if (StringUtils.isBlank(streamKey))
			streamKey = "zk_print_stream_" + currentTime;
		if (StringUtils.isBlank(mimeTypeKey))
			mimeTypeKey = "zk_print_mimetype_" + currentTime;
		sessionHelper.putSessionDataEx(streamKey, inputDataStream, new SessionHelper.SessionDataExCleanUpCallback());
		sessionHelper.putSessionDataEx(mimeTypeKey, mimeType);
		return String.format("zkprint_stream.jsp?zk_print_stream_key=%s&zk_print_mimetype_key=%s&keep_stream=%s", 
				streamKey, mimeTypeKey, needKeepStream ? "Y" : "N");
	}
	
	/***
	 * add drag and drop function to listbox and listitem
	 * @param p_xul
	 */
    public static void addDragAndDrop(org.zkoss.zul.impl.XulElement p_xul){
    	if (p_xul.getDraggable() == null || !p_xul.getDraggable().equals("true")){
    		UniLog.logm(null,"set drag and drop");
    		if (p_xul instanceof Listitem){
    			p_xul.setDraggable("true");
    		}
    		p_xul.setDroppable("true");
    		p_xul.addEventListener(Events.ON_DROP, new EventListener<Event>(){
    			public void onEvent(Event event) throws Exception {
    				Listitem drag = (Listitem)((DropEvent) event).getDragged();
    				if (event.getTarget() instanceof Listitem){
    					UniLog.logm(null,"drop item type: listitem");
    					Listitem drop = (Listitem)((DropEvent) event).getTarget();
    					drop.getListbox().insertBefore(drag, drop);
    				}
    				else if (event.getTarget() instanceof Listbox){
    					UniLog.logm(null,"drop item type: listbox");
    					drag.getListbox().appendChild(drag);
    				}
    			}
    		});
    	}
    	else{
    		UniLog.logm(null,"ignore set drag and drop");
    	}
    }
    public static void appendStyle(HtmlBasedComponent p_htmlComp, String p_style){
    	if (p_htmlComp == null || p_style == null || p_style.trim().equals("")){
    		UniLog.logm(null,"action ignore");
    		return;
    	}
    	StringBuilder sb = new StringBuilder();
    	if (p_htmlComp.getStyle() != null && !p_htmlComp.getStyle().trim().equals("")){
    		sb.append(p_htmlComp.getStyle());
    	}
    	
    	if (!sb.toString().trim().endsWith(";")){
    		sb.append(";");
    	}
   		sb.append(p_style);
    	p_htmlComp.setStyle(sb.toString());
    }    
    public static void appendSclass(HtmlBasedComponent p_htmlComp, String p_class){
    	if (p_htmlComp == null || p_class == null || p_class.trim().equals("")){
    		UniLog.logm(null,"action ignore");
    		return;
    	}
    	StringBuilder sb = new StringBuilder();
    	if (StringUtils.isNotBlank(p_htmlComp.getSclass())) {
    		sb.append(p_htmlComp.getSclass());
    	}
   		sb.append(" ");
   		sb.append(p_class);
    	p_htmlComp.setSclass(sb.toString().trim());
    }    
    public static void appendSclass(Component p_comp, String p_class){
    	if (p_comp instanceof HtmlBasedComponent) {
    		appendSclass((HtmlBasedComponent) p_comp, p_class);
    	}
    }
    /*
    //andrew190521: this method obsoleted
	public static void translateAllComp(SessionHelper p_sessionHelper, Component p_comp){
		//sample zul for reference <label value="Language"><custom-attributes lhlang="Y"/></label>
		for (Component curComp :p_comp.getChildren()){
			//UniLog.logm(this,"%s:%s", curComp, curComp.getAttribute("lhlang"));
			if (curComp instanceof Label){
				Label curLabel = (Label) curComp;
				if (curLabel.getValue() != null && "Y".equals(curLabel.getAttribute("lhlang"))){
					curLabel.setValue(p_sessionHelper.getLabel(curLabel.getValue()));
				}
			}
			if (curComp.getChildren().size() > 0){
				translateAllComp(p_sessionHelper, curComp);
			}
		}
	}
	*/
    /***
     * translate page without biview
     * key format [page_id].[tlkey]
     * @param p_sh
     * @param p_pageid
     * @param p_comp
     */
	public static void translateAllComp(SessionHelper p_sh, String p_pageid, Component p_comp){
		if (p_sh == null) {
			UniLog.log1("sessionHelper is null");
			return;
		}
		if (StringUtils.isBlank(p_pageid)) {
			UniLog.log1("pageid is blank");
			return;
		}
		if (p_comp == null) {
			UniLog.log1("comp is null");
			return;
		}
		translateAllComp(p_sh, p_comp, p_pageid, null);
	}
	
	/***
	 * translate page with biview
	 * key format [viewName].[tlkey]
	 * @param p_result
	 * @param p_comp
	 */
	public static void translateAllComp(BiResult p_result, Component p_comp){
		if (p_result == null) {
			UniLog.log1("result is null");
			return;
		}
		if (p_comp == null) {
			UniLog.log1("comp is null");
			return;
		}
		if (p_result.getSessionHelper() == null) {
			UniLog.log1("sessionHelper is null");
			return;
		}
		translateAllComp(p_result.getSessionHelper(), p_comp, p_result.getView().getName(), p_result);
	}
	
	/***
	 * new version, support both view / non-view page
	 * @param p_sh
	 * @param p_comp
	 * @param p_keyPrefix
	 * @param p_result
	 */
	public static void translateAllComp(SessionHelper p_sh, Component p_comp, String p_keyPrefix, BiResult p_result){
		// HAHA need update , has performance issue
		if (p_sh == null){
			UniLog.log1("sessionHelper is null");
			return;
		}
		if (p_sh.getAllowUpdateTranslate() && !p_sh.getAllowTranslate()){
			UniLog.log1("skip translation");
			return;
		}
		List<Component> comps = p_comp.getChildren();
		if (comps == null || comps.size() == 0){
			return;
		}
		for (int i=0; i<comps.size(); i++){
			Component curComp = comps.get(i);
//			UniLog.log("240316 Translate " + p_comp.getId() + " " + i + " of " + comps.size() + " type " + comps.getClass().toString());
			String tlkey = (String) curComp.getAttribute("tlkey");
			boolean isLeafNode = false;
			if (!StringUtils.isBlank(tlkey)){
				String key = p_keyPrefix +"." + tlkey;
				if (curComp instanceof Label){
					Label label = (Label) curComp;
					
					//obtain label value from zk label
					String defaultValue = label.getValue();
					if (defaultValue == null){
						//obtain label value from bicol
						if (p_result != null) {
							BiColumn col = p_result.getColumnByLabel(tlkey.startsWith("lb_") ? tlkey.substring(3) : tlkey);
							if (col != null){
								defaultValue = p_result.getSessionHelper().getLabel(col);
							}
						}
					}
					if (defaultValue == null){
						defaultValue = "";
					}
					
					if (p_sh.getAllowUpdateTranslate()){
						JxZkBiBase.addContextMenu(p_sh, (XulElement) curComp, MapUtil.of("changeLabel", MapUtil.of("key",key,"defaultValue",defaultValue)));
					}
					if (p_sh.getAllowTranslate()){
						label.setValue(ZkBiTranslateHelper.getText(p_sh, key, "LABEL", defaultValue));
					}
					isLeafNode = true;
				}
				/*
				//andrew 221130 handle button as label element
				else if (curComp instanceof Button){
					Button button = (Button) curComp;
					String defaultValue = button.getLabel();
					if (defaultValue == null){
						defaultValue = "";
					}
					if (p_sh.getAllowUpdateTranslate()){
						JxZkBiBase.addContextMenu(p_sh, (XulElement) curComp, MapUtil.of("changeLabel", MapUtil.of("key",key,"defaultValue",defaultValue)));
					}
					if (p_sh.getAllowTranslate()){
						button.setLabel(ZkBiTranslateHelper.getText(p_sh, key, "BUTTON", defaultValue));
					}
					isLeafNode = true;
				}
				else if (curComp instanceof Checkbox) {  //andrew221130 handle checkbox translation
					Checkbox checkbox = (Checkbox) curComp;
					String defaultValue = checkbox.getLabel();
					if (defaultValue == null){
						defaultValue = "";
					}
					if (p_sh.getAllowUpdateTranslate()){
						JxZkBiBase.addContextMenu(p_sh, (XulElement) curComp, MapUtil.of("changeLabel", MapUtil.of("key",key,"defaultValue",defaultValue)));
					}
					if (p_sh.getAllowTranslate()){
						checkbox.setLabel(ZkBiTranslateHelper.getText(p_sh, key, "BUTTON", defaultValue));
					}
					isLeafNode = true;
				}
				*/
				else if (curComp instanceof Button || curComp instanceof Tab || curComp instanceof Checkbox) {  //andrew221130 handle button,checkbox,tab translation
					LabelElement le = (LabelElement) curComp;
					String defaultValue = le.getLabel();
					if (defaultValue == null){
						defaultValue = "";
					}
					if (p_sh.getAllowUpdateTranslate()){
						JxZkBiBase.addContextMenu(p_sh, (XulElement) curComp, MapUtil.of("changeLabel", MapUtil.of("key",key,"defaultValue",defaultValue)));
					}
					if (p_sh.getAllowTranslate()){
						le.setLabel(ZkBiTranslateHelper.getText(p_sh, key, "BUTTON", defaultValue));
					}
					isLeafNode = true;
				}
				else if (curComp instanceof Radiogroup) {
					if (p_result.getSessionHelper().getAllowOptionTranslate()) {
						Radiogroup radiogroup = (Radiogroup) curComp;
						for (Component crd : radiogroup.queryAll("Radio")) {
							Radio rd = (Radio) crd;
							String defaultValue = StringUtils.defaultString(rd.getLabel());
							String key1 = key + "." + rd.getValue();
							if (p_sh.getAllowUpdateTranslate()){
								JxZkBiBase.addContextMenu(p_sh, (XulElement) rd, MapUtil.of("changeLabel", MapUtil.of("key",key1,"defaultValue",defaultValue)));
							}
							if (p_sh.getAllowTranslate()){
								rd.setLabel(ZkBiTranslateHelper.getText(p_sh, key1, "OPTION", defaultValue));
							}
						}
						isLeafNode = true;
					}
				}

			}
			if (!isLeafNode && curComp.getChildren().size() > 0){
				translateAllComp(p_sh, curComp, p_keyPrefix, p_result);
			}
		}
	}
	
	
	public static void translateOneComp(SessionHelper p_sh, Component curComp, String p_keyPrefix, BiResult p_result){
			String tlkey = (String) curComp.getAttribute("tlkey");
			boolean isLeafNode = false;
			if (!StringUtils.isBlank(tlkey)){
				String key = p_keyPrefix +"." + tlkey;
				if (curComp instanceof Label){
					Label label = (Label) curComp;
					
					//obtain label value from zk label
					String defaultValue = label.getValue();
					if (defaultValue == null){
						//obtain label value from bicol
						if (p_result != null) {
							BiColumn col = p_result.getColumnByLabel(tlkey.startsWith("lb_") ? tlkey.substring(3) : tlkey);
							if (col != null){
								defaultValue = p_result.getSessionHelper().getLabel(col);
							}
						}
					}
					if (defaultValue == null){
						defaultValue = "";
					}
					
					if (p_sh.getAllowUpdateTranslate()){
						JxZkBiBase.addContextMenu(p_sh, (XulElement) curComp, MapUtil.of("changeLabel", MapUtil.of("key",key,"defaultValue",defaultValue)));
					}
					if (p_sh.getAllowTranslate()){
						label.setValue(ZkBiTranslateHelper.getText(p_sh, key, "LABEL", defaultValue));
					}
					isLeafNode = true;
				}
				else if (curComp instanceof Button){
					Button button = (Button) curComp;
					String defaultValue = button.getLabel();
					if (defaultValue == null){
						defaultValue = "";
					}
					if (p_sh.getAllowUpdateTranslate()){
						JxZkBiBase.addContextMenu(p_sh, (XulElement) curComp, MapUtil.of("changeLabel", MapUtil.of("key",key,"defaultValue",defaultValue)));
					}
					if (p_sh.getAllowTranslate()){
						button.setLabel(ZkBiTranslateHelper.getText(p_sh, key, "BUTTON", defaultValue));
					}
					isLeafNode = true;
				}

			}
	
	}
	/***
	 * find child component by id
	 * @param p_comp
	 * @param p_id
	 * @return
	 */
	public static Component findChild(Component p_comp, String p_id){
		if (p_comp == null) {
			return (null);
		}
		if (StringUtils.isBlank(p_id)){
			return(null);
		}
		for (Component curComp : p_comp.getChildren()){
			if (StringUtils.equals(p_id, curComp.getId())){
				return(curComp);
			}
			if (curComp.getChildren().size() > 0){
				Component rtnComp = findChild(curComp, p_id);
				if (rtnComp != null){
					return (rtnComp);
				}
			}
		}
		return(null);
	}
	/***
	 * publish desktop event
	 * Remark: side effect. it poll server every 1.5 sec
	 * 
	 * @param p_data
	 */
	public static void sendDesktopEvent(Object p_data){
	    UniLog.log1("check queue exists:%s",EventQueues.exists("zkbi", EventQueues.DESKTOP));
	    EventQueue que = EventQueues.lookup("zkbi", EventQueues.DESKTOP, true);
	    que.publish(new Event("onDesktopEvent", null, p_data));
		
	}
	/***
	 * receive desktop event
	 * @param p_el
	 */
	public static void receiveDesktopEvent(EventListener p_el){
	    UniLog.log1("check queue exists:%s",EventQueues.exists("zkbi", EventQueues.DESKTOP));
		EventQueue eventQueue = EventQueues.lookup("zkbi", EventQueues.DESKTOP, true);
	    EventListener el = p_el;
	    if (el == null){ //log the event
			el = new EventListener(){
				@Override
				public void onEvent(Event p_event) throws Exception {
					UniLog.log1("got event:"+ p_event.getName() + ":"+ p_event.getData());
				}
			};
	    }
		eventQueue.subscribe(el);
	}
	
	
	/***
	 * publish session event
	 * Remark: side effect. it poll server every 1.5 sec
	 * 
	 * @param p_data
	 */
	public static void sendSessionEvent(Object p_data){
	    UniLog.log1("check queue exists:%s",EventQueues.exists("zkbisession", EventQueues.SESSION));
	    EventQueue que = EventQueues.lookup("zkbisession", EventQueues.SESSION, true);
	    que.publish(new Event("onSessionEvent", null, p_data));
		
	}
	/***
	 * receive session event
	 * @param p_el
	 */
	public static void receiveSessionEvent(EventListener p_el){
	    UniLog.log1("check queue exists:%s",EventQueues.exists("zkbisession", EventQueues.SESSION));
		EventQueue eventQueue = EventQueues.lookup("zkbisession", EventQueues.SESSION, true);
	    EventListener el = p_el;
	    if (el == null){ //log the event
			el = new EventListener(){
				@Override
				public void onEvent(Event p_event) throws Exception {
					UniLog.log1("got event:"+ p_event.getName() + ":"+ p_event.getData());
				}
			};
	    }
		eventQueue.subscribe(el);
	}
	
	/***
	 * send broadcast message
	 * Remark: side effect. it poll server every 1.5 sec
	 * @param p_sessionHelper
	 * @param p_msg
	 */
	public static ReturnMsg sendBroadcast(SessionHelper p_sessionHelper, String p_destSessionKey, String p_type, String p_data){
		if (p_sessionHelper == null || !p_sessionHelper.getAllowBroadcastMsg()) {
			UniLog.log1("do not allow to send broadcast");
			return new ReturnMsg(false,"Do not allow to send message");
		}
	    
	    JsonObject json = new JsonObject();
	    if (StringUtils.equals(p_type,"logoutUser")){ //called by system tools
	    	json.addProperty("data", p_data);
	    }
	    else if (StringUtils.equals(p_type,"message")){
	    	//prepare message
	    	if (StringUtils.isBlank(p_data)) {
	    		UniLog.log1("ignore blank message");
	    		return new ReturnMsg(false,"Ignore blank message");
	    	}
	    	StringBuilder msgSb = new StringBuilder();
	    	String sender = p_sessionHelper == null ? "Unknown Sender" : p_sessionHelper.getLoginId();
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    	msgSb.append(String.format("Message From: %s (%s)\n\n", sender, sdf.format(new Date())));
	    	msgSb.append(p_data);
	    	json.addProperty("data", msgSb.toString());
	    }
	    else {
	    	UniLog.log1("unknow type %s, action ignore", p_type);
	    	return new ReturnMsg(false, "Unknow message type");
	    }
	    json.addProperty("type", p_type);
	    json.addProperty("sourceSessionKey", p_sessionHelper == null ? "" : p_sessionHelper.getSessionKey());
	    json.addProperty("destSessionKey", p_destSessionKey == null ? "" : p_destSessionKey);
	    
	    //send msg
	    EventQueue que = EventQueues.lookup("broadcast", EventQueues.APPLICATION, true);
	    que.publish(new Event("onSystemMessage", null, json.toString()));
	    UniLog.log1("send %s", json.toString());
	    return ReturnMsg.defaultOk;
	}
	/***
	 * 
	 * @param p_sessionHelper - allow null
	 */
	public static void receiveBroadcast(final SessionHelper p_sessionHelper){
		if (p_sessionHelper == null || !p_sessionHelper.getAllowBroadcastMsg() || StringUtils.isBlank(p_sessionHelper.getSessionKey())){
			UniLog.log1("do not receive broadcast");
			return;
		}
		EventQueue eventQueue = EventQueues.lookup("broadcast", EventQueues.APPLICATION, true);
		eventQueue.subscribe(new EventListener() {
            public void onEvent(Event event) throws Exception {
        		UniLog.logm(this,"got event new: name:%s data:%s", event.getName(), event.getData());
        		//TODO: should filter the message based on agent, loginid
        		/*
        		if (!p_sessionHelper.isLogin()){
        			UniLog.logm(this,"skip display message");
        			return;
        		}
        		*/
            	if (event.getName() != null && event.getName().equals("onSystemMessage")){
            		JsonObject json = GsonUtil.createJsonObject(event.getData() + "");
            		if (json == null) {
            			UniLog.log1("json is null");
            			return;
            		}
       				String destSessionKey = GsonUtil.getString(json, "destSessionKey");
            		String type = GsonUtil.getString(json, "type");
            		if (StringUtils.equals(type, "message")){
            			if (StringUtils.equalsAny(destSessionKey,"ALL",p_sessionHelper.getSessionKey())) {
            				//Messagebox.show("" + GsonUtil.getString(json, "data"), "System Message", Messagebox.OK, Messagebox.INFORMATION);	
            				ZkUtil.normMsg("" + GsonUtil.getString(json, "data"));
            				return;
            			}
            			UniLog.log1("message not for me");
            			return;
            		}
            		if (StringUtils.equals(type, "logoutUser")) {  //called by system tools
            			if (StringUtils.equals(p_sessionHelper.getSessionKey(), destSessionKey)){
            				Clients.confirmClose(null);
            				//p_sessionHelper.logout(); //andrew211007 logout should be called by logout.html
  			            	Executions.sendRedirect(String.format("logout.html?agent=%s&keeplt=N%s",p_sessionHelper.getAgent(),p_sessionHelper.getLargeFlag()?"&large=Y":"")); //clean logintoken
  			            	return;
            			}
            			//UniLog.log1("message not for me");
            			return;
            		}
            		UniLog.log1("type[%s] not supported", type);
            		
            	}
            }
        });
	}
	
	private static EventListener eventAdjustWinWidthOne = new EventListener() {
	@Override
	public void onEvent(Event arg0) throws Exception {
		// TODO Auto-generated method stub
		String newWidthStr = (String) arg0.getData();
		UniLog.log1("onDelayAutoAdjustWinWidthOne %s, newWidthStr:%s", arg0, newWidthStr);
		if (StringUtils.equals(newWidthStr, autoAdjustWinWidthOneNewWidthStr))
			autoAdjustWinWidthOne((HtmlBasedComponent) arg0.getTarget(), newWidthStr);	
	}
	
	};		
	
	private static String autoAdjustWinWidthOneNewWidthStr;
	private static void autoAdjustWinWidthOne(HtmlBasedComponent p_comp, String newWidthStr){
		if (p_comp.getWidth() != null &&  !p_comp.getWidth().equals(newWidthStr)){
			UniLog.logm(null,"adjustWinWidth: %s->%s", p_comp.getWidth(), newWidthStr);
			p_comp.setWidth(newWidthStr);
			//p_comp.invalidate();  //andrew 181123: use it carefully, it will cleanup non-zk component (e.g. embeded pdf)
		}
		autoAdjustWinWidthOneNewWidthStr = null;
	}
	private static void delayAutoAdjustWinWidthOne(final HtmlBasedComponent p_comp, final long p_newWidth){
		final String newWidthStr = p_newWidth + "px";
		autoAdjustWinWidthOneNewWidthStr = newWidthStr;
		UniLog.log1("delayAutoAdjustWinWidthOne %s, %s", p_comp.getWidth(), newWidthStr);
		if(!p_comp.isListenerAvailable("onDelayAutoAdjustWinWidthOne" , false)) {
			p_comp.addEventListener("onDelayAutoAdjustWinWidthOne", eventAdjustWinWidthOne );
		}
//		p_comp.addEventListener("onDelayAutoAdjustWinWidthOne", new EventListener<Event>() {
//			@Override
//			public void onEvent(Event event) throws Exception {
//				UniLog.log1("onDelayAutoAdjustWinWidthOne %s, newWidthStr:%s", event, newWidthStr);
//				String widthStr = (String) event.getData();
//				if (StringUtils.equals(widthStr, autoAdjustWinWidthOneNewWidthStr))
//					autoAdjustWinWidthOne(p_comp, widthStr);
//			}
//		});
		delayPostEvent("onDelayAutoAdjustWinWidthOne", p_comp, newWidthStr, 10);
	}
	
	

	
	
	
	
//	private static String autoAdjustWinWidthOneNewWidthStr;
//	private static void autoAdjustWinWidthOne(HtmlBasedComponent p_comp, long p_newWidth){
//		String newWidthStr = p_newWidth + "px";
//		if (p_comp.getWidth() != null &&  !p_comp.getWidth().equals(newWidthStr)){
//			UniLog.logm(null,"adjustWinWidth: %s->%s", p_comp.getWidth(), newWidthStr);
//			p_comp.setWidth(newWidthStr);
//			//p_comp.invalidate();  //andrew 181123: use it carefully, it will cleanup non-zk component (e.g. embeded pdf)
//		}
//		autoAdjustWinWidthOneNewWidthStr = null;
//	}
//
//	private static void delayAutoAdjustWinWidthOne(final HtmlBasedComponent p_comp, final long p_newWidth){
//		final String newWidthStr = p_newWidth + "px";
//		autoAdjustWinWidthOneNewWidthStr = newWidthStr;
//		UniLog.log1("delayAutoAdjustWinWidthOne %s, %s", p_comp.getWidth(), newWidthStr);
//		p_comp.addEventListener("onDelayAutoAdjustWinWidthOne", new EventListener<Event>() {
//			@Override
//			public void onEvent(Event event) throws Exception {
//				UniLog.log1("onDelayAutoAdjustWinWidthOne %s, newWidthStr:%s", event, newWidthStr);
//				if (StringUtils.equals(newWidthStr, autoAdjustWinWidthOneNewWidthStr))
//					autoAdjustWinWidthOne(p_comp, p_newWidth);
//			}
//		});
//		delayPostEvent("onDelayAutoAdjustWinWidthOne", p_comp, null, 10);
//	}

	/***
	 * adjust component width based on client side event
	 * limitation: support single component only
	 * @param p_comp
	 * @param p_offsets
	 * @param p_isMobile
	 */
	/*
	//obsoleted, replaced by registerClientInfo(), delete it later
	public static void autoAdjustWinWidth(final HtmlBasedComponent p_comp, final int p_offsets, final boolean p_isMobile){
		final AtomicLong winWidth = new AtomicLong(0);
		final AtomicBoolean sidrOpenFlag = new AtomicBoolean(true); 
		p_comp.addEventListener(Events.ON_CLIENT_INFO, new EventListener(){
			public void onEvent(Event event) throws Exception {
				if (event instanceof ClientInfoEvent){
					ClientInfoEvent cie = (ClientInfoEvent) event; 
					//Window win = (Window) event.getTarget(); //it is null due to broadcast event, so required to use final comp
					UniLog.log(String.format("ClientInfoEvent: %dx%d %s offset:%d,%d", cie.getDesktopWidth(), cie.getDesktopHeight(), cie.getOrientation(), cie.getDesktopXOffset(),cie.getDesktopYOffset()));
					if (winWidth.get() == 0){ //obtain initial status
						//Clients.evalJavaScript("if (typeof zkGetSidrStatus === \"function\"){ zkGetSidrStatus();}");
						Clients.evalJavaScript(String.format("if (typeof zkGetSidrStatus === \"function\"){ zkGetSidrStatus('"+p_comp.getId()+"');}",p_comp.getId()));
					}
					if (p_isMobile){
						winWidth.set(Math.round((cie.getDesktopWidth())));
					}
					else{
						winWidth.set(Math.round((cie.getDesktopWidth() *0.98)));
					}
					if (sidrOpenFlag.get()){
						autoAdjustWinWidthOne(p_comp, winWidth.get() - 200 + p_offsets);
					}
					else{
						autoAdjustWinWidthOne(p_comp, winWidth.get() + p_offsets);
					}
				}
			}
		});
		p_comp.addEventListener("onSidrOpen", new EventListener<Event>(){
			public void onEvent(Event event) throws Exception {
				UniLog.log(String.format("onSidrOpen"));
				sidrOpenFlag.set(true);
				autoAdjustWinWidthOne(p_comp, winWidth.get() - 200 + p_offsets);
				SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
				if (!p_isMobile)
					sessionHelper.setRequestSidrAction("open");
			}
		});
		p_comp.addEventListener("onSidrClose", new EventListener<Event>(){
			public void onEvent(Event event) throws Exception {
				UniLog.log(String.format("onSidrClose"));
				sidrOpenFlag.set(false);
				autoAdjustWinWidthOne(p_comp, winWidth.get() + p_offsets);
				SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
				if (!p_isMobile)
					sessionHelper.setRequestSidrAction("close");
			}
		});
		p_comp.addEventListener("onLogout", new EventListener<Event>(){
			public void onEvent(Event event) throws Exception {
				UniLog.log(String.format("onLogout"));
           		Messagebox.show("Are you sure to logout?", "Confirmation", Messagebox.OK, Messagebox.INFORMATION);	
			}
		});
	}
	*/
	
	/*
	//obsoleted, replaced by registerClientInfo(), delete it later
	public static void notifySidrStatus(final HtmlBasedComponent p_comp, final SessionHelper sessionHelper){
		final AtomicLong winWidth = new AtomicLong(0);
		p_comp.addEventListener(Events.ON_CLIENT_INFO, new EventListener<Event>(){
			public void onEvent(Event event) throws Exception {
				if (event instanceof ClientInfoEvent){
					ClientInfoEvent cie = (ClientInfoEvent) event; 
					UniLog.log(String.format("ClientInfoEvent: %dx%d %s offset:%d,%d", cie.getDesktopWidth(), cie.getDesktopHeight(), cie.getOrientation(), cie.getDesktopXOffset(),cie.getDesktopYOffset()));
					if (winWidth.get() == 0){ //obtain initial status
						Clients.evalJavaScript(String.format("if (typeof zkGetSidrStatus === \"function\"){ zkGetSidrStatus('"+p_comp.getId()+"');}",p_comp.getId()));
					}
					winWidth.set(Math.round((cie.getDesktopWidth())));
				}
			}
		});
		p_comp.addEventListener("onSidrOpen", new EventListener<Event>(){
			public void onEvent(Event event) throws Exception {
				UniLog.log(String.format("notifySidrStatus onSidrOpen"));
				if (!sessionHelper.isMobileDevice())
					sessionHelper.setRequestSidrAction("open");
			}
		});
		p_comp.addEventListener("onSidrClose", new EventListener<Event>(){
			public void onEvent(Event event) throws Exception {
				UniLog.log(String.format("notifySidrStatus onSidrClose"));
				if (!sessionHelper.isMobileDevice())
					sessionHelper.setRequestSidrAction("close");
			}
		});
	}
	*/
	
	public static void registerClientInfoEvent(final HtmlBasedComponent p_comp, final SessionHelper p_sessionHelper, final boolean p_adjustCompWidth, final int p_adjustCompWidthOffset){
		registerClientInfoEvent(p_comp, p_sessionHelper, p_adjustCompWidth, p_adjustCompWidthOffset, true);
	}
	/***
	 * register clientInfoEvent, for adjust component width/handle logout
	 * limitation: support single component only
	 * @param p_comp
	 * @param p_offsets
	 * @param p_isMobile
	 */
	public static void registerClientInfoEvent(final HtmlBasedComponent p_comp, final SessionHelper p_sessionHelper, final boolean p_adjustCompWidth, final int p_adjustCompWidthOffset, boolean p_jsIdleCtrl){
		/*
		if (p_sessionHelper.getSideMenuAutoHide()){
			p_comp.addEventListener(Events.ON_CLIENT_INFO, new EventListener(){
				public void onEvent(Event event) throws Exception {
					if (event instanceof ClientInfoEvent){
						ClientInfoEvent cie = (ClientInfoEvent) event; 
						if (p_sessionHelper.isMobileDevice()){
							autoAdjustWinWidthOne(p_comp, Math.round(cie.getDesktopWidth()));
						}
						else{
							autoAdjustWinWidthOne(p_comp, Math.round(cie.getDesktopWidth() * 0.98));
						}
					}
				}
			});
			return;
		}
		*/
		if (p_sessionHelper == null) {
			UniLog.log1("sessionhelper is null");
			return;
		}
		if (p_comp == null) {
			UniLog.log1("comp is null");
			return;
		}
		if (StringUtils.isBlank(p_comp.getId())){
			UniLog.log1("comp id is blank, skip register clientinfoevent");
			return;
		}
		
		//check is registered or not
		synchronized(p_comp) {
			if (StringUtils.equals((String)p_comp.getAttribute("registerClientInfoEvent.status"),"Y")){
				UniLog.log1("already registered");
				return;
			}
			p_comp.setAttribute("registerClientInfoEvent.status","Y");
		}
		
		final AtomicLong winWidth = new AtomicLong(0);
		final AtomicBoolean sidrOpenFlag = new AtomicBoolean(true); 
		if (p_sessionHelper.getSideMenuAutoHide()) {
			sidrOpenFlag.set(false);
		}
		p_comp.addEventListener(Events.ON_CLIENT_INFO, new EventListener(){
			public void onEvent(Event event) throws Exception {
				if (event instanceof ClientInfoEvent){
					ClientInfoEvent cie = (ClientInfoEvent) event; 
					//Window win = (Window) event.getTarget(); //it is null due to broadcast event, so required to use final comp
					UniLog.log(String.format("ClientInfoEvent: %dx%d %s offset:%d,%d %d %d %s", 
							cie.getDesktopWidth(), cie.getDesktopHeight(), cie.getOrientation(), cie.getDesktopXOffset(),cie.getDesktopYOffset()
							,cie.getScreenHeight()
							,cie.getScreenWidth()
							,cie.getOrientation()
							));
					p_sessionHelper.setScreenHeight(cie.getScreenHeight());
					p_sessionHelper.setScreenWidth(cie.getScreenWidth());
					p_sessionHelper.setDesktopHeight(cie.getDesktopHeight());
					p_sessionHelper.setDesktopWidth(cie.getDesktopWidth());
					p_sessionHelper.setLandScape("landscape".equals(cie.getOrientation()));
					
					if (winWidth.get() == 0){ //obtain initial status
						//Clients.evalJavaScript("if (typeof zkGetSidrStatus === \"function\"){ zkGetSidrStatus();}");
						Clients.evalJavaScript(String.format("if (typeof zkGetSidrStatus === \"function\"){ zkGetSidrStatus('"+p_comp.getId()+"');}",p_comp.getId()));
					}
					
					if (p_sessionHelper.isMobileDevice()){
						winWidth.set(Math.round((cie.getDesktopWidth())));
					}
					else{
						winWidth.set(Math.round((cie.getDesktopWidth() *0.98)));
					}
					int offset = sidrOpenFlag.get() ? -200 : 0;
					
					//special handle for fill full mode
					if (StringUtils.equals(getURLParamFromComp(p_comp,"fillscreen"),"full")) {
						offset = 0;
					}
					
//					if (p_adjustCompWidth /* && !p_sessionHelper.isMobile() */){
// change back to disable this for mobile device, need to investigate more , 2025/10/02 Donald 
					if(winWidth.get() > 1000) offset = -300;
					if (p_adjustCompWidth && !p_sessionHelper.isMobile() ){
						UniLog.log1("winWidth:%d, offset:%d, p_adjustCompWidthOffset:%d", winWidth.get(), offset, p_adjustCompWidthOffset);
						delayAutoAdjustWinWidthOne(p_comp, winWidth.get() + offset + p_adjustCompWidthOffset);
					}
				}
			}
		});
		p_comp.addEventListener("onSidrOpen", new EventListener<Event>(){
			public void onEvent(Event event) throws Exception {
				UniLog.log(String.format("onSidrOpen p_adjustCompWidth:" + p_adjustCompWidth + ",data:" + event.getData()));
				/*if (p_sessionHelper.getSideMenuAutoHide()){
					return;
				}*/
				boolean isAutoHide = p_sessionHelper.getSideMenuAutoHide();
				if (event.getData() != null) {
					try {
						JSONObject json = new JSONObject(event.getData().toString());
						isAutoHide = json.getBoolean("autoHide");
						/*if (isAutoHide != p_sessionHelper.getSideMenuAutoHide()) {
							p_sessionHelper.setSideMenuAutoHide(isAutoHide);
						}*/
						if (p_sessionHelper.getSideMenuAutoHide()) {
							if (isAutoHide == p_sessionHelper.getSideMenuAutoHideDefaultPin())
								p_sessionHelper.setSideMenuAutoHideDefaultPin(!isAutoHide);
						}
						setBrowserWinId(p_comp, json.getString("browserWinId"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				sidrOpenFlag.set(true);
				if (p_adjustCompWidth /* && !p_sessionHelper.isMobile() */){
					//autoAdjustWinWidthOne(p_comp, winWidth.get() - 200 + p_adjustCompWidthOffset);
					autoAdjustWinWidthOne(p_comp, ""+(winWidth.get() - ((isAutoHide || p_sessionHelper.isMobileDevice()) ? 0 : 200) + p_adjustCompWidthOffset)+"px");
				}
				SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
				if (!sessionHelper.isMobileDevice())
					sessionHelper.setRequestSidrAction("open");
			}
		});
		p_comp.addEventListener("onSidrClose", new EventListener<Event>(){
			public void onEvent(Event event) throws Exception {
				UniLog.log(String.format("onSidrClose p_adjustCompWidth:" + p_adjustCompWidth + ",data:" + event.getData()));
				/*if (p_sessionHelper.getSideMenuAutoHide()){
					return;
				}*/
				if (event.getData() != null) {
					try {
						JSONObject json = new JSONObject(event.getData().toString());
						setBrowserWinId(p_comp, json.getString("browserWinId"));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}	
				sidrOpenFlag.set(false);
				if (p_adjustCompWidth /* && !p_sessionHelper.isMobile() */){
					autoAdjustWinWidthOne(p_comp, ""+(winWidth.get() + p_adjustCompWidthOffset)+"px");
				}
				SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
				if (!sessionHelper.isMobileDevice())
					sessionHelper.setRequestSidrAction("close");
			}
		});
		if (p_sessionHelper.getAllowReportProblem()) {
			p_comp.addEventListener("onReportProblem", new EventListener<Event>(){
				@Override
				public void onEvent(Event event) throws Exception {
					SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
					//UniLog.log1("event:%s, data:%s, desktopId:%s", event, event.getData(), p_comp.getDesktop().getId());
					org.zkoss.json.JSONObject json = (org.zkoss.json.JSONObject)event.getData();
					UniLog.log1("event:%s jsonlen:%d desktopId:%s", event, json != null ? json.toString().length() : 0, p_comp.getDesktop().getId());
					json.put("desktopId", p_comp.getDesktop().getId());
					new ZkBiReportProblemDialog(sessionHelper, p_comp, (org.zkoss.json.JSONObject)event.getData());
				}
			});
		}
		
		//when user click logout button
		p_comp.addEventListener("onLogout", new EventListener<Event>(){
			public void onEvent(Event event) throws Exception {
				UniLog.log(String.format("onLogout"));
				/*
           		Messagebox.show("Are you sure to logout?", "Confirmation", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
           			        new org.zkoss.zk.ui.event.EventListener<Event>(){
           			            public void onEvent(Event e){
       			            		UniLog.logm(this,"event received: %s", e.getName());
           			            	if (StringUtils.equals("onOK", e.getName())){
           			            		//ZkUtil.sendSessionEvent(MapUtil.of("action","SILIENT_LOGOUT"));  //experimental. logout all window (server side eventqueue)
           			            		if (p_sessionHelper.getAllowJSBroadcastChannel()) {
           			            			ZkUtil.js("zkbiBc.send({action:'redirect',data:'logout.html?agent=%s&keeplt=N'},true);",p_sessionHelper.getAgent());  //logout all window (client side bc)
           			            		}
           			            		else {
           			            			Executions.sendRedirect(String.format("logout.html?agent=%s&keeplt=N", p_sessionHelper.getAgent()));  //logout current window only
           			            		}
           			            	}
           			            }
           			        }
           			    );
   			    */
				ZkBiMsgbox.show(ZkBiMsgbox.Type.question, p_sessionHelper.getLabel("Are you sure to logout?"), new String[] {"Ok", "Cancel"}, new ZkBiEventListener() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
						if (btn.getName().equals("Ok")) {
							if (p_sessionHelper.getAllowJSBroadcastChannel()) {
								ZkUtil.js("zkbiBc.send({action:'redirect',data:'logout.html?agent=%s&keeplt=N%s'},true);",p_sessionHelper.getAgent(),p_sessionHelper.getLargeFlag()?"&large=Y":"");  //logout all window (client side bc)
							}
							else {
								Executions.sendRedirect(String.format("logout.html?agent=%s&keeplt=N%s", p_sessionHelper.getAgent(), p_sessionHelper.getLargeFlag()?"&large=Y":""));  //logout current window only
							}
						}
						
				}});
			}
		});
		p_comp.addEventListener("onStartup", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("onStartup called. event:%s data:%s", event, event.getData());
				//nothing to do. reserved for future use
				
				try {
					JSONObject json = new JSONObject(event.getData().toString());
					String deviceType = json.getString("devicetype");
					
					//andrew230609 set the device feature based on devicetype first. later can provide more info via native application
					if (StringUtils.equalsAny(deviceType, "android","ios")) {
						p_sessionHelper.setDeviceFeature(SessionHelper.DEVICE_FEATURE.SCANNER, SessionHelper.DEVICE_FEATURE_STATE.TRUE);
						p_sessionHelper.setDeviceFeature(SessionHelper.DEVICE_FEATURE.TAKE_PHOTO, SessionHelper.DEVICE_FEATURE_STATE.TRUE);
					}
					else {
						p_sessionHelper.setDeviceFeature(SessionHelper.DEVICE_FEATURE.SCANNER, SessionHelper.DEVICE_FEATURE_STATE.FALSE);
						p_sessionHelper.setDeviceFeature(SessionHelper.DEVICE_FEATURE.TAKE_PHOTO, SessionHelper.DEVICE_FEATURE_STATE.FALSE);
					}
				}
				catch(Exception ex) {
					UniLog.log1("error:" + ex.getMessage());
					//ex.printStackTrace();
				}
			}
			
		});
		/*
		if (p_sessionHelper.getAllowJSBroadcastChannel()) {  //andrew200623: moved to commonInit.jsp
			ZkUtil.js("zkbiBc.init()");
		}
		*/
		if (p_sessionHelper.getAllowJSIdleCtrl() && p_jsIdleCtrl) {
			ZkUtil.js("zkbiIdleCtrl.init(%d,%d,%s,%s)",
	   		    p_sessionHelper.getIdleCtrlMaxIdle(),
	   		    p_sessionHelper.getIdleCtrlInterval(),
	   			String.format("function(){ zkbiBc.send({action:'redirect',data:'logout.html?agent=%s&keeplt=Y%s'},true); }", p_sessionHelper.getAgent(), p_sessionHelper.getLargeFlag()?"&large=Y":""),
	   			p_sessionHelper.getAllowJSBroadcastChannel() ? "function(){ zkbiBc.send({action:'setIdleCnt',data:0}, false); }" : "null"
				);
		}

		
		
		
		/*
		//exprimental: idle and auto logout. 
		//issue: front window is active, back window is idle - will kill the front window
		//ZkUtil.js("zkbiIdle.setDebug(true); zkbiIdle.init(20000, 1000, function() { console.log('idle action'); zkbiSend('onIdleAction'); });");
		//ZkUtil.js("zkbiIdle.setDebug(true); zkbiIdle.init(3600000, 60000, function() { console.log('idle action'); zkbiSend('onIdleAction'); });");  //andrew200618:moved to jsp
		p_comp.addEventListener("onIdleAction", new EventListener() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("got event:%s %s", event.getName(), event.getData());
				//TODO prompt for keepalive
				//ZkUtil.sendSessionEvent(MapUtil.of("action","SILIENT_LOGOUT", "data", event.getData()));
			}
		});
		*/
		
		/*
		//experimental: obsoleted. replaced by client side broadcast channel
		ZkUtil.receiveSessionEvent(new EventListener(){
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("got event:%s data:%s", event.getName(), event.getData());
				if (event.getData() instanceof Map){
					String action = MapUtil.getString(event.getData(), "action");
					if (StringUtils.equalsAny(action,"SILIENT_LOGOUT")) {
            			Clients.confirmClose(null);
  			           	Executions.sendRedirect("logout.html?agent=" + p_sessionHelper.getAgent());
  			           	return;
					}
				}
		}});	
		*/
		
		
		//andrew220601 hook showNotifyMsg to every page
		if (((ZkSessionHelper)p_sessionHelper).getAllowNotifyMsg()) {
			p_comp.addEventListener("onBuildNotifyMsg", new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					UniLog.log1("got event:%s data:%s", event, event.getData());
					((ZkSessionHelper)p_sessionHelper).showNotifyMsg();
				}
			});
			ZkUtil.delayPostEvent("onBuildNotifyMsg", p_comp, null, 10);
		}
		
	}
	
	/***
	 * set browserwin id to component attribute
	 * @param p_comp
	 * @param p_id
	 */
	private static void setBrowserWinId(Component p_comp, String p_id) {
		if (!StringUtils.startsWith(p_id,"ZKBIBWID")) {
			UniLog.log1("invalid browserWinId:%s", p_id);
			return;
		}
		if (p_comp == null) {
			UniLog.log1("comp is null");
			return;
		}
		
		//guarantee event only fire one time
		synchronized(p_comp) {  
			//keep synchronized block simple
			if (StringUtils.isNotBlank((String)p_comp.getAttribute("ZKBIBWID"))){
				UniLog.log1("skip duplicate call");
				return;
			}
			p_comp.setAttribute("ZKBIBWID", p_id);
		}
		
		//send the event to target
		Events.sendEvent("onSetBrowserWinId", p_comp, p_id);
		
	}
	
	/***
	 * get webcontent real path
	 * e.g. images -> C:\eclipse_dev\.metadata\.plugins\org.eclipse.wst.server.core\tmp0\wtpwebapps\pmsdemo\images
	 * @param p_path
	 * @return
	 */
	/*
	public static String getWebContentRealPath(String p_path, boolean p_withSeparator){
		return getWebContentRealPath((HttpServletRequest)Executions.getCurrent().getNativeRequest(), p_path, p_withSeparator);
	}
	public static String getWebContentRealPath(HttpServletRequest request, String p_path, boolean p_withSeparator){
		return getWebContentRealPath(request.getSession(), p_path, p_withSeparator);
	}
	public static String getWebContentRealPath(HttpSession session, String p_path, boolean p_withSeparator){
		return getWebContentRealPath(session.getServletContext(), p_path, p_withSeparator);
	}
	public static String getWebContentRealPath(ServletContext svc, String p_path, boolean p_withSeparator){
		return svc.getRealPath(p_path) + (p_withSeparator ? File.separator :"");
	}
	*/
	
	/***
	 * get class root path
	 * 
	 * e.g. C:/eclipse_dev/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/pmsdemo/WEB-INF/classes/
	 */
//	public static String getClassRootPath() {
//		String rootPath = ZkUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//		if (fDebug.get()) UniLog.log1("rootPath:1:"+ rootPath);
//		
//		//remove class name
//	    //remark: getProtectionDomain output is depend on running env. it may contain class name
//		//C:/eclipse_dev/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/pmsdemo/WEB-INF/classes/com/uniinformation/utils/ZkUtil.class
//	    //C:/eclipse_dev/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/pmsdemo/WEB-INF/classes/
//		rootPath = StringUtils.removeEnd(rootPath, StringUtils.replaceChars(ZkUtil.class.getName(),'.','/') +".class");
//		
//		//append slash
//		if (!StringUtils.endsWith(rootPath, "/")) {
//			rootPath = rootPath +"/";
//		}
//		if (fDebug.get()) UniLog.log1("rootPath:2:"+ rootPath);
//		return rootPath;
//	}

	public static Window newPopupWindow(String p_title,Component parentComp){
		return(newPopupWindow(p_title,parentComp,false));
	}
	/***
	 * create a popup window
	 * @param p_title
	 * @param parentComp
	 * @param p_detachWhenClose - by default, it do not detach the window when click close window button
	 * @return
	 */
	public static Window newPopupWindow(String p_title,Component parentComp, final boolean p_detachWhenClose){
		return(newPopupWindow(p_title,parentComp,false,null));
	}
	public static Window newPopupWindow(String p_title,Component parentComp, final boolean p_detachWhenClose,final PopupWindowAction p_action){
		try{
			final Window pWin = new Window();
			pWin.setBorder("normal");
			pWin.setTitle(p_title);
			pWin.setWidth("300px");  //should not hard code window size
			pWin.setHeight("200px");
			pWin.setClosable(true);
			if (parentComp == null){
			}
			else{
				pWin.setParent(parentComp);
			}
			pWin.addEventListener(Events.ON_CLOSE, new EventListener<Event>(){
				public void onEvent(Event event) throws Exception {
					UniLog.log("Popup Closed");
					if(p_action != null) {
						p_action.onClose();
					}
					event.getTarget().setVisible(false);
					event.stopPropagation();
					if (p_detachWhenClose){
						pWin.detach();
					}
				}});
			return(pWin);
		}
		catch(Exception ex){
			UniLog.log(ex);
			return(null);
		}
	}
	/***
	 * 221116 better use ZkBiMsgbox, messageboxdlg has offscreen bug
	 * @param title
	 * @param child
	 * @param buttons
	 * @param parent
	 * @param eventListener
	 * @return
	 */
    public static MessageboxDlg buildMessageboxDlg(String title, Component child, Messagebox.Button[] buttons, Component parent, EventListener<Messagebox.ClickEvent> eventListener) {
    	return buildMessageboxDlg(title, child, buttons, null, parent, eventListener);
    }
    /***
	 * 221116 better use ZkBiMsgbox, messageboxdlg has offscreen bug
     * @param title
     * @param child
     * @param buttons
     * @param buttonsLabel
     * @param parent
     * @param eventListener
     * @return
     */
    public static MessageboxDlg buildMessageboxDlg(String title, Component child, Messagebox.Button[] buttons, String[] buttonsLabel, Component parent, EventListener<Messagebox.ClickEvent> eventListener) {
		UniLog.log1("parent:%s", parent);
		MessageboxDlg dlg = new MessageboxDlg();
		dlg.setTitle(title);
		dlg.setBorder("normal");
		dlg.setClosable(true);
    	dlg.setParent(parent);
    	dlg.appendChild(child);
    	dlg.setSclass("zkbi-messageboxdlg");
    	Separator separator = new Separator();
    	separator.setSpacing("15px");
    	dlg.appendChild(separator);
    	Hbox hbox = new Hbox();
    	hbox.setId("buttons");
    	hbox.setStyle("margin-left:auto; margin-right:auto; padding:0px;");
    	hbox.setAttribute("button.sclass", "z-messagebox-button");
    	dlg.appendChild(hbox);
    	dlg.setButtons(buttons, buttonsLabel);
    	dlg.setEventListener(eventListener);
    	dlg.setSizable(true);
    	dlg.setMaximizable(true);
    	return dlg;
    }
    /***
     * 221116 better use ZkBiMsgbox, messageboxdlg has offscreen bug
     * @param title
     * @param child
     * @param buttons
     * @param parent
     * @param eventListener
     * @return
     */
    public static MessageboxDlg buildSimpleMessageboxDlg(String title, final HtmlBasedComponent child, Messagebox.Button[] buttons, Component parent, EventListener<Messagebox.ClickEvent> eventListener) {
    	return buildSimpleMessageboxDlg(title, child, buttons, null, parent, eventListener);
    }
    /***
     * 221116 better use ZkBiMsgbox, messageboxdlg has offscreen bug
     * @param title
     * @param child
     * @param buttons
     * @param buttonsLabel
     * @param parent
     * @param eventListener
     * @return
     */
    public static MessageboxDlg buildSimpleMessageboxDlg(String title, final HtmlBasedComponent child, Messagebox.Button[] buttons, String[] buttonsLabel, Component parent, EventListener<Messagebox.ClickEvent> eventListener) {
		UniLog.log1("parent:%s", parent);
		MessageboxDlg dlg = new MessageboxDlg();
		dlg.setTitle(title);
		dlg.setBorder("normal");
		dlg.setClosable(true);
    	dlg.setParent(parent);
		dlg.setSclass("zkbi-messageboxdlg");
    	child.setVflex("1");
    	child.setHflex("1");
    	final Div div = new Div();
    	div.setId("buttons");
    	div.setSclass("zkbi-messagebox-button-container");
    	div.setStyle("display:flex;display:-webkit-flex;"
    			+ "flex-wrap:wrap;-webkit-wrap:wrap;"
    			+ "justify-content:center;-webkit-justify-content:center;"
    			+ "padding:0 5px 10px 0;");
    	div.setAttribute("button.sclass", "zkbi-messagebox-button");
    	dlg.appendChild(new Vbox() {{
    		appendChild(child);
    		appendChild(div);
    		setHflex("1");
    		setVflex("1");
    	}});
    	dlg.setButtons(buttons, buttonsLabel);
    	dlg.setEventListener(eventListener);
    	return dlg;
    }
    /***
     * 221116 better use ZkBiMsgbox, messageboxdlg has offscreen bug
     * @param title
     * @param child
     * @param buttons
     * @param buttonsLabel
     * @param parent
     * @param eventListener
     * @return
     */
    public static MessageboxDlg buildNoButtonsMessageboxDlg(String title, final HtmlBasedComponent child, Messagebox.Button[] buttons, String[] buttonsLabel, Component parent, EventListener<Messagebox.ClickEvent> eventListener) {
		MessageboxDlg dlg = new MessageboxDlg();
		dlg.setTitle(title);
		dlg.setBorder("normal");
		dlg.setClosable(true);
    	dlg.setParent(parent);
    	child.setVflex("1");
    	child.setHflex("1");
    	/*final Div div = new Div();
    	div.setId("buttons");
    	div.setStyle("display:flex;display:-webkit-flex;"
    			+ "flex-wrap:wrap;-webkit-wrap:wrap;"
    			+ "justify-content:center;-webkit-justify-content:center;"
    			+ "padding:0 5px 10px 0;");
    	div.setAttribute("button.sclass", "zkbi-messagebox-button");*/
    	dlg.appendChild(new Vbox() {{
    		appendChild(child);
    		//appendChild(div);
    		setHflex("1");
    		setVflex("1");
    	}});
    	if (buttons != null)
    		dlg.setButtons(buttons, buttonsLabel);
    	dlg.setEventListener(eventListener);
    	return dlg;
    }
	/***
	 * move dialog to screen(center), if dialog off screen
	 * @param win
	 */
    public static void adjustDialogToOnScreen(Window win) {
    	ZkUtil.delayJs(win, null, 50, "var e = $('#%s');var rect = e[0].getBoundingClientRect();"
    			+ "if (rect.x < 0 || rect.x + rect.width > window.innerWidth || rect.y < 0 || rect.y + rect.height > window.innerHeight) {"
    			+ "	e.css({'left': (rect.width > window.innerWidth) ? '0': 'calc(50%% - '+ rect.width/2 +'px)', "
    			+ "			'top': (rect.height > window.innerHeight) ? '0': 'calc(50%% - '+ rect.height/2 +'px)'});"
    			+ "}", win.getUuid());
    }
	/***
	 * move dialog to center of screen
	 * @param win
	 */
    public static void centerDialog(Window win) {
    	ZkUtil.delayJs(win, null, 50, "var e = $('#%s');var rect = e[0].getBoundingClientRect();"
    			+ "	e.css({'left': (rect.width > window.innerWidth) ? '0': 'calc(50%% - '+ rect.width/2 +'px)', "
    			+ "			'top': (rect.height > window.innerHeight) ? '0': 'calc(50%% - '+ rect.height/2 +'px)'});"
    			, win.getUuid());
    }
    public static void errMsg(String p_format, Object...p_args){
    	ZkBiMsgbox.show(ZkBiMsgbox.Type.error, StringUtils.isBlank(p_format) ? "Error" : String.format(p_format, p_args));
    	/*
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	if (StringUtils.isBlank(p_format)){
    		Messagebox.show("Error", "System Message " + sdf.format(new Date()), Messagebox.OK, Messagebox.ERROR);	
    	}
    	else{
    		Messagebox.show((p_args == null || p_args.length == 0) ? p_format : String.format(p_format, p_args), "System Message " + sdf.format(new Date()), Messagebox.OK, Messagebox.ERROR);	
    	}
    	*/
    }
    public static void msg(String p_format, Object...p_args){
    	normMsg(p_format, p_args);
    }
    public static void normMsg(String p_format, Object...p_args){
    	if (fDebug.get()) UniLog.log1(p_format,p_args);
    	ZkBiMsgbox.show(ZkBiMsgbox.Type.info, StringUtils.isBlank(p_format) ? "Information" : String.format(p_format, p_args));
    	/*
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	if (StringUtils.isBlank(p_format)){
    		Messagebox.show("", "System Message " + sdf.format(new Date()), Messagebox.OK, Messagebox.INFORMATION);	
    	}
    	else{
    		Messagebox.show((p_args == null || p_args.length == 0) ? p_format : String.format(p_format, p_args), "System Message " + sdf.format(new Date()), Messagebox.OK, Messagebox.INFORMATION);	
    	}
    	*/
    }
    public static void warnMsg(String p_format, Object...p_args){
    	if (fDebug.get()) UniLog.log1(p_format,p_args);
    	ZkBiMsgbox.show(ZkBiMsgbox.Type.warning, StringUtils.isBlank(p_format) ? "Warning" : String.format(p_format, p_args));
	    /*
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    	if (StringUtils.isBlank(p_format)){
    		Messagebox.show("Warning", "System Message " + sdf.format(new Date()), Messagebox.OK, Messagebox.EXCLAMATION);	
    	}
    	else{
    		Messagebox.show((p_args == null || p_args.length == 0) ? p_format : String.format(p_format, p_args), "System Message " + sdf.format(new Date()), Messagebox.OK, Messagebox.EXCLAMATION);	
    	}
    	*/
    }
    /***
     * @param p_comp - e.g. textbox
     * @param p_color - red/green/yellow/black null/blank remove style
     */
    public static void setFontColor(HtmlBasedComponent p_comp, String p_color){
    	if (StringUtils.isBlank(p_comp.getSclass()) && StringUtils.isBlank(p_color)){
    		//UniLog.logm(null, "ignore blank");
    		return;
    	}
   		StringBuilder newSclass = new StringBuilder();
   		
   		//remove old zkbi-fontcolor sclass
    	if (StringUtils.isNotBlank(p_comp.getSclass())){
    		String sclassList[] = StringUtils.split(p_comp.getSclass(), ", ");
    		for (String sclass : sclassList){
    			if (!StringUtils.containsIgnoreCase(sclass, "zkbi-fontcolor-")){
    				newSclass.append(sclass);
    				newSclass.append(" ");
    			}
    		}
    	}
    	
    	//append new fontcolor
    	if (StringUtils.isNotBlank(p_color)){
    		newSclass.append("zkbi-fontcolor-" + p_color.toLowerCase().trim());
    	}
    	
    	//set fontcolor
   		p_comp.setSclass(newSclass.toString());
    }
    /***
     * display info message
     * @param p_format
     * @param p_args
     */
    public static void showMsg(String p_format, Object...p_args){
    	if (fDebug.get()) UniLog.log1(p_format,p_args);
       	//Clients.evalJavaScript("$.notify(\""+String.format(p_format, p_args)+"\", { className: \"info\", globalPosition:\"bottom right\", autoHideDelay: 5000 })");
       	Clients.evalJavaScript("$.notify(\""+String.format(p_format, p_args)+"\", { className: \"info\", globalPosition:\"bottom right\", autoHideDelay: 5000, clickToHide: true })");
    }
    public static void showWarnMsg(String p_format, Object...p_args){
    	if (fDebug.get()) UniLog.log1(p_format, p_args);
       	//Clients.evalJavaScript("$.notify(\""+String.format(p_format, p_args)+"\", { className: \"warn\", globalPosition:\"bottom right\", autoHideDelay: 5000 })");
       	Clients.evalJavaScript("$.notify(\""+String.format(p_format, p_args)+"\", { className: \"warn\", globalPosition:\"bottom right\", autoHideDelay: 5000, clickToHide: true })");
    }
    public static void showErrMsg(String p_format, Object...p_args){
    	if (fDebug.get()) UniLog.log1(p_format, p_args);
       	//Clients.evalJavaScript("$.notify(\""+String.format(p_format, p_args)+"\", { className: \"error\", globalPosition:\"bottom right\", autoHideDelay: 5000 })");
       	Clients.evalJavaScript("$.notify(\""+String.format(p_format, p_args)+"\", { className: \"error\", globalPosition:\"bottom right\", autoHideDelay: 5000, clickToHide: true })");
    }
    public static void showErrMsg(Component comp, String p_format, Object...p_args){
    	if (fDebug.get()) UniLog.log1(p_format, p_args);
       	//Clients.evalJavaScript("$('#"+comp.getUuid()+"').notify(\""+String.format(p_format, p_args)+"\", { className: \"error\", elementPosition:\"bottom right\", arrowShow: false, autoHideDelay: 5000 })");
       	Clients.evalJavaScript("$('#"+comp.getUuid()+"').notify(\""+String.format(p_format, p_args)+"\", { className: \"error\", elementPosition:\"bottom right\", arrowShow: false, autoHideDelay: 5000, clickToHide: true })");
    }
    public static void showWarnMsg(Component comp, String p_format, Object...p_args){
    	if (fDebug.get()) UniLog.log1(p_format, p_args);
       	//Clients.evalJavaScript("$('#"+comp.getUuid()+"').notify(\""+String.format(p_format, p_args)+"\", { className: \"warn\", elementPosition:\"bottom right\", arrowShow: false, autoHideDelay: 5000 })");
       	Clients.evalJavaScript("$('#"+comp.getUuid()+"').notify(\""+String.format(p_format, p_args)+"\", { className: \"warn\", elementPosition:\"bottom right\", arrowShow: false, autoHideDelay: 5000, clickToHide: true })");
    }
    public static void showMsg(Component comp, String p_format, Object...p_args){
    	if (fDebug.get()) UniLog.log1(p_format, p_args);
       	//Clients.evalJavaScript("$('#"+comp.getUuid()+"').notify(\""+String.format(p_format, p_args)+"\", { className: \"info\", elementPosition:\"bottom right\", arrowShow: false, autoHideDelay: 5000 })");
       	Clients.evalJavaScript("$('#"+comp.getUuid()+"').notify(\""+String.format(p_format, p_args)+"\", { className: \"info\", elementPosition:\"bottom right\", arrowShow: false, autoHideDelay: 5000, clickToHide: true })");
    }
    /***
     * handy method to run javascript
     * @param p_fmt
     * @param p_args
     */
    public static void js(String p_fmt, Object...p_args){
       if (fDebug.get()) UniLog.log1(p_fmt, p_args);
	   if (p_args == null || p_args.length == 0){
		   //UniLog.log1("DEBUG:" + String.format(p_fmt));
		   Clients.evalJavaScript(p_fmt);
	   }
	   else{
		   //UniLog.log1("DEBUG:" + String.format(p_fmt, p_args));
		   Clients.evalJavaScript(String.format(p_fmt, p_args));
	   }
    }
    
    /***
     * run javascript with defer. It's useful waiting for UI component ready.
     * useful 
     * @param p_comt
     * @param p_fmt
     * @param p_args
     */
    public static void delayJs(Component p_comp, String p_busyMsg, final String p_fmt, final Object...p_args) {
		new ZkBiAbstractLongOp(p_comp, p_busyMsg){
			@Override
			public ReturnMsg longOp() {
				js(p_fmt, p_args);
				return null;
			}
		};
    }
    
    public static void delayJs(Component p_comp, String p_busyMsg, int p_delay, final String p_fmt, final Object...p_args) {
		new ZkBiAbstractLongOp(p_comp, p_busyMsg, p_delay){
			@Override
			public ReturnMsg longOp() {
				js(p_fmt, p_args);
				return null;
			}
		};
    }

    public static Timer delayJs(Timer oldTimer, Component p_comp, String p_busyMsg, int p_delay, final String p_fmt, final Object...p_args) {
		return timerEvent(oldTimer, p_comp, p_busyMsg, p_delay, () -> {
			UniLog.log1("delayJs:%s", p_fmt);
			ZkUtil.js(p_fmt, p_args);
		});
    }
    
    
    
    
    
    /***
     * loop all first level component, return first window/div
     * the logic is similar to js getMainComp()
     * @return
     */
    public static Component getMainComp(){
    	for (Component comp : Executions.getCurrent().getDesktop().getFirstPage().getRoots()){
    		if (comp instanceof Window){
    			return(comp);
    		}
		}
    	for (Component comp : Executions.getCurrent().getDesktop().getFirstPage().getRoots()){
    		if (comp instanceof Div){
    			return(comp);
    		}
		}
    	return(null);
    }
    public static Page getMainCompPage() {
    	Component mainComp = getMainComp();
    	if (mainComp == null) {
    		return null;
    	}
    	return mainComp.getPage();
    }
    
    public static boolean downloadFileFromFiling(SessionHelper p_sh, String p_filingKey, String p_fileName){
    	//return downloadFileFromFiling(p_sh, p_filingKey, p_fileName);  //230926 method calling itself, stack overflow bug!
    	return downloadFileFromFiling(p_sh, null, p_filingKey, p_fileName);
    }

    /***
     * download file
     * @param p_sh - sessionHelper
     * @param p_mimeType - Mime Type
     * @param p_filingKey - FilingUtil key
     * @param p_fileName null, construct filename based on key 
     */
    public static boolean downloadFileFromFiling(SessionHelper p_sh, String p_mimeType, String p_filingKey, String p_fileName){
    	if (StringUtils.isNotBlank(p_filingKey)){
    		ByteArrayOutputStream baos = new ByteArrayOutputStream();
    		try{
    			FilingUtilObject fuo;
    			if ((fuo = FilingUtil.getFile(p_sh.getAgent(), null, p_filingKey, baos)) != null){
    				Map<String,String> fileTypeMap;
    				if (StringUtils.isNotBlank(p_mimeType))
    					fileTypeMap = MapUtil.of("mimeType", p_mimeType, "ext", FilingUtil.guessFileExt(p_mimeType));
    				else
    					fileTypeMap = FilingUtil.guessFileType(baos.toByteArray());
    				UniLog.log1("key:%s fileTypeMap:%s", p_filingKey, fileTypeMap);

    				//define fileName
    				String fileName = p_fileName;
    				if (StringUtils.isBlank(fileName))
    					fileName = fuo.name;
    				if (StringUtils.isBlank(fileName))
    					fileName = p_filingKey;

    				//append ext if required
    				if (!fileName.matches("^.*[.][a-zA-Z]{3,6}$")){
    					fileName = fileName + fileTypeMap.get("ext");
    				}

    				Filedownload.save(baos.toByteArray(), fileTypeMap.get("mimeType"), fileName);
    				return true;
    			}
    			else{
    				UniLog.log1("file not fount %s", p_filingKey);
    			}
    		}
    		catch(Exception ex){
    			ex.printStackTrace();
    		}
    	}
    	else{
    		UniLog.log1("filingKey is blank");
		}
    	return false;
    }
    
    /***
     * add sclass to component
     * @param p_comp
     * @param p_sclass
     * @return
     */
    public static boolean addSclass(HtmlBasedComponent p_comp, String p_sclass){
    	try{
    		String orgSclass = p_comp.getSclass();
    		String newSclass = StringOptUtil.addOpt(orgSclass, " ", p_sclass);
    		if (!StringUtils.equals(orgSclass, newSclass)){
    			p_comp.setSclass(newSclass);
    		}
   			return true;
    	}
    	catch(Exception ex){
    		ex.printStackTrace();
    		return false;
    	}
    }
    /***
     * remove sclass from component
     * @param p_comp
     * @param p_sclass
     * @return
     */
    public static boolean removeSclass(HtmlBasedComponent p_comp, String p_sclass){
    	try{
    		String orgSclass = p_comp.getSclass();
    		String newSclass = StringOptUtil.removeOpt(orgSclass, " ", p_sclass);
    		if (!StringUtils.equals(orgSclass, newSclass)){
    			p_comp.setSclass(newSclass);
    		}
    		return true;
    	}
    	catch(Exception ex){
    		ex.printStackTrace();
    		return false;
    	}
    }
    
    /***
     * check component has specific sclass
     * @param p_comp
     * @param p_sclass
     * @return
     */
    public static boolean hasSclass(HtmlBasedComponent p_comp, String p_sclass) {
    	try {
    		if (p_comp == null || StringUtils.isBlank(p_sclass)) {
    			UniLog.log1("comp or sclass is null");
    			return false;
    		}
    		String compSclass = p_comp.getSclass();
    		if (StringUtils.isBlank(compSclass)) {
    			return false;
    		}
    		return ArrayUtils.contains(StringUtils.split(compSclass, " "), p_sclass);
    	}
    	catch(Exception ex) {
    		UniLog.log1("error:"+ex.getMessage());
    		return false;
    	}
    }
    
    public static boolean matchesSelector(Component comp, String selector, Predicate<Component> cb) {
   		if (comp == null) {
   			UniLog.log1("comp is null");
   			return false;
   		}
   		if (StringUtils.isBlank(selector))
   			return cb != null && cb.test(comp);
   		else {
   	        Matcher matcher = Pattern.compile("([a-zA-Z_][\\w-]*|#[a-zA-Z_][\\w-]*|\\.[a-zA-Z_][\\w-]*)").matcher(selector);
   			List<String> parts = new ArrayList<>();
   	        while (matcher.find())
   	            parts.add(matcher.group());
   	        if (parts.isEmpty())
   	        	return false;
   	        return parts.stream().allMatch(part -> {
   	        	if (part.startsWith("."))
   	        		return comp instanceof HtmlBasedComponent ? hasSclass((HtmlBasedComponent)comp, part.substring(1)) : false;
	          	else if (part.startsWith("#"))
	            	return StringUtils.equals(part.substring(1), comp.getId());
	          	else
	            	return StringUtils.equalsIgnoreCase(comp.getClass().getSimpleName(), part);
   	        }) && (cb == null || cb.test(comp));
   		}
    }

    public static boolean matchesSelector(Component comp, String selector) {
    	return matchesSelector(comp, selector, null);
    }

    public static Component closestComponent(Component comp, String selector, Predicate<Component> cb) {
    	for (; comp != null; comp = comp.getParent()) {
            if (matchesSelector(comp, selector, cb))
                return comp;
    	}
        return null;
    }

    public static Component closestComponent(Component comp, String selector) {
    	return closestComponent(comp, selector, null);
    }

	public static void showPdfDialog(final Component parentComp, final SessionHelper sessionHelper, final byte[] pdfData, final String downloadFileName) {
		showPdfDialog(parentComp, sessionHelper, pdfData, downloadFileName, false);
	}
    
    /***
     * show pdf dialog
     * @param parentComp
     * @param sessionHelper
     * @param pdfData
     * @param downloadFileName
     * @param needDownloadRename
     * @return
     */
	public static void showPdfDialog(final Component parentComp, final SessionHelper sessionHelper, final byte[] pdfData, final String downloadFileName, boolean needDownloadRename) {
		UniLog.log1("parentComp:%s", parentComp);
		final Div div = new Div();
		div.setId("pdf_container");
		div.setSclass("zkbi-pdf-container");
		//div.setWidth("100%");
		//div.setHeight("calc(100% - 45px)");
		final MessageboxDlg dlg = ZkUtil.buildSimpleMessageboxDlg(sessionHelper.getLabel("Print preview"), 
			div,
			new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.YES, Messagebox.Button.CANCEL}, 
			new String[]{sessionHelper.getBtLabel("Download"), sessionHelper.getBtLabel("Print"), sessionHelper.getBtLabel("Close")},
			parentComp,
			new EventListener<Messagebox.ClickEvent>(){
				@Override
				public void onEvent(ClickEvent event) throws Exception {
					if (event.getButton() == null)
						return;
					switch (event.getButton()) {
					case OK:
						UniLog.log1("click download button needDownloadRename:%b", needDownloadRename);
						if (needDownloadRename)
							downloadFileByRenameDlg(sessionHelper, downloadFileName, "application/pdf", pdfData, null);
						else
							Filedownload.save(pdfData, "application/pdf", downloadFileName);
						event.stopPropagation();
						break;
					case YES:
						UniLog.log("click print button");
						ZkUtil.printFromStream(new ByteArrayInputStream(pdfData), "application/pdf", sessionHelper);
						event.stopPropagation();
						break;
					default:
						UniLog.log("click close button");
						//Clients.evalJavaScript("$(document.documentElement).css('overflow', 'auto')");
						Clients.confirmClose(null);
						break;
					}
				}
			}
		);
		dlg.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				Clients.confirmClose(null);
			}
		});
		/*dlg.addEventListener("onCloseDialog", new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log("call onCloseDialog " + event);
				Events.sendEvent(Events.ON_CANCEL, dlg, null);
			}
		});*/
		dlg.setId("pdf_msgdlg");
		/*dlg.setStyle("padding:0px;");
		dlg.setBorder("none");
		dlg.setClosable(false);*/
		dlg.setMaximizable(!sessionHelper.isMobile());
		dlg.setSizable(!sessionHelper.isMobile());
		dlg.setWidth(sessionHelper.isMobile() ? "100%" : "90%");
		dlg.setHeight(sessionHelper.isMobile() ? "100%" : "90%");
		dlg.doHighlighted();

		String downloadLink = Sessions.getCurrent().getWebApp().getServletContext().getContextPath() + "/" + 
				ZkUtil.getDownloadLinkFromStream(new ByteArrayInputStream(pdfData),
						"application/pdf", 
						sessionHelper, 
						"HealthQnr_stream",  //stream key
						"HealthQnr_mimetype",  //mime key
						false);
		Clients.confirmClose("Are you sure to close the application?");
		/*Clients.evalJavaScript(String.format("embedPdfObject('%s','%s','%s');"
				//+ "$(document.documentElement).css('overflow', 'hidden');"
				+ "jq('$pdf_msgdlg').css({'position':'fixed','left':'0','top':'0'});"
				, downloadLink, "pdf_container","pdf_msgdlg"));*/
		js("embedPdfObject('%s','%s');", downloadLink, "pdf_container");
		if (sessionHelper.isMobile())
			ZkUtil.delayJs(dlg, null, 50, "jq('$pdf_msgdlg').css({'position':'fixed','left':'0','top':'0'});");
	}
	
	public static ZkBiMsgbox downloadFileByRenameDlg(SessionHelper sessionHelper, String downloadFileName, String mimeType, byte[] pdfData, Component compContent) throws Exception {
		if (compContent == null) {
			Hlayout hl = new Hlayout();
			Textbox tb = new Textbox(downloadFileName);
			tb.setId("tbInput");
			tb.setWidth("350px");
			hl.appendChild(new Space() {{ setHflex("1"); }});
			hl.appendChild(new Label(sessionHelper.getLabel("Export file name:")));
			hl.appendChild(tb);
			hl.appendChild(new Space() {{ setHflex("1"); }});
			compContent = hl;
		}
		Textbox tb = getFellowWithNullId(compContent, "tbInput");
		return new ZkBiMsgbox(sessionHelper).setContent(compContent).setButtons(Arrays.asList("Ok", "Cancel").stream().map(s -> sessionHelper.getBtLabel(s)).toArray(String[]::new)).setEventListener(new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
				if (btn.getIdx() == 0)
					Filedownload.save(pdfData, mimeType, StringUtils.defaultIfBlank(tb.getText(), downloadFileName));
			}
		}).build().appendStyle("width:500px").setVboxStyle("margin:10px 0 10px 0").doModal();
	}
	
	public static void clickBatchCancel(Button p_targetButton) {
		Button btBatchCancel = (Button) p_targetButton.getParent().query("#" + p_targetButton.getId() + "BatchCancel");
		if (btBatchCancel != null) {
			Events.echoEvent(Events.ON_CLICK, btBatchCancel, null);
		}
		else {
			UniLog.log1("BatchCancel button not found");
		}
	}
	public static void setupBatchModeButton(final Button p_targetButton, final Button p_batchModeButton) {
		setupBatchModeButton(p_targetButton, p_batchModeButton, null);
	}
	/***
	 * convert a normal button to batch mode button.
	 * @param p_targetButton - target button
	 * @param p_batchModeButton - optional. required BATCH_MODE_STATUS attribute for identify batchMode status
	 */
	public static void setupBatchModeButton(final Button p_targetButton, final Button p_batchModeButton, SessionHelper p_sh) {
		UniLog.log1("p_targetButton:%s", p_targetButton);
		
		//validation
		if (p_targetButton == null) {
			UniLog.log1("targetButton is null, setup abort");
			return;
		}
		
		//prevent re-setup
		if (StringUtils.equals((String)p_targetButton.getAttribute("IS_BATCH_MODE_BUTTON"),"Y")){
			UniLog.log1("button cannot apply batch mode twice, setup abort");
			return;
		}
		p_targetButton.setAttribute("IS_BATCH_MODE_BUTTON","Y");
		
		
		
		//collect button onClick event
		Iterable<EventListener<? extends Event>> iter = p_targetButton.getEventListeners("onClick");
		ArrayList<EventListener> events = new ArrayList<EventListener>();
		for (EventListener el : iter){
			events.add(el);
			p_targetButton.removeEventListener("onClick", el);
		}
		
		if (events.size() <= 0){
			UniLog.log1("button without onClick event, setup abort");
			return;
		}
		

		
		//setup ok/cancel button
		//final Button okButton = new ZkBiButton("Proceed");
		final Button okButton = new ZkBiButton(p_sh == null ? "Proceed" : p_sh.getBtLabel("Proceed"));
		if (StringUtils.isNotBlank(p_targetButton.getId())) {
			okButton.setId(p_targetButton.getId() + "BatchOk");
		}
		
	    okButton.setIconSclass("z-icon-check");
		//final Button cancelButton = new ZkBiButton("Back");
		final Button cancelButton = new ZkBiButton(p_sh == null ? "Back" : p_sh.getBtLabel("Back"));
		if (StringUtils.isNotBlank(p_targetButton.getId())) {
			cancelButton.setId(p_targetButton.getId() + "BatchCancel");
		}
		cancelButton.setIconSclass("z-icon-times");
		//cancelButton.setStyle("opacity:0.6;");
		final Hbox batchHbox = new Hbox() {{
			this.appendChild(okButton);
			this.appendChild(cancelButton);
		}};
		//batchHbox.setParent(p_targetButton.getParent());
		batchHbox.setVisible(false);
		
		
		//transfer onClick event to okButton
		for (EventListener el : events){
			okButton.addEventListener("onClick", el);
		}	
		
		//handle cancel button event
		cancelButton.addEventListener("onClick", new ZkBiEventListener() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("called");
				batchHbox.setVisible(false);
				p_targetButton.setDisabled(false);
				if (p_batchModeButton != null && StringUtils.equals((String)p_batchModeButton.getAttribute("BATCH_MODE_STATUS"), "Y")) {
					Events.echoEvent("onClick", p_batchModeButton, null);
				}
				
				for (Component comp : p_targetButton.getParent().getChildren()) {
					if (StringUtils.equals((String)comp.getAttribute("BATCH_MODE_HIDE"),"Y")) {
						comp.setAttribute("BATCH_MODE_HIDE", "N");
						comp.setVisible(true);
					}
				}
			}});
		
		//setup org button
		p_targetButton.addEventListener("onClick", new EventListener() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("XXXX called");
				if (batchHbox.getParent() == null) {
					batchHbox.setParent(p_targetButton.getParent());
				}
				batchHbox.setVisible(true);
				Clients.showNotification(ZkSessionHelper.getSessionHelper().getLabel("Next, choose action"), "info", batchHbox,"end_center", 5000, true); 
				
				p_targetButton.setDisabled(true);
				if (p_batchModeButton != null && !StringUtils.equals((String)p_batchModeButton.getAttribute("BATCH_MODE_STATUS"), "Y")) {
					Events.echoEvent("onClick", p_batchModeButton, null);
				}
				for (Component comp : p_targetButton.getParent().getChildren()) {
					if (comp instanceof Button && comp != p_targetButton && comp.isVisible()) {
						comp.setAttribute("BATCH_MODE_HIDE", "Y");
						comp.setVisible(false);
					}
				}
				UniLog.log1("XXXX ended");
			}});
		
	}
	
	static void getChildrenbyClass_real(List<Component> compList,Class cla,List<Component> ar) throws ClassNotFoundException{
		for(Component c : compList) {
			if(cla.isInstance(c)) {
				ar.add(c);
			}
			List<Component> childl = c.getChildren();
			getChildrenbyClass_real(childl,cla,ar);
		}
	}
	public static List<Component> getChildrenbyClass(Component rootComp,String className) throws ClassNotFoundException{
		ArrayList<Component> al = new ArrayList<Component>();
		Class cl = Class.forName(className);
		getChildrenbyClass_real(rootComp.getChildren(),cl,al);
		return(al);
	}
	
	
	public static String mineTypeToExtention(String p_mineType) {
		if(p_mineType.equals("application/pdf")) return(".pdf");
		if(p_mineType.equals("image/jpeg")) return(".jpg");
		if(p_mineType.equals("image/png")) return(".png");
		return("");
	}
	
	/***
	 * encrypt string
	 * remark: the encrypted string is much larger than original, as it contain iv(16byte), hash(32byte) and base64 (~+33%) + sha256
	 * 
	 * @param p_sh
	 * @param p_inStr
	 * @return
	 */
	public static String encryptStrToBase64(SessionHelper p_sh, String p_inStr) {
		try {
			return CryptoUtil.encryptToBase64(p_sh.getAESKey(), p_inStr.getBytes("UTF-8"), true);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	public static String decryptStrFromBase64(SessionHelper p_sh, String p_eDataWithIvString) {
		try {
			return new String(CryptoUtil.decryptFromBase64(p_sh.getAESKey(),p_eDataWithIvString),"UTF-8");
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/***
	 * for ui performance test only
	 * @param p_comp
	 * @param p_rowCnt
	 * @param p_instant
	 * @param p_withEventListener
	 */
	public static void createDummyGrid(Component p_comp, int p_rowCnt, int p_colCnt, final boolean p_instant, boolean p_withEventListener) {
		Grid grid = new Grid();
		p_comp.appendChild(grid);
    	Columns columns = new Columns();
    	for (int i=0; i<p_colCnt; i++) {
    		columns.appendChild(new Column());
    	}
    	grid.appendChild(columns);    	
    	Rows rows = new Rows();
    	grid.appendChild(rows);
    	final EventListener el = new EventListener() {
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log1("new value:" + ((Textbox)event.getTarget()).getValue());
		}};
    	for (int i=0; i<p_rowCnt; i++) {
    		final int rowIdx = i;
    		Row row = new Row();
    		rows.appendChild(row);
    		for (int j=0; j<p_colCnt; j++) {
    			final int colIdx = j;
    			if (p_withEventListener) {
    				row.appendChild(new Textbox() {{this.setInstant(p_instant);this.addEventListener(Events.ON_CHANGE, el);this.setHflex("1"); this.setValue(rowIdx+":"+colIdx);}});
    			}
    			else {
    				row.appendChild(new Textbox() {{this.setInstant(p_instant);this.setHflex("1"); this.setValue(rowIdx+":"+colIdx);}});
    			}
    		}
    	}
	}
	/***
	 * check listbox comp is s2listbox enabled
	 * @param p_sh - optional. null bypass some validation
	 * @param p_comp - the listbox component
	 * @param p_allowMobile - true: show s2listbox in mobile
	 * @return
	 */
	public static boolean isSelect2(SessionHelper p_sh, Component p_comp, boolean p_allowMobile) {
		/*
		if (p_sh == null || !p_sh.getAllowS2Listbox()) {
			UniLog.log1("function disabled");
			return false;
		}
		if (!p_allowMobile && p_sh.isMobile()) {
			UniLog.log1("function disabled for mobile");
			return false;
		}
		*/
		if (p_sh != null && !p_sh.getAllowS2Listbox()) {
			UniLog.log1("function disabled");
			return false;
		}
		if (p_sh != null && !p_allowMobile && p_sh.isMobile()) {
			UniLog.log1("function disabled for mobile");
			return false;
		}
		if (p_comp instanceof Listbox && StringUtils.equals((String)p_comp.getAttribute("select2-enable"), "Y")) {
			return true;
		}
		return false;
	}
	public static boolean isSelect2(SessionHelper p_sh, Component p_comp) {
		return isSelect2(p_sh, p_comp, false);
	}
	public static boolean isSelect2(Component p_comp) {
		return isSelect2(null, p_comp, false);
	}
	/*
	//230607 no caller, it should be useless
	public static void setupS2(Component p_comp, boolean p_allowMobile, Boolean p_allowClear) {
		setupSelect2(p_comp, p_allowMobile, p_allowClear, false);
	}
	*/
	public static void setupSelect2(Component p_comp, boolean p_allowMobile, Boolean p_allowClear) {
		setupSelect2(p_comp, p_allowMobile, p_allowClear, false);
	}
	/***
	 * traverse all component and setup select2 listbox
	 * @param p_comp parent component contain select2 listbox
	 * @param p_allowMobile false - disable select2 in mobile mode
	 * @param p_allowClear true - allow clear selections
	 */
	public static synchronized void setupSelect2(Component p_comp, boolean p_allowMobile, Boolean p_allowClear, final boolean p_allowListenResizeEvent) {
		//andrew221030 - TODO should narrow the synchronized scope to make it more efficient
		//andrew211101 - experimental try to fix s2 not selectable and incomplete list bug, probably not useful.
		
		if (fDebug.get()) UniLog.log1("called comp:%s", p_comp);
		//ZkUtil.dumpData(p_comp);
		SessionHelper sh = ZkSessionHelper.getSessionHelper(); //andrew210216: better obtain sh from parameter
		
		if (isSelect2(sh,p_comp,p_allowMobile)) {
			if (fDebug.get()) UniLog.log1("process %s %s", p_comp, p_comp.getId());
			final boolean allowClear = p_allowClear != null ? p_allowClear : sh.getS2AllowClearDef(); //andrew230607 allowClear default obtain from ini
			final String placeholder = (String) (p_comp.getAttribute("placeholder") == null ? "" : p_comp.getAttribute("placeholder"));
			final Listbox listbox = (Listbox) p_comp;
			final boolean multiple = StringUtils.equals((String)p_comp.getAttribute("select2-multiple"), "Y"); //for select multiple item
			final boolean tags = StringUtils.equals((String)p_comp.getAttribute("select2-tags"), "Y");  //for add new item
			if (fDebug.get()) UniLog.log1("call zkbis2.setup('%s',%s,%s)",listbox.getUuid(), multiple, tags);
			
			//call js to build select2 
			//andrew200811: need some delay to avoid duplicate list problem. //TODO: initialize select2 js without delay
			//ZkUtil.delayJs(listbox,null,50,"zkbis2.setup('%s',%s,%s,'%s',%b,%b)",listbox.getUuid(), multiple, tags, placeholder, allowClear, p_allowListenResizeEvent);
			js("setTimeout(function(){zkbis2.setup('%s',%s,%s,'%s',%b,%b)}, 50)",listbox.getUuid(), multiple, tags, placeholder, allowClear, p_allowListenResizeEvent);
			
			
			//andrew210211 avoid redundant event listener
			if (StringUtils.equals((String)p_comp.getAttribute("setupSelect2.status"),"Y")){
				if (fDebug.get()) UniLog.log1("already registered");
				return;
			}
			p_comp.setAttribute("setupSelect2.status","Y");
			
			//hide the original componenbt
			if (p_comp instanceof HtmlBasedComponent) {
				ZkUtil.appendStyle((HtmlBasedComponent) p_comp, "display:none");
			}

			//add event listener
			listbox.addEventListener("onSelect2Select", new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					UniLog.log1("%s got event:%s, data:%s", event.getTarget(), event, event.getData());
					org.zkoss.json.JSONObject json = (org.zkoss.json.JSONObject)event.getData();
					org.zkoss.json.JSONArray selectedItemIds = (org.zkoss.json.JSONArray)json.get("selectedItemIds");
					org.zkoss.json.JSONArray removeItemIds = (org.zkoss.json.JSONArray)json.get("removeItemIds");
					org.zkoss.json.JSONArray tagItemValues = (org.zkoss.json.JSONArray)json.get("tagItemValues");
					Set<String> selectedItemIdList = new HashSet<String>();
					Set<String> removeItemIdList = new HashSet<String>();
					Collections.addAll(selectedItemIdList, selectedItemIds.toArray(new String[0]));
					Collections.addAll(removeItemIdList, removeItemIds.toArray(new String[0]));

					boolean needReSetup = false;
					Set<Listitem> selectedItemList = new LinkedHashSet<Listitem>();
					
					//remove item
					for (int i = 0; i < listbox.getItemCount(); ) {
						Listitem li = listbox.getItemAtIndex(i);
						String id = li.getUuid();
						if (removeItemIdList.contains(id)) {
							listbox.removeItemAt(i);
							needReSetup = true;
						} 
						else {
							if (selectedItemIdList.contains(id))
								selectedItemList.add(li);
							i++;
						}
					}
					
					//add item
					for (int i = 0; i < tagItemValues.size(); i++) {
						String v = (String)tagItemValues.get(i);
						Listitem li = new Listitem(v, v);
						li.setClientAttribute("data-select2-tag", "true");
						listbox.appendChild(li);
						selectedItemList.add(li);
						needReSetup = true;
					}
					/*
					//andrew210217 seems no more required
					if (needReSetup) {
						//andrew201006 bug fix. hide the original listbox
						addSclass(listbox, "select2-hidden-accessible"); 
					}
					*/
					
					UniLog.log1("setSelectedItems %s, oldSelectedItems:%s, oldSelectItem:%s, isMultiple:%b", selectedItemList, listbox.getSelectedItems(), listbox.getSelectedItem(), listbox.isMultiple());
					if (listbox.isMultiple()) {
						listbox.clearSelection();
						listbox.setSelectedItems(selectedItemList);
					} 
					else {
						if (!selectedItemList.isEmpty())
							listbox.setSelectedItem(selectedItemList.iterator().next());
						else
							listbox.setSelectedItem(null);
					}
					UniLog.log1("getSelectedItems:%s, getSelectItem:%s", listbox.getSelectedItems(), listbox.getSelectedItem());
					Events.postEvent(new SelectEvent<Listitem, Object>(Events.ON_SELECT, listbox, listbox.getSelectedItems(), listbox.getSelectedItem()));
					if (needReSetup) {
						UniLog.log1("Resetup");
						ZkUtil.delayJs(listbox,null,50,"zkbis2.setup('%s',%s,%s,'%s',%b,%b);$('#%s').focus()",listbox.getUuid(), multiple, tags, placeholder, allowClear, p_allowListenResizeEvent, listbox.getUuid());
					}
				}
			});
		}
	}
	public static void setupSelect2(Component p_comp, boolean p_allowMobile) {
		setupSelect2(p_comp, p_allowMobile, null);
	}
	public static void setupSelect2(Component p_comp) {
		setupSelect2(p_comp, false, null);
	}
	public static void setURLParamsToComp(Component p_comp) {
		p_comp.setAttribute("urlParams",Executions.getCurrent().getParameterMap());
	}
	public static void setArgsToComp(Component p_comp) {
		p_comp.setAttribute("urlParams",Executions.getCurrent().getArg());
	}
	public static String getURLParamFromComp(Component p_comp,String p_key) {
		Map urlParams = (Map) p_comp.getAttribute("urlParams");
		if (urlParams == null) {
			return null;
		}
	  	Object obj = urlParams.get(p_key);
	  	if (obj == null) {
	  		return null;
	  	}
	  	else if (obj instanceof String) {
	  	   return (String) obj;
	  	}
	  	else if (obj instanceof String[] && ((String []) obj).length > 0) {
	  		return ((String []) obj)[((String []) obj).length-1];
	  	}
	  	return null;
	}
	public static HttpServletRequest getServletRequest() {
		return((HttpServletRequest) Executions.getCurrent().getNativeRequest());
	}
	public static String getURL() {
		StringBuilder urlSb = new StringBuilder();
		urlSb.append(((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getServletPath().substring(1));
		
		try {
			String queryStr = ((HttpServletRequest) Executions.getCurrent().getNativeRequest()).getQueryString();
			if (StringUtils.isNotBlank(queryStr)) {
				urlSb.append("?" + URLDecoder.decode(queryStr,"ISO8859-1"));
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return urlSb.toString();
	}
	public static void addTranslateContextMenu(SessionHelper p_sh, Label p_label, BiColumn p_col) {
		if (p_sh == null || !p_sh.getAllowUpdateTranslate()) {
			return;
		}
		JxZkBiBase.addContextMenu(p_sh,p_label, MapUtil.of("changeLabel", MapUtil.of("key",p_col.getCellFullName(),"defaultValue",p_col.getEngName())));
	}
	
	public static final int thumbnailMinWidth = 360;
	public static String getPhotoSize(byte[] photoData) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(photoData);
	    BufferedImage img = ImageIO.read(is);
	    is.close();
	    return img.getWidth() + "x" + img.getHeight();
	}
	
	
	public static int readPhotoDegree(byte[] photoData) {
		int degree = 0;
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new ByteArrayInputStream(photoData));
			FileType fileType = FileTypeDetector.detectFileType(bis);
			UniLog.log1("readPhotoDegree filetype:" + fileType);
			if (fileType == FileType.Jpeg){
				Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(photoData));
				ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
				int orientation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
				switch (orientation)
				{
				case 3:
				case 4:
					degree = 180; 
					break;
				case 5:
				case 6:
					degree = 90;
					break;
				case 7:
				case 8:
					degree = 270;
					break;
				default:
					degree = 0;
					break;
				}
			}
		} catch (Exception e) {
			//e.printStackTrace(); //andrew201218: avoid no exif exception
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		UniLog.log1("readPhotoDegree:" + degree);
		return degree;
	}
	public static byte[] rotatePhoto(byte[] photoData, Map<String, String> photoAttrMap) throws Exception {
		ByteArrayOutputStream bos = null;
		ImageOutputStream ios = null;
		try {
			int degree = readPhotoDegree(photoData);
			if (degree != 0) {
				ByteArrayInputStream is = new ByteArrayInputStream(photoData);
				BufferedImage img = ImageIO.read(is);
				int sWidth = img.getWidth();
				int sHeight = img.getHeight();
				int dWidth = sWidth;
				int dHeight = sHeight;
				if (degree == 90 || degree == 270) {
					dWidth = sHeight;
					dHeight = sWidth;
				}
				BufferedImage dimg = new BufferedImage(dWidth, dHeight, img.getType());
				Graphics2D g = dimg.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				AffineTransform tf = new AffineTransform();
				tf.translate((dWidth - sWidth) / 2, (dHeight - sHeight) / 2);
				tf.rotate(Math.toRadians(degree), sWidth / 2, sHeight / 2);
				g.drawImage(img, tf, null);
				g.dispose();
				is.close();

				bos = new ByteArrayOutputStream();
				ios = ImageIO.createImageOutputStream(bos);
				ImageWriter imgWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
				imgWriter.setOutput(ios);
				ImageWriteParam param = imgWriter.getDefaultWriteParam(); 
				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); 
				param.setCompressionQuality(1f);
				imgWriter.write(dimg);
				byte[] data = bos.toByteArray();
				if (data != null && data.length > 0) {
					if (photoAttrMap != null)
						photoAttrMap.put("data_size", getPhotoSize(data));
					return data;
				}
				/*if (ImageIO.write(dimg, "jpg", bos)) {
					byte[] data = bos.toByteArray();
					if (photoAttrMap != null)
						photoAttrMap.put("data_size", getPhotoSize(data));
					return data;
				}*/
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bos != null)
					bos.close();
				if (ios != null)
					ios.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (photoAttrMap != null)
			photoAttrMap.put("data_size", getPhotoSize(photoData));
		return photoData;
	}
	public static synchronized byte[] storeThumbnal(byte[] photoData, Map<String, String> photoAttrMap) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(photoData);
	    BufferedImage img = ImageIO.read(is);
	    is.close();
		float ratio = (float)thumbnailMinWidth / Math.min(img.getWidth(), img.getHeight());
		int newWidth = (int)(ratio * img.getWidth());
		int newHeight = (int)(ratio * img.getHeight());
		BufferedImage dimg = new BufferedImage(newWidth, newHeight, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, newWidth, newHeight, 0, 0, img.getWidth(), img.getHeight(), null);
		g.dispose();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(dimg, "jpg", bos);
		byte[] data = bos.toByteArray();
		bos.close();
		if (photoAttrMap != null)
			photoAttrMap.put("data_size", String.format("%dx%d", newWidth, newHeight));
		return(data);
	}
	/***
	 * check environment variable against compValue
	 * @param p_key
	 * @param p_value
	 * @return
	 */
	public static boolean checkEnv(String p_key, String p_compValue) {
		try {
			String value = System.getenv(p_key);
			return StringUtils.equalsIgnoreCase(value == null ? null : value.trim(), p_compValue);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}
	public static void dumpData(Object p_obj) {
		dumpData("", p_obj);
	}
	
	public static void dumpData(String p_label, Object p_obj) {
		dumpData(p_label, p_obj, 20);
	}
	
	/*
	 * dump the data to console for debug
	 * especially useful for debug list obj
	 */
	public static void dumpData(String p_label, Object p_obj, int p_maxCnt) {
		try {
			//validation
			if (p_obj == null) {
				UniLog.log1("value:null");
				return;
			}
			UniLog.log1("label:%s class:%s", StringUtils.defaultIfBlank(p_label, "na"), p_obj.getClass().getName());
			
			
			//implement dumpdata code here
			
			if (p_obj instanceof GipiNamedItemList) {
				GipiNamedItemList co = (GipiNamedItemList) p_obj;
				for(int i=0;i<co.getRowCount();i++) {
					UniLog.log1("idx:%d value:%s name:%s", (i+1), co.getRow(i), co.getString(co.getRow(i)));
					if (i >= p_maxCnt) {
						return;
					}
				}
				return;
			}

			if (p_obj instanceof AbstractGetItemProperty) {
				AbstractGetItemProperty co = (AbstractGetItemProperty) p_obj;
				for(int i=0;i<co.getRowCount();i++) {
					UniLog.log1("idx:%d value:%s", (i+1), co.getRow(i));
					if (i >= p_maxCnt) {
						return;
					}
				}
				return;
			}
			if (p_obj instanceof Listitem) {
				Listitem co = (Listitem) p_obj;
				UniLog.log1("id:%s label:%s value:%s", co.getId(), co.getLabel(), co.getValue());
				return;
			}
			if (p_obj instanceof Listbox) {
				Listbox co = (Listbox) p_obj;
				int idx = 0;
				for (EventListener el : co.getEventListeners(Events.ON_SELECT)) {
					UniLog.log1("el:%d:%s",idx,el.toString());
				}
				idx = 0;
				for (Listitem li:co.getItems()) {
					UniLog.log1("id:%s idx:%d selected:%s label:%s value:%s", co.getId(), ++idx, li.isSelected(), li.getLabel(), li.getValue());
					if (idx >= p_maxCnt) {
						return;
					}
				}
				return;
			}
			if (p_obj instanceof S2Listbox) {
				Listbox co = (Listbox) ((S2Listbox)p_obj).getComp();
				int idx = 0;
				for (Listitem li:co.getItems()) {
					UniLog.log1("id:%s idx:%d selected:%s label:%s value:%s", co.getId(), ++idx, li.isSelected(), li.getLabel(), li.getValue());
					if (idx >= p_maxCnt) {
						return;
					}
				}
				return;

			}
			if (p_obj instanceof Label) {
				Label co = (Label) p_obj;
				UniLog.log1("id:%s value:%s", co.getId(), co.getValue());
				return;
			}
			if (p_obj instanceof InputElement) {
				InputElement co = (InputElement) p_obj;
				UniLog.log1("id:%s value:%s", co.getId(), co.getText());
				return;
			}
			if (p_obj instanceof String[]) {
				int idx = 0;
				for (String co : (String[]) p_obj) {
					UniLog.log1("idx:%d %s", ++idx, co);
				}
				return;
			}
			if (p_obj instanceof Number[]) {
				int idx = 0;
				for (Number co : (Number[]) p_obj) {
					UniLog.log1("idx:%d %s", ++idx, co.toString());
				}
				return;
			}
			if (p_obj instanceof ColumnCell) {
				ColumnCell cc = (ColumnCell) p_obj;
				UniLog.log1("type:%d value:%s colDisStr:%s", cc.getType(), cc.getString(), cc.getColumnDisplayString());
				return;
			}
			if (p_obj instanceof BiResult) {
				BiResult br = (BiResult) p_obj;
				UniLog.log1("record count:%d", br.getRowCount());
				JSONObject json = BiResult.resultToJson(br);
				UniLog.log1("\n"+json.toString(3));
				return;
			}
			if (p_obj instanceof TableRec) {
				TableRec tr = (TableRec) p_obj;
				for(int i = 0;i<tr.getRecordCount();i++) {
					StringBuilder sb = new StringBuilder();
					sb.append("tr idx:" + i);
					for (int j=0; j<tr.getColumnCount(); j++) {
						sb.append(",");
						sb.append(tr.getFieldString(j,i)); 
					}
					UniLog.log1(sb.toString());
				}
				return;
			}
			
			if (p_obj instanceof Vector) {
				int idx = 0;
				for (Object obj : (Vector)p_obj) {
					UniLog.log1("vector idx:%d value:%s", ++idx, obj.toString());
				}
				return;
			}
			if (p_obj instanceof Object[]) {
				int idx = 0;
				for (Object co : (Object[]) p_obj) {
					UniLog.log1("idx:%d %s", ++idx, co);
				}
				return;
			}

			//no match, just show hashcode
			UniLog.log1("%d:%s", p_obj.hashCode(), p_obj.toString());
			return;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	/***
	 * get custom attribute of target component
	 * not yet test!!! probably buggy
	 * @param p_comp
	 * @param p_tag
	 * @param p_def
	 * @return
	 */
	public static <T> T getCustAttr(Component p_comp,String p_tag,T p_def) {
		try {
			Object valObj = p_comp.getAttribute(p_tag);
			//dumpData(valObj);
			if (valObj == null) {
				return p_def;
			}
			if (valObj instanceof String) {
				if (p_def instanceof Integer) {
					return (T) Integer.valueOf((String)valObj);
				}
				if (p_def instanceof Long) {
					return (T) Long.valueOf((String)valObj);
				}
				if (p_def instanceof Double) {
					return (T) Double.valueOf((String)valObj);
				}
				if (p_def instanceof String) {
					return (T) valObj;
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return p_def;
	}
	
	/***
	 * read url page_id and set to page attribute
	 * @param p_comp
	 */
	public static void setPageId(Component p_comp) {
		try {
			if (p_comp == null || p_comp.getPage() == null) {
				UniLog.log1("ignore for null comp");
				return;
			}
			String pageId = Executions.getCurrent().getParameter("page_id");
			if (StringUtils.isBlank(pageId)) {
				UniLog.log1("ignore for null pageid");
				return;
			}
			p_comp.getPage().setAttribute("ZKBI_PAGEID", pageId);
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	/***
	 * obtain page_id from page attribute
	 * @param p_comp
	 * @return
	 */
	public static String getPageId(Component p_comp) {
		try {
			String pageId = "";
			if (p_comp != null && p_comp.getPage() != null) {
				pageId = (String) p_comp.getPage().getAttribute("ZKBI_PAGEID");
			}
			return StringUtils.isBlank(pageId) ? "nopageid" : pageId;
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		return "nopageid";
	}
	
	
	/***
	 * obtain component full name for display only
	 * don't use it for key
	 * @param p_comp
	 * @return [pageid].[compid]...[compid/classname]
	 */
	public static String getCompFullName(Component p_comp) {
		if (p_comp == null) {
			return "";
		}
		return getCompFullNameReal(p_comp, p_comp, new StringBuilder(), 10);
	}
	private static String getCompFullNameReal(Component p_curComp, Component p_initComp, StringBuilder p_sb, int p_maxLevel) {
		if (p_curComp == null || p_maxLevel <= 1) {
			p_sb.insert(0, getPageId(p_initComp));
			return p_sb.toString();
		}
		if (StringUtils.startsWithAny(p_curComp.getId(), "abc-")) { 
			//ignore specific id
		}
		else if (StringUtils.isNotBlank(p_curComp.getId())) {
			p_sb.insert(0, "."+p_curComp.getId());
		}
		else {
			if (p_sb.length() == 0) {  //show class name for leave node without id
				//p_sb.insert(0, "."+p_curComp.getClass().getSimpleName());
				p_sb.insert(0, "."+(p_curComp.getClass().isAnonymousClass() ? "Anonymous" : p_curComp.getClass().getSimpleName()));
			}
		}
		return getCompFullNameReal(p_curComp.getParent(), p_initComp, p_sb, p_maxLevel-1);
	}
	/***
	 * Returns whether this component is real visible (all its parents are visible).
	 * similar to Components.isRealVisible(p_comp), but null comp will return false.
	 * @param p_comp
	 * @return
	 */
	public static boolean isRealVisible(Component p_comp) {
		if (p_comp == null) {
			UniLog.log1("comp is null");
			return false;
		}
		UniLog.log1("%s isVisible:%s isRealVisible:%s", p_comp, p_comp.isVisible(), Components.isRealVisible(p_comp));
		return Components.isRealVisible(p_comp);
	}
	
	/***
	 * set hflex for component with validation
	 * if validate check fail, will ignore hflex 
	 * @param p_comp
	 * @param p_flex
	 */
	public static void setHflexSafe(Component p_comp, String p_flex) {
		if (!(p_comp instanceof HtmlBasedComponent)) {
			return;
		}
		HtmlBasedComponent comp = (HtmlBasedComponent) p_comp;
		
		//when flex is blank, not require validation
		if (StringUtils.isBlank(p_flex)) {
			comp.setHflex(p_flex);
			return;
		}
		
		//nodom check
		if (isNoDOM(p_comp)) {
			if (fDebug.get()) UniLog.log1("ignore hflex for nodom component");
			return;
		}
		
		//set hflex
		comp.setHflex(p_flex);
		return;
	}
	
	/***
	 * check comp is nodom element
	 * it mainly used for avoid sethflex for nodom element 
	 * @param p_comp
	 * @return
	 */
	public static boolean isNoDOM(Component p_comp) {
		return isNoDOM(p_comp, 10);
	}
	private static boolean isNoDOM(Component p_comp, int p_maxLevel) {
		if (fDebug.get()) UniLog.log1("check comp:%s level:%d", p_comp, p_maxLevel);
		if (p_maxLevel <= 0) {
			UniLog.log1("max level reached, abort");
			return false;
		}
		if (p_comp == null) {
			return false;
		}
		if (p_comp instanceof NoDOM) {
			return true;
		}
		return isNoDOM(p_comp.getParent(), p_maxLevel - 1);
		
	}
	public static boolean isFormula(Component p_comp) {
		if (p_comp == null) {
			return false;
		}
		return StringUtils.equals((String)p_comp.getAttribute("ZKBI_IS_FORMULA"),"Y");
	}
	public static boolean isNoPaste(Component p_comp) {
		if (p_comp == null) {
			return false;
		}
		return StringUtils.equals((String)p_comp.getAttribute("ZKBI_IS_NOPASTE"),"Y");
	}
	public static void addCompMark(Component p_comp, BiColumn p_col) {
		if (p_comp == null || p_col == null) {
			return;
		}
		//UniLog.log1("comp:%s id:%s formula:%s", p_comp, p_comp.getId(), p_col.getFormula());
		if (StringUtils.isNotBlank(p_col.getFormula(false))) {
			p_comp.setAttribute("ZKBI_IS_FORMULA", "Y");
		}
		if (p_col.isNoPaste()) {
			p_comp.setAttribute("ZKBI_IS_NOPASTE", "Y");
		}
	}
	public static String joinStringLabel(SessionHelper sessionHelper, String delimiter, Object... o) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < o.length; i += 2) {
			if (sb.length() > 0)
				sb.append(delimiter);
			sb.append(String.format(sessionHelper.getLabel((String)o[i]), o[i + 1]));
		}
		return sb.toString();
	}
	public static void removeAllEventListener(Component comp, String eventName) {
    	Iterator<EventListener<?>> it = comp.getEventListeners(eventName).iterator();
    	while (it.hasNext())
    		comp.removeEventListener(eventName, it.next());
	}

    public static void echoEvent(Component targetComp, String eventName, Object data, boolean removeEvent, EventListener<? extends Event> el) {
    	if (removeEvent)
    		removeAllEventListener(targetComp, eventName);
		targetComp.addEventListener(eventName, el);
		Events.echoEvent(eventName, targetComp, data);
    }
    public static void echoEvent(Component targetComp, String eventName, Object data, EventListener<? extends Event> el) {
    	echoEvent(targetComp, eventName, data, true, el);
    }

    public static void setEventListener(Component targetComp, String eventName, EventListener<?> listener) {
   		removeAllEventListener(targetComp, eventName);
   		targetComp.addEventListener(eventName, listener);
    }
    public static <T extends Event> void setZkBiEventListener(Component targetComp, String eventName, EventListener<T> cb) {
   		removeAllEventListener(targetComp, eventName);
   		targetComp.addEventListener(eventName, new ZkBiEventListener<T>() {
			@Override
			public void onZkBiEvent(T event) throws Exception {
				cb.onEvent(event);
			}
   		});
    }
    public static <T extends Event> void setEventListenerForCallOne(Component targetComp, String eventName, Consumer<T> cb) {
   		removeAllEventListener(targetComp, eventName);
		targetComp.addEventListener(eventName, new EventListener<T>() {
			boolean flag = false;
			@Override
			public void onEvent(T event) throws Exception {
				UniLog.log1("event:%s, hashcode:%d, flag:%b", event, event.hashCode(), flag);
				removeAllEventListener(targetComp, eventName);
				if (flag)
					return;
				cb.accept(event);
				flag = true;
			}
		});
    }
    
    public static Timer timerEvent(Timer oldTimer, Component targetComp, int delay, Runnable cb) {
    	return timerEvent(oldTimer, targetComp, null, delay, cb);
    }
    public static Timer timerEvent(Timer oldTimer, Component targetComp, String busyMsg, int delay, Runnable cb) {
    	return timerEvent(oldTimer, targetComp, busyMsg, delay, false, true, () -> {
    		cb.run();
    		return true;
    	}, null, null);
    }
    public static Timer timerEvent(Timer oldTimer, Component targetComp, int delay, boolean repeat, boolean immediateRun, Supplier<Boolean> cb) {
    	return timerEvent(oldTimer, targetComp, null, delay, repeat, immediateRun, cb, null, null);
    }
    public static Timer timerEvent(Timer oldTimer, Component targetComp, int delay, boolean repeat, boolean immediateRun, Supplier<Boolean> cb, Runnable startCb, Runnable stopCb) {
    	return timerEvent(oldTimer, targetComp, null, delay, repeat, immediateRun, cb, startCb, stopCb);
    }
    public static Timer timerEvent(Timer oldTimer, Component targetComp, String busyMsg, int delay, boolean repeat, boolean immediateRun, Supplier<Boolean> cb, Runnable startCb, Runnable stopCb) {
    	if (oldTimer != null) {
    		UniLog.log1("timerEvent cancelled delay:%d", oldTimer.getDelay());
    		oldTimer.setRunning(false);
    		oldTimer.detach();
    	}
		Page page = targetComp != null ? targetComp.getPage() : null;
		if (page == null)
			page = ZkUtil.getMainCompPage();
    	Timer timer = new Timer() {
    		@Override
    		public void start() {
    			super.start();
    			Component busyComp = (Component)getAttribute("busyComp");
    			if (busyComp != null)
    				Clients.showBusy(busyComp, busyMsg);
    			if (startCb != null)
    				startCb.run();
    		}
    		@Override
    		public void stop() {
    			super.stop();
    			Component busyComp = (Component)getAttribute("busyComp");
    			if (busyComp != null)
    				Clients.clearBusy(busyComp);
    			if (stopCb != null)
    				stopCb.run();
    		}
    	};
  		if (StringUtils.isNotBlank(busyMsg)) {
  			if (targetComp != null && !(targetComp instanceof Window) && !(targetComp instanceof Div)) {
  				Component comp = targetComp;
  				targetComp = null;
				while ((comp = comp.getParent()) != null) {
					if (comp instanceof Window || comp instanceof Div) {
						targetComp = comp;
						break;
					}
				}
  			}
  			if (targetComp == null)
  				targetComp = getMainComp();
  			timer.setAttribute("busyComp", targetComp);
  		}
		timer.setDelay(delay);
		timer.setPage(page);
		timer.setRepeats(repeat);
		timer.addEventListener(Events.ON_TIMER, event -> {
			if (cb.get() || !repeat) {
				timer.setRunning(false);
				timer.detach();
			}
		});
		timer.setRunning(immediateRun);
    	return timer;
    }
    
    public static int executeInsertIntoSql(SelectUtil su, String tabName, List<String> fieldNameList, Wherecl wherecl) throws Exception {
		StringBuilder sb = new StringBuilder("insert into ");
		sb.append(tabName);
		sb.append("(");
		sb.append(String.join(",", fieldNameList));
		sb.append(") values (");
		sb.append(String.join(",", Stream.generate(() -> "?").limit(fieldNameList.size()).toArray(String[]::new)));
		sb.append(")");
		UniLog.log1("sql:%s, wherecl:%s", sb, wherecl.getValues());
		return su.executeUpdate(sb.toString(), wherecl);
    }
	public static Runnable safeRunnable(CheckedRunnable cb, boolean showErrMsg) {
		return () -> {
			try {
				cb.run();
			} catch (Exception e) {
				UniLog.log(e);
            	if (showErrMsg)
            		ZkUtil.errMsg(StringUtils.defaultIfBlank(e.getMessage(), e.toString()));
			}
		};
	}


	public static Runnable safeRunnable(CheckedRunnable cb) {
		return safeRunnable(cb, false);
	}

	public static JxActionListener safeJxActionListener(CheckedConsumer<JxField> throwingConsumer, boolean showErrMsg) {
        return field -> {
            try {
                throwingConsumer.accept(field);
            } catch (Exception e) {
            	UniLog.log(e);
            	if (showErrMsg)
            		ZkUtil.errMsg(e.getMessage());
            }
        };
	}

	public static JxActionListener safeJxActionListener(CheckedConsumer<JxField> throwingConsumer) {
		return safeJxActionListener(throwingConsumer, true);
	}

	public static JxChangeListener safeJxChangeListener(CheckedConsumer2<JxField, String> throwingConsumer, boolean returnValue, boolean showErrMsg) {
        return (field, orgvalue) -> {
            try {
                throwingConsumer.accept(field, orgvalue);
            } catch (Exception e) {
            	UniLog.log(e);
            	if (showErrMsg)
            		ZkUtil.errMsg(e.getMessage());
            }
           	return returnValue;
        };
	}
	
	public static JxChangeListener safeJxChangeListener(CheckedConsumer2<JxField, String> throwingPredicate) {
		return safeJxChangeListener(throwingPredicate, true, true);
	}

	public static void addJxActionListener(JxForm form, CheckedConsumer<JxField> cb, String... fieldNames) {
		Stream.of(fieldNames).map(form::jxAdd).forEach(k -> k.addActionListener(safeJxActionListener(cb)));
	}
	
	public static void addJxChangeListener(JxForm form, CheckedConsumer2<JxField, String> cb, String... fieldNames) {
		Stream.of(fieldNames).map(form::jxAdd).forEach(k -> k.addChangeListener(safeJxChangeListener(cb)));
	}

	public static CheckedConsumer2<JdbcPool, CheckedConsumer<SelectUtil>> importActionByJdbcPool = (jdbcPool, cb) -> {
		SelectUtil su1 = new SelectUtil(); 
		try {
			su1.init(jdbcPool);
			su1.setAutoCommit(false);
			cb.accept(su1);
			su1.commit();
		} catch (Exception e) {
			try {
				su1.rollback();
			} catch (Exception e1) {
				UniLog.log(e1);
			}
			throw e;
		} finally {
			try{
				su1.setAutoCommit(true);
				su1.close();
			} catch (Exception e2) { 
				UniLog.log(e2);
			}
		}
	};

	public static CheckedConsumer2<SessionHelper, CheckedConsumer<SelectUtil>> importAction = (sessionHelper, cb) -> {
		importActionByJdbcPool.accept(sessionHelper.getLoginTokenJdbcPool(), cb);
	};
	
	public static void batchExecuteUpdate(SessionHelper sessionHelper, Object... os) throws Exception {
		importAction.accept(sessionHelper, su -> {
			for (int i = 0; i < os.length; i+=2)
				su.executeUpdate((String)os[i], (Wherecl)os[i + 1]);
		});
	}
	
	
	public static void sendToMobileUsbPrinter(String str) {
		js("(typeof android !== 'undefined') ? android.findPrinterAndPrint('%s') : console.log('unknown device')", StringEscapeUtils.escapeJava(str));
	}
	
	public static Map<String, Object> getBiCellCollectionMap(BiCellCollection bcc, Vector<BiColumn> cls) {
		return cls.stream().collect(Collectors.toMap(
							BiColumn::getLabel, 
							bc -> bcc.getCell(bc.getLabel()).getObject(),
							(oldValue, newValue) -> newValue,
							LinkedHashMap::new));
	}

	public static <T> T getBiResultRecordMap(BiResult br) {
		return (T)getBiResultRecordMap(br, true);
	}
	
	public static <T> T getBiResultRecordMap(BiResult br, boolean currentOnly) {
		if (br.getParent() != null || !currentOnly)
			return (T)getBiResultRecordMapStream(br).collect(Collectors.toList());
		else
			return (T)getBiCellCollectionMap(br.getCurrentCollection(), br.getColumns());
	}

	public static <T> Stream<T> getBiResultRecordStream(BiResult br, CheckedFunction<BiCellCollection, T> cb) {
		if (br.getParent() != null)
			return br.getRowCollectionList().stream().map(throwFunction(cb));
		else
			return IntStream.range(0, br.getRowCount()).mapToObj(throwIntFunction(i -> {
				br.loadOneRecV(i);
				return cb.apply(br.getCurrentCollection());
			}));
	}

	public static Stream<Map<String, Object>> getBiResultRecordMapStream(BiResult br) {
		return getBiResultRecordStream(br, bcc -> getBiCellCollectionMap(bcc, br.getColumns()));
	}

	public static String getBiCellCollectionJson(BiCellCollection bcc, Vector<BiColumn> cls) {
		return GsonUtil.objToStr(getBiCellCollectionMap(bcc, cls));
	}

	public static String getBiResultRecordJson(BiResult br, boolean currentOnly) {
		return GsonUtil.objToStr(getBiResultRecordMap(br, currentOnly));
	}

	public static String getBiResultRecordJson(BiResult br) {
		return GsonUtil.objToStr(getBiResultRecordMap(br));
	}
	
	public static <T> Stream<T> getTableRecStream(TableRec tr, CheckedFunction<Integer, T> cb) {
		return IntStream.range(0, tr.getRecordCount()).mapToObj(throwIntFunction(cb));
	}

	public static Stream<CellCollection> getTableRecStream(TableRec tr) {
		return getTableRecStream(tr, tr::toCellCollection);
	}

	public static Stream<CellCollection> getTableRecStream(SelectUtil su, String sql, Wherecl wherecl) throws Exception {
		return getTableRecStream(su.getQueryResult(sql, wherecl));
	}

	public static Stream<CellCollection> getTableRecStream(SelectUtil su, String sql) throws Exception {
		return getTableRecStream(su, sql, null);
	}
	
	public static Optional<TableRec> getFirstTableRec(TableRec tr) throws TableRecException {
		if (tr.getRecordCount() > 0) {
			tr.setRecPointer(0);
			return Optional.of(tr);
		}
		return Optional.empty();
	}

	public static Optional<TableRec> getFirstTableRec(SelectUtil su, String sql, Wherecl wherecl) throws Exception {
		return getFirstTableRec(su.getQueryResult(sql, wherecl, 1));
	}

	public static Optional<TableRec> getFirstTableRec(SelectUtil su, String sql) throws Exception {
		return getFirstTableRec(su, sql, null);
	}

	public static boolean hasTableRec(TableRec tr) throws TableRecException {
		return tr.getRecordCount() > 0;
	}

	public static boolean hasTableRec(SelectUtil su, String sql, Wherecl wherecl) throws Exception {
		return hasTableRec(su.getQueryResult(sql, wherecl, 1));
	}

	public static boolean hasTableRec(SelectUtil su, String sql) throws Exception {
		return hasTableRec(su, sql, null);
	}
	
	public static <T extends Component> T getFellowWithNullId(Component comp, String id) {
		T cp = (T)comp.getFellow(id, true);
		cp.setId(null);
		return cp;
	}

	public static MethodHandle createStaticMethodHandle(String classMethodName, Class<?> returnType, Class<?>... parameterTypes) throws Throwable {
		int pos = classMethodName.lastIndexOf('.');
		return createStaticMethodHandle(classMethodName.substring(0, pos), classMethodName.substring(pos + 1), returnType, parameterTypes);
	}

	public static MethodHandle createStaticMethodHandle(String className, String methodName, Class<?> returnType, Class<?>... parameterTypes) throws Throwable {
		return createStaticMethodHandle(Class.forName(className), methodName, returnType, parameterTypes);
	}
	
	public static MethodHandle createStaticMethodHandle(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... parameterTypes) throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType methodType = MethodType.methodType(returnType, parameterTypes);
		return lookup.findStatic(clazz, methodName, methodType);
	}

	public static MethodHandle createMethodHandle(String classMethodName, Class<?> returnType, Class<?>... parameterTypes) throws Throwable {
		int pos = classMethodName.lastIndexOf('.');
		return createMethodHandle(classMethodName.substring(0, pos), classMethodName.substring(pos + 1), returnType, parameterTypes);
	}

	public static MethodHandle createMethodHandle(String className, String methodName, Class<?> returnType, Class<?>... parameterTypes) throws Throwable {
		return createMethodHandle(Class.forName(className), methodName, returnType, parameterTypes);
	}

	public static MethodHandle createMethodHandle(Class<?> clazz, String methodName, Class<?> returnType, Class<?>... parameterTypes) throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType methodType = MethodType.methodType(returnType, parameterTypes);
		return lookup.findVirtual(clazz, methodName, methodType);
	}
	
	public static MethodHandle createConstructorHandle(String className, Class<?>... parameterTypes) throws Throwable {
		return createConstructorHandle(Class.forName(className), parameterTypes);
	}

	public static MethodHandle createConstructorHandle(Class<?> clazz, Class<?>... parameterTypes) throws Throwable {
		MethodHandles.Lookup lookup = MethodHandles.lookup();
		MethodType methodType = MethodType.methodType(void.class, parameterTypes);
		return lookup.findConstructor(clazz, methodType);
	}

	public static class PickByBiResultForm {
		private JxSelOpt gipiForm;
		private BiPickGetItemProperty gipi;
		private CheckedSupplier3<String, String, List<Pair<String, Boolean>>> getSqlCallback;
		private String lastView;
		private String lastWhere;
		private List<Pair<String, Boolean>> lastOrderby;
		private SessionHelper sh;

		public PickByBiResultForm(SessionHelper sh, String[] labelList, String[] widthList, CheckedSupplier3<String, String, List<Pair<String, Boolean>>> getSqlCb, CheckedConsumer3<Object[], BiResult, Object> cb) {
			this.sh = sh;
			JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sh.getSessionData("jxzkgadgetprovider");
			gipi = new BiPickGetItemProperty(Lists.newArrayList(labelList), null, widthList != null ? Lists.newArrayList(widthList) : null);
			gipiForm = JxSelOpt.createJxSelOpt(pvdr);
			gipiForm.setOnSelectAction(fd -> {
				try {
					Object[] rec = (Object[]) fd.getValue();
					BiResult br = gipi.getBiResult();
					cb.accept(rec, br, gipiForm.getUserData());
				} catch (Exception ex) {
					UniLog.log(ex);
				}
				gipiForm.closeForm();
			});
			getSqlCallback = getSqlCb;
		}

		public void bindComponent(ZkJxPickInput pickComp, Object userData, boolean openRefill) throws Exception {
			pickComp.setJxZkForm(gipiForm);
			gipiForm.setUserData(userData);
			if (openRefill)
				setEventListener(pickComp, Events.ON_OPEN, openEvent);
			else
				openEvent.onEvent(null);
		}

		private EventListener<Event> openEvent = event -> {
			Triple<String, String, List<Pair<String, Boolean>>> p = getSqlCallback.get();
			if (!StringUtils.equals(p.getLeft(), lastView) || !StringUtils.equals(p.getMiddle(), lastWhere) || !Objects.equals(p.getRight(), lastOrderby)) {
				lastView = p.getLeft();
				lastWhere = p.getMiddle();
				lastOrderby = p.getRight();
				gipi.setBiResult(BiResultHelper.create(sh, lastView, gipi.getBiResult(), lastWhere, null, -1, lastOrderby));
				gipiForm.jxAdd("pickListBox").setItemListInterface(gipi);
			}
		};
	}
	
	public static class PickByTableTrForm {
		private JxSelOpt gipiForm;
		private TrGetItemProperty gipi;
		private CheckedSupplier3<SelectUtil, String, Wherecl> getSqlCallback;
		private String lastSqlSelect;
		private Wherecl lastSqlWhere;

		public PickByTableTrForm(SessionHelper sh, String[] fieldList, String[] widthList, CheckedSupplier3<SelectUtil, String, Wherecl> getSqlCb, CheckedConsumer3<Object[], TableRec, Object> cb) {
			JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sh.getSessionData("jxzkgadgetprovider");
			gipi = new TrGetItemProperty(Lists.newArrayList(fieldList), null, widthList != null ? Lists.newArrayList(widthList) : null);
			gipiForm = JxSelOpt.createJxSelOpt(pvdr);
			gipiForm.setOnSelectAction(fd -> {
				try {
					Object[] rec = (Object[]) fd.getValue();
					TableRec tr = gipi.getTableRec();
					cb.accept(rec, tr, gipiForm.getUserData());
				} catch (Exception ex) {
					UniLog.log(ex);
				}
				gipiForm.closeForm();
			});
			getSqlCallback = getSqlCb;
		}

		public PickByTableTrForm(SessionHelper sh, String[] fieldList, CheckedSupplier3<SelectUtil, String, Wherecl> getSqlCb, CheckedConsumer3<Object[], TableRec, Object> cb) {
			this(sh, fieldList, null, getSqlCb, cb);
		}
		
		public void bindComponent(ZkJxPickInput pickComp, Object userData, boolean openRefill) throws Exception {
			pickComp.setJxZkForm(gipiForm);
			gipiForm.setUserData(userData);
			if (openRefill)
				setEventListener(pickComp, Events.ON_OPEN, openEvent);
			else
				openEvent.onEvent(null);
		}
		
		private EventListener<Event> openEvent = event -> {
			Triple<SelectUtil, String, Wherecl> p = getSqlCallback.get();
			if (!StringUtils.equals(p.getMiddle(), lastSqlSelect) || !Objects.equals(p.getRight(), lastSqlWhere)) {
				lastSqlSelect = p.getMiddle();
				lastSqlWhere = p.getRight();
				gipi.setTableRec(p.getLeft().getQueryResult(lastSqlSelect, lastSqlWhere));
				gipiForm.jxAdd("pickListBox").setItemListInterface(gipi);
			}
		};
	}
	
	
	public static class PickByListForm {
		private JxSelOpt gipiForm;
		private ListGetItemProperty gipi;
		private ZkJxPickInput pickComp;
		private String[][] lastValueList;

		private CheckedSupplier<String[][]> getValueCallback;

		public PickByListForm(SessionHelper sh, String[] widthList, CheckedSupplier<String[][]> getValueCb, CheckedConsumer2<String[], Object> cb) {
			JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sh.getSessionData("jxzkgadgetprovider");
			gipi = new ListGetItemProperty();
			gipi.setWidthList(Lists.newArrayList(widthList));
			gipiForm = JxSelOpt.createJxSelOpt(pvdr);
			gipiForm.setOnSelectAction(fd -> {
				try {
					String[] rec = (String[]) fd.getValue();
					cb.accept(rec, gipiForm.getUserData());
				} catch (Exception ex) {
					UniLog.log(ex);
				}
				gipiForm.closeForm();
			});
			getValueCallback = getValueCb;
		}

		public void bindComponent(ZkJxPickInput pickComp, Object userData, boolean openRefill) throws Exception {
			this.pickComp = pickComp;
			pickComp.setJxZkForm(gipiForm);
			gipiForm.setUserData(userData);
			if (openRefill)
				setEventListener(pickComp, Events.ON_OPEN, openEvent);
			else
				openEvent.onEvent(null);
		}
		
		public ZkJxPickInput getPickComp() {
			return pickComp;
		}

		private EventListener<Event> openEvent = event -> {
			String[][] valueList = getValueCallback.get();
			if (!Arrays.deepEquals(valueList, lastValueList)) {
				gipi.setValueList(Arrays.stream(valueList).collect(Collectors.toList()));
				gipiForm.jxAdd("pickListBox").setItemListInterface(gipi);
				lastValueList = valueList;
			}
		};
	}
	
	/***
	 * backup a input stream
	 * @param p_folderName
	 * @param p_fileName
	 * @param p_is
	 * @return
	 */
	public static ReturnMsg backupFile(String p_folderName, String p_fileName, InputStream p_is) {
		try {
			
			String folderName = p_folderName;
			if (StringUtils.isBlank(p_folderName)) {
				folderName = "/yic/tmp/bibk";
			}
			String fileName = p_fileName;
			if (StringUtils.isBlank(p_fileName)) {
				fileName = "noname.dat";
			}
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			String nowStr = sdf.format(new Date());
			String bkFileName = String.format("%s/%s/%s/%s/%s_%s",
					folderName,
					StringUtils.substring(nowStr,0,4),
					StringUtils.substring(nowStr,4,6),
					StringUtils.substring(nowStr,6,8),
					nowStr, fileName);
			UniLog.log1("folderName:%s fileName:%s bkFileName:%s", folderName, fileName, bkFileName);

			FileUtils.copyInputStreamToFile(p_is, new File(bkFileName));
			return ReturnMsg.defaultOk;
		}
		catch(Exception ex) {
			UniLog.log1("error:" + ex.getMessage());
			return new ReturnMsg(ex);
		}
	}

	/*
	 * Theme 01 garyu 牙龍
	 * Theme 02 rohsha 弄奢
	 */
	
//	public static JSONObject getJsonFromFilingMulti(SessionHelper p_sh,String p_tabName,String p_key,String p_contentKey) {
//		JSONObject jo = null;
//		if(p_contentKey == null) {
//			try {
//				jo = FilingUtil.getJson(p_sh.getAgent(), p_tabName, p_key);
//				if(jo != null) return(jo);
//			} catch (Exception ex1) {
//				UniLog.log("getJsonFromFilingMulti(SessionHelper get single json failed");
//			}
//		}
//		try {
//			JSONArray ja = FilingUtil.getJsonArray(p_sh.getAgent(), p_tabName, p_key);
//			if(ja != null ) {
//				if(ja.length() < 1) return(null);
//				for(int i=0;i<ja.length();i++) {
//					jo = ja.getJSONObject(i);
//					String contentKey = jo.optString("contentKey");
//					if((StringUtils.isBlank(p_contentKey) && StringUtils.isBlank(contentKey)) ||
//						(p_contentKey.equals(contentKey))
//							) {
//						return (jo.getJSONObject("contentObject"));
//					}
//				}
//			if(ja.length() == 1) {
//				return(jo.getJSONObject("jsonContent"));
//			} else {
//					return(null);
//				}
//			}
//		} catch (Exception ex) {
//			UniLog.log("getJsonFromFilingMulti(SessionHelper get json array failed");
//		}	
//		if(StringUtils.isBlank(p_contentKey)) {
//			try {
//				jo = FilingUtil.getJson(p_sh.getAgent(), p_tabName, p_key);
//				if(jo != null) return(jo);
//			} catch (Exception ex1) {
//				UniLog.log("getJsonFromFilingMulti(SessionHelper get json from array failed");
//			}
//			return(null);
//		} else {
//			return(null);
//		}
//	}
//	public static void putJsonFromFilingMulti(SessionHelper p_sh,String p_tabName,String p_key,String p_contentKey,JSONObject p_contentObject) throws Exception {
//		FilingUtil.storeJson(p_sh.getAgent(), p_tabName, p_key, null, null, p_contentObject);
//	}
	public static ReturnMsg comboboxDialogZkForm(JSONObject p_jo,ArrayList<String> itemlist,ZkfAction p_action)  {
		ZkForm zkf = new ZkForm(null,"/zkf/modal/ComboboxDialog.zul");
		CellCollection col = new CellCollection();
		try {
			Iterator itr = p_jo.keys();
			while(itr.hasNext()) {
				String key = itr.next().toString();
				Object jo = p_jo.get(key);
				col.addCell(key, new Cell(jo));
			}
//			col.addCell("cbInput", new Cell("",Cell.VMODE_NORMAL,p_itemList));
			if(itemlist != null) {
				Combobox cb = (Combobox) zkf.getComponent("cbInput");
				if(cb != null) {
					int n = cb.getItemCount();
					for(int i = n-1;i>=0;i--) {
						cb.removeItemAt(i);
					}
					for(int i = 0;i<itemlist.size();i++) {
						Object o = itemlist.get(i);
						if(o instanceof Pair) {
							Comboitem ci = cb.appendItem(((Pair) o).getRight().toString());
							ci.setValue(o);
						} else {
							Comboitem ci = cb.appendItem(o.toString());
							ci.setValue(o);
						}
					}	
				}
			}
			zkf.doModal(col,new EventListener() {
				@Override
				public void onEvent(Event arg0) throws Exception {
//					if(arg0.getTarget().getId().equals("btOK")) {
						ReturnMsg rtn = p_action.processAction(arg0.getTarget().getId(), null, col, null, null, arg0.getTarget());
						if(rtn == null || rtn.getStatus()) zkf.exitModal();
//					}
				};
			}
			);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return(ReturnMsg.defaultOk);
	}
		
}

