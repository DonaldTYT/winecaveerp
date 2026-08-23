package com.uniinformation.jxapp;

import java.util.HashSet;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Window;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiActionListener;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.JxZkBiBaseCallback;

/**
 * Opens a BI view in the standard Jx/ZK detail editor for adding one record.
 * Components, option lists and pick inputs are created from the BiColumn
 * metadata by {@link JxZkBiBase#buildDetailWindow}.
 */
public final class JxZkBiFastCreate {
	public static final String FAST_CREATE_FORM_ATTRIBUTE = "JxZkBiFastCreate.form";
	public static final String FAST_CREATE_RESULT_ATTRIBUTE = "JxZkBiFastCreate.result";
	private static final Map<BiResult,JxZkBiBase> FORM_BY_RESULT =
			new WeakHashMap<BiResult,JxZkBiBase>();

	private JxZkBiFastCreate() {
	}

	public static JxZkBiBase getCurrentForm(Component p_component) {
		Component component = p_component;
		while(component != null) {
			Object form = component.getAttribute(FAST_CREATE_FORM_ATTRIBUTE);
			if(form instanceof JxZkBiBase) return (JxZkBiBase) form;
			component = component.getParent();
		}
		return null;
	}

	public static JxZkBiBase getForm(BiResult p_result) {
		if(p_result == null) return null;
		synchronized(FORM_BY_RESULT) {
			return FORM_BY_RESULT.get(p_result);
		}
	}

	public static boolean isFastCreateForm(JxZkBiBase p_form) {
		return p_form != null && p_form.isFastCreateMode();
	}

	public static boolean isFastCreateContainer(Component p_component) {
		Component component = p_component;
		while(component != null) {
			if(component.getAttribute(FAST_CREATE_RESULT_ATTRIBUTE) instanceof BiResult) return true;
			component = component.getParent();
		}
		return false;
	}

	public static void open(SessionHelper p_sh, SelectUtil p_su, String p_viewName) {
		open(p_sh, p_su, p_viewName, null);
	}

	public static void open(SessionHelper p_sh, SelectUtil p_su, String p_viewName,
			final BiActionListener<BiResult> p_onCreated) {
		if(p_sh == null) throw new IllegalArgumentException("SessionHelper is required");
		if(StringUtils.isBlank(p_viewName)) throw new IllegalArgumentException("viewName is required");

		try {
			BiView view = p_sh.getBiSchema().getViewByName(p_viewName);
			if(view == null) throw new IllegalArgumentException("BI view not found: " + p_viewName);

			final BiResult result = view.newBiResult(p_su, p_sh.getLoginId(), null, null, p_sh);
			if(result == null) throw new IllegalStateException("Unable to create BiResult: " + p_viewName);
			result.clearCurrentRec();
			result.clearLastUpdate();

			final Window popup = new Window();
			popup.setTitle(p_sh.getLabel("Create") + " - " + p_sh.getLabel(view.getName()));
			popup.setBorder("normal");
			popup.setWidth(p_sh.isMobile() ? "100vw" : "760px");
			popup.setHeight(p_sh.isMobile() ? "100vh" : "90%");
			popup.setPosition("center");
			popup.setSizable(true);
			popup.setMaximizable(true);
			popup.setClosable(false);
			popup.setContentStyle("overflow:auto;");
			popup.setAttribute(FAST_CREATE_RESULT_ATTRIBUTE, result);
			popup.setParent(ZkUtil.getMainComp());

			final boolean[] created = new boolean[] { false };
			JxZkBiBaseCallback callback = new JxZkBiBaseCallback() {
				@Override
				public void biBaseRefreshListitems(Object p_dataObj) {
				}

				@Override
				public void biBaseRefresh(BiResult p_result) {
					if(!created[0]) {
						created[0] = true;
						if(p_onCreated != null) p_onCreated.actionPerformed(p_result);
					}
				}

				@Override
				public void biBaseOpen() {
				}

				@Override
				public void biBaseClose(BiResult p_result) {
					synchronized(FORM_BY_RESULT) {
						FORM_BY_RESULT.remove(p_result);
					}
					if(popup.getParent() != null) popup.detach();
				}

				@Override
				public ReturnMsg fetchNext(BiResult p_result) {
					return null;
				}

				@Override
				public ReturnMsg fetchPrevious(BiResult p_result) {
					return null;
				}

				@Override
				public Boolean hasNextRec() {
					return Boolean.FALSE;
				}

				@Override
				public Boolean hasPrevRec() {
					return Boolean.FALSE;
				}

				@Override
				public String getExtraInfo() {
					return null;
				}

				@Override
				public HashSet<BiColumn> getVisibleColumns(BiResult p_result) {
					return null;
				}
			};

			JxZkBiBase form = JxZkBiBase.buildDetailWindow(
					result, popup, p_sh.isMobile(), true, callback);
			if(form == null) {
				popup.detach();
				throw new IllegalStateException("Unable to create Jx/ZK form: " + p_viewName);
			}

			form.setFastCreateMode(true);
			popup.setAttribute(FAST_CREATE_FORM_ATTRIBUTE, form);
			synchronized(FORM_BY_RESULT) {
				FORM_BY_RESULT.put(result, form);
			}
			form.setAddAndClose(JxZkBiBase.CloseAction.Close);
			form.setIsMobile(p_sh.isMobile());
			form.bindCellCollection(result, JxZkBiBase.MODE_ADD);
			for(Component component : Selectors.find(popup, ".zkbi-detail-toolbar-leftarea")) {
				component.setVisible(false);
			}
			form.doModalAdd();
		} catch(Exception ex) {
			UniLog.log(ex);
			ZkUtil.showErrMsg(ex.getMessage() == null ? ex.toString() : ex.getMessage());
		}
	}
}
