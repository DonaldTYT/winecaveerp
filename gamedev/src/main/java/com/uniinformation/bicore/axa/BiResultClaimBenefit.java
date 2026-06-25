package com.uniinformation.bicore.axa;

import java.util.Date;
import java.util.HashSet;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.bicore.erpv4.Erpv4BaseCellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultClaimBenefit extends BiResultErpv4 {

	public BiResultClaimBenefit(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		int classno = 0;
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		boolean isDependent = false;
		boolean isFlexi = false;
		if(getParent().getCell("axaphr_plantype") != null) {
			if(getParent().getCellString("axaphr_plantype").equals("F")) isFlexi = true;
		}
		if(getParent().getCell("axaclm_dependno") != null) {
			int cc = getParent().getCellInt("axaclm_dependno");
			if( cc > 1) isDependent = true;
		}
		classno = getParent().getCell("axacvp_class").getInt();
		/*
		BiResult pbr = getParent();
		try {
			TableRec tr = pbr.getSelectUtil().getQueryResult("select * from axacvpd where axacvd_phno = ? and axacvd_certdep = ? and axacvd_effectdate <= ? and axacvd_termination >= ? order by axacvd_effectdate desc",
					new Wherecl()
						.appendArgument(pbr.getCellString("axaclm_phno"))
						.appendArgument(pbr.getCellString("axaclm_certdep"))
						.appendArgument(pbr.getCellString("axaclm_date"))
						.appendArgument(pbr.getCellString("axaclm_date"))
				);
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				if(isFlexi) {
					classno = tr.getFieldInt("axacvd_occlass");
				} else {
					classno = tr.getFieldInt("axacvd_ohclass");
				}
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			return(p_hash);
		}
		*/
//		if(isFlexi) {
//			classno = getParent().getCellInt("axacvp_occlass");
//		} else {
//			classno = getParent().getCellInt("axacvp_ohclass");
//		}
		
		Wherecl wcl1 = new Wherecl();
//		wcl1.appendString(" and axapol_class = " + classno + " and axasta_certdep = '"+getParent().getCellString("axaclm_certdep")+"' ").stripAnd();
		BiResult parent = getParent();
		Date cdate = parent.getCellDate("axaclm_date");
		String phno = parent.getCellString("axaclm_phno");
//		Date pdate = BiResultAxaClaim.getPhrEffectDate(phno, cdate, su);
		Date pdate = parent.getCellDate("axaclm_poledate");
		wcl1.appendString(" and axapol_class = " + classno + " and axapol_effectdate = '"+DateUtil.toDateString(pdate, "yyyy/mm/dd")+"' " +
				" and axasta_certdep = '"+getParent().getCellString("axaclm_certdep")+"' ").stripAnd();
//		wcl1.appendString(" and axapol_class = " + classno + " and axapol_effectdate = '"+DateUtil.dateToDateTimeStr(pdate, "yyyy/mm/dd")+"' ").stripAnd();
		Wherecl wcl2 = new Wherecl();
//		wcl2.orUniop("axabgm_group", "=", getParent().getCellString("axabfg_group"));
//		wcl2.orUniop("axabfc_type", "=",2);
		wcl2.andUniop("axabgm_group", "=", getParent().getCellString("axabfg_group"));
		wcl1.andWherecl(wcl2);
		p_where.andWherecl(wcl1);
		if(isDependent) {
			p_where.andWherecl(new Wherecl().andUniop("axapol_dependcode", "=", "Y"));
		}

		p_where.andWherecl(new Wherecl().andUniop("axasta_lastandate", "=", getParent().getCellDate("axaphr_lastandate")));
		if(ht == null) {
			ht = new HashSet<BiTable>();
		}
		ht.add(getView().getSchema().getTable("axabfcode"));
		return(ht);
	}
	
	static public double calAxaAmount(BiCellCollection p_col,double p_agreefee) {
		double deductamt = 0.0;
		double benefitamt = p_col.getCellInt("axapol_benefit");
		int claim_per_day = p_col.getCellInt("axapol_noofday");
		if(claim_per_day <= 0) {
			benefitamt -= p_col.getCellDouble("axasta_payamount");
			if(benefitamt < 0) benefitamt = 0;
		}
				if(p_col.getBoolean("axapol_olimitcode")) {
					if(p_col.getCellDouble("axapol_oblimit") > 0 ) {
						double maxclaim = p_col.getCellDouble("axapol_oblimit") - p_col.getDouble("axaclm_totalclaim");
						if(maxclaim < benefitamt) {
							benefitamt = maxclaim;
						}
					}
				}
		double reimbursement = p_col.getCellInt("axapol_reimburse")/100.0;
		if(p_col.getCellString("axapol_deductcode").equals("Y")) {
			deductamt = p_col.getCellInt("axapol_deductamt");
		}
		double claimamt = (p_agreefee - deductamt) * reimbursement;
		return(claimamt > benefitamt ? benefitamt : claimamt);
	}
}
