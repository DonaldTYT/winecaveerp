package com.kikyosoft.utils;

import java.util.ArrayList;
import java.util.List;

public class MenuNode {
private String text;         // e.g. "Dashboard"
private String href;         // e.g. "/larva/dashboard"
private String icon;         // e.g. "ti ti-dashboard" (Tabler icon class)
private boolean caption;     // if true, render as <li class="pc-item pc-caption">...label...</li>
private final List<MenuNode> children = new ArrayList<>();

public MenuNode() {}
public MenuNode(String text, String href, String icon) {
 this.text = text; this.href = href; this.icon = icon;
}

// getters/setters
public String getText() { return text; }
public void setText(String text) { this.text = text; }
public String getHref() { return href; }
public void setHref(String href) { this.href = href; }
public String getIcon() { return icon; }
public void setIcon(String icon) { this.icon = icon; }
public boolean isCaption() { return caption; }
public void setCaption(boolean caption) { this.caption = caption; }
public List<MenuNode> getChildren() { return children; }

public MenuNode add(MenuNode child) { this.children.add(child); return this; }
public static MenuNode caption(String label, String icon) {
 MenuNode n = new MenuNode(); n.caption = true; n.text = label; n.icon = icon; return n;
}
}
