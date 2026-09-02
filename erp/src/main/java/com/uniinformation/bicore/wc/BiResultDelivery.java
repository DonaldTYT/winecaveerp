package com.uniinformation.bicore.wc;

import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.BiUtil;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultDelivery extends BiResultStmov {
	private final HashMap<Integer,Double> palcCommitedHash = new HashMap<Integer,Double>();
	private final HashMap<Integer,Double> icodeCommitedHash = new HashMap<Integer,Double>();

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

	@Override
	public void clearCurrentRec() {
		super.clearCurrentRec();
		if(palcCommitedHash != null) palcCommitedHash.clear();
		if(icodeCommitedHash != null) icodeCommitedHash.clear();
	}

	public HashMap<Integer,Double> getPalcCommitedHash() {
		return palcCommitedHash;
	}

	public HashMap<Integer,Double> getIcodeCommitedHash() {
		return icodeCommitedHash;
	}

	@Override
	public void afterPickColumn(ColumnCell p_pickColumn,
			BiCellCollection p_pickedCollection, String p_pickedColumnName,
			boolean p_update) throws CellException {
		if(p_pickedCollection == null) {
			UniLog.log1("No delivery row selected; skip afterPickColumn");
			return;
		}
		if(!"vd_vcode".equals(p_pickColumn.getCellLabel())) {
			super.afterPickColumn(p_pickColumn, p_pickedCollection,
					p_pickedColumnName, p_update);
			return;
		}

		String[] addressColumnNames = {
				"vd_addr0", "vd_addr1", "vd_addr2", "vd_addr3"
		};
		StringBuilder address = new StringBuilder();
		for(String addressColumnName : addressColumnNames) {
			Cell addressCell = p_pickedCollection.testCell(addressColumnName);
			if(addressCell == null) {
				throw new CellException(addressColumnName
						+ " is missing from the selected delivery-address row");
			}
			String addressLine = addressCell.getString().trim();
			if(addressLine.length() > 0) {
				if(address.length() > 0) address.append('\n');
				address.append(addressLine);
			}
		}

		Cell deliveryAddress = p_pickColumn.getCollection()
				.testCell("stmov_deliaddr");
		if(deliveryAddress == null) {
			throw new CellException(
					"stmov_deliaddr is missing from the Delivery row");
		}
		deliveryAddress.set(address.toString());
		super.afterPickColumn(p_pickColumn, p_pickedCollection,
				p_pickedColumnName, p_update);
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
		ReturnMsg addressResult = BiUtil.copyMemoFieldToSublinkColumn(this, pcol,
				"stmov_deliaddr", "wc.MemoStm", "mm_desc");
		if(addressResult != null && !addressResult.getStatus()) return addressResult;

		RpcClient rpc = getSelectUtil().getRpcClient();
		Vector<BiCellCollection> deliveryDetails = getSubLink("wc.DeliveryDetail").getRowCollectionList();
		for (BiCellCollection detail : deliveryDetails) {
			if (detail.getCellInt("stmd_org") <= 0) {
				return new ReturnMsg(false, "Order id is required for every delivery item", true);
			}
		}
		
		String ss = pcol.getCellString("stm_ref1");
		if(StringUtils.isBlank(ss)) {
		Vector args = new Vector();
		args.add("smdnvh");
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
		}

		return new ReturnMsg(true);
	}

	@Override
	protected void afterFetch() {
		super.afterFetch();
		rebuildCommitedHashes();
		ReturnMsg result = BiUtil.copySublinkColumnToMemoField(this, getCurrentCollection(),
				"wc.MemoStm", "mm_desc", "stmov_deliaddr");
		if(result != null && !result.getStatus()) UniLog.log(result.getMsg());
	}

	private void rebuildCommitedHashes() {
		palcCommitedHash.clear();
		icodeCommitedHash.clear();
		if(!"Y".equals(getCurrentCollection().getCellString("stm_void"))) {
			BiResult deliveryDetail = getSubLink("wc.DeliveryDetail");
			if(deliveryDetail != null) {
				for(BiCellCollection detail : deliveryDetail.getRowCollectionList()) {
					int palcSid = detail.getCellInt("palc_sid");
					if(palcSid > 0) {
						Integer palcKey = Integer.valueOf(palcSid);
						Double palcQty = palcCommitedHash.get(palcKey);
						palcCommitedHash.put(palcKey, Double.valueOf(
								(palcQty == null ? 0.0 : palcQty.doubleValue())
								+ detail.getCellDouble("stmd_dqty")));
					}
					int icodeSid = detail.getCellInt("pdls_sid");
					if(icodeSid > 0) {
						Integer icodeKey = Integer.valueOf(icodeSid);
						Double icodeQty = icodeCommitedHash.get(icodeKey);
						icodeCommitedHash.put(icodeKey, Double.valueOf(
								(icodeQty == null ? 0.0 : icodeQty.doubleValue())
								+ detail.getCellDouble("stmd_dqty")));
					}
				}
			}
		}
	}

	@Override
	public boolean commitWork() throws Exception {
		boolean committed = super.commitWork();
		if(committed) rebuildCommitedHashes();
		return(committed);
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
