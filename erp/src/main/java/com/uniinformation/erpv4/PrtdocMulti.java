package com.uniinformation.erpv4;

import java.util.Date;
import java.util.Vector;

import com.google.gson.JsonObject;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.prtdoc.PrtdocJson;
import com.kyoko.common.*;
import com.uniinformation.utils.GsonUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;

public class PrtdocMulti extends PrtdocClass  {
	protected BiResultErpv4 br;
    protected PrtdocJson ppj;
    int docCnt;
    protected String coCode;
	public PrtdocMulti (BiResultErpv4 p_br,JsonObject p_actionData) throws Exception {
		br = p_br;
		coCode = GsonUtil.getString(p_actionData, "coCode",null);
		String docCode = GsonUtil.getString(p_actionData, "docCode",null);
		String paperType = GsonUtil.getString(p_actionData, "paperType",null);
		if(coCode == null) coCode = BiConfig.getDefaultCoCode(br.getSessionHelper());
		if(docCode == null) docCode = "GENINV01";
		if(paperType == null) paperType = "A4P";
    	ppj = PrtdocJson.newPrtdocJson(	
    				coCode,
    				paperType,
    			    docCode,
    			    "erpv4_printDocument",
    			    PrtdocJson.Encoding.UTF8
    	);
		docCnt = 0;
	}

	@Override
	public void print() throws Exception {
		if(docCnt > 0) {
        	if(docCnt > 0) ppj.newContent();
		}
        docCnt++;
	}

	@Override
	public PrtdocJson getPrintDocJson() throws Exception {
		// TODO Auto-generated method stub
		return ppj;
	}

}
