package com.uniinformation.zkbi.erpv4;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.bischema.ExcelCellCollection;
import com.uniinformation.bicore.bischema.ExcelWorkSheetCache;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiComposerReport;

public class ZkBiComposerCoLocation extends ZkBiComposerBase{
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
		if(!ZkSessionHelper.getSessionHelper().hasAccessRight("#updateloc")) {
			if(hasAUDColumn == null) hasAUDColumn=false;
		}
		super.doAfterCompose(comp);
   	}
	@Override
	protected void doZkbiItemSelected(int p_idx,BiResult p_br) {
			UniLog.log("Switch to company");
			if(p_idx >= 0) {
				p_br.loadOneRecV(p_idx);
				try {
				if(Erpv4Config.isMultiCompany(p_br.getSessionHelper())) {
					String cocode = p_br.getCellString("co_cocode");
					Erpv4Config.setDefaultCocode(p_br.getSessionHelper(),cocode);
				}
    			if(Erpv4Config.isMultiStockLoc(getSessionHelper())) {
    				int lcrg = p_br.getCellInt("lc_rg");
    				Erpv4Config.setDefaultLcrg(p_br.getSessionHelper(),lcrg);
    			}
    			//240825 when bischema reload, it will clear excel brcache too
    			ExcelWorkSheetCache.clearBrCache();
    			Executions.getCurrent().sendRedirect("");
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			} 
			
	}
}
