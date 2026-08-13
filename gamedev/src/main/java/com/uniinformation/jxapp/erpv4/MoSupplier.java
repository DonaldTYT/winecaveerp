package com.uniinformation.jxapp.erpv4;

import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;

public class MoSupplier extends MO {

	@Override
	protected CellCollection addOneMoDetail(int p_irg,int p_org,double p_qty,String p_unit) throws Exception {
		CellCollection col = super.addOneMoDetail(p_irg,p_org,p_qty,p_unit);
		col.getCell("stmd_tdtype").set(Erpv4Config.getStmd_MI(getSessionHelper()));
		return(col);
	}
}
