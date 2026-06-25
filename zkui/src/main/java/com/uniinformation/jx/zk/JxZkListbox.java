package com.uniinformation.jx.zk;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zss.api.Range;
import org.zkoss.zss.api.Ranges;
import org.zkoss.zss.ui.Spreadsheet;
import org.zkoss.zul.Auxhead;
import org.zkoss.zul.Auxheader;
import org.zkoss.zul.Bandbox;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Html;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listfoot;
import org.zkoss.zul.Listfooter;
import org.zkoss.zul.Listhead;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Menuseparator;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Timer;
import org.zkoss.zul.impl.InputElement;
import org.zkoss.zul.impl.LabelElement;
import org.zkoss.zul.impl.XulElement;

import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.StringWithClass;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueMapper;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxChangeListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.kyoko.common.ChineseConvert;
import com.uniinformation.utils.BiUtil;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.MapUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;
import com.uniinformation.zkbi.ZkBiCellValueMapper;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiSearchHelper;
import com.uniinformation.zkbi.ZkBiTranslateHelper;
import com.uniinformation.zkcomp.S2Listbox;
import com.uniinformation.zkcomp.ZkBiHeaderLabel;

public class JxZkListbox extends JxZkElement {
	static final int ROW_ATTR_DELETED = 1;
	static final int ROW_ATTR_UPDATED = 2;
	static final int ROW_ATTR_INSERTED= 4;
   	final static String IMG_DELETE = "images/Delete_24x24.png";
   	final static String IMG_UPDATE = "images/Update_24x24.png";
   	final static String IMG_ADD = "images/Add_24x24.png";
   	final static String IMG_SINGLESELECT = "images/single24.png";
   	final static int BLUR_PROCESSING_DELAY = 100; //100ms

   	String colAlign = "center";
   	
   	/* onDemandLoad currently only work with GIPI */
   	boolean loadOnDemand = false;
   	int onDemandLoadCount= 200;
   	int onDemandIdx = 0;
   	
	Listbox listbox;
//	Vector <Cell[]> rows;
	List<Component> editRow;
//	HashMap <Component,ZkJxCellValueMapper> cellMapHash;
	int nCols = 0;
	int currcol = 0;
	Vector itemList;
	ListModelList listModelList;
	
	HashMap <Integer,Set <Integer>> rowAttrHM; //key by row
	
	boolean toogleModeFlag = false;
	boolean doubleClick = true;
	boolean canDelete = false;
	boolean canInsert = false;
	boolean canUpdate = false;
	
//    java.util.Date currSelectTime = null;
//    int lastSelectIdx = -1;
    
	int editingRow = -1;
	int firstEditCol = -1;
	boolean checkMarkCol=false;
	JxChangeListener changeListener = null;

	AbstractGetItemProperty gipi;
	String appliedFilter = null;
	Hlayout filterLayout = null;
	Textbox filterInput=null;;
	Paging pg = null;
	Auxhead auxHeadForFooterLayout = null;
	Hlayout footerLayout = null;
//	EventListener gipiChangeListener;
	Popup addPu = null;
	SessionHelper sh = null;
	Map tttMap = null; //for handle tooltiptext
	boolean isMobile = false;
	Vector<MobileHeader> mobileHeader;
	class MobileHeader {
		String hdrStr;
		void setHeader(Object p_obj) {
		if (p_obj instanceof String){
			hdrStr = (String) p_obj;
		}
		else if (p_obj instanceof Map){
			BiColumn col = (BiColumn) MapUtil.getObject(p_obj, "biColumn");
			BiResult biResult = (BiResult) MapUtil.getObject(p_obj, "biResult");
			if (col.getColumnType().trim().equals("button")) {
				hdrStr = "";
			}
			else {
				hdrStr = (ZkBiTranslateHelper.getText(sh, col.getCellFullName(), "LABEL", MapUtil.getString(p_obj, "label","")));
			}
		}
		}
	}
	
	void rendorOneRowByGipiNew(int p_idx,Listitem p_listItem,Object p_data,boolean deletedFlag) throws Exception
	{
						int n = gipi.getColumnCount(p_data);
						Listcell lco = null;
//						Vlayout vl = null;
						Rows rRows = null;
						for(int i=0;i<n;i++) {
							HtmlBasedComponent lcx;
							if(isMobile) {
								if(lco == null) {
									lco = new Listcell();
									lco.setPage(p_listItem.getPage());
									/*
									int s = gipi.getColumnSpan(p_data,i);
									if(s > 1) lco.setSpan(s);
									vl = new Vlayout();
									lco.appendChild(vl);
									*/
									Grid rGrid = new Grid();
									rRows = new Rows();
									rGrid.appendChild(rRows);

									Columns columns = new Columns();
									Column labelColumn = new Column();
									labelColumn.setAlign("right");
//									labelColumn.setWidth("100px");
									labelColumn.setHflex("min");
									Column textColumn = new Column();
									textColumn.setAlign("left");
									textColumn.setHflex("1");
									columns.appendChild(labelColumn);
									columns.appendChild(textColumn);
									rGrid.appendChild(columns);
									
									
									lco.appendChild(rGrid);
									p_listItem.appendChild(lco);
									
								}
								/*
								lcx = new Div();
								vl.appendChild(lcx);
								*/
								lcx = new Row();
								rRows.appendChild(lcx);
							} else {
								lco = new Listcell();
								lcx = lco;
								lco.setPage(p_listItem.getPage());
								int s = gipi.getColumnSpan(p_data,i);
								if(s > 1) lco.setSpan(s);
							}
							Object o = gipi.getColumnValue(p_data, i);
							if( o instanceof ColumnCell) {
								if(isMobile) {
									org.zkoss.zul.Cell lcol = new org.zkoss.zul.Cell();
									if(mobileHeader.size() > i && mobileHeader.get(i).hdrStr!= null ) {
										Label lb=new Label(mobileHeader.get(i).hdrStr);
										lb.setSclass("zkbi-col-label");
										lcol.appendChild(lb);
									} else {
//										lcol.appendChild(new Label(mobileHeader.get(i).header.toString()));
									}
									((Row) lcx).appendChild(lcol);
									lcol = new org.zkoss.zul.Cell();
									((Row) lcx).appendChild(lcol);
									lcx = lcol;
								}
								BiColumn col = ((ColumnCell) o).getBiColumn();
								Component cb = JxZkBiBase.createComponentByBiColumn(col);
								if (cb instanceof InputElement){
									((InputElement)cb).setWidth("100%"); //for listbox item, width should be controlled by header
									if(col.isReadOnly()) {
										((InputElement)cb).setReadonly(true);  //not work well for bandbox/combobox
									} else ((InputElement)cb).setReadonly(deletedFlag);  //not work well for bandbox/combobox
									ColumnCell cc = (ColumnCell)o;
									if (cc.getType() == Cell.VTYPE_INT || cc.getType() == Cell.VTYPE_DOUBLE) {
										JxZkBiBase.setComponentFormat(cb, cc.getBiColumn() , false, sh);
									}
									String alignValue=BiUtil.extractColDecorationValue(cc.getBiColumn().getDecoration(), "calign");
									if (StringUtils.isNotBlank(alignValue))
										ZkUtil.appendStyle((XulElement)cb, "text-align:" + alignValue);
								}
								else{
									JxZkBiBase.setComponentFormat(cb, ((ColumnCell) o).getBiColumn() , false, sh);
								}
								lcx.appendChild(cb);  //andrew211101 place appendChild before map to fix comp not found zk.Widget.$('#xxxxx')
								((ColumnCell) o).map(new ZkBiCellValueMapper(cb,gipi));
								
								if(cb instanceof Label) {
									int align = gipi.getColumnAlignment(i);
									if(align != 0) {
										if(isMobile) {
											ZkUtil.appendStyle(lcx, "text-align:left;");
										} else if (align > 0) {
											ZkUtil.appendStyle(lcx, "text-align:" + ((align & 4) != 0 ? "center" : "left") + ";");
										} else {
											ZkUtil.appendStyle(lcx, "text-align:right;");
										}
										if(((align > 0 ? align : -align) & 2) != 0) {
											ZkUtil.appendStyle(lcx, "word-wrap:word-break");
										}
										if(((align > 0 ? align : -align) & 8) != 0) {
											ZkUtil.appendStyle(lcx, "white-space:nowrap");
										}
									}	
									final String linkurl = ((ColumnCell) o).getBiResult().getLinkedUrl(
												((Cell) o).getCellLabel(),
												((ColumnCell) o).getCollection()
												);
									if(linkurl != null) {
										final JSONObject jo = ((ColumnCell) o).getBiResult().getLinkedCondition(((Cell) o).getCellLabel(),  (ColumnCell) o);
								   			try {
								   				Toolbarbutton visitViewBt = new Toolbarbutton();  
								    			visitViewBt.setTooltiptext(sh.getTtLabel("Visit view"));
								    			visitViewBt.setIconSclass("z-icon-link");
												visitViewBt.setSclass("narrowtoolbarbutton");
								   				visitViewBt.setStyle("vertical-align:top;");
								   				visitViewBt.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
													@Override
													public void onZkBiEvent(Event p_event) throws Exception {
														UniLog.logm(this, "clicked");
														String key = sh.putOneTimeData( jo);
														String url = linkurl + "&querycondition="+key;
														if(p_event instanceof MouseEvent) {
															MouseEvent me = (MouseEvent) p_event;
															if((me.getKeys() & me.CTRL_KEY) != 0) {
																ZkUtil.js("openNewTab('"+url+"')");
//																Executions.sendRedirect(url);
															} else {
																Executions.sendRedirect(url);
															}
														} else {
															Executions.sendRedirect(url);
														}
														//TODO implement visit view feature
													}}
								   				);
								   				Popup popup = new Popup();
								    			popup.appendChild(visitViewBt);
								    			popup.setParent(listbox.getRoot());
								    			((Label) cb).setTooltip(popup);
								    			ZkUtil.appendStyle((Label) cb, "color:rgb(26,13,171);");	
								   			} catch (Exception ex) {
								   				UniLog.log(ex);
								   			}	
									}
								} else ZkUtil.appendStyle(lcx, "text-align:left;");
								
								if (cb instanceof Radiogroup) {
									if (sh.getAllowOptionTranslate()) {
										for (Component crd : cb.queryAll("Radio")) {
											Radio rd = (Radio)crd;
											String defaultValue = StringUtils.defaultString(rd.getLabel());
											String key = ((ColumnCell)o).getBiResult().getView().getName() + "." + ((ColumnCell)o).getCellLabel() + "." + rd.getValue();
											if (sh.getAllowUpdateTranslate())
												JxZkBiBase.addContextMenu(sh, (XulElement) rd, MapUtil.of("changeLabel", MapUtil.of("key",key,"defaultValue",defaultValue)));
											if (sh.getAllowTranslate())
												rd.setLabel(ZkBiTranslateHelper.getText(sh, key, "OPTION", defaultValue));
										}
									}
								}
							} else if( o instanceof Template) {
								if(isMobile) {
									org.zkoss.zul.Cell lcol = new org.zkoss.zul.Cell();
									lcol.setColspan(2);
									lcx.appendChild(lcol);
									lcx = lcol;
								}
								Template tl = tl = (Template) o;
								Component carr[];
								carr = tl.create(lcx, null, null, null);
								ArrayList<String> ccList = new ArrayList<String>();
								Object nativeObject = gipi.getColumnNativeObject(p_data, tl);
								for(Component c : carr) {
									traverseGetGipiValues(c, p_data,p_idx,deletedFlag,ccList);
									if(nativeObject instanceof Cell) {
										((Cell) nativeObject).mapAdd(new ZkBiCellValueMapper(c,gipi));
									}
								}
								gipi.setColumnCellList(i, ccList);
								ZkUtil.appendStyle(lcx, "text-align:left;");
							} else {
								if(o != null) {
									String strClass = null;
									if(o instanceof StringWithClass) {
										strClass = ((StringWithClass) o).strClass();
										o = ((StringWithClass) o).toString();
									}
									if(isMobile) {
										org.zkoss.zul.Cell lcol = new org.zkoss.zul.Cell();
										if(mobileHeader.size() > i && mobileHeader.get(i).hdrStr!= null ) {
											Label lb=new Label(mobileHeader.get(i).hdrStr);
											lb.setSclass("zkbi-col-label");
											lcol.appendChild(lb);
										} else {
//											lcol.appendChild(new Label(mobileHeader.get(i).header.toString()));
										}
										((Row) lcx).appendChild(lcol);
										lcol = new org.zkoss.zul.Cell();
										((Row) lcx).appendChild(lcol);
										lcx = lcol;
									}
									/* text-color class should be set on label, not work if set on listcell because it will be overrided by default color setting in the XulLabel component */
									if(lcx instanceof LabelElement && strClass == null) {
										((LabelElement) lcx).setLabel(o.toString());
									} else {
										Label lb = new Label(o.toString());
										lcx.appendChild(lb);
										lb.setSclass(strClass);
									}
									int align = gipi.getColumnAlignment(i);
									if(align != 0) {
										if(isMobile) {
											ZkUtil.appendStyle(lcx, "text-align:left;");
										} else if (align > 0) {
											ZkUtil.appendStyle(lcx, "text-align:" + ((align & 4) != 0 ? "center" : "left") + ";");
										} else {
											ZkUtil.appendStyle(lcx, "text-align:right;");
										}
										if(((align > 0 ? align : -align) & 2) != 0) {
											ZkUtil.appendStyle(lcx, "word-wrap:word-break");
										}
										if(((align > 0 ? align : -align) & 8) != 0) {
											ZkUtil.appendStyle(lcx, "white-space:nowrap");
										}
									}
									if(!isMobile) {
										final String linkurl = gipi.getLinkedUrl(p_data, i);
										if(linkurl != null) {
											final JSONObject jo = gipi.getLinkedCondition(p_data, i);
								   			try {
								   				Toolbarbutton visitViewBt = new Toolbarbutton();  
								    			visitViewBt.setTooltiptext(sh.getTtLabel("Visit view"));
								    			visitViewBt.setIconSclass("z-icon-link");
												visitViewBt.setSclass("narrowtoolbarbutton");
								   				visitViewBt.setStyle("vertical-align:top;");
								   				visitViewBt.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
													@Override
													
													public void onZkBiEvent(Event p_event) throws Exception {
														UniLog.logm(this, "clicked");
														String key = sh.putOneTimeData( jo);
														String url = linkurl + "&querycondition="+key;
														if(p_event instanceof MouseEvent) {
															MouseEvent me = (MouseEvent) p_event;
															if((me.getKeys() & me.CTRL_KEY) != 0) {
																ZkUtil.js("openNewTab('"+url+"')");
															} else {
																Executions.sendRedirect(url);
															}
														} else {
															Executions.sendRedirect(url);
														}
														//TODO implement visit view feature
													}}
								   				);
								   				Popup popup = new Popup();
								    			popup.appendChild(visitViewBt);
								    			popup.setParent(listbox.getRoot());
								    			if(lcx instanceof XulElement) ((XulElement) lcx).setTooltip(popup);
								    			ZkUtil.appendStyle(lcx, "color:rgb(26,13,171);");	
								   			} catch (Exception ex) {
								   				UniLog.log(ex);
								   			}	
										}
									}
								}
							}
							if(!isMobile) {
								p_listItem.appendChild(lco);
								if (deletedFlag){
									ZkUtil.appendStyle(lco, "background:pink !important;");
//									lc.setStyle("text-align:left;background:pink !important;");
								}
								else{
//									lc.setStyle("text-align:left");
								}
							}
						}
							if(isMobile) {
								Row lcx = new Row();
								org.zkoss.zul.Cell lcol = new org.zkoss.zul.Cell();
								lcol.setColspan(2);
								lcol.setAlign("left");
								lcx.appendChild(lcol);
								Vlayout trailer = new Vlayout();

								boolean allowDeleteFlag = true;
								boolean insertedFlag = false;
								Hlayout perRecMenu = new Hlayout();
								trailer.appendChild(perRecMenu);

								Div seperator = new Div();
								seperator.setHeight("5px");
								ZkUtil.appendSclass(seperator, "z-listheader");
								trailer.appendChild(seperator);
								

						if(canInsert) {
							Toolbarbutton b = new Toolbarbutton(); 
    						//b.setIconSclass("z-icon-pencil-square-o z-icon-2x");
    						b.setIconSclass("z-icon-plus-square z-icon-2x");
    						b.setSclass("narrowtoolbarbutton");
							b.setTooltiptext(sh.getTtLabel("Insert Item"));
							b.setAttribute("isAddButton", true);
							b.addEventListener(Events.ON_CLICK,insBtnListener);
							if (deletedFlag){
								//b.setDisabled(true);
								b.setStyle("opacity:0.7;color:red;");
							}
							else if (insertedFlag){
								b.setStyle("opacity:0.7;color:limegreen");
							}
							else{
								b.setStyle("opacity:0.7");
							}
							//b.setId("btupdate_list_row" + p_idx + "_" + listbox.getId());
							if (gipi != null){
								b.setAttribute("gipiData", p_data);
							}
							perRecMenu.appendChild(b);
						}
						if(canDelete && allowDeleteFlag) {
							Toolbarbutton b = new Toolbarbutton(); 
    						b.setIconSclass("z-icon-trash-o z-icon-2x");
    						b.setSclass("narrowtoolbarbutton");
    						b.setAttribute("JxZkListbox.deleteItemButton","Y");
							b.setTooltiptext(sh.getTtLabel("Delete Item"));
							if (deletedFlag){
								b.setStyle("opacity:0.7;color:red");
							}
							else if (insertedFlag){
								b.setStyle("opacity:0.7;color:limegreen");
							}
							else{
								b.setStyle("opacity:0.7");
							}
							b.addEventListener(Events.ON_CLICK,delBtnListener);
							if (gipi != null){
								b.setAttribute("gipiData", p_data);
							}
							perRecMenu.appendChild(b);
						}
								lcol.appendChild(trailer);
								rRows.appendChild(lcx);
								if (deletedFlag){
									ZkUtil.appendStyle(lco, "background:pink !important;");
//									lc.setStyle("text-align:left;background:pink !important;");
								}
								else{
//									lc.setStyle("text-align:left");
								}
							}
	}
	void rendorOneRowByGipi(int p_idx,Listitem p_listItem,Object p_data,boolean deletedFlag) throws Exception
	{
//					getSkin.hasFellow("detail_grid",true)
//					Template tl = null;
//					if(gipi.getItemId(p_data) != null ) {
//						Component rootWin =(Component) getSkin().getNativeComponent();
//						tl  = rootWin.getTemplate( "template_"+gipi.getItemId(p_data));
//					}
//					if(tl != null) {
//						Listcell lc = new Listcell();
//						Component carr[];
//						carr = tl.create(lc, null, null, null);
//						for(Component c : carr) {
//							traverseGetGipiValues(c, p_data,p_idx);
//						}
//						p_listItem.appendChild(lc);
//					} else {
						int n = gipi.getColumnCount(p_data);
						for(int i=0;i<n;i++) {
							Listcell lc = new Listcell();
							lc.setPage(p_listItem.getPage());
							int s = gipi.getColumnSpan(p_data,i);
							if(s > 1) lc.setSpan(s);
							
							Object o = gipi.getColumnValue(p_data, i);
							if( o instanceof ColumnCell) {
								BiColumn col = ((ColumnCell) o).getBiColumn();
								Component cb = JxZkBiBase.createComponentByBiColumn(col);
								/*
								//obsoleted
								if (cb instanceof Button){
									//sepcial component button, add onClick event listener
									cb.setAttribute("rowBtnLabel", col.getLabel());
									cb.setAttribute("rowBtnCol", i);
									cb.setAttribute("rowBtnRow", p_idx);
									cb.addEventListener("onClick", rowBtnListener);
								}
								*/
								if (cb instanceof InputElement){
									((InputElement)cb).setWidth("100%"); //for listbox item, width should be controlled by header
									if(col.isReadOnly()) {
										((InputElement)cb).setReadonly(true);  //not work well for bandbox/combobox
									} else ((InputElement)cb).setReadonly(deletedFlag);  //not work well for bandbox/combobox
									ColumnCell cc = (ColumnCell)o;
									if (cc.getType() == Cell.VTYPE_INT || cc.getType() == Cell.VTYPE_DOUBLE) {
										JxZkBiBase.setComponentFormat(cb, cc.getBiColumn() , false, sh);
									}
									String alignValue=BiUtil.extractColDecorationValue(cc.getBiColumn().getDecoration(), "calign");
									if (StringUtils.isNotBlank(alignValue))
										ZkUtil.appendStyle((XulElement)cb, "text-align:" + alignValue);
								}
								else{
									JxZkBiBase.setComponentFormat(cb, ((ColumnCell) o).getBiColumn() , false, sh);
								}
								lc.appendChild(cb);  //andrew211101 place appendChild before map to fix comp not found zk.Widget.$('#xxxxx')
								((ColumnCell) o).map(new ZkBiCellValueMapper(cb,gipi));
								//ColumnCell cc = (ColumnCell)o;
								//UniLog.log("rec " + cc.getType() + "," + cc.getBiColumn().getFormat() + "," + cc.getBiColumn().getEngName() + "," + cb + "," + (cb instanceof InputElement));
								/*
								if(cb instanceof S2Listbox) {
									//Component lcc = cb.getChildren().get(0);
									//ZkUtil.setupSelect2(lcc);
									//ZkUtil.setupSelect2(((S2Listbox) cb).getComp());  //andrew211101 seems not required, already done in ZkBiCellValueMapper.cellMap_listchange()
								}
								*/
								
								if(cb instanceof Label) {
									int align = gipi.getColumnAlignment(i);
									if(align != 0) {
										if(align > 0) {
											ZkUtil.appendStyle(lc, "text-align:" + ((align & 4) != 0 ? "center" : "left") + ";");
										} else {
											ZkUtil.appendStyle(lc, "text-align:right;");
										}
										if(((align > 0 ? align : -align) & 2) != 0) {
											ZkUtil.appendStyle(lc, "word-wrap:word-break");
										}
									}	
									final String linkurl = ((ColumnCell) o).getBiResult().getLinkedUrl(
												((Cell) o).getCellLabel(),
												((ColumnCell) o).getCollection()
												);
									if(linkurl != null) {
										final JSONObject jo = ((ColumnCell) o).getBiResult().getLinkedCondition(((Cell) o).getCellLabel(),  (ColumnCell) o);
								   			try {
								   				Toolbarbutton visitViewBt = new Toolbarbutton();  
								    			visitViewBt.setTooltiptext(sh.getTtLabel("Visit view"));
								    			visitViewBt.setIconSclass("z-icon-link");
												visitViewBt.setSclass("narrowtoolbarbutton");
								   				visitViewBt.setStyle("vertical-align:top;");
								   				visitViewBt.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
													@Override
													public void onZkBiEvent(Event p_event) throws Exception {
														UniLog.logm(this, "clicked");
														String key = sh.putOneTimeData( jo);
														String url = linkurl + "&querycondition="+key;
														if(p_event instanceof MouseEvent) {
															MouseEvent me = (MouseEvent) p_event;
															if((me.getKeys() & me.CTRL_KEY) != 0) {
																ZkUtil.js("openNewTab('"+url+"')");
//																Executions.sendRedirect(url);
															} else {
																Executions.sendRedirect(url);
															}
														} else {
															Executions.sendRedirect(url);
														}
														//TODO implement visit view feature
													}}
								   				);
								   				Popup popup = new Popup();
								    			popup.appendChild(visitViewBt);
								    			popup.setParent(listbox.getRoot());
								    			((Label) cb).setTooltip(popup);
								    			ZkUtil.appendStyle((Label) cb, "color:rgb(26,13,171);");	
								   			} catch (Exception ex) {
								   				UniLog.log(ex);
								   			}	
									}
								} else ZkUtil.appendStyle(lc, "text-align:left;");
								
								if (cb instanceof Radiogroup) {
									if (sh.getAllowOptionTranslate()) {
										for (Component crd : cb.queryAll("Radio")) {
											Radio rd = (Radio)crd;
											String defaultValue = StringUtils.defaultString(rd.getLabel());
											String key = ((ColumnCell)o).getBiResult().getView().getName() + "." + ((ColumnCell)o).getCellLabel() + "." + rd.getValue();
											if (sh.getAllowUpdateTranslate())
												JxZkBiBase.addContextMenu(sh, (XulElement) rd, MapUtil.of("changeLabel", MapUtil.of("key",key,"defaultValue",defaultValue)));
											if (sh.getAllowTranslate())
												rd.setLabel(ZkBiTranslateHelper.getText(sh, key, "OPTION", defaultValue));
										}
									}
								}
							} else if( o instanceof Template) {
								Template tl = tl = (Template) o;
								Component carr[];
								carr = tl.create(lc, null, null, null);
								ArrayList<String> ccList = new ArrayList<String>();
								Object nativeObject = gipi.getColumnNativeObject(p_data, tl);
								for(Component c : carr) {
									traverseGetGipiValues(c, p_data,p_idx,deletedFlag,ccList);
									if(nativeObject instanceof Cell) {
										((Cell) nativeObject).mapAdd(new ZkBiCellValueMapper(c,gipi));
									}
								}
								gipi.setColumnCellList(i, ccList);
								ZkUtil.appendStyle(lc, "text-align:left;");
							} else {
								if(o != null) {
									lc.setLabel(o.toString());
									int align = gipi.getColumnAlignment(i);
									if(align != 0) {
										if(align > 0) {
											ZkUtil.appendStyle(lc, "text-align:" + ((align & 4) != 0 ? "center" : "left") + ";");
										} else {
											ZkUtil.appendStyle(lc, "text-align:right;");
										}
										if(((align > 0 ? align : -align) & 2) != 0) {
											ZkUtil.appendStyle(lc, "word-wrap:word-break");
										}
									}
									{
										final String linkurl = gipi.getLinkedUrl(p_data, i);
										if(linkurl != null) {
											final JSONObject jo = gipi.getLinkedCondition(p_data, i);
								   			try {
								   				Toolbarbutton visitViewBt = new Toolbarbutton();  
								    			visitViewBt.setTooltiptext(sh.getTtLabel("Visit view"));
								    			visitViewBt.setIconSclass("z-icon-link");
												visitViewBt.setSclass("narrowtoolbarbutton");
								   				visitViewBt.setStyle("vertical-align:top;");
								   				visitViewBt.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
													@Override
													
													public void onZkBiEvent(Event p_event) throws Exception {
														UniLog.logm(this, "clicked");
														String key = sh.putOneTimeData( jo);
														String url = linkurl + "&querycondition="+key;
														if(p_event instanceof MouseEvent) {
															MouseEvent me = (MouseEvent) p_event;
															if((me.getKeys() & me.CTRL_KEY) != 0) {
																ZkUtil.js("openNewTab('"+url+"')");
															} else {
																Executions.sendRedirect(url);
															}
														} else {
															Executions.sendRedirect(url);
														}
														//TODO implement visit view feature
													}}
								   				);
								   				Popup popup = new Popup();
								    			popup.appendChild(visitViewBt);
								    			popup.setParent(listbox.getRoot());
								    			lc.setTooltip(popup);
								    			ZkUtil.appendStyle(lc, "color:rgb(26,13,171);");	
								   			} catch (Exception ex) {
								   				UniLog.log(ex);
								   			}	
										}
									}
								}
							}
							p_listItem.appendChild(lc);
							if (deletedFlag){
								ZkUtil.appendStyle(lc, "background:pink !important;");
//								lc.setStyle("text-align:left;background:pink !important;");
							}
							else{
//								lc.setStyle("text-align:left");
							}
						}
//					}
	}
	void rendorOneRowByCellArray(int p_idx, Cell[] rec,Listitem p_listItem,EventListener tbChangeListener,boolean deletedFlag) throws Exception
	{
				final List<Component> compList = new ArrayList<Component>();
				firstEditCol = -1;
				for(int i = 0;i<rec.length;i++)  {
					p_listItem.setAttribute("renderidx", p_idx);
					//UniLog.logm(this,"listbox Render idx:%d i:%d rec.length:%d", p_idx, i, rec.length);
					if(rec[i] != null) {
						Listcell lc;
						if(editingRow == p_idx) {
							Listhead lh = listbox.getListhead();
							lc = new Listcell();
							final Cell fc = rec[i];
							if(editRow == null) {
								editRow = new ArrayList <Component>();
							}
							final Component tb;
							if(editRow.size() <= i) {
								if(rec[i].getType() == Cell.VTYPE_DATE) {
									tb = new Datebox();
									((Datebox) tb).setFormat("yy/MM/dd");
								} 
								else if(rec[i].getType() == Cell.VTYPE_BOOLEAN) {
										tb = new Checkbox();
								} 
								else if(rec[i].getType() == Cell.VTYPE_INT) {
										tb = new Textbox();
								} 
								else if(rec[i].getType() == Cell.VTYPE_DOUBLE) {
										tb = new Textbox();
								} 
								else {
									Vector <String> li = rec[i].getItemList();
									if(li == null) {
										if(rec[i] instanceof ColumnCell) {
											if(((ColumnCell) rec[i]).getBiColumn().getColumnType().equals("combobox")) {
												tb = new Combobox();
											} else if(((ColumnCell) rec[i]).getBiColumn().getColumnType().equals("list")) {
												/*
												if(JxZkBiBase.useS2Listbox)
													tb = new S2Listbox();
												else
													tb = new Combobox();
												*/
												if(sh.useS2ListboxForReadOnly()) {
													if(((ColumnCell) rec[i]).getBiColumn().isReadOnly()) {
														if(BiConfig.useS2Listbox(sh) || sh.getURLParamBool("s2")) {
															tb = new S2Listbox();
														} else {
															tb = new Listbox();
															((Listbox) tb).setMold("select");
														}
													} else {
														tb = new Combobox();
													}
												} else {
												if(BiConfig.useS2Listbox(sh) || sh.getURLParamBool("s2"))
													tb = new S2Listbox();
												else
													tb = new Combobox();
												}
//											} else if(((ColumnCell) rec[i]).isLookup() && ((ColumnCell) rec[i]).getColumnType().equals("list")){
											} else if(((ColumnCell) rec[i]).getBiColumn().getColumnType().equals("pickinput")){
												tb = new ZkJxPickInput();
												tb.addEventListener("onOpen",new ZkBiEventListener() {
													public void onZkBiEvent(Event event) throws Exception {	
														UniLog.log("ZkJxPickInput isOpened = " + ((ZkJxPickInput) tb).isOpen());
														if(actionListener != null) {
															if(((ZkJxPickInput) tb).isOpen()) {
																lastActionType = JxField.ACTIONTYPE_PICKINPUTOPENED;
															} else {
																lastActionType = JxField.ACTIONTYPE_PICKINPUTCLOSED;
															}
															currcol = editRow.indexOf(tb);
															actionListener.actionPerformed(getJxField());
															lastActionType = 0;
														}
													}
												});
												tb.addEventListener("onClick",new ZkBiEventListener() {
													public void onZkBiEvent(Event event) throws Exception {	
														UniLog.log("ZkJxPickInput click event trapped and ignored");
														
													}
												});
//												((ZkJxPickInput)tb).setOnClickListener(new ZkBiEventListener(){
//									    		    public void onZkBiEvent(Event event) throws Exception {
//									    		    	/*//obsoleted
//														if(blurFocusDelayTimer.isRunning()) blurFocusDelayTimer.setRunning(false);
//														lastEventTime = new Date();
//														*/
//														int idx = editRow.indexOf(tb);
//														if(idx >= 0) {
//															currcol = idx;
//															lastActionType = JxField.ACTIONTYPE_EDITCELLPICKED;
//															actionListener.actionPerformed(getJxField());
//															lastActionType = 0;
//														}
//									    		    }
//											    });
											}
											else {
												tb = new Textbox();
											}
										} 
										else {
											tb = new Textbox();
										}
									}
									else {
										tb = new Combobox();
										for(String listr : li) {
											((Combobox) tb).appendItem(listr);
										}
									}
								}
								if(tb instanceof InputElement) {
									//((InputElement) tb).setHflex("max"); //set hflex to max no effect, it does not occupy the space.
									((InputElement) tb).setHflex("true");
									((InputElement) tb).setCtrlKeys("#up#down");
									((InputElement) tb).setInstant(true); //set instance  by default
									tb.addEventListener(Events.ON_CHANGE, tbChangeListener);
								} else {
									tb.addEventListener(Events.ON_CHECK, tbChangeListener);
								}
								EventListener<Event> el = 
										new ZkBiEventListener<Event>() {
											public void onZkBiEvent(Event event) throws Exception {
												UniLog.log("ListBox EditBox enter pressed");
												if(editingRow >= 0) {
													int si = listbox.getSelectedIndex();
													if(si == editingRow ) {
														listModelList.removeFromSelection(listModelList.get(si));
													}
													listbox.clearSelection();
													listModelList.set(editingRow, listModelList.get(editingRow));
													editingRow = -1;
												}
											}		
										};
								tb.setAttribute("eventlistener_ok0", new AbstractMap.SimpleEntry<EventListener<Event>, Boolean>(el, true));
								tb.addEventListener(Events.ON_OK, el);
								tb.addEventListener(Events.ON_CTRL_KEY,
										new ZkBiEventListener() {
											public void onZkBiEvent(Event event) throws Exception {
												UniLog.log("ListBox EditBox control key pressed" + ((KeyEvent) event).getKeyCode());
												int keyCode = ((KeyEvent) event).getKeyCode();
												if(keyCode == KeyEvent.UP || keyCode == KeyEvent.DOWN) {
													if(editingRow >= 0) {
														
														int si = listbox.getSelectedIndex();
														UniLog.logm(this,"si:%d editingRow:%d", si, editingRow);
														/*
														//si seems not important, ignore this validation
														if (si < 0 || si != editingRow){
															UniLog.logm(this,"ignore invalid si");
															return;
														}
														*/
														int nextRowOffset = 0;
														if (keyCode == KeyEvent.UP){
															nextRowOffset = -1;
														}
														else if (keyCode == KeyEvent.DOWN){
															nextRowOffset = 1;
														}
														
														int nextEditingRow = getNextEditingRow(editingRow, nextRowOffset);
														UniLog.logm(this,"si:%d editingRow:%d nextRowOffset:%d nextEditingRow:%d ", si, editingRow, nextRowOffset, nextEditingRow);
														if (nextEditingRow < 0){
															UniLog.logm(this,"ignore, nextEditingRow < 0");
															return;
														}
														//remove edit component
														//listModelList.removeFromSelection(listModelList.get(si));
														listModelList.removeFromSelection(listModelList.get(editingRow));
														
														//re-render editingRow and nextEditingRow
														listModelList.set(editingRow, listModelList.get(editingRow));
														listModelList.set(nextEditingRow, listModelList.get(nextEditingRow));
														
														listbox.setSelectedIndex(nextEditingRow);
														editingRow = nextEditingRow;
													}
												}
											}		
										}
									);
								editRow.add(i,tb);
//								cellMapHash.put(tb, new ZkJxCellValueMapper(tb));
								tb.setAttribute("CellValueMapper", new ZkJxCellValueMapper(tb));
								if(actionListener != null) {
									currcol = i;
									lastActionType = JxField.ACTIONTYPE_EDITCELLREALIZED;
									actionListener.actionPerformed(getJxField());
									lastActionType = 0;
								}
							} 
							else {
								tb = editRow.get(i);
							}
							if(rec[i].getMode() == Cell.VMODE_DISPONLY) {
								if(tb instanceof InputElement)
									((InputElement) tb).setDisabled(true);
								else if(tb instanceof Checkbox)
									((Checkbox) tb).setDisabled(true);
							} 
							else {
								if(firstEditCol < 0) {
									firstEditCol = i;
								}
							}
							Listheader lhdr = (Listheader) lh.getChildren().get(i + (checkMarkCol ? 1:0));
							lc.appendChild(tb);
//							rec[i].map(cellMapHash.get(tb));
							rec[i].map(((ZkJxCellValueMapper) tb.getAttribute("CellValueMapper")));
							if (i == firstEditCol){
								UniLog.log("HAHA delayExecution");
								new ZkBiAbstractLongOp(comp,null){
									@Override
									public ReturnMsg longOp() {
										if(tb instanceof InputElement){
											UniLog.log("delayExecute: textbox focus");
											((InputElement) tb).focus();
										}
										return null;
									}
									
								};
								/*
								//delayExecute replaced by ZkBiAbstractLongOp
								ZkUtil.delayExecute(comp, 1, new ZkBiEventListener() {
									public void onZkBiEvent(Event event) throws Exception {
										if(tb instanceof InputElement){
											UniLog.log("delayExecute: textbox focus");
											((InputElement) tb).focus();
										}
									}
								});
								*/
							}
							compList.add(tb);
						}
						else {
							//test base64 image
							//rec[i].set("data:image/gif;base64,R0lGODlhPQBEAPeoAJosM//AwO/AwHVYZ/z595kzAP/s7P+goOXMv8+fhw/v739/f+8PD98fH/8mJl+fn/9ZWb8/PzWlwv///6wWGbImAPgTEMImIN9gUFCEm/gDALULDN8PAD6atYdCTX9gUNKlj8wZAKUsAOzZz+UMAOsJAP/Z2ccMDA8PD/95eX5NWvsJCOVNQPtfX/8zM8+QePLl38MGBr8JCP+zs9myn/8GBqwpAP/GxgwJCPny78lzYLgjAJ8vAP9fX/+MjMUcAN8zM/9wcM8ZGcATEL+QePdZWf/29uc/P9cmJu9MTDImIN+/r7+/vz8/P8VNQGNugV8AAF9fX8swMNgTAFlDOICAgPNSUnNWSMQ5MBAQEJE3QPIGAM9AQMqGcG9vb6MhJsEdGM8vLx8fH98AANIWAMuQeL8fABkTEPPQ0OM5OSYdGFl5jo+Pj/+pqcsTE78wMFNGQLYmID4dGPvd3UBAQJmTkP+8vH9QUK+vr8ZWSHpzcJMmILdwcLOGcHRQUHxwcK9PT9DQ0O/v70w5MLypoG8wKOuwsP/g4P/Q0IcwKEswKMl8aJ9fX2xjdOtGRs/Pz+Dg4GImIP8gIH0sKEAwKKmTiKZ8aB/f39Wsl+LFt8dgUE9PT5x5aHBwcP+AgP+WltdgYMyZfyywz78AAAAAAAD///8AAP9mZv///wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAACH5BAEAAKgALAAAAAA9AEQAAAj/AFEJHEiwoMGDCBMqXMiwocAbBww4nEhxoYkUpzJGrMixogkfGUNqlNixJEIDB0SqHGmyJSojM1bKZOmyop0gM3Oe2liTISKMOoPy7GnwY9CjIYcSRYm0aVKSLmE6nfq05QycVLPuhDrxBlCtYJUqNAq2bNWEBj6ZXRuyxZyDRtqwnXvkhACDV+euTeJm1Ki7A73qNWtFiF+/gA95Gly2CJLDhwEHMOUAAuOpLYDEgBxZ4GRTlC1fDnpkM+fOqD6DDj1aZpITp0dtGCDhr+fVuCu3zlg49ijaokTZTo27uG7Gjn2P+hI8+PDPERoUB318bWbfAJ5sUNFcuGRTYUqV/3ogfXp1rWlMc6awJjiAAd2fm4ogXjz56aypOoIde4OE5u/F9x199dlXnnGiHZWEYbGpsAEA3QXYnHwEFliKAgswgJ8LPeiUXGwedCAKABACCN+EA1pYIIYaFlcDhytd51sGAJbo3onOpajiihlO92KHGaUXGwWjUBChjSPiWJuOO/LYIm4v1tXfE6J4gCSJEZ7YgRYUNrkji9P55sF/ogxw5ZkSqIDaZBV6aSGYq/lGZplndkckZ98xoICbTcIJGQAZcNmdmUc210hs35nCyJ58fgmIKX5RQGOZowxaZwYA+JaoKQwswGijBV4C6SiTUmpphMspJx9unX4KaimjDv9aaXOEBteBqmuuxgEHoLX6Kqx+yXqqBANsgCtit4FWQAEkrNbpq7HSOmtwag5w57GrmlJBASEU18ADjUYb3ADTinIttsgSB1oJFfA63bduimuqKB1keqwUhoCSK374wbujvOSu4QG6UvxBRydcpKsav++Ca6G8A6Pr1x2kVMyHwsVxUALDq/krnrhPSOzXG1lUTIoffqGR7Goi2MAxbv6O2kEG56I7CSlRsEFKFVyovDJoIRTg7sugNRDGqCJzJgcKE0ywc0ELm6KBCCJo8DIPFeCWNGcyqNFE06ToAfV0HBRgxsvLThHn1oddQMrXj5DyAQgjEHSAJMWZwS3HPxT/QMbabI/iBCliMLEJKX2EEkomBAUCxRi42VDADxyTYDVogV+wSChqmKxEKCDAYFDFj4OmwbY7bDGdBhtrnTQYOigeChUmc1K3QTnAUfEgGFgAWt88hKA6aCRIXhxnQ1yg3BCayK44EWdkUQcBByEQChFXfCB776aQsG0BIlQgQgE8qO26X1h8cEUep8ngRBnOy74E9QgRgEAC8SvOfQkh7FDBDmS43PmGoIiKUUEGkMEC/PJHgxw0xH74yx/3XnaYRJgMB8obxQW6kL9QYEJ0FIFgByfIL7/IQAlvQwEpnAC7DtLNJCKUoO/w45c44GwCXiAFB/OXAATQryUxdN4LfFiwgjCNYg+kYMIEFkCKDs6PKAIJouyGWMS1FSKJOMRB/BoIxYJIUXFUxNwoIkEKPAgCBZSQHQ1A2EWDfDEUVLyADj5AChSIQW6gu10bE/JG2VnCZGfo4R4d0sdQoBAHhPjhIB94v/wRoRKQWGRHgrhGSQJxCS+0pCZbEhAAOw==");
							
							//if base64 image string, render it as image
							if (rec[i].getString() != null && rec[i].getString().startsWith("data:image/")){
								lc = new Listcell();
								String base64Image = rec[i].getString().split(",")[1];
								byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64Image);
								AImage aImage = new AImage("", imageBytes);
								Image image = new Image();
								image.setContent(aImage);
								image.setParent(lc);
							}
							else{
								lc = new Listcell(rec[i].getString());
							}
						}
						
						if (deletedFlag){
							//lc.setStyle("text-align:left;text-decoration:line-through;color:red !important;");
							lc.setStyle("text-align:left;background:pink !important;");
						}
						else{
							lc.setStyle("text-align:left");
						}
						lc.setParent(p_listItem);	
					}
				}
				if (!compList.isEmpty()) {
					UniLog.log("start collectAllTabComps");
					final JxZkBiBase.ComponentsFocusManage cfm = new JxZkBiBase.ComponentsFocusManage();
					cfm.collectAllTabComps(p_listItem);
					cfm.addComponent(compList);
					if (cfm.process())
						cfm.setFocusNextComp();
					for (HtmlBasedComponent c : cfm.getComponentList()) {
						Object elo_ok0 = c.getAttribute("eventlistener_ok0");
						Object elo_ok1 = c.getAttribute("eventlistener_ok1");
						if (elo_ok0 != null && elo_ok1 != null) {
							final Map.Entry<EventListener<Event>, Boolean> el_ok0 = (Map.Entry<EventListener<Event>, Boolean>) elo_ok0;
							final Map.Entry<EventListener<Event>, Boolean> el_ok1 = (Map.Entry<EventListener<Event>, Boolean>) elo_ok1;
							if (c.getAttribute("eventlistener_focus0") != null)
								c.removeEventListener(Events.ON_FOCUS, (EventListener<Event>)c.getAttribute("eventlistener_focus0"));
							EventListener<Event> el_focus = new ZkBiEventListener<Event>(){
								@Override
								public void onZkBiEvent(Event event) throws Exception {
									HtmlBasedComponent c1 = (HtmlBasedComponent) event.getTarget();
									UniLog.logm(this, "on_focus c1:" + c1 + ",islast:" + cfm.isLastCanFocusComponent(c1) + ",value:" + el_ok0.getValue());
									if (cfm.isLastCanFocusComponent(c1)) {
										if (!el_ok0.getValue()) {
											c1.addEventListener(Events.ON_OK, el_ok0.getKey());
											el_ok0.setValue(true);
										}
									} else {
										if (el_ok0.getValue()) {
											c1.removeEventListener(Events.ON_OK, el_ok0.getKey());
											el_ok0.setValue(false);
										}
									}
								}
							};
							c.setAttribute("eventlistener_focus0", el_focus);
							c.addEventListener(Events.ON_FOCUS, el_focus);
						}
					}
				}
				/*
				if(doubleClick)
					p_listItem.addEventListener(Events.ON_DOUBLE_CLICK, zkEventListener);
				else 
					p_listItem.addEventListener(Events.ON_CLICK, zkEventListener);
					*/
	}
		final EventListener delBtnListener = 
											new ZkBiEventListener() {
												public void onZkBiEvent(Event event) throws Exception {
													UniLog.log("delBtnListener entered");
													int actionIdx = getActionIdx(event, gipi, listbox);
													if (actionIdx >= 0){
														if(appliedFilter == null ) {
															listbox.setSelectedIndex(actionIdx);
														} else {
															int idx = listbox.getIndexOfItem((Listitem)event.getTarget().getParent().getParent());
															listbox.setSelectedIndex(idx);
														}
													}
													if(actionListener != null) {
														lastActionType = JxField.ACTIONTYPE_DELETE;
														actionListener.actionPerformed(getJxField());
														lastActionType = 0;
														
													}
												}		
											};
		final EventListener insBtnListener = 
											new ZkBiEventListener() {
												public void onZkBiEvent(Event event) throws Exception {
													UniLog.logm(this,"insBtnListener entered");
													int actionIdx = getActionIdx(event, gipi, listbox);
													if (actionIdx >= 0){
														if(appliedFilter == null ) {
															listbox.setSelectedIndex(actionIdx);
														} else {
															int idx = listbox.getIndexOfItem((Listitem)event.getTarget().getParent().getParent());
															listbox.setSelectedIndex(idx);
														}
													}
													//if has AddPopup, ignore default actionPerformed, the action handle by custom button
													/*
													if (event.getTarget().getFellowIfAny("puAddPopup") != null){
														UniLog.logm(this,"open popup");
														Popup addPu = (Popup) event.getTarget().getFellowIfAny("puAddPopup");
														addPu.setAttribute("actionIdx",actionIdx);
														addPu.open(event.getTarget());
													}
													*/
													if (addPu != null){
														UniLog.logm(this,"open popup");
														addPu.setAttribute("actionIdx",actionIdx);
														addPu.open(event.getTarget());
													}
													else if(actionListener != null) {
														lastActionType = JxField.ACTIONTYPE_INSERT;
														actionListener.actionPerformed(getJxField());
														lastActionType = 0;
													}
												}		
											};								
	public JxZkListbox(JxZkSkin p_skin,int p_fdtypeid, Component c)	
	{
		super(p_skin,p_fdtypeid, c);
		listbox = (Listbox) comp;
		listbox.setCtrlKeys("");
		
		tttMap = (Map) listbox.getAttribute("tttMap");
		//addPu = (Popup)ZkUtil.findChild(listbox, "puAddPopup");
		addPu = (Popup)ZkUtil.findChild(listbox.getParent(), "puAddPopup");  //andrew230721 fix popup not display. puAddPopup can be placed next to listbox or inside listheader
		
//		rows = new Vector();
//		listModelList = new ListModelList(rows);
		listModelList = new ListModelList();
//		cellMapHash = new HashMap <Component,ZkJxCellValueMapper> ();
		rowAttrHM = new HashMap<Integer, Set<Integer>>();
		listbox.setModel(listModelList);
		listbox.addEventListener(Events.ON_OK, zkEventListener);
		listbox.addEventListener(Events.ON_CANCEL, zkEventListener);
		listbox.addEventListener(Events.ON_SELECT, zkEventListener);
		sh = ZkSessionHelper.getSessionHelper();
		/*
		//obsoleted
		final EventListener rowBtnListener = 
											new ZkBiEventListener() {
												public void onZkBiEvent(Event event) throws Exception {
													UniLog.log1("rowBtnListener entered: jxField:%s cellFullame:%s", getJxField(), event.getTarget().getAttribute("cellFullName"));
													lastActionType = JxField.ACTIONTYPE_ROWBTNCLICK;
													listbox.setAttribute("rowBtnEvent", event);
													actionListener.actionPerformed(getJxField());
													lastActionType = 0;
												}
		};
		*/
		
		final EventListener updBtnListener = 
											new ZkBiEventListener() {
												public void onZkBiEvent(Event event) throws Exception {
													UniLog.log("updateBtnListener entered");
													int actionIdx = getActionIdx(event, gipi, listbox);
													if (actionIdx >= 0){
														if(appliedFilter == null ) {
															listbox.setSelectedIndex(actionIdx);
														} else {
															int idx = listbox.getIndexOfItem((Listitem)event.getTarget().getParent().getParent());
															listbox.setSelectedIndex(idx);
														}
													}
													
													if(actionListener != null) {
														lastActionType = JxField.ACTIONTYPE_UPDATE;
														actionListener.actionPerformed(getJxField());
														lastActionType = 0;
													}
												}		
											};
											
//		gipiChangeListener = 
//											new ZkBiEventListener() {
//												public void onZkBiEvent(Event event) throws Exception {
//													UniLog.log("gipi Component Changed");
//												}		
//											};
		final EventListener tbChangeListener = 
											new ZkBiEventListener() {
												public void onZkBiEvent(Event event) throws Exception {
													UniLog.log("JxZkListbox tbChangeListener entered");
													Component tb = event.getTarget();
													String s;
													ReturnMsg retMsg = null;
													try{
														String ov = "";
														Object orgV = null;
														if(tb instanceof Datebox) {
															orgV = ((Datebox) tb).getValue();
															s = com.kyoko.common.DateUtil.toDateString(((Datebox) tb).getValue(), "yyyy/mm/dd");
//															retMsg = cellMapHash.get(tb).validateChange(getJxField(),s);
															retMsg = ((ZkJxCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange(s); 
														} 
														else if(tb instanceof Checkbox) {
															orgV = ((Checkbox)tb).isChecked();
															if(((Checkbox)tb).isChecked()) {
//																retMsg = cellMapHash.get(tb).validateChange(getJxField(),"Y");
																retMsg = ((ZkJxCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange("Y"); 
															} 
															else {
//																retMsg = cellMapHash.get(tb).validateChange(getJxField(),"N");
																retMsg = ((ZkJxCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange("N"); 
															}
														} 
														else {
//															orgV = ov = ((InputElement) tb).getText();
//															orgV = ov = ChineseConvert.convertAuto2Bnew(((InputElement) tb).getText());
															if(needAutoConvert(getJxField())) orgV = ov = ChineseConvert.convertAuto2Bnew(((InputElement) tb).getText()); else orgV = ov = ((InputElement) tb).getText();
//															retMsg = cellMapHash.get(tb).validateChange(getJxField(),((InputElement) tb).getText());
															retMsg = ((ZkJxCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange(((InputElement) tb).getText()); 
														}
														if(retMsg.getStatus()) {
															currcol = editRow.indexOf(tb);
															lastActionType = JxField.ACTIONTYPE_EDITCELLCHANGED;
															actionListener.actionPerformed(getJxField());
															lastActionType = 0;
														} 
														else {
															if (tb instanceof InputElement){
																if(!((String) orgV).equals("")) {
																	((InputElement) tb).setText((String) orgV);
																	((InputElement) tb).setErrorMessage(retMsg.getMsg());
																}
															}
														}
													}
													catch(Exception ex){
														ex.printStackTrace();
														/*
														if (tb instanceof InputElement){
															String dispMsg = ex.getMessage();
															if (dispMsg.contains(":")){
																dispMsg = dispMsg.replaceFirst("^.*: ", "");
															}
															((InputElement) tb).setErrorMessage(dispMsg);
														}
														*/
													}
												}		
											};									
											
											
		ListitemRenderer listitemRenderer = new ListitemRenderer() {
			 public void render(Listitem p_listItem, Object p_data, int p_idx) throws Exception {
//				UniLog.log("jxListBox render row " + p_idx + " selected = " + listbox.getSelectedIndex() + " editrow " + editingRow + " listmodel selection " + listModelList.getSelection() );
				//ZkUtil.addDragAndDrop(p_listItem);
				
				boolean deletedFlag = false;
				boolean insertedFlag = false;
				boolean allowDeleteFlag = true;
				if (gipi != null){
					deletedFlag = false;
					insertedFlag = false;
					if (gipi.getStatus(p_data,AbstractGetItemProperty.GIPI_DELETED)){
						deletedFlag = true;
					}
					if (gipi.getStatus(p_data,AbstractGetItemProperty.GIPI_INSERTED)){
						insertedFlag = true;
					}
					allowDeleteFlag = gipi.getAllowDelete(p_data); //obtain row level allowdelete flag
				}
				else{
					deletedFlag = checkRowAttr(p_idx, ROW_ATTR_DELETED);
					insertedFlag = checkRowAttr(p_idx, ROW_ATTR_INSERTED);
				}
				if (deletedFlag && insertedFlag){
					//p_listItem.appendChild(new Listcell(){{ this.setHeight("0px"); this.setSpan(11);}});
					p_listItem.setStyle("display:none;");
					UniLog.log("not render and hide deleted row");
					return;
				}
				if(checkMarkCol) {
					Listcell lc = new Listcell();
					//lc.setHeight("36px");
					lc.setParent(p_listItem);
					//lc.setStyle("text-align:left;");
					lc.setStyle("padding-left:15px; text-align:left;");
					
					if (deletedFlag){
						lc.setStyle("padding-left:15px; text-align:left; background:pink !important;");
					}
					else{
						lc.setStyle("padding-left:15px; text-align:left;");
					}
					if(!listbox.isMultiple() && !isMobile) {
						if(canInsert) {
							Toolbarbutton b = new Toolbarbutton(); 
    						//b.setIconSclass("z-icon-pencil-square-o z-icon-2x");
    						b.setIconSclass("z-icon-plus-square z-icon-2x");
    						b.setSclass("narrowtoolbarbutton");
							b.setTooltiptext(sh.getTtLabel("Insert Item"));
							b.setAttribute("isAddButton", true);
							b.addEventListener(Events.ON_CLICK,insBtnListener);
							if (deletedFlag){
								//b.setDisabled(true);
								b.setStyle("opacity:0.7;color:red;");
							}
							else if (insertedFlag){
								b.setStyle("opacity:0.7;color:limegreen");
							}
							else{
								b.setStyle("opacity:0.7");
							}
							//b.setId("btupdate_list_row" + p_idx + "_" + listbox.getId());
							if (gipi != null){
								b.setAttribute("gipiData", p_data);
							}
							lc.appendChild(b);
						}
						if(canUpdate) {
							Toolbarbutton b = new Toolbarbutton(); 
    						//b.setIconSclass("z-icon-pencil-square-o z-icon-2x");
    						b.setIconSclass("z-icon-pencil z-icon-2x");
    						b.setSclass("narrowtoolbarbutton");
							b.setTooltiptext(sh.getTtLabel("Edit Item"));
							b.addEventListener(Events.ON_CLICK,updBtnListener);
							if (deletedFlag){
								//b.setDisabled(true);
								b.setStyle("opacity:0.7;color:red;");
							}
							else if (insertedFlag){
								b.setStyle("opacity:0.7;color:limegreen");
							}
							else{
								b.setStyle("opacity:0.7");
							}
							//b.setId("btupdate_list_row" + p_idx + "_" + listbox.getId());  //why required to set id?
							if (gipi != null){
								b.setAttribute("gipiData", p_data);
							}
							lc.appendChild(b);
						}
						if(canDelete && allowDeleteFlag) {
							Toolbarbutton b = new Toolbarbutton(); 
    						b.setIconSclass("z-icon-trash-o z-icon-2x");
    						b.setSclass("narrowtoolbarbutton");
    						b.setAttribute("JxZkListbox.deleteItemButton","Y");
							b.setTooltiptext(sh.getTtLabel("Delete Item"));
							if (deletedFlag){
								b.setStyle("opacity:0.7;color:red");
							}
							else if (insertedFlag){
								b.setStyle("opacity:0.7;color:limegreen");
							}
							else{
								b.setStyle("opacity:0.7");
							}
							b.addEventListener(Events.ON_CLICK,delBtnListener);
							if (gipi != null){
								b.setAttribute("gipiData", p_data);
							}
							lc.appendChild(b);
						}
					}
				}
				
				//if data is listcell, just attach it to listItem
				if (p_data instanceof Listcell){
						((Listcell)p_data).setParent(p_listItem);
						return;
				}
				if (p_data instanceof Cell[]){
					Cell rec[] = (Cell[]) p_data;
					rendorOneRowByCellArray(p_idx,rec,p_listItem,tbChangeListener,deletedFlag);
				} else if(gipi != null) {
					rendorOneRowByGipiNew(p_idx,p_listItem,p_data,deletedFlag);
				} else {
					if(p_data != null) {
					Listcell lc = new Listcell(p_data.toString());
					p_listItem.appendChild(lc);
					}
				}
				/*
				//andrew211103 remove double click to editing mode. the recreate component logic will trigger s2listbox error
				if(doubleClick) {
					p_listItem.addEventListener(Events.ON_DOUBLE_CLICK, zkEventListener);
				}
				else{
					p_listItem.addEventListener(Events.ON_CLICK, zkEventListener);
				}
				*/
				
				//andrew211108 fix smartac po cannot pick popup listitem
				p_listItem.addEventListener(Events.ON_CLICK, zkEventListener);
				if(loadOnDemand) {
					if(p_idx + 1 >= listModelList.size()) {
						loadRecordToList(null);
						/*
						int loadStart = listModelList.size();
						int loadEnd;
						loadEnd = gipi.getRowCount();
						if(loadEnd - loadStart > onDemandLoadCount) loadEnd = loadStart + onDemandLoadCount;
						for(int i=loadStart;i<loadEnd;i++) {
							listModelList.add(gipi.getRow(i));
						}
						*/
						listbox.invalidate();
					}
				}
			 }
		};
		listbox.setItemRenderer(listitemRenderer);
	}
	
//	void traverseGetGipiValuesXX(Component c, Object p_data,int p_idx,boolean deletedFlag) {
//		String p_id = c.getId();
//		if(p_id != null && !p_id.equals("")) {
//			Object o = gipi.getColumnValueByName(p_data,p_id);
//			if(o != null) {
//				if( o instanceof Cell) {
//					switch(((Cell) o).getType()) {
//						case Cell.VTYPE_STRING : 
//							if(c instanceof InputElement) {
//								((InputElement) c).setText(((Cell) o).getString());
//							}
//							if(c instanceof Label) {
//								((Label) c).setValue(((Cell) o).getString());
//							}
//							break;
//					}
//				}
//			}
//			c.setId(null);
//		}	
//		java.util.List <Component> clist = c.getChildren();
//		Iterator itr = clist.iterator();
// 		while(itr.hasNext()) {
// 			Component cc = (Component) itr.next();
// 			traverseGetGipiValues(cc, p_data,p_idx);
// 		}
//	}
	void traverseGetGipiValues(Component c, Object p_data,int p_idx,boolean deletedFlag,ArrayList<String> ccList) {
		//if (c instanceof Label) UniLog.log1("found label: id:%s sclass=%s tlkey:%s", c.getId(), ((Label) c).getSclass(), c.getAttribute("tlkey"));
		String p_id = c.getId();
		if(p_id != null && !p_id.equals("")) {
			if(p_id.startsWith("compdiv_")) {
				/*
				 * compdiv_.. component found crease actual jxField component and replace it
				 */
				String actualId = p_id.substring("compdiv_".length());
				final Object o = gipi.getColumnValueByName(p_data,actualId);
				if(o != null && o instanceof ColumnCell) {
					ColumnCell cc = ((ColumnCell) o);
					if(!cc.getBiColumn().isInvisible(cc.getBiResult().getSessionHelper())) {
						Component ac = JxZkBiBase.createComponentByBiColumn(cc.getBiColumn());
						ac.setId(actualId);
						c.appendChild(ac);
					} else {
						c.getParent().setVisible(false);
					}
				}
			}
			final Object o = gipi.getColumnValueByName(p_data,p_id);
			if(o != null) {
				if( o instanceof Cell) {
					if( o instanceof ColumnCell) {
						((ColumnCell) o).map(new ZkBiCellValueMapper(c,gipi));
//						c.addEventListener(Events.ON_CHANGE, gipiChangeListener);
						if(c instanceof HtmlBasedComponent) {
							if(((HtmlBasedComponent) c).getWidth()==null) {
								if (c instanceof InputElement){
									if(((ColumnCell) o).getBiColumn().isReadOnly()) {
										((InputElement)c).setReadonly(true);  //not work well for bandbox/combobox
									} else ((InputElement)c).setReadonly(deletedFlag);  //not work well for bandbox/combobox
									ColumnCell cc = (ColumnCell)o;
									JxZkBiBase.setComponentFormat(c, cc.getBiColumn() , false, sh);
								} else{
									JxZkBiBase.setComponentFormat(c, ((ColumnCell) o).getBiColumn() , false, sh);
								}
							} else {
								if (c instanceof InputElement) {
									ColumnCell cc = (ColumnCell)o;
									JxZkBiBase.setInputFormat(c, cc.getBiColumn());
								}
							}
						}
						if(c instanceof Label) {
							// setup Linked URL Here
							final String linkurl = ((ColumnCell) o).getBiResult().getLinkedUrl(
										((Cell) o).getCellLabel(),
										((ColumnCell) o).getCollection()
										);
							/*
							 won't work as ZK Lable cannot set alignment.
							switch(((ColumnCell) o).getType()) {
								case Cell.VTYPE_INT:
								case Cell.VTYPE_DOUBLE:
									ZkUtil.appendStyle((HtmlBasedComponent) c, "text-align:right;");
									break;
								
							}
							*/
							if(linkurl != null) {
								final JSONObject jo = ((ColumnCell) o).getBiResult().getLinkedCondition(((Cell) o).getCellLabel(),  (ColumnCell) o);
								   			try {
								   				Toolbarbutton visitViewBt = new Toolbarbutton();  
								    			visitViewBt.setTooltiptext(sh.getTtLabel("Visit view"));
								    			visitViewBt.setIconSclass("z-icon-link");
												visitViewBt.setSclass("narrowtoolbarbutton");
								   				visitViewBt.setStyle("vertical-align:top;");
								   				visitViewBt.addEventListener(Events.ON_CLICK, new ZkBiEventListener(){
													@Override
													public void onZkBiEvent(Event p_event) throws Exception {
														UniLog.logm(this, "clicked");
														String key = sh.putOneTimeData( jo);
														String url = linkurl + "&querycondition="+key;
														if(p_event instanceof MouseEvent) {
															MouseEvent me = (MouseEvent) p_event;
															if((me.getKeys() & me.CTRL_KEY) != 0) {
																ZkUtil.js("openNewTab('"+url+"')");
//																Executions.sendRedirect(url);
															} else {
																Executions.sendRedirect(url);
															}
														} else {
															Executions.sendRedirect(url);
														}
														//TODO implement visit view feature
													}}
								   				);
								   				Popup popup = new Popup();
								    			popup.appendChild(visitViewBt);
								    			popup.setParent(listbox.getRoot());
								    			((Label) c).setTooltip(popup);
								    			ZkUtil.appendStyle((Label) c, "color:rgb(26,13,171);");	
								   			} catch (Exception ex) {
								   				UniLog.log(ex);
								   			}	
							}
						}
						if (c instanceof Radiogroup) {
							if (sh.getAllowOptionTranslate()) {
								for (Component crd : c.queryAll("Radio")) {
									Radio rd = (Radio)crd;
									String defaultValue = StringUtils.defaultString(rd.getLabel());
									String key = ((ColumnCell)o).getBiResult().getView().getName() + "." + ((ColumnCell)o).getCellLabel() + "." + rd.getValue();
									if (sh.getAllowUpdateTranslate())
										JxZkBiBase.addContextMenu(sh, (XulElement) rd, MapUtil.of("changeLabel", MapUtil.of("key",key,"defaultValue",defaultValue)));
									if (sh.getAllowTranslate())
										rd.setLabel(ZkBiTranslateHelper.getText(sh, key, "OPTION", defaultValue));
								}
							}
						}
						ccList.add(((ColumnCell) o).getCellLabel());
					} else {
					switch(((Cell) o).getType()) {
						case Cell.VTYPE_STRING : 
							if(c instanceof InputElement) {
								((InputElement) c).setText(((Cell) o).getString());
							}
							if(c instanceof Label) {
								((Label) c).setValue(((Cell) o).getString());
							}
							if(c instanceof Html) {
								((Html) c).setContent(((Cell) o).getString());
							}
							break;
					}
					}
				} else
				if( o instanceof JxActionListener) {
					c.addEventListener(Events.ON_CLICK, 
								new ZkBiEventListener () {
									public void onZkBiEvent(Event event) throws Exception {
										((JxActionListener) o).actionPerformed(getJxField());
									};
								}
						);
				} else
				if( o instanceof String) {
//					if(c instanceof InputElement) ((InputElement) c).setText((String) o);
					if(c instanceof Label) ((Label) c).setValue((String) o); else {
						Label lb = new Label((String) o);
						Component p = c.getParent();
						p.insertBefore(lb, c);
						p.removeChild(c);
					}
				}
					
			} else {
				if(c instanceof ZkBiHeaderLabel) {
					String s = ((ZkBiHeaderLabel) c).getValue();
					if(s == null || s.equals("")) {
						String bid = (String) c.getAttribute("tlkey");
						if(bid != null) {
							//andrew201207: fill label value of single column detail template
							if (bid.startsWith("lb_") && bid.length() > 3) {
								bid = bid.substring(3);
							}
							
							Object o2 = gipi.getColumnValueByName(p_data,bid);
							if(o2 != null && o2 instanceof ColumnCell) {
								//((ZkBiHeaderLabel) c).setValue(((ColumnCell) o2).getBiColumn().getEngName());
								BiColumn biColumn = ((ColumnCell) o2).getBiColumn();
								if(!biColumn.isInvisible(((ColumnCell) o2).getBiResult().getSessionHelper())) {
								ZkBiHeaderLabel fieldLabel = (ZkBiHeaderLabel) c;
								fieldLabel.setValue(ZkBiTranslateHelper.getText(sh, biColumn.getCellFullName(), "LABEL", sh.getLabel(biColumn)));
								ZkUtil.addTranslateContextMenu(sh, fieldLabel, biColumn);
								}
							}
						}
					}
				}
			}
			c.setId(null);
		}	
		Object clist[] = c.getChildren().toArray();
		for(int i=0;i<clist.length;i++) {
 			traverseGetGipiValues((Component) clist[i], p_data,p_idx,deletedFlag,ccList);
		}
	}
	@Override
	public boolean grid_setcolheader(final int idx,Object p_obj)
	{
		if(idx >= nCols) return(false);
		if(isMobile) {
			mobileHeader.get(idx).setHeader(p_obj);
			return(true);
		} else {
			return(grid_setcolheader_real(idx,p_obj));
		}
	}
	public boolean grid_setcolheader_real(final int idx,Object p_obj)
	{
		Listhead lh = listbox.getListhead();
		Listheader lhdr;
		if( lh == null) {
			lh = new Listhead();
			lh.setParent(listbox);
			if(checkMarkCol) {
				lhdr = new Listheader("");
				lhdr.setAlign(colAlign);
				if(isMobile) {
				lhdr.setWidth("40px");
				} else {
				lhdr.setWidth("80px");
				}
				lhdr.setParent(lh);
			}
		}
		int nc = lh.getChildren().size();
		
		for(int i=nc;i <= idx + (checkMarkCol ? 1:0) ;i++){
			lhdr = new Listheader("Col " + i);
			lhdr.setParent(lh);
			lhdr.setAlign(colAlign);
		}
		lhdr = (Listheader) lh.getChildren().get(idx + (checkMarkCol ? 1:0));
		if(isMobile) {
			lhdr.setLabel(null);
			Component lc = lhdr.getLastChild();
			Div hdrDiv = null;
			if(lc != null && (lc instanceof Div)) {
				lhdr.removeChild(lc);
			}
			hdrDiv = new Div();
			hdrDiv.setAlign("right");
			hdrDiv.setStyle("float:right;");
			lhdr.appendChild(hdrDiv);
			hdrDiv.appendChild(new Label(p_obj.toString()));
		} else {
		if (p_obj instanceof String){
			lhdr.setLabel((String)p_obj);
		}
		else if (p_obj instanceof Map){
			//lhdr.setLabel(MapUtil.getString(p_obj, "label",""));
			BiColumn col = (BiColumn) MapUtil.getObject(p_obj, "biColumn");
			BiResult biResult = (BiResult) MapUtil.getObject(p_obj, "biResult");
			//SessionHelper sh = biResult == null ? null : biResult.getSessionHelper();  //move sh to member variable
			if (col.getColumnType().trim().equals("button")) {
				lhdr.setLabel("");
			}
			else {
				lhdr.setLabel(ZkBiTranslateHelper.getText(sh, col.getCellFullName(), "LABEL", MapUtil.getString(p_obj, "label","")));
   				//JxZkBiBase.addContextMenu(sh,lhdr,MapUtil.of("changeLabel", col));
   				JxZkBiBase.addContextMenu(sh,lhdr,MapUtil.of("changeLabel", MapUtil.of("key",col.getCellFullName(),"defaultValue",col.getEngName())));
   				lhdr.setAttribute("biColumnLabel", col.getLabel());
			}
			
			
			//set tooltiptext to listheader
			if (tttMap != null) {
				String tttValue = (String) tttMap.get(col.getCellLabel());
				if (StringUtils.isNotBlank(tttValue)) {
					lhdr.setTooltiptext(sh == null ? tttValue : sh.getLabel(tttValue));
				}
			}
		}
		}
		return(true);
	}	
	
	public boolean grid_setcolwidth(int idx,String width)
	{
		if(isMobile) {
			if(idx < nCols) return(true); else return(false);
		}
		try{
			Listheader lh = (Listheader) listbox.getListhead().getChildren().get(idx+ (checkMarkCol ? 1: 0));
			UniLog.logm(this, "%s:%s idx:%d width:%s", lh.getId(), lh.getLabel(), idx, width);
			if (lh != null){
				if (width != null){
//					if (width.contains("=")){
						String hflexValue = BiUtil.extractColDecorationValue(width, "hflex");
						if (!StringUtils.isBlank(hflexValue)){
							lh.setHflex(hflexValue);
						}
						String widthValue = BiUtil.extractColDecorationValue(width, "width");
						if (!StringUtils.isBlank(widthValue)){
							lh.setWidth(widthValue);
						}
						String alignValue = BiUtil.extractColDecorationValue(width, "calign");
						if (StringUtils.isNotBlank(alignValue))
							lh.setAlign(alignValue);
//					}
//					else{
//						lh.setWidth(width); //for backward compatibility
//					}
				}
				/*
				if (width != null){
					if (width.toLowerCase().startsWith("hflex=")){
						lh.setHflex(StringUtils.removeFirst(width, ".*="));
					}
					else if (width.toLowerCase().startsWith("width=")){
						lh.setWidth(StringUtils.removeFirst(width, ".*="));
					}
					else{
						lh.setWidth(width);
					}
				}
				*/
			}
			return(true);
		}
		catch(Exception ex){
			UniLog.log("setcolwidth fail");
			ex.printStackTrace();
		}
		return(false);
	}
	
	public void grid_setRow(int n)
	{
		ListModelList lm = (ListModelList) listbox.getListModel();
		for(int i = lm.size();i<n;i++) {
			lm.add(null);
		}
		for(int i = lm.size()-1;i >= n;i--) {
			lm.remove(i);
		}
	}
	
	public boolean grid_setValue(int col, int row, Object value)
	{
		Cell[] crow,orow;
		if(col >= nCols) {
			return(false);
		}
		if (value instanceof Listcell){
			ListModelList <Object>lm = (ListModelList) listbox.getListModel();
			if (row > lm.size()) return(false);
			lm.set(row, value);
			return true;
		}
		ListModelList <Cell[]>lm = (ListModelList) listbox.getListModel();
		if(row > lm.size()) return(false);
		orow = lm.get(row);
		crow = new Cell[nCols];	
		if(orow != null) {
			for(int i= 0; i < crow.length;i++) {
				if(orow.length > i) crow[i] = orow[i];
			}
		}
		//UniLog.log("HAHA listbox grid_setvalue " + col + " " + row + " " + value);
		if(value instanceof Cell) {
			crow[col] = (Cell) value;
		}
		else {
			crow[col] = new Cell(""+value);
		}
		lm.set(row, crow);
		return(true);
	}
	
	public void grid_setcurrentrow(int row)
	{
//		lastSelectIdx = listbox.getSelectedIndex();
		Listitem li = listbox.getItemAtIndex(row);
		if(li != null) {
			listbox.selectItem(li);
		} 
		else {
			listbox.clearSelection();
		}
//		currSelectTime = new Date();
		return;
	}
	public int grid_getcurrentrow()
	{
		return(listbox.getSelectedIndex());
		/*
		int cc = listbox.getSelectedIndex();
		if(appliedFilter == null) return(cc);
//		Object o = listbox.getItemAtIndex(cc);
		Object o = listModelList.get(cc);
		if(gipi != null) {
			cc = gipi.getIndexOf(o);
		} else {
			if(itemList != null) cc = itemList.indexOf(o);
		} 
		return(cc);
		*/
	}
	public int grid_getcurrentcol()
	{
		return(currcol);
	}
	protected String processAction(Event ev) {
		UniLog.log("Listbox Event Name: " + ev.getName());
		int eid = JxZkGadgetProvider.getEventID(ev.getName());
		switch(eid) {
			case JxZkGadgetProvider.EV_ONDOUBLECLICK:
				if(doubleClick) {
					if(editingRow < 0) {
						super.processAction(ev);
					}
				}
				break;
			case JxZkGadgetProvider.EV_ONCLICK:
				if(!doubleClick) super.processAction(ev);
				break;
			case JxZkGadgetProvider.EV_ONSELECT:
				if(listbox.isMultiple()) {
					UniLog.log("multiselect on selected item " + listbox.getSelectedIndex());
				} 
				else {
	    		    UniLog.log("multiselect off selected item " + listbox.getSelectedIndex() + " " + listModelList.getSelection().size());
	    		    if(listModelList.getSelection().size() == 1) {
	    		    	int idx = listModelList.indexOf(listModelList.getSelection().iterator().next());
	    		    	UniLog.log("170921 listbox idx from Selection = " + idx);
//	    		    	currSelectTime = new Date();
	    		    	/*
						if(lastSelectIdx >= 0 && lastSelectIdx == editingRow ) {
							grid_setDataFormat(-1,listbox.getSelectedIndex(),"editmode");
						}
	    		    	lastSelectIdx = listbox.getSelectedIndex();
	    		    	*/
	    		    	if(editingRow >= 0) {
							grid_setDataFormat(-1,listbox.getSelectedIndex(),"editmode");
	    		    	}
	    		    }
				}
				if(listbox.getModel().equals("select")) super.processAction(ev);
				break;
			case JxZkGadgetProvider.EV_ONCANCEL:
				if(listbox.getSelectedIndex() >= 0) {
					if(listbox.getSelectedIndex() >= 0 && listbox.getSelectedIndex() == editingRow ) {
						grid_setDataFormat(-1,-1,"editmode");
						UniLog.log("Cancel Editmode currSelectIdx = " + listbox.getSelectedIndex());
						listbox.setFocus(true);
					} 
				}
				break;
			case JxZkGadgetProvider.EV_ONOK:
				if(editingRow < 0) {
					super.processAction(ev);
				} else
					grid_setDataFormat(-1,listbox.getSelectedIndex(),"editmode");
				break;
		}
		return("");
	}
	public void grid_settooglemode(boolean p_toogleModeFlag){
		toogleModeFlag = p_toogleModeFlag;
	}
	
	void addAttribute(int p_row,int p_attr) {
		if (p_row < 0){
			UniLog.logm(this,"invalid row, ignore");
			return;
		}
		
		if (gipi == null){
			//update rowAttrHM
			Set<Integer> dataSet = rowAttrHM.get(new Integer(p_row)); 
			if (dataSet == null){
				dataSet = new HashSet<Integer>();
				rowAttrHM.put(p_row,dataSet);
			}
			
			//add attribute to dataSet
			dataSet.remove(new Integer(p_attr));
			dataSet.add(new Integer(p_attr));
		}
		
		//prepare for rendering
		if(p_attr == ROW_ATTR_DELETED &&  p_row == editingRow) {
			editingRow = -1;
		}
		listModelList.removeFromSelection(listModelList.get(p_row));
		listModelList.set(p_row,listModelList.get(p_row));
	}
	void delAttribute(int p_row,int p_attr) {
		if (gipi == null){
			//update rowAttrHM
			Set<Integer> dataSet = rowAttrHM.get(new Integer(p_row)); 
			if(dataSet == null) {
				UniLog.logm(this,"dataSet is null, ignore");
				return;
			}
			//remove attr from dataSet
			dataSet.remove(new Integer(p_attr));
		}
		
		//prepare for rendering
		listModelList.removeFromSelection(listModelList.get(p_row));
		listModelList.set(p_row,listModelList.get(p_row));
	}
	
	/***
	 *  p_col - not use
	 *  p_row - grid row
	 *  p_format - format string tag
	 */
	public void grid_setDataFormat(int p_col,int p_row,String p_format) {
		int listrow = p_row;
		UniLog.logm(this,"%d %d %s entered", p_col, p_row, p_format);
		/*
		if(p_row >= 0 && appliedFilter != null) {
			if(itemList != null) {
				listrow = listModelList.indexOf(itemList.get(p_row));
			} else if(gipi != null){
				listrow = listModelList.indexOf( gipi.getRow(p_row));
			}
		}
		*/
		ListModelList lm = (ListModelList) listbox.getListModel();
		if(p_format.equals("editmode")) {
			if(listrow >= lm.size()) {
				UniLog.logm(this, "row > list size, ignore");
				return;
			}
			if(editingRow == listrow) {
				//UniLog.logm(this, "editingRow = row = %d, ignore", editingRow);
				
				//toggle edit mode
				editingRow = -1;
				UniLog.logm(this, "switch row:%d to non edit mode", listrow);
				if (listrow >= 0){
					lm.set(listrow, lm.get(listrow));
				}
				
				return;
			}
			
			if(checkRowAttr(listrow, ROW_ATTR_DELETED)){
				UniLog.logm(this, "ignore for deleted row");
				return;
			}
			
			if(editingRow >= 0) {
				UniLog.log1("lm set editingRow:%d", editingRow);
				lm.set(editingRow, lm.get(editingRow));
				if(listrow < 0) {
					listbox.setSelectedIndex(editingRow);
				}
			}
			editingRow = listrow;
			if(editingRow >= 0) {
				UniLog.log1("lm set editingRow(1):%d", editingRow);
				lm.set(editingRow, lm.get(editingRow));
				listModelList.clearSelection();
				listbox.setSelectedIndex(editingRow);
			}
		}
		else if(p_format.equals("sizableHeader")) {
			Listhead lh = listbox.getListhead();
			if (lh != null){
				lh.setSizable(true);
			}
		}
		else if(p_format.equals("fixHeader")) {
			Listhead lh = listbox.getListhead();
			if (lh != null){
				lh.setSizable(false);
			}
		}
		else if(p_format.equals("add_deleted")) {
			addAttribute (listrow,ROW_ATTR_DELETED);
		}
		else if(p_format.equals("remove_deleted")) {
			delAttribute (listrow,ROW_ATTR_DELETED);
		}
		else if(p_format.equals("add_updated")) {
			addAttribute (listrow,ROW_ATTR_UPDATED);
		}
		else if(p_format.equals("remove_updated")) {
			delAttribute (listrow,ROW_ATTR_UPDATED);
		}
		else if(p_format.equals("add_inserted")) {
			addAttribute (listrow,ROW_ATTR_INSERTED);
		}
		else if(p_format.equals("remove_inserted")) {
			delAttribute (listrow,ROW_ATTR_INSERTED);
		}
		else{
			UniLog.logm(this, "unknow format %s", p_format);
		}
	}
	
	public void clear()
	{
		UniLog.log("Listbox Clear Called");
		grid_setRow(0);
		rowAttrHM.clear();
		if(editRow != null) {
			editRow.clear();
			editingRow = -1;
		}
	}

	@Override
	public void setItemList(List p_itemList)
	{
		setItemListGen(p_itemList,null);
	}
	@Override
	public void setItemListInterface(AbstractGetItemProperty p_interface)
	{
		setItemListGen(null,p_interface);
	}
	void setItemListGen(List p_itemList,AbstractGetItemProperty p_interface)
	{
		if(p_interface != null) {
			gipi = p_interface;
			itemList = null;
		} 
		else {
			gipi = null;
			if(p_itemList != null) itemList = new Vector(p_itemList); else itemList = null;
		}
		listModelList.clear();
		onDemandIdx = 0;
		rowAttrHM.clear();
		appliedFilter = null;
		if(filterInput != null) filterInput.setText("");
		if(editRow != null) {
			editRow.clear();
		}
		if( itemList == null && gipi == null) {
			UniLog.logm(this, "itemList and gipi is null, return");
			return;
		}
		if(gipi == null) {
			grid_setCol(1);
		}
//		if(gipi != null) itemList = p_itemList;
		int n;
		if(gipi == null) {
			n = p_itemList.size();
			grid_setRow(n);
			for(int i = 0;i<n;i++) {
				grid_setValue(0, i, p_itemList.get(i).toString());
			}
		} else {
			//ZkUtil.dumpData(gipi);
			if(loadOnDemand) {
				loadRecordToList(null);
			} else {
				n = gipi.getRowCount();
				for(int i = 0;i<n;i++) {
					listModelList.add(gipi.getRow(i));
				}
			}
			n = gipi.getColumnCount(null);
			grid_setCol(n);
			for(int i=0;i<n;i++) {
				grid_setcolheader(i,gipi.getHeader(null,i));
				grid_setcolwidth(i,gipi.getColumnWidth(null, i));
			}
//			Object listTitle = gipi.getListTitle();
//			Collection<Component> heads = listbox.getHeads();
//			for(Component hc : heads) {
//				if(hc instanceof Auxhead) {
//					listbox.removeChild(hc);
//				}
//			}
//			if(listTitle != null) {
//				Auxhead ah = new Auxhead();
//				ah.setHflex("1");
//				Listhead lh = listbox.getListhead();
//				int ncol=0;
//				if(lh != null) {
//					listbox.insertBefore(ah, lh);
//					ncol=lh.getChildren().size();
//				} else {
//					listbox.appendChild(ah);
//					ncol = 1;
//				}
//				Auxheader ahdr = new Auxheader();
//				ahdr.setAlign(colAlign);
//				ahdr.setColspan(ncol);
//				ahdr.setLabel(listTitle.toString());
//				ah.appendChild(ahdr);
//			} else {
//				for(Component hc : heads) {
//					if(hc instanceof Auxhead) {
//						listbox.removeChild(hc);
//					}
//				}
//			}
			listbox.invalidate();
		}
	}
//	@Override
//	public void setItemList(List p_itemList,AbstractGetItemProperty p_interface)
//	{
//		gipi = p_interface;
//		grid_setRow(0);
//		if(gipi == null) {
//			grid_setCol(1);
//		} else {
//			listModelList.clear();
//		}
//		rowAttrHM.clear();
//		if(editRow != null) {
//			editRow.clear();
//		}
//		if(gipi != null) itemList = p_itemList;
//		if(p_itemList == null) {
//			UniLog.logm(this, "itemList is null, ignore");
//			return;
//		}
//		if(gipi == null) {
//			grid_setRow(p_itemList.size());
//		}
//		for(int i = 0;i<p_itemList.size();i++) {
//			if(p_interface == null)
//				grid_setValue(0, i, p_itemList.get(i).toString());
//			else {
//				listModelList.add(p_itemList.get(i));
//			}
//		}
//	}	
	void setCheckMarkCol(boolean sw) {
		if(sw && !checkMarkCol) {
			checkMarkCol = true;
			Listhead lh = listbox.getListhead();
			Listheader lhdr;
			if(lh == null) {
				lh = new Listhead();
				lh.setParent(listbox);
			}
			int nc = lh.getChildren().size();
			if(nc <= nCols) {
				lhdr = new Listheader("");
				lhdr.setAlign(colAlign);
				if(isMobile) {
				lhdr.setWidth("40px");
				} else {
				lhdr.setWidth("80px");
				}
				List <Component> lhdrs = lh.getChildren();
				if(lhdrs == null || lhdrs.size() <= 0) {
					lhdr.setParent(lh);
				} 
				else {
					lh.insertBefore(lhdr, lhdrs.get(0));
				}
			}
		} 
		else if(!sw && checkMarkCol) {
			checkMarkCol = false;
			Listhead lh = listbox.getListhead();
			Listheader lhdr;
			if(lh != null) {
				List <Component> lhdrs = lh.getChildren();
				if(lhdrs != null && lhdrs.size() > 0) lh.removeChild(lhdrs.get(0));
			}
		}
	}
	@Override
	public void setAttribute(String p_attr,String p_value)
	{
		UniLog.log("listbox setattribute "+p_attr + " "  + p_value);
		if(p_attr.equals("mode")) {
			if(p_value.equals("singleselect")) {
				listModelList.setMultiple(false);
				listbox.setMultiple(false);
			}
			else if(p_value.equals("multiselect")) {
				UniLog.log("turn on multiselect");
				listModelList.setMultiple(true);
				listbox.setMultiple(true);
			}
			else if(p_value.equals("checkmark")) {
				listbox.setCheckmark(true);
				setCheckMarkCol(true);
			}
			else if(p_value.equals("checkmarkcol")) {
				setCheckMarkCol(true);
			}
			else if(p_value.equals("nocheckmark")) {
				listbox.setCheckmark(false);
				setCheckMarkCol(false);
			}
			else if(p_value.equals("doubleClickAction")) {
				doubleClick =  true;
				//listbox.setNonselectableTags("*");  //andrew190717: fix mouse up focus loss issue  //andrew210208 remove it to allow detail row modification (clerp2)
			}
			if(p_value.equals("singleClickAction")) {
				doubleClick =  false;
			}
			else if(p_value.equals("noDelete")) {
				canDelete = false;
			}
			else if(p_value.equals("canDelete")) {
				canDelete = true;
			}
			else if(p_value.equals("noUpdate")) {
				canUpdate = false;
			}
			else if(p_value.equals("canUpdate")) {
				canUpdate = true;
			}
			else if(p_value.equals("noInsert")) {
				canInsert = false;
			}
			else if(p_value.equals("canInsert")) {
				canInsert = true;
			}
			else{
				UniLog.logm(this, "unknow mode %s", p_value);
			}
		} else if(p_attr.equals("mobileView")) {
				if(p_value.equals("on")) isMobile = true;
				if(p_value.equals("off")) isMobile = false;
		} else if(p_attr.equals("showfilter")) {
			addFilterInput();
		} else if(p_attr.equals("filter")) {
			if(gipi == null && itemList == null) return;
			listModelList.clear();
			onDemandIdx = 0;
			if(p_value == null || p_value.trim().equals("")) {
//				listModelList.clear();
				appliedFilter = null;
				if(itemList != null) {
					listModelList.addAll(itemList);
				} else if(gipi != null) {
					if(loadOnDemand) {
						loadRecordToList(null);
					} else {
					int n = gipi.getRowCount();
					for(int i = 0;i<n;i++) {
						listModelList.add(gipi.getRow(i));
					}
					}
				}
			} else {
				appliedFilter = p_value;
				if(itemList != null) {
					for(Object o : itemList) {
						if(ZkBiSearchHelper.match(gipi.getString(o), p_value)) {
							listModelList.add(o);
						}
					}
				} else {
					if(loadOnDemand) {
						loadRecordToList(p_value);
					} else {
					int n = gipi.getRowCount();
					for (int i=0;i<n;i++) {
						Object o = gipi.getRow(i);
						if(ZkBiSearchHelper.match(gipi.getString(o), p_value)) {
							listModelList.add(o);
						}
					}
					}
				}
			}
			if(filterInput != null) filterInput.setText(p_value);
		}
		else if(p_attr.equals("nopaging")) {
				listbox.setMold("default");	
				if(pg != null) {
					if(footerLayout != null) {
						if(filterLayout != null) {
							footerLayout.removeChild(filterLayout); 
							filterLayout = null;
							filterInput = null;
						}
						footerLayout.removeChild(pg); 
						pg = null;
						/*
						List<Component> childs = footerLayout.getChildren();
						if(childs == null || childs.size() <= 0) {
							listbox.removeChild(footerLayout);
							footerLayout = null;
						}
						*/
					}
				}
		} else if(p_attr.equals("paging")) {
			if(p_value.equals("disabled")) {
				listbox.setMold("default");	
			}
			addPaging();
			listbox.setPaginal(pg);
			listbox.setMold("paging");	
			//listbox.setRows(8);
			//andrew201007: listbox not allow to set both height and rows since zk950
			if (StringUtils.isBlank(listbox.getHeight())){
				if(!ZkSessionHelper.getSessionHelper().useJxFormG2()) {
					listbox.setRows(8);
				}
			}
			if(p_value.equals("withfilter")) {
				addFilterInput();
			}
		} else if(p_attr.equals("onDemand")) {
				onDemandLoadCount = Integer.parseInt(p_value);
				if(onDemandLoadCount > 0) loadOnDemand = true; else loadOnDemand = false;
		} else{
			UniLog.logm(this, "unknow attribute %s", p_value);
		}
	}
	
	void addPaging() {
		if(pg != null) return;
		addFooterLayout();
		pg = new Paging();
		pg.setWidth("500px");
		pg.setDetailed(true);
		pg.setParent(footerLayout);
	}
	void addFooterLayout() { 
		if(footerLayout != null) return;
		Listfoot lf = listbox.getListfoot();
		HtmlBasedComponent lfdr = null;
		if(lf == null) {
			lf = new Listfoot();
			lf.setParent(listbox);
			lfdr = new Listfooter();
			lfdr.setId("footer_"+listbox.getId());
			lfdr.setParent(lf);
			((Listfooter) lfdr).setSpan(listbox.getListhead().getChildren().size());
		} else {
			footerLayout = (Hlayout) listbox.getFellowIfAny("footlayout_"+listbox.getId());
			if(footerLayout == null) {
				lfdr = (Listfooter) lf.getFellowIfAny("footer_"+listbox.getId());
				if(lfdr == null) {
					/*
					Listfoot does has footer or footlayout defined , put pagin in auxHead
					 */
					auxHeadForFooterLayout = new Auxhead();
//					auxHeadForFooterLayout.setParent(listbox);
					Listhead lh = listbox.getListhead();
					listbox.insertBefore(auxHeadForFooterLayout,lh);
					lfdr = new Auxheader();
					lfdr.setId("footer_"+listbox.getId());
					lfdr.setParent(auxHeadForFooterLayout);
					((Auxheader) lfdr).setColspan(listbox.getListhead().getChildren().size());
				}
			}
		}

		if(footerLayout == null) {
			footerLayout = new Hlayout();
			footerLayout.setValign("middle");
			footerLayout.setParent(lfdr);
			footerLayout.setId("footlayout_"+listbox.getId());
		}
	}
	void addFilterInput() {
		if(filterInput != null) return;
		addFooterLayout();
		filterLayout = new Hlayout();
		Label lb = new Label(sh == null ? "Filter:" : sh.getLabel("Filter:"));
		lb.setParent(filterLayout);
		filterInput = new Textbox();
		filterInput.setId("footlayout_filterInput_" + listbox.getId());
		filterInput.setInstant(true);
		filterInput.addEventListener( Events.ON_CHANGE, 
			new ZkBiEventListener() {
				public void onZkBiEvent(Event event) throws Exception {
					setAttribute("filter",((Textbox) event.getTarget()).getText());
				}		
			}
		);
		filterInput.setParent(filterLayout);
		filterLayout.setParent(footerLayout);
	}
	public void grid_setCol(int n)
	{
		if(isMobile ){
			if(mobileHeader == null) {
				mobileHeader = new Vector<MobileHeader>();
			}
			int nn = mobileHeader.size();
			for(int i=nn;i<n;i++) {
				mobileHeader.add(new MobileHeader());
			}
			for(int i=mobileHeader.size()-1;i>=n;i--) {
				mobileHeader.remove(i);
			}
			grid_setCol_real(1) ;
			grid_setcolheader_real(0,gipi != null ? gipi.getHeader(null, -1) : "");
			nCols = n;
		} else {
			grid_setCol_real(n) ;
		}
	}
		
	public void grid_setCol_real(int n) {
		
		Listhead lh = listbox.getListhead();
		Listheader lhdr;
		if( lh == null) {
			lh = new Listhead();
			lh.setParent(listbox);
			if(checkMarkCol) {
				lhdr = new Listheader("");
				lhdr.setAlign(colAlign);
				if(isMobile) {
				lhdr.setWidth("40px");
				} else {
				lhdr.setWidth("80px");
				}
				lhdr.setParent(lh);
			}
		}
		int nc = lh.getChildren().size();
		
		for(int i=nc;i < n + (checkMarkCol ? 1:0) ;i++){
			lhdr = new Listheader("Col " + i);
			lhdr.setParent(lh);
			lhdr.setAlign(colAlign);
		}
		for(int i = nc-1; i >= n + (checkMarkCol ? 1:0);i--) {
			lh.removeChild(lh.getChildren().get(i));
		}
		nCols = n;
	}
	public void addChangeListener(JxChangeListener x)
	{
		UniLog.log("JxCbuilderListbox.addChangeListener " + getName());
		/*
		if(x != null) {
			if(changeListener == null) {
				comp.addEventListener("onSelect", zkEventListener);
			}
		} else {
			if(changeListener != null) {
				comp.removeEventListener("onSelect", zkEventListener);
			}
		}
		*/
		changeListener = x;
	}	
	public void addActionListener(JxActionListener p_listener)
	{
		UniLog.log("JxCbuilderButton.addActionListener " + getName());
		/*
		if(p_listener != null) {
			if(actionListener == null) {
				comp.addEventListener("onClick", zkEventListener);
			}
		} else {
			if(actionListener != null) {
				comp.removeEventListener("onClick", zkEventListener);
			}
		}
		*/
		actionListener = p_listener;
	}
	
	@Override
	public Object grid_getValue(int col, int row)
	{
		if(col == -1) {
			if(row >= 0) return(listModelList.get(row)); else return(null);
		}
		if(row == -1 ) {
			return(editRow.get(col));
		}
		if(row == -2 ) {
//			ZkJxCellValueMapper cm = cellMapHash.get(editRow.get(col));
			ZkJxCellValueMapper cm = (ZkJxCellValueMapper) editRow.get(col).getAttribute("CellValueMapper");
			if(cm != null) return(cm.getBindedCell());
		}
		return(null);
	}
	
	private boolean checkRowAttr(int p_row, int p_attr){
		if (gipi != null){
			UniLog.logm(this,"skip for gipi mode");
			return (false);
		}
		if (rowAttrHM.get(new Integer(p_row)) != null && rowAttrHM.get(new Integer(p_row)).contains(new Integer(p_attr))){
			return(true);
		}
		return(false);
	}
	private int getNextEditingRow(int p_row, int p_offset){
		if (p_offset == 0){
			UniLog.logm(this, "invalid offset");
			return(-1);
		}
		int nextEditingRow = p_row + p_offset;
		if (nextEditingRow < 0) {
			return(-1);
		}
		if (nextEditingRow >= listModelList.getSize()){
			if(loadOnDemand) {
				
			}
			return(-1);
		}
		if (checkRowAttr(nextEditingRow, ROW_ATTR_DELETED)){
			return(getNextEditingRow(nextEditingRow, p_offset));
		}
		return(nextEditingRow);
	}
	
	@Override
	public Vector getSelectList()
	{	
		Vector v = new Vector();
		for(Listitem li : listbox.getSelectedItems()) {
			v.add(listModelList.get(li.getIndex()));
		}
		return(v);
	}
	@Override
	public int addItemToList(Object p_item,int p_idx){
		if(loadOnDemand) {
			UniLog.log("WARNING !!!! addItemToList not works with loadOnDemand");
		}
		int listidx = p_idx;
		// must modify to handle situation when filter applied
		if(gipi != null) {
			if(p_idx > 0 && appliedFilter != null) {
				listidx = listModelList.indexOf(gipi.getRow(p_idx-1))+1;
			}
			if(listidx < 0){
				listModelList.add(p_item);
				listbox.setSelectedIndex(listModelList.getSize()-1); //mark active row selected
			}
			else{
				//TODO: handle attribute 
				listModelList.add(listidx, p_item); 
				listbox.setSelectedIndex(listidx); //mark active row selected
			}
		}
		return(-1);
	}
	/*
	@Override
	public boolean setItemToList(Object p_item,int p_idx){
		// must modify to handle situation when filter applied
		if(gipi != null) {
			listModelList.set(p_idx, p_item);
		}
		return(false);
	}
	*/
	
	public static int getActionIdx(Event p_event, AbstractGetItemProperty p_gipi, Listbox p_listbox){
		int actionIdx  = -1;
		//obtain from gipi
		if (p_gipi != null && p_event.getTarget().getAttribute("gipiData") != null){
			actionIdx = p_gipi.getIndexOf(p_event.getTarget().getAttribute("gipiData"));
			UniLog.logm(null,"gipi getIndexOf return:%d",p_gipi.getIndexOf(p_event.getTarget().getAttribute("gipiData")));
		}
		//obtain from listitem index
		else if (p_listbox != null && p_event.getTarget().getParent().getParent() instanceof Listitem){
			actionIdx = p_listbox.getIndexOfItem((Listitem)p_event.getTarget().getParent().getParent());
		}
		else{
			UniLog.logm(null,"fail to obtain actionIdx");
		}
		UniLog.logm(null,"actionIdx=%d", actionIdx);
		return(actionIdx);
	}
	
	@Override
	public Object getValue() {
		Listitem li = listbox.getSelectedItem();
		if(li != null) return(listModelList.get(li.getIndex()));
		return(null);
	}

	@Override
	public void setText(String p_text)
	{
		if(itemList != null) {
			int idx = itemList.indexOf(p_text);
			if(idx >= 0) {
				listbox.setSelectedIndex(idx);
			} else {
				listbox.setSelectedIndex(-1);
			}
		}
	}
	public void setEnable(boolean b)
	{
		listbox.setDisabled(!b); 
		if(b) {
			ZkUtil.removeSclass((HtmlBasedComponent) listbox, "zkbi-selector-disabled");
		} else {
			ZkUtil.appendSclass(listbox, "zkbi-selector-disabled");
		}
	}
	
	int loadRecordToList(String filter) {
		int n;
		int m = listModelList.size();
		for(n=0;onDemandIdx<gipi.getRowCount();onDemandIdx++) {
			if(filter == null ) {
				listModelList.add(gipi.getRow(onDemandIdx));
				n++;
				if(n >= onDemandLoadCount) {
					return(n);
				}
			} else {
				Object o = gipi.getRow(onDemandIdx);
				if(ZkBiSearchHelper.match(gipi.getString(o), filter)) {
					listModelList.add(o);
					n++;
					if(n >= onDemandLoadCount) {
						return(n);
					}
				}
			}
		}
		return(n);
	}
}
