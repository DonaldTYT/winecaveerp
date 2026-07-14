package com.uniinformation.jxapp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Button;
import org.zkoss.zul.Fileupload;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class CompInfo extends JxZkBiBase {

	/*
	public void afterBind() {
		super.afterBind();
		new JxFieldAction("btUploadLogo") {
			@Override
			public void actionPerformed(JxField jxfield) {
				Fileupload.get(new EventListener <UploadEvent>(){
					public void onEvent(UploadEvent event) {
						    org.zkoss.util.media.Media media = event.getMedia();
						    if(media != null) {
						    	try  {
						    		InputStream is = media.getStreamData();
						    		String key = "LOGO_IMAGE_"+Erpv4Config.getDefaultCoCode(getSessionHelper());
						    		FilingUtil.storeFile(getSessionHelper().getAgent(), null, key, key, key, is);
						    		is.close();
						    		Executions.getCurrent().sendRedirect(null);
//						    		ZkUtil.js("zkbiBc.send({action:'reloadCurrent'},false);"); //trigger non-active browser window auto login
						    	} catch (Exception ex) {
						    		UniLog.log(ex);
						    	}
						    }
					}
				});
			}
		};
	}*/

	@Override
	public void bindCellCollection(BiResult p_br, int mode) {
		super.bindCellCollection(p_br, mode);
		Selectors.find(curComp, "[id^=btUpload]").stream().filter(bt -> bt.hasAttribute("keyprefix")).map(bt -> (Button)bt).forEach(bt -> {
			String keyprefix = (String)bt.getAttribute("keyprefix");
			String imageId = (String)bt.getAttribute("imageid");
			ZkUtil.setEventListener(bt, Events.ON_CLICK, ev -> {
				UniLog.log1("id:%s, keyprefix:%s", bt.getId(), keyprefix);
				Fileupload.get(new HashMap<>(), null, bt.getLabel(), 1, -1, true, event -> {
				    org.zkoss.util.media.Media media = event.getMedia();
					try (InputStream is = media.getStreamData()) {
			    		String key = keyprefix + Erpv4Config.getDefaultCoCode(getSessionHelper());
			    		FilingUtil.storeFile(getSessionHelper().getAgent(), null, key, key, key, is);
			    		Executions.getCurrent().sendRedirect(null);
			    	} catch (Exception ex) {
			    		UniLog.log(ex);
			    	}
				});
			});
			if (StringUtils.isNotBlank(imageId)) {
				String url = getImageUrl(sessionHelper, keyprefix);
				UniLog.log1("url:%s", url);
				if (StringUtils.isNotBlank(url))
					jxAdd(imageId).setText(url);
			}
		});
	}

	public static String getImageUrl(SessionHelper sessionHelper, String keyprefix) {
		try {
			String agent = sessionHelper.getAgent();
			String cocode = Erpv4Config.getDefaultCoCode(sessionHelper);
			keyprefix = keyprefix.toLowerCase();
			FilingUtilObject fobj = FilingUtil.getFile(agent, null, keyprefix + cocode, null);
			if (fobj != null) {
				File f = new File(sessionHelper.getWebContentRealPath("", true) + "images/logo/"+keyprefix+agent+"_"+cocode+"_"+fobj.cts.toString().replace(" ", "_").replace(":", "_")+".png");
				if (!f.exists()) {
					try (FileOutputStream os = new FileOutputStream(f)) {
						FilingUtil.getFile(agent, null, keyprefix + cocode, os);
					}
				}
				return "images/logo/"+keyprefix+agent+"_"+cocode+"_"+fobj.cts.toString().replace(" ", "_").replace(":", "_")+".png";
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		return null;
	}		
}
