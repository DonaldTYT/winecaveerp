package com.uniinformation.bicore;

import java.util.List;
import java.util.Map;
import java.util.Vector;

public interface BiReportInterface {
	static public enum ledgerColumns{
		st_icode, 
		st_iname, 
		lg_date, 
		stm_ref1, 
		stm_ref2, 
		stmd_tdtype, 
		stmd_openbal, 
		stmd_inqty, 
		stmd_outqty, 
		stmd_closebal,
		stmd_openamt, 
		stmd_inamount, 
		stmd_outamount, 
		stmd_closeamt, 
		stmd_avcost		
		/*
		CODE,
		NAME,
		DATE,
		VOUCHER,
		CORRESPONDENCE,
		NATURE,
		OPENING1,
		IN1,
		OUT1,
		CLOSE1,
		OPENING2,
		IN2,
		OUT2,
		CLOSE2,
		AVERATE
		*/
	};
	public ColumnCell getValue(ledgerColumns lgf);
	public BiColumn getCumulatorComparable();
	public String getFilterStr();
	public BiColumn getCumulatorKey();
	/*
	public List<String>getAggregateExpressionList(); // return list of expression , not mandatory
	public List<String>getPivotColumnList(); // return list of pivot columns columnLabel, mandatory
//	public void setGroupBy(int p_idx);
	*/
}