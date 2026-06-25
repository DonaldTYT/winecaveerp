package com.uniinformation.scorpion.driver;
import java.sql.*;

public class ScpSQLException extends SQLException {
   int sqlcode = -2;
   public ScpSQLException() {
	   super();
	}
   public ScpSQLException(String p_reason) {
	   super(p_reason);
	}
   public ScpSQLException(String p_reason, String p_state) {
	   super(p_reason+":"+p_state);
	}
   public ScpSQLException(String p_reason, String p_state, int p_vendorCode) {
	   super(p_reason+":"+p_state+":"+p_vendorCode);
	   sqlcode = p_vendorCode;
	}
   
   public int getErrorCode() {
	   return(sqlcode);
	   
   }
}
