package com.uniinformation.erpv4;

import java.io.InputStream;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
//import org.zkoss.zsoup.helper.StringUtil;

import com.google.gson.JsonObject;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class ZkfActionPrtdoc implements com.uniinformation.zkf.ZkfAction {

	@Override
	public ReturnMsg processAction(String p_id, SessionHelper p_sh, CellCollection p_col, JsonObject p_actionData,
			InputStream p_upload, Component p_target) throws Exception {
		if(StringUtils.isBlank(p_id)) {
			throw new Exception("Prtdoc Error : prtdocClass is null");
		}
		BiCellCollection bc = (BiCellCollection) p_col;
		BiResultErpv4 br = (BiResultErpv4) bc.getBr();
		byte[] outbytes = br.PrintOneDocument(p_id,p_actionData);
		ZkUtil.showPdfDialog(p_target.getRoot(), br.getSessionHelper(), outbytes, "download.pdf");
		return null;
	}

}
