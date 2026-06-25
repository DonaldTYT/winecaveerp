package com.uniinformation.utils;

import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.json.XML;

import java.sql.*;
import java.io.*;
import java.lang.reflect.*;

import com.uniinformation.cell.*;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.webcore.WebCoreUtil;

public class JdbcPool implements Runnable, ThreadPoolRunnable {
	public final static boolean DEFAULT_DEBUG = false;
	public final static int DEFAULT_CONNECTION_COUNT = 5;
	public final static int DEFAULT_MAX_CONNECTION_COUNT = 100;
	private final static int UL_MAX_CONNECTION_COUNT = 500; //upper limit
	private final static int LL_MAX_CONNECTION_COUNT = 50;  //lower limit
	public final static int DEFAULT_KEEP_ALIVE = 60;
	public static final int JDBC_ORACLE = 0;
	public static final int JDBC_SCORPION = 1;
	public static final int JDBC_MYSQL = 2;
	public static final int JDBC_POSTGRESQL = 3;
	static JdbcPool defaultJdbcPool = null;
	static Object lockObject = new Object();
	static Hashtable jdbcPoolHash = new Hashtable();
	private String connStr = null;
	private int curCnt = 0;
	private int connCnt = DEFAULT_CONNECTION_COUNT; //default connection count
	private int maxConnCnt = DEFAULT_MAX_CONNECTION_COUNT;
	private int keepAlive = DEFAULT_KEEP_ALIVE;
	private Hashtable<ConnectionRecord,ConnectionRecord> cPool = new Hashtable(); //connect pool
	private Hashtable<Integer,String> rPool = new Hashtable(); //requested connect pool (remark:should be allocated pool)  key:ConnectionRecord id
	private Thread thread;
	private boolean fPauseRequested = false;
	private boolean fStopMakeupConnections = false;
	private int nextId = 0;
	private int curBatchId = 0;  //for handle restart connection
	private long timeToMakeConnection = 0;
	private String loginname = null;
	private String passwd = null;
	private String appName = "default";
	public static AtomicBoolean fDebug = new AtomicBoolean(DEFAULT_DEBUG);
	private volatile boolean fExiting = false;
	private Object consumerLock = new Object();
	private long timeToReleaseConnection = 0;
	private AtomicInteger shutdownFlag = new AtomicInteger(0); //0:default 1:ask for shutdown 2:done //200121: for handle ThreadPoolThread.pleaseDie 
//	public final static String MYSQL_DRIVER="com.mysql.cj.jdbc.Driver";
	public final static String MYSQL_DRIVER="com.mysql.jdbc.Driver";
	public final static String POSTGRESQL_DRIVER="org.postgresql.Driver";


	public class ConnectionRecord implements Connection {
		Connection conn;
		long timeToCheckAlive;
		PreparedStatement aliveStatement = null;
		boolean fAvailable = true;
		boolean fDiscarded = false;
		int id;
		int batchId;
		Boolean defaultAutoCommit = null;
		int defaultTransactionIsolation = -1;
		long aliveUntilTime = 0L;
		private boolean fReuse = true;
		ConnectionRecord(Connection p_connection, int p_id, int p_batchId) throws SQLException {
			conn = p_connection;
			id = p_id;
			batchId = p_batchId;
			timeToCheckAlive = System.currentTimeMillis();
			defaultAutoCommit = conn.getAutoCommit();
			defaultTransactionIsolation = conn.getTransactionIsolation();
			if (fDebug.get()) UniLog.log1("[%s] new connection record added. id:%d", appName, id);
			addCurCnt(1);
			timeToReleaseConnection = System.currentTimeMillis() + 5000;
		}
		int getId() {
			return(id);
		}
		int getBatchId() {
			return(batchId);
		}
		protected void finalize() throws Throwable {
			if (fDebug.get()) UniLog.logClass(this, "["+appName+"]:"+"finalizing ...");
			synchronized(cPool) {
				if (fDebug.get()) {
					UniLog.logClass(JdbcPool.this, "["+appName+"]:"+"rPool.remove("+getId()+")");
				}
				rPool.remove(new Integer(getId()));
			}
			if (!fDiscarded) {
				if (fDebug.get()) {
					UniLog.logClass(this, "["+appName+"]:"+"finalizing not yet discarded ...");
				}
				addCurCnt(-1);
			}
		}
		boolean isAvailable() {
			return(fAvailable);
		}
		void setAvailable(boolean p_flag) {
			fAvailable = p_flag;
		}
		
		public boolean reconnect()
		{
			try {
				UniLog.log1("[%s] reconnect expired connection", appName);
				conn = getJdbcConnection();
				return(true);
			} catch (Exception ex) {
				UniLog.log(ex);
				return(false);
			}
		}
		private void resetAutoCommit() throws SQLException{
			if (defaultAutoCommit != null && conn != null && conn.getAutoCommit() != defaultAutoCommit){
				if (fDebug.get())	UniLog.logClass(this, "["+appName+"]:"+"reset autocommit");
				/* Add by DT on 2018-06-05, if a jdbc connection is returned to the jdbcpool within a tractions
			    the changes (if any) in the transaction is rollback
				 */
				if(!conn.getAutoCommit()) conn.rollback();
				conn.setAutoCommit(defaultAutoCommit);
			}
		}
		private void resetTransactionIsolation() throws SQLException{
			if (defaultTransactionIsolation >= 0 && conn != null && conn.getTransactionIsolation() != defaultTransactionIsolation){
				if (fDebug.get())	UniLog.logClass(this, "["+appName+"]:"+"reset isolation");
				conn.setTransactionIsolation(defaultTransactionIsolation);
			}
		}
		public void setReuse(boolean p_fReuse){
			fReuse = p_fReuse;
		}
		public boolean getReuse(){
			return fReuse;
		}
		public void discard() throws SQLException {
			if (fDebug.get()) UniLog.logClass(this, "["+appName+"]:"+"discarding ...");
			conn.close();
			conn = null;
			addCurCnt(-1);
			fDiscarded = true;
		}
		
		
		/* start of definition of java.sql.Connection */
		@Override
		public void clearWarnings() throws SQLException {
			conn.clearWarnings();
		}
		@Override
		public void close() throws SQLException {
			if (fDebug.get()) UniLog.logClass(this, "["+appName+"]:"+"close() trapped ...");
			if (fExiting) {
				discard();
				return;
			}
			resetAutoCommit();
			resetTransactionIsolation();
			synchronized(this) {
				setAvailable(true);
			}
			synchronized(cPool) {
				int oSize = cPool.size();
				if (fDebug.get()) UniLog.logClass(JdbcPool.this, "["+appName+"]:"+"cPool.put("+getId()+")");
				if (fReuse){
					cPool.put(this, this);
				}
				else{
					this.discard();
				}
				if (fDebug.get()) UniLog.logClass(JdbcPool.this, "["+appName+"]:"+"rPool.remove("+getId()+")");
				rPool.remove(new Integer(getId()));
				if (fDebug.get()) UniLog.logClass(JdbcPool.this, "["+appName+"]:"+"cPool.size(): "+oSize+"->"+cPool.size()+" ["+rPool.size()+"]");
				// cPool.notifyAll();
			}
			synchronized(consumerLock) {
				consumerLock.notifyAll();
			}
		}
		@Override
		public void commit() throws SQLException {
			conn.commit();
		}
		@Override
		public Statement createStatement() throws SQLException {
			return(conn.createStatement());
		}
		@Override
		public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
			return(conn.createStatement(resultSetType, resultSetConcurrency));
		}
		@Override
		public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
			return(conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability));
		}
		@Override
		public boolean getAutoCommit() throws SQLException {
			return(conn.getAutoCommit());
		}
		@Override
		public String getCatalog() throws SQLException {
			return(conn.getCatalog());
		}
		@Override
		public DatabaseMetaData getMetaData() throws SQLException {
			return(conn.getMetaData());
		}
		@Override
		public int getTransactionIsolation() throws SQLException {
			return(conn.getTransactionIsolation());
		}
		@Override
		public java.util.Map getTypeMap() throws SQLException {
			return(conn.getTypeMap());
		}
		@Override
		public SQLWarning getWarnings() throws SQLException {
			return(conn.getWarnings());
		}
		@Override
		public boolean isClosed() throws SQLException {
			return(conn.isClosed());
		}
		@Override
		public boolean isReadOnly() throws SQLException {
			return(conn.isReadOnly());
		}
		@Override
		public String nativeSQL(String sql) throws SQLException {
			return(conn.nativeSQL(sql));
		}
		@Override
		public CallableStatement prepareCall(String sql) throws SQLException {
			return(conn.prepareCall(sql));
		}
		@Override
		public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
			return(conn.prepareCall(sql, resultSetType, resultSetConcurrency));
		}
		@Override
		public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
			return(conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
		}
		@Override
		public PreparedStatement prepareStatement(String sql) throws SQLException {
			return(conn.prepareStatement(sql));
		}
		@Override
		public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
			return(conn.prepareStatement(sql, resultSetType, resultSetConcurrency));
		}
		@Override
		public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
			return(conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability));
		}
		@Override
		public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
			return(conn.prepareStatement(sql, columnNames));
		}
		@Override
		public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
			return(conn.prepareStatement(sql, columnIndexes));
		}
		@Override
		public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
			return(conn.prepareStatement(sql, autoGeneratedKeys));
		}
		@Override
		public void rollback() throws SQLException {
			conn.rollback();
		}
		@Override
		public void setAutoCommit(boolean autoCommit) throws SQLException {
			conn.setAutoCommit(autoCommit);
		}
		@Override
		public void setCatalog(String catalog) throws SQLException {
			conn.setCatalog(catalog);
		}
		@Override
		public void setReadOnly(boolean readOnly) throws SQLException {
			conn.setReadOnly(readOnly);
		}
		@Override
		public void setTransactionIsolation(int level) throws SQLException {
			conn.setTransactionIsolation(level);
		}
		@Override
		public void setTypeMap(java.util.Map map) throws SQLException {
			conn.setTypeMap(map);
		}
		@Override
		public void releaseSavepoint(Savepoint savepoint) throws SQLException {
			conn.releaseSavepoint(savepoint);
		}
		@Override
		public Savepoint setSavepoint() throws SQLException {
			return(conn.setSavepoint());
		}
		@Override
		public Savepoint setSavepoint(String name) throws SQLException {
			return(conn.setSavepoint(name));
		}
		@Override
		public void rollback(Savepoint savepoint) throws SQLException {
			conn.rollback(savepoint);
		}
		@Override
		public void setHoldability(int holdability) throws SQLException {
			conn.setHoldability(holdability);
		}
		@Override
		public int getHoldability() throws SQLException {
			return(conn.getHoldability());
		}
		/* end of definition of java.sql.Connection */
		
		
		public Connection getConnection() {
			return(conn);
		}
		boolean isAlive() {
			if (aliveUntilTime > 0L) {
				if (fDebug.get()) UniLog.log1("[%s] aliveUntilTimeExpired",appName);
				if (System.currentTimeMillis() > aliveUntilTime) {
					return(false);
				}
			}
			if (System.currentTimeMillis() < timeToCheckAlive) {
				return(true);
			}
			try {
				if (fDebug.get()) UniLog.log1("checkAlive (%s)...",connStr);
				if(!conn.isValid(900000)) {
					UniLog.log1("connection expired, isAlive return false without check"); 
					if(!reconnect()) return(false);
				}
				boolean fImplemented = false;
				if (conn.getClass().getName().equals("com.uniinformation.scorpion.driver.ScpConnection")) {
					if (fDebug.get()) UniLog.log1("[%s] calling myConnection.keepAlive()...", appName);
					Method method = conn.getClass().getMethod("keepAlive", new Class[0]);
					fImplemented = ((Boolean) method.invoke(conn, new Object[0])).booleanValue();
				}
				if (!fImplemented) {
					if (aliveStatement == null)
						if (conn.getClass().getName().startsWith("com.mysql.jdbc") 
								|| conn.getClass().getName().equals("com.mysql.jdbc.ConnectionImpl")
								|| conn.getClass().getName().equals("com.mysql.cj.jdbc.ConnectionImpl")
								|| conn.getClass().getName().startsWith("org.postgresql.")
								)
							
							aliveStatement = conn.prepareStatement("select 1");
						else if (conn.getClass().getName().equals("COM.ibm.db2.jdbc.net.DB2Connection"))
							aliveStatement = conn.prepareStatement("select serial_id from vendor where serial_id < 0");
						else if (conn.getClass().getName().equals("oracle.jdbc.driver.T4CConnection")) //for oracle cloud with 11g driver (jdk 1.6)
							aliveStatement = conn.prepareStatement("select count(*) from help");
						else if (conn.getClass().getName().equals("com.microsoft.sqlserver.jdbc.SQLServerConnection")) //for azure mssql with sqljdbc4.jar (jdk 1.6)
							aliveStatement = conn.prepareStatement("SELECT count(*) FROM information_schema.TABLES");
						else {
							// for oracle
							aliveStatement = conn.prepareStatement("select count(*) from sysdepend");
						}
					ResultSet tmprset = aliveStatement.executeQuery();
					tmprset.next();
				}
				timeToCheckAlive = System.currentTimeMillis() + keepAlive * 1000;
				if (fDebug.get()) UniLog.log1("[%s] connection is good", appName);
				return(true);
			} 
			catch (Exception ex) {
				UniLog.log(ex);
				UniLog.logClass(this, "["+appName+"]:"+"checkAlive failed");
				return(false);
			}
		}
		// for java 1.6 compatibility
		@Override
		public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
			return(conn.createStruct(typeName, attributes));
		}
		@Override
		public java.sql.Array createArrayOf(String typeName, Object[] elements) throws SQLException {
			return(conn.createArrayOf(typeName, elements));
		}
		@Override
		public Properties getClientInfo() throws SQLException {
			return(conn.getClientInfo());
		}
		@Override
		public String getClientInfo(String name) throws SQLException {
			return(conn.getClientInfo(name));
		}
		@Override
		public void setClientInfo(Properties properties) throws SQLClientInfoException {
			conn.setClientInfo(properties);
		}
		@Override
		public void setClientInfo(String name, String value) throws SQLClientInfoException {
			conn.setClientInfo(name, value);
		}
		@Override
		public boolean isValid(int timeout) throws SQLException {
			if (conn == null) {
				return false;
			}
			return(conn.isValid(timeout));
		}
		@Override
		public SQLXML createSQLXML() throws SQLException {
			return(conn.createSQLXML());
		}
		@Override
		public Blob createBlob() throws SQLException {
			return(conn.createBlob());
		}
		@Override
		public Clob createClob() throws SQLException {
			return(conn.createClob());
		}
		@Override
		public NClob createNClob()throws SQLException {
			return(conn.createNClob());
		}
		@Override
		public boolean isWrapperFor(Class<?> iface) throws SQLException {
			return(conn.isWrapperFor(iface));
		}
		@Override
		public <T> T unwrap(Class<T> iface) throws SQLException {
			return(conn.unwrap(iface));
		}
		@Override
		public String getSchema() throws SQLException {
			try {
				final Method gs = conn.getClass().getMethod("getSchema");
				try {
					return((String) gs.invoke(conn));
				} catch (Exception iex){
					UniLog.log(iex);
					return(null);
				}
			} 
			catch (NoSuchMethodException ex){
				UniLog.log("jdbcSpool conn.setSchema not implemented, return null");
				return(null);
			}
		}
		@Override
		public void setSchema(String p_schema) throws SQLException {
			try {
				Class argClasses[] = new Class[1];
				try {
					argClasses[0] = Class.forName("java.lang.String");
				} catch (ClassNotFoundException cex){
					UniLog.log(cex);
				}
				final Method gs = conn.getClass().getMethod("setSchema",argClasses);
				try {
					gs.invoke(conn);
				} catch (Exception iex){
					UniLog.log(iex);
				}
			} catch (NoSuchMethodException ex){
				UniLog.log("jdbcSpool conn.setSchema not implemented, ignored");
			}
			//			conn.setSchema(p_schema);
		}
		public void abort(Executor executor) throws SQLException{
			try {
				Class argClasses[] = new Class[1];
				argClasses[0] = Executor.class;
				final Method gs = conn.getClass().getMethod("setSchema",argClasses);
				try {
					gs.invoke(conn,executor);
				} catch (Exception iex){
					UniLog.log(iex);
				}
			} 
			catch (NoSuchMethodException ex){
				UniLog.log("jdbcSpool conn.setSchema not implemented, ignored");
			}
			//conn.abort(executor);
		}
		@Override
		public int getNetworkTimeout() throws SQLException {
			try {
				final Method gs = conn.getClass().getMethod("getNetworkTimeout");
				try {
					return((Integer) gs.invoke(conn));
				} catch (Exception iex){
					UniLog.log(iex);
					return(0);
				}
			} catch (NoSuchMethodException ex){
				UniLog.log("jdbcSpool conn.setSchema not implemented, return 0");
				return(0);
			}
		}
		@Override
		public void setNetworkTimeout(Executor executor,int p_timeout) throws SQLException {
			try {
				Class argClasses[] = new Class[1];
				argClasses[0] = int.class;
				final Method gs = conn.getClass().getMethod("setSchema",argClasses);
				try {
					gs.invoke(conn);
				} catch (Exception iex){
					UniLog.log(iex);
				}
			} catch (NoSuchMethodException ex){
				UniLog.log("jdbcSpool conn.setSchema not implemented, ignored");
			}
			//conn.setNetworkTimeout(executor,p_timeout);
		}

		public void setAliveUntilTime(int p_msec) {
			aliveUntilTime = System.currentTimeMillis() + 5000;
		}
	} //end of ConnectionRecord
	
	
	
	
	public static JdbcPool getJdbcPool() {
		if (defaultJdbcPool == null) {
			synchronized (lockObject) {
				if (defaultJdbcPool == null)
					defaultJdbcPool = new JdbcPool();
			}
		}
		return(defaultJdbcPool);
	}
	public static JdbcPool getJdbcPool(String p_appName) {
		synchronized(jdbcPoolHash) {
			return((JdbcPool) jdbcPoolHash.get(p_appName));
		}
	}
	public static JdbcPool setDefaultJdbcPool(JdbcPool p_pool) {
		if (fDebug.get()) UniLog.logClass(p_pool, "setDefaultJdbcPool()");
		JdbcPool oldPool = defaultJdbcPool;
		synchronized (lockObject) {
			defaultJdbcPool = p_pool;
		}
		return(oldPool);
	}
	public JdbcPool() {
		thread = ThreadPool.getDefaultThreadPool().getThread(this, "JdbcPool");
		UniLog.log1("[%s] start jdbcpool", appName);
		thread.start();
	}
	public JdbcPool(String p_appName) {
		appName = p_appName;
		thread = ThreadPool.getDefaultThreadPool().getThread(this, "JdbcPool");
		UniLog.log1("[%s] start jdbcpool", appName);
		thread.start();
		synchronized(jdbcPoolHash) {
			jdbcPoolHash.put(p_appName, this);
		}
	}
	public void run() {
		shutdownFlag.set(0);
		mainLoop();
		shutdownFlag.set(2);
	}
	public void mainLoop() {
		
		UniLog.log1("[%s] waiting for connection string", appName);
		synchronized (this) {
			for (;;) {
				try {
					wait(100);
					if (shutdownFlag.get() > 0) {
						UniLog.log1("[%s] shutdown in progress. abort", appName);
						return;
					}
					if (connStr != null) {
						break;
					}
					UniLog.log1("[%s] wait for connectionstring", appName);
					wait(10000);
				} 
				catch (Exception ex) { UniLog.log(ex); }
			}
		}
		
		
		for (;;) {
			if (shutdownFlag.get() > 0) {
				UniLog.log1("[%s] shutdown in progress. abort", appName);
				return;
			}
			synchronized(cPool) {
				if (fExiting && curCnt == 0) {
					UniLog.log1("[%s] exiting thread", appName);
					break;
				}
				if (fPauseRequested) {
					try { 
						UniLog.log1("[%s] pause", appName);
						cPool.wait(1000); 
					} 
					catch (Exception ex) {
						UniLog.log(ex);
					}
					continue;
				}
			}
			if (timeToMakeConnection > 0) {
				if (timeToMakeConnection < System.currentTimeMillis()) {
					makeupConnections();
					timeToMakeConnection = 0;
				}
				else {
					synchronized (this) {
						try { 
							wait(5000); 
						} 
						catch (Exception ex) {
							UniLog.log(ex);
						}
					}
				}
			}
			else {
				makeupConnections();
			}
			checkAllConnections();
			synchronized (cPool) {
				if (cPool.size() >= connCnt || curCnt >= maxConnCnt) {
					try { 
						// UniLog.logClass(this, "["+appName+"]:"+"sleeping ...");
						cPool.wait(5000); 
						// UniLog.logClass(this, "["+appName+"]:"+"wakeup ...");
					} catch (Exception ex) {
						UniLog.log(ex);
					};
				}
			}
		}
	}
	public void makeupConnections() {
		if (cPool.size() >= connCnt) {
			return;
		}
		if (fDebug.get()) UniLog.log1("[%s] called pool:%d curCnt:%d connCnt:%d maxConnCnt:%d", appName, cPool.size(), curCnt, connCnt, maxConnCnt);
		for (int i=0; i<maxConnCnt; i++) {
			if (cPool.size() >= connCnt) {
				break;
			}
			synchronized (this) {
				if (curCnt >= maxConnCnt) {
					UniLog.log1("[%s] max maconnection reached %d/%d", appName, curCnt, maxConnCnt);
					break;
				}
			}
			if (fDebug.get()) UniLog.log1("[%s] make new connection", appName);
			try {
				if (fStopMakeupConnections){
					break;
				}
				Connection conn = null;
				conn = getJdbcConnection();
				int myNextId;
				synchronized (cPool) {
					myNextId = nextId++;
				}
				ConnectionRecord cr = new ConnectionRecord(conn, myNextId, curBatchId);
				synchronized (cPool) {
					if (fDebug.get()) UniLog.logClass(JdbcPool.this, "["+appName+"]:"+"cPool.put("+cr.getId()+")");
					cPool.put(cr, cr);
					UniLog.log1("[%s] new cr:%s pool:%d rpool:%d curCnt:%d connCnt:%d maxConnCnt:%d", appName, cr, cPool.size(), rPool.size(), curCnt, connCnt, maxConnCnt);
					
				}
				new Thread(new Runnable() {
					public void run() {
						synchronized(consumerLock) {
							consumerLock.notifyAll();
						}
					}
				}).start();
			}
			catch (Exception ex) {
				UniLog.log1("error:" + ex.getMessage());
				if (fDebug.get()) {
					UniLog.log(ex);
				}
				UniLog.log1("[%s] resume in after timeout", appName);
				timeToMakeConnection = System.currentTimeMillis() + 60*1000;
				return;
			}
		}
	}

	public void checkAllConnections() {
		if (fDebug.get()) UniLog.log1("[%s] called pool:%d curCnt:%d connCnt:%d maxConnCnt:%d", appName, cPool.size(), curCnt, connCnt, maxConnCnt);
		Vector crs = null;
		synchronized (cPool) {
			crs = new VectorUtil(cPool.elements()).toVector();
		}
		for (int i=0; i<crs.size(); i++) {
			ConnectionRecord cr = (ConnectionRecord) crs.elementAt(i);
			boolean fSeized = false;
			synchronized (cr) {
				if (cr.isAvailable()) {
					cr.setAvailable(false);
					fSeized = true;
				}
			}
			if (fSeized) {
				boolean fgoaway = false;
				if (cr.getBatchId() != curBatchId) {
					if (fDebug.get()) UniLog.logClass(JdbcPool.this, "["+appName+"]:"+"cr.getBatchId()="+cr.getBatchId()+", curBatchId="+curBatchId+" restarting ...");
					fgoaway = true;
				} else if (cr.isAlive())
					cr.setAvailable(true);
				else
					fgoaway = true;
				if (fgoaway) {
					synchronized (cPool) {
						int oSize = cPool.size();
						if (fDebug.get()) UniLog.log1("[%s] cPool.remove(%d)",cr.getId());
						cPool.remove(cr);
						if (fDebug.get()) UniLog.log1("[%s] cPool.size(): %d->%d [%d]",oSize,cPool.size(),rPool.size());
						try { 
							cr.discard(); 
						} 
						catch (Exception ex2) {
							UniLog.log(ex2);
						}
					}
				}
			}
		}
		if (fDebug.get()) UniLog.log1("[%s] time left to release %d", appName, (timeToReleaseConnection-System.currentTimeMillis()));
		if (timeToReleaseConnection > System.currentTimeMillis()) {  //andrew221230 seems timeToReleaseConnection is useless
			return;
		}
		if (fDebug.get()) UniLog.log1("[%s] releasing connection connCnt:%d", appName, connCnt);
		timeToReleaseConnection = System.currentTimeMillis() + 5000;
		synchronized (cPool) {
			crs = new VectorUtil(cPool.elements()).toVector();
		}
		int releaseCnt = connCnt;  //andrew210222 limit cleanup for each round. look strange but fine
		
		//cleanup connection record and maintain minimum cr
		for (int i=connCnt; i<crs.size(); i++) {
			if (fDebug.get()) UniLog.log1("cleanup loop i:%d size:%d",i,crs.size());
			if (releaseCnt <= 0) {
				break;
			}
			releaseCnt--;
			ConnectionRecord cr = (ConnectionRecord) crs.elementAt(i);
			boolean fSeized = false;
			synchronized (cr) {
				if (cr.isAvailable()) {
					cr.setAvailable(false);
					fSeized = true;
				}
			}
			if (fSeized) {
				synchronized (cPool) {
					int oSize = cPool.size();
					if (fDebug.get()) UniLog.logClass(JdbcPool.this, "["+appName+"]:"+"cPool.remove("+cr.getId()+")");
					cPool.remove(cr);
					if (fDebug.get()) UniLog.logClass(JdbcPool.this, "["+appName+"]:"+"cPool.size(): "+oSize+"->"+cPool.size()+" ["+rPool.size()+"]");
					try {
						if (fDebug.get()) UniLog.log1("discard cr:" + cr);
						cr.discard();
					} 
					catch (Exception ex2) {
						UniLog.log(ex2);
					}
				}
			}
		}
	}
	public Connection getConnection() {
		return(getConnection(60));
	}
	public Connection getConnection(int p_timeoutsec) {
		if (fDebug.get()) UniLog.log1("called");
		long startTime = System.currentTimeMillis();
		long timeout = startTime + p_timeoutsec*1000;
		timeToReleaseConnection = System.currentTimeMillis() + 5000;
		for (;;) {
			
			//get connection timeout or exiting
			if (System.currentTimeMillis() > timeout || fExiting) {
				UniLog.logClass(this, "["+appName+"]:"+"Waiting for connection ("+appName+") ... timeout ["+rPool.size()+"]");
				if (fDebug.get()) UniLog.log(new Exception("Waiting for connection ("+appName+") ... timeout ["+rPool.size()+"]"));
				return(null);
			}
			
			
			//allocate and return connection record
			synchronized (cPool) {
				if (!fPauseRequested) {
					
					//when cPool is availble
					if (cPool.size() > 0) {
						
						//allocate connection record
						ConnectionRecord retCr = null;
						for (Enumeration en=cPool.elements(); en.hasMoreElements(); ) {
							ConnectionRecord cr = (ConnectionRecord) en.nextElement();
							synchronized (cr) {
								if (cr.isAvailable()) {
									cr.setAvailable(false);
									retCr = cr;
									break;
								}
							}
						}
						
						//when connection record available
						if (retCr != null) {
							
							int oSize = cPool.size();
							if (fDebug.get()) UniLog.logClass(JdbcPool.this, "["+appName+"]:"+"cPool.remove("+retCr.getId()+")");
							
							//consume cPool
							cPool.remove(retCr);
							
							//produce rPool
							Integer ii = new Integer(retCr.getId());
							if (fDebug.get()) UniLog.logClass(JdbcPool.this, "["+appName+"]:"+"rPool.put("+ii.intValue()+")");
							StringWriter sw = new StringWriter();
							PrintWriter pw = new PrintWriter(sw);
							new Exception("getConnection() stack trace for debug").printStackTrace(pw);
							rPool.put(ii, sw.toString());
							try {
								sw.close();
								pw.close();
							} 
							catch (Exception ex) {
								UniLog.log("Exception ignored.");
								UniLog.log(ex);
							}
							if (fDebug.get()){
								UniLog.logClass(JdbcPool.this, "["+appName+"]:"+"cPool.size(): "+oSize+"->"+cPool.size()+" ["+rPool.size()+"]");
								UniLog.logClass(JdbcPool.this, "["+appName+"]:"+"getConnection() return in "+(System.currentTimeMillis()-startTime)+"ms");
							}
							
							//send notify to make more connection
							if (cPool.size() < connCnt) {
								if (fDebug.get()) UniLog.logClass(JdbcPool.this, "["+appName+"]:notify to make more connection");
								//wakeup threads that are waiting for cPool i.e. cPool.wait()
								cPool.notifyAll(); 
								//if (fDebug.get()) UniLog.logClass(JdbcPool.this, "["+appName+"]:notified");
							}
							return(retCr);
						}
					}
				}
			}
			
			//when connection record not available
			UniLog.log1("[%s] waiting for connection... pool:%d rpool:%d", appName, cPool.size(), rPool.size());
			synchronized (cPool) {
				if (fDebug.get()) UniLog.logClass(JdbcPool.this, "["+appName+"]:notify to make more connection");
				cPool.notifyAll();
				//if (fDebug.get()) UniLog.logClass(JdbcPool.this, "["+appName+"]:notified");
			}
			
			//wait and try again
			synchronized (consumerLock) {
				try {
					//UniLog.logClass(this, "["+appName+"]:"+"consumerLock.wait(1000)...");
					consumerLock.wait(1000);
					//UniLog.logClass(this, "["+appName+"]:"+"consumerLock.wait() awake");
				} catch (Exception ex) {
					UniLog.logClass(this, ex);
				}
			}
		}
	}
	private void addCurCnt(int p_inc) {
		/*
		try {
			if (p_inc < 0) throw new Exception("debug deduct");
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		*/
		synchronized(cPool) {
			int oldCnt = curCnt;
			curCnt += p_inc;
			if (fDebug.get()) UniLog.log1("total count %d->%d", oldCnt, curCnt);
		}
	}
	public void setMaxConnectionCount(int p_maxcnt) {
		int oldMaxConnCnt = maxConnCnt;
		if (p_maxcnt < LL_MAX_CONNECTION_COUNT) {
			UniLog.log1("less than lower limit");
			maxConnCnt = LL_MAX_CONNECTION_COUNT;
		}
		else if (p_maxcnt > UL_MAX_CONNECTION_COUNT) {
			UniLog.log1("more than upper limit");
			maxConnCnt = UL_MAX_CONNECTION_COUNT;
		}
		else {
			maxConnCnt = p_maxcnt;
		}
		UniLog.log1("[%s] maxConnCnt(%d) %d->%d", appName, p_maxcnt, oldMaxConnCnt, maxConnCnt);
	}
	public void setConnectionCount(int p_count) {
		connCnt = p_count;
	}
	public void setConnectionString(String p_string, String p_loginname, String p_passwd) throws Exception {
		if (connStr == null) {
			synchronized (this) {
				if (connStr == null) {
					if(StringUtils.isBlank(p_string)) {
						throw new Exception("Connection String is blank");
//						UniLog.logClass(this, "["+appName+"]:"+"connStr is blank");
					}
					loginname = p_loginname;
					passwd = p_passwd;
					UniLog.logClass(this, "["+appName+"]:"+"connStr set to "+p_string);
					connStr = p_string;
					this.notifyAll();
				}
			}
		}
	}
	public String getConnectionString() {
		return(connStr);
	}
	public void setConnectionString(String p_string) throws Exception {
		setConnectionString(p_string, null, null);
	}
	public void resume() throws Exception {
		UniLog.log1("[%s] called", appName);
		synchronized (this) {
			if (!fStopMakeupConnections) {
				throw(new Exception("pauseAllConnections not in action"));
			}
			fStopMakeupConnections = false;
		}
		synchronized (cPool) {
			if (!fPauseRequested) {
				throw(new Exception("pauseAllConnections not in action"));
			}
			fPauseRequested = false;
			cPool.notifyAll();
		}
		synchronized (consumerLock) {
			consumerLock.notifyAll();
		}
		UniLog.log1("[%s] done", appName);
	}
	public void waitAllConnectionsFinish() throws Exception {
		UniLog.log1("[%s] called", appName);
		synchronized (cPool) {
			for (;;) {
				if (cPool.size() > 0) {
					ConnectionRecord retCr = null;
					for (Enumeration en=cPool.elements(); en.hasMoreElements(); ) {
						ConnectionRecord cr = (ConnectionRecord) en.nextElement();
						synchronized (cr) {
							if (cr.isAvailable()) {
								cr.setAvailable(false);
								try {
									cr.discard();
								} catch (Exception ex2) {
									UniLog.log(ex2);
								}
								cPool.remove(cr);
							}
						}
					}
				}
				if (curCnt == 0) {
					break;
				}
				try {
					UniLog.logClass(this, "["+appName+"]:"+"waitAllConnectionsFinish: Waiting ...");
					cPool.wait(1000);
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		}
		UniLog.log1("[%s] done", appName);
	}
	public void pauseAllConnections() throws Exception {
		UniLog.log1("[%s] called", appName);
		synchronized (this) {
			if (fStopMakeupConnections) {
				throw(new Exception("other pauseAllConnections in progress ..."));
			}
			fStopMakeupConnections = true;
		}
		synchronized (cPool) {
			if (fPauseRequested) {
				throw(new Exception("other pauseAllConnections in progress ..."));
			}
			fPauseRequested = true;
			for (;;) {
				if (cPool.size() > 0) {
					ConnectionRecord retCr = null;
					for (Enumeration en=cPool.elements(); en.hasMoreElements(); ) {
						ConnectionRecord cr = (ConnectionRecord) en.nextElement();
						synchronized (cr) {
							if (cr.isAvailable()) {
								cr.setAvailable(false);
								try {
									cr.discard();
								} catch (Exception ex2) {
									UniLog.log(ex2);
								}
								int oSize = cPool.size();
								if (fDebug.get()) UniLog.logClass(JdbcPool.this, "["+appName+"]:"+"cPool.remove("+cr.getId()+")");
								cPool.remove(cr);
								if (fDebug.get()) UniLog.logClass(JdbcPool.this, "["+appName+"]:"+"cPool.size(): "+oSize+"->"+cPool.size()+" ["+rPool.size()+"]");
							}
						}
					}
				}
				if (curCnt == 0) {
					break;
				}
				try {
					UniLog.log1("[%s] waiting", appName);
					cPool.wait(1000);
				} 
				catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		}
		UniLog.logClass(this, "["+appName+"]:"+"pauseAllConnections done");
	}
	public void restartAllConnections(int p_waittime) throws Exception {
		UniLog.log1("[%s] called. waittime:%d", appName, p_waittime);
		long timeToCheckAlive = System.currentTimeMillis() + p_waittime*1000;
		Hashtable existingId = new Hashtable();
		synchronized (cPool) {
			for (Enumeration en=cPool.elements(); en.hasMoreElements(); ) {
				ConnectionRecord cr = (ConnectionRecord) en.nextElement();
				Integer ii = new Integer(cr.getId());
				existingId.put(ii, ii);
			}
			for (Enumeration en=rPool.keys(); en.hasMoreElements(); ) {
				Integer ii = (Integer) en.nextElement();
				existingId.put(ii, ii);
			}
			curBatchId++;
			cPool.notifyAll();
			for (;;) {
				boolean found = false;
				for (Enumeration en=cPool.elements(); en.hasMoreElements(); ) {
					ConnectionRecord cr = (ConnectionRecord) en.nextElement();
					if (existingId.get(new Integer(cr.getId())) != null) {
						found = true;
						break;
					}
				}
				if (!found) {
					for (Enumeration en=rPool.keys(); en.hasMoreElements(); ) {
						if (existingId.get((Integer) en.nextElement()) != null) {
							found = true;
							break;
						}
					}
				}
				if (!found)
					break;
				if (timeToCheckAlive < System.currentTimeMillis()) {
					UniLog.log1("[%s] timeout", appName);
					throw(new Exception("Timeout waiting for all connection to be restarted"));
				}
				UniLog.log1("[%s] waiting...", appName);
				cPool.wait(1000);
			}
		}
	}
	public void setExiting() {
		UniLog.log1("[%s] called", appName);
		fExiting = true;
	}
	protected void finalize() throws Throwable {
		UniLog.log1("[%s] called", appName);
		super.finalize();
	}
	public CellCollection getRunningStatistics() {
		CellCollection cc = new CellCollection();
		// cc.putValue("connStr", connStr);
		cc.putValue("curCnt", curCnt);
		cc.putValue("connCnt", connCnt);
		cc.putValue("maxConnCnt", maxConnCnt);
		cc.putValue("keepAlive", keepAlive);
		cc.putValue("cPool", cPool.size());
		cc.putValue("rPool", rPool.size());
		cc.putValue("fPauseRequested", fPauseRequested);
		cc.putValue("fStopMakeupConnections", fStopMakeupConnections);
		cc.putValue("nextId", nextId);
		cc.putValue("curBatchId", curBatchId);
		cc.putValue("timeToMakeConnection", timeToMakeConnection);
		cc.putValue("appName", appName == null ? "" : appName);
		cc.putValue("fExiting", fExiting);
		return(cc);
	}
	public String getRunningStatisticsXML() {
		return getRunningStatistics().toXML(new StringBuffer()).toString();
	}
	public String getRunningStatisticsJson() {
		try {
			JSONObject xmlJSONObj = XML.toJSONObject(getRunningStatisticsXML());
			String jsonStr = xmlJSONObj.toString(3);
			return jsonStr;
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return "";
		}
		
	}
	public String logStatus(String p_labelfmt, Object...p_args) {
		String label = "[debug]";
		ArrayList<String> logs = new ArrayList<String>();
		if (StringUtils.isNotBlank(p_labelfmt)) {
			label = String.format("[%-25s]", StringUtils.substring(String.format(p_labelfmt, p_args), 0, 25));
		}
		logs.add(String.format("%s appName:%s obj:%s fExiting:%s shutdownFlag:%s",label,appName, this, fExiting,shutdownFlag));
		logs.add(String.format("%s curCnt:%d connCnt:%d maxConnCnt:%d cPool:%d rPool:%d",label,curCnt,connCnt,maxConnCnt, cPool.size(), rPool.size()));
		logs.add(String.format("%s fPauseRequested:%s fStopMakeupConnections:%s nextId:%d curBatchId:%d",label,fPauseRequested, fStopMakeupConnections,nextId, curBatchId));
		logs.add(String.format("%s timeToMakeConnection:%d timeToReleaseConnection:%d curTime:%d",label,timeToMakeConnection,timeToReleaseConnection,System.currentTimeMillis()));
		synchronized(cPool) {
			int idx = 0;
			int cnt = cPool.size();
			if (cnt == 0) {
				logs.add(String.format("%s crlist:empty",label));
			}
			for (ConnectionRecord cr : (Collection<ConnectionRecord>) cPool.values()) {
				logs.add(String.format("%s crlist:%d/%d id:%d reuse:%s avail:%s %s",label,++idx,cnt,cr.getId(),cr.getReuse(),cr.isAvailable(), cr.toString()));
			}
		}
		StringBuilder sb = new StringBuilder();
		for (String log : logs) {
			UniLog.log1(log);
			sb.append(log + "\n");
		}
		return sb.toString();
	}
	public static int findConnectionType(Connection p_dbConnection) {
		if (p_dbConnection instanceof com.uniinformation.utils.JdbcPool.ConnectionRecord) {
			ConnectionRecord conn = (ConnectionRecord) p_dbConnection;
			return(findConnectionType(conn.getConnection()));
		}
		if (p_dbConnection.getClass().getName().equals("com.uniinformation.scorpion.driver.ScpConnection"))
			return(JDBC_SCORPION);
		else if (p_dbConnection.getClass().getName().startsWith("com.mysql.jdbc")
				|| p_dbConnection.getClass().getName().startsWith("com.mysql.cj.jdbc"))
			return(JDBC_MYSQL);
		else if (p_dbConnection.getClass().getName().startsWith("org.postgresql."))
			return(JDBC_POSTGRESQL);
		else
			return(JDBC_ORACLE);
	}
	public static boolean setDebug(boolean p_flag) {
		boolean oldFlag = fDebug.get();
		fDebug.set(p_flag);
		return(oldFlag);
	}
	public static boolean getDebug() {
		return fDebug.get();
	}
	Connection getJdbcConnection() throws Exception {
		Connection conn = null;
		String jdbcUrl = getDriverConnectionString(connStr);
		if (loginname != null) {
			conn = DriverManager.getConnection(jdbcUrl, loginname, passwd);
		}
		else {
			conn = DriverManager.getConnection(jdbcUrl);
		}
		return(conn);
	}

	/** Add the legacy MySQL SSL default only to MySQL URLs. Other JDBC URLs,
	 * including PostgreSQL, must be passed to their driver unchanged. */
	static String getDriverConnectionString(String p_connStr) {
		if (p_connStr == null) return(null);
		String lower = p_connStr.toLowerCase(Locale.ROOT);
		if (lower.startsWith("jdbc:mysql:")
				&& !lower.contains("usessl=")
				&& !lower.contains("sslmode=")) {
			return(p_connStr + (p_connStr.indexOf('?') >= 0 ? "&" : "?") + "useSSL=false");
		}
		return(p_connStr);
	}
	
	/***
	 * 200121: for handle ThreadPoolThread.pleaseDie
	 */
	@Override
	public void shutdown() {
		shutdown(0);
	}
	/***
	 * 
	 * @param p_wait - max wait duration in ms
	 * @return
	 */
	public boolean shutdown(int p_wait) {
		UniLog.log1("called wait:%s",p_wait);
		if (shutdownFlag.get() != 0) {
			UniLog.log1("shutdown was called already. flag:%s", shutdownFlag.get());
			return false;
		}
		UniLog.log1("shutdown start");
		shutdownFlag.set(1);
		if (p_wait <= 0) {
			return true;
		}
		try {
			int timeRemain = p_wait >= 60000 ? p_wait : 60000; //set min timeout
			for (;;) {
				if (shutdownFlag.get() >= 2) {
					UniLog.log1("shutdown done");
					return true;
				}
				UniLog.log1("waiting for shutdown (%d)...", timeRemain);
				Thread.sleep(1000);
				timeRemain-=1000;
				if (timeRemain < 0) {
					UniLog.log1("timeout");
					return false;
				}
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
			return false;
		}
		
	}
	public static void main(String args[]) throws Exception{
		//selfTest1(args);
		//selfTest2(args);
		//selfTest3(args);
		//selfTest4(args);
	}
	static void selfTest1(String[] args) throws Exception {
		final JdbcPool pool = new JdbcPool();
		pool.setConnectionCount(5);
		pool.setMaxConnectionCount(100);
		pool.setConnectionString("jdbc:scorpion:perfrpc:192.168.18.102:5033:cp");
		for (int j=0; j<10; j++) {
			UniLog.log("getting thread test-"+j+"...");
			ThreadPool.getDefaultThreadPool().getThread(
					new Runnable() {
						public void run() {
							try {
								for (int i=0; i<10; i++) {
									SelectUtil su = new SelectUtil();
									su.init(pool.getConnection());
									TableRec tr = su.getQueryResult("select * from vendor where vd_vcode = 'lai'", null);
									UniLog.log("tr.size()="+tr.size());
									su.close();
									UniLog.log("su.close()");
								}
							} catch (Exception ex) {
								UniLog.log(ex);
							}
						}
					}, 
					"test-"+j)
					.start();
			UniLog.log("thread test-"+j+".start() called...");
		}
		Thread.currentThread().sleep(20000);
		pool.pauseAllConnections();
	}
	static void selfTest2(String[] args) throws Exception {
		final JdbcPool pool = new JdbcPool();
		pool.setConnectionCount(1);
		pool.setConnectionString("jdbc:scorpion:perfrpc:203.161.226.105:6002:austar");
		SelectUtil su = new SelectUtil();
		su.init(pool.getConnection());
		UniLog.log("before query");
		TableRec tr = su.getQueryResult("select * from vendor", null);
		UniLog.log("after query");
		su.close();
	}
	static void selfTest3(String[] args) {
		try {
			//JdbcPool.setDebug(true);
			JdbcPool pool = WebCoreUtil.getJdbcPoolByConnectionString("bischema",2,20,"jdbc:scorpion:perfrpc:dtqemu2.uniconn.com:3102:bischema:dbpath:/yic/v/bischema/data:chaindir:-p /yic/v/bischema/chn",null,null);
			Thread.sleep(1000);
			pool.logStatus("after create pool");
			HashMap<Connection,SelectUtil> connHM = new HashMap();
			for (int i=0; i<5; i++) {
				Connection conn = pool.getConnection();
				Thread.sleep(1000);
				pool.logStatus("#%d:%08x after get conn",i,conn.hashCode());
				SelectUtil su = new SelectUtil();
				su.init(conn);
				Thread.sleep(1000);
				pool.logStatus("#%d:%08x after su init",i,conn.hashCode());
				
				connHM.put(conn, su);
				TableRec tr = su.getQueryResult("select count(*) from ddd_database");
				//UniLog.log1("rec cnt:%d", tr.getRecordCount());
			}
			int idx = 0;
			for (Connection conn : connHM.keySet()) {
				idx++;
				SelectUtil su = connHM.get(conn);
				su.close();
				Thread.sleep(1000);
				pool.logStatus("#%d:%08x after su close",idx,conn.hashCode());
			}
			Thread.sleep(10000);
			pool.logStatus("after sleep");
			
			pool.shutdown(10000);
			pool.logStatus("after shutdown");
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		finally {
			System.exit(0);
			
		}
	}
//	static void selfTest4(String[] args) {
//		try {
//			//JdbcPool.setDebug(true);
//			JdbcPool pool = WebCoreUtil.getJdbcPoolByConnectionString("chungkee",1,5,
//					"jdbc:scorpion:perfrpc:dtqemu2.hellovoice.com:19002:gl:dbpath:/yic/v/erp_v4/data_chungkee:chaindir:-p /yic/v/erp_v4/chn -p /yic/v/acc_v4/src/chn -p /yic/v/acc_v4/src/ar -p /yic/v/acc_v4/src/gl -p /yic/v/acc_v4/src/nchn"
//					,null,null);
//			
//			for (int i=0;i<5;i++) {
//				SelectUtil su = new SelectUtil();
//				su.init(pool.getConnection());
//				TableRec tr = su.getQueryResult("select count(*) from vendor");
//				//RpcClient rpc = su.getRpcClient();
//				//ReturnMsg rtn = rpc.callm("uname");
//				//UniLog.log1("rtn:%s", rtn.toString());
//				su.close();
//				ZkUtil.dumpData(tr);
//			}
//		}
//		catch(Exception ex) {
//			ex.printStackTrace();
//		}
//		finally {
//			System.exit(0);
//			
//		}
//	}
}
