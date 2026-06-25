package com.uniinformation.zkbi.afs;


import com.uniinformation.bicore.BiResult;
import com.uniinformation.zkbi.erpv4.ZkBiComposerOsOrderDet;

public class ZkBiComposerAfsOsOrderDet extends ZkBiComposerOsOrderDet{	
	@Override
	 protected String getOrdType(BiResult p_result) {
 		if(p_result.getCell("inv_invno").getString().startsWith("AQM")) {
 			return("machine");
		} else {
 			return("parts");
 		}
	 }
	@Override
	 protected String getPoView(BiResult p_result,String p_ordType) {
 		if(p_ordType.equals("machine")) {
 			return("afs.AfsPoMc");
 		} else {
 			return("afs.AfsPoParts");
 		}
	 }
    
}
