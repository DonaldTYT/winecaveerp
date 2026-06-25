package com.uniinformation.zkbi.aw;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ChnftrParser;
import com.uniinformation.utils.ChnftrRpcServlet;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerWorkOrder extends ZkBiComposerBase {
	@Override
	protected void setupAddButton(final BiResult result)
	{
		super.setupAddButton(result);
		Button btnCopyOrder;
        btnCopyOrder = new ZkBiButton();
        btnCopyOrder.setLabel("Copy Order");
        btnCopyOrder.setId("btCopyOrder");
//        btnCopyOrder.setDisabled(true);
        btnCopyOrder.addEventListener("onClick",
            new EventListener() {
            	public void onEvent(Event event) throws Exception {
            		final java.util.Set selection = listModelList.getSelection();
        			if(selection.size() != 1) {
      					Messagebox.show(
   							"Please Select Order To Copy",
   							 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
       					return;
        			}
        			
//    				ArrayList<ZkBiMsgboxButton> btns = new ArrayList<ZkBiMsgboxButton>();
//					btns.add(new ZkBiMsgboxButton("Copy this revision").setName("copyOne"));
//					btns.add(new ZkBiMsgboxButton("Copy all revision").setName("copyAll"));
//					btns.add(new ZkBiMsgboxButton("Cancel").setName("cancel"));
//        		
//					Component msgContent = 
//							Executions.createComponentsDirectly(
//									"<div>"
//									+ "<vlayout>"
//									+ "<label>Copy Work Order</label>"
//									+ "<checkbox></checkbox>"
//									+ "</vlayout>"
//									+ "</div>"
//									, null, null, null);
//							
//					//ZkBiMsgbox.show(sessionHelper.getLabel("Copy Work Order"), btns.toArray(new ZkBiMsgboxButton[btns.size()]), new ZkBiEventListener(){
//					ZkBiMsgbox.show(msgContent, btns.toArray(new ZkBiMsgboxButton[btns.size()]), new ZkBiEventListener(){
//						@Override
//						public void onZkBiEvent(Event event) throws Exception {
//							ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
//							if (btn.getName().equals("copyOne")) {
//								UniLog.log("Copy One");
//							}
//							else if (btn.getName().equals("copyAll")) {
//								UniLog.log("Copy All");
//							}
//							else {
//								UniLog.log("Copy Cancel");
//							}
//						}
//					});        			
					
					
        			Messagebox.show(
        					"Copy Work Order",
        					"Confirm Copy Order ?", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new org.zkoss.zk.ui.event.EventListener() {
        			    public void onEvent(Event evt) throws InterruptedException {
        			        if (evt.getName().equals("onOK")) {
        			        	RpcClient rpc = sessionHelper.getRpcClient();
        			        	Object o = selection.toArray()[0];
        			        	int idx = /* listModelList.indexOf(o);*/ getTrIdxByObj(listModelList, o);
        			        	result.loadOneRecV(idx);
        			        	Vector args = new Vector();
        			        	args.add(result.getCell("jm_rg").getInt());
        			        	args.add("");
        			        	args.add(sessionHelper.getLoginId());
        			        	args.add(DateUtil.today());
        			        	//String custCode = null;
        			        	//String ordType= null;
        			        	//String doViewName = null;
        		           		Value v = rpc.callSegment(
        								"erpv4CloneWorkOrder",
        								args
        							);
        			        	rpc.close();
        	    				if(v != null && v.toString().startsWith("OK")) {
        	       					// redir to DO Update Page
        	    					try {
        	    					int mrg = Integer.parseInt(StringUtil.strpart(v.toString(), 4, -1).trim());
        	    					JSONObject jo = new JSONObject();
        	    					JSONArray ja = new JSONArray();
        	    					BiView pov = result.getView().getSchema().getViewByName("aw.WorkOrder");
        	    					ja.put(pov.getTable().getName());
        	    					jo.put("tablist", ja);
        	    					jo.put("wherestr", "jm_rg = " + mrg);
        	    					String key = sessionHelper.putOneTimeData( jo);
        	    					Executions.getCurrent().sendRedirect("zkbiloader.html?action=update&viewid=aw.WorkOrder&page_id=WorkOrder_01&zul=zkbiloader.zul&composer=aw.ZkBiComposerWorkOrder&querycondition="+key);
        	    					} catch (Exception ex) {
        	    						UniLog.log(ex);
        	    					}
        	    					//doPopupStmd(doViewName,mrg);
        	    				} else {
        	    					if(v == null) {
        	    						Messagebox.show(
        	    								"Copy Work Order Failed : Unknown Reason",
            								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
            						
        	    					} else {
        	    						Messagebox.show(
            								"Copy Work Order Failed : " + v.toString().substring(4),
            								 sessionHelper.getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        	    					}
        	    				}
        			        } else {
        			        	UniLog.log("Copy Order Canceled");
        			        }
        			    }
        			});		
        			
            	}
        	}
        );
        actionBar.appendChild(btnCopyOrder);
	}
	@Override
    protected void setupExtraButton(final BiResult result) {
    	super.setupExtraButton(result);
    	/*
		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbBatchPrintWo",sessionHelper.getBtLabel("Batch Print"),"fa-print",
			new BiActionHandler(this) {
				RpcClient rpc = null;
				ChnftrParser mainparser = null;

				@Override
				public ReturnMsg beforeAction(BiResult p_result,int cnt) { 
					if(cnt > 50) {
						return(new ReturnMsg(false,"Cannot Print more than 50 orders"));
					}
					try {
					mainparser = new ChnftrParser((InputStream)null, "-p14");
					rpc = getSessionHelper().getRpcClient();
					ChnftrRpcServlet rpcservlet = new ChnftrRpcServlet(rpc.getConnection());
					rpc.setRpcServlet(rpcservlet.getClass().getName(), rpcservlet);
					Value val = rpc.callSegment("printer_autoselect",
							new VectorUtil()
							.addElement(1)
							.toVector()
						);
					//val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement("c:\\images\\") .toVector());
					val = rpc.callSegment("erpv4SetImageDir", new VectorUtil() .addElement(ZkUtil.getWebContentRealPath("images", true)) .toVector());
					} catch (Exception ex) {
						UniLog.log(ex);
						return(new ReturnMsg(false,"Initialized Print Job Failed"));
					}
					return(ReturnMsg.defaultOk);
				}

				@Override
				public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
						try {
							boolean ok = result.fetchOneRecV(p_recIdx);
							if(!ok) return(new ReturnMsg(false,"Fetch Record failed"));
							Value val = rpc.callSegment("erpv4_print_wo",
							new VectorUtil()
							.addElement(result.getCell("jm_rg").getInt())
							.addElement("CHNPRINT")
							.addElement("VARIABLE")
							.addElement("A3P")
							.addElement("NORMAL")
							.addElement("LPTRAW")
							.toVector()
							);
							if(val == null || !val.toString().startsWith("OK")) {
								return(new ReturnMsg(false,"Print Work Order " + result.getCellString("inv_invno") + " Failed reason " + (val == null ? "null" : val.toString())));
							}
							String fname = val.toString().substring(4);
							InputStream is = getSessionHelper().newErpFileInputStream(fname);
							ChnftrParser ps = new ChnftrParser(is,"-p14"); // print as A3 , always two pages
							ByteArrayOutputStream bos = new ByteArrayOutputStream();
							ps.print(bos);
							ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
							mainparser.loadTemplateStream(bis);
							return(ReturnMsg.defaultOk);
						} catch (Exception ex) {
							UniLog.log(ex);
							rpc.close();
							return(new ReturnMsg(false,"Print Work Order " + result.getCellString("inv_invno") + " Failed " ));
						}
				}

				@Override
				public ReturnMsg afterAction(BiResult p_br) {
					rpc.close();
//					ByteArrayInputStream bis = null;
//					ZkUtil.printFromStream(bis, "application/pdf", getSessionHelper());
					try {
						ByteArrayOutputStream bos = new ByteArrayOutputStream();
						mainparser.print(bos);
						ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
						ZkUtil.printFromStream(bis, "application/pdf", getSessionHelper());
						return(ReturnMsg.defaultOk);
					} catch (Exception ex) {
						UniLog.log(ex);
						return(new ReturnMsg(false,"End Print Job Failed"));
					}
				}
			}
		);
		*/
    }
    
}
