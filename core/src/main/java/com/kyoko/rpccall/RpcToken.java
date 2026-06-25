package com.kyoko.rpccall;

public class RpcToken
{
	private int idx;
	private String str;

	public RpcToken( String p_str, int p_idx)
	{
		idx = p_idx;
		str = p_str;
	}
	public int getIndex()
	{
		return(idx);
	}
	public void setIndex(int p_idx)
	{
		idx = p_idx;
	}
	public String getString()
	{
		return(str);
	}
}
