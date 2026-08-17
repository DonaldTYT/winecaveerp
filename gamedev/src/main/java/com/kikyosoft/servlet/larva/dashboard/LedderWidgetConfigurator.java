package com.kikyosoft.servlet.larva.dashboard;

import java.text.DecimalFormat;
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

/** Cron-refreshed ranked list widgets. Browser requests read cache only. */
public final class LedderWidgetConfigurator {
	private static final String BICORE_RPC_PREFIX = "com.uniinformation.bicore.BiCoreRpcServlet.";
	private static final String CACHE_KEY = "LedderWidgetConfigurator.cache";

	private LedderWidgetConfigurator() {}

	public static List<LedderWidget> load(SessionHelper sp) {
		List<LedderWidget> widgets = new ArrayList<LedderWidget>();
		String ids = BiConfig.getString(sp, "ShellLedderIds");
		if(StringUtils.isBlank(ids)) return widgets;
		for(String rawId : StringUtils.split(ids, ',')) {
			String id = StringUtils.trimToNull(rawId);
			if(id == null) continue;
			String prefix = "ShellLedder_" + id + "_";
			if(!isEnabled(BiConfig.getString(sp, prefix + "Enabled")) || !isAllowed(sp, prefix)) continue;
			CachedLedger cached = getCache(sp.getAgent()).get(id);
			widgets.add(cached == null ? pending(sp, id, prefix) : cached.widget);
		}
		return widgets;
	}

	/** Called by WidgetCronJob. */
	public static void refresh(SessionHelper sp) {
		String ids = BiConfig.getString(sp, "ShellLedderIds");
		if(StringUtils.isBlank(ids)) return;
		for(String rawId : StringUtils.split(ids, ',')) {
			String id = StringUtils.trimToNull(rawId);
			if(id == null) continue;
			String prefix = "ShellLedder_" + id + "_";
			if(isEnabled(BiConfig.getString(sp, prefix + "Enabled"))) refreshOne(sp, id, prefix);
		}
	}

	private static void refreshOne(SessionHelper sp, String id, String prefix) {
		LedgerCache cache = getCache(sp.getAgent());
		int delaySeconds = positiveInt(BiConfig.getString(sp, prefix + "RefreshDelay"), 0);
		CachedLedger cached = cache.get(id);
		if(delaySeconds > 0 && cached != null && !cached.isExpired(delaySeconds)) return;
		LedderWidget widget = loadRemoteLedder(sp, id, prefix);
		if(widget.isAvailable()) cache.put(id, new CachedLedger(widget));
	}

	private static LedderWidget loadRemoteLedder(SessionHelper sp, String id, String prefix) {
		String title = StringUtils.defaultIfBlank(BiConfig.getString(sp, prefix + "Title"), id);
		String columnClass = columnClass(BiConfig.getString(sp, prefix + "Width"));
		String view = StringUtils.trimToNull(BiConfig.getString(sp, prefix + "View"));
		String agent = StringUtils.trimToNull(BiConfig.getString(sp, prefix + "Agent"));
		String group = StringUtils.trimToNull(BiConfig.getString(sp, prefix + "GroupColumn"));
		String period = StringUtils.trimToNull(BiConfig.getString(sp, prefix + "PeriodColumn"));
		String aggregate = StringUtils.trimToNull(BiConfig.getString(sp, prefix + "AggregateColumn"));
		String condition = StringUtils.trimToNull(BiConfig.getString(sp, prefix + "Condition"));
		int topCount = positiveInt(BiConfig.getString(sp, prefix + "TopCount"), 10);
		String format = StringUtils.defaultIfBlank(BiConfig.getString(sp, prefix + "Format"), "#,##0");
		String suffix = StringUtils.trimToEmpty(BiConfig.getString(sp, prefix + "Suffix"));
		if(StringUtils.isAnyBlank(view, agent, group, aggregate)) {
			return unavailable(id, title, columnClass, "View, Agent, GroupColumn and AggregateColumn are required");
		}
		try {
			List<ValueRow> values = queryValues(sp, id, agent, view, condition, group, period, aggregate);
			Collections.sort(values, new Comparator<ValueRow>() {
				@Override public int compare(ValueRow left, ValueRow right) { return Double.compare(right.value, left.value); }
			});
			List<LedderWidget.Row> rows = new ArrayList<LedderWidget.Row>();
			DecimalFormat numberFormat = new DecimalFormat(format);
			for(int i = 0; i < values.size() && i < topCount; i++) {
				ValueRow value = values.get(i);
				rows.add(new LedderWidget.Row(i + 1, value.label, numberFormat.format(value.value), suffix));
			}
			if(rows.isEmpty()) return unavailable(id, title, columnClass, "No ledger values returned");
			return new LedderWidget(id, title, columnClass, rows, null, true);
		} catch(Exception ex) {
			return unavailable(id, title, columnClass, ex.getMessage());
		}
	}

	private static List<ValueRow> queryValues(SessionHelper sp, String id, String agent, String view,
			String condition, String groupColumn, String periodColumn, String aggregateColumn) throws Exception {
		RpcClient rpc = openRemoteRpc(sp, id, agent, view, condition);
		try {
			String alias = "ShellLedder_" + id;
			JSONArray groups = new JSONArray().put(groupColumn);
			if(periodColumn != null) groups.put(periodColumn);
			int count = Integer.parseInt(requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "computeAggregateDataSet",
					new VectorUtil().addElement(alias).addElement(groups.toString()).toVector()), "compute ledger", agent).trim());
			if(count < 1) return Collections.emptyList();
			JSONArray rows = new JSONArray(requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "load",
					new VectorUtil().addElement(alias).addElement(0).addElement(count).toVector()), "load ledger", agent));
			List<PeriodValueRow> all = new ArrayList<PeriodValueRow>();
			String latestPeriod = null;
			for(int i = 0; i < rows.length(); i++) {
				JSONObject row = rows.getJSONObject(i);
				String label = StringUtils.defaultIfBlank(StringUtils.trimToNull(row.optString(groupColumn, null)), "No Customer Code");
				String period = periodColumn == null ? null : StringUtils.trimToNull(row.optString(periodColumn, null));
				Double value = numberValue(selectAggregate(row.optJSONArray("_aggregateValues"), aggregateColumn).opt("value"));
				if(value != null && value.doubleValue() > 0.0d) {
					all.add(new PeriodValueRow(label, period, value.doubleValue()));
					if(period != null && (latestPeriod == null || period.compareTo(latestPeriod) > 0)) latestPeriod = period;
				}
			}
			List<ValueRow> latest = new ArrayList<ValueRow>();
			for(PeriodValueRow row : all) if(periodColumn == null || StringUtils.equals(latestPeriod, row.period)) latest.add(new ValueRow(row.label, row.value));
			return latest;
		} finally {
			try { rpc.close(); } catch(Exception ex) { UniLog.log1("Cannot close ledger RPC %s: %s", id, ex.getMessage()); }
		}
	}

	private static RpcClient openRemoteRpc(SessionHelper sp, String id, String agent, String view, String condition) throws Exception {
		String host = StringUtils.trimToNull(BiConfig.getString(sp, "AgentRpcHost_" + agent));
		String portText = StringUtils.trimToNull(BiConfig.getString(sp, "AgentRpcPort_" + agent));
		if(host == null || portText == null) throw new IllegalArgumentException("RPC host/port is blank for " + agent);
		RpcClient rpc = new RpcClient(host, Integer.parseInt(portText));
		rpc.setTimeout(300000);
		String alias = "ShellLedder_" + id;
		try {
			rpc.open();
			requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "login", new VectorUtil().addElement("").addElement(sp.getLoginId()).addElement("").toVector()), "login", agent);
			requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "view", new VectorUtil().addElement(view).addElement(alias).toVector()), "open view", agent);
			requireOk(rpc.callSegment(BICORE_RPC_PREFIX + "query", new VectorUtil().addElement(alias).addElement(StringUtils.defaultString(condition)).toVector()), "query", agent);
			return rpc;
		} catch(Exception ex) { try { rpc.close(); } catch(Exception ignored) { } throw ex; }
	}

	private static JSONObject selectAggregate(JSONArray values, String column) {
		if(values == null) throw new IllegalArgumentException("Aggregate values are missing from RPC response");
		for(int i = 0; i < values.length(); i++) if(StringUtils.equals(column, values.getJSONObject(i).optString("column", null))) return values.getJSONObject(i);
		throw new IllegalArgumentException("Aggregate column not found: " + column);
	}
	private static Double numberValue(Object value) {
		if(value == null || value == JSONObject.NULL) return null;
		if(value instanceof Number) return ((Number) value).doubleValue();
		try { return Double.valueOf(String.valueOf(value)); } catch(NumberFormatException ex) { return null; }
	}
	private static String requireOk(Value response, String operation, String agent) {
		String text = response == null ? null : response.toString();
		if(text == null || !text.startsWith("OK")) throw new IllegalStateException("RPC " + operation + " failed for " + agent + ": " + StringUtils.defaultString(text, "no response"));
		return text.length() <= 4 ? "" : text.substring(4);
	}
	private static LedderWidget pending(SessionHelper sp, String id, String prefix) {
		return new LedderWidget(id, StringUtils.defaultIfBlank(BiConfig.getString(sp, prefix + "Title"), id), columnClass(BiConfig.getString(sp, prefix + "Width")), Collections.<LedderWidget.Row>emptyList(), "Refreshing ledder...", false);
	}
	private static LedderWidget unavailable(String id, String title, String columnClass, String reason) {
		UniLog.log1("Ledder %s is unavailable: %s", id, StringUtils.defaultString(reason));
		return new LedderWidget(id, title, columnClass, Collections.<LedderWidget.Row>emptyList(), reason, false);
	}
	private static LedgerCache getCache(String agent) {
		synchronized(BiConfig.class) {
			Object current = BiConfig.getAgentData(agent, CACHE_KEY);
			if(current instanceof LedgerCache) return (LedgerCache) current;
			LedgerCache cache = new LedgerCache(); BiConfig.putAgent(agent, CACHE_KEY, cache); return cache;
		}
	}
	private static boolean isEnabled(String value) { return StringUtils.isBlank(value) || StringUtils.equalsAnyIgnoreCase(StringUtils.trim(value), "Y", "YES", "TRUE", "1"); }
	private static boolean isAllowed(SessionHelper sp, String prefix) {
		String access = StringUtils.trimToNull(BiConfig.getString(sp, prefix + "Access"));
		boolean admin = StringUtils.equalsAnyIgnoreCase(StringUtils.trim(BiConfig.getString(sp, prefix + "AllowAdmin")), "Y", "YES", "TRUE", "1");
		return access == null || sp.hasAccessRight(access) || (admin && sp.isAdminUser());
	}
	private static int positiveInt(String value, int defaultValue) { try { return Math.max(0, Integer.parseInt(StringUtils.trimToEmpty(value))); } catch(NumberFormatException ex) { return defaultValue; } }
	private static String columnClass(String width) { return "full".equalsIgnoreCase(StringUtils.trim(width)) ? "col-12" : "col-md-6 col-xl-4"; }

	private static class ValueRow { protected final String label; protected final double value; ValueRow(String label, double value) { this.label = label; this.value = value; } }
	private static final class PeriodValueRow extends ValueRow { private final String period; PeriodValueRow(String label, String period, double value) { super(label, value); this.period = period; } }
	private static final class CachedLedger { private final LedderWidget widget; private final long loadedAt = System.currentTimeMillis(); CachedLedger(LedderWidget widget) { this.widget = widget; } boolean isExpired(int seconds) { return System.currentTimeMillis() - loadedAt >= seconds * 1000L; } }
	private static final class LedgerCache { private final Map<String, CachedLedger> values = new HashMap<String, CachedLedger>(); synchronized CachedLedger get(String id) { return values.get(id); } synchronized void put(String id, CachedLedger value) { values.put(id, value); } }
}
