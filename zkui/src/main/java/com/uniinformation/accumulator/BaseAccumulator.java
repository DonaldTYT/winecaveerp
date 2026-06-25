package com.uniinformation.accumulator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.uniinformation.utils.UniLog;
import org.apache.commons.lang3.tuple.Pair;
/*
 This implementation is a bit hard to understand, that ONLY the OPENING BALANCE is store in the date->amount object, the delta of each date
 is stored in the next date->amount object
 */
public abstract class BaseAccumulator {
//	public static Date accMaxDate = DateUtil.getDate("2037/12/31");
	public Comparable MaxValue = null;
	public abstract void saveToCache(Comparable p_date,double p_pAmount,double p_nAmount) throws CalculationErrorException;
	public abstract void deleteFromCache(Comparable p_date) throws CalculationErrorException;
//	public abstract void loadFromCache(Date p_date) CalculationErrorthrows Exception;
	public abstract DatedValue getCurrentBalance() throws CalculationErrorException;
	public abstract List <DatedValue> getDatedValues(Comparable p_datefrom, Comparable p_dateto) throws CalculationErrorException; // <= p_datefrom and  < p_dateto
	public Comparable purgedDate; // the date before or equal that the detail transaction is lost. values before this date is indeterminate, default to DateUtil.minDate
	protected List <DatedValue> dataSet;

	public class DatedValue {
		public Comparable valueDate;
		public double pAmount=0;
		public double nAmount=0;
		DatedValue(Comparable p_date) {
			valueDate = p_date;
		}
	}
	
	public DatedValue newDatedValue(Comparable p_date) {
		return(new DatedValue(p_date));
	}
	
	public BaseAccumulator(Comparable p_maxValue)
	{
		MaxValue = p_maxValue;
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
	
	int bSearch(Comparable p_date) throws CalculationErrorException
	{
		// assure that dateAt(0) <= p_date < dateAt(size()-1), other will give wrong answer
		if(dataSet.size() <= 0 || (p_date.compareTo(dataSet.get(0).valueDate) > 0) || !(dataSet.get(dataSet.size()-1).valueDate.compareTo(p_date) > 0)) {
//			loadFromCache(p_date);
			if(dataSet.size() == 0) {
				DatedValue dv =getCurrentBalance();
				if(dv == null) dv = newDatedValue(MaxValue);
				dataSet.add(dv);
			}
			double vP1,vN1;
			if(!(dataSet.get(0).valueDate.compareTo(p_date) < 0)) {
				List <DatedValue> dvList = getDatedValues(p_date,dataSet.get(0).valueDate);
				if(dvList == null){
					dataSet.add(0,newDatedValue(p_date));
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
		int idx = Collections.binarySearch(dataSet, newDatedValue(p_date), new Comparator<DatedValue>() {
				public int compare(DatedValue v0,DatedValue v1){
					if(v0.valueDate.compareTo(v1.valueDate) < 0) return(-1);
					if(v1.valueDate.compareTo(v0.valueDate) < 0) return(1);
					return(0);
				}
			}
		);
		return(idx);
	}
	
	protected DatedValue cloneDatedValue(Comparable p_date,DatedValue p_dv) throws CalculationErrorException {
		DatedValue dv = newDatedValue(p_date);
		dv.pAmount = p_dv.pAmount;
		dv.nAmount = p_dv.nAmount;
		return(dv);
	}
	
	protected void changeOneDatedValue(DatedValue p_dv,double p_deltaP,double p_deltaN) throws CalculationErrorException {
			p_dv.pAmount += p_deltaP;
			p_dv.nAmount += p_deltaN;
	}
	
	/*
	  0 = begin balance
	  1 = end balance
	  
	  note add begin balance not fully tested
	  2020-03-22 DT
	 */
	public void updateBalance(Comparable p_date,double p_deltaP,double p_deltaN,int beginOrEnd,boolean skipZeroDelta) throws CalculationErrorException
	{
		if(p_deltaP == 0.0 && p_deltaN == 0.0 && skipZeroDelta) return; // no need to add zero value 
		if(p_deltaP == 0.0 && p_deltaN == 0.0) {
			UniLog.log("add entry for zero delta");
		}
		int idx = bSearch(p_date);
		if(idx < 0 ){
			idx = -(idx+1);
			if(beginOrEnd == 1) {
				DatedValue dv = cloneDatedValue(p_date,dataSet.get(idx));
				/*
				DatedValue dv = newDatedValue(p_date);
				dv.pAmount = dataSet.get(idx).pAmount;
				dv.nAmount = dataSet.get(idx).nAmount;
				*/
				dataSet.add(idx, dv);
			}
		}
		for(int i=idx + beginOrEnd; i < dataSet.size();i++) {
//			dataSet.get(i).pAmount += p_deltaP;
//			dataSet.get(i).nAmount += p_deltaN;
			changeOneDatedValue(dataSet.get(i),p_deltaP,p_deltaN);
			saveToCache(dataSet.get(i).valueDate,dataSet.get(i).pAmount,dataSet.get(i).nAmount);
		}	
		
		/*
		 * code below not yet tested 
		 */
		
		/*
		 this is wrong, should compare to idx + 1 and remove the record with lower idx value, (not yet verified)
		if(idx > 0 && dataSet.get(idx-1).pAmount == dataSet.get(idx).pAmount && dataSet.get(idx-1).nAmount == dataSet.get(idx).nAmount) {
					dataSet.remove(idx);
					deleteFromCache(p_date);
		} 
		*/
//		if(skipZeroDelta && idx > 0 && dataSet.get(idx+1).pAmount == dataSet.get(idx).pAmount && dataSet.get(idx+1).nAmount == dataSet.get(idx).nAmount) {
		if(dataSetCanDelete(idx)) {
					dataSet.remove(idx);
					deleteFromCache(p_date);
		} 
	}
	
	protected boolean dataSetCanDelete(int idx) {
		if(dataSet.get(idx+1).pAmount == dataSet.get(idx).pAmount && dataSet.get(idx+1).nAmount == dataSet.get(idx).nAmount) {
			return(true);
		} else {
			return(false);
		}
	}
	
	/* returned List is in decending order */
	public List<Pair<Comparable,Double>> getBalanceEndFifo(Comparable p_date) throws CalculationErrorException {
		return( getBalanceEndFifo(p_date,null) );
	}
	public List<Pair<Comparable,Double>> getBalanceEndFifo(Comparable p_date,Double p_qty) throws CalculationErrorException
	{
		ArrayList<Pair<Comparable,Double>> dateBalance = new ArrayList<Pair<Comparable,Double>>();
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
//			return(dataSet.get(0).pAmount + dataSet.get(0).nAmount);
//			return(null);
			throw new CalculationErrorException("Cannot get fifo balance of date before earliest costdate");
		}
		int idx = bSearch(p_date);
		if(idx < 0) {
			idx = -(idx+1); // the index to the record just after p_date
		} else {
			idx++; //the balance of next tr date is tbe closing of current date
		}
		double balance = 0.0;
		if(p_qty == null) balance = dataSet.get(idx).pAmount + dataSet.get(idx).nAmount;  else balance = p_qty;
		if(balance < 0 ) return(null); // current don't handle negative balance
		while (balance > Double.MIN_VALUE) {
//			if(idx < 0) return(null);
			if(idx < 0) break;
			double inAmt = dataSet.get(idx).pAmount ;
			Comparable d;
			if(idx <= 0) {
				d = purgedDate;
			} else {
				d = dataSet.get(idx-1).valueDate;
				inAmt -= dataSet.get(idx-1).pAmount;
			}
			if(inAmt > balance) {
				inAmt = balance;
				balance = 0.0;
			} else {
				balance -= inAmt;
			}
			if(inAmt != 0) dateBalance.add(Pair.of(d, inAmt));
			idx--;
		}
		return(dateBalance);
	}
	public double getBalanceEnd(Comparable p_date) throws CalculationErrorException
	{
		// p_date must less then  MaxValue becauase there are no next record after dataSet at MaxValue, therefore cannot calculate the closing balance
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
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
	public double getBalanceBegin(Comparable p_date) throws CalculationErrorException
	{
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
			return(dataSet.get(0).pAmount + dataSet.get(0).nAmount);
		}
		int idx = bSearch(p_date);
		if(idx < 0) {
			idx = -(idx+1); // the index to the record just after p_date
		}
		return(dataSet.get(idx).pAmount + dataSet.get(idx).nAmount); 
	}
	public double getPosBalanceBegin(Comparable p_date) throws CalculationErrorException
	{
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
			return(dataSet.get(0).pAmount);
		}
		int idx = bSearch(p_date);
		if(idx < 0) {
			idx = -(idx+1); // the index to the record just after p_date
		}
		return(dataSet.get(idx).pAmount);
	}
	public double getPosBalanceEnd(Comparable p_date) throws CalculationErrorException
	{
		// p_date must less then  MaxValue becauase there are no next record after dataSet at MaxValue, therefore cannot calculate the closing balance
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
			return(dataSet.get(0).pAmount);
		}
		int idx = bSearch(p_date);
		if(idx < 0) {
			idx = -(idx+1); // the index to the record just after p_date
		} else {
			idx++; //the balance of next tr date is tbe closing of current date
		}
		return(dataSet.get(idx).pAmount); 
	}
	public double getNegBalanceBegin(Comparable p_date) throws CalculationErrorException
	{
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
			return(dataSet.get(0).nAmount);
		}
		int idx = bSearch(p_date);
		if(idx < 0) {
			idx = -(idx+1); // the index to the record just after p_date
		}
		return(dataSet.get(idx).nAmount);
	}
	public double getNegBalanceEnd(Comparable p_date) throws CalculationErrorException
	{
		// p_date must less then  MaxValue becauase there are no next record after dataSet at MaxValue, therefore cannot calculate the closing balance
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
			return(dataSet.get(0).nAmount);
		}
		int idx = bSearch(p_date);
		if(idx < 0) {
			idx = -(idx+1); // the index to the record just after p_date
		} else {
			idx++; //the balance of next tr date is tbe closing of current date
		}
		return(dataSet.get(idx).nAmount); 
	}
	public double getPosBalanceChange(Comparable p_date) throws CalculationErrorException
	{
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
			return(0.0);
		}
		int idx = bSearch(p_date);
		if(idx < 0) return(0.0);
		return(dataSet.get(idx+1).pAmount - dataSet.get(idx).pAmount); 
	}
	public double getNegBalanceChange(Comparable p_date) throws CalculationErrorException
	{
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
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
