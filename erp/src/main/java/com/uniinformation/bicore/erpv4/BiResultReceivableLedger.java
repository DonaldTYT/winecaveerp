package com.uniinformation.bicore.erpv4;

import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.ColumnCell;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.prtdoc.PrtdocJson;
import com.uniinformation.rpccall.RpcClient;
import com.uniinformation.rpccall.Strval;
import com.uniinformation.rpccall.Value;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.VectorUtil;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultReceivableLedger extends BiResultArApJournal {
	public BiResultReceivableLedger(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList,
			String p_whereStr, SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		RpcClient rpc = p_sh.getRpcClient();
		String cocode = Erpv4Config.getDefaultCoCode(p_sh);
		rpc.callSegment("setCocodeBaseccy",
				new VectorUtil()
				.addElement(cocode)
				.addElement(Erpv4Config.getBaseCcy(getSessionHelper(),cocode))
				.toVector()
				);
		Value val =
				rpc.callSegment("erpv4_getArAccountCode",
				new VectorUtil()
				.toVector()
				);
		if(val != null && (val instanceof Strval)) {
			ArApAccount = val.toString();
		}
	}
	
	@Override
	void beforeQuery() {
		super.beforeQuery();
		if(getQueryIncludeNoDetail()) {
//			addCustomCondition(" (stmd_openbal <> 0 or stmd_inqty <> 0 or stmd_outqty <> 0) ");
//			addCustomCondition(" (stmd_openbal <> 0 or stmd_irg > 0) ");
			addCustomCondition("sih_trxno2 = jn_xno");
		}
		
	}
}
