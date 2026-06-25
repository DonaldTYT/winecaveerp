package com.uniinformation.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.multi.GenericMultipleBarcodeReader;
import com.google.zxing.multi.qrcode.QRCodeMultiReader;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;


public class QRCodeUtil {
	/***
	 * decode multi qr code from image (zxing)
	 * @param p_bufferedImage
	 * @return
	 * @throws Exception
	 */
	public static List<String> decodeQRCode(BufferedImage p_bufferedImage) throws Exception{
	    LuminanceSource lSource = new BufferedImageLuminanceSource(p_bufferedImage);
	    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(lSource));

	    List<String> textList = new ArrayList<String>();
	    Map<DecodeHintType,Object> hints = new EnumMap<DecodeHintType,Object>(DecodeHintType.class);
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
	    hints.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.allOf(BarcodeFormat.class));
	    //hints.put(DecodeHintType.POSSIBLE_FORMATS, BarcodeFormat.QR_CODE);  //andrew190417: a bit faster if qrcode only

       	List<Result> results = new ArrayList<Result>();
    	Reader reader = new MultiFormatReader();
	    if (results.isEmpty()) {
	    	try {
	    		UniLog.log("decodeQRCode try0");
		    	//results.addAll(Arrays.asList(new GenericMultipleBarcodeReader(reader).decodeMultiple(bitmap, hints)));
	    		results.addAll(Arrays.asList(new QRCodeMultiReader().decodeMultiple(bitmap, hints)));
		    } catch (Exception e) {
		    	e.printStackTrace();
		    }
	    }
	    if (results.isEmpty()) {
	    	try {
	    		UniLog.log("decodeQRCode try1");
	    		//results.addAll(Arrays.asList(new QRCodeMultiReader().decodeMultiple(bitmap, hints)));
		    	results.addAll(Arrays.asList(new GenericMultipleBarcodeReader(reader).decodeMultiple(bitmap, hints)));
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    }
	    if (results.isEmpty()) {
	    	try {
	    		UniLog.log("decodeQRCode try2");
	    		results.add(reader.decode(bitmap, hints));
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    }
   		hints.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);
	    if (results.isEmpty()) {
	    	try {
	    		UniLog.log("decodeQRCode try3");
	    		results.add(reader.decode(bitmap, hints));
	    	} catch (Exception e) {
	    		e.printStackTrace();
	    	}
	    }

	    for (Result r : results)
	    	textList.add(r.getText());
	    return textList;
	}
	/***
	 * decode multi qr code from image base64 string (zxing)
	 * @param p_base64Image
	 * @return
	 * @throws Exception
	 */
	public static List<String> decodeQRCode(String p_base64Image) throws Exception{
		byte[] decoded = org.apache.commons.codec.binary.Base64.decodeBase64(p_base64Image);
		BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(decoded));
		return(decodeQRCode(bufferedImage));
	}
	
	/***
	 * decode multi qr code from pdf file (zxing) //TODO probably using itextpdf and zxing
	 * @param p_pdfFile
	 * @return
	 * @throws Exception
	 */
	public static List<String> decodeQRCode(File p_pdfFile) throws Exception{
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(p_pdfFile);
			return decodeQRCode(fis);
		} finally {
			if (fis != null)
				fis.close();
		}
	}
	/***
	 * decode multi qr code from pdf file (zxing) //TODO probably using itextpdf and zxing
	 * @param p_imgFile
	 * @return
	 * @throws Exception
	 */
	public static List<String> decodeImageFileQRCode(File p_imgFile) throws Exception {
		byte[] data = FileUtils.readFileToByteArray(p_imgFile);
		float angel = ChnftrParser.getExifOrientation(data);
		UniLog.logm(null,"angel:%f", angel);
		BufferedImage image = ImageIO.read(p_imgFile);
		if (angel != 0) {
			BufferedImage rImage = rotateImage(image, angel);
			if (rImage != null)
				image = rImage;
		}
		BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);      
		new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null).filter(image, grayImage);
		return decodeQRCode(grayImage);
	}
	/***
	 * decode multi qr code from pdf file (zxing) //TODO probably using itextpdf and zxing
	 * @param inStream
	 * @return
	 * @throws Exception
	 */
	public static List<String> decodeQRCode(InputStream inStream) throws Exception{
		List<String> textList = new ArrayList<String>();
		ByteArrayInputStream bis = null;
		try {
			//List<byte[]> dataList = PdfBoxUtil.pdfToBmpList(inStream, 300, ImageType.GRAY);
			List<byte[]> dataList = PdfBoxUtil.pdfToBmpList(inStream, 300, ImageType.RGB); //andrew220419 updated pdfbox from 2.0.4 to 2.0.25, use RGB improve detection rate
			for (byte[] data : dataList) {
				bis = new ByteArrayInputStream(data);
				BufferedImage bufferedImage = ImageIO.read(bis);
				textList.addAll(decodeQRCode(bufferedImage));
				bis.close();
			}
		} finally {
			if (bis != null)
				bis.close();
		}
		return textList;
	}
	public static BufferedImage rotateImage(BufferedImage src, float angel){
        try {
            int src_width = src.getWidth(null);
            int src_height = src.getHeight(null);
            
            int swidth = src_width;
            int sheight = src_height;
            
            if (angel == 90 || angel == 270){
                swidth = src_height;
                sheight = src_width;
            }
            Rectangle rect_des = new Rectangle(new Dimension(swidth, sheight));
            BufferedImage res = new BufferedImage(rect_des.width, rect_des.height,BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = res.createGraphics();
 
            g2.translate((rect_des.width - src_width) / 2, (rect_des.height - src_height) / 2);
            g2.rotate(Math.toRadians(angel), src_width / 2, src_height / 2);
            g2.drawImage(src, null, null);
            
            return res;
        } catch (Exception e) {
            e.printStackTrace();
        }  
        return null;
        
    }
	/***
	 * 
	 * @param p_format e.g. PNG
	 * @return
	 * @throws Exception
	 */
	public static byte[] createQRCode(String p_data, int p_width, int p_height, String p_fmt, Map<EncodeHintType, Object> p_hints) throws Exception{
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		BitMatrix bitMatrix;
		if (p_hints == null) {
			bitMatrix = qrCodeWriter.encode(p_data, BarcodeFormat.QR_CODE, p_width, p_height);
		}
		else {
			bitMatrix = qrCodeWriter.encode(p_data, BarcodeFormat.QR_CODE, p_width, p_height, p_hints);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		MatrixToImageWriter.writeToStream(bitMatrix, p_fmt, baos);
		baos.close();
		return baos.toByteArray();
	}
	public static byte[] createQRCode(String p_data, int p_width, int p_height, String p_fmt) throws Exception{
		return createQRCode(p_data, p_width, p_height, p_fmt, null);
	}
	
	/***
	 * read the qrcode from specific page
	 * it will be used for gbp game card card merge. it
	 * 
	 * Remark: 
	 * The pdf file is large (~100MB) and many pages(~64 pages)
	 * It need to concern about
	 * 
	 * - performance
	 * - memory allocation (allocation/free, file open/close...)
	 * 
	 * @param pdfFile - source pdf file
	 * @param p_startPage - for limit the lookup range. ignore if value < 0
	 * @param p_endPage - for limit the lookup range. ignore if value < 0
	 * @param p_isInvertImageColor - is invert image color flag
	 * @return
	 * @throws Exception
	 */
	public static List<String> decodeQRCode(File pdfFile, int p_startPage, int p_endPage, boolean p_isInvertImageColor) throws Exception {
		List<String> resultList = new ArrayList<String>();
		InputStream inStream = null;
		PDDocument document = null;
		try {
			inStream = new FileInputStream(pdfFile);
			document = PDDocument.load(inStream);
			if (p_startPage < 0) {
				p_startPage = 0;
			}
			if (p_endPage < 0) {
				p_endPage = document.getNumberOfPages() - 1;
			}
			UniLog.log1("startIdx:%d endIdx:%d", p_startPage, p_endPage);
			PDFRenderer pdfRenderer = new PDFRenderer(document);
			for (int i = p_startPage; i <= p_endPage && i < document.getNumberOfPages(); i++) { 
				UniLog.log1("decode page:%d", i);
			    //BufferedImage img = pdfRenderer.renderImageWithDPI(i, 300, ImageType.GRAY);
			    //BufferedImage img = pdfRenderer.renderImageWithDPI(i, 300, ImageType.RGB);  //andrew220419 updated pdfbox from 2.0.4 to 2.0.25, use RGB improve detection rate
			    BufferedImage img = pdfRenderer.renderImageWithDPI(i, 600, ImageType.RGB);  //andrew220419 updated pdfbox from 2.0.4 to 2.0.25, use RGB improve detection rate
			    if (p_isInvertImageColor) {
					for (int x = 0; x < img.getWidth(); x++) {
						for (int y = 0; y < img.getHeight(); y++) {
							int rgb = img.getRGB(x, y);
							Color col = new Color(rgb, true);
							col = new Color(255 - col.getRed(), 255 - col.getGreen(), 255 - col.getBlue());
							img.setRGB(x, y, col.getRGB());
						}
					}
			    }

			    List<String> list = new ArrayList<String>();
		    	try {
		    		LuminanceSource lSource = new BufferedImageLuminanceSource(img);
			    	BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(lSource));
			    	Map<DecodeHintType,Object> hints = new EnumMap<DecodeHintType,Object>(DecodeHintType.class);
			    	hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
			    	hints.put(DecodeHintType.POSSIBLE_FORMATS, EnumSet.allOf(BarcodeFormat.class));
		    		for (Result r : new QRCodeMultiReader().decodeMultiple(bitmap, hints))
		    			list.add(r.getText());
		    	} 
		    	catch (com.google.zxing.NotFoundException ex) {
		    		//UniLog.log1("qr code not found");
		    	}
		    	catch (Exception e) {
		    		e.printStackTrace();
		    	}
				UniLog.log1("file:%s page:%d qrcode:%s", pdfFile.getName(), i, list);
				resultList.addAll(list);
			}
			UniLog.log1("file:%s all qrcode:%s", pdfFile.getName(),resultList);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		finally {
			if (document != null)
				document.close();
			if (inStream != null)
				inStream.close();
			System.gc();
		}
		return resultList;
	}
	public static void main(String args[]) throws Exception{
		//List<String> outTexts = QRCodeUtil.decodeImageFileQRCode(new File("c:\\tmp\\sample_qrcode.jpg"));
		//List<String> outTexts = QRCodeUtil.decodeImageFileQRCode(new File("c:\\tmp\\qr_rotate.jpg"));
		//List<String> outTexts = QRCodeUtil.decodeQRCode(new File("/tmp/tst190212-qnr-report-SKM_C3350190128161000.pdf"));
		//List<String> outTexts = QRUtil.decodeQRCode(ImageIO.read(new File("c:\\tmp\\qrcode.jpg")));
		//List<String> outTexts = QRCodeUtil.decodeQRCode(new File("c:\\tmp\\qrcodes.pdf"));
		//List<String> outTexts = QRCodeUtil.decodeQRCode(new File("c:\\tmp\\sample_label.jpg"));
		//UniLog.logm(null,"qrcode=%s", outTexts);
		
		
		decodeQRCode(new File("/tmp/s3bkUT009909.pdf"), 61, 61, false);
		decodeQRCode(new File("/tmp/s3bkNE000904.pdf"), 61, 61, false);
		decodeQRCode(new File("/tmp/s3bkNE003628.pdf"), 61, 61, false);
		if (true) return;
		
		//default qrcode parameter
		FileUtils.writeByteArrayToFile(new File("/tmp/task.png"), createQRCode("http://localhost:8080/pmsdemo/wfmtaskg2ext.html?jobid=A23020002.4&taskid=00000698",200,200,"PNG"));

		//for long content, it used too much space onmargin. the qrcode is not easy to read
		FileUtils.writeByteArrayToFile(new File("/tmp/task_long.png"), createQRCode("http://192.168.0.5:8080/pmsdemo/wfmtaskg2ext.html?jobid=A23020002.3&taskid=00000788&phtv1=18890de4d65e25663f8f282f8da47928&asjhkjahsdkjahkdjahskdjhaskd=172389713987123987129837",150,150,"PNG"));
		
		//use margin 0 + correctionlevel L for long content & small qrcode seems better
		HashMap hints = new HashMap();
		hints.put(EncodeHintType.MARGIN, 0);
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		FileUtils.writeByteArrayToFile(new File("/tmp/task_long_margin0.png"), createQRCode("http://192.168.0.5:8080/pmsdemo/wfmtaskg2ext.html?jobid=A23020002.3&taskid=00000788&phtv1=18890de4d65e25663f8f282f8da47928&asjhkjahsdkjahkdjahskdjhaskd=172389713987123987129837",150,150,"PNG",hints));
		
		

		//System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog"); //affected scope is too large. try disable those logging by adding log4j.logger.org.apache.pdfbox=ERROR in log4j.properties
		boolean isInvert = true;
		int startIdx;
		int endIdx;
		String dirName;
		
		startIdx = endIdx = 61;
		dirName = "D:/tmp/gbptest/testlargejob/newlargesample";
		dirName = "D:/tmp/gbptest/retest220419";
		
		//dirName = "E:/tmp/Example_Decks";
		
		//endIdx = startIdx = 75;
		//isInvert = false;
		//dirName = "E:/tmp/GBP_Deck/GBP_Deck/OLD/DraftSet_956_1_kgvap89u";
		
		
		File dir = new File(dirName);
		Map<String, List<String>> map = new LinkedHashMap<String, List<String>>();
		for (File file : dir.listFiles()) {
			if (file.getName().toLowerCase().endsWith(".pdf")) {
				UniLog.log1("decode file:%s", file.getName());
				List<String> list = decodeQRCode(file, startIdx, endIdx, isInvert);
				map.put(file.getName(), list);
			}
		}
		int qrCodeCount = 0;
		for (Map.Entry<String, List<String>> entry : map.entrySet()) {
			UniLog.log1("file:%s, total:%d", entry.getKey(), entry.getValue().size());
			for (String qrcode : entry.getValue()) {
				qrCodeCount++;
				UniLog.log1("qrcode:%s", qrcode);
			}
		}
		UniLog.log1("qrCodeCount:%d", qrCodeCount);
	}
}
