package com.uniinformation.bicore.afs;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ChineseConvert;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.erpv4.BiResultQuoDet;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsQuoDetG2 extends BiResultQuoDet {
	Vector<String> brandList = null;
	GipiNamedItemList allIcodeList = null;
	Hashtable<String,GipiNamedItemList> pickIcodeHash;
	
	private GipiNamedItemList getDefaultIcodeItemList(ColumnCell cc) {
		if(allIcodeList == null) {
				allIcodeList = (GipiNamedItemList) cc.getItemPropertyInterface();
		}
		return(allIcodeList);
	}

	CellValueAction filterIcodeByBrandAndModel = 
				new CellValueAction() {

					@Override
					public void cellAction_onchange(Cell p_value) throws CellException {
						// TODO Auto-generated method stub
						UniLog.log("HAHA field");
						ColumnCell icode = (ColumnCell) ((ColumnCell) p_value).getCollection().testCell("st_icode");
						ColumnCell pickBrand = (ColumnCell) ((ColumnCell) p_value).getCollection().testCell("stbd_pickname");
						ColumnCell pickModel = (ColumnCell) ((ColumnCell) p_value).getCollection().testCell("st_pickmodel");
						if(icode != null) {
								if(
										(pickBrand == null || pickBrand.getString().equals(""))
										&& (pickModel == null || pickModel.getString().equals(""))
										) {
									icode.setItemPropertyInterface(getDefaultIcodeItemList(icode));
								}
								try {
								TableRec lookupTableTr = (TableRec) icode.getCCObj("lookup_uparent_tr");
								Vector<Object> lookupValues = (Vector<Object>) icode.getCCObj("lookup_uparent_values");
								
								String bdname = "";
								String modelno = "";
								String bdcode = "";
								
								if(pickBrand != null) {
									bdname = pickBrand.getString();
									bdname = ChineseConvert.convertAuto2B(bdname);
									try {
									SelectUtil su = getSelectUtil();
									TableRec tr = su.getQueryResult("select stbd_code from st_brand where stbd_name = ?", new Wherecl().appendArgument(bdname));
									if(tr.getRecordCount() > 0) {
										tr.setRecPointer(0);
										bdcode = tr.getFieldString("stbd_code");
									}
									} catch (Exception ex) {
										UniLog.log(ex);
									}
								}

								if(pickModel != null){
									if(p_value == pickBrand){
										Vector<String> modelList = new Vector<String>();
										if(!StringUtils.isBlank(bdcode)){
											SelectUtil su = getSelectUtil();
											TableRec tr = su.getQueryResult("select distinct st_modelno from stock where st_mbrand = ? order by 1",new Wherecl().appendArgument(bdcode));
											for(int i=0;i<tr.getRecordCount();i++) {
												tr.setRecPointer(i);
												String lstr = tr.getFieldString("st_modelno");
												modelList.add(lstr);
							
											}
										}
										pickModel.setItemList(modelList);
									}
									modelno = pickModel.getString();
								}
								GipiNamedItemList pickIcodeList = null;
								GipiNamedItemList allIcodeList = getDefaultIcodeItemList(icode);
								String pickHashCode = bdcode+"_"+modelno;
								if(pickIcodeHash == null) {
									pickIcodeHash = new Hashtable<String,GipiNamedItemList>();
								}
								
								pickIcodeList = pickIcodeHash.get(pickHashCode);
								if(pickIcodeList == null) {
								pickIcodeList = new GipiNamedItemList();
								for(int i=0;i< allIcodeList.getRowCount() ;i++) {
									Object o = allIcodeList.getRow(i);
									int idx = lookupValues.indexOf(o);
									lookupTableTr.setRecPointer(idx);
									if(!StringUtils.isBlank(bdcode) && !bdcode.equals(lookupTableTr.getFieldString("st_mbrand"))) {
										continue;
									}
									if(!StringUtils.isBlank(modelno) && !modelno.equals(lookupTableTr.getFieldString("st_modelno"))) {
										continue;
									}
									pickIcodeList.appendItem(o, allIcodeList.getString(o));
								}
								pickIcodeHash.put(pickHashCode, pickIcodeList);
								}
								icode.setItemPropertyInterface(pickIcodeList);
//								allToLocs = (GipiNamedItemList) ctloc.getItemPropertyInterface();
//								if(allToLocs != null) {
//									outToLocs = new GipiNamedItemList();
//									inToLocs =  new GipiNamedItemList();
//									for(int i=0;i<allToLocs.getRowCount();i++) {
//										Object o = allToLocs.getRow(i);
//										int idx = lookupValues.indexOf(o);
//										lookupTableTr.setRecPointer(idx);
//										if(true) {
//											outToLocs.appendItem(o, allToLocs.getString(o));
//										}
//										if(true) {
//											inToLocs.appendItem(o, allToLocs.getString(o));
//										}
//									}
//								}
//
//								allToLocList = ctloc.getItemList();
//								if(allToLocList != null) {
//									outToLocList = new Vector();
//									inToLocList =  new Vector();
//									for(int i=0;i<allToLocList.size();i++) {
//										int idx = lookupValues.indexOf(allToLocList.get(i));
//										lookupTableTr.setRecPointer(idx);
//										String tfrOnly = lookupTableTr.getFieldString("loc_tfronly");
//										String transitOnly = lookupTableTr.getFieldString("loc_transit");
//										int lcrg = lookupTableTr.getFieldInt("loc_mrg");
//										if(
//											("Y".equals(transitOnly) && dftlcrg != lcrg) ||
//											(!"Y".equals(transitOnly) && dftlcrg == lcrg)
//											) {
//											outToLocList.add(allToLocList.get(i));
//										}
//										if(
//											("Y".equals(transitOnly) && dftlcrg == lcrg) ||
//											(!"Y".equals(transitOnly) && dftlcrg == lcrg)
//											) {
//											inToLocList.add(allToLocList.get(i));
//										}
//									}
//								}
								} catch (Exception ex) {
									UniLog.log(ex);
								}
//							int mode = ctloc.getCollection().getCellInt("stm_nref4");
//							switch(mode) {
//							case 0: if(outToLocs != null) ctloc.setItemPropertyInterface(outToLocs); else ctloc.setItemList(outToLocList);
//									break;
//							case 1: if(inToLocs != null) ctloc.setItemPropertyInterface(inToLocs); else ctloc.setItemList(inToLocList);
//									break;
//							default : if(allToLocs != null) ctloc.setItemPropertyInterface(allToLocs); else ctloc.setItemList(allToLocList);
//									break;
//							}
						}	
					}

					@Override
					public void cellAction_onfree() throws CellException {
						// TODO Auto-generated method stub
						
					}
					
				};
	public BiResultAfsQuoDetG2(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void createColumnCells(BiCellCollection p_col)
	{
		super.createColumnCells(p_col);
		/*if(brandList == null) {
			brandList = new Vector<String>();
			SelectUtil su = getSelectUtil();
			try {
				TableRec tr = su.getQueryResult("select stbd_name from st_brand order by 1");
				for(int i=0;i<tr.getRecordCount();i++) {
					tr.setRecPointer(i);
					String lstr = tr.getFieldString("stbd_name");
					if(getSessionHelper().getLHLang().equals("SCHN")) {
						lstr = ChineseConvert.convertAuto2Gnew(lstr);
					}
					brandList.add(lstr);
					
				}
			} catch (Exception ex) {
				UniLog.log(ex);
			}
		}
		p_col.getCell("stbd_pickname").setItemList(brandList);*/
		p_col.getCell("stbd_pickname").addAction( 
				new CellValueAction() {
					@Override
					public void cellAction_onchange(Cell p_value) throws CellException {
						UniLog.log1("stbd_pickname cellAction_onchange %s, actionEnabled:%b", p_value.getObject(), isActionEnabled());
						// TODO Auto-generated method stub
						/*
						if(
								((ColumnCell) p_value).getCollection().getCell("stbd_name").equals(
								((ColumnCell) p_value).getCollection().getCell("stbd_pickname"))) {
									
						} else {
							((ColumnCell) p_value).getCollection().getCell("st_pickmodel").set("");
						}
						*/

						/*if(actionEnabled) {
							((ColumnCell) p_value).getCollection().getCell("st_pickmodel").set("");
						}*/
					}

					@Override
					public void cellAction_onfree() throws CellException {
						// TODO Auto-generated method stub
						
					}
					
				}
		);
		p_col.getCell("stbd_name").addAction(
				new CellValueAction() {

					@Override
					public void cellAction_onchange(Cell p_value) throws CellException {
						// TODO Auto-generated method stub
						UniLog.log1("stbd_name cellAction_onchange %s", p_value.getObject());
						((ColumnCell) p_value).getCollection().getCell("stbd_pickname").set(p_value.getObject());
					}

					@Override
					public void cellAction_onfree() throws CellException {
						// TODO Auto-generated method stub
						
					}
					
				}
		);
		p_col.getCell("st_modelno").addAction(
				new CellValueAction() {

					@Override
					public void cellAction_onchange(Cell p_value) throws CellException {
						// TODO Auto-generated method stub
						UniLog.log1("st_modelno cellAction_onchange %s", p_value.getObject());
						((ColumnCell) p_value).getCollection().getCell("st_pickmodel").set(p_value.getObject());
					}

					@Override
					public void cellAction_onfree() throws CellException {
						// TODO Auto-generated method stub
						
					}
					
				}
		);
		//p_col.getCell("stbd_pickname").addAction( filterIcodeByBrandAndModel);
		//p_col.getCell("st_pickmodel").addAction( filterIcodeByBrandAndModel);
	}
}
