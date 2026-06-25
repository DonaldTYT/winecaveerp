package com.uniinformation.bicore.vincero;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.bischema.BiResultExcelSheet;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultTradeJournal extends BiResultExcelSheet {
	public String getFilingKey() {
		return(String.format("VAWS_%s_%s_%s", "TradeJournal", getSessionHelper().getLoginId(), getSessionHelper().getVcode()));
	}
	String contentKey;
	JSONObject contentObject = null;
	public BiResultTradeJournal(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		virtualMaster = true;
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash) {
		if(contentKey == null) {
				contentKey = "";
		}
		/*
		if(contentObject == null) {
			try {
				contentObject = new JSONObject();
				contentObject.put("contentKey","");
				
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
		*/
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		p_where.andUniop("tradejournal.tradj_loginid", "=", sh.getVcode());
		p_where.andUniop("tradejournal.tradj_key", "=", contentKey);
		return(ht);
	}
	
	CellValueAction syncBiToJson;
	
	@Override
	protected void createColumnCells(final BiCellCollection col)
	{
		super.createColumnCells(col);	
		if(syncBiToJson == null) {
			syncBiToJson = new CellValueAction() {
				@Override
				public void cellAction_onfree() throws CellException {
				}
				@Override
				public void cellAction_onchange(Cell p_value) throws CellException {
				// TODO Auto-generated method stub
				UniLog.log("initbal changed");
				if(isActionEnabled()) {
					UniLog.log("Save values to json");
					try {
						contentObject = new JSONObject();
						contentObject.put("tradj_initbal", getCellDouble("tradj_initbal"));
						contentObject.put("tradj_priskpertrade", getCellDouble("tradj_priskpertrade"));
						FilingUtil.storeJson(getSessionHelper().getAgent(), null, getFilingKey(), null, null, contentObject);
					} catch (Exception ex) {
						UniLog.log(ex);
						throw new CellException(ex.toString());
					}
				} 
			}
			};
		}
		col.getCell("tradj_initbal").addAction(syncBiToJson);
		col.getCell("tradj_priskpertrade").addAction(syncBiToJson);
	}
	
	
	void readJson() {
//		contentObject = ZkUtil.getJsonFromFilingMulti(getSessionHelper(), null, getFilingKey(), contentKey);
		try {
		contentObject = FilingUtil.getJson(getSessionHelper().getAgent(), null , getFilingKey());
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		if(contentObject != null) {
			boolean ae = isActionEnabled();
			setActionEnabled(false);
			Iterator<String> iterator = contentObject.keys();
			while (iterator.hasNext()) {
			    String item = iterator.next();
			    try {
			    	if(getCurrentCollection().testCell(item) != null) {
			    		Object o = contentObject.get(item);
			    		getCell(item).set(o);
			    	}
			    } catch (Exception ex) {
			    	UniLog.log(ex);
			    }
			}
			setActionEnabled(ae);
		}
	}
	@Override 
	public ReturnMsg query(boolean p_rollback, boolean p_sortFlag) {
		ReturnMsg rtn = super.query(p_rollback, p_sortFlag);
		readJson();
		return(rtn);
	}

	@Override 
	public void clearCurrentRec() {
		super.clearCurrentRec();
		readJson();
	}
}
