package com.kikyosoft.servlet.larva.dashboard;

import java.util.List;

/** Render-ready ranked ledger/list widget for the shell dashboard. */
public final class LedderWidget {
	private final String id;
	private final String title;
	private final String columnClass;
	private final List<Row> rows;
	private final String note;
	private final boolean available;

	LedderWidget(String id, String title, String columnClass, List<Row> rows, String note, boolean available) {
		this.id = id;
		this.title = title;
		this.columnClass = columnClass;
		this.rows = rows;
		this.note = note;
		this.available = available;
	}
	public String getId() { return id; }
	public String getTitle() { return title; }
	public String getColumnClass() { return columnClass; }
	public List<Row> getRows() { return rows; }
	public String getNote() { return note; }
	public boolean isAvailable() { return available; }

	public static final class Row {
		private final int rank;
		private final String label;
		private final String formattedValue;
		private final String suffix;
		Row(int rank, String label, String formattedValue, String suffix) {
			this.rank = rank;
			this.label = label;
			this.formattedValue = formattedValue;
			this.suffix = suffix;
		}
		public int getRank() { return rank; }
		public String getLabel() { return label; }
		public String getFormattedValue() { return formattedValue; }
		public String getSuffix() { return suffix; }
	}
}
