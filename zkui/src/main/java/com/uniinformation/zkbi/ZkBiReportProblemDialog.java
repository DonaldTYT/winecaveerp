package com.uniinformation.zkbi;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeUtility;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;
import org.json.JSONObject;
import org.zkforge.ckez.CKeditor;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.AfterSizeEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Space;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.impl.MessageboxDlg;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.uniinformation.jx.zk.JxZkSystem;
import com.uniinformation.utils.Base64Util;
import com.kyoko.common.*;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZipUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class ZkBiReportProblemDialog {
	private SessionHelper sessionHelper;
	private Component parentComp;
	private Image imgScreenshot;
	private Div imgScreenshotContainer;
	private MessageboxDlg imageEditorDialog;

	private Date shotTime;
	private String currentUrl;
	private String desktopId;

	private Combobox cbType;
	private CKeditor ckDescription;
	
	private String screenshotUrl;

	public ZkBiReportProblemDialog(SessionHelper sessionHelper, Component parentComp, org.zkoss.json.JSONObject json) {
		this.sessionHelper = sessionHelper;
		this.parentComp = parentComp;
		if (json != null) {
			shotTime = new Date((Long)json.get("shotTime"));
			currentUrl = (String)json.get("currentUrl");
			desktopId = (String)json.get("desktopId");
		}
		shotTime = new Date(); //andrew220207 shotTime obtain from server side to avoid timezone problem
		show();
	}
	public void show() {
		final Vlayout vl = new Vlayout() {{
			setVflex("1");
			setHflex("1");
		}};
		cbType = new Combobox() {{
			appendItem("Bug Report");
			appendItem("Feature Suggestion");
			setReadonly(true);
			setSelectedIndex(0);
			setStyle("background:white;");
		}};
		if (shotTime != null) {
			imgScreenshot = new Image() {{
				setId("report_problem_screenshot_image");
				setTooltiptext("Click to show Image Editor");
				addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						showTuiImageEditor();
					}
				});
			}};
			vl.appendChild(imgScreenshotContainer = new Div() {{
				setStyle("overflow-y:auto");
				setHeight("300px");
				imgScreenshot.setStyle("max-width:100%");
				appendChild(imgScreenshot);
			}});
			vl.appendChild(new Hlayout() {{
				appendChild(new Label("*Click to edit screenshot") {{setStyle("font-size:8px !important; color:#0088b0;");}});
			}
			});
			vl.appendChild(new Space() {{setHeight("5px");}});
			vl.appendChild(new Hlayout() {{
				appendChild(new Label("Report Time: ") {{setWidth("100px");setStyle("display:inline-block");}});
				appendChild(new Textbox(DateUtil.dateToDateTimeStr(shotTime) ) {{ this.setReadonly(true); this.setStyle("background:white;");}});
			}});
			
		}
		vl.appendChild(new Space() {{setHeight("2px");}});
		vl.appendChild(new Hlayout() {{
			appendChild(new Label("Type: ") {{setWidth("100px");setStyle("display:inline-block");}});
			appendChild(cbType);
		}});
		vl.appendChild(new Space() {{setHeight("2px");}});

		ckDescription = new CKeditor() {{
			setId("report_problem_ckeditor");
			setCustomConfigurationsPath("/js/zkckeditor_config.js");
			setToolbar("Simple");
			final Map<String, Object> configMap = new HashMap<String, Object>();
			configMap.put("removePlugins", "elementspath");
			configMap.put("resize_enabled", false);
			setConfig(configMap);
			setVflex("1");
			setHflex("1");
		}};
		vl.appendChild(ckDescription);
		final Div div = new Div() {{
			appendChild(vl);
		}};
		final MessageboxDlg dlg = ZkUtil.buildSimpleMessageboxDlg("Report Problem", div, 
						new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL},
						new String[]{"Submit", "Close"}, 
				parentComp, new ZkBiEventListener<Messagebox.ClickEvent>(){
					@Override
					public void onZkBiEvent(ClickEvent event) throws Exception {
						UniLog.log1("event:%s", event);
						if (event.getButton() == null)
							return;
						switch (event.getButton()) {
						case OK: //submit
							if (StringUtils.isBlank(ckDescription.getValue())) {
								ZkUtil.showErrMsg(vl, "Please input description");
								event.stopPropagation();
								break;
							}
							//send image data to server
							ZkUtil.js("zAu.send(new zk.Event(zk.Widget.$('$report_problem_dialog'), 'onSubmit', {screenshotUrl: jq('$report_problem_screenshot_image').attr('src')}, {toServer:true}));", parentComp.getId());
							event.stopPropagation();
							break;
						default:
							detachImageEditorDialog();
							break;
						}
					}
				});
		dlg.setId("report_problem_dialog");
		//dlg.setWidth(sessionHelper.isMobile() ? "100%" : "90%");
		//dlg.setHeight(sessionHelper.isMobile() ? "100%" : "90%");
		dlg.setWidth("100%");
		dlg.setHeight("100%");
		dlg.doHighlighted();
		
		dlg.addEventListener("onSubmit", new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				org.zkoss.json.JSONObject json = (org.zkoss.json.JSONObject)event.getData();
				screenshotUrl = (String)json.get("screenshotUrl");
				if (sendEmail()) {
					detachImageEditorDialog();
					dlg.detach();
				}
			}
		});
		dlg.addEventListener(Events.ON_CLOSE, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				detachImageEditorDialog();
			}
		});
		dlg.addEventListener(Events.ON_AFTER_SIZE, new ZkBiEventListener<AfterSizeEvent>() {
			@Override
			public void onZkBiEvent(AfterSizeEvent event) throws Exception {
				if (imgScreenshotContainer != null)
					imgScreenshotContainer.setHeight(event.getHeight() / 3 + "px");
			}
		});

		//setup image src
		ZkUtil.js("setTimeout(function(){"
				+ "jq('$report_problem_ckeditor').addClass('zkbi-ckeditor');"
				+ "jq('$report_problem_screenshot_image').attr('src', jq('$%s').data('canvasDataUrl'))"
				+ "},150);", parentComp.getId());
	}
	
	private void detachImageEditorDialog() {
		if (imageEditorDialog != null) {
			imageEditorDialog.detach();
			imageEditorDialog = null;
		}
	}
	
	private boolean sendEmail() {
		ByteArrayInputStream bis1 = null;
		ByteArrayInputStream bis2 = null;
		FileOutputStream fos = null;
		File attFile = null;
		List<Object> list = new ArrayList<Object>();

		try {
			JSONObject json = FilingUtil.getJson(sessionHelper.getAgent(), null, 
					SessionHelper.SYSTEM_SMTP_FILING_STORE_KEY
					);
			if (json == null)
				throw new Exception("config json is null.");
			String reportProblemEmailTo = json.optString("reportProblemEmailTo");
			String reportProblemZipPassword = json.optString("reportProblemZipPassword");
			if (StringUtils.isBlank(reportProblemEmailTo))
				throw new Exception("Please setup Report problem email address.");
			//if (StringUtils.isBlank(reportProblemZipPassword))
			//	throw new Exception("Please setup Report problem zip password.");
			String errMsg = JxZkSystem.validateReportProblemZipPassword(reportProblemZipPassword);
			if (errMsg != null)
				throw new Exception(errMsg);

			//subject
			String subject = "Report Problem";
	
			//content
			String content = String.format("<html><body><p>Datetime: %s</p><p>Type: %s</p><p>Description:</p>%s</body></html>", 
					DateUtil.dateToDateTimeStr(DateUtil.now()), cbType.getValue(), ckDescription.getValue());
			UniLog.log1("content:%s", content);

			//attachment 
			//screenshot image
			if (StringUtils.isNotBlank(screenshotUrl) && screenshotUrl.indexOf("base64,") > 0) {
				String str = screenshotUrl.substring(screenshotUrl.indexOf("base64,") + 7);
				byte[] data = Base64Util.decodeAsBytes(str);
				if (data != null) {
					bis1 = new ByteArrayInputStream(data);
					list.add(bis1);
					list.add("screenshot.jpg");
				}
			}

			//info
			Map<String, String> map = new HashMap<String, String>();
			map.put("url", currentUrl);
			map.put("loginId", sessionHelper.getLoginId());
			map.put("desktopId", desktopId);
			map.put("viewId", sessionHelper.getURLParam("viewid"));
			map.put("reportTime", DateUtil.dateToDateTimeStr(shotTime));
			map.put("type", cbType.getValue());
			map.put("content", ckDescription.getValue());
			Gson gson = new GsonBuilder()
				.setPrettyPrinting()
				.disableHtmlEscaping()
				.create();
			String jsonStr = gson.toJson(map);
			bis2 = new ByteArrayInputStream(jsonStr.getBytes());
			list.add(bis2);
			list.add("info.json");

			int i = 0;
			do {
				attFile = new File(String.format("/tmp/ntemattach_%s_%s_%d", sessionHelper.getAgent(), sessionHelper.getLoginId(), System.currentTimeMillis() + i++));
			} while (attFile.exists());
			fos = new FileOutputStream(attFile);
			ZipUtil.createZip(reportProblemZipPassword, true,
					fos,
					list.toArray(new Object[0]));

			List<Pair<String, String>> emailList = new ArrayList<Pair<String, String>>();
			for (String s : StringUtils.split(reportProblemEmailTo, ",;")) {
				UniLog.log1("to email:%s", s);
				emailList.add(Pair.of(s, ""));
			}

			List<EmailAttachment> attList = new ArrayList<EmailAttachment>();
			EmailAttachment ea = new EmailAttachment();
			ea.setName(MimeUtility.encodeWord("screenshot.zip", "UTF-8", "B"));
			ea.setDisposition(EmailAttachment.ATTACHMENT);
			ea.setPath(attFile.getAbsolutePath());
			attList.add(ea);

			ReturnMsg rtnMsg = ZkUtil.sendEmail(null, emailList, null, null, subject, content, "", attList, sessionHelper);
			UniLog.log1("rtnMsg:" + rtnMsg);
			if (!rtnMsg.getStatus())
				throw rtnMsg.getEx();
			ZkUtil.msg("Email send successfully.");
			return true;
		}
		catch (Exception e) {
			ZkUtil.errMsg("Send email fail.\n" + e.getMessage());
			return false;
		}
		finally {
			try {
				if (bis1 != null)
					bis1.close();
				if (bis2 != null)
					bis2.close();
				if (fos != null)
					fos.close();
				if (attFile != null && attFile.exists())
					attFile.delete();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private void showTuiImageEditor() {
		if (imageEditorDialog != null) {
			imageEditorDialog.setVisible(true);
			ZkUtil.js("jq('$report_problem_screenshot_image').data('toggleOffAllButtonFunc')();");
			return;
		}
		final Div div1 = new Div();
		Div div = new Div() {{
			div1.setVflex("1");
			div1.setHflex("1");
			appendChild(div1);
		}};
		imageEditorDialog = ZkUtil.buildSimpleMessageboxDlg("Report Error", div, 
						new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL},
				parentComp, new ZkBiEventListener<Messagebox.ClickEvent>(){
					@Override
					public void onZkBiEvent(ClickEvent event) throws Exception {
						UniLog.log1("event:%s", event);
						if (event.getButton() == null)
							return;
						switch (event.getButton()) {
						case OK:
							ZkUtil.js("jq('$report_problem_screenshot_image').data('setTuiImageSrcFunc')();");
							imageEditorDialog.setVisible(false);
							event.stopPropagation();
							break;
						case CANCEL:
							//imageEditorDialog.setVisible(false);
							//event.stopPropagation();
							imageEditorDialog = null;
							break;
						}
					}
				});
		imageEditorDialog.setWidth("100%");
		imageEditorDialog.setHeight("100%");
		imageEditorDialog.doHighlighted();
		imageEditorDialog.addEventListener(Events.ON_CLOSE, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				//imageEditorDialog.setVisible(false);
				//event.stopPropagation();
				imageEditorDialog = null;
			}
		});
		ZkUtil.js("setTimeout(function(){"
				//+ "showTuiImageEditor('#%s', jq('$%s').data('canvasDataUrl'), '$report_problem_screenshot_image');"
				+ "showTuiImageEditor('#%s', jq('$report_problem_screenshot_image').attr('src'), '$report_problem_screenshot_image');"
				+ "},150);", div1.getUuid(), parentComp.getId());
	}
}
