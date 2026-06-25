package com.uniinformation.jxapp.erpv4;

import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.ZkJxPickInput;

public class Supplier extends Vendor {
	public void afterBind() {
		super.afterBind();
		new JxFieldAction("vd_addr2" ) {
			public void actionPerformed(JxField fd){
				if(fd.getActionType() == JxField.ACTIONTYPE_PICKINPUTOPENED) {
					ZkJxPickInput zkpi = (ZkJxPickInput) fd.getNativeObject();
					jxZkBiPickPresetString((ColumnCell) getBr().getCell(fd.getName()),zkpi,null);
				}
			}
		};			
		new JxFieldAction("vd_addr3" ) {
			public void actionPerformed(JxField fd){
				if(fd.getActionType() == JxField.ACTIONTYPE_PICKINPUTOPENED) {
					ZkJxPickInput zkpi = (ZkJxPickInput) fd.getNativeObject();
					jxZkBiPickPresetString((ColumnCell) getBr().getCell(fd.getName()),zkpi,null,
							" and (pstd_subcond = '' or pstd_subcond = '" + getBr().getCellString("vd_addr2")
							+ "') "
							);
				}
			}
		};			
		new JxFieldAction("vd_country" ) {
			public void actionPerformed(JxField fd){
				if(fd.getActionType() == JxField.ACTIONTYPE_PICKINPUTOPENED) {
					ZkJxPickInput zkpi = (ZkJxPickInput) fd.getNativeObject();
					jxZkBiPickPresetString((ColumnCell) getBr().getCell(fd.getName()),zkpi,null);
				}
			}
		};			
	}
}
