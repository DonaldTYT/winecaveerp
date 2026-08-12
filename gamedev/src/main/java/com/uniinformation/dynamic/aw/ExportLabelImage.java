package com.uniinformation.dynamic.aw;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.zkoss.zul.Filedownload;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.itext.text.pdf.BarcodeQRCode;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public class ExportLabelImage extends BiActionHandler implements JxActionListener {
	
	byte[] jpgData;
	public ExportLabelImage() {
		super(null);
	}
	public ExportLabelImage(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}
	public static byte[] createQrLabelJpeg(String qrValue, String textComment) throws Exception {
	    /*
	     * 40mm x 30mm at 203 dpi:
	     * 1 inch = 25.4mm
	     * 40mm = 1.5748 inch -> about 320 px
	     * 30mm = 1.1811 inch -> about 240 px
	     */
	    final int dpi = 203;
	    final int labelWidthPx = (int) Math.round(40.0 / 25.4 * dpi);   // about 320
	    final int labelHeightPx = (int) Math.round(30.0 / 25.4 * dpi);  // about 240

	    BufferedImage label = new BufferedImage(
	            labelWidthPx,
	            labelHeightPx,
	            BufferedImage.TYPE_INT_RGB
	    );

	    Graphics2D g = label.createGraphics();

	    try {
	        // White background
	        g.setColor(Color.WHITE);
	        g.fillRect(0, 0, labelWidthPx, labelHeightPx);

	        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

	        /*
	         * QR code area.
	         * Similar to the ZD411 ZPL layout:
	         * QR at upper-middle, text at bottom.
	         *
	         * For long URL QR code, 150px-155px is a practical size on 320x240 label.
	         */
	        int qrSize = 155;
	        int qrX = (labelWidthPx - qrSize) / 2;
	        int qrY = 10;

	        BufferedImage qrImage = createQrBufferedImage(qrValue, qrSize, qrSize);
	        g.drawImage(qrImage, qrX, qrY, null);

	        /*
	         * Bottom comment:
	         * max 2 rows, auto-wrap, centered.
	         */
	        int textX = 5;
	        int textY = 180;
	        int textWidth = labelWidthPx - 10;
	        int maxLines = 2;

	        Font commentFont = new Font("SansSerif", Font.PLAIN, 16);
	        g.setFont(commentFont);
	        g.setColor(Color.BLACK);

	        drawCenteredWrappedText(g, textComment, textX, textY, textWidth, maxLines);
	    } finally {
	        g.dispose();
	    }

	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    ImageIO.write(label, "jpg", baos);
	    return baos.toByteArray();
	}
	
	private static BufferedImage createQrBufferedImage(String qrValue, int width, int height) throws Exception {
	    if (qrValue == null) {
	        qrValue = "";
	    }

	    /*
	     * BarcodeQRCode constructor usually:
	     * BarcodeQRCode(String content, int width, int height, Map hints)
	     *
	     * width/height can be 1,1 because we scale it ourselves afterwards.
	     */
	    BarcodeQRCode qrCode = new BarcodeQRCode(qrValue, 1, 1, null);

	    java.awt.Image awtQrImage = qrCode.createAwtImage(Color.BLACK, Color.WHITE);

	    BufferedImage rawQr = new BufferedImage(
	            awtQrImage.getWidth(null),
	            awtQrImage.getHeight(null),
	            BufferedImage.TYPE_INT_RGB
	    );

	    Graphics2D g = rawQr.createGraphics();
	    try {
	        g.setColor(Color.WHITE);
	        g.fillRect(0, 0, rawQr.getWidth(), rawQr.getHeight());
	        g.drawImage(awtQrImage, 0, 0, null);
	    } finally {
	        g.dispose();
	    }

	    /*
	     * Scale QR with NEAREST_NEIGHBOR.
	     * Very important: do not use smooth scaling for QR code.
	     */
	    BufferedImage scaledQr = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g2 = scaledQr.createGraphics();
	    try {
	        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
	        g2.setColor(Color.WHITE);
	        g2.fillRect(0, 0, width, height);
	        g2.drawImage(rawQr, 0, 0, width, height, null);
	    } finally {
	        g2.dispose();
	    }

	    return scaledQr;
	}
	
	private static void drawCenteredWrappedText(
	        Graphics2D g,
	        String text,
	        int x,
	        int y,
	        int width,
	        int maxLines
	) {
	    if (text == null) {
	        text = "";
	    }

	    FontMetrics fm = g.getFontMetrics();
	    int lineHeight = fm.getHeight();

	    java.util.List<String> lines = wrapTextByWidth(text, fm, width, maxLines);

	    for (int i = 0; i < lines.size(); i++) {
	        String line = lines.get(i);

	        int lineWidth = fm.stringWidth(line);
	        int drawX = x + (width - lineWidth) / 2;
	        int drawY = y + fm.getAscent() + i * lineHeight;

	        g.drawString(line, drawX, drawY);
	    }
	}
	private static java.util.List<String> wrapTextByWidth(
	        String text,
	        FontMetrics fm,
	        int maxWidth,
	        int maxLines
	) {
	    java.util.List<String> result = new java.util.ArrayList<String>();

	    if (text == null || text.length() == 0) {
	        result.add("");
	        return result;
	    }

	    String[] words = text.trim().split("\\s+");
	    StringBuilder currentLine = new StringBuilder();

	    for (int i = 0; i < words.length; i++) {
	        String word = words[i];

	        String testLine;
	        if (currentLine.length() == 0) {
	            testLine = word;
	        } else {
	            testLine = currentLine.toString() + " " + word;
	        }

	        if (fm.stringWidth(testLine) <= maxWidth) {
	            currentLine.setLength(0);
	            currentLine.append(testLine);
	        } else {
	            if (currentLine.length() > 0) {
	                result.add(currentLine.toString());
	                currentLine.setLength(0);
	            }

	            if (result.size() >= maxLines) {
	                break;
	            }

	            /*
	             * If one word itself is too long, split it by character.
	             * Useful for values like Q25070917,Q25070918.
	             */
	            if (fm.stringWidth(word) > maxWidth) {
	                java.util.List<String> split = splitLongWord(word, fm, maxWidth);

	                for (String part : split) {
	                    if (result.size() >= maxLines) {
	                        break;
	                    }
	                    result.add(part);
	                }
	            } else {
	                currentLine.append(word);
	            }
	        }

	        if (result.size() >= maxLines) {
	            break;
	        }
	    }

	    if (currentLine.length() > 0 && result.size() < maxLines) {
	        result.add(currentLine.toString());
	    }

	    /*
	     * If still too many lines, cut to maxLines.
	     */
	    while (result.size() > maxLines) {
	        result.remove(result.size() - 1);
	    }

	    return result;
	}
	
	private static java.util.List<String> splitLongWord(String word, FontMetrics fm, int maxWidth) {
	    java.util.List<String> result = new java.util.ArrayList<String>();

	    StringBuilder line = new StringBuilder();

	    for (int i = 0; i < word.length(); i++) {
	        char c = word.charAt(i);
	        String test = line.toString() + c;

	        if (fm.stringWidth(test) <= maxWidth) {
	            line.append(c);
	        } else {
	            if (line.length() > 0) {
	                result.add(line.toString());
	                line.setLength(0);
	            }
	            line.append(c);
	        }
	    }

	    if (line.length() > 0) {
	        result.add(line.toString());
	    }

	    return result;
	}

	@Override
	public void actionPerformed(JxField field) {
		JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
		BiResult br = jxf.getBr();
		try {
			jpgData = createQrLabelJpeg(
				String.format("https://www.erpv4.com/bctag?agent=aw&dnno=%s", br.getCellString("stm_ref1")),
				br.getCellString("stm_ref1")+" "+ br.getCellString("stm_allorders")
				);
			if(jpgData != null) {
				Filedownload.save(jpgData, "image/jpeg", br.getCellString("stm_ref1")+"_"+ br.getCellString("stm_allorders")+".jpg");
			}
		} catch (Exception ex) {
			jxf.messageBox(ex.toString());
		}
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result, int cnt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ReturnMsg afterAction(BiResult p_result) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean isVisible(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(false);
//		return(p_br.getSessionHelper().hasAccessRight("#cfmwo"));
		return(true);
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		if(p_isBatch) {
			return(false);
		} else {
			if(StringUtils.isBlank(p_br.getCellString("stm_ref1"))) {
				return(true);
			}
			if(p_br.inBeginWork()) return(true);
			/*
			String qs = p_br.getCellString("inv_quostatus");
			if(qs.equals("Confirmed") || qs.equals("Void")) return(true);
			*/
			return(false);
		}
	}
}
