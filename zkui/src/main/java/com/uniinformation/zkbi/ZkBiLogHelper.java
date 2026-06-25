package com.uniinformation.zkbi;

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

import com.uniinformation.utils.MapUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

public class ZkBiLogHelper {

	public static Map<String, String> loginTagCache = MapUtil.createLRUMap(500); // key desktop id. only cache 500 desktop

	public static enum ETYPE {
		LOGIN_OK,
		LOGIN_FAIL,
		TOKENLOGIN_OK,
		TOKENLOGIN_FAIL,
		LOGOUT,
		ACCESS_PAGE,
		ACCESS_DENIED,
		SHUTDOWN,
		DEBUG_CHANGE_ID,
		DEBUG_SCHEMA_ENABLE,
		DEBUG_SCHEMA_DISABLE,
		DEBUG_JDBC_ENABLE,
		DEBUG_JDBC_DISABLE,
		LOG_BUTTON,
		LOG_BUTTON_ENABLE,
		LOG_BUTTON_DISABLE,
	};

	private static final Logger zkbiaccessLogger = LoggerFactory.getLogger("zkbiaccess");
	private static final Logger zkbidebugLogger = LoggerFactory.getLogger("zkbidebug");

	private static String safeFormat(String p_fmt, Object... p_arg) {
		if (p_fmt == null) {
			return null;
		}
		try {
			if (p_arg != null && p_arg.length > 0) {
				return String.format(p_fmt, p_arg);
			}
			return p_fmt;
		} catch (Exception ex) {
			// preserve logging flow even if format string is invalid
			return p_fmt;
		}
	}

	private static void logAccess(String p_fmt, Object... p_arg) {
		zkbiaccessLogger.info(safeFormat(p_fmt, p_arg));
	}

	public static void logAccess(SessionHelper p_sh, ETYPE p_eType) {
		logAccess(p_sh, p_eType, null);
	}

	public static void logAccess(SessionHelper p_sh, Component p_comp, ETYPE p_eType) {
		logAccess(p_sh, p_comp, p_eType, null);
	}

	public static void logEvent(SessionHelper p_sh, Event p_event, ETYPE p_eType) {
		// logAccess(p_sh, p_event == null ? null : p_event.getTarget(), p_eType, null);
		logAccess(p_sh, p_event == null ? null : p_event.getTarget(), p_eType, p_event == null ? null : p_event.getName()); // andrew220907 show event name
	}

	/***
	 * for log button event
	 * @param p_sh
	 * @param p_event
	 * @param p_eType
	 * @param p_data
	 */
	public static void logEvent(SessionHelper p_sh, Event p_event, ETYPE p_eType, String p_data) {
		logAccess(p_sh, p_event == null ? null : p_event.getTarget(), p_eType, p_data);
	}

	public static void logAccess(SessionHelper p_sh, ETYPE p_eType, String p_data) {
		ArrayList<String> logList = new ArrayList<String>();

		if (p_sh != null) {
			String loginTag = String.format("[login:%s@%s] [ip:%s]", p_sh.getLoginId(), p_sh.getAgent(), p_sh.getRemoteAddr());
			logList.add(loginTag);
		}

		if (p_eType != null) {
			logList.add(String.format("[event:%s]", p_eType));
		}

		if (!StringUtils.isBlank(p_data)) {
			logList.add(String.format("[data:%s]", p_data));
		}

		if (!logList.isEmpty()) {
			ZkBiLogHelper.logAccess(StringUtils.join(logList, " "));
		}
	}

	/***
	 * general log
	 * @param p_sh
	 * @param p_comp
	 * @param p_eType
	 * @param p_data
	 */
	public static void logAccess(SessionHelper p_sh, Component p_comp, ETYPE p_eType, String p_data) {
		ArrayList<String> logList = new ArrayList<String>();

		if (p_sh != null) {
			String loginTag = String.format("[login:%s@%s] [ip:%s]", p_sh.getLoginId(), p_sh.getAgent(), p_sh.getRemoteAddr());
			logList.add(loginTag);

			// cache the login tag
			if (p_comp != null && p_comp.getDesktop() != null && StringUtils.isNotBlank(p_comp.getDesktop().getId())) {
				loginTagCache.put(p_comp.getDesktop().getId(), loginTag);
			}
		} else {
			// obtain login tag from cache when sh is not available
			if (p_comp != null
					&& p_comp.getDesktop() != null
					&& StringUtils.isNotBlank(p_comp.getDesktop().getId())
					&& StringUtils.isNotBlank(loginTagCache.get(p_comp.getDesktop().getId()))) {
				logList.add(loginTagCache.get(p_comp.getDesktop().getId()));
			}
		}

		if (p_eType != null) {
			logList.add(String.format("[event:%s]", p_eType));
		}

		if (p_comp != null) {
			logList.add(String.format("[comp:%s]",
					(StringUtils.isNotBlank(p_comp.getId()) ? p_comp.getId() : p_comp.getClass().getSimpleName())
							+ (p_comp.getDesktop() != null ? "@" + p_comp.getDesktop().getId() : "")));
		}

		if (SessionHelper.logButtonFullNameFlag.get() && p_eType == ETYPE.LOG_BUTTON && p_comp != null) {
			logList.add(String.format("[fullname:%s]", ZkUtil.getCompFullName(p_comp)));
		}

		if (!StringUtils.isBlank(p_data)) {
			logList.add(String.format("[data:%s]", p_data));
		}

		if (!logList.isEmpty()) {
			ZkBiLogHelper.logAccess(StringUtils.join(logList, " "));
		}
	}

	public static void logDebug(String p_fmt, Object... p_arg) {
		zkbidebugLogger.debug(safeFormat(p_fmt, p_arg));
	}

	public static void logDebug(Exception p_ex) {
		zkbidebugLogger.error("ERROR", p_ex);
	}

	public static void logDebug(String p_msg, Exception p_ex) {
		zkbidebugLogger.error(p_msg, p_ex);
	}

	/*
	public static void main(String args[]){
		for (int i=0; i<20000; i++){
			ZkBiLogHelper.logAccess("test2:" + i +":" + new java.util.Date());
		}
		ZkBiLogHelper.logAccess("access2");
		ZkBiLogHelper.logAccess("access3");
		ZkBiLogHelper.logDebug("debug1");
		ZkBiLogHelper.logDebug("debug2");
		ZkBiLogHelper.logDebug("debug3");
	}
	*/
}