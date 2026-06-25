package com.uniinformation.dynamic.wfm;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.mail.EmailAttachment;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.erpv4.wip.WfmActionInterface;
import com.uniinformation.erpv4.wip.WfmJob;
import com.uniinformation.erpv4.wip.WfmTask;
import com.uniinformation.utils.CloseUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class WfmTestAction implements WfmActionInterface {
	@Override
	public ReturnMsg startAction(SessionHelper p_sh,WfmJob p_job, WfmTask p_task) {
		// TODO Auto-generated method stub
		// return ReturnMsg.defaultOk;
		return(wfmTestSendEmail(p_sh,p_job, p_task));
	}

	@Override
	public ReturnMsg getActionStatus(SessionHelper p_sh,WfmJob p_job, WfmTask p_task) {
		// TODO Auto-generated method stub
		return ReturnMsg.defaultOk;
	}

	private	ReturnMsg wfmTestSendEmail(SessionHelper p_sh,WfmJob p_job, WfmTask p_task) {
		List<Pair<String,String>> toList = new ArrayList<Pair<String,String>>();
		List<Pair<String,String>> ccList = new ArrayList<Pair<String,String>>();
		List<Pair<String,String>> bccList = new ArrayList<Pair<String,String>>();
		List<EmailAttachment> attList = new ArrayList<EmailAttachment>();
		try {
			int idx=0;
//			int cnt = (Integer) p_vargs.get(idx++);
//			for(;cnt > 0;idx++,cnt--) {
//				toList.add(Pair.of((String) p_vargs.get(idx), ""));
//			}
//			cnt = (Integer) p_vargs.get(idx++);
//			for(;cnt > 0;idx++,cnt--) {
//				ccList.add(Pair.of((String) p_vargs.get(idx), ""));
//			}
//			cnt = (Integer) p_vargs.get(idx++);
//			for(;cnt > 0;idx++,cnt--) {
//				bccList.add(Pair.of((String) p_vargs.get(idx), ""));
//			}
//			cnt = (Integer) p_vargs.get(idx++);
//			for(;cnt > 0;idx += 2,cnt--) {
//				File tmpFile = sh.newErpFileToTmpfile((String) p_vargs.get(idx), "tmpatt", ".tmp", new File("/tmp"));
//				EmailAttachment att1 = new EmailAttachment();
//				att1.setPath(tmpFile.getAbsolutePath());
//				att1.setName((String) p_vargs.get(idx+1));
//				att1.setDisposition(EmailAttachment.ATTACHMENT);
//				attList.add(att1);	
//				UniLog.log("add email Attachement " + tmpFile.getAbsolutePath()+" "+att1.getName());
//			}
//			ZkUtil.sendEmail(
//					Pair.of(p_from, (String) null),
//					toList, 
//					ccList, 
//					bccList, 
//					p_subject, 
//					p_html, null, attList, sh);
//		
			return(ReturnMsg.defaultOk);
		} catch (Exception ex) {
			UniLog.log(ex);
			return( new ReturnMsg(false,"FAILExcaption Catched"));
		}
		finally {
			for(EmailAttachment att: attList) {
				CloseUtil.close(new File(att.getPath()));
				CloseUtil.delete(new File(att.getPath()));
			}
		}
	}
}
