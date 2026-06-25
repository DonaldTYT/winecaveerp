package com.uniinformation.dynamic.aw;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Fileupload;

import com.kyoko.common.Sprintf;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultQuoDet;
import com.uniinformation.bicore.hw.BiResultHwQuoDet;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.jxapp.erpv4.DO;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.ZkBiGetItemProperty;

public class artwayDO extends DO {
	class ArtwayDoGetItemProperty extends DoGetItemProperty {
		public ArtwayDoGetItemProperty(BiResult p_br, JxZkBiBase p_bibase) {
			super(p_br, p_bibase);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Object getColumnValueByName(final Object p_v,String p_name) {
			Object o = bigibr.getTrStatObj(p_v);
			final CellCollection col = bigibr.getRowCollectionO(o);
			if(p_name.equals("btDpicUpload")) {
				return(new JxActionListener() {
					public void actionPerformed(JxField fd){
						if (!checkBr()) return;
						UniLog.log("btDpicUpload Pressed for "+ p_v);
						
					    Fileupload.get(new EventListener <UploadEvent>(){
				    		public void onEvent(UploadEvent event) {
				        		UniLog.log("upload event catched");
				        		SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper((HttpServletRequest) Executions.getCurrent().getNativeRequest() , (HttpServletResponse) Executions.getCurrent().getNativeResponse());
				                org.zkoss.util.media.Media media = event.getMedia();
				                if(media != null) {
				                	if(!media.getContentType().equals("image/jpeg") &&
				                	   !media.getContentType().equals("image/png")) {
				                		messageBox("Only Jpeg/Png Image File Are Accepted");
				                		return;
				                	}
				                	RpcClient rpc = getRpcClient();
				                	Value v = rpc.callSegment("getFilingMessageId",new Vector());
				                	String filingTable = Erpv4Config.getString(getSessionHelper(), "FilingAttachmentTable");
				                	rpc.close();
				                	if(v != null && v.toString().startsWith("OK")) {
				                		int cc = Integer.parseInt(v.toString().substring(4));
				                		try  {
				                			byte[] data = media.getByteData();
				                			//InputStream is = media.getStreamData();
				                			InputStream is = new ByteArrayInputStream(data);
				                			FilingUtil.storeFile(
				                					sessionHelper.getAgent(),
				                					filingTable,
				                					new Sprintf("FilingDoDetPic_%08d").add(cc).toString(),
				                					"",//mConditionPresetMapMap.customStoreName, 
				                					"",//mConditionPresetMapMap.customStoreDesc, 
				                					is);
				                			is.close();
				                				is = new ByteArrayInputStream(data);
				                				BufferedImage image = ImageIO.read(is);
				                				int imgWidth = image.getWidth();
				                				int imgHeight = image.getHeight();
				                				is.close();
				                				if(imgHeight > 0.0f) {
				                					float aspect = (float)imgWidth / imgHeight;
				                					UniLog.log("image aspect:" + aspect + ",width:" + imgWidth + ",height:" + imgHeight);
				                					col.getCell("stmd_fref2").set(aspect);
				                				} else {
				                					col.getCell("stmd_fref2").set(1.0f);
				                				}
				                			col.getCell("stmd_serialno").set(cc);
				                			/*
				                			col.getCell("ind_messagetype").set(media.getContentType());
				                			*/
											setDirtyFlag(true);
				                		} catch (Exception ex) {
				                			UniLog.log(ex);
				                		}
				                	}
				                }
				    		}
					    });						
						
					}
				}
				);
			}
			if(p_name.equals("btDpicDownload")) {
				return(new JxActionListener() {
					public void actionPerformed(JxField fd){
						if (!checkBr()) return;
						UniLog.log("btDpicDownload Pressed for "+ p_v);
					}
				}
				);
			}
			if(p_name.equals("btDpicRemove")) {
				if( col.getCell("stmd_serialno").getInt() <= 0) return(null);
				return(new JxActionListener() {
					public void actionPerformed(JxField fd){
						if (!checkBr()) return;
						UniLog.log("btDpicRemove Pressed for "+ p_v);
						try {
							//FilingUtil.deleteFile(sessionHelper.getAgent(), null, 
							//		String.format("jxHwQuoDetFiling_%06d", col.getCell("ind_messrg").getInt()));
							col.getCell("stmd_serialno").set(0);
                			col.getCell("stmd_fref2").set(0f);
							setDirtyFlag(true);
						} catch (Exception e) {
							e.printStackTrace();
						}	
					}
				}
				);
			}
			return(super.getColumnValueByName(p_v, p_name));
		}
	}
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		if(getGipi("erpv4.DoDet") == null) {
			setGipi("erpv4.DoDet",new ArtwayDoGetItemProperty(br.getSubLink("erpv4.DoDet"),this));	
		}
		super.bindCellCollection(br, mode);
	}
}
