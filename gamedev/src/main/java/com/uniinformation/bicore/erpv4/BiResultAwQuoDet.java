package com.uniinformation.bicore.erpv4;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultQuoDet;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAwQuoDet extends BiResultQuoDet {

	public BiResultAwQuoDet(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override
	ReturnMsg checkAndAddAllocation(CellCollection p_col) {
		Vector<BiResult> v = getSubLinks();
		if(v != null) {
			for(BiResult sr: v) {
				if(sr.getView().getTable().getName().equals("stmovd_si")) {
					try {
					Vector recs = sr.getRowCollectionList();
					if(p_col.getCell("ind_odrg").getInt() <= 0 || p_col.getCell("ind_irg").getInt() <= 0 || (!p_col.getCell("inv_quostatus").getString().equals("Confirmed"))) {
						for(int i=0;i<sr.getRowCount();i++) {
							sr.markDelete(sr.getTrStatObj(i),true);
						}
					} else {
//						if(!getCell("st_policy").getBoolean()) {
						if(!p_col.getCellBoolean("st_fserial") && !p_col.getCellBoolean("ind_instock")) {
//						if(false) {
						CellCollection scol=null;
						if(sr.getRowCount() <= 0) {
							scol = sr.newRowCollection();
							ReturnMsg rtn = sr.addSubRecord(scol, 0,"");
							Object tr = rtn.getData();
						} else {
							if(sr.isMarkedDelete(sr.getTrStatObj(0))) {
								sr.markDelete(sr.getTrStatObj(0),false);
							}
							for(int i = 1;i<sr.getRowCount();i++) {
								/*
								 * 2022/03/28, fixed  suspesious bug.
								 * not verified as that is only test to work on erpv4 system that stock will only have one org (200000001) 
								sr.markDelete(sr.getTrStatObj(0),true);
								 */
								sr.markDelete(sr.getTrStatObj(i),true);
							}
							scol = sr.getRowCollectionV(0);
						}
						scol.getCell("stmd_mrg").set(p_col.getCell("ind_rg").getInt());
						scol.getCell("stmd_qorg").set(p_col.getCell("ind_odrg").getInt());
						scol.getCell("stmd_irg").set(p_col.getCell("ind_irg").getInt());
						scol.getCell("stmd_qirg").set(p_col.getCell("ind_irg").getInt());
						scol.getCell("stmd_qty").set(p_col.getCell("ind_stqty").getDouble());
						scol.getCell("stmd_tdtype").set(Erpv4Config.getStmd_SI(getSessionHelper()));
						scol.getCell("stmd_tdindex").set(0);
//						scol.getCell("stmd_org").set(Erpv4Config.getCoWtAvOrg(sh, p_col.getCellString("inv_cocode")));
						scol.getCell("stmd_org").set(1900000000+p_col.getCell("ind_rg").getInt());
						if(p_col.testCell("inv_loc") != null && scol.testCell("stmd_loc") != null)  {
							scol.getCell("stmd_loc").set(p_col.getCellString("inv_loc"));
						}
						}
					}
					ReturnMsg rtnmsg = updateBucket(p_col,sr.getView().getName());
					return(rtnmsg);
						
					} catch (Exception ex) {
						UniLog.log(ex);
						return(new ReturnMsg(false,"Error 10101"));
					}
				}
			}
		}
		return(null);
	}
}
