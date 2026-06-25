package com.uniinformation.bicore.erpv4;

import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.Vector;

import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.PrtdocPrintInvoice;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.DynamicClassLoader;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public class BiResultDO extends BiResultStmov {
	public BiResultDO(BiResult p_parent,BiView p_view,SelectUtil p_su,Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException
	{
		super(p_parent,p_view,p_su,p_tabList, p_whereStr, p_sh);
		UniLog.log("BiResultDO Used");
		for(BiResult sr:getSubLinks()) {
			if(sr.getView().getTable().getName().equals("stmovd_so")) {
				stmdLinkName = sr.getView().getName();
				
			}
		}
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
	public String newDnCode(Date p_date) {
		try {
			java.util.Date d = p_date;
			String cocode = null;
			if(getCell("stm_cocode") != null)  {
				cocode = getCellString("stm_cocode");
			} else {
				cocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
			}
			
			String rgc = Erpv4Config.getString(getSessionHelper(), "rgcontrol_DN" );
			if(rgc != null && !rgc.trim().equals("")) {
				return(BiResultErpv4.getCodeByRgControl(this,cocode,rgc,d));
			}
			return("");
			/*
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
			*/
			
		} catch (Exception ex) {
			UniLog.log(ex);
			return(null);
		}
	}
	
	@Override
	protected ReturnMsg validateOneRow(CellCollection pcol,boolean isUpdate) {
		ReturnMsg rtnMsg;
		if(getCell("stm_cur") != null) try {
			BiResult sr = getSubLink(stmdLinkName);
			Vector<BiCellCollection> v = sr.getRowCollectionList();
			String dnCcy = null;
			for(BiCellCollection bc : v) {
				String stmdCcy = bc.getCellString("stmd_cur");
				if(stmdCcy.equals("")) {
					if(bc.getCell("stmd_cur").isOverrided()) bc.getCell("stmd_cur").clearOverride();
					if(bc.getCell("stmd_uprice").isOverrided()) bc.getCell("stmd_uprice").clearOverride();
					stmdCcy = bc.getCellString("stmd_cur");
				}
				if(!stmdCcy.equals("")) {
					if(dnCcy == null) dnCcy = stmdCcy; else {
						if(!dnCcy.equals(stmdCcy)) {
							return (new ReturnMsg(false,"Error !!! cannot have more than one billing currency in one D/N"));
						}
					}
				}
				/*
				double grtotal = bc.getCellDouble("inv_grtotal");
				double disc = bc.getCellDouble("inv_discount");
				double deposit = bc.getCellDouble("inv_invtotal");
				double amount = bc.getCellDouble("stmd_exprice");
				if(disc != 0) {
					bc.getCell("stmd_discount").set(disc * amount / grtotal);
				}
				if(deposit != 0) {
					bc.getCell("stmd_iexprice1").set(deposit * amount / grtotal);
				}
				*/
			}
			if(dnCcy != null) {
				pcol.getCell("stm_cur").set(dnCcy);
			}
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,cex.toString()));
		}
		try {
			Date d = pcol.getCell("stm_date").getDate();
			if(d == null || !d.after(DateUtil.minDate)) {
				d = DateUtil.today();
				pcol.getCell("stm_date").set(d);
			}
//			if(pcol.getCell("stm_ref1").getString().trim().equals("")) pcol.getCell("stm_ref1").set(newDnCode(pcol.getCell("stm_date").getDate()));
			if( StringUtils.isBlank(pcol.getCellString("stm_ref1")))  {
				pcol.getCell("stm_ref1").set(newDnCode(pcol.getCell("stm_date").getDate()));
			}
			if(pcol.testCell("stm_ref3") != null && pcol.getCell("stm_ref3").getString().trim().equals("")) {
				String rgc = Erpv4Config.getString(sh,"rgcontrol_DINV");
				if(rgc != null && !rgc.trim().equals("")) {
					String cocode = null;
					if(getCell("stm_cocode") != null)  {
						cocode = getCellString("stm_cocode");
					} else {
						cocode = Erpv4Config.getDefaultCoCode(getSessionHelper());
					}
					String ss = BiResultErpv4.getCodeByRgControl(this,cocode,rgc,d);
//					String ss = "A0001";
					if(ss != null && !ss.trim().equals("")) pcol.getCell("stm_ref3").set(ss.trim());
				}
			}
			int cc;
			cc = 0;
		} catch (Exception cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,cex.toString()));
		}
		if(!isUpdate) {
		}
		rtnMsg = super.validateOneRow(pcol,isUpdate);
		return(rtnMsg);
	}	
	@Override
	public ReturnMsg lockRecordForUpdate() {
		ReturnMsg rtnMsg = super.lockRecordForUpdate();
		if(rtnMsg != null && !rtnMsg.getStatus()) return(rtnMsg);
		if(getCell("stm_ref1").getString().equals("")) {
			try {
				getCell("stm_ref1").set(newDnCode(getCell("stm_date").getDate()));
			} catch (CellException cex) {
				UniLog.log(cex);
				rollbackWork();
				return(new ReturnMsg(false,"Error generation D/N Number"));
			}
		}
		return(rtnMsg);
	}
//	static public String newInvCode(SelectUtil su,java.util.Date p_date,String p_prefix) {
//		try {
//			String s = null;
//			java.util.Date d = p_date;
//			String ds = DateUtil.toDateString(d, "yymmdd");
//			int nextidx = 1;
//			TableRec tr = su.getQueryResult("select stm_ref3 from stmov where stm_ref3 matches '" + p_prefix + ds + "*' order by stm_ref3 desc",null);
//			if(tr.getRecordCount() > 0) {
//				tr.setRecPointer(0);
//				s = tr.getField("stm_ref3").toString();
//				String ss = StringUtil.strpart(s, 11, -1);
//				nextidx = Integer.parseInt(ss) + 1;
//			}
//			s = String.format("%s%s-%03d",p_prefix,ds, nextidx);
//			return(s);
//		} catch (Exception cex ) {
//			UniLog.log(cex);
//			return(null);
//		}
//	}		
	
	public ReturnMsg printDnote(OutputStream os) {
		BiResultQuotation ppr = null;
		ppr = (BiResultQuotation) getParent();
//		if(ppr == null) return(new ReturnMsg(false,"Error : no parent quotation record"));
		try {
    		String paperType = Erpv4Config.getString(getSessionHelper(),"InvPaperType");
    		String invDoc = Erpv4Config.getString(getSessionHelper(),"InvDocCode");
    		if(invDoc == null) invDoc = "GENINV01";
    		PrtdocJson ppj = PrtdocJson.newPrtdocJson(	
    				getCellString("stm_cocode"),
    				paperType,
    			    invDoc,
    			    "erpv4_printDocument"
    				) ;
    		ppj.setTrailerAtLastPageOnly(true);
    		ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
    		ppj.addHeaderField("doctitle","DELIVERY NOTE");
    		if(Erpv4Config.isMultiCompany(getSessionHelper())) {
    			String logo = Erpv4Config.getCoLogo(getSessionHelper(), getCellString("stm_cocode"));
    			if(logo != null && !logo.equals("")) {
    				ppj.addHeaderImage("logo", logo ,50,0,100,0);
    			}
				if(Erpv4Config.getString(getSessionHelper(), "CustomSmartac") != null) {
					ppj.setBold(true);
					ppj.addHeaderField("logo",Erpv4Config.getCoName(getSessionHelper(), getCellString("stm_cocode")),200,10);
					ppj.addHeaderField("logo",Erpv4Config.getCoAddr(getSessionHelper(), getCellString("stm_cocode")),200,40);
					ppj.setBold(false);
					/*
					if(getCellString("stm_cocode").equals("005")) {
						ppj.addHeaderField("logo","FPS轉數快  ID: 161693544" ,540,920);
						ppj.addHeaderImage("logo", "logo/smartac_barcode_01.jpg" ,600,950,120,0);
					}
					*/
				}
    		} else {
    			if(Erpv4Config.getDefaultLogo(getSessionHelper()) != null) {
    				ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(getSessionHelper()) ,50,0,100,0);
    			}
    		}
    		ppj.addHeaderField("cvname",getCellString("vd_vname"));
    		ppj.addHeaderField("cvname",getCellString("vd_addr0"),0,20);
    		ppj.addHeaderField("cvname",getCellString("vd_addr1"),0,40);
    		ppj.addHeaderField("cvname",getCellString("vd_addr2"),0,60);

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
    		
    		ppj.addHeaderField("dflabel","D/N #",0,0);
    		ppj.addHeaderField("dfvalue",getCellString("stm_ref1"),0,0);
    		ppj.addHeaderField("dflabel","Date",0,30);
//    		ppj.addHeaderField("dfvalue", DateUtil.toDateString( getCell("stm_date").getDate(),"yyyy/mm/dd") ,0,30);
    		String ds = DateUtil.toDateString( getCell("stm_date").getDate(),"yyyy/mm/dd");
    		ppj.addHeaderField("dfvalue", " "+ds ,0,30);
    		ppj.addHeaderField("dflabel","Page",0,60);
 
    		ppj.addDetailHeaderField("hdr_duedate","Due Date");
    		ppj.addDetailHeaderField("hdr_terms","TERMS.");
    		ppj.addDetailHeaderField("hdr_delivery","DELIVERY");
    		ppj.addDetailHeaderField("hdr_yourref","YOUR REF.");
    		ppj.addDetailHeaderField("hdr_ourref","OUR REF.");
    		ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
    		ppj.addDetailHeaderField("hdr_qty","QTY");

    		/*
    		ppj.addDetailHeaderField("hdr_uprice","UNIT PRICE",0,-5);
    		ppj.addDetailHeaderField("hdr_uprice","("+ getCellString("stm_cur")+")",0,12);
    		
   			if(getCellString("stm_module").equals("machine")) {
   				ppj.addDetailHeaderField("hdr_disc","DEPO",0,-5);
   			} else {
   				ppj.addDetailHeaderField("hdr_disc","DISC",0,-5);
   			}
    		ppj.addDetailHeaderField("hdr_disc","%",0,12);
    		ppj.addDetailHeaderField("hdr_amount","AMOUNT",0,-5);
    		ppj.addDetailHeaderField("hdr_amount","("+ getCellString("stm_cur")+")",0,12);
    		*/

//    		ppj.addDetailHeaderField("val_yourref",getCell("invh_pocode").getColumnDisplayString());
//    		ppj.addDetailHeaderField("val_duedate",DateUtil.toDateString( getCell("invh_duedate").getDate(),"dd/mm/yyyy") ,0,0);

    		Vector<BiCellCollection> v = null;
    		v = getSubLink(stmdLinkName).getRowCollectionList();
    		String indPrefix = "stmd_";

    		for(BiCellCollection c : v) {
    			ppj.addDetailRecord();
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
    			if(c.testCell(indPrefix+"desc") != null) {
   					s += c.getCellString(indPrefix+"desc");
    			}
    			ppj.addDetailRecordField("description", s);
    			ppj.setBold(false);
//    			ppj.setUnderLine(false);
    			if(c.getCell(indPrefix+"qty").getDouble() != 0) {
    			ppj.addDetailRecordField("quantity", c.getCell(indPrefix+"qty").getString());
    			}
    			
    			/*
    			if((c.getCellDouble(indPrefix+"sprice") != 0)  &&
    			   (c.getCell(indPrefix+"discpercent").getInt() != 0)) {
   					ppj.addDetailRecordField("price", c.getCell(indPrefix+"sprice").getString());
    			} else {
    				if(c.getCell(indPrefix+"uprice").getDouble() != 0) {
    					ppj.addDetailRecordField("price", c.getCell(indPrefix+"uprice").getString());
    				}
    				
    			}
    			if(getCellString("stm_module").equals("machine")) {
    				double dp = c.getCellDouble(indPrefix+"iexprice1");
    				double amt  = c.getCellDouble(indPrefix+"exprice");
    				if(dp != 0.0 && amt != 0) {
    					int  dpi = (int) (dp * 100 / amt);
    					ppj.addDetailRecordField("discount", (""+dpi));
    				}
    			} else {
    				if(c.getCellInt(indPrefix+"discpercent") > 0) {
    					ppj.addDetailRecordField("discount", c.getCell(indPrefix+"discpercent").getString());
    				}
    			}
    			if(c.getCell(indPrefix+"exprice").getDouble() != 0) {
    				ppj.addDetailRecordField("amount", c.getCell(indPrefix+"exprice").getString());
    			}
    			*/
    		}

    		ppj.addDetailRecord();
    		if(!getCellString("stm_ref1").equals(""))
    			ppj.addDetailRecordField("description", "[Our Ref: " + getCell("stm_ref1").getString() + "]");
//    		ppj.addBottomField("val_signco",Erpv4Config.getCoName(getSessionHelper(),getCellString("stm_cocode")));


    		int ofs = 0;
    		/*
    		if(getCellDouble("stm_discount") != 0) {
    			ppj.addBottomField("hdr_total","Less Discount:",0,ofs);
    			ppj.addBottomField("val_total",getCell("stm_discount").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    		}
    		ppj.addBottomField("hdr_total","Net Total:",0,ofs);
    		ppj.addBottomField("val_total",getCell("stm_nettotal").getColumnDisplayString(),0,ofs);
   			ofs += 20;
    		if(getCellDouble("stm_deposit") != 0) {
    			ppj.addBottomField("hdr_total","Less Deposit:",0,ofs);
    			ppj.addBottomField("val_total",getCell("stm_deposit").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    		}
   			ppj.addBottomField("hdr_total","Invoice Amount:",0,ofs);
   			ppj.addBottomField("val_total",getCell("stm_invamount").getColumnDisplayString(),0,ofs);
   			ofs += 20;
   			*/
   			String remark = null; 
   			remark = getCellString("floc_desc");
			if(remark == null) remark = getCell("stm_shipagent").getString(); else remark += "\r" + getCell("stm_shipagent").getString();
			if(!getCellString("stm_shipname").equals("")) {
				remark += "\r"+ getCellString("stm_shipname");
			}
			if(!getCellString("invh_term").equals("")) {
				remark += "\r"+ getCellString("invh_term");
			}

			/*
				SelectUtil su = getSelectUtil();
				TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+getCellString("stm_cocode")+ "'",null);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					if(!remark.trim().equals("")) remark += "\r";
					if(getCellString("stm_module").equals("parts")) 
						remark += tr.getFieldString("co_payment2");
					else 
						remark += tr.getFieldString("co_payment");
				}
				*/
			/*
			if(getCellString("stm_cocode").equals("005")) {
				ppj.addBottomField("logo","FPS轉數快  ID: 161693544" ,540,920);
				ppj.addBottomImage("logo", "logo/smartac_barcode_01.jpg" ,600,950,120,0);
			}
			*/
   			ppj.addBottomField("val_remark",remark,0,0,15,0);
    		return(ppj.toPdfStream(os, getSessionHelper()));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Fail Reason Unknown"));
		}	
		
	}
	
	protected String makeInvoiceItemDescription(String indPrefix,CellCollection c,PrtdocJson ppj) {
		String s = "";
		if(c.testCell(indPrefix+"irg") != null) {
			if(c.testCell(indPrefix+"irg").getInt() > 0) {
				if(c.testCell("st_iname") != null)  {
					s += c.getCellString("st_iname");
					if(Erpv4Config.getString(getSessionHelper(), "CustomSmartac") != null) {
						s += " ";
						s += "["+c.getCellString("st_icode")+"]";
					}
				}
			}
		}
		if(c.testCell(indPrefix+"desc") != null) {
				s += c.getCellString(indPrefix+"desc");
		}	
		return(s);
	}	
	
//	public ReturnMsg PrintOneDocument(String p_prtdocClass,OutputStream os,String p_coCode,String p_docCode,String p_paperType) {
//		try {
//			String coCode = p_coCode;
//			String docCode = p_docCode;
//			String paperType = p_paperType;
//			if(coCode == null) coCode = Erpv4Config.getDefaultCoCode(getSessionHelper());
//			if(docCode == null) docCode = "GENINV01";
//			if(paperType == null) paperType = "A4P";
//    		PrtdocJson ppj = PrtdocJson.newPrtdocJson(	
//    				coCode,
//    				paperType,
//    			    docCode,
//    			    "erpv4_printDocument"
//    				) ;
//    		PrtdocPrintInvoice jpi = null;
//			Class[]	paramTypes = new Class[]{BiResultErpv4.class,PrtdocJson.class,JSONObject.class};
//    		Class prtdocClass = Class.forName(p_prtdocClass);
//    		Constructor constructor = prtdocClass.getConstructor(paramTypes);
//    		if(constructor == null) {
//    			return(new ReturnMsg(false,"Fail PirintInvoiceClass "+ p_prtdocClass + " not found"));
//    		}	
//    		jpi = (PrtdocPrintInvoice) constructor.newInstance(this,ppj,null);
// 			jpi.print();
//    		return(ppj.toPdfStream(os, getSessionHelper()));
//		} catch (Exception ex) {
//			UniLog.log(ex);
//			return(new ReturnMsg(false,"Fail Reason Unknown"));
//		}	
//	}
	public ReturnMsg printInvoice(OutputStream os,JSONObject p_option) {
    	String printInvoiceClass = Erpv4Config.getString(getSessionHelper(), "PrtdocPrintInvoiceClass");
    	if(printInvoiceClass == null || printInvoiceClass.isEmpty()) {
    		printInvoiceClass = "com.uniinformation.erpv4.PrtdocPrintInvoice";
    		return(
    			PrintOneDocument(
    					printInvoiceClass,
    					os, getCellString("stm_cocode"),
    					Erpv4Config.getString(getSessionHelper(),"InvDocCode"),
    					Erpv4Config.getString(getSessionHelper(),"InvPaperType"),
    					p_option
    			)
    			);
    	} else {
			try {
				PrtdocClass jpi = null;
				Class[]	paramTypes = new Class[]{BiResultErpv4.class};
				jpi = (PrtdocClass) DynamicClassLoader.newInstance(printInvoiceClass, paramTypes, this);
				jpi.print();
				return jpi.getPrintDocJson().toPdfStream(os, getSessionHelper());	
			}
			catch (NoSuchMethodException ex) {
				return(
	   			PrintOneDocument(
    					printInvoiceClass,
    					os, getCellString("stm_cocode"),
    					Erpv4Config.getString(getSessionHelper(),"InvDocCode"),
    					Erpv4Config.getString(getSessionHelper(),"InvPaperType"),
    					p_option
    			)
    			);
			} catch (Exception ex) {
				UniLog.log(ex); 
				return new ReturnMsg(false, ex);
			}
    	}
	}
	
	/*
	public ReturnMsg printInvoiceXX(OutputStream os) {
		BiResultQuotation ppr = null;
		ppr = (BiResultQuotation) getParent();
//		if(ppr == null) return(new ReturnMsg(false,"Error : no parent quotation record"));
		try {
    		String paperType = Erpv4Config.getString(getSessionHelper(),"InvPaperType");
    		String invDoc = Erpv4Config.getString(getSessionHelper(),"InvDocCode");
    		if(invDoc == null) invDoc = "GENINV01";
    		PrtdocJson ppj = PrtdocJson.newPrtdocJson(	
    				getCellString("stm_cocode"),
    				paperType,
    			    invDoc,
    			    "erpv4_printDocument"
    				) ;
    		ppj.setTrailerAtLastPageOnly(true);
    		ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
    		ppj.addHeaderField("doctitle","INVOICE");
    		if(Erpv4Config.isMultiCompany(getSessionHelper())) {
    			String logo = Erpv4Config.getCoLogo(getSessionHelper(), getCellString("stm_cocode"));
    			if(logo != null && !logo.equals("")) {
    				ppj.addHeaderImage("logo", logo ,50,0,100,0);
    			}
				if(Erpv4Config.getString(getSessionHelper(), "CustomSmartac") != null) {
					ppj.setBold(true);
					ppj.addHeaderField("logo",Erpv4Config.getCoName(getSessionHelper(), getCellString("stm_cocode")),200,10);
					ppj.addHeaderField("logo",Erpv4Config.getCoAddr(getSessionHelper(), getCellString("stm_cocode")),200,40);
					ppj.setBold(false);
					if(getCellString("stm_cocode").equals("005")) {
						ppj.addHeaderField("logo","FPS轉數快  ID: 161693544" ,540,920);
						ppj.addHeaderImage("logo", "logo/smartac_barcode_01.jpg" ,600,950,120,0);
					}
				}
    		} else {
    			if(Erpv4Config.getDefaultLogo(getSessionHelper()) != null) {
    				ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(getSessionHelper()) ,50,0,100,0);
    			}
    		}
    		ppj.addHeaderField("cvname",getCellString("vd_vname"));
//    		ppj.addHeaderField("cvname",getCellString("vd_addr0"),0,20);
//   		ppj.addHeaderField("cvname",getCellString("vd_addr1"),0,40);
//    		ppj.addHeaderField("cvname",getCellString("vd_addr2"),0,60);

    		String addr = getCellString("vd_addr0").trim() + " " +  getCellString("vd_addr1").trim() + " " + getCellString("vd_addr2").trim() + getCellString("vd_addr3").trim();
    		ppj.addHeaderField("cvname",addr,0,20,0,320);
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
    		ppj.addHeaderField("dflabel","Invoice #",0,0);
    		ppj.addHeaderField("dfvalue",getCellString("stm_ref3"),0,0);
    		ppj.addHeaderField("dflabel","Date",0,30);
    		ppj.addHeaderField("dfvalue", DateUtil.toDateString( getCell("stm_date").getDate(),"dd/mm/yyyy") ,0,30);
    		ppj.addHeaderField("dflabel","Page",0,60);
 
    		ppj.addDetailHeaderField("hdr_duedate","Due Date");
    		ppj.addDetailHeaderField("hdr_terms","TERMS.");
    		ppj.addDetailHeaderField("hdr_delivery","DELIVERY");
    		ppj.addDetailHeaderField("hdr_yourref","YOUR REF.");
    		ppj.addDetailHeaderField("hdr_ourref","OUR REF.");
    		ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
    		ppj.addDetailHeaderField("hdr_qty","QTY");
    		ppj.addDetailHeaderField("hdr_uprice","UNIT PRICE",0,-5);
    		ppj.addDetailHeaderField("hdr_uprice","("+ getCellString("stm_cur")+")",0,12);
   			if(getCellString("stm_module").equals("machine")) {
   				ppj.addDetailHeaderField("hdr_disc","DEPO",0,-5);
   			} else {
   				ppj.addDetailHeaderField("hdr_disc","DISC",0,-5);
   			}
    		ppj.addDetailHeaderField("hdr_disc","%",0,12);
    		ppj.addDetailHeaderField("hdr_amount","AMOUNT",0,-5);
    		ppj.addDetailHeaderField("hdr_amount","("+ getCellString("stm_cur")+")",0,12);

//    		ppj.addDetailHeaderField("val_yourref",getCell("invh_pocode").getColumnDisplayString());
    		if(getCell("sih_duedate") != null ) {
    			ppj.addDetailHeaderField("val_duedate",DateUtil.toDateString( getCell("sih_duedate").getDate(),"dd/mm/yyyy") ,0,0);
    		}
//    		ppj.addDetailHeaderField("val_duedate",DateUtil.toDateString( getCell("invh_duedate").getDate(),"dd/mm/yyyy") ,0,0);

    		Vector<BiCellCollection> v = null;
    		v = getSubLink(stmdLinkName).getRowCollectionList();
    		String indPrefix = "stmd_";

    		for(BiCellCollection c : v) {
    			ppj.addDetailRecord();
    			String s = makeInvoiceItemDescription(indPrefix,c,ppj);
    			ppj.addDetailRecordField("description", s);
    			ppj.setBold(false);
//    			ppj.setUnderLine(false);
    			if(c.getCell(indPrefix+"qty").getDouble() != 0) {
    			ppj.addDetailRecordField("quantity", c.getCell(indPrefix+"qty").getString());
    			}
    			if((c.getCellDouble(indPrefix+"sprice") != 0)  &&
    			   (c.getCell(indPrefix+"discpercent").getInt() != 0)) {
   					ppj.addDetailRecordField("price", c.getCell(indPrefix+"sprice").getString());
    			} else {
    				if(c.getCell(indPrefix+"uprice").getDouble() != 0) {
    					ppj.addDetailRecordField("price", c.getCell(indPrefix+"uprice").getString());
    				}
    				
    			}
    			if(getCellString("stm_module").equals("machine")) {
    				double dp = c.getCellDouble(indPrefix+"iexprice1");
    				double amt  = c.getCellDouble(indPrefix+"exprice");
    				if(dp != 0.0 && amt != 0) {
    					int  dpi = (int) (dp * 100 / amt);
    					ppj.addDetailRecordField("discount", (""+dpi));
    				}
    			} else {
    				if(c.getCellInt(indPrefix+"discpercent") > 0) {
    					ppj.addDetailRecordField("discount", c.getCell(indPrefix+"discpercent").getString());
    				}
    			}
    			if(c.getCell(indPrefix+"exprice").getDouble() != 0) {
    				ppj.addDetailRecordField("amount", c.getCell(indPrefix+"exprice").getString());
    			}
    		}

    		ppj.addDetailRecord();
    		if(!getCellString("stm_ref1").equals(""))
    			ppj.addDetailRecordField("description", "[Our Ref: " + getCell("stm_ref1").getString() + "]");
    		ppj.addBottomField("val_signco",Erpv4Config.getCoName(getSessionHelper(),getCellString("stm_cocode")));


    		int ofs = 0;
    		
    		if(getCell("stm_grosstotal") != null) {
    			ppj.addBottomField("hdr_total","Gross Total:",0,ofs);
    			ppj.addBottomField("val_total",getCell("stm_grosstotal").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    		}
    		if(getCellDouble("stm_discount") != 0) {
    			ppj.addBottomField("hdr_total","Less Discount:",0,ofs);
    			ppj.addBottomField("val_total",getCell("stm_discount").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    		}

    		if(getCellDouble("stm_fref1") != 0) {
    			ppj.addBottomField("hdr_total", getServiceName(getCellString("stm_salescode1"))+":" ,0,ofs);
    			ppj.addBottomField("val_total",getCell("stm_fref1").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    		}
    		if(getCellDouble("stm_fref2") != 0) {
    			ppj.addBottomField("hdr_total", getServiceName(getCellString("stm_salescode2"))+":" ,0,ofs);
    			ppj.addBottomField("val_total",getCell("stm_fref2").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    		}
    		ppj.addBottomField("hdr_total","Net Total:",0,ofs);
    		ppj.addBottomField("val_total",getCell("stm_nettotal").getColumnDisplayString(),0,ofs);
   			ofs += 20;
    		if(getCellDouble("stm_deposit") != 0) {
    			ppj.addBottomField("hdr_total","Less Deposit:",0,ofs);
    			ppj.addBottomField("val_total",getCell("stm_deposit").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    			ppj.addBottomField("hdr_total","Invoice Amount:",0,ofs);
    			ppj.addBottomField("val_total",getCell("stm_invamount").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    		}
   			String remark = null; 
			if(remark == null) remark = getCell("stm_shipagent").getString(); else remark += "\r" + getCell("stm_shipagent").getString();
			if(!getCellString("stm_shipname").equals("")) {
				remark += "\r"+ getCellString("stm_shipname");
			}
			if(!getCellString("invh_term").equals("")) {
				remark += "\r"+ getCellString("invh_term");
			}

				SelectUtil su = getSelectUtil();
				TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+getCellString("stm_cocode")+ "'",null);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					if(!remark.trim().equals("")) remark += "\r";
					if(getCellString("stm_module").equals("parts")) 
						remark += tr.getFieldString("co_payment2");
					else 
						remark += tr.getFieldString("co_payment");
				}
   			ppj.addBottomField("val_remark",remark,0,0,15,0);
    		return(ppj.toPdfStream(os, getSessionHelper()));
		} catch (Exception ex) {
			UniLog.log(ex);
			return(new ReturnMsg(false,"Fail Reason Unknown"));
		}	
		
	}
	*/
	
//	String getServiceName(String p_accode) {
//		try {
//			SelectUtil su = getSelectUtil();
//			TableRec tr = su.getQueryResult("select * from prdsrvmaster where pds_ano = '"+ p_accode + "'");
//			if(tr.getRecordCount() > 0) {
//				tr.setRecPointer(0);
//				return(tr.getFieldString("pds_desc"));
//			}
//		} catch (Exception ex) {
//			UniLog.log(ex);
//		}
//		return("");
//	}
}
