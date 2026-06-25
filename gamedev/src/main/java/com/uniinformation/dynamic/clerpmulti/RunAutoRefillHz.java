package com.uniinformation.dynamic.clerpmulti;

import java.util.Date;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultMoStockTakeG2;
import com.uniinformation.clerpmulti.RefillHzInterface;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rest.clerpmulti.ClinicRefillHZentre;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class RunAutoRefillHz  implements RefillHzInterface {

	public void runRefill(SessionHelper p_sh,java.util.Date p_upTo) throws Exception {
		// TODO Auto-generated method stub
		UniLog.log("HAHA (4) run clerp cron " + "-> " + p_upTo );
		SelectUtil su = p_sh.getBiSchema().getSelectUtil();
		TableRec tr = su.getQueryResult("select * from stmov ", new Wherecl()
//							.andUniop("stm_status", "=", "Confirmed")
							.andUniop("stm_type", "=", "MO")
							.andUniop("stm_cocode", "=", "001")
							.andUniop("stm_module", "=", "cotfr")
							.andUniop("stm_toloc", "=", "HZ01")
							.andUniop("stm_ctrspec", "=", "Refill")
							.appendString(" order by stm_date desc")
				);
		Date stdate = p_upTo;
		if(tr.getRecordCount() > 0) {
			tr.setRecPointer(0);
			stdate = DateUtil.nextday(tr.getFieldDate("stm_date"));
		}
		int refillCnt = 0;
		while(!stdate.after(p_upTo)) {
			UniLog.log("Run One Refill at " + stdate);
			tr = su.getQueryResult("select count(*) numrec from stmov",new Wherecl()
							.andUniop("stm_status", "=", "Confirmed")
							.andUniop("stm_type", "=", "MO")
							.andUniop("stm_cocode", "=", "002")
							.andUniop("stm_module", "=", "cstmo")
							.andUniop("stm_fromloc", "=", "HZ01")
							.andUniop("stm_date", "=", stdate)
					);
			tr.setRecPointer(0);
			ReturnMsg rtn = refillOneDay(p_sh,stdate,tr.getFieldInt("numrec") > 0);
			if(rtn != null && !rtn.getStatus()) break;
			stdate = DateUtil.nextday(stdate);
			if(((Integer) rtn.getData()) > 0) {
				refillCnt++;
				if(refillCnt >= 10) break;
			}
		}
		su.close();
		/*
		BiResult stmovBr = p_sh.getBiSchema().getViewByName("erpv4.MoCompanyTfr").newBiResult(p_sh.getLoginId(), null, null, p_sh);
		stmovBr.addCustomCondition("stm_date ");
		*/
	}
	
	ReturnMsg refillOneDay(SessionHelper p_sh,Date p_date,boolean p_fixfifo) throws Exception {
		ReturnMsg rtn = ReturnMsg.defaultOk;
		UniLog.log("240321 refilOneDay:" + DateUtil.toDateString(p_date, "yyyy/mm/dd") + " Start");
		if(p_fixfifo) {
			UniLog.log("240321 refilOneDay:" + DateUtil.toDateString(p_date, "yyyy/mm/dd") + " fixfifo start");
			Erpv4Config.setDefaultCocode(p_sh,"002");
			Erpv4Config.setDefaultLcrg(p_sh,64);
			BiResultMoStockTakeG2 stocktakeBr = (BiResultMoStockTakeG2) p_sh.getBiSchema().getViewByName("erpv4.StockTakeG2").newBiResult(p_sh.getLoginId(), null, null, p_sh);
			rtn = stocktakeBr.generateBalance(p_date, true, true, "FixFifo "+DateUtil.toDateString(p_date, "yyyy/mm/dd"));
			if(rtn != null && !rtn.getStatus()) throw new Exception("Fix Fifo Error " + rtn.getMsg());
			UniLog.log("240321 refilOneDay:" + DateUtil.toDateString(p_date, "yyyy/mm/dd") + " fixfifo ended");
		}
    	Erpv4Config.setDefaultCocode(p_sh,"001");
    	Erpv4Config.setDefaultLcrg(p_sh,64);
		UniLog.log("240321 refilOneDay:" + DateUtil.toDateString(p_date, "yyyy/mm/dd") + " check negative stock start");
		BiResult asatBr  = p_sh.getBiSchema().getViewByName("erpv4.LocationAsAt").newBiResult(p_sh.getLoginId(), null, null, p_sh);
		asatBr.addCustomCondition("stmd_loc = 'HZ01' and stmd_sumqty < 0 and stmd_date <= '"+DateUtil.toDateString(p_date, "yyyy/mm/dd") + "'");
		asatBr.query();
		UniLog.log("240321 refilOneDay:" + DateUtil.toDateString(p_date, "yyyy/mm/dd") + " check negative stock start end rows = " + asatBr.getRowCount());
		if(asatBr.getRowCount() > 0) {
			UniLog.log("240321 refilOneDay:" + DateUtil.toDateString(p_date, "yyyy/mm/dd") + " create transfer start");
			ClinicRefillHZentre crhz = new ClinicRefillHZentre(asatBr);
			crhz.beginRefill();
			for(int i=0;i<asatBr.getRowCount();i++) {
				UniLog.log("240321 refilOneDay:" + DateUtil.toDateString(p_date, "yyyy/mm/dd") + " append one row start");
				asatBr.loadOneRecV(i);
				rtn = crhz.addOneItem();
				if(rtn == null || !rtn.getStatus()) return(rtn);
				UniLog.log("240321 refilOneDay:" + DateUtil.toDateString(p_date, "yyyy/mm/dd") + " append one row end");
			}
			UniLog.log("240321 refilOneDay:" + DateUtil.toDateString(p_date, "yyyy/mm/dd") + " end transfer start");
			rtn = crhz.endRefill();
			UniLog.log("240321 refilOneDay:" + DateUtil.toDateString(p_date, "yyyy/mm/dd") + " end transfer end" + rtn);
			if(rtn != null && !rtn.getStatus()) {
				UniLog.log("240321 refilOneDay:" + DateUtil.toDateString(p_date, "yyyy/mm/dd") + " failed " + rtn.getMsg());
				return(rtn);
			}
		}
		rtn = new ReturnMsg(true);
		rtn.setData(asatBr.getRowCount());
		UniLog.log("240321 refilOneDay:" + DateUtil.toDateString(p_date, "yyyy/mm/dd") + " process end");
		return(rtn);
		/*
		BiResult stmovBr = p_sh.getBiSchema().getViewByName("erpv4.MoCompanyTfr").newBiResult(p_sh.getLoginId(), null, null, p_sh);
		*/
	}

}
