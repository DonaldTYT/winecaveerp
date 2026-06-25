package com.uniinformation.zkbi.vincero;

import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zul.Messagebox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.edu.BiResultCourse;
import com.uniinformation.bicore.erpv4.BiResultLocationAsAt;
import com.uniinformation.utils.CloseUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.BiActionHandler;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiComposerBase.MultiSortMap;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;

//add db index for reference
//===========================
//#good txid, cusid, date, amount
//SELECT col_b,col_p, col_e, col_f,  count(*) FROM payment_record group by col_b,col_p,col_e,col_f having count(*) = 1 order by 1;
//SELECT col_b, LENGTH(col_b) from payment_record order by 2 desc; #id 100
//SELECT col_p, LENGTH(col_p) from payment_record order by 2 desc; #cust id 100
//select col_e, col_f from payment_record; #date, amount
//
//ALTER TABLE `vincero`.`payment_record` CHANGE COLUMN `col_b` `col_b` VARCHAR(100) NULL DEFAULT NULL , CHANGE COLUMN `col_p` `col_p` VARCHAR(100) NULL DEFAULT NULL ;
//ALTER TABLE payment_record ADD UNIQUE INDEX `payment_record_idx0` (col_b ASC,col_p ASC,col_e ASC,col_f ASC);
//
//
//#good cusid#
//SELECT col_b, count(*) FROM subscriptions group by col_c, col_b having count(*) > 1 order by 1 desc;
//SELECT col_b, LENGTH(col_b) from subscriptions order by 2 desc; #100 id
//
//ALTER TABLE `vincero`.`subscriptions`  CHANGE COLUMN `col_b` `col_b` VARCHAR(100) NULL DEFAULT NULL ;
//ALTER TABLE `vincero`.`subscriptions` ADD UNIQUE INDEX `col_b_UNIQUE` (`col_b` ASC);
//ALTER TABLE `vincero`.`subscriptions`  CHANGE COLUMN `col_c` `col_c` VARCHAR(150) NULL DEFAULT NULL ;  //optional for email column

public class ZkBiComposerPaymentRecord extends ZkBiComposerBase {
	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("called");
	}

    protected void setupExtraButton(final BiResult result)
    {
    	super.setupExtraButton(result);
    	
    	if(getSessionHelper().hasAccessRight("#addupdall")) {
    		
		addBatchBiActionHandler(result,true,BiActionHandler.ActionAccessMode_Custom, null,"pbAddMember","Add Member","fa-user",
				new BiActionHandler(this) {
			 		ArrayList<Integer> recIdxs = new ArrayList();
					BiResult br = null;
					@Override
					public ReturnMsg beforeAction(BiResult p_result,int cnt) {
						UniLog.log1("called");
						recIdxs.clear();
						return ReturnMsg.defaultOk;
						
					}
					@Override
					public ReturnMsg processAction(BiResult p_result,int p_recIdx) {
						try {
							UniLog.log1("called");
							recIdxs.add(p_recIdx);
							br = p_result;
						} catch (Exception ex) {
							UniLog.log(ex);
							return(new ReturnMsg(false,ex.toString()));
						}
						return(ReturnMsg.defaultOk);
					}
					@Override
					public ReturnMsg afterAction(BiResult p_br) {
						UniLog.log1("called");
						
						if (recIdxs.size() <= 0 || br == null) {
							return new ReturnMsg(false, "No record selected");
						}
						if (recIdxs.size() > 200) {
							return new ReturnMsg(false, "Too many record selected. max 200");
						}
						
						ZkBiMsgbox.show(String.format("Are you sure to add %d records", recIdxs.size()),new String[]{"Ok","Cancel"},new ZkBiEventListener<Event>(){
							@Override
							public void onZkBiEvent(Event event) throws Exception {
								ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
								UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
								if (btn.getIdx() != 0) {
									UniLog.log1("aborted");
									ZkUtil.showMsg("Action aborted");
									return;
								}
								BiResult subBr = null;
								try {
									int okCnt = 0;
									subBr = sessionHelper.newBiResult("vincero.subscriptions");
									subBr.beginWork();
									for (int idx : recIdxs) {
										subBr.clearCurrentRec();
										boolean rtn = br.loadOneRecV(idx);
										if (!rtn) {
											subBr.rollbackWork();
											ZkUtil.errMsg("Load record failed, aborted");
										}
										//TODO: fill known col from payment to subscriptions
										copyField(br,"Customer ID", subBr, "Customer ID");
										copyField(br,"Customer Email", subBr, "Email");
										copyField(br,"Created date (UTC)", subBr, "Webinar Date");
										copyField(br,"Created date (UTC)", subBr, "Start Date");
										copyField(br,"Card Issue Country", subBr, "Country");
										copyField(br,"Shipping Name", subBr, "Full Name");

										/*
										copyField(br,"", subBr, "Phone");
										copyField(br,"", subBr, "Source");
										*/
										
										ReturnMsg addRtn = subBr.addCurrent();
										UniLog.log1("addRtn:"+ addRtn);
										if (addRtn.getStatus()) {
											okCnt++;
										}
										
									}
									subBr.commitWork();
									ZkUtil.msg("%d record inserted. %d record skipped.", okCnt, recIdxs.size()-okCnt);
								}
								catch(Exception ex) {
									ZkUtil.errMsg("error:" + ex.getMessage());
									ex.printStackTrace();
									if (subBr != null) subBr.rollbackWork();
								}
								finally {
									CloseUtil.close(subBr);
								}
							}
						});
       			        return(ReturnMsg.defaultOk);
					}
				}
			);
    	}
    }
    private void copyField(BiResult srcBr, String srcHdr, BiResult destBr, String destHdr) {
    	try { 
    		destBr.getCellByHeader(destHdr).set(srcBr.getCellByHeader(srcHdr)); 
    	} 
    	catch(Exception ex) {
    		UniLog.log1("cannot copy %s %s error:%s", srcHdr, destBr, ex.getMessage()); 
    	}
    }
}
