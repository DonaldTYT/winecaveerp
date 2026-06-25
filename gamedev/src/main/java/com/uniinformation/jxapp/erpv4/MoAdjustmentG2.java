package com.uniinformation.jxapp.erpv4;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkf.ZkForm;

public class MoAdjustmentG2 extends MoPos {
	StockTakeUtil stu ;
	Set<String>locList = null;
	@Override
	public void afterBind() {
		super.afterBind();
		new JxFieldAction("btConvert") {
			@Override
			public void actionPerformed(JxField jxfield) {
				final ZkForm zkf1 = new ZkForm(null,"zkf/erpv4/adjConvertStock.zul");
				final CellCollection col = new CellCollection();
				try {
	        	zkf1.doModal(col,new EventListener() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						// TODO Auto-generated method stub
						if(arg0.getTarget().getId().equals("btOK")) {
//							StockTakeUtil stkutil = new StockTakeUtil(Erpv4Config.getString(getSessionHelper(), null));
							String fromIcode = col.getCellString("fdConvertFrom");
							String toIcode = col.getCellString("fdConvertTo");
							int fromIrg = 0;
							int toIrg = 0;
							if(StringUtils.isBlank(fromIcode)) {	
								throw new Exception("From Icode Is Blank");
							}
							if(StringUtils.isBlank(toIcode)) {	
								throw new Exception("To Icode Is Blank");
							}
							SelectUtil su = getBr().getSelectUtil();
							TableRec tr = su.getQueryResult("select st_irg from stock where st_icode = '"+fromIcode+"'");
							if(tr.getRecordCount() != 1) {
								throw new Exception("From Icode Not Exist");
							}
							tr.setRecPointer(0);
							fromIrg = tr.getFieldInt("st_irg");
							tr = su.getQueryResult("select st_irg from stock where st_icode = '"+toIcode+"'");
							if(tr.getRecordCount() != 1) {
								throw new Exception("To Icode Not Exist");
							}
							tr.setRecPointer(0);
							toIrg = tr.getFieldInt("st_irg");
							stu.getBalance(su, fromIrg, getBr().getCell("stm_date").getDate(), 0,locList);
							stu.convertStock(getBr(), fromIrg,toIrg,MoAdjustmentG2.this);
						}
						if(arg0.getTarget() instanceof Button) {
							zkf1.exitModal();
						}
					}
	        		
	        	}
	        	);
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
		};
	}
	@Override 
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		if(Erpv4Config.isMultiCompany(p_br.getSessionHelper())) {
			if(Erpv4Config.isMultiStockLoc(p_br.getSessionHelper())) {
				locList = Erpv4Config.getLocationListByCompany(p_br.getSessionHelper(), Erpv4Config.getDefaultCoCode(p_br.getSessionHelper()),Erpv4Config.LOCATION_TYPE.LOCATION_TYPE_BYLCRG_EXCLUDE_TRANSIT);
			} else {
				locList = Erpv4Config.getLocationListByCompany(p_br.getSessionHelper(), Erpv4Config.getDefaultCoCode(p_br.getSessionHelper()),Erpv4Config.LOCATION_TYPE.LOCATION_TYPE_ANY);
			}
					
		}
		stu = new StockTakeUtil(null);
	}
}
