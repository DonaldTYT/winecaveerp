package com.uniinformation.utils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.google.api.client.util.IOUtils;

import net.lingala.zip4j.io.inputstream.ZipInputStream;
import net.lingala.zip4j.io.outputstream.ZipOutputStream;
import net.lingala.zip4j.model.LocalFileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.AesKeyStrength;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.model.enums.EncryptionMethod;

public class ZipUtil {
	/*
	public static void addFile(ZipOutputStream p_zos, String p_file, InputStream p_is) throws Exception{
		ZipEntry zipEntry = new ZipEntry(p_file);
		p_zos.putNextEntry(zipEntry);
	
		byte[] bytes = new byte[1024];
		int length;
		while ((length = p_is.read(bytes)) >= 0) {
			p_zos.write(bytes, 0, length);
		}
		p_zos.closeEntry();
		p_is.close();
	}
	public static void main(String args[]) throws Exception{
		
		FileOutputStream fos = new FileOutputStream("/tmp/a.zip");
		ZipOutputStream zos = new ZipOutputStream(fos);
		ZipUtil.addFile(zos, "file1.txt", new FileInputStream("c:/tmp/a1.txt"));
		ZipUtil.addFile(zos, "file2.txt", new FileInputStream("c:/tmp/a2.txt"));
		ZipUtil.addFile(zos, "dir/file2.txt", new FileInputStream("c:/tmp/a2.txt"));
		zos.close();
		fos.close();
	}
	*/
	
	
	/**
	 * generate zip file. caller responsible to close outStream and inStreams.
	 * @param p_password - non null, encrypt zip file
	 * @param p_aes - ture: aes256 encryption, much stronger, supported by 7zip, winzip, etc
	 *                false: standard encryption, much weaker, supported by stock window
	 * @param p_outStream - zip outStream
	 * @param p_inStreamNamePairs - inStream/byte[], fileName pair...
	 */
	public static void createZip(String p_password, boolean p_aes, OutputStream p_outStream, Object...p_inStreamNamePairs) throws Exception {
		try (ZipOutputStream zos = new ZipOutputStream(p_outStream, StringUtils.isNotEmpty(p_password) ? p_password.toCharArray() : null)) {
			ZipParameters parameters = new ZipParameters();
            parameters.setCompressionMethod(CompressionMethod.DEFLATE);
            parameters.setCompressionLevel(CompressionLevel.NORMAL);
            
            if (StringUtils.isNotEmpty(p_password)) {
                parameters.setEncryptFiles(true);
                if (p_aes) {
                    parameters.setEncryptionMethod(EncryptionMethod.AES);
                    parameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
                } else
                    parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
            }
            
            for (int i = 0; i < p_inStreamNamePairs.length; i += 2){
    			InputStream inStream = null;
    			String inName = (String) p_inStreamNamePairs[i+1];
    			if (p_inStreamNamePairs[i] instanceof InputStream)
    				inStream = (InputStream) p_inStreamNamePairs[i];
    			else if (p_inStreamNamePairs[i] instanceof byte[])
    				inStream = new ByteArrayInputStream((byte[])p_inStreamNamePairs[i]);
    			if (inStream == null) {
    				UniLog.log1("inStrem is null, ignore %s", inName);
    				continue;
    			}
    			parameters.setFileNameInZip(inName);
    			zos.putNextEntry(parameters);
    			IOUtils.copy(inStream, zos);
                zos.closeEntry();
    		}
		}
	}
	/*public static void createZip(String p_password, boolean p_aes, OutputStream p_outStream, Object...p_inStreamNamePairs) throws Exception {
		File tmpZipFileLock = File.createTempFile("ziputil",  ".lck");
		File tmpZipFile = new File(tmpZipFileLock.getAbsolutePath() +".tmp");
		UniLog.logm(null,"createZip: %s %s",  tmpZipFileLock.getAbsolutePath(), tmpZipFile.getAbsolutePath());
		
		//zip4j config
		ZipFile zipFile = new ZipFile(tmpZipFile);
		ZipParameters parameters = new ZipParameters();
		//parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
		//parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL); 
		//parameters.setSourceExternalStream(true);	
		parameters.setCompressionMethod(CompressionMethod.DEFLATE);
		parameters.setCompressionLevel(CompressionLevel.NORMAL);
		if (p_password != null && p_password.length() > 0){
			parameters.setEncryptFiles(true);
			if (p_aes){
				//parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_AES);
				//parameters.setAesKeyStrength(Zip4jConstants.AES_STRENGTH_256);
				parameters.setEncryptionMethod(EncryptionMethod.AES);
				parameters.setAesKeyStrength(AesKeyStrength.KEY_STRENGTH_256);
			}
			else{
				//parameters.setEncryptionMethod(Zip4jConstants.ENC_METHOD_STANDARD);
				parameters.setEncryptionMethod(EncryptionMethod.ZIP_STANDARD);
			}
			//parameters.setPassword(p_password);
			zipFile.setPassword(p_password.toCharArray());
		}
		
		//add file to zip
		for (int i=0; i<p_inStreamNamePairs.length; i+=2){
			InputStream inStream = null;
			String inName = (String) p_inStreamNamePairs[i+1];
			if (p_inStreamNamePairs[i] instanceof InputStream) {
				inStream = (InputStream) p_inStreamNamePairs[i];
			}
			else if (p_inStreamNamePairs[i] instanceof byte[]) {
				inStream = new ByteArrayInputStream((byte[])p_inStreamNamePairs[i]);
			}
			if (inStream == null) {
				UniLog.log1("inStrem is null, ignore %s", inName);
				continue;
			}
			parameters.setFileNameInZip(inName);
			zipFile.addStream(inStream, parameters);
		}
		zipFile.close();

		
		//write output file to outStream
		FileInputStream fis = null;
		try{
			byte[] bytes = new byte[1024];
			fis = new FileInputStream(tmpZipFile);
			int readLen;
			while ((readLen = fis.read(bytes)) >= 0) {
				p_outStream.write(bytes, 0, readLen);
			}
		}
		catch(Exception ex){
			throw(ex);
		}
		finally{
			if (fis != null){
				try{ fis.close(); }
				catch(Exception ex){}
			}
		}
		
		//remove tmpfile
		tmpZipFileLock.delete();
		tmpZipFile.delete();
	}*/
	public static void main(String args[]) throws Exception{
	   /*
	   //sample1 use vararg
	   FileOutputStream fos = new FileOutputStream("/tmp/abc.zip");
	   FileInputStream fis1 = new FileInputStream("/tmp/1.jpg");
	   FileInputStream fis2 = new FileInputStream("/tmp/2.jpg");
	   createZip("password", true,
				  fos,
				  fis1 , "1.jpg",
				  fis2 , "2.jpg");
	   fos.close();
	   fis1.close();
	   fis2.close();
	   */
	   /*
	    //sample2 use arraylist
		FileOutputStream fos = new FileOutputStream("/tmp/abc.zip");
		FileInputStream fis1 = new FileInputStream("/tmp/1.txt");
		FileInputStream fis2 = new FileInputStream("/tmp/2.txt");
		ArrayList <Object> zipArgs = new ArrayList<Object>();
		zipArgs.add(fis1);
		zipArgs.add("haha1.txt");
		zipArgs.add(fis2);
		zipArgs.add("haha2.txt");
		createZip("password", true,
				fos, zipArgs.toArray());
		fos.close();
		fis1.close();
		fis2.close();		
		*/
		
		
		//ZipUtil.untar(new File("/tmp/pgp/PEDD2.decrypted"), new File("/tmp/pgp/out"));
		/*
		ZipUtil.tar(Arrays.asList(
						Pair.of(new File("/tmp/a.txt"),"1/1.txt"),
						Pair.of(new File("/tmp/a.txt"),"1/2.txt"),
						Pair.of(new File("/tmp/a.txt"),"3.txt")
					), 
					new File("/tmp/pgp/out/a.tar"));
		*/
		//ZipUtil.untar(new File("/tmp/pgp/out/a.tar"), new File("/tmp/pgp/out/testtar"));
	}
	/***
	 * @param p_srcFile - source tar file
	 * @param p_destFolder - dest output folder
	 * @return
	 */
	public static List<File> untar(File p_srcFile, File p_destFolder){
		ArrayList<File> outFiles = new ArrayList<File>();
		if (p_srcFile == null || !p_srcFile.exists()) {
			UniLog.log1("invalid source file");
			return null;
		}
		try {
			FileInputStream fis = new FileInputStream(p_srcFile);
			TarArchiveInputStream tis = new TarArchiveInputStream(fis);
			//TarArchiveInputStream tis = new TarArchiveInputStream(new GzipCompressorInputStream( new BufferedInputStream(fis))); //for handle gz
			
			
			TarArchiveEntry tarEntry = null;

			while ((tarEntry = tis.getNextTarEntry()) != null) {
				String outFileName = p_destFolder + File.separator + tarEntry.getName();  //andrew210524: probably has vulnerability, should test the tarEntry name
				File outputFile = new File(outFileName);
				//UniLog.log1("outputFile:%s tarName:%s",outputFile, tarEntry.getName());
				if(tarEntry.isDirectory()){            
					if(!outputFile.exists()){
						outputFile.mkdirs();
					}
				}
				else{
					File parent = outputFile.getParentFile();
					if (!parent.exists()) {
						parent.mkdirs();
					}
		            byte [] buf = new byte[1024];
		            BufferedOutputStream bout = new BufferedOutputStream(new FileOutputStream(outputFile));
		            int len = 0;

		            while((len = tis.read(buf)) != -1) {
		                bout.write(buf,0,len);
		            }

		            bout.close();
					outFiles.add(outputFile);
				}
			}
			tis.close();
			//for (File of : outFiles) UniLog.log1("%s", of.getAbsolutePath());
			UniLog.log1("%d files extracted", outFiles.size());
			return outFiles;
		}
		catch(IOException ex) {
			ex.printStackTrace();
			return null;
		}
	}
	public static void tar(List<Pair<File,String>> fileList, File outFile) throws Exception {
	    OutputStream os = new FileOutputStream(outFile);
	    TarArchiveOutputStream aos = (TarArchiveOutputStream) new ArchiveStreamFactory().createArchiveOutputStream("tar", os);
	    for(Pair<File,String> pair : fileList) {
	    	File file = pair.getLeft();
	    	String name = pair.getRight();
	    	UniLog.log1("file %s", file.getAbsolutePath());
	        TarArchiveEntry entry = null;
	        if (StringUtils.isBlank(name)) {
	        	entry = new TarArchiveEntry(file);
	        }
	        else {
	        	entry = new TarArchiveEntry(file,name);
	        }
	    	
	        entry.setSize(file.length());
	        aos.putArchiveEntry(entry);
	        IOUtils.copy(new FileInputStream(file), aos);
	        aos.closeArchiveEntry();
	    }
	    aos.finish();
	    aos.close();
	    os.close();
	}
	
	public static InputStream extractSpecFileFromZip(InputStream zipInputStream, String password, String targetFileName) throws Exception {
		ZipInputStream zis = null;
		try {
			zis = new ZipInputStream(zipInputStream);
			if (StringUtils.isNotEmpty(password))
				zis.setPassword(password.toCharArray());

			LocalFileHeader localFileHeader;
			while ((localFileHeader = zis.getNextEntry()) != null) {
				if (localFileHeader.isDirectory())
					continue;
				if (targetFileName == null || localFileHeader.getFileName().equals(targetFileName)) {
					try {
						return zis;
					} finally {
						zis = null;
					}
				}
			}
			if (targetFileName != null)
				throw new IOException("File not found: " + targetFileName);
			else
				throw new IOException("File not found");
		} finally {
			if (zis != null)
				zis.close();
		}
    }
}
