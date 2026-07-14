package com.uniinformation.bicore.propertymgmt;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.webcore.SessionHelper;

public class BiResultLocation extends BiResult {
	private static Object updateLocker = new Object();

	public BiResultLocation(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
	}

	@Override
	protected ReturnMsg addCurrent(BiCellCollection cl) {
		synchronized (updateLocker) {
			try {
				setupNextRg();
			} catch (Exception ex) {
				return new ReturnMsg(ex);
			}
			return super.addCurrent(cl);
		}
	}

	@Override
	public ReturnMsg updateCurrent() {
		synchronized (updateLocker) {
			try {
				setupNextRg();
			} catch (Exception ex) {
				return new ReturnMsg(ex);
			}
			return super.updateCurrent();
		}
	}
	
	private void setupNextRg() throws Exception {
		if (getCellInt("lc_rg") == 0) {
			int newRg = 1;
			TableRec tr = su.getQueryResult("select max(lc_rg) lcrg from location");
			if (tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				int rg = tr.getFieldInt("lcrg");
				if (rg > 0)
					newRg = rg + 1;
			}
			getCell("lc_rg").set(newRg);
		}
	}
}
