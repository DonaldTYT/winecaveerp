package com.uniinformation.bicore.erpv4;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import com.kyoko.common.DateUtil;
import com.kyoko.common.ReturnMsg;
import com.uniinformation.bicore.BiAsAtReportInterface;
import com.uniinformation.bicore.BiCellCollection;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.BiTable;
import com.uniinformation.bicore.BiView;
import com.uniinformation.cell.CellException;
import com.uniinformation.erpv4.CostCalculation;
import com.uniinformation.erpv4.Erpv4Config;
import com.uniinformation.utils.SelectUtil;
import com.uniinformation.utils.UniLog;
import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.whereclpar.Condition;
import com.uniinformation.webcore.SessionHelper;
import org.apache.commons.lang3.tuple.Pair;
public class BiResultStockAsAt extends BiResultAgingAsAt{
	
	boolean useFifoAging = false;

	public BiResultStockAsAt(BiResult p_parent, BiView p_view, SelectUtil p_su, Vector p_tabList, String p_whereStr,
			SessionHelper p_sh, boolean p_allowLookupItemList) throws CellException {
		super(p_parent, p_view, p_su, p_tabList, p_whereStr, p_sh, p_allowLookupItemList);
		setAsAtColumn("stmd_date");
		setSkipPivotColumns(new HashSet<String>(Arrays.asList("stmd_avcost")));
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected HashSet<BiTable> addExtraWhereStr(Wherecl p_where,HashSet<BiTable> p_hash)
	{
		HashSet<BiTable> ht = super.addExtraWhereStr(p_where, p_hash);
		Wherecl wcl1 = null;
		if(Erpv4Config.isMultiCompany(sh)) {
			if(!sh.hasAccessRight("#allloc")) {
			BiColumn locCol = getColumnByLabel("loc_cocode");
			
			if(locCol != null && columnInSelectList(locCol)) {
				String cocode = Erpv4Config.getDefaultCoCode(sh);
				if(wcl1 == null) wcl1 = new Wherecl();
				wcl1.appendString(" and loc_cocode = '"+cocode+"' ").stripAnd();
			}
			}
		}
		if(wcl1 != null) p_where.andWherecl(wcl1);
		return(ht);
	}
	@Override
	protected ReturnMsg afterLoadSerialMap() {
		int n;
		int datePos = getSelectFieldPosition( getView().getColumnByLabel("stmd_mindate"));
		int qtyPos = getSelectFieldPosition( getView().getColumnByLabel("stmd_qty"));
		if(datePos < 0) return(ReturnMsg.defaultOk);
		n = getTableRecCount();
		invalidateLoadCache();

		int lastirg = 0;
		int lastorg = 0;
		List<Pair<Comparable,Double>> fifoQty = null;
		for(int i=n-1;i>=0;i--) {
			loadOneRec(i,getDefaultRowCollection(),false);
			try {
			if(!useFifoAging) {
//				saveOneObjectToResultTr(i,datePos,DateUtil.zeroDate);
				if(getCellDouble("stmd_qty") == 0.0) {
					delTrRecord(i);
				}
			} else {

				if(getCellInt("st_irg") != lastirg ||
						getCellInt("stmd_org") != lastorg 
						) {
					fifoQty = CostCalculation.getBalanceFifo(sh, getCellInt("st_irg"), getCellInt("stmd_org"),asAtDate,Double.MAX_VALUE);
					lastirg = getCellInt("st_irg");
					lastorg = getCellInt("stmd_org");
				}
//				List<Pair<Comparable,Double>> fifoQty = CostCalculation.getLocBalanceFifo(sh, getCellInt("st_irg"), getCellInt("stmd_org"), getCellString("stmd_loc"), asAtDate);
//				double bal = CostCalculation.getLocBalance(sh, getCellInt("st_irg"), getCellInt("stmd_org"), getCellString("stmd_loc"), asAtDate);
				if(fifoQty == null) {
//					return(new ReturnMsg(false,"getFifoBalance Error"));
					UniLog.log("Stock Purchase History Error " + getCellInt("st_irg") + " "  +  getCellInt("stmd_org") + " " + getCellString("stmd_loc") + " " + asAtDate);
					saveOneObjectToResultTr(i,datePos,DateUtil.zeroDate);
					continue;
				}
				if(fifoQty.size() > 0) {
					Object[] orgrec = getTrRecord(i);
					double qty = getCellDouble("stmd_qty");
					int j = 0;
					while(qty > 0.0 && fifoQty.size() > 0) {
						Date agingDate = (Date) fifoQty.get(0).getLeft();
						double agingQty = (Double) fifoQty.get(0).getRight();
						if(agingQty > 0) {
							if(j>0) {
								addTrRecord(orgrec,i);
							}
							if(agingQty > qty) {
								fifoQty.set(0, Pair.of((Comparable) agingDate,agingQty - qty));
								saveOneObjectToResultTr(i,datePos,agingDate);
								saveOneObjectToResultTr(i,qtyPos,qty);
								qty = 0;
								j++;
							} else {
								saveOneObjectToResultTr(i,datePos,agingDate);
								saveOneObjectToResultTr(i,qtyPos,agingQty);
								qty -= agingQty;
								fifoQty.remove(0);
								j++;
							}
						} else {
							fifoQty.remove(0);
						}
					}
					if(qty != 0) {
						if(j>0) {
							addTrRecord(orgrec,i);
							saveOneObjectToResultTr(i,datePos,DateUtil.zeroDate);
							saveOneObjectToResultTr(i,qtyPos,qty);
						} else {
							saveOneObjectToResultTr(i,datePos,DateUtil.zeroDate);
						}
						qty = 0;
					}
					/*
					Object[] orgrec = getTrRecord(i);
					for(int j=0;j<fifoQty.size();j++) {
						if(j > 0) {
							addTrRecord(orgrec,i);
						}
						saveOneObjectToResultTr(i,datePos,fifoQty.get(j).getLeft());
						saveOneObjectToResultTr(i,qtyPos,fifoQty.get(j).getRight());
					}
					*/
				} else {
					saveOneObjectToResultTr(i,datePos,DateUtil.zeroDate);
				}
			}
			} catch (Exception ex) {
				UniLog.log(ex);
				return(new ReturnMsg(false,"getLocBalaecFifo error"));
			}
//			List<Pair<java.util.Date,Double>> getLocBalanceFifo(SessionHelper p_sh, int p_irg, int p_org,String p_loc,java.util.Date p_date ) throws Exception {
			
		}
		invalidateLoadCache();
		return(ReturnMsg.defaultOk);
	}
	
//	protected String getSelectName(BiColumn bc) {
//		if(useFifoAging && bc.getLabel().equals("stmd_ref4")) {
//			return("'' stmovd_any.stmd_ref4");
//		}
//		return(bc.getSelectName());
//	}

	@Override
	public boolean setFifoAging(boolean sw) {
		// TODO Auto-generated method stub
		if(sw != useFifoAging) {
			useFifoAging = sw;
			return(true);
		}
		return(false);
	}

	@Override
	public void resetViewList() {
		super.resetViewList();
		if(useFifoAging) {
			hideViewColumn(getColumnByLabel("stmd_ref4"));
		}
	}	
}
