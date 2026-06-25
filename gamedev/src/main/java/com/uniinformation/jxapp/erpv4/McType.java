package com.uniinformation.jxapp.erpv4;

import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.ZkJxPickInput;

public class McType extends JxZkBiErpv4{
//	@Override
//	public void bindCellCollection(BiResult br,int mode) {
//		super.bindCellCollection(br,mode);
//		try {
//			SelectUtil sr = br.getSelectUtil();
//			TableRec tr= sr.getQueryResult("select stu_unit from st_unit",null);
//			Vector uList = new Vector();
//			uList.add("");
//			for(int i=0;i<tr.getRecordCount();i++) {
//				tr.setRecPointer(i);
//				uList.add(tr.getFieldString("stu_unit"));
//				
//			}
//			br.getCell("mt_munit1").setItemList(uList);
//			br.getCell("mt_munit2").setItemList(uList);
//			br.getCell("mt_munit3").setItemList(uList);
//			br.getCell("mt_dunit1").setItemList(uList);
//			br.getCell("mt_dunit2").setItemList(uList);
//			br.getCell("mt_dunit3").setItemList(uList);
//		
//		} catch (Exception ex) {
//			UniLog.log(ex);
//		}
//
//	}
	

	@Override 
	public void afterBind() {
		super.afterBind();
		new JxFieldAction("mc_subcatlevel1" ) {
			public void actionPerformed(JxField fd){
				if(fd.getActionType() == JxField.ACTIONTYPE_PICKINPUTOPENED) {
					ZkJxPickInput zkpi = (ZkJxPickInput) fd.getNativeObject();
					jxZkBiPickPresetString((ColumnCell) getBr().getCell(fd.getName()),zkpi,null);
				}
			}

		};			
	}
}
