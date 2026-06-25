package com.uniinformation.utils;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLConnection;

import org.zkoss.zul.Filedownload;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.uniinformation.webcore.SessionHelper;

public class DownloadByteArrayOutputStream extends ByteArrayOutputStream {
	SessionHelper sh;
	String tabname;
	String key;
	String name;
	String desc;
	boolean isClosed = false;
	public DownloadByteArrayOutputStream (SessionHelper p_sh,String p_tabname,String p_key,String p_name,String p_desc) {
		super();
		sh = p_sh;
		tabname = p_tabname;
		key = p_key;
		name = p_name;
		desc = p_desc;
	}
	@Override 
	public void close() throws IOException {
		super.close();		
		try {
			synchronized(this) {
				if(!isClosed) {
//					FilingUtil.storeFile(sh.getAgent(), tabname, key, name, desc, new ByteArrayInputStream(this.toByteArray()));
//    				Filedownload.save(this.toByteArray(), fileTypeMap.get("mimeType"), fileName);
//					BufferedInputStream bufis = new BufferedInputStream( new ByteArrayInputStream(this.toByteArray()));
//					FileType fileType = FileTypeDetector.detectFileType(bufis);
//					String minmeType = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(this.toByteArray()));
    				Filedownload.save(this.toByteArray(), "application/pdf", "abc.pdf");
					isClosed = true;
				} 
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			throw new IOException(ex.toString());
		}
	}
}
