package com.uniinformation.bicore.afs;

import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.afs.BiResultAfsStockSet.BiCellAction_mt_tpname;
import com.uniinformation.bicore.erpv4.BiResultStock;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.cell.CellValueAction;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsStock extends BiResultStock {
	public BiResultAfsStock(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList,p_whereStr,p_sh);
		UniLog.log("BiResultAfsStock Used");
	}
	
	@Override
	protected void updateIcode(CellCollection col)  throws CellException {
			if(col.getCell("st_mtype").equals("M")) {
				col.getCell("st_icode").set(
						makeUniqueIcode(
						col.getCell("st_mtype").getString()
						+ col.getCell("mt_tpscode").getString()
						+ "-"
						+ col.getCell("st_mbrand").getString()
						+ "-"
						+ col.getCell("st_modelno").getString(),
						false,
						0,0,
						30,
						col.getCell("st_irg").getInt(),
						""
						)
				);
			} else 
			if(col.getCell("st_mtype").equals("O")) {
				col.getCell("st_icode").set(
						makeUniqueIcode(
						col.getCell("st_mtype").getString()
						+ col.getCell("mt_tpscode").getString()
						+ "-"
						+ col.getCell("st_mbrand").getString()
						+ "-"
						+ col.getCell("st_modelno").getString()+ col.getCell("st_oicode").getString(),
						false,
						0,0,
						30,
						col.getCell("st_irg").getInt(),
						""
						)
				);
			} else 
				if(col.getCell("st_mtype").equals("P")) {
				col.getCell("st_icode").set(
						makeUniqueIcode(
						col.getCell("st_mtype").getString()
						+ col.getCell("mt_tpscode").getString()
						+ "-"
						+ col.getCell("st_mbrand").getString()
						+ "-"
						+ col.getCell("st_oicode").getString()
						,
						false,
						0,0,
						30,
						col.getCell("st_irg").getInt(),
						""
						)
				);
			}
	}
	
	@Override
	protected String makeUniqueIcode(String p_icodex,boolean p_autoidx,int p_stidx,int p_maxidx,int p_maxlen,int p_skipirg,String p_seperator) throws CellException {
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
	
}
