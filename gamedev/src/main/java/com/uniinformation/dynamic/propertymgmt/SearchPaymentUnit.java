package com.uniinformation.dynamic.propertymgmt;

import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.propertymgmt.BiResultPayment;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;

public class SearchPaymentUnit implements JxActionListener {
	@Override
	public void actionPerformed(JxField field) {
		// TODO Auto-generated method stub
			JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
			BiResultPayment br = (BiResultPayment) jxf.getBr();
			Wherecl wherecl = null;
			String ss = br.getCellString("col_d");
			if(!StringUtils.isEmpty(ss)) {
				if(wherecl == null) wherecl = new Wherecl();
				wherecl.andUniop("property.col_h", "=", ss);
			}
			ss = br.getCellString("col_e");
			if(!StringUtils.isEmpty(ss)) {
				if(wherecl == null) wherecl = new Wherecl();
				wherecl.andUniop("property.col_l", "like", "%"+ss.trim()+"%");
			}
			if(wherecl != null) {
				try {
					br.payUnitClear();
					wherecl.andUniop("col_b", "=" , br.getCellString("col_c"));
					SelectUtil su = br.getSelectUtil();
					TableRec tr = su.getQueryResult("select * from property ", wherecl);
					for(int i=0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						if(!br.checkOrUnDeleteIfExist(tr.getFieldString("key_a"), jxf)) {
							br.payUnitAdd(tr.getFieldString("key_a"),null,0.0,0.0);
						}
						UniLog.log("HAHA " + tr.getFieldString("key_a"));
					}
					br.addPayUnitToList(jxf);
				} catch (Exception ex) {
					UniLog.log(ex);
				}
			}
			
			
			/*
			if(contractFee == null) {
				contractFee = br.getView().getSchema().getViewByName("propertymgmt.contractfee").newBiResult(br.getSessionHelper().getLoginId(), null, null, br.getSessionHelper());
			}
			String ss = br.getCellString("col_d");
			Wherecl wherecl = null;
			if(!StringUtils.isEmpty(ss)) {
				if(wherecl == null) wherecl = new Wherecl();
				wherecl.andUniop("property.col_h", "=", ss);
			}
			if(wherecl == null) {
				return;
			} else {
				Wherecl wcl = new Wherecl().appendString(" contractfee.col_c in (select key_a from property where " + wherecl.stripAnd().getWhereString() + ")");
				Vector vals = wherecl.getValues();
				for(Object arg : vals) {
					wcl.appendArgument(arg);
				}
				contractFee.appendWherecl(wcl);
				contractFee.query();
				for(int i=0;i<contractFee.getRowCount();i++) {
					contractFee.loadOneRecV(i);
					UniLog.log("HAHA " + contractFee.getCellString(""));
				}
			}
			*/
			/*
			BiResult sr = contractFee;
			String ss = br.getCellString("col_d");
			Wherecl wherecl = null;
			if(!StringUtils.isEmpty(ss)) {
				if(wherecl == null) wherecl = new Wherecl();
				wherecl.andUniop("property.col_h", "=", ss);
			}
			if(wherecl == null) {
				br.clearOneSublink(br.getCurrentCollection(), sr);
			} else {
				Wherecl wcl = new Wherecl().appendString(" contractfee.col_c in (select key_a from property where " + wherecl.stripAnd().getWhereString() + ")");
				Vector vals = wherecl.getValues();
				for(Object arg : vals) {
					wcl.appendArgument(arg);
				}
				br.fetchOneSubLink(br.getCurrentCollection(), sr, wcl);
			}
			JxField sv = jxf.jxAdd("list_"+jxf.replaceViewName(sr.getView().getName()));
			jxf.bindSublinkList(sv , sr);
			*/
	}

}
