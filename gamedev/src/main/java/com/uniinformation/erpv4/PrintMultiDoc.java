package com.uniinformation.erpv4;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.utils.MoneyToChinese;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultMO;
import com.uniinformation.erpv4.BatchPrtdocHandler;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;

public abstract class PrintMultiDoc extends BatchPrtdocHandler {
	protected String docCode;
	protected String cocode;
	protected String paperSize;
	protected SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    protected SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
    protected SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    protected DecimalFormat dfd = new DecimalFormat("$#,##0.00");
    protected DecimalFormat dfi = new DecimalFormat("#,##0");
	public PrintMultiDoc() {
		super(null);
		// TODO Auto-generated constructor stub
	}
	public PrintMultiDoc(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}

	protected void printOneDoc() throws Exception {
		
	}
	
	@Override
	public void print() throws Exception {
		ppj.setSkipB2GConvert(true);
		// TODO Auto-generated method stub
		if(docCnt > 0) {
        	if(docCnt > 0) ppj.newContent();
		}
    	
        docCnt++;
        String cocode = Erpv4Config.getDefaultCoCode(sh);
        Map<String, Object> coMap = Erpv4Config.getCoFieldMap(sh, cocode);
    	ppj.setTrailerAtLastPageOnly(false);
    	
    	printOneDoc();
    	/*
    	 * Start print content here
    	 */
    	
	}

    
	@Override
	protected ReturnMsg initPrtdoc() {
		try {
			if(docCode == null) docCode = "GENINV01";
			if(cocode == null) cocode = Erpv4Config.getDefaultCoCode(sh);
			if(paperSize == null) paperSize = "A4P";
			ppj = PrtdocJson.newPrtdocJson(	
    				cocode,
    				paperSize,
    			    docCode,
    			    "erpv4_printDocument"
			);
			ppj.setTopLeftMargin(0);
			docCnt = 0;
			//ppj.addHeaderField("doctitle","Quotation");
			//ppj.addHeaderImage("logo", Erpv4Config.getString(br.getSessionHelper(), "QuoBgImage"),0,0,0,800);    	
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return ReturnMsg.defaultOk;
	}

//	@Override
//	protected String getDocumentName(BiResult p_br) {
//		return ("Invoice");
//	}
}
