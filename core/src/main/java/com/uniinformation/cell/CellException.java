package com.uniinformation.cell;
import java.io.*;
import java.util.*;
import java.text.*;
public class CellException extends Exception {
	static public final int CELLEXCEPTION_RECURSIVE = 1;
	static public final int CELLEXCEPTION_RECORD_NOT_FOUND = 2;
	static public final int CELLEXCEPTION_EVAL_ERROR = 3;
	int exceptionType = 0;
	public CellException(String s)
	{
		super(s);
	}
	public CellException(String s,int p_type)
	{
		super(s);
		exceptionType = p_type;
	}
	public int getExceptionType() {
		return(exceptionType);
	}
}
