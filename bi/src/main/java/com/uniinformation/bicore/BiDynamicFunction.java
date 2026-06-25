package com.uniinformation.bicore;

import java.util.List;
import java.util.Vector;

public abstract class BiDynamicFunction{
	public BiDynamicFunction (BiCellCollection p_col) {
		
	}
	abstract protected Object eval(List p_args);
}
