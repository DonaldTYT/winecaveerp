package com.uniinformation.bicore.propmgmtpro;

import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.propertymgmt.BiResultPropertyMgmt;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultPropmgmtPro extends BiResultPropertyMgmt {

	public BiResultPropmgmtPro(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
	}

	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new PropmgmtProCellCollection(p_parent, this));
	}

	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
		if(!getSessionHelper().hasAccessRight("#allproperty")) {
    		int lcrg = Erpv4Config.getDefaultLcrg(sh);
    		String pncol = null;
    		switch (getView().getName()) {
    		case "propmgmtpro.Metting":
    		case "propmgmtpro.SigninInput":
    		case "propmgmtpro.VoteInput":
    		case "propmgmtpro.TmpVoteInput":
    		case "propmgmtpro.RptSignin":
    		case "propmgmtpro.RptVoteResult":
    		case "propmgmtpro.RptVoteDetail":
    			pncol = "col_a";
    			break;
    		}
    		if (pncol != null) {
    			p_where.andUniop(pncol, "=", lcrg);
    			Date mettingDate = (Date)sh.getSessionData("METTING_DATE");
    			UniLog.log1("mettingDate:%s", mettingDate);
    			if (mettingDate != null)
    				p_where.andUniop("col_b", "=", mettingDate);
    		}
		} else {
			UniLog.log("skip location filter");
		}
		return(ht);
	}	
}
