package com.uniinformation.zkbi.edu;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jxapp.edu.Tutor;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;
import com.uniinformation.zkcomp.ZkBiButton;

public class ZkBiComposerTutor extends ZkBiComposerBase {
	Button btAddAttendance, btBatchCourseSummary;

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		UniLog.log1("doAfterCompose called");
		if (!sessionHelper.isAdminUser() && !sessionHelper.hasAccessRight("#edu")) {
			queryBar.setVisible(false);
			bottomPanelVbox.setVisible(false);
		}
	}

	@Override
    public void showListPanel() {
		super.showListPanel();
		if (!sessionHelper.isAdminUser() && !sessionHelper.hasAccessRight("#edu")) {
			queryBar.setVisible(false);
			bottomPanelVbox.setVisible(false);
		}
    }

	@Override
	protected void setupExtraButton(final BiResult result) {
		btAddAttendance = new ZkBiButton("Add Attendance", "images/icons/zkweb/063-more-25x25.png", "btAddAttendance", "Add Attendance", sessionHelper);
		abHelper.addButton(btAddAttendance,"fa-plus");
		btAddAttendance.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				final Set<Integer> tutorList = getBatchTutorRgList(result);
       			if (tutorList.isEmpty()) {
       				ZkUtil.showErrMsg("Please choose student");
       				return;
       			}
       			Tutor.doUpdateAttendance(sessionHelper, "Add Attendance", btAddAttendance, null, -1, tutorList);
			}
		});
		ZkUtil.setupBatchModeButton(btAddAttendance, batchModeToggleButton);

		//course summary batch only visible for manager/admin
		if (sessionHelper.isAdminUser() || sessionHelper.hasAccessRight("#edu") || sessionHelper.hasAccessRight("#eduadmin")) {
			btBatchCourseSummary = new ZkBiButton("Course Summary (Batch)", "images/icons/zkweb/073-menu-25x25.png", "btBatchCourseSummary", "Course Summary (Batch)", sessionHelper);
			abHelper.addButton(btBatchCourseSummary,"fa-list");
			btBatchCourseSummary.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
				public void onZkBiEvent(Event event) throws Exception {
					UniLog.log1("event:%s", event);
					Tutor.showCourseSummary(sessionHelper, btBatchCourseSummary, null, null, null);
				}
			});
		}
	}

	private Set<Integer> getBatchTutorRgList(BiResult result) {
		final Set<Integer> tutorList = new LinkedHashSet<Integer>();
		Set selection = listModelList.getSelection();
       	for (Iterator it = selection.iterator(); it.hasNext();) {
       		Object o = it.next();
         	Object ts = o;
          	if (ts instanceof TrStatFilter)
            	ts = ((TrStatFilter)ts).getTrStatIdx();
       		CellCollection cc = result.getRowCollectionO(ts);
       		int rg = cc.getCellInt("estt_rg");
       		String ttNo = cc.getCellString("estt_ttno");
       		String name = cc.getCellString("estt_name");
       		UniLog.log1("rg:%d, ttNo:%s, name:%s", rg, ttNo, name);
       		tutorList.add(rg);
       	}
       	return tutorList;
	}
}
