package com.kikyosoft.servlet.larva.dashboard;

import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.kikyosoft.utils.MenuNode;
import com.uniinformation.erpv4.BiConfig;
import com.uniinformation.webcore.SessionHelper;

/** Builds the dashboard roadmap from the already access-filtered shell menu. */
public final class RoadMapWidgetConfigurator {
	private static final String PREFIX = "ShellRoadMap_";

	private RoadMapWidgetConfigurator() {}

	public static RoadMapWidget load(SessionHelper sp, List<MenuNode> nodes) {
		if(!isYes(BiConfig.getString(sp, PREFIX + "Enabled"))) return null;
		if(!isAllowed(sp)) return null;
		if(nodes == null || nodes.isEmpty()) return null;

		String title = StringUtils.defaultIfBlank(
				BiConfig.getString(sp, PREFIX + "Title"), "Roadmap");
		String columnClass = columnClass(BiConfig.getString(sp, PREFIX + "Width"));
		return new RoadMapWidget(title, columnClass, nodes);
	}

	private static boolean isAllowed(SessionHelper sp) {
		String access = StringUtils.trimToNull(BiConfig.getString(sp, PREFIX + "Access"));
		boolean allowAdmin = isYes(BiConfig.getString(sp, PREFIX + "AllowAdmin"));
		return access == null || sp.hasAccessRight(access) || (allowAdmin && sp.isAdminUser());
	}

	private static boolean isYes(String value) {
		return StringUtils.equalsAnyIgnoreCase(
				StringUtils.trim(value), "Y", "YES", "TRUE", "1");
	}

	private static String columnClass(String width) {
		String value = StringUtils.defaultIfBlank(width, "full").trim().toLowerCase(Locale.ROOT);
		if("half".equals(value)) return "col-md-6";
		if("two-thirds".equals(value)) return "col-lg-8";
		return "col-12";
	}
}
