package com.uniinformation.utils.oexprpar;

import java.util.List;
import java.util.Vector;

import com.uniinformation.cell.CellException;

public interface FunctionInterface {
  public Object evalFunction(String p_functName, Vector p_args) throws Exception;
}
