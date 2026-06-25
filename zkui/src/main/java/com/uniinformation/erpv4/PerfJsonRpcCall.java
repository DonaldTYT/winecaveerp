package com.uniinformation.erpv4;

import com.google.gson.JsonObject;
import com.uniinformation.bicore.bischema.ExcelCellCollection;
import com.uniinformation.bicore.bischema.ExcelWorkSheetCache;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellCollectionToJsonInterface;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.RpcServerConnection;
import com.uniinformation.rpccall.RpcServlet;
import com.uniinformation.rpccall.Value;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

import java.io.InputStream;
import java.io.OutputStream;

import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Filedownload;




public class PerfJsonRpcCall implements com.uniinformation.zkf.ZkfAction,RpcServlet{

	protected String cocode;
	protected String bccy;
	protected SessionHelper sh;
	protected String rtnFileName = null;
	protected String rtnFileType = null;
	protected String rtnSaveName = null;
	protected void afterRpcConnect(RpcClient p_rpc) {
	}
	
	@Override
	public ReturnMsg processAction(String p_id, SessionHelper p_sh, CellCollection p_col, JsonObject p_actionData, InputStream p_upload,Component p_target) throws Exception {
		// TODO Auto-generated method stub
		sh = p_sh;
		JSONObject jo = CellCollectionToJsonInterface.CellCollectionToJSON(p_col);
		RpcClient rpc = p_sh.getRpcClient();
		rpc.setRpcServlet(this.getClass().getName(), this);
		if(p_col.testCell("co_cocode") != null) {
			cocode = p_col.getCellString("co_cocode");
		} else {
			cocode = BiConfig.getDefaultCoCode(p_sh);
		}
		bccy = BiConfig.getBaseCcy(p_sh,cocode);
		
				
		rpc.callSegment("setCocodeBaseccy",
				new VectorUtil()
				.addElement(cocode)
				.addElement(bccy)
				.toVector()
				);
		rpc.callSegment("jdbc_putenv",
						new VectorUtil()
							.addElement("CHINESE=1")
							.toVector()
							);
		rpc.callSegment("callfunction",
						new VectorUtil()
							.addElement("settranslate")
							.addElement(1)
							.toVector()
							);
		afterRpcConnect(rpc);
		Value val = null;
		if(p_upload != null) {
    		val = rpc.callSegment("unique_filename",
    				new VectorUtil()
    				.addElement("/tmp")
    				.addElement("jrpc")
    				.toVector()
    						);
    		String fname = val.toString();
    		OutputStream os = p_sh.newErpFileOutputStream(fname);	
    		byte[] b = new byte[1024];
    		int len;
    		while((len = p_upload.read(b, 0, 1024)) > 0){
    		    os.write(b, 0, len);
    		}
    		
    		os.close();
			val = rpc.callSegment(p_id, 
					new VectorUtil()
					.addElement(jo.toString())
					.addElement(fname)
					.toVector()
				);
		} else {
			val = rpc.callSegment(p_id, 
					new VectorUtil()
					.addElement(jo.toString())
					.toVector()
				);
		}
		if(val != null && val.toString().startsWith("OK")) {
			String ms = "";
			if(val.toString().startsWith("OK  ")) {
				ms = val.toString().substring(4);
			} 
			if(rtnFileName != null) {
				InputStream is = p_sh.newErpFileInputStream(rtnFileName);
    			Filedownload.save(is, rtnFileType, rtnSaveName);
			}
    		return(new ReturnMsg(true,ms));
		} else {
			String ms = "Reason Unknown";
			if(val != null && val.toString().startsWith("FAIL")) {
				ms = val.toString().substring(4);
				if(ms.equals("")) ms = "Reason Unknown";
			}
			return(new ReturnMsg(false,ms));
		}
	}

	@Override
	public void init_servlet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void close_servlet() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setConnection(RpcServerConnection conn) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String ping() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public String getGlDaBalance(String p_cocode,String p_ano,String p_ccy,java.util.Date p_date) {
		try {
			double lbal = 0.0;
			double bal = 0.0;
			GlBalanceCalculation.BalanceAccumulatorPair acuPair = GlBalanceCalculation.getDaBalanceAccumulator(sh, p_cocode, p_ano, p_ccy);
			if(acuPair != null) {
				bal = acuPair.cacu.getBalanceBegin(p_date);
				lbal = acuPair.lacu.getBalanceBegin(p_date);
			}
			return(String.format("OK  %14.2f%14.2f", bal,lbal));
		} catch (Exception ex) {
			UniLog.log(ex);
			return ("FAIL"+ex.toString());
		}
	}

	public String clearAllAcu(String p_cocode) {
		try {
			GlBalanceCalculation.clearAcu(sh, p_cocode, null);
			return("OK");
		} catch (Exception ex) {
			UniLog.log(ex);
			return ("FAIL"+ex.toString());
		}
	}
	
	public String clearAllBrCache() {
		ExcelWorkSheetCache.clearBrCache();
		return("OK");
		/*
		try {
			GlBalanceCalculation.clearAcu(sh, p_cocode, null);
			return("OK");
		} catch (Exception ex) {
			UniLog.log(ex);
			return ("FAIL"+ex.toString());
		}
		*/
	}
	public String setReturnFileName(String p_filename,String p_savename,String p_filetype) {
		rtnFileName = p_filename;
		rtnSaveName = p_savename;
		rtnFileType = p_filetype;
		return("OK");
	}
}
