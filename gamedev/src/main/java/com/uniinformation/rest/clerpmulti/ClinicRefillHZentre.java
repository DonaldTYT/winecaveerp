package com.uniinformation.rest.clerpmulti;

import java.util.Hashtable;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultLocationAsAt;
import com.uniinformation.erpv4.CostCalculation;

public class ClinicRefillHZentre {
    	BiResult result;
		BiResult stmovBr = null;
		String toLoc = null;
		Hashtable<String,Double> refillQtyHash;
    	public ClinicRefillHZentre (BiResult p_result) {
    		result = p_result;
			stmovBr = p_result.getSessionHelper().getBiSchema().getViewByName("erpv4.MoCompanyTfr").newBiResult(p_result.getSessionHelper().getLoginId(), null, null, p_result.getSessionHelper());
    	}
    	public ReturnMsg beginRefill() {
			stmovBr.clearCurrentRec();
			toLoc = null;
			refillQtyHash = new Hashtable<String,Double>();
			return(ReturnMsg.defaultOk);
    	}
    	public ReturnMsg addOneItem() throws Exception {
							String loc = result.getCellString("stmd_loc");
							if(toLoc != null) {
								if(!toLoc.equals(loc)) {
									return(new ReturnMsg(false,"Cannot refill multiple location"));
								}
							} else {
								toLoc = loc;
								stmovBr.getCell("stm_date").set(((BiResultLocationAsAt) result).getAsAtDate());
								stmovBr.getCell("stm_fromloc").set("HQ01");
								stmovBr.getCell("stm_toloc").set(toLoc);
								stmovBr.getCell("stm_status").set("Confirmed");
								stmovBr.getCell("stm_ctrspec").set("Refill");
//								stmovBr.getCell("stm_cur").set( Erpv4Config.getBaseCcy(getSessionHelper(),Erpv4Config.getDefaultCoCode(getSessionHelper())));
							}

							int hqOrg = 2000000001;
							double fillQty = -result.getCellDouble("stmd_sumqty");
//							double avalQty = CostCalculation.getBalance(result.getSessionHelper(), result.getCellInt("stmd_irg"), hqOrg, stmovBr.getCellDate("stm_date"));
							double avalQty = CostCalculation.getLocBalance(result.getSessionHelper(), result.getCellInt("stmd_irg"), hqOrg, "HQ01",stmovBr.getCellDate("stm_date"));
//							if(avalQty > avalQty2) avalQty = avalQty2;
							Double usedQty = refillQtyHash.get(""+result.getCellInt("stmd_irg")+"_"+hqOrg);
							if(usedQty != null) {
								avalQty -= usedQty;
							}
							if(fillQty > avalQty) {
								fillQty = avalQty;
							}
							if(usedQty != null) usedQty += fillQty; else usedQty = fillQty;
							refillQtyHash.put(""+result.getCellInt("stmd_irg")+"_"+hqOrg,usedQty);
							
							BiResult sr = stmovBr.getSubLink("erpv4.MoCompanyTfrDet");
							BiCellCollection col = sr.newRowCollection();								
							sr.addSubRecord(col, -1 ,"");
							col.getCell("stmd_irg").set(result.getCellInt("stmd_irg"));
							col.getCell("stmd_org").set(hqOrg);
							col.getCell("stmd_entryqty").set(fillQty);
//							col.getCell("stmd_entryqty").set(-result.getCellDouble("stmd_sumqty"));
							col.getCell("stmd_eratio").set(1.0);
//							col.getCell("stmd_org").set(result.getCellInt("stmd_org"));
							
							col.getCell("stmd_ref4").set(result.getCellString("stmd_ref4"));
							if(col.getCellString("stmd_ref4").equals("")) {
								col.getCell("stmd_lotno").sync("");
								col.getCell("stmd_exprdate").sync(DateUtil.zeroDate);
							}
							return(ReturnMsg.defaultOk);
    	}
    	public ReturnMsg endRefill() {
	        return(stmovBr.addCurrent());
    	}
    	

}
