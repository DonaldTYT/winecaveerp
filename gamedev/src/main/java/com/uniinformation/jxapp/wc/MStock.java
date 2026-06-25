package com.uniinformation.jxapp.wc;


import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.kyoko.common.DateUtil;
import com.kyoko.common.Sprintf;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.JxZkSkin;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.Base64Util;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.PhotoSwipeUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkObj;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.utils.ChnftrParser.ChnftrGetImageInterface;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.wc.PrintBarcode;

public class MStock extends JxZkBiBase {
	private static final int thumbnailMinWidth = 360;
	void saveImageFile( org.zkoss.util.media.Media media ) {
			RpcClient rpc = getRpcClient();
			Value v = rpc.callSegment("getFilingMessageId",new Vector());
			rpc.close();
			if(v != null && v.toString().startsWith("OK")) {
				int cc = Integer.parseInt(v.toString().substring(4));
				try  {
				    byte[] photoData = media.getByteData();
					Map<String, String> map = new HashMap<String, String>();
					photoData = rotatePhoto(photoData, map);
					String photoSize = map.get("data_size");

					//String filekey = new Sprintf("jxStockImageFiling_%06d").add(cc).toString();
					String filekey = String.format("jxStockImageFiling_%010d_%010d",getBr().getCellInt("st_irg"),cc);  //add stirg to key
					ByteArrayInputStream is = new ByteArrayInputStream(photoData);
				    FilingUtil.storeFile(
				      sessionHelper.getAgent(),
				      null,
				      filekey,
				      "",//mConditionPresetMapMap.customStoreName, 
				      "",//mConditionPresetMapMap.customStoreDesc, 
				    is);
				    is.close();

				    String sfilekey = storeThumbnal(getBr().getCellInt("st_irg"), cc, photoData, map);
					String thumbSize = map.get("data_size");
				    
				    TableRec tr = getBr().getSelectUtil().getQueryResult(
				         "select * from multidoc where mdoc_type = 'STIM' and mdoc_mrg = '" + getBr().getCell("st_irg").getInt() + "' order by mdoc_seq desc", null);
				    int seq = 0;
				    if(tr.getRecordCount() > 0) {
				        tr.setRecPointer(0);
				        seq = (Integer) tr.getField("mdoc_seq");
				        seq++;
				    }
				    getBr().getSelectUtil().executeUpdate("insert into multidoc (mdoc_type,mdoc_mrg,mdoc_seq,mdoc_drg,mdoc_ctime,mdoc_cuser,mdoc_doctype,mdoc_filekey,mdoc_sfilekey,mdoc_photosize,mdoc_thumbsize) values (?,?,?,?,?,?,?,?,?,?,?)", 
				        new Wherecl()
				            .appendArgument("STIM")
				            .appendArgument(getBr().getCell("st_irg").getInt())
				            .appendArgument(seq)
				            .appendArgument(cc)
				            .appendArgument(DateUtil.dateToUnixtime(new java.util.Date()))
				            .appendArgument(getLoginId())
				            .appendArgument(media.getContentType())
				            .appendArgument(filekey)
				            .appendArgument(sfilekey)
				            .appendArgument(photoSize)
				            .appendArgument(thumbSize)
				            );
				    
				    //patch extraimg field
				    RpcClient rpc2 = getRpcClient();
					rpc2.callSegment("updateStExtraImg", 
								new VectorUtil()
									.addElement(getBr().getCell("st_irg").getInt())
									.toVector()
							);
					rpc2.close();
					
				    getBr().refetchCurrent();
				    bindCellCollection(getBr(),curMode);
				    
				} catch (Exception ex) {
				    UniLog.log(ex);
				}
			}
	}
	@Override
	public void afterBind() {
//		IdSpace isp = (IdSpace) getNativeComponent();
//		Button btUpload = null;
//		if(!isp.hasFellow("btUpload",true)) {
//			if(isp.hasFellow("btClose",true)) {
//				Component ca = isp.getFellow("btClose",true);
//				btUpload = new ZkBiButton("Upload");
//				btUpload.setId("btUpload");
//				btUpload.setParent(ca.getParent());
//				((JxZkSkin) getSkin()).addOneElementToSkin(btUpload);
//			};
//			
//		} else {
//			btUpload = (Button) isp.getFellow("btUpload");
//		}
		if(addToolBarButton("btUpload","Upload","flaticon-dtmb-box-2")) {
			
		}
		if(addToolBarButton("btCamera","Camera","fa-camera")) {
			
		}

		if(addToolBarButton("btView","View Image","fa-picture-o")) {
			
		}
		super.afterBind();
		
		new JxFieldAction("btCamera") {
			public void actionPerformed(JxField fd){
				Clients.evalJavaScript("launchPhotoCapture()");
				UniLog.log("Camera Pressed");
				try {
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		};
		
		((Button)jxAdd("btCamera").getNativeObject()).addEventListener("onAddPhoto", new EventListener(){

			@Override
			public void onEvent(Event event) throws Exception {
				UniLog.log("onAddPhoto: " + event.getName() + " data.length():" + event.getData().toString().length());
				Object obj = new JSONTokener((String)event.getData()).nextValue();
				if (obj != null && obj instanceof JSONObject) {
					JSONObject jsonObj = (JSONObject) obj;
					String action = (String) jsonObj.get("action");
					String value = (String) jsonObj.get("value");
					String version = (String) jsonObj.get("version");
					File tmpFile = File.createTempFile("MStock", ".jpg", new File("/tmp")); //copy img to tmpfile for dev
					Base64Util.convertStringToOutputStream(value, new FileOutputStream(tmpFile));
                	saveImageFile(new AImage(tmpFile));
					UniLog.log1("log to file %s", tmpFile.getAbsolutePath());
				}
			}
		});
		new JxFieldAction("btUpload") {
			public void actionPerformed(JxField fd){
					UniLog.log("upload Pressed");
					try {
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
				                	saveImageFile(media );
//				                	RpcClient rpc = getRpcClient();
//				                	Value v = rpc.callSegment("getFilingMessageId",new Vector());
//				                	rpc.close();
//				                	if(v != null && v.toString().startsWith("OK")) {
//				                		int cc = Integer.parseInt(v.toString().substring(4));
//				                		try  {
//				                			InputStream is = media.getStreamData();
//				                			FilingUtil.storeFile(
//				                					sessionHelper.getAgent(),
//				                					null,
////				                					new Sprintf("jxAfsServiceOrderFiling_%06d").add(cc).toString(),
//				                					new Sprintf("jxStockImageFiling_%06d").add(cc).toString(),
//				                					"",//mConditionPresetMapMap.customStoreName, 
//				                					"",//mConditionPresetMapMap.customStoreDesc, 
//				                					is);
//				                		
//				                			is.close();
//				                			TableRec tr = getBr().getSelectUtil().getQueryResult(
//				                					"select * from multidoc where mdoc_type = 'STIM' and mdoc_mrg = '" + getBr().getCell("st_irg").getInt() + "' order by mdoc_seq desc", null);
//				                			int seq = 0;
//				                			if(tr.getRecordCount() > 0) {
//				                				tr.setRecPointer(0);
//				                				seq = (Integer) tr.getField("mdoc_seq");
//				                				seq++;
//				                			}
//				                			getBr().getSelectUtil().executeUpdate("insert into multidoc (mdoc_type,mdoc_mrg,mdoc_seq,mdoc_drg,mdoc_ctime,mdoc_cuser,mdoc_doctype) values (?,?,?,?,?,?,?)", 
//				                					new Wherecl()
//				                						.appendArgument("STIM")
//				                						.appendArgument(getBr().getCell("st_irg").getInt())
//				                						.appendArgument(seq)
//				                						.appendArgument(cc)
//				                						.appendArgument(DateUtil.dateToUnixtime(new java.util.Date()))
//				                						.appendArgument(getLoginId())
//				                						.appendArgument(media.getContentType())
//				                					);
////				                			col.getCell("ind_messrg").set(cc);
////				                			col.getCell("ind_messagetype").set(media.getContentType());
//				                			getBr().refetchCurrent();
//				                			bindCellCollection(getBr(),curMode);
//				                		} catch (Exception ex) {
//				                			UniLog.log(ex);
//				                		}
//				                	}
				                }
				    		}
					    });
					} catch (Exception ex) {
							UniLog.log(ex);
					}
			}
		};
		new JxFieldAction("btClosePdf") {
			public void actionPerformed(JxField fd){
				UniLog.log("cloase view Pressed");
				jxAdd("pdfview").setVisible(false);
			}
		};	
		new JxFieldAction("btView") {
			public void actionPerformed(JxField fd){
				UniLog.log("view Pressed");
				/*RpcClient rpc = getRpcClient();
				Value val = rpc.callSegment("printer_autoselect",
						new VectorUtil()
						.addElement(1)
						.toVector()
				);
//				val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(ZkUtil.getWebContentRealPath("images", true)) .toVector());
				val = rpc.callSegment("erpv3_print_images",
						new VectorUtil()
						.addElement("STIM")
						.addElement(getBr().getCell("st_irg").getInt())
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
					UniLog.log("Print service order" + fname);
					try {
						InputStream is = erpFileInputStream(fname);
						ChnftrParser ps = new ChnftrParser(is,"'");
						ps.setChnftrGetImageInterface(new ChnftrGetImageInterface(){
						@Override
						public byte[] getImage(String p_key) {
							try{
								ByteArrayOutputStream bos = new ByteArrayOutputStream();
								FilingUtil.getFile(sessionHelper.getAgent(), null, p_key, bos);
								byte[] bytes = bos.toByteArray();
								bos.close();
								return(bytes);
							}
							catch(Exception ex){
								ex.printStackTrace();
								return(null);
							}
						}});
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ps.print(bos);
						ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
						
						jxAdd("pdfview").setVisible(true);
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
				}*/
				List<Map<String, String>> list = new ArrayList<Map<String, String>>();
				BiResult sr = getBr().getSubLink("wc.StockImgs");
				HttpServletRequest request = (HttpServletRequest)Executions.getCurrent().getNativeRequest();
				boolean updateTableFlag = false;
				for (int i = 0; i < sr.getRowCount(); i++) {
					try {
						Map<String, String> map = new HashMap<String, String>();
						CellCollection col = sr.getRowCollectionV(i);
						int mrg = col.getCell("mdoc_mrg").getInt();
						int drg = col.getCell("mdoc_drg").getInt();
						String filekey = col.getCell("mdoc_filekey").getString();
						String sfilekey = col.getCell("mdoc_sfilekey").getString();
						String photoSize = col.getCell("mdoc_photosize").getString();
						String thumbSize = col.getCell("mdoc_thumbsize").getString();

						byte[] photoData = null;
						//get photo size
						if (StringUtils.isBlank(photoSize)) {
							photoData = getPhotoData(filekey);
							photoSize = getPhotoSize(photoData);
							updateTableFlag = true;
						}
						//extract thumbnail
						if (StringUtils.isBlank(sfilekey)) {
							if (photoData == null)
								photoData = getPhotoData(filekey);
							Map<String, String> photoAttrMap = new HashMap<String, String>();
							sfilekey = storeThumbnal(mrg, drg, photoData, photoAttrMap);
							thumbSize = photoAttrMap.get("data_size");
							updateTableFlag = true;
						}
						//get thumbnail size
						if (StringUtils.isBlank(thumbSize)) {
							byte[] thumbData = getPhotoData(sfilekey);
							thumbSize = getPhotoSize(thumbData);
							updateTableFlag = true;
						}
						//update table
						if (updateTableFlag) {
							getBr().getSelectUtil().executeUpdate("update multidoc set mdoc_filekey=?,mdoc_photosize=?,mdoc_sfilekey=?,mdoc_thumbsize=? where mdoc_mrg=? and mdoc_drg=?", 
									new Wherecl()
				       	    	.appendArgument(filekey)
				       	    	.appendArgument(photoSize)
				       	    	.appendArgument(sfilekey)
				       	    	.appendArgument(thumbSize)
				       	    	.appendArgument(mrg)
				       	    	.appendArgument(drg)
				           	);
						}
						map.put("photoSrc", String.format("fileloader/%s", filekey));
						map.put("photoDataSize", photoSize);
						map.put("thumbnailSrc", String.format("fileloader/%s", sfilekey));
						map.put("thumbnailDataSize", thumbSize);
						list.add(map);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (updateTableFlag) {
					getBr().refetchCurrent();
					bindCellCollection(getBr(),curMode);
				}
				jxAdd("photoview").setVisible(true);
				PhotoSwipeUtil.loadPhoto(ZkObj.toDiv(addWithoutCheck("photoview")),list);
			}
		};		
		
		if(getSessionHelper().isMobileDevice()) {
			jxAdd("list_wc_Stmovd").setVisible(false);
		} else {
		}
	}
	
	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		
		//handle Download button
		return ListUtil.of(
			new BiGetItemProperty(p_br.getSubLink("wc.StockImgs")) {
				@Override
				public void onValueChanged(Object p_value,int p_ctype) {
					ColumnCell bcc = (ColumnCell) p_value;
					if(p_ctype != BiGetItemProperty.GIPI_VALUE_CHANGED ) {
						if(bcc.getCellLabel().equals("mdoc_brandimg")) {
							setDirtyFlag(true);
						}
					}
					if(p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED && bcc.getCellLabel().equals("mdoc_download")){
						UniLog.log1("%s clicked", bcc.getCellLabel());
						ZkUtil.downloadFileFromFiling(sessionHelper,bcc.getCollection().getCell("mdoc_filekey").getString(), null);
					}
				}
			},

			new BiGetItemProperty(p_br.getSubLink("wc.locbinstatus")) {
				@Override
				public void onValueChanged(Object p_value,int p_ctype) {
					ColumnCell bcc = (ColumnCell) p_value;
					if(p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED && bcc.getCellLabel().equals("pdlbs_printbc")){
						UniLog.log1("%s clicked", bcc.getCellLabel());
						try {
							PrintBarcode pbc = new PrintBarcode("PTR01");
							pbc.printOne(PrintBarcode.LABEL_TYPE.STOCK_ITEM, bcc.getCollection(), 1, true);
							pbc.close();
							ZkUtil.showMsg("Label " + bcc.getCollection().getCellString("st_icode")+" Printed");
						} catch (Exception ex) {
							UniLog.log(ex);
							messageBox(ex.toString());
						}
					}
					if(p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED && bcc.getCellLabel().equals("pdlbs_printbc2")){
						UniLog.log1("%s clicked", bcc.getCellLabel());
						try {
							PrintBarcode pbc = new PrintBarcode("PTR02");
							pbc.printOne(PrintBarcode.LABEL_TYPE.STOCK_ITEM, bcc.getCollection(), 1, true);
							pbc.close();
							ZkUtil.showMsg("Label " + bcc.getCollection().getCellString("st_icode")+" Printed");
						} catch (Exception ex) {
							UniLog.log(ex);
							messageBox(ex.toString());
						}
					}
				}
			}
		);	
		
	}

	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br,mode);
		jxSetVisible("pdfview",false);
		jxSetVisible("photoview",false);
		if(getSessionHelper().isMobileDevice()) {
		} else {
			JxField sv = jxAdd("list_"+JxZkBiBase.replaceViewName("wc_Stmovd"));
			sv.setAttribute("showfilter","");
			Listbox lb = (Listbox) sv.getNativeObject();
			lb.setHeight("400px");
			ListModelList lm = (ListModelList) lb.getListModel();
			int n = lm.getSize();
			Listitem li = lb.getItemAtIndex(n-1);
			Clients.scrollIntoView(li);
		}
	}
	@Override 
	protected void afterDeleteLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals("wc.StockImgs")) {
			try {
				CellCollection col = sr.getRowCollectionV(idx);
				String fileName = col.getCell("mdoc_filekey").getString();
				if (StringUtils.isNotBlank(fileName)) {
					int delCount = FilingUtil.deleteFile(sessionHelper.getAgent(), null, fileName, FilingUtil.VER_ALL);
					UniLog.log("deleted file " + fileName + ",delete count:" + delCount);
				}
				fileName = col.getCell("mdoc_sfilekey").getString();
				if (StringUtils.isNotBlank(fileName)) {
					int delCount = FilingUtil.deleteFile(sessionHelper.getAgent(), null, fileName, FilingUtil.VER_ALL);
					UniLog.log("deleted file " + fileName + ",delete count:" + delCount);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static byte[] rotatePhoto(byte[] photoData, Map<String, String> photoAttrMap) throws Exception {
		ByteArrayOutputStream bos = null;
		ImageOutputStream ios = null;
		try {
			int degree = readPhotoDegree(photoData);
			if (degree != 0) {
				ByteArrayInputStream is = new ByteArrayInputStream(photoData);
				BufferedImage img = ImageIO.read(is);
				int sWidth = img.getWidth();
				int sHeight = img.getHeight();
				int dWidth = sWidth;
				int dHeight = sHeight;
				if (degree == 90 || degree == 270) {
					dWidth = sHeight;
					dHeight = sWidth;
				}
				BufferedImage dimg = new BufferedImage(dWidth, dHeight, img.getType());
				Graphics2D g = dimg.createGraphics();
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				AffineTransform tf = new AffineTransform();
				tf.translate((dWidth - sWidth) / 2, (dHeight - sHeight) / 2);
				tf.rotate(Math.toRadians(degree), sWidth / 2, sHeight / 2);
				g.drawImage(img, tf, null);
				g.dispose();
				is.close();

				bos = new ByteArrayOutputStream();
				ios = ImageIO.createImageOutputStream(bos);
				ImageWriter imgWriter = ImageIO.getImageWritersByFormatName("jpeg").next();
				imgWriter.setOutput(ios);
				ImageWriteParam param = imgWriter.getDefaultWriteParam(); 
				param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT); 
				param.setCompressionQuality(1f);
				imgWriter.write(dimg);
				byte[] data = bos.toByteArray();
				if (data != null && data.length > 0) {
					if (photoAttrMap != null)
						photoAttrMap.put("data_size", getPhotoSize(data));
					return data;
				}
				/*if (ImageIO.write(dimg, "jpg", bos)) {
					byte[] data = bos.toByteArray();
					if (photoAttrMap != null)
						photoAttrMap.put("data_size", getPhotoSize(data));
					return data;
				}*/
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (bos != null)
					bos.close();
				if (ios != null)
					ios.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (photoAttrMap != null)
			photoAttrMap.put("data_size", getPhotoSize(photoData));
		return photoData;
	}
	private byte[] getPhotoData(String filekey) throws Exception {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		FilingUtil.getFile(sessionHelper.getAgent(), null, filekey, bos);
		bos.close();
		return bos.toByteArray();
	}
	private static String getPhotoSize(byte[] photoData) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(photoData);
	    BufferedImage img = ImageIO.read(is);
	    is.close();
	    return img.getWidth() + "x" + img.getHeight();
	}
	private synchronized String storeThumbnal(int mrg, int drg, byte[] photoData, Map<String, String> photoAttrMap) throws Exception {
		ByteArrayInputStream is = new ByteArrayInputStream(photoData);
		String sfilekey = String.format("jxStockImageFiling_S_%010d_%010d",mrg,drg);  
	    BufferedImage img = ImageIO.read(is);
	    is.close();
		float ratio = (float)thumbnailMinWidth / Math.min(img.getWidth(), img.getHeight());
		int newWidth = (int)(ratio * img.getWidth());
		int newHeight = (int)(ratio * img.getHeight());
		BufferedImage dimg = new BufferedImage(newWidth, newHeight, img.getType());
		Graphics2D g = dimg.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.drawImage(img, 0, 0, newWidth, newHeight, 0, 0, img.getWidth(), img.getHeight(), null);
		g.dispose();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ImageIO.write(dimg, "jpg", bos);
		bos.close();
		is = new ByteArrayInputStream(bos.toByteArray());
		FilingUtil.storeFile(
		   sessionHelper.getAgent(),
		   null,
		   sfilekey,
		   "",
		   "",
		   is);
		is.close();
		if (photoAttrMap != null)
			photoAttrMap.put("data_size", String.format("%dx%d", newWidth, newHeight));
		return sfilekey;
	}
	private static int readPhotoDegree(byte[] photoData) {
		int degree = 0;
		BufferedInputStream bis = null;
		try {
			bis = new BufferedInputStream(new ByteArrayInputStream(photoData));
			FileType fileType = FileTypeDetector.detectFileType(bis);
			UniLog.log1("readPhotoDegree filetype:" + fileType);
			if (fileType == FileType.Jpeg){
				Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(photoData));
				ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
				int orientation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
				switch (orientation)
				{
				case 3:
				case 4:
					degree = 180; 
					break;
				case 5:
				case 6:
					degree = 90;
					break;
				case 7:
				case 8:
					degree = 270;
					break;
				default:
					degree = 0;
					break;
				}
			}
		} catch (Exception e) {
			//e.printStackTrace(); //andrew201218: avoid no exif exception
		} finally {
			if (bis != null) {
				try {
					bis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		UniLog.log1("readPhotoDegree:" + degree);
		return degree;
	}
}
