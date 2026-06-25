package com.uniinformation.rpccall;

import java.io.*;
import java.util.*;
import java.net.*;
import java.math.*;
import com.uniinformation.utils.*;
import com.uniinformation.rpccall.*;

public class MyDataInputStream {
	DataInputStream is;
	boolean hexMode = false;

	private int readHexToByte() throws IOException
	{
		return(((is.readUnsignedByte()-'0') << 4) + (is.readUnsignedByte()-'0'));
		/*
		int c,c0,c1;
		c0 = is.readUnsignedByte();
		c1 = is.readUnsignedByte();
		c = ((c0-'0') << 4) + (c1-'0');
		UniLog.log("readHexToByte c0:"+c0+" C1:"+c1+ " ->c:"+c);
		return(c);	
		*/
	}

	public void setHexMode(boolean p_sw)
	{
		UniLog.log("RpcConnection MyDataInputStream HexMode " + p_sw);
		hexMode = p_sw;
	}
	public MyDataInputStream(BufferedInputStream p_s) {
		is = new DataInputStream(p_s);
	}
	public byte readByte() throws IOException {
		if(hexMode) {
			return((byte) readHexToByte());
		} else {
			return(is.readByte());
		}
	}
	public int readInt() throws IOException {
		if(hexMode) {
			return(
				(int) (
				(readHexToByte() << 24) + 
				(readHexToByte() << 16) + 
				(readHexToByte() << 8) + 
				(readHexToByte())
				)
				);
		} else {
			return(is.readInt());
		}
	}
	public int readUnsignedByte() throws IOException {
		if(hexMode) {
			return(
				(readHexToByte())
			);
		} else {
			return(is.readUnsignedByte());
		}
	}
	public short readShort() throws IOException {
		if(hexMode) {
			return(
				(short) (
				(readHexToByte() << 8) + 
				(readHexToByte())
				)
				);
		} else {
			return(is.readShort());
		}
	}
	public int read(byte ba[],int ofs,int len) throws IOException {
		if(hexMode) {
			int cc;
			byte ba2[] = new byte[len*2];
			cc = is.read(ba2,0,len*2);
			if((cc & 1) == 1) {
				ba2[cc] = (byte) is.readUnsignedByte();
				cc++;
			}
			int i,j;
			for(i=0,j=ofs;i<cc;i+=2,j++) {
				byte b;
				b = (byte) ((ba2[i] - '0') << 4);
				b += (ba2[i+1] - '0');
				ba[j] = b;
			}
			return(cc / 2);
		} else {
			return(is.read(ba,ofs,len));
		}
	}
	public void close () throws IOException {
		is.close();
	}
}
