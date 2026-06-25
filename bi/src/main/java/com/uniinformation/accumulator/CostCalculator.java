package com.uniinformation.accumulator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.uniinformation.accumulator.BaseAccumulator.DatedValue;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
public class CostCalculator extends BaseAccumulator {

	double currentPcost = 0.0;
	double currentPamount = 0.0;
	boolean debug = false;

	boolean sellShort = true;
	boolean newRealizedCosting = false;
	boolean useFIFO = false;
	
	
	class FiFoRemain {
		DatedValueWithCost dv;
		double remain;
		int idx;
		FiFoRemain( DatedValueWithCost p_dv, double p_remain,int p_idx) {
			idx = p_idx;
			dv = p_dv;
			remain = p_remain;
		}
	}
	public class DatedValueWithCost extends DatedValue {
		double nCostValid=Double.NaN;
		double pCost = 0.0;
		double nCost = 0.0;
		double realizedPL = 0.0;
//		double realizedAmt= 0.0;
		DatedValueWithCost(Comparable p_date) {
			super(p_date);
			if(newRealizedCosting) nCostValid = 0.0;
			// TODO Auto-generated constructor stub
		}
	}

	@Override
	public DatedValueWithCost newDatedValue(Comparable p_date) {
		return(new DatedValueWithCost(p_date));
	}	
	public CostCalculator(Comparable p_maxValue,boolean isNewCosting,boolean isFifo) {
		
		super(p_maxValue);
		newRealizedCosting = isNewCosting;
		useFIFO = isFifo;
		// TODO Auto-generated constructor stub
	}

	@Override
	public void saveToCache(Comparable p_date, double p_pAmount,
			double p_nAmount) throws CalculationErrorException {
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
	public List<DatedValue> getDatedValues(Comparable p_datefrom,
			Comparable p_dateto) throws CalculationErrorException {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	protected void changeOneDatedValue(DatedValue p_dv,double p_deltaP,double p_deltaN) throws CalculationErrorException {
		if(p_deltaP != currentPamount) throw new CalculationErrorException("Error p_deltaP not equals currentPamount");
		p_dv.pAmount += p_deltaP;
		p_dv.nAmount += p_deltaN;
		((DatedValueWithCost) p_dv).pCost += currentPcost;
		((DatedValueWithCost) p_dv).nCostValid = Double.NaN;
		((DatedValueWithCost) p_dv).realizedPL = 0;
//		((DatedValueWithCost) p_dv).realizedAmt = 0;
	}	
	@Override
	protected DatedValueWithCost cloneDatedValue(Comparable p_date,DatedValue p_dv) throws CalculationErrorException {
		DatedValueWithCost dv = newDatedValue(p_date);
		dv.pAmount = p_dv.pAmount;
		dv.nAmount = p_dv.nAmount;
		dv.pCost = ((DatedValueWithCost) p_dv).pCost;
		dv.nCost = ((DatedValueWithCost) p_dv).nCost;
		dv.realizedPL = ((DatedValueWithCost) p_dv).realizedPL;
//		dv.realizedAmt = ((DatedValueWithCost) p_dv).realizedAmt;
		dv.nCostValid = ((DatedValueWithCost) p_dv).nCostValid;
		return(dv);
	}	

	public boolean recalAverageBuyBeforeSellNew(Comparable p_date) throws CalculationErrorException {
		DatedValueWithCost dv0 = newDatedValue(null);
		for(int i=0;i<dataSet.size();i++) {
			DatedValueWithCost dv1 = (DatedValueWithCost) dataSet.get(i);
//			if(dv1.pAmount < 0 /* || dv1.nAmount > 0 */) {
//				UniLog.log("Cost Calculation error ! pAmount < 0 or nAmount > 0");
//				throw new CalculationErrorException("Cost Calculation error ! pAmount < 0 or nAmount > 0");
//			}
			// calculate the opening balance of dv1 == closing balanec of dv0
			
			if(Double.isNaN(dv1.nCostValid)) {
				double netAmount = 0;
				double netCost   = 0;
				
				/* Calculating Weighted Average Cost */
				double avCost=0;
				if(dv1.pAmount < 0) {
						// if cumulated pAmount < 0, cannot calculate weighted average , use previouse average cost
				} else {
					if(useFIFO) {
						double latestCost=0;
						if(dv1.pAmount - dv0.pAmount != 0) {
							latestCost = (dv1.pCost - dv0.pCost) / (dv1.pAmount - dv0.pAmount);
						} else {
							for(int j=i-1;j >= 0;j--) {
								DatedValueWithCost xdv = (DatedValueWithCost) dataSet.get(j);
								if(xdv.nCostValid != Double.NaN && xdv.nCostValid > 0) {
									latestCost = xdv.nCostValid;
									break;
								}
							}
						}
						dv1.nCostValid = latestCost;
						if(dv1.nAmount - dv0.nAmount > 0) {
							// positive sold amount, use latest cost
							netAmount = dv1.nAmount - dv0.nAmount;
							netCost = netAmount * latestCost;
							avCost = latestCost;
						}
						if(dv1.nAmount - dv0.nAmount < 0) {
							// netAmount = current buy + previouse remain
							double available = dv1.pAmount + dv0.nAmount;
							ArrayList<FiFoRemain> remainList = new ArrayList();
							for(int j=i;j>0;j--) {
								DatedValueWithCost xdv1 = (DatedValueWithCost) dataSet.get(j);
								DatedValueWithCost xdv0 = (DatedValueWithCost) dataSet.get(j-1);
								if(xdv1.pAmount - xdv0.pAmount != 0) {
									double remain = xdv1.pAmount - xdv0.pAmount > available ? available : xdv1.pAmount - xdv0.pAmount;
									remainList.add(new FiFoRemain(xdv1,remain,j));
									available -= remain;
									if(available <= 0) break;
								}
							}
							// search and sum fifo price
							available = dv0.nAmount - dv1.nAmount;
							for(int j=remainList.size()-1;j>=0;j--) {
								FiFoRemain fr = remainList.get(j);
								DatedValueWithCost xdv1 = (DatedValueWithCost) dataSet.get(fr.idx);
								DatedValueWithCost xdv0 = (DatedValueWithCost) dataSet.get(fr.idx-1);
								if(fr.remain > available) {
									netAmount += available;
									netCost += available * xdv1.nCostValid;
									break;
								} else {
									netAmount += fr.remain;
									netCost += fr.remain * xdv1.nCostValid;
									available -= fr.remain;
								}
							}
							if(netAmount < dv0.nAmount - dv1.nAmount) {
								netCost += (dv0.nAmount - dv1.nAmount - netAmount) * latestCost;
								netAmount += (dv0.nAmount - dv1.nAmount - netAmount);
							}
							avCost = netCost/netAmount;
						}
					} else {
						// if(opening balance > 0 , avcost = average of opening + day in, else avcost = average of dayin only
						if(dv1.pAmount - dv0.pAmount != 0) {
							netAmount += dv1.pAmount - dv0.pAmount;
							netCost   += dv1.pCost - dv0.pCost;
						}
						if(dv0.pAmount + dv0.nAmount > 0) {
							netAmount += dv0.pAmount + dv0.nAmount;
							netCost   += dv0.pCost + dv0.nCost;
						}
						if(netAmount == 0) {
							/* use last average cost */
							for(int j=i-1;j >= 0;j--) {
								DatedValueWithCost xdv = (DatedValueWithCost) dataSet.get(j);
								if(xdv.nCostValid != Double.NaN && xdv.nCostValid > 0) {
									avCost = xdv.nCostValid;
									break;
								}
							}
						}  else {
							/* calculate average cost */
							avCost = netCost/netAmount;
						}
						dv1.nCostValid = avCost;
					}
				}
				dv1.nCost = dv0.nCost + (dv1.nAmount - dv0.nAmount) * avCost; // add current out cost to cost balance
				
				double deltaP = dv1.pAmount - dv0.pAmount; // amount purchased on date dv.last
				
//				if(debug) UniLog.log("recalAverageBuyBeforeSell " + dv.valueDate + " pAmount " + dv.pAmount + " nAmount " + dv.nAmount + " netAmount " + netAmount + " deltaN " + deltaN  + " netCost " + netCost);
				
				if(deltaP != 0) {
					// * has no zero buy amount, av cost will change, check if has short sell amount
					double unRealizedAmount = dv0.pAmount + dv0.nAmount;
					if(unRealizedAmount < 0) {
						double realizedAmount = (deltaP + unRealizedAmount > 0.0) ? unRealizedAmount : -deltaP; // -ve
						double realizedCost = realizedAmount * (dv1.pCost -dv0.pCost) / (dv1.pAmount-dv0.pAmount); // -ve
						double unrealizedCost = realizedAmount * (dv0.pCost + dv0.nCost) / unRealizedAmount; // -ve
						double realizedPL = realizedCost  - unrealizedCost;
						dv1.nCost += realizedPL;
						dv1.realizedPL = realizedPL;
//						dv1.realizedAmt= realizedAmount;
					}
				}
			}
//			if(dv.valueDate.compareTo(p_date) >= 0) {
//				return(dv.valueDate);
//			}
			dv0 = dv1;
		}
//		return(lastdv.valueDate);
		return(true);
	}

	public boolean recalAverageBuyBeforeSell(Comparable p_date) throws CalculationErrorException {
		if(newRealizedCosting) {
			return(recalAverageBuyBeforeSellNew(p_date));
		}
		DatedValueWithCost lastdv = newDatedValue(null);
		for(int i=0;i<dataSet.size();i++) {
			
			DatedValueWithCost dv = (DatedValueWithCost) dataSet.get(i);
			if(Double.isNaN(dv.nCostValid)) {
				double netAmount = dv.pAmount + lastdv.nAmount; // this is the opening amount at date dv excluding the sold amount at date dv
				double netCost   = dv.pCost + lastdv.nCost;  // this is the opening cost of netAmount 
				double deltaN = dv.nAmount - lastdv.nAmount; // amount sold on date dv
				if(debug) UniLog.log("recalAverageBuyBeforeSell " + dv.valueDate + " pAmount " + dv.pAmount + " nAmount " + dv.nAmount + " netAmount " + netAmount + " deltaN " + deltaN  + " netCost " + netCost);
				double avCost;
				double deltaCost;
				if(deltaN > 0) {
//					throw new CalculationErrorException ("Cost Calculation Error : deltaN must not be positive");
					if(lastdv != null) {
						avCost = lastdv.nCostValid;
						dv.nCost = lastdv.nCost + avCost * deltaN;
					} else {
						return(false);
					}
				}
				if(deltaN < 0 && deltaN + netAmount < 0) {
					if(sellShort) {
					double deltaRemained = deltaN;
					double nAmount = lastdv.nAmount;
					double nCost = lastdv.nCost;
					for(int j=i;j<dataSet.size();j++) {
						DatedValueWithCost dv2 = (DatedValueWithCost) dataSet.get(j);
						netAmount = dv2.pAmount + nAmount;
						netCost   = dv2.pCost + nCost;
						if(netAmount > 0) {
						if(netAmount + deltaRemained >= 0) {
							avCost = netCost/netAmount;
							nCost += deltaRemained * avCost;
							deltaRemained = 0;
							break;
						} else {
							nCost -= netCost;
							nAmount -= netAmount;
							deltaRemained += netAmount;
						}
						}
					}
					if(deltaRemained < 0) return(false); else {
						dv.nCostValid = (nCost-lastdv.nCost) / deltaN;
						dv.nCost = nCost;
						lastdv = dv;
						continue;
					}
					} else return(false);
				}
				if(netAmount == 0) avCost = 0; else avCost = netCost/netAmount;
				deltaCost = deltaN * avCost;
				dv.nCost = lastdv.nCost + deltaCost;
				dv.nCostValid = avCost;
			}
//			if(dv.valueDate.compareTo(p_date) >= 0) {
//				return(dv.valueDate);
//			}
			lastdv = dv;
		}
//		return(lastdv.valueDate);
		return(true);
	}

	public void updateBalanceWithCost(Comparable p_date,double p_deltaP,double p_deltaN,int beginOrEnd,double p_cost) throws CalculationErrorException {
		currentPcost = p_cost;
		currentPamount = p_deltaP;
		super.updateBalance(p_date,p_deltaP,p_deltaN,beginOrEnd,p_cost == 0.0);
	}
	
	public double getCostBegin(Comparable p_date) throws CalculationErrorException 
	{
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
			return(0.0);
		}
		int idx = bSearch(p_date);
		if(idx < 0) {
			idx = -(idx+1); // the index to the record just after p_date
		}
		DatedValueWithCost dv0 = (DatedValueWithCost) dataSet.get(idx);
		return(dv0.pCost + dv0.nCost);
	}
//	public double getAverageCost(Comparable p_date) throws CalculationErrorException
//	{
//		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
//			return(0.0);
//		}
//		int idx = bSearch(p_date);
//		if(idx < 0) return(0.0);
//		DatedValueWithCost dv0 = (DatedValueWithCost) dataSet.get(idx);
//		DatedValueWithCost dv1 = (DatedValueWithCost) dataSet.get(idx+1);
//		double netAmount = dv1.pAmount + dv0.nAmount;
//		double netCost   = dv1.pCost + dv0.nCost;
//		if(netAmount < 0) throw new CalculationErrorException("Cost Calculation Error : net amount < 0");
//		double avCost;
//		if(netAmount == 0) avCost = 0; else avCost = netCost/netAmount;
//		return(avCost);
//	}	

	public double getCostOfGoodSold(Comparable p_date) throws CalculationErrorException 
	{
		if(! newRealizedCosting ) return(getAverageCostOld(p_date));
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
			return(dataSet.get(0).pAmount + dataSet.get(0).nAmount);
		}
		int idx = bSearch(p_date);
		if(idx < 0) {
			idx = -(idx+1); // the index to the record just after p_date
		} else {
			idx++; //the balance of next tr date is tbe closing of current date
		}
		if(idx <= 0) {
			UniLog.log("Warning !!!! try getCostOfGoodSold on first movementdate, cannot be calculated, return 0");
			return(0.0);
		}
		
		DatedValueWithCost dv1 = (DatedValueWithCost) dataSet.get(idx);
		DatedValueWithCost dv0 = (DatedValueWithCost) dataSet.get(idx-1);
		if(dv1.nAmount - dv0.nAmount == 0) return(0.0);
		return((dv0.nCost - dv1.nCost + dv1.realizedPL) / (dv0.nAmount - dv1.nAmount));
	}
	public double getAverageCost(Comparable p_date) throws CalculationErrorException 
	{
		if( !newRealizedCosting ) return(getAverageCostOld(p_date));
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
			return(dataSet.get(0).pAmount + dataSet.get(0).nAmount);
		}
		int idx = bSearch(p_date);
		if(idx < 0) {
			idx = -(idx+1); // the index to the record just after p_date
		} else {
			idx++; //the balance of next tr date is tbe closing of current date
		}
		DatedValueWithCost dv1 = (DatedValueWithCost) dataSet.get(idx);
		if(dv1.nAmount + dv1.pAmount == 0) return(dv1.nCostValid);
		return((dv1.pCost + dv1.nCost) / (dv1.pAmount + dv1.nAmount));
	}
	private double getAverageCostOld(Comparable p_date) throws CalculationErrorException {
		double d = getAverageCostOldOld(p_date);
		if(Double.isNaN(d)) return (getLastValidCost());
		return(d);
	}
	private double getAverageCostOldOld(Comparable p_date) throws CalculationErrorException
	{
		if(dataSet.size() == 0) return(0);
		if((p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
			return(0.0);
		}
		int idx = bSearch(p_date);
		if(idx < 0) return(getEndUnitCost(p_date));
		DatedValueWithCost dv0 = (DatedValueWithCost) dataSet.get(idx);
		DatedValueWithCost dv1 = (DatedValueWithCost) dataSet.get(idx+1);
		double netAmount = dv1.pAmount + dv0.nAmount;
		double netCost   = dv1.pCost + dv0.nCost;
		if(Double.isNaN(dv1.nCostValid)) {
			UniLog.log("Cost Calculation Error : nCostValid is false");
			if(debug) dumpDataSet();
			return(Double.NaN);
		}
		/*
		if(!sellShort && netAmount < 0) {
			UniLog.log("Cost Calculation Error : net amount < 0");
			return(Double.NaN);
		}
		*/
		double avCost;
		if(netAmount == 0) return(getEndUnitCost(p_date)); else avCost = netCost/netAmount;
		return(avCost);
	}
	public double getBuyCostChange(Comparable p_date) throws CalculationErrorException
	{
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
			return(0.0);
		}
		int idx = bSearch(p_date);
		if(idx < 0) return(0.0);
//		return(dataSet.get(idx+1).pAmount - dataSet.get(idx).pAmount); 
		return(((DatedValueWithCost) dataSet.get(idx+1)).pCost  - ((DatedValueWithCost) dataSet.get(idx)).pCost); 
	}
	public double getSellCostChange(Comparable p_date) throws CalculationErrorException
	{
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
			return(0.0);
		}
		int idx = bSearch(p_date);
		if(idx < 0) return(0.0);
//		return(dataSet.get(idx+1).pAmount - dataSet.get(idx).pAmount); 
		return(((DatedValueWithCost) dataSet.get(idx+1)).nCost  - ((DatedValueWithCost) dataSet.get(idx)).nCost); 
	}
	public double getBuyCostEnd(Comparable p_date) throws CalculationErrorException
	{
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
//			return(dataSet.get(0).pAmount + dataSet.get(0).nAmount);
			return(((DatedValueWithCost) dataSet.get(0)).pCost);
		}
		int idx = bSearch(p_date);
		if(idx < 0) {
			idx = -(idx+1); // the index to the record just after p_date
		} else {
			idx++; //the balance of next tr date is tbe closing of current date
		}
		DatedValueWithCost dv0 = (DatedValueWithCost) dataSet.get(idx);
		return(dv0.pCost);
	}
	public double getCostEnd(Comparable p_date) throws CalculationErrorException
	{
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
//			return(dataSet.get(0).pAmount + dataSet.get(0).nAmount);
			return(((DatedValueWithCost) dataSet.get(0)).pCost + ((DatedValueWithCost) dataSet.get(0)).nCost);
		}
		int idx = bSearch(p_date);
		if(idx < 0) {
			idx = -(idx+1); // the index to the record just after p_date
		} else {
			idx++; //the balance of next tr date is tbe closing of current date
		}
		DatedValueWithCost dv0 = (DatedValueWithCost) dataSet.get(idx);
		return(dv0.pCost + dv0.nCost);
	}
	public double getRealizedPL(Comparable p_date,double pAmount) throws CalculationErrorException
	{
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
			return(0.0);
		}
		int idx = bSearch(p_date);
		if(idx < 0) {
			idx = -(idx+1); // the index to the record just after p_date
		} else {
			idx++; //the balance of next tr date is tbe closing of current date
		}
		if(idx <= 0) return(0.0);
		DatedValueWithCost dv = (DatedValueWithCost) dataSet.get(idx);
		DatedValueWithCost lastdv = (DatedValueWithCost) dataSet.get(idx-1);
		double deltaP = dv.pAmount - lastdv.pAmount; // amount sold on date dv
		if(deltaP != 0) {
			return(dv.realizedPL * pAmount/deltaP);
		}
		return(0.0);
		/*
		DatedValueWithCost dv = (DatedValueWithCost) dataSet.get(idx);
		double deltaP = dv.pAmount - lastdv.pAmount; // amount sold on date dv
		if(deltaP != 0) {
			double unRealizedAmount = lastdv.pAmount + lastdv.nAmount;
					if(unRealizedAmount < 0) {
						double realizedAmount = (deltaP + unRealizedAmount > 0.0) ? unRealizedAmount : -deltaP;
						avCost = (dv.pCost - lastdv.pCost) / deltaP;
						double realizedCost = avCost * realizedAmount;
						double realizedPL = realizedCost - (realizedAmount * lastdv.nCostValid);
						dv.nCost += realizedPL;
					}
		}
		*/
	}
	double getEndUnitCost(Comparable p_date) throws CalculationErrorException
	{
		if(dataSet.size() > 0 && (p_date.compareTo(dataSet.get(0).valueDate) < 0) && !(p_date.compareTo(purgedDate) < 0)) {
			return(dataSet.get(0).pAmount + dataSet.get(0).nAmount);
		}
		int idx = bSearch(p_date);
		if(idx < 0) {
			idx = -(idx+1); // the index to the record just after p_date
		} else {
			idx++; //the balance of next tr date is tbe closing of current date
		}
		
		DatedValueWithCost dv0 = (DatedValueWithCost) dataSet.get(idx);
		double netAmount = dv0.pAmount + dv0.nAmount;
		if(Double.isNaN(dv0.nCostValid)) {
			UniLog.log("Cost Calculation Error : nCostValid is false");
			if(debug) dumpDataSet();
			return(Double.NaN);
		}
		/*
		if(!sellShort && netAmount < 0) {
			return(Double.NaN);
		}
		*/
		if(netAmount != 0) return( (dv0.pCost + dv0.nCost) / netAmount); else return(0.0);
	}

	protected void setDebug(boolean p_sw) {
		debug = p_sw;
	}
	double getLastValidCost() throws CalculationErrorException{
		for(int i = dataSet.size()-1;i>=0;i--) {
			DatedValueWithCost dv = (DatedValueWithCost) dataSet.get(i);
			if(!Double.isNaN(dv.nCostValid)) {
				UniLog.log("Last Cost Valid on " + dv.valueDate + " pAmount " + dv.pAmount + " nAmount " + dv.nAmount + " pCost " + dv.pCost + " nCost " + dv.nCost + " nCostValid " + dv.nCostValid);
				if(i > 0) {
					dv = (DatedValueWithCost) dataSet.get(i-1);
					return(getAverageCost(dv.valueDate));
				}
			}
		}
		return(Double.NaN);
	}
	void dumpDataSet() {
		for(int i=0;i<dataSet.size();i++) {
			DatedValueWithCost dv = (DatedValueWithCost) dataSet.get(i);
			UniLog.log("dumpDataset " + dv.valueDate + " pAmount " + dv.pAmount + " nAmount " + dv.nAmount + " pCost " + dv.pCost + " nCost " + dv.nCost + " nCostValid " + dv.nCostValid);
		}
	}
	
	public String toJson() throws Exception {
		JSONObject jo = new JSONObject();
		jo.put("maxdate",DateUtil.toDateString((Date) MaxValue, "yyyy/mm/dd"));
		jo.put("purgeddate",DateUtil.toDateString((Date) purgedDate, "yyyy/mm/dd"));
		JSONArray ja = new JSONArray();
		jo.put("values", ja);
		for(int i=0;i<dataSet.size();i++) {
			DatedValueWithCost dv = (DatedValueWithCost) dataSet.get(i);
			JSONObject jv = new JSONObject();
			jv.put("date", DateUtil.toDateString((Date) dv.valueDate,"yyyy/mm/dd"));
			jv.put("pAmount", dv.pAmount);
			jv.put("nAmount", dv.nAmount);
			jv.put("pCost", dv.pCost);
			jv.put("nCost", dv.nCost);
			if(!Double.isNaN(dv.nCostValid)) {
				jv.put("nCostValid", dv.nCostValid);
			}
			ja.put(jv);
		}
		return(jo.toString());
	}
	
	public void fromJson(String str) throws Exception {
		JSONObject jo = new JSONObject(str);
		MaxValue = DateUtil.dateTimeStrToDate(jo.getString("maxdate"));
		purgedDate = DateUtil.dateTimeStrToDate(jo.getString("purgeddate"));
		JSONArray ja  = jo.getJSONArray("values");
		for(int i=0;i<ja.length();i++) {
			JSONObject jv = ja.getJSONObject(i);
			Date dd = DateUtil.dateTimeStrToDate(jv.getString("date"));
			DatedValueWithCost dv = new DatedValueWithCost(dd);
			dv.pAmount =  jv.getDouble("pAmount");
			dv.nAmount =  jv.getDouble("nAmount");
			dv.pCost =  jv.getDouble("pCost");
			dv.nCost =  jv.getDouble("nCost");
			if(jv.has("nCostValid")) {
				dv.nCostValid = jv.getDouble("nCostValid");
				if(jv.has("realizedPL")) {
					dv.realizedPL = jv.getDouble("realizedPL");
				} else {
					dv.realizedPL = 0.0;
				}
			} else {
				dv.nCostValid = Double.NaN;
				dv.realizedPL = 0.0;
			}
			dataSet.add(dv);
		}
	}

	@Override
	protected boolean dataSetCanDelete(int idx) {
		if(((DatedValueWithCost) dataSet.get(idx)).pCost != ((DatedValueWithCost) dataSet.get(idx+1)).pCost) return(false);
		if(((DatedValueWithCost) dataSet.get(idx)).nCost != ((DatedValueWithCost) dataSet.get(idx+1)).nCost) return(false);
		if(((DatedValueWithCost) dataSet.get(idx)).realizedPL != ((DatedValueWithCost) dataSet.get(idx+1)).realizedPL) return(false);
		return(super.dataSetCanDelete(idx));
	}
}
