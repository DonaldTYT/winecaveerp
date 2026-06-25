package com.uniinformation.jx.zk;

import java.util.Calendar;
import java.util.Date;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

import com.kyoko.common.DateUtil;
import com.uniinformation.utils.UniLog;

public class ZkJxTimePickerList extends Listbox {
	public ZkJxTimePickerList() {
		setMold("select");
		for(int i=0;i<96;i++) {
			int h = i / 4;
			int m = i % 4;
			String s = String.format("%02d:%02d:00", h,m * 15);
			Date dt = DateUtil.dateTimeStrToDate(s);
			Listitem li = appendItem(
					String.format("%02d:%02d:00", h,m * 15),
					String.format("%02d:%02d:00", h,m * 15)
					);
			li.setValue(dt);
		}
		EventListener zkEventListener = new EventListener() {
			public void onEvent(Event event) throws Exception {
					UniLog.log("Time Picker Opened");
					if(getSelectedIndex() < 0) {
						Calendar cal=Calendar.getInstance();
						cal.setTime(new Date());
						int h = cal.get(Calendar.HOUR);
						int m = cal.get(Calendar.MINUTE);
//						setSelectedIndex(h * 4 + (m/15));
					}
			}	
		};
		addEventListener(Events.ON_OPEN, zkEventListener);
	}
	
	public void setSelectedItemByTime(Date p_date) {
		for(int i=0;i<getItemCount();i++) {
			Date val = (Date) getItemAtIndex(i).getValue();
			if(val.equals(p_date)) {
				setSelectedIndex(i);
				return;
			}
		}
		setSelectedIndex(-1);
	}
}
