package com.uniinformation.utils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Hashtable;
import java.util.Vector;

import com.thoughtworks.xstream.XStream;
import com.uniinformation.utils.UniLog;


public class XStreamUtil {
	public static void objToXML(Object p_obj, File p_file) throws Exception{
		objToXML(p_obj, p_file, null);
	}
	public static String objToXMLString(Object p_obj, Vector<String> p_opts) throws Exception{
		XStream xstream = new XStream();
		if (p_opts != null){
			for (int i=0; i<p_opts.size();i++){
				if (p_opts.elementAt(i).equals("omitField")){
					xstream.omitField( Class.forName(p_opts.elementAt(i+1)), p_opts.elementAt(i+2));
					i+=2;
				}
			}
		}
		return(xstream.toXML(p_obj));
	}
	public static void objToXML(Object p_obj, File p_file, Vector<String> p_opts) throws Exception{
		XStream xstream = new XStream();
		if (p_opts != null){
			for (int i=0; i<p_opts.size();i++){
				if (p_opts.elementAt(i).equals("omitField")){
					xstream.omitField( Class.forName(p_opts.elementAt(i+1)), p_opts.elementAt(i+2));
					i+=2;
				}
			}
		}
		/*
		//encoding problem
		xstream.toXML(p_obj, new FileWriter(p_file));
		*/
		Writer writer = null;
		try{
			Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(p_file), "UTF-8"));
			xstream.toXML(p_obj, out);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		finally{
			if (writer != null){
				try{ writer.close(); } catch(Exception ex2){}
			}
		}
	}
	public static Object xmlToObj(File p_file) throws Exception{
		XStream xstream = new XStream();
		xstream.ignoreUnknownElements();
		return(xstream.fromXML(p_file));
	}
	public static void test() throws Exception{
		//obj to xml
		Hashtable<String, Vector<StringBuffer>> h = new Hashtable<String,Vector<StringBuffer>>();
		Vector <StringBuffer> v = new Vector<StringBuffer>();
		v.addElement(new StringBuffer("sb1"));
		v.addElement(new StringBuffer("sb2"));
		v.addElement(new StringBuffer("sb3"));
		h.put("v", v);
		h.put("v2", v);  //point to same vector
		XStreamUtil.objToXML(h, new File("/tmp/a.xml"),null);
		
		//xml to obj
		Hashtable<String, Vector<StringBuffer>> h2 = (Hashtable<String, Vector<StringBuffer>>) XStreamUtil.xmlToObj(new File("/tmp/a.xml"));
		Vector<StringBuffer> v2 =  h2.get("v");
		for (StringBuffer sb : v2){
			UniLog.log("v2:"+":"+sb.hashCode() +":" + sb.toString());
		}	
		Vector<StringBuffer> v3 =  h2.get("v");
		for (StringBuffer sb : v3){
			UniLog.log("v3:"+":"+sb.hashCode() +":" + sb.toString());
		}	
	}
	
	public static void main(String args[]) throws Exception{
			XStreamUtil.test();
	}
}
