package com.uniinformation.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import org.apache.commons.lang3.StringUtils;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.kyoko.common.ChineseConvert;
import com.lowagie.text.BadElementException;
import com.uniinformation.itext.text.BaseColor;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
//import com.lowagie.text.pdf.Barcode128;
//import com.lowagie.text.pdf.BarcodeEAN;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.uniinformation.itext.text.pdf.Barcode128;
import com.uniinformation.itext.text.pdf.BarcodeEAN;
import com.uniinformation.itext.text.pdf.BarcodeQRCode;

public class ChnftrParser {
	public final static String GETIMAGE_TAG = "getimage://";
	public final static String GETTEMPLATE_TAG = "gettemplate://"; // gettemplate://name,pagenum
	//private static final boolean WX = true;
	public static final float DOCUMENT_DPI = 72f;
	public static final float CHNFTR_DPI = 100f;
	public static final float CHNFTR_UM_DPI = 25400f;
	public static final float IMAGE_SOURCE_DPI = 300f;
	public static final char ESC_CHAR = 27;
//	public static final String DEFAULT_CHARACTER_SET = "BIG5";
	public static final String DEFAULT_CHARACTER_SET = "MS950_HKSCS";
	private static final String WIN_FONT_PATH = "C:\\WINDOWS\\Fonts";
	private static final String LIN_FONT_PATH = "/usr/unc/font/ttf";
	private static final String SPEC_FONT_PATH = "/com/uniinformation/itext/font";
	private static final String FONT_PATH = new File(WIN_FONT_PATH).exists() ? WIN_FONT_PATH : LIN_FONT_PATH;
	private static final String REPLACESTR_PAGENUM = "${pagenum}";
	private static final String REPLACESTR_PAGECOUNT = "${pagecount}";
	private static final String REPLACESTR_TPAGENUM = "${tpagenum}";
	private static final String REPLACESTR_TPAGECOUNT = "${tpagecount}";
    private static final BaseColor DEFAULT_RESERVE_TEXT_BG_COLOR = BaseColor.GRAY;
	private static final Map<String, FontData> mEngFontMap = new HashMap<String, FontData>(){{
		put("ascii", new FontData("ubuntumono-r.ttf"));
//		put("cour_nr", new FontData(BaseFont.COURIER));
//		put("cour_nr", new FontData(BaseFont.HELVETICA));
		put("cour_nr", new FontData("cour.ttf"));
		put("ariblk", new FontData("ariblk.ttf"));
		put("ariblki", new FontData("ariblk.ttf", false, true));
		put("frabk", new FontData("frabk.ttf"));
		put("frabkb", new FontData("frabk.ttf", true, false));
		put("helv_nr", new FontData(BaseFont.HELVETICA));
		put("helv_br", new FontData(BaseFont.HELVETICA_BOLD));
		put("helv_ni", new FontData(BaseFont.HELVETICA_OBLIQUE));
		put("helv_bi", new FontData(BaseFont.HELVETICA_BOLDOBLIQUE));
		put("tmsr_nr", new FontData(BaseFont.TIMES_ROMAN));
		put("tmsr_br", new FontData(BaseFont.TIMES_BOLD));
		put("tmsr_ni", new FontData(BaseFont.TIMES_ITALIC));
		put("tmsr_bi", new FontData(BaseFont.TIMES_BOLDITALIC));}	
		private static final long serialVersionUID = -2111561373594798899L;
	};
	private static String[] paperType = {"A4P","A4L","FSP","FSL","A5P","A5L","B3P","B3L","A3P","A3L"};
	private static final Map<String, FontData> mChnFontMap = new HashMap<String, FontData>(){{
		put("chinese", new FontData("mingliu.ttc,0"));
		put("chineseb", new FontData("mingliu.ttc,0", true, false));
		put("chinesei", new FontData("mingliu.ttc,0", false, true));
		put("chinesebi", new FontData("mingliu.ttc,0", true, true));
		put("mshei", new FontData("msjhbd.ttc,0"));
		put("msheib", new FontData("msjhbd.ttc,0", true, false));
		put("msheii", new FontData("msjhbd.ttc,0", false, true));
		put("msheibi", new FontData("msjhbd.ttc,0", true, true));
		put("msheis", new FontData("msjh.ttc,0"));
		put("hansanb", new FontData("SourceHanSansHC-Bold.otf,0", true, false));
		put("schinese", new FontData("simsun.ttc,0"));
		put("kai", new FontData("simkai.ttf"));
		put("ming", new FontData("mingliu.ttc,0"));}
		private static final long serialVersionUID = -5311242026687744934L;
	};
	public static final int EVERYPAGE_TEMPLATE_COUNT = 2;
	
	//private static float gChnftrDpi = CHNFTR_DPI;
	private float mChnftrDpi = CHNFTR_DPI;
	private Rectangle mDocumentPageSize;
	private float mDocumentMarginLeft, mDocumentMarginTop;
	private int mCpi = 16;
	private Document mDocument;
    private PdfWriter mWriter;
    private OutputStream mOutputStream;
    private PdfContentByte mCanvas;
	private final List<Element> mElements = new ArrayList<Element>();
	private final List<BaseObject> mObjects = new ArrayList<BaseObject>();

	private InputStream mChnftrStream;
	private PdfTemplate[] mCurrentTemplates;
	private final TemplateFileReaderList mTemplateFileReaderList = new TemplateFileReaderList();
	private final TemplateFileReaderMap mTemplateFileReaderMap = new TemplateFileReaderMap();
	private final Map<String, PdfTemplate> mTemplateMap = new HashMap<String, PdfTemplate>();
	private boolean mHasNotChnftrStream;
    private int mCurrentX = 0, mCurrentY = 0;
    private String mCurrentEngFontFace = "ascii", mCurrentChnFontFace = "chinese";
    private int[] mCurrentPresetFontSizes = {8, 0, 0};
    private int mCurrentFontSizesIndex = 0;
    private int mCurrentTemplateOffset = 0;
    private BaseColor mCurrentColor = BaseColor.BLACK;
    private BaseColor mCurrentReserveTextBgColor = DEFAULT_RESERVE_TEXT_BG_COLOR;
    private boolean mCurrentUseSecondFontSize, mCurrentUseThirdFontSize;
    private float mCurrentLineHeight = DOCUMENT_DPI / mCpi;
	private boolean mCurrentIsBoldStarted, mCurrentIsReverseStarted, mCurrentIsItaticStarted, mCurrentIsUnderlineStarted;
	private boolean mCurrentIsRelativeX = true, mCurrentIsRelativeY = true;
	private int mCurrentIndent;
	private TextAlign mCurrentTextAlign;
	private int mCurrentUnderLineNum;
	private int mCurrentPage;

	private float mCurrentOffsetPrintX, mCurrentOffsetPrintY;
	private UnderLineInfo mLastUnderLineInfo;
	private ChnftrGetImageInterface chnftrGetImageInterface = null;
	private PrintCallbackInterface printCallback = null;
	private boolean useGetImageInterfaceByDefault = false;
	public static interface ChnftrGetImageInterface{
		byte[] getImage(String p_key);
	}
	public static interface PrintCallbackInterface {
		void afterPage();
	}
	public static class ImportedTemplatePageInfo {
		public PdfImportedPage page;
		public float left, top, width, height;
		public int cLeft, cTop, cWidth, cHeight;
	}
	public static class MarkObjectInfo {
		public MarkObject markObject;
		public float left, top, width, height;
		public int cLeft, cTop, cWidth, cHeight;
	}
    private static class FontData {
    	String faceOrFName;
    	boolean bold, itatic;
    	FontData(String faceOFName, boolean bold, boolean itatic) {
    		this.faceOrFName = faceOFName;
    		this.bold = bold;
    		this.itatic = itatic;
    	}
    	FontData(String faceName) {
    		this(faceName, false, false);
    	}
    }
	private Map<Integer, Map<Float, List<UnderLineInfo>>> mUnderLineInfoMap = new LinkedHashMap<Integer, Map<Float, List<UnderLineInfo>>>();
	private List<ImportedTemplatePageInfo> mPageImportedTemplatePageList = new ArrayList<ImportedTemplatePageInfo>();
	private List<MarkObjectInfo> mPageMarkObjectList = new ArrayList<MarkObjectInfo>();
	
	private boolean useAscender = true;

	public ChnftrParser(String str, String options) throws FileNotFoundException, IOException {
		this(str != null ? new ByteArrayInputStream(str.getBytes(DEFAULT_CHARACTER_SET)) : null, options, DEFAULT_CHARACTER_SET);
	}
	public ChnftrParser(InputStream p_stream, String options) throws FileNotFoundException, IOException {
		this(p_stream, options, DEFAULT_CHARACTER_SET);
	}
	public ChnftrParser(String str, String options, String charSet) throws FileNotFoundException, IOException {
		this(str != null ? new ByteArrayInputStream(str.getBytes(charSet)) : null, options, charSet);
	}
	public ChnftrParser(InputStream p_stream, String options, String charSet) throws FileNotFoundException, IOException {
		if (p_stream != null) {
			mChnftrStream = p_stream;
			BufferedReader br = new BufferedReader(new InputStreamReader(p_stream, charSet));
			mElements.addAll(parseChnftrStream(br));
			br.close();
		} else
			mHasNotChnftrStream = true;
		parseOptions(options);
		generateObjects();
	}
	public ChnftrParser(byte[] data, Rectangle docPageSize, float pageWidth, float pageLength, float dpi, int cpi, float lpi) throws FileNotFoundException, IOException {
		this(data != null ? new ByteArrayInputStream(data) : null, DEFAULT_CHARACTER_SET, docPageSize, pageWidth, pageLength, dpi, cpi, lpi);
	}
	public ChnftrParser(String str, Rectangle docPageSize, float pageWidth, float pageLength, float dpi, int cpi, float lpi) throws FileNotFoundException, IOException {
		this(str != null ? new ByteArrayInputStream(str.getBytes(DEFAULT_CHARACTER_SET)) : null, DEFAULT_CHARACTER_SET, docPageSize, pageWidth, pageLength, dpi, cpi, lpi);
	}
	public ChnftrParser(InputStream p_stream, Rectangle docPageSize, float pageWidth, float pageLength, float dpi, int cpi, float lpi) throws FileNotFoundException, IOException {
		this(p_stream, DEFAULT_CHARACTER_SET, docPageSize, pageWidth, pageLength, dpi, cpi, lpi);
	}
	public ChnftrParser(byte[] data, String charSet, Rectangle docPageSize, float pageWidth, float pageLength, float dpi, int cpi, float lpi) throws FileNotFoundException, IOException {
		this(data != null ? new ByteArrayInputStream(data) : null, charSet, docPageSize, pageWidth, pageLength, dpi, cpi, lpi);
	}
	public ChnftrParser(String str, String charSet, Rectangle docPageSize, float pageWidth, float pageLength, float dpi, int cpi, float lpi) throws FileNotFoundException, IOException {
		this(str != null ? new ByteArrayInputStream(str.getBytes(charSet)) : null, charSet, docPageSize, pageWidth, pageLength, dpi, cpi, lpi);
	}
	public ChnftrParser(InputStream p_stream, String charSet, Rectangle docPageSize, float pageWidth, float pageLength, float dpi, int cpi, float lpi) throws FileNotFoundException, IOException {
		if (p_stream != null) {
			mChnftrStream = p_stream;
			BufferedReader br = new BufferedReader(new InputStreamReader(p_stream, charSet));
			mElements.addAll(parseChnftrStream(br));
			br.close();
		} else
			mHasNotChnftrStream = true;
		parseOptions(docPageSize, pageWidth, pageLength, dpi, cpi, lpi);
		generateObjects();
	}
	public List<Element> parseChnftrText(String text) throws IOException {
		return parseChnftrStream(new StringReader(text));
	}
	public List<Element> parseChnftrStream(Reader reader) throws IOException {
		List<Element> eleList = new ArrayList<Element>();
		String src = "";
		StringBuilder sb = new StringBuilder();
		char[] buff = new char[1024];
		int readCharCount = 0;
		NextCharAndPos nextCharAndPos;
		while (!src.isEmpty() || (readCharCount = reader.read(buff)) != -1) {
//			UniLog.log("240827  parseChnftrStream read reader got " + readCharCount);
			if (src.isEmpty()) {
				if (readCharCount == 0)
					continue;
				src = new String(buff, 0, readCharCount);
			}
			nextCharAndPos = NextCharAndPos.findNextCharAndPos(src, String.format("%c\f", ESC_CHAR));
			if (nextCharAndPos != null) {
				sb.append(src.substring(0, nextCharAndPos.pos));
				Element element = Element.newInstance(sb.toString());
				if (element != null)
					eleList.add(element);
				if (nextCharAndPos.c != ESC_CHAR && nextCharAndPos.c != '\0')
					eleList.add(Element.newInstance(nextCharAndPos.c + ""));
				sb.setLength(0);
			}
			else 
				sb.append(src);
			if (nextCharAndPos != null && nextCharAndPos.pos + 1 < src.length())
				src = src.substring(nextCharAndPos.pos + 1);
			else
				src = "";
		}
		if (sb.length() > 0) {
			Element element = Element.newInstance(sb.toString());
			if (element != null)
				eleList.add(element);
		}
		if (eleList.size() > 0) {
			Element element = eleList.get(eleList.size() - 1);
			if (element.text.length() == 0 && element.controlChar == '\f')
				eleList.remove(eleList.size() - 1);
		}
		if (eleList.size() > 1) {
			Element element = eleList.get(eleList.size() - 1);
			Element element1 = eleList.get(eleList.size() - 2);
			if (element.text.length() == 1 && element.text.charAt(0) == '\n') {
				if (element1.text.length() == 0 && element1.controlChar == '\f') {
					eleList.remove(eleList.size() - 1);
					eleList.remove(eleList.size() - 1);
				}
			}
		}
		return eleList;
	}
	private List<Element> parseChnftrText(String chnftrText, Integer pageNum, Integer pageCount, Integer tpageNum, Integer tpageCount) throws IOException {
		if (pageNum != null)
			chnftrText = chnftrText.replace(REPLACESTR_PAGENUM, String.valueOf(pageNum));
		if (pageCount != null)
			chnftrText = chnftrText.replace(REPLACESTR_PAGECOUNT, String.valueOf(pageCount));
		if (tpageNum != null)
			chnftrText = chnftrText.replace(REPLACESTR_TPAGENUM, String.valueOf(tpageNum));
		if (tpageCount != null)
			chnftrText = chnftrText.replace(REPLACESTR_TPAGECOUNT, String.valueOf(tpageCount));
        Reader sr = new StringReader(chnftrText);
        return parseChnftrStream(sr);
	}
	private static class NextCharAndPos {
		public char c;
		public int pos;
		public static NextCharAndPos findNextCharAndPos(String src, String findChars) {
			for (int i = 0; i < src.length(); i++) {
				for (int j = 0; j < findChars.length(); j++) {
					if (src.charAt(i) == findChars.charAt(j)) {
						NextCharAndPos nextCharAndPos = new NextCharAndPos();
						nextCharAndPos.c = findChars.charAt(j);
						nextCharAndPos.pos = i;
						return nextCharAndPos;
					}
				}
			}
			return null;
		}
	}
	public static class Element {
		//private static final Pattern PATTERN = Pattern.compile("^((-?\\d+;?)+)([A-Za-z@^~\\.\\?])(<([^<>]+)>)?([^<>]*)$");
		private static final Pattern PATTERN = Pattern.compile("^((-?\\d+;?)+)([A-Za-z@^~\\.\\?])(<([^<>]+)>)?([\\s\\S]*)$");
		public ArrayList<Integer> params = new ArrayList<Integer>();
		public char controlChar = 0;
		public String option = "";
		public String text = "";
		@Override
		public String toString() {
			String params = "";
			for (Integer s : this.params)
				params += s + ";";
			return "params=" + params
					+ ", controlChar=" + controlChar
					+ ", option=" + option
					+ ", text=" + text;
		}
		public int getParam(int index) {
			if (params.size() > index)
				return params.get(index);
			return 0;
		}
		public boolean hasParam(int index) {
			if (params.size() > index)
				return true;
			return false;
		}
		public static Element newInstance(String src) {
			Element element = null;
			Matcher matcher = PATTERN.matcher(src);
			if (matcher.matches()) {
				element = new Element();
				String[] ss = matcher.group(1).split(";");
				for (String s : ss) {
					try {
						element.params.add(Integer.parseInt(s));
					} catch (NumberFormatException ex) {
					}
				}
				element.controlChar = matcher.group(3).charAt(0);
				element.option = matcher.group(5);
				element.text = matcher.group(6);
				if (element.option == null)
					element.option = "";
				if (element.text == null)
					element.text = "";
			}
			else {
				if (src.length() > 0) {
					element = new Element();
					char c = src.charAt(0);
					switch (c) {
					case 's':
					case 'w':
					case 'P':
					case 'u':
					case 'b':
					case 'r':
					case 'i':
					case 't':
					case '\f':
						element.controlChar = c;
						if (src.length() > 1)
							element.text = src.substring(1);
						break;
					default:
						element.text = src;
						break;
					}
				}
			}
			return element;
		}
	}
	private void generateObjects() {
		generateObjects(mElements, mObjects);
	}
	public void generateObjects(List<Element> elementList, List<BaseObject> outObjects) {
		int n=0;
		for (Element element : elementList) {
			n++;
			if(n % 100000 == 0) {
				UniLog.log("240827 ChnftrParser.generateObjects " + n + " object generated");
			}
			boolean isAddedText = false, isChangeXY = false;
			switch (element.controlChar) {
			case 'I':
				mCurrentIndent = element.getParam(0);
			case 'X':
			case 'U':
				mCurrentX = element.getParam(0);
				mCurrentIsRelativeX = false;
				isChangeXY = true;
				break;
			case 'Y':
			case 'V':
				mCurrentY = element.getParam(0);
				mCurrentIsRelativeY = false;
				isChangeXY = true;
				break;
			case 'x':
				mCurrentX += element.getParam(0);
				mCurrentIsRelativeX = true;
				mCurrentIsRelativeY = true;
				isChangeXY = true;
				break;
			case 'y':
				mCurrentY += element.getParam(0);
				mCurrentIsRelativeX = true;
				mCurrentIsRelativeY = true;
				isChangeXY = true;
				break;
			case 'm':
				mCurrentX = Math.round(element.getParam(0) * getCurrentCharWidthForChnftr());
				mCurrentIsRelativeX = false;
				isChangeXY = true;
				break;
			case 'O':
				mCurrentX += element.getParam(0);
				mCurrentY += element.getParam(1);
				mCurrentIsRelativeX = true;
				mCurrentIsRelativeY = true;
				isChangeXY = true;
				break;
			case 'T':
				if (element.option.equals("barcode")) {
					outObjects.add(new BarCodeObject(element));
					isAddedText = true;
				} else if (element.option.equals("qrcode")) {
					outObjects.add(new QRCodeObject(element));
					isAddedText = true;
				} else if (element.option.equals("barcode_ean")) {
					outObjects.add(new BarCodeEANObject(element));
					isAddedText = true;
				} else {
					if (element.getParam(0) > 0)
						mCurrentPresetFontSizes[0] = element.getParam(0);
					if (element.getParam(1) > 0)
						mCurrentPresetFontSizes[1] = element.getParam(1);
					if (element.getParam(2) > 0)
						mCurrentPresetFontSizes[2] = element.getParam(2);
					element.option = element.option.toLowerCase();
					mCurrentFontSizesIndex = 0;
					if (mEngFontMap.containsKey(element.option))
						mCurrentEngFontFace = element.option;
					if (mChnFontMap.containsKey(element.option))
						mCurrentChnFontFace = element.option;
				}
				break;
			case 'f':
				if (element.getParam(0) > 0)
					mCurrentPresetFontSizes[0] = element.getParam(0);
				if (element.getParam(1) > 0)
					mCurrentPresetFontSizes[1] = element.getParam(1);
				if (element.getParam(2) > 0)
					mCurrentPresetFontSizes[2] = element.getParam(2);
				mCurrentFontSizesIndex = 0;
				break;
			case 'w':
				mCurrentUseSecondFontSize = !mCurrentUseSecondFontSize;
				if (mCurrentPresetFontSizes[1] > 0)
					mCurrentFontSizesIndex = mCurrentUseSecondFontSize ? 1 : 0;
				break;
			case 's':
				mCurrentUseThirdFontSize = !mCurrentUseThirdFontSize;
				if (mCurrentPresetFontSizes[2] > 0)
					mCurrentFontSizesIndex = mCurrentUseThirdFontSize ? 2 : 0;
				break;
			case 'A':
				mCurrentColor = new ColorObject(element).makeBaseColor();
				break;
			case 'B':
				outObjects.add(new PictureObject(element));
				break;
			case 'L':
				outObjects.add(new LineObject(element));
				break;
			case 'M':
				outObjects.add(new MarkObject(element));
				break;
			case 'Z':
				mCurrentTemplateOffset = new TemplateOffsetObject(element).getOffset();
				break;
			case '.':
				outObjects.add(new CircleObject(element));
				break;
			case '@':
			case '^':
			case '~':
				element.text = " " + element.text;
				break;
			case 'l':
				if (element.getParam(0) > 0)
					mCurrentLineHeight = DOCUMENT_DPI / element.getParam(0);
				break;
			case 'j':
				if (element.getParam(0) == 0) {
					TextAlign textAlign = new TextAlign();
					textAlign.parent = mCurrentTextAlign;
					mCurrentTextAlign = textAlign;
				} else if (mCurrentTextAlign != null) {
					switch (element.getParam(0)) {
					case 1:
						mCurrentTextAlign.align = PdfContentByte.ALIGN_CENTER;
						mCurrentTextAlign.endX = element.getParam(1);
						isChangeXY = true;
						break;
					case 2:
						mCurrentTextAlign.align = PdfContentByte.ALIGN_RIGHT;
						mCurrentTextAlign.endX = element.getParam(1);
						isChangeXY = true;
						break;
					}
					if (isChangeXY) {
						mCurrentX = element.getParam(1);
						mCurrentIsRelativeX = false;
					}
					mCurrentTextAlign = mCurrentTextAlign.parent;
				}
				break;
			case 'b':
				mCurrentIsBoldStarted = !mCurrentIsBoldStarted;
				break;
			case 'r':
				mCurrentIsReverseStarted = !mCurrentIsReverseStarted;
				if (mCurrentIsReverseStarted && element.hasParam(0))
					mCurrentReserveTextBgColor = new ColorObject(element).makeBaseColor();
				else
					mCurrentReserveTextBgColor = DEFAULT_RESERVE_TEXT_BG_COLOR;
				break;
			case 'i':
				mCurrentIsItaticStarted = !mCurrentIsItaticStarted;
				break;
			case 'u':
				mCurrentIsUnderlineStarted = !mCurrentIsUnderlineStarted;
				if (mCurrentIsUnderlineStarted)
					mCurrentUnderLineNum++;
				break;
			case 'P':
			case '\f':
				outObjects.add(new ControlObject(element));
				mCurrentX = mCurrentIndent;
				mCurrentY = 0;
				mCurrentIsRelativeX = true;
				mCurrentIsRelativeY = true;
				break;
			}
			if (!isAddedText && !element.text.isEmpty()) {
				BaseObject lastObject = null;
				TextObject textObject = null;
				if (outObjects.size() > 0)
					lastObject = outObjects.get(outObjects.size() - 1);
				if (!isChangeXY && lastObject != null && lastObject instanceof TextObject && lastObject.x == mCurrentX && lastObject.y == mCurrentY)
					textObject = (TextObject) lastObject;
				else {
					textObject = new TextObject();
					outObjects.add(textObject);
				}
				textObject.appendText(element.text);
//				textObject.appendText(ChineseConvert.convertAuto2Gnew(element.text));
			} else if (isChangeXY)
				outObjects.add(new ControlObject(element));
		}
	}
	public float getPrintPx(int chnftrPx) {
		return getPrintPx(mChnftrDpi, chnftrPx);
	}
	public int getChnftrPx(float printPx) {
		return getChnftrPx(mChnftrDpi, printPx);
	}
	public static float getPrintPx(float chnftrDpi, int chnftrPx) {
		return chnftrPx / chnftrDpi * DOCUMENT_DPI;
	}
	public static int getChnftrPx(float chnftrDpi, float printPx) {
		return Math.round(printPx / DOCUMENT_DPI * chnftrDpi);
	}
	public static float dpi100ToPx(int chnftrPx) {
		return getPrintPx(CHNFTR_DPI, chnftrPx);
	}
	public static int pxToDpi100(float printPx) {
		return getChnftrPx(CHNFTR_DPI, printPx);
	}
	/*public static float getPrintPx(int chnftrPx) {
		return chnftrPx / gChnftrDpi * DOCUMENT_DPI;
	}
	public static int getChnftrPx(float printPx) {
		return Math.round(printPx / DOCUMENT_DPI * gChnftrDpi);
	}*/
	public static int umChnftrPx2ChnftrPx(int umPx) {
		return Math.round(umPx / 254f);
	}
	public static int mmChnftrPx2ChnftrPx(int mmPx) {
		return Math.round(mmPx / 0.254f);
	}
	private static float getChunkWidthPoint(Chunk chunk, String text) {
		return chunk.getFont().getBaseFont().getWidthPoint(text, chunk.getFont().getSize());
	}
	public void initCurrentTemplates(PdfTemplate[] templates) {
		if (templates != null)
			mCurrentTemplates = templates;
		else {
			mCurrentTemplates = new PdfTemplate[EVERYPAGE_TEMPLATE_COUNT];
			for (int i = 0; i < mCurrentTemplates.length; i++)
				mCurrentTemplates[i] = PdfTemplate.createTemplate(mWriter, mDocument.getPageSize().getWidth(), mDocument.getPageSize().getHeight());
		}
	}
	private void addCurrentTemplatesToCanvas() {
		for (PdfTemplate t : mCurrentTemplates)
			mCanvas.addTemplate(t, 0, 0);
	}
	public float getPrintX(int chnftrX) {
		return mDocument.left() + getPrintPx(chnftrX);
	}
	public float getPrintY(int chnftrY) {
		return mDocument.top() - getPrintPx(chnftrY);
	}
	public int getChnftrX(float px) {
		return getChnftrPx(px - mDocument.left());
	}
	public int getChnftrY(float px) {
		return getChnftrPx(mDocument.top() - px);
	}
	private static List<String> splitStringByNextLineChar(String src) {
		String[] ss = src.split("\\r\\n|\\r|\\n", -1);
		List<String> result = Arrays.asList(ss);
		return result;
	}
	private void parseOptions(Rectangle docPageSize, float pageWidthPx, float pageLengthPx, float chnftrDpi, int cpi, float lpi) {
		mChnftrDpi = chnftrDpi;
		mDocumentPageSize = docPageSize;
		mDocumentMarginLeft = (mDocumentPageSize.getWidth() - pageWidthPx) / 2;
		mDocumentMarginTop = (mDocumentPageSize.getHeight() - pageLengthPx) / 2;
		switch (cpi) {
			case 10:
				mCurrentPresetFontSizes[0] = 12;
				break;
			case 12:
				mCurrentPresetFontSizes[0] = 10;
				break;
			case 16:
				mCurrentPresetFontSizes[0] = 8;
				break;
			case 20:
				mCurrentPresetFontSizes[0] = 6;
				break;
		}
		mCurrentLineHeight = DOCUMENT_DPI / lpi;
		mCpi = cpi;
	}
	private void parseOptions(String options) {
		float pageWidth, pageLength;
		mDocumentPageSize = PageSize.A4;
		pageWidth = 8f;
		pageLength = 11f;
		
		int cpi = 16, lpi = 6;
		final Pattern PATTERN = Pattern.compile("-([a-z])(\\d+)");
		Matcher matcher = PATTERN.matcher(options);
		boolean autoMargin = true;
		while (matcher.find()) {
			char o = matcher.group(1).charAt(0);
			int n = Integer.parseInt(matcher.group(2));
			switch (o) {
			case 'm': {
					autoMargin = false;
					mDocumentMarginLeft = n;
					mDocumentMarginTop = n;
			}
					break;
			case 'p':
				if(n == 101) {

//					mDocumentPageSize = new Rectangle(0, 0, 595.275588f, 841.8897648f);
//					mDocumentPageSize = new Rectangle(0, 0, 580f,595.275588f);
//					mDocumentPageSize = new Rectangle(0, 0, 595f,430f);
					
					/*
					mDocumentPageSize = new Rectangle(0, 0, 612f,468f);
					pageWidth = 8f;
					pageLength = 6f;
					*/
//					mDocumentPageSize = new Rectangle(0, 0, 612f,612f);
					mDocumentPageSize = new Rectangle(0, 0, 595f,595f);
					pageWidth = 8f;
					pageLength = 8f;
					autoMargin = false;
					mDocumentMarginLeft = 9f;
					mDocumentMarginTop = 0f;
					break;
				}
				switch (n / 2) {
				case 1:
					mDocumentPageSize = PageSize.FLSA; //612x936
					pageWidth = 8f;
					pageLength = 13.5f;
					break;
				case 2:
					//mDocumentPageSize = PageSize.A5; 420x595
					mDocumentPageSize = new Rectangle(0, 0, 419.5275624f, 595.275588f);
					pageWidth = 5.5f;
					pageLength = 8f;
					break;
				case 3:
					//mDocumentPageSize = PageSize.B3; 1000x1417
					mDocumentPageSize = new Rectangle(0, 0, 1000.6299216f, 1417.3228368f);
					pageWidth = 11f;
					pageLength = 13.5f;
					break;
				case 7:
					//mDocumentPageSize = PageSize.A3; 842x1191
					mDocumentPageSize = new Rectangle(0, 0, 841.8897648f, 1190.5511832f);
					pageWidth = 11f;
					pageLength = 16f;
					break;
				default:
					//mDocumentPageSize = PageSize.A4; 595x842
					mDocumentPageSize = new Rectangle(0, 0, 595.275588f, 841.8897648f);
					pageWidth = 8f;
					pageLength = 11f;
					break;
				}
				if (n % 2 == 1) {
					mDocumentPageSize = mDocumentPageSize.rotate();
					float temp = pageWidth;
					pageWidth = pageLength;
					pageLength = temp;
				}
				break;
			case 'f':
				cpi = 16;
				lpi = 6;
				switch (n) {
				case 0:
					cpi = 10;
					lpi = 5;
					break;
				case 1:
					cpi = 10;
					lpi = 6;
					break;
				case 2:
					cpi = 12;
					lpi = 6;
					break;
				case 3:
					cpi = 12;
					lpi = 8;
					break;
				case 4:
					cpi = 16;
					lpi = 6;
					break;
				case 5:
					cpi = 16;
					lpi = 8;
					break;
				case 6:
					cpi = 16;
					lpi = 10;
					break;
				case 7:
					cpi = 20;
					lpi = 6;
					break;
				case 8:
					cpi = 20;
					lpi = 8;
					break;
				case 9:
					cpi = 20;
					lpi = 10;
					break;
				case 10:
					cpi = 20;
					lpi = 12;
					break;
				}
				break;
			}
		}

		if(autoMargin) {
			mDocumentMarginLeft = (mDocumentPageSize.getWidth() - pageWidth * DOCUMENT_DPI) / 2;
			mDocumentMarginTop = (mDocumentPageSize.getHeight() - pageLength * DOCUMENT_DPI) / 2;
		} else {
//			mDocumentMarginLeft = 9f;
//			mDocumentMarginTop = 0f;
			int cc;
			cc = 0;
		}
		switch (cpi) {
			case 10:
				mCurrentPresetFontSizes[0] = 12;
				break;
			case 12:
				mCurrentPresetFontSizes[0] = 10;
				break;
			case 16:
				mCurrentPresetFontSizes[0] = 8;
				break;
			case 20:
				mCurrentPresetFontSizes[0] = 6;
				break;
		}
		mCurrentLineHeight = DOCUMENT_DPI / lpi;
		mCpi = cpi;
	}
	public void print(String outputFilePath) throws FileNotFoundException, DocumentException, IOException {
		FileOutputStream os = new FileOutputStream(outputFilePath);
		print(os);
		os.close();
	}
	public void initWriter(OutputStream os) throws DocumentException {
		mDocument = new Document(mDocumentPageSize, mDocumentMarginLeft, mDocumentMarginLeft, mDocumentMarginTop, mDocumentMarginTop);
        mWriter = PdfWriter.getInstance(mDocument, os);
        mOutputStream = os;
	}
	public byte[] printToData() throws FileNotFoundException, DocumentException, IOException {
		try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
			print(os);
			return os.toByteArray();
		}
	}
	public void print(OutputStream os) throws FileNotFoundException, DocumentException, IOException {
		if (mOutputStream == null || os != mOutputStream)
			initWriter(os);
		mDocument.open();
        mCanvas = mWriter.getDirectContent();
        if (!mTemplateFileReaderList.isEmpty()) {
        	TemplateFileReader reader;
        	if (mHasNotChnftrStream) {
        		mElements.clear();
        		mObjects.clear();
        		mTemplateFileReaderList.reset();
        		int pageNum = 0;
        		int pageCount = mTemplateFileReaderList.totalPage();
        		while ((reader = mTemplateFileReaderList.next()) != null) {
        			pageNum++;
        			if (!mElements.isEmpty())
        				mElements.add(Element.newInstance("P"));
        			String chnftrText = reader.getChnftrText();
        			List<Element> eleList = parseChnftrText(chnftrText, pageNum, pageCount, reader.currentPage() + 1, reader.totalPage());
        			chnftrText = mTemplateFileReaderList.getChnftrText();
        			eleList.addAll(parseChnftrText(chnftrText, pageNum, pageCount, reader.currentPage() + 1, reader.totalPage()));
        			if (eleList.isEmpty())
        				eleList.add(Element.newInstance(" "));
       				mElements.addAll(eleList);
        		}
        		generateObjects();
        	}
        	mTemplateFileReaderList.reset();
        	reader = mTemplateFileReaderList.next();
        	if (reader != null) {
        		PdfImportedPage page = mWriter.getImportedPage(reader, reader.currentPage() + 1);
        		//mCanvas.addTemplate(page, -reader.getCropBox(1).getLeft(), -reader.getCropBox(1).getBottom());
				AffineTransform trans = new AffineTransform();
				trans.translate(-reader.getCropBox(1).getLeft(), -reader.getCropBox(1).getBottom());
				rotatePdfImportedPage(reader, page, trans);
        		//mCanvas.addTemplate(page, trans);
				addCanvasTemplate(page, trans);
        	}
        }

        initCurrentTemplates(null);
		mCurrentPage = 0;
		mCurrentOffsetPrintX = mCurrentOffsetPrintY = 0;
		mLastUnderLineInfo = null;
		mUnderLineInfoMap.clear();
		mPageImportedTemplatePageList.clear();
		mPageMarkObjectList.clear();
		for (BaseObject object : mObjects) {
			//if (object instanceof TextObject)
			//	System.out.println(object.getClass().getSimpleName() + " " + object.toString());
			if (!object.isRelativeX)
				mCurrentOffsetPrintX = 0;
			if (!object.isRelativeY)
				mCurrentOffsetPrintY = 0;
			object.print();
			mCurrentOffsetPrintX += object.getLastPrintX();
			mCurrentOffsetPrintY += object.getLastPrintY();
		}
		printUnderLineList();
		if (printCallback != null)
			printCallback.afterPage();
		addCurrentTemplatesToCanvas();
		mDocument.close();
		mTemplateFileReaderList.clear();
		mTemplateFileReaderMap.clear();
		if (mChnftrStream != null)
			mChnftrStream.close();
	}
	public void print(PdfTemplate[] templates) throws DocumentException {
		mDocument = new Document(mDocumentPageSize, mDocumentMarginLeft, mDocumentMarginLeft, mDocumentMarginTop, mDocumentMarginTop);
        initCurrentTemplates(templates);
		mCurrentPage = 0;
		mCurrentOffsetPrintX = mCurrentOffsetPrintY = 0;
		mLastUnderLineInfo = null;
		mUnderLineInfoMap.clear();
		mPageImportedTemplatePageList.clear();
		mPageMarkObjectList.clear();
		for (BaseObject object : mObjects) {
			if (!object.isRelativeX)
				mCurrentOffsetPrintX = 0;
			if (!object.isRelativeY)
				mCurrentOffsetPrintY = 0;
			object.print(templates);
			mCurrentOffsetPrintX += object.getLastPrintX();
			mCurrentOffsetPrintY += object.getLastPrintY();
		}
		printUnderLineList();
	}
	public void setUseAscender(boolean b) {
		useAscender = b;
	}
	private void addCanvasTemplate(PdfImportedPage page, AffineTransform trans) {
		double[] m = new double[6];
		trans.getMatrix(m);
     	mCanvas.addTemplate(page, (float)m[0], (float)m[1], (float)m[2], (float)m[3], (float)m[4], (float)m[5]);
	}
	private void addCanvasTemplate(PdfTemplate template, PdfImportedPage page, AffineTransform trans) {
		double[] m = new double[6];
		trans.getMatrix(m);
     	template.addTemplate(page, (float)m[0], (float)m[1], (float)m[2], (float)m[3], (float)m[4], (float)m[5]);
	}
	private void addCanvasTemplate(PdfTemplate template, PdfTemplate template1, AffineTransform trans) {
		double[] m = new double[6];
		trans.getMatrix(m);
     	template.addTemplate(template1, (float)m[0], (float)m[1], (float)m[2], (float)m[3], (float)m[4], (float)m[5]);
	}
	private static class TextAlign {
		int align = PdfContentByte.ALIGN_LEFT;
		int endX;
		TextAlign parent;
	}
	private static class PhraseEx extends Phrase {
		private static final long serialVersionUID = -2694263344610481930L;
		TextAlign textAlign;
		float textAscDes, textHeight, extraLineSpace;
		float offsetX, width;
		ArrayList<UnderLineInfo> underLineInfoList = new ArrayList<UnderLineInfo>();
		PhraseEx first, next;
	}
	public abstract class BaseObject {
		protected int x = mCurrentX, y = mCurrentY;
		protected boolean isRelativeX = mCurrentIsRelativeX, isRelativeY = mCurrentIsRelativeY;
		protected int indent = mCurrentIndent;
		public abstract boolean isValid();
		public abstract void print(PdfTemplate[] templates);
		public abstract void print();
		public abstract float getLastPrintX();
		public abstract float getLastPrintY();
		protected float getPrintX() {
			return ChnftrParser.this.getPrintX(x) + (isRelativeX ? mCurrentOffsetPrintX : 0);
		}
		protected float getPrintY() {
			return ChnftrParser.this.getPrintY(y) - (isRelativeY ? mCurrentOffsetPrintY : 0);
		}
	}
	public class ControlObject extends BaseObject {
		private char controlChar;
		public ControlObject(Element element) {
			controlChar = element.controlChar;
		}
		@Override
		public boolean isValid() {
			return true;
		}
		@Override
		public String toString() {
			return "" + controlChar;
		}
		@Override
		public float getLastPrintX() {
			return 0f;
		}
		@Override
		public float getLastPrintY() {
			return 0f;
		}
		@Override
		public void print(PdfTemplate[] templates) {
		}
		@Override
		public void print() {
			switch (controlChar) {
			case 'P':
			case '\f':
				printUnderLineList();
				if (printCallback != null)
					printCallback.afterPage();
				addCurrentTemplatesToCanvas();
				initCurrentTemplates(null);
				mPageImportedTemplatePageList.clear();
				mPageMarkObjectList.clear();
				mDocument.newPage();
				mCurrentPage++;
				if (!mTemplateFileReaderList.isEmpty()) {
					TemplateFileReader reader = mTemplateFileReaderList.next();
					if (reader == null) {
						mTemplateFileReaderList.reset();
						reader = mTemplateFileReaderList.next();
					}
					if (reader != null) {
						PdfImportedPage page = mWriter.getImportedPage(reader, reader.currentPage() + 1);
						//mCanvas.addTemplate(page, -reader.getCropBox(1).getLeft(), -reader.getCropBox(1).getBottom());
						//UniLog.log("chnparser getpagerotation:" + page.getRotation() + ",left:" + reader.getCropBox(1).getLeft() + ",bottom:" + reader.getCropBox(1).getBottom());
						UniLog.log("chnparser getpagerotation:" + reader.getPageRotation(page.getPageNumber()) + ",left:" + reader.getCropBox(1).getLeft() + ",bottom:" + reader.getCropBox(1).getBottom());
						AffineTransform trans = new AffineTransform();
						trans.translate(-reader.getCropBox(1).getLeft(), -reader.getCropBox(1).getBottom());
						rotatePdfImportedPage(reader, page, trans);
						//mCanvas.addTemplate(page, trans);
						addCanvasTemplate(page, trans);
					}
				}
				mCurrentOffsetPrintX = 0;
				mCurrentOffsetPrintY = 0;
				break;
			}
		}
	}
	public class TextObject extends BaseObject implements SplitChnEngTextListener {
		private String text = "";
		private LinkedList<PhraseEx> phrases = new LinkedList<PhraseEx>();
		private int templateOffset = mCurrentTemplateOffset;
		@Override
		public boolean isValid() {
			return !text.isEmpty() && phrases.size() > 0;
		}
		public void appendText(String text) {
			/*String src = text;
			final Pattern PATTERN_ENG = Pattern.compile("^([\\x00-\\xff]+)");
			final Pattern PATTERN_CHN = Pattern.compile("^([^\\x00-\\xff]+)");
			while (!src.isEmpty()) {
				Matcher matcher = PATTERN_CHN.matcher(src);
				String s;
				if (matcher.find()) {
					s = matcher.group(1);
					appendText(s, true);
				}
				else {
					matcher = PATTERN_ENG.matcher(src);
					if (matcher.find())
						s = matcher.group(1);
					else
						s = src;
					appendText(s, false);
				}
				src = src.substring(s.length());
			}*/
			splitChnEngText(text, this);
		}
		public void appendText(String text, boolean useChnFont) {
			if (text.isEmpty())
				return;
			if (useChnFont && mCurrentChnFontFace.equals("schinese"))
				text = ChineseConvert.convertAuto2Gnew(text);
			this.text += text;
			
			//init font
			Font font;
			float newTextAscDes;
			try {
				BaseFont bf = BaseFont.createFont(
						getCurrentFontPath(useChnFont),
						getCurrentFontEncoding(useChnFont),
						BaseFont.NOT_EMBEDDED);
				font = new Font(bf, mCurrentPresetFontSizes[mCurrentFontSizesIndex]);
				font.setStyle(((mCurrentIsBoldStarted || getCurrentFontBold(useChnFont)) ? Font.BOLD : 0)
						| ((mCurrentIsItaticStarted || getCurrentFontItatic(useChnFont)) ? Font.ITALIC : 0)
						/*| (mCurrentIsUnderlineStarted ? Font.UNDERLINE : 0)*/);
				font.setColor(mCurrentColor);
				String s = !text.trim().isEmpty() ? text : "ABC";
				newTextAscDes = bf.getAscentPoint(s, font.getSize()) - bf.getDescentPoint(s, font.getSize());
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				UniLog.logm(this,"err:1:" + e.getMessage());
				return;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				UniLog.logm(this,"err:2:" + e.getMessage());
				return;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				UniLog.logm(this,"err:3:" + e.getMessage());
				return;
			}
			
			//split lines
			List<String> splitStrings = splitStringByNextLineChar(text);
			Iterator<String> it = splitStrings.iterator();

			//first line
			PhraseEx p;
			if (!phrases.isEmpty()) {
				p = phrases.getLast();
				if (p.textAlign != mCurrentTextAlign) {
					PhraseEx oldp = p;
					p = new PhraseEx();
					p.first = oldp.first;
					p.offsetX = oldp.offsetX + oldp.width;
					phrases.add(p);
					oldp.next = p;
				} 
			} else {
				p = new PhraseEx();
				p.first = p;
				p.offsetX = 0f;
				phrases.add(p);
			}
			p.textAlign = mCurrentTextAlign;
			String lastString = it.next();
			Chunk chunk = new Chunk(lastString);
			if (mCurrentIsReverseStarted)
				chunk.setBackground(mCurrentReserveTextBgColor);
			chunk.setFont(font);
			p.add(chunk);
			float textWidth = getChunkWidthPoint(chunk, lastString);
			p.width += textWidth;
			if (mCurrentIsUnderlineStarted)
				p.underLineInfoList.add(new UnderLineInfo(p.offsetX + p.width - textWidth, p.offsetX + p.width));
			p.first.textAscDes = Math.max(newTextAscDes, p.first.textAscDes);
			p.first.textHeight = Math.max(font.getSize(), p.first.textHeight);
			p.first.extraLineSpace = Math.max(mCurrentLineHeight - p.first.textHeight, 1f);
			
			//more line
			int addedLine = 0;
			while (it.hasNext()) {
				lastString = it.next();
				chunk = new Chunk(lastString);
				if (mCurrentIsReverseStarted)
					chunk.setBackground(mCurrentReserveTextBgColor);
				chunk.setFont(font);
				textWidth = getChunkWidthPoint(chunk, lastString);
				p = new PhraseEx();
				p.first = p;
				p.offsetX = 0f;
				++addedLine;
				phrases.add(p);
				p.textAlign = mCurrentTextAlign;
				p.add(chunk);

				p.width = textWidth;
				if (mCurrentIsUnderlineStarted)
					p.underLineInfoList.add(new UnderLineInfo(p.offsetX, p.offsetX + p.width));
				p.textAscDes = newTextAscDes;
				p.textHeight = font.getSize();
				p.extraLineSpace = Math.max(mCurrentLineHeight - p.textHeight, 1f);
			}
			if (addedLine > 0) {
				mCurrentX = indent;
				mCurrentIsRelativeX = true;
				mCurrentIsRelativeY = true;
			}
		}
		@Override
		public String toString() {
			return "x=" + x + ", y=" + y 
					+ ", text=" + text;
		}
		@Override
		public float getLastPrintX() {
			return !phrases.isEmpty() ? phrases.getLast().offsetX + phrases.getLast().width : 0f;
		}
		@Override
		public float getLastPrintY() {
			float result = 0f;
			for (int i = 1; i < phrases.size(); i++) {
				PhraseEx p = phrases.get(i);
				if (p.first == p)
					result += p.textHeight + p.extraLineSpace;
			}
			return result;
		}
		@Override
		public void print(PdfTemplate[] templates) {
			if (!isValid())
				return;
			ColumnText columnText = new ColumnText(templates[templateOffset + 1]);
			columnText.setUseAscender(useAscender);
			UnderLineInfo lastUnderLineInfo = null;
			float printX, printY = getPrintY();
			for (PhraseEx p : phrases) {
				columnText.setExtraParagraphSpace(p.first.extraLineSpace);
				float prtX;
				if (printY < getPrintY()) {
					mCurrentOffsetPrintX = 0f;
					prtX = ChnftrParser.this.getPrintX(indent);
				} else
					prtX = getPrintX();
				printX = prtX + p.offsetX;
				//float printY = getPrintY() - p.lineNum * (p.textHeight + p.extraLineSpace);
				float textWidth = p.width;
				float textHeight = p.first.textHeight + p.first.extraLineSpace;
				float underLineOffset = 0f;
				if (p.textAlign != null && p.textAlign.endX > 0) {
					float oldTextWidth = textWidth;
					textWidth = ChnftrParser.this.getPrintX(p.textAlign.endX) - p.offsetX - prtX;
					if (textWidth > oldTextWidth) {
						underLineOffset = textWidth - oldTextWidth;
						if (p.textAlign.align == PdfContentByte.ALIGN_CENTER)
							underLineOffset /= 2;
					}
					columnText.setSimpleColumn(p, printX, printY - textHeight, printX + textWidth, printY, p.first.textHeight, p.textAlign.align);
				}
				else
					columnText.setSimpleColumn(p, printX, printY - textHeight, printX + textWidth + 1.0f, printY, p.first.textHeight, PdfContentByte.ALIGN_LEFT);
				try {
					columnText.go();
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				for (int i = 0; i < p.underLineInfoList.size(); i++) {
					UnderLineInfo uli = p.underLineInfoList.get(i);
					float startX = prtX + underLineOffset + uli.startX;
					float endX = prtX + underLineOffset + uli.endX;
					float Y = printY;
					//float Y = printY - textHeight + p.first.extraLineSpace;
					//float Y = printY - p.first.textAscDes;
					if (mLastUnderLineInfo != null && mLastUnderLineInfo.number == uli.number 
							&& mLastUnderLineInfo.Y == Y && mLastUnderLineInfo.endX < startX)
						startX = mLastUnderLineInfo.endX;
					Map<Float, List<UnderLineInfo>> map = mUnderLineInfoMap.get(uli.number);
					if (map == null)
						map = new LinkedHashMap<Float, List<UnderLineInfo>>();
					List<UnderLineInfo> list = map.get(Y);
					if (list == null)
						list = new ArrayList<UnderLineInfo>();
					list.add(new UnderLineInfo(uli.number, startX, endX, Y, p.first.textHeight, p.first.textAscDes, uli.color));
					map.put(Y, list);
					mUnderLineInfoMap.put(uli.number, map);
					/*templates[1].saveState();
					templates[1].setLineWidth(0.2f);
					templates[1].moveTo(startX, Y);
					templates[1].lineTo(endX, Y);
					templates[1].stroke();
					templates[1].restoreState();*/
					if (i == p.underLineInfoList.size() - 1)
						lastUnderLineInfo = new UnderLineInfo(uli.number, startX, endX, Y, p.first.textHeight, p.first.textAscDes, uli.color);
					mLastUnderLineInfo = null;
				}
				if (p.next == null)
					printY -= textHeight;
			}
			mLastUnderLineInfo = lastUnderLineInfo;
		}
		@Override
		public void print() {
			print(mCurrentTemplates);
		}
	}
	public class BarCodeObject extends BaseObject {
		protected int ratio;
		protected boolean isDoubleWidth;
		protected int width, height;
		protected String text = "";
		protected Image barcodeImage;
		private int templateOffset = mCurrentTemplateOffset;
		public BarCodeObject(Element element) {
			ratio = element.getParam(1);
			isDoubleWidth = element.getParam(2) == 1;
			height = element.getParam(3);
			width = element.getParam(4);
			text = element.text;
		}
		@Override
		public boolean isValid() {
			return ratio >= 0 && !text.isEmpty();
		}
		@Override
		public String toString() {
			return "ratio=" + ratio + ", text=" + text;
		}
		@Override
		public float getLastPrintX() {
			return barcodeImage != null ? barcodeImage.getScaledWidth() : 0f;
		}
		@Override
		public float getLastPrintY() {
			return 0f;
		}
		@Override
		public void print(PdfTemplate[] templates) {
			if (!isValid())
				return;
			//UniLog.log("width:" + width + ",height:" + height);
			Barcode128 barcode = new Barcode128();
			barcode.setCode(text);
			barcode.setFont(null);
			barcode.setBarHeight(height > 0 ? getPrintPx(height) : 16.0f);
			barcodeImage = barcode.createImageWithBarcode(templates[templateOffset + 0], null, null);
			float w = barcodeImage.getWidth();
			if (width > 0)
				w = getPrintPx(width);
			else {
				if (ratio > 1)
					w += w * (ratio - 1) * 0.5f;
				else if (ratio < 1)
					w /= 2;
				if (isDoubleWidth)
					w *= 2;
			}
			barcodeImage.scaleAbsoluteWidth(w);
			barcodeImage.setAbsolutePosition(getPrintX(), getPrintY() - barcodeImage.getScaledHeight());
			try {
				templates[templateOffset + 0].addImage(barcodeImage);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		@Override
		public void print() {
			print(mCurrentTemplates);
		}
	}
	private class BarCodeEANObject extends BarCodeObject {
		private int baseline;
		private int templateOffset = mCurrentTemplateOffset;
		public BarCodeEANObject(Element element) {
			super(element);
			baseline = element.getParam(5);
		}
		@Override
		public void print(PdfTemplate[] templates) {
			if (!isValid())
				return;
			try {
				BarcodeEAN barcode = new BarcodeEAN();
				barcode.setCode(text);

				BaseFont bf = BaseFont.createFont(
						getCurrentFontPath(false),
						getCurrentFontEncoding(false),
						BaseFont.NOT_EMBEDDED);
				int fontSize = mCurrentPresetFontSizes[mCurrentFontSizesIndex];
				barcode.setFont(bf);
				barcode.setBaseline(baseline != 0 ? baseline : fontSize);
				barcode.setSize(fontSize);

				barcode.setBarHeight(height > 0 ? getPrintPx(height) : 16.0f);
				barcodeImage = barcode.createImageWithBarcode(templates[templateOffset + 0], null, null);
				float w = barcodeImage.getWidth();
				if (width > 0)
					w = getPrintPx(width);
				else {
					if (ratio > 1)
						w += w * (ratio - 1) * 0.5f;
					else if (ratio < 1)
						w /= 2;
					if (isDoubleWidth)
						w *= 2;
				}
				barcodeImage.scaleAbsoluteWidth(w);
				barcodeImage.setAbsolutePosition(getPrintX(), getPrintY() - barcodeImage.getScaledHeight());
				templates[templateOffset + 0].addImage(barcodeImage);
			} catch (DocumentException e1) {
				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		@Override
		public void print() {
			print(mCurrentTemplates);
		}
	}
	public class QRCodeObject extends BaseObject {
		private int width, height;
		private String text = "";
		private Image qrcodeImage;
		private int templateOffset = mCurrentTemplateOffset;
		public QRCodeObject(Element element) {
			height = element.getParam(0);
			width = element.getParam(1);
			text = element.text;
		}
		@Override
		public boolean isValid() {
			return width >= 0 && height >= 0 && !text.isEmpty();
		}
		@Override
		public String toString() {
			return "width=" + width + ", height=" + height + ", text=" + text;
		}
		@Override
		public float getLastPrintX() {
			return qrcodeImage != null ? qrcodeImage.getScaledWidth() : 0f;
		}
		@Override
		public float getLastPrintY() {
			return 0f;
		}
		@Override
		public void print(PdfTemplate[] templates) {
			if (!isValid())
				return;
			int pw = Math.round(getPrintPx(width));
			int ph = Math.round(getPrintPx(height));
			BarcodeQRCode qrcode;
			if (pw > 0 && ph > 0)
				qrcode = new BarcodeQRCode(text, pw, ph, null);
			else if (pw > 0)
				qrcode = new BarcodeQRCode(text, pw, pw, null);
			else if (ph > 0)
				qrcode = new BarcodeQRCode(text, ph, ph, null);
			else
				qrcode = new BarcodeQRCode(text, 1, 1, null);
			try {
				qrcodeImage = qrcode.getImage();
				if (width > 0)
					qrcodeImage.scaleAbsoluteWidth(getPrintPx(width));
				if (height > 0)
					qrcodeImage.scaleAbsoluteHeight(getPrintPx(height));
				qrcodeImage.setAbsolutePosition(getPrintX(), getPrintY() - qrcodeImage.getScaledHeight());
				templates[templateOffset + 0].addImage(qrcodeImage);
			} catch (BadElementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		@Override
		public void print() {
			print(mCurrentTemplates);
		}
	}
	public class MarkObject extends BaseObject {
		private int width, height;
		public MarkObject(Element element) {
			height = element.getParam(0);
			width = element.getParam(1);
		}
		@Override
		public boolean isValid() {
			return true;
		}
		@Override
		public String toString() {
			return String.format("x:%d,y:%d,width:%d,height:%d", x, y, width, height);
		}
		@Override
		public float getLastPrintX() {
			return 0f;
		}
		@Override
		public float getLastPrintY() {
			return 0f;
		}
		@Override
		public void print(PdfTemplate[] templates) {
			print();
		}
		@Override
		public void print() {
			MarkObjectInfo moi = new MarkObjectInfo();
			moi.left = getPrintX();
			moi.top = getPrintY();
			moi.width = getPrintPx(width);
			moi.height = getPrintPx(height);
			moi.cLeft = getChnftrX(moi.left);
			moi.cTop = getChnftrY(moi.top);
			moi.cWidth = getChnftrPx(moi.width);
			moi.cHeight = getChnftrPx(moi.height);
			mPageMarkObjectList.add(moi);
		}
	}
	public class PictureObject extends BaseObject {
		private String filePath = "";
		private int type, width, height;
		private boolean reverse;
		private Image image;
     	private PdfImportedPage importPage;
     	private PdfTemplate template;
     	private boolean isCenterOrigin;
     	private float scaledWidth;
		private float scaledHeight;
		private int templateOffset = mCurrentTemplateOffset;
		public PictureObject(Element element) {
			filePath = element.option;
			type = element.getParam(0);
			height = element.getParam(1);
			width = element.getParam(2);
			reverse = element.getParam(3) != 1;
		}
		
		@Override
		public boolean isValid() {
			if(useGetImageInterfaceByDefault) {
				if (chnftrGetImageInterface != null){
					return true;
				}
			}
			if (StringUtils.startsWith(filePath, GETIMAGE_TAG)){
				if (chnftrGetImageInterface == null){
					return false;
				}
				if (filePath.trim().length() <= GETIMAGE_TAG.length()){
					return false;
				}
				return true;
			}
			if (StringUtils.startsWith(filePath, GETTEMPLATE_TAG) && filePath.trim().length() > GETTEMPLATE_TAG.length())
				return true;
			return !filePath.isEmpty() && new File(filePath).exists();
		}
		@Override
		public String toString() {
			return filePath;
		}
		@Override
		public float getLastPrintX() {
			if (scaledWidth > 0)
				return scaledWidth;
			return image != null ? image.getScaledWidth() : 0f;
		}
		@Override
		public float getLastPrintY() {
			if (scaledHeight > 0)
				return scaledHeight;
			return image != null ? image.getScaledHeight() : 0f;
		}
		@Override
		public void print(PdfTemplate[] templates) {
			if (!isValid())
				return;
			InputStream inStream = null;
			boolean rotated = false;
			try {
				if (StringUtils.startsWith(filePath, GETTEMPLATE_TAG)){
					String[] ss = filePath.substring(GETTEMPLATE_TAG.length()).trim().split(",");
					String name = ss[0];
					if (mTemplateFileReaderMap.containsKey(name)) {
						int page = Integer.parseInt(ss[1]);
						if (ss.length > 2)
							isCenterOrigin = Integer.parseInt(ss[2]) == 1;
						TemplateFileReader reader = mTemplateFileReaderMap.get(name);
						importPage = mWriter.getImportedPage(reader, page + 1);
					}
					else
						template = mTemplateMap.get(name);
				} else if (useGetImageInterfaceByDefault || StringUtils.startsWith(filePath, GETIMAGE_TAG)){
					byte[] imageBytes = null;
					if(useGetImageInterfaceByDefault) {
						imageBytes = chnftrGetImageInterface.getImage(filePath);
					} else {
						imageBytes = chnftrGetImageInterface.getImage(filePath.substring(GETIMAGE_TAG.length()));
					}
					if (imageBytes == null || imageBytes.length <= 0){
						UniLog.logm(this,"imageBytes is empty");
						return;
					}
					
					image = Image.getInstance(imageBytes);
					float angle = getExifOrientation(imageBytes);
					if (angle != 0){
						if(type != 19) image.setRotationDegrees(angle * -1);
						
						//if image rotated, swap width and height
						if (Math.abs(angle) == 90 || Math.abs(angle) == 270){
							rotated = true;
							if(type != 19) {
							int tmpWidth =  width;
							width = height;
							height = tmpWidth;
							}
						}
					}
					
				} else if (type == 17 || filePath.toLowerCase(Locale.US).endsWith(".pcx")) {
					inStream = new FileInputStream(filePath);
					PCXReader reader = new PCXReader();
					reader.unpackImage(inStream);
					int[] imgData = reader.getImageData();
					int r = reverse ? 0xff : 0;
					byte[] data = new byte[imgData.length * 3];
					for (int i = 0; i < imgData.length; i++) {
						data[i*3] = (byte) (imgData[i] & 0xff ^ r);
						data[i*3+1] = (byte) (imgData[i] >> 8 & 0xff ^ r);
						data[i*3+2] = (byte) (imgData[i] >> 16 & 0xff ^ r);
					}
					image = Image.getInstance(reader.getImageWidth(), reader.getImageHeight(), 3, 8, data);
				} else {
					image = Image.getInstance(filePath);
				}
				if (importPage != null) {
					ImportedTemplatePageInfo tPageInfo = new ImportedTemplatePageInfo();
					tPageInfo.page = importPage;
					mPageImportedTemplatePageList.add(tPageInfo);
					AffineTransform trans = new AffineTransform();
					if (isCenterOrigin) {
						tPageInfo.width = importPage.getWidth();
						tPageInfo.height = importPage.getHeight();
						tPageInfo.left = getPrintX() + getPrintPx(width) / 2 - tPageInfo.width / 2;
						tPageInfo.top = getPrintY() - tPageInfo.height - (getPrintPx(height) / 2 - tPageInfo.height / 2);
						scaledWidth = tPageInfo.width;
						scaledHeight = tPageInfo.height;
						trans.translate(tPageInfo.left, tPageInfo.top);
						//UniLog.log("printx:" + getPrintPx(width) + ",pagewidth:" + importPage.getWidth() + 
						//		",printy:" + getPrintPx(height) + ",pageheight:" + importPage.getHeight());
					} else {
						float scaleX, scaleY;
						if (width > 0 && height > 0) {
							scaleX = getPrintPx(width) / importPage.getWidth();
							scaleY = getPrintPx(height) / importPage.getHeight();
						} else if (width > 0 && importPage.getWidth() > 0)
							scaleX = scaleY = getPrintPx(width) / importPage.getWidth();
						else if (height > 0 && importPage.getHeight() > 0)
							scaleX = scaleY = getPrintPx(height) / importPage.getHeight();
						else
							scaleX = scaleY = 1f;
						tPageInfo.width = importPage.getWidth() * scaleX;
						tPageInfo.height = importPage.getHeight() * scaleY;
						tPageInfo.left = getPrintX();
						tPageInfo.top = getPrintY() - tPageInfo.height;
						scaledWidth = tPageInfo.width;
						scaledHeight = tPageInfo.height;
						trans.translate(tPageInfo.left, tPageInfo.top);
						trans.scale(scaleX, scaleY);
					}
					tPageInfo.cLeft = getChnftrX(tPageInfo.left);
					tPageInfo.cTop = getChnftrY(tPageInfo.top + tPageInfo.height);
					tPageInfo.cWidth = getChnftrPx(tPageInfo.width);
					tPageInfo.cHeight = getChnftrPx(tPageInfo.height);
					//templates[0].addTemplate(importPage, trans);
					addCanvasTemplate(templates[templateOffset + 0], importPage, trans);
				} 
				else if (template != null) {
					AffineTransform trans = new AffineTransform();
					float scaleX, scaleY;
					if (width > 0 && height > 0) {
						scaleX = getPrintPx(width) / template.getWidth();
						scaleY = getPrintPx(height) / template.getHeight();
					} 
					else if (width > 0 && template.getHeight() > 0)
						scaleX = scaleY = getPrintPx(height) / template.getHeight();
					else
						scaleX = scaleY = 1f;
					scaledWidth = template.getWidth() * scaleX;
					scaledHeight = template.getHeight() * scaleY;
					trans.translate(getPrintX(), getPrintY() - scaledHeight);
					addCanvasTemplate(templates[templateOffset + 0], template, trans);
				}
				else {
					if(rotated && type == 19) {
						if (width > 0 && height > 0) {
							image.scaleAbsoluteWidth(getPrintPx(width));
							image.scaleAbsoluteHeight(getPrintPx(height));
						} else {
							if(image.getWidth() > 0 && image.getHeight() > 0 ) {
								float aspect = image.getWidth() / image.getHeight();
								if (width > 0 ) {
									image.scaleAbsoluteWidth(getPrintPx(width));
									image.scaleAbsoluteHeight(getPrintPx(width) * aspect);
								} else if(height > 0) {
									image.scaleAbsoluteHeight(getPrintPx(height));
									image.scaleAbsoluteWidth(getPrintPx(height) / aspect);
								}
							}
						}
						/*
						else if (width > 0 && image.getWidth() > 0)
							image.scalePercent(getPrintPx(width) / image.getHeight() * 100);
						else if (height > 0 && image.getHeight() > 0)
							image.scalePercent(getPrintPx(height) / image.getWidth() * 100);
						else
							image.scalePercent(DOCUMENT_DPI / IMAGE_SOURCE_DPI * 100f);
						image.setAbsolutePosition(getPrintX(), getPrintY() - image.getScaledWidth());
						*/
//						image.scalePercent(30f);
//						image.setAbsolutePosition(getPrintX(), getPrintY() - image.getScaledWidth());
//						image.scalePercent(DOCUMENT_DPI / IMAGE_SOURCE_DPI * 30);
					} else {
						if (width > 0 && height > 0) {
							image.scaleAbsoluteWidth(getPrintPx(width));
							image.scaleAbsoluteHeight(getPrintPx(height));
						} else if (width > 0 && image.getWidth() > 0)
							image.scalePercent(getPrintPx(width) / image.getWidth() * 100);
						else if (height > 0 && image.getHeight() > 0)
							image.scalePercent(getPrintPx(height) / image.getHeight() * 100);
						else
							image.scalePercent(DOCUMENT_DPI / IMAGE_SOURCE_DPI * 100f);
					}
					image.setAbsolutePosition(getPrintX(), getPrintY() - image.getScaledHeight());
					templates[templateOffset + 0].addImage(image);
				}
			} catch (BadElementException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (inStream != null) {
					try {
						inStream.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		@Override
		public void print() {
			print(mCurrentTemplates);
		}
	}
	private static enum LINE_TYPE {LINE, BOX, SHADE, SOLID, UNKNOWN};
	public class LineObject extends BaseObject {
		private int left;
		private int top;
		private int right;
		private int bottom;
		private LINE_TYPE type = LINE_TYPE.UNKNOWN;
		private int thick;
		private float lineWidth;
		private BaseColor color = mCurrentColor;
		private int templateOffset = mCurrentTemplateOffset;
		public LineObject(Element element) {
			left = element.getParam(0);
			top = element.getParam(1);
			right = element.getParam(2);
			bottom = element.getParam(3);
			int num = element.getParam(4);
			int lw = element.getParam(5);
			if (num < 10) {
				type = LINE_TYPE.LINE;
				thick = num;
			} else if (num < 30) {
				type = LINE_TYPE.BOX;
				thick = num - 10;
			} else if (num < 40) {
				type = LINE_TYPE.SHADE;
				thick = num - 30;
			} else {
				type = LINE_TYPE.SOLID;
				thick = num - 40;
			}
			if (lw > 0)
				lineWidth = (float) lw / 100;
			else
				lineWidth = 0.5f;
		}
		public void setColor(BaseColor color) {
			this.color = color;
		}
		@Override
		public boolean isValid() {
			return type != LINE_TYPE.UNKNOWN && thick >= 0;
		}
		@Override
		public String toString() {
			return "left=" + left + ", top=" + top + ", right=" + right + ", bottom=" + bottom
					+ ", type=" + type + ", thick=" + thick;
		}
		@Override
		public float getLastPrintX() {
			return 0f;
		}
		@Override
		public float getLastPrintY() {
			return 0f;
		}
		protected float getPrintX(int chnftrX) {
			return ChnftrParser.this.getPrintX(chnftrX);
		}
		protected float getPrintY(int chnftrY) {
			return ChnftrParser.this.getPrintY(chnftrY);
		}
		@Override
		public void print(PdfTemplate[] templates) {
			if (!isValid())
				return;
			//UniLog.logm(this, "lineprint %d %d %d %d", left, top, right, bottom);
			templates[templateOffset + 0].saveState();
			templates[templateOffset + 1].saveState();
			switch (thick) {
			case 1:
				templates[templateOffset + 1].setLineDash(1, 5, 1);
				break;
			case 2:
				templates[templateOffset + 1].setLineDash(2, 4, 1);
				break;
			case 3:
				templates[templateOffset + 1].setLineDash(1, 2, 1);
				break;
			case 4:
				templates[templateOffset + 1].setLineDash(2, 3, 1);
				break;
			default:
				templates[templateOffset + 1].setLineDash(0);
				break;
			}
			switch (type) {
			case LINE:
				templates[templateOffset + 1].setLineWidth(lineWidth);
				templates[templateOffset + 1].moveTo(getPrintX(left), getPrintY(top));
				templates[templateOffset + 1].lineTo(getPrintX(right), getPrintY(bottom));
				templates[templateOffset + 1].setColorStroke(color);
				templates[templateOffset + 1].stroke();
				break;
			case BOX:
				Rectangle rect = new Rectangle(getPrintX(left), getPrintY(top), getPrintX(right), getPrintY(bottom));
				rect.setBorder(Rectangle.BOX);
				rect.setBorderWidth(lineWidth);
				templates[templateOffset + 1].setColorStroke(color);
				templates[templateOffset + 1].rectangle(rect);
				break;
			case SHADE:
				PdfGState gs = new PdfGState();
				gs.setFillOpacity(0.8f);
				templates[templateOffset + 0].setGState(gs);
				rect = new Rectangle(getPrintX(left), getPrintY(top), getPrintX(right), getPrintY(bottom));
				rect.setBorder(Rectangle.NO_BORDER);
				rect.setGrayFill(0.4f * thick);
				templates[templateOffset + 0].rectangle(rect);
				break;
			case SOLID:
				if (thick > 0 && thick < 10) {
					gs = new PdfGState();
					gs.setFillOpacity(1 - thick / 10f);
					templates[templateOffset + 0].setGState(gs);
				}
				rect = new Rectangle(getPrintX(left), getPrintY(top), getPrintX(right), getPrintY(bottom));
				rect.setBorder(Rectangle.NO_BORDER);
				rect.setBackgroundColor(color);
				templates[templateOffset + 0].rectangle(rect);
				break;
			default:
				break;
			}
			templates[templateOffset + 0].restoreState();
			templates[templateOffset + 1].restoreState();
		}
		@Override
		public void print() {
			print(mCurrentTemplates);
		}
	}
	public class CircleObject extends BaseObject {
		private int diameter;
		private BaseColor color = mCurrentColor;
		private int templateOffset = mCurrentTemplateOffset;
		public CircleObject(Element element) {
			diameter = element.getParam(0);
		}
		@Override
		public boolean isValid() {
			return diameter > 0;
		}

		@Override
		public void print(PdfTemplate[] templates) {
			if (!isValid())
				return;
			PdfTemplate template = templates[templateOffset + 0];
			template.saveState();
			template.setColorFill(color);
			template.circle(getPrintX(), getPrintY(), getPrintPx(diameter) / 2);
			template.fill();
			template.restoreState();
		}
		@Override
		public void print() {
			print(mCurrentTemplates);
		}

		@Override
		public float getLastPrintX() {
			return 0;
		}

		@Override
		public float getLastPrintY() {
			return 0;
		}

	}
	public class TemplateOffsetObject extends BaseObject {
		int offset;
		public TemplateOffsetObject(Element element) {
			offset = element.getParam(0);
		}
		public int getOffset() {
			return offset;
		}
		@Override
		public boolean isValid() {
			return false;
		}
		@Override
		public void print(PdfTemplate[] templates) {
		}
		@Override
		public void print() {
		}
		@Override
		public float getLastPrintX() {
			return 0;
		}
		@Override
		public float getLastPrintY() {
			return 0;
		}
	}
	public class ColorObject extends BaseObject {
		int r, g, b;
		public ColorObject(Element element) {
			r = element.getParam(0);
			g = element.getParam(1);
			b = element.getParam(2);
		}
		public BaseColor makeBaseColor() {
			return new BaseColor(r, g, b);
		}
		@Override
		public boolean isValid() {
			return false;
		}
		@Override
		public void print(PdfTemplate[] templates) {
		}
		@Override
		public void print() {
		}
		@Override
		public float getLastPrintX() {
			return 0;
		}
		@Override
		public float getLastPrintY() {
			return 0;
		}
	}
	private class UnderLineInfo {
		int number;
		float startX, endX, Y;
		float textHeight;
		float textAscDes;
		BaseColor color;
		public UnderLineInfo(int number, float startX, float endX, float Y, float textHeight, float textAscDes, BaseColor color) {
			this.number = number;
			this.startX = startX;
			this.endX = endX;
			this.Y = Y;
			this.textHeight = textHeight;
			this.textAscDes = textAscDes;
			this.color = color;
		}
		public UnderLineInfo(float startX, float endX) {
			this(mCurrentUnderLineNum, startX, endX, 0f, 0f, 0f, mCurrentColor);
		}
	}
	private static final Pattern PATTERN_FONT_NAME = Pattern.compile("[\\w-]+\\.(?i)(ttf|ttc,\\d)");
	private String getCurrentFontPath(boolean useChnFont) {
		if (useChnFont) {
			if (mChnFontMap.containsKey(mCurrentChnFontFace)) {
				//return FONT_PATH + File.separator + mChnFontMap.get(mCurrentChnFontFace).faceOrFName;
				String fontName = mChnFontMap.get(mCurrentChnFontFace).faceOrFName;
				return getFontPath(fontName);
			}
		} else {
			if (mEngFontMap.containsKey(mCurrentEngFontFace)) {
				String fontName = mEngFontMap.get(mCurrentEngFontFace).faceOrFName;
				return PATTERN_FONT_NAME.matcher(fontName).matches() 
						? getFontPath(fontName) : fontName;
			}
		}
		return "";
	}
	private String getCurrentFontEncoding(boolean useChnFont) {
		if (useChnFont) {
			if (mChnFontMap.containsKey(mCurrentChnFontFace))
				return BaseFont.IDENTITY_H;
		} else {
			if (mEngFontMap.containsKey(mCurrentEngFontFace)) {
				String fontName = mEngFontMap.get(mCurrentEngFontFace).faceOrFName;
				return PATTERN_FONT_NAME.matcher(fontName).matches() 
						? BaseFont.IDENTITY_H : BaseFont.WINANSI;
			}
		}
		return BaseFont.WINANSI;
	}
	private boolean getCurrentFontBold(boolean useChnFont) {
		if (useChnFont) {
			if (mChnFontMap.containsKey(mCurrentChnFontFace))
				return mChnFontMap.get(mCurrentChnFontFace).bold;
		} else {
			if (mEngFontMap.containsKey(mCurrentEngFontFace))
				return mEngFontMap.get(mCurrentEngFontFace).bold;
		}
		return false;
	}
	private boolean getCurrentFontItatic(boolean useChnFont) {
		if (useChnFont) {
			if (mChnFontMap.containsKey(mCurrentChnFontFace))
				return mChnFontMap.get(mCurrentChnFontFace).itatic;
		} else {
			if (mEngFontMap.containsKey(mCurrentEngFontFace))
				return mEngFontMap.get(mCurrentEngFontFace).itatic;
		}
		return false;
	}
	private static String getFontPath(String fontFace) {
		if (StringUtils.equalsAny(fontFace, "ubuntumono-r.ttf", "simkai.ttf", "ariblk.ttf", "frabk.ttf"))
			return SPEC_FONT_PATH + "/" + fontFace;
		String fontFace1 = fontFace.replace(",0", "");
		String path = FONT_PATH + File.separator + fontFace;
		String path1 = FONT_PATH + File.separator + fontFace1;
		if (!new File(path1).exists() && FONT_PATH.startsWith("C:\\WINDOWS")) {
			String path2 = String.format("C:\\Users\\%s\\AppData\\Local\\Microsoft\\Windows\\Fonts\\%s", System.getProperty("user.name"), fontFace);
			String path3 = String.format("C:\\Users\\%s\\AppData\\Local\\Microsoft\\Windows\\Fonts\\%s", System.getProperty("user.name"), fontFace1);
			if (new File(path3).exists())
				return path2;
		}
		return path;
	}
	public static String getFontPath(String fontFace, boolean useChnFont) {
		if (useChnFont) {
			if (mChnFontMap.containsKey(fontFace)) {
				//return FONT_PATH + File.separator + mChnFontMap.get(fontFace).faceOrFName;
				String fontName = mChnFontMap.get(fontFace).faceOrFName;
				return getFontPath(fontName);
			} else {
				String fontName = mChnFontMap.get("chinese").faceOrFName;
				return getFontPath(fontName);
			}
		} else {
			if (mEngFontMap.containsKey(fontFace)) {
				String fontName = mEngFontMap.get(fontFace).faceOrFName;
				return PATTERN_FONT_NAME.matcher(fontName).matches() 
						? getFontPath(fontName) : fontName;
			} else {
				String fontName = mEngFontMap.get("ascii").faceOrFName;
				return getFontPath(fontName);
			}
		}
	}
	public static String getFontEncoding(String fontFace, boolean useChnFont) {
		if (useChnFont) {
			if (mChnFontMap.containsKey(fontFace))
				return BaseFont.IDENTITY_H;
		} else {
			if (mEngFontMap.containsKey(fontFace)) {
				String fontName = mEngFontMap.get(fontFace).faceOrFName;
				return PATTERN_FONT_NAME.matcher(fontName).matches() 
						? BaseFont.IDENTITY_H : BaseFont.WINANSI;
			}
		}
		return BaseFont.WINANSI;
	}
	private static boolean isFontBold(String fontFace, boolean useChnFont) {
		if (useChnFont) {
			if (mChnFontMap.containsKey(fontFace))
				return mChnFontMap.get(fontFace).bold;
		} else {
			if (mEngFontMap.containsKey(fontFace))
				return mEngFontMap.get(fontFace).bold;
		}
		return false;
	}
	private static boolean isFontItatic(String fontFace, boolean useChnFont) {
		if (useChnFont) {
			if (mChnFontMap.containsKey(fontFace))
				return mChnFontMap.get(fontFace).itatic;
		} else {
			if (mEngFontMap.containsKey(fontFace))
				return mEngFontMap.get(fontFace).itatic;
		}
		return false;
	}
	private float getCurrentCharWidthForChnftr() {
		try {
			BaseFont bf = BaseFont.createFont(
					getCurrentFontPath(false),
					getCurrentFontEncoding(false),
					BaseFont.NOT_EMBEDDED);
			return bf.getWidthPoint('A', mCurrentPresetFontSizes[mCurrentFontSizesIndex]) / DOCUMENT_DPI * mChnftrDpi;
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mChnftrDpi / mCpi;
	}
	private void printUnderLineList() {
		for (Map<Float, List<UnderLineInfo>> map : mUnderLineInfoMap.values()) {
			for (List<UnderLineInfo> list : map.values()) {
				float textAscDes = 0f;
				if (useAscender) {
					for (UnderLineInfo uli : list)
						textAscDes = Math.max(uli.textAscDes, textAscDes);
				} else {
					for (UnderLineInfo uli : list)
						textAscDes = Math.max(uli.textHeight, textAscDes);
					textAscDes += 0.75f;
				}
				for (UnderLineInfo uli : list) {
					mCurrentTemplates[mCurrentTemplateOffset + 1].saveState();
					mCurrentTemplates[mCurrentTemplateOffset + 1].setLineWidth(0.2f);
					mCurrentTemplates[mCurrentTemplateOffset + 1].moveTo(uli.startX, uli.Y - textAscDes);
					mCurrentTemplates[mCurrentTemplateOffset + 1].lineTo(uli.endX, uli.Y - textAscDes);
					mCurrentTemplates[mCurrentTemplateOffset + 1].setColorStroke(uli.color);
					mCurrentTemplates[mCurrentTemplateOffset + 1].stroke();
					mCurrentTemplates[mCurrentTemplateOffset + 1].restoreState();
				}
			}
		}
		mUnderLineInfoMap.clear();
	}
	public TemplateFileReader loadTemplateFile(String path) {
		try {
			return loadTemplateStream(new FileInputStream(path));
		} catch (FileNotFoundException e) {
			System.out.println("loadTemplateFile fail " + e.toString());
		}
		return null;
	}
	public TemplateFileReader loadTemplateFile(String path, int startPage, int endPage) {
		try {
			return loadTemplateStream(new FileInputStream(path), startPage, endPage);
		} catch (FileNotFoundException e) {
			System.out.println("loadTemplateFile fail " + e.toString());
		}
		return null;
	}
	public TemplateFileReader loadTemplateStream(InputStream inStream) {
		try {
			TemplateFileReader reader = new TemplateFileReader(inStream);
			mTemplateFileReaderList.add(reader);
			return reader;
		} catch (IOException e) {
			System.out.println("loadTemplateFile fail " + e.toString());
		}
		return null;
	}
	public TemplateFileReader loadTemplateStream(InputStream inStream, int startPage, int endPage) {
		try {
			TemplateFileReader reader = new TemplateFileReader(inStream, startPage, endPage);
			mTemplateFileReaderList.add(reader);
			return reader;
		} catch (IOException e) {
			System.out.println("loadTemplateFile fail " + e.toString());
		}
		return null;
	}
	public TemplateFileReader loadTemplateStreamToMap(String name, InputStream inStream, int startPage, int endPage) {
		try {
			TemplateFileReader reader = new TemplateFileReader(inStream, startPage, endPage);
			mTemplateFileReaderMap.put(name, reader);
			return reader;
		} catch (IOException e) {
			System.out.println("loadTemplateFileToMap fail " + e.toString());
		}
		return null;
	}
	public TemplateFileReader loadTemplateStreamToMap(String name, InputStream inStream) {
		try {
			TemplateFileReader reader = new TemplateFileReader(inStream);
			mTemplateFileReaderMap.put(name, reader);
			return reader;
		} catch (IOException e) {
			System.out.println("loadTemplateFileToMap fail " + e.toString());
		}
		return null;
	}
	public TemplateFileReader loadTemplateStreamToMap(String name, TemplateFileReader reader) {
		mTemplateFileReaderMap.put(name, reader);
		return reader;
	}
	public void loadTemplateToMap(String name, PdfTemplate template) {
		mTemplateMap.put(name, template);
	}
	public boolean containsTemplate(String name) {
		return mTemplateFileReaderMap.containsKey(name) || mTemplateMap.containsKey(name);
	}
	public int getTemplateTotalPage(String name) {
		TemplateFileReader reader = mTemplateFileReaderMap.get(name);
		if (reader != null)
			return reader.totalPage();
		if (mTemplateMap.containsKey(name))
			return 1;
		return 0;
	}
	public void setTemplatePageChnftrText(String text) {
		mTemplateFileReaderList.setChnftrText(text);
	}
	private static interface SplitChnEngTextListener {
		void appendText(String text, boolean useChnFont);
	}
	public static void splitChnEngText(String text, final SplitChnEngTextListener listener) {
		String src = text;
		final Pattern PATTERN_ENG = Pattern.compile("^([\\x00-\\xff]+)");
		final Pattern PATTERN_CHN = Pattern.compile("^([^\\x00-\\xff]+)");
		while (!src.isEmpty()) {
			Matcher matcher = PATTERN_CHN.matcher(src);
			String s;
			if (matcher.find()) {
				s = matcher.group(1);
				listener.appendText(s, true);
			}
			else {
				matcher = PATTERN_ENG.matcher(src);
				if (matcher.find())
					s = matcher.group(1);
				else
					s = src;
				listener.appendText(s, false);
			}
			src = src.substring(s.length());
		}
	}
	public static class TextSpliter implements SplitChnEngTextListener {
		private String engFontFace, chnFontFace;
		private float fontSize;
		private float width;
		private float dpi;
		private static class LineText {
			StringBuilder sb = new StringBuilder();
			float width, height, ascent;
		}
		private LinkedList<LineText> resultList = new LinkedList<LineText>();
		public TextSpliter(float dpi, String text, String engFontFace, String chnFontFace, float fontSize, int chnftrWidth) {
			this.engFontFace = engFontFace;
			this.chnFontFace = chnFontFace;
			this.fontSize = fontSize;
			this.dpi = dpi;
			width = getPrintPx(dpi, chnftrWidth);
			splitChnEngText(text != null ? text : "", this);
		}
		public TextSpliter(String text, String engFontFace, String chnFontFace, float fontSize, int chnftrWidth) {
			this(CHNFTR_DPI, text, engFontFace, chnFontFace, fontSize, chnftrWidth);
		}
		public List<String> getResultList() {
			List<String> sl = new ArrayList<String>();
			for (LineText lineText : resultList)
				sl.add(lineText.sb.toString());
			return sl;
		}
		public int getResultCount() {
			return resultList.size();
		}
		public float getWidthPoint() {
			float w = 0f;
			for (LineText lineText : resultList)
				w = Math.max(w, lineText.width);
			return w;
		}
		public int getWidth() {
			return getChnftrPx(dpi, getWidthPoint());
		}
		public float getHeightPoint(float lineHeight) {
			float h = 0f;
			for (int i = 0; i < resultList.size(); i++) {
				if (i < resultList.size() - 1)
					h += lineHeight;
				else
					h += resultList.get(i).height;
			}
			return h;
		}
		public float getAscentPoint(float lineHeight) {
			float h = 0f;
			for (int i = 0; i < resultList.size(); i++) {
				if (i < resultList.size() - 1)
					h += lineHeight;
				else
					h += resultList.get(i).ascent;
			}
			return h;
		}
		public int getHeight(int lineHeight) {
			return getChnftrPx(dpi, getHeightPoint(getPrintPx(dpi, lineHeight)));
		}
		public int getAscent(int lineHeight) {
			return getChnftrPx(dpi, getAscentPoint(getPrintPx(dpi, lineHeight)));
		}
		public String toString() {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < resultList.size(); i++) {
				if (i > 0)
					sb.append("\n");
				sb.append(resultList.get(i).sb);
			}
			return sb.toString();
		}
		public void appendText(String text, boolean useChnFont) {
			if (!useChnFont) {
				String[] ss0 = text.split("\\r\\n|\\n|\\r", -1);
				for (int i = 0; i < ss0.length; i++) {
					if (i > 0)
						resultList.add(new LineText());
					String[] ss1 = ss0[i].split("\\s", -1);
					for (int j = 0; j < ss1.length; j++) {
						if (j > 0)
							appendText1(" ", false);
						appendText1(ss1[j], false);
					}
				}
			} else
				appendText1(text, true);
		}
		private void appendText1(String text, boolean useChnFont) {
			try {
				BaseFont bf = BaseFont.createFont(
						getFontPath(useChnFont ? chnFontFace : engFontFace, useChnFont),
						getFontEncoding(useChnFont ? chnFontFace : engFontFace, useChnFont),
						BaseFont.NOT_EMBEDDED);
				if (useChnFont && chnFontFace.equals("schinese"))
					text = ChineseConvert.convertAuto2Gnew(text);
				LineText lineText;
				if (resultList.isEmpty()) {
					lineText = new LineText();
					resultList.add(lineText);
				} else
					lineText = resultList.getLast();
				if (!useChnFont) {
					float w = bf.getWidthPoint(text, fontSize);
					if (w > width - lineText.width) {
						if (lineText.width == 0) {
							int startIdx = 0, endIdx = 0;
							for (char c : text.toCharArray()) {
								endIdx++;
								float w1 = bf.getWidthPoint(c, fontSize);
								if (w1 > width - lineText.width && lineText.width > 0) {
									String s = text.substring(startIdx, endIdx);
									float asc = bf.getAscentPoint(s, fontSize);
									lineText.height = Math.max(lineText.height, asc - bf.getDescentPoint(s, fontSize));
									lineText.ascent = Math.max(lineText.ascent, asc);
									lineText = new LineText();
									resultList.add(lineText);
									startIdx = endIdx;
								}
								lineText.sb.append(c);
								lineText.width += w1;
							}
							if (startIdx < text.length()) {
								String s = text.substring(startIdx);
								float asc = bf.getAscentPoint(s, fontSize);
								lineText.height = Math.max(lineText.height, asc - bf.getDescentPoint(s, fontSize));
								lineText.ascent = Math.max(lineText.ascent, asc);
							}
						} else {
							lineText = new LineText();
							resultList.add(lineText);
							appendText1(text, false);
						}
					} else {
						lineText.sb.append(text);
						lineText.width += w;
						float asc = bf.getAscentPoint(text, fontSize);
						lineText.height = Math.max(lineText.height, asc - bf.getDescentPoint(text, fontSize));
						lineText.ascent = Math.max(lineText.ascent, asc);
					}
				} else {
					int startIdx = 0, endIdx = 0;
					for (char c : text.toCharArray()) {
						endIdx++;
						float w1 = bf.getWidthPoint(c, fontSize);
						if (w1 > width - lineText.width && lineText.width > 0) {
							String s = text.substring(startIdx, endIdx);
							float asc = bf.getAscentPoint(s, fontSize);
							lineText.height = Math.max(lineText.height, asc - bf.getDescentPoint(s, fontSize));
							lineText.ascent = Math.max(lineText.ascent, asc);
							lineText = new LineText();
							resultList.add(lineText);
						}
						lineText.sb.append(c);
						lineText.width += w1;
					}
					if (startIdx < text.length()) {
						String s = text.substring(startIdx);
						float asc = bf.getAscentPoint(s, fontSize);
						lineText.height = Math.max(lineText.height, asc - bf.getDescentPoint(s, fontSize));
						lineText.ascent = Math.max(lineText.ascent, asc);
					}
				}
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
		}
	}
	public static class TemplateFileReader extends PdfReader {
		private int startPage, endPage;
		private int currentPage = -1;
		private boolean isValid = true;
		private String chnftrText = "";
		private InputStream handleInputStream;
		public TemplateFileReader(InputStream inStream) throws IOException {
			super(inStream);
			handleInputStream = inStream;
			startPage = 0;
			endPage = this.getNumberOfPages() - 1;
		}
		public TemplateFileReader(InputStream inStream, int p_startPage, int p_endPage) throws IOException {
			this(inStream);
			if (!(p_startPage >= startPage && p_endPage <= endPage && p_startPage <= p_endPage))
				isValid = false;
			else
				currentPage = startPage - 1;
		}
		public void setPageChnftrText(String text) {
			chnftrText = text;
		}
		public String getChnftrText() {
			return chnftrText;
		}
		public boolean isValid() {
			return isValid && currentPage <= endPage;
		}
		public int currentPage() {
			return currentPage;
		}
		public boolean next() {
			currentPage++;
			return isValid();
		}
		public void reset() {
			currentPage = startPage - 1;
		}
		public int totalPage() {
			return isValid ? endPage - startPage + 1 : 0;
		}
		@Override
		public void close() {
			super.close();
			try {
				handleInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private static class TemplateFileReaderList extends ArrayList<TemplateFileReader> {
		private int currentReader = -1;
		private String chnftrText = "";
		@Override
		public void clear() {
			for (TemplateFileReader reader : this) {
				reader.close();
			}
			currentReader = -1;
			super.clear();
		}
		public TemplateFileReader next() {
			if (currentReader < 0)
				currentReader = 0;
			if (currentReader < size()) {
				TemplateFileReader reader = get(currentReader);
				if (reader.next())
					return reader;
				else {
					currentReader++;
					return next();
				}
			} else
				return null;
		}
		public void reset() {
			for (TemplateFileReader reader : this) {
				reader.reset();
			}
			currentReader = -1;
		}
		public void setChnftrText(String text) {
			chnftrText = text;
		}
		public String getChnftrText() {
			return chnftrText;
		}
		public int totalPage() {
			int tot = 0;
			for (TemplateFileReader reader : this)
				tot += reader.totalPage();
			return tot;
		}
	}
	private static class TemplateFileReaderMap extends HashMap<String, TemplateFileReader> {
		@Override
		public void clear() {
			for (TemplateFileReader reader : values())
				reader.close();
			super.clear();
		}
	}

	public static List<String> splitText(float dpi, String text, String engFontFace, String chnFontFace, float fontSize, int chnftrWidth) {
		return new TextSpliter(dpi, text, engFontFace, chnFontFace, fontSize, chnftrWidth).getResultList();
	}
	public static String wrapText(float dpi, String text, String engFontFace, String chnFontFace, float fontSize, int chnftrWidth) {
		return new TextSpliter(dpi, text, engFontFace, chnFontFace, fontSize, chnftrWidth).toString();
	}
	public static TextSpliter getTextSpliter(float dpi, String text, String engFontFace, String chnFontFace, float fontSize, int chnftrWidth) {
		return new TextSpliter(dpi, text, engFontFace, chnFontFace, fontSize, chnftrWidth);
	}

	public static List<String> splitText(String text, String engFontFace, String chnFontFace, float fontSize, int chnftrWidth) {
		return splitText(CHNFTR_DPI, text, engFontFace, chnFontFace, fontSize, chnftrWidth);
	}
	public static String wrapText(String text, String engFontFace, String chnFontFace, float fontSize, int chnftrWidth) {
		return wrapText(CHNFTR_DPI, text, engFontFace, chnFontFace, fontSize, chnftrWidth);
	}
	public static TextSpliter getTextSpliter(String text, String engFontFace, String chnFontFace, float fontSize, int chnftrWidth) {
		return getTextSpliter(CHNFTR_DPI, text, engFontFace, chnFontFace, fontSize, chnftrWidth);
	}
	
	/***
	 * obtain exif orientation
	 * @param p_bytes
	 * @return
	 */
	public static float getExifOrientation(byte[] p_bytes){
		try{
			FileType fileType = FileTypeDetector.detectFileType(new BufferedInputStream(new ByteArrayInputStream(p_bytes)));
			if (fileType == null){
				UniLog.logm(null, "file Type is null");
				return 0;
			}
			if (fileType != FileType.Jpeg){
				UniLog.logm(null, "ignore non jpeg file");
				return 0;
			}
			UniLog.logm(null, "type:%s mimeType:%s", fileType.getName(), fileType.getMimeType());
			
			float angle = 0;
			Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(p_bytes));
			ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
			int orientation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);

			switch (orientation)  //it do not handle flip image
			{
			case 1:
			case 2:
				angle = 0; 
				break;
			case 3:
			case 4:
				angle = 180; 
				break;
			case 5:
			case 6:
				angle = 90;
				break;
			case 7:
			case 8:
				angle = 270;
				break;
			}
			return(angle);
		}
		catch(Exception ex){
			UniLog.logm(null, "cannot extra exif");
			return(0);
		}
	}
	public List<ImportedTemplatePageInfo> getPageImportedTemplatePageList() {
		return mPageImportedTemplatePageList;
	}
	public List<MarkObjectInfo> getPageMarkObjectList() {
		return mPageMarkObjectList;
	}
	public int getCurrentPage() {
		return mCurrentPage;
	}
	private void rotatePdfImportedPage(PdfReader reader, PdfImportedPage page, AffineTransform trans) {
		//int angle = page.getRotation();
		int angle = reader.getPageRotation(page.getPageNumber());
		if (angle == 0)
			return;
		float sw = page.getWidth(), dw = sw;
		float sh = page.getHeight(), dh = sh;
		switch (Math.abs(angle)) {
		case 90:
		case 270:
			dw = sh;
			dh = sw;
			break;
		}
		UniLog.log("rotatePdfImportedPage angle:" + angle + ",sw:" + sw + ",sh:" + sh + ",dw:" + dw + ",dh:" + dh);
		trans.translate((dw - sw) / 2, (dh - sh) / 2);
		trans.rotate(Math.toRadians(angle), sw / 2, sh / 2);
	}
	public static double[] rotateTemplate(PdfTemplate template, AffineTransform trans, int angle) {
		if (trans == null)
			trans = new AffineTransform();
		if (angle != 0 && angle % 90 == 0) {
			float sw = template.getWidth(), dw = sw;
			float sh = template.getHeight(), dh = sh;
			switch (Math.abs(angle)) {
			case 90:
			case 270:
				dw = sh;
				dh = sw;
				break;
			}
			trans.translate((dw - sw) / 2, (dh - sh) / 2);
			trans.rotate(Math.toRadians(angle), sw / 2, sh / 2);
		}
		double[] m = new double[6];
		trans.getMatrix(m);
		return m;
	}

	public static int getContrastRgbColor(int color) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;

		// Calculate the perceptive luminance (aka luma) - human eye favors green color... 
		double luma = ((0.299 * r) + (0.587 * g) + (0.114 * b)) / 255;

		// Return black for bright colors, white for dark colors
		return luma > 0.5 ? 0 : 0xFFFFFF;
	} 
	public static BaseColor getContrastRgbColor(BaseColor color) {
		int r = getContrastRgbColor(color.getRGB());
		return new BaseColor(r);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("begin print");
		//String s = wrapText("PRIMARY LONGMAN ELECT LISTENING 2B TE - POD", "helv_br", "chinese", 17, 475);
		//System.out.println(s);
		/*try {
			ChnftrParser parser = new ChnftrParser("C:\\Users\\Administrator\\Desktop\\pwxf.txt", "-x2 -p0 -f11 -c0 -l1");
			parser.print("C:\\Users\\Administrator\\Desktop\\output.pdf");
		} catch (FileNotFoundException ex) {
			System.out.println("ChnftrParser error " + ex.toString());
		} catch (IOException ex) {
			System.out.println("ChnftrParser error " + ex.toString());
		} catch (DocumentException ex) {
			System.out.println("ChnftrParser error " + ex.toString());
		} catch (Exception ex) {
			System.out.println("ChnftrParser error " + ex.toString());
		}*/
		/*try {
			ChnftrParser parser = new ChnftrParser((InputStream)null, "-x2 -p0 -f11 -c0 -l1");
			parser.setTemplatePageChnftrText("600X20Y12T<chinese>10T<helv_nr>${pagenum} of ${pagecount}");

			parser.loadTemplateFile("C:\\Users\\Administrator\\Downloads\\TestEmbed.pdf")
				.setPageChnftrText("100X100Y12T<chinese>12T<helv_br>testhaha");

			parser.loadTemplateFile("C:\\Users\\Administrator\\Downloads\\WF81800303.pdf");

			parser.loadTemplateFile("C:\\Users\\Administrator\\Downloads\\afs_do_info.pdf")
				.setPageChnftrText("500X200Y12T<chinese>12T<helv_br>testabc");

			parser.print("C:\\Users\\Administrator\\Desktop\\output.pdf");
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		/*try {
			PdfReader reader = new PdfReader("c:\\tmp\\hq.pdf");
			//PdfStamper stamp = new PdfStamper(reader, new FileOutputStream("c:\\tmp\\hqq.pdf"));
			UniLog.log("reader pagecount:" + reader.getNumberOfPages());
			UniLog.log("reader " + reader.getPageSize(1));
			UniLog.log("reader " + reader.getPageSize(2));
			Rectangle rect = new Rectangle(PageSize.B3);
			Document doc = new Document(rect);
			PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream("c:\\tmp\\hqq.pdf"));  
			doc.open();  
			doc.add(new Paragraph("Hello World"));
			doc.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		try {
			//byte[] data = createBarcode("QRCODE", "6901234567892", 100, 100, null, 0, 0);
			//byte[] data = createBarcode("BARCODE", "6901234567892", 0, 0, BaseFont.TIMES_ROMAN, 12f, 12f);
			//byte[] data = createBarcode("BARCODE", "6901234567892", 300, 100, BaseFont.TIMES_ROMAN, 16f, 12f);
			//byte[] data = createBarcode("BARCODE", "6901234567892", 300, 100, null, 0, 0);
			byte[] data = createBarcode("EAN", "6901234567892", 0, 0, BaseFont.TIMES_ROMAN, 12f, 12f);
			//byte[] data = createBarcode("EAN", "6901234567892", 300, 100, BaseFont.TIMES_ROMAN, 16f, 18f);
			//byte[] data = createBarcode("EAN", "6901234567892", 300, 100, null, 0, 0);
			String s = Base64Util.convertToImgString(data, "png");
			System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("end print");
	}
	public void setChnftrGetImageInterface(ChnftrGetImageInterface p_chnftrGetImageInterface){
		chnftrGetImageInterface = p_chnftrGetImageInterface;
	}
	public void setPrintCallbackInterface(PrintCallbackInterface p_printCallbackInterface){
		printCallback = p_printCallbackInterface;
	}
	public void setUseGetImageInterfaceByDefault(boolean p_sw) {
		useGetImageInterfaceByDefault = p_sw;
	}
	public PdfTemplate createTemplate(float widthPx, float heightPx) {
		return PdfTemplate.createTemplate(mWriter, widthPx, heightPx);
	}
	static public int getPaperTypeIndex(String p_type) {
		if(p_type != null && p_type.equals("USER1")) {
			return(101);
		}
		for(int i=0;i<paperType.length;i++) {
			if(paperType[i].equals(p_type)) return(i);
		}
		return(-1);
	}
	/***
	 * 
	 * @param p_baseline: If p_fontface is not null, the text distance under the bars
	 * @return
	 * @throws Exception
	 */
	public static byte[] createBarcode(String p_type, String p_content, int p_width, int p_height, String p_fontface, float p_fontSize, float p_baseline, int p_imagetype, Color p_foreground, Color p_background) throws Exception {
		if (p_type.equals("QRCODE")) {
			BarcodeQRCode barcode = new BarcodeQRCode(p_content, p_width, p_height, null);
			return barcode.createBarcode(p_imagetype, p_foreground, p_background);
		}
		else if (p_type.equals("BARCODE")) {
			Barcode128 barcode = new Barcode128();
			barcode.setCode(p_content);
			barcode.setSize(p_fontSize);
			barcode.setBaseline(p_baseline);
			return barcode.createBarcode(p_imagetype, p_foreground, p_background, p_fontface, p_width, p_height);
		} else if (p_type.equals("EAN")) {
			BarcodeEAN barcode = new BarcodeEAN();
			barcode.setCode(p_content);
			barcode.setSize(p_fontSize);
			barcode.setBaseline(p_baseline);
			return barcode.createBarcode(p_imagetype, p_foreground, p_background, p_fontface, p_width, p_height);
		}
		return null;
	}
	public static byte[] createBarcode(String p_type, String p_content, int p_width, int p_height, String p_fontface, float p_fontSize, float p_baseline) throws Exception {
		return createBarcode(p_type, p_content, p_width, p_height, p_fontface, p_fontSize, p_baseline, BufferedImage.TYPE_BYTE_BINARY, Color.BLACK, Color.WHITE);
	}
}
