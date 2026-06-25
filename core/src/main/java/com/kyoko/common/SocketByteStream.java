
package com.kyoko.common;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class SocketByteStream implements ByteStream
    {
		static public final int MAX_WRITELEN = 32000;
        Socket tcps;
        DataInputStream dis;
        DataOutputStream dos;
        public SocketByteStream(Socket s) throws IOException
        {
            tcps = s;
            dis = new DataInputStream(s.getInputStream());
            dos = new DataOutputStream(s.getOutputStream());
        }
        public void close() throws IOException
        {
            tcps.close();
        }

        public void flush()
        {

        }
        public int read(byte[] buf, int ofs, int len) throws IOException
        {
        	return(dis.read(buf,ofs,len));
        }

        public void setTimeout(int msec) throws IOException
        {
			tcps.setSoTimeout(msec);
        }

        public int write(byte[] buf, int ofs, int len) throws IOException
        {
        	if(len > MAX_WRITELEN) len = MAX_WRITELEN;
        	dos.write(buf,ofs,len);
        	return(len);
        }

        public Socket getSocket()
        {
            return (tcps);
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
		@Override
		public String getName() {
			// TODO Auto-generated method stub
			return "socket:"+tcps.toString();
		}
    }
