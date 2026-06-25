package com.uniinformation.utils;

import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Html;

import com.google.gson.Gson;

public class PhotoSwipeUtil {
	/***
	 * utility function for display a photos using photoswipe and swiper js
	 * @param p_comp - container for display photo block
	 * @param p_photoMap - a list of photo, should include thumbnail/M size/L size image 
	 */
	public static void loadPhoto(Component p_comp, List<Map<String, String>> p_photoList){
		for (Component c : p_comp.getChildren())
			p_comp.removeChild(c);
		StringBuilder sb = new StringBuilder();
		//TODO: inject thumbnail div (using swiper js)
		sb.append("<div id=\"swiper001\" class=\"swiper-container\">");
		sb.append("  <ul id=\"gallery001\" class=\"swiper-wrapper my-gallery\" itemscope itemtype=\"http://schema.org/ImageGallery\">");
		sb.append("  </ul>");
		sb.append("  <div class=\"swiper-pagination\"></div>");
		sb.append("  <div title=\"Prev\" class=\"swiper-button-prev\"></div>");
		sb.append("  <div title=\"Next\" class=\"swiper-button-next\"></div>");
		sb.append("</div>");
		
		//inject default photoswipe div to dom
		sb.append("		<div id=\"pswp001\" class=\"pswp\" tabindex=\"-1\" role=\"dialog\" aria-hidden=\"true\">");
		sb.append("		    <div class=\"pswp__bg\"></div>");
		sb.append("		    <div class=\"pswp__scroll-wrap\">");
		sb.append("		        <div class=\"pswp__container\">");
		sb.append("		            <div class=\"pswp__item\"></div>");
		sb.append("		            <div class=\"pswp__item\"></div>");
		sb.append("		            <div class=\"pswp__item\"></div>");
		sb.append("		        </div>");
		sb.append("		        <div class=\"pswp__ui pswp__ui--hidden\">");
		sb.append("		            <div class=\"pswp__top-bar\">");
		sb.append("		                <div class=\"pswp__counter\"></div>");
		sb.append("		                <button class=\"pswp__button pswp__button--close\" title=\"Close (Esc)\"></button>");
		sb.append("		                <button class=\"pswp__button pswp__button--share\" title=\"Share\"></button>");
		sb.append("		                <button class=\"pswp__button pswp__button--fs\" title=\"Toggle fullscreen\"></button>");
		sb.append("		                <button class=\"pswp__button pswp__button--zoom\" title=\"Zoom in/out\"></button>");
		sb.append("		                <div class=\"pswp__preloader\">");
		sb.append("		                    <div class=\"pswp__preloader__icn\">");
		sb.append("		                      <div class=\"pswp__preloader__cut\">");
		sb.append("		                        <div class=\"pswp__preloader__donut\"></div>");
		sb.append("		                      </div>");
		sb.append("		                    </div>");
		sb.append("		                </div>");
		sb.append("		            </div>");
		sb.append("		            <div class=\"pswp__share-modal pswp__share-modal--hidden pswp__single-tap\">");
		sb.append("		                <div class=\"pswp__share-tooltip\"></div> ");
		sb.append("		            </div>");
		sb.append("		            <button class=\"pswp__button pswp__button--arrow--left\" title=\"Previous (arrow left)\"></button>");
		sb.append("		            <button class=\"pswp__button pswp__button--arrow--right\" title=\"Next (arrow right)\"></button>");
		sb.append("		            <div class=\"pswp__caption\">");
		sb.append("		                <div class=\"pswp__caption__center\"></div>");
		sb.append("		            </div>");
		sb.append("		        </div>");
		sb.append("		    </div>");
		sb.append("		</div>");		
		Html html = new Html();
		html.setContent(sb.toString());
		p_comp.appendChild(html);
		
		//TODO pass photo url to function
		Gson gson = new Gson();
		String json = gson.toJson(p_photoList);
		UniLog.log1(json);
		Clients.evalJavaScript(String.format("showPhotoSwipe('#swiper001', '#gallery001', '#pswp001', %s)", json));  //test photoswipe
	}
}
