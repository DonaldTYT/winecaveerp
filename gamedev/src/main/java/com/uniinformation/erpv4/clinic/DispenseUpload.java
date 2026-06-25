package com.uniinformation.erpv4.clinic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.poi.ExcelPoi;

public class DispenseUpload {
	
	java.util.Date defaultDate;
	
	public class DispenseDetail {
		public String drugName;
		public double quantity;
		public double cost;
		public double price;
		public String errNo;
	}
	
	public class DispenseRec {
		public String trxNo;
		public java.util.Date trDate;
		public String patient;
		public String doctor;
		public String account;
		public String result;
		public String errNo;
		public ArrayList<DispenseDetail> details;
		public DispenseRec() {
			details = new ArrayList<DispenseDetail>();
		}
	}
	

	LinkedHashMap <String,DispenseRec>dispenseHash;
	public DispenseUpload(ExcelPoi p_excel) throws Exception {
		dispenseHash = new LinkedHashMap<String,DispenseRec>();
		int rows = p_excel.getRowCount();
		int startRow = 0;
		java.util.Date trtDate = null;
		for(int i=0;i<rows;i++) {
			if(startRow <= 0) {
				String str1 = p_excel.getStringValue(i, 0);
				String str2 = p_excel.getStringValue(i, 1);
				if("Date".equals(str1))	{
					trtDate = p_excel.getDateValue(i, 1);
					UniLog.log("DispenseUpload Date = " + trtDate);
					if(defaultDate == null) defaultDate = trtDate;
				}
				if("Drug Name".equals(str1) &&
				   "Patient Code".equals(str2)
						) {
					/*
					if(trtDate == null) {
						throw (new Exception("Excel Data Error : Date Invalid"));
					}
					*/
					startRow = i+1;
				}
			} else {
				String trxNo = p_excel.getStringValue(i, 4);
				UniLog.log("line " + (i+1) + " trxNo " + trxNo);
				if(trxNo == null || StringUtils.isBlank(trxNo)) {
					trtDate = null;
					startRow = 0;
					continue;
				}
				DispenseRec dRec = dispenseHash.get(trxNo);
				if(dRec == null) {
					dRec = new DispenseRec();
					dRec.trxNo = trxNo;
					dRec.patient = p_excel.getStringValue(i, 1);
					dRec.doctor = p_excel.getStringValue(i, 5);
					dRec.account = p_excel.getStringValue(i, 6);
//					dRec.trDate = trtDate;
					dRec.trDate = p_excel.getDateValue(i, 3);
					if(dRec.trDate == null) {
						String ss = p_excel.getStringValue(i, 3);
						if(ss.trim().length() == 10) {
							dRec.trDate = DateUtil.getDate(ss.trim(), "dd/mm/yyyy");
						}
					}
					
					dispenseHash.put(trxNo, dRec);
					UniLog.log("DisprenseUpload add Tr " +  trxNo );
				}
				DispenseDetail det = new DispenseDetail();
				det.drugName = p_excel.getStringValue(i, 0);
				det.quantity = p_excel.getDoubleValue(i, 7);
				det.cost = p_excel.getDoubleValue(i, 8);
				det.price = p_excel.getDoubleValue(i, 9);
				dRec.details.add(det);
				UniLog.log("DisprenseUpload add Drug " +  det.drugName);
			}
		}
	}
	
	public java.util.Date getDefaultDate() {
		return(defaultDate);
	}

	public int getTrCount() {
		return(dispenseHash.size());
	}
	
	public Collection<DispenseRec> getTrans() {
		return(dispenseHash.values());
	}
}
