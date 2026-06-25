package com.uniinformation.bicore.vincero;

import java.util.HashSet;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.internal.StringUtil;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.bischema.BiResultExcelSheet;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkf.ZkForm;

//import javafx.scene.control.ListCell;

public class BiResultCompoundResult extends BiResultExcelSheet {
	
	int numRec = 100;
	public BiResultCompoundResult(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		virtualMaster = true;
	}
	
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		for(int i=0;i<numRec;i++) {
			try {
				addTrRecord(null,i);
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
		return(ReturnMsg.defaultOk);
	}
	
	@Override
	protected ReturnMsg afterLoadSerialMap3() {
		try {
			recal();
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		return(ReturnMsg.defaultOk);
	}
	
	com.uniinformation.utils.exprpar.Parser parser ;
	@Override
	protected void recalOneRec(HashSet<BiColumn> p_excludeList) throws Exception {
		Object oo = parser.evaluate();
		getCell("cprtn_result").set(oo);
		saveObjectToCacheHash(getCurrentCollection().getIdx(),"cprtn_result",oo);
		super.recalOneRec(p_excludeList);
	}

	@Override
	public int recal() throws Exception {
		ColumnCell cc = getCell("cprtn_result");
		parser = new com.uniinformation.utils.exprpar.Parser(ignoreCase,cc.getBiColumn().getFormula(true),getCurrentCollection(),getCurrentCollection());
		return(super.recal());
	}
	

//	public void runSimulator( ) throws Exception {
//		ColumnCell cc = getCell("cprtn_result");
//		com.uniinformation.utils.exprpar.Parser parser 
//			= new com.uniinformation.utils.exprpar.Parser(ignoreCase,cc.getBiColumn().getFormula(true),getCurrentCollection(),getCurrentCollection());
//		for(int i=0;i<getRowCount();i++) {
//			loadOneRecV(i);
//			Object oo = parser.evaluate();
//			getCell("cprtn_result").set(oo);
//			saveObjectToCacheHash(getCurrentCollection().getIdx(),cc.getCellLabel(),oo);
//		}
//	}
	
	Listcell newMyListcell(String p_column) {
    	BiColumn biColumn = (BiColumn) getColumnByLabel(p_column);
    	String content = getCell(biColumn.getLabel()).getColumnDisplayString();
    	String sclass = getCell(biColumn.getLabel()).getColumnDisplayClass();
    	int align = getCell(biColumn.getLabel()).getAlignment();
		Listcell lc = new Listcell();
		Label lb = new Label(content);
    	if(!StringUtil.isBlank(sclass)) {
    		lb.setSclass(sclass);
    	}
    	if(align != 0) {
    		if(align > 0) {
    			ZkUtil.appendStyle(lc, "text-align:left;");
    		} else {
    			ZkUtil.appendStyle(lc, "text-align:right;");
    		}
    		if(((align > 0 ? align : -align) & 2) != 0) {
    			ZkUtil.appendStyle(lc, "word-wrap:word-break");
    		}
    	}
		lc.setSclass("genListCell");
    	lc.appendChild(lb);
		return(lc);
	}
	
	public void reLoadDataToZkForm( ZkForm zkf) {
		Listbox lb = (Listbox) zkf.getComponent("list_compoundresult");
		lb.getItems().clear();
		invalidateLoadRecIdx();
		for(int i=0;i<getRowCount();i++) {
			loadOneRecV(i);
			Listitem li = new Listitem();
			li.appendChild(newMyListcell("cprtn_trade"));
			li.appendChild(newMyListcell("cprtn_result"));
			li.appendChild(newMyListcell("cprtn_risk"));
			li.appendChild(newMyListcell("cprtn_pl"));
			li.appendChild(newMyListcell("cprtn_balance"));
			lb.appendChild(li);
		}
	}
}
