package com.uniinformation.jxapp.propertymgmt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Toolbarbutton;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.propertymgmt.BiResultParkingTmpLocPayment;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.ListGetItemProperty;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;
import static com.uniinformation.utils.ZkUtil.throwConsumer;
import static com.uniinformation.bicore.propertymgmt.BiResultProjectPayment.emptyCellValue;

public class ParkingTmpLocPayment extends JxZkBiBase {
	private final TrGetItemProperty.SelectorPick gipiPropertyBlock = new TrGetItemProperty.SelectorPick(Arrays.asList("col_c"));
	private final TrGetItemProperty.SelectorPick gipiPropertyFloor = new TrGetItemProperty.SelectorPick(Arrays.asList("col_d"));
	private final TrGetItemProperty.SelectorPick gipiPropertyFlat = new TrGetItemProperty.SelectorPick(Arrays.asList("col_e"));
	private final Map<String, Map<String, Set<String>>> pickContractMap = new HashMap<>();
	private final Map<CellCollection, Map<String, Set<String>>> pickContractMap2 = new HashMap<>();
	private List<Map<String, Object>> savedPayItemList;

	@Override
	public void afterBind() {
		super.afterBind();
		JxField jxfield = jxAdd("list_propertymgmt_PayParkingItem");
		if (jxfield != null) {
			Listbox lb = (Listbox)jxfield.getNativeObject();
			ZkUtil.setEventListener(lb, "onItemRendererCallback", event -> {
				Map<String, Object> m = (Map<String, Object>)event.getData();
				Listitem listItem = (Listitem)m.get("listItem");
				Object data = m.get("data");
				//int idx = (int)m.get("idx");
				//UniLog.log1("onItemRendererCallback event:%s, idx:%d", event, idx);
	          	if (data instanceof TrStatFilter)
	           		data = ((TrStatFilter)data).getTrStatIdx();
	          	BiResult brItem = getBr().getSubLink("propertymgmt.PayParkingItem");
	          	BiCellCollection bcc = brItem.getRowCollectionO(data);
	          	Listcell lc = (Listcell)listItem.getFirstChild();
	          	Toolbarbutton btn = (Toolbarbutton)lc.query("toolbarbutton[JxZkListbox.deleteItemButton='Y']");
	          	if (btn != null)
	          		btn.setVisible(bcc.getCell("col_c").getMode() != Cell.VMODE_DISPONLY);
			});
			lb.setAttribute("hasOnItemRendererCallback", true);
		}
		payment.setupScanBarcodeToPayment(this, () -> {
			try {
				return ((BiResultParkingTmpLocPayment)getBr()).validationRecord(false);
			} catch (Exception e) {
				UniLog.log(e);
				return new ReturnMsg(false, e.getMessage());
			}
		});
	}

	@Override
	public void bindCellCollection(final BiResult p_br, int mode) {
		BiResult brPayProjectItem = p_br.getSubLink("propertymgmt.PayParkingItem");
		savedPayItemList = ZkUtil.getBiResultRecordMap(brPayProjectItem);
		brPayProjectItem.getRowCollectionList().forEach(throwConsumer(cl -> {
			cl.getCell("vcol_block").set(cl.getString("pcol_c"));
			cl.getCell("vcol_floor").set(cl.getString("pcol_d"));
			cl.getCell("vcol_flat").set(cl.getString("pcol_e"));
			((BiResultParkingTmpLocPayment)p_br).calcPaidPeriodAndAmount(cl, true);
			setupBlockItemInterface(p_br, cl);
			setupFloorItemInterface(p_br, cl);
			setupFlatItemInterface(p_br, cl);
			setupParkingLocItemInterface(p_br, cl, true);
		}));
		pickContractMap.clear();
		pickContractMap2.clear();
		super.bindCellCollection(p_br, mode);
		ZkUtil.removeAllEventListener((Button)jxAdd("btAdd").getNativeObject(), "onAfterEpayment");
		payment.setupInitSelectItem(this, sessionHelper, p_br, "col_g");
	}

	@Override
	protected void formDirtyChanged() {
		super.formDirtyChanged();
		jxSetEnable("btAddForScanBarcode", jxAdd("btAdd").getEnable());
	}

	@Override
   	public void initForm(int p_mode) {
		super.initForm(p_mode);
		jxSetVisible("btAddForScanBarcode", jxAdd("btAdd").getVisible());
		jxSetVisible("btAdd", false);
   	}

	@Override
	protected ReturnMsg beforeAddLink(JxField fd, BiResult sr, CellCollection cl, int p_insIdx) {
		try {
			if (sr.getView().getName().equals("propertymgmt.PayParkingItem")) {
				BiResult br = getBr();
				setupBlockItemInterface(br, cl);
				setupFloorItemInterface(br, cl);
				setupFlatItemInterface(br, cl);
				setupParkingLocItemInterface(br, cl, false);
				BiResultParkingTmpLocPayment.setupPeriodCntItemInterface(cl, 0, 0, 0);
			}
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		return null;
	}	

	@Override
	protected void afterDeleteLink(BiResult sr, int idx) {
		super.afterDeleteLink(sr, idx);
		if (sr.getView().getName().equals("propertymgmt.PayParkingItem"))
			ZkUtil.safeRunnable(() -> ((BiResultParkingTmpLocPayment)getBr()).syncPaymentFromPayItem(), true).run();
	}

	@Override
	protected void afterUnDeleteLink(BiResult sr,int idx) {
		super.afterUnDeleteLink(sr, idx);
		if (sr.getView().getName().equals("propertymgmt.PayParkingItem"))
			ZkUtil.safeRunnable(() -> ((BiResultParkingTmpLocPayment)getBr()).syncPaymentFromPayItem(), true).run();
	}

	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		return Arrays.asList(
			new PayParkingItemGetItemProperty(p_br.getSubLink("propertymgmt.PayParkingItem"))
		);	
	}

	private class PayParkingItemGetItemProperty extends BiGetItemProperty {

		public PayParkingItemGetItemProperty(BiResult p_br) {
			super(p_br);
		}

		@Override
		public void onValueChanged(Object p_value, int p_ctype) {
			final ColumnCell bcc = (ColumnCell) p_value;
			final BiCellCollection cl = bcc.getCollection();
			UniLog.log1("onValueChanged p_ctype:%d, label:%s, mapper:%s", p_ctype, bcc.getCellLabel(), bcc.getMapper());
			if (p_ctype == GIPI_VALUE_CHANGED) {
				try {
					BiResultParkingTmpLocPayment br = (BiResultParkingTmpLocPayment)getBr();
					if (Objects.equals(bcc.getCellLabel(), "vcol_block")) {
						emptyCellValue(cl, "vcol_floor", "vcol_flat", "col_c", "col_d");
						setupFloorItemInterface(br, cl);
						setupFlatItemInterface(br, cl);
						setupParkingLocItemInterface(br, cl, false);
						BiResultParkingTmpLocPayment.setupPeriodCntItemInterface(cl, 0, 0, 0);
					} else if (Objects.equals(bcc.getCellLabel(), "vcol_floor")) {
						emptyCellValue(cl, "vcol_flat", "col_c", "col_d");
						setupFlatItemInterface(br, cl);
						setupParkingLocItemInterface(br, cl, false);
						BiResultParkingTmpLocPayment.setupPeriodCntItemInterface(cl, 0, 0, 0);
					} else if (Objects.equals(bcc.getCellLabel(), "vcol_flat")) {
						emptyCellValue(cl, "col_c", "col_d");
						setupParkingLocItemInterface(br, cl, false);
						BiResultParkingTmpLocPayment.setupPeriodCntItemInterface(cl, 0, 0, 0);
					} else if (Objects.equals(bcc.getCellLabel(), "col_c")) {
						emptyCellValue(cl, "col_d");
						setupConStartMonthItemInterface(cl);
						BiResultParkingTmpLocPayment.setupPeriodCntItemInterface(cl, 0, 0, 0);
					} else if (Objects.equals(bcc.getCellLabel(), "col_d")) {
						br.calcPaidPeriodAndAmount(cl, false);
						BiResultParkingTmpLocPayment.calcAmount(cl);
						br.syncPaymentFromPayItem();
					} else if (StringUtils.equalsAny(bcc.getCellLabel(), "col_f", "vcol_f")) {
						BiResultParkingTmpLocPayment.calcAmount(cl);
						br.syncPaymentFromPayItem();
					}
				} catch (Exception e) {
					UniLog.log(e);
				}
			}
			if (p_ctype != GIPI_CELL_MAPPED && p_ctype != GIPI_PULLDOWN_OPENED && p_ctype != GIPI_PULLDOWN_CLOSED)
				setDirtyFlag(true);
		}
	}

	@Override
	protected ReturnMsg beforeAdd(BiResult br) {
		ReturnMsg rtn = super.beforeAdd(br);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		try {
			rtn = ((BiResultParkingTmpLocPayment)getBr()).validationRecord(false);
			if (rtn.getStatus() && Objects.equals(br.getCellDate("col_a"), DateUtil.today()))
				br.getCell("col_a").set(DateUtil.now());
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.getMessage(), true);
		}
		
		return rtn;
	}

	@Override
	protected ReturnMsg afterAdd(BiResult br) {
		Events.echoEvent("onAfterEpayment", (Button)jxAdd("btAdd").getNativeObject(), br.getCellString("col_b"));
		return super.afterAdd(br);
	}

	private void setupBlockItemInterface(BiResult br, CellCollection cl) throws Exception {
		if (gipiPropertyBlock.getTableRec() == null)
			gipiPropertyBlock.setTableRec(br.getSelectUtil().getQueryResult("select distinct col_c from property where col_b = ? order by col_c", new Wherecl().appendArgument(br.getCellString("col_c"))));
		cl.getCell("vcol_block").setItemPropertyInterface(gipiPropertyBlock);
	}

	private void setupFloorItemInterface(BiResult br, CellCollection cl) throws Exception {
		gipiPropertyFloor.setTableRec(br.getSelectUtil().getQueryResult("select distinct col_d from property where col_b = ? and col_c = ? order by col_d", 
				new Wherecl().appendArgument(br.getCellString("col_c")).appendArgument(cl.getString("vcol_block"))));
		cl.getCell("vcol_floor").setItemPropertyInterface(gipiPropertyFloor);
	}

	private void setupFlatItemInterface(BiResult br, CellCollection cl) throws Exception {
		gipiPropertyFlat.setTableRec(br.getSelectUtil().getQueryResult("select distinct col_e from property where col_b = ? and col_c = ? and col_d = ? order by col_e", 
				new Wherecl().appendArgument(br.getCellString("col_c")).appendArgument(cl.getString("vcol_block")).appendArgument(cl.getString("vcol_floor"))));
		cl.getCell("vcol_flat").setItemPropertyInterface(gipiPropertyFlat);
	}

	private void setupParkingLocItemInterface(BiResult br, CellCollection cl, boolean isInit) throws Exception {
		String unit = cl.getString("vcol_unit");
		UniLog.log1("unit:%s", unit);
		ListGetItemProperty.SelectorPick gipic = new ListGetItemProperty.SelectorPick();
		ListGetItemProperty.SelectorPick gipid = new ListGetItemProperty.SelectorPick();
		if (StringUtils.isBlank(unit)) {
			gipic.setOneColValueList(new ArrayList<>());
			gipid.setOneColValueList(new ArrayList<>());
			cl.getCell("col_c").setItemPropertyInterface(gipic);
			cl.getCell("col_d").setItemPropertyInterface(gipid);
			pickContractMap2.remove(cl);
			return;
		}
		Map<String, Set<String>> map = pickContractMap.get(unit);
		if (map == null) {
			map = ZkUtil.getTableRecStream(br.getSelectUtil(), "select distinct cpt_tlcode, cpt_constartmonth from contractparkingtl where cpt_unit = ? and cpt_totalamount > cpt_paidamount ", 
						new Wherecl().appendArgument(unit)).collect(Collectors.groupingBy(c -> c.getString("cpt_tlcode"), 
																Collectors.mapping(c -> c.getString("cpt_constartmonth"), Collectors.toCollection(TreeSet::new))));
			pickContractMap.put(unit, map);
		}
		Map<String, Set<String>> newMap = new TreeMap<>(map);
		if (!isInit) {
			Map<String, Set<String>> nowMap = br.getSubLinkResult("propertymgmt.PayParkingItem").stream()
											.filter(c -> c != cl && Objects.equals(c.getString("vcol_unit"), unit) && StringUtils.isNoneBlank(c.getString("col_c")) && StringUtils.isNotBlank(c.getString("col_d")))
											.collect(Collectors.groupingBy(c -> c.getString("col_c"),
													Collectors.mapping(c -> c.getString("col_d"), Collectors.toCollection(TreeSet::new))));
			Map<String, Set<String>> savedMap = savedPayItemList.stream().filter(m -> Objects.equals(m.get("vcol_unit"), unit))
											.collect(Collectors.groupingBy(m -> (String)m.get("col_c"),
													Collectors.mapping(m -> (String)m.get("col_d"), Collectors.toCollection(TreeSet::new))));
			savedMap.entrySet().forEach(e -> {
				if (newMap.containsKey(e.getKey()))
					newMap.get(e.getKey()).addAll(e.getValue());
				else
					newMap.put(e.getKey(), e.getValue());
			});
			nowMap.entrySet().forEach(e -> {
				String k = e.getKey();
				if (newMap.containsKey(k)) {
					newMap.get(k).removeAll(e.getValue());
					if (newMap.get(k).isEmpty())
						newMap.remove(k);
				}
			});
		} else {
			String colc = cl.getString("col_c");
			String cold = cl.getString("col_d");
			if (newMap.containsKey(colc))
				newMap.get(colc).add(cold);
			else
				newMap.put(colc, Stream.of(cold).collect(Collectors.toCollection(TreeSet::new)));
		}
		pickContractMap2.put(cl, newMap);
		gipic.setOneColValueList(newMap.keySet().stream().collect(Collectors.toList()));
		gipid.setOneColValueList(new ArrayList<>());
		cl.getCell("col_c").setItemPropertyInterface(gipic);
		cl.getCell("col_d").setItemPropertyInterface(gipid);
	}

	private void setupConStartMonthItemInterface(CellCollection cl) throws Exception {
		ListGetItemProperty.SelectorPick gipid = new ListGetItemProperty.SelectorPick();
		String parkingLoc = cl.getString("col_c");
		Map<String, Set<String>> map = pickContractMap2.get(cl);
		if (map != null) {
			Set<String> list = map.get(parkingLoc);
			if (list != null)
				gipid.setOneColValueList(list.stream().collect(Collectors.toList()));
		}
		cl.getCell("col_d").setItemPropertyInterface(gipid);
	}

}
