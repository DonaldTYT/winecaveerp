package com.kikyosoft.servlet.larva.dashboard;

/**
 * Render-ready definition of one dashboard KPI tile.
 *
 * Values in this object have already been validated and formatted by
 * {@link KpiWidgetConfigurator}; the JSP only renders them.
 */
public final class KpiWidget {
	private final String id;
	private final String title;
	private final String formattedValue;
	private final String prefix;
	private final String suffix;
	private final String note;
	private final String icon;
	private final String tone;
	private final String columnClass;
	private final String badgeText;
	private final String badgeIcon;
	private final boolean available;

	KpiWidget(String id, String title, String formattedValue, String prefix,
			String suffix, String note, String icon, String tone,
			String columnClass, String badgeText, String badgeIcon,
			boolean available) {
		this.id = id;
		this.title = title;
		this.formattedValue = formattedValue;
		this.prefix = prefix;
		this.suffix = suffix;
		this.note = note;
		this.icon = icon;
		this.tone = tone;
		this.columnClass = columnClass;
		this.badgeText = badgeText;
		this.badgeIcon = badgeIcon;
		this.available = available;
	}

	public String getId() { return id; }
	public String getTitle() { return title; }
	public String getFormattedValue() { return formattedValue; }
	public String getPrefix() { return prefix; }
	public String getSuffix() { return suffix; }
	public String getNote() { return note; }
	public String getIcon() { return icon; }
	public String getTone() { return tone; }
	public String getColumnClass() { return columnClass; }
	public String getBadgeText() { return badgeText; }
	public String getBadgeIcon() { return badgeIcon; }
	public boolean isAvailable() { return available; }
	public boolean isShowBadge() { return badgeText != null; }
	public boolean isShowIcon() { return icon != null; }
}
