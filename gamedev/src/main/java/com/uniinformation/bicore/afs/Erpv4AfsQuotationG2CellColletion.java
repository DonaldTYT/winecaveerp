package com.uniinformation.bicore.afs;

import java.util.Date;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.erpv4.QuotationG2CellCollection;
import com.uniinformation.utils.UniLog;

public class Erpv4AfsQuotationG2CellColletion extends QuotationG2CellCollection {
	private enum FuncName { FUNC_isExpiredStatus, NOT_DEFINED }

	public Erpv4AfsQuotationG2CellColletion(BiCellCollection p_col, BiResultAfsQuotationG2 p_br) {
		super(p_col, p_br);
		UniLog.log1("called");
	}

	@Override
	public Object evalFunction(String p_fname, Vector p_args) throws Exception {
		UniLog.log1("p_fname:%s", p_fname);
		FuncName funcName = FuncName.NOT_DEFINED;
		try {
			funcName = FuncName.valueOf("FUNC_"+p_fname);
		}
		catch(Exception ex) {
			//remark: if enum not exist, will got exception here.
		}
		int sid = getSid();

		switch (funcName){
			case FUNC_isExpiredStatus:
				if (sid == 0)
					return false;
				Date quodate = (Date)p_args.get(0);
				Date invdate = (Date)p_args.get(1);
				int validdays = ((Double)p_args.get(2)).intValue();
				switch (((BiResultAfsQuotationG2)br).getQuomode()) {
				case QUOTATION:
					return DateUtil.nextday(quodate, validdays).compareTo(DateUtil.today()) < 0;
				case ORDER:
					return DateUtil.nextday(invdate, validdays).compareTo(DateUtil.today()) < 0;
				default:
					return false; 
				}
			default:
				break;
		}
		return super.evalFunction(p_fname, p_args);
	}
}
