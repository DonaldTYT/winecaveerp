package com.uniinformation.jxapp;

import java.io.InputStream;
import java.util.HashSet;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Fileupload;

import com.uniinformation.bicore.BiTable;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;

public class CompInfo extends JxZkBiBase {
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
	}

}
