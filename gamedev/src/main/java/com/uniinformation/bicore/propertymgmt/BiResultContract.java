package com.uniinformation.bicore.propertymgmt;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import com.google.common.collect.Sets;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.bischema.BiResultExcelSheet;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

//import oracle.net.aso.e;

public class BiResultContract extends BiResultPropertyMgmt {

	public BiResultContract(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	@Override 
	/* use this for post process validataion after add/update to database */
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col,boolean isUpdate) {
//		if(!isUpdate) return(ReturnMsg.defaultOk);
		Date stDate = DateUtil.monthStart(col.getCell("col_c").getDate());
		Date endDate = DateUtil.monthStart(col.getCell("col_d").getDate());
		boolean contractUnitChanged = false;
		
		String mStrBegin = DateUtil.dateToDateTimeStr(stDate,"yyyy-MM");
		String mStrEnd = DateUtil.dateToDateTimeStr(endDate,"yyyy-MM");
		try {
			getSelectUtil().executeUpdate("delete from contractmonth where col_a = ? and col_b = ? and col_c not between ? and ? ", 
							new Wherecl()
							.appendArgument(col.getCellString("col_a"))
							.appendArgument(col.getCellString("col_b"))
							.appendArgument(mStrBegin)
							.appendArgument(mStrEnd)
							) ;
			TableRec tr = getSelectUtil().getQueryResult("select * from contractmonth where col_a = ? and col_b = ? and col_c between ? and ? order by col_c", 
							new Wherecl()
							.appendArgument(col.getCellString("col_a"))
							.appendArgument(col.getCellString("col_b"))
							.appendArgument(mStrBegin)
							.appendArgument(mStrEnd)
							) ;
			int rec = 0;
			for(Date d = stDate;!d.after(endDate);d = DateUtil.nextMonthStart(d)) {
				String mStr = DateUtil.dateToDateTimeStr(d,"yyyy-MM");
				if(tr.getRecordCount() > rec) {
					tr.setRecPointer(rec);
					if(tr.getFieldString("col_c").equals(mStr)) {
						rec++;
						continue;
					}
				}
				getSelectUtil().executeUpdate("insert into contractmonth (col_a,col_b,col_c) values (?,?,?) ", 
							new Wherecl()
							.appendArgument(col.getCellString("col_a"))
							.appendArgument(col.getCellString("col_b"))
							.appendArgument(mStr)
							) ;
			}
			HashSet<String> cUnits = new HashSet<String>();
			for(BiCellCollection bc : getSubLink("propertymgmt.contractfee").getRowCollectionList()) {
				cUnits.add(bc.getCellString("col_c"));
			}
			HashSet<String> rUnits = new HashSet<String>();
			HashSet<String> ctypes = new HashSet<String>();
			if(getCellBoolean("col_e")) {
				ctypes.add("住宅");
			}
			if(getCellBoolean("col_f")) {
				ctypes.add("商鋪");
			}
			if(getCellBoolean("col_g")) {
				ctypes.add("車位");
			}
			if(!ctypes.isEmpty()) {
				tr = getSelectUtil().getQueryResult("select * from property",
						new Wherecl().andUniop("col_b", "=", col.getCellString("col_a")).genInList("and", "col_a", "in", ctypes)
						);
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					rUnits.add(tr.getFieldString("key_a"));
				}
			}
			Set<String> hs = Sets.difference(cUnits, rUnits);
			if(!hs.isEmpty()) {
				getSelectUtil().executeUpdate("delete from contractfee", 
						new Wherecl().andUniop("col_a", "=", col.getCellString("col_a")).andUniop("col_b", "=", col.getCell("col_b").getDate()).genInList("and","col_c","in",hs)
						);
				contractUnitChanged = true;
			}
			hs = Sets.difference(rUnits, cUnits);
			SimpleDateFormat dfmt = new SimpleDateFormat("yyyy-MM");
			for(String ss : hs) {
				tr = getSelectUtil().getQueryResult("select * from property",
						new Wherecl().andUniop("key_a", "=", ss));
				tr.setRecPointer(0);
				getSelectUtil().executeUpdate("insert into contractfee (col_a,col_b,col_c,col_d,col_e,col_f,col_g,col_h,col_i) values (?,?,?,?,?,?,?,?,?) ", 
							new Wherecl()
							.appendArgument(col.getCellString("col_a"))
							.appendArgument(col.getCellString("col_b"))
							.appendArgument(ss)
							.appendArgument(tr.getFieldDouble("col_i"))
							.appendArgument(tr.getFieldDouble("col_j"))
							.appendArgument(col.getCellInt("vcol_nmonths"))
							.appendArgument(col.getCellString("col_h"))
							.appendArgument(dfmt.format(col.getCell("col_c").getDate()))
							.appendArgument(dfmt.format(col.getCell("col_d").getDate()))
							) ;
				contractUnitChanged = true;
			}
			if( contractUnitChanged ) {
				tr = getSelectUtil().getQueryResult("select sum(col_d) summgtfee,sum(col_e) sumresfee from contractfee where col_a = ? and col_b = ?",
						new Wherecl().appendArgument(col.getCellString("col_a")).appendArgument(col.getCellString("col_b"))
						);
				getSelectUtil().executeUpdate("update contract set col_i = ? , col_j = ? where col_a = ? and col_b = ?", 
						new Wherecl()
							.appendArgument(tr.getFieldDouble("summgtfee")).appendArgument(tr.getFieldDouble("sumresfee"))
							.appendArgument(col.getCellString("col_a")).appendArgument(col.getCellString("col_b"))
						);
				
			}
		} catch (Exception ex ) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return(ReturnMsg.defaultOk);
	}

}
