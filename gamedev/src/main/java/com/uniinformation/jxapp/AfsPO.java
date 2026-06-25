package com.uniinformation.jxapp;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Window;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultPO;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.MessageBoxActionInterface;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.afs.AfsPoMc;
import com.uniinformation.jxapp.afs.AfsPoParts;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.TranslateListGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiCellValueMapper;

public class AfsPO extends JxZkBiBase {
	Window wPickItemCode = null;
	class AfsPoGetItemProperty extends BiGetItemProperty {
		AfsIcodePicker jxf = null;
		JxSelOpt jxfInvNo = null;
		TrGetItemProperty tgipiInvNo;
		Vector columnListItem = null;
		AfsPoGetItemProperty(BiResult p_br) {
			super(p_br);
			columnListItem = new Vector<BiColumn>();
			if(AfsPO.this instanceof AfsPoParts) return;
			Vector<BiColumn> v = p_br.getListColumns();
			for(BiColumn bc : v) {
				if(bc.getLabel().equals("stmd_ref2") 
					|| bc.getLabel().equals("stmd_ref3") 
						) {
//					columnListItem.add(null);
				}  else {
					columnListItem.add(bc);
				}
			}
		}
		@Override
		protected Vector getListColumns(Object p_v) {
			if(AfsPO.this instanceof AfsPoParts) return(super.getListColumns(p_v));
			return(columnListItem);
		}	
		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			ColumnCell bcc = (ColumnCell) p_value;
			if(p_ctype != GIPI_CELL_MAPPED) setDirtyFlag(true); else {
				if(bcc.getCellLabel().equals("inv_invno")) {
					if(!sessionHelper.isMobileDevice()){
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						zjpi.setPopupWidth("650px");
					}
				}
				if(bcc.getCellLabel().equals("st_icode")) {
					if(!sessionHelper.isMobileDevice()){
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						zjpi.setPopupWidth("700px");
					}
				}
			}
			if(p_ctype == GIPI_PULLDOWN_CLOSED) {
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " closed ");
			}
			if(p_ctype == GIPI_PULLDOWN_OPENED) {
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " opened ");
				if(bcc.getBiColumn().getLabel().equals("inv_invno"))  {
					try {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						if(jxfInvNo == null) {
							SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
							JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
							tgipiInvNo = new TrGetItemProperty(
										new VectorUtil()
											.addElement("inv_vcode")
											.addElement("inv_invno")
											.addElement("st_icode")
											.addElement("inv_pocode")
//											.addElement("ind_rg")
//											.addElement("ind_odrg")
											.toVector(),
										new VectorUtil()
											.addElement("Customer Code")
											.addElement("S.O. Number")
											.addElement("Item Code")
											.addElement("Customer PO")
//											.addElement("ind_rg")
//											.addElement("ind_odrg")
											.toVector()
									);
							jxfInvNo = JxSelOpt.createJxSelOpt(pvdr);
//							jxfInvNo.setWidth("630px");
							jxfInvNo.setOnSelectAction (
									new JxActionListener() {
										public void actionPerformed(JxField fd) {
											//Object[] rec = (Object[]) fd.getValue();
											Object[] rec = (Object[]) jxfInvNo.getPickListBoxValue();
											TableRec tr = tgipiInvNo.getTableRec();
											if (rec != null) {
												try {
													CellCollection ccx = (CellCollection) jxfInvNo.getUserData();
//													ccx.getCell("inv_invno").update(rec[tr.getFieldIndex("inv_invno")]);
//													ccx.getCell("ind_rg").update(rec[tr.getFieldIndex("ind_rg")]);
													ccx.getCell("stmd_qorg").update(rec[tr.getFieldIndex("ind_odrg")]);
													ccx.getCell("stmd_qirg").update(rec[tr.getFieldIndex("ind_irg")]);
													ccx.getCell("stmd_irg").update(rec[tr.getFieldIndex("ind_irg")]);
												} catch (CellException cex ) {  
													UniLog.log(cex);
												} 
												jxfInvNo.closeForm();
											}
										}
									}
							);
						}
//						zjpi.setPopupWidth("650px");
//						zjpi.setPopupHeight("500px");
						zjpi.setJxZkForm(jxfInvNo);
						TableRec tr = getBr().getSelectUtil().getQueryResult("select distinct inv_vcode, inv_invno, ind_odrg, ind_irg, st_icode,inv_pocode from qodetstatus, quodet, quotation, stock "
								+ "where qdst_ostqty > 0 "
								+ " and ind_odrg = qdst_qorg and ind_irg = qdst_qirg "
								+ " and inv_rg = ind_rg "
								+ " and st_irg = ind_irg ", null);
						tgipiInvNo.setTableRec(tr);
						jxfInvNo.setUserData(bcc.getCollection());
						jxfInvNo.jxAdd("pickListBox").setItemListInterface(tgipiInvNo);
					} catch (Exception ex) {
						UniLog.log(ex);
					}	
				}
				if(bcc.getBiColumn().getLabel().equals("st_icode"))  {
					try {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						if(jxf == null) {
							SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
							JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
							jxf = (AfsIcodePicker) getOrCreateJxZkForm(new Idspace(),pvdr ,"AfsIcodePicker");
						}
//						AfsIcodePicker jxf = (AfsIcodePicker) zjpi.getJxZkForm();
//						zjpi.setPopupWidth("700px");
//						zjpi.setPopupHeight("500px");
						zjpi.setJxZkForm(jxf);
						Wherecl wcl = createPullDownWherecl(bcc.getCollection());
						jxf.setPickerForAnyStock(AfsPO.this,getBr().getSelectUtil(), wcl,bcc,null,null);
					} catch (Exception ex) {
						UniLog.log(ex);
					}	
				}
			} 
		}
	}
	
	
	@Override
	public void afterBind() {
		super.afterBind();
	new JxFieldAction("btPrint") {
		public void actionPerformed(JxField fd){
				UniLog.log("print Pressed");
				JxZkBiBase jxf = (JxZkBiBase) fd.getJxForm();
				BiResult br = jxf.getBr();
				SessionHelper sh = br.getSessionHelper();
				RpcClient rpc = getRpcClient();
				Value val = rpc.callSegment("printer_autoselect",
							new VectorUtil()
							.addElement(1)
							.toVector()
						);
				//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
				val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(sh.getWebContentRealPath("images", true)) .toVector());
				
				val = rpc.callSegment("erpv4_print_po",
							new VectorUtil()
							.addElement(getBr().getCell("stm_mrg").getInt())
							.addElement("CHNPRINT")
							.addElement("VARIABLE")
							.addElement("A4P")
							.addElement("NORMAL")
							.addElement("LPTRAW")
							.toVector()
						);
				rpc.close();
				if(val != null && val.toString().startsWith("OK")) {
					String fname = val.toString().substring(4);
					UniLog.log("Print Po got " + fname);
					try {
//					ZkUtil.print((Component) (jxAdd("detail_grid").getNativeObject()));	
						InputStream is = erpFileInputStream(fname);
						ChnftrParser ps = new ChnftrParser(is,"'");
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ps.print(bos);
						ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
						SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
						ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
						
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
		}
	};
	new JxFieldAction("btConfirmOdr") {
		public void actionPerformed(JxField fd){
			String s = getBr().getCell("stm_status").getString();
			if(!s.equals("New") && !s.equals("Revised") && !s.equals("Void") && !s.equals("ReqApprove")) {
				messageBox("Cannot Confirm " + s + " Order ");
				return;
			} 
			confirm("Do you want to confirm this order ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
							try {
								if(((Integer) p_obj).intValue() == 1) {
								String s= getBr().getCell("stm_status").getString();
								String cfnStatus = "Confirmed";
								getBr().getCell("stm_status").set(cfnStatus);
								ReturnMsg rtnMsg = processUpdate(JxZkBiBase.AFTERADDUPDATE_ACTION_RELOAD);
								if(rtnMsg != null && !rtnMsg.getStatus()) {
									getBr().getCell("stm_status").set(s);
									messageBox(rtnMsg.getMsg());
								}
								}
							} catch (CellException cex)  {
								UniLog.log(cex);
							}
						}
					}
				);	
		}
	};
	new JxFieldAction("btUnConfirmOdr") {
		public void actionPerformed(JxField fd){
			String s = getBr().getCell("stm_status").getString();
			if(!s.equals("Confirmed")) {
				messageBox("Cannot UnConfirm " + s + " Order ");
				return;
			} 
			confirm("Do you want to unconfirm this order ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
							try {
								if(((Integer) p_obj).intValue() == 1) {
								String s= getBr().getCell("stm_status").getString();
								getBr().getCell("stm_status").set("New");
								ReturnMsg rtnMsg = processUpdate(JxZkBiBase.AFTERADDUPDATE_ACTION_RELOAD);
								if(rtnMsg != null && !rtnMsg.getStatus()) {
									getBr().getCell("stm_status").set(s);
									messageBox(rtnMsg.getMsg());
								}
								}
							} catch (CellException cex)  {
								UniLog.log(cex);
							}
						}
					}
				);	
		}
	};
		LOCK_RECORD_FOR_UPDATE = true;
	}
	
	@Override
	protected ReturnMsg beforeAddLink(JxField fd,BiResult sr,CellCollection cl,int p_insIdx) 
	{
//		return(getBr().doBeforeAdd(cl));
		if(sr.getView().getName().equals("AfsPoDet")) {
		}
		return(null);
	}
	@Override
	protected ReturnMsg beforeUpdateLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals("AfsPoDet")) {
		}
		return(null);
	}
	@Override
	protected ReturnMsg beforeDeleteLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals("AfsPoDet")) {
		}
		return(null);
	}
	
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		boolean isNew;
		isNew = getGipi("AfsPoDet") == null;
		if(isNew) {
			setGipi("AfsPoDet",new AfsPoGetItemProperty(p_br.getSubLink("AfsPoDet")));	
		}
		super.bindCellCollection(p_br, mode);
		p_br.getCell("stm_ref5").setItemPropertyInterface(
				new TranslateListGetItemProperty(
						new VectorUtil()
							.addElement("HK")
							.addElement("SH")
							.addElement("SZ")
							.addElement("TX")
							.toVector()
						) {
					public String translate(Object p_o) {
						if(p_o.toString().equals("HK"))return("Hong Kong");
						if(p_o.toString().equals("SH"))return("Shanghai ,China");
						if(p_o.toString().equals("SZ"))return("Yantian Shenzhen, China");
						if(p_o.toString().equals("TX"))return("Tianjin Xingang PRC");
						return("");
					}

					@Override
					public int getRowWidth() {
						// TODO Auto-generated method stub
						return 0;
					}
				}
		);
		
		if("Y".equals(Erpv4Config.getString(getSessionHelper(),"DPRequireApproval"))) { //stm_type = DP -> Purchase Order
			try {
			if(p_br.getCellString("stm_status").equals("Confirmed")) {
				p_br.getCell("stm_status").setMode(Cell.VMODE_DISPONLY);
				if(!BiSchema.hasAccessRight(getSessionHelper(), "#cfm"+p_br.getCellString("stm_type"))) {
					disableDeleteLink(p_br,((BiResultPO) p_br).getStmdLinkName());
				}
				p_br.getCell("stm_status").setItemList(
					new VectorUtil()
						.addElement("Confirmed")
						.toVector()
					);
			} else {
				p_br.getCell("stm_status").setItemList(
					new VectorUtil()
						.addElement("New")
						.addElement("ReqApprove")
						.addElement("Void")
						.toVector()
					);
			}
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		}

		jxSetEnable("btConfirmOdr",false);
		jxSetEnable("btUnConfirmOdr",false);
		/*
		if(mode == JxZkBiBase.MODE_ADD) {
			try {
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		} 
		*/
		if(mode == JxZkBiBase.MODE_UPDATE) { 
			if(p_br.getCell("stm_status").getString().equals("Confirmed")) {
				if("Y".equals(Erpv4Config.getString(getSessionHelper(),"DPRequireApproval"))) { //stm_type = DP -> Purchase Order
				if(BiSchema.hasAccessRight(getSessionHelper(), "#cfm"+p_br.getCellString("stm_type"))) {
					jxSetEnable("btUnConfirmOdr",true);
				}
					jxSetEnable("stm_status",false);
				}
				jxSetEnable("vd_vname",false);
			} else {
				if("Y".equals(Erpv4Config.getString(getSessionHelper(),"DPRequireApproval"))) { //stm_type = DP -> Purchase Order
				if(BiSchema.hasAccessRight(getSessionHelper(), "#cfm"+p_br.getCellString("stm_type"))) {
					jxSetEnable("btConfirmOdr",true);
				}
					jxSetEnable("stm_status",true);
				}
				jxSetEnable("vd_vname",true);
			}
			/*
			try {
			((BiResultQuotation) br).real_calTotalAmount();
			} catch (CellException cex) {
				UniLog.log(cex);
			}
			*/
		}
		try {
			if(p_br.getCellString("stm_ref1").equals("")) {
				p_br.getCell("stm_date").setMode(Cell.VMODE_NORMAL);
			} else {
				p_br.getCell("stm_date").setMode(Cell.VMODE_DISPONLY);
			}
		} catch (CellException cex) {
			UniLog.log(cex);
		}
	}
	
	@Override
	protected void linkDetailPickInputOpened(BiResult sr, JxField fd)
	{
		
		if(!fd.getName().equals("list_AfsPoDet")) return;
		Object o = fd.gridGetValue(fd.getCurrentCol() , -2 );
		if( !(o instanceof ColumnCell) ) return;
		BiColumn bc = ((ColumnCell) o).getBiColumn();
		if(!bc.getLabel().equals("st_icode")) return;
		
		try {
			ZkJxPickInput zjpi = (ZkJxPickInput) fd.gridGetValue(fd.getCurrentCol(),-1);
			if(fd.getActionType() == JxField.ACTIONTYPE_PICKINPUTOPENED) {
				AfsIcodePicker jxf = (AfsIcodePicker) zjpi.getJxZkForm();
				if(jxf == null) {
					zjpi.setPopupWidth("700px");
//					zjpi.setPopupHeight("500px");
					SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
					JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
					jxf = (AfsIcodePicker) getOrCreateJxZkForm(new Idspace(),pvdr ,"AfsIcodePicker");
					zjpi.setJxZkForm(jxf);
				}
				jxf.setPickerForAnyStock(this,sr.getSelectUtil(),null, sr.getRowCollectionV(fd.getCurrentRow()).getCell("st_icode"),null,null);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	protected Wherecl createPullDownWherecl(CellCollection col) {
		return(null);
	}
	
	
}
