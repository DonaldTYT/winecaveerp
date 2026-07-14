package com.uniinformation.bicore.propertymgmt;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;

import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.bicore.bischema.BiResultExcelSheet;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.webcore.SessionHelper;

public class BiResultPropertyMgmt extends BiResultExcelSheet{

	public BiResultPropertyMgmt(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh);
		if (StringUtils.equalsAny(p_view.getName(), "propertymgmt.payitem"))
			setRecLimit(500000);
	}

	@Override
	protected BiCellCollection createColumnCollection(BiCellCollection p_parent) {
		return(new PropertyMgmtCellCollection(p_parent, this));
	}
	@Override
    protected String brEvalFunction(String p_functName,List p_args) {
    	if(p_functName.equals("defaultLdesc")) {
    		String ss = Erpv4Config.getLcDesc(sh, Erpv4Config.getDefaultLcrg(sh));
    		if(ss != null) return("'"+ss.trim()+"'"); else return("''");
    	}
		return(super.brEvalFunction(p_functName, p_args));
    }	
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where,p_hash);
		if(!getSessionHelper().hasAccessRight("#allproperty")) {
    		String ss = Erpv4Config.getLcDesc(sh, Erpv4Config.getDefaultLcrg(sh));
    		String pncol = null;
    		if(getView().getName().equals("propertymgmt.contract")) pncol = "col_a";
    		if(getView().getName().equals("propertymgmt.rptfeebymonth")) pncol = "contractmonth.col_a";
    		if(getView().getName().equals("propertymgmt.property")) pncol = "col_b";
    		if(getView().getName().equals("propertymgmt.payment")) pncol = "payment.col_c";
    		if(getView().getName().equals("propertymgmt.monthpayment")) pncol = "mpy_propertyname";
    		if(getView().getName().equals("propertymgmt.rptpayitem")) pncol = "payitem.col_b";
    		if(getView().getName().equals("propertymgmt.ProjectFee")) pncol = "projectfee.col_c";
    		if(getView().getName().equals("propertymgmt.RenovationDeposit")) pncol = "renovationdeposit.col_b";
    		if(getView().getName().equals("propertymgmt.ParkingTmpZone")) pncol = "parkingtmpzone.col_a";
    		if(getView().getName().equals("propertymgmt.ParkingTmpLocContract")) pncol = "parkingtlcontract.col_a";
    		if(getView().getName().equals("propertymgmt.ProjectPayment")) pncol = "projectpayment.col_c";
    		if(getView().getName().equals("propertymgmt.ParkingTmpLocPayment")) pncol = "parkingtlpayment.col_c";
    		if(getView().getName().equals("propertymgmt.UnitProjectFee")) pncol = "upf_location";
    		if(getView().getName().equals("propertymgmt.MonthParkingTmpLoc")) pncol = "mpt_building";
    		if(getView().getName().equals("propertymgmt.PayProjectItem2")) pncol = "payprojectitem2.col_b";
    		if(getView().getName().equals("propertymgmt.PayParkingItem2")) pncol = "payparkingitem2.col_b";
    		if(pncol != null) p_where.andUniop(pncol, "=", ss == null ? "" : ss);
		} else {
			UniLog.log("skip location filter");
		}
		return(ht);
	}	

	public static int getAggregateIndex(BiResult br, String p_col) throws Exception {
		return IntStream.range(0, br.aggregateOrPivotSize()).filter(i -> p_col.equals(br.getAggregateOrPivotHeader().getAggregate(i).getKey())).findFirst().orElse(-1);
	}

	public static <T> T getAggregateValue(BiResult br, Object[] eggValues, String p_col) throws Exception {
		int eggIndex = getAggregateIndex(br, p_col);
		return (T)eggValues[eggIndex];
	}
}
