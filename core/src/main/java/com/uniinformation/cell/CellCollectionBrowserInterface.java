package com.uniinformation.cell;

import java.util.*;

public interface CellCollectionBrowserInterface 
{
	public void gotCell(String p_cellName, Cell p_cell) throws Exception;
	public void gotCellArray(String p_cellName, Cell[] p_cellArray) throws Exception;
	public void gotCollection(String p_cellName, CellCollection p_collection) throws Exception;
	public void gotCollectionList(String p_cellName, Vector p_collectionList) throws Exception;
}
