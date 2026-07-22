package com.uniinformation.dynamic.winecave;

import java.security.DigestInputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Fileupload;

import com.kyoko.common.ReturnMsg;
import com.kyoko.crypto.SHA256withRSA;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class BatchUploadPhoto  extends BiActionHandler{

	static final String filingTable = "medialib";
	public BatchUploadPhoto(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result, int cnt) {
		// TODO Auto-generated method stub
		try {
			
//			Fileupload.get(
//				    -1,                        // max number of files (or -1 for "no limit")
//				    new EventListener<UploadEvent>() {
//				        @Override
//				        public void onEvent(UploadEvent event) throws Exception {
//				            Media[] medias = event.getMedias();   // 🔹 all selected files
//				            if (medias != null) {
//				                for (Media media : medias) {
//				                    // process each file
//				                    System.out.println("Uploaded: " + media.getName());
//				                    // your save logic here
//				                }
//				            }
//				        }
//				    }
//				)

		    Fileupload.get(new EventListener <UploadEvent>(){
	    		public void onEvent(UploadEvent event) throws Exception {
	        		UniLog.log("upload event catched");
//	        		SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper((HttpServletRequest) Executions.getCurrent().getNativeRequest() , (HttpServletResponse) Executions.getCurrent().getNativeResponse());
	                org.zkoss.util.media.Media media = event.getMedia();
	                if(media != null) {
	                	JSONObject jo = new JSONObject();
	                	jo.put("contentType", media.getContentType());
	                	jo.put("fileName", media.getName());
	                	jo.put("format",media.getFormat());
	                	
	                	DigestInputStream dis = SHA256withRSA.newDigestInputStream(media.getStreamData());
	                	FilingUtil.storeFile(p_result.getSessionHelper().getAgent(), filingTable, null, media.getName(), jo.toString(), dis);
//	                	String sha256B64 = SHA256withRSA.bytesToBase64(SHA256withRSA.sha256Hex(media.getStreamData()));
//	                	UniLog.log("Media Type = "+media.getContentType() + " sha256 " + sha256B64);
//	                	FilingUtil.storeFile(p_result.getSessionHelper().getAgent(), filingTable, p_key, media.getName(), jo.toString(), p_is)
	                	/*
	                	if(!media.getContentType().equals("application/pdf") )
	                	{
	                		messageBox("Only Pdf File Are Accepted");
	                		return;
	                	}
	                	*/
	                	try  {

	                	} catch (Exception ex) {
	                		UniLog.log(ex);
	                		//messageBox(ex.toString());
	                	}
	                	
	                }
	    		}
		    });

		} catch (Exception ex) {
				UniLog.log(ex);
		}	
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

}
