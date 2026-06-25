package com.uniinformation.bicore.edu;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.utils.GipiNamedItemList;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.TableRec;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

public class BiResultCourseStudent extends BiResult {
	public BiResultCourseStudent(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		UniLog.log1("called");
	}

	@Override
	protected void setLookupItemList(TableRec lookupTableTr,ColumnCell colCell) throws Exception {
		//if (StringUtils.equals(colCell.getCellLabel(), "essd_name")) {
		if (StringUtils.equals(colCell.getCellLabel(), "essd_sdnox")) {
			Vector<Object> lookupValues = new Vector<Object>();
			List<Pair<String, Integer>> labelList = new ArrayList<Pair<String, Integer>>();
			for (int j = 0; j < lookupTableTr.getRecordCount(); j++) {
				lookupTableTr.setRecPointer(j);
				//String name = lookupTableTr.getFieldString(colCell.getBiColumn().getField().getName());
				//String sdno = lookupTableTr.getFieldString("essd_sdno");
				//lookupValues.add(name);
				String sdno = lookupTableTr.getFieldString(colCell.getBiColumn().getField().getName());
				String name = lookupTableTr.getFieldString("essd_name");
				lookupValues.add(sdno);
				labelList.add(Pair.of(String.format("%s (%s)", name, sdno), j));
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

	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeDeleteCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		if (getParent() == null || !(getParent() instanceof BiResultCourse))
			return rtn;

		//reset token
		BiResult brSession = null;
		BiResult brAttendance = null;
		try {
			int courseRg = getParent().getCellInt("eaav0_rg");
			String ccy = getParent().getCellString("eaav0_tokenccy");
			int studentRg = col.getCellInt("essbsd_sdrg");
			if (studentRg <= 0) {
				UniLog.log1("Invalid student rg %d", studentRg);
				return rtn;
			}
			//RpcClient rpc = getSelectUtil().getRpcClient();
			//List<Vector> list = new ArrayList<Vector>();
			brSession = BiResultHelper.create(sh, "edu.CourseSessionDet", String.format("essncs_avrg = %d", courseRg), -1, null);
			brAttendance = sh.newBiResult("edu.StudentAttendance");
			while (brSession.next()) {
				int sessionRg = brSession.getCellInt("essncs_rg");
				brAttendance.clearCondition();
				brAttendance.addCustomCondition(String.format("esatsd_atrg = %d and esatsd_snrg = %d", studentRg, sessionRg));
				if ((rtn = brAttendance.query(true, false)).getStatus()) {
					if (brAttendance.next())
						return new ReturnMsg(false, "Not allowed to removed student if attendance record is exist");
				}
				else
					throw new Exception(rtn.getMsg());
				/*Vector args = new Vector();
				args.add(studentRg);
				args.add(sessionRg);
				args.add(courseRg);
				args.add(ccy);
				args.add(0.0);
				list.add(args);*/
			}
			/*for (Vector args : list) {
				UniLog.log1("token courseRg:%d, sessionRg:%d, studentRg:%d, ccy:%s, fee:%f", args.get(2), args.get(1), args.get(0), args.get(3), args.get(4));
				Value value = rpc.callSegment("token_addUpdateCourseAttend", args);
				if (value == null)
					throw new Exception("rpccall failed");
				if (!StringUtils.startsWith(value.toString(), "OK"))
					throw new Exception(StringUtils.startsWith(value.toString(), "FAIL") ? value.toString().substring(4) : value.toString());
			}*/
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnMsg(e);
		}
		finally {
			if (brSession != null)
				brSession.close();
			if (brAttendance != null)
				brAttendance.close();
		}

		return(rtn);
	}
}
