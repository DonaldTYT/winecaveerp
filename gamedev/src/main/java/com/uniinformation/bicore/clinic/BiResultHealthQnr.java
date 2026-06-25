package com.uniinformation.bicore.clinic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellObjWrapper;
import com.uniinformation.cell.CellVector;
import com.uniinformation.utils.CryptoUtil;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.FilingUtilObject;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultHealthQnr extends BiResult{
	String agentId = null;
	JSONObject dataJson = null;
	public final static String dataJsonFilingKey = "health_qnr_data_%d";
	byte[] aesKey = null;
	
	public BiResultHealthQnr(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		UniLog.logm(this,"called");
	}
	
	private ReturnMsg syncToJson(LinkedHashMap<String,MutableTriple> p_hm){
		if (dataJson == null){
			return(new ReturnMsg(false,"data json is null"));
		}
		for (MutableTriple triObj : p_hm.values()){
			try{
				dataJson.put((String)triObj.getLeft(), triObj.getRight());
			}
			catch(Exception ex){
				//ex.printStackTrace();
			}
		}
		return(new ReturnMsg(true));
	}
	private ReturnMsg syncFromJson(LinkedHashMap<String,MutableTriple> p_hm){
		if (dataJson == null){
			return(new ReturnMsg(false,"data json is null"));
		}
		for (MutableTriple triObj : p_hm.values()){
			try{
				if (triObj.getRight() instanceof Integer){
					triObj.setRight(dataJson.getInt((String)triObj.getLeft()));
				}
				else if (triObj.getRight() instanceof Boolean){
					triObj.setRight(dataJson.getBoolean((String)triObj.getLeft()));
				}
				else if (triObj.getRight() instanceof String){
					triObj.setRight(dataJson.getString((String)triObj.getLeft()));
				}
			}
			catch(Exception ex){
				//ex.printStackTrace();
			}
			try{
				dataJson.put((String)triObj.getLeft(), triObj.getRight());
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			
			
		}
		return(new ReturnMsg(true));
		
	}
	public static ReturnMsg addQnrFields(CellCollection col, BiResult p_br){
		BiResultHealthQnr br = (BiResultHealthQnr)p_br;
		ReturnMsg rtnMsg = br.loadDataJson(col.getCell("bck_rg").getInt());
		if (!rtnMsg.getStatus()){
			return(rtnMsg);
		}
		col.delCell("custdata_pinfo");
		col.delCell("custdata_questions");
		
		//fill default value
		LinkedHashMap pinfo = new LinkedHashMap();
		
		col.putValue("custdata_pinfo", new CellObjWrapper(pinfo));
		//left 
		pinfo.put("bck_hybertension",MutableTriple.of("bck_hybertension","Hypertension \u9AD8\u8840\u58D3",false));
		pinfo.put("bck_diabetes",MutableTriple.of("bck_diabetes", "Diabetes \u7CD6\u5C3F\u75C5", false));
		pinfo.put("bck_heartdis",MutableTriple.of("bck_heartdis", "Heart Disease \u5FC3\u81DF\u75C5", false));
		pinfo.put("bck_kidneydis",MutableTriple.of("bck_kidneydis", "Kidney Disease \u814E\u75C5", false));
		pinfo.put("bck_mentaldisorder",MutableTriple.of("bck_mentaldisorder", "Mental disorder \u7CBE\u795E\u554F\u984C", false));
		pinfo.put("bdk_hereditarytdis",MutableTriple.of("bdk_hereditarytdis", "Hereditary Disease \u907A\u50B3\u6027\u75BE\u75C5", false));
		pinfo.put("bck_married",MutableTriple.of("bck_married", "Married \u5DF2\u5A5A", false));
		pinfo.put("bck_mf",MutableTriple.of("bck_mf", "Gender \u6027\u5225", "U"));
		pinfo.put("bck_numofchildren",MutableTriple.of("bck_numofchildren", "Number of Children \u5B50\u5973\u6578\u76EE", 0));
		pinfo.put("bck_smoking",MutableTriple.of("bck_smoking", "Smoking \u5438\u7159", false));
		pinfo.put("bck_smokingqty",MutableTriple.of("bck_smokingqty", "Quantity/Day \u6BCF\u65E5\u6578\u91CF", 0));
		pinfo.put("bck_alcohol",MutableTriple.of("bck_alcohol", "Alcohol \u559D\u6D12", false));
		pinfo.put("bck_alcoholqty",MutableTriple.of("bck_alcoholqty", "Quantity/Day \u6BCF\u65E5\u6578\u91CF", 0));
		pinfo.put("bck_exercise",MutableTriple.of("bck_exercise", "Exercise \u904B\u52D5", false));
		pinfo.put("signpad",MutableTriple.of("signpad", "", ""));
		br.syncFromJson(pinfo);
		//special handle signpad: middle store org value, right store new value, to allow reset
		((MutableTriple)pinfo.get("signpad")).setMiddle(((MutableTriple)pinfo.get("signpad")).getRight());
		
		//TODO: read value from dataJson
		/*
		if (true){
			return(new ReturnMsg(false,"sth wrong"));
		}
		*/
		
		LinkedHashMap<String,MutableTriple> questions = new LinkedHashMap<String,MutableTriple>();
		col.putValue("custdata_questions", new CellObjWrapper(questions));
		
		//for keep question properties (e.g. M/F/U)
		LinkedHashMap<String,HashMap> questionsProp = new LinkedHashMap<String,HashMap>();
		col.putValue("custdata_questions_prop", new CellObjWrapper(questionsProp));
		try{
			//read question list
			TableRec tr = p_br.getSelectUtil().getQueryResult("select * from qnrquestion order by qnrq_seqnum",null);
			for (int i=0;i<tr.getRecordCount();i++){
				tr.setRecPointer(i);
				boolean isValidQue = StringUtils.equals(tr.getFieldString("qnrq_status"),"Y");
				if (isValidQue){
					//questions.put(tr.getFieldString("qnrq_id"),MutableTriple.of(tr.getFieldString("qnrq_id"), tr.getFieldString("qnrq_desc"), false));
					questions.put(tr.getFieldString("qnrq_id"),MutableTriple.of(tr.getFieldString("qnrq_id"), tr.getFieldString("qnrq_desc"), -1));
					
					HashMap propHM = new HashMap();
					propHM.put("qnrq_mf",tr.getFieldString("qnrq_mf"));
					questionsProp.put(tr.getFieldString("qnrq_id"),propHM);
				}
			}
			br.syncFromJson(questions);
			return(new ReturnMsg(true));
		}
		catch(Exception ex){
			ex.printStackTrace();
			return(new ReturnMsg(ex));
		}
	}
	
	/*
	//remark: not require to call this function:
	@Override
	public boolean fetchOneRecV(int p_tridx){
		UniLog.logm(this,"called");
		boolean status = super.fetchOneRecV(p_tridx);
		if (!status) return (status);
		return(true);
	}
	*/
	
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col){
		UniLog.logm(this,"called");
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(col);
		//TODO: RG is ready here
		if(!rtnMsg.getStatus()) return(rtnMsg);
		
		rtnMsg = saveDataJson(col,col.getCell("bck_rg").getInt());
		if(!rtnMsg.getStatus()) return(rtnMsg);
		return(new ReturnMsg(true));
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col){
		UniLog.logm(this,"called");
		ReturnMsg rtnMsg = super.biBeforeUpdateCurrent(col);
		if(!rtnMsg.getStatus()) return(rtnMsg);
		UniLog.logm(this,"beforeUpdateCurrent: %s", col.getObjWrapper("custdata_pinfo").getObj());
		UniLog.logm(this,"beforeUpdateCurrent: %s", col.getObjWrapper("custdata_questions").getObj());
		
		rtnMsg = saveDataJson(col,col.getCell("bck_rg").getInt());
		if(!rtnMsg.getStatus()) return(rtnMsg);
		return(new ReturnMsg(true));
	}
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col){
		UniLog.logm(this,"called");
		ReturnMsg rtnMsg = super.biBeforeDeleteCurrent(col);
		if(!rtnMsg.getStatus()) return(rtnMsg);
		rtnMsg = deleteDataJson();
		if(!rtnMsg.getStatus()) return(rtnMsg);
		return(new ReturnMsg(true));
	}
	
	private boolean obtainAnsFromJson(String p_id){
		if (dataJson == null){
			return(false);
		}
		try{
			return(dataJson.getBoolean(p_id));
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return(false);
	}
	
	
	private ReturnMsg saveDataJson(CellCollection col, int p_rg){
		if (dataJson == null){
			return(new ReturnMsg(false,"data json is null"));
		}
		if (aesKey == null){
			return(new ReturnMsg(false,"aesKey is null"));
		}
		syncToJson((LinkedHashMap)col.getObjWrapper("custdata_pinfo").getObj());
		syncToJson((LinkedHashMap)col.getObjWrapper("custdata_questions").getObj());
		try{
			String ejsonBase64Str = CryptoUtil.encryptToBase64(aesKey, dataJson.toString().getBytes("UTF-8"),true);
			ByteArrayInputStream jsonIS = new ByteArrayInputStream(ejsonBase64Str.getBytes("UTF-8"));
			FilingUtil.storeFile(agentId, "filing", 
					String.format(dataJsonFilingKey, p_rg),
					String.format(dataJsonFilingKey, p_rg),
					"",
					jsonIS);
			return(new ReturnMsg(true));
		}	
		catch(Exception ex){
			ex.printStackTrace();
			return(new ReturnMsg(false,ex));
		}
	}
	private ReturnMsg loadDataJson(int p_rg){
		dataJson = null;
		if (p_rg <= 0) {
			UniLog.logm(this,"new record");
			dataJson = new JSONObject();
			return(new ReturnMsg(true));
		}
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] jsonByte = null;
		try{
			FilingUtilObject fuo = FilingUtil.getFile(agentId, "filing", String.format(dataJsonFilingKey, p_rg), os);
			if (fuo != null){
				byte[] ejsonByte = os.toByteArray();
				jsonByte = CryptoUtil.decryptFromBase64(aesKey, new String(ejsonByte));
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			return(new ReturnMsg(ex));
		}
		try{
			if (jsonByte == null){
				dataJson = new JSONObject();
			}
			else{
				dataJson = new JSONObject(new String(jsonByte));
			}
			return(new ReturnMsg(true));
		}
		catch(Exception ex){
			ex.printStackTrace();
			return(new ReturnMsg(ex));
		}
	}
	private ReturnMsg deleteDataJson(){ //TODO
		//FilingUtil.deleteFile(agentId, String p_tabName, String p_key) throws Exception{}
		
		return(new ReturnMsg(true));
	}
	
	/***
	 * called by ComposerBase
	 * @param p_agentId
	 */
	public void setAgentId(String p_agentId){
		agentId = p_agentId;
	}
	
	public void setAESKey(byte[] p_aesKey){
		aesKey = p_aesKey;
	}
	
	

}

