package com.uniinformation.dynamic.aw;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.BatchPrtdocHandler;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class PrintWipArrangement extends BatchPrtdocHandler {

	List<PrintingArrangement> paList;
	class PrintingArrangement {
		int wr_rg;
		String quoNum;
		int rev;
		String printDesc;
		String printMc;
		int shift;
		Date printDate;
		int printPriority;
		String color;
		String quantity;
		String sigcount;
		String customer;
		String imposition;
		String type;
		boolean outwork;
		boolean folding;
		boolean stitching;
		String binding;
		Date delidate;
		String checkcol;
		boolean matready;
		String imposition2;
	}
	public PrintWipArrangement(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		// TODO Auto-generated method stub
		ReturnMsg rtn = super.beforeAction(p_result, cnt);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		paList = new ArrayList<PrintingArrangement>();
		return(ReturnMsg.defaultOk);		
	}
	SimpleDateFormat sdf1;
	@Override
	public void print() throws Exception {
		PrintingArrangement pa = new PrintingArrangement();
		sdf1 = new SimpleDateFormat("yyyy/MM/dd");
		pa.wr_rg = br.getCellInt("wr_rg");
		pa.printDate = br.getCellDate("wr_startdate");
		pa.printMc = br.getCellString("wr_resource");
		pa.shift = br.getCellInt("wr_index0");
		pa.printPriority = br.getCellInt("wr_startseq");
		pa.quoNum = br.getCellString("inv_invno");
		pa.rev = br.getCellInt("jm_rev");
		pa.color = br.getCellString("wr_color");
		pa.type = br.getCellString("wr_name");
		pa.quantity = ""+br.getCellInt("wt_prtrun")+"+"+br.getCellInt("wt_wastage");
		pa.sigcount = ""+br.getCellInt("wt_signature");
		pa.customer = br.getCellString("vd_vname");
		int cc = br.getCellInt("wp_prtmethod");
		String optList[] = br.getColumnByLabel("wp_prtmethod").getOptionList(br,br.getCurrentCollection());
		if(optList != null && optList.length > cc) {
			pa.imposition = optList[cc];
		} else {
			pa.imposition = "";
		}
		pa.outwork = br.getCellBoolean("wr_flag0");
		pa.folding = br.getCellBoolean("wr_flag2");
		pa.stitching = br.getCellBoolean("wr_flag2");
		pa.binding = br.getCellString("wr_choice0");
		pa.delidate = br.getCellDate("jm_ddate");
		pa.checkcol = br.getCellString("wr_choice1");
		pa.matready = br.getCellBoolean("wr_choice2");
		pa.imposition2 = br.getCellString("wp_imposition");
		paList.add(pa);
	}

	String cocode;
	String docCode;
	String paperSize;
	@Override
	protected ReturnMsg initPrtdoc() {
		try {
			if(docCode == null) docCode = "PRTSHT01";
			if(cocode == null) cocode = Erpv4Config.getDefaultCoCode(sh);
			if(paperSize == null) paperSize = "A4L";
			ppj = PrtdocJson.newPrtdocJson(	
    				cocode,
    				paperSize,
    			    docCode,
    			    "erpv4_printDocument"
			);
			ppj.setTopLeftMargin(0);
			docCnt = 0;
			paList = new ArrayList<PrintingArrangement> ();
			//ppj.addHeaderImage("logo", Erpv4Config.getString(br.getSessionHelper(), "QuoBgImage"),0,0,0,800);    	
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return ReturnMsg.defaultOk;
		
	}
	
	void doPrint() throws Exception {
		Collections.sort(paList,new Comparator() {
			@Override
			public int compare(Object arg0, Object arg1) {
				int cc;
				PrintingArrangement pa0 = (PrintingArrangement) arg0;
				PrintingArrangement pa1 = (PrintingArrangement) arg1;
				cc = pa0.printMc.compareTo(pa1.printMc);
				if(cc != 0) return(cc);
				cc = pa0.printDate.compareTo(pa1.printDate);
				if(cc != 0) return(cc);
				// TODO Auto-generated method stub
				if(pa0.printPriority != pa1.printPriority) return(Integer.compare(pa0.printPriority, pa1.printPriority));
				cc = (pa0.quoNum.compareTo(pa1.quoNum));
				if(cc != 0) return(cc);
				return(Integer.compare(pa0.rev, pa1.rev));
			}
		});
		String mc = null;
		Date pd = null;
		for(PrintingArrangement pa : paList) {
			if(mc == null || !mc.equals(pa.printMc) || pd == null || !pd.equals(pa.printDate)) {
				UniLog.log("Printer One Page " + pa.printMc + " " +  pa.printDate + " " + pa.printPriority);
				
		        if(docCnt > 0) {
		        	ppj.newContent();
		        }
		        docCnt++;
		        mc = pa.printMc;
		        pd = pa.printDate;
				ppj.addHeaderField("doctitle","機房每日工作表");
				ppj.addHeaderField("docdate","日期:"+ sdf1.format(pd));
				ppj.addHeaderField("docresource","機號:"+ mc);
				ppj.addHeaderField("docshift","班次:"+(pa.shift == 0 ? "日更" : "夜更"));
				ppj.addDetailHeaderField("hdr_jobno", "Job No.");
				ppj.addDetailHeaderField("hdr_color", "顏色");
				ppj.addDetailHeaderField("hdr_quantity", "石數");
				ppj.addDetailHeaderField("hdr_sigcount", "手");
				ppj.addDetailHeaderField("hdr_sigcount", "數",0,20);
				ppj.addDetailHeaderField("hdr_type", "種類");
				ppj.addDetailHeaderField("hdr_customer", "客户名稱");
				ppj.addDetailHeaderField("hdr_imposition", "排版");
				ppj.addDetailHeaderField("hdr_outwork", "加");
				ppj.addDetailHeaderField("hdr_outwork", "工",0,20);

				/*
				 remove on 2025/12/13
				ppj.addDetailHeaderField("hdr_folding", "摺");
				ppj.addDetailHeaderField("hdr_stitching", "騎");
				ppj.addDetailHeaderField("hdr_stitching", "釘",0,20);
				*/
				ppj.addDetailHeaderField("hdr_folding", "排版方法");
				ppj.addDetailHeaderField("hdr_binding", "釘");
				ppj.addDetailHeaderField("hdr_binding", "裝",0,20);
				
				ppj.addDetailHeaderField("hdr_delidate", "交貨日期");
				ppj.addDetailHeaderField("hdr_checkdate", "睇色時間");
				ppj.addDetailHeaderField("hdr_matready", "已安");
				ppj.addDetailHeaderField("hdr_matready", "排紙",0,20);
			}
			ppj.addDetailRecord();
			ppj.addDetailRecordField("jobno", pa.quoNum+"-"+pa.rev);
			ppj.addDetailRecordField("color", pa.color);
			ppj.addDetailRecordField("type", pa.type);
			ppj.addDetailRecordField("quantity", pa.quantity);
			ppj.addDetailRecordField("sigcount", pa.sigcount);
			ppj.addDetailRecordField("customer", pa.customer);
			ppj.addDetailRecordField("imposition", pa.imposition);
			if(pa.outwork) ppj.addDetailRecordField("outwork", "Y");
			/*
			 remove on 2025/12/13
			if(pa.folding) ppj.addDetailRecordField("folding", "Y");
			if(pa.stitching) ppj.addDetailRecordField("stitching", "Y");
			*/
			ppj.addDetailRecordField("binding", pa.binding);
			ppj.addDetailRecordField("folding", pa.imposition2);
			ppj.addDetailRecordField("delidate", " "+DateUtil.toDateString(pa.delidate,"dd/mm"));
			ppj.addDetailRecordField("checkdate", " "+pa.checkcol);
			if(pa.matready) ppj.addDetailRecordField("matready", "Y");
			
				
			UniLog.log("Printer One Rec " + pa.printMc + " " +  pa.printDate + " " + pa.printPriority + " " + pa.wr_rg);
		}
	}

	@Override
	protected String getDocumentName(BiResult p_br) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public ReturnMsg afterAction(BiResult br) {
		try {
			doPrint();
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return(super.afterAction(br));
	}
	@Override
	public void afterActionAsync(BiActionHandler.AfterActionCallback cb) {
		try {
			doPrint();
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		super.afterActionAsync(cb);
	}
}
