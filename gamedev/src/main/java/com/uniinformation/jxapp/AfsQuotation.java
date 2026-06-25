package com.uniinformation.jxapp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Button;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.afs.BiResultAfsQuoDet;
import com.uniinformation.bicore.afs.BiResultAfsQuotation;
import com.uniinformation.bicore.erpv4.BiResultQuoDet;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.MessageBoxActionInterface;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.afs.AfsQuoMc;
import com.uniinformation.jxapp.erpv4.Quotation;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.JxZkBiBaseCallback;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiCellValueMapper;

public class AfsQuotation extends Quotation {
	class AfsQuoDetGetItemProperty extends BiGetItemProperty {
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
		AfsIcodePicker jxf = null;
		AfsQuoDetGetItemProperty(BiResult p_br) {
			super(p_br);
			columnListStockItem = new Vector<BiColumn>();
			columnListServiceItem = new Vector<BiColumn>();
			columnListDescription = new Vector<BiColumn>();
			columnListComboItem = new Vector<BiColumn>();
			columnListLineBreak = new Vector<BiColumn>();
			columnListTradeIn = new Vector<BiColumn>();
			Vector<BiColumn> v = p_br.getListColumns();
			priceTemplate = ((Component) getNativeComponent()).getTemplate("template_AfsQuoDet_Price");
			lineBreakTemplate = ((Component) getNativeComponent()).getTemplate("template_AfsQuoDet_LineBreak");
			statusTemplate = ((Component) getNativeComponent()).getTemplate("template_AfsQuoDet_QdstStatus");
			trideInPriceTemplate = ((Component) getNativeComponent()).getTemplate("template_AfsQuoDet_TrideInPrice");
			Vector<BiColumn> unIdentifiedColumn = new Vector<BiColumn>();
			for(BiColumn bc : v) {
				UniLog.log("bicolumn get:" + bc.getLabel());
				// ind_itemno is shown only for "main item" (ind_subitem = false) and is auto sequenced
				if(bc.getLabel().equals("ind_itemno")) {
					columnListStockItem.add(bc);
					columnListServiceItem.add(bc);
					columnListComboItem.add(bc);
					columnListDescription.add(null);
					columnListLineBreak.add(null);
					columnListTradeIn.add(bc);

				// ind_desc span 4 column and is mutually exclusive with ind_ref1,ind_ref2,st_icode and st_iname
				} else if(bc.getLabel().equals("ind_desc")) {
					columnListServiceItem.add(bc);
					columnListDescription.add(bc);
					columnListLineBreak.add(lineBreakTemplate);
				} else if(bc.getLabel().equals("ind_linebreak")) {
//					columnListLineBreak.add(bc);
				} else if(bc.getLabel().equals("st_icode")) {
					columnListStockItem.add(bc);
					columnListTradeIn.add(bc);
				} else if(bc.getLabel().equals("stmcm_name")) {
				// set product
					columnListComboItem.add(bc);
				} else if(bc.getLabel().equals("st_iname")) {
					columnListStockItem.add(bc);
					columnListTradeIn.add(bc);
				} else if(bc.getLabel().equals("ind_ref1")) {
					if(AfsQuotation.this instanceof AfsQuoMc) {
						columnListStockItem.add(null);
						columnListComboItem.add(null);
						columnListTradeIn.add(null);
					} else {
						columnListStockItem.add(bc);
						columnListComboItem.add(bc);
						columnListTradeIn.add(bc);
					}
				} else if(bc.getLabel().equals("ind_ref2")) {
					if(AfsQuotation.this instanceof AfsQuoMc) {
						columnListStockItem.add(null);
						columnListComboItem.add(null);
						columnListTradeIn.add(null);
					} else {
						columnListStockItem.add(bc);
						columnListComboItem.add(null);
						columnListTradeIn.add(bc);
					}
				} else if(bc.getLabel().equals("ind_unit")) {
					columnListStockItem.add(bc);
					columnListServiceItem.add(bc);
					columnListDescription.add(null);
					columnListComboItem.add(null);
					columnListTradeIn.add(bc);
				} else if(bc.getLabel().equals("ind_qty")) {
					columnListStockItem.add(bc);
					columnListServiceItem.add(bc);
					columnListDescription.add(null);
					columnListComboItem.add(null);
				} else if(bc.getLabel().equals("ind_tiqty")) {
					columnListServiceItem.add(bc);
					columnListDescription.add(null);
					//columnListComboItem.add(null);
					columnListTradeIn.add(bc);
				} else if(bc.getLabel().equals("ind_uprice")) {
				// other qty/price related fields (ind_qty ind_unit ind_amount  ...) are only shown for stock or service item
//					Template tl = ((Component) getNativeComponent()).getTemplate("template_AfsQuoDet_Price");
					if(priceTemplate != null){
						columnListStockItem.add(priceTemplate);
						columnListServiceItem.add(priceTemplate);
					} else {
						columnListStockItem.add(bc);
						columnListServiceItem.add(bc);
					}
					columnListDescription.add(null);
					columnListComboItem.add(null);
				} else if(bc.getLabel().equals("ind_tiuprice")) {
					if(priceTemplate != null){
						columnListTradeIn.add(trideInPriceTemplate);
					} else {
						columnListTradeIn.add(bc);
					}
//				} else if(bc.getLabel().equals("ind_discount")) {
				} else if(bc.getLabel().equals("ind_netuprice")) {
					columnListStockItem.add(bc);
					columnListServiceItem.add(bc);
					columnListDescription.add(null);
					columnListComboItem.add(null);
					columnListTradeIn.add(bc);
				} else if(bc.getLabel().equals("ind_amount")) {
					columnListStockItem.add(bc);
					columnListServiceItem.add(bc);
					columnListDescription.add(null);
					columnListTradeIn.add(bc);
				} else if(bc.getLabel().equals("ind_setamount")) {
					columnListComboItem.add(bc);
				} else if(bc.getLabel().equals("qdst_status")) {
				// ind_status is only shown for stock item
					columnListStockItem.add(statusTemplate);
					columnListServiceItem.add(null);
					columnListDescription.add(null);
					columnListComboItem.add(null);
					columnListTradeIn.add(null);
//				} else if(bc.getLabel().equals("ind_srg")) {
				} else {
					unIdentifiedColumn.add(bc);
				}
			}
			for(BiColumn bc : unIdentifiedColumn) {
					columnListStockItem.add(bc);
					columnListServiceItem.add(bc);
					columnListComboItem.add(bc);
					columnListDescription.add(bc);
					columnListLineBreak.add(bc);
					columnListTradeIn.add(null);
			}
		}
		@Override
		public Object getHeader(Object p_v,int p_col) {
			Object o = getListColumns(p_v).get(p_col);
			UniLog.log("getHeader: " + o);
			if(o instanceof BiColumn) return(super.getHeader(p_v, p_col));
			if(o instanceof Template) {
				if(o == priceTemplate || o == trideInPriceTemplate) return(sessionHelper.getLabel("Retail Price & Discount"));
			}
			return("");
		}	
		@Override
		public String getColumnWidth(Object p_v ,int p_col){
			Object o = getListColumns(p_v).get(p_col);
			if(o instanceof BiColumn) return(super.getColumnWidth(p_v, p_col));
			if(o instanceof Template) {
				if(o == priceTemplate) return("width=200px");
				if(o == trideInPriceTemplate) return("width=200px");
				if(o == statusTemplate) return("width=200px");
			}
			return("10px");
		}	
		
		
		@Override
		public Object getColumnValueByName(Object p_v,String p_name) {
			Object o = bigibr.getTrStatObj(p_v);
			final CellCollection col = bigibr.getRowCollectionO(o);
			if(p_name.equals("btQdstReserve")) {
				return(new JxActionListener() {
					public void actionPerformed(JxField fd){
						UniLog.log("btQdstReserve Pressed for "+ col.getCell("ind_odrg").getInt());

//						ColumnCell bcc = (ColumnCell) col.getCell("st_icode");
//						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
//						try {
//							ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
//							zjpi.open();
//						} catch (Exception ex) {
//							UniLog.log(ex);
//						}						
//						if(true) return;
						
                		if(popupPoScr == null) {
                			popupPoScr = ZkUtil.newPopupWindow("Create Goods Receive Record",parentComp);
                			popupPoScr.setWidth("1920px");
                			popupPoScr.setHeight("1000px");
                			popupPoScr.setContentStyle("overflow:auto;");
                			BiSchema schema = (BiSchema) sessionHelper.getSessionData("biSchema");
                			BiView view = schema.getViewByName("AfsOsOrderDet");
                			UniLog.log("queryResult view:"+view);
                			//popupPoBr = view.newBiResult(sessionHelper.getVcode(),null,"com.uniinformation.bicore.afs.BiResultAfsOsOrderDet");
                			popupPoBr = view.newBiResult(sessionHelper.getLoginId(),null,"com.uniinformation.bicore.afs.BiResultAfsOsOrderDet",sessionHelper);
                			popupJx = JxZkBiBase.buildDetailWindow(popupPoBr, popupPoScr, false, true, (JxZkBiBaseCallback) null);
                		}
                		popupPoBr.clearCondition();
                		popupPoBr.addCustomCondition("ind_odrg = " + col.getCell("ind_odrg").getInt());
                		popupPoBr.query(true);
                		popupPoBr.loadOneRecV(0);
                		popupPoBr.fetchOneRecV(0);
    					popupJx.setIsMobile(false);
    					popupJx.bindCellCollection(popupPoBr,JxZkBiBase.MODE_UPDATE);
    					popupJx.jxSetVisible("btUpdate",false);
    					popupJx.jxSetVisible("btAdd",true);
    					popupJx.showForm();	
    					popupJx.doModalUpdate();   					
						
					}
				}
				);
			}
//			if(p_name.equals("ind_itemno") || p_name.equals("ind_amount")) {
//				Cell cc = ((Cell) super.getColumnValueByName(p_v, "ind_subitem"));
//				if(cc.getString().equals("Y")) return(null);
//			}
			return(super.getColumnValueByName(p_v, p_name));
		}
		
		@Override
		public Object getColumnValue(Object p_v,int p_col) {
			Object o = getListColumns(p_v).get(p_col);
			if(o instanceof BiColumn) return(super.getColumnValue(p_v, p_col));
			else return(o);
		}	
		@Override
		public int getColumnSpan(Object p_v,int p_col) {
//			BiColumn bc = (BiColumn) getListColumns(p_v).get(p_col);
//			if(bc != null && bc.getLabel().equals("ind_desc")) return(4);
//			if(bc != null && bc.getLabel().equals("stmcm_name")) return(2);
//			if(bc != null && bc.getLabel().equals("ind_linebreak")) return(9);
//			return(1);
			Object o = getListColumns(p_v).get(p_col);
			if(o == null ) return(1);
			if(o instanceof BiColumn) {
				BiColumn bc = (BiColumn) o;
				if(bc.getLabel().equals("ind_desc")) return(4);
				if(bc.getLabel().equals("stmcm_name")) return(2);
//				if(bc.getLabel().equals("ind_linebreak")) return(9);
			}
			if(o instanceof Template) {
				if(o == priceTemplate) return(1);
				if(o == trideInPriceTemplate) return(1);
				if(o == statusTemplate) return(1);
				if(o == lineBreakTemplate) return(9);
			}
			return(1);
		}
		@Override
		protected Vector getListColumns(Object p_v) {
			if(p_v == null ) return(columnListStockItem);
			Object o = bigibr.getTrStatObj(p_v);
			CellCollection col = bigibr.getRowCollectionO(o);
//			int pdsrg = col.getCell("ind_pdsrg").getInt();
			BiResultQuoDet.DELTATYPE pdsrg = BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt());
			if(pdsrg == BiResultAfsQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM ) {
				return(columnListStockItem);
			} else if(pdsrg == BiResultAfsQuoDet.DELTATYPE.DELTALTYPE_SERVICE_ITEM ) {
				return(columnListServiceItem);
			} else if(pdsrg == BiResultAfsQuoDet.DELTATYPE.DELTALTYPE_COMBO_ITEM ) {
				return(columnListComboItem);
			} else if(pdsrg == BiResultAfsQuoDet.DELTATYPE.DELTALTYPE_LINEBREAK) {
				return(columnListLineBreak);
			} else if(pdsrg == BiResultAfsQuoDet.DELTATYPE.DELTALTYPE_TRADEIN) {
				return(columnListTradeIn);
			} else {
				return(columnListDescription);
			}
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
					BiResult sr = getBr().getSubLink("AfsQuoDet");
					Vector<BiCellCollection> colList = sr.getRowCollectionList();
					int idx = colList.indexOf(col);
					if(idx < colList.size()-1) {
						nextCell = ((ColumnCell) colList.get(idx+1).getCell("st_icode"));
					} else {
						listboxAddRow(AfsQuotation.this, sr, jxAdd("list_AfsQuoDet"), null, -1);
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
							jxf = (AfsIcodePicker) getOrCreateJxZkForm(new Idspace(),pvdr ,"AfsIcodePicker");
						}
						zjpi.setPopupWidth("700px");
//						zjpi.setPopupHeight("500px");
						zjpi.setJxZkForm(jxf);
//						AfsIcodePicker jxf = (AfsIcodePicker) zjpi.getJxZkForm();
						CellCollection col = bcc.getCollection();
//						Wherecl wcl = null;
						Wherecl wcl = wcl = createPullDownWherecl(col);
						Component comp = ((ZkBiCellValueMapper) ((ColumnCell) col.getCell(
								BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultAfsQuoDet.DELTATYPE.DELTALTYPE_TRADEIN ? "ind_unit" : "ind_qty"
								)).getMapper()).getComponent();
						jxf.setPickerForAnyStockWithBalance(false,AfsQuotation.this,getBr().getSelectUtil(), wcl,bcc,null,(HtmlBasedComponent) comp);
						setDirtyFlag(true);
					} catch (Exception ex) {
						UniLog.log(ex);
					}	
				}
				break;
			case GIPI_CELL_MAPPED :
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " mapped ");
				CellCollection col = bcc.getCollection();
				if(BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultAfsQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION) {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						Component comp = zcvm.getComponent();
						if(comp instanceof Textbox)  {
							((Textbox) comp).setRows(3);
						}
				}
				if(bcc.getBiColumn().getLabel().equals("st_icode")) {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						final Component comp = zcvm.getComponent();
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
					if(BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultAfsQuoDet.DELTATYPE.DELTALTYPE_TRADEIN) {
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
						jxf = (AfsIcodePicker) getOrCreateJxZkForm(new Idspace(),pvdr ,"AfsIcodePicker");
					}
					zjpi.setPopupWidth("700px");
//					zjpi.setPopupHeight("500px");
					zjpi.setJxZkForm(jxf);
//					AfsIcodePicker jxf = (AfsIcodePicker) zjpi.getJxZkForm();
					CellCollection col = bcc.getCollection();
//					Wherecl wcl = null;
					Wherecl wcl = wcl = createPullDownWherecl(col);
					Component comp = ((ZkBiCellValueMapper) ((ColumnCell) col.getCell(
							BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultAfsQuoDet.DELTATYPE.DELTALTYPE_TRADEIN ? "ind_unit" : "ind_qty"
							)).getMapper()).getComponent();
					jxf.setPickerForAnyStockWithBalance(false,AfsQuotation.this,getBr().getSelectUtil(), wcl,bcc,code,(HtmlBasedComponent) comp);					
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
		detailViewId = "AfsQuoDet";
        allocateViewId = "AfsOsOrderDet";
//        defaultStatus = "New";
		new JxFieldAction("btUpload") {
			public void actionPerformed(JxField fd){
					UniLog.log("upload Pressed");
					try {
					    Fileupload.get(new EventListener <UploadEvent>(){
				    		public void onEvent(UploadEvent event) {
				        		UniLog.log("upload event catched");
				        		SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper((HttpServletRequest) Executions.getCurrent().getNativeRequest() , (HttpServletResponse) Executions.getCurrent().getNativeResponse());
				                org.zkoss.util.media.Media media = event.getMedia();
				                if(media != null) {
				                	if(!media.getContentType().equals("application/pdf") )
				                	{
				                		messageBox("Only Pdf File Are Accepted");
				                		return;
				                	}
				                	
				                	saveImageFile(media );
				                }
				    		}
					    });
					} catch (Exception ex) {
							UniLog.log(ex);
					}
			}
		};
	}
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		if(getGipi(detailViewId) == null) {
			setGipi(detailViewId,new AfsQuoDetGetItemProperty(br.getSubLink(detailViewId)));	
		}	
		String filingView = "afs.OrdersDoc";
		if(br.getSubLink(filingView) != null) {
			setGipi(filingView,
					new BiGetItemProperty(br.getSubLink(filingView)) {
				@Override
				public void onValueChanged(Object p_value,int p_ctype) {
					ColumnCell bcc = (ColumnCell) p_value;
					/*
					if(p_ctype != BiGetItemProperty.GIPI_VALUE_CHANGED ) {
						if(bcc.getCellLabel().equals("mdoc_brandimg")) {
							setDirtyFlag(true);
						}
					}
					*/
					if(p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED && bcc.getCellLabel().equals("mdoc_download")){
						UniLog.log1("%s clicked", bcc.getCellLabel());
						ZkUtil.downloadFileFromFiling(sessionHelper,bcc.getCollection().getCell("mdoc_filekey").getString(), "download.pdf");
					}
				}
			}
			);
		}
		super.bindCellCollection(br, mode);
	}

	void saveImageFile( org.zkoss.util.media.Media media ) {
			RpcClient rpc = getRpcClient();
			Value v = rpc.callSegment("getFilingMessageId",new Vector());
			rpc.close();
			if(v != null && v.toString().startsWith("OK")) {
				int cc = Integer.parseInt(v.toString().substring(4));
				try  {
				    byte[] photoData = media.getByteData();
					Map<String, String> map = new HashMap<String, String>();
					String filekey = String.format("jxOrdersFiling_%010d_%010d",getBr().getCellInt("inv_rg"),cc);  //add stirg to key
					ByteArrayInputStream is = new ByteArrayInputStream(photoData);
				    FilingUtil.storeFile(
				      sessionHelper.getAgent(),
				      null,
				      filekey,
				      "",//mConditionPresetMapMap.customStoreName, 
				      "",//mConditionPresetMapMap.customStoreDesc, 
				    is);
				    is.close();

//				    String sfilekey = storeThumbnal(getBr().getCellInt("st_irg"), cc, photoData, map);
//					String thumbSize = map.get("data_size");
				    
				    TableRec tr = getBr().getSelectUtil().getQueryResult(
				         "select * from multidoc where mdoc_type = 'QUOM' and mdoc_mrg = '" + getBr().getCell("inv_rg").getInt() + "' order by mdoc_seq desc", null);
				    int seq = 0;
				    if(tr.getRecordCount() > 0) {
				        tr.setRecPointer(0);
				        seq = (Integer) tr.getField("mdoc_seq");
				        seq++;
				    }
				    getBr().getSelectUtil().executeUpdate("insert into multidoc (mdoc_type,mdoc_mrg,mdoc_seq,mdoc_drg,mdoc_ctime,mdoc_cuser,mdoc_doctype,mdoc_filekey,mdoc_sfilekey,mdoc_photosize,mdoc_thumbsize) values (?,?,?,?,?,?,?,?,?,?,?)", 
				        new Wherecl()
				            .appendArgument("QUOM")
				            .appendArgument(getBr().getCell("inv_rg").getInt())
				            .appendArgument(seq)
				            .appendArgument(cc)
				            .appendArgument(DateUtil.dateToUnixtime(new java.util.Date()))
				            .appendArgument(getLoginId())
				            .appendArgument(media.getContentType())
				            .appendArgument(filekey)
				            .appendArgument("")
				            .appendArgument(0)
				            .appendArgument(0)
				            );
				    
				    //patch extraimg field
				    getBr().refetchCurrent();
				    bindCellCollection(getBr(),curMode);
				    
				} catch (Exception ex) {
				    UniLog.log(ex);
				}
			}
	}
}
