package com.uniinformation.dynamic.afs;

import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.JsonObject;
import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.erpv4.BiResultErpv4;
import com.uniinformation.bicore.erpv4.BiResultQuotation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.erpv4.PrtdocMulti;
import com.uniinformation.prtdoc.PrtdocClass;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;

public class PrtdocPrintContractCn extends PrtdocClass {
	BiResultQuotation br;
    PrtdocJson ppj;
    String cocode;

	public PrtdocPrintContractCn(BiResultQuotation p_br) throws Exception {
		br = p_br;
		cocode = Erpv4Config.getDefaultCoCode(br.getSessionHelper());
    	ppj = PrtdocJson.newPrtdocJson(	
   			cocode,
 			"A4P",
  			"AFSQUOPT01",
   			"erpv4_printDocument",
   			PrtdocJson.Encoding.UTF8
    	);
	}

	@Override
	public void print() throws Exception {
    	ppj.setTrailerAtLastPageOnly(true);
    	ppj.addPageNo("pagestr", "%s / %s", 0, 60, 0);

    	switch (br.getQuotationType()) {
    	case "AQM":
    		UniLog.log("print Contract AQM");
    		break;
    	case "AQP":
    		UniLog.log("print Contract AQP");
    		break;
    	}

    	ppj.addHeaderField("htitle", "AFS—产品报价单");
    	if (Erpv4Config.getDefaultLogo(br.getSessionHelper()) != null)
    		ppj.addHeaderImage("logo", Erpv4Config.getDefaultLogo(br.getSessionHelper()), 0, 0, 50, 0);

   		ppj.useDetailGroup("detailrowh");
   		ppj.addDetailRecord();
    	ppj.addDetailRecordField("doctitle", "报  价  单");
    	ppj.addDetailRecordField("ciname", "客户名称：");
    	ppj.addDetailRecordField("ciname", "地址：", 0, 20);
    	ppj.addDetailRecordField("ciname", "收件人：", 0, 40);
    	ppj.addDetailRecordField("ciname", "电话：", 0, 60);
    	ppj.addDetailRecordField("ciname", "传真：", 0, 80);
    	ppj.addDetailRecordField("ciname", "邮箱：", 0, 100);
    	ppj.addDetailRecordField("cicontent1", br.getCellString("svloc_desp"));
    	ppj.addDetailRecordField("cicontent", br.getCellString("inv_addr0"), 0, 20);
    	ppj.addDetailRecordField("cicontent", br.getCellString("inv_contact"), 0, 40);
    	ppj.addDetailRecordField("cicontent", br.getCellString("inv_tel"), 0, 60);
    	ppj.addDetailRecordField("cicontent", br.getCellString("inv_fax"), 0, 80);
    	ppj.addDetailRecordField("cicontent", br.getCellString("inv_email"), 0, 100);

    	ppj.addDetailRecordField("dflabel", "日期：");
    	ppj.addDetailRecordField("dflabel", "单号：", 0, 20);
    	ppj.addDetailRecordField("dfvalue", DateUtil.toDateString(br.getCellDate("inv_date"), "yyyy-mm-dd"));
    	ppj.addDetailRecordField("dfvalue", br.getCellString("inv_invno"), 0, 20);
    	ppj.addDetailRecordField("ciname", "承蒙贵公司的垂询，不胜感激， 我们很荣幸提供以下设备报价：", 0, 170);

   		ppj.useDetailGroup("detailrowi");
   		ppj.addDetailRecord();
    	ppj.addDetailRecordField("hdr_col0", "项次");
    	ppj.addDetailRecordField("hdr_col1", "产品名称");
    	ppj.addDetailRecordField("hdr_col2", "规格型号");
    	ppj.addDetailRecordField("hdr_col3", "品牌");
    	ppj.addDetailRecordField("hdr_col4", "数量");
    	ppj.addDetailRecordField("hdr_col5", String.format("优惠单价(%s)", br.getCellString("inv_cid")));
    	ppj.addDetailRecordField("hdr_col6", String.format("总金额(%s)", br.getCellString("inv_cid")));

   		ppj.useDetailGroup("");
   		Vector<BiCellCollection> v = br.getSubLink("erpv4.QuoDetG2").getRowCollectionList();
   		int n = 0;
		for (BiCellCollection c : v) {
			ppj.addDetailRecord();
			ppj.addDetailRecordField("det_col0", "" + (n + 1));
			ppj.addDetailRecordField("det_col1", c.getCellString("st_iname"));
			ppj.addDetailRecordField("det_col2", c.getCellString("st_modelno"));
			ppj.addDetailRecordField("det_col3", c.getCellString("st_mbrand"));
			ppj.addDetailRecordField("det_col4", "" + c.getCellInt("ind_qty"));
			ppj.addDetailRecordField("det_col5", c.getCellString("ind_uprice"));
			ppj.addDetailRecordField("det_col6", c.getCellString("ind_setamount"));
			n++;
		}

   		ppj.useDetailGroup("detailrow1");
   		ppj.addDetailRecord();
		ppj.addDetailRecordField("ddesp", "以上价格为完税后客户国内工厂交货价（含税）");

   		ppj.useDetailGroup("detailrow2");
   		ppj.addDetailRecord();
   		ppj.addDetailRecordField("dtitle", "付款方式");
   		/*ppj.addDetailRecord();
		ppj.addDetailRecordField("dlabel", "I. 30%于签订合同时支付。");
   		ppj.addDetailRecord();
		ppj.addDetailRecordField("dlabel", "II. 70%余款于发货前付清。");*/
   		if (br.getCellInt("inv_psch0") != 0) {
   			ppj.addDetailRecord();
   			ppj.addDetailRecordField("dlabel", String.format("首付:  %d", br.getCellInt("inv_psch0")));
   			if (StringUtils.isNotBlank(br.getCellString("inv_pschs0")))
   				ppj.addDetailRecordField("dlabel", String.format("首付项: %s", br.getCellString("inv_pschs0")), 100, 0);
   		}
   		if (br.getCellInt("inv_psch1") != 0) {
   			ppj.addDetailRecord();
   			ppj.addDetailRecordField("dlabel", String.format("次付:  %d", br.getCellInt("inv_psch1")));
   			if (StringUtils.isNotBlank(br.getCellString("inv_pschs1")))
   				ppj.addDetailRecordField("dlabel", String.format("次付项: %s", br.getCellString("inv_pschs1")), 100, 0);
   		}
   		if (br.getCellInt("inv_psch2") != 0) {
   			ppj.addDetailRecord();
   			ppj.addDetailRecordField("dlabel", String.format("尾数:  %d", br.getCellInt("inv_psch2")));
   			if (StringUtils.isNotBlank(br.getCellString("inv_pschs2")))
   				ppj.addDetailRecordField("dlabel", String.format("尾数项: %s", br.getCellString("inv_pschs2")), 100, 0);
   		}
   		ppj.addDetailRecord();
   		ppj.addDetailRecord();
   		ppj.addDetailRecordField("dtitle", "交货期");
   		ppj.addDetailRecord();
		ppj.addDetailRecordField("dlabel", "合同签订且收到定金后3.5个月内发货。");
   		ppj.addDetailRecord();
   		ppj.addDetailRecord();
		ppj.addDetailRecordField("dtitle", "运输及保险");
   		ppj.addDetailRecord();
		ppj.addDetailRecordField("dlabel", "此价格包含运输及保险至客户国内工厂。");
   		ppj.addDetailRecord();
   		ppj.addDetailRecord();
		ppj.addDetailRecordField("dtitle", "安装及培训");
   		ppj.addDetailRecord();
		ppj.addDetailRecordField("dlabel", "卖方派出技术人员进行一次性免费安装、调试及技术培训。");
   		ppj.addDetailRecord();
   		ppj.addDetailRecord();
		ppj.addDetailRecordField("dtitle", "质保");
   		ppj.addDetailRecord();
		ppj.addDetailRecordField("dlabel", "i)提供到货后为期12个月的设备质保期。");
   		ppj.addDetailRecord();
		ppj.addDetailRecordField("dlabel", "ii)保修期内，在正常操作情况下，如因机器本身的质量问题所造成的机器故障，卖方负责免费维修机器。");
   		ppj.addDetailRecord();
		ppj.addDetailRecordField("dlabel", "如因操作不慎或人力不可抗拒的因素所造成的机器故障，卖方负责免费维修机器，买方应支付所更换的", 10, 0);
   		ppj.addDetailRecord();
		ppj.addDetailRecordField("dlabel", "零件费用及国内维修人员的往返交通及食宿费用。", 10, 0);
   		ppj.addDetailRecord();
   		ppj.addDetailRecord();
		ppj.addDetailRecordField("dtitle", "有效期");
   		ppj.addDetailRecord();
		ppj.addDetailRecordField("dlabel", "此报价单有效期为三十天。");
   		ppj.addDetailRecord();
		ppj.addDetailRecordField("dlabel", "商祺！");

   		ppj.useDetailGroup("detailrow3");
   		ppj.addDetailRecord();
   		ppj.addDetailRecordField("blabel1", Erpv4Config.getCoName(br.getSessionHelper(), cocode), 0, 25);
   		ppj.addDetailRecordField("blabel", "");
	}

	@Override
	public PrtdocJson getPrintDocJson() throws Exception {
		return(ppj);
	}
 
}
