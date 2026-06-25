package com.uniinformation.jxapp.erpv4;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueue;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Image;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.GenbucketUtil;
import com.uniinformation.erpv4.DeviceControl;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiCellValueMapper;
import com.uniinformation.zkf.ZkForm;

public class MoPos extends MO {
//	String barcodeDevId = "BCN02";
//	String barcodeDevId = "BC01";
	protected ZkForm setBarcodeForm = null;
//	protected String fromLoc = null;
//	protected String toLoc = null;
	BiCellCollection setBarcodeRow = null;
	@Override
	public void onBarcode(String p_barcode) {
				try {
					String s = p_barcode.replaceAll("\\s+", "");
					SelectUtil su = getBr().getSelectUtil();
					TableRec tr = su.getQueryResult("select * from stock where st_icode = ?", new Wherecl().appendArgument(s));
					if(tr.getRecordCount() <= 0) {
						tr = su.getQueryResult("select * from stock where st_barcode = ?", new Wherecl().appendArgument(s));
					}
					if(tr.getRecordCount() <= 0) throw new CellException("Record Not In Stock");
					/* addOneTransferDetail(irg,org);*/
//					addOneMoDetail(tr.getFieldInt("st_irg"),Erpv4Config.useWeightedAverageOrg(sessionHelper) ? GenbucketUtil.WEIGHTED_AVERAGE_ORG : 0,1.0, tr.getFieldString("st_unit"));
					addOneMoDetail(tr.getFieldInt("st_irg"),Erpv4Config.useWeightedAverageOrg(sessionHelper) ? Erpv4Config.getCoWtAvOrg(getSessionHelper(), getBr().getCellString("stm_cocode")) : 0,1.0, tr.getFieldString("st_unit"));
					JxField jl = jxAdd("bt_gotolast");
					if(jl != null) {
						Button bt = (Button) jl.getNativeObject();
						Events.echoEvent("onClick", bt, null);
					}
				} catch (Exception ex) {
					UniLog.log("barcode invalid " + ex.toString() + "["+p_barcode+"]");
					if(ex instanceof CellException) ZkUtil.showMsg(ex.toString()); else {
						ZkUtil.showMsg("Barcode Invalid (0)");
						UniLog.log(ex);
					}
				}
					
	}

	/*
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		super.bindCellCollection(p_br, mode);
//		setDirtyFlag(true);
		try {
			if(fromLoc != null && p_br.getCell("stm_fromloc") != null) {
				p_br.getCell("stm_fromloc").set(fromLoc);
			}
			if(toLoc != null && p_br.getCell("stm_toloc") != null) {
				p_br.getCell("stm_toloc").set(toLoc);
			}
		} catch (CellException cex) {
			UniLog.log(cex);
		}
//		DeviceControl.attachListiner(getBarcodeId(),"BarcodeNotify");
//		DeviceControl.attachListiner(getBarcodeId(),eventQueId);
		
	}
	*/
	
	/*
	public void setDirtyFlag(boolean p_flag)
	{
		super.setDirtyFlag(p_flag);
		if(p_flag) {
			DeviceControl.attachListiner(getBarcodeId(),"BarcodeNotify");
		} else {
			DeviceControl.attachListiner(getBarcodeId(),null);
		}
	}
	*/
	
	@Override
	protected void detailCellChange(ColumnCell p_cc) {
		if(p_cc.getCellLabel().equals("stmd_setbarcode")) {
		if(p_cc.getCollection().getCellInt("stmd_irg") <= 0) {
			messageBox("Please Select Stock Item Before Set Barcode");
		} else {
        	setBarcodeForm = new ZkForm(null,"zkf/erpv4/setBarcode.zul");
	        final CellCollection col = new CellCollection();
	        try {
	        	setBarcodeRow = p_cc.getCollection();
	        	setBarcodeForm.doModal(col,new EventListener() {
	        			@Override
	        			public void onEvent(Event arg0) throws Exception {
									// TODO Auto-generated method stub
	        					setBarcodeRow = null;
	        					setBarcodeForm.exitModal();
						}
	        		}
	        	);
	        } catch (CellException cex) {
	        	UniLog.log(cex);
	        }
		}
		}
		super.detailCellChange(p_cc);
	}
	@Override
	protected void detailCellMapped(ColumnCell p_cc) {
		if(p_cc.getCellLabel().equals("stmd_setbarcode")) {
			ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) p_cc.getMapper();
			Button btn = (Button) zcvm.getComponent();
			btn.setImage("images/scanner.png");
		}
	}
	
}
