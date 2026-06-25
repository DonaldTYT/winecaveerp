package com.uniinformation.bicore.bischema;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiField;
import com.uniinformation.bicore.BiJoin;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultExcelSheet extends BiResult {

	public BiResultExcelSheet(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new ExcelCellCollection(p_parent, this));
	}

	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		// * mark view as reequire recal 
	    ExcelWorkSheetCache.setDirtyAll(getView().getSchema(),getView().getName());
	    HashSet<String> aliasSet = getView().getSchema().getAliasSet(getView().getName());
	    if(aliasSet != null) {
	    	for(String vN : aliasSet) {
	    		ExcelWorkSheetCache.setDirtyAll(getView().getSchema(),vN);
	    	}
	    }
		Vector v = getSubLinks();
		if(v != null) {
			for(int i = 0;i < v.size();i++) {
				BiResult sr = (BiResult) v.get(i);
				ExcelWorkSheetCache.setDirtyAll(getView().getSchema(),sr.getView().getName());
			}
		}
		return( super.biAfterAddUpdateCurrent(col,isUpdate));
	}
	@Override
	protected ReturnMsg biAfterDeleteCurrent(CellCollection col) {
		// * mark view as reequire recal 
	    ExcelWorkSheetCache.setDirtyAll(getView().getSchema(),getView().getName());
	    // this method is only called by master view 
	    // therefore need to loop setDirtyAll for all detail links
	    HashSet<String> aliasSet = getView().getSchema().getAliasSet(getView().getName());
	    if(aliasSet != null) {
	    	for(String vN : aliasSet) {
	    		ExcelWorkSheetCache.setDirtyAll(getView().getSchema(),vN);
	    	}
	    }
		Vector v = getSubLinks();
		if(v != null) {
			for(int i = 0;i < v.size();i++) {
				BiResult sr = (BiResult) v.get(i);
				ExcelWorkSheetCache.setDirtyAll(getView().getSchema(),sr.getView().getName());
			}
		}
	    
		return( super.biAfterDeleteCurrent(col));
	}
	
	
	public void resetViewList() {
		super.resetViewList();
		if(getParent() != null) {
			BiJoin bj = getParent().getView().getTable().getJoin(getView().getTable());
			if(bj != null) {
				ArrayList<BiColumn> hList = new ArrayList<BiColumn>();
				for(BiField bf : bj.getToFields()) {
					int cc;
					cc = 0;
					for(BiColumn bc : getListColumns()) {
						if(bc.getField() == bf) {
							hList.add(bc);
							break;
						}
					}
				}
				for(BiColumn bc : hList) {
					hideViewColumn(bc);
				}
			}
		}
	}
}
