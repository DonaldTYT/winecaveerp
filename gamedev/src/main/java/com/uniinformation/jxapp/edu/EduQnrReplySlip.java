package com.uniinformation.jxapp.edu;

import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.CheckEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Radiogroup;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellCollectionToJsonInterface;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.ZkComposerBase;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;

public class EduQnrReplySlip extends ZkComposerBase {
	public static final String QNR_TYPE = "replyslip";

	@Wire
	Include asPage;

	Row rowButtons;
	Button btSubmit, btCancel;
	
	Checkbox ca_001, ca_002, ca_003, ca_004, ca_005, ca_006;
	Radiogroup q1_001, q2_001;
	Textbox q3_001, q3_002;
	Checkbox q3_003, q3_004, q3_005;
	Textbox q3_003_1, q3_003_2, q3_004_1, q3_004_2;

	@Override
	public void doAfterCompose(Component p_comp) throws Exception {
		super.doAfterCompose(p_comp);
		if (!accessOkFlag) {
			return;
		}
		UniLog.log("doAfterCompose EduQnr");
		
		rowButtons = (Row)asPage.query("#rowButtons");
		btSubmit = (Button)asPage.query("#btSubmit");
		btCancel = (Button)asPage.query("#btCancel");
		ca_001 = (Checkbox)asPage.query("#ca_001");
		ca_002 = (Checkbox)asPage.query("#ca_002");
		ca_003 = (Checkbox)asPage.query("#ca_003");
		ca_004 = (Checkbox)asPage.query("#ca_004");
		ca_005 = (Checkbox)asPage.query("#ca_005");
		ca_006 = (Checkbox)asPage.query("#ca_006");
		q1_001 = (Radiogroup)asPage.query("#q1_001");
		q2_001 = (Radiogroup)asPage.query("#q2_001");
		q3_001 = (Textbox)asPage.query("#q3_001");
		q3_002 = (Textbox)asPage.query("#q3_002");
		q3_003 = (Checkbox)asPage.query("#q3_003");
		q3_004 = (Checkbox)asPage.query("#q3_004");
		q3_005 = (Checkbox)asPage.query("#q3_005");
		q3_003_1 = (Textbox)asPage.query("#q3_003_1");
		q3_003_2 = (Textbox)asPage.query("#q3_003_2");
		q3_004_1 = (Textbox)asPage.query("#q3_004_1");
		q3_004_2 = (Textbox)asPage.query("#q3_004_2");
		
		rowButtons.setVisible(true);
		
		btCancel.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				ZkUtil.js("changeUrl('custom_menu.html?menuitem=DASHBOARD&agent=edu',true)");
			}
		});
		
		if (!sessionHelper.getAllowReplySlip()) {
			Events.echoEvent(Events.ON_CLICK, btCancel, null);
			return;
		}
		
		final int studentRg = getStudentRg();
		if (studentRg <= 0) {
			ZkBiMsgbox.show("Invalid student login",new String[]{sessionHelper.getBtLabel("OK")},new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					Events.echoEvent(Events.ON_CLICK, btCancel, null);
				}
			});
			return;
		}
		
		
		for (Component comp : new Component[] {ca_001, ca_002, ca_003, ca_004, ca_005, ca_006, q1_001, q2_001, q3_001, q3_002, q3_003, q3_004, q3_005, q3_003_1, q3_003_2, q3_004_1, q3_004_2}) {
			comp.addEventListener(Events.ON_CHECK, new ZkBiEventListener<CheckEvent>() {
				@Override
				public void onZkBiEvent(CheckEvent event) throws Exception {
					UniLog.log1("event:%s", event);
					setDirty(true);
				}
			});
		}
		
		btSubmit.addEventListener(Events.ON_CLICK, new ZkBiEventListener<Event>() {
			@Override
			public void onZkBiEvent(Event event) throws Exception {
				UniLog.log1("event:%s", event);
				JSONObject jo = new JSONObject()
					.put("ca_001", ca_001.isChecked())
					.put("ca_002", ca_002.isChecked())
					.put("ca_003", ca_003.isChecked())
					.put("ca_004", ca_004.isChecked())
					.put("ca_005", ca_005.isChecked())
					.put("ca_006", ca_006.isChecked());
				if (q1_001.getSelectedIndex() >= 0)
					jo.put("q1_001", q1_001.getSelectedIndex());
				if (q2_001.getSelectedIndex() >= 0)
					jo.put("q2_001", q2_001.getSelectedIndex());
				jo.put("q3_001", q3_001.getText())
					.put("q3_002", q3_002.getText())
					.put("q3_003", q3_003.isChecked())
					.put("q3_003_1", q3_003_1.getText())
					.put("q3_003_2", q3_003_2.getText())
					.put("q3_004", q3_004.isChecked())
					.put("q3_004_1", q3_004_1.getText())
					.put("q3_004_2", q3_004_2.getText())
					.put("q3_005", q3_005.isChecked());
				String errMsg = validJsonObject(jo);
				if (errMsg != null) {
					ZkUtil.showErrMsg(errMsg);
					return;
				}
				
				BiResult brEduQnr = null;
				ReturnMsg rtn = null;
				try {
					brEduQnr = sessionHelper.newBiResult("edu.EduQnr");
					brEduQnr.clearCurrentRec();
					brEduQnr.getCell("eqnr_sdrg").set(studentRg);
					brEduQnr.getCell("eqnr_qnrtype").set(QNR_TYPE);
					brEduQnr.getCell("eqnr_status").set("New");
					brEduQnr.getCell("eqnr_ctime").set(DateUtil.now());
					brEduQnr.getCell("eqnr_utime").set(DateUtil.now());
					CellCollection col = brEduQnr.getCurrentCollection().clearCollection("eqnr_jsoncc");
					CellCollectionToJsonInterface.JSONObjectToCellCollection(col, jo);
					rtn = brEduQnr.addCurrent();
					if (!rtn.getStatus())
						throw new Exception(rtn.getMsg());
					brEduQnr.commitWork();
					setDirty(false);
					btSubmit.setDisabled(true);
					ZkUtil.showMsg("Reply Slip sent successfully");
					ZkUtil.delayPostEvent(Events.ON_CLICK, btCancel, null, 3000);
				}
				catch (Exception e) {
					e.printStackTrace();
					if (brEduQnr != null)
						brEduQnr.rollbackWork();
					ZkUtil.errMsg("Error: %s", e.getMessage());
				}
				finally {
					brEduQnr.close();
				}
			}
		});
	}

	@Override
	protected boolean validateURL(String p_url) {
		return true;
	}
	
	private int getStudentRg() {
		String studentCode = sessionHelper.getLoginId().toUpperCase();
		BiResult brStudent = null;
		try {
			brStudent = BiResultHelper.create(sessionHelper, "edu.Student", String.format("essd_sdno = '%s'", studentCode), -1, null);
			if (brStudent.next())
				return brStudent.getCellInt("essd_rg");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			if (brStudent != null)
				brStudent.close();
		}
		return -1;
	}
	
	public static String validJsonObject(JSONObject jo) {
		int checkCaCount = 0;
		for (String s : new String[] {"ca_001", "ca_002", "ca_003", "ca_004", "ca_005", "ca_006"}) {
			if (jo.optBoolean(s))
				checkCaCount++;
		}
		if (checkCaCount == 0)
			return "Class Attendings need to pick at least one item";
		for (String s : new String[] {"q1_001", "q2_001"}) {
			if (!jo.has(s) || (jo.optInt(s) != 0 && jo.optInt(s) != 1))
				return "Q1 Q2 radio button must be choose yes or no";
		}
		return null;
	}

	private static void setDirty(boolean isDirty) {
		Clients.evalJavaScript(String.format("if (typeof showEditing !== 'undefined'){showEditing(%s);}", isDirty ? "true" : "false"));
		Clients.confirmClose(isDirty ? "Are you sure to leave?" : null);
	}
}
