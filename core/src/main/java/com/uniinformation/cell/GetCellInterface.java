package com.uniinformation.cell;

import java.util.*;

public interface GetCellInterface 
{
	public Cell getCell(String p_cellName);
	public Cell getCellArray(String p_cellName,int p_idx);
	public CellCollection getCollection(String p_cellName);
	public Vector getCollectionList(String p_cellName);
}
