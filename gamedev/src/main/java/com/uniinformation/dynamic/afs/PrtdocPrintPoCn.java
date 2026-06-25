package com.uniinformation.dynamic.afs;

import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.erpv4.BiResultPO;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;

public class PrtdocPrintPoCn extends PrtdocClass {
	BiResultPO br;
    PrtdocJson ppj;
    String cocode;

	public PrtdocPrintPoCn(BiResultPO p_br) throws Exception {
		br = p_br;
		cocode = Erpv4Config.getDefaultCoCode(br.getSessionHelper());
    	ppj = PrtdocJson.newPrtdocJson(	
   			cocode,
 			"A4P",
  			"AFSPOPT01",
   			"erpv4_printDocument",
   			PrtdocJson.Encoding.UTF8
    	);
	}

	@Override
	public void print() throws Exception {
    	ppj.setTrailerAtLastPageOnly(true);
    	String coname = "";
		SelectUtil su = br.getSelectUtil();
		TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+cocode+ "'",null);
		if(tr.getRecordCount() > 0) {
			tr.setRecPointer(0);
			coname = tr.getFieldString("co_coname");
			ppj.addHeaderField("coname", coname);
			ppj.addHeaderField("coenname", tr.getFieldString("co_chnname"));
			ppj.addHeaderField("coaddr", tr.getFieldString("co_coaddr1"));
			ppj.addHeaderField("cotelfax", String.format("Tel: %s  Fax: %s", tr.getFieldString("co_telnum"), tr.getFieldString("co_faxnum")));
		}
    	if (Erpv4Config.getDefaultLogo(br.getSessionHelper()) != null)
    		ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(br.getSessionHelper()), 0, 0, 100, 100);
		ppj.addHeaderField("doctitle", "Purchase Order");

    	ppj.addHeaderField("ciname", "To   :");
    	ppj.addHeaderField("ciname", "Attn :", 0, 20);
    	ppj.addHeaderField("ciname", "Tel  :", 0, 40);
    	ppj.addHeaderField("ciname", "Fax :", 0, 60);
    	ppj.addHeaderField("cicontent", br.getCellString("vd_vname"));
    	ppj.addHeaderField("dflabel", "PO No. :");
    	ppj.addHeaderField("dflabel", "Date     :", 0, 20);
    	ppj.addHeaderField("dfvalue", br.getCellString("stm_ref1"));
    	ppj.addHeaderField("dfvalue", DateUtil.toDateString(br.getCellDate("stm_date"), "yyyy/mm/dd"), 0, 20);

    	ppj.addHeaderField("hdr_col0", "Item");
    	ppj.addHeaderField("hdr_col1", "Product");
    	ppj.addHeaderField("hdr_col2", "Model");
    	ppj.addHeaderField("hdr_col3", "Qty");
    	ppj.addHeaderField("hdr_col4", "Contract No");
    	ppj.addHeaderField("hdr_col5", "End User");

   		Vector<BiCellCollection> v = br.getSubLink("erpv4.PoDet").getRowCollectionList();
   		int n = 0;
		for (BiCellCollection c : v) {
			ppj.addDetailRecord();
			ppj.addDetailRecordField("det_col0", "" + (n + 1));
			ppj.addDetailRecordField("det_col1", c.getCellString("st_iname"));
			ppj.addDetailRecordField("det_col2", c.getCellString("st_modelno"));
			ppj.addDetailRecordField("det_col3", "" + c.getCellInt("stmd_qty"));
			n++;
		}
		
		ppj.addBottomField("bdesp", "Remark:");
		ppj.addBottomField("bdesp", "1. 交货日期：", 0, 20);
		ppj.addBottomField("bdesp", "2. 交货地点：", 0, 40);
		ppj.addBottomField("bdesp", "买方确认签章：", 10, 60);
		ppj.addBottomField("bdesp", "卖方确认签章：", 500, 60);
	}

	@Override
	public PrtdocJson getPrintDocJson() throws Exception {
		return ppj;
	}
}
