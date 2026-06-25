package com.uniinformation.bicore;

import java.util.List;

import com.uniinformation.utils.Wherecl;
import com.uniinformation.utils.whereclpar.Condition;

public class BiQueryPlan {
	public int maxPrefetch;
	public String keyField;
	public String keyTable;
	public String queryStr;
	public Wherecl queryWherecl;
	public Condition nullCondition;
	public List<Object> prefetchList;
	public boolean prefetchLimitReached;
	public String masterTabe;
	public String masterJoinStr;
	public BiQueryPlan(int p_maxPrefetch) {
		maxPrefetch = p_maxPrefetch;
	}
}
