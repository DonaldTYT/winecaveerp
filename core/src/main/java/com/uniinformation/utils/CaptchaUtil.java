package com.uniinformation.utils;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.tuple.Pair;

public class CaptchaUtil {
    private static final String CAPTCHA_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
    private static final Random RANDOM = new Random();
    
    private static String generateText(int length) {
        StringBuilder sb = new StringBuilder(length);
        IntStream.range(0, length).forEach(i -> sb.append(CAPTCHA_CHARS.charAt(RANDOM.nextInt(CAPTCHA_CHARS.length()))));
        return sb.toString();
    }
    
    private static BufferedImage drawImage(String text, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        
        try {
        	// 开启抗锯齿
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            // 绘制灰色背景 (灰底色)
            g.setColor(new Color(240, 240, 240)); // 浅灰色背景
            g.fillRect(0, 0, width, height);
            
            // 绘制干扰线（浅灰色调）
            drawNoiseLines(g, width, height);
            
            // 绘制噪点
            drawNoisePixels(image, width, height);
            
            // 绘制验证码字符（深色字体）
            drawText(g, text, width, height);
            
            // 绘制浅灰色边框
            g.setColor(new Color(200, 200, 200));
            g.drawRect(0, 0, width - 1, height - 1);
            
        } finally {
            g.dispose();
        }
        
        return image;
    }
    
    private static void drawNoiseLines(Graphics2D g, int width, int height) {
        g.setColor(new Color(180, 180, 180, 100)); // 半透明灰色
        for (int i = 0; i < 3; i++) { // 减少干扰线数量以适应小图片
            int x1 = RANDOM.nextInt(width);
            int y1 = RANDOM.nextInt(height);
            int x2 = RANDOM.nextInt(width);
            int y2 = RANDOM.nextInt(height);
            g.drawLine(x1, y1, x2, y2);
        }
    }
    
    private static void drawNoisePixels(BufferedImage image, int width, int height) {
        for (int i = 0; i < 30; i++) { // 减少噪点数量
            int x = RANDOM.nextInt(width);
            int y = RANDOM.nextInt(height);
            int c = 160 + RANDOM.nextInt(60); // 160-220之间的灰色
            image.setRGB(x, y, new Color(c, c, c, 100).getRGB());
        }
    }
    
    private static void drawText(Graphics2D g, String text, int width, int height) {
        // 根据图片大小调整字体
        Font font = new Font("Arial", Font.BOLD, 16); // 减小字体大小
        g.setFont(font);
        
        char[] chars = text.toCharArray();
        int x = 5; // 起始位置
        
        // 计算每个字符的宽度，使字符均匀分布
        int charWidth = (width - 10) / chars.length;
        
        for (int i = 0; i < chars.length; i++) {
            // 随机上下偏移
            int y = 16 + RANDOM.nextInt(4) - 2;
            
            // 深灰色字体
            int grayValue = 60 + RANDOM.nextInt(60); // 60-120之间的深灰色
            g.setColor(new Color(grayValue, grayValue, grayValue));
            
            // 绘制字符
            g.drawString(String.valueOf(chars[i]), x + i * charWidth, y);
        }
    }

    private static String toBase64(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return Base64.encodeBytes(baos.toByteArray());
    }
    
    public static Pair<String, String> generateNumberImage(int textLength, int width, int height) throws IOException {
        String code = generateText(textLength);
        BufferedImage image = drawImage(code, width, height);
        return Pair.of(code, toBase64(image, "png"));
    }
}
