package com.uniinformation.bicore.bischema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.JdbcRpcServer;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.WebCoreUtil;

public class BiResultBiView extends BiResult {

	public BiResultBiView(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void createColumnCells(BiCellCollection p_col) {
		super.createColumnCells(p_col);
	}

	ReturnMsg doInsertBiTable(CellCollection p_col) throws Exception {
		if(!StringUtil.strpart(p_col.getCellString("grpth_attribute"),7,1).equals("Y")) return(ReturnMsg.defaultOk);
		BiResultBiTable br = (BiResultBiTable) getView().getSchema().getViewByName("bischema.BiTable").newBiResult(sh.getLoginId(), null, null, sh);
		br.clearCurrentRec();
		{
			br.getCell("ddt_database").set(p_col.getCellString("dddb_database"));
			br.getCell("ddt_tabname").set(p_col.getCellString("grpth_table"));
			br.getCell("ddt_dbtname").set(p_col.getCellString("grpth_table"));
			br.getCell("ddt_ename").set(p_col.getCellString("grpth_table"));
			BiResult sr = br.getSubLink("bischema.BiField");
			for(BiCellCollection bc : getSubLink("bischema.BiColumn").getRowCollectionList()) {
				BiCellCollection col = sr.newRowCollection();								
				if(!bc.getCellString("grptc_subtable").equals(p_col.getCellString("grpth_table"))) continue;
				sr.addSubRecord(col, -1 ,"");
				col.getCell("ddf_fdname").set(bc.getCellString("grptc_fd"));
				col.getCell("ddf_type").set(bc.getCellString("grptc_fdtype"));
				col.getCell("ddf_len").set(bc.getCellInt("grptc_fdlen")); /* should replace to use prefix if prefix is not blank */
			}
			return(br.addCurrent());
		}
	}
	ReturnMsg doAlterBiTable(CellCollection p_col) throws Exception {
		/*
		if (true) {
			//TODO 240209 should skip alter table if definition unchanged. especially for formula update only!
			UniLog.log1("HAHA240209 skip alter bitable temporary!!!!");
			return ReturnMsg.defaultOk;
		}
		*/
		if(!StringUtil.strpart(p_col.getCellString("grpth_attribute"),8,1).equals("Y")) return(ReturnMsg.defaultOk);
		BiResultBiTable br = (BiResultBiTable) getView().getSchema().getViewByName("bischema.BiTable").newBiResult(sh.getLoginId(), null, null, sh);
		br.clear();
		br.addCustomCondition("ddt_database = '"+p_col.getCellString("dddb_database")+"' and ddt_tabname = '"+p_col.getCellString("grpth_table")+"'");
		br.query();
		if(br.getRowCount() == 1) {
			br.fetchOneRecV(0);
			HashMap<String,Integer>newColumnSet = new HashMap<String,Integer>();
			Vector<BiCellCollection> bcList = getSubLink("bischema.BiColumn").getRowCollectionList();
			for(int i=0;i<bcList.size();i++) {
				if(!bcList.get(i).getCellString("grptc_subtable").equals(p_col.getCellString("grpth_table"))) continue;
				newColumnSet.put(bcList.get(i).getCellString("grptc_fd"), i);
			}
			BiResult sr = br.getSubLink("bischema.BiField");
			Vector<BiCellCollection> colList = sr.getRowCollectionList();
			for(int i=0;i<colList.size();i++) {
				BiCellCollection col = colList.get(i);
				Integer idx = newColumnSet.get(col.getCellString("ddf_fdname"));
				if(idx != null) {
					BiCellCollection bc = bcList.get(idx);
					if(!StringUtils.isBlank(bc.getCellString("grptc_fdtype"))) {
						/* new Column Type is blank , use existing database column type */
						if(bc.getCellString("grptc_fdtype").equals("label")) {
							int cc;
							cc = 0;
						} else {
							col.getCell("ddf_type").set(bc.getCellString("grptc_fdtype"));
						}
					}
					col.getCell("ddf_len").set(bc.getCellInt("grptc_fdlen"));
					newColumnSet.remove(col.getCellString("ddf_fdname"));
				} else {
					Object o = sr.getTrStatObj(new Integer(i));
					sr.markDelete( o, true);
				}
			}	
			for(Integer idx : newColumnSet.values()) {
				BiCellCollection col = sr.newRowCollection();								
				sr.addSubRecord(col, -1 ,"");
				BiCellCollection bc = bcList.get(idx);
				col.getCell("ddf_fdname").set(bc.getCellString("grptc_fd"));
				col.getCell("ddf_type").set(bc.getCellString("grptc_fdtype"));
				col.getCell("ddf_len").set(bc.getCellInt("grptc_fdlen")); /* should replace to use prefix if prefix is not blank */
			}
			return(br.updateCurrent());
		} else {
			return(new ReturnMsg(false,"Select BiTable error"));
		}
	}
	
	ReturnMsg doInsertOrAlterDbTable(CellCollection col) {
		ReturnMsg rtn;
		try {
		if(StringUtils.isBlank(col.getCellString("ddt_tabname"))) {
			return(doInsertBiTable(col));
		} else {
			//return(doAlterBiTable(col));
			//update at 250304, skip update bitable
			return ReturnMsg.defaultOk;
		}
		} catch (Exception ex) {
			UniLog.log("ex");
			return(new ReturnMsg(false,ex.toString()));
		}
	}
	
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col)
	{
		ReturnMsg rtn = super.biBeforeAddCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		return(doInsertOrAlterDbTable(col));
	}
	
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeUpdateCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		return(doInsertOrAlterDbTable(col));
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash) {
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		if(!sh.isBiSchemaView()) {
			p_where.andUniop("grpth_database", "=", sh.getDbName());
		}
		return(ht);
	}
}
