package com.uniinformation.dynamic.winecave;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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
		public double totalAmountDue;
		public double invoiceAmount;

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
			summary.totalAmountDue = 0.0;
			summary.invoiceAmount = 0.0;
		}
	}

	void addInvoiceToAgingSummary(Date p_invoiceDate, double p_outstandingBalance) {
		if(p_invoiceDate == null) return;
		AgingSummary invoicePeriod = null;
		for(AgingSummary summary : agingSummaryList) {
			if(summary.beforeDate != null && p_invoiceDate.before(summary.beforeDate)) {
				summary.totalAmountDue += p_outstandingBalance;
				if(invoicePeriod == null ||
						summary.beforeDate.before(invoicePeriod.beforeDate)) {
					invoicePeriod = summary;
				}
			}
		}
		if(invoicePeriod == null) {
			for(AgingSummary summary : agingSummaryList) {
				if(summary.beforeDate != null &&
						(invoicePeriod == null ||
						 invoicePeriod.beforeDate.before(summary.beforeDate))) {
					invoicePeriod = summary;
				}
			}
		}
		if(invoicePeriod != null) invoicePeriod.invoiceAmount += p_outstandingBalance;
	}

	private void setupDefaultAgingSummaryList(Date p_statementEndDate) {
		if(!agingSummaryList.isEmpty() || p_statementEndDate == null) return;
		Date[] beforeDates = new Date[5];
		String[] periods = new String[5];
		SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM yyyy", Locale.ENGLISH);
		Calendar beforeDate = Calendar.getInstance();
		beforeDate.setTime(p_statementEndDate);
		beforeDate.set(Calendar.DAY_OF_MONTH, 1);
		beforeDate.set(Calendar.HOUR_OF_DAY, 0);
		beforeDate.set(Calendar.MINUTE, 0);
		beforeDate.set(Calendar.SECOND, 0);
		beforeDate.set(Calendar.MILLISECOND, 0);
		beforeDate.add(Calendar.MONTH, 1);
		for(int i=0;i<beforeDates.length;i++) {
			beforeDates[i] = beforeDate.getTime();
			if(i == 0) {
				periods[i] = "Current Month";
			} else {
				Calendar periodMonth = (Calendar) beforeDate.clone();
				periodMonth.add(Calendar.MONTH, -1);
				periods[i] = (i == beforeDates.length-1 ? "On or before " : "")+
						monthFormat.format(periodMonth.getTime());
			}
			beforeDate.add(Calendar.MONTH, -1);
		}
		resetAgingSummaryList(beforeDates, periods);
	}

	private String formatAgingBalance(DecimalFormat p_df, double p_balance) {
		if(p_balance == 0.0) return "";
		return p_df.format(p_balance);
	}

	private void printAgingSummary(DecimalFormat p_df, Date p_statementEndDate)
			throws Exception {
		if(agingSummaryList.isEmpty() || p_statementEndDate == null) return;
		int y = 0;
		double totalInvoiceAmount = 0.0;
		AgingSummary currentPeriod = null;
		ppj.addBottomField("val_agentdet", "Aging Information:", 0, y);
		ppj.addBottomField("val_agentdet", "Total Amount Due", 330, y);
		ppj.addBottomField("val_agentdet", "Invoice Amount", 450, y);
		y += 20;
		ppj.addBottomField("val_agentdet", "HKD", 380, y);
		ppj.addBottomField("val_agentdet", "HKD", 480, y);
		y += 20;
		for(AgingSummary summary : agingSummaryList) {
			ppj.addBottomField("val_agentdet", summary.period, 0, y);
			ppj.addBottomField("val_agentdet",
					formatAgingBalance(p_df, summary.totalAmountDue), 350, y);
			ppj.addBottomField("val_agentdet",
					formatAgingBalance(p_df, summary.invoiceAmount), 450, y);
			totalInvoiceAmount += summary.invoiceAmount;
			if(summary.beforeDate != null &&
					(currentPeriod == null ||
					 currentPeriod.beforeDate.before(summary.beforeDate))) {
				currentPeriod = summary;
			}
			y += 20;
		}
		ppj.addBottomField("val_agentdet", "_____________", 450, y);
		y += 20;
		ppj.addBottomField("val_agentdet", p_df.format(totalInvoiceAmount), 450, y);
		y += 40;
		String asAt = DateUtil.dateToDateTimeStr(p_statementEndDate, "yy/MM/dd");
		double totalAmountDue = currentPeriod == null ? 0.0 : currentPeriod.totalAmountDue;
		ppj.addBottomField("val_agentdet", "Total amount due as at "+asAt, 0, y);
		ppj.addBottomField("val_agentdet", "HKD", 280, y);
		ppj.addBottomField("val_agentdet", p_df.format(totalAmountDue), 350, y);
		ppj.addBottomField("val_agentdet", "=============", 350, y+20);
	}

	@Override
	protected boolean skipPrint() {
		arBr = (BiResultArApStatement) br;
		vcode = br.getCellString("vd_vcode");
		vIdx=arBr.getVindexes(vcode);
		if(vIdx == null) return(true); else return(false);
	}
	
	void printOneSih (
		String sno,
		String module,
		String type,
		String frxno,
		String vcode,
		Date date,
		double losbal
		) throws Exception {
		if(losbal == 0.0) return;
		addInvoiceToAgingSummary(date, losbal);
		ppj.addDetailRecord();
		ppj.addDetailRecordField("dvinvoice", sno,0,0);
		if(module.equals("AP")) {
			if(type.equals("D")) {
				ppj.addDetailRecordField("dvdesc", "Debit Note",0,0);
				ppj.addDetailRecordField("dvdebit",
						dfd.format(losbal)
						,0,0);
			} else {
				ppj.addDetailRecordField("dvdesc", "Invoice "+frxno,0,0);
				ppj.addDetailRecordField("dvcredit",
						dfd.format(-losbal)
						,0,0);
			}
		} else {
			if(type.equals("C")) {
				ppj.addDetailRecordField("dvdesc", "Credit Note",0,0);
				ppj.addDetailRecordField("dvcredit",
						dfd.format(-losbal)
						,0,0);
			} else {
				ppj.addDetailRecordField("dvdesc", "Invoice "+frxno,0,0);
				ppj.addDetailRecordField("dvdebit",
						dfd.format(losbal)
						,0,0);
			}
		}
		String tstr = DateUtil.dateToDateTimeStr(date,"yyyy-MMM-dd");
		ppj.addDetailRecordField("dvdate", tstr,0,0);	
		ppj.addDetailRecordField("dvccy", "HKD",0,0);	
	}
			
			
	@Override
	protected void printOneDoc() throws Exception {
		setupDefaultAgingSummaryList(br.getCellDate("stmt_edate"));
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
		/*
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
		*/
		if(vIdx.agingIdx >= 0) {
			for(int idx = vIdx.agingIdx;;idx++) {
				BiResultArApStatement.SihRecord sih = arBr.getAgingSih(idx);
				if(sih == null) break;
				if(!vcode.equals(sih.vcode)) break;
				printOneSih(
						sih.sno,
						sih.module,
						sih.type,
						sih.frxno,
						sih.vcode,
						sih.date,
						sih.losbal
						);
			}
		}
		BiCellCollection bc;
		if(vIdx.sihIdx >= 0) {
			for(int i=vIdx.sihIdx;;i++) {
				bc = arBr.getSihRec(i);
				if(bc == null) break;
				if(!vcode.equals(bc.getCellString("sih_vcode"))) break;
				/*
				ppj.addDetailRecord();
				ppj.addDetailRecordField("dvinvoice", bc.getCellString("sih_frxno"),0,0);
				ppj.addDetailRecordField("dvdesc", "本月訂單",0,0);
				String module = bc.getCellString("sih_module");
				ppj.addDetailRecordField("dvdebit",
					df.format(bc.getCellDouble("sih_losbal"))
					,0,0);
				String tstr = DateUtil.dateToDateTimeStr(bc.getCellDate("sih_date"),"yyyy-MMM-dd");
				ppj.addDetailRecordField("dvdate", tstr,0,0);
				*/
				printOneSih(
						bc.getCellString("sih_sno"),
						bc.getCellString("sih_module"),
						bc.getCellString("sih_type"),
						bc.getCellString("sih_frxno"),
						bc.getCellString("sih_vcode"),
						bc.getCellDate("sih_date"),
						bc.getCellDouble("sih_losbal")
						);
			}
		}
		/*
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
		*/
		printAgingSummary(df, br.getCellDate("stmt_edate"));
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
