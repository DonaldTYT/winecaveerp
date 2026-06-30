package com.uniinformation.dynamic.hapyik;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zsoup.helper.StringUtil;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultArApStatement;
import com.uniinformation.bicore.erpv4.BiResultStmov;
import com.uniinformation.bicore.erpv4.BiResultArApStatement.vIndexes;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.PrintMultiDoc;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.UniqueStrings;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class PrintArStatement extends PrintMultiDoc {

	public static class AgingSummary {
		public Date beforeDate;
		public String period;
		public double balanceByInvoiecDate;
		public double balanceByDueDate;

		public AgingSummary(Date p_beforeDate, String p_period) {
			beforeDate = p_beforeDate;
			period = p_period;
		}
	}

	final ArrayList<AgingSummary> agingSummaryList = new ArrayList<AgingSummary>();

	public PrintArStatement() {
		super(null);
	}
	public PrintArStatement(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		skipFetch = true;
	}
	BiResultArApStatement arBr;
	String vcode ;
	vIndexes vIdx;

	public void resetAgingSummaryList(Date[] p_beforeDates, String[] p_periods) {
		if(p_beforeDates == null || p_periods == null ||
				p_beforeDates.length != p_periods.length) {
			throw new IllegalArgumentException("Aging dates and periods must have the same length");
		}
		agingSummaryList.clear();
		for(int i=0;i<p_beforeDates.length;i++) {
			Date beforeDate = p_beforeDates[i];
			agingSummaryList.add(new AgingSummary(
					beforeDate == null ? null : new Date(beforeDate.getTime()),
					p_periods[i]));
		}
	}

	void resetAgingSummaryBalances() {
		for(AgingSummary summary : agingSummaryList) {
			summary.balanceByInvoiecDate = 0.0;
			summary.balanceByDueDate = 0.0;
		}
	}

	void addInvoiceToAgingSummary(Date p_invoiceDate, Date p_dueDate, double p_balance) {
		addBalanceToAgingSummary(p_invoiceDate, p_balance, true);
		addBalanceToAgingSummary(p_dueDate, p_balance, false);
	}

	private void addBalanceToAgingSummary(Date p_date, double p_balance,
			boolean p_byInvoiceDate) {
		if(p_date == null) return;
		AgingSummary matchedSummary = null;
		for(AgingSummary summary : agingSummaryList) {
			if(summary.beforeDate != null && p_date.before(summary.beforeDate) &&
					(matchedSummary == null ||
					 summary.beforeDate.before(matchedSummary.beforeDate))) {
				matchedSummary = summary;
			}
		}
		if(matchedSummary == null) return;
		if(p_byInvoiceDate) {
			matchedSummary.balanceByInvoiecDate += p_balance;
		} else {
			matchedSummary.balanceByDueDate += p_balance;
		}
	}

	@Override
	protected boolean skipPrint() {
		arBr = (BiResultArApStatement) br;
		vcode = br.getCellString("vd_vcode");
		vIdx=arBr.getVindexes(vcode);
		if(vIdx == null) return(true); else return(false);
	}
	@Override
	protected void printOneDoc() throws Exception {
		resetAgingSummaryBalances();
		/*
		BiResultArApStatement arBr = (BiResultArApStatement) br;
		String vcode = br.getCellString("vd_vcode");
		vIndexes vIdx=arBr.getVindexes(vcode);
		if(vIdx == null) return;
		*/
		DecimalFormat df = new DecimalFormat("#,###,##0.00");
		ppj.addPageNo("pageno", "%s of %s",0, 0, 0);
    	ppj.setTrailerAtLastPageOnly(true);
		ppj.addHeaderField("doctitle","月結單",0,0);
		ppj.addHeaderField("cvname",br.getCellString("vd_vname"),0,0);
		ppj.addHeaderField("cvaddr",br.getCellString("vd_addr0"),0,0);
		ppj.addHeaderField("cvphone",br.getCellString("vd_contact"),0,0);
		ppj.addHeaderField("cvphone",br.getCellString("vd_tel"),0,20);
		ppj.addHeaderField("dfvalue",
				"由:"+DateUtil.dateToDateTimeStr(br.getCellDate("stmt_sdate"),"yyyy-MMM-dd")
				,0,0);
		ppj.addHeaderField("dfvalue",
				"至:"+DateUtil.dateToDateTimeStr(br.getCellDate("stmt_edate"),"yyyy-MMM-dd")
				,0,20);
		ppj.addDetailHeaderField("hdr_date", "Date");
		ppj.addDetailHeaderField("hdr_invoiceno", "Invoice");
		ppj.addDetailHeaderField("hdr_description", "Description");
		ppj.addDetailHeaderField("hdr_ccy", "CCY");
		ppj.addDetailHeaderField("hdr_debit", "Debit");
		ppj.addDetailHeaderField("hdr_credit", "Credit");
		if(vIdx.agingIdx >= 0) {
			ppj.addDetailRecord();
			ppj.addDetailRecordField("dvdesc", "承上結欠",0,0);
			double bf = br.getCellDouble("stmt_bf");
			if(bf >= 0) {
				ppj.addDetailRecordField("dvdebit",
					df.format(bf)
					,0,0);
			} else {
				ppj.addDetailRecordField("dvcredit",
					df.format(-bf)+"(結餘)"
					,0,0);
			}
		}
		BiCellCollection bc;
		if(vIdx.sihIdx >= 0) {
			for(int i=vIdx.sihIdx;;i++) {
				bc = arBr.getSihRec(i);
				if(bc == null) break;
				if(!vcode.equals(bc.getCellString("sih_vcode"))) break;
				ppj.addDetailRecord();
				ppj.addDetailRecordField("dvinvoice", bc.getCellString("sih_frxno"),0,0);
				ppj.addDetailRecordField("dvdesc", "本月訂單",0,0);
				ppj.addDetailRecordField("dvdebit",
					df.format(bc.getCellDouble("sih_losbal"))
					,0,0);
				String tstr = DateUtil.dateToDateTimeStr(bc.getCellDate("sih_date"),"yyyy-MMM-dd");
				ppj.addDetailRecordField("dvdate", tstr,0,0);
			}
		}
		if(vIdx.crdIdx >= 0) {
			for(int i=vIdx.crdIdx;;i++) {
				bc = arBr.getCrdRec(i);
				if(bc == null) break;
				if(!vcode.equals(bc.getCellString("vd_vcode"))) break;
				ppj.addDetailRecord();
				ppj.addDetailRecordField("dvinvoice", bc.getCellString("sih_frxno"),0,0);
				ppj.addDetailRecordField("dvdesc", "繳款",0,0);
				ppj.addDetailRecordField("dvcredit",
					df.format(-bc.getCellDouble("crd_lamount"))
					,0,0);
				String tstr = DateUtil.dateToDateTimeStr(bc.getCellDate("crh_date"),"yyyy-MMM-dd");
				ppj.addDetailRecordField("dvdate", tstr,0,0);
			}
		}
		ppj.addBottomField("val_desp","本月結欠",0,0);
		double cf = br.getCellDouble("stmt_cf");
		if(cf >= 0) {
			ppj.addBottomField("val_drtotal", df.format(cf),0,0);
		} else {
			ppj.addBottomField("val_crtotal", df.format(-cf)+"(結餘",0,0);
		}
		super.printOneDoc();
	}
	
	@Override
	protected ReturnMsg initPrtdoc() {
		docCode = "NEWSTMT1";
		ReturnMsg rtn = super.initPrtdoc();
		try {
			ppj.addPageNo("pageno", "%s of %s",0, 0, 0);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		return(rtn);
	}
	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		if(p_isBatch) {
			Object o = biBase.getStateValue("statementReady");
			if((o == null) || ! (o instanceof Boolean) || !((Boolean) o)) {
				return(true);
			}
			return(false);
		} else {
			return(true);
		}
	}
	@Override
	protected String getDocumentName(BiResult p_br) {
		// TODO Auto-generated method stub
		return ("Customer Statement");
	}
	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		batchDownloadReport = true;
		ReturnMsg rtn = super.beforeAction(p_result, cnt);
		return(rtn);
	}	
}
