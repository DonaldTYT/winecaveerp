package com.uniinformation.jxapp.erpv4ext;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Label;
import org.zkoss.zul.Checkbox;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jx.zk.JxZkGadgetProvider;
import com.uniinformation.jx.zk.ZkJxPickInput;
import com.uniinformation.jxapp.JxSelOpt;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.GridHelper;
import com.uniinformation.webcore.SessionHelper;
import com.uniinformation.zkbi.ZkBiEventListener;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkbi.ZkBiMsgbox.ZkBiMsgboxButton;

public class Shift extends JxZkBiBase {
	private static final int SHIFT_START_ALLOWANCE = 90;
	private static final int SHIFT_END_ALLOWANCE  = 300;
	
	@Override
	public void afterBind() {
		UniLog.log1("afterBind called");
		super.afterBind();
		for (final String fs : new String[] {"emsft_sttime", "emsft_endtime"}) {
			new JxFieldChange(fs) {
				@Override
				public boolean valueChanged(JxField jxfield, String orgvalue) {
					UniLog.log1("%s valueChanged orgvalue:%s, newValue:%s, newText:%s", fs, orgvalue, jxfield.getValue(), jxfield.getText());
					try {
						String[] ss = jxfield.getText().split(":", -1);
						if (NumberUtils.toInt(ss[2]) != 0)
							jxfield.setText(String.format("%s:%s", ss[0], ss[1]));
					} catch (Exception e) {
						UniLog.log1("Error:%s", e.getMessage());
					}
					return true;
				}
			};
		}
	}

	@Override
	protected ReturnMsg beforeAdd(BiResult br) {
		ReturnMsg rtn = super.beforeAdd(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			rtn = validationRecord();
		} catch (Exception ex) {
			UniLog.log(ex);
			return new ReturnMsg(false, ex.getMessage(), true);
		}
		
		return rtn;
	}

	@Override
	protected ReturnMsg beforeUpdate(BiResult br) {
		ReturnMsg rtn = super.beforeUpdate(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);
		
		try {
			rtn = validationRecord();
		} catch (Exception ex) {
			UniLog.log(ex);
			return new ReturnMsg(false, ex.getMessage(), true);
		}
		
		return rtn;
	}
	
	private ReturnMsg validationRecord() throws Exception {
		Vector<BiCellCollection> recs = getBr().getSubLinkResult("erpv4ext.ShiftDetail");
		for (BiCellCollection cc : recs) {
			Date startTime = cc.getCell("emsftd_sttime").getDate();
			Date endTime = cc.getCell("emsftd_endtime").getDate();
			if (DateUtil.isDateNull(startTime))
				return new ReturnMsg(false, "Start Time cannot be empty", true);
			if (DateUtil.isDateNull(endTime))
				return new ReturnMsg(false, "End Time cannot be empty", true);
			if (startTime.compareTo(LeaveApplication.MAX_TIME_IN_DAY) >= 0)
				return new ReturnMsg(false, "Invalid time", true);
			if (endTime.compareTo(LeaveApplication.MAX_TIME_IN_DAY) >= 0)
				return new ReturnMsg(false, "Invalid time", true);
			if (startTime.compareTo(endTime) >= 0)
				return new ReturnMsg(false, "Start Time must be less than End Time", true);
		}
		return ReturnMsg.defaultOk;
	}
	
	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		return ListUtil.of(
			new ShiftDetailGetItemProperty(p_br.getSubLink("erpv4ext.ShiftDetail"))
		);	
	}

	/* Customized GIPI for ShiftDetail */
	private class ShiftDetailGetItemProperty extends BiGetItemProperty {
		public ShiftDetailGetItemProperty(BiResult p_br) {
			super(p_br);
			UniLog.log1("ShiftDetailGetItemProperty");
		}

		@Override
		public void onValueChanged(Object p_value, int p_ctype) {
			ColumnCell bcc = (ColumnCell) p_value;
			BiCellCollection cc = bcc.getCollection();
			UniLog.log1("onValueChanged p_ctype:%d, label:%s", p_ctype, bcc.getCellLabel());
			if (p_ctype == GIPI_VALUE_CHANGED) {
				if (StringUtils.equalsAny(bcc.getCellLabel(), "emsftd_sttime", "emsftd_endtime")) {
					try {
						LeaveApplication.zeroColumnCellDateSecond(bcc);
						Date startTime = cc.getCell("emsftd_sttime").getDate();
						Date endTime = cc.getCell("emsftd_endtime").getDate();
						if (!DateUtil.isDateNull(startTime) && !DateUtil.isDateNull(endTime)) {
							if (bcc.getDate().compareTo(LeaveApplication.MAX_TIME_IN_DAY) >= 0)
								LeaveApplication.showErrorNotification("Invalid time", bcc);
							else if (startTime.compareTo(endTime) >= 0)
								LeaveApplication.showErrorNotification("Start Time must be less than End Time", bcc);
						}
						calcMasterHours();
					}
					catch (Exception ex) {
						UniLog.log1("Error:%s", ex.getMessage());
					}
				}
			}
			if (p_ctype != GIPI_CELL_MAPPED)
				setDirtyFlag(true);
		}
	}

	@Override
	protected void afterDeleteLink(BiResult sr, int idx) {
		super.afterDeleteLink(sr, idx);
		try {
			calcMasterHours();
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}

	@Override
	protected void afterUnDeleteLink(BiResult sr,int idx) {
		super.beforeUnDeleteLink(sr, idx);
		try {
			calcMasterHours();
		} catch (Exception ex) {
			UniLog.log(ex);
		}
	}
	
	private void calcMasterHours() throws Exception {
		Vector<BiCellCollection> recs = getBr().getSubLinkResult("erpv4ext.ShiftDetail");
		Date minTime = null;
		Date maxTime = null;
		long totalTime = 0L;
		for (BiCellCollection cc : recs) {
			Date startTime = cc.getCell("emsftd_sttime").getDate();
			Date endTime = cc.getCell("emsftd_endtime").getDate();
			if (!DateUtil.isValid(startTime) || !DateUtil.isValid(endTime))
				continue;
			if (minTime == null || minTime.compareTo(startTime) > 0)
				minTime = startTime;
			if (maxTime == null || maxTime.compareTo(endTime) < 0)
				maxTime = endTime;
			totalTime += endTime.getTime() - startTime.getTime();
		}
		if (minTime != null) {
			minTime = new Date(minTime.getTime() - SHIFT_START_ALLOWANCE * 60 * 1000);
			getBr().getCell("emsft_sttime").set(minTime);
		}
		if (maxTime != null) {
			maxTime = new Date(maxTime.getTime() + SHIFT_END_ALLOWANCE * 60 * 1000);
			getBr().getCell("emsft_endtime").set(maxTime);
		}
		getBr().getCell("emsft_numhr").set(totalTime / 60000);
	}
	
	public static void setupShiftArrange(final SessionHelper sh, final JxZkBiBase biBase, final BiResult br, final String rgKey, final Map<String, Object> map) {
		ZkBiMsgboxButton[] btns = new ZkBiMsgboxButton[] {new ZkBiMsgboxButton(sh.getBtLabel("Ok")),new ZkBiMsgboxButton(sh.getBtLabel("Cancel"))};
		try {
			JxSelOpt tjxf = (JxSelOpt)map.get("jxSelOpt");
			TrGetItemProperty tgipi = (TrGetItemProperty)map.get("trGetItemProperty");

			if (tjxf == null) {
				JxZkGadgetProvider pvdr = (JxZkGadgetProvider) sh.getSessionData("jxzkgadgetprovider");
				tgipi = new TrGetItemProperty(
						new VectorUtil()
							.addElement("emsft_code")
							.addElement("emsft_name")
							.toVector()
					);
				tjxf = JxSelOpt.createJxSelOpt(pvdr);
				final JxSelOpt tjxf1 = tjxf;
				final TrGetItemProperty tgipi1 = tgipi;
				tjxf.setOnSelectAction(new JxActionListener() {
					public void actionPerformed(JxField fd) {
						Object[] rec = (Object[]) fd.getValue();
						TableRec tr = tgipi1.getTableRec();
						ZkJxPickInput comp = (ZkJxPickInput) tjxf1.getUserData();
						comp.setText((String)rec[tr.getFieldIndex("emsft_code")]);
						tjxf1.closeForm();
					}
				});
				map.put("jxSelOpt", tjxf);
				map.put("trGetItemProperty", tgipi);
			}
			final JxSelOpt tjxf1 = tjxf;
			final TrGetItemProperty tgipi1 = tgipi;

			final ZkJxPickInput[][] zjpis = new ZkJxPickInput[7][3];
			for (int i = 0; i < zjpis.length; i++) {
				for (int j = 0; j < zjpis[0].length; j++) {
					final ZkJxPickInput zjpi = new ZkJxPickInput();
					zjpi.setText(br.getCellString(String.format("shtar_sfcode%d%c", i + 1, 'a' + j)));
					zjpi.setReadonly(true);
					zjpi.setWidth("60px");
					zjpi.setPopupWidth("450px");
					zjpi.addEventListener(Events.ON_OPEN, new ZkBiEventListener<Event>() {
						@Override
						public void onZkBiEvent(Event event) throws Exception {
							UniLog.log1("zkpi open:%s", event);
							zjpi.setJxZkForm(tjxf1);
							TableRec tr = br.getSelectUtil().getQueryResult("select emsft_code, emsft_name from emshiftmaster order by emsft_code", null);
							tgipi1.setTableRec(tr);
							tjxf1.setUserData(zjpi);
							tjxf1.jxAdd("pickListBox").setItemListInterface(tgipi1);
						}
					});
					zjpis[i][j] = zjpi;
				}
			}
			final Checkbox cbNoHoliday = new Checkbox("Public holidays") {{
				setAttribute("tlkey", "lb_shiftsetup_pubholidays");
				setChecked(br.getCellInt(rgKey) == 0 ? true : br.getCell("shtar_pubhol").getBoolean());
			}};
			GridHelper gh = new GridHelper(4);
			gh.getColumn(0).setHflex("1");
			gh.getColumn(1).setWidth("80px");
			gh.getColumn(2).setWidth("80px");
			gh.getColumn(3).setWidth("80px");
			gh.addRow(new Label("lb_shiftsetup_desc") {{
				setAttribute("tlkey", "lb_shiftsetup_desc");
			}});
			gh.getRow(0).setSpans("4");
			gh.getRow(0).setHeight("50px");
			gh.addRow(new Label(sh.getLabel("Monday")), zjpis[0][0], zjpis[0][1], zjpis[0][2]);
			gh.addRow(new Label(sh.getLabel("Tuesday")), zjpis[1][0], zjpis[1][1], zjpis[1][2]);
			gh.addRow(new Label(sh.getLabel("Wednesday")), zjpis[2][0], zjpis[2][1], zjpis[2][2]);
			gh.addRow(new Label(sh.getLabel("Thursday")), zjpis[3][0], zjpis[3][1], zjpis[3][2]);
			gh.addRow(new Label(sh.getLabel("Friday")), zjpis[4][0], zjpis[4][1], zjpis[4][2]);
			gh.addRow(new Label(sh.getLabel("Saturday")), zjpis[5][0], zjpis[5][1], zjpis[5][2]);
			gh.addRow(new Label(sh.getLabel("Sunday")), zjpis[6][0], zjpis[6][1], zjpis[6][2]);
			gh.addRow(cbNoHoliday);
			gh.getRow(8).setSpans("4");
			new ZkBiMsgbox(sh).setContent(gh).setButtons(btns).setEventListener(new ZkBiEventListener<Event>(){
				@Override
				public void onZkBiEvent(Event event) throws Exception {
					ZkBiMsgboxButton btn = (ZkBiMsgboxButton) event.getTarget();
					UniLog.log1("event:%s button:[%s,%s,%d]", event, event.getTarget(), btn.getName(), btn.getIdx());
					if (StringUtils.equals(btn.getName(), sh.getBtLabel("Ok"))) {
						try {
							if (br.getCellInt(rgKey) == 0) {
								Value v = br.getView().getSchema().getUniqueRg(br, "", 12001, "shiftarrange", "shtar_rg", "");
								br.getCell(rgKey).set(v.toInt());
								br.getCell("shtar_rg").set(v.toInt());
							}
							for (int i = 0; i < zjpis.length; i++) {
								for (int j = 0; j < zjpis[0].length; j++)
									br.getCell(String.format("shtar_sfcode%d%c", i + 1, 'a' + j)).set(zjpis[i][j].getText());
							}
							br.getCell("shtar_pubhol").set(cbNoHoliday.isChecked());
							biBase.setDirtyFlag(true);
						}
						catch (Exception ex) {
							UniLog.log1("Error:%s", ex.getMessage());
							ZkBiMsgbox.show(ZkBiMsgbox.Type.error, "Error:" + ex.getMessage());
						}
					}
				}
			}).build().appendStyle("width:380px;max-width:100%").doModal();
			ZkUtil.translateAllComp(sh, gh, br.getView().getName(), br);
		} catch (Exception e) {
			UniLog.log1("setupShiftArrange Error:%s", e.getMessage());
		}
	}
}
