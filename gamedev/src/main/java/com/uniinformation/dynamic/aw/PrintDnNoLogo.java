package com.uniinformation.dynamic.aw;

import java.text.DecimalFormat;

import org.apache.commons.lang3.StringUtils;
//import org.zkoss.zsoup.helper.StringUtil;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.erpv4.BiResultStmov;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.PrintMultiDoc;
import com.uniinformation.utils.UniqueStrings;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class PrintDnNoLogo extends PrintMultiDoc {

public PrintDnNoLogo() {
	
	super(null);
}
public PrintDnNoLogo(ZkBiComposerBase p_bibase) {
	super(p_bibase);
}

@Override
protected void printOneDoc() throws Exception {
    DecimalFormat df = new DecimalFormat("#,##0");
	ppj.addHeaderField("doctitle","Delivery Note",0,0);
	ppj.addHeaderField("cvname",br.getCellString("vd_vname"),0,0);
	ppj.addHeaderField("cvaddr",br.getCellString("stm_shipagent"),0,0);
	String tstr = DateUtil.dateToDateTimeStr(br.getCellDate("stm_date"),"yyyy-MMM-dd");
	ppj.addHeaderField("clphone","Date:",0,0);
	ppj.addHeaderField("cvphone",tstr,0,0);
	ppj.addHeaderField("clphone","Attn:",0,-20);
	ppj.addHeaderField("cvphone",br.getCellString("stm_contact"),0,-20);
	ppj.addHeaderField("clphone","Tel:",0,20);
	ppj.addHeaderField("cvphone",br.getCellString("stm_tel"),0,20);
	ppj.addHeaderField("clphone","D/N No:",0,50);
	ppj.addHeaderField("cvphone",br.getCellString("stm_ref1"),20,50);
	ppj.addHeaderField("barcode",String.format("https://www.erpv4.com/bctag?agent=aw&dnno=%s", br.getCellString("stm_ref1"))
			,0,0);
	UniqueStrings ustr = new UniqueStrings(",");
	for(BiCellCollection bc : br.getSubLink(((BiResultStmov) br).getStmdLinkName()).getRowCollectionList()) {
		ustr.add(bc.getCellString("inv_invno"));
	}
	ppj.addHeaderField("clphone","Quotation:",0,70);
	ppj.addHeaderField("cvphone",ustr.toString(),20,70);
	UniqueStrings ustr2 = new UniqueStrings(",");
	for(BiCellCollection bc : br.getSubLink(((BiResultStmov) br).getStmdLinkName()).getRowCollectionList()) {
		ustr2.add(bc.getCellString("inv_pocode"));
	}
	ppj.addHeaderField("clphone","P.O.:",0,90);
	ppj.addHeaderField("cvphone",ustr2.toString(),20,90);

	ppj.addDetailHeaderField("hdr_description", "Item");
//	ppj.addDetailHeaderField("hdr_detail", "Description");
	ppj.addDetailHeaderField("hdr_qty", "Quantity");
	int lastIndrg = 0;
	int itemno = 1;
	for(BiCellCollection bc : br.getSubLink(((BiResultStmov) br).getStmdLinkName()).getRowCollectionList()) {
		int curIndrg = bc.getCellInt("ind_rg");
		String f2 = bc.getCellString("stmd_flag2");
		
		boolean title=false,product=false,description=false;
		if(!(f2.equals("1") || f2.equals("3"))) {
			if(curIndrg != lastIndrg) {
				String ss = bc.getCellString("inv_projecttitle");
				if(!StringUtils.isBlank(ss)) {
					title = true;
					lastIndrg = curIndrg;
				}
			}
		}
		if(!(f2.equals("2") || f2.equals("3"))) {
			String ss = bc.getCellString("ind_desc");
			if(!StringUtils.isBlank(ss)) {
				product = true;
			}
		} 
		{
			String ss = bc.getCellString("stmd_remark");
			if(!StringUtils.isBlank(ss)) {
				description = true;
			}
		}
		int messRg = bc.getCellInt("stmd_serialno");
		for(int n = 0;n < 2;n++ ) {
			ppj.addDetailRecord();
			if(title && n == 0) {
				ppj.addDetailRecordField("description", bc.getCellString("inv_projecttitle"));
			}
			if(product && ((!title && n == 0) || (title && n == 1))) {
				ppj.addDetailRecordField("description", bc.getCellString("ind_desc"));
			}
			if(description && n == 0) {
				ppj.addDetailRecordField("detail", bc.getCellString("stmd_remark"));
			}
			if(messRg > 0 && ((!description && n == 0) || (description && n == 1))) {
				String filingTable = Erpv4Config.getString(sh, "FilingAttachmentTable");
				String key = String.format("getimage://FilingDoDetPic_%08d", messRg);
				double aspect = bc.getCellDouble("stmd_fref2");
				if(aspect <= 0.0f) aspect = 1.0f;
				double DESIRE_WIDTH = 200f;
				double MAX_HEIGHT = 300f;
				double h,w;
				if(aspect < 0.5) {
					h = MAX_HEIGHT;
					w = h * aspect;
				} else {
					w = DESIRE_WIDTH;
					h = w / aspect;
				}
				int ih = (int) h;
				int iw = (int) w;
				if(StringUtils.isBlank(filingTable)) {
					ppj.addDetailRecordImage("detail",key,0,0,ih,iw);
				} else {
					ppj.addDetailRecordImage("detail",key+"@"+filingTable,0,0,ih,iw);
				}
			}
			if(n == ((title && product) ? 1 : 0)) {
				ppj.addDetailRecordField("item", ""+itemno);
				ppj.addDetailRecordField("qty", String.format("%s %s", 
					df.format(bc.getCellDouble("stmd_entryqty")),
					bc.getCellString("stmd_entryunit")
					));
				
			}
			if((title & product) || (description && messRg > 0)) {
			} else {
				break;
			}
		}
		itemno++;
		
		/*
		if(!(f2.equals("1") || f2.equals("3"))) {
		if(curIndrg != lastIndrg) {
			String ss = bc.getCellString("inv_projecttitle");
			if(!StringUtil.isBlank(ss)) {
				ppj.addDetailRecord();
				ppj.addDetailRecordField("description", bc.getCellString("inv_projecttitle"));
			}
			lastIndrg = curIndrg;
		}
		}
		ppj.addDetailRecord();
		if(!(f2.equals("2") || f2.equals("3"))) {
			String ss = bc.getCellString("ind_desc");
			if(!StringUtils.isBlank(ss)) {
				ppj.addDetailRecordField("description", ss);
			}
		}
		ppj.addDetailRecordField("detail", bc.getCellString("stmd_remark"));
		ppj.addDetailRecordField("qty", String.format("%s %s", 
					df.format(bc.getCellDouble("stmd_entryqty")),
					bc.getCellString("stmd_entryunit")
					));
		int messRg = bc.getCellInt("stmd_serialno");
		if(messRg > 0) {
			String filingTable = Erpv4Config.getString(sh, "FilingAttachmentTable");
			ppj.addDetailRecord();
			String key = String.format("getimage://FilingDoDetPic_%08d", messRg);
			double aspect = bc.getCellDouble("stmd_fref2");
			if(aspect <= 0.0f) aspect = 1.0f;
			double DESIRE_WIDTH = 200f;
			double MAX_HEIGHT = 300f;
			double h,w;
			if(aspect < 0.5) {
				h = MAX_HEIGHT;
				w = h * aspect;
			} else {
				w = DESIRE_WIDTH;
				h = w / aspect;
			}
			int ih = (int) h;
			int iw = (int) w;
			if(StringUtils.isBlank(filingTable)) {
				ppj.addDetailRecordImage("detail",key,0,0,ih,iw);
			} else {
				ppj.addDetailRecordImage("detail",key+"@"+filingTable,0,0,ih,iw);
			}
		}
		*/
	}
				
	ppj.addBottomField("val_remark",br.getCellString("stm_remark"),0,0);
	ppj.addBottomField("val_signco",br.getCellString("sm_name"),0,20);
	ppj.addBottomField("val_signco",br.getCellString("sm_mobile"),0,40);
	
	super.printOneDoc();
	
}
	
@Override
protected ReturnMsg initPrtdoc() {
	docCode = "AWDN01";
	return(super.initPrtdoc());
}
	@Override
	public boolean isDisabled(BiResult p_br,boolean p_isBatch) {
		if(p_br == null) return(true);
		if(p_isBatch) {
			return(false);
		} else {
			String qs = p_br.getCellString("stm_status");
			if(!qs.equals("Confirmed")) return(true);
			if(p_br.inBeginWork()) return(true);
			return(false);
		}
	}
	@Override
	protected String getDocumentName(BiResult p_br) {
		// TODO Auto-generated method stub
		return ("Delivery Note");
	}
	
}
