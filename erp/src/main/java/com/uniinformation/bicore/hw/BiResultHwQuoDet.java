package com.uniinformation.bicore.hw;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.ColorPickerGetItemProperty;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;


public class BiResultHwQuoDet extends BiResultHwOrdDetBase {
	ColorPickerGetItemProperty cpi = null;
	public BiResultHwQuoDet(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr,p_sh);
		UniLog.log("erp.BiResultHwQuoDet used");
		cpi = new ColorPickerGetItemProperty();
	}
	
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection p_col) {
		int linkOdrg =  p_col.getCell("ind_linked").getInt();
		if(linkOdrg > 0) {
			try {
				getSelectUtil().executeUpdate("delete from invquodet where invqd_odrg = " + linkOdrg,null);
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,"Error unlink quodet/invdet",true));
			}
				
		}
		return(super.biBeforeDeleteCurrent(p_col));
	}

	@Override
	protected void afterLoadCollection(boolean p_isFetch,BiCellCollection p_cc){
		super.afterLoadCollection(p_isFetch, p_cc);
		if(p_cc.testCell("ind_linked") != null) {
			if(p_cc.testCell("ind_linked").getInt() > 0)  {
				BiCellCollection bc  = (BiCellCollection) p_cc;
				try {
					bc.lock();
				} catch (CellException cex) {
					UniLog.log(cex);
				}
					
			}
		}
	}
}
