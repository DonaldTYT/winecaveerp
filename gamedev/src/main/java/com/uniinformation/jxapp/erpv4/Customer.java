package com.uniinformation.jxapp.erpv4;


import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;

public class Customer extends JxZkBiErpv4 {

	/*
	protected ReturnMsg beforeUpdate(BiResult br) {
		syncAddrToSvLoc(br);
		return(super.beforeUpdate(br));
	}
	protected ReturnMsg beforeAdd(BiResult br) {
		syncAddrToSvLoc(br);
		return(super.beforeAdd(br));
	}
	void syncAddrToSvLoc(BiResult br)
	{
		try {
			Vector<BiResult> v = br.getSubLinks();
			for(BiResult sr : v) {
				if(sr.getView().getTable().getName().equals("sv_loc")) {
					if(sr.getRowCount() <= 0) {
						CellCollection scol = sr.newRowCollection();
						ReturnMsg rtn = sr.addSubRecord(scol, 0,"");
						Object tr = rtn.getData();
						if(scol.testCell("svloc_sameasmain") != null)  {
							scol.getCell("svloc_sameasmain").set(true);
						}
						scol.getCell("svloc_desp").set(br.getCell("vd_vname"));
						scol.getCell("svloc_chndesp").set(br.getCell("vd_chnname"));
						scol.getCell("svloc_contact").set(br.getCell("vd_contact"));
						scol.getCell("svloc_tel").set(br.getCell("vd_tel"));
						scol.getCell("svloc_addr1").set(br.getCell("vd_addr0"));
						scol.getCell("svloc_addr2").set(br.getCell("vd_addr1"));
						scol.getCell("svloc_city").set(br.getCell("vd_addr2"));
						scol.getCell("svloc_state").set(br.getCell("vd_addr3"));
					} else {
					if(sr.getColumnByLabel("svloc_sameasmain") != null) {
						Vector<BiCellCollection> v2 = sr.getRowCollectionList();
						for(BiCellCollection scol : v2) {
							if(scol.getCell("svloc_sameasmain").getBoolean())  {
								scol.getCell("svloc_desp").set(br.getCell("vd_vname"));
								scol.getCell("svloc_chndesp").set(br.getCell("vd_chnname"));
								scol.getCell("svloc_contact").set(br.getCell("vd_contact"));
								scol.getCell("svloc_tel").set(br.getCell("vd_tel"));
								scol.getCell("svloc_addr1").set(br.getCell("vd_addr0"));
								scol.getCell("svloc_addr2").set(br.getCell("vd_addr1"));
								scol.getCell("svloc_city").set(br.getCell("vd_addr2"));
								scol.getCell("svloc_state").set(br.getCell("vd_addr3"));
							}
						}
					}
					}
				}
			}
		} catch (CellException cex) {
			UniLog.log(cex);
		}
	}
	*/
	@Override 
	public void afterBind() {
		super.afterBind();
		new JxFieldAction("vd_bstype vd_addr2" ) {
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
							" and (pstd_subcond = '' or pstd_subcond = '" + getBr().getCellString("vd_bstype")
							+ "') "
							);
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
	}
}
