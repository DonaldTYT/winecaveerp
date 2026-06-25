package com.uniinformation.dynamic.aw;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.ReturnMsg;
import com.kyoko.common.StringUtil;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkf.ZkForm;

import nl.basjes.parse.useragent.yauaa.shaded.org.apache.commons.lang3.StringUtils;

public class CopyOrderMulti extends BiActionHandler{
	public CopyOrderMulti(ZkBiComposerBase p_bibase) {
		super(p_bibase);
		// TODO Auto-generated constructor stub
	}
	String invno = null;
	String vcode = null;
	int cnt=0;
	BiResult result;
    Vector args;
    Vector revs;
    
	@Override
	public ReturnMsg beforeAction(BiResult p_result,int cnt) {
		// TODO Auto-generated method stub
		args = new Vector();
		revs = new Vector();
		cnt = 0;
		invno = null;
		result = null;
		return (ReturnMsg.defaultOk);
	}

	@Override
	public ReturnMsg processAction(BiResult p_result, int p_recIdx) {
		// TODO Auto-generated method stub
		result = p_result;
		String ss = p_result.getCellString("inv_invno");
		if(invno == null) {
			invno = ss;
		} else {
			if(!ss.equals(invno)) {
				invno = "";
			}
		}
		/*
		if(!ss.equals(invno)) {
			if(invno != null) {
				return(new ReturnMsg(false,"Cannot copy from multiple quotation"));
			}
		}
		*/
		args.add(p_result.getCellInt("jm_rg"));
		revs.add(p_result.getCellInt("jm_rev"));
		cnt++;
		return (ReturnMsg.defaultOk);
	}
	@Override
	public ReturnMsg afterAction(BiResult p_br) {
		final ZkForm zkf1 = new ZkForm(null,"zkf/aw/CopyOrderMulti.zul");
		final CellCollection col = new CellCollection();
		try {
			zkf1.doModal(col,new EventListener() {
				@Override
				public void onEvent(Event arg0) throws Exception {
						if(arg0.getTarget() instanceof Button) {
							zkf1.exitModal();
						}
						if(arg0.getTarget().getId().equals("btOK")) {
        			        	RpcClient rpc = result.getSessionHelper().getRpcClient();
        			        	int copycocode = col.getCellInt("copycocode");
        			        	String copyvcode = col.getCellString("copyvcode");
        			        	args.add(0,col.getCellString("copyquo"));
        			        	switch(copycocode) {
        			        		case 1: 
        			        				if(StringUtils.isBlank(copyvcode)) {
        			        					Messagebox.show(
        	    									"Please Enter Customer Code",
            						        		result.getSessionHelper().getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        			        					return;
        			        				}
        			        				args.add(0,"AAW1"+copyvcode); break;
        			        		case 2: 
        			        				if(StringUtils.isBlank(copyvcode)) {
        			        					Messagebox.show(
        	    									"Please Enter Customer Code",
            						        		result.getSessionHelper().getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        			        					return;
        			        				}
        			        				args.add(0,"BAW1"+copyvcode); break;
        			        		default : {
        			        			if(StringUtils.isBlank(invno)) {
        			        				Messagebox.show(
        	    								"Please Select Company and Enter Customer Code for Multiple Quotation Copy",
            						    	    result.getSessionHelper().getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        			        				return;
        			        			}
        			        			args.add(0,invno);
        			        		}
        			        	}
        		           		Value v = rpc.callSegment(
        								"erpv4CloneWorkOrderMulti",
        								args
        							);
        			        	rpc.close();
        	    				if(v != null && v.toString().startsWith("OK")) {
        	       					// redir to DO Update Page
        	    					try {
//        	    					int mrg = Integer.parseInt(StringUtil.strpart(v.toString(), 4, -1).trim());
        	    					String newJobNo = StringUtil.strpart(v.toString(), 4, -1).trim();
        	    					JSONObject jo = new JSONObject();
        	    					JSONArray ja = new JSONArray();
        	    					BiView pov = result.getView().getSchema().getViewByName("aw.WorkOrder");
        	    					ja.put(pov.getTable().getName());
        	    					jo.put("tablist", ja);
        	    					jo.put("wherestr", "jm_jobno = '" + newJobNo + "'");
        	    					String key = result.getSessionHelper().putOneTimeData( jo);
//        	    					Executions.getCurrent().sendRedirect("zkbiloader.html?action=browse&viewid=aw.WorkOrder&page_id=WorkOrder_01&zul=zkbiloader.zul&composer=aw.ZkBiComposerWorkOrder&querycondition="+key);
        	    					Executions.getCurrent().sendRedirect("zkbiloader.html?action=update&viewid=aw.WorkOrder&page_id=WorkOrder_01&zul=zkbiloader.zul&composer=aw.ZkBiComposerWorkOrder&querycondition="+key);
        	    					
        	    					} catch (Exception ex) {
        	    						UniLog.log(ex);
        	    					}
        	    					//doPopupStmd(doViewName,mrg);
        	    				} else {
        	    					if(v == null) {
        	    						Messagebox.show(
        	    								"Copy Work Order Failed : Unknown Reason",
            								 result.getSessionHelper().getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
            						
        	    					} else {
        	    						Messagebox.show(
            								"Copy Work Order Failed : " + v.toString().substring(4),
            								 result.getSessionHelper().getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        	    					}
        	    				}
						}
				}
			});
		} catch (CellException cex) {
			UniLog.log(cex);
			return(new ReturnMsg(false,cex.toString()));
		}
				
		return null;
	}
	public ReturnMsg afterActionXx() {
		// TODO Auto-generated method stub
        			Messagebox.show(
        					"Confirm Copy Order from job " + invno + " revision " + revs + " ?"  ,
        					"Copy Work Order", Messagebox.OK | Messagebox.CANCEL, Messagebox.QUESTION, new org.zkoss.zk.ui.event.EventListener() {
        			    public void onEvent(Event evt) throws InterruptedException {
        			        if (evt.getName().equals("onOK")) {
        			        	RpcClient rpc = result.getSessionHelper().getRpcClient();
        			        	args.add(0,invno);
        		           		Value v = rpc.callSegment(
        								"erpv4CloneWorkOrderMulti",
        								args
        							);
        			        	rpc.close();
        	    				if(v != null && v.toString().startsWith("OK")) {
        	       					// redir to DO Update Page
        	    					try {
//        	    					int mrg = Integer.parseInt(StringUtil.strpart(v.toString(), 4, -1).trim());
        	    					String newJobNo = StringUtil.strpart(v.toString(), 4, -1).trim();
        	    					JSONObject jo = new JSONObject();
        	    					JSONArray ja = new JSONArray();
        	    					BiView pov = result.getView().getSchema().getViewByName("aw.WorkOrder");
        	    					ja.put(pov.getTable().getName());
        	    					jo.put("tablist", ja);
        	    					jo.put("wherestr", "jm_jobno = '" + newJobNo + "'");
        	    					String key = result.getSessionHelper().putOneTimeData( jo);
//        	    					Executions.getCurrent().sendRedirect("zkbiloader.html?action=browse&viewid=aw.WorkOrder&page_id=WorkOrder_01&zul=zkbiloader.zul&composer=aw.ZkBiComposerWorkOrder&querycondition="+key);
        	    					Executions.getCurrent().sendRedirect("zkbiloader.html?action=update&viewid=aw.WorkOrder&page_id=WorkOrder_01&zul=zkbiloader.zul&composer=aw.ZkBiComposerWorkOrder&querycondition="+key);
        	    					
        	    					} catch (Exception ex) {
        	    						UniLog.log(ex);
        	    					}
        	    					//doPopupStmd(doViewName,mrg);
        	    				} else {
        	    					if(v == null) {
        	    						Messagebox.show(
        	    								"Copy Work Order Failed : Unknown Reason",
            								 result.getSessionHelper().getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
            						
        	    					} else {
        	    						Messagebox.show(
            								"Copy Work Order Failed : " + v.toString().substring(4),
            								 result.getSessionHelper().getLabel("Error Message"), Messagebox.OK, Messagebox.ERROR);
        	    					}
        	    				}
        			        } else {
        			        	UniLog.log("Copy Order Canceled");
        			        }
        			    }
        			});		
		
		return null;
	}

}
