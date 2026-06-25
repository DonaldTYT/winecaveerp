package com.uniinformation.jxapp.afs;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.erpv4.BiResultQuoDet;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jxapp.AfsQuotation;
import com.uniinformation.utils.Wherecl;

public class AfsQuoParts extends AfsQuotation {
	@Override
	protected Wherecl createPullDownWherecl(CellCollection col) {
		Wherecl wcl;
		wcl = super.createPullDownWherecl(col);
		if(wcl == null) wcl = new Wherecl();
		wcl.genInList("and", "st_mtype","in","P");
		return(wcl);
	}
	@Override
	public void afterBind() {
		super.afterBind();
		detTypeList.clear();
		detTypeList.add(BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM ));
		detTypeList.add(BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION));
		detTypeList.add(BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_LINEBREAK));
	}
	@Override
	public void bindCellCollection(BiResult p_br,int p_mode)
	{
		super.bindCellCollection(p_br,p_mode);
		if("Y".equals(Erpv4Config.getString(getSessionHelper(),"QUORequireApproval"))) {
			if(!BiSchema.hasAccessRight(getSessionHelper(), "#cfmso")) {
				jxAdd("btConfirmOdr").setEnable(false);
			} 
		}
	}
}
