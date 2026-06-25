package com.uniinformation.zkbi;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.zkoss.mesg.Messages;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Execution;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.ForwardEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Configuration;
import org.zkoss.zul.Button;
import org.zkoss.zul.Window;
import org.zkoss.zul.mesg.MZul;

import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;

public class ZkBiFileuploadDlg extends Window {
	private LinkedList<Media> _result = new LinkedList<Media>();
	private static final String ATTR_FILEUPLOAD_TARGET = "org.zkoss.zul.Fileupload.target";
	private EventListener<UploadEvent> _listener;

	private Button clickedButton;

	/**
	 * Set the upload call back event listener
	 * @since 6.5.3
	 */
	public void setUploadListener(EventListener<UploadEvent> listener) {
		_listener = listener;
	}
	
	public void onClose(Event evt) {
   		try {
			if (evt.getData() == null)
				_result.clear();
			else {
				final Desktop desktop = Executions.getCurrent().getDesktop();
				final Configuration config = desktop.getWebApp().getConfiguration();
				if (!config.isEventThreadEnabled()) {
					if (_listener != null)
						try {
							_listener.onEvent(new UploadEvent(Events.ON_UPLOAD, clickedButton, getResult()));
						} catch (Exception e) {
							throw new UiException(e);
						}
					else
						Events.postEvent(new UploadEvent(Events.ON_UPLOAD, (Component) desktop.getAttribute(ATTR_FILEUPLOAD_TARGET), getResult()));
				}
			}
		} finally {
			detach();
		}
	}

	public void onClick(Event event) {
		UniLog.log1("event:%s", event);
		if (event instanceof ForwardEvent) {
			ForwardEvent evt = (ForwardEvent) event;
			if (evt.getOrigin().getTarget() instanceof Button) {
				clickedButton = (Button) evt.getOrigin().getTarget();
				ZkUtil.js("$('#%s').click()", getFellow("submit", true).getUuid());
			}
		}
	}
	
	/**
	 * Called when a file is received.
	 * It is used only for component development.
	 * @since 5.0.0
	 */
	public void onUpload(ForwardEvent evt) {
		_result.add(((UploadEvent) evt.getOrigin()).getMedia());
	}

	/** Returns the result.
	 * @return an array of media (length &gt;= 1), or null if nothing.
	 */
	public Media[] getResult() {
		return _result.isEmpty() ? null : _result.toArray(new Media[_result.size()]);
	}

	public void service(org.zkoss.zk.au.AuRequest request, boolean everError) {
		final String cmd = request.getCommand();
		if (cmd.equals("onRemove")) {
			_result.remove(((Integer) request.getData().get("")).intValue());
		} else
			super.service(request, everError);
	}

	public static Media get(String templ) {
		return get(templ, null, null, false);
	}
	
	public static Media get(String templ, boolean alwaysNative) {
		return get(templ, null, null, alwaysNative);
	}
	
	public static Media get(String templ, String message, String title) {
		return get(templ, message, title, false);
	}
	
	public static Media get(String templ, String message, String title, boolean alwaysNative) {
		final Media[] result = get(templ, message, title, 1, alwaysNative);
		return result != null ? result[0] : null;
	}
	
	public static Media[] get(String templ, EventListener<UploadEvent> listener) {
		return get(templ, 1, listener);
	}
	
	public static Media[] get(String templ, int max, EventListener<UploadEvent> listener) {
		return get(templ, new HashMap<String, Object>(8), null, null, max, -1, false, listener);
	}
	
	public static Media[] get(String templ, int max) {
		return get(templ, null, null, max, false);
	}

	public static Media[] get(String templ, int max, boolean alwaysNative) {
		return get(templ, null, null, max, alwaysNative);
	}
	
	public static Media[] get(String templ, String message, String title, int max) {
		return get(templ, message, title, max, false);
	}
	
	public static Media[] get(String templ, String message, String title, int max, boolean alwaysNative) {
		return get(templ, message, title, max, -1, alwaysNative);
	}
	
	public static Media[] get(String templ, String message, String title, int max, int maxsize, boolean alwaysNative) {
		return get(templ, new HashMap<String, Object>(8), message, title, max, maxsize, alwaysNative);
	}
	
	protected static Media[] get(String templ, Map<String, Object> params, String message, String title, int max, int maxsize, boolean alwaysNative) {
		return get(templ, params, message, title, max, maxsize, alwaysNative, null);
	}
	
	public static Media[] get(String templ, Map<String, Object> params, String message, String title, int max, int maxsize, boolean alwaysNative, EventListener<UploadEvent> listener) {
		return get(templ, params, message, title, null, max, maxsize, alwaysNative, listener);
	}
	
	public static Media[] get(String templ, Map<String, Object> params, String message, String title, String accept, int max, int maxsize, boolean alwaysNative, EventListener<UploadEvent> listener) {
		final Execution exec = Executions.getCurrent();
		params.put("message", message == null ? Messages.get(MZul.UPLOAD_MESSAGE) : message);
		params.put("title", title == null ? Messages.get(MZul.UPLOAD_TITLE) : title);
		params.put("max", new Integer(max == 0 ? 1 : max > 1000 ? 1000 : max < -1000 ? -1000 : max));
		params.put("accept", accept);
		params.put("native", Boolean.valueOf(alwaysNative));
		params.put("maxsize", String.valueOf(maxsize));
		params.put("listener", listener);

		final ZkBiFileuploadDlg dlg = (ZkBiFileuploadDlg) exec.createComponents(templ, null, params);
		try {
			dlg.doModal();
		} catch (Throwable ex) {
			try {
				dlg.detach();
			} catch (Throwable ex2) {
				UniLog.log(ex2);
			}
			throw UiException.Aide.wrap(ex);
		}
		return dlg.getResult();
	}
}
