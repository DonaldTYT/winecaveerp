package com.uniinformation.jxapp.hw;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Fileupload;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.Sprintf;
import com.kyoko.common.StringUtil;
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
import com.uniinformation.bicore.hw.BiResultHwInvoice;
import com.uniinformation.bicore.hw.BiResultHwOrderBase;
import com.uniinformation.bicore.hw.BiResultHwQuoDet;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
//import com.uniinformation.estimation.database.EstDb;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.JxFormCloseListener;
import com.uniinformation.jx.MessageBoxActionInterface;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.AfsIcodePicker;
import com.uniinformation.jxapp.AfsQuotation;
import com.uniinformation.jxapp.JxSelOpt;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.jxapp.erpv4.JxZkBiErpv4;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrParser.ChnftrGetImageInterface;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.ColorPickerGetItemProperty;
import com.uniinformation.utils.FilingUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.TranslateListGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiCellValueMapper;
import com.uniinformation.zkf.ZkForm;

public class HwOrderBase extends JxZkBiErpv4 {
	Window customScr = null;
	HwItemOption iof;
	protected String detViewName = null;
	boolean addInitFlag = false;
	ColumnCell nextEnterField(CellCollection col) {
		ColumnCell colc;
		return(null);
	}	
	JxActionListener quoDetailAddListener=null;
	class quoDetGetItemProperty extends BiGetItemProperty {
		Vector columnListStockItem;
		Vector columnListServiceItem;
		Vector columnListDescription;
		Vector columnListComboItem;
		Vector columnListLineBreak;
		Vector columnListPrintingItem;
		Template priceTemplate;
		Template qtyTemplate;
		Template lineBreakTemplate;
		Template statusTemplate;
		Template jobTemplate;
		Template printingTemplate;
		Template sellcodeTemplate;
		Template messageTypeTemplate;
		AfsIcodePicker jxf = null;
		quoDetGetItemProperty(final BiResult p_br) {
			super(p_br);
			columnListStockItem = new Vector<BiColumn>();
			columnListServiceItem = new Vector<BiColumn>();
			columnListDescription = new Vector<BiColumn>();
			columnListComboItem = new Vector<BiColumn>();
			columnListLineBreak = new Vector<BiColumn>();
			columnListPrintingItem= new Vector<BiColumn>();
			Vector<BiColumn> v = p_br.getListColumns();
			priceTemplate = ((Component) getNativeComponent()).getTemplate("template_AfsQuoDet_Price");
			qtyTemplate = ((Component) getNativeComponent()).getTemplate("template_AfsQuoDet_Quantity");
			lineBreakTemplate = ((Component) getNativeComponent()).getTemplate("template_AfsQuoDet_LineBreak");
			statusTemplate = ((Component) getNativeComponent()).getTemplate("template_AfsQuoDet_QdstStatus");
			jobTemplate = ((Component) getNativeComponent()).getTemplate("template_HwQuoDet_Job");
			printingTemplate = ((Component) getNativeComponent()).getTemplate("template_HwQuoDet_Printing");
			messageTypeTemplate = ((Component) getNativeComponent()).getTemplate("template_HwQuoDet_MessageType");
			sellcodeTemplate = ((Component) getNativeComponent()).getTemplate("template_SellCode");
			Vector<BiColumn> unIdentifiedColumn = new Vector<BiColumn>();
			for(BiColumn bc : v) {
				// ind_itemno is shown only for "main item" (ind_subitem = false) and is auto sequenced
				if(bc.getLabel().equals("ind_itemno")) {
					columnListStockItem.add(bc);
					columnListServiceItem.add(bc);
					columnListPrintingItem.add(bc);
					columnListComboItem.add(bc);
					columnListDescription.add(null);
					columnListLineBreak.add(null);

				// ind_desc span 4 column and is mutually exclusive with ind_ref1,ind_ref2,st_icode and st_iname
				} else if(bc.getLabel().equals("ind_desc")) {
					columnListServiceItem.add(bc);
//					columnListPrintingItem.add(bc);
					columnListDescription.add(bc);
					columnListLineBreak.add(lineBreakTemplate);
				} else if(bc.getLabel().equals("ind_linebreak")) {
//					columnListLineBreak.add(bc);
				} else if(bc.getLabel().equals("st_icode")) {
					columnListStockItem.add(bc);
				} else if(bc.getLabel().equals("stmcm_name")) {
				// set product
					columnListComboItem.add(bc);
				} else if(bc.getLabel().equals("st_iname")) {
					columnListStockItem.add(bc);
//					columnListComboItem.add(null);
					columnListComboItem.add(jobTemplate);
				} else if(bc.getLabel().equals("ind_ref1")) {
					columnListStockItem.add(bc);
//					columnListComboItem.add(null);
//					columnListPrintingItem.add(bc);
				} else if(bc.getLabel().equals("ind_ref2")) {
					columnListStockItem.add(bc);
					columnListPrintingItem.add(printingTemplate);
//					columnListComboItem.add(null);
				} else if(bc.getLabel().equals("ind_qty")) {
					columnListStockItem.add(bc);
					columnListServiceItem.add(bc);
					/*
					if(qtyTemplate == null) {
						columnListPrintingItem.add(bc);
					} else {
						columnListPrintingItem.add(qtyTemplate);
					}
					*/
					columnListComboItem.add(null);
				} else if(bc.getLabel().equals("ind_unit")) {
					columnListStockItem.add(bc);
//					columnListPrintingItem.add(bc);
//					columnListDescription.add(null);
					columnListComboItem.add(null);
				} else if(bc.getLabel().equals("ind_color")) {
					columnListServiceItem.add(bc);
				} else if(bc.getLabel().equals("ind_messagetype")) {
					columnListDescription.add(messageTypeTemplate);
//					columnListDescription.add(bc);
				// other qty/price related fields (ind_qty ind_unit ind_amount  ...) are only shown for stock or service item
				} else if(bc.getLabel().equals("ind_uprice")) {
//					Template tl = ((Component) getNativeComponent()).getTemplate("template_AfsQuoDet_Price");
					if(priceTemplate != null){
						columnListStockItem.add(priceTemplate);
						columnListServiceItem.add(priceTemplate);
						columnListPrintingItem.add(priceTemplate);
					} else {
						columnListStockItem.add(bc);
						columnListServiceItem.add(bc);
						columnListPrintingItem.add(bc);
					}
					columnListDescription.add(null);
					columnListComboItem.add(null);
				} else if(bc.getLabel().equals("ind_amount")) {
					columnListStockItem.add(bc);
					columnListServiceItem.add(bc);
//					columnListPrintingItem.add(bc);
					columnListDescription.add(null);
					columnListPrintingItem.add(bc);
				} else if(bc.getLabel().equals("ind_areas")) {
				} else if(bc.getLabel().equals("ind_setamount")) {
					columnListComboItem.add(bc);
				// ind_status is only shown for stock item
				} else if(bc.getLabel().equals("qdst_status")) {
//					columnListStockItem.add(statusTemplate);
					columnListStockItem.add(bc);
					columnListServiceItem.add(sellcodeTemplate);
					columnListPrintingItem.add(statusTemplate);
//					columnListDescription.add(null);
					columnListDescription.add(statusTemplate);
					columnListComboItem.add(null);
				} else if(bc.getLabel().equals("qdst_status2")) {
					columnListStockItem.add(bc);
					columnListServiceItem.add(null);
//					columnListPrintingItem.add(null);
					columnListDescription.add(null);
					columnListComboItem.add(null);
				} else {
					unIdentifiedColumn.add(bc);
				}
			}
			for(BiColumn bc : unIdentifiedColumn) {
					columnListStockItem.add(bc);
					columnListServiceItem.add(bc);
					columnListPrintingItem.add(bc);
					columnListComboItem.add(bc);
					columnListDescription.add(bc);
					columnListLineBreak.add(bc);
			}
		}
		@Override
		public Object getHeader(Object p_v,int p_col) {
			Object o = getListColumns(p_v).get(p_col);
			if(o instanceof BiColumn) {
				return(super.getHeader(p_v, p_col));
			}
			if(o instanceof Template) {
				if(o == priceTemplate) return("Unit Price");
				if(o == statusTemplate) return("Production Detail");
			}
			return("XXX");
		}	
		@Override
		public String getColumnWidth(Object p_v ,int p_col){
			Object o = getListColumns(p_v).get(p_col);
			if(o instanceof BiColumn) return(super.getColumnWidth(p_v, p_col));
			if(o instanceof Template) {
//				if(o == priceTemplate) return("width=90px");
				if(o == priceTemplate) return("240px");
//				if(o == statusTemplate) return("width=150px");
//				if(o == statusTemplate) return("hflex=1");
//				if(o == statusTemplate) return("width=300px");
			}
			return("");
		}	
		
		
		@Override
		public Object getColumnValueByName(final Object p_v,String p_name) {
			Object o = bigibr.getTrStatObj(p_v);
			final CellCollection col = bigibr.getRowCollectionO(o);
			BiResultQuoDet.DELTATYPE pdsrg = BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt());
			if(p_name.equals("btQdstRemove")) {
				if(
					pdsrg != BiResultHwQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION
					&& pdsrg != BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM
					) return(null);
				if( col.getCell("ind_messrg").getInt() <= 0) return(null);
				return(new JxActionListener() {
					public void actionPerformed(JxField fd){
						if (!checkBr()) return;
						UniLog.log("btQdstRemove Pressed for "+ p_v);
						try {
							FilingUtil.deleteFile(sessionHelper.getAgent(), null, 
									String.format("jxHwQuoDetFiling_%06d", col.getCell("ind_messrg").getInt()));
							col.getCell("ind_messrg").set(0);
                			col.getCell("ind_messagetype").set("");
                			col.getCell("ind_imgaspect").set(0f);
							setDirtyFlag(true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
				);
			}
			if(p_name.equals("btQdstDownload")) {
				if(
					pdsrg != BiResultHwQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION
					&& pdsrg != BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM
					) return(null);
				if( col.getCell("ind_messrg").getInt() <= 0) return(null);
				return(new JxActionListener() {
					public void actionPerformed(JxField fd){
						if (!checkBr()) return;
						UniLog.log("btQdstDownload Pressed for "+ p_v);
						int cc = col.getCell("ind_messrg").getInt();
						if( cc > 0) {
							try {
								if ( BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM)  {
//									displayImage(col);
								}
				                String contentType = col.getCell("ind_messagetype").getString();
				        		SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper((HttpServletRequest) Executions.getCurrent().getNativeRequest() , (HttpServletResponse) Executions.getCurrent().getNativeResponse());
								ByteArrayOutputStream bos = new ByteArrayOutputStream();
								FilingUtil.getFile(sessionHelper.getAgent(), null, new Sprintf("jxHwQuoDetFiling_%06d").add(cc).toString() , bos);
								bos.close();
								Filedownload.save(bos.toByteArray(), contentType, "downloadFile");
							} catch (Exception ex){
								UniLog.log(ex);
							}	
						}
					}
				}
				);
			}
			if(p_name.equals("btQdstUpload")) {
				if(
					pdsrg != BiResultHwQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION
					&& pdsrg != BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM
					) return(null);
				return(new JxActionListener() {
					public void actionPerformed(JxField fd){
						if (!checkBr()) return;
						UniLog.log("btQdstUpload Pressed for "+ p_v);
					    Fileupload.get(new EventListener <UploadEvent>(){
				    		public void onEvent(UploadEvent event) {
				        		UniLog.log("upload event catched");
				        		SessionHelper sessionHelper = ZkSessionHelper.getSessionHelper((HttpServletRequest) Executions.getCurrent().getNativeRequest() , (HttpServletResponse) Executions.getCurrent().getNativeResponse());
				                org.zkoss.util.media.Media media = event.getMedia();
				                if(media != null) {
				                	if ( BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM)  {
				                		if(!media.getContentType().equals("image/jpeg") &&
				                		   !media.getContentType().equals("image/png")) {
				                			messageBox("Only Jpeg/Png Image File Are Accepted");
				                			return;
				                		}
				                	}
				                	RpcClient rpc = getRpcClient();
				                	Value v = rpc.callSegment("getFilingMessageId",new Vector());
				                	rpc.close();
				                	if(v != null && v.toString().startsWith("OK")) {
				                		int cc = Integer.parseInt(v.toString().substring(4));
				                		try  {
				                			byte[] data = media.getByteData();
				                			//InputStream is = media.getStreamData();
				                			InputStream is = new ByteArrayInputStream(data);
				                			FilingUtil.storeFile(
				                					sessionHelper.getAgent(),
				                					null,
				                					new Sprintf("jxHwQuoDetFiling_%06d").add(cc).toString(),
				                					"",//mConditionPresetMapMap.customStoreName, 
				                					"",//mConditionPresetMapMap.customStoreDesc, 
				                					is);
				                			is.close();
				                			if ( BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM)  {
				                				is = new ByteArrayInputStream(data);
				                				BufferedImage image = ImageIO.read(is);
				                				int imgWidth = image.getWidth();
				                				int imgHeight = image.getHeight();
				                				float aspect = (float)imgWidth / imgHeight;
				                				is.close();
				                				UniLog.log("image aspect:" + aspect + ",width:" + imgWidth + ",height:" + imgHeight);
				                				col.getCell("ind_imgaspect").set(aspect);
				                			}
				                			col.getCell("ind_messrg").set(cc);
				                			col.getCell("ind_messagetype").set(media.getContentType());
											setDirtyFlag(true);
				                		} catch (Exception ex) {
				                			UniLog.log(ex);
				                		}
				                	}
				                }
				    		}
					    });
						
					}
				}
				);
			}
			if(p_name.equals("btQdstDetail")) {
				if(
						pdsrg != BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM
						) return(null);
				return(new JxActionListener() {
					public void actionPerformed(JxField fd){
						if (!checkBr()) return;
						UniLog.log("btQdstDetail Pressed for "+ p_v);
						if(customScr == null) {
							customScr = newPopupWindow("Production Detail");
							customScr.setWidth("1400px");
							customScr.setHeight("800px");
                			customScr.setContentStyle("overflow:auto;");
							SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
							JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
							iof= (HwItemOption) getOrCreateJxZkForm(customScr,pvdr ,"hw.HwItemOption");
							
							iof.addFormCloseListener(
								new JxFormCloseListener( ) {
									public int formClose(JxForm jxf) {
										return(JxFormCloseListener.caHide);
									}
								}	
							);
							customScr.setVisible(false);
							
						}
						String preOpt = null;
						{ 
							Vector<BiCellCollection> v = bigibr.getRowCollectionList();
							int cidx = v.indexOf(col);
							for(int i=cidx-1;i>=0;i--) {
								if(BiResultQuoDet.getDeltaType(getSessionHelper(),v.get(i).getCell("ind_pdsrg").getInt()) == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM) {
									preOpt = v.get(i).getCell("ind_options").getString();
									if(preOpt != null && !preOpt.equals("")) break;
								}
							}
//							if(cidx > 1) {
//								preOpt = v.get(cidx-1).getCell("ind_options").getString();
//							}
						}
						if(iof.setCellCollection(col, getBr().getSelectUtil(),preOpt)) {
							customScr.doModal();
							setDirtyFlag(true);
						}
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
				if(bc.getLabel().equals("stmcm_name")) return(1);
//				if(bc.getLabel().equals("ind_linebreak")) return(9);
			}
			if(o instanceof Template) {
				if(o == priceTemplate) return(1);
				if(o == statusTemplate) return(2);
				if(o == lineBreakTemplate) return(11);
				if(o == jobTemplate) return(3);
				if(o == printingTemplate) return(6);
				if(o == messageTypeTemplate) return(2);
				if(o == qtyTemplate) return(1);
			}
			return(1);
		}
		@Override
		protected Vector getListColumns(Object p_v) {
			if(p_v == null ) return(columnListStockItem);
			Object o = bigibr.getTrStatObj(p_v);
			CellCollection col = bigibr.getRowCollectionO(o);
			BiResultQuoDet.DELTATYPE pdsrg = BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt());
			if(pdsrg == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM ) {
				return(columnListStockItem);
			} else if(pdsrg == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_SERVICE_ITEM ) {
				return(columnListServiceItem);
			} else if(pdsrg == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM ) {
				return(columnListPrintingItem);
			} else if(pdsrg == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_COMBO_ITEM ) {
				return(columnListComboItem);
			} else if(pdsrg == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_LINEBREAK) {
				return(columnListLineBreak);
			} else {
				return(columnListDescription);
			}
		}	
		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			ColumnCell bcc = (ColumnCell) p_value;
			switch(p_ctype ) {
			case GIPI_VALUE_ONOK:
			{
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " ok");
				CellCollection col = bcc.getCollection();
				ColumnCell nextCell = nextEnterField(col);
				if(nextCell == null) {
					BiResult sr = getBr().getSubLink(detViewName);
					Vector<BiCellCollection> colList = sr.getRowCollectionList();
					int idx = colList.indexOf(col);
					if(idx < colList.size()-1) {
						BiResultQuoDet.DELTATYPE pdsrg = BiResultQuoDet.getDeltaType(getSessionHelper(),colList.get(idx+1).getCell("ind_pdsrg").getInt());
						switch(pdsrg) {
						case DELTALTYPE_COMBO_ITEM :
							nextCell = ((ColumnCell) colList.get(idx+1).getCell("stmcm_name"));
							break;
						case DELTALTYPE_PRINTING_ITEM :
							nextCell = ((ColumnCell) colList.get(idx+1).getCell("ind_ref1"));
							break;
						case DELTALTYPE_DESCRIPTION :
							nextCell = ((ColumnCell) colList.get(idx+1).getCell("ind_desc"));
							break;
						case DELTALTYPE_SERVICE_ITEM:
							nextCell = ((ColumnCell) colList.get(idx+1).getCell("ind_desc"));
							break;
						}
					} else {
						listboxAddRow(HwOrderBase.this, sr, jxAdd("list_"+JxZkBiBase.replaceViewName(detViewName)), null, -1);
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
				break;
			case GIPI_PULLDOWN_OPENED:
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " opened ");
				if(bcc.getBiColumn().getLabel().equals("st_icode"))  {
					try {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						if(jxf == null) {
							SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
							JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
							jxf = (AfsIcodePicker) getOrCreateJxZkForm(new Idspace(),pvdr ,"AfsIcodePicker");
						}
//						zjpi.setPopupWidth("700px");
//						zjpi.setPopupHeight("500px");
						zjpi.setJxZkForm(jxf);
//						AfsIcodePicker jxf = (AfsIcodePicker) zjpi.getJxZkForm();
						CellCollection col = bcc.getCollection();
						Wherecl wcl = null;
						int srg = col.getCell("ind_srg").getInt();
						if(srg > 0) {
							wcl = new Wherecl().appendString(" and st_irg in (select mcfm_mrg from mcfitmodel where mcfm_modelrg = " + srg + ") ");
						}
						jxf.setPickerForAnyStock(HwOrderBase.this,getBr().getSelectUtil(), wcl,bcc,null,null);
						setDirtyFlag(true);
					} catch (Exception ex) {
						UniLog.log(ex);
					}	
				}
				if(bcc.getBiColumn().getLabel().equals("ind_postano"))  {

					BiResult thisBr = getBr().getView().getSchema().getViewByName("hw.HwService").newBiResult( getLoginId(), null, null, sessionHelper);
					thisBr.query();
						
					final CellCollection thiscol = bcc.getCollection();
					final JxSelOpt selopt = getPopupSelOpt();
					selopt.setOnSelectAction (
						new JxActionListener() {
							public void actionPerformed(JxField fd) {
								if (!checkBr()) return;
								Object o  = fd.getValue();
								BiGetItemProperty gipi = (BiGetItemProperty) selopt.getUserData();
								CellCollection col = gipi.getCellCollectionByValue(o);
								try {
									thiscol.getCell("ind_postano").set(col.getCell("st_mszrange").getString());
								} catch (CellException cex) {
									UniLog.log(cex);
								}
								selopt.closeForm();
							}
						}
					);
					
					BiGetItemProperty gipi = new BiGetItemProperty(thisBr);
					gipi.setItemMode(BiGetItemProperty.GETITEM_MODE_PICK);
					selopt.jxAdd("pickListBox").setItemListInterface( gipi);
					selopt.setUserData(gipi);
					selopt.setPopupWidth("500px");
					selopt.modalForm();
					
				}
				break;
			case GIPI_CELL_MAPPED :
				CellCollection col = bcc.getCollection();
				if(BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_SERVICE_ITEM) {
					if(bcc.getBiColumn().getLabel().equals("ind_desc")) {
						JxZkBiBase.setFocusComponent(bcc, parentComp);
					}	
				}
				if(BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION) {
//						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
//						Component comp = zcvm.getComponent();
//						if(comp instanceof Textbox)  {
//							((Textbox) comp).setRows(3);
//						}
					if(bcc.getBiColumn().getLabel().equals("ind_desc")) {
						JxZkBiBase.setFocusComponent(bcc, parentComp);
					}	
				}
				if(BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM) {
					if(bcc.getBiColumn().getLabel().equals("ind_ref1")) {
						JxZkBiBase.setFocusComponent(bcc, parentComp);
					}
				}
				break;
			default :
						setDirtyFlag(true);
				break;
			}
		}
	}
	@Override
	public void afterBind() {
		super.afterBind();
		detViewName = "hw.QuoDet";
	new JxFieldAction("btConfirmOdr") {
		public void actionPerformed(JxField fd){
			if (!checkBr()) return;
			String s = getBr().getCell("inv_quostatus").getString();
			if(!s.equals("New") && !s.equals("Revised") && !s.equals("Void") && !s.equals("UnConfirm") && !s.equals("Revised")) {
				messageBox("Cannot Confirm " + s + " Order ");
				return;
			} 
			confirm("Do you want to confirm this order ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
							try {
								if(((Integer) p_obj).intValue() == 1) {
								String s= getBr().getCell("inv_quostatus").getString();
								getBr().getCell("inv_quostatus").set("Confirmed");
								BiResultHwQuoDet sr = (BiResultHwQuoDet) getBr().getSubLink(detViewName);
								Vector<BiCellCollection> v = sr.getRowCollectionList();
								String lastOptions = "";
								for(BiCellCollection cc : v) {
									if(BiResultQuoDet.getDeltaType(getSessionHelper(),cc.getCell("ind_pdsrg").getInt())==BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM) {
										String ss = cc.getCell("ind_options").getString();
										if(ss != null && !ss.trim().equals("")) lastOptions = ss;
										int irg = sr.optionGetPrinterId(lastOptions);
										cc.getCell("ind_irg").set(irg);
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
//	new JxFieldChange("vd_vname") {
//		public boolean valueChanged(JxField fd, String p_value) {
//			try {
//				TableRec tr = getBr().getSelectUtil().getQueryResult("select * from vendor where vd_vcode = '"+getBr().getCell("inv_vcode")+"'", null);
//				if(tr.getRecordCount() > 0) {
//					tr.setRecPointer(0);
//					String s0 = null;
////					String s = tr.getFieldString("vd_contact");
////					getBr().getCell("inv_contact").set(s);
//					String s = tr.getFieldString("vd_tel");
//					if(!StringUtils.isBlank(s)) {
//						if(s0 != null) s0 += "/"; else s0 = "";
//						s0 += s;
//					}
//					s = tr.getFieldString("vd_telex");
//					if(!StringUtils.isBlank(s)) {
//						if(s0 != null) s0 += "/"; else s0 = "";
//						s0 += s;
//					}
//					s = tr.getFieldString("vd_cable");
//					if(!StringUtils.isBlank(s)) {
//						if(s0 != null) s0 += "/"; else s0 = "";
//						s0 += s;
//					}
//					getBr().getCell("inv_tel").set(s0);
//				}
//			} catch (Exception ex) {
//				UniLog.log(ex);
//			}
//			return(true);
//		}
//	};
	new JxFieldAction("btVoidOdr") {
		public void actionPerformed(JxField fd){
			if (!checkBr()) return;
			String s = getBr().getCell("inv_quostatus").getString();
			/*
			if(!s.equals("Confirmed")) {
				messageBox("Cannot UnConfirm " + s + " Order ");
				return;
			} 
			*/
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
	new JxFieldAction("btUnConfirmOdr") {
		public void actionPerformed(JxField fd){
			if (!checkBr()) return;
			String s = getBr().getCell("inv_quostatus").getString();
			if(!s.equals("Confirmed")) {
				messageBox("Cannot UnConfirm " + s + " Order ");
				return;
			} 
								{
								BiResultHwQuoDet sr = (BiResultHwQuoDet) getBr().getSubLink(detViewName);
								Vector<BiCellCollection> v = sr.getRowCollectionList();
								for(BiCellCollection cc : v) {
									if(cc.getCell("ind_linked").getInt() > 0) {
										messageBox("Invoice Genereted, Cannot Unconfirm");
										return;
									}
								}
								}
			confirm("Do you want to unconfirm this order ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
							try {
								if(((Integer) p_obj).intValue() == 1) {
								String s= getBr().getCell("inv_quostatus").getString();
								getBr().getCell("inv_quostatus").set("UnConfirm");

								BiResultHwQuoDet sr = (BiResultHwQuoDet) getBr().getSubLink(detViewName);
								Vector<BiCellCollection> v = sr.getRowCollectionList();
								for(BiCellCollection cc : v) {
									if(BiResultQuoDet.getDeltaType(getSessionHelper(),cc.getCell("ind_pdsrg").getInt())==BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM) {
										cc.getCell("ind_irg").set(0);
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
	
	/*
	new JxFieldChange("vd_vname") {
		public boolean valueChanged(JxField fd,String p_orgValue) {
			try {
				getBr().getCell("inv_deliloc").set(0);
				TableRec tr = getBr().getSelectUtil().getQueryResult("select svloc_desp from sv_loc where svloc_custcode = '" + getBr().getCell("inv_vcode").getString() + "'", null);
				Vector v = new Vector();
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					v.add(tr.getField("svloc_desp"));
				}
				getBr().getCell("svloc_desp").setItemList(v);
			} catch (Exception cex) {
				UniLog.log(cex);
			}
			return(true);
		}
	};
	*/
	
	new JxFieldAction("btCopyOdr") {
		public void actionPerformed(JxField fd){
			if (!checkBr()) return;
			confirm("Do you want to copy this order ?", 
					new MessageBoxActionInterface() {
						public void onButtonClicked( Object p_obj) {
							try {
								if(((Integer) p_obj).intValue() == 1) {
								RpcClient rpc = getRpcClient();
								java.util.Date d = DateUtil.today();
//								String s = ((BiResultHwQuotation) br).getNewOrderNumber(d);
								String s = "";
								Value val = rpc.callSegment("copyQuotation",
											new VectorUtil()
											.addElement(getBr().getCell("inv_rg").getInt())
											.addElement(s)
											.addElement(d)
											.toVector()
										);
								if(val != null && val.toString().startsWith("OK")) {
//									String fname = val.toString().substring(4);
									int invrg = Integer.parseInt(StringUtil.strpart(val.toString(),4,10).trim());
									String ordnum = StringUtil.strpart(val.toString(),14,15);
									String quonum = StringUtil.strpart(val.toString(),29,15);
									UniLog.log("Order copied to " + invrg + " " + ordnum + " " + quonum);
									messageBox("Order copied to " + ordnum + "/" + quonum);
									needRefreshFlag = true;
									setDirtyFlag(false);

//									BiView view = getBr().getView();
					                getBr().clearCondition();
//					                getBr().addCustomCondition("inv_invno = '" + fname + "'");
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
									messageBox("Order copy failed " + val == null ? "Reason Unknown" : val.toString());
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
	new JxFieldAction("inv_quodeli inv_assignto") {
		public void actionPerformed(JxField fd){
			if (!checkBr()) return;
			if(fd.getActionType() == JxField.ACTIONTYPE_PICKINPUTOPENED) {
				ZkJxPickInput pi = (ZkJxPickInput) fd.getNativeObject();
//				pickPresetStringByBiColumn( (ColumnCell) getBr().getCell(fd.getName()),pi);
				if(fd.getName().equals("inv_quodeli")) {
					jxZkBiPickPresetString( (ColumnCell) getBr().getCell(fd.getName()),pi,"DTO");
				}
				if(fd.getName().equals("inv_assignto")) {
					jxZkBiPickPresetString( (ColumnCell) getBr().getCell(fd.getName()),pi,"AST");
				}
			}
		}
	};
	LOCK_RECORD_FOR_UPDATE = true;
	}
	/*
	private void resetMode0() throws CellException {
		if(jxAdd("inv_contact").getFieldMode() == Cell.VMODE_OVERRIDED) {
				jxAdd("inv_contact").setFieldMode(Cell.VMODE_PROTECTED);
		}
		if(jxAdd("inv_tel").getFieldMode() == Cell.VMODE_OVERRIDED) {
				jxAdd("inv_tel").setFieldMode(Cell.VMODE_PROTECTED);
		}
		if(jxAdd("inv_fax").getFieldMode() == Cell.VMODE_OVERRIDED) {
			jxAdd("inv_fax").setFieldMode(Cell.VMODE_PROTECTED);
		}
	}
	*/
	
	@Override
	protected ReturnMsg beforeAddLink(JxField fd,BiResult sr,CellCollection cl,int p_insIdx) 
	{
//		return(getBr().doBeforeAdd(cl));
		if(sr.getView().getName().equals(detViewName)) {
			int actionIdx = -1;
			if(fd != null) {
				actionIdx = (Integer)ZkUtil.getAttributeInt((Component) fd.getNativeObject(), "actionIdx", 3, -1); 
				UniLog.logm(this,"field id = %s %s idx %d",fd.getName(),((Component) fd.getNativeObject()).getId(),actionIdx);
			}
			if(getBr().getCell("inv_quostatus").getString().equals("Confirmed")) {
				return(new ReturnMsg(false,"Cannot add confirmed order details"));
			}
			try  {
			if(fd == jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM))) {
					cl.getCell("ind_pdsrg").set( BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM ));
				}
				if(fd == jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_SERVICE_ITEM))) {
					cl.getCell("ind_pdsrg").set( BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_SERVICE_ITEM ));
				}
				if(fd == jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION))) {
					cl.getCell("ind_pdsrg").set( BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_DESCRIPTION));
				}
				if(fd == jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_COMBO_ITEM))) {
					cl.getCell("ind_pdsrg").set( BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_COMBO_ITEM));
				}
				if(fd == jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_LINEBREAK))) {
					cl.getCell("ind_pdsrg").set( BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_LINEBREAK));
				}
				if(fd == null || fd == jxAdd("btAddQuoDet_"+BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM))) {
					cl.getCell("ind_pdsrg").set( BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM));
				}
				switch(BiResultQuoDet.getDeltaType(getSessionHelper(),cl.getCell("ind_pdsrg").getInt())){
				case DELTALTYPE_STOCK_ITEM:
				case DELTALTYPE_SERVICE_ITEM:
				case DELTALTYPE_LINEBREAK:
				case DELTALTYPE_PRINTING_ITEM:
				case DELTALTYPE_DESCRIPTION:
					if(actionIdx < 0 && fd == null) actionIdx = sr.getRowCount()-1;
					CellCollection lastComboCol = null,lastPrintingCol = null;
					for(int i = actionIdx; i>= 0;i--) {
						if(!sr.isMarkedDelete(i)) {
							CellCollection col = sr.getRowCollectionV(i);
							if(BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM) {
								if(lastPrintingCol == null) lastPrintingCol = col;
							}
							if(BiResultQuoDet.getDeltaType(getSessionHelper(),col.getCell("ind_pdsrg").getInt()) == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_COMBO_ITEM) {
								lastComboCol = col;
								break;
							}
						}
					}
					if(BiResultQuoDet.getDeltaType(getSessionHelper(),cl.getCell("ind_pdsrg").getInt()) == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM) {
						if(lastComboCol == null) {
							return(new ReturnMsg(false,"Printing Item Should Follow Job Item"));
						}
					}
					if(BiResultQuoDet.getDeltaType(getSessionHelper(),cl.getCell("ind_pdsrg").getInt()) == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM) {
					cl.getCell("ind_roundmode").set(getBr().getCell("inv_roundmode").getInt());
					if(lastPrintingCol != null) {
						cl.getCell("ind_usize1").set(lastPrintingCol.getCell("ind_usize1").getDouble());
						cl.getCell("ind_usize2").set(lastPrintingCol.getCell("ind_usize2").getDouble());
						cl.getCell("ind_size1").set(lastPrintingCol.getCell("ind_size1").getDouble());
						cl.getCell("ind_size2").set(lastPrintingCol.getCell("ind_size2").getDouble());
						cl.getCell("ind_bleed1").set(lastPrintingCol.getCell("ind_bleed1").getDouble());
						cl.getCell("ind_bleed2").set(lastPrintingCol.getCell("ind_bleed2").getDouble());
						cl.getCell("ind_bleed1r").set(lastPrintingCol.getCell("ind_bleed1r").getDouble());
						cl.getCell("ind_bleed2r").set(lastPrintingCol.getCell("ind_bleed2r").getDouble());
						cl.getCell("ind_ref1").set(lastPrintingCol.getCell("ind_ref1").getString());
						cl.getCell("ind_unit").set(lastPrintingCol.getCell("ind_unit").getString());
						cl.getCell("ind_qty").set(lastPrintingCol.getCell("ind_qty").getDouble());
						cl.getCell("ind_puprice").set(lastPrintingCol.getCell("ind_puprice").getDouble());
						cl.getCell("ind_punit").set(lastPrintingCol.getCell("ind_punit").getString());
						cl.getCell("ind_uprice").sync(lastPrintingCol.getCell("ind_uprice").getDouble());
//						cl.getCell("ind_options").set(lastPrintingCol.getCell("ind_options").getString());
					} else {
						cl.getCell("ind_qty").set(1.0);
						cl.getCell("ind_unit").set("mm");
					}
					} else {
						if(cl.testCell("ind_puprice") != null) cl.getCell("ind_puprice").setMode(Cell.VMODE_HIDDEN);
						if(cl.testCell("mur_name") != null) cl.getCell("mur_name").setMode(Cell.VMODE_HIDDEN);
					}
					if(lastComboCol != null) {
							cl.getCell("ind_subitem").set("Y");
							cl.getCell("ind_itemno").setMode(Cell.VMODE_HIDDEN);
//							cl.getCell("ind_amount").setMode(Cell.VMODE_HIDDEN);
							cl.getCell("ind_srg").set(lastComboCol.getCell("ind_srg").getInt());
					}
					if( BiResultQuoDet.getDeltaType(getSessionHelper(),cl.getCell("ind_pdsrg").getInt()) == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_SERVICE_ITEM) {
							cl.getCell("ind_unit").set("Pcs");
//							cl.getCell("ind_color").setItemPropertyInterface(
//									new ColorPickerGetItemProperty()
//							);
//							cl.getCell("ind_color").setItemList(
//									new VectorUtil()
//									.addElement("Black")
//									.addElement("Red")
//									.addElement("Blue")
//									.addElement("Green")
//									.toVector()
//									);
//							cl.getCell("ind_color").setItemPropertyInterface(
//									new TranslateListGetItemProperty(
//											new VectorUtil()
//											.addElement("Black")
//											.addElement("Red")
//											.addElement("Blue")
//											.addElement("Green")
//											.toVector()
//											) {
//										public String translate(Object p_item) {
//											return((String) p_item);
//										};
//										@Override 
//										public Object getRow(int p_row) {
//											switch(p_row) {
//											case 1: return(new Integer(0xff0000));
//											case 2: return(new Integer(0x00ff00));
//											case 3: return(new Integer(0x0000ff));
//											default : return(new Integer(0));
//											}
//										}
//									}	
//							);
							
					}
//					if(actionIdx >= 0) {
//						CellCollection col = sr.getRowCollectionV(actionIdx);
//						if(col.getCell("ind_pdsrg").getInt() == BiResultHwQuoDet.DELTALTYPE_COMBO_ITEM
//								|| col.getCell("ind_subitem").getString().equals("Y")) {
//							cl.getCell("ind_subitem").set("Y");
//							cl.getCell("ind_itemno").setMode(Cell.VMODE_HIDDEN);
//							cl.getCell("ind_amount").setMode(Cell.VMODE_HIDDEN);
//						}
//						/*
//						if(cl.getCell("ind_pdsrg").getInt() == BiResultHwQuoDet.DELTALTYPE_PRINTING_ITEM
//						   && col.getCell("ind_pdsrg").getInt() == BiResultHwQuoDet.DELTALTYPE_PRINTING_ITEM) {
//							cl.getCell("ind_usize1").set(col.getCell("ind_bleed1").getDouble());
//							cl.getCell("ind_usize2").set(col.getCell("ind_bleed2").getDouble());
//						}
//						*/
//							cl.getCell("ind_bleed1").set(col.getCell("ind_bleed1").getDouble());
//							cl.getCell("ind_bleed2").set(col.getCell("ind_bleed2").getDouble());
//							cl.getCell("ind_bleed1r").set(col.getCell("ind_bleed1r").getDouble());
//							cl.getCell("ind_bleed2r").set(col.getCell("ind_bleed2r").getDouble());
//					}
				}
//				cl.getCell("ind_optdesc").set(BiResultHwQuoDet.optionToDesc(cl.getCell("ind_options").getString()));
				if(addInitFlag && (BiResultQuoDet.getDeltaType(getSessionHelper(),cl.getCell("ind_pdsrg").getInt()) == BiResultHwQuoDet.DELTATYPE.DELTALTYPE_PRINTING_ITEM)) {
//					Component comp = (Component) jxAdd("inv_vcode").getNativeObject();
//					((HtmlBasedComponent) comp).focus();
					addInitFlag = false;
				}
			} catch (CellException cex) {
				UniLog.log(cex);
				return(new ReturnMsg(false,cex.toString()));
			}
		}
		return(null);
	}
	@Override
	protected ReturnMsg beforeUpdateLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals(detViewName)) {
			if(getBr().getCell("inv_quostatus").getString().equals("Confirmed")) {
				return(new ReturnMsg(false,"Cannot update confirmed order details"));
			}
		}
		return(null);
	}
	@Override
	protected ReturnMsg beforeDeleteLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals(detViewName)) {
		if(getBr().getCell("inv_quostatus").getString().equals("Confirmed")) {
			return(new ReturnMsg(false,"Cannot delete confirmed order details"));
		}
		}
		return(null);
	}
	
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		if(getGipi(detViewName) == null) {
			setGipi(detViewName,new quoDetGetItemProperty(br.getSubLink(detViewName)));	
		}
		super.bindCellCollection(br, mode);
		if(curMode == JxZkBiBase.MODE_ADD) {
			try {
				br.getCell("inv_date").set(DateUtil.today());
				br.getCell("inv_quostatus").update("New");
				if(br.getCell("inv_assignby") != null) br.getCell("inv_assignby").update(getLoginId());
				br.getCell("inv_cid").update("HKD");
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		}
		setupActionButton(false);
		JxField amountDiv = jxAdd("quoamountinfo");
		if(amountDiv != null) {
			if(!BiSchema.hasAccessRight(br.getSessionHelper(), "quoshowp")) {
				amountDiv.setVisible(false);
			}
		}
	}
	
	void setupActionButton(boolean is_dirty)
	{
	}
	
	@Override 
	protected void afterAddLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals(detViewName)) {
			try {
			((BiResultHwOrderBase) getBr()).real_calTotalAmount();
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		}
	}
	@Override 
	protected void afterUnDeleteLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals(detViewName)) {
			try {
			((BiResultHwOrderBase) getBr()).real_calTotalAmount();
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		}
	}
	@Override 
	protected void afterDeleteLink(BiResult sr,int idx)
	{
		if(sr.getView().getName().equals(detViewName)) {
			try {
			((BiResultHwOrderBase) getBr()).real_calTotalAmount();
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		}
	}
	
//	public void displayImage(CellCollection col) throws Exception {
//		int cc = col.getCell("ind_messrg").getInt();
//		if( cc <= 0) return;
//		String contentType = col.getCell("ind_messagetype").getString();
//		SessionHelper sessionHelper = SessionHelper.getSessionHelper((HttpServletRequest) Executions.getCurrent().getNativeRequest() , (HttpServletResponse) Executions.getCurrent().getNativeResponse());
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		FilingUtil.getFile(sessionHelper.getAgent(), null, new Sprintf("jxHwQuoDetFiling_%06d").add(cc).toString() , bos);
//		bos.close();
//		ByteArrayInputStream ios = new ByteArrayInputStream(bos.toByteArray());
//		AImage aimg = new AImage("HAHA",ios);
//		ios.close();
//		
//	}

	@Override 
	protected void formDirtyChanged() {
		super.formDirtyChanged();
		setupActionButton(isDirty());
	}
	
	class QuotationGetItemProperty extends BiGetItemProperty {
		QuotationGetItemProperty(BiResult p_br) {
			super(p_br);
		}
		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			ColumnCell bcc = (ColumnCell) p_value;
			if(p_ctype != GIPI_CELL_MAPPED) {;
				setDirtyFlag(true);
			} else {
			}
			if(p_ctype == GIPI_PULLDOWN_CLOSED) {
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " closed ");
			}
			if(p_ctype == GIPI_PULLDOWN_OPENED) {
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " opened ");
				ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
				ZkJxPickInput zkpi = (ZkJxPickInput) zcvm.getComponent();
				if(zkpi.isOpen()) {
//					jxZkBiPickPresetString(bcc,zkpi);
					UniLog.log("HAHA xxx");
				}
			} 
		}
	}
}
