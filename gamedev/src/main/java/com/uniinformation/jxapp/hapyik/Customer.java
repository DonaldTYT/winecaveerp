package com.uniinformation.jxapp.hapyik;

import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxSelOpt;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;

public class Customer extends com.uniinformation.jxapp.erpv4.Customer {
//	TrGetItemProperty tgipiSelPresetStr;
//	JxActionListener presetStrActionListener;
	@Override 
	public void afterBind() {
		super.afterBind();
		new JxFieldAction("vd_bstype vd_bsnature vd_addr2 vd_addr3" ) {
			public void actionPerformed(JxField fd){
				if(fd.getActionType() == JxField.ACTIONTYPE_PICKINPUTOPENED) {
					ZkJxPickInput zkpi = (ZkJxPickInput) fd.getNativeObject();
					jxZkBiPickPresetString((ColumnCell) getBr().getCell(fd.getName()),zkpi,null);
				}
			}
		};			
	}
}
