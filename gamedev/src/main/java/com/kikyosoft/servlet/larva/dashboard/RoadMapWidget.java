package com.kikyosoft.servlet.larva.dashboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.kikyosoft.utils.MenuNode;

/** Render-ready roadmap of the menu entries available to the current user. */
public final class RoadMapWidget {
	private final String title;
	private final String columnClass;
	private final List<MenuNode> nodes;

	RoadMapWidget(String title, String columnClass, List<MenuNode> nodes) {
		this.title = title;
		this.columnClass = columnClass;
		this.nodes = Collections.unmodifiableList(new ArrayList<MenuNode>(nodes));
	}

	public String getTitle() { return title; }
	public String getColumnClass() { return columnClass; }
	public List<MenuNode> getNodes() { return nodes; }
}
