package com.uniinformation.bicore.propertymgmt;

import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.stream.Collectors;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.BiUtil;
import com.uniinformation.webcore.SessionHelper;
import static com.uniinformation.utils.BiUtil.throwConsumer;
import static com.uniinformation.bicore.propertymgmt.PropertyMgmtCellCollection.getMonthRange;

public class BiResultParkingTmpLocContract extends BiResultPropertyMgmt {

	public BiResultParkingTmpLocContract(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
	}

	@Override
	protected ReturnMsg biAfterAddUpdateCurrent(BiCellCollection col, boolean isUpdate) {
		try {
			String lcDesc = col.getString("col_a");
			String locCode = col.getString("col_b");
			String startMonth = col.getString("col_d");
			String endMonth = col.getString("col_e");
			Set<String> monthRange1 = getMonthRange(startMonth, endMonth);
			Set<String> monthRange2 = BiUtil.getTableRecStream(su, "select col_d from parkingtlconmonth where col_a = ? and col_b = ? and col_c = ?", 
						new Wherecl().appendArgument(lcDesc).appendArgument(locCode).appendArgument(startMonth)).map(c -> c.getString("col_d")).collect(Collectors.toCollection(TreeSet::new));
			UniLog.log1("monthRange1:%s, monthRange2:%s", monthRange1, monthRange2);
			monthRange2.stream().filter(item -> !monthRange1.contains(item)).forEach(throwConsumer(month -> {
				UniLog.log1("delete from parkingtlconmonth %s", month);
				su.executeUpdate("delete from parkingtlconmonth where col_a = ? and col_b = ? and col_c = ? and col_d = ?", 
						new Wherecl().appendArgument(lcDesc).appendArgument(locCode).appendArgument(startMonth).appendArgument(month));
			}));
			monthRange1.stream().filter(item -> !monthRange2.contains(item)).forEach(throwConsumer(month -> {
				BiUtil.executeInsertIntoSql(su, "parkingtlconmonth", Arrays.asList("col_a", "col_b", "col_c", "col_d"), 
						new Wherecl().appendArgument(lcDesc).appendArgument(locCode).appendArgument(startMonth).appendArgument(month));
			}));
		} catch (Exception e) {
			UniLog.log(e);
			return new ReturnMsg(false, e.toString(), true);
		}
		return ReturnMsg.defaultOk;
	}
	
}
