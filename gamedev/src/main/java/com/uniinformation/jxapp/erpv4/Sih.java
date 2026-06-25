package com.uniinformation.jxapp.erpv4;

import java.util.Vector;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiCellCollectionToJsonInterface;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.DbLog;
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

public class Sih extends JxZkBiBase {
	String sihViewId = null;
	String sidViewId = null;
	public ReturnMsg sih_remove(String p_sno) {
		SelectUtil su = getBr().getSelectUtil();
		RpcClient rpc = su.getRpcClient();
		Value v;
		try {
			TableRec tr = su.getQueryResult("select * from sih where sih_cocode = ? and sih_sno = ?",
					new Wherecl().appendArgument(getBr().getCellString("sih_cocode")).appendArgument(p_sno)
					);
			if(tr.getRecordCount() != 1) {
				UniLog.log("sih_remove voucher not exist, return OK");
				return (ReturnMsg.defaultOk);
			}
			v = rpc.callSegment("erpv4_autoposttogl");
			if(v.toInt() > 0) {
				tr.setRecPointer(0);
				int trno = tr.getFieldInt("sih_trxno");
				if(trno > 0) {
					tr = su.getQueryResult("select * from tr where tr_cocode = ? and tr_xno = ?",
						new Wherecl().appendArgument(getBr().getCellString("sih_cocode")).appendArgument(trno)
						);
					if(tr.getRecordCount() != 1) {
						return (new ReturnMsg(false,"Error sih_remove"));
					}
					tr.setRecPointer(0);
					if(tr.getFieldString("tr_post").equals(("P"))) {
						return (new ReturnMsg(false,"Error sih_remove transaction already posted"));
					}
					v = rpc.callSegment("generate_reversetr",
								new VectorUtil()
								.addElement(trno)
								.toVector()
							);
					if(v == null || v.toInt() != 0) {
						return (new ReturnMsg(false,"Error sih_remove remove gl tran error"));
					}
					su.executeUpdate("update sih set sih_trxno=0, sih_post='' where sih_sno = ?", 
						new Wherecl().appendArgument(p_sno)
							);
				}
			}
		} catch (Exception ex) {
			UniLog.log(ex);
				return (new ReturnMsg(false,"Error sih_remove"));
		}
		v = rpc.callSegment("sih_remove",
			new VectorUtil()
			.addElement(p_sno)
			.toVector()
		);
		if(v != null && v.toInt() != 0) {
			v = rpc.callSegment("callfunction",
						new VectorUtil()
						.addElement( "getvalue")
						.addElement("HOLDMSG").toVector()
						);
			return (new ReturnMsg(false,"Error Removing Invoice "  +
						(v == null ? "" : v.toString())
						));
		}
		return(ReturnMsg.defaultOk);
	}
	
	ReturnMsg sih_add() {
		SelectUtil su = getBr().getSelectUtil();
		RpcClient rpc = su.getRpcClient();
		ReturnMsg rtnMsg;
		rtnMsg = AccApi.sih_add_start(
			rpc,
			getBr().getCell("sih_module").getString(),
			getBr().getCell("sih_sno").getString(),
			getBr().getCell("sih_vcode").getString(),
			getBr().getCell("sih_date").getDate(),
			getBr().getCell("sih_due").getInt(),
			getBr().getCell("sih_cid").getString(),
			getBr().getCell("sih_total").getDouble(),
			getBr().getCell("sih_xrate").getDouble(),
			getBr().getCell("sih_ltotal").getDouble(),
			getBr().getCell("sih_type").getString(),
			getBr().getCell("sih_gldpcode").getString(),
			"",
			getBr().getCell("sih_ref").getString()
				);
		if(!rtnMsg.getStatus()) return(rtnMsg);
		BiResult sr = getBr().getSubLink(sidViewId);
		Vector <BiCellCollection> cv = sr.getRowCollectionList();
		for(BiCellCollection c : cv) {
			if(c.testCell("sid_dpcode") != null) {
			rtnMsg = AccApi.sih_add_addanoex(
					rpc,
					c.getCell("sid_inputano").getString(),
					c.getCell("sid_amount").getDouble(),
					c.getCell("sid_xrate").getDouble(),
					c.getCell("sid_lamount").getDouble(),
					c.getCell("sid_cid").getString(),
					c.getCell("sid_desc0").getString(),
					c.getCell("sid_dpcode").getString()
			);
				
			} else {
			rtnMsg = AccApi.sih_add_addano(
					rpc,
					c.getCell("sid_inputano").getString(),
					c.getCell("sid_amount").getDouble(),
					c.getCell("sid_xrate").getDouble(),
					c.getCell("sid_lamount").getDouble(),
					c.getCell("sid_cid").getString(),
					c.getCell("sid_desc0").getString()
			);
			}
			if(!rtnMsg.getStatus()) return(rtnMsg);
		}
		rtnMsg = AccApi.sih_add_end(rpc);
		if(!rtnMsg.getStatus()) return(rtnMsg);
		Value v = rpc.callSegment("erpv4_autoposttogl");
		if(v != null && v.toInt() < 0) {
			return (new ReturnMsg(false,"Error sih_add_end"));
		}
		if(v.toInt() > 0) {
			rtnMsg = AccApi.arpost_one_sih(rpc,
				getBr().getCell("sih_module").getString(),
				getBr().getCell("sih_sno").getString()
				);
			if(!rtnMsg.getStatus()) return(rtnMsg);
			try {
				TableRec tr = su.getQueryResult("select * from sih where sih_cocode = ? and sih_sno = ?",
						new Wherecl().appendArgument(getBr().getCellString("sih_cocode")).appendArgument(getBr().getCellString("sih_sno"))
						);
				if(tr.getRecordCount() != 1) {
					return (new ReturnMsg(false,"Error sih_add_end"));
				}
				tr.setRecPointer(0);
				if(tr.getFieldInt("sih_trxno") <= 0) {
					return (new ReturnMsg(false,"Error sih_add_end"));
				}
			} catch (Exception ex) {
				UniLog.log(ex);
				return (new ReturnMsg(false,"Error sih_add_end"));
			}
		}
		return(rtnMsg.defaultOk);
	}
	protected String snoRgControl = null;
	@Override
	public void afterBind() {
		if(addToolBarButton("btDelCurrent","Delete","fa-trash")) {
			
		}
		super.afterBind();

		new JxFieldAction("btDelCurrent") {
			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				if(!getBr().getCellString("sih_frxno").equals("")) {
					messageBox("Cannot Delete Invoice Generated From Other Module");
					return;
				}
			Messagebox.show(sessionHelper.getLabel("Are you sure to delete this record ?"), 
			    sessionHelper.getLabel("Confirmation"), Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION,
			        new org.zkoss.zk.ui.event.EventListener(){
						@Override
						public void onEvent(Event arg0) throws Exception {
							try {
								getBr().beginWork();
								RpcClient rpc = getBr().getSelectUtil().getRpcClient();
								rpc.callSegment("setCocodeBaseccy",
										new VectorUtil()
										.addElement(getBr().getCell("sih_cocode").getString())
										.addElement(
												Erpv4Config.getBaseCcy(getSessionHelper(),getBr().getCellString("sih_sno"))
												)
										.toVector()
										);
								ReturnMsg rtnMsg ;
								rtnMsg = sih_remove(getBr().getCell("sih_sno").getString());
								if(!rtnMsg.getStatus()) {
									if(getBr().inBeginWork()) getBr().rollbackWork();
									getBr().refetchCurrent();
									bindCellCollection(getBr(),curMode);
									messageBox(rtnMsg.getMsg());
									return;
								}
								if(getSessionHelper().dblogDeleteEnabled()) {
								try {
								DbLog.dblogInsertOne(getSessionHelper(), getBr().getView().getName(), getBr().getCurrentCollection().getSid(), "delete", 
										BiCellCollectionToJsonInterface.BiCellCollectionToJSON( getBr().getCurrentCollection()), null);
								} catch (Exception ex) {
									UniLog.log(ex);
								}
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
							.addElement(getBr().getCell("sih_cocode").getString())
							.addElement(
									Erpv4Config.getBaseCcy(getSessionHelper(),getBr().getCellString("sih_cocode"))
									)
							.toVector()
							);
					if(getBr().getCellString("sih_sno").equals("")) {
						getBr().getCell("sih_sno").set(AccApi.getRgControl(rpc, snoRgControl, getBr().getCell("sih_date").getDate()));
					}
					ReturnMsg rtnMsg ;
					rtnMsg = sih_add();
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
					messageBox("Error Adding Invoice");
				}
			}
			
		};
		new JxFieldAction("btUpdate") {

			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				UniLog.log("btUpdate trapped");
				/*
				 Update By DT  to allow change invoice genearated from other module, should have restriction ,add later
				if(!getBr().getCellString("sih_frxno").equals("")) {
					messageBox("Cannot Update Invoice Generated From Other Module");
					return;
				}
				*/
				try {
					getBr().beginWork();
					RpcClient rpc = getBr().getSelectUtil().getRpcClient();
					
					rpc.callSegment("setCocodeBaseccy",
							new VectorUtil()
							.addElement(getBr().getCell("sih_cocode").getString())
							.addElement(
									Erpv4Config.getBaseCcy(getSessionHelper(),getBr().getCellString("sih_cocode"))
									)
							.toVector()
							);
					ReturnMsg rtnMsg ;
					rtnMsg = sih_remove(getBr().getCell("sih_sno").getString());
					if(rtnMsg.getStatus()) rtnMsg = sih_add();
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
					messageBox("Error Updating Invoice");
				}
			}
		};	
	}
}
