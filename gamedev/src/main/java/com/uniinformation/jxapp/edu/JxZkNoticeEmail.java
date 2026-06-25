package com.uniinformation.jxapp.edu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.mail.internet.MimeUtility;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;
import org.json.JSONObject;
import org.zkforge.ckez.CKeditor;
import org.zkoss.zhtml.I;
import org.zkoss.zhtml.Messagebox;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Window;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jx.zk.JxZkSystem;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;

public class JxZkNoticeEmail extends ZkComposerBase {
	private static final long maxAttachmentKB = 10240;
	private static final String PICK_ALL_STUDENT_LABEL = "All Student";
	private static boolean fAllowAllStudent = false; //disable it temporary

	@Wire
	Window winMain;
	
	@Wire
	Checkbox cbEmailTo, cbEmailCc, cbEmailBcc;

	@Wire
	Textbox tbEmailFrom;

	@Wire
	Textbox tbEmailTo;
	@Wire
	Listbox s2EmailTo;
	@Wire
	Div divEmailTo, divEmailTo1;

	@Wire
	Textbox tbEmailCc;
	@Wire
	Listbox s2EmailCc;
	@Wire
	Div divEmailCc, divEmailCc1;

	@Wire
	Textbox tbEmailBcc;
	@Wire
	Listbox s2EmailBcc;
	@Wire
	Div divEmailBcc, divEmailBcc1;

	@Wire
	Checkbox cbOnlyUseFirstAddr;

	@Wire
	Textbox tbEmailSubject;

	@Wire
	CKeditor ckEmailContent;
	
	@Wire
	Vbox vbAttachment;
	
	@Wire
	Button btEmail;
	@Wire
	Label lbAddLoginLink;
	@Wire
	Button btAttachment, btAddLoginLink;
	
	@Wire
	Label lbAddReplySlipLink;
	@Wire
	Button btAddReplySlipLink;
	
	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		if (!accessOkFlag) {
			return;
		}
		UniLog.log("doAfterCompose NoticeEmail");
		
		//sender
		try {
			JSONObject json = FilingUtil.getJson(sessionHelper.getAgent(), null, JxZkSystem.smtpFilingKey);
			if (json != null)
				tbEmailFrom.setText(json.getString("smtpFrom"));
			else
				UniLog.log1("json is null. key:%s", JxZkSystem.smtpFilingKey);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	
		//fill email to/cc/bcc picklist of student
		BiResult biResult = null;
		try {
			//biResult = BiResultHelper.create(sessionHelper, "edu.Student", "essd_status <> 'Canceled'", -1, new ArrayList(Arrays.asList(Pair.of("essd_name", false))));
			biResult = BiResultHelper.create(sessionHelper, "edu.Student", null, "essd_status <> 'Canceled'", null, -1, new ArrayList(Arrays.asList(Pair.of("essd_name", false))), false);
			while (biResult.next(true)) {
				final String idno = String.format("Student - %s %s (%s)", biResult.getCellString("essd_sdno"), biResult.getCellString("essd_name"), biResult.getCellString("essd_sdtel"));
				final String semail = biResult.getCellString("essd_sdemail");
				UniLog.log1("student email:%s", semail);
				if (StringUtils.isNotBlank(semail)) {
					s2EmailTo.appendChild(new Listitem(idno){{this.setValue(semail);}});
					s2EmailCc.appendChild(new Listitem(idno){{this.setValue(semail);}});
					s2EmailBcc.appendChild(new Listitem(idno){{this.setValue(semail);}});
				}
			}	
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (biResult != null)
				biResult.close();
		}

		//fill email to/cc/bcc picklist of course
		biResult = null;
		try {
			//biResult = BiResultHelper.create(sessionHelper, "edu.Course", "eaav0_status in ('New','Normal','Confirmed')", -1, null);
			biResult = BiResultHelper.create(sessionHelper, "edu.Course", null, "eaav0_status in ('New','Normal','Confirmed')",null, -1, null,false);
			BiResult brStudent = biResult.getSubLink("edu.CourseStudent");
			while (biResult.next(false)) {
				final int rg = biResult.getCellInt("eaav0_rg");
				final String code = String.format("Course - %s %s", biResult.getCellString("eaav0_code"), biResult.getCellString("eaav0_name"));
				UniLog.log1("course code: %s", code);
				int studentCount = 0;
				for (CellCollection scc : brStudent.getRowCollectionList()){
					String sname = scc.getString("essd_name");
					String sstatus = scc.getString("essbsd_status");
					String semail = scc.getString("essd_sdemail");
					UniLog.log1("course student name: %s, status: %s, email: %s", sname, sstatus, semail);
					if (!StringUtils.equals(sstatus, "Cancelled") && StringUtils.isNotBlank(semail))
						studentCount++;
				}
				if (studentCount > 0) {
					s2EmailTo.appendChild(new Listitem(code){{this.setValue(rg);}});
					s2EmailCc.appendChild(new Listitem(code){{this.setValue(rg);}});
					s2EmailBcc.appendChild(new Listitem(code){{this.setValue(rg);}});
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (biResult != null)
				biResult.close();
		}

		//fill email to/cc/bcc picklist of all students
		if (fAllowAllStudent) {
			s2EmailTo.appendChild(new Listitem(PICK_ALL_STUDENT_LABEL){{this.setValue(null);}});
			s2EmailCc.appendChild(new Listitem(PICK_ALL_STUDENT_LABEL){{this.setValue(null);}});
			s2EmailBcc.appendChild(new Listitem(PICK_ALL_STUDENT_LABEL){{this.setValue(null);}});
		}

		s2EmailTo.setMultiple(true);
		s2EmailTo.setAttribute("placeholder", "Please choose recipient");
		s2EmailTo.setAttribute("select2-multiple", "Y");
		ZkUtil.setupSelect2(s2EmailTo, true, true);
		s2EmailCc.setMultiple(true);
		s2EmailCc.setAttribute("placeholder", "Please choose recipient");
		s2EmailCc.setAttribute("select2-multiple", "Y");
		ZkUtil.setupSelect2(s2EmailCc, true, true);
		s2EmailBcc.setMultiple(true);
		s2EmailBcc.setAttribute("placeholder", "Please choose recipient");
		s2EmailBcc.setAttribute("select2-multiple", "Y");
		ZkUtil.setupSelect2(s2EmailBcc, true, true);

		//remove the html tag in cke window
		final Map<String, Object> configMap = new HashMap<String, Object>();
		configMap.put("removePlugins", "elementspath");
		ckEmailContent.setConfig(configMap);
		
		//add dirty event
		winMain.addEventListener("onAddDirtyEvent", new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				setDirtyEvents();
			}
		});
		Events.sendEvent("onAddDirtyEvent", winMain, null);

		//setup send email button
		btEmail.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>(){
			@Override
			public void onZkBiEvent(Event p_event) throws Exception {
				UniLog.logm(this, "got click event");

				if (!cbEmailTo.isChecked() && !cbEmailCc.isChecked() && !cbEmailBcc.isChecked()) {
					ZkUtil.showErrMsg("Please turn on To/CC/Bcc switch button");
					return;
				}
				
				if ((cbEmailTo.isChecked() && isSelectedAllStudent(s2EmailTo))
						|| (cbEmailCc.isChecked() && isSelectedAllStudent(s2EmailCc)) 
						|| (cbEmailBcc.isChecked() && isSelectedAllStudent(s2EmailBcc))) {
					Messagebox.show("Confirm send all students email?", "Question", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new ZkBiEventListener<Event>() {
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							UniLog.log1("event name:%s", event.getName());
							if (event.getName().equals(Events.ON_OK))
								sendEmail();
						}
					});
				}
				else
					sendEmail();
			}
		});
		
		//setup attachment button
		btAttachment.setUpload(String.format("true,maxsize=%d,multiple=true,native", maxAttachmentKB));
		btAttachment.addEventListener(Events.ON_UPLOAD, new ZkBiEventListener<UploadEvent>() {
			@Override
			public void onZkBiEvent(UploadEvent event) throws Exception {
				UniLog.log1("upload event:%s", event.getMedia().getClass());
				long totalAttachmentSize = 0;
				for (Component comp : vbAttachment.getChildren()) {
					Hbox hbox = (Hbox) comp;
					InputStream mediaStream = (InputStream) hbox.getAttribute("mediaStream");
					totalAttachmentSize += mediaStream.available();
				}
				org.zkoss.util.media.Media[] medias = event.getMedias();
				for (org.zkoss.util.media.Media media : medias) {
					InputStream mediaStream = media.getStreamData();
					totalAttachmentSize += mediaStream.available();
				}
				if (totalAttachmentSize > maxAttachmentKB * 1024) {
					ZkUtil.showErrMsg("The total attachment size(%s) cannot exceed %s", FileUtils.byteCountToDisplaySize(totalAttachmentSize), FileUtils.byteCountToDisplaySize(maxAttachmentKB * 1024));
					return;
				}

				for (org.zkoss.util.media.Media media : medias) {
					UniLog.log1("media conentType:%s, format:%s, isBinary:%b", media.getContentType(), media.getFormat(), media.isBinary());
					String mediaName = media.getName();
					InputStream mediaStream = media.getStreamData();
					long mediaSize = mediaStream.available();
					try {
						Hbox hbox = new Hbox();
						hbox.setStyle("background-color:#ececec");
						I icon = new I();
						icon.setSclass("z-icon-paperclip");
						Label label = new Label(String.format("%s (%s)", mediaName, FileUtils.byteCountToDisplaySize(mediaSize)));
						label.setStyle("display:inline-block;min-width:500px");
						Toolbarbutton btnDelete = new Toolbarbutton();
						btnDelete.setIconSclass("z-icon-times");
						btnDelete.setSclass("narrowtoolbarbutton");
						btnDelete.setStyle("color:#444!important");
						btnDelete.setTooltip("Delete attachment");
						btnDelete.setAttribute("hbox", hbox);
						hbox.appendChild(icon);
						hbox.appendChild(label);
						hbox.appendChild(btnDelete);
						hbox.setAttribute("name", mediaName);
						hbox.setAttribute("mediaStream", mediaStream);
						vbAttachment.appendChild(hbox);
						btnDelete.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
							@Override
							public void onZkBiEvent(Event event) throws Exception {
								Hbox hbox = (Hbox) event.getTarget().getAttribute("hbox");
								vbAttachment.removeChild(hbox);
								setDirty(true);
							}
						});
					}
					catch (Exception e) {
						ZkUtil.showErrMsg("Upload error: %s", e.toString());
						e.printStackTrace();
					}
				}
				setDirty(true);
			}
		});

		//setup add login link button
		String baseURL = sessionHelper.getPublicBaseURL();
		if (StringUtils.isNotBlank(baseURL)) {
			lbAddLoginLink.setVisible(true);
			btAddLoginLink.setVisible(true);
			
			final String loginLinkUrl = baseURL;
			btAddLoginLink.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					UniLog.log1("event:%s", event);
					String oldValue = ckEmailContent.getValue();
					String newHtml = "<br/><div style=\"color:black; position:relative; background:lightgrey;width:500px;height:250px;\">" + 
							"   <div style=\"position:absolute; left:50px; top:80px\"><span>The content is protected. Please login to view the content<br/></div>" + 
							"   <div style=\"position:absolute; left:50px; bottom:50px\"><h1><a style=\"color:black; text-decoration:none;\" href=\""+loginLinkUrl+"\">Login(General)</a></h1></div>" + 
							"</div>";
					ckEmailContent.setValue(oldValue + newHtml);
				}
			});
			
			 //andrew220602 add replysliplink
			lbAddReplySlipLink.setVisible(sessionHelper.getAllowReplySlip());
			btAddReplySlipLink.setVisible(sessionHelper.getAllowReplySlip());
			final String replySlipLinkUrl = baseURL + "/jxzkloader.html?zul=eduQnrReplySlip.zul"; 
			btAddReplySlipLink.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					UniLog.log1("event:%s", event);
					String oldValue = ckEmailContent.getValue();
					String newHtml = "<br/><div style=\"color:black; position:relative; background:lightgrey;width:500px;height:250px;\">" + 
							"   <div style=\"position:absolute; left:50px; top:80px\"><span>The content is protected. Please login to view the content<br/></div>" + 
							"   <div style=\"position:absolute; left:50px; bottom:50px\"><h1><a style=\"color:black; text-decoration:none;\" href=\""+replySlipLinkUrl+"\">Login(ReplySlip)</a></h1></div>" + 
							"</div>";
					ckEmailContent.setValue(oldValue + newHtml);
				}
			});
		}
		else {
			lbAddLoginLink.setVisible(false);
			btAddLoginLink.setVisible(false);
			lbAddReplySlipLink.setVisible(false);
			btAddReplySlipLink.setVisible(false);
		}
		
		//setup to/cc/bcc switch button
		cbEmailTo.setChecked(false);
		cbEmailCc.setChecked(false);
		cbEmailBcc.setChecked(true);
		divEmailTo.setVisible(false);
		divEmailTo1.setVisible(false);
		divEmailCc.setVisible(false);
		divEmailCc1.setVisible(false);
		divEmailBcc.setVisible(true);
		divEmailBcc1.setVisible(true);
		cbOnlyUseFirstAddr.setChecked(true);
		cbEmailTo.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
			@Override
			public void onZkBiEvent(CheckEvent event) throws Exception {
				divEmailTo.setVisible(event.isChecked());
				divEmailTo1.setVisible(event.isChecked());
				if (event.isChecked())
					ZkUtil.setupSelect2(s2EmailTo, true, true);
			}
		});
		cbEmailCc.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
			@Override
			public void onZkBiEvent(CheckEvent event) throws Exception {
				divEmailCc.setVisible(event.isChecked());
				divEmailCc1.setVisible(event.isChecked());
				if (event.isChecked())
					ZkUtil.setupSelect2(s2EmailCc, true, true);
			}
		});
		cbEmailBcc.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
			@Override
			public void onZkBiEvent(CheckEvent event) throws Exception {
				divEmailBcc.setVisible(event.isChecked());
				divEmailBcc1.setVisible(event.isChecked());
				if (event.isChecked())
					ZkUtil.setupSelect2(s2EmailBcc, true, true);
			}
		});

	}
	
	private void clearValues() {
		tbEmailTo.setText("");
		Clients.evalJavaScript(String.format("$('#%s').val(null).trigger('change').trigger('select2:unselect')", s2EmailTo.getUuid()));

		tbEmailCc.setText("");
		Clients.evalJavaScript(String.format("$('#%s').val(null).trigger('change').trigger('select2:unselect')", s2EmailCc.getUuid()));

		tbEmailBcc.setText("");
		Clients.evalJavaScript(String.format("$('#%s').val(null).trigger('change').trigger('select2:unselect')", s2EmailBcc.getUuid()));

		tbEmailSubject.setText("");
		ckEmailContent.setValue("");
		List<Component> list = new ArrayList<Component>(vbAttachment.getChildren());
		for (Component comp : list)
			vbAttachment.removeChild(comp);
	}
	
	private void setDirty(boolean isDirty) {
		Clients.evalJavaScript(String.format("if (typeof showEditing !== 'undefined'){showEditing(%s);}", isDirty ? "true" : "false"));
		Clients.confirmClose(isDirty ? "Are you sure to leave?" : null);
	}
	
	private void setDirtyEvents() {
		for (Component comp : new Component[] {cbEmailTo, cbEmailCc, cbEmailBcc, tbEmailFrom, 
				tbEmailTo, s2EmailTo, 
				tbEmailCc, s2EmailCc, 
				tbEmailBcc, s2EmailBcc, 
				cbOnlyUseFirstAddr,
				tbEmailSubject, ckEmailContent}) {
			ZkBiEventListener<? extends Event> el = (ZkBiEventListener<? extends Event>) comp.getAttribute("dirtyEvent");
			if (el != null)
				continue;
			if (comp instanceof Listbox) {
				comp.addEventListener(Events.ON_SELECT, el = new ZkBiEventListener<Event>() {
					@Override
					public void onZkBiEvent(Event event) throws Exception {
						UniLog.log1("event target:%s, name:%s", event.getTarget(), event.getName());
						setDirty(true);
					}
				});
			}
			else if (comp instanceof Checkbox) {
				comp.addEventListener(Events.ON_CHECK, el = new ZkBiEventListener<CheckEvent>() {
					@Override
					public void onZkBiEvent(CheckEvent event) throws Exception {
						UniLog.log1("event target:%s, name:%s", event.getTarget(), event.getName());
						setDirty(true);
					}
				});
			}
			else {
				comp.addEventListener(Events.ON_CHANGING, el = new ZkBiEventListener<InputEvent>() {
					@Override
					public void onZkBiEvent(InputEvent event) throws Exception {
						UniLog.log1("event target:%s, name:%s, prevvalue:%s, value:%s", event.getTarget(), event.getName(), event.getPreviousValue(), event.getValue());
						if (!StringUtils.equals(event.getPreviousValue() != null ? event.getPreviousValue().toString() : null, event.getValue()))
							setDirty(true);
					}
				});
			}
			comp.setAttribute("dirtyEvent", el);
		}
	}

	private void clearDirtyEvents() {
		for (Component comp : new Component[] {cbEmailTo, cbEmailCc, cbEmailBcc, tbEmailFrom, 
				tbEmailTo, s2EmailTo, 
				tbEmailCc, s2EmailCc, 
				tbEmailBcc, s2EmailBcc, 
				cbOnlyUseFirstAddr,
				tbEmailSubject, ckEmailContent}) {
			ZkBiEventListener<? extends Event> el = (ZkBiEventListener<? extends Event>) comp.getAttribute("dirtyEvent");
			if (el == null)
				continue;
			if (comp instanceof Listbox)
				comp.removeEventListener(Events.ON_SELECT, el);
			else if (comp instanceof Checkbox)
				comp.removeEventListener(Events.ON_CHECK, el);
			else
				comp.removeEventListener(Events.ON_CHANGING, el);
			comp.removeAttribute("dirtyEvent");
		}
	}
	
	private boolean isSelectedAllStudent(Listbox s2EmailSend) {
		for (Listitem li : s2EmailSend.getSelectedItems()) {
			if (StringUtils.equals(li.getLabel(), PICK_ALL_STUDENT_LABEL))
				return true;
		}
		return false;
	}
	
	private List<Pair<String, String>> getSendEmailAddrList(Checkbox cbEmailSend, Textbox tbEmailSend, Listbox s2EmailSend) throws Exception {
		Set<String> emailSet = new LinkedHashSet<String>();
		List<Pair<String, String>> emailList = new ArrayList<Pair<String, String>>();
		if (!cbEmailSend.isChecked())
			return emailList;

		if (isSelectedAllStudent(s2EmailSend)) {
			BiResult biResult = null;
			try {
				//biResult = BiResultHelper.create(sessionHelper, "edu.Student", "essd_status <> 'Canceled'", -1, null);
				biResult = BiResultHelper.create(sessionHelper, "edu.Student", null, "essd_status <> 'Canceled'", null, -1, null, false);
				while (biResult.next(true)) {
					final String semail = biResult.getCellString("essd_sdemail");
					if (StringUtils.isNotBlank(semail)) {
						UniLog.log1("send email (student all):%s", semail);
						emailSet.addAll(getStudentEmailAddressList(semail));
					}
				}	
			}
			catch (Exception e) {
				throw e;
			}
			finally {
				if (biResult != null)
					biResult.close();
			}
		}
		else {
			if (StringUtils.isNotBlank(tbEmailSend.getValue())) {
				for (String s : tbEmailSend.getValue().split(",|;|\\s")) {
					if (StringUtils.isNotBlank(s)) {
						UniLog.log1("send email (manual):%s", s);
						emailSet.add(s);
					}
				}
			}
			for (Listitem li : s2EmailSend.getSelectedItems()) {
				String label = li.getLabel();
				if (StringUtils.startsWith(label, "Student - ")) {
					String s = li.getValue();
					if (StringUtils.isNotBlank(s)) {
						UniLog.log1("send email (student):%s", s);
						emailSet.addAll(getStudentEmailAddressList(s));
					}
				}
				else if (StringUtils.startsWith(label, "Course - ")) {
					int courseRg = li.getValue();
					UniLog.log1("send email (course):%d", courseRg);
					BiResult biResult = null;
					try {
						//biResult = BiResultHelper.create(sessionHelper, "edu.Course", String.format("eaav0_rg = %d and eaav0_status in ('New','Normal','Confirmed')", courseRg), -1, null);
						biResult = BiResultHelper.create(sessionHelper, "edu.Course", null, String.format("eaav0_rg = %d and eaav0_status in ('New','Normal','Confirmed')", courseRg), null, -1, null, false);
						if (biResult.next(false)) {
							String code = biResult.getCellString("eaav0_code");
							UniLog.log1("send course code: %s", code);
							BiResult brStudent = biResult.getSubLink("edu.CourseStudent");
							for (CellCollection scc : brStudent.getRowCollectionList()){
								String sname = scc.getString("essd_name");
								String sstatus = scc.getString("essbsd_status");
								String semail = scc.getString("essd_sdemail");
								UniLog.log1("send course student name: %s, status: %s, email: %s", sname, sstatus, semail);
								if (!StringUtils.equals(sstatus, "Cancelled") && StringUtils.isNotBlank(semail)) {
									UniLog.log1("send email (course):%s", semail);
									emailSet.addAll(getStudentEmailAddressList(semail));
								}
							}
						}
					}
					catch (Exception e) {
						e.printStackTrace();
						throw e;
					}
					finally {
						if (biResult != null)
							biResult.close();
					}
				}
			}
		}
		for (String s : emailSet)
			emailList.add(Pair.of(s, ""));
		return emailList;
	}
	
	private void sendEmail() {
		List<Pair<String,String>> toList, ccList, bccList;
		try {
			//to
			toList = getSendEmailAddrList(cbEmailTo, tbEmailTo, s2EmailTo);
				
			//cc
			ccList = getSendEmailAddrList(cbEmailCc, tbEmailCc, s2EmailCc);

			//bcc
			bccList = getSendEmailAddrList(cbEmailBcc, tbEmailBcc, s2EmailBcc);
		}
		catch (Exception e) {
			e.printStackTrace();
			ZkUtil.showErrMsg("error: " + e.getMessage());
			return;
		}
				
		if (toList.isEmpty() && ccList.isEmpty() && bccList.isEmpty()) {
			ZkUtil.showErrMsg("Please input recipient address");
			return;
		}
				
		//subject
		String subject = tbEmailSubject.getValue();
		if (StringUtils.isBlank(subject)) {
			ZkUtil.showErrMsg("Please input subject");
			return;
		}
	
		//content
		String htmlMsg = "<html><body>" + ckEmailContent.getValue() + "</body></html>";
		UniLog.log1("htmlMsg:%s", htmlMsg);

		String txtMsg = null;

		//attachment
		List<EmailAttachment> attList = new ArrayList<EmailAttachment>();
		try {
			for (Component comp : vbAttachment.getChildren()) {
				Hbox hbox = (Hbox) comp;
				String name = (String) hbox.getAttribute("name");
				InputStream mediaStream = (InputStream) hbox.getAttribute("mediaStream");
				File file;
				int i = 0;
				do {
					file = new File(String.format("/tmp/ntemattach_%s_%s_%d", sessionHelper.getAgent(), sessionHelper.getLoginId(), System.currentTimeMillis() + i++));
				} while (file.exists());
				EmailAttachment ea = new EmailAttachment();
				//ea.setName("=?UTF-8?B?" + new String(Base64.encodeBase64(name.getBytes("UTF-8"))) + "?=");
				ea.setName(MimeUtility.encodeWord(name, "UTF-8", "B"));
				ea.setDisposition(EmailAttachment.ATTACHMENT);
				ea.setPath(file.getAbsolutePath());
				attList.add(ea);
				FileOutputStream fos = null;
				try {
					fos = new FileOutputStream(file);
					int len;
					byte[] buffer = new byte[4096];
					while ((len = mediaStream.read(buffer)) != -1)
						fos.write(buffer, 0, len);
				}
				catch (Exception e) {
					e.printStackTrace();
					throw e;
				}
				finally {
					fos.close();
				}
			}
			ReturnMsg rtnMsg = ZkUtil.sendEmail(null, toList, ccList, bccList, subject, htmlMsg, txtMsg, attList, sessionHelper);
			UniLog.log1("rtnMsg:" + rtnMsg);
			if (rtnMsg.getStatus()) {
				Map<String, Object> map = (Map<String, Object>) rtnMsg.getData();
				int okCnt = (Integer)map.get("okCnt");
				int failCnt = (Integer)map.get("failCnt");
				ZkUtil.msg(String.format("Email send successfully.\nTotal:%d, Pass:%d, Fail:%d.", okCnt + failCnt, okCnt, failCnt));
				clearDirtyEvents();
				clearValues();
				setDirty(false);
				ZkUtil.delayPostEvent("onAddDirtyEvent", winMain, null, 500);
			}
			else
				ZkUtil.errMsg("Unable to send email.\nError Message: " + rtnMsg.getMsg());
		}
		catch (Exception e) {
			e.printStackTrace();
			ZkUtil.errMsg("Send email fail, error:" + e.getMessage());
		}
				
		//clear temp attachment file
		for (EmailAttachment ea : attList) {
			try {
				File file = new File(ea.getPath());
				UniLog.log1("delete file %s, exists:%b", ea.getPath(), file.exists());
				if (file.exists()) {
					boolean b = file.delete();
					UniLog.log1("deleted:%b", b);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private Set<String> getStudentEmailAddressList(String addr) {
		Set<String> list = new LinkedHashSet<String>();
		for (String s : addr.trim().split(",|;")) {
			if (StringUtils.isNotBlank(s)) {
				list.add(s.trim());
				if (cbOnlyUseFirstAddr.isChecked())
					break;
			}
		}
		return list;
	}
}
