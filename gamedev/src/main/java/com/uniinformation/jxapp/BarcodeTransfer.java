package com.uniinformation.jxapp;

import java.util.HashMap;
import java.util.Vector;

import org.zkoss.json.JSONObject;
import org.zkoss.json.parser.JSONParser;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Image;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;

import com.kyoko.common.StringUtil;
//import com.uniinformation.estimation.printing.PrintingJob;
//import com.uniinformation.estimation.printing.UI_control;
//import com.uniinformation.estimation.printing.ZKUI_control;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.JxForm;
import com.uniinformation.jx.MessageBoxActionInterface;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;

public class BarcodeTransfer extends JxForm {
	String fromBin="";
	
	class ItemDescription {
		int idx;
		int irg;
		int org;
		int cnt;
		String jobnumber;
		String revision;
	}
	HashMap <String,String> scannedHash = new HashMap<String,String>();
	HashMap <String,ItemDescription> itemHash = new HashMap<String,ItemDescription>();
	
	void clearRecords()
	{
				
				itemHash.clear();
				scannedHash.clear();
				jxGridSetRow("lbItems",0);
				jxGridSetRow("lbScanned",0);
				jxSetText("ibTotal","");
		
	}
	public void afterBind()
	{
		super.afterBind();
		UniLog.log("BarcodeTransfer afterbind");
		jxGridSetCol("lbScanned",1);
		JxField lbfd = jxAdd("lbItems");
		lbfd.gridSetRow(1);
		lbfd.gridSetCol(3);
		if(jxGetText("tfrMode").equals("IN")) {
			jxSetText("tfrName","成品入倉");
			fromBin = "000000";
		} else {
			jxSetText("tfrName","成品出倉");
			fromBin = "IN";
		}
		
		new JxFieldAction("btClear") {
			public void actionPerformed(JxField fd) {
				confirm("Confirm Clear ?", new MessageBoxActionInterface()  {
						public void onButtonClicked(Object p_data) {
							UniLog.log("HAHA Clear Button Clicked " + p_data);
							if(p_data instanceof Integer && ((Integer) p_data).intValue() == 1) {
								clearRecords();
							}
						}
					}
				);
			}
		};
		new JxFieldAction("btSubmit") {
			public void actionPerformed(JxField fd) {
					if(scannedHash.size() > 0) {
						RpcClient rpc = getRpcClient();
						VectorUtil vu = new VectorUtil().addElement("").addElement(jxGetText("tfrMode")).addElement(itemHash.size());
						for(String key: itemHash.keySet()) {
							ItemDescription item = itemHash.get(key);
							vu.addElement(item.irg);
							vu.addElement(item.org);
							vu.addElement(item.cnt);
							vu.addElement(fromBin);
						}
						Value v = rpc.callSegment("winecave_submitTransfer",vu.toVector());
						if(v == null) messageBox("Submit Failed, Unknown Error",1,null);
							else if(!v.toString().startsWith("OK")) {
								messageBox("Submit Failed, " + v.toString().substring(4),1,null);
							} else {
								messageBox("Transfer Submited");
								clearRecords();
							}
						UniLog.log("submit Transfer got " + v.toString());
						
					} else {
						messageBox("沒有掃描記錄",1,null);
					}
			}
		};
		new JxFieldAction("btScanX") {
			public void actionPerformed(JxField fd) {
				jxSetVisible("scr1",false);
				jxSetVisible("scr2",true);
				UniLog.log("HAHA activate scan barcode");
//			   	Clients.evalJavaScript("setBrowserWindowId('"+browserWindowId.getUuid()+"')");
			   	Clients.evalJavaScript("launchScanner()");
			}
		};
		new JxFieldAction("btScanE") {
			public void actionPerformed(JxField fd) {
				jxSetVisible("scr1",true);
				jxSetVisible("scr2",false);
				UniLog.log("HAHA deactivate scan barcode");
//			   	Clients.evalJavaScript("setBrowserWindowId('"+browserWindowId.getUuid()+"')");
			   	Clients.evalJavaScript("closeScanner()");
			}
		};
		new JxFieldChange("txNewBarcode") {
			public boolean valueChanged(JxField fd,String orgValue) {
				UniLog.log("Barcode scanned value change to " + fd.getText());
				if(fd.getText().startsWith("0") && fd.getText().length()==16) {
				String itemKey = scannedHash.get(fd.getText());
				if(itemKey == null) {
					int cnt = scannedHash.size();
					jxGridSetRow("lbScanned",cnt+1);
					itemKey = StringUtil.strpart(fd.getText(), 1, 12);
					ItemDescription item = itemHash.get(itemKey);
					if(item == null) {
						item = new ItemDescription();
						item.irg = Integer.parseInt(StringUtil.strpart(fd.getText(), 1, 6));
						item.org = Integer.parseInt(StringUtil.strpart(fd.getText(), 7, 6));
						item.cnt = 1;
						item.idx = itemHash.size();
						itemHash.put(itemKey, item);
						jxGridSetRow("lbItems",item.idx+1);
					} else {
						item.cnt++;
					}
					jxGridSetValue("lbScanned",0,cnt,fd.getText());
					scannedHash.put(fd.getText(), itemKey);
					jxGridSetValue("lbItems",0,item.idx,itemKey);
					jxGridSetValue("lbItems",1,item.idx,"");
					jxGridSetValue("lbItems",2,item.idx,""+item.cnt);
					jxSetText("ibTotal",""+scannedHash.size());
					Clients.evalJavaScript("beep('1')");
				} else {
					Clients.evalJavaScript("beep('0')");
				}
				} else {
					Clients.evalJavaScript("beep('0')");
				}
				return(false);
			}
		};
	}
}
