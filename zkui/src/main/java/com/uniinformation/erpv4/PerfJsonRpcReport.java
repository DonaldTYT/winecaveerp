package com.uniinformation.erpv4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Window;

import com.google.gson.JsonObject;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.GsonUtil;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class PerfJsonRpcReport extends PerfJsonRpcCall {
	protected boolean isShowPdfDialog = true;
	protected String chnftrInitStr = "";
	@Override
	public ReturnMsg processAction(String p_id, SessionHelper p_sh, CellCollection p_col, JsonObject p_actionData,InputStream p_upload,Component p_target) throws Exception {
		ReturnMsg rtnMsg = super.processAction(p_id,p_sh,p_col,p_actionData,p_upload,p_target);
		if(rtnMsg.getStatus()) {
			String fname = rtnMsg.getMsg();
			InputStream is = p_sh.newErpFileInputStream(fname);
			ChnftrParser ps = new ChnftrParser(is,chnftrInitStr);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ps.print(bos);
			String fileName = GsonUtil.getString(p_actionData, "fileName","");
			if (isShowPdfDialog) {
				UniLog.log1("show pdf dialog, download file name:%s", fileName);
				Collection<Component> comps = Executions.getCurrent().getDesktop().getComponents();
				for (Component comp : comps) {
					if (comp instanceof Window) {
						ZkUtil.showPdfDialog(comp, p_sh, bos.toByteArray(), fileName);
						break;
					}
				}
			} else {
				ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
				ZkUtil.printFromStream(bis, "application/pdf", p_sh);
			}
			bos.close();
		}
		return(rtnMsg);
	}
//	@Override
//	public ReturnMsg processAction(String p_id, SessionHelper p_sh,
//			CellCollection p_col) throws Exception {
//		ReturnMsg rtnMsg = super.processAction(p_id,p_sh,p_col);
//		if(rtnMsg.getStatus()) {
//			String fname = rtnMsg.getMsg().substring(4);
//			InputStream is = p_sh.newErpFileInputStream(fname);
//			ChnftrParser ps = new ChnftrParser(is,chnftrInitStr);
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			ps.print(bos);
//			ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
//			
//			String downloadLink = Sessions.getCurrent().getWebApp().getServletContext().getContextPath() + "/" + 
//					ZkUtil.getDownloadLinkFromStream(bis,
//							"application/pdf", 
//							p_sh, 
//							"JxZkTestEmbedPdf_stream",  //stream key
//							"JxZkTestEmbedPdf_mimetype",  //mime key
//							false);
//			String jsString = String.format("zkDisplayPdf('%s','%s','%s');", downloadLink,"pdfcontent", "btDownloadPdf");
//			UniLog.logm(this,"DEBUG:" + jsString);
//			Clients.evalJavaScript(jsString);
//		}
//		return(rtnMsg);
//	}

}
