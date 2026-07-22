package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAccountBalance extends BiResultErpv4 {
	Date blStart,blEnd;
	Hashtable<String,Double>blDebitHash;
	Hashtable<String,Double>blCreditHash;
	Hashtable<String,Double>lblDebitHash;
	Hashtable<String,Double>lblCreditHash;
	String department = null;


	public BiResultAccountBalance(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
		Date td = DateUtil.today();
		blStart = DateUtil.monthStart(td);
		blEnd = DateUtil.monthEnd(td);
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
		lblDebitHash = new Hashtable<String,Double>();
		lblCreditHash = new Hashtable<String,Double>();
		if(blStart.after(DateUtil.minDate) && blEnd.after(DateUtil.minDate)) {
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			try {
				TableRec tr 
					= getSelectUtil().getQueryResult("select jn_ano,sum(greatest(0.0,jn_lamount)) plAmount,sum(least(0.0,jn_lamount)) nlAmount, sum(greatest(0.0,jn_amount)) pAmount, sum(least(0.0,jn_amount)) nAmount from jn,tr"
						+ "	where jn_cocode = '" + cocode + "' and tr_cocode = jn_cocode and tr_xno = jn_xno and tr_post = 'P' and "
						+ (department == null ? "" : " jn_gldpcode = '"+department+"' and ")
						+ " tr_xdate between '"+DateUtil.toDateString(blStart, "yyyy/mm/dd")+"' and '"+DateUtil.toDateString(blEnd, "yyyy/mm/dd")+"'" 
						+ " group by 1 " , null);	
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					blDebitHash.put(tr.getFieldString("jn_ano"), tr.getFieldDouble("pAmount"));
					blCreditHash.put(tr.getFieldString("jn_ano"), tr.getFieldDouble("nAmount"));
					lblDebitHash.put(tr.getFieldString("jn_ano"), tr.getFieldDouble("plAmount"));
					lblCreditHash.put(tr.getFieldString("jn_ano"), tr.getFieldDouble("nlAmount"));
				}
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,ex.toString()));
			}
		}
		return(super.query(p_rollback, p_sortFlag));
	}
	
	private enum FuncName { FUNC_getDebitAmount, FUNC_getCreditAmount,FUNC_getlDebitAmount,FUNC_getlCreditAmount, NOT_DEFINED }
	class AccountBalanceCellCollection extends Erpv4BaseCellCollection {
		
		public AccountBalanceCellCollection(BiCellCollection p_parent, BiResultErpv4 p_br) {
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
				case FUNC_getlDebitAmount: {
					String ano = (String) p_args.get(0);
					if(ano == null || lblDebitHash == null) return(0.0);
					Double d = lblDebitHash.get(ano);
					if(d == null ) return(0.0);
					return(d);
				}
				case FUNC_getlCreditAmount: {
					String ano = (String) p_args.get(0);
					if(ano == null || lblCreditHash == null) return(0.0);
					Double d = lblCreditHash.get(ano);
					if(d == null ) return(0.0);
					return(d);
				}
			}
			return(super.evalFunction(p_fname,p_args) );
		}
		
	}
	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new AccountBalanceCellCollection(p_parent, this));
	}
	@Override
	protected void afterFetch() {
		BiResult sr = getSubLink("erpv4.JnDetail");
		if(sr != null && sr instanceof BiResultJnDetail) {
				((BiResultJnDetail) sr).reloadBalance();
		}
	}
	public void setDepartment(String p_dept) {
		department = p_dept;
		if(department == null || department.trim().equals("")) department = null;
	}
}
