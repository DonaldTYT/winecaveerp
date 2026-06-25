package com.uniinformation.zkbi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import org.zkoss.zul.Listheader;

import com.uniinformation.bicore.AggregateOrPivotHeader;
import com.uniinformation.bicore.BiColumn;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.bicore.AggregateOrPivot.AggregateRec;

public class ZkBiComposerAggregateReport extends ZkBiComposerExtended {
//	@Override
//    protected boolean isAggregateVisible(BiResult p_result,AggregateOrPivotHeader p_aop,int p_idx) {
//		AggregateRec aggRec = p_aop.getAggregate(p_idx);
//		String colLabel = aggRec.getKey();
//		if(isAggregateHidden(colLabel)) return(false);
//		BiColumn bc = p_result.getColumnByLabel(colLabel);
//		if(bc == null) return(true);
//		if(p_aop.isSubTotalColumn(p_idx)) {
//			return(bc.isAggregateFlagOn());
//		} else {
//			return(bc.isPivotFlagOn());
//		}
//    }	
//	
//	
//	@Override
//	protected List<BiColumn> allowSelectAggregateList(BiResult result) {
//		return(result.getAggregateColumnList());
//	}
	@Override
	public HashSet<BiColumn> getVisibleColumns(BiResult p_br) {
			HashSet<BiColumn> lc = new HashSet<BiColumn>();
			Vector<BiColumn> listColumns1 = p_br.getListColumns();
		// TODO Auto-generated method stub
							for(int i=0;i<listColumns1.size();i++){
								BiColumn biColumn = listColumns1.get(i);
								Listheader listheader = (Listheader) masterWin.query("#browser_listheader_" + (i+1));
								if (listheader != null && (Boolean)listheader.getAttribute("isHideForTempBiColumn") == null) {
									if (listheader.isVisible()) {
										lc.add(biColumn);
									}
								}
							}
		return(lc);
	}
}
