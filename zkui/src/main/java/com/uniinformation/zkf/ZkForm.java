package com.uniinformation.zkf;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Doublebox;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Vbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.InputElement;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueMapper;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.ZkJxCellValueMapper;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jx.zk.ZkJxQueryInput;
import com.uniinformation.jxapp.JxZkBiBase;
import com.kyoko.common.*;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.webcore.ZkSessionHelper;
import com.uniinformation.zkbi.QueryConditionRow;

public class ZkForm {
	Component rootComp = null;
	CellCollection collection = null;
	EventListener listener = null;
	
	EventListener tbChangeListener = 
			new EventListener() {
				public void onEvent(Event event) throws Exception {
					if(event.getName().equals(Events.ON_OPEN) && event.getTarget() instanceof ZkJxPickInput) {
						UniLog.log("HAHA pikcinput opened " + ((ZkJxPickInput) event.getTarget()).isOpen());
						if(((ZkJxPickInput) event.getTarget()).isOpen()) {
						String viewName = (String) event.getTarget().getAttribute("listView");
						if(viewName != null) {
							Component tb = event.getTarget();
							ZkJxCellValueMapper vm = (ZkJxCellValueMapper) tb.getAttribute("CellValueMapper");
							final Cell bindedCell = vm.getBindedCell();
							
							String condStr = (String) event.getTarget().getAttribute("listCondition");
							final String colName  = (String) event.getTarget().getAttribute("listColumn");
							SessionHelper sh = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
							JxZkBiBase.pickBySelect(sh,viewName,condStr, new EventListener() {
								@Override
								public void onEvent(Event arg0) throws Exception {
									// TODO Auto-generated method stub
									CellCollection col = (CellCollection) arg0.getData();
									if(collection!= null && col != null && colName != null) {
										Object o = col.getCell(colName).getObject();
										bindedCell.set(col.getCell(colName).getObject());
										
									}
								}
										
							}
							);
						}
						}
						return;
					}
					UniLog.log1("ZkForm tbChangeListener entered");
					Component tb = event.getTarget();
					String s;
					ReturnMsg retMsg = null;

					try{
//						String ov = "";
						Object orgV = null;
						if(tb instanceof Datebox) {
							orgV = ((Datebox) tb).getValue();
							s = com.kyoko.common.DateUtil.toDateString(((Datebox) tb).getValue(), "yyyy/mm/dd");
//							retMsg = cellMapHash.get(tb).validateChange(getJxField(),s);
							retMsg = ((ZkJxCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange(s); 
						} else if(tb instanceof Radiogroup) {
							ZkJxCellValueMapper cm = (ZkJxCellValueMapper) tb.getAttribute("CellValueMapper");
							AbstractGetItemProperty gipi = cm.getBindedCell().getItemPropertyInterface();
							if(gipi != null) {
								int idx = ((Radiogroup)tb).getSelectedIndex();
								if(idx >= 0) {
									orgV = gipi.getRow(idx);
								} else {
									orgV = "";
								}
							} else {
								orgV = ((Radiogroup)tb).getSelectedIndex();
							}
							retMsg = ((ZkJxCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange(orgV.toString()); 
						} else if(tb instanceof Checkbox) {
							orgV = ((Checkbox)tb).isChecked();
							if(((Checkbox)tb).isChecked()) {
//								retMsg = cellMapHash.get(tb).validateChange(getJxField(),"Y");
								retMsg = ((ZkJxCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange("Y"); 
							} 
							else {
//								retMsg = cellMapHash.get(tb).validateChange(getJxField(),"N");
								retMsg = ((ZkJxCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange("N"); 
							}
						} else if(tb instanceof Listbox) {
							UniLog.log("listbox changed event = " + event.toString());
							ZkJxCellValueMapper cm = ((ZkJxCellValueMapper) tb.getAttribute("CellValueMapper"));
							Cell bc = cm.getBindedCell();
							
						} else if(tb instanceof Label) {
							retMsg = ((ZkJxCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange(((Label) tb).getValue()); 
						} else if(tb instanceof QueryConditionRow){
							retMsg = ReturnMsg.defaultOk;
							
						} else if(tb instanceof InputElement){
//							orgV = ov = ((InputElement) tb).getText();
//							orgV = ov = ChineseConvert.convertAuto2Bnew(((InputElement) tb).getText());
//							orgV = ChineseConvert.convertAuto2Bnew(((InputElement) tb).getText());
							orgV = ((ZkJxCellValueMapper) tb.getAttribute("CellValueMapper")).getBindedCell().getObject();
//							retMsg = cellMapHash.get(tb).validateChange(getJxField(),((InputElement) tb).getText());
							retMsg = ((ZkJxCellValueMapper) tb.getAttribute("CellValueMapper")).validateChange(((InputElement) tb).getText()); 
						}
						if(retMsg == null || retMsg.getStatus()) {
							// validate ok, noop
						} 
						else {
							if (tb instanceof InputElement){
							//	if(!((String) orgV).equals("")) {
									if(orgV instanceof String) {
										((InputElement) tb).setText((String) orgV);
									} else {
										((InputElement) tb).setText(orgV.toString());
									}
									((InputElement) tb).setErrorMessage(retMsg.getMsg());
							//	}
							}
						}
						
						//andrew210526 propagate the event to listener. it's not good to propagate onClick event, cannot identify dirty state
						if (StringUtils.equalsAnyIgnoreCase(event.getName(), "onChange", "onClick") && listener != null) {
							listener.onEvent(event);
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
		
	
	void createCellValueMapper(Component p_comp) {
		SessionHelper sh = ZkSessionHelper.getSessionHelper();
		List childs = p_comp.getChildren();
		for(Iterator it = childs.iterator();it.hasNext();) {
			 Component child = (Component)  it.next();
			 createCellValueMapper(child);
			 if(!child.getId().equals("")) {
				 if(child instanceof Button 
						 || child instanceof Toolbarbutton
						 ) {
				 } else {
						if(child instanceof Hlayout) {
							if(!(child instanceof QueryConditionRow)) {
								continue;
							}
						}
						if(child instanceof Vlayout) continue;
						if(child instanceof Hbox) continue;
						if(child instanceof Vbox) continue;
						if(child instanceof Div) continue;
						if(child instanceof Datebox) {
							if(sh != null && StringUtils.isEmpty(((Datebox) child).getFormat()) ) {
								String ss = BiConfig.getDefaultDateFormat(sh);
								if(ss != null) {
									((Datebox) child).setFormat(ss);
								} else {
									((Datebox) child).setFormat("yyyy/MM/dd");
								}
							}
						}
						child.setAttribute("CellValueMapper", new ZkJxCellValueMapper(child));
						if(child instanceof QueryConditionRow) continue;
						UniLog.log("Add tbChangeListenr to " + child.getId());
						child.addEventListener(Events.ON_CHANGE, tbChangeListener );
						child.addEventListener(Events.ON_OPEN, tbChangeListener );
						if(child instanceof Listbox ) {
							child.addEventListener(Events.ON_SELECT, tbChangeListener );
						}
						/*
						else if (child instanceof Checkbox) {  
							//andrew210602 handle checkbox, if send onCheck event to comp, will cause Event cannot be cast to CheckEvent
							child.addEventListener(Events.ON_CHECK, tbChangeListener);
						}
						*/
						else {
							child.addEventListener(Events.ON_CLICK, tbChangeListener );
						}
				 }
			 }
		}
		
	}
	public ZkForm (Component p_root,String p_zul) {
		/*
		UniLog.log1("called root:%s zul:%s", p_root, p_zul);
		if(p_root != null) {
			rootComp = p_root;
		} else {
			rootComp = new Window();
//			rootComp.setParent( Executions.getCurrent().getDesktop().getFirstPage().getFirstRoot());
			rootComp.setParent( ZkUtil.getMainComp());
		}
		if(p_zul != null) {
			Executions.getCurrent().createComponents(p_zul, rootComp,null);
		}
		createCellValueMapper(rootComp);
		*/
		UniLog.log1("called root:%s zul:%s", p_root, p_zul);
		if(p_zul != null) {
			Component createdComp = Executions.getCurrent().createComponents(p_zul, p_root,null);
			if(createdComp != null)  {
				if(p_root == null) {
					if(createdComp instanceof IdSpace) {
						rootComp = createdComp;
						rootComp.setParent(ZkUtil.getMainComp());
					} else {
						rootComp = new Window();
						createdComp.setParent(rootComp);
						rootComp.setParent(ZkUtil.getMainComp());
					}
				} else {
					if( createdComp instanceof IdSpace) {
						rootComp = createdComp;
					} else {
						rootComp = p_root;
					}
				}
			}
		} else {
			if(p_root != null) {
				rootComp =p_root;
			} else {
				rootComp = new Window();
				rootComp.setParent( ZkUtil.getMainComp());
			}
		}
		
		createCellValueMapper(rootComp);
	}
	
	void addCellsToCollection(Component p_comp,EventListener p_listener,CellCollection p_col) throws CellException {
		List childs = p_comp.getChildren();
		for(Iterator it = childs.iterator();it.hasNext();) {
			 Component child = (Component)  it.next();
			 if(!child.getId().equals("")) {
				 if(child instanceof Button 
						 || child instanceof Toolbarbutton
						 ) {
					 if(p_col.testCell(child.getId()) != null) {
					 	((Button) child).setLabel(p_col.getCellString(child.getId()));
					 }
					 if(listener != null) {
						 child.removeEventListener(Events.ON_CLICK, listener);
					 }
					 if(p_listener != null) {
						 child.addEventListener(Events.ON_CLICK, p_listener);
					 }
				 } else {
					 Cell c;
					 CellValueMapper cm = ((CellValueMapper) child.getAttribute("CellValueMapper"));
					 if(collection != null) {
						 c = collection.testCell(child.getId());
						 if(c != null) c.mapDelete(cm);
					 }
					 if(p_col != null ) {
						 c = p_col.testCell(child.getId());
						 if(c == null) {
							 if(child instanceof Radiogroup)  {
								 c = new Cell(0);
	 						} else if(child instanceof Combobox )  {
		 						c = new Cell(((Combobox) child).getValue());
//			 					String viewName = (String) child.getAttribute("listView");
//				 				if(viewName != null) {
//					 				try {
//						 				SessionHelper sh = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
//							 			BiSchema schema = BiSchema.loadSchema(sh);
//								 		BiResult br = schema.getViewByName(viewName).newBiResult(sh.getLoginId(), null, null, sh);
//									 	String condStr = (String) child.getAttribute("listCondition");
//										 if(condStr != null) {
//											 br.addCustomCondition(condStr);
//	 									}
//		 								br.query();
//			 							String colName  = (String) child.getAttribute("listColumn");
//				 						Vector<String> v = new Vector<String>();
//					 					for(int i=0;i<br.getRowCount();i++) {
//						 					br.loadOneRecV(i);
//							 				v.add(br.getCellString(colName));
//								 		}
//									 	Collections.sort(v);
//										 c.setItemList(v);
//	 									c.set(((Combobox) child).getValue());
//		 							} catch (Exception ex) {
//			 							UniLog.log(ex);
//				 					}
//					 			}
						 	} else if(child instanceof ZkJxPickInput)  {
		 						c = new Cell(((ZkJxPickInput) child).getValue());
//							 	String viewName = (String) child.getAttribute("listView");
//								 if(viewName != null) {
//	 								try {
//			 							String condStr = (String) child.getAttribute("listCondition");
//				 						String colName  = (String) child.getAttribute("listColumn");
//					 				} catch (Exception ex) {
//						 				UniLog.log(ex);
//							 		}
//								 }
	 						} else if(child instanceof Textbox)  {
		 						c = new Cell("");
			 					c.set(((Textbox) child).getValue());
				 			} else if(child instanceof Datebox)  {
					 			c = new Cell(new java.util.Date());
						 		c.set(((Datebox) child).getValue());
	 						} else if(child instanceof Intbox)  {
		 						c = new Cell(0);
			 					c.set(((Intbox) child).getValue());
				 			} else if(child instanceof Label)  {
					 			c = new Cell("");
						 		c.set(((Label) child).getValue());
	 						} else if(child instanceof QueryConditionRow)  {
		 						c = new Cell("");
			 				} else if(child instanceof Doublebox)  {
				 				c = new Cell(0.0);
					 			c.set(((Doublebox) child).getValue());
						 	} else if(child instanceof Checkbox)  {
							 	c = new Cell(false);
								 c.set(((Checkbox) child).isChecked());
	 						} 
		 					if(c != null) {
			 					p_col.addCell(child.getId(), c);
				 			}
					 	 } else {
					 		 int cc;
					 		 cc = 0;
					 	 }
						 if(c != null) {
	 						if(child instanceof Combobox )  {
			 					String viewName = (String) child.getAttribute("listView");
				 				if(viewName != null) {
					 				try {
						 				SessionHelper sh = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
							 			BiSchema schema = BiSchema.loadSchema(sh);
								 		BiResult br = schema.getViewByName(viewName).newBiResult(sh.getLoginId(), null, null, sh);
									 	String condStr = (String) child.getAttribute("listCondition");
										 if(condStr != null) {
											 br.addCustomCondition(condStr);
	 									}
		 								br.query();
			 							String colName  = (String) child.getAttribute("listColumn");
				 						Vector<String> v = new Vector<String>();
					 					for(int i=0;i<br.getRowCount();i++) {
						 					br.loadOneRecV(i);
							 				v.add(br.getCellString(colName));
								 		}
					 					String value = c.getString();
									 	Collections.sort(v);
										c.setItemList(v);
//	 									c.set(((Combobox) child).getValue());
	 									c.set(value);
		 							} catch (Exception ex) {
			 							UniLog.log(ex);
				 					}

					 			}
	 						}
				 			if(child instanceof ZkJxPickInput)  {
				 				String viewName = (String) child.getAttribute("listView");
				 				if(viewName != null) {
				 					try {
			 							String condStr = (String) child.getAttribute("listCondition");
				 						String colName  = (String) child.getAttribute("listColumn");
					 				} catch (Exception ex) {
						 				UniLog.log(ex);
									}
							 	}
				 			}
							c.mapAdd(cm);
					 	 }
					 }
				 }
			 }
			 addCellsToCollection(child,p_listener,p_col);
		}
	}
	public void doModal(CellCollection p_col,EventListener p_listener) throws CellException {
		addCellsToCollection(rootComp,p_listener,p_col);
		listener = p_listener;
		collection = p_col;
		if(rootComp instanceof Window) {
			((Window) rootComp).doModal();
		}
	}
	public void exitModal() {
		if(rootComp instanceof Window) {
			((Window) rootComp).setVisible(false);
		}
	}
	
	public Component getComponent(String p_id) {
		return(rootComp.getFellowIfAny(p_id, true));
	}
	public void mapCellCollection(CellCollection p_col,EventListener p_listener) throws CellException {
		 addCellsToCollection(rootComp,p_listener,p_col);
		 listener = p_listener;
		collection = p_col;
	}
	/***
	 * trigger tbChangeListener manually
	 * e.g. checkbox value updated by program, it will sync the cell value back to cell
	 * @param p_comp
	 */
	public static void notifyUpdate(Component p_comp) {
		Events.echoEvent(Events.ON_CLICK, p_comp, null);
	}
	
	public Component getRootComponent( ) {
		return(rootComp);
	}

	public EventListener getTbChangeListener() {
		return(tbChangeListener);
	}
}
