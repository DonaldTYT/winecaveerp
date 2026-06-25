package com.uniinformation.jxapp.hw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.hw.BiResultHwInvoice;
import com.uniinformation.bicore.hw.BiResultHwQuoDet;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.MessageBoxActionInterface;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import org.json.JSONException;
import org.json.JSONObject;
public class HwInvoice extends HwOrderBase {
	@Override
	void setupActionButton(boolean is_dirty)
	{
		super.setupActionButton(is_dirty);
		jxAdd("btConfirmInv").setEnable(false);
		jxAdd("btUnConfirmInv").setEnable(false);
		jxSetEnable("btVoidInv",false);
		jxSetEnable("btPrintInv",false);
		if(curMode == JxZkBiBase.MODE_UPDATE) { 
		if (getBr().allowUpdate()) {
			if(getBr().getCell("inv_quostatus").getString().equals("Confirmed")) {
				jxAdd("btUnConfirmInv").setEnable(true);
				jxSetEnable("btVoidInv",true);
			} else if(getBr().getCell("inv_quostatus").getString().equals("Void")) {
				jxAdd("btUnConfirmInv").setEnable(true);
			} else {
				jxAdd("btConfirmInv").setEnable(true);
				jxSetEnable("btVoidInv",true);
			}
		}
			if(!is_dirty) {
				jxAdd("btPrintInv").setEnable(true);
			}
		}
	}
	
	
	@Override
	public void afterBind() {
		super.afterBind();
//		detViewName = "hw.InvDet";
		detViewName = "hw.InvQuoDet";
		
	new JxFieldAction("btUnConfirmInv") {
		public void actionPerformed(JxField fd){
			String s = getBr().getCell("inv_quostatus").getString();
			/*
			if(!s.equals("Confirmed")) {
				messageBox("Cannot UnConfirm " + s + " Order ");
				return;
			} 
			*/
			confirm("Do you want to unconfirm this invoice ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
							try {
								if(((Integer) p_obj).intValue() == 1) {
								String s= getBr().getCell("inv_quostatus").getString();
								getBr().getCell("inv_quostatus").set("New");
								ReturnMsg rtnMsg = processUpdate(JxZkBiBase.AFTERADDUPDATE_ACTION_RELOAD);
								if(rtnMsg != null && !rtnMsg.getStatus()) {
									getBr().getCell("inv_quostatus").set(s);
									messageBox(rtnMsg.getMsg());
								}
								setupActionButton(false);
								}
							} catch (CellException cex)  {
								UniLog.log(cex);
							}
						}
					}
				);	
		}
	};
	new JxFieldAction("btVoidInv") {
		public void actionPerformed(JxField fd){
			String s = getBr().getCell("inv_quostatus").getString();
			/*
			if(!s.equals("Confirmed")) {
				messageBox("Cannot UnConfirm " + s + " Order ");
				return;
			} 
			*/
			confirm("Do you want to void this invoice ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
							try {
								if(((Integer) p_obj).intValue() == 1) {
								String s= getBr().getCell("inv_quostatus").getString();
								getBr().getCell("inv_quostatus").set("Void");
								BiResult sr = getBr().getSubLink("hw.InvQuoDet");
								int nRow = sr.getRowCount();
								for(int i=0;i<nRow;i++) {
									Object o = sr.getTrStatObj(new Integer(i));
									if(o != null) {
										sr.markDelete(o, true);
									}
								}
								ReturnMsg rtnMsg = processUpdate(JxZkBiBase.AFTERADDUPDATE_ACTION_RELOAD);
								if(rtnMsg != null && !rtnMsg.getStatus()) {
									getBr().getCell("inv_quostatus").set(s);
									messageBox(rtnMsg.getMsg());
								}
								setupActionButton(false);
								}
							} catch (CellException cex)  {
								UniLog.log(cex);
							}
						}
					}
				);	
		}
	};

	new JxFieldAction("btConfirmInv") {
		public void actionPerformed(JxField fd){
			String s = getBr().getCell("inv_quostatus").getString();
			if(getBr().getCellString("inv_invno").equals("")) {
				Messagebox.show("Please Save or set Invoice Number before Post");
				return;
			}
			/*
			if(!s.equals("Confirmed")) {
				messageBox("Cannot UnConfirm " + s + " Order ");
				return;
			} 
			*/
			confirm("Do you want to confirm this invoice ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
							try {
								if(((Integer) p_obj).intValue() == 1) {
								String s= getBr().getCell("inv_quostatus").getString();
								getBr().getCell("inv_quostatus").set("Confirmed");
								ReturnMsg rtnMsg = processUpdate(JxZkBiBase.AFTERADDUPDATE_ACTION_RELOAD);
								if(rtnMsg != null && !rtnMsg.getStatus()) {
									getBr().getCell("inv_quostatus").set(s);
									messageBox(rtnMsg.getMsg());
								}
								setupActionButton(false);
								}
							} catch (CellException cex)  {
								UniLog.log(cex);
							}
						}
					}
				);	
		}
	};
		
	
	new JxFieldAction("btGenOrder") {
		public void actionPerformed(JxField fd){
			confirm("Do you want to create project for this quotation/invoice ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
							try {
								if(((Integer) p_obj).intValue() == 1) {
								RpcClient rpc = getRpcClient();
								java.util.Date d = DateUtil.today();
//								String s = ((BiResultHwQuotation) getBr()).getNewOrderNumber(d);
								String s = "";
								Value val = rpc.callSegment("createQuoFromInvoice",
											new VectorUtil()
											.addElement(getBr().getCell("inv_rg").getInt())
											.addElement(s)
											.addElement(d)
											.toVector()
										);
								if(val != null && val.toString().startsWith("OK")) {
									String fname = val.toString().substring(4);
									UniLog.log("Project created :" + val.toString().substring(4));
									messageBox("Project created :" + val.toString().substring(4));
								} else {
									messageBox("Create Project failed " + val == null ? "Reason Unknown" : val.toString());
								}
								}
								
							} catch (Exception cex)  {
								UniLog.log(cex);
							}
						}
					}
				);	
			
		}
	};
	
	new JxFieldAction("btPrintInv") {
		public void actionPerformed(JxField fd){
			if(getBr().getCellString("inv_invno").equals("")) {
				Messagebox.show("Please Save or set Invoice Number before Print");
				return;
			}
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ReturnMsg rtn = ((BiResultHwInvoice) getBr()).printInvoice(bos);
            if(rtn.getStatus()) {
				ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), bos.toByteArray(), "Invoice-"+getBr().getCell("inv_invno").getString());
            } else {
				Messagebox.show(rtn.getMsg());
            }
		}
	};
		
	}
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		super.bindCellCollection(br,mode);
		/*
		if(mode == JxZkBiBase.MODE_ADD) {
			try {
				SelectUtil su = br.getSelectUtil();
				TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+Erpv4Config.getCoCode(br.getSessionHelper())+ "'",null);
				br.getCell("inv_remark").set( "HAHA"
					);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					br.getCell("inv_remark").set(tr.getFieldString("co_payment"));
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
		*/
		if(br.getCellString("inv_invno").equals("")) setDirtyFlag(true);
	}
//	public void bindCellCollection(BiResult br,int mode) {
//		boolean isFirst = false;
//		if(quoDetailAddListener==null) {
//			isFirst = true;
//		}	
//		super.bindCellCollection(br,mode);
//		if(isFirst) {
//			jxAdd("btAddQuoDet_"+BiResultHwQuoDet.DELTALTYPE_SERVICE_ITEM ).setVisible(true);
//			jxAdd("btAddQuoDet_"+BiResultHwQuoDet.DELTALTYPE_PRINTING_ITEM ).setVisible(false);
//			jxAdd("btAddQuoDet_"+BiResultHwQuoDet.DELTALTYPE_DESCRIPTION).setVisible(false);
//			jxAdd("btAddQuoDet_"+BiResultHwQuoDet.DELTALTYPE_COMBO_ITEM).setVisible(false);
//			jxAdd("btAddQuoDet_"+BiResultHwQuoDet.DELTALTYPE_LINEBREAK).setVisible(true);
//		}
//	}
	@Override 
	protected void afterUnDeleteLink(BiResult sr,int idx)
	{
		super.afterUnDeleteLink(sr,idx);
		if(sr.getView().getName().equals("hw.InvDet")) {
			((BiResultHwInvoice) getBr()).realCalInvTotal();
		}
	}
	@Override 
	protected void afterDeleteLink(BiResult sr,int idx)
	{
		super.afterDeleteLink(sr,idx);
		if(sr.getView().getName().equals("hw.InvDet")) {
			((BiResultHwInvoice) getBr()).realCalInvTotal();
		}
	}
}
