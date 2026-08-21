package com.uniinformation.bicore.wc;

import java.util.Vector;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultDelivery extends BiResultStmov {
	private static final DeliveryOptionInfo[] DELIVERY_OPTIONS = {
			new DeliveryOptionInfo("Bubble wrap", "氣泡膜", "50300"),
			new DeliveryOptionInfo("Inflatable bottle bag", "充氣瓶袋", "50300"),
			new DeliveryOptionInfo("Bubble wine bag", "泡泡酒袋", "50300"),
			new DeliveryOptionInfo("Self Pick Up", "自取", "50300"),
			new DeliveryOptionInfo("Delivery", "送貨", "62800"),
			new DeliveryOptionInfo("urgent fee", "urgent fee", "50300"),
			new DeliveryOptionInfo("Carton Case", "紙箱", "50300"),
			new DeliveryOptionInfo("Foam Box", "飛機空運箱", "50300"),
			new DeliveryOptionInfo("Label Sticking", "標籤張貼", "50300"),
			new DeliveryOptionInfo("Wooden Case Opening", "木箱開封", "50300"),
			new DeliveryOptionInfo("Palletization", "卡板自取", "50300"),
			new DeliveryOptionInfo("Palletized Delivery", "以卡板交付", "50300")
	};

	public static final class DeliveryOptionInfo {
		private final String englishName;
		private final String chineseName;
		private final String accountCode;

		private DeliveryOptionInfo(String englishName, String chineseName, String accountCode) {
			this.englishName = englishName;
			this.chineseName = chineseName;
			this.accountCode = accountCode;
		}

		public String getEnglishName() {
			return englishName;
		}

		public String getChineseName() {
			return chineseName;
		}

		public String getAccountCode() {
			return accountCode;
		}
	}

	public BiResultDelivery(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
	}

	/**
	 * Returns the hard-coded option description and account code from deliopt.inc.
	 *
	 * @param optionIndex delivery option index (0 through 11)
	 * @return option metadata
	 * @throws IllegalArgumentException if the option index is invalid
	 */
	public DeliveryOptionInfo getDeliveryOption(int optionIndex) {
		if (optionIndex < 0 || optionIndex >= DELIVERY_OPTIONS.length) {
			throw new IllegalArgumentException("Invalid delivery option index: " + optionIndex);
		}
		return DELIVERY_OPTIONS[optionIndex];
	}

	@Override
	protected ReturnMsg biBeforeAddUpdateCurrent(BiCellCollection pcol, boolean isUpdate) {
		RpcClient rpc = getSelectUtil().getRpcClient();
		Vector<BiCellCollection> deliveryDetails = getSubLink("wc.DeliveryDetail").getRowCollectionList();
		for (BiCellCollection detail : deliveryDetails) {
			if (detail.getCellInt("stmd_org") <= 0) {
				return new ReturnMsg(false, "Order id is required for every delivery item", true);
			}
		}

		Vector args = new Vector();
		args.add("smomvh");
		args.add(pcol.getCell("stm_date").getDate());
		Value value = rpc.callSegment("erpv3GetrgByControl", args);
		if (value == null || !value.toString().startsWith("OK")) {
			return new ReturnMsg(false, "Unknown Error", true);
		}
		try {
			pcol.getCell("stm_ref1").set(value.toString().substring(4).trim());
		} catch (CellException ex) {
			UniLog.log(ex);
			return new ReturnMsg(false, "Unknown Error", true);
		}

		return new ReturnMsg(true);
	}

	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection pcol, boolean isUpdate) {
		if (deliveryOptionsChanged(pcol)) {
			Vector args = new Vector();
			args.add(pcol.getCellInt("stm_mrg"));
			args.add(Erpv4Config.getDefaultCoCode(getSessionHelper()));
			Value value = getSelectUtil().getRpcClient().callSegment("genDeliOptInvoice", args);
			if (value == null || !value.toString().startsWith("OK")) {
				String message = value == null ? "Unknown Error" : value.toString();
				return new ReturnMsg(false, "Generate delivery option invoice failed: " + message, true);
			}
		}

		return super.biAfterAddUpdateCurrent(pcol, isUpdate);
	}

	private boolean deliveryOptionsChanged(BiCellCollection pcol) {
		for (int optionIndex = 0; optionIndex < DELIVERY_OPTIONS.length; optionIndex++) {
			if (isDirty(pcol, "delio_qty" + optionIndex)
					|| isDirty(pcol, "delio_amt" + optionIndex)
					|| isDirty(pcol, "delio_opt" + optionIndex)) {
				return true;
			}
		}
		return false;
	}

	private boolean isDirty(BiCellCollection pcol, String cellName) {
		Cell cell = pcol.testCell(cellName);
		return cell != null && cell.isDirty();
	}
}
