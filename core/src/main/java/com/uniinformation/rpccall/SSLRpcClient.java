package com.uniinformation.rpccall;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Vector;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class SSLRpcClient extends RpcClient{
	private String keyStoreFile = "";
	private String keyStorePassword = "";
	private String trustStoreFile = "";
	private String trustStorePassword = "";
	private String keyPassword = "";
	public SSLRpcClient(String p_host, int p_port) {
		super(p_host, p_port);
	}
	public Socket createSocket(String p_host, int p_port) throws UnknownHostException, IOException {
		//SocketFactory sf = SSLSocketFactory.getDefault();
		SocketFactory sf = getSocketFactory();
		SSLSocket socket = (SSLSocket) sf.createSocket(p_host, p_port);
		return(socket);
	}
	public SSLRpcClient setSSLKeyStore(String p_keyStoreFile, String p_keyStorePassword, String p_keyPassword){
		keyStoreFile = p_keyStoreFile;
		keyStorePassword = p_keyStorePassword;
		keyPassword = p_keyPassword;
		return(this);
	}
	public SSLRpcClient setSSLTrustStore(String p_trustStoreFile, String p_trustStorePassword){
		trustStoreFile = p_trustStoreFile;
		trustStorePassword = p_trustStorePassword;
		return(this);
	}
	private SSLSocketFactory getSocketFactory() throws IOException{
		try{
			char[] keyStorePasswordChars = keyStorePassword.toCharArray();
			char[] keyPasswordChars = keyPassword.toCharArray();
			//FileInputStream keyFile = new FileInputStream(keyStoreFile);
			InputStream keyFile = Thread.currentThread().getContextClassLoader().getResourceAsStream(keyStoreFile);
			
			char[] trustStorePasswordChars = trustStorePassword.toCharArray();
			//FileInputStream storeFile = new FileInputStream(trustStoreFile);
			InputStream storeFile = Thread.currentThread().getContextClassLoader().getResourceAsStream(trustStoreFile);

			//init keystore
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(keyFile, keyStorePasswordChars);

			//init key factory
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyManagerFactory.init(keyStore, keyPasswordChars);
			
			
			//trust store
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(storeFile, trustStorePasswordChars);
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			tmf.init(trustStore);

			//init key manager
			KeyManager keyManagers[] = keyManagerFactory.getKeyManagers();

			//init context
			//Remark: SSLv3 (a.k.a. SSL); TLSv1 (a.k.a. TLS); TLSv1.1, supported since Java SE 6u111; TLSv1.2, supported since Java SE 6u121
			//SSLContext sslContext = SSLContext.getInstance("SSL");
			SSLContext sslContext = SSLContext.getInstance("TLSv1");
			sslContext.init(keyManagers, tmf.getTrustManagers(), new SecureRandom());

			//get factory
			//SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
			SSLSocketFactory factory = sslContext.getSocketFactory();
			return(factory);
		}
		catch(Exception ex){
			ex.printStackTrace();
			throw new IOException(ex.getMessage());
		}

	}
	public static void main(String[] args) {
		
		SSLRpcClient rpcclient = new SSLRpcClient("dtqemu2.uniconn.com" , 3434).setSSLKeyStore("testssl-rpcclient-any.uniconn.com.jks", "abcdef","abcdef").setSSLTrustStore("testssl-rpc-ssca.jks","abcdef");
		Vector<Object> arglist = new Vector<Object>();
		arglist.addElement("arg1");
		arglist.addElement("arg2");
		rpcclient.open();
		Value val = rpcclient.callSegment("segment_name",arglist);
		rpcclient.close();
		if(val != null) System.out.println(val.toString());
		else System.out.println("FAIL");
	}
}
