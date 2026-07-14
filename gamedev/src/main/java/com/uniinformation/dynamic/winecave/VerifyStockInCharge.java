package com.uniinformation.dynamic.winecave;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Filedownload;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultInvoiceBase;
import com.uniinformation.bicore.wc.BiResultStockInEx;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrParser.ChnftrGetImageInterface;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public class VerifyStockInCharge  extends BiActionHandler implements JxActionListener {

	BiResultStockInEx br;
	public VerifyStockInCharge(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		if(p_bibase != null) useAsync = p_bibase.getSessionHelper().getAllowBatchPrtdocAsync(); else useAsync = false;
		// TODO Auto-generated constructor stub
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		br = (BiResultStockInEx) p_result;
		try {
			br.beginWork();
		} catch(Exception ex) {
			UniLog.log(ex);
			return new ReturnMsg(false,ex.toString());
		}
		return ReturnMsg.defaultOk;
	}

	@Override
	public ReturnMsg processAction(BiResult p_br,int p_idx) {
		br.fetchOneRecV(p_idx);
		try {
			List<String> errList = br.verify_storage_charge();
			if(errList != null && !errList.isEmpty()) {
				return(new ReturnMsg(false,errList.toString()));
			}
		} catch (Exception ex) {
			br.rollbackWork();
			UniLog.log(ex);
			return new ReturnMsg(false,ex.toString());
		}
		return ReturnMsg.defaultOk;
	} 
	
	@Override
	public ReturnMsg afterAction(BiResult p_result) {
		br.rollbackWork();
		return(ReturnMsg.defaultOk);
	}
	@Override
	public boolean isVisible(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(false);
		return(true);
	}

	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		if(p_isBatch) {
			return(false);
		} else {
			if(!p_br.getSessionHelper().hasAccessRight("#prtinv")) {
				return(true);
			}
			if(p_br.inBeginWork()) return(true);
			return(false);
		}
	}
	
	public ReturnMsg isRunnable(BiResult br,boolean isBatch) {
		return(ReturnMsg.defaultOk);
	}
	
	@Override
	public void afterActionAsync(BiActionHandler.AfterActionCallback cb) {
		UniLog.log1("afterActionAsync start");
		ReturnMsg rtn = afterAction(null);
		biBase.hideProgressPanel();
		cb.callback(rtn);
	}
	
	@Override
	public boolean preserveListOrder () {
		return(true);
	}

	@Override
	public void actionPerformed(JxField field) {
		// TODO Auto-generated method stub
		
	}
}
