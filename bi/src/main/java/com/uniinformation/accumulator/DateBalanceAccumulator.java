package com.uniinformation.accumulator;

import java.util.List;

import com.kyoko.common.*;

public class DateBalanceAccumulator extends BaseAccumulator {
	public DateBalanceAccumulator() {
		super(DateUtil.maxDate);
//		super(/*DateUtil.maxDate */DateUtil.maxDate);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void saveToCache(Comparable p_date, double p_pAmount, double p_nAmount)
			throws CalculationErrorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteFromCache(Comparable p_date) throws CalculationErrorException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public DatedValue getCurrentBalance() throws CalculationErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<DatedValue> getDatedValues(Comparable p_datefrom, Comparable p_dateto)
			throws CalculationErrorException {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
