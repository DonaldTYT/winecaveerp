package com.uniinformation.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.uniinformation.webcore.SessionHelper;

public class FilingByteArrayOutputStream extends ByteArrayOutputStream {
	SessionHelper sh;
	String tabname;
	String key;
	String name;
	String desc;
	boolean isClosed = false;
	public FilingByteArrayOutputStream (SessionHelper p_sh,String p_tabname,String p_key,String p_name,String p_desc) {
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
					FilingUtil.storeFile(sh.getAgent(), tabname, key, name, desc, new ByteArrayInputStream(this.toByteArray()));
					isClosed = true;
				} 
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			throw new IOException(ex.toString());
		}
	}
}
