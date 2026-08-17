package com.kikyosoft.servlet.larva.dashboard;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.json.JSONArray;

/** Render-ready, cron-refreshed chart data for the shell dashboard. */
public final class StatisticWidget {
	private final String id;
	private final String title;
	private final String columnClass;
	private final List<String> labels;
	private final List<Double> values;
	private final String note;
	private final boolean available;

	StatisticWidget(String id, String title, String columnClass, List<String> labels,
			List<Double> values, String note, boolean available) {
		this.id = id;
		this.title = title;
		this.columnClass = columnClass;
		this.labels = labels;
		this.values = values;
		this.note = note;
		this.available = available;
	}

	public String getId() { return id; }
	public String getDomId() { return "shell-statistic-" + id.replaceAll("[^A-Za-z0-9_-]", "_"); }
	public String getTitle() { return title; }
	public String getColumnClass() { return columnClass; }
	public String getNote() { return note; }
	public boolean isAvailable() { return available; }
	public boolean isShowChart() { return available && !values.isEmpty(); }

	/** Base64 avoids JSON/HTML attribute escaping problems in country labels. */
	public String getLabelsBase64() { return encode(new JSONArray(labels).toString()); }
	public String getValuesBase64() { return encode(new JSONArray(values).toString()); }

	private static String encode(String json) {
		return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
	}
}
