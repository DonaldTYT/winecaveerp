package com.uniinformation.bicore.erpv4;

import java.io.OutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Strval;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultArApJournal extends BiResultLedger {

	String ArApAccount  = null;
	public BiResultArApJournal(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		setCumulatorColumn("jn_xdate");
		RpcClient rpc = p_sh.getRpcClient();
		String cocode = Erpv4Config.getDefaultCoCode(p_sh);
		rpc.callSegment("setCocodeBaseccy",
				new VectorUtil()
				.addElement(cocode)
				.addElement(Erpv4Config.getBaseCcy(getSessionHelper(),cocode))
				.toVector()
				);
		Value val =
				rpc.callSegment("erpv4_getArAccountCode",
				new VectorUtil()
				.toVector()
				);
		if(val != null && (val instanceof Strval)) {
			ArApAccount = val.toString();
		}
//		activeAccountCondition = " (jn_openbal <> 0 or jn_amount <> 0) ";
//		getOpeningBalanceViewId = "erpv4.GlJnG2AsAt";
		
		// TODO Auto-generated constructor stub
	}
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
		/*
		String arAno = Erpv4Config.getString(sh, "ARACNO");
		if(StringUtils.isBlank(arAno)) {
			arAno = "13200";
		}
		*/
				
		p_where.andUniop("jn.jn_inputano", "=", ArApAccount).stripAnd();
		/*
		if(rptCol.testCell("showInvoiceOnly") != null && rptCol.getCell("showInvoiceOnly").getBoolean()) {
			p_where.andUniop("tr.tr_jcode", "=", "AR").stripAnd();
		}
		*/
		return(ht);
	}

	@Override
	void beforeQuery() {
		super.beforeQuery();
		if(rptCol.testCell("showInvoiceOnly") != null && rptCol.getCell("showInvoiceOnly").getBoolean()) {
			addCustomCondition(" tr_jcode = 'AR' ");
		}
	 }
	
	@Override
	public String getReportTitle() {
		// TODO Auto-generated method stub
		return "Recivable Ledger Report";
	}
	@Override
	protected double getUnitCost(Vector args) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	protected double getInQty(Vector args) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	protected double getOutQty(Vector args) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	protected void setOpeningBalance(LedgerCostCalculator ca) throws Exception {
		// TODO Auto-generated method stub
//		setOpeningBalance(LedgerCostCalculator ca) throws Exception {
		double amt = bbr.getCellDouble("jn_amount");
		double lamt = bbr.getCellDouble("jn_lamount");
		ca.updateBalanceWithCost(-1, amt,0, 1, lamt);
	}
	@Override
	protected void setRunningBalance(int idx, LedgerCostCalculator ca) throws Exception {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		double amt = getCellDouble("jn_amount");
		double lamt = getCellDouble("jn_lamount");
		ca.updateBalanceWithCost(idx, amt,0, 1, lamt);
	}
	@Override
	protected BiColumn getCumulatorKey() {
		// TODO Auto-generated method stub
		
		return(getColumnByLabel("jn_accountkey"));
	}
	@Override
	public ColumnCell getValue(ledgerColumns lgf) {
		// TODO Auto-generated method stub
		return null;
	}		
	
	public ReturnMsg printStatement(OutputStream os,JSONObject option) {
		try {
    		String paperType = Erpv4Config.getString(getSessionHelper(),"StmtPaperType");
//    		String dnDoc = Erpv4Config.getString(getSessionHelper(),"dnDocCode");
    		String dnDoc = Erpv4Config.getString(getSessionHelper(),"arstmtDocCode");
    		if(dnDoc == null) dnDoc = "GENINV03";
    		PrtdocJson ppj = PrtdocJson.newPrtdocJson(	
    				Erpv4Config.getDefaultCoCode(sh),
    				paperType,
    			    dnDoc,
    			    "erpv4_printDocument"
    				) ;
    		
    		String currentVdKey = null;
    		int docIdx = 0;
    		double cfAmount = 0;
    		String cfString = null;
    		for(int i=0;i<getRowCount();i++) {
    			loadOneRecV(i);
    			String thisKey = getCellString("jn_accountkey");
    			if(!thisKey.equals(currentVdKey)) {
    				if(currentVdKey != null) {
    					ppj.addBottomField("val_remark",cfString,550,250);
    				}
    				currentVdKey = thisKey;
    				if(docIdx > 0) {
    					ppj.newContent();
    				}
    				docIdx++;
    				cfAmount = 0;
    				cfString = "";

    				ppj.setTrailerAtLastPageOnly(true);
    				ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
//    				ppj.addHeaderField("doctitle","Customer Statement");
    				String preprintLayout = "logo/ckh_statement_2400.jpg";
    				ppj.addHeaderImage("logo", preprintLayout ,0,0,0,800);
    				ppj.addHeaderField("ciname",getCellString("vd_vname"),80,150);
    				ppj.addHeaderField("ciname", DateUtil.toDateString(closeBalDate, "dd/mm/yyyy"), 500, 200);
    				ppj.addDetailRecord();
    				ppj.addDetailRecordField("amount", "B/F",-200,0);
    				ppj.addDetailRecordField("amount", getCell("jn_openbal").getColumnDisplayString(),-100,0);
    			}
   				ppj.addDetailRecord();
//    		    ppj.addDetailRecordField("amount", DateUtil.toDateString(getCell("jn_xdate").getDate(), "dd/mm/yyyy"), -580,0);
    		    ppj.addDetailRecordField("itemcode", DateUtil.toDateString(getCell("jn_xdate").getDate(), "dd/mm/yyyy"), -80,0);
    		    if(getCellString("tr_jcode").equals("AR")) {
    		    	ppj.addDetailRecordField("itemcode", getCellString("jn_desc1"),20,0);
    		    } else {
    		    	ppj.addDetailRecordField("itemcode", "Payment Thank You",20,0);
    		    }
   				ppj.addDetailRecordField("amount", getCell("jn_amount").getColumnDisplayString(),-200,0);
   				ppj.addDetailRecordField("amount", getCell("jn_balance").getColumnDisplayString(),-100,0);
    			cfAmount = getCellDouble("jn_closebal");
    			cfString = getCell("jn_closebal").getColumnDisplayString();
    		}
    		
    		
    		return(ppj.toPdfStream(os, getSessionHelper()));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Fail Reason Unknown"));
		}		
	}
	public ReturnMsg printStatement_all(OutputStream os,JSONObject option) {
		try {
    		String paperType = Erpv4Config.getString(getSessionHelper(),"StmtPaperType");
//    		String dnDoc = Erpv4Config.getString(getSessionHelper(),"dnDocCode");
    		String dnDoc = Erpv4Config.getString(getSessionHelper(),"arstmtDocCode");
    		if(dnDoc == null) dnDoc = "GENINV03";
    		PrtdocJson ppj = PrtdocJson.newPrtdocJson(	
    				Erpv4Config.getDefaultCoCode(sh),
    				paperType,
    			    dnDoc,
    			    "erpv4_printDocument"
    				) ;
    		
    		String currentVdKey = null;
    		int docIdx = 0;
    		double cfAmount = 0;
    		String cfString = null;
    		for(int i=0;i<getRowCount();i++) {
    			loadOneRecV(i);
    			String thisKey = getCellString("jn_accountkey");
    			if(!thisKey.equals(currentVdKey)) {
    				if(currentVdKey != null) {
    					ppj.addBottomField("val_remark",cfString,550,250);
    				}
    				currentVdKey = thisKey;
    				if(docIdx > 0) {
    					ppj.newContent();
    				}
    				docIdx++;
    				cfAmount = 0;
    				cfString = "";

    				ppj.setTrailerAtLastPageOnly(true);
    				ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
//    				ppj.addHeaderField("doctitle","Customer Statement");
//    				String preprintLayout = "logo/ckh_statement_2400.jpg";
//   				ppj.addHeaderImage("logo", preprintLayout ,0,0,0,800);
    				ppj.addHeaderField("ciname",getCellString("vd_vname"),80,150);
    				ppj.addHeaderField("ciname", DateUtil.toDateString(closeBalDate, "dd/mm/yyyy"), 500, 200);
    				ppj.addDetailRecord();
    				ppj.addDetailRecordField("amount", "B/F",-200,0);
    				ppj.addDetailRecordField("amount", getCell("jn_openbal").getColumnDisplayString(),-100,0);
    			}
   				ppj.addDetailRecord();
//    		    ppj.addDetailRecordField("amount", DateUtil.toDateString(getCell("jn_xdate").getDate(), "dd/mm/yyyy"), -580,0);
    		    ppj.addDetailRecordField("itemcode", DateUtil.toDateString(getCell("jn_xdate").getDate(), "dd/mm/yyyy"), -80,0);
    		    if(getCellString("tr_jcode").equals("AR")) {
    		    	ppj.addDetailRecordField("itemcode", getCellString("jn_desc1"),20,0);
    		    } else {
    		    	ppj.addDetailRecordField("itemcode", "Payment Thank You",20,0);
    		    }
   				ppj.addDetailRecordField("amount", getCell("jn_amount").getColumnDisplayString(),-200,0);
   				ppj.addDetailRecordField("amount", getCell("jn_balance").getColumnDisplayString(),-100,0);
    			cfAmount = getCellDouble("jn_closebal");
    			cfString = getCell("jn_closebal").getColumnDisplayString();
    		}
    		
    		
//    		ppj.setTrailerAtLastPageOnly(true);
//    		ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
//    		ppj.addHeaderField("doctitle","Customer Statement");
//    		String preprintLayout = "logo/ckh_statement_2400.jpg";
//    		ppj.addHeaderImage("logo", preprintLayout ,0,0,0,800);
    		
    		/*
    		ppj.addHeaderField("ciname","Company Name:");
    		ppj.addHeaderField("ciname","Address:",0,20);
    		ppj.addHeaderField("ciname","Contact Person:",0,40);
    		ppj.addHeaderField("ciname","Email:",0,60);
    		ppj.addHeaderField("ciname","Tel:",0,80);
    		ppj.addHeaderField("ciname","Mob:",0,100);
    		*/
    		
//    		ppj.addHeaderField("cicontent",getCellString("vd_vname"));
//   			String addr = getCellString("vd_addr0").trim()+getCellString("vd_addr1").trim() + " " +  getCellString("vd_addr2").trim() + " " + getCellString("vd_addr3").trim();
//    		ppj.addHeaderField("cicontent",addr,0,20);
//    		ppj.addHeaderField("cicontent",getCellString("inv_contact"),0,40);
//    		ppj.addHeaderField("cicontent",getCellString("inv_tel"),0,80);
//    		ppj.addHeaderField("cicontent",getCellString("inv_fax"),0,100);
//
//    		
//    		ppj.addHeaderField("dflabel","Refs:",0,0);
//    		ppj.addHeaderField("dfvalue",getCellString("inv_quonum"),0,0);
//    		ppj.addHeaderField("dflabel","Date:",0,30);
//    		String ds = DateUtil.toDateString( getCell("inv_date").getDate(),"yyyy/mm/dd");
//    		ppj.addHeaderField("dfvalue", " "+ds ,-5,30);
//    		ppj.addHeaderField("dflabel","Page",0,60);
// 
//    		ppj.addDetailHeaderField("hdr_seq","Item");
//    		ppj.addDetailHeaderField("hdr_orderno","Brand");
//    		ppj.addDetailHeaderField("hdr_itemcode","Model");
//    		ppj.addDetailHeaderField("hdr_description","Description");
//    		ppj.addDetailHeaderField("hdr_qty","Qty");
//    		ppj.addDetailHeaderField("hdr_serialno","Amount("+getCellString("inv_cid")+")");

    		/*
    		ppj.addDetailHeaderField("hdr_seq","Item",0,20);
    		ppj.addDetailHeaderField("hdr_orderno","Contract No.",0,20);
    		ppj.addDetailHeaderField("hdr_itemcode","Model",0,20);
    		ppj.addDetailHeaderField("hdr_description","Description",0,20);
    		ppj.addDetailHeaderField("hdr_qty","QTY",0,20);
    		ppj.addDetailHeaderField("hdr_serialno","Serial No.",0,20);
    		*/

//    		Vector<BiCellCollection> v = null;
////    		v = getSubLink(subLinkId).getRowCollectionList();
//    		String indPrefix = "ind_";
//
//    		int n = 0;
//    		for(BiCellCollection c : v) {
//    			ppj.addDetailRecord();
//    			String s = "";
//    			boolean isSubitem = false;
//    			if(c.testCell(indPrefix+"subitem") != null) {
//    				if(!c.testCell(indPrefix+"subitem").getBoolean()) {
//    					if(c.testCell("stmcm_name") != null)  {
//    						ppj.setBold(true);
////    						ppj.setUnderLine(true);
//    						s += c.getCellString("stmcm_name");
//    						ppj.addDetailRecordField("amount", c.getCell(indPrefix+"setamount").getString());
//    					}
//    				} else {
//    					isSubitem = true;
//    				}
//    			}
//    			if(!isSubitem) {
//    					n++;
//    					ppj.addDetailRecordField("seq", ""+n);
//    			}
//    			if(c.testCell(indPrefix+"irg") != null) {
//    				if(c.testCell(indPrefix+"irg").getInt() > 0) {
//    					if(c.testCell("st_iname") != null)  {
//    						s += c.getCellString("st_iname");
//    					}
//    					if(Erpv4Config.getString(getSessionHelper(), "CustomSmartac") != null) {
//							s += " ";
//							s += "["+c.getCellString("st_icode")+"]";
//    					}
//    				}
//    			}
//    			if(c.testCell(indPrefix+"desp") != null) {
//   					s += c.getCellString(indPrefix+"desc");
//    			}
//    			ppj.addDetailRecordField("description", s);
////    			ppj.addDetailRecordField("serialno",c.getCellString(indPrefix+"ref4"));
////    			ppj.addDetailRecordField("orderno",c.getCellString("inv_invno"));
//    			if(!isSubitem) {
//    				ppj.addDetailRecordField("amount",c.getCellString(indPrefix+"amount"));
//    			}
//    			ppj.addDetailRecordField("brand", c.getCellString("stbd_name"));
//    			String oicode = c.getCellString("st_oicode");
//    			String modelno = c.getCellString("st_modelno");
//    			if(oicode != null && !oicode.equals("")) {
//    				ppj.addDetailRecordField("itemcode", oicode);
//    			} else {
//    				if(modelno != null && !modelno.equals("")) {
//    					ppj.addDetailRecordField("itemcode", modelno);
//    				}
//    			}
//    			ppj.setBold(false);
////    			ppj.setUnderLine(false);
//    			if(c.getCell(indPrefix+"qty").getDouble() != 0) {
//    			ppj.addDetailRecordField("quantity", c.getCell(indPrefix+"qty").getString());
//    			}
//    		}
//    		int ofs = 0;
//   			String remark = null; 
//   			remark = getCellString("inv_term");
//			if(remark == null) remark = getCell("inv_quodeli").getString(); else remark += "\r" + getCell("inv_quodeli").getString();
//			if(!getCellString("inv_remark").equals("")) {
//				remark += "\r"+ getCellString("inv_remark");
//			}
//			if(!getCellString("invh_term").equals("")) {
//				remark += "\r"+ getCellString("inv_remark");
//			}
//   			ppj.addBottomField("val_remark",remark,0,0,15,0);
    		return(ppj.toPdfStream(os, getSessionHelper()));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Fail Reason Unknown"));
		}		
	}
	@Override
	public void setLedgerDate(Date p_openBalDate, Date p_closeBalDate) {
		// TODO Auto-generated method stub
		
	}
}
