package com.uniinformation.jxapp.edu;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkbi.edu.ZkBiComposerPaymentReminder;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.HtmlEmail;
import org.json.JSONException;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Div;
import org.zkoss.zul.Html;
import org.zkoss.zul.Tabbox;

public class PaymentReminderHistory extends JxZkBiBase {
	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private String contentTemplate;

	@Override
	public void afterBind() {
		UniLog.log1("afterBind called");
		super.afterBind();
	}
	
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		UniLog.log1("bindCellCollection called");
		super.bindCellCollection(p_br, mode);

		contentTemplate = ZkBiComposerPaymentReminder.loadEmailHtmlContentTemplate(sessionHelper);
		if (contentTemplate == null) {
			ZkUtil.errMsg("Email content template not exist");
			return;
		}

		JxField f = jxAdd("html_email_content");
		final Html htmlEmail = (Html) f.getNativeObject();

		f = jxAdd("detail_tabbox");
		final Tabbox tabbox = (Tabbox) f.getNativeObject();

		htmlEmail.addEventListener(Events.ON_AFTER_SIZE, new ZkBiEventListener<AfterSizeEvent>() {
			@Override
			public void onZkBiEvent(AfterSizeEvent event) throws Exception {
				UniLog.log1("event:%s, height:%d", event, event.getHeight());
				Integer oldHtmlHeight = (Integer) tabbox.getAttribute("htmlHeight");
				int newHtmlHeight = event.getHeight();
				if (oldHtmlHeight == null || oldHtmlHeight != newHtmlHeight) {
					tabbox.setHeight((newHtmlHeight + 100 + 62) + "px"); //100: html div padding
					tabbox.setAttribute("htmlHeight", newHtmlHeight);
				}
			}
		});

		jxAdd("btResend").addActionListener(new JxActionListener() {
			@Override
			public void actionPerformed(JxField field) {
				final List<Pair<String, String>> emailList = ZkBiComposerPaymentReminder.getStudentEmailAddressList(getBr().getCellString("esprh_toemail"));
				int emailCount = emailList.size();
				if (emailCount == 0) {
					ZkUtil.showErrMsg("No email address found");
					return;
				}
				ZkBiMsgbox.show(ZkBiMsgbox.Type.question, String.format("Resend %d payment reminder emails?", emailCount), new String[] {"Ok", "Cancel"}, new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
						if (btn.getName().equals("Ok"))
							sendEmail(emailList);
					}
				});
			}
		});

		jxAdd("btPrint").addActionListener(new JxActionListener() {
			@Override
			public void actionPerformed(JxField field) {
				UniLog.log1("btPrint action:%s", field);
				try {
					ZkUtil.print(htmlEmail);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		htmlEmail.setContent(buildEmailHtmlContent(contentTemplate));
	}
	
	private String buildEmailHtmlContent(String template) {
		return ZkBiComposerPaymentReminder.buildEmailHtmlContent(sessionHelper, template, "'images/logo/edu_logo.jpg'", "'images/logo/edu_logo1.jpg'", "'images/logo/edu_companychop.png'",
							"'images/logo/edu_logo.jpg'", "'images/whatsapp-icon-100.png'", "'images/facebook-icon-100.png'", "'images/instagram-icon-100.png'",
							getBr().getCellString("esprh_invoiceno"), 
							getBr().getCellDate("esprh_remtime"), 
							getBr().getCellString("eaav0_tokenccy"), 
							getBr().getCellString("eaav0_name"), 
							getBr().getCellString("eaav0_code"), 
							getBr().getCellString("estt_name"), 
							getBr().getCellString("eaav0_sessionday"), 
							getBr().getCellDate("eaav0_sessiontime"), 
							getBr().getCellInt("eaav0_sessionlen"), 
							getBr().getCellInt("esprh_sdrg"),
							getBr().getCellString("essd_name"), 
							getBr().getCellString("essd_sdno"), 
							getBr().getCellInt("esprh_sremcnt"), 
							getBr().getCellDate("esprh_startdate"),
							getBr().getCellDate("esprh_finsdate"),
							getBr().getCellString("essd_contactby"),
							getBr().getCellString("essd_firstname"),
							getBr().getCellInt("eaav0_numsession"),
							getBr().getCellDouble("eaav0_coursefee"),
							getBr().getCellDouble("esprh_adjust"),
							getBr().getCellString("esprh_attendhis"));
	}
	
	private void sendEmail(List<Pair<String, String>> emailList) {
		int okCount = 0;
		int failCount = 0;
		
		File logoFile = new File(Sessions.getCurrent().getWebApp().getRealPath("images/logo/edu_logo.jpg"));
		File logo1File = new File(Sessions.getCurrent().getWebApp().getRealPath("images/logo/edu_logo1.jpg"));
		File logo2File = new File(Sessions.getCurrent().getWebApp().getRealPath("images/logo/edu_companychop.png"));
		File linkLogoFile = logoFile;
		File linkLogo1File = new File(Sessions.getCurrent().getWebApp().getRealPath("images/whatsapp-icon-100.png"));
		File linkLogo2File = new File(Sessions.getCurrent().getWebApp().getRealPath("images/facebook-icon-100.png"));
		File linkLogo3File = new File(Sessions.getCurrent().getWebApp().getRealPath("images/instagram-icon-100.png"));

		//subject
		//String subject = String.format("Little Scholars Education Centre Payment Reminder (%s)", getBr().getCellString("eaav0_name"));
		String subject = String.format("Little Scholars Education Centre Invoice [%s] [%s]", getBr().getCellString("eaav0_name"), sdf.format(getBr().getCellDate("esprh_remtime")));

		//attachment
		List<EmailAttachment> attList = new ArrayList<EmailAttachment>();
		try {
			HtmlEmail hemailObj = new HtmlEmail();
		
			//content
			String htmlMsg = ZkBiComposerPaymentReminder.buildEmailHtmlContent(sessionHelper, contentTemplate, "cid:" + hemailObj.embed(logoFile), "cid:" + hemailObj.embed(logo1File), "cid:" + hemailObj.embed(logo2File),
							"cid:" + hemailObj.embed(linkLogoFile), "cid:" + hemailObj.embed(linkLogo1File), "cid:" + hemailObj.embed(linkLogo2File), "cid:" + hemailObj.embed(linkLogo3File),
							getBr().getCellString("esprh_invoiceno"), 
							getBr().getCellDate("esprh_remtime"), 
							getBr().getCellString("eaav0_tokenccy"), 
							getBr().getCellString("eaav0_name"), 
							getBr().getCellString("eaav0_code"), 
							getBr().getCellString("estt_name"), 
							getBr().getCellString("eaav0_sessionday"), 
							getBr().getCellDate("eaav0_sessiontime"), 
							getBr().getCellInt("eaav0_sessionlen"), 
							getBr().getCellInt("esprh_sdrg"),
							getBr().getCellString("essd_name"), 
							getBr().getCellString("essd_sdno"), 
							getBr().getCellInt("esprh_sremcnt"), 
							getBr().getCellDate("esprh_startdate"),
							getBr().getCellDate("esprh_finsdate"),
							getBr().getCellString("essd_contactby"),
							getBr().getCellString("essd_firstname"),
							getBr().getCellInt("eaav0_numsession"),
							getBr().getCellDouble("eaav0_coursefee"),
							getBr().getCellDouble("esprh_adjust"),
							getBr().getCellString("esprh_attendhis"));
			UniLog.log1("htmlMsg:%s", htmlMsg);
		
			ReturnMsg rtnMsg = ZkUtil.sendEmail(hemailObj, null, emailList, null, null, subject, htmlMsg, "", attList, sessionHelper);
			UniLog.log1("rtnMsg:" + rtnMsg);
			if (rtnMsg.getStatus()) {
				Map<String, Object> rMap = (Map<String, Object>) rtnMsg.getData();
				okCount = (Integer)rMap.get("okCnt");
				failCount = (Integer)rMap.get("failCnt");
			}
			else
				failCount = emailList.size();
		}
		catch (Exception e) {
			e.printStackTrace();
			failCount = emailList.size();
		}
		if (failCount > 0)
			ZkUtil.msg(String.format("%d emails send fail.", failCount));
		else
			ZkUtil.msg("Email send successfully.");
	}
}
