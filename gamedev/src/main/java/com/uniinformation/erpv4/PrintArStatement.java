package com.uniinformation.erpv4;

import java.util.HashMap;
import java.util.Hashtable;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.PrintMultiDoc;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.BiActionHandler.AfterActionCallback;
import com.uniinformation.zkbi.ZkBiComposerBase.MultiSortMap;

public class PrintArStatement extends PrintMultiDoc {

public PrintArStatement() {
	super(null);
}
public PrintArStatement(ZkBiComposerBase p_bibase) {
	super(p_bibase);
}

@Override
protected void printOneDoc() throws Exception {
	super.printOneDoc();
	
}

@Override
public boolean isVisible(BiResult p_br,boolean p_isBatch) {
	return(true);
}

@Override
public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
	if(p_br == null) return(true);
	int rowCnt = p_br.getRowCount();
	if(rowCnt <= 0) return(true);
	HashMap<String,Integer> cuHash = new HashMap<String,Integer>();
	try {
		for(int i=0;i<rowCnt;i++) {
			String cuCode = (String) p_br.getColumnValueFromCacheV("vd_vcode",i);
			Integer cuCnt = cuHash.get(cuCode);
			if(cuCnt == null) {
				cuHash.put(cuCode, 1);
			} else {
				return(true);
			}
		}
	} catch (Exception ex) {
		UniLog.log(ex);
		return(true);
	}
	return(false);
}
	
@Override
protected ReturnMsg initPrtdoc() {
	return(super.initPrtdoc());
}

@Override
public void afterActionCallback(BiResult br,ReturnMsg rtn) {
	if (biBase != null) {
		biBase.reload(br);
	}
}
@Override
protected String getDocumentName(BiResult p_br) {
	// TODO Auto-generated method stub
	return "Statement";
}

/*
@Override
public void afterActionAsync(AfterActionCallback cb) {
	super.afterActionAsync(cb);
	if (biBase != null) {
		biBase.reload(br);
	}
}
*/
}
