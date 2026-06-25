package com.uniinformation.erpv4;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.accumulator.BaseAccumulator;
import com.uniinformation.accumulator.CalculationErrorException;
import com.kyoko.common.*;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class GlBalanceCalculation {
	static public class BalanceAccumulator extends BaseAccumulator {
		public BalanceAccumulator(Comparable p_maxValue) {
			super(p_maxValue);
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
	public static class BalanceAccumulatorPair{
		BalanceAccumulator lacu;
		BalanceAccumulator cacu;
		public BalanceAccumulatorPair(Comparable p_maxValue) {
			lacu = new BalanceAccumulator(p_maxValue);
			cacu = new BalanceAccumulator(p_maxValue);
		}
		public BalanceAccumulator getLacu() {
			return(lacu);
		}
		public BalanceAccumulator getCacu() {
			return(cacu);
		}
	}
	static class DacAccumulator {
		BalanceAccumulator lccyAcu;
		Hashtable<String,BalanceAccumulatorPair> ccyAcuHash;
		public DacAccumulator() {
			lccyAcu = new BalanceAccumulator(DateUtil.maxDate);
			ccyAcuHash = new Hashtable<String,BalanceAccumulatorPair>();
		}
	}
	static Hashtable<String,Hashtable<String,Hashtable<String,DacAccumulator>>> agCoAcHash = new Hashtable<String,Hashtable<String,Hashtable<String,DacAccumulator>>> (); ;
	
	
	static public void clearAcu(SessionHelper p_sh,String p_cocode,String p_ano) throws Exception {
		synchronized(agCoAcHash) {
		if(p_cocode != null) {
			Hashtable<String,Hashtable<String,DacAccumulator>> coAcHash = agCoAcHash.get(p_sh.getAgent());
			if(coAcHash != null) {
				if(p_ano != null) {
					Hashtable<String,DacAccumulator> acHash = coAcHash.get(p_cocode);
					if(acHash != null) {
						acHash.remove(p_ano);
					}
				} else {
					coAcHash.remove(p_cocode);
				}
			}
		} else {
			agCoAcHash.remove(p_sh.getAgent());
		}
		}
	}
	static DacAccumulator getOrLoadAcu(SessionHelper p_sh,String p_cocode,String p_ano) throws Exception {
		synchronized(agCoAcHash) {
		if(StringUtils.isBlank(p_cocode)) return(null);
		if(StringUtils.isBlank(p_ano)) return(null);
		Hashtable<String,Hashtable<String,DacAccumulator>> coAcHash = agCoAcHash.get(p_sh.getAgent());
		if(coAcHash == null) {
			coAcHash = new Hashtable<String,Hashtable<String,DacAccumulator>> (); ;
			agCoAcHash.put(p_sh.getAgent(), coAcHash);
		}
		Hashtable<String,DacAccumulator> acHash = coAcHash.get(p_cocode);
		if(acHash == null) {
			acHash = new Hashtable<String,DacAccumulator> (); ;
			coAcHash.put(p_cocode,acHash);
		}
		DacAccumulator acu = acHash.get(p_ano);
		if(acu == null) {
			acu = new DacAccumulator(); 
			SelectUtil su = new SelectUtil();
			try {
				UniLog.log1("Loading Gl Transaction for %s:%s",p_cocode,p_ano);
				su.init(p_sh.getBiSchema().getConn());
				TableRec tr = su.getQueryResult("select * from jn,tr where jn_cocode = ? and jn_inputano = ? and tr_cocode = jn_cocode and tr_xno = jn_xno order by jn_xdate,jn_xno",
						new Wherecl().appendArgument(p_cocode).appendArgument(p_ano)
						);
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					Date d;
					if(tr.getFieldInt("tr_fpladj") > 0) {
						d = new Date(tr.getFieldDate("jn_xdate").getTime() - 3600000);
					} else {
						d = tr.getFieldDate("jn_xdate");
					}
					double pamt = tr.getFieldDouble("jn_lamount");
					double namt = 0;
					if(pamt < 0) {
						namt = pamt;
						pamt = 0;
					}
					acu.lccyAcu.updateBalance(d, pamt, namt, 1 , true);
					BalanceAccumulatorPair ccyacuPair = acu.ccyAcuHash.get(tr.getFieldString("jn_cid"));
					if(ccyacuPair == null) {
						ccyacuPair = new BalanceAccumulatorPair(DateUtil.maxDate);
						acu.ccyAcuHash.put(tr.getFieldString("jn_cid"), ccyacuPair);
					}
					ccyacuPair.lacu.updateBalance(d, pamt, namt, 1 , true);
					pamt = tr.getFieldDouble("jn_amount");
					namt = 0;
					if(pamt < 0) {
						namt = pamt;
						pamt = 0;
					}
					ccyacuPair.cacu.updateBalance(d, pamt, namt, 1 , true);
				}
			} catch (Exception e) {
				throw(e);
			} finally {
				su.close();
			}
			acHash.put(p_ano, acu);
			
		}
		return(acu);
		}
	}
	
	static public BalanceAccumulator getCaBalanceAccumulator(SessionHelper p_sh,String p_cocode,String p_ano) throws Exception{
		DacAccumulator acu = getOrLoadAcu(p_sh,p_cocode,p_ano);
		if(acu == null) return(null);
		return(acu.lccyAcu);
	}
	static public BalanceAccumulatorPair getDaBalanceAccumulator(SessionHelper p_sh,String p_cocode,String p_ano,String p_ccy) throws Exception{
		if(StringUtils.isBlank(p_ccy)) return(null);
		DacAccumulator acu = getOrLoadAcu(p_sh,p_cocode,p_ano);
		if(acu == null) return(null);
		return(acu.ccyAcuHash.get(p_ccy));
	}
}
