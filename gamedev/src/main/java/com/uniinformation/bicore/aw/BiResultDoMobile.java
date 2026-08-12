package com.uniinformation.bicore.aw;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultDoMobile extends BiResultErpv4 {

	public BiResultDoMobile(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	void addDdateToQash(HashMap<String,Date> qHash,HashSet<String> xquos,String cocode) throws Exception {
			String condStr = null;
			for(String ss : xquos) {
				if(condStr == null) condStr = "inv_invno in('"+ss.trim()+"'";
				else condStr += ",'"+ ss.trim()+"'";
			}
			condStr += ")";
			SelectUtil su = getSelectUtil();
					
			TableRec tr = su.getQueryResult(
						"select inv_invno,min(jm_ddate) ddate from quotation,jobmaster_real where inv_cocode = '"+cocode+"' and jm_jobno = inv_jobno and jm_ddate > 0 and "+condStr + " group by 1");
			UniLog.log("query quotation" + tr.getRecordCount() + " records");
			condStr = null;
			for(int i=0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				qHash.put(tr.getFieldString("inv_invno"), tr.getFieldDate("ddate"));
			}
		
	}
	HashMap<String,Date>quoDdateHash;
	@Override
	protected ReturnMsg afterLoadSerialMap2() {
			String cocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
			quoDdateHash = new HashMap<String,Date>();
			HashSet<String> xquos = new HashSet();
			int dDatePos = getSelectFieldPosition( getView().getColumnByLabel("stm_expdeli"));
			try {
			for(int i=0;i<getTableRecCount();i++) {
				loadOneRec(i,getDefaultRowCollection(),false);
				Date dd = getCellDate("stm_expdeli");
				
				if(!dd.after(DateUtil.minDate)) {
					String qs = getCellString("stm_allorders");
					if(!StringUtils.isBlank(qs)) {
						String qlist[] = qs.split(" ");
						for(String q : qlist) {
							if(!xquos.contains(q)) {
								xquos.add(q);
							}
						}
						if(xquos.size() > 1500) {
							addDdateToQash(quoDdateHash,xquos,cocode);
							xquos.clear();
						}
					}
				}
			}
			if(xquos.size() > 0) {
				addDdateToQash(quoDdateHash,xquos,cocode);
				xquos.clear();
			}
			} catch (Exception ex) { 
				UniLog.log(ex);
			}
			for(int i=0;i<getTableRecCount();i++) {
				loadOneRec(i,getDefaultRowCollection(),false);
				Date dd = getCellDate("stm_expdeli");
				if(!dd.after(DateUtil.minDate)) {
					String qs = getCellString("stm_allorders");
					if(!StringUtils.isBlank(qs)) {
						String qlist[] = qs.split(" ");
						Date xdate = null;
						for(String q : qlist) {
							Date qd = quoDdateHash.get(q);
							if(qd != null) {
								if(xdate == null || qd.before(xdate)) {
									xdate = qd;
								}
							}
						}
						if(xdate != null) {
							try {
								saveOneObjectToResultTr(i,dDatePos,xdate);
							} catch (Exception ex) {
								UniLog.log(ex);
							}
						}
					}
				}
			}
		return(ReturnMsg.defaultOk);
	}
}
