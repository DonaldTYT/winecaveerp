package com.uniinformation.bicore;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public interface BiAsAtReportInterface {
//	public BiColumn getCumulatorComparable();
	public String getFilterStr();
	public void setAsAtColumn(String p_columnLabel);
	public String getAsAtColumn();
	public Date getAsAtDate();
	public void setSkipPivotColumns(HashSet<String> p_cols);
	public boolean skipForPivot(String p_aggLabel,AggregateOrPivotHeader p_app);
	public void setSkipSummaryColumns(HashSet<String> p_cols);
	public boolean skipForSummary(String p_aggLabel,AggregateOrPivotHeader p_app);
	public boolean setFifoAging(boolean sw);
//	public BiColumn getCumulatorKey();
}