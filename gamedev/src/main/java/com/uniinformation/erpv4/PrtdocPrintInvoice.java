// 
// Decompiled by Procyon v0.5.36
// 

package com.uniinformation.erpv4;

import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultDO;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.bicore.erpv4.BiResultInvoice;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.utils.SelectUtil;
import java.util.Vector;

import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.prtdoc.PrtdocClass;

public class PrtdocPrintInvoice extends PrtdocClass
{
	public enum INVOICETYPE { INVOICETYPE_GENERAL, INVOICETYPE_QUOTATION, INVOICETYPE_DELIVERY,INVOICETYPE_MULTIDELIVERY};
    protected SessionHelper sh;
    protected PrtdocJson ppj;
    protected BiCellCollection col;
    protected String indPrefix;
    protected Vector<BiCellCollection> v;
    protected SelectUtil su;
    protected INVOICETYPE invoiceType;
    protected JSONObject option;
    public PrtdocPrintInvoice(final BiResultErpv4 p_br, final PrtdocJson p_ppj, JSONObject p_option) {
        this.sh = p_br.getSessionHelper();
        this.ppj = p_ppj;
        this.col = p_br.getCurrentCollection();
        this.su = p_br.getSelectUtil();
        this.option = p_option;
        if(p_br instanceof BiResultInvoice) {
        	String indLinkName = ((BiResultInvoice) p_br).getIndLinkName();
        	if (indLinkName != null) {
        	    final BiResult ssr = p_br.getSubLink(indLinkName);
        	    this.v = (Vector<BiCellCollection>)ssr.getRowCollectionList();
        	    this.indPrefix = "invd_";
        	    invoiceType = INVOICETYPE.INVOICETYPE_GENERAL;
        	} else if( p_br.getParent() != null){
        		if(p_br.getParent() instanceof BiResultQuotation) {
        			BiResultQuotation ppr = null;
    			  	ppr = (BiResultQuotation) p_br.getParent();
        		  	BiResult ssr = ppr.getSubLink(ppr.get_subLinkId());
       			  	v = ssr.getRowCollectionList();
        		  	indPrefix="ind_";
        		  	invoiceType = INVOICETYPE.INVOICETYPE_QUOTATION;
        		}
        	} 
        }
        if(p_br instanceof BiResultDO) {
        	if(p_br.getSubLink("erpv4.StmPostInv") != null) {
        		invoiceType = INVOICETYPE.INVOICETYPE_MULTIDELIVERY;
        	} else {
        		invoiceType = INVOICETYPE.INVOICETYPE_DELIVERY;
        	}
    		v = p_br.getSubLink(((BiResultDO) p_br).getStmdLinkName() ).getRowCollectionList();
        }
    }
    
    protected void printMultiDoInvoice() throws Exception {
    	throw new Exception ("Multi Delivery Invoice Not Supported");
    }
    
    protected void printDoInvoice() throws Exception {
    		ppj.setTrailerAtLastPageOnly(true);
    		ppj.addPageNo("dfvalue", "%s of %s",0, 60, 0);
    		ppj.addHeaderField("doctitle","INVOICE");
    		if(Erpv4Config.isMultiCompany(sh)) {
    			String logo = Erpv4Config.getCoLogo(sh, col.getCellString("stm_cocode"));
    			if(logo != null && !logo.equals("")) {
    				ppj.addHeaderImage("logo", logo ,50,0,100,0);
    			}
				if(Erpv4Config.getString(sh, "CustomSmartac") != null) {
					ppj.setBold(true);
					ppj.addHeaderField("logo",Erpv4Config.getCoName(sh, col.getCellString("stm_cocode")),200,10);
					ppj.addHeaderField("logo",Erpv4Config.getCoAddr(sh, col.getCellString("stm_cocode")),200,40);
					ppj.setBold(false);
//					if(col.getCellString("stm_cocode").equals("005")) {
//						ppj.addHeaderField("logo","FPS轉數快  ID: 161693544" ,540,920);
//						ppj.addHeaderImage("logo", "logo/smartac_barcode_01.jpg" ,600,950,120,0);
//					}
				}
    		} else {
    			if(Erpv4Config.getDefaultLogo(sh) != null) {
    				ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(sh) ,50,0,100,0);
    			}
    		}
    		ppj.addHeaderField("cvname",col.getCellString("vd_vname"));
    		/*
    		ppj.addHeaderField("cvname",col.getCellString("vd_addr0"),0,20);
    		ppj.addHeaderField("cvname",col.getCellString("vd_addr1"),0,40);
    		ppj.addHeaderField("cvname",col.getCellString("vd_addr2"),0,60);
    		*/

    		String addr = col.getCellString("vd_addr0").trim() + " " +  col.getCellString("vd_addr1").trim() + " " + col.getCellString("vd_addr2").trim() + col.getCellString("vd_addr3").trim();
    		ppj.addHeaderField("cvname",addr,0,20,0,320);
    		if(false) {
    			ppj.addHeaderField("clphone","Phone",0,0);
    			ppj.addHeaderField("cvphone",col.getCellString("vd_tel"),0,0);
    			ppj.addHeaderField("clphone","Fax",0,18);
    			ppj.addHeaderField("cvphone",col.getCellString("vd_fax"),0,18);
    			ppj.addHeaderField("clphone","Attn",0,36);
    			ppj.addHeaderField("cvphone",col.getCellString("vd_contact"),0,36);
    		} else {
    			ppj.addHeaderField("clphone","Attn",0,0);
    			ppj.addHeaderField("cvphone",col.getCellString("stm_contact"),0,0);
    			ppj.addHeaderField("clphone","Tel",0,18);
    			ppj.addHeaderField("cvphone",col.getCellString("stm_tel"),0,18);
    		}
    		ppj.addHeaderField("dflabel","Invoice #",0,0);
    		ppj.addHeaderField("dfvalue",col.getCellString("stm_ref3"),0,0);
    		ppj.addHeaderField("dflabel","Date",0,30);
    		ppj.addHeaderField("dfvalue", " "+DateUtil.toDateString( col.getCell("stm_date").getDate(),"yyyy/mm/dd") ,0,30);
    		ppj.addHeaderField("dflabel","Page",0,60);
 
    		ppj.addDetailHeaderField("hdr_duedate","Due Date");
    		ppj.addDetailHeaderField("hdr_terms","TERMS.");
    		ppj.addDetailHeaderField("hdr_delivery","DELIVERY");
    		ppj.addDetailHeaderField("hdr_yourref","YOUR REF.");
    		ppj.addDetailHeaderField("hdr_ourref","OUR REF.");
    		ppj.addDetailHeaderField("hdr_description","DESCRIPTION");
    		ppj.addDetailHeaderField("hdr_qty","QTY");
    		ppj.addDetailHeaderField("hdr_uprice","UNIT PRICE",0,-5);
    		ppj.addDetailHeaderField("hdr_uprice","("+ col.getCellString("stm_cur")+")",0,12);
   			if(col.getCellString("stm_module").equals("machine")) {
   				ppj.addDetailHeaderField("hdr_disc","DEPO",0,-5);
   			} else {
   				ppj.addDetailHeaderField("hdr_disc","DISC",0,-5);
   			}
    		ppj.addDetailHeaderField("hdr_disc","%",0,12);
    		ppj.addDetailHeaderField("hdr_amount","AMOUNT",0,-5);
    		ppj.addDetailHeaderField("hdr_amount","("+ col.getCellString("stm_cur")+")",0,12);


//    		ppj.addDetailHeaderField("val_yourref",getCell("invh_pocode").getColumnDisplayString());
    		if(col.testCell("sih_duedate") != null ) {
    			ppj.addDetailHeaderField("val_duedate"," "+DateUtil.toDateString( col.getCell("sih_duedate").getDate(),"yyyy/mm/dd") ,0,0);
    		}
//    		ppj.addDetailHeaderField("val_duedate",DateUtil.toDateString( getCell("invh_duedate").getDate(),"dd/mm/yyyy") ,0,0);

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
    			if(col.getCellString("stm_module").equals("machine")) {
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
    		if(!col.getCellString("stm_ref1").equals(""))
    			ppj.addDetailRecordField("description", "[Our Ref: " + col.getCell("stm_ref1").getString() + "]");
    		ppj.addBottomField("val_signco",Erpv4Config.getCoName(sh,col.getCellString("stm_cocode")));


    		int ofs = 0;
    		
    		if(col.testCell("stm_grosstotal") != null) {
    			ppj.addBottomField("hdr_total","Gross Total:",0,ofs);
    			ppj.addBottomField("val_total",col.getCell("stm_grosstotal").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    		}
    		if(col.getCellDouble("stm_discount") != 0) {
    			ppj.addBottomField("hdr_total","Less Discount:",0,ofs);
    			ppj.addBottomField("val_total",col.getCell("stm_discount").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    		}

    		if(col.getCellDouble("stm_fref1") != 0) {
    			ppj.addBottomField("hdr_total", getServiceName(col.getCellString("stm_salescode1"))+":" ,0,ofs);
    			ppj.addBottomField("val_total",col.getCell("stm_fref1").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    		}
    		if(col.getCellDouble("stm_fref2") != 0) {
    			ppj.addBottomField("hdr_total", getServiceName(col.getCellString("stm_salescode2"))+":" ,0,ofs);
    			ppj.addBottomField("val_total",col.getCell("stm_fref2").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    		}
    		ppj.addBottomField("hdr_total","Net Total:",0,ofs);
    		ppj.addBottomField("val_total",col.getCell("stm_nettotal").getColumnDisplayString(),0,ofs);
   			ofs += 20;
    		if(col.getCellDouble("stm_deposit") != 0) {
    			ppj.addBottomField("hdr_total","Less Deposit:",0,ofs);
    			ppj.addBottomField("val_total",col.getCell("stm_deposit").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    			ppj.addBottomField("hdr_total","Invoice Amount:",0,ofs);
    			ppj.addBottomField("val_total",col.getCell("stm_invamount").getColumnDisplayString(),0,ofs);
    			ofs += 20;
    		}
   			String remark = null; 
			if(remark == null) remark = col.getCell("stm_shipagent").getString(); else remark += "\r" + col.getCell("stm_shipagent").getString();
			if(!col.getCellString("stm_shipname").equals("")) {
				remark += "\r"+ col.getCellString("stm_shipname");
			}
			if(!col.getCellString("invh_term").equals("")) {
				remark += "\r"+ col.getCellString("invh_term");
			}

				TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+col.getCellString("stm_cocode")+ "'",null);
				if(tr.getRecordCount() > 0) {
					tr.setRecPointer(0);
					if(!remark.trim().equals("")) remark += "\r";
					if(col.getCellString("stm_module").equals("parts")) 
						remark += tr.getFieldString("co_payment2");
					else 
						remark += tr.getFieldString("co_payment");
				}
   			ppj.addBottomField("val_remark",remark,0,0,15,0);
					if(col.getCellString("stm_cocode").equals("005")) {
						ppj.addBottomField("logo","FPS轉數快  ID: 161693544" ,540,920);
						ppj.addBottomImage("logo", "logo/smartac_barcode_01.jpg" ,600,950,120,0);
					}
    }
    
    protected void printGeneralInvoice() throws Exception {
    	printGeneralAndQuotationInvoice();
    }
    protected void printQuotationInvoice() throws Exception {
    	printGeneralAndQuotationInvoice();
    }
    public void print() throws Exception {
    	switch(invoiceType) {
    	case INVOICETYPE_GENERAL:
    		printGeneralInvoice();
    		break;
    	case INVOICETYPE_QUOTATION:
    		printQuotationInvoice();
    		break;
    	case INVOICETYPE_DELIVERY:
    		printDoInvoice();
    		break;
    	case INVOICETYPE_MULTIDELIVERY:
    		printMultiDoInvoice();
    		break;
    	default:
    		throw new Exception("Invoice Type Not Identified");
    	}
    }	
    
    protected void printGeneralAndQuotationInvoice() throws Exception
    {
        this.ppj.setTrailerAtLastPageOnly(true);
        this.ppj.addPageNo("dfvalue", "%s of %s", 0, 60, 0);
        final int it = this.col.getCellInt("invh_invtype");
        switch (it) {
            case 1: {
                this.ppj.addHeaderField("doctitle", "DEBIT NOTE");
                break;
            }
            case 2: {
                this.ppj.addHeaderField("doctitle", "CREDIT NOTE");
                break;
            }
            default: {
                this.ppj.addHeaderField("doctitle", "INVOICE");
                break;
            }
        }
        if (Erpv4Config.getDefaultLogo(this.sh) != null) {
            this.ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(this.sh), 0, 0, 0, 440);
        }
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
        this.ppj.addHeaderField("dfvalue", " "+DateUtil.toDateString(this.col.getCell("invh_date").getDate(), "yyyy/mm/dd"), 0, 30);
        this.ppj.addHeaderField("dflabel", "Page", 0, 60);
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
        this.ppj.addDetailHeaderField("val_duedate", " "+DateUtil.toDateString(this.col.getCell("invh_duedate").getDate(), "yyyy/mm/dd"), 0, 0);
        this.ppj.addDetailRecord();
        this.ppj.addDetailRecordField("description", this.col.getCell("invh_projecttitle").getString());
        for (final BiCellCollection c : this.v) {
            this.ppj.addDetailRecord();
            final String s = this.makeInvoiceItemDescription(this.indPrefix, (CellCollection)c, this.ppj);
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
    }
    
    protected String makeInvoiceItemDescription(final String indPrefix, final CellCollection c, final PrtdocJson ppj) {
        String s = "";
        if (c.testCell(String.valueOf(indPrefix) + "irg") != null && c.testCell(String.valueOf(indPrefix) + "irg").getInt() > 0 && c.testCell("st_iname") != null) {
            s = String.valueOf(s) + c.getCellString("st_iname");
        }
        if (c.testCell(String.valueOf(indPrefix) + "desc") != null) {
            s = String.valueOf(s) + c.getCellString(String.valueOf(indPrefix) + "desc");
        }
        return s;
    }

	protected String getServiceName(String p_accode) {
		try {
			TableRec tr = su.getQueryResult("select * from prdsrvmaster where pds_ano = '"+ p_accode + "'");
			if(tr.getRecordCount() > 0) {
				tr.setRecPointer(0);
				return(tr.getFieldString("pds_desc"));
			} else return(p_accode);
		} catch (Exception ex) {
			UniLog.log(ex);
		}
		return("");
	}

	@Override
	public PrtdocJson getPrintDocJson() throws Exception {
		return ppj;
	}
}