package com.uniinformation.utils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.rpccall.RpcClient;

public class CloseUtil {
	/***
	 * close anything without throw exception
	 * with error msg
	 * @param p_objs 
	 */
	public static void close(Object... p_objs) {
		if (p_objs == null) return;
		for (int i=0; i<p_objs.length; i++) {
			Object obj = p_objs[i];
			try {
				if (obj == null) {
					UniLog.log1("obj is null, ignore");
					continue;
				}
				if (obj instanceof SelectUtil) {
					((SelectUtil) obj).close();
					continue;
				}
				if (obj instanceof BiResult) {
					((BiResult) obj).close();
					continue;
				}
				if (obj instanceof Reader) {
					((Reader) obj).close();
					continue;
				}
				if (obj instanceof InputStream) {
					((InputStream) obj).close();
					continue;
				}
				if (obj instanceof OutputStream) {
					((OutputStream) obj).close();
					continue;
				}
				if (obj instanceof Writer) {
					((Writer) obj).close();
					continue;
				}
				if (obj instanceof RpcClient) {
					((RpcClient) obj).close();
					continue;
				}
				try {
					throw new Exception("warning: object not supported:"+ obj);
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			
			
		}
	}
	/***
	 * delete anything without exception
	 * @param p_objs
	 */
	public static void delete(Object... p_objs) {
		if (p_objs == null) return;
		for (int i=0; i<p_objs.length; i++) {
			Object obj = p_objs[i];
			try {
				if (obj == null) {
					UniLog.log1("obj is null, ignore");
					continue;
				}
				if (obj instanceof File) {
					((File) obj).delete();
					continue;
				}
				if (obj instanceof String) {
					(new File((String)obj)).delete();
					continue;
				}
				try {
					throw new Exception("warning: object not supported:"+ obj);
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			
			
		}
	}
	
	public static void flush(Object... p_objs) {
		if (p_objs == null) return;
		for (int i=0; i<p_objs.length; i++) {
			Object obj = p_objs[i];
			try {
				if (obj == null) {
					UniLog.log1("obj is null, ignore");
					continue;
				}
				if (obj instanceof OutputStream) {
					((OutputStream) obj).flush();
					continue;
				}
				if (obj instanceof Writer) {
					((Writer) obj).flush();
					continue;
				}
				try {
					throw new Exception("warning: object not supported:"+ obj);
				}
				catch(Exception ex) {
					ex.printStackTrace();
				}
			}
			catch(Exception ex) {
				ex.printStackTrace();
			}
			
			
		}
	}
	
	public static void main(String args[]) {
		close(null);
		close(null,null);
	}

}
