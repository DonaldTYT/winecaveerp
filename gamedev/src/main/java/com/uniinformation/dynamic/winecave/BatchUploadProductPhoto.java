package com.uniinformation.dynamic.winecave;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import org.json.JSONObject;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zul.Fileupload;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;

public class BatchUploadProductPhoto  extends BiActionHandler{
	ArrayList<Integer> recs = new ArrayList<Integer>();
	
	final int MAXIMGSIZE=800;
	
	
	private static BufferedImage applyExifOrientation(BufferedImage src, int orientation) {
	    int width = src.getWidth();
	    int height = src.getHeight();

	    AffineTransform tx = new AffineTransform();
	    int newWidth = width;
	    int newHeight = height;

	    switch (orientation) {
	        case 1: // normal
	            return src;

	        case 2: // flip horizontal
	            tx.scale(-1, 1);
	            tx.translate(-width, 0);
	            break;

	        case 3: // rotate 180
	            tx.translate(width, height);
	            tx.rotate(Math.PI);
	            break;

	        case 4: // flip vertical
	            tx.scale(1, -1);
	            tx.translate(0, -height);
	            break;

	        case 5: // transpose
	            tx.rotate(Math.PI / 2);
	            tx.scale(1, -1);
	            newWidth = height;
	            newHeight = width;
	            break;

	        case 6: // rotate 90 CW
	            tx.translate(height, 0);
	            tx.rotate(Math.PI / 2);
	            newWidth = height;
	            newHeight = width;
	            break;

	        case 7: // transverse
	            tx.translate(height, 0);
	            tx.rotate(Math.PI / 2);
	            tx.scale(-1, 1);
	            newWidth = height;
	            newHeight = width;
	            break;

	        case 8: // rotate 270 CW / 90 CCW
	            tx.translate(0, width);
	            tx.rotate(-Math.PI / 2);
	            newWidth = height;
	            newHeight = width;
	            break;

	        default:
	            return src;
	    }

	    BufferedImage dst = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
	    Graphics2D g2 = dst.createGraphics();
	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
	    g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	    g2.drawImage(src, tx, null);
	    g2.dispose();

	    return dst;
	}
	

	public BatchUploadProductPhoto(ZkBiComposerBase p_bibase) {
		super(p_bibase);
	}
	static void copy(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[8192];   // 8 KB buffer
	    int bytesRead;
	    while ((bytesRead = in.read(buffer)) != -1) {
	        out.write(buffer, 0, bytesRead);
	    }
	}
	@Override
	public ReturnMsg beforeAction(BiResult p_result, int cnt) {
		recs.clear();
		return null;
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		recs.add(p_recIdx);
		return (ReturnMsg.defaultOk);
	}

	@Override
	public ReturnMsg afterAction(BiResult p_result) {
		// TODO Auto-generated method stub
		try {
		    Fileupload.get(new EventListener <UploadEvent>(){
	    		public void onEvent(UploadEvent event) throws Exception {
	        		UniLog.log("upload event catched");
//	        		SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper((HttpServletRequest) Executions.getCurrent().getNativeRequest() , (HttpServletResponse) Executions.getCurrent().getNativeResponse());
	                org.zkoss.util.media.Media media = event.getMedia();
	                if(media != null) {
	                	JSONObject jo = new JSONObject();
	                	/*
	                	jo.put("contentType", media.getContentType());
	                	jo.put("fileName", media.getName());
	                	jo.put("format",media.getFormat());
	                	*/
	                	String contentType = media.getContentType();
	                	String format = media.getFormat();
	                	int messageRg=0;
	                	if(
	                			"image/jpeg".equals(contentType) &&
	                			"jpeg".equals(format) ) {
	                			RpcClient rpc = p_result.getSessionHelper().getRpcClient();
	                			Value v = rpc.callSegment("pdphoto_createMessage",
	                					new VectorUtil()
	                						.addElement("jpg")
	                						.toVector()
	                					);
	                			rpc.close();
	                			if(v != null && v.toString().startsWith("OK  ")) {
	                				BufferedImage originalImage = ImageIO.read(media.getStreamData());
	                				
	                				ByteArrayOutputStream baos = new ByteArrayOutputStream();
	                				try (InputStream in = media.getStreamData()) {
	                				    byte[] buf = new byte[8192];
	                				    int len;
	                				    while ((len = in.read(buf)) != -1) {
	                				        baos.write(buf, 0, len);
	                				    }
	                				}
	                				byte[] imageBytes = baos.toByteArray();

	                				int orientation = 1;
	                				try {
	                				    Metadata metadata = ImageMetadataReader.readMetadata(new ByteArrayInputStream(imageBytes));
	                				    ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
	                				    if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
	                				        orientation = directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
	                				    }
	                				} catch (Exception e) {
	                					UniLog.log(e);
	                				    // ignore EXIF read failure, fallback to original orientation
	                				}
	                				
	                				UniLog.log("Photo orientatin is " + orientation);
	                				BufferedImage orientedImage = applyExifOrientation(originalImage, orientation);	
	                				originalImage = orientedImage;
	                				int width = originalImage.getWidth();
	                				int height = originalImage.getHeight();	
	                				double scale = Math.min(
	                				        (double) MAXIMGSIZE / width,
	                				        (double) MAXIMGSIZE / height
	                				);	
	                				scale = Math.min(scale, 1.0);
	                				int newWidth = (int) (width * scale);
	                				int newHeight = (int) (height * scale);
	                				
	                				/* Resize */
	                				BufferedImage resizedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

	                				Graphics2D g = resizedImage.createGraphics();
	                				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	                				g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	                				g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	                				g.drawImage(originalImage, 0, 0, newWidth, newHeight, null);
	                				g.dispose();	
	                				
	                				messageRg = Integer.parseInt(v.toString().substring(4,14).trim());
	                				String fname = v.toString().substring(14);
	                				OutputStream os = p_result.getSessionHelper().newErpFileOutputStream(fname);
//	                				copy(media.getStreamData(),os);
	                				ImageIO.write(resizedImage, "jpg", os);	
	                				os.close();
	                			}
	                	} else {
	                		ZkUtil.msg("Invalid Format , Only Jpeg is current supported");
	                	}
	                	try  {
	                		int cc;
	                		cc = 1;
	                		for(int p_recIdx : recs) {
	                			try {
               						p_result.loadOneRecV(p_recIdx);
               						p_result.fetchOneRecV(p_recIdx);
               						p_result.getCell("pdpi_photoid").set(messageRg);
               						p_result.updateCurrent();
               					} catch (Exception ex) {
               						UniLog.log(ex);
               					}
	                		}
						    biBase.biBaseRefresh(p_result);
	                	} catch (Exception ex) {
	                		UniLog.log(ex);
	                		//messageBox(ex.toString());
	                	}
	                	
	                }
	    		}
		    });

		} catch (Exception ex) {
				UniLog.log(ex);
		}	
		return null;
	}

}
