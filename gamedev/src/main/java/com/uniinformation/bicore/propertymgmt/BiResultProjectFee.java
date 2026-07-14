package com.uniinformation.bicore.propertymgmt;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.MapUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.ZkUtil;
import com.uniinformation.webcore.SessionHelper;
import static com.uniinformation.utils.ZkUtil.throwConsumer;

public class BiResultProjectFee extends BiResultPropertyMgmt {
	private static final DecimalFormat df = new DecimalFormat("#,##0.00");
	public static final List<String> periodColumnLabelList = Arrays.asList("col_d", "col_e", "col_f", "col_g", "col_h");

	public BiResultProjectFee(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
	}

	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col, boolean isUpdate) {
		try {
			if (!isUpdate) {
				String code = col.getString("col_a");
				String lcdesc = col.getString("col_c");
				ZkUtil.importAction.accept(sh, (su1) -> {
					ZkUtil.getTableRecStream(su, "select key_a from property where col_b = ?", new Wherecl().appendArgument(lcdesc)).forEach(throwConsumer(c -> {
						ZkUtil.executeInsertIntoSql(su1, "projectfeeunit", Arrays.asList("col_a", "col_b"), 
								new Wherecl().appendArgument(code)
											.appendArgument(c.getString("key_a")));
					}));
				});
			}
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.toString());
		}
		return ReturnMsg.defaultOk;
	}

	public Stream<String> getPeriodColumnLabelStream() {
		return periodColumnLabelList.stream().filter(label -> periodColumnLabelList.indexOf(label) < getCellInt("col_g"));
	}

	public void setupStatisticsField() throws Exception {
		Map<String, Map<String, Object>> map = getSubLinkResult("propertymgmt.ProjectFeeUnit").stream().collect(Collectors.groupingBy(
				c -> c.getString("pcol_a"), 
				Collectors.collectingAndThen(Collectors.toList(), items -> {
					Map<String, Object> m = MapUtil.of("count", items.size(), "amount", items.stream().mapToDouble(item -> item.getDouble("col_c")).sum());
					return m;
				})));
		getCell("vcol_statistics").set(Stream.of("住宅", "商鋪", "車位").map(t -> {
			return t + "：" + Optional.ofNullable(map.get(t)).map(m -> m.get("count") + "戶 $" + df.format(m.get("amount"))).orElse("0戶 $0.00");
		}).collect(Collectors.joining("\u00A0\u00A0\u00A0\u00A0|\u00A0\u00A0\u00A0\u00A0")));
	}
}
