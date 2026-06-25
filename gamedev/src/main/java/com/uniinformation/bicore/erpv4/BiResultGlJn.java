package com.uniinformation.bicore.erpv4;

import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultGlJn extends BiResultErpv4 {

	public BiResultGlJn(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
	}

	ReturnMsg check_detail_account(CellCollection col) {
		try {
			SelectUtil su = getSelectUtil();
			TableRec tr = su.getQueryResult("select * from ca where ca_cocode = '"+col.getCellString("jn_cocode")+"' and ca_ano = '"+col.getCellString("jn_ano")+"'");
			if(tr.getRecordCount() != 1) {
				RpcClient rpc = su.getRpcClient();
				Value v;
				v = rpc.callSegment("setCocodeBaseccy",
							new VectorUtil()
							.addElement(
//										Erpv4Config.getCoCode(getSessionHelper()) 
										col.getCellString("tr_cocode")
								)
							.addElement(
									Erpv4Config.getBaseCcy(getSessionHelper(),col.getCellString("tr_cocode"))
									)
							.toVector()
							);
				v = rpc.callSegment("glpost_check_detail_account", 
							new VectorUtil()
							.addElement(col.getCellString("jn_inputano"))
							.addElement(col.getCellString("jn_cid"))
							.toVector()
						);
				if(v.toInt() != 0) {
					ReturnMsg rtn = new ReturnMsg(false,"Error Create Detail Account " + col.getCellString("jn_inputano") + " " + col.getCellString("jn_cid"));
					rtn.setFatal(true);
					return(rtn);
				}
			}
		} catch (Exception ex) {
			UniLog.log(ex);
			ReturnMsg rtn = new ReturnMsg(false,ex.toString());
			rtn.setFatal(true);
			return(rtn);
		}
		return(ReturnMsg.defaultOk);
	}

	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeUpdateCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		return(check_detail_account(col) );
	}
	protected ReturnMsg biBeforeAddCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeUpdateCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		return(check_detail_account(col) );
	}
	@Override
	public String getPickColumnCondition(ColumnCell p_cc) {
		if(Erpv4Config.isMultiCompany(sh)) {
			String cocode = Erpv4Config.getDefaultCoCode(sh);
			return(" ud_account.ca_cocode = '" + cocode + "' ");
		}
		return(null);
	}	

	@Override
	protected void setLookupItemList(TableRec lookupTableTr,ColumnCell colCell) throws Exception {
		if(!colCell.getCellLabel().equals("ca_ano")) {
			super.setLookupItemList(lookupTableTr, colCell);
			return;
		}
		Vector <Object> lookupValues = new Vector<Object>();
		Hashtable<Object,String> ht = new Hashtable<Object,String>();
		for(int j = 0;j<lookupTableTr.getRecordCount();j++) {
			lookupTableTr.setRecPointer(j);
			Object oo = lookupTableTr.getField(colCell.getBiColumn().getField().getName());
			lookupValues.add(oo);
			String listString = 
					lookupTableTr.getFieldString("ca_ano") + " " +
					lookupTableTr.getFieldString("ca_aname");
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
//		colCell.setItemList(vv);
		colCell.setCCObj("lookup_uparent_tr", lookupTableTr);
		colCell.setCCObj("lookup_uparent_values", lookupValues);
	}
	
}
