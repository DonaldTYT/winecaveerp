package com.uniinformation.axa;

import java.io.InputStream;
import java.util.HashSet;

import org.zkoss.zk.ui.Component;

import com.google.gson.JsonObject;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class EdiAction implements com.uniinformation.zkf.ZkfAction{

	@Override
	public ReturnMsg processAction(String p_id, SessionHelper p_sh, CellCollection p_col, JsonObject p_actionData, InputStream p_upload,Component p_target) throws Exception {
		// TODO Auto-generated method stub
		if(p_id.equals("uploadPHR"))  {
			ReturnMsg rtn = AxaEdi.uploadPHR(p_sh, p_upload);
			return(rtn);
		}
		if(p_id.equals("uploadPOL"))  {
			ReturnMsg rtn = AxaEdi.uploadPOL(p_sh, p_upload);
			return(rtn);
		}
		if(p_id.equals("uploadCVP"))  {
			ReturnMsg rtn = AxaEdi.uploadCVP(p_sh, p_upload);
			return(rtn);
		}
		if(p_id.equals("uploadSTA"))  {
			ReturnMsg rtn = AxaEdi.uploadSTA(p_sh, p_upload);
			return(rtn);
		}
		if(p_id.equals("uploadER2"))  {
			HashSet<AxaEdi.ERRTYPE> updSet = new HashSet<AxaEdi.ERRTYPE>();
			if(p_col.getCell("errAPHR") != null && p_col.getCell("errAPHR").getBoolean()) updSet.add(AxaEdi.ERRTYPE.APHR);
			if(p_col.getCell("errAPOL") != null && p_col.getCell("errAPOL").getBoolean()) updSet.add(AxaEdi.ERRTYPE.APOL);
			if(p_col.getCell("errACVP") != null && p_col.getCell("errACVP").getBoolean()) updSet.add(AxaEdi.ERRTYPE.ACVP);
			if(p_col.getCell("errSPCN") != null && p_col.getCell("errSPCN").getBoolean()) updSet.add(AxaEdi.ERRTYPE.SPCN);
			if(p_col.getCell("errCHGC") != null && p_col.getCell("errAPHR").getBoolean()) updSet.add(AxaEdi.ERRTYPE.CHGC);
			ReturnMsg rtn = AxaEdi.uploadER2(p_sh, p_upload,updSet);
			return(rtn);
		}
		if(p_id.equals("pollEmailEdi"))  {
			return AxaUtil.pollAndProcessEmail(true,1,p_sh);
		}
		if(p_id.equals("pollEmailNotice"))  {
			BiResult br = p_sh.getBiSchema().getViewByName("axa.EmailMessage").newBiResult(p_sh.getLoginId(), null, null, p_sh);
			AxaUtilEx.getEmailNotice(br,"imap.gmail.com", "tyt92791082@gmail.com", "ykvsymvlllpojzsi", false, 1000,true);
			return ReturnMsg.defaultOk;
		}
		
		return null;
		
	}
}
