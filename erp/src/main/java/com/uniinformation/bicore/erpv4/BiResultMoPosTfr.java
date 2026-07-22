package com.uniinformation.bicore.erpv4;

import java.io.ByteArrayOutputStream;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.prtdoc.PrtdocPerfJson;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;


public class BiResultMoPosTfr extends BiResultMO {

	public BiResultMoPosTfr(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		// TODO Auto-generated constructor stub
//		stmdLinkName = "erpv4.MoDetPosTfr";
		for(BiResult sr : getSubLinks()) {
			if(sr.getView().getTable().getName().equals("stmovd_ko")) {
				stmdLinkName = sr.getView().getName();
				break;
			}
		}		
		
		extraStmds.add("stmdki");
	}

	@Override
	protected TableRec getLookupTabTr(BiTable p_lookupTable, Wherecl p_wherecl,BiCellCollection p_col) throws Exception{	
		Wherecl wcl = p_wherecl;
		if(p_lookupTable.getName().equals("toloc")) {
			if(wcl == null ) wcl = new Wherecl();
			String fromLoc = Erpv4Config.getString(sh,"DefaultLoc");
			if(fromLoc != null && !fromLoc.equals("")) {
				wcl.appendString(" and toloc.loc_code <> '"+fromLoc+"' ").stripAnd();
			}
		}
//		if(p_lookupTable.getName().equals("stmcmodel")) {
//		if(wcl == null ) wcl = new Wherecl();
//		wcl.appendString(" and stmcm_rg in(select mcfm_modelrg from mcfitmodel where mcfm_mrg = "+ getCell("svmc_irg").getInt() + ") ").stripAnd();
//		}
		return(super.getLookupTabTr(p_lookupTable, wcl,p_col));
	}
	
	/*
	@Override
	protected Wherecl beforeLookupTableTr(BiTable p_lookupTable, Wherecl p_wherecl,BiCellCollection p_col)  {
		if(p_lookupTable.getName().equals("toloc")) {
		if(p_wherecl == null ) 
			p_wherecl = new Wherecl().andUniop("toloc.loc_code", "=" , "HQ01");
		else 
			p_wherecl.orWherecl(new Wherecl().andUniop("toloc.loc_code", "=" , "HQ01"));
		}
		return(p_wherecl);
	}
	*/

	@Override
	   public ReturnMsg printVoucher(ByteArrayOutputStream bos) {
			String paperType;
			String docCode;
			PrtdocJson ppj;
			paperType = Erpv4Config.getString(getSessionHelper(),"TfrPaperType");
			docCode = Erpv4Config.getString(getSessionHelper(),"TfrDocCode");
			if(paperType == null || paperType.trim().equals("")) paperType = "A4P";
			if(docCode == null || docCode .trim().equals("")) docCode = "GENTFR01";
			try {
				ppj = PrtdocJson.newPrtdocJson(	
					Erpv4Config.getDefaultCoCode(getSessionHelper()),
				    paperType,
				    docCode,
				    "erpv4_printDocument"
				    ) ;
//					ppj.setTrailerAtLastPageOnly(true);
				ppj.setTrailerAtLastPageOnly(true);
				ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
				String module = getCellString("stm_module");
						SelectUtil su = getSelectUtil();
						TableRec tr;
						ppj.addHeaderField("doctitle","Transfer");	
						ppj.addHeaderField("trfrom","From: " + getCellString("floc_desc"),0,0);
						tr = su.getQueryResult("select * from locationcode,location where loc_code = '"+ getCellString("stm_fromloc")+"' and lc_rg = loc_mrg");
						if(tr.getRecordCount() > 0) {
							tr.setRecPointer(0);
							ppj.addHeaderField("fromaddr",
							tr.getFieldString("lc_addr1") + " "+ 
							tr.getFieldString("lc_addr2") + " "+ 
							tr.getFieldString("lc_addr3") + " "+ 
							tr.getFieldString("lc_addr4")
							,0,0);
						}
						ppj.addHeaderField("trto","To: " + getCellString("tloc_desc"),0,0);
						tr = su.getQueryResult("select * from locationcode,location where loc_code = '"+ getCellString("stm_toloc")+"' and lc_rg = loc_mrg");
						if(tr.getRecordCount() > 0) {
							tr.setRecPointer(0);
							ppj.addHeaderField("toaddr",
							tr.getFieldString("lc_addr1") + " "+ 
							tr.getFieldString("lc_addr2") + " "+ 
							tr.getFieldString("lc_addr3") + " "+ 
							tr.getFieldString("lc_addr4")
							,0,0);
						}
						ppj.addHeaderField("dflabel","No.");
						ppj.addHeaderField("dfvalue",getCellString("stm_ref1"),0,0);
						ppj.addHeaderField("dflabel","Date",0,30);
						ppj.addHeaderField("dfvalue",DateUtil.toDateString( getCell("stm_date").getDate(),"dd/mm/yyyy") ,0,30);
						ppj.addHeaderField("dflabel","Page",0,60);

						ppj.addDetailHeaderField("hdr_idx","Item");
						ppj.addDetailHeaderField("hdr_oicode","SKU/Barcode");
						ppj.addDetailHeaderField("hdr_icode","Product Code");
						ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
						ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
						ppj.addDetailHeaderField("hdr_qty","QTY");
					BiResult ssr = getSubLink(stmdLinkName);
					Vector<BiCellCollection> v = ssr.getRowCollectionList();

					int idx=0;
					int cnt=0;
					for(BiCellCollection c : v) {
						ppj.addDetailRecord();
						idx++;
						String s = "";
						s += c.getCellString("st_iname");
//						s += "("+c.getCellString("st_icode")+")";
						ppj.addDetailRecordField("index", (""+idx));
						ppj.addDetailRecordField("icode", c.getCellString("st_icode"));
						ppj.addDetailRecordField("oicode", c.getCellString("st_oicode"));
						ppj.addDetailRecordField("description", s);
// 					ppj.setBold(false);
// 					ppj.setUnderLine(false);
						ppj.addDetailRecordField("quantity", c.getCell("stmd_qty").getString());
						cnt += (int) c.getCellDouble("stmd_qty");
					}
					ppj.addBottomField("hdr_total","Total:");
					ppj.addBottomField("val_total",""+cnt);
				return(ppj.toPdfStream(bos, getSessionHelper()));
			} catch(Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,ex.toString()));
			}
	    	
	    }	
	@Override
	protected boolean allowCrossLocation() {
		/*
		int lcrg = Erpv4Config.getDefaultLcrg(sh);
		int stlcrg = Erpv4Config.getStockTakeLcrg(sh);
		if(lcrg == stlcrg) return(true);
		*/
		return(false);
	}
}
