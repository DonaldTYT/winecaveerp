package com.uniinformation.bicore.afs;

import java.util.HashSet;
import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultQuotationG2;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsQuotationG2 extends BiResultQuotationG2 {

	public BiResultAfsQuotationG2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}
	public void setInvType(String p_invtype) {
		quotationType = p_invtype;
		resetViewList();
		if(quotationType.equals("AQS")) {
			hideViewColumn(getView().getColumnByLabel("inv_delidate"));
		}
	}
	
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
		if(quotationType != "QUO") p_where.andUniop("inv_type", "=", quotationType);
		return(ht);
	}	
	
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		UniLog.log("createColumnCollection");
		return new Erpv4AfsQuotationG2CellColletion(p_parent, this);
	}
}
