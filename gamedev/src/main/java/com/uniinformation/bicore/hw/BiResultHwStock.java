package com.uniinformation.bicore.hw;

import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultHwStock extends BiResult {
	public BiResultHwStock(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr,p_sh);
		UniLog.log("BiResultHwStock Used");
	}
	
	void updateIcode(CellCollection col)  throws CellException {
				col.getCell("st_icode").set(
						makeUniqueIcode(
						col.getCell("st_mtype").getString()
						+ col.getCell("mt_tpscode").getString()
						+ "-"
						+ col.getCell("st_modelno").getString()+ col.getCell("st_oicode").getString(),
						false,
						30,
						col.getCell("st_irg").getInt()
						)
				);
	}
	
	String makeUniqueIcode(String p_icodex,boolean p_autoidx,int p_maxlen,int p_skipirg) throws CellException {
		try {
			TableRec tr;
			String s;
			String p_icode;
			if(p_icodex == null ) throw new CellException("Generate Icode Error : code is null");
			p_icode = p_icodex.replaceAll("\\s+","");
//			if(p_icode.contains(" "))throw new CellException("Generate Icode Error : '" + p_icode + "' has space");
			if(p_icode.contains("("))throw new CellException("Generate Icode Error : '" + p_icode + "' has bracket");
			if(p_icode.contains(")"))throw new CellException("Generate Icode Error : '" + p_icode + "' has ");
			for(int i = 0;i<1000;i++) {
				if(i == 0) s = p_icode; else   {
					if(! p_autoidx) throw new CellException("Generate Icode Error : '" + p_icode + "' already exist");
						else s = p_icode+"/"+i; 
				}
				if(s.length() > p_maxlen) throw new CellException("Generate Icode Error : '" + p_icode + "' too long");
				tr = su.getQueryResult("select st_irg from stock where st_icode = '" + s + "' and st_irg <> " + p_skipirg,null);
				if(tr.getRecordCount() == 0) {
					UniLog.log("HAHA 2018 make icode " + s);
					return(s);
				}
			}
			throw new CellException("Cannot Generate Stock Code Too Many Duplicate");
		} catch (Exception ex){
			UniLog.log(ex);
			throw new CellException(ex.toString());
		}
	}
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection col) {
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			updateIcode(col);
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,-1,cex.getMessage()));
		}
		return(rtnMsg);
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col)
	{
		ReturnMsg rtnMsg = super.biBeforeAddCurrent(col);
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		try {
			updateIcode(col);
			col.getCell("st_cuser").set(su.getLoginId());
			col.getCell("st_uuser").set(su.getLoginId());
			col.getCell("st_cdate").set(new java.util.Date());
			col.getCell("st_udate").set(new java.util.Date());
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,-1,cex.getMessage()));
		}
		return(rtnMsg);
	}
	
}
