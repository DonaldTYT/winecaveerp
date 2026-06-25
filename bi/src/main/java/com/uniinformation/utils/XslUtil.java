package com.uniinformation.utils;
import java.io.*;
import java.math.*;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.stream.*;

import com.kyoko.common.StringUtil;
import com.uniinformation.rpccall.*;

public class XslUtil implements RpcServlet {
   String orgFilename = null;
	String xslUrl = null;
	String xslString = null;
	InputStream xslStream = null;
   TransformerFactory factory = null;
	Templates stylesheet = null;
	private RpcServerConnection conn = null;
	public XslUtil() {
	}
	public XslUtil(int p_type, String p_string, String p_orgFilename) throws XslUtilException {
	   super();
	   setup(p_type, p_string, p_orgFilename);
	}
	public XslUtil(InputStream p_xslStream, String p_orgFilename) throws XslUtilException {
		orgFilename = p_orgFilename;
	   xslStream = p_xslStream;
	   setupFactory();
	}
	private void setupFactory() throws XslUtilException {
      try {
         factory = TransformerFactory.newInstance();
      } catch (Exception ex) {
			UniLog.log(ex);
			throw(new XslUtilException(ex.toString()));
		}
	}
	public void setup(int p_type, String p_string, String p_orgFilename) throws XslUtilException {
		orgFilename = p_orgFilename;
	   if (p_type == 0)
	      xslUrl = p_string;
		else
	      xslString = p_string;
	   setupFactory();
	}
	public static XslUtil getByUrl(String p_xslUrl) throws XslUtilException {
	   return(new XslUtil(0, p_xslUrl, p_xslUrl));
	}
	public static XslUtil getByString(String p_xslString, String p_orgFilename) throws XslUtilException {
	   return(new XslUtil(1, p_xslString, p_orgFilename));
	}
	public static XslUtil getByInputStream(InputStream p_xslStream, String p_orgFilename) throws XslUtilException {
	   return(new XslUtil(p_xslStream, p_orgFilename));
	}
	synchronized public XslUtil prepare() throws XslUtilException {
		if (factory == null)
		   throw(new XslUtilException("factory is null"));
		try {
			if (xslUrl != null)
	         stylesheet = factory.newTemplates(new StreamSource(xslUrl));
	      else if (xslString != null)
	         stylesheet = factory.newTemplates(new StreamSource(new StringReader(xslString)));
	      else if (xslStream != null)
	         stylesheet = factory.newTemplates(new StreamSource(xslStream));
		   else
			   throw(new Exception("XSL stylesheet not properly setup"));
		} catch (Exception ex) {
			UniLog.log(ex);
			throw(new XslUtilException(ex.toString()));
		}
	   return(this);
	}
	private Reader saveInput(Reader p_xmlInput) {
		if (orgFilename == null)
		   return(p_xmlInput);
		String basename = StringUtil.basename(orgFilename);
		int len = basename.length();
		if (len > 4 &&
		    basename.substring(len-4).toLowerCase().equals(".xsl"))
		basename = basename.substring(0, len-4);
		basename = StringUtil.strpart(basename, 0, 200);
		String filename = SiteConfig.getSiteConfig().getParameter("XslInputSampleDir")
		                + (SiteConfig.getSiteConfig().getParameter("ServerType").equals("Linux") ? "/" : "\\")
		                + basename
							 + ".xml";
		if (SiteConfig.getSiteConfig().getParameter("ServerType").equals("Windows"))
			filename = filename.replace('?', '_');
	   if (new File(filename).canRead())
		   return(p_xmlInput);
		try {
			FileWriter writer = new FileWriter(filename);
	      ReaderWriterUtil.copy(p_xmlInput, writer);
			writer.close();
			return(new FileReader(filename));
		} catch (IOException ex) {
			UniLog.log(ex);
		   return(null);
		}
	}
	synchronized public XslUtil transform(Reader p_xmlInput, Writer p_resultWriter) throws XslUtilException {
	   if (stylesheet == null)
		   throw(new XslUtilException("stylesheet is null"));
		try {
		   Reader xmlInput = saveInput(p_xmlInput);
         Transformer transformer = stylesheet.newTransformer();
			transformer.transform(new StreamSource(xmlInput), new StreamResult(p_resultWriter));
		} catch (Exception ex) {
			UniLog.log(ex);
			throw(new XslUtilException(ex.toString()));
		}
	   return(this);
	}
	synchronized public XslUtil close() {
	   return(this);
	}
	/*
	synchronized StylesheetRoot xlsPrepare(String p_xslfile, XSLTProcessor p_processor) throws Exception {
		if (xlsRoot == null) {
	      FileReader fr = null;
		   StringWriter sw = null;
		   try {
		      fr = new FileReader(new File(p_xslfile));
		      sw = new StringWriter();
            ReaderCopier.copy(fr, sw);
		      StringReader sr = new StringReader(sw.toString());
            xlsRoot = p_processor.processStylesheet(new XSLTInputSource(sr));
				return(xlsRoot);
		   } catch (Exception e) {
		      e.printStackTrace();
		   } finally {
			   if (fr != null) fr.close();
			   if (sw != null) sw.close();
			}
	   }
		return(null);
	}
	public String Process(String p_xmlstring) {
      StylesheetRoot tmpXlsRoot;
		StringWriter sw = new StringWriter();
		try {
         XSLTProcessor xslprocessor = XSLTProcessorFactory.getProcessor();
	      tmpXlsRoot = xlsPrepare(xslfile, xslprocessor);
         xslprocessor.setStylesheet(tmpXlsRoot);
         xslprocessor.process(
			         new XSLTInputSource(new StringReader(p_xmlstring)),
						null,
                  new XSLTResultTarget(sw));
		} catch (Exception e) {
		   e.printStackTrace();
		   return(null);
		}
		return(sw.toString());
	}
	public void close() {
	}
	*/
	/* for RpcServlet Interface started */
	public void setConnection(RpcServerConnection p_conn) {
		conn = p_conn ;
		conn.setDebug(false);
	}
	public void init_servlet() {
	}
	public void close_servlet() {
      clear();
	}
	/* for RpcServlet Interface ended */

	/* rpcservice started */
	public String ping() {
		return("OK  ");
	}
   public String clear() {
      orgFilename = null;
	   xslUrl = null;
	   xslString = null;
      factory = null;
	   stylesheet = null;
		return("OK  ");
	}
	public String setupXslString(String p_string, String p_orgFilename) {
	   try {
	      setup(1, p_string, p_orgFilename);
			return("OK  ");
		} catch (Exception ex) {
		   UniLog.log(ex);
			return("FAIL"+ex.toString());
		}
	}
	public String getTransformResult(String p_xmlstring) {
	   try {
	      if (stylesheet == null)
			   prepare();
		   StringWriter sw = new StringWriter();
		   StringReader sr = new StringReader(p_xmlstring);
	      transform(sr, sw);
	      return("OK  "+sw.toString());
		} catch (Exception ex) {
		   UniLog.log(ex);
			return("FAIL"+ex.toString());
		}
	}
	/* rpcservice ended */
	/*
	public static void main(String[] args) throws Exception {
		UniLog.log("haha:0");
		XslUtil xslUtil = XslUtil.getByUrl(args[0]);
		UniLog.log("haha:1");
	   xslUtil.prepare();
		UniLog.log("haha:2");
		xslUtil.transform(new FileReader(args[1]), new OutputStreamWriter(System.out));
		UniLog.log("haha:3");
		xslUtil.transform(new FileReader(args[1]), new OutputStreamWriter(System.out));
		UniLog.log("haha:4");
	}
	*/
	public static void main(String[] args) throws Exception {
		XslUtil xslUtil = XslUtil.getByInputStream(new FileInputStream(args[0]), args[0]);
	   xslUtil.prepare();
		/*
		xslUtil.transform(new FileReader(args[1]), 
		     new OutputStreamWriter(System.out));
		*/
		StringWriter sw = new StringWriter();
		xslUtil.transform(new FileReader(args[1]), sw);
UniLog.log("trace:00 sw="+sw.toString());
	}
}
