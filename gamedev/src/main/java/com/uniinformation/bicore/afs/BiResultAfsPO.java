package com.uniinformation.bicore.afs;

import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultPO;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsPO extends BiResultPO{
	public BiResultAfsPO (BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr,p_sh);
		UniLog.log("BiResultAfsPO Used");
		stmdLinkName = "AfsPoDet";
		if(getView().getName().equals("afs.AfsPoParts")) {
			poType = "AFSP";
		} else
		if(getView().getName().equals("afs.AfsPoMc")) {
			poType = "AFSM";
//		} else throw new CellException("unspported purchase order view");
		} 
	}
	
	@Override
	public String newPoCode(SelectUtil su,java.util.Date p_date,String p_prefix) {
		try {
			String s = null;
			java.util.Date d = p_date;
			String ds = DateUtil.toDateString(d, "yymmdd");
			int nextidx = 1;
			TableRec tr = null;
			if(/* p_prefix.equals("AFSM")*/ true) {
				tr = su.getQueryResult("select stm_ref1 from stmov where stm_type = 'DP' and stm_ref1 regexp '^" + p_prefix + ds + ".*' order by stm_ref1 desc",null);
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					s = tr.getField("stm_ref1").toString();
					String ss = StringUtil.strpart(s, 11, -1);
					int n = Integer.parseInt(ss) + 1;
					if(n >= nextidx) nextidx = n;
				}
				s = String.format("%s%s-%d",p_prefix,ds, nextidx);
			} else {
				tr = su.getQueryResult("select stm_ref1 from stmov where stm_ref1 regexp '^" + p_prefix + ds + ".*' order by stm_ref1 desc",null);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					s = tr.getField("stm_ref1").toString();
					String ss = StringUtil.strpart(s, 11, -1);
					nextidx = Integer.parseInt(ss) + 1;
				}
				s = String.format("%s%s-%03d",p_prefix,ds, nextidx);
			}
			return(s);
		} catch (Exception cex ) {
			UniLog.log(cex);
			return(null);
		}
	}	
}
