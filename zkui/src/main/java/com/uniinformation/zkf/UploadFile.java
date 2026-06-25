package com.uniinformation.zkf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Window;

import com.google.gson.JsonObject;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.ChnftrParser.ChnftrGetImageInterface;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.webcore.SessionHelper;

public class UploadFile implements com.uniinformation.zkf.ZkfAction{
	protected String chnftrInitStr = "";
	protected boolean isShowPdfDialog = true;
	@Override
	public ReturnMsg processAction(String p_id, final SessionHelper p_sh, CellCollection p_col, JsonObject p_actionData, InputStream p_upload,Component p_target) throws Exception {
		// TODO Auto-generated method stub
		if(p_id.equals("uploadChnftr"))  {
//			ChnftrParser ps = new ChnftrParser(p_upload,chnftrInitStr);
//			ps.setChnftrGetImageInterface(new ChnftrGetImageInterface(){
//				@Override
//				public byte[] getImage(String p_key) {
//					InputStream is = null;
//					try {
//						int readLen;
//						int bufLen = 1024;
//						byte[] buf = new byte[bufLen];
//						is = p_sh.newErpFileInputStream(p_key);
//						ByteArrayOutputStream bos = new ByteArrayOutputStream();
//						while ((readLen = is.read(buf, 0, bufLen)) != -1)
//			                 bos.write(buf, 0, readLen);
//						bos.close();
//			            return bos.toByteArray();
//					} catch (Exception ex) {
//						UniLog.log(ex);
//						return(null);
//					} finally {
//						if(is != null) {
//							try {
//								is.close();
//							} catch (Exception ex) {
//								UniLog.log(ex);
//							}
//							is = null;
//						}
//					}
//					
//				}});
//			ps.setUseGetImageInterfaceByDefault(true);
//			ByteArrayOutputStream bos = new ByteArrayOutputStream();
//			ps.print(bos);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ChnftrRpcServlet.streamChnftrToPdf(p_upload, bos, p_sh);
			String fileName = "upload.pdf";
			if (isShowPdfDialog) {
				UniLog.log1("show pdf dialog, download file name:%s", fileName);
				for (Component root = p_target;root != null;root = root.getParent()) {
					if (root instanceof Window) {
						ZkUtil.showPdfDialog(root, p_sh, bos.toByteArray(), fileName);
						break;
					}
				}
				/*
				Collection<Component> comps = Executions.getCurrent().getDesktop().getComponents();
				for (Component comp : comps) {
					if (comp instanceof Window) {
						ZkUtil.showPdfDialog(comp, p_sh, bos.toByteArray(), fileName);
						break;
					}
				}
				*/
			} else {
				ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
				ZkUtil.printFromStream(bis, "application/pdf", p_sh);
			}
			bos.close();
			return(ReturnMsg.defaultOk);
		}
		if(p_id.equals("uploadPOL"))  {
			return(ReturnMsg.defaultOk);
		}
		if(p_id.equals("uploadCVP"))  {
			return(ReturnMsg.defaultOk);
		}
		if(p_id.equals("uploadSTA"))  {
			return(ReturnMsg.defaultOk);
		}
		if(p_id.equals("uploadER2"))  {
			return(ReturnMsg.defaultOk);
		}
		return null;
		
	}
}
