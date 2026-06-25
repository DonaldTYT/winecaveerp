package com.uniinformation.scorpion.driver;
import com.kyoko.common.CoreLog;
import java.sql.*;

public class ScpDbMeta implements DatabaseMetaData {
ScpConnection myConn;
public ScpDbMeta(ScpConnection c)
{
	myConn = c;
}
public boolean	allProceduresAreCallable() 
{
//				Retrieves whether the current user can call all the procedures returned by the method getProcedures.
	return(false);
}

public boolean	allTablesAreSelectable() 
{
//          Retrieves whether the current user can use all the tables returned by the method getTables in a SELECT statement.
				return(true);
}
public boolean	autoCommitFailureClosesAllResultSets() 
{
//          Retrieves whether a SQLException while autoCommit is true inidcates that all open ResultSets are closed, even ones that are holdable.
				return(true);
}
public boolean	dataDefinitionCausesTransactionCommit() 
{
//          Retrieves whether a data definition statement within a transaction forces the transaction to commit.
				return(false);
}
public boolean	dataDefinitionIgnoredInTransactions() 
{
//          Retrieves whether this database ignores a data definition statement within a transaction.
				return(false);
}
public boolean	deletesAreDetected(int type) 
{
//         Retrieves whether or not a visible row delete can be detected by calling the method ResultSet.rowDeleted.
				return(false);
}
public boolean	doesMaxRowSizeIncludeBlobs() 
{
//          Retrieves whether the return value for the method getMaxRowSize includes the SQL data types LONGVARCHAR and LONGVARBINARY.
				return(false);
}
public ResultSet	getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) 
{
//          Retrieves a description of the given attribute of the given type for a user-defined type (UDT) that is available in the given schema and catalog.
				// should implement
				CoreLog.log("DatabaseMetaData.getAttribute called");
				return(null);
}
public ResultSet	getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) 
{
//          Retrieves a description of a table's optimal set of columns that uniquely identifies a row.
				CoreLog.log("DatabaseMetaData.getBestRowItentifier called");
				return(null);
}
public ResultSet	getCatalogs() 
{
//          Retrieves the catalog names available in this database.
				CoreLog.log("DatabaseMetaData.getCatalog called");
				return(new ScpCatalogResultSet(myConn));
}
public String	getCatalogSeparator() 
{
//          Retrieves the String that this database uses as the separator between a catalog and table name.
				CoreLog.log("DatabaseMetaData.getCatalogSeparator called");
				return(null);
}
public String	getCatalogTerm() 
{
//          Retrieves the database vendor's preferred term for "catalog".
				CoreLog.log("DatabaseMetaData.getCatalogTerm called");
				return(null);
}
public ResultSet	getClientInfoProperties() 
{
//          Retrieves a list of the client info properties that the driver supports.
				CoreLog.log("DatabaseMetaData.getClientInfoProperties() called");
				return(null);
}
public ResultSet	getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) 
{
//          Retrieves a description of the access rights for a table's columns.
				CoreLog.log("DatabaseMetaData.getColumnPrivileges called");
				return(null);
}
public ResultSet	getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) 
{
//          Retrieves a description of table columns available in the specified catalog.
				CoreLog.log("DatabaseMetaData.getColumns called");
				return(null);
}
public Connection	getConnection() 
{
//          Retrieves the connection that produced this metadata object.
	return(myConn);
}
public ResultSet	getCrossReference(String parentCatalog, String parentSchema, String parentTable, String foreignCatalog, String foreignSchema, String foreignTable) 
{
// 			Retrieves a description of the foreign key columns in the given foreign key table that reference the primary key or the columns representing a unique constraint of the parent table (could be the same or a different table).
		CoreLog.log("Not Done:001");
		return(null);
}
public int	getDatabaseMajorVersion() 
{
//          Retrieves the major version number of the underlying database.
		return(0);
}
public int	getDatabaseMinorVersion() 
{
//          Retrieves the minor version number of the underlying database.
		return(0);
}
public String	getDatabaseProductName() 
{
//          Retrieves the name of this database product.
		return("scorpion");
}
public String	getDatabaseProductVersion() 
{
//          Retrieves the version number of this database product.
		return("1.0");
}
public int	getDefaultTransactionIsolation() 
{
//          Retrieves this database's default transaction isolation level.
		return(0);
}
public int	getDriverMajorVersion() 
{
//          Retrieves this JDBC driver's major version number.
		return(1);
}
public int	getDriverMinorVersion() 
{
//          Retrieves this JDBC driver's minor version number.
		return(0);
}
public String	getDriverName() 
{
//          Retrieves the name of this JDBC driver.
		return("scorpion");
}
public String	getDriverVersion() 
{
//          Retrieves the version number of this JDBC driver as a String.
		return("1.0");
}
public ResultSet	getExportedKeys(String catalog, String schema, String table) 
{
//          Retrieves a description of the foreign key columns that reference the given table's primary key columns (the foreign keys exported by a table).
		CoreLog.log("Not Done:002");
		return(null);
}
public String	getExtraNameCharacters() 
{
//          Retrieves all the "extra" characters that can be used in unquoted identifier names (those beyond a-z, A-Z, 0-9 and _).
		return("");
}
public ResultSet	getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) 
{
//          Retrieves a description of the given catalog's system or user function parameters and return type.
		CoreLog.log("Not Done:003");
	return(null);
}
public ResultSet	getFunctions(String catalog, String schemaPattern, String functionNamePattern) 
{
//          Retrieves a description of the system and user functions available in the given catalog.
		CoreLog.log("Not Done:004");
	return(null);
}
public String	getIdentifierQuoteString() 
{
//          Retrieves the string used to quote SQL identifiers.
		CoreLog.log("Not Done:005");
	return(null);
}
public ResultSet	getImportedKeys(String catalog, String schema, String table) 
{
//          Retrieves a description of the primary key columns that are referenced by the given table's foreign key columns (the primary keys imported by a table).
		CoreLog.log("Not Done:006");
	return(null);
}
public ResultSet	getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) 
{
//          Retrieves a description of the given table's indices and statistics.
		CoreLog.log("Not Done:007");
	return(null);
}
public int	getJDBCMajorVersion() 
{
//          Retrieves the major JDBC version number for this driver.
	return(1);
}
public int	getJDBCMinorVersion() 
{
//          Retrieves the minor JDBC version number for this driver.
	return(0);
}
public int	getMaxBinaryLiteralLength() 
{
//          Retrieves the maximum number of hex characters this database allows in an inline binary literal.
	return(1024);
}

public int	getMaxCatalogNameLength() 
{
//          Retrieves the maximum number of characters that this database allows in a catalog name.
	return(14);
}

public int	getMaxCharLiteralLength() 
{
//          Retrieves the maximum number of characters this database allows for a character literal.
	return(1024);
}
public int	getMaxColumnNameLength() 
{
//          Retrieves the maximum number of characters this database allows for a column name.
	return(18);
}
public int	getMaxColumnsInGroupBy() 
{
//          Retrieves the maximum number of columns this database allows in a GROUP BY clause.
	return(100);
}
public int	getMaxColumnsInIndex() 
{
// Retrieves the maximum number of columns this database allows in an index.
	return(5);
}
public int	getMaxColumnsInOrderBy() 
{
// Retrieves the maximum number of columns this database allows in an ORDER BY clause.
	return(100);
}
public int	getMaxColumnsInSelect() 
{
//          Retrieves the maximum number of columns this database allows in a SELECT list.
	return(256);
}
public int	getMaxColumnsInTable() 
{
//				Retrieves the maximum number of columns this database allows in a table.
	return(256);
}
public int	getMaxConnections() 
{
//          Retrieves the maximum number of concurrent connections to this database that are possible.
	return(256);
}
public int	getMaxCursorNameLength() 
{
//          Retrieves the maximum number of characters that this database allows in a cursor name.
	return(32);
}
public int	getMaxIndexLength() 
{
//          Retrieves the maximum number of bytes this database allows for an index, including all of the parts of the index.
	return(256);
}
public int	getMaxProcedureNameLength() 
{
//          Retrieves the maximum number of characters that this database allows in a procedure name.
	return(0);
}
public int	getMaxRowSize() 
{
//          Retrieves the maximum number of bytes this database allows in a single row.
	return(4096);
}
public int	getMaxSchemaNameLength() 
{
//          Retrieves the maximum number of characters that this database allows in a schema name.
	return(32);
}
public int	getMaxStatementLength() 
{
//          Retrieves the maximum number of characters this database allows in an SQL statement.
	return(32768);
}
public int	getMaxStatements() 
{
//          Retrieves the maximum number of active statements to this database that can be open at the same time.
	return(256);
}
public int	getMaxTableNameLength() 
{
//          Retrieves the maximum number of characters this database allows in a table name.
	return(18);
}
public int	getMaxTablesInSelect() 
{
//          Retrieves the maximum number of tables this database allows in a SELECT statement.
	return(256);
}
public int	getMaxUserNameLength() 
{
//          Retrieves the maximum number of characters this database allows in a user name.
	return(10);
}
public String	getNumericFunctions() 
{
//          Retrieves a comma-separated list of math functions available with this database.
	return("");
}
public ResultSet	getPrimaryKeys(String catalog, String schema, String table) 
{
//          Retrieves a description of the given table's primary key columns.
		CoreLog.log("Not Done:008");
	return(null);
}
public ResultSet	getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) 
{
//          Retrieves a description of the given catalog's stored procedure parameter and result columns.
		CoreLog.log("Not Done:009");
	return(null);
}
public ResultSet	getProcedures(String catalog, String schemaPattern, String procedureNamePattern) 
{
//          Retrieves a description of the stored procedures available in the given catalog.
		CoreLog.log("Not Done:010");
	return(null);
}
public String	getProcedureTerm() 
{
//          Retrieves the database vendor's preferred term for "procedure".
		CoreLog.log("Not Done:011");
	return(null);
}
public int	getResultSetHoldability() 
{
//          Retrieves this database's default holdability for ResultSet objects.
	return(0);
}
public RowIdLifetime	getRowIdLifetime() 
{
//          Indicates whether or not this data source supports the SQL ROWID type, and if so the lifetime for which a RowId object remains valid.
		CoreLog.log("Not Done:012");
	return(null);
}
public ResultSet	getSchemas() 
{
//          Retrieves the schema names available in this database.
		CoreLog.log("Not Done:013");
	return(null);
}
public ResultSet	getSchemas(String catalog, String schemaPattern) 
{
//          Retrieves the schema names available in this database.
		CoreLog.log("Not Done:014");
	return(null);
}
public String	getSchemaTerm() 
{
//          Retrieves the database vendor's preferred term for "schema".
	return("schema");
}
public String	getSearchStringEscape() 
{
//          Retrieves the string that can be used to escape wildcard characters.
		CoreLog.log("Not Done:015");
	return(null);
}
public String	getSQLKeywords() 
{
//          Retrieves a comma-separated list of all of this database's SQL keywords that are NOT also SQL:2003 keywords.
		CoreLog.log("Not Done:016");
	return(null);
}
public int	getSQLStateType() 
{
//          Indicates whether the SQLSTATE returned by SQLException.getSQLState is X/Open (now known as Open Group) SQL CLI or SQL:2003.
			 return(0);
}
public String	getStringFunctions() 
{
//          Retrieves a comma-separated list of string functions available with this database.
		CoreLog.log("Not Done:017");
	return(null);
}
public ResultSet	getSuperTables(String catalog, String schemaPattern, String tableNamePattern) 
{
//          Retrieves a description of the table hierarchies defined in a particular schema in this database.
		CoreLog.log("Not Done:018");
	return(null);
}
public ResultSet	getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) 
{
//          Retrieves a description of the user-defined type (UDT) hierarchies defined in a particular schema in this database.
		CoreLog.log("Not Done:019");
	return(null);
}
public String	getSystemFunctions() 
{
//          Retrieves a comma-separated list of system functions available with this database.
		CoreLog.log("Not Done:020");
	return(null);
}
public ResultSet	getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) 
{
//          Retrieves a description of the access rights for each table available in a catalog.
		CoreLog.log("Not Done:021");
	return(null);
}
public ResultSet	getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) 
{
//          Retrieves a description of the tables available in the given catalog.
	CoreLog.log("Make 022 Tables");
	/*
	DbmResultSet rs = new DbmResultSet(null);
	return(rs);
	*/
	try {
		ScpStatement stmt = new ScpStatement(myConn);
		String sqlstr = "select tabname TABLE_NAME from systables where tabid >= 100 and tabtype in ('XYZ'";
		for(String st : types) {
			if(st.toLowerCase().equals("table")) sqlstr += ",'T'";
			if(st.toLowerCase().equals("view")) sqlstr += ",'V'";
		}
		sqlstr += ")";
		ResultSet rs = stmt.executeQuery(sqlstr);
		return(rs);
	} catch (SQLException sex) {
		CoreLog.log(sex);
		return(null);
	}
}
public ResultSet	getTableTypes() 
{
//          Retrieves the table types available in this database.
		CoreLog.log("Make 023 TableTypes");
	DbmResultSet rs = new DbmResultSet(null);
	return(rs);
}
public String	getTimeDateFunctions() 
{
//          Retrieves a comma-separated list of the time and date functions available with this database.
	CoreLog.log("Make 024 TImeDateFunctions");
	return("");
}
public ResultSet	getTypeInfo() 
{
//          Retrieves a description of all the data types supported by this database.
	CoreLog.log("Make 025 TypeInfo");
	DbmResultSet rs = new DbmResultSet(null);
	return(rs);
}
public ResultSet	getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) 
{
//          Retrieves a description of the user-defined types (UDTs) defined in a particular schema.
		CoreLog.log("Not Done:026");
  return(null);
}
public String	getURL() 
{
//          Retrieves the URL for this DBMS.
  return("scorpion_db");
}
public String	getUserName() 
{
//          Retrieves the user name as known to this database.
  return("hlv");
}
public ResultSet	getVersionColumns(String catalog, String schema, String table) 
{
//          Retrieves a description of a table's columns that are automatically updated when any value in a row is updated.
		CoreLog.log("Not Done:027");
  return(null);
}
public boolean	insertsAreDetected(int type) 
{
//          Retrieves whether or not a visible row insert can be detected by calling the method ResultSet.rowInserted.
	return(false);
}
public boolean	isCatalogAtStart() 
{
//          Retrieves whether a catalog appears at the start of a fully qualified table name.
	return(false);
}
public boolean	isReadOnly() 
{
//          Retrieves whether this database is in read-only mode.
	return(false);
}
public boolean	locatorsUpdateCopy() 
{
//          Indicates whether updates made to a LOB are made on a copy or directly to the LOB.
	return(false);
}
public boolean	nullPlusNonNullIsNull() 
{
//          Retrieves whether this database supports concatenations between NULL and non-NULL values being NULL.
	return(true);
}
public boolean	nullsAreSortedAtEnd() 
{
//          Retrieves whether NULL values are sorted at the end regardless of sort order.
	return(false);
}
public boolean	nullsAreSortedAtStart() 
{
//          Retrieves whether NULL values are sorted at the start regardless of sort order.
	return(true);
}
public boolean	nullsAreSortedHigh() 
{
//          Retrieves whether NULL values are sorted high.
	return(false);
}
public boolean	nullsAreSortedLow() 
{
//         Retrieves whether NULL values are sorted low.
	return(true);
}
public boolean	othersDeletesAreVisible(int type) 
{
//          Retrieves whether deletes made by others are visible.
	return(true);
}
public boolean	othersInsertsAreVisible(int type) 
{
//          Retrieves whether inserts made by others are visible.
	return(true);
}
public boolean	othersUpdatesAreVisible(int type) 
{
//          Retrieves whether updates made by others are visible.
	return(true);
}
public boolean	ownDeletesAreVisible(int type) 
{
//          Retrieves whether a result set's own deletes are visible.
	return(true);
}
public boolean	ownInsertsAreVisible(int type) 
{
//         Retrieves whether a result set's own inserts are visible.
	return(true);
}
public boolean	ownUpdatesAreVisible(int type) 
{
//          Retrieves whether for the given type of ResultSet object, the result set's own updates are visible.
	return(true);
}
public boolean	storesLowerCaseIdentifiers() 
{
//          Retrieves whether this database treats mixed case unquoted SQL identifiers as case insensitive and stores them in lower case.
	return(false);
}
public boolean	storesLowerCaseQuotedIdentifiers() 
{
//          Retrieves whether this database treats mixed case quoted SQL identifiers as case insensitive and stores them in lower case.
	return(false);
}
public boolean	storesMixedCaseIdentifiers() 
{
//          Retrieves whether this database treats mixed case unquoted SQL identifiers as case insensitive and stores them in mixed case.
	return(true);
}
public boolean	storesMixedCaseQuotedIdentifiers() 
{
//          Retrieves whether this database treats mixed case quoted SQL identifiers as case insensitive and stores them in mixed case.
	return(true);
}
public boolean	storesUpperCaseIdentifiers() 
{
//          Retrieves whether this database treats mixed case unquoted SQL identifiers as case insensitive and stores them in upper case.
	return(false);
}
public boolean	storesUpperCaseQuotedIdentifiers() 
{
//          Retrieves whether this database treats mixed case quoted SQL identifiers as case insensitive and stores them in upper case.
	return(false);
}
public boolean	supportsAlterTableWithAddColumn() 
{
//          Retrieves whether this database supports ALTER TABLE with add column.
		return(false);
}
public boolean	supportsAlterTableWithDropColumn() 
{
//          Retrieves whether this database supports ALTER TABLE with drop column.
		return(false);
}
public boolean	supportsANSI92EntryLevelSQL() 
{
//          Retrieves whether this database supports the ANSI92 entry level SQL grammar.
		return(true);
}
public boolean	supportsANSI92FullSQL() 
{
//          Retrieves whether this database supports the ANSI92 full SQL grammar supported.
		return(true);
}
public boolean	supportsANSI92IntermediateSQL() 
{
//          Retrieves whether this database supports the ANSI92 intermediate SQL grammar supported.
		return(true);
}
public boolean	supportsBatchUpdates() 
{
//          Retrieves whether this database supports batch updates.
		return(true);
}
public boolean	supportsCatalogsInDataManipulation() 
{
//          Retrieves whether a catalog name can be used in a data manipulation statement.
		return(false);
}
public boolean	supportsCatalogsInIndexDefinitions() 
{
//          Retrieves whether a catalog name can be used in an index definition statement.
		return(false);
}
public boolean	supportsCatalogsInPrivilegeDefinitions() 
{
//          Retrieves whether a catalog name can be used in a privilege definition statement.
		return(false);
}
public boolean	supportsCatalogsInProcedureCalls() 
{
//          Retrieves whether a catalog name can be used in a procedure call statement.
		return(false);
}
public boolean	supportsCatalogsInTableDefinitions() 
{
//          Retrieves whether a catalog name can be used in a table definition statement.
		return(false);
}
public boolean	supportsColumnAliasing() 
{
//          Retrieves whether this database supports column aliasing.
		return(true);
}
public boolean	supportsConvert() 
{
//          Retrieves whether this database supports the JDBC scalar function CONVERT for the conversion of one JDBC type to another.
		return(false);
}
public boolean	supportsConvert(int fromType, int toType) 
{
//          Retrieves whether this database supports the JDBC scalar function CONVERT for conversions between the JDBC types fromType and toType.
		return(false);
}
public boolean	supportsCoreSQLGrammar()  
{
//         Retrieves whether this database supports the ODBC Core SQL grammar.
		return(true);
}
public boolean	supportsCorrelatedSubqueries() 
{
//          Retrieves whether this database supports correlated subqueries.
		return(true);
}
public boolean	supportsDataDefinitionAndDataManipulationTransactions() 
{
//          Retrieves whether this database supports both data definition and data manipulation statements within a transaction.
		return(false);
}
public boolean	supportsDataManipulationTransactionsOnly() 
{
//          Retrieves whether this database supports only data manipulation statements within a transaction.
		return(true);
}
public boolean	supportsDifferentTableCorrelationNames() 
{
//          Retrieves whether, when table correlation names are supported, they are restricted to being different from the names of the tables.
		return(true);
}
public boolean	supportsExpressionsInOrderBy() 
{
//          Retrieves whether this database supports expressions in ORDER BY lists.
		return(false);
}
public boolean	supportsExtendedSQLGrammar() 
{
//          Retrieves whether this database supports the ODBC Extended SQL grammar.
		return(false);
}
public boolean	supportsFullOuterJoins() 
{
//          Retrieves whether this database supports full nested outer joins.
		return(true);
}
public boolean	supportsGetGeneratedKeys() 
{
//          Retrieves whether auto-generated keys can be retrieved after a statement has been executed
		return(false);
}
public boolean	supportsGroupBy() 
{
//          Retrieves whether this database supports some form of GROUP BY clause.
		return(true);
}
public boolean	supportsGroupByBeyondSelect() 
{
//          Retrieves whether this database supports using columns not included in the SELECT statement in a GROUP BY clause provided that all of the columns in the SELECT statement are included in the GROUP BY clause.
		return(false);
}
public boolean	supportsGroupByUnrelated() 
{
//          Retrieves whether this database supports using a column that is not in the SELECT statement in a GROUP BY clause.
		return(false);
}
public boolean	supportsIntegrityEnhancementFacility() 
{
//          Retrieves whether this database supports the SQL Integrity Enhancement Facility.
		return(false);
}
public boolean	supportsLikeEscapeClause() 
{
//         Retrieves whether this database supports specifying a LIKE escape clause.
		return(false);
}
public boolean	supportsLimitedOuterJoins() 
{
//          Retrieves whether this database provides limited support for outer joins.
		return(true);
}
public boolean	supportsMinimumSQLGrammar() 
{
//          Retrieves whether this database supports the ODBC Minimum SQL grammar.
		return(true);
}
public boolean	supportsMixedCaseIdentifiers() 
{
//          Retrieves whether this database treats mixed case unquoted SQL identifiers as case sensitive and as a result stores them in mixed case.
		return(true);
}
public boolean	supportsMixedCaseQuotedIdentifiers() 
{
//          Retrieves whether this database treats mixed case quoted SQL identifiers as case sensitive and as a result stores them in mixed case.
		return(true);
}
public boolean	supportsMultipleOpenResults() 
{
//          Retrieves whether it is possible to have multiple ResultSet objects returned from a CallableStatement object simultaneously.
		return(false);
}
public boolean	supportsMultipleResultSets() 
{
//          Retrieves whether this database supports getting multiple ResultSet objects from a single call to the method execute.
		return(false);
}
public boolean	supportsMultipleTransactions() 
{
//          Retrieves whether this database allows having multiple transactions open at once (on different connections).
		return(false);
}
public boolean	supportsNamedParameters() 
{
//          Retrieves whether this database supports named parameters to callable statements.
		return(false);
}
public boolean	supportsNonNullableColumns() 
{
//          Retrieves whether columns in this database may be defined as non-nullable.
		return(false);
}
public boolean	supportsOpenCursorsAcrossCommit() 
{
//         Retrieves whether this database supports keeping cursors open across commits.
		return(false);
}
public boolean	supportsOpenCursorsAcrossRollback() 
{
//         Retrieves whether this database supports keeping cursors open across rollbacks.
		return(false);
}
public boolean	supportsOpenStatementsAcrossCommit() 
{
//          Retrieves whether this database supports keeping statements open across commits.
		return(false);
}
public boolean	supportsOpenStatementsAcrossRollback() 
{
//          Retrieves whether this database supports keeping statements open across rollbacks.
		return(false);
}
public boolean	supportsOrderByUnrelated() 
{
//          Retrieves whether this database supports using a column that is not in the SELECT statement in an ORDER BY clause.
		return(false);
}
public boolean	supportsOuterJoins() 
{
//          Retrieves whether this database supports some form of outer join.
		return(true);
}
public boolean	supportsPositionedDelete() 
{
//          Retrieves whether this database supports positioned DELETE statements.
		return(false);
}
public boolean	supportsPositionedUpdate() 
{
//          Retrieves whether this database supports positioned UPDATE statements.
		return(false);
}
public boolean	supportsResultSetConcurrency(int type, int concurrency) 
{
//          Retrieves whether this database supports the given concurrency type in combination with the given result set type.
		return(false);
}
public boolean	supportsResultSetHoldability(int holdability) 
{
//          Retrieves whether this database supports the given result set holdability.
		return(false);
}
public boolean	supportsResultSetType(int type)  {
//          Retrieves whether this database supports the given result set type.
		return(false);
}
public boolean	supportsSavepoints() 
{
//          Retrieves whether this database supports savepoints.
		return(false);
}
public boolean	supportsSchemasInDataManipulation() 
{
//          Retrieves whether a schema name can be used in a data manipulation statement.
		return(false);
}
public boolean	supportsSchemasInIndexDefinitions() 
{
//          Retrieves whether a schema name can be used in an index definition statement.
		return(false);
}
public boolean	supportsSchemasInPrivilegeDefinitions() 
{
//          Retrieves whether a schema name can be used in a privilege definition statement.
		return(false);
}
public boolean	supportsSchemasInProcedureCalls() 
{
//          Retrieves whether a schema name can be used in a procedure call statement.
		return(false);
}
public boolean	supportsSchemasInTableDefinitions() 
{
//          Retrieves whether a schema name can be used in a table definition statement.
		return(false);
}
public boolean	supportsSelectForUpdate() 
{
//          Retrieves whether this database supports SELECT FOR UPDATE statements.
		return(false);
}
public boolean	supportsStatementPooling() 
{
//         Retrieves whether this database supports statement pooling.
		return(false);
}
public boolean	supportsStoredFunctionsUsingCallSyntax() 
{
//          Retrieves whether this database supports invoking user-defined or vendor functions using the stored procedure escape syntax.
		return(false);
}
public boolean	supportsStoredProcedures() 
{
//          Retrieves whether this database supports stored procedure calls that use the stored procedure escape syntax.
		return(false);
}
public boolean	supportsSubqueriesInComparisons() 
{
//          Retrieves whether this database supports subqueries in comparison expressions.
		return(false);
}
public boolean	supportsSubqueriesInExists() 
{
//          Retrieves whether this database supports subqueries in EXISTS expressions.
		return(true);
}
public boolean	supportsSubqueriesInIns() 
{
//          Retrieves whether this database supports subqueries in IN expressions.
		return(true);
}
public boolean	supportsSubqueriesInQuantifieds() 
{
//        Retrieves whether this database supports subqueries in quantified expressions.
		return(true);
}
public boolean	supportsTableCorrelationNames() 
{
//          Retrieves whether this database supports table correlation names.
		return(true);
}
public boolean	supportsTransactionIsolationLevel(int level) 
{
//          Retrieves whether this database supports the given transaction isolation level.
		return(false);
}
public boolean	supportsTransactions() 
{
//          Retrieves whether this database supports transactions.
		return(true);
}
public boolean	supportsUnion() 
{
//         Retrieves whether this database supports SQL UNION.
		return(false);
}
public boolean	supportsUnionAll() 
{
//          Retrieves whether this database supports SQL UNION ALL.
		return(false);
}
public boolean	updatesAreDetected(int type) 
{
//         Retrieves whether or not a visible row update can be detected by calling the method ResultSet.rowUpdated.
		return(false);
}

public boolean	usesLocalFilePerTable() 
{
//          Retrieves whether this database uses a file for each table.
		return(true);
}
public boolean	usesLocalFiles() 
{
//          Retrieves whether this database stores tables in a local file.
		return(true);
}
	 public boolean isWrapperFor(Class<?> iface) throws SQLException {
		 throw(new SQLException("Not yet implemented"));
	 }
	 public <T> T unwrap(Class<T> iface) throws SQLException {
		 throw(new SQLException("Not yet implemented"));
	 }
	 
	 public boolean generatedKeyAlwaysReturned() {
		 return(true);
	 }
	 public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) {
		 return(null);
	 }
	 
} 
