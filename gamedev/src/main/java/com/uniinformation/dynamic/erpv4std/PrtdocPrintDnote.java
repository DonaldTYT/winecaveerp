package com.uniinformation.dynamic.erpv4std;

import java.util.HashSet;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.bicore.erpv4.BiResultInvoice;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.PrtdocPrintInvoice;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.Wherecl;

/* 
 *  This class is outdated. It use stmpostinv as master record to print multiple d/n or invoice , for each customer/so/currency that is to deliver in a single D/N entry.
 *  this is only use in view DoMulti. DoMulti is not use by any deployed system and will be replaced by DoMultiG2 which while D/N for multiple customer is supported, do not 
 *  support "Invoice by D/N" mode, thus will not generate stmpostinv.
 *  2023/05/16 by DT
 */
public class PrtdocPrintDnote extends PrtdocPrintInvoice {
	Vector<BiCellCollection> invList=null;
	public PrtdocPrintDnote(BiResultErpv4 p_br, PrtdocJson p_ppj,JSONObject p_option) {
		super(p_br, p_ppj,p_option);
		BiResult sr = p_br.getSubLink("erpv4.StmPostInv");
		if(sr != null) {
			invList = sr.getRowCollectionList();
		}
	}
	
	protected void printMultiDoInvoice() throws Exception {
		HashSet<String>snolist = null;
		if(option != null) {
			snolist = new HashSet<String>();
			JSONArray sno = option.optJSONArray("snolist");
			if(sno != null)	{
				for(int i=0;i<sno.length();i++) {
					snolist.add(sno.getString(i));
				}
			}
		}
        TableRec cotr = this.su.getQueryResult("select * from cocode where co_cocode = '" + this.col.getCellString("stm_cocode") + "'", (Wherecl)null);
        cotr.setRecPointer(0);

		int docIdx=0;
        for(BiCellCollection invoice : invList) {
        	String sno = invoice.getCellString("stmpi_sno");
        	if(snolist == null  || snolist.contains(sno)) {
        		if(docIdx > 0) ppj.newContent();
        		docIdx++;

        		ppj.setTrailerAtLastPageOnly(true);
        		ppj.addPageNo("page", "%s / %s", 0, 0, 0);
        		ppj.addHeaderField("doctitle", "發 INVOICE 票");
		        String addr;
		        /*
    		    this.ppj.addHeaderImage("logo", "logo/hapyic/1.png",0,0,70,0);
        		this.ppj.addHeaderImage("logo2", "logo/hapyic/2.png",0,0,70,0);
		        this.ppj.addHeaderImage("logo3", "logo/hapyic/3.png",0,0,50,0);
		        */
        
    		    int yofs = 0;
        		addr = cotr.getFieldString("co_coaddr1") + cotr.getFieldString("co_coaddr2") +cotr.getFieldString("co_coaddr3");
		        ppj.addHeaderField("coaddr", addr,0,yofs);
    		    addr = cotr.getFieldString("co_eaddr");
        		yofs = 12;
		        if(!addr.isEmpty()) {
    		    	ppj.addHeaderField("coaddr", addr,0,yofs);
        			yofs += 12;
        		}
		        String ss = "";
    		    addr = cotr.getFieldString("co_telnum");
        		if(!addr.isEmpty()) ss += "電話/Tel:"+ ss;
		        addr = cotr.getFieldString("co_faxnum");
    		    if(!addr.isEmpty()) ss += "傳真/Fax:"+ ss;
        		addr = cotr.getFieldString("co_email");
		        if(!addr.isEmpty()) ss += "電郵/Email:"+ ss;
    		    if(!ss.isEmpty()) {
        			ppj.addHeaderField("coaddr", ss,0,yofs);
        			yofs += 12;
		        }
    		    ppj.addHeaderField("cvname", invoice.getCellString("vd_vname"));
    		    addr = String.valueOf(invoice.getCellString("svloc_addr1").trim()) + " " + invoice.getCellString("svloc_addr2").trim() + " " + invoice.getCellString("svloc_city").trim() + " " + invoice.getCellString("svloc_state");
    		    ppj.addHeaderField("cvaddr", addr);
    		    ppj.addHeaderField("cvphone", invoice.getCellString("inv_tel"));
    		    this.ppj.addHeaderField("dfvalue", invoice.getCellString("stmpi_sno"), 0, 0);
    		    this.ppj.addHeaderField("barcode", invoice.getCellString("stmpi_sno"), 0, 0);
    		    this.ppj.addHeaderField("dfvalue", invoice.getCellString("inv_pocode"), 0, 15);
    		    this.ppj.addHeaderField("dfvalue", DateUtil.toDateString(invoice.getCell("stmpi_invdate").getDate(), "dd/mm/yyyy"), 0, 45);
    		    this.ppj.addHeaderField("dfvalue", DateUtil.toDateString(col.getCell("stm_date").getDate(), "dd/mm/yyyy"), 0, 60);

    		    this.ppj.addDetailHeaderField("hdr_description", "貨名",0,0);
    		    this.ppj.addDetailHeaderField("hdr_description", "Description",0,15);
    		    this.ppj.addDetailHeaderField("hdr_standard", "規格",0,0);
    		    this.ppj.addDetailHeaderField("hdr_standard", "Standards",0,15);

    		    this.ppj.addDetailHeaderField("hdr_qty", "數量",0,0);
    		    this.ppj.addDetailHeaderField("hdr_qty", "Quantity",0,15);

    		    this.ppj.addDetailHeaderField("hdr_uprice", "單價",0,0);
    		    this.ppj.addDetailHeaderField("hdr_uprice", "Unit Price",0,15);

    		    this.ppj.addDetailHeaderField("hdr_disc", "折扣",0,0);
    		    this.ppj.addDetailHeaderField("hdr_disc", "Discount",0,15);
    		    this.ppj.addDetailHeaderField("hdr_amount", "金額",0,0);
    		    this.ppj.addDetailHeaderField("hdr_amount", "Amount",0,15);

    		    for (final BiCellCollection c : this.v) {
    		    	if(c.getCellString("inv_vcode").equals(invoice.getCellString("inv_vcode"))
    		    	    && c.getCellString("inv_cid").equals(invoice.getCellString("stmpi_cid"))) {
    		    		this.ppj.addDetailRecord();
    		    		final String s = makeInvoiceItemDescription("stmd_", (CellCollection)c, this.ppj);
            			this.ppj.addDetailRecordField("description", s);
            			this.ppj.setBold(false);
            			if (c.getCell("stmd_entryqty").getDouble() != 0.0) {
            				this.ppj.addDetailRecordField("quantity", 
            						c.getCell("stmd_entryqty").getColumnDisplayString() +
            						c.getCell("stmd_entryunit").getColumnDisplayString()
            				);
            				double eq = c.getCell("stmd_entryqty").getDouble();
            				double uq = c.getCell("stmd_qty").getDouble();
            				if(eq != uq) {
            					ppj.addDetailRecordField("standard", 
            							"每" +
            							c.getCell("stmd_entryunit").getString() +
            							((int)(uq/eq))+
            							c.getCell("st_unit").getString() 
            					);
            				}
            			}
            			if (c.getCell("stmd_oprice").getDouble() != 0.0) {
            				ppj.addDetailRecordField("price", 
            						c.getCell("stmd_oprice").getColumnDisplayString()
            						+"/"+
            						c.getCell("stmd_entryunit").getString()
                			);
            			}
            			if (c.getCell("ind_discount").getDouble() != 0.0) {
            				ppj.addDetailRecordField("discount", 
            						c.getCell("ind_discount").getColumnDisplayString()
                			);
            			}
            			if (c.getCell("stmd_exprice").getDouble() != 0.0) {
            				ppj.addDetailRecordField("amount", 
            						c.getCell("stmd_exprice").getColumnDisplayString()
                			);
            			}

//            if (c.getCellDouble(String.valueOf(this.indPrefix) + "sprice") != 0.0 && c.getCell(String.valueOf(this.indPrefix) + "discpercent").getInt() != 0) {
//                this.ppj.addDetailRecordField("price", c.getCell(String.valueOf(this.indPrefix) + "sprice").getString());
//            }
//            else if (c.getCell(String.valueOf(this.indPrefix) + "uprice").getDouble() != 0.0) {
//                this.ppj.addDetailRecordField("price", c.getCell(String.valueOf(this.indPrefix) + "uprice").getString());
//            }
//            if (c.getCell(String.valueOf(this.indPrefix) + "discpercent").getInt() > 0) {
//                this.ppj.addDetailRecordField("discount", c.getCell(String.valueOf(this.indPrefix) + "discpercent").getString());
//            }
//            if (c.getCell(String.valueOf(this.indPrefix) + "amount").getDouble() != 0.0) {
//                this.ppj.addDetailRecordField("amount", c.getCell(String.valueOf(this.indPrefix) + "amount").getString());
//            }
    		    	}
    		    			
    		    }
        
    		    this.ppj.addBottomField("hdr_total", "總計",0,0);
    		    this.ppj.addBottomField("hdr_total", "Total Mop",0,20);
    		    this.ppj.addBottomField("val_total", invoice.getCell("stmpi_invamount").getColumnDisplayString());
        	}
        }

	}
	protected void printDoInvoice() throws Exception {
		throw new Exception("Do Invoice Not Supported");
		
	}
	protected void printQuotationnvoice() throws Exception {
		throw new Exception("Quotation Invoice Not Supported");
	}

    protected void printGeneralInvoice() throws Exception {
        ppj.setTrailerAtLastPageOnly(true);
        ppj.addPageNo("page", "%s / %s", 0, 0, 0);
        
        TableRec cotr = this.su.getQueryResult("select * from cocode where co_cocode = '" + this.col.getCellString("invh_cocode") + "'", (Wherecl)null);
        final int it = this.col.getCellInt("invh_invtype");
        switch (it) {
            case 1: {
                ppj.addHeaderField("doctitle", "DEBIT NOTE");
                break;
            }
            case 2: {
                ppj.addHeaderField("doctitle", "CREDIT NOTE");
                break;
            }
            default: {
                ppj.addHeaderField("doctitle", "發 INVOICE 票");
                break;
            }
        }
        /* 
        if (Erpv4Config.getDefaultLogo(this.sh) != null) {
            this.ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(this.sh), 0, 0, 0, 440);
        }
        */
        /*
        this.ppj.addHeaderImage("logo", "logo/hapyic/1.png",0,0,0,100);
        this.ppj.addHeaderImage("logo2", "logo/hapyic/2.png",0,0,0,100);
        this.ppj.addHeaderImage("logo3", "logo/hapyic/3.png",0,0,0,100);
        */
        String addr;
        this.ppj.addHeaderImage("logo", "logo/hapyic/1.png",0,0,70,0);
        this.ppj.addHeaderImage("logo2", "logo/hapyic/2.png",0,0,70,0);
        this.ppj.addHeaderImage("logo3", "logo/hapyic/3.png",0,0,50,0);
        
        int yofs = 0;
        addr = cotr.getFieldString("co_coaddr1") + cotr.getFieldString("co_coaddr2") +cotr.getFieldString("co_coaddr3");
        ppj.addHeaderField("coaddr", addr,0,yofs);
        addr = cotr.getFieldString("co_eaddr");
        yofs = 12;
        if(!addr.isEmpty()) {
        	ppj.addHeaderField("coaddr", addr,0,yofs);
        	yofs += 12;
        }
        String ss = "";
        addr = cotr.getFieldString("co_telnum");
        if(!addr.isEmpty()) ss += "電話/Tel:"+ ss;
        addr = cotr.getFieldString("co_faxnum");
        if(!addr.isEmpty()) ss += "傳真/Fax:"+ ss;
        addr = cotr.getFieldString("co_email");
        if(!addr.isEmpty()) ss += "電郵/Email:"+ ss;
        if(!ss.isEmpty()) {
        	ppj.addHeaderField("coaddr", ss,0,yofs);
        	yofs += 12;
        }
        
        
        ppj.addHeaderField("cvname", this.col.getCellString("vd_vname"));
        addr = String.valueOf(this.col.getCellString("invh_addr0").trim()) + " " + this.col.getCellString("invh_addr1").trim() + " " + this.col.getCellString("invh_addr2").trim() + " " + col.getCellString("invh_addr3");
        ppj.addHeaderField("cvaddr", addr);
        ppj.addHeaderField("cvphone", this.col.getCellString("invh_tel"));
        /*
        this.ppj.addHeaderField("cvname", this.col.getCellString("vd_vname"));
        final String addr = String.valueOf(this.col.getCellString("vd_addr0").trim()) + " " + this.col.getCellString("vd_addr1").trim() + " " + this.col.getCellString("vd_addr2").trim();
        this.ppj.addHeaderField("cvname", addr, 0, 20, 0, 320);
        this.ppj.addHeaderField("clphone", "Phone", 0, 0);
        this.ppj.addHeaderField("cvphone", this.col.getCellString("invh_tel"), 0, 0);
        this.ppj.addHeaderField("clphone", "Fax", 0, 18);
        this.ppj.addHeaderField("cvphone", this.col.getCellString("invh_fax"), 0, 18);
        this.ppj.addHeaderField("clphone", "Attn", 0, 36);
        this.ppj.addHeaderField("cvphone", this.col.getCellString("invh_contact"), 0, 36);
        this.ppj.addHeaderField("dflabel", "Invoice #", 0, 0);
        this.ppj.addHeaderField("dfvalue", this.col.getCellString("invh_invno"), 0, 0);
        this.ppj.addHeaderField("dflabel", "Date", 0, 30);
        this.ppj.addHeaderField("dfvalue", DateUtil.toDateString(this.col.getCell("invh_date").getDate(), "dd/mm/yyyy"), 0, 30);
        this.ppj.addHeaderField("dflabel", "Page", 0, 60);
        */
        this.ppj.addHeaderField("dfvalue", this.col.getCellString("invh_invno"), 0, 0);
        this.ppj.addHeaderField("barcode", this.col.getCellString("invh_invno"), 0, 0);
        this.ppj.addHeaderField("dfvalue", this.col.getCellString("invh_pocode"), 0, 15);
        this.ppj.addHeaderField("dfvalue", DateUtil.toDateString(this.col.getCell("invh_date").getDate(), "dd/mm/yyyy"), 0, 45);
        
        this.ppj.addDetailHeaderField("hdr_description", "貨名",0,0);
        this.ppj.addDetailHeaderField("hdr_description", "Description",0,15);
        this.ppj.addDetailHeaderField("hdr_standard", "規格",0,0);
        this.ppj.addDetailHeaderField("hdr_standard", "Standards",0,15);

        this.ppj.addDetailHeaderField("hdr_qty", "數量",0,0);
        this.ppj.addDetailHeaderField("hdr_qty", "Quantity",0,15);

        this.ppj.addDetailHeaderField("hdr_uprice", "單價",0,0);
        this.ppj.addDetailHeaderField("hdr_uprice", "Unit Price",0,15);

        this.ppj.addDetailHeaderField("hdr_disc", "折扣",0,0);
        this.ppj.addDetailHeaderField("hdr_disc", "Discount",0,15);
        this.ppj.addDetailHeaderField("hdr_amount", "金額",0,0);
        this.ppj.addDetailHeaderField("hdr_amount", "Amount",0,15);
        /*
        this.ppj.addDetailHeaderField("hdr_duedate", "Due Date");
        this.ppj.addDetailHeaderField("hdr_terms", "TERMS.");
        this.ppj.addDetailHeaderField("hdr_delivery", "DELIVERY");
        this.ppj.addDetailHeaderField("hdr_yourref", "YOUR REF.");
        this.ppj.addDetailHeaderField("hdr_ourref", "OUR REF.");
        this.ppj.addDetailHeaderField("hdr_description", "DESCRIPTION");
        this.ppj.addDetailHeaderField("hdr_qty", "QTY");
        this.ppj.addDetailHeaderField("hdr_uprice", "UNIT PRICE", 0, -5);
        this.ppj.addDetailHeaderField("hdr_uprice", "(" + this.col.getCellString("invh_cid") + ")", 0, 12);
        this.ppj.addDetailHeaderField("hdr_disc", "DISC", 0, -5);
        this.ppj.addDetailHeaderField("hdr_disc", "%", 0, 12);
        this.ppj.addDetailHeaderField("hdr_amount", "AMOUNT", 0, -5);
        this.ppj.addDetailHeaderField("hdr_amount", "(" + this.col.getCellString("invh_cid") + ")", 0, 12);
        this.ppj.addDetailHeaderField("val_yourref", this.col.getCell("invh_pocode").getColumnDisplayString());
        this.ppj.addDetailHeaderField("val_ourref", this.col.getCell("invh_jobno").getColumnDisplayString());
        this.ppj.addDetailHeaderField("val_duedate", DateUtil.toDateString(this.col.getCell("invh_duedate").getDate(), "dd/mm/yyyy"), 0, 0);
        */
        for (final BiCellCollection c : this.v) {
            this.ppj.addDetailRecord();
            final String s = makeInvoiceItemDescription(this.indPrefix, (CellCollection)c, this.ppj);
            this.ppj.addDetailRecordField("longdescription", s);
            this.ppj.setBold(false);
            if (c.getCell(String.valueOf(this.indPrefix) + "qty").getDouble() != 0.0) {
                this.ppj.addDetailRecordField("quantity", 
                		c.getCell(indPrefix + "qty").getColumnDisplayString()+
               			c.getCell( indPrefix + "unit").getString()
                		);
            }
            if (c.getCellDouble(String.valueOf(this.indPrefix) + "sprice") != 0.0 && c.getCell(String.valueOf(this.indPrefix) + "discpercent").getInt() != 0) {
                this.ppj.addDetailRecordField("price", 
                			c.getCell( indPrefix + "sprice").getColumnDisplayString()+"/"+
                			c.getCell( indPrefix + "unit").getString()
                		);
            }
            else if (c.getCell(String.valueOf(this.indPrefix) + "uprice").getDouble() != 0.0) {
                this.ppj.addDetailRecordField("price", 
                			c.getCell(indPrefix + "uprice").getColumnDisplayString()+"/"+
                			c.getCell( indPrefix + "unit").getString()
                			);
            }
            if (c.getCell(String.valueOf(this.indPrefix) + "discpercent").getInt() > 0) {
                this.ppj.addDetailRecordField("discount", (c.getCell(String.valueOf(this.indPrefix) + "discpercent").getString()+"%"));
            }
            if (c.getCell(String.valueOf(this.indPrefix) + "amount").getDouble() != 0.0) {
                this.ppj.addDetailRecordField("amount", c.getCell(String.valueOf(this.indPrefix) + "amount").getColumnDisplayString());
            }
        }
        /*
        this.ppj.addDetailRecord();
        this.ppj.addDetailRecordField("description", this.col.getCell("invh_projecttitle").getString());
        for (final BiCellCollection c : this.v) {
            this.ppj.addDetailRecord();
            final String s = makeInvoiceItemDescription(this.indPrefix, (CellCollection)c, this.ppj);
            this.ppj.addDetailRecordField("description", s);
            this.ppj.setBold(false);
            if (c.getCell(String.valueOf(this.indPrefix) + "qty").getDouble() != 0.0) {
                this.ppj.addDetailRecordField("quantity", c.getCell(String.valueOf(this.indPrefix) + "qty").getString());
            }
            if (c.getCellDouble(String.valueOf(this.indPrefix) + "sprice") != 0.0 && c.getCell(String.valueOf(this.indPrefix) + "discpercent").getInt() != 0) {
                this.ppj.addDetailRecordField("price", c.getCell(String.valueOf(this.indPrefix) + "sprice").getString());
            }
            else if (c.getCell(String.valueOf(this.indPrefix) + "uprice").getDouble() != 0.0) {
                this.ppj.addDetailRecordField("price", c.getCell(String.valueOf(this.indPrefix) + "uprice").getString());
            }
            if (c.getCell(String.valueOf(this.indPrefix) + "discpercent").getInt() > 0) {
                this.ppj.addDetailRecordField("discount", c.getCell(String.valueOf(this.indPrefix) + "discpercent").getString());
            }
            if (c.getCell(String.valueOf(this.indPrefix) + "amount").getDouble() != 0.0) {
                this.ppj.addDetailRecordField("amount", c.getCell(String.valueOf(this.indPrefix) + "amount").getString());
            }
        }
        this.ppj.addDetailRecord();
        if (!this.col.getCellString("invh_quonum").equals("")) {
            this.ppj.addDetailRecordField("description", "[Our Ref: " + this.col.getCell("invh_quonum").getString() + "]");
        }
        */
        this.ppj.addBottomField("hdr_total", "總計",0,0);
        this.ppj.addBottomField("hdr_total", "Total Mop",0,20);
        this.ppj.addBottomField("val_total", this.col.getCell("invh_total").getColumnDisplayString());
        /*
        this.ppj.addBottomField("val_signco", Erpv4Config.getCoName(this.sh, this.col.getCellString("invh_cocode")));
        this.ppj.addBottomField("hdr_total", "Invoice Total:");
        this.ppj.addBottomField("hdr_total", "Less Discount:", 0, 20);
        this.ppj.addBottomField("hdr_total", "Less Trade In:", 0, 40);
        this.ppj.addBottomField("hdr_total", "Delivery:", 0, 60);
        this.ppj.addBottomField("hdr_total", "Net Amount:", 0, 80);
        this.ppj.addBottomField("val_total", this.col.getCell("invh_total").getColumnDisplayString());
        if (this.col.getCell("invh_discount").getDouble() != 0.0) {
            this.ppj.addBottomField("val_total", this.col.getCell("invh_discount").getColumnDisplayString(), 0, 20);
        }
        if (this.col.getCell("invh_tradein").getDouble() != 0.0) {
            this.ppj.addBottomField("val_total", this.col.getCell("invh_tradein").getColumnDisplayString(), 0, 40);
        }
        if (this.col.getCell("invh_delichg").getDouble() != 0.0) {
            this.ppj.addBottomField("val_total", this.col.getCell("invh_delichg").getColumnDisplayString(), 0, 60);
        }
        this.ppj.addBottomField("val_total", this.col.getCell("invh_total").getColumnDisplayString(), 0, 80);
        String remark = null;
        if (remark == null) {
            remark = this.col.getCell("invh_remark").getString();
        } else {
            remark = String.valueOf(remark) + "\r" + this.col.getCell("invh_remark").getString();
        }
        if (!this.col.getCellString("invh_paytype").equals("")) {
            remark = String.valueOf(remark) + "\r" + this.col.getCellString("invh_paytype");
        }
        if (!this.col.getCellString("invh_term").equals("")) {
            remark = String.valueOf(remark) + "\r" + this.col.getCellString("invh_term");
        }
        */
        /*
        final TableRec tr = this.su.getQueryResult("select * from cocode where co_cocode = '" + this.col.getCellString("invh_cocode") + "'", (Wherecl)null);
        if (tr.getRecordCount() > 0) {
            tr.setRecPointer(0);
            if (!remark.trim().equals("")) {
                remark = String.valueOf(remark) + "\r";
            }
            if (this.col.getCellInt("invh_bankinfo") > 0) {
                remark = String.valueOf(remark) + tr.getFieldString("co_payment2");
            }
            else {
                remark = String.valueOf(remark) + tr.getFieldString("co_payment");
            }
        }
        this.ppj.addBottomField("val_remark", remark, 0, 0, 15, 0);
        */
    }
    protected String makeInvoiceItemDescription(final String indPrefix, final CellCollection c, final PrtdocJson ppj) {
        String s = "";
        if (c.testCell(String.valueOf(indPrefix) + "irg") != null && c.testCell(String.valueOf(indPrefix) + "irg").getInt() > 0 && c.testCell("st_iname") != null) {
            s = String.valueOf(s) + c.getCellString("st_iname");
        }
        if (c.testCell("ind_desc") != null) {
            s = String.valueOf(s) + c.getCellString("ind_desc");
        }
        return s;
    }

}
