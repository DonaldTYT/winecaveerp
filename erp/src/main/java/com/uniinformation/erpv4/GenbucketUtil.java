package com.uniinformation.erpv4;

import com.uniinformation.bicore.BiView;
import com.kyoko.common.*;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
public class GenbucketUtil {
	static public int WEIGHTED_AVERAGE_ORG = 2000000001;
	static public int WEIGHTED_AVERAGE_ORGMIN = 2000000001;
	
	static String translateGenBucketName(String p_fieldName) {
		if("stg_freestock".equals(p_fieldName)) return ("Free Stock");
		if("pdls_stockqty".equals(p_fieldName)) return ("Stock Quantity");
		return(p_fieldName);
	}
	
	static String parseJSONError(JSONObject jsonItem, String p_errStr,SessionHelper p_sh,HashSet<Integer> ignoreIrgSet) throws Exception {

		String errStr = p_errStr;
		if(jsonItem.optJSONArray("keys") != null) {
			String prefix = jsonItem.getString("prefix");
			JSONArray keys = jsonItem.getJSONArray("keys");
			if(prefix.equals("QODETSTATUS")) {
				if(Erpv4Config.getAllowNegativeStock(p_sh)) {
					return(errStr);
				}
			}
			if(prefix.equals("PODETSTATUS")) {
				int org = jsonItem.getInt("pds_org");
					if(org == 0 || org >= WEIGHTED_AVERAGE_ORGMIN) return(errStr);
				int irg = jsonItem.getInt("pds_irg");
				if(ignoreIrgSet != null && ignoreIrgSet.contains(irg)) return(errStr);
			}
			if(prefix.equals("PODETLOCSTATUS")) {
				int irg = jsonItem.getInt("pdls_irg");
				if(ignoreIrgSet != null && ignoreIrgSet.contains(irg)) return(errStr);
				String loccode = jsonItem.getString("pdls_loc");
				if(Erpv4Config.getLocationAllowNegative(p_sh, loccode)) {
					return(errStr);
				}
				BiView stv ;
				String locCodeHdr = "Location";
				stv = p_sh.getBiSchema().getViewByName("erpv4.LocationCode");
				if(stv != null) {
					locCodeHdr = stv.getColumnByLabel("loc_code").getEngName();
				}
				if(errStr == null) errStr = "Balance Control Denied" + "\n"; else errStr += "\n";
//				irg = Integer.parseInt(jsonItem.getString("pdls_irg"));
				String stockView = Erpv4Config.getStockViewId(p_sh);
				SelectUtil su =  p_sh.getBiSchema().getSelectUtil();
				stv = p_sh.getBiSchema().getViewByName(stockView);
				errStr += stv.getColumnByLabel("st_icode").getEngName() + " :";
				TableRec tr = su.getQueryResult("select * from stock where st_irg  = " + irg);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					errStr += tr.getFieldString("st_icode") + "\n";
				}
				
				errStr += locCodeHdr + " :" + jsonItem.getString("pdls_loc")+ "\n"; 
				errStr += translateGenBucketName(jsonItem.getString("qtyName"))+ ":" + jsonItem.getDouble("qtyValue")+ "\n"; 
				return(errStr);
			}
			if(prefix.equals("STOCKSERIAL")) {
				int irg = jsonItem.getInt("stsn_irg");
				if(ignoreIrgSet != null && ignoreIrgSet.contains(irg)) return(errStr);
				String loccode = jsonItem.getString("stsn_loc");
				String stsn_bin = jsonItem.optString("stsn_bin");
				if((stsn_bin == null || stsn_bin.isEmpty())  && Erpv4Config.getLocationAllowNegative(p_sh, loccode)) {
					return(errStr);
				}
				BiView stv ;
				String locCodeHdr = "Expiry Date/Serial No";
				stv = p_sh.getBiSchema().getViewByName("erpv4.LocationCode");
				if(stv != null) {
					locCodeHdr = stv.getColumnByLabel("loc_code").getEngName();
				}
				if(errStr == null) errStr = "Balance Control Denied" + "\n"; else errStr += "\n";
				String stockView = Erpv4Config.getString(p_sh, "customStockView");
				if(stockView == null || stockView.equals("")) {
					stockView = Erpv4Config.getStockViewId(p_sh);
				}
				SelectUtil su =  p_sh.getBiSchema().getSelectUtil();
				stv = p_sh.getBiSchema().getViewByName(stockView);
				errStr += stv.getColumnByLabel("st_icode").getEngName() + " :";
				TableRec tr = su.getQueryResult("select * from stock where st_irg  = " + irg);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					errStr += tr.getFieldString("st_icode") + "\n";
				}
				
				errStr += locCodeHdr + " :" + jsonItem.getString("stsn_loc")+ "\n"; 
				errStr += translateGenBucketName(jsonItem.getString("qtyName"))+ ":" + jsonItem.getDouble("qtyValue")+ "\n"; 
				return(errStr);
			}
			if(prefix.equals("STOCKGEN")) {
				if(Erpv4Config.getAllowNegativeStock(p_sh)) {
					return(errStr);
				}
				int irg = jsonItem.getInt("stg_irg");
				if(ignoreIrgSet != null && ignoreIrgSet.contains(irg)) return(errStr);
				if(errStr == null) errStr = "Balance Control Denied" + "\n"; else errStr += "\n";
//				int irg = Integer.parseInt(jsonItem.getString("stg_irg"));
				String stockView = Erpv4Config.getString(p_sh, "customStockView");
				if(stockView == null || stockView.equals("")) {
					stockView = Erpv4Config.getStockViewId(p_sh);
				}
				BiView stv = p_sh.getBiSchema().getViewByName(stockView);
				errStr += stv.getColumnByLabel("st_icode").getEngName() + " :";
				SelectUtil su =  p_sh.getBiSchema().getSelectUtil();
				TableRec tr = null;
				try {
					tr = su.getQueryResult("select * from stock where st_irg  = " + irg);
					su.close();
				} catch(Exception ex) {
					if(su != null) {
						su.close();
					}
					UniLog.log(ex);
					throw(ex);
				}
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					errStr += tr.getFieldString("st_icode") + "\n";
				}
				errStr += translateGenBucketName(jsonItem.getString("qtyName"))+ ":" + jsonItem.getDouble("qtyValue")+ "\n"; 
				return(errStr);
			}
			if(prefix.equals("PODETLOCBINSTATUS")) {
				int irg = jsonItem.getInt("pdlbs_irg");
				if(ignoreIrgSet != null && ignoreIrgSet.contains(irg)) return(errStr);
				/*
				String loccode = jsonItem.getString("pdlbs_loc");
				if(Erpv4Config.getLocationAllowNegative(p_sh, loccode)) {
					return(errStr);
				}
				*/
				String bincode = jsonItem.getString("pdlbs_bin");
				if(StringUtils.isBlank(bincode)) {
					return(errStr);
				}
			}
			if(errStr == null) errStr = "Balance Control Denied" + "\n"; else errStr += "\n";
			for(int k =0;k < keys.length();k++) {
				String key = keys.getString(k);
				String value = jsonItem.getString(key);
				errStr += key + ":" + value + "\n"; 
			}
			errStr += translateGenBucketName(jsonItem.getString("qtyName"))+ ":" + jsonItem.getDouble("qtyValue")+ "\n"; 
			
		}	
		return(errStr);
	}
	static public ReturnMsg qoGenBucketCheckResult(String p_str,SessionHelper p_sh) {
		return(qoGenBucketCheckResult(p_str,p_sh,null));
	}
	static public ReturnMsg qoGenBucketCheckResult(String p_str,SessionHelper p_sh,HashSet<Integer> ignoreIrgSet) {
		try {
			JSONArray jsonArray;
			try {
				JSONObject jsonFile = new JSONObject(p_str);
				String jsonFilePath = jsonFile.getString("filepath");
				InputStream in = p_sh.newErpFileInputStream(jsonFilePath);
				Writer writer = new StringWriter();
			    IOUtils.copy(in, writer);
			    jsonArray = new JSONArray(writer.toString());
				
			} catch (JSONException jex) {
				jsonArray = new JSONArray(p_str);
			}
			String errStr = null;
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonItem = jsonArray.getJSONObject(i);
				errStr = parseJSONError(jsonItem, errStr,p_sh,ignoreIrgSet);
				/*
				String prefix = jsonItem.getString("prefix");
				if(prefix.equals("PODETSTATUS")) {
					int org = jsonItem.getInt("pds_org");
					if(org == 0 || org >= WEIGHTED_AVERAGE_ORGMIN) continue;
				}
				UniLog.log("Balance Control Denied : " + jsonItem.getString("qtyName") + " = " + jsonItem.getDouble("qtyValue")+"["+ p_str +"]");
				
				if(errStr == null) errStr = "Balance Control Denied" + "\n";
				if(jsonItem.optJSONArray("keys") != null) {
					JSONArray keys = jsonItem.getJSONArray("keys");
					for(int k =0;k < keys.length();k++) {
						String key = keys.getString(k);
						String value = jsonItem.getString(key);
						errStr += key + ":" + value + "\n"; 
					}
					
				}
				errStr += jsonItem.getString("qtyName") + " shorts " + jsonItem.getDouble("qtyValue") + "\n";
				*/
				
			}	
			if(errStr == null) return(ReturnMsg.defaultOk);
			ReturnMsg msg = new ReturnMsg(false,errStr);
			msg.setFatal(true);
			if(p_sh.getSchemaDebugFlag()) msg.setData(p_str);
			return(msg);
		} catch (Exception e) {
			UniLog.log("parseConditionPreset fail " + e.toString());
			ReturnMsg msg  = new ReturnMsg(false,"Json Parse Error");
			msg.setFatal(true);
			return(msg);
		}	
	}
}
