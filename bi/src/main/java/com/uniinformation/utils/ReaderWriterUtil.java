package com.uniinformation.utils;
import java.io.*;

public class ReaderWriterUtil {
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
	   cnt = copy(reader, writer);
		reader.close();
		writer.close();
		return(cnt);
	}
   public static void main(String[] args) {
	   FileReader fr;
	   FileWriter fw;

		try {
		   fr = new FileReader(new File(args[0]));
		   fw = new FileWriter(new File(args[1]));
         System.out.println("copy() return "+copy_and_close(fr, fw));
		} catch (Exception e) {
		}
	}
}
