package com.uniinformation.bicore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.uniinformation.bicore.AggregateOrPivot.AggregateRec;
import com.uniinformation.utils.UniLog;

public class AggregateOrPivotHeader {
	boolean aggregateFirst=true;
	ArrayList<Comparable>aopList;
	ArrayList<Comparable[]>sortedPivotList = null;
	ArrayList<String>aggregateList;
	AggregateOrPivot aop;
	int[] aopListOrder = null;
	public static class APLabel{
		//final private boolean isPivot;
		final private String label;
		final private String pivotId;
		public APLabel(Comparable label, String pivotId) {
			this.label = label == null ? "" : label.toString();
			//this.isPivot = isPivot;
			this.pivotId = pivotId;
		}
		public static APLabel of(Comparable label) {
			return new APLabel(label, null);
		}
		public static APLabel of(Comparable label, String pivotId) {
			return new APLabel(label, pivotId);
		}
		public boolean isPivot() {
			return pivotId != null;
		}
		public String pivotId() {
			return pivotId;
		}
		public String pivotName(BiResult result) {
			if (pivotId != null) {
				BiColumn biColumn = result.getColumnByLabel(pivotId);
				if (biColumn != null)
					return StringUtils.defaultString(biColumn.getEngName());
			}
			return "";
		}
		@Override
		public String toString() {
			return label;
		}
		
	}
    String makePivotColumnsName(Comparable o[]) {
    	String s = null;
    	for(int i=0;i<o.length;i++) {
    		if(o[i]==null) return("");
    		if(s == null ) s=o[i].toString(); else s += "," + o[i];
    	}
    	return(s);
    }
	public AggregateOrPivotHeader(AggregateOrPivot p_aop,ArrayList<String> p_aggregateList , ArrayList<Comparable[]> p_pivotList, boolean p_aggregateFirst) {
		aop = p_aop;
		aggregateList = p_aggregateList;
		if(p_pivotList == null || p_pivotList.size() <= 0) {
			aopList = new ArrayList<Comparable>();
			for(String ss : p_aggregateList) {
				aopList.add(ss);
			}
//			aopList = p_aggregateList;
			aggregateFirst=false;
			return;
		} 
		sortedPivotList = (ArrayList<Comparable[]>) p_pivotList.clone();
    	Collections.sort(sortedPivotList, new Comparator() {

			@Override
			public int compare(Object arg0, Object arg1) {
				Comparable[] s0 = (Comparable[]) arg0;
				Comparable[] s1 = (Comparable[]) arg1;
				for(int i=0;i<s0.length;i++) {
					if(s0[i] == null) return(1);
					if(s1[i] == null) return(-1);
					int cc = s0[i].compareTo(s1[i]);
					if(cc != 0) return(cc);
				}
				// TODO Auto-generated method stub
				return 0;
			}
    		
    	});
		aopListOrder = new int[p_pivotList.size() * p_aggregateList.size()];
		aopList = new ArrayList<Comparable>();
		
		if(p_aggregateList.size() <= 1) {
			aggregateFirst = true;
		} else {
			aggregateFirst = p_aggregateFirst;
		}
			String[]aopStrList = new String[p_pivotList.size() * p_aggregateList.size()];
			for(int i = 0;i < p_pivotList.size();i++) {
				Comparable pivot[] = p_pivotList.get(i);
				int idx = sortedPivotList.indexOf(pivot);
//    			aopListOrder[i] = sortList.indexOf(aopList.get(i));
				String ss = makePivotColumnsName(pivot);
				for(int j=0;j<p_aggregateList.size();j++) {
					String s = ss;
					if(aggregateFirst) {
						if(p_aggregateList.size() > 1) {
							s = p_aggregateList.get(j)+":"+ss;
						}
//						aopListOrder[i + p_pivotList.size() * j] = idx + p_pivotList.size() * j;
						aopListOrder[i * p_aggregateList.size() + j] = idx + p_pivotList.size() * j;
						
						
//						aopList.add(idx + p_pivotList.size() * j,s);
						aopStrList[idx + p_pivotList.size() * j]=s;
					} else {
						if(p_aggregateList.size() > 1) {
							s = ss + ":" + p_aggregateList.get(j);
						}
						aopListOrder[i * p_aggregateList.size() + j] = idx * p_aggregateList.size() + j;
//						aopList.add(idx * p_aggregateList.size() + j,s);
						aopStrList[idx * p_aggregateList.size() + j] = s;
					}
				}
			}
			for(String ss : aopStrList) {
				aopList.add(ss);
			}
	}
	
	public List<String>getAggregateOrPivotList()
	{
		ArrayList<String>aopStrList = new ArrayList<String>();
		for(Comparable aop : aopList) {
			aopStrList.add(aop.toString());
		}
		return(aopStrList);
	}
	
	public int[]getAopListOrder() {
		return(aopListOrder);
	}
	
	public int getAuxHeaderCount() {
		if( sortedPivotList == null) return(0);
		return(sortedPivotList.get(0).length);
	}
	
	public APLabel getHeader(int p_idx) {
		if(aggregateFirst)  {
			int pivotRow = sortedPivotList.get(0).length-1;
			return( APLabel.of(sortedPivotList.get(p_idx % sortedPivotList.size())[pivotRow], aop.colsArr.get(pivotRow).getId()));
		} else {
			if(sortedPivotList == null) {
				return( APLabel.of(aggregateList.get(p_idx), null));
			} else {
				return( APLabel.of(aggregateList.get(p_idx % aggregateList.size())));
			}
		}
	}
	
	public int getPivotIndexByHeaderPosition(int p_idx) {
		int idx = -1;
		for(int i=0;i<aopListOrder.length;i++) {
			if(aopListOrder[i] == p_idx) {
				idx = i;
				break;
			}
		}
		if(idx < 0) return(-1);
		return(idx / aggregateList.size());
	}

	public Object[] getHeadersArray(int p_idx) {
		int idx;
		if(aggregateFirst)  {
			idx = p_idx % sortedPivotList.size();
		} else {
			idx = p_idx/aggregateList.size();
		}
		return(sortedPivotList.get(idx));
	}
	

	public APLabel getAuxHeader(int p_row,int p_idx) {
		int pivotRow;
		if(aggregateFirst) {
			if(p_row == 0) {
				return( APLabel.of(aggregateList.get(p_idx / sortedPivotList.size())));
			}
			pivotRow = p_row-1;
		} else {
			pivotRow = p_row;
		}
		if(aggregateFirst)  {
			return( APLabel.of(sortedPivotList.get(p_idx % sortedPivotList.size())[pivotRow], aop.colsArr.get(pivotRow).getId()));
		} else {
			return( APLabel.of(sortedPivotList.get(p_idx / aggregateList.size())[pivotRow], aop.colsArr.get(pivotRow).getId()));
		}
	}
	
	public AggregateRec getAggregate(int p_idx) {
		if(aggregateFirst && sortedPivotList != null) {
			return(aop.aggsArr.get(p_idx / sortedPivotList.size()));
		} else {
			return(aop.aggsArr.get(p_idx % aggregateList.size()));
		}
	}

	public AggregateRec getAggregate(String aopStr) {
		return(aop.aggsArr.get(aopList.indexOf(aopStr) % aggregateList.size()));
	}
	
	public boolean isSubTotalColumn(int p_idx) {
		if(sortedPivotList == null) return(true);
		int pivotIdx;
		if(aggregateFirst) {
			pivotIdx = p_idx % sortedPivotList.size();
		} else {
			pivotIdx = p_idx / aggregateList.size();
		}
		
		for(int i=0;i<sortedPivotList.get(pivotIdx).length;i++) {
			if(sortedPivotList.get(pivotIdx)[i] != null) return(false);
		}
		return(true);
	}
	
	public boolean groupColumnContains(String p_label) {
		if(aop.getRowColumnIds().contains(p_label)) return(true);
		if(aop.getColColumnIds().contains(p_label)) return(true);
		return(false);
	}
	public boolean rowColumnContains(String p_label) {
		if(aop.getRowColumnIds().contains(p_label)) return(true);
		return(false);
	}
	public boolean colColumnContains(String p_label) {
		if(aop.getColColumnIds().contains(p_label)) return(true);
		return(false);
	}
}
