package com.uniinformation.utils;
import java.io.*;

public class ReaderCopier {
	public static int copy(Reader reader, Writer writer) throws IOException {
	   char buffer[];
		int cc, cnt;

		buffer = new char[1024];
		cnt = 0;
		for (;;) {
		   cc = reader.read(buffer);
			if (cc < 0) 
			   break;
		   writer.write(buffer, 0, cc);
			cnt += cc;
		}
		return(cnt);
	}
	public static int copy_and_close(Reader reader, Writer writer) throws IOException {
		int cnt;
		try {
	      cnt = copy(reader, writer);
		} catch (IOException ex) {
		   throw(ex);
		} finally {
			try { reader.close(); } catch (Exception ex1) {}
			try { writer.close(); } catch (Exception ex1) {}
		}
		return(cnt);
	}
	public static int copy(InputStream reader, OutputStream writer) throws IOException {
	   byte buffer[];
		int cc, cnt;

		buffer = new byte[1024];
		cnt = 0;
		for (;;) {
		   cc = reader.read(buffer);
			if (cc < 0) 
			   break;
		   writer.write(buffer, 0, cc);
			cnt += cc;
		}
		return(cnt);
	}
	public static int copy_and_close(InputStream reader, OutputStream writer) throws IOException {
		int cnt;
		try {
	      cnt = copy(reader, writer);
		} catch (IOException ex) {
		   throw(ex);
		} finally {
			try { reader.close(); } catch (Exception ex1) {}
			try { writer.close(); } catch (Exception ex1) {}
		}
		return(cnt);
	}
   public static void main_xxx(String[] args) {
	   FileReader fr;
	   FileWriter fw;

		try {
		   fr = new FileReader(new File(args[0]));
		   fw = new FileWriter(new File(args[1]));
         System.out.println("copy() return "+copy_and_close(fr, fw));
		} catch (Exception e) {
		}
	}
   public static void main(String[] args) throws Exception {
	   FileInputStream fis = new FileInputStream(args[0]);
	   PushbackInputStream pis = new PushbackInputStream(fis, 1024);
	   FileOutputStream fos = new FileOutputStream(args[1]);
		pis.unread(args[2].getBytes());
	   copy_and_close(pis, fos);
	}
}
