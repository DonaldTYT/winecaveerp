package com.uniinformation.jxapp.erpv4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Button;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Image;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.InputElement;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultInvoice;
import com.uniinformation.bicore.erpv4.BiResultQuoDet;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.MessageBoxActionInterface;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.JxZkBiBaseCallback;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiCellValueMapper;
import com.uniinformation.zkbi.ZkBiGetItemProperty;

public class Quotation extends JxZkBiBase {
	protected Window popupPoScr = null;
    protected BiResult popupPoBr = null;
	protected JxZkBiBase popupJx = null;
	protected boolean popupOpened = false;
	protected String detailViewId = null;
	protected String invoiceViewId = null;
	protected String allocateViewId = null;
	protected JxActionListener quoDetailAddListener=null;
	protected ArrayList<Integer> detTypeList = null;
	protected boolean reviseConfirmedQuo = false;
//	protected String defaultStatus = "Confirmed";
//	class QuoRemarkGetItemProperty extends BiGetItemProperty {
//		QuoRemarkGetItemProperty(BiResult p_br) {
//			super(p_br);
//		}
//	}
	
	protected ColumnCell nextEnterField(CellCollection col) {
		ColumnCell colc;
		if(BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM ) {
			if(col.getCell("st_irg").getInt() <= 0) {
			colc = (ColumnCell) col.getCell("st_icode");
			Component comp = ((ZkBiCellValueMapper) colc.getMapper()).getComponent();
			Clients.showNotification("Please Select Stock Item", "warning", comp, "end_center", 3000);
			return(colc);
			}
		}
		if(BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultQuoDet.DELTATYPE.DELTALTYPE_COMBO_ITEM ) {
			if(col.getCell("ind_srg").getInt() <= 0) {
			colc = (ColumnCell) col.getCell("stmcm_name");
			Component comp = ((ZkBiCellValueMapper) colc.getMapper()).getComponent();
			Clients.showNotification("Please Select Set Name", "warning", comp, "end_center", 3000);
			return(colc);
			}
		}
		return(null);
	}
	class QuoDetGetItemProperty extends BiGetItemProperty {
		Vector columnListStockItem;
		Vector columnListServiceItem;
		Vector columnListDescription;
		Vector columnListComboItem;
		Vector columnListLineBreak;
		Vector columnListTradeIn;
		Template priceTemplate;
		Template lineBreakTemplate;
		Template statusTemplate;
		Template trideInPriceTemplate;
		IcodePicker jxf = null;
		QuoDetGetItemProperty(BiResult p_br) {
			super(p_br);
		}
		
		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			ColumnCell bcc = (ColumnCell) p_value;
//			if(p_ctype != GIPI_CELL_MAPPED) setDirtyFlag(true);
			switch(p_ctype ) {
			case GIPI_VALUE_ONOK:
			{
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " ok");
				if(popupOpened) {
					if(bcc.getBiColumn().getLabel().equals("st_icode"))  {
						return;
					} else {
						popupOpened = false;
					}
				}
				CellCollection col = bcc.getCollection();
				ColumnCell nextCell = nextEnterField(col);
				if(nextCell == null) {
					BiResult sr = getBr().getSubLink(detailViewId);
					Vector<BiCellCollection> colList = sr.getRowCollectionList();
					int idx = colList.indexOf(col);
					if(idx < colList.size()-1) {
						nextCell = ((ColumnCell) colList.get(idx+1).getCell("st_icode"));
					} else {
						listboxAddRow(Quotation.this, sr, jxAdd("list_"+detailViewId.replace(".", "_")), null, -1);
						setDirtyFlag(true);
					}
				}
				if(nextCell != null) {
					Component comp = ((ZkBiCellValueMapper) nextCell.getMapper()).getComponent();
					((HtmlBasedComponent) comp).focus();
				}
			}
				break;
			case GIPI_PULLDOWN_CLOSED :
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " closed ");
				popupOpened = false;
				break;
			case GIPI_PULLDOWN_OPENED:
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " opened ");
				if(bcc.getBiColumn().getLabel().equals("st_icode"))  {
					

					try {
						popupOpened = true;
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						if(jxf == null) {
							SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
							JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
							jxf = (IcodePicker) getOrCreateJxZkForm(new Idspace(),pvdr ,"erpv4.IcodePicker");
						}
//						zjpi.setPopupWidth("700px");
//						zjpi.setPopupHeight("500px");
						zjpi.setJxZkForm(jxf);
//						IcodePicker jxf = (IcodePicker) zjpi.getJxZkForm();
						CellCollection col = bcc.getCollection();
//						Wherecl wcl = null;
						Wherecl wcl = wcl = createPullDownWherecl(col);
						Component comp = ((ZkBiCellValueMapper) ((ColumnCell) col.getCell(
								(BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultQuoDet.DELTATYPE.DELTALTYPE_TRADEIN) ? "ind_unit" : "ind_qty"
								)).getMapper()).getComponent();
						jxf.setPickerForAnyStockWithBalance(false,Quotation.this,getBr().getSelectUtil(), wcl,bcc,null,(HtmlBasedComponent) comp);
						setDirtyFlag(true);
					} catch (Exception ex) {
						UniLog.log(ex);
					}	

				}
				break;
			case GIPI_CELL_MAPPED :
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " mapped ");
				CellCollection col = bcc.getCollection();
				if(BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION) {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						Component comp = zcvm.getComponent();
						if(comp instanceof Textbox)  {
							((Textbox) comp).setRows(3);
						}
				}
				if(bcc.getBiColumn().getLabel().equals("st_icode")) {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						final ZkJxPickInput comp = (ZkJxPickInput) zcvm.getComponent();
						comp.setPopupWidth("700px");
//						((HtmlBasedComponent) comp).focus();
						new ZkBiAbstractLongOp(parentComp, null){
							@Override
							public ReturnMsg longOp() {
								((HtmlBasedComponent) comp).focus();
								return null;
							}
						};
				}
				break;
			case GIPI_VALUE_CHANGED:
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " value_changed ");
				col = bcc.getCollection();
				if(bcc.getBiColumn().getLabel().equals("ind_tiuprice")) {
					if(BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultQuoDet.DELTATYPE.DELTALTYPE_TRADEIN) {
						UniLog.log("ind_tiuprice value:" + bcc.getDouble());
						if (bcc.getDouble() > 0) {
							try {
								bcc.set(-bcc.getDouble());
							} catch (Exception e) {
								UniLog.log("ind_tiuprice set value:" + e.toString());
							}
						}
					}
				}
				setDirtyFlag(true);
				break;
			default:
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " " + p_ctype);
				setDirtyFlag(true);
				break;
			}
		}
		
		@Override
		public Object onBeforeValueChange(Object bindedCell,Object value) {
			ColumnCell bcc = (ColumnCell) bindedCell;
			if(bcc.getBiColumn().getLabel().equals("st_icode"))  {
				String code = value.toString();
				try {
					TableRec tr = getBr().getSelectUtil().getQueryResult("select st_icode from stock where st_oicode = '"+code+"' or st_icode = '"+code+"'", null);
					if(tr.getRecordCount() == 1) return(tr.getField("st_icode"));
					ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
					ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
					zjpi.open();
					popupOpened = true;
					
					if(jxf == null) {
						SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
						JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
						jxf = (IcodePicker) getOrCreateJxZkForm(new Idspace(),pvdr ,"IcodePicker");
					}
					zjpi.setPopupWidth("700px");
//					zjpi.setPopupHeight("500px");
					zjpi.setJxZkForm(jxf);
//					IcodePicker jxf = (IcodePicker) zjpi.getJxZkForm();
					CellCollection col = bcc.getCollection();
//					Wherecl wcl = null;
					Wherecl wcl = wcl = createPullDownWherecl(col);
					Component comp = ((ZkBiCellValueMapper) ((ColumnCell) col.getCell(
							BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultQuoDet.DELTATYPE.DELTALTYPE_TRADEIN ? "ind_unit" : "ind_qty"
							)).getMapper()).getComponent();
					jxf.setPickerForAnyStockWithBalance(false,Quotation.this,getBr().getSelectUtil(), wcl,bcc,code,(HtmlBasedComponent) comp);					
//					return(bcc.getString());
					return("");
				} catch (Exception ex) {
					UniLog.log(ex);
				}	
			}
			return(null);
		}
//		public Object onBeforeValueChange(Object bindedCell,Object value) {
//			ColumnCell bcc = (ColumnCell) bindedCell;
//			if(bcc.getBiColumn().getLabel().equals("st_icode"))  {
//				String code = value.toString();
//				try {
//					TableRec tr = getBr().getSelectUtil().getQueryResult("select st_icode from stock where st_oicode = '"+code+"' or st_icode = '"+code+"'", null);
//					if(tr.getRecordCount() == 1) {
//						return(tr.getField("st_icode"));
//					} else {
//
//					try {
//						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
//						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
//						zjpi.open();
//					} catch (Exception ex) {
//						UniLog.log(ex);
//					}	
//						
//						
//						return(bcc.getString());
//					}
//				} catch (Exception ex) {
//					UniLog.log(ex);
//				}
//			}
//			return(null);
//		}
	}
	@Override
	public void afterBind() {
		super.afterBind();
		detailViewId = "erpv4.QuoDet";
        allocateViewId = "erpv4.OsOrderDet";
	new JxFieldAction("btReviseOdr") {

		@Override
		public void actionPerformed(JxField jxfield) {
			// TODO Auto-generated method stub
			try {
				if(getBr().getCellString("inv_quostatus").equals("Confirmed") && !reviseConfirmedQuo) {
					reviseConfirmedQuo = true;
					bindCellCollection(getBr(),JxZkBiBase.MODE_UPDATE);
				} else {
					reviceQuotation() ;
					/*
					SelectUtil su = getBr().getSelectUtil();
					int rev = getBr().getCell("inv_revisonno").getInt();
					rev = rev+1;
					getBr().getCell("inv_revisonno").set(rev);
					su.executeUpdate("update quotation set inv_revisonno = ? where inv_rg = ?", 
							new Wherecl().appendArgument(rev).appendArgument(getBr().getCellInt("inv_rg"))
						);
								RpcClient rpc = getBr().getSelectUtil().getRpcClient();
								Value val = rpc.callSegment("reviseQuotation",
											new VectorUtil()
											.addElement(getBr().getCell("inv_rg").getInt())
											.addElement(rev-1)
											.toVector()
										);
								if(val != null && val.toString().startsWith("OK")) {
								}
					*/
				}
			} catch(Exception ex) {
				UniLog.log(ex);
			}
		}
		
	};
	new JxFieldAction("btCopyOdr") {
		public void actionPerformed(JxField fd){
			confirm("Do you want to copy this order ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
							try {
								if(((Integer) p_obj).intValue() == 1) {
								getBr().beginWork();
								RpcClient rpc = getBr().getSelectUtil().getRpcClient();
								java.util.Date d = DateUtil.today();
								String s = ((BiResultQuotation) getBr()).getNewOrderNumber(d);
								Value val = rpc.callSegment("copyQuotation",
											new VectorUtil()
											.addElement(getBr().getCell("inv_rg").getInt())
											.addElement(s)
											.addElement(d)
											.toVector()
										);
								if(val != null && val.toString().startsWith("OK")) {
									getBr().commitWork();
									int invrg = Integer.parseInt(StringUtil.strpart(val.toString(),4,10).trim());
									String ordnum = StringUtil.strpart(val.toString(),14,15);
									String quonum = StringUtil.strpart(val.toString(),29,15);
									UniLog.log("Order copied to " + invrg + " " + ordnum + " " + quonum);
									messageBox("Order copied to " + ordnum + "/" + quonum);
									needRefreshFlag = true;
									setDirtyFlag(false);

									BiView view = getBr().getView();
					                getBr().clearCondition();
					                getBr().addCustomCondition("inv_rg = " + invrg);
//					                getBr().appendWherecl(new Wherecl().andUniop(view.getTable().getSidField(), "=", addedSid));
					                getBr().query(true);
					                if(getBr().getRowCount() >0) {
										getBr().fetchOneRecV(0);
										getBr().clearLastUpdate();
										initForm(MODE_UPDATE);
										bindCellCollection(getBr(),MODE_UPDATE);
										needRefreshFlag = true;
					                }
								} else {
									getBr().rollbackWork();
									messageBox("Order copy failed " + val == null ? "Reason Unknown" : val.toString());
								}
								}
								
							} catch (Exception cex)  {
								if(getBr().inBeginWork()) { getBr().rollbackWork(); }
								UniLog.log(cex);
							}
						}
					}
				);	
			
		}
	};
	new JxFieldAction("btConfirmOdr") {
		public void actionPerformed(JxField fd){
			String s = getBr().getCell("inv_quostatus").getString();
			if(!s.equals("New") && !s.equals("Revised") && !s.equals("Void") && !s.equals("ReqApprove")) {
				messageBox("Cannot Confirm " + s + " Order ");
				return;
			} 
			confirm("Do you want to confirm this order ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
							try {
								if(((Integer) p_obj).intValue() == 1) {
								String s= getBr().getCell("inv_quostatus").getString();
								String cfnStatus = "Confirmed";
								getBr().getCell("inv_quostatus").set(cfnStatus);
								ReturnMsg rtnMsg = processUpdate(JxZkBiBase.AFTERADDUPDATE_ACTION_RELOAD);
								if(rtnMsg != null && !rtnMsg.getStatus()) {
									getBr().getCell("inv_quostatus").set(s);
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
	new JxFieldChange("inv_vcode vd_vname") {
		public boolean valueChanged(JxField fd, String p_value) {
			try {
				if(getBr().getCell("inv_contact").isOverrided()) {
					getBr().getCell("inv_contact").clearOverride();
				}
				if(getBr().getCell("inv_tel").isOverrided()) {
					getBr().getCell("inv_tel").clearOverride();
				}
				if(getBr().getCell("inv_fax").isOverrided()) {
					getBr().getCell("inv_fax").clearOverride();
				}
				/*
				if(jxAdd("inv_contact").getFieldMode() == Cell.VMODE_OVERRIDED) {
					jxAdd("inv_contact").setFieldMode(Cell.VMODE_PROTECTED);
				}
				if(jxAdd("inv_tel").getFieldMode() == Cell.VMODE_OVERRIDED) {
					jxAdd("inv_tel").setFieldMode(Cell.VMODE_PROTECTED);
				}
				if(jxAdd("inv_fax").getFieldMode() == Cell.VMODE_OVERRIDED) {
					jxAdd("inv_fax").setFieldMode(Cell.VMODE_PROTECTED);
				}
				*/
			} catch(CellException cex) {
				UniLog.log(cex);
			}
			return(true);
		}
	};
	new JxFieldAction("btVoidOdr") {
		public void actionPerformed(JxField fd){
			String s = getBr().getCell("inv_quostatus").getString();
			confirm("Do you want to void this order ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
							try {
								if(((Integer) p_obj).intValue() == 1) {
								String s= getBr().getCell("inv_quostatus").getString();
								getBr().getCell("inv_quostatus").set("Void");
								ReturnMsg rtnMsg = processUpdate(JxZkBiBase.AFTERADDUPDATE_ACTION_RELOAD);
								if(rtnMsg != null && !rtnMsg.getStatus()) {
									getBr().getCell("inv_quostatus").set(s);
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
			String s = getBr().getCell("inv_quostatus").getString();
			if(!s.equals("Confirmed") && !s.equals("Void")) {
				messageBox("Cannot UnConfirm " + s + " Order ");
				return;
			} 
			confirm("Do you want to unconfirm this order ?", 
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
								}
							} catch (CellException cex)  {
								UniLog.log(cex);
							}
						}
					}
				);	
		}
	};
	
	new JxFieldAction("btPrintBody") {
		public void actionPerformed(JxField fd){
				UniLog.log("print Pressed");
				try {
					ZkUtil.print((Component) (jxAdd("detail_grid").getNativeObject()));	
					} catch (Exception ex) {
						UniLog.log(ex);
					}
		}
	};

	new JxFieldAction("btPrintQuo") {
		public void actionPerformed(JxField fd){
			UniLog.log("print quo Pressed");
			RpcClient rpc = getRpcClient();
			Value val = rpc.callSegment("printer_autoselect",
						new VectorUtil()
						.addElement(1)
						.toVector()
					);
			//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
			val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(getSessionHelper().getWebContentRealPath("images", true)) .toVector());
			
			List<String> termsList = ChnftrParser.splitText(getBr().getCell("inv_term").getString().trim(), "helv_nr", "chinese", 10, 535);
			List<String> deliveryList = ChnftrParser.splitText(getBr().getCell("inv_quodeli").getString().trim(), "helv_nr", "chinese", 10, 535);
			List<String> remarkList = ChnftrParser.splitText(getBr().getCell("inv_remark").getString().trim(), "helv_nr", "chinese", 10, 715);
			val = rpc.callSegment("erpv4_print_quo",
						new VectorUtil()
						.addElement(getBr().getCell("inv_rg").getInt())
						.addElement(StringUtils.join(termsList, "\n"))
						.addElement(StringUtils.join(deliveryList, "\n"))
						.addElement(StringUtils.join(remarkList, "\n"))
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
				UniLog.log("Print quo got " + fname);
				try {
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
	abHelper.addButton((Button) jxAdd("btPrintQuo").getNativeObject(),false,true,"fa-print",-1);

	new JxFieldAction("btPrintQuoNew") {
			public void actionPerformed(JxField fd){
				// TODO Auto-generated method stub
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				ReturnMsg rtn = ((BiResultQuotation) getBr()).printQuotation(bos,null);
				if(rtn.getStatus()) {
					String ss = getBr().getCellString("inv_quonum");
					ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), bos.toByteArray(), "Quotation-"+ss);
				} else {
					Messagebox.show(rtn.getMsg());
				}
			}
	};
	if(jxAdd("btPrintQuoNew") != null) abHelper.addButton((Button) jxAdd("btPrintQuoNew").getNativeObject(),false,true,"fa-print",-1);

	new JxFieldAction("btPrintCon") {
		public void actionPerformed(JxField fd){
			UniLog.log("print con Pressed");
			RpcClient rpc = getRpcClient();
			ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
			rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
			Value val = rpc.callSegment("printer_autoselect",
						new VectorUtil()
						.addElement(1)
						.toVector()
					);
			//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
			val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(getSessionHelper().getWebContentRealPath("images", true)) .toVector());
			
			val = rpc.callSegment("erpv4_print_mccon",
						new VectorUtil()
						.addElement(getBr().getCell("inv_rg").getInt())
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
				UniLog.log("Print con got " + fname);
				try {
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
	if(jxAdd("svloc_desp") != null ) {
			
	new JxFieldChange("inv_vcode vd_vname") {
	public boolean valueChanged(JxField fd,String orgValue){  
		try {
			TableRec tr = getBr().getSelectUtil().getQueryResult("select svloc_desp from sv_loc where svloc_custcode = '" + getBr().getCell("inv_vcode").getString() + "'", null);
				Vector v = new Vector();
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					v.add(tr.getField("svloc_desp"));
				}
				getBr().getCell("svloc_desp").setItemList(v);
			} catch (Exception ex){ 
				UniLog.log(ex);
			}	
		return(true);
	}
	};						
	}
		LOCK_RECORD_FOR_UPDATE = true;
		detTypeList = new ArrayList<Integer>();
		detTypeList.add(BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM ));
		detTypeList.add(BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION));
		detTypeList.add(BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_LINEBREAK));
	}
	
	@Override
	protected ReturnMsg beforeAddLink(JxField fd,BiResult sr,CellCollection cl,int p_insIdx) 
	{
//		return(getBr().doBeforeAdd(cl));
		if(sr.getView().getName().equals(detailViewId)) {
			int actionIdx;
			if(fd != null) {
				Integer aIdx = (Integer)ZkUtil.getAttribute((Component) fd.getNativeObject(), "actionIdx", 3); 
				if(aIdx != null) {
					actionIdx = aIdx.intValue();
					actionIdx = (Integer)ZkUtil.getAttribute((Component) fd.getNativeObject(), "actionIdx", 3); 
					UniLog.logm(this,"field id = %s %s idx %d",fd.getName(),((Component) fd.getNativeObject()).getId(),actionIdx);
				} else {
					actionIdx = -1;
				}
			} else actionIdx = -1;
			try  {
				if(fd == null) {
					cl.getCell("ind_pdsrg").set( BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM ));
				} else {
				if(fd == jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM))) {
					cl.getCell("ind_pdsrg").set( BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM ));
				}
				if(fd == jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_SERVICE_ITEM))) {
					cl.getCell("ind_pdsrg").set( BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_SERVICE_ITEM ));
				}
				if(fd == jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION))) {
					cl.getCell("ind_pdsrg").set( BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION));
				}
				if(fd == jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_COMBO_ITEM))) {
					cl.getCell("ind_pdsrg").set( BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_COMBO_ITEM));
				}
				if(fd == jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_LINEBREAK))) {
					cl.getCell("ind_pdsrg").set( BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_LINEBREAK));
				}
				if(fd == jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_TRADEIN))) {
					cl.getCell("ind_pdsrg").set( BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_TRADEIN));
				}
				}
				switch(BiResultQuoDet.getDeltaType(getSessionHelper(),cl.getCell("ind_pdsrg").getInt())) {
				case DELTALTYPE_STOCK_ITEM:
				case DELTALTYPE_SERVICE_ITEM:
				case DELTALTYPE_DESCRIPTION:
					if(actionIdx < 0) actionIdx = sr.getRowCount()-1;
					if(actionIdx >= 0) {
						CellCollection col = sr.getRowCollectionV(actionIdx);
						if(BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultQuoDet.DELTATYPE.DELTALTYPE_COMBO_ITEM
								|| col.getCell("ind_subitem").getString().equals("Y")) {
							cl.getCell("ind_subitem").set("Y");
							cl.getCell("ind_itemno").setMode(Cell.VMODE_HIDDEN);
							cl.getCell("ind_amount").setMode(Cell.VMODE_HIDDEN);
						}
					}
				}
			} catch (CellException cex) {
				UniLog.log(cex);
				return(new ReturnMsg(false,cex.toString()));
			}
		}
//		if(sr.getView().getName().equals("QuoRemark")) {
//			Vector <CellCollection> v = sr.getRowCollectionList();
//			for(CellCollection col : v) {
//				if(col.getCell("qm_tmdesp").getString().trim().equals("")) {
//					return(new ReturnMsg(false,"Current Line Is Empty"));
//				}
//			}
//		}
		return(null);
	}
	/*
	@Override
	protected ReturnMsg beforeUpdateLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals("QuoDet")) {
		if(getBr().getCell("inv_quostatus").getString().equals("Confirmed")) {
			return(new ReturnMsg(false,"Cannot update confirmed order details"));
		}
		}
		return(null);
	}
	@Override
	protected ReturnMsg beforeDeleteLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals("QuoDet")) {
		if(getBr().getCell("inv_quostatus").getString().equals("Confirmed")) {
			return(new ReturnMsg(false,"Cannot delete confirmed order details"));
		}
		}
		return(null);
	}
	*/
	public void bindCellCollection(BiResult br,int mode) {
		if(br.getSubLink("erpv4.QuoInvoice") != null) invoiceViewId = "erpv4.QuoInvoice";
		else invoiceViewId = null;
		
		JxField invListfd = null;
		if(invoiceViewId != null) invListfd = jxAdd("list_"+invoiceViewId.replace(".", "_"));
		if(invListfd != null) {
			invListfd.setVisible(false);
		}
		JxField qd=null;
		if(quoDetailAddListener==null) {
			BiResult sr = br.getSubLink(detailViewId);
			quoDetailAddListener = genListboxAddActionListener(this, sr , jxAdd("list_"+detailViewId.replace(".", "_")),INS_IDX_ACTIONIDX);
			for(int detType:detTypeList) {
				qd = jxAdd("btAddQuoDet_"+detType);
				if(qd != null) qd.addActionListener(quoDetailAddListener);
				
			}
//			jxAdd("btAddQuoDet_"+BiResultQuoDet.DELTALTYPE_STOCK_ITEM ).addActionListener(quoDetailAddListener);
////			jxAdd("btAddQuoDet_"+BiResultQuoDet.DELTALTYPE_SERVICE_ITEM ).addActionListener(quoDetailAddListener);
//			jxAdd("btAddQuoDet_"+BiResultQuoDet.DELTALTYPE_DESCRIPTION).addActionListener(quoDetailAddListener);
//			jxAdd("btAddQuoDet_"+BiResultQuoDet.DELTALTYPE_LINEBREAK).addActionListener(quoDetailAddListener);
		}
		for(int detType:detTypeList) {
			qd = jxAdd("btAddQuoDet_"+detType);
			if(qd != null) qd.setVisible(true);
			
		}
////		jxAdd("btAddQuoDet_"+BiResultQuoDet.DELTALTYPE_SERVICE_ITEM ).setVisible(true);
//		jxAdd("btAddQuoDet_"+BiResultQuoDet.DELTALTYPE_STOCK_ITEM ).setVisible(true);
//		jxAdd("btAddQuoDet_"+BiResultQuoDet.DELTALTYPE_DESCRIPTION).setVisible(true);
//		jxAdd("btAddQuoDet_"+BiResultQuoDet.DELTALTYPE_LINEBREAK).setVisible(true);
		if(getGipi(detailViewId) == null) {
			setGipi(detailViewId,new QuoDetGetItemProperty(br.getSubLink(detailViewId)));	
		}	
		super.bindCellCollection(br, mode);
		
		jxAdd("list_"+detailViewId.replace(".", "_")).setAttribute("mode","canInsert");
		if(mode == JxZkBiBase.MODE_ADD) {
			jxSetEnable("btConfirmOdr",false);
			jxSetEnable("btUnConfirmOdr",false);
			jxSetEnable("btVoidOdr",false);
			jxSetEnable("btReviseOdr",false);
			try {
				br.getCell("inv_date").set(new java.util.Date());
//				br.getCell("inv_quostatus").update(defaultStatus);
				br.getCell("inv_cid").update(Erpv4Config.getBaseCcy(br.getSessionHelper(),br.getCellString("inv_cocode")));
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		} 
		if(mode == JxZkBiBase.MODE_UPDATE) { 
			if(br.getCell("inv_quostatus").getString().equals("Confirmed")) {
				jxSetEnable("btConfirmOdr",false);
				jxSetEnable("btUnConfirmOdr",true);
				jxSetEnable("btVoidOdr",true);
				jxAdd("inv_quostatus").setEnable(false);
				jxSetEnable("btReviseOdr",true);
				if(invListfd != null) {
					invListfd.setVisible(true);
				}
				jxSetEnable("vd_vname",false);
			} else if(br.getCell("inv_quostatus").getString().equals("Void")) {
				jxSetEnable("btConfirmOdr",true);
				jxSetEnable("btUnConfirmOdr",true);
				jxSetEnable("btVoidOdr",false);
				jxAdd("inv_quostatus").setEnable(false);
				jxSetEnable("vd_vname",true);
			} else {
				jxSetEnable("btConfirmOdr",true);
				jxSetEnable("btUnConfirmOdr",false);
				jxSetEnable("btVoidOdr",true);
				jxSetEnable("btReviseOdr",true);
				jxAdd("inv_quostatus").setEnable(true);
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
		if(jxAdd("svloc_desp") != null ) {
		if(mode == JxZkBiBase.MODE_ADD) {
				br.getCell("svloc_desp").setItemList(new Vector());
		} 
		if(mode == JxZkBiBase.MODE_UPDATE) { 
			try {
			TableRec tr = br.getSelectUtil().getQueryResult("select svloc_desp from sv_loc where svloc_custcode = '" + br.getCell("inv_vcode").getString() + "'", null);
				Vector v = new Vector();
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					v.add(tr.getField("svloc_desp"));
				}
				br.getCell("svloc_desp").setItemList(v);
			} catch (Exception ex){ 
				UniLog.log(ex);
			}
		}
		}
		BiResult sr = getBr().getSubLink(invoiceViewId);
		if(sr != null) {
			Vector<BiCellCollection> cl = sr.getRowCollectionList();
			for(BiCellCollection col : cl) {
				try {
					Cell c = col.testCell("invh_print");
					if(c != null) c.setMode(Cell.VMODE_NORMAL); 
				} catch (CellException cex) {
					UniLog.log(cex);
				}
			}
		}
	}
	

	@Override 
	protected void afterAddLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals(detailViewId)) {
			try {
			((BiResultQuotation) getBr()).real_calTotalAmount();
			} catch (CellException cex) {
				UniLog.log(cex);
			}
			
		}
		if(sr.getView().getName().equals(invoiceViewId)) {
			double iTotal = 0;
			CellCollection ccol = sr.getRowCollectionV(idx);
			int n = sr.getRowCount();
			for(int i=0;i<idx;i++) {
				if(i != idx) {
				CellCollection col = sr.getRowCollectionV(i);
				if(!col.getCell("invh_post").getString().equals("V")) {
					iTotal += col.getDouble("invh_grnettotal");
				}
				}
			}
			double gTotal = getBr().getCell("inv_grtotal").getDouble() - getBr().getCell("inv_discount").getDouble();
			if(gTotal > iTotal) {
				try {
					double bTotal = gTotal - iTotal;
					ccol.getCell("invh_payratio").set(bTotal/gTotal * 100);
					ccol.getCell("invh_grnettotal").set(bTotal);
				} catch (CellException cex) {
					UniLog.log(cex);
				}
			}
			try {
				ccol.getCell("invh_print").setMode(Cell.VMODE_DISPONLY);
			} catch (CellException cex) {
					UniLog.log(cex);
			}
		}
	}
	@Override 
	protected void afterUnDeleteLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals(detailViewId)) {
			try {
			((BiResultQuotation) getBr()).real_calTotalAmount();
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		}
	}
	@Override 
	protected void afterDeleteLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals(detailViewId)) {
			try {
			((BiResultQuotation) getBr()).real_calTotalAmount();
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		}
	}
	protected Wherecl createPullDownWherecl(CellCollection col) {
		Wherecl wcl = null;
		int srg = col.getCell("ind_srg").getInt();
		if(srg > 0) {
			wcl = new Wherecl().appendString(" and st_irg in (select mcfm_mrg from mcfitmodel where mcfm_modelrg = " + srg + ") ");
		}
		return(wcl);
	}
	
	@Override 
	protected void formDirtyChanged() {
		super.formDirtyChanged();
		BiResult sr = getBr().getSubLink(invoiceViewId);
		if(sr != null) {
			Vector<BiCellCollection> cl = sr.getRowCollectionList();
			for(BiCellCollection col : cl) {
				try {
					if(isDirty()) col.getCell("invh_print").setMode(Cell.VMODE_DISPONLY); else col.getCell("invh_print").setMode(Cell.VMODE_NORMAL); 
				} catch (CellException cex) {
					UniLog.log(cex);
				}
			}
		}

		if(isDirty()) {
			
		}
	}	
	
	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		
		//handle Download button
		List gList = super.getCustomItemPropertyList(p_br, mode);
		BiResult sr = p_br.getSubLink(invoiceViewId);
		if(sr == null) return(gList);
		ZkBiGetItemProperty thisGipi = new ZkBiGetItemProperty(sr,this) {
			@Override
			public void onValueChanged(Object p_value,int p_ctype) {
				ColumnCell bcc = (ColumnCell) p_value;
				if(p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED && bcc.getCellLabel().equals("invh_print")){
					UniLog.log1("%s clicked", bcc.getCellLabel());
					CellCollection col = bcc.getCollection();
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					BiResult sr = getBr().getSubLink(invoiceViewId);
					int idx  = sr.getIndexByCollection(col);
					sr.fetchOneRecV(idx);
//					ReturnMsg rtn = ((BiResultQuoInvoice) sr).printInvoice(bos);
					ReturnMsg rtn = null;
//					if(sr instanceof BiResultQuoInvoice_not_used) rtn = ((BiResultQuoInvoice_not_used) sr).printInvoice(bos);
					if(sr instanceof BiResultInvoice) rtn = ((BiResultInvoice) sr).printInvoice(bos,null);
					if(rtn.getStatus()) {
						ZkUtil.showPdfDialog((Component) getNativeComponent(), getSessionHelper(), bos.toByteArray(), "Invoice-"+col.getCell("invh_invno").getString());
					} else {
						Messagebox.show(rtn.getMsg());
					}
					
					
					
				} else super.onValueChanged(p_value, p_ctype);
			}
		};
		if(gList == null) return ListUtil.of(thisGipi); else {
				gList.add(thisGipi);
				return(gList);
		}
		
	}

	protected void lockQuo(BiResult p_br)
	{
				JxField sv = jxAdd("list_"+replaceViewName(detailViewId));
				AbstractGetItemProperty gipi = getGipi(detailViewId );
				((BiGetItemProperty) gipi).setItemMode(BiGetItemProperty.GETITEM_MODE_LIST);
				sv.setAttribute("mode", "noDelete");
				sv.setAttribute("mode", "noInsert");
				try {
					HashSet<String> hs = new HashSet<String>();
					hs.add("inv_allowpo");
					hs.add("inv_allowdn");
					p_br.getCurrentCollection().lock(hs);
				} catch (Exception ex) {
					UniLog.log(ex);
				}
	}
	protected void unlockQuo(BiResult p_br)
	{
				JxField sv = jxAdd("list_"+replaceViewName(detailViewId));
				AbstractGetItemProperty gipi = getGipi(detailViewId );
				((BiGetItemProperty) gipi).setItemMode(BiGetItemProperty.GETITEM_MODE_INPUT);
				sv.setAttribute("mode", "canDelete");
				sv.setAttribute("mode", "canInsert");
				try {
					p_br.getCurrentCollection().unlock();
				} catch (Exception ex) {
					UniLog.log(ex);
				}
	}

	protected void reviceQuotation() throws Exception
	{
					setDirtyFlag(true);
					SelectUtil su = getBr().getSelectUtil();
					int rev = getBr().getCell("inv_revisonno").getInt();
					rev = rev+1;
					getBr().getCell("inv_revisonno").set(rev);
					su.executeUpdate("update quotation set inv_revisonno = ? where inv_rg = ?", 
							new Wherecl().appendArgument(rev).appendArgument(getBr().getCellInt("inv_rg"))
						);
								RpcClient rpc = getBr().getSelectUtil().getRpcClient();
								Value val = rpc.callSegment("reviseQuotation",
											new VectorUtil()
											.addElement(getBr().getCell("inv_rg").getInt())
											.addElement(rev-1)
											.toVector()
										);
								if(val != null && val.toString().startsWith("OK")) {
								}
	
	}
}