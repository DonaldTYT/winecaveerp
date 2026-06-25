package com.uniinformation.accumulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;

/*
 This implementation is a bit hard to understand, that ONLY the OPENING BALANCE is store in the date->amount object, the delta of each date
 is stored in the next date->amount object
 */
public abstract class DateAccumulator {
	public static Date accMaxDate = DateUtil.getDate("2037/12/31");
	public abstract void saveToCache(Date p_date,double p_pAmount,double p_nAmount) throws Exception;
	public abstract void deleteFromCache(Date p_date) throws Exception;
//	public abstract void loadFromCache(Date p_date) throws Exception;
	public abstract DatedValue getCurrentBalance() throws Exception;
	public abstract List <DatedValue> getDatedValues(Date p_datefrom, Date p_dateto) throws Exception; // <= p_datefrom and  < p_dateto
	Date purgedDate; // the date before or equal that the detail transaction is lost. values before this date is indeterminate, default to DateUtil.minDate
	List <DatedValue> dataSet;
	public class DatedValue {
		public Date valueDate;
		public double pAmount=0;
		public double nAmount=0;
		public DatedValue(Date p_date) {
			valueDate = p_date;
		}
	}
	public DateAccumulator()
	{
		reset();
	}
	/*
	public Date getPurgedDate() {
		return purgedDate;
	}
	public void setPurgedDate(Date purgedDate) {
		this.purgedDate = purgedDate;
	}
	*/
	
	int bSearch(Date p_date) throws Exception
	{
		// assure that dateAt(0) <= p_date < dateAt(size()-1), other will give wrong answer
		if(dataSet.size() <= 0 || p_date.before(dataSet.get(0).valueDate) || !dataSet.get(dataSet.size()-1).valueDate.after(p_date)) {
//			loadFromCache(p_date);
			if(dataSet.size() == 0) {
				DatedValue dv =getCurrentBalance();
				if(dv == null) dv = new DatedValue(accMaxDate);
				dataSet.add(dv);
			}
			double vP1,vN1;
			if(!dataSet.get(0).valueDate.before(p_date)) {
				List <DatedValue> dvList = getDatedValues(p_date,dataSet.get(0).valueDate);
				if(dvList == null){
					dataSet.add(0,new DatedValue(p_date));
				} else {
				vP1 = dataSet.get(0).pAmount;
				vN1 = dataSet.get(0).nAmount;
				for(int i=dvList.size()-1;i>=0;i--) {
					DatedValue dv = dvList.get(i);
//					UniLog.log("HAHA " + i + " " + dv.valueDate + " " + dv.pAmount + " " + dv.nAmount);
					vP1 -= dv.pAmount;
					vN1 -= dv.nAmount;
					dv.pAmount = vP1;
					dv.nAmount = vN1;
					dataSet.add(0,dv);
				}
				}
				purgedDate = p_date;
			}
		}
		int idx = Collections.binarySearch(dataSet, new DatedValue(p_date), new Comparator<DatedValue>() {
				public int compare(DatedValue v0,DatedValue v1){
					if(v0.valueDate.before(v1.valueDate)) return(-1);
					if(v1.valueDate.before(v0.valueDate)) return(1);
					return(0);
				}
			}
		);
		return(idx);
	}
	
	/*
	  0 = begin balance
	  1 = end balance
	 */
	public void changeOneValue(Date p_date,double p_deltaP,double p_deltaN,int beginOrEnd) throws Exception
	{
		if(p_deltaP == 0.0 && p_deltaN == 0.0) return; // no need to add zero value 
		int idx = bSearch(p_date);
		if(idx < 0 ){
			idx = -(idx+1);
			DatedValue dv = new DatedValue(p_date);
			if(idx > 0) {
				dv.pAmount = dataSet.get(idx-1).pAmount;
				dv.nAmount = dataSet.get(idx-1).nAmount;
			}
			dataSet.add(idx, dv);
		}
		for(int i=idx + beginOrEnd; i < dataSet.size();i++) {
			dataSet.get(i).pAmount += p_deltaP;
			dataSet.get(i).nAmount += p_deltaN;
			saveToCache(dataSet.get(i).valueDate,dataSet.get(i).pAmount,dataSet.get(i).nAmount);
		}	
		if(idx > 0 && dataSet.get(idx-1).pAmount == dataSet.get(idx).pAmount && dataSet.get(idx-1).nAmount == dataSet.get(idx).nAmount) {
					dataSet.remove(idx);
					deleteFromCache(p_date);
		} 
	}
	
	public double getBalanceEnd(Date p_date) throws Exception
	{
		if(dataSet.size() > 0 && p_date.before(dataSet.get(0).valueDate) && !p_date.before(purgedDate)) {
			return(dataSet.get(0).pAmount + dataSet.get(0).nAmount);
		}
		int idx = bSearch(p_date);
		if(idx < 0) {
			idx = -(idx+1); // the index to the record just after p_date
		} else {
			idx++; //the balance of next tr date is tbe closing of current date
		}
		return(dataSet.get(idx).pAmount + dataSet.get(idx).nAmount); 
	}
	public double getBalanceBegin(Date p_date) throws Exception
	{
		if(dataSet.size() > 0 && p_date.before(dataSet.get(0).valueDate) && !p_date.before(purgedDate)) {
			return(dataSet.get(0).pAmount + dataSet.get(0).nAmount);
		}
		int idx = bSearch(p_date);
		if(idx < 0) {
			idx = -(idx+1); // the index to the record just after p_date
		}
		return(dataSet.get(idx).pAmount + dataSet.get(idx).nAmount); 
	}
	public double getPAmount(Date p_date) throws Exception
	{
		if(dataSet.size() > 0 && p_date.before(dataSet.get(0).valueDate) && !p_date.before(purgedDate)) {
			return(0.0);
		}
		int idx = bSearch(p_date);
		if(idx < 0) return(0.0);
		return(dataSet.get(idx+1).pAmount - dataSet.get(idx).pAmount); 
	}
	public double getNAmount(Date p_date) throws Exception
	{
		if(dataSet.size() > 0 && p_date.before(dataSet.get(0).valueDate) && !p_date.before(purgedDate)) {
			return(0.0);
		}
		int idx = bSearch(p_date);
		if(idx < 0) return(0.0);
		return(dataSet.get(idx+1).nAmount - dataSet.get(idx).nAmount); 
	}
	/*
	void addDatedValueAt(Date p_date,int p_idx,double p_pAmount,double p_nAmount) throws Exception
	{
		DatedValue dv = new DatedValue(p_date);
		dv.pAmount = p_pAmount;
		dv.nAmount = p_nAmount;
		if(p_idx >= 0) dataSet.add(p_idx, dv); else dataSet.add(dv);
	}
	*/
	
	public void reset() {
		dataSet=new ArrayList<DatedValue>();
		purgedDate = null;
	}
	
	public void dump() {
		for(DatedValue dv : dataSet) {
			UniLog.log("DataValue " + dv.valueDate + " " + dv.pAmount + " " + dv.nAmount);
		}
	}
}
