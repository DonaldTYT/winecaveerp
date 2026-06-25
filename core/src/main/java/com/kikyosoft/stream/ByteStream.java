package com.kikyosoft.stream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface ByteStream
    
    {
        public final int SOH = 0x01;
        public final int STX = 0x02;
        public final int ETX = 0x03;
        public final int EOT = 0x04;
        public final int ENQ = 0x05;
        public final int ACK = 0x06;
        public final int DC1 = 0x11;
        public final int DC2 = 0x12;
        public final int DC3 = 0x13;
        public final int DC4 = 0x14;
        public final int NAK = 0x15;
        public final int SYN = 0x16;
        public final int ETB = 0x17;
        public final int CAN = 0x18;
        
        public static void writeByte(ByteStream bs,int val) throws IOException
        {
        	bs.getDos().write(val);
        }
        public static void writeShort(ByteStream bs,int val) throws IOException
        {
        	bs.getDos().writeShort(val);
        }
        public static void writeInt(ByteStream bs,int val) throws IOException
        {
        	bs.getDos().writeInt(val);
        }
        public static void writeLong(ByteStream bs,long val) throws IOException
        {
        	bs.getDos().writeLong(val);
        }
        public static void writeDouble(ByteStream bs,double val) throws IOException
        {
        	bs.getDos().writeDouble(val);
        }
        abstract public DataInputStream getDis();
        abstract public DataOutputStream getDos();
        abstract public void close() throws IOException;
        abstract public void setTimeout(int msec) throws IOException;
        abstract public void flush() throws IOException;
        abstract public int write(byte[] buf, int ofs, int len) throws IOException;
        abstract public int read(byte[] buf, int ofs, int len) throws IOException;
        abstract public String getName();

        public static int readByte(ByteStream bs)  throws IOException
        {
        	return(bs.getDis().readByte());
        }
        public static int readUnsignedByte(ByteStream bs)  throws IOException
        {
            int val =bs.getDis().readByte();
            if(val < 0) val += 256;
            return (val);
        }
        public static int readShort(ByteStream bs) throws IOException
        {
        	return(bs.getDis().readShort());
        }
        public static int readUnsignedShort(ByteStream bs) throws IOException
        {
            int val = bs.getDis().readShort();
            if (val < 0) val += 65536;
            return (val);
        }
        public static int readInt(ByteStream bs) throws IOException
        {
        	return(bs.getDis().readInt());
        }
        public static long readLong(ByteStream bs) throws IOException
        {
        	return(bs.getDis().readLong());
        }
        public static double readDouble(ByteStream bs) throws IOException
        {
        	return(bs.getDis().readDouble());
        }
        public static void loopRead(ByteStream bs,byte[] buf,int idx,int len) throws IOException
        {
            while(len > 0)
            {
                int cc = 
                            bs.read(buf, idx, len);
                if (cc < 0) throw new StreamException("Error during loop read");
                idx += cc;
                len -= cc;
            }
        }
        public static void loopWrite(ByteStream bs,byte[] buf,int idx,int len) throws IOException
        {
            while(len > 0)
            {
                int wlen;
                wlen = len > 30000 ? 30000:len;
                int cc = 
                            bs.write(buf, idx, wlen);
                if (cc < 0) throw new StreamException("Error during loop write");
                idx += cc;
                len -= cc;
            }
        }
    }
