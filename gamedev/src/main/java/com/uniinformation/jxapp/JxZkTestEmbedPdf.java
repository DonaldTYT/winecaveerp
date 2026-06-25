package com.uniinformation.jxapp;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.uniinformation.jx.*;
import com.uniinformation.utils.*;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Window;


public class JxZkTestEmbedPdf extends JxZkBiBase {
	public void afterBind() {
		super.afterBind();
		
		
		//handle download action(optional)
		JxField jxDownloadPdf = addWithoutCheck("btDownloadPdf"); 
		new JxFieldAction("btDownloadPdf") {
			public void actionPerformed(JxField fd) {
				try{
					UniLog.logm(this, "event received");
					File file = new File(pdfPath());
					UniLog.logm(this,"haha download file:%s", file.getAbsolutePath());
					Filedownload.save(new FileInputStream(file), "application/pdf", "filename.pdf");  //it does not work on ios device, need investigate
					//Filedownload.save(new FileInputStream(file), "application/pdf", "abc.pdf");
					//Filedownload.save(new FileInputStream(file), null, "abc.pdf");
					//Filedownload.save(file, null);
				}
				catch(Exception ex){
					ex.printStackTrace();
				}
			}
		};
		
		//handle print action(optional)
		addWithoutCheck("btPrintPdf"); 
		new JxFieldAction("btPrintPdf") {
			public void actionPerformed(JxField fd) {
				UniLog.logm(this,"btPrintPdf got event");
				SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
				try {
					ZkUtil.printFromStream(new FileInputStream(pdfPath()), "application/pdf", sessionHelper);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		
		//handle close
		addWithoutCheck("btClose");
		new JxFieldAction("btClose") {
			public void actionPerformed(JxField fd) {
				UniLog.logm(this,"btClose got event");
			}
		};
		
		//display a pdf
		displayPdf(new File(pdfPath()), "pdf001", "btDownloadPdf");
		
	}
	private String pdfPath() {
		return Sessions.getCurrent().getWebApp().getRealPath("tmp/test.pdf");
	}
	private void displayPdf(File p_file, String p_pdfContainerId, String p_downloadBtId){
		//File file = new File(Sessions.getCurrent().getWebApp().getRealPath("tmp/test.pdf"));
		try{
			String downloadLink = Sessions.getCurrent().getWebApp().getServletContext().getContextPath() + "/" + 
					ZkUtil.getDownloadLinkFromStream(new FileInputStream(p_file),
							"application/pdf", 
							sessionHelper, 
							"JxZkTestEmbedPdf_stream",  //stream key
							"JxZkTestEmbedPdf_mimetype",  //mime key
							false);
			String jsString = String.format("zkDisplayPdf('%s','%s','%s');", downloadLink,p_pdfContainerId, p_downloadBtId);
			UniLog.logm(this,"DEBUG:" + jsString);
			Clients.evalJavaScript(jsString);
			
			
			/*
			//try to hide mobile url bar. fail!
			if (getSessionHelper().isMobileDevice()){
				Clients.scrollIntoView(btDownloadPdf);
				new ZkBiAbstractLongOp(btDownloadPdf, null){
					@Override
					public ReturnMsg longOp() {
						UniLog.logm(this,"haha: try to hide url");
						Clients.evalJavaScript("window.scrollTo(0,1);");
						return null;
					}
				};
			}
			*/
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
}
