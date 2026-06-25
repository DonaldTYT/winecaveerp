package com.uniinformation.jxapp.erpv4;

import java.util.Collections;
import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.jx.JxField;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;

public class MoTransferG2 extends MoPos {
	GipiNamedItemList allToLocs = null;
	GipiNamedItemList outToLocs = null;
	GipiNamedItemList inToLocs = null;
	Vector allToLocList = null;
	Vector outToLocList= null;
	Vector inToLocList = null;
	
	
	void getPendingTransfer() {
					UniLog.log("toLoc Changed1 " + getBr().getCellString("stm_toloc"));
					UniLog.log("toLoc Changed2 " + getBr().getCell("tloc_transit"));
					UniLog.log("toLoc Changed3 " + getBr().getCell("tloc_tfronly"));
						String transitLoc = null;
						String transitBin = null;
						if(getBr().getCellString("stm_fromloc").equals("HZ01")) transitLoc = "HZ02";
						if(getBr().getCellString("stm_fromloc").equals("CTL01")) transitLoc = "CTL03";
						if(getBr().getCellString("stm_fromloc").equals("TST01")) transitLoc = "TST06";
						if(getBr().getCellString("stm_fromloc").equals("DVR01")) transitLoc = "DVR02";
						if(getBr().getCellString("stm_fromloc").equals("DSC01")) transitLoc = "DSCT1";
						if(getBr().getCellString("stm_fromloc").equals("PPDVR0")) transitLoc = "PPDVRT";
						if(getBr().getCellString("stm_fromloc").equals("DSC02")) transitLoc = "DSCT2";
						if(getBr().getCellString("stm_fromloc").equals("PO001")) transitLoc = "PO005";
						if(getBr().getCellString("stm_fromloc").equals("NC001")) transitLoc = "NC005";
						if(getBr().getCellString("stm_fromloc").equals("PANA01")) transitLoc = "PANA05";
						if(transitLoc != null && !getBr().getCellBoolean("tloc_tfronly")) {
							transitBin = getBr().getCellString("stm_toloc");
							/*
							if(!getBr().getCellBoolean("tloc_tfronly")) {
								transitBin = getBr().getCellString("stm_toloc");
							} else {
								transitBin = "";
							}
							*/
					try {
					TableRec tr = getBr().getSelectUtil().getQueryResult(
							"select * from stockserial where stsn_org = ? and stsn_loc = ? and stsn_bin = ? and stsn_nqty <> 0",
								new Wherecl()
										.appendArgument(Erpv4Config.getCoWtAvOrg(getSessionHelper(), getBr().getCellString("stm_cocode")))
										.appendArgument(transitLoc)
										.appendArgument(transitBin)
								);
						BiResult sr = getBr().getSubLink(detViewId);
						JxField sv = null;
						sv = jxAdd("list_"+sr.getView().getName().replace(".", "_"));
						for(int i=0;i<tr.getRecordCount();i++) {
							tr.setRecPointer(i);
							BiCellCollection col = null;
							if(i >= sr.getRowCount()) {
								col = sr.newRowCollection();
								ReturnMsg rtn = sr.addSubRecord(col, -1 ,"");
								Object trobj = rtn.getData();
								sv.addItemToList(trobj, i);
							} else {
								Object o = sr.getTrStatObj(new Integer(i));
								sr.markDelete( o, false);
								col = sr.getRowCollectionV(i);
								sv.gridSetDataFormat(-1,i,"remove_deleted");
							}
							col.getCell("stmd_nref4").set(1);
							col.getCell("stmd_irg").set(tr.getFieldInt("stsn_irg"));
							col.getCell("stmd_org").set(tr.getFieldInt("stsn_org"));
							col.getCell("stmd_ref4").set(StringUtil.strpart(tr.getFieldString("stsn_ref4"),0,30));
							col.getCell("stmdki_ref4").set(tr.getFieldString("stsn_ref4"));
//							col.getCell("stmdki_bin").set(transitBin);
							col.getCell("stmd_entryqty").set(tr.getFieldDouble("stsn_nqty"));
						}
						
					} catch (Exception ex) {
						UniLog.log(ex);
					}
			}
	}
	@Override
	public void afterBind() {
		super.afterBind();
		int cc = 1;
		new JxFieldChange("tloc_desc")  {
			@Override
			public boolean valueChanged(JxField jxfield, String orgvalue) {
				// TODO Auto-generated method stub
				if(curMode == MODE_ADD && getBr().getCellInt("stm_nref4") == 1) {
					UniLog.log("toLoc Changed1 " + getBr().getCellString("stm_toloc"));
					UniLog.log("toLoc Changed2 " + getBr().getCell("tloc_transit"));
					UniLog.log("toLoc Changed3 " + getBr().getCell("tloc_tfronly"));
						String transitLoc = null;
						String transitBin = null;
						if(getBr().getCellString("stm_fromloc").equals("HZ01")) transitLoc = "HZ02";
						if(getBr().getCellString("stm_fromloc").equals("CTL01")) transitLoc = "CTL03";
						if(getBr().getCellString("stm_fromloc").equals("TST01")) transitLoc = "TST06";
						if(getBr().getCellString("stm_fromloc").equals("DVR01")) transitLoc = "DVR02";
						if(getBr().getCellString("stm_fromloc").equals("DSC01")) transitLoc = "DSCT1";
						if(getBr().getCellString("stm_fromloc").equals("PPDVR0")) transitLoc = "PPDVRT";
						if(getBr().getCellString("stm_fromloc").equals("DSC02")) transitLoc = "DSCT2";
						if(getBr().getCellString("stm_fromloc").equals("PO001")) transitLoc = "PO005";
						if(getBr().getCellString("stm_fromloc").equals("NC001")) transitLoc = "NC005";
						if(getBr().getCellString("stm_fromloc").equals("PANA01")) transitLoc = "PANA05";
						if(transitLoc != null && !getBr().getCellBoolean("tloc_tfronly")) {
							transitBin = getBr().getCellString("stm_toloc");
						} else {
							if(getSessionHelper().isAdminUser()) {
								transitLoc = getBr().getCellString("stm_toloc");
								transitBin = "";
							}
						}
							/*
							if(!getBr().getCellBoolean("tloc_tfronly")) {
								transitBin = getBr().getCellString("stm_toloc");
							} else {
								transitBin = "";
							}
							*/
						if(transitLoc != null && transitBin != null) {
					try {
					TableRec tr = getBr().getSelectUtil().getQueryResult(
							"select * from stockserial where stsn_org = ? and stsn_loc = ? and stsn_bin = ? and stsn_nqty <> 0",
								new Wherecl()
										.appendArgument(Erpv4Config.getCoWtAvOrg(getSessionHelper(), getBr().getCellString("stm_cocode")))
										.appendArgument(transitLoc)
										.appendArgument(transitBin)
								);
						BiResult sr = getBr().getSubLink(detViewId);
						JxField sv = null;
						sv = jxAdd("list_"+sr.getView().getName().replace(".", "_"));
						int i;
						for(i=0;i<tr.getRecordCount();i++) {
							tr.setRecPointer(i);
							BiCellCollection col = null;
							if(i >= sr.getRowCount()) {
								col = sr.newRowCollection();
								ReturnMsg rtn = sr.addSubRecord(col, -1 ,"");
								Object trobj = rtn.getData();
								sv.addItemToList(trobj, i);
							} else {
								Object o = sr.getTrStatObj(new Integer(i));
								sr.markDelete( o, false);
								col = sr.getRowCollectionV(i);
								sv.gridSetDataFormat(-1,i,"remove_deleted");
							}
							col.getCell("stmd_nref4").set(1);
							col.getCell("stmd_irg").set(tr.getFieldInt("stsn_irg"));
							col.getCell("stmd_org").set(tr.getFieldInt("stsn_org"));
							col.getCell("stmd_ref4").set(StringUtil.strpart(tr.getFieldString("stsn_ref4"),0,30));
							col.getCell("stmdki_ref4").set(tr.getFieldString("stsn_ref4"));
//							col.getCell("stmdki_bin").set(transitBin);
							col.getCell("stmd_entryqty").set(tr.getFieldDouble("stsn_nqty"));
						}
						for(;i<sr.getRowCount();i++) {
							Object o = sr.getTrStatObj(new Integer(i));
							sr.markDelete( o, true);
							sv.gridSetDataFormat(-1,i,"add_deleted");
						}
					} catch (Exception ex) {
						UniLog.log(ex);
					}
						}
				}
				return true;
			}
		};
		cc = 2;
		new JxFieldAction("stm_nref4_notused")  {
			@Override
			public void actionPerformed(JxField jxfield) {
				UniLog.log("mode changed");
				ColumnCell ctloc = getBr().getCell("tloc_desc");
				if(ctloc != null) {
					int dftlcrg = Erpv4Config.getDefaultLcrg(getSessionHelper());
					if(allToLocs == null && allToLocList == null) {
						try {
						TableRec lookupTableTr = (TableRec) ctloc.getCCObj("lookup_uparent_tr");
						Vector<Object> lookupValues = (Vector<Object>) ctloc.getCCObj("lookup_uparent_values");
						allToLocs = (GipiNamedItemList) ctloc.getItemPropertyInterface();
						if(allToLocs != null) {
							outToLocs = new GipiNamedItemList();
							inToLocs =  new GipiNamedItemList();
							for(int i=0;i<allToLocs.getRowCount();i++) {
								Object o = allToLocs.getRow(i);
								int idx = lookupValues.indexOf(o);
								lookupTableTr.setRecPointer(idx);
								if(true) {
									outToLocs.appendItem(o, allToLocs.getString(o));
								}
								if(true) {
									inToLocs.appendItem(o, allToLocs.getString(o));
								}
							}
						}

						allToLocList = ctloc.getItemList();
						if(allToLocList != null) {
							outToLocList = new Vector();
							inToLocList =  new Vector();
							for(int i=0;i<allToLocList.size();i++) {
								int idx = lookupValues.indexOf(allToLocList.get(i));
								lookupTableTr.setRecPointer(idx);
								String tfrOnly = lookupTableTr.getFieldString("loc_tfronly");
								String transitOnly = lookupTableTr.getFieldString("loc_transit");
								int lcrg = lookupTableTr.getFieldInt("loc_mrg");
								if(
									("Y".equals(transitOnly) && dftlcrg != lcrg) ||
									(!"Y".equals(transitOnly) && dftlcrg == lcrg)
									) {
									outToLocList.add(allToLocList.get(i));
								}
								if(
									("Y".equals(transitOnly) && dftlcrg == lcrg) ||
									(!"Y".equals(transitOnly) && dftlcrg == lcrg)
									) {
									inToLocList.add(allToLocList.get(i));
								}
							}
						}
						} catch (Exception ex) {
							UniLog.log(ex);
						}
					}
					int mode = ctloc.getCollection().getCellInt("stm_nref4");
					switch(mode) {
					case 0: if(outToLocs != null) ctloc.setItemPropertyInterface(outToLocs); else ctloc.setItemList(outToLocList);
							break;
					case 1: if(inToLocs != null) ctloc.setItemPropertyInterface(inToLocs); else ctloc.setItemList(inToLocList);
							break;
					default : if(allToLocs != null) ctloc.setItemPropertyInterface(allToLocs); else ctloc.setItemList(allToLocList);
							break;
					}
				}
			}
		};
	}
}
