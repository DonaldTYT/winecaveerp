package com.uniinformation.dynamic.propertymgmt;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.utils.UrlUtils;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.BatchPrtdocHandler;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class PrintPaymentNotice extends BatchPrtdocHandler {
	
	public PrintPaymentNotice(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		skipFetch = true;
	}

	class CarryForwardRec{
		String fromMonth;
		String toMonth;
		String propUnit;
		double mgtUnPaid;
		double resUnPaid;
	}
	class NoticeRec {
		String propUnit;
		String fromMonth;
		String toMonth;
		double mgtFeePerMonth;
		double resFeePerMonth;
		double numOfMonth;
		double mgtUnPaid;
		double resUnPaid;
		String getKey() {
			return(propUnit);
		}
	}
	
	ArrayList<NoticeRec> noticeRecList; 
	CarryForwardRec carryForwardRec; 
//	ArrayList<String,NoticeRec> noticeRecList = new ArrayList<String,NoticeRec>
	
	
	int getAggregateIndex(BiResult p_result,String p_col) {
		int hSize = p_result.aggregateOrPivotSize();
		for(int j=0;j<hSize;j++) {
			if(p_col.equals(p_result.getAggregateOrPivotHeader().getAggregate(j).getKey())) {
				return(j);
			}
		}
		return(-1);
	}

	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		batchDownloadReport = true;
		ReturnMsg rtn = super.beforeAction(p_result, cnt);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		noticeRecList = new ArrayList<NoticeRec>();
		carryForwardRec = null;
		return(ReturnMsg.defaultOk);		
	}
	
	private ReturnMsg afterAction1() {
		try {
			if(noticeRecList.size() > 0 ) {
				print();
				noticeRecList = new ArrayList<NoticeRec>();
				carryForwardRec = null;
			};
			return ReturnMsg.defaultOk;
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
	}


	@Override
	public ReturnMsg afterAction(BiResult p_br) {
		ReturnMsg rtn = afterAction1();
		if (rtn.getStatus())
			return(super.afterAction(p_br));
		return rtn;
	}

	@Override
	public void afterActionAsync(BiActionHandler.AfterActionCallback cb) {
		ReturnMsg rtn = afterAction1();
		if (rtn.getStatus())
			super.afterActionAsync(cb);
		cb.callback(rtn);
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		try {
			br = p_result;

			int idx_numOfMonth = getAggregateIndex(p_result,"COUNT()");
			int idx_mgtUnPaid = getAggregateIndex(p_result,"vcol_mgtunpaid");
			int idx_resUnPaid = getAggregateIndex(p_result,"vcol_resunpaid");
			int idx_fromMonth = getAggregateIndex(p_result,"mpy_month");
			int idx_toMonth = getAggregateIndex(p_result,"mpy_month2");
			Object[] eggValues = p_result.getAggregateValues(p_recIdx);
			NoticeRec nrec = new NoticeRec();
			nrec.propUnit  = p_result.getCellString("mpy_propertyunit");
			nrec.mgtFeePerMonth = p_result.getCellDouble("mpy_mgtfee");
			nrec.resFeePerMonth = p_result.getCellDouble("mpy_resfee");
			nrec.fromMonth = (String) eggValues[idx_fromMonth];
			nrec.toMonth = (String) eggValues[idx_toMonth];
			nrec.mgtUnPaid = (Double) eggValues[idx_mgtUnPaid];
			nrec.resUnPaid = (Double) eggValues[idx_resUnPaid];
			nrec.numOfMonth = (Double)eggValues[idx_numOfMonth];
			if(noticeRecList.size() > 0 && !noticeRecList.get(0).getKey().equals(nrec.getKey())) {
				print();
				noticeRecList = new ArrayList<NoticeRec>();
				carryForwardRec = null;
			} 
			noticeRecList.add(nrec);
			if(carryForwardRec == null) {
				carryForwardRec = new CarryForwardRec();
				carryForwardRec.propUnit = nrec.propUnit;
			}
			if(carryForwardRec.fromMonth == null || carryForwardRec.fromMonth.compareTo(nrec.fromMonth) > 0) {
				carryForwardRec.fromMonth = nrec.fromMonth;
			}
			if(carryForwardRec.toMonth == null || carryForwardRec.toMonth.compareTo(nrec.toMonth) < 0) {
				carryForwardRec.toMonth = nrec.toMonth;
			}
			return(ReturnMsg.defaultOk);
		} catch (Exception ex) {
			UniLog.log(ex);
			//return(new ReturnMsg(false,"Print Invoice" + result.getCellString("inv_invno") + " Failed " ));
			return(new ReturnMsg(false,String.format(sh.getLabel("Print Document %d Failed"), p_recIdx)));
		}
		
		
		
		
		// TODO Auto-generated method stub
	}

	@Override
	public void print() throws Exception {
		if(docCnt > 0) {
        	if(docCnt > 0) ppj.newContent();
		}
        docCnt++;	

    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
    	DecimalFormat df = new DecimalFormat("$#,##0.00");
        String cocode = Erpv4Config.getDefaultCoCode(sh);
        int lcrg = Erpv4Config.getDefaultLcrg(sh);
        Map<String, Object> coMap = Erpv4Config.getCoFieldMap(sh, cocode);
        Map<String, Object> lcMap = Erpv4Config.getLcFieldMap(sh, lcrg);

    	ppj.setTrailerAtLastPageOnly(false);
    	ppj.addHeaderField("companyZhName", (String)coMap.get("co_coname"));
    	ppj.addHeaderField("companyEnName", (String)coMap.get("co_chnname"));
    	ppj.addHeaderField("companyAddress", Erpv4Config.getCoAddr(sh, cocode));
    	ppj.addHeaderField("companyPhone", String.format("電話 TEL: %s       傳真 FAX: %s       准照編號 LIC.: %s", coMap.get("co_telnum"), coMap.get("co_faxnum"), coMap.get("co_license")));
//    	ppj.addHeaderField("docBarcode", "12345abcdef");
    	
//    	ppj.addHeaderField("docBarcode", "www.erpv4.com/qsdoc?agent=propmgmt001&propname=xyz12345wijewl&block=05&floor=24&unit=A");
    	ppj.addHeaderField("title", "繳費通知單 Payment Notice");

		if (noticeRecList.size() <= 0) return;

		TableRec tr = br.getSelectUtil().getQueryResult("select * from property where key_a = ?",new Wherecl().appendArgument(noticeRecList.get(0).propUnit));
		if (tr.getRecordCount() > 0) {
			tr.setRecPointer(0);
			Map<String, Object> m = Erpv4Config.getLcFieldMap(sh, tr.getFieldString("col_b"));
			if ((int)m.get("lc_epayment") > 0) {
				ppj.addHeaderField("docBarcode", 
					UrlUtils.buildURLWithParams("https://www.erpv4.com/qrdecode", "agid","pgmt0001","punit",noticeRecList.get(0).propUnit)
				);
			}
		}

        String unitOwner = "";
        String unitTel = "";
        String unitContact = "";
		tr = br.getSelectUtil().getQueryResult("select * from property where key_a = ?",new Wherecl().appendArgument(carryForwardRec.propUnit));
		if (tr.getRecordCount() > 0) {
			tr.setRecPointer(0);
        	unitOwner = tr.getFieldString("col_h");
        	unitTel = tr.getFieldString("col_l");
        	unitContact = tr.getFieldString("col_k");
		}

    	ppj.addHeaderField("monthRange", String.format("繳費月份 Payment Month %s 至 %s", carryForwardRec.fromMonth, carryForwardRec.toMonth));
    	ppj.addHeaderField("bkLabel", "聯絡人Contact:");
    	ppj.addHeaderField("bkValue", unitContact, 100, 0);
    	/*
    	ppj.addHeaderField("bkLabel", "業權人 Owner:");
    	ppj.addHeaderField("bkValue", unitOwner, 100, 0);
    	ppj.addHeaderField("bkLabel", "電話 Tel:", 220, 0);
    	ppj.addHeaderField("bkValue", unitTel, 285, 0);
    	*/
    	ppj.addHeaderField("bkLabel", "發單日期 Issue Date:", 600 - 100 - 150, 0);
    	ppj.addHeaderField("bkValue100Right", " " + sdf.format(new Date()), 600 - 150, 2);

    	int offset = 0;
    	ppj.addHeaderField("hdr80", "#", 0, 15);
    	offset += 80;
    	ppj.addHeaderField("hdr230", "繳費單位", offset, 5);
    	ppj.addHeaderField("hdr230", "Property Unit", offset, 25);
    	offset += 230;
    	ppj.addHeaderField("hdr80", "繳費由月份", offset, 5);
    	ppj.addHeaderField("hdr80", "Month from", offset, 25);
    	offset += 80;
    	ppj.addHeaderField("hdr80", "繳費至月份", offset, 5);
    	ppj.addHeaderField("hdr80", "Month to", offset, 25);
    	offset += 80;
    	ppj.addHeaderField("hdr80", "月數", offset, 5);
    	ppj.addHeaderField("hdr80", "No of mon.", offset, 25);
    	offset += 80;
    	ppj.addHeaderField("hdr100", "每月金額", offset, 5);
    	ppj.addHeaderField("hdr100", "Monthly fee", offset, 25);
    	offset += 100;
    	ppj.addHeaderField("hdr110", "合計", offset, 5);
    	ppj.addHeaderField("hdr110", "Total amount", offset, 25);

   		List<Map<String, Object>> list = new ArrayList<>();
    	{
    		carryForwardRec.mgtUnPaid = 0.0;
    		carryForwardRec.resUnPaid = 0.0;
    		double d;
    		/*tr = br.getSelectUtil().getQueryResult(
    				"select sum(mpy_mgtfee) mgtfee,sum(mpy_resfee) resfee from monthpayment join contract on contract.col_a = mpy_propertyname and contract.col_h = 'Y' where mpy_propertyunit = ? and mpy_month < ?",
    				new Wherecl().appendArgument(carryForwardRec.propUnit).appendArgument(carryForwardRec.fromMonth)
    				);
    		tr.setRecPointer(0);
    		d = tr.getFieldDouble("mgtfee");
    		if(!Double.isNaN(d)) carryForwardRec.mgtUnPaid += d;
    		d = tr.getFieldDouble("resfee");
    		if(!Double.isNaN(d)) carryForwardRec.resUnPaid += d;
    		
    		tr = br.getSelectUtil().getQueryResult(
    				"select sum(payitem.col_e) mgtfee,sum(payitem.col_f) resfee from monthpayment join payitem on payitem.col_c = mpy_propertyunit and payitem.col_d = mpy_month join contract on contract.col_a = mpy_propertyname and contract.col_h = 'Y' where mpy_propertyunit = ? and mpy_month < ?",
    				new Wherecl().appendArgument(carryForwardRec.propUnit).appendArgument(carryForwardRec.fromMonth)
    				);
    		tr.setRecPointer(0);
    		d = tr.getFieldDouble("mgtfee");
    		if(!Double.isNaN(d)) carryForwardRec.mgtUnPaid -= d;
    		d = tr.getFieldDouble("resfee");
    		if(!Double.isNaN(d)) carryForwardRec.resUnPaid -= d;*/
    		tr = br.getSelectUtil().getQueryResult(
    				"select mpy_mgtfee mgtfee, mpy_resfee resfee, mpy_month month from monthpayment join contract on contract.col_a = mpy_propertyname and contract.col_h = 'Y' and STR_TO_DATE(CONCAT(mpy_month, '-01'), '%Y-%m-%d') >= contract.col_c and LAST_DAY(STR_TO_DATE(CONCAT(mpy_month, '-01'), '%Y-%m-%d')) <= contract.col_d where mpy_propertyunit = ? and mpy_month < ? order by month",
    				new Wherecl().appendArgument(carryForwardRec.propUnit).appendArgument(carryForwardRec.fromMonth)
    				);
    		for (int i = 0; i < tr.getRecordCount(); i++) {
    			tr.setRecPointer(i);
    			String month = tr.getFieldString("month");
    			d = tr.getFieldDouble("mgtfee");
    			if (!Double.isNaN(d)) 
    				list.add(MapUtil.of("startMonth", month, "endMonth", month, "monthCount", 1, "type", "管理費", "fee", d));
    			d = tr.getFieldDouble("resfee");
    			if (!Double.isNaN(d)) 
    				list.add(MapUtil.of("startMonth", month, "endMonth", month, "monthCount", 1, "type", "儲備金", "fee", d));
    		}

    		tr = br.getSelectUtil().getQueryResult(
    				"select payitem.col_e mgtfee, payitem.col_f resfee, mpy_month month from monthpayment join payitem on payitem.col_c = mpy_propertyunit and payitem.col_d = mpy_month join contract on contract.col_a = mpy_propertyname and contract.col_h = 'Y' and STR_TO_DATE(CONCAT(mpy_month, '-01'), '%Y-%m-%d') >= contract.col_c and LAST_DAY(STR_TO_DATE(CONCAT(mpy_month, '-01'), '%Y-%m-%d')) <= contract.col_d where mpy_propertyunit = ? and mpy_month < ?",
    				new Wherecl().appendArgument(carryForwardRec.propUnit).appendArgument(carryForwardRec.fromMonth)
    				);
    		for (int i = 0; i < tr.getRecordCount(); i++) {
    			tr.setRecPointer(i);
    			String month = tr.getFieldString("month");
    			final double d1 = tr.getFieldDouble("mgtfee");
    			if (!Double.isNaN(d1))
    				list.stream().filter(m -> StringUtils.equals((String)m.get("startMonth"), month) && StringUtils.equals((String)m.get("type"), "管理費")).findFirst()
   							.ifPresent(m -> m.put("fee", (Double)m.get("fee") - d1));
    			final double d2 = tr.getFieldDouble("resfee");
    			if (!Double.isNaN(d2))
    				list.stream().filter(m -> StringUtils.equals((String)m.get("startMonth"), month) && StringUtils.equals((String)m.get("type"), "儲備金")).findFirst()
   							.ifPresent(m -> m.put("fee", (Double)m.get("fee") - d1));
    		}

    		list.removeIf(m -> (Double)m.get("fee") <= 0);
    		SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");
    		for (int i = 0; i < list.size(); ) {
    			Map<String, Object> m1 = list.get(i);
    			Date date1 = sdf1.parse((String)m1.get("endMonth"));
    			int j = i + 1;
    			for (; j < list.size(); j++) {
    				Map<String, Object> m2 = list.get(j);
    				Date date2 = sdf1.parse((String)m2.get("endMonth"));
    				if (m1.get("type").equals(m2.get("type")) && date1.compareTo(DateUtil.prevMonthStart(date2)) == 0) {
    					m1.put("endMonth", m2.get("endMonth"));
    					m1.put("monthCount", (Integer)m1.get("monthCount") + 1);
    					m1.put("fee", (Double)m1.get("fee") + (Double)m2.get("fee"));
    					list.remove(j);
    					break;
    				}
    			}
    			if (j == list.size())
    				i++;
    		}
    		carryForwardRec.mgtUnPaid = list.stream().filter(m -> m.get("type").equals("管理費")).mapToDouble(m -> (Double)m.get("fee")).sum();
    		carryForwardRec.resUnPaid = list.stream().filter(m -> m.get("type").equals("儲備金")).mapToDouble(m -> (Double)m.get("fee")).sum();
    	}
//		carryForwardRec.mgtUnPaid = 100.0; /* will add code to select the actual unpaid later */
//		carryForwardRec.resUnPaid = 200.0; /* will add code to select the actual unpaid later */
		
		UniLog.log("Print Notice for " + noticeRecList.get(0).propUnit + " from " + carryForwardRec.fromMonth + ":" + carryForwardRec.toMonth);

		ppj.addDetailRecord();
		ppj.addDetailRecordField("detLeftBold", "未繳交之費用 Unpaid fees:");
		NoticeRec nr = noticeRecList.get(0);
		/*if (carryForwardRec.mgtUnPaid > 0)
			addDetailRow(0, "管理費", nr.propUnit, "", "", "", "", df.format(carryForwardRec.mgtUnPaid));
		if (carryForwardRec.resUnPaid > 0)
			addDetailRow(0, "儲備金", nr.propUnit, "", "", "", "", df.format(carryForwardRec.resUnPaid));*/
		for (Map<String, Object> m : list)
			addDetailRow(0, (String)m.get("type"), nr.propUnit, (String)m.get("startMonth"), (String)m.get("endMonth"), String.valueOf(m.get("monthCount")), "", df.format(m.get("fee")));
		addDetailRow(0, "", "", "", "", "", "合共 (MOP): ", df.format(carryForwardRec.mgtUnPaid + carryForwardRec.resUnPaid));
		setDetailRow(-18, "", "", "", "", "", "", underlineBlank());
		setDetailRow(4, "", "", "", "", "", "", underlineBlank());
		setDetailRow(6, "", "", "", "", "", "", underlineBlank());

		ppj.addDetailRecord();
		ppj.addDetailRecordField("detLeftBold", "本期之費用 Fees for this period:");
		double totalUnPaid = 0;
		for (NoticeRec nrec : noticeRecList) {
			if (nrec.mgtFeePerMonth > 0) {
				UniLog.log("Print one notice Mgt " + nrec.propUnit + "," + nrec.numOfMonth + "," + nrec.fromMonth + ":" + nrec.toMonth + "," + nrec.mgtFeePerMonth + " " + nrec.mgtUnPaid);
				double totalMgt = nrec.mgtFeePerMonth * nrec.numOfMonth;
				double paidMgt = totalMgt - nrec.mgtUnPaid;
				addDetailRow(0, "管理費", nrec.propUnit, 
						nrec.fromMonth, nrec.toMonth, 
						String.valueOf((int)nrec.numOfMonth),
						df.format(nrec.mgtFeePerMonth),
						df.format(totalMgt));
				if (paidMgt != 0)
					addDetailRow(0, "", "", "", "", "", "已繳", df.format(paidMgt));
				totalUnPaid += nrec.mgtUnPaid;
			}
			if (nrec.resFeePerMonth > 0) {
				UniLog.log("Print one notice Res " + nrec.propUnit + "," + nrec.numOfMonth + "," + nrec.fromMonth + ":" + nrec.toMonth + "," + nrec.resFeePerMonth + " " + nrec.resUnPaid);
				double totalRes = nrec.resFeePerMonth * nrec.numOfMonth;
				double paidRes = totalRes - nrec.resUnPaid;
				addDetailRow(0, "儲備金", nrec.propUnit, 
						nrec.fromMonth, nrec.toMonth, 
						String.valueOf((int)nrec.numOfMonth),
						df.format(nrec.resFeePerMonth),
						df.format(totalRes));
				if (paidRes != 0)
					addDetailRow(0, "", "", "", "", "", "已繳", df.format(paidRes));
				totalUnPaid += nrec.resUnPaid;
			}
		}
		addDetailRow(0, "", "", "", "", "", "合共 (MOP): ", df.format(totalUnPaid));
		setDetailRow(-18, "", "", "", "", "", "", underlineBlank());
		setDetailRow(4, "", "", "", "", "", "", underlineBlank());
		setDetailRow(6, "", "", "", "", "", "", underlineBlank());

		if(carryForwardRec != null) {
			addDetailRow(0, "", "", "", "", "", "總額 (MOP): ", df.format(
						totalUnPaid+carryForwardRec.mgtUnPaid + carryForwardRec.resUnPaid
						));
			setDetailRow(4, "", "", "", "", "", "", underlineBlank());
			setDetailRow(6, "", "", "", "", "", "", underlineBlank());
		}

		ppj.addDetailRecord();
		ppj.addDetailRecordField("detLeftBold", "備註 Remark: ");
		String coPayment = (String)coMap.get("co_payment");
		String lcPayment = (String)lcMap.get("lc_payment");
		if (StringUtils.isNotBlank(coPayment)) {
			ppj.addDetailRecord();
			ppj.addDetailRecordField("detLeft", coPayment, 0, 0, 20, 760);
		}
		if (StringUtils.isNotBlank(lcPayment)) {
			ppj.addDetailRecord();
			ppj.addDetailRecordField("detLeft", lcPayment, 0, 0, 20, 760);
		}
	}
	
	private String underlineBlank() {
		return String.format("%cu                            %cu", ChnftrParser.ESC_CHAR, ChnftrParser.ESC_CHAR);
	}

	private void addDetailRow(int y, String...p) throws JSONException {
		ppj.addDetailRecord();
		setDetailRow(y, p);
	}
	private void setDetailRow(int y, String...p) throws JSONException {
		int offset = 0;
		ppj.addDetailRecordField("detLeft", p[0], offset, y);
		offset += 80;
		ppj.addDetailRecordField("detLeft", p[1], offset, y, 20, 230);
		offset += 230;
		ppj.addDetailRecordField("detLeft", p[2], offset, y);
		offset += 80;
		ppj.addDetailRecordField("detLeft", p[3], offset, y);
		offset += 80;
		ppj.addDetailRecordField("detCenter80", p[4], offset, y);
		offset += 80;
		ppj.addDetailRecordField("detRight100", p[5], offset, y);
		offset += 100;
		ppj.addDetailRecordField("detRight110", p[6], offset, y);
	}

	@Override
	protected ReturnMsg initPrtdoc() {
		try {
			String docCode = "GENINV03";
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			ppj = PrtdocJson.newPrtdocJson(	
    				cocode,
    				"A4P",
    			    docCode,
    			    "erpv4_printDocument"
			);
			ppj.setTopLeftMargin(0);
			docCnt = 0;
			//ppj.addHeaderField("doctitle","Quotation");
			//ppj.addHeaderImage("logo", Erpv4Config.getString(br.getSessionHelper(), "QuoBgImage"),0,0,0,800);    	
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return ReturnMsg.defaultOk;
	}

	@Override
	protected String getDocumentName(BiResult p_br) {
		// TODO Auto-generated method stub
		return "Payment Notice";
	}

}
