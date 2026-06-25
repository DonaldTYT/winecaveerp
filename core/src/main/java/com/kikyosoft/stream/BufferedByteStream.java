package com.kikyosoft.stream;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BufferedByteStream implements ByteStream
    {
		ByteStream sourceStream;
		DataInputStream dis;
		DataOutputStream dos;
        public BufferedByteStream(ByteStream p_rpcs,int sendBufSize,int recvBufSize) 
        {
        	sourceStream = p_rpcs;
        	dis = new DataInputStream(new BufferedInputStream(p_rpcs.getDis(),recvBufSize));
        	dos = new DataOutputStream(new BufferedOutputStream(p_rpcs.getDos(),sendBufSize));
        }
        public void close() throws IOException
        {
        	dis.close();
        	dos.close();
        }

        public void flush() throws IOException
        {
        	dos.flush();
        }

        public int read(byte[] buf, int ofs, int len) throws IOException
        {
            return (dis.read(buf,ofs,len));
        }

        public void setTimeout(int msec) throws IOException
        {
            sourceStream.setTimeout(msec);
        }

        public int write(byte[] buf, int ofs, int len) throws IOException
        {
            dos.write(buf,ofs,len);
            return(len);
        }

        public ByteStream getByteStream()
        {
            return (sourceStream);
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
			return ("Buffered("+sourceStream.getName()+")");
		}

    }

