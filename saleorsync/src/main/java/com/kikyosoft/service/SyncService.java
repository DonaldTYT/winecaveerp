package com.kikyosoft.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.kikyosoft.config.SaleorConfig;
import com.kikyosoft.utils.LogUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiSchema;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.ZkSessionHelper;

@Service
public class SyncService {
    @Autowired
	private SaleorMediaService mediaService;
	
	
	ZkSessionHelper sp;
	public void runIncrementalSync() throws Exception {
		if(sp == null) {
			LogUtil.log("SessionHelper is null, login");
//			sp = ZkSessionHelper.getSessionHelperDummy("winecavescp", "hlv");
			sp = ZkSessionHelper.getSessionHelperDummy(null,"hlv",null);
		}
		if(sp == null) {
			LogUtil.log("Failed to create SessionHelper , aborted");
			return;
		}
		LogUtil.log("Reading data ....");
		BiSchema schema = BiSchema.loadSchema(sp);
		BiResult br = schema.getViewByName("CompInfo").newBiResult(sp.getLoginId(), null, null, sp);
		br.clear();
		br.clearCondition();
		br.query();
		UniLog.log("Company rec count = " + br.getRecordCount());
		if(br.getRecordCount() > 0) {
			br.loadOneRecV(0);
			UniLog.log("Company Name ["+br.getCellString("co_coname")+ "]");
		}
//		mediaService.addImage("chateau-clerc-milon-2006-pauillac-bordeaux","http://192.168.46.16:8081/sync/getResource?url=message://STOCK_IMAGE/28999/JPG&ext=jpg&snp=009" , "jpg");
//		mediaService.addImage("spirit-whisky-scotland-mesmswb3-28428","http://192.168.46.16:8081/sync/getResource?url=message://STOCK_IMAGE/28999/JPG&ext=jpg&snp=009" , "jpg");
//		mediaService.addImage("chateau-clerc-milon-2006-pauillac-bordeaux","https://images.pexels.com/photos/121191/pexels-photo-121191.jpeg" , "jpg");
//		mediaService.addImage("sss1","https://images.pexels.com/photos/121191/pexels-photo-121191.jpeg" , "jpg");
		
	}
}

