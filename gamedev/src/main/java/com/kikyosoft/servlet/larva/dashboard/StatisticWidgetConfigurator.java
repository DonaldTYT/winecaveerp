package com.kikyosoft.servlet.larva.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kikyosoft.rpccall.RpcClient;
import com.kikyosoft.rpccall.Value;
import com.kikyosoft.utils.VectorUtil;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

/**
 * Configurable background statistics for dashboard charts. Dashboard requests
 * only read this agent-scoped cache; all BI/RPC work is done by the KPI cron.
 */
public final class StatisticWidgetConfigurator {
	private static final String BICORE_RPC_PREFIX = "com.uniinformation.bicore.BiCoreRpcServlet.";
	private static final String CACHE_KEY = "StatisticWidgetConfigurator.cache";
	private static final String DEFAULT_COLUMN_CLASS = "col-md-6 col-xl-6";

	private StatisticWidgetConfigurator() {}

	public static List<StatisticWidget> load(SessionHelper sp) {
		List<StatisticWidget> widgets = new ArrayList<StatisticWidget>();
		String ids = BiConfig.getString(sp, "ShellStatisticIds");
		if(StringUtils.isBlank(ids)) return widgets;
		for(String rawId : StringUtils.split(ids, ',')) {
			String id = StringUtils.trimToNull(rawId);
			if(id == null) continue;
			String prefix = "ShellStatistic_" + id + "_";
			if(!isEnabled(BiConfig.getString(sp, prefix + "Enabled")) || !isAllowed(sp, prefix)) continue;
			CachedStatistic cached = getCache(sp.getAgent()).get(id);
			widgets.add(cached == null ? pending(sp, id, prefix) : cached.widget);
		}
		return widgets;
	}

	/** Called by WidgetCronJob, never by a browser request. */
	public static void refresh(SessionHelper sp) {
		String ids = BiConfig.getString(sp, "ShellStatisticIds");
		if(StringUtils.isBlank(ids)) return;
		for(String rawId : StringUtils.split(ids, ',')) {
			String id = StringUtils.trimToNull(rawId);
			if(id == null) continue;
			String prefix = "ShellStatistic_" + id + "_";
			if(isEnabled(BiConfig.getString(sp, prefix + "Enabled"))) refreshOne(sp, id, prefix);
		}
	}

	private static void refreshOne(SessionHelper sp, String id, String prefix) {
		int delaySeconds = refreshDelaySeconds(sp, prefix);
		StatisticCache cache = getCache(sp.getAgent());
		CachedStatistic cached = cache.get(id);
		if(delaySeconds > 0 && cached != null && !cached.isExpired(delaySeconds)) return;

		StatisticWidget widget = loadPie(sp, id, prefix);
		if(widget.isAvailable()) cache.put(id, new CachedStatistic(widget));
	}

	private static StatisticWidget loadPie(SessionHelper sp, String id, String prefix) {
		String title = StringUtils.defaultIfBlank(BiConfig.getString(sp, prefix + "Title"), id);
		String columnClass = columnClass(BiConfig.getString(sp, prefix + "Width"));
		String type = StringUtils.defaultIfBlank(BiConfig.getString(sp, prefix + "Type"), "pie")
				.trim().toLowerCase(Locale.ROOT);
		String viewName = StringUtils.trimToNull(BiConfig.getString(sp, prefix + "View"));
		String dataAgent = StringUtils.trimToNull(BiConfig.getString(sp, prefix + "Agent"));
		String groupColumn = StringUtils.trimToNull(BiConfig.getString(sp, prefix + "GroupColumn"));
		String periodColumn = StringUtils.trimToNull(BiConfig.getString(sp, prefix + "PeriodColumn"));
		String aggregateColumn = StringUtils.trimToNull(BiConfig.getString(sp, prefix + "AggregateColumn"));
		String condition = StringUtils.trimToNull(BiConfig.getString(sp, prefix + "Condition"));
		int maxSegments = positiveInt(BiConfig.getString(sp, prefix + "MaxSegments"), 10);
		String otherLabel = StringUtils.defaultIfBlank(BiConfig.getString(sp, prefix + "OtherLabel"), "Others");
		if(!"pie".equals(type)) return unavailable(id, title, columnClass, "Unsupported statistic type: " + type);
		if(StringUtils.isAnyBlank(viewName, dataAgent, groupColumn, aggregateColumn)) {
			return unavailable(id, title, columnClass, "View, Agent, GroupColumn and AggregateColumn are required");
		}
		try {
			List<PieValue> values = loadRemoteValues(sp, id, dataAgent, viewName, condition,
					groupColumn, periodColumn, aggregateColumn);
			if(values.isEmpty()) return unavailable(id, title, columnClass, "No statistic values returned");
			Collections.sort(values, new Comparator<PieValue>() {
				@Override public int compare(PieValue left, PieValue right) {
					return Double.compare(right.value, left.value);
				}
			});
			List<String> labels = new ArrayList<String>();
			List<Double> series = new ArrayList<Double>();
			double others = 0.0d;
			for(int i = 0; i < values.size(); i++) {
				PieValue value = values.get(i);
				if(i < maxSegments) {
					labels.add(value.label);
					series.add(value.value);
				} else {
					others += value.value;
				}
			}
			if(others > 0.0d) {
				labels.add(otherLabel);
				series.add(others);
			}
			return new StatisticWidget(id, title, columnClass, labels, series, null, true);
		} catch(Exception ex) {
			return unavailable(id, title, columnClass, ex.getMessage());
		}
	}

	private static List<PieValue> loadRemoteValues(SessionHelper sp, String id, String dataAgent,
			String viewName, String condition, String groupColumn, String periodColumn,
			String aggregateColumn) throws Exception {
		RpcClient rpc = openRemoteRpc(sp, id, dataAgent, viewName, condition);
		try {
			String alias = "ShellStatistic_" + id;
			JSONArray groupColumns = new JSONArray().put(groupColumn);
			if(periodColumn != null) groupColumns.put(periodColumn);
			String groupsJson = groupColumns.toString();
			int count = Integer.parseInt(requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "computeAggregateDataSet",
					new VectorUtil().addElement(alias).addElement(groupsJson).toVector()),
					"compute grouped aggregate", dataAgent).trim());
			if(count < 1) return Collections.emptyList();
			JSONArray rows = new JSONArray(requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "load",
					new VectorUtil().addElement(alias).addElement(0).addElement(count).toVector()),
					"load grouped aggregate", dataAgent));
			List<PeriodPieValue> values = new ArrayList<PeriodPieValue>();
			String latestPeriod = null;
			for(int i = 0; i < rows.length(); i++) {
				JSONObject row = rows.getJSONObject(i);
				String label = StringUtils.defaultIfBlank(StringUtils.trimToNull(row.optString(groupColumn, null)), "No Country");
				String period = periodColumn == null ? null : StringUtils.trimToNull(row.optString(periodColumn, null));
				JSONObject aggregate = selectAggregate(row.optJSONArray("_aggregateValues"), aggregateColumn);
				Double value = numberValue(aggregate.opt("value"));
				if(value != null && value.doubleValue() > 0.0d) {
					values.add(new PeriodPieValue(label, period, value.doubleValue()));
					if(period != null && (latestPeriod == null || period.compareTo(latestPeriod) > 0)) latestPeriod = period;
				}
			}
			List<PieValue> latestValues = new ArrayList<PieValue>();
			for(PeriodPieValue value : values) {
				if(periodColumn == null || StringUtils.equals(latestPeriod, value.period)) {
					latestValues.add(new PieValue(value.label, value.value));
				}
			}
			return latestValues;
		} finally {
			try { rpc.close(); } catch(Exception ex) { UniLog.log1("Cannot close statistic RPC %s: %s", id, ex.getMessage()); }
		}
	}

	private static RpcClient openRemoteRpc(SessionHelper sp, String id, String dataAgent,
			String viewName, String condition) throws Exception {
		String host = StringUtils.trimToNull(BiConfig.getString(sp, "AgentRpcHost_" + dataAgent));
		String portText = StringUtils.trimToNull(BiConfig.getString(sp, "AgentRpcPort_" + dataAgent));
		if(host == null || portText == null) throw new IllegalArgumentException("RPC host/port is blank for " + dataAgent);
		RpcClient rpc = new RpcClient(host, Integer.parseInt(portText));
		rpc.setTimeout(300000);
		String alias = "ShellStatistic_" + id;
		try {
			rpc.open();
			requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "login", new VectorUtil()
					.addElement("").addElement(sp.getLoginId()).addElement("").toVector()), "login", dataAgent);
			requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "view", new VectorUtil()
					.addElement(viewName).addElement(alias).toVector()), "open view", dataAgent);
			requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "query", new VectorUtil()
					.addElement(alias).addElement(StringUtils.defaultString(condition)).toVector()), "query", dataAgent);
			return rpc;
		} catch(Exception ex) {
			try { rpc.close(); } catch(Exception ignored) { }
			throw ex;
		}
	}

	private static JSONObject selectAggregate(JSONArray aggregates, String aggregateColumn) {
		if(aggregates == null) throw new IllegalArgumentException("Aggregate values are missing from RPC response");
		for(int i = 0; i < aggregates.length(); i++) {
			JSONObject aggregate = aggregates.getJSONObject(i);
			if(StringUtils.equals(aggregateColumn, aggregate.optString("column", null))) return aggregate;
		}
		throw new IllegalArgumentException("Aggregate column not found: " + aggregateColumn);
	}

	private static Double numberValue(Object value) {
		if(value == null || value == JSONObject.NULL) return null;
		if(value instanceof Number) return ((Number) value).doubleValue();
		try { return Double.valueOf(String.valueOf(value)); } catch(NumberFormatException ex) { return null; }
	}

	private static String requireOk(Value value, String operation, String agent) {
		String response = value == null ? null : value.toString();
		if(response == null || !response.startsWith("OK")) {
			throw new IllegalStateException("RPC " + operation + " failed for " + agent + ": "
					+ StringUtils.defaultString(response, "no response"));
		}
		return response.length() <= 4 ? "" : response.substring(4);
	}

	private static StatisticWidget pending(SessionHelper sp, String id, String prefix) {
		return new StatisticWidget(id, StringUtils.defaultIfBlank(BiConfig.getString(sp, prefix + "Title"), id),
				columnClass(BiConfig.getString(sp, prefix + "Width")), Collections.<String>emptyList(),
				Collections.<Double>emptyList(), "Refreshing statistic...", false);
	}

	private static StatisticWidget unavailable(String id, String title, String columnClass, String reason) {
		UniLog.log1("Statistic %s is unavailable: %s", id, StringUtils.defaultString(reason));
		return new StatisticWidget(id, title, columnClass, Collections.<String>emptyList(),
				Collections.<Double>emptyList(), reason, false);
	}

	private static StatisticCache getCache(String agent) {
		synchronized(BiConfig.class) {
			Object current = BiConfig.getAgentData(agent, CACHE_KEY);
			if(current instanceof StatisticCache) return (StatisticCache) current;
			StatisticCache cache = new StatisticCache();
			BiConfig.putAgent(agent, CACHE_KEY, cache);
			return cache;
		}
	}

	private static int refreshDelaySeconds(SessionHelper sp, String prefix) {
		return positiveInt(BiConfig.getString(sp, prefix + "RefreshDelay"), 0);
	}

	private static int positiveInt(String value, int defaultValue) {
		try { return Math.max(0, Integer.parseInt(StringUtils.trimToEmpty(value))); }
		catch(NumberFormatException ex) { return defaultValue; }
	}

	private static boolean isEnabled(String value) {
		return StringUtils.isBlank(value) || StringUtils.equalsAnyIgnoreCase(StringUtils.trim(value), "Y", "YES", "TRUE", "1");
	}

	private static boolean isAllowed(SessionHelper sp, String prefix) {
		String access = StringUtils.trimToNull(BiConfig.getString(sp, prefix + "Access"));
		boolean allowAdmin = StringUtils.equalsAnyIgnoreCase(StringUtils.trim(BiConfig.getString(sp, prefix + "AllowAdmin")), "Y", "YES", "TRUE", "1");
		return access == null || sp.hasAccessRight(access) || (allowAdmin && sp.isAdminUser());
	}

	private static String columnClass(String width) {
		String value = StringUtils.defaultIfBlank(width, "half").trim().toLowerCase(Locale.ROOT);
		if("quarter".equals(value)) return "col-md-6 col-xl-3";
		if("full".equals(value)) return "col-12";
		return DEFAULT_COLUMN_CLASS;
	}

	private static class PieValue {
		protected final String label;
		protected final double value;
		private PieValue(String label, double value) { this.label = label; this.value = value; }
	}

	private static final class PeriodPieValue extends PieValue {
		private final String period;
		private PeriodPieValue(String label, String period, double value) {
			super(label, value);
			this.period = period;
		}
	}

	private static final class CachedStatistic {
		private final StatisticWidget widget;
		private final long loadedAt = System.currentTimeMillis();
		private CachedStatistic(StatisticWidget widget) { this.widget = widget; }
		private boolean isExpired(int delaySeconds) { return System.currentTimeMillis() - loadedAt >= delaySeconds * 1000L; }
	}

	private static final class StatisticCache {
		private final Map<String, CachedStatistic> values = new HashMap<String, CachedStatistic>();
		private synchronized CachedStatistic get(String id) { return values.get(id); }
		private synchronized void put(String id, CachedStatistic statistic) { values.put(id, statistic); }
	}
}
