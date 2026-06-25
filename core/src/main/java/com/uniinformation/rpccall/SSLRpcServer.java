package com.uniinformation.rpccall;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class SSLRpcServer extends RpcServer{
	private String keyStoreFile = "";
	private String keyStorePassword = "";
	private String keyPassword = "";
	public SSLRpcServer(int portno) {
		super(portno);
	}
	public SSLRpcServer(int portno ,int backlog)
	{
		super(portno, backlog);
	}
	public SSLRpcServer(int portno,int backlog,String bindAddr)
	{
		super(portno, backlog, bindAddr);
	}
	public SSLRpcServer setSSLKeyStore(String p_keyStoreFile, String p_keyStorePassword, String p_keyPassword){
		keyStoreFile = p_keyStoreFile;
		keyStorePassword = p_keyStorePassword;
		keyPassword = p_keyPassword;
		return(this);
	}
	protected ServerSocket createServerSocket(int p_portno, int p_backlog, InetAddress p_bindaddr) throws IOException{
		//SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		SSLServerSocketFactory factory = getSocketFactory();
		SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(p_portno, p_backlog, p_bindaddr);
		return(serverSocket);
	}
	protected ServerSocket createServerSocket(int p_portno, int p_backlog) throws IOException{
		//SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		SSLServerSocketFactory factory = getSocketFactory();
		SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(p_portno, p_backlog);
		return(serverSocket);
	}
	protected ServerSocket createServerSocket(int p_portno) throws IOException{
		//SSLServerSocketFactory factory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		SSLServerSocketFactory factory = getSocketFactory();
		SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(p_portno);
		return(serverSocket);
	}
	private SSLServerSocketFactory getSocketFactory() throws IOException{
		try{
			char[] keyStorePasswordChars = keyStorePassword.toCharArray();
			char[] keyPasswordChars = keyPassword.toCharArray();
			FileInputStream keyFile = new FileInputStream(keyStoreFile);

			//init keystore
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(keyFile, keyStorePasswordChars);
			
			//init key factory
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, keyPasswordChars);
			
			//init key manager
			KeyManager keyManagers[] = keyManagerFactory.getKeyManagers();
			
			//init context
			SSLContext sslContext = SSLContext.getDefault();
			sslContext.init(keyManagers, null, new SecureRandom());
			
			//get factory
			SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
			return(factory);
		}
		catch(Exception ex){
			ex.printStackTrace();
			throw new IOException(ex.getMessage());
		}

	}
	public static void main(String args[]){
		//SSLRpcServer rpcserver = new SSLRpcServer(1234);
		SSLRpcServer rpcserver = new SSLRpcServer(1234).setSSLKeyStore("/tmp/abc.jks", "abc", "abc");
		rpcserver.loadService();
		rpcserver.run();
	}

}
