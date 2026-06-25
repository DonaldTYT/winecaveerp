package com.uniinformation.utils;

public class TableRecException extends Exception
{
	final static int INVALID_FIELD_INDEX = 1;
	final static int INVALID_INSTANCE = 2;
	final static int INVALID_RECORD_POINTER = 3;
	final static int EMPTY_TABLE = 4;
	final static int INVALID_FIELD_NAME = 5;
	int errcode = 0;
	public TableRecException(int p_errcode, String msg) {
		super(msg);
		errcode = p_errcode;
	}
	public TableRecException(int p_errcode) {
		super();
		errcode = p_errcode;
	}
	String codeToString(int p_errcode) {
	   switch (p_errcode) {
		   case INVALID_FIELD_INDEX: return("INVALID_FIELD_INDEX");
		   case INVALID_INSTANCE: return("INVALID_INSTANCE");
		   case INVALID_RECORD_POINTER: return("INVALID_RECORD_POINTER");
		   case EMPTY_TABLE: return("EMPTY_TABLE");
		   case INVALID_FIELD_NAME: return("INVALID_FIELD_NAME");
		   default: return("UNKNOWN ERROR CODE "+p_errcode);
		}
	}
	public String toString() {
		return(
		   new StringBuffer()
				 .append(super.toString())
				 .append("Error Code : ")
				 .append(codeToString(errcode))
			    .toString()
		);
	}
}
