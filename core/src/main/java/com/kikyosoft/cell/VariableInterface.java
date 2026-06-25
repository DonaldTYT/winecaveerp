package com.kikyosoft.cell;
public interface VariableInterface {
	  public Object evalVariable(String p_varname) throws Exception;
	  public Object evalVariable(String p_varname, int p_idx) throws Exception;
	  public Object evalVariableRelative(String p_varname, int p_idx) throws Exception;
	}