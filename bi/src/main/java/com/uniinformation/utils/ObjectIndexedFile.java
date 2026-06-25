package com.uniinformation.utils;
import java.util.*;
import java.io.*;
import java.util.Hashtable;
public class ObjectIndexedFile {
   File idxFile = null;
   File objFile = null;
	RandomAccessFile idxRand = null;
	RandomAccessFile objRand = null;
	int objcnt;
   public ObjectIndexedFile() throws Exception {
		idxFile = File.createTempFile("oif","idx");
		objFile = File.createTempFile("oif","obj");
	   idxRand = new RandomAccessFile(idxFile, "rw");
	   objRand = new RandomAccessFile(objFile, "rw");
		objcnt = 0;
	}
	public void addObject(Object p_obj) throws Exception {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream toos = new ObjectOutputStream(baos);
		toos.writeObject(p_obj);
		toos.close();
		byte[] ba = baos.toByteArray();
		long offset = objRand.length();
		objRand.seek(offset);
		objRand.writeInt(ba.length);
		objRand.write(ba);
		idxRand.seek(idxRand.length());
		idxRand.writeLong(offset);
		objcnt++;
	}
	public Object getObject(int p_idx) throws Exception {
	   if (p_idx >= objcnt)
		   return(null);
		idxRand.seek(((long) p_idx) * 8);
		long offset = idxRand.readLong();
		objRand.seek(offset);
		int objlen = objRand.readInt();
		byte[] ba = new byte[objlen];
	   objRand.read(ba);
		ByteArrayInputStream bais = new ByteArrayInputStream(ba);
		ObjectInputStream tois = new ObjectInputStream(bais);
		Object obj = tois.readObject();
		bais.close();
		tois.close();
		return(obj);
	}
	public void close() {
	   if (objRand != null)
		   try { objRand.close(); objRand = null; } catch (Exception ex) { UniLog.log(ex); };
	   if (idxRand != null)
		   try { idxRand.close(); idxRand = null; } catch (Exception ex) { UniLog.log(ex); };
      if (objFile != null)
		   try { objFile.delete(); objFile = null; } catch (Exception ex) { UniLog.log(ex); };
      if (idxFile != null)
		   try { idxFile.delete(); idxFile = null; } catch (Exception ex) { UniLog.log(ex); };
	}
   protected void finalize() {
		UniLog.logClass(this, "finalizing ...");
	   close();
	}
}
