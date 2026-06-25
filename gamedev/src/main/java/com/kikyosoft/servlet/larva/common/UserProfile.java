package com.kikyosoft.servlet.larva.common;

public class UserProfile {
	  private final String displayName;
	  /** Use a path relative to the webapp root (no context path). We'll wrap with <c:url> in JSP. */
	  private final String avatarPath;
	  public UserProfile(String displayName, String avatarPath) {
	    this.displayName = displayName; this.avatarPath = avatarPath;
	  }
	  public String getDisplayName() { return displayName; }
	  public String getAvatarPath() { return avatarPath; }
}
