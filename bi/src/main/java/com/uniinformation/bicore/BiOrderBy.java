package com.uniinformation.bicore;
import java.io.*;
import java.util.*;
import com.uniinformation.utils.*;
import com.uniinformation.cell.*;
public class BiOrderBy extends BiBase {
	Cell desc;
	int trFieldIdx;
	public BiOrderBy(BiView p_parent,BiColumn p_column,boolean p_desc) {
		super(p_parent);
		if(p_column != null) addCollection("column",p_column);
		desc = new Cell(p_desc);
	}
	public BiColumn getColumn() {
		return((BiColumn) getCollection("column"));
	}
	public boolean getDesc() {
		return(desc.getBoolean());
	}
}
