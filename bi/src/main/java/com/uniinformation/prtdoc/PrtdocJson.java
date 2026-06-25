package com.uniinformation.prtdoc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.kyoko.common.ChineseConvert;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.prtdoc.PrtdocPerfJson;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.webcore.SessionHelper;

public abstract class PrtdocJson {
	public static enum Encoding{ MS950_HKSCS,UTF8};
	public static enum AttrName { ATTR_bold, ATTR_underline,ATTR_hideOnFirstPage,ATTR_hideOnMiddlePage,ATTR_hideOnLastPage};
	abstract public ReturnMsg toPdfStream(OutputStream os,SessionHelper sh) throws Exception ;
	abstract public ReturnMsg toExcelStream(OutputStream os,SessionHelper sh) throws Exception ;
	protected String paperType;
	protected JSONObject jRoot;
	protected JSONObject jContent;
	protected JSONArray jHeaderFields;
	protected JSONArray jBottomFields;
	protected JSONArray jDetailEndFields;
	protected JSONObject jDetail;
	protected JSONArray jDetailHeaderFields;
	protected JSONArray jDetailRecords;
	protected JSONArray jDetailRecordFields;
	protected JSONArray contentList;
	protected HashSet<AttrName> attrSet=new HashSet<AttrName>();
	protected Encoding encode=Encoding.MS950_HKSCS;
	protected int topLeftMargin = -1;
	protected boolean skipB2GConvert = false;
	
	
	public void setSkipB2GConvert(boolean p_sw) {
		skipB2GConvert = p_sw;
	}
	public void newContent() throws JSONException {
		if(jContent == null) {
			jContent = new JSONObject();
    		jRoot.put("content", jContent);
		} else {
    		if(contentList == null) {
    			contentList = new JSONArray();
    			jRoot.put("contentList", contentList);
    			jRoot.remove("content");
    			contentList.put(jContent);
    		}
			jContent = new JSONObject();
    		contentList.put(jContent);
		}
    	jHeaderFields = new JSONArray();
    	jContent.put("header", jHeaderFields);
    	
    	jDetailEndFields = new JSONArray();
    	jContent.put("detailend", jDetailEndFields);
    	jBottomFields = new JSONArray();
    	jContent.put("bottom", jBottomFields);
    	
    	jDetail = new JSONObject();
    	jContent.put("detail", jDetail);
    	
    	jDetailRecords = new JSONArray();
    	jDetail.put("records", jDetailRecords);
    	jDetailHeaderFields = new JSONArray();
    	jDetail.put("header", jDetailHeaderFields);
	}
	
	public PrtdocJson(String p_paperType,String p_docname,String p_callsegment,Encoding p_encoding) throws JSONException {
		encode = p_encoding;
		jRoot = new JSONObject();
    	jRoot.put("callsegment", p_callsegment);
    	jRoot.put("prtdocname",  p_docname);
    	paperType = p_paperType;
//    	jContent = new JSONObject();
    	/*
    	if(isMultidoc) {
    		contentList = new JSONArray();
    		jRoot.put("contentList", contentList);
    		newContent();
    	} else {
    		newContent();
    		jRoot.put("content", jContent);
    	}
    	*/
    	newContent();

//    	jHeaderFields = new JSONArray();
//    	jContent.put("header", jHeaderFields);
//    	
//    	jBottomFields = new JSONArray();
//    	jContent.put("bottom", jBottomFields);
//    	
//    	jDetail = new JSONObject();
//    	jContent.put("detail", jDetail);
//    	
//    	jDetailRecords = new JSONArray();
//    	jDetail.put("records", jDetailRecords);
//    	jDetailHeaderFields = new JSONArray();
//    	jDetail.put("header", jDetailHeaderFields);
	}

	public void toJsonStream(OutputStream os,boolean p_close) throws JSONException,IOException {
		/*
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Writer wtr = new OutputStreamWriter(bos,"MS950_HKSCS");
		jRoot.write(wtr);
		if(p_close) wtr.close();
		byte[] bs = bos.toByteArray();
		bos.close();
		boolean isBig5 = false;
		for(byte b :bs) {
			if(isBig5) {
				if(b == 92) {
					os.write(b);
				}
				isBig5 = false;
			} else {
				if(b < 0) isBig5 = true;
			}
			os.write(b);
		}
		os.flush();
		os.close();
		*/
		Writer wtr;
		switch(encode) {
		case UTF8:
				wtr = new OutputStreamWriter(os,"UTF-8");
				break;
		default: wtr = new OutputStreamWriter(os,encode.name());
		}
//		if(encode == Encoding.ENCODE_MS950) wtr = new OutputStreamWriter(os,"MS950_HKSCS"); else wtr = new OutputStreamWriter(os,"UTF-8");
		jRoot.write(wtr);
		if(p_close) wtr.close();
	}
	public void useDetailGroup(String p_detailGroup) throws JSONException {
		JSONObject jo = new JSONObject();
		jo.put("detailGroup",p_detailGroup);
		jDetailRecords.put(jo);
	}
	public void addDetailRecord() {
		jDetailRecordFields= new JSONArray();
		jDetailRecords.put(jDetailRecordFields);
	}
	
	
	void addField(Object p_parent,String p_name,String p_tag,String p_output,String p_format,String p_image,int p_x,int p_y,int p_h, int p_w) throws JSONException {
		JSONObject jField = new JSONObject();
		jField.put("tag", p_tag);
		if(p_output != null) {
			if(encode == Encoding.MS950_HKSCS && (!skipB2GConvert)) {
				/*
				 * Use MS950 to encode output, ensure that the print string is MS950 compatible
				 */
				jField.put("output", ChineseConvert.convertAuto2Bnew(p_output));
			} else jField.put("output", p_output);
		}
		if(p_format != null) jField.put("format", p_format);
		if(p_image != null) jField.put("image", p_image);
		if(p_x != 0) jField.put("x", p_x);
		if(p_y != 0) jField.put("y", p_y);
		if(p_w != 0) jField.put("w", p_w);
		if(p_h != 0) jField.put("h", p_h);
		if(!attrSet.isEmpty()) {
			if(attrSet.contains(AttrName.ATTR_bold)) {
				jField.put("bold", true);
			}
			if(attrSet.contains(AttrName.ATTR_underline)) {
				jField.put("underline", true);
			}
			int hpmask = 0;
			if(attrSet.contains(AttrName.ATTR_hideOnFirstPage)) hpmask |= 1;
			if(attrSet.contains(AttrName.ATTR_hideOnMiddlePage)) hpmask |= 2;
			if(attrSet.contains(AttrName.ATTR_hideOnLastPage)) hpmask |= 4;
			if(hpmask != 0) jField.put("hidepg", hpmask);
		}
		if(p_parent instanceof JSONArray) ((JSONArray) p_parent).put(jField);
		if(p_parent instanceof JSONObject) ((JSONObject) p_parent).put(p_name,jField);
	}
	public void addHeaderField(String p_tag,String p_output) throws JSONException {
		addField(jHeaderFields,null,p_tag,p_output,null,null,0,0,0,0);
	}
	public void addHeaderField(String p_tag,String p_output,int p_x, int p_y) throws JSONException {
		addField(jHeaderFields,null,p_tag,p_output,null,null,p_x,p_y,0,0);
	}
	public void addHeaderField(String p_tag,String p_output,int p_x,int p_y,int p_h, int p_w) throws JSONException {
		addField(jHeaderFields,null,p_tag,p_output,null,null,p_x,p_y,p_h, p_w);
	}
	public void addPageNo(String p_tag,String p_format,int p_x,int p_y,int p_h) throws JSONException {
		addField(jContent,"pageno",p_tag,null,p_format,null,p_x,p_y,p_h, 0);
	}
	public void addDetailHeaderField(String p_tag,String p_output) throws JSONException {
		addField(jDetailHeaderFields,null,p_tag,p_output,null,null,0,0,0,0);
	}
	public void addDetailHeaderField(String p_tag,String p_output,int p_x,int p_y) throws JSONException {
		addField(jDetailHeaderFields,null,p_tag,p_output,null,null,p_x,p_y,0,0);
	}
	public void addDetailRecordField(String p_tag,String p_output) throws JSONException {
		addField(jDetailRecordFields,null,p_tag,p_output,null,null,0,0,0,0);
	}
	public void addDetailRecordField(String p_tag,String p_output,int p_x,int p_y) throws JSONException {
		addField(jDetailRecordFields,null,p_tag,p_output,null,null,p_x,p_y,0, 0);
	}
	public void addDetailRecordField(String p_tag,String p_output,int p_x,int p_y,int p_h, int p_w) throws JSONException {
		addField(jDetailRecordFields,null,p_tag,p_output,null,null,p_x,p_y,p_h, p_w);
	}
	public void addDetailRecordImage(String p_tag,String p_image,int p_x,int p_y,int p_h, int p_w) throws JSONException {
		addField(jDetailRecordFields,null,p_tag,null,null,p_image,p_x,p_y,p_h, p_w);
	}

	public void addHeaderImage(String p_tag,String p_image,int p_x,int p_y,int p_h, int p_w) throws JSONException {
		addField(jHeaderFields,null,p_tag,null,null,p_image,p_x,p_y,p_h, p_w);
	}
	
	public void setTrailerAtLastPageOnly(boolean p_sw) throws JSONException {
		jContent.put("trailerAtLastPage", p_sw);
	}

	public void addBottomField(String p_tag,String p_output) throws JSONException {
		addField(jBottomFields,null,p_tag,p_output,null,null,0,0,0,0);
	}
	public void addBottomField(String p_tag,String p_output,int p_x, int p_y) throws JSONException {
		addField(jBottomFields,null,p_tag,p_output,null,null,p_x,p_y,0,0);
	}
	public void addBottomField(String p_tag,String p_output,int p_x, int p_y,int p_h,int p_w) throws JSONException {
		addField(jBottomFields,null,p_tag,p_output,null,null,p_x,p_y,p_h,p_w);
	}
	public void addBottomImage(String p_tag,String p_image,int p_x,int p_y,int p_h, int p_w) throws JSONException {
		addField(jBottomFields,null,p_tag,null,null,p_image,p_x,p_y,p_h, p_w);
	}
	public void addDetailEndField(String p_tag,String p_output) throws JSONException {
		addField(jDetailEndFields,null,p_tag,p_output,null,null,0,0,0,0);
	}
	public void addDetailEndField(String p_tag,String p_output,int p_x, int p_y) throws JSONException {
		addField(jDetailEndFields,null,p_tag,p_output,null,null,p_x,p_y,0,0);
	}
	public void addDetailEndField(String p_tag,String p_output,int p_x, int p_y,int p_h,int p_w) throws JSONException {
		addField(jDetailEndFields,null,p_tag,p_output,null,null,p_x,p_y,p_h,p_w);
	}
	public void addDetailEndImage(String p_tag,String p_image,int p_x,int p_y,int p_h, int p_w) throws JSONException {
		addField(jDetailEndFields,null,p_tag,null,null,p_image,p_x,p_y,p_h, p_w);
	}
	public void setBold(boolean p_sw) {
		if(p_sw) {
			attrSet.add(AttrName.ATTR_bold);
		} else {
			attrSet.remove(AttrName.ATTR_bold);
		}
	}
	public void setUnderLine(boolean p_sw) {
		if(p_sw) {
			attrSet.add(AttrName.ATTR_underline);
		} else {
			attrSet.remove(AttrName.ATTR_underline);
		}
	}
	
	public void setAttribute(AttrName p_attr) {
		attrSet.add(p_attr);
	}
	public void unsetAttribute(AttrName p_attr) {
		attrSet.remove(p_attr);
	}
	
	public void setEncoding(Encoding p_encode) {
		encode = p_encode;
	}
	
	static public PrtdocJson newPrtdocJson(String p_cocode,String p_paperType,String p_docCode,String p_callSegment) throws Exception {
		return ( new PrtdocPerfJson(p_cocode,p_paperType,p_docCode,p_callSegment,Encoding.MS950_HKSCS));
	}
	static public PrtdocJson newPrtdocJson(String p_cocode,String p_paperType,String p_docCode,String p_callSegment,Encoding p_encoding) throws Exception {
		return ( new PrtdocPerfJson(p_cocode,p_paperType,p_docCode,p_callSegment,p_encoding));
	}
	
	static public void addMultiHeaderField(PrtdocJson ppj,String p_tag,int p_x,int p_y,int p_h,int p_w, List<String> p_values) throws JSONException {
		int ofs=0;
		for(String val : p_values) {
			ppj.addHeaderField(p_tag,val,p_x,p_y+ofs,p_h, p_w);
			ofs += p_h;
		}
	}
	
	static public List<String> joinAndSplit(String eTypeFace,String cTypeFace,float pointSize,int chnftrWidth,String ...strings ) {
		String ss = null;
		for(String s : strings) {
			if(s != null && !s.trim().isEmpty()) {
				if(ss == null) ss = s.trim(); else ss = ss + " " + s.trim();
			}
		}
		return(ChnftrParser.splitText(ss, eTypeFace, cTypeFace, pointSize, chnftrWidth));
	}
	
	public void setTopLeftMargin(int p_m) {
		topLeftMargin = p_m;
	}
}
