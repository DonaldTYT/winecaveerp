package com.kikyosoft.stream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.kikyosoft.utils.LogUtil;

public class StreamMultiplexer implements Runnable
    {
        protected ByteStream rpcs;
        HashMap<Integer, MpxStream> activeStreams;
        HashMap<Integer, ArrayList<MpxStream>> pendingStreams;
        public final int BUFSIZE = 2048;
        

        int streamid = 0;
        Thread servthread = null;

        public class MpxStream implements ByteStream
        {
            int id;
            int peerid;
            int port;
//            public List<byte[]> rbufs;

            DataInputStream dis;
            DataOutputStream dos;

            PipedInputStream pis;
            PipedOutputStream pos;

//            public CancellationTokenSource cancelToken;

            public MpxStream(int p_id, int p_peerid,int p_port) throws IOException
            {
                id = p_id;
                peerid = p_peerid;
                port = p_port;

                pos = new PipedOutputStream();
                dis = new DataInputStream(new PipedInputStream(pos,BUFSIZE));

                PipedOutputStream os = new PipedOutputStream();
                dos = new DataOutputStream(os);
                pis = new PipedInputStream(os,BUFSIZE);
            }
            public void close() throws IOException
            {
            	LogUtil.log("MpxStream closed");
            	pos.close();
            	pis.close();
            	dos.close();
            	dis.close();
            	if(peerid > 0) {
            		synchronized(rpcs) {
            		ByteStream.writeByte(rpcs,ByteStream.ETB);
            		ByteStream.writeInt(rpcs,peerid);
            		}
            	}
                rpcs.flush();
            }
            
            
            private void flushMpxStream() throws IOException {
            	int available;
            	while ( (available = pis.available()) > 0) {
            		byte ba[] = new byte[available];
            		int cc = pis.read(ba,0,available);
//            		CoreLog.log("flushMpxStream write " + cc + " bytes");
            		synchronized(rpcs) {
            		ByteStream.writeByte(rpcs,ByteStream.STX);
            		ByteStream.writeInt(rpcs,peerid);
            		ByteStream.writeShort(rpcs,cc);
                    ByteStream.loopWrite(rpcs, ba,0, cc);
            		}
            	}
            }
            
            public void flush() throws IOException
            {
            	flushMpxStream();
                rpcs.flush();
            }

            public int read(byte[] buf, int ofs, int len) throws IOException
            {
            	return(dis.read(buf,ofs,len));
            }

            public void setTimeout(int msec)
            {
                //                myrpcs.setTimeout(msec);
            }

            public int write(byte[] buf, int ofs, int len) throws IOException
            {
            	int wlen;
            	if(len > BUFSIZE) wlen = BUFSIZE; else wlen = len;
            	if(wlen+pis.available() >= BUFSIZE) {
            		flushMpxStream();
            	}
            	dos.write(buf,ofs,wlen);
            	dos.flush();
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
			@Override
			public String getName() {
				// TODO Auto-generated method stub
				return null;
			}
			
			public StreamMultiplexer getMpx() {
				return(StreamMultiplexer.this);
			}
        }


        /*
        MpxStream newStream(int port)
        {
            streamid++;
            if(activeStreams == null) return(null);
            MpxStream ms = new MpxStream(streamid, port,rpcs);
            activeStreams.Add(streamid,ms);
            return (ms);
        }
        */
        public StreamMultiplexer(ByteStream p_rpcs)
        {
            LogUtil.log("RpcMultiplexer Created " );
            rpcs = p_rpcs;
            activeStreams = new HashMap<Integer, MpxStream>();
            pendingStreams = new HashMap<Integer, ArrayList<MpxStream>>();
// NEED HANDLING            receiver =
        }
        
        public void start() {
 		    servthread = new Thread(this);
 		    servthread.setDaemon(true);
 		    servthread.setName("StreamMultiplexer "+rpcs.getName());
 		    servthread.start();
        }
        
        @Override
        public void run()
        {
        	LogUtil.log(Thread.currentThread().getName()+" Started");
            try
            {

            for (; ; )
            {
                int cmd =
                    ByteStream.readByte(rpcs);
//                CoreLog.log("got mpx command " + cmd);
                switch (cmd)
                {
                    case ByteStream.EOT: // reset all
                        activeStreams.clear();
                        pendingStreams.clear();
                        LogUtil.log("EOT received");
                        break;
                    case ByteStream.CAN: // reset all
                        activeStreams.clear();
                        pendingStreams.clear();
                        synchronized(rpcs) {
                        ByteStream.writeByte(rpcs,ByteStream.EOT);
                        }
                        rpcs.flush();
                        LogUtil.log("CAN received");
                        break;
                    case ByteStream.SOH: // start stream by caller
                        int peerid = 
                            ByteStream.readInt(rpcs);

                        int port =
                            ByteStream.readShort(rpcs);
//                      List<MpxStream> msl = pendingStreams[port];
//                      if(msl == null)
                        ArrayList<MpxStream> msl = null;
                        MpxStream ms = null;
                        synchronized(activeStreams)
                        {
                        	msl = pendingStreams.get(port);
                            if(msl == null)
                            {
                                msl = new ArrayList<MpxStream>();
//                              pendingStreams[port] = msl;
                                pendingStreams.put(port, msl);
                            }
                            LogUtil.log("msl count = " + msl.size());
                            for(int i=0;i<msl.size();i++)
                            {
                                LogUtil.log("msl" + i + " " + msl.get(i).peerid + " , " + msl.get(i).port);
                                if (msl.get(i).peerid == 0 && msl.get(i).port == port)
                                {
                                	ms = msl.get(i);
                                    /* NEED HANDLING */
                                    /*
                                    if(ms.cancelToken != null)
                                    {
                                        ms.cancelToken.Cancel();
                                        ms.cancelToken = null;
                                    }
                                    */
                                    break;
                                }
                            }
                        }
                        if (ms == null)
                        {
                            // new MpxStream(id, rpcs);
                            LogUtil.log("add unaccepted tcp stream peerid " + peerid + " port " + port);
                            ms = new MpxStream(0, peerid, port);
                            msl.add(ms);
                        } else {
                            synchronized(ms) {
                            	ms.peerid = peerid;
                            	LogUtil.log("wakeup accepted tcp stream peerid " + peerid + " port " + port + " myid " + ms.id);
                            	ms.notifyAll();
                            }
                        }
                        break;
                    case ByteStream.ETB: // end stream by caller
                    	int closeid = ByteStream.readInt(rpcs);
                    	LogUtil.log("Mpx Stream closed by remote id " + closeid);
                        ms = null;
                        synchronized(activeStreams) {
                        	ms = activeStreams.get(closeid);
                        }
                        if(ms != null)  {
                        	ms.peerid = 0;
                        	ms.close();
                        }
                        break;
                    case ByteStream.STX: // start packet
                        int thisid = 
                            ByteStream.readInt(rpcs);
                        int len =
                            ByteStream.readShort(rpcs);
//                        CoreLog.log("in tunnel read " + thisid + " " + len);
                        byte[] tbuf = new byte[len];
                        ByteStream.loopRead(rpcs,tbuf, 0, len);
                        //                        MpxStream mpx = activeStreams[thisid];
                        //                        if(mpx != null)
                        ms = null;
                        synchronized(activeStreams) {
                        	ms = activeStreams.get(thisid);
                        }
                        if(ms != null) 
                        {
                            synchronized(ms.dis)
                            {
                                ms.pos.write(tbuf,0,len);
                                ms.pos.flush();
                            }
                        }
//                        CoreLog.log("240519 tunnel read flushed");
                        break;
                    case ByteStream.ETX: // end packet
                        break;
                    case ByteStream.ACK: // end packet
                        thisid = 
                            ByteStream.readInt(rpcs);
                        peerid =
                            ByteStream.readInt(rpcs);
                        synchronized(activeStreams)
                        {
                        	ms = activeStreams.get(thisid);
                        }
                        if(ms != null)
                        {
                           	synchronized(ms) {
                           		ms.peerid = peerid;
                               	ms.notifyAll();
                           	}
                        }

                        break;
                }
            }
            } catch (Exception e)
            {
                LogUtil.log("Exception trapped " + e.toString());
                for (ArrayList<MpxStream> mpsl : pendingStreams.values())
                {
                    for(MpxStream mpsl2 : mpsl)
                    {
                    	/*
                    	 * NEED HANDLING
                        if(mpsl2.cancelToken != null)
                        {
                            mpsl2.cancelToken.Cancel(); 
                        }
                        */
                    }
                }
//                foreach (MpxStream mpsl in activeStreams.Values)    
                for (MpxStream mpsl : activeStreams.values())    
                {
                	/*
                	 * NEED HANDLING
                    if(mpsl.cancelToken != null)
                    {
                        mpsl.cancelToken.Cancel();
                    }
                    */
                	
                }
                //                activeStreams.Clear();
                //                pendingStreams.Clear();
                activeStreams = null;
                pendingStreams = null;
                return;
            }
        }

        public ByteStream connect(int port) throws IOException
        {
            if (activeStreams == null) return (null);
            streamid++;
            MpxStream ms = new MpxStream(streamid, 0, port);
            synchronized(activeStreams)
            {
                activeStreams.put(streamid,ms);
            }
            synchronized(rpcs) {
            ByteStream.writeByte(rpcs,ByteStream.SOH);
            ByteStream.writeInt(rpcs,ms.id);
            ByteStream.writeShort(rpcs,port);
            }
            rpcs.flush();
            try {
                // NEED HANDLING
//                Thread.sleep(30000);
            	synchronized(ms) {
            		ms.wait(30000);
            	}
            } catch (Exception ex) {
                LogUtil.log("Connect Delay cancelled "+ex);
            }
            synchronized(ms)
            {
                if(ms.peerid > 0)
                {
                        LogUtil.log("connect return id = "+ms.id);
                        return(ms);
                } else {
                	synchronized(activeStreams)
                	{
                		activeStreams.remove(streamid);
                	}
                }
            }
            return (null);
        }

        public ByteStream accept(int port) throws IOException
        {
            if (activeStreams == null) return (null);
            ArrayList<MpxStream> msl = null;
            MpxStream ms = null;
            synchronized (activeStreams)
            {
            	msl = pendingStreams.get(port);
                if (msl == null)
                {
                    msl = new ArrayList<MpxStream>();
                    //                  pendingStreams[port] = msl;
                    pendingStreams.put(port, msl);
                }
                for (int i = 0; i < msl.size(); i++)
                {
                    if (msl.get(i).port == port && msl.get(i).peerid > 0 && msl.get(i).id == 0)
                    {
                        ms = msl.get(i);
                        streamid++;
                        ms.id = streamid;
                        break;
                    }
                }
            }
            if (ms == null)
            {
                streamid++;
                ms = new MpxStream(streamid, 0, port);
                msl.add(ms);
                try
                {
                    // NEED HANDLING
//                    Thread.sleep(30000);
                	synchronized(ms) {
                		ms.wait(30000);
                	}
                }
                catch (Exception ex)
                {
                    LogUtil.log("Accept Delay cancelled " + ex);
                    synchronized(ms)
                    {
                        if (ms.peerid <= 0)
                        {
                            // Exception catched but peerid not set, throws Stream Exception
                            msl.remove(ms);
                            throw new StreamException("Link Discounnted");
                        }
                    }
                }
            }
            synchronized(ms)
            {
                msl.remove(ms);
                if (ms.peerid <= 0)
                {
                    return (null);
                }
                activeStreams.put(ms.id, ms);
            }
            	synchronized(rpcs) {
                ByteStream.writeByte(rpcs,ByteStream.ACK);
                ByteStream.writeInt(rpcs,ms.peerid);
                ByteStream.writeInt(rpcs,ms.id);
            	}
                rpcs.flush();
            return (ms);
        }

        public void close() throws IOException
        {
            rpcs.close();
        }

        public ByteStream getByteStream()
        {
            return (rpcs);
        }
    }

