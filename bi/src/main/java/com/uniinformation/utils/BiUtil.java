package com.uniinformation.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.json.JSONObject;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jx.JxField;
import com.uniinformation.webcore.SessionHelper;

public class BiUtil {
	/*
   	public final static int pxPerChar = 10;
   	public final static int pxForInt  = 140;
   	public final static int pxForFloat= 140;
   	public final static int pxForDate = 140;
   	public final static int pxForDateTime = 280;
   	*/
   	public final static int pxPerChar = 8;
   	public final static int pxPerChnChar = 14;
   	public final static int pxTextboxMargin=16;
   	public final static int pxForInt  = 110;
   	public final static int pxForFloat= 110;
   	public final static int pxForDate = 110;
   	public final static int pxForDateTime = 220;
   	public final static int pxMin = 80;
   	public final static int pxMax = 800;
   	public final static int pxComboButton = 40;
   	public final static int numberDefaultChar = 10;
   	public static AtomicBoolean fDebug = new AtomicBoolean(false);
	
    /***
     * Calculate column width. 
     * Multiple result separated by ";". e.g. fhlex=min;width=123px;
     * The result should be parsed by extractColWidthValue()
     * @param p_biCol column
     * @param p_adjust - width offset
     * @return hflex=<value> or width=<value>px; 
     *         
     */
//    public static String calColumnWidth(BiColumn p_biCol, int p_adjust){
//    	return(calColumnWidth(p_biCol, p_adjust, 0, 0));
//    }
//    public static String calColumnWidth(BiColumn p_biCol, int p_adjust, int p_minWidth){
//    	return(calColumnWidth(p_biCol, p_adjust, p_minWidth, 0));
//    }
//    public static String calColumnWidth(BiColumn p_biCol, int p_adjust, int p_minWidth, int p_maxWidth){
//    	return calColumnWidth(p_biCol, p_adjust, p_minWidth, p_maxWidth, null);
//    }
    public static int calColumnPx(BiColumn p_biCol, int p_adjust, int p_minWidth, int p_maxWidth){
    	int minWidth = p_minWidth;
    	if (p_biCol == null){
    		return 0;
    	}
    	if (p_biCol.isFlexWidth()) {
    		return(p_maxWidth); // or setHflex("true");???? not sure which one better
    	}
    	
    	int resultPx = p_adjust;
    	if (p_biCol.getColumnType().trim().matches("date|time")) {
    		resultPx += pxForDate;
    	} 
    	else if (p_biCol.getColumnType().trim().equals("label")) {
//    		return("hflex=min"); // or setHflex("true");???? not sure which one better
    		resultPx += p_biCol.getColumnLength() * pxPerChar ;
    		minWidth = 10;
    	}
    	else if (p_biCol.getColumnType().trim().equals("datetime")) {
    		resultPx += pxForDateTime;
    	}
		else if (p_biCol.getColumnType().trim().matches("float|money|serial|integer")){
			if(!StringUtils.isBlank(p_biCol.getFormat())){
				resultPx += p_biCol.getFormat().length() * pxPerChar;
			} 
			else {
				resultPx += numberDefaultChar * pxPerChar;
			}
		}
    	else{  //handle normal char type
    		/*
    		if(p_biCol.isChinese()) {
    			resultPx += p_biCol.getColumnLength() * pxPerChnChar + pxTextboxMargin;
    		} else {
    			resultPx += p_biCol.getColumnLength() * pxPerChar ;
    		}
    		*/
    		resultPx += p_biCol.getColumnLength() * pxPerChar ;
			if(p_biCol.getColumnType().trim().matches("list|pickinput|combobox")){
				resultPx += pxComboButton;
			}
    	}
    	
    	//range check
		if (minWidth <= 0){
			minWidth = pxMin;
		}
		int maxWidth = p_maxWidth;
		if (maxWidth <= 0){
			maxWidth = pxMax;
		}
    	if (resultPx < minWidth){
    		resultPx = minWidth;
    	}
    	else if (resultPx > maxWidth){
    		resultPx = maxWidth;
    	}
    	return(resultPx);
    }
    /***
     * estimation column width
     * @param p_biCol
     * @param p_adjust
     * @param p_minWidth
     * @param p_maxWidth
     * @param p_isSkipFlexWidth
     * @return [tag]=[value]
     */
    public static String calColumnWidth(BiColumn p_biCol, int p_adjust, int p_minWidth, int p_maxWidth, String p_flexValue){
    	int minWidth = p_minWidth;
    	if (p_biCol == null){
    		return null;
    	}
    	if(!StringUtils.isBlank(extractColDecorationValue(p_biCol.getDecoration(),"width"))) return(p_biCol.getDecoration());
    	if(!StringUtils.isBlank(extractColDecorationValue(p_biCol.getDecoration(),"hflex"))) return(p_biCol.getDecoration());
    	if (p_biCol.isFlexWidth()) {
//    		return("hflex=max"); // or setHflex("true");???? not sure which one better
    		//return("hflex=1"); // or setHflex("true");???? not sure which one better
    		//return("hflex=max"); //andrew210506 hflex=max avoid column hide when listbox overflow
    		//return("hflex=1"); //andrew221102 try to fix column not aligned bug (seems max is obsoleted and not supported anymore)
    		return "hflex=" + StringUtils.defaultIfBlank(p_flexValue, "max");
    	}
    	
    	int resultPx = p_adjust;
    	if (p_biCol.getColumnType().trim().equals("date")) {
    		resultPx += pxForDate;
    	} 
    	else if (p_biCol.getColumnType().trim().equals("time")) {
    		if (p_biCol.getTimeCompIsShortFmt())
    			resultPx += pxMin;
    		else
    			resultPx += pxForDate;
    	} 
    	/*
    	else if (p_biCol.getColumnType().trim().equals("label")) {
//    		return("hflex=min"); // or setHflex("true");???? not sure which one better
    		resultPx += p_biCol.getColumnLength() * pxPerChar ;
    		minWidth = 10;
    	}*/
    	else if (p_biCol.getColumnType().trim().equals("datetime")) {
    		resultPx += pxForDateTime;
    	}
		else if (p_biCol.getColumnType().trim().matches("float|money|serial|integer")){
			if(!StringUtils.isBlank(p_biCol.getFormat())){
				resultPx += p_biCol.getFormat().length() * pxPerChar;
			} 
			else {
				resultPx += numberDefaultChar * pxPerChar;
			}
		}
		else if (p_biCol.getColumnType().equals("label")) {
//			if (p_biCol.getField() != null && p_biCol.getField().getFieldType().trim().matches("float|money|serial|integer")){
			if (p_biCol.isNumber()) {
				if(!StringUtils.isBlank(p_biCol.getFormat())){
					resultPx += p_biCol.getFormat().length() * pxPerChar;
					minWidth = 10;
				} 
				else {
					resultPx += numberDefaultChar * pxPerChar;
					minWidth = 10;
				}
			} else {
//				if(!StringUtils.isBlank(p_biCol.getDecoration())){
//						int cc;
//						cc = 0;
//				}
				String ss = p_biCol.getDecoration();
				if(!StringUtils.isBlank(ss) &&
						ss.indexOf('=') < 0 && 
						!"{".equals(ss.substring(0, 1))) {
						return(ss);
				}
				resultPx += p_biCol.getColumnLength() * pxPerChar ;
				minWidth = 10;
//				if(!StringUtils.isBlank(p_biCol.getDecoration())){
//					return(p_biCol.getDecoration());
//				} else {
//					resultPx += p_biCol.getColumnLength() * pxPerChar ;
//					minWidth = 10;
//				}
			}
		}
    	else{  //handle normal char type
    		/*
			if(!StringUtils.isBlank(p_biCol.getDecoration())){
					return(p_biCol.getDecoration());
			}
			*/
			/*
    		if(p_biCol.isChinese()) {
    			resultPx += p_biCol.getColumnLength() * pxPerChnChar + pxTextboxMargin;
    		} else {
    		}
    		*/
    		resultPx += p_biCol.getColumnLength() * pxPerChar ;
			if(p_biCol.getColumnType().trim().matches("list|pickinput")){
				resultPx += pxComboButton;
			}
    	}
    	
    	//range check
		if (minWidth <= 0){
			minWidth = pxMin;
		}
		int maxWidth = p_maxWidth;
		if (maxWidth <= 0){
			maxWidth = pxMax;
		}
    	if (resultPx < minWidth){
    		resultPx = minWidth;
    	}
    	else if (resultPx > maxWidth){
    		resultPx = maxWidth;
    	}
    	return("width=" + resultPx + "px");
    }
    /***
     * e.g.1 
     * input:
     *    p_colWidthStr = "var1=123;var2=456;var3=789";" 
     *    p_tag = var1
     * return:
     *    123
     * 
     * @param p_colWidthStr generated by calColumnWidth()
     * @param p_tag - width / hflex
     * @return value for setWidth() or setHflex
     */
    public static String extractColDecorationValueByJson(String p_colWidthStr, String p_tag) {
    	try {
    		JSONObject jo = new JSONObject(p_colWidthStr);
    		String strVal = jo.optString(p_tag);
    		return(strVal);
    	} catch (Exception p_ex) {
    		UniLog.log(p_ex);
    	}
    	return(null);
    }
    public static String extractColDecorationValue(String p_colWidthStr, String p_tag){
		if (StringUtils.isBlank(p_colWidthStr) || StringUtils.isBlank(p_tag)){
			return("");
		}
		if("{".equals(p_colWidthStr.substring(0, 1))) {
			return(extractColDecorationValueByJson(p_colWidthStr,p_tag));
		}
		if(!p_colWidthStr.contains("=") && "format".equals(p_tag)) {
			return(p_colWidthStr);
		}
		String orgStr = p_colWidthStr.replaceAll("\\s+", "").toLowerCase();
		String resultStr = orgStr;
		resultStr = StringUtils.removeFirst(resultStr, String.format("^%s=", p_tag));
		resultStr = StringUtils.removeFirst(resultStr, String.format(".*;%s=", p_tag));
		if (StringUtils.equals(orgStr, resultStr)){
			//UniLog.logm(null,"extractColWidthValue(%s,%s) tag not found",p_colWidthStr, p_tag);
			return("");
		}
		resultStr = StringUtils.removeFirst(resultStr, ";.*");
		//UniLog.logm(null,"extractColWidthValue(%s,%s) return %s",p_colWidthStr, p_tag, resultStr);
		return(resultStr);
    }
    /*
    //obsoleted, need to change from sessionHelper to filing
	public static ReturnMsg sendEmail(
			Pair<String,String> p_from, 
			List<Pair<String,String>> p_toList, 
			List<Pair<String,String>> p_bccList, 
			String p_subject, String p_htmlMsg, String p_txtMsg, List<EmailAttachment> p_attList, SessionHelper p_sh){
		if (p_sh == null || !p_sh.getAllowSmtp()){
			return(new ReturnMsg(false,"Not allow to send email"));
		}
		
		return(EmailUtil.sendHtmlEmail(p_from, p_toList, p_bccList, p_subject, p_htmlMsg, p_txtMsg, p_attList, p_sh.getSmtpHost(), p_sh.getSmtpPort(), p_sh.getSmtpLoginId(), p_sh.getSmtpLoginPassword(), p_sh.getSmtpSSLOnConnect()));
	}
	*/
	public static ReturnMsg sendEmail(
			Pair<String,String> p_from, 
			List<Pair<String,String>> p_toList, 
			List<Pair<String,String>> p_bccList, 
			String p_subject, String p_htmlMsg, String p_txtMsg, List<EmailAttachment> p_attList, SessionHelper p_sh){
		return sendEmail(p_from, p_toList, null, p_bccList, p_subject, p_htmlMsg, p_txtMsg, p_attList, p_sh);
	}

	public static ReturnMsg sendEmail(
			Pair<String,String> p_from, 
			List<Pair<String,String>> p_toList, 
			List<Pair<String,String>> p_ccList, 
			List<Pair<String,String>> p_bccList, 
			String p_subject, String p_htmlMsg, String p_txtMsg, List<EmailAttachment> p_attList, SessionHelper p_sh){
		return sendEmail(new HtmlEmail(), p_from, p_toList, p_ccList, p_bccList, p_subject, p_htmlMsg, p_txtMsg, p_attList, p_sh);
	}

    /***
     * send email 
     * @param p_email - HtmlEmail object
     * @param p_from - sender email address. if null, use default value from config
     * @param p_toList - recipient email address.
     * @param p_ccList - recipient email address.
     * @param p_bccList - recipient email address.
     * @param p_subject - subject
     * @param p_htmlMsg - html msg
     * @param p_txtMsg - text msg
     * @param p_attList - attachment list
     * @param p_sh - session helper - for obtain agent specific config
     * @return ReturnMsg
     */
	public static ReturnMsg sendEmail(
			HtmlEmail p_email,
			Pair<String,String> p_from, 
			List<Pair<String,String>> p_toList, 
			List<Pair<String,String>> p_ccList, 
			List<Pair<String,String>> p_bccList, 
			String p_subject, String p_htmlMsg, String p_txtMsg, List<EmailAttachment> p_attList, SessionHelper p_sh){
		try{
			if (p_sh == null || !p_sh.getAllowSmtp()){
				return(new ReturnMsg(false,"Not allow to send email"));
			}
			JSONObject json = FilingUtil.getJson(p_sh.getAgent(), null, SessionHelper.SYSTEM_SMTP_FILING_STORE_KEY);
			if (json == null){
				UniLog.log1("json is null. key:%s", SessionHelper.SYSTEM_SMTP_FILING_STORE_KEY);
				return ReturnMsg.defaultFail;
			}
			String smtpHost = json.getString("smtpHost");
			int smtpPort = -1;
			try{
				smtpPort = json.getInt("smtpPort");
			}
			catch(Exception ex){ }
			EmailUtil.SecMode secMode = EmailUtil.SecMode.valueOf(json.getString("smtpSecMode"));
			boolean sslValidate = !StringUtils.equalsAnyIgnoreCase(json.getString("smtpSSLValidate"),"N");
			String smtpLogin = json.getString("smtpLogin");
			String smtpPassword = json.getString("smtpPassword");
			Pair<String,String> from = p_from;
			if (from == null) {
				from = json.getString("smtpFrom") == null ? null : Pair.of(json.getString("smtpFrom"),(String)null);
			}
			return(EmailUtil.sendHtmlEmail(p_email, from, p_toList, p_ccList, p_bccList, p_subject, p_htmlMsg, p_txtMsg, p_attList, smtpHost, smtpPort, smtpLogin, smtpPassword, secMode, sslValidate));
		}
		catch(Exception ex){
			return new ReturnMsg(ex);
		}
		
		
	}

	@FunctionalInterface
	public interface CheckedRunnable {
	    void run() throws Exception;
	}
	
	@FunctionalInterface
	public interface CheckedSupplier<T> {
	    T get() throws Exception;
	}

	@FunctionalInterface
	public interface CheckedSupplier2<T1, T2> {
	    Pair<T1, T2> get() throws Exception;
	}

	@FunctionalInterface
	public interface CheckedSupplier3<T1, T2, T3> {
	    Triple<T1, T2, T3> get() throws Exception;
	}
	
	@FunctionalInterface
	public interface CheckedConsumer<T> {
	    void accept(T t) throws Exception;
	}

	@FunctionalInterface
	public interface CheckedConsumer2<T1, T2> {
	    void accept(T1 t, T2 t2) throws Exception;
	}

	@FunctionalInterface
	public interface CheckedConsumer3<T1, T2, T3> {
	    void accept(T1 t, T2 t2, T3 t3) throws Exception;
	}

	@FunctionalInterface
	public interface CheckedConsumer4<T1, T2, T3, T4> {
	    void accept(T1 t, T2 t2, T3 t3, T4 t4) throws Exception;
	}

	@FunctionalInterface
	public interface CheckedConsumer5<T1, T2, T3, T4, T5> {
	    void accept(T1 t, T2 t2, T3 t3, T4 t4, T5 t5) throws Exception;
	}
	
	@FunctionalInterface
	public interface CheckedFunction<T, R> {
	    R apply(T t) throws Exception;
	}

	@FunctionalInterface
	public interface CheckedFunction2<T1, T2, R> {
	    R apply(T1 t1, T2 t2) throws Exception;
	}

	@FunctionalInterface
	public interface CheckedFunction3<T1, T2, T3, R> {
	    R apply(T1 t1, T2 t2, T3 t3) throws Exception;
	}

	@FunctionalInterface
	public interface CheckedFunction4<T1, T2, T3, T4, R> {
	    R apply(T1 t1, T2 t2, T3 t3, T4 t4) throws Exception;
	}
	
	@FunctionalInterface
	public interface CheckedPredicate<T> {
	    boolean test(T t) throws Exception;
	}

	@FunctionalInterface
	public interface CheckedPredicate2<T1, T2> {
	    boolean test(T1 t1, T2 t2) throws Exception;
	}

	@FunctionalInterface
	public interface CheckedPredicate3<T1, T2, T3> {
	    boolean test(T1 t1, T2 t2, T3 t3) throws Exception;
	}

	public static <T> Consumer<T> throwConsumer(CheckedConsumer<T> throwingConsumer) {
        return t -> {
            try {
                throwingConsumer.accept(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

	public static <T, R> Function<T, R> throwFunction(CheckedFunction<T, R> throwingFunction) {
        return t -> {
            try {
                return throwingFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

	public static <T> Predicate<T> throwPredicate(CheckedPredicate<T> throwingPredicate) {
        return t -> {
            try {
                return throwingPredicate.test(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
	}

	public static IntConsumer throwIntConsumer(CheckedConsumer<Integer> throwingConsumer) {
        return t -> {
            try {
                throwingConsumer.accept(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

	public static IntPredicate throwIntPredicate(CheckedPredicate<Integer> throwingPredicate) {
        return t -> {
            try {
                return throwingPredicate.test(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
	}

	public static <R> IntFunction<R> throwIntFunction(CheckedFunction<Integer, R> throwingFunction) {
        return t -> {
            try {
                return throwingFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

	public static <T> ToIntFunction<T> throwToIntFunction(CheckedFunction<T, Integer> throwingFunction) {
        return t -> {
            try {
                return throwingFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

	public static <R> LongFunction<R> throwLongFunction(CheckedFunction<Long, R> throwingFunction) {
        return t -> {
            try {
                return throwingFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

	public static <R> DoubleFunction<R> throwDoubleFunction(CheckedFunction<Double, R> throwingFunction) {
        return t -> {
            try {
                return throwingFunction.apply(t);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
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

	public static String joinStringLabel(SessionHelper sessionHelper, String delimiter, Object... o) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < o.length; i += 2) {
			if (sb.length() > 0)
				sb.append(delimiter);
			sb.append(String.format(sessionHelper.getLabel((String)o[i]), o[i + 1]));
		}
		return sb.toString();
	}
	public static String getSessionHelperLabel(SessionHelper sh, String defaultStr) {
		return sh != null ? sh.getLabel(defaultStr) : defaultStr;
	}
	public static String getSessionHelperLabel(Cell c, String defaultStr) {
		return (c != null && c instanceof ColumnCell) ? ((ColumnCell)c).getBiResult().getSessionHelper().getLabel(defaultStr) : defaultStr;
	}
	public static String getSessionHelperLabel(JxField fd, String defaultStr) {
		return (fd != null && fd.getJxValue() != null) ? getSessionHelperLabel(fd.getJxValue(), defaultStr) : defaultStr;
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

	public static Runnable safeRunnable(CheckedRunnable cb) {
		return () -> {
			try {
				cb.run();
			} catch (Exception e) {
				UniLog.log(e);
			}
		};	
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

	public static Stream<CellCollection> getTableRecStream(SelectUtil su, String sql, Object... args) throws Exception {
		return getTableRecStream(su, sql, buildWhereclByArgs(args));
	}

	public static TableRec getTableRec(SelectUtil su, String sql, Object... args) throws Exception {
		return su.getQueryResult(sql, buildWhereclByArgs(args));
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

	public static Optional<TableRec> getFirstTableRec(SelectUtil su, String sql, Object... args) throws Exception {
		return getFirstTableRec(su, sql, buildWhereclByArgs(args));
	}

	public static boolean hasTableRec(TableRec tr) throws TableRecException {
		return tr.getRecordCount() > 0;
	}

	public static boolean hasTableRec(SelectUtil su, String sql, Wherecl wherecl) throws Exception {
		return hasTableRec(su.getQueryResult(sql, wherecl, 1));
	}

	public static boolean hasTableRec(SelectUtil su, String sql, Object... args) throws Exception {
		return hasTableRec(su, sql, buildWhereclByArgs(args));
	}
	
	public static Wherecl buildWhereclByArgs(Object... args) {
		Wherecl w = new Wherecl();
		for (Object o : args)
			w.appendArgument(o);
		return w;
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

}

