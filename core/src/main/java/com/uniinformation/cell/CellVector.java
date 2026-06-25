package com.uniinformation.cell;

import java.io.*;
import java.util.*;

public class CellVector<CellCollection> extends Vector
{
	static final long serialVersionUID = 5366931991944166555L;
	boolean notvalid=false;
	public CellVector(Collection c)
	{
		super(c);
	}
	public CellVector()
	{
		super();
	}
	public CellVector(boolean p_sw)
	{
		super();
		notvalid = ! p_sw;
	}
	public void setValid(boolean p_sw) {
		notvalid = !p_sw;
	}
	public Vector toStringVector()
	{
		Vector v = new Vector();
		for(int i = 0;i<size();i++) {
			v.add(get(i).toString());
		}
		return(v);
	}
}
