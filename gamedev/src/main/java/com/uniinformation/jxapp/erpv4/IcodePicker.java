package com.uniinformation.jxapp.erpv4;

import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.XulElement;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.cell.AbstractGetItemProperty;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.GenbucketUtil;
//import com.uniinformation.estimation.database.EstDb;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.JxFormCloseListener;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
//import com.uniinformation.utils.AbstractGetItemProperty;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class IcodePicker extends JxForm {
	static final int PICKMODE_ICODE = 0;
	static final int PICKMODE_ICODE_ORG = 1;
	static final int PICKMODE_POALLOCATE = 2;
	int pickMode;
	TableRec stockTr;
	SelectUtil su;
	Cell icodeTarget;
	Cell orgTarget;
	Cell qtyTarget;
	double reqQty;
//	double reqQty;
//	Window wCreateStock = null;
	AbstractGetItemProperty gipi;
	JxZkBiBase parentBase;
	StockCreator jxf = null;
	protected String myViewName = null;
//	ZkBiComposerBase zkcb;
	HtmlBasedComponent nextComp=null;
	boolean isMobile = false;
	boolean canCreateStock = true;
	boolean showOicode = false;
	
	AbstractGetItemProperty gipi_poallocate = null;
	AbstractGetItemProperty get_gipi_poallocate()
	{
		if(gipi_poallocate == null) {
			
			gipi_poallocate = new AbstractGetItemProperty(){
				@Override
				public int getColumnCount(Object p_v) {
					return(1);
				}
				@Override
				public int getRowCount() {
					return(stockTr.getRecordCount());
				}
				@Override
				public Object getRow(int p_row) {
					return(stockTr.getRecord(p_row));
				}	
				@Override
				public int getIndexOf(Object p_v) {
					return(stockTr.getAllData().indexOf(p_v));
				}
				@Override
				public String getString(Object p_v) {
					return("HAHA");
				}
				@Override
				public int getRowWidth() {
					// TODO Auto-generated method stub
					return 0;
				}	
		};
		}
		return(gipi_poallocate);
	}
	
	
	void setSelectedRow( boolean p_isrelative, int p_idx) {
		int curidx;
		if(p_isrelative) {
			curidx = jxAdd("ipk_list").getCurrentRow();
			if(curidx < 0) curidx = 0;
		}  else curidx = 0;
		curidx += p_idx;
		jxAdd("ipk_list").gridSetCurrentRow(curidx);
	}
	public void setPickerForAnyStock( JxZkBiBase p_base,SelectUtil p_su, Wherecl p_where,Cell p_target,String p_filter,HtmlBasedComponent p_nextComp) {
		setPickerForAnyStockWithBalance( false,p_base,p_su, p_where,p_target,p_filter,p_nextComp) ;
	}
	public void setPickerForAnyStockWithBalance( boolean p_withBalance,JxZkBiBase p_base,SelectUtil p_su, Wherecl p_where,Cell p_target,String p_filter,HtmlBasedComponent p_nextComp) {
		su = p_su;
		icodeTarget = p_target;
		orgTarget = null;
		parentBase = p_base;
		nextComp = p_nextComp;
		try {
			jxAdd("btAdd").setVisible(canCreateStock); 
			Wherecl wcl = p_where;
			if(wcl == null) wcl = new Wherecl();
			wcl.andUniop("st_obsolete", "<>", "Y" );
			if(p_withBalance) {
				jxAdd("ipk_list").gridSetCol(3);
				stockTr = su.getQueryResult("select * from stock", wcl);
			} else {
				jxAdd("ipk_list").gridSetCol(2);
				stockTr = su.getQueryResult("select * from stock", wcl);
			}
			pickMode = PICKMODE_ICODE;
			jxAdd("ipk_list").setItemListInterface(gipi);
			if(p_filter != null) {
				jxAdd("ipk_filter").setText(p_filter);
				if(p_filter != null) 
				jxAdd("ipk_list").setAttribute("filter", p_filter);
			} else {
				jxAdd("ipk_filter").setText("");
			}
			setSelectedRow( false, 0);
//			jxAdd("ipk_list").gridSetCurrentRow(0);
//			HtmlBasedComponent co = (HtmlBasedComponent) jxAdd("ipk_list").getNativeObject();
			if(!isMobile) {
				XulElement filterComp = (XulElement) jxAdd("ipk_filter").getNativeObject();
				filterComp.focus();
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}

	public void setPickerForAvailableStock_org( JxZkBiBase p_base,SelectUtil p_su, Wherecl p_andWhere,Cell p_target,Cell p_orgTarget,Cell p_qtyTarget, double p_reqQty, int p_alcOrg, double p_alcQty) {
		su = p_su;
		icodeTarget = p_target;
		orgTarget = p_orgTarget;
		qtyTarget = p_qtyTarget;
//		reqQty = p_reqQty;
		reqQty = p_reqQty;
		parentBase = p_base;
		try {
			Wherecl wcl = new Wherecl();
//			wcl.appendString( "and pds_irg = st_irg and stmd_irg = pds_irg and stmd_org = pds_org and stmd_tdtype in ("+Erpv4Config.STOCK_IN_TDtypes+") and stm_mrg = stmd_mrg");
			wcl.appendString( "and pds_irg = st_irg and stmd_irg = pds_irg and stmd_org = pds_org and stmd_flag1 = 'Y' and stm_mrg = stmd_mrg");
			if(p_andWhere != null) wcl.andWherecl(p_andWhere);
			if(p_alcOrg > 0)  {
				wcl.andWherecl(new Wherecl().orUniop("pds_stockqty", ">", 0).orUniop("pds_org", "=", p_alcOrg));
			} else {
				wcl.andUniop("pds_stockqty", ">", 0);
			}
			
			jxAdd("btAdd").setVisible(false);
			jxAdd("ipk_list").gridSetCol(3);
			jxAdd("ipk_list").gridSetColHeader(1,"PO Number");
			jxAdd("ipk_list").gridSetColHeader(2,"Available Qty");

			stockTr = su.getQueryResult("select * from stock,podetstatus,stmov,stmovd ",wcl);
			if(p_alcOrg > 0) {
				for(int i=0;i<stockTr.getRecordCount();i++) {
					stockTr.setRecPointer(i);
					int thisOrg = (Integer) stockTr.getField("pds_org");
					if(thisOrg == p_alcOrg) {
						Object[] rec = stockTr.getRecord(i);
						int idx = stockTr.getFieldIndex("pds_stockqty");
						double alcQty = (Double) rec[idx];
						alcQty += p_alcQty;
						rec[idx] = new Double(alcQty);
					}
				}
			}
			pickMode = PICKMODE_ICODE_ORG;
			jxAdd("ipk_list").setItemListInterface(gipi);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	public void setPickerForAvailableStock( JxZkBiBase p_base,SelectUtil p_su, Wherecl p_andWhere,Cell p_target,Cell p_orgTarget,Cell p_qtyTarget, double p_reqQty, int p_alcOrg, double p_alcQty) {
		su = p_su;
		icodeTarget = p_target;
		orgTarget = p_orgTarget;
		qtyTarget = p_qtyTarget;
//		reqQty = p_reqQty;
		reqQty = p_reqQty;
		parentBase = p_base;
		try {
			Wherecl wcl = new Wherecl();
//			wcl.appendString( "and pds_irg = st_irg and stmd_irg = pds_irg and stmd_org = pds_org and stmd_tdtype in ("+Erpv4Config.STOCK_IN_TDtypes+") and stm_mrg = stmd_mrg");
			wcl.appendString( "and pds_irg = st_irg ");
			if(p_andWhere != null) wcl.andWherecl(p_andWhere);
			if(p_alcOrg > 0)  {
				wcl.andWherecl(new Wherecl().orUniop("pds_stockqty", ">", 0).orUniop("pds_org", "=", p_alcOrg));
			} else {
				wcl.andUniop("pds_stockqty", ">", 0);
			}
			jxAdd("btAdd").setVisible(false);
			jxAdd("ipk_list").gridSetCol(3);
			jxAdd("ipk_list").gridSetColHeader(1,"PO Number");
			jxAdd("ipk_list").gridSetColHeader(2,"Available Qty");

			stockTr = su.getQueryResult("select * , 'NO PO' stm_ref1,'' stmd_ref4 from stock,podetstatus",wcl);
				for(int i=0;i<stockTr.getRecordCount();i++) {
					stockTr.setRecPointer(i);
					int thisOrg = (Integer) stockTr.getField("pds_org");

			if(thisOrg < GenbucketUtil.WEIGHTED_AVERAGE_ORGMIN) {
				
				TableRec tr = su.getQueryResult("select * from stmov,stmovd where stmd_org = ? and stmd_flag1 = 'Y' and stm_mrg = stmd_mrg",
						new Wherecl().appendArgument(thisOrg)
					);
				if(tr.getRecordCount() == 1) {
					tr.setRecPointer(0);
//					(tr.getFieldString("stm_ref1"));
					Object[] rec = stockTr.getRecord(i);
					int idx = stockTr.getFieldIndex("stm_ref1");
					rec[idx] = tr.getFieldString("stm_ref1");
				}
			}
			
			if(p_alcOrg > 0) {
					if(thisOrg == p_alcOrg) {
						Object[] rec = stockTr.getRecord(i);
						int idx = stockTr.getFieldIndex("pds_stockqty");
						double alcQty = (Double) rec[idx];
						alcQty += p_alcQty;
						rec[idx] = new Double(alcQty);
					}
			}
				}
			pickMode = PICKMODE_ICODE_ORG;
			jxAdd("ipk_list").setItemListInterface(gipi);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	
//	public void setPickerForPoallocate( JxZkBiBase p_base,SelectUtil p_su, Wherecl p_where,Cell p_target) {
//		su = p_su;
//		icodeTarget = null;
//		orgTarget = null;
//		parentBase = null;
//		try {
//			jxAdd("btAdd").setVisible(false);
//			jxAdd("ipk_list").gridSetCol(2);
//			stockTr = su.getQueryResult("select * from poallocate", null);
//			jxAdd("ipk_list").setItemListInterface(get_gipi_poallocate());
//			pickMode = PICKMODE_POALLOCATE;
//		} catch (Exception ex) {
//			UniLog.log(ex);
//		}
//	}
	
	ReturnMsg updateToCells(Object[] rec) throws CellException
	{
		ReturnMsg rtnMsg;
		if(icodeTarget != null) {
			rtnMsg = icodeTarget.update( rec[stockTr.getFieldIndex("st_icode")]);
			if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		}
		if(pickMode == PICKMODE_ICODE_ORG) {
			rtnMsg = orgTarget.update( rec[stockTr.getFieldIndex("pds_org")]);
			if(rtnMsg != null && !rtnMsg.getStatus())  return(rtnMsg);
			if(qtyTarget != null) {
				double freeQty = (Double) rec[stockTr.getFieldIndex("pds_stockqty")];
				if(freeQty > reqQty) freeQty = reqQty;
				rtnMsg = qtyTarget.update(freeQty);
				if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
			}
		}
		return(null);
	}
	
	void pickItemAndClose() {
		Vector<Object[]>  v = jxAdd("ipk_list").getSelectList();
		if(v != null && v.size() > 0) {
			try {
					ReturnMsg rtnMsg = updateToCells(v.get(0));
					if(rtnMsg != null && !rtnMsg.getStatus())  {
						messageBox(rtnMsg.getMsg());
					};
					
				
//					ReturnMsg rtnMsg = target.update(
//						((Object []) v.get(0))[stockTr.getFieldIndex("st_icode")]
//							);
//					if(rtnMsg != null && !rtnMsg.getStatus())  {
//						messageBox(rtnMsg.getMsg());
//					} else {
//						if(pickMode == PICKMODE_ICODE_AND_ORG) {
//							rtnMsg = orgTarget.update( v.get(0)[stockTr.getFieldIndex("pds_org")]);
//							if(rtnMsg != null && !rtnMsg.getStatus())  {
//								messageBox(rtnMsg.getMsg());
//								return;
//							}
//							if(qtyTarget != null) {
//								double freeQty = (Double) v.get(0)[stockTr.getFieldIndex("pds_stockqty")];
//								if(freeQty > reqQty) freeQty = reqQty;
//								rtnMsg = qtyTarget.update(freeQty);
//								if(rtnMsg != null && !rtnMsg.getStatus())  {
//									messageBox(rtnMsg.getMsg());
//									return;
//								}
//							}
//						}
//					}
					if(nextComp != null) nextComp.focus();
					closeForm();
			} catch (CellException cex) {
				UniLog.log(cex);
				messageBox("Unknown Item Pick Error ");
			}
		}
		
	}
	@Override
	public void afterBind() {
		super.afterBind();
		myViewName = "erpv4.StockCreator";
		//btSelect removed in single click version
		new JxFieldAction("btSelect") {
			public void actionPerformed(JxField fd){
				pickItemAndClose();
			}
		};
		
		new JxFieldAction("btAdd") {
			public void actionPerformed(JxField fd){
				if(jxf == null) {
					Window wCreateStock= wCreateStock = parentBase.newPopupWindow("Create Spare Parts Record");
					wCreateStock.setWidth("640px");
					wCreateStock.setHeight("300px");
					SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
					JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
					jxf =  (StockCreator) parentBase.getOrCreateJxZkForm(wCreateStock,pvdr ,myViewName);
					jxf.addFormCloseListener(
					new JxFormCloseListener( ) {
						public int formClose(JxForm jxf) {
								return(JxFormCloseListener.caHide);
						}
					}	
					);
					
					
				}
//				parentBase.zkcb.modelWindow(wCreateStock);
				jxf.modalExecute(su,parentBase.getRpcClient(), icodeTarget);
			}
		};
		new JxFieldAction("ipk_list") {
			public void actionPerformed(JxField fd){
				switch(fd.getActionType()) {
				case JxField.ACTIONTYPE_CLICK:
				case JxField.ACTIONTYPE_DOUBLECLICK:
				case JxField.ACTIONTYPE_SELECT:
					pickItemAndClose();
					break;
				}
			}
		};	
		new JxFieldChange("ipk_filter") {
			public boolean valueChanged(JxField fd,String p_text){
				jxAdd("ipk_list").setAttribute("filter", fd.getText());
				setSelectedRow( false, 0);
				return(true);
			}
			
		};
		//jxAdd("ipk_list").setAttribute("mode", "doubleClickAction");
		jxAdd("ipk_list").setAttribute("mode", "singleClickAction");
		gipi = new AbstractGetItemProperty(){
			int idx;
			public int getColumnCount(Object p_v) {
				switch(pickMode) {
				case PICKMODE_ICODE_ORG: return(3);
				default : if(showOicode) return(3); else return(2);
				}
			}
			public int getColumnSpan(Object p_v,int p_col) {
				return(1);
			}
			public Object getColumnValue(Object p_v,int p_col) {
				switch(p_col) {
				case 0:
					idx = stockTr.getFieldIndex("st_icode");
					return(((Object[]) p_v)[idx]);
					/*
				case 1:
					idx = stockTr.getFieldIndex("st_iname");
					return(((Object[]) p_v)[idx]);
					*/
				case 1:
			if(pickMode == PICKMODE_ICODE_ORG) {
					idx = stockTr.getFieldIndex("stm_ref1");
					String ss = (String) (((Object[]) p_v)[idx]);
					idx = stockTr.getFieldIndex("stmd_ref4");
					String sr = (String) (((Object[]) p_v)[idx]);
					if(sr == null || sr.trim().equals("")) {
						return(ss);
					} else {
						return(ss+"("+sr+")");
					}
				
			} else {
					idx = stockTr.getFieldIndex("st_iname");
					return(((Object[]) p_v)[idx]);
			}
				case 2:
					if(pickMode == PICKMODE_ICODE_ORG) {
						idx = stockTr.getFieldIndex("pds_stockqty");
						return(((Object[]) p_v)[idx]);
					} else {
						idx = stockTr.getFieldIndex("st_oicode");
						return(((Object[]) p_v)[idx]);
					}
				default : return(null);
				}
			}
			public Object getColumnValueByName(Object p_v,String p_name) {
				return(null);
			}
			public String getColumnLabel(Object p_v,int p_col) {
				return( getColumnValue(p_v,p_col).toString());
			}
			public String getString(Object p_v) {
				String s =
						getColumnValue(p_v,0).toString() + " " +
						getColumnValue(p_v,1).toString();
				if(showOicode) {
						s += " " + getColumnValue(p_v,2).toString();
				}
				return( s);
			}	
			public String getHeader(Object p_v,int p_col) {
				switch(p_col) {
				case 0:
					return("Item Code");
					/*
				case 1:
					return("Item Name");
					*/
				case 1:
					return("PO Number");
				case 2:
					return("Stock Qty");
				default : return(null);
				}
			}
			@Override
			public String getColumnWidth(Object item,int p_col){
				if(pickMode == PICKMODE_ICODE_ORG) {
					if(isMobile) {
						switch(p_col){
						case 0: return("30%");
						case 1: return("40%");
						case 2: return("30%");
						}
					} else {
						switch(p_col){
						case 0: return("150px");
						case 1: return("200px");
						case 2: return("100%");
						}
					}
				} else {
					if(showOicode) {
						if(isMobile) {
							switch(p_col){
							case 0: return("30%");
							case 1: return("50%");
							case 2: return("20%");
							}
						} else {
							switch(p_col){
							case 0: return("150px");
							case 1: return("100%");
							case 2: return("150px");
							}
						}
					} else {
						if(isMobile) {
							switch(p_col){
							case 0: return("30%");
							case 1: return("70%");
							}
						} else {
							switch(p_col){
							case 0: return("150px");
							case 1: return("100%");
							}
						}
					}
				}
				return(null);
			}	
			public int getRowCount() {
				return(stockTr.getRecordCount());
			}
			public Object getRow(int p_row) {
				return(stockTr.getRecord(p_row));
			}
			
			public int getIndexOf(Object p_v) {
				return(stockTr.getAllData().indexOf(p_v));
			}
			public void onValueChanged(Object p_value,int p_ctype){
				
			}
			@Override
			public int getRowWidth() {
				// TODO Auto-generated method stub
				return 0;
			}
	};
			XulElement filterComp = (XulElement) jxAdd("ipk_filter").getNativeObject();
			filterComp.setCtrlKeys("#up#down");
			filterComp.addEventListener(Events.ON_OK, new EventListener(){
				@Override
				public void onEvent(Event p_event) throws Exception {
					pickItemAndClose();
			}});
			filterComp.addEventListener(Events.ON_CTRL_KEY, new EventListener(){
				@Override
				public void onEvent(Event p_event) throws Exception {
					if (p_event instanceof KeyEvent){
						int keyCode = ((KeyEvent) p_event).getKeyCode();
						switch(keyCode){
							case 40: 
								UniLog.logm(this,"down arrow click");
								setSelectedRow( true, 1);
							break;
							case 38: 
								UniLog.logm(this,"up arrow click");
								setSelectedRow( true, -1);
							break;
						}
					}
				}});
		
	}
	public void setShowOicode(boolean p_sw) {
		showOicode = p_sw;
	}
	public void setCanCreateStock(boolean p_sw) {
		canCreateStock = p_sw;
	}
	public void setIsMobile(boolean p_sw) {
		isMobile = p_sw;
	}
}
