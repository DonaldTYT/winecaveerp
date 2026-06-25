package com.uniinformation.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

/***
 *
 * Step to add require lib:
 * ========================
 * project properties -> libraries -> add jar -> select all jar under unidev/lib/pdfbox
 *
 * unidev/lib/pdfbox/pdfbox-2.0.4.jar
 * unidev/lib/pdfbox/pdfbox-tools-2.0.4.jar
 * unidev/lib/pdfbox/fontbox-2.0.4.jar
 * unidev/lib/pdfbox/commons-logging-1.2.jar
 * unidev/lib/pdfbox/bcpkix-jdk15on-156.jar
 * unidev/lib/pdfbox/bcmail-jdk15on-156.jar
 * unidev/lib/pdfbox/bcprov-jdk15on-156.jar
 * 
 *
 */
public class PdfBoxUtil {
	
	/***
	 * convert pdf to image 
	 * status log: 
	 *    170214 - probably need to handle font missing problem
	 *           - List<byte[]> may cause out of memory problem
	 * 
	 * @param p_is pdf file input stream
	 * @param p_dpi output resolution
	 * @param p_imgType output bitmap type, e.g. BINARY, RGB, ARGB
	 * @return List of bitmap byte array
	 */
	public static List<byte[]> pdfToBmpList(InputStream p_is, float p_dpi, ImageType p_imgType, int startPage, int endPage){
		ArrayList<byte[]> resultList = new ArrayList<byte[]>();
		try{
		
			PDDocument document = PDDocument.load(p_is);
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			for (int i = startPage; i <= endPage && i < document.getNumberOfPages(); i++)
			{ 
				UniLog.log("pdfToBmpList: processing idx:" +i);
			    BufferedImage img = pdfRenderer.renderImageWithDPI(i, p_dpi, p_imgType);
			    ByteArrayOutputStream bos = new ByteArrayOutputStream();
			    ImageIOUtil.writeImage(img, "BMP", bos);
			    resultList.add(bos.toByteArray());
			    bos.close();
			}
			document.close();
			return(resultList);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		finally{
			UniLog.log("HAHA close is");
			try{ p_is.close(); } catch(Exception ex){}
		}
		return null;
	}
	public static List<byte[]> pdfToBmpList(InputStream p_is, float p_dpi, ImageType p_imgType){
		return pdfToBmpList(p_is, p_dpi, p_imgType, 0, Integer.MAX_VALUE);
	}
	/***
	 * merge pdfs file
	 * @param p_os
	 * @param p_iss
	 */
	public static void mergePdf(OutputStream p_os, InputStream... p_iss){
		if (p_iss == null){
			UniLog.log("mergePdf: ignore no is");
			return;
		}
		try{
			PDFMergerUtility mu = new PDFMergerUtility();
			for (int i=0; i<p_iss.length; i++){
				UniLog.log("mergePdf: process:" + i);
				mu.addSource(p_iss[i]);
			}
			mu.setDestinationStream(p_os);
			mu.mergeDocuments(null);
			UniLog.log("mergePdf: done");
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		finally{
			try{ p_os.close(); } catch(Exception ex){}
			for (int i=0; i<p_iss.length; i++){
				try{ p_iss[i].close();} catch(Exception ex){}
			}
		}
	}
	public static void main(String args[]) throws Exception{
		//List<byte[]> baList = PdfBoxUtil.pdfToBmpList(new FileInputStream("c:/tmp/test_mp.pdf"), 300, ImageType.RGB);
		//List<byte[]> baList = PdfBoxUtil.pdfToBmpList(new FileInputStream("c:/tmp/report.pdf"), 300, ImageType.RGB);
		//List<byte[]> baList = PdfBoxUtil.pdfToBmpList(new FileInputStream("c:/tmp/37713-C.pdf"), 300, ImageType.BINARY); 
		/*
		List<byte[]> baList = PdfBoxUtil.pdfToBmpList(new FileInputStream("c:/tmp/37713-C.pdf"), 300, ImageType.RGB); 
		for (int i=0; i<baList.size(); i++){
			FileOutputStream fos = new FileOutputStream("c:/tmp/out."+i+".bmp");
			fos.write(baList.get(i));
		}
		*/
		PdfBoxUtil.mergePdf(new FileOutputStream("c:/tmp/merge.pdf"), new FileInputStream("/tmp/a1.pdf"), new FileInputStream("/tmp/37713-C.pdf"), new FileInputStream("/tmp/a2.pdf"));
		
		
	}
	
}
