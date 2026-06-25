package com.uniinformation.utils.erpv4;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.*;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.Wherecl;

public class BxRateUtil {
	String basecid;
	class RateRec {
		Date sdate;
		double rate;
		public RateRec(Date p_sdate,double p_rate) {
			sdate = p_sdate;
			rate = p_rate;
		}
	}
	HashMap<String,ArrayList<RateRec>> xRateMap;
	HashMap<String,Double> currentXrateMap;
	
	public BxRateUtil(String p_basecid) {
		basecid = p_basecid;
	}
	public Double getRateByDate(String p_cid,Date p_date,SelectUtil su) throws Exception {
		if(basecid.equals(p_cid)) return(1.0);
		if(StringUtils.isBlank(p_cid)) return(0.0);
		if(xRateMap == null) {
			xRateMap = new HashMap<String,ArrayList<RateRec>> ();
		}
		ArrayList <RateRec> arr = xRateMap.get(p_cid);
		if(arr == null) {
			arr = new ArrayList <RateRec> ();
			TableRec tr = su.getQueryResult("select * from bxrate where bx_basecid = ? and bx_cid = ? order by bx_sdate",
						new Wherecl()
							.appendArgument(basecid)
							.appendArgument(p_cid)
					);
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				arr.add(new RateRec(
							tr.getFieldDate("bx_sdate"),
							tr.getFieldDouble("bx_xrate")
						));
			}
			xRateMap.put(p_cid,arr);
		}
		for(int i=arr.size()-1;i>=0;i--) {
			if(!arr.get(i).sdate.after(p_date)) {
				return(arr.get(i).rate);
			}
		}
		return(null);
	}
	public Double getRate(String p_cid,SelectUtil su) throws Exception {
		if(currentXrateMap == null) {
			Date dd = DateUtil.today();
			currentXrateMap = new HashMap<String,Double>();
			TableRec tr = su.getQueryResult("select * from bxrate where bx_basecid = ? and bx_sdate <= ? and bx_edate >= ?",
						new Wherecl()
							.appendArgument(basecid)
							.appendArgument(dd)
							.appendArgument(dd)
					);
			for(int i = 0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				currentXrateMap.put(tr.getFieldString("bx_cid"), tr.getFieldDouble("bx_xrate"));
			}
		}	
		return(currentXrateMap.get(p_cid));
	}
	
	public String getBaseCid() {
		return(basecid);
	}
}
