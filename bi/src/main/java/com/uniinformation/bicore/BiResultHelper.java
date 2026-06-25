package com.uniinformation.bicore;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.commons.lang3.tuple.Pair;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;

public class BiResultHelper {
	/***
	 * reuse biresult and select data
	 * @param p_biResult
	 * @param p_customConditionStr
	 * @param p_recLimit
	 * @param p_orderByList
	 * @return
	 */
	public static BiResult create(BiResult p_biResult, String p_customConditionStr, int p_recLimit, List <Pair<String,Boolean>> p_orderByList){
		return(create(null, null, p_biResult, p_customConditionStr, null, p_recLimit, p_orderByList));
	}
	/***
	 * create a new biresult and select data
	 * @param p_sh
	 * @param p_viewName
	 * @param p_customConditionStr
	 * @param p_recLimit
	 * @param p_orderByList
	 * @return
	 */
	public static BiResult create(SessionHelper p_sh, String p_viewName, String p_customConditionStr, int p_recLimit, List <Pair<String,Boolean>> p_orderByList){
		return(create(p_sh, p_viewName, null, p_customConditionStr, null, p_recLimit, p_orderByList));
	}
	
	
	public static BiResult create(SessionHelper p_sh, String p_viewName, BiResult p_biResult, String p_customConditionStr, Wherecl p_wherecl, int p_recLimit, List <Pair<String,Boolean>> p_orderByList){
		return create(p_sh, p_viewName, p_biResult, p_customConditionStr, p_wherecl, p_recLimit, p_orderByList, true);
	}
	
	/***
	 * sample call for select data
	 * @param p_sh - session helper
	 * @param p_viewName - view name
	 * @param p_biResult - biresult
	 * @param p_customConditionStr - append to default where clause,
	 *                     remark: use cellName instead of field name
	 * @param p_wherecl - low level wherecl. support complicated structure. use field name instead of cellName                    
	 * @param p_recLimit - record limit, <0 no limit
	 * @param p_orderByList - pair of <cellName,orderFlag>
	 * @param p_allowLookupItemList - generate lookup itemlist to allow user to pick. set to false for non-ui biresult to speed up the query
	 * @return biResult
	 */
	public static BiResult create(SessionHelper p_sh, String p_viewName, BiResult p_biResult, String p_customConditionStr, Wherecl p_wherecl, int p_recLimit, List <Pair<String,Boolean>> p_orderByList, boolean p_allowLookupItemList){
		try{
			//1. obtain biresult base
			BiResult biResult = null;
			if (p_biResult != null){
				if (p_viewName == null || StringUtils.equals(p_viewName, p_biResult.getView().getName())){
					UniLog.logm(null, "reuse biresult view:%s", p_viewName, p_biResult.getView().getName());
					biResult = p_biResult;  //reuse same biresult
					p_biResult.clearCondition();
					p_biResult.clearOrderBy();
				}
				else{
					UniLog.logm(null, "cannot reuse biresult due to different view %s %s", p_viewName, p_biResult.getView().getName());
				}
			}
			if (biResult == null){
				BiSchema schema = BiSchema.loadSchema(p_sh);
				BiView view = schema.getViewByName(p_viewName);	
				if (view == null){
					UniLog.logm(null,"view not found");
					return null;
				}
				//biResult = view.newBiResult(p_sh.getLoginId(), null,null,p_sh);
				biResult = view.newBiResult(null,p_sh.getLoginId(), null,null,p_sh,p_allowLookupItemList);
				
			}
			if (biResult == null){
				UniLog.logm(null,"Cannot construct new biresult");
				return(null);
			}
			
			//2. set condition
			biResult.setRecLimit(p_recLimit);
			for (Pair<String,Boolean> orderByItem : ListUtil.safe(p_orderByList)){
				biResult.addOrderByColumnList(orderByItem.getLeft(), orderByItem.getRight());
			}
			
			//andrew190221: best way to append where clause
			ReturnMsg rtnMsg = biResult.addCustomCondition(p_customConditionStr);
			if (!rtnMsg.getStatus()){
				UniLog.logm(null,"add condition failed: %s", rtnMsg);
				return(null);
			}
			
			//append low level wherecl
			if (p_wherecl != null){
				biResult.appendWherecl(p_wherecl);
			}
			
			//3. execute query
			biResult.query(true,true);
			return(biResult);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return(null);
	}
	
	/***
	 * sample call for list all record, for debug purpose only
	 * @param p_agentId
	 * @param p_viewId
	 * @param p_customConditionStr
	 */
	public static void showRec(String p_agentId, String p_viewId, String p_customConditionStr, int p_recLimit, boolean p_fromCache, List <Pair<String,Boolean>> p_orderByList, String p_separator){
		StopWatch stopWatch = new StopWatch();
		SessionHelper sh = ZkSessionHelper.getSessionHelperDummy(p_agentId,"dummy",null);
		String separator = p_separator == null ? "," : p_separator;
		try {
			BiResult biResult = create(sh, p_viewId, p_customConditionStr, p_recLimit, p_orderByList);
			int rowIdx = 0;
			stopWatch.start();
			
			String headerStr = StringUtils.join(biResult.getColumns(),separator);
			UniLog.logm(null,"header:%s",headerStr);
			while (biResult.next(p_fromCache)){
				//int colIdx = 0;
				ArrayList<String> vals = new ArrayList<String>();
				for (BiColumn biColumn : biResult.getColumns()) {
					vals.add(biResult.getCell(biColumn.getLabel()).getString());
					//UniLog.logm(null,"rowIdx:%d colIdx:%d label:%s value:%s", rowIdx, colIdx, biColumn.getLabel(), biResult.getCell(biColumn.getLabel()).getString());
					//colIdx++;
				}
				UniLog.logm(null,"%d:%s",rowIdx,StringUtils.join(vals,separator));
				rowIdx++;
			}
			stopWatch.stop();
			UniLog.logm(null, "elapsed time: %d", stopWatch.getTime());
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
	}
	
	/* Method to set a cell value of a row in a BiResult (Detail BiResult Only) in sequence starting from 1 */
	public static void sequenceArray(BiResult p_br,String p_cell) throws CellException {
		if( p_br.getParent() == null) {
			// is root BiResult , cannot set sequence 
			throw new CellException("BiResult is not detail array");
		}
		Vector<BiCellCollection> cList = p_br.getRowCollectionList();
		int seq = 0;
		for(BiCellCollection cl : cList) {
			seq++;
			cl.getCell(p_cell).set(seq);
		}
	}
	
	public static void close(BiResult p_br) {
		try {
			if (p_br == null) {
				UniLog.log1("biresult is null");
				return;
			}
			p_br.close();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	/*
	public static ReturnMsg testUpdate(BiResult p_biResult, String p_wherecl, List<Pair<String,Object>> p_fields){
		try{
			p_biResult.beginWork();
			p_biResult.commitWork();
			p_biResult.fetch(false, p_idx);
			for (Pair<String,Object> field : p_fields){
				p_biResult.getCell(field.getLeft()).set(field.getRight());
			}
			UniLog.log("update bodychkfiling " + biResult.getCell("bckf_seq").getInt() + "," + biResult.getCell("bckf_time").getInt());
			biResult.getCell("bckf_key").set(key);
			biResult.getCell("bcdt_name").set(docType);
			if (StringUtils.isBlank(biResult.getCell("bckf_desc").toString()))
				biResult.getCell("bckf_desc").set(fileName);
			biResult.getCell("bckf_time").set((int)(System.currentTimeMillis() / 1000));
			rtnMsg = biResult.updateCurrent();
		}
		catch(Exception ex){
			return(new ReturnMsg(ex));
		}
	}
	*/
	/*
	//TODO implement add/update/delete helper function
	//delete
	public boolean markDelete(Object o,boolean p_sw)
	updateCurrent()
	
	//add main record
	ReturnMsg addCurrent(CellCollection cl)
	
	//add sub record
	public Object addSubRecord(CellCollection cl)
	updateCurrent
	*/
	
	
	

}
