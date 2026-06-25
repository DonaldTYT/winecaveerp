package com.uniinformation.zkbi.edu;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.edu.ProcessScanLog;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerScanLog extends ZkBiComposerBase {
	Button btUpdateAttendance, btResetCSMap, btWakeup, btDumpCSMap;

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("called");
	}

	@Override
	protected void setupExtraButton(BiResult result) {
		btUpdateAttendance = new ZkBiButton("Update Attendance", "images/icons/zkweb/063-more-25x25.png", "btAddAttendance", "Update Attendance", sessionHelper);
		abHelper.addButton(btUpdateAttendance,"fa-plus");
		btUpdateAttendance.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				ProcessScanLog o = ProcessScanLog.getProcessScanLogObject();
				if (o == null) {
					ZkUtil.showErrMsg("Cannot call update attendance function");
					return;
				}
				ReturnMsg rtn = o.updateAttend();
				if (rtn.getStatus())
					ZkUtil.showMsg("Update attendance done");
				else
					ZkUtil.showErrMsg("Update attendance fail: %s", rtn.getMsg());
			}
		});

		if (sessionHelper.isAdminUser()) {
			btResetCSMap = new ZkBiButton("Reset CSMap", null, "btResetCSMap", "Reset CSMap", sessionHelper);
			abHelper.addButton(btResetCSMap);
			btResetCSMap.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				public void onZkBiEvent(Event event) throws Exception {
					UniLog.log1("event:%s", event);
					ZkUtil.showMsg("Reset CSMap called");
					ProcessScanLog.setCSMapDirty();
				}
			});
		}
		
		//andrew220328 add wakeup button debug only
		if (sessionHelper.isAdminUser()) {
			btWakeup = new ZkBiButton("Wakeup", null, "btWakeup", "Wakeup", sessionHelper);
			abHelper.addButton(btWakeup);
			btWakeup.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				public void onZkBiEvent(Event event) throws Exception {
					UniLog.log1("event:%s", event);
					ZkUtil.showMsg("Wakeup called");
					ProcessScanLog.wakeup();
				}
			});
			
			btDumpCSMap = new ZkBiButton("DumpCSMap", null, "btDumpCSMap", "DumpCSMap", sessionHelper);
			abHelper.addButton(btDumpCSMap);
			btDumpCSMap.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				public void onZkBiEvent(Event event) throws Exception {
					UniLog.log1("event:%s", event);
					ZkUtil.showMsg("DumpCSMap called");
					ProcessScanLog.dumpCSMap();
				}
			});
		}
	}
}
