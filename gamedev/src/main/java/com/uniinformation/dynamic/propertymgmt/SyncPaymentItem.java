package com.uniinformation.dynamic.propertymgmt;

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

public class SyncPaymentItem implements JxActionListener {
	@Override
	public void actionPerformed(JxField field) {
			JxZkBiBase jxf = (JxZkBiBase) field.getJxForm();
			BiResultPayment br = (BiResultPayment) jxf.getBr();
			br.syncPayItemFromPayUnit(jxf);
	}

}
