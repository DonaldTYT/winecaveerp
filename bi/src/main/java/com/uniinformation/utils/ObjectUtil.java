package com.uniinformation.utils;
import java.io.*;
import java.util.*;
import java.net.*;
import java.text.*;
import com.uniinformation.cell.*;

public class ObjectUtil {
	/***
	 * get the serializable object size
	 * for debug only. this operation is expensive
	 * @param p_obj
	 * @return
	 */
	public static int getObjectSize(Serializable p_obj) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(p_obj);
			oos.flush();
			oos.close();
			int length = bos.toByteArray().length;
			bos.close();
			return(length);
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}
}
