package com.kyoko.parser;

import com.uniinformation.cell.Cell;
import com.uniinformation.cell.CellException;

public interface VariableInterface {
  public String toString(String p_varName,int p_idx,boolean p_idxAbsolute);
  public Object collectObject(String p_varName,int p_idx,boolean p_idxAbsolute) throws CellException;
  public Cell evalVariable(String p_varName,int p_idx,boolean p_idxAbsolute,Object p_recData) throws CellException;
  public int getDataType(String p_varName);
}
