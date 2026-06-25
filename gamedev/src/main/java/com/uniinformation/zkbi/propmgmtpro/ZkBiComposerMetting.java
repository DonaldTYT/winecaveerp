package com.uniinformation.zkbi.propmgmtpro;

import java.util.Date;
import java.util.Vector;

import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Image;
import org.zkoss.zul.Listitem;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.zkbi.ZkBiComposerBase;

public class ZkBiComposerMetting extends ZkBiComposerBase {

	@Override
    protected void renderOneRecord_real(Listitem item, Object trStat, Vector listColumns, final BiResult result, int idx, Object ts) throws Exception {
		super.renderOneRecord_real(item, trStat, listColumns, result, idx, ts);
		Component statusComp = item.query("[bclabel='vcol_status']");
		if (statusComp != null) {
			statusComp.getChildren().clear();
  			Date date = result.getCellDate("col_b");
  			if (date.compareTo(DateUtil.today()) <= 0) {
  				Image img = new Image();
				img.setWidth("20px");
				img.setHeight("20px");
				img.setSrc("images/icons/propmgmtpro/" + (date.compareTo(DateUtil.today()) == 0 ? "bulb_on.svg" : "bulb_off.svg"));
  				statusComp.appendChild(img);
  			}
		}
    }
}
