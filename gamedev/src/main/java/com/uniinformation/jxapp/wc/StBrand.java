package com.uniinformation.jxapp.wc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Html;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.ImageUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class StBrand extends JxZkBiBase {
	@Override 
	public void afterBind() {
		if(!addToolBarButton("btCopyImg","Copy Image","flaticon-dtmb-box-2")) {
		}
		if(!addToolBarButton("btUploadImg","Upload Image","flaticon-dtmb-box-2")) {
		}
		super.afterBind();
		new JxFieldAction("btCopyImg") {
			public void actionPerformed(JxField fd){
                	try {
                		getBr().beginWork();
                		ReturnMsg rtn = copyImageFromStockImages(getBr());
                		getBr().commitWork();
                	} catch (Exception ex) {
                		UniLog.log(ex);
                	}
			        getBr().refetchCurrent();
			        bindCellCollection(getBr(),curMode);
			}
		};
		new JxFieldAction("btUploadImg") {
			public void actionPerformed(JxField fd){
				UniLog.log("upload Pressed");
				try {
				    Fileupload.get(new EventListener <UploadEvent>(){
			    		public void onEvent(UploadEvent event) {
			        		UniLog.log("upload event catched");
			                org.zkoss.util.media.Media media = event.getMedia();
			                if(media != null) {
			                	if(!media.getContentType().equals("image/jpeg") &&
			                	   !media.getContentType().equals("image/png")) {
			                		messageBox("Only Jpeg/Png Image File Are Accepted");
			                		return;
			                	}
			                	try {
			                		getBr().beginWork();
			                		saveImageFile(getBr(),media.getByteData(),media.getContentType());
			                		getBr().commitWork();
			                	} catch (Exception ex) {
			                		UniLog.log(ex);
			                	}
			                	getBr().refetchCurrent();
			                	bindCellCollection(getBr(),curMode);
				    
			                }
			    		}
				    });
				} catch (Exception ex) {
						UniLog.log(ex);
				}
			}
		};
	}

	static void saveImageFile( BiResult p_br,byte[] p_data,String p_doctype) {
			RpcClient rpc = p_br.getSelectUtil().getRpcClient();
			Value v = rpc.callSegment("getFilingMessageId",new Vector());
			if(v != null && v.toString().startsWith("OK")) {
				int cc = Integer.parseInt(v.toString().substring(4));
				try  {
				    byte[] photoData = p_data;
					Map<String, String> map = new HashMap<String, String>();
					photoData = ZkUtil.rotatePhoto(photoData, map);
					String photoSize = map.get("data_size");

					String filekey = String.format("jxBrandImageFiling_%s",p_br.getCellString("mdoc_type"));  //add stirg to key
					ByteArrayInputStream is = new ByteArrayInputStream(photoData);
				    FilingUtil.storeFile(
				      p_br.getSessionHelper().getAgent(),
				      null,
				      filekey,
				      "",//mConditionPresetMapMap.customStoreName, 
				      "",//mConditionPresetMapMap.customStoreDesc, 
				    is);
				    is.close();
				    photoData = ZkUtil.storeThumbnal(photoData, map);
					String thumbSize = map.get("data_size");

					String sfilekey = String.format("jxBrandImageFiling_S_%s",p_br.getCellString("mdoc_type"));  
					is = new ByteArrayInputStream(photoData);
					FilingUtil.storeFile(
							p_br.getSessionHelper().getAgent(),
							null,
							sfilekey,
							"",
							"",
					is);
					is.close();

				    p_br.getCell("mdoc_ctime").set(DateUtil.dateToUnixtime(new java.util.Date()));
				    p_br.getCell("mdoc_cuser").set(p_br.getSessionHelper().getLoginId());
				    p_br.getCell("mdoc_drg").set(cc);
				    p_br.getCell("mdoc_doctype").set(p_doctype);
				    p_br.getCell("mdoc_filekey").set(filekey);
				    p_br.getCell("mdoc_sfilekey").set(sfilekey);
				    p_br.getCell("mdoc_photosize").set(photoSize);
				    p_br.getCell("mdoc_thumbsize").set(thumbSize);
				    p_br.updateCurrent();
				} catch (Exception ex) {
				    UniLog.log(ex);
				}
			}
	}
	static public ReturnMsg copyImageFromStockImages(BiResult p_br) throws Exception {
		TableRec tr;
			SelectUtil su = p_br.getSelectUtil();
			tr = su.getQueryResult("select * from multidoc,stock where mdoc_type = 'STIM' and mdoc_mrg = st_irg and st_mbrand = '"+p_br.getCellString("stbd_code") + "' order by mdoc_mrg, mdoc_seq");
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				FilingUtil.getFile(p_br.getSessionHelper().getAgent(), null,tr.getFieldString("mdoc_filekey"), bos);
				byte[] data = bos.toByteArray();
				bos.close();
				saveImageFile( p_br,data,tr.getFieldString("mdoc_doctype"));
			}
		return(ReturnMsg.defaultOk);
	}
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		super.bindCellCollection(br, mode);
		JxField imgfd = jxAdd("bd_image");
		if(imgfd != null) {
			Html comp = (Html) imgfd.getNativeObject();
			String imgKey = br.getCellString("mdoc_filekey");
			if(imgKey != null && !imgKey.equals("")) {
			try{
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				FilingUtil.getFile(getSessionHelper().getAgent(), null, imgKey, bos);
				String base64Img = null;
				bos.close();
				//TODO: obtain image
//				base64Img = ImageUtil.getBase64ImageString(new FileInputStream(ZkUtil.getWebContentRealPath("/images/logo/banner_hw.jpg",false)),"image/jpeg");
				ByteArrayInputStream ios = new ByteArrayInputStream(bos.toByteArray());
				base64Img = ImageUtil.getBase64ImageString(ios ,"image/jpeg");
				ios.close();
				if (base64Img != null){
					comp.setContent( "<img src=\""+ base64Img + "\" style=\"max-width:300px; max-height:300px;\" alt=\"Image\" />");
				} else {
					comp.setContent("");
				}
			}
			catch(Exception ex){
				comp.setContent("");
				ex.printStackTrace();
			}
				
			} else {
				comp.setContent("");
			}
		}
	}
}
