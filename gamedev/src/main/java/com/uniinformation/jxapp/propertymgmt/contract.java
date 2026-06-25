package com.uniinformation.jxapp.propertymgmt;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

import com.kyoko.common.DateUtil;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
import com.uniinformation.jxapp.JxZkBiBase;
import com.uniinformation.utils.UniLog;
import com.uniinformation.zkbi.ZkBiAbstractLongOp;

public class contract extends JxZkBiBase {
	private ZkBiAbstractLongOp longOp;
	@Override
	public void bindCellCollection(BiResult p_br,int mode) {
		UniLog.log("bindCellCollection contract called");
		super.bindCellCollection(p_br, mode);
		if (mode == JxZkBiBase.MODE_UPDATE) {
			longOp = ZkBiAbstractLongOp.newInstance(longOp, curComp, "Loading", 100, () -> {
				BiResult brFee = getBr().getSubLink("propertymgmt.contractfee");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
				for (BiCellCollection cc : brFee.getRowCollectionList()) {
					String startMonth = cc.getString("col_h");
					String endMonth = cc.getString("col_i");
					if (DateUtil.isValidYearMonth(startMonth, sdf.toPattern()) && DateUtil.isValidYearMonth(endMonth, sdf.toPattern())) {
						try {
							Date startDate = sdf.parse(startMonth);
							Date endDate = DateUtil.monthEnd(sdf.parse(endMonth));
							if (startDate.compareTo(DateUtil.today()) <= 0 && endDate.compareTo(startDate) > 0) {
								brFee.getListColumns().stream().map(bc -> cc.getCell(bc.getLabel())).filter(c -> c != null && c.getMode() == Cell.VMODE_NORMAL).forEach(c -> {
									try {
										c.setMode(Cell.VMODE_DISPONLY);
									} catch (CellException e) {
										UniLog.log(e);
									}
								});
							}
						} catch (ParseException e) {
						}
					}
				}
				return null;
			});
		}
	}
}
