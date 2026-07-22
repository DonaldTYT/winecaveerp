package com.uniinformation.bicore.erpv4;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.poi.ExcelPoi;
import com.uniinformation.webcore.SessionHelper;

public class BiResultMO extends BiResultStmov {
	
	public BiResultMO (BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
//		stmdLinkName = "AfsMoDet";
//		stmdLinkName = "erpv4.MoDet";
		if(getCell("stm_tolAmt") != null && stmdLinkName != null) {
			tolAmtCell = "stm_tolAmt";
			BiResult sr = getSubLink(stmdLinkName);
			if(sr.getColumnByLabel("stmd_exprice0") != null) {
				detAmtCell = "stmd_exprice0";
			} else {
				if(sr.getColumnByLabel("stmd_exprice") != null) {
					detAmtCell = "stmd_exprice";
				}
			}
		}
	}

	
	
//	@Override
//	protected ReturnMsg biBeforeAddCurrent(CellCollection pcol) {
//		try {
//			pcol.getCell("stm_ref1").set(newMoCode(su,pcol.getCell("stm_date").getDate()));
//		} catch (CellException cex) {
//			UniLog.log(cex);
//			return(new ReturnMsg(false,cex.toString()));
//		}
//		return(super.biBeforeAddCurrent(pcol));
//	}
	@Override
	protected ReturnMsg validateOneRow(CellCollection pcol,boolean isUpdate) {
		if(pcol.getCell("stm_ref1").isBlank()) {
		try {
			pcol.getCell("stm_ref1").set(newMoCode(this,pcol.getCell("stm_date").getDate()));
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,cex.toString()));
		}
		}
		return(super.validateOneRow(pcol,isUpdate));
	}
	
	static public String newMoCode(BiResult p_br,java.util.Date p_date) {
		try {
			String s = null;
			java.util.Date d = p_date;
			String rgc = Erpv4Config.getString(p_br.getSessionHelper(), "rgcontrol_MO" );
			if(rgc != null && !rgc.trim().equals("")) {
				String useModule = Erpv4Config.getString(p_br.getSessionHelper(), "rgcontrol_MOByModule" );
				String cocode;
				if(useModule != null && useModule.equals("Y")) {
					if(Erpv4Config.isMultiStockLoc(p_br.getSessionHelper())) {
						rgc = Erpv4Config.getDefaultLcGroup(p_br.getSessionHelper()) + rgc;
					}
					rgc+=p_br.getCellString("stm_module").trim();
				}
				if(p_br.getCell("stm_cocode") != null) {
					cocode = p_br.getCellString("stm_cocode");
				} else {
					cocode = Erpv4Config.getDefaultCoCode(p_br.getSessionHelper());
				}
				return(BiResultErpv4.getCodeByRgControl(p_br,cocode,rgc,d));
			}
			String ds = DateUtil.toDateString(d, "yymmdd");
			int nextidx = 1;
			TableRec tr = p_br.getSelectUtil().getQueryResult("select stm_ref1 from stmov where stm_ref1 matches '" + "AFSM" + ds + "*' order by stm_ref1 desc",null);
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				s = tr.getField("stm_ref1").toString();
				String ss = StringUtil.strpart(s, 11, -1);
				nextidx = Integer.parseInt(ss) + 1;
			}
			s = String.format("AFSM%s-%03d",ds, nextidx);
			return(s);
		} catch (Exception cex ) {
			UniLog.log(cex);
			return(null);
		}
	}	
	/*
	@Override
	protected void setLookupItemList(TableRec lookupTableTr,ColumnCell colCell) throws Exception {
		if(colCell.getCellLabel().equals("vd_vcode")) {
			Vector <Object> lookupValues = new Vector<Object>();
			Hashtable<Object,String> ht = new Hashtable<Object,String>();
			for(int j = 0;j<lookupTableTr.getRecordCount();j++) {
				lookupTableTr.setRecPointer(j);
				Object oo = lookupTableTr.getField(colCell.getBiColumn().getField().getName());
				lookupValues.add(oo);
				String listString = 
						lookupTableTr.getFieldString("vd_vcode") + " " +
						lookupTableTr.getFieldString("vd_vname");
				ht.put(oo,listString);
			}
			Vector<Comparable> vv = new Vector<Comparable>();
			for(Object o : lookupValues) {
				vv.add((Comparable) o);
			}
			Collections.sort(vv);
			GipiNamedItemList prdList;
			prdList = new GipiNamedItemList();
			for(int i=0;i<vv.size();i++) {
				prdList.appendItem( vv.get(i), ht.get(vv.get(i)));
			}
			colCell.setItemPropertyInterface(prdList);
			colCell.setCCObj("lookup_uparent_tr", lookupTableTr);
			colCell.setCCObj("lookup_uparent_values", lookupValues);
			return;
		}
		super.setLookupItemList(lookupTableTr, colCell);
	}
	*/
}
