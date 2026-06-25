package com.uniinformation.utils;
import java.io.*;
import java.util.*;
import org.apache.regexp.*;

public class FileUtil {
   static class AutoDeleteFileInputStream extends FileInputStream {
	   File file;
      AutoDeleteFileInputStream(File p_file) throws Exception {
			super(p_file);
		   file = p_file;
		}
		synchronized public void delete() {
		   if (file == null)
			   return;
			UniLog.logClass(this, "deleting "+file.getAbsolutePath()+" ...");
		   file.delete();
			file = null;
		}
		public void close() throws IOException {
		   super.close();
		   delete();
		}
		public void finalize() throws IOException {
		   delete();
		   super.finalize();
		}
	}
   static class AutoDeleteFileOutputStream extends FileOutputStream {
	   File file;
      AutoDeleteFileOutputStream(File p_file) throws Exception {
			super(p_file);
		   file = p_file;
		}
		synchronized public void delete() {
		   if (file == null)
			   return;
			UniLog.logClass(this, "deleting "+file.getAbsolutePath()+" ...");
		   file.delete();
			file = null;
		}
		public void close() throws IOException {
		   super.close();
		   delete();
		}
		public void finalize() throws IOException {
		   delete();
		   super.finalize();
		}
	}
   public static InputStream getAutoDeleteFileInputStream(File p_file) throws Exception {
      return(new AutoDeleteFileInputStream(p_file));
	}
   public static OutputStream getAutoDeleteFileOutputStream(File p_file) throws Exception {
      return(new AutoDeleteFileOutputStream(p_file));
	}
   public static InputStream getAutoDeleteFileInputStream(String p_filename) throws Exception {
      return(new AutoDeleteFileInputStream(new File(p_filename)));
	}
   public static OutputStream getAutoDeleteFileOutputStream(String p_filename) throws Exception {
      return(new AutoDeleteFileOutputStream(new File(p_filename)));
	}

	public static void getFileList(String p_dir, Vector p_vector, boolean p_recursive)
	{
		File dir = new File(p_dir);
		if(dir.isDirectory()) {
			String filelist[] = dir.list();
			for(int i=0;i <filelist.length; i++) {
				String s = new String(p_dir + "/" + filelist[i]);
				File f = new File(s);
				if(f.isDirectory()) {
					if(p_recursive)	
						getFileList(f.getPath(), p_vector, p_recursive);
				} else {
					p_vector.addElement(s);
				}
			}
		}
	}
	public static class FileListCache
	{
		Vector vector;
		long timeStamp;
		String dirname;

		public FileListCache(String p_dir, long p_timeStamp, Vector p_v)
		{
			dirname = p_dir;
			timeStamp = p_timeStamp;
			vector = p_v;
		}
		public long getTimeStamp() 
		{
			return(timeStamp);
		}
		public String getDirName() 
		{
			return(dirname);
		}
		public Vector getList() 
		{
			return(vector);
		}
	}
	static Hashtable fileListHashtable = new Hashtable();
	public static String searchFile(String p_dir, RE p_regexp)
	{
		File dir = new File(p_dir);
		Vector v;
		if(dir.isDirectory() == false)
			return(null);
		FileListCache cache;
		synchronized (fileListHashtable) {
			cache = (FileListCache) fileListHashtable.get(p_dir);
		}
		if(cache != null) {
			if(dir.lastModified() > cache.getTimeStamp()) {
				UniLog.log("filelist cache " + p_dir + " modified");
				cache = null;
			}
		}
		if(cache == null ) {
			UniLog.log("build filelist cache " + p_dir);
			long t = dir.lastModified();
			v = new Vector();
			synchronized (fileListHashtable) {
				getFileList(p_dir, v, false);
				cache = new FileListCache(p_dir,t,v);
				fileListHashtable.put(p_dir, cache);
			}
		}

		v = cache.getList();
		for(int i=0;i< v.size(); i++) {
			String s = (String)v.elementAt(i);
			if(p_regexp.match(new File(s).getName())) {
				return(s);
			}
		}
		return(null);
	}
	public static Vector searchFileList(String p_dir, RE p_regexp)
	{
		File dir = new File(p_dir);
		Vector v;
		Vector filelist = null;
		if(dir.isDirectory() == false)
			return(null);
		FileListCache cache;

		synchronized (fileListHashtable) {
			cache = (FileListCache) fileListHashtable.get(p_dir);
		}
		if(cache != null) {
			if(dir.lastModified() > cache.getTimeStamp()) {
				UniLog.log("filelist cache " + p_dir + " modified");
				cache = null;
			}
		}
		if(cache == null ) {
			UniLog.log("build filelist cache " + p_dir);
			long t = dir.lastModified();
			v = new Vector();
			synchronized (fileListHashtable) {
				getFileList(p_dir, v, false);
				cache = new FileListCache(p_dir,t,v);
				fileListHashtable.put(p_dir, cache);
			}
		}
		
		v = cache.getList();
		for(int i=0;i< v.size(); i++) {
			String s = (String)v.elementAt(i);
			if(p_regexp.match(new File(s).getName())) {
				if(filelist == null)
					filelist = new Vector();
				filelist.addElement(s);
			}
		}
		return(filelist);
	}
	/*
	public static String searchFile(String p_dir, RE p_regexp)
	{
		File dir = new File(p_dir);
		if(dir.isDirectory()) {
			String filelist[] = dir.list();
			for(int i=0;i <filelist.length; i++) {
				File f = new File(p_dir + "/" + filelist[i]);
				if(p_regexp.match(f.getName())) {
					return(f.getPath());
				}
				if(f.isDirectory()) {
					String s = searchFile(f.getPath(), p_regexp);
					if(s != null)
						return(s);
				}
			}
		}
		return(null);
	}
	*/
	public static byte[] getBytesFromFile(File p_objFile) {
		FileInputStream fis = null;
      ByteArrayOutputStream bos = null;
		try {
			if (p_objFile.exists()) {
				fis = new FileInputStream(p_objFile);
            bos = new ByteArrayOutputStream();
            ReaderCopier.copy_and_close(fis, bos);
				return(bos.toByteArray());
			}
		} catch(Exception ex) {
			UniLog.log(ex);
		} finally {
			try {
				if (fis != null)
					fis.close();
			} catch(Exception ex) {
				UniLog.log(ex);
			}
		}
		return(new byte[0]);
	}
	public static File bytesToFile(byte[] p_bytes) {
	   try {
		   return(bytesToFile(p_bytes, File.createTempFile("tmp", "tmp")));
		} catch (Exception ex) {
		   UniLog.log(ex);
		   return(null);
		}
	}
	public static File bytesToFile(byte[] p_bytes, File p_outputFile) {
		FileOutputStream fos = null;
		ByteArrayInputStream bis = null;
		File objFile = null;
		try {
			objFile = p_outputFile;
			fos = new FileOutputStream(objFile);	
			bis = new ByteArrayInputStream(p_bytes);
      	ReaderCopier.copy(bis, fos);
		} catch(Exception ex) {
			UniLog.log(ex);
		} finally {
			try {
				if (fos != null)
					fos.close();
				if (bis != null)
					bis.close();
			} catch(Exception ex) {
				UniLog.log(ex);
			}
		}
		return(objFile);
	}
	public static void main(String[] p_args) {
		File objFile = new File("/tmp/l1");
		byte[] bytes = FileUtil.getBytesFromFile(objFile);
		File tmpFile = FileUtil.bytesToFile(bytes);
		UniLog.log("trace:fileName="+tmpFile.getAbsolutePath());
	}
}
