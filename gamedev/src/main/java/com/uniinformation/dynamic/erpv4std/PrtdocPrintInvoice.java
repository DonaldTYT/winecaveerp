package com.uniinformation.dynamic.erpv4std;

import java.util.Date;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;

public class PrtdocPrintInvoice extends PrtdocClass  {
	BiResultQuotation br;
    PrtdocJson ppj;
    int docCnt;
    String cocode;
	public PrtdocPrintInvoice (BiResultQuotation p_br) throws Exception {
    	String dnDoc = "HYINV01";
    	cocode = Erpv4Config.getDefaultCoCode(p_br.getSessionHelper());
    	ppj = PrtdocJson.newPrtdocJson(	
    				cocode,
    				"A4P",
    			    dnDoc,
    			    "erpv4_printDocument",
    			    PrtdocJson.Encoding.UTF8
    	);
		br = p_br;
		docCnt = 0;
//    	ppj.addHeaderField("doctitle","Quotation");
//  	ppj.addHeaderImage("logo", Erpv4Config.getString(br.getSessionHelper(), "QuoBgImage"),0,0,0,800);
	}

	@Override
	public void print() throws Exception {
		if(docCnt > 0) {
        	if(docCnt > 0) ppj.newContent();
		}
        docCnt++;
    	ppj.setTrailerAtLastPageOnly(true);
    	ppj.addPageNo("page", "%s of %s",0, 0, 0);
    	/*
    	ppj.addHeaderImage("logo", "logo/hapyic/1.png",0,0,70,0);
        ppj.addHeaderImage("logo2", "logo/hapyic/2.png",0,0,70,0);
		ppj.addHeaderImage("logo3", "logo/hapyic/3.png",0,0,50,0);
		*/
		String tstr = DateUtil.dateToDateTimeStr(new Date());
				
        ppj.addHeaderField("page", "列印時間: "+tstr, 100,0);
        ppj.addHeaderField("page", "星期天休息",300,0);
		String addr;
        TableRec cotr = br.getSelectUtil().getQueryResult("select * from cocode where co_cocode = '" + cocode + "'", (Wherecl)null);
        cotr.setRecPointer(0);
        int yofs = 0;
        addr = cotr.getFieldString("co_coaddr1") + cotr.getFieldString("co_coaddr2") +cotr.getFieldString("co_coaddr3");
        ppj.addHeaderField("coaddr", addr,0,yofs);
        yofs += 15;

        /*
        addr = cotr.getFieldString("co_eaddr");
        if(!addr.isEmpty()) {
        	ppj.addHeaderField("coaddr", addr,0,yofs);
        	yofs += 15;
        }
        */
        String ss = "";
        addr = cotr.getFieldString("co_telnum");
        if(!addr.isEmpty()) ss += "電話/Tel:"+ addr + " ";
        addr = cotr.getFieldString("co_faxnum");
        if(!addr.isEmpty()) ss += "傳真/Fax:"+ addr + " ";
        addr = cotr.getFieldString("co_email");
        if(!addr.isEmpty()) ss += "電郵/Email:"+ addr + " ";
        if(!ss.isEmpty()) {
        	ppj.addHeaderField("coaddr", ss,0,yofs);
        	yofs += 15;
        }
        /*
		PrtdocJson.addMultiHeaderField(ppj, "ciname", 0, 0, 20, 0, 
				new VectorUtil()
				.addElement(br.getCellString("vd_vname"))
				.addElement(br.getCellString("inv_addr0"))
				.toVector()
				);
				*/

    	ppj.addHeaderField("cvname", br.getCellString("vd_vname"));
    	addr = br.getCellString("inv_addr0").trim() + " " + br.getCellString("inv_addr1").trim() + " " + br.getCellString("inv_addr2").trim();
    	ppj.addHeaderField("cvaddr", addr,0,0,-15,0);
    	ppj.addHeaderField("cvphone", br.getCellString("inv_tel"));
    	ppj.addHeaderField("barcode", br.getCellString("inv_invno"), 0, 0);
		PrtdocJson.addMultiHeaderField(ppj, "dfvalue", 0, 0, 15, 0, 
				new VectorUtil()
				.addElement(br.getCellString("inv_invno"))
				.addElement(br.getCellString("inv_pocode"))
				.addElement(br.getCellString("sm_name"))
				.addElement(DateUtil.toDateString(br.getCellDate("inv_date"),"dd-mm-yyyy"))
				.addElement(DateUtil.toDateString(br.getCellDate("inv_delidate"),"dd-mm-yyyy"))
				.toVector()
				);

    	ppj.addDetailHeaderField("hdr_description", "貨名",0,0);
    	ppj.addDetailHeaderField("hdr_description", "Description",0,15);
    	ppj.addDetailHeaderField("hdr_standard", "規格",0,0);
    	ppj.addDetailHeaderField("hdr_standard", "Standards",0,15);

    	ppj.addDetailHeaderField("hdr_qty", "數量",0,0);
    	ppj.addDetailHeaderField("hdr_qty", "Quantity",0,15);

    	ppj.addDetailHeaderField("hdr_uprice", "單價",0,0);
    	ppj.addDetailHeaderField("hdr_uprice", "Unit Price",0,15);

    	ppj.addDetailHeaderField("hdr_disc", "折扣",0,0);
    	ppj.addDetailHeaderField("hdr_disc", "Discount",0,15);
    	ppj.addDetailHeaderField("hdr_amount", "金額",0,0);
    	ppj.addDetailHeaderField("hdr_amount", "Amount",0,15);
    	
   		Vector<BiCellCollection> v = null;
		v = br.getSubLink(br.get_subLinkId()).getRowCollectionList();
		String indPrefix = "ind_";
		int n = 0;

		for(BiCellCollection c : v) {
			ppj.addDetailRecord();
			String s = "";
			n++;
			ppj.addDetailRecordField("itemcode", c.getCell("st_icode").getString());
			ppj.addDetailRecordField("description", c.getCell(indPrefix+"desc").getString());
            if (c.getCell(indPrefix+"qty").getDouble() != 0.0) {
            		ppj.addDetailRecordField("quantity", 
            			c.getCell(indPrefix+"qty").getColumnDisplayString() +
            			c.getCell(indPrefix+"unit").getColumnDisplayString()
            		);
            		double eq = c.getCell(indPrefix+"qty").getDouble();
            		double uq = c.getCell(indPrefix+"stqty").getDouble();
            		if(eq != uq) {
            			ppj.addDetailRecordField("standard", 
            				"每" +
            				c.getCell(indPrefix+"unit").getString() +
            				((int)(uq/eq))+
            				c.getCell("st_unit").getString() 
            			);
            		}
            }
			ppj.addDetailRecordField("price", c.getCell(indPrefix+"uprice").getColumnDisplayString()+"/"+c.getCell(indPrefix+"unit").getString() , 0, 0);
            if (c.getCell("ind_discpercent").getInt() != 0) {
            	ppj.addDetailRecordField("discount", c.getCell("ind_discpercent").getColumnDisplayString()+"%");
            } else if (c.getCell("ind_discount").getDouble() != 0.0) {
            	ppj.addDetailRecordField("discount", c.getCell("ind_discount").getColumnDisplayString());
            }
			ppj.addDetailRecordField("amount", c.getCell(indPrefix+"amount").getString(), 0, 0);
		}
    	ppj.addBottomField("hdr_total", "總計",0,0);
    	ppj.addBottomField("hdr_total", "Total "+br.getCellString("inv_cid"),0,20);
    	ppj.addBottomField("val_total", br.getCell("inv_total").getColumnDisplayString());
	}

	@Override
	public PrtdocJson getPrintDocJson() throws Exception {
		// TODO Auto-generated method stub
		return ppj;
	}

}
