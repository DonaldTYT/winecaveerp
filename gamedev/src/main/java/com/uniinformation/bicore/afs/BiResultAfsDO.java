package com.uniinformation.bicore.afs;

import java.io.OutputStream;
import java.util.Date;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.erpv4.BiResultDO;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAfsDO extends BiResultDO {

	public BiResultAfsDO(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		UniLog.log("BiResultAfsDO Used");
//		stmdLinkName = "AfsDoDet";
//		if(getCell("stm_tolAmt") != null && stmdLinkName != null) {
//			tolAmtCell = "stm_tolAmt";
//			BiResult sr = getSubLink(stmdLinkName);
//			if(sr.getColumnByLabel("stmd_exprice0") != null) {
//				detAmtCell = "stmd_exprice0";
//			} else {
//				if(sr.getColumnByLabel("stmd_exprice") != null) {
//					detAmtCell = "stmd_exprice";
//				}
//			}
//		}
	}

	ReturnMsg updatePdStmdRef()
	{
			/* 
			  update stmd_ref of orddet from value in put during D/N update
			 */
			try {
				Vector <BiCellCollection> recs = getSubLinkResult(stmdLinkName);
				for(CellCollection col:recs) {
					TableRec tr = su.getQueryResult("select * from stmovd " +
							" where stmd_tdtype in ("+Erpv4Config.PURCHASE_TDtypes+") "
									+ " and stmd_org = " + col.getCell("stmd_org").getInt() 
									+ " and stmd_irg = " + col.getCell("stmd_irg").getInt(),null);
					if(tr.getRecordCount() > 0) {
						tr.setRecPointer(0);
						String execStr = null;
						Wherecl wcl = null;
						if(((String) tr.getField("stmd_ref")).equals("") ) {
							if(wcl == null) wcl = new Wherecl().appendString(
							"stmd_tdtype in ("+Erpv4Config.PURCHASE_TDtypes+") "
									+ " and stmd_org = " + col.getCell("stmd_org").getInt() 
									+ " and stmd_irg = " + col.getCell("stmd_irg").getInt()
									);
							wcl.appendArgument(col.getCell("orddet_ref").getString());
							if(execStr == null ) {
								execStr = "update stmovd set stmd_ref = ? ";
							} else {
								execStr += ", stmd_ref = ? ";
							}
						}
						if(((Double) tr.getField("stmd_fref1")).doubleValue() <= 0.0) {
							if(wcl == null) wcl = new Wherecl().appendString(
							"stmd_tdtype in ("+Erpv4Config.PURCHASE_TDtypes+") "
									+ " and stmd_org = " + col.getCell("stmd_org").getInt() 
									+ " and stmd_irg = " + col.getCell("stmd_irg").getInt()
									);
							wcl.appendArgument(col.getCell("stmd_fref1").getDouble());
							if(execStr == null ) {
								execStr = "update stmovd set stmd_fref1 = ? ";
							} else {
								execStr += ", stmd_fref1 = ? ";
							}
						}
						if(wcl != null) {
							su.executeUpdate(execStr,wcl);
							
						}
					}
				}
				return(null);
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,ex.toString()));
			}
	}	
	@Override
	public String newDnCode(Date p_date) {
		try {
			Value v = null;
			if(getCell("stm_module").getString().equals("parts")) {
				v = su.getRpcClient().callSegment("getuniquerg",
							new VectorUtil()
								.addElement(2015)
								.addElement("stmov")
								.addElement("stm_ref1")
								.addElement("ADN&&&&&")
								.addElement("")
								.toVector()
								);	
			} else {
				v = su.getRpcClient().callSegment("getuniquerg",
							new VectorUtil()
								.addElement(2025)
								.addElement("stmov")
								.addElement("stm_ref1")
								.addElement("MDN&&&&&")
								.addElement("")
								.toVector()
								);	
			}
			if(v == null) return(null); else return(v.toString());
		} catch (Exception ex) {
			UniLog.log(ex);
			return(null);
		}
	}
	@Override
	protected ReturnMsg biBeforeAddCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg;
		rtnMsg = super.biBeforeAddCurrent(pcol);
		if(rtnMsg == null || rtnMsg.getStatus()) {
		try {
			if(pcol.getCell("stm_ref1").getString().trim().equals("")) pcol.getCell("stm_ref1").set(newDnCode(getCell("stm_date").getDate()));
			pcol.getCell("stm_date").set(new java.util.Date());
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,cex.toString()));
		}
			return(updatePdStmdRef());
		}
		return(rtnMsg);
	}	
	@Override
	protected ReturnMsg biBeforeUpdateCurrent(CellCollection pcol) {
		ReturnMsg rtnMsg;
		rtnMsg = super.biBeforeUpdateCurrent(pcol);
		if(rtnMsg == null || rtnMsg.getStatus()) {
			return(updatePdStmdRef());
		}
		return(rtnMsg);
	}		
	static public String newInvCode(SelectUtil su,java.util.Date p_date,String p_prefix) {
		try {
			String s = null;
			java.util.Date d = p_date;
			String ds = DateUtil.toDateString(d, "yymmdd");
			int nextidx = 1;
			TableRec tr = su.getQueryResult("select stm_ref3 from stmov where stm_ref3 matches '" + p_prefix + ds + "*' order by stm_ref3 desc",null);
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				s = tr.getField("stm_ref3").toString();
				String ss = StringUtil.strpart(s, 11, -1);
				nextidx = Integer.parseInt(ss) + 1;
			}
			s = String.format("%s%s-%03d",p_prefix,ds, nextidx);
			return(s);
		} catch (Exception cex ) {
			UniLog.log(cex);
			return(null);
		}
	}		
	@Override
	public String getLinkedView(String p_colName,CellCollection p_col) {
		if(p_colName.equals("stm_ref3")) {
			Date dd = p_col.getCell("sih_duedate").getDate();
			if(dd != null && dd.after(DateUtil.minDate)) {
				return("erpv4.SihAr");
			} else return(null);
		}
		return(super.getLinkedView(p_colName,p_col));
	}

	@Override
	public String getLinkedColumn(String p_colName) {
		if(p_colName.equals("stm_ref3")) {
			return("sih_sno");
		}
		return(super.getLinkedColumn(p_colName));
	}

	@Override
	protected String makeInvoiceItemDescription(String indPrefix,CellCollection c,PrtdocJson ppj) {
		String s = "";
		/*
		if(c.testCell(indPrefix+"desc") != null)  {
			s += c.getCellString(indPrefix+"desc");
		}
		*/
		if(c.testCell(indPrefix+"subitem") != null) {
			if(!c.testCell(indPrefix+"subitem").getBoolean()) {
				if(c.testCell("stmcm_name") != null)  {
					ppj.setBold(true);
//					ppj.setUnderLine(true);
					s += c.getCellString("stmcm_name");
				}
			}
		}

		if(c.testCell(indPrefix+"irg") != null) {
			if(c.testCell(indPrefix+"irg").getInt() > 0) {
				if(c.testCell("st_mtype") != null) {
					String mt = c.getCellString("st_mtype");
					if(mt.equals("M")) {
						String md = c.getCellString("st_modelno");
						if(!md.isEmpty()) {
							s += md + " ";
						}
					}
					if(mt.equals("P")) {
						String pn = c.getCellString("st_oicode");
						if(!pn.isEmpty()) {
							s += pn + " ";
						}
					}
				}
				if(c.testCell("st_iname") != null)  {
					s += c.getCellString("st_iname");
				}
			}
		}
		if(c.testCell("stmd_ref4") != null)  {
			String sn = c.getCellString("stmd_ref4");
			if(!sn.isEmpty()) {
				s += " " + sn ;
			}
		}
		if(c.testCell(indPrefix+"desc") != null) {
				s += c.getCellString(indPrefix+"desc");
		}
		return(s);
	}	
	@Override
	public ReturnMsg printDnote(OutputStream os) {
		BiResultQuotation ppr = null;
		ppr = (BiResultQuotation) getParent();
//		if(ppr == null) return(new ReturnMsg(false,"Error : no parent quotation record"));
		try {
    		String paperType = Erpv4Config.getString(getSessionHelper(),"InvPaperType");
    		String dnDoc = Erpv4Config.getString(getSessionHelper(),"dnDocCode");
    		if(dnDoc == null) dnDoc = "GENINV02";
    		PrtdocJson ppj = PrtdocJson.newPrtdocJson(	
    				getCellString("stm_cocode"),
    				paperType,
    			    dnDoc,
    			    "erpv4_printDocument"
    				) ;
    		ppj.setTrailerAtLastPageOnly(true);
    		ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
//    		ppj.addHeaderField("doctitle","DELIVERY NOTE");
    		ppj.addHeaderField("doctitle","送貨單");
    		if(Erpv4Config.isMultiCompany(getSessionHelper())) {
    			String logo = Erpv4Config.getCoLogo(getSessionHelper(), getCellString("stm_cocode"));
    			if(logo != null && !logo.equals("")) {
    				ppj.addHeaderImage("logo", logo ,0,0,0,440);
    			}
    		} else {
    			if(Erpv4Config.getDefaultLogo(getSessionHelper()) != null) {
    				ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(getSessionHelper()) ,0,0,0,440);
    			}
    		}
    		if(!getCellString("svloc_desp").equals("")) {
    			ppj.addHeaderField("ciname","至：");
    			ppj.addHeaderField("cicontent",getCellString("svloc_desp"));
    			ppj.addHeaderField("ciname","送貨地址：",0,40);
    			String addr = getCellString("svloc_addr1").trim() + " " +  getCellString("svloc_addr2").trim() + " " + getCellString("svloc_city").trim();
    			ppj.addHeaderField("cicontent",addr,0,40);
    		}
   			ppj.addHeaderField("ciname","聯絡人：",0,80);
   			ppj.addHeaderField("cicontent",getCellString("stm_contact"),0,80);
   			ppj.addHeaderField("ciname","電話：",380,80);
   			ppj.addHeaderField("cicontent",getCellString("stm_tel"),380,80);
   			/*
    		ppj.addHeaderField("cicontent",getCellString("vd_vname"));
    		ppj.addHeaderField("ciname","至：");
    		ppj.addHeaderField("cicontent",getCellString("vd_vname"));
    		ppj.addHeaderField("cvname",getCellString("vd_addr0"),0,20);
    		ppj.addHeaderField("cvname",getCellString("vd_addr1"),0,40);
    		ppj.addHeaderField("cvname",getCellString("vd_addr2"),0,60);
    		*/

    		/*
    		if(false) {
    			ppj.addHeaderField("clphone","Phone",0,0);
    			ppj.addHeaderField("cvphone",getCellString("vd_tel"),0,0);
    			ppj.addHeaderField("clphone","Fax",0,18);
    			ppj.addHeaderField("cvphone",getCellString("vd_fax"),0,18);
    			ppj.addHeaderField("clphone","Attn",0,36);
    			ppj.addHeaderField("cvphone",getCellString("vd_contact"),0,36);
    		} else {
    			ppj.addHeaderField("clphone","Attn",0,0);
    			ppj.addHeaderField("cvphone",getCellString("stm_contact"),0,0);
    			ppj.addHeaderField("clphone","Tel",0,18);
    			ppj.addHeaderField("cvphone",getCellString("stm_tel"),0,18);
    		}
    		*/
    		
    		ppj.addHeaderField("dflabel","D/N #",0,0);
    		ppj.addHeaderField("dfvalue",getCellString("stm_ref1"),0,0);
    		ppj.addHeaderField("dflabel","Date:",0,30);
//    		ppj.addHeaderField("dfvalue", DateUtil.toDateString( getCell("stm_date").getDate(),"yyyy/mm/dd") ,0,30);
    		String ds = DateUtil.toDateString( getCell("stm_date").getDate(),"yyyy/mm/dd");
    		ppj.addHeaderField("dfvalue", " "+ds ,0,30);
    		ppj.addHeaderField("dflabel","Page",0,60);
 
    		ppj.addDetailHeaderField("hdr_seq","項");
    		ppj.addDetailHeaderField("hdr_orderno","合同號碼");
    		ppj.addDetailHeaderField("hdr_itemcode","型號");
    		ppj.addDetailHeaderField("hdr_description","貨名");
    		ppj.addDetailHeaderField("hdr_qty","件數");
    		ppj.addDetailHeaderField("hdr_serialno","機身編號");

    		ppj.addDetailHeaderField("hdr_seq","Item",0,20);
    		ppj.addDetailHeaderField("hdr_orderno","Contract No.",0,20);
    		ppj.addDetailHeaderField("hdr_itemcode","Model",0,20);
    		ppj.addDetailHeaderField("hdr_description","Description",0,20);
    		ppj.addDetailHeaderField("hdr_qty","QTY",0,20);
    		ppj.addDetailHeaderField("hdr_serialno","Serial No.",0,20);

    		Vector<BiCellCollection> v = null;
    		v = getSubLink(stmdLinkName).getRowCollectionList();
    		String indPrefix = "stmd_";

    		int n = 0;
    		for(BiCellCollection c : v) {
    			n++;
    			ppj.addDetailRecord();
    			ppj.addDetailRecordField("seq", ""+n);
    			String s = "";
    			/*
    			if(c.testCell(indPrefix+"desc") != null)  {
    				s += c.getCellString(indPrefix+"desc");
    			}
    			*/
    			if(c.testCell(indPrefix+"subitem") != null) {
    				if(!c.testCell(indPrefix+"subitem").getBoolean()) {
    					if(c.testCell("stmcm_name") != null)  {
    						ppj.setBold(true);
//    						ppj.setUnderLine(true);
    						s += c.getCellString("stmcm_name");
    					}
    				}
    			}
    			if(c.testCell(indPrefix+"irg") != null) {
    				if(c.testCell(indPrefix+"irg").getInt() > 0) {
    					if(c.testCell("st_iname") != null)  {
    						s += c.getCellString("st_iname");
    					}
    					if(Erpv4Config.getString(getSessionHelper(), "CustomSmartac") != null) {
							s += " ";
							s += "["+c.getCellString("st_icode")+"]";
    					}
    				}
    			}
    			if(c.testCell(indPrefix+"remark") != null) {
   					s += c.getCellString(indPrefix+"remark");
    			}
    			ppj.addDetailRecordField("description", s);
    			ppj.addDetailRecordField("serialno",c.getCellString(indPrefix+"ref4"));
    			ppj.addDetailRecordField("orderno",c.getCellString("inv_invno"));
    			String oicode = c.getCellString("st_oicode");
    			String modelno = c.getCellString("st_modelno");
    			if(oicode != null && !oicode.equals("")) {
    				ppj.addDetailRecordField("itemcode", oicode);
    			} else {
    				if(modelno != null && !modelno.equals("")) {
    					ppj.addDetailRecordField("itemcode", modelno);
    				}
    			}
    			ppj.setBold(false);
//    			ppj.setUnderLine(false);
    			if(c.getCell(indPrefix+"qty").getDouble() != 0) {
    			ppj.addDetailRecordField("quantity", c.getCell(indPrefix+"qty").getString());
    			}
    		}

    		/*
    		ppj.addDetailRecord();
    		if(!getCellString("stm_ref1").equals(""))
    			ppj.addDetailRecordField("description", "[Our Ref: " + getCell("stm_ref1").getString() + "]");
			*/

    		int ofs = 0;
   			String remark = null; 
   			remark = getCellString("floc_desc");
			if(remark == null) remark = getCell("stm_shipagent").getString(); else remark += "\r" + getCell("stm_shipagent").getString();
			if(!getCellString("stm_shipname").equals("")) {
				remark += "\r"+ getCellString("stm_shipname");
			}
			if(!getCellString("invh_term").equals("")) {
				remark += "\r"+ getCellString("invh_term");
			}
   			ppj.addBottomField("val_remark",remark,0,0,15,0);
    		return(ppj.toPdfStream(os, getSessionHelper()));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Fail Reason Unknown"));
		}	
		
	}
}
