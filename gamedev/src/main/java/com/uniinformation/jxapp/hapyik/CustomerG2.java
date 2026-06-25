package com.uniinformation.jxapp.hapyik;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.ZkJxPickInput;

public class CustomerG2 extends com.uniinformation.jxapp.erpv4.CustomerG2 {
	protected boolean jxZkBiPickPresetString(ColumnCell p_bcc,ZkJxPickInput p_zkpi,String p_key) {
		return(jxZkBiPickPresetString(p_bcc,p_zkpi,p_key,null));
	}
	@Override 
	public void afterBind() {
		super.afterBind();
		new JxFieldChange("vd_bstype") {
			@Override
			public boolean valueChanged(JxField jxfield, String orgvalue) {
				ColumnCell c2 ;
				c2 = getBr().getCell("vd_bsnature");
				if(c2 != null) {
					c2.setItemList(jxZkBiGetPresetItemList(getBr(),c2,jxfield.getText()));
				}
				return true;
			}
			
		};

		new JxFieldChange("vd_addr1") {
			@Override
			public boolean valueChanged(JxField jxfield, String orgvalue) {
				ColumnCell c2 ;
				c2 = getBr().getCell("vd_addr2");
				if(c2 != null) {
					c2.setItemList(jxZkBiGetPresetItemList(getBr(),c2,jxfield.getText()));
				}
				return true;
			}
			
		};
		
		/*
		new JxFieldAction("vd_bstype vd_addr1" ) {
			public void actionPerformed(JxField fd){
				if(fd.getActionType() == JxField.ACTIONTYPE_PICKINPUTOPENED) {
					ZkJxPickInput zkpi = (ZkJxPickInput) fd.getNativeObject();
					jxZkBiPickPresetString((ColumnCell) getBr().getCell(fd.getName()),zkpi,null);
				}
			}
		};			
		new JxFieldAction("vd_bsnature" ) {
			public void actionPerformed(JxField fd){
				if(fd.getActionType() == JxField.ACTIONTYPE_PICKINPUTOPENED) {
					ZkJxPickInput zkpi = (ZkJxPickInput) fd.getNativeObject();
					jxZkBiPickPresetString((ColumnCell) getBr().getCell(fd.getName()),zkpi,null,
							" and (pstd_subcond = '' or pstd_subcond = '" + getBr().getCellString("vd_bstype") + "') "
							);
				}
			}
		};			
		new JxFieldAction("vd_addr2" ) {
			public void actionPerformed(JxField fd){
				if(fd.getActionType() == JxField.ACTIONTYPE_PICKINPUTOPENED) {
					ZkJxPickInput zkpi = (ZkJxPickInput) fd.getNativeObject();
					jxZkBiPickPresetString((ColumnCell) getBr().getCell(fd.getName()),zkpi,null,
							" and (pstd_subcond = '' or pstd_subcond = '" + getBr().getCellString("vd_addr1")
							+ "') "
							);
				}
			}
		};			
		*/
	}
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
		/*
		ColumnCell c1 ;
		c1 = p_br.getCell("vd_bstype");
		if(c1 != null) {
			c1.setItemList(jxZkBiGetPresetItemList(getBr(),c1,null));
			ColumnCell c2 ;
			c2 = p_br.getCell("vd_bsnature");
			if(c2 != null) {
				c2.setItemList(jxZkBiGetPresetItemList(getBr(),c2,c1.getString()));
			}
		}
		c1 = p_br.getCell("vd_addr1");
		if(c1 != null) {
			c1.setItemList(jxZkBiGetPresetItemList(getBr(),c1,null));
			ColumnCell c2 ;
			c2 = p_br.getCell("vd_addr2");
			if(c2 != null) {
				c2.setItemList(jxZkBiGetPresetItemList(getBr(),c2,c1.getString()));
			}
		}
		*/
	}
	
}
