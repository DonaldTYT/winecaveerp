package com.uniinformation.jxapp.erpv4;

import java.util.Date;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultCrhAr;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.AccApi;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;

public class Crh extends JxZkBiBase {
	String crdViewId = null;
	String craViewId = null;
	
	public ReturnMsg crh_add() {
		SelectUtil su = getBr().getSelectUtil();
		RpcClient rpc = su.getRpcClient();
		ReturnMsg rtnMsg;
		BiResult sr = getBr().getSubLink(crdViewId);
		BiResult sr2 = getBr().getSubLink(craViewId);
		Vector <BiCellCollection> cv = sr.getRowCollectionList();
		for(BiCellCollection c : cv) {
			if(StringUtils.isBlank(c.getCellString("crd_sno"))) {
				return(new ReturnMsg(false,"Paid Invoice No. Should not be Blank"));
			}
		}
		cv = sr2.getRowCollectionList();
		for(BiCellCollection c : cv) {
			if(StringUtils.isBlank(c.getCellString("cra_inputano"))) {
				return(new ReturnMsg(false,"Account Code Should not be Blank"));
			}
		}
		rtnMsg = AccApi.crh_add_start(
			rpc,
			getBr().getCell("crh_module").getString(),
			getBr().getCell("crh_crno").getString(),
			getBr().getCell("crh_date").getDate(),
			getBr().getCell("crh_vcode").getString(),
			getBr().getCell("crh_check").getString(),
			getBr().getCell("crh_bank").getString(),
			getBr().getCell("crh_amount").getDouble(),
			getBr().getCell("crh_cid").getString(),
			getBr().getCell("crh_xrate").getDouble(),
			getBr().getCell("crh_dbinputano").getString(),
			getBr().getCell("crh_voucher").getString(),
			getBr().getCell("crh_lamount").getDouble()
				);
		if(!rtnMsg.getStatus()) return(rtnMsg);
		if(!getBr().getCellString("crhd_desc").equals("")) {
			rtnMsg = AccApi.crh_add_adddesc(
					rpc,
					getBr().getCellString("crhd_desc")
			);	
			if(!rtnMsg.getStatus()) return(rtnMsg);
		}
		cv = sr.getRowCollectionList();
		for(BiCellCollection c : cv) {
			rtnMsg = AccApi.crh_add_crd(
					rpc,
					c.getCell("crd_sno").getString(),
					"",
					c.getCell("crd_cid").getString(),
					c.getCell("crd_xrate").getDouble(),
					c.getCell("crd_amount").getDouble(),
					c.getCell("crd_lamount").getDouble(),
					c.getCell("sih_vcode").getString(),
					c.getCell("crd_desc").getString()
			);
			if(!rtnMsg.getStatus()) return(rtnMsg);
		}
		cv = sr2.getRowCollectionList();
		for(BiCellCollection c : cv) {
			rtnMsg = AccApi.crh_add_crd(
					rpc,
					"",
//					c.getCell("cra_ano").getString()+"-"+c.getCell("cra_cid").getString(),
					c.getCell("cra_inputano").getString(),
					c.getCell("cra_cid").getString(),
					c.getCell("cra_xrate").getDouble(),
					c.getCell("cra_amount").getDouble(),
					c.getCell("cra_lamount").getDouble(),
					"",
					c.getCell("cra_desc").getString()
			);
			if(!rtnMsg.getStatus()) return(rtnMsg);
		}
		rtnMsg = AccApi.crh_add_end(rpc);
		if(!rtnMsg.getStatus()) return(rtnMsg);
		Value v = rpc.callSegment("erpv4_autoposttogl");
		if(v != null && v.toInt() < 0) {
			return (new ReturnMsg(false,"Error crh_add_end"));
		}
		if(v.toInt() > 0) {
			rtnMsg = AccApi.arpost_one_crh(rpc,
				getBr().getCell("crh_module").getString(),
				getBr().getCell("crh_crno").getString()
				);
			if(!rtnMsg.getStatus()) return(rtnMsg);
			try {
				TableRec tr = su.getQueryResult("select * from crh where crh_cocode = ? and crh_crno = ?",
						new Wherecl().appendArgument(getBr().getCellString("crh_cocode")).appendArgument(getBr().getCellString("crh_crno"))
						);
				if(tr.getRecordCount() != 1) {
					return (new ReturnMsg(false,"Error crh_add_end"));
				}
				tr.setRecPointer(0);
				if(tr.getFieldInt("crh_trxno") <= 0) {
					return (new ReturnMsg(false,"Error crh_add_end"));
				}
			} catch (Exception ex) {
				UniLog.log(ex);
				return (new ReturnMsg(false,"Error crh_add_end"));
			}
		}
		return(rtnMsg.defaultOk);
	}
	public ReturnMsg crh_remove(String p_crno) {
		SelectUtil su = getBr().getSelectUtil();
		RpcClient rpc = su.getRpcClient();
		Value v;
		try {
			TableRec tr = su.getQueryResult("select * from crh where crh_cocode = ? and crh_crno = ?",
					new Wherecl().appendArgument(getBr().getCellString("crh_cocode")).appendArgument(p_crno)
					);
			if(tr.getRecordCount() != 1) {
				UniLog.log("crh_remove voucher not exist, return OK");
				return (ReturnMsg.defaultOk);
			}
			v = rpc.callSegment("erpv4_autoposttogl");
			if(v.toInt() > 0) {
				tr.setRecPointer(0);
				int trno = tr.getFieldInt("crh_trxno");
				if(trno > 0) {
					tr = su.getQueryResult("select * from tr where tr_cocode = ? and tr_xno = ?",
						new Wherecl().appendArgument(getBr().getCellString("crh_cocode")).appendArgument(trno)
						);
					if(tr.getRecordCount() != 1) {
						return (new ReturnMsg(false,"Error crh_remove"));
					}
					tr.setRecPointer(0);
					if(tr.getFieldString("tr_post").equals(("P"))) {
						return (new ReturnMsg(false,"Error crh_remove transaction already posted"));
					}
					v = rpc.callSegment("generate_reversetr",
								new VectorUtil()
								.addElement(trno)
								.toVector()
							);
					if(v == null || v.toInt() != 0) {
						return (new ReturnMsg(false,"Error crh_remove remove gl tran error"));
					}
					su.executeUpdate("update crh set crh_trxno=0, crh_post='' where crh_crno = ?", 
						new Wherecl().appendArgument(p_crno)
							);
				}
			}
		} catch (Exception ex) {
			UniLog.log(ex);
				return (new ReturnMsg(false,"Error crh_remove"));
		}
		v = rpc.callSegment("crh_remove",
			new VectorUtil()
			.addElement(p_crno)
			.toVector()
		);
		if(v != null && v.toInt() != 0) {
			return (new ReturnMsg(false,"Error Updating Payement Voucher"));
		}
		return(ReturnMsg.defaultOk);
	}
	
	public void afterBind() {
		if(addToolBarButton("btDelCurrent","Delete","fa-trash")) {
			
		}
		
		super.afterBind();
		crdViewId = "erpv4.CrdAr";
		craViewId = "erpv4.CrdArAc";
		new JxFieldAction("btDelCurrent") {
			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
			Messagebox.show(sessionHelper.getLabel("Are you sure to delete this record ?"), 
			    sessionHelper.getLabel("Confirmation"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			        new org.zkoss.zk.ui.event.EventListener(){
			            public void onEvent(Event e){
			                if(Messagebox.ON_OK.equals(e.getName())){
				try {
					getBr().beginWork();
					RpcClient rpc = getBr().getSelectUtil().getRpcClient();
					
					rpc.callSegment("setCocodeBaseccy",
							new VectorUtil()
							.addElement(getBr().getCell("crh_cocode").getString())
							.addElement(
									Erpv4Config.getBaseCcy(getSessionHelper(),getBr().getCellString("crh_cocode"))
									)
							.toVector()
							);
					ReturnMsg rtnMsg ;
					rtnMsg = crh_remove(getBr().getCell("crh_crno").getString());
					if(!rtnMsg.getStatus()) {
						if(getBr().inBeginWork()) getBr().rollbackWork();
						getBr().refetchCurrent();
						bindCellCollection(getBr(),curMode);
						messageBox(rtnMsg.getMsg());
						return;
					}
					getBr().commitWork();
					afterUpdate(getBr());
					setDirtyFlag(false);
					needRefreshFlag = true;
					doClose(false);
				} catch (Exception p_ex) {
					UniLog.log(p_ex);
					if(getBr().inBeginWork()) getBr().rollbackWork();
					getBr().refetchCurrent();
					bindCellCollection(getBr(),curMode);
					messageBox("Error Delete Payement Voucher");
				}
			                	
			                }
			            }
					}
			);
			}
		};
			
		new JxFieldAction("btAdd") {

			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				UniLog.log("btAdd trapped");
				try {
					getBr().beginWork();
					RpcClient rpc = getBr().getSelectUtil().getRpcClient();
					
					rpc.callSegment("setCocodeBaseccy",
							new VectorUtil()
							.addElement(getBr().getCell("crh_cocode").getString())
							.addElement(
									Erpv4Config.getBaseCcy(getSessionHelper(),getBr().getCellString("crh_cocode"))
									)
							.toVector()
							);
					if(getBr().getCellString("crh_crno").trim().equals(""))
						getBr().getCell("crh_crno").set(AccApi.getRgControl(rpc, "arcash", getBr().getCell("crh_date").getDate()));
					if(getBr().getCellString("crh_voucher").trim().equals(""))
						getBr().getCell("crh_voucher").set(AccApi.getRgControl(rpc, "arcashvch", getBr().getCell("crh_date").getDate()));
					ReturnMsg rtnMsg ;
					rtnMsg = crh_add();
					if(rtnMsg == null || rtnMsg.getStatus()) rtnMsg = afterUpdate(getBr());
					if(!rtnMsg.getStatus()) {
						if(getBr().inBeginWork()) getBr().rollbackWork();
						getBr().refetchCurrent();
						bindCellCollection(getBr(),curMode);
						messageBox(rtnMsg.getMsg());
						return;
					}
					getBr().commitWork();
					setDirtyFlag(false);
					needRefreshFlag = true;
					doClose(false);
				} catch (Exception p_ex) {
					UniLog.log(p_ex);
					if(getBr().inBeginWork()) getBr().rollbackWork();
					getBr().refetchCurrent();
					bindCellCollection(getBr(),curMode);
					messageBox("Error Updating Payement Voucher");
				}
			}
			
		};
		new JxFieldAction("btUpdate") {

			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				UniLog.log("btAdd trapped");
				try {
					getBr().beginWork();
					RpcClient rpc = getBr().getSelectUtil().getRpcClient();
					
					rpc.callSegment("setCocodeBaseccy",
							new VectorUtil()
							.addElement(getBr().getCell("crh_cocode").getString())
							.addElement(
									Erpv4Config.getBaseCcy(getSessionHelper(),getBr().getCellString("crh_cocode"))
									)
							.toVector()
							);
					/*
					Value v = rpc.callSegment("crh_remove",
							new VectorUtil()
							.addElement(getBr().getCell("crh_crno").getString())
							.toVector()
							);
					if(v != null && v.toInt() != 0) {
						if(getBr().inBeginWork()) getBr().rollbackWork();
						getBr().refetchCurrent();
						bindCellCollection(getBr(),curMode);
						messageBox("Error Updating Payement Voucher");
						return;
					}
					*/
					ReturnMsg rtnMsg ;
					rtnMsg = crh_remove(getBr().getCell("crh_crno").getString());
					if(rtnMsg.getStatus()) rtnMsg = crh_add();
					if(!rtnMsg.getStatus()) {
						if(getBr().inBeginWork()) getBr().rollbackWork();
						getBr().refetchCurrent();
						bindCellCollection(getBr(),curMode);
						messageBox(rtnMsg.getMsg());
						return;
					}
					getBr().commitWork();
					afterUpdate(getBr());
					setDirtyFlag(false);
					needRefreshFlag = true;
					doClose(false);
				} catch (Exception p_ex) {
					UniLog.log(p_ex);
					if(getBr().inBeginWork()) getBr().rollbackWork();
					getBr().refetchCurrent();
					bindCellCollection(getBr(),curMode);
					messageBox("Error Updating Payement Voucher");
				}
			}
		};
	}
	@Override 
	protected void afterAddLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals(crdViewId)) {
			try {
			((BiResultCrhAr) getBr()).calPaymentAmountX();
			} catch (CellException cex) {
				UniLog.log(cex);
			}
			
		}
	}
	@Override 
	protected void afterUnDeleteLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals(crdViewId)) {
			try {
			((BiResultCrhAr) getBr()).calPaymentAmountX();
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		}
	}
	@Override 
	protected void afterDeleteLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals(crdViewId)) {
			try {
			((BiResultCrhAr) getBr()).calPaymentAmountX();
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		}
	}
	@Override
	public void bindCellCollection(BiResult p_br , int p_mode) {
		super.bindCellCollection(p_br, p_mode);
		try {
			p_br.getCell("crh_liamount").eval();
			p_br.getCell("crh_ljamount").eval();
		} catch (CellException cex) {
			UniLog.log(cex);
		}
	}
}
