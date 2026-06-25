package com.uniinformation.bicore;
import java.io.*;
import java.util.*;
import java.sql.*;
import com.uniinformation.utils.*;
import com.uniinformation.cell.*;

public class BiBase extends CellCollection {
	public BiBase(CellCollection parent)
	{
		super(parent);
	}
	public BiTable getTable(String p_tabname)
	{
		CellCollection c;
		for(c = this;c != null;c = c.getParent()) {
			if(c instanceof BiSchema) return(((BiSchema) c).getTable(p_tabname));
		}
		return(null);
	}
	public BiSchema getSchema()
	{
		CellCollection c;
		for(c = this;c != null;c = c.getParent()) {
			if(c instanceof BiSchema) return((BiSchema) c);
		}
		return(null);
	}
	/*
	public String getLoginId()
	{
		CellCollection c;
		for(c = this;c != null;c = c.getParent()) {
			if(c instanceof BiSchema) {
				BiSchema sch = (BiSchema) c;
				if(sch.userid != null) return(sch.userid.getString()) ; else return(null);
			}
		}
		return(null);
	}
	*/
	public Connection getConn()
	{
		CellCollection c;
		for(c = this;c != null;c = c.getParent()) {
			if(c instanceof BiSchema) return(((BiSchema) c).getConn.getConnection());
		}
		return(null);
	}
	public GetConnectionInterface getConnInterface()
	{
		CellCollection c;
		for(c = this;c != null;c = c.getParent()) {
			if(c instanceof BiSchema) return(((BiSchema) c).getConn);
		}
		return(null);
	}
}
