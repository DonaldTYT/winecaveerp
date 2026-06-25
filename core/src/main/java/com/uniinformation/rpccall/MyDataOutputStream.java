package com.uniinformation.rpccall;

import java.io.*;
import java.util.*;
import java.net.*;
import java.math.*;
import com.uniinformation.rpccall.*;
import com.uniinformation.utils.*;

public class MyDataOutputStream {
	DataOutputStream is;
	boolean hexMode = false;
	private byte[] byteToHex(int b)
	{
		byte ba[] = new byte[2];
		ba[0] = (byte) (((b >> 4) & 0xf) + '0');
		ba[1] = (byte) ((b & 0xf) + '0');
		return(ba);
	}
	private byte[] shortToHex(int b)
	{
		byte ba[] = new byte[4];
		ba[0] = (byte) (((b >> 12) & 0xf) + '0');
		ba[1] = (byte) (((b >> 8) & 0xf) + '0');
		ba[2] = (byte) (((b >> 4) & 0xf) + '0');
		ba[3] = (byte) ((b & 0xf) + '0');
		return(ba);
	}
	private byte[] intToHex(int b)
	{
		byte ba[] = new byte[8];
		ba[0] = (byte) (((b >> 28) & 0xf) + '0');
		ba[1] = (byte) (((b >> 24) & 0xf) + '0');
		ba[2] = (byte) (((b >> 20) & 0xf) + '0');
		ba[3] = (byte) (((b >> 16) & 0xf) + '0');
		ba[4] = (byte) (((b >> 12) & 0xf) + '0');
		ba[5] = (byte) (((b >> 8) & 0xf) + '0');
		ba[6] = (byte) (((b >> 4) & 0xf) + '0');
		ba[7] = (byte) ((b & 0xf) + '0');
		return(ba);
	}
	public void setHexMode(boolean p_sw)
	{
		UniLog.log("RpcConnection MyDataOutputStream HexMode " + p_sw);
		hexMode = p_sw;
	}
	public MyDataOutputStream(BufferedOutputStream p_s) {
		is = new DataOutputStream(p_s);
	}
	public void writeByte(int b) throws IOException {
		if(hexMode) {
			is.write(byteToHex(b),0,2);
		} else
			is.writeByte(b);
	}
	public void writeShort(int s) throws IOException {
		if(hexMode) {
			is.write(shortToHex(s),0,4);
		} else {
			is.writeShort(s);
		}
	}
	public void writeInt(int i) throws IOException {
		if(hexMode) {
			is.write(intToHex(i),0,8);
		} else {
			is.writeInt(i);
		}
	}
	public void writeBytes(String s) throws IOException {
		if(hexMode) {
			byte ba[] = s.getBytes();
			for(int i = 0;i<ba.length;i++) {
				is.write(byteToHex(ba[i]));
			}
		} else {
			is.writeBytes(s);
		}
	}
	public void write(byte ba[],int ofs,int len) throws IOException {
		if(hexMode) {
			int n = ofs+len;
			for(int i = ofs;i<n;i++) {
				is.write(byteToHex(ba[i]));
			}
		} else {
			is.write(ba,ofs,len);
		}
	}
	public void flush() throws IOException {
		is.flush();
	}
	public void close () throws IOException {
		is.close();
	}
}
