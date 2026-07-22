package com.uniinformation.dynamic.winecave;

import java.text.DecimalFormat;
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
		Date duedate,
		double ltotal,
		double losbal
		) throws Exception {
		if(losbal == 0.0) return;
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
						sih.duedate,
						sih.ltotal,
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
						bc.getCellDate("sih_duedate"),
						bc.getCellDouble("sih_ltotal"),
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
