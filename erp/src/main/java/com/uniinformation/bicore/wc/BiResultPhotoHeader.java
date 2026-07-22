package com.uniinformation.bicore.wc;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultPhotoHeader extends BiResultErpv4 {

	public BiResultPhotoHeader(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		// TODO Auto-generated constructor stub
	}

	
	void doSelectAndUpdateLocs(Hashtable<Integer,Integer> shash) throws Exception{
		int locPos = getSelectFieldPosition( getView().getColumnByLabel("pdph_locs"));
		String ss = null;
		for(int sid : shash.keySet()) {
			if(ss == null) ss = "("+sid; else ss += ","+sid;
		}
		ss += ")";
		TableRec tr = getSelectUtil().getQueryResult(
					"select distinct pdlbs_bin,pdphoto_hdr.serial_id sid from pdphoto_hdr,stock,podetlocbinstatus where st_mbrand = pdph_code and st_msize2 = pdph_vol and st_msize3 = pdph_year and pdlbs_irg = st_irg and pdlbs_stockqty > 0 " + 
						" and pdphoto_hdr.serial_id in " + ss,
						null
				);
		Hashtable<Integer,String> lhash = new Hashtable<Integer,String>();
		for(int i=0;i<tr.getRecordCount();i++) {
			tr.setRecPointer(i);
			int sid = tr.getFieldInt("sid");
			ss = lhash.get(sid);
			if(ss == null) {
				ss = ""+tr.getFieldString("pdlbs_bin");
			} else ss += ","+tr.getFieldString("pdlbs_bin");
			lhash.put(sid,ss);
		}
		for(int sid : lhash.keySet()) {
			ss = lhash.get(sid);
			int idx = shash.get(sid);
			saveOneObjectToResultTr(idx,locPos,ss);
		}
	}
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		int inCnt = 0;
		int maxIn = 1500;
		try {
			Hashtable <Integer,Integer>shash = new Hashtable<Integer,Integer>();
			for(int i=0;i<resultTr.getRecordCount();i++) {
				shash.put((Integer) resultTr.getField(0, i),i);
				inCnt++;
				if(inCnt >= maxIn) {
					doSelectAndUpdateLocs(shash);
					shash.clear();
					inCnt = 0;
				}	
			}
			if(inCnt >= maxIn) {
				doSelectAndUpdateLocs(shash);
				inCnt = 0;
			}	
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,ex.toString()));
		}
		return(ReturnMsg.defaultOk);
	}
}
