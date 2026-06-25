package com.uniinformation.jxapp.clinic;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;
import org.zkoss.image.AImage;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.Template;
//import org.zkoss.zsoup.helper.StringUtil;
import org.zkoss.zul.Box;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Image;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Messagebox.ClickEvent;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.impl.InputElement;
import org.zkoss.zul.impl.MessageboxDlg;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfContentByte;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.afs.BiResultAfsQuoDet;
import com.uniinformation.bicore.clinic.BiResultHealthQnr;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellObjWrapper;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ChnftrBuilder;
import com.uniinformation.utils.ChnftrBuilder.PictureItem;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrParser.TemplateFileReader;
import com.uniinformation.utils.CryptoUtil;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TranslateListGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
public class HealthQnr extends JxZkBiBase {
	ArrayList<Radiogroup> questionsRdgList = new ArrayList<Radiogroup>();
	
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		/* use customized GIPI for HealthQnrFiling */
		if(getGipi("clinic.HealthQnrFiling") == null) {
			setGipi("clinic.HealthQnrFiling",new HealthQnrFilingGetItemProperty(p_br.getSubLink("clinic.HealthQnrFiling")));	
		}
		super.bindCellCollection(p_br,mode);
		if(mode == JxZkBiBase.MODE_ADD) {
			jxAdd("btBarcode").setVisible(false);
			jxAdd("btGeneratePdf").setVisible(false);
			jxAdd("list_clinic_HealthQnrFiling").setVisible(false);
			jxAdd("commentsBox").setVisible(false);
			jxAdd("zkDropzone").setVisible(false);
			//jxAdd("btClose").setVisible(false);
			try {
				getBr().getCell("bck_date").set(DateUtil.today());
				getBr().getCell("bck_status").set("NEW");
			} 
			catch(Exception ex){
				UniLog.log(ex);
			}
			
			//jxAdd("bck_status").setVisible(false);
			
			//handle signpad
			jxAdd("btEditSign").setEnable(false);
			jxAdd("btUndoSign").setEnable(false);
			jxAdd("btClearSign").setEnable(true);
			//Clients.evalJavaScript("enableSign(true)");
			new ZkBiAbstractLongOp((Component)jxAdd("btEditSign").getNativeObject(),null,100){
				@Override
				public ReturnMsg longOp() {
					//delay 100 ms to avoid sign object not ready problem
					Clients.evalJavaScript("enableSign(true)");
					return null; 
				}
			};
			jxAdd("statusRow").setVisible(false);
			
		}  else {
			
			//jxAdd("bck_status").setVisible(true);
			
			jxAdd("btBarcode").setVisible(true);
			jxAdd("btGeneratePdf").setVisible(true);
			jxAdd("list_clinic_HealthQnrFiling").setVisible(true);
			jxAdd("commentsBox").setVisible(true);
			jxAdd("zkDropzone").setVisible(true);
			//jxAdd("btUpdate").setVisible(false);
			
			//handle signpad
			jxAdd("btEditSign").setEnable(true);
			jxAdd("btUndoSign").setEnable(false);
			jxAdd("btClearSign").setEnable(false);
			//Clients.evalJavaScript("enableSign(false)");
			new ZkBiAbstractLongOp((Component)jxAdd("btEditSign").getNativeObject(),null,100){
				@Override
				public ReturnMsg longOp() {
					//delay 100 ms to avoid sign object not ready problem
					Clients.evalJavaScript("enableSign(false)");
					return null; 
				}
			};
			
			jxAdd("statusRow").setVisible(true);
		}
		//focus to first tab
		((Tabbox)jxAdd("tabbox").getNativeObject()).setSelectedIndex(0);
		/*
		JxField fd = jxAdd("Pdx_optionlist");
		if(fd != null) { 
			//sample 1
			fd.setItemList(
					new VectorUtil()
					.addElement("Option 1")
					.addElement("Option 2")
					.addElement("Option 3")
					.addElement("Option 4")
					.toVector()
				);
			fd.setSelectList(
					new VectorUtil()
					.addElement("Option 1")
					.addElement("Option 3")
					.toVector()
				);
			
			//sample 2
			fd.setItemListInterface(
					new TranslateListGetItemProperty(
							new VectorUtil()
							.addElement(0)
							.addElement(1)
							.addElement(2)
							.toVector()
							) {

						@Override
						public String translate(Object p_item) {
							// TODO Auto-generated method stub
							if(((Integer) p_item) == 0) return("opttion 1");
							if(((Integer) p_item) == 1) return("opttion 2");
							if(((Integer) p_item) == 2) return("opttion 3");
							return null;
						}

					}
					);
			fd.setSelectList(
					new VectorUtil()
					.addElement(0)
					.addElement(2)
					.toVector()
					);
		}
		*/
		
		//Add question to cc. //it should move to biresult 
		ReturnMsg rtnMsg = ((BiResultHealthQnr) p_br).addQnrFields(p_br.getCurrentCollection(), p_br);
		if (!rtnMsg.getStatus()){ 
			ZkUtil.errMsg(rtnMsg.getMsg());
			abort(true);
			return;
			//throw new RuntimeException(rtnMsg.getMsg()); //TODO handle exception gracefully instead of runtime exception
		}
		
		//Handle custom field event
		//This block of code should merge with ZkBiCellValueMapper
		final EventListener qnrCbEL = new EventListener() {
			public void onEvent(Event event) throws Exception {
				MutableTriple qnrTri = (MutableTriple)event.getTarget().getAttribute("qnrTri");
				synchronized(qnrTri){
					if (event.getTarget() instanceof Checkbox){
						Checkbox cb = (Checkbox) event.getTarget();
						UniLog.logm(this,"got event: %s val:%s", qnrTri, cb.isChecked());
						qnrTri.setRight(cb.isChecked());
					}
					else if (event.getTarget() instanceof Intbox){
						Intbox intbox = (Intbox) event.getTarget();
						UniLog.logm(this,"got event: %s val:%s", qnrTri, intbox.getValue());
						qnrTri.setRight(intbox.getValue());
					}
					else if (event.getTarget() instanceof Textbox){
						Textbox textbox = (Textbox) event.getTarget();
						UniLog.logm(this,"got event: %s val:%s", qnrTri, textbox.getValue());
						qnrTri.setRight(textbox.getValue());
					}
					else if (event.getTarget() instanceof Radiogroup){
						Radiogroup radiogroup = (Radiogroup) event.getTarget();
						UniLog.logm(this,"got event: %s val:%s", qnrTri, radiogroup.getSelectedIndex());
						qnrTri.setRight(radiogroup.getSelectedIndex());
					}
					else{
						UniLog.log1("component type not supported: %s", event.getTarget());
					}
				}
				setDirtyFlag(true);
			}	
		};
		
		//Construct persoanl info grid
		Grid detailGrid = (Grid) jxAdd("detail_grid").getNativeObject();
		LinkedHashMap<String,MutableTriple> pinfo = (LinkedHashMap) p_br.getCurrentCollection().getObjWrapper("custdata_pinfo").getObj();
		for (MutableTriple triObj : pinfo.values()){
			addCustField(triObj,detailGrid,qnrCbEL);
		}
		
		//Construct question grid
		Grid qnrGrid = (Grid) jxAdd("qnr_grid").getNativeObject();
		LinkedHashMap<String,MutableTriple> questions = (LinkedHashMap) p_br.getCurrentCollection().getObjWrapper("custdata_questions").getObj();
		LinkedHashMap<String,HashMap> questionsProp = (LinkedHashMap) p_br.getCurrentCollection().getObjWrapper("custdata_questions_prop").getObj();
		if (qnrGrid != null && questions.size() > 0){
			Components.removeAllChildren(qnrGrid.getRows());
			
			//add dummy row for odd/even row color only
			qnrGrid.getRows().appendChild(new Row(){{
				this.setStyle("display:none;");
				appendChild(new Div());
				appendChild(new Div());
				appendChild(new Div());
				
			}});
			
			int i=0;
			questionsRdgList.clear();
			for (final MutableTriple qnrTri : questions.values()){
				final HashMap propHM = questionsProp.get(qnrTri.getLeft());
				final int seqNum =  ++i;
				
				qnrGrid.getRows().appendChild(new Row(){{
					this.appendChild(new Label() {{
						this.setValue(seqNum +".");
					}});
					this.appendChild(new Label((String)qnrTri.getMiddle()));
					/*
					this.appendChild(new Checkbox(){{ 
						this.setAttribute("qnrTri", qnrTri);
						this.setChecked((Boolean)qnrTri.getRight());
						this.addEventListener(Events.ON_CLICK, qnrCbEL);
					}});
					*/
					Radiogroup rdg = new Radiogroup(){{
						this.setAttribute("qnrTri", qnrTri);
						this.setAttribute("propHM", propHM);
						this.appendItem("Yes\u662F", "Y");
						this.appendItem("No\u5426", "N");
						this.appendItem("N/A\u4E0D\u9069\u7528", "NA");
						if (qnrTri.getRight() instanceof Integer){
							this.setSelectedIndex((Integer)qnrTri.getRight());
						}
						if (this.getSelectedIndex() < 0){
							this.setSelectedIndex(2);
						}
						this.addEventListener(Events.ON_CLICK, qnrCbEL);
					}};
					this.appendChild(rdg);
					questionsRdgList.add(rdg);
				}});
			}

			enableQnrByGender((String)pinfo.get("bck_mf").getRight());
		}
		
	}
	private static void addCustField(final MutableTriple p_triObj, Component p_parentComp,EventListener p_el){
		if (p_triObj == null){
			UniLog.logm(null, "triObj is null");
			return;
		}
		if (StringUtils.isBlank((String) p_triObj.getLeft())){  //non zk component
			//UniLog.logm(null, "triObj left is blank");
			return;
		}
		if (StringUtils.equals((String) p_triObj.getLeft(), "signpad")){
			if (p_triObj.getRight() != null){
				
				String md5 = DigestUtils.md5Hex(""+p_triObj.getRight()).toUpperCase();
				UniLog.logm(null, "loadSign:md5:%s",md5);
				//Clients.evalJavaScript("loadSign('"+p_triObj.getRight()+"')");
				new ZkBiAbstractLongOp(p_parentComp,null,100){
					@Override
					public ReturnMsg longOp() {
						//delay 100 ms to avoid sign object not ready problem
						Clients.evalJavaScript("loadSign('"+p_triObj.getRight()+"')");
						return null;
					}
				};
			}
			return;
		}
		Component comp = p_parentComp.getFellowIfAny((String)p_triObj.getLeft());
		UniLog.log1("comp:%s",comp);
		if (comp == null) {
			UniLog.logm(null, "comp not found %s", ((String) p_triObj.getLeft()));
			return;
		}
		
		if (comp instanceof Checkbox){
			((Checkbox)comp).setChecked((Boolean)p_triObj.getRight());
			comp.addEventListener(Events.ON_CLICK, p_el);
		}
		else if (comp instanceof Radiogroup){
			((Radiogroup)comp).setSelectedIndex(1);
			comp.addEventListener(Events.ON_CLICK, p_el);
		}
		else if (comp instanceof Intbox){
			((Intbox)comp).setValue((Integer)p_triObj.getRight());
			((Intbox)comp).setInstant(true);
			comp.addEventListener(Events.ON_CHANGE, p_el);
		}
		else if (comp instanceof Textbox){
			((Textbox)comp).setValue((String)p_triObj.getRight());
			((Textbox)comp).setInstant(true);
			comp.addEventListener(Events.ON_CHANGE, p_el);
		}
		comp.setAttribute("qnrTri", p_triObj);
		
		Label lb = (Label)p_parentComp.getFellowIfAny("lb_" +(String)p_triObj.getLeft());
		if (lb != null){
			lb.setValue((String) p_triObj.getMiddle());
		}
	}
	
	@Override
	public void afterBind() {
		super.afterBind();
		jxAdd("btGeneratePdf").addActionListener(new JxActionListener(){
			@Override
			public void actionPerformed(JxField field) {
				final Vbox vbox = new Vbox();
				final Checkbox cbHeaderAndFooter = new Checkbox("Header and footer");
				final Checkbox cbTableOfContent = new Checkbox("Table of content");
				final Checkbox cbMaskByDocType = new Checkbox("Mask by document type");
				//andrew190417: do not allow user to choose toc and headerfooters
				/*
				vbox.appendChild(cbHeaderAndFooter);
				vbox.appendChild(cbTableOfContent);
				*/
				vbox.appendChild(cbMaskByDocType);
				cbHeaderAndFooter.setChecked(false);
				cbHeaderAndFooter.setDisabled(true);
				cbTableOfContent.setChecked(false);
				cbTableOfContent.setDisabled(true);
				cbMaskByDocType.setChecked(true);
				MessageboxDlg dlg = ZkUtil.buildMessageboxDlg("Generate Pdf", 
					vbox, 
					new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
					((Component)field.getNativeObject()).getRoot(),
					new EventListener<Messagebox.ClickEvent>(){
						@Override
						public void onEvent(ClickEvent event) throws Exception {
							if (event.getButton() == null)
								return;
							switch (event.getButton()) {
							case OK:
								pdfGenerate(cbHeaderAndFooter.isChecked(), cbTableOfContent.isChecked(), cbMaskByDocType.isChecked());
								break;
							default:
								break;
							}
						}
					}
				);
				dlg.doHighlighted();
				ZkUtil.centerDialog(dlg);
			}
		});
		jxAdd("btBarcode").addActionListener(new JxActionListener(){
			@Override
			public void actionPerformed(JxField field) {
				printBarcode();
			}
		});
		
		//init dropzone
		//Clients.evalJavaScript("addDropzone('div#jsDropzone',true);");
		Clients.evalJavaScript("addDropzone('div#jsDropzone',true,false,'application/pdf',false);");
			
		//handle dropzone add file
		JxField f = addWithoutCheck("zkDropzone");
		((Div) f.getNativeObject()).addEventListener("onDropzoneAdd", new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log("onDropzone: getName():" + event.getName() + " " + event.getData());
				if (!StringUtils.startsWith((String)event.getData(), "dropzone-")){
					UniLog.logm(this,"ignore invalid dropzone uuid");
					return;
				}
				SessionHelper.SessionDataEx filePairSd = (SessionHelper.SessionDataEx) sessionHelper.getSessionData((String)event.getData());
				Pair filePair = filePairSd == null ? null : (Pair)filePairSd.getData();
				if (filePair == null){
					UniLog.logm(this,"ignore, invalid filePair");
					return;
				}
				
				UniLog.logm(this,"got file uuid:%s name:%s size:%d", event.getData(), filePair.getLeft(), ((byte[])filePair.getRight()).length);
				UniLog.logm(this,"change encoding HAHA1:%s", filePair.getLeft());
				//IOUtils.write((byte[])filePair.getRight(), new FileOutputStream("/tmp/haha2.out"));  //write to fs for debug
				//TODO: add to detail listbox
				final BiResult sr = getBr().getSubLink("clinic.HealthQnrFiling");
				String fileName = (String) filePair.getKey();
				byte[] fileData = (byte[]) filePair.getValue();
				byte[] encryptData = CryptoUtil.encrypt(sessionHelper.getAESKey(), fileData, null, true);
				if (encryptData == null) {
					Messagebox.show("encrypt data fail");
					return;
				}
				String key = "zkbi_bodychk_" + getBr().getCell("bck_rg").toString() + "_" + fileName;
				UniLog.log("upload:" + key + ",name:" + fileName + ",dataSize:" + fileData.length + ",encryptSize:" + encryptData.length);
				ByteArrayInputStream bis = new ByteArrayInputStream(encryptData);
				FilingUtil.storeFile(sessionHelper.getAgent(), null, key, key, fileName, bis);
				bis.close();
				CellCollection col = null;
				//int seq = 0;
				int oldRowCount = sr.getRowCount();
				for (int i = 0; i < oldRowCount; i++) {
					CellCollection c = sr.getRowCollectionV(i);
					if (col == null && StringUtils.equals(c.getCell("bckf_ofilename").toString(), fileName))
						col = c;
					//seq = Math.max(seq, c.getCell("bckf_seq").getInt());
				}
				//seq++;
				if (col == null) {
					listboxAddRow(HealthQnr.this, sr, jxAdd("list_clinic_HealthQnrFiling"), null, -1);
					col = sr.getRowCollectionV(oldRowCount);
					//col.getCell("bckf_type").set("Common");
					//col.getCell("bcdt_name").set("General");
					//col.getCell("bcdt_name").set("General");
					col.getCell("bcdt_name").set("");
					//col.getCell("bckf_seq").set(seq);
				}
				col.getCell("bckf_key").set(key);
				col.getCell("bckf_ofilename").set(fileName);
				if (StringUtils.isBlank(col.getCell("bckf_desc").toString())){
					col.getCell("bckf_desc").set(fileName); //set the default desc as filename
				}
				col.getCell("bckf_time").set((int)(System.currentTimeMillis() / 1000));
				setDirtyFlag(true);
			}
		});
		
		//handle dropzone delete file
		((Div) f.getNativeObject()).addEventListener("onDropzoneDelete", new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log("onDropzone: getName():" + event.getName() + " " + event.getData());
				if (!StringUtils.startsWith((String)event.getData(), "dropzone-")){
					UniLog.logm(this,"ignore invalid dropzone uuid");
					return;
				}
				final BiResult sr = getBr().getSubLink("clinic.HealthQnrFiling");
				String key = (String) event.getData();
			}
		});
		
		JxField jxSignBox = addWithoutCheck("signBox");
		((Box) jxSignBox.getNativeObject()).addEventListener("onSaveSign", new EventListener<Event>(){
			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log("onSaveSign: getName():" + event.getName() + " " + event.getData());
				if (event.getData() == null) {
					UniLog.logm(this, "data is null, ignore save");
					return;
				}
				LinkedHashMap<String,MutableTriple> pinfo = (LinkedHashMap) getBr().getCurrentCollection().getObjWrapper("custdata_pinfo").getObj();
				synchronized(pinfo){
					if (pinfo.get("signpad") != null){
						String md5 = DigestUtils.md5Hex(""+event.getData()).toUpperCase();
						boolean isBlank = StringUtils.equals(md5,"F481D5119EF459AA2A987075DF87A4F8");
						UniLog.logm(this, "store signpad data:md5:%s isBlank=%s",md5, isBlank);
						
						pinfo.get("signpad").setRight(event.getData());
						setDirtyFlag(true);
					}
				}
				
				/*
				String base64Image = event.getData().toString().split(",")[1];
				byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
				AImage aImage = new AImage("", imageBytes);
				*/
				
			}
		});		
		JxField jxBtEditSign = addWithoutCheck("btEditSign").addActionListener(new JxActionListener(){
			@Override
			public void actionPerformed(JxField field) {
				jxAdd("btEditSign").setEnable(false);
				jxAdd("btUndoSign").setEnable(true);
				jxAdd("btClearSign").setEnable(false);
				Clients.evalJavaScript("enableSign(true)");
			}
		});
		
		JxField jxBtUndoSign = addWithoutCheck("btUndoSign").addActionListener(new JxActionListener(){
			@Override
			public void actionPerformed(JxField field) {
				LinkedHashMap<String,MutableTriple> pinfo = (LinkedHashMap) getBr().getCurrentCollection().getObjWrapper("custdata_pinfo").getObj();
				MutableTriple signTri = pinfo.get("signpad");
				if (signTri != null){
					synchronized(signTri){
						Clients.evalJavaScript("loadSign('"+pinfo.get("signpad").getMiddle()+"')");
						signTri.setRight(signTri.getMiddle());
					}
				}
			}
		});
		
		((Combobox) jxAdd("bck_mf").getNativeObject()).addEventListener("onChange", new EventListener() {
				public void onEvent(Event event) throws Exception {
					UniLog.log1("got: %s",((Combobox)event.getTarget()).getValue());
					enableQnrByGender(((Combobox)event.getTarget()).getValue());
				}
		});
	}
	
	/***
	 * enable/disable question based on gender selection
	 * @param p_gender
	 */
	private void enableQnrByGender(String p_gender){
		UniLog.log1("gender:%s", p_gender);
		
		for (Radiogroup rg : questionsRdgList){
			boolean enableFlag = checkQuestionIsEnable((HashMap)rg.getAttribute("propHM"), p_gender);
			for (Radio rd : rg.getItems()){
				rd.setDisabled(!enableFlag);
			}
		}
	}
	
	/***
	 * 
	 * @param propHM - question properties hashmap
	 * @param p_gender - use selected gender
	 * @return true enable
	 */
	private static boolean checkQuestionIsEnable(HashMap propHM, String p_gender){
		boolean enableFlag = false;
		String qnrq_mf = (String) propHM.get("qnrq_mf");
		if (!enableFlag && (qnrq_mf == null || qnrq_mf.equals("U"))){  //question is unspecified
			enableFlag = true;
		}
		if (!enableFlag && (p_gender == null || p_gender.equals("U"))){  //user select unspecified
			enableFlag = true;
		}
		if (!enableFlag && (StringUtils.equalsIgnoreCase(p_gender, qnrq_mf))){ //question is equal to user selection
			enableFlag = true;
		}
		return(enableFlag);
	}
	
	@Override 
	protected void afterDeleteLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals("clinic.HealthQnrFiling")) {
			try {
				CellCollection col = sr.getRowCollectionV(idx);
				String fileName = col.getCell("bckf_key").getString();
				if (StringUtils.isNotBlank(fileName)) {
					//int delCount = FilingUtil.deleteFile(sessionHelper.getAgent(), null, fileName, FilingUtil.VER_ALL);
					//UniLog.log("deleted file " + fileName + ",delete count:" + delCount);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	private static final String PDF_LOGO_PATH = Sessions.getCurrent().getWebApp().getRealPath("images/hq_pdf_header.jpg");
	private ChnftrParser createChnftrParser(InputStream inStream) throws Exception {
		//return new ChnftrParser(inStream, PageSize.A4, 8f * ChnftrParser.DOCUMENT_DPI, 11.69f * ChnftrParser.DOCUMENT_DPI, ChnftrParser.CHNFTR_DPI, 11, 6);
		return new ChnftrParser(inStream, PageSize.A4, PageSize.A4.getWidth(), PageSize.A4.getHeight(), ChnftrParser.CHNFTR_DPI, 11, 6);
	}
	private void pdfGenerate(boolean hasHeaderAndFooter, boolean hasTableOfContent, boolean isMaskByDocType) {
		//build Health Questionnaire pdf
		PdfGenerateHealthQ genhealthQ = new PdfGenerateHealthQ(hasHeaderAndFooter, isMaskByDocType);
		ByteArrayOutputStream bosHealthQ = genhealthQ.build();
		if (hasTableOfContent) {
			//build Table of content pdf
			PdfGenerateTableOfContent genTableOfContent = new PdfGenerateTableOfContent(hasHeaderAndFooter, isMaskByDocType);
			ByteArrayOutputStream bosTableOfContent = genTableOfContent.build(genhealthQ.getTableOfContentRowList());

			ByteArrayInputStream inStream = null;
			ByteArrayOutputStream outStream = null;
			ByteArrayInputStream ins = null;
			try {
				//marge Health Questionnaire & Table of content pdf
				ChnftrParser parser = createChnftrParser(null);
				inStream = new ByteArrayInputStream(bosTableOfContent.toByteArray());
				parser.loadTemplateStream(inStream);
				inStream.close();
				inStream = new ByteArrayInputStream(bosHealthQ.toByteArray());
				parser.loadTemplateStream(inStream);
				inStream.close();
				outStream = new ByteArrayOutputStream();
				parser.print(outStream);
				outStream.close();
				ZkUtil.showPdfDialog(((Button) jxAdd("btGeneratePdf").getNativeObject()).getRoot(), sessionHelper, outStream.toByteArray(), buildPdfFilename());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (inStream != null)
						inStream.close();
					if (outStream != null)
						outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} else {
			ZkUtil.showPdfDialog(((Button) jxAdd("btGeneratePdf").getNativeObject()).getRoot(), sessionHelper, bosHealthQ.toByteArray(), buildPdfFilename());
		}
	}
	private String buildPdfFilename(){
		StringBuilder sb = new StringBuilder();
		sb.append("QNR_");
		sb.append(getBr().getCurrentCollection().getCell("bck_id"));
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		sb.append("_");
		sb.append(sdf.format(getBr().getCell("bck_date").getDate()));
		sb.append(".pdf");
		return(sb.toString().replaceAll("[ ]*", "").toUpperCase()); 
	}
	private void printBarcode() {
		//final List<Pair<String, Intbox>> compList = new ArrayList<Pair<String, Intbox>>();
		final List<Pair<String, Spinner>> compList = new ArrayList<Pair<String, Spinner>>();
		//final List<Pair<String, Integer>> valList = new ArrayList<Pair<String, Integer>>();
		Grid grid = new Grid();
		grid.setWidth("300px");
		grid.appendChild(new Columns(){{
			appendChild(new Column(){{setHflex("1");}});
			appendChild(new Column(){{setHflex("min");}});
		}});
		Rows rows = new Rows();
		rows.setParent(grid);
		try {
			//TableRec tr = getBr().getSelectUtil().getQueryResult("select bcdt_rg, bcdt_name from bodychk_doctype where bcdt_status = 'Y'", null);
			TableRec tr = getBr().getSelectUtil().getQueryResult("select bcdt_rg, bcdt_name from bodychk_doctype where bcdt_status = 'Y' order by bcdt_name", null); //andrew190425: should obtain from view instead of direct query
			for (int i = 0; i < tr.getRecordCount(); i++) {
				tr.setRecPointer(i);
				Row row = new Row();
				Label label = new Label((String) tr.getField("bcdt_name"));
				/*
				Intbox intbox = new Intbox(0);
				intbox.setAttribute("bcdt_rg", tr.getFieldInt("bcdt_rg"));
				row.appendChild(label);
				row.appendChild(intbox);
				row.setParent(rows);
				compList.add(Pair.of(label.getValue(), intbox));
				*/
				Spinner spinner = new Spinner(0);
				spinner.setConstraint("no negative");
				spinner.setAttribute("bcdt_rg", tr.getFieldInt("bcdt_rg"));
				row.appendChild(label);
				row.appendChild(spinner);
				row.setParent(rows);
				compList.add(Pair.of(label.getValue(), spinner));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		ZkUtil.buildMessageboxDlg("Print QR Code", 
			grid, 
			new Messagebox.Button[]{Messagebox.Button.OK, Messagebox.Button.CANCEL}, 
			((Button) jxAdd("btBarcode").getNativeObject()).getRoot(),
			new EventListener<Messagebox.ClickEvent>(){
				private void print() {
					/*BarcodePrint bp = new BarcodePrint(valList);
					ByteArrayOutputStream bos = bp.build();
					showPdfDialog(bos.toByteArray(), "qrcode.pdf");*/
					printLabels(sessionHelper.getPrinterHost(), sessionHelper.getPrinterPort(), compList);
				}
				@Override
				public void onEvent(ClickEvent event) throws Exception {
					if (event.getButton() == null)
						return;
					switch (event.getButton()) {
					case OK:
						boolean foundAllZero = true;
						for (Pair<String, Spinner> pair : compList) {
							if (pair.getValue().intValue() > 0)
								foundAllZero = false;
							//valList.add(Pair.of(pair.getKey(), pair.getValue().getValue()));
						}
						if (foundAllZero){
							//Messagebox.show("Warning! all qty is 0");
							ZkUtil.showWarnMsg("Please adjust label quantity");
						}
						else
							print();
						break;
					default:
						break;
					}
				}
			}
		).doHighlighted();
	}
	private abstract class PdfGenerate {
		int docHeight = 1120;
		int docLeft = 50;
		int docTop = 50;
		int fontPt = 10;
		//static final String parserOption = "-x2 -p0 -f4 -c0 -l1";
		int offset;
		int rowHeight = 20;
		String engFont = "helv_nr";
		String chnFont = "chinese";
		boolean hasHeaderAndFooter, isMaskByDocType;
		ChnftrBuilder builder = new ChnftrBuilder();
		ChnftrBuilder.Cell currCell = null;
		PdfGenerate(boolean hasHeaderAndFooter, boolean isMaskByDocType) {
			this.hasHeaderAndFooter = hasHeaderAndFooter;
			this.isMaskByDocType = isMaskByDocType;
		}
		boolean offset(int o, int rowHeight) {
			offset += o;
			if (currCell.getAbsoluteY() + offset + rowHeight > docHeight) {
				currCell.build();
				builder.P();
				currCell = new ChnftrBuilder.Cell(builder, docLeft, docTop, 0, 0).setFontAndSize(fontPt, engFont, chnFont);
				offset = 0;
				return true;
			}
			return false;
		}
		boolean offset(int o) {
			return offset(o, rowHeight);
		}
		boolean offset() {
			return offset(rowHeight);
		}
	}
	private class MaskArea {
		int left, top, right, bottom;
		String picturePath;
		@Override
		public boolean equals(Object o) {
			if (o != null && o instanceof MaskArea) {
				MaskArea ma = (MaskArea) o;
				return left == ma.left && top == ma.top && right == ma.right && bottom == ma.bottom;
			}
			return false;
		}
	}
	private class TableOfContentRow {
		String desc = "", desc1 = "";
		int firstPage;
		List<MaskArea> maskAreas = new ArrayList<MaskArea>();
		public void addMaskAreas(String src) {
			UniLog.log("addMaskAreas:" + src);
			if (StringUtils.isNotBlank(src)) {
				try {
					String[] ss = src.trim().split(";");
					for (String s : ss) {
						String mapsa = s;
						String mapsp = null;
						String[] maps = s.split(":");
						if (maps.length == 2) {
							mapsa = maps[0];
							mapsp = maps[1];
						}
						UniLog.log("mapsa:" + mapsa + ",mapsp:" + mapsp);
						String[] mas = mapsa.split(",");
						if (mas.length == 4) {
							MaskArea ma = new MaskArea();
							try {
								int[] ltrb = new int[mas.length];
								for (int i = 0; i < mas.length; i++)
									ltrb[i] = (int)(Double.parseDouble(mas[i]) * 10 + 0.5) * 100;
								ma.left = ChnftrParser.umChnftrPx2ChnftrPx(ltrb[0]);
								ma.top = ChnftrParser.umChnftrPx2ChnftrPx(ltrb[1]);
								ma.right = ChnftrParser.umChnftrPx2ChnftrPx(ltrb[2]);
								ma.bottom = ChnftrParser.umChnftrPx2ChnftrPx(ltrb[3]);
								UniLog.log("maskAreas rect:" + ltrb[0] + "," + ltrb[1] + "," + ltrb[2] + "," + ltrb[3]);
								UniLog.log("maskAreas rect:" + ma.left + "," + ma.top + "," + ma.right + "," + ma.bottom);
							} catch (Exception e) {
								e.printStackTrace();
							}
							ma.picturePath = sessionHelper.getMaskPhotoFolder() + File.separator + mapsp;
							maskAreas.add(ma);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	private class PdfGenerateTableOfContent extends PdfGenerate {
		PdfGenerateTableOfContent(boolean hasHeaderAndFooter, boolean isMaskByDocType) {
			super(hasHeaderAndFooter, isMaskByDocType);
		}
		ByteArrayOutputStream build(List<TableOfContentRow> tableOfContentRows) {
			UniLog.log("PdfGenerateTableOfContent build " + tableOfContentRows.size());
			ByteArrayOutputStream outStream = null;
			ByteArrayInputStream inStream = null;
			try {
				currCell = new ChnftrBuilder.Cell(builder, docLeft, docTop, 0, 0).setFontAndSize(fontPt, engFont, chnFont);
				if (hasHeaderAndFooter) {
					currCell.addItem(new ChnftrBuilder.PictureItem(0, 0).setAll(0, 75, 0, false, PDF_LOGO_PATH));
					offset(90);
				}
				
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText("Table of contents");
				offset(rowHeight * 2);
				boolean flag = false;
				int itemNum = 0;
				for (TableOfContentRow toc : tableOfContentRows) {
					if (flag)
						offset();
					currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(String.format("%d.", ++itemNum));
					currCell.addItem(new ChnftrBuilder.TextItem(30, offset)).setText(toc.desc);
					currCell.addItem(new ChnftrBuilder.TextItem(130, offset)).setText(toc.desc1);
					currCell.addItem(new ChnftrBuilder.TextItem(650, offset).setAlign(PdfContentByte.ALIGN_CENTER, 50)).setText("" + (toc.firstPage + 1));
					flag = true;
				}
				currCell.build();
				outStream = new ByteArrayOutputStream();
				builder.writeTo(outStream);
				outStream.close();
				inStream = new ByteArrayInputStream(outStream.toByteArray());
				ChnftrParser parser = createChnftrParser(inStream);
				outStream = new ByteArrayOutputStream();
				parser.print(outStream);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (outStream != null)
						outStream.close();
					if (inStream != null)
						inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return outStream;
		}
	}
	private class PdfGenerateHealthQ extends PdfGenerate {
		List<TableOfContentRow> tableOfContentRows = new ArrayList<TableOfContentRow>();
		PdfGenerateHealthQ(boolean hasHeaderAndFooter, boolean isMaskByDocType) {
			super(hasHeaderAndFooter, isMaskByDocType);
			if (hasHeaderAndFooter) {
				docTop = 100;
				docHeight = 1100;
			}
		}
		ByteArrayOutputStream build() {
			UniLog.log("PdfGenerateHealthQ build");
			ByteArrayOutputStream outStream = null;
			ByteArrayInputStream inStream = null;
			try {
				currCell = new ChnftrBuilder.Cell(builder, docLeft, docTop, 0, 0).setFontAndSize(fontPt, engFont, chnFont);
				//currCell.addItem(new ChnftrBuilder.PictureItem(0, offset).setAll(0, 0, 0, false, Sessions.getCurrent().getWebApp().getRealPath("clinic/maskimg/logo_2478x507.png")));
				//offset(180, 0);
				currCell.addItem(new ChnftrBuilder.PictureItem(130, offset).setAll(0, 0, 0, false, Sessions.getCurrent().getWebApp().getRealPath("images/logo/clinic_logo.png")));
				offset(160, 0);
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset).setB().setU()).setText("Health Questionnaire \u9AD4\u683C\u5065\u5EB7\u8ABF\u67E5\u554F\u5377");
				offset();
				
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(columnHeader("bck_date", null));
				currCell.addItem(new ChnftrBuilder.TextItem(200, offset)).setText(cellValue("bck_date", null).toString());
				offset();
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(columnHeader("bck_id", null));
				currCell.addItem(new ChnftrBuilder.TextItem(200, offset)).setText(cellValue("bck_id", null).toString());
				offset();
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(columnHeader("bck_name", null));
				currCell.addItem(new ChnftrBuilder.TextItem(200, offset)).setText(cellValue("bck_name", null).toString());
				offset(rowHeight * 2);
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset).setB()).setText("Family History \u5bb6\u5ead\u8cc7\u6599");
				offset();
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(
						String.format("%s   %s   %s", 
							cellCheckLabelAndValue("bck_hybertension", "custdata_pinfo"),
							cellCheckLabelAndValue("bck_diabetes", "custdata_pinfo"),
							cellCheckLabelAndValue("bck_heartdis", "custdata_pinfo")
							));
				offset();
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(
						String.format("%s   %s   %s", 
							cellCheckLabelAndValue("bck_kidneydis", "custdata_pinfo"),
							cellCheckLabelAndValue("bck_mentaldisorder", "custdata_pinfo"), 
							cellCheckLabelAndValue("bdk_hereditarytdis", "custdata_pinfo")
							));
				offset(rowHeight * 2);
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset).setB()).setText("Personal History\u500b\u4eba\u8cc7\u6599");
				offset();
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(
						String.format("%s   %s   %s", 
							cellLabelAndValue("bck_mf", "custdata_pinfo"), 
							cellCheckLabelAndValue("bck_married", "custdata_pinfo"), 
							cellLabelAndValue("bck_numofchildren", "custdata_pinfo")
							));
				offset(rowHeight * 2);
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset).setB()).setText("Personal Health \u500b\u4eba\u5065\u5eb7\u72c0\u6cc1:");
				offset();
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(
						String.format("%s   %s", 
							cellCheckLabelAndValue("bck_smoking", "custdata_pinfo"), 
							cellLabelAndValue("bck_smokingqty", "custdata_pinfo")
							));
				offset();
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(
						String.format("%s   %s", 
							cellCheckLabelAndValue("bck_alcohol", "custdata_pinfo"), 
							cellLabelAndValue("bck_alcoholqty", "custdata_pinfo")
							));
				offset();
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(
						String.format("%s", 
							cellCheckLabelAndValue("bck_exercise", "custdata_pinfo")
							));
				offset(rowHeight * 2);
				currCell.addItem(new ChnftrBuilder.TextItem(0, offset).setB()).setText("Personal Health Questionnaire \u500b\u4eba\u5065\u5eb7\u72c0\u6cc1");
				int seq = 0;
				for (String cellLabel : custdataCellLabels("custdata_questions"))
					printPersonHealthQ(cellLabel, ++seq);
				//for (int i = 0; i < 39; i++)
				//	printPersonHealthQ(String.format("bck_checkbox_%02d", i + 1));
				currCell.build();
				outStream = new ByteArrayOutputStream();
				builder.writeTo(outStream);
				outStream.close();
				
				//parse Health Questionnaire
				inStream = new ByteArrayInputStream(outStream.toByteArray());
				ChnftrParser parser = createChnftrParser(inStream);
				inStream.close();
				outStream = new ByteArrayOutputStream();
				parser.print(outStream);
				outStream.close();

				//marge Health Questionnaire & pdfs
				parser = createChnftrParser(null);
				ChnftrBuilder builderM = new ChnftrBuilder();
				if (hasHeaderAndFooter) {
					//add pagenum of pagecount
					new ChnftrBuilder.TextItem(builderM, 700, 1110)
						.setFontAndSize(10, engFont, chnFont)
						.setText("${pagenum} of ${pagecount}")
						.build();
					new ChnftrBuilder.ColorItem(builderM).setColor(255, 255, 255).build();
					new ChnftrBuilder.SolidItem(builderM, 0, 0).setRect(0, 0, 826, 100).build();
					new ChnftrBuilder.ColorItem(builderM).setColor(0, 0, 0).build();
					new ChnftrBuilder.PictureItem(builderM, 50, 0)
						.setAll(0, 75, 0, false, PDF_LOGO_PATH)
						.build();
					UniLog.log("setTemplatePageChnftrText " + builderM.toString());
					//parser.setTemplatePageChnftrText(builderM.toString());
				}
				inStream = new ByteArrayInputStream(outStream.toByteArray());
				TemplateFileReader tfr = parser.loadTemplateStream(inStream);
				inStream.close();
				int totPage = 0;
				TableOfContentRow toc = new TableOfContentRow();
				toc.desc = "Health Questionnaire";
				toc.firstPage = totPage;
				totPage += tfr.totalPage();
				tableOfContentRows.add(toc);
				if (hasHeaderAndFooter) {
					//add sub header
					tfr.setPageChnftrText(
						new ChnftrBuilder.TextItem(new ChnftrBuilder(), 50, 75)
							.setFontAndSize(12, engFont, chnFont)
							.setText(String.format("%d. %s (${tpagenum}/${tpagecount})", tableOfContentRows.size(), toc.desc))
							.build().toString() + builderM.toString()
					);
				}

				BiResult sr = getBr().getSubLink("clinic.HealthQnrFiling");
				for (int i = 0; i < sr.getRowCount(); i++) {
					ByteArrayOutputStream bos = null;
					ByteArrayInputStream bis = null;
					try {
						CellCollection col = sr.getRowCollectionV(i);
						bos = new ByteArrayOutputStream();
						String type = col.getCell("bckf_type").getString();
						String key = col.getCell(type.equals("L") ? "bcpdt_filkey" : "bckf_key").getString();
						FilingUtil.getFile(sessionHelper.getAgent(), null, key, bos);
						byte[] decryptData = CryptoUtil.decrypt(sessionHelper.getAESKey(), bos.toByteArray(), true);
						if (decryptData != null) {
							bis = new ByteArrayInputStream(decryptData);
							tfr = parser.loadTemplateStream(bis);
							if (tfr != null) {
								toc = new TableOfContentRow();
								//toc.desc = col.getCell("bckf_type").getString();
								toc.desc = col.getCell("bcdt_name").getString();
								toc.desc1 = col.getCell("bckf_desc").getString();
								toc.addMaskAreas(col.getCell("bcdt_maskarea").getString());
								toc.firstPage = totPage;
								totPage += tfr.totalPage();
								tableOfContentRows.add(toc);
								ChnftrBuilder b = new ChnftrBuilder();
								if (hasHeaderAndFooter) {
									//add sub header
									new ChnftrBuilder.TextItem(b, 50, 75)
										.setFontAndSize(12, engFont, chnFont)
										.setText(String.format("%d. %s (${tpagenum}/${tpagecount})", 
												tableOfContentRows.size(), (toc.desc + " " + toc.desc1).trim())).build();
								}
								if (isMaskByDocType) {
									new ChnftrBuilder.ColorItem(b).setColor(255, 255, 255).build();
									for (MaskArea ma : toc.maskAreas) {
										new ChnftrBuilder.SolidItem(b).setRect(ma.left, ma.top, ma.right, ma.bottom).build();
										new ChnftrBuilder.PictureItem(b).setAll(0, 0, 0, false, ma.picturePath).setXY(ma.left, ma.top).build();
									}
									new ChnftrBuilder.ColorItem(b).setColor(0, 0, 0).build();
								}
								tfr.setPageChnftrText(b.toString() + builderM.toString());
								UniLog.log("chntext:" + tfr.getChnftrText());
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (bos != null)
							bos.close();
						if (bis != null)
							bis.close();
					}
				}
				outStream = new ByteArrayOutputStream();
				parser.print(outStream);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (outStream != null)
						outStream.close();
					if (inStream != null)
						inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return outStream;
		}
		Set<String> custdataCellLabels(String custdataCellLabel) {
			CellObjWrapper ow = getBr().getCurrentCollection().getCell(custdataCellLabel).getObjWrapper();
			Map<String, MutableTriple<String, String, Object>> map = (Map<String, MutableTriple<String, String, Object>>) ow.getObj();
			return map.keySet();
		}
		String columnHeader(String colLabel, String custdataCellLabel) {
			String value;
			if (custdataCellLabel != null) {
				CellObjWrapper ow = getBr().getCurrentCollection().getCell(custdataCellLabel).getObjWrapper();
				Map<String, MutableTriple<String, String, Object>> map = (Map<String, MutableTriple<String, String, Object>>) ow.getObj();
				value = map.get(colLabel).getMiddle();
			} else
				value = getBr().getView().getColumnByLabel(colLabel).getEngName();
			return sessionHelper.getLabel(value);
		}
		Object cellValue(String cellLabel, String custdataCellLabel) {
			if (custdataCellLabel != null) {
				CellObjWrapper ow = getBr().getCurrentCollection().getCell(custdataCellLabel).getObjWrapper();
				Map<String, MutableTriple<String, String, Object>> map = (Map<String, MutableTriple<String, String, Object>>) ow.getObj();
				return map.get(cellLabel).getRight();
			} else
				return getBr().getCell(cellLabel).getString();
		}
		String cellCheckValue(String cellLabel, String custdataCellLabel) {
			Object value = cellValue(cellLabel, custdataCellLabel);
			/*if (value instanceof Boolean)
				return ((Boolean) value) ? "\u221a" : " ";
			else
				return value.equals("Y") ? "\u221a" : " ";*/
			if (value instanceof Boolean)
				return ((Boolean) value) ? " [ X ]" : " [   ]";
			else
				return value.equals("Y") ? " [ X ]" : " [   ]";
		}
		String cellCheckLabelAndValue(String cellLabel, String custdataCellLabel) {
			return columnHeader(cellLabel, custdataCellLabel) + " " + cellCheckValue(cellLabel, custdataCellLabel);
		}
		String cellLabelAndValue(String cellLabel, String custdataCellLabel) {
			return columnHeader(cellLabel, custdataCellLabel) + " " + cellValue(cellLabel, custdataCellLabel);
		}
		void printPersonHealthQ(String cellLabel, int seq) {
			if (offset()) { //is next page
				currCell.addItem(new ChnftrBuilder.PictureItem(130, offset).setAll(0, 0, 0, false, Sessions.getCurrent().getWebApp().getRealPath("images/logo/clinic_logo.png")));
				offset(160, 0);
			}
			currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setAlign(PdfContentByte.ALIGN_RIGHT, 20).setText(String.format("%d.", seq));
			currCell.addItem(new ChnftrBuilder.TextItem(30, offset)).setText(columnHeader(cellLabel, "custdata_questions"));
			//currCell.addItem(new ChnftrBuilder.TextItem(650, offset)).setText(cellCheckValue(cellLabel));
			currCell.addItem(new ChnftrBuilder.TextItem(650, offset)).setAlign(PdfContentByte.ALIGN_RIGHT, 10).setText("[");
			currCell.addItem(new ChnftrBuilder.TextItem(690, offset)).setAlign(PdfContentByte.ALIGN_LEFT, 10).setText("]");
			//if (getBr().getCell(cellLabel).getString().equals("Y"))
			//if ((Boolean)cellValue(cellLabel, "custdata_questions"))
			//	currCell.addItem(new ChnftrBuilder.TextItem(660, offset)).setAlign(PdfContentByte.ALIGN_CENTER, 20).setText("X");
			String s;
			switch ((Integer)cellValue(cellLabel, "custdata_questions")) {
			case 0:
				s = "Y";
				break;
			case 1:
				s = "N";
				break;
			default:
				s = "N/A";
				break;
			}
			currCell.addItem(new ChnftrBuilder.TextItem(660, offset)).setAlign(PdfContentByte.ALIGN_CENTER, 30).setText(s);
		}
		List<TableOfContentRow> getTableOfContentRowList() {
			return tableOfContentRows;
		}
	}
	private class BarcodePrint extends PdfGenerate {
		final List<Pair<String, Integer>> valList;
		BarcodePrint(List<Pair<String, Integer>> valList) {
			super(false, false);
			this.valList = valList;
		}
		ByteArrayOutputStream build() {
			UniLog.log("BarcodePrint build");
			ByteArrayOutputStream outStream = null;
			ByteArrayInputStream inStream = null;
			try {
				currCell = new ChnftrBuilder.Cell(builder, docLeft, docTop, 0, 0).setFontAndSize(fontPt, engFont, chnFont);
				//BiResult sr = getBr().getSubLink("clinic.HealthQnrFiling");
				boolean flag = false;
				/*
				for (int i = 0; i < sr.getRowCount(); i++) {
					ByteArrayOutputStream bos = null;
					ByteArrayInputStream bis = null;
					try {
						CellCollection col = sr.getRowCollectionV(i);
						JSONObject json = new JSONObject();
						json.put("ID Number", getBr().getCell("bck_id").getString());
						json.put("User Name", getBr().getCell("bck_name").getString());
						json.put("Doc Type", col.getCell("bckf_type").getString());
						if (flag)
							offset(50, 100);
						currCell.addItem(new ChnftrBuilder.QRcodeItem(0, offset).setText(json.toString()).setSize(100, 100));
						offset(100);
						currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(
								String.format("%s, %s, %s", json.getString("ID Number"), json.getString("User Name"), json.getString("Doc Type")));
						flag = true;
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (bos != null)
							bos.close();
						if (bis != null)
							bis.close();
					}
				}
				*/
				
				/*//hardcode the label list
				String reportTypes[] = {"General", "General", "Blood Test", "Blood Test", "Urinalsis", "Urinalsis" };
				for (String reportType : reportTypes){
					ByteArrayOutputStream bos = null;
					ByteArrayInputStream bis = null;
					try {
						JSONObject json = new JSONObject();
						json.put("ID Number", getBr().getCell("bck_id").getString());
						json.put("User Name", getBr().getCell("bck_name").getString());
						json.put("Doc Type", reportType);
						if (flag)
							offset(50, 100);
						currCell.addItem(new ChnftrBuilder.QRcodeItem(0, offset).setText(json.toString()).setSize(100, 100));
						offset(100);
						currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(
								String.format("%s (%s) - %s", json.getString("User Name"), json.getString("ID Number"), json.getString("Doc Type")));
						flag = true;
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (bos != null)
							bos.close();
						if (bis != null)
							bis.close();
					}
				}*/
				for (Pair<String, Integer> pair : valList){
					ByteArrayOutputStream bos = null;
					ByteArrayInputStream bis = null;
					try {
						JSONObject json = new JSONObject();
						json.put("User ID", getBr().getCell("bck_id").getString());  //key: USER ID + DATE
						json.put("Date", getBr().getCell("bck_date").getString());
						json.put("User Name", getBr().getCell("bck_name").getString());
						json.put("Doc Type", pair.getKey());
						for (int i = 0; i < pair.getValue(); i++) {
							if (flag)
								offset(50, 150);
							currCell.addItem(new ChnftrBuilder.QRcodeItem(0, offset).setText(json.toString()).setSize(150, 150));
							offset(150);
							currCell.addItem(new ChnftrBuilder.TextItem(0, offset)).setText(
									String.format("%s %s - %s", json.getString("User ID"), json.getString("Date"), json.getString("Doc Type")));
							flag = true;
						}
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						if (bos != null)
							bos.close();
						if (bis != null)
							bis.close();
					}
				}
				
				currCell.build();
				UniLog.log("printBarcode text:" + builder.toString());

				outStream = new ByteArrayOutputStream();
				builder.writeTo(outStream);
				outStream.close();
				inStream = new ByteArrayInputStream(outStream.toByteArray());
				ChnftrParser parser = createChnftrParser(inStream);
				outStream = new ByteArrayOutputStream();
				parser.print(outStream);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					if (outStream != null)
						outStream.close();
					if (inStream != null)
						inStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return outStream;
		}
	}
	
	/* Customized GIPI for HealthQnrFiling */
	class HealthQnrFilingGetItemProperty extends BiGetItemProperty {
		Template buttonTemplate;
		Vector columnListFiling;
		HealthQnrFilingGetItemProperty(BiResult p_br) {
			super(p_br);
			/* create customized list columns */
			columnListFiling = new Vector<BiColumn>();
			buttonTemplate = ((Component) getNativeComponent()).getTemplate("template_HealthQnrFiling");
			/* clone columnlist from BiView definition */
			Vector<BiColumn> v = p_br.getListColumns();
			for(BiColumn bc : v) {
				columnListFiling.add(bc);
			}
			/* append button panel at end of columnlist */
			columnListFiling.add(buttonTemplate);
		}
		/* override getListColumns to use customized columnlist*/
		@Override
		protected Vector getListColumns(Object p_v) {
			return(columnListFiling);
		}
		
		/* override getColumnValueByName to set onClick event to buttons in template */
		@Override
		public Object getColumnValueByName(final Object p_v,String p_name) {
			Object o = bigibr.getTrStatObj(p_v);
			final CellCollection col = bigibr.getRowCollectionO(bigibr.getTrStatObj(p_v));
			if(p_name.equals("btOpenPdf")) {
				return(new JxActionListener() {
					public void actionPerformed(JxField fd){
						UniLog.logm(this, "Pressed for "+ p_v);
						String type = col.getCell("bckf_type").getString();
						String key;
						String fileName;
						if (type.equals("L")) {
							key = col.getCell("bcpdt_filkey").getString();
							fileName = col.getCell("bcpdt_docfname").getString();
						} else {
							key = col.getCell("bckf_key").getString();
							fileName = col.getCell("bckf_ofilename").getString();
						}
						UniLog.logm(this, "key:"+ key + ",filename:" + fileName + ",type:" + type);
						ByteArrayOutputStream bos = null;
						try {
							bos = new ByteArrayOutputStream();
							FilingUtilObject fuo = FilingUtil.getFile(sessionHelper.getAgent(), null, key, bos);
							bos.close();
							if (fuo == null){
								ZkUtil.showErrMsg("Cannot open %s",fileName);
								return;
							}
							byte[] decryptData = CryptoUtil.decrypt(sessionHelper.getAESKey(), bos.toByteArray(), true);
							ZkUtil.showPdfDialog(((Button) jxAdd("btGeneratePdf").getNativeObject()).getRoot(), sessionHelper, decryptData, fileName);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (bos != null) {
								try {
									bos.close();
								} catch (Exception e) {
									e.printStackTrace();
								}
							}
						}
					}
				});
			}
			return(super.getColumnValueByName(p_v, p_name));
		}
		
		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			if(p_ctype != GIPI_CELL_MAPPED) {
				setDirtyFlag(true);
			}
		}
	}
	
	private void printLabels(String p_host, int p_port, List<Pair<String, Spinner>> compList) {
		int printCnt = 0;
		for (Pair<String, Spinner> pair : compList){
			try {
				Spinner spinner = pair.getValue();
				for (int i = 0; i < spinner.intValue(); i++) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
					printOneLabel(p_host, p_port, 
							getBr().getCell("bck_rg").getInt(), 
							(Integer)spinner.getAttribute("bcdt_rg"),
							getBr().getCell("bck_id").getString(), 
							getBr().getCell("bck_name").getString(), 
							sdf.format(getBr().getCell("bck_date").getDate()), 
							pair.getKey());
					printCnt++;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (printCnt > 0){
			ZkUtil.showMsg("%d label printed", printCnt);
		}
	}

	/***
	 * print one label to label printer TSC TE310 (2x1 inch label)
	 * support cjk character
	 * @param p_host - printip ip
	 * @param p_port -  printer port e.g. 9100
	 * @param p_bckrg -- for qr code
	 * @param p_docType - for qr code
	 * @param p_id  - desc
	 * @param p_name  - desc
	 * @param p_dateStr - desc
	 * @param p_docTypeName - desc
	 * @throws Exception
	 */
	public static ReturnMsg printOneLabel(String p_host, int p_port, int p_bckrg, int p_dtrg, String p_id, String p_name, String p_dateStr, String p_docTypeName) {
		Socket clientSocket = null;
		DataOutputStream os = null;
		try{
			UniLog.log1("print label start: %s %d", p_host, p_port);
			UniLog.log("printOneLabel: " + p_bckrg + "," + p_dtrg + "," + p_id + "," + p_name + "," + p_dateStr + "," + p_docTypeName);
			//TSC printer note: 300dpi 1mm 12dot, 200dpi 1mm 8dot
			if (StringUtils.isBlank(p_host) || p_port <= 0){
				UniLog.log1("Invalid printer configuration");
				return new ReturnMsg(false);
			}
			
			if (StringUtils.startsWith(p_host, "file:")){
				//os = new DataOutputStream(new FileOutputStream(p_host.substring(5)));
				os = new DataOutputStream(new FileOutputStream(p_host.substring(5) +"."+ new Date().getTime()));
				Thread.sleep(1);
			}
			else{
				clientSocket = new Socket(p_host, p_port);
				os = new DataOutputStream(clientSocket.getOutputStream());
			}

			os.writeBytes("SIZE 52.5 mm, 25.4 mm\n");
			os.writeBytes("GAP 3 mm, 0 mm\n");
			os.writeBytes("DIRECTION 0,0\n");
			os.writeBytes("REFERENCE 0,0\n");
			os.writeBytes("OFFSET 0 mm\n");
			os.writeBytes("SET PEEL OFF\n");
			os.writeBytes("SET CUTTER OFF\n");
			os.writeBytes("SET PARTIAL_CUTTER OFF\n");
			os.writeBytes("SET TEAR ON\n");
			os.writeBytes("CLS\n");
			os.writeBytes("CODEPAGE UTF-8\n");
			/*
			os.writeBytes(String.format("QRCODE 597,232,L,6,A,180,M2,S7,\"BCKRG%010dDTYPE%-30s\"\n", p_bckrg, p_docType));
			os.write(String.format("TEXT 433,249,\"ML.TTF\",180,10,10,\"%s\"\n",p_id).getBytes("UTF-8"));
			os.write(String.format("TEXT 433,201,\"ML.TTF\",180,10,10,\"%s\"\n",p_name).getBytes("UTF-8"));
			os.write(String.format("TEXT 433,152,\"ML.TTF\",180,10,10,\"%s\"\n",p_dateStr).getBytes("UTF-8"));
			os.write(String.format("TEXT 433,103,\"ML.TTF\",180,10,10,\"%s\"\n",p_docTypeName).getBytes("UTF-8"));
			*/
			//os.writeBytes(String.format("QRCODE 600,232,L,6,A,180,M2,S7,\"BCKRG%010dDTYPE%-30s\"\n", p_bckrg, p_docType));
			//os.writeBytes(String.format("QRCODE 600,232,L,6,A,180,M2,S7,\"BCKRG%010dBDTRG%010d\"\n", p_bckrg, p_dtrg));
			os.writeBytes(String.format("QRCODE 580,232,L,6,A,180,M2,S7,\"BCKRG%010dBDTRG%010d\"\n", p_bckrg, p_dtrg));
			os.write(String.format("TEXT 410,239,\"ML.TTF\",180,10,10,\"%s\"\n",p_id).getBytes("UTF-8"));
			os.write(String.format("TEXT 410,191,\"ML.TTF\",180,10,10,\"%s\"\n",p_name).getBytes("UTF-8"));
			os.write(String.format("TEXT 410,142,\"ML.TTF\",180,10,10,\"%s\"\n",p_dateStr).getBytes("UTF-8"));
			os.write(String.format("TEXT 410,93,\"ML.TTF\",180,10,10,\"%s\"\n",p_docTypeName).getBytes("UTF-8"));
			os.writeBytes("PRINT 1,1\n");

			os.flush();

			UniLog.log1("print label end");
			return new ReturnMsg(true);
		}
		catch(Exception ex){
			ex.printStackTrace();
			return new ReturnMsg(ex);
		}
		finally{
			try{
				if (os != null) os.close();
				if (clientSocket != null) clientSocket.close();
			}
			catch(Exception ex){}
		}
	}
	
	@Override
	protected ReturnMsg beforeAddLink(JxField fd,BiResult sr,CellCollection cl,int p_insIdx) {
		if(sr.getView().getName().equals("clinic.HealthQnrFiling")) {
			try{
				if (fd == null){ //data from dropzone
					cl.getCell("bckf_type").set("R"); //Report
				}
				else{  //data from add button
					cl.getCell("bckf_type").set("L"); //Leaflet
				}
				int seq = 0;
				int rowCount = sr.getRowCount();
				for (int i = 0; i < rowCount; i++) {
					CellCollection c = sr.getRowCollectionV(i);
					seq = Math.max(seq, c.getCell("bckf_seq").getInt());
				}
				cl.getCell("bckf_seq").set(++seq);
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
		}
		return(null);
	}
}