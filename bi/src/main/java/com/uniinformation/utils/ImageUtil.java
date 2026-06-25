package com.uniinformation.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.Hashtable;

import javax.imageio.ImageIO;

public class ImageUtil {

    static public void resizeImageAsJPG(File p_inputFile, File p_outputFile, int pMaxWidth) throws IOException {
        BufferedImage src = ImageIO.read(p_inputFile);
        if (src == null) throw new IOException("Unsupported image: " + p_inputFile);
        int w = src.getWidth(), h = src.getHeight();
        double scale = (pMaxWidth > 0 && w > pMaxWidth) ? ((double) pMaxWidth / w) : 1.0;
        int nw = Math.max(1, (int) Math.round(w * scale));
        int nh = Math.max(1, (int) Math.round(h * scale));
        BufferedImage dst = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, nw, nh, null);
        g.dispose();
        ImageIO.write(dst, "jpg", p_outputFile);
    }

    static public void cropRelativeImageAsJPG(File p_inputFile, File p_outputFile,
                                             double p_xpc, double p_ypc, double p_widthpc, double p_heightpc) throws IOException {
        BufferedImage src = ImageIO.read(p_inputFile);
        if (src == null) throw new IOException("Unsupported image: " + p_inputFile);
        int w = src.getWidth(), h = src.getHeight();
        int x = clamp((int) Math.round(w * p_xpc), 0, w - 1);
        int y = clamp((int) Math.round(h * p_ypc), 0, h - 1);
        int cw = clamp((int) Math.round(w * p_widthpc), 1, w - x);
        int ch = clamp((int) Math.round(h * p_heightpc), 1, h - y);
        BufferedImage crop = src.getSubimage(x, y, cw, ch);
        BufferedImage rgb = new BufferedImage(cw, ch, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.drawImage(crop, 0, 0, null);
        g.dispose();
        ImageIO.write(rgb, "jpg", p_outputFile);
    }

    static public void rotateImageAsPNG(File p_inputFile, File p_outputFile, double p_rotate) throws IOException {
        BufferedImage src = ImageIO.read(p_inputFile);
        if (src == null) throw new IOException("Unsupported image: " + p_inputFile);
        int w = src.getWidth(), h = src.getHeight();
        BufferedImage dst = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        AffineTransform at = AffineTransform.getRotateInstance(p_rotate, w / 2.0, h / 2.0);
        g.drawImage(src, at, null);
        g.dispose();
        ImageIO.write(dst, "png", p_outputFile);
    }

    static public Hashtable getImageProperties(File p_inputFile) throws IOException {
        BufferedImage src = ImageIO.read(p_inputFile);
        if (src == null) throw new IOException("Unsupported image: " + p_inputFile);
        Hashtable h = new Hashtable();
        h.put("width", Integer.valueOf(src.getWidth()));
        h.put("height", Integer.valueOf(src.getHeight()));
        return h;
    }

    public static String getBase64ImageString(InputStream p_is, String p_mineType) {
        try {
            byte[] bytes = p_is.readAllBytes();
            return "data:" + p_mineType + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private static int clamp(int v, int min, int max) { return Math.max(min, Math.min(max, v)); }
}