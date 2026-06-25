package com.uniinformation.jxapp;

import java.util.Date;

import com.kyoko.common.Sprintf;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellException;
import com.uniinformation.jx.JxField;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;

public class PmsWoMaster extends JxZkBiBase {
	@Override
	public void afterBind() {
		super.afterBind();
		jxSetItemList("category", new VectorUtil()
			.addElement("書本")
			.addElement("小冊子")
			.addElement("單張")
			.addElement("影印")
			.toVector());
		jxSetItemList("prtcolor", new VectorUtil()
			.addElement("彩色")
			.addElement("黑白")
			.toVector());
		jxSetItemList("cprtcolor", new VectorUtil()
			.addElement("彩色")
			.addElement("黑白")
			.toVector());
		jxSetItemList("prtsize", new VectorUtil()
			.addElement("A3")
			.addElement("A4")
			.addElement("A5")
			.addElement("自訂")
			.toVector());
		jxSetItemList("bindtype", new VectorUtil()
			.addElement("騎釘")
			.addElement("膠圈")
			.addElement("金屬圈")
			.toVector());
		jxSetItemList("fintype", new VectorUtil()
			.addElement("無")
			.addElement("光膠")
			.addElement("啞膠")
			.toVector());
		jxSetItemList("shiptype", new VectorUtil()
			.addElement("自取")
			.addElement("送貨")
			.toVector());
		jxSetItemList("shipregion", new VectorUtil()
			.addElement("香港")
			.addElement("九龍")
			.addElement("新界")
			.toVector());
		jxSetItemList("paytype", new VectorUtil()
			.addElement("現金")
			.addElement("VISA")
			.addElement("八達通")
			.toVector());
		jxSetItemList("cpapertype", new VectorUtil()
			.addElement("210 GSM 單粉咭紅塔 ( 中國 )")
			.addElement("170 GSM 單粉咭紅塔 ( 中國 )")
			.toVector());
		jxSetItemList("papertype", new VectorUtil()
			.addElement("105 GSM 啞粉 金東 ( 中國 )")
			.addElement("80 GSM 書紙 UPM ( 中國 )")
			.toVector());
		jxSetItemList("wo_qostatus", new VectorUtil()
			.addElement("新單")
			.addElement("已付款")
			.addElement("發貨")
			.addElement("已發貨")
			.addElement("已簽收")
			.toVector());
		
		JxField fd = null;
		new JxFieldChange("category")
		{
			public boolean valueChanged(JxField fd,String orgValue){
				int idx = fd.getItemIndex();
				UniLog.log("PmsWoMaster category changed to " + idx);
				if(idx == 0) {
					jxSetVisible("cprtScr",true);
					jxSetVisible("bindtypeScr",true);
				} else {
					jxSetVisible("cprtScr",false);
					jxSetVisible("bindtypeScr",false);
				}
				recalPrice();
				return(true);
			}
		};
		new JxFieldChange("shiptype")
		{
			public boolean valueChanged(JxField fd,String orgValue){
				int idx = fd.getItemIndex();
				UniLog.log("PmsWoMaster shiptype changed to " + idx);
				if(idx == 1) {
					jxSetVisible("addressScr",true);
				} else {
				jxSetVisible("addressScr",false);
				}
				recalPrice();
				return(true);
			}
		};
		new JxFieldChange("ppage prtsize pdepth pwidth prtcolor papertype cprtcolor cpapertype bindtype fintype folding")
		{
			public boolean valueChanged(JxField fd,String orgValue){
				recalPrice();
				return(true);
			}
		};
	};
	@Override
	public void bindCellCollection(BiResult c,int mode) {
		super.bindCellCollection(c,mode);
		JxField fd;
		if(mode == MODE_ADD) {
			try {
				getBr().getCell("wo_cdate").set(new Date());
				getBr().getCell("wo_custcode").set("RETAIL");
				getBr().getCell("wo_qostatus").set("新單");
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		}
		fd = jxAdd("shiptype");
		if(fd != null) {
			int idx = fd.getItemIndex();
			if(idx == 1) {
				jxSetVisible("addressScr",true);
			} else {
				jxSetVisible("addressScr",false);
			}
		}
		fd = jxAdd("category");
		if(fd != null) {
			int idx = fd.getItemIndex();
			if(idx == 0) {
				jxSetVisible("cprtScr",true);
				jxSetVisible("bindtypeScr",true);
			} else {
				jxSetVisible("cprtScr",false);
				jxSetVisible("bindtypeScr",false);
			}
		}
	}	
	public void recalPrice()
	{
			try {
				double uprice = 0.0;
				int n;
				n = getBr().getCell("category").getInt();
				UniLog.log("category = " + n);
				switch(n) {
				case 0: uprice += 1.0 * getBr().getCell("ppage").getInt(); break;
				case 1: uprice += 8.0 * getBr().getCell("ppage").getInt(); break;
				case 2: uprice += 6.0 * getBr().getCell("ppage").getInt(); break;
				case 3: uprice += 4.0 * getBr().getCell("ppage").getInt(); break;
				}
				n = getBr().getCell("fintype").getInt();
				UniLog.log("fintype = " + n);
				switch(n) {
				case 1: uprice += 0.1; break;
				case 2: uprice += 0.2; break;
				}
				n = getBr().getCell("bindtype").getInt();
				UniLog.log("bindtype = " + n);
				switch(n) {
				case 0: uprice += 0.5; break;
				case 1: uprice += 0.8; break;
				case 2: uprice += 1.0; break;
				}
				
				getBr().getCell("unitprice").set(uprice);
			} catch (CellException cex) {
				UniLog.log(cex);
			}
		
	}
}
