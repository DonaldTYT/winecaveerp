package com.uniinformation.itext.text.pdf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import com.lowagie.text.Element;
import com.lowagie.text.pdf.BaseFont;
import com.uniinformation.utils.UniLog;

public class Barcode128 extends com.lowagie.text.pdf.Barcode128 {
	public byte[] createBarcode(int imageType, Color foreground, Color background, String fontFace, int widthPx, int heightPx) throws Exception {
		BaseFont font = fontFace != null ? BaseFont.createFont(fontFace, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED) : null;
		if (widthPx > 0) {
        	int f = 0;
        	for (;;) {
        		setX(0.75f * ++f);
        		int w = pt2Px(getBarcodeSize().getWidth());
        		if (w > widthPx) {
        			if (f > 1)
        				setX(0.75f * --f);
        			break;
        		}
        		else if (w == widthPx)
        			break;
        	}
        	UniLog.log("f:" + f);
        }
		if (heightPx > 0)
			setBarHeight(px2Pt(heightPx) - (font != null ? Math.abs(baseline) : 0));

		String fullCode;
        if (codeType == CODE128_RAW) {
            int idx = code.indexOf('\uffff');
            if (idx < 0)
                fullCode = "";
            else
                fullCode = code.substring(idx + 1);
        }
        else if (codeType == CODE128_UCC)
            fullCode = getHumanReadableUCCEAN(code);
        else
            fullCode = removeFNC1(code);
        float fontX = 0;
        if (font != null) {
            fontX = font.getWidthPoint(fullCode = altText != null ? altText : fullCode, size);
        }
        String bCode;
        if (codeType == CODE128_RAW) {
            int idx = code.indexOf('\uffff');
            if (idx >= 0)
                bCode = code.substring(0, idx);
            else
                bCode = code;
        }
        else {
            bCode = getRawText(code, codeType == CODE128_UCC);
        }
        int len = bCode.length();
        float fullWidth = (len + 2) * 11 * x + 2 * x;
        float barStartX = 0;
        float textStartX = 0;
        switch (textAlignment) {
            case Element.ALIGN_LEFT:
                break;
            case Element.ALIGN_RIGHT:
                if (fontX > fullWidth)
                    barStartX = fontX - fullWidth;
                else
                    textStartX = fullWidth - fontX;
                break;
            default:
                if (fontX > fullWidth)
                    barStartX = (fontX - fullWidth) / 2;
                else
                    textStartX = (fullWidth - fontX) / 2;
                break;
        }
        float barStartY = 0;
        float textStartY = 0;
        if (font != null) {
            if (baseline <= 0) {
            	textStartY = font.getFontDescriptor(BaseFont.ASCENT, size) - font.getFontDescriptor(BaseFont.DESCENT, size);
            	barStartY = -baseline;
            }
            else
            	textStartY = barHeight + baseline;
        }
        byte bars[] = getBarsCode128Raw(bCode);
        boolean print = true;

        int imageWidth = pt2Px(getBarcodeSize().getWidth());
        if (widthPx > 0 && imageWidth < widthPx)
        	imageWidth = widthPx;
        int imageHeight = pt2Px(barHeight + (font != null ? Math.abs(baseline) : 0));
        BufferedImage bimage = new BufferedImage(imageWidth, imageHeight, imageType);
		Graphics2D bGr = bimage.createGraphics();
		bGr.setColor(background);
		bGr.fillRect(0, 0, imageWidth, imageHeight);
		bGr.setColor(foreground);
		bGr.setFont(new java.awt.Font(fontFace, java.awt.Font.PLAIN, (int)size));

		UniLog.log1("getBarcodeSize:%s, imageWidth:%d, imageHeight:%d, x:%f, size:%f, baseline:%f", getBarcodeSize(), imageWidth, imageHeight, x, size, baseline);
		
		for (int k = 0; k < bars.length; ++k) {
            float w = bars[k] * x;
            if (print)
                bGr.fillRect(pt2Px(barStartX), pt2Px(barStartY), pt2Px(w - inkSpreading), pt2Px(barHeight));
            print = !print;
            barStartX += w;
        }
		if (font != null)
            bGr.drawString(fullCode, pt2Px(textStartX), pt2Px(textStartY));
    	bGr.dispose();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bimage, "png", baos);
		baos.close();
		return baos.toByteArray();
	}
	
	private int pt2Px(float pt) {
		return Math.round(pt / 72f * 96f);
	}
	private float px2Pt(float px) {
		return Math.round(px / 96f * 72f);
	}
}
