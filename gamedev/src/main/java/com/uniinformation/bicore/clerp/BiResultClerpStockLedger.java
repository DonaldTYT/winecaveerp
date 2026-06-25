package com.uniinformation.bicore.clerp;

import java.util.Vector;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultStockLedger;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultClerpStockLedger extends BiResultStockLedger {

	boolean ledgerUsePurchaseQty=false;
	public BiResultClerpStockLedger(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		if("Y".equals(Erpv4Config.getString(p_sh,"ledgerUsePurchaseQty"))) {
			ledgerUsePurchaseQty = true;
		}
		// TODO Auto-generated constructor stub
	}

	@Override
	public ColumnCell getValue(ledgerColumns lgf) {
		// TODO Auto-generated method stub
		switch(lgf) {
		case st_icode: return(getCell("st_icode"));
		case st_iname: return(getCell("st_iname"));
		case lg_date: return(getCell("stmd_date"));
		case stm_ref1: return(getCell("stm_ref1"));
		case stm_ref2: return(getCell("cldoc_name"));
		case stmd_tdtype: return(getCell("stmd_tdtype"));
		case stmd_openbal: return(getCell("stmd_openbal"));
		case stmd_inqty: return(ledgerUsePurchaseQty ? getCell("stmd_miqty"): getCell("stmd_inqty"));
		case stmd_outqty: return(ledgerUsePurchaseQty ? getCell("stmd_nonmiqty"): getCell("stmd_outqty"));
		case stmd_closebal: return(getCell("stmd_closebal"));
		case stmd_openamt: return(getCell("stmd_openamt"));
		case stmd_inamount: return(ledgerUsePurchaseQty ? getCell("stmd_miamount"): getCell("stmd_inamount"));
		case stmd_outamount: return(ledgerUsePurchaseQty ? getCell("stmd_nonmiamount"): getCell("stmd_outamount"));
		case stmd_closeamt: return(getCell("stmd_closeamt"));
		case stmd_avcost: return(getCell("stmd_avcost"));
		}
		return null;
	}

}
