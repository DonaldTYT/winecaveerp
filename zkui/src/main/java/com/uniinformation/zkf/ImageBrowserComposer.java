package com.uniinformation.zkf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.DigestInputStream;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.crypto.SHA256withRSA;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.ZkSessionHelper;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

/**
 * Image browser with multiple-file upload support (ZK 9.5.x).
 */
public class ImageBrowserComposer extends SelectorComposer<Component> {
	static final String filingTable = "medialib";

    @Wire
    private Listbox imageListbox;

    @Wire
    private Label msg;
    
    @Wire
    private Textbox searchBox;

    /** In-memory list backing the model. */
    private final List<ImageRecord> images = new ArrayList<>();

    /** Strongly-typed model, so we don't need casts from listbox.getModel(). */
    private ListModelList<ImageRecord> model;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        loadImages();
        initListbox();

        if (msg != null) {
            msg.setValue("Loaded " + images.size() + " image(s).");
        }
    }
    BiResult br;
  	protected ZkSessionHelper sessionHelper;

    /** Stub – replace with your own DAO/service to select image records. */
    private void loadImages() {
     	
    	if(sessionHelper == null) sessionHelper = ZkSessionHelper.getSessionHelper();		
    	if(br == null) br = sessionHelper.getBiSchema().getViewByName("wcerp.MediaLib").newBiResult(sessionHelper.getLoginId(),null , null, sessionHelper);
    	br.clear();
    	br.clearCondition();
    	String cond = searchBox.getText();
    	if(!StringUtils.isBlank(cond)) {
    		ReturnMsg rtn = br.addCustomCondition(cond, false);
		  	if(!rtn.getStatus()) {
			 	rtn = br.addCustomCondition("fl_name like '%"+cond+"%'" , false);
		  	}
    	}
    	br.query();
        images.clear();
    	for(int i=0;i<br.getRowCount();i++) {
    		br.loadOneRecV(i);
    		images.add(new ImageRecord(
                br.getCellString("fl_name"),
                "https://winecave.erpv4.com/saleorsync/getResource?url=filing://medialib/"+ br.getCell("fl_key")+"&ext=jpg",
                "",
                DateUtil.dateToDateTimeStr(br.getCellDate("fl_cts"))
                ));
    	}

        
        
        // TODO: query your DB or filesystem and fill `images`
    }

    private void initListbox() {
        if (imageListbox == null) {
            return;
        }

        model = new ListModelList<>(images);
        imageListbox.setModel(model);

        imageListbox.setItemRenderer(new ListitemRenderer<ImageRecord>() {
            @Override
            public void render(Listitem item, ImageRecord data, int index) throws Exception {
                item.setValue(data);
                item.getChildren().clear();

                Listcell cell = new Listcell();
                item.appendChild(cell);

                Div card = new Div();
                card.setSclass("img-card");
                cell.appendChild(card);

                org.zkoss.zul.Image img = new org.zkoss.zul.Image();
                img.setSrc(data.getUrl());
                img.setSclass("img-card-img");
                card.appendChild(img);

                Div body = new Div();
                body.setSclass("img-card-body");
                card.appendChild(body);

                Label title = new Label(data.getTitle());
                title.setSclass("img-card-title");
                body.appendChild(title);

                Label sub = new Label(data.getSubtitle());
                sub.setSclass("img-card-sub");
                body.appendChild(sub);

                Label meta = new Label(data.getMeta());
                meta.setSclass("img-card-meta");
                body.appendChild(meta);
            }
        });
    }

    // ───────────────────── UPLOAD HANDLER ─────────────────────

    @Listen("onUpload = #uploadBtn")
    public void onUploadImages(UploadEvent event) throws Exception {
        Media[] medias = event.getMedias();
        if (medias == null || medias.length == 0) {
            return;
        }

        int count = 0;

        for (Media media : medias) {
            String url = saveMediaToServer(media);
            if (url == null) {
                continue;
            }

            ImageRecord rec = new ImageRecord(
                    media.getName(),
                    url,
                    "",
                    media.getContentType()
            );

            images.add(rec);
            if (model != null) {
                model.add(0, rec); // show newest first
            }
            count++;
        }

//        if (count > 0) {
//               // after upload: reload the whole page
//                Executions.sendRedirect(null);
//        } else if (msg != null) {
//        	if (msg != null) {
//                msg.setValue("No image uploaded.");
//        	}
//        }
        if(count > 0) {
        	if (msg != null) {
        		msg.setValue("Uploaded " + count + " image(s).");
        	}
        	loadImages();
    	    if (model != null) {
    	        model.clear();
    	        model.addAll(images);
    	    }
        } else {
        	if (msg != null) {
                msg.setValue("No image uploaded.");
        	}
        }
    }

    private String saveMediaToServer(Media media) throws Exception {
    	JSONObject jo = new JSONObject();
    	jo.put("contentType", media.getContentType());
    	jo.put("fileName", media.getName());
    	jo.put("format",media.getFormat());
    	
    	DigestInputStream dis = SHA256withRSA.newDigestInputStream(media.getStreamData());
    	FilingUtilObject fo = FilingUtil.storeFile(sessionHelper.getAgent(), filingTable, null, media.getName(), jo.toString(), dis);
    	if(fo != null) return("https://winecave.erpv4.com/saleorsync/getResource?url=filing://medialib/"+ br.getCell("fl_key")+"&ext=jpg"); else return(null);
//    	String sha256B64 = SHA256withRSA.bytesToBase64(SHA256withRSA.sha256Hex(media.getStreamData()));
//    	UniLog.log("Media Type = "+media.getContentType() + " sha256 " + sha256B64);
//    	FilingUtil.storeFile(p_result.getSessionHelper().getAgent(), filingTable, p_key, media.getName(), jo.toString(), p_is)
    	/*
    	if(!media.getContentType().equals("application/pdf") )
    	{
    		messageBox("Only Pdf File Are Accepted");
    		return;
    	}
    	*/
    }

    @Listen("onClick = #searchBtn; onOK = #searchBox")
    public void onSearch() {
    	loadImages();
    	   if (model != null) {
    	        model.clear();
    	        model.addAll(images);
    	    }
    }

    
    
//    private String saveMediaToServer(Media media) throws IOException {
//        String baseDir = "/var/www/winecave/images/upload"; // TODO: adjust
//        Files.createDirectories(Paths.get(baseDir));
//
//        String fileName = media.getName();
//        Path dest = Paths.get(baseDir, fileName);
//
//        try (InputStream in = media.isBinary()
//                ? media.getStreamData()
//                : new ByteArrayInputStream(
//                        media.getStringData().getBytes(StandardCharsets.UTF_8))) {
//            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
//        }
//
//        String baseUrl = "/images/upload/"; // TODO: adjust
//        return baseUrl + fileName;
//    }

    // ───────────────────── POJO MODEL ─────────────────────

    public static class ImageRecord {
        private final String title;
        private final String url;
        private final String subtitle;
        private final String meta;

        public ImageRecord(String title, String url, String subtitle, String meta) {
            this.title = title;
            this.url = url;
            this.subtitle = subtitle;
            this.meta = meta;
        }

        public String getTitle()    { return title; }
        public String getUrl()      { return url; }
        public String getSubtitle() { return subtitle; }
        public String getMeta()     { return meta; }
    }
}
