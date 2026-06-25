package com.uniinformation.bicore.clinic;

import java.io.ByteArrayInputStream;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultPubDocType extends BiResult {
	private static final int RG_KEY = 16903;
	String agentId = null;
	private byte[] pdfData;
	private boolean needRemovePdf;
	public BiResultPubDocType(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col) {
		UniLog.logm(this,"biBeforeAddCurrent");
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(col);
		rtnMsg = super.biBeforeAddCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			if (col.getCell("bcpdt_rg").getInt() == 0)
				getCell("bcpdt_rg").set(getView().getSchema().getRg(this,"", RG_KEY));
			addPdfData(col);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,-1,ex.getMessage()));
		}
		return(new ReturnMsg(true));
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col){
		UniLog.logm(this,"biBeforeUpdateCurrent");
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(col);
		if(!rtnMsg.getStatus()) return(rtnMsg);
		try {
			updatePdfData(col);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,-1,ex.getMessage()));
		}
		return(new ReturnMsg(true));
	}
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col){
		UniLog.logm(this,"biBeforeDeleteCurrent");
		ReturnMsg rtnMsg = super.biBeforeDeleteCurrent(col);
		if(!rtnMsg.getStatus()) return(rtnMsg);
		try {
			removePdfData(col);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,-1,ex.getMessage()));
		}
		return(new ReturnMsg(true));
	}
	private void addPdfData(CellCollection col) throws Exception {
		if (pdfData != null) {
			String key = String.format("zkbi_bodychk_pubdtype_%010d", col.getCell("bcpdt_rg").getInt());
			UniLog.log("upload:" + key + "dataSize:" + ",encryptSize:" + pdfData.length);
			ByteArrayInputStream bis = new ByteArrayInputStream(pdfData);
			FilingUtil.storeFile(agentId, null, key, key, col.getCell("bcpdt_docfname").getString(), bis);
			bis.close();
			col.getCell("bcpdt_filkey").set(key);
		}
	}
	private void updatePdfData(CellCollection col) throws Exception {
		if (needRemovePdf)
			removePdfData(col);
		else if (pdfData != null)
			addPdfData(col);
	}
	private void removePdfData(CellCollection col) throws Exception {
		if (needRemovePdf) {
			String key = col.getCell("bcpdt_filkey").getString();
			if (StringUtils.isNotBlank(key)) {
				int rtnCode = FilingUtil.deleteFile(agentId, null, key);
				UniLog.logm(this, "remove pdf agent:%s key:%s return code %d", agentId, key, rtnCode);
			} 
			col.getCell("bcpdt_filkey").set("");
			col.getCell("bcpdt_docfname").set("");
		}
	}
	/***
	 * called by ComposerBase
	 * @param p_agentId
	 */
	public void setAgentId(String p_agentId){
		agentId = p_agentId;
	}
	public void setPdfData(byte[] data) {
		pdfData = data;
	}
	public void setNeedRemovePdf(boolean b) {
		needRemovePdf = b;
	}
}
