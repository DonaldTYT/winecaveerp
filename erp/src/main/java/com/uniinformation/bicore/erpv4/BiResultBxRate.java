package com.uniinformation.bicore.erpv4;

import java.util.Date;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultBxRate extends BiResult {
	public BiResultBxRate(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
	}
	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		Date dd = col.getCell("bx_sdate").getDate();
		if(!(dd.after(DateUtil.getDate("1970/01/01")))) {
			return(new ReturnMsg(false,"Cannot Delete First Rate Record"));
		}
		try {
			removeBxRateChain(
						getCell("bx_basecid").getString(),
						getCell("bx_cid").getString(),
						getCell("bx_sdate").getDate(),
						getCell("bx_edate").getDate()
					);
			TableRec tr = getSelectUtil().getQueryResult("select * from bxrate where bx_basecid <> bx_cid and bx_basecid = ? and bx_sdate = ? ",
						new Wherecl()
							.appendArgument( getCell("bx_basecid").getString())
							.appendArgument( getCell("bx_sdate").getDate())
					);
			for(int i = 0;i<tr.getRecordCount();i++) {
				tr.setRecPointer(i);
				removeBxRateChain(
						tr.getFieldString("bx_basecid"),
						tr.getFieldString("bx_cid"),
						tr.getFieldDate("bx_sdate"),
						tr.getFieldDate("bx_edate")
					);
			}
			getSelectUtil().executeUpdate("delete from bxrate where bx_basecid <> bx_cid and bx_basecid = ? and bx_sdate = ? ",
						new Wherecl()
							.appendArgument( getCell("bx_basecid").getString())
							.appendArgument( getCell("bx_sdate").getDate())
					);
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Exception catched"));
		}
		return(new ReturnMsg(true));
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection col)
	{
		ReturnMsg rtn = super.biBeforeAddCurrent(col);
		if(rtn == null || rtn.getStatus()) {
			addBxRateChain(
						getCell("bx_basecid").getString(),
						getCell("bx_cid").getString(),
						getCell("bx_sdate").getDate(),
						getCell("bx_edate").getDate()
					);
			BiResult sr = getSubLink("erpv4.BxRateDet");
			if(sr != null) {
				Vector<BiCellCollection> v = sr.getRowCollectionList();
				for(BiCellCollection scol: v) {
					addBxRateChain(
						scol.getCell("bxd_basecid").getString(),
						scol.getCell("bxd_cid").getString(),
						scol.getCell("bxd_sdate").getDate(),
						scol.getCell("bxd_edate").getDate()
					);
				}
			}
		}
		return(rtn);
	}
	
	void removeBxRateChain(String p_basecid,String p_cid,Date p_sdate,Date p_edate) {
		try {
			SelectUtil su = getSelectUtil();
			TableRec tr = su.getQueryResult("select * from bxrate where " 
				+ " bx_basecid = ? and bx_cid = ? and bx_edate = ?",
				new Wherecl()
					.appendArgument(p_basecid)
					.appendArgument(p_cid)
					.appendArgument(DateUtil.prevday(p_sdate))
			);
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				su.executeUpdate("update bxrate set bx_edate = ? where serial_id = ?" ,
						new Wherecl()
							.appendArgument(p_edate)
							.appendArgument(tr.getFieldInt("serial_id"))
						);
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	void addBxRateChain(String p_basecid,String p_cid,Date p_sdate,Date p_edate) {
		try {
			SelectUtil su = getSelectUtil();
			TableRec tr = su.getQueryResult("select * from bxrate where " 
				+ " bx_basecid = ? and bx_cid = ? and bx_edate = ?",
				new Wherecl()
					.appendArgument(p_basecid)
					.appendArgument(p_cid)
					.appendArgument(p_edate)
			);
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				su.executeUpdate("update bxrate set bx_edate = ? where serial_id = ?" ,
						new Wherecl()
							.appendArgument(DateUtil.prevday(p_sdate))
							.appendArgument(tr.getFieldInt("serial_id"))
						);
			}
			
//			TableRec tr = su.getQueryResult("select * from bxrate where " 
//				+ " bx_basecid = ? and bx_cid = ? and bx_sdate <= ? and bx_edate >= ?",
//				new Wherecl()
//					.appendArgument(p_basecid)
//					.appendArgument(p_cid)
//					.appendArgument(p_sdate)
//					.appendArgument(p_sdate)
//			);
//			if(tr.getRecordCount() > 0) {
//				tr.setRecPointer(0);
//				su.executeUpdate("update bxrate set bx_edate = ? where serial_id = ?" ,
//						new Wherecl()
//							.appendArgument(DateUtil.prevday(p_sdate))
//							.appendArgument(tr.getFieldInt("serial_id"))
//						);
//			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
}
