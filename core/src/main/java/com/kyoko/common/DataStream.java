package com.kyoko.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public abstract class DataStream implements ByteStream{
    int id;
    int peerid;
    int port;
//    public List<byte[]> rbufs;

    DataInputStream dis;
    DataOutputStream dos;

    PipedInputStream pis;
    PipedOutputStream pos;
    
    int obufsize = 0;
    int ibufsize = 0;
//    static final int BUFSIZE=4096;

    abstract protected void flushOutputPipe() throws IOException;

    public DataStream(int p_ibufsize,int p_obufsize) throws IOException
    {
        pos = new PipedOutputStream();
        ibufsize = p_ibufsize;
        dis = new DataInputStream(new PipedInputStream(pos,ibufsize));

        PipedOutputStream os = new PipedOutputStream();
        dos = new DataOutputStream(os);
        obufsize = p_obufsize;
        pis = new PipedInputStream(os,obufsize);
    }
    public void close() throws IOException
    {
    	pos.close();
    	pis.close();
    	dos.close();
    	dis.close();
    }
    
    
    public int read(byte[] buf, int ofs, int len) throws IOException
    {
    	return(dis.read(buf,ofs,len));
    }

    public int write(byte[] buf, int ofs, int len) throws IOException
    {
    	int wlen;
    	if(len > obufsize ) wlen = obufsize ; else wlen = len;
    	if(wlen+pis.available() >= obufsize) {
    		flushOutputPipe();
    	}
    	dos.write(buf,ofs,wlen);
    	return(wlen);
    }
	@Override
	
	public DataInputStream getDis() {
		// TODO Auto-generated method stub
		return dis;
	}
	@Override
	public DataOutputStream getDos() {
		// TODO Auto-generated method stub
		return dos;
	}
}
