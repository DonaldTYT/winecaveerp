package com.uniinformation.bicore.edu;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;

import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiResultHelper;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellCollection;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.edu.ProcessScanLog;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultCourseSessionDet extends BiResult{
	public BiResultCourseSessionDet(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		//this.setRecLimit(5);
	}

	@Override
	protected ReturnMsg biBeforeDeleteCurrent(CellCollection col) {
		ReturnMsg rtn = super.biBeforeDeleteCurrent(col);
		if(rtn != null && !rtn.getStatus()) return(rtn);

		if (getParent() == null || !(getParent() instanceof BiResultCourse))
			return rtn;

		//reset token
		BiResult brStudent = null;
		try {
			int courseRg = getParent().getCellInt("eaav0_rg");
			String ccy = getParent().getCellString("eaav0_tokenccy");
			List<String> cardNoList = new ArrayList<String>();
			Vector args = new Vector();
			args.add(courseRg);
			args.add(col.getCellInt("essncs_rg"));
			brStudent = BiResultHelper.create(sh, "edu.CourseStudent", String.format("essbsd_avrg = %d", courseRg), -1, null);
			while (brStudent.next()) {
				args.add(brStudent.getCellInt("essbsd_sdrg"));
				args.add(ccy);
				args.add(0.0);
				UniLog.log1("token courseRg:%d, sessionRg:%d, studentRg:%d, ccy:%s, fee:%f", args.get(0), args.get(1), 
						args.get(args.size() - 3), args.get(args.size() - 2), args.get(args.size() - 1));
				cardNoList.add(brStudent.getCellString("essd_cardno"));
			}
			if (args.size() > 2) {
				RpcClient rpc = getSelectUtil().getRpcClient();
				Value value = rpc.callSegment("token_addUpdateCourseAttendMulti", args);
				if (value == null)
					throw new Exception("rpccall failed");
				if (!StringUtils.startsWith(value.toString(), "OK"))
					throw new Exception(StringUtils.startsWith(value.toString(), "FAIL") ? value.toString().substring(4) : value.toString());
				//need to call the ProcessScanLog.setCSMapDirty() when attendance updated
				for (String cardNo : cardNoList)
					ProcessScanLog.setCSMapDirty(cardNo);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return new ReturnMsg(e);
		}
		finally {
			if (brStudent != null)
				brStudent.close();
		}

		return(rtn);
	}
	
	/*
	@Override 
	public HashSet<BiTable>addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash) {
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
		//p_where.andRange(p_colname, p_int0, p_int1);
		return(ht);
	}	
	*/

}
