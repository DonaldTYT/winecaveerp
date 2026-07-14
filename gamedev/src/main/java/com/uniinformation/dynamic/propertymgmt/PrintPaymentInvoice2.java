package com.uniinformation.dynamic.propertymgmt;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.utils.ChnftrBuilder.*;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class PrintPaymentInvoice2 extends PrintParkingTmpLocPaymentInvoice {
   	protected static SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM");

	public PrintPaymentInvoice2(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		ptp = new PrintTemplate();
	}

	public PrintPaymentInvoice2() {
		this(null);
	}

	@Override
	protected void print() throws Exception {
		ptp.startPrint();
		
		updatePrintCount();
		payItemList = br.getSubLinkResult("propertymgmt.payitem");
		
    	boolean paidMgtFee = br.getCellBoolean("col_n");
    	boolean paidResFee = br.getCellBoolean("col_o");
    	double discount = br.getCellDouble("col_q");
    	double actualFee = br.getCellDouble("vcol_actualfee");

    	List<PayItem> payItemList = new ArrayList<PayItem>();
    	PayItem lpi = null;
    	
    	String pmContact = null;
		for (BiCellCollection c : br.getSubLink("propertymgmt.payitem").getRowCollectionList()) {
			PayItem pi = new PayItem();
			pi.propUnit = c.getCellString("col_c");
			pi.startDate = pi.endDate = sdf.parse(c.getCellString("col_d") + "-01");
			pi.monthCount = 1;
			pi.mgtFee = paidMgtFee ? c.getCellDouble("col_e") : 0;
			pi.resFee = paidResFee ? c.getCellDouble("col_f") : 0;
			
			String ss = c.getCellString("pm_col_k");
			if(!StringUtils.isBlank(ss)) {
				if(pmContact == null) {
					pmContact = ss; 
				} else if(!pmContact.equals(ss)) {
					pmContact = "";
				} else {
					pmContact = ss;
				}
			}
			if (lpi == null || !StringUtils.equals(lpi.propUnit, pi.propUnit) 
					|| DateUtil.nextmonth(lpi.endDate).compareTo(pi.endDate) != 0 
					|| lpi.mgtFee != pi.mgtFee || lpi.resFee != pi.resFee) {
				payItemList.add(pi);
				lpi = pi;
			} else {
				lpi.endDate = pi.startDate;
				lpi.monthCount++;
			}
		}

		pageFee = 0;
		for (PayItem pi : payItemList) {
			if (paidMgtFee) {
				double fee = pi.mgtFee * pi.monthCount;
				ptp.buildAdjustPhrase(1, "管理費", pi.propUnit, sdf1.format(pi.startDate), 
							sdf1.format(pi.endDate), String.valueOf(pi.monthCount), 
							df.format(pi.mgtFee), df.format(fee));
				pageFee += fee;
			}
			if (paidResFee) {
				double fee = pi.resFee * pi.monthCount;
				ptp.buildAdjustPhrase(1, "儲備金", pi.propUnit, sdf1.format(pi.startDate), 
							sdf1.format(pi.endDate), String.valueOf(pi.monthCount), 
							df.format(pi.resFee), df.format(fee));
				pageFee += fee;
			}
		}
		if (discount != 0) {
			ptp.buildPhrase(1, "預繳優惠", "", "", "", "", "", "-" + df.format(discount));
			pageFee -= discount;
		}
		ptp.endPrint();
	}

	@Override
	protected String getDocumentName(BiResult p_br) {
		return "管理費及儲備金";
	}

	private static class PayItem {
		String propUnit;
		Date startDate;
		Date endDate;
		int monthCount;
		double mgtFee;
		double resFee;
	}
}
