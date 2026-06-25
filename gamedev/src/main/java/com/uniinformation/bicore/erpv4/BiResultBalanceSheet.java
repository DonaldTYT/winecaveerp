package com.uniinformation.bicore.erpv4;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiReportInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultBalanceSheet extends BiResultControlAccount {
	
	Date blStart,blEnd;
	Hashtable<String,Double>blDebitHash;
	Hashtable<String,Double>blCreditHash;
	String department = null;


	public BiResultBalanceSheet(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		Date td = DateUtil.today();
		blStart = DateUtil.monthStart(td);
		blEnd = DateUtil.monthEnd(td);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		String cocode = Erpv4Config.getDefaultCoCode(sh);
		String blcocode;
		if(department != null) {
			blcocode = String.format("%-10.10s%s", cocode,department);
		} else {
			blcocode = cocode;
		}
		Wherecl wcl0 = new Wherecl();
		wcl0.appendString(" and bl_start.bl_cocode = '"+blcocode+"' and bl_start.bl_pstart = '"+DateUtil.toDateString(blStart, "yyyy/mm/dd")+"' ").stripAnd();
//		wcl0.appendString(" and bl_end.bl_cocode = '"+blcocode+"' and bl_end.bl_pend= '"+DateUtil.toDateString(blEnd, "yyyy/mm/dd")+"' ").stripAnd();
		p_where.andWherecl(wcl0);
		if(Erpv4Config.isMultiCompany(sh)) {
			if(getCell("ca_cocode") != null) {
				Wherecl wcl1 = new Wherecl();
				wcl1.appendString(" and ca_cocode = '"+cocode+"' ").stripAnd();
				p_where.andWherecl(wcl1);
				if(ht == null) {
					ht = new HashSet<BiTable>();
				}
			}
			return(ht);
		} else return(ht);
	}
	public void setBlStart(Date p_date) {
		blStart = p_date;
	}
	public void setBlEnd(Date p_date) {
		blEnd = p_date;
	}

	@Override 
	public ReturnMsg query(boolean p_rollback, boolean p_sortFlag) {
		
		blDebitHash = new Hashtable<String,Double>();
		blCreditHash = new Hashtable<String,Double>();
		if(blStart.after(DateUtil.minDate) && blEnd.after(DateUtil.minDate)) {
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			/*
			if(Erpv4Config.isMultiCompany(getSessionHelper()) && department != null) {
				cocode = String.format("%-10.10s%s", cocode,department);
			}
			*/
			try {
				TableRec tr 
					= getSelectUtil().getQueryResult("select ca_ctrlano,sum(greatest(0.0,jn_lamount)) pAmount,sum(least(0.0,jn_lamount)) nAmount from ca,jn,tr"
						+ "	where ca_cocode = '" + cocode + "' and jn_cocode = ca_cocode and jn_ano = ca_ano and tr_cocode = jn_cocode and tr_xno = jn_xno and tr_post = 'P' and "
						+ (department == null ? "" : " jn_gldpcode = '"+department+"' and ")
						+ " tr_xdate between '"+DateUtil.toDateString(blStart, "yyyy/mm/dd")+"' and '"+DateUtil.toDateString(blEnd, "yyyy/mm/dd")+"'" 
						+ " and (tr_xdate > '" + DateUtil.toDateString(blStart, "yyyy/mm/dd") + "' or tr_fpladj <> 1)"
						+ " group by 1 " , null);	
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					blDebitHash.put(tr.getFieldString("ca_ctrlano"), tr.getFieldDouble("pAmount"));
					blCreditHash.put(tr.getFieldString("ca_ctrlano"), tr.getFieldDouble("nAmount"));
				}
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,ex.toString()));
			}
		}
		return(super.query(p_rollback, p_sortFlag));
	}

	private enum FuncName { FUNC_getDebitAmount, FUNC_getCreditAmount,NOT_DEFINED }
	class BalanceSheetCellCollection extends Erpv4BaseCellCollection {
		
		public BalanceSheetCellCollection(BiCellCollection p_parent, BiResultErpv4 p_br) {
			super(p_parent, p_br);
			// TODO Auto-generated constructor stub
		}
		@Override
		public Object evalFunction(String p_fname,Vector p_args) throws Exception {
			FuncName funcName = FuncName.NOT_DEFINED;
			try {
				funcName = FuncName.valueOf("FUNC_"+p_fname);
			}
			catch(Exception ex) {
				//remark: if enum not exist, will got exception here.
			}
			switch (funcName){
				case FUNC_getDebitAmount: {
					String ano = (String) p_args.get(0);
					if(ano == null || blDebitHash == null) return(0.0);
					Double d = blDebitHash.get(ano);
					if(d == null ) return(0.0);
					return(d);
				}
				case FUNC_getCreditAmount: {
					String ano = (String) p_args.get(0);
					if(ano == null || blCreditHash == null) return(0.0);
					Double d = blCreditHash.get(ano);
					if(d == null ) return(0.0);
					return(d);
				}
			}
			
			return(super.evalFunction(p_fname,p_args) );
		}
		
	}

	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new BalanceSheetCellCollection(p_parent, this));
	}

	@Override
	protected ReturnMsg afterLoadSerialMap() {
		ReturnMsg rtn = super.afterLoadSerialMap();
		if(rtn != null && !rtn.getStatus()) {
			return(rtn);
		}
		ArrayList<String> acs = new ArrayList();
		for(String ks : blDebitHash.keySet()) {
			acs.add(ks);
		}
		for(String s : acs) {
			List<String> parents = getParent(s);
			for(String ps : parents) {
				Double d0 = blDebitHash.get(s);
				Double d1 = blDebitHash.get(ps);
				if(d1 == null) d1 = 0.0;
				d1 += d0;
				blDebitHash.put(ps, d1);
				
				d0 = blCreditHash.get(s);
				d1 = blCreditHash.get(ps);
				if(d1 == null) d1 = 0.0;
				d1 += d0;
				blCreditHash.put(ps, d1);
				
			}
		}
		return(rtn);
	}
	
	public void setDepartment(String p_dept) {
		department = p_dept;
		if(department == null || department.trim().equals("")) department = null;
	}
}
