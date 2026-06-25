package com.uniinformation.rpccall;

import java.io.*;
import java.util.*;
import java.net.*;
import java.math.*;
import java.lang.reflect.*;

import com.kyoko.common.ChineseConvert;
import com.kyoko.common.DateUtil;
import com.uniinformation.cell.Cell;
import com.uniinformation.rpccall.*;
import com.uniinformation.utils.UniLog;

// public class RpcServerConnection extends Thread implements Valuable,RpcConst
public class RpcServerConnection implements Valuable, RpcConst, Runnable,
		RpcClientCallableWithCallback {
	private int min_token_len = 0;
	private Hashtable tokenHash;
	private boolean tokenSupport = false;
	private int tokenidx = 0;
	private boolean simToTran = false;
	private boolean nowalker;
	private Hashtable walkerHash, methodsHash;
	private String defaultServletClassName = null;
	private int varArgStart;
	private int rpcserialnum = 0;
	private Socket sock;
	private MyDataInputStream is;
	private MyDataOutputStream os;
	private int timeoutMSec = 60000;
	private RpcServletProvider provider;
	private Hashtable servletInUse = new Hashtable(1);
	private RpcClient rpcclient = null; // either rpcserver or rpcclient is not
										// null
	private RpcServer rpcserver = null;
	private boolean returnFlag = true;
	private boolean noAckFlag = false;
	private boolean debug = true;
	private int debugcnt = 2;
	private Date lastCallTime;
	/*
	 * private String rpc_encoding = new String("BIG5"); private String
	 * rpc_read_encoding = new String("BIG5");
	 */
	private String rpc_encoding = null;
	private String rpc_read_encoding = null;
	private boolean exitFlag = false;
	private boolean keepFlag = false;
	private RpcFlowControl flowcontrol = null;

	public void rpcflush() {
		if (os != null) {
			try {
				os.flush();
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
	}

	public void setFlowControl(RpcFlowControl p_flowcontrol) {
		flowcontrol = p_flowcontrol;
	}

	public void setSimToTran(boolean p_flag) {
		simToTran = p_flag;
	}

	public void setDefaultServletClassName(String p_classname) {
		UniLog.logClass(this, p_classname);
		defaultServletClassName = p_classname;
	}

	private void chkJVMVersion() {
		/*
		 * String vmversion = System.getProperty("java.version");
		 */
		String vmvendor = System.getProperty("java.vendor");
		// if (debug) UniLog.logClass(this, vmvendor);
		if (vmvendor.startsWith("Microsoft Corp.")
				|| vmvendor.startsWith("Netscape")) {
			/*
			 * rpc_encoding = new String("ISO-8859-1"); rpc_read_encoding = new
			 * String("ISO-8859-1");
			 */
			// UniLog.logClass(this, "set encoding to Big5");
			/*
			 * rpc_encoding = new String("Big5"); rpc_read_encoding = new
			 * String("Big5");
			 */
			/*
			 * rpc_encoding = new String("MS950"); rpc_read_encoding = new
			 * String("MS950");
			 */
			rpc_encoding = new String("MS950_HKSCS");
			rpc_read_encoding = new String("MS950_HKSCS");
		} else {
			/*
			 * rpc_encoding = new String("BIG5"); rpc_read_encoding = new
			 * String("BIG5");
			 */
			rpc_encoding = new String("MS950_HKSCS");
			rpc_read_encoding = new String("MS950_HKSCS");
		}
	}

	public RpcServerConnection(RpcServletProvider r) {
		provider = r;
		chkJVMVersion();
		String s = System
				.getProperty("com.uniinformation.rpccall.nomethodwalker");
		if (s == null) {
			nowalker = false;
		} else {
			nowalker = s.equals("Y");
		}
		// UniLog.logClass(this,"nomethodwalker " + nowalker);
		if (nowalker == false) {
			walkerHash = new Hashtable();
			methodsHash = new Hashtable();
		}
	}

	public RpcServer getRpcServer() {
		return (rpcserver);
	}

	public void setRpcServer(RpcServer p_rpcserver) {
		rpcserver = p_rpcserver;
	}

	public RpcClient getRpcClient() {
		return (rpcclient);
	}

	public void setRpcClient(RpcClient p_rpcclient) {
		rpcclient = p_rpcclient;
	}

	public boolean setTimeOut(int tmout) {
		timeoutMSec = tmout;
		if (sock != null) {
			try {
				sock.setSoTimeout(timeoutMSec);
			} catch (SocketException e) {
				return (false);
			}
		}
		return (true);
	}

	public void setTokenSupport(boolean sw) {
		tokenSupport = sw;
		if (tokenHash == null)
			tokenHash = new Hashtable();
		UniLog.log("set tokenSupport to " + sw);
	}

	public void addToken(String p_str) {
		if (tokenHash.get(p_str) != null)
			return;
		RpcToken rtoken = new RpcToken(p_str, 0);
		tokenHash.put(p_str, rtoken);
	}

	public void setSocket(Socket s) {
		sock = s;
		try {
			sock.setSoTimeout(timeoutMSec);

			sock.setTcpNoDelay(true);
			if (debug) {
				UniLog.logClass( this, "Rpc Connection Turn On Tcp_No_Delay " + sock.getSendBufferSize());
			}
			// sock.setSendBufferSize(8192);

			is = new MyDataInputStream(new BufferedInputStream(
					sock.getInputStream(), 2048));
			os = new MyDataOutputStream(new BufferedOutputStream(
					sock.getOutputStream(), 8192));
		} catch (Exception e) {
			UniLog.log(e);
		}
	}

	public void removeServlet(String servicename) {
		RpcConnServlet s = (RpcConnServlet) servletInUse.get(servicename);
		if (s == null)
			return;
		/*
		 * if(s.returnToProvider()) provider.freeService(s.getServlet());
		 */
		servletInUse.remove(servicename);
		return;
	}

	public RpcServlet getServlet(String servicename) {
		RpcConnServlet s = (RpcConnServlet) servletInUse.get(servicename);
		if (s == null)
			return (null);
		return (s.getServlet());
	}

	public boolean setServlet(String servicename, RpcServlet r) {
		RpcConnServlet s = new RpcConnServlet(r, false);
		servletInUse.put(servicename, s);
		return true;
	}

	public boolean setServlet(String servicename, RpcServlet r, boolean flag) {
		RpcConnServlet s = new RpcConnServlet(r, flag);
		servletInUse.put(servicename, s);
		return true;
	}

	private RpcServlet searchFeature(String servicename) {
		/* if a servlet is provide by service provider, it is set to return to provider 
		 * that is the class will only instantiated on demand.
		 * Discoverd on 2024/03/18 by DT
		 * if the servlet is set by setservlet (normal called by rpccall client
		 * the return to provider is always false
		 */
		if (servicename == null)
			return (null);
		if (servicename.equals(""))
			return (null);
		// UniLog.logClass(this, "search Feature " + servicename + " " +
		// Thread.currentThread());
		RpcConnServlet rpcconnservlet = (RpcConnServlet) servletInUse
				.get(servicename);
		try {
			if (rpcconnservlet != null) {
				// rpcservlet.setConnection(this);
				return (rpcconnservlet.getServlet());
			}
		} catch (Exception e) {
			UniLog.log(e);
			return (null);
		}
		RpcServlet rpcservlet = provider.getService(servicename);
		if (rpcservlet != null) {
			rpcservlet.setConnection(this);
			rpcconnservlet = new RpcConnServlet(rpcservlet, true);
			servletInUse.put(servicename, rpcconnservlet);
		} else {
			UniLog.logClass(this, "Provider return null servlet");
		}
		return (rpcservlet);
	}

	final public void run() {
		if (debug)
			UniLog.logClass(this, "Connection runs " + Thread.currentThread());
		try {
			/*
			 * sock.setSoTimeout(timeoutMSec); is = new
			 * DataInputStream(sock.getInputStream()); os = new
			 * DataOutputStream(sock.getOutputStream());
			 */
			for (;;) {
				int cc = is.readByte();
				if (cc == 'H') {
					UniLog.logClass(this, "Request to use HexMode");
					is.setHexMode(true);
					os.setHexMode(true);
					continue;
				}
				if (cc != SOH) {
					UniLog.logClass(this, "Protocol error cannot get SOH");
					// changed by lai Fri Aug 10 23:30:31 HKG 2001 return;
					break;
				}
				realRpcServer();
				if (exitFlag)
					break;
			}
		} catch (java.io.InterruptedIOException ioe) {
			UniLog.logClass(this, this + " timeout");
		} catch (Exception e) {
		}

		if (!keepFlag)
			stopService();
		if (rpcclient != null)
			rpcclient.afterExecute(this);
		else if (rpcserver != null)
			rpcserver.afterExecute(this);
		if (debug)
			UniLog.logClass(this, "Connection Gone" + Thread.currentThread());
		return;
	}

	public void setReturnFlag(boolean b) {
		returnFlag = b;
	}

	public void setNoAckFlag(boolean b) {
		noAckFlag = b;
	}

	private void releaseServlet() {
		if (servletInUse == null)
			return;
		for (Enumeration e = servletInUse.elements(); e.hasMoreElements();) {
			RpcConnServlet rpcconnservlet = (RpcConnServlet) e.nextElement();
			if (rpcconnservlet.returnToProvider())
				provider.freeService(rpcconnservlet.getServlet());
		}
		servletInUse.clear();
	}

	private Enumeration getdirlist() {
		Vector v = new Vector();
		try {
			String dirlist;
			FileInputStream in = null;
			String homedir = System.getProperty("java.home");
			String separator = System.getProperty("file.separator");

			Properties prop = new Properties();
			try {
				in = new FileInputStream("." + separator
						+ "rpcserver.properties");
			} catch (Exception e) {
				// UniLog.log(e);
			}
			try {
				if (in == null)
					in = new FileInputStream(homedir + separator + "lib"
							+ separator + "rpcserver.properties");
			} catch (Exception e) {
				// UniLog.log(e);
			}
			if (in != null) {
				prop.load(in);
				dirlist = prop.getProperty("rpccalldir");
			} else
				dirlist = System.getProperty("java.io.tmpdir");
			if (dirlist != null) {
				int sidx = 0;
				int eidx = 0;
				for (;;) {
					eidx = dirlist.indexOf(';', sidx);
					if (eidx < 0)
						eidx = dirlist.length();
					v.addElement(dirlist.substring(sidx, eidx));
					sidx = eidx + 1;
					if (sidx >= dirlist.length())
						break;
				}
			}
			if (in != null)
				in.close();
		} catch (Exception e) {
			UniLog.log(e);
		}
		v.add("/yic/v/unidev/wxcache");
		return v.elements();
	}

	private int matchdirlist(String fname) {
		UniLog.log("matchdirlist skipped.");
		return (1);
		/*
		 * File file = new File(fname); String parent = file.getParent();
		 * for(Enumeration e = getdirlist();e.hasMoreElements();) { String s =
		 * (String) e.nextElement(); // if(parent.compareTo(s)==0)
		 * if(parent.startsWith(s)) return 1; } return 0;
		 */
	}

	public int putFile(InputStream input, int maxlen, String filename,
			int offset, int maxrate) {
		int cc;
		try {
			lastCallTime = new Date();
			os.writeByte(SOH);
			os.writeByte(RPCPUTFILEID);
			os.writeInt(offset);
			byte[] buf;
			if (simToTran) {
				buf = ChineseConvert.convertB2G(filename)
						.getBytes(rpc_encoding);
			} else {
				buf = filename.getBytes(rpc_encoding);
			}
			os.writeShort(buf.length);
			os.write(buf, 0, buf.length);
			os.writeByte(STX);
			os.flush();
			sock.setSoTimeout(RPCACKTIMEOUT * 1000);
			cc = is.readByte();
			// sock.setSoTimeout(0);
			sock.setSoTimeout(timeoutMSec);
			switch (cc) {
			case ACK:
				cc = rpcserver_putfile(input, maxlen, maxrate);
				if (cc < 0)
					return (-1);
				return (0);
			case NAK:
				Value v = readValue();
				if (v == null)
					return (-1);
				UniLog.logClass(this, "get fail 1 " + v.toString());
				v = null;
				return (-1);
			}
		} catch (IOException ioe) {
			UniLog.logClass(this, "get fail 2 " + ioe.getMessage());
			return (-1);
		}
		return (-1);
	}

	public int putFile(DataInput dataInput, int maxlen, String filename,
			int offset, int maxrate) {
		int cc;
		try {
			lastCallTime = new Date();
			os.writeByte(SOH);
			os.writeByte(RPCPUTFILEID);
			os.writeInt(offset);
			byte[] buf;
			if (simToTran) {
				buf = ChineseConvert.convertB2G(filename)
						.getBytes(rpc_encoding);
			} else {
				buf = filename.getBytes(rpc_encoding);
			}
			os.writeShort(buf.length);
			os.write(buf, 0, buf.length);
			os.writeByte(STX);
			os.flush();
			sock.setSoTimeout(RPCACKTIMEOUT * 1000);
			cc = is.readByte();
			// sock.setSoTimeout(0);
			sock.setSoTimeout(timeoutMSec);
			switch (cc) {
			case ACK:
				cc = rpcserver_putfile(dataInput, maxlen, maxrate);
				if (cc < 0)
					return (-1);
				return (0);
			case NAK:
				Value v = readValue();
				if (v == null)
					return (-1);
				UniLog.logClass(this, "get fail 3 " + v.toString());
				v = null;
				return (-1);
			}
		} catch (IOException ioe) {
			UniLog.log(ioe);
			return (-1);
		}
		return (-1);
	}

	private int rpcserver_putfile(DataInput input, int maxlen, int maxrate) {
		int writecnt = 0;
		boolean eof = false;
		try {
			byte putbuf[] = new byte[RPCPUTLEN];
			for (; eof;) {
				int readcnt;
				for (readcnt = 0; readcnt < RPCPUTLEN; readcnt++) {
					try {
						putbuf[readcnt] = input.readByte();
					} catch (EOFException e2) {
						eof = true;
						break;
					}
				}
				// int readcnt = input.read(putbuf);
				if (readcnt <= 0)
					break;
				if (readcnt > 0) {
					writecnt += readcnt;
					os.writeInt(readcnt);
					os.write(putbuf, 0, readcnt);
				}
			}
			os.writeInt(0);
			os.flush();
			if (is.readByte() != EOT)
				throw new RpcException("Cannot get EOT");
		} catch (IOException e) {
			UniLog.logClass(this,
					"rpcserver_putfile I/O error " + e.getMessage());
			UniLog.log(e);
			return (-8);
		} catch (RpcException e) {
			UniLog.logClass(this, "rpcserver_putfile error " + e.getMessage());
			UniLog.log(e);
			return (-10);
		}
		return (writecnt);
	}

	private int rpcserver_putfile(InputStream inputstream, int maxlen,
			int maxrate) {
		int writecnt = 0;
		try {
			byte putbuf[] = new byte[RPCPUTLEN];
			for (;;) {
				int readcnt = inputstream.read(putbuf);
				if (readcnt < 0)
					break;
				if (readcnt > 0) {
					writecnt += readcnt;
					os.writeInt(readcnt);
					os.write(putbuf, 0, readcnt);
				}
				if (readcnt == 0) {
					UniLog.logClass(this,
							"rpcserver_putfile read file readcnt=0");
				}
			}
			os.writeInt(0);
			os.flush();
			if (is.readByte() != EOT)
				throw new RpcException("Cannot get EOT");
		} catch (IOException e) {
			UniLog.logClass(this,
					"rpcserver_putfile I/O error " + e.getMessage());
			UniLog.log(e);
			return (-8);
		} catch (RpcException e) {
			UniLog.logClass(this, "rpcserver_putfile error " + e.getMessage());
			UniLog.log(e);
			return (-10);
		}
		return (writecnt);
	}

	public int getFile(DataOutput dataOutput, int maxlen, String filename,
			int offset, int maxrate) {
		int cc;
		try {
			lastCallTime = new Date();
			os.writeByte(SOH);
			os.writeByte(RPCGETFILEID);
			os.writeInt(offset);
			byte[] buf;
			if (simToTran) {
				buf = ChineseConvert.convertB2G(filename)
						.getBytes(rpc_encoding);
			} else {
				buf = filename.getBytes(rpc_encoding);
			}
			os.writeShort(buf.length);
			os.write(buf, 0, buf.length);
			os.writeByte(STX);
			os.writeInt(maxlen);
			os.writeInt(maxrate);
			os.flush();
			sock.setSoTimeout(RPCACKTIMEOUT * 1000);
			cc = is.readByte();
			// sock.setSoTimeout(0);
			sock.setSoTimeout(timeoutMSec);
			switch (cc) {
			case ACK:
				cc = rpcserver_getfile(dataOutput);
				if (cc < 0)
					return (-1);
				return (0);
			case NAK:
				Value v = readValue();
				if (v == null)
					return (-1);
				UniLog.logClass(this, "get fail 4 " + v.toString());
				v = null;
				return (-1);
			}
		} catch (IOException ioe) {
			UniLog.logClass(this, "get fail 5 " + ioe.getMessage());
			return (-1);
		}
		return (-1);
	}

	public int getFile(OutputStream out, int maxlen, String filename,
			int offset, int maxrate) {
		int cc;
		try {
			lastCallTime = new Date();
			os.writeByte(SOH);
			os.writeByte(RPCGETFILEID);
			os.writeInt(offset);
			byte[] buf;
			if (simToTran) {
				buf = ChineseConvert.convertB2G(filename)
						.getBytes(rpc_encoding);
			} else {
				buf = filename.getBytes(rpc_encoding);
			}
			os.writeShort(buf.length);
			os.write(buf, 0, buf.length);
			os.writeByte(STX);
			os.writeInt(maxlen);
			os.writeInt(maxrate);
			os.flush();
			sock.setSoTimeout(RPCACKTIMEOUT * 1000);
			cc = is.readByte();
			// sock.setSoTimeout(0);
			sock.setSoTimeout(timeoutMSec);
			switch (cc) {
			case ACK:
				cc = rpcserver_getfile(out);
				if (cc < 0)
					return (-1);
				return (0);
			case NAK:
				Value v = readValue();
				if (v == null)
					return (-1);
				UniLog.logClass(this, "get fail 6 " + v.toString());
				v = null;
				return (-1);
			}
		} catch (IOException ioe) {
			UniLog.logClass(this, "get fail 7 " + ioe.getMessage());
			return (-1);
		}
		return (-1);
	}

	private int rpcserver_getfile(DataOutput outputstream) {
		try {
			for (;;) {
				int len;
				int b = is.readByte();
				switch (b) {
				case 0:
					break;
				case 1:
					if (realRpcServer() >= 0) {
						continue;
					}
					throw new RpcException(
							"rpcserver_getfile 1 rpcserver() fail");
				default:
					throw new RpcException(
							"rpcserver_getfile 1 Undefined state " + b);
				}
				len = readSerial();
				// len = is.readInt();
				if (len < 0)
					return (-1);
				if (len == 0)
					break;
				byte getbuf[] = new byte[len];
				for (; len > 0;) {
					int cc = is.read(getbuf, 0, len);
					len -= cc;
					outputstream.write(getbuf, 0, cc);
				}
			}
			os.writeByte(EOT);
			os.flush();
		} catch (IOException e) {
			UniLog.logClass(this, "rpcserver_getfile " + e.getMessage());
			UniLog.log(e);
			return (-1);
		}
		return (0);
	}

	private int rpcserver_getfile(OutputStream outputstream) {
		try {
			for (;;) {
				int len;
				int b = is.readByte();
				switch (b) {
				case 0:
					break;
				case 1:
					if (realRpcServer() >= 0) {
						continue;
					}
					throw new RpcException(
							"rpcserver_getfile 1 rpcserver() fail");
				default:
					throw new RpcException(
							"rpcserver_getfile 1 Undefined state " + b);
				}
				len = readSerial();
				// len = is.readInt();
				if (len < 0)
					return (-1);
				if (len == 0)
					break;
				byte getbuf[] = new byte[len];
				for (; len > 0;) {
					int cc = is.read(getbuf, 0, len);
					len -= cc;
					outputstream.write(getbuf, 0, cc);
				}
			}
			os.writeByte(EOT);
			os.flush();
		} catch (IOException e) {
			UniLog.logClass(this, "rpcserver_getfile " + e.getMessage());
			UniLog.log(e);
			return (-1);
		}
		return (0);
	}

	private Method checkVarArg(Class myclass, Class margclasses[],
			String mymethodname, Object margobj[]) {
		Method allmethods[] = myclass.getMethods();
		for (int i = 0; i < allmethods.length; i++) {
			Method m = allmethods[i];
			if (m.getName().compareTo(mymethodname) == 0) {
				Class ptype[] = m.getParameterTypes();
				for (int j = 0; j < ptype.length; j++) {
					String pname = ptype[j].getName();
					if (pname.equals("java.lang.Double")
							|| pname.equals("double")) {
						String cname = margclasses[j].getName();
						if (cname.equals("java.lang.Double") == false
								&& cname.equals("java.lang.Integer") == false)
							return null;
						if (cname.equals("java.lang.Integer")) {
							Integer in = (Integer) margobj[j];
							margobj[j] = new Double(in.doubleValue());
						}
					} else if (pname.equals("java.lang.Integer")
							|| pname.equals("int")) {
						String cname = margclasses[j].getName();
						if (cname.equals("java.lang.Double") == false
								&& cname.equals("java.lang.Integer") == false)
							return null;
						if (cname.equals("java.lang.Double")) {
							Double d = (Double) margobj[j];
							margobj[j] = new Integer(d.intValue());
						}
					} else if (pname.compareTo("java.util.Vector") == 0) {
						for (varArgStart = 0; varArgStart < j; varArgStart++) {
							if (ptype[varArgStart] != margclasses[varArgStart]) {
								String cname = margclasses[varArgStart]
										.getName();
								String pname2 = ptype[varArgStart].getName();
								if (pname2.equals("java.lang.Integer")
										|| pname2.equals("int")) {
									if (cname.equals("java.lang.Double") == false
											&& cname.equals("java.lang.Integer") == false) {
										UniLog.logClass(this,
												"variable return null 1");
										return null;
									}
								} else if (pname2.equals("java.lang.Double")
										|| pname2.equals("double")) {
									if (cname.equals("java.lang.Double") == false
											&& cname.equals("java.lang.Integer") == false) {
										UniLog.logClass(this,
												"variable return null 2");
										return null;
									}
								} else {
									UniLog.logClass(this, "argument "
											+ varArgStart + " " + pname2);
									UniLog.logClass(this,
											"variable return null 3");
									return null;
								}
							}
						}
						if (varArgStart == j) {
							return (m);
						}
					}
				}
				return m;
			}
		}
		return null;
	}

	private Object invokeServlet(RpcServlet serv, String methodname,
			Class[] margclasses, Object[] margobjects) throws Exception {
		Object retobj = null;
		Method mymethod;
		int i;

		if (nowalker) {
			// Class myclass = Class.forName(myclassname);
			Class myclass = serv.getClass();
			try {
				mymethod = myclass.getMethod(methodname, margclasses);
				retobj = mymethod.invoke(serv, margobjects);
			} catch (NoSuchMethodException nsmE) {
				varArgStart = -1;
				UniLog.log("checkVarArg 1");
				mymethod = checkVarArg(myclass, margclasses, methodname,
						margobjects);
				UniLog.log("checkVarArg 2");
				if (mymethod == null) {
					UniLog.logClass(this, "myclassname=" + myclass.getName());
					UniLog.logClass(this, "methodname=" + methodname);
					for (int z = 0; z < margobjects.length; z++) {
						UniLog.logClass(this, "margclasses[" + z + "]="
								+ margclasses[z]);
					}
					throw new RpcException("Not a variable argument method "
							+ myclass.getName() + "." + methodname);
				}
				if (varArgStart >= 0) {
					int numarg = margobjects.length;
					Vector varArgList = new Vector(numarg - varArgStart);
					for (i = varArgStart; i < numarg; i++)
						varArgList.addElement(margobjects[i]);
					Object methodVarArgList[] = new Object[varArgStart + 1];
					for (i = 0; i < varArgStart; i++)
						methodVarArgList[i] = margobjects[i];
					methodVarArgList[varArgStart] = varArgList;
					retobj = mymethod.invoke(serv, methodVarArgList);
				} else {
					retobj = mymethod.invoke(serv, margobjects);
				}
			}
		} else {
			String myclassname = serv.getClass().getName();
			String methodFullName = myclassname + "." + methodname;
			MethodWalker walker = (MethodWalker) walkerHash.get(methodFullName);
			if (walker == null) {
				Method methods[] = (Method[]) methodsHash.get(myclassname);
				if (methods == null) {
					methods = Class.forName(myclassname).getMethods();
					methodsHash.put(myclassname, methods);
				}
				if (debug)
					UniLog.log("start build " + methodFullName);
				walker = new MethodWalker(methods, methodname);
				if (debug)
					UniLog.log("end build " + methodFullName);
				walkerHash.put(methodFullName, walker);
			}
			// if(debug) UniLog.log("start search " + methodFullName);
			mymethod = walker.searchMethodTree(margobjects);
			// if(debug) UniLog.log("end search " + methodFullName);
			if (mymethod == null) {
				UniLog.logClass(this, "method=" + methodFullName);
				for (int z = 0; z < margobjects.length; z++) {
					UniLog.logClass(this, "margclasses[" + z + "]="
							+ margclasses[z]);
				}
				throw new RpcException("Method not found " + methodFullName);
			}
			retobj = mymethod.invoke(serv,
					walker.transformParameters(mymethod, margobjects));
		}
		return (retobj);
	}

	private int realRpcServer() {
		int cc;
		try {
			int rpccallid = is.readByte();
			int rpcserial = is.readInt();
			int offset;
			short len = is.readShort();
			if (len <= 0)
				throw new RpcException("callname len = " + len + " i.e. <=0");
			byte buf[] = new byte[len];
			/* is.read(buf); */
			offset = 0;
			for (; len > 0;) {
				int rlen;
				rlen = is.read(buf, offset, len);
				len -= rlen;
				offset += rlen;
			}
			// String callname = new String(buf);
			String callname = null;
			if (rpccallid == RPCPUTFILEID || rpccallid == RPCGETFILEID
					|| rpccallid == RPCGETFILEID2
					|| rpccallid == RPCGETFILEIDWITHTIME)
				callname = new String(buf, rpc_read_encoding);
			else
				callname = new String(buf);
			if (is.readByte() != STX)
				throw new RpcException("Cannot get STX");
			if (rpccallid == RPCPUTFILEID) {
				RandomAccessFile outputstream = null;
				FileOutputStream fileoutputstream = null;
				try {
					if (matchdirlist(callname) == 0)
						throw new RpcException("Permission Denied.");
					if (rpcserial > 0) {
						outputstream = new RandomAccessFile(callname, "rw");
						outputstream.seek(rpcserial);
					} else {
						fileoutputstream = new FileOutputStream(callname);
					}
				} catch (Exception e) {
					if (outputstream != null)
						outputstream.close();
					Strval v = new Strval("FAIL [" + callname + "] "
							+ e.getMessage());
					os.writeByte(NAK);
					writeValue(v);
					os.flush();
					return (0);
				}
				callname = null;
				os.writeByte(ACK);
				os.flush();
				if (outputstream != null) {
					cc = rpcserver_getfile(outputstream);
					outputstream.close();
					outputstream = null;
				} else {
					cc = rpcserver_getfile(fileoutputstream);
					fileoutputstream.close();
					fileoutputstream = null;
				}
				return (cc);
			}
			if (rpccallid == RPCGETFILEID || rpccallid == RPCGETFILEID2
					|| rpccallid == RPCGETFILEIDWITHTIME) {
				int kbyte = 0;
				String callbackname = null;
				Vector arglist = null;
				int maxlen = is.readInt();
				int maxrate = is.readInt();
				if (rpccallid == RPCGETFILEID2) {
					kbyte = is.readInt();
					len = is.readShort();
					buf = new byte[len];
					offset = 0;
					for (; len > 0;) {
						int rlen;
						rlen = is.read(buf, offset, len);
						len -= rlen;
						offset += rlen;
					}
					callbackname = new String(buf);
					int reserved = is.readInt();
					int argcnt = is.readShort();
					if (argcnt > 0)
						arglist = new Vector();
					for (int i = 0; i < argcnt; i++) {
						Value v;
						v = readValue();
						if (v == null)
							throw new RpcException("Argument count mismatch");
						arglist.addElement(valToObject(v));
					}
				}
				FileInputStream inputstream = null;
				try {
					if (matchdirlist(callname) == 0) {
						throw new RpcException("Permission Denied.");
					}
					inputstream = new FileInputStream(callname);
					inputstream.skip(rpcserial);
					if (rpccallid == RPCGETFILEIDWITHTIME) {
						UniLog.log("sendFileTime true, sending DC2 and filetime");
						File f = new File(callname);
						os.writeByte(DC2);
						os.writeInt((int) (f.lastModified() / 1000));
					}
					os.writeByte(ACK);
					os.flush();
					if (rpccallid == RPCGETFILEID
							|| rpccallid == RPCGETFILEIDWITHTIME)
						cc = rpcserver_putfile(inputstream, maxlen, maxrate);
					else
						cc = rpcserver_putfile_with_callback(inputstream,
								maxlen, maxrate, kbyte, "R", callbackname,
								arglist);
					inputstream.close();
					return (cc);
				} catch (Exception e) {
					UniLog.log(e);
					if (inputstream != null)
						inputstream.close();
					Strval v = new Strval("FAIL [" + callname + "] "
							+ e.getMessage());
					os.writeByte(NAK);
					writeValue(v);
					os.flush();
					return (0);
				}
			}
			short numarg = is.readShort();
			if (numarg < 0)
				throw new RpcException("Argument count < 0");
			Vector arglist = new Vector();
			for (int i = 0; i < numarg; i++) {
				Value v;
				v = readValue();
				if (v == null)
					throw new RpcException("Argument count mismatch");
				arglist.addElement(v);
			}
			if (is.readByte() != ETX)
				throw new RpcException("Cannot get ETX");
			if (rpccallid != RPCCALLSEGMENTID_NORETURN
					&& rpccallid != RPCCALLSEGMENTID_NOACK) {
				os.writeByte(ACK);
				os.flush();
			}
			Value v = null;
			// UniLog.logClass(this, "callname " + callname);
			StringTokenizer st = new StringTokenizer(callname, ".");
			int tokencnt = st.countTokens();
			String myclassname = "";
			String methodname = "";
			RpcServlet serv;
			if (tokencnt == 1) {
				if (defaultServletClassName != null) {
					myclassname = defaultServletClassName;
					methodname = callname;
				} else {
					myclassname = callname;
					methodname = "execute";
				}
				// UniLog.logClass(this, "Method " + methodname);
				serv = searchFeature(myclassname);
			} else {
				for (int i = 1; i < tokencnt; i++) {
					myclassname += st.nextToken();
					if (i < tokencnt - 1)
						myclassname += ".";
				}
				methodname = st.nextToken();
				String instanceName = "";
				int c = methodname.indexOf(':');
				// UniLog.log("HAHA 12345 " + methodname + " " + c);
				if (c >= 0) {
					instanceName = "." + methodname.substring(0, c + 1);
					methodname = methodname.substring(c + 1);
				}
				// UniLog.logClass(this, "Method " + methodname);
				// UniLog.log("HAHA 123456 [" +
				// myclassname+","+instanceName+"]");
				serv = searchFeature(myclassname + instanceName);
			}
			if (serv == null) {
//				UniLog.logClass(this, "searchFeature fail " + myclassname);
//				/* Tue Apr 9 16:01:11 HKG 2002 WinG */
//				v = new Strval("FAILNo such a service " + callname);
//				os.writeByte(NAK);
//				writeValue(v);
//				os.flush();
//				return (-1);

				arglist = null;
				if (rpccallid != RPCCALLSEGMENTID_NORETURN) {
					UniLog.log("rpc " + callname + " not found");
					os.writeInt(rpcserial);
					if (v == null)
						v = new Longval(-1);
					writeValue(v);
					os.writeByte(EOT);
					os.flush();
				}
				return(0);
			}
			Class margclasses[] = new Class[numarg];
			Object margobjects[] = new Object[numarg];
			try {
				int i;
				for (i = 0; i < numarg; i++) {
					Value val = (Value) arglist.elementAt(i);
					switch (val.valType()) {
					case ISIVALUE:
						margclasses[i] = Class.forName("java.lang.Integer");
						Longval ival = (Longval) val;
						margobjects[i] = new Integer(ival.toInt());
						break;
					case ISRVALUE:
						margclasses[i] = Class.forName("java.lang.Double");
						Realval rval = (Realval) val;
						margobjects[i] = new Double(rval.toDouble());
						break;
					case ISNULVALUE:
						Nulval nulval = (Nulval) val;
						switch (nulval.getNulType()) {
						case ISIVALUE:
							margclasses[i] = Class.forName("java.lang.Integer");
							break;
						case ISRVALUE:
							margclasses[i] = Class.forName("java.lang.Double");
							break;
						case ISSVALUE:
							margclasses[i] = Class.forName("java.lang.String");
							break;
						case ISDVALUE:
							margclasses[i] = Class.forName("java.util.Date");
							break;
						case ISBUFVALUE:
							margclasses[i] = Class.forName("[B");
							break;
						}
						margobjects[i] = null;
						break;
					case ISBUFVALUE:
						/* margclasses[i] = Class.forName("[Ljava.lang.Byte;"); */
						margclasses[i] = Class.forName("[B");
						Bufval bval = (Bufval) val;
						margobjects[i] = bval.getBuffer();
						break;
					case ISSVALUE:
						margclasses[i] = Class.forName("java.lang.String");
						Strval sval = (Strval) val;
						margobjects[i] = new String(sval.toString());
						break;
					case ISBVALUE:
						break;
					case ISDVALUE:
						margclasses[i] = Class.forName("java.util.Date");
						Dateval dval = (Dateval) val;
						// modified by dt Mon Aug 23 19:59:53 HKG 2010
						// return null if perf date value <= 0
						// avoid parsing invalid date value to caller when
						// dateint <= 0 which
						// should means "no date set"
						// may cause caller to have error if caller dose not
						// handle null java.utils.date
						// value properly.
						// UniLog.log("HAHA in rpcserverconnection rpcservercallback "
						// + dval);
						if (dval.toInt() <= 0) {
							margobjects[i] = null;
						} else {
							// long l = dval.toInt() - 25568;
							// l *= 86400000;
							// l -= DateUtil.getGmtOffset();
							// Date tdate = new Date(l);

							Date tdate = DateUtil.informixToDate(dval.toInt());

							margobjects[i] = tdate;
							// UniLog.log("HAHA in rpcserverconnection rpcservercallback jd = "
							// + tdate);
						}
						break;
					}
				}
				Method mymethod = null;
				Object retobj = null;
				/*
				 * retobj =
				 * invokeServlet(serv,methodname,margclasses,margobjects);
				 */
				if (nowalker) {
					Class myclass = Class.forName(myclassname);
					try {
						mymethod = myclass.getMethod(methodname, margclasses);
						retobj = mymethod.invoke(serv, margobjects);
					} catch (NoSuchMethodException nsmE) {
						varArgStart = -1;
						UniLog.log("checkVarArg 1");
						mymethod = checkVarArg(myclass, margclasses,
								methodname, margobjects);
						UniLog.log("checkVarArg 2");
						if (mymethod == null) {
							UniLog.logClass(this, "myclassname=" + myclassname);
							UniLog.logClass(this, "methodname=" + methodname);
							for (int z = 0; z < margobjects.length; z++) {
								UniLog.logClass(this, "margclasses[" + z + "]="
										+ margclasses[z]);
							}
							throw new RpcException(
									"Not a variable argument method "
											+ methodname + ":" + callname);
						}
						if (varArgStart >= 0) {
							Vector varArgList = new Vector(numarg - varArgStart);
							for (i = varArgStart; i < numarg; i++)
								varArgList.addElement(margobjects[i]);
							Object methodVarArgList[] = new Object[varArgStart + 1];
							for (i = 0; i < varArgStart; i++)
								methodVarArgList[i] = margobjects[i];
							methodVarArgList[varArgStart] = varArgList;
							retobj = mymethod.invoke(serv, methodVarArgList);
						} else {
							retobj = mymethod.invoke(serv, margobjects);
						}
					}
				} else {

					String methodFullName = myclassname + "." + methodname;
					MethodWalker walker = (MethodWalker) walkerHash
							.get(methodFullName);
					if (walker == null) {
						Method methods[] = (Method[]) methodsHash
								.get(myclassname);
						if (methods == null) {
							methods = Class.forName(myclassname).getMethods();
							methodsHash.put(myclassname, methods);
						}
						if (debug)
							UniLog.log("start build " + methodFullName);
						walker = new MethodWalker(methods, methodname);
						if (debug)
							UniLog.log("end build " + methodFullName);
						walkerHash.put(methodFullName, walker);
					}
					if (debug)
						UniLog.log("start search " + methodFullName);
					mymethod = walker.searchMethodTree(margobjects);
					if (debug)
						UniLog.log("end search " + methodFullName);
					if (mymethod == null) {
						if (debug) {
							UniLog.logClass(this, "method=" + methodFullName);
							for (int z = 0; z < margobjects.length; z++) {
								UniLog.logClass(this, "margclasses[" + z + "]="
										+ margclasses[z]);
							}
						}
						throw new RpcException("Method not found " + methodname
								+ ":" + callname);
					}
					{
						Object obj[] = walker.transformParameters(mymethod,
								margobjects);
						retobj = mymethod.invoke(serv, obj);
						/*
						 * try { retobj = mymethod.invoke(serv,obj); } catch
						 * (IOException ex) { UniLog.logClass(this,
						 * "exteption trapped inside invoke"); UniLog.log(ex);
						 * return(-1); }
						 */
					}
				}

				Class retClass = mymethod.getReturnType();
				String rettype = mymethod.getReturnType().getName();

				if (debug)
					UniLog.logClass(this, "return type " + rettype);
				if (retClass
						.isAssignableFrom(Class.forName("java.lang.String"))) {
					String retstr = (String) retobj;
					v = new Strval(retstr);
				} else if (retClass.isAssignableFrom(Class
						.forName("java.lang.Integer"))) {
					Integer intret = (Integer) retobj;
					v = new Longval(intret.intValue());
				} else if (retClass.isAssignableFrom(Class
						.forName("java.lang.Double"))) {
					Double doubleret = (Double) retobj;
					v = new Realval(doubleret.doubleValue());
					Realval dval = (Realval) v;
				} else if (retClass.isAssignableFrom(Class
						.forName("java.lang.Void"))) {
					v = new Strval("");
				} else if (retClass.isAssignableFrom(Class
						.forName("java.util.Date"))) {
					Date dateret = (Date) retobj;
					// int l =
					// (int)((dateret.getTime()+DateUtil.getGmtOffset())/
					// 86400000) + 25568;

					// long ll = dateret.getTime() + DateUtil.getGmtOffset() +
					// 2209075200000L;
					// int l = (int) (ll / 86400000);
					// v = new Dateval(l);

					v = new Dateval(
							com.kyoko.common.DateUtil
									.dateToInformix(dateret));

				} else if (retClass.isAssignableFrom(Class
						.forName("com.uniinformation.rpccall.Value"))) {
					v = (Value) retobj;
				} else {
					if (retobj != null)
						v = new Strval(retobj.toString());
					else
						v = new Strval("");
				}
			} catch (Exception e) {
				UniLog.logClass(
						this,
						"invoke "
								+ e.getClass()
								+ (e.getMessage() != null ? (" " + e
										.getMessage()) : ""));
				if (debug)
					UniLog.log(e);
			}
			callname = null;
			arglist = null;
			if (rpccallid != RPCCALLSEGMENTID_NORETURN) {
				os.writeInt(rpcserial);
				if (v == null)
					v = new Longval(-1);
				writeValue(v);
				os.writeByte(EOT);
				os.flush();
			}
			cc = 0;
		} catch (IOException e) {
			UniLog.logClass(
					this,
					"I/O error for the connection to: "
							+ sock.getInetAddress().getHostAddress() + " "
							+ e.getMessage() + " " + timeoutMSec);
			UniLog.log(e);
			cc = -1;
		} catch (RpcException e) {
			UniLog.logClass(this, "Error in rpc protocol " + e.getMessage());
			UniLog.log(e);
			cc = -1;
		}
		return (cc);
	}
	public void stopService() {
		releaseServlet();
		if (sock == null)
			return;
		try {
			if (os != null)
				os.close();
			if (is != null)
				is.close();
			if (sock != null)
				sock.close();
		} catch (Exception e) {
			UniLog.logClass(this, "stop service fail");
		}
		provider = null;
		servletInUse = null;
		os = null;
		is = null;
		sock = null;
	}

	protected void finalize() {
		stopService();
	}

	private boolean writeValue(Value val) throws IOException {
		switch (val.valType()) {
		case ISTVALUE:
			TokenVal tokenval = (TokenVal) val;
			os.writeShort(V_TOKEN);
			os.writeInt(tokenval.toInt());
			break;
		case ISTSVALUE:
			TokenStringVal tsval = (TokenStringVal) val;
			os.writeShort(V_STRING_WITH_TOKEN);
			os.writeInt(tsval.toInt());
			byte[] mybuf;
			if (simToTran) {
				mybuf = ChineseConvert.convertB2G(tsval.toString()).getBytes(
						rpc_encoding);
			} else {
				mybuf = tsval.toString().getBytes(rpc_encoding);
			}
			os.writeShort(mybuf.length);
			os.write(mybuf, 0, mybuf.length);
			break;
		case ISSVALUE:
			Strval strval = (Strval) val;
			byte[] buf;
			if (strval.getEncoding() != null) {
				byte[] buf2;
				{
					String ss = strval.toString();
					char cr[] = ss.toCharArray();
					for (int i = 0; i < cr.length; i++) {
						int ci;
						ci = cr[i];
						UniLog.log("UTF8 convert C " + i + " " + ci);
					}
				}
				/*
				 * if(simToTran) { buf2 =
				 * ChineseConvert.convertB2G(strval.toString
				 * ()).getBytes(strval.getEncoding()); } else { buf2 =
				 * strval.toString().getBytes(strval.getEncoding()); }
				 */
				buf2 = strval.toString().getBytes(strval.getEncoding());
				// assument UTF8
				buf = new byte[buf2.length + 2];
				buf[0] = -1;
				if (strval.getEncoding().equals("SJIS"))
					buf[1] = 'j';
				else
					buf[1] = 'u';
				UniLog.log("HAHA_UTF8 A " + buf2.length);
				for (int i = 0; i < buf2.length; i++) {
					UniLog.log("HAHA_UTF8 B " + i + " " + buf2[i]);
					buf[i + 2] = buf2[i];
				}
			} else {
				if (simToTran) {
					buf = ChineseConvert.convertB2G(strval.toString())
							.getBytes(rpc_encoding);
				} else {
					buf = strval.toString().getBytes(rpc_encoding);
				}
			}
			if (buf.length > 32767) {
				os.writeShort(V_LONGSTR);
				os.writeInt(buf.length);
			} else {
				os.writeShort(V_STRING);
				os.writeShort(buf.length);
			}
			os.write(buf, 0, buf.length);
			break;
		case ISRVALUE:
			os.writeShort(V_FLOAT);
			long longbits = Double.doubleToLongBits(val.toDouble());
			for (int i = 0; i < 8; i++) {
				os.writeByte((byte) (longbits & 0xff));
				longbits >>= 8;
			}
			/*
			 * for(int i=0;i<8;i++) { long t = longbits >> ((7-i)*8);
			 * os.writeByte((byte)(t)); }
			 */
			break;
		case ISIVALUE:
			os.writeShort(V_LONG);
			os.writeInt(val.toInt());
			break;
		case ISDVALUE:
			os.writeShort(V_DATE);
			os.writeInt(val.toInt());
			break;
		case ISBUFVALUE:
			byte ba[] = ((Bufval) val).getBuffer();
			os.writeShort(V_BYTEARR);
			// Fri Aug 4 16:03:48 HKG 2006
			// use short instead of int , compatible with perf bytearr
			// os.writeInt(ba.length);
			os.writeShort((short) ba.length);
			for (int i = 0; i < ba.length; i++)
				os.writeByte(ba[i]);
			break;
		default:
			return (false);
		}
		return (true);
	}

	private Value readValue() throws IOException {
		int cc;
		int offset;
		int len;
		cc = is.readShort();
		// UniLog.logClass(this, "readvalue type = " + cc);
		switch (cc) {
		case V_NULL:
			short t = is.readShort();
			return (new Nulval(t));
		case V_BYTEARR:
			len = is.readShort();
			if (len >= 0) {
				byte buf[] = new byte[len];
				offset = 0;
				for (; len > 0;) {
					int rlen;
					rlen = is.read(buf, offset, len);
					len -= rlen;
					offset += rlen;
				}
				return (new Bufval(buf));
			} else
				return (null);
		case V_STRING:
		case V_LONGSTR:
			if (cc == V_STRING) {
				len = is.readShort();
			} else {
				len = is.readInt();
			}
			if (len > 0) {
				byte buf[] = new byte[len];
				offset = 0;
				/* is.read(buf); */
				for (; len > 0;) {
					int rlen;
					rlen = is.read(buf, offset, len);
					len -= rlen;
					offset += rlen;
				}
				if (offset >= 2 && buf[0] == -1 /* && buf[1] == 'u' */) {
					switch (buf[1]) {
					case 'j':
						UniLog.log("SJIS convert");
						return (new Strval(new String(buf, 2, offset - 2,
								"SJIS")));
					default:
						UniLog.log("UTF8 convert");
						/*
						 * return(new Strval(new
						 * String(buf,2,offset-2,"UTF8")));
						 */
						{
							for (int i = 0; i < buf.length; i++) {
								UniLog.log("UTF8 convert A " + i + " " + buf[i]);
							}
							String ss = new String(buf, 2, offset - 2, "UTF8");
							char cr[] = ss.toCharArray();
							for (int i = 0; i < cr.length; i++) {
								int ci;
								ci = cr[i];
								UniLog.log("UTF8 convert B " + i + " " + ci);
							}
							return (new Strval(ss));
						}
					}
				} else {
					if (simToTran) {
						return (new Strval(
								ChineseConvert.convertG2B(new String(buf,
										rpc_read_encoding))));
					} else {
						// return(new Strval(new
						// String(buf,rpc_read_encoding),rpc_read_encoding));
						return (new Strval(new String(buf, rpc_read_encoding)));
					}
				}
			} else if (len == 0) {
				return (new Strval(""));
			} else
				return (null);
		case V_LONG:
		case V_INTEGER:
			return (new Longval(is.readInt()));
		case V_DATE:
			return (new Dateval(is.readInt()));
		case V_FLOAT:
			long longbits = 0;
			for (int i = 0; i < 64; i += 8) {
				/*
				 * byte t = is.readByte(); long tlong = (t & 0xff);
				 */
				long tlong = is.readUnsignedByte();
				tlong <<= i;
				longbits |= tlong;
			}
			return (new Realval(Double.longBitsToDouble(longbits)));
		default:
			return (null);
		}
	}

	public void setWriteEncoding(String encoding) {
		rpc_encoding = new String(encoding);
	}

	public String getWriteEncoding() {
		return (rpc_encoding);
	}

	public void setReadEncoding(String encoding) {
		rpc_read_encoding = new String(encoding);
	}

	public String getReadEncoding() {
		return (rpc_read_encoding);
	}

	public Value callSegment(String segname, Enumeration enum0, int argcnt)
			throws Exception
	// throws RpcException, ClassNotFoundException
	{
		if (debug) {
			if (argcnt == 0)
				UniLog.logClass(this, "RpcConnection callsegment " + returnFlag
						+ " " + segname + " " + argcnt);
		}
		try {
			int i = 0;
			Vector vlist;
			if (argcnt > 0) {
				StringBuffer sb = null;
				if (debug) {
					sb = new StringBuffer();
					sb.append("RpcConnection callsegment " + returnFlag + " ")
							.append(segname).append(" ").append(argcnt)
							.append(" [");
				}
				vlist = new Vector(argcnt);
				while (enum0.hasMoreElements()) {
					Object obj = enum0.nextElement();
					if (debug) {
						if (i < debugcnt) {
							if (i > 0)
								sb.append(",");
							sb.append(obj);
						} else if (i == debugcnt) {
							if (i > 0)
								sb.append(",");
							sb.append("...");
						}
					}
					if(obj instanceof Cell) {
						obj = ((Cell) obj).getObject();
					}
					Class aClass = obj.getClass();
					// String argtype = obj.getClass().getName();
					if (aClass.isAssignableFrom(Class
							.forName("java.lang.String"))) {
						String strret = (String) obj;
						if (tokenSupport) {
							RpcToken rtoken = (RpcToken) tokenHash.get(strret);
							if (rtoken != null) {
								int idx = rtoken.getIndex();
								if (idx == 0) {
									tokenidx++;
									rtoken.setIndex(tokenidx);
									vlist.addElement(new TokenStringVal(strret,
											tokenidx));
									// UniLog.log("init new Token arg " +
									// tokenidx + " " + strret);
								} else {
									vlist.addElement(new TokenVal(idx));
									// UniLog.log("use Token arg " + idx);
								}
							} else {
								vlist.addElement(new Strval(strret));
							}
						} else {
							vlist.addElement(new Strval(strret));
						}
						/*
						 * if(tokenSupport && strret.length() >= min_token_len)
						 * { RpcToken rtoken = (RpcToken)tokenHash.get(strret);
						 * if(rtoken != null) { tokenidx++; rtoken = new
						 * RpcToken(strret, tokenidx); tokenHash.put(strret,
						 * rtoken); vlist.addElement(new TokenStringVal(strret,
						 * tokenidx)); } else { vlist.addElement(new
						 * TokenVal(rtoken.getIndex())); } } else {
						 * vlist.addElement(new Strval(strret)); }
						 */
					} else if (aClass.isAssignableFrom(Class
							.forName("java.lang.Integer"))) {
						Integer intret = (Integer) obj;
						vlist.addElement(new Longval(intret.intValue()));
					} else if (aClass.getName().equals("java.lang.Long")) {
						Long intret = (Long) obj;
						vlist.addElement(new Longval(intret.intValue()));
						UniLog.logClass(this, "not support long type"); // andrew190323:
																		// server
																		// side
																		// will
																		// treat
																		// long
																		// as
																		// int?
					} else if (aClass.getName().equals("java.lang.Boolean")) {
						if (((Boolean) obj).booleanValue()) {
							vlist.addElement(new Strval("Y"));
						} else {
							vlist.addElement(new Strval("N"));
						}
					} else if (aClass.isAssignableFrom(Class
							.forName("java.lang.Double"))) {
						Double doubleret = (Double) obj;
						vlist.addElement(new Realval(doubleret.doubleValue()));
					} else if (aClass.isAssignableFrom(Class
							.forName("java.util.Date"))) {
						Date dateret = (Date) obj;
						// int l = (int)((dateret.getTime() +
						// DateUtil.getGmtOffset())/ 86400000) + 25568;

						// long ll = dateret.getTime() + DateUtil.getGmtOffset()
						// + 2209075200000L;
						// int l = (int) (ll / 86400000);
						int l = com.kyoko.common.DateUtil
								.dateToInformix(dateret);

						// UniLog.log("HAHA in RpcServerConnection date getTime "
						// + dateret.getTime());
						// UniLog.log("HAHA in RpcServerConnection date getGmtOffset "
						// + DateUtil.getGmtOffset());
						// UniLog.log("HAHA in RpcServerConnection date argument "
						// + dateret + " -> " + l);
						vlist.addElement(new Dateval(l));
					} else if (aClass.isAssignableFrom(Class.forName("[B"))) {
						vlist.addElement(new Bufval((byte[]) obj));
					} else if (aClass.getName().equals(
							"com.uniinformation.rpccall.Strval")) {
						vlist.addElement(obj);
					} else if (aClass.isAssignableFrom(Class
							.forName("java.math.BigInteger"))) {
						BigInteger intret = (BigInteger) obj;
						vlist.addElement(new Longval(intret.intValue()));
					} else if (aClass.isAssignableFrom(Class
							.forName("java.sql.Date"))) {
						int l = com.kyoko.common.DateUtil
								.dateToInformix((java.sql.Date) obj);
						vlist.addElement(new Dateval(l));
					} else {
						throw new RpcException("Invalid Argument " + i + " "
								+ aClass.getName());
					}
					/*
					 * if(argtype.compareTo("java.lang.String") == 0 ) { String
					 * strret = (String) obj; vlist.addElement(new
					 * Strval(strret)); } else
					 * if(argtype.compareTo("java.lang.Integer") == 0 ||
					 * argtype.compareTo("int")== 0) { Integer intret =
					 * (Integer) obj; vlist.addElement(new
					 * Longval(intret.intValue())); } else
					 * if(argtype.compareTo("java.lang.Double") == 0 ||
					 * argtype.compareTo("double") == 0) { Double doubleret =
					 * (Double) obj; vlist.addElement(new
					 * Realval(doubleret.doubleValue())); } else
					 * if(argtype.compareTo("java.util.Date") == 0) { Date
					 * dateret = (Date) obj; int l = (int)((dateret.getTime() +
					 * DateUtil.getGmtOffset())/ 86400000) + 25568;
					 * vlist.addElement(new Dateval(l)); } else { throw new
					 * RpcException("Invalid Argument " + i); }
					 */
					i++;
				}
				if (debug) {
					UniLog.logClass(this, sb.append("]").toString());
				}
			} else {
				vlist = new Vector();
			}

			/*
			return (callRemoteServer(segname,
					returnFlag ? (noAckFlag ? RPCCALLSEGMENTID_NOACK
							: RPCCALLSEGMENTID) : RPCCALLSEGMENTID_NORETURN,
					vlist.elements(), vlist.size()));
					*/

//			UniLog.log("before call remote server");
			Value v =
					(callRemoteServer(segname,
					returnFlag ? (noAckFlag ? RPCCALLSEGMENTID_NOACK
							: RPCCALLSEGMENTID) : RPCCALLSEGMENTID_NORETURN,
					vlist.elements(), vlist.size()));
// 			UniLog.log("after call remote server");
			return(v);
		} catch (Exception e) {
			UniLog.log("call fail: " +e.getMessage());
			if (debug) {
				UniLog.log(e);
			}
			throw (e);
		}
	}

	private Value callRemoteServer(String segname, int callid,
			Enumeration enum0, int argcnt) throws Exception {
		if (os == null || is == null) {
			return null;
		}
		try {
			int tmpserial, cc;
			Value val;

			rpcserialnum++;
			tmpserial = rpcserialnum;
			lastCallTime = new Date();
			os.writeByte(SOH);
			os.writeByte(callid);
			os.writeInt(tmpserial);
			if (tokenSupport) {
				RpcToken rtoken = (RpcToken) tokenHash.get(segname);
				if (rtoken != null) {
					int idx = rtoken.getIndex();
					if (idx == 0) {
						tokenidx++;
						rtoken.setIndex(tokenidx);
						os.writeShort(segname.length());
						os.writeBytes(segname);
						os.writeByte(DC1);
						os.writeInt(tokenidx);
						// UniLog.log("init new Token seg " + tokenidx + " " +
						// segname);
					} else {
						os.writeShort(0);
						os.writeInt(idx);
						os.writeByte(STX);
						// UniLog.log("use Token seg " + idx);
					}
				} else {
					os.writeShort(segname.length());
					os.writeBytes(segname);
					os.writeByte(STX);
				}
			} else {
				os.writeShort(segname.length());
				os.writeBytes(segname);
				os.writeByte(STX);
			}
			/*
			 * if(tokenSupport && segname.length() >= min_token_len) { RpcToken
			 * rtoken = (RpcToken)tokenHash.get(segname); if(rtoken != null) {
			 * tokenidx++; rtoken = new RpcToken(segname, tokenidx);
			 * tokenHash.put(segname, rtoken); os.writeShort(segname.length());
			 * os.writeBytes(segname); os.writeByte(DC1); os.writeInt(tokenidx);
			 * } else { os.writeShort(0); os.writeInt(rtoken.getIndex()); } }
			 * else { os.writeShort(segname.length()); os.writeBytes(segname);
			 * os.writeByte(STX); }
			 */
			os.writeShort(argcnt);
			while (enum0.hasMoreElements()) {
				writeValue((Value) enum0.nextElement());
			}
			os.writeByte(ETX);
			if (callid == RPCCALLSEGMENTID_NORETURN) {
				return (new Longval(0));
			}
			os.flush();

			if (callid != RPCCALLSEGMENTID_NOACK) {
				sock.setSoTimeout(RPCACKTIMEOUT * 1000);
				cc = is.readByte();
				// sock.setSoTimeout(0);
				sock.setSoTimeout(timeoutMSec);
				if (cc != ACK)
					throw new RpcException("No ACK got " + cc);
			}
			if (flowcontrol != null) {
				for (;;) {
					try {
						// UniLog.logClass(this, "flowcontrol gettimeout");
						sock.setSoTimeout(flowcontrol.gettimeout());
						cc = is.readByte();
						// sock.setSoTimeout(0);
						sock.setSoTimeout(timeoutMSec);
					} catch (java.io.InterruptedIOException ioe) {
						// UniLog.log("Before fcnotify");
						if (flowcontrol.fcnotify(-1) < 0)
							return (null);
						continue;
					}
					switch (cc) {
					case 0:
						if (flowcontrol.fcnotify(0) < 0)
							return (null);
						break;
					case 1:
						if (flowcontrol.fcnotify(1) < 0)
							return (null);
						if (realRpcServer() >= 0)
							continue;
						throw new RpcException("rpcserver() fail");
					default:
						throw new RpcException("Undefined state " + cc);
					}
					break;
				}
			} else {
				for (;;) {
					cc = is.readByte();
					switch (cc) {
					case 0:
						break;
					case 1:
						if (realRpcServer() >= 0)
							continue;
						throw new RpcException("rpcserver() fail");
					default:
						throw new RpcException("Undefined state " + cc);
					}
					break;
				}
			}
			int tserial = readSerial();
			if (tserial != tmpserial)
				throw new RpcException("Wrong serial no " + tserial + " "
						+ tmpserial + " " + (tserial ^ tmpserial));
			if ((val = readValue()) == null)
				throw new RpcException("Get Value");
			if (is.readByte() != EOT)
				throw new RpcException("Cannot get EOT");
			return (val);
		} catch (IOException e) {
			if (debug)
				UniLog.logClass(this, "Couldn't get I/O for the connection");
			if (debug)
				UniLog.log(e);
			throw (e);
		} catch (RpcException e) {
			if (debug)
				UniLog.logClass(this, "Error in rpc protocol " + e.getMessage());
			if (debug)
				UniLog.log(e);
			throw (e);
		}
		// return null;
	}

	private int readSerial() throws IOException {
		int t;
		int cc;
		t = 0;
		for (int i = 0; i < 3; i++) {
			cc = is.readUnsignedByte();
			t = t << 8;
			t |= cc;
			/* System.out.println("readSerial " + cc + " " + t); */
		}
		return (t);
	}

	public void setDebug(boolean p_flag) {
		debug = p_flag;
		if (debug) {
			UniLog.log("Server Connection setDebug " + p_flag);
		}
	}

	public void setDebugCnt(int p_cnt) {
		debugcnt = p_cnt;
	}

	public boolean isDebug() {
		return (debug);
	}

	/* added by lai */
	public Value callSegmentWithException(String segname) throws Exception {
		return (callSegmentWithException(segname, null));
	}

	public Value callSegment(String segname) {
		return (callSegment(segname, null));
	}

	public Value callSegmentWithException(String segname, Vector arglist)
			throws Exception {
		try {
			return (callSegment(segname,
					arglist == null ? null : arglist.elements(),
					arglist == null ? 0 : arglist.size()));
		} catch (Exception e) {
			if (isDebug())
				UniLog.log(e);
			throw (e);
		}
	}

	public Value callSegment(String segname, Vector arglist) {
		try {
			return (callSegmentWithException(segname, arglist));
		} catch (Exception e) {
			if (isDebug())
				UniLog.log(e);
			return (null);
		}
	}

	public InetAddress getLocalAddress() {
		if (sock == null)
			return null;
		else
			return (sock.getLocalAddress());
	}

	public InetAddress getRemoteAddress() {
		if (sock == null)
			return null;
		else
			return (sock.getInetAddress());
	}

	public void addCallback(RpcServlet p_callback) {
		setServlet(p_callback.getClass().getName(), p_callback);
	}

	public RpcServletProvider getServletProvider() {
		return (provider);
	}

	public void setKeep() {
		keepFlag = true;
	}

	public void setExit() {
		exitFlag = true;
	}

	public int putFileWithCallback(InputStream input, int maxlen,
			String filename, int offset, int maxrate, int kbyte, String mode,
			String callbackname, Vector arglist) {
		int cc;
		try {
			lastCallTime = new Date();
			os.writeByte(SOH);
			os.writeByte(RPCPUTFILEID);
			os.writeInt(offset);
			byte[] buf;
			if (simToTran) {
				buf = ChineseConvert.convertB2G(filename)
						.getBytes(rpc_encoding);
			} else {
				buf = filename.getBytes(rpc_encoding);
			}
			os.writeShort(buf.length);
			os.write(buf, 0, buf.length);
			os.writeByte(STX);
			os.flush();
			sock.setSoTimeout(RPCACKTIMEOUT * 1000);
			cc = is.readByte();
			// sock.setSoTimeout(0);
			sock.setSoTimeout(timeoutMSec);
			switch (cc) {
			case ACK:
				cc = rpcserver_putfile_with_callback(input, maxlen, maxrate,
						kbyte, mode, callbackname, arglist);
				if (cc < 0)
					return (-1);
				return (0);
			case NAK:
				Value v = readValue();
				if (v == null)
					return (-1);
				UniLog.logClass(this, "get fail 8 " + v.toString());
				v = null;
				return (-1);
			}
		} catch (IOException ioe) {
			UniLog.logClass(this, "get fail 9 " + ioe.getMessage());
			return (-1);
		}
		return (-1);
	}

	public int putFileWithCallback(DataInput dataInput, int maxlen,
			String filename, int offset, int maxrate, int kbyte, String mode,
			String callbackname, Vector arglist) {
		int cc;
		try {
			lastCallTime = new Date();
			os.writeByte(SOH);
			os.writeByte(RPCPUTFILEID);
			os.writeInt(offset);
			byte[] buf;
			if (simToTran) {
				buf = ChineseConvert.convertB2G(filename)
						.getBytes(rpc_encoding);
			} else {
				buf = filename.getBytes(rpc_encoding);
			}
			os.writeShort(buf.length);
			os.write(buf, 0, buf.length);
			os.writeByte(STX);
			os.flush();
			sock.setSoTimeout(RPCACKTIMEOUT * 1000);
			cc = is.readByte();
			// sock.setSoTimeout(0);
			sock.setSoTimeout(timeoutMSec);
			switch (cc) {
			case ACK:
				cc = rpcserver_putfile_with_callback(dataInput, maxlen,
						maxrate, kbyte, mode, callbackname, arglist);
				if (cc < 0)
					return (-1);
				return (0);
			case NAK:
				Value v = readValue();
				if (v == null)
					return (-1);
				UniLog.logClass(this, "get fail 10 " + v.toString());
				v = null;
				return (-1);
			}
		} catch (IOException ioe) {
			UniLog.log(ioe);
			return (-1);
		}
		return (-1);
	}

	private int rpcserver_putfile_with_callback(DataInput input, int maxlen,
			int maxrate, int kbyte, String mode, String callbackname,
			Vector arglist) {
		int writecnt = 0;
		int bufcnt = 0;
		int threshold = kbyte * 1024;
		boolean eof = false;
		try {
			byte putbuf[] = new byte[RPCPUTLEN];
			for (; eof;) {
				int readcnt;
				for (readcnt = 0; readcnt < RPCPUTLEN; readcnt++) {
					try {
						putbuf[readcnt] = input.readByte();
					} catch (EOFException e2) {
						eof = true;
						break;
					}
				}
				// int readcnt = input.read(putbuf);
				if (readcnt <= 0)
					break;
				writecnt += readcnt;
				os.writeInt(readcnt);
				os.write(putbuf, 0, readcnt);
				if (threshold > 0) {
					bufcnt += readcnt;
					for (; bufcnt >= threshold; bufcnt -= threshold) {
						if (mode.equals("R")) {
							callSegment(callbackname, arglist);
						} else {
							fileProgress(callbackname, arglist);
						}
					}
				}
			}
			if (threshold > 0) {
				for (; bufcnt >= threshold; bufcnt -= threshold) {
					if (mode.equals("R")) {
						callSegment(callbackname, arglist);
					} else {
						fileProgress(callbackname, arglist);
					}
				}
				if (bufcnt > 0) {
					if (mode.equals("R")) {
						callSegment(callbackname, arglist);
					} else {
						fileProgress(callbackname, arglist);
					}
				}
			}
			os.writeInt(0);
			os.flush();
			if (is.readByte() != EOT)
				throw new RpcException("Cannot get EOT");
		} catch (IOException e) {
			UniLog.logClass(this,
					"rpcserver_putfile I/O error " + e.getMessage());
			UniLog.log(e);
			return (-8);
		} catch (RpcException e) {
			UniLog.logClass(this, "rpcserver_putfile error " + e.getMessage());
			UniLog.log(e);
			return (-10);
		}
		return (writecnt);
	}

	private int rpcserver_putfile_with_callback(InputStream inputstream,
			int maxlen, int maxrate, int kbyte, String mode,
			String callbackname, Vector arglist) {
		int writecnt = 0;
		int bufcnt = 0;
		int threshold = kbyte * 1024;
		try {
			byte putbuf[] = new byte[RPCPUTLEN];
			for (;;) {
				int readcnt = inputstream.read(putbuf);
				if (readcnt < 0)
					break;
				if (readcnt > 0) {
					writecnt += readcnt;
					os.writeInt(readcnt);
					os.write(putbuf, 0, readcnt);
					if (threshold > 0) {
						bufcnt += readcnt;
						for (; bufcnt >= threshold; bufcnt -= threshold) {
							if (mode.equals("R")) {
								callSegment(callbackname, arglist);
							} else {
								fileProgress(callbackname, arglist);
							}
						}
					}
				}
				if (readcnt == 0) {
					UniLog.logClass(this,
							"rpcserver_putfile read file readcnt=0");
				}
			}
			if (threshold > 0) {
				for (; bufcnt >= threshold; bufcnt -= threshold) {
					if (mode.equals("R")) {
						callSegment(callbackname, arglist);
					} else {
						fileProgress(callbackname, arglist);
					}
				}
				if (bufcnt > 0) {
					if (mode.equals("R")) {
						callSegment(callbackname, arglist);
					} else {
						fileProgress(callbackname, arglist);
					}
				}
			}
			os.writeInt(0);
			os.flush();
			if (is.readByte() != EOT)
				throw new RpcException("Cannot get EOT");
		} catch (IOException e) {
			UniLog.logClass(this,
					"rpcserver_putfile I/O error " + e.getMessage());
			UniLog.log(e);
			return (-8);
		} catch (RpcException e) {
			UniLog.logClass(this, "rpcserver_putfile error " + e.getMessage());
			UniLog.log(e);
			return (-10);
		}
		return (writecnt);
	}

	private void fileProgress(String p_methodname, Vector arglist) {
		String myclassname = "";
		String methodname = "";
		try {
			StringTokenizer st = new StringTokenizer(p_methodname, ".");
			int tokencnt = st.countTokens();
			if (tokencnt == 1) {
				if (defaultServletClassName != null) {
					myclassname = defaultServletClassName;
					methodname = p_methodname;
				} else {
					myclassname = p_methodname;
					methodname = "execute";
				}
			} else {
				for (int i = 1; i < tokencnt; i++) {
					myclassname += st.nextToken();
					if (i < tokencnt - 1)
						myclassname += ".";
				}
				methodname = st.nextToken();
			}
			RpcServlet serv = searchFeature(myclassname);
			if (serv == null) {
				UniLog.log("fileProgress fail 1 " + p_methodname + " "
						+ myclassname);
				return;
			}
			int argcnt = (arglist == null ? 0 : arglist.size());
			Class[] argclasses = new Class[argcnt];
			Object[] argobjects = new Object[argcnt];
			for (int i = 0; i < argcnt; i++) {
				argclasses[i] = arglist.elementAt(i).getClass();
				argobjects[i] = arglist.elementAt(i);
			}
			invokeServlet(serv, methodname, argclasses, argobjects);
		} catch (Exception e) {
			UniLog.log("fileProgress fail 2 " + p_methodname + " "
					+ myclassname);
			UniLog.log(e.getMessage());
		}
	}

	private void writeGetFileExtendedParameter(int kbyte, String mode,
			String callbackname, Vector arglist) throws RpcException,
			IOException {
		try {
			os.writeInt(kbyte);
			// writeValue(new Strval(callbackname));
			os.writeShort(callbackname.length());
			os.writeBytes(callbackname);
			os.writeInt(0); /* reserved */
			int argcnt = (arglist == null ? 0 : arglist.size());
			os.writeShort(argcnt);
			for (int i = 0; i < argcnt; i++) {
				Object obj = arglist.elementAt(i);
				Class aClass = obj.getClass();
				if (aClass.isAssignableFrom(Class.forName("java.lang.String"))) {
					writeValue(new Strval((String) obj));
				} else if (aClass.isAssignableFrom(Class
						.forName("java.lang.Integer"))) {
					writeValue(new Longval(((Integer) obj).intValue()));
				} else if (aClass.isAssignableFrom(Class
						.forName("java.lang.Double"))) {
					writeValue(new Realval(((Double) obj).doubleValue()));
				} else if (aClass.isAssignableFrom(Class
						.forName("java.util.Date"))) {
					Date dateret = (Date) obj;
					// int l = (int)((dateret.getTime() +
					// DateUtil.getGmtOffset())/ 86400000) + 25568;
					int l = com.kyoko.common.DateUtil
							.dateToInformix(dateret);

					writeValue(new Dateval(l));
				} else {
					throw new RpcException("Invalid Argument " + i + " "
							+ aClass.getName());
				}
			}
		} catch (ClassNotFoundException e) {
			throw new RpcException("Invalid Class " + e.getMessage());
		}
	}

	public int getFileWithCallback(DataOutput dataOutput, int maxlen,
			String filename, int offset, int maxrate, int kbyte, String mode,
			String callbackname, Vector arglist) {
		int cc;
		try {
			lastCallTime = new Date();
			os.writeByte(SOH);
			os.writeByte(kbyte > 0 ? RPCGETFILEID2 : RPCGETFILEID);
			os.writeInt(offset);
			byte[] buf;
			if (simToTran) {
				buf = ChineseConvert.convertB2G(filename)
						.getBytes(rpc_encoding);
			} else {
				buf = filename.getBytes(rpc_encoding);
			}
			os.writeShort(buf.length);
			os.write(buf, 0, buf.length);
			os.writeByte(STX);
			os.writeInt(maxlen);
			os.writeInt(maxrate);
			if (kbyte > 0)
				writeGetFileExtendedParameter(kbyte, mode, callbackname,
						arglist);

			os.flush();
			sock.setSoTimeout(RPCACKTIMEOUT * 1000);
			cc = is.readByte();
			// sock.setSoTimeout(0);
			sock.setSoTimeout(timeoutMSec);
			switch (cc) {
			case ACK:
				cc = rpcserver_getfile(dataOutput);
				if (cc < 0)
					return (-1);
				return (0);
			case NAK:
				Value v = readValue();
				if (v == null)
					return (-1);
				UniLog.logClass(this, "get fail 11 " + v.toString());
				v = null;
				return (-1);
			}
		} catch (IOException ioe) {
			UniLog.logClass(this, "get fail 12 " + ioe.getMessage());
			return (-1);
		}
		return (-1);
	}

	public int getFileWithCallback(OutputStream out, int maxlen,
			String filename, int offset, int maxrate, int kbyte, String mode,
			String callbackname, Vector arglist) {
		int cc;
		try {
			lastCallTime = new Date();
			os.writeByte(SOH);
			os.writeByte(kbyte > 0 ? RPCGETFILEID2 : RPCGETFILEID);
			os.writeInt(offset);
			byte[] buf;
			if (simToTran) {
				buf = ChineseConvert.convertB2G(filename)
						.getBytes(rpc_encoding);
			} else {
				buf = filename.getBytes(rpc_encoding);
			}
			os.writeShort(buf.length);
			os.write(buf, 0, buf.length);
			os.writeByte(STX);
			os.writeInt(maxlen);
			os.writeInt(maxrate);

			if (kbyte > 0)
				writeGetFileExtendedParameter(kbyte, mode, callbackname,
						arglist);

			os.flush();
			sock.setSoTimeout(RPCACKTIMEOUT * 1000);
			cc = is.readByte();
			// sock.setSoTimeout(0);
			sock.setSoTimeout(timeoutMSec);
			switch (cc) {
			case ACK:
				cc = rpcserver_getfile(out);
				if (cc < 0)
					return (-1);
				return (0);
			case NAK:
				Value v = readValue();
				if (v == null)
					return (-1);
				UniLog.logClass(this, "get fail 13 " + v.toString());
				v = null;
				return (-1);
			}
		} catch (IOException ioe) {
			UniLog.logClass(this, "get fail 14 " + ioe.getMessage());
			ioe.printStackTrace();
			return (-1);
		}
		return (-1);
	}

	private Object valToObject(Value val) {
		switch (val.valType()) {
		case ISIVALUE:
			Longval ival = (Longval) val;
			return (new Integer(ival.toInt()));
		case ISRVALUE:
			Realval rval = (Realval) val;
			return (new Double(rval.toDouble()));
		case ISNULVALUE:
			return (null);
		case ISBUFVALUE:
			Bufval bval = (Bufval) val;
			return (bval.getBuffer());
		case ISSVALUE:
			Strval sval = (Strval) val;
			return (new String(sval.toString()));
		case ISBVALUE:
			break;
		case ISDVALUE:
			Dateval dval = (Dateval) val;
			// long l = dval.toInt() - 25568;
			// l *= 86400000;
			// l -= DateUtil.getGmtOffset();
			// return(new Date(l));
			return (DateUtil.informixToDate(dval.toInt()));
		}
		return (null);
	}

	public int getLocalPort() {
		if (sock == null)
			return 0;
		else
			return (sock.getLocalPort());
	}

	public int getRemotePort() {
		if (sock == null)
			return 0;
		else
			return (sock.getPort());
	}

	public boolean isStopped() {
		if (sock == null)
			return (true);
		else
			return (false);
	}

	public String getServletPath(RpcServlet p_r) {
		if (servletInUse == null)
			return (null);
		for (Enumeration e = servletInUse.keys(); e.hasMoreElements();) {
			String pathName = (String) e.nextElement();
			RpcConnServlet rpcconnservlet = (RpcConnServlet) servletInUse
					.get(pathName);
			// UniLog.log("HAHA checking " + pathName + " " + p_r + " " +
			// rpcconnservlet);
			if (rpcconnservlet.getServlet() == p_r)
				return (pathName);
		}
		return (null);
	}

	public boolean checkGetFileWithTime() {
		UniLog.log("checkGetFileWithTime ");
		return (true);
	}

	public int getFileStart(int maxlen, String filename, int offset, int maxrate) {
		int cc;
		try {
			lastCallTime = new Date();
			os.writeByte(SOH);
			os.writeByte(RPCGETFILEID);
			os.writeInt(offset);
			byte[] buf;
			if (simToTran) {
				buf = ChineseConvert.convertB2G(filename)
						.getBytes(rpc_encoding);
			} else {
				buf = filename.getBytes(rpc_encoding);
			}
			os.writeShort(buf.length);
			os.write(buf, 0, buf.length);
			os.writeByte(STX);
			os.writeInt(maxlen);
			os.writeInt(maxrate);
			os.flush();
			sock.setSoTimeout(RPCACKTIMEOUT * 1000);
			cc = is.readByte();
			// sock.setSoTimeout(0);
			sock.setSoTimeout(timeoutMSec);
			switch (cc) {
			case ACK:
				return (0);
			case NAK:
				Value v = readValue();
				if (v == null)
					return (-1);
				UniLog.logClass(this, "get fail 4 " + v.toString());
				v = null;
				return (-1);
			}
		} catch (IOException ioe) {
			UniLog.logClass(this, "get fail 5 " + ioe.getMessage());
			return (-1);
		}
		return (-1);
	}

	public byte[] getFileBytes() throws RpcException, IOException {
		int len;
		int b = is.readByte();
		if (b != 0) {
			throw new RpcException("Protocol Error 1");
		}
		len = readSerial();
		if (len < 0) {
			throw new RpcException("Protocol Error 2");
		}
		if (len == 0) {
			os.writeByte(EOT);
			os.flush();
			return (null);
		}
		byte getbuf[] = new byte[len];
		for (int n = 0; len > 0;) {
			int cc = is.read(getbuf, n, len);
			n += cc;
			len -= cc;
		}
		return (getbuf);
	}

	public int putFileStart(int maxlen, String filename, int offset, int maxrate) {
		int cc;
		try {
			lastCallTime = new Date();
			os.writeByte(SOH);
			os.writeByte(RPCPUTFILEID);
			os.writeInt(offset);
			byte[] buf;
			if (simToTran) {
				buf = ChineseConvert.convertB2G(filename)
						.getBytes(rpc_encoding);
			} else {
				buf = filename.getBytes(rpc_encoding);
			}
			os.writeShort(buf.length);
			os.write(buf, 0, buf.length);
			os.writeByte(STX);
			os.flush();
			sock.setSoTimeout(RPCACKTIMEOUT * 1000);
			cc = is.readByte();
			// sock.setSoTimeout(0);
			sock.setSoTimeout(timeoutMSec);
			switch (cc) {
			case ACK:
				if (cc < 0)
					return (-1);
				return (0);
			case NAK:
				Value v = readValue();
				if (v == null)
					return (-1);
				UniLog.logClass(this, "get fail 1 " + v.toString());
				v = null;
				return (-1);
			}
		} catch (IOException ioe) {
			UniLog.logClass(this, "get fail 2 " + ioe.getMessage());
			return (-1);
		}
		return (-1);
	}

	public int putFileBytes(byte[] putbuf, int ofs, int len) {
		try {
			if (putbuf != null) {
				os.writeInt(len);
				os.write(putbuf, ofs, len);
			} else {
				os.writeInt(0);
				os.flush();
				if (is.readByte() != EOT)
					throw new RpcException("Cannot get EOT");
			}
		} catch (IOException e) {
			UniLog.logClass(this,
					"rpcserver_putfile I/O error " + e.getMessage());
			UniLog.log(e);
			return (-8);
		} catch (RpcException e) {
			UniLog.logClass(this, "rpcserver_putfile error " + e.getMessage());
			UniLog.log(e);
			return (-10);
		}
		return (len);
	}

	public Date getLastCallTime() {
		return (lastCallTime);
	}

}
