package com.uniinformation.wip;

import java.util.Date;

public interface WipStep extends Comparable{
	public int getType();
	public String getId();
	public String getDescription();
	public int getLevel();
	public int getOrder();
	public void setLevel(int level);
	public String toString();
	public String getColor();
	public String getFontColor();
}
