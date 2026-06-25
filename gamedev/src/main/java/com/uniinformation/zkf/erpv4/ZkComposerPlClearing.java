package com.uniinformation.zkf.erpv4;

import java.util.Vector;

import org.zkoss.zk.ui.Component;

import com.kyoko.common.DateUtil;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.zkf.ZkCellActionForm;

public class ZkComposerPlClearing  extends ZkCellActionForm{
	public void doAfterCompose(Component arg0) throws Exception {
		if(formCollection == null) formCollection = new CellCollection();
		formCollection.addCell("date1", new Cell(""));
		Vector<String> yList = new Vector<String>();
		super.doAfterCompose(arg0);
		String ss = Erpv4Config.getString(sessionHelper, "ErpMinDate");
		java.util.Date md = DateUtil.minDate;
		if(ss != null) {
			md = DateUtil.dateTimeStrToDate(ss);
		}
		for(int y = 2021;y<2040;y++) {
			java.util.Date dd = DateUtil.dateTimeStrToDate(String.format("%04d/01/01", y));
			if(dd.after(md)) {
				yList.add(String.format("%04d/01/01", y));
			}
		}
		formCollection.getCell("date1").setItemList(yList);
		RpcClient rpc = sessionHelper.getRpcClient();
		Value v = rpc.callSegment("erpv4_getLatestPlDate", new VectorUtil().toVector()); 
		if(v != null && v.toString().startsWith("OK")) {
			String plDate = v.toString().substring(4);
			// java.util.Date d = DateUtil.getDate(plDate);
			formCollection.getCell("date1").set(plDate);
		}
		rpc.close();
	}
}
