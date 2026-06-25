package com.uniinformation.zkbi.erpv4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.XulElement;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiComposerReport;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerApproveQUO extends ZkBiComposerReport {
 	Button btApprove = new ZkBiButton("Approve");
    @Override
    public void buildBrowserWindow(final BiResult result,final Component comp, int p_sortIdx, boolean p_sortDesc){
    	super.buildBrowserWindow(result,comp, p_sortIdx, p_sortDesc);
    	Component pdfClose = comp.getFellow("btClosePdf");
    	if(pdfClose != null) {
    		pdfClose.addEventListener("onClick",	
    			 new EventListener() {
    	     		public void onEvent(Event event) throws Exception {
    	     			masterWin.getFellow("pdfview").setVisible(false);
    	     			showListPanel();
    	     		}
    			}
  	     	);
    		{
    			btApprove = new ZkBiButton("Approve");
    			Component parent = pdfClose.getParent();
    			btApprove.setId("btApprove");
    			btApprove.setParent(parent);
    	  		btApprove.addEventListener("onClick",	
    	    			 new EventListener() {
    	    	     		public void onEvent(Event event) throws Exception {
								result.getCell("inv_quostatus").set("Confirmed");
								ReturnMsg rtnMsg = result.updateCurrent();
								if(rtnMsg != null && !rtnMsg.getStatus()) {
									ZkBiMsgbox.show(rtnMsg.getMsg());
								} else {
									ZkBiMsgbox.show("Order Confirmed");
								}
    	    	     				
    	    	     			masterWin.getFellow("pdfview").setVisible(false);
    	    	     			showListPanel();
    	    	     			refresh(result,null);
    	    	     		}
    	    			}
    	  	     	);
    		}
    		
    	}
    }
    void openSelectedOrderForApprove(BiResult p_result) {
			SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
//	RpcClient rpc = p_result.getSelectUtil().getRpcClient();
			RpcClient rpc = sessionHelper.getRpcClient();
			Value val = rpc.callSegment("printer_autoselect",
						new VectorUtil()
						.addElement(1)
						.toVector()
					);
			//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
			val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(getSessionHelper().getWebContentRealPath("images", true)) .toVector());
			
			List<String> termsList = ChnftrParser.splitText(p_result.getCell("inv_term").getString().trim(), "helv_nr", "chinese", 10, 535);
			List<String> deliveryList = ChnftrParser.splitText(p_result.getCell("inv_quodeli").getString().trim(), "helv_nr", "chinese", 10, 535);
			List<String> remarkList = ChnftrParser.splitText(p_result.getCell("inv_remark").getString().trim(), "helv_nr", "chinese", 10, 715);
			val = rpc.callSegment("erpv4_print_quo",
						new VectorUtil()
						.addElement(p_result.getCell("inv_rg").getInt())
						.addElement(StringUtils.join(termsList, "\n"))
						.addElement(StringUtils.join(deliveryList, "\n"))
						.addElement(StringUtils.join(remarkList, "\n"))
						.addElement("CHNPRINT")
						.addElement("VARIABLE")
						.addElement("A4P")
						.addElement("NORMAL")
						.addElement("LPTRAW")
						.toVector()
					);
			rpc.close();
			if(val != null && val.toString().startsWith("OK")) {
				String fname = val.toString().substring(4);
				UniLog.log("Print quo got " + fname);
				try {
//			SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
					InputStream is = sessionHelper.newErpFileInputStream(fname);
					ChnftrParser ps = new ChnftrParser(is,"'");
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ps.print(bos);
					ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
					/*
					ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
					*/
					masterWin.getFellow("pdfview").setVisible(true);
					hideListPanel();
					String downloadLink = Sessions.getCurrent().getWebApp().getServletContext().getContextPath() + "/" + 
								ZkUtil.getDownloadLinkFromStream(bis,
										"application/pdf", 
										sessionHelper, 
										"JxZkTestEmbedPdf_stream",  //stream key
										"JxZkTestEmbedPdf_mimetype",  //mime key
										false);
						String jsString = String.format("zkDisplayPdf('%s','%s','%s');", downloadLink,"pdfcontent", "btDownloadPdf");
						UniLog.logm(this,"DEBUG:" + jsString);
						Clients.evalJavaScript(jsString);
							
					
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
    		if(p_result.getCellString("inv_quostatus").equals("ReqApprove")) {
    				btApprove.setDisabled(false);
    			} else {
    				btApprove.setDisabled(true);
    			}
    	
    }
	
    @Override
    public boolean doBrowseItemSelected(XulElement p_win, BiResult p_result)
    {
    	UniLog.log("doBrowseItemSelected do print quotation");
    	openSelectedOrderForApprove(p_result);
    	return(true);
    }
	@Override
	protected void doZkbiItemSelected(int p_idx,BiResult p_br) {
		if(!isMobile()) {
		try {
			if(p_idx >= 0) {
				p_br.fetchOneRecV(p_idx);
				openSelectedOrderForApprove(p_br);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		}
	}
}
