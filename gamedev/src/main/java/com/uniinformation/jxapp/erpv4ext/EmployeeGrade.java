package com.uniinformation.jxapp.erpv4ext;

import java.util.HashMap;
import java.util.Map;

import com.uniinformation.jx.JxActionListener;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;

public class EmployeeGrade extends JxZkBiBase {
	private Map<String, Object> setupShiftArrangeMap = new HashMap<String, Object>();

	@Override
	public void afterBind() {
		UniLog.log1("afterBind called");
		super.afterBind();
		jxAdd("btOfficeHour").addActionListener(new JxActionListener() {
			@Override
			public void actionPerformed(JxField field) {
				UniLog.log("click btOfficeHour");
				Shift.setupShiftArrange(sessionHelper, EmployeeGrade.this, getBr(), "gdmt_shtrg", setupShiftArrangeMap);
			}
		});
	}
}
