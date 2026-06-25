package com.uniinformation.jxapp.erpv4;

import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.util.Template;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Window;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultQuoDet;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.JxFormCloseListener;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.JxZkSkin;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.AfsIcodePicker;
import com.uniinformation.jxapp.JxSelOpt;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiCellValueMapper;
import com.uniinformation.zkcomp.ZkBiButton;

public class SvUnit extends JxZkBiBase {
	JxSelOpt jxfSelOptForm = null;
	Window customScr = null;
	TrGetItemProperty tgipiSelLocation;
	TrGetItemProperty tgipiSelSeries;
	TrGetItemProperty tgipiSelIcode;
//	TrGetItemProperty tgipiSelOrder;
	TrGetItemProperty tgipiSelOrderSeries;
	@Override
	public void afterBind() {
		super.afterBind();
		IdSpace isp = (IdSpace) getNativeComponent();
		if(!isp.hasFellow("btPickOrderSeries",true)) {
			if(isp.hasFellow("stmcm_name",true)) {
				Component ca = isp.getFellow("stmcm_name",true);
				Hlayout hl = new Hlayout();
				hl.setParent(ca.getParent());
				ca.setParent(hl);
				Button bt = new ZkBiButton("Pick");
				bt.setId("btPickOrderSeries");
				bt.setParent(hl);
				((JxZkSkin) getSkin()).addOneElementToSkin(bt);
			};
			
		}
		
		SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
		if(sessionHelper.isMobileDevice()){
//			zkpi.setPopupWidth("100%");
		} else {
			ZkJxPickInput zkpi = (ZkJxPickInput) jxAdd("svloc_desp").getNativeObject();
			zkpi.setPopupWidth("500px");
//			zkpi = (ZkJxPickInput) jxAdd("inv_invno").getNativeObject();
//			zkpi.setPopupWidth("500px");
			zkpi = (ZkJxPickInput) jxAdd("st_icode").getNativeObject();
			zkpi.setPopupWidth("500px");
//			zkpi.setPopupHeight("400px");
		}
		new JxFieldAction("svloc_desp") {
			public void actionPerformed(JxField fd){
				UniLog.log("svloc_desp clicked");
				ZkJxPickInput zkpi = (ZkJxPickInput) jxAdd("svloc_desp").getNativeObject();
//				if(isMobile) {
////					zkpi.setPopupWidth("100%");
//				} else {
//					zkpi.setPopupWidth("500px");
////					zkpi.setPopupHeight("400px");
//				}
				pickLocation((ColumnCell) getBr().getCell("svloc_desp"),zkpi);
			}
		};			
		new JxFieldAction("stmcm_name") {
			public void actionPerformed(JxField fd){
				UniLog.log("stmcm_name clicked");
				ZkJxPickInput zkpi = (ZkJxPickInput) jxAdd("stmcm_name").getNativeObject();
//				if(isMobile) {
//					zkpi.setPopupWidth("100%");
//				} else {
//					zkpi.setPopupWidth("1000px");
//				}
//				zkpi.setPopupHeight("500px");
				pickSeries((ColumnCell) getBr().getCell("stmcm_name"),zkpi);
			}
		};			
		new JxFieldAction("st_icode") {
			public void actionPerformed(JxField fd){
				UniLog.log("st_icode clicked");
				ZkJxPickInput zkpi = (ZkJxPickInput) jxAdd("st_icode").getNativeObject();
//				if(isMobile) {
//					zkpi.setPopupWidth("100%");
//				} else {
//					zkpi.setPopupWidth("1000px");
//				}
//				zkpi.setPopupHeight("500px");
				pickItemCode((ColumnCell) getBr().getCell("st_icode"),zkpi);
			}
		};			
//		new JxFieldAction("inv_invno") {
//			public void actionPerformed(JxField fd){
//				UniLog.log("inv_invno clicked");
//				ZkJxPickInput zkpi = (ZkJxPickInput) jxAdd("inv_invno").getNativeObject();
////				if(isMobile) {
////					zkpi.setPopupWidth("100%");
////				} else {
////					zkpi.setPopupWidth("1000px");
////				}
////				zkpi.setPopupHeight("500px");
//				pickOrder((ColumnCell) getBr().getCell("inv_invno"),zkpi);
//			}
//		};			
		
		new JxFieldChange("stmcm_name") {

			@Override
			public boolean valueChanged(JxField jxfield, String orgvalue) {
				// TODO Auto-generated method stub
				if(jxfield.getText().trim().equals("")) {
					Vector v = getBr().getSubLink("AfsServiceItem").getRowCollectionList();
					if(v.size() > 0) {
						messageBox("To Unset Series Name, please delete all subitem first");
						return(false);
					}
				}
				setScreen(!jxfield.getText().trim().equals(""));
				return true;
			}
			
		};
		new JxFieldAction("btPickOrderSeries") {
			public void actionPerformed(JxField fd){
				UniLog.log("pick order series");
				if(customScr == null) {
					customScr = newPopupWindow("Pick Sales Order");
					customScr.setId("popup1");
					customScr.setWidth("1000px");
//					customScr.setHeight("300px");
					customScr.setHeight("");
//					customScr.setWidth("");
					customScr.setVflex("1");
//					customScr.setHflex("min");
//                	customScr.setContentStyle("overflow:auto;");
					customScr.setVisible(false);
				}
				pickOrderSeries(); 
			}
		};			
	}
	void setScreen(boolean p_isSeries) {
		if(p_isSeries) {
//			jxSetEnable("st_icode",false);
//			jxSetVisible("st_icode",false);
//			jxSetVisible("lb_st_icode",false);
//			jxSetVisible("st_iname",false);
//			jxSetVisible("lb_st_iname",false);
			jxSetVisible("list_AfsServiceItem",true);
		} else {
//			jxSetEnable("st_icode",true);
//			jxSetVisible("st_icode",true);
//			jxSetVisible("lb_st_icode",true);
//			jxSetVisible("st_iname",true);
//			jxSetVisible("lb_st_iname",true);
			jxSetVisible("list_AfsServiceItem",false);
		}
	}
	void createSelOpt() {
		if(jxfSelOptForm == null) {
		SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
		JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sessionHelper.getSessionData("jxzkgadgetprovider");
//		jxfSelQuotation = JxSelOpt.createJxSelOpt(pvdr,"SelOptQuotation");
		jxfSelOptForm = JxSelOpt.createJxSelOpt(pvdr);
		if(isMobile) {
//			int cc = sessionHelper.getScreenWidth();
//			if( cc <= 100) {
//				jxfSelOptForm.setWidth("100%");
//				p_zkpi.setPopupWidth("100%");
//			} else {
//				jxfSelOptForm.setWidth(""+(cc - 20)+"px");
//				p_zkpi.setPopupWidth(""+(cc - 20)+"px");
//			}
		} else {
//			jxfSelOptForm.setWidth("1000px");
//			p_zkpi.setPopupWidth("1000px");
		}
		jxfSelOptForm.setOnSelectAction (
			new JxActionListener() {
				public void actionPerformed(JxField fd) {
					Object[] rec = (Object[]) jxfSelOptForm.getPickListBoxValue();
					if (rec != null) {
						try {
							ColumnCell ccol = (ColumnCell) jxfSelOptForm.getUserData();
							SelectUtil su = getBr().getSelectUtil();
							if(ccol.getBiColumn().getLabel().equals("svloc_desp")) {
								TableRec tr = tgipiSelLocation.getTableRec();
								getBr().getCell("svmc_locrg").set( rec[tr.getFieldIndex("svloc_rg")] );
							}
							if(ccol.getBiColumn().getLabel().equals("st_icode")) {
								TableRec tr = tgipiSelIcode.getTableRec();
								getBr().getCell("svmc_irg").set( rec[tr.getFieldIndex("st_irg")] );
								getBr().getCell("svmc_odrg").set( rec[tr.getFieldIndex("ind_odrg")] );
								getBr().getCell("svmc_qirg").set( rec[tr.getFieldIndex("ind_irg")] );
								getBr().getCell("svmc_serno").set( rec[tr.getFieldIndex("stsn_ref4")] );
								getBr().getCell("svmc_org").set( rec[tr.getFieldIndex("stsn_org")] );
							}
							if(ccol.getBiColumn().getLabel().equals("std_icode")) {
								CellCollection cl = ccol.getCollection();
								TableRec tr = tgipiSelIcode.getTableRec();
								cl.getCell("svmi_irg").set( rec[tr.getFieldIndex("st_irg")] );
								cl.getCell("svmi_odrg").set( rec[tr.getFieldIndex("ind_odrg")] );
								cl.getCell("svmi_qirg").set( rec[tr.getFieldIndex("ind_irg")] );
								cl.getCell("svmi_serno").set( rec[tr.getFieldIndex("stsn_ref4")] );
								cl.getCell("svmi_org").set( rec[tr.getFieldIndex("stsn_org")] );
							}
							if(ccol.getBiColumn().getLabel().equals("stmcm_name")) {
								TableRec tr = tgipiSelSeries.getTableRec();
								getBr().getCell("svmc_phrg").set( rec[tr.getFieldIndex("stmcm_rg")] );
								getBr().getCell("svmc_odrg").set( rec[tr.getFieldIndex("ind_odrg")] );
//								if(getBr().getCell("stmcm_name").getString().trim().equals("")) {
//									setScreen(false);
//								} else {
//									getBr().getCell("svmc_irg").set(0);
//									setScreen(true);
//								}
								setScreen(!getBr().getCell("stmcm_name").getString().trim().equals(""));
							}
//							if(ccol.getBiColumn().getLabel().equals("inv_invno")) {
//								TableRec tr = tgipiSelOrder.getTableRec();
//								TableRec tr2 = getBr().getSelectUtil().getQueryResult("select ind_odrg from quodet where ind_rg = "  + 
//											(Integer) rec[tr.getFieldIndex("inv_rg")]
//										);
//								if(tr2.getRecordCount() > 0) {
//									tr2.setRecPointer(0);
//									getBr().getCell("svmc_modrg").set( tr2.getFieldInt("ind_odrg"));
//									getBr().getCell("svmc_phrg").set( 0 );
//									setScreen(!getBr().getCell("stmcm_name").getString().trim().equals(""));
//								} else getBr().getCell("svmc_modrg").set( 0);
//							}
							if(ccol.getBiColumn().getLabel().equals("svmc_modrg")) {
								TableRec tr = tgipiSelOrderSeries.getTableRec();
								getBr().getCell("svmc_modrg").set( rec[tr.getFieldIndex("ind_odrg")] );
								getBr().getCell("svmc_phrg").set( rec[tr.getFieldIndex("ind_srg")] );
								getBr().getCell("svmc_serno").set( rec[tr.getFieldIndex("serialno")] );
								setScreen(!getBr().getCell("stmcm_name").getString().trim().equals(""));
//								getBr().getCell("stmcm_name").syncMode(Cell.VMODE_DISPONLY);
								//cd.isOverride
								getBr().getCell("stmcm_name").setMode(Cell.VMODE_DISPONLY);
								if(getBr().getCell("svmc_modrg").getInt() > 0){
									BiResult sr = getBr().getSubLink("AfsServiceItem");
									if(sr.getRowCount() <= 0) {
										tr = getAllItemsFromSeries(getBr().getCell("svmc_modrg").getInt());
										for(int i = 0;i<tr.getRecordCount();i++) {
											tr.setRecPointer(i);
											JxField sv = jxAdd("list_"+JxZkBiBase.replaceViewName(sr.getView().getName()));
											CellCollection col = sr.newRowCollection();
											col.getCell("svmi_irg").set(tr.getFieldInt("st_irg"));
											col.getCell("svmi_odrg").set(tr.getFieldInt("ind_odrg"));
											col.getCell("svmi_qirg").set(tr.getFieldInt("ind_irg"));
											col.getCell("svmi_serno").set(tr.getFieldString("stsn_ref4"));
											col.getCell("svmi_org").set(tr.getFieldInt("stsn_org"));
											ReturnMsg rtn = sr.addSubRecord(col, i,"");
											Object o = rtn.getData();
											int rowIdx = getGipi(sr.getView().getName()).getIndexOf(o);
											sv.addItemToList(o, rowIdx);
										}
									}
								}
							}
						} catch (Exception cex ) {  
							UniLog.log(cex);
						} 
					}
					jxfSelOptForm.closeForm();
				}
			}
		);
		}
		
	}
	void createSelectopt(ZkJxPickInput p_zkpi) {
		createSelOpt();
		if(tgipiSelLocation == null) {
			tgipiSelLocation = new TrGetItemProperty(
				new VectorUtil()
					.addElement("svloc_desp")
					.toVector(),
				new VectorUtil()
					.addElement("Location")
					.toVector(),
				new VectorUtil()
					.addElement("100%")
					.toVector()
			);
		}
		if(tgipiSelSeries == null) {
			tgipiSelSeries = new TrGetItemProperty(
				new VectorUtil()
					.addElement("stmcm_name")
					.addElement("serialno")
					.toVector(),
				new VectorUtil()
					.addElement("Series")
					.addElement("Serial No.")
					.toVector(),
				new VectorUtil()
					.addElement("50%")
					.addElement("50%")
					.toVector()
			);
		}
		if(tgipiSelIcode == null) {
			tgipiSelIcode = new TrGetItemProperty(
				new VectorUtil()
					.addElement("st_icode")
					.addElement("st_iname")
					.addElement("stsn_ref4")
					.toVector(),
				new VectorUtil()
					.addElement("Code")
					.addElement("Name")
					.addElement("Serial No.")
					.toVector(),
				new VectorUtil()
					.addElement("30%")
					.addElement("40%")
					.addElement("30%")
					.toVector()
			);
		}
//		if(tgipiSelOrder == null) {
//			tgipiSelOrder = new TrGetItemProperty(
//				new VectorUtil()
//					.addElement("inv_vcode")
//					.addElement("inv_invno")
//					.addElement("inv_pocode")
//					.addElement("inv_contract")
//					.toVector(),
//				new VectorUtil()
//					.addElement("Customer")
//					.addElement("Order No.")
//					.addElement("Customer PO")
//					.addElement("Contract No")
//					.toVector(),
//				new VectorUtil()
//					.addElement("20%")
//					.addElement("30%")
//					.addElement("30%")
//					.addElement("20%")
//					.toVector()
//			);
//		}
	}
	boolean pickOrderSeries() {
		createSelOpt();
		if(tgipiSelOrderSeries == null) {
			String w0;
			if(isMobile) w0 = "300px"; else w0 = "100%";
			tgipiSelOrderSeries = new TrGetItemProperty(
				new VectorUtil()
					.addElement("inv_vcode")
					.addElement("inv_invno")
					.addElement("inv_pocode")
					.addElement("inv_contract")
					.addElement("stmcm_name")
					.addElement("serialno")
					.toVector(),
				new VectorUtil()
					.addElement("Customer")
					.addElement("Order No.")
					.addElement("Customer PO")
					.addElement("Contract No")
					.addElement("Series")
					.addElement("Serial No")
					.toVector(),
//				new VectorUtil()
//					.addElement("20%")
//					.addElement("30%")
//					.addElement("30%")
//					.addElement("20%")
//					.toVector()
				new VectorUtil()
					.addElement("100px")
					.addElement("150px")
					.addElement("150px")
					.addElement("150px")
					.addElement(w0)
					.addElement("150px")
					.toVector()
			);
		}
		try {
			TableRec tr=null;
//			tr = getBr().getSelectUtil().getQueryResult
//						("select distinct inv_invno,inv_contract,inv_pocode,inv_rg,inv_vcode"
//								+ "	from poallocate,quodet,quotation"
//								+ " where palc_serviceqty > 0 and ind_odrg = palc_qorg and inv_rg = ind_rg order by 1,2,3,4,5");
			tr = getBr().getSelectUtil().getQueryResult
						("select inv_invno,inv_contract,inv_pocode,inv_rg,inv_vcode,stmcm_name,ind_srg,ind_odrg,ind_seq,callsegs('get_series_main_serial',inv_rg,ind_seq) serialno "
								+ "	from quodet,quotation,stmcmodel"
								+ " where inv_rg = ind_rg and ind_pdsrg = 5 and ind_odrg not in (select svmc_modrg from sv_machine) "
								+ " and inv_vcode <> 'C0576'"
								+ " and stmcm_rg = ind_srg order by 1,2,3,4,5");
			if(tr.getRecordCount() > 0) {
				tgipiSelOrderSeries.setTableRec(tr);
				jxfSelOptForm.setUserData(getBr().getCell("svmc_modrg"));
				jxfSelOptForm.jxAdd("pickListBox").setItemListInterface(tgipiSelOrderSeries);	
				jxfSelOptForm.beginPick(!isMobile);
				((Component) jxfSelOptForm.getNativeComponent()).setParent(customScr);
				jxfSelOptForm.addFormCloseListener(
								new JxFormCloseListener( ) {
									public int formClose(JxForm jxf) {
										customScr.setVisible(false);
										return(JxFormCloseListener.caNone);
									}
								}	
							);
				customScr.doModal();
			} 
		} catch (Exception ex) {
			UniLog.log (ex);
		}
		return(true);
	}
	boolean pickSeries(ColumnCell p_bcc,ZkJxPickInput p_zkpi) {
		createSelectopt(p_zkpi);
		p_zkpi.setJxZkForm(jxfSelOptForm);
		try {
			TableRec tr=null;
			if(getBr().getCell("inv_rg").getInt() > 0) {
				tr = getBr().getSelectUtil().getQueryResult("select stmcm_name,stmcm_rg,ind_odrg,ind_rg,ind_seq,callsegs('get_series_main_serial',ind_rg,ind_seq) serialno from stmcmodel, quodet where stmcm_rg = ind_srg and ind_pdsrg = 5 and ind_rg = " + getBr().getCell("inv_rg").getInt());
			} else {
				tr = getBr().getSelectUtil().getQueryResult("select stmcm_name,stmcm_rg,0 ind_odrg,0 ind_rg, 0 ind_seq,'' serialno from stmcmodel");
			}
			if(tr.getRecordCount() > 0) {
				tgipiSelSeries.setTableRec(tr);
				jxfSelOptForm.setUserData(p_bcc);
				jxfSelOptForm.jxAdd("pickListBox").setItemListInterface(tgipiSelSeries);	
				jxfSelOptForm.beginPick(!isMobile);
			} else p_zkpi.close();
		} catch (Exception ex) {
			UniLog.log (ex);
		}
		return(true);
	}
	
	boolean pickLocation(ColumnCell p_bcc,ZkJxPickInput p_zkpi) {
		createSelectopt(p_zkpi);
		p_zkpi.setJxZkForm(jxfSelOptForm);
		try {
			TableRec tr=null;
			tr = getBr().getSelectUtil().getQueryResult("select * from sv_loc");
			if(tr.getRecordCount() > 0) {
				tgipiSelLocation.setTableRec(tr);
				jxfSelOptForm.setUserData(p_bcc);
				jxfSelOptForm.jxAdd("pickListBox").setItemListInterface(tgipiSelLocation);	
				jxfSelOptForm.beginPick(!isMobile);
			} else p_zkpi.close();
		} catch (Exception ex) {
			UniLog.log (ex);
		}
		return(true);
	}
	
	TableRec getAllItemsFromSeries(int p_srg) {
		TableRec tr=null;
		try {
				int stseq = 0;
				int invrg = 0;
				tr = getBr().getSelectUtil().getQueryResult("select ind_seq,ind_rg from quodet where ind_odrg = " + p_srg);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					stseq = tr.getFieldInt("ind_seq");
					invrg = tr.getFieldInt("ind_rg");
					tr = getBr().getSelectUtil().getQueryResult("select * from quodet where ind_rg = " + invrg + 
							" and ind_seq > " + stseq + " order by ind_seq");
					int i =0;
					for(i=0;i< tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						BiResultQuoDet.DELTATYPE pdsrg = BiResultQuoDet.getDeltaType(getSessionHelper(),tr.getFieldInt("ind_pdsrg"));
						switch(pdsrg) {
						case DELTALTYPE_COMBO_ITEM:
						case DELTALTYPE_LINEBREAK:
						case DELTALTYPE_TRADEIN:
							break;
						}
					}
					tr = getBr().getSelectUtil().getQueryResult("select st_irg,st_icode,st_iname,ind_odrg,stsn_org, stsn_ref4,ind_irg,ind_seq from quodet,stock,poallocate,stockserial,stmovd where " + 
							" ind_rg = ? and palc_qorg = ind_odrg and palc_serviceqty > 0 and stsn_serviceqty > 0 and stmd_qorg = ind_odrg and stmd_tdtype = 'SO' and stmd_org = palc_org and stmd_irg = palc_irg and stsn_irg = stmd_irg and stsn_org = stmd_org and stsn_ref4 = stmd_ref4" +
							" and st_irg = ind_irg and ind_pdsrg  =  ? and ind_seq between ? and ? order by ind_seq",
							new Wherecl()
								.appendArgument(invrg)
								.appendArgument(BiResultQuoDet.getPdsrg(getSessionHelper(),BiResultQuoDet.DELTATYPE.DELTALTYPE_STOCK_ITEM))
								.appendArgument(stseq+1)
								.appendArgument(stseq+i)
							);
				}
			
		} catch (Exception ex) {
			UniLog.log (ex);
			return(null);
		}
		return(tr);
	}
	boolean pickItemCode(ColumnCell p_bcc,ZkJxPickInput p_zkpi) {
		createSelectopt(p_zkpi);
		p_zkpi.setJxZkForm(jxfSelOptForm);
		try {
			TableRec tr=null;
			if(getBr().getCell("svmc_modrg").getInt() > 0) {
				tr = getAllItemsFromSeries(getBr().getCell("svmc_modrg").getInt());
//				int stseq = 0;
//				tr = getBr().getSelectUtil().getQueryResult("select ind_seq from quodet where ind_odrg = " + getBr().getCell("svmc_modrg").getInt());
//				if(tr.getRecordCount() > 0) {
//					tr.setRecPointer(0);
//					stseq = tr.getFieldInt("ind_seq");
//					tr = getBr().getSelectUtil().getQueryResult("select * from quodet where ind_rg = " + getBr().getCell("ind_rg").getInt() + 
//							" and ind_seq > " + stseq + " order by ind_seq");
//					int i =0;
//					for(i=0;i< tr.getRecordCount();i++) {
//						tr.setRecPointer(i);
//						int pdsrg = tr.getFieldInt("ind_pdsrg");
//						switch(pdsrg) {
//						case BiResultQuoDet.DELTALTYPE_COMBO_ITEM:
//						case BiResultQuoDet.DELTALTYPE_LINEBREAK:
//						case BiResultQuoDet.DELTALTYPE_TRADEIN:
//							break;
//						}
//					}
//					tr = getBr().getSelectUtil().getQueryResult("select st_irg,st_icode,st_iname,ind_odrg,stsn_org, stsn_ref4,ind_irg,ind_seq from quodet,stock,poallocate,stockserial,stmovd where " + 
//							" ind_rg = ? and palc_qorg = ind_odrg and palc_serviceqty > 0 and stsn_serviceqty > 0 and stmd_qorg = ind_odrg and stmd_tdtype = 'SO' and stmd_org = palc_org and stmd_irg = palc_irg and stsn_irg = stmd_irg and stsn_org = stmd_org and stsn_ref4 = stmd_ref4" +
//							" and st_irg = ind_irg and ind_pdsrg  =  ? and ind_seq between ? and ? order by ind_seq",
//							new Wherecl()
//								.appendArgument(getBr().getCell("ind_rg").getInt())
//								.appendArgument(BiResultQuoDet.DELTALTYPE_STOCK_ITEM)
//								.appendArgument(stseq+1)
//								.appendArgument(stseq+i)
//							);
//				}
			} else {
				if(getBr().getCell("svmc_phrg").getInt() > 0) {
					tr = getBr().getSelectUtil().getQueryResult
						("select st_irg,st_icode,st_iname,0 ind_odrg,0 stsn_org, '' stsn_ref4 ,0 ind_irg from stock,mcfitmodel where st_mtype in ('M','O') and mcfm_mrg = st_irg and mcfm_modelrg = " + getBr().getCell("svmc_phrg").getInt());
				} else {
					tr = getBr().getSelectUtil().getQueryResult("select st_irg,st_icode,st_iname,0 ind_odrg,0 stsn_org,'' stsn_ref4 ,0 ind_irg from stock where st_mtype in ('M','O')");
				}
			}
			if(tr != null && tr.getRecordCount() > 0) {
				tgipiSelIcode.setTableRec(tr);
				jxfSelOptForm.setUserData(p_bcc);
				jxfSelOptForm.jxAdd("pickListBox").setItemListInterface(tgipiSelIcode);	
				jxfSelOptForm.beginPick(!isMobile);
			} else p_zkpi.close();
		} catch (Exception ex) {
			UniLog.log (ex);
		}
		return(true);
	}
//	boolean pickOrder(ColumnCell p_bcc,ZkJxPickInput p_zkpi) {
//		createSelectopt(p_zkpi);
//		p_zkpi.setJxZkForm(jxfSelOptForm);
//		try {
//			TableRec tr=null;
//			tr = getBr().getSelectUtil().getQueryResult
//						("select inv_invno,inv_contract,inv_pocode,inv_rg,inv_vcode"
//								+ "	from poallocate,quodet,quotation"
//								+ " where palc_serviceqty > 0 and ind_odrg = palc_qorg and inv_rg = ind_rg order by 1,2,3,4,5");
//			if(tr.getRecordCount() > 0) {
//				tgipiSelOrder.setTableRec(tr);
//				jxfSelOptForm.setUserData(p_bcc);
//				jxfSelOptForm.jxAdd("pickListBox").setItemListInterface(tgipiSelOrder);	
//				jxfSelOptForm.beginPick(!isMobile);
//			} else p_zkpi.close();
//		} catch (Exception ex) {
//			UniLog.log (ex);
//		}
//		return(true);
//	}
	class ServiceItemGetItemProperty extends BiGetItemProperty {
		Template serviceUnitTemplate=null;
		Vector listColumns=null;
		ServiceItemGetItemProperty(final BiResult p_br) {
			super(p_br);
			serviceUnitTemplate = ((Component) getNativeComponent()).getTemplate("template_ServiceUnit_Item");
			if(serviceUnitTemplate != null) {
				listColumns = new Vector();
				listColumns.add(serviceUnitTemplate);
			}
		}
		@Override
		public Object getHeader(Object p_v,int p_col) {
			if(serviceUnitTemplate == null)return(super.getHeader(p_v, p_col));
			return("Service Item");
		}	
		@Override
		public String getColumnWidth(Object p_v ,int p_col){
			if(serviceUnitTemplate == null) return(super.getColumnWidth(p_v, p_col));
			return("100%");
		}
		
		@Override
		public Object getColumnValue(Object p_v,int p_col) {
			if(serviceUnitTemplate == null) return(super.getColumnValue(p_v, p_col));
			return(serviceUnitTemplate);
		}	
		@Override
		public int getColumnSpan(Object p_v,int p_col) {
			if(serviceUnitTemplate == null) return(super.getColumnSpan(p_v, p_col));
			return(1);
		}
		@Override
		protected Vector getListColumns(Object p_v) {
			if(serviceUnitTemplate == null) return(super.getListColumns(p_v));
			return(listColumns);
		}	
		@Override
		public void onValueChanged(Object p_value,int p_ctype) {
			ColumnCell bcc = (ColumnCell) p_value;
			if(p_ctype != GIPI_CELL_MAPPED) {;
				setDirtyFlag(true);
			} else {
				if(bcc.getCellLabel().equals("std_icode")) {
					if(!sessionHelper.isMobileDevice()){
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						zjpi.setPopupWidth("500px");
					}
				}
			}
			switch(p_ctype ) {
			case GIPI_PULLDOWN_CLOSED :
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " closed ");
				break;
			case GIPI_PULLDOWN_OPENED:
				UniLog.log("ColumnCell " + bcc.getBiColumn().getLabel()+ " opened ");
				if(bcc.getBiColumn().getLabel().equals("std_icode"))  {
					try {
						ZkBiCellValueMapper zcvm = (ZkBiCellValueMapper) bcc.getMapper();
						ZkJxPickInput zjpi = (ZkJxPickInput) zcvm.getComponent();
						pickItemCode(bcc,zjpi);
					} catch (Exception ex) {
						UniLog.log(ex);
					}	
				}
				break;
			default :
				break;
			}
		}	
	}
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		if(getGipi("AfsServiceItem") == null) {
			setGipi("AfsServiceItem",new ServiceItemGetItemProperty(br.getSubLink("AfsServiceItem")));	
		}
		super.bindCellCollection(br, mode);
		setScreen(!br.getCell("stmcm_name").getString().trim().equals(""));
	}
}
