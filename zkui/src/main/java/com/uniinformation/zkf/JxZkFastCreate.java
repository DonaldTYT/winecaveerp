package com.uniinformation.zkf;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiActionListener;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;

/**
 * Opens a small ZUL form for adding one record to a BI view.
 */
public final class JxZkFastCreate {
	public static final String CREATE_BUTTON_ID = "btCreate";
	public static final String CANCEL_BUTTON_ID = "btCancel";

	private JxZkFastCreate() {
	}

	public static void open(SessionHelper p_sh, SelectUtil p_su, String p_viewName, String p_zkfName) {
		open(p_sh, p_su, p_viewName, p_zkfName, null);
	}

	public static void open(SessionHelper p_sh, SelectUtil p_su, String p_viewName, String p_zkfName,
			final BiActionListener<BiResult> p_onCreated) {
		if(p_sh == null) throw new IllegalArgumentException("SessionHelper is required");
		if(StringUtils.isBlank(p_viewName)) throw new IllegalArgumentException("viewName is required");
		if(StringUtils.isBlank(p_zkfName)) throw new IllegalArgumentException("zkfName is required");

		try {
			BiView view = p_sh.getBiSchema().getViewByName(p_viewName);
			if(view == null) throw new IllegalArgumentException("BI view not found: " + p_viewName);

			final BiResult result = view.newBiResult(p_su, p_sh.getLoginId(), null, null, p_sh);
			if(result == null) throw new IllegalStateException("Unable to create BiResult: " + p_viewName);
			result.clearCurrentRec();

			final ZkForm form = new ZkForm(null, p_zkfName);
			if(form.getRootComponent() == null) throw new IllegalStateException("Unable to create ZK form: " + p_zkfName);

			form.doModal(result.getCurrentCollection(), new EventListener<Event>() {
				@Override
				public void onEvent(Event p_event) throws Exception {
					String buttonId = p_event.getTarget().getId();
					if(CANCEL_BUTTON_ID.equals(buttonId)) {
						closeAndDetach(form);
						return;
					}
					if(!CREATE_BUTTON_ID.equals(buttonId)) return;

					ReturnMsg rtn = result.addCurrent();
					if(rtn != null && !rtn.getStatus()) {
						ZkUtil.showErrMsg(rtn.getMsg());
						return;
					}

					closeAndDetach(form);
					if(p_onCreated != null) p_onCreated.actionPerformed(result);
				}
			});
		} catch(Exception ex) {
			UniLog.log(ex);
			ZkUtil.showErrMsg(ex.getMessage() == null ? ex.toString() : ex.getMessage());
		}
	}

	private static void closeAndDetach(ZkForm p_form) {
		Component root = p_form.getRootComponent();
		p_form.exitModal();
		if(root != null && root.getParent() != null) root.detach();
	}
}
