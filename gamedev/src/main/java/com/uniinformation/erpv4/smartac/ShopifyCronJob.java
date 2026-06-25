package com.uniinformation.erpv4.smartac;

import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.cron.CronJob;
import com.uniinformation.erpv4.wip.WfmTaskUpdate;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkf.smartac.ShopifyGetProduct;

public class ShopifyCronJob extends CronJob {
	SessionHelper sessionHelper;
	BiSchema schema;
	@Override
	public int runOnce() throws Exception {
		// TODO Auto-generated method stub
		UniLog.log1("CronServer Shopify Running");
		doImportOrders("004");
		return 0;
	}
	void doImportOrders(String p_cocode) throws Exception {
		HashSet<String> shops = new HashSet<String>();
		SelectUtil su = schema.getSelectUtil();
		TableRec tr = su.getQueryResult("select * from locationcode where loc_cocode = '"+p_cocode+"'");
		for(int i=0;i<tr.getRecordCount();i++) {
			tr.setRecPointer(i);
			String shopid = tr.getFieldString("loc_shopid");
			if(!StringUtils.isBlank(shopid)) shops.add(shopid);
		}
		java.util.Date fromDate = DateUtil.prevday(DateUtil.today(), 300);
		java.util.Date toDate = DateUtil.prevday(DateUtil.today(), 1);
		ShopifyGetProduct.apiGetOrders(shops, fromDate,toDate);
		UniLog.log("Import order for " +shops + " from " + fromDate);
	}

	@Override
	public void setSessionHelper(SessionHelper p_sh) throws Exception {
		// TODO Auto-generated method stub
		sessionHelper = p_sh;
		// TODO Auto-generated method stub
		schema = BiSchema.loadSchema(sessionHelper);
	}

}
