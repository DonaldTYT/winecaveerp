package com.uniinformation.erpv4.edu;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.erpv4.NotifyMsgObj;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class NotifyMsgEdu{
	/***
	 * called by SessionHelper.showNotifyMsg
	 * It build the msg list based on urlparam menuitem or page_id
	 * @param sh sessionHelper
	 * @return msglist
	 */
	public static List<NotifyMsgObj> getMsgs(SessionHelper sh) {
		UniLog.log1("called");
		ArrayList<NotifyMsgObj> msgList = new ArrayList<NotifyMsgObj>();
		if (sh == null) {
			return null;
		}
		try {
			if (StringUtils.equalsAnyIgnoreCase(sh.getURLParam("menuitem"), "DASHBOARD")) {
				/***
				 * TODO
				 *  If current login is student Sxxxxxx, load student br and check the token balance detail.
				 *  If any token balance is negative, add overdue notification e.g. "Overdue Notification. %s:%.02f",tokenCcy, tokenBalance;
				 */
				boolean isStudent = Pattern.compile("^s[0-9]{6}$",Pattern.CASE_INSENSITIVE).matcher(sh.getLoginId()).find();
				UniLog.log1("login:%s isStudent:%s", sh.getLoginId(), isStudent);
				if (isStudent) {
					BiResult brStudent = null;
					BiResult brTokenBal = null;
					try {
						brStudent = BiResultHelper.create(sh, "edu.Student", String.format("essd_sdno = '%s'", sh.getLoginId().toUpperCase()), -1, null);
						if (brStudent.next(false)) {
							brTokenBal = brStudent.getSubLink("edu.TokenBal");
							for (int i = 0; i < brTokenBal.getRowCount(); i++) {
								brTokenBal.fetch(true, i);
								String tokenCcy = brTokenBal.getCellString("tkbal_ccy");
								double tokenBalance = brTokenBal.getCellDouble("tkbal_ostqty");
								if (StringUtils.isNotBlank(tokenCcy) && tokenBalance < 0)
									msgList.add(new NotifyMsgObj(String.format("Overdue Notification. %s:%.02f",tokenCcy, tokenBalance), NotifyMsgObj.Level.warn));
							}
						}
					}
					catch (Exception ex) {
						UniLog.log1("ERROR:" + ex.getMessage());
					}
					finally {
						if (brTokenBal != null)
							brTokenBal.close();
						if (brStudent != null)
							brStudent.close();
					}
				}
			}
		}
		catch(Exception ex) {
			UniLog.log1("ERROR:" + ex.getMessage());
		}
		UniLog.log1("end msgList.size:%d", msgList.size());
		return msgList;
	}
}
