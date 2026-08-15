package com.kikyosoft.servlet.larva.dashboard;

import java.text.DecimalFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import com.kyoko.common.ReturnMsg;
import com.kikyosoft.rpccall.RpcClient;
import com.kikyosoft.rpccall.Value;
import com.kikyosoft.utils.VectorUtil;
import com.uniinformation.bicore.AggregateOrPivot;
import com.uniinformation.bicore.AggregateOrPivot.AggregateRec;
import com.uniinformation.bicore.BiResult;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.utils.UniLog;
import com.uniinformation.webcore.SessionHelper;

/**
 * Loads ordered KPI tile definitions from the current agent configuration and
 * obtains each value through the normal BI view/query/aggregate pipeline.
 * No SQL, agent name, view name, or business-specific KPI is hardcoded here.
 */
public final class KpiWidgetConfigurator {
	private static final String BICORE_RPC_PREFIX = "com.uniinformation.bicore.BiCoreRpcServlet.";
	private static final String DEFAULT_FORMAT = "#,##0.##";
	private static final String DEFAULT_COLUMN_CLASS = "col-md-6 col-xl-3";
	private static final String KPI_TYPE_PERIOD_COMPARE = "periodcompare";
	/** Agent-scoped cache key. The value itself is stored through BiConfig so
	 *  shell instances for different agents never share a KPI result. */
	private static final String CACHE_KEY = "KpiWidgetConfigurator.cache";

	private KpiWidgetConfigurator() {}

	public static List<KpiWidget> load(SessionHelper sp) {
		List<KpiWidget> widgets = new ArrayList<KpiWidget>();
		String widgetIds = BiConfig.getString(sp, "ShellKpiIds");
		if(StringUtils.isBlank(widgetIds)) return widgets;

		for(String rawId : StringUtils.split(widgetIds, ',')) {
			String id = StringUtils.trimToNull(rawId);
			if(id == null) continue;
			String configPrefix = "ShellKpi_" + id + "_";
			if(!isEnabled(BiConfig.getString(sp, configPrefix + "Enabled"))) continue;
			if(!isAllowed(sp, configPrefix)) continue;
			widgets.add(getReadyWidget(sp, id, configPrefix));
		}
		return widgets;
	}

	/**
	 * Called only by the cron job. Dashboard requests deliberately do not call
	 * this method: a slow remote BI/RPC request must never hold up a page.
	 */
	public static void refresh(SessionHelper sp) {
		String widgetIds = BiConfig.getString(sp, "ShellKpiIds");
		if(StringUtils.isBlank(widgetIds)) return;
		for(String rawId : StringUtils.split(widgetIds, ',')) {
			String id = StringUtils.trimToNull(rawId);
			if(id == null) continue;
			String configPrefix = "ShellKpi_" + id + "_";
			if(!isEnabled(BiConfig.getString(sp, configPrefix + "Enabled"))) continue;
			refreshOne(sp, id, configPrefix);
		}
	}

	private static KpiWidget getReadyWidget(SessionHelper sp, String id, String configPrefix) {
		CachedKpiWidget cached = getCache(sp.getAgent()).get(id);
		return cached == null ? pending(id, sp, configPrefix) : cached.widget;
	}

	private static KpiWidget refreshOne(SessionHelper sp, String id, String configPrefix) {
		int refreshDelaySeconds = getRefreshDelaySeconds(sp, configPrefix);
		KpiWidgetCache cache = getCache(sp.getAgent());
		CachedKpiWidget cached = cache.get(id);
		if(refreshDelaySeconds > 0 && cached != null && !cached.isExpired(refreshDelaySeconds)) {
			return cached.widget;
		}

		// This may take minutes; it intentionally happens outside the cache lock.
		KpiWidget widget = loadOneUncached(sp, id, configPrefix);
		if(widget.isAvailable()) {
			cache.put(id, new CachedKpiWidget(widget));
		}
		return widget;
	}

	private static KpiWidget loadOneUncached(SessionHelper sp, String id, String configPrefix) {
		String title = StringUtils.defaultIfBlank(
				BiConfig.getString(sp, configPrefix + "Title"), id);
		String prefix = StringUtils.trimToEmpty(BiConfig.getString(sp, configPrefix + "Prefix"));
		String suffix = StringUtils.trimToEmpty(BiConfig.getString(sp, configPrefix + "Suffix"));
		String note = StringUtils.trimToNull(BiConfig.getString(sp, configPrefix + "Note"));
		String tone = safeTone(BiConfig.getString(sp, configPrefix + "Tone"));
		String icon = safeIcon(BiConfig.getString(sp, configPrefix + "Icon"));
		String badgeText = StringUtils.trimToNull(BiConfig.getString(sp, configPrefix + "BadgeText"));
		String badgeIcon = safeIcon(BiConfig.getString(sp, configPrefix + "BadgeIcon"));
		String columnClass = columnClass(BiConfig.getString(sp, configPrefix + "Width"));
		String viewName = StringUtils.trimToNull(BiConfig.getString(sp, configPrefix + "View"));
		String aggregateColumn = StringUtils.trimToNull(
				BiConfig.getString(sp, configPrefix + "AggregateColumn"));
		String condition = StringUtils.trimToNull(BiConfig.getString(sp, configPrefix + "Condition"));
		String configuredFormat = StringUtils.trimToNull(BiConfig.getString(sp, configPrefix + "Format"));
		String type = StringUtils.defaultIfBlank(BiConfig.getString(sp, configPrefix + "Type"), "aggregate")
				.trim().toLowerCase(Locale.ROOT);
		String periodColumn = StringUtils.trimToNull(BiConfig.getString(sp, configPrefix + "PeriodColumn"));
		String dataAgent = StringUtils.trimToNull(BiConfig.getString(sp, configPrefix + "Agent"));
		if(dataAgent == null) {
			// Keep compatibility with the initially proposed lower-case setting.
			dataAgent = StringUtils.trimToNull(BiConfig.getString(sp, configPrefix + "agent"));
		}

		if(viewName == null) {
			return unavailable(id, title, prefix, suffix, note, icon, tone,
					columnClass, badgeText, badgeIcon, "View is blank");
		}
		if(KPI_TYPE_PERIOD_COMPARE.equals(type)) {
			if(dataAgent == null) {
				return unavailable(id, title, prefix, suffix, note, icon, tone,
						columnClass, badgeText, badgeIcon, "Period comparison KPI requires Agent");
			}
			if(periodColumn == null) {
				return unavailable(id, title, prefix, suffix, note, icon, tone,
						columnClass, badgeText, badgeIcon, "PeriodColumn is blank");
			}
			try {
				RemotePeriodComparison comparison = loadRemotePeriodComparison(sp, id, dataAgent,
						viewName, aggregateColumn, condition, periodColumn);
				String format = configuredFormat == null ? comparison.format : configuredFormat;
				String comparisonBadge = badgeText == null ? formatPercentChange(comparison) : badgeText;
				String comparisonNote = note == null ? comparisonNote(comparison) : note;
				return new KpiWidget(id, title, formatValue(comparison.latestValue, format), prefix,
						suffix, comparisonNote, icon, tone, columnClass, comparisonBadge, badgeIcon, true);
			} catch(Exception ex) {
				return unavailable(id, title, prefix, suffix, note, icon, tone,
						columnClass, badgeText, badgeIcon, ex.getMessage());
			}
		}
		if(dataAgent != null) {
			try {
				RemoteAggregate remote = loadRemoteAggregate(sp, id, dataAgent, viewName,
						aggregateColumn, condition);
				String format = configuredFormat == null ? remote.format : configuredFormat;
				return new KpiWidget(id, title, formatValue(remote.value, format), prefix,
						suffix, note, icon, tone, columnClass, badgeText, badgeIcon, true);
			} catch(Exception ex) {
				return unavailable(id, title, prefix, suffix, note, icon, tone,
						columnClass, badgeText, badgeIcon, ex.getMessage());
			}
		}

		BiResult result = null;
		try {
			result = sp.newBiResult(viewName);
			if(result == null) throw new IllegalArgumentException("BI view not found: " + viewName);
			result.clear();
			result.clearCondition();
			if(condition != null) {
				ReturnMsg conditionResult = result.addCustomCondition(condition, false);
				if(conditionResult == null || !conditionResult.getStatus()) {
					throw new IllegalArgumentException("Invalid condition: "
							+ (conditionResult == null ? "no result" : conditionResult.getMsg()));
				}
			}
			ReturnMsg queryResult = result.query(true);
			if(queryResult == null || !queryResult.getStatus()) {
				throw new IllegalStateException("BI query failed: "
						+ (queryResult == null ? "no result" : queryResult.getMsg()));
			}

			AggregateOrPivot aggregate = result.computeGroupedDataSet(Collections.<String>emptyList());
			if(aggregate == null || aggregate.getAggsArr().isEmpty()) {
				throw new IllegalArgumentException("BI view has no visible aggregate columns");
			}
			int aggregateIndex = findAggregateIndex(aggregate, aggregateColumn);
			Object[] subtotals = result.getAggregateSubtotal();
			if(subtotals == null || aggregateIndex >= subtotals.length) {
				throw new IllegalStateException("Aggregate produced no subtotal");
			}
			AggregateRec aggregateRec = aggregate.getAggsArr().get(aggregateIndex);
			String format = configuredFormat;
			if(format == null) format = aggregateRec.getFormat(result);
			String formattedValue = formatValue(subtotals[aggregateIndex], format);
			return new KpiWidget(id, title, formattedValue, prefix, suffix, note,
					icon, tone, columnClass, badgeText, badgeIcon, true);
		} catch(Exception ex) {
			return unavailable(id, title, prefix, suffix, note, icon, tone,
					columnClass, badgeText, badgeIcon, ex.getMessage());
		} finally {
			if(result != null) {
				try { result.close(); } catch(Exception ex) {
					UniLog.log1("Cannot close KPI BiResult %s: %s", id, ex.getMessage());
				}
			}
		}
	}

	/**
	 * Omitted or zero delay refreshes on every cron poll. A positive delay
	 * shares a successful result across all browser sessions that run under the
	 * same shell agent.
	 */
	private static int getRefreshDelaySeconds(SessionHelper sp, String configPrefix) {
		String value = StringUtils.trimToNull(BiConfig.getString(sp, configPrefix + "RefreshDelay"));
		if(value == null) return 0;
		try {
			int seconds = Integer.parseInt(value);
			return Math.max(0, seconds);
		} catch(NumberFormatException ex) {
			UniLog.log1("Invalid KPI RefreshDelay %s for %s; caching is disabled", value, configPrefix);
			return 0;
		}
	}

	private static KpiWidgetCache getCache(String agent) {
		/*
		 * BiConfig.agentData is the application-wide, agent-isolated state
		 * holder. Synchronize creation so simultaneous first dashboard requests
		 * for the same agent use the same cache and then one refresh at a time.
		 */
		synchronized(BiConfig.class) {
			Object existing = BiConfig.getAgentData(agent, CACHE_KEY);
			if(existing instanceof KpiWidgetCache) return (KpiWidgetCache) existing;
			KpiWidgetCache cache = new KpiWidgetCache();
			BiConfig.putAgent(agent, CACHE_KEY, cache);
			return cache;
		}
	}

	private static final class KpiWidgetCache {
		private final Map<String, CachedKpiWidget> values = new HashMap<String, CachedKpiWidget>();

		private synchronized CachedKpiWidget get(String id) {
			return values.get(id);
		}

		private synchronized void put(String id, CachedKpiWidget value) {
			values.put(id, value);
		}
	}

	private static final class CachedKpiWidget {
		private final KpiWidget widget;
		private final long loadedAtMillis;

		private CachedKpiWidget(KpiWidget widget) {
			this.widget = widget;
			this.loadedAtMillis = System.currentTimeMillis();
		}

		private boolean isExpired(int refreshDelaySeconds) {
			long delayMillis = refreshDelaySeconds * 1000L;
			return System.currentTimeMillis() - loadedAtMillis >= delayMillis;
		}
	}

	/**
	 * Reads one aggregate row for each period from the remote BI server.  This
	 * keeps the shell database-free: the source agent owns both the BI schema
	 * and the storage snapshot data.
	 */
	private static RemotePeriodComparison loadRemotePeriodComparison(SessionHelper sp, String id,
			String dataAgent, String viewName, String aggregateColumn, String condition,
			String periodColumn) throws Exception {
		RpcClient rpc = openRemoteRpc(sp, id, dataAgent, viewName, condition);
		try {
			String groupsJson = new JSONArray().put(periodColumn).toString();
			String aggregateResponse = requireOk(rpc.callSegment(
					BICORE_RPC_PREFIX + "computeAggregateDataSet",
					new VectorUtil().addElement("ShellKpi_" + id).addElement(groupsJson).toVector()),
					"compute period aggregate", dataAgent);
			int groupCount = Integer.parseInt(aggregateResponse.trim());
			if(groupCount < 1) throw new IllegalStateException("No storage periods returned");
			String loadResponse = requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "load",
					new VectorUtil().addElement("ShellKpi_" + id).addElement(0).addElement(groupCount).toVector()),
					"load period aggregate", dataAgent);
			JSONArray rows = new JSONArray(loadResponse);
			Map<YearMonth, RemoteAggregate> valuesByPeriod = new LinkedHashMap<YearMonth, RemoteAggregate>();
			for(int i = 0; i < rows.length(); i++) {
				JSONObject row = rows.getJSONObject(i);
				String periodText = StringUtils.trimToNull(row.optString(periodColumn, null));
				if(periodText == null) continue;
				try {
					YearMonth period = YearMonth.parse(periodText);
					JSONArray values = row.optJSONArray("_aggregateValues");
					if(values == null) continue;
					JSONObject selected = selectRemoteAggregate(values, aggregateColumn);
					Object value = selected.opt("value");
					if(value == JSONObject.NULL) value = null;
					valuesByPeriod.put(period, new RemoteAggregate(value,
							StringUtils.trimToNull(selected.optString("format", null))));
				} catch(Exception ex) {
					UniLog.log1("Ignoring invalid KPI period %s: %s", periodText, ex.getMessage());
				}
			}
			if(valuesByPeriod.isEmpty()) throw new IllegalStateException(
					"No valid yyyy-MM values returned for " + periodColumn);
			YearMonth latestPeriod = Collections.max(valuesByPeriod.keySet());
			YearMonth previousPeriod = latestPeriod.minusMonths(1);
			RemoteAggregate latest = valuesByPeriod.get(latestPeriod);
			RemoteAggregate previous = valuesByPeriod.get(previousPeriod);
			if(previous == null) throw new IllegalStateException(
					"No previous-month storage snapshot for " + previousPeriod);
			return new RemotePeriodComparison(latestPeriod, latest, previousPeriod, previous);
		} finally {
			try { rpc.close(); } catch(Exception ex) {
				UniLog.log1("Cannot close KPI RPC client %s: %s", id, ex.getMessage());
			}
		}
	}

	private static RemoteAggregate loadRemoteAggregate(SessionHelper sp, String id,
			String dataAgent, String viewName, String aggregateColumn, String condition) throws Exception {
		RpcClient rpc = openRemoteRpc(sp, id, dataAgent, viewName, condition);
		try {
			String alias = "ShellKpi_" + id;
			String aggregateResponse = requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "computeAggregateDataSet",
					new VectorUtil().addElement(alias).addElement("[]").toVector()),
					"compute aggregate", dataAgent);
			int aggregateRows = Integer.parseInt(aggregateResponse.trim());
			if(aggregateRows < 1) throw new IllegalStateException("Aggregate returned no rows");
			String loadResponse = requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "load",
					new VectorUtil().addElement(alias).addElement(0).addElement(1).toVector()),
					"load aggregate", dataAgent);
			JSONArray rows = new JSONArray(loadResponse);
			if(rows.length() == 0) throw new IllegalStateException("Aggregate load returned no rows");
			JSONArray values = rows.getJSONObject(0).optJSONArray("_aggregateValues");
			if(values == null || values.length() == 0) {
				throw new IllegalStateException("Aggregate values are missing from RPC load");
			}
			JSONObject selected = selectRemoteAggregate(values, aggregateColumn);
			Object value = selected.opt("value");
			if(value == JSONObject.NULL) value = null;
			return new RemoteAggregate(value, StringUtils.trimToNull(selected.optString("format", null)));
		} finally {
			try { rpc.close(); } catch(Exception ex) {
				UniLog.log1("Cannot close KPI RPC client %s: %s", id, ex.getMessage());
			}
		}
	}

	private static RpcClient openRemoteRpc(SessionHelper sp, String id, String dataAgent,
			String viewName, String condition) throws Exception {
		String rpcHost = StringUtils.trimToNull(BiConfig.getString(sp, "AgentRpcHost_" + dataAgent));
		String rpcPortText = StringUtils.trimToNull(BiConfig.getString(sp, "AgentRpcPort_" + dataAgent));
		if(rpcHost == null || rpcPortText == null) {
			throw new IllegalArgumentException("RPC host/port is not configured for agent " + dataAgent);
		}
		int rpcPort;
		try {
			rpcPort = Integer.parseInt(rpcPortText);
		} catch(NumberFormatException ex) {
			throw new IllegalArgumentException("Invalid RPC port for agent " + dataAgent + ": " + rpcPortText);
		}

		RpcClient rpc = new RpcClient(rpcHost, rpcPort);
		rpc.setTimeout(300000); /* 5 mins timeout */
		String alias = "ShellKpi_" + id;
		try {
			rpc.open();
			requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "login",
					// The destination agent is selected by its server's iniAgent default.
					// dataAgent above is only the shell configuration key for host/port.
					new VectorUtil().addElement("").addElement(sp.getLoginId()).addElement("").toVector()),
					"login", dataAgent);
			requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "view",
					new VectorUtil().addElement(viewName).addElement(alias).toVector()),
					"open view", dataAgent);
			requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "query",
					new VectorUtil().addElement(alias).addElement(StringUtils.defaultString(condition)).toVector()),
					"query", dataAgent);
			return rpc;
		} catch(Exception ex) {
			try { rpc.close(); } catch(Exception closeEx) {
				UniLog.log1("Cannot close failed KPI RPC client %s: %s", id, closeEx.getMessage());
			}
			throw ex;
		}
	}

	private static JSONObject selectRemoteAggregate(JSONArray values, String aggregateColumn) {
		if(aggregateColumn == null) {
			if(values.length() == 1) return values.getJSONObject(0);
			throw new IllegalArgumentException(
					"AggregateColumn is required when the BI view has multiple aggregates");
		}
		for(int i = 0; i < values.length(); i++) {
			JSONObject value = values.getJSONObject(i);
			if(StringUtils.equals(aggregateColumn, value.optString("column", null))) return value;
		}
		throw new IllegalArgumentException("Aggregate column not found: " + aggregateColumn);
	}

	private static String requireOk(Value value, String operation, String dataAgent) {
		String response = value == null ? null : value.toString();
		if(response == null || !response.startsWith("OK")) {
			throw new IllegalStateException("RPC " + operation + " failed for agent " + dataAgent
					+ ": " + StringUtils.defaultString(response, "no response"));
		}
		return response.length() <= 4 ? "" : response.substring(4);
	}

	private static final class RemoteAggregate {
		private final Object value;
		private final String format;

		private RemoteAggregate(Object value, String format) {
			this.value = value;
			this.format = format;
		}
	}

	private static final class RemotePeriodComparison {
		private final YearMonth latestPeriod;
		private final Object latestValue;
		private final String format;
		private final YearMonth previousPeriod;
		private final Object previousValue;

		private RemotePeriodComparison(YearMonth latestPeriod, RemoteAggregate latest,
				YearMonth previousPeriod, RemoteAggregate previous) {
			this.latestPeriod = latestPeriod;
			this.latestValue = latest.value;
			this.format = latest.format;
			this.previousPeriod = previousPeriod;
			this.previousValue = previous.value;
		}
	}

	private static String formatPercentChange(RemotePeriodComparison comparison) {
		if(!(comparison.latestValue instanceof Number) || !(comparison.previousValue instanceof Number)) {
			return null;
		}
		double previous = ((Number) comparison.previousValue).doubleValue();
		double latest = ((Number) comparison.latestValue).doubleValue();
		if(Double.isNaN(previous) || Double.isNaN(latest) || previous == 0.0d) return "n/a";
		double percent = ((latest - previous) / Math.abs(previous)) * 100.0d;
		String text = new DecimalFormat("0.0").format(Math.abs(percent)) + "%";
		return percent > 0.0d ? "+" + text : (percent < 0.0d ? "-" + text : text);
	}

	private static String comparisonNote(RemotePeriodComparison comparison) {
		return "Compared with " + comparison.previousPeriod;
	}

	private static int findAggregateIndex(AggregateOrPivot aggregate, String aggregateColumn) {
		if(aggregateColumn == null) {
			if(aggregate.getAggsArr().size() == 1) return 0;
			throw new IllegalArgumentException(
					"AggregateColumn is required when the BI view has multiple aggregates");
		}
		for(int i = 0; i < aggregate.getAggsArr().size(); i++) {
			if(StringUtils.equals(aggregateColumn, aggregate.getAggsArr().get(i).getKey())) return i;
		}
		throw new IllegalArgumentException("Aggregate column not found: " + aggregateColumn);
	}

	private static String formatValue(Object value, String pattern) {
		if(value == null) return "0";
		if(!(value instanceof Number)) return String.valueOf(value);
		String safePattern = StringUtils.defaultIfBlank(pattern, DEFAULT_FORMAT);
		try {
			return new DecimalFormat(safePattern).format(value);
		} catch(IllegalArgumentException ex) {
			UniLog.log1("Invalid KPI number format %s; using %s", safePattern, DEFAULT_FORMAT);
			return new DecimalFormat(DEFAULT_FORMAT).format(value);
		}
	}

	private static KpiWidget pending(String id, SessionHelper sp, String configPrefix) {
		String title = StringUtils.defaultIfBlank(BiConfig.getString(sp, configPrefix + "Title"), id);
		String prefix = StringUtils.trimToEmpty(BiConfig.getString(sp, configPrefix + "Prefix"));
		String suffix = StringUtils.trimToEmpty(BiConfig.getString(sp, configPrefix + "Suffix"));
		String note = StringUtils.defaultIfBlank(
				StringUtils.trimToNull(BiConfig.getString(sp, configPrefix + "Note")),
				"Refreshing statistic...");
		String icon = safeIcon(BiConfig.getString(sp, configPrefix + "Icon"));
		String tone = safeTone(BiConfig.getString(sp, configPrefix + "Tone"));
		String columnClass = columnClass(BiConfig.getString(sp, configPrefix + "Width"));
		return new KpiWidget(id, title, "--", prefix, suffix, note, icon,
				tone, columnClass, null, null, false);
	}

	private static KpiWidget unavailable(String id, String title, String prefix,
			String suffix, String note, String icon, String tone, String columnClass,
			String badgeText, String badgeIcon, String reason) {
		UniLog.log1("KPI %s is unavailable: %s", id, StringUtils.defaultString(reason));
		return new KpiWidget(id, title, "--", prefix, suffix, note, icon,
				tone, columnClass, badgeText, badgeIcon, false);
	}

	private static boolean isEnabled(String value) {
		return StringUtils.isBlank(value)
				|| StringUtils.equalsAnyIgnoreCase(StringUtils.trim(value), "Y", "YES", "TRUE", "1");
	}

	private static boolean isAllowed(SessionHelper sp, String configPrefix) {
		String access = StringUtils.trimToNull(BiConfig.getString(sp, configPrefix + "Access"));
		boolean allowAdmin = StringUtils.equalsAnyIgnoreCase(
				StringUtils.trim(BiConfig.getString(sp, configPrefix + "AllowAdmin")),
				"Y", "YES", "TRUE", "1");
		return access == null || sp.hasAccessRight(access) || (allowAdmin && sp.isAdminUser());
	}

	private static String safeTone(String value) {
		String tone = StringUtils.defaultIfBlank(value, "primary").trim().toLowerCase(Locale.ROOT);
		return StringUtils.equalsAny(tone, "primary", "secondary", "success",
				"danger", "warning", "info") ? tone : "primary";
	}

	private static String safeIcon(String value) {
		String icon = StringUtils.trimToNull(value);
		return icon != null && icon.matches("[A-Za-z0-9 _-]+") ? icon : null;
	}

	private static String columnClass(String width) {
		String normalized = StringUtils.defaultIfBlank(width, "quarter").trim().toLowerCase(Locale.ROOT);
		if("half".equals(normalized)) return "col-md-6";
		if("full".equals(normalized)) return "col-12";
		return DEFAULT_COLUMN_CLASS;
	}
}
