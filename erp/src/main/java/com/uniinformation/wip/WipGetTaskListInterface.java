package com.uniinformation.wip;

import java.util.Date;

public interface WipGetTaskListInterface{
	public int getCount();
	public Date getStart(int p_idx);
	public Date getEnd(int p_idx);
	public String getId(int p_idx);
	public String getName(int p_idx);
	public String getTitle(int p_idx);
}
