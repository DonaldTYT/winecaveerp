package com.uniinformation.webcore.clclaim;

import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.erpv4.Erpv4SessionHelper;

public class ClclaimSessionHelper extends Erpv4SessionHelper {
	/*
	String fullName = null;
	int mrg;
	*/
	@Override
	public boolean loginProceed(String p_loginid, String p_password) throws Exception{
			try {
				SelectUtil su = new SelectUtil(); 
				su.init(getJdbcPool());
				TableRec tr = su.getQueryResult("select * from otlogmaster where otlm_loginid = ?",new Wherecl().appendArgument(p_loginid));
				su.close();
				if(tr.getRecordCount() <= 0) return(super.loginProceed(p_loginid, p_password));
				tr.setRecPointer(0);
				if(tr.getFieldString("otlm_password").equals(p_password)) {
					boolean ok = super.loginProceed("clinicot", "cl2022");
					if(ok) {
						setVcode(p_loginid);
						/*
						fullName = tr.getFieldString("otlm_name");
						mrg = tr.getFieldIndex("otlm_rg");
						*/
						return(ok);
					}
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
			return(false);
	}
	
	@Override
	protected String getLoginIdByVcode(String p_vcode) {
			try {
				SelectUtil su = new SelectUtil(); 
				su.init(getJdbcPool());
				TableRec tr = su.getQueryResult("select * from otlogmaster where otlm_loginid = ?",new Wherecl().appendArgument(p_vcode));
				su.close();
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					/*
					fullName = tr.getFieldString("otlm_name");
					mrg = tr.getFieldIndex("otlm_rg");
					*/
					return("clinicot");
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		
		return(p_vcode);
	}
	
	/*
	@Override
	public Object getLoginProperty(PROPNAME p_propname) throws Exception {
		switch (p_propname) {
			case FULLNAME: if(fullName != null) return(fullName);
			case URG: return(mrg);
		}
		return(null);
	}
	*/
}
