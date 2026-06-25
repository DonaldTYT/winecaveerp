package com.uniinformation.scorpion.driver;
import com.kyoko.common.CoreLog;
import java.sql.*;

public class ScpCacheStmtXX extends ScpStatement {

	 /*
	 public ScpCacheStmt(ScpConnection p_connection)
	 {
	 	super(p_connection);
	 }
	 */

	 protected void finalize()
	 {
	 	try {
			super.close();
		} catch (SQLException e) {
			CoreLog.log(e.getSQLState() + " Errcode:"+e.getErrorCode());
			CoreLog.log(e);
		}
	 }

	 public ScpCacheStmtXX(ScpConnection p_connection,String p_sqlstring) throws SQLException
	 {
	 	super(p_connection,p_sqlstring);
	 }
	 public void Scorpion_openstmt() throws SQLException {
	 	CoreLog.logClass(this,"ScpCacheStmt.Open " + statementid + " " +sqlstring);
		super.Scorpion_openstmt();
	 }
	 public void close() throws SQLException
	 {
	 	CoreLog.logClass(this,"ScpCacheStmt.Close " + statementid + " " +sqlstring);
		closeForReuse();
		//currconnection.hashStatement(sqlstring,this);
		currconnection.hashStatement(this);
	 }
}	
