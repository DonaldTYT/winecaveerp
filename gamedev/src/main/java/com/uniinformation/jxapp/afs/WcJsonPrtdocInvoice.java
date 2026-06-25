package com.uniinformation.jxapp.afs;

import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.webcore.SessionHelper;

public class WcJsonPrtdocInvoice {
	public void print(SessionHelper p_sh,PrtdocJson ppj,BiCellCollection col, String indPrefix,Vector<BiCellCollection> v,SelectUtil su) throws Exception {
    		ppj.setTrailerAtLastPageOnly(true);
    		ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
    		int it = col.getCellInt("invh_invtype");
    		switch(it) {
    		case 1 : ppj.addHeaderField("doctitle","DEBIT NOTE");
    			break;
    		case 2 : ppj.addHeaderField("doctitle","CREDIT NOTE");
    			break;
    		default : ppj.addHeaderField("doctitle","INVOICE");
    			break;
    		}
    		if(Erpv4Config.getDefaultLogo(p_sh) != null) {
    			ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(p_sh) ,0,0,0,440);
    		}

    		ppj.addHeaderField("cvname",col.getCellString("vd_vname"));
    		/*
    		ppj.addHeaderField("cvname",getCellString("vd_addr0"),0,20);
    		ppj.addHeaderField("cvname",getCellString("vd_addr1"),0,40);
    		ppj.addHeaderField("cvname",getCellString("vd_addr2"),0,60);
    		*/
    		String addr = col.getCellString("vd_addr0").trim() + " " +  col.getCellString("vd_addr1").trim() + " " + col.getCellString("vd_addr2").trim();
    		ppj.addHeaderField("cvname",addr,0,20,0,320);
    		
    		ppj.addHeaderField("clphone","Phone",0,0);
    		ppj.addHeaderField("cvphone",col.getCellString("invh_tel"),0,0);
    		ppj.addHeaderField("clphone","Fax",0,18);
    		ppj.addHeaderField("cvphone",col.getCellString("invh_fax"),0,18);
    		ppj.addHeaderField("clphone","Attn",0,36);
    		ppj.addHeaderField("cvphone",col.getCellString("invh_contact"),0,36);
    		ppj.addHeaderField("dflabel","Invoice #",0,0);
    		ppj.addHeaderField("dfvalue",col.getCellString("invh_invno"),0,0);
    		ppj.addHeaderField("dflabel","Date",0,30);
    		ppj.addHeaderField("dfvalue", DateUtil.toDateString( col.getCell("invh_date").getDate(),"dd/mm/yyyy") ,0,30);
    		ppj.addHeaderField("dflabel","Page",0,60);


    		ppj.addDetailHeaderField("hdr_duedate","Due Date");
    		ppj.addDetailHeaderField("hdr_terms","TERMS.");
    		ppj.addDetailHeaderField("hdr_delivery","DELIVERY");
    		ppj.addDetailHeaderField("hdr_yourref","YOUR REF.");
    		ppj.addDetailHeaderField("hdr_ourref","OUR REF.");
    		ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
    		ppj.addDetailHeaderField("hdr_qty","QTY");
    		ppj.addDetailHeaderField("hdr_uprice","UNIT PRICE",0,-5);
    		ppj.addDetailHeaderField("hdr_uprice","("+ col.getCellString("invh_cid")+")",0,12);
    		ppj.addDetailHeaderField("hdr_disc","DISC",0,-5);
    		ppj.addDetailHeaderField("hdr_disc","%",0,12);
    		ppj.addDetailHeaderField("hdr_amount","AMOUNT",0,-5);
    		ppj.addDetailHeaderField("hdr_amount","("+ col.getCellString("invh_cid")+")",0,12);
    		ppj.addDetailHeaderField("val_yourref",col.getCell("invh_pocode").getColumnDisplayString());
    		ppj.addDetailHeaderField("val_ourref",col.getCell("invh_jobno").getColumnDisplayString());
    		
    		ppj.addDetailHeaderField("val_duedate",DateUtil.toDateString( col.getCell("invh_duedate").getDate(),"dd/mm/yyyy") ,0,0);
    		
    		ppj.addDetailRecord();
    		ppj.addDetailRecordField("description", col.getCell("invh_projecttitle").getString());
    		
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
    			if(c.getCell(indPrefix+"discpercent").getInt() > 0) {
    				ppj.addDetailRecordField("discount", c.getCell(indPrefix+"discpercent").getString());
    			}
    			if(c.getCell(indPrefix+"amount").getDouble() != 0) {
    				ppj.addDetailRecordField("amount", c.getCell(indPrefix+"amount").getString());
    			}
    		}
    		ppj.addDetailRecord();
    		if(!col.getCellString("invh_quonum").equals(""))
    			ppj.addDetailRecordField("description", "[Our Ref: " + col.getCell("invh_quonum").getString() + "]");
    		ppj.addBottomField("val_signco",Erpv4Config.getCoName(p_sh,col.getCellString("invh_cocode")));
    		ppj.addBottomField("hdr_total","Invoice Total:");
    		ppj.addBottomField("hdr_total","Less Discount:",0,20);
    		ppj.addBottomField("hdr_total","Less Trade In:",0,40);
    		ppj.addBottomField("hdr_total","Delivery:",0,60);
    		ppj.addBottomField("hdr_total","Net Amount:",0,80);
    		ppj.addBottomField("val_total",col.getCell("invh_total").getColumnDisplayString());
    		if(col.getCell("invh_discount").getDouble() != 0) {
    			ppj.addBottomField("val_total",col.getCell("invh_discount").getColumnDisplayString(),0,20);
    		}
    		if(col.getCell("invh_tradein").getDouble() != 0) {
    			ppj.addBottomField("val_total",col.getCell("invh_tradein").getColumnDisplayString(),0,40);
    		}
    		if(col.getCell("invh_delichg").getDouble() != 0) {
    			ppj.addBottomField("val_total",col.getCell("invh_delichg").getColumnDisplayString(),0,60);
    		}
   			ppj.addBottomField("val_total",col.getCell("invh_total").getColumnDisplayString(),0,80);
   			String remark = null; 
			if(remark == null) remark = col.getCell("invh_remark").getString(); else remark += "\r" + col.getCell("invh_remark").getString();
			if(!col.getCellString("invh_paytype").equals("")) {
				remark += "\r"+ col.getCellString("invh_paytype");
			}
			if(!col.getCellString("invh_term").equals("")) {
				remark += "\r"+ col.getCellString("invh_term");
			}

				TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+col.getCellString("invh_cocode")+ "'",null);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					if(!remark.trim().equals("")) remark += "\r";
					if(col.getCellInt("invh_bankinfo") > 0) {
						remark += tr.getFieldString("co_payment2");
					} else {
						remark += tr.getFieldString("co_payment");
					}
				}
   			ppj.addBottomField("val_remark",remark,0,0,15,0);
		
	}

	protected String makeInvoiceItemDescription(String indPrefix,CellCollection c,PrtdocJson ppj) {
    			String s = "";
    			if(c.testCell(indPrefix+"irg") != null) {
    				if(c.testCell(indPrefix+"irg").getInt() > 0) {
    					if(c.testCell("st_iname") != null)  {
    						s += c.getCellString("st_iname");
    					}
    				}
    			}
    			if(c.testCell(indPrefix+"desc") != null) {
   					s += c.getCellString(indPrefix+"desc");
    			}
    			return(s);
	}
}
