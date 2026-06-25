package com.uniinformation.zkbi.erpv4;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collection;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkcomp.ZkBiButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ZkBiComposerSihAr extends ZkBiComposerSih {
	Object currentEditObject = null;
	@Override
   	public void doAfterCompose(final Component comp) throws Exception { 
		super.doAfterCompose(comp);
		module = "AR";
		paymentViewId = "erpv4.CrhAr";
		adjListboxHeight(65);
	}
	@Override
	protected void setupDetailButton(final BiResult result)
	{
			super.setupDetailButton(result);
	        Button btn = new ZkBiButton();
	        btn.setLabel("Print");
	        btn.setId("btScanRec");
	        btn.setAttribute("tlkey", "btPrint");
	        actionBar.appendChild(btn);
			btn.addEventListener("onClick",
					new EventListener() {
						@Override
						public void onEvent(Event arg0) throws Exception {
							final java.util.Set selection = listModelList.getSelection();
							if(selection.size() != 1) {
								Messagebox.show(
										"Please Select Invoice",
										sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
								return;
							}
    			        	currentEditObject = selection.toArray()[0];
    			        	int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, currentEditObject);
    			        	result.loadOneRecV(idx);
    			        	{
    			        		String paperType = Erpv4Config.getString(result.getSessionHelper(),"DnPaperType");
    			        		if(paperType == null) paperType="A4P";
    			        		JSONObject jo = new JSONObject();
    			        		jo.put("callsegment", "real_print_invoice");
    			        		jo.put("prtdocname", "ERPV4INV1");
    			        		jo.put("sno", result.getCellString("sih_sno"));
    			        		JSONArray ja = new JSONArray();
    			        		ja.put("CHNPRINT");
    			        		ja.put("VARIABLE");
    			        		ja.put(paperType);
    			        		ja.put("NORMAL");
    			        		ja.put("LPTRAW");
    			        		jo.put("plptopts", ja);
    			        		RpcClient rpc = result.getSessionHelper().getRpcClient();
    			        		rpc.callSegment("setCocodeBaseccy",
									new VectorUtil()
//									.addElement(Erpv4Config.getCoCode(result.getSessionHelper()))
//									.addElement(Erpv4Config.getBaseCcy(result.getSessionHelper()))
									.addElement(result.getCellString("sih_cocode"))
									.addElement(Erpv4Config.getBaseCcy(result.getSessionHelper(),result.getCellString("sih_cocode")))
									.toVector()
									);
    			        		rpc.callSegment("printer_autoselect",
    			        				new VectorUtil()
    			        			.addElement(1)
    			        			.toVector()
    			        			);
    			        		rpc.callSegment("erpv4SetImageDir", new VectorUtil().addElement(getSessionHelper().getWebContentRealPath("images", true)).toVector());
    			        		Value val = rpc.callSegment("prtdocutil_print_jsonstr",
    			        				new VectorUtil()
    			        				.addElement(jo.toString())
    			        				.toVector()
    			        						);
    			        		rpc.close();
				if(val != null && val.toString().startsWith("OK")) {
					String fname = val.toString().substring(4);
					UniLog.log("Print Do got " + fname);
					try {
//					ZkUtil.print((Component) (jxAdd("detail_grid").getNativeObject()));	
						InputStream is = result.getSessionHelper().newErpFileInputStream(fname);
						int cc = ChnftrParser.getPaperTypeIndex(paperType);
						ChnftrParser ps;
						if( cc >= 0) {
							ps = new ChnftrParser(is,"-p"+cc);
						} else {
							ps = new ChnftrParser(is,"'");
						}
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						ps.print(bos);
						/*
						ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
						SessionHelper sessionHelper = (SessionHelper) Executions.getCurrent().getSession().getAttribute(SessionHelper.getNameByContextPath(Executions.getCurrent().getContextPath()));	
						ZkUtil.printFromStream(bis, "application/pdf", sessionHelper);
						*/
						{
							//xjcheng221111: getDesktop().getComponents possible to get a non-root/hide window 
							/*Collection<Component> comps = Executions.getCurrent().getDesktop().getComponents();
							for (Component comp : comps) {
								if (comp instanceof Window) {
									ZkUtil.showPdfDialog(comp, result.getSessionHelper(), bos.toByteArray(), "Invoice-"+result.getCellString("sih_sno"));;
									break;
								}
							}*/
							ZkUtil.showPdfDialog(actionBar.getRoot(), result.getSessionHelper(), bos.toByteArray(), "Invoice-"+result.getCellString("sih_sno"));;
						}
						
					} catch (Exception ex) {
						UniLog.log(ex);
					}
				} else {
					if(val != null) 
						Messagebox.show("Print D/N Error : " + val.toString());
					else
						Messagebox.show("Print D/N Error : Unknown");
				}
    			        		
    			        	}
    			        	
						}
				}
			);
	}
}
