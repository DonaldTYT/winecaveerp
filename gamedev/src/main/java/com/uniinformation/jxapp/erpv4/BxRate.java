package com.uniinformation.jxapp.erpv4;

import java.util.Date;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;

public class BxRate extends JxZkBiBase {
	@Override 
	public void afterBind() {
		super.afterBind();
		new JxFieldChange("bx_sdate") {

			@Override
			public boolean valueChanged(JxField jxfield, String orgvalue) {
				// TODO Auto-generated method stub
				UniLog.log("HAHA" + jxfield.getText() + " : " + orgvalue);
				Date dd = DateUtil.getDate(jxfield.getText());
				if(dd != null) {
					try {
						dd = setBxRateDet(getBr(),dd);
						getBr().getCell("bx_edate").set(dd);
						return(true);
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				}
				return false;
			}
			
		};
	}
	Date setBxRateDet(BiResult p_br,Date dd) throws Exception {
		TableRec tr;
					if(dd == null ||
							!(dd.after(DateUtil.getDate("1970/01/01"))) ||
							!(DateUtil.getDate("2049/12/31").after(dd))
							) {
						return(null);
					}
					tr = p_br.getSelectUtil().getQueryResult("select * from bxrate where bx_basecid = bx_cid and bx_sdate = '" + 
							DateUtil.toDateString(dd, "yyyy/mm/dd") + "'",null
					);
					if(tr.getRecordCount() > 0) return(null);
					tr = p_br.getSelectUtil().getQueryResult("select * from cur where cc_cid <> '" + 
							p_br.getCell("bx_basecid").getString()
							+ "'",null
							);
					BiResult sr = p_br.getSubLink("erpv4.BxRateDet");
					for(int i = 0;i<tr.getRecordCount();i++) {
						tr.setRecPointer(i);
						CellCollection col;
					
						if(sr.getRowCount() <= i) {
							JxField sv = jxAdd("list_"+sr.getView().getName().replace(".", "_"));
							col = sr.newRowCollection();
							ReturnMsg rtn = sr.addSubRecord(col, i,"");
							Object trs = rtn.getData();
							int rowIdx = getGipi(sr.getView().getName()).getIndexOf(trs);
							sv.addItemToList(trs, rowIdx);
						} else {
							col = sr.getRowCollectionV(i);
						}
						col.getCell("bxd_cid").set(tr.getFieldString("cc_cid"));
						TableRec tr2 = p_br.getSelectUtil().getQueryResult("select * from bxrate where bx_basecid = '" +
									p_br.getCell("bx_basecid").getString() + "' and bx_cid = '" +
									tr.getFieldString("cc_cid") + "' and bx_sdate <= '" +
									DateUtil.toDateString(dd, "yyyy/mm/dd") + "'" +
									" order by bx_sdate desc"
								);
						if(tr2.getRecordCount() > 0) {
							tr2.setRecPointer(0);
							col.getCell("bxd_xrate").set(tr2.getFieldDouble("bx_xrate"));
						}
					}
					tr = p_br.getSelectUtil().getQueryResult("select * from bxrate where bx_basecid = bx_cid and bx_sdate > '" + 
							DateUtil.toDateString(dd, "yyyy/mm/dd") + "' order by bx_sdate",null
					);
					if(tr.getRecordCount() > 0) {
						return(DateUtil.prevday(tr.getFieldDate("bx_sdate")));
					} else {
						return (DateUtil.getDate("2049/12/31"));
					}
	}
	@Override
	public void bindCellCollection(BiResult br,int mode) {
		super.bindCellCollection(br,mode);
		try {
			switch(mode) {
			case	JxZkBiBase.MODE_ADD:
			{
					TableRec tr;
					tr = br.getSelectUtil().getQueryResult("select * from baseccy");
					tr.setRecPointer(0);
					br.getCell("bx_basecid").set(tr.getFieldString("bbc_cid"));
					java.util.Date dd = DateUtil.today();
					java.util.Date ed = setBxRateDet(br,dd);
					if(ed != null) {
						br.getCell("bx_sdate").set(dd);
						br.getCell("bx_edate").set(ed);
					}
//					tr = br.getSelectUtil().getQueryResult("select * from bxrate where bx_basecid = bx_cid and bx_sdate = '" + 
//							DateUtil.toDateString(dd, "yyyy/mm/dd") + "'",null
//					);
//					if(tr.getRecordCount() <= 0) {
//					} else {
//						dd = DateUtil.getDate("1970/01/01");
//					}
//					br.getCell("bx_sdate").set(dd);
//					tr = br.getSelectUtil().getQueryResult("select * from cur where cc_cid <> '" + 
//							br.getCell("bx_basecid").getString()
//							+ "'",null
//							);
//					BiResult sr = br.getSubLink("erpv4.BxRateDet");
//					for(int i = 0;i<tr.getRecordCount();i++) {
//						tr.setRecPointer(i);
//						JxField sv = jxAdd("list_"+sr.getView().getName().replace(".", "_"));
//						CellCollection col = sr.newRowCollection();
//						col.getCell("bxd_cid").set(tr.getFieldString("cc_cid"));
//						TableRec tr2 = br.getSelectUtil().getQueryResult("select * from bxrate where bx_basecid = '" +
//									br.getCell("bx_basecid").getString() + "' and bx_cid = '" +
//									tr.getFieldString("cc_cid") + "' and bx_sdate <= '" +
//									DateUtil.toDateString(dd, "yyyy/mm/dd") + "'" +
//									" order by bx_sdate desc"
//								);
//						if(tr2.getRecordCount() > 0) {
//							tr2.setRecPointer(0);
//							col.getCell("bxd_xrate").set(tr2.getFieldDouble("bx_xrate"));
//						}
//						Object trs = sr.addSubRecord(col, i);
//						int rowIdx = getGipi(sr.getView().getName()).getIndexOf(trs);
//						sv.addItemToList(trs, rowIdx);
//					}
			}
					break;
			}
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
}
