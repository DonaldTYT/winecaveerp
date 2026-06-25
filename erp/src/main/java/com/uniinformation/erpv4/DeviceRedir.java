package com.uniinformation.erpv4;

import java.io.File;
import java.net.DatagramPacket;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kyoko.common.*;
import com.uniinformation.utils.UdpRedir;
import com.uniinformation.utils.UniLog;

public class DeviceRedir {
	public static AtomicBoolean fDebug = new AtomicBoolean(true);
	private final static String deviceMapFileName = "deviceredir.json";
	
	public static class DevInfo {
		String devid;
		String devip;
		int devport;
		int udpseq;
		String version;
		String sessionMsg;
	}
	
	public static void main(String args[]) throws Exception{
		//default value
		String lAddr = "0.0.0.0";
		int lPort = 5678;
		String cAddr = "localhost";
		int cPort = 5679;
		
		//parse main param
		try {
			final Options options = new Options();
			new Option("laddr", "laddr", true, "e.g. 0.0.0.0"){{this.setRequired(false); options.addOption(this);}};
			new Option("lport", "lport", true, "e.g. 5678"){{this.setRequired(false); options.addOption(this);}};
			new Option("caddr", "caddr", true, "e.g. localhost"){{this.setRequired(false); options.addOption(this);}};
			new Option("cport", "cport", true, "e.g. 5679"){{this.setRequired(false); options.addOption(this);}};

			CommandLineParser cliParser = new DefaultParser();
			CommandLine cli = cliParser.parse(options, args);

			if (StringUtils.isNotBlank(cli.getOptionValue("laddr"))){
				lAddr = cli.getOptionValue("laddr").trim();
			}
			if (StringUtils.isNotBlank(cli.getOptionValue("lport"))){
				lPort = Integer.parseInt(cli.getOptionValue("lport"));
			}

			if (StringUtils.isNotBlank(cli.getOptionValue("caddr"))){
				cAddr = cli.getOptionValue("caddr").trim();
			}
			if (StringUtils.isNotBlank(cli.getOptionValue("cport"))){
				cPort = Integer.parseInt(cli.getOptionValue("cport"));
			}
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		UniLog.log1("main param - lAddr:%s lPort:%d cAddr:%s cPort:%d",lAddr, lPort, cAddr, cPort);
		
		
		//read devicemap from json
		File deviceMapFile = null;
		try {
			//load the mapping file (optional. load this file if required to change modify deviceid)
			deviceMapFile = new File(Thread.currentThread().getContextClassLoader().getResource(deviceMapFileName).getFile());
			UniLog.log1("load %s ok", deviceMapFile.getAbsolutePath());
		}
		catch(Exception ex) {
			UniLog.log1("load %s fail:%s",deviceMapFileName, ex.getMessage());
		}
		
		ObjectMapper mapper = new ObjectMapper();
		final ConcurrentHashMap<String,String> deviceHM = deviceMapFile == null ? new ConcurrentHashMap() : mapper.readValue(deviceMapFile, ConcurrentHashMap.class);
		if (fDebug.get()) UniLog.log1("deviceHM:" + deviceHM);
		
		
		//construct redirect thread
		new UdpRedir(lAddr, lPort, cAddr, cPort){
			@Override
			public byte[] processData(byte[] data, DatagramPacket rawPacket) {
				if (fDebug.get()) UniLog.log1("from:%s:%d data:[%s]", rawPacket.getAddress().getHostAddress(), rawPacket.getPort(), new String(data).trim());
				try {
				
					// construct devinfo class
					String msg = new String(data);
					DevInfo devinfo = new DevInfo();
					devinfo.devid = StringUtil.strpart(msg, 0, 20).trim();
					devinfo.devip = StringUtil.strpart(msg, 20, 20).trim();
					devinfo.devport = Integer.parseInt(StringUtil.strpart(msg, 40, 10) .trim());
					devinfo.udpseq  = Integer.parseInt(StringUtil.strpart(msg, 50, 10) .trim());
					devinfo.version = StringUtil.strpart(msg, 60, 10).trim();
					devinfo.sessionMsg = StringUtil.strpart(msg, 70, -1).trim();
					
					// call translation
					if(translateDevInfo(devinfo, rawPacket.getAddress().getHostAddress(), rawPacket.getPort())) {
						// format udp message
						msg = String.format("%-20.20s%-20.20s%10d%10d%-10.10s%s",devinfo.devid,devinfo.devip,devinfo.devport,devinfo.udpseq,devinfo.version,devinfo.sessionMsg);
						return msg.getBytes();
					} 
					else {
						//not required to translate, return original data
						return data;
					}
				}
				catch(Exception ex) {
					UniLog.log1("error:" +  ex.getMessage());
				}
				return(null);
			}
			
			private boolean translateDevInfo(DevInfo devinfo,String fromAddress,int fromPort) {
				// default to skip udp if no translation rue matched
				if (fDebug.get()) UniLog.log1("process translate ["+devinfo.devid+"] ip ["+devinfo.devip+"] port ["+devinfo.devport+"] data["+devinfo.sessionMsg+"]");
				
				String newid = deviceHM.get(devinfo.devid); //format ShortCode + P/S + ID e.g. TSS01, CTS01, CTP01
				if (StringUtils.isNotBlank(newid)){
					devinfo.devid = newid;
					//if (fDebug.get()) UniLog.log1("after translate ["+devinfo.devid+"] ip ["+devinfo.devip+"] port ["+devinfo.devport+"] data["+devinfo.sessionMsg+"]");
					return true;
				}
				return(false);
			}
				
			
		}.setSoTimeout(5000).setDebug(true).start();
	}
}
