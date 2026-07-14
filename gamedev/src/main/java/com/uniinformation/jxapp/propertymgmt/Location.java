package com.uniinformation.jxapp.propertymgmt;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiMsgbox;
import com.uniinformation.zkf.propertymgmt.ZkFormEpayment;

public class Location extends JxZkBiBase {

	@Override
	public void afterBind() {
		super.afterBind();
		ZkUtil.addJxActionListener(this, field -> {
			Grid grid = (Grid)curComp.getTemplate("inputRefTemplate").create(null, null, null, null)[0];
			Textbox tbBank = ZkUtil.getFellowWithNullId(grid, "tb0");
			Textbox tbMerchantId = ZkUtil.getFellowWithNullId(grid, "tb1");
			Textbox tbTerminalNo = ZkUtil.getFellowWithNullId(grid, "tb2");
			Textbox tbOrgCode = ZkUtil.getFellowWithNullId(grid, "tb3");
			Textbox tbPublicKey = ZkUtil.getFellowWithNullId(grid, "tb4");
			Textbox tbSystemKey = ZkUtil.getFellowWithNullId(grid, "tb5");

   			int epayment = getBr().getCellInt("lc_epayment");
   			tbBank.setText(ZkFormEpayment.BANK.fromIndex(epayment).getName());
   			tbMerchantId.setText(getBr().getCellString("lc_merchantid"));
   			tbTerminalNo.setText(getBr().getCellString("lc_terminalno"));
   			if (epayment == 2)
   				tbOrgCode.setText(getBr().getCellString("lc_orgcode"));
   			else {
   				Component c = ZkUtil.closestComponent(tbOrgCode, "row");
   				c.getParent().removeChild(c);
   			}
   			tbPublicKey.setText(getBr().getCellString("lc_publickey"));
   			tbSystemKey.setText(getBr().getCellString("lc_systemkey"));

			ZkBiMsgbox.build2(grid, 0, 500, (ev, mbbt) -> {
       			if (mbbt.getIdx() == 0) {
       				getBr().getCell("lc_merchantid").set(tbMerchantId.getText().trim());
       				getBr().getCell("lc_terminalno").set(tbTerminalNo.getText().trim());
       				getBr().getCell("lc_orgcode").set(tbOrgCode.getText().trim());
       				getBr().getCell("lc_publickey").set(tbPublicKey.getText().trim());
       				getBr().getCell("lc_systemkey").set(tbSystemKey.getText().trim());
       				setupReferenceLabel();
       				setDirtyFlag(true);
       			}
       		}, "Ok", "Cancel").appendVboxStyle("margin-bottom:5px").doModal();
		}, "btReference");
		ZkUtil.addJxChangeListener(this, (field, orgvalue) -> {
			if (!setupReferenceVisible()) {
     			getBr().getCell("lc_merchantid").set("");
       			getBr().getCell("lc_terminalno").set("");
       			getBr().getCell("lc_orgcode").set("");
       			getBr().getCell("lc_publickey").set("");
       			getBr().getCell("lc_systemkey").set("");
       			setupReferenceLabel();
			}
		}, "lc_epayment");
	}

	@Override
	public void bindCellCollection(BiResult p_br, int mode) {
		super.bindCellCollection(p_br, mode);
		setupReferenceLabel();
		setupReferenceVisible();
	}
	
	private void setupReferenceLabel() {
		try {
			String merchantId = getBr().getCellString("lc_merchantid");
			String terminalNo = getBr().getCellString("lc_terminalno");
			String orgCode = getBr().getCellString("lc_orgcode");
			List<String> list = new ArrayList<>();
			if (StringUtils.isNotBlank(merchantId))
				list.add(String.format("[商戶編號：%s]", merchantId));
			if (StringUtils.isNotBlank(terminalNo))
				list.add(String.format("[終端號：%s]", terminalNo));
			if (StringUtils.isNotBlank(orgCode))
				list.add(String.format("[機構號：%s]", orgCode));
			getBr().getCell("vcol_reference").set(!list.isEmpty() ? "關聯：" + list.stream().collect(Collectors.joining("、")) : "");
		} catch (CellException e) {
			UniLog.log(e);
		}
	}
	
	private boolean setupReferenceVisible() {
		Button btReference = (Button)Selectors.find("button[id='btReference']").get(0);
		Label lbReference = (Label)Selectors.find("label[id='vcol_reference']").get(0);
		boolean isVisible = getBr().getCellInt("lc_epayment") > 1;
  		btReference.setVisible(isVisible);
  		lbReference.setVisible(isVisible);
  		return isVisible;
	}
}
