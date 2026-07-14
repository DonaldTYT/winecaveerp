package com.uniinformation.jxapp.propertymgmt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.zkoss.lang.Objects;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Toolbarbutton;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiGetItemProperty;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.bicore.propertymgmt.BiResultProjectPayment;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.jx.JxField;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.GipiNamedItemList;
import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.TrGetItemProperty;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.zkbi.ZkBiSearchHelper.TrStatFilter;

import static com.uniinformation.utils.ZkUtil.throwConsumer;

public class ProjectPayment extends JxZkBiBase {
	//private final TrGetItemProperty.SelectorPick gipiPropertyUnit = new TrGetItemProperty.SelectorPick(Arrays.asList("key_a"));
	private final TrGetItemProperty.SelectorPick gipiPropertyBlock = new TrGetItemProperty.SelectorPick(Arrays.asList("col_c"));
	private final TrGetItemProperty.SelectorPick gipiPropertyFloor = new TrGetItemProperty.SelectorPick(Arrays.asList("col_d"));
	private final TrGetItemProperty.SelectorPick gipiPropertyFlat = new TrGetItemProperty.SelectorPick(Arrays.asList("col_e"));
	private final Map<String, Map<String, String>> pickProjectNameMap = new HashMap<>();
	private List<Map<String, Object>> savedPayItemList;
	
	@Override
	public void afterBind() {
		super.afterBind();
		JxField jxfield = jxAdd("list_propertymgmt_PayProjectItem");
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
	          	BiResult brItem = getBr().getSubLink("propertymgmt.PayProjectItem");
	          	BiCellCollection bcc = brItem.getRowCollectionO(data);
	          	Listcell lc = (Listcell)listItem.getFirstChild();
	          	Toolbarbutton btn = (Toolbarbutton)lc.query("toolbarbutton[JxZkListbox.deleteItemButton='Y']");
	          	if (btn != null)
	          		btn.setVisible(bcc.getCell("col_d").getMode() != Cell.VMODE_DISPONLY);
			});
			lb.setAttribute("hasOnItemRendererCallback", true);
		}
		payment.setupScanBarcodeToPayment(this, () -> {
			try {
				return ((BiResultProjectPayment)getBr()).validationRecord(false);
			} catch (Exception e) {
				UniLog.log(e);
				return new ReturnMsg(false, e.getMessage());
			}
		});
	}

	@Override
	public void bindCellCollection(final BiResult p_br, int mode) {
		BiResult brPayProjectItem = p_br.getSubLink("propertymgmt.PayProjectItem");
		savedPayItemList = ZkUtil.getBiResultRecordMap(brPayProjectItem);
		brPayProjectItem.getRowCollectionList().forEach(throwConsumer(cl -> {
			cl.getCell("vcol_block").set(cl.getString("pcol_c"));
			cl.getCell("vcol_floor").set(cl.getString("pcol_d"));
			cl.getCell("vcol_flat").set(cl.getString("pcol_e"));
			((BiResultProjectPayment)p_br).calcPaidPeriodAndAmount(cl, true);
			//setupUnitItemInterface(p_br, cl);
			setupBlockItemInterface(p_br, cl);
			setupFloorItemInterface(p_br, cl);
			setupFlatItemInterface(p_br, cl);
			setupProjectNameItemInterface(p_br, cl, true);
		}));
		pickProjectNameMap.clear();
		super.bindCellCollection(p_br, mode);
		Listbox lb = (Listbox)jxAdd("list_propertymgmt_PayProjectItem").getNativeObject();
		Selectors.find(lb, "listheader").forEach(lh -> {
			if (StringUtils.equalsAny((String)lh.getAttribute("biColumnLabel"), "col_d", "col_f"))
				lh.setVisible(false);
		});
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
			if (sr.getView().getName().equals("propertymgmt.PayProjectItem")) {
				BiResult br = getBr();
				//setupUnitItemInterface(sr, cl);
				setupBlockItemInterface(br, cl);
				setupFloorItemInterface(br, cl);
				setupFlatItemInterface(br, cl);
				setupProjectNameItemInterface(br, cl, false);
				BiResultProjectPayment.setupPeriodCntItemInterface(cl, 0, 0, 0);
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
		if (sr.getView().getName().equals("propertymgmt.PayProjectItem"))
			ZkUtil.safeRunnable(() -> ((BiResultProjectPayment)getBr()).syncPaymentFromPayItem(), true).run();
	}

	@Override
	protected void afterUnDeleteLink(BiResult sr,int idx) {
		super.afterUnDeleteLink(sr, idx);
		if (sr.getView().getName().equals("propertymgmt.PayProjectItem"))
			ZkUtil.safeRunnable(() -> ((BiResultProjectPayment)getBr()).syncPaymentFromPayItem(), true).run();
	}

	@Override
	public List<BiGetItemProperty> getCustomItemPropertyList(BiResult p_br, int mode){
		return Arrays.asList(
			new PayProjectItemGetItemProperty(p_br.getSubLink("propertymgmt.PayProjectItem"))
		);	
	}

	private class PayProjectItemGetItemProperty extends BiGetItemProperty {

		public PayProjectItemGetItemProperty(BiResult p_br) {
			super(p_br);
		}

		@Override
		public void onValueChanged(Object p_value, int p_ctype) {
			final ColumnCell bcc = (ColumnCell) p_value;
			final BiCellCollection cl = bcc.getCollection();
			UniLog.log1("onValueChanged p_ctype:%d, label:%s, mapper:%s", p_ctype, bcc.getCellLabel(), bcc.getMapper());
			if (p_ctype == GIPI_VALUE_CHANGED) {
				try {
					BiResultProjectPayment br = (BiResultProjectPayment)getBr();
					if (Objects.equals(bcc.getCellLabel(), "vcol_block")) {
						BiResultProjectPayment.emptyCellValue(cl, "vcol_floor", "vcol_flat", "pjf_name", "col_e", "col_g");
						br.calcUnit(cl);
						setupFloorItemInterface(br, cl);
						setupFlatItemInterface(br, cl);
						setupProjectNameItemInterface(br, cl, false);
						BiResultProjectPayment.setupPeriodCntItemInterface(cl, 0, 0, 0);
					} else if (Objects.equals(bcc.getCellLabel(), "vcol_floor")) {
						BiResultProjectPayment.emptyCellValue(cl, "vcol_flat", "pjf_name", "col_e", "col_g");
						br.calcUnit(cl);
						setupFlatItemInterface(br, cl);
						setupProjectNameItemInterface(br, cl, false);
						BiResultProjectPayment.setupPeriodCntItemInterface(cl, 0, 0, 0);
					} else if (Objects.equals(bcc.getCellLabel(), "vcol_flat")) {
						BiResultProjectPayment.emptyCellValue(cl, "pjf_name", "col_e", "col_g");
						br.calcUnit(cl);
						setupProjectNameItemInterface(br, cl, false);
						BiResultProjectPayment.setupPeriodCntItemInterface(cl, 0, 0, 0);
					} else if (Objects.equals(bcc.getCellLabel(), "pjf_name")) {
						br.calcPaidPeriodAndAmount(cl, false);
						BiResultProjectPayment.calcAmount(cl);
						br.syncPaymentFromPayItem();
					} else if (StringUtils.equalsAny(bcc.getCellLabel(), "col_f", "vcol_f")) {
						BiResultProjectPayment.calcAmount(cl);
						br.syncPaymentFromPayItem();
					}
				} catch (Exception e) {
					UniLog.log(e);
				}
			}
			if (p_ctype == GIPI_CELL_MAPPED) {
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
			rtn = ((BiResultProjectPayment)getBr()).validationRecord(false);
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
	
	/*private void setupUnitItemInterface(BiResult br, CellCollection cl) throws Exception {
		if (gipiPropertyUnit.getTableRec() == null)
			gipiPropertyUnit.setTableRec(br.getSelectUtil().getQueryResult("select key_a from property where col_b = ?", new Wherecl().appendArgument(locDesc)));
		cl.getCell("col_d").setItemPropertyInterface(gipiPropertyUnit);
	}*/
	
	private void setupProjectNameItemInterface(BiResult br, CellCollection cl, boolean isInit) throws Exception {
		String unit = cl.getString("col_d");
		GipiNamedItemList gipi = new GipiNamedItemList();
		if (StringUtils.isBlank(unit)) {
			cl.getCell("pjf_name").setItemPropertyInterface(gipi);
			return;
		}
		Map<String, String> map = pickProjectNameMap.get(unit);
		if (map == null) {
			map = ZkUtil.getTableRecStream(br.getSelectUtil(), "select distinct upf_projectno, upf_projectname from unitprojectfee where upf_unit = ? and upf_allocAmt > upf_totpayamt", 
									new Wherecl().appendArgument(unit)).collect(Collectors.toMap(c -> c.getString("upf_projectno"), c -> c.getString("upf_projectname"), (o, n) -> n));
			pickProjectNameMap.put(unit, map);
		}
		Map<String, String> newMap = new TreeMap<>(map);
		if (!isInit) {
			Map<String, String> nowMap = br.getSubLinkResult("propertymgmt.PayProjectItem").stream()
											.filter(c -> c != cl && Objects.equals(c.getString("col_d"), unit) && StringUtils.isNoneBlank(c.getString("col_c")))
											.collect(Collectors.toMap(c -> c.getString("col_c"), c -> c.getString("pjf_name"), (o, n) -> n));
			Map<String, String> savedMap = savedPayItemList.stream().filter(m -> Objects.equals(m.get("col_d"), unit))
														.collect(Collectors.toMap(m -> (String)m.get("col_c"), m -> (String)m.get("pjf_name"), (o, n) -> n));
			newMap.putAll(savedMap);
			newMap.keySet().removeAll(nowMap.keySet());
		} else
			newMap.put(cl.getString("col_c"), cl.getString("pjf_name"));
		newMap.entrySet().forEach(e -> gipi.appendItem(e.getValue(), e.getKey() + " | " + e.getValue()));
		cl.getCell("pjf_name").setItemPropertyInterface(gipi);
	}

}
