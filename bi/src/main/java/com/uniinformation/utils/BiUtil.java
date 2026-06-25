package com.uniinformation.utils;

import java.io.File;
import java.util.List;
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

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.google.common.collect.Lists;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.jx.JxField;
import com.uniinformation.webcore.SessionHelper;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;

public class BiUtil {
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
    	if(!StringUtils.isBlank(BiUtil.extractColDecorationValue(p_biCol.getDecoration(),"width"))) return(p_biCol.getDecoration());
    	if(!StringUtils.isBlank(BiUtil.extractColDecorationValue(p_biCol.getDecoration(),"hflex"))) return(p_biCol.getDecoration());
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
	public static String getWebContentRealPath(String p_path, boolean p_withSeparator){
//		return getWebContentRealPath((HttpServletRequest)Executions.getCurrent().getNativeRequest(), p_path, p_withSeparator);
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
	public static String getClassRootPath() {
		String rootPath = BiUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		if (fDebug.get()) UniLog.log1("rootPath:1:"+ rootPath);
		
		//remove class name
	    //remark: getProtectionDomain output is depend on running env. it may contain class name
		//C:/eclipse_dev/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/pmsdemo/WEB-INF/classes/com/uniinformation/utils/ZkUtil.class
	    //C:/eclipse_dev/.metadata/.plugins/org.eclipse.wst.server.core/tmp0/wtpwebapps/pmsdemo/WEB-INF/classes/
		rootPath = StringUtils.removeEnd(rootPath, StringUtils.replaceChars(BiUtil.class.getName(),'.','/') +".class");
		
		//append slash
		if (!StringUtils.endsWith(rootPath, "/")) {
			rootPath = rootPath +"/";
		}
		if (fDebug.get()) UniLog.log1("rootPath:2:"+ rootPath);
		return rootPath;
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
	
	public static String defaultStringIfBlank(String str, String... defaultStrs) {
		List<String> list = Lists.asList(str, defaultStrs);
		for (String s : list) {
			if (StringUtils.isNotBlank(s))
				return s;
		}
		return list.get(list.size() - 1);
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
	public static String getSessionHelperLabel(SessionHelper sh, String defaultStr) {
		return sh != null ? sh.getLabel(defaultStr) : defaultStr;
	}
	public static String getSessionHelperLabel(Cell c, String defaultStr) {
		return (c != null && c instanceof ColumnCell) ? ((ColumnCell)c).getBiResult().getSessionHelper().getLabel(defaultStr) : defaultStr;
	}
	public static String getSessionHelperLabel(JxField fd, String defaultStr) {
		return (fd != null && fd.getJxValue() != null) ? getSessionHelperLabel(fd.getJxValue(), defaultStr) : defaultStr;
	}
}
