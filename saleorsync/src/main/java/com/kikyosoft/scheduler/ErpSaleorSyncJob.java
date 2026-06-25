package com.kikyosoft.scheduler;

import com.kikyosoft.config.IniLoader;
import com.kikyosoft.service.SyncService;

//import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "sync.enabled", havingValue = "true", matchIfMissing = true)
public class ErpSaleorSyncJob {
  private static final Logger log = LoggerFactory.getLogger(ErpSaleorSyncJob.class);

  private final SyncService syncService;
//  private final IniLoader iniLoader;
  public ErpSaleorSyncJob(SyncService syncService , IniLoader iniLoader) {
    this.syncService = syncService;
//    this.iniLoader = iniLoader;	
//    IniHelper.setIniLoader(iniLoader);
  }

//  @PostConstruct
//  public void loadIni() throws Exception {
//	  if(sp == null) {
//		 sp = SessionHelper.getSessionHelperDummy("winecaveerp", "hlv", null, () -> new ZkSessionHelper());
//	  }
//	  /*
//	INIConfiguration cfg = iniLoader.load("classpath:config/erpsetup.properties",null);
//	String agent = cfg.getString("iniAgent", null);
//	LogUtil.log("Agent="+agent);
//		*/
//  }

  @Scheduled(fixedDelayString = "600000")
  public void run() {
    log.info("Scheduled ERP→Saleor sync starting…");
    try {
      
      syncService.runIncrementalSync();
      log.info("Scheduled sync completed.");
    } catch (Exception e) {
      log.error("Scheduled sync failed", e);
    }
  }
}