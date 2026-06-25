package com.uniinformation.zkbi.afs;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.zkbi.erpv4.ZkBiComposerOsDeli;

public class ZkBiComposerAfsOsDeli extends ZkBiComposerOsDeli {
	 protected String getOrdType(BiResult p_result) {
	   if(p_result == null) return("machine");
       if(p_result.getCell("inv_invno").getString().startsWith("AQM")) {
    	   return("machine");
       } else {
    	   return("parts");
       }
	 }
	 protected String getDoView(BiResult p_result,String p_ordType) {
		if(p_ordType.equals("machine")) {
			return("afs.AfsDoMc");
		} else {
			return("afs.AfsDoParts");
		}
	 }
}
