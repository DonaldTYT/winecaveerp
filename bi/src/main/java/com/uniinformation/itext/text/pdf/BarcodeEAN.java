package com.uniinformation.itext.text.pdf;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import javax.imageio.ImageIO;
import com.lowagie.text.pdf.BaseFont;
import com.uniinformation.utils.UniLog;

public class BarcodeEAN extends com.lowagie.text.pdf.BarcodeEAN {
	/** The bar positions that are guard bars.*/    
	private static final int GUARD_EMPTY[] = {};
    /** The bar positions that are guard bars.*/    
	private static final int GUARD_UPCA[] = {0, 2, 4, 6, 28, 30, 52, 54, 56, 58};
    /** The bar positions that are guard bars.*/    
	private static final int GUARD_EAN13[] = {0, 2, 28, 30, 56, 58};
    /** The bar positions that are guard bars.*/    
	private static final int GUARD_EAN8[] = {0, 2, 20, 22, 40, 42};
    /** The bar positions that are guard bars.*/    
	private static final int GUARD_UPCE[] = {0, 2, 28, 30, 32};
    /** The x coordinates to place the text.*/
	private static final float TEXTPOS_EAN13[] = {6.5f, 13.5f, 20.5f, 27.5f, 34.5f, 41.5f, 53.5f, 60.5f, 67.5f, 74.5f, 81.5f, 88.5f};
    /** The x coordinates to place the text.*/
	private static final float TEXTPOS_EAN8[] = {6.5f, 13.5f, 20.5f, 27.5f, 39.5f, 46.5f, 53.5f, 60.5f};
	
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

        float barStartX = 0;
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
        switch (codeType) {
            case EAN13:
            case UPCA:
            case UPCE:
                if (font != null)
                    barStartX += font.getWidthPoint(code.charAt(0), size);
                break;
        }
        byte bars[] = null;
        int guard[] = GUARD_EMPTY;
        switch (codeType) {
            case EAN13:
                bars = getBarsEAN13(code);
                guard = GUARD_EAN13;
                break;
            case EAN8:
                bars = getBarsEAN8(code);
                guard = GUARD_EAN8;
                break;
            case UPCA:
                bars = getBarsEAN13("0" + code);
                guard = GUARD_UPCA;
                break;
            case UPCE:
                bars = getBarsUPCE(code);
                guard = GUARD_UPCE;
                break;
            case SUPP2:
                bars = getBarsSupplemental2(code);
                break;
            case SUPP5:
                bars = getBarsSupplemental5(code);
                break;
        }
        float keepBarX = barStartX;
        boolean print = true;
        float gd = 0;
        if (font != null && baseline > 0 && guardBars) {
            gd = baseline / 2;
            textStartY -= gd + font.getFontDescriptor(BaseFont.DESCENT, size);
        }

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
            if (print) {
                if (Arrays.binarySearch(guard, k) >= 0)
                    bGr.fillRect(pt2Px(barStartX), pt2Px(barStartY), pt2Px(w - inkSpreading), pt2Px(barHeight + gd));
                else
                    bGr.fillRect(pt2Px(barStartX), pt2Px(barStartY), pt2Px(w - inkSpreading), pt2Px(barHeight));
            }
            print = !print;
            barStartX += w;
        }
        if (font != null) {
        	switch (codeType) {
        		case EAN13:
                    bGr.drawString(code.substring(0, 1), 0, pt2Px(textStartY));
        			for (int k = 1; k < 13; ++k) {
                        String c = code.substring(k, k + 1);
                        float len = font.getWidthPoint(c, size);
                        float pX = keepBarX + TEXTPOS_EAN13[k - 1] * x - len / 2;
                        bGr.drawString(c, pt2Px(pX), pt2Px(textStartY));
                    }
        			break;
        		case EAN8:
        			for (int k = 0; k < 8; ++k) {
                        String c = code.substring(k, k + 1);
                        float len = font.getWidthPoint(c, size);
                        float pX = TEXTPOS_EAN8[k] * x - len / 2;
                        bGr.drawString(c, pt2Px(pX), pt2Px(textStartY));
                    }
        			break;
        		case UPCA:
                    bGr.drawString(code.substring(0, 1), 0, pt2Px(textStartY));
                    for (int k = 1; k < 11; ++k) {
                        String c = code.substring(k, k + 1);
                        float len = font.getWidthPoint(c, size);
                        float pX = keepBarX + TEXTPOS_EAN13[k] * x - len / 2;
                        bGr.drawString(c, pt2Px(pX), pt2Px(textStartY));
                    }
                    bGr.drawString(code.substring(11, 12), pt2Px(keepBarX + x * (11 + 12 * 7)), pt2Px(textStartY));
        			break;
        		case UPCE:
                    bGr.drawString(code.substring(0, 1), 0, pt2Px(textStartY));
                    for (int k = 1; k < 7; ++k) {
                        String c = code.substring(k, k + 1);
                        float len = font.getWidthPoint(c, size);
                        float pX = keepBarX + TEXTPOS_EAN13[k - 1] * x - len / 2;
                        bGr.drawString(c, pt2Px(pX), pt2Px(textStartY));
                    }
                    bGr.drawString(code.substring(7, 8), pt2Px(keepBarX + x * (9 + 6 * 7)), pt2Px(textStartY));
        			break;
        		case SUPP2:
                case SUPP5:
                	for (int k = 0; k < code.length(); ++k) {
                        String c = code.substring(k, k + 1);
                        float len = font.getWidthPoint(c, size);
                        float pX = (7.5f + (9 * k)) * x - len / 2;
                        bGr.drawString(c, pt2Px(pX), pt2Px(textStartY));
                    }
                	break;
        	}
        }
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