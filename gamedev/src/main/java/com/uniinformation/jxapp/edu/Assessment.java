package com.uniinformation.jxapp.edu;

import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.Clients;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ListUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;

public class Assessment extends JxZkBiBase {
	@Override
	public void afterBind() {
		UniLog.log1("afterBind called");
		super.afterBind();

		new JxFieldChange("essnas_fee") {
			@Override
			public boolean valueChanged(JxField jxfield, String orgvalue) {
				UniLog.log1("essnas_fee valueChanged orgvalue:%s, newValue:%s", orgvalue, jxfield.getValue());
				if (curMode == JxZkBiBase.MODE_UPDATE) {
					Component comp = (Component)jxfield.getNativeObject();
					String initValue = (String)comp.getAttribute("initValue");
					String newValue = (String)jxfield.getValue();
					double dInitValue = NumberUtils.toDouble(initValue);
					double dNewValue = NumberUtils.toDouble(newValue);
					if (dInitValue != 0 && dInitValue != dNewValue)
						Clients.showNotification(String.format("Warning: <br>Session Fee changed from [%s] to [%s].", initValue, newValue), "warning", comp, "end_center", 5000, true); 
				}
				return true;
			}
		};
		new JxFieldChange("tkccy_name") {
			@Override
			public boolean valueChanged(JxField jxfield, String orgvalue) {
				UniLog.log1("tkccy_name valueChanged orgvalue:%s, newValue:%s", orgvalue, jxfield.getValue());
				if (curMode == JxZkBiBase.MODE_UPDATE) {
					Component comp = (Component)jxfield.getNativeObject();

					JxField ccyField = jxAdd("essnas_tokenccy");
					Component ccyComp = (Component)ccyField.getNativeObject();
					String ccyInitValue = (String)ccyComp.getAttribute("initValue");
					String ccyNewValue = (String)ccyField.getValue();
					UniLog.log1("eaav0_tokenccy initValue:%s, newValue:%s", ccyInitValue, ccyNewValue);

					if (StringUtils.isNotBlank(ccyInitValue) && !StringUtils.equals(ccyNewValue, ccyInitValue))
						Clients.showNotification(String.format("Warning: <br>Token changed from [%s] to [%s].", ccyInitValue, ccyNewValue), "warning", comp, "end_center", 5000, true); 
				}
				return true;
			}
		};
	}
	
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		UniLog.log1("bindCellCollection called");
		super.bindCellCollection(p_br, mode);
		if (mode == JxZkBiBase.MODE_ADD) {
			getBr().getCell("essnas_sttime").setSilent(DateUtil.dateTimeStrToDate("09:00:00"));
			getBr().getCell("essnas_endtime").setSilent(DateUtil.dateTimeStrToDate("10:00:00"));
			getBr().getCell("essnas_date").setSilent(new Date());
			getBr().getCell("essnas_name").setSilent("Assessment");
		}
		if (!sessionHelper.isAdminUser() && !sessionHelper.hasAccessRight("#edu") && !sessionHelper.hasAccessRight("#eduadmin")) {
			jxSetEnable("btPrevious", false);
			jxSetEnable("btNext", false);
			jxSetEnable("essnas_status", false);
		}

		JxField f = jxAdd("essnas_fee");
		((Component)f.getNativeObject()).setAttribute("initValue", f.getValue());
		f = jxAdd("tkccy_name");
		((Component)f.getNativeObject()).setAttribute("initValue", f.getValue());
		f = jxAdd("essnas_tokenccy");
		((Component)f.getNativeObject()).setAttribute("initValue", f.getValue());
	}
	
	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		//handle open student record button
		return ListUtil.of(new BiGetItemProperty(p_br.getSubLink("edu.Attendance")) {
			@Override
			public void onValueChanged(Object p_value,int p_ctype) {
				ColumnCell bcc = (ColumnCell) p_value;
				UniLog.log1("onValueChanged p_value:%s, p_ctype:%d, type:%d", p_value, p_ctype, bcc.getType());
				if(p_ctype == BiGetItemProperty.GIPI_VALUE_CHANGED && bcc.getCellLabel().equals("esatsd_tostudent")){
					int sdrg = bcc.getCollection().getCellInt("essd_rg");
					UniLog.log1("%s clicked sdrg:%d", bcc.getCellLabel(), sdrg);
					if (sdrg <= 0) {
						UniLog.log1("invalid sdrg");
						return;
					}
					try {
						JSONObject jo = new JSONObject();
						JSONArray ja = new JSONArray();
						BiView pov = getBr().getView().getSchema().getViewByName("edu.Student");
						ja.put(pov.getTable().getName());
						jo.put("tablist", ja);
						jo.put("wherestr", "essd_rg = " + sdrg);
						//String key = sessionHelper.putOneTimeData(jo);
						String key = sessionHelper.putOneTimeData(jo,true);
						//ZkUtil.js("openNewTab('%s')", "zkbiloader.html?action=update&viewid=edu.Student&page_id=student_01&zul=zkbiloader.zul&prefix=zkbi&composer=edu.ZkBiComposerStudent&closetab=Y&querycondition="+key);
						ZkUtil.js("openNewTab('%s')", "zkbiloader.html?action=update&viewid=edu.Student&page_id=student_01&zul=zkbiloader.zul&prefix=zkbi&composer=edu.ZkBiComposerStudent&closetab=Y&sidemenu=N&querycondition="+key);
					}
					catch(Exception ex) {
						ex.printStackTrace();
					}
				}
				if (p_ctype != GIPI_CELL_MAPPED) {
					if (!StringUtils.equals(bcc.getBiColumn().getColumnType(), "button")) {
						setDirtyFlag(true);
					}
				}
			}
		});	
	}
}