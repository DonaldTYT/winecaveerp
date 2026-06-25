package com.uniinformation.dynamic.afs;

import java.util.Vector;

import com.google.gson.JsonObject;
import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.PrtdocMulti;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;

public class AfscnPartsDn extends PrtdocMulti {

	public AfscnPartsDn(BiResultErpv4 p_br, JsonObject p_actionData) throws Exception {
		super(p_br, p_actionData);
	}
	
	@Override 
	public void print() throws Exception {
		super.print();
    	ppj.setTrailerAtLastPageOnly(true);
    	//ppj.addPageNo("pagestr", "%s of %s",0, 0, 0);

    	String coname = "";
		SelectUtil su = br.getSelectUtil();
		TableRec tr = su.getQueryResult("select * from cocode where co_cocode = '"+coCode+ "'",null);
		if(tr.getRecordCount() > 0) {
			tr.setRecPointer(0);
			coname = tr.getFieldString("co_coname");
			ppj.addHeaderField("coname", coname);
			ppj.addHeaderField("coenname", tr.getFieldString("co_chnname"));
			ppj.addHeaderField("coaddr", tr.getFieldString("co_coaddr1"));
			ppj.addHeaderField("cotelfax", String.format("Tel: %s  Fax: %s", tr.getFieldString("co_telnum"), tr.getFieldString("co_faxnum")));
		}

  		//ppj.addHeaderImage("logo", Erpv4Config.getString(br.getSessionHelper(), "LogoImage"),0,0,0,100);
    	if (Erpv4Config.getDefaultLogo(br.getSessionHelper()) != null)
    		ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(br.getSessionHelper()), 0, 0, 100, 100);
		ppj.addHeaderField("doctitle", "送  货  单");
    	ppj.addHeaderField("ciname","客户名称：");
    	ppj.addHeaderField("ciname","用户：", 0, 20);
    	ppj.addHeaderField("ciname","收货地址：",0,40);
    	ppj.addHeaderField("ciname","联 系 人：",0,60);
    	ppj.addHeaderField("ciname","电    话：",0,80);
    	
    	ppj.addHeaderField("cicontent",br.getCellString("svloc_desp"));
   		String addr = br.getCellString("svloc_addr1").trim()+br.getCellString("svloc_city").trim() ;
    	ppj.addHeaderField("cicontent",addr,0,40);
    	ppj.addHeaderField("cicontent",br.getCellString("stm_contact"),0,60);
    	ppj.addHeaderField("cicontent",br.getCellString("stm_tel"),0,80);
    	
		ppj.addHeaderField("dflabel", "出货单号：");
		ppj.addHeaderField("dflabel", "合同号：", 0, 40);
		ppj.addHeaderField("dflabel", "日期：", 0, 80);

    	ppj.addHeaderField("dfvalue", br.getCellString("stm_ref1"));
    	ppj.addHeaderField("dfvalue", br.getCellString("inv_contract"), 0, 40);
    	ppj.addHeaderField("dfvalue", DateUtil.toDateString(br.getCellDate("stm_date"), "yyyy-mm-dd"), 0, 80);

    	ppj.addHeaderField("hdr_col0", "编号");
    	ppj.addHeaderField("hdr_col1", "商品名称");
    	ppj.addHeaderField("hdr_col2", "规格型号");
    	ppj.addHeaderField("hdr_col3", "机身编号");
    	ppj.addHeaderField("hdr_col4", "箱数");
    	ppj.addHeaderField("hdr_col5", "数量");

   		Vector<BiCellCollection> v = br.getSubLink("erpv4.DoDet").getRowCollectionList();
   		int n = 0;
		for (BiCellCollection c : v) {
			ppj.addDetailRecord();
			ppj.addDetailRecordField("det_col0", c.getCellString("inv_invno"));
			ppj.addDetailRecordField("det_col1", c.getCellString("st_iname"));
			ppj.addDetailRecordField("det_col2", c.getCellString("st_modelno"));
			ppj.addDetailRecordField("det_col3", c.getCellString("stmd_ref4"));
			ppj.addDetailRecordField("det_col5", "" + c.getCellInt("stmd_entryqty"));
			n++;
		}
		ppj.addDetailEndField("ddesp", String.format("备注: 共%d件。", n));
		ppj.addBottomField("val_coname", coname);
		ppj.addBottomField("val_perby", String.format("制单人: %s", br.getCellString("stm_cuser")));
		ppj.addBottomField("val_signlabel", "客户签收盖章:");
		ppj.addBottomField("val_signdatelabel", "日期:");
	}

}
