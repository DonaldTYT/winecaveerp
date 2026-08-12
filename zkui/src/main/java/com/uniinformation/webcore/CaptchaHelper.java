package com.uniinformation.webcore;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.uniinformation.utils.UniLog;

/***
 *  generate captcha image
 *  e.g. 
 *  http://localhost:8080/ZKpages/captcha.jpg?v=abc 
 *  
 *  web.xml add
 * 	<servlet>
 *      <servlet-name>CaptchaHelper</servlet-name>
 *      <servlet-class>com.uniinformation.webcore.CaptchaHelper</servlet-class>
 * </servlet>
 * <servlet-mapping>
 *  	<servlet-name>CaptchaHelper</servlet-name>
 *  	<url-pattern>/captcha.jpg</url-pattern>
 * </servlet-mapping>
 *
 */

public class CaptchaHelper extends HttpServlet {
	public static int imageHeight = 40;
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }
	protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		int charCnt = 0;
		int imageWidth = 0;
	    String imageCode = "";
	    
	    Random randChars = new Random();
	    
	    /*
	    //for testing/demo purpose only, the code should be obtained from session
	    if (request.getParameter("v") != null && request.getParameter("v").trim().length() > 0){
	    	imageCode = request.getParameter("v").trim();
	    	UniLog.log1("warning: imageCode should be obtained from session. remoteip:%s", request.getRemoteAddr());
	    }
	    */
	    
	    HttpSession session = request.getSession(false);
	    if (session != null){
	    	if (session.getAttribute("CaptchaHelper.imageCode") != null && !session.getAttribute("CaptchaHelper.imageCode").toString().trim().equals("")){
	    		imageCode = session.getAttribute("CaptchaHelper.imageCode").toString().trim();
	    	}
	    }
	    
   		UniLog.log("imageCode="+imageCode);
	    
    	charCnt = imageCode.length();
    	if (charCnt <= 0){
    		UniLog.log("ignore empty code");
    		return;
    	}
    	
    	imageWidth = charCnt * 25;
	    String imageCodeDummy = (Long.toString(Math.abs(randChars.nextLong()), 36)).substring(0, charCnt);
	    BufferedImage biImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g2d = (Graphics2D) biImage.getGraphics();
	    g2d.setColor(Color.WHITE);
	    g2d.fillRect(0, 0, imageWidth, imageHeight);

	    //fill some background dot
	    for (int i = 0; i < 500; i++) {
	        g2d.setColor(new Color(randChars.nextInt(255), randChars.nextInt(255), randChars.nextInt(255)));
	        int offSet = getRandomInt(1,3);
	        int iX = (int) (Math.random() * imageWidth - offSet);
	        int iY = (int) (Math.random() * imageHeight - offSet);
	        g2d.drawOval(iX, iY, 1, 1);
	    }
	    
	    for (int i = 0; i < charCnt; i++) {
	    	setRandomFont(g2d);
	        g2d.setColor(new Color(getRandomInt(50,200), getRandomInt(50,200), getRandomInt(50,200)));
            g2d.drawString(imageCode.substring(i, i + 1), 25 * i + getRandomInt(0,5), getRandomInt(20,35));
            
            //draw some dummy char
			g2d.setFont(new Font("Arial", Font.PLAIN, getRandomInt(3,7)));
            g2d.drawString("/", 25 * i + getRandomInt(0,5), getRandomInt(15,imageHeight));
			g2d.setFont(new Font("Arial", Font.PLAIN, getRandomInt(3,7)));
            g2d.drawString("\\", 25 * i + getRandomInt(0,5), getRandomInt(15,imageHeight));
			g2d.setFont(new Font("Arial", Font.PLAIN, getRandomInt(3,7)));
            g2d.drawString("!", 25 * i + getRandomInt(0,5), getRandomInt(15,imageHeight));
			g2d.setFont(new Font("Arial", Font.PLAIN, getRandomInt(3,7)));
            g2d.drawString(imageCodeDummy.substring(i,i+1), 25 * i + getRandomInt(0,10), getRandomInt(15,imageHeight));
			g2d.setFont(new Font("Arial", Font.PLAIN, getRandomInt(3,7)));
            g2d.drawString(imageCodeDummy.substring(i,i+1), 25 * i + getRandomInt(0,10), getRandomInt(15,imageHeight));
	    }


	    response.setHeader("Cache-Control", "private, no-store, no-cache, must-revalidate");
	    response.setHeader("Pragma", "no-cache");
	    response.setContentType("image/jpg");
	    OutputStream osImage = response.getOutputStream();
	    ImageIO.write(biImage, "jpeg", osImage);
	    osImage.close();
	    g2d.dispose();
	}
	private void setRandomFont(Graphics2D g2d){
		int randFontId = getRandomInt(1,3);
		int ranFontSize = getRandomInt(18,30);
		int ranFontStyle = 0;
		if (getRandomInt(0,1) == 1)
			ranFontStyle = ranFontStyle | Font.BOLD;
		if (getRandomInt(0,1) == 1)
			ranFontStyle = ranFontStyle | Font.ITALIC;
		switch (randFontId){
			case 1: g2d.setFont(new Font("Arial", ranFontStyle, ranFontSize));
			break;
			case 2: g2d.setFont(new Font("Verdanna", ranFontStyle, ranFontSize));
			break;
			case 3: g2d.setFont(new Font("Times New Roman", ranFontStyle, ranFontSize));
			break;
		}
	}
	private int getRandomInt(int min, int max){
	    return((new Random()).nextInt(max - min + 1) + min);
	}


}
