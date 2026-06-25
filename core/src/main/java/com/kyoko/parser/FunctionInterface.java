package com.kyoko.parser;
import java.util.List;
import java.util.Vector;

import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;
public interface FunctionInterface {
  public String toString(String p_functName, List p_args);
  public Object collectObject(String p_functName, List p_args) throws CellException;
  public Cell evalFunction(String p_functName, Vector p_args,Object p_data) throws Exception;
  public int getDataType(String p_functName);
}
