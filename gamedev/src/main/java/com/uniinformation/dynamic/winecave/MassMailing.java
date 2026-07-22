package com.uniinformation.dynamic.winecave;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;
import org.jsoup.internal.StringUtil;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
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

public class MassMailing extends BiActionHandler {
	int sendEmailRg = 0;
	/*
	String plainTextPath="c:/tmp/email_plaintext.txt";
	String htmlPath="c:/tmp/email_html.txt";
	String attachmentPath="c:/tmp/email_attachment.txt";
	*/
	String plainText = null;
	String htmlText = null;
	ArrayList<EmailAttachment> attachment = null;
	HashMap<String,String> sendEmail;
	public MassMailing() {
		super(null);
	}
	public MassMailing(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}
	
	ReturnMsg sendOneEmail(String p_emailAddr,SessionHelper p_sh) {
		return(ZkUtil.sendEmail(
					Pair.of("storage@winecavehk.com", (String) null),
					new VectorUtil()
						.addElement(
								Pair.of(p_emailAddr, (String) null)
								)
						.toVector(),
					null, 
					null,
					"Important Updates: Enhanced Platform and New Rates/ 重要通知：平台升級及費用調整與[NOTICE REF20260101]",
					htmlText,
					plainText, 
					attachment, 
					p_sh
				));
	}
	
	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		sendEmailRg = 0;
		sendEmail = new HashMap<String,String>();
		plainText = null;
		htmlText = null;
		attachment = null;
		String fpath;
		byte ba[];
		fpath = Erpv4Config.getString(p_result.getSessionHelper(), "SDEMHTML");
		if(!StringUtils.isBlank(fpath)) {
			ba = FileUtil.getBytesFromFile(new File(fpath));
			htmlText = new String(ba,StandardCharsets.UTF_8);
		}
		fpath = Erpv4Config.getString(p_result.getSessionHelper(), "SDEMTEXT");
		if(!StringUtils.isBlank(fpath)) {
			ba = FileUtil.getBytesFromFile(new File(fpath));
			plainText = new String(ba,StandardCharsets.UTF_8);
		}
		fpath = Erpv4Config.getString(p_result.getSessionHelper(), "SDEMATTACHMENT1");
		if(!StringUtils.isBlank(fpath)) {
			if(attachment == null) attachment = new ArrayList<EmailAttachment>();
			EmailAttachment att1 = new EmailAttachment();
			att1.setPath(fpath);
			String ss = Erpv4Config.getString(p_result.getSessionHelper(), "SDEMATTACHNAME1");
			att1.setName(StringUtils.isBlank(ss) ? "attachment1" :ss);
			att1.setDisposition(EmailAttachment.ATTACHMENT);
			attachment.add(att1);
		}
		fpath = Erpv4Config.getString(p_result.getSessionHelper(), "SDEMATTACHMENT2");
		if(!StringUtils.isBlank(fpath)) {
			if(attachment == null) attachment = new ArrayList<EmailAttachment>();
			EmailAttachment att1 = new EmailAttachment();
			att1.setPath(fpath);
			String ss = Erpv4Config.getString(p_result.getSessionHelper(), "SDEMATTACHNAME2");
			att1.setName(StringUtils.isBlank(ss) ? "attachment2" :ss);
			att1.setDisposition(EmailAttachment.ATTACHMENT);
			attachment.add(att1);
		}
		return (ReturnMsg.defaultOk);
	}
	
	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		String emailAddr = p_result.getCellString("vd_primaryEmail");
		if(!StringUtils.isBlank(emailAddr)) {
			sendEmail.put(p_result.getCellString("vd_customerCode"), emailAddr);
		}
		return (ReturnMsg.defaultOk);
	}
	@Override
	public ReturnMsg afterAction(BiResult p_br) {
		if(!sendEmail.isEmpty()) {
			Messagebox.show("Confirm SendEmail ?", "Message", Messagebox.YES|Messagebox.NO, Messagebox.EXCLAMATION,
				new EventListener() {
				   public void onEvent(Event evt) throws Exception {
				    	if (((Integer)evt.getData()) == Messagebox.YES){
				    		TableRec tr;
					    	SelectUtil su;
					    	su = p_br.getSelectUtil();
					    	int numSent=0;
					    	int numSkip=0;
					    	int numFail=0;
				    		for(String key : sendEmail.keySet()) {
				    			String sendStatus = null;
				    			String emailAddr = sendEmail.get(key);
				    			tr = su.getQueryResult("select * from sendemail where sdem_mrg = ? and sdem_key = ? and sdem_email = ?",
				    				new Wherecl()
				    					.appendArgument(sendEmailRg)
				    					.appendArgument(key)
				    					.appendArgument(emailAddr)
				    				);
				    			if(tr.getRecordCount() == 0) {
				    				su.executeUpdate("insert into sendemail (sdem_mrg,sdem_key,sdem_email,sdem_status) values( ? , ? , ? , ?)", 
				    				new Wherecl()
				    					.appendArgument(sendEmailRg)
				    					.appendArgument(key)
				    					.appendArgument(emailAddr)
				    					.appendArgument("")
				    				);
				    			} else {
				    				tr.setRecPointer(0);
				    				sendStatus = tr.getFieldString("sdem_status");
				    			}
				    			if(!"Sent".equals(sendStatus)) {
				    				ReturnMsg rtn = sendOneEmail(emailAddr,p_br.getSessionHelper());
				    				if(rtn != null && rtn.getStatus()) {
				    					int ld = DateUtil.dateToUnixtime(DateUtil.now());
				    					su.executeUpdate("update sendemail set sdem_status = 'Sent' , sdem_senttime = ?  where sdem_mrg = ? and sdem_key = ? and sdem_email = ?",
				    					new Wherecl()
				    						.appendArgument(ld)
				    						.appendArgument(sendEmailRg)
				    						.appendArgument(key)
				    						.appendArgument(emailAddr)
				    					);
				    					numSent++;
				    				} else {
				    					numFail++;
				    				}
				    			} else {
				    				numSkip++;
				    			}
				    		}
				    		Messagebox.show("Massmail Completed : " + numSent + " Sent " + numSkip + " Skipped " + numFail + " Failed");
				    	} else{
				    		return;
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
