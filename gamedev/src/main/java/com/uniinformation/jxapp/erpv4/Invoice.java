package com.uniinformation.jxapp.erpv4;

import java.io.ByteArrayOutputStream;

import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.MessageBoxActionInterface;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.jxapp.erpv4.OsOrderDet.OsOrderDetGetItemProperty;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.bicore.erpv4.BiResultInvoice;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;

public class Invoice extends JxZkBiBase {
	@Override
	public void afterBind() {
		super.afterBind();
		new JxFieldAction("btPrintInvoice") {

			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					ReturnMsg rtn = ((BiResultInvoice) getBr()).printInvoice(bos,null);
					if(rtn.getStatus()) {
						ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), bos.toByteArray(), "Invoice-"+getBr().getCell("invh_invno").getString());
					} else {
						Messagebox.show(rtn.getMsg());
					}
			}
			
		};
		
		new JxFieldAction("btCopyInv") {
			@Override
			public void actionPerformed(JxField jxfield) {
				// TODO Auto-generated method stub
				
				confirm("Do you want to copy this invoice ?", 
						new MessageBoxActionInterface() {
							public void onButtonClicked( Object p_obj) {
								try {
									if(((Integer) p_obj).intValue() == 1) {
										
				RpcClient rpc = getSessionHelper().getRpcClient();
				
				rpc.callSegment("setCocodeBaseccy",
						new VectorUtil()
						.addElement(
//									Erpv4Config.getCoCode(getSessionHelper()) 
									getBr().getCellString("invh_cocode")
							)
						.addElement(
								Erpv4Config.getBaseCcy(getSessionHelper(),getBr().getCellString("invh_cocode"))
								)
						.toVector()
						);	
				Value v = rpc.callSegment(
						"copyInvoice",
						new VectorUtil()
							.addElement(getBr().getCellInt("invh_rg"))
							.addElement("")
							.addElement(DateUtil.today())
							.toVector()
						);
				rpc.close();
				if(v != null && v.toString().startsWith("OK")) {

					try {
						int invrg = Integer.parseInt(StringUtil.strpart(v.toString(), 4, 10).trim());
						String visitUrl = SessionHelper.getUrlByViewid(getSessionHelper(), "erpv4.Invoice-upd");
						JSONObject jo = new JSONObject();
						jo.put("customCondition","invh_rg = " + invrg);
						String key = sessionHelper.putOneTimeData( jo);
						String url = visitUrl + "&querycondition="+key;
						Executions.sendRedirect(url);
						
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				} else {
					
					messageBox("Copy Invoice Failed " + v == null ? "null" : v.toString());
				}
										
									}
								} catch (Exception ex)  {
									UniLog.log(ex);
								}
							}
						}
					);		
				
				
			}
		};
	}
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		super.bindCellCollection(br, mode);
		/*
		JxField sv = jxAdd("list_erpv4_InvDet");
		if(sv != null) {
			if(br.getCellString("invh_post").equals("P")) {
				sv.setAttribute("mode", "noDelete");
				sv.setAttribute("mode", "noInsert");
			} else {
				sv.setAttribute("mode", "canDelete");
				sv.setAttribute("mode", "canInsert");
			}
		 }
		*/
		if(mode == JxZkBiBase.MODE_UPDATE) {
			if(br.getCell("invh_post") != null && br.getCell("invh_invno") != null) {
				
			if(br.getCellString("invh_post").equals("P")) {
				try {
					if(br.getCell("invh_invno") != null) 
						br.getCell("invh_invno").setMode(Cell.VMODE_DISPONLY);
					if(br.getCell("vd_vname") != null) 
						br.getCell("vd_vname").setMode(Cell.VMODE_DISPONLY);
				} catch (Exception ex) {
					UniLog.log("ex");
				}
			}
			}
			if(br.getCellString("invh_invno").startsWith("NINV")) {
				try {
					br.getCell("invh_invno").set("");
					setDirtyFlag(true);
				} catch(CellException cex) {
					UniLog.log(cex);
				}
			}
		}
	}
}
