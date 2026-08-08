package com.uniinformation.dynamic.winecave;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.internal.StringUtil;
import org.zkoss.util.media.AMedia;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.wc.BiResultStockListPush;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.prtdoc.PrtdocInterface;
import com.uniinformation.utils.FileUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public class GoogleMerchantCenterProduct extends BiActionHandler {
	HashSet<String> productHash ;
	JSONObject jo;
	JSONArray ja;
	/*
	int sendEmailRg = 0;
	String plainTextPath="c:/tmp/email_plaintext.txt";
	String htmlPath="c:/tmp/email_html.txt";
	String attachmentPath="c:/tmp/email_attachment.txt";
	*/
	/*
	String plainText = null;
	String htmlText = null;
	ArrayList<EmailAttachment> attachment = null;
	HashMap<String,String> sendEmail;
	*/
	public GoogleMerchantCenterProduct() {
		super(null);
	}
	public GoogleMerchantCenterProduct(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		try {
			productHash = new HashSet<String>();
			jo = new JSONObject();
			ja = new JSONArray();
			return(BiResultStockListPush.doBeforeAction(jo,ja,productHash,p_result,cnt));
		} catch(Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
	}
	
	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		try {
			return(BiResultStockListPush.doProcessAction(ja,productHash,p_result,p_recIdx));
		} catch(Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
	}
	@Override
	public ReturnMsg afterAction(BiResult p_br) {
		if(jo != null) {
			Messagebox.show("Confirm Export Xml ?", "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
				new EventListener() {
				   public void onEvent(Event evt) throws Exception {
				    	if (((Integer)evt.getData()) == Messagebox.YES){
				    	    AMedia media = new AMedia(
				    	            "thisdownload.xml",
				    	            "xml",
				    	            "application/xml",
				    	            BiResultStockListPush.convert(jo).getBytes(StandardCharsets.UTF_8)
				    	        );

				    	    Filedownload.save(media);
				    	    Messagebox.show("File Exported");
				   	    }
				   }
				}
			);
		}
		return (ReturnMsg.defaultOk);
	}
	
	@Override
	public boolean isVisible(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(false);
		return(p_br.getSessionHelper().hasAccessRight("#massmail"));
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		return(false);
	}
}
