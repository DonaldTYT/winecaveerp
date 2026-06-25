package com.uniinformation.bicore.propmgmtpro;

import java.util.Vector;

import com.google.common.collect.Lists;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultMetting extends BiResultPropmgmtPro {

	public BiResultMetting(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
	}

	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
		ReturnMsg rtn = super.biBeforeAddUpdateCurrent(col, isUpdate);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		
		try {
			if (!isUpdate) {
				ZkUtil.importAction.accept(sh, (su1) -> {
					TableRec tr = su.getQueryResult("select key_a from property join location on lc_desc = col_b where lc_rg = ?", new Wherecl().appendArgument(getCellInt("col_a")));
					for (int i = 0; i < tr.getRecordCount(); i++) {
						tr.setRecPointer(i);
						ZkUtil.executeInsertIntoSql(su1, "mettingsignin", Lists.newArrayList("col_a", "col_b", "col_c"), 
								new Wherecl().appendArgument(getCellInt("col_a"))
											.appendArgument(getCellDate("col_b"))
											.appendArgument(tr.getFieldString("key_a")));
					}
				});
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return new ReturnMsg(ex);
		}

		return(rtn);
	}
}
