package com.uniinformation.jxapp.clinic;

import java.io.ByteArrayOutputStream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Div;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.clinic.BiResultPubDocType;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.CryptoUtil;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class PubDocType extends JxZkBiBase {
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		UniLog.log("pubdoctype bindCellCollection");
		try {
			BiResultPubDocType br = (BiResultPubDocType) p_br;
			br.setPdfData(null);
			br.setNeedRemovePdf(false);
			br.getCell("bcpdt_utime").set((int)(System.currentTimeMillis() / 1000));
			if (mode == JxZkBiBase.MODE_ADD){
				br.getCell("bcpdt_status").set("Y");
			}
		} catch (CellException e) {
			e.printStackTrace();
		}
	}
	@Override
	public void afterBind() {
		super.afterBind();
		UniLog.log("pubdoctype afterbind");
		//init dropzone
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
				String fileName = (String) filePair.getKey();
				byte[] fileData = (byte[]) filePair.getValue();
				byte[] encryptData = CryptoUtil.encrypt(sessionHelper.getAESKey(), fileData, null, true);
				if (encryptData == null) {
					Messagebox.show("encrypt data fail");
					return;
				}
				/*String key = String.format("zkbi_bodychk_pubdtype_%010d", getBr().getCell("bcpdt_rg"));
				UniLog.log("upload:" + key + ",name:" + fileName + ",dataSize:" + fileData.length + ",encryptSize:" + encryptData.length);
				ByteArrayInputStream bis = new ByteArrayInputStream(encryptData);
				FilingUtil.storeFile(sessionHelper.getAgent(), null, key, key, fileName, bis);
				bis.close();
				getBr().getCell("bcpdt_filkey").set(key); */
				getBr().getCell("bcpdt_docfname").set(fileName);
				BiResultPubDocType br1 = (BiResultPubDocType) getBr();
				br1.setPdfData(encryptData);
				br1.setNeedRemovePdf(false);
				setDirtyFlag(true);
			}
		});
		jxAdd("btRemovePdf").addActionListener(new JxActionListener(){
			@Override
			public void actionPerformed(JxField field) {
				try {
					BiResultPubDocType br1 = (BiResultPubDocType) getBr();
					br1.setPdfData(null);
					if (StringUtils.isNotBlank(getBr().getCell("bcpdt_filkey").getString())) {
						br1.setNeedRemovePdf(true);
						setDirtyFlag(true);
					}
					if (StringUtils.isNotBlank(getBr().getCell("bcpdt_docfname").getString())) {
						getBr().getCell("bcpdt_docfname").set("");
						setDirtyFlag(true);
					}
				} catch (CellException e) {
					e.printStackTrace();
				}
			}
		});
		jxAdd("btDownloadDoc").addActionListener(new JxActionListener(){
			@Override
			public void actionPerformed(JxField field) {
				try {
					String key = getBr().getCell("bcpdt_filkey").getString();
					String fileName = getBr().getCell("bcpdt_docfname").getString();
					if (StringUtils.isNotBlank(key)) {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						FilingUtil.getFile(sessionHelper.getAgent(), null, key, bos);
						bos.close();
						byte[] encryptData = bos.toByteArray();
						byte[] data = CryptoUtil.decrypt(sessionHelper.getAESKey(), encryptData, true);
						Filedownload.save(data, "application/pdf", fileName);
					} else
						Messagebox.show("document file not found");
				} catch (Exception e) {
					e.printStackTrace();
					Messagebox.show("download doc fail:" + e.toString());
				}
			}
		});
		LOCK_RECORD_FOR_UPDATE = true;
	}
}
