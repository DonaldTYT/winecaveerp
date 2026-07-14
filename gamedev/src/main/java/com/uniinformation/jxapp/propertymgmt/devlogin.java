package com.uniinformation.jxapp.propertymgmt;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ZkUtil;

public class devlogin extends JxZkBiBase {
	private static final String[] lcDescFieldLabelList = { "vcol_lcdesc", "ldv_lcdesc2", "ldv_lcdesc3", "ldv_lcdesc4" };

	@Override
	public void beforeBind() {
		super.beforeBind();
		ZkUtil.addJxChangeListener(this, (field, orgvalue) -> {
			refreshLcDescListbox(field.getName(), true);
		}, lcDescFieldLabelList);
	}

	@Override
	public void bindCellCollection(BiResult p_br, int mode) {
		super.bindCellCollection(p_br, mode);
		refreshLcDescListbox(null, false);
	}
	
	private void refreshLcDescListbox(String excludeFieldLabel, boolean resetS2Listbox) {
		Arrays.stream(lcDescFieldLabelList).filter(l -> !Objects.equals(l, excludeFieldLabel)).forEach(fieldLabel -> {
			Set<String> ss = Arrays.stream(lcDescFieldLabelList).filter(l -> !l.equals(fieldLabel)).map(l -> getBr().getCellString(l)).collect(Collectors.toSet());
			String curValue = getBr().getCellString(fieldLabel);
			Listbox lb = (Listbox)jxAdd(fieldLabel).getNativeObject();
			lb.setSelectedIndex(-1);
			for (Listitem li : lb.getItems()) {
				boolean isSelect = Objects.equals(curValue, li.getLabel());
				boolean isVisible = isSelect || !ss.contains(li.getLabel()) || StringUtils.isBlank(li.getLabel());
				li.setVisible(isVisible);
				if (isSelect && isVisible)
					lb.setSelectedItem(li);
			}
			if (resetS2Listbox)
				ZkUtil.setupSelect2(lb);
		});
	}
}
