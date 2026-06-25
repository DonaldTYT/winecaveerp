package com.uniinformation.bicore.edu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultAttendance extends BiResult {
	public BiResultAttendance(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	@Override
	protected void setLookupItemList(TableRec lookupTableTr,ColumnCell colCell) throws Exception {
		if (StringUtils.equals(colCell.getCellLabel(), "essd_sdnox")) {
			Vector<Object> lookupValues = new Vector<Object>();
			List<Pair<String, Integer>> labelList = new ArrayList<Pair<String, Integer>>();
			for (int j = 0; j < lookupTableTr.getRecordCount(); j++) {
				lookupTableTr.setRecPointer(j);
				String sdno = lookupTableTr.getFieldString(colCell.getBiColumn().getField().getName());
				String name = lookupTableTr.getFieldString("essd_name");
				lookupValues.add(sdno);
				labelList.add(Pair.of(name, j));
			}
			Collections.sort(labelList, new Comparator<Pair<String, Integer>>() {
				@Override
				public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
					return o1.getLeft().compareTo(o2.getLeft());
				}
			});
	    	GipiNamedItemList gipi = new GipiNamedItemList();
			for (Pair<String, Integer> p : labelList) {
				int i = p.getRight();
				gipi.appendItem(lookupValues.get(i), p.getLeft());
			}
			colCell.setItemPropertyInterface(gipi);
			colCell.setCCObj("lookup_uparent_tr", lookupTableTr);
			colCell.setCCObj("lookup_uparent_values", lookupValues);
		}
		else
			super.setLookupItemList(lookupTableTr, colCell);
	}
}
