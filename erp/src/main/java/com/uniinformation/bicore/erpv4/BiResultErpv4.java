package com.uniinformation.bicore.erpv4;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import org.json.JSONObject;

import com.uniinformation.bicore.erpv4.Erpv4BaseCellCollection;
import com.google.gson.JsonObject;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.erpv4.PrtdocMulti;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Strval;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.DynamicClassLoader;
import com.kyoko.common.*;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultErpv4 extends BiResult {
	
	protected String logUserPrefix=null;
	

	public BiResultErpv4(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
		
	}
	
	@Deprecated
	public BiResultErpv4(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
		
	}
	
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new Erpv4BaseCellCollection(p_parent, this));
	}
	
	static public String getCodeByRgControl(BiResult p_br,String p_cocode,String p_codeType,Date p_date) throws Exception {
		RpcClient rpc = p_br.getSelectUtil().getRpcClient();
		rpc.callSegment("setCocodeBaseccy",
				new VectorUtil()
				.addElement(p_cocode)
				.addElement(BiConfig.getBaseCcy(p_br.getSessionHelper(),p_cocode))
				.toVector()
				);
		Value val = rpc.callSegment("getrg_byrgcontrol_bycategory",
				new VectorUtil()
				.addElement(p_codeType)
				.addElement(p_date)
				.toVector()
				);
		if(val == null || !(val instanceof Strval)) {
			throw new Exception("Get " + p_codeType +" Number Failed");
		}
		return(val.toString());
	}
	
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(pcol);
		if((logUserPrefix == null) || (rtnMsg != null && !rtnMsg.getStatus())) return(rtnMsg);
		try {
			if(pcol.testCell(logUserPrefix+"_cuser") != null)
				pcol.getCell(logUserPrefix+"_cuser").set(su.getLoginId());
			if(pcol.testCell(logUserPrefix+"_cdate") != null)
				pcol.getCell(logUserPrefix+"_cdate").set(new java.util.Date());
		} catch (CellException cex) {
			UniLog.log(cex);
		}
		return(rtnMsg);
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(pcol);
		if((logUserPrefix == null) || (rtnMsg != null && !rtnMsg.getStatus())) return(rtnMsg);
		try {
			if(pcol.testCell(logUserPrefix+"_uuser") != null)
				pcol.getCell(logUserPrefix+"_uuser").set(su.getLoginId());
			if(pcol.testCell(logUserPrefix+"_udate") != null)
				pcol.getCell(logUserPrefix+"_udate").set(new java.util.Date());
		} catch (CellException cex) {
			UniLog.log(cex);
		}
		return(rtnMsg);
	}
	
	public ReturnMsg PrintOneDocument(String p_prtdocClass,OutputStream os,String p_coCode,String p_docCode,String p_paperType,JSONObject p_option) {
		try {
			String coCode = p_coCode;
			String docCode = p_docCode;
			String paperType = p_paperType;
			if(coCode == null) coCode = BiConfig.getDefaultCoCode(getSessionHelper());
			if(docCode == null) docCode = "GENINV01";
			if(paperType == null) paperType = "A4P";
    		PrtdocJson ppj = PrtdocJson.newPrtdocJson(	
    				coCode,
    				paperType,
    			    docCode,
    			    "erpv4_printDocument"
    				) ;
    		PrtdocClass jpi = null;
//			Class[]	paramTypes = new Class[]{BiResultErpv4.class,PrtdocJson.class,JSONObject.class};
//    		Class prtdocClass = Class.forName(p_prtdocClass);
//    		Constructor constructor = prtdocClass.getConstructor(paramTypes);
//    		if(constructor == null) {
//    			return(new ReturnMsg(false,"Fail PirintInvoiceClass "+ p_prtdocClass + " not found"));
//    		}	
//    		jpi = (PrtdocPrintInvoice) constructor.newInstance(this,ppj,p_option);
// 			jpi.print();
//    		return(ppj.toPdfStream(os, getSessionHelper()));

			Class[]	paramTypes = new Class[]{BiResultErpv4.class,PrtdocJson.class,JSONObject.class};
//			Object[] params = new Object[] {this,ppj,p_option};
    		jpi = (PrtdocClass) DynamicClassLoader.loadClass2(p_prtdocClass, paramTypes, this,ppj,p_option);
 			jpi.print();
    		return(ppj.toPdfStream(os, getSessionHelper()));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Fail " + ex.toString()));
		}	
	}

	public byte[] PrintOneDocument(String p_prtdocClass,JsonObject p_option) throws Exception {
    		PrtdocMulti jpi = null;
			Class[]	paramTypes = new Class[]{BiResultErpv4.class,JsonObject.class};
    		jpi = (PrtdocMulti) DynamicClassLoader.loadClass2(p_prtdocClass, paramTypes, this,p_option);
 			jpi.print();
 			ByteArrayOutputStream os = new ByteArrayOutputStream();
    		jpi.getPrintDocJson().toPdfStream(os, getSessionHelper());
    		return(os.toByteArray());
	}
	
	public byte[] PrintMultiDocument(String p_prtdocClass,JsonObject p_option,List<Integer> p_recs) throws Exception {
    		PrtdocMulti jpi = null;
			Class[]	paramTypes = new Class[]{BiResultErpv4.class,JsonObject.class};
    		jpi = (PrtdocMulti) DynamicClassLoader.loadClass2(p_prtdocClass, paramTypes, this,p_option);
    		for(Integer recidx : p_recs) {
    			fetchOneRecV(recidx);
    			jpi.print();
    		}
 			ByteArrayOutputStream os = new ByteArrayOutputStream();
    		jpi.getPrintDocJson().toPdfStream(os, getSessionHelper());
    		return(os.toByteArray());
	}
}
