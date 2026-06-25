package com.uniinformation.utils;

import java.awt.Color;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.media.jai.operator.AWTImageDescriptor;

import com.uniinformation.itext.text.pdf.BarcodeQRCode;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
//import com.lowagie.text.pdf.parser.ImageRenderInfo;
//import com.lowagie.text.pdf.parser.PdfImageObject;
//import com.lowagie.text.pdf.parser.PdfReaderContentParser;
//import com.lowagie.text.pdf.parser.RenderListener;
//import com.lowagie.text.pdf.parser.TextRenderInfo;

public class ITextPdfUtil {
	/***
	 * 
	 * convert pdf to bmp
	 * status log: 
	 *    170214 - support image content only, for mixed content, please try PdfBoxUtil
	 * 
	 * @param p_is pdf file input stream
	 * @return list of bitmap array 
	 * @throws Exception
	 */
	/*public static List<byte[]> pdfToBmpList(InputStream p_is) throws Exception{
		List<byte[]> resultList = new ArrayList<byte[]>();
		try{
			PdfReader reader = new PdfReader(p_is);
			ITextPdfUtilRenderer renderer = new ITextPdfUtilRenderer(resultList);
			PdfReaderContentParser readerParser = new PdfReaderContentParser(reader);
			for(int i=1; i<=reader.getNumberOfPages(); i++){
				readerParser.processContent(i, renderer);
			}
			return(resultList);
		}
		catch(Exception ex){
			ex.printStackTrace();
			return(resultList);
		}
		finally{
			try{
				p_is.close();
			}
			catch(Exception ex){}
		}
	}*/
	
	/***
	 * nest class for rendering
	 */
	/*private static class ITextPdfUtilRenderer implements RenderListener{
		List<byte[]> baList = null;
		public ITextPdfUtilRenderer(List<byte[]> p_baList){
			baList = p_baList;
		}
		public void beginTextBlock() { }
		public void endTextBlock() { } 
		public void renderImage(ImageRenderInfo p_imageInfo) {
			UniLog.log("process renderImage:"+p_imageInfo.toString());
			try{
				PdfImageObject imageObject = p_imageInfo.getImage();
				baList.add(imageObject.getImageAsBytes());
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
		public void renderText(TextRenderInfo p_textInfo) {
			UniLog.log("ignore renderText:"+p_textInfo.toString() + " - " + p_textInfo.getText());
		}
		
		
	}*/
	public static void testQRCode() throws Exception
	{
		try {
			String begin="BEGIN:VEVENT";
			String summary="SUMMARY:QR code for calendar event, pure Java Code";
			String startdate="DTSTART;VALUE=DATE:20121109";
			String enddate="DTEND;VALUE=DATE:20121111";
			String location="LOCATION:Italy";
			String description="DESCRIPTION:Java, iText, QR Code, Example Code";
			String endevent="END:VEVENT";
			String finala=String.format("%s%n%s%n%s%n%s%n%s%n%s%n%s%n", begin, summary, startdate, enddate,location,description,endevent);

			UniLog.logm(null,"create qrcode");
			BarcodeQRCode my_code = new BarcodeQRCode(finala, 10, 10, null);

			UniLog.logm(null,"create awt image");
			Image qr_awt_image = my_code.createAwtImage(Color.BLACK,Color.WHITE);

			UniLog.logm(null,"convert raw image to png");
			ImageIO.write(AWTImageDescriptor.create(qr_awt_image,null), "png",new File("/tmp/testQRCode2.png"));
			UniLog.logm(null,"done");
		}
		catch (Exception e) {
			e.printStackTrace();
		}	
	}
	public static void encryptPdf(String src, String dest, String p_userPassword, String p_ownerPassword) throws Exception{
	    PdfReader reader = new PdfReader(src);
	    PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(dest));
	    stamper.setEncryption(p_userPassword.getBytes(), p_ownerPassword.getBytes(),
	        //PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_256 | PdfWriter.DO_NOT_ENCRYPT_METADATA); //itext v5 ok
	        PdfWriter.ALLOW_COPY | PdfWriter.ALLOW_PRINTING, PdfWriter.ENCRYPTION_AES_128); //itext v2 ok. com.lowagie.text_2.1.7.v201004222200.jar + bcprov-jdk15on-146
	    PdfDictionary infoDict = stamper.getReader().getTrailer().getAsDict(PdfName.INFO);
	    if (infoDict != null) {
            PdfString producer = (PdfString) infoDict.get(PdfName.PRODUCER);
            if (producer != null) {
                String producerStr = producer.toUnicodeString();
                infoDict.put(PdfName.PRODUCER, new PdfString(producerStr));
            }
        }
	    stamper.close();
	    reader.close();
	}

	public static void main(String args[]) throws Exception{
		/*
		List<byte[]> baList = pdfToBmpList(new FileInputStream("c:/tmp/test_mp.pdf"));
		for (int i=0; i<baList.size(); i++){
			FileOutputStream fos = new FileOutputStream("c:/tmp/out."+i+".bmp");
			fos.write(baList.get(i));
		}
		*/
		/*
		testQRCode();
		*/
		UniLog.log("HAHA1");
		encryptPdf("/tmp/a.pdf", "/tmp/a_e.pdf", "user", "owner");
		UniLog.log("HAHA2");
	}

}
